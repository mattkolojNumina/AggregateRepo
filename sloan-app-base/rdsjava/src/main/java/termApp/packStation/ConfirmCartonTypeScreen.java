package termApp.packStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;

import static termApp.util.Constants.*;

import java.util.ArrayList;
import java.util.Map;

public class ConfirmCartonTypeScreen extends AbstractPackStationScreen {

   private Button confirm,cancel;
   private String selectedCartonType;

   public ConfirmCartonTypeScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      selectedCartonType = getStrParam(TermParam.cartonType);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				String.format("Put items from tote to suggested/selected type"),
				ENTRYCODE_NONE,
				true,
				true);   
		showActionMsg("Selected %s; suggested %s.", selectedCartonType, getStrParam(TermParam.cartonType));
		initScrollBoxModule(true);
		setScrollBox();
		super.initDisplay();
	}
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      confirm = new Button(x,y,f, "Confirm",Align.LEFT,-1,-1,COLOR_WHITE,true);
      confirm.registerOnActionListener(confirmAction());
      confirm.show();    
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   } 
   
   @Override
   protected void doConfirm() {
   	String suggested = getStrParam(TermParam.cartonType);
   	if( selectedCartonType.equals(suggested) ) {
   		useEstDim();
   	} else {
   		updateCartonTypeAndDim(suggested,selectedCartonType);
   	}
   	if( requireQC() ) 
   		setNextScreen("packStation.AuditScreen");			
		else 
			setNextScreen("packStation.WeightAuditScreen"); 
   } 
   
   private void setScrollBox() {
      int x = 10;
      int w = 400;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Carton Type", "cartonType");
      
      x += w;
      w = 250;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Length", "exteriorLength");
      
      x += w;
      w = 250;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Width", "exteriorWidth");
      
      x += w;
      w = 250;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Height", "exteriorHeight");
      
      x += w;
      w = -1;
      Button btn = new Button(10,-5,30," Select ",Align.LEFT,w,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
         	selectCartonType(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);   
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }  
   
   private void selectCartonType(Map<String, String> p) {
   	selectedCartonType = getMapStr(p, "cartonType");
      inform("button push: cartonType [%s]",selectedCartonType );
      showActionMsg("Selected %s; suggested %s.", selectedCartonType, getStrParam(TermParam.cartonType));
   }
   
   private void setScrollBoxList() {
   	scrollBoxList = getCartonTypes();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);      
      scrollBox.updateDisplayList(scrollBoxList);
   }
   

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}