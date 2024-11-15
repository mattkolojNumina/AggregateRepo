package dao;

import java.util.*;

public class RdsCartonDAO extends AbstractDAO {

	private static final String dataTable = "rdsCartons";

   public boolean tombStoneIsSet( String field ) {
  	 	return !getMapStr(recordMap,field).isEmpty();
   }
   
   public static void setTombstone( int cartonSeq, String field ) {
   	setTableTombStoneByIntId(dataTable,field,"cartonSeq",cartonSeq);
   }
   
   public static Map<String,String> getRecordMap(int cartonSeq){
   	return getTableRowByIntId(dataTable,"cartonSeq",cartonSeq);
   }
   
   public static String getFieldValueString(int cartonSeq, String field, String otherwise) {
   	return db.getString(otherwise, "SELECT %s FROM %s WHERE cartonSeq=%d", field, dataTable, cartonSeq);
   }

}
