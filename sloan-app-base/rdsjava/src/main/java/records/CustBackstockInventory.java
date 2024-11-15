package records;


public class CustBackstockInventory {

  private long seq;
  private String location;
  private String sku;
  private long qty;
  private java.sql.Timestamp downloadStamp;
  private java.sql.Timestamp stamp;


  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }


  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }


  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }


  public long getQty() {
    return qty;
  }

  public void setQty(long qty) {
    this.qty = qty;
  }


  public java.sql.Timestamp getDownloadStamp() {
    return downloadStamp;
  }

  public void setDownloadStamp(java.sql.Timestamp downloadStamp) {
    this.downloadStamp = downloadStamp;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }
  private CustBackstockInventory(CustBackstockInventoryBuilder builder) {
    this.location = builder.location;
    this.sku = builder.sku;
    this.qty = Long.parseLong(String.valueOf(builder.qty));
  }

  public static class CustBackstockInventoryBuilder {
    private final String location;
    private final String sku;
    private final long qty;

    public CustBackstockInventoryBuilder(String location, String sku, String qty) {
      this.location = location;
      this.sku = sku;
      this.qty = Long.parseLong(qty);
    }

    public CustBackstockInventory build() {
    return new CustBackstockInventory(this);
  }
  }
}
