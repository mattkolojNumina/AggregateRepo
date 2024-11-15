package host.geek;

import dao.DataAccessException;
import dao.GeekDAO;
import dao.RdsGeekInventoryAdjustmentsDAO;
import records.RdsGeekInventoryAdjustments;

import java.util.List;
import java.util.Map;

import static host.StringUtils.isNumeric;
import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class StocktakeTranslator implements GeekTranslator {

    GeekDAO geekDAO = new GeekDAO();
    RdsGeekInventoryAdjustments rdsGeekInventoryAdjustments;
    RdsGeekInventoryAdjustmentsDAO rdsGeekInventoryAdjustmentsDAO = new RdsGeekInventoryAdjustmentsDAO();
    private List<Map<String, String>> allUnprocessedPickingCreations;
    private List<Map<String, String>> allUnprocessedPickingCreationAcknowledgements;
    private List<Map<String, String>> allUnprocessedPickingCancellations;
    private List<Map<String, String>> allUnprocessedPickingCancellationAcknowledgements;
    private List<Map<String, String>> allUnprocessedConfirmations;
    /**
     * sentToGeek
     */
    @Override
    public void moveToGeek() {

    }

    /**
     * ackToGeek
     */
    @Override
    public void acknowledgedByGeek() {

    }

    /**
     * confirmation from Geek
     */
    @Override
    public void moveFromGeek() {
        stocktakeFeedbackConfirmations();
    }

    private void stocktakeFeedbackConfirmations() {

        try {
            String receipt_code = "";
            String transaction_type = "";
            String putaway_type = "0";
            String putaway_replen_code = "";
            String putaway_code = "";
            String part_number = "";
            int quantity_change = 0;
            String operator_id = "";


            allUnprocessedConfirmations = geekDAO.getAllProcessedOrdered("geekInternalFeedback", "no", "seq", "ASC");

            if(allUnprocessedConfirmations != null && !allUnprocessedConfirmations.isEmpty()){

                for (Map<String, String> unprocessedConf : allUnprocessedConfirmations) {
                    int rowsAdded = 0 ;
                    int confSkusListSize = 0;
                    int seq = 0;

                    transaction_type = "";
                    putaway_type = "0";
                    putaway_replen_code = "000000000000";
                    putaway_code = "00000000";

                    seq = Integer.parseInt(unprocessedConf.get("seq"));
                    operator_id = unprocessedConf.get("operator");
                    if(!isNumeric(operator_id)) {
                        alert("invalid receiptor [%s] for seq [%s] skipping the record", operator_id, seq);
                        int rowsUpdated = geekDAO.updateProcessed("geekInternalFeedback","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekInternalFeedback.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }

                    List<Map<String, String>> allByParent = geekDAO.getAllByParent(seq, "geekInternalFeedbackSku");
                    confSkusListSize = allByParent.size();
                    trace("for conf[%d], rows found [%d]", seq, confSkusListSize);
                    confSkusListSize = allByParent.size();

                    for (Map<String, String> childRow : allByParent) {
                        int childSeq = Integer.parseInt(childRow.get("seq"));
                        part_number = "";
                        quantity_change = 0;

                        part_number = childRow.get("sku_code");

                        int amount = Integer.parseInt(childRow.get("amount"));

                        if(amount > 0){
                            transaction_type = "23";
                        }else if(amount < 0){
                            transaction_type = "24";
                            amount *= -1;
                        }if(amount == 0){
                            //SR: TBD
                        }

                        quantity_change = amount;

                        trace("processing geekInternalFeedback conf seq  : [%d]-[%d]", seq,childSeq);
                        trace("         transaction_type      : [%s]", transaction_type);
                        trace("         putaway_type          : [%s]", putaway_type);
                        trace("         putaway_replen_code   : [%s]", putaway_replen_code);
                        trace("         putaway_code          : [%s]", putaway_code);
                        trace("         part_number           : [%s]", part_number);
                        trace("         quantity_change       : [%d]", quantity_change);
                        trace("         operator_id           : [%s]", operator_id);

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
                        int rowsUpdated = geekDAO.updateProcessed("geekInternalFeedback","yes",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekInternalFeedback.seq[%d] as processed = 'yes'",seq);
                        }
                    }else{
                        int rowsUpdated = geekDAO.updateProcessed("geekInternalFeedback","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekInternalFeedback.seq[%d] as processed = 'err'",seq);
                        }
                    }
                }
            }else{
                trace("no geekInternalFeedback(s) to translate");
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
