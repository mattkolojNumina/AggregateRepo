package termApp.auditStation;

import static rds.RDSLog.*;
import static app.Constants.*;
import term.TerminalDriver;

public class StartScreen extends AbstractAuditStationScreen {

   public StartScreen( TerminalDriver term ) {
      super(term) ;
      setLogoutAllowed(true);
      clearAllParam();
   }

   @Override
	public void initDisplay() {
		initPromptModule(
				"Scan carton/tote LPN",
				ENTRYCODE_REGULAR,
				false,
				true);
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
         int cartonSeq = lookupCartonByLpn(scan);
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
   	if( cartonID.length() > 9 )
   		cartonID = cartonID.substring(cartonID.length()-9);
   	switch(cartonStatus){
   	case CARTON_STATUS_CANCELED: 
   		showAlertMsg("[%s] is canceled",cartonID); 
   		break;
   	case CARTON_STATUS_LABELED: 
   	case CARTON_STATUS_PACKED: 
   	case CARTON_STATUS_AUDITED:
   		showSuccessMsg("[%s] is audited, bring to conveyor",cartonID); 
   		break;
   	case CARTON_STATUS_SHORT:
   	case CARTON_STATUS_PICKED:
      	setNextScreen("auditStation.AuditScreen");  			
   		break;
   	case CARTON_STATUS_PICKING:
   		showAlertMsg("[%s] has open picks, finish pick first",cartonID); 
   		break;
		default:
			showAlertMsg("[%s] unknown error, check with supervisor",cartonID);	
   	}		
   }

   public void handleTick() {
      super.handleTick();
   }

   // helpers

}