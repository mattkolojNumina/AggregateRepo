package sloane;

import dao.*;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rds.RDSDatabase;

import static dao.SloaneCommonDAO.padZeros;
import static sloane.SloaneConstants.*;
import static rds.RDSUtil.*;

public class CombinedEstimatesRecord extends AbstractDAO implements FileRecord {

    FileWatcher fw;
    FileWriter fWriter;

    public CombinedEstimatesRecord(FileWatcher fw){
        this.fw = fw;
        fWriter = new FileWriter(fw);
    }



    List<List<String>> linesList = new ArrayList<>();

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
        linesList = new ArrayList<>();
        int fileSeq = CustOutboundOrderFileDAO.getUnprocessedFile();
        //int fileSeq = db.getInt(-1,"SELECT fileSeq FROM custOutboundOrderFiles WHERE cartonizeStamp IS NOT NULL AND cartonizedUploadStamp IS NULL LIMIT 1");
        if(fileSeq > 0){
            validationErrors.addAll(validateToteContentsEstimates(fileSeq)); // 6.3 Estimates
            validationErrors.addAll(validateTotesPerOrderEstimates(fileSeq)); // 7.1 Estimates

            CustOutboundOrderFileDAO.setTombstone(fileSeq, "cartonizedUploadStamp");
            //db.execute("UPDATE custOutboundOrderFiles SET cartonizedUploadStamp = NOW() WHERE fileSeq = %d",fileSeq);
            inform("Estimates for custOutboundOrderFiles.fileSeq [%d] are sent",fileSeq);

        }

        return validationErrors;
    }

    public List<String> validateToteContentsEstimates(int fileSeq) throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        RdsToteContentsUploadDAO rdsToteContentsUploadDAO = new RdsToteContentsUploadDAO();

        //get the list of cartonSeqs to upload
        List<String> unprocessedList = rdsToteContentsUploadDAO.getAllUnprocessedEstimates(fileSeq);

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

            String cartonSeq = "0";
            String orderLineSeq = "";
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String lineSeq = ""; // new field
            String toteNumber = "";
            String totalTotes = "";
            String finalFlag = "";
            String cartonLpn = "";
            String toteSeqNumber = "";
            String origin = "";

            for (int i = 0; i < unprocessedList.size(); i++) {
                boolean cartonError = false;
                cartonSeq = unprocessedList.get(i);
                trace("estimated tote contents upload, cartonSeq : [%s]", cartonSeq);

                //get the list of maps with details of order lines which belong to that cartonSeq
                List<Map<String, String>> unprocessedLinesMapList = rdsToteContentsUploadDAO.getEstimatedOrderLinesPerToteToUpload(Integer.parseInt(cartonSeq));

                boolean orderLineError = false;
                for (int j = 0; j < unprocessedLinesMapList.size(); j++) {
                    orderLineError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedLinesMapList.get(j);

                    typeCode = "18";
                    if( j == 0 ){ typeCode = "17"; }
                    orderLineSeq = getMapStr(unprocessedLinesMap,"orderLineSeq");
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    lineSeq = getMapStr(unprocessedLinesMap, "lineNumber"); // new field
                    orderNumber = getMapStr(unprocessedLinesMap, "orderId");
                    pageNumber = getMapStr(unprocessedLinesMap, "pageId");
                    lineNumber = getMapStr(unprocessedLinesMap, "lineId");
                    toteNumber = getMapStr(unprocessedLinesMap, "cartonCount");
                    totalTotes = rdsToteContentsUploadDAO.totalEstimatedTotesByOrder(orderNumber)+"";
                    finalFlag = "N";
                    cartonLpn = getMapStr(unprocessedLinesMap, "lpn");
                    toteSeqNumber = getMapStr(unprocessedLinesMap, "cartonCount");
                    origin = getMapStr(unprocessedLinesMap, "pickType");
                    if(     origin.equals(PICKTYPE_PERISHABLES)
                            ||  origin.equals(PICKTYPE_AERSOLBOOM)
                            ||  origin.equals(PICKTYPE_LIQUIDS)){
                        origin = "3";
                    }else if(origin.equals(PICKTYPE_GEEK)){
                        origin = "2";
                    }else if(origin.equals(PICKTYPE_ZONEROUTE)){
                        origin = "1";
                    }else{
                        orderLineError = true;
                        alert("error during cartonSeq: [%s], [%d]th line, skipping the line", cartonSeq, j+1);
                        continue;
                    }

                    typeCode = padZeros(typeCode, 2);
                    batchId = padZeros(batchId, 7);
                    groupNumber = padZeros(groupNumber, 7);
                    lineSeq = padZeros(lineSeq, 7); // new field
                    orderNumber = padZeros(orderNumber, 7);
                    pageNumber = padZeros(pageNumber, 7);
                    lineNumber = padZeros(lineNumber, 7);
                    toteNumber = padZeros(toteNumber, 7);
                    totalTotes = padZeros(totalTotes, 7);
                    finalFlag = padZeros(finalFlag, 1);
                    cartonLpn = padZeros(cartonLpn, 9);
                    toteSeqNumber = padZeros(toteSeqNumber, 4);
                    origin = padZeros(origin, 2);

                    trace("cartonSeq: [%s], [%d]th line", cartonSeq, j+1);
                    trace("orderLineSeq             : [%s]", orderLineSeq);
                    trace("    typeCode             : [%s]", typeCode);
                    trace("    batchId              : [%s]", batchId);
                    trace("    groupNumber          : [%s]", groupNumber);
                    trace("    lineSeq              : [%s]", lineSeq); // new field
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
                        String finalLineSeq = lineSeq; // new field
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
//                                add(finalGroupNumber); //SR: old field, go replaced with finalLineSeq
                                add(finalLineSeq);  //SR: new field
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

                        //add field list to line list
//                        if( !finalTypeCode.equals("18") ){ // SR: 18 not in scope yet :03/22/2024 // SR: now it is in scope :06/10/2024
                            linesList.add(fieldsList);
//                        }

                    } else {
                        alert("error during processing custOrderLine.orderLineSeq[%s], skipping the line", orderLineSeq);
                        continue;
                    }
                }
                AbstractDAO.updateRowByIntIdTimeStampValue("rdsCartons","estContentsUploadStamp","now()","cartonSeq",Integer.parseInt(cartonSeq));
                trace("\n");
            }

        } else {
            validationErrors.add("no estimated tote contents to upload");// no records found, should not
            // proceed to persist
            trace("no estimated tote contents to upload");
        }
        return validationErrors; // this return is not used anymore
    }
    public List<String> validateTotesPerOrderEstimates(int fileSeq) throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        RdsTotesPerOrderUploadDAO rdsTotesPerOrderUploadDAO = new RdsTotesPerOrderUploadDAO();

        // gets the list of orderIds which needs totes per order upload
        List<String> unprocessedList = rdsTotesPerOrderUploadDAO.getAllUnprocessedEstimates(fileSeq);

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

            String seq = "-1";
            String orderId = "0";
            String typeCode = "";
            String finalFlag = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String totalTotes = "";

            for (int i = 0; i < unprocessedList.size(); i++) {

                boolean orderError = false;
                orderId = unprocessedList.get(i);
                trace("estimated totes per order for order : [%s]", orderId);

                // gets data for the order that needs to be uploaded
                List<Map<String, String>> unprocessedDetailsMapList = rdsTotesPerOrderUploadDAO.getTotesPerOrderToUploadDetails(orderId);

                if(unprocessedDetailsMapList.size() != 1){
                    alert("error getting details, should have just found 1 record, found [%d] for custOrders.orderId[%s], skipping the record",unprocessedDetailsMapList.size(),  orderId);
                    orderError = true;
                    continue;
                }

                boolean orderDetailError = false;
                for (int j = 0; j < unprocessedDetailsMapList.size(); j++) {
                    orderDetailError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedDetailsMapList.get(j);

                    typeCode = "10";
                    finalFlag = "N";
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    orderNumber = orderId;
                    totalTotes = rdsTotesPerOrderUploadDAO.totalEstimatedTotesByOrder(orderNumber)+"";

                    typeCode = padZeros(typeCode, 2);
                    finalFlag = padZeros(finalFlag, 1);
                    batchId = padZeros(batchId, 7);
                    groupNumber = padZeros(groupNumber, 7);
                    orderNumber = padZeros(orderNumber, 7);
                    totalTotes = padZeros(totalTotes, 7);

                    trace("orderID [%s], [%d]th interation", orderId, j);
                    trace("    typeCode             : [%s]", typeCode);
                    trace("    batchId              : [%s]", batchId);
                    trace("    groupNumber          : [%s]", groupNumber);
                    trace("    orderNumber          : [%s]", orderNumber);
                    trace("    totalTotes           : [%s]", totalTotes);
                    trace("    finalFlag            : [%s]", finalFlag);

                    if (!orderDetailError) {
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

                    } else {
                        alert("error during custOrders.orderId[%s], skipping the record", orderId);
                        continue;
                    }
                }
                AbstractDAO.updateRowByStringIdTimeStampValue("custOrders","cartonizedUploadStamp","now()","orderId",orderId);
                trace("\n");

            }

        } else {
            validationErrors.add("no estimated totes per order to upload");// no records found, should not
            // proceed to persist
            trace("no estimated totes per order to upload");
        }
        return validationErrors; // this return is not used anymore
    }

    @Override
    public void persist() throws DataAccessException {

        try {
            if(linesList.size() > 0){
                fWriter.writeLine(linesList, "ESTIMATES_FILE");
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
