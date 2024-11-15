package records;


import java.util.List;

public class GeekPickOrder {

  private long seq;
  private String warehouseCode;
  private String outOrderCode;
  private String ownerCode;
  private String outWaveCode;
  private String origNote;
  private long orderType;
  private long pickingType;
  private long isWaiting;
  private long isAllowLack;
  private long isAutoPushWall;
  private long canMerge;
  private long designatedContainerType;
  private long isAllowSplit;
  private long priority;
  private java.sql.Timestamp creationDate;
  private java.sql.Timestamp expectedFinishDate;
  private long infFunction;
  private long printType;
  private String waybillCode;
  private String printContent;
  private long carrierType;
  private String carrierCode;
  private String carrierName;
  private String carrierLineCode;
  private String carrierLineName;
  private String consignorProvince;
  private String consignorCity;
  private String consignorDistrict;
  private String consignorAddress;
  private String consignorZipCode;
  private String consignor;
  private String consignorPhone;
  private String consignorTel;
  private String consignmentCompany;
  private String consignorRemark;
  private String consigneeProvince;
  private String consigneeCity;
  private String consigneeDistrict;
  private String consigneeAddress;
  private String consigneeZipCode;
  private String consigneeCode;
  private String consignee;
  private String consigneePhone;
  private String consigneeTel;
  private String consigneeRemark;
  private String origPlatformCode;
  private String origPlatformName;
  private String shopCode;
  private String shopName;
  private String packageTypeCode;
  private String packageTypeName;
  private String followOperation;
  private java.sql.Timestamp payTime;
  private double paymentFee;
  private String realName;
  private String identityCard;
  private long isNeedWaybill;
  private long payMethod;
  private long isReverse;
  private double totalFee;
  private double skuFee;
  private double discountFee;
  private double receivableFee;
  private long expressType;
  private String processed;

  public List<GeekPutawayOrderSku> getSkuList() {
    return skuList;
  }

  public void setSkuList(List<GeekPutawayOrderSku> skuList) {
    this.skuList = skuList;
  }

  private List<GeekPutawayOrderSku> skuList;
  private java.sql.Timestamp stamp;


  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }


  public String getWarehouseCode() {
    return warehouseCode;
  }

  public void setWarehouseCode(String warehouseCode) {
    this.warehouseCode = warehouseCode;
  }


  public String getOutOrderCode() {
    return outOrderCode;
  }

  public void setOutOrderCode(String outOrderCode) {
    this.outOrderCode = outOrderCode;
  }


  public String getOwnerCode() {
    return ownerCode;
  }

  public void setOwnerCode(String ownerCode) {
    this.ownerCode = ownerCode;
  }


  public String getOutWaveCode() {
    return outWaveCode;
  }

  public void setOutWaveCode(String outWaveCode) {
    this.outWaveCode = outWaveCode;
  }


  public String getOrigNote() {
    return origNote;
  }

  public void setOrigNote(String origNote) {
    this.origNote = origNote;
  }


  public long getOrderType() {
    return orderType;
  }

  public void setOrderType(long orderType) {
    this.orderType = orderType;
  }


  public long getPickingType() {
    return pickingType;
  }

  public void setPickingType(long pickingType) {
    this.pickingType = pickingType;
  }


  public long getIsWaiting() {
    return isWaiting;
  }

  public void setIsWaiting(long isWaiting) {
    this.isWaiting = isWaiting;
  }


  public long getIsAllowLack() {
    return isAllowLack;
  }

  public void setIsAllowLack(long isAllowLack) {
    this.isAllowLack = isAllowLack;
  }


  public long getIsAutoPushWall() {
    return isAutoPushWall;
  }

  public void setIsAutoPushWall(long isAutoPushWall) {
    this.isAutoPushWall = isAutoPushWall;
  }


  public long getCanMerge() {
    return canMerge;
  }

  public void setCanMerge(long canMerge) {
    this.canMerge = canMerge;
  }


  public long getDesignatedContainerType() {
    return designatedContainerType;
  }

  public void setDesignatedContainerType(long designatedContainerType) {
    this.designatedContainerType = designatedContainerType;
  }


  public long getIsAllowSplit() {
    return isAllowSplit;
  }

  public void setIsAllowSplit(long isAllowSplit) {
    this.isAllowSplit = isAllowSplit;
  }


  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }


  public java.sql.Timestamp getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(java.sql.Timestamp creationDate) {
    this.creationDate = creationDate;
  }


  public java.sql.Timestamp getExpectedFinishDate() {
    return expectedFinishDate;
  }

  public void setExpectedFinishDate(java.sql.Timestamp expectedFinishDate) {
    this.expectedFinishDate = expectedFinishDate;
  }


  public long getInfFunction() {
    return infFunction;
  }

  public void setInfFunction(long infFunction) {
    this.infFunction = infFunction;
  }


  public long getPrintType() {
    return printType;
  }

  public void setPrintType(long printType) {
    this.printType = printType;
  }


  public String getWaybillCode() {
    return waybillCode;
  }

  public void setWaybillCode(String waybillCode) {
    this.waybillCode = waybillCode;
  }


  public String getPrintContent() {
    return printContent;
  }

  public void setPrintContent(String printContent) {
    this.printContent = printContent;
  }


  public long getCarrierType() {
    return carrierType;
  }

  public void setCarrierType(long carrierType) {
    this.carrierType = carrierType;
  }


  public String getCarrierCode() {
    return carrierCode;
  }

  public void setCarrierCode(String carrierCode) {
    this.carrierCode = carrierCode;
  }


  public String getCarrierName() {
    return carrierName;
  }

  public void setCarrierName(String carrierName) {
    this.carrierName = carrierName;
  }


  public String getCarrierLineCode() {
    return carrierLineCode;
  }

  public void setCarrierLineCode(String carrierLineCode) {
    this.carrierLineCode = carrierLineCode;
  }


  public String getCarrierLineName() {
    return carrierLineName;
  }

  public void setCarrierLineName(String carrierLineName) {
    this.carrierLineName = carrierLineName;
  }


  public String getConsignorProvince() {
    return consignorProvince;
  }

  public void setConsignorProvince(String consignorProvince) {
    this.consignorProvince = consignorProvince;
  }


  public String getConsignorCity() {
    return consignorCity;
  }

  public void setConsignorCity(String consignorCity) {
    this.consignorCity = consignorCity;
  }


  public String getConsignorDistrict() {
    return consignorDistrict;
  }

  public void setConsignorDistrict(String consignorDistrict) {
    this.consignorDistrict = consignorDistrict;
  }


  public String getConsignorAddress() {
    return consignorAddress;
  }

  public void setConsignorAddress(String consignorAddress) {
    this.consignorAddress = consignorAddress;
  }


  public String getConsignorZipCode() {
    return consignorZipCode;
  }

  public void setConsignorZipCode(String consignorZipCode) {
    this.consignorZipCode = consignorZipCode;
  }


  public String getConsignor() {
    return consignor;
  }

  public void setConsignor(String consignor) {
    this.consignor = consignor;
  }


  public String getConsignorPhone() {
    return consignorPhone;
  }

  public void setConsignorPhone(String consignorPhone) {
    this.consignorPhone = consignorPhone;
  }


  public String getConsignorTel() {
    return consignorTel;
  }

  public void setConsignorTel(String consignorTel) {
    this.consignorTel = consignorTel;
  }


  public String getConsignmentCompany() {
    return consignmentCompany;
  }

  public void setConsignmentCompany(String consignmentCompany) {
    this.consignmentCompany = consignmentCompany;
  }


  public String getConsignorRemark() {
    return consignorRemark;
  }

  public void setConsignorRemark(String consignorRemark) {
    this.consignorRemark = consignorRemark;
  }


  public String getConsigneeProvince() {
    return consigneeProvince;
  }

  public void setConsigneeProvince(String consigneeProvince) {
    this.consigneeProvince = consigneeProvince;
  }


  public String getConsigneeCity() {
    return consigneeCity;
  }

  public void setConsigneeCity(String consigneeCity) {
    this.consigneeCity = consigneeCity;
  }


  public String getConsigneeDistrict() {
    return consigneeDistrict;
  }

  public void setConsigneeDistrict(String consigneeDistrict) {
    this.consigneeDistrict = consigneeDistrict;
  }


  public String getConsigneeAddress() {
    return consigneeAddress;
  }

  public void setConsigneeAddress(String consigneeAddress) {
    this.consigneeAddress = consigneeAddress;
  }


  public String getConsigneeZipCode() {
    return consigneeZipCode;
  }

  public void setConsigneeZipCode(String consigneeZipCode) {
    this.consigneeZipCode = consigneeZipCode;
  }


  public String getConsigneeCode() {
    return consigneeCode;
  }

  public void setConsigneeCode(String consigneeCode) {
    this.consigneeCode = consigneeCode;
  }


  public String getConsignee() {
    return consignee;
  }

  public void setConsignee(String consignee) {
    this.consignee = consignee;
  }


  public String getConsigneePhone() {
    return consigneePhone;
  }

  public void setConsigneePhone(String consigneePhone) {
    this.consigneePhone = consigneePhone;
  }


  public String getConsigneeTel() {
    return consigneeTel;
  }

  public void setConsigneeTel(String consigneeTel) {
    this.consigneeTel = consigneeTel;
  }


  public String getConsigneeRemark() {
    return consigneeRemark;
  }

  public void setConsigneeRemark(String consigneeRemark) {
    this.consigneeRemark = consigneeRemark;
  }


  public String getOrigPlatformCode() {
    return origPlatformCode;
  }

  public void setOrigPlatformCode(String origPlatformCode) {
    this.origPlatformCode = origPlatformCode;
  }


  public String getOrigPlatformName() {
    return origPlatformName;
  }

  public void setOrigPlatformName(String origPlatformName) {
    this.origPlatformName = origPlatformName;
  }


  public String getShopCode() {
    return shopCode;
  }

  public void setShopCode(String shopCode) {
    this.shopCode = shopCode;
  }


  public String getShopName() {
    return shopName;
  }

  public void setShopName(String shopName) {
    this.shopName = shopName;
  }


  public String getPackageTypeCode() {
    return packageTypeCode;
  }

  public void setPackageTypeCode(String packageTypeCode) {
    this.packageTypeCode = packageTypeCode;
  }


  public String getPackageTypeName() {
    return packageTypeName;
  }

  public void setPackageTypeName(String packageTypeName) {
    this.packageTypeName = packageTypeName;
  }


  public String getFollowOperation() {
    return followOperation;
  }

  public void setFollowOperation(String followOperation) {
    this.followOperation = followOperation;
  }


  public java.sql.Timestamp getPayTime() {
    return payTime;
  }

  public void setPayTime(java.sql.Timestamp payTime) {
    this.payTime = payTime;
  }


  public double getPaymentFee() {
    return paymentFee;
  }

  public void setPaymentFee(double paymentFee) {
    this.paymentFee = paymentFee;
  }


  public String getRealName() {
    return realName;
  }

  public void setRealName(String realName) {
    this.realName = realName;
  }


  public String getIdentityCard() {
    return identityCard;
  }

  public void setIdentityCard(String identityCard) {
    this.identityCard = identityCard;
  }


  public long getIsNeedWaybill() {
    return isNeedWaybill;
  }

  public void setIsNeedWaybill(long isNeedWaybill) {
    this.isNeedWaybill = isNeedWaybill;
  }


  public long getPayMethod() {
    return payMethod;
  }

  public void setPayMethod(long payMethod) {
    this.payMethod = payMethod;
  }


  public long getIsReverse() {
    return isReverse;
  }

  public void setIsReverse(long isReverse) {
    this.isReverse = isReverse;
  }


  public double getTotalFee() {
    return totalFee;
  }

  public void setTotalFee(double totalFee) {
    this.totalFee = totalFee;
  }


  public double getSkuFee() {
    return skuFee;
  }

  public void setSkuFee(double skuFee) {
    this.skuFee = skuFee;
  }


  public double getDiscountFee() {
    return discountFee;
  }

  public void setDiscountFee(double discountFee) {
    this.discountFee = discountFee;
  }


  public double getReceivableFee() {
    return receivableFee;
  }

  public void setReceivableFee(double receivableFee) {
    this.receivableFee = receivableFee;
  }


  public long getExpressType() {
    return expressType;
  }

  public void setExpressType(long expressType) {
    this.expressType = expressType;
  }


  public String getProcessed() {
    return processed;
  }

  public void setProcessed(String processed) {
    this.processed = processed;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }

}
