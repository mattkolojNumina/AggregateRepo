package dao;

import java.util.*;
import records.CustFullcaseLine;

public class CustFullcaseLinesDAO extends AbstractDAO {

	private static final String dataTable = "custFullcaseLines";
   
   public static void setTombstone( int fullcaseLineSeq, String field ) {
   	setTableTombStoneByIntId(dataTable,field,"fullcaseLineSeq",fullcaseLineSeq);
   }
   
   public static Map<String,String> getRecordMap(int fullcaseLineSeq){
   	return getTableRowByIntId(dataTable,"fullcaseLineSeq",fullcaseLineSeq);
   }
   
   public static String getFieldValueString(int fullcaseLineSeq, String field, String otherwise) {
   	return db.getString(otherwise, "SELECT %s FROM %s WHERE fullcaseLineSeq=%d", field, dataTable);
   }
   
   public static boolean exist(String orderId, String pageId, String lineId) {
   	return db.getInt(0, "SELECT fullcaseLineSeq FROM %s WHERE orderId='%s' AND pageId='%s' AND lineId='%s'", dataTable, orderId, pageId, lineId)>0;
   }
   
   public static int create(CustFullcaseLine line) {
   	db.executePreparedStatement("INSERT INTO custFullcaseLines SET "
   			+ "`orderId`=(?),`pageId`=(?),`lineId`=(?),"
   			+ "`truckNumber`=(?),`trackingPrefix`=(?),`verifyUPC`=(?)", 
   			line.getOrderId(),line.getPageId(),line.getLineId(),line.getTruckNumber(),line.getTrackingPrefix(),""+line.getVerifyUPC());
   	return db.getSequence();
   }

}
