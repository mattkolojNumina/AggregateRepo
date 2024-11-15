package termApp.util;

public class Constants {

   public static final int SCREEN_WIDTH = 1920;
   public static final int SCREEN_HEIGHT = 1080;
   
   // layout constants
   public static final int MARGIN      = 50;
   public static final int FONT_S      = 20;
   public static final int FONT_M      = 60;
   public static final int FONT_L      = 75;
   public static final int GAP_S       =  5;
   public static final int GAP_M       = 10;
   public static final int GAP_L       = 15;

   public static final int MSG_MARGIN  = 100;
   public static final int MSG_Y       = 680;
   public static final int MSG_SCAN_Y  = 580;

   public static final int FONT_TITLE  = 90;
   public static final int TITLE_Y     = 30;
   public static final int INFO_Y      = TITLE_Y*2 + FONT_TITLE;
   public static final int BODY_Y      = INFO_Y + 60*4 + 10;
   
   public static final int BTN_Y    = 880;
   public static final int BTN_FONT =  60;
   
   public static final int W1_4 = SCREEN_WIDTH * 1/4;
   public static final int W1_3 = SCREEN_WIDTH * 1/3;
   public static final int W1_2 = SCREEN_WIDTH * 1/2;
   public static final int W2_3 = SCREEN_WIDTH * 2/3;
   public static final int W3_4 = SCREEN_WIDTH * 3/4;

   public static final double WIDTH_FACTOR = 0.63;
   
   public static final int DEFAULT_BUTTON_FONT = 60;
   public static final int BUTTON_MARGIN = 10;
   public static final int BUTTON_Y = 900;
   
   public static final int DEFAULT_FONT = 75;

   
   // COLORS
   public static final String COLOR_GREEN  = "$0093ff93";
   public static final String COLOR_YELLOW = "$0093ffff";
   public static final String COLOR_RED    = "$009393FF";
   public static final String COLOR_BLUE   = "$00ffb893";
   public static final String COLOR_PURPLE = "$00ff93b8";
   public static final String COLOR_ORANGE = "$0093d9ff";
   public static final String COLOR_PINK   = "$00d993ff";
   public static final String COLOR_WHITE  = "$00ffffff";
   public static final String COLOR_BLACK  = "$00000000";

   
   public static final String INVISIBLE = "$FF000000";

   
   public static final String TEXT_COLOR = "$00000000";
   public static final String BKGD_COLOR = term.ScreenHandler.DEFAULT_SCREEN_COLOR;

   public enum ColorStatus {
      DEFAULT(BKGD_COLOR),
      GREEN(COLOR_GREEN),
      YELLOW(COLOR_YELLOW),
      RED(COLOR_RED),
      BLUE(COLOR_BLUE),
      ORANGE(COLOR_ORANGE),
      PURPLE(COLOR_PURPLE),
      ;

      private final String color;

      ColorStatus(String color) {
         this.color = color;
      }
      
      public String getColor() {
         return this.color;
      }
   };
   public static final String BKGD_C0  = BKGD_COLOR;
   public static final String BKGD_C1  = COLOR_BLUE; 
   public static final String BKGD_C2  = COLOR_ORANGE; 
   public static final String BKGD_C3  = COLOR_PURPLE;
   public static final String BKGD_C4  = COLOR_YELLOW; 
   
   public static final String BKGD_BAD  = COLOR_RED;
   public static final String BKGD_GOOD = COLOR_GREEN;

   
   
   /**
    * Enum for setting alignment of objects. Values are: <ul>
    * <li> {@code LEFT} </li> 
    * <li> {@code CENTER} </li>
    * <li> {@code RIGHT} </li>
    * </ul>
    */
   public static enum Align {
      LEFT,
      CENTER,
      RIGHT,
   };
   
}
