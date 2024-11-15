/*
 * ContainerBuildApp.java
 * 
 * Import/prep orders and pick lines from host data.
 * 
 * For the Candy.com pick/pack/ship system
 * 
 * (c) 2019, Numina Group, Inc.
 */

package orchestration;

import java.util.*;

import polling.*;
import rds.*;


import static sloane.SloaneConstants.*;
import dao.SloaneCommonDAO;

import static rds.RDSLog.*;

public class ContainerBuildApp
      extends AbstractPollingApp {

   private static final String DEFAULT_ID = "ctBuild";
   private Map<String,String> controlMap;
   private static final double MAX_CART_WEIGHT = 300;
   private static final String SLOT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   private static int neighborScoreWeight = 5;
   
   private int cartSeq;
   private String cartId;
   private String cartType;
   private double cartWeight;
   private double maxWeight;
   private int numCartons;
   private int numSlots = 4;

   public ContainerBuildApp( String id, String rdsDb ) {
      super( id, rdsDb );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		SloaneCommonDAO.setDatabase(db);
		controlMap = db.getControlMap("system");
   }
   
   private void resetVariables(){
   	cartSeq = -1;
   	cartId = "";
   	cartType = "";
      cartWeight = 0;
      numCartons = 0;
      maxWeight = MAX_CART_WEIGHT;  
   }   

   protected void poll() {
      pollCartBuild();
   }

   
   private void pollCartBuild() {
      long start = System.currentTimeMillis();
      Map<String,String> cartMap = getNextCart();
      if (cartMap == null || cartMap.isEmpty())
         return;
      resetVariables();
      buildCart(cartMap);
      long dt = System.currentTimeMillis() - start;
      inform( "processing took %d msec", dt );
   }
   
   private Map<String,String> getNextCart(){
   	return db.getRecordMap("SELECT rdsCarts.*, numSlots FROM rdsCarts JOIN cfgCarts USING(cartId) "
   			+ "WHERE createStamp IS NULL AND errorStamp IS NULL ORDER BY cartSeq LIMIT 1");
   }
   
   private void buildCart( Map<String,String> cartMap  ) {
      try {
         if (lock( "carts" )){
            doBuildCart( cartMap );
            updateCartCreate( true, "" );
         } else
            throw new ProcessingException( "unable to obtain database lock" );
      } catch (DataException ex) {
         alert( "data error: %s", ex.getMessage() );
         updateCartCreate( false, ex.getMessage() );
      } catch (ProcessingException ex) {
         alert( "processing error: %s", ex.getMessage() );
         updateCartCreate( false, ex.getMessage() );
      } finally {
         unlock( "carts" );
      }
   }
   
   private void doBuildCart( Map<String,String> cartMap )
         throws DataException, ProcessingException {
   	cartId = getMapStr(cartMap,"cartId");
      if (cartId.isEmpty())
         throw new DataException( "invalid cart ID" );
      cartSeq = getMapInt(cartMap,"cartSeq");
      cartType = getMapStr(cartMap,"cartType");
      numSlots = getMapInt(cartMap,"numSlots");
      trace("Start build cart: cartId [%s], cartSeq [%d], cartType [%s]",cartId,cartSeq,cartType);
      SloaneCommonDAO.postCartLog(cartSeq+"", id, "Start cart build");
      assignCarton( cartMap );
   }   
   
   private void assignCarton( Map<String,String> cartMap )
         throws DataException, ProcessingException {	
      assignSlots();
      int numOfSlotsAssigned = db.getInt(0, 
      		"SELECT COUNT(*) FROM rdsCartons WHERE cartSeq=%d ", cartSeq);
      if( numOfSlotsAssigned == 0 )
      	throw new ProcessingException( "no cartons ready for cart build" );          
   }
   
   private void assignSlots() 
   		throws ProcessingException {
   	for( int i=0; i<numSlots; i++ ){
   		String slot = getSlot(i);
   		inform( "assign %s slot %s",cartId,slot);
			Map<String,String> cartonMap = getCarton( maxWeight-cartWeight);
      	if( cartonMap == null || cartonMap.isEmpty() ){
      		break;	
      	}
      	int cartonSeq = getMapInt( cartonMap,"cartonSeq");
      	double cartonWeight = getMapDbl(cartonMap,"estWeight");   
      	inform( "   found carton %d", cartonSeq );
      	cartWeight += cartonWeight;
      	db.execute("UPDATE rdsCartons SET cartSeq=%d,cartSlot='%s' WHERE cartonSeq=%d", 
      			cartSeq,slot,cartonSeq);
      	SloaneCommonDAO.postCartonLog(cartonSeq+"", id, "Assigned on cartSeq %d slot %s", cartSeq, slot); 
      	numCartons++;
   	}	
   }   
   
   private Map<String,String> getCarton( double allowedWeight )
         throws ProcessingException {
   	String typeSql = cartType.equals(CARTTYPE_PQ)?String.format("c.pickType IN ('%s','%s') ", PICKTYPE_LIQUIDS, PICKTYPE_PERISHABLES):
   										                   String.format("c.pickType = '%s' ", PICKTYPE_AERSOLBOOM );
      Map<String,String> cartonMap = db.getRecordMap(
      		"SELECT c.*,  COUNT(DISTINCT pickSeq) AS numPicks, COUNT(DISTINCT rdsLocations.neighborhood)*%d AS numNeighbors, "
      		+ "SUM(t.qty) AS totalOverlap, SUM(l.qty)*%d AS overlapNeighbor "
      		+ "FROM rdsCartons c "
      		+ "JOIN rdsPicks USING(cartonSeq) JOIN custOrderLines USING(orderLineSeq) "
      		+ "JOIN rdsLocations USING(location) "
      		+ "LEFT JOIN "
      		+ "( "
      		+ "SELECT p.sku, COUNT(*) AS qty FROM rdsPicks p JOIN rdsCartons USING(cartonSeq) "
      		+ "WHERE rdsCartons.cartSeq=%d GROUP BY p.sku "
      		+ ") AS t ON rdsPicks.sku=t.sku "
      		+ "LEFT JOIN "
      		+ "( "
      		+ "SELECT neighborhood, COUNT(*) AS qty "
      		+ "FROM rdsPicks p JOIN rdsCartons USING(cartonSeq) "
      		+ "JOIN custOrderLines USING(orderLineSeq) "
      		+ "JOIN rdsLocations USING(location) "
      		+ "WHERE rdsCartons.cartSeq=%d GROUP BY neighborhood "
      		+ ") AS l ON l.neighborhood=rdsLocations.neighborhood "
      		+ "WHERE %s "
      		+ "AND c.releaseStamp IS NOT NULL AND c.cancelStamp IS NULL "
      		+ "AND c.cartSeq=-1 "
      		+ "AND estWeight<= %f "
      		+ "GROUP BY c.cartonSeq "
      		+ "ORDER BY DATE(c.releaseStamp), HOUR(c.releaseStamp), MINUTE(c.releaseStamp), totalOverlap DESC, overlapNeighbor DESC, numPicks DESC, numNeighbors DESC "
      		+ "LIMIT 1", neighborScoreWeight, neighborScoreWeight, cartSeq, cartSeq, typeSql, allowedWeight 		
      		);
      return cartonMap;
   }   
  
   private void updateCartCreate( boolean success, String errorMsg ) {
      if( success ){
      	db.execute("UPDATE rdsCarts SET createStamp=NOW() WHERE cartSeq=%d", cartSeq);
      	trace("%s(%d) built, cart weight %f", cartId, cartSeq, cartWeight);
      	SloaneCommonDAO.postCartLog(cartSeq+"", id, "cart is created with %d cartons",numCartons);
      } else {
      	db.execute("UPDATE rdsCarts SET errorStamp=NOW(), errorMsg='%s' WHERE cartSeq=%d", errorMsg, cartSeq);
      	db.execute("UPDATE rdsCartons SET cartSeq=-1,cartSlot='' WHERE cartSeq=%d", cartSeq);
      }
      resetVariables();
   }   
   
   
	

   
   /*
    * --- utility methods
    */
   
   private String getSlot( int slotNum ) {
      if (slotNum < 0 || slotNum >= SLOT_LETTERS.length())
         return "";
      return SLOT_LETTERS.substring( slotNum, slotNum + 1 );
   }   

   /*
    * --- main ---
    */

   /**
    * Application entry point.
    * 
    * @param   args  command-line arguments
    */
   public static void main( String... args ) {
      String id = (args.length > 0) ? args[0] : DEFAULT_ID;
      String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;
      trace( "application started, id = [%s], db = [%s]", id, rdsDb );
      ContainerBuildApp app = new ContainerBuildApp( id, rdsDb );
      app.run();
   }

}
