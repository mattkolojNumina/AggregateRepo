/*
 * DispatchDynamoDynamo.java
 * 
 * Create and monitor missions for Addverb Dynamo AMRs 
 * 
 * for Powerstop
 * 
 * (C) 2023, NuminaGroup Inc.
 */

package dispatch;

import static rds.RDSLog.inform;
import static rds.RDSLog.trace;
import static app.Constants.*;
import java.util.* ;

import app.AppCommon;
import polling.*;
import rds.* ;

public class DispatchDynamo 
	extends AbstractPollingApp {

	public static final int TASK_STATUS_NOT_ASSIGNED = 0;
	public static final int TASK_STATUS_ACCEPTED = 1;
	public static final int TASK_STATUS_INPROGRESS = 2;
	public static final int TASK_STATUS_PICKED = 3;
	public static final int TASK_STATUS_DROPPED = 4;
	public static final int TASK_STATUS_COMPLETED = 5;
	public static final int TASK_STATUS_TASKWAIT = 6;

	public static final int SUBTASK_STATUS_NOT_ASSIGNED = 0;
	public static final int SUBTASK_STATUS_ACCEPTED = 1;
	public static final int SUBTASK_STATUS_COMPLETED = 4;

	public static final int ROBOT_STATE_MOVE = 6;
	public static final int ROBOT_STATE_LOAD = 7;
	public static final int ROBOT_STATE_UNLOAD = 8;
	public static final int ROBOT_STATE_TASKWAIT = 13;
  
   private static final String DEFAULT_ID = "dispatch";
   private int oldRobots = -1 ;
   private int oldMissions = -1 ;

   public DispatchDynamo( String id, String rdsDb ) {
   	super( id, rdsDb );
		RDSHistory.setDatabase(db);
		RDSEvent.setDatabase(db);
		RDSCounter.setDatabase(db);
		AppCommon.setDatabase(db);
   }

   @Override
   protected void poll() {
   	missionAssign() ;
   	missionCheck() ;
   	checkAmrPalletsStatus() ;
   }  
   
   private void missionAssign() {
   	assignReplen();
   	//assignPallet(PALLETTYPE_AMRBULK);
   	assignPallet(PALLETTYPE_AMROVERPACK);
   }
   
   private void assignReplen() {
   	String destacker = getEmptyDestacker();
   	if( !destacker.isEmpty() ) {
   		String palletStand = getEmptyPalletStackPalletStand();
   		if( !palletStand.isEmpty() ) {
   		    db.execute(
   		   	      "INSERT fmsMissions SET cartId='', state='replenRequest', " +
   		   	      "fromLocation='%s', toLocation='%s', " +
   		   	      "taskSeq=0", palletStand, destacker); 
   		    int taskId = db.getSequence();
   		    reserveLocation(palletStand,taskId);
   		    reserveLocation(destacker,taskId);
   		}
   	}
   }
   
   private void assignPallet(String palletType) {
   	String pickUpLocation = "";
   	if( palletType.equals(PALLETTYPE_AMRBULK) ) {
      	pickUpLocation = getAvailablePickupDestacker();
      	if( pickUpLocation.isEmpty() )
      		pickUpLocation = getAvailablePickupPalletStand();
      	if( pickUpLocation.isEmpty() )
      		return;  		
   	} else {
   		pickUpLocation = getAvailablePickupOverPack();
   	}
   	if( pickUpLocation.isEmpty() ) return;
   	Map<String,String> m = null;
   	m = getUnassignedPallet(palletType);
   	if( m!=null && !m.isEmpty() && getMapInt(m,"palletSeq")>0 ) {
   		int palletSeq = getMapInt(m,"palletSeq");
   		String orderId = getMapStr(m,"refValue");
   		String to = getNextPickWayPoint(palletSeq,orderId,palletType);
   		if( to.isEmpty() ) return;
   		db.execute(
   				"INSERT fmsMissions SET cartId='%s', state='loadRequest', " +
	   	      "fromLocation='%s', toLocation='%s', " +
	   	      "taskSeq=-1", palletSeq, pickUpLocation, to       
   	    ); 
   		int taskId = db.getSequence();
   		db.execute("UPDATE rdsPallets SET taskId=%d, status='pickingMissionCreated' "
   				+ "WHERE palletSeq=%d", taskId,palletSeq);
   		reserveLocation(pickUpLocation,taskId);
   	}
   }
   
	private void checkAmrPalletsStatus() {
		List<Map<String,String>> pallets = db.getResultMapList(
				"SELECT * FROM rdsPallets WHERE taskId>0");
		for( Map<String,String> pallet : pallets ) {
			String status = getMapStr(pallet,"status");
			switch( status ) {
			case "pickingMissionCreated":
				checkPickingMissionCreatedPallet(pallet);break;
			case "movingToPickLocation":
				checkMovingToPickLocationPallet(pallet);break;
			case "picking":
				checkPickingPallet(pallet);break;
			case "movingToDropOffLocation":
				checkMovingToDropOffLocationPallet(pallet);break;
			default: break;
			}
		}
	} 
	
	private void checkPickingMissionCreatedPallet( Map<String,String> pallet ) {
		int palletSeq = getMapInt(pallet,"palletSeq");
		int taskId = getMapInt(pallet,"taskId");
		Map<String,String> amr = db.getRecordMap(
				"SELECT state, t.robotId FROM fmsMissions m "
				+ "LEFT JOIN fmsTasks t ON m.taskSeq=t.seq WHERE m.taskId=%d", taskId);
		String state = getMapStr( amr, "state" );
		String robotId = getMapStr( amr, "robotId" );
		String fromLocation = getMapStr( amr, "fromLocation" );
		if( !state.isEmpty() && (state.contains("move") || state.contains("via")) && !robotId.isEmpty()) {
			db.execute("UPDATE rdsPallets SET robotId='%s', status='movingToPickLocation' WHERE palletSeq=%d", robotId, palletSeq);
			db.execute("UPDATE rdsLocations SET assignmentValue='' WHERE location='%s' AND assignmentValue='singlePallet'", fromLocation);
			inform("set palletSeq %d (%s) status to [movingToPickLocation]",palletSeq);
		}  
	}	
	
	private void checkMovingToPickLocationPallet( Map<String,String> pallet ) {
		int palletSeq = getMapInt(pallet,"palletSeq");
		int taskId = getMapInt(pallet,"taskId");
		Map<String,String> amr = db.getRecordMap(
				"SELECT state, sourceLocation FROM fmsMissions m "
				+ "LEFT JOIN fmsTasks t ON m.taskSeq=t.seq WHERE m.taskId=%d", taskId);
		String state = getMapStr( amr, "state" );
		String sourceLocation = getMapStr( amr, "sourceLocation" );
		if( state.equals("waiting") ) {
			db.execute("UPDATE rdsPallets SET status='picking' WHERE palletSeq=%d", palletSeq);	
			setPalletLocation( sourceLocation, palletSeq );
			inform("set palletSeq %d status to [picking]",palletSeq);
		}  
	}
	
	private void checkPickingPallet( Map<String,String> pallet ) {
		int palletSeq = getMapInt(pallet,"palletSeq");
		int taskId = getMapInt(pallet,"taskId");
		String currentWayPoint = getMapStr(pallet,"lastPositionLogical");
		String palletType = getMapStr(pallet,"palletType");
		String orderId = getMapStr(pallet,"refValue");
		int numOfRemainingPicks = getRemainingPicks(palletSeq,orderId,palletType,currentWayPoint);
		if( numOfRemainingPicks == 0 ) {
			String nextWayPoint = getNextPickWayPoint( palletSeq,orderId,palletType );
			if( !nextWayPoint.isEmpty() ) {
				updateFMSMission( currentWayPoint, nextWayPoint, "moveRequest", taskId);
				db.execute("UPDATE rdsPallets SET status='movingToPickLocation' WHERE palletSeq=%d", palletSeq);
				inform("set palletSeq %d status to [movingToPickLocation]",palletSeq);
			} else {
				exitPickLanes( palletSeq, orderId, currentWayPoint, palletType, taskId );
			}
		}  
	}
	
	private void exitPickLanes( int palletSeq, String orderId, String currentWayPoint, String palletType, int taskId ) {
		if( palletType.equals(PALLETTYPE_AMRBULK) ) {
	   	String dropoffLocation = db.getString("", "SELECT location FROM rdsLocations "
	   			+ "WHERE locationType='singlePallet' AND assignmentValue='%s' LIMIT 1", orderId);
	   	if( dropoffLocation.isEmpty() )
	   		dropoffLocation = db.getString("", "SELECT a.location FROM rdsLocations a"
	      			+ "JOIN rdsLocations b ON a.area=b.location "
	      			+ "WHERE a.locationType='palletStand' AND a.assignmentValue = '' "
	      			+ "AND b.locationType='consolidateCell' AND b.assignmentValue = '%s' "
	      			+ "ORDER BY IF(a.robotId='',1,0) DESC, a.lastAssigned LIMIT 1", orderId);
	   	updateFMSMission( currentWayPoint, dropoffLocation, "unloadRequest", taskId);
	   	db.execute("UPDATE rdsPallets SET status='movingToDropOffLocation' WHERE palletSeq=%d",palletSeq);
	   	reserveLocation(dropoffLocation,taskId);
	   	inform("set palletSeq %d status to [movingToDropOffLocation]",palletSeq);
		} else {
	   	String dropoffLocation = db.getString("", "SELECT location FROM rdsLocations "
	   			+ "WHERE locationType='overPackStation' AND assignmentValue='' "
	   			+ "ORDER BY IF(robotId='',1,0) DESC, lastAssigned LIMIT 1");	
	   	updateFMSMission( currentWayPoint, dropoffLocation, "unloadRequest", taskId);
	   	db.execute("UPDATE rdsPallets SET status='movingToDropOffLocation' WHERE palletSeq=%d",palletSeq);
	   	reserveLocation(dropoffLocation,taskId);
	   	inform("set palletSeq %d status to [movingToDropOffLocation]",palletSeq);	   	
		}
	}	
	
	private void checkMovingToDropOffLocationPallet( Map<String,String> pallet ) {
		int palletSeq = getMapInt(pallet,"palletSeq");
		int taskId = getMapInt(pallet,"taskId");
		Map<String,String> amr = db.getRecordMap(
				"SELECT state, sourceLocation FROM fmsMissions m "
				+ "LEFT JOIN fmsTasks t ON m.taskSeq=t.seq WHERE m.taskId=%d", taskId);
		String state = getMapStr( amr, "state" );
		String sourceLocation = getMapStr( amr, "sourceLocation" );
		if( state.equals("complete") ) {
			db.execute("UPDATE rdsPallets SET status='amrComplete' WHERE palletSeq=%d", palletSeq);	
			setPalletLocation( sourceLocation, palletSeq );
			setLocationAssignmentValue( sourceLocation, palletSeq );
			inform("set palletSeq %d status to [amrComplete]",palletSeq);
		}	  
	}
   
   private String getNextPickWayPoint(int palletSeq,String orderId, String palletType) {
   	if( palletType.equals(PALLETTYPE_AMRBULK) ) {
   		return db.getString("", 
  	          "SELECT amrWayPoint FROM rdsLocations " +
 	          "JOIN custOrderLines USING (location) " +
 	          "JOIN rdsPicks USING (orderLineSeq) " +
 	          "WHERE rdsPicks.orderId='%s' AND locationType = 'pickLocation' " +
 	          "AND picked=0 AND shortPicked=0 AND canceled=0 " +
 	          "AND rdsLocations.enabled = 1 " + 
 	          "ORDER BY enabled DESC, walkSequence LIMIT 1", orderId);
   	} else {
  	    return db.getString("",
	          "SELECT amrWayPoint FROM rdsLocations " +
	          "JOIN custOrderLines USING (location) " +
	          "JOIN rdsPicks USING (orderLineSeq) " +
	          "JOIN rdsCartons USING (cartonSeq) " +
	          "WHERE palletSeq=%d AND locationType = 'pickLocation' " +
	          "AND picked=0 AND shortPicked=0 AND canceled=0 " +
	          "AND rdsLocations.enabled = 1 " + 
	          "ORDER BY enabled DESC, walkSequence LIMIT 1", palletSeq);	   		
   	}
   }
   
	private int getRemainingPicks( int palletSeq,String orderId, String palletType, String currentWayPoint ) {
		if( palletType.equals(PALLETTYPE_AMRBULK) ) {
			return db.getInt(1, 
					"SELECT COUNT(pickSeq) FROM rdsPicks p "
					+ "JOIN custOrderLines USING(orderLineSeq) "
					+ "JOIN rdsCartons USING(cartonSeq) "
					+ "JOIN rdsLocations USING(location) "
					+ "WHERE p.orderId='%s' "
					+ "AND amrWayPoint='%s' "
					+ "AND picked=0 AND shortPicked=0 AND canceled=0 ", orderId, currentWayPoint);			
		} else {
			return db.getInt(1, 
					"SELECT COUNT(pickSeq) FROM rdsPicks p "
					+ "JOIN custOrderLines USING(orderLineSeq) "
					+ "JOIN rdsCartons USING(cartonSeq) "
					+ "JOIN rdsLocations USING(location) "
					+ "WHERE palletSeq=%d "
					+ "AND amrWayPoint='%s' "
					+ "AND picked=0 AND shortPicked=0 AND canceled=0 ", palletSeq, currentWayPoint);
		}
	}   
   
   protected void setPalletLocation(String location, int palletSeq) {
   	db.execute("UPDATE rdsPallets SET lastPositionLogical='%s' WHERE palletSeq=%d", 
   			location, palletSeq);
   }
   
   protected void setLocationAssignmentValue(String location, int palletSeq) {
   	db.execute("UPDATE rdsLocations SET assignmentValue='%d' WHERE location='%s'", 
   			palletSeq,location);  	
   }
   
	private void updateFMSMission( String fromWayPoint, String toWayPoint, 
			String requestType, int taskId) {
		trace("update dynamo mission: type [%s], from [%s], to [%s] updated for taskId [%d] ",
				requestType,fromWayPoint, toWayPoint, taskId);
		db.execute(
				"UPDATE fmsMissions SET state='%s', " +
   	   	"fromLocation='%s', toLocation='%s', " +
   	   	"taskSeq=-1 WHERE taskId='%d'", requestType, fromWayPoint, toWayPoint,taskId);
	}


   private void missionCheck() {
   	
   	List<Map<String,String>> missions
      	= db.getResultMapList("SELECT * FROM fmsMissions "
      								+"WHERE active='yes' ORDER BY taskSeq") ;
   	for(Map<String,String> mission : missions)
   		missionStep(mission) ;
 	}

   private void missionStep(Map<String,String> mission) {
   	String state = getMapStr(mission,"state") ;
   	int taskId = getMapInt(mission,"taskId") ;
   	int taskSeq = getMapInt(mission,"taskSeq") ;
   	int cartId = getMapInt(mission,"cartId") ;
		String toLocation = getMapStr(mission,"toLocation");
		String fromLocation = getMapStr(mission,"fromLocation");
   	
   	switch(state) {
	   	case "replenRequest":{
	   		if(!noRobotsAvailable()) {
	   			createTask(taskId,fromLocation,"LOAD");
	   			updateMission(taskId, "replenQueued");
	   		}
	   	}
   		break;
   		
	   	case "replenQueued":{
	   		if(taskSent(taskSeq)) 
	   			updateMission(taskId, "replenSent");  
	   	}
   	   break;
   	   
      	case "replenSent": {
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if(subTaskStatus>0) updateMission(taskId, "replenAccepted");
      	}
      	break ;  
      	
      	case "replenAccepted": {
      		String robotId
      			= db.getValue("SELECT robotId FROM fmsTasks "
                            +"WHERE seq="+taskSeq+" ","") ;
      		if(!robotId.equals("")) updateMission(taskId, "replenAssigned");
      	}
      	break ;      
      	
      	case "replenAssigned": {
      		int robotState
      			= db.getIntValue("SELECT robotState FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int taskStatus
               = db.getIntValue("SELECT taskStatus FROM fmsTasks "
                          		 +"WHERE seq="+taskSeq+" ",0) ;
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
      								 +"WHERE seq="+taskSeq+" ",0) ;
      		if((robotState==12)&&(taskStatus==2)&&(subTaskStatus==3)) {
      			updateMission(taskId,"faulted");

      			String json= String.format("{\"taskId\":\"%s\"," +
      							"\"taskType\":\"LOAD\"," +
      							"\"sourceLocation\":\"%s\"}",taskId,toLocation) ;
    
      			db.execute("INSERT INTO fmsRequests " +
   								"(sent,service,request) " +
									"VALUES " +
									"('no','fms/amr/cancelTask','%s') ",json) ;
      			unreserveLocation(toLocation);
      			unreserveLocation(fromLocation);
           
      		}
      		if((robotState==ROBOT_STATE_MOVE)&&
      		   (taskStatus==TASK_STATUS_TASKWAIT)&&
               (subTaskStatus==9)) {
      			unreserveLocation(fromLocation);
      			clearLocationAssignment(fromLocation);
      			updateMission(taskId,"dropoffRequest");
      		}
      	}
      	break ;      	
   		
      	case "loadRequest": {
      		if(!noRobotsAvailable()) {
      			createTask(taskId,fromLocation,"LOAD");
      			updateMission(taskId, "loadQueued");
      		}
      	}
      	break ;
      	case "loadQueued": {
      		if(taskSent(taskSeq)) 
      			updateMission(taskId, "loadSent");
      	}
      	break ;
      
      	case "loadSent": {
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if(subTaskStatus>0) updateMission(taskId, "loadAccepted");
      	}
      	break ;

      	case "loadAccepted": {
      		String robotId
      			= db.getValue("SELECT robotId FROM fmsTasks "
                            +"WHERE seq="+taskSeq+" ","") ;
      		if(!robotId.equals("")) updateMission(taskId, "loadAssigned");
      	}
      	break ;

      	case "loadAssigned": {
      		int robotState
      			= db.getIntValue("SELECT robotState FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int taskStatus
               = db.getIntValue("SELECT taskStatus FROM fmsTasks "
                          		 +"WHERE seq="+taskSeq+" ",0) ;
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
      								 +"WHERE seq="+taskSeq+" ",0) ;
      		if((robotState==12)&&(taskStatus==2)&&(subTaskStatus==3)) {
      			updateMission(taskId,"faulted");
      			String json= String.format("{\"taskId\":\"%s\"," +
      							"\"taskType\":\"LOAD\"," +
      							"\"sourceLocation\":\"%s\"}",taskId,toLocation) ;
    
      			db.execute("INSERT INTO fmsRequests " +
   								"(sent,service,request) " +
									"VALUES " +
									"('no','fms/amr/cancelTask','%s') ",json) ;
      			unreserveLocation(fromLocation);
           
      		}
      		if((robotState==ROBOT_STATE_MOVE)&&
      		   (taskStatus==TASK_STATUS_TASKWAIT)&&
               (subTaskStatus==9)) {
      			updateMission(taskId,"moveRequest");
      			unreserveLocation(fromLocation);
      		}
      	}
      	break ;
      
      	case "moveRequest": {
      		createTask(taskId,toLocation,"MOVE");
      		updateMission(taskId, "moveQueued");
      	}
         break;
         
      	case "moveQueued": {
      		if(taskSent(taskSeq)) 
      			updateMission(taskId, "moveSent");
      	}
         break ;
         
      	case "moveSent": {
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if(subTaskStatus>0) updateMission(taskId, "moveAssigned");
      	}
      	break ;
      	
      	case "moveAssigned": {
      		int robotState
      			= db.getIntValue("SELECT robotState FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int taskStatus
      			= db.getIntValue("SELECT taskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if((robotState==ROBOT_STATE_MOVE)&&
      		   (taskStatus==TASK_STATUS_TASKWAIT)&&
               (subTaskStatus==9)) {
      			updateMission(taskId,"waiting");
      			updatePalletLocation(cartId,toLocation);
      		}
      	}
      	break ;
      	
      	case "waiting": {
      		//wait for further commands via Cart.java in victory
      	}
         break;
         
      	case "unloadRequest": {
      		createTask(taskId,toLocation,"UNLOAD");
      		updateMission(taskId, "unloadQueued");       
      	}
         break;
         
      	case "unloadQueued": {
      		String sent = db.getValue("SELECT sent FROM fmsTasks "
                                     +"WHERE seq="+taskSeq+" ","") ;
      		if(sent.equals("yes")) updateMission(taskId, "unloadSent");
      	}
      	break ;
      	
      	case "unloadSent": {
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if(subTaskStatus>0) updateMission(taskId, "unloadAssigned");
      	}
      	break ;
      
      	case "unloadAssigned": {
      		int robotState
      			= db.getIntValue("SELECT robotState FROM fmsTasks "
                          	    +"WHERE seq="+taskSeq+" ",0) ;
      		int taskStatus
      			= db.getIntValue("SELECT taskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if((robotState==ROBOT_STATE_UNLOAD)&&
               (taskStatus==TASK_STATUS_COMPLETED)&&
               (subTaskStatus==10)) {
      			completeMission(taskId,taskSeq);
      			unreserveLocation(toLocation);
      		}
      	}
      	break ;
      	
      	case "dropoffRequest": {
      		createTask(taskId,toLocation,"UNLOAD");
      		updateMission(taskId, "dropoffQueued");       
      	}
         break;
         
      	case "dropoffQueued": {
      		String sent = db.getValue("SELECT sent FROM fmsTasks "
                                     +"WHERE seq="+taskSeq+" ","") ;
      		if(sent.equals("yes")) updateMission(taskId, "dropoffSent");
      	}
      	break ;
      	
      	case "dropoffSent": {
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if(subTaskStatus>0) updateMission(taskId, "dropoffAssigned");
      	}
      	break ;
      
      	case "dropoffAssigned": {
      		int robotState
      			= db.getIntValue("SELECT robotState FROM fmsTasks "
                          	    +"WHERE seq="+taskSeq+" ",0) ;
      		int taskStatus
      			= db.getIntValue("SELECT taskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		int subTaskStatus 
      			= db.getIntValue("SELECT subTaskStatus FROM fmsTasks "
                               +"WHERE seq="+taskSeq+" ",0) ;
      		if((robotState==ROBOT_STATE_UNLOAD)&&
               (taskStatus==TASK_STATUS_COMPLETED)&&
               (subTaskStatus==10)) {
      			completeMission(taskId,taskSeq);
      			unreserveLocation(toLocation);
      			clearLocationAssignment(toLocation);
      		}
      	}
      	break ;      	
      	
      	default:
        	break;
      } 
   }
   
   private String getEmptyDestacker() {
   	return db.getString("", "SELECT a.location FROM rdsLocations a "
   			+ "JOIN rdsLocations b ON a.area=b.location "
   			+ "WHERE a.locationType='destackerDrop' AND a.robotId='' AND a.assignmentValue='empty' "
   			+ "AND b.locationType='destacker' AND b.enabled =1 "
   			+ "LIMIT 1");
   }
   
   private String getEmptyPalletStackPalletStand() {
   	return db.getString("", 
   			"SELECT a.location FROM rdsLocations a "
   			+ "WHERE locationType='palletDropoff' AND assignmentValue = 'palletStack' "
   			+ "AND robotId='' ORDER BY lastAssigned LIMIT 1");
   }
   
   private String getAvailablePickupDestacker() {
   	return db.getString("", "SELECT a.location FROM rdsLocations a "
   			+ "JOIN rdsLocations b ON a.area=b.location "
   			+ "WHERE a.locationType='destackerPickup' AND a.robotId='' AND a.assignmentValue='singlePallet' "
   			+ "AND b.locationType='destacker' AND b.enabled =1 "
   			+ "ORDER BY a.lastAssigned LIMIT 1");
   }
   
   private String getAvailablePickupPalletStand() {
   	return db.getString("", 
   			"SELECT location FROM rdsLocations "
   			+ "WHERE locationType='palletDropoff' AND assignmentValue = 'singlePallet' "
   			+ "AND robotId='' ORDER BY lastAssigned LIMIT 1");
   }
   
   private String getAvailablePickupOverPack() {
   	return db.getString("", 
   			"SELECT location FROM rdsLocations "
   			+ "WHERE locationType='overPackStation' AND assignmentValue = 'singlePallet' "
   			+ "AND robotId='' ORDER BY lastAssigned LIMIT 1");
   }   
   
   private Map<String,String> getUnassignedPallet( String palletType) {
   	return db.getRecordMap("SELECT * FROM rdsPallets WHERE palletType='%s' AND taskId=-1 LIMIT 1", palletType);
   }
   
   private void reserveLocation( String location, int taskId ) {
   	db.execute("UPDATE rdsLocations SET robotId='%d' WHERE location='%s'", taskId, location);
   }
   
   private void unreserveLocation( String location ) {
   	inform("unreserve robot task at %s",location);
   	db.execute("UPDATE rdsLocations SET robotId='' WHERE location='%s'", location);
   }
   
   private void clearLocationAssignment( String location ) {
   	db.execute("UPDATE rdsLocations SET assignmentValue='' WHERE location='%s'", location);
   }
   
   private void updatePalletLocation( int palletSeq, String location ) {
   	db.execute("UPDATE rdsPallets SET lastLocationLogical='%s' WHERE palletSeq=%d", location, palletSeq);
   }
   
   private void createTask(int taskId, String sourceLocation, String cmdType) {
   	db.execute("INSERT INTO fmsTasks SET "
					+"taskId="+taskId+", "
					+"priority=1, "
					+"sourceLocation='"+sourceLocation+"', "
					+"deadline=DATE_ADD(NOW(),INTERVAL 1 YEAR), "
					+"taskType='"+cmdType+"' ") ; 
   	int taskSeq = db.getSequence() ;
   	db.execute("UPDATE fmsMissions SET "
					+"state='loadQueued', "
					+"taskSeq="+taskSeq+" "
					+"WHERE taskId="+taskId+" ") ;
   	RDSLog.inform("taskId %d seq %d loadQueued",taskId,taskSeq) ;
   }

   private void updateMission(int taskId, String status) {
   	db.execute("UPDATE fmsMissions SET "
   				+"state='"+status+"' "
   				+"WHERE taskId="+taskId+" ") ;
   	RDSLog.inform("taskId %d %s",taskId, status) ;
   }

   private void completeMission(int taskId, int taskSeq) {
   	db.execute("UPDATE fmsMissions SET "
   				+"state='complete', "
					+"active='no' " 
					+"WHERE taskId="+taskId+" ") ;
   	RDSLog.inform("taskId %d seq %d complete",taskId,taskSeq) ;
   }
   
   private int activeRobots(){
   	int active = db.getIntValue("SELECT count(*) "
                      +"FROM fmsRobots "
                      +"WHERE isConnected='true' "
                      +"AND LEFT(currentTask,11)!='TaskBattery' "
                      +"AND DATE_ADD(stamp,INTERVAL 10 SECOND)>NOW() ",
                       0) ;
   	if(active != oldRobots) {
   		RDSLog.inform("active robots %d",active) ;
   		oldRobots = active ;
      }
   	return active ;
   }

   private int activeMissions() {
   	int active = db.getIntValue("SELECT count(*) FROM fmsMissions "
                      +"WHERE active='yes' ",999) ;
   	if(active != oldMissions) {
   		RDSLog.inform("active missions %d",active) ;
   		oldMissions = active ;
      }
   	return active ;
   }

   private boolean noRobotsAvailable() {
   	return !((activeRobots() - activeMissions()) > 0) && false;
   }
   
   private boolean taskSent(int taskSeq) {
		return db.getString("",
				  "SELECT sent FROM fmsTasks "
				 +"WHERE seq=%d",taskSeq).equals("yes") ;
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
     DispatchDynamo app = new DispatchDynamo( id, rdsDb );
     app.run();
  }  
  
}
