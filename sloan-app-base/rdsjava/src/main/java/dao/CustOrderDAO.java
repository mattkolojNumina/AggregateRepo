package dao;

import rds.RDSUtil;
import records.CustOrder;
import host.ConnectionPool;

import java.sql.*;
import java.util.*;

import static rds.RDSLog.*;
import rds.RDSDatabase;

public class CustOrderDAO extends AbstractDAO implements DAO<CustOrder> {
	
   private static final String DELETE_STATEMENT =
         "DELETE FROM rds.custOrders WHERE orderId = (?)";
	
    private static final String INSERT_STATEMENT =
            "INSERT INTO rds.custOrders SET "
            + "orderId = (?), shipmentId = '', orderType = '', customerNumber = (?), truckNumber = (?), door = (?),"
            + "stop = (?), demandDate = (?), poId = (?), poNumbers = (?), "
            + "toteOrBox = (?), dailyWaveSeq = (?), QPAGroup = (?), waveSeq = (?)";

	 private String orderId;
	 
	 public CustOrderDAO() {
	 }
	 
	 public CustOrderDAO( String orderId ) {
		 this.orderId = orderId;
		 recordMap = getTableRowByStringId("custOrders","orderId",orderId);
	 }    
    
    @Override
    public Optional<CustOrder> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustOrder> getAll() {
        return null;
    }

    @Override
    public int save(CustOrder order) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_STATEMENT)) {
            stmt.setString(1, order.getOrderId());
            stmt.setString(2, order.getCustomerNumber());
            stmt.setString(3, order.getTruckNumber());
            stmt.setString(4, order.getDoor());
            stmt.setString(5, order.getStop());
            stmt.setString(6, order.getDemandDate());
            stmt.setString(7, order.getPoId());
            stmt.setString(8, order.getPoNumbers());
            stmt.setString(9, order.getToteOrBox());
            stmt.setString(10, order.getToteCRC());
            stmt.setString(11, order.getQPAGroup());
            stmt.setInt(12, order.getWaveSeq());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustOrder proOperator, String[] params) {

    }

    @Override
    public void delete(CustOrder order) throws DataAccessException {
       try(Connection cxn = ConnectionPool.getConnection();
     		  PreparedStatement stmt = cxn.prepareStatement(DELETE_STATEMENT)) {
           stmt.setString(1, order.getOrderId());
           stmt.execute();
       } catch (SQLException e) {
           alert(e.toString());
           throw new DataAccessException(e.toString());
       }
    }
    
    public boolean exist( String orderId ) throws DataAccessException {
   	 String sql = String.format(
   			 "SELECT COUNT(*) FROM rds.custOrders WHERE orderId='%s' "
   			 + "AND downloadStamp IS NOT NULL AND status NOT IN ('error','canceled')", orderId);
   	 boolean exist = true;
   	 try(Connection cxn = ConnectionPool.getConnection();
   		  Statement stmt = cxn.createStatement()){
   		 boolean areThereResults = stmt.execute(sql);
   		 if( areThereResults ) {
   			 ResultSet rs = stmt.getResultSet();
   	       if (rs.next())
   	      	 exist = rs.getInt( 1 ) == 1;
   		 }
   	 }catch (SQLException e) {
          alert(e.toString());
          throw new DataAccessException(e.toString());
   	 }
   	 return exist;
    }


    
    public void updateField( String orderId, String field, String value ) throws DataAccessException {
   	 String sql = String.format(
   			 "UPDATE rds.custOrders SET %s=(?) WHERE orderId=(?)", field);
       try(Connection cxn = ConnectionPool.getConnection();
       		  PreparedStatement stmt = cxn.prepareStatement(sql)) {
             stmt.setString(1, value);
             stmt.setString(2, orderId);
             stmt.execute();
         } catch (SQLException e) {
             alert(e.toString());
             throw new DataAccessException(e.toString());
         }
    }


    
    public void setTombStone( String orderId, String field ) {
   	 db.execute("UPDATE custOrders SET %s=NOW() WHERE orderId='%s'", field, orderId);
    }
    
    public static void setStatusAndTombStone( String orderId, String status, String field ) {
   	 db.execute("UPDATE custOrders SET `status`='%s', %s=IFNULL(%s,NOW()) WHERE orderId='%s'", status, field, field, orderId);
    }


    public List<Map<String, String>> getTotesPerOrderToUploadDetails(String orderId) {

        List<Map<String, String>> rows = new ArrayList<>();

        String SELECT_ORDER_DETAILS_FOR_ORDER = "SELECT w.waveName, ol.groupNumber, ol.orderId " +
                "FROM rdsCartons c " +
                "JOIN custOrders o " +
                "ON c.orderId = o.orderId " +
                "JOIN rdsPicks p " +
                "ON p.cartonSeq = c.cartonSeq " + // change here 07-09 remove extra join on picks and cartons
                "JOIN custOrderLines ol " +
                "ON ol.orderLineSeq = p.orderLineSeq " +
                "JOIN rdsWaves w " +
                "ON w.waveSeq = o.waveSeq " +
                "WHERE o.orderId = (?)" +
                "GROUP BY o.orderId ";
        try(Connection cxn = ConnectionPool.getConnection();
            PreparedStatement stmt = cxn.prepareStatement(SELECT_ORDER_DETAILS_FOR_ORDER)) {
            stmt.setString(1, orderId);

            boolean areThereResults = stmt.execute();

            if (areThereResults) {
                ResultSet resultSet = stmt.getResultSet();
                while(resultSet.next()) {
                    Map<String, String> row = new HashMap<>();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        row.put(columnName, columnValue);
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            RDSUtil.trace(e.toString());
        }
        return rows;

    }

    public List<String> getEstimatedTotesPerOrderToUpload() {
        String SELECT_TOTES_PER_ORDER_ESTIMATE = " SELECT orderId FROM custOrders o " +
                " WHERE o.status = 'cartonized' " +
                " AND o.cartonizeStamp IS NOT NULL " +
                " AND o.cartonizedUploadStamp IS NULL " +
                " ORDER BY cartonizeStamp; ";
        return db.getValueList(SELECT_TOTES_PER_ORDER_ESTIMATE);
    }

    public List<String> getActualTotesPerOrderToUpload() {
        String SELECT_TOTES_PER_ORDER_ESTIMATE = " SELECT orderId FROM custOrders o " +
                " WHERE o.labelStamp IS NOT NULL " +
                " AND o.uploadStamp IS NULL " +
                " ORDER BY cartonizeStamp; ";
        return db.getValueList(SELECT_TOTES_PER_ORDER_ESTIMATE);
    }



    
    public static void resetOrderData( String orderId ) {
   	 db.execute("UPDATE custOrders SET status='downloaded',errorMsg='',prepareStamp=NULL,cartonizeStamp=NULL WHERE orderId='%s'", orderId);
    }

}
