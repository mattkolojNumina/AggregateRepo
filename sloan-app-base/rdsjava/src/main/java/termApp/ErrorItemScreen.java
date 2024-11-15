package termApp;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.Button;
import termApp.util.TermActionObject.*;

public class ErrorItemScreen
      extends AbstractNuminaScreen {

   private final int SCREEN_DURATION = 10; //10 ticks at .5s/tick = 5 seconds

   private int ticks;
   private Button okButton;

   public ErrorItemScreen( TerminalDriver term ) {
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
      statusMsg.updateText("%s doesn't go in carton %s", getParam("SKU"),getParam("cartonID"));
      statusMsg.show();

      //create the button, make it in the lower-mid part of the screen, make it say "OK",
      //and make it visible by default
      okButton = new Button(SCREEN_WIDTH/2, 3 * SCREEN_HEIGHT / 4, "OK", true);

      //make it change the screen when its pressed   
      okButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("OK button pressed");
            setParam("SKU", "");
            setNextScreen("ScanItemScreen");
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
         setParam("SKU", "");
         setNextScreen("ScanItemScreen");
      }
   }

}
