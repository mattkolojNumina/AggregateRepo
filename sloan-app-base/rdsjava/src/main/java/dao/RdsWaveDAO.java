package dao;

import records.RdsWave;
import host.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static rds.RDSLog.*;

public class RdsWaveDAO extends AbstractDAO implements DAO<RdsWave> {
	
	 private int waveSeq;
	 private static final String dataTable = "rdsWaves";
	 
	 public RdsWaveDAO() {
	 }
	 
	 public RdsWaveDAO( int waveSeq ) {
		 this.waveSeq = waveSeq;
		 recordMap = getTableRowByIntId(dataTable,"waveSeq",waveSeq);
	 }

    private static final String INSERT_STATEMENT =
            "INSERT INTO rds.rdsWaves SET waveName = (?), waveType = (?), fileSeq = (?)";
    
    @Override
    public Optional<RdsWave> get(int id) {
        return Optional.empty();
    }

    @Override
    public List<RdsWave> getAll() {
        return null;
    }

    @Override
    public int save(RdsWave wave) throws DataAccessException {
        try(Connection cxn = ConnectionPool.getConnection();
      		PreparedStatement stmt = cxn.prepareStatement(INSERT_STATEMENT)) {
            stmt.setString(1, wave.getWaveName());
            stmt.setString(2, wave.getWaveType());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            alert(e.toString());
            throw new DataAccessException(e.toString());
        }
    }
    
    public int createNewWave( RdsWave wave ) throws DataAccessException {
   	 int waveSeq = -1;
       try(Connection cxn = ConnectionPool.getConnection();
     		PreparedStatement stmt = cxn.prepareStatement(INSERT_STATEMENT)) {
           stmt.setString(1, wave.getWaveName());
           stmt.setString(2, wave.getWaveType());
           stmt.setInt(3, wave.getFileSeq());
           stmt.execute();
           Statement stmt2 = cxn.createStatement();
    		  boolean areThereResults = stmt2.execute("SELECT LAST_INSERT_ID()");
    		  if( areThereResults ) {
    			 ResultSet rs = stmt2.getResultSet();
    	       if (rs.next())
    	      	 waveSeq = rs.getInt( 1 );
    		  }
       } catch (SQLException e) {
           alert(e.toString());
           throw new DataAccessException(e.toString());
       }  
       return waveSeq;
    }

    @Override
    public void update(RdsWave proOperator, String[] params) {

    }

    @Override
    public void delete(RdsWave proOperator) {

    }
    
    public boolean exist( String waveName ) throws DataAccessException {
   	 String sql = String.format(
   			 "SELECT COUNT(*) FROM rds.rdsWaves WHERE waveName='%s' AND cancelStamp IS NULL AND errorStamp IS NULL AND labelStamp IS NULL", waveName);
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
    
    public List<String> getOrders( String waveName) throws DataAccessException {
       List<String> orderList = new ArrayList<>();
       String sql = String.format(
   			 "SELECT DISTINCT orderId FROM rds.custOrders JOIN rds.rdsWaves USING(waveSeq) WHERE waveName='%s'", waveName);
       try(Connection cxn = ConnectionPool.getConnection();
           Statement stmt = cxn.createStatement()) {
           boolean areThereResults = stmt.execute(sql);
           if (areThereResults) {
               ResultSet rs = stmt.getResultSet();
               while(rs.next()) {
               	 String orderId = rs.getString(1);
               	 orderList.add(orderId);
               }
           }
       } catch (Exception e) {
           trace(e.toString());
       }
       return orderList;
    }
    
    public static void setTombstone( int waveSeq, String field ) {
   	 setTableTombStoneByIntId(dataTable,field,"waveSeq",waveSeq);
    }
    
    public static void updateWaveCanReleaseFlag( int waveSeq ) {
   	 int numZoneRouteUnreleasedWave = db.getInt(-1, "SELECT COUNT(*) FROM rdsWaves WHERE waveSeq<%d AND zoneRouteReleaseStamp IS NULL", waveSeq);
   	 if( numZoneRouteUnreleasedWave == 0 )
   		 updateRowByIntIdIntValue(dataTable,"canReleaseZoneRoute",1,"waveSeq",waveSeq);
   	 int numCartPickUnreleasedWave = db.getInt(-1, "SELECT COUNT(*) FROM rdsWaves WHERE waveSeq<%d AND cartPickReleaseStamp IS NULL", waveSeq);
   	 if( numCartPickUnreleasedWave == 0 )
   		 updateRowByIntIdIntValue(dataTable,"canReleaseCartPick",1,"waveSeq",waveSeq);
   	 int numGeekUnreleasedWave = db.getInt(-1, "SELECT COUNT(*) FROM rdsWaves WHERE waveSeq<%d AND geekReleaseStamp IS NULL", waveSeq);
   	 if( numGeekUnreleasedWave == 0 )
   		 updateRowByIntIdIntValue(dataTable,"canReleaseGeek",1,"waveSeq",waveSeq);
    }
    
    public static void updateNextWaveCanReleaseFlag( int waveSeq, String area ) {
   	 String flagField = "";
   	 switch(area) {
   	 case "zoneRoute":
   		 flagField = "canReleaseZoneRoute";
   		 break;
   	 case "cartPick":
   		 flagField = "canReleaseCartPick";
   		 break;
   	 case "geek":
   		 flagField = "canReleaseGeek";
   		 break;
   	 }
   	 int nextWaveSeq = db.getInt(-1, "SELECT waveSeq FROM rdsWaves WHERE waveSeq>%d ORDER BY waveSeq LIMIT 1", waveSeq);
   	 if( nextWaveSeq>0 ) {
   		 updateRowByIntIdIntValue(dataTable,flagField,1,"waveSeq",nextWaveSeq);
   	 }
    }    
    
    public boolean tombStoneIsSet( String field ) {
   	 return !getMapStr(recordMap,field).isEmpty();
    }
    
    
}
