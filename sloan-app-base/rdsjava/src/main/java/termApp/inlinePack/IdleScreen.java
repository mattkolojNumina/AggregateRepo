package termApp.inlinePack;

import term.TerminalDriver;
import rds.RDSCounter;
import rds.RDSTrak;

import static rds.RDSLog.*;

public class IdleScreen
extends AbstractInLinePackStationScreen {

   boolean cartonWaiting = false;
   
   public IdleScreen(TerminalDriver term) {
      super(term);
      setLogoutAllowed(true);
      clearAllParam();
      cartonWaiting = isCartonWaiting();
   }

   /*
    * interface methods
    */

   /** Initializes the screen and lays out UI elements. */
   public void initDisplay() {
		initPromptModule(
				"Waiting for next carton.",
				ENTRYCODE_NONE,
				true,
				true);
		super.initDisplay();      
   }

   
   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();  // calls handleScan -> processScan
      if (pollForCarton())
         return;
      pollForWaitingCarton();
   }

   /*
    * processing methods
    */

   @Override
   protected void processScan(String scan) {
      trace("processing scan [%s]", scan);
      
      if(scan == null || scan.isEmpty()) {
         return;
      }

      setParam(TermParam.scan, scan);
      setParam(TermParam.cartonLpn, scan);
      setNextScreen("inlinePack.ProcessBox");
   }

   private boolean pollForCarton() {
      String lpn = db.getRuntime(runtimeLPN);

      if (lpn == null || lpn.isEmpty()) {
         return false;
      }

      inform("Found carton [%s] in runtime [%s]", lpn, runtimeLPN);
      setParam(TermParam.cartonLpn, lpn);
      RDSCounter.increment("inlinePack", "inducted");
      setNextScreen("inlinePack.ProcessBox");
      return true;
   }
   
   /*
    * Checks if a carton is waiting in the previous zone, 
    * so the operator knows to press the foot swtich
    */
   private boolean pollForWaitingCarton() {
      boolean waiting = isCartonWaiting();
      if (waiting == cartonWaiting)
         return false;
      
      cartonWaiting = waiting;
      updateMsg();
      return true;
   }

   private boolean isCartonWaiting() {
      if (preZone == null || preZone.isEmpty())
         return false;
      int box = RDSTrak.read( preZone, REG_BOX );
      if (box == BOX_NONE || box == BOX_EMPTY) 
         return false;
      
      if (!cartonWaiting)
         inform( "Detected box ID [%d] waiting in previous zone [%s]", box, preZone );

      return true;
   }

   /*
    * display methods
    */

   private void updateMsg() {
      if (cartonWaiting) {
      	showActionMsg("Carton waiting, press foot switch.");;
      } else 
      	hideResultMsgModule();
   }
   
}
