package termApp.util;

import static termApp.util.Constants.*;

public class TextEntry extends TermActionObject {
   protected static final double FONT_HEIGHT_FACTOR = 1.0;
   protected final int font ;

   /*
    * constructors
    */

   public TextEntry( int x, int y, int fontSize, 
         int width, int height, Align align, boolean displayNow ) {
      super(x,y,width, height, align, displayNow);
      this.font = fontSize;
      //trace("entry %d", tag);

      refresh();
   }
   
   public TextEntry( int x, int y, int fontSize, 
               int width, int height, boolean displayNow ) {
      this(x, y, fontSize, width, height, Align.LEFT, displayNow);
   }
   
   public TextEntry( TextEntry t ) {
      this(t.x0,t.y0,t.font,t.width,t.height,t.alignment,false);
   }
   
   public TextEntry( TermBaseObject o ) {
      this(o.x0,o.y0,(int)(o.height/FONT_HEIGHT_FACTOR),o.getWidth(),o.getHeight(),o.alignment,false);
   }
   
   @Override
   public TermBaseObject clone( ) {
      return new TextEntry(x0,y0,font,width,height,alignment,false);
   }
   
   /*
    * display methods
    */

   @Override
   public void clear() {
      show();
   }
   
   @Override
   public void hide() {
      on = false;
      term.setEntry( tag, -1, -1, 0, 0, 0 );
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
      term.setEntry( tag, x, y0, width, height, font );
   }

   @Deprecated
   public boolean entered( int entryTag ) {
      return actionOccured(entryTag);
   }
}
