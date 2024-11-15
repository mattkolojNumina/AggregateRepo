/*
 * AbstractRUTScreen.java
 */

package termApp;

import static rds.RDSLog.*;

import java.util.List;
import java.util.Map;

import rds.RDSDatabase;
import rds.RDSTrak;
import rds.RDSUtil;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.InfoBox.InfoBoxConstructor;
import termApp.util.ScrollBox.ScrollBoxConstructor;

import static termApp.util.Constants.*;

import termApp.util.TermActionObject.OnActionListener;
import termApp.util.TermActionObject.OnTextActionListener;

/**
 * Base class for terminal screens, defining constants and default
 * behavior.
 */
public abstract class AbstractProjectAppScreen
      extends AbstractScreen {
   protected static final String BARCODE_LOGOUT = "LOGOUT";
   protected static final String BARCODE_CANCEL = "CANCEL";
   protected static final String BARCODE_COMPLETE = "COMPLETE";
   protected static final String BARCODE_OK = "OK";
   protected static final String BARCODE_CONFIRM = "CONFIRM";
   protected static final String BARCODE_SKIP = "SKIP";
   protected static final String BARCODE_BACK = "BACK";
   protected static final String BARCODE_REPRINT = "REPRINT";
   protected static final String BARCODE_NOREAD = "?";
   protected static final String BARCODE_OVERRIDE = "MANUAL_OVERRIDE";

   
   protected static final int BOX_STATUS_FAILED_WEIGHT   = -2;
   protected static final int BOX_STATUS_ERROR           = -1;
   protected static final int BOX_STATUS_PROCESSING      = 0;
   protected static final int BOX_STATUS_OKAY            = 1;
   protected static final int BOX_STATUS_QC              = 2;

   protected static final int RESPONSE_CODE_SUCCESS = 200;

   protected static final int PRIORITY_PACK_STATION = 0;

   // rect
   protected static final int RECT_X = MARGIN;
   protected static final int RECT_Y = 665;
   protected static final int RECT_H = SCREEN_HEIGHT-RECT_Y-140;
   
   
   //prompt module 
   protected static final int RECT_W = SCREEN_WIDTH-MARGIN;
   protected static final int RECT_TEXT_HEIGHT = 100;
   protected static final int RECT_ENTRY_HEIGHT = 100;
   protected static final int RECT_BUTTON_HEIGHT = 100;
   protected static final int MARGIN_SMALL = 25;
   
   //entry type constants
   public static final int ENTRYCODE_NONE = 0;
   public static final int ENTRYCODE_REGULAR = 1;
   public static final int ENTRYCODE_NUMBER = 2;
   public static final int ENTRYCODE_PASSWORD = 3;

   
   protected static final int REG_BOX = 0;
   protected static final int BOX_EMPTY = 0;
   protected static final int BOX_NONE = -1;
   protected static final int BOX_UNKNOWN = -2;
   protected static final int BOX_LOST = -99;
   
   
   private static final double DEFAULT_WEIGHT_CONVERSION = 0.01;
   //private static final double MAX_WEIGHT = 100;
   
   
   protected final String station;
   protected final String rScanner, rScale;
   private final double weightConversion;
   protected final String printer, labeler, reservationId, facility;

   protected TextWrap promptText;
   protected Rectangle promptRect;
   protected TextEntry promptEntry;
   protected PasswordEntry promptPWEntry;
   protected TextWrap resultMsgText;
   protected Rectangle resultMsgRect;
   protected ScrollBoxConstructor scrollBoxModel;
   protected ScrollBox scrollBox;
   protected List<Map<String, String>> scrollBoxList, scrollBoxListBackup;
   protected InfoBoxConstructor infoBoxModel;
   protected InfoBox rightInfoBox, leftInfoBox;
   protected TermGroup infoBoxLines;
   
   protected boolean cycleCount;
   protected int cycleMax,cycle;
   protected String host="";
   protected String nextScreen;

   protected Double MIN_WEIGHT = 0.5;
   protected Double MAX_WEIGHT = 250.0;
   protected Double MIN_HEIGHT = 1.0;
   protected Double MAX_HEIGHT = 72.0;
   protected Double MIN_LENGTH = 2.0;
   protected Double MAX_LENGTH = 72.0;
   protected Double MIN_WIDTH = 2.0;
   protected Double MAX_WIDTH = 72.0;
   
   private long tempDisplaySet, tempDisplayDuration, tempDispStdDuration;
   private static final long DEFAULT_DURATION = 5000L;
   protected long start = 0L, processing = 0L, lastActivity = 0L, reset = 5000L, timeout = 10000L, logout = 20 * 60 * 1000L;
   
   // each row takes 50 vertiacally
   protected int numOfInfoRows;
   protected int numOfBottomRows;
   
   /** Constructs a screen with default values. */
   public AbstractProjectAppScreen( TerminalDriver term ) {
      super( term );

      station = getStrParam("station") ;
      setLogoutAllowed(false);

      this.rScanner = getStrParam( "scanner" );
      this.rScale   = getStrParam( "scale" );
      this.printer  = getStrParam( "printer" );
      this.labeler  = getStrParam( "labeler" );
      this.weightConversion = getWeightConversion();
      
      this.facility  = getStrParam( "facility" );
      host = db.getString("","SELECT host FROM launch WHERE nickName='tm-pk1'");
      
      int autoLogoff = RDSUtil.stringToInt(db.getControl("system", "autoLogoff", "20"),20);
      logout = autoLogoff * 60 * 1000L;
      
      numOfInfoRows = 0;
      numOfBottomRows = 0;
      
      reservationId = term.getTermName();
      setTempDisplayDuration(DEFAULT_DURATION);
      
      initActivityTimer();
      //getMaxValues();
   }

   protected void getMaxValues() {
      getMaxWeight();
      getMaxWidth();
      getMaxHeight();
      getMaxLength();
   }

   protected void getMaxWeight() {
      // weight
      try {
         MAX_WEIGHT = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxWeight"));
      } catch(Exception ex) {
         alert("Using default max weight: %.2f",MAX_WEIGHT);
      }
   }

   protected void getMaxWidth() {
      // width
      try {
         MAX_WIDTH = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxWidth"));
      } catch(Exception ex) {
         alert("Using default max width: %.2f",MAX_WIDTH);
      }
   }

   protected void getMaxHeight() {
      // height
      try {
         MAX_HEIGHT = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxHeight"));
      } catch(Exception ex) {
         alert("Using default max weight: %.2f",MAX_HEIGHT);
      } 
   }

   protected void getMaxLength() {
      // length
      try {
         MAX_LENGTH = Double.parseDouble(db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='%s' AND NAME='%s'",host,"tm-pk1","maxLength"));
      } catch(Exception ex) {
         alert("Using default max length: %.2f",MAX_LENGTH);
      }
   }
   
   private double getWeightConversion() {
      String val = getStrParam("weightConversion");
      if (val == null || val.isEmpty())
         return DEFAULT_WEIGHT_CONVERSION;
      
      double conversion = RDSUtil.stringToDouble(val, 0.00);
      if (conversion <= 0)
         return DEFAULT_WEIGHT_CONVERSION;

      return conversion;
   }

   /*
    * ScreenHandler interface methods
    */
   
   public void initDisplay() {
      super.initDisplay();
      clearScan();
      clearScale();
      header.updateTitle(getStrParam("stationName"));
		footer.show();
		footer.refresh();
   }
   
   public void initPromptModule(String promptMsg, int numOfMsgLines,
   				int entryCode, int entryWidth, 
   				boolean hasButton, boolean hasResultMsgModule, String color,
   				int numOfResultMsgLines, boolean promptShareButtonLine ) {
 	  	int rectHeight = RECT_TEXT_HEIGHT*numOfMsgLines + (entryCode>0?RECT_ENTRY_HEIGHT:0) + (hasButton&&!promptShareButtonLine?RECT_BUTTON_HEIGHT:0);
 	  	int rectY = 990 - MARGIN_SMALL - rectHeight;
 	  	int currentY = rectY;
 	  	promptText = new TextWrap(MARGIN, currentY, SCREEN_WIDTH-MARGIN*2, FONT_L, numOfMsgLines);
 	  	promptText.wrap(promptMsg);	 
 	  	promptText.show();
 	  	if( hasButton )
 	  		numOfBottomRows += 2;
 	  	if( entryCode>0 ) {
 	  		numOfBottomRows += 2;
 	  		currentY += RECT_TEXT_HEIGHT;
 	  		if( entryCode == ENTRYCODE_REGULAR || entryCode == ENTRYCODE_NUMBER) {
 		      promptEntry = new TextEntry(MARGIN,currentY,FONT_L,entryWidth,RECT_ENTRY_HEIGHT,true);
 		      promptEntry.registerOnTextActionListener(new OnTextActionListener() {
 		         @Override
 		         public void onAction(String text) {
 		            inform( "entry received text [%s]", text );
 		            inputDecision( text );            
 		         }
 		      }); 
 		      promptEntry.setFocus();
 	  		}
         if( entryCode == ENTRYCODE_PASSWORD ) {
 			  	promptPWEntry = new PasswordEntry(MARGIN,currentY,FONT_L,entryWidth,RECT_ENTRY_HEIGHT,true);
 			   promptPWEntry.registerOnTextActionListener(new OnTextActionListener() {
 		         @Override
 		         public void onAction(String text) {
 		            inform( "entry received text [%s]", text );
 		            inputDecision( text );            
 		         }
 		      }); 
 			   promptPWEntry.setFocus();
 		  	}
 	  	} if( hasResultMsgModule ) {
 	  		initResultMsgModule(rectY-RECT_TEXT_HEIGHT*numOfResultMsgLines, numOfResultMsgLines);
 	  		numOfBottomRows += 2*numOfResultMsgLines;
 	  	}
 	  	promptRect = new Rectangle(MARGIN_SMALL,rectY,SCREEN_WIDTH-MARGIN,rectHeight,color,1,"black",true);
 	   numOfBottomRows += numOfMsgLines*2;
   }
   
   // default module has 1 line text, full size entry, and fill color blue
   public void initPromptModule(String promptMsg, int entryCode, boolean hasButton, boolean hasResultMsgModule) {
  	  initPromptModule(promptMsg,1,entryCode,RECT_W-MARGIN,hasButton,hasResultMsgModule,COLOR_BLUE,1,false);
   }
   
   public void initPromptModule(String promptMsg, int entryCode, boolean hasButton, boolean hasResultMsgModule, int numOfResultMsgLines) {
   	  initPromptModule(promptMsg,1,entryCode,RECT_W-MARGIN,hasButton,hasResultMsgModule,COLOR_BLUE,numOfResultMsgLines,false);
    }
   
   public void initPromptModule(String promptMsg, int numOfMsgLines, int entryCode, boolean hasButton, boolean hasResultMsgModule,boolean promptShareButtonLine) {
   	  initPromptModule(promptMsg,numOfMsgLines,entryCode,RECT_W-MARGIN,hasButton,hasResultMsgModule,COLOR_BLUE,1,promptShareButtonLine);
   }
   
   public void initPromptModule(String promptMsg, int numOfMsgLines, int entryCode, boolean hasButton, boolean hasResultMsgModule,boolean promptShareButtonLine, int numOfResultMsgLines) {
 	  initPromptModule(promptMsg,numOfMsgLines,entryCode,RECT_W-MARGIN,hasButton,hasResultMsgModule,COLOR_BLUE,numOfResultMsgLines,promptShareButtonLine);
 }   
   
   protected void updatePromptMsg(String color, String format, Object... args) {
   	if( !color.isEmpty() )
   		background.setFill(color);
		promptText.wrap(format, args);
		String message = String.format(format,args);
		inform("update msg: %s", message);
	}
   
   protected void updatePromptMsg(boolean showMsg, String color, String format, Object... args) {
   	if( !color.isEmpty() )
   		background.setFill(color);
		promptText.wrap(format, args);
		String message = String.format(format,args);
		if( showMsg )
			inform("update msg: %s", message);
	}
     
   
   public void inputDecision( String text ) {
   }
   
   
   // result msg module
   
   public void initResultMsgModule(int y, int numOfResultMsgLines) {
      resultMsgText = new TextWrap(MARGIN, y, SCREEN_WIDTH-MARGIN*2, FONT_L, numOfResultMsgLines);
      resultMsgText.wrap("");
      resultMsgRect = new Rectangle(MARGIN_SMALL,y,SCREEN_WIDTH-MARGIN,RECT_TEXT_HEIGHT*numOfResultMsgLines,COLOR_RED,1,"black",false);
   }   
   
   public void showResultMsgModule(String resultMsg) {
      resultMsgText.wrap(resultMsg);
      resultMsgRect.show();
      resultMsgText.show();
      //cycleCount = true;
   }
   
   public void showResultMsgModule(String format, Object... args) {
 	  	String resultMsg = String.format(format, args);
 	  	showResultMsgModule(resultMsg);
   }  
   
   public void showAlertMsg(String format, Object... args) {
 	  	String resultMsg = String.format(format, args);
   	alert(resultMsg);
 	   resultMsgRect.updateFill(COLOR_RED);
 	  	showResultMsgModule(resultMsg);
   }
   
   public void showActionMsg(String format, Object... args) {
 	  	String resultMsg = String.format(format, args);
 	  	inform(resultMsg);
 	   resultMsgRect.updateFill(COLOR_YELLOW);
 	  	showResultMsgModule(resultMsg);
   }  
   
   public void showSuccessMsg(String format, Object... args) {
 	  	String resultMsg = String.format(format, args);
 	   inform(resultMsg);
 	  	resultMsgRect.updateFill(COLOR_GREEN);
 	  	showResultMsgModule(resultMsg);
   }   
   
   public void hideResultMsgModule() {
   	resultMsgText.wrap("");
   	resultMsgRect.hide();
   }  
   
   // scroll box module
   public void initScrollBoxModule(boolean drawHeader, int indexJump, int defaultRowNumber ) {
      int y = INFO_Y + FONT_M*(numOfInfoRows+1) + 10;
      scrollBoxModel = new ScrollBoxConstructor(0, y);
      scrollBoxModel.setFont(40,40);
      scrollBoxModel.setMargins(0, 10, 0, 0);
      scrollBoxModel.setRows(defaultRowNumber+3-numOfInfoRows-numOfBottomRows, 5);
      scrollBoxModel.setWidth(SCREEN_WIDTH);
      scrollBoxModel.setButton(30, 60, 15);
      scrollBoxModel.setIndexJump(indexJump);
      scrollBoxModel.drawHeaders(drawHeader);
   }
   
   public void initScrollBoxModule(boolean drawHeader) {
   	initScrollBoxModule(drawHeader,1,11);
   }
   
   public void initScrollBoxModule() {
   	initScrollBoxModule(true,1,11);
   }
   
   // infobox module
   
   protected void initInfoBox() {
      infoBoxModel = new InfoBoxConstructor(0,INFO_Y);
      infoBoxModel.setFont(60, 50);
      infoBoxModel.setWidth(W1_2);
      infoBoxModel.setWidthRatio(0.5);
      infoBoxModel.setMargins(0, 0, 50, 50);
      infoBoxModel.setRows(4, 10);
      leftInfoBox = infoBoxModel.build();
      infoBoxModel.setOrigin(W1_2, INFO_Y);
      rightInfoBox = infoBoxModel.build();
      initInfoBoxLines(false);
   }
   
   protected void initInfoBoxLines( boolean show ) {
      initLines();
      showLines(show);
   }

   protected void initLines() {
      if (infoBoxLines != null)
         return;
      int t = 5;
      int y1 = INFO_Y;
      int y2 = INFO_Y + FONT_M*numOfInfoRows + 10;
      infoBoxLines = new TermGroup(0,0);
      infoBoxLines.add(horizontalLine(y1, t));
      infoBoxLines.add(horizontalLine(y2, t));
      infoBoxLines.add(verticalLine(y1, y2, W1_2,t));
   }

   protected void showLines(boolean show) {
      if (show)
      	infoBoxLines.show();
      else
      	infoBoxLines.hide();
   }
   
   protected void setInfoBox() {
   	setInfoBox(true);
   }
   
   protected void setInfoBox(boolean showLines) {
      showLines(showLines);
      setLeftInfoBox();
      setRightInfoBox();
   }

   protected void setLeftInfoBox() {
      if (leftInfoBox==null)
         return;
      leftInfoBox.updateInfoPair(0, "Column1", "value1" );
      leftInfoBox.show(); 
   }

   protected void setRightInfoBox() {
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Column2", "value2");
	   rightInfoBox.show(); 
   }
   
   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      if( !getOperatorId().isEmpty() &&  lastActivity > 0 && System.currentTimeMillis() - lastActivity > logout ) {
      	processLogout();
      	inform("auto logout");
      }
      if (resetTempDisplay())
         resetDisplay();
      handleScan();
      handleScale();
      
   }
   
   /** Processes a button press. */
   public void handleButton( int tag ) {
      try {
         TermUtility.getButtons().getBoolean("doAction", tag);
      } catch ( NullPointerException ex ) {
      } catch ( Exception ex ) {
         alert(ex);
      }
   }

   /** Processes text from an entry field. */
   public void handleEntry( int tag, String text ) {
      try {
         TermUtility.getTextEntries().getBoolean("doAction", tag, text);
      } catch ( NullPointerException ex ) {
      } catch ( Exception ex ) {
         alert(ex);
      }   

      try {
         TermUtility.getPasswordEntries().getBoolean("doAction", tag, text);
      } catch ( NullPointerException ex ) {
      } catch ( Exception ex ) {
         alert(ex);
      } 
   }
   
   
   
   // printer functions
   
   protected void clearAllPrintQueue() {
      clearPrintQueue(printer);
      clearPrintQueue(labeler);
   }
   
   protected void clearPrintQueue(String printer) {
      if (printer != null && !printer.isEmpty())
      db.execute("UPDATE docRequest SET status = 'done' " +
            "WHERE status = 'idle' " +
            "AND printer = '%s'", printer);
   }

   protected int getIntControl(String ctl) {
      String str = db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='tm-pk1' AND name='%s'",host,ctl);
      int num = -1;
      try {
         num = Integer.parseInt(str);
      } catch(Exception e) {}
      return num;
   }

   protected String getControl(String ctl) {
      return db.getString("","SELECT value FROM controls WHERE host='%s' AND zone='tm-pk1' AND name='%s'",host,ctl);
   }
   
   // process Bluetooth scan
   
   private void handleScan() {
      String result = getScan();
      if ( result.isEmpty() ) 
         return;
      initActivityTimer();
      if ( BARCODE_LOGOUT.equals(result) ) {
         processLogout();
      } else if ( BARCODE_CANCEL.equals(result) ) {
         doCancel();
      } else if ( BARCODE_COMPLETE.equals(result) ) {
         doComplete();      
      } else if ( BARCODE_OK.equals(result) ) {
         doOkay();  
      } else if ( BARCODE_CONFIRM.equals(result) ) {
         doConfirm();  
      } else if ( BARCODE_SKIP.equals(result) ) {
         doSkip();  
      } else if ( BARCODE_REPRINT.equals(result) ) {
         doReprint();
      } else if ( BARCODE_BACK.equals(result) ) {
         doBack();
      } else {
         if (result.startsWith("]C1"))
            result = result.substring(8, result.length() - 1);
         processScan(result);
      }
   }
   

   protected boolean manualOverrideScan(String scan) {
      return BARCODE_OVERRIDE.equals(scan);
   }
   
   protected void doCancel() {
      inform( "scan [%s] ignored", BARCODE_CANCEL);
   }

   protected OnActionListener cancelAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doCancel();
         }
      };
   }
   
   protected void doComplete() {
      inform( "scan [%s] ignored", BARCODE_COMPLETE);
   }
   
   protected OnActionListener completeAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doComplete();
         }
      };
   }
   
   protected void doOkay() {
      inform( "scan [%s] ignored", BARCODE_OK);
   }
   
   protected OnActionListener okAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doOkay();
         }
      };
   }
   
   protected void doConfirm() {
      inform( "scan [%s] ignored", BARCODE_CONFIRM);
   }
   
   protected OnActionListener confirmAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doConfirm();
         }
      };
   }
   
   protected void doSkip() {
      inform( "scan [%s] ignored", BARCODE_SKIP);
   }
   
   protected OnActionListener skipAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doSkip();
         }
      };
   }
   
   protected void doReprint() {
      inform( "scan [%s] ignored", BARCODE_REPRINT);
   }
   
   protected OnActionListener reprintAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            doReprint();
         }
      };
   }
   
   protected void doBack() {
      inform( "scan [%s] ignored", BARCODE_BACK);
   }

   protected OnActionListener modeAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            inform("switch station mode");
            setNextScreen("ChangeModeScreen");
         }
      };
   }
   
   protected void processScan(String scan) {
      inform( "scan ignored [%s]", scan );
   }
      
   private String getScan() {
      if (rScanner == null || rScanner.isEmpty() )
         return "";
      String result = db.getRuntime( rScanner );
      if ( result == null || result.isEmpty() ) 
         return "";
      trace( "[%s] scan [%s]", rScanner, result );
      clearScan();
      return result;
   }
   
   protected void clearScan() {
      if (rScanner == null || rScanner.isEmpty() )
         return;
      db.setRuntime( rScanner, "");
   }

   protected void setScannerMode(String mode){
      db.setRuntime(rScanner + "-mode", mode);
   }
   
   // process USB scale
   
   private void handleScale() {
      String result = getScale();
      if ( result.isEmpty() ) 
         return;
      
      double scaleWeight = RDSUtil.stringToDouble(result, 0.00);
      double weight = scaleWeight * weightConversion;
      if (weight > MAX_WEIGHT)
         return;
      
      processScale(weight);
   }

   protected void processScale(double weight) {
      inform( "scale ignored [%.2f]", weight );
   }
      
   protected boolean scaleRuntimeDefined() {
      return rScale != null && !rScale.isEmpty();
   }
   
   protected String getScale() {
      if (!scaleRuntimeDefined() )
         return "";
      String result = db.getRuntime( rScale );
      if ( result == null || result.isEmpty() ) 
         return "";
      trace( "usb scale [%s]", result );
      clearScale();
      return result;
   }
   
   protected void triggerScale() {
      clearScale();
   }
   
   protected void clearScale() {
      if (rScale == null || rScale.isEmpty() )
         return;
      db.setRuntime( rScale, "");
   }
   
   /*
    * box carton
    */
   
   protected int getBoxCarton(String zone) {
      int box = RDSTrak.read( zone, REG_BOX );
      if (box == BOX_NONE) 
         return BOX_NONE;

      RDSTrak.write( zone, REG_BOX, BOX_NONE );
      return box;
   }
   
   protected Map<String,String> getZoneRecordByBarcode(String prefix, String barcode) {
      Map<String,String> map = db.getRecordMap(
            "SELECT * FROM `%sCartons` " +
            "WHERE barcode = '%s' " +
            "ORDER  BY seq DESC", prefix, barcode);
      return map;
   }
   
   protected Map<String,String> getZoneRecordByBox(String prefix, int box) {
      Map<String,String> map = db.getRecordMap(
            "SELECT * FROM `conveyorBoxes` " +
            "WHERE box = %d " +
            "AND area='%s' " +
            "ORDER BY seq DESC", box, prefix);
      return map;
   }
   
   protected int setZoneCarton(String prefix, int seq, String name, Object value) {
      String sql = String.format("UPDATE `%sCartons` SET `%s` = ? " +
            "WHERE seq = %d", prefix, name, seq);
      return db.executePreparedStatement(sql, RDSDatabase.convertValue( value ));
   }
   
   protected int setZoneCartonStamp(String prefix, int seq, String name) {
      String sql = String.format("UPDATE `%sCartons` SET `%s` = NOW() " +
            "WHERE seq = %d", prefix, name, seq);
      return db.execute("%s",sql);
   }
   
   /*
    * processing methods
    */
   
   /*
    * display helpers
    */
   
   protected void initStartTimer() {
   	start = System.currentTimeMillis();
   }
   
   protected void initProcessTimer() {
   	processing = System.currentTimeMillis();
   }
   
   protected void initActivityTimer() {
   	inform("init last activity");
   	lastActivity = System.currentTimeMillis();
   }   
   
   protected String reqMsg(boolean req) {
      if (req)
         return "Required";
      return "Not Required";
   }
   
   protected String getPacklistMsg(boolean req, boolean printed, int count) {
      if (!req)
         return "Not required";
      if (count==0)
         return "Waiting";
      if (count==1)
         return "1 doc";
      return String.format("%d docs", count); 
   }

   protected String getLabelMsg(boolean req, int count) {
      if (!req)
         return "Not required";
      if (count==0)
         return "Waiting";
      if (count==1)
         return "1 label";
      return String.format("%d labels", count); 
   }
   
   protected void initRuntime( String name, String value ) {
      if (name == null || name.isEmpty())
         return;
      if (value == null )
         value = "";
      db.execute("INSERT IGNORE INTO runtime SET " +
            "name = '%s', " +
            "value = '%s'", name, value);
   }
   
   protected TextField initScanField() {
      TextField field = new TextField(MSG_MARGIN, MSG_SCAN_Y, FONT_L,"");
      field.show();
      return field;
   }
   
   protected TextWrap initMsg() {
      TextWrap msg = new TextWrap(MSG_MARGIN, MSG_Y, 1750, FONT_L, 2);
      msg.show();
      return msg;
   }
   
   protected void setTempDisplayDuration( long duration ) {
      tempDispStdDuration = duration;
   }
   
   protected void triggerTempDisplay() {
      triggerTempDisplay(tempDispStdDuration);
   }
   
   protected void clearTempTrigger() {
      tempDisplaySet = 0;
      tempDisplayDuration = tempDispStdDuration;
   }
   
   protected boolean tempDisplayTriggered() {
      return tempDisplaySet > 0;
   }
   
   protected void triggerTempDisplay( long duration ) {
      tempDisplayDuration = duration;
      tempDisplaySet = System.currentTimeMillis();
   }
   
   private boolean resetTempDisplay() {
      if (tempDisplaySet <= 0)
         return false;
      if (System.currentTimeMillis() - tempDisplaySet < tempDisplayDuration)
         return false;
         
      clearTempTrigger();
      return true;
   }
   
   protected void resetDisplay() {
   }
   
   protected FaultEvent trakDpFault(String monitor, String reset) {
      FaultEvent event = new FaultEvent();
      FaultEvent.FaultAction trigger = new FaultEvent.FaultAction() {
         @Override
         public boolean action() {
            int get = RDSTrak.read(monitor);
            return get > 0;
         }
      };
      FaultEvent.FaultAction fltReset = new FaultEvent.FaultAction() {
         @Override
         public boolean action() {
            RDSTrak.write(reset, 1);
            return true;
         }
      };
      event.setGlobalTrigger( trigger );
      event.setFaultReset( fltReset );
      event.setDelay(5000L);
      
      String msg = db.getString(null, "SELECT description FROM trak WHERE name = '%s'", monitor);
      if (msg == null || msg.isEmpty())
         msg = String.format("Fault Detected: [%s]", monitor);
      event.setMessage(msg);
      return event;
   }
   
   /** An {@code Exception} due to failure during processing. */
   @SuppressWarnings("serial")
   protected static class TerminalException
         extends Exception {
      public TerminalException( String message ) {
         super( message );
      }
      public TerminalException( String message, Throwable cause ) {
         super( message, cause );
      }
   }
   
   public boolean lock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT GET_LOCK( '%s', %d )",
            lockName, 5 );
      if (lockVal != 1) {
         String connId = db.getString( "",
               "SELECT IS_USED_LOCK( '%s' )", lockName );
         alert( "unable to get database lock for '%s', in use by id %s, operatorId %s",
               lockName, connId, getOperatorId() );
      }
      trace( "%s: get lock return %d",getOperatorId(),lockVal);
      return (lockVal == 1);
   }

   /** Releases a lock with the specified name. */
   public boolean unlock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT RELEASE_LOCK( '%s' )",
            lockName );
      trace( "%s: release lock return %d",getOperatorId(),lockVal);      
      return (lockVal == 1);
   } 

}

