package victoryApp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.google.gson.* ;
import com.google.gson.stream.JsonReader;
import rds.*;

import victoryApp.gui.* ;
import static victoryApp.gui.GUIConstants.*;

public abstract class Screen {

  protected VictoryApp app;
  protected RDSDatabase db;

  protected long start;
  protected String phrase;

  private Screen nextScreen;
  private GUIState state;
  protected GUIText title;

  protected static final int LOCK_TIMEOUT   = 10;  // seconds

  protected long lastPress;
  protected int lastPressedButtonTag;

  protected boolean vibrateOnError;
  protected boolean hideMessage;

  protected String lastResponse;

  public Screen(VictoryApp app) {
    this.app = app;
    this.app.ticks = 0;
    this.db = app.db;
    this.start = System.currentTimeMillis();
    this.state = new GUIState();
    this.state.backGroundColor = DEFAULT_BACKGROUND_COLOR;
    this.state.scanEnabled = false;
    this.state.voiceEnabled = false;
    this.state.enableSettings = true;
    this.nextScreen = null;
    this.lastPress = 0;
    this.lastPressedButtonTag = -1;
    this.vibrateOnError = true;

    //Have to set the VictoryPhrases here since Footer does not have access to Screen.java
    state.getFooter().operatorInfo.text = interpretPhrase("FOOTER_OPERATOR");
    lastResponse = interpretPhrase("FOOTER_LAST_RESPONSE");
    state.getFooter().lastResponseInfo.text = lastResponse;
    state.getFooter().message.text = interpretPhrase("FOOTER_MESSAGE");
    state.getFooter().logout.text = interpretPhrase("FOOTER_LOGOUT");
    state.getFooter().changeArea.text = interpretPhrase("FOOTER_AREA");
    state.getFooter().changeTask.text = interpretPhrase("FOOTER_TASK");
    
    //Set or clear lastResponse. Needs to be set after updating text values above to prevent a flashing effect
    if(!exists(app.lastResponse)) {
      app.lastResponse="";
    }
    state.getFooter().setLastResponse(app.lastResponse);
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);

    //Allow the toggling of the send message button/feature
    hideMessage = false;
    if(db.getControl("victory", "showMessageButton", "true").equals("false")) {
      this.getGUIState().getFooter().hideMessageButton();
      hideMessage = true;
    }

    if(exists(getOperatorID())) {
      state.getFooter().setOperatorID(getOperatorID());
      //logout button is visible by default 
    }
    else {
      state.getFooter().clearOperatorID();
      this.getGUIState().getFooter().hideLogoutButton();
      this.getGUIState().getFooter().hideMessageButton();
      this.getGUIState().getFooter().hideChangeTaskButton();
      this.getGUIState().getFooter().hideChangeAreaButton();
    }

    //if the user's preferred language is specified in a table, use it.
    //otherwise, go with English
    String languagePreference = app.getUserPreference("language");
    if(!exists(languagePreference)) languagePreference="english";
    //get any relevant data for this screen class from the db
    Map<String,String> settings = getScreenInfo();
    //if we don't have any known settings for the screen in the database,
    //the handheld won't say anything, won't listen for anything, and won't allow scanning
    if ((settings != null) && (!settings.isEmpty())) {
      //say "(prefix message) .. (regular message)"
      phrase = (exists(getPrefixPhrase()) ? getPrefixPhrase() + " .. " : "");
      phrase = phrase + replaceParams(interpretPhrase(settings.get("phrase")), true);
      title = addTitleText(replaceParams(interpretPhrase(settings.get("titleText")
      ), false).toUpperCase());
      if(exists(phrase)) {
        phrase = replaceParams(phrase,true);
        addPhrase(phrase);
      }
      if(exists(settings.get("grammar"))) {
        //victoryScreens.grammar is a 'char' field in SQL;
        //use try-catch in case someone puts non-numeric text here
        //(e.g. 'none')
        try {
          int i = Integer.parseInt(settings.get("grammar"));
          addGrammar(i);
          this.state.voiceEnabled = true;
        } catch (Exception ex) {
        }
      }
      if(settings.get("useScanner").equals("yes")) this.state.scanEnabled = true;
      if(settings.get("enableSettings").equals("no")) this.state.enableSettings = false;
    }
    else {
      alert("No params defined for %s in the victoryScreens table!", this.getClass().getName()); 
    }
    //keep this screen's prefix message from playing on the next screen unintentionally
    setPrefixPhrase("");
  }  

  public Screen(VictoryApp app, String background) {
    this(app);
    state.backGroundColor = background;
  }  

  public Screen(Screen screen) {
    this(screen.app);
  }
  
  //getters and setters

  /**
   * Gets the current GUIState object.
   * @return GUIState
   */
  public GUIState getGUIState() {
    return this.state;
  }

  /**
   * Gets the amount of time in milliseconds since start variable was set for Screen.
   * @return time in ms
   */
  public long elapsed() {
    if(start>0)
      return System.currentTimeMillis()-start ;
    return 0 ;
  }

  /**
   * Returns the next Screen.
   * @return Next Screen
   */
  public Screen getNextScreen() {
    return this.nextScreen;
  }

  /**
   * Sets the next Screen to go to
   * @param screen Next Screen
   */
  protected void setNextScreen(Screen screen) {
    this.nextScreen = screen;
  }

  /**
   * Clears the next Screen.
   */
  public void clearNextScreen() {
    this.nextScreen = null;
  }

  /**
   * Gets a specific param for current operator from db.
   * @param key Param name.
   * @return Param value.
   */
  public String getParam(String key) {
    return app.getParam(key);
  }

  /**
   * Gets a specific Integer param for current operator from db.
   * @param name Param name.
   * @return Param Integer value.
   */
  public int getIntParam(String key) {
    int value = -1;
    try {
      value = Integer.parseInt(app.getParam(key));
    } catch (NumberFormatException ex) {
      if(exists(app.getParam(key))) alert("param '%s' has a non-numeric value tied to it", key);
    } catch (Exception ex) {
    } 
    return value;
  }

  /**
   * Gets a specific Double param for current operator from db.
   * @param name Param name.
   * @return Param Double value.
   */
  public double getDblParam(String key) {
    return getDoubleParam(key);
  }

  /**
   * Gets a specific Double param for current operator from db.
   * @param name Param name.
   * @return Param Double value.
   */
  public double getDoubleParam(String key) {
    double value = -1.0;
    try {
      value = Double.parseDouble(app.getParam(key));
    } catch (NumberFormatException ex) {
      alert("param '%s' has a non-numeric value tied to it (%s)", key, app.getParam(key));
    } catch (Exception ex) {
    }
    return value;
  }

  /**
   * Gets the Screen from a param such as "previousScreen"
   * @param key Param name.
   * @return Screen
   */
  public Screen getScreenParam(String key) {
    if(exists(getParam(key))) {
      try {
        return (Screen)Class.forName(getParam(key)).getConstructor(VictoryApp.class).newInstance(app);
      } catch (Exception ex) {
        alert("could not get screen param", getParam(key));
        ex.printStackTrace();
        return null;
      }
    }
    else { 
      return null; 
    }
  }

  /**
   * Gets a Map of param values given a list of param names.
   * @param args Param names.
   * @return Map fo param name, param value.
   */
  public Map<String,String> getParams(String... args) {
    LinkedHashMap<String,String> params = new LinkedHashMap<String,String>();
    for(String arg : args) {
      params.put(arg, getParam(arg));
    }
    return params;
  }

  /**
   * Gets a list of param values from a comma-separated param.
   * @param key Param name.
   * @return List of param values.
   */
  public List<String> getListParam(String key) {
    ArrayList<String> paramList = new ArrayList<>();
    String tableEntry = getParam(key);
    String values[] = tableEntry.split(", ");
    for(String value : values) {
      paramList.add(value.replace("\"","").replace("'",""));
    }
    return paramList;
  }

  /**
   * Gets the current deviceId.
   * @return deviceId
   */
  public String getDeviceID() {
    return app.getDeviceID();
  }

  /**
   * Gets the operatorID for the session.
   * @return OperatorID
   */
  public String getOperatorID() {
    return app.getOperatorID();
  }

  /**
   * Gets the task of the current operator.
   * @return Task name.
   */
  public String getTask() {
    return app.getTask();
  }

  /**
   * Gets the area of the current operator.
   * @return Area name.
   */
  public String getArea() {
    return app.getArea();
  }

  /**
   * Sets a param to given value and debug logs the setting.
   * @param key Param name.
   * @param value Param value.
   */
  protected void setParam(String key, String value) {
    app.setParam(key, value);
    debug("set param '%s' to '%s'", key, value);
  }

  /**
   * Sets a param to given value and debug logs the setting.
   * @param key Param name.
   * @param value Param value.
   */
  protected void setParam(String key, int value) {
    app.setParam(key,value+"");
    debug("set param '%s' to %d", key, value);
  }

  /**
   * Sets a param to given value and debug logs the setting.
   * @param key Param name.
   * @param value Param value.
   */
  protected void setParam(String key, double value) {
    app.setParam(key, String.valueOf(value));
    debug("set param '%s' to %.3f", key, value);
  }

  /**
   * Sets a param to given value and debug logs the setting.
   * @param key Param name.
   * @param value Param value.
   */
  protected void setParam(String key, List<String> value) {
    String strValue = value.toString();
    strValue = strValue.substring(1, strValue.length()-1);
    setParam(key, value);
  }

  /**
   * Clears a param and debug logs the clearing.
   * @param key Param name.
   */
  protected void clearParam(String key) {
    app.clearParam(key);
    debug("cleared param '%s'", key);
  }

  /** Clears all task params, override this method in task */
  protected void clearAllParam() {
    //Overriden in Abstract Task ...
  }

  /** Gets all information from VictoryScreens table */
  protected Map<String,String> getScreenInfo() {
    return db.getRecordMap(
      "SELECT * FROM victoryScreens WHERE name='%s'",
      this.getClass().getName());
  }

  /** Gets the phrase for TTS to speak on current screen */
  protected String getScreenPhrase() {
    return db.getString("",
      "SELECT phrase FROM victoryScreens WHERE name='%s'",
      this.getClass().getName());
  }

  /**
   * Gets a phrase in the current operator's language preference.
   * @param token Phrase name.
   * @return Translated Phrase.
   */
  public String interpretPhrase(String token) {
    return app.interpretPhrase(token);
  }

  /**
   * Creates an entry into the log table to record history of an action.
   * @param idType
   * @param id
   * @param code
   * @param format
   * @param args
   */
  public void recordHistory(String idType, String id, String code, String format, Object... args) {
    app.recordHistory(idType, id, code, String.format(format, args));
  }

  /**
   * Creates an entry into the log table to record history of an action.
   * @param idType
   * @param id
   * @param code
   * @param desc
   */
  public void recordHistory(String idType, String id, String code, String desc) {
    app.recordHistory(idType, id, code, desc);
  }

  //handle_______

  //override these as necessary

  /**
   * Handles the session when the Socket disconnects.
   */
  public void handleDisconnect() {
    inform("Screen handleDisconnect called");
    String op = getOperatorID();
    if(exists(op)) {
      int thisSessionId = app.getSessionId();
      int dbSessionId = getIntParam("sessionId");
      if(dbSessionId == thisSessionId) {
        if(db.getControl("victory", "delayedLogout", "false").equals("true")) {
          app.setOperatorID(null);
          sendDelayLogoutRequest(op, thisSessionId);
        }
        else {
          app.logout();
        }
      } else {
        inform("SessionId verification failed, ignore disconnect request.");
      }
    } 
	  this.setNextScreen(new LoginScreen(app));
  } 

  /**
   * Unreserves "standard" table picks, cartons, pallets and carts.
   */
  public void unreserveOperatorTasks() {
    inform("Unreserving rdsCartons, rdsPallets, rdsCarts from operator");
    String operatorID = app.getOperatorID();
    unreserveOperatorPicks(operatorID);
    db.execute("UPDATE rdsCartons SET reservedBy='' WHERE reservedBy='%s'",operatorID); 
    db.execute("UPDATE rdsPallets SET reservedBy='' WHERE reservedBy='%s'",operatorID); 
    db.execute("UPDATE rdsCarts SET reservedBy='' WHERE reservedBy='%s'",operatorID); 
  }
 
  /**
   * Unreserves "standard" table picks.
   * @param operatorID
   */
  public void unreserveOperatorPicks(String operatorID) {
    inform("Unreserving rdsPicks from operator");
    db.execute(
      "UPDATE rdsPicks SET pickOperatorId='' " +
      "WHERE pickOperatorId='%s' AND picked=0 AND shortPicked=0 AND canceled=0",operatorID
    );
  } 

  /**
   * Base handleInit for overriding in each Screen.
   * @return false
   */
  public boolean handleInit() {
    return false;
  }

  /**
   * Base handleTick for overriding in each Screen.
   * @return false
   */
  public boolean handleTick() { 
    return false;
  }

  /**
   * Handles mapping an intent received via Voice input into a "Standard" handle__ method.
   * @param text Input intent.
   * @return Returns true if what the user said was enough for these screen. Returns false if you want them to say something else at this screen.
   */
  public boolean handleVoice(String text) {
    trace("operator [%s] said '%s'", getOperatorID(), camelCasePrettyPrint(text));
    state.getFooter().lastResponseInfo.text = lastResponse;
    this.getGUIState().getFooter().setLastResponse(camelCasePrettyPrint(text));
    app.lastResponse=camelCasePrettyPrint(text);
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);
    switch (text.toLowerCase()) {
      case "logout": handleLogoutConfirm(); return true;
      case "exception": return handleException();
      case "message": return handleMessage();
      case "cancel": return handleCancel();
      case "new": return handleNew();
      case "startnew": return handleNew();
      case "short": return handleShort();
      case "add": return handleAdd();
      case "change": return handleChange();
      case "split": return handleSplit();
      case "join": return handleJoin();
      case "location": return handleLocation();
      case "full": return handleFull();
      case "noscan": return handleNoScan();
      case "skip": return handleSkip();
      case "confirm": return handleConfirm();
      case "okay": return handleConfirm();
      case "done": return handleConfirm();
      case "undo": return handleUndo();
      case "moveCarton": return handleMoveCarton();
      case "movePallet": return handleMovePallet();
      case "close": return handleClose();
      case "reprint": return handleReprint();
      case "override": return handleOverride();
      case "changetask": return handleChangeTask();
      case "changearea": return handleChangeArea();
      case "taskone": return handleTaskOne();
      case "tasktwo": return handleTaskTwo();
      case "taskthree": return handleTaskThree();
      case "taskfour": return handleTaskFour();
      case "taskfive": return handleTaskFive();
      case "tasksix": return handleTaskSix();
      case "taskseven": return handleTaskSeven();
      case "taskeight": return handleTaskEight();
      case "tasknine": return handleTaskNine();
      case "changelocation": return handleChangeLocation();
      case "locationone": return handleLocationOne();
      case "locationtwo": return handleLocationTwo();
      case "locationthree": return handleLocationThree();
      case "locationfour": return handleLocationFour();
      case "locationfive": return handleLocationFive();
      case "locationsix": return handleLocationSix();
      case "locationseven": return handleLocationSeven();
      case "locationeight": return handleLocationEight();
      case "locationnine": return handleLocationNine();
      case "assign": return handleAssign();
      case "remove": return handleRemove();
      default: return false;
    }
  } 

  /**
   * Base handleScan. Updates last repsonse.
   * @param scan Barcode scan.
   * @return Return true if the scan was enough for this screen,  
   * false if you want another scan on this screen.
   */
  public boolean handleScan(String scan) {
    if(!exists(scan)) return false;
    trace("operator [%s] scanned [%s]",getOperatorID() , scan);
    state.getFooter().lastResponseInfo.text = lastResponse;
    this.getGUIState().getFooter().setLastResponse(scan);
    app.lastResponse=scan;
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);
    return false;
  } 

  /**
   * Base handleButton. Updates last response, and will handle "standard" buttons.
   * @param tag Button tag (id).
   * @return True if button exists, False if button does not.
   */
  public boolean handleButton(int tag) {
    long pressTime = System.currentTimeMillis();
    //Max delay of 500ms for conesutive clicks to be interpreted as a double-click on a computer.
    //Given that these buttons are a bit smaller, and harder to tap than a mouse button,
    //750ms feels like an appropriate value
    if(((pressTime - lastPress) < 750) && (lastPressedButtonTag == tag)) {
      return handleDoubleClick(tag);
    }
    lastPress = pressTime;
    lastPressedButtonTag = tag;
    state.getFooter().lastResponseInfo.text = lastResponse;
    this.getGUIState().getFooter().setLastResponse(getButtonText(tag));
    app.lastResponse=getButtonText(tag);
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);
    //tables have so many elements, it probably makes more sense (and is less error-prone)
    //to resend the screen rather then send tens or hundreds of JSON packets all at once.
    if (tag == SCROLL_UP_BUTTON) {
      inform("operator [%s] scrolled up table", getOperatorID());
      this.getGUIState().scrollUpTable();
      app.updateScreen(this);
    }
    else if (tag == SCROLL_DOWN_BUTTON) {
      inform("operator [%s] scrolled down table", getOperatorID());
      this.getGUIState().scrollDownTable();
      app.updateScreen(this);
    }
    else if (tag == PUTWALL_SCROLL_LEFT_BUTTON) {
      inform("operator [%s] scrolled left on putwall", getOperatorID());
      this.getGUIState().scrollLeftPutwall();
      app.updateScreen(this);
    }
    else if (tag == PUTWALL_SCROLL_RIGHT_BUTTON) {
      inform("operator [%s] scrolled right on putwall", getOperatorID());
      this.getGUIState().scrollRightPutwall();
      app.updateScreen(this);
    }

    trace("operator [%s] pressed button %d ('%s' button)", getOperatorID(), tag, getButtonText(tag));
    if (tag == LOGOUT_BUTTON_TAG) {
      handleLogoutConfirm();
      return true;
    }
    if (tag == SEND_MESSAGE_BUTTON_TAG) {
      handleMessage();
      return true;
    }
    switch (tag) {
      case EXCEPTION_BUTTON_TAG: return handleException();
      case CANCEL_BUTTON_TAG: return handleCancel();
      case START_NEW_BUTTON_TAG: return handleNew();
      case REPRINT_BUTTON_TAG: return handleReprint();
      case OVERRIDE_BUTTON_TAG: return handleOverride();
      case SHORT_BUTTON_TAG: return handleShort();
      case ADD_BUTTON_TAG: return handleAdd();
      case CHANGE_BUTTON_TAG: return handleChange();
      case SPLIT_BUTTON_TAG: return handleSplit();
      case JOIN_BUTTON_TAG: return handleJoin();
      case LOCATION_BUTTON_TAG: return handleLocation();
      case FULL_BUTTON_TAG: return handleFull();
      case NO_SCAN_BUTTON_TAG: return handleNoScan();
      case SKIP_BUTTON_TAG: return handleSkip();
      case CONFIRM_BUTTON_TAG: return handleConfirm();
      case UNDO_BUTTON_TAG: return handleUndo();
      case MOVE_CARTON_BUTTON_TAG: return handleMoveCarton();
      case MOVE_PALLET_BUTTON_TAG: return handleMovePallet();
      case CLOSE_BUTTON_TAG: return handleClose();
      case CHANGE_TASK_BUTTON_TAG: return handleChangeTask();
      case CHANGE_AREA_BUTTON_TAG: return handleChangeArea();
      case TASK_ONE_BUTTON_TAG: return handleTaskOne();
      case TASK_TWO_BUTTON_TAG: return handleTaskTwo();
      case TASK_THREE_BUTTON_TAG: return handleTaskThree();
      case TASK_FOUR_BUTTON_TAG: return handleTaskFour();
      case TASK_FIVE_BUTTON_TAG: return handleTaskFive();
      case TASK_SIX_BUTTON_TAG: return handleTaskSix();
      case TASK_SEVEN_BUTTON_TAG: return handleTaskSeven();
      case TASK_EIGHT_BUTTON_TAG: return handleTaskEight();
      case TASK_NINE_BUTTON_TAG: return handleTaskNine();
      case CHANGE_LOCATION_BUTTON_TAG: return handleChangeLocation();
      case LOCATION_ONE_BUTTON_TAG: return handleLocationOne();
      case LOCATION_TWO_BUTTON_TAG: return handleLocationTwo();
      case LOCATION_THREE_BUTTON_TAG: return handleLocationThree();
      case LOCATION_FOUR_BUTTON_TAG: return handleLocationFour();
      case LOCATION_FIVE_BUTTON_TAG: return handleLocationFive();
      case LOCATION_SIX_BUTTON_TAG: return handleLocationSix();
      case LOCATION_SEVEN_BUTTON_TAG: return handleLocationSeven();
      case LOCATION_EIGHT_BUTTON_TAG: return handleLocationEight();
      case LOCATION_NINE_BUTTON_TAG: return handleLocationNine();
      default: return false;
    }
  }

  /** Determines if the operator needs to confirm a logout button press or not. */
  public void handleLogoutConfirm() {
    if(db.getControl("victory", "confirmLogout", "false").equals("true")) {
      setNextScreen(new ConfirmLogoutScreen(app));
    }
    else {
      logout();
    }
  }

  /**
   * Base handleDoubleClick for when double clicking a button.
   * @param tag Button tag (id).
   * @return false
   */
  public boolean handleDoubleClick(int tag) {
    trace("operator [%s] double-clicked button %d ('%s' button)", getOperatorID(), tag, getButtonText(tag));
    return false;
  }

  /**
   * Base handleText. Updates last repsonse.
   * @param tag Text entry box tag (id).
   * @param text Text entered.
   * @return false
   */
  public boolean handleText(int tag, String text) {
    if(!exists(text)) return false;
    trace("operator [%s] typed '%s' into text field %d ('%s' text field)",getOperatorID(), text, tag, getEntryHint(tag));
    state.getFooter().lastResponseInfo.text = lastResponse;
    this.getGUIState().getFooter().setLastResponse(text);
    app.lastResponse=text;
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);
    return false;
  }

  /**
   * Base handlePassword. 
   * @param tag Password entry box tag (id).
   * @param text Password entered.
   * @return false
   */
  public boolean handlePassword(int tag, String text) {
    trace("operator [%s] typed '%s' into password field %d ('%s' password field)",getOperatorID(), text, tag, getPasswordHint(tag));
    return false;
  }

  //handle buttons that substitute for voice commands.
  //rather than overriding handleButton and having a (potentially) very large switch statement,
  //you can just override specific methods here so long as you've created a button with the 
  //appropriate tag reserved in GUIConstants.


  /**
   * Handles navigating operator to the Message Screen.
   * @return true
   */
  public boolean handleMessage(){
    if(!hideMessage) {
      this.setNextScreen(new MessageScreen(app));
    }
    return true;
  }

  /**
   * Base handleException.
   * @return false
   */
  public boolean handleException(){
    return false;
  }

  /**
   * Base handleCancel.
   * @return false
   */
  public boolean handleCancel(){
    return false;
  }

  /**
   * Base handleNew.
   * @return false
   */
  public boolean handleNew(){
    return false;
  }

  /**
   * Base handleReprint.
   * @return false
   */
  public boolean handleReprint(){
    return false;
  }

  /**
   * Base handleOverride.
   * @return false
   */
  public boolean handleOverride(){
    return false;
  }

  /**
   * Base handleShort.
   * @return false
   */
  public boolean handleShort(){
    return false;
  }

  /**
   * Base handleAdd.
   * @return false
   */
  public boolean handleAdd(){
    return false;
  }

  /**
   * Base handleChange.
   * @return false
   */
  public boolean handleChange(){
    return false;
  }

  /**
   * Base handleSplit.
   * @return false
   */
  public boolean handleSplit(){
    return false;
  }

  /**
   * Base handleJoin.
   * @return false
   */
  public boolean handleJoin(){
    return false;
  }

  /**
   * Base handleLocation.
   * @return false
   */
  public boolean handleLocation(){
    return false;
  }

  /**
   * Base handleFull.
   * @return false
   */
  public boolean handleFull(){
    return false;
  }

  /**
   * Base handleNoScan.
   * @return false
   */
  public boolean handleNoScan(){
    return false;
  }

  /**
   * Base handleSkip.
   * @return false
   */
  public boolean handleSkip(){
    return false;
  }

  /**
   * Base handleConfirm.
   * @return false
   */
  public boolean handleConfirm(){
    return false;
  }

  /**
   * Base handleUndo.
   * @return false
   */
  public boolean handleUndo(){
    return false;
  }

  /**
   * Base handleMoveCarton.
   * @return false
   */
  public boolean handleMoveCarton(){
    return false;
  }

  /**
   * Base handleMovePallet.
   * @return false
   */
  public boolean handleMovePallet(){
    return false;
  }

  /**
   * Base handleClose.
   * @return false
   */
  public boolean handleClose(){
    return false;
  }

  /**
   * Base handleChangeTask.
   * @return true
   */
  public boolean handleChangeTask(){
    this.setNextScreen(new TaskSelectScreen(app));
    return true;
  }

  /**
   * Base handleChangeTask.
   * @return true
   */
  public boolean handleChangeArea(){
    // this.setNextScreen(new AreaSelectScreen(app));
    return true;
  }

  /**
   * Base handleTaskOne.
   * @return true
   */
  public boolean handleTaskOne(){
    return false;
  }

  /**
   * Base handleTaskTwo.
   * @return true
   */
  public boolean handleTaskTwo(){
    return false;
  }

  /**
   * Base handleTaskThree.
   * @return true
   */
  public boolean handleTaskThree(){
    return false;
  }

  /**
   * Base handleTaskFour.
   * @return true
   */
  public boolean handleTaskFour(){
    return false;
  }

  /**
   * Base handleTaskFive.
   * @return true
   */
  public boolean handleTaskFive(){
    return false;
  }

  /**
   * Base handleTaskSix.
   * @return true
   */
  public boolean handleTaskSix(){
    return false;
  }

  /**
   * Base handleTaskSeven.
   * @return true
   */
  public boolean handleTaskSeven(){
    return false;
  }

  /**
   * Base handleTaskEight.
   * @return true
   */
  public boolean handleTaskEight(){
    return false;
  }

  /**
   * Base handleTaskNine.
   * @return true
   */
  public boolean handleTaskNine(){
    return false;
  }

  /**
   * Base handleChangeLocation.
   * @return true
   */
  public boolean handleChangeLocation(){
    return false;
  }

  /**
   * Base handleLocationOne.
   * @return true
   */
  public boolean handleLocationOne(){
    return false;
  }

  /**
   * Base handleLocationTwo.
   * @return true
   */
  public boolean handleLocationTwo(){
    return false;
  }

  /**
   * Base handleLocationThree.
   * @return true
   */
  public boolean handleLocationThree(){
    return false;
  }

  /**
   * Base handleLocationFour.
   * @return true
   */
  public boolean handleLocationFour(){
    return false;
  }

  /**
   * Base handleLocationFive.
   * @return true
   */
  public boolean handleLocationFive(){
    return false;
  }

  /**
   * Base handleLocationSix.
   * @return true
   */
  public boolean handleLocationSix(){
    return false;
  }

  /**
   * Base handleLocationSeven.
   * @return true
   */
  public boolean handleLocationSeven(){
    return false;
  }

  /**
   * Base handleLocationEight.
   * @return true
   */
  public boolean handleLocationEight(){
    return false;
  }

  /**
   * Base handleLocationNine.
   * @return true
   */
  public boolean handleLocationNine(){
    return false;
  }

  /**
   * Base handleAssign.
   * @return true
   */
  public boolean handleAssign(){
    return false;
  }

  /**
   * Base handleRemove.
   * @return true
   */
  public boolean handleRemove(){
    return false;
  }

  //GUI Elements

  /**
   * Adds a rectangle given an GUIRectangle object.
   * @param rectangle GUIRectangle
   * @return GUIRectangle added to GUIState.
   */
  public GUIRectangle addRectangle(GUIRectangle rectangle)
  {
    if(state.rectangles==null)
      state.rectangles = new ArrayList<GUIRectangle>();
    state.rectangles.add(rectangle);
    return rectangle;
  }

  /**
   * Creates a GUIRectangle with specified values.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param color background fill color.
   * @param borderSize border width in pixels.
   * @param borderColor border color.
   * @return GUIRectangle object.
   */
  public GUIRectangle addRectangle(Integer tag,
          Integer x, Integer y, Float z,
          Integer width, Integer height, 
          String color, int borderSize, String borderColor) {
    GUIRectangle rectangle = new GUIRectangle(tag,
                                              x,y,z,
                                              width,height,
                                              color,borderSize,borderColor) ;
    return addRectangle(rectangle);
  }

  /**
   * Deletes a GUIRectangle from screen.
   * @param rectangle GUIRectangle object to delete.
   */
  public void deleteRectangle(GUIRectangle rectangle) {
    app.deleteRectangle(rectangle);
  }

  /**
   * Adds a circle given a GUICircle object.
   * @param circle GUICircle
   * @return GUICircle added to GUIState.
   */
  public GUICircle addCircle(GUICircle circle)
  {
    if(state.circles==null)
      state.circles = new ArrayList<GUICircle>();
    state.circles.add(circle);
    return circle;
  }

  /**
   * Creates a GUICircle with specified values.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param color background fill color.
   * @param borderSize border width in pixels.
   * @param borderColor border color.
   * @return GUICircle object.
   */
  public GUICircle addCircle(Integer tag,
          Integer x, Integer y, Float z,
          Integer width, Integer height,
          String color, int borderSize, String borderColor) {
    GUICircle circle = new GUICircle(tag,
                                     x,y,z,
                                     width,height,
                                     color,borderSize,borderColor) ;
    return addCircle(circle);
  }

  /**
   * Deletes a GUICircle from screen.
   * @param circle GUICircle object to delete.
   */
  public void deleteCircle(GUICircle circle) {
    app.deleteCircle(circle);
  }

  /**
   * Adds an image to the screen given a GUIImage
   * @param image GUIImage to add to screen.
   * @return GUIImage added to GUIState.
   */
  public GUIImage addImage(GUIImage image) {
    if (state.guiImageList==null) {
      state.guiImageList = new ArrayList<GUIImage>();
    }
    state.guiImageList.add(image);
    return image;
  }

  /**
   * Adds an image to screen by url. The image will be scaled to specified height/width.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param url Image url.
   * @return GUIImage added to GUIState.
   */
  public GUIImage addImage(Integer tag,
            Integer x, Integer y, Float z,
            Integer width, Integer height, String url) {

    GUIImage image = new GUIImage(tag, x, y, z, width, height, url);
    return addImage(image);
  }

  /**
   * Deletes an Image from the screen.
   * @param image GUIImage to delete.
   */
  public void deleteImage(GUIImage image) {
    app.deleteImage(image);
  }

  /**
   * Adds a piece of text to screen given a GUIText.
   * @param guiText GUIText to add to screen.
   * @return GUIText added to GUIState.
   */
  public GUIText addText(GUIText guiText) {
    if (state.texts==null)
      state.texts = new ArrayList<GUIText>();
    state.texts.add(guiText);
    return guiText;
  }

  /**
   * Adds a text to screen at specified position and size.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param z z-index.
   * @param textSize font size.
   * @param textColor font color.
   * @param text Text to display.
   * @param bold font bold?
   * @param italic font italic?
   * @return GUIText added to GUIState.
   */
  public GUIText addText(Integer tag,
          Integer x, Integer y, Float z,
          Float textSize, String textColor, String text,
          Boolean bold, Boolean italic) {
    GUIText guiText = new GUIText(tag,
                                  x,y,z,
                                  textSize,textColor,text,
                                  bold,italic) ;
    return addText(guiText);
  }

  /**
   * Adds a text to screen at specified position.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param text Text to display.
   * @return GUIText added to GUIState.
   */
  public GUIText addText(Integer tag, Integer x, Integer y, String text) {
    return addText(tag,
            x,y,0f,
            DEFAULT_TEXT_SIZE,DEFAULT_TEXT_COLOR,text,
            DEFAULT_IS_BOLD,DEFAULT_IS_ITALIC) ;
  }

  /**
   * Adds a text to screen at specified position and size.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param text Text to display.
   * @param size font size.
   * @return GUIText added to GUIState.
   */
  public GUIText addText(Integer tag, Integer x, Integer y, String text, float size) {
    return addText(tag,
            x,y,0f,
            size,DEFAULT_TEXT_COLOR,text,
            DEFAULT_IS_BOLD,DEFAULT_IS_ITALIC) ;
  }

  /**
   * Adds a text to screen at specified position and size, and if the text should be interpreted as a phrase.
   * @param tag GUIElement tag.
   * @param x x-coord.
   * @param y y-coord.
   * @param text Text to display.
   * @param size font size.
   * @param victoryPhrase True/False if text should be interpreted as a phrase.
   * @return
   */
  public GUIText addText(Integer tag, Integer x, Integer y, String text, float size, Boolean victoryPhrase) {
    if(victoryPhrase) {
    return addText(tag,
            x,y,0f,
            size,DEFAULT_TEXT_COLOR,replaceParams(interpretPhrase(text),false),
            DEFAULT_IS_BOLD,DEFAULT_IS_ITALIC) ;
    } else {
      return addText(tag,
      x,y,0f,
      size,DEFAULT_TEXT_COLOR,text,
      DEFAULT_IS_BOLD,DEFAULT_IS_ITALIC) ;
    }
  }
  
  /**
   * Deletes a GUIText from the screen.
   * @param text GUIText to delete.
   */
  public void deleteText(GUIText text) {
    app.deleteText(text);
  }

  /**
   * Adds a title text to top of screen layout.
   * @param text Text to display.
   * @return Title text added to GUIState.
   */
  public GUIText addTitleText(String text) {
    //this formula will approximately center the text
    //between the left edge of the screen 
    //and the signal strength indicator circle
    int x = 50;
    return addText(TITLE_TEXT_TAG,
            x, 0, 0f,
            3*DEFAULT_TITLE_TEXT_SIZE/4,BLACK,text,
            true,false) ;
  }

  /**
   * Gets the alias of an rdsLocation from "standard" table.
   * @param location Location name.
   * @return Alias.
   */
  public String spokenAlias(String location) {
    String alias = db.getString(location,
      "SELECT alias FROM rdsLocations " +
      "WHERE location='%s'", location
    );
    return alias;
  }

  /**
   * Adds a phrase for TTS to speak to operator.
   * @param phrase Phrase to add.
   */
  private void addPhrase(String phrase) {
    state.phrases = addSSML(phrase) ;
  }



  /**
   * Adds a grammar object to the GUIState. Note: adding a grammar to a state 
   * before sending all the screen info inherently makes the screen start listening.
   * @param grammar Grammar file number.
   */
  public void addGrammar(int grammar) {
    /*
     * Need to get the grammar from the resource directory.
     * Victory Voice KEEN will cache grammars depending on the "key" value.
     * If updates are made to a grammar file itself, the key will need be to be changed for the grammar,
     * so devices know to redownload and recache the grammars.
     */
    String fileName = expectedGrammarFilename(grammar);
    try (FileReader fileReader = new FileReader(app.resourceDir+app.locale+"/grammars/"+fileName)) {
      JsonReader jsonReader = new JsonReader(fileReader);
      Gson gson = new Gson();
      Grammar myGrammar = gson.fromJson(jsonReader, Grammar.class);
      state.grammar = myGrammar;
      inform("use grammar [%d]", grammar);
      fileReader.close();
    }
    catch (Exception e) {
      if(e instanceof FileNotFoundException) {
        alert("Grammar file not found for grammar [%d]", grammar);
        e.printStackTrace();
      }
      else if(e instanceof IOException) {
        alert("IOException occured trying to load grammar [%d]", grammar);
        e.printStackTrace();
      }
      else {
        alert("Exception occured trying to load grammar [%d]", grammar);
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets the KEEN json filename of a grammar given it's number.
   * @param i Grammar number.
   * @return Grammar filename with extension.
   */
  public static String expectedGrammarFilename(int i) {
    return "grammar" + (i < 10 ? "0" : "") + i + ".json";
  }
  
  /**
   * Adds an exception button at specified coordinates.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return Exception button added to GUIState.
   */
  public GUIButton addExceptionButton(int x, int y) {
    return addButton(EXCEPTION_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("EXCEPTION")); //Exception
  }

  /**
   * Adds a cancel button at specified coordinates.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return Cancel button added to GUIState.
   */
  public GUIButton addCancelButton(int x, int y) {
    return addButton(CANCEL_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("CANCEL")); //Cancel
  }

  /**
  * Adds a start new button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Start New button added to GUIState.
  */
  public GUIButton addStartNewButton(int x, int y) {
    return addButton(START_NEW_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("START_PALLET")); //Start Pallet
  }

  /**
  * Adds a start button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Start button added to GUIState.
  */
  public GUIButton addStartButton(int x, int y) {
    return addButton(START_NEW_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("START")); //Start
  }

  /**
  * Adds an add pallet button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Add Pallet button added to GUIState.
  */
  public GUIButton addAddButton(int x, int y) {
    return addButton(ADD_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("ADD_PALLET")); //Add Pallet
  }

  /**
  * Adds a move carton button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Move Carton button added to GUIState.
  */
  public GUIButton addMoveCartonButton(int x, int y) {
    return addButton(MOVE_CARTON_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("MOVE_CARTON")); //Move carton
  }

  /**
  * Adds a move pallet button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Move Pallet button added to GUIState.
  */
  public GUIButton addMovePalletButton(int x, int y) {
    return addButton(MOVE_PALLET_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("MOVE_PALLET")); //Move Pallet
  }

  /**
  * Adds a split button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Split button added to GUIState.
  */
  public GUIButton addSplitButton(int x, int y) {
    return addButton(SPLIT_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("SPLIT")); //Split
  }

  /**
  * Adds a join button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Join button added to GUIState.
  */
  public GUIButton addJoinButton(int x, int y) {
    return addButton(JOIN_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("JOIN")); //Join
  }

  /**
  * Adds a full button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Full button added to GUIState.
  */
  public GUIButton addFullButton(int x, int y) {
    return addButton(FULL_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("FULL")); //Full
  }

  /**
  * Adds an undo button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Undo button added to GUIState.
  */
  public GUIButton addUndoButton(int x, int y) {
    return addButton(UNDO_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("UNDO")); //Undo
  }

  /**
  * Adds a change task button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Change Task button added to GUIState.
  */
  public GUIButton addChangeButton(int x, int y) {
    return addButton(CHANGE_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("CHANGE_TASK")); //Change Task
  }

  /**
  * Adds a change task button at specified coordinates.
  * This is a duplicate functionality of {@link #addChangeButton(int, int)}.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Change Task button added to GUIState.
  */
  public GUIButton addChangeTaskButton(int x, int y) {
    return addButton(CHANGE_TASK_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("CHANGE_TASK")); //Change Task
  }

  /**
  * Adds a change area button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Change Area button added to GUIState.
  */
  public GUIButton addChangeAreaButton(int x, int y) {
    return addButton(CHANGE_AREA_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("CHANGE_AREA")); //Change Area
  }

  /**
  * Adds a confirm button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Confirm button added to GUIState.
  */
  public GUIButton addConfirmButton(int x, int y) {
    return addButton(CONFIRM_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("CONFIRM")); //Confirm
  }

  /**
  * Adds a short button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Short button added to GUIState.
  */
  public GUIButton addShortButton(int x, int y) {
    return addButton(SHORT_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("SHORT")); //Short
  }

  /**
  * Adds a no scan button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return No Scan button added to GUIState.
  */
  public GUIButton addNoScanButton(int x, int y) {
    return addButton(NO_SCAN_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("NO_SCAN")); //No Scan
  }

  /**
  * Adds a skip button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Skip button added to GUIState.
  */
  public GUIButton addSkipButton(int x, int y) {
    return addButton(SKIP_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("SKIP")); //Skip
  }

  /**
  * Adds a change location button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Change Location button added to GUIState.
  */
  public GUIButton addChangeLocationButton(int x, int y) {
    return addButton(CHANGE_LOCATION_BUTTON_TAG,x,y,720,interpretPhrase("CHANGE_LOCATION")); //Change Location
  }

  /**
  * Adds a close pallet button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Close Pallet button added to GUIState.
  */
  public GUIButton addCloseButton(int x, int y) {
    return addButton(CLOSE_BUTTON_TAG,x,y,BUTTON_WIDTH,interpretPhrase("CLOSE_PALLET")); //Close Pallet
  }

  /**
  * Adds a reprint button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Reprint button added to GUIState.
  */
  public GUIButton addReprintButton(int x, int y) {
    return addButton(REPRINT_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("REPRINT")); //Reprint
  }

  /**
  * Adds an override button at specified coordinates.
  * @param x x-coordinate
  * @param y y-coordinate
  * @return Override button added to GUIState.
  */
  public GUIButton addOverrideButton(int x, int y) {
    return addButton(OVERRIDE_BUTTON_TAG,x,y,BUTTON_WIDTH,WARNING_YELLOW,interpretPhrase("OVERRIDE")); //Override
  }

  /**
   * Adds a button to the screen given a GUIButton.
   * @param guiButton GUIButton to add.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(GUIButton guiButton) {
    if(state.buttons==null)
      state.buttons = new ArrayList<GUIButton>() ;
    state.buttons.add(guiButton) ;
    return guiButton;
  }

  /**
   * Adds a button to the screen at the specified position and size.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param backgroundColor button fill color.
   * @param textColor font color.
   * @param borderColor border color.
   * @param borderSize border size in pixels.
   * @param text Text of button.
   * @param textSize font size.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y, Float z,
            Integer width, Integer height,
            String backgroundColor, String textColor,
            String borderColor, Integer borderSize,
            String text, Float textSize) {
    GUIButton guiButton = new GUIButton(tag,
                                        x,y,z,
                                        width,height,
                                        backgroundColor,textColor,
                                        borderColor,borderSize,
                                        text,textSize) ;
    return addButton(guiButton);
  }

  /**
   * Adds a button to the screen at the specified position and width.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param text Text of button.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y,
            Integer width, String text) {
    return addButton(tag,
              x,y,0.0f,
              width,DEFAULT_BUTTON_HEIGHT,
              DEFAULT_BUTTON_BACKGROUND, DEFAULT_BUTTON_TEXT_COLOR,
              DEFAULT_BUTTON_BORDER_COLOR, DEFAULT_BUTTON_BORDER_SIZE,
              text, DEFAULT_BUTTON_TEXT_SIZE) ;
  }

  /**
   * Adds a button to the screen at the specified position with a specific background/font color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param backgroundColor background fill color of button.
   * @param textColor font color.
   * @param text Text of button.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y,
            String backgroundColor, String textColor,
            Integer width, String text) {
    return addButton(tag,
              x,y,0.0f,
              width,DEFAULT_BUTTON_HEIGHT,
              backgroundColor, textColor,
              DEFAULT_BUTTON_BORDER_COLOR, DEFAULT_BUTTON_BORDER_SIZE,
              text, DEFAULT_BUTTON_TEXT_SIZE) ;
  }
 
  /**
   * Adds a button to screen at specified position and size.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param height height in piexls.
   * @param text Text of button.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y,
            Integer width, Integer height,
            String text) {
    return addButton(tag,
              x,y,0.0f,
              width,height,
              DEFAULT_BUTTON_BACKGROUND, DEFAULT_BUTTON_TEXT_COLOR,
              DEFAULT_BUTTON_BORDER_COLOR, DEFAULT_BUTTON_BORDER_SIZE,
              text, DEFAULT_BUTTON_TEXT_SIZE) ;
  }

  /**
   * Adds a button to the screen at specified position.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param text Text of button.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y,
            String text) {
    return addButton(tag,
              x,y,0.0f,
              50 + (int) (0.5 * getWidth(text) * DEFAULT_BUTTON_TEXT_SIZE),DEFAULT_BUTTON_HEIGHT,
              DEFAULT_BUTTON_BACKGROUND, DEFAULT_BUTTON_TEXT_COLOR,
              DEFAULT_BUTTON_BORDER_COLOR, DEFAULT_BUTTON_BORDER_SIZE,
              text, DEFAULT_BUTTON_TEXT_SIZE) ;
  }

  /**
   * Adds a button to screen at specified position and size.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param backgroundColor background fill color of button.
   * @param text Text of button.
   * @return GUIButton added to the GUIState.
   */
  public GUIButton addButton(Integer tag,
            Integer x, Integer y,
            Integer width, String backgroundColor,
            String text) {
    return addButton(tag,
              x,y,0.0f,
              width,DEFAULT_BUTTON_HEIGHT,
              backgroundColor, DEFAULT_BUTTON_TEXT_COLOR,
              DEFAULT_BUTTON_BORDER_COLOR, DEFAULT_BUTTON_BORDER_SIZE,
              text, DEFAULT_BUTTON_TEXT_SIZE) ;
  }

  /**
   * Deletes a button from the screen given a GUIButton.
   * @param button GUIButton to delete.
   */
  public void deleteButton(GUIButton button) {
    app.deleteButton(button);
  }

  /**
   * Adds a text entry box to the screen given a GUIEntry.
   * @param guiEntry GUIEntry
   * @return GUIEntry added to the GUIState.
   */
  public GUIEntry addEntry(GUIEntry guiEntry) {
    if(state.entries==null)
      state.entries = new ArrayList<GUIEntry>() ;

    state.entries.add(guiEntry) ;
    return guiEntry;
  }

  /**
   * Creates a text entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param textSize font size.
   * @param defaultValue default placeholder text.
   * @param hint text hint.
   * @param textColor font color.
   * @param backgroundColor background fill color.
   * @param accentColor entry accent color.
   * @param useNumericKeypad true/false use numeric numpad or standard keyboard.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addEntry(Integer tag,
           Integer x, Integer y, Float z,
           Integer width, Integer height,
           Float textSize,
           String defaultValue, String hint,
           String textColor, String backgroundColor, String accentColor,
           Boolean useNumericKeypad) {
    GUIEntry guiEntry = new GUIEntry(tag,
                                     x,y,z,
                                     width,height,
                                     textSize,
                                     defaultValue,hint,
                                     textColor,backgroundColor,accentColor,
                                     useNumericKeypad) ;
    return addEntry(guiEntry);
  }

  /**
   * Creates a text entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addTextEntry(Integer tag,
               Integer x, Integer y,
               Integer width ) {
    return addEntry(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "","",
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             false) ;
  }

  /**
   * Creates a text entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param hint text hint.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addTextEntry(Integer tag,
               Integer x, Integer y,
               Integer width, String hint ) {
    return addEntry(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "",hint,
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             false) ;
  }

  /**
   * Creates a text entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param height width in pixels.
   * @param hint text hint.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addTextEntry(Integer tag,
               Integer x, Integer y,
               Integer width, Integer height, String hint ) {
    return addEntry(tag,
             x, y, 0f,
             width, height,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "",hint,
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             false) ;
  }

  /**
   * Creates a number entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addNumberEntry(Integer tag,
                Integer x, Integer y,
                Integer width ) {
    return addEntry(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "","",
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             true) ;
  }

  /**
   * Creates a number entry box at specified position, size, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param hint number entry hint.
   * @return Adds a GUIEntry to GUIState.
   */
  public GUIEntry addNumberEntry(Integer tag,
                Integer x, Integer y,
                Integer width, String hint ) {
    return addEntry(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "",hint,
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             true) ;
  }

  /**
   * Deletes a GUIEntry from screen.
   * @param entry GUIEntry to be deleted.
   */
  public void deleteEntry(GUIEntry entry) {
    app.deleteEntry(entry);
  }

  /**
   * Adds a password text entry box given a GUIEntryPW.
   * @param guiEntryPW GUIEntryPW to be added.
   * @return GUIButton added to the GUIState.
   */
  public GUIEntryPW addEntryPW(GUIEntryPW guiEntryPW) {
    if(state.passwords==null)
      state.passwords = new ArrayList<GUIEntryPW>() ;

    state.passwords.add(guiEntryPW) ;
    return guiEntryPW;
  }

  /**
   * Adds a password text entry box at specified size, position, and color.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param z z-index.
   * @param width width in pixels.
   * @param height height in pixels.
   * @param textSize font size.
   * @param defaultValue default placeholder text.
   * @param hint text hint.
   * @param textColor font color.
   * @param backgroundColor background fill color.
   * @param accentColor entry accent color.
   * @param useNumericKeypad true/false use numeric numpad or standard keyboard.
   * @return Adds a GUIEntryPW to GUIState.
   */
  public GUIEntryPW addEntryPW(Integer tag,
           Integer x, Integer y, Float z,
           Integer width, Integer height,
           Float textSize,
           String defaultValue, String hint,
           String textColor, String backgroundColor, String accentColor,
           Boolean useNumericKeypad) {
    GUIEntryPW guiEntryPW
      = new GUIEntryPW(tag,
                     x,y,z,
                     width,height,
                     textSize,
                     defaultValue,hint,
                     textColor,backgroundColor,accentColor,
                     useNumericKeypad) ;
    return addEntryPW(guiEntryPW);  
  }

  /**
   * Adds a password text entry box at specified size and width.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param z z-index.
   * @param width width in pixels.
   * @return Adds a GUIEntryPW to GUIState.
   */
  public GUIEntryPW addTextEntryPW(Integer tag,
               Integer x, Integer y,
               Integer width ) {
    return addEntryPW(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "","",
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             false) ;
  }

  /**
   * Adds a password text entry box at specified size and width.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param hint password entry hint.
   * @return Adds a GUIEntryPW to GUIState.
   */
  public GUIEntryPW addTextEntryPW(Integer tag,
               Integer x, Integer y,
               Integer width, String hint ) {
    return addEntryPW(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "",hint,
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             false) ;
  }

  /**
   * Adds a password text entry box at specified size and width.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @return Adds a GUIEntryPW to GUIState.
   */
  public GUIEntryPW addNumberEntryPW(Integer tag,
                Integer x, Integer y,
                Integer width ) {
    return addEntryPW(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "","",
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             true) ;
  }

  /**
   * Adds a password text entry box at specified size and width.
   * @param tag GUIElement tag.
   * @param x x-coordinate.
   * @param y y-coordinate.
   * @param width width in pixels.
   * @param hint password entry hint.
   * @return Adds a GUIEntryPW to GUIState.
   */
  public GUIEntryPW addNumberEntryPW(Integer tag,
                Integer x, Integer y,
                Integer width, String hint ) {
    return addEntryPW(tag,
             x, y, 0f,
             width, DEFAULT_TEXT_ENTRY_HEIGHT,
             DEFAULT_TEXT_ENTRY_TEXT_SIZE,
             "",hint,
             DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
             DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
             true) ;
  }

  /**
   * Deletes a GUIEntryPW from the screen.
   * @param entryPW GUIEntryPW to be deleted.
   */
  public void deleteEntryPW(GUIEntryPW entryPW) {
    app.deleteEntryPW(entryPW);
  }

  // util functions

  /**
   * Replaces all instances of a substring in a given string.
   * @param source Complete string.
   * @param toReplace Substring to replace.
   * @param replacement Substring replacement value.
   * @return Complete string with replaced substrings.
   */
  public String replaceAll(String source, String toReplace, String replacement) {
    int idx = source.lastIndexOf(toReplace) ;
    if(idx!=-1) {
      StringBuffer ret = new StringBuffer(source) ;
      ret.replace(idx,idx+toReplace.length(),replacement) ;
      while((idx=source.lastIndexOf(toReplace,idx-1))!=-1) {
        ret.replace(idx,idx+toReplace.length(),replacement) ;
      }
      source = ret.toString() ;
    }
    return source ;
  }

  /**
   * Replaces VictoryParams inside of a VictoryPhrase. 
   * Standard replacement uses <> "cartID"->"C123". 
   * Advanced VoiceLevel will not get anything inside of (). 
   * NATO alphabet substitute uses {} "a"->"alpha".
   * @param textToSpeak VictoryPhrase that needs replacement.
   * @param spoken True/False if value is spoken aloud for additional replacement.
   * @return VictoryPhrase with replaced VictoryParams.
   */
  public String replaceParams( String textToSpeak, boolean spoken ) {
    String delims = "[ ]+";
    String[] tokensArray = textToSpeak.split(delims);
    String newTextToSpeak = "";
    String userLevel = app.getUserPreference("level");
    List<String> tokens = new ArrayList<String>();
    for(int i = 0; i < tokensArray.length; i++) {
      tokens.add(tokensArray[i]);
    }
    //exclude the optional dialog for beginners (denoted by parentheses)
    //if the user is advanced
    if(exists(userLevel) && userLevel.equals("advanced")) {
      int beginnerPhraseStart = -1;
      int beginnerPhraseEnd = -1;
      do {
        beginnerPhraseStart = -1;
        beginnerPhraseEnd = -1;
        for (int i = 0; i < tokens.size(); i++) {
          if (tokens.get(i).startsWith("(")) 
            beginnerPhraseStart=i;
          if (tokens.get(i).endsWith(")")) {
            beginnerPhraseEnd=i;
            break;
          }
        }
        if((beginnerPhraseStart >= 0) && (beginnerPhraseEnd >= beginnerPhraseStart)) {
          for(int i = beginnerPhraseEnd; i >= beginnerPhraseStart; i--) {
            tokens.remove(i);
          }
        }
      } while ((beginnerPhraseEnd > -1) && (beginnerPhraseStart > -1));
    }
    //replace all text in angle brackets with param values of that name
    for ( String token : tokens ) {
      if ( token.startsWith( "<" ) && token.endsWith( ">" ) ) {
        token = token.substring( 1, token.length()-1 );
        String paramValue = getParam(token);
        if ( paramValue  != null )
          newTextToSpeak = newTextToSpeak + paramValue + " ";
        else
          newTextToSpeak = newTextToSpeak + token + " ";
      //speak all text in curly braces as NATO phonetic
      } else if ( token.startsWith( "{" ) && token.endsWith( "}" )) {
        token = token.substring( 1, token.length()-1 );
        String paramValue = getParam( token );
        if (token.equals("location")) paramValue = spokenAlias(getParam(token));
        if(spoken) {
          String[] alphanums = paramValue.split("");
          for(String alphanum : alphanums) {
            if(alphanum.equals("A")) alphanum = "ei";
            else if (alphanum.equals("-")) alphanum="dash";
            alphanum = natoSubstitute(alphanum);
            newTextToSpeak = newTextToSpeak + alphanum + " ";
          }
        }
        else {
          if ( paramValue  != null )
            newTextToSpeak = newTextToSpeak + paramValue + " ";
          else
            newTextToSpeak = newTextToSpeak + token + " ";
        }
      //remove parentheses from words spoken to beginner users
      } else if ( token.startsWith( "(" ) && token.endsWith( ")" )) {
        token = token.substring( 1, token.length()-1 );     
        newTextToSpeak = newTextToSpeak + token + " ";
      } else if ( token.startsWith( "(" )) {
        token = token.substring( 1, token.length() );     
        newTextToSpeak = newTextToSpeak + token + " ";
      } else if ( token.endsWith( ")" )) {
        token = token.substring( 0, token.length()-1 );     
        newTextToSpeak = newTextToSpeak + token + " ";
      } else {
        newTextToSpeak = newTextToSpeak + token + " ";
      }
    }
    //For non-spoken phrases such as error messages, remove the vibrate prefix
    if(!spoken) {
      if(newTextToSpeak.startsWith("*"))
        newTextToSpeak = newTextToSpeak.substring(5); //Remove "* .. "
    }
    return newTextToSpeak;
  }

  /**
   * Substitutes Alphabet characters into NATO pronounciations.
   * @param s Alphabet character
   * @return NATO substitute.
   */
  public String natoSubstitute(String s) {
    if(!exists(s)) return "";
    String[] alphanumerics = s.split("");
    String replacement = "";
    for(String alphanum : alphanumerics) {
      if(alphanum.equals("A")) alphanum="alpha";
      if(alphanum.equals("B")) alphanum="bravo";
      if(alphanum.equals("C")) alphanum="charlie";
      if(alphanum.equals("D")) alphanum="delta";
      if(alphanum.equals("E")) alphanum="echo";
      if(alphanum.equals("F")) alphanum="foxtrot";
      if(alphanum.equals("G")) alphanum="golf";
      if(alphanum.equals("H")) alphanum="hotel";
      if(alphanum.equals("I")) alphanum="india";
      if(alphanum.equals("J")) alphanum="juliet";
      if(alphanum.equals("K")) alphanum="kilo";
      if(alphanum.equals("L")) alphanum="lima";
      if(alphanum.equals("M")) alphanum="mike";
      if(alphanum.equals("N")) alphanum="november";
      if(alphanum.equals("O")) alphanum="oscar";
      if(alphanum.equals("P")) alphanum="papa";
      if(alphanum.equals("Q")) alphanum="quebec";
      if(alphanum.equals("R")) alphanum="romeo";
      if(alphanum.equals("S")) alphanum="sierra";
      if(alphanum.equals("T")) alphanum="tango";
      if(alphanum.equals("U")) alphanum="uniform";
      if(alphanum.equals("V")) alphanum="victor";
      if(alphanum.equals("W")) alphanum="whiskey";
      if(alphanum.equals("X")) alphanum="x-ray";
      if(alphanum.equals("Y")) alphanum="yankee";
      if(alphanum.equals("Z")) alphanum="zulu";
      replacement = replacement + alphanum + " .. ";
    }
    return replacement;
  }

  /**
   * Adds Speech Synthesis Markup Language (SSML) tags to speech for use by TTS engine.
   * @param text Input
   * @return String array of words/ssml characters for TTS to speak.
   */
  public String[] addSSML(String text) {
    Pattern p = Pattern.compile("\\* ");
    if(p.matcher(text).find()) {
      app.vibrateUnit(500);
    }
    text = " " + text + " " ;
    text = replaceAll(text,".. ","<break time=\"125ms\"/> ") ;
    text = replaceAll(text,"* ","[bonk] ") ;
    text = replaceAll(text,"_"," ") ;

    String[] words = text.trim().split("\\s+") ;
    ArrayList<String> list = new ArrayList<String>() ;
    String line = "" ;
    for(int i=0 ; i<words.length ; i++)
      {
      if(words[i].equals("[bonk]"))
        {
        if(!line.equals(""))
           list.add("<speak>" + line + "</speak>") ;
        line = "" ;
        list.add(words[i]) ;
        }
      else
        {
        line = line + " " + words[i] ;
        }
      }
     if(!line.equals(""))
        list.add("<speak>" + line + " </speak>") ;
     line = "" ;
    String[] voice = new String[list.size()] ;
    for(int i=0 ; i<list.size() ; i++)
      voice[i] = list.get(i) ;
    return voice ;
  }

  /**
   * Gets the text of a GUIButton given its tag.
   * @param tag GUIButton tag.
   * @return Text of button.
   */
  public String getButtonText(int tag) {
    for(GUIButton b : state.buttons) {
      if (b.tag.equals(Integer.valueOf(tag))) return b.text;
    }
    return "";
  }

  /**
   * Gets the entry hint message of a GUIEntry given its tag.
   * @param tag GUIEntry tag. 
   * @return Entry hint.
   */
  public String getEntryHint(int tag) {
    for(GUIEntry e : state.entries) {
      if (e.tag.equals(Integer.valueOf(tag))) return e.hint;
    }
    return "";
  }
 
  /**
   * Gets the entry hint message of a GUIEntryPW given its tag.
   * @param tag GUIEntryPW tag. 
   * @return Entry hint.
   */
  public String getPasswordHint(int tag) {
    for(GUIEntryPW e : state.passwords) {
      if (e.tag.equals(Integer.valueOf(tag))) return e.hint;
    }
    return "";
  }

  /**
   * Determines if a String is not empty and not null.
   * @param s String to check.
   * @return True/False.
   */
  public boolean exists(String s) {
    return((s!=null) && (!s.isEmpty()));
  }

  /**
   * Determines if a List of generic Type is not empty and not null.
   * @param s List to check.
   * @return True/False.
   */
  public boolean exists(List<?> l) {
    return((l!=null) && (!l.isEmpty()));
  }

  /**
   * Determines if a Map of generic Type is not empty and not null.
   * @param s Map to check.
   * @return True/False.
   */
  public boolean exists(Map<?,?> m) {
    return((m!=null) && (!m.isEmpty()));
  }

  /**
   * Passes login attempt to VictoryApp.
   * @param text Login id.
   * @return Result of login.
   */
  protected String login(String text) {
    String result = app.login(text);
    return result;
  }

  /**
   * Passes logout to VictoryApp and sets the next screen to LoginScreen.
   */
  public void logout() {
    app.clearParams();
    app.logout();
    this.setNextScreen(new LoginScreen(app));
  }

  /**
   * Loads user preferences (will resend the resourcelist).
   * This needs to resend the grammarMap as well, otherwise app will think there are no more grammars on device. 
   * Required as the user may be a different language, we always start in english.
   */
  public void loadUserPreferences(){
    app.sendResourceList(app.resourceDir);
  }

  /**
   * Gets the current prefix phrase of the Screen object.
   * @return Prefix phrase.
   */
  public String getPrefixPhrase() {
    return app.getPrefixPhrase();
  }
 
  /**
   * Sets the current prefix phrase of the Screen object.
   * @param message Prefix phrase.
   */
  public void setPrefixPhrase(String message) {
    app.setPrefixPhrase(message);
  }

  /**
   * Adds another phrase to the end of the current prefix phrase.
   * @param message Prefix phrase to add.
   */
  public void appendPrefixPhrase(String message) {
    app.appendPrefixPhrase(message);
  }

  /**
   * Adds another phrase to the beginning of the current prefix phrase.
   * @param message Prefix phrase to add.
   */
  public void prependPrefixPhrase(String message) {
    app.prependPrefixPhrase(message);
  }

  /**
   * Prompts the opertor to confirm they wish to proceed with login of given operator ID.
   * @param tentativeLogin Pending operator ID to login with.
   */
  public void requestLoginConfirmation(String tentativeLogin) {
    String phrase = "Confirm " + tentativeLogin;
    trace("operator [%s] heard [%s] (no screen change)",getOperatorID(), phrase);
    state.phrases = addSSML(replaceParams(phrase,true));
    app.updatePhrase(state.phrases);    
  }

  /**
   * Sends a phrase to be spoken through TTS on client's device.
   * @param token Phrase to be spoken, replaceParams will automatically be called.
   */
  public void sayPhrase(String token) {
    String phrase = replaceParams(interpretPhrase(token),true);
    phrase = replaceParams(phrase,true);
    trace("operator [%s] heard [%s] (no screen change)",getOperatorID() , phrase);
    app.logOutput(phrase);
    state.phrases = addSSML(phrase);
    app.updatePhrase(state.phrases);
  }

  /**
   * Speaks an error message, then repeats the original instruction of the screen.
   * @param token Error phrase to spoken.
   */
  public void sayErrorPhrase(String token) {
    sayPhrase(token);
    sayPhrase(getScreenPhrase());
  }

  /**
   * Sets the background color of the current Screen.
   * @param color Color code in ARGB hex.
   */
  public void setBackground(String color) {
    state.backGroundColor = color;
    app.updateScreen(this);
  }

  /**
   * Sets the Title of a screen.
   * @param phrase Title to be shown, replaceParams will automatically be called.
   */
  public void updateTitle(String phrase) {
    String languagePreference = app.getUserPreference("language");
    if(!exists(languagePreference)) languagePreference="english";
    title.text = replaceParams(interpretPhrase(phrase.toUpperCase()).toUpperCase(),false); 
    app.updateText(title);
  }

  /** 
   * Optional delayed logout/unreserving logic that can be controlled via status app. 
   * REQUIRES a status app program to be useful.
   * @param operatorId The Victory operator
   * @param sessionId Current session of operator's connection
   */
  protected void sendDelayLogoutRequest( String operatorId, int sessionId ) {
    JsonObject json = new JsonObject();
    json.addProperty("sessionId", sessionId);
    db.execute("INSERT INTO status SET statusType='delayLogout', data='%s', operator='%s' ,status='idle'", json.toString(), operatorId); 
  }

  /**
   * Sets a db lock with given name.
   * @param lockName name of lock.
   * @return True/False if lock is successful.
   */
  protected synchronized boolean lock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT GET_LOCK( '%s', %d )",
            lockName, LOCK_TIMEOUT );
      return (lockVal == 1);
  }

  /**
   * Releases a db lock with given name.
   * @param lockName name of lock.
   * @return True/False if unlock is successful.
   */
  protected synchronized boolean unlock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT RELEASE_LOCK( '%s' )",
            lockName );
      return (lockVal == 1);
  }

  /**
   * Productivity counting of operation/quantity for current operator ID.
   * @param operation operation name.
   * @param qty integer quantity performed.
   */
  public void recordAction(String operation, int qty) {
    app.recordAction(operation,qty);
  }

    /**
   * Productivity counting of operation/quantity for current operator ID.
   * @param operation operation name.
   * @param qty double quantity performed.
   */
  public void recordAction(String operation, double qty) {
    app.recordAction(operation,qty);
  }

  /**
   * RDS trace.
   * @param format formatted string.
   * @param args formatted arguments
   */
  public void trace(String format, Object... args) {
    app.trace(format, args);
  }

  /**
   * RDS alert.
   * @param format formatted string.
   * @param args formatted arguments
   */
  public void alert(String format, Object... args) {
    app.alert(format, args);
  }

  /**
   * RDS inform.
   * @param format formatted string.
   * @param args formatted arguments
   */
  public void inform(String format, Object... args) {
    app.inform(format, args);
  }

  /**
   * Shows optional debug messages if "debufMsg" control param is set to "true"
   * @param format formatted string.
   * @param args formatted arguments
   */
  public void debug(String format, Object... args) {
    if (db.getControl("victory", "debugMsg", "false").equals("true"))
      app.debug(format, args);
  }

}
