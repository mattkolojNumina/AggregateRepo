package termApp.util;

import java.util.Arrays;
import java.util.Iterator;

import termApp.util.Constants.*;

public class InfoBox extends TermObject {
   private static final int IB_WIDTH = 500;
   private static final int IB_RATIO = 0;
   
   private static final int IB_FONT_BODY  = 70;
   private static final int IB_FONT_TITLE = 80;
   
   private static final int IB_MARGIN_TOP    = 0;
   private static final int IB_MARGIN_BOTTOM = 0;
   private static final int IB_MARGIN_LEFT   = 0;
   private static final int IB_MARGIN_RIGHT  = 0;

   private static final int IB_ROWS = 0;
   private static final int IB_GAP  = 0;
   
   protected TextField header;
   protected Rectangle bkgd;
   protected TermTable info;
   private int x0, y0, topMargin, bottomMargin, leftMargin, rightMargin, width, rows,font,rowGap;
   private int headerFont;
   private double ratio;

   private InfoBox() {

   }

   private void setOrigin(int x0, int y0) {
      this.x0 = x0;
      this.y0 = y0;      
   }

   private void setFont(int headerFont, int font) {
      this.headerFont = headerFont;
      this.font = font;
   }

   private void setWidth(int width) {
      this.width = width;
   }
   
   private void setWidthRatio(double ratio) {
      this.ratio = ratio;
   }
   
   private void setWidthParams(int width, double ratio) {
      setWidth(width);
      setWidthRatio(ratio);
   }

   private void setRows(int rows, int rowGap) {
      this.rows = rows;
      this.rowGap = rowGap;         
   }

   private void setMargins(int top, int bottom, int left, int right) {
      this.topMargin = top;
      this.leftMargin = left;
      this.rightMargin = right;   
      this.bottomMargin = bottom + font/4;
   }

   private int getNameWidth() {
      if (ratio <= 0 || ratio > 1) 
         return 0;
      int w = width - leftMargin - rightMargin - rowGap;
      int nameWidth = (int) Math.floor(w*ratio); 
      return nameWidth;
   }
   
   private int getValueWidth() {
      if (ratio <= 0 || ratio > 1) 
         return 0;
      int w = width - leftMargin - rightMargin - rowGap;
      int nameWidth = (int) Math.floor(w*(1-ratio)); 
      return nameWidth;
   }
   
   private void init() {
      initHeader();
      initInfo();
      initBackground();
   }

   private void initHeader() {
      //int x = x0 + leftMargin + width/2;
      int x = x0 + width/2;
      int y = y0 + topMargin - rowGap - headerFont;
      int f = headerFont;
      header = new TextField(x, y,f,"black","",Align.CENTER,false);
   }

   private void initInfo() {
      int x = x0 + leftMargin;
      int y = y0 + topMargin;
      int f = font;
      int s = font + rowGap;
      //int w = width;
      int w = width - leftMargin - rightMargin;
   
      TermColumn col = new TermColumn(new TextField(x,y,f,"",false),s);
      col.increase(rows-1);
      info = new TermTable( col );
      info.addColumn( w, col.clone() );
      info.getColumn(1).setAlignment(Align.RIGHT);
      //info.show();        
   }

   private void initBackground() {
      int x = x0;
      int y = y0;
      int s = font+rowGap;
      //int w = leftMargin + width + rightMargin;
      int w = width;
      int h = topMargin + s*rows + bottomMargin;
      bkgd = new Rectangle(x, y, w, h, Constants.INVISIBLE, false);
   }

   public void show() {
      header.show();
      info.show();
      bkgd.show();
   }

   public void hide() {
      header.hide();
      info.hide(); 
      bkgd.hide();
   }
   
   public void shift(int x, int y) {
      header.shift(x, y);
      info.shift(x, y); 
      bkgd.shift(x, y);
   }

   public void setHeader(String title) {
      header.setText(title);
      header.adaptFontToText(width);
   }

   public void setBackground(String fill, String border, int thickness) {
      bkgd.updateFill(fill);
      bkgd.updateBorder(border,thickness);
   }

   public void setInfoNames(String... names) {
      TermOrderedGroup col = info.getColumn(0);
      col.setList("setText", names);
   }

   public void updateInfoPair(int index, String name, String format, Object... args) {
      updateInfoName(index, name);
      updateInfoValue(index, format, args);
   }
   
   public void updateInfoNames(String... names) {
      Iterator<String> textIt = Arrays.asList(names).iterator();
      Iterator<TermBaseObject> labelIt = info.getColObj(0).iterator();
      
      while (labelIt.hasNext()) {
         try { 
            TextField label = (TextField)labelIt.next(); 
            String text = textIt.hasNext() ? textIt.next() : "";
            TextField.updateLabel(label,text,font,getNameWidth());
         } catch (Exception ex) { }
      }
   }

   public void updateInfoName(int index, String format, Object... args) {
      try { 
         TextField label = (TextField)info.getColumn(0).getElement(index);
         String text = String.format(format, args);
         TextField.updateLabel(label,text,font,getNameWidth());
      } catch (Exception ex) { }
   }
   
   public void updateInfoValues(String... values) {
      Iterator<String> textIt = Arrays.asList(values).iterator();
      Iterator<TermBaseObject> labelIt = info.getColObj(1).iterator();
      
      while (labelIt.hasNext()) {
         try { 
            TextField label = (TextField)labelIt.next(); 
            String text = textIt.hasNext() ? textIt.next() : "";
            TextField.updateLabel(label,text,font,getValueWidth());
         } catch (Exception ex) { }
      }
   }
   
   public void updateInfoValue(int index, String format, Object... args) {
      try { 
         TextField label = (TextField)info.getColumn(1).getElement(index);
         String text = String.format(format, args);
         TextField.updateLabel(label,text,font,getValueWidth());
      } catch (Exception ex) { }
   }   
   
   public void updateInfoValueWidth(int index, int width, String format, Object... args) {
      try { 
         TextField label = (TextField)info.getColumn(1).getElement(index);
         String text = String.format(format, args);
         TextField.updateLabel(label,text,font,width);
      } catch (Exception ex) { }
   }   
   
   public static class InfoBoxConstructor{ 
      private int x0, y0, topMargin, bottomMargin, leftMargin, rightMargin, width, rows,font,rowGap;
      private int headerFont;
      private double ratio;
      
      public InfoBoxConstructor(int x0, int y0) {
         setOrigin(x0,y0);
         setFont(IB_FONT_TITLE,IB_FONT_BODY);
         setMargins(IB_MARGIN_TOP,IB_MARGIN_BOTTOM,IB_MARGIN_LEFT,IB_MARGIN_RIGHT);
         setRows(IB_ROWS,IB_GAP);
         setWidthParams(IB_WIDTH,IB_RATIO);
      }
      
      public void setOrigin(int x0, int y0) {
         this.x0 = x0;
         this.y0 = y0;      
      }

      public void setFont(int headerFont, int font) {
         this.headerFont = headerFont;
         this.font = font;
      }

      /**
       * Sets the width of the InfoBox
       * @param width
       *    (Defaults to {@value termApp.util.InfoBox#IB_WIDTH})
       */
      public void setWidth(int width) {
         this.width = width;
      }
      
      /**
       * Sets the width ratio of the InfoBox. This parameter is used to auto adjust the 
       * font size of the name-value pairs to fit the entire text object.
       * 
       * @param ratio
       *    Accepts {@code double} values between 0.00 and 1.00. A value of 0 disables auto 
       *    fitting. Otherwise, its the percentage ratio of the name (left) field.
       */
      public void setWidthRatio(double ratio) {
         this.ratio = ratio;
      }
      
      public void setWidthParams(int width, double ratio) {
         setWidth(width);
         setWidthRatio(ratio);
      }

      /**
       * Sets number of allowed rows and the spacing between the rows.
       * @param rows
       *    (Defaults to {@value termApp.util.InfoBox#IB_ROWS})
       * @param rowGap
       *    (Defaults to {@value termApp.util.InfoBox#IB_GAP})
       */
      public void setRows(int rows, int rowGap) {
         this.rows = rows;
         this.rowGap = rowGap;         
      }

      /**
       * Sets the margins for the InfoBox. Margins are used for aligning text within the 
       * bounds of the origin ({@link #x0}, {@link #y0}) and the {@link #width}. They are 
       * also used for creating the background rectangle object.
       * 
       * @param top 
       *    (Defaults to {@value termApp.util.InfoBox#IB_MARGIN_TOP})
       * @param bottom 
       *    (Defaults to {@value termApp.util.InfoBox#IB_MARGIN_BOTTOM})
       * @param left 
       *    (Defaults to {@value termApp.util.InfoBox#IB_MARGIN_LEFT})
       * @param right 
       *    (Defaults to {@value termApp.util.InfoBox#IB_MARGIN_RIGHT})
       */
      public void setMargins(int top, int bottom, int left, int right) {
         this.topMargin = top;
         this.leftMargin = left;
         this.rightMargin = right;   
         this.bottomMargin = bottom + font/4;
      }
      
      /**
       * Initializes and builds an InfoBox object
       * @return an InfoBox object
       */
      public InfoBox build() {
         InfoBox box = new InfoBox();
         box.setOrigin(x0, y0);
         box.setFont(headerFont, font);
         box.setMargins(topMargin, bottomMargin, leftMargin, rightMargin);
         box.setRows(rows, rowGap);
         box.setWidthParams(width,ratio);
         box.init();
         return box;
      }

   }

   public void clear() {
      info.clear();
   }

}