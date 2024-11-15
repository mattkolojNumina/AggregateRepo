package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class GoToBayScreen extends AbstractRepackPickingApp {

  public GoToBayScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "GO_TO_AISLE_BAY" Go to Aisle <aisle> Bay <bay>
     * textEntryHint: "LOCATION" Location
     * phrase: "GO_TO_AISLE_BAY_VOICE" <aisle> <bay>
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
      setNextScreen(new ConfirmItemScreen(app));
      return true;
    }
    //Operator can use voice to reverse picking order
    else if(canOperatorReversePick() && scan.equalsIgnoreCase("Reverse")) {
      toggleReversePickState();
      return true;
    }
    setParam(VictoryParam.scan, scan);
    if(isValidBayScan(scan)) {
   	setNextScreen(new ConfirmItemScreen(app));
      return true;
    } else {
      sayPhrase("INVALID_LOCATION"); // Invalid location
      setError("INVALID_LOCATION"); // Invalid location
      return false;
    }
  }

}