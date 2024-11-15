package dao;

import java.util.List;
import java.util.Map;

public class ProOperatorLogDAO extends  AbstractDAO{

    public List<Map<String,String>> getUnprocessedLogins(){
        return db.getResultMapList("SELECT * FROM proOperatorLog "
                + " WHERE startTime IS NOT NULL "
                + " AND loginUploaded IS NULL ORDER BY startTime");
    }

    public List<Map<String,String>> getUnprocessedLogouts(){
        return db.getResultMapList("SELECT * FROM proOperatorLog "
                + " WHERE loginUploaded IS NOT NULL "
                + " AND endTime IS NOT NULL "
                + " AND logoffUploaded IS NULL ORDER BY endTime");
    }

    public int setLoginUploadedStamp(String logSeq){
        return db.execute("UPDATE proOperatorLog SET loginUploaded = now() WHERE logSeq = %d",Integer.parseInt(logSeq));
    }

    public int setLogoutUploadedStamp(String logSeq){
        return db.execute("UPDATE proOperatorLog SET logoffUploaded = now() WHERE logSeq = %d",Integer.parseInt(logSeq));
    }

    public int setDefaultLoginUploadedStamp(String logSeq){
        return db.execute("UPDATE proOperatorLog SET loginUploaded = '1970-01-01 00:00:00' WHERE logSeq = %d",Integer.parseInt(logSeq));
    }

    public int setDefaultLogoutUploadedStamp(String logSeq){
        return db.execute("UPDATE proOperatorLog SET logoffUploaded = '1970-01-01 00:00:00' WHERE logSeq = %d",Integer.parseInt(logSeq));
    }
}
