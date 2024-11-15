package host;

import dao.AbstractDAO;
import polling.AbstractPollingApp;

import java.util.Map;

import static rds.RDSLog.trace;

public class FileUpload extends AbstractPollingApp {

    private FileUploadHandler fileUploadHandler;

    public FileUpload(String id, String rdsDb) {
        super(id, rdsDb);
        AbstractDAO.setDatabase(db);
    }

    public void setFileUploadHandler(FileUploadHandler fileUploadHandler) {
        this.fileUploadHandler = fileUploadHandler;
    }

    @Override
    protected void poll() {
        fileUploadHandler.cycle();
    }

    public static void main(String[] args) {

        String rdsDb = "db"; // should not be hardcoded

        if (args.length < 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Application name is a required parameter.");
        }

        String appName = args[0];
        trace("application started, id = [%s], db = [%s]", appName, rdsDb);

        FileUpload obj = new FileUpload(appName, rdsDb);

        Map<String, String> controlMap = obj.db.getControlMap(appName);

        HostLog hl = new HostLog(obj.db);

        FileWatcher fw = new FileWatcher(
                controlMap.get("filePath"),
                controlMap.get("fileName"),
                controlMap.get("tempFilePath"),
                controlMap.get("expectedFieldCount"),
                controlMap.get("fileExtension"),
                controlMap.get("filePrefix"),
                controlMap.get("fileSuffix")
        );
        
        FileUploadHandler fuh = new FileUploadHandler(
              fw,
              hl,
              appName
        );
        fuh.setDatabase(obj.db);

        obj.setFileUploadHandler(fuh);

        obj.run();
    }
}