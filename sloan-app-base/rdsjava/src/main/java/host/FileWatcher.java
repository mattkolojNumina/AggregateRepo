package host;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static rds.RDSLog.inform;
import static rds.RDSLog.trace;

public class FileWatcher {

    private final String filePath;
    private final String fileSuffix;
    private final String filePrefix;
    private String fileName;
    private String archiveFilePath;
    private String tempFilePath;
    private String fileExtension;
    private long fileSize;
	private String expectedFieldCount;

    private int tableRowCount;

    private int lastPageUnprocessedRowCount;

    public FileWatcher(String filePath, String filePrefix, String fileSuffix, String fileName, String archiveFilePath) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.filePrefix = filePrefix;
        this.fileSuffix = fileSuffix;
        this.archiveFilePath = archiveFilePath;
        this.fileSize = 0;
    }
  
    public FileWatcher(String filePath, String fileName, String tempFilePath, String expectedFieldCount, String fileExtension, String filePrefix, String fileSuffix ) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.filePrefix = filePrefix;
        this.fileSuffix = fileSuffix;
        this.tempFilePath = tempFilePath;
        this.fileSize = 0;
        this.expectedFieldCount = expectedFieldCount;
        this.fileExtension = fileExtension;
    }  

    public void setFileName( String name ) {
   	 this.fileName = name;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public String getFileSuffix() {
   	 return this.fileSuffix;
    }
    
    public String getFilePrefix() {
   	 return this.filePrefix;
    }
  
    public String getFileExtension() {
        return this.fileExtension;
    }  

    public String getFilePath() {
        return this.filePath;
    }
	
	public String getArchiveFilePath() {
		return this.archiveFilePath;
	}

    public String getTempFilePath() {
        return this.tempFilePath;
    }
	
	public String getExpectedFieldCount() {
		return this.expectedFieldCount;
	}

    public File getFile() {
        return new File(this.filePath + "/" + this.fileName);
    }
    
    public void getFileNameByPrefixAndSuffix() {
       File fileDir = new File( this.filePath );
       File[] files = fileDir.listFiles( new FilenameFilter() {
          @Override
          public boolean accept( File dir, String name ) {
             return name.endsWith( getFileSuffix() ) && name.startsWith( getFilePrefix() );
          }
       });
       if( files.length>0 ) {
      	 setFileName( files[0].getName() );
       }
    }

    public boolean doesStableFileExist() throws FileProcessingException {
        try {
            File f = getFile();
            if (f.exists()) {
                // check file size to see if it is stable
                long currentFileSize = f.length();
                trace(String.format("Measured file size [%d]", currentFileSize));
                trace(String.format("Previously measured file size [%d]", this.fileSize));
                if (currentFileSize == this.fileSize) {
                    // file size has not changed, proceed with processing
                    return true;
                }
                inform(String.format("[%s] exists but is not yet stable.", this.filePath + "\\" + this.fileName));
                this.fileSize = currentFileSize;
                return false;
            } else {
            	getFileNameByPrefixAndSuffix();
            	return false;
            }
        } catch (SecurityException e) {
            throw new FileProcessingException(e.toString());
        }
    }

    public boolean doesStableTableExist(int currentUnprocessedRowCount) {

        if (currentUnprocessedRowCount>0) {

            trace(String.format("       measured row count [%d]", currentUnprocessedRowCount));
            trace(String.format("       previously measured row count [%d]", this.tableRowCount));
            if (currentUnprocessedRowCount == this.tableRowCount) {
                // file size has not changed, proceed with processing
                return true;
            }
            inform(String.format("[%d] rows exists but is not yet stable.", currentUnprocessedRowCount));
            this.tableRowCount = currentUnprocessedRowCount;
            return false;
        } else {
//            trace("here 1");
            return false;
        }

    }

    public boolean doesLastPageRowCountExist(int lastPageRowCount) {

        if (lastPageRowCount>0) {

            trace(String.format("       measured last page row count [%d]", lastPageRowCount));
            trace(String.format("       previously measured last page row count [%d]", this.lastPageUnprocessedRowCount));
            if (lastPageRowCount == this.lastPageUnprocessedRowCount) {
                // file size has not changed, proceed with processing
                return true;
            }
            inform(String.format("[%d] last page rows exists but is not yet stable.", lastPageRowCount));
            this.lastPageUnprocessedRowCount = lastPageRowCount;
            return false;
        } else {
//            trace("here 1");
            return false;
        }

    }

    public void archiveFile() throws FileProcessingException {
        trace("Archiving file [%s] to [%s]", this.fileName, this.archiveFilePath);
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            Files.move(Paths.get(this.filePath + "/" + this.fileName), Paths.get(this.archiveFilePath + "/" +
                    this.fileName + "." + formatter.format(currentTime)));
        } catch(Exception e) {
            throw new FileProcessingException(e.toString());
        }
    }

    public void reset() {
        this.fileSize = 0;
    }

    public void resetTableRowCount() {
        this.tableRowCount = 0;
    }
}
