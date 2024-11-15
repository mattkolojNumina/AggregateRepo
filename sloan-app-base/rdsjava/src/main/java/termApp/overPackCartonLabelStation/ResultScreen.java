package termApp.overPackCartonLabelStation;


import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class ResultScreen extends AbstractOverPackCartonLabelStationScreen {

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
				getStrParam(TermParam.resultMsg),
				ENTRYCODE_NONE,
				true,
				true);
		initStartTimer();
		nextScreen = "overPackCartonLabelStation.StartScreen";
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