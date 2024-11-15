/*
 * OrderPrepApp.java
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
import static app.Constants.*;
import app.AppCommon;

import static rds.RDSLog.*;

public class CreateTestOrder
      extends AbstractPollingApp {

   private Random rand;
	
   public CreateTestOrder( String id, String rdsDb ) {
      super( id, rdsDb );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		AppCommon.setDatabase(db);
		rand = new Random();
   }

   protected void poll() {
   }
   
   protected void createTestOrder( String type, int count ) {
   	for( int i=0;i<count;i++ ) {
   		doCreateTestOrder(type);
   	}
   }
   
   protected void doCreateTestOrder( String type ) {
   	String testOrderSeqStr = db.getRuntime("testOrderSeq");
   	int testOrderSeq = RDSUtil.stringToInt(testOrderSeqStr, -1);
   	String custOrderId = String.format("ORDER%06d", testOrderSeq);
   	testOrderSeq++;
   	db.setRuntime("testOrderSeq", testOrderSeq+"");
   	String testShipSeqStr = db.getRuntime("testShipSeq");
   	int testShipSeq = RDSUtil.stringToInt(testShipSeqStr, -1);
   	String shipmentId = String.format("SHIP%06d", testShipSeq);
   	testShipSeq++;
   	db.setRuntime("testShipSeq", testShipSeq+"");
   	String orderId = shipmentId + "-" + custOrderId;
   	
   	int shipToShippingInfoSeq = type.equals(SHIPMENTTYPE_LTL)?52:56;
   	String shippingMethod = type;
   	db.execute("INSERT INTO custShipments SET shipmentId='%s', shippingMethod='%s', shipToShippingInfoSeq=%d", 
   			shipmentId,shippingMethod,shipToShippingInfoSeq);
   	db.execute("INSERT INTO custOrders SET orderId='%s',shipmentId='%s',custOrderId='%s'",orderId,shipmentId,custOrderId);
   	// create EA in splitCase area
   	List<String> splitCaseItems = db.getValueList(
   			"SELECT sku FROM custSkus WHERE uom='EA' AND weight<=10 ORDER BY SKU LIMIT 4");
   	/*
   	List<String> splitCaseLocations = db.getValueList(
   			"SELECT location FROM rdsLocations WHERE area REGEXP 'zone'");
   			*/
   	List<String> splitCaseLocations = new ArrayList<>();
   	splitCaseLocations.add(db.getString("", "SELECT location FROM rdsLocations WHERE area='zone1' LIMIT 1"));
   	splitCaseLocations.add(db.getString("", "SELECT location FROM rdsLocations WHERE area='zone2' LIMIT 1"));
   	splitCaseLocations.add(db.getString("", "SELECT location FROM rdsLocations WHERE area='zone3' LIMIT 1"));
   	splitCaseLocations.add(db.getString("", "SELECT location FROM rdsLocations WHERE area='zone4' LIMIT 1"));
   	int lineNumber = 1;
   	int n = random(2,4);
   	for( int i=0;i<n;i++ ) {
   		String sku = splitCaseItems.get(i);
   		String location = splitCaseLocations.get(i);
   		int qty = random(1,5);
   		db.execute("INSERT INTO custOrderLines SET orderId='%s', lineId='%d',sku='%s',uom='EA',location='%s',qty=%d", 
   				orderId,lineNumber,sku,location,qty);
   		lineNumber++;
   	}
   	// create amr bulk pick
   	List<String> amrBulkItems = db.getValueList(
   			"SELECT sku FROM custSkus WHERE uom='CA' AND description NOT LIKE '%%POLE%%' ORDER BY sku");
   	List<String> amrBulkLocations = db.getValueList(
   			"SELECT location FROM rdsLocations WHERE area='amr-bulk'"); 
   	n = type.equals(SHIPMENTTYPE_LTL)? random(4,20) : random(1,4);
   	for( int i=0;i<n;i++ ) {
   		String sku = amrBulkItems.get(i);
   		String location = amrBulkLocations.get(i);
   		int qty = random(1,3);
   		db.execute("INSERT INTO custOrderLines SET orderId='%s', lineId='%d',sku='%s',uom='CA',location='%s',qty=%d", 
   				orderId,lineNumber,sku,location,qty);
   		lineNumber++;   		
   	}
   	// create bulk pick
   	List<String> bulkItems = db.getValueList(
   			"SELECT sku FROM custSkus WHERE uom='CA' AND description LIKE '%%POLE%%' ORDER BY sku LIMIT 4");
   	List<String> bulkLocations = db.getValueList(
   			"SELECT location FROM rdsLocations WHERE area='bulk'");  
   	n = random(2,4);
   	for( int i=0;i<n;i++ ) {
   		String sku = bulkItems.get(i);
   		String location = bulkLocations.get(i);
   		int qty = random(1,3);
   		db.execute("INSERT INTO custOrderLines SET orderId='%s', lineId='%d',sku='%s',uom='CA',location='%s',qty=%d", 
   				orderId,lineNumber,sku,location,qty);
   		lineNumber++;   		
   	}
   	if( type.equals(SHIPMENTTYPE_LTL) ) {
	   	// create pallet pick
	   	List<String> palletItems = db.getValueList(
	   			"SELECT a.* FROM custSkus a JOIN custSkus b ON a.sku=b.sku WHERE a.uom='PA' AND b.uom='CA' ORDER BY a.sku LIMIT 4");
	   	List<String> palletLocations = db.getValueList(
	   			"SELECT location FROM rdsLocations WHERE area='pallet'");  
	   	n = random(2,4);
	   	for( int i=0;i<n;i++ ) {
	   		String sku = palletItems.get(i);
	   		String location = palletLocations.get(i);
	   		int qty = (i==0)? 1 : random(1,3);
	   		db.execute("INSERT INTO custOrderLines SET orderId='%s', lineId='%d',sku='%s',uom='PA',location='%s',qty=%d", 
	   				orderId,lineNumber,sku,location,qty);
	   		if( i==0 ) {
		   		db.execute("INSERT INTO custOrderLines SET orderId='%s', lineId='%d',sku='%s',uom='CA',location='%s',qty=-2", 
		   				orderId,lineNumber,sku,location);	   			
	   		}
	   		lineNumber++; 	   		
	   	}
   	}
   	db.execute("UPDATE custOrders SET status='downloaded',downloadStamp=NOW() WHERE orderId='%s'",orderId);
   	db.execute("UPDATE custShipments SET status='downloaded',downloadStamp=NOW() WHERE shipmentId='%s'",shipmentId);
   }
   
   private int random( int min, int max ) {
   	int range = max - min + 1;
   	return rand.nextInt(range) + min;
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
      String type = (args.length > 0) ? args[0] : SHIPMENTTYPE_LTL;
      int count = (args.length > 1) ? RDSUtil.stringToInt(args[1], 1) : 1;
      CreateTestOrder app = new CreateTestOrder("app","db");
      app.createTestOrder(type,count);
   }

}
