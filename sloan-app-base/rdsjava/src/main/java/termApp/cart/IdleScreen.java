package termApp.cart;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnTextActionListener;
import termApp.util.TermActionObject.OnActionListener;

import static termApp.util.Constants.*;

import static rds.RDSLog.*;

public class IdleScreen
extends AbstractCartScreen {

   private Button switchMode, switchLocation;

   private String cartType;
   private TextField cartonCount = new TextField(0,0,0, "");
   private int count = 0;

   public IdleScreen(TerminalDriver term) {
      super(term);
    
      //saveParams();
      setLogoutAllowed(true);
      clearAllParam();
   }

   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();  // calls handleScan -> processScan
      updateDisplay();
      String cartType = getStrParam(TermParam.cartType);
      if (availableCartons(cartType) <=0) {
         inform("Location empty, moving to location select screen");
         setNextScreen("cart.LocationScreen");
      }
   }

   /*
    * processing methods
    */

   protected void processScan(String scan) {
      trace( "processing scan [%s]", scan);
      processBarcode(scan);
   }

   private void processBarcode(String scan) {
      String cartType = getStrParam(TermParam.cartType);
      inform("processing barcode for cartType : [%s]", cartType);
      setParam(TermParam.barcode, scan);
      setNextScreen("cart.ProcessScan");      
   }

   /*
    * display methods
    */
   @Override
   public void initDisplay() {
      
      //Return to location screen if location not set
      if (getStrParam(TermParam.cartType).isEmpty()) {
         inform("Location is empty, setting next screen to location screen");
         setNextScreen("cart.LocationScreen");
         clearAllParam();
         return;
      }

      super.initDisplay();
      new TextField(100, 650, 75, "Scan cart barcode."); 
      new TextField(100, 250, 75, String.format("Current Location %s", getStrParam(TermParam.cartType)));
      
      TextEntry entry = new TextEntry(100, 700, 60, SCREEN_WIDTH-200, 75, false);
      entry.registerOnTextActionListener(new OnTextActionListener() {
         @Override
         public void onAction(String text) {
            inform("cart entry [%s]", text);
            processBarcode(text);
         }
      });

      int x = W1_2;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      if (!(term.getTermName().equals("east-cart-build"))) {
      switchMode = new Button(x, y, f, "Change Mode",Align.CENTER,false);
      switchMode.registerOnActionListener(modeAction());
      switchMode.show();
      
      
      switchLocation = new Button(x - 625, y, f, "Change Location",Align.CENTER,false);
      switchLocation.registerOnActionListener(locationAction());
      switchLocation.show();
      }
   }

   private void updateDisplay() {
      count ++;
      if (count % 10 == 0 || count == 1) {
      cartonCount.hide();
      cartonCount = new TextField(100, 400, 50, String.format("Available Cartons: %d", availableCartons(getStrParam(TermParam.cartType))));
      cartonCount.show();
      count = 1; }
   }

   private OnActionListener locationAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            inform("Changing cartType parameter");
            setNextScreen("cart.LocationScreen");            
         }
      };
   }
   
}