package termApp.packStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

import termApp.util.TermActionObject.OnActionListener;

public class ConfirmDimScreen extends AbstractPackStationScreen {

   private Button confirm,cancel,customize;

   public ConfirmDimScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				String.format("Confirm dimension: %s", getEstDim()),
				ENTRYCODE_NONE,
				true,
				false);   	
		super.initDisplay();
	}
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      confirm = new Button(x,y,f, "Confirm",Align.LEFT,-1,-1,COLOR_WHITE,true);
      confirm.registerOnActionListener(confirmAction());
      confirm.show();
      
      x = W1_2;
      customize = new Button(x,y,f,"Customize",Align.LEFT,-1,-1,COLOR_YELLOW,true);
      customize.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed customize");
            updateCartonType("custom");
            setNextScreen("packStation.CartonDimScreen");
         }
      });
      customize.show();      
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   } 
   
   @Override
   protected void doConfirm() {
   	useEstDim();
		triggerShipLabelRequest();
		setNextScreen("packStation.WaitShipLabelRequestResultScreen");
   }   
   

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}