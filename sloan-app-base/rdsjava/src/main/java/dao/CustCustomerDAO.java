package dao;

import records.CustCustomer;
import host.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.*;

public class CustCustomerDAO implements DAO<CustCustomer> {

    private static final String SELECT_STATEMENT = 
   		 "SELECT customerNumber,customerName,addressLine1,addressLine2,addressLine3," +
   		 "city,state,zipcode,exportFlag from rds.custCustomers";
    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.custCustomers SET customerNumber = (?), customerName = (?), addressLine1 = (?), addressLine2 = (?), " +
            "addressLine3 = (?), city = (?), state = (?), zipcode = (?), exportFlag = (?) " +
            "ON DUPLICATE KEY UPDATE customerName = (?), addressLine1 = (?), addressLine2 = (?), " +
            "addressLine3 = (?), city = (?), state = (?), zipcode = (?), exportFlag = (?) ";

    @Override
    public Optional<CustCustomer> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustCustomer> getAll() {
        List<CustCustomer> customerList = new ArrayList<>();
        try(Connection cxn = ConnectionPool.getConnection();
      		Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_STATEMENT);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    CustCustomer customer = new CustCustomer(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8),
                            rs.getInt(9)==1?"Y":"N"
                    );
                    customerList.add(customer);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return customerList;
    }

    @Override
    public int save(CustCustomer customer) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            stmt.setString(1, customer.customerNumber());
            stmt.setString(2, customer.customerName());
            stmt.setString(3, customer.addressLine1());
            stmt.setString(4, customer.addressLine2());
            stmt.setString(5, customer.addressLine3());
            stmt.setString(6, customer.city());
            stmt.setString(7, customer.state());
            stmt.setString(8, customer.zipcode());
            stmt.setInt(9, customer.exportFlag().equals("Y")?1:0);
            stmt.setString(10, customer.customerName());
            stmt.setString(11, customer.addressLine1());
            stmt.setString(12, customer.addressLine2());
            stmt.setString(13, customer.addressLine3());
            stmt.setString(14, customer.city());
            stmt.setString(15, customer.state());
            stmt.setString(16, customer.zipcode());
            stmt.setInt(17, customer.exportFlag().equals("Y")?1:0);
            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustCustomer proOperator, String[] params) {

    }

    @Override
    public void delete(CustCustomer proOperator) {

    }
}
