package dao;

public class CustOutboundOrderFileDAO extends AbstractDAO {

	private static final String dataTable = "custOutboundOrderFiles";
   
   public static void setTombstone( int fileSeq, String field ) {
   	setTableTombStoneByIntId(dataTable,field,"fileSeq",fileSeq);
   }
   
   public static int create(String fileName) {
   	db.executePreparedStatement("INSERT INTO custOutboundOrderFiles SET "
   			+ "`fileName`=(?)", 
   			fileName);
   	return db.getSequence();
   }
   
   public static int getUnprocessedFile() {
   	return db.getInt(-1,"SELECT fileSeq FROM custOutboundOrderFiles "
   			+ "WHERE cartonizeStamp IS NOT NULL AND cartonizedUploadStamp IS NULL LIMIT 1");
   }

}
