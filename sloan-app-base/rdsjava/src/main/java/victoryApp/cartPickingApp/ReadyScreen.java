package victoryApp.cartPickingApp;

import victoryApp.*;

public class ReadyScreen extends AbstractCartPickingApp {

  public ReadyScreen(VictoryApp app) {
    super(app);
    clearAllParam();
    unreserveOperatorTasks();
	}

	public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();

    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "SCAN_CART" Scan cart
     * textEntryHint: "CART_BARCODE" Cart Barcode
     * phrase: "SCAN_CART" Scan cart
     */

    if(getParam(VictoryParam.showComplete).equals("true")) {
      setWarning("PICKING_COMPLETE"); //Picking complete
      setParam(VictoryParam.showComplete, "false");
    }

    return true;
	}

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    setParam(VictoryParam.scan,scan);
    int code = isValidCart(scan);
    if(code == 1) {
      getNextPick();
      return true;
    }
    else if(code == -1) {
      sayPhrase("CART_DISABLED"); //Cart disabled
      setError("CART_DISABLED"); //Cart disabled
      return false;
    }
    else if(code == -2) {
      sayPhrase("CART_ALREADY_RESERVED"); //Cart already reserved
      setError("CART_ALREADY_RESERVED"); //Cart already reserved
      return false;
    }
    else if(code == -3) {
      sayPhrase("CART_STILL_BUILDING"); //Cart still building
      setError("CART_STILL_BUILDING"); //Cart still building
      return false;
    }
    else if(code == -4) {
      sayPhrase("CART_COMPLETE"); //Cart complete
      setError("CART_COMPLETE"); //Cart complete
      return false;
    }
    else if(code == -5) {
      sayPhrase("CART_HAS_ERROR"); //Cart has error
      setError("CART_HAS_ERROR"); //Cart has error
      return false;
    }
    else {
      sayPhrase("INVALID_CART"); //Invalid cart
      setError("INVALID_CART"); //Invalid cart
      return false;
    }
  }

}