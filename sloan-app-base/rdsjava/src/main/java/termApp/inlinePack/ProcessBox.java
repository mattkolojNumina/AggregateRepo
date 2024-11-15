package termApp.inlinePack;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

public class ProcessBox
extends AbstractInLinePackStationScreen {

   private String lpn;
   private String nextScreen;
   protected long printPLStart = 0L;
   protected long printOLStart = 0L;
   private Button ok;
   private Button reprintPackListButton;
   private Button reprintOrderLabelButton;
   private Button markRepack;
   private Button markException;
   protected int plJobSeq, olJobSeq;
   protected boolean allowReprintPackList, allowReprintOrderLabel, checkPLJob, checkOLJob;

   public ProcessBox(TerminalDriver term) {
      super(term);
      lpn = getParam(TermParam.cartonLpn);
      allowReprintPackList = allowReprintOrderLabel = checkPLJob = checkOLJob = false;
      plJobSeq = olJobSeq = -1;
      nextScreen = "inlinePack.IdleScreen";
   }

   /*
    * interface methods
    */

   /** Initializes the screen and lays out UI elements. */
   public void initDisplay() {
		initButtons();
		initPromptModule(
				String.format("Processing carton %s", lpn),
				ENTRYCODE_NONE,
				true,
				true);
		super.initDisplay();     
		processBox();
      if(allowReprintPackList)
      	reprintPackListButton.show();
      if(allowReprintOrderLabel)
      	reprintOrderLabelButton.show();
   }
   
   private void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      int width = 650;
      ok = new Button(x,y,f, "OK",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());
      ok.show();
      
      x = W1_4-100;
      reprintPackListButton = new Button(x,y,f, "Reprint Packlist",Align.LEFT,width,-1,COLOR_WHITE,false);
      reprintPackListButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            doPlPrint();            
         }
      });
      reprintPackListButton.hide();

      x = W1_4*2+150;
      reprintOrderLabelButton = new Button(x,y,f, "Reprint Label",Align.LEFT,width,-1,COLOR_WHITE,false);
      reprintOrderLabelButton.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            doOlPrint();            
         }
      });
      reprintOrderLabelButton.hide();

      y = 200;
      x = SCREEN_WIDTH-750;
      width = 600;
      markRepack = new Button(x, y, f, "Mark Repack",Align.LEFT,width,false);
      markRepack.registerOnActionListener( new OnActionListener() {
         @Override
         public void onAction() {
         	markRepack();
         }
      });
      markRepack.show();

      y += 150;
      markException = new Button(x, y, f, "Mark Exception",Align.LEFT,width,false);
      markException.registerOnActionListener( new OnActionListener() {
         @Override
         public void onAction() {
         	markException();
         }
      });
      markException.hide();
   }   
   
   @Override
   protected void doOkay() {
      trace("OK button pressed/timer expired");
      clearRuntime();
      setNextScreen(nextScreen);
   }
   
   protected void doPlPrint() {
   	if(!reprintPackListButton.on())
   		return;
      start = 0L;
   	trace("Reprinting packlist");
      startPLPrintJob();
   } 

   protected void doOlPrint() {
   	if(!reprintOrderLabelButton.on())
   		return;
      start = 0L;
   	trace("Reprinting order label");
      startOLPrintJob();
   } 
   
   /*
    * processing methods
    */   
   
   @Override
   protected void processScan(String scan) {
      trace("processing scan [%s]", scan);

      if(scan == null || scan.isEmpty()) {
         return;
      }

      setParam(TermParam.scan, scan);
      setParam(TermParam.cartonLpn, scan);
      setNextScreen("inlinePack.ProcessBox"); //TODO: ignore scans on this screen?
   }
   
   private void startPLPrintJob() {
   	printPLStart = System.currentTimeMillis();
      reprintPackListButton.hide();
      plJobSeq = printPacklist();
      if( plJobSeq > 0 ) {
      	checkPLJob = true;
      } else {
         if(plJobSeq == PRINT_ERROR_DATABASE) {
         	showAlertMsg("Error connecting to database");
         } else if(plJobSeq == PRINT_ERROR_NOLABEL) {
         	showAlertMsg("No document found");         	
         } else {
         	showAlertMsg("Something went wrong");   
         }  
      }
   }

   private void startOLPrintJob() {
   	printOLStart = System.currentTimeMillis();
      reprintOrderLabelButton.hide();
      olJobSeq = printOrderLabel();
      if( olJobSeq > 0 ) {
      	checkOLJob = true;
      } else {
         if(olJobSeq == PRINT_ERROR_DATABASE) {
         	showAlertMsg("Error connecting to database");
         } else if(olJobSeq == PRINT_ERROR_NOLABEL) {
         	showAlertMsg("No document found");         	
         } else {
         	showAlertMsg("Something went wrong");   
         }  
      }
   }

   /*
    * logic methods
    */
   
   private void processBox() {
      if (lpn == null || lpn.isEmpty() || lpn.equals(BARCODE_NOREAD)) {
         lpn = null;
         showActionMsg("Scanner noread, scan carton to process");
         ok.show();
         markRepack.hide();
         markException.hide();
         initStartTimer();
         return;
      }

      int cartonSeq = lookupCartonByLpn(lpn);
      if (cartonSeq > 0) {
         setParam(TermParam.cartonSeq, ""+cartonSeq);
         processCarton(cartonSeq);
         return;
      }

      showAlertMsg("Carton not found with lpn [%s]", lpn );
   	ok.show();
      markRepack.hide();
      markException.hide();
   	initStartTimer();
   }

   private void processCarton(int cartonSeq) {
      setCarton(cartonSeq);
      String cartonStatus = getStrParam(TermParam.cartonStatus);
      String cartonID = getStrParam(TermParam.cartonLpn);
   	switch(cartonStatus){
   	case CARTON_STATUS_CANCELED: 
   		showAlertMsg("Carton %s is canceled", cartonID); 
   		ok.show();
   		initStartTimer();
   		break;
      case CARTON_STATUS_AUDIT_REQUIRED: 
   		showAlertMsg("Carton %s requires audit, process at Audit station", cartonID); 
   		ok.show();
   		initStartTimer();
   		break;
      case CARTON_STATUS_REPACK_REQUIRED: 
   		showAlertMsg("Carton requires repack, process at Audit station", cartonID); 
   		ok.show();
   		initStartTimer();
   		break;
      case CARTON_STATUS_PACK_EXCEPTION: 
   		showAlertMsg("Carton has an exception, process at Audit station", cartonID); 
   		ok.show();
   		initStartTimer();
   		break;
   	case CARTON_STATUS_SHORT:
   		showAlertMsg("Carton is short, process at Audit station", cartonID); 
   		ok.show();
   		initStartTimer();
   		break;
   	case CARTON_STATUS_PICKING:
   		showAlertMsg("Carton has open picks, finish picks first", cartonID); 
   		break;
      case CARTON_STATUS_PICKED:
         int docs = getDocuments();
         if(docs > 0) {
            setNextScreen("inlinePack.PacklistScreen");
            break;
         }
         else if (docs == -1) {
            showAlertMsg("Carton order label not found", cartonID); 
         }
         else if(docs == -2) {
            showAlertMsg("Carton packlist not found", cartonID); 
         }
         ok.show();
         initStartTimer();
         break;
      case CARTON_STATUS_PACKED:
   		showSuccessMsg("Carton %s is packed", cartonID); 
   		if(getPacklist()) {
   			allowReprintPackList = true;
            allowReprintOrderLabel = true;
   		}
         if(getOrderLabel()) {
            allowReprintPackList = true;
            allowReprintOrderLabel = true;
         }
         initStartTimer();
   		break;
		default:
			showAlertMsg("Carton %s unknown error, check with supervisor", cartonID);	
   	}
   }
   
   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      String errorMsg = "";
      boolean error = true;
      boolean action = true;
      boolean showErrorMsg = false;
      //Checks if a packlist reprint is finished
   	if(checkPLJob) {
   		showErrorMsg = true;
   		int complete = getPrintJobStatus( plJobSeq );
   		if(complete == 1) {
   			checkPLJob = false;
   			errorMsg = "Packlist printed";
   			error = false;
            action = false;
   			ok.show();
   			reprintPackListButton.show();
   			printPLStart = 0;
   			initStartTimer();
   		} else if( System.currentTimeMillis() - printPLStart > timeout ) {
   			checkPLJob = false;
   			errorMsg = "Timeout printing packlist. Check document printer";
            error = true;
            action = false;
   			ok.show();
   			reprintPackListButton.show();
   			printPLStart = 0;
            initStartTimer();
   		} else {
   			errorMsg = "Reprinting packlist";
   			error = false;
            action = true;
   		}
   	}
      //Checks if an order label (1x4) is finished
      else if(checkOLJob) {
   		showErrorMsg = true;
   		int complete = getPrintJobStatus( olJobSeq );
   		if(complete == 1) {
   			checkOLJob = false;
   			errorMsg = "Label printed";
   			error = false;
            action = false;
   			ok.show();
   			reprintOrderLabelButton.show();
   			printOLStart = 0;
   			initStartTimer();
   		} else if( System.currentTimeMillis() - printOLStart > timeout ) {
   			checkOLJob = false;
   			errorMsg = "Timeout printing label. Check document printer";
            error = true;
            action = false;
   			ok.show();
   			reprintOrderLabelButton.show();
   			printOLStart = 0;
            initStartTimer();
   		} else {
   			errorMsg = "Reprinting Label";
   			error = false;
            action = true;
   		}
   	} 
      else {
   		error = false;
      }
   	if(showErrorMsg) {
   		String msg = errorMsg;
   		if(error)
   			showAlertMsg(msg);
         else if(action) 
            showActionMsg(msg);
   		else if(!checkPLJob)
   			showSuccessMsg(msg);
         else if(!checkOLJob)
   			showSuccessMsg(msg);
   		else
   			showActionMsg(msg);
   	}
   }
   
}
