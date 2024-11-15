package records;

import java.time.Instant;

public class CustSku {

    private final String sku;
    private final String uom;
    private final String description;
    private final String baseUom;
    private final float quantityBaseUom;
    private final String barcode;
    private final float weight;
    private final float length;
    private final float width;
    private final float height;
    private final String nestingGroup;
    private final String nestingDimension;
    private final float nestingValue;
    private final String sizeCode;
    private final String skuType;
    private final String manufacturer;
    private final float price;
    private final boolean conveyable;
    private final boolean fragile;
    private final String hazmat;
    private final boolean shippable;
    private final boolean shipAlone;
    private final boolean bagEligible;
    private final String velocity;
    private final String imageUrl;
    private final String location;
    private final Instant downloadStamp;
    private final int downloadReference;
    int quantityShelfPack;
    private final String upc2;
    private final String upc3;
    private final String gtin8;
    private final String gtin13;
    int cubicDivisorInt;
    private final String geekToteCode;
    int geekToteDesiredQuantity;
    int geekPrimaryLocationCapacity;
    private final String buyingDepartmentCode;
    private final String qroFlag;
    private final float upcScanProbability;
    private final int setToGeek;
    private final int ackByGeek;


    private final Instant stamp;
    public String getSku() {
        return sku;
    }

    public String getUom() {
        return uom;
    }

    public String getDescription() {
        return description;
    }

    public String getBaseUom() {
        return baseUom;
    }

    public float getQuantityBaseUom() {
        return quantityBaseUom;
    }

    public String getBarcode() {
        return barcode;
    }

    public float getWeight() {
        return weight;
    }

    public float getLength() {
        return length;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getNestingGroup() {
        return nestingGroup;
    }

    public String getNestingDimension() {
        return nestingDimension;
    }

    public float getNestingValue() {
        return nestingValue;
    }

    public String getSizeCode() {
        return sizeCode;
    }

    public String getSkuType() {
        return skuType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public float getPrice() {
        return price;
    }

    public boolean isConveyable() {
        return conveyable;
    }

    public boolean isFragile() {
        return fragile;
    }

    public String getHazmat() {
        return hazmat;
    }

    public boolean isShippable() {
        return shippable;
    }

    public boolean isShipAlone() {
        return shipAlone;
    }

    public boolean isBagEligible() {
        return bagEligible;
    }

    public String getVelocity() {
        return velocity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public Instant getDownloadStamp() {
        return downloadStamp;
    }

    public int getDownloadReference() {
        return downloadReference;
    }

    public int getQuantityShelfPack() {
        return quantityShelfPack;
    }

    public String getUpc2() {
        return upc2;
    }

    public String getUpc3() {
        return upc3;
    }

    public String getGtin8() {
        return gtin8;
    }
    public String getGtin13() {
        return gtin13;
    }

    public int getCubicDivisorInt() {
        return cubicDivisorInt;
    }

    public String getGeekToteCode() {
        return geekToteCode;
    }

    public int getGeekToteDesiredQuantity() {
        return geekToteDesiredQuantity;
    }

    public int getGeekPrimaryLocationCapacity() {
        return geekPrimaryLocationCapacity;
    }

    public String getBuyingDepartmentCode() {
        return buyingDepartmentCode;
    }

    public String getQroFlag() {
        return qroFlag;
    }

    public float getUpcScanProbability() {
        return upcScanProbability;
    }

    public Instant getStamp() {
        return stamp;
    }

    public int getSetToGeek(){
        return setToGeek;
    }

    public int getAckByGeek() {
        return ackByGeek;
    }

    private CustSku(CustSkuBuilder builder) {
        this.sku = builder.sku;
        this.uom = builder.uom;
        this.description = builder.description;
        this.baseUom = builder.baseUom;
        this.quantityBaseUom = builder.quantityBaseUom;
        this.barcode = builder.barcode;
        this.weight = builder.weight;
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.nestingGroup = builder.nestingGroup;
        this.nestingDimension = builder.nestingDimension;
        this.nestingValue = builder.nestingValue;
        this.sizeCode = builder.sizeCode;
        this.skuType = builder.skuType;
        this.manufacturer = builder.manufacturer;
        this.price = builder.price;
        this.conveyable = builder.conveyable;
        this.fragile = builder.fragile;
        this.hazmat = builder.hazmat;
        this.shippable = builder.shippable;
        this.shipAlone = builder.shipAlone;
        this.bagEligible = builder.bagEligible;
        this.velocity = builder.velocity;
        this.imageUrl = builder.imageUrl;
        this.location = builder.location;
        this.downloadStamp = builder.downloadStamp;
        this.downloadReference = builder.downloadReference;
        this.quantityShelfPack = builder.quantityShelfPack;
        this.upc2 = builder.upc2;
        this.upc3 = builder.upc3;
        this.gtin8 = builder.gtin8;
        this.gtin13 = builder.gtin13;
        this.cubicDivisorInt = builder.cubicDivisorInt;
        this.geekToteCode = builder.geekToteCode;
        this.geekToteDesiredQuantity = builder.geekToteDesiredQuantity;
        this.geekPrimaryLocationCapacity = builder.geekPrimaryLocationCapacity;
        this.buyingDepartmentCode = builder.buyingDepartmentCode;
        this.qroFlag = builder.qroFlag;
        this.upcScanProbability = builder.upcScanProbability;
        this.stamp = builder.stamp;
        this.setToGeek = builder.sentToGeek;
        this.ackByGeek = builder.ackByGeek;
    }

    public static class CustSkuBuilder {

        private final String sku;
        private final String uom;
        private String description;
        private String baseUom;
        private float quantityBaseUom;
        private String barcode;
        private float weight;
        private float length;
        private float width;
        private float height;
        private String nestingGroup;
        private String nestingDimension;
        private float nestingValue;
        private String sizeCode;
        private String skuType;
        private String manufacturer;
        private float price;
        private boolean conveyable;
        private boolean fragile;
        private String hazmat;
        private boolean shippable;
        private boolean shipAlone;
        private boolean bagEligible;
        private String velocity;
        private String imageUrl;
        private String location;
        private Instant downloadStamp;
        private int downloadReference;
        int quantityShelfPack;
        private String upc2;
        private String upc3;
        private String gtin8;
        private String gtin13;
        int cubicDivisorInt;
        private String geekToteCode;
        int geekToteDesiredQuantity;
        int geekPrimaryLocationCapacity;
        private String buyingDepartmentCode;
        private String qroFlag;
        private float upcScanProbability;
        private Instant stamp;
        private int sentToGeek;
        private int ackByGeek;

        public CustSkuBuilder(String sku, String uom) {
            this.sku = sku;
            this.uom = uom;
        }

        public CustSku build() {
            return new CustSku(this);
        }

        public CustSkuBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public CustSkuBuilder setBaseUom(String baseUom) {
            this.baseUom = baseUom;
            return this;
        }

        public CustSkuBuilder setQuantityBaseUom(float quantityBaseUom) {
            this.quantityBaseUom = quantityBaseUom;
            return this;
        }

        public CustSkuBuilder setBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public CustSkuBuilder setWeight(float weight) {
            this.weight = weight;
            return this;
        }

        public CustSkuBuilder setLength(float length) {
            this.length = length;
            return this;
        }

        public CustSkuBuilder setWidth(float width) {
            this.width = width;
            return this;
        }

        public CustSkuBuilder setHeight(float height) {
            this.height = height;
            return this;
        }

        public CustSkuBuilder setNestingGroup(String nestingGroup) {
            this.nestingGroup = nestingGroup;
            return this;
        }

        public CustSkuBuilder setNestingDimension(String nestingDimension) {
            this.nestingDimension = nestingDimension;
            return this;
        }

        public CustSkuBuilder setNestingValue(float nestingValue) {
            this.nestingValue = nestingValue;
            return this;
        }

        public CustSkuBuilder setSizeCode(String sizeCode) {
            this.sizeCode = sizeCode;
            return this;
        }

        public CustSkuBuilder setSkuType(String skuType) {
            this.skuType = skuType;
            return this;
        }

        public CustSkuBuilder setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public CustSkuBuilder setPrice(float price) {
            this.price = price;
            return this;
        }

        public CustSkuBuilder setConveyable(boolean conveyable) {
            this.conveyable = conveyable;
            return this;
        }

        public CustSkuBuilder setFragile(boolean fragile) {
            this.fragile = fragile;
            return this;
        }

        public CustSkuBuilder setHazmat(String hazmat) {
            this.hazmat = hazmat;
            return this;
        }

        public CustSkuBuilder setShippable(boolean shippable) {
            this.shippable = shippable;
            return this;
        }

        public CustSkuBuilder setShipAlone(boolean shipAlone) {
            this.shipAlone = shipAlone;
            return this;
        }

        public CustSkuBuilder setBagEligible(boolean bagEligible) {
            this.bagEligible = bagEligible;
            return this;
        }

        public CustSkuBuilder setVelocity(String velocity) {
            this.velocity = velocity;
            return this;
        }

        public CustSkuBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public CustSkuBuilder setLocation(String location) {
            this.location = location;
            return this;
        }

        public CustSkuBuilder setDownloadStamp(Instant downloadStamp) {
            this.downloadStamp = downloadStamp;
            return this;
        }

        public CustSkuBuilder setDownloadReference(int downloadReference) {
            this.downloadReference = downloadReference;
            return this;
        }

        public CustSkuBuilder setQuantityShelfPack(int quantityShelfPack) {
            this.quantityShelfPack = quantityShelfPack;
            return this;
        }

        public CustSkuBuilder setUpc2(String upc2) {
            this.upc2 = upc2;
            return this;
        }

        public CustSkuBuilder setUpc3(String upc3) {
            this.upc3 = upc3;
            return this;
        }

        public CustSkuBuilder setGtin8(String gtin8) {
            this.gtin8 = gtin8;
            return this;
        }

        public CustSkuBuilder setGtin13(String gtin13) {
            this.gtin13 = gtin13;
            return this;
        }

        public CustSkuBuilder setCubicDivisor(int cubicDivisorInt) {
            this.cubicDivisorInt = cubicDivisorInt;
            return this;
        }

        public CustSkuBuilder setGeekToteCode(String geekToteCode) {
            this.geekToteCode = geekToteCode;
            return this;
        }

        public CustSkuBuilder setGeekToteDesiredQuantity(int geekToteDesiredQuantity) {
            this.geekToteDesiredQuantity = geekToteDesiredQuantity;
            return this;
        }

        public CustSkuBuilder setGeekPrimaryLocationCapacity(int geekPrimaryLocationCapacity) {
            this.geekPrimaryLocationCapacity = geekPrimaryLocationCapacity;
            return this;
        }

        public CustSkuBuilder setBuyingDepartmentCode(String buyingDepartmentCode) {
            this.buyingDepartmentCode = buyingDepartmentCode;
            return this;
        }

        public CustSkuBuilder setQroFlag(String qroFlag) {
            this.qroFlag = qroFlag;
            return this;
        }

        public CustSkuBuilder setUpcScanProbability(float upcScanProbability) {
            this.upcScanProbability = upcScanProbability;
            return this;
        }

        public CustSkuBuilder getSetToGeek(int sentToGeek) {
            this.sentToGeek = sentToGeek;
            return this;
        }

        public CustSkuBuilder getAckByGeek(int ackByGeek) {
            this.ackByGeek = ackByGeek;
            return this;
        }
    }
}
