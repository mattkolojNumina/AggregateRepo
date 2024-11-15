package dao;

import records.CustFullcaseLineData;

public class CustFullcaseLineDataDAO extends AbstractDAO {

	private static final String dataTable = "custFullcaseLineData";
   
   public static void create(CustFullcaseLineData data) {
   	db.executePreparedStatement("REPLACE INTO custFullcaseLineData SET "
   			+ "`fullcaseLineSeq`=(?),`barcodeType`=(?),`upc`=(?)",
   			""+data.getFullcaseLineSeq(),""+data.getBarcodeType(),data.getUpc());
   }

}
