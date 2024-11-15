package victoryApp.repackPickingApp;

import victoryApp.*;
import victoryApp.gui.GUIButton;

import static victoryApp.gui.GUIConstants.*;

public class ReadyScreen extends AbstractRepackPickingApp {

  public ReadyScreen(VictoryApp app) {
    super(app);
    clearAllParam();
    unreserveOperatorTasks();
  }

	public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    this.getGUIState().getFooter().showChangeAreaButton();

		super.handleInit();
    /*
     * titleText: "REPACK_PICKING" Repack picking
     * promptText: "SCAN_CONTAINER" Scan container
     * textEntryHint: "CONTAINER_BARCODE" Container Barcode
     * phrase: "SCAN_CONTAINER" Scan container
     */

    if(getParam(VictoryParam.showComplete).equals("true")) {
      setWarning("PICKING_COMPLETE"); //Picking complete
      setParam(VictoryParam.showComplete, "false");
    }

    setParam(VictoryParam.containerNumber, 0);
    if(!exists(getParam(VictoryParam.containerPosition))) {
      setParam(VictoryParam.containerPosition, "A");
    }

    determineGangingLevel();
    return true;
	}

  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    setParam(VictoryParam.scan,scan);
    int code = isValidCarton(scan);
    if(code == 1) {
      incrementGanging();
      return true;
    }
    if(code == -1) {
      sayPhrase("CONTAINER_ALREADY_RESERVED"); //Container already reserved
      setError("CONTAINER_ALREADY_RESERVED"); //Container already reserved
      return false;
    }
    else if(code == -2) {
      sayPhrase("CONTAINER_NOT_RELEASED"); //Container not released
      setError("CONTAINER_NOT_RELEASED"); //Container not released
      return false;
    }
    else if(code == -3) {
      sayPhrase("CONTAINER_COMPLETE"); //Container complete
      setError("CONTAINER_COMPLETE"); //Container complete
      return false;
    }
    else if(code == -5) {
      sayPhrase("NO_PICKS_IN_ZONE"); //No picks in zone
      setError("NO_PICKS_IN_ZONE"); //No picks in zone
      return false;
    }
    else {
      sayPhrase("INVALID_CONTAINER"); //Invalid container
      setError("INVALID_CONTAINER"); //Invalid container
      return false;
    }
  }

  /*
   * Confirms the currently selected cartons to be used for picking.
   */
  @Override
  public boolean handleNew() {
    if(getIntParam(VictoryParam.containerNumber) == 0) return false;
    trace("Operator [%S] is picking to (%d) containers", getOperatorID(), getIntParam(VictoryParam.containerNumber));
    getNextPick();
    return true;
  }

  /*
   * Allow operator to batch upto the max "ganging" of cartons operator is allowed.
   */
  public void incrementGanging() {
    setParam(VictoryParam.containerNumber, getIntParam(VictoryParam.containerNumber)+1);

    //Add in confirm button to let operator continue with currently selected cartons.
    GUIButton start = addStartButton(SCREEN_WIDTH-BUTTON_WIDTH-120, PROMPT_BOX_Y+20+PROMPT_BOX_HEIGHT/2);
    app.updateButton(start);

    int gangingLevel = getIntParam(VictoryParam.gangingLevel);

    //Check if operator has reserved the max number of cartons.
    if(getIntParam(VictoryParam.containerNumber) >= gangingLevel) {
      inform("Operator [%s] has reserved the max number (%d) of containers", getOperatorID(), gangingLevel);
      getNextPick();
    }
    else {
      //Update text to show next containerPosition.
      promptMsg.text = replaceParams(interpretPhrase("SCAN_CONTAINER_X"), false);
      app.updateText(promptMsg);
      sayPhrase("SCAN_CONTAINER");
    }
  }

}
