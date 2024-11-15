package victoryApp.cartPickingApp;

import java.util.ArrayList;
import java.util.List;

import victoryApp.*;

import static victoryApp.gui.GUIConstants.*;

//This class extends Screen so it doesn't draw a blue "promptbox"
public class ExceptionScreen extends Screen {

  public ExceptionScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    this.getGUIState().getFooter().hideMessageButton();
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();

    super.handleInit();
    /*
     * titleText: "EXCEPTION" Exceptions
     * promptText: empty
     * textEntryHint: empty
     * phrase: "EXCEPTION_VOICE" Choose exception
     */
    
    List<String> exceptions = new ArrayList<String>();
    exceptions.add(interpretPhrase("BAD_LOCATION_LABEL"));
    exceptions.add(interpretPhrase("NO_PRODUCT_UPC"));
    exceptions.add(interpretPhrase("DAMAGED_PRODUCT"));
    exceptions.add(interpretPhrase("SLOT_EMPTY"));

    int tag = 1;
    int x = 100;
    int y = 250;
    int w = (exceptions.size() > 3 ? 625 : 1300);
    //note screen assumes there are no more than 6 exceptions
    for (String exception : exceptions) {
      if(!exists(exception)) continue;
      addButton(tag++,x,y,w,exception);
      if(tag > 3) x = 825;
      y = 250 + (((tag-1)%3) * 200);
    }

    addButton(CANCEL_BUTTON_TAG,x,y,w,WARNING_YELLOW,interpretPhrase("CANCEL"));

    return true;
  }

  /*
   * Handles edge case where param isn't set to start operator back at startScreen
   */
  @Override
  public boolean handleCancel() {
    //Handle edge case where param isn't set to start operator back at startScreen
    if(getParam("exceptionPreviousScreen") == null || getParam("exceptionPreviousScreen").isEmpty()) {
      this.setNextScreen(new StartScreen(app));
      return true;
    }
    this.setNextScreen(getScreenParam("exceptionPreviousScreen"));
    return true;
  }

  public boolean handleButton(int tag) {
    if (super.handleButton(tag)) return true;
    switch (tag){
      case 1:
        reportBadLocationLabel();
        inform("Operator reported bad location label, prompting UPC scan");
        setNextScreen(new ScanUPCScreen(app));
        return true;
      case 2:
        reportNoProductUPC();
        return handleCancel();
      case 3:
        reportDamagedProduct();
        return handleCancel();
      case 4:
        doSlotEmpty();
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean handleVoice(String text) {
    if(super.handleVoice(text)) return true;
    return handleInput(text);
  }

  @Override
  public boolean handleScan(String text) {
    super.handleScan(text);
    return handleInput(text);
  }

  /*
   * Operator can also say the various exceptions with voice input.
   * NOTE: This does not override because we do not want to render 
   * the blue box behind the buttons on this screen
   */
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;

    if(scan.equalsIgnoreCase("noProductLabel")) {
      reportBadLocationLabel();
      inform("Operator reported bad location label, prompting UPC scan");
      setNextScreen(new ScanUPCScreen(app));
      return true;
    }
    else if(scan.equalsIgnoreCase("noProductUPC")) {
      reportNoProductUPC();
      return handleCancel();
    }
    else if(scan.equalsIgnoreCase("damagedProduct")) {
      reportDamagedProduct();
      return handleCancel();
    }
    else if(scan.equalsIgnoreCase("slotEmpty")) {
      doSlotEmpty();
      return true;
    }
    else if(scan.equalsIgnoreCase("cancel")) {
      return handleCancel();
    }
    return false;
  }

  protected void reportBadLocationLabel() {
    inform(
      "Operator is reporting a bad location label department [%s] aisle [%s] bay [%s]", 
      getParam("department"), getParam("aisle"), getParam("bay")
    );
    db.execute(
      "INSERT INTO victoryExceptions " +
      "SET sku='%s', uom='%s', location='%s', " +
      "qty=%d, operatorId='%s', reason='%s'",
      getParam("item"), getParam("uom"),
      getParam("location"), getIntParam("qty"),
      getOperatorID(), "Bad location label"
    );
    recordAction("badLocationLabel", 1);
  }

  protected void reportNoProductUPC() {
    inform(
      "Operator is reporting no product UPC for sku [%s] expected barcode [%s] in department [%s] aisle [%s] bay [%s]", 
      getParam("item"), getParam("barcode"), getParam("department"), getParam("aisle"), getParam("bay")
    );
    db.execute(
      "INSERT INTO victoryExceptions " +
      "SET sku='%s', uom='%s', location='%s', " +
      "qty=%d, operatorId='%s', reason='%s'",
      getParam("item"), getParam("uom"),
      getParam("location"), getIntParam("qty"),
      getOperatorID(), "No product UPC"
    );
    recordAction("noProductUPC", 1);
  }

  protected void reportDamagedProduct() {
    inform(
      "Operator is reporting a damaged product for sku [%s] in department [%s] aisle [%s] bay [%s]", 
      getParam("item"), getParam("department"), getParam("aisle"), getParam("bay")
    );
    db.execute(
      "INSERT INTO victoryExceptions " +
      "SET sku='%s', uom='%s', location='%s', " +
      "qty=%d, operatorId='%s', reason='%s'",
      getParam("item"), getParam("uom"),
      getParam("location"), getIntParam("qty"),
      getOperatorID(), "Damaged product"
    );
    recordAction("damagedProduct", 1);
  }

  protected void doSlotEmpty() {
    inform("Operator wants to confirm a slot as empty");
    setNextScreen(new ConfirmSlotEmptyScreen(app));
  }

}
