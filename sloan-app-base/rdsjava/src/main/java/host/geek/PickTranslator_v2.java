package host.geek;

import dao.*;
import rds.RDSUtil;
import records.RdsGeekInventoryAdjustments;

import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.*;
import static host.StringUtils.isNumeric;
import static rds.RDSLog.*;



public class PickTranslator_v2 extends AbstractDAO implements GeekTranslator {
    SloaneCommonDAO sloaneCommonDao = new SloaneCommonDAO();
    GeekDAO geekDAO = new GeekDAO();
    RdsGeekInventoryAdjustments rdsGeekInventoryAdjustments;
    RdsGeekInventoryAdjustmentsDAO rdsGeekInventoryAdjustmentsDAO = new RdsGeekInventoryAdjustmentsDAO();

    private List<Map<String, String>> allUnprocessedPickingCreations;
    private List<Map<String, String>> allUnprocessedPickingCancellations;
    private List<String> allUnprocessedPickingCreationAcknowledgements;
    private List<String> allUnprocessedPickingCancellationAcknowledgements;
    private List<Map<String, String>> allUnprocessedPickingConfirmations;


    /**
     * sentToGeek
     */
    @Override
    public void moveToGeek() {
        pickOrderCreation();
        pickOrderCancellation();
    }

    private void pickOrderCreation() {

        try {

            allUnprocessedPickingCreations = SloaneCommonDAO.getTableRowsListByColumnString("rdsCartons","geekStatus","readyToSend");

            if(allUnprocessedPickingCreations != null && !allUnprocessedPickingCreations.isEmpty()){

                for (Map<String,String> readyForGeekCartonMap : allUnprocessedPickingCreations ){
                    int seq = 0;
                    int cartonSeq = Integer.parseInt(readyForGeekCartonMap.get("cartonSeq"));
                    String trackingNumber = readyForGeekCartonMap.get("trackingNumber");
                    String dailyWaveSeq = getTableRowByStringId( "custOrders", "orderId",readyForGeekCartonMap.get("orderId")).get("dailyWaveSeq");
                    String designatedContainerType = readyForGeekCartonMap.get("cartonType");
                    int designatedContainerTypeCode = Integer.parseInt(getTableRowByStringId( "cfgCartonTypes", "cartonType", designatedContainerType).get("geekType"));

                    String dailyWaveSeqString = dailyWaveSeq;

                    if(dailyWaveSeqString.length() == 1){
                        dailyWaveSeqString = "0" + dailyWaveSeqString;
                    }
                    dailyWaveSeqString = "W"+dailyWaveSeqString;

                    int orderType = switch (dailyWaveSeqString) {
                        case "W01" -> 25;
                        case "W02" -> 26;
                        case "W03" -> 27;
                        case "W04" -> 28;
                        case "W05" -> 29;
                        case "W06" -> 30;
                        case "W07" -> 31;
                        case "W08" -> 32;
                        case "W09" -> 33;
                        case "W10" -> 34;
                        case "W11" -> 35;
                        case "W12" -> 36;
                        default -> 14;
                    };

                    int priority = switch (dailyWaveSeqString) {
                        case "W01" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W01").get("priority"),99999);
                        case "W02" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W02").get("priority"),99998);
                        case "W03" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W03").get("priority"),99997);
                        case "W04" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W04").get("priority"),99996);
                        case "W05" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W05").get("priority"),99995);
                        case "W06" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W06").get("priority"),99994);
                        case "W07" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W07").get("priority"),99993);
                        case "W08" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W08").get("priority"),99992);
                        case "W09" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W09").get("priority"),99991);
                        case "W10" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W10").get("priority"),99990);
                        case "W11" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W11").get("priority"),99989);
                        case "W12" -> RDSUtil.stringToInt(getTableRowByStringId( "cfgGeekWave", "waveSeq","W12").get("priority"),99988);
                        default -> 99999;
                    };

                    List<Map<String,String>> picksMapList = getTableRowsListByColumnInt( "rdsPicks","cartonSeq", cartonSeq);

                    int childRowCount = picksMapList.size();
                    if(childRowCount > 0){
                        trace("creating geekPickOrder for cartonSeq [%d], trackingNumber [%s], wave [%s], designatedContainerTypeCode [%d], priority [%d]", cartonSeq, trackingNumber, dailyWaveSeqString, designatedContainerTypeCode, priority);
                        if(createGeekPickOrder(trackingNumber, designatedContainerTypeCode, dailyWaveSeqString, orderType, priority)){
                            seq = db.getSequence();
                            //geekDAO.updateProcessed("geekPickOrder","wait", seq);
                        }else{
                            alert("error while creating geekPickOrder [%s], skipping the order", cartonSeq +"");
                            continue;
                        }
                    }else{
                        alert("no picks found for cartonSeq [%d] in rds picks, skipping the order", cartonSeq);
                        continue;
                    }

                    int rowsAdded = 0;
                    for (Map<String,String> picksMap : picksMapList ){

                        int pickSeq = Integer.parseInt(picksMap.get("pickSeq"));
                        String sku = picksMap.get("sku");
                        String uom = picksMap.get("uom");
                        int qty = (int) Float.parseFloat(picksMap.get("qty"));

                        trace("creating geekPickOrderSku for geekPickOrder.seq: [%d]", seq);
                        trace("                 pickSeq : [%d]", pickSeq);
                        trace("                 sku     : [%s]", sku);
                        trace("                 uom     : [%s]", uom);
                        trace("                 qty     : [%d]", qty);

                        // ask tim about value for skuLevel
                         rowsAdded += SloaneCommonDAO.createGeekPickOrderSku(seq, sku, qty, pickSeq);

                    }

                    if(childRowCount == rowsAdded){
                        int rowsUpdated = geekDAO.updateProcessed("geekPickOrder","no", seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPickOrder.seq[%d] as processed = 'no'",seq);
                        }
                        if( 1!=updateRowByIntIdStringValue("rdsCartons", "geekStatus","sent","cartonSeq", cartonSeq)){
                            alert("error");
                        }
                    }else{
                        // childRow insertions mismatch, update geekPickOrder processed = 'err'
                        int rowsUpdated = geekDAO.updateProcessed("geekPickOrder","err", seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPickOrder.seq[%d] as processed = 'err'",seq);
                        }
                        updateRowByIntIdStringValue("rdsCartons", "geekStatus","error","cartonSeq", cartonSeq);
                    }

                }
            }else{
                trace("no pick orders to create");
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

    }

    private void pickOrderCancellation() {
    }



    /**
     * ackToGeek
     */
    @Override
    public void acknowledgedByGeek() {
        pickOrderCreationAcknowledgement();
        pickOrderCancellationAcknowledgement();
    }

    private void pickOrderCreationAcknowledgement() {
        try {
            RdsCartonsDAO rdsCartonsDAO = new RdsCartonsDAO();
           allUnprocessedPickingCreationAcknowledgements = rdsCartonsDAO.getAcknowledgedOrders();
            if(allUnprocessedPickingCreationAcknowledgements != null && !allUnprocessedPickingCreationAcknowledgements.isEmpty()){
                for (String trackingNumber : allUnprocessedPickingCreationAcknowledgements){
                    updateRowByStringIdStringValue("rdsCartons", "geekStatus","received","trackingNumber", trackingNumber);
                    updateRowByStringIdTimeStampValue("rdsCartons", "receivedByGeekStamp","now()","trackingNumber", trackingNumber);
                }
            }else{
                trace("no pick acknowledgements to process");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pickOrderCancellationAcknowledgement() {
    }



    /**
     * confirmation from Geek
     */
    @Override
    public void moveFromGeek() {
        bindingUpdate();
        pickOrderConfirmation();
    }

    private void bindingUpdate() {
        try {
            List<Map<String, String>> unprocessedBindingContainers = geekDAO.getAllProcessedOrdered("geekBinding", "no", "seq", "ASC");
            if( unprocessedBindingContainers == null || unprocessedBindingContainers.isEmpty() ) {
                inform("no binding to translate");
                return;
            }
            for(Map<String,String> m : unprocessedBindingContainers) {
                int seq = getMapInt(m,"seq");
                String out_order_code = getMapStr(m,"out_order_code");
                String container_code = getMapStr(m,"container_code");
                String wall_code = getMapStr(m,"wall_code");
                String workstation_no = getMapStr(m,"workstation_no");
                String seeding_bin_code = getMapStr(m,"seeding_bin_code");
                int cartonSeq = db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE trackingNumber='%s' ORDER BY cartonSeq DESC LIMIT 1", out_order_code);
                if( cartonSeq <=0 ){
                    alert("unknown rdsCartons.trackingNumber ~ (geekBinding.out_order_code) [%s, skipping the row",out_order_code);
                    int rowsUpdated = geekDAO.updateProcessed("geekBinding","err",seq);
                    if(rowsUpdated != 1){
                        alert("error while setting geekBinding.seq[%d] as processed = 'err'",seq);
                    }
                    continue;
                }
                db.execute("UPDATE rdsCartons SET lpn='%s', assigned=1 WHERE cartonSeq=%d", container_code, cartonSeq);
                inform("Update LPN %s for Geek cartonSeq %d from geekBinding",container_code, cartonSeq);
                int rowsUpdated = geekDAO.updateProcessed("geekBinding","yes",seq);
                if(rowsUpdated != 1){
                    alert("error while setting geekBinding.seq[%d] as processed = 'yes'",seq);
                }
            }

        }catch (Exception e) {
            alert("exception: %s",e.toString());
        }
    }

    private void pickOrderConfirmation()  {

        try {
            String receipt_code = "";
            String transaction_type = "";
            String container_code = "";
            String out_order_code = "";
            String putaway_type = "0";
            String putaway_replen_code = "";
            String putaway_code = "";
            String part_number = "";
            int quantity_change = 0;
            String operator_id = "";

            int rdsPicksSeq=0;
            int rdsCartonsSeq=0;

            allUnprocessedPickingConfirmations = geekDAO.getAllProcessedOrdered("geekPickContainerConf", "no", "seq", "ASC");

            if(allUnprocessedPickingConfirmations != null && !allUnprocessedPickingConfirmations.isEmpty()){

                for (Map<String, String> unprocessedPickingConf : allUnprocessedPickingConfirmations) {
                    int rowsAdded = 0 ;
                    int confSkusListSize = 0;
                    int geekPickConfContainerSeqListSize = 0 ;
                    int geekPickOrderConfSeq = 0;
                    int geekPickOrderConfSkuSeq = 0;

                    transaction_type = "22";
                    putaway_type = "0";
                    putaway_replen_code = "000000000000";
                    putaway_code = "00000000";

                    operator_id = unprocessedPickingConf.get("picker");
                    geekPickOrderConfSeq = Integer.parseInt(unprocessedPickingConf.get("seq"));

                    container_code = unprocessedPickingConf.get("container_code");
                    trace("processing geekPickContainerConf seq [%d], lpn [%s]", geekPickOrderConfSeq, container_code);

                    List<Map<String, String>> geekPickConfSkuList = geekDAO.getAllByParent(geekPickOrderConfSeq,"geekPickContainerConfSku");
                    confSkusListSize = geekPickConfSkuList.size();
                    trace("for geekPickContainerConf seq[%d], [%d] rows found in geekPickContainerConfSku", geekPickOrderConfSeq, confSkusListSize);


                    if( geekPickConfSkuList != null || !geekPickConfSkuList.isEmpty()){

                        boolean isFirst = true;
                        for(Map<String, String> geekPickConfSku : geekPickConfSkuList){
                            geekPickOrderConfSkuSeq = Integer.parseInt(geekPickConfSku.get("seq"));
                            trace("processing geekPickContainerConf seq : [%d]-[%d]", geekPickOrderConfSeq, geekPickOrderConfSkuSeq);

                            if(isFirst == true){
                                out_order_code = geekPickConfSku.get("out_order_code");
                                isFirst = false;
                                Map<String,String> recordMap = getTableRowByStringId("rdsCartons", "trackingNumber", out_order_code);
                                if(recordMap == null || !recordMap.containsKey("cartonSeq")){
                                    alert("unknown rdsCartons.trackingNumber ~ (geekPickContainerConfSku.out_order_code) [%s], under geekPickContainerConf seq : [%d], skipping the row", out_order_code, geekPickOrderConfSeq);
                                    int rowsUpdated = geekDAO.updateProcessed("geekPickContainerConf","err", geekPickOrderConfSeq);
                                    if(rowsUpdated != 1){
                                        alert("error while setting geekPickContainerConf.seq[%d] as processed = 'err'", geekPickOrderConfSeq);
                                    }
                                    break;
                                }
                                rdsCartonsSeq = Integer.parseInt(recordMap.get("cartonSeq"));

                                trace("processing geekPickContainerConf Seq [%d], trackingNumber[%s], cartonSeq[%d]",geekPickOrderConfSeq,out_order_code,rdsCartonsSeq);

                                updateRowByIntIdStringValue("rdsCartons","lpn",container_code,"cartonSeq",rdsCartonsSeq);

                                // set rdsCartons.geekPickConfirmedStamp
                                updateRowByIntIdTimeStampValue("rdsCartons","geekPickConfirmedStamp","now()","cartonSeq",rdsCartonsSeq);
                                // set rdsCartons.geekStatus = confirmed
                                updateRowByIntIdStringValue("rdsCartons","geekStatus","confirmed","cartonSeq",rdsCartonsSeq);
                            }

                            rdsPicksSeq = Integer.parseInt(geekPickConfSku.get("out_batch_code"));
                            part_number = geekPickConfSku.get("sku_code");
                            quantity_change = Integer.parseInt(geekPickConfSku.get("amount"));

                            trace("         rdsPicks.seq        : [%d]", rdsPicksSeq);
                            trace("         transaction_type    : [%s]", transaction_type);
                            trace("         putaway_type        : [%s]", putaway_type);
                            trace("         putaway_replen_code : [%s]", putaway_replen_code);
                            trace("         putaway_code        : [%s]", putaway_code);
                            trace("         part_number         : [%s]", part_number);
                            trace("         quantity_change     : [%d]", quantity_change);
                            trace("         operator_id         : [%s]", operator_id);

                            /**
                             * rds level changes
                             */

                            if(!isNumeric(operator_id)) {
                                String defaultOperatorId = db.getControl("geekplus","defaultOperatorId","70113");
                                alert("rdsCartons.trackingNumber ~ (geekPickConf.out_order_code) [%s] has invalid picker [%s], defaulting to [%s]",out_order_code,operator_id,defaultOperatorId);
                                operator_id = defaultOperatorId;
                            }

                            // rdsPicks.picked , rdsPicks.pickStamp / rdsPicks.shortPicked ,  rdsPicks.shortStamp
                            if(quantity_change < 1){
                                updateRowByIntIdStringValue("rdsPicks", "pickOperatorId",operator_id,"pickSeq", rdsPicksSeq);
                                confirmShort(rdsPicksSeq, operator_id);
                            }else if(quantity_change > 0){
                                updateRowByIntIdStringValue("rdsPicks", "pickOperatorId",operator_id,"pickSeq", rdsPicksSeq);
                                confirmPick(rdsPicksSeq, operator_id);
                            }


                        /**
                         * only do below if the rds level changes work
                         */
                            if(true){ // if customer does not want adjustments when nothing is picked i.e. amount = 0, then add that condition in this IF condition i.e. && amount > 0
                                rdsGeekInventoryAdjustments = new RdsGeekInventoryAdjustments.RdsGeekInventoryAdjustmentsBuilder
                                        (transaction_type,
                                                part_number,
                                                putaway_replen_code,
                                                putaway_code,
                                                putaway_type,
                                                quantity_change,
                                                operator_id,
                                                out_order_code)
                                        .build();
                                rowsAdded += rdsGeekInventoryAdjustmentsDAO.save(rdsGeekInventoryAdjustments);
                                trace("for conf[%d], added rows [%d]", geekPickOrderConfSeq, rowsAdded);
                            }

                    }

                        /** Change here */
                    if(rowsAdded == confSkusListSize){
                        int rowsUpdated = geekDAO.updateProcessed("geekPickContainerConf","yes",geekPickOrderConfSeq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPickContainerConf.seq[%d] as processed = 'yes'",geekPickOrderConfSeq);
                        }
                    }else{
                        int rowsUpdated = geekDAO.updateProcessed("geekPickContainerConf","err",geekPickOrderConfSeq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekPickContainerConf.seq[%d] as processed = 'err'",geekPickOrderConfSeq);
                        }
                    }}
                }



            }else{
                trace("no pick confirmations to translate");
            }

        } catch (DataAccessException e) {

            e.printStackTrace();
        }
    }

}
