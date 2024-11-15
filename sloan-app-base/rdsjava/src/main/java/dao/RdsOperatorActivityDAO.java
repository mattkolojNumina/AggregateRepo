package dao;

import host.ConnectionPool;
import records.RdsGeekInventoryAdjustments;
import records.RdsOperatorActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static rds.RDSUtil.*;

public class RdsOperatorActivityDAO implements DAO<RdsOperatorActivity>{

    private static final String INSERT_STATEMENT = 
            "INSERT INTO `rds`.`rdsOperatorActivity` " +
            "("+
            "`operator_id`, " +
            "`type_code`, " +
            "`operator_task`,"+
            "`device`, " +
            "`startStamp`, " +
            "`endStamp`, " +
            "`processed`" +
            ")" +
            "VALUES" + 
            "(" + 
            "(?), " +
            "(?), " +
            "(?), " +
            "(?), " +
            "(?), " +
            "(?), " +
            "(?)" +
            ");" ;

    @Override
    public Optional<RdsOperatorActivity> get(int id) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public List<RdsOperatorActivity> getAll() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }


    @Override
    public int save(RdsOperatorActivity t) throws DataAccessException {
       try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(
                       INSERT_STATEMENT);) {
            // Insert parameters
            stmt.setObject(1, t.getOperatorId());
            stmt.setString(2, t.getTypeCode());
            stmt.setString(3, t.getOperatorTask());
            stmt.setObject(4, t.getDevice());
            stmt.setObject(5, t.getStartStamp());
            stmt.setString(6, t.getEndStamp());
            stmt.setString(7, "no");

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(RdsOperatorActivity t, String[] params) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(RdsOperatorActivity t) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
