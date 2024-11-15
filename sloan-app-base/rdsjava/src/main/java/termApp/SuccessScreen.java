package termApp;

import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.Button;
import termApp.util.TextField;
import termApp.util.TermActionObject.*;

public class SuccessScreen
      extends AbstractNuminaScreen {

   private final int SCREEN_DURATION = 10; //10 ticks at .5s/tick = 5 seconds


   private Button nextButton;
   private int ticks;

   public SuccessScreen( TerminalDriver term ) {
      super( term );
     //operators should be allowed to logout; this makes the footer display a logout button
     setLogoutAllowed(true); 
     ticks = 0;

   }

   /*
    * interface methods
    */

   public void handleInit() {
      //term.clearScreen(COLOR_GREEN);
      super.handleInit();
      header.init();
      header.updateTitle("SUCCESS SCREEN");
      //statusMsg.updateText("Cart is complete!");

      String s =getParam("Success");
      if(s.equals("special")){
         String cartonID = getParam("LPN");
         statusMsg.updateText("Place LPN: [%s] back",cartonID);
         TextField display = new TextField(350,450, 75,"onto the conveyor", true);
      }
      else if(s.equals("shipLabel")){
         //String cartonID = getParam("LPN");
         //header.updateTitle("AUDIT COMPLETE");
         statusMsg.updateText("Verification Complete");
         //TextField display = new TextField(350,450, 75,"onto the conveyor", true);
      }
      else if(s.equals("shipLabel2")){
         //String cartonID = getParam("LPN");
         //header.updateTitle("AUDIT COMPLETE");
         statusMsg.updateText("Verification Complete");
         //TextField display = new TextField(350,450, 75,"onto the conveyor", true);
      }
      else{
         //tring cartId=getParam("cartID");
         statusMsg.updateText("Complete!");
         setParam("LPN","");
      }

      statusMsg.show();

      //create the button, make it in the lower-mid part of the screen, make it say "Next",
      //and make it visible by default
      nextButton = new Button(SCREEN_WIDTH/2, 3 * (SCREEN_HEIGHT/4), "Next", true);
      //make it change the screen when its pressed   
      nextButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            //inform("'Next' button pressed");
            //setParam("cartonID", "");
            if(s.equals("special")){
               setNextScreen("specialBuild.ScanBarCodeScreen");
            }
            else if(s.equals("shipLabel")){
               setNextScreen("shipExc.ScanLPNScreen");
            }
            else if(s.equals("shipLabel2")){
               setNextScreen("fullExc.ScanLPNScreen");
            }
            else{
               setNextScreen("shipExc.ScanLPNScreen");
            }
         }
      });
   }

   @Override
   public void preInit() {
      term.clearScreen(COLOR_GREEN);

      faultBar.init();
      footer.init();
      header.init();

   }

   public void handleTick() {
      super.handleTick();
      //change the screen after 10 ticks
      if (++ticks > SCREEN_DURATION) {
         String s = getParam("Success");
            if(s.equals("special")){
               setNextScreen("specialBuild.ScanBarCodeScreen");
            }
            else if(s.equals("shipLabel")){
               setNextScreen("shipExc.ScanLPNScreen");
            }
            else if(s.equals("shipLabel2")){
               setNextScreen("fullExc.ScanLPNScreen");
            }
            else{
               setNextScreen("shipExc.ScanLPNScreen");
            }
      }
   }

}
