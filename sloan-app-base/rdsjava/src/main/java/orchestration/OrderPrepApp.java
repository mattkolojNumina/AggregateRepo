/*
 * OrderPrepApp.java
 * 
 * Import/prep orders and pick lines from host data.
 * 
 * For the Candy.com pick/pack/ship system
 * 
 * (c) 2019, Numina Group, Inc.
 */

package orchestration;

import java.util.*;

import polling.*;
import rds.*;
import dao.AbstractDAO;
import dao.SloaneCommonDAO;
import dao.CustShipmentDAO;
import dao.RdsWaveDAO;
import static sloane.SloaneConstants.*;

import static rds.RDSLog.*;

public class OrderPrepApp
      extends AbstractPollingApp {

   private static final String DEFAULT_ID = "orderPrep";
   private SloaneCommonDAO sloaneCommonDao;
   private String shipmentType = "";
   private String orderType = "";
   private String orderId = "";
   private Map<String,String> controlMap;

   public OrderPrepApp( String id, String rdsDb ) {
      super( id, rdsDb );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		AbstractDAO.setDatabase(db);
		RdsWaveDAO.setDatabase(db);
		sloaneCommonDao = new SloaneCommonDAO();
		controlMap = db.getControlMap("system");
   }

   protected void poll() {
      pollOrderPrep();
      checkWavePrep();
      //checkShipmentPrep();
   }
   
   private void checkWavePrep() {
      long start = System.currentTimeMillis();
      List<Map<String,String>> waves = null;
      try {
      	waves = getWaves();
      } catch (ProcessingException ex) {
         alert( "proc error: %s", ex.getMessage() );
      }
      if (waves == null || waves.isEmpty())
         return;

      inform( "%d wave(s) require prepare check", waves.size() );
      for (Map<String,String> w : waves)
         checkWave( w );

      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   } 

   private void checkWave( Map<String,String> w ) {
   	int waveSeq = getMapInt(w,"waveSeq");
		int total = getMapInt(w,"total");
		int prepared = getMapInt(w,"prepared");
		int canceled = getMapInt(w,"canceled");
		int error = getMapInt(w,"error");
		if (total == canceled ) {
			inform("wave %d is canceled", waveSeq);	
			RdsWaveDAO.setTombstone(waveSeq, "cancelStamp");
			SloaneCommonDAO.postWaveLog(""+waveSeq, id, "wave is canceled because all the orders are canceled");
		} else if( total == (prepared+canceled+error) && prepared>0 ) {
			inform("wave %d is prepared", waveSeq);
			RdsWaveDAO.setTombstone(waveSeq, "prepareStamp");
			SloaneCommonDAO.postWaveLog(""+waveSeq, id, "wave is prepared");
		} 
   }
   
   private List<Map<String,String>> getWaves()
         throws ProcessingException {
      List<Map<String,String>> waves = db.getResultMapList(
      		"SELECT w.waveSeq, COUNT(orderId) AS total, "
				+ "COUNT(CASE WHEN o.prepareStamp IS NOT NULL AND o.cancelStamp IS NULL THEN orderId END) as prepared,"
				+ "COUNT(CASE WHEN o.cancelStamp IS NOT NULL THEN orderId END) as canceled, "
				+ "COUNT(CASE WHEN o.status='error' THEN orderId END) as error "
				+ "FROM rdsWaves w JOIN custOrders o USING(waveSeq) "
				+ "WHERE w.createStamp IS NOT NULL AND w.prepareStamp IS NULL AND w.cancelStamp IS NULL "
				+ "GROUP BY w.waveSeq");
      if (waves == null)
         throw new ProcessingException( "error while retrieving waves" );
      return waves;
   }
   
   private void checkShipmentPrep() {
      long start = System.currentTimeMillis();
      List<Map<String,String>> shipments = null;
      try {
      	shipments = getShipments();
      } catch (ProcessingException ex) {
         alert( "proc error: %s", ex.getMessage() );
      }
      if (shipments == null || shipments.isEmpty())
         return;

      inform( "%d shipment(s) require prep...", shipments.size() );
      for (Map<String,String> s : shipments)
         checkShipment( s );

      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   }
   
   private void checkShipment( Map<String,String> s ) {
   	String shipmentId = getMapStr(s,"shipmentId");
		int total = getMapInt(s,"total");
		int prepared = getMapInt(s,"prepared");
		int canceled = getMapInt(s,"canceled");
		int error = getMapInt(s,"error");
		if (total == canceled ) {
			inform("ShipmentId %s is canceled", shipmentId);
			db.execute("UPDATE custShipments SET status='canceled', cancelStamp=NOW() WHERE shipmentId='%s'", shipmentId);	
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "shipment is canceled because all the orders are canceled");
		} else if( total == (prepared+canceled) ) {
			inform("ShipmentId %s is prepared", shipmentId);
			db.execute("UPDATE custShipments SET status='prepared', prepareStamp=NOW() WHERE shipmentId='%s'", shipmentId);
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "shipment is prepared");
		} else if( total == error ) {
			inform("ShipmentId %s has all orders in error",shipmentId);
			db.execute("UPDATE custShipments SET status='error' WHERE shipmentId='%s'", shipmentId);
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "marked as error");
		}
   }
   
   private List<Map<String,String>> getShipments()
         throws ProcessingException {
      List<Map<String,String>> shipments = db.getResultMapList(
      		"SELECT s.shipmentId, COUNT(orderId) AS total, "
				+ "COUNT(CASE WHEN o.prepareStamp IS NOT NULL AND o.cancelStamp IS NULL THEN orderId END) as prepared,"
				+ "COUNT(CASE WHEN o.cancelStamp IS NOT NULL THEN orderId END) as canceled, "
				+ "COUNT(CASE WHEN o.status='error' THEN orderId END) as error "
				+ "FROM custShipments s JOIN custOrders o USING(shipmentId) WHERE s.status='downloaded' "
				+ "GROUP BY s.shipmentId");
      if (shipments == null)
         throw new ProcessingException( "error while retrieving shipments" );
      return shipments;
   }

   /*
    * O R D E R
    */

   private void pollOrderPrep() {
      long start = System.currentTimeMillis();
      List<Map<String,String>> orders = null;
      try {
         orders = getOrders();
      } catch (ProcessingException ex) {
         alert( "proc error: %s", ex.getMessage() );
      }
      if (orders == null || orders.isEmpty())
         return;

      inform( "%d order(s) require prep...", orders.size() );
      for (Map<String,String> o : orders)
         prepOrder( o );

      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   }

   private List<Map<String,String>> getOrders()
         throws ProcessingException {
      List<Map<String,String>> orders = db.getResultMapList(
            "SELECT * FROM custOrders " +
            "WHERE status='downloaded' " +
            "ORDER BY waveSeq, downloadStamp" );
      if (orders == null)
         throw new ProcessingException( "error while retrieving orders" );
      return orders;
   }

   private void prepOrder( Map<String,String> o ) {
      long start = System.currentTimeMillis();
      orderId = getMapStr( o, "orderId" );
      shipmentType = "";
      orderType = "";
      try {
         doPrepOrder( o );
         updateOrderPrep();
      } catch (DataException ex) {
         updateOrderError( ex.getMessage() );
      } catch (ProcessingException ex) {
         updateOrderError( ex.getMessage() );
      } catch (Exception ex) {
         ex.printStackTrace();
         String msg = "internal processing error";
         updateOrderError( msg );
      }
      long dt = System.currentTimeMillis() - start;
      if (dt > getMaxTime())
         inform( "prep order took %d msec", dt );
   }

   private void doPrepOrder( Map<String,String> o )
         throws DataException, ProcessingException {
      trace( "prep orderId %s start",orderId );
      //checkShipInfo( o );
      checkCustomer( o );
      checkEcomTruck( o );
      if( controlParameterYes("requireCreatingPicks") ) {
      	int totalPick = createPicks();
      	if( totalPick == 0 )
      		throw new DataException( "No pick created" );
      }
   }
   
   private void checkCustomer( Map<String,String> o )
         throws DataException, ProcessingException {
   	String customerNumber = getMapStr(o,"customerNumber");
   	boolean exist = SloaneCommonDAO.stringIdExistInTable("custCustomers", "customerNumber", customerNumber);
   	if( !exist ) 
   		throw new DataException( "Unknow customer" );
   	
   }   
   
   private void checkEcomTruck( Map<String,String> o ) {
   	String truckNumber = getMapStr(o,"truckNumber");
   	boolean exist = SloaneCommonDAO.stringIdExistInTable("cfgEcomTruckNumber", "truckNumber", truckNumber);
   	if( exist ) 
   		orderType = ORDERTYPE_ECOM;
   	else {
   		String customerNumber = getMapStr(o,"customerNumber");
      	sloaneCommonDao.setRecordMapByStringId("custCustomers", "customerNumber", customerNumber);
      	int exportFlag = sloaneCommonDao.getRecordMapInt("exportFlag");
   		String toteOrBox = getMapStr(o,"toteOrBox");
   		orderType = exportFlag==1? ORDERTYPE_EXPORT:
   			         toteOrBox.equals(TOTEORBOX_BOX)? ORDERTYPE_BOX: ORDERTYPE_TOTE;
   	}
   }
   		
   
   private void checkShipInfo( Map<String,String> o )
         throws DataException, ProcessingException {
   	String shipmentId = getMapStr(o,"shipmentId");
   	CustShipmentDAO dao = new CustShipmentDAO(shipmentId);
   	Map<String,String> shipmentMap = dao.getShipmentMap();
   	if( shipmentMap == null || shipmentMap.isEmpty() ) 
   		throw new DataException( "No shipment info" );
   	String shippingMethod = dao.getRecordMapStr("shippingMethod");
   	sloaneCommonDao.setRecordMapByStringId("cfgShipMethods", "shipMethod", shippingMethod);
   	String shipType = sloaneCommonDao.getRecordMapStr("shipType");
   	/*
   	if(shipType.equals("LTL")) {
   		shipmentType = ORDERTYPE_LTL;
   		orderType = ORDERTYPE_LTL;
   	} else if(shipType.equals(SHIPMENTTYPE_PARCEL)){
   		shipmentType = SHIPMENTTYPE_PARCEL;
   		orderType = ORDERTYPE_PARCEL;
   	} else
   		throw new DataException( "Invalid shippingMethod" );
   	*/
   	db.execute("UPDATE custShipments SET shipmentType='%s' WHERE shipmentId='%s'", shipmentType,shipmentId);
   	if( SHIPMENTTYPE_LTL.equals(shipmentType) ) {
   		if( controlParameterYes("requireLTLShippingMethodCheck") && !SloaneCommonDAO.stringIdExistInTable("cfgShipMethods","shipMethod",shippingMethod))
   			throw new DataException( "Invalid shippingMethod" );
   		if( controlParameterYes("requireLTLShipToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"shipToShippingInfoSeq")))
   			throw new DataException( "Invalid shipTo info" );
   		if( controlParameterYes("requireLTLShipFromCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"shipFromShippingInfoSeq")))
   			throw new DataException( "Invalid shipFrom info" );   	
   		if( controlParameterYes("requireLTLReturnToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"returnToShippingInfoSeq")))
   			throw new DataException( "Invalid returnTo info" );     
   		if( controlParameterYes("requireLTLSoldToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"soldToShippingInfoSeq")))
   			throw new DataException( "Invalid soldTo info" );
   		if( controlParameterYes("requireLTLBillingCheck") && !SloaneCommonDAO.isValidBillingInfo(getMapInt(shipmentMap,"billingInfoSeq")))
   			throw new DataException( "Invalid billing info" );
   	} else {
   		if( controlParameterYes("requireParcelShippingMethodCheck") && !SloaneCommonDAO.stringIdExistInTable("cfgShipMethods","shipMethod",shippingMethod))
   			throw new DataException( "Invalid shippingMethod" );
   		if( controlParameterYes("requireParcelShipToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"shipToShippingInfoSeq")))
   			throw new DataException( "Invalid shipTo info" );
   		if( controlParameterYes("requireParcelShipFromCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"shipFromShippingInfoSeq")))
   			throw new DataException( "Invalid shipFrom info" );   	
   		if( controlParameterYes("requireParcelReturnToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"returnToShippingInfoSeq")))
   			throw new DataException( "Invalid returnTo info" );     
   		if( controlParameterYes("requireParcelSoldToCheck") && !SloaneCommonDAO.isValidShippingInfo(getMapInt(shipmentMap,"soldToShippingInfoSeq")))
   			throw new DataException( "Invalid soldTo info" );
   		if( controlParameterYes("requireParcelBillingCheck") && !SloaneCommonDAO.isValidBillingInfo(getMapInt(shipmentMap,"billingInfoSeq")))
   			throw new DataException( "Invalid billing info" );   		
   	}
   	
   }

   private int createPicks()
         throws DataException, ProcessingException {
   	db.execute("DELETE FROM rdsPicks WHERE orderId='%s'", orderId);
   	List<Map<String,String>> lineItems = db.getResultMapList(
   			"SELECT custOrderLines.*,(`length`*width*height*shelfPackQty/cubicDivisorInt) AS shelfPackVolume FROM custOrderLines "
   			+ "JOIN custSkus USING(sku,uom) WHERE orderId='%s' AND status<>'canceled'", orderId); 
   	int totalPick = 0;
   	for( Map<String,String> lineItemMap : lineItems ){
   		int orderLineSeq = getMapInt(lineItemMap,"orderLineSeq");
   		String pageId = getMapStr(lineItemMap,"pageId"); 
   		String lineId = getMapStr(lineItemMap,"lineId"); 
   		String sku = getMapStr(lineItemMap,"sku");
   		String uom = getMapStr(lineItemMap,"uom");
   		int qty = (int) getMapDbl(lineItemMap,"qty");
   		int shelfPackQty = getMapInt(lineItemMap,"shelfPackQty");
   		String location = getMapStr(lineItemMap,"location");  
   		inform("create picks for sku(uom) %s/%s, location %s, qty %d", sku, uom, location, qty);
			if( !SloaneCommonDAO.pairIdExistInTable("custSkus","sku",sku,"uom",uom) ) 
				throw new DataException( String.format("PageId %s, LineId %s: sku/uom %s/%s not found", pageId, lineId, sku, uom) );
			if( qty == 0 )
				throw new DataException( String.format("PageId %s, LineId %s: invalid qty", pageId, lineId) );
			String pickType = SloaneCommonDAO.determinePickType(location);
			inform("PageId %s, LineId %s: sku/uom %s/%s pickType %s", pageId, lineId, sku, uom, pickType);
			if( pickType.equals(ERROR) ) {
				throw new DataException( String.format("LineId %s: unable to determine pick type", lineId) );
			}
			if( !SloaneCommonDAO.isValidLocation(location) && !pickType.equals(PICKTYPE_GEEK) ) 
				throw new DataException( String.format("PageId %s, LineId %s: location %s not found", pageId, lineId, location) );
			String qroFlag = db.getString("", "SELECT qroFlag FROM custSkus WHERE sku='%s' AND uom='%s'", sku,uom);
			boolean isMarkedOutSku = SloaneCommonDAO.isMarkedOutSku(sku);
			//boolean createShelfPackPick = controlParameterYes("createShelfPackPick") && SloaneCommonDAO.createShelfPackPick(pickType,qroFlag);
			boolean createShelfPackPick = SloaneCommonDAO.createShelfPackPick(pickType,qroFlag);
			if( !createShelfPackPick || shelfPackQty == 1 ) {
				for( int i=0; i<qty; i++ )
					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,uom,1,pickType,isMarkedOutSku);		
				totalPick += qty;
			} else {
				int shelfPackCount = qty/shelfPackQty;
				totalPick += shelfPackCount;
				for( int i=0; i<shelfPackCount; i++ )
					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,UOM_SHELFPACK,shelfPackQty,pickType,isMarkedOutSku);
				int salesUomCount = qty%shelfPackQty;
				totalPick += salesUomCount;
				for( int i=0; i<salesUomCount; i++ )
					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,uom,1,pickType,isMarkedOutSku);				
			}
			if( isMarkedOutSku )
				db.execute("UPDATE custOrderLines SET status='short',pickStamp=NOW(),labelStamp=NOW() "
						+ "WHERE orderLineSeq=%d",orderLineSeq);
   	}
   	return totalPick;
   }  

   private void updateOrderPrep() {
		db.execute(
				"UPDATE custOrders SET orderType='%s', status='prepared', prepareStamp=NOW(), errorMsg='' "
				+ "WHERE orderId='%s'",
				orderType, orderId);
		SloaneCommonDAO.postOrderLog(orderId,id,"prep order successfully");
      RDSCounter.increment(COUNTER_ORDER_PREP_SUCCESS);  
   }
   
   private void updateOrderError( String msg ) {
   	if( msg==null ) msg="";
   	msg = SloaneCommonDAO.truncate(msg,255);
		alert("prep orderId %s with error %s",orderId, msg);
		db.executePreparedStatement("UPDATE custOrders SET status='error', errorMsg=? WHERE orderId=?", 
				msg, orderId);
		SloaneCommonDAO.postOrderLog(orderId, id, msg);
		RDSCounter.increment(COUNTER_ORDER_PREP_ERROR);  
   }

   
   /*
    * --- utility methods
    */
  
   private boolean controlParameterYes(String name) {
   	return getMapStr(controlMap,name).equalsIgnoreCase("yes");
   }

   /*
    * --- main ---
    */

   /**
    * Application entry point.
    * 
    * @param   args  command-line arguments
    */
   public static void main( String... args ) {
      String id = (args.length > 0) ? args[0] : DEFAULT_ID;
      String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;
      trace( "application started, id = [%s], db = [%s]", id, rdsDb );
      OrderPrepApp app = new OrderPrepApp( id, rdsDb );
      app.run();
   }

}
