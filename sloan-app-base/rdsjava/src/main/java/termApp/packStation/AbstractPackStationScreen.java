package termApp.packStation;

import static rds.RDSLog.*;
import static app.Constants.*;

import java.util.regex.Pattern;

import org.json.JSONObject;

import app.AppCommon;

import java.util.regex.Matcher;
import java.util.*;
import java.util.List;
import java.util.Map;

import term.TerminalDriver;
import termApp.*;
import rds.RDSUtil;
import rds.RDSHistory;

/**
 * Base class for terminal screens, defining constants and default
 * behavior.
 */
public abstract class AbstractPackStationScreen extends AbstractProjectAppScreen {

   
   // generic code
   protected static final int CODE_ERROR = -1;
   
   // station mode
   protected static final String MODE_LABELING = "labeling";   
   protected static final String MODE_OVERPACK = "overpack";   
   
   // carton mode
   protected static final int MODE_CARTON = 1;
   protected static final int MODE_PALLET = 2;
   
   // carton status
   protected static final String CARTON_STATUS_PICKING = "PICKING";
   protected static final String CARTON_STATUS_PICKED = "PICKED";
   protected static final String CARTON_STATUS_SHORT = "SHORT";
   protected static final String CARTON_STATUS_LABELED = "LABELED";
   protected static final String CARTON_STATUS_PACKED = "PACKED";
   protected static final String CARTON_STATUS_AUDITED = "AUDITED";
   protected static final String CARTON_STATUS_VASDONE = "VASDONE";
   protected static final String CARTON_STATUS_QC = "QC";
   protected static final String CARTON_STATUS_CANCELED = "CANCELED";   
   
   
   
   // LTL carton status
   protected final int CASE_PLLTIZED   = 1;
   protected final int CASE_SORT_LBL   = 2;
   protected final int CASE_INDUCT     = 3;
   protected final int CASE_NON_IND    = 4;

   protected static final String CARTON_LPN_REGEX = "C[A-Z0-9]{6}";
   protected static final String TOTE_LPN_REGEX = "T[A-Z0-9]{6}";
   protected static final String PALLET_LPN_REGEX = "P[A-Z0-9]{8}";
   
   protected static final int INVALID_CARTON = -1;
   protected static final int PRINT_PACKLIST = 3;
   protected static final int PRINT_SHIPLABEL = 4;
   protected static final int WEIGHT_CARTON = 5;

   protected static final int EMPTY_PALLET = -2;
   protected static final int GOOD_PALLET = 1;
   protected static final int INVALID_PALLET = -1;

   protected static final int PRINT_ERROR_DATABASE = -2;
   protected static final int PRINT_ERROR_NOLABEL = -1;

   protected static final int VALIDATE_MODE_LPN = 1;
   protected static final int VALIDATE_MODE_PARCEL_SKU = 2;
   protected static final int VALIDATE_MODE_LTL_SKU = 3;

   private String startScreen;
   protected int processMode;

   public static enum TermParam {
      scan, cartonLpn, cartonSeq, cartonUcc,pickType,cartonType,cartonStatus,trackingNumber,
      shipmentType,shipmentId,orderId,
      estWeight,estLength,estHeight,estWidth,actLength,actWidth,actHeight,actWeight,dimension,
      labelStatus,sku,shipId,
      labelSeq,needLength,needWidth,needHeight,ucc,nextLabelSeq,
      palletLpn,palletSeq,palletUcc,expCount,
      processMode,validateMode,nextScreen,audited,
      orderType,shipMethod,packlistSeq,docSeq,resultMsg,refType,refValue,palletType,
   };
   
   /** Constructs a screen with default values. */
   public AbstractPackStationScreen( TerminalDriver term ) {
      super( term );
      startScreen = "packStation.StartScreen";
      setAllDatabase();
      screenSetup();
      numOfInfoRows = 1;
      AppCommon.setDatabase(db);
   }
   
   // display methods
   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      /*
      if( processMode == MODE_PALLET_LABEL )
      	leftInfoBox.updateInfoPair(0, "Pallet", getStrParam(TermParam.palletLpn) );
      else if( processMode == MODE_LTL_PALLET_CARTON || processMode == MODE_PARCEL_PALLET_CARTON )
      	leftInfoBox.updateInfoPair(0, "Carton", getStrParam(TermParam.cartonUcc) );
      else {
         String cartonID = getStrParam(TermParam.cartonLpn);
         String cartonUcc = getStrParam(TermParam.cartonUcc);
         if( cartonID.isEmpty() )
         	cartonID = cartonUcc;
         leftInfoBox.updateInfoPair(0, "Carton", cartonID );
      }*/
      String cartonID = getStrParam(TermParam.cartonLpn);
      String cartonUcc = getStrParam(TermParam.cartonUcc);
      if( cartonID.isEmpty() )
      	cartonID = cartonUcc;
      leftInfoBox.updateInfoPair(0, "Carton", cartonID );
      leftInfoBox.show(); 
   }
   
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.hide(); 
   }

   // logic

   protected List<Map<String,String>> getExpectedSkus(int palletSeq) {
      return db.getResultMapList("SELECT sku,COUNT(sku) `count` FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) WHERE palletSeq = %d AND packStamp IS NULL AND p.pickType<>'bulb' GROUP BY sku",palletSeq) ;
   }

   protected boolean checkWeight() {
      return !getParam(TermParam.actWeight).isEmpty();
   }

   protected void getMaxValues() {
      getMaxWeight();
      getMaxWidth();
      getMaxHeight();
      getMaxLength();
   }

   protected void getMaxWeight() {
      // weight
      try {
         MAX_WEIGHT = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxWeight"));
      } catch(Exception ex) {
         //alert("Using default max weight: %.2f",MAX_WEIGHT);
      }
   }

   protected void getMaxWidth() {
      // width
      try {
         MAX_WIDTH = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxWidth"));
      } catch(Exception ex) {
         //alert("Using default max width: %.2f",MAX_WIDTH);
      }
   }

   protected void getMaxHeight() {
      // height
      try {
         MAX_HEIGHT = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxHeight"));
      } catch(Exception ex) {
         //alert("Using default max weight: %.2f",MAX_HEIGHT);
      } 
   }

   protected void getMaxLength() {
      // length
      try {
         MAX_LENGTH = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxLength"));
      } catch(Exception ex) {
         //alert("Using default max length: %.2f",MAX_LENGTH);
      }
   }

   protected boolean validateWeight(Double weight) {
      if(weight > 0 && weight <= MAX_WEIGHT) {
         updateWeight(weight);
         return true;
      }
      return false;
   }
   
   // StartScreen methods
   
   protected int lookupCartonByLpn( String barcode ) {
   	return db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn='%s'", barcode);
   }
   
   protected int lookupCartonByLabelBarcode( String barcode ) {
   	int cartonSeq = db.getInt(-1, 
   			"SELECT cartonSeq FROM rdsCartons WHERE trackingNumber='%s'", barcode);
   	return cartonSeq;
   }
  
   /*
   protected int lookupCartonBySku( String barcode ) {
   	// Rules for look up sku
   	// 1. get sku using barcode
   	// 2. check the LTL shipments with an active laneAssignment
   	// 3. check the released parcel orders
   	// 4. check the open LTL shipments without an active laneAssignment
   	// 5. check any carton that failed labeling at MEZZ
   	Map<String,String> m = db.getRecordMap("SELECT * FROM custSkus WHERE barcode='%s' LIMIT 1", barcode);
   	if( m==null || m.isEmpty() )
   		return -1;
   	String sku = getMapStr(m,"sku");
   	String uom = getMapStr(m,"uom");
   	int cartonSeq = -1;
   	int result = db.getInt(0, "SELECT GET_LOCK('cartonLookup',5)");
   	if( result>0 ) {
   		cartonSeq = db.getInt(-1, 
      			"SELECT cartonSeq FROM rdsPicks p JOIN rdsCartonsView cv USING(cartonSeq) "
      			+ "JOIN custSkus USING(sku,uom) "
	   			+ "WHERE p.pickType LIKE 'fullCase%%' AND p.sku='%s' AND barcode='%s' "
	   			+ "AND p.pickStamp IS NOT NULL AND p.shortStamp IS NULL "
	   			+ "AND cv.inductStamp IS NOT NULL AND cv.labelStamp IS NULL "
	   			+ "AND cv.cancelStamp IS NULL AND cv.failedLabeling=1 LIMIT 1",
	   			sku, barcode);
   		if( cartonSeq < 0 )
		   	cartonSeq = db.getInt(-1, 
		   			"SELECT cartonSeq FROM rdsPicks p JOIN rdsCartonsView cv USING(cartonSeq) "
		   			+ "JOIN rdsLaneAssignments la USING(shipmentId) "
		   			+ "JOIN custSkus USING(sku,uom) "
		   			+ "WHERE p.pickType LIKE 'fullCase%%' AND p.sku='%s' AND barcode='%s' "
		   			+ "AND p.pickStamp IS NOT NULL AND p.shortStamp IS NULL "
		   			+ "AND cv.inductStamp IS NULL AND cv.cancelStamp IS NULL "
		   			+ "AND la.isActive=1 AND la.complete=0 LIMIT 1", sku, barcode);
	   	if( cartonSeq < 0 )
	   		cartonSeq = db.getInt(-1, 
	      			"SELECT cartonSeq FROM rdsPicks p JOIN rdsCartonsView cv USING(cartonSeq) "
	      			+ "JOIN custSkus USING(sku,uom) "
		   			+ "WHERE p.pickType LIKE 'fullCase%%' AND p.sku='%s' AND barcode='%s' "
		   			+ "AND p.pickStamp IS NOT NULL AND p.shortStamp IS NULL "
		   			+ "AND cv.shipmentType<>'LTL' AND cv.inductStamp IS NULL AND cv.cancelStamp IS NULL LIMIT 1",
		   			sku, barcode);
	   	if( cartonSeq < 0 )
	      	cartonSeq = db.getInt(-1, 
	      			"SELECT cartonSeq FROM rdsPicks p JOIN rdsCartonsView cv USING(cartonSeq) "
	      		   + "JOIN rdsLaneAssignments la USING(shipmentId) "
	      		   + "JOIN custSkus USING(sku,uom) "
	      			+ "WHERE p.pickType LIKE 'fullCase%%' AND p.sku='%s' AND barcode='%s' "
	      			+ "AND p.pickStamp IS NOT NULL AND p.shortStamp IS NULL "
	      			+ "AND cv.inductStamp IS NULL AND cv.cancelStamp IS NULL "
	      			+ "AND la.isActive=0 AND la.complete=1 LIMIT 1", sku, barcode);
	   	if( cartonSeq < 0 )
	      	cartonSeq = db.getInt(-1, 
	      			"SELECT cartonSeq FROM rdsPicks p JOIN rdsCartonsView cv USING(cartonSeq) "
	      			+ "JOIN custSkus USING(sku,uom) "
	      			+ "WHERE p.pickType LIKE 'fullCase%%' AND p.sku='%s' AND barcode='%s' "
	      			+ "AND p.pickStamp IS NOT NULL AND p.shortStamp IS NULL "
	      			+ "AND cv.inductStamp IS NULL AND cv.cancelStamp IS NULL "
	      			+ "LIMIT 1", sku, barcode);	   	
   	}
   	db.getInt(0, "SELECT RELEASE_LOCK('cartonLookup')");
   	return cartonSeq;
   }*/
   
   protected int lookupPalletByLpn( String barcode ) {
   	return db.getInt(-1, "SELECT palletSeq FROM rdsPallets WHERE lpn='%s'", barcode);
   }
   
   protected void setCarton( int cartonSeq ) {
   	Map<String,String> map = db.getRecordMap(
   			"SELECT c.*, orderType, shippingMethod FROM rdsCartons c "
   			+ "JOIN custOrders USING(orderId) "
   			+ "JOIN custShipments USING(shipmentId) WHERE cartonSeq=%d",cartonSeq);
   	String cartonLpn = getMapStr(map,"lpn");
   	String orderId = getMapStr(map,"orderId");
   	String pickType = getMapStr(map,"pickType");
   	String cartonType = getMapStr(map,"cartonType");
   	String orderType = getMapStr(map,"orderType");
   	String shipMethod = getMapStr(map,"shippingMethod");
   	double estWeight = getMapDouble(map,"estWeight");
   	double estLength = getMapDouble(map,"estLength");
   	double estWidth = getMapDouble(map,"estWidth");
   	double estHeight = getMapDouble(map,"estHeight");
   	double actLength = getMapDouble(map,"actLength");
   	double actWidth = getMapDouble(map,"actWidth");
   	double actHeight = getMapDouble(map,"actHeight");   	
   	String trackingNumber = getMapStr(map,"trackingNumber");
   	String ucc = getMapStr(map,"ucc");
   	boolean canceled = !getMapStr(map,"cancelStamp").isEmpty();
   	boolean picked = !getMapStr(map,"pickStamp").isEmpty();
   	boolean shortPicked = !getMapStr(map,"shortStamp").isEmpty();
   	boolean packed = !getMapStr(map,"packStamp").isEmpty();
   	boolean audited = !getMapStr(map,"auditStamp").isEmpty();
   	boolean labeled = !getMapStr(map,"labelStamp").isEmpty();
   	   	
   	String dimension = String.format("%.2fX%.2fX%.2f", estLength,estWidth,estHeight);
   	String cartonStatus = (canceled)? CARTON_STATUS_CANCELED :
   								 (labeled)? CARTON_STATUS_LABELED :
									 (packed)? CARTON_STATUS_PACKED :
   								 (audited)? CARTON_STATUS_AUDITED:
   								 (shortPicked)?CARTON_STATUS_SHORT:
   								 (picked)? CARTON_STATUS_PICKED:CARTON_STATUS_PICKING;
   	setParam(TermParam.cartonSeq,""+cartonSeq);
   	setParam(TermParam.cartonUcc,ucc);
   	setParam(TermParam.orderId,orderId);
   	setParam(TermParam.cartonLpn,cartonLpn);
   	setParam(TermParam.pickType,pickType);
   	setParam(TermParam.estWeight,estWeight+"");
   	setParam(TermParam.cartonType,cartonType);
   	setParam(TermParam.cartonStatus,cartonStatus);
   	setParam(TermParam.dimension,dimension);
   	setParam(TermParam.estLength,""+estLength);
   	setParam(TermParam.estWidth,""+estWidth);
   	setParam(TermParam.estHeight,""+estHeight);
   	setParam(TermParam.actLength,""+actLength);
   	setParam(TermParam.actWidth,""+actWidth);
   	setParam(TermParam.actHeight,""+actHeight);
   	setParam(TermParam.trackingNumber,trackingNumber);
   	setParam(TermParam.orderType,orderType);
   	setParam(TermParam.shipMethod,shipMethod);
   	setParam(TermParam.audited, audited?"true":"false" );
   	setParam(TermParam.refType, "Carton");
   	setParam(TermParam.refValue, cartonLpn);
   	setParam(TermParam.processMode, MODE_CARTON+"");
   } 

   // Packlist is required on the first box seen at a pack station for a parcel shipment
   protected boolean requirePacklist() {
   	String orderId = getStrParam(TermParam.orderId);
   	int packlistSeq = db.getInt(-1, "SELECT docSeq FROM rdsDocuments WHERE docType='packlist' AND refValue='%s'", orderId);
   	if( packlistSeq < 0 ) return false;
   	int notPackedCarton = db.getInt(-1, 
   			"SELECT COUNT(*) FROM rdsCartons WHERE orderId='%s' AND packStamp IS NULL AND cancelStamp IS NULL", orderId);
   	if( notPackedCarton > 1 ) return false;
   	setParam(TermParam.packlistSeq,""+packlistSeq);
   	return true;
   }
   
   protected int printPacklist() {
      return printRdsDocument(getIntParam(TermParam.packlistSeq),getStrParam("hpPrinter"));
   }
   
   protected int printRdsDocument(int docSeq, String printer) {
      int result = db.execute(
            "INSERT INTO docs (id, doc) " +
            "SELECT docSeq, FROM_BASE64(printDoc) FROM rdsDocuments " +
            "WHERE docSeq=%s", docSeq
         );
      if( result <= 0 ) return PRINT_ERROR_DATABASE;
      int docsTableDocSeq = db.getSequence();
      result = db.execute("INSERT INTO docQueue SET device='%s', docSeq=%d", printer, docsTableDocSeq);
      if( result <= 0 ) return PRINT_ERROR_DATABASE;
      return db.getSequence();
   }   
   
   //TODO
   protected boolean requireQC() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	String shipmentId = getStrParam(TermParam.shipmentId);
   	boolean pickRequireQc = db.getInt(0, "SELECT SUM(qcRequired) FROM rdsPicks JOIN custOrderLines USING(orderLineSeq) "
   			+ "WHERE cartonSeq=%d", cartonSeq)>0;
   	boolean shipmentRequireQc = db.getInt(0, "SELECT qcRequired FROM custShipments WHERE shipmentId='%s'", shipmentId)>0;
   	return pickRequireQc || shipmentRequireQc;
   }
   
   protected boolean cartonIsAudited() {
   	return getStrParam(TermParam.audited).equals("true");
   }
   
   protected boolean isTote() {
   	String pickType = getStrParam(TermParam.pickType);
   	return pickType.equals(PICKTYPE_TOTE);  	
   }
   
   protected boolean isToteOrSplitCase() {
   	String pickType = getStrParam(TermParam.pickType);
   	return pickType.equals(PICKTYPE_TOTE) || pickType.equals(PICKTYPE_SPLITCASE);
   }
   
   protected boolean isParcel() {
   	String orderType = getStrParam(TermParam.orderType);
   	return orderType.equals(ORDERTYPE_PARCEL);
   }
   
   protected int shipLabelRequestStatus() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	int uploadSeq = db.getInt(-1, "SELECT dataValue FROM rdsCartonData WHERE cartonSeq=%d AND dataType='shipLabelRequest'", cartonSeq);
   	if( uploadSeq <0 ) return -2;
   	String uploadStatus = db.getString("", "SELECT status FROM rdsUploadQueue WHERE uploadSeq=%d", uploadSeq);
   	if( uploadStatus.equals("error") ) return -1;
   	if( uploadStatus.equals("uploaded")) return 1;
   	return 0;
   }
   
   protected void markCartonAudited() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	AppCommon.triggerCartonAudited(cartonSeq, getOperatorId());
   	trace("cartonSeq %d audited at work station", cartonSeq);   	
   }

   protected void markCartonPacked() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	AppCommon.triggerCartonPacked(cartonSeq, getOperatorId());
   	trace("cartonSeq %d packed at work station", cartonSeq);   	
   }
   
   protected boolean isLTLPallet( int palletSeq ) {
   	return db.getInt(-1, 
   			"SELECT palletSeq FROM rdsPallets WHERE palletSeq=%d "
   			+ "AND refType='orderId' AND refValue<>'%s'", palletSeq, ORDERTYPE_PARCEL)>0;
   }
   
   protected boolean validPalletType( String validType ) {
   	return getStrParam(TermParam.palletType).equals(validType);
   }
   
   protected String getLtlCartonDestination() {
   	String orderId = getStrParam(TermParam.orderId);
   	return db.getString("", "SELECT location FROM rdsLocations "
   			+ "WHERE locationType IN ('consolidateCell','singlePallet') AND assignmentValue='%s' LIMIT 1", orderId);
   }
   
   protected String getParcelCartonDestination() {
   	String shipMethod = getStrParam(TermParam.shipMethod);
   	return db.getString("", "SELECT sorterLane FROM cfgShipMethods "
   			+ "WHERE shipMethod='%s' LIMIT 1", shipMethod);
   }
   
   protected String getPalletDestination(int palletSeq) {
   	String orderId = db.getString("","SELECT refValue FROM rdsPallets WHERE palletSeq=%d",palletSeq);
   	return db.getString("", "SELECT location FROM rdsLocations "
   			+ "WHERE locationType IN ('consolidateCell','singlePallet') AND assignmentValue='%s' LIMIT 1", orderId);
   }
   
   protected void setPallet( int palletSeq ) {
   	Map<String,String> map = db.getRecordMap(
   			"SELECT * FROM rdsPallets WHERE palletSeq=%d",palletSeq);
   	String palletLpn = getMapStr(map,"lpn");
   	String palleType = getMapStr(map,"palletType");
   	db.execute("UPDATE rdsPallets SET lastPositionPhysical='%s' WHERE palletSeq=%d", getStrParam("stationName"),palletSeq);
   	setParam(TermParam.palletSeq, ""+palletSeq);
   	setParam(TermParam.palletLpn, palletLpn);
   	setParam(TermParam.refType, "Pallet");
   	setParam(TermParam.refValue, palletLpn);
   	setParam(TermParam.palletType, palleType);
   	setParam(TermParam.processMode, MODE_PALLET+"");
   } 
   
   protected void markPalletPicked() {
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	db.execute("UPDATE rdsPallets SET pickStamp=IFNULL(pickStamp,NOW()) WHERE palletSeq=%d",palletSeq);   	
   }
   
   protected void markPalletClosed() {
   	String operator = getOperatorId();
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	AppCommon.triggerPalletClose(palletSeq, operator); 
   }
   
   protected int numNotLabeledCarton() {
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	return db.getInt(1, "SELECT COUNT(*) FROM rdsCartons WHERE palletSeq=%d AND labelStamp IS NULL AND cancelStamp IS NULL", palletSeq );
   }
   
   // ScanSkuScreen methods  
   
   protected int lookupPalletCartonBySku( String scan ) {
   	// Rules for look up sku
   	// 1. get sku using barcode
   	// 2. check the LTL shipments with an active laneAssignment
   	// 3. check the released parcel orders
   	// 4. check the open LTL shipments without an active laneAssignment
		Map<String,String> m = 
				db.getRecordMap("SELECT sku,uom FROM custSkus WHERE barcode='%s' LIMIT 1",scan);
		String sku = getMapStr(m,"sku");
		String uom = getMapStr(m,"uom");
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	int cartonSeq = -1;
   	Map<String,String> cartonMap = db.getRecordMap( 
   			"SELECT c.cartonSeq,ucc,c.orderId,shippingMethod FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) "
   			+ "JOIN custSkus USING(sku,uom) "
   			+ "JOIN custOrders o ON p.orderId = o.orderId "
   			+ "JOIN custShipments USING(shipmentId) "
   			+ "WHERE c.palletSeq=%d AND c.labelStamp IS NULL AND c.cancelStamp IS NULL "
   			+ "AND p.sku='%s' AND p.uom='%s' AND barcode='%s' LIMIT 1", 
   			palletSeq, sku, uom, scan);
		if( cartonMap!=null && !cartonMap.isEmpty() ) {
			cartonSeq = getMapInt(cartonMap,"cartonSeq");
			setParam(TermParam.cartonSeq,""+cartonSeq);  
			setParam(TermParam.cartonUcc, getMapStr(cartonMap,"ucc"));
			setParam(TermParam.orderId, getMapStr(cartonMap,"orderId"));
			setParam(TermParam.shipMethod, getMapStr(cartonMap,"shippingMethod"));
		}
   	return cartonSeq;
   }
   
   protected int lookupPalletCartonBySku( String sku, String uom ) {
   	// Rules for look up sku
   	// 1. get sku using barcode
   	// 2. check the LTL shipments with an active laneAssignment
   	// 3. check the released parcel orders
   	// 4. check the open LTL shipments without an active laneAssignment
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	int cartonSeq = -1;
   	Map<String,String> cartonMap = db.getRecordMap( 
   			"SELECT c.cartonSeq,ucc,c.orderId,shippingMethod FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) "
   			+ "JOIN custSkus USING(sku,uom) "
   			+ "JOIN custOrders o ON p.orderId = o.orderId "
   			+ "JOIN custShipments USING(shipmentId) "
   			+ "WHERE c.palletSeq=%d AND c.labelStamp IS NULL AND c.cancelStamp IS NULL "
   			+ "AND p.sku='%s' AND p.uom='%s' LIMIT 1", 
   			palletSeq, sku, uom);
		if( cartonMap!=null && !cartonMap.isEmpty() ) {
			cartonSeq = getMapInt(cartonMap,"cartonSeq");
			setParam(TermParam.cartonSeq,""+cartonSeq);  
			setParam(TermParam.cartonUcc, getMapStr(cartonMap,"ucc"));
			setParam(TermParam.orderId, getMapStr(cartonMap,"orderId"));
			setParam(TermParam.shipMethod, getMapStr(cartonMap,"shippingMethod"));
		}
   	return cartonSeq;
   }
   
   protected List<Map<String,String>> getPalletCartonSkuList() {
   	int palletSeq = getIntParam(TermParam.palletSeq);
   	return db.getResultMapList("SELECT p.sku,p.uom,i.barcode, "
   			+ "COUNT(*) AS totalQty, COUNT(CASE WHEN c.labelStamp IS NOT NULL THEN c.cartonSeq END) AS labeledQty "
   			+ "FROM rdsPicks p "
   			+ "JOIN rdsCartons c USING(cartonSeq) "
   			+ "JOIN custSkus i ON p.sku=i.sku AND p.uom=i.uom "
   			+ "WHERE c.palletSeq=%d AND c.cancelStamp IS NULL "
   			+ "GROUP BY p.sku,p.uom ",palletSeq);
   } 
     
   
   // AuditScreen methods
   
   protected List<Map<String,String>> getAuditPickList() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	return db.getResultMapList("SELECT p.sku,p.uom,i.barcode,ol.location, "
   			+ "COUNT(CASE WHEN p.canceled=0 THEN pickSeq END) AS totalQty, "
   			+ "COUNT(CASE WHEN p.picked=1 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS pickedQty, 0 AS scannedQty, "
   			+ "COUNT(CASE WHEN p.picked=0 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS openQty, "
   			+ "COUNT(CASE WHEN p.shortPicked=1 AND p.canceled=0 THEN pickSeq END) AS shortQty "
   			+ "FROM rdsPicks p "
   			+ "JOIN custSkus i ON p.sku=i.sku AND p.uom=i.uom "
   			+ "JOIN custOrderLines ol USING(orderLineSeq) "
   			+ "WHERE cartonSeq=%d AND p.canceled=0 GROUP BY sku,uom ",cartonSeq);
   }
   
   protected boolean isValidPassword(String operatorID) {
   	return db.getInt(0, "SELECT auditAllowed FROM proOperators WHERE operatorID='%s'", operatorID)==1;
   }
   
   protected void markOpenPickPicked() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   			+ "WHERE cartonSeq=%d "
   			+ "AND picked=0 AND canceled=0", 
   			cartonSeq);
   	for( String pickSeq_str : picks ) {
   		int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
   		if( pickSeq>0 ) {
   			db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),shortPicked=0,shortStamp=NULL,"
   					+ "pickOperatorId='%s' WHERE pickSeq=%d", getOperatorId(),pickSeq);
   			JSONObject json = new JSONObject() ;
		  	  	json.put("pickSeq",pickSeq) ;
		  	  	db.execute("INSERT status SET statusType='pick', data='%s', operator='' ,status='idle'",json.toString()); 
   		}
   	}
   }
   
   protected void markOpenPickShipShort(List<Map<String,String>> itemList) {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	for( Map<String,String> m : itemList ) {
   		int totalQty = getMapInt(m,"totalQty");
   		int scannedQty = getMapInt(m,"scannedQty");
   		int pickedQty = getMapInt(m,"pickedQty");
   		String sku = getMapStr(m,"sku");
   		String uom = getMapStr(m,"uom");
   		if( scannedQty > pickedQty ) {
   			int n = scannedQty - pickedQty;
   			List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   					+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s' AND picked=0 AND canceled=0 "
   					+ "ORDER BY shortPicked LIMIT %d", cartonSeq,sku,uom,n);
   			for( String pickSeq_str : picks ) {
   				int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
      			db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),shortPicked=0,shortStamp=NULL,"
      					+ "pickOperatorId='%s' WHERE pickSeq=%d", getOperatorId(),pickSeq);
      			JSONObject json = new JSONObject() ;
   		  	  	json.put("pickSeq",pickSeq) ;
   		  	  	db.execute("INSERT status SET statusType='pick', data='%s', operator='' ,status='idle'",json.toString()); 
   			}
   		}
   		if( scannedQty < totalQty ) {
   			int n = totalQty - scannedQty;
   			List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   					+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s' AND canceled=0 "
   					+ "ORDER BY picked, shortPicked LIMIT %d", cartonSeq,sku,uom,n);
   			for( String pickSeq_str : picks ) {
   				int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
      			db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),shortPicked=1,shortStamp=NOW(),"
      					+ "pickOperatorId='%s' WHERE pickSeq=%d", getOperatorId(),pickSeq);
      			JSONObject json = new JSONObject() ;
   		  	  	json.put("pickSeq",pickSeq) ;
   		  	  	db.execute("INSERT status SET statusType='shipShort', data='%s', operator='' ,status='idle'",json.toString()); 
   			}
   		}
   	}
   }   
   
   protected void createPickTicket( List<Map<String,String>> itemList) {
   	String orderId = getParam(TermParam.orderId);
   	String cartonLpn = getParam(TermParam.cartonLpn);
   	String thisPage = db.getString("", "SELECT template FROM labelTemplate WHERE type='pickTicket'");
		thisPage = thisPage.replaceAll("RDS_ORDERID", orderId);
		thisPage = thisPage.replaceAll("RDS_LPN", cartonLpn);
		int yPos = 270;
		for( Map<String,String> itemMap : itemList ) {
   		int totalQty = getMapInt(itemMap,"totalQty");
   		int scannedQty = getMapInt(itemMap,"scannedQty");
   		if( scannedQty == totalQty ) continue;
			thisPage = thisPage.concat(String.format("^FO0,%d^GB812,2,2^FS\n", yPos));
			yPos +=10;
			int numOfItem = 0;
			int numOfDesc = 0;
			String sku = getMapStr(itemMap,"sku");
			String uom = getMapStr(itemMap,"uom");
			String location = getMapStr(itemMap,"location");
			int qty = totalQty - scannedQty;
			int maxSkuLength = 13;
			String currentSku = sku;
			while( currentSku.length()>maxSkuLength) {
				int cutoffIndex = maxSkuLength-1;
				for( int i=maxSkuLength-1;i>=0;i-- ) {
					if( !Character.isLetterOrDigit(currentSku.charAt(i)) ) {
						cutoffIndex = i;
						break;
					}
				}
				thisPage = thisPage.concat(String.format("^FO20,%d^FD%s^FS\n", yPos+numOfItem*20,currentSku.substring(0,cutoffIndex+1) ));
				numOfItem++;
				currentSku = currentSku.substring(cutoffIndex+1);				
			}
			if( currentSku.length()>0) {
				thisPage = thisPage.concat(String.format("^FO20,%d^FD%s^FS\n", yPos+numOfItem*20,currentSku ));
				numOfItem++;				
			}
			thisPage = thisPage.concat(String.format("^FO150,%d^FD%s^FS\n", yPos,uom));
			thisPage = thisPage.concat(String.format("^FO210,%d^FD%s^FS\n", yPos,location));
			thisPage = thisPage.concat(String.format("^FO640,%d,1^FD%d^FS\n", yPos,qty));
			yPos += 20*Math.max(numOfItem, numOfDesc);
		}
		thisPage = thisPage.concat("^XZ");
		db.executePreparedStatement("INSERT INTO docs SET doc=?, id=?", 
				thisPage,getParam(TermParam.cartonSeq));		
		int docSeq = db.getSequence();
		trace("picking ticket %d created for carton %s",docSeq,getParam(TermParam.cartonLpn));
		setParam(TermParam.docSeq,""+docSeq);
   }

   protected int printPickTicket() {
      return printDoc(getIntParam(TermParam.docSeq),getStrParam("zebraPrinter"));
   }  
   
   // WeightAuditScreen methods
   
   protected boolean isValidWeight( double weight ) {
   	double estWeight = getDoubleParam(TermParam.estWeight);
   	String weighCheckMode = db.getControl("audit", "mode","on");
   	double absoluteTolerance = RDSUtil.stringToDouble(db.getControl("audit", "weightToleranceAbsolute","1"), 1.0 );
   	double tolerancePercent = RDSUtil.stringToDouble(db.getControl("audit", "weightTolerancePercent","10"), 10.0 );
   	if( weighCheckMode.equals("off") )
   		return true;
   	else {
   		double tolerance = Math.max(absoluteTolerance, tolerancePercent * estWeight / 100 );
   		return (weight >= ( estWeight - tolerance )) && ( weight <= ( estWeight + tolerance )) ;
      }
   }

   // CartonWeightScreen, ConfirmDimScreen, CartonLengthScreen, CartonWidthScreen, CartonHeightScreen methods
   
   protected void updateWeight(Double weight) {
      db.execute("UPDATE rdsCartons SET actWeight='%.2f' WHERE cartonSeq =%d",weight,getIntParam(TermParam.cartonSeq));
   }
   
   protected void useEstWeight() {
   	db.execute("UPDATE rdsCartons SET actWeight=estWeight WHERE cartonSeq =%d",getIntParam(TermParam.cartonSeq));
   }
   
   protected boolean hasActDim() {
   	return getDoubleParam(TermParam.actLength)>0 && 
   			getDoubleParam(TermParam.actWidth)>0 &&
   			getDoubleParam(TermParam.actHeight)>0;
   } 
   
   protected void triggerShipLabelRequest() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	String operator = getOperatorId();
   	AppCommon.triggerShipLabelRequest(cartonSeq, operator);
   }
   
   protected String getEstDim() {
   	return String.format("%.2fx%.2fx%.2f", getDoubleParam(TermParam.estLength),
   			getDoubleParam(TermParam.estWidth),getDoubleParam(TermParam.estHeight));
   }
   
   protected void useEstDim() {
   	db.execute("UPDATE rdsCartons SET actLength='%.2f',actWidth='%.2f',actHeight='%.2f' "
   			+ "WHERE cartonSeq =%d",getDoubleParam(TermParam.estLength),getDoubleParam(TermParam.estWidth),
   			getDoubleParam(TermParam.estHeight),getIntParam(TermParam.cartonSeq));
   }
   
   protected String getActDim() {
   	return String.format("%.2fx%.2fx%.2f", getDoubleParam(TermParam.actLength),
   			getDoubleParam(TermParam.actWidth),getDoubleParam(TermParam.actHeight));
   }
   
   protected void updateActDim() {
   	db.execute("UPDATE rdsCartons SET actLength='%.2f',actWidth='%.2f',actHeight='%.2f' "
   			+ "WHERE cartonSeq =%d",getDoubleParam(TermParam.actLength),getDoubleParam(TermParam.actWidth),
   			getDoubleParam(TermParam.actHeight),getIntParam(TermParam.cartonSeq));
   }   
   
   protected void updateCartonType(String cartonType) {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	Map<String,String> m = db.getRecordMap("SELECT * FROM cfgCartonTypes WHERE cartonType='%s'", cartonType);
   	if( m==null || m.isEmpty() )
   		db.execute("UPDATE rdsCartons SET cartonType='%s' WHERE cartonSeq=%d", cartonType, cartonSeq);
   	else {
   		db.execute("UPDATE rdsCartons SET cartonType='%s',"
   				+ "estLength='%.2f', estWidth='%.2f', estHeight='%.2f',"
   				+ "actLength='%.2f', actWidth='%.2f', actHeight='%.2f' WHERE cartonSeq=%d", 
   				cartonType, getMapDouble(m,"exteriorLength"),getMapDouble(m,"exteriorWidth"),getMapDouble(m,"exteriorHeight"),
   				getMapDouble(m,"exteriorLength"),getMapDouble(m,"exteriorWidth"),getMapDouble(m,"exteriorHeight"),cartonSeq);
   	}
   }
   
   
   // WaitLabelScreen methods
   
   protected boolean requireUccLabel() {
   	return db.getInt(0, "SELECT uccCartonLabelRequired FROM custShipments WHERE shipmentId='%s'", getStrParam(TermParam.shipmentId))==1;
   }
   
   protected boolean requirePalletLabel() {
   	return db.getInt(0, "SELECT uccPalletLabelRequired FROM custShipments WHERE shipmentId='%s'", getStrParam(TermParam.shipmentId))==1;
   }
   
   // VerifyLabelScreen & PrintLabelScreen methods
   protected List<Map<String,String>> getPalletLabels(){
   	String palletUcc = getStrParam(TermParam.palletUcc);
   	List<Map<String,String>> list = db.getResultMapList(
   			"SELECT docSeq,docType,verification FROM rdsDocuments "
   			+ "WHERE refType='ucc' AND refValue='%s' AND docType NOT IN ('packlist','cartonContent')",palletUcc);
   	for( Map<String,String> m : list ) {
   		String docType = getMapStr(m,"docType");
   		String verification = getMapStr(m,"verification");
   		m.put("docTypeDisplay", docType+" Pallet Label");
   		if( verification.isEmpty() )
   			m.put("status", "verify not required");
   		else
   			m.put("status", "use button to confirm");
   	}
   	return list;
   }
   
   protected List<Map<String,String>> getCartonLabels(){
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<Map<String,String>> list = db.getResultMapList(
   			"SELECT docSeq,docType,verification FROM rdsDocuments "
   			+ "WHERE refType='cartonSeq' AND refValue='%d' AND docType='shipLabel' "
   			+ "ORDER BY docSeq DESC LIMIT 1",cartonSeq);
   	for( Map<String,String> m : list ) {
   		String docType = getMapStr(m,"docType");
   		String verification = getMapStr(m,"verification");
   		m.put("docTypeDisplay", docType);
   		if( verification.isEmpty() )
   			m.put("status", "use button to confirm");
   		else
   			m.put("status", "Scan label to verify");
   	}
   	return list;
   }
   
   protected int printDoc(int docSeq, String printer) {
      int result = db.execute("INSERT INTO docQueue SET device='%s', docSeq=%d", printer, docSeq);
      if( result <= 0 ) return PRINT_ERROR_DATABASE;
      return db.getSequence();
   }
   
   protected int getPrintJobStatus( int seq ) {
      return db.getInt(0, "SELECT complete FROM docQueue WHERE queueSeq=%d", seq);
   }
   
   protected void markCartonLabeled() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	db.execute("UPDATE rdsCartons SET labelStamp=IFNULL(labelStamp,NOW()) WHERE cartonSeq=%d", cartonSeq);
   	AppCommon.triggerCartonLabeled(cartonSeq, getOperatorId());
   	trace("cartonSeq %d complete label process at work station", cartonSeq);   	
   }  
   
   protected String getParcelDestination() {
   	String shipmentId = getStrParam(TermParam.shipmentId);
   	if( shipmentId.isEmpty() )
   		shipmentId = db.getString("", "SELECT shipmentId FROM rdsCartonsView WHERE cartonSeq=%d", getIntParam(TermParam.cartonSeq));
   	String shipMethod = db.getString("", "SELECT shipMethod FROM custShippingInfo JOIN custShipments USING(shipInfoSeq) WHERE shipmentId='%s'", getStrParam(TermParam.shipmentId));
   	String lane = db.getString("", "SELECT sorterLane FROM cfgShipMethods WHERE shipMethod='%s'", shipMethod);
   	return String.format("%s lane %s", shipMethod, lane);
   }
   
   protected String getLtlDestination() {
   	String shipmentId = getStrParam(TermParam.shipmentId);
   	String lane = db.getString("", 
   			"SELECT lane FROM rdsLaneAssignments WHERE shipmentId='%s' AND isActive=1 AND complete=0", shipmentId);
   	if( lane.isEmpty() )
   		lane = db.getString("", 
      			"SELECT lane FROM rdsLaneAssignments WHERE shipmentId='%s' AND complete=0", shipmentId);
   	if( !lane.isEmpty() ) {
   		return String.format("sorter lane %s", lane);
   	} else {
   		Map<String,String> m = db.getRecordMap("", "SELECT lastPositionLogical FROM rdsPallets "
   				+ "WHERE refType='shipmentId' AND refValue='%s' AND closeStamp IS NULL", shipmentId);
   		if( m == null || m.isEmpty() )
   			m = db.getRecordMap("", "SELECT lastPositionLogical FROM rdsPallets "
      				+ "WHERE refType='shipmentId' AND refValue='%s' AND lastPositionLogical IS NOT NULL", shipmentId);
   		if( m == null || m.isEmpty() ) {
   			return "";
   		}
   		String lastPositionLogical = getMapStr(m,"lastPositionLogical");
   		String palletLpn = getMapStr(m,"lpn");
   		if( palletLpn.isEmpty() )
   			return "";
   		String position = lastPositionLogical.isEmpty()?"unknown loc":lastPositionLogical;
   		return String.format("%s at %s", palletLpn,position);
   	}
   }
   
   // ValidateCartonScreen methods
   protected int getValidateMode() {
   	return VALIDATE_MODE_LPN;
   }
   
   protected Map<String,String> getValidateInfo(){
   	Map<String,String> m = new HashMap<>();
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	String cartonLpn = getStrParam(TermParam.cartonLpn);
   	if( !cartonLpn.isEmpty() ) {
   		m.put("validateType", "LPN");
   		m.put("validateValue", cartonLpn);
   	} else {
   		m.put("validateType", "SKU");
   		Map<String,String> skuMap = db.getRecordMap(
   				"SELECT sku,uom FROM rdsPicks WHERE cartonSeq=%d AND pickType<>'bulb' LIMIT 1", cartonSeq);
   		String sku = getMapStr(skuMap,"sku");
   		String uom = getMapStr(skuMap,"uom");
   		m.put("validateValue", sku);
   		m.put("validateUom", uom);
   	}
   	return m;
   }
   
   // ConfirmCartonTypeScreen methods
   protected void updateCartonTypeAndDim( String suggested, String cartonType ) {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	inform("Update cartonSeq %d cartonType from %s to %s",cartonSeq,suggested,cartonType);
   	double suggestedTareWeight = RDSUtil.stringToDouble(
   			db.getString("0", "SELECT tareWeight FROM cfgCartonTypes WHERE cartonType='%s'", suggested), 0);
   	setParam(TermParam.cartonType, cartonType );
   	Map<String,String> m = db.getRecordMap("SELECT * FROM cfgCartonTypes WHERE cartonType='%s'", cartonType);
   	db.execute("UPDATE rdsCartons SET cartonType='%s', "
   			+ "actLength=%.3f, actWidth=%.3f, actHeight=%.3f, "
   			+ "estWeight=( estWeight-%.3f+%.3f ) WHERE cartonSeq=%d",
   			cartonType,getMapDouble(m,"exteriorLength"),getMapDouble(m,"exteriorWidth"),getMapDouble(m,"exteriorHeight"),
   			suggestedTareWeight,getMapDouble(m,"tareWeight"),cartonSeq);
   }
   
   protected List<Map<String,String>> getCartonTypes(){
   	return db.getResultMapList("SELECT * FROM cfgCartonTypes WHERE enabled=1");
   }
   
   // ScanLpnScreen methods
   protected boolean isValidCartonLpn( String barcode ) {
      Pattern pat = Pattern.compile(CARTON_LPN_REGEX,Pattern.CASE_INSENSITIVE);
      Matcher match = pat.matcher(barcode);
      if(!match.find()) {
         return false;
      }
      return db.getInt(1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn='%s'", barcode)==0;
   }
   
   protected void updateCartonLpn( String barcode ) {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	trace( "update cartonSeq %d lpn %s", cartonSeq, barcode );
   	setParam(TermParam.cartonLpn, barcode);
   	db.execute("UPDATE rdsCartons SET lpn='%s' WHERE cartonSeq=%d", barcode, cartonSeq);
   }

   protected boolean validateWidth(Double width) {
      if(width > 0 && width <= MAX_WIDTH) {
         updateWidth(width);
         return true;
      } else
         return false;
   }

   protected void updateWidth(Double width) {
      db.execute("UPDATE rdsCartons SET actWidth=%f WHERE cartonSeq = %d",width, getIntParam(TermParam.cartonSeq));
   }

   protected boolean validateHeight(Double height) {
      if(height > 0 && height <= MAX_HEIGHT) {
         updateHeight(height);
         return true;
      } else
         return false;
   }

   protected void updateHeight(Double height) { 
      db.execute("UPDATE rdsCartons SET actHeight=%f WHERE cartonSeq = %d",height, getIntParam(TermParam.cartonSeq));
   }

   protected boolean validateLength(Double length) {
      if(length > 0 && length <= MAX_LENGTH) {
         updateLength(length);
         return true;
      } else
         return false;
   }

   protected void updateLength(Double length) {
      db.execute("UPDATE rdsCartons SET actLength=%f WHERE cartonSeq = %d",length, getIntParam(TermParam.cartonSeq));
   }

   protected boolean showNoKeyboard() {
      return db.getControl(host,"tm-pk1","showNoKeyboard").equalsIgnoreCase("true")?true:false;
   }
   
   // helpers

   public void handleTick() {
      super.handleTick();
      cycleCheck();
      if( start > 0 && System.currentTimeMillis() - start > reset )
      	doOkay();
   }

   protected void screenSetup() {
      cycle = 0;
      cycleCount = false;
      cycleMax = getIntControl("cycleMax")*2;
   }

   protected void cycleCheck() {
      if(cycleMax > 0 && cycleCount) {
         if(cycle++ >= cycleMax) {
            hideResultMsgModule();
         }
      } else
         cycle = 0;
   }

   protected boolean isNumeric(String strNum) {
   	if(strNum == null) return false;
   	Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
   	return pattern.matcher(strNum).matches();
   }   
   
   protected int getIntParam( TermParam param ) {
      return RDSUtil.stringToInt( term.fetchAtom( param.toString(), "" ), -1 );
   }

   protected boolean getBoolParam( TermParam param ) {
      return term.fetchAtom( param.toString(), "" ).equalsIgnoreCase("true");
   }
   
   protected double getDoubleParam( TermParam param ) {
      return RDSUtil.stringToDouble( term.fetchAtom( param.toString(), "" ), 0.00 );
   }

   protected String getStrParam( TermParam param ) {
      return term.fetchAtom( param.toString(), "" );
   }
   
   protected String getParam( TermParam param ) {
      return term.fetchAtom( param.toString(), null );
   }
   
   protected void setParam( TermParam param, String format, Object... args ) {
      if(format == null)
         term.dropAtom( param.toString() );
      else
         term.saveAtom( param.toString(), String.format(format, args) );
   }

   protected void clearAllParam() {
      for( TermParam param : TermParam.values())
         term.dropAtom(param.toString());
   }

   protected void gotoStartScreen() {
      setNextScreen(startScreen);
   }
   
   @Override
   protected void doCancel(){
   	inform("cancel button pressed");
   	setNextScreen("packStation.StartScreen");
   }

}

