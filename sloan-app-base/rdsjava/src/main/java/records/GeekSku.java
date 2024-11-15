package records;

import java.util.List;

public class GeekSku {

    private final String warehouseCode;
    private final String skuCode;
    private final String ownerCode;
    private final String skuName;
    private final String unit;
    private final float length;
    private final float width;
    private final float height;
    private final float netWeight;

    private final String remark;
    private final String wares_type_code;
    private final String specification;
    private final String item_size;
    private final String item_color;
    private final String production_location;
    private final String item_style;

    private final List<String> barcodes;

    private GeekSku(GeekSkuBuilder builder) {

        this.warehouseCode = builder.warehouseCode;
        this.skuCode = builder.skuCode;
        this.ownerCode = builder.ownerCode;
        this.unit = builder.unit;
        this.skuName = builder.skuName;
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.netWeight = builder.netWeight;
        this.barcodes = builder.barcodes;
        this.specification = builder.specification;
        this.remark = builder.remark;
        this.wares_type_code = builder.wares_type_code;
        this.item_size = builder.item_size;
        this.item_color = builder.item_color;
        this.production_location = builder.production_location;
        this.item_style = builder.item_style;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getOwnerCode() { return ownerCode; }

    public String getSkuName() {
        return skuName;
    }

    public String getUnit() {
        return unit;
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

    public float getNetWeight() {
        return netWeight;
    }

    public String getRemark() {
        return remark;
    }

    public String getWares_type_code() {
        return wares_type_code;
    }

    public String getSpecification() {
        return specification;
    }

    public String getItem_size() {
        return item_size;
    }

    public String getItem_color() {
        return item_color;
    }

    public String getProduction_location() {
        return production_location;
    }

    public String getItem_style() {
        return item_style;
    }

    public List<String> getBarcodes() {
        return barcodes;
    }

    public static class GeekSkuBuilder {
        private final String warehouseCode;
        private final String skuCode;
        private String ownerCode;
        private String skuName;
        private String unit;
        private float length;
        private float width;
        private float height;
        private float netWeight;
        private String remark;


        private String wares_type_code;
        private String specification;
        private String item_size;
        private String item_color;
        private String production_location;
        private String item_style;
        private List<String> barcodes;

        public GeekSkuBuilder(String warehouseCode, String skuCode) {
            this.warehouseCode = warehouseCode;
            this.skuCode = skuCode;
        }

        public GeekSku build() {
            return new GeekSku(this);
        }

        public GeekSkuBuilder setSkuName(String skuName) {
            this.skuName = skuName;
            return this;
        }

        public GeekSkuBuilder setOwnerCode(String ownerCode) {
            this.ownerCode = ownerCode;
            return this;
        }

        public GeekSkuBuilder setUnit(String unit) {
            this.unit = unit;
            return this;
        }

        public GeekSkuBuilder setLength(float length) {
            this.length = length;
            return this;
        }

        public GeekSkuBuilder setWidth(float width) {
            this.width = width;
            return this;
        }

        public GeekSkuBuilder setHeight(float height) {
            this.height = height;
            return this;
        }

        public GeekSkuBuilder setNetWeight(float netWeight) {
            this.netWeight = netWeight;
            return this;
        }

        public GeekSkuBuilder setBarcodes(List<String> barcodes) {
            this.barcodes = barcodes;
            return this;
        }


        public GeekSkuBuilder setRemark(String remark) {
            this.remark = remark;
            return this;
        }

        public GeekSkuBuilder setWares_type_code(String wares_type_code) {
            this.wares_type_code = wares_type_code;
            return this;
        }

        public GeekSkuBuilder setSpecification(String specification) {
            this.specification = specification;
            return this;
        }

        public GeekSkuBuilder setItem_size(String item_size) {
            this.item_size = item_size;
            return this;
        }

        public GeekSkuBuilder setItem_color(String item_color) {
            this.item_color = item_color;
            return this;
        }

        public GeekSkuBuilder setProduction_location(String production_location) {
            this.production_location = production_location;
            return this;
        }

        public GeekSkuBuilder setItem_style(String item_style) {
            this.item_style = item_style;
            return this;
        }
    }
}
