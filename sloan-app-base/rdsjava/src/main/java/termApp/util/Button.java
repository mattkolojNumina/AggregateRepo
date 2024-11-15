package termApp.util;

import static termApp.util.Constants.*;

import termApp.util.Constants.Align;

public class Button extends TermActionObject {
   protected static final double FONT_HEIGHT_FACTOR = 2.00;
   protected static final double FONT_WIDTH_FACTOR  = .7;

   private final int fontSize;
   private String fillColor;
   //My addition
   private int count;
   private int countTotal;
   private int id;
   private String sku;
   private boolean complete;
   private String text;

   /*
    * constructors
    */
   
   public Button( int x, int y, int fontSize, String text, Align align, 
         int width, int height, String color, boolean displayNow ) {
      super(x,y,width, height, align, displayNow);
      this.fontSize = fontSize;
      this.text = text;
      this.fillColor = color;
      //trace("button %d", tag);
      refresh();
   }
   
   public Button( int x, int y, int fontSize, String text, Align align, 
         int width, boolean displayNow ) {
      this( x, y, fontSize, text, align,width, -1, null, displayNow );
   }
   
   public Button( int x, int y, int fontSize, String text, Align align, 
         boolean displayNow ) {
      this( x, y, fontSize, text, align, -1, -1, null, displayNow );
   }
   
   public Button( int x, int y, String text, boolean displayNow ) {
      this( x, y, DEFAULT_BUTTON_FONT, text, Align.CENTER, -1, -1, null, displayNow );
   }
   
   public Button( int x, int y, String text ) {
      this( x, y, DEFAULT_BUTTON_FONT, text, Align.CENTER, -1, -1, null, false );
   }
   
   public Button( int x, int y, String text, Align align, int width ) {
      this( x, y, DEFAULT_BUTTON_FONT, text, align, width, -1, null, false );
   }
   
   public Button( int x, int y, String text, Align align ) {
      this( x, y, DEFAULT_BUTTON_FONT, text, align, -1, -1, null, false );
   }
   
   public Button( int x, int y, String text, int width ) {
      this( x, y, DEFAULT_BUTTON_FONT, text, Align.CENTER, width, -1, null, false );
   }
   
   public Button( Button b ) {
         this(b.x0,b.y0,b.fontSize,b.text,b.alignment,b.width,b.height,b.fillColor,false);
   }  
   
   public Button( TermBaseObject o ) {
      this(o.x0,o.y0,(int)(o.getHeight()/FONT_HEIGHT_FACTOR),"",o.alignment,o.getWidth(),-1,null,false);
   }  


   
   //@Override
   public void counter()
   {
      count++;
   }

   //@Override
   public int countTotal()
   {
      return count;
   }

   public void setCount(int quantity){
      countTotal = quantity;
   }
   
   public int getCount(){
      return countTotal;
   }

   public void setID(int ID){
      id = ID;
   }

   public int getID(){
      return id;
   }

   public void setSku(String Sku){
      sku = Sku;
   }
   
   public String getSku(){
      return sku;
   }

   public void setCompletion(boolean x){
      complete=x;
   }

   public boolean getCompletion(){
      return complete;
   }

   @Override
   public TermBaseObject clone( ) {
      return new Button(x0,y0,fontSize,text,alignment,width,height,fillColor,false);
   }
   
   /*
    * display methods
    */

   @Override
   public void clear() {
      text = "";
      show();
   }
   
   @Override
   public void hide() {
      on = false;
      term.setButton( tag, -1, -1, 0, 0, 0, "", "" );
   }
   
   @Override
   public void show( ) {
      on = true;
      int w, h, x, y;
      String c = "";

      w = width < 0 ? getWidth( fontSize, text + "MMM" ) : width;  // extra width for padding
      h = getHeight();
      y = y0;
      if (fillColor != null)
         c = fillColor;
      
      switch( alignment ) {
         case LEFT:  x = x0;
            break;
         case RIGHT: x = x0 - w;
            break;
         case CENTER:  
         default:    x = x0 - w/2;
         break;
      }
      term.setButton( tag, x, y, w, h, fontSize, text, c );
   }
   
   @Deprecated
   public boolean pressed( int pressedTag ) {
      return actionOccured(pressedTag);
   }
   
   public void setText( String newText ) {
      text = newText;
   }
   
   public void setFill( String color ) {
      if( color == null )
         color = "";
      fillColor = color;
   }
   
   public String getText() {
      return text;
   }
   
   public int getFontSize() {
      return fontSize;
   }
   
   public int getHeight() {
      if (height > 0)
         return super.getHeight();
      return (int)(fontSize + 2 * BUTTON_MARGIN);
   }
   
   public int getWidth( ) {
      if (width > 0)
         return width;
      return (int)(text.length() * fontSize * FONT_WIDTH_FACTOR);
   }
   
   private int getWidth( int size, String text ) {
      return (int)(text.length() * size * WIDTH_FACTOR);
   }

   public void display(boolean display) {
      if (display)
         show();
      else
         hide();
   }
}
