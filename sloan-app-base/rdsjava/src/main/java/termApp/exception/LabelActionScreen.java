package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;
import termApp.util.TermActionObject.*;

import java.util.Map;
import java.util.ArrayList;

public class LabelActionScreen
      extends AbstractExceptionStationScreen {

   private Button cancel,done,repack,audit;

   TextField lastScanDisplay;

   public LabelActionScreen( TerminalDriver term ) {
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
				"Print any documents the carton is Missing, then scan the tracking barcode.",
				2,
				ENTRYCODE_NONE,
				true,
				true,false);
		initScrollBoxModule(false);
		setScrollBox();
		super.initDisplay();
	}
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      done = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      done.registerOnActionListener(okAction());
      
      x = W1_2;
      repack = new Button(x,y,f, "Repack",Align.CENTER,-1,-1,COLOR_WHITE,false);
      repack.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            inform("pressed repak");
            setNextScreen("exception.RepackIdleScreen");
         }
      });
      repack.display(showRepack());
      
      x = SCREEN_WIDTH - MARGIN;
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();

      x = SCREEN_WIDTH - MARGIN;
      audit = new Button(x,y - 100,f, "Audit",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
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
   
   @Override
   protected void doOkay() {
   	inform("okay button pressed");
   	markCartonLabeled();
   	setNextScreen("exception.IdleScreen");
   }
   
   private void setScrollBox() {
      int x = 100;
      int w = 400;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Doc Type", "docType");
      
      x += w;
      w = 500;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Status", "status");

      x += w;
      w = 200;
      Button btn = new Button(10,-5,30,"reprint",Align.LEFT,-1,false);
      ButtonAction action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            reprintLabel(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);     
      
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }   
   
   private void setScrollBoxList() {
   	scrollBoxList = getCartonLabels();
      if (scrollBoxList == null) {
      	scrollBoxList = new ArrayList<Map<String,String>>();
      	scrollBoxListBackup = new ArrayList<Map<String,String>>();
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);    
      scrollBox.updateDisplayList(scrollBoxList);
   }   
   
   private void reprintLabel(Map<String, String> p) {
   	int docSeq = getMapInt(p,"docSeq");
   	String printer = getMapStr(p,"printer");
   	printLabel(docSeq,printer);
   	markCartonPacked();
   	repack.hide();
   }

   public void handleTick() {
      super.handleTick();
   }

   //Methods created
   protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
   }
   
   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      String verification = getParam(TermParam.trackingNumber);
      if ( !scan.equals(verification) ) {
      	showAlertMsg("Invalid tracking number!");
      } else {
      	showSuccessMsg("Label Verified, re-induct carton.");
      	done.show();
      	initStartTimer();
      }
   }
   
}
