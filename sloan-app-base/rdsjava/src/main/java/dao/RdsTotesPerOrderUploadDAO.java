package dao;

import host.ConnectionPool;
import rds.RDSUtil;
import records.CustOrder;
import records.RdsTotesPerOrderUpload;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rds.RDSLog.alert;
import static rds.RDSUtil.trace;


public class RdsTotesPerOrderUploadDAO extends AbstractDAO {

    public int save(RdsTotesPerOrderUpload rdsTotesPerOrderUpload) throws DataAccessException {
        String INSERT_INTO_rdsTotesPerOrderUploadDAO = "insert into `rds`.`rdsTotesPerOrderUpload`  " +
                " (  " +
                " `typeCode`,  " +
                " `finalFlag`,  " +
                " `batchId`,  " +
                " `groupNumber`,  " +
                " `orderNumber`,  " +
                " `totalTotes` " +
                " ) " +
                " values " +
                " ( " +
                " (?),  " +
                " (?),  " +
                " (?),  " +
                " (?),  " +
                " (?),  " +
                " (?) " +
                " );";
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_INTO_rdsTotesPerOrderUploadDAO)) {
            stmt.setString(1, rdsTotesPerOrderUpload.getTypeCode());
            stmt.setString(2, rdsTotesPerOrderUpload.getFinalFlag());
            stmt.setString(3, rdsTotesPerOrderUpload.getBatchId());
            stmt.setString(4, rdsTotesPerOrderUpload.getGroupNumber());
            stmt.setString(5, rdsTotesPerOrderUpload.getOrderNumber());
            stmt.setString(6, rdsTotesPerOrderUpload.getTotalTotes());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    public List<String> getAllUnprocessedEstimates(int fileSeq) {
        List<String> rows = new ArrayList<>();

        String SELECT_FROM_ESTIMATED_TOTES_PER_ORDER = " SELECT c.orderId FROM `custOutboundOrderFiles` coof " +
                "JOIN rdsWaves w " +
                "ON coof.fileSeq = w.fileSeq " +
                "JOIN custOrders o " +
                "ON o.waveSeq = w.waveSeq " +
                "JOIN rdsCartons c  " +
                "ON c.orderId = o.orderId  " +
                "WHERE coof.fileSeq = (?) " +
                "AND o.cartonizeStamp IS NOT NULL  " +
                "AND o.cartonizedUploadStamp IS NULL  " +
                "GROUP BY o.orderId; ";
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_FROM_ESTIMATED_TOTES_PER_ORDER)) {
            stmt.setInt(1,fileSeq);
            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while(resultSet.next()) {
                    String row = "";
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        row = resultSet.getString(i);
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return rows;

    }


    public List<Map<String, String>> getTotesPerOrderToUploadDetails(String orderId) {

        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ORDER_DETAILS_FOR_ORDER =
                "SELECT w.waveName, o.groupNumber,o.orderId " +
                        "FROM rdsCartons c " +
                        "JOIN custOrders o " +
                        "ON c.orderId = o.orderId " +
                        "JOIN rdsWaves w " +
                        "ON w.waveSeq = o.waveSeq " +
                        "WHERE o.orderId = (?)" +
                        "GROUP BY o.orderId;";
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(SELECT_ORDER_DETAILS_FOR_ORDER)) {
            stmt.setString(1, orderId);

            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while(resultSet.next()) {
                    Map<String, String> row = new HashMap<>();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        row.put(columnName, columnValue);
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            RDSUtil.trace(e.toString());
        }
        return rows;

    }

    public List<String> getEstimatedTotesPerOrderToUpload() {
        String SELECT_TOTES_PER_ORDER_ESTIMATE = " SELECT orderId FROM custOrders o " +
                " WHERE o.status = 'cartonized' " +
                " AND o.cartonizeStamp IS NOT NULL " +
                " AND o.cartonizedUploadStamp IS NULL " +
                " ORDER BY cartonizeStamp; ";
        return db.getValueList(SELECT_TOTES_PER_ORDER_ESTIMATE);
    }


    public int totalEstimatedTotesByOrder( String orderId ) throws DataAccessException {
        int totalToteCount =0 ;
        String sql = String.format(
                "SELECT MAX(cartonCount) FROM rdsCartons WHERE orderId = '%s'", orderId);
        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()){
            boolean areThereResults = stmt.execute(sql);
            if( areThereResults ) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()){
                    totalToteCount = rs.getInt( 1 ) ;
                }
            }
        }catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
        return totalToteCount;
    }


    private String SELECT_FROM_rdsTotesPerOrderUpload =
            " SELECT * FROM rds.rdsTotesPerOrderUpload "
                    + " WHERE (typeCode = '10') "
                    + " AND finalFlag = 'Y' "
                    + " AND processed = 'no' "
                    + " ORDER BY createStamp ASC";

    public List<Map<String, String>> getAllUnprocessed() {
        List<Map<String, String>> rows = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_FROM_rdsTotesPerOrderUpload)) {
            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while(resultSet.next()) {
                    Map<String, String> row = new HashMap<>();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        row.put(columnName, columnValue);
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return rows;

    }




}