package termApp.packStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

import rds.RDSUtil;
import termApp.util.NumPad.NumPadAction;
import termApp.util.NumPad.NumPadConstructor;
import termApp.util.TermActionObject.OnActionListener;

public class CartonDimScreen extends AbstractPackStationScreen {

   private Button ok,cancel,noKeyboard;
   private TextField padField;
   private NumPad np;

   private int progress;

   public CartonDimScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      progress = 1;
      reset = (2) * (1000L);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Enter carton length",
				ENTRYCODE_REGULAR,
				true,
				true);   	
      initNumPad();
		super.initDisplay();
	}

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      
      x = W1_2;
      noKeyboard = new Button(x,y,f,"No Keyboard",Align.LEFT,-1,-1,COLOR_YELLOW,true);
      noKeyboard.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed no keyboard");
            np.show();
            padField.show();
            promptEntry.hide();
            noKeyboard.hide();
         }
      });
      noKeyboard.show();      
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   @Override
   protected void doOkay() {
		triggerShipLabelRequest();
		setNextScreen("packStation.WaitShipLabelRequestResultScreen");
   }    

   private void initNumPad() {
      int x = W1_2 + 400;
      int y = INFO_Y + 50*2 ;
      padField = new TextField(x-400,y,65,"", false);

      NumPadConstructor npBuild = new NumPadConstructor(x, INFO_Y);
      np = npBuild.build();
      np.setGlobalBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            padField.updateText(numpadToDim(value));
         }
      });
      np.setEnterBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            inputDecision(numpadToDim(value));
         }
      });
      np.hide();
   }
   
   private String numpadToDim(String value) {
      return "" + RDSUtil.stringToDouble(value, 0)/100.0;
   }

   // logic
   
   public void inputDecision(String dim_str) {
      double dimValue = -1.0;
      try{
      	dimValue = Double.parseDouble(dim_str);
      } catch(Exception e) {
      	showAlertMsg("Invalid dim value %s",dim_str);
      }
      if( progress == 1 ) {
         trace("recorded length: %.3f",dimValue);
      	if( dimValue<MIN_LENGTH || dimValue>MAX_LENGTH ) {
      		showAlertMsg("Invalid length value %s",dim_str);
      		return;   		
      	}
      	setParam(TermParam.actLength,dimValue+"");
      	showSuccessMsg("Get length: %.2f", dimValue);
      	updatePromptMsg("","Enter carton width");
      	progress++;
      	return;
      }
      if( progress == 2 ) {
         trace("recorded width: %.3f",dimValue);
      	if( dimValue<MIN_WIDTH || dimValue>MAX_WIDTH ) {
      		showAlertMsg("Invalid width value %s",dim_str);
      		return;   		
      	}
      	setParam(TermParam.actWidth,dimValue+"");
      	showSuccessMsg("Get width: %.2f", dimValue);
      	updatePromptMsg("","Enter carton height");
      	progress++;
      	return;
      }
      if( progress == 3 ) {
         trace("recorded height: %.3f",dimValue);
      	if( dimValue<MIN_HEIGHT || dimValue>MAX_HEIGHT ) {
      		showAlertMsg("Invalid height value %s",dim_str);
      		return;   		
      	}
      	setParam(TermParam.actHeight,dimValue+"");
      	showSuccessMsg("Get height: %.2f", dimValue);
      	updatePromptMsg("","Get dim as %s", getActDim());
      }
      updateActDim();
   	ok.show();
   	initStartTimer();
   }   

   public void handleTick() {
      super.handleTick();
   }

   // helpers
}