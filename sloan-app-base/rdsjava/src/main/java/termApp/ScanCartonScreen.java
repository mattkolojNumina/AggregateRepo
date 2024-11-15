package termApp;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.TermActionObject.*;

public class ScanCartonScreen
      extends AbstractNuminaScreen {

   private String scan;
   private TextEntry textBox;
   public ScanCartonScreen( TerminalDriver term ) {
      super( term );
     //operators should be allowed to logout; this makes the footer display a logout button
     setLogoutAllowed(true); 
   }

   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("SCAN CARTON SCREEN");
      footer.show();
      statusMsg.updateText("Please scan or type in a carton ID");
      statusMsg.show();      

      //create the button, make it in the lower-mid part of the screen, make it say "OK",
      //and make it visible by default
      textBox = new TextEntry(100, 3 * (SCREEN_HEIGHT/4), 60, 500, 70, true);

      //make the textbox do something when someone enters text
      textBox.registerOnTextActionListener(new OnTextActionListener() {
         @Override
         public void onAction(String text) {
            //evaluate the text to see if it's a good carton ID, change screen accordingly
            if((text==null) || (text.isEmpty())) return;
            inform("User typed in [%s]", text);
            setParam("cartonID", text);
            //setParam("location","");
            if (isCartonValid(text)) {
               setNextScreen("ScanItemScreen");
            }
            else {
               setNextScreen("ErrorScreen");
            }
         }
      });


   }

   public void handleTick() {
      super.handleTick();
      scan = getScan(); //See AbstractNuminaScreen
      if ((scan!=null) && (!scan.isEmpty())) setParam("cartonID", scan);
      else return;
      if(isCartonValid(scan)) { //See AbstractNuminaScreen
         //setParam("location","");
         setNextScreen("ScanItemScreen");
      }
      else {
         setNextScreen("ErrorScreen");
      }
   }

}
