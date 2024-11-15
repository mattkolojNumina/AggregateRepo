package termApp.overPackCartonLabelStation;

import static rds.RDSLog.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;
import static termApp.util.Constants.*;

import java.util.*;

public class LabelPrintScreen extends AbstractOverPackCartonLabelStationScreen {

   private Button ok,cancel;
   private List<printObj> printSeqList;
   private boolean checkPrintJob;

   public LabelPrintScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      processMode = getIntParam(TermParam.processMode);
      printSeqList = new ArrayList<>();
      checkPrintJob = false;
      reset = 2000L;
   }
   
   class printObj{
   	private int docSeq,printJobSeq;
   	public printObj( int docSeq, int printJobSeq) {
   		this.docSeq = docSeq;
   		this.printJobSeq = printJobSeq;
   	}
   }

   // init

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
		initPromptModule(
				"Scan label barcode",
				ENTRYCODE_NONE,
				true,
				true);
		initScrollBoxModule(true);
		setScrollBox();
		initPrintJob();
		super.initDisplay();
	}
   
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Printing label(s)", "" );
      rightInfoBox.show();
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
   	inform("okay button pressed");
   	setNextScreen(nextScreen);
   }
   
   private void setScrollBox() {
      int x = 100;
      int w = 400;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Label Type", "docTypeDisplay");

      x += w;
      w = 500;
      scrollBoxModel.addColumn(  x, w, Align.LEFT, "Verify", "verification");
      
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
      
      x += w;
      btn = new Button(10,-5,30,"confirm",Align.LEFT,-1,false);
      action = new ButtonAction() {
         @Override
         public void onAction(Button btn, Map<String, String> map) {
            confirmLabel(map);
         }
      };
      scrollBoxModel.addButtonColumn(x, "", btn, action);      
      
      scrollBox = scrollBoxModel.build();
      scrollBox.show();
      setScrollBoxList();   	
   }
   
   private void reprintLabel(Map<String, String> p) {
   	int docSeq = getMapInt(p,"docSeq");
   	doPrint(docSeq);
   } 
   
   private void doPrint(int docSeq) {
   	for( printObj o : printSeqList ) {
   		if( o.docSeq == docSeq ) {
   			updateTextFieldBySeq(docSeq,"Label is printing.",COLOR_YELLOW);
   			return;
   		}
   	}
      processing = 0;
      int printJobSeq = printRdsDocument(docSeq,getStrParam("printer"));
      if( printJobSeq > 0 ) {
      	printObj o = new printObj(docSeq,printJobSeq);
         printSeqList.add(o);
         checkPrintJob = true;
         updateTextFieldBySeq(docSeq,"printing label","");
      } else {
         if(printJobSeq == PRINT_ERROR_DATABASE) {
         	updateTextFieldBySeq(docSeq,"Error connecting to database",COLOR_RED);
         } else if(printJobSeq == PRINT_ERROR_NOLABEL) {
         	updateTextFieldBySeq(docSeq,"No labels found",COLOR_RED);
         } else {
         	updateTextFieldBySeq(docSeq,"Something went wrong",COLOR_RED);
         }  
      }
   }   

   private void confirmLabel(Map<String, String> p) {
      String verification = getMapStr(p,"verification");
      if( !verification.isEmpty()) {
      	showAlertMsg("Scan label to verify");
      	return;
      }
      String docType = getMapStr(p, "docTypeDisplay");
      int docSeq = getMapInt(p, "docSeq");
      trace("%s(%d) label verified",docType, docSeq);
      scrollBoxList.remove(p);
      scrollBoxListBackup.remove(p);
      p.put("background", "green");
      p.put("status", "verified");
      scrollBoxListBackup.add(p);
      scrollBoxList.add(p);
      showSuccessMsg("%s verified",docType);
      checkLabelVerified();
      scrollBox.update();
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
      } else
      	scrollBoxListBackup = new ArrayList<Map<String,String>>(scrollBoxList);    
      scrollBox.updateDisplayList(scrollBoxList);
   }
   // logic
   
   protected void initPrintJob() {
   	List<Map<String,String>> labels = null;
   	/*
   	if( processMode == MODE_PALLET_LABEL )
   		labels = getPalletLabels();
   	else
   		labels = getCartonLabels();
   	*/
   	labels = getCartonLabels();
   	for( Map<String,String> m : labels ) {
   		int docSeq = getMapInt(m,"docSeq");
   		doPrint(docSeq);
   	}
   }

	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      Iterator<Map<String, String>> itr = scrollBoxList.iterator();
      String verification;
      while (itr.hasNext()) {
         Map<String, String> p = itr.next();
         verification = getMapStr(p,"verification");
         if ( !scan.equals(verification) )
            continue;
         verifyLabel(p,itr);
         return;
      }
      showAlertMsg("No label found with scan [%s]",scan);
   }
   
   private void verifyLabel(Map<String, String> p, Iterator<Map<String, String>> itr) {
      String docType = getMapStr(p, "docTypeDisplay");
      int docSeq = getMapInt(p, "docSeq");
      trace("%s(%d) label verified",docType, docSeq);
      if (!scrollBoxList.contains(p))
         return;
      itr.remove();
      scrollBoxListBackup.remove(p);
      p.put("status", "verified");
      p.put("background", "green");
      showSuccessMsg("%s verified",docType);
      scrollBoxList.add(0,p);
      scrollBoxListBackup.add(0,p);
      checkLabelVerified();
      scrollBox.updateDisplayList(scrollBoxList);
   } 
   
   private void checkLabelVerified() {
   	boolean allVerified = true;
   	for( Map<String,String> m : scrollBoxList ) {
   		if( !getMapStr(m,"status").equals("verified") ) {
   			allVerified = false;
   			break;
   		}
   	}
   	if( allVerified ) {
   		markCartonLabeled();
   		nextScreen = "overPackCartonLabelStation.ResultScreen";
   		String resultMsg = isParcel()?String.format("Parcel carton, bring to %s",getParcelCartonDestination()):
				String.format("LTL carton, bring to %s",getLtlCartonDestination());
   		setParam(TermParam.resultMsg, resultMsg);
   		ok.show();
   		initStartTimer();
   	}
   }

   public void handleTick() {
      super.handleTick();
      checkPrintJob();
   }
   
   private void checkPrintJob() {
      if(checkPrintJob) {
      	printObj o = printSeqList.get(0);
      	int docSeq = o.docSeq;
      	int printJobSeq = o.printJobSeq;
      	int complete = getPrintJobStatus( printJobSeq );
         if( complete == 1 ) {
            updateTextFieldBySeq(docSeq,"Printing complete",COLOR_YELLOW);
            printSeqList.remove(0);
         } else {
            if(processing == 0)
               initProcessTimer();
         }
         if( printSeqList.size() == 0 ) {
         	checkPrintJob = false;
         	processing = 0;
         }
      }
      if( checkPrintJob && processing > 0 && System.currentTimeMillis() - processing > timeout ) {
         showAlertMsg("Timeout reprinting labels. Check Zebra printer.");
         checkPrintJob = false;
         processing = 0;
      }
   }
   
   private void updateTextFieldBySeq( int docSeq, String status, String color) {
      Iterator<Map<String, String>> itr = scrollBoxList.iterator();
      int currentDocSeq;
      while (itr.hasNext()) {
         Map<String, String> p = itr.next();
         currentDocSeq = getMapInt(p,"docSeq");
         if ( currentDocSeq!=docSeq )
            continue;
         itr.remove();
         scrollBoxListBackup.remove(p);
         p.put("status", status);
         if( !color.isEmpty() )
         	p.put("background", color);
         else
         	p.remove("background");
         scrollBoxList.add(0,p);
         scrollBoxListBackup.add(0,p);
         scrollBox.update();
         return;
      }   	
   }
}