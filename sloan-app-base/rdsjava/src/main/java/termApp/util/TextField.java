package termApp.util;

import static termApp.util.Constants.*;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class TextField extends TermBaseObject {
   private int font;
   private String text, fontColor;
   protected static final double FONT_WIDTH_FACTOR  = 1.05; //1.01;
   protected static final double FONT_HEIGHT_FACTOR = 1.01;
   protected static final double Y_FUDGE = 0.0;

   /*
    * constructors
    */
   
   public TextField( int x, int y, int fontSize, String color, String initText, 
         Align align, boolean displayNow ) {
      super(x,y,0,0,align,displayNow);
      
      this.font = fontSize;
      this.width = (int) (font * FONT_WIDTH_FACTOR);
      this.height = (int) (font * FONT_HEIGHT_FACTOR);
      this.fontColor = color;
      this.text = initText;
      
      refresh();
   }

   public TextField( int x, int y, int fontSize, 
               String initText, boolean displayNow ) {
      this( x, y, fontSize, "Black", initText, Align.LEFT, displayNow );
   }

   public TextField( int x, int y, int fontSize, 
               String initText, Align align ) {
      this( x, y, fontSize,  "Black", initText, align, true );
   }

   public TextField( int x, int y, int fontSize, 
               String initText ) {
      this( x, y, fontSize,  "Black", initText, Align.LEFT, true );
   }

   public TextField( int x, int y, String initText ) {
      this( x, y, DEFAULT_FONT, "Black", initText, Align.LEFT, true );
   }

   public TextField( int x, int y ) {
      this( x, y, DEFAULT_FONT, "Black", "", Align.LEFT, false );
   }
   
   public TextField( TextField t ) {
      this(t.x0,t.y0,t.font,t.fontColor,t.text,t.alignment,false);
   }
   
   public TextField( TermBaseObject o ) {
      this(o.x0,o.y0,(int)(o.getHeight()/FONT_HEIGHT_FACTOR),"Black","",o.alignment,false);
   }
   
   
   @Override
   public TermBaseObject clone( ) {
      return new TextField(x0,y0,font,fontColor,text,alignment,false);
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
      super.hide();
      term.staticTextColor( tag, 0, 0, 0, "", fontColor );
   }
   
   @Override
   public void show( ) {
      super.show();
      int w, a, x, y;

      w = SCREEN_WIDTH;
      y = (int) (y0 + font*Y_FUDGE);
      
      switch( alignment ) {
         case LEFT:  
            x = x0;       
            a = 0;
            break;
         case RIGHT: 
            x = x0 - w;   
            a = -w;
            break;
         case CENTER:  
         default:    
            x = x0 - w/2; 
            a = w;
         break;
      }
      
      term.staticTextColorAlign( tag, x, y, font, text, fontColor, a);
   }

   public void setText( String format, Object... args ) {
      setText(  String.format( format, args ) );
   }
   
   public void setText( String newText ) {
      text = newText;
   }
   
   public int getFont() {
      return font;
   }
   
   public void setFont( int newFont ) {
      font = newFont;
   }
   
   public void setFontColor( String color ) {
   	this.fontColor = color;
   }   
   
   public String getText() {
      return text;
   }
   
   public void updateText( String newText ) {
      if(text != null && text.equals(newText))
         return;
      
      text = newText;
      refresh();
   }

   public void updateText( String format, Object... args ) {
      updateText(  String.format( format, args ) );
   }
   
   public void setAlignment( String align ) {
      try {
         alignment = Align.valueOf(align.toUpperCase());
      } catch (IllegalArgumentException ex ) {
         TermUtility.error( "failed to extract Align from [%s]", align);
      }
   }
   
   @Override
   public int getWidth() {
      AffineTransform affinetransform = new AffineTransform();     
      FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
      //Font f = new Font("Tahoma", Font.PLAIN, font);
      Font f = new Font("Dialog", Font.BOLD, font);
      return (int)(f.getStringBounds(text, frc).getWidth() * FONT_WIDTH_FACTOR);
      //return (int)(getText().length() * font * FONT_WIDTH_FACTOR);
   }
   
   @Override
   public int getHeight() {
      AffineTransform affinetransform = new AffineTransform();     
      FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
      //Font f = new Font("Tahoma", Font.PLAIN, font);
      Font f = new Font("Dialog", Font.BOLD, font);
      return (int)(f.getStringBounds(text, frc).getHeight() * FONT_HEIGHT_FACTOR);
      //return (int)(getText().length() * font * FONT_WIDTH_FACTOR);
   }

   public void resetFont(int oldFont) {
      shift(0,-(oldFont-font)/2);
      font = oldFont;
   }
   
   public void adaptFontToText(int width) {
      int testWidth = getWidth();
      int oldFont = font;

      while (testWidth>width) {
         font = font * 9 / 10;
         testWidth = getWidth();
      }
      
      shift(0,(oldFont-font)/2);
   }
   
   public void updateLabelWidth(int font, int width, String format, Object... args) {
      String oldLabel = getText();
      String text = String.format(format, args);
      if(!oldLabel.equals(text)) {
         setText(text);
         if (width>0) {
            hide();
            resetFont(font);
            adaptFontToText(width);
         }
      }
      show();
   }
   
   protected static void updateLabel(TextField textField, String text, int font, int width) {
      String oldLabel = textField.getText();
      if(!oldLabel.equals(text)) {
         textField.setText(text);
         if (width>0) {
            textField.hide();
            textField.resetFont(font);
            textField.adaptFontToText(width);
         }
      }
      textField.show();
   }
}
