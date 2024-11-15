package termApp.overPackCartonPackStation;

import static rds.RDSLog.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;
import termApp.util.TermActionObject.OnActionListener;

import static termApp.util.Constants.*;

public class AuditScreen extends AbstractOverPackCartonPackStationScreen {

   private Button ok,cancel,countComplete,printShort,shipShort;
   private boolean goodCarton, pickFixed;
   private int totalQty, scannedQty, pickedQty;
   private boolean requirePasswordScan;
   private String promptMsg;

   public AuditScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      goodCarton = pickFixed = false;
      totalQty = scannedQty = pickedQty = 0;
      requirePasswordScan = false;
      promptMsg = "";
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
   	promptMsg = "Scan next SKU or confirm qty";
		initPromptModule(
				promptMsg,
				ENTRYCODE_PASSWORD,
				true,
				true);
		promptPWEntry.hide();
		initScrollBoxModule(true);
		setScrollBox();
		super.initDisplay();
	}  
   
   /*
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Has Short:", getParam(TermParam.cartonStatus).equals(CARTON_STATUS_SHORT)?"YES":"NO" );
      rightInfoBox.show(); 
   }*/

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.hide();
      
      x = W1_4;
      printShort = new Button(x,y,f, "Pick Ticket",Align.CENTER,-1,-1,COLOR_WHITE,false);
      printShort.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed print short");
            printShort();
         }
      });
      printShort.hide();
      
      x = W1_2;
      shipShort = new Button(x,y,f, "Ship Short",Align.LEFT,-1,-1,COLOR_WHITE,false);
      shipShort.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed ship short");
            shipShort();
         }
      });
      shipShort.hide();      
      
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
   	countComplete.hide();
   	String msg = "Count complete with missing picks. ";
   	showActionMsg(msg);
   	shipShort.show();
   	printShort.show();
   }
   
   private void shipShort() {
   	countComplete.hide();
   	String msg = "Admin required to ship short.";
   	showActionMsg(msg);
   	promptMsg = "Enter Admin Password";
   	updatePromptMsg("", promptMsg);
   	promptPWEntry.show();
   	requirePasswordScan = true;
   }
   
   private void printShort() {
   	createPickTicket(scrollBoxList);
   	setNextScreen("overPackCartonPackStation.PrintPickTicketScreen");
   }
   
   @Override
   protected void doOkay() {
   	start = 0;
   	if( goodCarton || pickFixed ) {
   		if( pickFixed )
   			markOpenPickPicked();
   		markCartonAudited();
   		determineNextScreen();   			
   	} 
   }

   private void setScrollBox() {
      int x = 10;
      int w = 180;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "SKU", "sku");

      x += w;
      w = 160;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Uom", "uom");
      
      x += w;
      w = 150;
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
      w = 180;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Scanned", "scannedQty");   
      
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
      
      x += 135;
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
      scrollBoxModel.addButtonColumn(x, "", btn, action);      
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }
  
   private void plusOne(Map<String, String> p) {
      boolean canPlus = getMapInt(p,"scannedQty")<getMapInt(p,"totalQty");
      if( canPlus ) {
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
      	checkPicks();
      }
   }
   
   private void plusTen(Map<String, String> p) {
      boolean canPlus = getMapInt(p,"scannedQty")<getMapInt(p,"totalQty");
      if( canPlus ) {
      	int index = scrollBoxList.indexOf(p);
      	int skuScannedQty = getMapInt(p,"scannedQty");
      	int skuTotalQty = getMapInt(p,"totalQty");
      	int actPlusQty = 10;
      	if( (skuScannedQty+10) > skuTotalQty )
      		actPlusQty = skuTotalQty - skuScannedQty;
      	skuScannedQty += actPlusQty;
      	scannedQty += actPlusQty;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("scannedQty", ""+skuScannedQty);
         if( skuScannedQty == skuTotalQty ) {
         	p.put("background", "green");
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
         p.put("background", "");
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
         }
      }
   } 
   
   private void minusTen(Map<String, String> p) {
      boolean canMinus = getMapInt(p,"scannedQty")>0;
      if( canMinus ) {
      	int index = scrollBoxList.indexOf(p);
      	int skuScannedQty = getMapInt(p,"scannedQty");
      	int actMinusQty = 10;
      	if( skuScannedQty < 10 )
      		actMinusQty = skuScannedQty;
      	skuScannedQty -= actMinusQty;
         scannedQty -= actMinusQty;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("scannedQty", ""+skuScannedQty);
         p.put("background", "");
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
   		ok.show();
   		countComplete.hide();	
      	String msg = "SKU quantity checked!";
      	showSuccessMsg(msg);
      	promptMsg = "Item scan complete.";
      	updatePromptMsg("", promptMsg);
      	initStartTimer();
   	}
   }

	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      if( !requirePasswordScan ) {
	      Iterator<Map<String, String>> itr = scrollBoxList.iterator();
	      String barcode;
	      while (itr.hasNext()) {
	         Map<String, String> p = itr.next();
	         barcode = getMapStr(p,"barcode");
	         if ( !scan.equals(barcode) )
	            continue;
	         processSku(p,itr);
	         return;
	      }
	      showAlertMsg("No SKU found with scan [%s]",scan);
      } else {
      	if( isValidPassword(scan) ) {
      		markOpenPickShipShort(scrollBoxList);
      		markCartonAudited();  
      		determineNextScreen();
      	} else {
      		showAlertMsg("Not a valid supervisor");
      	}
      }
   } 
   
   private void processSku(Map<String, String> p, Iterator<Map<String, String>> itr) {
      String sku = getMapStr(p, "sku");
      trace("scanned 1 %s",sku);
      int skuTotalQty = getMapInt(p, "totalQty");
      int skuScannedQty = getMapInt(p, "scannedQty"); 
      if (!scrollBoxList.contains(p))
         return;
      if( skuTotalQty == skuScannedQty ) {
      	showAlertMsg("%s has all picks scanned", sku);
      	return;
      }
      int index = scrollBoxList.indexOf(p);
      itr.remove();
      scrollBoxListBackup.remove(p);
      skuScannedQty++;
      p.put("scannedQty",""+skuScannedQty);
      if( skuTotalQty == skuScannedQty ) {
         p.put("background", "green");
      } 
   	if( index>=scrollBoxList.size()) {
   		scrollBoxList.add(p);
   		scrollBoxListBackup.add(p);
   	}
   	else {
   		scrollBoxList.add(index, p);
   		scrollBoxListBackup.add(p);
   	}
      checkPicks();
      scrollBox.update();
   }
   
   private void determineNextScreen() {
		if( requirePacklist() ) {
			setNextScreen("overPackCartonPackStation.PackListScreen");
		} else {
			markCartonPacked();
			setNextScreen("overPackCartonPackStation.ResultScreen");
		}
   }
   
   public void handleTick() {
      super.handleTick();
   }

   // helpers

   
}
