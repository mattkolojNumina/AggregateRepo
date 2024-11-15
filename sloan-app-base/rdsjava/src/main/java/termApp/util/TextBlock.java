package termApp.util;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;


public class TextBlock extends TermColumn {
   
   protected final int charWidth;
   private int font, width;
   protected static final double WIDTH_FACTOR = 1.15;

   public TextBlock( int x, int y, int width, 
         int fontSize, int rows ) {
      super( new TextField(x, y,fontSize, ""), TermColumn.DEFAULT_SPACING, rows);
      this.increase(maxSize-1);
      this.font = fontSize;
      this.width = width;
      this.charWidth =  (int)(width / fontSize / WIDTH_FACTOR);
   }
   
   /*
   public TextWrap( int x, int y, int width, 
         int fontSize ) {
      super( new TextField(x, y, fontSize, "") );
      this.charWidth =  (int)(width / fontSize / WIDTH_FACTOR);
   }
   */
   
   /*
   public TextWrap( int left, int top, int width, 
         int font, String format, Object... args ) {
      this( left, top, width, font );
      wrap(format, args);
   }
   */
   public TextBlock( int left, int top, int width, 
         int font, int rows, String format, Object... args ) {
      this( left, top, width, font, rows );
      show();
      wrap(format, args);
   }
   
   public void wrap(String format, Object... args) {
      String[] parts = String.format(format, args).split(" ");
      int j = 0;
      boolean bNewline = false;
      String buffer = parts[j++];
      List<String> list = new ArrayList<String>();

      while (list.size() <= maxSize && j < parts.length) {
         if (parts[j].contains("\n")) {
            bNewline = true;
            parts[j] = parts[j].replaceAll("\n", "");
            buffer += " " + parts[j++];
         // } else if (buffer.length() + parts[j].length() + 1 > charWidth || bNewline) {
         } else if (calculateWidth(buffer + " " + parts[j]) > width || bNewline) {
            bNewline = false;
            if (list.size() == maxSize - 1)
               break;

            list.add(buffer);
            buffer = parts[j++];
         } else {
            buffer += " " + parts[j++];
         }
      }
      list.add(buffer + (j == parts.length ? "" : "...") );

      setList("updateText",list);

   }
   
   public int calculateWidth( String text ) {
      AffineTransform affinetransform = new AffineTransform();     
      FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
      Font f = new Font("Dialog", Font.BOLD, font);
      //System.out.println( "" + f);
      int w = (int)(f.getStringBounds(text, frc).getWidth() * WIDTH_FACTOR);
      return w;
      //return (int)(getText().length() * font * FONT_WIDTH_FACTOR);
   }

}   

