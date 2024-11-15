package dao;

import host.ConnectionPool;
import records.CustBuyingDepartments;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class CustBuyingDepartmentsDAO implements DAO<CustBuyingDepartments> {

    private static final String SELECT_STATEMENT = "SELECT departmentName, departmentDesc from rds.custBuyingDepartments";
    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.custBuyingDepartments SET departmentName = (?), departmentDesc = (?) ON DUPLICATE KEY UPDATE departmentDesc = (?);";

    @Override
    public Optional<CustBuyingDepartments> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustBuyingDepartments> getAll() {
        List<CustBuyingDepartments> buyingDepartmentsList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement  stmt = cxn.prepareStatement(SELECT_STATEMENT)) {
            boolean areThereResults = stmt.execute();
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    CustBuyingDepartments bd = new CustBuyingDepartments.CustBuyingDepartmentsBuilder(
                            rs.getString(1),
                            rs.getString(2)
                    ).build();
                    buyingDepartmentsList.add(bd);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return buyingDepartmentsList;
    }

    @Override
    public int save(CustBuyingDepartments custBuyingDepartments) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {

            stmt.setString(1, custBuyingDepartments.getDepartmentName());
            stmt.setString(2, custBuyingDepartments.getDepartmentDesc());
            stmt.setString(3, custBuyingDepartments.getDepartmentDesc());

            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustBuyingDepartments custBuyingDepartments, String[] params) {

    }

    @Override
    public void delete(CustBuyingDepartments custBuyingDepartments) {

    }
}
