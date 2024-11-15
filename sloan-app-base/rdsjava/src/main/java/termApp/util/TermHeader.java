package termApp.util;

import static termApp.util.Constants.*;
import termApp.util.Constants.Align;

/*
 *  Header class
 */
public class TermHeader extends TermObject {
   private static TermHeader single_instance = null; 

   private TextField title;
   private Rectangle border;
   
   public TermHeader() { 
      title = null;
      border = null;
   }
   
   /*
   public static TermHeader getHeader() {
      if (single_instance == null)
         single_instance =  new TermHeader();
      
      return single_instance;
   }
   */
   
   public void init() {
      if (title != null)
         return;
      
      title = new TextField( SCREEN_WIDTH/2, TITLE_Y, FONT_TITLE, "",Align.CENTER);
      title.show(); //enable?
      border = new  Rectangle(  MARGIN, TITLE_Y, SCREEN_WIDTH-2*MARGIN, 100, INVISIBLE, true);
   }
   
   public void show() {
      super.show();
      title.show();
      border.show();
   }
   
   public void hide() {
      super.hide();
      title.hide();
      border.hide();
   }

   public void updateBorder( String color ) {
      border.updateFill(color);
   }   
   
   public void updateTitle(String format, Object... args) {
      title.updateText(format, args);
   }
   
}
