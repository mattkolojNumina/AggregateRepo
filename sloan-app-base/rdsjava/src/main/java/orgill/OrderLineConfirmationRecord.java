package sloane;

import dao.*;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;
import records.RdsOrderLineConfirmationUpload;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class OrderLineConfirmationRecord extends AbstractDAO implements FileRecord {

    FileWatcher fw;
//    FileWriter fWriter;

    public OrderLineConfirmationRecord(FileWatcher fw) {
        this.fw = fw;
//        fWriter = new FileWriter(fw);
    }

    List<List<String>> linesList = new ArrayList<>();

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
   	  linesList = new ArrayList<>();
        CustOrderLineDAO custOrderLineDAO = new CustOrderLineDAO();
        RdsOrderLineConfirmationUpload rdsOrderLineConfrimationUpload = new RdsOrderLineConfirmationUpload();
        RdsOrderLineConfirmationUploadDAO rdsOrderLineConfrimationUploadDAO = new RdsOrderLineConfirmationUploadDAO();

        List<String> unprocessedList = new ArrayList<>();

        unprocessedList = custOrderLineDAO.getLineConfirmationToUpload();
        if (unprocessedList != null && !unprocessedList.isEmpty()) {

            String orderLineSeq = "0";
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String qtyOrdered = "";
            String changeQtyFlag = "";
            String qtyPicked = "";
            String numOfTotes = "";
            String operatorId = "";
            String lineQPAGroup = "";

            for (int i = 0; i < unprocessedList.size(); i++) {
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                orderLineSeq = unprocessedList.get(i);
                trace("upload conf orderLineSeq : [%s]", orderLineSeq);

                Map<String, String> custOrderLinesMap = getTableRowByIntId("custOrderLines", "orderLineSeq", Integer.parseInt(orderLineSeq));
                String orderId = getMapStr(custOrderLinesMap, "orderId");
                Map<String, String> custOrdersMap = getTableRowByIntId("custOrders", "orderId", Integer.parseInt(orderId));
                int waveSeq = getMapInt(custOrdersMap, "waveSeq");
                Map<String, String> rdsWavesMap = getTableRowByIntId("rdsWaves", "waveSeq", waveSeq);
                String waveName = getMapStr(rdsWavesMap, "waveName");

                typeCode = "12";
                batchId = waveName;
                groupNumber = getMapStr(custOrderLinesMap, "groupNumber");
                orderNumber = getMapStr(custOrderLinesMap, "orderId");
                pageNumber = getMapStr(custOrderLinesMap, "pageId");
                lineNumber = getMapStr(custOrderLinesMap, "lineId");
                qtyOrdered = String.valueOf((int)(Float.parseFloat(getMapStr(custOrderLinesMap, "qty"))));
                changeQtyFlag = getMapStr(custOrderLinesMap, "qtyChanged").equals("1") ? "Y" : "N";
                qtyPicked = String.valueOf((int)(Float.parseFloat(getMapStr(custOrderLinesMap, "labeledQty"))));
                numOfTotes = custOrderLineDAO.getNumOfTotesFromOrderLine(Integer.parseInt(orderLineSeq)) + "";
                operatorId = custOrderLineDAO.getPickOperatorIdFromOrderLine(Integer.parseInt(orderLineSeq)) + "";
                lineQPAGroup = getMapStr(custOrderLinesMap, "lineQPAGroup");

                typeCode = padZeros(typeCode, 2);
                batchId = padZeros(batchId, 7);
                groupNumber = padZeros(groupNumber, 7);
                orderNumber = padZeros(orderNumber, 7);
                pageNumber = padZeros(pageNumber, 7);
                lineNumber = padZeros(lineNumber, 7);
                qtyOrdered = padZeros(qtyOrdered, 7);
                changeQtyFlag = padZeros(changeQtyFlag, 1);
                qtyPicked = padZeros(qtyPicked, 7);
                numOfTotes = padZeros(numOfTotes, 7);
                operatorId = padZeros(operatorId, 5);
                lineQPAGroup = padZeros(lineQPAGroup, 3);

                trace("    typeCode             : [%s]", typeCode);
                trace("    batchId              : [%s]", batchId);
                trace("    groupNumber          : [%s]", groupNumber);
                trace("    orderNumber          : [%s]", orderNumber);
                trace("    pageNumber           : [%s]", pageNumber);
                trace("    lineNumber           : [%s]", lineNumber);
                trace("    qtyOrdered           : [%s]", qtyOrdered);
                trace("    changeQtyFlag        : [%s]", changeQtyFlag);
                trace("    qtyPicked            : [%s]", qtyPicked);
                trace("    numOfTotes           : [%s]", numOfTotes);
                trace("    operatorId           : [%s]", operatorId);
                trace("    lineQPAGroup         : [%s]", lineQPAGroup);

                if (!error) {
                    String finalTypeCode = typeCode;
                    String finalBatchId = batchId;
                    String finalGroupNumber = groupNumber;
                    String finalOrderNumber = orderNumber;
                    String finalPageNumber = pageNumber;
                    String finalLineNumber = lineNumber;
                    String finalQtyOrdered = qtyOrdered;
                    String finalChangeQtyFlag = changeQtyFlag;
                    String finalQtyPicked = qtyPicked;
                    String finalNumOfTotes = numOfTotes;
                    String finalOperatorId = operatorId;
                    String finalLineQPAGroup = lineQPAGroup;
                    fieldsList.addAll(new ArrayList<String>(12) {
                        {
                            add(finalTypeCode);
                            add(finalBatchId);
                            add(finalGroupNumber);
                            add(finalOrderNumber);
                            add(finalPageNumber);
                            add(finalLineNumber);
                            add(finalQtyOrdered);
                            add(finalChangeQtyFlag);
                            add(finalQtyPicked);
                            add(finalNumOfTotes);
                            add(finalOperatorId);
                            add(finalLineQPAGroup);
                        }
                    });

                    rdsOrderLineConfrimationUpload.setTypeCode(finalTypeCode);
                    rdsOrderLineConfrimationUpload.setBatchId(finalBatchId);
                    rdsOrderLineConfrimationUpload.setGroupNumber(finalGroupNumber);
                    rdsOrderLineConfrimationUpload.setOrderNumber(finalOrderNumber);
                    rdsOrderLineConfrimationUpload.setPageNumber(finalPageNumber);
                    rdsOrderLineConfrimationUpload.setLineNumber(finalLineNumber);
                    rdsOrderLineConfrimationUpload.setQtyOrdered(finalQtyOrdered);
                    rdsOrderLineConfrimationUpload.setChangedQtyFlag(finalChangeQtyFlag);
                    rdsOrderLineConfrimationUpload.setQtyPicked(finalQtyPicked);
                    rdsOrderLineConfrimationUpload.setNumOfTotes(finalNumOfTotes);
                    rdsOrderLineConfrimationUpload.setOperatorId(finalOperatorId);
                    rdsOrderLineConfrimationUpload.setQpaGroup(finalLineQPAGroup);

                    if(! (1 == rdsOrderLineConfrimationUploadDAO.save(rdsOrderLineConfrimationUpload))){
                        alert("error inserting into rdsToteContentsUpload, orderLineSeq [%s]",orderLineSeq);
                    }else{
                        AbstractDAO.updateRowByIntIdTimeStampValue("custOrderLines","uploadStamp","now()","orderLineSeq",Integer.parseInt(orderLineSeq));
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

    @Override
    public void persist() throws DataAccessException {
//        try {
            if (linesList.size() > 0) {
          	  linesList = new ArrayList<>();
//                fWriter.writeLine(linesList, "ORDER_LINE_CONFIRMATION");
            } else {
                trace("no records found");
            }
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


}
