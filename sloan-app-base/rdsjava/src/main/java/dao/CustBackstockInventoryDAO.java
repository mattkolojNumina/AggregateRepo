package dao;

import host.ConnectionPool;
import records.CustBackstockInventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dao.AbstractDAO.db;
import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class CustBackstockInventoryDAO implements DAO<CustBackstockInventory> {

    private static final String SELECT_STATEMENT =
            "SELECT `location`,`sku`,`qty` from rds.custBackstockInventory";
    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.custBackstockInventory SET location = (?), sku = (?), qty = (?), downloadStamp = now() " +
                    "ON DUPLICATE KEY UPDATE qty = (?), downloadStamp = now() ";

    @Override
    public Optional<CustBackstockInventory> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustBackstockInventory> getAll() {
        List<CustBackstockInventory> custBackstockInventoryList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_STATEMENT);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    CustBackstockInventory backstockInventory = new CustBackstockInventory.CustBackstockInventoryBuilder(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3)
                    ).build();
                    custBackstockInventoryList.add(backstockInventory);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return custBackstockInventoryList;
    }

    @Override
    public int save(CustBackstockInventory custBackstockInventory) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            stmt.setString(1, custBackstockInventory.getLocation());
            stmt.setString(2, custBackstockInventory.getSku());
            stmt.setLong(3, custBackstockInventory.getQty());
            stmt.setLong(4, custBackstockInventory.getQty());
            return stmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustBackstockInventory custBackstockInventory, String[] params) {

    }

    @Override
    public void delete(CustBackstockInventory custBackstockInventory) {

    }

    public int  getNoOfOldRecords() {
        return db.getInt(-1,"SELECT COUNT(*) FROM `custBackstockInventory` WHERE DATE(downloadStamp) <> DATE(NOW());");
    }

    public void copyOverAndTruncate(){
        db.execute("INSERT INTO `custBackstockInventory_backup` SELECT * FROM custBackstockInventory;");
        db.execute("TRUNCATE TABLE custBackstockInventory;");
    }

}
