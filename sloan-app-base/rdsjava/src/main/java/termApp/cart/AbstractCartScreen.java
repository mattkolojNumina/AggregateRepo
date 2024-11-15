package termApp.cart;

import rds.*;
import term.TerminalDriver;
import termApp.*;
import termApp.util.*;
import termApp.util.InfoBox.InfoBoxConstructor;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import rds.RDSUtil;

import static sloane.SloaneConstants.*;

import termApp.util.TermActionObject.OnActionListener;

import java.util.*;

import dao.SloaneCommonDAO;

public class AbstractCartScreen extends AbstractProjectAppScreen {   
   protected InfoBox rightInfo, leftInfo;
   protected TermGroup lines;
   private String startScreen;

   private static final boolean TERM_OPERATORS = false;

   protected static final int INVALID_FORMAT = -1;
   protected static final int DUPLICATE = -2;
   protected static final int NOT_FOUND = -3;
   protected static final int ASSIGNED = -4;
   protected static final int ASSIGNED_TOTE = -9;
   protected static final int UNEXPECTED_LPN = -5;
   protected static final int LPN_SMALL = -6;
   protected static final int LPN_MEDIUM = -7;
   protected static final int LPN_LARGE = -8;
   protected static final int GOOD_LPN = 1;
   protected static final int IS_TOTE = 2;
   protected static final  String CART_REGEX = "[C]\\d{3,4}";
   //protected static final String TOTE_REGEX = "T\\d{6}";
   protected static final String CART_LEFT = "LEFT";
   protected static final String CART_RIGHT = "RIGHT";
   protected static final String CART_BOTH = "LEFTRIGHT";
   protected static final String MODE_CONV = "convCart";
   

   public static enum TermParam {
      cartSeq,
      cartID,
      cartStatus,
      barcode, 
      cartSide,
      cartType,
   };

   public AbstractCartScreen(TerminalDriver term) {
      super(term);
      reloadParams();
      setAllDatabase();
      startScreen = "cart.IdleScreen";
   }

   protected void reloadParams() {
   }
   
   protected void loadGlobals() {
      setCart(getIntParam(TermParam.cartSeq));
   }

   protected String getOperatorId( String barcode ) {
   	return db.getString("", "SELECT operatorId FROM proOperators WHERE barcode='%s'", barcode);
   }

   protected void logout() {
      // log the operator off the current terminal
      if (TERM_OPERATORS)
         db.execute(
            "UPDATE termOperators SET " +
            "operatorID = '', " +
            "logoutAllowed = 'false', " +
            "autoLogout = 'false' " +
            "WHERE terminal = '%s'",
            term.getTermName() );
      
      String operatorId = getOperatorId();
      if (operatorId == null || operatorId.isEmpty())
         return;

      String task = getStationTask();
      String area = getStationArea();

      // log the operator off from the current station task/area
      db.execute(
            "UPDATE proOperators SET " +
            "task = '', " +
            "area = '', " +
            "terminal = '' " +
            "WHERE operatorID = '%s' " +
            "AND task = '%s' " +
            "AND area = '%s' " +
            "AND terminal = '%s'",
            operatorId, task, area, term.getTermName() );
      db.execute(
            "UPDATE proOperatorLog SET " +
            "endTime = NOW() " +
            "WHERE operatorID = '%s' " +
            "AND task = '%s' " +
            "AND area = '%s' " +
            "AND endTime IS NULL",
            operatorId, task, area );

      setParam("operatorId","");
      trace( "logout [%s]", operatorId );
   }
   
   protected void doOkay() {
      inform( "scan [%s] ignored");
   }
   
   protected OnActionListener okAction() {
      return new OnActionListener() {
         public void onAction() {
            doOkay();
         }
      };
   }
   
   protected void doConfirm() {
      inform( "scan [%s] ignored");
   }
   
   protected OnActionListener confirmAction() {
      return new OnActionListener() {
         public void onAction() {
            doConfirm();
         }
      };
   }

   protected OnActionListener modeAction() {
      return new OnActionListener() {
            public void onAction() {
         	logout();
            inform("switch station mode");
            setNextScreen("ChangeModeScreen");
         }
      };
   }   

   protected void doCancel() {
      trace("cancel");
      setNextScreen("cart.IdleScreen");

   }

   protected OnActionListener cancelAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doCancel();
         }
      };
   }

   protected TextWrap initMsg() {
      TextWrap msg = new TextWrap(MSG_MARGIN, MSG_Y, 1750, FONT_L, 2);
      msg.show();
      return msg;
   }
   
   protected Map<String, String> getCartMap( String cartID ) {
   	return db.getRecordMap( 
   			"SELECT * FROM rdsCarts WHERE cartID = '%s' ORDER BY cartSeq DESC LIMIT 1", cartID);
   }    

   protected String cartFromBarcode(String barcode) {
      if( barcode == null || barcode.isEmpty() ) {
         return "";
      }

       if(barcode.matches(CART_REGEX)) {
         return barcode;
      }
      
      return barcode.substring(0, barcode.length() - 1);
   }

   protected String slotFromBarcode(String barcode) {
       if( barcode == null || barcode.isEmpty() ) {
         return "";
      }

      return barcode.substring( barcode.length() - 1);
   }
   
   protected boolean isValidCart( String barcode ) {
   	return db.getInt(0, "SELECT COUNT(*) FROM cfgCarts WHERE cartID='%s' AND enabled = 1 AND barcode = '%s'", barcode, barcode)>0;
   }
   
   protected int getCartSeq( String barcode ) {
      return db.getInt(-1, "SELECT cartSeq FROM rdsCarts WHERE cartID = '%s' ORDER BY cartSeq DESC LIMIT 1", barcode);
   }

   protected String getCartName(int cartSeq) {
      return db.getString("NONE", "SELECT cartID FROM rdsCarts WHERE cartSeq=%d", cartSeq);
   }
   
   protected String getCartMessageStatus( String barcode ) {
   	return db.getString(null, 
   			"SELECT status FROM cartMessages " + 
	         "WHERE cartID = '%s' " +
	         "ORDER BY seq DESC " +
	         "LIMIT 1", barcode); 
   }

   protected String getCartStatus( int cartSeq ) {

      Map<String,String> m = getCartMap( getCartName(cartSeq) );
      String createStamp = getMapStr(m, "createStamp");
      String buildStamp = getMapStr(m, "buildStamp");
      String completeStamp = getMapStr(m, "completeStamp");
      String errorStamp = getMapStr(m, "errorStamp");

      //inform("Checking status for cart [%d]", cartSeq);

      if (!errorStamp.equals(""))
        return "idle";

      //cart ready and waiting for new assignment
      if (!completeStamp.equals(""))
        return "idle";

      //cart in process of building at buildScreen
      if (!createStamp.equals("") && buildStamp.equals(""))
         return "building";

      //cart is ready for cartBuild process
      if (createStamp.equals("") && buildStamp.equals(""))
        return "ready";
        
      //cart has been released for picking
      return "released";
   }

   protected int availableCartons(String cartType) {
      if (cartType.equals(CARTTYPE_PQ)) {
   	return db.getInt(0, "SELECT COUNT(*) FROM rdsCartons "
   			+ "WHERE pickType IN ('%s', '%s')  "
   			+ "AND cartSeq=-1 AND cartSlot IS NULL AND assigned IS NULL AND lpn IS NULL "
            + "AND rdsCartons.releaseStamp IS NOT NULL AND rdsCartons.cancelStamp IS NULL", 
            PICKTYPE_PERISHABLES, PICKTYPE_LIQUIDS);
      }
      else { 
         return db.getInt(0, "SELECT COUNT(*) FROM rdsCartons JOIN custOrders USING(orderID) "
            + "WHERE pickType IN ('%s') "
            + "AND cartSeq=-1 AND cartSlot IS NULL AND assigned IS NULL And lpn IS NULL "
            + "AND rdsCartons.releaseStamp IS NOT NULL AND rdsCartons.cancelStamp IS NULL",
            PICKTYPE_AERSOLBOOM);
   }

      
   }
   
   protected boolean isConveyorBuildCart() {
   	String cartID = getStrParam(TermParam.cartID);
   	return db.getInt(1, "SELECT isConveyorBuild FROM rdsCarts WHERE cartID='%s'", cartID)>0;
   }
   
   protected void setCart(int cartSeq) {
      if (cartSeq > 0) {
         String cartID = db.getString("", "SELECT cartID from rdsCarts WHERE cartSeq = %s", cartSeq);
         String status = getCartStatus(cartSeq);
         //String cartType = getCartType(cartSeq);
         trace("set cartSeq[%d] cartID[%s]", cartSeq, cartID);
         setParam(TermParam.cartID,cartID);
         setParam(TermParam.cartStatus,status);
         setParam(TermParam.cartSeq,""+cartSeq);
         //setParam(TermParam.cartType, cartType);
      } else {
         inform("cleared cart params");
         clearCartParam();
      } 
   }

   protected int createCart(String barcode) {
      String cartType = getStrParam(TermParam.cartType);
      int count = db.execute(
            "INSERT INTO rdsCarts (cartID, cartType) " +
            "VALUES ('%s','%s')",
            barcode, cartType );
      if (count != 1) {
         alert("error creating cart record in rdsCarts.");
         return -1;
      }
      int seq = db.getSequence();
      trace("created cart message record seq[%d] cartID [%s]", seq, barcode);
      return seq;
	}

   protected List<Map<String, String>> getAssignedCartonsList(int cartSeq) {
      return db.getResultMapList(
            "SELECT lpn as LPN, " + 
            "cartonType AS size, " + 
            "IF(assigned IS NULL, '', cartSlot) AS slot, " + 
            "IF(assigned IS NULL, '', lpn) AS lpn, " +
            "IF(assigned IS NULL, 'gray', 'green') AS background " + 
            "FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "ORDER BY assigned DESC, cartSlot ASC",
             cartSeq); 
   }   

   protected List<Map<String, String>> getAssignedCartonsListNoColor(int cartSeq) {
      return db.getResultMapList(
            "SELECT lpn as LPN, " + 
            "cartonType AS size, " + 
            "IF(assigned IS NULL, '', cartSlot) AS slot, " + 
            "IF(assigned IS NULL, '', lpn) AS lpn " +
            "FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "ORDER BY assigned DESC, cartSlot ASC",
             cartSeq); 
   } 
   
   //Assigned to cart in cartBuild process, and put onto cart/ scanned by operator
   protected int getBuildCount() {
   	int cartSeq = getIntParam(TermParam.cartSeq);
      return db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "AND assigned IS NOT NULL", 
            cartSeq);
   }
   
   protected int getBuildCount(int cartSeq) {
      return db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "AND assigned IS NOT NULL", 
            cartSeq);
   }

   //Assigned to cart by cartBuild process, but not yet put on/scanned by operator so assigned value in db should not be set
   protected int assignedCartonCount(int cartSeq) {
      return db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = '%d' " +
            "AND assigned IS NULL",
            cartSeq);    
   }

   protected String getErrorMsg(int cartSeq) {
      return db.getString(null, "SELECT errorMsg FROM rdsCarts WHERE cartSeq = %d", cartSeq);
   }

   protected void clearErrorMsg(int cartSeq) {
      db.execute("UPDATE rdsCarts SET errorStamp = NULL, errorMsg = NULL");
   }
   
   protected boolean nextIsTote(int cartSeq) {
   	int toteCount = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = %d " +
   			"AND cartonType IN ('singlesTote','batchTote') " +
            "AND assigned IS NULL",
            cartSeq);
   	int cartonCount = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = %d " +
   			"AND cartonType = 'splitCase' " +
            "AND assigned IS NULL",
            cartSeq);
   	return toteCount>0 && cartonCount<0;
   }

   
   protected int getNextCarton(String lpn, int cartSeq) {
      int seq = db.getInt(-1, "SELECT cartonSeq FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "AND cartonType = '%s' " +
            "AND assigned IS NULL " +
            "ORDER BY cartSlot ASC " +
            "LIMIT 1", cartSeq, lpn);

      if (seq < 0) {
         alert("failed to get next carton for cartSeq [%d]", cartSeq);
      }
      return seq;
   }

   protected String getNextCartonType(int cartSeq) {
      String cartonType = db.getString("", "SELECT cartonType FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "AND assigned IS NULL " +
            "ORDER BY cartSlot ASC " +
            "LIMIT 1", cartSeq);

      if (cartonType.equals("")) {
         alert("failed to get cartType for cartSeq [%d]", cartSeq);
      }
      return cartonType;
   }   

   protected int getNextBuildTote(int cartSeq) {
      int seq = db.getInt(-1, "SELECT cartonSeq FROM rdsCartons AS c " +
            "LEFT OUTER JOIN rdsCarts AS pcs " +
              "ON pcs.cartSeq=c.cartSeq " +
              //"AND pcs.slot = c.cartSlot " +
            "WHERE c.cartSeq = %d " +
            "AND cartonType IN ('singlesTote','batchTote') " +
            "AND c.assigned IS NULL " +
            //"ORDER BY pcs.shelf ASC, pcs.ordinal ASC, c.cartSlot ASC " +
            "ORDER BY c.cartSlot ASC " +
            "LIMIT 1", cartSeq);
      if (seq < 0) {
         alert("failed to get next tote for cartSeq [%d]", cartSeq);
      }
      return seq;
   }   
   
   protected String getToteType( int cartonSeq ) {
   	String toteType = db.getString("", "SELECT cartonType FROM rdsCartons WHERE cartonSeq=%d", cartonSeq);
   	return "singlesTote".equals(toteType)?"Singles Tote":"Multi Tote";
   }   
   
   protected String getCartonSize( int cartonSeq ) {
   	return db.getString("", "SELECT IF(cartonSize='batchTote', 'multiTote', cartonSize) FROM rdsCartons WHERE cartonSeq=%d", cartonSeq);
   }
   
   protected List<Map<String, String>> getBuildList(int cartSeq, int cartonSeq) {

      return db.getResultMapList(
            "SELECT lpn as LPN, " + 
            "cartonType AS size, " + 
            "IF(assigned IS NULL, '', cartSlot) AS slot, " + 
            "IF(assigned IS NULL, '', lpn) AS lpn, " +
            "IF(cartonSeq = %d, 'yellow', IF(assigned IS NULL, 'gray', 'green')) AS background " + 
            "FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "ORDER BY assigned DESC, cartSlot ASC",
            cartonSeq, cartSeq);    	
   } 

   protected String getSlot(String cartID, String slotBarcode) {
      if (!validateSlot(cartID,slotBarcode, true))
         return null;
      return slotBarcode;
   }
   
   protected boolean validateSlot(String cartID, String slotBarcode, boolean alert) {
      if (slotBarcode == null || slotBarcode.isEmpty())
         return false;
      
      if (slotBarcode.length()>1) {
         if (alert)
            alert("barcode [%s] is not a slot on cart [%s]", slotBarcode, cartID);
         return false;
      }

      inform ("CARTID: [%s] SLOTBARCODE: [%s]", cartID, slotBarcode);
      int count = db.getInt(0, "SELECT COUNT(*) FROM cfgCartSlots WHERE cartId = '%s' AND cartSlot IN ('%s','%s')", cartID, cartID + slotBarcode, slotBarcode );
         return count > 0;
   }
   
   protected boolean isSlotAvailable(int cartSeq, String cartID, String slot, boolean alert) {
      if (slot == null || slot.isEmpty())
         return false;
      String slotBarcode = String.format("%s", slot);

      //gets count of cartons that have been placed onto slot. Function returns true if no slot physically occupying space
      int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons " +
            "WHERE cartSeq = %d " +
            "AND cartSlot = '%s' " +
            "AND assigned IS NOT NULL ",
            cartSeq, slotBarcode);
      if (count < 0) {
         if (alert)
             alert("not a valid slot [%s]  on cartID [%s]", slot, cartID);
         return false; }

      return count == 0 ;
   }
   
    protected void setLPN(int cartonSeq, String cartonLpn) {
       db.execute("UPDATE rdsCartons SET lpn='%s' WHERE cartonSeq=%d", cartonLpn, cartonSeq);
       SloaneCommonDAO.postCartonLog(""+cartonSeq, "cartBuild", "lpn [%s] set", cartonLpn);
    }  

   protected void assignCarton(int cartSeq, String cartID, int cartonSeq, String slot) {
      String slotBarcode = String.format("%s", slot);
      db.execute("UPDATE rdsCartons SET " +
            "cartonSeq = %d " +
            "WHERE cartSeq = %d " +
            "AND slot = '%s'", 
            cartonSeq, cartSeq, slotBarcode );
   } 
   
   protected void assignToCart(int cartSeq, String cartID, int cartonSeq, String slot) {
      String slotBarcode = String.format("%s", slot);
      db.execute("UPDATE rdsCartons SET " +
            "assigned = 1 ," +
            //"cartSeq = '%d', " +
            "cartSlot = '%s' " + 
            "WHERE cartonSeq = %d", 
            slotBarcode, cartonSeq);
            trace("cartonSeq %d", cartonSeq);    
   }   

   //Selects expected lpn for cartBuild based on order of cartSlot ASC. Ignores cartons already marked assigned
   protected String getExpectedLpn(int cartSeq) {
      return db.getString (null, "SELECT lpn from rdsCartons WHERE cartSeq = %d && assigned IS NULL ORDER BY cartSlot ASC", cartSeq);
   }

   //Selects expected lpn for cartBuild based on order of cartSlot ASC. Ignores cartons already marked assigned
   protected String getExpectedSize(int cartSeq) {
      return db.getString (null, "SELECT cartonType from rdsCartons WHERE cartSeq = %d && assigned IS NULL ORDER BY cartSlot ASC", cartSeq);
   }

   protected String getToteRegex() {
      return db.getString(null, "SELECT lpnFormat from cfgCartonTypes where cartonType = '%s'",TYPECLASS_TOTE);
   }

   protected String getCartType(int cartSeq) {
      return db.getString(null, "SELECT cartType from rdsCarts WHERE cartSeq = %d", cartSeq);
   }

   protected String getExpectedRegex(String expectedSize){
      return db.getString(null, "SELECT lpnFormat from cfgCartonTypes WHERE cartonType = '%s'", expectedSize);
   }

   protected List<Map<String, String>> getRegexMap(int cartSeq) {	
   	return db.getResultMapList( 
   			"SELECT cfgCartonTypes.lpnFormat " +
            "FROM rdsCartons " +
            "JOIN cfgCartonTypes ON rdsCartons.cartonType = cfgCartonTypes.cartonType " + 
            "WHERE rdsCartons.cartSeq = %d AND rdsCartons.assigned IS NULL; ", cartSeq);
   }

   protected List<Map<String, String>> getCartonTypeMap() {	
   	return db.getResultMapList( 
   			"SELECT cartonType FROM cfgCartonTypes");
   }

   protected String getRegex (String cartonType) {
      return db.getString("", "SELECT lpnFormat from cfgCartonTypes WHERE cartonType = '%s'", cartonType);
   }

   //Compares lpn at all lpnFormats in rdsCartonTypes, returns true if good match
   protected boolean isValidLpn(String lpn, int cartSeq) {
    List<Map<String, String>> m = getRegexMap(cartSeq);

    for (Map<String, String> regexMap : m) {
        String regex = regexMap.get("lpnFormat");
        //inform("checking if scannedLpn [%s] matches regex[%s]", lpn, regex); 
        if (lpn.matches(regex)) {
            return true;
        }
    }

    return false;
}

   //Compares lpn at all lpnFormats in rdsCartonTypes, returns cartonType if matched
   protected String getCartonType(String lpn) {
      List<Map<String, String>> m = getCartonTypeMap();
  
      for (Map<String, String> cartonTypeMap : m) {
          String cartonType = cartonTypeMap.get("cartonType");
          String regex = getRegex(cartonType);
          //inform("Checking if lpn %s matches cartonType %s", lpn, cartonType);
          if (lpn.matches(regex)) {
              return cartonTypeMap.get("cartonType");
          }
      }
  
      return ("");
  }

   //Checks for duplication, empty string, other generic errors. Calls isValidLpn
   protected int validateLpn(String barcode, boolean alert, List<Map<String, String>> buildList) {
      if (barcode == null || barcode.isEmpty())
         return INVALID_FORMAT;
   
      boolean validLpn = isValidLpn(barcode, getIntParam(TermParam.cartSeq));

      // check length/format
      if ( !validLpn ) {
         if (alert)
            alert("lpn does not have proper length/format");
         return INVALID_FORMAT;
      }

      int cartSeq = getIntParam(TermParam.cartSeq);

      //If barcode matches tote format, skip checking if already assigned.
      if (!barcode.matches(db.getString("", "SELECT lpnFormat from cfgCartonTypes WHERE cartonType = '%s'", TYPECLASS_TOTE) )) {
      	int assignedCount = db.getInt(-1, 
               "SELECT COUNT(*) FROM rdsCartons " +
               "WHERE lpn = '%s' ", barcode,cartSeq);
      	if (assignedCount > 0 ) {
            if (alert)
               alert("carton has been assigned already");
            return ASSIGNED;
         }
      } else {
         if (!isValidToteLpn(barcode)) {
            return ASSIGNED_TOTE;
         }
      }
      return GOOD_LPN;
   }

   //isValidToteLpn returns true if contents shipped/cancelled/ 3 days amount of time has passed
   protected boolean isValidToteLpn(String lpn){
   	   /*
         int count = db.getInt(-1, "SELECT COUNT(*) " +
         "FROM rdsCartons " +
         "WHERE cartonSeq = ( " +
            "SELECT cartonSeq " +
            "FROM rdsCartons " +
            "WHERE lpn = '%s' " +
            "ORDER BY cartonSeq DESC " +
            "LIMIT 1 " +
            ") AND ( " +
               "palletStamp IS NOT NULL " + 
               "OR cancelStamp IS NOT NULL " + 
               "OR createStamp > DATE_SUB(NOW(), INTERVAL 3 DAY) " +
               ");", lpn);
         
         if (count > 0) {
            inform("returning false");
            return false;
        } else {
            inform("returning true");
            return true;
        }*/
		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s' "
				+ "AND cancelStamp IS NULL "
				+ "AND shipStamp IS NULL "
				+ "AND ( labelStamp IS NULL OR ( labelStamp > DATE_SUB(NOW(), INTERVAL 1 DAY) )  )", lpn);
		if( count == 0 ) {
			SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "shipStamp", "lpn", lpn);
			return true;
		}
		return false;
   }
   
   protected int getToteSeq(int cartSeq, String cartonLpn) {
   	return db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn='%s' AND cartSeq=%d", cartonLpn,cartSeq);
   }
   
   protected int getCartonSeq(String cartonLpn) {
   	return db.getInt(-1, "SELECT cartonSeq FROM rdsCartons WHERE lpn='%s' AND cartSeq=%d", cartonLpn);
   }
   
   protected boolean toteExist(String barcode) {
   	return db.getInt(1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn='%s' AND completeStamp IS NULL", barcode)>0;
   }
   
   protected void completeBuild(int cartSeq) {
      int cartonCount = getBuildCount();
      String cartID = getCartName(cartSeq);
      if (cartonCount>0) {
         clearRemaining(cartSeq);
         inform("cart seq %d built with %d cartons/totes", cartSeq, cartonCount);
         db.execute("UPDATE rdsCarts SET " +
               "buildStamp = NOW() " +
               "WHERE cartSeq = %d", 
               cartSeq );
         //Set lastPositionLogical for each carton
         db.execute("UPDATE rdsCartons SET " +
               "lastPositionLogical = '%s' " +
               "WHERE cartSeq = %d AND assigned IS NOT NULL", 
               cartID,cartSeq );
         //clear unassigned cartons
         db.execute("UPDATE rdsCartons SET " +
               "cartSeq = -1, cartSlot = NULL " +
               "WHERE cartSeq = %d AND assigned IS NULL", 
               cartSeq );
         SloaneCommonDAO.postCartLog(""+cartSeq, "cartBuild", "released for picking");
      } else {
         inform("cart seq %d completed empty. clear cart data", cartSeq);
         clearRemaining( cartSeq );
      }
   }
   
   protected void clearTotes( int cartSeq ) {
   	List<String> totes = db.getValueList(
   			"SELECT cartonSeq FROM rdsCartons WHERE cartSeq=%d "
   			+ "AND cartonType IN ('singlesTote','batchTote') "
   			+ "AND assigned IS NULL",cartSeq);
   	for( String tote_str : totes ) {
   		int toteSeq = RDSUtil.stringToInt(tote_str, -1);
   		if( toteSeq<0 ) continue;
   		//db.execute("UPDATE rrPicks SET cartSeq=-1,cartSeq=-1 WHERE cartSeq=%d", toteSeq);
   		//db.execute("UPDATE rdsCartons SET cartSeq=-999,cartSeq=-1,cartID='',cartSlot='' WHERE cartSeq=%d", toteSeq);
   		//db.execute("DELETE FROM rrCartons WHERE seq=%d", toteSeq);
   		trace("reset picks in tote %d",toteSeq);
   	}   	
   }

   protected void clearRemaining( int cartSeq) {
   	clearTotes( cartSeq );
     /*  db.execute("UPDATE rdsPicks AS p " +
            "JOIN rdsCartons AS c ON c.cartonSeq=p.cartonSeq SET " +
            "p.cartSeq = DEFAULT, " +
            "c.cartSeq = DEFAULT, " +
            "c.cartSlot = DEFAULT " +
            "WHERE p.cartSeq = %d " +
            "AND cartonType='splitCase' AND assigned IS NULL", cartSeq); */
      db.execute("UPDATE rdsCartons SET " +
            "cartSeq = -1, " +
            "cartSlot = NULL, " +
            "assigned = NULL " +
            "WHERE cartSeq = %d " +
            "AND assigned IS NULL", cartSeq); 
   }   
   
   protected static String cartStatus( String status ) {
	   switch (status) {
	   case "":
	      return "Empty";
      case "building":
	   case "built":
	   case "boxing":
	   case "boxed":
      case "picking":
      case "released":
	      return "Open";
	   case "picked":
	      return "Picked";
	   case "error":
	   default:
	      return "Error";
	   }
	}

	
   protected static String createStatus( String status ) {
	   switch (status) {
      case "idle":
         return "";
      case "ready":
         return "Ready";
      case "building":
         return "Complete";
      case "released":
         return "Complete";
	   case "error":
      default:
         return "Error";
	   }
	}

   protected static String buildStatus( String status ) {
	   switch (status) {
      case "idle":
         return "Complete";
      case "ready":
         return "";
      case "building":
         return "Building";
      case "released":
         return "Complete";
	   case "error":
      default:
         return "Error";
	   }
	}

   protected static String pickStatus( String status ) {
	   switch (status) {
	   case "":
	      return "N/A";
      case "idle":
         return "Complete";
      case "ready":
         return "";
      case "building":
         return "";
      case "released":
         return "Pending";
	   case "error":
      default:
         return "Error";
	   }
	}   
   
   protected void gotoStartScreen() {
      setNextScreen(startScreen);
   }
   
   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      tickDisplay();
   }

   /*
    * display methods
    */

   public void initDisplay() {
      super.initDisplay();  
      header.updateTitle("CART BUILD STATION");
   }

   protected void tickDisplay() {

   }

   protected void initInfo() {
      InfoBoxConstructor infoModel = new InfoBoxConstructor(0,INFO_Y);
      infoModel.setFont(60, 50);
      infoModel.setWidth(W1_2);
      infoModel.setMargins(0, 0, 50, 50);
      infoModel.setRows(4, 10);
      leftInfo = infoModel.build();
      
      infoModel.setOrigin(W1_2, INFO_Y);
      rightInfo = infoModel.build();
      
      initLines(false);
   }
   
   protected void setCartInfo() {
   	int cartSeq = getIntParam( TermParam.cartSeq );
      if (cartSeq > 0)
         showLines();
      setLeftInfo(cartSeq);
      setRightInfo(cartSeq);
   }
   
   private void setLeftInfo( int cartSeq ) {
      if (leftInfo==null)
         return;
      if (cartSeq>0) {
         leftInfo.updateInfoPair(0, "Cart", "%s",getStrParam(TermParam.cartID));
         leftInfo.updateInfoPair(1, "Cartons", "%d", getBuildCount());
         leftInfo.updateInfoPair(2, "Status", getCartStatus( cartSeq ));
         leftInfo.show();
      } else {
         leftInfo.hide();
      }         
   }

   private void setRightInfo( int cartSeq ) {
      if (rightInfo==null)
         return;
      if (cartSeq>0) {
         String status = getCartStatus( cartSeq );
         String create = createStatus(status);
         String build = buildStatus(status);
         String pick = pickStatus(status);
         rightInfo.updateInfoPair(0, "Create", create);
         rightInfo.updateInfoPair(1, "Build", build);
         rightInfo.updateInfoPair(2, "Pick", pick);
         rightInfo.show();
      } else {
         rightInfo.hide();
      }         
   }

   protected void initLines( boolean show ) {
      if (lines != null)
         return;
      int t = 5;
      int y1 = INFO_Y;
      int y2 = 340;
      lines = new TermGroup(0,0);
      lines.add(horizontalLine(y1, t));
      lines.add(horizontalLine(y2, t));
      lines.add(verticalLine(y1, y2, W1_2,t));
      if (show)
         showLines();
   }
   
   protected void showLines() {
      lines.show();
   }

   /*
    * buttons
    */
   
   /*@Override
   protected void doCancel() {
      trace("cancel");
      setNextScreen("cart.IdleScreen");
   }*/
   
   /*
    * helper methods
    */
   
   /*
   private void handleScan() {
      String result = getScan();
      if ( result == null || result.isEmpty() ) 
         return;
      else {
         processScan(result);
      }
   }*/

   protected void processScan(String scan) {
      inform( "scan ignored [%s]", scan );
   }
   
   protected int getIntParam( TermParam param ) {
      return RDSUtil.stringToInt( term.fetchAtom( param.toString(), "" ), -1 );
   }

   protected String getStrParam( TermParam param ) {
      return term.fetchAtom( param.toString(), "" );
   }
   
   protected String getParam( TermParam param ) {
      return term.fetchAtom( param.toString(), null );
   }
   
   protected void setParam( TermParam param, String format, Object... args ) {
      if(format == null)
         term.dropAtom( param.toString() );
      else
         term.saveAtom( param.toString(), String.format(format, args) );
   }
   
   protected void clearAllParam() {
      for( TermParam param : TermParam.values())
      if (param.toString()!= "cartType")
            term.dropAtom(param.toString());
   }
   
   protected void clearCartParam() {
   	term.dropAtom(TermParam.cartSeq.toString());
   	term.dropAtom(TermParam.cartID.toString());
   	term.dropAtom(TermParam.cartStatus.toString());
   }   

}
