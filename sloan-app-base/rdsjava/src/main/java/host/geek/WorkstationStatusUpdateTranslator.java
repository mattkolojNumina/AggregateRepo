package host.geek;

import dao.*;
import records.ProOperatorLog;
import records.RdsOperatorActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static rds.RDSLog.alert;
import static rds.RDSUtil.*;

public class WorkstationStatusUpdateTranslator implements GeekTranslator {

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
        workstationStatusUpdateConfirmations();
        proOperatorsConfirmations();
    }

    private void workstationStatusUpdateConfirmations() {

        GeekDAO geekDAO = new GeekDAO();
        RdsOperatorActivity rdsOperatorActivity = null;
        RdsOperatorActivityDAO rdsOperatorActivityDAO = new RdsOperatorActivityDAO();
        GeekWorkstaionStatusUpdateDAO geekWorkstaionStatusUpdateDAO = new GeekWorkstaionStatusUpdateDAO();
        List<Map<String, String>> allUnprocessedConfirmations;

        try {
            String worker;
            String workstationId;
            String status;
            String workstationType;
            String happenedAt;

            allUnprocessedConfirmations = geekDAO.getAllProcessedOrdered("geekWorkstationStatusUpdate", "no", "seq", "ASC");

            if(allUnprocessedConfirmations != null && !allUnprocessedConfirmations.isEmpty()){

                for (Map<String, String> unprocessedConf : allUnprocessedConfirmations) {
                    int rowsAdded = 0 ;
                    int seq = 0;

                    seq = Integer.parseInt(unprocessedConf.get("seq"));
                    worker = unprocessedConf.get("worker");
                    if(!isNumeric(worker)) {
                        alert("invalid worker [%s] for seq [%s], skipping the record", worker, seq);
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }
                    workstationId = unprocessedConf.get("workstationId");
                    status = unprocessedConf.get("status");
                    workstationType = unprocessedConf.get("workstationType");
                    if(workstationType == null || workstationType.isBlank()) {
                        alert("invalid workstationType, null or empty for seq [%s], skipping the record", worker, seq);
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }
                    happenedAt = unprocessedConf.get("happenedAt");

                    trace("processing geekWorkstationStatusUpdate conf seq  : [%d]", seq);
                    trace("         worker              : [%s]", worker);
                    trace("         status              : [%s]", status);
                    trace("         workstationId       : [%s]", workstationId);
                    trace("         workstationType     : [%s]", workstationType);
                    trace("         happened at         : [%s]", happenedAt);

                    String loginTime = "";
                    if("LOGIN".equals(status)){
                        status = "1";
                    }else if("LOGOUT".equals(status)){
                        loginTime = geekWorkstaionStatusUpdateDAO.getLoginTimeForWorker(worker,workstationId,workstationType);
//                        trace("loginTime: [%s]", loginTime);
                        if(loginTime.isBlank()){
                            alert("no login time found for seq [%s], skipping the record", seq);
                            int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                            if(rowsUpdated != 1){
                                alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                            }
                            continue;
                        }
                        status = "2";
                    }else{
                        alert("invalid status [%s], seq [%d]", status, seq);
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }

                    if (workstationType.equals("PICKING") && workstationId.length() == 5){
                        workstationType = "17";
                    }else if (workstationType.equals("PUT_AWAY")){
                        workstationType = "11";
                    }else if (workstationType.equals("STOCK_TAKE")) {
                        workstationType = "12";
                    }else if (workstationType.equals("PICKING")){
                        workstationType = "10";
                    }else{
                        alert("seq [%d], invalid workstationType [%s]",seq, workstationType);
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                        }
                        continue;
                    }

                    workstationId = "GEEK " + workstationId;

                    if("1".equals(status)){
                        rdsOperatorActivity = new RdsOperatorActivity.RdsOperatorActivityBuilder
                                (Integer.parseInt(worker), status, workstationType, workstationId, happenedAt, null)
                                .build();
                    }else if("2".equals(status)){
                        rdsOperatorActivity = new RdsOperatorActivity.RdsOperatorActivityBuilder
                                (Integer.parseInt(worker), status, workstationType, workstationId, loginTime, happenedAt)
                                .build();
                    }

                    rowsAdded = rdsOperatorActivityDAO.save(rdsOperatorActivity);

                    if(rowsAdded == 1){
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","yes",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'yes'",seq);
                        }
                    }else{
                        alert("error inserting into rdsOperatorActivity table, seq [%d]",seq);
                        int rowsUpdated = geekDAO.updateProcessed("geekWorkstationStatusUpdate","err",seq);
                        if(rowsUpdated != 1){
                            alert("error while setting geekWorkstationStatusUpdate.seq[%d] as processed = 'err'",seq);
                        }
                    }
                }
            }else{
                trace("no geekWorkstationStatusUpdate(s) to translate");
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private void proOperatorsConfirmations(){
        RdsOperatorActivity rdsOperatorActivity = null;
        RdsOperatorActivityDAO rdsOperatorActivityDAO = new RdsOperatorActivityDAO();
        ProOperatorLog proOperatorLog = new ProOperatorLog();
        ProOperatorLogDAO proOperatorLogDAO = new ProOperatorLogDAO();

        List<Map<String, String>> allUnprocessedLogins;
        List<Map<String, String>> allUnprocessedLogouts;

        try {
            String logSeq;
            String status; // login or logout
            String operatorID;
            String task;
            String area;
            String startTime;
            String endTime;

            allUnprocessedLogins = proOperatorLogDAO.getUnprocessedLogins();
            status = "1";
            if(allUnprocessedLogins != null && !allUnprocessedLogins.isEmpty()){
                for (Map<String, String> unprocessedConf : allUnprocessedLogins) {
                    boolean error = false;
                    int rowsAdded = 0 ;

                    logSeq = unprocessedConf.get("logSeq");
                    trace("processing proOperatorLog seq    : [%s]", logSeq);

                    operatorID = unprocessedConf.get("operatorID");
                    task = unprocessedConf.get("task");
                    area = unprocessedConf.get("area");
                    startTime = unprocessedConf.get("startTime");

                    if (!error && !isNumeric(operatorID)) {
                        alert("logsSeq [%s]: invalid operator ID [%s]",logSeq,operatorID);
                        error = true;
                    }
                    if (!error && task.isEmpty()) {
                        alert("logsSeq [%s]: invalid task [%s]",logSeq,task);
                        error = true;
                    }

                    trace("         operatorID              : [%s]", operatorID);
                    trace("         task                    : [%s]", task);
                    trace("         area                    : [%s]", area);
                    trace("         startTime               : [%s]", startTime);

                    String taskCode = determineTask(task,area);
                    trace("         taskCode                : [%s]", taskCode);

                    if(error || taskCode.isBlank()){
                        proOperatorLogDAO.setDefaultLoginUploadedStamp(logSeq);
                        continue;
                    }

                    rdsOperatorActivity = new RdsOperatorActivity.RdsOperatorActivityBuilder
                            (Integer.parseInt(operatorID), status, taskCode, area, startTime, null)
                            .build();

                    rowsAdded = rdsOperatorActivityDAO.save(rdsOperatorActivity);

                    if(rowsAdded == 1){
                        int success = proOperatorLogDAO.setLoginUploadedStamp(logSeq);
                        if(success != 1){
                            alert("error setting loginUploadedStamp for logSeq [%s]",logSeq);
                        }
                    }
                }
            }
            else{
                trace("no proOperator Logins(s) to translate");
            }

            allUnprocessedLogouts = proOperatorLogDAO.getUnprocessedLogouts();
            status = "2";
            if(allUnprocessedLogouts != null && !allUnprocessedLogouts.isEmpty()){
                for (Map<String, String> unprocessedConf : allUnprocessedLogouts) {
                    boolean error = false;
                    int rowsAdded = 0 ;

                    logSeq = unprocessedConf.get("logSeq");
                    trace("processing proOperatorLog seq    : [%s]", logSeq);

                    operatorID = unprocessedConf.get("operatorID");
                    task = unprocessedConf.get("task");
                    area = unprocessedConf.get("area");
                    startTime = unprocessedConf.get("startTime");
                    endTime = unprocessedConf.get("endTime");

                    if (!error && !isNumeric(operatorID)) {
                        alert("logsSeq [%s]: invalid operator ID [%s]",logSeq,operatorID);
                        error = true;
                    }
                    if (!error && task.isEmpty()) {
                        alert("logsSeq [%s]: invalid task [%s]",logSeq,task);
                        error = true;
                    }

                    trace("         operatorID              : [%s]", operatorID);
                    trace("         task                    : [%s]", task);
                    trace("         area                    : [%s]", area);
                    trace("         startTime               : [%s]", startTime);
                    trace("         endTime                 : [%s]", endTime);

                    String taskCode = determineTask(task,area);
                    trace("         taskCode                : [%s]", taskCode);

                    if(error || taskCode.isBlank()){
                        proOperatorLogDAO.setDefaultLogoutUploadedStamp(logSeq);
                        continue;
                    }

                    rdsOperatorActivity = new RdsOperatorActivity.RdsOperatorActivityBuilder
                            (Integer.parseInt(operatorID), status, taskCode, area, startTime, endTime)
                            .build();

                    rowsAdded = rdsOperatorActivityDAO.save(rdsOperatorActivity);

                    if(rowsAdded == 1){
                        int success = proOperatorLogDAO.setLogoutUploadedStamp(logSeq);
                        if(success != 1){
                            alert("error setting logoutUploadedStamp for logSeq [%s]",logSeq);
                        }
                    }
                }
            }
            else{
                trace("no proOperator Logout(s) to translate");
            }



        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private String determineTask(String task, String area){
        String taskCode = "";
        if(task.equals("cartPicking") && area.equals("D&U") ){
            taskCode = "13";
        }else if(task.equals("repackPicking")||task.equals("makeupShorts")){ // split case module picking
            taskCode = "14";
        }else if(task.equals("meeting")){
            taskCode = "15";
        }else if(task.equals("cleaning")){
            taskCode = "16";
        }else if(task.equals("cartStation")){ // cart build
            taskCode = "18";
        }else if(task.equals("exceptionStationStation") && area.equals("east")){ // geek pakt audit station
            taskCode = "19";
        }else if(task.equals("exceptionStationStation") && area.equals("west")){ // split pakt audit station
            taskCode = "20";
        }else if(task.equals("orderStartStation")){ // split module carton start
            taskCode = "21";
        }else if(task.equals("cartPicking") && area.equals("P&Q") ){
            taskCode = "23";
        }else if(task.equals("inlinePackStation")&& area.equals("west")){ // split inline pack station
            taskCode = "24";
        }else if(task.equals("inlinePackStation")&& area.equals("east")){ // geek inline pack station
            taskCode = "25";
        }else if(task.equals("break")){
            taskCode = "26";
        }

        if(taskCode.isBlank()){
            alert("could not determine task, not idle scenario, no record will be sent to Sloane");
        }

        return taskCode;
    }
}
