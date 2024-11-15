package dao;

import host.ConnectionPool;
import host.geek.GeekTranslator;
import records.GeekSnapshot;

import java.sql.*;
import java.util.*;

import static rds.RDSUtil.*;

public class GeekSnapshotDAO extends AbstractDAO {


    public List<Map<String, String>> getUnprocessedGroupedBySku(){
        String SELECT_STATEMENT_PROCESSED =
                "SELECT seq, sku_code, SUM(amount) AS amount FROM geekSnapshot WHERE processed = 'no' GROUP BY sku_code;";

        List<Map<String, String>> unprocessedGeekSnapshotsMapList = new ArrayList<>();
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_STATEMENT_PROCESSED)) {
//            boolean areThereResults = stmt.execute(SELECT_STATEMENT_PROCESSED);


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
                    unprocessedGeekSnapshotsMapList.add(row);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return unprocessedGeekSnapshotsMapList;
    }

    public int currentUnprocessedRowCountOnLastPage() {

        int currentUnprocessedRowCountOnLastPage =  db.getInt(-1," SELECT COUNT(*) FROM geekSnapshot "
                + " WHERE total_page_num = current_page "
                + " AND DATE(audit_time) = DATE(CURRENT_TIMESTAMP) AND processed = 'no';");

        trace("currentUnprocessedRowCountOnLastPage [%d]",currentUnprocessedRowCountOnLastPage);
        return currentUnprocessedRowCountOnLastPage;

    }

    public String getAuditTime() {
        String auditTime =  db.getString("",
                "SELECT DISTINCT(audit_time) FROM geekSnapshot WHERE processed = 'no';"
        );
        return auditTime;
    }

    public int getDistinctAuditTimeCount() {
        int distinctAuditTimeCount =  db.getInt(-1,
                "SELECT COUNT(DISTINCT(audit_time)) FROM geekSnapshot WHERE processed = 'no';"
        );
        return distinctAuditTimeCount;
    }

    public boolean isNewSnapshotDataAvailable() {
        int noOfNewRows = db.getInt(-1,"SELECT COUNT(*) FROM geekSnapshot WHERE stamp > CURRENT_DATE();");
        return noOfNewRows > 0;
    }
}
