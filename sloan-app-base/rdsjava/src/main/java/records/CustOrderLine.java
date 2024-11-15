package records;

public class CustOrderLine {
	private String orderId;
	private String pageId;
	private String lineId;
	private String orderLineRef;
	private String sku;
	private String uom;
	private String lot;
	private String description;
	private double qty;
	private int shelfPackQty ;
	private String location;
	private String status;
	private String groupNumber;
	private String lineQPAGroup;
	
	public CustOrderLine( String orderId, String pageId, String lineId ) {
		this.orderId = orderId;
		this.pageId = pageId;
		this.lineId = lineId;
		this.orderLineRef = "";
		this.sku = "";
		this.uom = "";
		this.lot = "";
		this.description = "";
		this.qty = 0;
		this.shelfPackQty = 1;
		this.location = "";
		this.groupNumber = "";
		this.lineQPAGroup = "";
		this.status = "notProcessed";
	}
	
	public void setOrderLineRef( String orderLineRef ) { this.orderLineRef = orderLineRef; }
	public void setSku( String sku ) { this.sku = sku; }
	public void setUom( String uom ) { this.uom = uom; }
	public void setLot( String lot ) { this.lot = lot; }
	public void setDescription( String description ) { this.description = description; }
	public void setQty( double qty ) { this.qty = qty; }
	public void setShelfPackQty( int shelfPackQty ) { this.shelfPackQty = shelfPackQty; }
	public void setLocation( String location ) { this.location = location; }
	public void setStatus( String status ) { this.status = status; }
	public void setGroupNumber( String groupNumber ) { this.groupNumber = groupNumber; } 
	public void setLineQPAGroup( String lineQPAGroup ) { this.lineQPAGroup = lineQPAGroup; } 
	
	public String getOrderId() { return this.orderId; }
	public String getPageId() { return this.pageId; }
	public String getLineId() { return this.lineId; }
	public String getOrderLineRef() { return this.orderLineRef; }
	public String getSku() { return this.sku; }
	public String getUom() { return this.uom; }
	public String getLot() { return this.lot; }
	public String getDescription() { return this.description; }
	public double getQty() { return this.qty; }
	public int getShelfPackQty() { return this.shelfPackQty; }
	public String getLocation() { return this.location; }
	public String getStatus() { return this.status; }	
	public String getGroupNumber() { return this.groupNumber; };
	public String getLineQPAGroup() { return this.lineQPAGroup; };
}
