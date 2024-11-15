package host;

import dao.AbstractDAO;
import polling.AbstractPollingApp;
import dao.AbstractDAO;

import java.util.Map;

import static rds.RDSLog.trace;

public class FileDownload extends AbstractPollingApp {

    private FileHandler fileHandler;

    public FileDownload(String id, String rdsDb) {
        super(id, rdsDb);
        AbstractDAO.setDatabase(db);
    }

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @Override
    protected void poll() {
        fileHandler.cycle();
    }

    public static void main(String[] args) {

        String rdsDb = "db"; // should not be hardcoded

        if (args.length < 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Application name is a required parameter.");
        }

        String appName = args[0];
        trace("application started, id = [%s], db = [%s]", appName, rdsDb);

        FileDownload obj = new FileDownload(appName, rdsDb);

        Map<String, String> controlMap = obj.db.getControlMap(appName);
        HostLog hl = new HostLog(obj.db);

        FileWatcher fw = new FileWatcher(
                controlMap.get("filePath"),
                controlMap.get("filePrefix"),
                controlMap.get("fileSuffix"),
                controlMap.get("fileName"),
                controlMap.get("archiveFilePath")
        );
        
        FileHandler fh = new FileHandler(
              fw,
              hl,
              appName,
              controlMap.getOrDefault("rowCountText",""),
              controlMap.get("expectedFieldCount"),
              controlMap.get("preReqFileCSV"));
        fh.setDatabase(obj.db);

        obj.setFileHandler(fh);

        obj.run();
    }
}