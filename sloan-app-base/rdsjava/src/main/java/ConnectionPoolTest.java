import host.ConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectionPoolTest {

    public ConnectionPoolTest () {}

    public void run() throws Exception {
        String sql = "SELECT * FROM rds.controls;";

        try (Connection cxn = ConnectionPool.getConnection();
             Statement stmt = cxn.createStatement() ) {

            boolean areThereResults = stmt.execute(sql);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    System.out.println(rs.getString(1) + " - " + rs.getString(2) + " - " + rs.getString(3));
                }
            }
        }
    }

    public static void main (String[] args) throws Exception {
        ConnectionPoolTest cpt = new ConnectionPoolTest();
        cpt.run();
    }
}
