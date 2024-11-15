package termApp;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import static termApp.util.Constants.*;
import termApp.util.NumPad.NumPadAction;
import termApp.util.NumPad.NumPadConstructor;
import termApp.util.TermActionObject.OnActionListener;
import termApp.util.TermActionObject.OnTextActionListener;

public class LoginNumPadScreen
      extends AbstractProjectAppScreen {

   TextEntry entry;
   TextWrap msg;
   
   public LoginNumPadScreen( TerminalDriver term ) {
      super( term );
   }

   /*
    * interface methods
    */

   public void initDisplay() {
      super.initDisplay();  // displays login footer

      header.updateTitle( "LOGIN SCREEN" );
      header.on();
      
      new TextField(100, 700, 60, "Scan or Enter Login ID");
      msg = new TextWrap(100, 300, 1600, 80, 3);
      msg.show();
      entry = new TextEntry( 100,800,60,760,70,true);
      entry.registerOnTextActionListener(new OnTextActionListener() {
         @Override
         public void onAction(String text) {
            inform( "entry received text [%s]", text );
            processLogin( text );            
         }
      });
      
      entry.setFocus();
      
      initNumPad();
   }
   
   private void initNumPad() {
      int x = W1_2 + 400;
      int y = 400 ;
      TextField field = new TextField(x-400,y+200,65,"", true);

      NumPadConstructor npBuild = new NumPadConstructor(x, 500);
      NumPad np = npBuild.build();
      np.setGlobalBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            field.updateText(value);
         }
      });
      np.setEnterBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            processLogin(value);
         }
      });
      np.on();
      np.show();
   }

   public void handleTick() {
      super.handleTick();
      
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