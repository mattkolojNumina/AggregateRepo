package termApp.util;

import static termApp.util.Constants.*;

public class PasswordEntry extends TextEntry{

   /*
    * constructors
    */

   public PasswordEntry( int x, int y, int fontSize, 
         int width, int height, Align align, boolean displayNow ) {
      super(x, y, fontSize, width, height, align, displayNow);
   }
   
   public PasswordEntry( int x, int y, int fontSize, 
               int width, int height, boolean displayNow ) {
      this(x, y, fontSize, width, height, Align.LEFT, displayNow);
   }
   
   
   public PasswordEntry( PasswordEntry p ) {
      this(p.x0,p.y0,p.font,p.width,p.height,p.alignment,false);
   }
   
   public PasswordEntry( TermBaseObject o ) {
      this(o.x0,o.y0,(int)(o.getHeight()/FONT_HEIGHT_FACTOR),o.getWidth(),o.height,o.alignment,false);
   }
   
   @Override
   public PasswordEntry clone( ) {
      return new PasswordEntry(x0,y0,font,width,height,alignment,false);
   }
   
   /*
    * display methods
    */

   @Override
   public void hide() {
      on = false;
      term.setPassword( tag, -1, -1, 0, 0, 0 );
   }
   
   @Override
   public void show() {
      on = true;
      
      int x;
      switch( alignment ) {
         case CENTER: x = x0 - width/2;
            break;
         case RIGHT:  x = x0 - width;
            break;
         case LEFT: 
         default:     x = x0;
            break;
      }
      term.setPassword( tag, x, y0, width, height, font );
   }
}
