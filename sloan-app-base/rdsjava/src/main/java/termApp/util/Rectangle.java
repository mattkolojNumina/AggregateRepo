package termApp.util;

import static termApp.util.Constants.*;

public class Rectangle extends TermBaseObject {
   private int borderWidth;
   private String fillColor, borderColor;

   /*
    * constructors
    */
   
   public Rectangle( int x, int y, int xL, int yL,
             String color, int b, String bColor, Align align, boolean display ) {
      super(x,y,xL,yL,align,display);
      this.fillColor = color;
      this.borderWidth = b;
      this.borderColor = bColor;

      //trace("rectagnle %d", tag);

      refresh();
   }
   
   public Rectangle( int x, int y, int xL, int yL,
         String color, int b, String bColor, boolean display ) {
      this( x, y, xL, yL, color, b, bColor, Align.LEFT, display);
   }

   public Rectangle( int x, int y, int xL, int yL,
             String color, int b, String bColor ) {
      this( x, y, xL, yL, color, b, bColor, false);
   }

   public Rectangle( int x, int y, int xL, int yL ) {
      this( x, y, xL, yL, BKGD_COLOR, 0, BKGD_COLOR, false);
   }

   public Rectangle( int x, int y, int xL, int yL,
             String color, boolean display ) {
      this( x, y, xL, yL, color, 0, BKGD_COLOR, display);
   }

   public Rectangle( int x, int y, int xL, int yL,
             String color ) {
      this( x, y, xL, yL, color, 0, BKGD_COLOR, false);
   }
   
   public Rectangle( Rectangle r ) {
      this(r.x0,r.y0,r.width,r.height,r.fillColor,r.borderWidth,r.borderColor,r.alignment,false);
   }
   
   public Rectangle( TermBaseObject o ) {
      this(o.x0,o.y0,o.getWidth(),o.getHeight(),BKGD_COLOR,0,BKGD_COLOR, o.alignment,false);
   }
   
   @Override
   public TermBaseObject clone( ) {
      return new Rectangle(x0,y0,width,height,fillColor,borderWidth,borderColor,alignment,false);
   }
   
   /*
    * display methods
    */

   @Override
   public void clear() {
      term.setRectangle( tag, -1, -1, 0, 0, fillColor, 0, borderColor ); 
   }
   
   @Override
   public void hide() {
      on = false;
      term.setRectangle( tag, -1, -1, 0, 0, fillColor, 0, borderColor ); 
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
      term.setRectangle( tag, x, y0, width, height, fillColor, borderWidth, borderColor ); 
   }

   
   public void setBorder( String color, int width ) {
      if( color == null || color.isEmpty() )
         return;
      borderWidth = width >= 0 ? width : borderWidth > 0 ? borderWidth : 0;
      borderColor = color;
   }
   
   public void updateBorder( String color ) {
      setBorder(color, -1);
      refresh();
   }
   
   public void updateBorder( String color, int width ) {
      setBorder(color, width);
      refresh();
   }
   
   public void setFill( String color ) {
      if( color == null || color.isEmpty() )
         return;
      fillColor = color;
   }
   
   public void updateFill( String color ) {
      setFill(color);
      refresh();
   }

   public void margin(int i) {
      shift(-i,-i);
      width += 2*i;
      height += 2*i;
      refresh();
   }

   public String getFillColor() {
      return fillColor;
   }

   public void setWidth(int width) {
      this.width = width;
   }

}
