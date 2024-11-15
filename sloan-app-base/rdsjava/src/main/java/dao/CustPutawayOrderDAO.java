package dao;

import host.ConnectionPool;
import records.CustPutawayOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class CustPutawayOrderDAO extends AbstractDAO implements DAO<CustPutawayOrder> {

    private static final String SELECT_STATEMENT = "SELECT putawayOrderCode, palletCode, putawayType, sku, uom, " +
            "qty, shelfQty FROM rds.custPutawayOrders WHERE status = 'downloaded';";

    private static final String SELECT_STATEMENT_All_UNSENT = "SELECT putawayOrderCode, palletCode, putawayType, sku, uom, " +
            "qty, shelfQty FROM rds.custPutawayOrders WHERE sentToGeek = 0 AND status <> 'error' AND status <> 'canceled';";

    private static final String SELECT_TYPE_STATEMENT_BY_KEY = "SELECT putawayType " +
            " FROM rds.custPutawayOrders WHERE putawayOrderCode = (?);";
    private static final String INSERT_UPDATE_STATEMENT = "INSERT INTO rds.custPutawayOrders SET putawayOrderCode = (?), " +
            "palletCode = (?), putawayType = (?), sku = (?), uom = (?), qty = (?), shelfQty = (?), status = 'downloaded'," +
            "downloadStamp = current_timestamp ON DUPLICATE KEY UPDATE palletCode = (?), " +
            "putawayType = (?), sku = (?), uom = (?), qty = (?), shelfQty = (?), status = 'downloaded', " +
            "downloadStamp = current_timestamp";

    private static final String UPDATE_SENT_TO_GEEK_STATEMENT = "UPDATE rds.custPutawayOrders SET sentToGeek = 1 WHERE putawayOrderCode = (?)";

    @Override
    public Optional<CustPutawayOrder> get(int id) throws DataAccessException {
        return Optional.empty();
    }


    public String getPutawayTypeByKey(String putawayOrderCode) throws DataAccessException {
        String putawayType = "";
        try (Connection cxn = ConnectionPool.getConnection();
                PreparedStatement stmt = cxn.prepareStatement(SELECT_TYPE_STATEMENT_BY_KEY)) {
                    stmt.setString(1, putawayOrderCode);
            boolean areThereResults = stmt.execute();
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                if( rs.next() )
                   putawayType = rs.getString(1);
                else
               	 putawayType = "-1";
            }
            
        } catch (Exception e) {
            trace(e.toString());
        }
        return putawayType;
    }

    @Override
    public List<CustPutawayOrder> getAll() throws DataAccessException {
        List<CustPutawayOrder> orderList = new ArrayList<>();

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(SELECT_STATEMENT_All_UNSENT)) {
            boolean areThereResults = stmt.execute();
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    CustPutawayOrder order = new CustPutawayOrder.CustPutawayOrderBuilder(rs.getString(1))
                            .setPalletCode(rs.getString(2))
                            .setPutawayType(rs.getInt(3))
                            .setSku(rs.getString(4))
                            .setUom(rs.getString(5))
                            .setBuyingDepartmentCode("")
                            .setQuantity(rs.getInt(6))
                            .setShelfQuantity(rs.getInt(7))
                    .build();
                    orderList.add(order);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return orderList;
    }

    @Override
    public int save(CustPutawayOrder custPutawayOrder) throws DataAccessException {

        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            // inserts
            stmt.setString(1, custPutawayOrder.getPutawayOrderCode());

            stmt.setString(2, custPutawayOrder.getPalletCode());
            stmt.setInt(3, custPutawayOrder.getPutawayType());
            stmt.setString(4, custPutawayOrder.getSku());
            stmt.setString(5, custPutawayOrder.getUom());
            stmt.setInt(6, custPutawayOrder.getQuantity());
            stmt.setInt(7, custPutawayOrder.getShelfQuantity());
            // updates
            stmt.setString(8, custPutawayOrder.getPalletCode());
            stmt.setInt(9, custPutawayOrder.getPutawayType());
            stmt.setString(10, custPutawayOrder.getSku());
            stmt.setString(11, custPutawayOrder.getUom());
            stmt.setInt(12, custPutawayOrder.getQuantity());
            stmt.setInt(13, custPutawayOrder.getShelfQuantity());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustPutawayOrder custPutawayOrder, String[] params) throws DataAccessException {

    }

    @Override
    public void delete(CustPutawayOrder custPutawayOrder) throws DataAccessException {

    }

    public void setSentToGeek(String putawayOrderCode) throws DataAccessException {

        try (Connection cxn = ConnectionPool.getConnection();
             PreparedStatement stmt = cxn.prepareStatement(UPDATE_SENT_TO_GEEK_STATEMENT)) {
            stmt.setString(1, putawayOrderCode);

            stmt.execute();
        } catch (SQLException | NumberFormatException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }

    }

    private static final String SELECT_ACKNOWLEDGED_CARTONS =
            " SELECT receipt_code FROM geekPutawayOrder gpo " +
                    " JOIN custPutawayOrders cpo " +
                    " ON gpo.receipt_code = cpo.putawayOrderCode " +
                    " WHERE gpo.`processed` = 'yes' " +
                    " AND cpo.ackByGeek = 0 ";

    public List<String> getAcknowledgedOrders() {
        return db.getValueList(SELECT_ACKNOWLEDGED_CARTONS);
    }
}
