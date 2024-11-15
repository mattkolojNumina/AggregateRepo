package termApp;

import static rds.RDSLog.inform;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.Button;
import termApp.util.TextField;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

public class ChangeModeScreen
      extends AbstractProjectAppScreen {

   private TextField msg;

   public ChangeModeScreen( TerminalDriver term ) {
      super( term );
   }

   /*
    * interface methods
    */

   public void initDisplay() {
      header.updateTitle("SELECT STATION MODE");
      msg = new TextField(100, 750, 75, ""); 
      updateModeMsg();
      initButtons();
   }

   private void updateModeMsg() {
      msg.updateText("Current Mode: %s",getMode());
   }

   private String getMode() {
      String mode = getStrParam( "mode" );
      inform("current mode: %s",mode) ;
      return modeName(mode);
   }

   private String modeName(String mode) {
      if (mode == null)
         return "";
      switch(mode) {
      case "cart":
         return "Cart Build";
      case "audit":
         return "Audit";
      case "exception":
         return "Exception";
      case "bulk":
      //case "pack":
      case "orderStart":
      default: 
         return "Carton Start";
      }
   }

   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      int w = W1_2 - 400;

      Button btn = new Button(0, 0, 0, "",Align.LEFT,false);

      
      x = W1_4;
      y = 300;
      //String mode = "pack";
      String mode = "orderStart";
      btn = new Button(x, y, f, modeName(mode),Align.CENTER, w,true);
      btn.registerOnActionListener(setMode(mode));
      btn.show();
      
      /*x = W1_4;
      y = 500;
      mode = "audit";
      btn = new Button(x, y, f, modeName(mode),Align.CENTER, w,true);
      btn.registerOnActionListener(setMode(mode));
      btn.show();*/
      
      x = W3_4;
      y = 300;
      mode = "cart";
      btn = new Button(x, y, f, modeName(mode),Align.CENTER, w,true);
      btn.registerOnActionListener(setMode(mode));
      btn.show();
      
      /*x = W3_4;
      y = 500;
      mode = "exception";
      btn = new Button(x, y, f, modeName(mode),Align.CENTER, w,true);
      btn.registerOnActionListener(setMode(mode));
      btn.show();*/
      
   }
   
   private OnActionListener setMode(String mode) {
      return new OnActionListener() {
         @Override
         public void onAction() {
            saveMode(mode);
            updateModeMsg();
            setNextScreen(determineStartPage());
         }
      };   
   }
   
   public void handleTick() {

   }

}