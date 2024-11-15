package termApp.packStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;

import static termApp.util.Constants.*;

import java.util.ArrayList;
import java.util.Map;

public class ScanSkuScreen extends AbstractPackStationScreen {

   private Button cancel;


   public ScanSkuScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
		initPromptModule(
				"Scan SKU barcode",
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
      int w = 400;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "SKU", "sku");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "UOM", "uom");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Need", "totalQty");
      
      x += w;
      w = 180;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Labeled", "labeledQty");
      
      x += w;
      w = -1;
      Button btn = new Button(10,-5,30," Choose ",Align.LEFT,w,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            chooseSku(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);   
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }   

   private void chooseSku(Map<String, String> p) {
      boolean canChoose = getMapInt(p,"labeledQty")<getMapInt(p,"totalQty");
      if( canChoose ) {
      	String sku = getMapStr(p,"sku");
      	String uom = getMapStr(p,"uom");
	   	int cartonSeq = lookupPalletCartonBySku(sku,uom);
	   	if( cartonSeq > 0 ) {
	   		determineNextScreen();
	   	} else {
	   		showAlertMsg("Failed to get carton for SKU %s",sku);
	   	}
      }
   }  
   
   private void setScrollBoxList() {
   	/*
   	if( isCartonLabelFullPallet() )
   		scrollBoxList = getCartonLabelFullPalletSku();
   	else
   		scrollBoxList = getPalletCartonSkuList();
		*/
   	scrollBoxList = getPalletCartonSkuList();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);  
      for (Map<String, String> p : scrollBoxList) {
         int skuTotalQty = getMapInt(p,"totalQty");
         int skuLabeledQty = getMapInt(p,"labeledQty");
         if( skuLabeledQty == skuTotalQty )
         	p.put("background", "green");
      }      
      scrollBox.updateDisplayList(scrollBoxList);
   }
   
   private void determineNextScreen() {
		if( requireQC() || false) {
   		setNextScreen("packStation.AuditScreen");
   		return;   			
		} else {
			markCartonAudited();
   		if( requirePacklist() ) {
   			setNextScreen("packStation.PackListScreen");
   			return;
   		} else {
 				markCartonPacked();
      		int uploadStatus = shipLabelRequestStatus();
      		if( uploadStatus<0 ) {
      			useEstWeight();
      			setNextScreen("packStation.ConfirmDimScreen");
      		} else if( uploadStatus == 0 ) {
      			setNextScreen("packStation.WaitShipLabelRequestResultScreen");
      		} else {
      			setNextScreen("packStation.WaitLabelScreen"); 			
      		}
   			return;
   		}   				
		}   	
   }

   // logic

   @Override
	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   /*
   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      if( isCartonLabelFullPallet() ) {
      	int cartonSeq = getNextPalletCarton(scan);
	   	if( cartonSeq > 0 ) {
	   		determineNextScreen();
	   	} else {
	   		showAlertMsg("Failed to get carton for barcode %s",scan);
	   	}      	
      } else {     
	   	int cartonSeq = lookupPalletCartonBySku(scan);
	   	if( cartonSeq > 0 ) {
	   		determineNextScreen();
	   	} else {
	   		showAlertMsg("Failed to get carton for barcode %s",scan);
	   	}
      }
   }*/
   
   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);   
   	int cartonSeq = lookupPalletCartonBySku(scan);
   	if( cartonSeq > 0 ) {
   		determineNextScreen();
   	} else {
   		showAlertMsg("Failed to get carton for barcode %s",scan);
   	}
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers
}