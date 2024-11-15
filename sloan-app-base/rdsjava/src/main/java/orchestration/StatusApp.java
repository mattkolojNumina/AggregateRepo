package orchestration;

import rds.*;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import static rds.RDSLog.* ;

import polling.*;
import dao.SloaneCommonDAO;
import dao.AbstractDAO;
import dao.CustOrderDAO;
import dao.RdsWaveDAO;
import dao.RdsCartonDAO;

import static sloane.SloaneConstants.*;

public class StatusApp 
	extends AbstractPollingApp {
		
	public StatusApp(String id){
		super( id,"db" );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		RDSTrak.setDatabase(db);
		AbstractDAO.setDatabase(db);
		//SloaneCommonDAO.setDatabase(db);
		//CustOrderDAO.setDatabase(db);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Util functions
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	private List<String> getOrdersInCarton( int cartonSeq ){
		return db.getValueList("SELECT DISTINCT orderId FROM rdsPicks WHERE cartonSeq=%d", cartonSeq);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Shipment/order Status monitor
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void updateOrderStatus( String orderId ) {
      Map<String,String> m = db.getRecordMap(
      	"SELECT o.status, o.waveSeq, "
      	+ "COUNT(DISTINCT CASE WHEN ol.cancelStamp IS NULL THEN p.orderLineSeq END ) AS lineCount, "
      	+ "COUNT(DISTINCT CASE WHEN c.cancelStamp IS NULL THEN p.cartonSeq END ) AS cartonCount, "
      	+ "COUNT(DISTINCT CASE WHEN c.cancelStamp IS NULL AND c.pickStamp IS NOT NULL THEN p.cartonSeq END) AS numPicked, "
      	+ "COUNT(DISTINCT CASE WHEN c.cancelStamp IS NULL AND c.packStamp IS NOT NULL THEN p.cartonSeq END) AS numPacked, "
      	+ "COUNT(DISTINCT CASE WHEN c.cancelStamp IS NULL AND c.labelStamp IS NOT NULL THEN p.cartonSeq END) AS numLabeled, "
      	+ "COUNT(DISTINCT CASE WHEN c.cancelStamp IS NULL AND c.shipStamp IS NOT NULL THEN p.cartonSeq END) AS numShipped "
      	+ "FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) "
      	+ "JOIN custOrderLines ol USING(orderLineSeq) "
      	+ "JOIN custOrders o ON p.orderId = o.orderId "
      	+ "WHERE o.orderId='%s' GROUP BY o.orderId",orderId);	
		String status = getMapStr( m, "status" );
		int waveSeq = getMapInt( m, "waveSeq" );
		int cartonCount = getMapInt( m, "cartonCount" );
		int lineCount = getMapInt( m, "lineCount" );
		int numPicked = getMapInt( m, "numPicked" );
		int numPacked = getMapInt( m, "numPacked" );
		int numLabeled = getMapInt( m, "numLabeled" );
		int numShipped = getMapInt( m, "numShipped" );
		if( cartonCount == 0 || lineCount == 0 ) {
   		alert("orderId %s has all cartons/lines canceled", orderId);
   		SloaneCommonDAO.postOrderLog(orderId,id,"order canceled due to all cartons/lines canceled");
	   	db.execute("UPDATE custOrders SET status='canceled', cancelStamp=NOW(), errorMsg='all cartons canceled' WHERE orderId='%s'", orderId);
	   	status = "canceled";
		}
		if( "cartonized".equals(status) ) {
			trace("orderId %s started picking", orderId);
			//db.execute("UPDATE custOrders SET status='picking', pickStartStamp=NOW() WHERE orderId='%s'", orderId );
			CustOrderDAO.setStatusAndTombStone(orderId, "picking", "pickStartStamp");
	   	SloaneCommonDAO.postOrderLog(orderId,id,"order started picking");
	   	status = "picking";
		}		
		if( "picking".equals(status) ) {
			if( numPicked == cartonCount ) {
	   		trace("orderId %s has all cartons picked", orderId);
		   	//db.execute("UPDATE custOrders SET status='picked', pickEndStamp=NOW() WHERE orderId='%s'", orderId );
		   	CustOrderDAO.setStatusAndTombStone(orderId, "picked", "pickEndStamp");
		   	SloaneCommonDAO.postOrderLog(orderId,id,"order is picked");
	   		status = "picked";
			}
		}
		if( "picked".equals(status) ) {
			if( numPacked == cartonCount ) {
	   		trace("orderId %s has all cartons packed", orderId);
		   	//db.execute("UPDATE custOrders SET status='packed', packStamp=NOW() WHERE orderId='%s'", orderId );
		   	CustOrderDAO.setStatusAndTombStone(orderId, "packed", "packStamp");
		   	SloaneCommonDAO.postOrderLog(orderId,id,"order has all cartons packed");
	   		status="packed";
			}			
		}
		if( "packed".equals(status) ) {
			if( numLabeled == cartonCount ) {
	   		trace("orderId %s has all cartons labeled", orderId);
	   		//db.execute("UPDATE custOrders SET status='labeled', labelStamp=NOW() WHERE orderId='%s'", orderId );
	   		CustOrderDAO.setStatusAndTombStone(orderId, "labeled", "labelStamp");
	   		SloaneCommonDAO.postOrderLog(orderId,id,"order has all cartons labeled");
	   		status="labeled";
			}			
		}
		if( "labeled".equals(status) ) {
			if( numShipped == cartonCount ) {
	   		trace("orderId %s has all cartons shipped", orderId);
	   		//db.execute("UPDATE custOrders SET status='completed', completeStamp=NOW() WHERE orderId='%s'", orderId );
	   		CustOrderDAO.setStatusAndTombStone(orderId, "completed", "completeStamp");
	   		SloaneCommonDAO.postOrderLog(orderId,id,"order has all cartons shipped, mark completed");
		   	RDSCounter.increment(COUNTER_ORDER_COMPLETE);
			}			
		}
		updateWaveStatus( waveSeq );
	}	
	
	private void updateWaveStatus( int waveSeq ) {
      Map<String,String> m = db.getRecordMap(
      	"SELECT w.*, "
      	+ "COUNT(CASE WHEN o.cancelStamp IS NULL THEN orderId END) AS orderCount, "
      	+ "COUNT(CASE WHEN o.cancelStamp IS NULL THEN o.pickEndStamp END) AS numPicked, "
      	+ "COUNT(CASE WHEN o.cancelStamp IS NULL THEN o.labelStamp END) AS numLabeled, "
      	+ "COUNT(CASE WHEN o.cancelStamp IS NULL THEN o.completeStamp END) AS numCompleted "
      	+ "FROM custOrders o JOIN rdsWaves w USING(waveSeq) "
      	+ "WHERE w.waveSeq=%d GROUP BY w.waveSeq",waveSeq);	
		int orderCount = getMapInt( m, "orderCount" );
		int numPicked = getMapInt( m, "numPicked" );
		int numLabeled = getMapInt( m, "numLabeled" );
		int numCompleted = getMapInt( m, "numCompleted" );
		if( orderCount == 0 ) {
   		alert("wave %d has all orders canceled", waveSeq);
   		SloaneCommonDAO.postWaveLog(waveSeq+"",id,"wave canceled due to all orders canceled");
	   	db.execute("UPDATE rdsWaves SET cancelStamp=NOW(), errorMsg='all orders canceled' WHERE waveSeq=%d", waveSeq);
	   	return;
		}
		if( getMapStr(m,"startStamp").isEmpty() ) {
			trace("waveSeq %d started picking", waveSeq);
			RdsWaveDAO.setTombstone(waveSeq, "startStamp");
	   	SloaneCommonDAO.postWaveLog(waveSeq+"",id,"wave started picking");
		}
		if( getMapStr(m,"pickEndStamp").isEmpty() ) {
			if( numPicked == orderCount ) {
	   		trace("waveSeq %d has all orders picked", waveSeq);
				RdsWaveDAO.setTombstone(waveSeq, "pickEndStamp");
		   	SloaneCommonDAO.postWaveLog(waveSeq+"",id,"wave has all orders picked");
			}			
		}	
		if( getMapStr(m,"labelStamp").isEmpty() ) {
			if( numLabeled == orderCount ) {
	   		trace("waveSeq %d has all orders labeled", waveSeq);
				RdsWaveDAO.setTombstone(waveSeq, "labelStamp");
		   	SloaneCommonDAO.postWaveLog(waveSeq+"",id,"wave has all orders labeled");
			}			
		}	
		if( getMapStr(m,"completeStamp").isEmpty() ) {
			if( numCompleted == orderCount ) {
	   		trace("waveSeq %d has all orders completed", waveSeq);
				RdsWaveDAO.setTombstone(waveSeq, "completeStamp");
		   	SloaneCommonDAO.postWaveLog(waveSeq+"",id,"wave has all orders completed");
			}			
		}
	}		

	private void processStatusMessages() {
		List<Map<String,String>> messages = SloaneCommonDAO.getStatusMessages(id);
	   for( Map<String,String> message : messages ) {
	   	String statusType = getMapStr(message,"statusType");
	   	String operator = getMapStr( message, "operator" );
	   	trace("process %s message requested by %s",statusType, operator);
	   	switch(statusType) {
	   	case "pick":
	   		doPickConfirm(message);break;		
	   	case "pickShort":
	   		doPickShortConfirm(message);break;		   		
	   	case "shipShort":
	   		doShipShortConfirm(message);break;
		case "shipShortCarton":
			doShipShortCartonConfirm(message);break;
	   	case "dashboardPick":
	   		doDashboardPickConfirm(message);break;
	   	case "assignChasePick":
	   		doAssignChasePick(message);break;
	   	case "assignChasePickForSku":
	   		doAssignChasePickForSku(message);break;
		case "assignChasePickLocationForRecord":
			doAssignChasePickLocationForRecord(message);break;
		case "assignChasePickLocationForSku":
			doAssignChasePickLocationForSku(message);break;
	   	case "chasePicked":
	   		doChasePick(message);break;
		case "chasePickedForSku":
			doChasePickForSku(message);break;
		case "chasePickedNotFound":
			doChasePickNotFound(message);break;
		case "chasePickedNotFoundForSku":
			doChasePickNotFoundForSku(message);break;
	   	case "chasePickPut":
	   		doChasePickPut(message);break;
	   	case "markOut":
	   		doMarkOut(message);break;
	   	case "clearMarkOut":
	   		doClearMarkOut(message);break;
	   	case "cartonPick":
	   		doCartonPick(message);break;
	   	case "cartonPack":
	   		doCartonPack(message);break;	   		
	   	case "cartonLabel":
	   		doCartonLabel(message);break;
	   	case "cartonShip":
	   		doCartonShip(message);break;	 
	   	case "waveComplete":
	   		doWaveComplete(message);break;	   		
	   	case "completeCart":
	   		doCompleteCart(message);break;
	   	case "retryOrder":
	   		doRetryOrder(message);break;
	   	case "cancelOrder":
	   		doCancelOrder(message);break;
	   	case "cancelCarton":
	   		doCancelCarton(message);break;
	   	case "updateOrderLineQty":
	   		doUpdateOrderLineQty(message);break;
	   	case "assignLpn":
	   		doAssignLpnForGeekShortCarton(message);break;
	   	case "reassignLpn":
	   		doReAssignLpnNotStartedCarton(message);break;
	   	case "requireAudit":
	   		doRequireAudit(message);break;
	   	case "updateToteLpn":
	   		doUpdateToteLpn(message);break;
	   	}
	   }
	   	messages = db.getResultMapList(
			"SELECT * FROM status " +
			"WHERE appName='status' AND statusType='delayLogout' AND status='idle' " +
			"AND DATE_ADD(stamp, INTERVAL 5 MINUTE)<NOW() ORDER BY seq" );	
		for( Map<String,String> message : messages ) {
			String statusType = getMapStr(message,"statusType");
			String operator = getMapStr( message, "operator" );
			trace("process %s message requested by %s",statusType, operator);
			doDelayedLogout(message);
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Carton Status Messages Monitor
	//////////////////////////////////////////////////////////////////////////////////////////////////	
	
	private void doDashboardPickConfirm( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");
   	String operator = getMapStr( message, "operator" );
   	trace("process dashboard pick confirm message for pickSeq %d",pickSeq);
   	db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),pickOperatorId='%s' WHERE pickSeq=%d", operator, pickSeq);
   	updatePickStatus( pickSeq, false );
	}
	
	private void doPickConfirm( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");
   	trace("process pick confirm message for pickSeq %d",pickSeq);
   	updatePickStatus( pickSeq, false );
	}
	
	private void doPickShortConfirm( Map<String,String> message ) {
		int seq = getMapInt(message,"seq");
		SloaneCommonDAO.setStatusMessageDone(seq);
		String data = getMapStr( message, "data" );
		JSONObject jsonObject = new JSONObject(data);
		int pickSeq = jsonObject.getInt("pickSeq");
		trace("process pick confirm message for pickSeq %d",pickSeq);
	   	//get pick type
		String sku= db.getString("", "SELECT sku FROM rdsPicks WHERE pickSeq=%d", pickSeq);
		int result = db.getInt(-1, "SELECT count(*) FROM rdsPicks "
				+ "JOIN rdsMarkOutSkus USING (sku) WHERE pickSeq=%d "
				+ "AND pickType='Geek' AND isActive=1 AND sku='%s'", pickSeq, sku);
		if (result == 1) {
			trace("Geek pick short confirm message for pickSeq %d sku %s in mark out skus ", pickSeq, sku);
			db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW() WHERE pickSeq=%d", pickSeq);
			updatePickStatus( pickSeq, false );
			return;
		}
		boolean autoAssignChasePicker = db.getControl("system", "autoAssignChasePicker", "no").equals("yes");
		if( autoAssignChasePicker )
			autoAssignChasePicker( pickSeq );
		updatePickStatus( pickSeq, true );
	}
	
	private void autoAssignChasePicker( int pickSeq ) {
		trace("Auto assigning chase picker for pickSeq %d", pickSeq);
		Map<String,String> pickMap = SloaneCommonDAO.getTableRowByIntId("rdsPicks", "pickSeq", pickSeq);
		String chasePickOperatorId = getMapStr(pickMap,"chasePickOperatorId");
		if( !chasePickOperatorId.isEmpty() ) return;
		String pickType = getMapStr(pickMap,"pickType");
		String defaultChasePicker = db.getString("", "SELECT defaultChasePicker FROM cfgDepartments WHERE rdsPickZone='%s' LIMIT 1", pickType);
		if( defaultChasePicker.isEmpty() ) return;
		db.execute("UPDATE rdsPicks SET chasePickOperatorId='%s' WHERE pickSeq=%d", defaultChasePicker, pickSeq);
	}
	
	private void doShipShortConfirm( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");  
   	trace("process ship short confirm message for pickSeq %d",pickSeq);
   	db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW() WHERE pickSeq=%d", pickSeq);
   	updatePickStatus( pickSeq,false );
	}

	private void doShipShortCartonConfirm( Map<String,String> message ) {
	int seq = getMapInt(message,"seq");
	SloaneCommonDAO.setStatusMessageDone(seq);
	String data = getMapStr( message, "data" );
	JSONObject jsonObject = new JSONObject(data);
	int cartonSeq = jsonObject.getInt("cartonSeq");
	trace("process ship short carton confirm message for cartonSeq %d",cartonSeq);
	List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks "
			+ "WHERE picked=0 AND cartonSeq=%d", cartonSeq);
	for( String s : picks ) {
		int pickSeq = RDSUtil.stringToInt(s, -1);
		if( pickSeq > 0 ) {
			db.execute("UPDATE rdsPicks SET picked=1, pickStamp=NOW(), shortPicked=1, shortStamp = NOW() WHERE pickSeq=%d", pickSeq);
			updatePickStatus( pickSeq,false );
		}
	}
	}
	
	private void doAssignChasePick( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");
   	String chasePickOperatorId = jsonObject.getString("chasePickOperatorId");
   	trace("process assign chase pick message for pickSeq %d",pickSeq);
   	db.execute("UPDATE rdsPicks SET chasePickOperatorId='%s' WHERE pickSeq=%d", chasePickOperatorId, pickSeq);
	}
	
	private void doAssignChasePickForSku( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String sku = jsonObject.getString("sku");
   	String pickType = jsonObject.getString("pickType");
   	String chasePickOperatorId = jsonObject.getString("chasePickOperatorId");
   	trace("process assign chase pick message for sku %s pickType %s",sku,pickType);
   	List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE sku='%s' "
   			+ "AND rdsPicks.pickType='%s' AND picked=0 AND shortPicked=1 AND chasePickOperatorId='' "
   			+ "AND c.lpn<>c.trackingNumber AND c.cancelStamp IS NULL", sku, pickType);
   	for( String s : picks ) {
   		int pickSeq = RDSUtil.stringToInt(s, -1);
   		if( pickSeq > 0 ) {
   			db.execute("UPDATE rdsPicks SET chasePickOperatorId='%s' WHERE pickSeq=%d", chasePickOperatorId, pickSeq);
   		}
   	}
	}

	private void doAssignChasePickLocationForRecord( Map<String,String> message ) {
		int seq = getMapInt(message,"seq");
		SloaneCommonDAO.setStatusMessageDone(seq);
		String data = getMapStr( message, "data" );
		JSONObject jsonObject = new JSONObject(data);
		int pickSeq = jsonObject.getInt("pickSeq");
		String chasePickLocation = jsonObject.getString("chasePickLocation");
		trace("process assign chase pick location for pickSeq %d",pickSeq);
		db.execute("UPDATE rdsPicks SET chasePickLocation='%s' WHERE pickSeq=%d", chasePickLocation, pickSeq);
	}

	private void doAssignChasePickLocationForSku( Map<String,String> message ) {
		int seq = getMapInt(message,"seq");
		SloaneCommonDAO.setStatusMessageDone(seq);
		String data = getMapStr( message, "data" );
		JSONObject jsonObject = new JSONObject(data);
		String sku = jsonObject.getString("sku");
		String chasePickOperatorId = jsonObject.getString("chasePickOperatorId");
		String currentChasePickLocation  = jsonObject.getString("currentChasePickLocation");
		String newChasePickLocation = jsonObject.getString("newChasePickLocation");
		trace("process assign chase pick location %s for sku %s", newChasePickLocation, sku);
		db.execute("UPDATE rdsPicks SET chasePickLocation='%s' "
				+ "WHERE sku='%s' AND chasePickLocation='%s' AND chasePickOperatorId='%s' "
				+ "AND picked = 0 AND shortPicked = 1 AND chasePicked=1",
				newChasePickLocation, sku, currentChasePickLocation, chasePickOperatorId);
	}
	
	private void doChasePick( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");
   	trace("process dashboard chase pick confirm message for pickSeq %d",pickSeq);
   	db.execute("UPDATE rdsPicks SET chasePicked=2 WHERE pickSeq=%d", pickSeq);
	}

	private void doChasePickForSku( Map<String,String> message ) {
	int seq = getMapInt(message,"seq");
	SloaneCommonDAO.setStatusMessageDone(seq);
	String data = getMapStr( message, "data" );
	JSONObject jsonObject = new JSONObject(data);
	String sku = jsonObject.getString("sku");
	String pickType = jsonObject.getString("pickType");
	String chasePickOperatorId = jsonObject.getString("chasePickOperatorId");
	trace("process dashboard chase pick confirm message for sku %s pickType %s",sku, pickType);
	List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE sku='%s' "
			+ "AND rdsPicks.pickType='%s' AND picked=0 AND shortPicked=1 AND chasePicked=1 AND chasePickOperatorId='%s' "
			+ "AND c.lpn<>c.trackingNumber AND c.cancelStamp IS NULL", sku, pickType, chasePickOperatorId);
	for( String s : picks ) {
		int pickSeq = RDSUtil.stringToInt(s, -1);
		if( pickSeq > 0 ) {
			db.execute("UPDATE rdsPicks SET chasePicked=2 WHERE pickSeq=%d", sku);
		}
	}
	}

	private void doChasePickNotFound( Map<String,String> message ) {
	int seq = getMapInt(message,"seq");
	SloaneCommonDAO.setStatusMessageDone(seq);
	String data = getMapStr( message, "data" );
	JSONObject jsonObject = new JSONObject(data);
	int pickSeq = jsonObject.getInt("pickSeq");
	trace("process dashboard chase pick not found message for pickSeq %d",pickSeq);
	db.execute("UPDATE rdsPicks SET chasePicked=2, notFound=1 WHERE pickSeq=%d", pickSeq);
	}

	private void doChasePickNotFoundForSku( Map<String,String> message ) {
		int seq = getMapInt(message,"seq");
		SloaneCommonDAO.setStatusMessageDone(seq);
		String data = getMapStr( message, "data" );
		JSONObject jsonObject = new JSONObject(data);
		String sku = jsonObject.getString("sku");
		String pickType = jsonObject.getString("pickType");
		String chasePickOperatorId = jsonObject.getString("chasePickOperatorId");
		trace("process dashboard chase pick not found message for sku %s pickType %s", sku, pickType);
		List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE sku='%s' "
				+ "AND rdsPicks.pickType='%s' AND picked=0 AND shortPicked=1 AND chasePicked=1 AND chasePickOperatorId='%s' "
				+ "AND c.lpn<>c.trackingNumber AND c.cancelStamp IS NULL", sku, pickType, chasePickOperatorId);
		for( String s : picks ) {
			int pickSeq = RDSUtil.stringToInt(s, -1);
			if( pickSeq > 0 ) {
				db.execute("UPDATE rdsPicks SET chasePicked=2, notFound=1 WHERE pickSeq=%d", pickSeq);
			}
		}
	}

	private void doChasePickPut( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int pickSeq = jsonObject.getInt("pickSeq");
   	trace("process dashboard chase pick put confirm message for pickSeq %d",pickSeq);
   	db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),shortPicked=0,shortStamp=NULL WHERE pickSeq=%d", pickSeq);
   	updatePickStatus( pickSeq, false );
	}
	
	private void doMarkOut( Map<String,String> message ) {
		int seq = getMapInt(message,"seq");
		SloaneCommonDAO.setStatusMessageDone(seq);
		String data = getMapStr( message, "data" );
		JSONObject jsonObject = new JSONObject(data);
		String sku = jsonObject.getString("sku");
		String operator = getMapStr( message, "operator" );
		//String uom = jsonObject.getString("uom");
		trace("process mark out message for sku %s",sku);
		db.execute("REPLACE INTO rdsMarkOutSkus SET sku='%s',operator='%s',isActive=1,stamp=NOW()", sku,operator);
	   	//pick type is geek
		List<String> geekPicks = db.getValueList("SELECT pickSeq FROM rdsPicks WHERE pickType='Geek' AND sku='%s' AND picked=0 AND shortPicked=1", sku);
		for( String s : geekPicks ) {
			int pickSeq = RDSUtil.stringToInt(s, -1);
			if( pickSeq > 0 ) {
				trace("process mark out for geek pick pickSeq %d",pickSeq);
				db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),markOut=1 WHERE pickSeq=%d", pickSeq);
				updatePickStatus( pickSeq, false );
			}
		}
		//pick type is not geek
		List<String> picks = db.getValueList("SELECT pickSeq FROM rdsPicks WHERE pickType!='Geek' AND sku='%s' AND picked=0", sku);
		for( String s : picks ) {
			int pickSeq = RDSUtil.stringToInt(s, -1);
			if( pickSeq > 0 ) {
				trace("process mark out for non geek pick pickSeq %d",pickSeq);
				db.execute("UPDATE rdsPicks SET picked=1,pickStamp=NOW(),markOut=1,shortPicked=1,shortStamp=NOW() WHERE pickSeq=%d", pickSeq);
				updatePickStatus( pickSeq, false );
			}
		}
	}	
	
	private void doClearMarkOut( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String sku = jsonObject.getString("sku");
   	trace("process clear mark out message for sku %s",sku);
   	db.execute("UPDATE rdsMarkOutSkus SET isActive=0 WHERE sku='%s'", sku);
	}	

	private void updatePickStatus( int pickSeq, boolean isPickShort ) {
   	Map<String,String> pickMap = SloaneCommonDAO.getTableRowByIntId("rdsPicks", "pickSeq", pickSeq);
   	String orderId = getMapStr( pickMap, "orderId" );
   	int orderLineSeq = getMapInt( pickMap, "orderLineSeq" );
   	int cartonSeq = getMapInt( pickMap, "cartonSeq" );  	
		updateOrderLinePickStatus( orderLineSeq );
		updateCartonPickStatus( cartonSeq );
		if( isPickShort ) {
			RdsCartonDAO.setTombstone(cartonSeq,"pickShortStamp");
			SloaneCommonDAO.setTableTombStoneByIntId("custOrderLines", "primaryShortStamp", "orderLineSeq", orderLineSeq);
		}
		updateOrderStatus( orderId );
	}	
	
	private void updateCartonPickStatus( int cartonSeq ) {
		Map<String,String> cartonMap = RdsCartonDAO.getRecordMap(cartonSeq);
		if( !getMapStr(cartonMap,"pickStamp").isEmpty() ) return;
		if( getMapStr(cartonMap,"pickStartStamp").isEmpty() ) {
			RdsCartonDAO.setTombstone(cartonSeq,"pickStartStamp");
			SloaneCommonDAO.postCartonLog(""+cartonSeq,id,"carton started picking");
		}
		doUpdateCartonPickStatus( cartonSeq );
	}
	
	private void doUpdateCartonPickStatus( int cartonSeq ) {
		Map<String,String> m = db.getRecordMap(
				"SELECT COUNT(CASE WHEN picked=0 THEN pickSeq END) AS notPicked, "
				+ "COUNT(CASE WHEN shortPicked=1 THEN pickSeq END) AS shortPicked, "
				+ "COUNT(CASE WHEN picked=0 AND shortPicked=0 THEN pickSeq END) AS notAttempted, "
				+ "COUNT(CASE WHEN picked=1 AND shortPicked=0 THEN pickSeq END) AS numPicked "
				+ "FROM rdsPicks WHERE cartonSeq=%d AND canceled=0 ", cartonSeq);
		int numNotPicked = getMapInt(m,"notPicked");
		int shortPick = getMapInt(m,"shortPicked");
		int numPicked = getMapInt(m,"numPicked");
		int notAttempted = getMapInt(m,"notAttempted");
		if( notAttempted == 0 )
			SloaneCommonDAO.setTableTombStoneByIntId("rdsCartons", "pickAttemptedStamp", "cartonSeq", cartonSeq);
		if( numNotPicked == 0  ) {
			if( shortPick == 0 && numPicked>0) {
				trace("cartonSeq %d picked", cartonSeq);
				db.execute("UPDATE rdsCartons SET pickStamp=NOW(),pickShortStamp=NULL WHERE cartonSeq=%d ", cartonSeq);
				SloaneCommonDAO.postCartonLog(""+cartonSeq,id,"carton picked");
				SloaneCommonDAO.triggerLabelCreation(cartonSeq);
			} else if( numPicked>0 ){
				trace("cartonSeq %d picked with short", cartonSeq);
				db.execute("UPDATE rdsCartons SET pickStamp=NOW(),pickShortStamp=NOW() WHERE cartonSeq=%d ", cartonSeq);
				SloaneCommonDAO.postCartonLog(""+cartonSeq,id,"carton picked with short");
				SloaneCommonDAO.triggerLabelCreation(cartonSeq);
			} else {
				trace("cartonSeq %d has all picks canceled or short", cartonSeq);
				db.execute("UPDATE rdsCartons SET pickStamp=NOW(),pickShortStamp=NOW(),cancelStamp=NOW() WHERE cartonSeq=%d ", cartonSeq);
				SloaneCommonDAO.postCartonLog(""+cartonSeq,id,"carton canceled due to all picks short or canceled");	
				checkLineStatus(cartonSeq);
			}
		}
	}
		
	
	private void updateOrderLinePickStatus(int orderLineSeq) {
		Map<String,String> m = db.getRecordMap(
				"SELECT COUNT(CASE WHEN picked=0 THEN pickSeq END) AS notPicked, "
				+ "COUNT(CASE WHEN shortPicked=1 THEN pickSeq END) AS shortPicked, "
				+ "COUNT(CASE WHEN picked=0 AND shortPicked=0 THEN pickSeq END) AS notAttempted, "
				+ "COUNT(CASE WHEN picked=1 AND shortPicked=0 THEN pickSeq END) AS numPicked "
				+ "FROM rdsPicks WHERE orderLineSeq=%d AND canceled=0 ", orderLineSeq);
		int numNotPicked = getMapInt(m,"notPicked");
		int shortPick = getMapInt(m,"shortPicked");
		int numPicked = getMapInt(m,"numPicked");
		int notAttempted = getMapInt(m,"notAttempted");
		if( notAttempted == 0 )
			SloaneCommonDAO.setTableTombStoneByIntId("custOrderLines", "pickAttemptedStamp", "orderLineSeq", orderLineSeq);
		if( numNotPicked == 0 ) {
			if( shortPick == 0 && numPicked>0 ) {
				db.execute("UPDATE custOrderLines SET actQty=%d,status='complete',pickStamp=NOW() "
						+ "WHERE orderLineSeq=%d",numPicked,orderLineSeq);
				trace("orderLineSeq [%d] picked", orderLineSeq);
			} else if( numPicked>0 ){
				db.execute("UPDATE custOrderLines SET actQty=%d,status='short',pickStamp=NOW() "
						+ "WHERE orderLineSeq=%d",numPicked,orderLineSeq);
				trace("orderLineSeq [%d] picked with short", orderLineSeq);
			} else {
				db.execute("UPDATE custOrderLines SET status='canceled',cancelStamp=NOW() "
						+ "WHERE orderLineSeq=%d",orderLineSeq);
				trace("orderLineSeq [%d] marked as canceled", orderLineSeq);
				SloaneCommonDAO.setTableTombStoneByIntId("custOrderLines", "labelStamp", "orderLineSeq", orderLineSeq);
			}
		}
		else {
			db.execute("UPDATE custOrderLines SET actQty=%d,status='notPicked' "
					+ "WHERE orderLineSeq=%d",numPicked,orderLineSeq);
		}		
	}
	
	private void doCartonPick( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	//String orderId = jsonObject.getString("orderId");
   	trace("process carton pick message for cartonSeq %d",cartonSeq);
   	doUpdateCartonPickStatus(cartonSeq);
   	List<String> orders = getOrdersInCarton(cartonSeq);
   	for( String orderId : orders )
   		updateOrderStatus(orderId);
	}
	
	private void doCartonPack( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process carton pack message for cartonSeq %d",cartonSeq);
   	RdsCartonDAO.setTombstone(cartonSeq,"packStamp");
   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is packed");
   	//String orderId = RdsCartonDAO.getFieldValueString(cartonSeq, "orderId", "");
   	List<String> orders = getOrdersInCarton(cartonSeq);
   	for( String orderId : orders )
   		updateOrderStatus(orderId);
	}		
	
	private void doCartonLabel( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process carton label message for cartonSeq %d",cartonSeq);
   	RdsCartonDAO.setTombstone(cartonSeq,"packStamp");
   	RdsCartonDAO.setTombstone(cartonSeq,"labelStamp");
   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is labeled");
   	//String orderId = RdsCartonDAO.getFieldValueString(cartonSeq, "orderId", "");
   	checkLineStatus(cartonSeq);
   	List<String> orders = getOrdersInCarton(cartonSeq);
   	for( String orderId : orders )
   		updateOrderStatus(orderId);
	}
	
	private void checkLineStatus( int cartonSeq ) {
		List<String> orderLines = SloaneCommonDAO.getTableDistinctValueListByColumnInt("rdsPicks", "orderLineSeq", "cartonSeq", cartonSeq);
		for( String orderLineSeqStr : orderLines ) {
			int orderLineSeq = RDSUtil.stringToInt(orderLineSeqStr, -1);
			if( SloaneCommonDAO.tombStoneSetForIntIdInTable("custOrderLines", "orderLineSeq", orderLineSeq, "labelStamp") ) continue; 
			int notLabeledCarton = db.getInt(-1, "SELECT COUNT(DISTINCT p.cartonSeq) FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) "
					+ "WHERE orderLineSeq=%d AND c.labelStamp IS NULL AND c.cancelStamp IS NULL", orderLineSeq);
			int numLabeledPicks = db.getInt(0, "SELECT COUNT(*) FROM rdsPicks p JOIN rdsCartons c USING(cartonSeq) "
					+ "WHERE orderLineSeq=%d AND c.labelStamp IS NOT NULL AND c.cancelStamp IS NULL "
					+ "AND p.picked=1 AND p.shortPicked=0 AND p.canceled=0", orderLineSeq);
			db.execute("UPDATE custOrderLines SET labeledQty=%d WHERE orderLineSeq=%d", numLabeledPicks, orderLineSeq);
			if( notLabeledCarton == 0 ) {
				db.execute("UPDATE custOrderLines SET `status`=IF( actQty=0, 'canceled', 'short' ), "
						+ "pickStamp=IFNULL(pickStamp,NOW()), primaryShortStamp=IFNULL(primaryShortStamp,NOW()), "
						+ "cancelStamp=IF( actQty=0, NOW(), NULL ) WHERE orderLineSeq=%d AND `status`='notPicked'",orderLineSeq);
				SloaneCommonDAO.setTableTombStoneByIntId("custOrderLines", "labelStamp", "orderLineSeq", orderLineSeq);
			}
		}
	}
	
	private void doCartonShip( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process carton ship message for cartonSeq %d",cartonSeq);
   	RdsCartonDAO.setTombstone(cartonSeq,"shipStamp");
   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is shipped");
   	//String orderId = RdsCartonDAO.getFieldValueString(cartonSeq, "orderId", "");
   	List<String> orders = getOrdersInCarton(cartonSeq);
   	for( String orderId : orders )
   		updateOrderStatus(orderId);
	}
	
	private void doWaveComplete( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int waveSeq = jsonObject.getInt("waveSeq");
   	trace("process wave sort message for waveSeq %d",waveSeq);
   	List<String> cartons = db.getValueList(
   			"SELECT cartonSeq FROM rdsCartons JOIN custOrders USING(orderId) "
   			+ "WHERE waveSeq=%d AND pickType<>'FullCase' "
   			+ "AND rdsCartons.labelStamp IS NOT NULL AND rdsCartons.shipStamp IS NULL", waveSeq);
   	for( String carton: cartons ) {
   		int cartonSeq = RDSUtil.stringToInt(carton, -1);
   		if( cartonSeq > 0 ) {
   	   	RdsCartonDAO.setTombstone(cartonSeq,"shipStamp");
   	   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is shipped");   			
   		}
   	}
   	List<String> orders = db.getValueList("SELECT orderId FROM custOrders WHERE waveSeq=%d AND status='labeled'", waveSeq);
   	for( String orderId : orders )
   		updateOrderStatus(orderId);
	}
	
	private void doCompleteCart( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	String operator = getMapStr( message, "operator" );
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartSeq = jsonObject.getInt("cartSeq");
   	trace("process complete cart message for cartSeq %d",cartSeq);
   	db.execute("UPDATE rdsCarts SET completeStamp=NOW() WHERE cartSeq=%d", cartSeq);
   	SloaneCommonDAO.postCartLog(""+cartSeq, id, "carton is completed manually by %s",operator);
	}
	
	private void doRetryOrder( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String orderId = jsonObject.getString("orderId");
   	trace("process retry order message for orderId %s",orderId);
   	CustOrderDAO.resetOrderData(orderId);
   	SloaneCommonDAO.postOrderLog(orderId, id, "retry order");
	}
	
	private void doCancelOrder( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String orderId = jsonObject.getString("orderId");
   	trace("process cancel order message for orderId %s",orderId);
   	cancelOrder( orderId );
	}	
	
	private void cancelOrder( String orderId ) {
		Map<String,String> m = SloaneCommonDAO.getTableRowByStringId("custOrders", "orderId", orderId);
		int waveSeq = getMapInt( m, "waveSeq" );
		CustOrderDAO.setStatusAndTombStone(orderId, "canceled", "cancelStamp");
		SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "cancelStamp", "orderId", orderId);
		SloaneCommonDAO.updateRowByStringIdIntValue("rdsPicks", "canceled", 1, "orderId", orderId);
		SloaneCommonDAO.postOrderLog(orderId, id, "order is canceled");
		boolean orderCartonized = !getMapStr(m,"cartonizeStamp").isEmpty();
		if( !orderCartonized ) {
			int fileSeq = db.getInt(-1, "SELECT fileSeq FROM rdsWaves WHERE waveSeq=%d", waveSeq);
			SloaneCommonDAO.checkFileCartonized(fileSeq);
		}
		updateWaveStatus( waveSeq );
	}
	
	private void doCancelCarton( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	String orderId = jsonObject.getString("orderId");
   	trace("process cancel carton message for cartonSeq %d",cartonSeq);
   	cancelCarton( cartonSeq, orderId );
	}
	
	private void cancelCarton( int cartonSeq, String orderId ) {
		SloaneCommonDAO.setTableTombStoneByIntId("rdsCartons", "cancelStamp", "cartonSeq", cartonSeq);
		SloaneCommonDAO.updateRowByIntIdIntValue("rdsPicks", "canceled", 1, "cartonSeq", cartonSeq);
		SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is canceled");
		checkLineStatus(cartonSeq);
		updateOrderStatus(orderId);
	}
	
	
	private void doUpdateOrderLineQty( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String orderId = jsonObject.getString("orderId");
   	JSONArray changedLines = jsonObject.getJSONArray("changedLines") ;
   	trace("process update order line qty message for orderId %s",orderId);
   	boolean hasIncrements = false;
   	boolean hasCanceledLines = false;
   	HashSet<Integer> cartonsWithCanceledPicks = new HashSet<>();
   	for( int i=0 ; i<changedLines.length() ; i++) {
   		JSONObject lineObject = changedLines.getJSONObject(i);
   		int orderLineSeq = lineObject.getInt("orderLineSeq");
   		int originQty = lineObject.getInt("qty");
   		int newQty = lineObject.getInt("newQty");
   		if( newQty == originQty ) continue;
   		else if( newQty > originQty ) {
   			hasIncrements = true;
   			int incrementQty = newQty - originQty;
   			Map<String,String> lineItemMap = SloaneCommonDAO.getTableRowByIntId("custOrderLines", "orderLineSeq", orderLineSeq);
      		String sku = getMapStr(lineItemMap,"sku");
      		String uom = getMapStr(lineItemMap,"uom");
      		int shelfPackQty = getMapInt(lineItemMap,"shelfPackQty");
      		String location = getMapStr(lineItemMap,"location"); 
      		String pickType = SloaneCommonDAO.determinePickType(location);
   			String qroFlag = db.getString("", "SELECT qroFlag FROM custSkus WHERE sku='%s' AND uom='%s'", sku,uom);
   			boolean isMarkedOutSku = SloaneCommonDAO.isMarkedOutSku(sku);
   			String createShelfPackPick_cp = db.getControl("system", "createShelfPackPick", "no");
   			boolean createShelfPackPick = createShelfPackPick_cp.equalsIgnoreCase("yes") && SloaneCommonDAO.createShelfPackPick(pickType,qroFlag);
   			if( !createShelfPackPick ) {
   				for( int k=0; k<incrementQty; k++ )
   					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,uom,1,pickType,isMarkedOutSku);		
   			} else {
   				int shelfPackCount = incrementQty/shelfPackQty;
   				for( int k=0; k<shelfPackCount; k++ )
   					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,UOM_SHELFPACK,shelfPackQty,pickType,isMarkedOutSku);
   				int salesUomCount = incrementQty%shelfPackQty;
   				for( int k=0; k<salesUomCount; k++ )
   					SloaneCommonDAO.createOrderPick(orderLineSeq,orderId,sku,uom,uom,1,pickType,isMarkedOutSku);				
   			}
   		} else {
   			if( newQty == 0 ) {
   				hasCanceledLines = true;
   				db.execute("UPDATE custOrderLines SET status='canceled',cancelStamp=NOW() "
   						+ "WHERE orderLineSeq=%d",orderLineSeq);
   				SloaneCommonDAO.setTableTombStoneByIntId("custOrderLines", "labelStamp", "orderLineSeq", orderLineSeq);
   				db.execute("UPDATE rdsPicks SET picked=1,shortPicked=1,canceled=1 WHERE orderLineSeq=%d", orderLineSeq);
   				// check carton status
   				List<String> cartons = db.getValueList("SELECT DISTINCT cartonSeq FROM rdsPicks WHERE orderLineSeq=%d", orderLineSeq);
   				for( String carton_str : cartons ) {
   					int cartonSeq = RDSUtil.stringToInt(carton_str, -1);
   					doUpdateCartonPickStatus( cartonSeq );
   				}
   			} else {
   				int decrementQty = originQty - newQty;
   				notPickedOuterLoop:
   				while( decrementQty> 0 ) {
   					List<Map<String,String>> cartonsWithNotPickedLine = null;
   					if( cartonsWithCanceledPicks.isEmpty() ) {
	   					cartonsWithNotPickedLine = db.getResultMapList(
	   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
	   							+ "FROM rdsPicks WHERE orderLineSeq=%d "
	   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 GROUP BY cartonSeq "
	   							+ "ORDER BY target DESC, diff", decrementQty, orderLineSeq);
   					} else {
   						String cartonSeqListStr = buildCartonSeqListStr(cartonsWithCanceledPicks);
	   					cartonsWithNotPickedLine = db.getResultMapList(
	   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
	   							+ "FROM rdsPicks WHERE cartonSeq IN (%s) AND orderLineSeq=%d "
	   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 GROUP BY cartonSeq "
	   							+ "ORDER BY target DESC, diff", decrementQty, cartonSeqListStr, orderLineSeq);
	   					if( cartonsWithNotPickedLine==null || cartonsWithNotPickedLine.isEmpty() )
		   					cartonsWithNotPickedLine = db.getResultMapList(
		   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
		   							+ "FROM rdsPicks WHERE orderLineSeq=%d "
		   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 GROUP BY cartonSeq "
		   							+ "ORDER BY target DESC, diff", decrementQty, orderLineSeq);
   					}
   					if( cartonsWithNotPickedLine==null || cartonsWithNotPickedLine.isEmpty() ) break;
   					for( Map<String,String> m : cartonsWithNotPickedLine ) {
   						int cartonSeq = getMapInt(m,"cartonSeq");
   						int numPicks = getMapInt(m,"numPicks");
   						int requiredNum = Math.min(numPicks, decrementQty);
   						int updated = db.execute(
   								"UPDATE rdsPicks SET picked=1,shortPicked=1,canceled=1 "
   								+ "WHERE cartonSeq=%d AND orderLineSeq=%d "
   								+ "AND picked=0 AND shortPicked=0 AND canceled=0 LIMIT %d", 
   								cartonSeq, orderLineSeq,requiredNum);
   						decrementQty -= updated;
   						if( updated > 0 ) cartonsWithCanceledPicks.add(cartonSeq);
   						if( decrementQty == 0 )
   							break notPickedOuterLoop;
   					}   					
   				}
   				notLabeledOuterLoop:
   				while( decrementQty>0 ) {
   					List<Map<String,String>> cartonsWithNotLabeledLine = null;
   					if( cartonsWithCanceledPicks.isEmpty() ) {
   						cartonsWithNotLabeledLine = db.getResultMapList(
	   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
	   							+ "FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE orderLineSeq=%d "
	   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 AND labelStamp IS NULL GROUP BY cartonSeq "
	   							+ "ORDER BY target DESC, diff", decrementQty, orderLineSeq);
   					} else {
   						String cartonSeqListStr = buildCartonSeqListStr(cartonsWithCanceledPicks);
   						cartonsWithNotLabeledLine = db.getResultMapList(
	   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
	   							+ "FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE c.cartonSeq IN (%s) AND orderLineSeq=%d "
	   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 AND labelStamp IS NULL GROUP BY cartonSeq "
	   							+ "ORDER BY target DESC, diff", decrementQty, cartonSeqListStr, orderLineSeq);
	   					if( cartonsWithNotLabeledLine==null || cartonsWithNotLabeledLine.isEmpty() )
	   						cartonsWithNotLabeledLine = db.getResultMapList(
		   							"SELECT cartonSeq, COUNT(*) AS numPicks, IF(COUNT(*)>%d,1,0) AS target,ABS(COUNT(*)-5) AS diff "
		   							+ "FROM rdsPicks JOIN rdsCartons c USING(cartonSeq) WHERE orderLineSeq=%d "
		   							+ "AND picked=0 AND shortPicked=0 AND canceled=0 AND labelStamp IS NULL GROUP BY cartonSeq "
		   							+ "ORDER BY target DESC, diff", decrementQty, orderLineSeq);
   					}
   					if( cartonsWithNotLabeledLine==null || cartonsWithNotLabeledLine.isEmpty() ) break;
   					for( Map<String,String> m : cartonsWithNotLabeledLine ) {
   						int cartonSeq = getMapInt(m,"cartonSeq");
   						int numPicks = getMapInt(m,"numPicks");
   						int requiredNum = Math.min(numPicks, decrementQty);
   						int updated = db.execute(
   								"UPDATE rdsPicks SET picked=1,shortPicked=1,canceled=1 "
   								+ "WHERE cartonSeq=%d AND orderLineSeq=%d "
   								+ "AND canceled=0 LIMIT %d", 
   								cartonSeq, orderLineSeq,requiredNum);
   						decrementQty -= updated;
   						if( updated > 0 ) {
   							cartonsWithCanceledPicks.add(cartonSeq);
   							db.execute("UPDATE rdsCartons SET auditRequired=1, auditStamp=NULL WHERE cartonSeq=%d", cartonSeq);
   						}
   						if( decrementQty == 0 )
   							break notLabeledOuterLoop;
   					}
   				}
   				newQty += decrementQty;
   				for( int cartonSeq : cartonsWithCanceledPicks ) {
   					doUpdateCartonPickStatus( cartonSeq );
   				}
   				updateOrderLinePickStatus(orderLineSeq);
   			}
   		}
   		if( newQty != originQty ) {
   			db.execute("UPDATE custOrderLines SET qty=%d, qtyChanged=1 WHERE orderLineSeq=%d", newQty,orderLineSeq);
   		}
   	}
   	if( hasCanceledLines ) {
   		updateOrderStatus(orderId);
   	}
   	if( hasIncrements ) {
   		String operator = getMapStr( message, "operator" );
         JSONObject json = new JSONObject() ;
      	json.put("orderId",orderId) ;
   		SloaneCommonDAO.insertStatusMessages("carton", "cartonize", json.toString(),operator);
   	}
	}
	
	private String buildCartonSeqListStr( HashSet<Integer> set ) {
		String s = "";
		for( Integer cartonSeq : set ) {
			s += s.isEmpty()?cartonSeq:(","+cartonSeq);
		}
		return s;
	}
	
	private void doAssignLpnForGeekShortCarton( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String cartonLpn = jsonObject.getString("newCartonLpn");
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process assign lpn message for cartonSeq %d",cartonSeq);
   	Map<String,String> m = SloaneCommonDAO.getTableRowByIntId("rdsCartons", "cartonSeq", cartonSeq);
   	String currentLpn = getMapStr(m,"lpn");
   	String trackingNumber = getMapStr(m,"trackingNumber");
   	String cartonType = getMapStr(m,"cartonType");
   	if( !currentLpn.isEmpty() && !currentLpn.equals(trackingNumber) ) {
   		alert("geek short carton %d has lpn %s assigned already! Ignore new lpn %s.", cartonSeq, currentLpn, cartonLpn);
   		return;
   	}
   	String regex = db.getString("", "SELECT lpnFormat FROM cfgCartonTypes WHERE cartonType = '%s'", cartonType);
   	if (!cartonLpn.matches(regex)) {
   		alert("invalid LPN %s!", cartonLpn);
   		return;  		
   	}
   	if( cartonLpn.startsWith("TT") ) {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s' "
   				+ "AND cancelStamp IS NULL "
   				+ "AND shipStamp IS NULL "
   				+ "AND ( labelStamp IS NULL OR ( labelStamp > DATE_SUB(NOW(), INTERVAL 1 DAY) )  )", cartonLpn);
   		if( count == 0 ) {
   			SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "shipStamp", "lpn", cartonLpn);
   			db.execute("UPDATE rdsCartons SET lpn='%s', assigned = 1 WHERE cartonSeq=%d", cartonLpn, cartonSeq);
   	   	inform("lpn %s is assigned for geek short carton %d",cartonLpn,cartonSeq);
   	   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "assigned lpn %s through dashboard request",cartonLpn);
   		} else {
   			alert("LPN %s is still open, can't assign it to cartonSeq %d",cartonLpn,cartonSeq);
   		}
   	} else {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn= '%s' ", cartonLpn);
   		if( count == 0 ) {
   			db.execute("UPDATE rdsCartons SET lpn='%s', assigned = 1 WHERE cartonSeq=%d", cartonLpn, cartonSeq);
   	   	inform("lpn %s is assigned for geek short carton %d",cartonLpn,cartonSeq);
   	   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "assigned lpn %s through dashboard request",cartonLpn);
   		} else {
   			alert("LPN %s is still open, can't assign it to cartonSeq %d",cartonLpn,cartonSeq);
   		}
   	}
	}	
	
	private void doReAssignLpnNotStartedCarton( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	String cartonLpn = jsonObject.getString("newCartonLpn");
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process re-assign lpn message for cartonSeq %d",cartonSeq);
   	Map<String,String> m = SloaneCommonDAO.getTableRowByIntId("rdsCartons", "cartonSeq", cartonSeq);
   	String pickStartStamp = getMapStr(m,"pickStartStamp");
   	String currentLpn = getMapStr(m,"lpn");
   	String cartonType = getMapStr(m,"cartonType");
   	if( currentLpn.isEmpty() ) {
   		alert("carton %d is not assigned with any LPN yet! Ignore new lpn %s.", cartonSeq, cartonLpn);
   		return;  		
   	}
   	if( !pickStartStamp.isEmpty() ) {
   		alert("carton %d has started picking! Ignore new lpn %s.", cartonSeq, cartonLpn);
   		return;
   	}
   	String regex = db.getString("", "SELECT lpnFormat FROM cfgCartonTypes WHERE cartonType = '%s'", cartonType);
   	if (!cartonLpn.matches(regex)) {
   		alert("invalid LPN %s!", cartonLpn);
   		return;  		
   	}
   	
   	if( cartonLpn.startsWith("TT") ) {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s' "
   				+ "AND cancelStamp IS NULL "
   				+ "AND shipStamp IS NULL "
   				+ "AND ( labelStamp IS NULL OR ( labelStamp > DATE_SUB(NOW(), INTERVAL 1 DAY) )  )", cartonLpn);
   		if( count == 0 ) {
   			SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "shipStamp", "lpn", cartonLpn);
   			db.execute("UPDATE rdsCartons SET lpn='%s' WHERE cartonSeq=%d", cartonLpn, cartonSeq);
   	   	inform("lpn %s is reassigned for not started carton %d",cartonLpn,cartonSeq);
   	   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "reassigned lpn %s through dashboard request",cartonLpn);
   		} else {
   			alert("LPN %s is still open, can't assign it to cartonSeq %d",cartonLpn,cartonSeq);
   		}
   	} else {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn= '%s' ", cartonLpn);
   		if( count == 0 ) {
   			db.execute("UPDATE rdsCartons SET lpn='%s' WHERE cartonSeq=%d", cartonLpn, cartonSeq);
   	   	inform("lpn %s is reassigned for not started carton %d",cartonLpn,cartonSeq);
   	   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "assigned lpn %s through dashboard request",cartonLpn);
   		} else {
   			alert("LPN %s is still open, can't assign it to cartonSeq %d",cartonLpn,cartonSeq);
   		}
   	}
	}	
	
	private void doRequireAudit( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	String operator = getMapStr( message, "operator" );
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int cartonSeq = jsonObject.getInt("cartonSeq");
   	trace("process require audit message for cartonSeq %d",cartonSeq);
   	db.execute("UPDATE rdsCartons SET auditRequired=1 WHERE cartonSeq=%d", cartonSeq);
   	SloaneCommonDAO.postCartonLog(""+cartonSeq, id, "carton is marked requiring audit by %s",operator);
	}
	
	private void doUpdateToteLpn( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	trace("update tote LPN for labeled or canceled carton");
   	/*
   	List<String> cartons = db.getValueList(
   			"SELECT cartonSeq FROM rdsCartons WHERE lpn REGEXP '^TT[0-9]{7}$' "
   			+ "AND ( DATE(labelStamp)<CURDATE() OR DATE(cancelStamp)<CURDATE() )");
   	*/
   	List<String> cartons = db.getValueList(
   			"SELECT cartonSeq FROM rdsCartons WHERE LENGTH(lpn)=9 "
   			+ "AND ( DATE(labelStamp)<CURDATE() OR DATE(cancelStamp)<CURDATE() )");   	
   	for( String s : cartons ) {
   		int cartonSeq = RDSUtil.stringToInt(s, -1);
   		if( cartonSeq>0 )
   			db.execute("UPDATE rdsCartons SET lpn=CONCAT(lpn,'-',DATE_FORMAT(stamp, \"%%y%%m%%d\")) WHERE cartonSeq=%d", cartonSeq);
   	}
	}	

	private void doDelayedLogout(Map<String,String> message) {
		int seq = getMapInt(message,"seq");
		db.execute("UPDATE status SET status='done' WHERE seq=%d", seq);
		String data = getMapStr( message, "data" );
		String operator = getMapStr( message, "operator" );
		JSONObject jsonObject = new JSONObject(data);
		int sessionId = jsonObject.getInt("sessionId");
		try {
			if( lock(operator) ) {
				int lastSessionId = db.getInt(-1, "SELECT value FROM victoryParams WHERE operatorID='%s' AND name='sessionId'", operator);
				if( lastSessionId != sessionId ) {
					inform("different sessionId, ignore delay logout");
					return;
				}
				db.execute("UPDATE proOperators SET task='', area='', device='' " +
						"WHERE operatorID='%s'", operator);
				db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
						"WHERE operatorID='%s' AND endTime IS NULL", operator);
				db.execute(
						"UPDATE rdsPicks SET pickOperatorId='' " +
						"WHERE pickOperatorId='%s' AND picked=0 AND shortPicked=0 AND canceled=0",operator);
				db.execute(
						"UPDATE rdsCartons SET reservedBy='' WHERE reservedBy='%s'",operator); 
				db.execute(
						"UPDATE rdsCarts SET reservedBy='' WHERE reservedBy='%s'",operator); 
				db.execute(
					"DELETE FROM victoryParams " +
					"WHERE operatorID='" + operator + "' "
					);
			}
		}finally{
			unlock(operator);
		}
	}
	
	@Override
	protected void poll() {   
      processStatusMessages();
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
		String id = "statusApp";      
	   RDSLog.trace( "application started, id = [%s]", id );
	   StatusApp app = new StatusApp( id );
	   app.run();
	}	
	

}