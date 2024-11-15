package sloane;

import host.FileRecord;
import host.FileWatcher;

public class FileRecordFactory {

    //downloads
    public static FileRecord getFileRecordImpl(String appName, String[] fields) {
        if (appName.equals("UserDownload")) {
            return new UserFileRecord(fields);
        }
        if (appName.equals("PutawayOrderDownload")) {
            return new PutawayOrderFileRecord(fields);
        }
        if (appName.equals("ProductDownload")) {
            return new ProductFileRecord(fields);
        }
        if (appName.equals("BuyingDeptDownload")) {
            return new BuyingDeptFileRecord(fields);
        }
        if (appName.equals("LocationDownload")) {
           return new LocationFileRecord(fields);
        }
        if (appName.equals("CustomerDownload")) {
           return new CustomerFileRecord(fields);
        }
        if (appName.equals("OutboundOrderDownload")) {
           return new OutBoundOrderFileRecord(fields);
        }
        if (appName.equals("BackstockInventoryDownload")) {
            return new BackstockFileRecord(fields);
        }

        throw new IllegalArgumentException("The system is not configured for application name [" + appName + "]");
    }


    public static FileRecord getFileRecordImpl(String appName, FileWatcher fw) {
        if (appName.equals("GeekInventoryAdjustmentUpload")) {
            return new GeekInventoryAdjustmentsRecord(fw);
        }

        if (appName.equals("GeekInventorySnapshotReconUpload")) {
            return new GeekInventorySnapshotReconciliationRecord(fw);
        }

        if (appName.equals("RdsOperatorActivityRecordUpload")) {
            return new RdsOperatorActivityRecord(fw);
        }
        if (appName.equals("CombinedEstimatesUpload")) {
            return new CombinedEstimatesRecord(fw);
        }
        if (appName.equals("CombinedActualsUpload")) {
            return new CombinedActualsRecord(fw);
        }

        if (appName.equals("TotesPerOrderUpload")) {
            return new TotesPerOrderUpload(fw);
        }
      
        if (appName.equals("ToteContentsRecordUpload")) {
            return new ToteContentsRecord(fw);
        }
      
        if (appName.equals("OrderLineConfRecordUpload")) {
            return new OrderLineConfirmationRecord(fw);
        }
      
        throw new IllegalArgumentException("The system is not configured for application name [" + appName + "]");

    }

}
