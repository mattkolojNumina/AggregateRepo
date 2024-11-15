package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class ConfirmBreakShelfPackScreen extends AbstractRepackPickingApp {

  public ConfirmBreakShelfPackScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "CONFIRM_BREAK_PACK" Confirm break shelf pack
     * textEntryHint: empty
     * phrase: "CONFIRM_BREAK_PACK" Confirm break shelf pack
     */

    promptBox.color = ERROR_RED;
    initPickingInfo(); // adds header info
    addCancelButton(SCREEN_WIDTH - BUTTON_WIDTH - 120, PROMPT_BOX_Y+20+PROMPT_BOX_HEIGHT/2);
    addConfirmButton(120, PROMPT_BOX_Y+20+PROMPT_BOX_HEIGHT/2);
    return true;
  }

  @Override
  public boolean handleConfirm() {
    confirmBreakShelfPack("repackPicking");
    getNextPick();
    return true;
  }

  @Override
  public boolean handleCancel() {
    setNextScreen(getScreenParam("previousScreen"));
    return true;
  }

}
