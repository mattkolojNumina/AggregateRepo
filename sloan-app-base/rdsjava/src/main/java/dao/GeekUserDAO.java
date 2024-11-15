package dao;

import host.ConnectionPool;
import records.GeekUser;
import records.GeekUserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeekUserDAO implements DAO<GeekUser> {

    private static final String SELECT_STATEMENT =
            "SELECT warehouse_code, user_name, real_name, password, status from rds.geekUser";

    private final String SELECT_USERS_TO_ACKNOWLEDGE =
            " SELECT user_name FROM geekUser gu " +
                    " JOIN proOperators po " +
                    " ON gu.user_name = po.operatorID " +
                    " WHERE gu.`processed` = 'yes' " +
                    " AND po.ackByGeek = 0 ";

    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.geekUser SET warehouse_code = (?), user_name = (?), real_name = (?), password = (?), " +
                    "status = (?), processed = 'wait', stamp = current_timestamp ON DUPLICATE KEY UPDATE " +
                    "real_name = (?), password = (?), status = (?), processed = 'wait', stamp = current_timestamp;";

    private static final String SET_PROCESSED_TO_NO =
            "UPDATE rds.geekUser SET processed = 'no' WHERE processed = 'wait';";


    @Override
    public Optional<GeekUser> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<GeekUser> getAll() {
        return new ArrayList<>();
    }

    @Override
    public int save(GeekUser geekUser) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            // Insert parameters
            stmt.setString(1, geekUser.getWarehouseCode());
            stmt.setString(2, geekUser.getUserName());

            stmt.setString(3, geekUser.getRealName());
            stmt.setString(4, geekUser.getPassword());
            stmt.setString(5, geekUser.getStatus());
            // Update parameters
            stmt.setString(6, geekUser.getRealName());
            stmt.setString(7, geekUser.getPassword());
            stmt.setString(8, geekUser.getStatus());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(GeekUser geekUser, String[] params) {

    }

    @Override
    public void delete(GeekUser geekUser) {

    }

    public void setProcessedFlag() throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            stmt.execute(SET_PROCESSED_TO_NO);
        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }
}

