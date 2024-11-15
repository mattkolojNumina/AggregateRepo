package victoryApp.cartPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class GoToBayScreen extends AbstractCartPickingApp {

  public GoToBayScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "GO_TO_AISLE_BAY" Go to Aisle <aisle> Bay <bay>
     * textEntryHint: "LOCATION" Location
     * phrase: "GO_TO_AISLE_BAY_VOICE" <aisle> <bay>
     */

    initPickingInfo(); // adds header info
    addButtons("", "", EXCEPTION, CANCEL);
    return true;
  }

  /*
   * Operator can optionally scan/type an Aisle's barcode or say "Ready" to confirm Aisle.
   */
  @Override
  public boolean handleInput(String text) {
    if(text.equals("")) return false;
    //If operator uses voice or scans or types "ready" go to next screen
    if(text.equalsIgnoreCase("Ready")) {
      setNextScreen(new ConfirmItemScreen(app));
      return true;
    }
    setParam(VictoryParam.scan, text);
    if(isValidBayScan(text)) {
   	setNextScreen(new ConfirmItemScreen(app));
      return true;
    } else {
      sayPhrase("INVALID_LOCATION"); // Invalid location
      setError("INVALID_LOCATION"); // Invalid location
      return false;
    }
  }

}
