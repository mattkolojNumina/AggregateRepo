package host.geek;

import dao.*;
import records.*;

import java.util.*;

import static host.StringUtils.isNumeric;
import static sloane.SloaneConstants.geekOwnerCode;
import static sloane.SloaneConstants.geekWarehouseCode;
import static rds.RDSLog.alert;
import static rds.RDSLog.inform;
import static rds.RDSLog.trace;

public class BaseDataTranslator implements GeekTranslator {
    /**
     * sentToGeek
     */
    @Override
    public void moveToGeek() {
        sendSkus();
        sendUsers();
    }

    private void sendUsers() {
        ProOperatorDAO proOperatorDao = new ProOperatorDAO();
        GeekUserDAO geekUserDAO = new GeekUserDAO();
        GeekUserRoleDAO geekUserRoleDAO = new GeekUserRoleDAO();
        try {
            List<ProOperator> operatorList = proOperatorDao.getAllUnprocessed();
            inform("[%d] operators ready to be sent to geek", operatorList.size());
            for(ProOperator operator: operatorList) {
                GeekUser geekUser = new GeekUser.GeekUserBuilder(
                        geekWarehouseCode,
                        operator.getOperatorId(),
                        operator.getOperatorName(),
                        operator.getPassword(),
                        operator.getStatus()
                ).build();
                if(1 == geekUserDAO.save(geekUser)){
                    GeekUserRole geekUserRole = new GeekUserRole.GeekUserRoleBuilder(
                            geekWarehouseCode,
                            operator.getOperatorId(),
                            "operator"
                    ).build();
                    geekUserRoleDAO.save(geekUserRole);
                    proOperatorDao.setSentToGeek( operator.getOperatorId());
                }

            }
            geekUserDAO.setProcessedFlag();
        } catch (DataAccessException e) {
            alert("Error occurred while moving an operator to RDS Geek geekUser. [%s]", e.toString());
        }
    }

    private void sendSkus() {
        String emptyBarcode = "00000000000000";
        String emptyGtin8 = "00000000";
        String emptyGtin13 = "0000000000000";
        CustSkusDAO custSkusDAO = new CustSkusDAO();
        GeekSkuDAO geekSkuDAO = new GeekSkuDAO();

        try {
            List<CustSku> skuList = custSkusDAO.getAllToBeSentToGeek();
            inform("[%d] skus ready to be sent to geek", skuList.size());

            for(CustSku sku: skuList) {

                // eliminate barcodes that are invalid (empty)
                // use Set to ensure uniqueness of barcode list
                Set<String> barcodes = new HashSet<>(8);
                if (isNumeric(sku.getBarcode()) && !emptyBarcode.equals(sku.getBarcode()) ) { //remove first 2
                    barcodes.add(sku.getBarcode().substring(2));
                }
                if (isNumeric(sku.getBarcode()) && !emptyBarcode.equals(sku.getBarcode()) ) { //remove first and last
                    barcodes.add(sku.getBarcode().substring(1,sku.getBarcode().length()-1));
                }
                if (isNumeric(sku.getUpc2()) && !emptyBarcode.equals(sku.getUpc2()) ) { //remove first 2
                    barcodes.add(sku.getUpc2().substring(2));
                }
                if (isNumeric(sku.getUpc2()) && !emptyBarcode.equals(sku.getUpc2()) ) { //remove first and last
                    barcodes.add(sku.getUpc2().substring(1,sku.getUpc2().length()-1));
                }
                if (isNumeric(sku.getUpc3()) && !emptyBarcode.equals(sku.getUpc3()) ) { //remove first 2
                    barcodes.add(sku.getUpc3().substring(2));
                }
                if (isNumeric(sku.getUpc3()) && !emptyBarcode.equals(sku.getUpc3()) ) { //remove first and last
                    barcodes.add(sku.getUpc3().substring(1,sku.getUpc3().length()-1));
                }
                if (isNumeric(sku.getGtin8()) && !emptyGtin8.equals(sku.getGtin8()) ) {
                    barcodes.add(sku.getGtin8());
                }
                if (isNumeric(sku.getGtin13()) && !emptyGtin13.equals(sku.getGtin13()) ) {
                    barcodes.add(sku.getGtin13());
                }

                GeekSku geekSku = new GeekSku.GeekSkuBuilder(geekWarehouseCode, sku.getSku())
                        .setOwnerCode(geekOwnerCode)
                        .setUnit(sku.getUom())
                        .setSkuName(sku.getDescription())
                        .setLength(sku.getLength())
                        .setHeight(sku.getHeight())
                        .setWidth(sku.getWidth())
                        .setNetWeight(sku.getWeight())
                        .setRemark(sku.getDescription())
                        .setWares_type_code(sku.getBuyingDepartmentCode())
                        .setSpecification(sku.getGeekToteCode())
                        .setItem_size(sku.getGeekToteDesiredQuantity()+"")
                        .setItem_color(sku.getGeekPrimaryLocationCapacity()+"")
                        .setProduction_location(sku.getQuantityShelfPack()+"")
                        .setItem_style(sku.getQroFlag())
                        .setBarcodes(new ArrayList<>(barcodes))
                        .build();
                if(1 == geekSkuDAO.save(geekSku)){
                    custSkusDAO.updateSentToGeek( sku.getSku(), sku.getUom(), 1);
                    geekSkuDAO.setProcessedFlagForSkuUom(sku.getSku(), sku.getUom());
                }
            }

        } catch (DataAccessException e) {
            alert("Error occurred while moving skus to Geek. [%s]", e.toString());
        }
    }

    /**
     * ackToGeek
     */
    @Override
    public void acknowledgedByGeek() {
//        acknowledgementForSkus();
        acknowledgementForSkus_v2();
//        acknowledgementForUsers();
        acknowledgementForUsers_v2();
    }
    GeekDAO geekDAO = null;
    private void acknowledgementForSkus() {
        CustSkusDAO custSkusDAO = new CustSkusDAO();
        geekDAO = new GeekDAO();
        try {
            List<Map<String, String>> processedSkusList = geekDAO.getAllProcessedOrdered("geekSku", "yes", "stamp", "ASC");
            inform("[%d] skus acknowledged by geek",processedSkusList.size());
            for(Map<String,String> processedSkuRow : processedSkusList){
                String sku_code = processedSkuRow.get("sku_code");
                String uom = processedSkuRow.get("unit");
                custSkusDAO.updateAckByGeek(sku_code,uom,1);
            }
        }catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private void acknowledgementForUsers()  {
        ProOperatorDAO proOperatorDao = new ProOperatorDAO();
        geekDAO = new GeekDAO();
        try {
            List<Map<String, String>> processedUsersList = geekDAO.getAllProcessedOrdered("geekUser", "yes", "user_name", "ASC");
            inform("[%d] users acknowledged by geek",processedUsersList.size());
            for(Map<String,String> processedUserRow : processedUsersList){
                String operatorID = processedUserRow.get("user_name");
                proOperatorDao.setAckByGeek(operatorID);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void acknowledgementForSkus_v2()  {
        CustSkusDAO custSkusDAO = new CustSkusDAO();
        try {
            List<Map<String, String>> acknowledgedSkusList = custSkusDAO.getAcknowledgedSkus();
            inform("[%d] skus acknowledged by geek",acknowledgedSkusList.size());
            for(Map<String,String> acknowledgedSku : acknowledgedSkusList){
//                System.out.println("acknowledgedSku: " + acknowledgedSku);
                for (Map.Entry<String, String> entry : acknowledgedSku.entrySet()) {
                    String sku_code = entry.getKey();
                    String uom = entry.getValue();
//                    System.out.println("sku_code: " + sku_code + ", uom: " + uom);

                    custSkusDAO.updateAckByGeek(sku_code,uom,1);
                }
            }


        }catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void acknowledgementForUsers_v2()  {
        ProOperatorDAO proOperatorDao = new ProOperatorDAO();
        try {
            List<String> acknowledgedUsersList = proOperatorDao.getAcknowledgedUsers();
            inform("[%d] users acknowledged by geek",acknowledgedUsersList.size());
            for(String acknowledgedUser : acknowledgedUsersList){
                proOperatorDao.setAckByGeek(acknowledgedUser);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * confirmation from Geek
     */
    @Override
    public void moveFromGeek() {

    }
}
