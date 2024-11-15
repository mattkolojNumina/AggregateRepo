package host;

import rds.RDSDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HostLog {

    private final RDSDatabase _db;
    private static final String INSERT_STMT = "INSERT INTO rds.hostLog SET messageType = (?), refType = (?), refValue = (?), isError = (?), errorMessage = (?);";

    public HostLog(RDSDatabase db) {
        _db = db;
    }

    public RDSDatabase getDb() {
        return _db;
    }

    public void add(String msg, String msgType, String refType, String refValue, boolean isError) throws SQLException {
        try (
                Connection connection = getDb().connect();
                PreparedStatement stmt = connection.prepareStatement(INSERT_STMT)) {

            stmt.setString(1, msgType);
            stmt.setString(2, refType);
            stmt.setString(3, refValue);
            stmt.setBoolean(4, isError);
            stmt.setString(5, msg);
            stmt.execute();
        }
    }
}
