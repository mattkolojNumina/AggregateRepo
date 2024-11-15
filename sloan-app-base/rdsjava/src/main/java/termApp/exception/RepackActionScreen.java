package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.*;
import termApp.util.TermActionObject.*;

import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

public class RepackActionScreen
      extends AbstractExceptionStationScreen {

   private Button ok, confirm, cancel, moveAll;
   private int totalQty, totalInNew;
   private boolean moveDone = false;

   public RepackActionScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
   }

   /*
    * interface methods
    */
   
   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
		initPromptModule(
				String.format("Move next SKU with Scanner or Button."),
				ENTRYCODE_NONE,
				true,
				true,2);
		initScrollBoxModule(true);
		setScrollBox();
		super.initDisplay();
	}   
   
   @Override
   public void setRightInfoBox() {
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "New LPN", getParam(TermParam.newCartonLpn));
      rightInfoBox.show(); 
   }
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Done",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.hide(); 
      
      confirm = new Button(x,y,f, "Confirm",Align.LEFT,-1,-1,COLOR_WHITE,false);
      confirm.registerOnActionListener(confirmAction());
      confirm.hide();
      
      x = W1_2;
      moveAll = new Button(x,y,f, "Move All",Align.CENTER,-1,-1,COLOR_WHITE,false);
      moveAll.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed move all");
            moveAll();
         }
      });
      moveAll.show();
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }
   
   @Override
   protected void doCancel() {
   	if( confirm.on() ) {
   		hideResultMsgModule();
   		confirm.hide();
   		ok.hide();
   		moveAll.show();
   	} else {
   		super.doCancel();
   	}
   }
   
   @Override
   protected void doConfirm() {
   	if( confirm.on() ) {
	      inform("confirm button pressed");
	   	moveDone = true;
	   	confirm.hide();
	   	scrollBox.hide();
	   	moveAll.hide();
	   	if( totalInNew == totalQty ) {
	   		moveAllPickstoNewCarton();
	   		showSuccessMsg("New carton %s will be labeled here. Old carton %s can be restocked.", getParam(TermParam.newCartonLpn), getParam(TermParam.cartonLpn));
	   		ok.show();
	   		setParam(TermParam.cartonLpn,getParam(TermParam.newCartonLpn));
	   		nextScreen = "exception.LabelIdleScreen";
	   		initStartTimer();
	   	} else if( totalInNew == 0 ) {
	   		showActionMsg("Nothing moved, this should not happen");
	   	} else {
	   		movePicksToNewCarton(scrollBoxList);
	   		showSuccessMsg("Re-scan new carton %s and old carton %s to get them labeled.", getParam(TermParam.newCartonLpn), getParam(TermParam.cartonLpn));
	   		ok.show();
	   		nextScreen = "exception.IdleScreen";
	   		initStartTimer();
	   	}
   	}
   }
   
   @Override
   protected void doOkay() {
   	if( ok.on() ) {
	      inform("done button pressed");
	      if( !moveDone ) {
		      showActionMsg("Qty in old %d, qty in new %d, Confirm or Cancel", totalQty-totalInNew, totalInNew);
		      initCycleCount(false);
		      ok.hide();
		      confirm.show();
	      } else {
	      	setNextScreen(nextScreen);
	      }
   	}
   }
   
   protected void moveAll() {
   	for(Map<String,String> p : scrollBoxList ) {
	   	int totalQty = getMapInt(p,"totalQty");
	      p.put("numInOld", "0");
	      p.put("numInNew", ""+totalQty);
   	}
   	scrollBox.updateDisplayList(scrollBoxList);
   	showSuccessMsg("Moved all picks to new carton.");
   	totalInNew = totalQty;
   	initCycleCount(false);
   	ok.show();   	
   }
   
   private void setScrollBox() {
      int x = 10;
      int w = 180;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "SKU", "sku");
      
      x += w;
      w = 500;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Description", "description");

      x += w;
      w = 175;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Uom", "displayUom");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "Total", "totalQty");
      
      x += w;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "in old", "numInOld");      
      
      x += w;
      w = -1;
      Button btn = new Button(10,-5,30," -> ",Align.LEFT,w,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            moveOne(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);
      
      x += 200;
      w = 150;
      scrollBoxModel.addColumn(x, w, Align.LEFT, "in new", "numInNew");   
      
      x += w;
      w = -1;
      btn = new Button(10,-5,30," <- ",Align.LEFT,w,false);
      action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            moveBackOne(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);      
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }
   
   private void moveOne(Map<String, String> p) {
      boolean canMove = getMapInt(p,"numInOld")>0;
      if( canMove ) {
      	String sku = getMapStr(p, "sku");
      	int index = scrollBoxList.indexOf(p);
      	int numInOld = getMapInt(p,"numInOld");
      	int numInNew = getMapInt(p,"numInNew");
      	numInOld--;
      	numInNew++;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("numInOld", ""+numInOld);
         p.put("numInNew", ""+numInNew);
      	p.put("background", "yellow");
      	for( Map<String,String> m : scrollBoxList ) {
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
      	showSuccessMsg("Moved 1 %s to new carton.", sku);
      	totalInNew++;
      	initCycleCount(true);
      	ok.show();
      }
   }
   
   private void moveBackOne(Map<String, String> p) {
      boolean canMoveBack = getMapInt(p,"numInNew")>0;
      if( canMoveBack ) {
      	String sku = getMapStr(p, "sku");
      	int index = scrollBoxList.indexOf(p);
      	int numInOld = getMapInt(p,"numInOld");
      	int numInNew = getMapInt(p,"numInNew");
      	numInOld++;
      	numInNew--;
      	scrollBoxList.remove(p);
      	scrollBoxListBackup.remove(p);
         p.put("numInOld", ""+numInOld);
         p.put("numInNew", ""+numInNew);
      	p.put("background", "yellow");
      	for( Map<String,String> m : scrollBoxList ) {
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
      	showSuccessMsg("Moved 1 %s back to old carton.", sku);
      	totalInNew--;
      	initCycleCount(true);
      	if( totalInNew> 0)
      		ok.show();
      	else
      		ok.hide();
      }
   }   
   
   private void setScrollBoxList() {
   	scrollBoxList = getRepackPickList();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList); 
      for (Map<String, String> p : scrollBoxList) {
         int skuTotalQty = getMapInt(p,"totalQty");
         totalQty += skuTotalQty;
      }
      totalInNew = 0;
      scrollBox.updateDisplayList(scrollBoxList);
      
   }   

   public void handleTick() {
      super.handleTick();
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
      	initCycleCount(true);
      	return;
      };
      while (itr.hasNext()) {
         Map<String, String> p = itr.next();
         String sku = getMapStr(p,"sku");
         String baseUom = getMapStr(p,"baseUom");
         if ( !sku.equals(getMapStr(skuFromScan,"sku")) || !baseUom.equals(getMapStr(skuFromScan,"uom")) )
            continue;
         processSku(p,itr);
         return;
      }
      showAlertMsg("No demand for sku in carton, restock.");
      initCycleCount(true);
   }
   
   private void processSku(Map<String, String> p, Iterator<Map<String, String>> itr) {
      String sku = getMapStr(p, "sku");
      trace("scanned 1 %s",sku);
   	int numInOld = getMapInt(p,"numInOld");
   	int numInNew = getMapInt(p,"numInNew");
      if (!scrollBoxList.contains(p))
         return;
      if( numInOld == 0 ) {
      	showAlertMsg("No demand for sku %s in carton, restock.", sku);
      	initCycleCount(true);
      	return;
      }
      itr.remove();
      scrollBoxListBackup.remove(p);
   	numInOld--;
   	numInNew++;
      p.put("numInOld", ""+numInOld);
      p.put("numInNew", ""+numInNew);
   	p.put("background", "yellow");
   	for( Map<String,String> m : scrollBoxList ) {
   		m.put("background", "");
   	}
		scrollBoxList.add(0, p);
		scrollBoxListBackup.add(0,p);
   	showSuccessMsg("Moved 1 %s to new carton.", sku);
   	totalInNew++;
   	initCycleCount(true);
      scrollBox.update();
      scrollBox.resetScroll();
      ok.show();
   }   
}

