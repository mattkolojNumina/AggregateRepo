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


import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONObject;

import polling.*;
import rds.*;
import dao.SloaneCommonDAO;
import dao.RdsWaveDAO;
import static sloane.SloaneConstants.*;

import static rds.RDSLog.*;

public class OrderReleaseApp
      extends AbstractPollingApp {

   private static final String DEFAULT_ID = "orderRelease";
   private Map<String,String> controlMap;
   
   private boolean checkZoneRouteAutoRelease = false;
   private boolean checkCartPickAutoRelease = false;
   private boolean checkGeekAutoRelease = false;
   private boolean waveCartonized = false;
   private int firstWaveSeq = -1;
   private String currentDate = "";

   public OrderReleaseApp( String id, String rdsDb ) {
      super( id, rdsDb );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		SloaneCommonDAO.setDatabase(db);
		controlMap = db.getControlMap("system");
		currentDate = getCurrentDate();
   }
   
   

   protected void poll() {
   	checkNewDay();
   	if( firstWaveSeq<0 )
   		getFirstWave();
   	if( firstWaveSeq>0 && !waveCartonized )
   		checkWaveCartonized();
   	if( firstWaveSeq>0 && waveCartonized )
   		checkAutoRelease();
   		
      processStatusMessages();
   }

   /*
    * O R D E R
    */
   
   private void checkNewDay() {
   	String date = new SimpleDateFormat("MM/dd/yy").format(new java.util.Date());
   	if( !date.equals(currentDate) ) {
   		currentDate = date;
   		checkZoneRouteAutoRelease = true;
   		checkCartPickAutoRelease = true;
   		checkGeekAutoRelease = true;
   		waveCartonized = false;
   		firstWaveSeq = -1;
   	}
   }
   
   private void getFirstWave() {
   	firstWaveSeq = db.getInt(-1, "SELECT waveSeq FROM custOrders WHERE demandDate='%s' AND dailyWaveSeq='01'", currentDate);
   	if( firstWaveSeq>0 )
   		trace("found firstWaveSeq %d for date %s",firstWaveSeq,currentDate);
   }
   
   private void checkWaveCartonized() {
   	RdsWaveDAO dao = new RdsWaveDAO(firstWaveSeq);
   	waveCartonized = !dao.getRecordMapStr("cartonizeStamp").isEmpty();
   	if( waveCartonized ) {
   		checkZoneRouteAutoRelease = dao.getRecordMapStr("zoneRouteReleaseStamp").isEmpty();
   		checkCartPickAutoRelease = dao.getRecordMapStr("cartPickReleaseStamp").isEmpty();
   		checkGeekAutoRelease = dao.getRecordMapStr("geekReleaseStamp").isEmpty();
   	}
   }
   
   
   private String getCurrentDate() {
   	return new SimpleDateFormat("MM/dd/yy").format(new java.util.Date());
   }
   
   private void checkAutoRelease() {
   	RdsWaveDAO dao = new RdsWaveDAO(firstWaveSeq);
   	if(controlParameterYes("autoRelease")) {
   		if( checkZoneRouteAutoRelease && dao.getRecordMapInt("canReleaseZoneRoute") == 1) {
   			String zoueRouteAutoReleaseTime = getMapStr( controlMap, "zoueRouteAutoReleaseTime" );
   			String currentTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
   			if( currentTime.compareTo(zoueRouteAutoReleaseTime) >=0 ) {
   				doZoneRouteRelease(firstWaveSeq);
   				checkZoneRouteAutoRelease = false;
   			}
   		}
   		if( checkCartPickAutoRelease && dao.getRecordMapInt("canReleaseCartPick") == 1) {
   			String cartPickAutoReleaseTime = getMapStr( controlMap, "cartPickAutoReleaseTime" );
   			String currentTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
   			if( currentTime.compareTo(cartPickAutoReleaseTime) >=0 ) {
   				doCartPickRelease(firstWaveSeq);
   				checkCartPickAutoRelease = false;
   			}
   		}
   		if( checkGeekAutoRelease && dao.getRecordMapInt("canReleaseGeek") == 1) {
   			String geekAutoReleaseTime = getMapStr( controlMap, "geekAutoReleaseTime" );
   			String currentTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
   			if( currentTime.compareTo(geekAutoReleaseTime) >=0 ) {
   				doGeekRelease(firstWaveSeq);
   				checkGeekAutoRelease = false;
   			}
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
	   	case "zoneRouteRelease":
	   		zoneRouteRelease(message);
	   		break;
	   	case "cartPickRelease":
	   		cartPickRelease(message);
	   		break;
	   	case "geekRelease":
	   		geekRelease(message);
	   	}
	   }
   }
   
   private void zoneRouteRelease( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int waveSeq = jsonObject.getInt("waveSeq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	trace(" release zoneRoute picks for waveSeq %d",waveSeq);
   	doZoneRouteRelease( waveSeq );
   }
   
   private void doZoneRouteRelease( int waveSeq ) {
   	List<String> orders = db.getValueList(
   			"SELECT orderId FROM custOrders "
   			+ "WHERE waveSeq=%d AND cartonizeStamp IS NOT NULL", waveSeq);
   	for( String orderId : orders ) {
   		db.execute("UPDATE custOrders SET releaseStamp=IFNULL(releaseStamp,NOW()) WHERE orderId='%s'", orderId);
   		SloaneCommonDAO.postOrderLog(orderId, id, "zoneRoute picks is released for picking");
   		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_ZONEROUTE );
   	}
   	SloaneCommonDAO.postWaveLog(waveSeq+"", id, "zoneRoute picks is released for picking");
   	RdsWaveDAO.setTombstone(waveSeq, "zoneRouteReleaseStamp");
   	RdsWaveDAO.updateNextWaveCanReleaseFlag(waveSeq, "zoneRoute");
   }
   
   private void cartPickRelease( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int waveSeq = jsonObject.getInt("waveSeq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	trace(" release cartPick picks for waveSeq %d",waveSeq);
   	doCartPickRelease( waveSeq );   	
   }
   
   private void doCartPickRelease( int waveSeq ) {
   	List<String> orders = db.getValueList(
   			"SELECT orderId FROM custOrders "
   			+ "WHERE waveSeq=%d AND cartonizeStamp IS NOT NULL", waveSeq);
   	for( String orderId : orders ) {
   		db.execute("UPDATE custOrders SET releaseStamp=IFNULL(releaseStamp,NOW()) WHERE orderId='%s'", orderId);
   		SloaneCommonDAO.postOrderLog(orderId, id, "cartPick picks is released for picking");
   		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_AERSOLBOOM );
   		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_LIQUIDS );
   		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_PERISHABLES );
   	}
   	SloaneCommonDAO.postWaveLog(waveSeq+"", id, "cartPick picks is released for picking");
   	RdsWaveDAO.setTombstone(waveSeq, "cartPickReleaseStamp");
   	RdsWaveDAO.updateNextWaveCanReleaseFlag(waveSeq, "cartPick");
   }
   
   private void geekRelease( Map<String,String> message ) {
   	int seq = getMapInt(message,"seq");
   	String data = getMapStr( message, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	int waveSeq = jsonObject.getInt("waveSeq");
   	SloaneCommonDAO.setStatusMessageDone(seq);
   	trace(" release geek picks for waveSeq %d",waveSeq);
   	doGeekRelease( waveSeq );
   }
   
   private void doGeekRelease( int waveSeq ) {
   	if( SloaneCommonDAO.tombStoneSetForIntIdInTable("rdsWaves", "waveSeq", waveSeq, "geekReleaseStamp") ) {
   		alert("geek picks has been released for waveSeq %d already, ignore duplicate release request.", waveSeq);
   		return;
   	}
   	List<String> orders = db.getValueList(
   			"SELECT orderId FROM custOrders "
   			+ "WHERE waveSeq=%d AND cartonizeStamp IS NOT NULL", waveSeq);
   	for( String orderId : orders ) {
   		db.execute("UPDATE custOrders SET releaseStamp=IFNULL(releaseStamp,NOW()) WHERE orderId='%s'", orderId);
   		SloaneCommonDAO.postOrderLog(orderId, id, "geek picks is released for picking");
   		SloaneCommonDAO.releaseOrderPicksByPickType( orderId, PICKTYPE_GEEK );
   	}
   	SloaneCommonDAO.postWaveLog(waveSeq+"", id, "geek picks is released for picking");
   	RdsWaveDAO.setTombstone(waveSeq, "geekReleaseStamp");
   	RdsWaveDAO.updateNextWaveCanReleaseFlag(waveSeq, "geek");
   }   
   
	

   
   /*
    * --- utility methods
    */
  
   private boolean controlParameterYes(String name) {
   	return getMapStr(controlMap,name).equals("yes");
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
      OrderReleaseApp app = new OrderReleaseApp( id, rdsDb );
      app.run();
   }

}
