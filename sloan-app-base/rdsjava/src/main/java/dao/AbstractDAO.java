package dao;

import java.util.List;
import java.util.Map;

import rds.RDSCounter;
import rds.RDSDatabase;
import rds.RDSHistory;
import rds.RDSUtil;

public abstract class AbstractDAO {
	public static RDSDatabase db;
	protected Map<String,String> recordMap;
	protected static String dataTable;

	public static boolean stringIdExistInTable( String tableName, String column, String id ) {
		return db.getInt(0, "SELECT COUNT(*) FROM %s WHERE %s='%s'", tableName, column, id) == 1;
	}
	
	public static boolean intIdExistInTable( String tableName, String column, int id ) {
		return db.getInt(0, "SELECT COUNT(*) FROM %s WHERE %s=%d", tableName, column, id) == 1;
	}
	
	public static boolean pairIdExistInTable( String tableName, String col1, String val1, String col2, String val2 ) {
		return db.getInt(0, "SELECT COUNT(*) FROM %s WHERE %s='%s' AND %s='%s'", tableName, col1, val1, col2, val2) == 1;
	}
	
	public static Map<String,String> getTableRowByStringId( String tableName, String column, String id ){
		return db.getRecordMap("SELECT * FROM %s WHERE %s='%s'", tableName, column, id);
	}

	public static Map<String,String> getTableRowByIntId( String tableName, String column, int id ){
		return db.getRecordMap("SELECT * FROM %s WHERE %s=%d", tableName, column, id);
	}

	public static List<Map<String,String>> getTableRowsListByColumnInt( String tableName, String column, int value ){
		return db.getResultMapList("SELECT * FROM %s WHERE %s=%d", tableName, column, value);
	}

	public static List<Map<String,String>> getTableRowsListByColumnString(String tableName, String column, String value ){
		return db.getResultMapList("SELECT * FROM %s WHERE `%s`='%s'", tableName, column, value);
	}
	
	public static Map<String,String> getTableRowByPairId( String tableName, String col1, String val1, String col2, String val2 ){
		return db.getRecordMap("SELECT * FROM %s WHERE %s='%s' AND %s='%s'", tableName, col1, val1, col2, val2);
	}
	
	public static List<String> getTableValueListByColumnInt( String tableName, String column, String idColumn, int id ){
		return db.getValueList("SELECT `%s` FROM %s WHERE `%s`=%d",column,tableName,idColumn,id);
	}
	
	public static List<String> getTableValueListByColumnString( String tableName, String column, String idColumn, String id ){
		return db.getValueList("SELECT `%s` FROM %s WHERE `%s`='%s'",column,tableName,idColumn,id);
	}
	
	public static List<String> getTableDistinctValueListByColumnInt( String tableName, String column, String idColumn, int id ){
		return db.getValueList("SELECT DISTINCT `%s` FROM %s WHERE `%s`=%d",column,tableName,idColumn,id);
	}
	
	public static List<String> getTableDistinctValueListByColumnString( String tableName, String column, String idColumn, String id ){
		return db.getValueList("SELECT DISTINCT `%s` FROM %s WHERE `%s`='%s'",column,tableName,idColumn,id);
	}	
	
	public static void deleteTableRecordByIntId( String tableName, String column, int id ) {
		db.execute("DELETE FROM %s WHERE %s=%d", tableName, column, id);
	}
	
	public static void setTableTombStoneByIntId( String tableName, String tombStone, String pkColumn, int id) {
		db.execute("UPDATE %s SET %s=IFNULL(%s,NOW()) WHERE %s=%d", tableName,tombStone,tombStone,pkColumn,id);
	}
	
	public static void setTableTombStoneByStringId( String tableName, String tombStone, String pkColumn, String id) {
		db.execute("UPDATE %s SET %s=IFNULL(%s,NOW()) WHERE %s='%s'", tableName,tombStone,tombStone,pkColumn,id);
	}
	
	public static void clearTableTombStoneByIntId( String tableName, String tombStone, String pkColumn, int id) {
		db.execute("UPDATE %s SET %s=NULL WHERE %s=%d", tableName,tombStone,pkColumn,id);
	}
	
	public static void clearTableTombStoneByStringId( String tableName, String tombStone, String pkColumn, String id) {
		db.execute("UPDATE %s SET %s=NULL WHERE %s='%s'", tableName,tombStone,pkColumn,id);
	}
	
	public static boolean tombStoneSetForIntIdInTable( String tableName, String pkColumn, int id, String tombStone ) {
		return db.getInt(0, "SELECT COUNT(*) FROM %s WHERE %s=%d AND %s IS NOT NULL", tableName, pkColumn, id, tombStone) == 1;
	}
	
	public static boolean tombStoneSetForStringIdInTable( String tableName, String pkColumn, int id, String tombStone ) {
		return db.getInt(0, "SELECT COUNT(*) FROM %s WHERE %s='%s' AND %s IS NOT NULL", tableName, pkColumn, id, tombStone) == 1;
	}

	public static int updateRowByIntIdIntValue(String tableName, String column, int value, String idColumn, int idValue) {
		return db.execute("UPDATE `%s` SET %s=%d WHERE %s=%d  ",tableName,column,value,idColumn,idValue);
	}

	public static int updateRowByIntIdStringValue(String tableName, String column, String value, String idColumn, int idValue) {
		return db.execute("UPDATE `%s` SET %s='%s' WHERE %s=%d  ",tableName,column,value,idColumn,idValue);
	}

	public static int updateRowByIntIdTimeStampValue(String tableName, String column, String value, String idColumn, int idValue) {
		return db.execute("UPDATE `%s` SET %s=%s WHERE %s=%d  ",tableName,column,value,idColumn,idValue);
	}

	public static int updateRowByStringIdStringValue(String tableName, String column, String value, String idColumn, String idValue) {
		return db.execute("UPDATE `%s` SET %s='%s' WHERE %s='%s' ",tableName,column,value,idColumn,idValue);
	}


	public int updateRowByStringIdStringValue(String tableName, String column, String value, String idColumn1, String idValue1,String idColumn2, String idValue2) {
		return db.execute("UPDATE `%s` SET %s='%s' WHERE %s='%s' AND %s='%s'",tableName,column,value,idColumn1,idValue1,idColumn2,idValue2);
	}

	public static int updateRowByStringIdIntValue(String tableName, String column, int value, String idColumn, String idValue) {
		return db.execute("UPDATE `%s` SET %s=%d WHERE %s='%s' ",tableName,column,value,idColumn,idValue);
	}

	public static int updateRowByStringIdTimeStampValue(String tableName, String column, String value, String idColumn, String idValue) {
		return db.execute("UPDATE `%s` SET %s=%s WHERE %s='%s'  ",tableName,column,value,idColumn,idValue);
	}
	
	public void setRecordMapByStringId(String tableName, String column, String id ) {
		recordMap = getTableRowByStringId( tableName, column, id );
	}
	
	public void setRecordMapByIntId(String tableName, String column, int id ) {
		recordMap = getTableRowByIntId( tableName, column, id );
	}
	
	public Map<String,String> getRecordMap(){
		return this.recordMap;
	}
	
	public int getRecordMapInt( String name ) {
		return getMapInt(recordMap,name);
	}
	
	public String getRecordMapStr( String name ) {
		return getMapStr(recordMap,name);
	}
	
	public double getRecordMapDbl( String name ) {
		return getMapDouble(recordMap,name);
	}
	
	public boolean getRecordMapBoolean( String name ) {
		return getMapBoolean(recordMap,name);
	}
	
	public static void postLog( String idType, String id, String code, String message ) {
		db.executePreparedStatement("INSERT INTO `log` SET idType=?,id=?,code=?,message=?,stamp=NOW()", idType,id,code,message);
	}
	
	public static void postWaveLog( String id, String code, String format, Object... args) {
		postLog("waveSeq",id,code,String.format(format, args));
	}
   
	public static void postShipmentLog( String id, String code, String format, Object... args) {
		postLog("shipmentId",id,code,String.format(format, args));
	}

	public static void postOrderLog( String id, String code, String format, Object... args) {
		postLog("orderId",id,code,String.format(format, args));
	}
	
	public static void postCartonLog( String id, String code, String format, Object... args) {
		postLog("cartonSeq",id,code,String.format(format, args));
	}
	
	public static void postCartLog( String id, String code, String format, Object... args) {
		postLog("cartSeq",id,code,String.format(format, args));
	}
	
	public static void postPalletLog( String id, String code, String format, Object... args) {
		postLog("palletSeq",id,code,String.format(format, args));
	}
	
	public static List<Map<String,String>> getStatusMessages( String appName ){
		return db.getResultMapList("SELECT * FROM `status` WHERE appName='%s' AND statusType<>'delayLogout' AND `status`='idle' ORDER BY seq", appName);
	}
	
	public static Map<String,String> getStatusMessage( String appName ){
		return db.getRecordMap("SELECT * FROM `status` WHERE appName='%s' AND `status`='idle' ORDER BY seq LIMIT 1", appName);
	}
	
	public static void insertStatusMessages( String appName, String statusType, String data, String op ) {
		db.execute("INSERT status SET appName='%s', statusType='%s', data='%s', operator='%s' ,status='idle'",appName,statusType,data,op); 
	}
	
	public static void setStatusMessageDone( int seq ) {
		db.execute("UPDATE status SET status='done' WHERE seq=%d", seq);
	}
	
	public static void setStatusMessageError( int seq, String errorMsg ) {
		db.executePreparedStatement("UPDATE status SET status='error', errorMsg=? WHERE seq=?", errorMsg, ""+seq);
	}
	
   protected static int getMapInt(Map<String, String> m, String name) {
      return getMapInt(-1,m,name);
   }

   protected static  int getMapInt(int otherwise, Map<String, String> m, String name) {
      if (m == null)
         return otherwise;
      return RDSUtil.stringToInt(m.get(name), otherwise);
   }

   protected static String getMapStr(Map<String, String> m, String name) {
		if (m == null)
			return "";
		String val = m.get(name);
		return (val == null) ? "" : val;
	}

	protected static double getMapDouble(Map<String, String> m, String name) {
		if (m == null)
			return 0.0;
		return RDSUtil.stringToDouble(m.get(name), 0.0);
	}
	
	protected static boolean getMapBoolean(Map<String,String> m, String name) {
		if (m == null) return false ;
		if (m.get(name) == null) return false ;
		return Boolean.parseBoolean(m.get(name) ) ;
	}
	
   public static String truncate(String s, int len) { 
      if (s == null) return null;
      return s.substring(0, Math.min(len, s.length()));
   }	

	public static void setDatabase(RDSDatabase rds) {
		db = rds;
      RDSHistory.setDatabase(db); 
      RDSCounter.setDatabase(db); 
	}


	
   /** An {@code Exception} due to failure during processing. */
   public static class ProcessingException extends Exception {
      public ProcessingException(String message) {
         super(message);
      }

      public ProcessingException(String message, Throwable cause) {
         super(message, cause);
      }
   }
}