package victoryApp.repackPickingApp;

import java.util.List;
import java.util.Map;

import dao.AbstractDAO;

import java.util.LinkedHashMap;

import victoryApp.*;
import victoryApp.gui.*;

public class AbstractRepackPickingApp extends AbstractSloaneScreen {

  public AbstractRepackPickingApp(VictoryApp app) {
    super(app);
  }

  protected enum VictoryParam {
    scan, gangingLevel, reversePicking, 
    qroFlag, breakPacks, customerNumber, 
    cartonLpn, cartonSeq, lpn, pickQty, putQty, qty, item, uom, uomPhrase, orderId,
    containerPosition, containerNumber, locationCheckDigits, checkDigitValidation,
    department, aisle, bay, shelf, slot, location, walkSequence, 
    showComplete, pickWording, eachQty, shelfPackQty, aisleBay, alias,
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
    //unreserveOperatorTasks();
    //clearAllParam();
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
    //Direct operator to start screen to pick a new sub-area instead of level
    if( getParam("screen").contains("ReadyScreen") )
   	 setNextScreen(new StartScreen(app));
    else
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
   * Determines the level at which an operator can "gang" up cartons to pick to.
   */
  protected void determineGangingLevel() {
    int level = db.getInt(1,
      "SELECT gangingLevel FROM proOperators " +
      "WHERE operatorId='%s'", getOperatorID()
    );
    setParam(VictoryParam.gangingLevel, level);
  }

  /*
   * Determines the level at which an operator can "gang" up cartons to pick to.
   */
  protected boolean canOperatorReversePick() {
    int allowReverse = db.getInt(0,
      "SELECT allowReverse FROM proOperators " +
      "WHERE operatorId='%s'", getOperatorID()
    );
    if(allowReverse == 1) {
      return true;
    }
    else {
      return false;
    }
  }

  /*
   * Handles state of whether or not operator is reverse picking.
   * Each time the operator will voice "Reverse Direction", the state will toggle 
   * and a new pick will be found.
   */
  protected boolean toggleReversePickState() {
    if(canOperatorReversePick()) {
      if(getParam(VictoryParam.reversePicking).equals("true")) {
        setParam(VictoryParam.reversePicking, "false");
        recordAction("reverseOrder", 1);
        unreservePicks();
        getNextPick();
      }
      else {
        setParam(VictoryParam.reversePicking, "true");
        recordAction("reverseOrder", 1);
        unreservePicks();
        getNextPick();
      }
      return true;
    }
    else {
      trace("operator can not reverse pick");
      return false;
    }
  }

  /*
   * Handles all unreserving logic
   */
  public void unreserveOperatorTasks() {
    unreserveCarton();
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
        put(interpretPhrase("LOCATION"), getParam(VictoryParam.alias));
        put(interpretPhrase("ITEM"), displayItem);
        put(interpretPhrase("QTY"), getIntParam(VictoryParam.qty) + " " + getParam(VictoryParam.uomPhrase));
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
   * Determines if a scanned carton is valid.
   */
  protected int isValidCarton(String scan) {
    Map<String, String> cartonInfo = db.getRecordMap(
      "SELECT * FROM rdsCartons " +
      "WHERE lpn='%s' ORDER BY cartonSeq DESC LIMIT 1", scan
    );
    if(!exists(cartonInfo)) {
      alert("Scanned carton [%s] not found", scan);
      return 0; 
    }

    String lpn = getMapStr(cartonInfo, "lpn");
    int cartonSeq = getMapInt(cartonInfo, "cartonSeq");
    String orderId = getMapStr(cartonInfo, "orderId");
    String reservedBy = getMapStr(cartonInfo, "reservedBy");
    boolean releaseStamp = exists(getMapStr(cartonInfo, "releaseStamp"));
    boolean pickStamp = exists(getMapStr(cartonInfo, "pickStamp"));
    boolean packStamp = exists(getMapStr(cartonInfo, "packStamp"));
    boolean completeStamp = exists(getMapStr(cartonInfo, "completeStamp"));
    boolean cancelStamp = exists(getMapStr(cartonInfo, "cancelStamp"));

    if(exists(reservedBy) && !reservedBy.equals(getOperatorID())) {
      alert("Carton [%s] seq [%d] is reserved to another operator [%s]", lpn, cartonSeq, reservedBy);
      return -1;
    }
    if(!releaseStamp) {
      alert("Carton [%s] seq [%d] is not released for picking yet", lpn, cartonSeq);
      return -2;
    }
    if(pickStamp || packStamp || completeStamp) {
      alert("Carton [%s] seq [%d] has completed picking", lpn, cartonSeq);
      return -3;
    }
    if(cancelStamp) {
      alert("Carton [%s] seq [%d] has been canceled", lpn, cartonSeq);
      return -4;
    }

    //Do a quick calculation on if there are any picks in the current zone for container
    int numPicks = db.getInt(-1,
      "SELECT COUNT(*) FROM rdsPicks p " +
      "JOIN rdsCartons c USING (cartonSeq) " +
      "JOIN custOrderLines ol USING (orderLineSeq)  " +
      "JOIN rdsLocations l USING (location)  " +
      "JOIN rdsPickAreas pa USING (`area`, aisle, bay)  " +
      "WHERE cartonSeq=%d AND pa.zone='%s' " +
      "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0",
      cartonSeq, getParam("zone")
    );
    if(numPicks < 1) {
      alert("Carton [%s] seq [%d] has no picks in zone [%s]", lpn, cartonSeq, getParam("zone"));
      return -5;
    }

    setParam(VictoryParam.cartonLpn, lpn);
    setParam(VictoryParam.cartonSeq, cartonSeq);
    setParam(VictoryParam.orderId, orderId);

    reserveCarton(cartonSeq);
    inform("Operator reserved carton [%s] seq [%d] to start picking", lpn, cartonSeq);

    return 1;
  }

  /*
   * Reserves a container to an operator.
   */
  protected boolean reserveCarton(int cartonSeq) {
    try {
      lock("reserve");
      db.execute("UPDATE rdsCartons SET reservedBy='%s' WHERE cartonSeq=%d ", getOperatorID(), cartonSeq);
    }
    catch(Exception e) {
      alert("Could not reserve the container");
      unreserveCarton();
      //Set the operator to the startScreen to clear all params and reset them.
      setNextScreen(new StartScreen(app));
      return false;
    }
    finally {
      unlock("reserve");
    }
    AbstractDAO.postLog("cartonSeq", getParam(VictoryParam.cartonSeq), "repackPicking", 
      String.format("Operator %s starting picking", getOperatorID())
    );
    AbstractDAO.postLog("orderId", getParam(VictoryParam.orderId), "repackPicking", 
      String.format("Operator %s starting picking %s", getOperatorID(), getParam(VictoryParam.cartonLpn))
    );
    //Assign the container to the alphanumeric slot to keep track of for put confirmations
    assignContainerToSlot(cartonSeq, getParam(VictoryParam.containerPosition));
    //Increment the container slot A, B, C...
    setParam(VictoryParam.containerPosition, "" + (char) (1 + getParam(VictoryParam.containerPosition).charAt(0)));
    return true;
  }
  
  /*
   * Unreserves a carton to an operator
   */
  protected void unreserveCarton() {
    db.execute("UPDATE rdsCartons SET reservedBy='', cartSlot='' WHERE reservedBy='%s'", getOperatorID());
  }

  /*
   * Assigns a container to a "cartSlot" to keep track of it's alphanumeric position.
   */
  protected void assignContainerToSlot(int cartonSeq, String position) {
    db.execute(
      "UPDATE rdsCartons SET cartSlot='%s' " +
      "WHERE cartonSeq=%d", position, cartonSeq
    );
  }

  /*
   * Determine the next pick for the operator.
   */
  protected boolean getNextPick() {
    String operatorId = getOperatorID();
    String oldLocation = getParam(VictoryParam.location);
    String oldSKU = getParam(VictoryParam.item);
    String oldAisleBay = getParam(VictoryParam.aisleBay);

    Map<String, String> nextPick = findNextPick();
    if (nextPick == null || nextPick.isEmpty()) {
      inform("Picking complete");
      unreserveOperatorTasks();
      clearParam("containerPosition");
      setPrefixPhrase("PICKING_COMPLETE");
      setParam(VictoryParam.showComplete, "true");
      setNextScreen(new ReadyScreen(app));
      return true;
    }

    String orderId = getMapStr(nextPick, "orderId");
    String customerNumber = getMapStr(nextPick, "customerNumer");
    int cartonSeq = getMapInt(nextPick, "cartonSeq");
    String containerPosition = getMapStr(nextPick, "cartSlot");
    int eachQty = getMapInt(nextPick, "eachQty");
    int shelfPackQty = getMapInt(nextPick, "shelfPackQty");
    String sku = getMapStr(nextPick, "sku");
    String uom = getMapStr(nextPick, "uom");
    String qroFlag = getMapStr(nextPick, "qroFlag");
    String lpn = getMapStr(nextPick, "lpn");
    String aisle = getMapStr(nextPick, "aisle");
    String bay = getMapStr(nextPick, "bay");
    String slot = getMapStr(nextPick, "slot");
    String shelf = getMapStr(nextPick, "shelf");
    String location = getMapStr(nextPick, "location");
    int walkSequence = getMapInt(nextPick, "walkSequence");
    String checkDigits = getMapStr(nextPick, "checkDigits");
    String alias = getMapStr(nextPick, "alias");
    String aisleBay = String.format("%s%s", aisle, bay);

    setParam(VictoryParam.orderId, orderId);
    setParam(VictoryParam.customerNumber, customerNumber);
    setParam(VictoryParam.item, sku);
    setParam(VictoryParam.uom, uom);
    setParam(VictoryParam.qroFlag, qroFlag);
    setParam(VictoryParam.eachQty, eachQty);
    setParam(VictoryParam.shelfPackQty, shelfPackQty);
    setParam(VictoryParam.cartonSeq, cartonSeq);
    setParam(VictoryParam.cartonLpn, lpn);
    setParam(VictoryParam.containerPosition, containerPosition);
    setParam(VictoryParam.aisle, aisle);
    setParam(VictoryParam.bay, bay);
    setParam(VictoryParam.slot, slot);
    setParam(VictoryParam.shelf, shelf);
    setParam(VictoryParam.location, location);
    setParam(VictoryParam.walkSequence, walkSequence);
    setParam(VictoryParam.locationCheckDigits, checkDigits);
    setParam(VictoryParam.alias, alias);
    setParam(VictoryParam.aisleBay, aisleBay);

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
    
    /*
    if( qroFlag.equalsIgnoreCase("x") ) {
   	 int qty = pickQty / shelfPackQty;
   	 inform("convert %d %s/%s to %d shelfPack with shelfPackQty %d", pickQty, sku, uom, qty, shelfPackQty);
   	 setParam(VictoryParam.qty, qty);
    } else {
   	 setParam(VictoryParam.qty, pickQty);
    }*/

    setParam(VictoryParam.qty, pickQty);
    //Need to determine the "wording" of the picking prompt
    determinePickingWording(qroFlag,true);

    /*
     * If the operator is reverse picking, 
     * force new location confirmation on edge case of there
     * not being another pick and operator "skips" location checks
    */
    if (!aisleBay.equals(oldAisleBay) || getParam(VictoryParam.reversePicking).equals("true")) {
      this.setNextScreen(new GoToBayScreen(app));
    } else if (!oldLocation.equals(location)) {
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
    String cartonSeqs = getCartonSeqListString();
    String orderBy = determinePickingOrder();
    map = db.getRecordMap(
      "SELECT p.orderId, p.cartonSeq, p.sku, p.uom, " +
      "customerNumber, FLOOR(ol.qty) AS eachQty, ol.shelfPackQty, " +
      "cs.qroFlag, " +
      "c.lpn, c.cartSlot, " +
      "l.area AS department, l.aisle, l.bay, l.row AS slot, pa.zone, l.alias, " +
      "l.column AS shelf, l.location, l.barcode AS locationBarcode, l.walkSequence, l.checkDigits " +
      "FROM rdsCartons c " +
      "JOIN custOrders o USING (orderId) " +
      "JOIN rdsPicks p USING (cartonSeq) " +
      "JOIN custOrderLines ol USING (orderLineSeq) " +
      "JOIN rdsLocations l USING (location) " +
      "JOIN rdsPickAreas pa USING (`area`, aisle, bay) " +
      "JOIN custSkus cs ON p.sku=cs.sku AND p.baseUom=cs.uom " +
      "WHERE c.cartonSeq IN (%s) AND pa.zone='%s' " +
      "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 " +
      "AND (p.pickOperatorId='%s' OR p.pickOperatorId='') " +
      "ORDER BY %s LIMIT 1",
      cartonSeqs, getParam("zone"), getOperatorID(), orderBy
    );
    if (map == null || map.isEmpty()) {
      inform("Could not find any more picks for cartons [%s]", cartonSeqs);
    }
    return map;
  }

  /*
   * Determines the order by clause of standard or reverse order for picking.
   */
  protected String determinePickingOrder() {
    boolean reverseOrder = getParam(VictoryParam.reversePicking).equals("true");
    if(reverseOrder) {
      return String.format(
        "CASE " +
        "WHEN l.walkSequence <= %d THEN l.walkSequence " +
        "ELSE l.walkSequence - (SELECT MIN(walkSequence) FROM rdsLocations) "+
        "END DESC", getIntParam(VictoryParam.walkSequence)
      );
    }
    //Standard ordering of picks >= current walk seq otherwise, 
    //wrap around to beginning from 0 (or what is smallest)
    else {
      return String.format(
        "CASE " +
        "WHEN l.walkSequence >= %d THEN l.walkSequence " +
        "ELSE l.walkSequence + (SELECT MAX(walkSequence)+1 FROM rdsLocations) " +
        "END ASC", getIntParam(VictoryParam.walkSequence)
      );
    }
  }

  /*
   * Gets list of carton sequences that the operator has reserved.
   */
  protected String getCartonSeqListString() {
    List<String> cartonSeqs = db.getValueList(
        "SELECT cartonSeq FROM rdsCartons " +
        "WHERE reservedBy='%s' AND pickStamp IS NULL " +
        "AND packStamp IS NULL AND shipStamp IS NULL", getOperatorID()
    );
    String cartonSeqList = cartonSeqs.toString();
    return cartonSeqList.substring(1, cartonSeqList.length()-1);
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