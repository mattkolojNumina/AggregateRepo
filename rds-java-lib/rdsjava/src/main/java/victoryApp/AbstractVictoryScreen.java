package victoryApp;

import rds.*;
import victoryApp.gui.*;
import static victoryApp.gui.GUIConstants.*;
import java.util.*;

public abstract class AbstractVictoryScreen extends Screen {

  protected Map<String,String> screenInfo;

  GUIRectangle alertRectangle;
  GUIText alertText;
  int alertDuration;
  GUIText headerText;
  GUIRectangle headerRectangle;

  protected GUIRectangle promptBox;
  protected GUIText promptMsg;
  protected String promptMsgStr;
  protected GUIEntry promptEntry;
  protected String promptEntryHint;
  protected List<GUIButton> promptButtons;
  int buttonTag;

  public AbstractVictoryScreen(VictoryApp app) {
    super(app);
  }

  public AbstractVictoryScreen(VictoryApp app, String background) {
    super(app, background);
  }

  /**
   * Initializes the screen by setting up necessary components and configurations.
   * @return True
   */
  public boolean handleInit() {
    super.handleInit();
    alertDuration = 0;
    screenInfo = getScreenInfo();
    buttonTag = BUTTON_TAG;
    setPromptBoxDimensions(PROMPT_BOX_X, PROMPT_BOX_Y, PROMPT_BOX_WIDTH, PROMPT_BOX_HEIGHT);
    return true;
  }

  /**
   * Sets the dimensions of the prompt box (blue box).
   * Must be used in {@link #handleInit} for the function to have any effect
   * @param x X-coordinate
   * @param y Y-coordinate
   * @param w width of box
   * @param h height of box
   */
  public void setPromptBoxDimensions(int x, int y, int w, int h) {
    if(promptBox==null) {
      promptBox = addRectangle(PROMPT_RECT_TAG,
            x, y, -0.1f,w,h, 
            LIGHT_BLUE, 0, BLACK
      );
    } else {
      promptBox.x=x; promptBox.y=y; promptBox.width=w; promptBox.height=h;
    }

    if(promptMsg==null) {
      promptMsgStr = replaceParams(interpretPhrase(screenInfo.get("promptText")),false);
      if(exists(promptMsgStr)) {
        promptMsg= addText(PROMPT_TEXT_TAG,
            2*x, y, 0.9f,
            DEFAULT_TEXT_SIZE, BLACK, promptMsgStr,
            false, false
        );
      }
    } else {
      promptMsg.x=2*x; promptMsg.y=y;
    }

    if(screenInfo.get("textEntryType").equals("none")) {
      return;
    }
    else if(promptEntry==null && !screenInfo.get("textEntryType").equals("none")) {
      promptEntryHint = replaceParams(interpretPhrase(screenInfo.get("textEntryHint")),false);
      boolean useNumeric = screenInfo.get("textEntryType").equals("numeric");
      promptEntry = addEntry(PROMPT_ENTRY_TAG,
          2*x, promptBox.y + promptBox.height/2 -25, 0.9f,
          promptBox.width/3, promptBox.height/2,
          DEFAULT_TEXT_ENTRY_TEXT_SIZE,
          "", promptEntryHint,
          DEFAULT_TEXT_ENTRY_TEXT_COLOR, DEFAULT_TEXT_ENTRY_BACKGROUND,
          DEFAULT_TEXT_ENTRY_ACCENT_COLOR,
          useNumeric
      );
    } else {
      promptEntry.x=2*x; promptEntry.y=promptBox.y+promptBox.height/2;
      promptEntry.width=promptBox.width/3; promptEntry.height=promptBox.height/2-25;
    }
  }

  /**
   * Overrides handleTick to clear alert popups.
   */
  public boolean handleTick() {
    super.handleTick();
    if(alertDuration > 0) {
      if(--alertDuration == 0) clearAlert();
    }
    return true;
  }

  //alert methods

  /**
   * Sets an error message to be displayed above the prompt box.
   * @param msg Error message to display.
   */
  public void setError(String msg) {
    setError(DEFAULT_ALERT_DURATION, ABOVE_PROMPT_BOX_Y, msg);
  }

  /**
   * Sets an error message to be displayed above the prompt box.
   * @param format Formatted string of Error message to display.
   * @param args Message arguments.
   */
  public void setError(String format, Object... args) {
    setError(DEFAULT_ALERT_DURATION, ABOVE_PROMPT_BOX_Y, String.format(format, args));
  }

  /**
   * Sets an error message to be displayed above the prompt box.
   * @param y Y-coordinate
   * @param msg Error message to display.
   */
  public void setError(int y, String msg) {
    setError(DEFAULT_ALERT_DURATION, y, msg);
  }

  /**
   * Sets an error message to be displayed above the prompt box.
   * @param duration number of ticks to show alert (1 tick = 100 msec)
   * @param y Y-coordinate
   * @param msg Error message to display.
   */
  public void setError(int duration, int y, String msg) {
    alertDuration = duration;
    alertRectangle = addRectangle(ALERT_RECT_TAG,
        PROMPT_BOX_X, y, -0.1f,
        PROMPT_BOX_WIDTH, 0 + (int) (2*PIXELS_PER_CHARACTER_SIZE_HEIGHT * DEFAULT_TEXT_SIZE), 
        ERROR_RED, 0, BLACK
    );
    alertText = addText(ALERT_TEXT_TAG,
        PROMPT_BOX_X + GUI_PADDING/2, y, 0.0f,
        DEFAULT_TEXT_SIZE, WHITE, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateRectangle(alertRectangle);
    app.updateText(alertText);
  }

  /**
   * Sets a warning message to be displayed above the prompt box.
   * @param msg Warning message to display.
   */
  public void setWarning(String msg) {
    setWarning(DEFAULT_ALERT_DURATION, ABOVE_PROMPT_BOX_Y, msg);
  }

  /**
   * Sets a warning message to be displayed above the prompt box.
   * @param format Formatted string of Warning message to display.
   * @param args Message arguments.
   */
  public void setWarning(String format, Object... args) {
    setWarning(DEFAULT_ALERT_DURATION, ABOVE_PROMPT_BOX_Y, String.format(format, args));
  }

  /**
   * Sets a warning message to be displayed above the prompt box.
   * @param y Y-coordinate
   * @param msg Warning message to display.
   */
  public void setWarning(int y, String msg) {
    setWarning(DEFAULT_ALERT_DURATION, y, msg);
  }

  /**
   * Sets a warning message to be displayed above the prompt box.
   * @param duration number of ticks to show alert (1 tick = 100 msec)
   * @param y Y-coordinate
   * @param msg Warning message to display.
   */
  public void setWarning(int duration, int y, String msg) {
    alertDuration = duration;
    alertRectangle = addRectangle(ALERT_RECT_TAG,
        PROMPT_BOX_X, y, -0.1f,
        PROMPT_BOX_WIDTH, 0 + (int) (2*PIXELS_PER_CHARACTER_SIZE_HEIGHT * DEFAULT_TEXT_SIZE), 
        WARNING_YELLOW, 0, BLACK
    );
    alertText = addText(ALERT_TEXT_TAG,
        PROMPT_BOX_X + GUI_PADDING/2, y, 0.0f,
        DEFAULT_TEXT_SIZE, BLACK, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateRectangle(alertRectangle);
    app.updateText(alertText);
  }

  /**
   * Sets a general message to be displayed above the prompt box.
   * @param msg General message to display.
   */
  public void setMessage(String msg) {
    setMessage(DEFAULT_ALERT_DURATION, ABOVE_PROMPT_BOX_Y, msg);
  }

  /**
   * Sets a general message to be displayed above the prompt box.
   * @param y Y-coordinate
   * @param msg General message to display.
   */
  public void setMessage(int y, String msg) {
    setMessage(DEFAULT_ALERT_DURATION, y, msg);
  }

  /**
   * Sets a general message to be displayed above the prompt box.
   * @param duration number of ticks to show alert (1 tick = 100 msec)
   * @param y Y-coordinate
   * @param msg General message to display.
   */
  public void setMessage(int duration, int y, String msg) {
    alertDuration = duration;
    alertRectangle = addRectangle(ALERT_RECT_TAG,
        PROMPT_BOX_X, y, -0.1f,
        PROMPT_BOX_WIDTH, (int) DEFAULT_TEXT_SIZE-130, 
        WHITE, 0, BLACK
    );
    alertText = addText(ALERT_TEXT_TAG,
        PROMPT_BOX_X + GUI_PADDING/2, y, 0.0f,
        DEFAULT_TEXT_SIZE, BLACK, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateRectangle(alertRectangle);
    app.updateText(alertText);
  }

  /**
   * Sets a header message to be displayed at the top of the screen.
   * @param msg Header message to display.
   */
  public void setHeader(String msg) {
    headerText = addText(INFO1_HEADER_TAG,
        GRID_TXT_X, GRID_TXT_Y, 0.0f,
        DEFAULT_TEXT_SIZE, BLACK, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateText(headerText);
  }

  /**
   * Sets a header message to be displayed at the top of the screen.
   * @param msg Header message to display.
   */
  public void setHeader(String msg, String color) {
    headerRectangle = addRectangle(HEADER_RECT_TAG,
        PROMPT_BOX_X, GRID_TXT_Y, -0.1f,
        PROMPT_BOX_WIDTH, (int) DEFAULT_TEXT_SIZE-130, 
        color, 0, BLACK
    );
    setHeader(PROMPT_BOX_X + GUI_PADDING/2, GRID_TXT_Y, msg);
  }

  /**
   * Sets a header message to be displayed at the top of the screen.
   * @param y Y-coordinate
   * @param msg Header message to display.
   * @param color Color of header background rectangle.
   */
  public void setHeader(int y, String msg, String color) {
    headerRectangle = addRectangle(HEADER_RECT_TAG,
        PROMPT_BOX_X, GRID_TXT_Y, -0.1f,
        PROMPT_BOX_WIDTH, (int) DEFAULT_TEXT_SIZE-130, 
        color, 0, BLACK
    );
    setHeader(PROMPT_BOX_X + GUI_PADDING/2, y, msg);
  }

  /**
   * Sets a header message to be displayed at the top of the screen.
   * @param format Formatted string of Header message to display.
   * @param args Message arguments.
   */
  public void setHeader(String format, Object... args) {
    String msg = String.format(format, args);
    headerText = addText(INFO1_HEADER_TAG,
        GRID_TXT_X, GRID_TXT_Y, 0.0f,
        DEFAULT_TEXT_SIZE, BLACK, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateText(headerText);
  }

  /**
   * Sets a header message to be displayed at the top of the screen.
   * @param x X-coordinate
   * @param y Y-coordinate
   * @param msg Header message to display.
   */
  public void setHeader(int x, int y, String msg) {
    headerText = addText(INFO1_HEADER_TAG,
        x, y, 0.0f,
        DEFAULT_TEXT_SIZE, BLACK, replaceParams(interpretPhrase(msg),false),
        false, false
    );
    app.updateText(headerText);
  }

  /** Clears error message above prompt box from screen */
  public void clearError() {
    clearAlert();
  }

  /** Clears warning message above prompt box from screen */
  public void clearWarning() {
    clearAlert();
  } 

  /** Clears message above prompt box from screen */
  public void clearMessage() {
    clearAlert();
  } 

  /** Clears all alerts/warnings/errors above prompt box from screen */
  public void clearAlert() {
    deleteRectangle(alertRectangle);
    deleteText(alertText);
    alertRectangle=null;
    alertText=null;
    alertDuration = 0;
  }

  /**
   * Adds a button to the prompt box.
   * @param buttonType Type of the button to add.
   */
  public void addButton(String buttonType) {
    GUIButton b = null;
    if(promptButtons==null) promptButtons = new ArrayList<GUIButton>();
    int x_0 = promptBox.x + promptBox.width/2;
    int x_1 = x_0;
    if (promptEntry != null) x_1 = promptEntry.x + promptEntry.width;
    int x = Math.max(x_0, x_1);
    int y = 20 + promptBox.y;
    if(promptButtons.size()%2==1) x += (5+BUTTON_WIDTH);
    if(promptButtons.size()/2>0) y += promptBox.height/2;    
    switch(buttonType) {
      case CANCEL: b = addCancelButton(x,y);break;
      case EXCEPTION: b = addExceptionButton(x,y);break;
      case START_NEW: b = addStartNewButton(x,y);break;
      case START: b = addStartButton(x,y);break;
      case SHORT: b = addShortButton(x,y);break;
      case ADD: b = addAddButton(x,y);break;
      case CHANGE: b = addChangeButton(x,y);break;
      case SPLIT: b = addSplitButton(x,y);break;
      case JOIN: b = addJoinButton(x,y);break;
      case FULL: b = addFullButton(x,y);break;
      case NO_SCAN: b = addNoScanButton(x,y);break;
      case SKIP: b = addSkipButton(x,y);break;
      case CONFIRM: b = addConfirmButton(x,y);break;
      case UNDO: b = addUndoButton(x,y);break;
      case MOVE_CARTON: b = addMoveCartonButton(x,y);break;
      case MOVE_PALLET: b = addMovePalletButton(x,y);break;
      case CLOSE: b = addCloseButton(x,y);break;
      case CHANGE_TASK: b = addChangeTaskButton(x,y);break;
      case CHANGE_AREA: b = addChangeAreaButton(x,y);break;
      case REPRINT: b = addReprintButton(x, y);break;
      case OVERRIDE: b = addOverrideButton(x, y);break;
      default:  if(exists(buttonType)) {
                  b = addButton(buttonTag++,x,y,BUTTON_WIDTH,buttonType);
                } 
                break;
    }
    promptButtons.add(b);
  }

  /**
   * Adds multiple buttons to the prompt box based on the provided types.
   * @param buttonTypes Types of the buttons to add.
   */
  public void addButtons(String... buttonTypes) {
    for(String buttonType : buttonTypes) {
      addButton(buttonType);
    }
  }

  /**
   * Adds multiple buttons to the prompt box based on the provided list of types.
   * @param buttons List of button types to add.
   */
  public void addButtons(List<String> buttons) {
    for (String button : buttons) {
      addButton(button);
    }
  }

  /** Gets a value from a {@code Map} or an empty string. */
  protected static String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String v = m.get( name );
      return (v == null) ? "" : v;
   }

  /** Gets a value from a {@code Map} and converts it to an int. */
  protected static int getMapInt( Map<String,String> m, String name ) {
    if (m == null)
        return -1;
    return RDSUtil.stringToInt( m.get( name ), -1 );
  }

  /** Gets a value from a {@code Map} and converts it to a double. */
  protected static double getMapDbl( Map<String,String> m, String name ) {
    if (m == null)
        return 0.0;
    return RDSUtil.stringToDouble( m.get( name ), 0.0 );
  }

  //Overriden handle____ methods

  /**
   * Overriding handleVoice to go into the generic handleInput method.
   */
  @Override
  public boolean handleVoice(String text) {
    if(super.handleVoice(text)) return true;
    return handleInput(text);
  }

  /**
   * Overriding handleText to go into the generic handleInput method.
   */
  @Override
  public boolean handleText(int tag, String text) {
    super.handleText(tag, text);
    if(tag == PROMPT_ENTRY_TAG) {
      return handleInput(text);
    }
    return false;
  }

  /**
   * Overriding handleScan to go into the generic handleInput method.
   */
  @Override
  public boolean handleScan(String text) {
    super.handleScan(text);
    return handleInput(text);
  }

  /**
   * Generic input method that will handle both text and scans by default. 
   * Can be extended to also handle voice input as well, but needs to be done 
   * manually.
   * @param text input
   * @return false
   */
  public boolean handleInput(String text) {
    alert("please override handleInput() in your screen class");
    return false;
  }

}
