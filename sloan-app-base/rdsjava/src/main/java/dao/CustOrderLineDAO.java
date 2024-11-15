package dao;

import records.CustOrderLine;
import host.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static rds.RDSLog.*;

public class CustOrderLineDAO extends AbstractDAO implements DAO<CustOrderLine> {
	
    private static final String INSERT_STATEMENT =
            "INSERT INTO rds.custOrderLines SET "
            + "orderId = (?), pageId = (?), lineId = (?), sku = (?),"
            + "uom = (?), description = (?), qty = (?), location = (?), "
            + "shelfPackQty = (?), groupNumber = (?), lineQPAGroup = (?)";

    private final String SELECT_ORDER_LINE_CONFIRMATION_TO_UPLOAD =
            "SELECT * FROM custOrderLines WHERE labelStamp IS NOT NULL AND uploadStamp IS NULL ORDER BY labelStamp ASC;";



    @Override
    public Optional<CustOrderLine> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustOrderLine> getAll() {
        return null;
    }

    @Override
    public int save(CustOrderLine line) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_STATEMENT)) {
            stmt.setString(1, line.getOrderId());
            stmt.setString(2, line.getPageId());
            stmt.setString(3, line.getLineId());
            stmt.setString(4, line.getSku());
            stmt.setString(5, line.getUom());
            stmt.setString(6, line.getDescription());
            stmt.setDouble(7, line.getQty());
            stmt.setString(8, line.getLocation());
            stmt.setInt(9, line.getShelfPackQty());
            stmt.setString(10, line.getGroupNumber());
            stmt.setString(11, line.getLineQPAGroup());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustOrderLine line, String[] params) {

    }

    @Override
    public void delete(CustOrderLine line) throws DataAccessException {
    }

    public List<String> getLineConfirmationToUpload(){
        return db.getValueList(SELECT_ORDER_LINE_CONFIRMATION_TO_UPLOAD);
    }

    public int getNumOfTotesFromOrderLine(int orderLineSeq){
        String sql =
                String.format("SELECT COUNT(DISTINCT cartonSeq) FROM rdsPicks "
                        + " WHERE orderLineSeq = %d "
                        + " AND picked = 1 "
                        + " AND canceled = 0 "
                        + " AND shortPicked = 0;",orderLineSeq);
        return db.getInt(-1,sql);
    }

    public int getPickOperatorIdFromOrderLine(int orderLineSeq){
        String sql =
                String.format("SELECT pickOperatorId FROM rdsPicks "
                        + " WHERE orderLineSeq = %d "
                        + " AND picked = 1 "
                        + " AND shortPicked = 0;",orderLineSeq);
        return db.getInt(0,sql);
    }
    
    public boolean exist( String orderId, String pageId, String lineId ) throws DataAccessException {
   	 String sql = String.format(
   			 "SELECT COUNT(*) FROM rds.custOrderLines WHERE orderId='%s' "
   			 + "AND pageId='%s' AND lineId='%s' AND status NOT IN ('error','canceled')", orderId,pageId,lineId);
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
    
}
