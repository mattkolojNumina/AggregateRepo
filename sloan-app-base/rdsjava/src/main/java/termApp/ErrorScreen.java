package termApp;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.Button;
import termApp.util.TermActionObject.*;


public class ErrorScreen
      extends AbstractNuminaScreen {

   private final int SCREEN_DURATION = 10; //10 ticks at .5s/tick = 5 seconds

   private int ticks;
   private Button okButton;

   public ErrorScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(true); 
      ticks = 0;
   }

   /*
    * interface methods
    */

   public void handleInit() {
      super.handleInit();
      header.init();
      header.updateTitle("ERROR SCREEN");

      String error = getParam("Error");

      //statusMsg.updateText("Something went wrong");

      if(error.equals("orderStartLpnExists"))
      {
         String lpn = getParam("LPN");
         statusMsg.updateText("LPN [%s] is already in use",lpn);
      }

      else if(error.equals("orderStartNoMoreCartons"))
      {
         String selectedBox = getParam("selectedBox");
         statusMsg.updateText("All available cartons [%s] have an assigned LPN",selectedBox);
      }


      else
      {
         //String text = getParam("cartID");
         statusMsg.updateText("Unknown Error");
      }
      //String text = getParam("CartID");
      //statusMsg.updateText("CartID: %s is being used",text);
      statusMsg.show();

      //create the button, make it in the lower-mid part of the screen, make it say "OK",
      //and make it visible by default
      okButton = new Button(SCREEN_WIDTH/2, 3 * SCREEN_HEIGHT / 4, "OK", true);

      //make it change the screen when its pressed   
      okButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("OK button pressed");
            if(error.equals("orderStartLpnExists")){
               //setParam("cartID", "");
               setParam("Error","");
               setNextScreen("orderStart.OrderStartScreen");
            }

            else if(error.equals("orderStartNoMoreCartons")){
               //setParam("cartID", "");
               setParam("Error","");
               setNextScreen("orderStart.OrderStartScreen");
            }

         
         }
      });
   }

   @Override
   public void preInit() {
      term.clearScreen(COLOR_RED);

      faultBar.init();
      footer.init();
      header.init();
   }

   public void handleTick() {
      super.handleTick();
      //change the screen after 10 ticks
      if (++ticks > SCREEN_DURATION) {
         String error = getParam("Error");
         if(error.equals("orderStartLpnExists")){
            setParam("Error","");
            setNextScreen("orderStart.OrderStartScreen");
         }

         else if(error.equals("orderStartNoMoreCartons")){
               //setParam("cartID", "");
               setParam("Error","");
               setNextScreen("orderStart.OrderStartScreen");
         }
         
      }
   }

}
