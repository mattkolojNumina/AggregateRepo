package records;


public class RdsCartons {

  private long cartonSeq;
  private String lpn;
  private String ucc;
  private String upc;
  private String trackingNumber;
  private String orderId;
  private String cartonType;
  private String pickType;
  private String reservedBy;
  private long cartSeq;
  private String cartSlot;
  private long parentCartonSeq;
  private String palletGroup;
  private long palletSeq;
  private String palletOperatorId;
  private double estWeight;
  private double estWeightLow;
  private double estWeightHigh;
  private double estLength;
  private double estWidth;
  private double estHeight;
  private double actWeight;
  private double actLength;
  private double actWidth;
  private double actHeight;
  private double netShippingCharge;
  private double listShippingCharge;
  private long assigned;
  private long auditRequired;
  private long repackRequired;
  private long packException;
  private java.sql.Timestamp createStamp;
  private java.sql.Timestamp releaseStamp;
  private java.sql.Timestamp readyForGeekStamp;
  private java.sql.Timestamp pickStartStamp;
  private java.sql.Timestamp pickStamp;
  private java.sql.Timestamp shortStamp;
  private java.sql.Timestamp auditStamp;
  private java.sql.Timestamp packStamp;
  private java.sql.Timestamp vasStamp;
  private java.sql.Timestamp labelStamp;
  private java.sql.Timestamp shipStamp;
  private java.sql.Timestamp palletStamp;
  private java.sql.Timestamp cancelStamp;
  private String lastPositionLogical;
  private String lastPositionPhysical;
  private java.sql.Timestamp lastPositionStamp;
  private java.sql.Timestamp stamp;


  public long getCartonSeq() {
    return cartonSeq;
  }

  public void setCartonSeq(long cartonSeq) {
    this.cartonSeq = cartonSeq;
  }


  public String getLpn() {
    return lpn;
  }

  public void setLpn(String lpn) {
    this.lpn = lpn;
  }


  public String getUcc() {
    return ucc;
  }

  public void setUcc(String ucc) {
    this.ucc = ucc;
  }


  public String getUpc() {
    return upc;
  }

  public void setUpc(String upc) {
    this.upc = upc;
  }


  public String getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }


  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }


  public String getCartonType() {
    return cartonType;
  }

  public void setCartonType(String cartonType) {
    this.cartonType = cartonType;
  }


  public String getPickType() {
    return pickType;
  }

  public void setPickType(String pickType) {
    this.pickType = pickType;
  }


  public String getReservedBy() {
    return reservedBy;
  }

  public void setReservedBy(String reservedBy) {
    this.reservedBy = reservedBy;
  }


  public long getCartSeq() {
    return cartSeq;
  }

  public void setCartSeq(long cartSeq) {
    this.cartSeq = cartSeq;
  }


  public String getCartSlot() {
    return cartSlot;
  }

  public void setCartSlot(String cartSlot) {
    this.cartSlot = cartSlot;
  }


  public long getParentCartonSeq() {
    return parentCartonSeq;
  }

  public void setParentCartonSeq(long parentCartonSeq) {
    this.parentCartonSeq = parentCartonSeq;
  }


  public String getPalletGroup() {
    return palletGroup;
  }

  public void setPalletGroup(String palletGroup) {
    this.palletGroup = palletGroup;
  }


  public long getPalletSeq() {
    return palletSeq;
  }

  public void setPalletSeq(long palletSeq) {
    this.palletSeq = palletSeq;
  }


  public String getPalletOperatorId() {
    return palletOperatorId;
  }

  public void setPalletOperatorId(String palletOperatorId) {
    this.palletOperatorId = palletOperatorId;
  }


  public double getEstWeight() {
    return estWeight;
  }

  public void setEstWeight(double estWeight) {
    this.estWeight = estWeight;
  }


  public double getEstWeightLow() {
    return estWeightLow;
  }

  public void setEstWeightLow(double estWeightLow) {
    this.estWeightLow = estWeightLow;
  }


  public double getEstWeightHigh() {
    return estWeightHigh;
  }

  public void setEstWeightHigh(double estWeightHigh) {
    this.estWeightHigh = estWeightHigh;
  }


  public double getEstLength() {
    return estLength;
  }

  public void setEstLength(double estLength) {
    this.estLength = estLength;
  }


  public double getEstWidth() {
    return estWidth;
  }

  public void setEstWidth(double estWidth) {
    this.estWidth = estWidth;
  }


  public double getEstHeight() {
    return estHeight;
  }

  public void setEstHeight(double estHeight) {
    this.estHeight = estHeight;
  }


  public double getActWeight() {
    return actWeight;
  }

  public void setActWeight(double actWeight) {
    this.actWeight = actWeight;
  }


  public double getActLength() {
    return actLength;
  }

  public void setActLength(double actLength) {
    this.actLength = actLength;
  }


  public double getActWidth() {
    return actWidth;
  }

  public void setActWidth(double actWidth) {
    this.actWidth = actWidth;
  }


  public double getActHeight() {
    return actHeight;
  }

  public void setActHeight(double actHeight) {
    this.actHeight = actHeight;
  }


  public double getNetShippingCharge() {
    return netShippingCharge;
  }

  public void setNetShippingCharge(double netShippingCharge) {
    this.netShippingCharge = netShippingCharge;
  }


  public double getListShippingCharge() {
    return listShippingCharge;
  }

  public void setListShippingCharge(double listShippingCharge) {
    this.listShippingCharge = listShippingCharge;
  }


  public long getAssigned() {
    return assigned;
  }

  public void setAssigned(long assigned) {
    this.assigned = assigned;
  }


  public long getAuditRequired() {
    return auditRequired;
  }

  public void setAuditRequired(long auditRequired) {
    this.auditRequired = auditRequired;
  }


  public long getRepackRequired() {
    return repackRequired;
  }

  public void setRepackRequired(long repackRequired) {
    this.repackRequired = repackRequired;
  }


  public long getPackException() {
    return packException;
  }

  public void setPackException(long packException) {
    this.packException = packException;
  }


  public java.sql.Timestamp getCreateStamp() {
    return createStamp;
  }

  public void setCreateStamp(java.sql.Timestamp createStamp) {
    this.createStamp = createStamp;
  }


  public java.sql.Timestamp getReleaseStamp() {
    return releaseStamp;
  }

  public void setReleaseStamp(java.sql.Timestamp releaseStamp) {
    this.releaseStamp = releaseStamp;
  }


  public java.sql.Timestamp getReadyForGeekStamp() {
    return readyForGeekStamp;
  }

  public void setReadyForGeekStamp(java.sql.Timestamp readyForGeekStamp) {
    this.readyForGeekStamp = readyForGeekStamp;
  }


  public java.sql.Timestamp getPickStartStamp() {
    return pickStartStamp;
  }

  public void setPickStartStamp(java.sql.Timestamp pickStartStamp) {
    this.pickStartStamp = pickStartStamp;
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


  public java.sql.Timestamp getAuditStamp() {
    return auditStamp;
  }

  public void setAuditStamp(java.sql.Timestamp auditStamp) {
    this.auditStamp = auditStamp;
  }


  public java.sql.Timestamp getPackStamp() {
    return packStamp;
  }

  public void setPackStamp(java.sql.Timestamp packStamp) {
    this.packStamp = packStamp;
  }


  public java.sql.Timestamp getVasStamp() {
    return vasStamp;
  }

  public void setVasStamp(java.sql.Timestamp vasStamp) {
    this.vasStamp = vasStamp;
  }


  public java.sql.Timestamp getLabelStamp() {
    return labelStamp;
  }

  public void setLabelStamp(java.sql.Timestamp labelStamp) {
    this.labelStamp = labelStamp;
  }


  public java.sql.Timestamp getShipStamp() {
    return shipStamp;
  }

  public void setShipStamp(java.sql.Timestamp shipStamp) {
    this.shipStamp = shipStamp;
  }


  public java.sql.Timestamp getPalletStamp() {
    return palletStamp;
  }

  public void setPalletStamp(java.sql.Timestamp palletStamp) {
    this.palletStamp = palletStamp;
  }


  public java.sql.Timestamp getCancelStamp() {
    return cancelStamp;
  }

  public void setCancelStamp(java.sql.Timestamp cancelStamp) {
    this.cancelStamp = cancelStamp;
  }


  public String getLastPositionLogical() {
    return lastPositionLogical;
  }

  public void setLastPositionLogical(String lastPositionLogical) {
    this.lastPositionLogical = lastPositionLogical;
  }


  public String getLastPositionPhysical() {
    return lastPositionPhysical;
  }

  public void setLastPositionPhysical(String lastPositionPhysical) {
    this.lastPositionPhysical = lastPositionPhysical;
  }


  public java.sql.Timestamp getLastPositionStamp() {
    return lastPositionStamp;
  }

  public void setLastPositionStamp(java.sql.Timestamp lastPositionStamp) {
    this.lastPositionStamp = lastPositionStamp;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }

}
