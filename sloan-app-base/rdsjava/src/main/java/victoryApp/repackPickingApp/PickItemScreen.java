package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class PickItemScreen extends AbstractRepackPickingApp {

  public PickItemScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "PICK_WORDING" <pickWording>
     * textEntryHint: "QUANTITY" Quantity
     * phrase: "PICK_WORDING" <pickWording>
     */

    initPickingInfo(); // adds header info
    if(getParam(VictoryParam.breakPacks).equals("true")) {
      addButtons("", interpretPhrase("BREAK_PACK"), EXCEPTION, CANCEL);
    }
    else {
      addButtons("", "", EXCEPTION, CANCEL);
    }
    return true;
  }

  @Override
  public boolean handleButton(int tag) {
    if(super.handleButton(tag)) return true;
    if(tag==9500) {
      setNextScreen(new ConfirmBreakShelfPackScreen(app));
      return true;
    }
    return false;
  }

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    //If operator says "break shelf pack" and intent is returned, prompt operator to confirm
    if(scan.equalsIgnoreCase("breakPack")) {
      if(getParam(VictoryParam.breakPacks).equals("true")) {
        setNextScreen(new ConfirmBreakShelfPackScreen(app));
        return true;
      }
      else {
        sayPhrase("BREAK_PACK_NOT_ALLOWED"); //Break pack not allowed
        setError("BREAK_PACK_NOT_ALLOWED"); //Break pack not allowed
        return false;
      }
    }
    setParam(VictoryParam.scan, scan);
    if(validateQty(scan)) {
      if(requiresPutConfirmation()) {
        setNextScreen(new PutItemScreen(app));
        return true;
      }
      else {
        confirmPut("repackPicking");
        // Check if the user did not pick the full amount required, 
        // if so take them back to pick item screen.
        if (getIntParam(VictoryParam.qty) > 0) {
      	 determinePickingWording(getParam("qroFlag"),false);
          setNextScreen(new PickItemScreen(app));
          return true;
        }
        // No more picks, get next.
        else {
          getNextPick();
          return true;
        }
      }
    } else {
      sayPhrase("INVALID_QTY"); //Invalid quantity
      setError("INVALID_QTY"); //Invalid quantity
      return false;
    }
  }

  /*
   * Validates the quantity the operator entered.
   */
  public boolean validateQty(String qty) {
    boolean result = true;
    try {
      int amtConfirmed = Integer.parseInt(qty);
      // Determine if qty is outside range [0-required]
      int requiredQty = getIntParam(VictoryParam.qty);
      if (amtConfirmed <= 0 || amtConfirmed > requiredQty)
        throw new NumberFormatException();
      String qroFlag = getParam(VictoryParam.qroFlag);
      if( qroFlag.equalsIgnoreCase("x") ) {
      	int shelfPackQty = getIntParam(VictoryParam.shelfPackQty);
      	if( shelfPackQty > 0 ) {
      		if( (requiredQty % shelfPackQty == 0) && (amtConfirmed % shelfPackQty > 0 ) ) {
      			inform("confirmed qty %d is not multiple of shelf pack Qty %d, invalid!", amtConfirmed, shelfPackQty);
      			return false;
      		}
      	}
      }      
      setParam(VictoryParam.pickQty, amtConfirmed);
    } catch (NumberFormatException nfe) {
      inform("'%s' is not a valid number", qty);
      result = false;
    } catch (Exception ex) {
      ex.printStackTrace();
      result = false;
    }
    return result;
  }

  /*
   * Determines if the operator needs to put confirm to the container.
   * > 1 containers ganged up require put confirmation
   * Single containers AND control param set to "false" can bypass confirmation
   */
  protected boolean requiresPutConfirmation() {
    if(getIntParam(VictoryParam.containerNumber) != 1) {
      return true;
    }
    String required = db.getControl("victory", "requirePutConfirmationForSingles", "true");
    if(required.equalsIgnoreCase("true")) {
      trace("Put confirmation for single container required");
      return true;
    }
    trace("Put confirmation for single container NOT required");
    return false;
  }

}
