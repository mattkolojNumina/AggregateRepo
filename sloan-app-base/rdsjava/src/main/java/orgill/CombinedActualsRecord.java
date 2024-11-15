package sloane;

import dao.*;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static rds.RDSLog.alert;
import static rds.RDSUtil.*;

public class CombinedActualsRecord extends AbstractDAO implements FileRecord {

    FileWatcher fw;
    FileWriter fWriter;

    public CombinedActualsRecord(FileWatcher fw){
        this.fw = fw;
        fWriter = new FileWriter(fw);
    }



    List<List<String>> linesList = new ArrayList<>();

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
   	  linesList = new ArrayList<>();
        validationErrors.addAll(validateGeekInventoryAdjustments()); // 8.1
        validationErrors.addAll(validateOperatorActivity()); // 4.6
        validationErrors.addAll(validateOrderLineConfirmations()); // 6.2
        validationErrors.addAll(validateToteContentsActuals()); // 6.3
        validationErrors.addAll(validateTotesPerOrderActuals()); // 7.1

        return validationErrors;
    }

    public List<String> validateGeekInventoryAdjustments() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
        GeekDAO geekDao = new GeekDAO();
        String seq;
        String transaction_type;
        String part_number;
        String putaway_replen_code;
        String putaway_code;
        String putaway_type;
        String quantity_change;
        String operator_id;

        List<Map<String, String>> unprocessedInvenAdjustList = new ArrayList<>();
        unprocessedInvenAdjustList = geekDao.getAllProcessedOrdered("rdsGeekInventoryAdjustments", "no","createStamp","ASC");
//        trace(unprocessedInvenAdjustList.toString()+"");
        if(unprocessedInvenAdjustList!= null && !unprocessedInvenAdjustList.isEmpty()){
            for (int i = 0; i < unprocessedInvenAdjustList.size(); i++) {
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                Map<String, String> unprocessedInvenAdjustMap = unprocessedInvenAdjustList.get(i);

                seq = unprocessedInvenAdjustMap.get("seq");
                trace("seq                     : [%s]", seq);
                int rowsUpdated = geekDao.updateProcessed("rdsGeekInventoryAdjustments", "processing", Integer.parseInt(seq));
                if(rowsUpdated != 1){
                    alert("error while setting rdsGeekInventoryAdjustments.seq[%d] as processed = 'processing'",Integer.parseInt(seq));
                }
                transaction_type = unprocessedInvenAdjustMap.get("transaction_type");
                putaway_type = unprocessedInvenAdjustMap.get("putaway_type");
                putaway_replen_code = (unprocessedInvenAdjustMap.get("putaway_replen_code") != null)
                        ? unprocessedInvenAdjustMap.get("putaway_replen_code")
                        : "000000000000";
                putaway_code = (unprocessedInvenAdjustMap.get("putaway_code") != null)
                        ? unprocessedInvenAdjustMap.get("putaway_code")
                        : "00000000";// SR: bugfix/289
                part_number = unprocessedInvenAdjustMap.get("part_number");
                quantity_change = unprocessedInvenAdjustMap.get("quantity_change");
                operator_id = unprocessedInvenAdjustMap.get("operator_id");

                // do validation here
                if(!error && transaction_type.equals("21")){
                    if(putaway_type.equals("1")) {
                        if(!putaway_replen_code.equals("000000000000")
                                || putaway_code.equals("00000000")) {// SR: bugfix/289
                            validationErrors.add("invalid putaway codes for transaction-putaway type 21-1");
                            alert("seq [%s]: invalid putaway codes for transaction-putaway type 21-1",seq);
                            error = true;
                        }
                    }
                    else if (putaway_type.equals("4")) {
                        if (!putaway_replen_code.equals("000000000000")
                                || putaway_code.equals("00000000")) {// SR: bugfix/289
                            validationErrors.add("invalid putaway codes for transaction-putaway type 21-4");
                            alert("seq [%s]: invalid putaway codes for transaction-putaway type 21-4",seq);
                            error = true;
                        }
                    }
                    else if (putaway_type.equals("5")){
                        if(!putaway_code.equals("00000000")) {// SR: bugfix/289
                            validationErrors.add("invalid putaway codes for transaction-putaway type 21-5");
                            alert("seq [%s]: invalid putaway codes for transaction-putaway type 21-5", seq);
                            error = true;
                        }

                    }else{
                        validationErrors.add("invalid putaway codes for transaction-putaway type 21-["+ putaway_type+"]");
                        alert("seq [%s]: invalid putaway codes for transaction-putaway type 21-["+ putaway_type+"]",seq);
                        error = true;
                    }

                }
                else if(!error && transaction_type.equals("22") && !putaway_type.equals("0")){
                    validationErrors
                            .add("invalid putaway code for transaction-putaway type 22-[" + putaway_type + "]");
                    alert("seq [%s]: invalid putaway code for transaction-putaway type 22-[" + putaway_type + "]",seq);
                    error = true;
                }
                else if (!error && (transaction_type.equals("23") || transaction_type.equals("24")) && !putaway_type.equals("0")) {
                    validationErrors
                            .add("invalid putaway code for transaction-putaway type (23-24)-[" + putaway_type + "]");
                    alert("seq [%s]: invalid putaway code for transaction-putaway type (23-24)-[" + putaway_type + "]",seq);
                    error = true;
                }
                else if (!error && !isNumeric(operator_id)) { // SR: this can be better by checking ackByGeek on proOperators
                    validationErrors
                            .add("invalid operator ID ["+operator_id+"]");
                    alert("seq [%s]: invalid operator ID ["+operator_id+"]",seq);
                    error = true;
                }
                seq = padZeros(seq, 10);
                transaction_type = padZeros(transaction_type, 2);
                putaway_type = padZeros(putaway_type, 1);
                putaway_replen_code = padZeros(putaway_replen_code, 12);
                putaway_code = padZeros(putaway_code, 8);
                part_number = padZeros(part_number, 7);
                quantity_change = padZeros(quantity_change, 7);
                operator_id = padZeros(operator_id, 5);


                trace("    transaction_type    : [%s]", transaction_type);
                trace("    putaway_type        : [%s]", putaway_type);
                trace("    putaway_replen_code : [%s]", putaway_replen_code);
                trace("    putaway_code        : [%s]", putaway_code);
                trace("    part_number         : [%s]", part_number);
                trace("    quantity_change     : [%s]", quantity_change);
                trace("    operator_id         : [%s]", operator_id);

                if(!error){
                    String finalTransaction_type = transaction_type;
                    String finalPart_number = part_number;
                    String finalPutaway_replen_code = putaway_replen_code;
                    String finalPutaway_code = putaway_code;
                    String finalPutaway_type = putaway_type;
                    String finalQuantity_change = quantity_change;
                    String finalOperator_id = operator_id;
                    String finalSeq = seq;
                    fieldsList.addAll(new ArrayList<String>(8) {
                        {
                            add(finalTransaction_type);
                            add(finalPart_number);
                            add(finalPutaway_replen_code);
                            add(finalPutaway_code);
                            add(finalPutaway_type);
                            add(finalQuantity_change);
                            add(finalOperator_id);
                            add(finalSeq);
                        }
                    });

                    linesList.add(fieldsList);
                    rowsUpdated = geekDao.updateProcessed("rdsGeekInventoryAdjustments", "yes", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsGeekInventoryAdjustments.seq[%d] as processed = 'yes'",Integer.parseInt(seq));
                    }
                }else{
                    rowsUpdated = geekDao.updateProcessed("rdsGeekInventoryAdjustments", "error", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsGeekInventoryAdjustments.seq[%d] as processed = 'error'",Integer.parseInt(seq));
                    }
                }

            }

        }else{
            validationErrors.add("no geek inventory adjustments records found");// no records found, should not
            // proceed to persist
            trace("no geek inventory adjustments records found");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateOperatorActivity() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
        GeekDAO geekDao = new GeekDAO();
        String seq;
        String operator_id;
        String type_code;
        String operator_task;
        String start_time;
        String end_time;


        List<Map<String, String>> unprocessedMapList = geekDao.getAllProcessedOrdered("rdsOperatorActivity", "no","createStamp","ASC");
//        trace(unprocessedMapList.toString()+"");
        if(unprocessedMapList!= null && !unprocessedMapList.isEmpty()){
            for (int i = 0; i < unprocessedMapList.size(); i++) {
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                Map<String, String> unprocessedMap = unprocessedMapList.get(i);

                seq = unprocessedMap.get("seq");
                int rowsUpdated = geekDao.updateProcessed("rdsOperatorActivity", "processing", Integer.parseInt(seq));
                if(rowsUpdated != 1){
                    alert("error while setting rdsOperatorActivity.seq[%d] as processed = 'processing'",Integer.parseInt(seq));
                }
                operator_id = unprocessedMap.get("operator_id");
                operator_task = unprocessedMap.get("operator_task");
                type_code = unprocessedMap.get("type_code");
                start_time = unprocessedMap.get("startStamp");
                end_time = unprocessedMap.get("endStamp");

                if (!error && !isNumeric(operator_id)) {
                    validationErrors
                            .add("invalid operator ID ["+operator_id+"]");
                    alert("seq [%s]: invalid operator ID ["+operator_id+"]",seq);
                    error = true;
                }

                start_time = start_time.replace("-","").replace(" ","").replace(":","");
                if(end_time!=null && !end_time.isEmpty()){
                    end_time = end_time.replace("-","").replace(" ","").replace(":","");
                }else{
                    end_time = "";
                }

                type_code = padZeros(type_code, 2);
                operator_task = padZeros(operator_task, 2);
                operator_id = padZeros(operator_id, 5);
                start_time = padZeros(start_time, 14);
                end_time = padZeros(end_time, 14);

                trace("seq              : [%s]", seq);
                trace("    operator_id      : [%s]", operator_id);
                trace("    type_code        : [%s]", type_code);
                trace("    operator_task    : [%s]", operator_task);
                trace("    start_time       : [%s]", start_time);
                trace("    end_time         : [%s]", end_time);

                if(!error){
                    String finalType_code = type_code;
                    String finalOperator_task = operator_task;
                    String finalOperator_id = operator_id;
                    String finalStart_time = start_time;
                    String finalEnd_time = end_time;
                    fieldsList.addAll(new ArrayList<String>(5) {
                        {
                            add(finalType_code);
                            add(finalOperator_task);
                            add(finalOperator_id);
                            add(finalStart_time);
                            add(finalEnd_time);
                        }
                    });

                    linesList.add(fieldsList);
                    rowsUpdated = geekDao.updateProcessed("rdsOperatorActivity", "yes", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsOperatorActivity.seq[%d] as processed = 'yes'",Integer.parseInt(seq));
                    }
                }else{
                    alert("marking rdsOperatorActivity.seq[%s] as 'error'",seq);
                    rowsUpdated = geekDao.updateProcessed("rdsOperatorActivity", "error", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsOperatorActivity.seq[%d] as processed = 'error'",Integer.parseInt(seq));
                    }
                }

            }

        }else{
            validationErrors.add("no operator activity records found");// no records found, should not
            // proceed to persist
            trace("no operator activity records found");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateOrderLineConfirmations() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
        GeekDAO geekDao = new GeekDAO();
        List<Map<String, String>>  unprocessedMapList = geekDao.getAllProcessedOrdered("rdsOrderLineConfirmationUpload", "no","createStamp","ASC");
//        trace(unprocessedMapList.toString()+"");
        if (unprocessedMapList != null && !unprocessedMapList.isEmpty()) {

            String orderLineSeq = ""; // assign value later
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String qtyOrdered = "";
            String changedQtyFlag = "";
            String qtyPicked = "";
            String numOfTotes = "";
            String operatorId = "";
            String QPAGroup = "";

            for (int i = 0; i < unprocessedMapList.size(); i++) {
                Map<String,String> unprocessedMap = null;
                String seq = "-1";
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                unprocessedMap = unprocessedMapList.get(i);
                seq = unprocessedMap.get("seq");
                int rowsUpdated = geekDao.updateProcessed("rdsOrderLineConfirmationUpload", "processing", Integer.parseInt(seq));
                if(rowsUpdated != 1){
                    alert("error while setting rdsOrderLineConfirmationUpload.seq[%d] as processed = 'processing'",Integer.parseInt(seq));
                }
                orderLineSeq = unprocessedMap.get("orderLineSeq");
                trace("upload conf rdsOrderLineConfirmationUpload.seq : [%s]", seq);

                typeCode = getMapStr(unprocessedMap, "typeCode");
                batchId = getMapStr(unprocessedMap, "batchId");
                groupNumber = getMapStr(unprocessedMap, "groupNumber");
                orderNumber = getMapStr(unprocessedMap, "orderNumber");
                pageNumber = getMapStr(unprocessedMap, "pageNumber");
                lineNumber = getMapStr(unprocessedMap, "lineNumber");
                qtyOrdered = getMapStr(unprocessedMap, "qtyOrdered");
                changedQtyFlag = getMapStr(unprocessedMap, "changedQtyFlag");
                qtyPicked = getMapStr(unprocessedMap, "qtyPicked");
                numOfTotes = getMapStr(unprocessedMap, "numOfTotes");
                operatorId = getMapStr(unprocessedMap, "operatorId");
                QPAGroup = getMapStr(unprocessedMap, "QPAGroup");

                typeCode = padZeros(typeCode, 2);
                batchId = padZeros(batchId, 7);
                groupNumber = padZeros(groupNumber, 7);
                orderNumber = padZeros(orderNumber, 7);
                pageNumber = padZeros(pageNumber, 7);
                lineNumber = padZeros(lineNumber, 7);
                qtyOrdered = padZeros(qtyOrdered, 7);
                changedQtyFlag = padZeros(changedQtyFlag, 1);
                qtyPicked = padZeros(qtyPicked, 7);
                numOfTotes = padZeros(numOfTotes, 7);
                operatorId = padZeros(operatorId, 5);
                QPAGroup = padZeros(QPAGroup, 3);

                trace("    typeCode             : [%s]", typeCode);
                trace("    batchId              : [%s]", batchId);
                trace("    groupNumber          : [%s]", groupNumber);
                trace("    orderNumber          : [%s]", orderNumber);
                trace("    pageNumber           : [%s]", pageNumber);
                trace("    lineNumber           : [%s]", lineNumber);
                trace("    qtyOrdered           : [%s]", qtyOrdered);
                trace("    changeQtyFlag        : [%s]", changedQtyFlag);
                trace("    qtyPicked            : [%s]", qtyPicked);
                trace("    numOfTotes           : [%s]", numOfTotes);
                trace("    operatorId           : [%s]", operatorId);
                trace("    QPAGroup             : [%s]", QPAGroup);

                if (!error) {
                    String finalTypeCode = typeCode;
                    String finalBatchId = batchId;
                    String finalGroupNumber = groupNumber;
                    String finalOrderNumber = orderNumber;
                    String finalPageNumber = pageNumber;
                    String finalLineNumber = lineNumber;
                    String finalQtyOrdered = qtyOrdered;
                    String finalChangedQtyFlag = changedQtyFlag;
                    String finalQtyPicked = qtyPicked;
                    String finalNumOfTotes = numOfTotes;
                    String finalOperatorId = operatorId;
                    String finalQPAGroup = QPAGroup;
                    fieldsList.addAll(new ArrayList<String>(12) {
                        {
                            add(finalTypeCode);
                            add(finalBatchId);
                            add(finalGroupNumber);
                            add(finalOrderNumber);
                            add(finalPageNumber);
                            add(finalLineNumber);
                            add(finalQtyOrdered);
                            add(finalChangedQtyFlag);
                            add(finalQtyPicked);
                            add(finalNumOfTotes);
                            add(finalOperatorId);
                            add(finalQPAGroup);
                        }
                    });
                    linesList.add(fieldsList);
                    rowsUpdated = geekDao.updateProcessed("rdsOrderLineConfirmationUpload", "yes", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsOrderLineConfirmationUpload.seq[%d] as processed = 'yes'",Integer.parseInt(seq));
                    }
                } else {
                    alert("error during custOrderLines.orderLineSeq[%s], skipping the line", orderLineSeq);
                    continue;
                }

            }

        } else {
            validationErrors.add("no order lines confirmation found");// no records found, should not
            // proceed to persist
            trace("no order lines confirmation found");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateToteContentsActuals() throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        GeekDAO geekDao = new GeekDAO();
        RdsToteContentsUploadDAO rdsToteContentsUploadDAO = new RdsToteContentsUploadDAO();
        List<Map<String,String>> unprocessedMapList = rdsToteContentsUploadDAO.getAllUnprocessed();
//        trace(unprocessedMapList.toString()+"");
        if (unprocessedMapList != null && !unprocessedMapList.isEmpty()) {

            String seq = "-1";
            String cartonSeq = "0";
            String orderLineSeq = "";
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String lineSeq = "";       // SR: in      scope since 06/10/2024
            String toteNumber = "";
            String totalTotes = "";
            String finalFlag = "";
            String cartonLpn = "";
            String toteSeqNumber = "";
            String origin = "";

            for (int i = 0; i < unprocessedMapList.size(); i++) {
                boolean orderLineError = false;
                Map<String,String> unprocessedMap = unprocessedMapList.get(i);
                seq = getMapStr(unprocessedMap,"seq");
                String lpn = getMapStr(unprocessedMap,"cartonLpn");
                trace("tote contents upload cartonLpn : [%s]", lpn);

                List<String> fieldsList = new ArrayList<>();

                typeCode = getMapStr(unprocessedMap,"typeCode");
                orderLineSeq = getMapStr(unprocessedMap,"orderLineSeq");
                batchId = getMapStr(unprocessedMap, "batchId");
                groupNumber = getMapStr(unprocessedMap, "groupNumber");
                orderNumber = getMapStr(unprocessedMap, "orderNumber");
                pageNumber = getMapStr(unprocessedMap, "pageNumber");
                lineNumber = getMapStr(unprocessedMap, "lineNumber");
                lineSeq = getMapStr(unprocessedMap, "lineSeq");       // SR: in      scope since 06/10/2024
                toteNumber = getMapStr(unprocessedMap, "toteNumber");
                totalTotes = getMapStr(unprocessedMap, "totalTotes");
                finalFlag = getMapStr(unprocessedMap, "finalFlag");
                cartonLpn = getMapStr(unprocessedMap, "cartonLpn");
                toteSeqNumber = getMapStr(unprocessedMap, "toteSeqNumber");
                origin = getMapStr(unprocessedMap, "origin");

                typeCode = padZeros(typeCode, 2);
                batchId = padZeros(batchId, 7);
                groupNumber = padZeros(groupNumber, 7);
                orderNumber = padZeros(orderNumber, 7);
                pageNumber = padZeros(pageNumber, 7);
                lineNumber = padZeros(lineNumber, 7);
                lineSeq = padZeros(lineSeq, 7);       // SR: in      scope since 06/10/2024
                toteNumber = padZeros(toteNumber, 7);
                totalTotes = padZeros(totalTotes, 7);
                finalFlag = padZeros(finalFlag, 1);
                cartonLpn = padZeros(cartonLpn, 9);
                toteSeqNumber = padZeros(toteSeqNumber, 4);
                origin = padZeros(origin, 2);

                trace("cartonLpn                : [%s]", cartonLpn);
                trace("    typeCode             : [%s]", typeCode);
                trace("    batchId              : [%s]", batchId);
                trace("    groupNumber          : [%s]", groupNumber);
                trace("    lineSeq              : [%s]", lineSeq);       // SR: in      scope since 06/10/2024
                trace("    orderNumber          : [%s]", orderNumber);
                trace("    pageNumber           : [%s]", pageNumber);
                trace("    lineNumber           : [%s]", lineNumber);
                trace("    toteNumber           : [%s]", toteNumber);
                trace("    totalTotes           : [%s]", totalTotes);
                trace("    finalFlag            : [%s]", finalFlag);
                trace("    cartonLpn            : [%s]", cartonLpn);
                trace("    toteSeqNumber        : [%s]", toteSeqNumber);
                trace("    origin               : [%s]", origin);

                if (!orderLineError) {
                    String finalTypeCode = typeCode;
                    String finalBatchId = batchId;
                    String finalGroupNumber = groupNumber;
                    String finalLineSeq = lineSeq;       // SR: in      scope since 06/10/2024
                    String finalOrderNumber = orderNumber;
                    String finalPageNumber = pageNumber;
                    String finalLineNumber = lineNumber;
                    String finalToteNumber= toteNumber;
                    String finalTotalTotes = totalTotes;
                    String finalFinalFlag = finalFlag;
                    String finalCartonLpn = cartonLpn;
                    String finalToteSeqNumber = toteSeqNumber;
                    String finalOrigin = origin;
                    fieldsList.addAll(new ArrayList<String>(12) {
                        {
                            add(finalTypeCode);
                            add(finalBatchId);
//                            add(finalGroupNumber); // SR: out of  scope since 06/10/2024
                            add(finalLineSeq);       // SR: in      scope since 06/10/2024
                            add(finalOrderNumber);
                            add(finalPageNumber);
                            add(finalLineNumber);
                            add(finalToteNumber);
                            add(finalTotalTotes);
                            add(finalFinalFlag);
                            add(finalCartonLpn);
                            add(finalToteSeqNumber);
                            add(finalOrigin);
                        }
                    });
                    linesList.add(fieldsList);
                    int rowsUpdated = geekDao.updateProcessed("rdsToteContentsUpload", "yes", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsToteContentsUpload.seq[%d] as processed = 'yes'",Integer.parseInt(seq));
                    }
                } else {
                    alert("error during custOrderLine.orderLineSeq[%s], skipping the line", orderLineSeq);
                    continue;
                }

            }

        } else {
            validationErrors.add("no actual tote contents to upload");// no records found, should not
            // proceed to persist
            trace("no actual tote contents to upload");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateTotesPerOrderActuals() throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        GeekDAO geekDao = new GeekDAO();
        RdsTotesPerOrderUploadDAO rdsTotesPerOrderUploadDAO = new RdsTotesPerOrderUploadDAO();
        List<Map<String,String>> unprocessedMapList = rdsTotesPerOrderUploadDAO.getAllUnprocessed();
//        trace(unprocessedMapList.toString()+"");
        if (unprocessedMapList != null && !unprocessedMapList.isEmpty()) {

            String seq = "-1";
            String orderId = "0";
            String typeCode = "";
            String finalFlag = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String totalTotes = "";

            for (int i = 0; i < unprocessedMapList.size(); i++) {
                boolean orderError = false;
                Map<String,String> unprocessedMap = unprocessedMapList.get(i);
                seq = getMapStr(unprocessedMap, "seq");
                int rowsUpdated = geekDao.updateProcessed("rdsTotesPerOrderUpload", "processing", Integer.parseInt(seq));
                if(rowsUpdated != 1){
                    alert("error while setting rdsTotesPerOrderUpload.seq[%d] as processed = 'processing'",Integer.parseInt(seq));
                }
                orderId = getMapStr(unprocessedMap, "orderNumber");
                trace("actual totes per order for order : [%s]", orderId);

                List<String> fieldsList = new ArrayList<>();

                typeCode = getMapStr(unprocessedMap, "typeCode");
                finalFlag = getMapStr(unprocessedMap, "finalFlag");
                batchId = getMapStr(unprocessedMap, "batchId");
                groupNumber = getMapStr(unprocessedMap, "groupNumber");
                orderNumber = getMapStr(unprocessedMap, "orderNumber");
                totalTotes = getMapStr(unprocessedMap, "totalTotes");

                typeCode = padZeros(typeCode, 2);
                finalFlag = padZeros(finalFlag, 1);
                batchId = padZeros(batchId, 7);
                groupNumber = padZeros(groupNumber, 7);
                orderNumber = padZeros(orderNumber, 7);
                totalTotes = padZeros(totalTotes, 7);

                trace("orderId             : [%s]", orderId);
                trace("    typeCode             : [%s]", typeCode);
                trace("    batchId              : [%s]", batchId);
                trace("    groupNumber          : [%s]", groupNumber);
                trace("    orderNumber          : [%s]", orderNumber);
                trace("    totalTotes           : [%s]", totalTotes);
                trace("    finalFlag            : [%s]", finalFlag);

                if (!orderError) {
                    String finalTypeCode = typeCode;
                    String finalBatchId = batchId;
                    String finalGroupNumber = groupNumber;
                    String finalOrderNumber = orderNumber;
                    String finalTotalTotes = totalTotes;
                    String finalFinalFlag = finalFlag;
                    fieldsList.addAll(new ArrayList<String>(6) {
                        {
                            add(finalTypeCode);
                            add(finalFinalFlag);
                            add(finalBatchId);
                            add(finalGroupNumber);
                            add(finalOrderNumber);
                            add(finalTotalTotes);
                        }
                    });
                    linesList.add(fieldsList);
                    rowsUpdated = geekDao.updateProcessed("rdsTotesPerOrderUpload", "yes", Integer.parseInt(seq));
                    if(rowsUpdated != 1){
                        alert("error while setting rdsTotesPerOrderUpload.seq[%d] as processed = 'yes'",Integer.parseInt(seq));
                    }

                } else {
                    alert("error during custOrders.orderId[%s], skipping the record", orderId);
                    continue;
                }

            }

        } else {
            validationErrors.add("no actual totes per order to upload");// no records found, should not
            // proceed to persist
            trace("no actual totes per order to upload");
        }
        return validationErrors; // this return is not used anymore
    }

    @Override
    public void persist() throws DataAccessException {

        try {
            if(linesList.size() > 0){
                fWriter.writeLine(linesList, "ACTUALS_FILE"); // temporarily stopped
//                fWriter.writeLine(linesList, "ACTUALS_FILE_TEST"); // for the test on 03/22
                linesList = new ArrayList<>();
            }else{
                trace("no records found");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
