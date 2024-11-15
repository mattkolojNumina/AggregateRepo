package victoryApp.cleaningApp;

import victoryApp.*;

public class StartScreen extends AbstractVictoryScreen {

  public StartScreen(VictoryApp app) {
    super(app);
    inform("Operator is cleaning");
    this.getGUIState().getFooter().hideChangeAreaButton();
  }

}