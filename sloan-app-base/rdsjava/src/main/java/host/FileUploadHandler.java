package host;

import static rds.RDSLog.*;

import dao.AbstractDAO;
import dao.DataAccessException;
import sloane.FileRecordFactory;
import rds.RDSDatabase;
import rds.RDSEvent;


public class FileUploadHandler {

    private final FileWatcher fileWatcher;
    private final HostLog hostLog;
    private final String appName;
    private static RDSDatabase db;
    private FileRecord fr;

    public FileUploadHandler(FileWatcher fw, HostLog hl, String appName){
        this.fileWatcher = fw;
        this.hostLog = hl;
        this.appName = appName;
        this.fr = FileRecordFactory.getFileRecordImpl(appName, fileWatcher);
    }

    public void setDatabase( RDSDatabase rds ) {
   	 db = rds;
   	 AbstractDAO.setDatabase(db);
    }
    
    public void cycle(){
        try {
            processFile();
            RDSEvent.stop(appName);
        } catch (FileProcessingException e) {
            alert(e.getMessage());
            RDSEvent.start(appName);
            // TODO handle notifications for submission level failures
        }
       
    }

    private void processFile() throws FileProcessingException {
        try {
      	  fr.validate();
      	  fr.persist();
        } catch( DataAccessException e) {
            throw new FileProcessingException(e.toString());
        }
    }
}
