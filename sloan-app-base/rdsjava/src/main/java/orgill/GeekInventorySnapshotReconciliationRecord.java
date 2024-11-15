package sloane;

import dao.*;
import host.*;
import rds.RDSEvent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static org.apache.commons.io.FileUtils.getFile;

import static rds.RDSUtil.*;


public class GeekInventorySnapshotReconciliationRecord extends AbstractDAO implements FileRecord {

    FileWatcher fw;
    FileWriter fWriter;

    GeekSnapshotDAO geekSnapShotDAO = new GeekSnapshotDAO();;
    List<List<String>> linesList;
    private String seq;
    private String sku_code;
    private String amount;
    private boolean canCreateFile = false;
    private int currentUnprocessedRowCount = 0;


    public GeekInventorySnapshotReconciliationRecord(FileWatcher fw) {
        this.fw = fw;
        fWriter = new FileWriter(fw);
        linesList = new ArrayList<>();
    }

    GeekDAO geekDao = new GeekDAO();

    /**
     * @return
     * @throws NumberFormatException
     * @throws DataAccessException
     */
    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {

        /***************************************************DO WE HAVE THE SNAPSHOT DATA?**************************************************/
        /**
         * reset snapshotReceivedForToday runtime value at 00:00
         */
        resetSnapshotReceivedForToday();

        /**
         * check if new data has been received for snapshot after expected time
         */
        String snapshotReceivedForToday = db.getRuntime("snapshotReceivedForToday");
        if(isAfterSnapshotDataCheckTime() && snapshotReceivedForToday.equals("0")){
            //check in geekSnapshot for new data
            if(geekSnapShotDAO.isNewSnapshotDataAvailable()){
                inform("new geek snapshot data received from geek, setting runtime value of snapshotReceivedForToday to 1");
                db.setRuntime("snapshotReceivedForToday","1");
            }else{
                alert("no new geek snapshot data received from geek, raising and event SNAPSHOT_DATA_UNAVAILABLE");
                db.setRuntime("snapshotReceivedForToday","-1");
                RDSEvent.start("SNAPSHOT_DATA_UNAVAILABLE");
            }
        }

        /***********************************************IS THE GEEK AUDIT RUNNING? (FOR WEBPAGE)*************************************/
        /**
         * set runtime for "geek audit is running" indication on a web page
         * */
        // check current time
        // greater than start time AND lesser than end time AND runtime is not set then, set the runtime
        if(isAfterAuditStart() && db.getRuntime("geekAuditRunning").equals("0") && !isAfterAuditEnd() ){
            trace("geek audit has started, setting the runtime value of geekAuditRunning...");
            db.setRuntime("geekAuditRunning","1");
        }
        // greater than end time AND runtime is set then, unset the runtime
        if(isAfterAuditEnd() && db.getRuntime("geekAuditRunning").equals("1")){
            trace("geek audit has ended, unsetting the runtime value of geekAuditRunning...");
            db.setRuntime("geekAuditRunning","0");
        }

        /**************************************************CREATE INVENTORY FILE***************************************************/
        /**
         * create inventory snapshot file
         * */
        List<String> validationErrors = new ArrayList<>();
        linesList = new ArrayList<>();

        List<Map<String, String>> unprocessedGeekSnapshotsMapList = new ArrayList<>();
        List<Map<String, String>> unprocessedGeekSnapshotsGroupedBySkuMapList = new ArrayList<>();
        unprocessedGeekSnapshotsMapList = geekDao.getAllProcessedOrdered("geekSnapshot","no","sku_code","ASC");

        trace("processing snapshot of found [%d] records", unprocessedGeekSnapshotsMapList.size());
        currentUnprocessedRowCount = unprocessedGeekSnapshotsMapList.size();

        unprocessedGeekSnapshotsGroupedBySkuMapList = geekSnapShotDAO.getUnprocessedGroupedBySku();
        canCreateFile = fw.doesStableTableExist(currentUnprocessedRowCount);

        if(canCreateFile && unprocessedGeekSnapshotsGroupedBySkuMapList!= null && !unprocessedGeekSnapshotsGroupedBySkuMapList.isEmpty()){
            boolean hasValidAuditTime = true;

            int ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES = 120;
            String ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES_STR = db.getControl("geekplus","acceptableAuditTimeDifference","120");
            if(StringUtils.isNumeric(ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES_STR)){
                ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES = Integer.parseInt(db.getControl("geekplus","acceptableAuditTimeDifference","120"));
            }

            // once the stable table exists, get distinct audit times of unprocessed rows
            int distinctAuditTimeCount = geekSnapShotDAO.getDistinctAuditTimeCount();
            if(distinctAuditTimeCount == 1) {

                // if we get just one records check how old it is from the current time
                String auditTimeStampString = geekSnapShotDAO.getAuditTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    Date parsedTimestamp = dateFormat.parse(auditTimeStampString);
                    Date currentTimestamp = new Date();
                    long differenceInMillis = currentTimestamp.getTime() - parsedTimestamp.getTime();

                    long minutes = (differenceInMillis / 1000) / 60;
                    trace("audit time is [%s] minutes old",minutes+"");

                    if(minutes < 0){
                        //if audit time is greater than sent time/current time
                        alert("invalid time difference between audit time and current time, audit is [%s] minutes old",minutes+"");
                        hasValidAuditTime = false;
                        RDSEvent.start("INVALID_AUDIT_TIME");
                        SloaneCommonDAO.createCommsError("Geek Snapshot","Invalid audit time",1);
                        updateRowByStringIdStringValue("geekSnapshot","processed","err","processed","no");
                    }else if(minutes > ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES){
                        //if audit time is older than acceptable time
                        alert("audit time is [%s] minutes old, which is greater than [%d] minutes",minutes+"",ACCEPTABLE_AUDIT_WINDOW_IN_MINUTES);
                        hasValidAuditTime = false;
                        RDSEvent.start("AUDIT_OLDER_THAN_ACCEPTABLE_AMOUNT");
                        SloaneCommonDAO.createCommsError("Geek Snapshot","Audit older than acceptable amount",1);
                        updateRowByStringIdStringValue("geekSnapshot","processed","err","processed","no");
                    }

                } catch (ParseException e) {
                    alert("Error parsing timestamp: " + e.getMessage());
                    hasValidAuditTime = false;
                    updateRowByStringIdStringValue("geekSnapshot","processed","err","processed","no");
                    throw new RuntimeException(e);
                }

            }else {
                // if we get more than one results then that's an issue. All records should have same audit time
                alert("[%d] distinct audit time(s) found for unprocessed snapshot data. All unprocessed snapshot data should have same audit time.", distinctAuditTimeCount);
                hasValidAuditTime = false;
                RDSEvent.start("MULTIPLE_AUDIT_TIME_PRESENT_ERROR");
                SloaneCommonDAO.createCommsError("Geek Snapshot","Multiple audit time present Error",1);
                updateRowByStringIdStringValue("geekSnapshot","processed","err","processed","no");
            }

            if(hasValidAuditTime){

                RDSEvent.stop("INVALID_AUDIT_TIME");
                RDSEvent.stop("MULTIPLE_AUDIT_TIME_PRESENT_ERROR");
                RDSEvent.stop("AUDIT_OLDER_THAN_ACCEPTABLE_AMOUNT");

                for(int i = 0; i < unprocessedGeekSnapshotsGroupedBySkuMapList.size(); i++) {

                    boolean error = false;
                    List<String> fieldsList = new ArrayList<>();
                    Map<String, String> unprocessedGeekSnapshotsMap = unprocessedGeekSnapshotsGroupedBySkuMapList.get(i);

                    // SR: since query has a group by clause, seq printed will be one of the rows which got grouped
                    seq = unprocessedGeekSnapshotsMap.get("seq");
                    trace("seq             : [%s]", seq);

                    sku_code = unprocessedGeekSnapshotsMap.get("sku_code");
                    amount = unprocessedGeekSnapshotsMap.get("amount");

                    if(sku_code == null){
                        validationErrors.add("invalid sku_code [null]");
                        alert("seq [%s]: invalid sku_code [null]",seq);
                        error = true;
                    }
                    if(sku_code.isBlank() || sku_code.isEmpty()){
                        validationErrors.add("invalid sku_code ["+ sku_code+"]");
                        alert("seq [%s]: invalid sku_code ["+ sku_code+"]",seq);
                        error = true;
                    }
                    if(amount == null){
                        validationErrors.add("invalid amount [null]");
                        alert("seq [%s]: invalid amount [null]",seq);
                        error = true;
                    }
                    if(amount.isBlank() || amount.isEmpty()){
                        validationErrors.add("invalid amount ["+ amount+"]");
                        alert("seq [%s]: invalid amount ["+ amount+"]",seq);
                        error = true;
                    }
                    if(Integer.parseInt(amount) < 0){
                        validationErrors.add("invalid amount ["+ amount+"]");
                        alert("seq [%s]: invalid amount ["+ amount+"]",seq);
                        error = true;
                    }

                    if(!error){
                        String final_sku_code = padZeros(sku_code, 7);
                        String final_amount = padZeros(amount, 7);

                        trace("    sku_code    : [%s]", final_sku_code);
                        trace("    amount      : [%s]", final_amount);


                        fieldsList.addAll(new ArrayList<String>(5000) {

                            {
                                add(final_sku_code);
                                add(final_amount);
                            }
                        });

                        linesList.add(fieldsList);
//                    geekDao.updateProcessed("geekSnapshot", "yes", Integer.parseInt(seq));
                        updateRowByStringIdStringValue("geekSnapshot","processed","yes","sku_code",sku_code,"processed","no");

                    }else{
//                    geekDao.updateProcessed("geekSnapshot", "err", Integer.parseInt(seq));
                        updateRowByStringIdStringValue("geekSnapshot","processed","err","sku_code",sku_code,"processed","no");
                    }

                }
            }


            fw.resetTableRowCount();

        }else{
            validationErrors.add("no geek inventory snapshot records found");// no records found, should not
            // proceed to persist
            trace("no geek inventory snapshot records found");
        }
        return validationErrors;
    }

    private void resetSnapshotReceivedForToday() {
        String auditStartTime = db.getControl("geekplus","snapshotResetTime","00:00");
        String[] auditTimeArray = auditStartTime.split(":");
        int hour = Integer.parseInt(auditTimeArray[0]);
        int min = Integer.parseInt(auditTimeArray[1]);
        LocalTime currentTime = LocalTime.now();
        LocalTime targetTime1 = LocalTime.of(hour, min);
        LocalTime targetTime2 = LocalTime.of(hour, min,30);

        if(currentTime.isAfter(targetTime1) && currentTime.isBefore(targetTime2)){
            trace("resetting runtime value of snapshotReceivedForToday to 0");
            db.setRuntime("snapshotReceivedForToday","0");
        }
    }

    public static boolean isAfterAuditStart() {

        String auditStartTime = db.getControl("geekplus","auditStartTime","00:25");
        String[] auditTimeArray = auditStartTime.split(":");
        int hour = Integer.parseInt(auditTimeArray[0]);
        int min = Integer.parseInt(auditTimeArray[1]);
        LocalTime currentTime = LocalTime.now();
        LocalTime targetTime = LocalTime.of(hour, min);

        return currentTime.isAfter(targetTime);
    }

    public static boolean isAfterAuditEnd() {

        String auditStartTime = db.getControl("geekplus","auditEndTime","01:30");
        String[] auditTimeArray = auditStartTime.split(":");
        int hour = Integer.parseInt(auditTimeArray[0]);
        int min = Integer.parseInt(auditTimeArray[1]);
        LocalTime currentTime = LocalTime.now();
        LocalTime targetTime = LocalTime.of(hour, min);

        return currentTime.isAfter(targetTime);
    }

    public static boolean isAfterSnapshotDataCheckTime() {

        LocalTime currentTime = LocalTime.now();

        String snapshotExpectedTime = db.getControl("geekplus","snapshotExpectedTime","01:30");
        String[] auditTimeArray = snapshotExpectedTime.split(":");
        int hour1 = Integer.parseInt(auditTimeArray[0]);
        int min1 = Integer.parseInt(auditTimeArray[1]);
        LocalTime targetTime1 = LocalTime.of(hour1, min1);

        return currentTime.isAfter(targetTime1);
    }

    /**
     * @throws DataAccessException
     */
    @Override
    public void persist() throws DataAccessException {

        try {
            if(linesList.size() > 0){
                fWriter.writeLine(linesList,"SNAPSHOT_RECON_FILE");
                linesList = new ArrayList<>();
            }else{
                trace("no data to create lines");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
