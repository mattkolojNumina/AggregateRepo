package termApp.util;

import termApp.util.Constants.Align;

public class BorderButton2 extends Button {

   private Rectangle borderBox;
   private int borderWidth;
   
   public BorderButton2(int x, int y, int fontSize, String text, Align align, int width, int height, String color,
         boolean displayNow) {
      super(x, y, fontSize, text, align, width, height, null, displayNow);
      
      borderWidth = 10;
      int bX = x;
      int bY = y - borderWidth;
      int bW = width + 2*borderWidth;
      int bH = fontSize + 20 + 2*borderWidth;
      
      switch(align) {
      case CENTER:
      default:
         break;
      case LEFT:
         bX = x - borderWidth;
         break;
      case RIGHT:
         bX = x + borderWidth;
         break;
      }
      
      borderBox = new Rectangle(bX, bY, bW, bH, color, 0, "", align, displayNow);
   }
   
   @Override
   public void show() {
      super.show();
      if (borderBox!=null)
         borderBox.show();
   }

   @Override
   public void hide() {
      super.hide();
      if (borderBox!=null)
         borderBox.hide();
   }
   
   @Override
   public void clear() {
      super.clear();
      if (borderBox!=null)
         borderBox.clear();
   }
   
   @Override
   public void refresh() {
      super.refresh();
      if (borderBox!=null)
         borderBox.refresh();
   }

}
