package termApp.cart;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

public class Complete
extends AbstractCartScreen {

   private TextWrap msg;
   protected long start = 0L, reset = 500L;
   private String nextScreen;
   private int cycles = 0;

   private enum cartState {
      STANDARD,
      EMPTY,
   };
   private cartState state;

   public Complete(TerminalDriver term) {
      super(term);
      nextScreen = "cart.IdleScreen";
      start = System.currentTimeMillis();
      int count = getBuildCount();
      state = cartState.STANDARD;
      if (count == 0)
         state = cartState.EMPTY;
      bkgdColor = getStateColor();
   }

   /*
    * interface methods
    */

   private String getStateColor() {
      switch(state) {
      case EMPTY: 
         return COLOR_RED;
      case STANDARD:
      default: 
         return COLOR_GREEN;
      }
   }

   private String getStateMsg() {
      switch(state) {
      case EMPTY: 
         return "Cart is empty. Ready for new cart build.";
      case STANDARD:
      default: 
         return "Cart build complete. Ready for picking.";
      }
   }
   
   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      cycles++;
      if (cycles >= 10) {
         inform("Cart build completed for cart [%d]", getIntParam(TermParam.cartSeq));
         setNextScreen(nextScreen);
      }
   }

   /*
    * processing methods
    */

   protected void processScan(String scan) {
      trace( "processing scan [%s]", scan);
      inform("scan ignored"); 
   }

   /*
    * display methods
    */

   @Override
   public void initDisplay() {
      super.initDisplay();
      msg = initMsg();
      msg.wrap(getStateMsg());
      initButtons();
      initInfo();
      setCartInfo();
   }
   
   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      Button btn = new Button(x, y, f,  "Ok",Align.LEFT,false);
      btn.registerOnActionListener(okAction());
      btn.show();
   }

   @Override
   protected void doOkay() {
      trace("Ok");
      setNextScreen(nextScreen);
   }

   @Override
   protected void initInfo() {
      super.initInfo();
   }
}
