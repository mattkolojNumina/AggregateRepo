package dao;

import host.ConnectionPool;
import records.RdsOrderLineConfirmationUpload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static rds.RDSLog.alert;

public class RdsOrderLineConfirmationUploadDAO {

    private String INSERT_INTO_rdsOrderLineConfirmationUpload =
            "INSERT INTO `rds`.`rdsOrderLineConfirmationUpload` " +
                    "(" +
                    "`typeCode`, " +
                    "`batchId`, " +
                    "`groupNumber`, " +
                    "`orderNumber`, " +
                    "`pageNumber`, " +
                    "`lineNumber`, " +
                    "`qtyOrdered`, " +
                    "`changedQtyFlag`, " +
                    "`qtyPicked`, " +
                    "`numOfTotes`, " +
                    "`operatorId`," +
                    "`QPAGroup`" +
                    ")" +
                    "VALUES" +
                    "(" +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?), " +
                    "(?)" +
                    ");";

    public int save(RdsOrderLineConfirmationUpload rdsOrderLineConfrimationUpload) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_INTO_rdsOrderLineConfirmationUpload)) {
            stmt.setString(1, rdsOrderLineConfrimationUpload.getTypeCode());
            stmt.setString(2, rdsOrderLineConfrimationUpload.getBatchId());
            stmt.setString(3, rdsOrderLineConfrimationUpload.getGroupNumber());
            stmt.setString(4, rdsOrderLineConfrimationUpload.getOrderNumber());
            stmt.setString(5, rdsOrderLineConfrimationUpload.getPageNumber());
            stmt.setString(6, rdsOrderLineConfrimationUpload.getLineNumber());
            stmt.setString(7, rdsOrderLineConfrimationUpload.getQtyOrdered());
            stmt.setString(8, rdsOrderLineConfrimationUpload.getChangedQtyFlag());
            stmt.setString(9, rdsOrderLineConfrimationUpload.getQtyPicked());
            stmt.setString(10, rdsOrderLineConfrimationUpload.getNumOfTotes());
            stmt.setString(11, rdsOrderLineConfrimationUpload.getOperatorId());
            stmt.setString(12, rdsOrderLineConfrimationUpload.getQpaGroup());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

}

