package host;

import dao.*;
import host.geek.GeekTranslator;
import host.geek.GeekTranslatorFactory;
import sloane.FileRecordFactory;
import sloane.OutBoundOrderFileRecord;
import sloane.FullcaseScanTunnelFileRecord;
import static sloane.SloaneConstants.*;

import rds.RDSDatabase;
import rds.RDSEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


import static rds.RDSLog.*;

public class FileHandler {

    private final FileWatcher fileWatcher;
    private final HostLog hostLog;
    private final String rowCountText;
    private final String preReqFileListCSV;
    private int expectedFieldCount;
    private final String appName;
    private static RDSDatabase db;

    public FileHandler(FileWatcher fw, HostLog hl, String appName, String rowCountText,
            String expectedFieldCount, String preReqFileListCSV) {
        this.fileWatcher = fw;
        this.hostLog = hl;
        this.appName = appName;
        this.rowCountText = rowCountText;
        this.preReqFileListCSV = preReqFileListCSV;

        try {
            this.expectedFieldCount = Integer.parseInt(expectedFieldCount);
        } catch (NumberFormatException e) {
            this.expectedFieldCount = 0;
        }
    }
    
    public void setDatabase( RDSDatabase rds ) {
   	 db = rds;
   	 AbstractDAO.setDatabase(db);
    }

    public void cycle() {
 	     boolean downloadSuccess = false;
        try {
            if (fileWatcher.doesStableFileExist()) {
                inform(String.format("[%s] exists and is stable.", fileWatcher.getFileName()));

                boolean isRowCountValidationRequired = rowCountText != null && !rowCountText.isEmpty();
                boolean preReqFileValidationRequired = preReqFileListCSV != null && !preReqFileListCSV.isEmpty();

                boolean isRowCountValid = false;
                boolean isPreRequisiteFilePresent = true;

                if (isRowCountValidationRequired) {
                    isRowCountValid = validateFileRowCount();
                }

                if (preReqFileValidationRequired) {
                    isPreRequisiteFilePresent = isPreRequisiteFilePresent(fileWatcher.getFilePath(), preReqFileListCSV);
                    trace("isPreRequisiteFilePresent: " + isPreRequisiteFilePresent);
                }

                // a row count mismatch is file level (submission) validation error.  Do not process any rows.
                if (isRowCountValidationRequired && !isRowCountValid) {
                    throw new FileProcessingException(
                        String.format(
                            "[%s] cannot be processed because the actual row count does not match the expected row count.",
                            fileWatcher.getFileName()
                        )
                    );
                }

                if (preReqFileValidationRequired && isPreRequisiteFilePresent) {
                    alert(String.format("Waiting to process [%s} because Pre-requisite file has not been processed.", fileWatcher.getFileName()));
                } else {
                    // normal processing flow
                    if( appName.equals("OutboundOrderDownload"))
                  	  processOutboundOrderFile();
                    else if( appName.equals("FullcaseScanTunnelDownload"))
                  	  processFullcaseFile();
                    else
                  	  processFile();
                    RDSEvent.stop(appName);
                    downloadSuccess = true;

                    fileWatcher.archiveFile();
                    fileWatcher.reset();
                    GeekTranslator geekTranslator = GeekTranslatorFactory.getGeekTranslatorImpl(appName);
                    geekTranslator.moveToGeek();
                    RDSEvent.stop(appName+ " geek translator");
                }
            }
        } catch (FileProcessingException ex) {
            alert(ex.getMessage());
            if( downloadSuccess )
            	RDSEvent.start(appName + " geek translator");
            else
            	RDSEvent.start(appName);
        }
    }

    public boolean validateFileRowCount() throws FileProcessingException {
        int actualLineCount = 0;
        int expectedLineCount = 0;
        File f = fileWatcher.getFile();

        try (BufferedReader reader =
              new BufferedReader( new FileReader( f) );) {
      	   String line;
            while((line = reader.readLine()) != null) {
//                trace("%d: %s", actualLineCount+1, line);
                if (line.toUpperCase().contains(rowCountText)) {
                    String[] fields = line.split("\\^");
                    expectedLineCount = Integer.parseInt(fields[1].trim());
                    break;
                }
                actualLineCount++;
            }
            trace(String.format("Expected row count [%d].  Actual row count: [%d]", expectedLineCount, actualLineCount));
            if (expectedLineCount != actualLineCount) {
                return false;
            }
        } catch (IOException e) {
            alert("Error occurred while validating file row count: " + e);
            throw new FileProcessingException("Error occurred while validating file row count." + e);
        }
        return true;
    }

    private void processFile() throws FileProcessingException {
        File f = fileWatcher.getFile();
        int currentRow = 1;

        try (BufferedReader reader =
              new BufferedReader( new FileReader( f) );) {
      	  String line;
            while ((line = reader.readLine()) != null) {
                if (line != null) {

                    if (rowCountText.isBlank() ||
                            !line.toUpperCase().contains(rowCountText.toUpperCase())) {

                        String[] fields = line.split("\\^");
//                        System.out.println("current fields["+ fields.length +"]");
//                        System.out.println("expectedFieldCount["+expectedFieldCount+"]");
                        if (fields.length != expectedFieldCount) {
                            // if number of fields in row does not match what is expected, log an error, but do not
                            // throw an exception
                            alert("Row #[%d]: Expected [%d] fields, but received [%d] fields.", currentRow, expectedFieldCount, fields.length);
                            this.hostLog.add(
                                    String.format("Row #[%d]: Expected [%d] fields, but received [%d] fields.", currentRow, expectedFieldCount, fields.length),
                                    appName,
                                    "RowNumber",
                                    String.valueOf(currentRow),
                                    true
                            );
                        } else {
                            FileRecord record = FileRecordFactory.getFileRecordImpl(appName, fields);
                            List<String> validationErrors = record.validate();
                            if (validationErrors.isEmpty()) {
                                record.persist();
                            } else {
                                String concatMessage = "";
                                for (String msg : validationErrors) {
                                    concatMessage = concatMessage.concat(msg);
                                }
                                this.hostLog.add(
                                        String.format("Row [%d] validation errors: [%s]", currentRow, concatMessage),
                                        appName,
                                        "RowNumber",
                                        String.valueOf(currentRow),
                                        true
                                );
                            }
                        }
                        currentRow++;
                    }
                }
            }
        } catch(IOException | SQLException | DataAccessException e) {
            throw new FileProcessingException(e.toString());
        }
    }
    
    private void processOutboundOrderFile() throws FileProcessingException {
       File f = fileWatcher.getFile();
       int currentRow = 1;
       String fileName = fileWatcher.getFileName();
       try (BufferedReader reader =
              	new BufferedReader( new FileReader( f) );) {
      	  Map<String,Integer> waves = new HashMap<>();
      	  HashSet<String> orders = new HashSet<>();
      	  String line;
      	  boolean firstWave = true;
      	  int fileSeq = -1;
      	  while ((line = reader.readLine()) != null) {
               if (line != null) {

                   if (rowCountText.isBlank() ||
                           !line.toUpperCase().contains(rowCountText.toUpperCase())) {

                       String[] fields = line.split("\\^");
                       if (fields.length != expectedFieldCount) {
                           // if number of fields in row does not match what is expected, log an error, but do not
                           // throw an exception
                           this.hostLog.add(
                                   String.format("Row #[%d]: Expected [%d] fields, but received [%d] fields.", currentRow, expectedFieldCount, fields.length),
                                   appName,
                                   "RowNumber",
                                   String.valueOf(currentRow),
                                   true
                           );
                       } else {
                     	   OutBoundOrderFileRecord fileRecord = new OutBoundOrderFileRecord(fields);
                           String waveName = fileRecord.getWaveName();
                           String orderId = fileRecord.getOrderId();
                           String lineId = fileRecord.getLineId();
                           String pageId = fileRecord.getPageId();
                     	   if( fileSeq < 0 ) {
                     	   	fileSeq = fileRecord.createFile( fileName );
                     	   	if( fileSeq == SQL_ERROR ) {
                                 this.hostLog.add(
                                       String.format("Row [%d] sql errors: [failed to create file]", currentRow),
                                       appName,
                                       "RowNumber",
                                       String.valueOf(currentRow),
                                       true
                                 		);  
                                 break;
                     	   	}
                     	   }
                           int waveSeq = -1;                           
                           List<String> validationErrors = fileRecord.validate();
                           if (validationErrors.isEmpty()) {
                              if( !waves.containsKey( waveName ) ) {
                              	waveSeq = fileRecord.checkWave( waveName, fileSeq );
                              	if( waveSeq == SQL_ERROR ) {
                                    this.hostLog.add(
                                          String.format("Row [%d] sql errors: [failed to create wave]", currentRow),
                                          appName,
                                          "RowNumber",
                                          String.valueOf(currentRow),
                                          true
                                    		);
                                    continue;
                              	} else if( waveSeq == DUPLICATE ) {
                                    this.hostLog.add(
                                          String.format("Row [%d] duplicate batch ID: [%s]", currentRow, waveName),
                                          appName,
                                          "RowNumber",
                                          String.valueOf(currentRow),
                                          true
                                    		);
                                    continue;
                              	} else {
                              		waves.put(waveName, waveSeq);
                                 	trace("wave list %s",waves.keySet().toString());
                                 	if( firstWave ) {
                                 		RdsWaveDAO.updateWaveCanReleaseFlag(waveSeq);
                                 		firstWave = false;
                                 	}
                              	}
                              } else {
                              	waveSeq = waves.get( waveName );
                              	//SloaneCommonDAO.postWaveLog(""+waveSeq, "fileDownload", "Start download");
                              }
                              if( !orders.contains( orderId ) ) {
                              	int result = fileRecord.checkOrder( orderId, waveSeq );
                              	if( result == DUPLICATE ) {
                                    this.hostLog.add(
                                          String.format("Row [%d] duplicate invoice: [%s]", currentRow, orderId),
                                          appName,
                                          "RowNumber",
                                          String.valueOf(currentRow),
                                          true
                                    		);
                                    continue;                           		
                              	} else {
                              		orders.add(orderId);
                              		trace("Start downloading order %s",orderId);
                              		SloaneCommonDAO.postOrderLog(orderId, "fileDownload", "Start download");
                              		trace("order list %s",orders.toString());
                              	}
                              }
                              if( fileRecord.checkOrderLine(orderId, pageId, lineId) == DUPLICATE ) {
                                 this.hostLog.add(
                                       String.format("Row [%d] duplicate invoice/page/line: [%s/%s/%s]", currentRow, orderId,pageId,lineId),
                                       appName,
                                       "RowNumber",
                                       String.valueOf(currentRow),
                                       true
                                 		);
                                 continue;                           	
                              }
                               fileRecord.createLine();
                           } else {
                               String concatMessage = "";
                               for (String msg : validationErrors) {
                                   concatMessage = concatMessage.concat(msg);
                               }
                               this.hostLog.add(
                                       String.format("Row [%d] validation errors: [%s]", currentRow, concatMessage),
                                       appName,
                                       "RowNumber",
                                       String.valueOf(currentRow),
                                       true
                               );
                           }
                       }
                       currentRow++;
                   }
               }
           }
      	  trace("order list %s", orders.toString() );
      	  trace("wave list %s", waves.toString() );
      	  for( String orderId: orders ) {
      		  trace("orderId %s is downloaded", orderId);
      		  CustOrderDAO.setStatusAndTombStone(orderId, "downloaded", "downloadStamp");
      		  SloaneCommonDAO.postOrderLog(orderId, "fileDownload", "Order is downloaded");
      	  }
           for( int waveSeq: waves.values() ) {
         	  trace("waveSeq %d is created", waveSeq);
         	  RdsWaveDAO.setTombstone(waveSeq, "createStamp");
         	  SloaneCommonDAO.postWaveLog(""+waveSeq, "fileDownload", "Wave created");
           }
           CustOutboundOrderFileDAO.setTombstone(fileSeq, "downloadStamp");
       } catch(IOException | SQLException | DataAccessException e) {
           throw new FileProcessingException(e.toString());
       }
   }    
    
    private void processFullcaseFile() throws FileProcessingException {
       File f = fileWatcher.getFile();
       int currentRow = 1;

       try (BufferedReader reader =
              	new BufferedReader( new FileReader( f) );) {
      	  Map<String,Integer> lines = new HashMap<>();
      	  String line;
      	  while ((line = reader.readLine()) != null) {
               if (line != null) {

                   if (rowCountText.isBlank() ||
                           !line.toUpperCase().contains(rowCountText.toUpperCase())) {

                       String[] fields = line.split("\\^");
                       if (fields.length != expectedFieldCount) {
                           // if number of fields in row does not match what is expected, log an error, but do not
                           // throw an exception
                           this.hostLog.add(
                                   String.format("Row #[%d]: Expected [%d] fields, but received [%d] fields.", currentRow, expectedFieldCount, fields.length),
                                   appName,
                                   "RowNumber",
                                   String.valueOf(currentRow),
                                   true
                           );
                       } else {
                     	  FullcaseScanTunnelFileRecord fileRecord = new FullcaseScanTunnelFileRecord(fields);
                           String orderId = fileRecord.getOrderId();
                           String pageId = fileRecord.getPageId();
                           String lineId = fileRecord.getLineId();
                           String trackingPrefix = String.format("%s%s%s", orderId,pageId,lineId);
                           int fullcaseLineSeq = -1;                           
                           List<String> validationErrors = fileRecord.validate();
                           if (validationErrors.isEmpty()) {
                           	if( !fileRecord.getCartonIndicator().equals("1") ) {
                           		inform("Row [%d] not for fullcase, ignore",currentRow);
                           		currentRow++;
                           		continue;
                           	}
                              if( !lines.containsKey( trackingPrefix ) ) {
                              	fullcaseLineSeq = fileRecord.checkFullcaseLine(orderId, pageId, lineId);
                              	if( fullcaseLineSeq == SQL_ERROR ) {
                                    this.hostLog.add(
                                          String.format("Row [%d] sql errors: [failed to create fullCase line]", currentRow),
                                          appName,
                                          "RowNumber",
                                          String.valueOf(currentRow),
                                          true
                                    		);
                                    currentRow++;
                                    continue;
                              	} else if( fullcaseLineSeq == DUPLICATE ) {
//                                    this.hostLog.add(
//                                          String.format("Row [%d] duplicate fullcase invoice/page/line: [%s]", currentRow, trackingPrefix),
//                                          appName,
//                                          "RowNumber",
//                                          String.valueOf(currentRow),
//                                          true
//                                    		);
                                    currentRow++;
                                    continue;
                              	} else {
                              		lines.put(trackingPrefix, fullcaseLineSeq);
                              	}
                              } else {
                              	fullcaseLineSeq = lines.get( trackingPrefix );
                              }
                              fileRecord.createLineData(fullcaseLineSeq);
                           } else {
                               String concatMessage = "";
                               for (String msg : validationErrors) {
                                   concatMessage = concatMessage.concat(msg);
                               }
                               this.hostLog.add(
                                       String.format("Row [%d] validation errors: [%s]", currentRow, concatMessage),
                                       appName,
                                       "RowNumber",
                                       String.valueOf(currentRow),
                                       true
                               );
                           }
                       }
                       currentRow++;
                   }
               }
           }
           for( int fullcaseLineSeq: lines.values() ) {
         	  CustFullcaseLinesDAO.setTombstone(fullcaseLineSeq, "downloadStamp");
           }
       } catch(IOException | SQLException | DataAccessException e) {
           throw new FileProcessingException(e.toString());
       }
   }        

    private boolean isPreRequisiteFilePresent(String filePath, String preReqFileListCSV) {
        String[] preRequisiteFiles = preReqFileListCSV.split(",");

        for (String preRequisiteFile : preRequisiteFiles) {
            // file extension are TBD
            File fileObj = new File(filePath + preRequisiteFile);
            boolean fileExists = fileObj.exists(); // this needs to change to match pattern rather than exact string
            if (fileExists) {
                alert("[%s] file found in [%s] directory, delaying this file processing", preRequisiteFile,
                        filePath);
                return true;
            }
        }
        return false;

    }// isPreRequisiteFilePresent()

}