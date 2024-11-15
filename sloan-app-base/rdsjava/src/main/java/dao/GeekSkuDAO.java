package dao;

import host.ConnectionPool;
import records.CustSku;
import records.GeekSku;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static rds.RDSUtil.*;

public class GeekSkuDAO implements DAO<GeekSku> {

    private final String SELECT_STATEMENT =
            "SELECT warehouse_code, skuCode, ownerCode, skuName, unit, length, width, height, netWeight FROM rds.geekSku";
    
    private final String SELECT_STATEMENT_PROCESSED =
            "SELECT warehouse_code, sku_code, owner_code, sku_name, unit, length, width, height, net_weight FROM rds.geekSku WHERE processed = 'yes'";

    private final String INSERT_UPDATE_STATEMENT =
            "INSERT INTO rds.geekSku SET warehouse_code = (?), sku_code = (?), owner_code = (?), sku_name = (?), unit = (?), length = (?)," +
                    " width = (?), height = (?), net_weight = (?), " +
                    " remark = (?), wares_type_code=(?), specification=(?), item_size=(?), " +
                    " item_color = (?), production_location = (?), item_style = (?), processed = 'wait'" +
                    " ON DUPLICATE KEY UPDATE owner_code = (?), sku_name = (?), unit = (?), length = (?)," +
                    " width = (?), height = (?), net_weight = (?), " +
                    " remark = (?), wares_type_code=(?), specification=(?), item_size=(?), " +
                    " item_color = (?), production_location = (?), item_style = (?), processed = 'wait';";

    private final String INSERT_CHILD_STATEMENT =
            "INSERT INTO rds.geekSkuBarcode SET warehouse_code = (?), sku_code = (?), bar_code = (?)" +
                    " ON DUPLICATE KEY UPDATE warehouse_code = warehouse_code;";

    private final String SET_PROCESSED_TO_NO =
            "UPDATE rds.geekSku SET processed = 'no' WHERE processed = 'wait';";

    private final String SET_PROCESSED_TO_NO_FOR_SKU_UOM =
            "UPDATE rds.geekSku SET processed = 'no' WHERE processed = 'wait' AND sku_code = (?) AND unit = (?);";



    @Override
    public Optional<GeekSku> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<GeekSku> getAll() {
        return new ArrayList<>();
    }

    public List<GeekSku> getAllProcessed() {

        List<GeekSku> skus = new ArrayList<>();

        try(Connection connection = ConnectionPool.getConnection();
            PreparedStatement stmt = connection.prepareStatement(SELECT_STATEMENT_PROCESSED)) {
                skus = returnResults(stmt);
        } catch (Exception e) {
            trace(e.toString());
        }
        return skus;
        
    }

    private List<GeekSku> returnResults(PreparedStatement stmt) throws DataAccessException, SQLException{

        List<GeekSku> skus = new ArrayList<>();
        boolean areThereResults = stmt.execute();
        if (areThereResults) {
            try (ResultSet rs = stmt.getResultSet()) {
                while(rs.next()) {
                   GeekSku sku = new GeekSku.GeekSkuBuilder(
                        rs.getString(1),
                        rs.getString(2))
                            .setOwnerCode(rs.getString(3))
                            .setSkuName(rs.getString(4))
                            .setUnit(rs.getString(5))
                            .setLength(rs.getFloat(6))
                            .setWidth(rs.getFloat(7))
                            .setHeight(rs.getFloat(8))
                            .setNetWeight(rs.getFloat(9))
                            .build();
                    skus.add(sku);
                }
            } catch (SQLException e) {
                alert(e.toString());
                throw new DataAccessException(e.toString());
            }
        }
         return skus;
    }

    @Override
    public int save(GeekSku geekSku) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(INSERT_UPDATE_STATEMENT);
            PreparedStatement childStmt = cxn.prepareStatement(INSERT_CHILD_STATEMENT)) {
            // Insert parameters
            stmt.setString(1, geekSku.getWarehouseCode());
            stmt.setString(2, geekSku.getSkuCode());
            stmt.setString(3, geekSku.getOwnerCode());
            stmt.setString(4, geekSku.getSkuName());
            stmt.setString(5, geekSku.getUnit());
            stmt.setFloat(6, geekSku.getLength());
            stmt.setFloat(7, geekSku.getWidth());
            stmt.setFloat(8, geekSku.getHeight());
            stmt.setFloat(9, geekSku.getNetWeight());
            //SR: new parameters
            stmt.setString(10, geekSku.getRemark()); // description
            stmt.setString(11, geekSku.getWares_type_code()); // buying departments
            stmt.setString(12, geekSku.getSpecification()); //geekToteCode
            stmt.setString(13, geekSku.getItem_size()); //geekToteDesiredQty
            stmt.setString(14, geekSku.getItem_color()); // geekTotePrimaryLocationCapacity
            stmt.setString(15, geekSku.getProduction_location()); // qtyShelfPack
            stmt.setString(16, geekSku.getItem_style()); // qroFlag
            // Update parameters
            stmt.setString(17, geekSku.getOwnerCode());
            stmt.setString(18, geekSku.getSkuName());
            stmt.setString(19, geekSku.getUnit());
            stmt.setFloat(20, geekSku.getLength());
            stmt.setFloat(21, geekSku.getWidth());
            stmt.setFloat(22, geekSku.getHeight());
            stmt.setFloat(23, geekSku.getNetWeight());
            //SR: new parameters
            stmt.setString(24, geekSku.getRemark()); // description
            stmt.setString(25, geekSku.getWares_type_code()); // buying departments
            stmt.setString(26, geekSku.getSpecification()); //geekToteCode
            stmt.setString(27, geekSku.getItem_size()); //geekToteDesiredQty
            stmt.setString(28, geekSku.getItem_color()); // geekTotePrimaryLocationCapacity
            stmt.setString(29, geekSku.getProduction_location()); // qtyShelfPack
            stmt.setString(30, geekSku.getItem_style()); // qroFlag

            int rowsUpdated = stmt.executeUpdate();

            for (String barcode: geekSku.getBarcodes()) {
                childStmt.setString(1, geekSku.getWarehouseCode());
                childStmt.setString(2, geekSku.getSkuCode());
                childStmt.setString(3, barcode);
                childStmt.execute();
            }

            return rowsUpdated;

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(GeekSku geekSku, String[] params) {}

    @Override
    public void delete(GeekSku geekSku) {}

//    public void setProcessedFlag() throws DataAccessException {
//        try(Connection cxn = ConnectionPool.getConnection();
//            Statement stmt = cxn.createStatement()) {
//            stmt.execute(SET_PROCESSED_TO_NO);
//        } catch (SQLException e) {
//            throw new DataAccessException(e.toString());
//        }
//    }


    public int setProcessedFlagForSkuUom(String sku, String uom) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(SET_PROCESSED_TO_NO_FOR_SKU_UOM)) {
            stmt.setString(1,sku);
            stmt.setString(2,uom);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e.toString());
        }
    }
}

