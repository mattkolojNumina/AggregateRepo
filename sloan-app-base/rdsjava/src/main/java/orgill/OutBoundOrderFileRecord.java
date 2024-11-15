package sloane;

import dao.DataAccessException;
import dao.CustOrderDAO;
import dao.CustOrderLineDAO;
import dao.CustOutboundOrderFileDAO;
import dao.RdsWaveDAO;
import host.FileRecord;
import host.StringUtils;
import records.RdsWave;
import records.CustOrder;
import records.CustOrderLine;
import static sloane.SloaneConstants.*;

import java.util.ArrayList;
import java.util.List;

public class OutBoundOrderFileRecord implements FileRecord {

	 private final String division;
	 private final String pickType;
	 private final String wave;
	 private final String waveName;
	 private final String group;
	 private final String linesInGroup;
    private final String orderId;
    private final String orderPageNumber;
    private final String orderLineNumber;
    private final String customerNumber;
    private final String truckNumber;
    private final String door;
    private final String stop;
    private final String demandDate;
    private final String sku;
    private final String department;
    private final String aisle;
    private final String bay;
    private final String slot;
    private final String shelf;
    private final String orderQuantity;
    private final String poNumber;
    private final String poNumbers;
    private final String toteOrBox;
    private final String putTote;
    private final String toteCRC;
    private final String isKitLine;
    private final String kitPageNumber;
    private final String kitLineNumber;
    private final String pieceCount;
    private final String unitOfMeasure;
    private final String packFactor;
    private final String standardTime;
    private final String headsUpMessage;
    private final String QPAGroup;
    private final String uom;
    private final String labelSequenceNumber;
    private final String shelfPackQty;
    private final String appendedStop;

    public OutBoundOrderFileRecord(String[] fields) {
   	 division = fields[0].trim();
   	 pickType = fields[1].trim();
   	 wave = fields[2].trim();
   	 waveName = fields[3].trim();
   	 group = fields[4].trim();
   	 linesInGroup = fields[5].trim();
   	 orderId = fields[6].trim();
   	 orderPageNumber = fields[7].trim();
   	 orderLineNumber = fields[8].trim();
   	 customerNumber = fields[9].trim();
   	 truckNumber = fields[10].trim();
   	 door = fields[11].trim();
   	 stop = fields[12].trim();
   	 demandDate = fields[13].trim();
   	 sku = fields[14].trim();
   	 department = fields[15].trim();
   	 aisle = fields[16].trim();
   	 bay = fields[17].trim();
   	 slot = fields[18].trim();
   	 shelf = fields[19].trim();
   	 orderQuantity = fields[20].trim();
   	 poNumber = fields[21].trim();
   	 poNumbers = fields[22].trim();
   	 toteOrBox = fields[23].trim();
   	 putTote = fields[24].trim();
   	 toteCRC = fields[25].trim();
   	 isKitLine = fields[26].trim();
   	 kitPageNumber = fields[27].trim();
   	 kitLineNumber = fields[28].trim();
   	 pieceCount = fields[29].trim();
   	 unitOfMeasure = fields[30].trim();
   	 packFactor = fields[31].trim();
   	 standardTime = fields[32].trim();
   	 headsUpMessage = fields[33].trim();
   	 QPAGroup = fields[34].trim();
   	 uom = fields[35].trim();
   	 labelSequenceNumber = fields[36].trim();
   	 shelfPackQty = fields[37].trim();
   	 appendedStop = stop + putTote;
    }
    
    public String getWaveName() { return this.waveName; }
    public String getOrderId() { return this.orderId; }
    public String getLineId() { return this.orderLineNumber; }
    public String getPageId() { return this.orderPageNumber; }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (pickType == null || pickType.isBlank()) {
           validationErrors.add("Field [pickType] is empty.");
        } else if (!pickType.equals("R")) {
      	  validationErrors.add("Field [pickType] is not R.");
        }
        
        if (waveName == null || waveName.isBlank()) {
           validationErrors.add("Field [batchID] is empty.");
        } else if (waveName.length() !=7 ) {
      	  validationErrors.add("Field [batchID] length is not 7.");
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
        } else if (orderPageNumber.length() !=7 ) {
      	  validationErrors.add("Field [orderPageNumber] length is not 7.");
        } else if (!StringUtils.isNumeric(orderPageNumber)) {
           validationErrors.add("Field [orderPageNumber] is not numeric.");
        }
        
        if (orderLineNumber == null || orderLineNumber.isBlank()) {
           validationErrors.add("Field [orderLineNumber] is empty.");
        } else if (orderLineNumber.length() !=7 ) {
      	  validationErrors.add("Field [orderLineNumber] length is not 7.");
        } else if (!StringUtils.isNumeric(orderLineNumber)) {
           validationErrors.add("Field [orderLineNumber] is not numeric.");
        }
        
        if (customerNumber == null || customerNumber.isBlank()) {
           validationErrors.add("Field [customerNumber] is empty.");
        } else if (customerNumber.length() !=6 ) {
      	  validationErrors.add("Field [customerNumber] length is not 6.");
        } else if (!StringUtils.isNumeric(customerNumber)) {
           validationErrors.add("Field [customerNumber] is not numeric.");
        }
        
        if (truckNumber == null || truckNumber.isBlank()) {
           validationErrors.add("Field [truckNumber] is empty.");
        } else if (truckNumber.length() > 3 ) {
      	  validationErrors.add("Field [truckNumber] length exceeds 3.");
        } 
        
        if (door == null || door.isBlank()) {
           validationErrors.add("Field [door] is empty.");
        } else if (door.length() > 3 ) {
      	  validationErrors.add("Field [door] length exceeds 3.");
        } 
        
        if (stop == null || stop.isBlank()) {
           validationErrors.add("Field [stop] is empty.");
        } else if (stop.length() > 5 ) {
      	  validationErrors.add("Field [stop] length exceeds 5.");
        } 
        
        if (demandDate == null ) {
           validationErrors.add("Field [demandDate] is empty.");
        } else if (demandDate.length() > 8 ) {
      	  validationErrors.add("Field [demandDate] length exceeds 8.");
        }
        
        if (sku == null || sku.isBlank()) {
           validationErrors.add("Field [part number] is empty.");
        } else if (sku.length() > 7 ) {
      	  validationErrors.add("Field [part number] length exceeds 7.");
        }
        
        if (department == null || department.isBlank()) {
           validationErrors.add("Field [department] is empty.");
        } else if (department.length() > 1 ) {
      	  validationErrors.add("Field [department] length exceeds 1.");
        }
        
        if (aisle == null || aisle.isBlank()) {
           validationErrors.add("Field [aisle] is empty.");
        } else if (aisle.length() > 2 ) {
      	  validationErrors.add("Field [aisle] length exceeds 2.");
        }
        
        if (bay == null || bay.isBlank()) {
           validationErrors.add("Field [bay] is empty.");
        } else if (bay.length() > 2 ) {
      	  validationErrors.add("Field [bay] length exceeds 2.");
        }
        
        if (slot == null || slot.isBlank()) {
           validationErrors.add("Field [slot] is empty.");
        } else if (slot.length() > 1 ) {
      	  validationErrors.add("Field [slot] length exceeds 1.");
        }
        
        if (shelf == null || shelf.isBlank()) {
           validationErrors.add("Field [shelf] is empty.");
        } else if (shelf.length() > 1 ) {
      	  validationErrors.add("Field [shelf] length exceeds 1.");
        }
        
        if (orderQuantity == null || orderQuantity.isBlank()) {
           validationErrors.add("Field [orderQuantity] is empty.");
        } else if (orderQuantity.length() > 7 ) {
      	  validationErrors.add("Field [orderQuantity] length exceeds 7.");
        } else if (!StringUtils.isNumeric(orderQuantity)) {
           validationErrors.add("Field [orderQuantity] is not numeric.");
        }
        
        if (poNumber == null ) {
           validationErrors.add("Field [poNumber] is empty.");
        } else if (shelf.length() > 25 ) {
      	  validationErrors.add("Field [poNumber] length exceeds 25.");
        }
        
        if (poNumbers == null ) {
           validationErrors.add("Field [poNumbers] is empty.");
        } else if (shelf.length() > 25 ) {
      	  validationErrors.add("Field [poNumbers] length exceeds 25.");
        }
        
        if (toteOrBox == null || toteOrBox.isBlank()) {
           validationErrors.add("Field [toteOrBox] is empty.");
        } else if (toteOrBox.length() > 25 ) {
      	  validationErrors.add("Field [toteOrBox] length exceeds 25.");
        }
        
        if (toteCRC == null ) {
           validationErrors.add("Field [toteCRC] is empty.");
        } else if (toteCRC.length() > 2 ) {
      	  validationErrors.add("Field [toteCRC] length exceeds 2.");
        } else if (!StringUtils.isNumeric(toteCRC)) {
           validationErrors.add("Field [toteCRC] is not numeric.");
        }
        
        if (headsUpMessage != null && !headsUpMessage.isBlank()) {
           validationErrors.add("Field [headsUpMessage] is not blank.");
        } 
        
        if (QPAGroup == null ) {
           validationErrors.add("Field [QPAGroup] is empty.");
        } else if (QPAGroup.length() > 3 ) {
      	  validationErrors.add("Field [QPAGroup] length exceeds 3.");
        }
        
        if (group == null) {
           validationErrors.add("Field [group] is empty.");
        } else if (group.length() > 7 ) {
      	  validationErrors.add("Field [group] length exceeds 7.");
        }        
        
        if (uom == null || uom.isBlank()) {
           validationErrors.add("Field [uom] is empty.");
        } else if (uom.length() > 5 ) {
      	  validationErrors.add("Field [uom] length exceeds 5.");
        }
        
        if (shelfPackQty == null || shelfPackQty.isBlank()) {
           validationErrors.add("Field [shelfPackQty] is empty.");
        } else if (shelfPackQty.length() > 7 ) {
      	  validationErrors.add("Field [shelfPackQty] length exceeds 7.");
        } else if (!StringUtils.isNumeric(shelfPackQty)) {
           validationErrors.add("Field [shelfPackQty] is not numeric.");
        }
        return validationErrors;
    }

    public void persist() throws DataAccessException {
    }
    
    public int createFile( String fileName )throws DataAccessException {
   	 int fileSeq = SQL_ERROR;
   	 fileSeq = CustOutboundOrderFileDAO.create(fileName);
   	 return fileSeq;
    }
    
    public int checkWave( String waveName, int fileSeq ) throws DataAccessException {
   	 int waveSeq = SQL_ERROR;
   	 RdsWaveDAO waveDao = new RdsWaveDAO();
   	 CustOrderDAO orderDao = new CustOrderDAO();
   	 if( waveDao.exist(waveName) ) {
   		 return DUPLICATE;
   	 }
   	 List<String> orders = waveDao.getOrders(waveName);
   	 for( String orderId: orders ) {
   		 CustOrder order = new CustOrder(orderId);
   		 orderDao.delete(order);
   	 }
   	 RdsWave wave = new RdsWave(waveName);
   	 wave.setFileSeq(fileSeq);
   	 waveDao.delete(wave);
   	 waveSeq = waveDao.createNewWave(wave);
   	 return waveSeq;
    }
    
    public int checkOrder( String orderId, int waveSeq ) throws DataAccessException {
   	 CustOrderDAO orderDao = new CustOrderDAO();
   	 if( orderDao.exist( orderId ) )
   		 return DUPLICATE;
   	 CustOrder order = new CustOrder(orderId);
   	 orderDao.delete( order );
   	 order.setCustomerNumber(this.customerNumber);
   	 order.setCustOrderId(this.orderId);
   	 order.setDoor(this.door);
   	 order.setErrorMsg("");
   	 order.setPoId(this.poNumber);
   	 order.setDemandDate(this.demandDate);
   	 order.setPoNumbers(this.poNumbers);
   	 order.setQPAGroup(this.QPAGroup);
   	 order.setGroupNumber(this.group);
   	 order.setStop(this.appendedStop);
   	 order.setToteCRC(this.toteCRC);
   	 order.setToteOrBox(this.toteOrBox);
   	 order.setTruckNumber(this.truckNumber);
   	 order.setWaveSeq( waveSeq );
   	 orderDao.save(order);
   	 return GOOD_RESULT;
    }
    
    public int checkOrderLine( String orderId, String pageId, String lineId ) throws DataAccessException {
   	 CustOrderLineDAO lineDao = new CustOrderLineDAO();
   	 if( lineDao.exist( orderId, pageId, lineId ) )
   		 return DUPLICATE;
   	 return GOOD_RESULT;
    }
    
    public void createLine() throws DataAccessException {
       CustOrderLine line = new CustOrderLine( orderId, orderPageNumber, orderLineNumber);
       line.setSku( sku );
       line.setUom( uom );
       line.setShelfPackQty( Integer.parseInt(shelfPackQty) );
       line.setQty( Double.parseDouble(orderQuantity) );
       line.setDescription(headsUpMessage);
       line.setGroupNumber(this.group);
       line.setLineQPAGroup(this.QPAGroup);
       line.setLocation(String.format("%s%s%s%s%s", department,aisle,bay,slot,shelf));
       line.setStatus("notProcessed");
       CustOrderLineDAO dao = new CustOrderLineDAO();
       dao.save(line);
   }
}
