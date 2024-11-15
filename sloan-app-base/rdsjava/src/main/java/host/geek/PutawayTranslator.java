package host.geek;

import dao.*;
import records.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static host.StringUtils.isNumeric;
import static sloane.SloaneConstants.geekOwnerCode;
import static sloane.SloaneConstants.geekWarehouseCode;
import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class PutawayTranslator extends AbstractDAO implements GeekTranslator {
    GeekDAO geekDAO = new GeekDAO();
    CustPutawayOrderDAO custPutawayOrderDAO = new CustPutawayOrderDAO();
    private List<Map<String, String>> allUnprocessedConfirmations;

    private final int NORMAL_RECEIVING_PUTAWAY_TYPE = 1;
    private final int RETURNS_RECEIVING_PUTAWAY_TYPE = 4;
    private final int REPLENISHMENT_PUTAWAY_TYPE = 5;

    /**
     * sentToGeek
     */
    @Override
    public void moveToGeek() {
        putawayOrderCreation();
        putawayOrderCancellation();
    }

    private void putawayOrderCancellation() {
    }

    private void putawayOrderCreation() {
            CustPutawayOrderDAO dao = new CustPutawayOrderDAO();
            CustSkusDAO custSkuDao = new CustSkusDAO();
            try {
                List<CustPutawayOrder> custPutawayOrderList = dao.getAll();
                GeekPutawayOrderDAO geekPutawayOrderDAO = new GeekPutawayOrderDAO();
                for(CustPutawayOrder custPutawayOrder : custPutawayOrderList) {
                    trace("processing custPutawayOrder: " +custPutawayOrder.getPutawayOrderCode());

                    List<GeekPutawayOrderSku> skus = new ArrayList<>(1);

                    CustSku custSku = custSkuDao.getBySku(custPutawayOrder.getSku(),custPutawayOrder.getUom());

                    int putawayOrderType = 0;
                    int putawayQuantity = custPutawayOrder.getQuantity();
                    String custPutawayOrderCode = custPutawayOrder.getPutawayOrderCode();
                    int custPutawayOrdersType = custPutawayOrder.getPutawayType();
                    if(custSku != null){

                        String geekToteCode = custSku.getGeekToteCode();

                        if(geekToteCode.equals("Q") && custPutawayOrdersType == 4){
                            putawayOrderType = 8;
                        }else if(geekToteCode.equals("F") && custPutawayOrdersType == 4){
                            putawayOrderType = 7;
                        }else if(geekToteCode.equals("Q") && (custPutawayOrdersType == 1 || custPutawayOrdersType == 5)){
                            putawayOrderType = 1;
                        }else if(geekToteCode.equals("F") && (custPutawayOrdersType == 1 || custPutawayOrdersType == 5)){
                            putawayOrderType = 0;
                        }


                        /**
                         * change putaway qty sent to geek, after comparing with custSkus.geekPrimaryLocCapacity vs custPutawayOrder.qty
                         * sent the lesser one
                         * */
//                    if(custPutawayOrdersType == 1 || custPutawayOrdersType == 4){
//
//                        int geekPrimaryLocationCapacity = custSku.getGeekPrimaryLocationCapacity();
//
//                        if(geekPrimaryLocationCapacity < putawayQuantity){
//                            trace("     changing putaway qty from [%d] to [%d]",putawayQuantity, geekPrimaryLocationCapacity);
//                            putawayQuantity = geekPrimaryLocationCapacity;
//                        }
//                    } // SR: commented out temporarily after meeting, dated 02/14

                    }else{
                        alert("sku [%s] not found for custPutawayOrder [%s], skipping custPutawayOrder",custPutawayOrder.getSku(),custPutawayOrderCode);
                        updateRowByStringIdStringValue("custPutawayOrders", "status","error","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                        updateRowByStringIdStringValue("custPutawayOrders", "errorMsg","sku-uom not found","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                        continue;
                    }

                    skus.add(new GeekPutawayOrderSku(custPutawayOrder.getSku(),
                            geekOwnerCode,
                            putawayQuantity,
                            0));

                    GeekPutawayOrder geekPutawayOrder = new GeekPutawayOrder(
                            geekWarehouseCode,
                            custPutawayOrderCode,
                            custPutawayOrder.getPalletCode(),
                            putawayOrderType,
                            skus
                    );
                    int rowsInserted = geekPutawayOrderDAO.save(geekPutawayOrder);
                    trace("rowsInserted: [" + rowsInserted + "] for custPutawayOrder ["+ custPutawayOrderCode+"]");
                    if(1 == rowsInserted){
                        geekPutawayOrderDAO.setProcessedFlag();
                        dao.setSentToGeek( custPutawayOrder.getPutawayOrderCode());
                        updateRowByStringIdStringValue("custPutawayOrders", "status","uploadedToGeek","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                        updateRowByStringIdTimeStampValue("custPutawayOrders", "uploadToGeekStamp","now()","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                        updateRowByStringIdIntValue("custPutawayOrders","qtySentToGeek",putawayQuantity,"putawayOrderCode",custPutawayOrderCode);
                    }else{
                        updateRowByStringIdStringValue("custPutawayOrders", "status","error","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                        updateRowByStringIdStringValue("custPutawayOrders", "errorMsg","error inserting records in geek putaway table","putawayOrderCode", custPutawayOrder.getPutawayOrderCode());
                    }
                }

            } catch (DataAccessException e) {
                alert("Error occurred while moving putaway orders to Geek. [%s]", e.toString());
            }

    }

    /**
     * ackToGeek
     */
    @Override
    public void acknowledgedByGeek() {
        putawayOrderCreationAcknowledgement();
        putawayOrderCancellationAcknowledgement();
    }

    private void putawayOrderCreationAcknowledgement() {

        try {

            List<String> allUnprocessedCreationAcknowledgements = custPutawayOrderDAO.getAcknowledgedOrders();
            if(allUnprocessedCreationAcknowledgements != null && !allUnprocessedCreationAcknowledgements.isEmpty()){
                for (String putawayOrderCode : allUnprocessedCreationAcknowledgements){
                    updateRowByStringIdIntValue("custPutawayOrders", "ackByGeek",1,"putawayOrderCode", putawayOrderCode);
                }
            }else{
                trace("no pick acknowledgements to process");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void putawayOrderCancellationAcknowledgement() {
    }

    /**
     * confirmation from Geek
     */
    @Override
    public void moveFromGeek() {
        putawayOrderConfirmation();
    }

    private void putawayOrderConfirmation() {
        try {
            String receipt_code = "";
            String transaction_type = "";
            String putaway_type = "0";
            String putaway_replen_code = "";
            String putaway_code = "";
            String part_number = "";
            int quantity_change = 0;
            String operator_id = "";

            RdsGeekInventoryAdjustments rdsGeekInventoryAdjustments;
            RdsGeekInventoryAdjustmentsDAO rdsGeekInventoryAdjustmentsDAO = new RdsGeekInventoryAdjustmentsDAO();



            allUnprocessedConfirmations= geekDAO.getAllProcessedOrdered("geekPutawayConf", "no", "seq", "ASC");

            /* *
             * Put away Confirmations
             * */
            if(allUnprocessedConfirmations!= null && !allUnprocessedConfirmations.isEmpty()){
                for(Map<String, String> unprocessedPutawayConf : allUnprocessedConfirmations){
                    int rowsAdded = 0 ;
                    int confSkusListSize = 0;
                    int seq = 0;
                    String type = "-1";

                    receipt_code = "";
                    transaction_type = "21";
                    putaway_type = "";
                    putaway_replen_code = "000000000000";
                    putaway_code = "00000000";
                    part_number = "";
                    quantity_change = 0;
                    operator_id = "";

                    seq = Integer.parseInt(unprocessedPutawayConf.get("seq"));
                    trace("processing putaway conf seq  : [%d]", seq);
                    receipt_code = unprocessedPutawayConf.get("receipt_code");

                    putaway_type = custPutawayOrderDAO.getPutawayTypeByKey(receipt_code);

                    /* changes for no order putaway*/
                    type = unprocessedPutawayConf.get("type");

                    if (Integer.parseInt(putaway_type) == NORMAL_RECEIVING_PUTAWAY_TYPE || Integer.parseInt(putaway_type) == RETURNS_RECEIVING_PUTAWAY_TYPE) {
                        putaway_code = receipt_code;
                    } else if (Integer.parseInt(putaway_type) == REPLENISHMENT_PUTAWAY_TYPE) {
                        putaway_replen_code = receipt_code;
                    } else if(putaway_type.equals("-1") // was not able to find in custPutawayOrders
                            && type.equals("103010")){ // Tim said to work with 103010, after meeting on 03/19 with Geek
                        putaway_type = "5"; // Tim said set to 5, after meeting on 03/19 with Sloane
                        putaway_replen_code = "000000000000";
                        putaway_code = "00000000";
                    }
                    else{
                        alert("invalid putaway_type [%s] for seq [%s], skipping the record", putaway_type, seq);
                        updateRowByStringIdStringValue("custPutawayOrders", "status","error","putawayOrderCode", receipt_code);
                        updateRowByStringIdStringValue("custPutawayOrders", "errorMsg","GeekError: invalid putaway_type ["+putaway_type+"] for seq ["+seq+"]","putawayOrderCode", receipt_code);
                        int rowsUpdated = geekDAO.updateProcessed("geekPutawayConf","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPutawayConf.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }

                    operator_id = unprocessedPutawayConf.get("receiptor");
                    if(!isNumeric(operator_id)) {
                        String defaultOperatorId = db.getControl("geekplus","defaultOperatorId","70113");
                        alert("invalid receiptor [%s] for seq [%s], defaulting to", operator_id, seq, defaultOperatorId);
                        operator_id = defaultOperatorId;

//                        updateRowByStringIdStringValue("custPutawayOrders", "status","error","putawayOrderCode", receipt_code);
//                        updateRowByStringIdStringValue("custPutawayOrders", "errorMsg","GeekError: invalid receiptor  ["+operator_id+"] for seq ["+seq+"]","putawayOrderCode", receipt_code);
//                        int rowsUpdated = geekDAO.updateProcessed("geekPutawayConf","err", seq);
//                        if(rowsUpdated != 1){
//                            alert("error while setting geekPutawayConf.seq[%d] as processed = 'err'",seq);
//                        }
//                        continue;
                    }



                    List<Map<String, String>> allByParent = geekDAO.getAllByParent(seq,"geekPutawayConfSku");
                    confSkusListSize = allByParent.size();
                    trace("for conf[%d], rows found [%d]", seq, confSkusListSize);

                    //For Sloane, one putaway is only one line in putawaySku
                    if(confSkusListSize != 1){
                        alert("invalid total no. of lines for conf seq [%d], confSkusListSize [%d], skipping this conf", seq, confSkusListSize);
                        updateRowByStringIdStringValue("custPutawayOrders", "status","error","putawayOrderCode", receipt_code);
                        updateRowByStringIdStringValue("custPutawayOrders", "errorMsg","GeekError: invalid total no. of lines  ["+confSkusListSize+"] for seq ["+seq+"]","putawayOrderCode", receipt_code);
                        int rowsUpdated = geekDAO.updateProcessed("geekPutawayConf","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPutawayConf.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }

                    for(Map<String, String> geekPutawayConfSku : allByParent) {
                        int childSeq = Integer.parseInt(geekPutawayConfSku.get("seq"));
                        part_number = geekPutawayConfSku.get("sku_code");
                        quantity_change = Integer.parseInt(geekPutawayConfSku.get("amount"));

                        trace("processing putaway conf seq  : [%d]-[%d]", seq, childSeq);
                        trace("         transaction_type    : [%s]", transaction_type);
                        trace("         putaway_type        : [%s]", putaway_type);
                        trace("         putaway_replen_code : [%s]", putaway_replen_code);
                        trace("         putaway_code        : [%s]", putaway_code);
                        trace("         part_number         : [%s]", part_number);
                        trace("         quantity_change     : [%d]", quantity_change);
                        trace("         operator_id         : [%s]", operator_id);

                        rdsGeekInventoryAdjustments = new RdsGeekInventoryAdjustments.RdsGeekInventoryAdjustmentsBuilder
                                (transaction_type,
                                        part_number,
                                        putaway_replen_code,
                                        putaway_code,
                                        putaway_type,
                                        quantity_change,
                                        operator_id,
                                        "N/A")
                                .build();

                        rowsAdded += rdsGeekInventoryAdjustmentsDAO.save(rdsGeekInventoryAdjustments);
                        trace("for conf[%d], added rows [%d]", seq, rowsAdded);
                    }
                    if(rowsAdded == confSkusListSize){
                        int rowsUpdated = geekDAO.updateProcessed("geekPutawayConf","yes", seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPutawayConf.seq[%d] as processed = 'yes'",seq);
                        }
                        updateRowByStringIdStringValue("custPutawayOrders", "status","confirmed","putawayOrderCode", receipt_code);
                        updateRowByStringIdTimeStampValue("custPutawayOrders", "confirmStamp","now()","putawayOrderCode", receipt_code);
                    }else{
                        int rowsUpdated = geekDAO.updateProcessed("geekPutawayConf","err", seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPutawayConf.seq[%d] as processed = 'err'",seq);
                        }
                    }
                }
            }else{
                trace("no putaway confirmations to translate");
            }


        }catch (DataAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
