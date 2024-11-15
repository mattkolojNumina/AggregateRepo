package termApp;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.ScrollBox.ScrollBoxConstructor;

import static termApp.util.Constants.*;

import java.util.Map;

public class ControlsScreen
      extends AbstractScreen {

   ScrollBox table;
   
   public ControlsScreen( TerminalDriver term ) {
      super( term );
   }

   /*
    * interface methods
    */

   public void initDisplay() {
      term.clearScreen(DEFAULT_SCREEN_COLOR);
      
      header.updateTitle("Controls for %s", term.getTermName());

      ScrollBoxConstructor model = new ScrollBoxConstructor(100, INFO_Y);
      model.setFont(60, 50);
      model.setRows(20, 5);
      model.setMargins(60, 0, 0, 0);
      model.setWidth(SCREEN_WIDTH-200);
      model.setIndexJump(4);
      model.drawHeaders(true);
      model.addColumn(0, 0, Align.LEFT, "Name", "name");
      model.addColumn(0, 0, Align.LEFT, "Value", "value");
      table = model.build();
      table.show();
 
   }

   public void handleTick() {
      @SuppressWarnings("unused")
      Map<String,String> map = db.getControlMap( term.getTermName());
      
     /* table.updateInfoName(0, format, args);setPair(0, 0, "updateText", "Name", "Value");
      table.setElement( 1, 0, "updateText", "SCANNER");
      table.setElement( 2, 0, "updateText", "MAC");
      String scanner = getMapStr(map, "scanner");
      if (!scanner.isEmpty()) {
         String piHost = scanner.split("/")[1];
         Map<String,String> scannerMap = db.getControlMap( piHost, "btScan1");
         table.setElement( 2, 1, "updateText", getMapStr(scannerMap, "mac"));

         String scan = db.getRuntime(scanner);
         if (scan != null && !scan.isEmpty() ) {
            RDSLog.trace("scan [%s]", scan);
            table.setElement( 1, 1, "updateText", scan);
            db.setRuntime(scanner, "");
         }
      }

      int i = 3;
      for (String key : map.keySet() ) {
         table.setPair(i++, 0, "updateText", key, getMapStr(map,key));
      }
*/
   }
}
