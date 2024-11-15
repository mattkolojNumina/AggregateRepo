package termApp.exception;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import static termApp.util.Constants.*;
import termApp.util.TermActionObject.OnActionListener;

public class IdleScreen
      extends AbstractExceptionStationScreen {

   TextField lastScanDisplay;

   TextField invalidLPNDisplay;
   Rectangle invalidLPNRectangle;

   public IdleScreen( TerminalDriver term ) {
      super( term );
      clearAllParam();	
      setLogoutAllowed(true); 
   }

   /*
    * interface methods
    */

   public void handleInit() {
      super.handleInit();
      initButtons();
   }
   
   @Override
	public void initDisplay() {
		initPromptModule(
				"Please scan the LPN of the next carton to process exceptions.",
				2,
				ENTRYCODE_NONE,
				false,
				true,
				true,
				2);
		super.initDisplay();
	}  

   protected void initButtons() {
      int x = SCREEN_WIDTH - MARGIN;
      int y = 200;
      int f = BTN_FONT;

      Button shortsButton = new Button(x,y,f, "Make Up Shorts",Align.RIGHT,-1,-1,COLOR_YELLOW,true);  
      shortsButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("shorts button pressed");
            setNextScreen("exception.ScanShortScreen");
         }
      });
      shortsButton.show();
   }

   public void handleTick() {
      super.handleTick();
   }

   //Methods created
   protected void processScan(String scan) {
      inform( "entry received text [%s]", scan );
      inputDecision(scan);

   }
   
   public void inputDecision(String scan) {
      int cartonSeq = lookupCartonByLpn(scan);
      //Invalid LPN that does not exist 
      if(cartonSeq<0) {
      	showAlertMsg("No carton found with LPN %s. Restock product.",scan); 
      	initCycleCount(true);
      }
      //Valid LPN that is in our system, begin processing carton
      else {
      	setCarton(cartonSeq);
      	String cartonStatus = getParam(TermParam.cartonStatus);
      	switch(cartonStatus) {
      	case CARTON_STATUS_CANCELED:
      		showAlertMsg("Carton with LPN %s canceled. Restock product.",scan); 
      		initCycleCount(true);
      		break;
      	/*
      	case CARTON_STATUS_SHORT:
      		showActionMsg("Carton %s has short picks. Please place at hold area.",scan);
      		initCycleCount(true);
      		setCartonAtHoldArea(cartonSeq);
      		break;
         */
   		default:
   			nextScreen = getNextScreen(cartonStatus);
   			setNextScreen(nextScreen);
      		break;	
      	}
      }
   }
}
