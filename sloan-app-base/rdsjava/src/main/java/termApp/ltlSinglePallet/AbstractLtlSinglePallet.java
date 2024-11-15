package termApp.ltlSinglePallet;

import termApp.*;
import term.TerminalDriver;
import java.util.List;
import java.util.Map;

import app.AppCommon;
import rds.RDSUtil;



public class AbstractLtlSinglePallet extends AbstractNuminaScreen {


   public AbstractLtlSinglePallet( TerminalDriver term ) {
      super( term );
     //operators should be allowed to logout; this makes the footer display a logout button
     setLogoutAllowed(true); 
     AppCommon.setDatabase(db);
   }

   /*
    * processing methods
    */

   public List<Map<String,String>> getSinglePalletLocationDetails(){
   	return db.getResultMapList(
   			"SELECT location, p.* FROM rdsLocations l LEFT JOIN rdsPallets p ON l.assignmentValue=p.refValue "
   			+ "WHERE locationType='singlePallet' ORDER BY location");
   }
   
   public void doMarkAvailable( Map<String,String> m ) {
		String location = getMapStr(m,"location");
		int palletSeq = getMapInt(m,"palletSeq");
		List<String> cartons = db.getValueList(
				"SELECT cartonSeq FROM rdsCartons WHERE palletSeq=%d", palletSeq);
		String operator = getOperatorId();
		for( String cartonSeqStr: cartons ) {
			int cartonSeq = RDSUtil.stringToInt(cartonSeqStr, -1);
			AppCommon.triggerCartonPalletized(cartonSeq,palletSeq,operator);
		}
		AppCommon.triggerPalletClose(palletSeq, operator); 
		AppCommon.clearLocationAssignment(location);
   }


   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("SINGLE PALLET DROP-OFF");
      footer.show();
   }

   public void handleTick() {
      super.handleTick();
   }
}
