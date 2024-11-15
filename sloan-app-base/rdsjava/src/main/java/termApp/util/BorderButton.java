package termApp.util;

public class BorderButton extends TermObject {

   private TermGroup borderBox;
   
   /**
    * Use example:
    * <pre>{@code      
    *  Button btn = new Button(...);
    *  BorderButton brderBtn = new BorderButton(btn,borderColor, borderWidth);
    *  }</pre>
    * @param btn : Button object to add border to
    * @param color : color of border
    * @param width : width of border
    */
   public BorderButton(Button btn, String color, int width) {
      borderBox = new TermGroup(btn);
      int borderWidth = width;
      int bX = btn.x0;
      int bY = btn.y0 - borderWidth;
      int bW = btn.getWidth()  + 2*borderWidth;
      int bH = btn.getHeight() + 2*borderWidth;
      
      switch(btn.alignment) {
      case CENTER:
      default:
         break;
      case LEFT:
         bX = btn.x0 - borderWidth;
         break;
      case RIGHT:
         bX = btn.x0 + borderWidth;
         break;
      }
      
      borderBox.add( new Rectangle(bX, bY, bW, bH, color, 0, "", btn.alignment, btn.on()));
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
   public void refresh() {
      super.refresh();
      if (borderBox!=null)
         borderBox.refresh();
   }

}
