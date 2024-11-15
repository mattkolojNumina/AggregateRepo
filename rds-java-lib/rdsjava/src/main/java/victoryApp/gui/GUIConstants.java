package victoryApp.gui;

import java.util.regex.*;

public class 
GUIConstants 
  {

    /*reserved tags*/
    //7600-7899: info box
    public static final int LEFT_BOX_TAG = 7600;
    public static final int RIGHT_BOX_TAG = 7601;
    public static final int INFO_BOX_START_TAG = 7610;
    //7900-7999: login screen
    public static final int LOGIN_ENTRY_TAG = 7900;
    //8000-8999: putwall
    public static final int PUTWALL_SCROLL_LEFT_BUTTON = 8000;
    public static final int PUTWALL_SCROLL_RIGHT_BUTTON = 8001;
    public static final int PUTWALL_SCROLL_BAR_BACKGROUND = 8002;
    public static final int PUTWALL_SCROLL_BAR = 8003;
    public static final int PUTWALL_START_TAG = 8004;
    //9000-9099: title
    public static final int TITLE_TEXT_TAG = 9000;
    public static final int SSI_TAG = 9001; //signal strength indicator circle
    public static final int LISTENING_TAG = 9002; //listening indicator
    //9100-9199: footer
    public static final int FOOTER_BOX_TAG = 9100;
    public static final int OPERATOR_ID_TAG = 9101;
    public static final int LOGOUT_BUTTON_TAG = 9102;
    public static final int SEND_MESSAGE_BUTTON_TAG = 9103;
    public static final int LAST_RESPONSE_TAG = 9104;
    //9200-9699: table
    public static final int SCROLL_UP_BUTTON = 9200;
    public static final int SCROLL_DOWN_BUTTON = 9201;
    public static final int TABLE_START_TAG = 9202;
    //9700-9999: generic UI elements
    public static final int START_RECT_TAG = 9700;
    public static final int DTL_RECT_TAG = 9701;
    public static final int ERROR_MSG_RECT_TAG = 9702;
    public static final int ERROR_MSG_TEXT_TAG = 9703;
    public static final int PROMPT_RECT_TAG = 9704;
    public static final int PROMPT_TEXT_TAG = 9705;
    public static final int PROMPT_ENTRY_TAG = 9706;
    public static final int ALERT_RECT_TAG = 9707;
    public static final int ALERT_TEXT_TAG = 9708;
    public static final int HEADER_RECT_TAG = 9709;

    //"Header" tag values for a 2x2 header and value pair
    public static final int INFO1_HEADER_TAG = 9310;
    public static final int INFO2_HEADER_TAG = 9311;
    public static final int INFO3_HEADER_TAG = 9312;
    public static final int INFO4_HEADER_TAG = 9313;
    public static final int INFO1_VALUE_TAG = 9320;
    public static final int INFO2_VALUE_TAG = 9321;
    public static final int INFO3_VALUE_TAG = 9322; 
    public static final int INFO4_VALUE_TAG = 9323;  

    //Tag that can be iterated for "custom" buttons in addButtons(List<String>)
    public static int BUTTON_TAG = 9500;

    //10000-10100: buttons that substitute for voice commands
    public static final int CANCEL_BUTTON_TAG = 10001;
    public static final int START_NEW_BUTTON_TAG = 10002;
    public static final int SHORT_BUTTON_TAG = 10003;
    public static final int ADD_BUTTON_TAG = 10004;
    public static final int CHANGE_BUTTON_TAG = 10005;
    public static final int SPLIT_BUTTON_TAG = 10006;
    public static final int JOIN_BUTTON_TAG = 10007;
    public static final int LOCATION_BUTTON_TAG = 10008;
    public static final int FULL_BUTTON_TAG = 10009;
    public static final int NO_SCAN_BUTTON_TAG = 10010;
    public static final int SKIP_BUTTON_TAG = 10011;
    public static final int CONFIRM_BUTTON_TAG = 10012;
    public static final int UNDO_BUTTON_TAG = 10013;
    public static final int MOVE_CARTON_BUTTON_TAG = 10014;
    public static final int MOVE_PALLET_BUTTON_TAG = 10015;
    public static final int CLOSE_BUTTON_TAG = 10016;
    public static final int REPRINT_BUTTON_TAG = 10017;
    public static final int OVERRIDE_BUTTON_TAG = 10018;
    public static final int EXCEPTION_BUTTON_TAG = 10019;
    public static final int CHANGE_TASK_BUTTON_TAG = 10020;
    public static final int TASK_ONE_BUTTON_TAG = 10021;
    public static final int TASK_TWO_BUTTON_TAG = 10022;
    public static final int TASK_THREE_BUTTON_TAG = 10023;
    public static final int TASK_FOUR_BUTTON_TAG = 10024;
    public static final int TASK_FIVE_BUTTON_TAG = 10025;
    public static final int TASK_SIX_BUTTON_TAG = 10026;
    public static final int TASK_SEVEN_BUTTON_TAG = 10027;
    public static final int TASK_EIGHT_BUTTON_TAG = 10028;
    public static final int TASK_NINE_BUTTON_TAG = 10029;
    public static final int CHANGE_LOCATION_BUTTON_TAG = 10030;
    public static final int LOCATION_ONE_BUTTON_TAG = 10031;
    public static final int LOCATION_TWO_BUTTON_TAG = 10032;
    public static final int LOCATION_THREE_BUTTON_TAG = 10033;
    public static final int LOCATION_FOUR_BUTTON_TAG = 10034;
    public static final int LOCATION_FIVE_BUTTON_TAG = 10035;
    public static final int LOCATION_SIX_BUTTON_TAG = 10036;
    public static final int LOCATION_SEVEN_BUTTON_TAG = 10037;
    public static final int LOCATION_EIGHT_BUTTON_TAG = 10038;
    public static final int LOCATION_NINE_BUTTON_TAG = 10039;
    public static final int CHANGE_AREA_BUTTON_TAG = 10040;

    /*signal strength indicator*/
    public static final int ANNOUNCEMENT_REFRESH_TIME = 50; //each increment is roughly 100ms. A value of 50 then would equate to a roughly 5s refresh

    /*connection constants*/
    public static final String DEFAULT_MAX_RECONNECT = "900"; //number of seconds


    /*default speech preferences*/
    public static final String DEFAULT_VOLUME = ".75"; //use a value between 0 and 1
    public static final String DEFAULT_SENSITIVITY = ".35"; //use a value between 0 and 1
    public static final String DEFAULT_RATE = "1.0"; //1.0 is a "normal" talking rate. 2.0 is twice as fast, 0.5 is half as fast...
                                               //2.0 is probably the fastest you would want to go for the speech to still be understandable
    public static final String DEFAULT_PITCH = "1.0"; //1.0 is a normal pitch, >1 is high pitch, <1 is low pitch; 0.5-2.0 would be the most reasonable boundaries
    public static final String DEFAULT_LANGUAGE = "english"; //NOTE: logic is in place for Spanish voice, but Android will use English pronunciation rules

    /*default screen and GUI Elements settings*/
    //screen dimensions
    public static final int SCREEN_WIDTH = 1920; 
    public static final int SCREEN_HEIGHT = 1080;
    public static final double PIXELS_PER_CHARACTER_SIZE = 0.1806;
    public static final double PIXELS_PER_CHARACTER_SIZE_HEIGHT = 0.244;
    
    //timings
    public static final int DEFAULT_ALERT_DURATION = 45;
    public static final double TICKS_PER_SECOND = 9.0;
    
    //colors
    public static final String BLACK           = "FF000000";
    public static final String WHITE           = "FFFFFFFF";
    public static final String RED             = "FFFF0000";
    public static final String BLUE            = "FF0000FF";
    public static final String GREEN           = "FF00FF00";
    public static final String YELLOW          = "FFFFFF00";
    public static final String LIGHT_YELLOW    = "FFFFFFA7";
    public static final String CYAN            = "FF00FFFF";
    public static final String MAGENTA         = "FFFF00FF";
    public static final String PURPLE          = "FF6A0DAD";
    public static final String ORANGE          = "FFFFD700";
    public static final String PINK            = "FFFF9898";
    public static final String LIGHT_BLUE      = "FFC3DCFF";
    public static final String LIGHT_GREEN     = "FF98FF98";
    public static final String BROWN           = "FF654321";
    public static final String LIGHT_GRAY      = "FFC0C0C0";
    public static final String DARK_GRAY       = "FF707070";
    public static final String CARDBOARD       = "FFAD8762";
    
    public static final String WAITING         = LIGHT_YELLOW;
    public static final String ERROR_RED       = "FFFF8F8F";
    public static final String WARNING_YELLOW  = "FFFFF7A1";
    public static final String SUCCESS         = LIGHT_GREEN;

    //commands
    public static final String CANCEL = "cancel";
    public static final String START_NEW = "new";
    public static final String START = "start";
    public static final String NO_SCAN = "noscan";
    public static final String SHORT = "short";
    public static final String ADD = "add";
    public static final String SKIP = "skip";   
    public static final String FULL = "full";
    public static final String CONFIRM = "confirm";
    public static final String MOVE_CARTON = "moveCarton";
    public static final String MOVE_PALLET = "movePallet";
    public static final String UNDO = "undo";
    public static final String CLOSE = "close";
    public static final String JOIN = "join";
    public static final String SPLIT = "split"; 
    public static final String CHANGE = "change";
    public static final String CHANGE_TASK = "changetask";
    public static final String CHANGE_AREA = "changearea";
    public static final String REPRINT = "reprint";
    public static final String OVERRIDE = "override";
    public static final String EXCEPTION = "exception";

    //screen background & text
    public static final String  DEFAULT_BACKGROUND_COLOR = "FFC0C0C0" ;
    public static final String  DEFAULT_TEXT_COLOR = "FF000000" ;
    public static final float   DEFAULT_TEXT_SIZE  = 250f ;
    public static final boolean DEFAULT_IS_BOLD    = false ;
    public static final boolean DEFAULT_IS_ITALIC   = false ;
    public static final int     FOOTER_Y = SCREEN_HEIGHT-160;
    public static final int     GUI_PADDING = 25;

    //prompt box (blue box)
    public static final int PROMPT_BOX_X = 50;
    public static final int PROMPT_BOX_Y = 550;
    public static final int PROMPT_BOX_WIDTH = SCREEN_WIDTH-(2*PROMPT_BOX_X);
    public static final int PROMPT_BOX_HEIGHT = FOOTER_Y - PROMPT_BOX_X - PROMPT_BOX_Y;
    public static final int ABOVE_PROMPT_BOX_Y = PROMPT_BOX_Y - 135; //Y value for boxes (warnings, errors...) above prompt box

    //buttons
    public static final String DEFAULT_BUTTON_BACKGROUND = "FFF0F0F0" ;
    public static final String DEFAULT_BUTTON_TEXT_COLOR  = "FF0000F0" ;
    public static final String DEFAULT_BUTTON_BORDER_COLOR = "FFFFFFFF" ;
    public static final int    DEFAULT_BUTTON_BORDER_SIZE = -1 ;
    public static final float  DEFAULT_BUTTON_TEXT_SIZE = 130f ;
    public static final int    DEFAULT_BUTTON_HEIGHT   = 125 ;
    public static final int    BUTTON_WIDTH = 420;

    //text boxes
    public static final int    DEFAULT_TEXT_ENTRY_HEIGHT = 175 ;
    public static final float  DEFAULT_TEXT_ENTRY_TEXT_SIZE = 150f ;
    public static final String DEFAULT_TEXT_ENTRY_TEXT_COLOR = "FF0000F0" ;
    public static final String DEFAULT_TEXT_ENTRY_BACKGROUND = "FFF0F0F0" ; 
    public static final String DEFAULT_TEXT_ENTRY_ACCENT_COLOR = "FF808000" ;

    //rectangles
    public static final int DTL_RECT_X = 50;
    public static final int DTL_RECT_Y = 415;
    public static final int DTL_RECT_H = 550;
    public static final int DTL_RECT_WIDTH = SCREEN_WIDTH - (DTL_RECT_X * 2);

    //table constants
    public static final float DEFAULT_TITLE_TEXT_SIZE = 400f;
    public static final float DEFAULT_COLUMN_TITLE_TEXT_SIZE = 150f;
    public static final float DEFAULT_TABLE_ENTRY_TEXT_SIZE = 150f; 

    public static final int COLUMN_TITLE_BOX_HEIGHT = (int) DEFAULT_COLUMN_TITLE_TEXT_SIZE;
    public static final int ROW_HEIGHT = (int) DEFAULT_TABLE_ENTRY_TEXT_SIZE;

    //putwall constants
    public static final int   BINS_PER_ROW = 10;
    public static final int   BIN_UNIT_WIDTH = (SCREEN_WIDTH) / BINS_PER_ROW;
    public static final int   BIN_UNIT_HEIGHT = BIN_UNIT_WIDTH;
    public static final float BIN_BUTTON_TEXT_SIZE = 150f;

    //info box constants
    public static final int INFO_BOX_Y = 250;
    public static final int INFO_BOX_WIDTH = SCREEN_WIDTH/2;
    public static final int INFO_BOX_HEIGHT = 200;

    //2x2 header x and y values
    public static final int MARGIN = 50;
    public static final int MARGIN_SMALL = 25;
    public static final int GRID_TXT_X = MARGIN_SMALL*2;
    public static final int GRID_TXT_X2 = SCREEN_WIDTH/2;
    public static final int GRID_TXT_Y = 125;
    public static final int GRID_TXT_Y2 = 200; 
    public static final int GRID_TXT_Y3 = 275; //Y value to add a third row to header

    /*
     * Converts camelCase to TitleCase, does not space out integers
     */
    public static String camelCasePrettyPrint(String s) {
      if((s==null) || (s.isEmpty())) return "";
      char[] temp = s.toCharArray();
      temp[0] = Character.toUpperCase(temp[0]);
      s = new String(temp);
      Pattern p = Pattern.compile("[a-z][A-Z]");
      Matcher m = p.matcher(s);
      while(m.find()) {
        char[] group = m.group().toCharArray();
        s = s.replaceAll(m.group(), new String(group[0] + " " + group[1]));
      }
      return s;
    }

    /*
     * Converts camelCase to TitleCase, including integers
     * Ex: "zone1" -> "Zone 1"
     */
    public static String camelCasePrettyPrintIntegers(String s) {
      if ((s == null) || (s.isEmpty())) return "";
      char[] temp = s.toCharArray();
      temp[0] = Character.toUpperCase(temp[0]);
      s = new String(temp);
      Pattern p = Pattern.compile("([a-zA-Z])([0-9])|([0-9])([a-zA-Z])");
      Matcher m = p.matcher(s);
      while(m.find()) {
        char[] group = m.group().toCharArray();
        s = s.replaceAll(m.group(), new String(group[0] + " " + group[1]));
      }
      return s;
    }

    public static double getWidth(String s) {
      if((s==null)||(s.isEmpty())) return 0;
      char[] chars = s.toCharArray();
      double width = 0;
      for (char c : chars) {
        if (c=='A')width+=24;
        else if (c=='B')width+=19;
        else if (c=='C')width+=22;
        else if (c=='D')width+=21;
        else if (c=='E')width+=19;
        else if (c=='F')width+=17;
        else if (c=='G')width+=24;
        else if (c=='H')width+=24;
        else if (c=='I')width+=6;
        else if (c=='J')width+=21;
        else if (c=='K')width+=21;
        else if (c=='L')width+=18;
        else if (c=='M')width+=28;
        else if (c=='N')width+=23;
        else if (c=='O')width+=24;
        else if (c=='P')width+=19;
        else if (c=='Q')width+=23;
        else if (c=='R')width+=20;
        else if (c=='S')width+=19;
        else if (c=='T')width+=22;
        else if (c=='U')width+=20;
        else if (c=='V')width+=21;
        else if (c=='W')width+=30;
        else if (c=='X')width+=21;
        else if (c=='Y')width+=20;
        else if (c=='Z')width+=18;
        else if (c==' ')width+=7;
        else if (c=='a')width+=18;
        else if (c=='b')width+=18;
        else if (c=='c')width+=18;
        else if (c=='d')width+=18;
        else if (c=='e')width+=18;
        else if (c=='f')width+=13;
        else if (c=='g')width+=18;
        else if (c=='h')width+=18;
        else if (c=='i')width+=5;
        else if (c=='j')width+=10;
        else if (c=='k')width+=18;
        else if (c=='l')width+=8;
        else if (c=='m')width+=29;
        else if (c=='n')width+=18;
        else if (c=='o')width+=19;
        else if (c=='p')width+=18;
        else if (c=='q')width+=20;
        else if (c=='r')width+=11;
        else if (c=='s')width+=14;
        else if (c=='t')width+=12;
        else if (c=='u')width+=17;
        else if (c=='v')width+=17;
        else if (c=='w')width+=26;
        else if (c=='x')width+=16;
        else if (c=='y')width+=16;
        else if (c=='z')width+=16;
        else if (c=='0')width+=18;
        else if (c=='1')width+=17;
        else if (c=='2')width+=18;
        else if (c=='3')width+=19;
        else if (c=='4')width+=19;
        else if (c=='5')width+=20;
        else if (c=='6')width+=19;
        else if (c=='7')width+=19;
        else if (c=='8')width+=18;
        else if (c=='9')width+=18;
        else if (c=='.')width+=8;
        else if (c=='?')width+=13;
        else if (c=='!')width+=6;
        else if (c==':')width+=8;
        else if (c==';')width+=9;
        else if (c=='/')width+=15;
        else if (c=='(')width+=12;
        else if (c==')')width+=12;
        else if (c=='<')width+=18;
        else if (c=='>')width+=17;
        else if (c==',')width+=8;
        else if (c=='@')width+=30;
        else if (c=='#')width+=21;
        else if (c=='$')width+=18;
        else if (c=='%')width+=24;
        else if (c=='^')width+=14;
        else if (c=='&')width+=20;
        else if (c=='*')width+=16;
        else if (c=='(')width+=11;
        else if (c==')')width+=13;
        else if (c=='[')width+=7;
        else if (c==']')width+=7;
        else if (c=='{')width+=11;
        else if (c=='}')width+=13;
        else if (c=='+')width+=17;
        else if (c=='-')width+=8;
        else if (c=='=')width+=16;
        else if (c=='_')width+=16;
        else width+=22;
      }
      width /= 40.0;
      return width;
    }

  }

