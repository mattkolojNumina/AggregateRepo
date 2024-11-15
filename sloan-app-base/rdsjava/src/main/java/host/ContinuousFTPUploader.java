package host;

import dao.SloaneCommonDAO;
import host.FileWatcher;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import polling.AbstractPollingApp;
import rds.RDSEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static rds.RDSLog.inform;
import static rds.RDSLog.trace;
import static rds.RDSUtil.alert;

public class ContinuousFTPUploader extends AbstractPollingApp {

    static String ftpServer = "";
    static int ftpPort = 21;
    static String username = "";
    static String password = "";
    static String localDirectoryPath = "";
    static String archiveDirectoryPath = "";
    static String remoteDirectoryPath = "";

    final String ftpConnectionErrorEvent = "FTP_CONNECTION_ERROR";
    final String ftpFileUploadErrorEvent = "FTP_FILE_UPLOAD_ERROR";
    final String ftpUnrecognizedFileEvent = "FTP_UNRECOGNIZED_FILE_ERROR";
    final String ftpFailedToArchiveEvent = "FTP_FAILED_TO_ARCHIVE_ERROR";

    public ContinuousFTPUploader(String id, String rdsDb) {
        super(id, rdsDb);
        SloaneCommonDAO.setDatabase(db);
    }

    public void moveFile() {

        FTPClient ftpClient = new FTPClient();

        try {

            //temporary addition
//            localDirectoryPath = "C:\\Numina_Projects\\Sloane\\workingRepos\\sloane-app-shubham\\sftp\\base\\orgdatatransfer\\out";

            File localDirectory = new File(localDirectoryPath);
            trace("polling at [%s]",localDirectoryPath);
            File[] files = localDirectory.listFiles();

                if (files != null && files.length > 0) {
                    trace("has files, attempting an ftp connection...");
                    trace("FTP configured to connect with username : [%s]",username);
                    trace("                                   port : [%d]",ftpPort);
                    trace("                                 server : [%s]",ftpServer);

                    ftpClient.connect(ftpServer, ftpPort);

                    if(ftpClient.login(username, password)){
                        RDSEvent.stop(ftpConnectionErrorEvent);
                        ftpClient.setConnectTimeout(10*1000);
                        ftpClient.setDataTimeout(10*1000);

                        // Set file type to binary
                        ftpClient.setFileType(FTP.ASCII_FILE_TYPE);

                        for (File file : files) {
                            if (file.isFile() && isFileStable(file) ) {
                                if(isKnownFilePrefix(file.getName())){
                                    //RDSEvent.stop(ftpUnrecognizedFileEvent);

                                    // Upload the stable file to the FTP server
                                    FileInputStream fileInputStream = new FileInputStream(file);
                                    String remoteFilePath = remoteDirectoryPath + file.getName();
                                    trace("remoteFilePath: [%s]",remoteFilePath);
                                    boolean success = ftpClient.storeFile(remoteFilePath, fileInputStream);

                                    if (success) {
                                        inform("File [%s] uploaded successfully",file.getName());
                                        RDSEvent.stop(ftpFileUploadErrorEvent);

                                        if (file.renameTo( new File(archiveDirectoryPath+file.getName()) )) {
                                            trace("File [%s] moved from active directory to archive directory successfully",file.getName());
                                            RDSEvent.stop(ftpFailedToArchiveEvent);
                                        } else {
                                            alert("File [%s] was NOT moved from active directory to archive directory",file.getName());
                                            SloaneCommonDAO.createCommsError("FTP Outbound","File '" + file.getName() + "' was NOT moved from active directory to archive directory",1);
                                            RDSEvent.start(ftpFailedToArchiveEvent);
                                        }

                                        fileInputStream.close();
                                    } else {

                                        alert("File [%s] had error while uploading",file.getName());
                                        RDSEvent.start(ftpFileUploadErrorEvent);
                                        SloaneCommonDAO.createCommsError("FTP Outbound","Error sending file: ["+file.getName()+"]",1);
                                        fileInputStream.close();
                                    }
                                }else{
                                    alert("[%s] file has a unrecognized prefix,skipping the file",file.getName());
                                    //RDSEvent.start(ftpUnrecognizedFileEvent);
                                    SloaneCommonDAO.createCommsError("FTP Outbound","Unknown file present",9);
                                    continue;
                                }
                            }else{
                                alert("[%s] is not a file OR file is not stable yet",file.getName());
                            }
                        }
                    }else{
                        alert("Connection error OR Login error while connecting to FTP server at [%s]:[%d]",ftpServer, ftpPort);
                        RDSEvent.start(ftpConnectionErrorEvent);
                        SloaneCommonDAO.createCommsError("FTP Outbound","Connection error",1);
                    }
                }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    trace("closing an ftp connection...");
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isKnownFilePrefix(String fileName) {
        trace("file name: [%s]", fileName);

        List<String> prefixList = db.getValueList("SELECT DISTINCT(`value`) FROM controls WHERE `NAME` LIKE '%%fileName%%' AND zone LIKE '%%upload';");

        for (String prefix : prefixList){
            if (fileName.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }


    @Override
    protected void poll() {
        moveFile();
    }

    public static void main(String[] args) {

        String rdsDb = "db"; // should not be hardcoded

        String appName = args[0];
        trace("application started, id = [%s], db = [%s]", appName, rdsDb);

        ContinuousFTPUploader obj = new ContinuousFTPUploader(appName, rdsDb);

        Map<String, String> controlMap = obj.db.getControlMap(appName);

        HostLog hl = new HostLog(obj.db);

        username = controlMap.get("username");
        password = controlMap.get("password");
        localDirectoryPath = controlMap.get("localDirectoryPath");
        archiveDirectoryPath = controlMap.get("archiveDirectoryPath");
        remoteDirectoryPath = controlMap.get("remoteDirectoryPath");
        ftpServer = controlMap.get("ftpServer");
        ftpPort = Integer.parseInt(obj.db.getControl("ContinuousFTPUploader","ftpPort", String.valueOf(21)));

        obj.run();


    }

    private static boolean isFileStable(File file) {
        long fileSizeBefore = file.length();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long fileSizeAfter = file.length();

        return fileSizeBefore == fileSizeAfter;
    }

}
