package dao;

import static sloane.SloaneConstants.*;
import static rds.RDSLog.inform;
import static rds.RDSUtil.*;

import zplTemplates.ZplTemplates;
import org.json.JSONObject;

import rds.RDSCounter;
import rds.RDSUtil;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SloaneCommonDAO extends AbstractDAO{

	public static boolean isValidLocation( String location ) {
		return stringIdExistInTable("rdsLocations","location",location);
	}
	
	public static boolean isValidSkuUomDimension( String sku, String uom ) {
		Map<String,String> map = getTableRowByPairId("custSkus","sku",sku,"uom",uom);
		double l = getMapDouble(map,"length");
		double w = getMapDouble(map,"width");
		double h = getMapDouble(map,"height");
		int cubicDivisorint = getMapInt(map,"cubicDivisorint");
		int qtyShelfPack = getMapInt(map,"qtyShelfPack");
		double v = l*w*h/cubicDivisorint;
		double shelfPack_v = v*qtyShelfPack;
		Map<String,String> toteSizeMap = getTableRowByStringId("cfgCartonTypes","cartonType","tote");
		double tote_l = getMapDouble(toteSizeMap,"interiorLength");
		double tote_w = getMapDouble(toteSizeMap,"interiorWidth");
		double tote_h = getMapDouble(toteSizeMap,"interiorHeight");
		double fillFactor = getMapDouble(toteSizeMap,"fillFactor");
		double tote_v = tote_l*tote_w*tote_h*fillFactor;
		return qtyShelfPack>0 && shelfPack_v<=tote_v;
	}
	
   public static boolean isValidShippingInfo( int shipInfoSeq ) {
   	Map<String,String> shipInfoMap = getTableRowByIntId( "custShippingInfo","shipInfoSeq",shipInfoSeq );
   	if( shipInfoMap == null || shipInfoMap.isEmpty() )
   		return false;
		String name = getMapStr( shipInfoMap,"name" );
		String company = getMapStr( shipInfoMap,"company" );
		String address1 = getMapStr( shipInfoMap,"address1");
		String address2 = getMapStr( shipInfoMap,"address2");
		String address3 = getMapStr( shipInfoMap,"address3");
		String city = getMapStr( shipInfoMap,"city");
		String state = getMapStr( shipInfoMap,"state");
		String postalCode = getMapStr( shipInfoMap,"postalCode");
		String country = getMapStr( shipInfoMap,"country");
		//String phone = getMapStr( shipInfoMap,"phone");
		//String email = getMapStr( shipInfoMap,"email");
		boolean international = !country.toUpperCase().startsWith("US");
		if( name.isEmpty() && company.isEmpty() ) return false;
		if( address1.isEmpty() && address2.isEmpty() && address3.isEmpty()) return false;
		if( city.isEmpty() ) return false;
		if( state.isEmpty() && !international ) return false;
		if( postalCode.isEmpty() && !international ) return false;
		if( country.isEmpty() ) return false;
		return true;
   }
   
   public static boolean isValidBillingInfo( int billingInfoSeq ) {
   	return true;
   }
	
   public static boolean isMarkedOutSku(String sku) {
   	return db.getInt(0, "SELECT isActive FROM rdsMarkOutSkus WHERE sku='%s'", sku)==1;
   }
   
   public static boolean createShelfPackPick(String pickType, String qroFlag) {
   	//return (qroFlag.equals("X") ) && !pickType.equals(PICKTYPE_GEEK);
   	return (qroFlag.equals("X") );
   }   
   
   public static String determinePickType( String location ) {
   	if( location.isEmpty() ) return ERROR;
   	String department = db.getString("", "SELECT area FROM rdsLocations WHERE location='%s'", location);
   	if( !department.isEmpty() ) {
	   	String rdsPickZone = db.getString("", "SELECT rdsPickZone FROM cfgDepartments WHERE department='%s'", department);
	   	return rdsPickZone.isEmpty()? ERROR: rdsPickZone;
   	} else {
   		department = location.substring(0,1);
   		String rdsPickZone = db.getString("", "SELECT rdsPickZone FROM cfgDepartments WHERE department='%s'", department);
   		if( rdsPickZone.equals(PICKTYPE_GEEK) ) return rdsPickZone;
   		return ERROR;
   	}
   }    
	
   public static void createOrderPick(int orderLineSeq,String orderId,String sku,String baseUom,String uom,int qty,String pickType,boolean isMarkedOutSku ) {
   	if( !isMarkedOutSku )
			db.executePreparedStatement(
					"INSERT INTO rdsPicks SET "
				  +"orderLineSeq=?,"
				  +"orderId=?,"
				  +"pickType=?,"
				  +"sku=?,"
				  +"baseUom=?,"
				  +"uom=?,"
				  +"qty=?",
				  ""+orderLineSeq,orderId,pickType,sku,baseUom,uom,""+qty);   	
   	else
			db.executePreparedStatement(
					"INSERT INTO rdsPicks SET "
				  +"orderLineSeq=?,"
				  +"orderId=?,"
				  +"pickType=?,"
				  +"sku=?,"
				  +"baseUom=?,"
				  +"uom=?,"
				  +"qty=?,"
				  +"picked=1,shortPicked=1,pickStamp=NOW(),shortStamp=NOW()",
				  ""+orderLineSeq,orderId,pickType,sku,baseUom,uom,""+qty);   	
   }
   
   public static void createPickFromShelfPack(int orderLineSeq,int cartonSeq, String orderId,String sku,String baseUom,String uom,
   		int qty,String pickType, int readyForPick, int lineNumber ) {
		db.executePreparedStatement(
				"INSERT INTO rdsPicks SET "
			  +"orderLineSeq=?,"
			  +"orderId=?,"
			  +"cartonSeq=?,"
			  +"pickType=?,"
			  +"sku=?,"
			  +"baseUom=?,"
			  +"uom=?,"
			  +"qty=?,"
			  +"readyForPick=?,"
			  +"lineNumber=?",
			  ""+orderLineSeq,orderId,""+cartonSeq,pickType,sku,baseUom,uom,""+qty,""+readyForPick,""+lineNumber);   	
   }     
   
   public static void releaseOrderPicksByPickType( String orderId, String pickType ) {
		List<String> cartons = db.getValueList(
				"SELECT cartonSeq FROM rdsCartons WHERE orderId='%s' AND pickType='%s' "
				+ "AND cancelStamp IS NULL ", orderId, pickType);
   	if( pickType.equals(PICKTYPE_GEEK) ) {
   		for( String s : cartons ) {
   			int cartonSeq = RDSUtil.stringToInt(s, -1);
   			if( cartonSeq>0 )
   	      	db.execute("UPDATE rdsCartons "
   	      			+ "SET releaseStamp=NOW(), geekStatus='readyToSend' WHERE cartonSeq=%d ",cartonSeq);
   		}
   	} else {
   		for( String s : cartons ) {
   			int cartonSeq = RDSUtil.stringToInt(s, -1);
   			if( cartonSeq>0 )
   				setTableTombStoneByIntId("rdsCartons","releaseStamp","cartonSeq",cartonSeq);
   		}
   	}
   	
   	db.execute("UPDATE rdsPicks  "
   			+ "SET readyForPick=1 WHERE orderId='%s' AND pickType='%s' "
   			+ "AND canceled=0 ", orderId, pickType); 
   }
   
   public static void confirmPick( int pickSeq, String operatorId ) {
      db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),shortPicked=0,shortStamp=NULL WHERE pickSeq=%d", pickSeq);
      JSONObject json = new JSONObject() ;
   	json.put("pickSeq",pickSeq) ;
   	insertStatusMessages("statusApp","pick",json.toString(),operatorId);
   }
   
   public static void confirmShort( int pickSeq, String operatorId ) {
      db.execute("UPDATE rdsPicks SET picked=0, shortPicked=1, chasePicked=1, pickStamp=NULL,shortStamp=NOW() WHERE pickSeq=%d", pickSeq);
      JSONObject json = new JSONObject() ;
   	json.put("pickSeq",pickSeq) ;
   	insertStatusMessages("statusApp","pickShort",json.toString(),operatorId);
   }
   
   public static void confirmCartonLabel( int cartonSeq, String operatorId ) {
      JSONObject json = new JSONObject() ;
   	json.put("cartonSeq",cartonSeq) ;
   	insertStatusMessages("statusApp","cartonLabel",json.toString(),operatorId);
   }
   
   public static void updateCartonLpnAndCartonType( int cartonSeq, String lpn, String cartonType ) {
   	db.execute("UPDATE rdsCartons SET lpn='%s', cartonType='%s', repackRequired=0 WHERE cartonSeq=%d", lpn, cartonType, cartonSeq);
   }
   
   public static int createRepackCarton( int oldCartonSeq, String lpn, String cartonType ) {
   	int cartonSeq = -1;
   	Map<String,String> m = RdsCartonDAO.getRecordMap(oldCartonSeq);
   	String orderId = getMapStr(m,"orderId");
   	String pickType = getMapStr(m,"pickType");
   	String truckNumber = getMapStr(m,"truckNumber");
   	db.executePreparedStatement("INSERT INTO rdsCartons SET " +
            "orderId = ?, " +
   			"lpn = ?, " +
            "pickType = ?, " +
            "cartonType = ?, " +
            "truckNumber = ?, " +
            "isRepack = 1, " +
            "createStamp = NOW(), releaseStamp = NOW(), " +
            "estContentsUploadStamp = NOW(), geekPickConfirmedStamp = NOW(), " +
            "geekStatus='confirmed'",
            orderId,
            lpn,
            pickType,
            cartonType,
            truckNumber
            );
   	cartonSeq = db.getSequence();
   	RDSCounter.increment(String.format("/cartonization/%s/%s",pickType,cartonType));
   	postCartonLog(""+cartonSeq, "repack", "carton created");
   	return cartonSeq;
   }
   
   public static void updateRepackCartonCountAndTracking( int cartonSeq ) {
   	Map<String,String> map = db.getRecordMap("SELECT ol.orderId, ol.pageId, ol.lineId FROM custOrderLines ol "
				+ "JOIN rdsPicks USING(orderLineSeq) WHERE cartonSeq=%d ORDER BY ol.orderLineSeq LIMIT 1", cartonSeq);
		String orderId = getMapStr(map,"orderId");
		String pageId = getMapStr(map,"pageId");
		String lineId = getMapStr(map,"lineId");
		int cartonCount = db.getInt(1, "SELECT MAX(cartonCount) FROM rdsCartons WHERE orderId='%s'", orderId)+1;
		String trackingNumber = String.format("%s%s%s%04d", orderId,pageId.substring(pageId.length()-3),lineId.substring(lineId.length()-4),cartonCount);
		db.execute("UPDATE rdsCartons SET trackingNumber='%s', cartonCount=%d, pickStamp=NOW(),pickShortStamp=NULL WHERE cartonSeq=%d", trackingNumber, cartonCount, cartonSeq );
      JSONObject json = new JSONObject() ;
   	json.put("cartonSeq",cartonSeq) ;
   	json.put("orderId",orderId) ;
   	insertStatusMessages("statusApp","cartonPick",json.toString(),"");
   }
   
   public static void regenerateLabelForOldCarton( int cartonSeq ) {
   	triggerLabelCreation(cartonSeq);
   }
   
   public static void breakShelfPack( int pickSeq, int readyForPick ) {
   	Map<String,String> pickMap = getTableRowByIntId( "rdsPicks","pickSeq",pickSeq );
   	int orderLineSeq = getMapInt(pickMap,"orderLineSeq");
   	int cartonSeq = getMapInt(pickMap,"cartonSeq");
   	int lineNumber = getMapInt(pickMap,"lineNumber");
   	String orderId = getMapStr(pickMap,"orderId");
   	String sku = getMapStr(pickMap,"sku");
   	String baseUom = getMapStr(pickMap,"baseUom");
   	int qty = (int) getMapDouble(pickMap,"qty");
   	String pickType = getMapStr(pickMap,"pickType");
   	for( int i=0;i<qty; i++ )
   		createPickFromShelfPack(orderLineSeq,cartonSeq,orderId,sku,baseUom,baseUom,1,pickType,readyForPick,lineNumber);
   	deleteTableRecordByIntId("rdsPicks", "pickSeq", pickSeq);
   }
   
   public static void triggerLabelCreation( int cartonSeq ) {
      JSONObject json = new JSONObject() ;
   	json.put("cartonSeq",cartonSeq) ;
   	insertStatusMessages("pclpack","packlist",json.toString(),"rds");
   	String orderType = db.getString("", 
   			"SELECT orderType FROM custOrders JOIN rdsCartons USING(orderId) WHERE cartonSeq=%d", cartonSeq);
   	ZplTemplates zpl = new ZplTemplates(db);
   	zpl.requestOrderLabel(cartonSeq);
   	if( orderType.equals(ORDERTYPE_ECOM) )
   		zpl.requestECommerceShippingLabel(cartonSeq);
   	else
   		zpl.requestShippingLabel(cartonSeq);
   }
   
   public static Map<String,String> getSkuFromUPC( String barcode ){
   	int barcodeLength = barcode.length();
   	Map<String,String> map = null;
   	switch(barcodeLength) {
   	case 8:
   		map = db.getRecordMap("SELECT * FROM custSkus WHERE gtin8='%s' LIMIT 1", barcode);
   		break;
   	case 13:
   		map = db.getRecordMap("SELECT * FROM custSkus WHERE gtin13='%s' LIMIT 1", barcode);
   		break;
   	case 14:
   		map = db.getRecordMap("SELECT * FROM custSkus WHERE ( barcode='%s' OR upc2='%s' OR upc3='%s' ) LIMIT 1", barcode, barcode, barcode);
   		if( map == null || map.isEmpty() ) {
   			String barcode12 = barcode.substring(1,13);
   			map = db.getRecordMap("SELECT * FROM custSkus WHERE ( LOCATE('%s',barcode)>0 OR LOCATE('%s',upc2)>0 OR LOCATE('%s',upc3)>0 ) LIMIT 1", barcode12, barcode12, barcode12);
   		}
   		break;
   	case 12:
   		map = db.getRecordMap("SELECT * FROM custSkus WHERE ( barcode='%s' OR upc2='%s' OR upc3='%s' ) LIMIT 1", barcode, barcode, barcode);
   		if( map == null || map.isEmpty() ) {
   			map = db.getRecordMap("SELECT * FROM custSkus WHERE ( LOCATE('%s',barcode)>0 OR LOCATE('%s',upc2)>0 OR LOCATE('%s',upc3)>0 ) LIMIT 1", barcode, barcode, barcode);
   		}
   		break;
   	default:
   		map = null;
   	}
   	if( map == null || map.isEmpty() )
   		map = db.getRecordMap("SELECT * FROM custSkus WHERE sku='%s' LIMIT 1", barcode);
   	return map;
   }

   public static int getFullcaseLineFromScan( String tracking, String upc ){
   	int upcLength = upc.length();
   	String trackingPrefix = tracking.substring(0,14);
   	Map<String,String> map = null;
   	map = db.getRecordMap("SELECT * FROM custFullcaseLines WHERE trackingPrefix='%s'", trackingPrefix);
   	if( map==null || map.isEmpty() ) return -1;
   	int fullcaseLineSeq = getMapInt( map,"fullcaseLineSeq" );
   	int verifyUPC = getMapInt(map,"verifyUPC");
   	boolean upcVerified = false;
   	switch(upcLength) {
   	case 0:
   		if( verifyUPC == 0 ) return fullcaseLineSeq;
   		else return 0;
   	case 8:
   		upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=2 AND upc='%s'", fullcaseLineSeq,upc)>0;
   		return upcVerified?fullcaseLineSeq:0;
   	case 13:
   		upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=3 AND upc='%s'", fullcaseLineSeq,upc)>0;
   		return upcVerified?fullcaseLineSeq:0;
   	case 12:
   		upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=1 AND upc='%s'", fullcaseLineSeq,upc)>0;
   		if(!upcVerified) {
   			upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=1 AND LOCATE('%s',upc)>0", fullcaseLineSeq,upc)>0;
   		}
   		return upcVerified?fullcaseLineSeq:0;
   	case 14:
   		upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=1 AND upc='%s'", fullcaseLineSeq,upc)>0;
   		if(!upcVerified) {
   			String upc12 = upc.substring(1,13);
   			upcVerified = db.getInt(0, "SELECT COUNT(*) FROM custFullcaseLineData WHERE fullcaseLineSeq=%d AND barcodeType=1 AND LOCATE('%s',upc)>0", fullcaseLineSeq,upc12)>0;
   		}
   		return upcVerified?fullcaseLineSeq:0;
		default:
   		return verifyUPC==1?0:fullcaseLineSeq;
   	}
   }  
   
   public static void checkFileCartonized( int fileSeq ) {
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
		if( total == (cartonized+canceled) && cartonized>0 ) {
			inform("File seq %d is cartonized", fileSeq);
			CustOutboundOrderFileDAO.setTombstone(fileSeq, "cartonizeStamp");	
		} 
   }   
   
   public static boolean cartonRequireAudit(int cartonSeq) {
   	long seed = System.currentTimeMillis();
   	Random rand = new Random(seed);
   	int random = rand.nextInt(100) + 1;
   	int opQcLevel = db.getInt(50, 
   			"SELECT CAST( MAX(randomQc)*100 AS INT) FROM proOperators op " +
				"JOIN rdsPicks p ON p.pickOperatorId = op.operatorID " +
				"WHERE cartonSeq=%d", cartonSeq );
   	int skuQcLevel = db.getInt(50, 
   			"SELECT CAST( MAX(randomQc)*100 AS INT) FROM custSkus s " +
				"JOIN rdsPicks p ON s.sku=p.sku AND s.uom=p.baseUom " +
				"WHERE cartonSeq=%d", cartonSeq );
   	int customerQcLevel = db.getInt(50, 
   			"SELECT CAST( randomQc*100 AS INT) FROM custCustomers " +
   			"JOIN custOrders USING(customerNumber) " +
   			"JOIN rdsCartons USING(orderId) " +
   			"WHERE cartonSeq=%d", cartonSeq );
   	int qcLevel = Math.max(Math.max(opQcLevel, skuQcLevel), customerQcLevel);
   	boolean auditRequired = random<=qcLevel;
   	if( auditRequired )
   		db.execute("UPDATE rdsCartons SET auditRequired=1 WHERE cartonSeq=%d", cartonSeq);
   	return auditRequired;
   }

	public static boolean createGeekPickOrder(String orderId, int designatedContainerTypeCode, String dailyWaveSeqString, int orderType, int priority){
		Map<String,String> geekMap = db.getControlMap("geekplus");
		int is_allow_lack = Integer.parseInt(geekMap.get("is_allow_lack"));
		int is_waiting = Integer.parseInt(geekMap.get("is_waiting"));
		int inf_function = Integer.parseInt(geekMap.get("needFeedbackByContainer"));
		trace("is_allow_lack: "+is_allow_lack);
		trace("is_waiting: "+is_waiting);


		int success = db.execute(
				"INSERT INTO geekPickOrder SET "
				+ " warehouse_code = '%s', "
				+ " out_order_code = '%s', "
				+ " owner_code = '%s', "
				+ " out_wave_code = '%s', "
				+ " order_type = %d, "
				+ " designated_container_type = %d, " // designatedContainerTypeCode
				+ " is_allow_split = 0, "
        		+ " priority = %d, "
				+ " is_allow_lack = %d, "
				+ " is_waiting = %d, "
				+ " inf_function = %d, "
				+ " creation_date = NOW()",
				geekWarehouseCode,
				orderId,
				geekOwnerCode,
				dailyWaveSeqString,
				orderType,
				designatedContainerTypeCode,
        		priority,
				is_allow_lack,
				is_waiting,
				inf_function
		);

	   if(success == 1){
		   return true;
	   }else{
		   return  false;
	   }

   }

	public static int createGeekPickOrderSku(int geekOrderSeq, String sku, int qty, int pickSeq){
		// geek pick order line creation
		return db.execute(
				"INSERT INTO geekPickOrderSku SET "
						+ " parent = %d, "
						+ " sku_code = '%s', "
						+ " owner_code = '%s', "
						+ " amount = %d, "
						+ " out_batch_code = '%s', "
						+ " sku_level = 0 ",
				geekOrderSeq,
				sku,
				geekOwnerCode,
				qty,
				pickSeq+""
				);

	}

	// Function to pad zeros to the left of the string
	public static String padZeros(String input, int expectedLength) {
		int currentLength = input.length();

		// Check if padding is needed
		if (currentLength < expectedLength) {
			int zerosToAdd = expectedLength - currentLength;
			StringBuilder stringBuilder = new StringBuilder();

			// Append zeros to the StringBuilder
			for (int i = 0; i < zerosToAdd; i++) {
				stringBuilder.append('0');
			}

			// Append the original string
			stringBuilder.append(input);

			// Return the padded string
			return stringBuilder.toString();
		} else if (currentLength > expectedLength){
			alert("value [%s], actual length [%d] is greater that expected length [%d]",input,currentLength,expectedLength);
			return input;
		}else {
			// If the length is already equal, return the original string
			return input;
		}
	}



	public static void insertUserInWebTables(String username, String realname, String password){


		db.execute(" INSERT INTO _user (user,name) " +
				" VALUES ('%s','%s') " +
				" ON DUPLICATE KEY UPDATE " +
				" name = '%s' ;", username, realname, realname);

		db.execute(" UPDATE _user " +
				" SET password=PASSWORD('%s') " +
				" WHERE user='%s' ;", password, username);

		db.execute(" REPLACE INTO _userRole (user,role) " +
				" VALUES ('%s','%s') ;", username, "picker");
	}

	public static void createCommsError(String commType, String desc, int priority){
		db.execute("INSERT INTO commErrors SET commType='%s', description='%s', priority=%d", commType, desc, priority);
	}

	public static void createNotification(String recipient, String message){
		int hasRecentlyBeenNotified = db.getInt(-1,"SELECT COUNT(*) "
				+ " FROM notifications "
				+ " WHERE stamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR) "
				+ " AND recipient = '%s' "
				+ " AND message = '%s' ",
				recipient,message);

		if(hasRecentlyBeenNotified < 1) {
			inform("creating notification for [%s], message [%s]...",recipient,message);
			db.execute("INSERT INTO notifications SET recipient = '%s', message = '%s';", recipient, message);
		}
	}

	public static void createNotificationWithAttachment(String recipient, String message, String attachment){
		int hasRecentlyBeenNotified = db.getInt(-1,"SELECT COUNT(*) "
				+ " FROM notifications "
				+ " WHERE stamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR) "
				+ " AND recipient = '%s' "
				+ " AND message = '%s' ",
				recipient,message);

		if(hasRecentlyBeenNotified < 1){
			inform("creating notification for [%s], message [%s]...",recipient,message);
			db.execute("INSERT INTO notifications SET recipient = '%s', message = '%s', attachment = '%s'", recipient, message, attachment);
		}

	}
	
	public static void updateSkuException(String sku, String exception, String solution) {
		db.executePreparedStatement("INSERT INTO skuExceptions SET sku=?,exception=?,solution=? "
				+ "ON DUPLICATE KEY UPDATE exception=?, solution=?, stamp=NOW(), acknowledged=0",
				sku, exception, solution, exception, solution);
	}

}
