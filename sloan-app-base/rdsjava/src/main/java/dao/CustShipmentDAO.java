package dao;

import records.CustShipment;
import host.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static rds.RDSLog.*;

public class CustShipmentDAO extends AbstractDAO implements DAO<CustShipment>{
	
	private String shipmentId;
	
	public CustShipmentDAO( String shipmentId ) {
		this.shipmentId = shipmentId;
		recordMap = getTableRowByStringId("custShipments","shipmentId",shipmentId);
	}
	
	public Map<String,String> getShipmentMap(){
		return this.recordMap;
	}

   private static final String DELETE_STATEMENT =
         "DELETE FROM rds.custShipments WHERE shipmentId = (?)";
	
    private static final String INSERT_STATEMENT =
            "INSERT INTO rds.custShipments SET shipmentId = (?), status = (?)";

    @Override
    public Optional<CustShipment> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<CustShipment> getAll() {
        return null;
    }

    @Override
    public int save(CustShipment shipment) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_STATEMENT)) {
            stmt.setString(1, shipment.getShipmentId());
            stmt.setString(2, shipment.getStatus());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void update(CustShipment proOperator, String[] params) {

    }

    @Override
    public void delete(CustShipment shipment) throws DataAccessException {
       try(Connection cxn = ConnectionPool.getConnection();
     		  PreparedStatement stmt = cxn.prepareStatement(DELETE_STATEMENT)) {
           stmt.setString(1, shipment.getShipmentId());
           stmt.execute();
       } catch (SQLException e) {
           alert(e.toString());
           throw new DataAccessException(e.toString());
       }
    }
    
    public boolean exist( String shipmentId ) throws DataAccessException {
   	 String sql = String.format(
   			 "SELECT COUNT(*) FROM rds.custShipments WHERE shipmentId='%s' AND status NOT IN ('error','canceled')", shipmentId);
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
    
    public void updateField( String shipmentId, String field, String value ) throws DataAccessException {
   	 String sql = String.format(
   			 "UPDATE rds.custShipments SET %s=(?) WHERE shipmentId=(?)", field);
       try(Connection cxn = ConnectionPool.getConnection();
       		  PreparedStatement stmt = cxn.prepareStatement(sql)) {
             stmt.setString(1, value);
             stmt.setString(2, shipmentId);
             stmt.execute();
         } catch (SQLException e) {
             alert(e.toString());
             throw new DataAccessException(e.toString());
         }
    }
}
