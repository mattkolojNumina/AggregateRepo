package victoryApp;

public class ConnectScreen extends Screen {

  private static final int MOMENTARY_DISCONNECT = 60;

  public ConnectScreen(VictoryApp app) {
    super(app);
  }  

  public ConnectScreen(VictoryApp app, String background) {
    super(app, background);
  }   

  //handle_______

  public boolean handleInit() {
    addTitleText("Connected");
    return true;
  }

  public boolean handleTick() {
    String operator = db.getString("",
      "SELECT operatorID FROM victoryParams " +
      "WHERE name='deviceID' AND value='%s' ", app.deviceID
    );
    if(exists(operator)) {
      try {
        if(lock(operator)) {
          int timestampdiff = db.getInt(-1,
            "SELECT TIMESTAMPDIFF(SECOND,VALUE,NOW()) " +
            "FROM victoryParams WHERE operatorID='%s' " +
            "AND name='lastHeartbeat'", operator);
          if(timestampdiff < MOMENTARY_DISCONNECT) {
            //if it's been less than... however many seconds since the last heartbeat, this is a reconnect.
            String loginResult = login(operator);
            if (loginResult.equals("LOGGING_IN") || loginResult.equals("RECONNECTING")) {
              loadUserPreferences();
              String screenName = getParam("screen");
              if(exists(screenName)) {
                //If operator is reconnecting and disconnects, they get stuck in infinite loop if screen is connect, so log them out
                if(screenName.equals("victoryApp.ConnectScreen")) {
                  inform("Operator is reconnecting to ConnectScreen, logging them out!");
                  logout();
                  return true;
                }
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
                  inform("could not restore user to %s, restarting at LoginScreen", screenName);
                  ex.printStackTrace();
                  this.setNextScreen(new LoginScreen(app)); 
                }
              }
              else { 
                this.setNextScreen(new LoginScreen(app)); 
              }
              return true;
            }       
          }
        }
      } finally {
        unlock( operator );
      }
    }
    if(elapsed()>1000) 
      this.setNextScreen(new LoginScreen(app));
    return true;
  }

}
