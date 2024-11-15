package victoryApp.cartPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class PutItemScreen extends AbstractCartPickingApp {

  public PutItemScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "PUT_TO_SLOT" Put to slot <cartSlot>
     * textEntryHint: "CART_SLOT" Cart slot
     * phrase: "PUT_TO_SLOT_VOICE" <cartSlot>
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
   * Determines if the scanned cartSlot is valid.
   */
  protected boolean isValidScan(String scan) {
    if (isValidCartSlot(scan) || isValidCartSlotCheckDigits(scan)) {
      confirmPut("cartPicking");
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
