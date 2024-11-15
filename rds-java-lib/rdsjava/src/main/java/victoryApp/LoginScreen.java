package victoryApp;

import victoryApp.gui.* ;
import static victoryApp.gui.GUIConstants.*;

public class LoginScreen extends AbstractVictoryScreen {

  public String tentativeLogin = "";
  public GUIButton quit;

  public LoginScreen(VictoryApp app) {
    super(app);
    this.tentativeLogin = "";
  }  

  public LoginScreen(VictoryApp app, String background) {
    super(app, background);
    this.tentativeLogin = "";
  }   

  //handle_______

  public boolean handleInit() {
    super.handleInit();
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();
    addButton(9500, SCREEN_WIDTH/2 + 600, FOOTER_Y+20, 0.0f,
              300, 100, DEFAULT_BUTTON_BACKGROUND,
              DEFAULT_BUTTON_TEXT_COLOR,
              DEFAULT_BUTTON_BORDER_COLOR, -1,
              interpretPhrase("QUIT"), 90f
    );
    return true;
  }

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    trace("attempting to login as '%s'", scan);
    String loginResult = login(scan);
    if (loginResult.equals("LOGGING_IN") || loginResult.equals("RECONNECTING")) {
      loadUserPreferences();
      setPrefixPhrase(loginResult);
      //Set the initial force logoff state to false
      setParam("logoff", "false");
      String screenName = getParam("screen");
      if(exists(screenName)) {
        try {
          if(!screenName.equals("victoryApp.LoginScreen") && !screenName.equals("victoryApp.TaskSelectScreen") && !screenName.equals("victoryApp.AreaSelectScreen")) {
            //Make sure to reset proOperators incase of disconnect clearing it.
            String task = getParam("task");
            String area = getParam("area");
            db.execute(
              "UPDATE proOperators SET task='%s', area='%s', device='%s' " +
              "WHERE operatorID='%s'", task, area, getDeviceID(), getOperatorID()
            );
            if(exists(task)) {
              db.execute(
                "INSERT INTO proOperatorLog (operatorID, task, area, startTime) " +
                "VALUES('%s', '%s', '%s', NOW())",
                getOperatorID(), task, area
              );
            }
          }
          this.setNextScreen((Screen)Class.forName(screenName).getConstructor(VictoryApp.class).newInstance(app));
        } catch (Exception ex) {
          alert("could not restore user to %s", screenName);
          ex.printStackTrace();
          appendPrefixPhrase("READY"); //Ready
          this.setNextScreen(new TaskSelectScreen(app));
        }
      }
      else {
        appendPrefixPhrase("READY"); //Ready
        this.setNextScreen(new TaskSelectScreen(app));
      }
      return true;
    } else if (loginResult.equals("LOCK_ERROR")) {
      alert("Failed to get login DB LOCK");
   	  setError("ERROR_LOGIN");
    }
    else {
      setError("INVALID_OPERATOR_TEXT");
    } 
    sayPhrase(loginResult);
    return false;
  }

  @Override
  public boolean handleButton(int tag) {
    if(super.handleButton(tag)) return true;
    //Quit button tag will quit (close) the app
    if(tag==9500) app.quit();
    return true;
  }

  @Override
  public boolean handleVoice(String text) {
    inform("operator passed intent '%s'", text);
    this.getGUIState().getFooter().lastResponseInfo.text = lastResponse;
    this.getGUIState().getFooter().setLastResponse(camelCasePrettyPrint(text));
    app.lastResponse=camelCasePrettyPrint(text);
    app.updateText(this.getGUIState().getFooter().lastResponseInfo);
    boolean result = false;
    //if the operator said "cancel" when asked to confirm a login ID,
    //we go back to prompting them for the login ID. 
    //otherwise, the cancel is meaningless, and we'll just quietly prompt
    //the device to listen for another response (via "ignoreVoiceResponse()")
    if (text.equalsIgnoreCase("cancel")) {
      if(!exists(tentativeLogin)) {
        inform("operator wanted to cancel, but there's nothing to cancel. Ignoring");
        result = false;
      }
      else {
        sayPhrase("CANCELED_LOGIN");
        inform("operator canceled so as not to log in as %s", tentativeLogin);
        tentativeLogin=""; 
        result = false;
      }
    }
    //operator may say 'ok' or 'confirm'; Android will parse it as "confirm" and send "confirm"
    //to the server.
    //saying "confirm" won't do anything if there's nothing to confirm
    else if (text.equalsIgnoreCase("confirm")) {
      if(!exists(tentativeLogin)) {
        inform("operator wanted to confirm, but there's nothing to confirm. Ignoring");
        result = false;
      }
      //otherwise, proceed with login ATTEMPT.
      else {
        trace("attempting to login as '%s' (via voice)", tentativeLogin);
        String loginResult = login(tentativeLogin);
        if (loginResult.equals("LOGGING_IN") || loginResult.equals("RECONNECTING")) {
          loadUserPreferences();
          setPrefixPhrase(loginResult);
          //Set the initial force logoff state to false
          setParam("logoff", "false");
          if(exists(getParam("screen"))) {
            try {
              this.setNextScreen((Screen)Class.forName(getParam("screen")).getConstructor(VictoryApp.class).newInstance(app));
            } catch (Exception ex) {
              alert("could not restore user to %s", getParam("screen"));
              ex.printStackTrace();
              this.setNextScreen(new TaskSelectScreen(app));
            }
          }
          else {
            this.setNextScreen(new TaskSelectScreen(app));
          }
          return true;
        } 
        else {
          setError("INVALID_OPERATOR_TEXT");
        }
        tentativeLogin = "";
        sayPhrase(loginResult);
        return false;
      }
    }

    else if (!exists(tentativeLogin)) {
      inform( "user said '%s'; asking them to confirm", text );
      tentativeLogin = text;
      requestLoginConfirmation(text);
      app.clearResponse();
      app.requestVoiceResponse();
      result = true;
    } 
    else {
      inform("ignoring '%s' intent", text);
      result = false;
    }
    app.clearResponse();
    return result;
  } 

}
