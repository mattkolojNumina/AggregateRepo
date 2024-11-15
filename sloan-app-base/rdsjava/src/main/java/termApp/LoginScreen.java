package termApp;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.TermActionObject.OnTextActionListener;

public class LoginScreen
      extends AbstractNuminaScreen {

   TextEntry entry;
   TextWrap msg;
   String scan;
   
   public LoginScreen( TerminalDriver term ) {
      super( term );
   }

   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();  // displays login footer

      header.updateTitle( "LOGIN SCREEN" );
      
      new TextField(100, 500, 60, "Scan Login ID");
      msg = new TextWrap(100, 300, 1400, 50, 3);
      msg.show();
      entry = new TextEntry( 100,700,60,1400,70,true);
      entry.registerOnTextActionListener(new OnTextActionListener() {
         @Override
         public void onAction(String text) {
            inform( "entry received text [%s]", text );
            processLogin( text );            
         }
      });
      //term.setFocus(entry.getTag());
   }
   
   public void handleTick() {
      super.handleTick();
      scan = getScan();
      if ((scan==null) || (scan.isEmpty())) return;
      processLogin(scan);      
   }

   protected void processScan(String scan) {
      inform( "scan received [%s]", scan );
      processLogin( scan );
   }
   
   protected void processLogin( String loginId ) {
      if ( !isOperator(loginId) ) {
         msg.wrap("Invalid operator: [%s]", loginId );
         return;
      }

//      if ( operatorLoggedIn(loginId) ) {
//         msg.wrap("Operator [%s] already logged in %s", loginId, getOperatorLoggedInLocation(loginId) );
//         return;
//      }
      
      doLogin(loginId);
   }
   
}
