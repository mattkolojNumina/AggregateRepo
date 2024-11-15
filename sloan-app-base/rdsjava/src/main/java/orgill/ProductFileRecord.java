package sloane;

import dao.CustSkusDAO;
import dao.DataAccessException;
import host.FileRecord;
import host.StringUtils;
import records.CustSku;

import java.util.ArrayList;
import java.util.List;

import static rds.RDSUtil.trace;


class ProductFileRecord implements FileRecord {
    private final String partNumber;
    private final String salesUom;
    private final String description;
    private final String upc;
    private final String length;
    private final String width;
    private final String height;
    private final String weight;
    private final String factoryNumber; // manufacturer (ignored by rds)
    private final String geekToteCode;
    private final String geekToteDesiredQty;
    private final String geekPrimaryLocCapacity;
    private final String cubicDivisor;
    private final String qroFlag;
    private final String upc2;
    private final String upc3;
    private final String gtin8;
    private final String gtin13;
    private final String buyingDepartmentCode;
    private final String qtyShelfPack;

    public ProductFileRecord(String[] fields) {

        this.partNumber = fields[0].trim();
        this.factoryNumber = fields[1].trim();
        this.description = fields[2].trim();

        this.upc = fields[3].trim();
        this.qroFlag = fields[4].trim();
        this.upc2 = fields[5].trim();
        this.upc3 = fields[6].trim();
        this.buyingDepartmentCode = fields[7].trim();

        this.length = fields[8].trim();
        this.width = fields[9].trim();
        this.height = fields[10].trim();
        this.cubicDivisor = fields[11].trim();
        this.weight = fields[12].trim();

        this.geekToteCode = fields[13].trim();
        this.geekToteDesiredQty = fields[14].trim();
        this.geekPrimaryLocCapacity = fields[15].trim();

        this.salesUom = fields[16].trim();
        this.qtyShelfPack = fields[17].trim();// shelf pack
        this.gtin8 = fields[18].trim();
        this.gtin13 = fields[19].trim();

    }

    public List<String> validate() {

        List<String> validationErrors = new ArrayList<>();

        if (partNumber == null || partNumber.isBlank()) {
            validationErrors.add("Part number is empty.");
        } else if (partNumber.length() > 25) {
            validationErrors.add("Part Number is greater than 25 characters.");
        } else if (!StringUtils.isNumeric(partNumber)) {
            validationErrors.add("Username is not numeric.");
        }

        if (description == null || description.isBlank()) {
            validationErrors.add("Description is empty.");
        } else if (description.length() > 100) {
            validationErrors.add("Description is greater than 100 characters.");
        }

        if (upc == null || upc.isBlank()) {
            validationErrors.add("upc is empty.");
        } else if (upc.length() > 14) {
            validationErrors.add("upc is greater than 14 characters.");
        } else if (!StringUtils.isNumeric(upc)) {
            validationErrors.add("upc is not numeric.");
        }

        if (upc2 == null || upc2.isBlank()) {
            validationErrors.add("upc2 is empty.");
        } else if (upc2.length() > 14) {
            validationErrors.add("upc2 is greater than 14 characters.");
        } else if (!StringUtils.isNumeric(upc2)) {
            validationErrors.add("upc2 is not numeric.");
        }

        if (upc3 == null || upc3.isBlank()) {
            validationErrors.add("upc3 is empty.");
        } else if (upc3.length() > 14) {
            validationErrors.add("upc3 is greater than 14 characters.");
        } else if (!StringUtils.isNumeric(upc3)) {
            validationErrors.add("upc3 is not numeric.");
        }

        if (buyingDepartmentCode == null || buyingDepartmentCode.isBlank()) {
            validationErrors.add("buyingDepartment code is empty.");
        } else if (buyingDepartmentCode.length() > 1) {
            validationErrors.add("buyingDepartment code is greater than 1 characters.");
        }

        if (length == null || length.isBlank()) {
            validationErrors.add("length is empty.");
        } else if (length.length() > 8) {
            validationErrors.add("length is greater than 8 characters.");
        }

        if (width == null || width.isBlank()) {
            validationErrors.add("width is empty.");
        } else if (width.length() > 8) {
            validationErrors.add("width is greater than 8 characters.");
        }

        if (height == null || height.isBlank()) {
            validationErrors.add("height is empty.");
        } else if (height.length() > 8) {
            validationErrors.add("height is greater than 8 characters.");
        }

        if (cubicDivisor == null || cubicDivisor.isBlank()) {
            validationErrors.add("cubicDivisor is empty.");
        } else if (cubicDivisor.length() > 5) {
            validationErrors.add("cubicDivisor is greater than 5 characters.");
        } else if (!StringUtils.isNumeric(cubicDivisor)) {
            validationErrors.add("cubicDivisor is not numeric.");
        }

        if (weight == null || weight.isBlank()) {
            validationErrors.add("weight is empty.");
        } else if (weight.length() > 10) {
            validationErrors.add("weight is greater than 10 characters.");
        }

        if (geekToteCode == null) {
            validationErrors.add("geekToteCode is empty.");
        } else if (geekToteCode.length() > 1) {
            validationErrors.add("geekToteCode is greater than 1 characters.");
        }

        if (geekToteDesiredQty == null || geekToteDesiredQty.isBlank()) {
            validationErrors.add("geekToteDesiredQty is empty.");
        } else if (geekToteDesiredQty.length() > 7) {
            validationErrors.add("geekToteDesiredQty is greater than 7 characters.");
        } else if (!StringUtils.isNumeric(geekToteDesiredQty)) {
            validationErrors.add("geekToteDesiredQty is not numeric.");
        }

        if (geekPrimaryLocCapacity == null || geekPrimaryLocCapacity.isBlank()) {
            validationErrors.add("geekPrimaryLocCapacity is empty.");
        } else if (geekPrimaryLocCapacity.length() > 7) {
            validationErrors.add("geekPrimaryLocCapacity is greater than 7 characters.");
        } else if (!StringUtils.isNumeric(geekPrimaryLocCapacity)) {
            validationErrors.add("geekPrimaryLocCapacity is not numeric.");
        }

        if (salesUom == null || salesUom.isBlank()) {
            validationErrors.add("salesUom is empty.");
        } else if (salesUom.length() > 5) {
            validationErrors.add("salesUom is greater than 5 characters.");
        }

        if (qtyShelfPack == null || qtyShelfPack.isBlank()) {
            validationErrors.add("qtyShelfPack is empty.");
        } else if (qtyShelfPack.length() > 5) {
            validationErrors.add("qtyShelfPack is greater than 5 characters.");
        } else if (!StringUtils.isNumeric(qtyShelfPack)) {
            validationErrors.add("qtyShelfPack is not numeric.");
        }

        if (gtin8 == null || gtin8.isBlank()) {
            validationErrors.add("gtin8 is empty.");
        } else if (gtin8.length() > 8) {
            validationErrors.add("gtin8 is greater than 8 characters.");
        } else if (!StringUtils.isNumeric(gtin8)) {
            validationErrors.add("gtin8 is not numeric.");
        }

        if (gtin13 == null || gtin13.isBlank()) {
            validationErrors.add("gtin13 is empty.");
        } else if (gtin13.length() > 13) {
            validationErrors.add("gtin13 is greater than 13 characters.");
        } else if (!StringUtils.isNumeric(gtin13)) {
            validationErrors.add("gtin13 is not numeric.");
        }
        return validationErrors;
    }

    public void persist() throws DataAccessException {
        CustSkusDAO dao = new CustSkusDAO();
        CustSku sku = new CustSku.CustSkuBuilder(this.partNumber, this.salesUom)
                .setDescription(this.description)
                .setBarcode(this.upc)
                .setQroFlag(this.qroFlag)
                .setUpc2(this.upc2)
                .setUpc3(this.upc3)
                .setBuyingDepartmentCode(this.buyingDepartmentCode)
                .setLength(Float.parseFloat(this.length))
                .setWidth(Float.parseFloat(this.width))
                .setHeight(Float.parseFloat(this.height))
                .setWeight(Float.parseFloat(this.weight))
                .setCubicDivisor(Integer.parseInt(this.cubicDivisor))
                .setGeekToteCode(this.geekToteCode)
                .setGeekToteDesiredQuantity(Integer.parseInt(this.geekToteDesiredQty))
                .setGeekPrimaryLocationCapacity(Integer.parseInt(this.geekPrimaryLocCapacity))
                .setQuantityShelfPack(Integer.parseInt(this.qtyShelfPack))
                .setGtin8(this.gtin8)
                .setGtin13(this.gtin13)
                .build();

        int rowsChanged = dao.save(sku);

        if(rowsChanged == 1 || rowsChanged == 2){
            dao.updateSentToGeek( this.partNumber, this.salesUom, 0);
            dao.updateAckByGeek( this.partNumber, this.salesUom, 0);
            trace("changes for sku [%s] uom [%s]",this.partNumber, this.salesUom);
        }else{
            trace("no change for sku [%s] uom [%s]",this.partNumber, this.salesUom);
        }
    }
}
