package termApp.cart;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

public class ProcessScan
extends AbstractCartScreen {

   private String nextScreen;
   private boolean canProcess, canAudit, canCreate, isValidCart;
   private String msgColor, msgText;
   private TextField scanMsg;
   protected long start = 0L, reset = (5) * (60) * 1000L;
   private TextWrap msg;

   private Button audit, cancel, create;
   private String barcode;
   private String cartType;

   public ProcessScan(TerminalDriver term) {
      super(term);
      barcode = getStrParam(TermParam.barcode);
      cartType = getStrParam(TermParam.cartType);
      inform("scanned cart barcode [%s] for cartType [%s]",barcode, cartType) ;
      processLastScan();
   }

   /*
    * interface methods
    */

   /** Initializes the screen and lays out UI elements. */
   public void handleInit() {
      if (canProcess) {
         setNextScreen(nextScreen);
         return;
      }
      super.handleInit();  // displays login footer
   }

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      if (canProcess) {
         setNextScreen(nextScreen);
         return;
      }
      super.handleTick();
      if (start > 0 && System.currentTimeMillis() - start > reset) 
         setNextScreen(nextScreen);
   }

   /*
    * processing methods
    */

   protected void processScan(String scan) {
      setParam(TermParam.barcode, scan);
      this.barcode = scan;
      processLastScan();
      loadMsg();
   }

   /*
    * display methods
    */

   @Override
   public void initDisplay() {
      super.initDisplay();
      msg = initMsg();
      if (!canAudit)
         new TextField(MSG_MARGIN, MSG_Y-300, FONT_L, String.format("%d cartons remaining for %s", availableCartons(cartType), cartType));
      scanMsg = new TextField(MSG_MARGIN, MSG_Y-100,FONT_L, "");
      initButtons();
      initInfo();
      loadMsg();
   }

   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      create = new Button(x, y, f, "Create",Align.LEFT,false);
      create.registerOnActionListener(createAction());
      create.hide();
      
      x = W1_2;
      audit = new Button(x, y, f, "Audit",Align.RIGHT,false);
      audit.registerOnActionListener(auditAction());
      audit.hide();

      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x, y, f, "Cancel",Align.RIGHT,false);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   private OnActionListener auditAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doAudit();            
         }
      };
   }
   
   protected void doAudit() {
      //if (!audit.on())
      //   return;
      trace("audit cart");
      setNextScreen("cart.Audit");
   }
   
   private OnActionListener createAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doCreate();            
         }
      };
   }

   protected void doCreate() {
      if (!create.on())
         return;
      if( canCreate )
      	canCreate = false;
      create.hide();
      String formattedBarcode = cartFromBarcode(barcode);
      trace("create cart with barcode [%s]", formattedBarcode);
      int cmSeq = createCart(formattedBarcode);
      if (cmSeq <= 0) {
         alert("failed to create valid cart message.");
         updateMsg(COLOR_RED,"Processing error. Take cart to supervisor.");
         return;
      }
      
      setCart(getCartSeq(formattedBarcode));
      inform("cart set: ID [%d] Seq [%s] Status[%s]", getIntParam(TermParam.cartSeq), getStrParam(TermParam.cartID), getStrParam(TermParam.cartStatus));
      setNextScreen("cart.Wait");
   } 
   
   @Override
   protected void doCancel() {
      if (!cancel.on())
         return;
      trace("cancel");
      gotoStartScreen();
   }
   
   @Override
   protected void tickDisplay() {
   
   }
   

   /*
    * helper methods
    */
   private void processLastScan() {
      canProcess = false;
      canAudit = false;
      canCreate = false;
      isValidCart = false;

      nextScreen = determineStartPage();
      if (barcode.isEmpty()) {
         alert("scan param not set.");
         saveMsg(COLOR_RED,"Cart not found. Invalid scan.");
         return;
      }
      
      String formattedBarcode = cartFromBarcode(barcode);
      
      if (!isValidCart(formattedBarcode)) {
         alert("invalid cart barcode [%s]", barcode);
         saveMsg(COLOR_RED,"Invalid cart barcode.");
         return;
      }

      setParam(TermParam.cartID, formattedBarcode );      
      
      //If cart has no history (cart exists in cfgCarts, doesn't in rdsCarts), create new cart
      int cartSeq = getCartSeq(formattedBarcode);
         if (cartSeq == -1) {
            inform("New cart [%s] needs creation", formattedBarcode);
            startNewCart();
            return;
         }

      String status = getCartStatus(cartSeq);
      
      inform("cart status [%s]", status);
      switch (status) {

      case "building":
         inform("found new building cart seq %d", cartSeq);
         setCart(cartSeq);
         canProcess = true;
         nextScreen = "cart.Build";
         return;

      case "idle":
         inform("cart processed as idle");
         startNewCart();
         return;

      case "ready":
         alert("cart ready but cartBuild not triggered. potential issue. creating new cart");
         //startNewCart();
         setCart(cartSeq);
         canProcess = true;
         nextScreen = "cart.Wait";
         return;

      case "released":
         canAudit = true;
         setCart(cartSeq);
         saveMsg(COLOR_YELLOW,"");
         //doAudit();
         return;
      }
   }

   private void startNewCart() {

         cartType = getStrParam(TermParam.cartType);
         inform("cartType in startNewCarton is : %s", cartType);
   		int count = availableCartons(cartType);
         if (count > 0) {
            inform("cart ready for new cartons, %d cartons ready for building", count);
            saveMsg(bkgdColor,"Cart ready for new cartons.");
            canCreate = true;
         } else {
            alert("cart ready but no cartons available for build");
            saveMsg(bkgdColor,"No cartons available for cart build.");
         }
   }

   private void processCreateCart() {
   	if( isConveyorBuildCart() ) {
         inform("building at conveyor cart build station.");
         saveMsg(COLOR_YELLOW,"Cart should be processed at conveyor cart build station");   		
   	} else {
         inform( "waiting for cart creation and boxing..." );
         canProcess = true;
         nextScreen = "cart.Wait";	
   	}
   }
   
   private void saveMsg(String color, String format, Object... args) {
      canProcess = false;
      msgColor = color;
      msgText = String.format(format,args);
   }

   private void loadMsg() {
      updateMsg(msgColor,msgText);
      setCartInfo();
      int cartSeq = getIntParam(TermParam.cartSeq);
      String cartType = getStrParam(TermParam.cartType);
      inform("cartType in loadMsg is : %s", cartType);
      if (cartSeq <= 0 && isValidCart) {
         showLines();
         leftInfo.updateInfoPair(0, "Cart", "%s", cartFromBarcode(barcode));
         rightInfo.updateInfoPair(0, "Available Cartons", "%d", availableCartons(cartType) );
      }
      
      if( canCreate ) {
         create.display(canCreate);
         doCreate();
      } else { 
         create.display(canCreate);
      }
      if (canAudit) { 
         audit.display(canAudit);
         doAudit();
      }
   }
   
   private void updateMsg(String color, String format, Object... args) {
      start = System.currentTimeMillis();
      background.setFill(color);
      scanMsg.updateText("Scan: [%s]",barcode);
      msg.wrap(format, args);
   }
   
}
