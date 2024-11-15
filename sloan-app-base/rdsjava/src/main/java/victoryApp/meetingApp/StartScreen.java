package victoryApp.meetingApp;

import victoryApp.*;

public class StartScreen extends AbstractVictoryScreen {

  public StartScreen(VictoryApp app) {
    super(app);
    inform("Operator is in meeting");
    this.getGUIState().getFooter().hideChangeAreaButton();
  }

}