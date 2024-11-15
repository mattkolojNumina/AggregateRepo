package termApp.orderStart;


import rds.*;
import term.TerminalDriver;
import termApp.*;
import termApp.util.*;
import termApp.util.TermActionObject.OnActionListener;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.*;

import dao.SloaneCommonDAO;

public class AbstractCartonStartScreen
extends AbstractProjectAppScreen {

   protected InfoBox leftInfo;
   protected TermGroup lines;
   private String startScreen;

   protected static final String TOTE_REGEX = "T\\d{6}";
   
   public static enum TermParam {
      cartonSeq,
      scan,
      length,
      width,
      height,
      weight,
   };

   public AbstractCartonStartScreen(TerminalDriver term) {
      super(term);
      setAllDatabase();
   }

   protected List<Map<String, String>> getRegexMap() {	
   	return db.getResultMapList( 
   			"SELECT lpnFormat FROM cfgCartonTypes");
   }

   protected boolean isValidLpn(String lpn, String cartonType) {
      boolean isValidLpn = false;
      String regex = db.getString("", "SELECT lpnFormat FROM cfgCartonTypes WHERE cartonType = '%s'", cartonType);
      
      if (lpn.matches(regex)) 
         isValidLpn = true;

      return isValidLpn;
   }

   protected boolean isLpnAvailable (String lpn, String cartonType) {
      //inform("lpn in isLpnAvailable = [%s]", lpn);
   	if( cartonType.equalsIgnoreCase("TOTE")) {
   		int count = db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s' "
   				+ "AND cancelStamp IS NULL "
   				+ "AND shipStamp IS NULL "
   				+ "AND ( labelStamp IS NULL OR ( labelStamp > DATE_SUB(NOW(), INTERVAL 1 DAY) )  )", lpn);
   		if( count == 0 ) {
   			SloaneCommonDAO.setTableTombStoneByStringId("rdsCartons", "shipStamp", "lpn", lpn);
   			return true;
   		}
   		return false;
   	} else {
   		return db.getInt(-1, "SELECT COUNT(*) FROM rdsCartons WHERE lpn = '%s'", lpn) == 0;
   	}
   }
  
   
   public void setLpn(int cartonSeq, String lpn) {
   	db.execute("UPDATE rdsCartons SET lpn='%s', assigned = 1 WHERE cartonSeq=%d", lpn, cartonSeq);
   	inform("lpn %s is assigned for carton %d",lpn,cartonSeq);
   	SloaneCommonDAO.postCartonLog(""+cartonSeq, "cartonStart", "assigned lpn %s at carton start station",lpn);
   }   
   
   /*
    * interface methods
    */

   /** Initializes the screen and lays out UI elements. */
   public void handleInit() {
      super.handleInit();  // displays login footer
      initDisplay();
   }

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();  // calls handleScan -> processScan
   }

   /*
    * display methods
    */

   public void initDisplay() {
   	super.initDisplay();
      header.updateTitle("Carton Start Station");

   }
   
   private String completeMsg(boolean complete) {
      if (complete)
         return "Complete";
      return "Incomplete";
   }

   protected void initLines( boolean show ) {
      initLines();
      showLines(show);
   }
   
   protected void initLines() {
      if (lines != null)
         return;
      int t = 5;
      int y1 = INFO_Y;
      int y2 = 400;
      lines = new TermGroup(0,0);
      lines.add(horizontalLine(y1, t));
      lines.add(horizontalLine(y2, t));
      lines.add(verticalLine(y1, y2, W1_2,t));      
   }

   protected void showLines(boolean show) {
      if (show)
         lines.show();
      else
         lines.hide();
   }
   /*
    * buttons
    */
   
   @Override
   protected void doCancel() {
      trace("cancel");
      setNextScreen(startScreen);
   }

   protected OnActionListener modeAction() {
      return new OnActionListener() {
            public void onAction() {
         	//logout();
            inform("switch station mode");
            setNextScreen("ChangeModeScreen");
         }
      };
   }
   
   /*
    * helper methods
    */
   
   protected int getIntParam( TermParam param ) {
      return RDSUtil.stringToInt( term.fetchAtom( param.toString(), "" ), -1 );
   }
   
   protected double getDoubleParam( TermParam param ) {
      return RDSUtil.stringToDouble( term.fetchAtom( param.toString(), "" ), 0.00 );
   }

   protected String getStrParam( TermParam param ) {
      return term.fetchAtom( param.toString(), "" );
   }
   
   protected String getParam( TermParam param ) {
      return term.fetchAtom( param.toString(), null );
   }
   
   protected void setParam( TermParam param, String format, Object... args ) {
      if(format == null)
         term.dropAtom( param.toString() );
      else
         term.saveAtom( param.toString(), String.format(format, args) );
   }
   
   protected void clearAllParam() {
      for( TermParam param : TermParam.values())
         term.dropAtom(param.toString());
   }

}