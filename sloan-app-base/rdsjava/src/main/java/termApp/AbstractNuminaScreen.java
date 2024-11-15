/*
 * AbstractNuminaScreen.java
 */

package termApp;

import static rds.RDSLog.*;

import java.util.regex.*;
import java.lang.*;

import term.TerminalDriver;
import termApp.util.*;
import static termApp.util.Constants.*;

/**
 * Base class for terminal screens, defining constants and default
 * behavior.
 */
public abstract class AbstractNuminaScreen
      extends AbstractScreen {
   
      protected TextField statusMsg;
      private Pattern CARTON_REGEX = Pattern.compile("C\\d{8}");
   /** Constructs a screen with default values. */
   public AbstractNuminaScreen( TerminalDriver term ) {
     super( term );
     //default to size 75 black text, blank message, left aligned, and hidden
     statusMsg = new TextField(100, SCREEN_HEIGHT/3);
   }

   /*
    * ScreenHandler interface methods
    */

   /** Processes a button press. */
   public void handleButton( int tag ) {
      try {
         TermUtility.getButtons().getBoolean("doAction", tag);
      } catch ( NullPointerException ex ) {
      } catch ( Exception ex ) {
         alert(ex);
      }
   }

   /** Processes text from an entry field. */
   public void handleEntry( int tag, String text ) {
      try {
         TermUtility.getTextEntries().getBoolean("doAction", tag, text);
         TermUtility.getPasswordEntries().getBoolean("doAction", tag, text);
      } catch ( NullPointerException ex ) {
      } catch ( Exception ex ) {
         alert(ex);
      }   
   }
    
   /*
    * processing methods
    */

   protected String getScan() {
      String scan = db.getString("",
            "SELECT value FROM runtime WHERE name='%s'", getParam("scanner")
      );
      //if no scan was recorded, then we're done.
      if ((scan == null) || (scan.isEmpty()))  return null;

      //trace the scan
      trace("got scan [%s]", scan);
      //blank the runtime entry so we don't re-use the same scan
      db.execute("UPDATE runtime SET value='' WHERE name='%s'", getParam("scanner"));
      return scan;
   }

   protected void disableScanner() {
      if ((getParam("scanner")==null) || (getParam("scanner").isEmpty())) return;
      if (getParam("scanner").indexOf("srlScan") < 0) return;
      inform("disable scanner");
      db.execute("UPDATE runtime SET value = 'disable' " +
                 "WHERE name='%s-cmd'", getParam("scanner")
      );
   }

   protected void enableScanner() {
      if ((getParam("scanner")==null) || (getParam("scanner").isEmpty())) return;
      if (getParam("scanner").indexOf("srlScan") < 0) return;
      inform("enable scanner");
      db.execute("UPDATE runtime SET value = 'enable' " +
                 "WHERE name='%s-cmd'", getParam("scanner")
      );
   }   
  
   protected String getScale(){
      String scan = db.getString("",
            "SELECT value FROM runtime WHERE name='%s'", getParam("scale")
      );
      //if no scan was recorded, then we're done.
      if ((scan == null) || (scan.isEmpty()))  return null;

      //trace the scan
      trace("got scale [%s]", scan);
      //blank the runtime entry so we don't re-use the same scan
      db.execute("UPDATE runtime SET value='' WHERE name='%s'", getParam("scale"));
      return scan;
   }

   
   
   protected boolean isCartonValid(String scan) {
      if((scan == null) || (scan.isEmpty())) return false;
      inform("Checking validity of [%s]", scan);
      boolean result = CARTON_REGEX.matcher(scan).matches();
      if (result) {
         inform("%s is valid!", scan);
      }
      else {
         inform("%s is not valid!", scan);
      }
      return result;
   }
}

