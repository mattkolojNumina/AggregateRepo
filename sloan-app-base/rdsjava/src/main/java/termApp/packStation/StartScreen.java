package termApp.packStation;

import static rds.RDSLog.*;
import term.TerminalDriver;

import static app.Constants.*;

public class StartScreen extends AbstractPackStationScreen {

   public StartScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(true);
      clearAllParam();
   }

   @Override
	public void initDisplay() {
   	String stationMode = getParam("mode");
		initPromptModule(
				stationMode.equals(MODE_OVERPACK)?"Scan carton LPN":"Scan carton/tote/pallet LPN",
				ENTRYCODE_REGULAR,
				false,
				true,2);
		super.initDisplay();
	}

   // logic

   @Override
	protected void processScan(String scan) {
		trace( "processing scan [%s]", scan);
      inputDecision( scan );
	}

   //10/03/2022 modified by ZZ
   public void inputDecision(String scan) {
   	String stationMode = getParam("mode");
      if(!scan.isEmpty()) {
         setParam(TermParam.scan, scan);
         int cartonSeq = -1;
         switch(stationMode) {
         case MODE_OVERPACK:
            cartonSeq = lookupCartonByLpn(scan);
            if (cartonSeq>0) {
               processCarton(cartonSeq);
               return;
            }
            break;
         case MODE_LABELING:
            cartonSeq = lookupCartonByLabelBarcode(scan);
            if (cartonSeq>0) {
            	processLabeledCarton(cartonSeq);
            	return;
            }
            cartonSeq = lookupCartonByLpn(scan);
            if (cartonSeq>0) {
               processCarton(cartonSeq);
               return;
            }
            int palletSeq = lookupPalletByLpn(scan);
            if (palletSeq>0) {
            	processPallet(palletSeq);
            	return;
            }
            break;
         }
         showAlertMsg("Invalid scan[%s]",scan);
      } else {
      	showAlertMsg("Invalid scan: empty");
      }
   }
   
   private void processCarton( int cartonSeq ) {
   	setCarton(cartonSeq);
   	String cartonStatus = getStrParam(TermParam.cartonStatus);
   	String cartonID = getStrParam(TermParam.cartonLpn);	
   	if(cartonID.isEmpty())
   		cartonID = getStrParam(TermParam.cartonUcc);
   	if( cartonID.length() > 9 )
   		cartonID = cartonID.substring(cartonID.length()-9);
   	inform("carton status %s",cartonStatus);
   	switch(cartonStatus){
   	case CARTON_STATUS_CANCELED: 
   		showAlertMsg("[%s] is canceled",cartonID); 
   		break;
   	case CARTON_STATUS_LABELED: 
   		inform("carton labeled");
   		setNextScreen("packStation.VerifyLabelScreen"); 
   		break;
   	case CARTON_STATUS_PACKED: 
   		inform("carton packed");
   		if( isParcel() ) {
      		int uploadStatus = shipLabelRequestStatus();
      		if( uploadStatus<0 ) {
      			setNextScreen("packStation.CartonWeightScreen");
      			break;
      		} else if( uploadStatus == 0 ) {
      			setNextScreen("packStation.WaitShipLabelRequestResultScreen");
      			break;
      		} else {
      			setNextScreen("packStation.WaitLabelScreen");
      			break;   			
      		}   			
   		} else {
   			if( isToteOrSplitCase() ) {
   				triggerShipLabelRequest();
   				setNextScreen("packStation.WaitLabelScreen");
   			} else {
   				setParam(TermParam.resultMsg, String.format("LTL carton, bring to %s",getLtlCartonDestination()));
   				setNextScreen("packStation.ResultScreen");
   			}
   			break;   			
   		}
   	case CARTON_STATUS_AUDITED:
   		inform("carton audited");
   		if( requirePacklist() ) {
   			setNextScreen("packStation.PackListScreen");
   			break;
   		} else {
 				markCartonPacked();
   			if( isParcel() ) {
   				setNextScreen("packStation.CartonWeightScreen");
   			} else {
      			if( isToteOrSplitCase() )
      				setNextScreen("packStation.WaitLabelScreen");
      			else {
      				setParam(TermParam.resultMsg, String.format("LTL carton, bring to %s",getLtlCartonDestination()));
      				setNextScreen("packStation.ResultScreen");
      			}
   			}
   			break;
   		}
   	case CARTON_STATUS_SHORT:
   		setNextScreen("packStation.AuditScreen");
   		break;
   	case CARTON_STATUS_PICKED:
   		if( isTote() ) {
   			setNextScreen("packStation.ConfirmCartonTypeScreen"); 
   		} else if( isToteOrSplitCase() && requireQC() ) {
      		setNextScreen("packStation.AuditScreen");  			
   		} else {
   			if( isToteOrSplitCase() ) {
   				setNextScreen("packStation.WeightAuditScreen");
   			} else {
   				markCartonAudited();
   	   		if( requirePacklist() ) {
   	   			setNextScreen("packStation.PackListScreen");
   	   		} else {
   	 				markCartonPacked();
   	   			if( isParcel() ) {
   	   				setNextScreen("packStation.CartonWeightScreen");
   	   			} else {
   	      			if( isToteOrSplitCase() )
   	      				setNextScreen("packStation.WaitLabelScreen");
   	      			else {
   	      				setParam(TermParam.resultMsg, String.format("LTL carton, bring to %s",getLtlCartonDestination()));
   	      				setNextScreen("packStation.ResultScreen");
   	      			}
   	   			}
   	   		}   				
   			}
   		}
   		break;
   	case CARTON_STATUS_PICKING:
   		showAlertMsg("[%s] has open picks, finish pick first",cartonID); 
   		break;
		default:
			showAlertMsg("[%s] unknown error, check with supervisor",cartonID);	
   	}		
   }
   
   private void processLabeledCarton( int cartonSeq ) { 	
   	setCarton(cartonSeq);
   	String cartonStatus = getStrParam(TermParam.cartonStatus);
   	String cartonID = getStrParam(TermParam.cartonLpn);
   	if(cartonID.isEmpty())
   		cartonID = getStrParam(TermParam.cartonUcc);
   	switch(cartonStatus){
   	case CARTON_STATUS_CANCELED: 
   		showAlertMsg("[%s] is canceled",cartonID); 
   		break;
		default:
			setNextScreen("packStation.ValidateCartonScreen");
   	}		   	
   }
   
   private void processPallet( int palletSeq ) {
   	setPallet(palletSeq);
   	if( isLTLPallet(palletSeq) ) {
   		setParam(TermParam.resultMsg, String.format("LTL pallet, bring to %s",getPalletDestination(palletSeq)));
   		setNextScreen("packStation.ResultScreen");
   		return;
   	}
   	if( !validPalletType(PALLETTYPE_BULK) ) {
   		showAlertMsg("%s pallet, bring to OverPack Station");
   		return;
   	}
   	markPalletPicked();
		if( numNotLabeledCarton() == 0 ) {
			markPalletClosed();
			showSuccessMsg("Pallet complete");
		} else {
			setNextScreen("packStation.ScanSkuScreen");
		}
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}