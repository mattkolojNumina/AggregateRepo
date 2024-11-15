package termApp.cart;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ScrollBoxConstructor;
import termApp.util.TermActionObject.OnActionListener;

public class Audit
extends AbstractCartScreen {

   private TextField msg;
   private Button ok;
   
   private final static int LIST_LENGTH = 6;
   private ScrollBox leftList,rightList;
   private int cartSeq;
   private String cartID;
   
   public Audit(TerminalDriver term) {
      super(term);
      cartSeq = getIntParam(TermParam.cartSeq);
      cartID = getStrParam(TermParam.cartID);
      inform("cart audit cartID [%s] seq [%d] ",
            cartID, cartSeq) ;

      bkgdColor = COLOR_YELLOW;
   }

   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
   }

   /*
    * display methods
    */

   @Override
   public void initDisplay() {
      super.initDisplay();
      msg = new TextField(MSG_MARGIN, 780, FONT_L, "");
      msg.show();
      msg.updateText("Audit cart");
      
      initButtons();
      initInfo();
      setCartInfo();
      initList();
      updateList();
   }
   
   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x, y, f, "Done",Align.LEFT,false);
      ok.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            doOkay();            
         }
      });
      ok.show();
   }


   @Override
   protected void doOkay() {
      if (!ok.on())
         return;
      trace("ok");
      setNextScreen( "cart.IdleScreen" );   
   }
   
   @Override
   protected void tickDisplay() {
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
      List<Map<String,String>> buildList = getAssignedCartonsListNoColor(cartSeq);
      if (buildList == null || buildList.isEmpty()) {
         leftList.hide();
         rightList.hide();
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
   
}
