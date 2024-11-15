package termApp.overPackCartonPackStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class PackListScreen extends AbstractOverPackCartonPackStationScreen {

   private Button ok,cancel,reprint;
   private int printJobSeq;
   private boolean checkPrintJob;;

   public PackListScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      checkPrintJob = false;
   }

   // init

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Printing packlist",
				ENTRYCODE_NONE,
				true,
				true);
      startPrintJob();
		super.initDisplay();
	}

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.hide();
      
      x= W1_2;
      reprint = new Button(x,y,f, "Reprint",Align.CENTER,-1,-1,COLOR_WHITE,false);
      reprint.registerOnActionListener(reprintAction());
      reprint.hide();
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   @Override
   protected void doOkay() {
   	inform("okay button pressed");
		markCartonPacked();
		setNextScreen("overPackCartonPackStation.ResultScreen");
   }
   
   @Override
   protected void doReprint() {
   	showActionMsg("Reprinting packlist");
   	startPrintJob();
   }
   
   // logic
   
   private void startPrintJob() {
      initProcessTimer();
      printJobSeq = printPacklist();
      if( printJobSeq > 0 ) {
         checkPrintJob = true;
      } else {
         if(printJobSeq == PRINT_ERROR_DATABASE) {
         	showAlertMsg("Error connecting to database");
         } else if(printJobSeq == PRINT_ERROR_NOLABEL) {
         	showAlertMsg("No document found");         	
         } else {
         	showAlertMsg("Something went wrong");   
         }  
      }
   }

   public void handleTick() {
      super.handleTick();
      checkPrintJob();
   }
   
   private void checkPrintJob() {
      if(checkPrintJob) {
      	int complete = getPrintJobStatus( printJobSeq );
         if( complete == 1 ) {
      		showSuccessMsg("Printing complete");
         	reprint.show();
         	ok.show();
         	checkPrintJob = false;
         } else {
            if(processing == 0)
               initProcessTimer();
         }
      }
      if( checkPrintJob && processing > 0 && System.currentTimeMillis() - processing > timeout ) {
         showAlertMsg("Timeout printing. Check document printer.");
         checkPrintJob = false;
         processing = 0;
         reprint.show();
      }
   }

   // logic


}