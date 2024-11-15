package records;

public class CustShipment{
	
	private String shipmentId;
	private String shipmentType;
	private String shippingMethod;
	private int shipToShippingInfoSeq;
	private int shipFromShippingInfoSeq;
	private int returnToShippingInfoSeq;
	private int soldToShippingInfoSeq;
	private int billingMethod;
	private String billingAccount;
	private String billingPostalCode;
	private int priority;
	private String customerId;
	private String poId;
	private int packslipRequired;
	private int cartonContentRequired;
	private int uccCartonLabelRequired;
	private int uccPalletLabelRequired;
	private int qcRequired;
	private String status;
	private int waveSeq;
	private String errorMsg;
	
	public CustShipment( String shipmentId ) {
		this.shipmentId = shipmentId;
		this.shipmentType = "";
		this.shippingMethod = "";
		this.shipToShippingInfoSeq = -1;
		this.shipFromShippingInfoSeq = -1;
		this.returnToShippingInfoSeq = -1;
		this.soldToShippingInfoSeq = -1;
		this.billingMethod = -1;
		this.billingAccount = "";
		this.billingPostalCode = "";
		this.priority = 0;
		this.customerId = "";
		this.poId = "";
		this.packslipRequired = 0;
		this.cartonContentRequired = 0;
		this.uccCartonLabelRequired = 0;
		this.uccPalletLabelRequired = 0;
		this.qcRequired = 0;
		this.status="error";
		this.waveSeq=-1;
		this.errorMsg="";
	}
	
	public void setShipmentType( String shipmentType ) { this.shipmentType = shipmentType; }
	public void setShippingMethod( String shippingMethod ) { this.shippingMethod = shippingMethod; }
	public void setShipToShippingInfoSeq( int shipToShippingInfoSeq ) { this.shipToShippingInfoSeq = shipToShippingInfoSeq; }
	public void setShipFromShippingInfoSeq( int shipFromShippingInfoSeq ) { this.shipFromShippingInfoSeq = shipFromShippingInfoSeq; }
	public void setReturnToShippingInfoSeq( int returnToShippingInfoSeq ) { this.returnToShippingInfoSeq = returnToShippingInfoSeq; }
	public void setSoldToShippingInfoSeq( int soldToShippingInfoSeq ) { this.soldToShippingInfoSeq = soldToShippingInfoSeq; }
	public void setBillingMethod( int billingMethod ) { this.billingMethod = billingMethod; }
	public void setBillingAccount( String billingAccount ) { this.billingAccount = billingAccount; }
	public void setBillingPostalCode( String billingPostalCode ) { this.billingPostalCode = billingPostalCode; }
	public void setPriority( int priority ) { this.priority = priority; }
	public void setCustomerId( String customerId ) { this.customerId = customerId; }
	public void setPoId( String poId ) { this.poId = poId; }
	public void setPackslipRequired( int packslipRequired ) { this.packslipRequired = packslipRequired; }
	public void setCartonContentRequired( int cartonContentRequired ) { this.cartonContentRequired = cartonContentRequired; }
	public void setUccCartonLabelRequired( int uccCartonLabelRequired ) { this.uccCartonLabelRequired = uccCartonLabelRequired; }
	public void setUccPalletLabelRequired( int uccPalletLabelRequired ) { this.uccPalletLabelRequired = uccPalletLabelRequired; }
	public void setQcRequired( int qcRequired ) { this.qcRequired = qcRequired; }
	public void setStatus( String status ) { this.status = status; }
	public void setWaveSeq( int waveSeq ) { this.waveSeq = waveSeq; }
	public void setErrorMsg( String errorMsg ) { this.errorMsg = errorMsg; }
	
	public String getShipmentId() { return this.shipmentId; }
	public String getShipmentType() { return this.shipmentType; }
	public String getShippingMethod() { return this.shippingMethod; }
	public int getShipToShippingInfoSeq() { return this.shipToShippingInfoSeq; }
	public int getShipFromShippingInfoSeq() { return this.shipFromShippingInfoSeq; }
	public int getReturnToShippingInfoSeq() { return this.returnToShippingInfoSeq; }
	public int getSoldToShippingInfoSeq() { return this.soldToShippingInfoSeq; }
	public int getBillingMethod() { return this.billingMethod; }
	public String getBillingAccount() { return this.billingAccount; }
	public String getBillingPostalCode() { return this.billingPostalCode; }
	public int getPriority() { return this.priority; }
	public String getCustomerId() { return this.customerId; }
	public String getPoId() { return this.poId; }
	public int getPackslipRequired() { return this.packslipRequired; }
	public int getCartonContentRequired() { return this.cartonContentRequired; }
	public int getUccCartonLabelRequired() { return this.uccCartonLabelRequired; }
	public int getUccPalletLabelRequired() { return this.uccPalletLabelRequired; }
	public int getQcRequired() { return this.qcRequired; }
	public String getStatus() { return this.status; }
	public int getWaveSeq() { return this.waveSeq; }
	public String getErrorMsg() { return this.errorMsg; }
	
}
