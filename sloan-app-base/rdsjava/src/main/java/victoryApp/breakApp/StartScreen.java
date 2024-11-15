package victoryApp.breakApp;

import victoryApp.*;

public class StartScreen extends AbstractVictoryScreen {

  public StartScreen(VictoryApp app) {
    super(app);
    inform("Operator is on break");
    this.getGUIState().getFooter().hideChangeAreaButton();
  }

}