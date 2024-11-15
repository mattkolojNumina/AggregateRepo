package termApp.cart;

import rds.RDSCounter;
import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ScrollBoxConstructor;

public class Build
extends AbstractCartScreen {

   private TextField msg;
   private String oldMsg, oldColor;
   protected long start = 0L, reset = 5000L;

   private boolean validCart;
   private Button cancel, complete;
   
   private String nextLpn;
   private String expectedSize;
   private boolean cartonSet,requireTote,isTote;
   private int cartSeq;
   private String cartID;
   private int nextCartonSeq;
   
   private int assignedCartonCount;
   
   private final static int LIST_LENGTH = 6;
   private ScrollBox leftList,rightList;
   
   private List<Map<String,String>> buildList;

   public Build(TerminalDriver term) {
      super(term);
      cartSeq = getIntParam(TermParam.cartSeq);
      cartID = getStrParam(TermParam.cartID);
      String status = getCartStatus(cartSeq);
      nextCartonSeq = -1;
      nextLpn = "";
      cartonSet = requireTote = isTote = false;
      assignedCartonCount = 0;
      inform("cart build cartID [%s] seq [%d] status [%s]",
            cartID, cartSeq, status) ;

      validCart = status.equals("building");
      bkgdColor = validCart ? DEFAULT_SCREEN_COLOR : COLOR_RED;
   }

   /*
    * interface methods
    */

   private void getNext() {
   	assignedCartonCount = assignedCartonCount(cartSeq);
   	if( assignedCartonCount>0 ) {
      	setNext();	
      }
   	else 
   		clearNext();

      inform("update list");
      updateDisplay();
   }   
   
   private void setNext() {
   	//requireTote = nextIsTote(cartSeq);
   	//if(requireTote)
   		//nextCartonSeq = getNextBuildTote(cartSeq);
   	//else
      //expectedSize = getExpectedSize(cartSeq);
   	//nextCartonSeq = getNextCarton(cartSeq);
      nextLpn = null;
      nextCartonSeq = -1;
      cartonSet = false;
      isTote = false;
   }

   private void clearNext() {
      nextCartonSeq = -1;
      nextLpn = null;
      cartonSet = false;
      requireTote = false;
      isTote = false;
   }

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
   }


   /*
    * processing methods
    */

   protected void processScan(String scan) {
      trace( "processing scan [%s]", scan);
      if (assignedCartonCount <= 0 ) {
         inform("scan ignored. no remaining containers");
         return;
      }

      if( !("" +cartFromBarcode(scan)).equals("" + cartID) && cartonSet) {
         trace("user scanned cart[%s], we expect cart[%s]", cartFromBarcode(scan), cartID );
         tempMsg(COLOR_RED, "Wrong cart! Scan a slot on cart %s", cartID);
         return;
      }

      if (cartonSet) 
         processCartSlot(scan);
      else 
         processLpn(scan);
   }

   /*
    * display methods
    */

   private void processCartSlot(String scan) {
      String scanSlot = slotFromBarcode(scan);
      String slot = getSlot(cartID,scanSlot);
      if (slot == null) {
         alert("barcode [%s] is not a slot on cartID [%s]", scanSlot, cartID);
         tempMsg(COLOR_RED, "Invalid slot barcode [%s] for cart.", scan);
         return;
      }
      
      if (!isSlotAvailable(cartSeq,cartID,slot, true)) {
         alert("slot is not available [%s] on cartID [%s]", slot, cartID);
         tempMsg(COLOR_RED, "Slot [%s] not available.", scan);
         return;
      }
      trace("isTote ? %d, nextLpn %s, nextCartonSeq %d", isTote ? 1 : 0, nextLpn, nextCartonSeq);
      if (nextLpn != null)
         setLPN(nextCartonSeq,nextLpn); 
      //assignCarton(cartSeq, cartID, nextCartonSeq, slot);
      assignToCart(cartSeq, cartID, nextCartonSeq, slot);
   
      trace("assign cartonSeq[%d] to cart[%s] seq[%d] on  slot[%s]", nextCartonSeq, cartID, cartSeq, slot);
      getNext();
      //RDSCounter.increment("build", "packsize", "assign");
   }

   private void processLpn(String scan) {

      String cartonType = getCartonType(scan);
      inform("Scanned cartonType: [%s]", cartonType);
   
      switch (validateLpn(scan, true, buildList)) {

      case GOOD_LPN:

      //If container is a tote, need to check if tote is available or not
      //tote lpns CAN be duplicated in rdsCartons for Sloane

      	nextCartonSeq = getNextCarton(cartonType, cartSeq);
      	nextLpn = scan;
      	cartonSet = true;	
      	updateDisplay();
      	break;

      case NOT_FOUND:
         tempMsg(COLOR_RED, "LPN not found for cart, scan new LPN");
         break;
      case ASSIGNED:
         tempMsg(COLOR_RED, "LPN is assigned already, scan new LPN");
         break;      
      case ASSIGNED_TOTE:
         tempMsg(COLOR_RED, "TOTE LPN is still open, scan new LPN");
         break;          
      case INVALID_FORMAT:
         tempMsg(COLOR_RED, "Invalid LPN for carton/tote, scan new LPN");
         break;
      default:
         tempMsg(COLOR_RED, "Invalid LPN, scan new LPN");
         break;
      }
   }

   @Override
   public void initDisplay() {
      inform("init display");
      super.initDisplay();
      msg = new TextField(MSG_MARGIN, BTN_Y-100,FONT_L, "");
      inform("init button");
      initButtons();
      inform("init info");
      initInfo();
      inform("set info");
      setCartInfo();
      inform("init list");
      initList();
   }

   @Override
   public void handleInit() {
      super.handleInit();
      if (validCart) 
         getNext();
   }
   
   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      complete = new Button(x, y, f,  "Complete",Align.LEFT,false);
      complete.registerOnActionListener(okAction());
      complete.hide();
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x, y, f,  "Cancel",Align.RIGHT,false);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }

   @Override
   protected void doOkay() {
      completeBuild(cartSeq);
      setNextScreen("cart.Complete");
      RDSCounter.increment("build", "packsize", "completed");
   }
   
   @Override
   protected void doCancel() {
      super.doCancel();
   }

   @Override
   protected void setCartInfo() {
      if (validCart) {
         super.setCartInfo();
         return;
      }
      leftInfo.updateInfoPair(0, "Cart", "%s",getStrParam(TermParam.cartID));
      leftInfo.updateInfoPair(1, "Cartons", "%d", getBuildCount());
      leftInfo.updateInfoPair(2, "Status", "Assign Cartons");
      rightInfo.updateInfoPair( 0,"","");
      rightInfo.updateInfoPair( 1,"","");
      rightInfo.updateInfoPair( 2,"","");
   }
   
   @Override
   protected void tickDisplay() {
      String status = getCartStatus(cartSeq);
      validCart = status.equals("building");
      bkgdColor = validCart ? DEFAULT_SCREEN_COLOR : COLOR_RED;

      if (start > 0 && System.currentTimeMillis() - start > reset) 
         restoreMsg();
   }
   
   private void updateDisplay() {
      int buildCount = getBuildCount();
      if (buildCount > 0)
         complete.show(); 
      leftInfo.updateInfoValue(1, "%d", buildCount );
      //rightInfo.updateInfoPair(1, "","");
      updateList();
      clearMsgHistory();
      if( assignedCartonCount == 0 ) {
         if (buildCount == 0) {
            background.setFill(COLOR_RED);
            msg.updateText("Cartons already assigned. Complete and begin picking");    
         } else {
            background.setFill(COLOR_GREEN);
            msg.updateText("Cart build complete.");
            completeBuild(cartSeq);
            setNextScreen("cart.Complete");
            RDSCounter.increment("build", "packsize", "completed");
         }
         cancel.hide();
         return;      	
      }
      background.setFill(BKGD_COLOR);
      if (cartonSet) {
         String currentLpn = nextLpn == null ? "Not set" : nextLpn;
         rightInfo.updateInfoPair(2, "Current LPN",currentLpn);
         if( isTote )
         	msg.updateText("Assign tote to empty slot on cart.");
         else
         	msg.updateText("Assign carton to empty slot on cart.");
         return;
      } else {
      	if( requireTote )
      		msg.updateText("Scan new %s", getToteType(nextCartonSeq));
      	else
      		msg.updateText("Scan new carton");
         return;
      }
   }
   
   private void initList() {
      int y = 405;
      
      ScrollBoxConstructor sbModel = new ScrollBoxConstructor(0,y);
      sbModel.setFont(50,45);
      sbModel.setMargins(0, 0, 50, 50);
      sbModel.setRows(LIST_LENGTH, 5);
      sbModel.setWidth(W1_2);
      sbModel.setButton(0, 0, 0);
      sbModel.setIndexJump(0);
      sbModel.drawHeaders(true);
      sbModel.addColumn(0, 0, Align.LEFT, "Slot", "slot");
      sbModel.addColumn(140, 0, Align.LEFT, "Size", "size");
      sbModel.addColumn(W1_2-120, 0, Align.RIGHT, "LPN", "lpn");
      leftList = sbModel.build();
      leftList.hide();
      
      sbModel.setOrigin(W1_2, y);
      rightList = sbModel.build();
      rightList.hide();
   }
   
   private void updateList() {
      buildList = getBuildList(cartSeq,nextCartonSeq);
      if (buildList == null || buildList.isEmpty()) {
         leftList.hide();
         rightList.hide();
         alert("update list empty");
         return;
      }
      
      int size = buildList.size();
      if (size > LIST_LENGTH ) {
         leftList.updateDisplayList(buildList.subList(0, LIST_LENGTH));
         rightList.updateDisplayList(buildList.subList(LIST_LENGTH, size));
         leftList.show();
         rightList.show();   	
      }
      else {
         leftList.updateDisplayList(buildList);
         leftList.show();
         rightList.hide();
      }
   }
   
   private void tempMsg(String color, String format, Object... args) {
      start = System.currentTimeMillis();
      oldColor = background.getFillColor();
      background.setFill(color);
      oldMsg = msg.getText();
      msg.updateText(format, args);
   }
   
   private void clearMsgHistory() {
      start = 0;
      oldColor = null;
      oldMsg = null;
   }
   
   private void restoreMsg() {
      start = 0;
      if (oldColor != null)
         background.setFill(oldColor);
      if (oldMsg != null)
         msg.updateText(oldMsg);
   }
   
}
