package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class GoToAisleScreen extends AbstractRepackPickingApp {

  public GoToAisleScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "GO_TO_DEPARTMENT_AISLE" Go to Department <department> Aisle <aisle>
     * textEntryHint: "LOCATION" Location
     * phrase: "GO_TO_DEPARTMENT_AISLE_VOICE" <department> <aisle>
     */

    initPickingInfo(); // adds header info
    if(canOperatorReversePick()) {
      addButtons("", interpretPhrase("REVERSE"), EXCEPTION, CANCEL);
    }
    else {
      addButtons("", "", EXCEPTION, CANCEL);
    }
    return true;
  }

  /*
   * Allow operator to reverse picking direction.
   */
  @Override
  public boolean handleButton(int tag) {
    if(super.handleButton(tag)) return true;
    if(tag == 9500) {
      toggleReversePickState();
      return true;
    }
    return false;
  }

  /*
   * Operator can optionally scan/type an Aisle's barcode or say "Ready" to confirm Aisle.
   */
  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    //If operator uses voice or scans or types "ready" go to next screen
    if(scan.equalsIgnoreCase("Ready")) {
      setNextScreen(new GoToBayScreen(app));
      return true;
    }
    //Operator can use voice to reverse picking order
    else if(canOperatorReversePick() && scan.equalsIgnoreCase("Reverse")) {
      toggleReversePickState();
      return true;
    }
    setParam(VictoryParam.scan, scan);
    if(isValidAisleScan(scan)) {
   	setNextScreen(new GoToBayScreen(app));
      return true;
    } else {
      sayPhrase("INVALID_LOCATION"); // Invalid location
      setError("INVALID_LOCATION"); // Invalid location
      return false;
    }
  }

}
