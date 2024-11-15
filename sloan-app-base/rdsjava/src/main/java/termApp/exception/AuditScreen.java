package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;

import java.util.Map;

import java.util.ArrayList;
import java.util.Iterator;

import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;
import termApp.util.TermActionObject.OnActionListener;

public class AuditScreen
      extends AbstractExceptionStationScreen {

   TextField lastScanDisplay;

   private Button ok,cancel,countComplete,continueLabel;
   private boolean goodCarton, pickFixed;
   private int totalQty, scannedQty, pickedQty;
   private String promptMsg;

   public AuditScreen( TerminalDriver term ) {
      super( term );
      setLogoutAllowed(false);
      goodCarton = pickFixed = false;
      totalQty = scannedQty = pickedQty = 0;
      promptMsg = "";
   }

   /*
    * interface methods
    */

   /*
   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("AUDIT STATION");
      footer.show();

      lastScanDisplay = new TextField(100, 850, 60, TEXT_COLOR, "Last Scan: ", Align.LEFT, true);

   }*/

   public void handleTick() {
      super.handleTick();
   }

   //Methods created
/*    protected void processScan(String scan) {
      inform( "entry received text [%s]", scan );

      lastScanDisplay.updateText("Last Scan: " + scan);

      setNextScreen(getNextScreen(getParam("cartonSeq")));
   } */

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
   	promptMsg = "Scan next SKU or confirm qty";
		initPromptModule(
				promptMsg,
				ENTRYCODE_NONE,
				true,
				true,2);
		initScrollBoxModule(true);
		setScrollBox();
		super.initDisplay();
	}  

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Done",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.hide();   
      
      continueLabel = new Button(x,y,f, "Continue label",Align.LEFT,-1,-1,COLOR_WHITE,false);
      continueLabel.registerOnActionListener(okAction());
      continueLabel.hide();
      
      x = W1_2;
      countComplete = new Button(x,y,f, "Count Complete",Align.CENTER,-1,-1,COLOR_WHITE,true);
      countComplete.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed count complete");
            countComplete();
         }
      });
      countComplete.show();
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   private void countComplete() {
   	if( goodCarton || pickFixed) {
   		return;
   	}
   	countComplete.hide();
   	markOpenPickPickShort(scrollBoxList);
   	String msg = "Count complete with missing picks. Please place at hold area.";
   	showActionMsg(msg);
   	initCycleCount(true);
   	setCartonAtHoldArea(getIntParam(TermParam.cartonSeq));
   	ok.show();
   	nextScreen = "exception.IdleScreen";
   }
   
   @Override
   protected void doOkay() {
   	setNextScreen(nextScreen);
   }

   private void setScrollBox() {
      int x = 10;
      int w = 180;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "SKU", "sku");
      
      x += w;
      w = 475;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Description", "description");

      x += w;
      w = 175;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Uom", "displayUom");
      
      x += w;
      w = 125;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Need", "totalQty");
      
      x += w;
      w = 160;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Picked", "pickedQty");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Open", "openQty");      
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Short", "shortQty");
      
      x += w;
      w = 125;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Count", "scannedQty");   
      
      x += w;
      w = -1;
      Button btn = new Button(10,-5,30," +1 ",Align.LEFT,w,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            plusOne(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);
      
      x += 135;
      w = -1;
      btn = new Button(10,-5,30," -1 ",Align.LEFT,w,false);
      action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            minusOne(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);     
      
/*       x += 135;
      w = -1;
      btn = new Button(10,-5,30,"+10 ",Align.LEFT,w,false);
      action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            plusTen(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action); 
      
      x += 135;
      w = -1;
      btn = new Button(10,-5,30,"-10 ",Align.LEFT,w,false);
      action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
         	minusTen(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);  */     
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }
  
   private void plusOne(Map<String, String> p) {
      boolean canPlus = getMapInt(p,"scannedQty")<getMapInt(p,"totalQty");
      if( canPlus ) {
      	String sku = getMapStr(p, "sku");
      	int index = scrollBoxList.indexOf(p);
      	int skuScannedQty = getMapInt(p,"scannedQty");
      	int skuTotalQty = getMapInt(p,"totalQty");
      	skuScannedQty ++;
         scannedQty++;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("scannedQty", ""+skuScannedQty);
         if( skuScannedQty == skuTotalQty ) {
         	p.put("background", "green");
         } else
         	p.put("background", "yellow");
      	for( Map<String,String> m : scrollBoxList ) {
      		if( !getMapStr(m,"background").equals("green") )
      			m.put("background", "");
      	}
      	if( index>=scrollBoxList.size()) {
      		scrollBoxList.add(p);
      		scrollBoxListBackup.add(p);
      	}
      	else {
      		scrollBoxList.add(index, p);
      		scrollBoxListBackup.add(p);
      	}
      	showSuccessMsg("1 %s found(total %d), return sku to carton.", sku, skuScannedQty);
      	initCycleCount(true);
      	scrollBox.updateDisplayList(scrollBoxList);
      	checkPicks();
      }
   }  
   
   private void minusOne(Map<String, String> p) {
      boolean canMinus = getMapInt(p,"scannedQty")>0;
      if( canMinus ) {
      	int index = scrollBoxList.indexOf(p);
      	int skuScannedQty = getMapInt(p,"scannedQty");
      	skuScannedQty --;
         scannedQty--;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("scannedQty", ""+skuScannedQty);
         p.put("background", "yellow");
      	for( Map<String,String> m : scrollBoxList ) {
      		if( !getMapStr(m,"background").equals("green") )
      			m.put("background", "");
      	}
      	if( index>=scrollBoxList.size()) {
      		scrollBoxList.add(p);
      		scrollBoxListBackup.add(p);
      	}
      	else {
      		scrollBoxList.add(index, p);
      		scrollBoxListBackup.add(p);
      	}
      	scrollBox.updateDisplayList(scrollBoxList);
      	goodCarton = false;
      	pickFixed = false;
         if( scannedQty < pickedQty ) {
         	ok.hide();
         	updatePromptMsg("", promptMsg);
         }  else if( scannedQty < totalQty ) {
         	ok.hide();
         	showActionMsg("All picked SKUs confirmed, have %d missing pick(s)",totalQty-scannedQty);  
         	initCycleCount(false);
         }
      }
   } 
       
   private void setScrollBoxList() {
   	scrollBoxList = getAuditPickList();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);  
      for (Map<String, String> p : scrollBoxList) {
         int skuTotalQty = getMapInt(p,"totalQty");
         int skuPickedQty = getMapInt(p,"pickedQty");
         totalQty += skuTotalQty;
         pickedQty += skuPickedQty;
      }      
      scrollBox.updateDisplayList(scrollBoxList);
   }
   
   // logic
   
   private void checkPicks() {
   	goodCarton = true;
   	pickFixed = true;
   	for(Map<String,String> p : scrollBoxList ) {
      	int skuScannedQty = getMapInt(p,"scannedQty");
      	int skuTotalQty = getMapInt(p,"totalQty");
      	int skuShortQty = getMapInt(p,"shortQty");
      	int skuOpenQty = getMapInt(p,"shortQty");
      	int skuPickedQty = getMapInt(p,"pickedQty");
      	inform("sku %s, scanned %d total %d, short %d, open %d, picked %d",
      			getMapStr(p,"sku"),skuScannedQty,skuTotalQty,skuShortQty,skuOpenQty,skuPickedQty);
      	boolean skuGood = ((skuScannedQty==skuTotalQty) && (skuScannedQty==skuPickedQty));
      	goodCarton = goodCarton && skuGood;
      	boolean skuFixed = (!skuGood) && (skuScannedQty==skuTotalQty) ;
      	pickFixed = pickFixed && ( skuFixed || skuGood );
      	trace("sku %s good: %s fix %s",getMapStr(p,"sku"),skuGood?"Y":"N",skuFixed?"Y":"N");
   	}
   	if( goodCarton || pickFixed) {
   		countComplete.hide();
   		continueLabel.show();
      	String msg = "SKU quantity checked! Continue Label at station, or Cancel to reinduct carton.";
      	showSuccessMsg(msg);
      	initCycleCount(false);
      	promptMsg = "Audit Complete!";
      	updatePromptMsg("", promptMsg);
      	if( pickFixed )
   			markOpenPickPicked();
   		markCartonAudited();
   		nextScreen = getNextScreen(getParam(TermParam.cartonStatus));
   	}
   }

	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      Iterator<Map<String, String>> itr = scrollBoxList.iterator();
      //String barcode;
      Map<String,String> skuFromScan = getSkuFromScan(scan);
      if( skuFromScan == null || skuFromScan.isEmpty() ) {
      	showAlertMsg("No SKU found for barcode [%s].",scan);
      	initCycleCount(false);
      	return;
      };
      while (itr.hasNext()) {
         Map<String, String> p = itr.next();
         String sku = getMapStr(p,"sku");
         //String baseUom = getMapStr(p,"baseUom");
         if ( !sku.equals(getMapStr(skuFromScan,"sku")) )
            continue;
         processSku(p,itr);
         return;
      }
      showAlertMsg("No demand for sku in carton, restock.");
      initCycleCount(false);
   } 
   
   private void processSku(Map<String, String> p, Iterator<Map<String, String>> itr) {
      String sku = getMapStr(p, "sku");
      trace("scanned 1 %s",sku);
      int skuTotalQty = getMapInt(p, "totalQty");
      int skuScannedQty = getMapInt(p, "scannedQty"); 
      if (!scrollBoxList.contains(p))
         return;
      if( skuTotalQty == skuScannedQty ) {
      	showAlertMsg("No demand for sku %s in carton, restock.", sku);
      	initCycleCount(false);
      	return;
      }
      //int index = scrollBoxList.indexOf(p);
      itr.remove();
      scrollBoxListBackup.remove(p);
      skuScannedQty++;
      scannedQty++;
      p.put("scannedQty",""+skuScannedQty);
      if( skuTotalQty == skuScannedQty ) {
         p.put("background", "green");
      } else 
      	p.put("background", "yellow");
   	for( Map<String,String> m : scrollBoxList ) {
   		if( !getMapStr(m,"background").equals("green") )
   			m.put("background", "");
   	}
		scrollBoxList.add(0, p);
		scrollBoxListBackup.add(0,p);
      /*
   	if( index>=scrollBoxList.size()) {
   		scrollBoxList.add(p);
   		scrollBoxListBackup.add(p);
   	}
   	else {
   		scrollBoxList.add(index, p);
   		scrollBoxListBackup.add(p);
   	}*/
   	showSuccessMsg("1 %s found(total %d), return sku to carton.", sku, skuScannedQty);
   	initCycleCount(true);
      checkPicks();
      scrollBox.update();
      scrollBox.resetScroll();
   }

}
