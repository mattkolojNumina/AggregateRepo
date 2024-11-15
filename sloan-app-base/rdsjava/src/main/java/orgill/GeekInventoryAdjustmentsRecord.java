package sloane;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import dao.DataAccessException;
import dao.GeekDAO;
import host.FileRecord;
import host.FileWatcher;
import host.FileWriter;

import static dao.SloaneCommonDAO.padZeros;
import static host.StringUtils.isNumeric;
import static rds.RDSUtil.*;

public class GeekInventoryAdjustmentsRecord implements FileRecord {

    FileWatcher fw;
//    FileWriter fWriter;

    public GeekInventoryAdjustmentsRecord (FileWatcher fw){
        this.fw = fw;
//        fWriter = new FileWriter(fw);
    }

    GeekDAO geekDao = new GeekDAO();


    List<List<String>> linesList = new ArrayList<>();

    private String seq;
    private String transaction_type;
    private String part_number;
    private String putaway_replen_code;
    private String putaway_code;
    private String putaway_type;
    private String quantity_change;
    private String operator_id;
    private Instant stamp;
    private String processed;

    @Override
    public List<String> validate() throws NumberFormatException, DataAccessException {
   	  linesList = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();
       
        List<Map<String, String>> unprocessedInvenAdjustList = new ArrayList<>();
        unprocessedInvenAdjustList = geekDao.getAllProcessedOrdered("rdsGeekInventoryAdjustments", "no","createStamp","ASC");
//        trace(unprocessedInvenAdjustList.toString()+"");
        if(unprocessedInvenAdjustList!= null && !unprocessedInvenAdjustList.isEmpty()){
            for (int i = 0; i < unprocessedInvenAdjustList.size(); i++) {
                boolean error = false;
                List<String> fieldsList = new ArrayList<>();
                Map<String, String> unprocessedInvenAdjustMap = unprocessedInvenAdjustList.get(i);

                seq = unprocessedInvenAdjustMap.get("seq");
                geekDao.updateProcessed("rdsGeekInventoryAdjustments", "processing", Integer.parseInt(seq));

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
                    /* commented out below because of no order putaways*/
                    else if (putaway_type.equals("5") ){
                        if(!putaway_code.equals("00000000")) {// SR: bugfix/289
                            validationErrors.add("invalid putaway codes for transaction-putaway type 21-5");
                            alert("seq [%s]: invalid putaway codes for transaction-putaway type 21-5", seq);
                            error = true;
                        }

                    }
                    else{
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

                trace("seq                     : [%s]", seq);
                trace("    transaction_type    : [%s]", transaction_type);
                trace("    putaway_type        : [%s]", putaway_type);
                trace("    putaway_replen_code : [%s]", putaway_replen_code);
                trace("    putaway_code        : [%s]", putaway_code);
                trace("    part_number         : [%s]", part_number);
                trace("    quantity_change     : [%s]", quantity_change);
                trace("    operator_id         : [%s]", operator_id);

                if(!error){
                    fieldsList.addAll(new ArrayList<String>(8) {
                        {
                            add(transaction_type);
                            add(part_number);
                            add(putaway_replen_code);
                            add(putaway_code);
                            add(putaway_type);
                            add(quantity_change);
                            add(operator_id);
                            add(seq);
                        }
                    });

                    linesList.add(fieldsList);
                    geekDao.updateProcessed("rdsGeekInventoryAdjustments", "yes", Integer.parseInt(seq));

                }else{
                    geekDao.updateProcessed("rdsGeekInventoryAdjustments", "error", Integer.parseInt(seq));
                }

            }

        }else{
            validationErrors.add("no geek inventory adjustments records found");// no records found, should not
                                                     // proceed to persist
            trace("no geek inventory adjustments records found");
        }
        return validationErrors; // this return is not used anymore
    }

    @Override
    public void persist() throws DataAccessException {

//        try {
            if(linesList.size() > 0){
//                fWriter.writeLine(linesList, "ADJUSTMENTS_FILE");
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
