package termApp.overPackCartonPackStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class ScanLpnScreen extends AbstractOverPackCartonPackStationScreen {

   private Button ok,cancel;


   public ScanLpnScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      reset = (2) * 1000L;
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
   	String cartonLpn = getStrParam(TermParam.cartonLpn);
   	String promptMsg = cartonLpn.isEmpty()?"Scan new LPN barcode":"Confirm LPN barcode";
		initPromptModule(
				promptMsg,
				ENTRYCODE_REGULAR,
				true,
				true);	
		//showActionMsg("Move items from slot to carton");
		super.initDisplay();
	}
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.hide();
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   } 
   
   @Override
   protected void doOkay() {
   	inform("okay button pressed");
   	/*
   	if( requireQC() ) 
   		setNextScreen("overPackCartonPackStation.AuditScreen");			
		else {
			if( requirePacklist() ) {
				setNextScreen("overPackCartonPackStation.PackListScreen");
			} else {
				markCartonPacked();
				setNextScreen("overPackCartonPackStation.ResultScreen");
			}
		}*/
   	setNextScreen("overPackCartonPackStation.AuditScreen");	
   }

   // logic

   @Override
	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      String cartonLpn = getStrParam(TermParam.cartonLpn);
      if( cartonLpn.isEmpty() && isValidCartonLpn(scan) ) {
      	showSuccessMsg("Assign LPN [%s] to carton",scan);
      	updateCartonLpn(scan);
      	ok.show();
      	initStartTimer();
      } else if( !cartonLpn.isEmpty() && cartonLpn.equals(scan) ) { 
      	showSuccessMsg("LPN [%s] validated",scan);
      	ok.show();
      	initStartTimer();
      } else {
      	showAlertMsg("Invalid LPN [%s]",scan);
      }
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers
}