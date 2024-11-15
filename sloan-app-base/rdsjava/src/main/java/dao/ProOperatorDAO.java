package dao;

import host.ConnectionPool;
import records.ProOperator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class ProOperatorDAO implements DAO<ProOperator> {

    private static final String SELECT_STATEMENT = "SELECT operatorID, operatorName, password, status from rds.proOperators";

    private static final String SELECT_UNPROCESSED_STATEMENT = "SELECT operatorID, operatorName, password, status from rds.proOperators WHERE sentToGeek = 0";

    private static final String SELECT_ACKNOWLEDGED_USERS =
            " SELECT user_name FROM geekUser gu" +
            " JOIN proOperators po" +
            " ON gu.user_name = po.operatorID" +
            " WHERE gu.`processed` = 'yes'" +
            " AND po.ackByGeek = 0;";


    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.proOperators SET operatorID = (?), operatorName = (?), password = (?), status = (?) " +
            "ON DUPLICATE KEY UPDATE operatorName = (?), password = (?), status = (?);";

    private static final String UPDATE_SENT_TO_GEEK_STATEMENT = "UPDATE rds.proOperators SET sentToGeek = 1 WHERE operatorID = (?)";
    private static final String UPDATE_ACK_BY_GEEK_STATEMENT = "UPDATE rds.proOperators SET ackByGeek = 1 WHERE operatorID = (?)";



    @Override
    public Optional<ProOperator> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<ProOperator> getAll() {
        List<ProOperator> operatorList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_STATEMENT);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    ProOperator op = new ProOperator.ProOperatorBuilder(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    ).build();
                    operatorList.add(op);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return operatorList;
    }

    public List<ProOperator> getAllUnprocessed() {
        List<ProOperator> operatorList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_UNPROCESSED_STATEMENT);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    ProOperator op = new ProOperator.ProOperatorBuilder(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    ).build();
                    operatorList.add(op);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return operatorList;
    }

    public List<String> getAcknowledgedUsers() {
        List<String> acknowledgedUsersList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_ACKNOWLEDGED_USERS);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    acknowledgedUsersList.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return acknowledgedUsersList;
    }

    @Override
    public int save(ProOperator proOperator) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            stmt.setString(1, proOperator.getOperatorId());

            stmt.setString(2, proOperator.getOperatorName());
            stmt.setString(3, proOperator.getPassword());
            stmt.setInt(4, Integer.parseInt(proOperator.getStatus()));

            stmt.setString(5, proOperator.getOperatorName());
            stmt.setString(6, proOperator.getPassword());
            stmt.setInt(7, Integer.parseInt(proOperator.getStatus()));
            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(ProOperator proOperator, String[] params) {

    }

    public void setSentToGeek(String operatorId) throws DataAccessException {

        try (Connection cxn = ConnectionPool.getConnection();
             PreparedStatement stmt = cxn.prepareStatement(UPDATE_SENT_TO_GEEK_STATEMENT)) {
            stmt.setString(1, operatorId);

            stmt.execute();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    public void setAckByGeek(String operatorId) throws DataAccessException {

        try (Connection cxn = ConnectionPool.getConnection();
             PreparedStatement stmt = cxn.prepareStatement(UPDATE_ACK_BY_GEEK_STATEMENT)) {
             stmt.setString(1, operatorId);
            stmt.execute();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void delete(ProOperator proOperator) {

    }


}
