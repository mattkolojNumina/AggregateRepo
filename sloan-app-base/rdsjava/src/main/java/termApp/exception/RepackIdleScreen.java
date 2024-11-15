package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

import java.util.Map;
import java.util.List;


public class RepackIdleScreen
      extends AbstractExceptionStationScreen {

   private Button cancel, cancelRepack;

   private final int INVALID_LPN_MESSAGE_TIMEOUT = 10;
   private int timeoutCounter;

   TextField newCartonLPNDisplay;
   Rectangle newCartonLPNRectangle;
   TextField invalidLPNDisplay;
   Rectangle invalidLPNRectangle;

   TextField invalidTypeClassDisplay1;
   TextField invalidTypeClassDisplay2;
   Rectangle invalidTypeClassRectangle;

   TextField cartonLPNDisplay;
   TextField informationDisplay;
   Rectangle informationRectangle;
      
   TextField action1Display;
   TextField action2Display;
   Rectangle actionRectangle;

   String oldCartonType;
   String oldTypeClass;
   List<Map<String, String>> potentialNewCartonTypes;

   public RepackIdleScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
   }

   /*
    * interface methods
    */
   
   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
		initPromptModule(
				"Marked for repack. Scan LPN of new carton.",
				2,
				ENTRYCODE_NONE,
				true,
				true,
				true,
				2);
		super.initDisplay();
	}
   
   protected void initButtons() {
      int x = W1_2;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      cancelRepack = new Button(x,y,f, "No Repack needed",Align.CENTER,-1,-1,COLOR_YELLOW,true);
      cancelRepack.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed cancel repack");
            clearRepack();
         }
      });
      
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }  
   
   protected void clearRepack() {
   	int cartonSeq = getIntParam(TermParam.cartonSeq);
   	doClearRepackFlag(cartonSeq);
   	setNextScreen("exception.IdleScreen");
   }

   public void handleTick() {
      super.handleTick();
      timeoutCounter++;
      if(timeoutCounter > INVALID_LPN_MESSAGE_TIMEOUT) {
      	hideResultMsgModule();
         timeoutCounter = 0;
      }
      if(timeoutCounter > 100000){
         timeoutCounter = 0;
      }
   }
   
   //Methods created
   protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
   }  

   //Methods created
   public void inputDecision(String scan) {
      String newLpnResutl = isValidLpn(scan);
      if( newLpnResutl.equals(LPN_EXIST) ) {
      	showAlertMsg("Duplicate LPN %s scanned, please scan an unused LPN.", scan);
      	initCycleCount(true);
      } else if( newLpnResutl.equals(LPN_INVALID_FORMAT) ) {
      	showAlertMsg("Lpn of invalid carton type scanned, please scan a LPN starts with %s",getParam(TermParam.lpnPrefix));
      	initCycleCount(true);
      } else {
      	setNextScreen("exception.RepackActionScreen");
      }
   }
}
