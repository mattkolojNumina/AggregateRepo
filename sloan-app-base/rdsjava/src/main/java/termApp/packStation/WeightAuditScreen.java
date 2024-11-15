package termApp.packStation;

import static rds.RDSLog.*;

import rds.RDSUtil;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;
import termApp.util.NumPad.NumPadAction;
import termApp.util.NumPad.NumPadConstructor;
import termApp.util.TermActionObject.OnActionListener;

public class WeightAuditScreen extends AbstractPackStationScreen {

   private Button ok,cancel,noKeyboard;
   private TextField padField;
   private NumPad np;
   private boolean auditSuccess;
   private double weight;

   public WeightAuditScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      MAX_WEIGHT = 50.0;
      reset = 5000L;
      auditSuccess = false;
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
   	if(auditSuccess) {
			markCartonAudited();
			if( requirePacklist() ) {
				setNextScreen("packStation.PackListScreen");
			} else {
				markCartonPacked();
				if( isParcel() ) {
					setNextScreen("packStation.CartonWeightScreen");
				} else {
	   			if( isToteOrSplitCase() ) {
	   				triggerShipLabelRequest();
	   				setNextScreen("packStation.WaitLabelScreen");
	   			} else
	   				setNextScreen("packStation.ResultScreen");
				}
			}
   	} else {
   		setNextScreen("packStation.AuditScreen");
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
      weight = -1.0;
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
      if( !isValidWeight(weight) ) {
      	showAlertMsg("Invalid weight %.2f, est. weight %.2f",weight,getDoubleParam(TermParam.estWeight));
      	ok.show();
      	initStartTimer();
      	return;
      }
   	//updateWeight( weight );
   	//setParam(TermParam.actWeight,weight+"");
   	showSuccessMsg("Weight audit passed");
   	auditSuccess = true;
   	ok.show();
   	initStartTimer();
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}