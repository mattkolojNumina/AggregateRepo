package sloane;

import dao.DataAccessException;
import dao.CustFullcaseLineDataDAO;
import dao.CustFullcaseLinesDAO;
import host.FileRecord;
import host.StringUtils;
import rds.RDSUtil;
import records.CustFullcaseLine;
import records.CustFullcaseLineData;
import static sloane.SloaneConstants.*;

import java.util.ArrayList;
import java.util.List;

public class FullcaseScanTunnelFileRecord implements FileRecord {

	 private final String dField;
	 private final String cartonIndicator;
    private final String orderId;
    private final String orderPageNumber;
    private final String orderLineNumber;
    private final String upc;
    private final String truckNumber;
    private final String verifyUpc;
    private final String barcodeType;

    public FullcaseScanTunnelFileRecord(String[] fields) {
   	 dField = fields[0].trim();
   	 cartonIndicator = fields[1].trim();
   	 orderId = fields[2].trim();
   	 orderPageNumber = fields[3].trim();
   	 orderLineNumber = fields[4].trim();
   	 upc = fields[5].trim();
   	 verifyUpc = fields[6].trim();
   	 truckNumber = fields[7].trim();
   	 barcodeType = fields[8].trim();
    }
    
    public String getOrderId() { return this.orderId; }
    public String getLineId() { return this.orderLineNumber; }
    public String getPageId() { return this.orderPageNumber; }
    public String getCartonIndicator() { return this.cartonIndicator; }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (cartonIndicator == null || cartonIndicator.isBlank()) {
           validationErrors.add("Field [cartonIndicator] is empty.");
        } else if (cartonIndicator.length() !=1 ) {
      	  validationErrors.add("Field [cartonIndicator] length is not 1.");
        } else if (!("01").contains(cartonIndicator)) {
      	  validationErrors.add("Field [cartonIndicator] value is invalid");
        }
        
        if (orderId == null || orderId.isBlank()) {
           validationErrors.add("Field [invoice] is empty.");
        } else if (orderId.length() !=7 ) {
      	  validationErrors.add("Field [invoice] length is not 7.");
        } else if (!StringUtils.isNumeric(orderId)) {
           validationErrors.add("Field [invoice] is not numeric.");
        }
        
        if (orderPageNumber == null || orderPageNumber.isBlank()) {
           validationErrors.add("Field [orderPageNumber] is empty.");
        } else if (orderPageNumber.length() !=3 ) {
      	  validationErrors.add("Field [orderPageNumber] length is not 3.");
        } else if (!StringUtils.isNumeric(orderPageNumber)) {
           validationErrors.add("Field [orderPageNumber] is not numeric.");
        }
        
        if (orderLineNumber == null || orderLineNumber.isBlank()) {
           validationErrors.add("Field [orderLineNumber] is empty.");
        } else if (orderLineNumber.length() !=4 ) {
      	  validationErrors.add("Field [orderLineNumber] length is not 4.");
        } else if (!StringUtils.isNumeric(orderLineNumber)) {
           validationErrors.add("Field [orderLineNumber] is not numeric.");
        }
        
        if (upc == null || upc.isBlank()) {
           validationErrors.add("Field [upc] is empty.");
        } else if (barcodeType.equals("1") && upc.length() !=14 && upc.length() !=12) {
      	  validationErrors.add("Field [upc] length is not 14 for type 1.");
        } else if (barcodeType.equals("3") && upc.length() !=13 ) {
      	  validationErrors.add("Field [upc] length is not 13 for type 3.");
        } else if (barcodeType.equals("2") && upc.length() !=8 ) {
      	  validationErrors.add("Field [upc] length is not 8 for type 2.");
        } else if (!StringUtils.isNumeric(upc)) {
           validationErrors.add("Field [upc] is not numeric.");
        }
        
        if (verifyUpc == null || verifyUpc.isBlank()) {
           validationErrors.add("Field [verifyUpc] is empty.");
        } else if (cartonIndicator.length() !=1 ) {
      	  validationErrors.add("Field [verifyUpc] length is not 1.");
        } else if (!("01").contains(verifyUpc)) {
      	  validationErrors.add("Field [verifyUpc] value is invalid");
        }        
        
        if (truckNumber == null || truckNumber.isBlank()) {
           validationErrors.add("Field [truckNumber] is empty.");
        } else if (truckNumber.length() > 3 ) {
      	  validationErrors.add("Field [truckNumber] length exceeds 3.");
        }
        
        if (barcodeType == null || barcodeType.isBlank()) {
           validationErrors.add("Field [barcodeType] is empty.");
        } else if (barcodeType.length() !=1 ) {
      	  validationErrors.add("Field [barcodeType] length is not 1.");
        } else if (!("123").contains(barcodeType)) {
      	  validationErrors.add("Field [barcodeType] value is invalid");
        }        
        
        return validationErrors;
    }

    public void persist() throws DataAccessException {
    }
    
    public int checkFullcaseLine( String orderId, String pageId, String lineId ) {
   	 int fullcaseLineSeq = SQL_ERROR;
   	 if( CustFullcaseLinesDAO.exist(orderId, pageId, lineId) )
   		 return DUPLICATE;
   	 CustFullcaseLine fullcaseLine = new CustFullcaseLine(orderId,pageId,lineId);
   	 fullcaseLine.setTrackingPrefix(String.format("%s%s%s",orderId,pageId,lineId));
   	 fullcaseLine.setTruckNumber(this.truckNumber);
   	 fullcaseLine.setVerifyUPC(RDSUtil.stringToInt(this.verifyUpc, 1));
   	 fullcaseLineSeq = CustFullcaseLinesDAO.create(fullcaseLine);
   	 return fullcaseLineSeq;
    }
    
    public void createLineData(int fullcaseLineSeq) throws DataAccessException {
   	 int barcodeTypeInt = RDSUtil.stringToInt(this.barcodeType, 1);
       CustFullcaseLineData data = new CustFullcaseLineData( fullcaseLineSeq, barcodeTypeInt, this.upc);
       CustFullcaseLineDataDAO.create(data);
   }
}
