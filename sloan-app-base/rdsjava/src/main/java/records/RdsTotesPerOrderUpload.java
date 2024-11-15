package records;


public class RdsTotesPerOrderUpload {

  private long seq;
  private String typeCode;
  private String finalFlag;
  private String batchId;
  private String groupNumber;
  private String orderNumber;
  private String totalTotes;
  private String processed;
  private java.sql.Timestamp createStamp;
  private java.sql.Timestamp stamp;


  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }


  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }


  public String getFinalFlag() {
    return finalFlag;
  }

  public void setFinalFlag(String finalFlag) {
    this.finalFlag = finalFlag;
  }


  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }


  public String getGroupNumber() {
    return groupNumber;
  }

  public void setGroupNumber(String groupNumber) {
    this.groupNumber = groupNumber;
  }


  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }


  public String getTotalTotes() {
    return totalTotes;
  }

  public void setTotalTotes(String totalTotes) {
    this.totalTotes = totalTotes;
  }


  public String getProcessed() {
    return processed;
  }

  public void setProcessed(String processed) {
    this.processed = processed;
  }


  public java.sql.Timestamp getCreateStamp() {
    return createStamp;
  }

  public void setCreateStamp(java.sql.Timestamp createStamp) {
    this.createStamp = createStamp;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }

}
