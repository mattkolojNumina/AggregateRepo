package termApp.exception;

import static rds.RDSLog.*;
import java.util.regex.Pattern;


import java.util.List;
import java.util.Map;

import term.TerminalDriver;
import termApp.*;
import rds.RDSUtil;

import dao.SloaneCommonDAO;

public abstract class AbstractExceptionStationScreen extends AbstractProjectAppScreen {

       // generic code
   protected static final int CODE_ERROR = -1;
   
   // LPN result
   protected static final String LPN_EXIST = "EXIST";
   protected static final String LPN_INVALID_FORMAT = "INVALIDFORMAT";
   
   // carton status
   protected static final String CARTON_STATUS_PICKING = "PICKING";
   protected static final String CARTON_STATUS_PICKED = "PICKED";
   protected static final String CARTON_STATUS_SHORT = "SHORT";
   protected static final String CARTON_STATUS_LABELED = "LABELED";
   protected static final String CARTON_STATUS_PACKED = "PACKED";
   protected static final String CARTON_STATUS_REQUIREAUDIT = "REQUIREAUDIT";
   protected static final String CARTON_STATUS_VASDONE = "VASDONE";
   protected static final String CARTON_STATUS_QC = "QC";
   protected static final String CARTON_STATUS_CANCELED = "CANCELED";   
   protected static final String CARTON_STATUS_REPACK = "REPACK";
   
   
   
   // LTL carton status
   protected final int CASE_PLLTIZED   = 1;
   protected final int CASE_SORT_LBL   = 2;
   protected final int CASE_INDUCT     = 3;
   protected final int CASE_NON_IND    = 4;

   protected static final String CARTON_LPN_REGEX = "C\\d{8}";
   protected static final String TOTE_LPN_REGEX = "T\\d{4}";
   protected static final String PALLET_LPN_REGEX = "P\\d{8}";
   
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

    public AbstractExceptionStationScreen(TerminalDriver term) {
        super(term);
        setAllDatabase();
        numOfInfoRows = 1;
        cycleMax = 10;
    }
    
    public static enum TermParam {
       scan, cartonLpn, cartonSeq, cartonUcc,pickType,cartonType,cartonStatus,trackingNumber,
       shipmentType,shipmentId,orderId,
       estWeight,estLength,estHeight,estWidth,actLength,actWidth,actHeight,actWeight,dimension,
       labelStatus,sku,shipId,
       labelSeq,needLength,needWidth,needHeight,ucc,nextLabelSeq,
       palletLpn,palletSeq,palletUcc,expCount,
       processMode,docSeq,exceptionReason,repackRequired,
       newCartonType, newCartonLpn,lpnPrefix,uom,qty,makeUpQty,
    };    
   
   /** Constructs a screen with default values. */
   //public AbstractAuditStationScreen( TerminalDriver term ) {
   //   super( term );
   //   startScreen = "auditStation.StartScreen";
   //   setAllDatabase();
   //   numOfInfoRows = 1;
   //   AppCommon.setDatabase(db);
   //}
   
   // display methods
   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      String cartonID = getParam(TermParam.cartonLpn);
      leftInfoBox.updateInfoPair(0, "Carton LPN", cartonID );
      leftInfoBox.show(); 
   }
   
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Invoice", getParam(TermParam.orderId));
      rightInfoBox.show(); 
   }
   
   // StartScreen methods  
   
   protected int lookupCartonByLpn( String barcode ) {
   	int cartonSeq = db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn='%s' ORDER BY cartonSeq DESC LIMIT 1", barcode);
   	//check if there are GeekShort cartons that can use the scanned lpn. Only for east pack
   	if( cartonSeq<0 && getStationArea().equalsIgnoreCase("east") ) {
	      List<Map<String,String>> cartonTypes = db.getResultMapList(
	      		"SELECT cartonType,lpnFormat FROM cfgCartonTypes WHERE lpnFormat<>''");
	      String cartonType = "";
	      for( Map<String,String> m : cartonTypes ) {
	      	String lpnFormat = getMapStr(m,"lpnFormat");
	         if( barcode.matches(lpnFormat) ) {
	         	cartonType = getMapStr(m,"cartonType");
	         	break;
	         }
	      }
	      if( !cartonType.isEmpty() ) {
	      	cartonSeq = db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn=trackingNumber "
	      			+ "AND pickType='Geek' AND cartonType='%s' AND pickShortStamp IS NOT NULL AND cancelStamp IS NULL LIMIT 1", cartonType);
	      	if( cartonSeq > 0 ) {
	      		db.execute("UPDATE rdsCartons SET lpn='%s' WHERE cartonSeq=%d", barcode, cartonSeq);
	      		trace("assign lpn %s for Geek Short cartonSeq %d", barcode, cartonSeq); 
	      		SloaneCommonDAO.postCartonLog(""+cartonSeq, getStationName(), "assigned lpn %s", barcode);
	      	}
	      }
   	}
      return cartonSeq;
   }
   
   protected void setCarton( int cartonSeq ) {
   	Map<String,String> map = db.getRecordMap(
   			"SELECT * FROM rdsCartons WHERE cartonSeq=%d",cartonSeq);
   	String cartonLpn = getMapStr(map,"lpn");
   	String orderId = getMapStr(map,"orderId");
   	String pickType = getMapStr(map,"pickType");
   	String cartonType = getMapStr(map,"cartonType");  	
   	String trackingNumber = getMapStr(map,"trackingNumber");
      int repackRequired = getMapInt(map, "repackRequired");
      int auditRequired = getMapInt(map, "auditRequired");
      
      int numOpenPicks = db.getInt(0, "SELECT COUNT(*) FROM rdsPicks WHERE cartonSeq=%d AND picked=0 AND shortPicked=0 AND canceled=0",cartonSeq);
      
   	boolean canceled = !getMapStr(map,"cancelStamp").isEmpty();
   	boolean picked = !getMapStr(map,"pickStamp").isEmpty();
   	boolean packed = !getMapStr(map,"packStamp").isEmpty();
   	boolean shortPicked = !getMapStr(map,"pickShortStamp").isEmpty();
   	boolean requireAudit = auditRequired>0 && getMapStr(map,"auditStamp").isEmpty();
   	boolean labeled = !getMapStr(map,"labelStamp").isEmpty();
   	   	
   	String cartonStatus = (canceled)? CARTON_STATUS_CANCELED :
   		                   (requireAudit)? CARTON_STATUS_REQUIREAUDIT:
   								 (labeled)? CARTON_STATUS_LABELED :
   								 (repackRequired>0)?CARTON_STATUS_REPACK:
									 (packed)? CARTON_STATUS_PACKED :
									 (numOpenPicks>0)? CARTON_STATUS_PICKING :
									 (picked)?CARTON_STATUS_PICKED:
									 (shortPicked)? CARTON_STATUS_SHORT:CARTON_STATUS_PICKING;
   	
   	if( cartonStatus.equals(CARTON_STATUS_PICKED) ) {
   		requireAudit = SloaneCommonDAO.cartonRequireAudit(cartonSeq);
   		if( requireAudit )
   			cartonStatus = CARTON_STATUS_REQUIREAUDIT;
   	}
   	String exceptionReason = "Unkonwn";
   	switch(cartonStatus) {
   	case CARTON_STATUS_REQUIREAUDIT:
   		exceptionReason = "Random Audit Required.";
   		break;
   	case CARTON_STATUS_REPACK:
   		exceptionReason = "Repack Required.";
   		break;
   	case CARTON_STATUS_PICKING:
   		exceptionReason = "Has open picks, audit required.";
   		break;
   	case CARTON_STATUS_PICKED:
   	case CARTON_STATUS_PACKED:
   		exceptionReason = getCartonExceptionReason(cartonSeq);
   		break;
   	}
   	

	   inform("Carton status: %s",cartonStatus);
   	setParam(TermParam.cartonSeq,""+cartonSeq);
   	setParam(TermParam.orderId,orderId);
   	setParam(TermParam.cartonLpn,cartonLpn);
   	setParam(TermParam.pickType,pickType);
   	setParam(TermParam.cartonType,cartonType);
   	setParam(TermParam.cartonStatus,cartonStatus);
   	setParam(TermParam.trackingNumber,trackingNumber);
   	setParam(TermParam.exceptionReason,exceptionReason);
   	setParam(TermParam.repackRequired,""+repackRequired);
   	if( cartonLpn.startsWith("TT") )
   		setParam(TermParam.lpnPrefix,"TT");
   	else if( cartonLpn.startsWith("C4") )
   		setParam(TermParam.lpnPrefix,"C4");
   	else
   		setParam(TermParam.lpnPrefix,"C1,C2,C3");
   } 
   
   protected String getNextScreen( String cartonStatus ) {
   	switch(cartonStatus) {
   	case CARTON_STATUS_REQUIREAUDIT:
   	case CARTON_STATUS_PICKING:
   		return "exception.AuditScreen";
   	case CARTON_STATUS_LABELED:
   		return "exception.LabelActionScreen";
   	case CARTON_STATUS_REPACK:
   		return "exception.RepackIdleScreen";
   	case CARTON_STATUS_PACKED:
   	case CARTON_STATUS_PICKED:
   		return "exception.LabelIdleScreen";
   	case CARTON_STATUS_SHORT:
   		return "exception.ShortDetailsScreen";
   	default:
   		return "exception.IdleScreen";
   	}
   }
   
   protected void setCartonAtHoldArea(int cartonSeq) {
   	String holdArea = getStrParam("stationName")+" Hold Area";
   	db.execute("UPDATE rdsCartons SET lastPositionLogical='%s' WHERE cartonSeq=%d", holdArea, cartonSeq);
   }
   
   protected List<Map<String,String>> getCartonLabels(){
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<Map<String,String>> list = db.getResultMapList(
   			"SELECT 'Packlist' AS docType, IF(MAX(docSeq) IS NULL, 'Not ready', 'Ready') AS `status`, MAX(docSeq) as docSeq "
   			+ "FROM rdsDocuments WHERE docType='packlist' AND refType='cartonSeq' AND refValue='%d' "
   			+ "UNION "
   			+ "SELECT 'Order Label' AS docType, IF(MAX(docSeq) IS NULL, 'Not ready', 'Ready') AS `status`, MAX(docSeq) as docSeq "
   			+ "FROM rdsDocuments WHERE docType='label' AND refType='cartonSeq' AND refValue='%d' "
   			+ "UNION "
   			+ "SELECT 'Ship Label' AS docType, IF(MAX(docSeq) IS NULL, 'Not ready', 'Ready') AS `status`, MAX(docSeq) as docSeq  "
   			+ "FROM rdsDocuments WHERE docType='shipLabel' AND refType='cartonSeq' AND refValue='%s'", cartonSeq, cartonSeq, cartonSeq); 	
   	for( Map<String,String> m : list ) {
   		String status = getMapStr(m,"status");
   		String docType = getMapStr(m,"docType");
   		switch(docType) {
   		case "Packlist":
   			m.put("printer", getParam("printer"));
   			break;
   		case "Order Label":
   			m.put("printer", getParam("labeler"));
   			break;
   		case "Ship Label":
   			m.put("printer", getParam("shippingLabeler"));
   			break;   		
   		}
   		if( status.equals("Ready") )
   			m.put("background", "green");
   		else
   			m.put("background", "red");
   	}
   	return list;
   }
   
   protected void markCartonPacked() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	//db.execute("UPDATE rdsCartons SET labelStamp=IFNULL(labelStamp,NOW()) WHERE cartonSeq=%d", cartonSeq);
   	SloaneCommonDAO.setTableTombStoneByIntId("rdsCartons", "packStamp", "cartonSeq", cartonSeq);	
   }
   
   protected void markCartonLabeled() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	//db.execute("UPDATE rdsCartons SET labelStamp=IFNULL(labelStamp,NOW()) WHERE cartonSeq=%d", cartonSeq);
   	SloaneCommonDAO.confirmCartonLabel(cartonSeq, getOperatorId());
   	trace("cartonSeq %d complete label process at %s", cartonSeq, getParam("stationName"));   	
   }
   
   protected String getCartonExceptionReason(int cartonSeq) {
   	String tableName = getStrParam("stationName").contains("West")?"westCartons":"eastCartons";
   	int xpalSeq = db.getInt(-1, "SELECT seq FROM %s WHERE cartonSeq=%d ORDER BY seq DESC LIMIT 1", tableName, cartonSeq);
   	if( xpalSeq < 0 ) return "Unknown reason";
   	String failedName = db.getString("", "SELECT `name` FROM cartonStatus WHERE seq=%d AND `status` IN ('pending','failed') ORDER BY ordinal LIMIT 1", xpalSeq);
   	if( failedName.isEmpty() ) return "Unknown reason";
   	switch(failedName) {
   	case "ready":
   		return "Audit,repack or exception process required";
   	case "label":
   		return "No label data found at PNA";
   	case "westPrn1":
   	case "westPrn2":
   	case "eastPrn1":
   	case "eastPrn2":
   		return "Printer failed to apply label";
   	case "validate":
   		return "LPN validation failure";
   	case "verify":
   		return "Label verification failure";
		default:
			return "Unknown reason";
   	}
   }
   
   protected boolean showRepack() {
   	String cartonStatus = getParam(TermParam.cartonStatus);
   	if( cartonStatus.equals(CARTON_STATUS_LABELED))
   		return false;
   	return true;
   }
   
   protected void doClearRepackFlag(int cartonSeq) {
   	db.execute("UPDATE rdsCartons SET repackRequired=0 WHERE cartonSeq=%d", cartonSeq);
   }

   protected void auditCarton() {
	db.execute("UPDATE rdsCartons SET auditRequired = 1 WHERE cartonSeq=%d", getIntParam(TermParam.cartonSeq));
   }
   
   protected void markCartonAudited() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	trace("cartonSeq %d audited at work station", cartonSeq); 
   	db.execute("UPDATE rdsCartons SET auditRequired=0, auditStamp=NOW() WHERE cartonSeq=%d", cartonSeq);
   	int repackRequired = getIntParam(TermParam.repackRequired);
   	if( repackRequired == 1 )
   		setParam(TermParam.cartonStatus, CARTON_STATUS_REPACK);
   	else
   		setParam(TermParam.cartonStatus, CARTON_STATUS_PICKED);
   }
   
   protected boolean isValidLpnFormat(String lpn, String cartonType) {
      boolean isValidLpn = false;
      String regex = db.getString("", "SELECT lpnFormat FROM cfgCartonTypes WHERE cartonType = '%s'", cartonType);
      
      if (lpn.matches(regex)) 
         isValidLpn = true;

      return isValidLpn;
   }

   protected String isValidLpn(String barcode) {
      String cartonType = getParam(TermParam.cartonType);
      String typeClass = db.getString("","SELECT typeClass FROM cfgCartonTypes WHERE cartonType='%s'",cartonType);
      //inform("carton type %s", cartonType);
      String typeClassSql = "";
      if( !cartonType.equalsIgnoreCase("TOTE") )
      	typeClassSql = String.format("WHERE typeClass='%s'", typeClass);
      List<Map<String,String>> potentialNewCartonTypes = db.getResultMapList(
      		"SELECT cartonType,lpnFormat FROM cfgCartonTypes %s", typeClassSql);
      String newCartonType = "";
      for( Map<String,String> m : potentialNewCartonTypes ) {
      	String thisCartonType = getMapStr(m,"cartonType");
      	String lpnFormat = getMapStr(m,"lpnFormat");
         if( barcode.matches(lpnFormat) ) {
         	newCartonType = thisCartonType;
         	break;
         }
      }
      if( newCartonType.isEmpty())
      	return LPN_INVALID_FORMAT;
   	if( cartonType.equalsIgnoreCase("TOTE") && newCartonType.equalsIgnoreCase("TOTE") ) {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s' "
   				+ "AND cancelStamp IS NULL "
   				+ "AND shipStamp IS NULL "
   				+ "AND ( labelStamp IS NULL OR ( labelStamp > DATE_SUB(NOW(), INTERVAL 1 DAY) )  )", barcode);
   		if( count == 0 ) {
   			SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "shipStamp", "lpn", barcode);
   		} else {
   			return LPN_EXIST;
   		}
   	} else {
   		boolean exist=db.getInt(1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn='%s'", barcode)>0;
   		if( exist ) return LPN_EXIST;
   	}
   	setParam(TermParam.newCartonType, newCartonType);
   	setParam(TermParam.newCartonLpn, barcode);
   	return newCartonType;
   }
   
   protected List<Map<String,String>> getRepackPickList() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<Map<String,String>> list = db.getResultMapList("SELECT p.sku,p.uom,p.baseUom,p.qty,i.barcode,i.description,ol.location, "
   			+ "COUNT(CASE WHEN p.picked=1 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS totalQty, 0 AS numInNew "
   			+ "FROM rdsPicks p "
   			+ "JOIN custSkus i ON p.sku=i.sku AND p.baseUom=i.uom "
   			+ "JOIN custOrderLines ol USING(orderLineSeq) "
   			+ "WHERE cartonSeq=%d AND p.canceled=0 GROUP BY sku,uom ",cartonSeq);
      for(Map<String,String> map : list ){
         String uom = getMapStr(map,"uom");
         String baseUom = getMapStr(map,"baseUom");
         int qty = (int) getMapDouble(map,"qty");
         String displayUom = uom;
         if( uom.equals("shelfPack") )
            displayUom = String.format("%s(%d %s)",uom,qty,baseUom);
         map.put("displayUom",displayUom);
         int totalQty = getMapInt(map,"totalQty");
         map.put("numInOld", ""+totalQty);
      }
      return list;
   }
   
   protected void moveAllPickstoNewCarton() {
   	SloaneCommonDAO.updateCartonLpnAndCartonType(getIntParam(TermParam.cartonSeq), getParam(TermParam.newCartonLpn), getParam(TermParam.newCartonType));
   }
   
   protected void movePicksToNewCarton(List<Map<String,String>> itemList) {
   	int oldCartonSeq = getIntParam(TermParam.cartonSeq);
   	int newCartonSeq = SloaneCommonDAO.createRepackCarton(oldCartonSeq, getParam(TermParam.newCartonLpn), getParam(TermParam.newCartonType));
   	for( Map<String,String> m : itemList ) {
   		int numInNew = getMapInt(m,"numInNew");
   		String sku = getMapStr(m,"sku");
   		String uom = getMapStr(m,"uom");
   		if( numInNew > 0 ) {
   			List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   					+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s' AND picked=1 AND shortPicked=0 AND canceled=0 "
   					+ "LIMIT %d", oldCartonSeq,sku,uom,numInNew);
   			for( String pickSeq_str : picks ) {
   				int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
   				db.execute("UPDATE rdsPicks SET cartonSeq=%d WHERE pickSeq=%d", newCartonSeq, pickSeq);
   			}
   		}
   	}   	
   	db.execute("UPDATE rdsCartons SET repackRequired=0 WHERE cartonSeq=%d", oldCartonSeq);
      SloaneCommonDAO.updateRepackCartonCountAndTracking(newCartonSeq);
      SloaneCommonDAO.regenerateLabelForOldCarton(oldCartonSeq);
   }
   
   // ShortDetailsScreen methods
   
   protected List<Map<String,String>> getShortPickList() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<Map<String,String>> list = db.getResultMapList("SELECT p.sku,p.uom,p.baseUom,p.qty,i.barcode,i.description,ol.location, "
   			+ "COUNT(CASE WHEN p.canceled=0 AND (p.picked=0 OR p.shortPicked=0) THEN pickSeq END) AS totalQty, "
   			+ "COUNT(CASE WHEN p.picked=1 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS pickedQty, 0 AS scannedQty, "
   			+ "COUNT(CASE WHEN p.picked=0 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS openQty, "
   			+ "COUNT(CASE WHEN p.picked=0 AND p.shortPicked=1 AND p.canceled=0 THEN pickSeq END) AS shortQty "
   			+ "FROM rdsPicks p "
   			+ "JOIN custSkus i ON p.sku=i.sku AND p.baseUom=i.uom "
   			+ "JOIN custOrderLines ol USING(orderLineSeq) "
   			+ "WHERE cartonSeq=%d AND p.picked=0 AND p.canceled=0 GROUP BY sku,uom ",cartonSeq);
      for(Map<String,String> map : list ){
         String uom = getMapStr(map,"uom");
         String baseUom = getMapStr(map,"baseUom");
         int qty = (int) getMapDouble(map,"qty");
         String displayUom = uom;
         if( uom.equals("shelfPack") )
            displayUom = String.format("%s(%d %s)",uom,qty,baseUom);
         map.put("displayUom",displayUom);
      }
      return list;
   }
   
    
   // AuditScreen methods
   
   protected List<Map<String,String>> getAuditPickList() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	List<Map<String,String>> list = db.getResultMapList("SELECT p.sku,p.uom,p.baseUom,p.qty,i.barcode,i.description,ol.location, "
   			+ "COUNT(CASE WHEN p.canceled=0 AND (p.picked=0 OR p.shortPicked=0) THEN pickSeq END) AS totalQty, "
   			+ "COUNT(CASE WHEN p.picked=1 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS pickedQty, 0 AS scannedQty, "
   			+ "COUNT(CASE WHEN p.picked=0 AND p.shortPicked=0 AND p.canceled=0 THEN pickSeq END) AS openQty, "
   			+ "COUNT(CASE WHEN p.picked=0 AND p.shortPicked=1 AND p.canceled=0 THEN pickSeq END) AS shortQty "
   			+ "FROM rdsPicks p "
   			+ "JOIN custSkus i ON p.sku=i.sku AND p.baseUom=i.uom "
   			+ "JOIN custOrderLines ol USING(orderLineSeq) "
   			+ "WHERE cartonSeq=%d AND p.canceled=0 GROUP BY sku,uom ",cartonSeq);
      for(Map<String,String> map : list ){
         String uom = getMapStr(map,"uom");
         String baseUom = getMapStr(map,"baseUom");
         int qty = (int) getMapDouble(map,"qty");
         String displayUom = uom;
         if( uom.equals("shelfPack") )
            displayUom = String.format("%s(%d %s)",uom,qty,baseUom);
         map.put("displayUom",displayUom);
      }
      return list;
   }
   
   protected Map<String,String> getSkuFromScan( String scan ) {
   	return SloaneCommonDAO.getSkuFromUPC(scan);
   }
   
   protected boolean isValidPassword(String operatorID) {
   	return db.getInt(0, "SELECT auditAllowed FROM proOperators WHERE operatorID='%s'", operatorID)==1;
   }
   
   protected void markOpenPickPicked() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	String op = getOperatorId();
   	List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   			+ "WHERE cartonSeq=%d "
   			+ "AND picked=0 AND canceled=0", 
   			cartonSeq);
   	for( String pickSeq_str : picks ) {
   		int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
   		if( pickSeq>0 ) {
   			SloaneCommonDAO.confirmPick(pickSeq, op);
   		}
   	}
   }
   
   protected void markOpenPickPickShort(List<Map<String,String>> itemList) {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	String op = getOperatorId();
   	boolean hasShort = false;
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
   				SloaneCommonDAO.confirmPick(pickSeq, op);
   			}
   		}
   		if( scannedQty < totalQty ) {
   			hasShort = true;
   			int n = totalQty - scannedQty;
   			List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
   					+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s' AND canceled=0 "
   					+ "ORDER BY picked, shortPicked LIMIT %d", cartonSeq,sku,uom,n);
   			for( String pickSeq_str : picks ) {
   				int pickSeq = RDSUtil.stringToInt(pickSeq_str, -1);
   				SloaneCommonDAO.confirmShort(pickSeq, op);
   			}
   		}
   	}
   	if( hasShort )
   		SloaneCommonDAO.clearTableTombStoneByIntId("rdsCartons", "pickStamp", "cartonSeq", cartonSeq);
   }   
   
   protected int printLabel(int docSeq, String printer) {
      int result = db.execute("INSERT INTO docQueue SET device='%s', docSeq=%d", printer, docSeq);
      if( result <= 0 ) return PRINT_ERROR_DATABASE;
      return db.getSequence();
   }
   
   protected int getPrintJobStatus( int seq ) {
      return db.getInt(0, "SELECT complete FROM docQueue WHERE queueSeq=%d", seq);
   }

   /*
    * Gets the number of open picks remaining and if zero, returns true
    */
   protected boolean isContainerComplete(int cartonSeq) {
      return(db.getInt(-1, 
         "SELECT COUNT(*) FROM rdsPicks " +
         "WHERE cartonSeq=%d AND picked=0 " +
         "AND canceled=0",cartonSeq
         ) == 0
      );
   }
   
   protected void unreserveMakeupShorts() {
      db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='' "
      		+ "WHERE cartonSeq=%s AND sku='%s' AND uom='%s'", getParam(TermParam.cartonSeq), getParam(TermParam.sku), getParam(TermParam.uom));
    }   

   // helpers

   public void handleTick() {
      super.handleTick();
      cycleCheck();
      if( start > 0 && System.currentTimeMillis() - start > reset ) {
      	doOkay();
      }
   }

   protected void cycleCheck() {
      if(cycleMax > 0 && cycleCount) {
         if(cycle++ >= cycleMax) {
            hideResultMsgModule();
         }
      } else
         cycle = 0;
   }
   
   protected void initCycleCount(boolean enabled) {
   	cycleCount = enabled;
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
   	setNextScreen("exception.IdleScreen");
   }

    protected String getScan() {
      String scan = db.getString("",
            "SELECT value FROM runtime WHERE name='%s'", getParam("scanner")
      );
      //if no scan was recorded, then we're done.
      if ((scan == null) || (scan.isEmpty()))  return null;

      //trace the scan
      trace("got scan [%s]", scan);
      //blank the runtime entry so we don't re-use the same scan
      db.execute("UPDATE runtime SET value='' WHERE name='%s'", getParam("scanner"));
      return scan;
   }
}