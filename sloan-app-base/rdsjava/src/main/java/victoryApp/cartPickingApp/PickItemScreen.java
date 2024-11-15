package victoryApp.cartPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class PickItemScreen extends AbstractCartPickingApp {

  public PickItemScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
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
      updateCartPosition();
      setNextScreen(new PutItemScreen(app));
      return true;
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

}
