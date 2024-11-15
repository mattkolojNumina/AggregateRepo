package victoryApp;

import static victoryApp.gui.GUIConstants.*;

import victoryApp.gui.* ;
import java.util.*;

public class AreaSelectScreen extends AbstractVictoryScreen {

  private Map<Integer,String> areaMap = new LinkedHashMap<Integer,String>();

  public AreaSelectScreen(VictoryApp app) {
    super(app);
  }

  @Override
	public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    this.getGUIState().getFooter().hideChangeAreaButton();
    /*
     * titleText: "ENTER_AREA" Select area
     * promptText: empty
     * textEntryHint: empty
     * phrase: "ENTER_AREA" Select area
     */
    
    db.execute("UPDATE proOperatorLog " +
          "SET endTime=NOW() " +
          "WHERE operatorID='%s' " +
          "AND endTime IS NULL",
          getOperatorID()
    );    
    
    List<String> areas = db.getValueList(
      "SELECT area FROM proTaskAreas " +
      "WHERE task='%s'", getParam("task") 
    );
    int tag = 1;
    int x = 100;
    int y = 250;
    int w = 1300;
    if (areas.size() > 3)
       w = 625;
    if (areas.size() > 6)
       w = 510;
    if (areas.size() > 9)
       w = 400;
    for (String area : areas) {
      if(!exists(area)) continue;
      areaMap.put(Integer.valueOf(tag), area);
      x = 100+((tag-1)/3)*(w+10);
      addButton(tag++,x,y,w,camelCasePrettyPrint(area));
      y = 250 + (((tag-1)%3) * 200);
    }
    return true;
  }

  /*
   * Allows an operator to scan a barcode of an area
   * and navigate operator to corresponding task/area.
   */
  @Override
  public boolean handleScan(String scan) {
    if (super.handleScan(scan)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrint(scan))) {
        try {
          setParam("area", scan);
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), scan, getOperatorID()
          );
          db.execute(
            "INSERT INTO proOperatorLog (operatorID, task, area, startTime) " +
            "VALUES('%s', '%s', '%s', NOW())",
            getOperatorID(), getParam("task"), scan
          );
          this.setNextScreen((Screen)Class.forName("victoryApp." + getParam("task") + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
        } catch (Exception ex) {
          sayPhrase("ERROR_LOADING_TASK");
          alert("could not restore user to %s", getParam("screen"));
          ex.printStackTrace();
        }
        return true;
      }
    }
    inform("Operator scanned area [%s] and was not found", scan);
    return false;
  }

  /*
   * Handle each button that can be pressed on the screen 
   * and navigate operator to corresponding task/area.
   */
  @Override
  public boolean handleButton(int tag) {
    if (super.handleButton(tag)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if (tag == b.tag) {
        try {
          setParam("area", areaMap.get(tag));
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), areaMap.get(tag), getOperatorID()
          );
          db.execute(
            "INSERT INTO proOperatorLog (operatorID, task, area, startTime) " +
            "VALUES('%s', '%s', '%s', NOW())",
            getOperatorID(), getParam("task"), areaMap.get(tag)
          );
          this.setNextScreen((Screen)Class.forName("victoryApp." + getParam("task") + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
        } catch (Exception ex) {
          sayPhrase("ERROR_LOADING_TASK");
          alert("could not restore user to %s", getParam("screen"));
          ex.printStackTrace();
        }
        return true;
      }
    }
    alert("no behavior associated with this button!");
    return false;
  }

  /*
   * handleVoice checks that a voice intent matches the area, which will also match
   * the button's text in handleButton through camelCasePrettyPrint().
   */
  public boolean handleVoice(String text) {
    if(text.equals("")) return false;
    if (super.handleVoice(text)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrint(text))) {
        try {
          setParam("area", text);
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), text, getOperatorID()
          );
          db.execute(
            "INSERT INTO proOperatorLog (operatorID, task, area, startTime) " +
            "VALUES('%s', '%s', '%s', NOW())",
            getOperatorID(), getParam("task"), text
          );
          this.setNextScreen((Screen)Class.forName("victoryApp." + getParam("task") + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
        } catch (Exception ex) {
          sayPhrase("ERROR_LOADING_TASK");
          alert("could not restore user to %s", getParam("screen"));
          ex.printStackTrace();
        }
        return true;
      }
    }
    alert("no behavior associated with this voice intent!");
    return false;
  }

  @Override
  public boolean handleInput(String text) {
    return false;
  }

}
