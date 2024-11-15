package victoryApp;

import java.util.*;

import victoryApp.gui.* ;
import static victoryApp.gui.GUIConstants.*;

public class TaskSelectScreen extends Screen {

  private Map<Integer,String> taskMap = new LinkedHashMap<Integer,String>();

  public TaskSelectScreen(VictoryApp app) {
    super(app);
  }  

  public TaskSelectScreen(VictoryApp app, String background) {
    super(app, background);
  }   

  //handle_______

  public boolean handleInit() {
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();
    
    clearParam("task");
    clearParam("area");
    db.execute("UPDATE proOperatorLog " +
               "SET endTime=NOW() " +
               "WHERE operatorID='%s' " +
               "AND endTime IS NULL",
               getOperatorID()
    );
    List<String> tasks = db.getValueList(
      "SELECT task FROM victoryTasks WHERE enabled=1 ORDER BY ordinal"
    );
    int tag = 1;
    int x = 100;
    int y = 250;
    int w = (tasks.size() > 3 ? 625 : 1300);
    //note screen assumes there are no more than 6 tasks, and they will match
    //the names of the folder for the task screens in ~/app/src/victory/victoryApp
    //if you append "App" to the end of the task name.
    //For example, you would need an entry in proTasks called cartBuild to
    //access screens in a ~/app/src/victory/victoryApp/cartBuildApp folder
    for (String task : tasks) {
      if(!exists(task)) continue;
      //If there is no matching victoryPhrase for the task, then default to pretty printed taskName
      //however for multi-language support you will need a phrase
      String phrase = db.getString(camelCasePrettyPrint(task), 
        "SELECT %s FROM victoryPhrases WHERE phrase=(SELECT phrase FROM victoryTasks WHERE task='%s')",
        app.getUserPreference("language"), task
      );
      taskMap.put(Integer.valueOf(tag), task);
      addButton(tag++,x,y,w,phrase);
      if(tag > 3) x = 825;
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
    if(scan.equals("")) return false;
    if(super.handleScan(scan)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrint(scan))) {
        //If there are areas for the task, prompt operator to select area first.
        int count = db.getInt(-1, "SELECT COUNT(*) FROM proTaskAreas WHERE task='%s'", taskMap.get(b.getTag()));
        if(count > 0) {
          try {
            setParam("task",taskMap.get(b.getTag()));
            this.setNextScreen((Screen)Class.forName("victoryApp.AreaSelectScreen").getConstructor(VictoryApp.class).newInstance(app));
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
        //If there are no areas for task, continue to <task>App.StartScreen...
        else {
          try {
            this.setNextScreen((Screen)Class.forName("victoryApp." + taskMap.get(b.getTag()) + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
            setParam("task",taskMap.get(b.getTag()));
            db.execute(
              "UPDATE proOperators SET task='%s', area='' " +
              "WHERE operatorID='%s'", 
              taskMap.get(b.getTag()), getOperatorID()
            );
            db.execute(
              "INSERT INTO proOperatorLog (operatorID, task, startTime) " +
              "VALUES('%s', '%s', NOW())",
              getOperatorID(), taskMap.get(b.getTag())
            );
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
      }
    }
    inform("Operator scanned task [%s] and was not found", scan);
    return false;
  }

  /*
   * Handle each button that can be pressed on the screen 
   * and navigate operator to corresponding task/area.
   */
  public boolean handleButton(int tag) {
    if (super.handleButton(tag)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if (tag == b.tag) {
        //If there are areas for the task, prompt operator to select area first.
        int count = db.getInt(-1, "SELECT COUNT(*) FROM proTaskAreas WHERE task='%s'", taskMap.get(tag));
        if(count > 0) {
          try {
            setParam("task", taskMap.get(tag));
            this.setNextScreen((Screen)Class.forName("victoryApp.AreaSelectScreen").getConstructor(VictoryApp.class).newInstance(app));
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
        //If there are no areas for task, continue to <task>App.StartScreen...
        else {
          try {
            setParam("task", taskMap.get(tag));
            db.execute(
              "UPDATE proOperators SET task='%s', area='' " +
              "WHERE operatorID='%s'", 
              taskMap.get(tag), getOperatorID()
            );
            db.execute(
              "INSERT INTO proOperatorLog (operatorID, task, startTime) " +
              "VALUES('%s', '%s', NOW())",
              getOperatorID(), taskMap.get(tag)
            );
            this.setNextScreen((Screen)Class.forName("victoryApp." + taskMap.get(tag) + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
      }
    }
    alert("no behavior associated with this button!");
    return false;
  }

  /*
   * handleVoice checks that a voice intent matches the task in the proTasks table, which will also match
   * the button's text in handleButton through camelCasePrettyPrint().
   */
  public boolean handleVoice(String text) {
    if(text.equals("")) return false;
    if (super.handleVoice(text)) return true;
    for (GUIButton b : this.getGUIState().getButtons()) {
      if(b.getButtonText().equalsIgnoreCase(camelCasePrettyPrint(text))) {
        //If there are areas for the task, prompt operator to select area first.
        int count = db.getInt(-1, "SELECT COUNT(*) FROM proTaskAreas WHERE task='%s'", taskMap.get(b.getTag()));
        if(count > 0) {
          try {
            setParam("task",taskMap.get(b.getTag()));
            this.setNextScreen((Screen)Class.forName("victoryApp.AreaSelectScreen").getConstructor(VictoryApp.class).newInstance(app));
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
        //If there are no areas for task, continue to <task>App.StartScreen...
        else {
          try {
            this.setNextScreen((Screen)Class.forName("victoryApp." + taskMap.get(b.getTag()) + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app));
            setParam("task",taskMap.get(b.getTag()));
            db.execute(
              "UPDATE proOperators SET task='%s', area='' " +
              "WHERE operatorID='%s'", 
              taskMap.get(b.getTag()), getOperatorID()
            );
            db.execute(
              "INSERT INTO proOperatorLog (operatorID, task, startTime) " +
              "VALUES('%s', '%s', NOW())",
              getOperatorID(), taskMap.get(b.getTag())
            );
          } catch (Exception ex) {
            sayPhrase("ERROR_LOADING_TASK");
            alert("could not restore user to %s", getParam("screen"));
            ex.printStackTrace();
          }
          return true;
        }
      }
    }
    alert("no behavior associated with this voice intent!");
    return false;
  }

  public boolean handleInput(String text) {
    return false;
  }

}