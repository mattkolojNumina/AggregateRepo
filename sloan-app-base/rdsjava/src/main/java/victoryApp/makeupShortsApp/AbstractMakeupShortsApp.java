package victoryApp.makeupShortsApp;

import java.util.Map;

import dao.SloaneCommonDAO;

import java.util.LinkedHashMap;

import victoryApp.*;
import victoryApp.gui.*;

public class AbstractMakeupShortsApp extends AbstractSloaneScreen {

  public AbstractMakeupShortsApp(VictoryApp app) {
    super(app);
  }

  protected enum VictoryParam {
    scan, 
    customerNumber, 
    cartonLpn, cartonSeq, lpn, putQty, qty, item, uom, uomPhrase, sku, orderId,
    showComplete, makeUpQty,
  };

  public boolean handleInit() {
    super.handleInit();
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();
    this.getGUIState().getFooter().hideLogoutButton();
    return true;
  }

  // // // // // // //
  // handleMethods that are the same for each screen

  /*
   * Changes the task.
   */
  @Override
  public boolean handleChangeTask() {
    super.handleChangeTask();
    clearAllParam();
    return true;
  }

  /*
   * Cancels the current operation and returns operator to ready screen.
   */
  @Override
  public boolean handleCancel() {
    //unreserveOperatorTasks();
    //clearAllParam();
    unreserveMakeupShorts();
    setNextScreen(new StartScreen(app));
    return true;
  }

    /*
   * Create a header that shows picking information
   */
  protected void initPickingInfo() {

    Map<String, String> pickingInfo = new LinkedHashMap<String, String>() {
      {
        put(interpretPhrase("SKU"),     getParam(VictoryParam.sku));
        put(interpretPhrase("UOM"),     getParam(VictoryParam.uom));
        put(interpretPhrase("qty"),     getParam(VictoryParam.qty));
        put(interpretPhrase("OrderId"), getParam(VictoryParam.orderId));
        put(interpretPhrase("Carton LPN"), getParam(VictoryParam.cartonLpn));
      }
    };
    this.getGUIState().setInfoBox(new GUIInfoBox(pickingInfo));
  }

  /*
   * Logs user out and unreserves the current cart.
   */
  @Override
  public void logout() {
    super.logout();
  }

  /*
   * Unreserves picks on disconnect.
   */
  @Override
  public void handleDisconnect() {
	 db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='' WHERE makeupShortsOperatorId='%s' ", getOperatorID());
    super.handleDisconnect();
  }
  
  @Override
  public void unreserveOperatorTasks() {
	  db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='' WHERE makeupShortsOperatorId='%s' ", getOperatorID());
	  super.unreserveOperatorTasks();
  }

  public boolean isShortUPC(String upc) {
    return true;
  }

  protected Map<String,String> getSkuFromScan( String scan ) {
    return SloaneCommonDAO.getSkuFromUPC(scan);
  }

     /*
    * Gets the number of open picks remaining and if zero, returns true
    */
   protected boolean isContainerComplete(int cartonSeq) {
      return(db.getInt(-1, 
         "SELECT COUNT(*) FROM rdsPicks " +
         "WHERE cartonSeq=%d AND picked=0 " +
         "AND canceled=0",cartonSeq
         ) == 0
      );
   }

  protected void unreserveMakeupShorts() {
    db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='' WHERE cartonSeq=%s AND sku='%s' AND uom='%s'", getParam(VictoryParam.cartonSeq), getParam(VictoryParam.sku), getParam(VictoryParam.uom));
  }


  // // // // // // //
  // Helper methods

  protected String getParam(VictoryParam param) {
    return getParam(param.toString());
  }

  protected int getIntParam(VictoryParam param) {
    return getIntParam(param.toString());
  }

  protected void setParam(VictoryParam param, String value) {
    setParam(param.toString(), value);
  }

  protected void setParam(VictoryParam param, int value) {
    setParam(param.toString(), value);
  }

  protected void clearAllParam() {
    for (VictoryParam param : VictoryParam.values())
      setParam(param.toString(), "");
  }

}