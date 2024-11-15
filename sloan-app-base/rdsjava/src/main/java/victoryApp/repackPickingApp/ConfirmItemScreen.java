package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class ConfirmItemScreen extends AbstractRepackPickingApp {

  public ConfirmItemScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "SHELF_SLOT" Shelf <shelf> Slot <slot>
     * textEntryHint: "LOCATION" Location
     * phrase: "SHELF_SLOT_VOICE" <shelf> <slot>
     */

    initPickingInfo(); // adds header info
    addButtons("", "", EXCEPTION, CANCEL);
    return true;
  }

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    //Validate a spoken/scaned/typed check digit
    if(isValidShelfSlotCheckDigit(scan)) {
      return determineScanRequired();
    }
    setParam(VictoryParam.scan, scan);
    //Validate a scanned/typed location barcode
    if(isValidShelfSlotBarcode(scan)) {
      return determineScanRequired();
    } else {
      if(isValidUpcScan(scan)) {   
      	setNextScreen(new PickItemScreen(app));
      	return true;
      } else {
	      sayPhrase("INVALID_LOCATION"); // Invalid location
	      setError("INVALID_LOCATION"); // Invalid location
	      return false;
      }
    }
  }

  /*
   * Determines if a scan is required.
   */
  private boolean determineScanRequired() {
    if(scanUPCRequired(getOperatorID(), getParam(VictoryParam.item), getParam((VictoryParam.customerNumber)))) {
      setNextScreen(new ScanUPCScreen(app));
      return true;
    }
    else {
      setNextScreen(new PickItemScreen(app));
      return true;
    }
  }

}
