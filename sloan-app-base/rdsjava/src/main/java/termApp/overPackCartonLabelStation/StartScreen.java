package termApp.overPackCartonLabelStation;

import static rds.RDSLog.*;
import term.TerminalDriver;

public class StartScreen extends AbstractOverPackCartonLabelStationScreen {

   public StartScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(true);
      clearAllParam();
   }

   @Override
	public void initDisplay() {
		initPromptModule(
				"Scan carton LPN",
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
   		setNextScreen("overPackCartonLabelStation.VerifyLabelScreen"); 
   		break;
   	case CARTON_STATUS_PACKED: 
   		inform("carton packed");
   		int uploadStatus = shipLabelRequestStatus();
   		if( uploadStatus<0 ) {
   			setNextScreen("overPackCartonLabelStation.CartonWeightScreen");
   			break;
   		} else if( uploadStatus == 0 ) {
   			setNextScreen("overPackCartonLabelStation.WaitShipLabelRequestResultScreen");
   			break;
   		} else {
   			setNextScreen("overPackCartonLabelStation.WaitLabelScreen");
   			break;   			
   		}   			
   	case CARTON_STATUS_AUDITED:
   	case CARTON_STATUS_SHORT:
   	case CARTON_STATUS_PICKED:
   	case CARTON_STATUS_PICKING:
   		showAlertMsg("[%s] is not packed, bring to pack station",cartonID); 
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
			setNextScreen("overPackCartonLabelStation.ValidateCartonScreen");
   	}		   	
   }
   

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}