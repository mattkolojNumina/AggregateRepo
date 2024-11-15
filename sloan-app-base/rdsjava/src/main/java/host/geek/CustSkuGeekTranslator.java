package host.geek;

import dao.CustSkusDAO;
import dao.DataAccessException;
import dao.GeekSkuDAO;
import records.CustSku;
import records.GeekSku;

import java.util.*;

import static sloane.SloaneConstants.geekOwnerCode;
import static sloane.SloaneConstants.geekWarehouseCode;
import static rds.RDSLog.alert;
import static rds.RDSLog.inform;

public class CustSkuGeekTranslator implements GeekTranslator {

    private static final String emptyBarcode = "00000000000000";

    CustSkusDAO custSkusDAO = new CustSkusDAO();
    GeekSkuDAO geekSkuDAO = new GeekSkuDAO();

    public void moveToGeek() {
        
        try {
            List<CustSku> skuList = custSkusDAO.getAllToBeSentToGeek();
            inform("[%d] skus found.", skuList.size());

            for(CustSku sku: skuList) {

                // eliminate barcodes that are invalid (empty)
                // use Set to ensure uniqueness of barcode list
                Set<String> barcodes = new HashSet<>(3);
                if (!emptyBarcode.equals(sku.getBarcode())) {
                    barcodes.add(sku.getBarcode());
                }
                if (!emptyBarcode.equals(sku.getUpc2())) {
                    barcodes.add(sku.getUpc2());
                }
                if (!emptyBarcode.equals(sku.getUpc3())) {
                    barcodes.add(sku.getUpc3());
                }

                GeekSku geekSku = new GeekSku.GeekSkuBuilder(geekWarehouseCode, sku.getSku())
                        .setOwnerCode(geekOwnerCode)
                        .setUnit(sku.getUom())
                        .setSkuName(sku.getDescription())
                        .setLength(sku.getLength())
                        .setHeight(sku.getHeight())
                        .setWidth(sku.getWidth())
                        .setNetWeight(sku.getWeight())
                        .setBarcodes(new ArrayList<>(barcodes))
                        .build();


                geekSkuDAO.save(geekSku);
            }
//            geekSkuDAO.setProcessedFlag();
        } catch (DataAccessException e) {
            alert("Error occurred while moving skus to Geek. [%s]", e.toString());
        }
    }

    public void acknowledgedByGeek(){

        try{
            //take list of processed=yes geekSku
            List<GeekSku> geekSkuList = geekSkuDAO.getAllProcessed();

            for (int i=0; i<geekSkuList.size();i++) {
                String sku = geekSkuList.get(i).getSkuCode();
                String uom = geekSkuList.get(i).getUnit();
                // mark custSkus where ackByGeek=0 to ackByGeek=1
                custSkusDAO.updateAckByGeek(sku,uom,0);
            }
        } catch (DataAccessException e) {
            alert("Error occurred while acknowkedging skus from Geek. [%s]", e.toString());
        }
    }

    @Override
    public void moveFromGeek() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveFromGeek'");
    }

//    public static void main(String[] args) {
//        GeekTranslator gk = new CustSkuGeekTranslator();
//        gk.moveToGeek();
//        gk.acknowledgedByGeek();
//    }
}
