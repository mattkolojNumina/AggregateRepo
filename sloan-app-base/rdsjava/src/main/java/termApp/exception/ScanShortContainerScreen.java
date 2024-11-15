package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

public class ScanShortContainerScreen
      extends AbstractExceptionStationScreen {

   private String scan;
   private Button cancel;

   public ScanShortContainerScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
   }

   /*
    * interface methods
    */

   @Override
   public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
      initButtons();
      initPromptModule(
            String.format("Please scan container %s", getParam(TermParam.cartonLpn)),
            1,
            ENTRYCODE_NONE,
            true,
            true,
            false,
            1);
      super.initDisplay();
   }  

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }    

   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      String cartonID = getParam(TermParam.cartonLpn);
      leftInfoBox.updateInfoPair(0, "Carton LPN", cartonID );
      leftInfoBox.updateInfoPair(1, "SKU", getParam(TermParam.sku)+" "+getParam(TermParam.uom) );
      leftInfoBox.show(); 
   }

   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Invoice", getParam(TermParam.orderId));
      rightInfoBox.updateInfoPair(1, "Qty", getParam(TermParam.qty));
      rightInfoBox.show(); 
   }

   public void handleTick() {
      super.handleTick();

      scan = getScan(); //See AbstractNuminaScreen

      if(scan != null) {
         processScan(scan);
      }
   }
   
   @Override
   protected void doCancel(){
   	inform("cancel button pressed");
   	unreserveMakeupShorts();
   	setNextScreen("exception.IdleScreen");
   }   

   //Methods created
   protected void processScan(String scan) {
      inform( "entry received text [%s]", scan );
      String lpn = getParam(TermParam.cartonLpn);

      if(scan.equalsIgnoreCase(lpn)) {
         inform("Operator scanned the requested container lpn");
         setNextScreen("exception.ScanShortQtyScreen");
      }
      else {
         alert("Operator scanned wrong container lpn, expected [%s]", lpn);
         showAlertMsg("Invalid container");
      }
   }
}
