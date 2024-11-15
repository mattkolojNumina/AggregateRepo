package cartonize;

import static rds.RDSLog.*;
import static sloane.SloaneConstants.*;

import rds.*;
import java.util.*;

import dao.SloaneCommonDAO;
import dao.RdsWaveDAO;
import dao.CustOutboundOrderFileDAO;
import polling.AbstractPollingApp;
import pack.*;

public class CartonizeApp extends AbstractPollingApp {

   private static final String DEFAULT_ID = "carton";
   
   private static final double DEFAULT_FLUID_VOLUME = 4.0 * 4.0 * 4.0 ;
   private static final double DEFAULT_FLUID_FILL   = 0.9 ;

   
   private Map<String,String> controlMap;
   private String truckNumber = "";
   private int waveSeq = -1;
   private int fileSeq = -1;
   private String orderType = "";
   private String orderId = "";
   private String shipmentId = "";
   private List<String> waveTruckOrders = null;
   private Map<String,Integer> orderCartonCountMap = null;
   private Map<String,List<String>> waveTruckErrorOrders = null;
   private String orderListStr = "";
   private int cartonCount = 1;
   private int lineNumber = 1;
   
   private class Item {
      String pickSeq;
      int orderLineSeq;
      String sku;
      String uom;
      String buyingDepartment;
      String orderId;
      double length;
      double width;
      double height;
      double weight;
      double volume;
      int qty;
   }
   
   private class Carton {
      String cartonType;
      double length;
      double width;
      double height;
      double weight;
      double tare;
      int count;
      double fillFactor;
   }
   
   public CartonizeApp(String id, String rdsDb) {
      super(id, rdsDb);
      RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		SloaneCommonDAO.setDatabase(db);
		controlMap = db.getControlMap(DEFAULT_ID);
   }

   protected void poll() {
   	pollEcomOrders();
      pollOrders();
   }
   
   private void pollEcomOrders() {
      long start = System.currentTimeMillis();
      List<Map<String,String>> waveTrucks = null;
      try {
      	waveTrucks = getEcomWaveTruck();
      } catch (ProcessingException ex) {
         alert( "processing error: %s", ex.getMessage() );
      }
      if (waveTrucks == null || waveTrucks.isEmpty())
         return;
      inform( "%d wave truck(s) require cartonization", waveTrucks.size() );
      for (Map<String,String> waveTruck : waveTrucks)
         processWaveTruck( waveTruck );
      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   }
   
   private List<Map<String, String>> getEcomWaveTruck() 
         throws ProcessingException {
      List<Map<String,String>> list = db.getResultMapList(          
            "SELECT DISTINCT truckNumber, waveSeq, fileSeq FROM custOrders " + 
            "JOIN rdsWaves USING(waveSeq) " +
            "WHERE orderType='%s' AND custOrders.status = 'prepared' " +
            "AND custOrders.cartonizeStamp IS NULL " +
            "AND rdsWaves.prepareStamp IS NOT NULL " +
            "ORDER BY rdsWaves.waveSeq",ORDERTYPE_ECOM );
      if (list == null)
         throw new ProcessingException( "error while retrieving ecom wave truck" );
      return list;
   }
   
   private void processWaveTruck( Map<String,String> waveTruck ) {
      long start = System.currentTimeMillis();
      waveSeq = getMapInt( waveTruck, "waveSeq" );
      fileSeq = getMapInt( waveTruck, "fileSeq" );
      truckNumber = getMapStr( waveTruck, "truckNumber" );
      waveTruckOrders = getWaveTruckOrders();
      waveTruckErrorOrders = new HashMap<>();
      orderCartonCountMap = new HashMap<>();
      cartonCount = 1;
      lineNumber = 1;
      try {
         doProcessWaveTruck();
         updateWaveTruckPickLineNumber();
         updateWaveTruckCartonized();
         checkWaveCartonized();
         SloaneCommonDAO.checkFileCartonized(fileSeq);
         checkWaveReleaseStatus(ORDERTYPE_ECOM);
      	trace("orderId %s: cartonization complete", orderId);
      } catch (DataException ex) {
      	alert( "orderId %s, data error during cartoniztion: %s",orderId,ex.getMessage() );
      	updateWaveTruckOrderError( ex.getMessage() );
      } catch (ProcessingException ex) {
      	alert( "orderId %s, processing error during cartoniztion: %s",orderId,ex.getMessage() );
      	updateWaveTruckOrderError( ex.getMessage() );
      } catch (Exception ex) {
         ex.printStackTrace();
         String msg = "internal processing error";
         updateWaveTruckOrderError( msg );
      }
      long dt = System.currentTimeMillis() - start;
      if (dt > getMaxTime())
         inform( "processing order took %d msec", dt );
   }
   
   private List<String> getWaveTruckOrders(){
   	List<String> list = db.getValueList("SELECT DISTINCT orderId FROM custOrders WHERE waveSeq=%d AND truckNumber='%s' "
   			+ "AND status='prepared' AND cartonizeStamp IS NULL", waveSeq, truckNumber);
   	orderListStr = "";
   	for( String orderId : list ) {
   		String val = "'"+orderId+"'";
   		orderListStr += orderListStr.isEmpty()?val:(","+val);
   	}
   	return list;
   }
   
   private void doProcessWaveTruck()
         throws DataException, ProcessingException {
      trace( "cartonize waveSeq [%d] truckNumber [%s]", waveSeq, truckNumber );
      prepOrderOfWaveTruck();
      cartonizeOrderOfWaveTruck(false);
      updateWaveTruckOrderList();
      cartonizeOrderOfWaveTruck(true);
      postCartonizeCheckForWaveTruck();
   }
   
   private void updateWaveTruckOrderList() {
      for( String orderId : waveTruckErrorOrders.keySet() ) {
      	List<String> list = waveTruckErrorOrders.get(orderId);
      	inform("%d sku/uom pairs do not fit in order %s", list.size(), orderId);
      	String unfitSkus = RDSUtil.separate(",", list);
      	String errorMsg = String.format("failed to cartonize sku/uom %s", unfitSkus);
      	updateOrderError(orderId, errorMsg);
      	if( waveTruckOrders.contains(orderId) )
      		waveTruckOrders.remove(orderId);
      }
      orderListStr = "";
   	for( String orderId : waveTruckOrders ) {
   		String val = "'"+orderId+"'";
   		orderListStr += orderListStr.isEmpty()?val:(","+val);
   	}
   }
   
   private void prepOrderOfWaveTruck() {
   	for( String orderId : waveTruckOrders) {
         int count = db.execute( "DELETE FROM rdsCartons " +
               "WHERE orderId = '%s'",
               orderId); 
         if ( count > 0 )
            inform("  %d cartons removed from order", count);
         count = db.execute( "UPDATE rdsPicks " +
               "SET cartonSeq = -1 " +
               "WHERE orderId = '%s'",
               orderId);
         if ( count > 0 )
            inform("  %d picks reset cartonSeq", count);
   	}
   }
   
   private void cartonizeOrderOfWaveTruck(boolean createCarton) 
         throws DataException, ProcessingException {
   	int count = 0;
   	for( String orderId : waveTruckOrders) {
         count += db.getInt(-1, "SELECT COUNT(*) FROM rdsPicks " +
            "WHERE orderId = '%s'", orderId);
   	}
      if (count < 0)
         throw new ProcessingException( "error retrieving pick records" );
      if (count == 0)
         throw new DataException( "no picks in order to cartonize" );
      inform("%d total picks to cartonize", count);
      doCartonizeOrderOfWaveTruck(createCarton);
   }
   
   private void doCartonizeOrderOfWaveTruck(boolean createCarton) 
         throws DataException, ProcessingException {
      tetrisSplitCaseCartons(ORDERTYPE_ECOM,PICKTYPE_AERSOLBOOM,createCarton);
      tetrisSplitCaseCartons(ORDERTYPE_ECOM,PICKTYPE_LIQUIDS,createCarton);
      tetrisSplitCaseCartons(ORDERTYPE_ECOM,PICKTYPE_PERISHABLES,createCarton);
      tetrisSplitCaseCartons(ORDERTYPE_ECOM,PICKTYPE_GEEK,createCarton);
      tetrisSplitCaseCartons(ORDERTYPE_ECOM,PICKTYPE_ZONEROUTE,createCarton);
   }
   
   private void postCartonizeCheckForWaveTruck()
         throws DataException, ProcessingException {
      int orderCartonCount = db.getInt(-1, 
            "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE orderId IN (%s) ", orderListStr);
      int pickCartonCount = db.getInt(-1, 
            "SELECT COUNT(DISTINCT cartonSeq) FROM rdsPicks " +
            "WHERE orderId IN (%s) " +
            "AND cartonSeq > 0 ", orderListStr );
      if (orderCartonCount != pickCartonCount && orderCartonCount >= 0)
         throw new ProcessingException(String.format( "carton count mismatch: order cartons %d, pick cartons %d",
               orderCartonCount, pickCartonCount ) );
   }
   
   private void updateWaveTruckPickLineNumber() {
   	int startLineNumber = 1;
   	for( String order : waveTruckOrders ) {
   		int n = updatePickLineNumber(order,startLineNumber);
   		startLineNumber = n;
   	}
   }
   
   private void updateWaveTruckCartonized() {
   	for( String order : waveTruckOrders ) {
   		updateOrderCartonized(order);
   	}
   }
   
   private void updateWaveTruckOrderError(String error) {
   	for( String order : waveTruckOrders ) {
   		updateOrderError(order, error);
   	}
   }
   
   private void pollOrders() {
      long start = System.currentTimeMillis();
      List<Map<String,String>> orders = null;
      try {
         orders = getOrders();
      } catch (ProcessingException ex) {
         alert( "processing error: %s", ex.getMessage() );
      }
      if (orders == null || orders.isEmpty())
         return;
      inform( "%d order(s) require cartonization", orders.size() );
      for (Map<String,String> order : orders)
         process( order );
      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   }

   private List<Map<String, String>> getOrders() 
         throws ProcessingException {
      List<Map<String,String>> orderMapList = db.getResultMapList(          
            "SELECT custOrders.*,fileSeq FROM custOrders " + 
            "JOIN rdsWaves USING(waveSeq) " +
            "WHERE custOrders.status = 'prepared' " +
            "AND orderType<>'%s' " +
            "AND custOrders.cartonizeStamp IS NULL " +
            "AND rdsWaves.prepareStamp IS NOT NULL " +
            "ORDER BY custOrders.waveSeq, custOrders.priority DESC, custOrders.downloadStamp ASC",ORDERTYPE_ECOM );
      if (orderMapList == null)
         throw new ProcessingException( "error while retrieving orders" );
      return orderMapList;
   }

   private void process( Map<String,String> o ) {
      long start = System.currentTimeMillis();
      orderId = getMapStr( o, "orderId" );
      waveSeq = getMapInt( o, "waveSeq" );
      fileSeq = getMapInt( o, "fileSeq" );
      orderType = getMapStr( o, "orderType" );
      truckNumber = getMapStr( o, "truckNumber" );
      cartonCount = 1;
      lineNumber = 1;
      try {
         doProcess( o );
         updatePickLineNumber( orderId, 1);
         updateOrderCartonized();
         checkWaveCartonized();
         SloaneCommonDAO.checkFileCartonized(fileSeq);
         checkWaveReleaseStatus("");
      	trace("orderId %s: cartonization complete", orderId);
      } catch (DataException ex) {
      	alert( "orderId %s, data error during cartoniztion: %s",orderId,ex.getMessage() );
         updateOrderError( ex.getMessage() );
      } catch (ProcessingException ex) {
      	alert( "orderId %s, processing error during cartoniztion: %s",orderId,ex.getMessage() );
         updateOrderError( ex.getMessage() );
      } catch (Exception ex) {
         ex.printStackTrace();
         String msg = "internal processing error";
         updateOrderError( msg );
      }
      long dt = System.currentTimeMillis() - start;
      if (dt > getMaxTime())
         inform( "processing order took %d msec", dt );
   }
   
   private void doProcess( Map<String,String> o )
         throws DataException, ProcessingException {
      if (orderId == null || orderId.isEmpty())
         throw new ProcessingException( "invalid orderId" );
      trace( "cartonize orderId [%s]", orderId );
      prepOrder();
      cartonizeOrder( o );
      postCartonizeCheck();
   }
   
   private void prepOrder() {
      int count = db.execute( "DELETE FROM rdsCartons " +
            "WHERE orderId = '%s'",
            orderId); 
      if ( count > 0 )
         inform("  %d cartons removed from order", count);
      count = db.execute( "UPDATE rdsPicks " +
            "SET cartonSeq = -1 " +
            "WHERE orderId = '%s'",
            orderId);
      if ( count > 0 )
         inform("  %d picks reset cartonSeq", count);
   }

   private void cartonizeOrder(Map<String,String> o) 
         throws DataException, ProcessingException {
      String orderId = getMapStr(o, "orderId");
      int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsPicks " +
            "WHERE orderId = '%s'", orderId);
      if (count < 0)
         throw new ProcessingException( "error retrieving pick records" );
      if (count == 0)
         throw new DataException( "no picks in order to cartonize" );
      
      inform("%d total picks to cartonize", count);
     
      doCartonizeOrder(o);
   }
   
   private void doCartonizeOrder(Map<String,String> o) 
         throws DataException, ProcessingException {
      tetrisSplitCaseCartons(orderType,PICKTYPE_AERSOLBOOM,true);
      tetrisSplitCaseCartons(orderType,PICKTYPE_LIQUIDS,true);
      tetrisSplitCaseCartons(orderType,PICKTYPE_PERISHABLES,true);
      tetrisSplitCaseCartons(orderType,PICKTYPE_GEEK,true);
      tetrisSplitCaseCartons(orderType,PICKTYPE_ZONEROUTE,true);
   }

   private int createCarton(String orderId, String pickType, String cartonType) {
      db.executePreparedStatement("INSERT INTO rdsCartons SET " +
            "orderId = ?, " +
            "pickType = ?, " +
            "cartonType = ?, " +
            "truckNumber = ?, " +
            "createStamp = NOW() ",
            orderId,
            pickType,
            cartonType == null ? "" : cartonType,
            truckNumber
            );
      int cartonSeq = db.getSequence();
      inform("  cartonSeq %d created. pick type [%s] carton type [%s]", 
            cartonSeq, pickType, cartonType == null ? "n/a" : cartonType);
      RDSCounter.increment(String.format("/cartonization/%s",pickType));
      if( cartonType !=null && !cartonType.isEmpty() )
      	RDSCounter.increment(String.format("/cartonization/%s/%s",pickType,cartonType));
      SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton created");
      return cartonSeq;
   }

   private void addCartonPick(int cartonSeq, int pickSeq, int lineNumber)
         throws DataException, ProcessingException {
      if (cartonSeq <= 0)
         throw new DataException("invalid carton seq for addCartonPick");
      if (pickSeq <= 0)
         throw new DataException("invalid pick seq for addCartonPick");
      //inform("  add pickSeq %d to cartonSeq %d", pickSeq, cartonSeq);
      int count = db.execute("UPDATE rdsPicks SET " +
            "cartonSeq = %d, lineNumber = %d " +
            "WHERE pickSeq = %d",
            cartonSeq, lineNumber, pickSeq );
      if (count <= 0)
         throw new ProcessingException("failed to update pick record");
   }
   
   private double getPickWeight(int cartonSeq) {
      String strWeight = db.getString(null, "SELECT " +
            "SUM( weight*qty ) AS weight " +
            "FROM rdsPicks AS p " +
            "JOIN custSkus AS s ON p.sku=s.sku AND p.baseUom=s.uom " +
            "WHERE cartonSeq = %d", cartonSeq) ;     
      return RDSUtil.stringToDouble(strWeight, 0);
   }

   private Point getOuterCartonSizeDimensions(String cartonType) {
      Map<String, String> type = db.getRecordMap("SELECT * FROM cfgCartonTypes " +
            "WHERE cartonType = '%s'", cartonType) ;
      double length = getMapDbl(type,"exteriorLength");
      double width = getMapDbl(type,"exteriorWidth");
      double height = getMapDbl(type,"exteriorHeight");
      
      if (length < width) {
         double swap;
         swap = length;
         length = width;
         width = swap;
      }
      if (length < height) {
         double swap;
         swap = length;
         length = height;
         height = swap;
      }
      if (width < height) {
         double swap;
         swap = width;
         width = height;
         height = swap;
      }
      
      return new Point( length, width, height );
   }
   
   private Point getPickDimensions(int pickSeq) {
      Map<String, String> p = db.getRecordMap("SELECT p.pickSeq, s.* " +
            "FROM rdsPicks AS p " +
            "JOIN custSkus AS s USING(sku,uom) " +
            "WHERE p.pickSeq = %d", pickSeq) ;     
      RdsPick pick = new RdsPick(p);
      return pick.getSize();
   }
   
   private void updateCartonDimensions(int cartonSeq, Point dimensions, 
         double pickWeight, double tareWeight, boolean setWeight) {
      double length = dimensions.getX();
      double width  = dimensions.getY();
      double height = dimensions.getZ();
      double expWeight = pickWeight + tareWeight ;
      
      db.execute("UPDATE rdsCartons SET " +
            "estLength = %.2f, " +
            "estWidth = %.2f, " +
            "estHeight = %.2f, " +
            "estWeight = %.2f " +
            "WHERE cartonSeq = %d",
            length, width, height, 
            expWeight, cartonSeq);
   }
   
   private void postCartonizeCheck()
         throws DataException, ProcessingException {
      List<Map<String,String>>  p = db.getResultMapList(
            "SELECT p.* FROM rdsPicks AS p " +
            "WHERE orderId = '%s' " +
            "AND cartonSeq <= 0",
            orderId );
      if ( p != null && !p.isEmpty()) {
         generateFailedSkus(p);
      }
      int orderCartonCount = db.getInt(-1, 
            "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE orderId = '%s' ", 
            orderId );
      int pickCartonCount = db.getInt(-1, 
            "SELECT COUNT(DISTINCT cartonSeq) FROM rdsPicks " +
            "WHERE orderId = '%s' " +
            "AND cartonSeq > 0 ", orderId );
      if (orderCartonCount != pickCartonCount && orderCartonCount >= 0)
         throw new ProcessingException(String.format( "carton count mismatch: order cartons %d, pick cartons %d",
               orderCartonCount, pickCartonCount ) );
   } 

 
   /*
    *  Tetris Cartonization
    */
   
   private boolean tetrisSplitCaseCartons(String type, String pickType, boolean createCarton) 
         throws DataException, ProcessingException {
   	int numPicks = 0;
   	String orderIdSql = "";
   	String pickTypeSql = String.format("AND p.pickType='%s'", pickType);;
   	String typeClassSql = "";
   	String typeClass = "";
   	if( type.equals(ORDERTYPE_TOTE) ) {
   		orderIdSql = String.format("WHERE p.orderId='%s'",orderId);
   		typeClass = TYPECLASS_TOTE;
   		boolean useBoxForToteOrder = db.getString("", "SELECT useBoxForToteOrder FROM cfgDepartments WHERE rdsPickZone='%s'", pickType).equals("yes");
   		if( useBoxForToteOrder ) {
   			inform("use box for tote order in %s", pickType);
   			typeClass = TYPECLASS_BOX;
   		}
   		typeClassSql = String.format("AND typeClass='%s'", typeClass);
   	} else if( type.equals(ORDERTYPE_BOX) ) {
   		orderIdSql = String.format("WHERE p.orderId='%s'",orderId);
   		typeClass = TYPECLASS_BOX;
   		typeClassSql = String.format("AND typeClass='%s'", typeClass);
   	} else if( type.equals(ORDERTYPE_EXPORT) ) {
   		orderIdSql = String.format("WHERE p.orderId='%s'",orderId);
   		typeClass = TYPECLASS_EXPORT;
   		typeClassSql = String.format("AND typeClass='%s'", typeClass);
   	} else {
   		orderIdSql = String.format("WHERE p.orderId IN (%s)", orderListStr);
   		typeClass = TYPECLASS_TOTE;
   		typeClassSql = String.format("AND typeClass='%s'", typeClass);
   	}
      numPicks = db.getInt(-1,
            "SELECT COUNT(*) FROM rdsPicks p " +
            "%s " +
            "AND cartonSeq <= 0 " +
            "%s",
            orderIdSql, pickTypeSql );
      if (numPicks == 0) {
         inform("no picks require tetris cartonization");
         return true;
      }
      inform("%d picks require tetris cartonization", numPicks);
      ArrayList<Item> items = getTetrisSplitCaseItems(orderIdSql,pickTypeSql,typeClass);
      ArrayList<Carton> cartons = getTetrisSplitCaseCartonTypes(typeClassSql);
      //int cutoff = getFluidCountCutoff();
      //double maxFluid = getMaxFluidVolume(cutoff,numPicks);
      //double maxFill = getMaxFluidFill(cutoff,numPicks);
      //int maxSkuCount = RDSUtil.stringToInt(db.getControl(id,"maxSkuCount", null),99);
      boolean debug = getDebug();
      long timeout = getTimeout();
      
      inform("begin pack %d cartons types %d picks", cartons.size(), items.size());
      /*
      inform("  cartonization timeout: %d msec", timeout);
      inform("  pick count fluid model cutoff: %d", cutoff);
      inform("  pick volume fluid cutoff: %.2f", maxFluid );
      inform("  pick fill fluid volume to: %.2f%%", maxFill*100);
      if (maxFluid > 0) {
         int fluidPickCount = db.getInt(0, 
               "SELECT COUNT(DISTINCT p.pickSeq) " +
               "FROM rdsPicks AS p " +
               "JOIN custSkus AS s USING(sku,uom) " +
               "WHERE orderId = '%s' " +
               "AND pickType='%s' " +
               "AND length*width*height < %.3f " +
               "AND cartonSeq <= 0", 
               orderId, PICKTYPE_SPLITCASE, maxFluid);
         inform("  %d fluid like picks in order", fluidPickCount);
      }
      */
      if (debug)
         inform("  debug enabled");
      
      Job job = new Job(orderId);
      //job.setMaxFluid( maxFluid ) ;
      //job.setMaxFill ( maxFill ) ;
      //job.setMaxSkuCount(maxSkuCount);
      job.setDebug( debug );
      job.setTimeout(timeout);
   
      addJobCartons(job, cartons);
      addJobItems(job,items);
   
      boolean ok = job.pack();
      inform("cartonization ok: %b", ok);
      
      if (ok) {
      	if( createCarton )
      		createTetrisCartons(type, pickType, job);
      } else if (createCarton) {
      	if (job.errorType == Job.ERROR_TIMEOUT) {
            long timeout_minutes = timeout / (60 * 1000);
            throw new ProcessingException( "cartonization timed out after " + timeout_minutes + 
                  " min during processing");
         } else {
      		generateFailedSkus(job.items);
         }
      } else {
      	excludeFailedSkus(job.items);
      }
      /*
      if (job.debug)
         for (Manifest manifest : job.manifests)
            manifest.list();
      */
      return ok;
   }

   private void addJobCartons(Job job, ArrayList<Carton> cartons) {
      for (Carton carton : cartons) {
         Box box = new Box(
               carton.cartonType,
               new Point(
                     carton.length , 
                     carton.width , 
                     carton.height ), 
               carton.weight ,
               0);
         box.setTare(carton.tare );
         box.setFill(carton.fillFactor);
         box.setCount(carton.count);
         job.addContainer(box);
      }      
   }

   private void addJobItems(Job job, ArrayList<Item> items) {
      for (Item item : items) {
         Box box = new Box(
               item.pickSeq, 
               item.weight,
               item.volume,
               item.orderId,
               item.buyingDepartment,
               item.orderLineSeq
               );
         box.setSku(item.sku + ":" + item.uom);
         box.setCount(item.qty);
         box.setSize(               
         		new Point(
                     item.length , 
                     item.width , 
                     item.height ));
         job.addContent(box);
      }      
   }

   private int getFluidCountCutoff() {
      String val = db.getControl(id,"fluidCountCutoff", null);
      int cutoff = RDSUtil.stringToInt(val, -1);
      return cutoff;
   }
   
   private double getMaxFluidVolume(int cutoff, int pickCount) {
      if (cutoff < 0 || pickCount <= cutoff)
         return 0;
      
      String val = db.getControl(id,"fluidMaxVolume", null);
      double volume = RDSUtil.stringToDouble(val, DEFAULT_FLUID_VOLUME);
      return volume;
   }
      
   private double getMaxFluidFill(int cutoff, int pickCount) {
      if (cutoff < 0 || pickCount <= cutoff)
         return 0;
      
      String val = db.getControl(id,"fluidMaxFill", null);
      double fill = RDSUtil.stringToDouble(val, DEFAULT_FLUID_FILL);
      return fill;
   }
   
   private boolean getDebug() {
      String val = db.getControl(id,"debug", null);
      boolean debug = "true".equals(val);
      return debug;
   }

   private long getTimeout() {
      // get value in minutes and return in ms
      String val = db.getControl(id,"timeout", null);
      int timeout_minutes = RDSUtil.stringToInt(val, Job.DEFAULT_TIMEOUT_MINUTES);
      return timeout_minutes * 60 * 1000;
   }
   
   private void createTetrisCartons(String type, String pickType, Job job) 
         throws DataException, ProcessingException {
      trace("  create %s cartons using %s for order '%s'", pickType, type, type.equals(ORDERTYPE_ECOM)?orderListStr:orderId);
      for (Manifest m : job.manifests) {
      	String order = orderId;
         String cartonType = m.container.id;
         Point dim = null;      
      	if( type.equals(ORDERTYPE_ECOM) ) {
            for (Placement p : m.placements) {
               int pickSeq = RDSUtil.stringToInt(p.box.getId(), -1);
               if (pickSeq <= 0)
                  continue;
               order = db.getString("", "SELECT orderId FROM rdsPicks WHERE pickSeq=%d", pickSeq);
               break;
            }      		
      	}
         int cartonSeq = createCarton(order, pickType, cartonType);
         if (cartonSeq <= 0)
            throw new ProcessingException("failed to create carton record");
         Map<Integer,Integer> lineNumberMap = new HashMap<>();
         for (Placement p : m.placements) {
            int pickSeq = RDSUtil.stringToInt(p.box.getId(), -1);
            int orderLineSeq = p.box.orderLineSeq;
            int currentLineNumber = lineNumberMap.getOrDefault(orderLineSeq, lineNumber);
            lineNumberMap.put(orderLineSeq, currentLineNumber);
            if( currentLineNumber == lineNumber )
            	lineNumber++;
            if (pickSeq <= 0)
               continue;
            addCartonPick(cartonSeq, pickSeq, currentLineNumber);
         }
         breakShelfPacks(cartonSeq);
         dim = getOuterCartonSizeDimensions(cartonType);
         double pickWeight = getPickWeight(cartonSeq);
         double tareWeight = m.getContainer().getTare();
         updateCartonDimensions(cartonSeq, dim, pickWeight, tareWeight, false );
         generateTrackingNumber(cartonSeq,type);
         checkCartonPickStatus(cartonSeq);
      }
   }
   
   private void breakShelfPacks(int cartonSeq) {
   	List<String> shelfPacks = db.getValueList("SELECT pickSeq FROM rdsPicks WHERE cartonSeq=%d AND uom='%s' AND picked=0", cartonSeq, UOM_SHELFPACK);
   	for( String pickSeqStr : shelfPacks ) {
   		int pickSeq = RDSUtil.stringToInt(pickSeqStr, -1);
   		SloaneCommonDAO.breakShelfPack(pickSeq,0);
   	}
   }

   private void generateFailedSkus(List<?> itemList) 
         throws DataException, ProcessingException {
      trace("  generate failed sku list for orderId %s", orderId);
      List<String> unfits = new ArrayList<String>();
      for (Object item : itemList) {
         String sku = "";
         if (item instanceof Box) {
            Box b = (Box)item;
            sku = b.sku;
         } else if (item instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<String,String> m = (Map<String,String>)item;
            sku = getMapStr(m, "item");
         }
         if (unfits.contains(sku))
            continue;
         unfits.add(sku);
         inform("   sku %s do not fit", sku);
         SloaneCommonDAO.postOrderLog(orderId, id, "cannot cartonize: %s", sku);
      }
      inform("%d sku/uom pairs do not fit", unfits.size());
      int unfitNo = Math.min(unfits.size(), 3);
      String unfitExamples = RDSUtil.separate(", ", unfits.subList(0, unfitNo));
      inform("  unfit sku e.g. %s", unfitExamples);
      throw new ProcessingException(String.format("failed to cartonize all skus. (e.g. [%s]", unfitExamples ));
   }
   
   private void excludeFailedSkus(List<?> itemList){
      trace("  exclude failed sku from orders in wave %d with truckNumber %s", waveSeq, truckNumber);
      for (Object item : itemList) {
         String sku = "";
         String orderId = "";
         if (item instanceof Box) {
            Box b = (Box)item;
            sku = b.sku;
            orderId = b.orderId;
         } else if (item instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<String,String> m = (Map<String,String>)item;
            sku = getMapStr(m, "item");
            orderId = getMapStr(m,"orderId");
         }
         if( waveTruckErrorOrders.containsKey(orderId) ) {
         	if( waveTruckErrorOrders.get(orderId).contains(sku) ) continue;
         	waveTruckErrorOrders.get(orderId).add(sku);
         } else {
         	List<String> unfits = new ArrayList<String>();
         	unfits.add(sku);
         	waveTruckErrorOrders.put(orderId, unfits);
         }
      }
   }

   /* 
    * Tetris Cartonization classes
    */
   
   private ArrayList<Item> getTetrisSplitCaseItems(String orderIdSql, String pickTypeSql, String typeClass) 
         throws DataException, ProcessingException {
      ArrayList<Item> items = new ArrayList<Item>();
      List<Map<String, String>> picks = db.getResultMapList(
      				"SELECT p.pickSeq,p.qty,p.pickType, p.orderLineSeq, p.uom AS pickUom, p.picked, s.* FROM rdsPicks p " +
      				"JOIN custSkus s ON p.sku=s.sku AND p.baseUom=s.uom " +
                  "%s " +
                  "AND p.cartonSeq <= 0 " +
                  "%s",
                  orderIdSql, pickTypeSql);
      if ( picks == null || picks.isEmpty())
         throw new DataException ("failed to load pick data");
      Map<String,String> maxVandW = db.getRecordMap(
      		"SELECT MAX(interiorLength*interiorWidth*interiorHeight*fillFactor) AS maxV,"
      		+ "MAX(maxWeight-tareWeight) AS maxW FROM cfgCartonTypes WHERE typeClass='%S'", typeClass);
      Map<String,ExceptionAndSolution> skuExceptions = new HashMap<>();
      for (Map<String, String> p : picks) {
      	p.put("defaultLength", getMapStr(controlMap,"defaultLength"));
      	p.put("defaultWidth", getMapStr(controlMap,"defaultWidth"));
      	p.put("defaultHeight", getMapStr(controlMap,"defaultHeight"));
      	p.put("defaultWeight", getMapStr(controlMap,"defaultWeight"));
      	p.put("maxV", getMapStr(maxVandW,"maxV"));
      	p.put("maxW", getMapStr(maxVandW,"maxW"));
         RdsPick pick = new RdsPick(p);
         if (pick.getWeight() <= 0) 
            throw new DataException("invalid weight for item " + pick.getSku());
         if (pick.getVolume() <= 0) 
            throw new DataException("invalid dimensions for item " + pick.getSku());
         Item item = new Item();
         item.pickSeq = "" + pick.getPickSeq();
         item.orderLineSeq = pick.getOrderLineSeq();
         item.sku = pick.getSku();
         item.uom = pick.getUom();
         item.length = pick.getLength();
         item.width = pick.getWidth();
         item.height = pick.getHeight();
         item.volume = pick.getVolume();
         item.buyingDepartment = pick.getBuyingDepartment();
         item.weight = pick.getWeight();
         item.orderId = pick.getOrderId();
         item.qty = pick.getQty();
         items.add(item);
         if( !pick.getException().isEmpty() ) {
         	if( !skuExceptions.containsKey(item.sku) ) {
         		ExceptionAndSolution eAndS = new ExceptionAndSolution(pick.getException(),pick.getSolution());
         		skuExceptions.put(item.sku, eAndS);
         	}
         }
      }
      for( String sku : skuExceptions.keySet() ) {
      	SloaneCommonDAO.updateSkuException(sku, skuExceptions.get(sku).exception,skuExceptions.get(sku).solution);
      }
      return items;
   }
   
   class ExceptionAndSolution{
   	public String exception;
   	public String solution;
   	
   	ExceptionAndSolution(String exception, String solution){
   		this.exception = exception;
   		this.solution = solution;
   	}
   }
   
   private ArrayList<Carton> getTetrisSplitCaseCartonTypes(String typeClassSql) 
         throws DataException, ProcessingException {
      ArrayList<Carton> cartons = new ArrayList<Carton>();
   
      List<Map<String, String>> cartonTypes = db.getResultMapList(
            "SELECT * FROM cfgCartonTypes " +
            "WHERE enabled = 1 %s", typeClassSql);
      
      if ( cartonTypes == null || cartonTypes.isEmpty())
         throw new DataException ("failed to load valid carton types for cartonization");
      
      for (Map<String, String> type : cartonTypes) {
         Carton carton = new Carton();
         carton.cartonType = getMapStr(type,"cartonType");
         carton.length = getMapDbl(type,"interiorLength");
         carton.width = getMapDbl(type,"interiorWidth");
         carton.height = getMapDbl(type,"interiorHeight");
         carton.weight = getMapDbl(type,"maxWeight");
         carton.tare = getMapDbl(type,"tareWeight");
         carton.count = getMapInt(type,"maxItemCount");
         carton.fillFactor = getMapDbl(type,"fillFactor");
   
         if (carton.length < carton.width) {
            double swap;
            swap = carton.length;
            carton.length = carton.width;
            carton.width = swap;
         }
         if (carton.length < carton.height) {
            double swap;
            swap = carton.length;
            carton.length = carton.height;
            carton.height = swap;
         }
         if (carton.width < carton.height) {
            double swap;
            swap = carton.width;
            carton.width = carton.height;
            carton.height = swap;
         }
   
         cartons.add(carton);
      }
      return cartons;
   }
   
   private int updatePickLineNumber( String order, int startLineNumber) {
   	List<Map<String,String>> lines = db.getResultMapList(
   			"SELECT DISTINCT cartonSeq, orderLineSeq FROM rdsPicks WHERE orderId='%s' ORDER BY cartonSeq, orderLineSeq", order);
   	int size = lines.size();
   	for( int i=0; i<size;i++ ) {
   		Map<String,String> m = lines.get(i);
   		int cartonSeq = getMapInt(m,"cartonSeq");
   		int orderLineSeq = getMapInt(m,"orderLineSeq");
   		int lineNumber = i+startLineNumber;
   		db.execute("UPDATE rdsPicks SET lineNumber=%d WHERE cartonSeq=%d AND orderLineSeq=%d", lineNumber,cartonSeq,orderLineSeq);
   	}
   	return startLineNumber+size;
   }
   
   private void updateOrderCartonized() {
      db.execute(
            "UPDATE custOrders SET " +
            "status = 'cartonized', " +
            "errorMsg = '', " +
            "cartonizeStamp = NOW() " +
            "WHERE orderId = '%s'",
            orderId );
      SloaneCommonDAO.postOrderLog(orderId,id,"cartonize order successfully");
      RDSCounter.increment(COUNTER_ORDER_CARTONIZE_SUCCESS);  
   }
   
   private void updateOrderCartonized(String order) {
      db.execute(
            "UPDATE custOrders SET " +
            "status = 'cartonized', " +
            "errorMsg = '', " +
            "cartonizeStamp = NOW() " +
            "WHERE orderId = '%s'",
            order );
      SloaneCommonDAO.postOrderLog(orderId,id,"cartonize order successfully");
      RDSCounter.increment(COUNTER_ORDER_CARTONIZE_SUCCESS);  
   }
   
   private void updateOrderError( String msg ) {
   	if( msg==null ) msg="";
   	msg = SloaneCommonDAO.truncate(msg,255);
      db.execute(
            "UPDATE custOrders SET " +
                  "status = 'error', " +
                  "errorMsg = '%s' " +
                  "WHERE orderId = '%s'",
                  msg, orderId );
		SloaneCommonDAO.postOrderLog(orderId, id, msg);
		RDSCounter.increment(COUNTER_ORDER_CARTONIZE_ERROR);  
   } 
   
   private void updateOrderError( String order, String msg ) {
   	if( msg==null ) msg="";
   	msg = SloaneCommonDAO.truncate(msg,255);
      db.execute(
            "UPDATE custOrders SET " +
                  "status = 'error', " +
                  "errorMsg = '%s' " +
                  "WHERE orderId = '%s'",
                  msg, order );
		SloaneCommonDAO.postOrderLog(order, id, msg);
		RDSCounter.increment(COUNTER_ORDER_CARTONIZE_ERROR);  
   } 
   
   private void checkShipmentCartonized() {
   	Map<String,String> s = db.getRecordMap(
   			"SELECT COUNT(o.orderId) AS total, "
				+ "COUNT(CASE WHEN o.cartonizeStamp IS NOT NULL AND o.cancelStamp IS NULL THEN orderId END) as cartonized,"
				+ "COUNT(CASE WHEN o.cancelStamp IS NOT NULL THEN orderId END) as canceled, "
				+ "COUNT(CASE WHEN o.status='error' THEN orderId END) as error "
				+ "FROM custOrders o JOIN custShipments s USING(shipmentId) "
				+ "WHERE s.shipmentId='%s' AND s.status='prepared' ", shipmentId);
		int total = getMapInt(s,"total");
		int cartonized = getMapInt(s,"cartonized");
		int canceled = getMapInt(s,"canceled");
		int error = getMapInt(s,"error");
		if (total == canceled ) {
			inform("ShipmentId %s is canceled", shipmentId);
			db.execute("UPDATE custShipments SET status='canceled', cancelStamp=NOW() WHERE shipmentId='%s'", shipmentId);	
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "shipment is canceled because all the orders are canceled");
		} else if( total == (cartonized+canceled) ) {
			inform("ShipmentId %s is cartonized", shipmentId);
			db.execute("UPDATE custShipments SET status='cartonized', cartonizeStamp=NOW() WHERE shipmentId='%s'", shipmentId);
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "shipment is cartonized");
		} else if( total == error ) {
			inform("ShipmentId %s has all orders in error");
			db.execute("UPDATE custShipments SET status='error' WHERE shipmentId='%s'", shipmentId);
			SloaneCommonDAO.postShipmentLog(shipmentId, id, "marked as error");
		}
   }
   
   private void checkWaveCartonized() {
   	Map<String,String> s = db.getRecordMap(
   			"SELECT COUNT(o.orderId) AS total, "
				+ "COUNT(CASE WHEN o.cartonizeStamp IS NOT NULL AND o.cancelStamp IS NULL THEN orderId END) as cartonized,"
				+ "COUNT(CASE WHEN o.cancelStamp IS NOT NULL THEN orderId END) as canceled, "
				+ "COUNT(CASE WHEN o.status='error' THEN orderId END) as error "
				+ "FROM custOrders o "
				+ "WHERE o.waveSeq=%d ", waveSeq);
		int total = getMapInt(s,"total");
		int cartonized = getMapInt(s,"cartonized");
		int canceled = getMapInt(s,"canceled");
		int error = getMapInt(s,"error");
		if (total == canceled ) {
			inform("Wave %d is canceled", waveSeq);
			RdsWaveDAO.setTombstone(waveSeq, "cancelStamp");	
			SloaneCommonDAO.postWaveLog(waveSeq+"", id, "wave is canceled because all the orders are canceled");
		} else if( total == (cartonized+canceled+error) && cartonized>0 ) {
			inform("Wave %d is cartonized", waveSeq);
			RdsWaveDAO.setTombstone(waveSeq, "cartonizeStamp");	
			SloaneCommonDAO.postWaveLog(waveSeq+"", id, "wave is cartonized");
		} 
   }
   
   private void checkFileCartonized() {
   	Map<String,String> s = db.getRecordMap(
   			"SELECT COUNT(o.orderId) AS total, "
				+ "COUNT(CASE WHEN o.cartonizeStamp IS NOT NULL AND o.cancelStamp IS NULL THEN orderId END) as cartonized,"
				+ "COUNT(CASE WHEN o.cancelStamp IS NOT NULL THEN orderId END) as canceled, "
				+ "COUNT(CASE WHEN o.status='error' THEN orderId END) as error "
				+ "FROM custOrders o JOIN rdsWaves USING(waveSeq) "
				+ "WHERE fileSeq=%d ", fileSeq);
		int total = getMapInt(s,"total");
		int cartonized = getMapInt(s,"cartonized");
		int canceled = getMapInt(s,"canceled");
		int error = getMapInt(s,"error");
		if( total == (cartonized+canceled) && cartonized>0 ) {
			inform("File seq %d is cartonized", fileSeq);
			CustOutboundOrderFileDAO.setTombstone(fileSeq, "cartonizeStamp");	
		} 
   }   
   
   private void checkWaveReleaseStatus(String type) {
   	RdsWaveDAO waveDao = new RdsWaveDAO( waveSeq );
   	if( type.equals(ORDERTYPE_ECOM) ) {
   		for( String order: waveTruckOrders ) {
         	if( waveDao.tombStoneIsSet("zoneRouteReleaseStamp") )
         		SloaneCommonDAO.releaseOrderPicksByPickType( order, PICKTYPE_ZONEROUTE );
         	if( waveDao.tombStoneIsSet("cartPickReleaseStamp") ) {
         		SloaneCommonDAO.releaseOrderPicksByPickType( order, PICKTYPE_AERSOLBOOM );
         		SloaneCommonDAO.releaseOrderPicksByPickType( order, PICKTYPE_LIQUIDS );
         		SloaneCommonDAO.releaseOrderPicksByPickType( order, PICKTYPE_PERISHABLES );
         	}
         	if( waveDao.tombStoneIsSet("geekReleaseStamp") )
         		SloaneCommonDAO.releaseOrderPicksByPickType( order, PICKTYPE_GEEK );     			
   		}
   	} else {
      	if( waveDao.tombStoneIsSet("zoneRouteReleaseStamp") )
      		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_ZONEROUTE );
      	if( waveDao.tombStoneIsSet("cartPickReleaseStamp") ) {
      		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_AERSOLBOOM );
      		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_LIQUIDS );
      		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_PERISHABLES );
      	}
      	if( waveDao.tombStoneIsSet("geekReleaseStamp") )
      		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_GEEK );   		
   	}

   }
   
   
   /*
    *  Helper methods
    */
   
   /** Gets a lock with the specified name. */
   public boolean lock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT GET_LOCK( '%s', %d )",
            lockName, LOCK_TIMEOUT );
      if (lockVal != 1) {
         String connId = db.getString( "",
               "SELECT IS_USED_LOCK( '%s' )", lockName );
         alert( "unable to get database lock for '%s', in use by id %s",
               lockName, connId );
      }
      //trace( "get lock return %d",lockVal);
      return (lockVal == 1);
   }

   /** Releases a lock with the specified name. */
   public boolean unlock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT RELEASE_LOCK( '%s' )",
            lockName );
      //trace( "release lock return %d",lockVal);      
      return (lockVal == 1);
   }
   
   private void generateTrackingNumber( int cartonSeq, String type ) {
   	if( type.equals(ORDERTYPE_ECOM) ) {
   		Map<String,String> map = db.getRecordMap("SELECT ol.orderId,ol.pageId, ol.lineId FROM custOrderLines ol "
   				+ "JOIN rdsPicks USING(orderLineSeq) WHERE cartonSeq=%d ORDER BY ol.orderLineSeq LIMIT 1", cartonSeq);   	
   		String orderId = getMapStr(map,"orderId");
   		String pageId = getMapStr(map,"pageId");
   		String lineId = getMapStr(map,"lineId");
   		int orderCartonCount = orderCartonCountMap.getOrDefault(orderId, 0)+1;
   		orderCartonCountMap.put(orderId, orderCartonCount);
   		String trackingNumber = String.format("%s%s%s%04d", orderId,pageId.substring(pageId.length()-3),lineId.substring(lineId.length()-4),orderCartonCount);
   		db.execute("UPDATE rdsCartons SET trackingNumber='%s', orderId='%s', cartonCount=%d WHERE cartonSeq=%d", trackingNumber, orderId, orderCartonCount, cartonSeq );
   	} else {
   		Map<String,String> map = db.getRecordMap("SELECT ol.pageId, ol.lineId FROM custOrderLines ol "
   				+ "JOIN rdsPicks USING(orderLineSeq) WHERE cartonSeq=%d ORDER BY ol.orderLineSeq LIMIT 1", cartonSeq);
   		String pageId = getMapStr(map,"pageId");
   		String lineId = getMapStr(map,"lineId");
   		String trackingNumber = String.format("%s%s%s%04d", orderId,pageId.substring(pageId.length()-3),lineId.substring(lineId.length()-4),cartonCount);
   		db.execute("UPDATE rdsCartons SET trackingNumber='%s', cartonCount=%d WHERE cartonSeq=%d", trackingNumber, cartonCount, cartonSeq );
   		cartonCount++;
   	}
   }
   
   private void checkCartonPickStatus( int cartonSeq ) {
   	int numNotPicked = db.getInt(0, "SELECT COUNT(CASE WHEN picked=0 THEN pickSeq END) AS notPicked FROM rdsPicks WHERE cartonSeq=%d", cartonSeq);
   	if( numNotPicked == 0 ) {
			trace("cartonSeq %d has only picks for skus that have been marked out", cartonSeq);
			db.execute("UPDATE rdsCartons SET pickStamp=NOW(),pickShortStamp=NOW(),cancelStamp=NOW() WHERE cartonSeq=%d ", cartonSeq);
			SloaneCommonDAO.postCartonLog(""+cartonSeq,id,"carton canceled due to all picks marked out");	
   	}
   }
   
   private boolean generateUcc( int cartonSeq ) {
   	String gs1CompanyPrefix = db.getControl("system", "cartonUccGS1CompanyPrefix" , ""); // 0071497
   	if( gs1CompanyPrefix.isEmpty() )
   		return false;
   	if( gs1CompanyPrefix.charAt(0) != '0' ) {
   		gs1CompanyPrefix = '0' + gs1CompanyPrefix;
   	}
   	int gs1CompanyPrefixLength = gs1CompanyPrefix.length();
   	int maxSerialNumberLength = 16 - gs1CompanyPrefixLength;
   	int maxSerialNumber = (int)Math.pow(10,maxSerialNumberLength)-1;
   	String currentUccSerialNumberString = "";
   	if( lock("ucc") ) {
	   	currentUccSerialNumberString = db.getRuntime("cartonUccSerialNumber");
	   	int currentUccSerialNumber = RDSUtil.stringToInt(currentUccSerialNumberString, 1);
	   	if( currentUccSerialNumber > maxSerialNumber ) {
	   		currentUccSerialNumber = 500000000;
	   		currentUccSerialNumberString = "500000000";
	   	}
	   	currentUccSerialNumber++;
	   	db.setRuntime("cartonUccSerialNumber", ""+currentUccSerialNumber);
	   	unlock("ucc");
   	} else {
   		return false;
   	}
   	int currentUccSerialNumberStringLength = currentUccSerialNumberString.length();
   	for( int i=0 ; i<(maxSerialNumberLength-currentUccSerialNumberStringLength); i++ ) {
   		currentUccSerialNumberString = "0" + currentUccSerialNumberString;
   	}
   	String companyExtensionDigit = db.getControl("system", "cartonUccCompanyExtensionDigit" , "0"); // 0
   	if( companyExtensionDigit.length()> 1 )
   		return false;
   	if( RDSUtil.stringToInt(companyExtensionDigit, -1) == -1 )
   		return false;
   	String uccWithoutCheckDigits = "00" + companyExtensionDigit + gs1CompanyPrefix + currentUccSerialNumberString;
   	String currentUcc = uccWithoutCheckDigits + getCheckDigit(uccWithoutCheckDigits);
   	db.execute("UPDATE rdsCartons SET ucc='%s' WHERE cartonSeq=%d", currentUcc, cartonSeq );
   	return true;
   }
   
   private int getCheckDigit(String uccWithoutCheckDigits) {
   	String validateValue = "1" + uccWithoutCheckDigits.substring(2);
   	int result = 0;
   	for( int i=1; i<validateValue.length(); i=i+2 ) {
   		result += (int) (validateValue.charAt(i) - '0');
   	}
   	result = result * 3;
   	for( int i=2; i<validateValue.length(); i=i+2 ) {
   		result += (int) (validateValue.charAt(i) - '0');
   	}
   	int mod = result % 10;
   	return mod==0?0:(10-mod);
   }   

   /*
    * --- main ---
    */
   
   /**
    * Application entry point.
    * 
    * @param args
    *           command-line arguments
    */
   public static void main(String... args) {
      String id = (args.length > 0) ? args[0] : DEFAULT_ID;
      String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;

      trace("application started, id = [%s], db = [%s]", id, rdsDb);

      CartonizeApp app = new CartonizeApp(id, rdsDb);

      app.run();
   }

}
