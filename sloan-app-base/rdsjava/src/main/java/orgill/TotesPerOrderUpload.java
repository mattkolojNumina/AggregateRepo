package sloane;

import dao.*;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;
import records.RdsTotesPerOrderUpload;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static sloane.SloaneConstants.*;
import static rds.RDSUtil.*;

public class TotesPerOrderUpload extends AbstractDAO implements FileRecord {

    FileWatcher fw;
//    FileWriter fWriter;

    public TotesPerOrderUpload(FileWatcher fw) {
        this.fw = fw;
//        fWriter = new FileWriter(fw);
    }

    List<List<String>> linesList = new ArrayList<>();

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
   	  linesList = new ArrayList<>();

//        validationErrors.addAll(validateEstimates()); // finalFlag = N
        validationErrors.addAll(validateActuals()); // finalFlag = Y

        return validationErrors; // this return is not used anymore
    }

    public List<String> validateEstimates() throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        CustOrderDAO custOrderDAO = new CustOrderDAO();
        RdsCartonsDAO rdsCartonsDAO = new RdsCartonsDAO();
        RdsTotesPerOrderUploadDAO rdsTotesPerOrderUploadDAO = new RdsTotesPerOrderUploadDAO();
        RdsTotesPerOrderUpload rdsTotesPerOrderUpload = new RdsTotesPerOrderUpload();
        GeekDAO geekDAO = new GeekDAO();

        List<String> unprocessedList = new ArrayList<>();
        unprocessedList = custOrderDAO.getEstimatedTotesPerOrderToUpload();

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

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

                List<Map<String, String>> unprocessedDetailsMapList = new ArrayList<>();
                unprocessedDetailsMapList = custOrderDAO.getTotesPerOrderToUploadDetails(orderId);

                if(unprocessedDetailsMapList.size() != 1){
                    alert("error getting details, should have just found 1 record, found [%d] custOrders.orderId[%s], skipping the record",unprocessedDetailsMapList.size(),  orderId);
                    continue;
                }

                for (int j = 0; j < unprocessedDetailsMapList.size(); j++) {
                    orderError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedDetailsMapList.get(j);

                    typeCode = "10";
                    finalFlag = "N";
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    orderNumber = getMapStr(unprocessedLinesMap, "orderId");
                    totalTotes = rdsCartonsDAO.totalEstimatedTotesByOrder(orderNumber)+"";

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

                        rdsTotesPerOrderUpload.setTypeCode(finalTypeCode);
                        rdsTotesPerOrderUpload.setFinalFlag(finalFinalFlag);
                        rdsTotesPerOrderUpload.setBatchId(finalBatchId);
                        rdsTotesPerOrderUpload.setGroupNumber(finalGroupNumber);
                        rdsTotesPerOrderUpload.setOrderNumber(finalOrderNumber);
                        rdsTotesPerOrderUpload.setTotalTotes(finalTotalTotes);


                        if(! (1 == rdsTotesPerOrderUploadDAO.save(rdsTotesPerOrderUpload))){
                            alert("error inserting into rdsToteContentsUpload, custOrders.orderId[%s]",orderId);
                        }

                    } else {
                        alert("error during custOrders.orderId[%s], skipping the record", orderId);
                        continue;
                    }
                }

                AbstractDAO.updateRowByStringIdTimeStampValue("custOrders","cartonizedUploadStamp","now()","orderId", orderId);
                trace("\n");
            }

        } else {
            validationErrors.add("no estimated totes per order to upload");// no records found, should not
            // proceed to persist
            trace("no estimated totes per order to upload");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateActuals() throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        CustOrderDAO custOrderDAO = new CustOrderDAO();
        RdsCartonsDAO rdsCartonsDAO = new RdsCartonsDAO();
        RdsTotesPerOrderUploadDAO rdsTotesPerOrderUploadDAO = new RdsTotesPerOrderUploadDAO();
        RdsTotesPerOrderUpload rdsTotesPerOrderUpload = new RdsTotesPerOrderUpload();
        GeekDAO geekDAO = new GeekDAO();

        List<String> unprocessedList = new ArrayList<>();
        unprocessedList = custOrderDAO.getActualTotesPerOrderToUpload();

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

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
                trace("actual totes per order for order : [%s]", orderId);

                List<Map<String, String>> unprocessedDetailsMapList = new ArrayList<>();
                unprocessedDetailsMapList = custOrderDAO.getTotesPerOrderToUploadDetails(orderId);

                if(unprocessedDetailsMapList.size() != 1){
                    alert("error getting details, should have just found 1 record, found [%d] custOrders.orderId[%s], skipping the record",unprocessedDetailsMapList.size(),  orderId);
                    continue;
                }

                for (int j = 0; j < unprocessedDetailsMapList.size(); j++) {
                    orderError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedDetailsMapList.get(j);

                    typeCode = "10";
                    finalFlag = "Y";
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    orderNumber = getMapStr(unprocessedLinesMap, "orderId");
                    totalTotes = rdsCartonsDAO.totalActualTotesByOrder(orderNumber)+""; // change here 0709 for ECOM if order Type is ECOM them total no. of totes query needs to be different

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
                        fieldsList.addAll(new ArrayList<String>(12) {
                            {
                                add(finalTypeCode);
                                add(finalFinalFlag);
                                add(finalBatchId);
                                add(finalGroupNumber);
                                add(finalOrderNumber);
                                add(finalTotalTotes);
                            }
                        });
                        rdsTotesPerOrderUpload.setTypeCode(finalTypeCode);
                        rdsTotesPerOrderUpload.setFinalFlag(finalFinalFlag);
                        rdsTotesPerOrderUpload.setBatchId(finalBatchId);
                        rdsTotesPerOrderUpload.setGroupNumber(finalGroupNumber);
                        rdsTotesPerOrderUpload.setOrderNumber(finalOrderNumber);
                        rdsTotesPerOrderUpload.setTotalTotes(finalTotalTotes);


                        if(! (1 == rdsTotesPerOrderUploadDAO.save(rdsTotesPerOrderUpload))){
                            alert("error inserting into rdsToteContentsUpload, custOrders.orderId[%s]",orderId);
                        }
                    } else {
                        alert("error during custOrders.orderId[%s], skipping the record", orderId);
                        continue;
                    }
                }

                AbstractDAO.updateRowByStringIdTimeStampValue("custOrders","uploadStamp","now()","orderId", orderId);
                trace("\n");
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
//        try {
            if (linesList.size() > 0) {
//                fWriter.writeLine(linesList, "TOTES_PER_ORDER_UPLOAD");
          	  linesList = new ArrayList<>();
            } else {
                trace("no records found");
            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


}