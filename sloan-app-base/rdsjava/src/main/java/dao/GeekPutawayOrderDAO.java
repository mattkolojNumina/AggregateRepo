package dao;

import host.ConnectionPool;
import records.GeekPutawayOrder;
import records.GeekPutawayOrderSku;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeekPutawayOrderDAO implements DAO<GeekPutawayOrder> {
    // TODO determine if we need to delete data before inserting
    private static final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.geekPutawayOrder SET warehouse_code = (?), receipt_code = (?), pallet_code = (?), " +
                    "type = (?), processed = 'wait', stamp = current_timestamp;";
    //SR: 02/02 go live change
    private static final String INSERT_UPDATE_STATEMENT_v2 =
            "INSERT INTO rds.geekPutawayOrder SET warehouse_code = (?), receipt_code = (?), " +
                    "type = (?), processed = 'wait', stamp = current_timestamp;";

    private static final String INSERT_CHILD_STATEMENT =
            "INSERT INTO rds.geekPutawayOrderSku SET parent = (?), sku_code = (?), owner_code = (?), amount = (?), " +
                    "sku_level = (?), stamp = current_timestamp;";
    private static final String SET_PROCESSED_TO_NO =
            "UPDATE rds.geekPutawayOrder SET processed = 'no' WHERE processed = 'wait';";

    @Override
    public Optional<GeekPutawayOrder> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<GeekPutawayOrder> getAll() {
        return new ArrayList<>();
    }

    @Override
    public int save(GeekPutawayOrder geekPutawayOrder) throws DataAccessException {

        try(Connection cxn = ConnectionPool.getConnection();
            //SR: 02/02 go live change
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT_v2, Statement.RETURN_GENERATED_KEYS);//SR: 02/02 go-live change
            PreparedStatement childStmt = cxn.prepareStatement(INSERT_CHILD_STATEMENT)) {
            // Insert parameters
            stmt.setString(1, geekPutawayOrder.warehouseCode());
            stmt.setString(2, geekPutawayOrder.receiptCode());
//            stmt.setString(3, geekPutawayOrder.palletCode()); //SR: 02/02 go-live change
            stmt.setInt(3, geekPutawayOrder.type());

            int result = stmt.executeUpdate();

            ResultSet autoGenKeys = stmt.getGeneratedKeys();
            autoGenKeys.next();
            int putawayOrderId = autoGenKeys.getInt(1);

            for (GeekPutawayOrderSku sku: geekPutawayOrder.skus()) {
                childStmt.setInt(1, putawayOrderId);
                childStmt.setString(2, sku.skuCode());
                childStmt.setString(3, sku.ownerCode());
                childStmt.setInt(4, sku.amount());
                childStmt.setInt(5, sku.skuLevel());
                childStmt.execute();
            }
            return result;

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(GeekPutawayOrder GeekPutawayOrder, String[] params) {

    }

    @Override
    public void delete(GeekPutawayOrder GeekPutawayOrder) {

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

