package records;

public class CustPutawayOrder {

    private final String putawayOrderCode;
    private final String palletCode;
    private final int putawayType;
    private final String sku;
    private final String uom;
    private final String buyingDepartmentCode;
    private final int quantity;
    private final int shelfQuantity;

    public String getPutawayOrderCode() {
        return putawayOrderCode;
    }

    public String getPalletCode() {
        return palletCode;
    }

    public int getPutawayType() {
        return putawayType;
    }

    public String getSku() {
        return sku;
    }

    public String getUom() {
        return uom;
    }

    public String getBuyingDepartmentCode() {
        return buyingDepartmentCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getShelfQuantity() {
        return shelfQuantity;
    }

    private CustPutawayOrder(CustPutawayOrderBuilder builder) {
        this.putawayOrderCode = builder.putawayOrderCode;
        this.palletCode = builder.palletCode;
        this.putawayType = builder.putawayType;
        this.sku = builder.sku;
        this.uom = builder.uom;
        this.buyingDepartmentCode = builder.buyingDepartmentCode;
        this.quantity = builder.quantity;
        this.shelfQuantity = builder.shelfQuantity;
    }

    public static class CustPutawayOrderBuilder {

        private final String putawayOrderCode;
        private String palletCode;
        private int putawayType;
        private String sku;
        private String uom;
        private String buyingDepartmentCode;
        private int quantity;
        private int shelfQuantity;

        public CustPutawayOrderBuilder setPalletCode(String palletCode) {
            this.palletCode = palletCode;
            return this;
        }

        public CustPutawayOrderBuilder setPutawayType(int putawayType) {
            this.putawayType = putawayType;
            return this;
        }

        public CustPutawayOrderBuilder setSku(String sku) {
            this.sku = sku;
            return this;
        }

        public CustPutawayOrderBuilder setUom(String uom) {
            this.uom = uom;
            return this;
        }

        public CustPutawayOrderBuilder setBuyingDepartmentCode(String buyingDepartmentCode) {
            this.buyingDepartmentCode = buyingDepartmentCode;
            return this;
        }

        public CustPutawayOrderBuilder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public CustPutawayOrderBuilder setShelfQuantity(int shelfQuantity) {
            this.shelfQuantity = shelfQuantity;
            return this;
        }

        public CustPutawayOrderBuilder(String putawayOrderCode) {
            this.putawayOrderCode = putawayOrderCode;
        }

        public CustPutawayOrder build() {
            return new CustPutawayOrder(this);
        }

     }
}
