package victoryApp.makeupShortsApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;
import java.util.Map;

public class ScanShortContainerScreen extends AbstractMakeupShortsApp {

  public ScanShortContainerScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    super.handleInit();

    addButtons("", "", "", CANCEL);
    initPickingInfo();

    return true;
  }
 
  @Override
  public boolean handleInput(String scan) {
    inform( "entry received text [%s]", scan );
    
    String lpn = getParam(VictoryParam.cartonLpn);

    if(scan.equalsIgnoreCase(lpn)) {
        setNextScreen(new ScanShortQtyScreen(app));
        return true;
    }
    else {
        sayPhrase("INVALID_CONTAINER");
        setError("INVALID_CONTAINER");
        return false;
    }

    
  }


}
