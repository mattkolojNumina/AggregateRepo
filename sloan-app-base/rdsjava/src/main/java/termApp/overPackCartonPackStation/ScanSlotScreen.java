package termApp.overPackCartonPackStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;

import static termApp.util.Constants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ScanSlotScreen extends AbstractOverPackCartonPackStationScreen {

   private Button cancel;


   public ScanSlotScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
		initPromptModule(
				"Scan Slot barcode",
				ENTRYCODE_REGULAR,
				true,
				true);
		initScrollBoxModule(true);
		setScrollBox();		
		super.initDisplay();
	}
   
   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      leftInfoBox.updateInfoPair(0, "Pallet", getStrParam(TermParam.palletLpn) );
      leftInfoBox.show(); 
   }
   
   protected void initButtons() {
      int x = SCREEN_WIDTH - MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }   
      
   private void setScrollBox() {
      int x = 10;
      int w = 150;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Slot", "cartSlot");
      
      x += w;
      w = 400;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "LPN", "lpn");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "# Items", "totalQty");
      
      x += w;
      w = -1;
      Button btn = new Button(10,-5,30," Choose ",Align.LEFT,w,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            chooseSlot(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);   
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }   

   private void chooseSlot(Map<String, String> p) {
      boolean packed = !getMapStr(p,"packStamp").isEmpty();
      boolean canceled = !getMapStr(p,"cancelStamp").isEmpty();
      String slot = getMapStr(p,"cartSlot");
      if( packed )
      	showActionMsg("Slot %s is packed.",slot);
      else if (canceled)
      	showAlertMsg("Slot %s is canceled!",slot);
      else {
	   	setParam(TermParam.cartonSeq,getMapStr(p,"cartonSeq"));
	   	setParam(TermParam.cartonLpn,getMapStr(p,"lpn"));
	   	setParam(TermParam.cartonType,getMapStr(p,"cartonType"));
	   	determineNextScreen();
      }
   }  
   
   private void setScrollBoxList() {
   	/*
   	if( isCartonLabelFullPallet() )
   		scrollBoxList = getCartonLabelFullPalletSku();
   	else
   		scrollBoxList = getPalletCartonSkuList();
		*/
   	scrollBoxList = getPalletCartonSlotList();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);  
      for (Map<String, String> p : scrollBoxList) {
      	boolean canceled = !getMapStr(p,"cancelStamp").isEmpty();
      	boolean packed = !getMapStr(p,"packStamp").isEmpty();
      	if( canceled )
         	p.put("background", "red");
      	else if( packed )
      		p.put("background", "green");
      }      
      scrollBox.updateDisplayList(scrollBoxList);
   }
   
   private void determineNextScreen() {
		setNextScreen("overPackCartonPackStation.ConfirmCartonTypeScreen");  	
   }

   // logic

   @Override
	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
   }
   
   public void inputDecision(String scan) {  
      Iterator<Map<String, String>> itr = scrollBoxList.iterator();
      while (itr.hasNext()) {
         Map<String, String> p = itr.next();
         String cartSlot = getMapStr(p,"cartSlot");
         if ( !scan.equals(cartSlot) )
            continue;
         chooseSlot(p);
         return;
      }
      showAlertMsg("No Slot found with scan [%s]",scan);
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers
}