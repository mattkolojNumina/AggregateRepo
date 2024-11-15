package victoryApp;

import static victoryApp.gui.GUIConstants.*;

public class ConfirmLogoutScreen extends AbstractVictoryScreen {

  public ConfirmLogoutScreen(VictoryApp app) {
    super(app);
  }

  //handle_______

  public boolean handleInit() {
    super.handleInit();
    this.getGUIState().getFooter().hideMessageButton();
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();
    this.getGUIState().getFooter().hideLogoutButton();

    addConfirmButton(150, SCREEN_HEIGHT-BUTTON_WIDTH-30);
    addCancelButton(SCREEN_WIDTH-BUTTON_WIDTH-120, SCREEN_HEIGHT-BUTTON_WIDTH-30);

    return true;
  }

  @Override
  public boolean handleCancel() {
    if(getScreenParam("previousScreen") == null) {
      alert("Previous screen is null, return to task select");
      setNextScreen(new TaskSelectScreen(app));
    }
    else {
      this.setNextScreen(getScreenParam("previousScreen"));
    }
    return false;
  }

  @Override
  public boolean handleConfirm() {
    if(exists(getParam("task"))) {
      try {
        Screen currentTaskScreen = (Screen)Class.forName("victoryApp." + getParam("task") + "App.StartScreen").getConstructor(VictoryApp.class).newInstance(app);
        currentTaskScreen.logout();
      } catch ( Exception e ) {
        alert("Exception occured: " + e.toString());
      }
    }
    logout();
    return true;
  }
}
