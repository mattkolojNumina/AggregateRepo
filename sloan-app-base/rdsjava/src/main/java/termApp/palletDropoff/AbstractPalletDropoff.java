package termApp.palletDropoff;

import termApp.*;

import java.util.List;
import java.util.Map;

import term.TerminalDriver;



public class AbstractPalletDropoff extends AbstractNuminaScreen {

   public AbstractPalletDropoff( TerminalDriver term ) {
      super( term );
     //operators should be allowed to logout; this makes the footer display a logout button
     setLogoutAllowed(true); 
   }
   
   /*
    * processing methods
    */
   public List<String> getPalletDropOffLocations(){
   	return db.getValueList(
   			"SELECT location FROM rdsLocations WHERE locationType='palletDropoff' ORDER BY location");
   }
   
   public Map<String,String> getLocationStatus(String location){
   	return db.getRecordMap("SELECT * FROM rdsLocations WHERE location='%s'",location);
   }
   
   public void dropoffStack( String location ) {
   	db.execute("UPDATE rdsLocations SET assignmentValue = 'palletStack' WHERE location = '%s'", location);
   }
   
   public void dropoffSingle( String location ) {
   	db.execute("UPDATE rdsLocations SET assignmentValue = 'singlePallet' WHERE location = '%s'", location);
   }
   
   public int getDestackHealthy() {
   	return db.getInt(0,"SELECT SUM(enabled) FROM rdsLocations WHERE locationType='destacker'");
   }

   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("PALLET DROP-OFF");
      footer.show();
   }

   public void handleTick() {
      super.handleTick();
   }




}
