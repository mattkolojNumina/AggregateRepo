package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;

import java.util.Map;

import java.util.ArrayList;

import termApp.util.Constants.Align;

import termApp.util.TermActionObject.*;


public class ShortDetailsScreen
      extends AbstractExceptionStationScreen {

   TextField lastScanDisplay;

   private Button cancel, audit;

   public ShortDetailsScreen( TerminalDriver term ) {
      super( term );
      setLogoutAllowed(false);
   }

   /*
    * interface methods
    */

   public void handleTick() {
      super.handleTick();
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(true);
   	initButtons();
		initPromptModule(
				"Short Details. Please place at hold area.",
				2,
				ENTRYCODE_NONE,
				true,
				false,
				true,
				1);
		initScrollBoxModule(true);
		setScrollBox();
		super.initDisplay();
	}  

   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();

      audit = new Button(x - 350,y,f, "Audit",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      audit.registerOnActionListener(auditAction());
      audit.show();
   }

   protected OnActionListener auditAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doAudit();
         }
      };
   }

   protected void doAudit() {
   	inform("audit button pressed");
      auditCarton();
   	setNextScreen("exception.AuditScreen");
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
        
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   } 
       
   private void setScrollBoxList() {
   	scrollBoxList = getShortPickList();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);    
      scrollBox.updateDisplayList(scrollBoxList);
   }

}
