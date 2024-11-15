package dao;

import host.ConnectionPool;
import records.GeekUserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GeekUserRoleDAO implements DAO<GeekUserRole>{

    private static final String REPLACE_STATEMENT =
            "REPLACE INTO rds.geekUserRole SET warehouse_code = (?), user_name = (?), role_name = (?)";


    @Override
    public int save(GeekUserRole geekUserRole) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(REPLACE_STATEMENT)) {
            // Insert parameters
            stmt.setString(1, geekUserRole.getWarehouseCode());
            stmt.setString(2, geekUserRole.getUserName());
            stmt.setString(3, geekUserRole.getRoleName());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    /**
     * @param geekUserRole
     * @param params
     * @throws DataAccessException
     */
    @Override
    public void update(GeekUserRole geekUserRole, String[] params) throws DataAccessException {

    }

    /**
     * @param geekUserRole
     * @throws DataAccessException
     */
    @Override
    public void delete(GeekUserRole geekUserRole) throws DataAccessException {

    }

    /**
     * @param id
     * @return
     * @throws DataAccessException
     */
    @Override
    public Optional<GeekUserRole> get(int id) throws DataAccessException {
        return Optional.empty();
    }

    /**
     * @return
     * @throws DataAccessException
     */
    @Override
    public List<GeekUserRole> getAll() throws DataAccessException {
        return null;
    }


}
