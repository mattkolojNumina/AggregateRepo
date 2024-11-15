package termApp.overPackCartonPackStation;


import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class ResultScreen extends AbstractOverPackCartonPackStationScreen {

   private Button ok;

   public ResultScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      reset = 8000L;
      processing = System.currentTimeMillis();
      processMode = getIntParam(TermParam.processMode);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Carton packed, bring to overpack label station",
				ENTRYCODE_NONE,
				true,
				true);
		initStartTimer();
		nextScreen = "overPackCartonPackStation.StartScreen";
   	if( processMode == MODE_PALLET ) {
   		if( numNotPackedCarton() == 0 ) {
   			markPalletClosed();
   			showSuccessMsg("Pallet complete");
   		} else 
   			nextScreen = "overPackCartonPackStation.ScanSlotScreen";
   	}
		super.initDisplay();
	}
   
   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      leftInfoBox.updateInfoPair(0, getStrParam(TermParam.refType), getStrParam(TermParam.refValue) );
      leftInfoBox.show(); 
   }
   

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,true);
      ok.registerOnActionListener(okAction());
   }
   
   @Override
   protected void doOkay() {
   	setNextScreen(nextScreen);
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

   
}