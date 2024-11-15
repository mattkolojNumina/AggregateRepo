package dao;

import host.ConnectionPool;
import records.GeekSku;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class GeekWorkstaionStatusUpdateDAO {

    private static final String SELECT_LOGIN_TIME_FROM_OPERATOR =
            "SELECT happenedAt FROM geekWorkstationStatusUpdate "
            + " WHERE worker = (?) "
            + " AND workstationId = (?) "
            + " AND workstationType = (?) "
            + " AND `status` = (?) "
            + " AND processed = (?) ORDER BY seq desc limit 1";


    public String getLoginTimeForWorker(String worker, String workstationId, String workstationType) throws DataAccessException  {
        String happenedAt = "";
        try (Connection cxn = ConnectionPool.getConnection();
             PreparedStatement stmt = cxn.prepareStatement(SELECT_LOGIN_TIME_FROM_OPERATOR)) {
            stmt.setString(1, worker);
            stmt.setString(2, workstationId);
            stmt.setString(3, workstationType);
            stmt.setString(4, "LOGIN");
            stmt.setString(5, "yes");

            boolean areThereResults = stmt.execute();
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    happenedAt = String.valueOf(rs.getTimestamp(1));
                } else {
//                    alert("no login time found 2");
                }
            } else {
//                alert("no login time found 1");
            }

        } catch (Exception e) {
            trace(e.toString());
        }
        return happenedAt;
    }

}

