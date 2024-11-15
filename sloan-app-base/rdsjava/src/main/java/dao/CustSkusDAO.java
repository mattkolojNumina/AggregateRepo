package dao;

import host.ConnectionPool;
import records.CustSku;

import java.sql.*;
import java.util.*;

import static rds.RDSLog.*;

public class CustSkusDAO implements DAO<CustSku> {

    private static final String SELECT_STATEMENT = "SELECT sku, uom, description, barcode, weight, height, length, width, " +
            "qtyShelfPack, upc2, upc3, cubicDivisorint, geekToteCode, geekToteDesiredQty, geekPrimaryLocCapacity, " +
            "buyingDepartment, qroFlag, gtin8, gtin13 FROM rds.custSkus;";

    private static final String SELECT_STATEMENT_TO_BE_SENT_TO_GEEK = "SELECT sku, uom, description, barcode, weight, height, length, width, " +
            "qtyShelfPack, upc2, upc3, cubicDivisorint, geekToteCode, geekToteDesiredQty, geekPrimaryLocCapacity, " +
            "buyingDepartment, qroFlag, gtin8, gtin13 FROM rds.custSkus WHERE sentToGeek = 0;";

    private static final String INSERT_UPDATE_STATEMENT = "INSERT INTO rds.custSkus "
            + " SET `sku` = (?), "
            + " `uom` = (?),  "
            + " `description` = (?),  "
            + " `barcode` = (?),  "
            + " `weight` = (?),  "
            + " `height` = (?),  "
            + " `length` = (?),  "
            + " `width` = (?),  "
            + " `qtyShelfPack` = (?),  "
            + " `upc2` = (?),  "
            + " `upc3` = (?),  "
            + " `cubicDivisorint` = (?),  "
            + " `geekToteCode` = (?),  "
            + " `geekToteDesiredQty` = (?),  "
            + " `geekPrimaryLocCapacity` = (?),  "
            + " `buyingDepartment` = (?), "
            + " `qroFlag` = (?), "
            + " `gtin8` = (?), "
            + " `gtin13` = (?) "
            + " ON DUPLICATE KEY UPDATE  "
            + " `description` = (?),  "
            + " `barcode` = (?),  "
            + " `weight` = (?),  "
            + " `height` = (?),  "
            + " `length` = (?),  "
            + " `width` = (?),  "
            + " `qtyShelfPack` = (?),  "
            + " `upc2` = (?),  "
            + " `upc3` = (?),  "
            + " `cubicDivisorint` = (?),  "
            + " `geekToteCode` = (?),  "
            + " `geekToteDesiredQty` = (?),  "
            + " `geekPrimaryLocCapacity` = (?),  "
            + " `buyingDepartment` = (?), "
            + " `qroFlag` = (?), "
            + " `gtin8` = (?), "
            + " `gtin13` = (?) ";


    private static final String SELECT_STATEMENT_BY_SKU = "SELECT sku, uom, description, barcode, weight, height, length, width, " +
            "qtyShelfPack, upc2, upc3, cubicDivisorint, geekToteCode, geekToteDesiredQty, geekPrimaryLocCapacity, " +
            "buyingDepartment, qroFlag, gtin8, gtin13 FROM rds.custSkus WHERE sku = (?) and uom = (?);";

    private static final String UPDATE_SET_ACK_BY_GEEK =
            "UPDATE rds.custSkus SET ackByGeek = (?) WHERE sku = (?) and uom = (?);";

    private static final String UPDATE_SET_SENT_TO_GEEK =
            "UPDATE rds.custSkus SET sentToGeek = (?) WHERE sku = (?) and uom = (?);";

    @Override
    public Optional<CustSku> get(int id) throws DataAccessException {
        return Optional.empty();
    }

    @Override
    public List<CustSku> getAll() throws DataAccessException {
        List<CustSku> skus = new ArrayList<>();

        try(Connection connection = ConnectionPool.getConnection();
            PreparedStatement stmt = connection.prepareStatement(SELECT_STATEMENT)) {
            skus = returnResults(stmt);
        } catch (Exception e) {
            trace(e.toString());
        }
        return skus;
    }

    public List<CustSku> getAllToBeSentToGeek() throws DataAccessException {
        List<CustSku> skus = new ArrayList<>();

        try(Connection connection = ConnectionPool.getConnection();
            PreparedStatement stmt = connection.prepareStatement(SELECT_STATEMENT_TO_BE_SENT_TO_GEEK)) {
              skus =  returnResults(stmt);
        } catch (Exception e) {
            trace(e.toString());
        }
        return skus;
    }

    public CustSku getBySku(String sku, String uom) throws DataAccessException {
        
        try(Connection connection = ConnectionPool.getConnection();
              PreparedStatement stmt = connection.prepareStatement(SELECT_STATEMENT_BY_SKU)) {

                stmt.setString(1, sku);
                stmt.setString(2, uom);
                boolean areThereResults = stmt.execute();
                if (areThereResults) {
                    ResultSet rs = stmt.getResultSet();
                    rs.next();
                    CustSku custSkuObj = new CustSku.CustSkuBuilder(
                    rs.getString(1),
                    rs.getString(2))
                            .setDescription(rs.getString(3))
                            .setBarcode(rs.getString(4))
                            .setWeight(rs.getFloat(5))
                            .setHeight(rs.getFloat(6))
                            .setLength(rs.getFloat(7))
                            .setWidth(rs.getFloat(8))
                            .setQuantityShelfPack(rs.getInt(9))
                            .setUpc2(rs.getString(10))
                            .setUpc3(rs.getString(11))
                            .setCubicDivisor(rs.getInt(12))
                            .setGeekToteCode(rs.getString(13))
                            .setGeekToteDesiredQuantity(rs.getInt(14))
                            .setGeekPrimaryLocationCapacity(rs.getInt(15))
                            .setBuyingDepartmentCode(rs.getString(16))
                            .setQroFlag(rs.getString(17))
                            .setGtin8(rs.getString(18))
                            .setGtin13(rs.getString(19))
                            .build();

                    return custSkuObj;
                }else{
                    trace("no results");
                }
            
        } catch (Exception e) {
            trace(e.toString());
        }
        return null;
    }

    private List<CustSku> returnResults(PreparedStatement stmt) throws DataAccessException, SQLException{

        List<CustSku> skus = new ArrayList<>();

        boolean areThereResults = stmt.execute();
        if (areThereResults) {
            try (ResultSet rs = stmt.getResultSet()) {
                while(rs.next()) {
                   CustSku sku = new CustSku.CustSkuBuilder(
                        rs.getString(1),
                        rs.getString(2))
                            .setDescription(rs.getString(3))
                            .setBarcode(rs.getString(4))
                            .setWeight(rs.getFloat(5))
                            .setHeight(rs.getFloat(6))
                            .setLength(rs.getFloat(7))
                            .setWidth(rs.getFloat(8))
                            .setQuantityShelfPack(rs.getInt(9))
                            .setUpc2(rs.getString(10))
                            .setUpc3(rs.getString(11))
                            .setCubicDivisor(rs.getInt(12))
                            .setGeekToteCode(rs.getString(13))
                            .setGeekToteDesiredQuantity(rs.getInt(14))
                            .setGeekPrimaryLocationCapacity(rs.getInt(15))
                            .setBuyingDepartmentCode(rs.getString(16))
                            .setQroFlag(rs.getString(17))
                            .setGtin8(rs.getString(18))
                            .setGtin13(rs.getString(19))
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
    public int save(CustSku custSku) throws DataAccessException {
        try ( Connection connection = ConnectionPool.getConnection();
              PreparedStatement stmt = connection.prepareStatement(INSERT_UPDATE_STATEMENT)) {
            stmt.setString(1, custSku.getSku());
            stmt.setString(2, custSku.getUom());

            stmt.setString(3, custSku.getDescription());
            stmt.setString(4, custSku.getBarcode());
            stmt.setFloat(5, custSku.getWeight());
            stmt.setFloat(6, custSku.getHeight());
            stmt.setFloat(7, custSku.getLength());
            stmt.setFloat(8, custSku.getWidth());
            stmt.setInt(9, custSku.getQuantityShelfPack());
            stmt.setString(10, custSku.getUpc2());
            stmt.setString(11, custSku.getUpc3());
            stmt.setInt(12, custSku.getCubicDivisorInt());
            stmt.setString(13, custSku.getGeekToteCode());
            stmt.setInt(14, custSku.getGeekToteDesiredQuantity());
            stmt.setInt(15, custSku.getGeekPrimaryLocationCapacity());
            stmt.setString(16, custSku.getBuyingDepartmentCode());
            stmt.setString(17, custSku.getQroFlag());
            stmt.setString(18, custSku.getGtin8());
            stmt.setString(19, custSku.getGtin13());

            stmt.setString(20, custSku.getDescription());
            stmt.setString(21, custSku.getBarcode());
            stmt.setFloat(22, custSku.getWeight());
            stmt.setFloat(23, custSku.getHeight());
            stmt.setFloat(24, custSku.getLength());
            stmt.setFloat(25, custSku.getWidth());
            stmt.setInt(26, custSku.getQuantityShelfPack());
            stmt.setString(27, custSku.getUpc2());
            stmt.setString(28, custSku.getUpc3());
            stmt.setInt(29, custSku.getCubicDivisorInt());
            stmt.setString(30, custSku.getGeekToteCode());
            stmt.setInt(31, custSku.getGeekToteDesiredQuantity());
            stmt.setInt(32, custSku.getGeekPrimaryLocationCapacity());
            stmt.setString(33, custSku.getBuyingDepartmentCode());
            stmt.setString(34, custSku.getQroFlag());
            stmt.setString(35, custSku.getGtin8());
            stmt.setString(36, custSku.getGtin13());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustSku custSku, String[] params) throws DataAccessException {

    }

    public void updateAckByGeek(String sku, String uom, int ackByGeekValue) throws DataAccessException{
//        System.out.println("sku: " + sku);
//        System.out.println("uom: " + uom);
        try ( Connection connection = ConnectionPool.getConnection();
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SET_ACK_BY_GEEK)) {
                stmt.setInt(1, ackByGeekValue);
                stmt.setString(2, sku);
                stmt.setString(3, uom);
                stmt.execute();
            } catch (SQLException e) {
                alert(e.toString());
                throw new DataAccessException(e.toString());
            }

    }

    public void updateSentToGeek(String sku, String uom, int sentToGeekValue) throws DataAccessException{

        try ( Connection connection = ConnectionPool.getConnection();
              PreparedStatement stmt = connection.prepareStatement(UPDATE_SET_SENT_TO_GEEK)) {
            stmt.setInt(1, sentToGeekValue);
            stmt.setString(2, sku);
            stmt.setString(3, uom);
            stmt.execute();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }

    }

    @Override
    public void delete(CustSku custSku) throws DataAccessException {

    }

    public List<Map<String,String>> getAcknowledgedSkus() {
        List<Map<String,String>> acknowledgedSkusList = new ArrayList<>();

        String SELECT_ACKNOWLEDGED_SKUS =
                " SELECT sku_code, unit FROM geekSku gs"
                        +" JOIN custSkus cs "
                        +" ON gs.sku_code = cs.sku "
                        +" AND cs.uom = gs.unit "
                        +" WHERE gs.`processed` = 'yes' "
                        +" AND cs.ackByGeek = 0;";

        try(Connection cxn = ConnectionPool.getConnection();
            Statement stmt = cxn.createStatement()) {
            boolean areThereResults = stmt.execute(SELECT_ACKNOWLEDGED_SKUS);
            if (areThereResults) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    Map<String,String> acknowledgedSkusMap = new HashMap<>();
                    acknowledgedSkusMap.put(rs.getString("sku_code"),rs.getString("unit"));
                    acknowledgedSkusList.add(acknowledgedSkusMap);
                }
            }
        } catch (Exception e) {
            trace(e.toString());
        }
        return acknowledgedSkusList;
    }
}
