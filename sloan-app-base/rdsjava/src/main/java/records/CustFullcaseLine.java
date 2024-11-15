package records;

public class CustFullcaseLine {
	private String orderId;
	private String pageId;
	private String lineId;
	private String truckNumber;
	private String trackingPrefix;
	private int verifyUPC ;
	
	public CustFullcaseLine( String orderId, String pageId, String lineId ) {
		this.orderId = orderId;
		this.pageId = pageId;
		this.lineId = lineId;
		this.truckNumber = "";
		this.trackingPrefix = "";
		this.verifyUPC = 1;
	}
	
	public void setTruckNumber( String truckNumber ) { this.truckNumber = truckNumber; }
	public void setTrackingPrefix( String trackingPrefix ) { this.trackingPrefix = trackingPrefix; }
	public void setVerifyUPC( int verifyUPC ) { this.verifyUPC = verifyUPC; }
	
	public String getOrderId() { return this.orderId; }
	public String getPageId() { return this.pageId; }
	public String getLineId() { return this.lineId; }
	public String getTruckNumber() { return this.truckNumber; }
	public String getTrackingPrefix() { return this.trackingPrefix; }
	public int getVerifyUPC() { return this.verifyUPC; }
}
