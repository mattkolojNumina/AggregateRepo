package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import host.ConnectionPool;
import records.RdsGeekInventoryAdjustments;

public class RdsGeekInventoryAdjustmentsDAO implements DAO<RdsGeekInventoryAdjustments>{

    private static final String INSERT_STATEMENT = 
            "INSERT INTO `rds`.`rdsGeekInventoryAdjustments` " + 
            "(`transaction_type`, " +
            "`putaway_type`,"+
            "`putaway_replen_code`, " +
            "`putaway_code`, " + 
            "`part_number`, " + 
            "`quantity_change`, " + 
            "`operator_id`," +
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
            "(?), " +
            "(?)" +
            ");" ;

    @Override
    public Optional<RdsGeekInventoryAdjustments> get(int id) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public List<RdsGeekInventoryAdjustments> getAll() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }


    @Override
    public int save(RdsGeekInventoryAdjustments t) throws DataAccessException {
       try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(
                       INSERT_STATEMENT);) {
            // Insert parameters
            stmt.setString(1, t.getTransaction_type());
            stmt.setString(2, t.getPutaway_type());

            stmt.setObject(3, t.getPutaway_replen_code());
            stmt.setObject(4, t.getPutaway_code());

            stmt.setString(5, t.getPart_number());
            stmt.setInt(6, t.getQuantity_change());
            stmt.setString(7, t.getOperator_id());
            stmt.setString(8, "no");

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(RdsGeekInventoryAdjustments t, String[] params) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(RdsGeekInventoryAdjustments t) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
