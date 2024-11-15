package victoryApp;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonObject;

import dao.AbstractDAO;
import dao.SloaneCommonDAO;
import static sloane.SloaneConstants.*;
import rds.RDSUtil;

public abstract class AbstractSloaneScreen extends AbstractVictoryScreen {

  public AbstractSloaneScreen(VictoryApp app) {
    super(app);
    SloaneCommonDAO.setDatabase(db);
    AbstractDAO.setDatabase(db);
  }

  public AbstractSloaneScreen(VictoryApp app, String background) {
    super(app, background);
    SloaneCommonDAO.setDatabase(db);
  }

  @Override
  protected void sendDelayLogoutRequest( String operatorId, int sessionId ) {
    JsonObject json = new JsonObject();
    json.addProperty("sessionId", sessionId);
    db.execute("INSERT INTO status SET appName='status', statusType='delayLogout', data='%s', operator='%s' ,status='idle'", json.toString(), operatorId); 
  }

  /*
   * Unreserves non-completed picks from an operator
   */
  protected void unreservePicks() {
    db.execute(
      "UPDATE rdsPicks SET pickOperatorId='' " +
      "WHERE pickOperatorId='%s' AND readyForPick=1 " +
      "AND picked=0 AND shortPicked=0 AND canceled=0", 
      getOperatorID()
    );
  }

  /*
   * Determines if the carton is complete.
   */
  protected boolean checkCartonComplete(int cartonSeq) {
    int numOpenPicks = db.getInt(-1,
      "SELECT COUNT(*) FROM rdsPicks AS p " +
      "JOIN custOrderLines USING (orderLineSeq) " +
      "JOIN rdsLocations AS l USING (location) " +
      "WHERE cartonSeq = %d " +
      "AND p.readyForPick = 1 AND p.picked = 0 AND p.shortPicked = 0 AND p.canceled = 0 ",
      cartonSeq
    );
    return (numOpenPicks == 0);
  }

  /*
   * Determines if a QRO flag can let a picker break a shelf pack or not.
   * If the QRO flag is set to X, then can NOT break pack
   * If QRO flag is anything else, then CAN break pack
   */
  protected void determineQRO(String qroFlag, String sku, String uom) {
    if(qroFlag.equalsIgnoreCase("x")) {
      trace("QRO set to [X] sku [%s] uom [%s], operator can NOT break shelf packs", sku, uom);
      setParam("breakPacks", "false");
    } else if(!uom.equals(UOM_SHELFPACK)) {
   	 trace("sku [%s] is picked as [%s], no need to break shelf packs", sku, uom);
   	 setParam("breakPacks", "false");
    }
    else {
      trace("QRO set to [%s] sku [%s] uom [%s], operator CAN break shelf packs", qroFlag, sku, uom);
      setParam("breakPacks", "true");
    }
  }

  /*
   * Determines the "wording" of the picking prompt:
   * If the item is an "X" qro -> "tell operator number of eaches in shelf pack, then total eaches required"
   * Else -> "normal" wording
   */
  protected void determinePickingWording(String qroFlag, boolean fromPut) {
    if(qroFlag.equalsIgnoreCase("x")) {
      trace("QRO set to [X] setting 'X' prompt wording");
      //X to <shelfPackQty> Pick <eachQty> each
      if( fromPut )
      	setParam("pickWording", replaceParams(interpretPhrase("PICK_QTY_UOM_X"), false));
      else
      	setParam("pickWording", replaceParams(interpretPhrase("PICKED_QTY_PICK_QTY_UOM_X"), false));
    }
    else {
      //Pick <qty> <uomPhrase>
   	 if( fromPut )
   		 setParam("pickWording", replaceParams(interpretPhrase("PICK_QTY_UOM"), false));
   	 else
   		 setParam("pickWording", replaceParams(interpretPhrase("PICKED_QTY_PICK_QTY_UOM"), false));
    }
  }

  /*
   * Gets the picking qty for a cartonSeq of a sku, uom
   */
  protected int getPickQty(int cartonSeq, String sku, String uom, String location) {
    return db.getInt(0, 
      "SELECT COUNT(*) FROM rdsPicks p " +
      "JOIN custOrderLines ol USING(orderLineSeq) " +
      "WHERE p.cartonSeq=%d AND p.sku='%s' AND p.uom='%s' AND ol.location='%s' " +
      "AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0  ",
      cartonSeq, sku, uom, location
    );
  }

  /*
   * Gets the spoken phrase for a uom.
   * Do not strip the (potentially) extra stuff if there is a slash
   * Ex: 1/2GL, keep as normal
   */
  protected String getUomPhrase(String uom) {
    //Handle fractional uoms as normal
    if(uom.contains("/")) {
      return getPhraseForUom(uom);
    }
    String baseUom = uom.split("(\\d)")[0];
    String phrase = getPhraseForUom(baseUom);
    int convertedNumber = extractNumber(uom) * getRomanNumeralValue(uom);
    String output = phrase;
    if(convertedNumber > 0) {
      output += " " + convertedNumber;
    }
    return output;
  }

  /*
   * Determine the phrase for the uom.
   */
  private String getPhraseForUom(String uom) {
    return db.getString("", 
      "SELECT phrase FROM cfgUomToPhrase " +
      "WHERE uom = '%s'", uom
    );
  }

  /*
   * Gets the optional number inside uom.
   */
  private static int extractNumber(String uom) {
    String numberPart = uom.replaceAll("[^0-9]", "");
    return numberPart.isEmpty() ?  0 : Integer.parseInt(numberPart);
  }

  /*
   * Gets the optional roman numeral at the end of uom.
   */
  private static int getRomanNumeralValue(String uom) {
    String romanNumeral = uom.substring(uom.length()-1);
    switch (romanNumeral) {
      case "M": return  1000;
      case "L": return  50;
      case "C": return  100;
      default: return  1;
    }
  }

  /*
   * Determines if a scan is a valid Aisle location.
   */
  protected boolean isValidAisleScan(String scan) {
    String aisle = db.getString("", 
      "SELECT aisle FROM rdsLocations " +
      "WHERE location='%s'", scan
    );
    if(aisle.equals(getParam("aisle"))) {
      return true;
    }
    return false;
  }

  /*
   * Determines if a scan is a valid Bay location.
   */
  protected boolean isValidBayScan(String scan) {
    String bay = db.getString("", 
      "SELECT bay FROM rdsLocations " +
      "WHERE location='%s'", scan
    );
    if(bay.equals(getParam("bay"))) {
      return true;
    }
    return false;
  }
  
  /*
   * Determines if a scan is a valid UPC barcode.
   */
  protected boolean isValidUpcScan(String scan) {
	  String sku = getParam("item");
	  if( sku.equals(scan) ) return true;
	  Map<String,String> m = SloaneCommonDAO.getSkuFromUPC(scan);
	  if( m==null || m.isEmpty() ) return false;
	  return ( sku.equals(getMapStr(m,"sku")));
  }   

  /*
   * Determines if a location check digit is valid.
   * Will check if the control param is set to first or last 2 digits.
   */
  protected boolean isValidShelfSlotCheckDigit(String text) {
    String type = getCheckDigitPositions();

    String checkDigitString = getParam("locationCheckDigits");

    if(text.length() != 2) {
      trace(
        "Provided checkDigit [%s] for location [%s] is not 2 characters long, trying as barcode scan", 
        checkDigitString, getParam("location")
      );
      return false;
    }
    if(type.equals("first")) {
      if(text.equals(checkDigitString.substring(0, 2))) {
        trace("Check digit validated using first 2");
        return true;
      }
      alert(
        "Provided checkDigit [%s] for location [%s] using first 2 is invalid, expected [%s], got [%s]", 
        checkDigitString, getParam("location"), checkDigitString.substring(0, 2), text
      );
      return false;
    }
    if(text.equals(checkDigitString.substring(checkDigitString.length() - 2))) {
      trace("Check digit validated using last 2");
      return true;
    }
    alert(
      "Provided checkDigit [%s] for location [%s] using last 2 is invalid, expected [%s], got [%s]", 
      checkDigitString, getParam("location"), checkDigitString.substring(checkDigitString.length() - 2), text
    );
    return false;
  }

  /*
   * Determines if the operator needs to confirm the first or last 2 digits for check digits.
   * Handles cases where the control param is set to some other value or doesn't exist.
   */
  protected String getCheckDigitPositions() {
    String type = db.getControl("victory", "checkDigits", "first");
    if(!type.equalsIgnoreCase("first") && !type.equalsIgnoreCase("last")) {
      type = "first";
    } else 
   	type = type.toLowerCase();
    return type;
  }

  /*
   * Determines if a location barcode is valid.
   */
  protected boolean isValidShelfSlotBarcode(String text) {
	 String location = db.getString("", "SELECT location FROM rdsLocations WHERE barcode='%s'", text);
	 if( location.equals(getParam("location")) )
		 return true;
	 alert("Invalid location barcode got [%s] expected [%s]",text, location);
	 return false;
  }

  /*
   * Determines if a scan UPC validation is required for a pick.
   */
  protected boolean scanUPCRequired(String operatorId, String sku, String customerNumber) {
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());
    Double randomNumber = random.nextDouble(1);
    String probability = db.getString("",
      "SELECT MAX(upcScanProbability) AS upcScanProbability " +
      "FROM ( " +
      "  SELECT upcScanProbability FROM proOperators " +
      "  WHERE operatorID = %s " +
      "  UNION ALL " +
      "  SELECT upcScanProbability FROM custSkus " +
      "  WHERE sku = '%s' " +
      "  UNION ALL " +
      "  SELECT upcScanProbability FROM custCustomers " +
      "  WHERE customerNumber = '%s' " +
      ") AS upc", operatorId, sku, customerNumber
    );

    Double upcScanProbability = Double.valueOf(probability);
    trace("Random probability [%.2f] Scan probability [%.2f]",randomNumber, upcScanProbability);
    
    if(randomNumber < upcScanProbability) {
      inform("UPC scan required by probability");
      recordAction("upcScanRequired", 1);
      return true;
    }
    return false;
  }

  /*
   * Confirms a slot is empty, for Sloane this is similar to short pick.
   */
  protected boolean confirmSlotEmpty(String code) {
    int requiredQty = getIntParam("qty");
    String uom = getParam("uom");
    String sku = getParam("item");
    String location = getParam("location");
    String operatorID = getOperatorID();
    if (sku.isEmpty() || requiredQty <= 0) {
      alert("invalid status, can not confirm short");
      return false;
    }
    List<String> pickSeqs = db.getValueList(
      "SELECT pickSeq FROM rdsPicks p " +
      "JOIN custOrderLines ol USING(orderLineSeq) " +
      "WHERE p.cartonSeq=%s AND p.sku='%s' AND p.uom='%s' AND ol.location='%s' " +
      "AND p.pickOperatorId='%s' AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 ",
      getIntParam("cartonSeq"), sku, uom, location, operatorID, requiredQty
    );
    for (String pickSeqStr : pickSeqs) {
      int pickSeq = RDSUtil.stringToInt(pickSeqStr, -1);
      SloaneCommonDAO.confirmShort(pickSeq, getOperatorID());
    }
    //History messages for carton and order
    AbstractDAO.postLog("cartonSeq", getParam("cartonSeq"), code,
      String.format("Operator %s confirmed slot empty for sku %s uom %s location %s", getOperatorID(), sku, uom, location)
    );
    AbstractDAO.postLog("orderId", getParam("orderId"), code,
      String.format("Operator %s confirmed slot empty for sku %s uom %s location %s", getOperatorID(), sku, uom, location)
    );

    db.execute(
      "INSERT INTO victoryExceptions " +
      "SET sku='%s', uom='%s', location='%s', " +
      "qty=%d, operatorId='%s', reason='%s'",
      getParam("item"), getParam("uom"),
      getParam("location"), getIntParam("qty"),
      getOperatorID(), "Empty slot"
    );

    recordAction("slotEmpty", 1);
    return true;
  }

  /*
   * Confirms breaking of a shelf pack.
   */
  protected boolean confirmBreakShelfPack(String code) {
    int requiredQty = getIntParam("qty");
    String uom = getParam("uom");
    String sku = getParam("item");
    String location = getParam("location");
    String operatorID = getOperatorID();
    if (sku.isEmpty() || requiredQty <= 0) {
      alert("invalid status, can not confirm break pack");
      return false;
    }
    List<String> pickSeqs = db.getValueList(
      "SELECT pickSeq FROM rdsPicks p " +
      "JOIN custOrderLines ol USING(orderLineSeq) " +
      "WHERE p.cartonSeq=%s AND p.sku='%s' AND p.uom='%s' AND ol.location='%s' " +
      "AND p.pickOperatorId='%s' AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 ",
      getIntParam("cartonSeq"), sku, uom, location, operatorID, requiredQty
    );
    for (String pickSeqStr : pickSeqs) {
      int pickSeq = RDSUtil.stringToInt(pickSeqStr, -1);
      SloaneCommonDAO.breakShelfPack(pickSeq,1);
    }
    //History messages for carton and order
    AbstractDAO.postLog("cartonSeq", getParam("cartonSeq"), code,
      String.format("Operator %s broke shelf pack for sku %s uom %s location %s", getOperatorID(), sku, uom, location)
    );
    AbstractDAO.postLog("orderId", getParam("orderId"), code,
      String.format("Operator %s broke shelf pack for sku %s uom %s location %s", getOperatorID(), sku, uom, location)
    );
    recordAction("breakShelfPack", 1);
    return true;
  }

  /*
   * Confirms the picking and putting of an item.
   */
  protected boolean confirmPut(String code) {
	 /*
	 int pickQty = getIntParam("pickQty");
	 int putQty = pickQty;
	 int qtyInEach = putQty;
	 String qroFlag = getParam("qroFlag");
	 if( qroFlag.equalsIgnoreCase("x") ) {
		 int shelfPackQty = getIntParam("shelfPackQty");
		 qtyInEach = shelfPackQty * pickQty;
	 }*/
	 int putQty = getIntParam("pickQty");
    int requiredQty = getIntParam("qty");
    String uom = getParam("uom");
    String sku = getParam("item");
    String location = getParam("location");
    String operatorID = getOperatorID();
    if (sku.isEmpty() || putQty <= 0) {
      alert("invalid status, can not confirm put");
      return false;
    }
    List<String> pickSeqs = db.getValueList(
      "SELECT pickSeq FROM rdsPicks p " +
      "JOIN custOrderLines ol USING(orderLineSeq) " +
      "WHERE p.cartonSeq=%s AND p.sku='%s' AND p.uom='%s' AND ol.location='%s' " +
      "AND p.pickOperatorId='%s' AND p.readyForPick=1 AND p.picked=0 AND p.shortPicked=0 AND p.canceled=0 " +
      "LIMIT %d",
      getIntParam("cartonSeq"), sku, uom, location, operatorID, putQty
    );
    for (String pickSeqStr : pickSeqs) {
      int pickSeq = RDSUtil.stringToInt(pickSeqStr, -1);
      SloaneCommonDAO.confirmPick(pickSeq, getOperatorID());
    }
    recordAction("pick-"+code+"-"+uom, putQty);
    inform("%d SKU/UOM %s/%s confirmed put", putQty, sku, uom);
    int remainingQty = requiredQty - putQty;
    setParam("qty", remainingQty);

    //Set history for the current cart if applicable
    if(code.equals("cartPicking")) {
      AbstractDAO.postLog("cartSeq", getParam("cartSeq"), code,
        String.format("Operator %s picked %d of sku %s uom %s location %s", getOperatorID(), putQty, sku, uom, location)
      );
    }
    //History messages for carton and order
    AbstractDAO.postLog("cartonSeq", getParam("cartonSeq"), code,
      String.format("Operator %s picked %d of sku %s uom %s location %s", getOperatorID(), putQty, sku, uom, location)
    );
    AbstractDAO.postLog("orderId", getParam("orderId"), code,
      String.format("Operator %s picked %d of sku %s uom %s location %s", getOperatorID(), putQty, sku, uom, location)
    );
    recordAction("pick-"+code+"-"+uom, putQty);

    //Need to recalculate the "wording" so the qty will update on screen if they pick under required
    /*
    if(remainingQty > 0) {
      determinePickingWording(getParam("qroFlag"));
    }*/
    return true;
  }

}