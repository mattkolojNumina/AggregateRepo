package records;

public class CustOrder {
	private String orderId;
	private String shipmentId;
	private String custOrderId;
	private String orderType;
	private String customerNumber;
	private String truckNumber;
	private String door;
	private String stop;
	private String demandDate;
	private String poId;
	private String poNumbers;
	private String toteOrBox;
	private String toteCRC;
	private String QPAGroup;
	private String groupNumber;
	private int priority;
	private int qcRequired;
	private String status;
	private int waveSeq;
	private String errorMsg;
	
	public CustOrder( String orderId) {
		this.orderId = orderId;
		this.shipmentId = "";
		this.custOrderId = orderId;
		this.orderType = "";
		this.customerNumber = "";
		this.truckNumber = "";
		this.door = "";
		this.stop = "";
		this.demandDate = "";
		this.poId = "";
		this.poNumbers = "";
		this.toteOrBox = "";
		this.toteCRC = "";
		this.QPAGroup = "";
		this.groupNumber = "";
		this.priority = 0;
		this.qcRequired = 0;
		this.status="error";
		this.waveSeq=-1;
		this.errorMsg="";
	}
	
	public void setShipmentId( String shipmentId ) { this.shipmentId = shipmentId; }
	public void setCustOrderId( String custOrderId ) { this.custOrderId = custOrderId; }
	public void setOrderType( String orderType ) { this.orderType = orderType; }
	public void setCustomerNumber( String customerNumber ) { this.customerNumber = customerNumber; }
	public void setPoId( String poId ) { this.poId = poId; }
	public void setTruckNumber( String truckNumber ) { this.truckNumber = truckNumber; }
	public void setDoor( String door ) { this.door = door; }
	public void setStop( String stop ) { this.stop = stop; }
	public void setDemandDate( String demandDate ) { this.demandDate = demandDate; }
	public void setPoNumbers( String poNumbers ) { this.poNumbers = poNumbers; }
	public void setToteOrBox( String toteOrBox ) { this.toteOrBox = toteOrBox; }
	public void setToteCRC( String toteCRC ) { this.toteCRC = toteCRC; }
	public void setQPAGroup( String QPAGroup ) { this.QPAGroup = QPAGroup; }
	public void setGroupNumber( String groupNumber ) { this.groupNumber = groupNumber; }
	public void setPriority( int priority ) { this.priority = priority; }
	public void setQcRequired( int qcRequired ) { this.qcRequired = qcRequired; }
	public void setStatus( String status ) { this.status = status; }
	public void setWaveSeq( int waveSeq ) { this.waveSeq = waveSeq; }
	public void setErrorMsg( String errorMsg ) { this.errorMsg = errorMsg; }
	
	public String getOrderId() { return this.orderId; }
	public String getShipmentId() { return this.shipmentId; }
	public String getCustOrderId() { return this.custOrderId; }
	public String getOrderType() { return this.orderType; }
	public String getCustomerNumber() { return this.customerNumber; }
	public String getTruckNumber() { return this.truckNumber; }
	public String getDoor() { return this.door; }
	public String getStop() { return this.stop; }
	public int getPriority() { return this.priority; }
	public String getPoId() { return this.poId; }
	public String getPoNumbers() { return this.poNumbers; }
	public String getToteOrBox() { return this.toteOrBox; }
	public String getToteCRC() { return this.toteCRC; }
	public String getQPAGroup() { return this.QPAGroup; }
	public String getGroupNumber() { return this.groupNumber; }
	public int getQcRequired() { return this.qcRequired; }
	public String getStatus() { return this.status; }
	public int getWaveSeq() { return this.waveSeq; }
	public String getErrorMsg() { return this.errorMsg; }	
	public String getDemandDate() { return this.demandDate; }
}
