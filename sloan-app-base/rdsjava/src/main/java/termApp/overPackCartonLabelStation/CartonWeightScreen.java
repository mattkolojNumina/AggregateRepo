package termApp.overPackCartonLabelStation;

import static rds.RDSLog.*;

import rds.RDSUtil;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

import termApp.util.NumPad.NumPadAction;
import termApp.util.NumPad.NumPadConstructor;
import termApp.util.TermActionObject.OnActionListener;

public class CartonWeightScreen extends AbstractOverPackCartonLabelStationScreen {

   private Button ok,cancel,noKeyboard,trigger;
   private TextField padField;
   private NumPad np;

   public CartonWeightScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      MAX_WEIGHT = 70.0;
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Place container on scale or enter weight",
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
      
      x = W1_4;
      noKeyboard = new Button(x,y,f,"No Keyboard",Align.CENTER,-1,-1,COLOR_YELLOW,true);
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
      
      x = W2_3;
      trigger = new Button(x,y,f,"Trigger",Align.CENTER,-1,-1,COLOR_YELLOW,true);
      trigger.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
         	triggerScale();
         }
      });
      noKeyboard.show(); 
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   } 
   
   @Override
   protected void triggerScale() {
      db.execute("REPLACE runtime SET NAME = '%s/xmit', VALUE = CONCAT('SGW',0x0D)", getParam("scale"));
   }
   
   @Override
   protected void doOkay() {
   	if( hasActDim() ) {
   		triggerShipLabelRequest();
   		setNextScreen("overPackCartonLabelStation.WaitShipLabelRequestResultScreen");
   	} else {
   		useEstDim();
   		triggerShipLabelRequest();
   		setNextScreen("overPackCartonLabelStation.WaitShipLabelRequestResultScreen");
   	}
   }   
   
   private void initNumPad() {
      int x = W1_2 + 400;
      int y = INFO_Y + 50*2;
      padField = new TextField(x-400,y,65,"", false);

      NumPadConstructor npBuild = new NumPadConstructor(x, INFO_Y);
      np = npBuild.build();
      np.setGlobalBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            padField.updateText(numpadToWeight(value));
         }
      });
      np.setEnterBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            inputDecision(numpadToWeight(value));
         }
      });
      np.hide();
   } 
   
   private String numpadToWeight(String value) {
      return "" + RDSUtil.stringToDouble(value, 0)/100.0;
   }   

   // logic
	@Override
   protected void processScale(double weight_value) {
      inform("weight: "+weight_value);
      inputDecision(weight_value+"");
   }

   public void inputDecision(String weight_str) {
      double weight = -1.0;
      try{
         weight = Double.parseDouble(weight_str);
      } catch(Exception e) {
      	showAlertMsg("Invalid weight value %s",weight_str);
      }
      trace("recorded weight: %.3f",weight);
   	if( weight<MIN_WEIGHT || weight> MAX_WEIGHT ) {
   		showAlertMsg("Invalid weight value %.2f",weight);
   		return;   		
   	}
   	updateWeight( weight );
   	setParam(TermParam.actWeight,weight+"");
   	showSuccessMsg("Get weight: %.2f", weight);
   	ok.show();
   	initStartTimer();
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}