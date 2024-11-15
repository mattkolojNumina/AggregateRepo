package victoryApp.cartPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class ConfirmSlotEmptyScreen extends AbstractCartPickingApp {

  public ConfirmSlotEmptyScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "CONFIRM_SLOT_EMPTY" Confirm slot empty
     * textEntryHint: empty
     * phrase: "CONFIRM_SLOT_EMPTY" Confirm slot empty
     */

    promptBox.color = ERROR_RED;
    initPickingInfo(); // adds header info
    addCancelButton(SCREEN_WIDTH - BUTTON_WIDTH - 120, PROMPT_BOX_Y+20+PROMPT_BOX_HEIGHT/2);
    addConfirmButton(120, PROMPT_BOX_Y+20+PROMPT_BOX_HEIGHT/2);
    return true;
  }

  @Override
  public boolean handleConfirm() {
    confirmSlotEmpty("cartPicking");
    getNextPick();
    return true;
  }

  @Override
  public boolean handleCancel() {
	  setNextScreen(new ExceptionScreen(app));
    return true;
  }

}
