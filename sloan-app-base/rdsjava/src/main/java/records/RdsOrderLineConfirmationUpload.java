package records;


public class RdsOrderLineConfirmationUpload {

  private long seq;
  private String typeCode;
  private String batchId;
  private String groupNumber;
  private String orderNumber;
  private String pageNumber;
  private String lineNumber;
  private String qtyOrdered;
  private String changedQtyFlag;
  private String qtyPicked;
  private String numOfTotes;
  private String toteSeqNumber;
  private String operatorId;
  private String qpaGroup;
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


  public String getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(String pageNumber) {
    this.pageNumber = pageNumber;
  }


  public String getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(String lineNumber) {
    this.lineNumber = lineNumber;
  }


  public String getQtyOrdered() {
    return qtyOrdered;
  }

  public void setQtyOrdered(String qtyOrdered) {
    this.qtyOrdered = qtyOrdered;
  }


  public String getChangedQtyFlag() {
    return changedQtyFlag;
  }

  public void setChangedQtyFlag(String changedQtyFlag) {
    this.changedQtyFlag = changedQtyFlag;
  }


  public String getQtyPicked() {
    return qtyPicked;
  }

  public void setQtyPicked(String qtyPicked) {
    this.qtyPicked = qtyPicked;
  }


  public String getNumOfTotes() {
    return numOfTotes;
  }

  public void setNumOfTotes(String numOfTotes) {
    this.numOfTotes = numOfTotes;
  }


  public String getToteSeqNumber() {
    return toteSeqNumber;
  }

  public void setToteSeqNumber(String toteSeqNumber) {
    this.toteSeqNumber = toteSeqNumber;
  }


  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }


  public String getQpaGroup() {
    return qpaGroup;
  }

  public void setQpaGroup(String qpaGroup) {
    this.qpaGroup = qpaGroup;
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
