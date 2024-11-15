package termApp.overPackCartonPackStation;

import static app.Constants.*;
import static rds.RDSLog.*;
import term.TerminalDriver;


public class StartScreen extends AbstractOverPackCartonPackStationScreen {

   public StartScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(true);
      clearAllParam();
   }

   @Override
	public void initDisplay() {
		initPromptModule(
				"Scan carton/pallet LPN",
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
      if(!scan.isEmpty()) {
         setParam(TermParam.scan, scan);
         int cartonSeq = -1;
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
   	if( cartonID.length() > 8 )
   		cartonID = cartonID.substring(cartonID.length()-8);
   	inform("carton status %s",cartonStatus);
   	switch(cartonStatus){
   	case CARTON_STATUS_CANCELED: 
   		showAlertMsg("[%s] is canceled",cartonID); 
   		break;
   	case CARTON_STATUS_LABELED: 
   		inform("carton labeled");
   		showSuccessMsg("[%s] is labeled, bring to label station",cartonID);
   		break;
   	case CARTON_STATUS_PACKED: 
   		inform("carton packed");
   		showSuccessMsg("[%s] is packed, bring to label station",cartonID); 
   		break;
   	case CARTON_STATUS_AUDITED:
   		inform("carton audited");
   		if( requirePacklist() ) {
   			setNextScreen("overPackCartonPackStation.PackListScreen");
   			break;
   		} else {
 				markCartonPacked();
 				showSuccessMsg("[%s] is packed, bring to label station",cartonID); 
   			break;
   		}
   	case CARTON_STATUS_SHORT:
   		setNextScreen("overPackCartonPackStation.AuditScreen");
   		break;
   	case CARTON_STATUS_PICKED:
   		if( requireQC() ) {
      		setNextScreen("overPackCartonPackStation.AuditScreen");  			
   		} else {
				markCartonAudited();
	   		if( requirePacklist() ) {
	   			setNextScreen("overPackCartonPackStation.PackListScreen");
	   			break;
	   		} else {
	 				markCartonPacked();
	 				showSuccessMsg("[%s] is packed, bring to label station",cartonID); 
	   			break;
	   		} 				
   		}
   		break;
   	case CARTON_STATUS_PICKING:
   		setNextScreen("overPackCartonPackStation.AuditScreen");
   		break;
		default:
			showAlertMsg("[%s] unknown error, check with supervisor",cartonID);	
   	}		
   }
   
   private void processPallet( int palletSeq ) {
   	setPallet(palletSeq);
   	if( isLTLPallet(palletSeq) ) {
   		setParam(TermParam.resultMsg, String.format("LTL pallet, bring to %s",getPalletDestination(palletSeq)));
   		setNextScreen("overPackCartonPackStation.ResultScreen");
   		return;
   	}
   	if( !validPalletType(PALLETTYPE_AMROVERPACK) ) {
   		showAlertMsg("%s pallet, bring to Labeling Station");
   		return;
   	}
   	if( palletClosed() ) {
   		showAlertMsg("pallet is completed already!");
   		return;
   	}
   	String workStation = getPalletWorkStation();
   	if( !workStation.equals(getStrParam("stationName")) && !workStation.isEmpty()) {
   		showAlertMsg("pallet is being processed at %s",workStation);
   		return;
   	}
   	setPalletAtWorkStation();
   	markPalletPicked();
		if( numNotPackedCarton() == 0 ) {
			markPalletClosed();
			showSuccessMsg("Pallet complete");
		} else {
			setNextScreen("overPackCartonPackStation.ScanSlotScreen");
		}
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}