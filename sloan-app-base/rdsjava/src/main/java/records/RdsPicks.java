package records;


public class RdsPicks {

  private long pickSeq;
  private long orderLineSeq;
  private String orderId;
  private long waveSeq;
  private String pickType;
  private String sku;
  private String uom;
  private String lot;
  private double qty;
  private long cartonSeq;
  private long needsConsolidation;
  private java.sql.Timestamp consolidationPutStamp;
  private java.sql.Timestamp consolidationPackStamp;
  private long readyForPick;
  private long picked;
  private long shortPicked;
  private long audited;
  private long canceled;
  private java.sql.Timestamp createStamp;
  private java.sql.Timestamp pickStamp;
  private java.sql.Timestamp shortStamp;
  private java.sql.Timestamp putStamp;
  private java.sql.Timestamp uploadStamp;
  private String pickOperatorId;
  private java.sql.Timestamp stamp;


  public long getPickSeq() {
    return pickSeq;
  }

  public void setPickSeq(long pickSeq) {
    this.pickSeq = pickSeq;
  }


  public long getOrderLineSeq() {
    return orderLineSeq;
  }

  public void setOrderLineSeq(long orderLineSeq) {
    this.orderLineSeq = orderLineSeq;
  }


  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }


  public long getWaveSeq() {
    return waveSeq;
  }

  public void setWaveSeq(long waveSeq) {
    this.waveSeq = waveSeq;
  }


  public String getPickType() {
    return pickType;
  }

  public void setPickType(String pickType) {
    this.pickType = pickType;
  }


  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }


  public String getUom() {
    return uom;
  }

  public void setUom(String uom) {
    this.uom = uom;
  }


  public String getLot() {
    return lot;
  }

  public void setLot(String lot) {
    this.lot = lot;
  }


  public double getQty() {
    return qty;
  }

  public void setQty(double qty) {
    this.qty = qty;
  }


  public long getCartonSeq() {
    return cartonSeq;
  }

  public void setCartonSeq(long cartonSeq) {
    this.cartonSeq = cartonSeq;
  }


  public long getNeedsConsolidation() {
    return needsConsolidation;
  }

  public void setNeedsConsolidation(long needsConsolidation) {
    this.needsConsolidation = needsConsolidation;
  }


  public java.sql.Timestamp getConsolidationPutStamp() {
    return consolidationPutStamp;
  }

  public void setConsolidationPutStamp(java.sql.Timestamp consolidationPutStamp) {
    this.consolidationPutStamp = consolidationPutStamp;
  }


  public java.sql.Timestamp getConsolidationPackStamp() {
    return consolidationPackStamp;
  }

  public void setConsolidationPackStamp(java.sql.Timestamp consolidationPackStamp) {
    this.consolidationPackStamp = consolidationPackStamp;
  }


  public long getReadyForPick() {
    return readyForPick;
  }

  public void setReadyForPick(long readyForPick) {
    this.readyForPick = readyForPick;
  }


  public long getPicked() {
    return picked;
  }

  public void setPicked(long picked) {
    this.picked = picked;
  }


  public long getShortPicked() {
    return shortPicked;
  }

  public void setShortPicked(long shortPicked) {
    this.shortPicked = shortPicked;
  }


  public long getAudited() {
    return audited;
  }

  public void setAudited(long audited) {
    this.audited = audited;
  }


  public long getCanceled() {
    return canceled;
  }

  public void setCanceled(long canceled) {
    this.canceled = canceled;
  }


  public java.sql.Timestamp getCreateStamp() {
    return createStamp;
  }

  public void setCreateStamp(java.sql.Timestamp createStamp) {
    this.createStamp = createStamp;
  }


  public java.sql.Timestamp getPickStamp() {
    return pickStamp;
  }

  public void setPickStamp(java.sql.Timestamp pickStamp) {
    this.pickStamp = pickStamp;
  }


  public java.sql.Timestamp getShortStamp() {
    return shortStamp;
  }

  public void setShortStamp(java.sql.Timestamp shortStamp) {
    this.shortStamp = shortStamp;
  }


  public java.sql.Timestamp getPutStamp() {
    return putStamp;
  }

  public void setPutStamp(java.sql.Timestamp putStamp) {
    this.putStamp = putStamp;
  }


  public java.sql.Timestamp getUploadStamp() {
    return uploadStamp;
  }

  public void setUploadStamp(java.sql.Timestamp uploadStamp) {
    this.uploadStamp = uploadStamp;
  }


  public String getPickOperatorId() {
    return pickOperatorId;
  }

  public void setPickOperatorId(String pickOperatorId) {
    this.pickOperatorId = pickOperatorId;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }

}
