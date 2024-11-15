package termApp.cart;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

public class Wait
extends AbstractCartScreen {

   private TextWrap msg;
   private boolean errorSet;
   
   public Wait(TerminalDriver term) {
      super(term);
      
      String cartID = getStrParam(TermParam.cartID);
      inform("monitor cart creation cartID [%s]", cartID) ;

      if (cartID == null)
         alert("cartID = null");
      if (cartID.isEmpty())
         alert("cartID is empty");
      errorSet = (cartID == null || cartID.isEmpty());
      bkgdColor = !errorSet ? COLOR_YELLOW : COLOR_RED;
   }

   /*
    * interface methods
    */


   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      pollCartCreation();
   }

   private void pollCartCreation() {
      String cartID = getParam(TermParam.cartID);
      String status = getCartStatus(getCartSeq(cartID));

      if (errorSet) {
         inform("errorSet = true");
         return;}

      inform("pollingStatus for cartID['%s'] [%d]: ['%s']", cartID, getCartSeq(cartID), status);
      //inform("cart status [%s]", status);

      switch (status) {
      case "ready":
      	updateMsg(BKGD_COLOR, "Waiting for cart creation");
      	return; 
      case "building": //"boxed"
         int cartSeq = getCartSeq(cartID);
            setParam(TermParam.cartSeq, "%d", cartSeq);
            trace("cart(%d) creation complete",cartSeq);
            setNextScreen("cart.Build");
         return;           
      default:
         return;
         // wait
      }

      
   }

   /*
    * display methods
    */

   @Override
   public void initDisplay() {
      super.initDisplay();
      msg = initMsg();
      if (!errorSet)
         msg.wrap("Waiting for cart construction."); 
      else
         msg.wrap("Error during cart construction.");
      initButtons();
      initInfo();
      setCartInfo();
   }
   
   private void initButtons() {
      int x = SCREEN_WIDTH - MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      Button btn = new Button(x, y, f,  "Cancel",Align.RIGHT,false);
      btn.registerOnActionListener(cancelAction());
      btn.show();
   }

   @Override
   protected void doCancel() {
      super.doCancel();
   }

   @Override
   protected void setCartInfo() {
      if (!errorSet) {
         super.setCartInfo();
         return;
      }
      showLines();
      String cartID = getStrParam(TermParam.cartID);
      leftInfo.updateInfoPair( 0, "Cart", cartID);
      rightInfo.updateInfoPair( 0, "Status", errorSet ? "Error" : "Processing");
   }
   
   private void updateMsg(String color, String format, Object... args) {
      background.setFill(color);
      msg.wrap(format, args);
   }
   
   @Override
   protected void tickDisplay() {
   
   }
   
}
