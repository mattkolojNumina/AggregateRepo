package dao;

import static rds.RDSLog.*;
import static rds.RDSUtil.trace;

import java.util.*;
import java.sql.*;

import host.ConnectionPool;

public class GeekDAO{

    public Map<String, String> get(String tableName, int seq) {
        return null;
    }

    public List<Map<String, String>> getAll(String tableName) {
        return new ArrayList<>();
    }

    public List<Map<String, String>> getAllProcessedOrdered(String tableName, String processed, String orderByColName, String orderBy) {
        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ALL_PROCESSED_ORDERBY = "SELECT * FROM rds.$tableName WHERE processed = (?) ORDER BY $orderByColName $orderBy";
        String query = SELECT_ALL_PROCESSED_ORDERBY.replace("$tableName", tableName).replace("$orderByColName", orderByColName).replace("$orderBy", orderBy);

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(query)) {
            stmt.setString(1, processed);

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

    public Map<String, String> getByKey(int seq, String tableName) throws DataAccessException {
        Map<String, String> row = new HashMap<>();
        String SELECT_ALL_BY_KEY = "SELECT * FROM rds.$tableName WHERE seq = (?)";
        String query = SELECT_ALL_BY_KEY.replace("$tableName", tableName);

        try (Connection cxn = ConnectionPool.getConnection();
                PreparedStatement stmt = cxn.prepareStatement(query)) {
            stmt.setInt(1, seq);

            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                resultSet.next();
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    row.put(columnName, columnValue);
                }
                
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return row;
    }

    public String getValueByKey(int seq, String tableName, String colName) throws DataAccessException {
        String value = "";
        String SELECT_VALUE_BY_KEY = "SELECT $colName FROM rds.$tableName WHERE seq = (?)";
        String query  = SELECT_VALUE_BY_KEY.replace("$tableName", tableName).replace("$colName", colName);

        try (Connection cxn = ConnectionPool.getConnection();
                PreparedStatement stmt = cxn.prepareStatement(query)) {
            stmt.setInt(1, seq);

            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                resultSet.next();

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    value = columnValue;
                }

            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return value;
    }
    
    public List<Map<String, String>> getAllByParent(int parentSeq, String tableName) throws DataAccessException {
        List<Map<String, String>> rows = new ArrayList<>();
        String SELECT_ALL_BY_PARENT = "SELECT * FROM rds.$tableName WHERE parent = (?)";
        String query = SELECT_ALL_BY_PARENT.replace("$tableName", tableName);
        try (Connection cxn = ConnectionPool.getConnection();
                PreparedStatement stmt = cxn.prepareStatement(query)) {
            stmt.setInt(1, parentSeq);

            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while (resultSet.next()) {
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

    public int updateProcessed(String tableName, String processed, int seq) throws DataAccessException {

        String UPDATE_PROCESSED_STATEMENT = "UPDATE rds.$tableName SET processed = (?) WHERE seq = (?)";
        String query = UPDATE_PROCESSED_STATEMENT.replace("$tableName", tableName);

        try (Connection cxn = ConnectionPool.getConnection();
                PreparedStatement stmt = cxn.prepareStatement(query)) {

            stmt.setString(1, processed);
            stmt.setInt(2, seq);

            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
        
    }


}
