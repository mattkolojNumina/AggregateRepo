package dao;

import host.ConnectionPool;
import rds.RDSUtil;
import records.RdsCartons;

import java.sql.*;
import java.util.*;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class RdsCartonsDAO extends AbstractDAO {

    public List<String> getAcknowledgedOrders() {
        String SELECT_ACKNOWLEDGED_CARTONS = " SELECT out_order_code FROM geekPickOrder gpo "
                + " JOIN rdsCartons rc "
                + " ON gpo.out_order_code = rc.trackingNumber "
                + " WHERE gpo.`processed` = 'yes' "
                + " AND rc.geekStatus = 'sent' ";
        return db.getValueList(SELECT_ACKNOWLEDGED_CARTONS);
    }

    public List<String> getEstimatedToteContentsToUpload() {
        String SELECT_CARTONS_TO_UPLOAD_TOTE_CONTENTS = " SELECT cartonSeq FROM rdsCartons car "
                + " JOIN custOrders ords "
                + " ON car.orderId = ords.orderId "
                + " WHERE car.createStamp IS NOT NULL "
                + " AND ords.status = 'cartonized' "
                + " AND ords.cartonizeStamp IS NOT NULL "
                + " AND car.estContentsUploadStamp IS NULL "
                + " ORDER BY createStamp; ";
        return db.getValueList(SELECT_CARTONS_TO_UPLOAD_TOTE_CONTENTS);
    }

    public List<Map<String, String>> getEstimatedOrderLinesPerToteToUpload(int cartonSeq) {
        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ORDER_LINES_FOR_TOTE = "SELECT w.waveName, o.groupNumber, ol.orderId, ol.pageId, ol.lineId, c.cartonCount, c.lpn, c.pickType, ol.orderLineSeq " +
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

    public List<Map<String, String>> getActualOrderLinesPerToteToUpload(int cartonSeq) {
        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ORDER_LINES_FOR_TOTE = "SELECT w.waveName, ol.groupNumber, ol.orderId, ol.pageId, ol.lineId, c.cartonCount, c.lpn, c.pickType, ol.orderLineSeq, p.lineNumber " +
                "FROM rdsCartons c " +
                "JOIN custOrders o " +
                "ON c.orderId = o.orderId " +
                "JOIN rdsPicks p " +
                "ON p.cartonSeq = c.cartonSeq " + // change here 0709 for ECOM rearrange joins
                "JOIN custOrderLines ol " +
                "ON ol.orderLineSeq = p.orderLineSeq " +
                "JOIN rdsWaves w " +
                "ON w.waveSeq = o.waveSeq " +
                "WHERE  c.cartonSeq = (?) " +
                "AND  p.picked = 1 " +
                "AND  p.shortPicked = 0 " +
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

    public List<String> getActualToteContentsToUpload() {
        String SELECT_CARTONS_TO_UPLOAD_TOTE_CONTENTS = " SELECT cartonSeq FROM rdsCartons car "
                + " WHERE car.labelStamp IS NOT NULL "
                + " AND car.estContentsUploadStamp IS NOT NULL "
                + " AND car.actContentsUploadStamp IS NULL ";
        return db.getValueList(SELECT_CARTONS_TO_UPLOAD_TOTE_CONTENTS);
    }

    public List<String> getEstimatedTotesPerOrderToUpload() {
        String SELECT_TOTES_PER_ORDER_ESTIMATE = " SELECT orderId FROM custOrders o " +
                " WHERE o.status = 'cartonized' " +
                " AND o.cartonizeStamp IS NOT NULL " +
                " AND o.cartonizedUploadStamp IS NULL " +
                " ORDER BY cartonizeStamp; ";
        return db.getValueList(SELECT_TOTES_PER_ORDER_ESTIMATE);
    }

    public List<String> getActualTotesPerOrderToUpload() {
        String SELECT_TOTES_PER_ORDER_ESTIMATE = " SELECT orderId FROM custOrders o " +
                " WHERE o.labelStamp IS NOT NULL " +
                " AND o.uploadStamp IS NULL " +
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

    public int totalActualTotesByOrder( String orderId ) throws DataAccessException {
        int totalToteCount =0 ;
        String sql = String.format(
                "SELECT count(*) FROM rdsCartons WHERE orderId = '%s' AND pickType <> 'FullCase' AND cancelStamp IS NULL", orderId);
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
