package sloane;

import dao.*;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;
import records.RdsToteContentsUpload;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static sloane.SloaneConstants.*;
import static rds.RDSUtil.*;

public class ToteContentsRecord extends AbstractDAO implements FileRecord {

    FileWatcher fw;
//    FileWriter fWriter;

    public ToteContentsRecord(FileWatcher fw) {
        this.fw = fw;
//        fWriter = new FileWriter(fw);
    }

    List<List<String>> linesList = new ArrayList<>();

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
   	  linesList = new ArrayList<>();

//        validationErrors = validateEstimates(); // finalFlag = N
        validationErrors = validateActuals(); // finalFlag = Y

        return validationErrors; // this return is not used anymore
    }

    public List<String> validateEstimates() throws NumberFormatException, DataAccessException {


        List<String> validationErrors = new ArrayList<>();
        RdsCartonsDAO rdsCartonsDAO = new RdsCartonsDAO();
        CustOrderDAO custOrderDAO = new CustOrderDAO();
        RdsToteContentsUploadDAO rdsToteContentsUploadDAO = new RdsToteContentsUploadDAO();
        RdsToteContentsUpload rdsToteContentsUpload = new RdsToteContentsUpload();

        List<String> unprocessedList = new ArrayList<>();
        unprocessedList = rdsCartonsDAO.getEstimatedToteContentsToUpload();

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

            String cartonSeq = "0";
            String orderLineSeq = "";
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String toteNumber = "";
            String totalTotes = "";
            String finalFlag = "";
            String cartonLpn = "";
            String toteSeqNumber = "";
            String origin = "";

            for (int i = 0; i < unprocessedList.size(); i++) {
                boolean cartonError = false;
                cartonSeq = unprocessedList.get(i);
                trace("tote contents upload cartonSeq : [%s]", cartonSeq);

                List<Map<String, String>> unprocessedLinesMapList = new ArrayList<>();
                unprocessedLinesMapList = rdsCartonsDAO.getEstimatedOrderLinesPerToteToUpload(Integer.parseInt(cartonSeq));

                for (int j = 0; j < unprocessedLinesMapList.size(); j++) {
                    boolean orderLineError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedLinesMapList.get(j);

                    typeCode = "18";
                    if( j == 0 ){ typeCode = "17"; }
                    orderLineSeq = getMapStr(unprocessedLinesMap,"orderLineSeq");
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    orderNumber = getMapStr(unprocessedLinesMap, "orderId");
                    pageNumber = getMapStr(unprocessedLinesMap, "pageId");
                    lineNumber = getMapStr(unprocessedLinesMap, "lineId");
                    toteNumber = getMapStr(unprocessedLinesMap, "cartonCount");
                    totalTotes = rdsCartonsDAO.totalEstimatedTotesByOrder(orderNumber)+"";
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
                    }

                    typeCode = padZeros(typeCode, 2);
                    batchId = padZeros(batchId, 7);
                    groupNumber = padZeros(groupNumber, 7);
                    orderNumber = padZeros(orderNumber, 7);
                    pageNumber = padZeros(pageNumber, 7);
                    lineNumber = padZeros(lineNumber, 7);
                    toteNumber = padZeros(toteNumber, 7);
                    totalTotes = padZeros(totalTotes, 7);
                    finalFlag = padZeros(finalFlag, 1);
                    cartonLpn = padZeros(cartonLpn, 9);
                    toteSeqNumber = padZeros(toteSeqNumber, 4);
                    origin = padZeros(origin, 2);

                    trace("orderLineSeq             : [%s]", orderLineSeq);
                    trace("    typeCode             : [%s]", typeCode);
                    trace("    batchId              : [%s]", batchId);
                    trace("    groupNumber          : [%s]", groupNumber);
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
                                add(finalGroupNumber);
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

                        rdsToteContentsUpload.setTypeCode(finalTypeCode);
                        rdsToteContentsUpload.setBatchId(finalBatchId);
                        rdsToteContentsUpload.setGroupNumber(finalGroupNumber);
                        rdsToteContentsUpload.setOrderNumber(finalOrderNumber);
                        rdsToteContentsUpload.setPageNumber(finalPageNumber);
                        rdsToteContentsUpload.setLineNumber(finalLineNumber);
                        rdsToteContentsUpload.setToteNumber(finalToteNumber);
                        rdsToteContentsUpload.setTotalTotes(finalTotalTotes);
                        rdsToteContentsUpload.setFinalFlag(finalFinalFlag);
                        rdsToteContentsUpload.setCartonLpn(finalCartonLpn);
                        rdsToteContentsUpload.setToteSeqNumber(finalToteSeqNumber);
                        rdsToteContentsUpload.setOrigin(finalOrigin);

                        if(! (1 == rdsToteContentsUploadDAO.save(rdsToteContentsUpload))){
                            alert("error inserting into rdsToteContentsUpload, orderLineSeq [%s]",orderLineSeq);
                        }
                    } else {
                        alert("error during custOrderLine.orderLineSeq[%s], skipping the line", orderLineSeq);
                        continue;
                    }
                }

                AbstractDAO.updateRowByIntIdTimeStampValue("rdsCartons","estContentsUploadStamp","now()","cartonSeq",Integer.parseInt(cartonSeq));
                trace("\n");
            }

        } else {
            validationErrors.add("no estiamted tote contents to upload");// no records found, should not
            // proceed to persist
            trace("no estiamted tote contents to upload");
        }
        return validationErrors; // this return is not used anymore
    }

    public List<String> validateActuals() throws NumberFormatException, DataAccessException {

        List<String> validationErrors = new ArrayList<>();
        RdsCartonsDAO rdsCartonsDAO = new RdsCartonsDAO();
        CustOrderDAO custOrderDAO = new CustOrderDAO();
        RdsToteContentsUploadDAO rdsToteContentsUploadDAO = new RdsToteContentsUploadDAO();
        RdsToteContentsUpload rdsToteContentsUpload = new RdsToteContentsUpload();

        List<String> unprocessedList = new ArrayList<>();
        unprocessedList = rdsCartonsDAO.getActualToteContentsToUpload();

        if (unprocessedList != null && !unprocessedList.isEmpty()) {

            String cartonSeq = "0";
            String orderLineSeq = "";
            String typeCode = "";
            String batchId = "";
            String groupNumber = "";
            String orderNumber = "";
            String pageNumber = "";
            String lineNumber = "";
            String lineSeq = "";       // SR: in scope since 06/10/2024
            String toteNumber = "";
            String totalTotes = "";
            String finalFlag = "";
            String cartonLpn = "";
            String toteSeqNumber = "";
            String origin = "";

            for (int i = 0; i < unprocessedList.size(); i++) {
                boolean cartonError = false;
                cartonSeq = unprocessedList.get(i);
                trace("tote contents upload cartonSeq : [%s]", cartonSeq);

                List<Map<String, String>> unprocessedLinesMapList = new ArrayList<>();
                unprocessedLinesMapList = rdsCartonsDAO.getActualOrderLinesPerToteToUpload(Integer.parseInt(cartonSeq));

                for (int j = 0; j < unprocessedLinesMapList.size(); j++) {
                    boolean orderLineError = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedLinesMap = unprocessedLinesMapList.get(j);

                    typeCode = "18";
                    if( j == 0 ){ typeCode = "17"; }
                    orderLineSeq = getMapStr(unprocessedLinesMap,"orderLineSeq");
                    batchId = getMapStr(unprocessedLinesMap, "waveName");
                    groupNumber = getMapStr(unprocessedLinesMap, "groupNumber");
                    orderNumber = getMapStr(unprocessedLinesMap, "orderId");
                    pageNumber = getMapStr(unprocessedLinesMap, "pageId");
                    lineNumber = getMapStr(unprocessedLinesMap, "lineId");
                    lineSeq = getMapStr(unprocessedLinesMap, "lineNumber");       // SR: in scope since 06/10/2024
                    toteNumber = getMapStr(unprocessedLinesMap, "cartonCount");
                    totalTotes = rdsCartonsDAO.totalActualTotesByOrder(orderNumber)+"";
                    finalFlag = "Y";
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
                    }

                    typeCode = padZeros(typeCode, 2);
                    batchId = padZeros(batchId, 7);
                    groupNumber = padZeros(groupNumber, 7);
                    orderNumber = padZeros(orderNumber, 7);
                    pageNumber = padZeros(pageNumber, 7);
                    lineSeq = padZeros(lineSeq, 7);       // SR: in scope since 06/10/2024
                    lineNumber = padZeros(lineNumber, 7);
                    toteNumber = padZeros(toteNumber, 7);
                    totalTotes = padZeros(totalTotes, 7);
                    finalFlag = padZeros(finalFlag, 1);
                    cartonLpn = padZeros(cartonLpn, 9);
                    toteSeqNumber = padZeros(toteSeqNumber, 4);
                    origin = padZeros(origin, 2);

                    trace("orderLineSeq             : [%s]", orderLineSeq);
                    trace("    typeCode             : [%s]", typeCode);
                    trace("    batchId              : [%s]", batchId);
                    trace("    groupNumber          : [%s]", groupNumber);
                    trace("    orderNumber          : [%s]", orderNumber);
                    trace("    pageNumber           : [%s]", pageNumber);
                    trace("    lineSeq              : [%s]", lineSeq);       // SR: in scope since 06/10/2024
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
                        String finalOrderNumber = orderNumber;
                        String finalPageNumber = pageNumber;
                        String finalLineNumber = lineNumber;
                        String finalLineSeq = lineSeq;        // SR: in scope since 06/10/2024
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
                                add(finalGroupNumber);
                                add(finalLineSeq);       // SR: in scope since 06/10/2024
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

                        rdsToteContentsUpload.setTypeCode(finalTypeCode);
                        rdsToteContentsUpload.setBatchId(finalBatchId);
                        rdsToteContentsUpload.setGroupNumber(finalGroupNumber);
                        rdsToteContentsUpload.setOrderNumber(finalOrderNumber);
                        rdsToteContentsUpload.setPageNumber(finalPageNumber);
                        rdsToteContentsUpload.setLineNumber(finalLineNumber);
                        rdsToteContentsUpload.setLineSeq(finalLineSeq); // SR: in scope since 06/10/2024
                        rdsToteContentsUpload.setToteNumber(finalToteNumber);
                        rdsToteContentsUpload.setTotalTotes(finalTotalTotes);
                        rdsToteContentsUpload.setFinalFlag(finalFinalFlag);
                        rdsToteContentsUpload.setCartonLpn(finalCartonLpn);
                        rdsToteContentsUpload.setToteSeqNumber(finalToteSeqNumber);
                        rdsToteContentsUpload.setOrigin(finalOrigin);

                        if(! (1 == rdsToteContentsUploadDAO.save(rdsToteContentsUpload))){
                            alert("error inserting into rdsToteContentsUpload, orderLineSeq [%s]",orderLineSeq);
                        }

                    } else {
                        alert("error during custOrderLine.orderLineSeq[%s], skipping the line", orderLineSeq);
                        continue;
                    }
                }

                AbstractDAO.updateRowByIntIdTimeStampValue("rdsCartons","actContentsUploadStamp","now()","cartonSeq",Integer.parseInt(cartonSeq));
                trace("\n");
            }

        } else {
            validationErrors.add("no actual tote contents to upload");// no records found, should not
            // proceed to persist
            trace("no actual tote contents to upload");
        }
        return validationErrors; // this return is not used anymore
    }


    @Override
    public void persist() throws DataAccessException {
//        try {
            if (linesList.size() > 0) {
          	  linesList = new ArrayList<>();
//                fWriter.writeLine(linesList, "TOTE_CONTENTS_UPLOAD");
            } else {
                trace("no records found");
            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


}
