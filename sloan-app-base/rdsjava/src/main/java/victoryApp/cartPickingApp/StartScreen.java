package victoryApp.cartPickingApp;

import victoryApp.*;

public class StartScreen extends AbstractCartPickingApp {

  public StartScreen(VictoryApp app) {
    super(app);
    //clearAllParam();
    //unreserveOperatorTasks();
    this.setNextScreen(new ReadyScreen(app));
  }

}
