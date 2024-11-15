package dao;

import host.ConnectionPool;
import rds.RDSUtil;

import records.CustOrder;
import records.RdsToteContentsUpload;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rds.RDSLog.alert;
import static rds.RDSUtil.trace;

public class RdsToteContentsUploadDAO {

    private String SELECT_FROM_rdsToteContentsUpload =
            " SELECT * FROM rds.rdsToteContentsUpload "
                    + " WHERE (typeCode = '17' OR typeCode = '18') "
                    + " AND finalFlag = 'Y' "
                    + " AND processed = 'no' "
                    + " ORDER BY createStamp ASC";

    public List<Map<String, String>> getAllUnprocessed() {
        List<Map<String, String>> rows = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_FROM_rdsToteContentsUpload)) {
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


    private String INSERT_INTO_rdsToteContentsUpload =

            "INSERT INTO `rds`.`rdsToteContentsUpload` " +
                    "(" +
                    "`typeCode`, " +
                    "`batchId`, " +
                    "`groupNumber`, " +
                    "`lineSeq`, " +
                    "`orderNumber`, " +
                    "`pageNumber`, " +
                    "`lineNumber`, " +
                    "`toteNumber`, " +
                    "`totalTotes`, " +
                    "`finalFlag`, " +
                    "`cartonLpn`, " +
                    "`toteSeqNumber`, " +
                    "`origin`" +
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
                    "(?), " +
                    "(?)" +
                    ");";

    public int save(RdsToteContentsUpload rdsToteContentsUpload) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();

            PreparedStatement stmt = cxn.prepareStatement(INSERT_INTO_rdsToteContentsUpload)) {

            stmt.setString(1, rdsToteContentsUpload.getTypeCode());
            stmt.setString(2, rdsToteContentsUpload.getBatchId());
            stmt.setString(3, rdsToteContentsUpload.getGroupNumber());
            stmt.setString(4, rdsToteContentsUpload.getLineSeq());
            stmt.setString(5, rdsToteContentsUpload.getOrderNumber());
            stmt.setString(6, rdsToteContentsUpload.getPageNumber());
            stmt.setString(7, rdsToteContentsUpload.getLineNumber());
            stmt.setString(8, rdsToteContentsUpload.getToteNumber());
            stmt.setString(9, rdsToteContentsUpload.getTotalTotes());
            stmt.setString(10, rdsToteContentsUpload.getFinalFlag());
            stmt.setString(11, rdsToteContentsUpload.getCartonLpn());
            stmt.setString(12, rdsToteContentsUpload.getToteSeqNumber());
            stmt.setString(13, rdsToteContentsUpload.getOrigin());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }





    public List<String> getAllUnprocessedEstimates(int fileSeq) {
        List<String> rows = new ArrayList<>();

        String SELECT_FROM_ESTIMATED_TOTE_CONTENTS=
                " SELECT c.cartonSeq FROM `custOutboundOrderFiles` coof " +
                        " JOIN rdsWaves w " +
                        " ON coof.fileSeq = w.fileSeq " +
                        " JOIN custOrders o " +
                        " ON o.waveSeq = w.waveSeq " +
                        " JOIN rdsCartons c " +
                        " ON c.orderId = o.orderId " +
                        " WHERE coof.fileSeq = (?) " +
                        " AND o.cartonizeStamp IS NOT NULL " +
                        " AND c.estContentsUploadStamp IS NULL " +
                        " ORDER BY c.cartonSeq; ";

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_FROM_ESTIMATED_TOTE_CONTENTS)) {
            stmt.setInt(1,fileSeq);
            boolean areThereResults = stmt.execute();
            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while(resultSet.next()) {
                    String row = "";
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnValue = resultSet.getString(i);
                        row=columnValue;
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return rows;

    }


    public List<Map<String, String>> getEstimatedOrderLinesPerToteToUpload(int cartonSeq) {
        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ORDER_LINES_FOR_TOTE = "SELECT w.waveName, o.groupNumber, ol.orderId, ol.pageId, ol.lineId, c.cartonCount, c.lpn, c.pickType, ol.orderLineSeq, p.lineNumber " +
                "FROM rdsCartons c " +
                "JOIN custOrders o " +
                "ON c.orderId = o.orderId " +
                "JOIN rdsPicks p " +
                "ON p.cartonSeq = c.cartonSeq " +
                "JOIN custOrderLines ol " +
                "ON ol.orderLineSeq = p.orderLineSeq " +
                "JOIN rdsWaves w " +
                "ON w.waveSeq = o.waveSeq " +
                "WHERE  c.cartonSeq = (?) " +
                "GROUP BY p.orderLineSeq " +
                "ORDER BY p.orderLineSeq ";
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(SELECT_ORDER_LINES_FOR_TOTE)) {
            stmt.setInt(1, cartonSeq);

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


}

