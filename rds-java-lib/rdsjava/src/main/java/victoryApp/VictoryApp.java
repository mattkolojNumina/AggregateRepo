package victoryApp;

import java.net.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import rds.*;

import victoryApp.gui.* ;
import static victoryApp.gui.GUIConstants.*;

/**
 * Victory voice thread application layer.
 */
public class VictoryApp extends VictoryWorker {
  protected RDSDatabase db;
  protected int ticks = 0;

  /**
   * Victory Voice thread application layer.
   * @param socket Socket connection to client
   * @param dbhost Database IP address
   * @param sessionId Session ID of the current thread
   */
  public VictoryApp(Socket socket, String dbhost, int sessionId) { 
    super(socket,dbhost,sessionId) ;
    db = new RDSDatabase(dbhost) ;
    RDSHistory.setDatabase(db) ; 
    RDSEvent.setDatabase(db) ; 
    RDSCounter.setDatabase(db) ;
    // Need to send the resourceList at start of connection, not after the client has sent first packet
    sendResourceList(resourceDir);
    screen = new ConnectScreen(this);
    clearResponse();
    screen.handleInit();
    sendScreen(screen);
    logOutput(screen.phrase);
    trace("start at [%s], sessionId [%d]",screen.getClass().getName(),this.sessionId);
  }
 
  /**
   * Function that is called when socket is disconnected. 
   * Handles cleanup and informing of last connection values from client.
   */
  @Override
  public void onDisconnect() {
    alert("victoryApp onDisconnect clean up");
    inform("Last rssi [%s] bssid [%s]", rssi, bssid);
    if(screen != null) {
      screen.handleDisconnect();
    } 
  } 

  /**
   * Gets the value of the force logoff param for current operator.
   * @return Value of force logoff.
   */
  private boolean forceLogoff() {
    return (getParam("logoff").equals("true") );
  }

  /**
   * Handles logic for what the application should check each tick cycle.
   * @return True
   */
  @Override
  public boolean onTick() {
    if(screen.getNextScreen() != null) {
      //sometimes we have a "next" screen that's the same type of screen as our current screen.
      //if that happens, there's no need to trace that we're "switching to" the same screen
      setParam("previousScreen", screen.getClass().getName());
      screen = screen.getNextScreen();
      setParam("screen", screen.getClass().getName());
      trace("switch to [%s], user hears [%s], can scan [%b], settings panel [%b], device is listening [%s]",
        screen.getClass().getName(), screen.phrase.replace(" ei ", " A "), screen.getGUIState().scanEnabled,
        screen.getGUIState().enableSettings, screen.getGUIState().voiceEnabled);
      clearResponse();
      screen.handleInit();
      sendScreen(screen);
      logOutput(screen.phrase);
      requestSettings();
      lastResponseTime = System.currentTimeMillis();
    }
    //Check if the operator has been force logged off via dashboard
    if(forceLogoff()) {
      alert( "operator forced logoff from dashboard" );
      this.screen.unreserveOperatorTasks();
      clearParams();
      logout();
      this.screen.setNextScreen(new LoginScreen(this));
      return true;
    }
    if(++ticks == ANNOUNCEMENT_REFRESH_TIME) {
      String announcement = db.getString("",
        "SELECT message FROM victoryMessages " +
        "WHERE stamp > DATE_SUB(NOW(), INTERVAL %d SECOND) " +
        "AND (toOperator = 'everyone' OR toOperator = '%s') " +
        "ORDER BY seq DESC LIMIT 1", 
        ANNOUNCEMENT_REFRESH_TIME/10, getOperatorID());
      if(exists(announcement)) {
        logOutput(announcement);
        inform("Heard announcement '%s'", announcement);
        screen.getGUIState().phrases = screen.addSSML(announcement);
        updatePhrase(screen.getGUIState().phrases);
      }
      ticks = 0;
    }
    screen.handleTick();
    GUIResponse response = nextResponse();
    if(response == null) { 
      long now = System.currentTimeMillis();
      if (now - lastResponseTime > autoLogout ) {
        if( exists(operatorID) ) {
          inform("auto logout!");
          this.screen.unreserveOperatorTasks();
          clearParams();
          this.screen.setNextScreen(new LoginScreen(this));
          logout();
        }
      }
   	  return true;
    }
    String responseSource = response.getSource();
    String text = response.getText();
    String description = "";
    int tag = response.getTag();
    boolean success = false;  
    switch(responseSource) {
      case "Voice": 
      	if( !screen.getGUIState().voiceEnabled ) {
          description = "voice responce is not expected"; 
          alert(description);
          clearResponse();
          break;
      	}
        success = screen.handleVoice(text) ; 
        if (!success) ignoreVoiceResponse(); 
        description = text; 
        break;
      case "Scan": 
        if( !screen.getGUIState().scanEnabled ) {
          description = "scan response is not expected"; 
          alert(description);
          clearResponse();
          break;
        }
        success = screen.handleScan(text); 
        if(!success) ignoreScan(); 
        description = text; 
        break;
      case "Button": screen.handleButton(tag); description = screen.getButtonText(tag); break;
      case "Text": screen.handleText(tag, text); description = text + " ('" + screen.getEntryHint(tag) + "' text box)"; break;
      case "Password": screen.handlePassword(tag, text); description = text + " ('" + screen.getPasswordHint(tag) + "' password box)"; break;
      default: description = String.format("unknown response type '%s'", responseSource); alert(description); break;
    }
    lastResponseTime = System.currentTimeMillis();
    logInput(responseSource, description);
    return true;
  }

  /**
   * Creates a log in victory interactions for what server told client.
   * @param description Description from server.
   */
  protected void logOutput(String description) {
    if(!exists(getOperatorID())) return;
    if(!exists(description)) return;
    description=description.replace("'", "\\'");
    description=description.replace(" ei ", " A ");
    description=description.replace(" dash ", "-");
    db.execute("INSERT victoryInteractions SET operatorID='%s', " + 
               "deviceID='%s', task='%s', area='%s', screen='%s', " +
               "source='device', responseType='', description='%s'",
               getOperatorID(), this.deviceID, getTask(), getArea(),
               screen.getClass().getName(), description
    );
  }

  /**
   * Creates a log in victory interations for what server received from client (GUIResponse).
   * @param responseSource Source of client GUIResponse.
   * @param description Description from client.
   */
  protected void logInput(String responseSource, String description) {
    if(!exists(getOperatorID())) return;
    if(!exists(description)) return;
    if(responseSource.equals("Voice")) description = camelCasePrettyPrint(description);
    description=description.replace("'", "\\'");
    db.execute("INSERT victoryInteractions SET operatorID='%s', " + 
               "deviceID='%s', task='%s', area='%s', screen='%s', " +
               "source='user', responseType='%s', description=\"%s\"",
               getOperatorID(), this.deviceID, getTask(), getArea(),
               screen.getClass().getName(), responseSource, description
    );
  } 

  /**
   * Creates an entry into the log table to record history of an action.
   * @param idType ID type (cartonSeq)
   * @param id ID value (12345)
   * @param code Task code (cartPicking)
   * @param message Action completed (Cart 12345 complete)
   */
  protected void recordHistory(String idType, String id, String code, String message) {
    db.execute("INSERT INTO log SET idType='%s', id='%s', code='%s', message='%s'",
               idType, id, code, message);
  }

  /**
   * Creates an entry into the log table to record history of an action.
   * @param idType ID type (e.g. cartonSeq)
   * @param id ID value (e.g. 12345)
   * @param code Task code (e.g. cartPicking)
   * @param format Formatted action completed (e.g. Cart %s complete)
   * @param args Formatted action arguments (e.g. 12345)
   */
  protected void recordHistory(String idType, String id, String code, String format, Object... args) {
    db.execute("INSERT INTO log SET idType='%s', id='%s', code='%s', message='%s'",
               idType, id, code, String.format(format, args));
  }
 
  //core backend functionality

  /**
   * Gets the task of current operator.
   * @return Task name.
   */
  protected String getTask() {
    return db.getString("", 
      "SELECT task FROM proOperators " + 
      "WHERE operatorID='%s'", getOperatorID()
    );
  }

  /**
   * Gets the area of current operator.
   * @return Area name.
   */
  protected String getArea() {
    return db.getString("", 
      "SELECT area FROM proOperators " + 
      "WHERE operatorID='%s'", getOperatorID()
    );
  }
  
  /**
   * Gets a phrase in the current operator's language preference.
   * @param token Phrase name
   * @return Translated phrase.
   */
  protected String interpretPhrase(String token) {
    String phrase = db.getString(token,
        "SELECT %s FROM victoryPhrases " +
        "WHERE phrase='%s'", getUserPreference("language"), token);
    return phrase.isEmpty()?token:phrase;
  }

  /**
   * Handles the login of an operator.
   */
  @Override
  protected String login(String text) {
    //Check if there are too many operators logged in
    if(!validLicense()) {
      alert( "no remaining licensed connections" );
      quit();
      return "";
    }
    //first, check if the user ID even exists, use prepared statement to avoid SQL injection
    String query = "SELECT operatorID FROM proOperators WHERE operatorID = ?";
    String user = null;
    try (PreparedStatement preparedStatement = db.connect().prepareStatement(query)) {
      preparedStatement.setString(1, text);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          user = resultSet.getString("operatorID");
        }
      }
    } catch (SQLException e) {
      alert("Login exception %s", e.toString());
    }

    //Check if log dumping is set. If true, ask devices to dump logs on each login.
    boolean dumpDeviceLogs = db.getControl("victory", "dumpDeviceLogs", "false").equals("true");

    if(exists(user)) {
      try {
        if(this.screen.lock(user)) {
		      //if so, let's see if they're theoretically active on victory by seeing if there are any params tied to them
		      String operatorParams = db.getString("",
		        "SELECT operatorID FROM victoryParams " +
		        "WHERE operatorID = '%s' LIMIT 1", user);
		      if (!exists(operatorParams)) {
		        trace("user %s logged in successfully", user);
		        setOperatorID(user);
		        db.execute("UPDATE proOperators SET task='', area='', device='%s' " +
		                   "WHERE operatorID='%s'", deviceID, user);
		        db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
		                   "WHERE operatorID='%s' AND endTime IS NULL", user);
		        setParam("deviceID", deviceID);
		        setParam("sessionId", sessionId+"");
		        handleDeviceParams();
            if(dumpDeviceLogs) {
              dumpLogs();
            }
		        return "LOGGING_IN";
		      }
		      else {
		        //if we saw params listed for them in victoryParams, let's see if they're active on another device,
		        //were briefly disconnected, or if we need to start them from scratch. 
		        //start by checking their last device
		        String lastDevice = db.getString("",
		          "SELECT value FROM victoryParams WHERE operatorID='%s' AND name='deviceID'", user);
		        if(deviceID.equals(lastDevice)) {
		          //if we still have a record that they were last using this device,
		          //let's see how long it's been since then
		          int timestampdiff = db.getInt(-1,
		            "SELECT TIMESTAMPDIFF(SECOND,VALUE,NOW()) " +
		            "FROM victoryParams WHERE operatorID='%s' " +
		            "AND name='lastHeartbeat'", user);
		          if(timestampdiff < Integer.parseInt(db.getControl("victory", "reconnectTime", DEFAULT_MAX_RECONNECT))) {
		            //if it's been less than... however many seconds since the last heartbeat, this is a reconnect.
		            String result = String.format("Operator %s is reconnecting after a %d second disruption", user, timestampdiff);
		            setOperatorID(user);
		            setParam("deviceID", deviceID);
		            setParam("sessionId", sessionId+"");
                //Make sure to reset proOperators incase of disconnect clearing it.
                db.execute(
                  "UPDATE proOperators SET task='', area='', device='%s' " +
                  "WHERE operatorID='%s'", deviceID, user
                );
                db.execute(
                  "UPDATE proOperatorLog SET endTime=NOW() " +
                  "WHERE operatorID='%s' AND endTime IS NULL", user
                );
                handleDeviceParams();
                if(dumpDeviceLogs) {
                  dumpLogs();
                }
		            trace(result);
		            return "RECONNECTING";
		          }
		          else {
		            //otherwise, let's just give them a fresh start
		            clearParams(user);
		            db.execute("UPDATE proOperators SET task='', area='', device='%s' " +
		                       "WHERE operatorID='%s'", deviceID, user);
		            db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
		                       "WHERE operatorID='%s' AND endTime IS NULL", user);
		            trace("user %s logged in successfully", user);
		            setOperatorID(user);
		            setParam("deviceID", deviceID);
		            setParam("sessionId", sessionId+"");
		            handleDeviceParams();
                if(dumpDeviceLogs) {
                  dumpLogs();
                }
		            return "LOGGING_IN";
		          }
		        }
		        else if (exists(lastDevice)) {
		          //otherwise, if they were last active on some other device, let's see if the user ID is still active on that device
		          //or if they are just switching devices for some reason.
		          //quickest way is to see if we're still getting heartbeats
		          int timestampdiff = db.getInt(-1,
		            "SELECT TIMESTAMPDIFF(SECOND,VALUE,NOW()) " +
		            "FROM victoryParams WHERE operatorID='%s' " +
		            "AND name='lastHeartbeat'", user);
		          //we get heartbeats every second. let's arbitrarily say that if we got a heartbeat in the last minute,
		          //we'll assume the user ID is still in use on another device
		          if(timestampdiff < 60) {
		            String result = String.format("* .. operator %s is already logged in on another device", user);
		            trace(result);
		            return "ALREADY_LOGGED_IN";
		          }
		          else {
		            //in this case, they're probably just hopping over to this device.
		            //let's stop any sort of time logging that may have been lingering from the last device (just a precaution),
		            //and log them into this device
		            db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
		                       "WHERE operatorID='%s' AND endTime IS NULL", user);
		            trace("user %s logged in successfully", user);
		            setOperatorID(user);
		            setParam("deviceID", deviceID);
		            setParam("sessionId", sessionId+"");
		            handleDeviceParams();
                if(dumpDeviceLogs) {
                  dumpLogs();
                }
		            return "LOGGING_IN";
		          }
		        }
		        else {
		          //at this point, we have params for the operator, we don't know what device they were last using,
		          //we can't say whether this is a reconnect, or if they were in the middle of something... I think 
		          //we just have to clear out the old data and restart them from scratch
		          //proOperators must be incorrect somehow. Let's clear out any old params and let them log back in
		          clearParams(user);
		          db.execute("UPDATE proOperators SET task='', area='', device='%s' " +
		                     "WHERE operatorID='%s'", deviceID, user);
		          db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
		                     "WHERE operatorID='%s' AND endTime IS NULL", user);
		          trace("user %s logged in successfully", user);
		          setOperatorID(user);
		          setParam("deviceID", deviceID);
		          setParam("sessionId", sessionId+"");
		          handleDeviceParams();
              if(dumpDeviceLogs) {
                dumpLogs();
              }
		          return "LOGGING_IN";
		        }
		      }
        } else {
          return "LOCK_ERROR";
        }
      } finally{
        this.screen.unlock( user );
      }
    }
    else {
      //we checked the operator ID, but the operator ID is not valid
      String result = String.format("* .. %s is not a valid operator I D", text);
      trace(result);
      return "INVALID_OPERATOR";
    }
  }

  /**
   * Handles logout of operator.
   */
  @Override
  protected void logout() {
    //stop productivity tracking for this user.
	  inform("VictoryApp logout is called");
    String user = getOperatorID();
    db.execute("UPDATE proOperators SET task='', area='', device='' " +
               "WHERE operatorID='%s'", user);
    db.execute("UPDATE proOperatorLog SET endTime=NOW() " +
               "WHERE operatorID='%s' AND endTime IS NULL", user);
    //this logout could be the result of a disconnect. a 'dirty' logout if you will.
    setOperatorID(null);
    //Optionally close the app on logout, otherwise return to login screen
    if(db.getControl("victory", "quitOnLogoff", "false").equals("true")) {
      quit();
    }
  } 

  /**
   * Gets the prefix phrase for current operator.
   * @return Prefix phrase.
   */
  public String getPrefixPhrase() {
    return getParam("prefix");
  }

  /**
   * Prefix gets calculated after screen change occurs. Consequently,
   * we may have cleared some params we need in order to produce a certain phrase
   * before we get to the param substitution (e.g. clearing an lpn param after finishing a carton,
   * but you want to say "carton <lpn> complete".
   * @param token Prefix phrase.
   */
  public void setPrefixPhrase(String token) {
    if(screen == null) return;
    String phrase = interpretPhrase(token);
    setParam("prefix", screen.replaceParams(phrase,true).replace("'","\\'"));
  }

  /**
   * Adds to the end of the current prefix phrase.
   * @param token Prefix phrase.
   */
  public void appendPrefixPhrase(String token) {
    if(screen == null) return;
    setParam("prefix",         
      getParam("prefix") + " .. " + 
      screen.replaceParams(interpretPhrase(token),
           true
      )
    );
  }

  /**
   * Adds to the beginning of the current prefix phrase.
   * @param token Prefix phrase.
   */
  public void prependPrefixPhrase(String token) {
    if(screen == null) return;
    setParam("prefix",         
      screen.replaceParams(interpretPhrase(token),
           true
      )
      + " .. " + getParam("prefix"));
  }

  /**
   * Creates an entry in proTracker to record the completion of an action (productivity tracking).
   * @param operation Operation name.
   * @param qty Qty operator performed.
   */
  protected void recordAction(String operation, int qty) {
    if(!exists(getOperatorID())) return;
    db.execute(
      "INSERT proTracker SET task='%s', area='%s', " +
      "operatorID='%s', operation='%s', value=%d, stamp=NOW()",
      getParam("task"), (exists(getParam("area")) ? getParam("area") : getParam("task")),
      getOperatorID(), operation, qty
    );
  }

  /**
   * Creates an entry in proTracker to record the completion of an action (productivity tracking).
   * @param operation Operation name.
   * @param qty Qty operator performed.
   */
  protected void recordAction(String operation, double qty) {
    if(!exists(getOperatorID())) return;
    db.execute(
      "INSERT proTracker SET task='%s', area='%s', " +
      "operatorID='%s', operation='%s', value=%.1f, stamp=NOW()",
      getParam("task"), (exists(getParam("area")) ? getParam("area") : getParam("task")),
      getOperatorID(), operation, qty
    );
  }

  /** Ignores received voice response from client and requests client to listen again. */
  protected void ignoreVoiceResponse() {
    clearResponse();
    requestVoiceResponse();
  }

  /** Ignores received scan response from client and requests client to trigger scanner again. */
  protected void ignoreScan() {
    clearResponse();
    requestScan();
  }

}
