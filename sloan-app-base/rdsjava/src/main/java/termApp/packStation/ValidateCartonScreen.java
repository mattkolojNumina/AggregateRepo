package termApp.packStation;

import static rds.RDSLog.*;

import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static termApp.util.Constants.*;

public class ValidateCartonScreen extends AbstractPackStationScreen {

   private Button ok,cancel,reprint;
   private int validateMode;
   private Map<String,String> validateInfo;

   public ValidateCartonScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(false);
      validateMode = getValidateMode();
      validateInfo = getValidateInfo();
      reset = 1000L;
   }

   @Override
	public void initDisplay() {
   	initInfoBox();
   	setInfoBox(false);
   	initButtons();
   	String promptMsg = validateMode==VALIDATE_MODE_LPN?"Scan carton LPN barcode":"Scan carton SKU barcode";
		initPromptModule(
				promptMsg,
				ENTRYCODE_REGULAR,
				true,
				true);
		super.initDisplay();
	}
   
   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, getMapStr(validateInfo,"validateType"),getMapStr(validateInfo,"validateValue"));
      rightInfoBox.show();
   }
   
   protected void initButtons() {
      int x = MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      ok = new Button(x,y,f, "Ok",Align.LEFT,-1,-1,COLOR_WHITE,false);
      ok.registerOnActionListener(okAction());  
      
      x = W1_2;
      reprint = new Button(x,y,f, "Reprint",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      reprint.registerOnActionListener(reprintAction());
      reprint.hide();
      
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
   
	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}
	
   @Override
   protected void doReprint() {
   	setNextScreen("packStation.VerifyLabelScreen");
   }
   
	public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      String validateValue = getMapStr(validateInfo,"validateValue");
   	if( !scan.equals(validateValue) )
   		showAlertMsg("Invalid carton LPN barcode");
   	else {
			showSuccessMsg("Carton validated, label verify required.");
			nextScreen = "packStation.ResultScreen";
   		String resultMsg = isParcel()?String.format("Parcel carton, bring to %s",getParcelCartonDestination()):
				String.format("LTL carton, bring to %s",getLtlCartonDestination());
   		setParam(TermParam.resultMsg, resultMsg);
			ok.show();
			initStartTimer();
   	}
   }

	/*
	public void inputDecision(String scan) {
      setParam(TermParam.scan,scan);
      String validateValue = getMapStr(validateInfo,"validateValue");
      String validateUom = getMapStr(validateInfo,"validateUom");
      if( validateMode == VALIDATE_MODE_LPN ) {
      	if( !scan.equals(validateValue) )
      		showAlertMsg("Invalid carton LPN barcode");
      	else {
   			showSuccessMsg("Carton validated, label verify required.");
   			nextScreen = "packStation.ResultScreen";
   			ok.show();
   			initStartTimer();
      	}
      } else if( validateMode == VALIDATE_MODE_PARCEL_SKU ) {
      	if( !isValidSkuBarcode(scan,validateValue,validateUom) )
      		showAlertMsg("Invalid carton SKU barcode");
      	else {
      		if( requirePacklist() ) {
      			showSuccessMsg("Carton validated, require packlist");
      			nextScreen = "packStation.CheckPacklistScreen";
      			ok.show();
      			initStartTimer();  
      			setParam(TermParam.validateMode,"true");
      		} else if( numberOfLabels() == 1 ) {
      			String destination = getParcelDestination();
      			showSuccessMsg("Carton validated, bring to %s", destination);
      			completeParcelCarton();
      			reprint.show();
      			nextScreen = "packStation.StartScreen";
      			ok.show();
      		} else {
      			showSuccessMsg("Carton validated, require label verify");
      			nextScreen = "packStation.VerifyLabelScreen";
      			ok.show();
      			initStartTimer();      			
      		}
      	}      	
      } else {
      	if( !isValidSkuBarcode(scan,validateValue,validateUom) ) {
      		showAlertMsg("Invalid carton SKU barcode");
      	} else {
            switch(checkSku(scan)) {
            case CASE_SORT_LBL: 
            case CASE_INDUCT: correctLane(); break;
            case CASE_NON_IND: ;
            case CASE_PLLTIZED: removeLabel(); break;
            default: caseError();
            }
      	}
      }
   }
	
   protected void correctLane() {
		if( numberOfLabels() == 1 ) {
			String destination = getLtlDestination();
			showSuccessMsg("Carton validated, bring to %s", destination);
			markLtlCartonLabeled();
			String stationMode = getParam("mode");
			if( !stationMode.equals(MODE_SORTEXCEPTION) )
				markLtlCartonSorted();
			nextScreen = "packStation.StartScreen";
			ok.show();
			initStartTimer();
		} else {
			showSuccessMsg("Carton validated, require label verify");
			nextScreen = "packStation.VerifyLabelScreen";
			ok.show();
			initStartTimer();      			
		}
   }
   
   protected void removeLabel() {
   	showAlertMsg("Remove all labels. Need to reprocess this SKU");
   	nextScreen = "packStation.StartScreen";
		ok.show();
		initStartTimer();      			
   }
   
   protected void caseError() {
   	showAlertMsg("Something is wrong. Please cancel and retry later.");
   	ok.show();
   	initStartTimer();
   }*/

   public void handleTick() {
      super.handleTick();
   }


   // helpers

   
}