package victoryApp.repackPickingApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;

public class ScanUPCScreen extends AbstractRepackPickingApp {

  public ScanUPCScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    super.handleInit();
    /*
     * titleText: "CART_PICKING" Cart picking
     * promptText: "SCAN_UPC" Scan item UPC
     * textEntryHint: "UPC" UPC
     * phrase: "SCAN_UPC" Scan item UPC
     */

    initPickingInfo(); //adds header info
    String noScanFeature = db.getControl("victory", "noScanFeature", "false").equals("true") ? NO_SCAN : ""; //Show or hide no scan button
    addButtons("", noScanFeature, EXCEPTION, CANCEL);
    return true;
  }
 
  @Override
  public boolean handleInput(String scan) {
    if(scan.equals("")) return false;
    setParam(VictoryParam.scan, scan);
    //There are up to 3 upc's per sku at Sloane
    if(isValidUpcScan(scan)) {   
       setNextScreen(new PickItemScreen(app));
       return true;
     }
     else {
       sayPhrase("INVALID_BARCODE"); //Invalid barcode
       setError("INVALID_BARCODE"); //Invalid barcode
       return false;
     }
  }

  /*
   * If enabled by control param, allow operator to skip scanning of the barcode.
   */
  public boolean handleNoScan() {
    setNextScreen(new PickItemScreen(app));
    return true;
  }

}
