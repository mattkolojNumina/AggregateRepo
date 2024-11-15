package dao;

import host.ConnectionPool;
import records.GeekPickOrder;
import records.GeekPutawayOrder;
import records.GeekPutawayOrderSku;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static sloane.SloaneConstants.geekOwnerCode;
import static sloane.SloaneConstants.geekWarehouseCode;

public class GeekPickOrderDAO implements DAO<GeekPickOrder> {

    private String CREATE_PICK_ORDER =
            "INSERT INTO geekPickOrder SET "
                    + " warehouse_code = (?), "
                    + " out_order_code = (?), "
                    + " owner_code = (?), "
                    + " order_type = (?), "
                    + " is_allow_split = (?), "
                    + " is_allow_lack = (?), "
                    + " creation_date = NOW()";

    private String CREATE_PICK_ORDER_SKU =
            "INSERT INTO geekPickOrderSku SET "
            + " parent = (?), "
            + " sku_code = (?), "
            + " owner_code = (?), "
            + " amount = (?), "
            + " sku_level = (?) ";


    /**
     * @param id
     * @return
     * @throws DataAccessException
     */
    @Override
    public Optional get(int id) throws DataAccessException {
        return Optional.empty();
    }

    /**
     * @return
     * @throws DataAccessException
     */
    @Override
    public List getAll() throws DataAccessException {
        return null;
    }

    /**
     * @param geekPickOrder
     * @return
     * @throws DataAccessException
     */
    @Override
    public int save(GeekPickOrder geekPickOrder) throws DataAccessException {

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(CREATE_PICK_ORDER, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement childStmt = cxn.prepareStatement(CREATE_PICK_ORDER_SKU)) {

            stmt.setString(1, geekWarehouseCode);
            stmt.setString(2, geekPickOrder.getOutOrderCode());
            stmt.setString(3, geekOwnerCode);
            stmt.setLong(4, geekPickOrder.getOrderType());
            stmt.setLong(5, geekPickOrder.getIsAllowSplit());
            stmt.setLong(6, geekPickOrder.getIsAllowLack());

            int result = stmt.executeUpdate();

            ResultSet autoGenKeys = stmt.getGeneratedKeys();
            autoGenKeys.next();
            int pickingOrderSeq = autoGenKeys.getInt(1);

            for (GeekPutawayOrderSku sku: geekPickOrder.getSkuList()) {
                childStmt.setInt(1, pickingOrderSeq);
                childStmt.setString(2, sku.skuCode());
                childStmt.setString(3, geekOwnerCode);
                childStmt.setInt(4, sku.amount());
                childStmt.setInt(5, sku.skuLevel());
                childStmt.execute();
            }
            return result;

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    /**
     * @param geekPickOrder
     * @param params
     * @throws DataAccessException
     */
    @Override
    public void update(GeekPickOrder geekPickOrder, String[] params) throws DataAccessException {

    }

    /**
     * @param geekPickOrder
     * @throws DataAccessException
     */
    @Override
    public void delete(GeekPickOrder geekPickOrder) throws DataAccessException {

    }


}
