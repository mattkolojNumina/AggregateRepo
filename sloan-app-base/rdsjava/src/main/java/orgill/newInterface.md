New host interface

Set up controls entry for new file (ex: "UserDownload")
"filePath",
"fileName",
"archiveFilePath",
"expectedFieldCount",
"preReqFileCSV"

Create concrete implementation of FileRecord interface.  
Structure of incoming file
Handle trimming of fields
Add any enumerations, constant values relevant to the file.
Implement validate method.  
Implement persist method which should delegate persistence to a DAO.

Add creation of above to FileRecordFactory

If one doesn't already exist, create a DAO implementation for the target table(s).
DAO implementations can add methods beyond the interface.  DAOs will be instantiated and referenced as their specific type not as the interface type.