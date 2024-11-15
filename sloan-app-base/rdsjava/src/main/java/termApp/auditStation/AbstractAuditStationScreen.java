
package termApp.auditStation;

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
public abstract class AbstractAuditStationScreen extends AbstractProjectAppScreen {

   
   // generic code
   protected static final int CODE_ERROR = -1;
   
   // carton mode
   protected static final int MODE_PARCEL_CARTON = 1;
   protected static final int MODE_LTL_CARTON = 2;
   protected static final int MODE_PARCEL_PALLET_CARTON = 3;
   protected static final int MODE_LTL_PALLET_CARTON = 4;
   protected static final int MODE_PALLET_LABEL = 5;
   
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

   public static enum TermParam {
      scan, cartonLpn, cartonSeq, cartonUcc,pickType,cartonType,cartonStatus,trackingNumber,
      shipmentType,shipmentId,orderId,
      estWeight,estLength,estHeight,estWidth,actLength,actWidth,actHeight,actWeight,dimension,
      labelStatus,sku,shipId,
      labelSeq,needLength,needWidth,needHeight,ucc,nextLabelSeq,
      palletLpn,palletSeq,palletUcc,expCount,
      processMode,docSeq,
   };
   
   /** Constructs a screen with default values. */
   public AbstractAuditStationScreen( TerminalDriver term ) {
      super( term );
      startScreen = "auditStation.StartScreen";
      setAllDatabase();
      screenSetup();
      numOfInfoRows = 1;
   }
   
   // display methods
   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      String cartonID = getStrParam(TermParam.cartonLpn);
      leftInfoBox.updateInfoPair(0, "Carton", cartonID );
      leftInfoBox.show(); 
   }
   
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.hide(); 
   }
   
   // StartScreen methods
   
   protected int lookupCartonByLpn( String barcode ) {
   	return db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn='%s'", barcode);
   }
   
   protected void setCarton( int cartonSeq ) {
   	Map<String,String> map = db.getRecordMap(
   			"SELECT * FROM rdsCartons WHERE cartonSeq=%d",cartonSeq);
   	String cartonLpn = getMapStr(map,"lpn");
   	String orderId = getMapStr(map,"orderId");
   	String pickType = getMapStr(map,"pickType");
   	String cartonType = getMapStr(map,"cartonType");
   	double estWeight = getMapDouble(map,"estWeight");
   	double estLength = getMapDouble(map,"estLength");
   	double estWidth = getMapDouble(map,"estWidth");
   	double estHeight = getMapDouble(map,"estHeight");
   	double actLength = getMapDouble(map,"actLength");
   	double actWidth = getMapDouble(map,"actWidth");
   	double actHeight = getMapDouble(map,"actHeight");   	
   	String trackingNumber = getMapStr(map,"trackingNumber");
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
   } 
   
   protected void markCartonAudited() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
		JSONObject json = new JSONObject() ;
  	  	json.put("cartonSeq",cartonSeq) ;
  	  	db.execute("INSERT status SET statusType='cartonAudit', data='%s', operator='' ,status='idle'",json.toString());
   	trace("cartonSeq %d audited at work station", cartonSeq);   	
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
   		  	  	db.execute("INSERT status SET statusType='short', data='%s', operator='' ,status='idle'",json.toString()); 
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
      return printLabel(getIntParam(TermParam.docSeq));
   }
   
   protected int printLabel(int docSeq) {
      int result = db.execute("INSERT INTO docQueue SET device='%s', docSeq=%d", getParam("printer"), docSeq);
      if( result <= 0 ) return PRINT_ERROR_DATABASE;
      return db.getSequence();
   }
   
   protected int getPrintJobStatus( int seq ) {
      return db.getInt(0, "SELECT complete FROM docQueue WHERE queueSeq=%d", seq);
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
   	setNextScreen("auditStation.StartScreen");
   }

}

