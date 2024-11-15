package termApp.packStation;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class WaitShipLabelRequestResultScreen extends AbstractPackStationScreen {

   private Button ok,cancel;
   private boolean checkShipLabelRequestStatus;

   public WaitShipLabelRequestResultScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      processing = System.currentTimeMillis();
      checkShipLabelRequestStatus = true;
      reset = 1000L;
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Sending ship label request to shipExec",
				ENTRYCODE_NONE,
				true,
				true);
		super.initDisplay();
	}

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   @Override
   protected void doOkay() {
   	setNextScreen("packStation.WaitLabelScreen");
   }

   // logic 

   public void handleTick() {
      super.handleTick();
      if(checkShipLabelRequestStatus)
      	checkShipLabelRequestStatus();
      if( processing > 0 && System.currentTimeMillis() - processing > timeout ) {
         showAlertMsg("ShipExec label request timeout!");
         processing = 0;
      }
   }
   
   private void checkShipLabelRequestStatus() {
		int uploadStatus = shipLabelRequestStatus();
		if( uploadStatus == 1 ) {
			showSuccessMsg("Ship label request uploaded");
			processing = 0;
			ok.show();
			initStartTimer();
			checkShipLabelRequestStatus = false;
		} 
		if( uploadStatus == -1 ) {
			showAlertMsg("Error sending ship label request to ShipExec");
			processing = 0;
			checkShipLabelRequestStatus = false;
		} 
   }

   // helpers

   
}