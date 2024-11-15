/*
 * AbstractScreen.java
 */

package termApp;

import java.util.*;

import dao.SloaneCommonDAO;
import rds.*;
import term.ScreenHandler;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;


/**
 * Base class for terminal screens, defining constants and default
 * behavior.
 */
public abstract class AbstractScreen
      implements ScreenHandler {

   // general constants
   private static final String PKG = "termApp.";
   
   protected static final String ZERO_STAMP = "0000-00-00 00:00:00";
   protected static final String DEFAULT_SCREEN_NAME = "Default Screen";

   protected static final int KEY_HELP     =  1;
   protected static final int KEY_CONFIRM  =  3;
   protected static final int KEY_CANCEL   =  7;
   protected static final int KEY_SHUTDOWN = 10;

   private static final boolean TERM_OPERATORS = false;
   private static final boolean PRO_OP_TERM = true;


   // class variables
   protected TerminalDriver term;
   protected RDSDatabase db;

   protected boolean logoutAllowed, autoLogout;
   protected TermFooter footer;
   protected FaultNotifierBar faultBar;
   protected TermHeader header;
   protected TermBackground background;
   
   protected String bkgdColor = BKGD_COLOR;

   /*
    * constructor
    */

   /** Constructs a screen with default values. */
   public AbstractScreen( TerminalDriver term ) {
      this.term = term;
      this.db = term.getDb();
      setTerm();
      clearAllTags();

      footer = new TermFooter();
      header = new TermHeader();
      faultBar = new FaultNotifierBar();
      background = new TermBackground();
      logoutAllowed = false;
      autoLogout = false;
      
   }

   /*
    * ScreenHandler interface methods
    */

   /** Initializes the screen and lays out UI elements. 
    * @throws ProcessingException */
   public void handleInit() {
      preInit();
      initDisplay();
      postInit();
   }

   public void preInit() {
      term.clearScreen( bkgdColor );

      faultBar.init();
      footer.init();
      header.init();
      footer.setTermName(term.getTermName());
   }
   
   public void initDisplay() {
      term.saveAtom( "screenName", DEFAULT_SCREEN_NAME );
   }
   
   public void postInit() {
      background.init(bkgdColor);
   }

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      footer.tick();
      faultBar.tick();
      background.tick();
      if (autoLogout)
         monitorForceLogout();
   }

   /** Performs exit tasks before loading the next screen. */
   public void handleExit() {
      
   }
   
   /** Processes a button press. */
   public void handleButton( int tag ) {
      inform( "button %d pressed (ignored)", tag );
   }

   /** Processes a function key. */
   public void handleFunction( int key ) {
      if (key == KEY_HELP) {
         inform( "fn key F%d triggered -- help", key );
         doHelp();
      } else if (key == KEY_SHUTDOWN) {
         inform( "fn key F%d triggered -- shutdown", key );
         doShutdown();
      } else
         inform( "fn key F%d triggered (ignored)", key );
   }

   /** Processes text from an entry field. */
   public void handleEntry( int tag, String text ) {
      inform( "entry %d provided text [%s] (ignored)", tag, text );
   }

   /** Processes text from a serial device. */
   public void handleSerial( int tag, String text ) {
      inform( "serial %d received text [%s] (ignored)", tag, text );
   }

   /** Processes a message from the terminal. */
   public void handleMessage( String text ) {
      alert( text );
   }
   
   /*
    * processing methods
    */
   
   protected void setAllDatabase() {
      RDSCounter.setDatabase( db );
      RDSHistory.setDatabase( db );
      RDSTrak.setDatabase( db );
      SloaneCommonDAO.setDatabase(db);
   }
   
   private void clearAllTags() {
      TermUtility.clearTags();
   }

   private void setTerm() {
      TermUtility.setTerm(term, this);
   }
   
   protected String determineStartPage() {
      String mode = getStrParam( "mode" );
      inform("mode = %s",mode) ;

      //Big Screen BoomRoom display
      if("BigScreenDU".equals(mode))
         return "BigScreen.BigScreenDU";
      //Big Screen Liquids/Perishables display
      if("BigScreenPQ".equals(mode))
         return "BigScreen.BigScreenPQ";
      //Big Screen Carton Start Display
      if("BigScreenCartonStart".equals(mode))
         return "BigScreen.BigScreenCartonStart";
      //Big Screen Geek display
      if("BigScreenGeek".equals(mode))
         return "BigScreen.BigScreenGeek";
      //Big Screen Truck Changes display
      if("BigScreenTruck".equals(mode))
         return "BigScreen.BigScreenTruck";
      //Order Start
      if("orderStart".equals(mode))
         return "orderStart.IdleScreen";
      //cartBuild station
      else if("cart".equals(mode))
	      return "cart.LocationScreen";
      //QC audit station
      else if("audit".equals(mode))
         return "auditStation.StartScreen";
      //Manual Labeling station
      else if("labeling".equals(mode))
         return "packStation.StartScreen";    
      //inline pack station
      else if("inlinePack".equals(mode))
         return "inlinePack.IdleScreen";    
      //ltl single pallet station
      else if("ltlSinglePallet".equals(mode))
         return "ltlSinglePallet.SinglePalletReady";      
      //Overpack carton pack station
      else if("overPackCartonPack".equals(mode))
         return "overPackCartonPackStation.StartScreen";
      //Overpack carton label station
      else if("overPackCartonLabel".equals(mode))
         return "overPackCartonLabelStation.StartScreen";      
      //Pallet Load (AMR)
      else if("palletDropoff".equals(mode))
         return "palletDropoff.EmptyPalletReady";
      //Pallet Build Cell
      else if("palletbuildcell".equals(mode))
         return "palletBuild.PalletBuildStation";
      //Exception Station
      else if("exceptionStation".equals(mode))
         return "exception.IdleScreen";
      // Pallet Build Cell
      else if ("timlinePack".equals(mode))
         return "timlinePack.Screen1";
      //Sorter Exception Station
      else if("sorterException".equals(mode))
         return "exception.SorterScreen";
      else return "packlist.ScanCartonScreen";  
   }

   protected boolean getMapIntBln( Map<String,String> m, String name ) {
      if (m == null)
         return false;
      return RDSUtil.stringToInt( m.get( name ), 0 ) >= 1;
   }
   
   protected boolean getMapStrBln( Map<String,String> m, String name ) {
      if (m == null)
         return false;
      return "true".equals(m.get( name ));
   }
   
   protected boolean getMapStamp( Map<String,String> m, String name, boolean zeroReturns ) {
      if (m == null)
         return false;
      String stamp = m.get( name );
      if (stamp == null)
         return false;
      if (stamp.equals(ZERO_STAMP))
         return zeroReturns;
      return true;
   }
   
   protected boolean getMapStamp( Map<String,String> m, String name ) {
      return getMapStamp(m, name, false);
   }

   protected int getMapInt( Map<String,String> m, String name ) {
      if (m == null)
         return -1;
      return RDSUtil.stringToInt( m.get( name ), -1 );
   }

   protected String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String val = m.get( name );
      return (val == null) ? "" : val;
   }
   
   protected static double getMapDouble( Map<String,String> m, String name ) {
      if (m == null)
         return 0.0;
      return RDSUtil.stringToDouble( m.get( name ), 0.0 );
   }
   
   protected void saveParams() {
      Map<String,String> paramMap = db.getControlMap( term.getTermName() );
      for (String paramName : paramMap.keySet()) {
         String paramVal = paramMap.get( paramName );
         if (paramVal == null)
            term.dropAtom( paramName );
         else
            term.saveAtom( paramName, paramVal );
      }
   }
   
   protected int getIntParam( String name ) {
      return RDSUtil.stringToInt( term.fetchAtom( name, "" ), -1 );
   }

   protected String getStrParam( String name ) {
      return term.fetchAtom( name, "" );
   }
   
   protected String getParam( String name ) {
      return term.fetchAtom( name, null );
   }
   
   protected void setParam( String name, String format, Object... args ) {
      if(format == null)
         term.dropAtom( name );
      else
         term.saveAtom( name, String.format(format, args) );
   }
   
   protected void saveMode(String mode) {
      if (mode == null || mode.isEmpty())
         return;
      setParam("mode", mode);
      setParam("savedMode", mode);
   }
   
   protected void restoreSavedMode() {
      String mode = getParam("savedMode");
      if (mode == null || mode.isEmpty())
         return;
      setParam("mode", mode);
   }
   
   public String getOperatorId() {
   	return term.fetchAtom( "operatorId", "" );
   }

   public String getFooterOperatorId() {
      String operatorId = getOperatorId();
      if (operatorId == null || operatorId.isEmpty())
         return operatorId;
      return getOperatorDisplayName(operatorId, true);
   }

   protected String getStationName() {
      String val  = term.fetchAtom( "stationName", "" );
      return val;
   }
   
   protected String getStationArea() {
      String area  = term.fetchAtom( "area", "" );
      return area;
   }

   protected String getStationTask() {
      String task = term.fetchAtom( "stationTask", "" );
      if (!task.isEmpty())
         return task;
      String mode = term.fetchAtom( "mode", "terminal" );
      return mode + "Station";
   }


   // TESTING MY FUNCTION
   protected List<String> getSku(String CarID){
      List<String> mytest = new ArrayList<>();
      if(CarID == null || CarID.isEmpty() )
         return null;
         
         mytest = db.getValueList("SELECT sku FROM items WHERE cartonID = '%s' "
      , CarID);
      
      inform("Data when you call getSku: ");
      for( String str : mytest)
      {
         inform(str);
      }
      return mytest;
   }

   protected int getQuantity(String SKU){
      int quantity=0;
      if(SKU == null || SKU.isEmpty() )
         return 0;
         quantity = db.getInt(0,"SELECT qty FROM items WHERE sku = '%s' "
         , SKU);
         inform("Quantity for SKU [%s]: [%d]", SKU, quantity);
         return quantity;

   }
   
   //Not used anywhere
   protected int getNumOfSku(String CarID){
      int totalOrders=0;
      if(CarID == null || CarID.isEmpty() )
         return 0;
         totalOrders = db.getInt(0,"SELECT COUNT(*) FROM items WHERE cartonID = '%s' "
         , CarID);
         inform("Num of orders is: [%d]",totalOrders);
         return totalOrders;
   }

   protected boolean checkMatch(Button myButton){
      inform("checking if it matches");
      if(myButton.countTotal() == myButton.getCount())
      {  
         myButton.setCompletion(true);
         inform("it matches!");
         return true;
      }
      
      return false;
   }

   protected boolean checkCompletion(List<Button> allButtons){
      for(int i=0; i< allButtons.size();i++)
      {
         if(!allButtons.get(i).getCompletion())
            return false;
      }
      return true;
   }
   // TESTING


   protected String getOperatorName(String operatorId) {
      if (operatorId == null || operatorId.isEmpty() )
         return null;
      return db.getString(null, "SELECT operatorName FROM proOperators " +
            "WHERE operatorId = '%s'", operatorId );
   }
   
   protected String getOperatorDisplayName(String operatorId, boolean append) {
      if (operatorId == null || operatorId.isEmpty() )
         return "N/A";
      String name = getOperatorName(operatorId);
      if (name == null || name.isEmpty() )
         return operatorId;
      if (append)
         return String.format("%s (%s)", name, operatorId);
      return name;
   }
   
   protected boolean isOperator( String operatorId ) {
      if ( 0 == db.getInt(0, "SELECT COUNT(*) FROM proOperators " +
            "WHERE operatorId = '%s'", operatorId ) ) {
         alert("invalid operator id [%s]", operatorId);
         return false;
      }
      return true;
   }
   
   protected String getOperatorLoggedInLocation( String operatorId ) {
      String loggedInLoc = "";
      if (!PRO_OP_TERM)
         return loggedInLoc;
      String loggedInTerm = db.getString("", 
            "SELECT terminal FROM proOperators " +
            "WHERE operatorID = '%s' ",
            operatorId );
      if (loggedInTerm == null || loggedInTerm.isEmpty())
         return loggedInLoc;
      
      String mode = db.getControl(loggedInTerm, "mode", "");
      if (mode != null && !mode.isEmpty())
         loggedInLoc = mode + " Station";
      
      String area = db.getControl(loggedInTerm, "area", "");
      if (area != null && !area.isEmpty())
         loggedInLoc = loggedInLoc + " (" + area + ")";
      
      if (!loggedInLoc.isEmpty())
         loggedInLoc = "at " + loggedInLoc;
      
      return loggedInLoc;
   }
   
   protected boolean operatorLoggedIn( String operatorId ) {
      if (!PRO_OP_TERM)
         return false;
      Map<String,String> m = db.getRecordMap("SELECT * FROM proOperators WHERE operatorID='%s'", operatorId);
      String terminal = getMapStr(m,"terminal");
      if( terminal.equals(term.getTermName()) )
      	return false;
      return !getMapStr(m,"task").isEmpty();
   }
   
   protected void doLogin(String operatorId) {
      login( operatorId );
      setNextScreen( determineStartPage() );      
   }

   private void login( String operatorId ) {
      term.saveAtom( "operatorId", operatorId );
      footer.setOperator(operatorId);
      String task = getStationTask();
      String area = getStationArea();

      // log the operator off any tasks currently logged onto
      db.execute(
            "UPDATE proOperatorLog SET " +
            "endTime = NOW() " +
            "WHERE operatorID = '%s' " +
            "AND endTime IS NULL",
            operatorId );

      // log the operator off any terminals logged onto
      if (TERM_OPERATORS)
         db.execute(
            "UPDATE termOperators SET " +
            "operatorID = '', " +
            "logoutAllowed = 'false', " + 
            "autoLogout = 'false' " +
            "WHERE operatorID = '%s'",
            operatorId );
      
      // log the operator in to the current station task/area
      if (PRO_OP_TERM)
         db.execute(
               "UPDATE proOperators SET " +
               "task = '%s', " +
               "area = '%s', " +
               "terminal = '%s' " +
               "WHERE operatorID = '%s'",
               task, area, term.getTermName(), operatorId );
      else
         db.execute(
               "UPDATE proOperators SET " +
               "task = '%s', " +
               "area = '%s' " +
               "WHERE operatorID = '%s'",
               task, area, operatorId );
      db.execute(
            "INSERT INTO proOperatorLog SET " +
            "operatorID = '%s', " +
            "task = '%s', " +
            "area = '%s', " +
            "startTime = NOW(), " +
            "endTime = NULL",
            operatorId, task, area );
      
      // log the operator in to the current terminal
      if (TERM_OPERATORS)
         db.execute(
            "REPLACE INTO termOperators " +
            "(terminal, operatorID, logoutAllowed, autoLogout) " +
            "VALUES ('%s', '%s', '%s', '%s' )",
            term.getTermName(), operatorId, 
            logoutAllowed ? "true" : "false",
            autoLogout ? "true" : "false" );
      
      trace( "login [%s] area [%s] task [%s]", operatorId, area, task );
      setParam("operatorId",operatorId);
   }

   protected void setLogoutAllowed( boolean isAllowed ) {
      setLogoutParams(isAllowed,false);
   }

   protected void setAutoLogoutMonitor( boolean enableMonitor ) {
      setLogoutParams(true,enableMonitor);
   }
   private void setLogoutParams( boolean isAllowed, boolean enableMonitor ) {
      logoutAllowed = isAllowed;
      autoLogout = enableMonitor;
      footer.enableLogout(isLoginRequired(), logoutAllowed);

      if (TERM_OPERATORS)
         db.execute(
            "UPDATE termOperators SET " +
            "logoutAllowed = '%s', " +
            "autoLogout = '%s' " +
            "WHERE terminal = '%s'",
            logoutAllowed ? "true" : "false",
            autoLogout ? "true" : "false",
            term.getTermName() );
   }
   
   private boolean isLoginRequired() {
      return "true".equals( getParam( "login" ) );
   }
   
   private void logout() {
      // log the operator off the current terminal
      if (TERM_OPERATORS)
         db.execute(
            "UPDATE termOperators SET " +
            "operatorID = '', " +
            "logoutAllowed = 'false', " +
            "autoLogout = 'false' " +
            "WHERE terminal = '%s'",
            term.getTermName() );
      
      String operatorId = getOperatorId();
      if (operatorId == null || operatorId.isEmpty())
         return;

      String task = getStationTask();
      String area = getStationArea();

      // log the operator off from the current station task/area
      if (PRO_OP_TERM)
         db.execute(
            "UPDATE proOperators SET " +
            "task = '', " +
            "area = '', " +
            "terminal = '' " +
            "WHERE operatorID = '%s' " +
            "AND task = '%s' " +
            "AND area = '%s' " +
            "AND terminal = '%s'",
            operatorId, task, area, term.getTermName() );
      else 
         db.execute(
               "UPDATE proOperators SET " +
               "task = '', " +
               "area = '' " +
               "WHERE operatorID = '%s' " +
               "AND task = '%s' " +
               "AND area = '%s'",
               operatorId, task, area );
      db.execute(
            "UPDATE proOperatorLog SET " +
            "endTime = NOW() " +
            "WHERE operatorID = '%s' " +
            "AND task = '%s' " +
            "AND area = '%s' " +
            "AND endTime IS NULL",
            operatorId, task, area );

      trace( "logout [%s]", operatorId );
      term.dropAtom( "operatorId" );
   }

   private void monitorForceLogout() {
      if (!TERM_OPERATORS)
         return;
      String operatorId = getParam( "operatorId" );
      if (operatorId == null || operatorId.isEmpty())
         return;
      
      String op = db.getString(null, "SELECT operatorID FROM termOperators " + 
            "WHERE terminal = '%s'", term.getTermName());
      if (op == null)
         return;
      if (op.equals(operatorId))
         return;
      alert("force log out detected");
      doLogout();
   }
   
   protected void postHistory( String containerType, int containerSeq,
         String code, String format, Object... args ) {
      trace( containerType + " " + containerSeq + ": " + format, args );
      RDSHistory.post( containerType, "" + containerSeq, code, format, args );
   }

   protected void recordOperation( String operation, int qty ) {
      String operatorId = getParam( "operatorId" );
      if (operatorId == null || operatorId.isEmpty())
         return;

      db.execute(
            "INSERT INTO proTracker SET " +
            "operatorID = '%s', " +
            "task = '%s', " +
            "area = '%s', " +
            "operation = '%s', " +
            "value = %d, " +
            "stamp = NOW()",
            operatorId, getStationTask(), getStationArea(), operation, qty );
      RDSCounter.add(qty, operation);

   }

   protected void setNextScreen( String screen ) {
      term.setNextScreen( PKG + screen );
   }

   public void processLogout() {
      if (!logoutAllowed) {
         alert("logout not allowed");
         return;
      }
      
      doLogout();
   }
   
   protected void doLogout() {
      logout();
      setNextScreen( isLoginRequired() ? "LoginNumPadScreen" : determineStartPage() );      
   }

   /*
    * layout methods
    */
   
   @Deprecated
   protected void drawGrid(int box ) {
      String c = "$DD000000";
      int font = 10;
      for (int i = 0; i <= SCREEN_HEIGHT; i += box ) {
         new Rectangle( 0, i-2, SCREEN_WIDTH, 4, c, 0, c, true);
         new TextField( 0, i+font, font, ""+i, Align.LEFT);
      }
      for (int i = 0; i <= SCREEN_WIDTH; i += box ) {
         new Rectangle( i-2, 0, 4, SCREEN_HEIGHT, c, 0, c, true);
         new TextField( i, 0, font, ""+i, Align.LEFT);
      }
   }
   
   @Deprecated
   protected void drawGrid(int xDiv, int yDiv) {
      String c = "$DD000000";
      int font = 10;
      for (int i = 0; i <= yDiv; i++) {
         new Rectangle( 0, SCREEN_HEIGHT*i/yDiv-2, SCREEN_WIDTH, 4, c, 0, c, true);
         new TextField( 0, SCREEN_HEIGHT*i/yDiv-2+font, font, ""+(SCREEN_HEIGHT*i/yDiv), Align.LEFT);
      }
      for (int i = 0; i <= xDiv; i++) {
         new Rectangle( SCREEN_WIDTH*i/xDiv-2, 0, 4, SCREEN_HEIGHT, c, 0, c, true);
         new TextField( SCREEN_WIDTH*i/xDiv-2+font, 0, font, ""+(SCREEN_WIDTH*i/xDiv), Align.LEFT);
      }
   }
   
   protected Rectangle verticalLine( int y0, int y1, int x, int thickness) {
      return new Rectangle(x-thickness/2,y0,thickness, y1-y0, "black",0,INVISIBLE,false);
   }
   
   protected Rectangle verticalLine( int x, int thickness) {
      return verticalLine( 0, SCREEN_HEIGHT, x, thickness);
   }
   
   protected Rectangle horizontalLine( int x0, int x1, int y, int thickness) {
      return new Rectangle(x0,y-thickness/2,x1-x0,thickness, "black",0,INVISIBLE,false);
   }
   
   protected Rectangle horizontalLine( int y, int thickness) {
      return horizontalLine( 0, SCREEN_WIDTH, y, thickness);
   }
   
   protected Rectangle simpleBox( int x0, int y0, int x1, int y1, String color) {
      return new Rectangle(x0,y0,x1-x0, y1-y0, "",0,color,false);
   }

   protected void doHelp() {
      // not yet implemented
   }

   protected void doShutdown() {
      trace( "terminate application" );
      System.exit( 0 );
   }

   /*
    * helper display functions
    */
   public TermFooter getFooter() {
      return footer;
   }
}
