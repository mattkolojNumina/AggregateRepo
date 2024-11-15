package records;

public class CustFullcaseLineData {
	private int fullcaseLineSeq;
	private int barcodeType;
	private String upc;
	
	public CustFullcaseLineData( int fullcaseLineSeq, int barcodeType, String upc ) {
		this.fullcaseLineSeq = fullcaseLineSeq;
		this.barcodeType = barcodeType;
		this.upc = upc;
	}
	
	public int getFullcaseLineSeq() { return this.fullcaseLineSeq; }
	public String getUpc() { return this.upc; }
	public int getBarcodeType() { return this.barcodeType; }
}
