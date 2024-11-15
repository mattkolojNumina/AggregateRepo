package app;

import java.util.Map;

import org.json.JSONObject;

import rds.*;

public final class AppCommon {
	private static RDSDatabase db;
	private AppCommon() {}
	
   public static void setDatabase( RDSDatabase db ) {
   	AppCommon.db = db;
   	RDSHistory.setDatabase(db);
   	RDSEvent.setDatabase(db);
   	RDSTrak.setDatabase(db);
   }
   
   public static Map<String,String> getShipmentMap( String shipmentId ){
   	return db.getRecordMap("SELECT * FROM custShipments WHERE shipmentId='%s'", shipmentId);
   }
   
   public static boolean isValidShippingMethod( String shippingMethod ) {
   	Map<String,String> shipMethodMap = getShipMethodMap( shippingMethod );
   	if( shipMethodMap == null || shipMethodMap.isEmpty() )
   		return false;
   	return true;
   }
   
   public static String getShipType( String shipMethod ) {
   	return db.getString("", "SELECT shipType FROM cfgShipMethods WHERE shipMethod='%s'", shipMethod);
   }
   
   public static Map<String,String> getShipMethodMap(String shipMethod){
   	return db.getRecordMap("SELECT * FROM cfgShipMethods WHERE shipMethod='%s'", shipMethod);
   }
   
   public static boolean isValidShippingInfo( int shipInfoSeq ) {
   	Map<String,String> shipInfoMap = getShippingInfo( shipInfoSeq );
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
		if( name.isEmpty() && company.isEmpty() ) return false;
		if( address1.isEmpty() && address2.isEmpty() && address3.isEmpty()) return false;
		if( city.isEmpty() ) return false;
		if( state.isEmpty() ) return false;
		if( postalCode.isEmpty() ) return false;
		if( country.isEmpty() ) return false;
		return true;
   }
   
   public static Map<String,String> getShippingInfo( int shipInfoSeq ){
   	return db.getRecordMap("SELECT * FROM custShippingInfo WHERE shipInfoSeq=%d", shipInfoSeq);
   }
   
   //TODO
   public static boolean isValidBillingInfo( int billingInfoSeq ) {
   	return true;
   }
   
   public static boolean isValidLocation( String location ) {
   	return db.getInt(0, "SELECT COUNT(*) FROM rdsLocations WHERE location='%s'", location)>0;
   }
   
   public static boolean isValidSku( String sku, String uom ) {
   	return db.getInt(0, "SELECT COUNT(*) FROM custSkus WHERE sku='%s' AND uom='%s'", sku,uom)>0;
   }
   
   public static void createOrderPick(int orderLineSeq,String orderId,String sku,String uom,int qty,String pickType ) {
		db.executePreparedStatement(
				"INSERT INTO rdsPicks SET "
			  +"orderLineSeq=?,"
			  +"orderId=?,"
			  +"pickType=?,"
			  +"sku=?,"
			  +"uom=?,"
			  +"qty=?",
			  ""+orderLineSeq,orderId,pickType,sku,uom,""+qty);   	
   }
   
   public static void createOrderCartonPick(int orderLineSeq,String orderId,int cartonSeq, String sku,String uom,int qty,String pickType ) {
		db.executePreparedStatement(
				"INSERT INTO rdsPicks SET "
			  +"orderLineSeq=?,"
			  +"orderId=?,"
			  +"cartonSeq=?,"
			  +"pickType=?,"
			  +"sku=?,"
			  +"uom=?,"
			  +"qty=?",
			  ""+orderLineSeq,orderId,""+cartonSeq,pickType,sku,uom,""+qty);   	
   }
   
   public static void updateRdsInventoryReleasedQty( String sku, String uom, String location, int qty ) {
   	db.execute("UPDATE rdsInventory SET releasedQty = releasedQty + %d WHERE sku='%s' AND uom='%s' AND location='%s'", qty,sku,uom,location);
   }
   
   
   public static void setCustOrderData( String orderId, String dataType, String dataValue ) {
   	db.executePreparedStatement("REPLACE custOrderData SET orderId=?, dataType=?, dataValue=?", orderId, dataType, dataValue);
   }
   
   public static String getCustOrderData( String orderId, String dataType ) {
   	return db.getString("", "SELECT dataValue FROM custOrderData WHERE orderId='%s' AND dataType='%s'", orderId, dataType);
   }
   
   public static int createOrderPallet( String orderId, String palletType ) {
   	db.execute("INSERT INTO rdsPallets SET palletType='%s', refType='orderId', refValue='%s', createStamp=NOW()", palletType, orderId);
   	return db.getSequence();
   }
   
   public static void triggerCartonAudited( int cartonSeq, String operator ) {
		JSONObject json = new JSONObject() ;
	  	json.put("cartonSeq",cartonSeq) ;
	   db.execute("INSERT status SET statusType='cartonAudit', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   }
   
   public static void triggerCartonPacked( int cartonSeq, String operator ) {
		JSONObject json = new JSONObject() ;
	  	json.put("cartonSeq",cartonSeq) ;
	   db.execute("INSERT status SET statusType='cartonPack', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   }
   
   public static void triggerShipLabelRequest( int cartonSeq, String operator ) {
		JSONObject json = new JSONObject() ;
	  	json.put("cartonSeq",cartonSeq) ;
	   db.execute("INSERT status SET statusType='shipLabelRequest', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   } 
   
   public static void triggerCartonLabeled( int cartonSeq, String operator ) {
		JSONObject json = new JSONObject() ;
	  	json.put("cartonSeq",cartonSeq) ;
	   db.execute("INSERT status SET statusType='cartonLabel', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   }   
  
   public static void triggerCartonPalletized( int cartonSeq, int palletSeq, String operator ) {
   	db.execute("UPDATE rdsCartons SET palletSeq=%d, palletStamp=IFNULL(palletStamp,NOW()) WHERE cartonSeq=%d", palletSeq, cartonSeq);
		JSONObject json = new JSONObject() ;
	  	json.put("cartonSeq",cartonSeq) ;
	   db.execute("INSERT status SET statusType='cartonPalletized', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   }
   
   public static void triggerPalletClose( int palletSeq, String operator ) {
		JSONObject json = new JSONObject() ;
	  	json.put("palletSeq",palletSeq) ;
	   db.execute("INSERT status SET statusType='palletClose', data='%s', operator='%s' ,status='idle'",json.toString(),operator); 
   }
   
   public static void clearLocationAssignment( String location ) {
   	db.execute("UPDATE rdsLocations SET assignmentValue='' WHERE location='%s'", location);
   }
   
	public static void postWaveLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "waveSeq", code,  String.format( format, args ));
	}
   
	public static void postShipmentLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "shipmentId", code,  String.format( format, args ));
	}

	public static void postOrderLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "orderId", code,  String.format( format, args ));
	}
	
	public static void postCartonLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "cartonSeq", code,  String.format( format, args ));
	}
	
	public static void postCartLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "cartSeq", code,  String.format( format, args ));
	}
	
	public static void postPalletLog( String id, String code, String format, Object... args) {
		RDSHistory.post(id, "palletSeq", code,  String.format( format, args ));
	}
	
   /*
    * --- utilities ---
    */

   /** Gets a value from a {@code Map} or an empty string. */
   protected static String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String v = m.get( name );
      return (v == null) ? "" : v;
   }

   /** Gets a value from a {@code Map} and converts it to an int. */
   protected static int getMapInt( Map<String,String> m, String name ) {
      if (m == null)
         return -1;
      return RDSUtil.stringToInt( m.get( name ), -1 );
   }

   /** Gets a value from a {@code Map} and converts it to a double. */
   protected static double getMapDbl( Map<String,String> m, String name ) {
      if (m == null)
         return 0.0;
      return RDSUtil.stringToDouble( m.get( name ), 0.0 );
   }
   
   public static String truncate(String s, int len) { 
      if (s == null) return null;
      return s.substring(0, Math.min(len, s.length()));
   }

	
}
