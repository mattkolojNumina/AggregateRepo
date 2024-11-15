package host;

import dao.AbstractDAO;
import org.apache.commons.io.FileUtils;
import rds.RDSEvent;
import rds.RDSDatabase;


import static rds.RDSUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class FileWriter extends AbstractDAO {
    String pathName = "";
    String extName = "";
    String tempPathName = "";
    String fileNamePrefix = "";
    FileWatcher fw;
    RDSDatabase db;

    private String HEADER;
    private String FOOTER;

    public FileWriter(FileWatcher fw){

        pathName = fw.getFilePath();
        extName = fw.getFileExtension().trim();
        tempPathName = fw.getTempFilePath().trim();
        fileNamePrefix = fw.getFileName().trim();
        db = new RDSDatabase("db");
        RDSEvent.setDatabase(db);

    }


    public boolean writeLine(
            List<List<String>> linesList, String interfaceId) 
    throws IOException {
        
        //chooses header & footer based on the interfaceId
        chooseHeaderFooter(interfaceId);

        //create final line
        String lines = concatenateNestedLists(linesList);
        lines = HEADER + lines + FOOTER;
        String UUIDString = String.valueOf(UUID.randomUUID());
        String tempFileName = tempPathName + fileNamePrefix + getDateTime() + "_" + UUIDString + "." + extName;
        File file1 = new File(tempFileName);

        if (!file1.isFile()) {
            String fileName = pathName + fileNamePrefix + getDateTime() + "_" + UUIDString + "." + extName;
            File file2 = new File(fileName);
            if (!file2.isFile()) {
                RandomAccessFile stream = new RandomAccessFile(tempFileName, "rw");
                FileChannel channel = stream.getChannel();

                byte[] strBytes = lines.getBytes();
                ByteBuffer buffer = ByteBuffer.allocate(strBytes.length);
                buffer.put(strBytes);
                buffer.flip();
                int success = channel.write(buffer);
                stream.close();
                channel.close();
                trace("created file: [%s]", tempFileName);

                trace("moving to: [%s]", fileName);
                FileUtils.moveFile(new File(tempFileName), new File(fileName));

                if(success>0){
                    return true;
                }else{
                    RDSEvent.instant("FILE_CREATION_FAILED","creation failed for file: [" + tempFileName + "]");
                    return false;
                }
            }else{
                alert("[%s] already exists",fileName);
                return false;
            }
        }else{
            alert("[%s] already exists",tempFileName);
            return false;
        }
    }

    private String getDateTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        // Get the current date and time
        Date currentDate = new Date();

        // Format the date and time using the SimpleDateFormat
        String timestamp = dateFormat.format(currentDate);
        return timestamp;
    }

    // Function to concatenate nested lists with "^" delimiter and end each line
    // with "<CR><LF>"
    private static String concatenateNestedLists(List<List<String>> listOfLines) {
        // Concatenate each inner list with "^" delimiter
        List<String> concatenatedLists = listOfLines.stream()
                .map(innerList -> innerList.stream().collect(Collectors.joining("^")))
                .collect(Collectors.toList());

        // Join the lines with "<CR><LF>"
        String result = String.join("\r\n", concatenatedLists);

        // Append "<CR><LF>" to the final result
        result += "\r\n";

        // Return the result
        return result;
    }

   
    private boolean chooseHeaderFooter(String interfaceId){


        //910
        if(interfaceId.equals("ACTUALS_FILE") ){

            HEADER = 
            "* $$ JOB JNM=RDS910CP,CLASS=V,DISP=D,USER=RDS.001\n" +
                    "// JOB RDS910CP\n" +
                    "// OPTION PARTDUMP\n" +
                    "* **************************************************************\n" +
                    "* *** DO NOT RUN RDS910CP WITH RDS911CP ***\n" +
                    "* *** DO NOT RUN RDS910CP WITH RDS911CP ***\n" +
                    "* *** DO NOT RUN RDS910CP WITH RDS911CP ***\n" +
                    "* *** DO NOT RUN RDS910CP WITH RDS911CP ***\n" +
                    "* **************************************************************\n" +
                    "* STEP 0 WRITE BEGINNING JOB SCHEDULE RECORD\n" +
                    "// EXEC PROC=F99X07\n" +
                    "// EXEC C99085,SIZE=(C99085,64K)\n" +
                    "BRDS910CP\n" +
                    "/*\n" +
                    "* STEP 1 LOAD DIV 001 RDS/GEEK PICK/INV DATA TO SCRATCH FILE\n" +
                    "// ASSGN SYSLST,IGN\n" +
                    "// DLBL S1C0X01,'RDS910CP.VOICE.PICK.DIV.001'\n" +
                    "// EXEC C04MPAR,SIZE=(C04MPAR,64K)\n";
        
            FOOTER = 
            "####### RDS TRAILER RECORD\n" +
                    "/*\n" +
                    "* STEP 2 JOB RDS911CP\n" +
                    "// EXEC PROC=BIMELIBD\n" +
                    "// EXEC BIMUTIL\n" +
                    "LOGON BIMBAT,BATCH\n" +
                    "JOB RDS911CP\n" +
                    "/*\n" +
                    "* STEPLAST WRITE ENDING JOB SCHEDULE RECORD\n" +
                    "// EXEC PROC=F99X07\n" +
                    "// EXEC C99085,SIZE=(C99085,64K)\n" +
                    "ERDS910CP\n" +
                    "/*\n" +
                    "/&\n" +
                    "* $$ EOJ\n";

            return true;

        }

        //510
        else if(interfaceId.equals("SNAPSHOT_RECON_FILE")){
            HEADER = 
            "* $$ JOB JNM=RDS510CP,CLASS=V,DISP=D,USER=RDS.002\n" +
                    "// JOB RDS510CP\n" +
                    "// OPTION PARTDUMP\n" +
                    "* **************************************************************\n" +
                    "* *** DO NOT RUN RDS510CP WITH RDS511CP ***\n" +
                    "* *** DO NOT RUN RDS510CP WITH RDS511CP ***\n" +
                    "* *** DO NOT RUN RDS510CP WITH RDS511CP ***\n" +
                    "* *** DO NOT RUN RDS510CP WITH RDS511CP ***\n" +
                    "* **************************************************************\n" +
                    "* STEP 0 WRITE BEGINNING JOB SCHEDULE RECORD\n" +
                    "// EXEC PROC=F99X07\n" +
                    "// EXEC C99085,SIZE=(C99085,64K)\n" +
                    "BRDS510CP\n" +
                    "/*\n" +
                    "* STEP 1 LOAD DIV 001 RDS NIGHTLY RECON DATA TO SCRATCH FILE\n" +
                    "// ASSGN SYSLST,IGN\n" +
                    "// DLBL S1C0X01,'RDS510CP.RDS.DATA.DIV.001'\n" +
                    "// EXEC C04MPAI,SIZE=(C04MPAI,64K)\n";

            FOOTER = 
            "####### RDS TRAILER RECORD\n" +
                    "/*\n" +
                    "* STEP 2 JOB RDS511CP\n" +
                    "// EXEC PROC=BIMELIBD\n" +
                    "// EXEC BIMUTIL\n" +
                    "LOGON BIMBAT,BATCH\n" +
                    "JOB RDS511CP\n" +
                    "/*\n" +
                    "* STEPLAST WRITE ENDING JOB SCHEDULE RECORD\n" +
                    "// EXEC PROC=F99X07\n" +
                    "// EXEC C99085,SIZE=(C99085,64K)\n" +
                    "ERDS510CP\n" +
                    "/*\n" +
                    "/&\n" +
                    "* $$ EOJ\n";

            return true;

        }

        //610
        else if(interfaceId.equals("ESTIMATES_FILE")){

            HEADER =
                    "* $$ JOB JNM=RDS610CP,CLASS=V,DISP=D,USER=RDS.001\n" +
                            "// JOB RDS610CP\n" +
                            "// OPTION PARTDUMP\n" +
                            "* **************************************************************\n" +
                            "* *** DO NOT RUN RDS610CP WITH RDS611CP ***\n" +
                            "* *** DO NOT RUN RDS610CP WITH RDS611CP ***\n" +
                            "* *** DO NOT RUN RDS610CP WITH RDS611CP ***\n" +
                            "* *** DO NOT RUN RDS610CP WITH RDS611CP ***\n" +
                            "* **************************************************************\n" +
                            "* STEP 0 WRITE BEGINNING JOB SCHEDULE RECORD\n" +
                            "// EXEC PROC=F99X07\n" +
                            "// EXEC C99085,SIZE=(C99085,64K)\n" +
                            "BRDS610CP\n" +
                            "/*\n" +
                            "* STEP 1 LOAD DIV 001 RDS INITIAL CARTONIZATION RECORDS FLAG N\n" +
                            "// ASSGN SYSLST,IGN\n" +
                            "// DLBL S1C0X01,'RDS610CP.RDS.CARTON.DIV.001'\n" +
                            "// EXEC C04MPAR,SIZE=(C04MPAR,64K)\n";

            FOOTER =
                    "####### RDS TRAILER RECORD\n" +
                            "/*\n" +
                            "* STEP 2 JOB RDS611CP\n" +
                            "// EXEC PROC=BIMELIBD\n" +
                            "// EXEC BIMUTIL\n" +
                            "LOGON BIMBAT,BATCH\n" +
                            "JOB RDS611CP\n" +
                            "/*\n" +
                            "* STEPLAST WRITE ENDING JOB SCHEDULE RECORD\n" +
                            "// EXEC PROC=F99X07\n" +
                            "// EXEC C99085,SIZE=(C99085,64K)\n" +
                            "ERDS610CP\n" +
                            "/*\n" +
                            "/&\n" +
                            "* $$ EOJ\n";

            return true;

        }

        //310
        else if(interfaceId.equals("ACTUALS_FILE_TEST")){

            HEADER =
                    "* $$ JOB JNM=RDS310CP,CLASS=V,DISP=D,USER=RDS.001\n" +
                            "// JOB RDS310CP\n" +
                            "// OPTION PARTDUMP\n" +
                            "* **************************************************************\n" +
                            "* *** DO NOT RUN RDS310CP WITH RDS311CP ***\n" +
                            "* *** DO NOT RUN RDS310CP WITH RDS311CP ***\n" +
                            "* *** DO NOT RUN RDS310CP WITH RDS311CP ***\n" +
                            "* *** DO NOT RUN RDS310CP WITH RDS311CP ***\n" +
                            "* **************************************************************\n" +
                            "* STEP 0 WRITE BEGINNING JOB SCHEDULE RECORD\n" +
                            "// EXEC PROC=F99X07\n" +
                            "// EXEC C99085,SIZE=(C99085,64K)\n" +
                            "BRDS310CP\n" +
                            "/*\n" +
                            "* STEP 1 LOAD DIV 001 RDS/GEEK PICK/INV DATA TO SCRATCH FILE\n" +
                            "// ASSGN SYSLST,IGN\n" +
                            "// DLBL S1C0X01,'RDS310CP.VOICE.PICK.DIV.001'\n" +
                            "// EXEC C04MPAR,SIZE=(C04MPAR,64K)\n";

            FOOTER =
                    "####### RDS TRAILER RECORD\n" +
                            "/*\n" +
                            "* STEP 2 JOB RDS311CP\n" +
                            "// EXEC PROC=BIMELIBD\n" +
                            "// EXEC BIMUTIL\n" +
                            "LOGON BIMBAT,BATCH\n" +
                            "JOB RDS311CP\n" +
                            "/*\n" +
                            "* STEPLAST WRITE ENDING JOB SCHEDULE RECORD\n" +
                            "// EXEC PROC=F99X07\n" +
                            "// EXEC C99085,SIZE=(C99085,64K)\n" +
                            "ERDS310CP\n" +
                            "/*\n" +
                            "/&\n" +
                            "* $$ EOJ\n";


            return true;

        }


        else{
            alert("unknown interface Id, [%s]",interfaceId);
            return false;
        }
    }

}
