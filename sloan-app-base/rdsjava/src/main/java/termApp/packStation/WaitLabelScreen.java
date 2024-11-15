package termApp.packStation;

import java.util.Map;
import java.util.ArrayList;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class WaitLabelScreen extends AbstractPackStationScreen {

   private Button ok,cancel;
   private boolean requireShipLabel, requireParcelUccLabel, requireLtlUccLabel, requirePalletLabel;
   private boolean hasShipLabel, hasParcelUccLabel, hasLtlUccLabel, hasPalletLabel, hasAllLabel;

   public WaitLabelScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      //processMode = getIntParam(TermParam.processMode);
      //requireShipLabel = requireParcelUccLabel = requireLtlUccLabel = requirePalletLabel = false;
      requireShipLabel = true;
      /*
      if( processMode == MODE_PALLET_LABEL)
      	requirePalletLabel = true;
      else if( processMode == MODE_PARCEL_PALLET_CARTON || processMode == MODE_PARCEL_CARTON ) {
      	requireShipLabel = true;
      	requireParcelUccLabel = requireUccLabel();
      } else 
      	requireLtlUccLabel = true;
   	*/
      hasShipLabel = hasParcelUccLabel = hasLtlUccLabel = hasPalletLabel = hasAllLabel = false;
      reset = 200L;
      processing = System.currentTimeMillis();
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Waiting ship label from ShipExec",
				ENTRYCODE_NONE,
				true,
				true);
		initScrollBoxModule(true);
		setScrollBox();
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
   	setNextScreen("packStation.LabelPrintScreen");
   }

   private void setScrollBox() {
      int x = 100;
      int w = 600;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Label Type", "docTypeDisplay");
      x += w;
      w = 600;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Verify", "verification");
      scrollBox = scrollBoxModel.build();
      scrollBox.show();  	
   }
   
   private void setScrollBoxList() {
   	/*
   	if( processMode == MODE_PALLET_LABEL )
   		scrollBoxList = getPalletLabels();
   	else
   		scrollBoxList = getCartonLabels();
   	*/
   	scrollBoxList = getCartonLabels();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else {
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList); 
      	if( !hasAllLabel ) {
	      	for( Map<String,String> m : scrollBoxList ) {
	      		String docType = getMapStr(m,"docType");
	      		if( docType.equalsIgnoreCase("shipLabel") ) {
	      			if( requireShipLabel )
	      				hasShipLabel = true;
	      			if( requireLtlUccLabel)
	      				hasLtlUccLabel = true;
	      			if( requirePalletLabel)
	      				hasPalletLabel = true;
	      		}
	      		if( docType.equalsIgnoreCase("4x6") ) {
	      			if( requireParcelUccLabel )
	      				hasParcelUccLabel = true;
	      		}
	      	}
	         if( ((requireShipLabel && hasShipLabel) || !requireShipLabel ) && 
	               ((requireParcelUccLabel && hasParcelUccLabel) || !requireParcelUccLabel )	&& 
	               ((requireLtlUccLabel && hasLtlUccLabel) || !requireLtlUccLabel ) && 
	               ((requirePalletLabel && hasPalletLabel) || !requirePalletLabel )) {
	           	hasAllLabel = true;
	           	ok.show();
	           	processing = 0;
	           	showSuccessMsg("Get all required labels");
	           	initStartTimer();
	         }
      	}
      }
      scrollBox.updateDisplayList(scrollBoxList);
   }
   // logic 

   public void handleTick() {
      super.handleTick();
      if( scrollBox != null )
      	setScrollBoxList();
      if( processing > 0 && System.currentTimeMillis() - processing > timeout ) {
         showAlertMsg("Timeout getting required label");
         processing = 0;
      }
   }

   // helpers

   
}