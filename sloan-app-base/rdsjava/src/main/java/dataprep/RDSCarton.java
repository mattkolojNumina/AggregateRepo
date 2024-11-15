package dataprep;

import dao.DataAccessException;
import host.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class RDSCarton {

    String lpn;
    String type;
    String ucc = "TESTDATA";
    boolean auditRequired;
    boolean repackRequired;
    boolean packException;
    Instant pickStamp = null;
    Instant shortStamp = null;
    Instant packStamp = null;
    Instant cancelStamp = null;

    private static final String INSERT_STATEMENT = "INSERT INTO rds.rdsCartons "
            + " SET lpn = (?), "
            + " ucc = (?),"
            + " cartonType = (?), "
            + " auditRequired = (?), "
            + " repackRequired = (?), "
            + " packException = (?), "
            + " createStamp = CURRENT_TIMESTAMP, "
            + " pickStamp = (?), "
            + " pickShortStamp = (?), "
            + " auditStamp = (?), "
            + " packStamp = (?), "
            + " cancelStamp = (?)";

    public void save() throws DataAccessException {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_STATEMENT)) {

            stmt.setString(1, lpn);
            stmt.setString(2, ucc);
            stmt.setString(3, type);
            stmt.setInt(4, auditRequired ? 1 : 0);
            stmt.setInt(5, repackRequired ? 1 : 0);
            stmt.setInt(6, packException ? 1 : 0);
            stmt.setTimestamp(7, getStamp(pickStamp));
            stmt.setTimestamp(8, getStamp(shortStamp));
            stmt.setTimestamp(9, null);
            stmt.setTimestamp(10, getStamp(packStamp));
            stmt.setTimestamp(11, getStamp(cancelStamp));

            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new DataAccessException(e.toString());
        }
    }

    public Timestamp getStamp(Instant instant) {
        if (instant == null) {
            return null;
        } else {
            return Timestamp.from(instant);
        }
    }
}