package sloane;

import dao.DataAccessException;
import dao.GeekDAO;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class RdsOperatorActivityRecord implements FileRecord {

    FileWatcher fw;
//    FileWriter fWriter;

    public RdsOperatorActivityRecord(FileWatcher fw){
        this.fw = fw;
//        fWriter = new FileWriter(fw);
    }

    GeekDAO geekDao = new GeekDAO();
    List<List<String>> linesList = new ArrayList<>();

    private String seq;
    private String operator_id;
    private String type_code;
    private String operator_task;
    private String start_time;
    private String end_time;
    private Instant stamp;
    private String processed;


    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
        linesList = new ArrayList<>();
        validationErrors.addAll(validateGeekOperatorActivity()); // geek 4.6

        return validationErrors;
    }

    public List<String> validateGeekOperatorActivity() throws NumberFormatException, DataAccessException {
        List<String> validationErrors = new ArrayList<>();
       
        List<Map<String, String>> unprocessedList = new ArrayList<>();
        unprocessedList = geekDao.getAllProcessedOrdered("rdsOperatorActivity", "no","seq","ASC");
        if(unprocessedList!= null && !unprocessedList.isEmpty()){
            for (int i = 0; i < unprocessedList.size(); i++) {
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                Map<String, String> unprocessedMap = unprocessedList.get(i);

                seq = unprocessedMap.get("seq");
                geekDao.updateProcessed("rdsOperatorActivity", "processing", Integer.parseInt(seq));

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

                trace("seq                  : [%s]", seq);
                trace("    operator_id      : [%s]", operator_id);
                trace("    type_code        : [%s]", type_code);
                trace("    operator_task    : [%s]", operator_task);
                trace("    start_time       : [%s]", start_time);
                trace("    end_time         : [%s]", end_time);

                if(!error){
                    fieldsList.addAll(new ArrayList<String>(8) {
                        {
                            add(type_code);
                            add(operator_task);
                            add(operator_id);
                            add(start_time);
                            add(end_time);
                        }
                    });

                    linesList.add(fieldsList);
                    geekDao.updateProcessed("rdsOperatorActivity", "yes", Integer.parseInt(seq));

                }else{
                    alert("marking rdsOperatorActivity.seq[%s] as 'error'",seq);
                    geekDao.updateProcessed("rdsOperatorActivity", "error", Integer.parseInt(seq));
                }

            }

        }else{
            validationErrors.add("no operator activity records found");// no records found, should not
                                                     // proceed to persist
            trace("no operator activity records found");
        }
        return validationErrors; // this return is not used anymore
    }


    @Override
    public void persist() throws DataAccessException {
//        try {
            if(linesList.size() > 0){
//                fWriter.writeLine(linesList, "USER_ACTIVITY_FILE");
            	linesList = new ArrayList<>();
            }else{
                trace("no records found");
            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }




}
