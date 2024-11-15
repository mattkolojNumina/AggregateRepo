package victoryApp.repackPickingApp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static victoryApp.gui.GUIConstants.*;

import victoryApp.*;
import victoryApp.gui.GUIButton;

public class StartScreen extends AbstractRepackPickingApp {

  private Map<Integer,String> areaMap = new LinkedHashMap<Integer,String>();

  public StartScreen(VictoryApp app) {
    super(app);
    //clearAllParam();
    //unreserveOperatorTasks();
  }

  @Override
	public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    /*
     * titleText: "SELECT_SUB_AREA" Select sub area
     * promptText: empty
     * textEntryHint: empty
     * phrase: "SELECT_SUB_AREA" Select sub area
     */
    
    //Select the "sub-area" of the first area that was select
    //Ex: TaskSelectScreen -> "Level 1" -> This screen -> "Level 1 : 1"
    List<String> areas = db.getValueList(
      "SELECT area FROM proTaskAreas " +
      "WHERE task='%s' " +
      "ORDER BY ordinal, LENGTH(`area`), `area`", getParam("area") 
    );
    int tag = 1;
    int x = 100;
    int y = 200;
    int w = (areas.size() > 4 ? 625 : 1300);
    String backgroundColor = DEFAULT_BUTTON_BACKGROUND;
    for (String area : areas) {
      if(!exists(area)) continue;
      areaMap.put(Integer.valueOf(tag), area);
      backgroundColor = DEFAULT_BUTTON_BACKGROUND;
      int numPicks = db.getInt(0, 
        "SELECT COUNT(DISTINCT pickSeq) " +
        "FROM rdsPickAreas pa " +
        "JOIN rdsLocations l USING (`area`, aisle, bay) " +
        "LEFT JOIN custOrderLines ol USING (location) " +
        "LEFT JOIN rdsPicks p USING (orderLineSeq) " +
        "WHERE pa.zone='%s' " +
        "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 " +
        "AND (p.pickOperatorId='%s' OR p.pickOperatorId='') " +
        "GROUP BY pa.zone", area, getOperatorID()
      );
      if (numPicks > 0) {
        backgroundColor = LIGHT_BLUE;
      }
      else if (numPicks > 9) {
        backgroundColor = RED;
      }
     addButton(tag++,x,y,backgroundColor,DEFAULT_BUTTON_TEXT_COLOR,w,camelCasePrettyPrintIntegers(area));
      if(tag > 4) x = 825;
      y = 200 + (((tag-1)%4) * 175);
    }
    return true;
  }

  public boolean handleButton(int tag) {
    if (super.handleButton(tag)) return true;
    setParam("zone", camelCasePrettyPrintIntegers(areaMap.get(tag)));
    for (GUIButton b : this.getGUIState().getButtons()) {
      if (tag == b.tag) {
        try {
          setParam("zone", areaMap.get(tag));
          this.setNextScreen(new ReadyScreen(app));
          /*
          setParam("area", areaMap.get(tag));
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), getParam("area"), getOperatorID()
          );
          db.execute(
            "UPDATE proOperatorLog SET area='%s' " +
            "WHERE operatorId='%s' AND task='%s' AND endTime IS NULL",
            getParam("area"), getOperatorID(), getParam("task")
          );
          */
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
    if (super.handleVoice(text)) return true;
    setParam("zone", camelCasePrettyPrintIntegers(text));
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrintIntegers(text))) {
        try {
      	 setParam("zone", text.toLowerCase()); 
          this.setNextScreen(new ReadyScreen(app));
          /*
          setParam("area", text);
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), getParam("area"), getOperatorID()
          );
          db.execute(
            "UPDATE proOperatorLog SET area='%s' " +
            "WHERE operatorId='%s' AND task='%s' AND endTime IS NULL",
            getParam("area"), getOperatorID(), getParam("task")
          );
          */
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

  public boolean handleScan(String scan) {
    setParam("zone", camelCasePrettyPrintIntegers(scan));
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrintIntegers(scan))) {
        try {
      	 setParam("zone", scan.toLowerCase());
          this.setNextScreen(new ReadyScreen(app));
          /*
          setParam("area", scan);
          db.execute(
            "UPDATE proOperators SET task='%s', area='%s' " +
            "WHERE operatorID='%s'", 
            getParam("task"), getParam("area"), getOperatorID()
          );
          db.execute(
            "UPDATE proOperatorLog SET area='%s' " +
            "WHERE operatorId='%s' AND task='%s' AND endTime IS NULL",
            getParam("area"), getOperatorID(), getParam("task")
          );
          */
        } catch (Exception ex) {
          sayPhrase("ERROR_LOADING_TASK");
          alert("could not restore user to %s", getParam("screen"));
          ex.printStackTrace();
        }
        return true;
      }
    }
    alert("invalid scan [%s]", scan);
    return false;
  }

  public boolean handleInput(String text) {
    return false;
  }

}
