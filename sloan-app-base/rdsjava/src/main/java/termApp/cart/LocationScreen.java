package termApp.cart;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnTextActionListener;
import termApp.util.TermActionObject.OnActionListener;

import static termApp.util.Constants.*;
import static sloane.SloaneConstants.*;

import static rds.RDSLog.*;

public class LocationScreen
extends AbstractCartScreen {

   private Button switchMode;

   private Button boom, aersol, pq;

   private TextField boomCountText, pqCountText, buildTypeText, availableCartonsText, noCountCartonsText, scanMsg;

   private String cartType;

   public LocationScreen(TerminalDriver term) {
      super(term);
      //saveParams();
      setLogoutAllowed(true);
      clearAllParam();
      inform("Building cart from [%s]", term.getTermName());
   }

   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();

      if (term.getTermName().equals("east-cart-build") && availableCartons(CARTTYPE_DU) > 0) {
         inform("skipping location selection");
         setParam(TermParam.cartType, CARTTYPE_DU);
         setNextScreen("cart.IdleScreen");
      }

      updateDisplay();  
   }

   /*
    * processing methods
    */

   protected void processScan(String scan) {
      trace( "processing scan [%s]", scan);
      processBarcode(scan);

      
   }

   private void processBarcode(String scan) {
      setParam(TermParam.barcode, scan);
      if (getCartSeq(scan) < 0 )
         return;

      int cartSeq = getCartSeq(scan);
      String status = getCartStatus(cartSeq);
      inform("scanned cart status is [%s]", status);

      if (status.equals("building")) {
         setCart(cartSeq);
         setNextScreen("cart.Build");  
      }
      
      if (status.equals("released")) {
         setCart(cartSeq);
         setNextScreen("cart.Audit");
      }

   }

   /*
    * display methods
    */
    public void updateDisplay() { 

      int boomCartons = availableCartons(CARTTYPE_DU);
      int pqCartons = availableCartons(CARTTYPE_PQ);

      //if no cartons available, hide all functional elements
      if (boomCartons + pqCartons <=0 || term.getTermName().equals("east-cart-build") && boomCartons <= 0) {
      buildTypeText.hide();
      availableCartonsText.hide();
      scanMsg.hide();
      noCountCartonsText.show();
      
      } else {
         buildTypeText.show();
         availableCartonsText.show();
         scanMsg.show();
         noCountCartonsText.hide();
      }

      //Location selection appears if cartons are available
      if (boomCartons > 0 && !(term.getTermName().equals("east-cart-build"))) {
         boom.show();
         boomCountText.show();
         boomCountText.updateText(String.valueOf(boomCartons));
      } else {
         boom.hide();
         boomCountText.hide();
      }

      if (pqCartons > 0 && !(term.getTermName().equals("east-cart-build"))) {
         pq.show();
         pqCountText.show();
         pqCountText.updateText(String.valueOf(pqCartons));
      } else {
         pq.hide();
         pqCountText.hide();
      }

    }

   @Override
   public void initDisplay() {
      super.initDisplay();

      int x = W1_2;
      int y = BTN_Y;
      int f = BTN_FONT;
      switchMode = new Button(x, y, f, "Change Mode",Align.CENTER,false);
      switchMode.registerOnActionListener(modeAction());
      if (!(term.getTermName().equals("east-cart-build")))
         switchMode.show();

      int boomCartons = availableCartons(CARTTYPE_DU);
      int pqCartons = availableCartons(CARTTYPE_PQ);

      scanMsg = new TextField(100, 225, 75, "Scan an active cart or select a cart type");
      buildTypeText = new TextField(325, 400, 60, "Cart Type:"); 
      availableCartonsText = new TextField(1100, 400, 60, "Available Cartons:"); 
      boomCountText = new TextField(1300, 525, 75, String.valueOf(boomCartons));
      pqCountText = new TextField(1300, 725, 75, String.valueOf(pqCartons));
      noCountCartonsText = new TextField(200, 550, 75, "No cartons available for cart build.");

      x = 100;
      y = 500;
      f = BTN_FONT;

      boom = new Button(450, 550, f, CARTTYPE_DU,Align.CENTER,false);
      boom.registerOnActionListener(selectType(CARTTYPE_DU));

      pq = new Button(450, 750, f, CARTTYPE_PQ,Align.CENTER,false);
      pq.registerOnActionListener(selectType(CARTTYPE_PQ));

      if (boomCartons + pqCartons <=0) {
         buildTypeText.hide();
         availableCartonsText.hide();
         scanMsg.hide();
      }

      if (boomCartons <= 0) {
         boom.hide();
         boomCountText.hide();
      }

      if (pqCartons <= 0) {
         pq.hide();
         pqCountText.hide();
      }
         
      
   }

   private OnActionListener selectType(String cartType) {
      return new OnActionListener() {
         @Override
         public void onAction() {
            inform("Building cart for cartType: '%s'", cartType);
            setParam(TermParam.cartType, cartType);
            setNextScreen("cart.IdleScreen");            
         }
      };
   }
   
}