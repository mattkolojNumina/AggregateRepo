package termApp.exception;

import static termApp.util.Constants.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import java.util.Map;
import java.util.ArrayList;


public class LabelIdleScreen
      extends AbstractExceptionStationScreen {

   private int tick;
   private Button cancel;

   public LabelIdleScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
      processing = System.currentTimeMillis();
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
				"Please wait for any documents the carton is missing.",
				2,
				ENTRYCODE_NONE,
				true,
				true,
				true);
		initScrollBoxModule(false);
		setScrollBox();
		super.initDisplay();
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
      int x = 100;
      int w = 600;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Doc Type", "docType");
      x += w;
      w = 600;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Status", "status");
      scrollBox = scrollBoxModel.build();
      scrollBox.show();  	
   }
   
   private void setScrollBoxList() {
   	scrollBoxList = getCartonLabels();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else {
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList); 
      	boolean hasAllLabel = true;
      	for( Map<String,String> m : scrollBoxList ) {
      		String status = getMapStr(m,"status");
      		hasAllLabel &= status.equalsIgnoreCase("Ready");
      	}
         if( hasAllLabel) {
         	setNextScreen("exception.LabelActionScreen");
         }
   	}
      scrollBox.updateDisplayList(scrollBoxList);
   }   
   
   public void handleTick() {
      super.handleTick();
      if( scrollBox != null && tick % 10 == 0 )
      	setScrollBoxList();
      tick++;
      if(tick > 1000000) {
         tick = 0;
      }
      if( processing > 0 && System.currentTimeMillis() - processing > timeout ) {
         showAlertMsg("Timeout getting required label");
         processing = 0;
      }
   }   

}
