package dao;

import records.RdsLocation;
import host.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.*;

public class RdsLocationDAO implements DAO<RdsLocation> {

    private static final String SELECT_STATEMENT = 
   		 "SELECT location,alias,barcode,`area`," +
   		 "aisle,bay,`row`,`column`,`neighborhood`,checkDigits from rds.rdsLocations";
    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.rdsLocations SET location = (?), alias = (?), barcode = (?), checkDigits = (?), " +
            "`area` = (?), aisle = (?), bay = (?), `row` = (?), `column` = (?), `neighborhood` = (?), downloadStamp = now() " +
            "ON DUPLICATE KEY UPDATE alias = (?), barcode = (?), checkDigits = (?), " +
            "`area` = (?), aisle = (?), bay = (?), `row` = (?), `column` = (?), `neighborhood` = (?), downloadStamp = now() ";

    @Override
    public Optional<RdsLocation> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<RdsLocation> getAll() {
        List<RdsLocation> locationList = new ArrayList<>();
        try(Connection cxn = ConnectionPool.getConnection();
      		Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_STATEMENT);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    RdsLocation loc = new RdsLocation(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8),
                            rs.getString(9),
                            rs.getString(10)
                    );
                    locationList.add(loc);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return locationList;
    }

    @Override
    public int save(RdsLocation location) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            stmt.setString(1, location.location());
            stmt.setString(2, location.alias());
            stmt.setString(3, location.barcode());
            stmt.setString(4, location.checkDigits());
            stmt.setString(5, location.area());
            stmt.setString(6, location.aisle());
            stmt.setString(7, location.bay());
            stmt.setString(8, location.row());
            stmt.setString(9, location.column());
            stmt.setString(10, location.neighborhood());
            stmt.setString(11, location.alias());
            stmt.setString(12, location.barcode());
            stmt.setString(13, location.checkDigits());
            stmt.setString(14, location.area());
            stmt.setString(15, location.aisle());
            stmt.setString(16, location.bay());
            stmt.setString(17, location.row());
            stmt.setString(18, location.column());
            stmt.setString(19, location.neighborhood());
            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(RdsLocation proOperator, String[] params) {

    }

    @Override
    public void delete(RdsLocation proOperator) {

    }
}
