package termApp.palletBuild;

import termApp.*;
import term.TerminalDriver;


public class AbstractPalletBuild extends AbstractNuminaScreen {


   public AbstractPalletBuild( TerminalDriver term ) {
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
      header.updateTitle("SHIPPING LABEL SCREEN");
      footer.show();


   }

   public void handleTick() {
      super.handleTick();

   }




}
