package app;

import java.util.List;
import java.util.Map;
import org.json.* ;
import java.time.ZonedDateTime ;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId ;
import rds.*;

public final class HostCommon {
	private static RDSDatabase db;
	private static DateTimeFormatter format ;
	private static ZoneId zoneId ;
	
	private HostCommon() {
	}
	
   public static void setDatabase( RDSDatabase db ) {
   	HostCommon.db = db;
   	RDSHistory.setDatabase(db);
   	RDSEvent.setDatabase(db);
   	RDSTrak.setDatabase(db);
   }
   
   private static String now() {
		format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") ;
		String timezone = db.getControl("system","timezone","America/New_York") ;
		zoneId = ZoneId.of(timezone) ;
      ZonedDateTime zdt = ZonedDateTime.now(zoneId) ;
      return zdt.format(format) ;
   }
   
   public static int lineCompletion( int orderLineSeq ){
   	int seq = 0;
   	Map<String,String> m = db.getRecordMap(
   			"SELECT * FROM custOrderLines WHERE orderLineSeq=%d", orderLineSeq);
   	if( m==null || m.isEmpty() )
   		return seq;
   	JSONObject json = new JSONObject() ;
   	json.put("orderId",getMapStr(m,"orderId")) ;
   	json.put("lineId",getMapStr(m,"lineId")) ;
   	json.put("sku",getMapStr(m,"sku")) ;
   	json.put("uom",getMapStr(m,"uom")) ;
   	json.put("location",getMapStr(m,"location")) ;
   	json.put("qty",getMapDbl(m,"actQty")) ;
   	json.put("status",getMapStr(m,"status")) ;
   	json.put("dateTime",now()) ;
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('lineCompletion','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
   }
   
   public static int cartonContents( int cartonSeq, String type ){
   	int seq = 0;
   	Map<String,String> cartonMap = db.getRecordMap(
   			"SELECT * FROM rdsCartons WHERE cartonSeq=%d", cartonSeq);
   	if( cartonMap==null || cartonMap.isEmpty() )
   		return seq;
   	
   	JSONObject json = new JSONObject() ;
   	json.put("orderId",getMapStr(cartonMap,"orderId")) ;
   	json.put("type",type) ;
   	json.put("cartonId",getMapStr(cartonMap,"ucc")) ;
   	json.put("weight",getMapDbl(cartonMap,"actWeight")) ;
   	json.put("length",getMapDbl(cartonMap,"actLength")) ;
   	json.put("width",getMapDbl(cartonMap,"actWidth")) ;
   	json.put("height",getMapDbl(cartonMap,"actHeight")) ;
   	json.put("dateTime",now()) ;
   	
      // get item list
      JSONArray item_list = new JSONArray() ;
      String pickStatusSql = type.equals("estimate")?"":"AND p.picked=1 AND p.shortPicked=0 AND p.canceled=0 ";
      List<Map<String,String>> items 
      	= db.getResultMapList("SELECT p.sku, p.uom, SUM(p.qty) AS total, "
                             +"l.lineId, p.pickOperatorId "
                             +"FROM rdsPicks AS p "
                             +"JOIN custOrderLines as l USING(orderLineSeq) "
                             +"WHERE p.cartonSeq=%s "
                             +"%s "
                             +"GROUP BY l.lineId,p.sku,p.uom,p.pickOperatorId ",
                              cartonSeq, pickStatusSql) ;
      for(Map<String,String> item : items) {
      	JSONObject entry = new JSONObject() ;
      	entry.put("lineId", getMapStr(item,"lineId")) ;
      	entry.put("sku", getMapStr(item,"sku")) ;
      	entry.put("uom", getMapStr(item,"uom")) ;
      	entry.put("operatorId", getMapStr(item,"pickOperatorId")) ;
      	entry.put("qty", getMapDbl(item,"total")) ;
      	item_list.put(entry) ;
      }
      json.put("items", item_list);
   	
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('cartonContents','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
   }  
   
   public static int shipmentStatus( String orderId, String status ){
   	int seq = 0;
   	JSONObject json = new JSONObject() ;
   	json.put("orderId",orderId) ;
   	json.put("status",status) ;
   	json.put("dateTime",now()) ;
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('shipmentStatus','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
   }   
   
   public static int cartonStatus( int cartonSeq, String status ){
   	int seq = 0;
   	JSONObject json = new JSONObject() ;
   	Map<String,String> cartonMap = db.getRecordMap(
   			"SELECT * FROM rdsCartons WHERE cartonSeq=%d", cartonSeq);
   	if( cartonMap==null || cartonMap.isEmpty() )
   		return seq;
   	json.put("shipmentId","") ;
   	json.put("orderId",getMapStr(cartonMap,"orderId")) ;
   	json.put("cartonId",getMapStr(cartonMap,"cartonId")) ;
   	json.put("status",status) ;
   	json.put("dateTime",now()) ;
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('cartonStatus','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
   } 
   
   public static int parcelShipStatus( int cartonSeq ){
   	int seq = 0;
   	JSONObject json = new JSONObject() ;
   	Map<String,String> cartonMap = db.getRecordMap(
   			"SELECT * FROM rdsCartons WHERE cartonSeq=%d", cartonSeq);
   	Map<String,String> cartonDataMap = db.getMap(
   			"SELECT dataType, dataValue FROM rdsCartonData WHERE cartonSeq=%d", cartonSeq);
   	if( cartonMap==null || cartonMap.isEmpty() )
   		return seq;
   	json.put("shipmentId","") ;
   	json.put("orderId",getMapStr(cartonMap,"orderId")) ;
   	json.put("cartonId",getMapStr(cartonMap,"cartonId")) ;
   	json.put("trackingNumber",getMapStr(cartonMap,"trackingNumber")) ;
   	json.put("shippingCharge",getMapStr(cartonDataMap,"shippingCharge")) ;
   	json.put("actualShippingMethod",getMapStr(cartonDataMap,"actualShippingMethod")) ;
   	json.put("logicalDestination",getMapStr(cartonMap,"lastPositionLogical")) ;
   	json.put("physicalDestination",getMapStr(cartonMap,"lastPositionPhysical")) ;
   	json.put("operatorId","") ; // what is the operator?
   	json.put("dateTime",now()) ;
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('parcelShipStatus','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
   } 
   
   public static int pallets( int palletSeq ){
   	int seq = 0;
   	Map<String,String> palletMap = db.getRecordMap(
   			"SELECT * FROM rdsPallets WHERE palletSeq=%d", palletSeq);
   	if( palletMap==null || palletMap.isEmpty() )
   		return seq;
   	
   	JSONObject json = new JSONObject() ;
   	json.put("shipmentId","") ;
   	json.put("orderId",getMapStr(palletMap,"refValue")) ;
   	json.put("palletId",getMapStr(palletMap,"lpn")) ;
   	json.put("closeDateTime",getMapStr(palletMap,"closeStamp")) ;
   	json.put("closeOperatorId",getMapStr(palletMap,"closeOperator")) ;
   	json.put("physicalLocation",getMapStr(palletMap,"lastPositionPhysical")) ;
   	json.put("logicalLocation",getMapStr(palletMap,"lastPositionLogical")) ;
   	
      // get carton list
      JSONArray carton_list = new JSONArray() ;
      List<Map<String,String>> cartons 
      	= db.getResultMapList("SELECT cartonId,palletOperatorId,palletStamp "
                             +"FROM rdsCartons "
                             +"WHERE palletSeq=%d ",
                              palletSeq) ;
      for(Map<String,String> carton : cartons) {
      	JSONObject entry = new JSONObject() ;
      	entry.put("cartonId", getMapStr(carton,"ucc")) ;
      	entry.put("operatorId", getMapStr(carton,"palletOperatorId")) ;
      	entry.put("dateTime", getMapStr(carton,"palletStamp")) ;
      	carton_list.put(entry) ;
      }
      json.put("cartons", carton_list);
   	
      db.execute("INSERT INTO rdsUploadQueue "
            +"(uploadType,data,status) "
            +"VALUES "
            +"('pallets','%s','notUploaded') ",
             json.toString()) ;
      seq = db.getSequence() ;
      return seq ;
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
