package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class PutItemScreen extends AbstractRepackPickingApp {

  public PutItemScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "PUT_TO_CONTAINER" Put to container <cartonLpn>
     * textEntryHint: "CONTAINER_BARCODE" Container barcode
     * phrase: "PUT_TO_CONTAINER_VOICE" Put to container
     */

    initPickingInfo(); // adds header info
    addButtons("", "", "", CANCEL);
    return true;
  }

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    setParam(VictoryParam.scan, scan);
    if (isValidScan(scan)) {
      return true;
    } else {
      sayPhrase("INVALID_BARCODE"); // Invalid barcode
      setError("INVALID_BARCODE"); // Invalid barcode
      return false;
    }
  }

  /*
   * Determines if the scanned cartonLpn or "gang" slot is valid.
   */
  protected boolean isValidScan(String scan) {
    if (scan.equalsIgnoreCase(getParam(VictoryParam.cartonLpn)) || 
        scan.equalsIgnoreCase(getParam(VictoryParam.containerPosition))
    ) {
      confirmPut("repackPicking");
      // Check if the user did not pick the full amount required, 
      // if so take them back to pick item screen.
      if (getIntParam(VictoryParam.qty) > 0) {
        determinePickingWording(getParam("qroFlag"),true);	
        setNextScreen(new PickItemScreen(app));
        return true;
      }
      // No more picks, get next.
      else {
        getNextPick();
        return true;
      }
    }
    return false;
  }

}
