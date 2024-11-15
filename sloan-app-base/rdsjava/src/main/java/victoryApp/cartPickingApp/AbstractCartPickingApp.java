package victoryApp.cartPickingApp;

import java.util.Map;

import dao.AbstractDAO;

import java.util.LinkedHashMap;

import victoryApp.*;
import victoryApp.gui.*;

public class AbstractCartPickingApp extends AbstractSloaneScreen {

  public AbstractCartPickingApp(VictoryApp app) {
    super(app);
  }

  protected enum VictoryParam {
    scan, qroFlag, breakPacks, customerNumber, 
    cartonSeq, lpn, pickQty, putQty, qty, item, uom, uomPhrase, orderId,
    cartId, cartSeq, locationCheckDigits, checkDigitValidation, cartSlot,
    department, aisle, bay, shelf, slot, location, walkSequence, 
    showComplete, eachQty, shelfPackQty, departmentAisleBay, alias,
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
   * Cancels the current operation and returns operator to ready screen.
   */
  @Override
  public boolean handleCancel() {
    unreserveOperatorTasks();
    clearAllParam();
    setNextScreen(new ReadyScreen(app));
    return true;
  }

  /*
   * Skips the current location/item.
   */
  @Override
  public boolean handleSkip() {
    setParam(VictoryParam.walkSequence, getIntParam(VictoryParam.walkSequence) + 1);
    recordAction("skip", 1);
    //Unreserve the current incomplete picks
    unreservePicks();
    getNextPick();
    return true;
  }

  /*
   * If enabled by control param, allow operator to skip scanning of the barcode.
   */
  public boolean handleNoScan() {
    recordAction("noscan", 1);
    setNextScreen(new PickItemScreen(app));
    return true;
  }
  
  /*
   * Sends operator to exception screen
   */
  @Override
  public boolean handleException() {
	 setParam("exceptionPreviousScreen", this.getClass().getName()); 
    setNextScreen(new ExceptionScreen(app));
    return true;
  }

  /*
   * Changes the task.
   */
  @Override
  public boolean handleChangeTask() {
    unreserveOperatorTasks();
    super.handleChangeTask();
    clearAllParam();
    return true;
  }

  /*
   * Changes the area.
   */
  @Override
  public boolean handleChangeArea() {
    unreserveOperatorTasks();
    clearAllParam();
    setNextScreen(new AreaSelectScreen(app));
    return true;
  }

  /*
   * Logs user out and unreserves the current cart.
   */
  @Override
  public void logout() {
    unreserveOperatorTasks();
    super.logout();
  }

  /*
   * Unreserves picks on disconnect.
   */
  @Override
  public void handleDisconnect() {
    super.handleDisconnect();
  }

  // // // // // // //
  // Logic Methods

  /*
   * Handles all unreserving logic
   */
  public void unreserveOperatorTasks() {
    unreserveCart();
    unreservePicks();
  }

  /*
   * Create a header that shows picking information
   */
  protected void initPickingInfo() {
	 String item = getParam(VictoryParam.item);
	 String qroFlag = getParam(VictoryParam.qroFlag);
	 String displayItem = item.substring(0,3)+"-"+item.substring(3)+(qroFlag.equalsIgnoreCase("x")?"X":"");
    Map<String, String> pickingInfo = new LinkedHashMap<String, String>() {
      {
        put(interpretPhrase("LOCATION"), getParam(VictoryParam.alias)+"("+getParam(VictoryParam.locationCheckDigits)+")");
        put(interpretPhrase("ITEM"), displayItem);
        put(interpretPhrase("QTY"), getIntParam(VictoryParam.qty) + " " + getParam(VictoryParam.uom));
        //If the sku can be broken on shelf pack, show in header
        if(getParam(VictoryParam.breakPacks).equals("true")) {
          put(interpretPhrase("BREAK"), interpretPhrase("ALLOWED"));
        }
        else {
          put("", "");
        }
      }
    };
    this.getGUIState().setInfoBox(new GUIInfoBox(pickingInfo));
  }

  /*
   * Determines if a scanned cart is valid.
   */
  protected int isValidCart(String scan) {
    Map<String, String> cartInfo = db.getRecordMap(
      "SELECT rdsCarts.*, enabled FROM rdsCarts " +
      "JOIN cfgCarts USING (cartId) " +
      "WHERE cartId='%s' " +
      "ORDER BY cartSeq DESC LIMIT 1", scan
    );
    if(!exists(cartInfo)) {
      alert("Scanned cart [%s] not found", scan);
      return 0; 
    }

    String cartId = getMapStr(cartInfo, "cartId");
    int cartSeq = getMapInt(cartInfo, "cartSeq");
    String reservedBy = getMapStr(cartInfo, "reservedBy");
    String cartType = getMapStr(cartInfo, "cartType");
    boolean enabled = getMapStr(cartInfo, "enabled").equals("1") ? true : false;
    boolean buildStamp = exists(getMapStr(cartInfo, "buildStamp"));
    boolean pickEndStamp = exists(getMapStr(cartInfo, "pickEndStamp"));
    boolean completeStamp = exists(getMapStr(cartInfo, "completeStamp"));
    boolean errorStamp = exists(getMapStr(cartInfo, "errorStamp"));

    if(!enabled) {
      alert("Cart [%s] seq [%d] is not enabled", cartId, cartSeq);
      return -1;
    }
    if(exists(reservedBy) && !reservedBy.equals(getOperatorID())) {
      alert("Cart [%s] seq [%d] is reserved to another operator [%s]", cartId, cartSeq, reservedBy);
      return -2;
    }
    if(!buildStamp) {
      alert("Cart [%s] seq [%d] has not finished building", cartId, cartSeq);
      return -3;
    }
    if(pickEndStamp || completeStamp) {
      alert("Cart [%s] seq [%d] has completed picking", cartId, cartSeq);
      return -4;
    }
    if(errorStamp) {
      alert("Cart [%s] seq [%d] has errorStamp set", cartId, cartSeq);
      return -5;
    }

    setParam(VictoryParam.cartId, cartId);
    setParam(VictoryParam.cartSeq, cartSeq);

    reserveCart(cartSeq);
    inform("Operator reserved cart [%s] seq [%d] to start picking", cartId, cartSeq);

    db.execute(
      "UPDATE rdsCarts SET pickStartStamp=NOW() " +
      "WHERE cartSeq=%d AND pickStartStamp IS NULL", cartSeq
    );
    
    String area = getParam("area");
    String task = getParam("task");
    String op = getOperatorID();
    if( area.isEmpty() ) {
   	 setParam("area", cartType);
       db.execute(
      		 "UPDATE proOperators SET task='%s', area='%s' " +
             "WHERE operatorID='%s'", 
             task, cartType, op
		 );
       db.execute(
      		 "UPDATE proOperatorLog SET area='%s' " +
				 "WHERE operatorId='%s' AND task='%s' AND endTime IS NULL",
				 cartType, op, task
		 );
    } else if( !area.equals(cartType) ) {
       db.execute("UPDATE proOperatorLog " +
             "SET endTime=NOW() " +
             "WHERE operatorID='%s' " +
             "AND endTime IS NULL",
             op
       );
       setParam("area", cartType);
       db.execute(
         "UPDATE proOperators SET task='%s', area='%s' " +
         "WHERE operatorID='%s'", 
         task, cartType, op
       );
       db.execute(
         "INSERT INTO proOperatorLog (operatorID, task, area, startTime) " +
         "VALUES('%s', '%s', '%s', NOW())",
         op, task, cartType
       );
    }

    return 1;
  }

  /*
   * Reserves a cart to an operator
   */
  protected boolean reserveCart(int cartSeq) {
    try {
      lock("reserve");
      db.execute(
        "UPDATE rdsCarts SET reservedBy='%s' " +
        "WHERE cartSeq=%d", getOperatorID(), cartSeq
      );
    } catch (Exception e) {
      alert("Could not reserve the cart %s", e.toString());
      unreserveCart();
      // Set the operator to the startScreen to clear all params and reset them.
      setNextScreen(new StartScreen(app));
      return false;
    }
    unlock("reserve");
    AbstractDAO.postLog("cartSeq", getParam(VictoryParam.cartSeq), "cartPicking",
      String.format("Operator %s started picking cart", getOperatorID())
    );
    AbstractDAO.postLog("orderId", getParam(VictoryParam.cartSeq), "cartPicking",
      String.format("Operator %s started picking cart %s", getOperatorID(), getParam(VictoryParam.cartId))
    );
   return true;
  }

  /*
   * Unreserves a cart from an operator
   */
  protected void unreserveCart() {
    db.execute("UPDATE rdsCarts SET reservedBy='' WHERE cartId='%s'", getParam(VictoryParam.cartId));
    inform("Operator unreserved cart [%s] seq [%d]", getParam(VictoryParam.cartId), getIntParam(VictoryParam.cartSeq));
    setParam(VictoryParam.cartId, "");
    setParam(VictoryParam.cartSeq, "");
  }

  /*
   * Sets the lastPosition... columns for the current cart.
   */
  protected void updateCartPosition() {
    db.execute(
      "UPDATE cfgCarts " +
      "SET lastPositionLogical='Pick Location', " +
      "lastPositionPhysical='%s' " +
      "WHERE cartId='%s'",
      getParam(VictoryParam.location),
      getParam(VictoryParam.cartId)
    );
  }

  /*
   * Determine the next pick for the operator.
   */
  protected boolean getNextPick() {
    String operatorId = getOperatorID();
    String oldLocation = getParam(VictoryParam.location);
    String oldSKU = getParam(VictoryParam.item);
    String oldDepartment = getParam(VictoryParam.department);
    String oldDepartmentAisleBay = getParam(VictoryParam.departmentAisleBay);

    Map<String, String> nextPick = findNextPick();
    if (nextPick == null || nextPick.isEmpty()) {
      inform("Picking complete");
      db.execute(
        "UPDATE rdsCarts SET pickEndStamp=NOW(),completeStamp=NOW() " +
        "WHERE cartSeq=%d AND pickEndStamp IS NULL", 
        getIntParam(VictoryParam.cartSeq)
      );
      unreserveOperatorTasks();
      setPrefixPhrase("PICKING_COMPLETE");
      setParam(VictoryParam.showComplete, "true");
      setNextScreen(new ReadyScreen(app));
      return true;
    }

    String orderId = getMapStr(nextPick, "orderId");
    String customerNumber = getMapStr(nextPick, "customerNumer");
    int cartonSeq = getMapInt(nextPick, "cartonSeq");
    int eachQty = getMapInt(nextPick, "eachQty");
    int shelfPackQty = getMapInt(nextPick, "shelfPackQty");
    String sku = getMapStr(nextPick, "sku");
    String uom = getMapStr(nextPick, "uom");
    String qroFlag = getMapStr(nextPick, "qroFlag");
    String lpn = getMapStr(nextPick, "lpn");
    String cartSlot = getMapStr(nextPick, "cartSlot");
    String department = getMapStr(nextPick, "department");
    String aisle = getMapStr(nextPick, "aisle");
    String bay = getMapStr(nextPick, "bay");
    String slot = getMapStr(nextPick, "slot");
    String shelf = getMapStr(nextPick, "shelf");
    String location = getMapStr(nextPick, "location");
    int walkSequence = getMapInt(nextPick, "walkSequence");
    String checkDigits = getMapStr(nextPick, "checkDigits");
    String alias = getMapStr(nextPick, "alias");
    String departmentAisleBay = String.format("%s%s%s", department, aisle, bay);

    setParam(VictoryParam.orderId, orderId);
    setParam(VictoryParam.customerNumber, customerNumber);
    setParam(VictoryParam.item, sku);
    setParam(VictoryParam.uom, uom);
    setParam(VictoryParam.qroFlag, qroFlag);
    setParam(VictoryParam.eachQty, eachQty);
    setParam(VictoryParam.shelfPackQty, shelfPackQty);
    setParam(VictoryParam.cartonSeq, cartonSeq);
    setParam(VictoryParam.lpn, lpn);
    setParam(VictoryParam.cartSlot, cartSlot);
    setParam(VictoryParam.department, department);
    setParam(VictoryParam.aisle, aisle);
    setParam(VictoryParam.bay, bay);
    setParam(VictoryParam.slot, slot);
    setParam(VictoryParam.shelf, shelf);
    setParam(VictoryParam.location, location);
    setParam(VictoryParam.walkSequence, walkSequence);
    setParam(VictoryParam.locationCheckDigits, checkDigits);
    setParam(VictoryParam.departmentAisleBay,departmentAisleBay);
    setParam(VictoryParam.alias,alias);

    //Need to determine break packs from QRO flag
    determineQRO(qroFlag, sku, uom);

    //Get the spoken uom phrase from converison table
    String uomPhrase = getUomPhrase(uom);
    if(exists(uomPhrase)) {
      setParam(VictoryParam.uomPhrase, uomPhrase);
    }
    else {
      setParam(VictoryParam.uomPhrase, uom);
    }

    int rowCount = db.execute(
      "UPDATE rdsPicks p JOIN custOrderLines ol USING(orderLineSeq) " +
      "SET p.pickOperatorId='%s' WHERE cartonSeq=%d " +
      "AND p.sku='%s' AND p.uom='%s' AND ol.location='%s' AND p.pickOperatorId='' " +
      "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 ",
      operatorId, cartonSeq, sku, uom, location
    );
    int pickQty = getPickQty(cartonSeq, sku, uom, location);
    if (rowCount != pickQty || pickQty == 0) {
      alert("error getting pick qty");
      return false;
    }

    inform(
      "Next pick: %s of SKU/UOM %s/%s from %s to cartonSeq %d",
      pickQty, sku, uom, location, cartonSeq
    );
    setParam(VictoryParam.qty, pickQty);

    //Need to determine the "wording" of the picking prompt
    determinePickingWording(qroFlag,true);

    if(!oldDepartment.equals(department)) { //Only prompt operator to go to department if it changes
      this.setNextScreen(new GoToAisleScreen(app));
    } else if (!departmentAisleBay.equals(oldDepartmentAisleBay)) {
       this.setNextScreen(new GoToBayScreen(app));
    } else if (!location.equals(oldLocation)) {
      this.setNextScreen(new ConfirmItemScreen(app));
    } else if (!oldSKU.equals(sku)) {
      setPrefixPhrase("SAME_LOCATION"); //Inform operator the pick is at same location
      this.setNextScreen(new ScanUPCScreen(app));
    } else {
      this.setNextScreen(new PickItemScreen(app));
    }
    return true;
  }

  /*
   * Determines what and where the next pick is.
   */
  protected Map<String, String> findNextPick() {
    Map<String, String> map = null;
    int cartSeq = getIntParam(VictoryParam.cartSeq);
    String cartId = getParam(VictoryParam.cartId);
    map = db.getRecordMap(
      "SELECT p.orderId, p.cartonSeq, p.sku, p.uom, " +
      "customerNumber, FLOOR(ol.qty) AS eachQty, ol.shelfPackQty, " +
      "cs.qroFlag, " +
      "c.lpn, c.cartSlot, " +
      "l.area AS department, l.aisle, l.bay, l.row AS slot, l.alias, " +
      "l.column AS shelf, l.location, l.barcode AS locationBarcode, l.walkSequence, l.checkDigits " +
      "FROM rdsCartons c " +
      "JOIN custOrders o USING (orderId) " +
      "JOIN rdsPicks p USING (cartonSeq) " +
      "JOIN custOrderLines ol USING (orderLineSeq) " +
      "JOIN rdsLocations l USING (location) " +
      "JOIN custSkus cs ON p.sku=cs.sku AND p.baseUom=cs.uom " +
      "WHERE c.cartSeq=%d " +
      "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 " +
      "AND (p.pickOperatorId='%s' OR p.pickOperatorId='') " +
      "ORDER BY CASE " +
      "  WHEN l.walkSequence >= %d THEN l.walkSequence  " +
      "  ELSE l.walkSequence + (SELECT MAX(walkSequence)+1 FROM rdsLocations)  " +
      "END ASC LIMIT 1",
      cartSeq, getOperatorID(), getIntParam(VictoryParam.walkSequence)
    );
    if (map == null || map.isEmpty()) {
      inform("Could not find any more picks for cart [%s] seq [%d]", cartId, cartSeq);
    }
    return map;
  }
  
  /*
   * Determines if a scan is a valid cart slot barcode.
   */
  protected boolean isValidCartSlot(String scan) {
	  return scan.equalsIgnoreCase(getParam(VictoryParam.cartId)+getParam(VictoryParam.cartSlot));
  }   
  
  /*
   * Determines if a message is the valid cart slot checkDigits.
   */
  protected boolean isValidCartSlotCheckDigits(String text) {
	  String cartId = getParam(VictoryParam.cartId);
	  String cartSlot = getParam(VictoryParam.cartSlot);
	  String checkDigitsString = db.getString("", "SELECT checkDigits FROM cfgCartSlots WHERE cartId='%s' AND cartSlot='%s'", 
			  cartId,cartSlot);
	  String type = getCheckDigitPositions();
	  if(checkDigitsString.length() != 3) {
       alert(
	         "Provided checkDigit [%s] for cart slot [%s%s] is not 3 characters long", 
	         checkDigitsString, cartId, cartSlot
       );
       return false;
     }
	  if(type.equals("first")) {
	    if(text.equals(checkDigitsString.substring(0, 2))) {
	      trace("Check digit validated using first 2");
	      return true;
	    }
	    return false;
	  }
	  if(text.equals(checkDigitsString.substring(1))) {
	    trace("Check digit validated using last 2");
	    return true;
	  }
	  return false;	  
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