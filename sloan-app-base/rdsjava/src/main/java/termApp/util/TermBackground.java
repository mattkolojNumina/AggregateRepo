package termApp.util;

import static termApp.util.Constants.INVISIBLE;
import static termApp.util.Constants.SCREEN_HEIGHT;
import static termApp.util.Constants.SCREEN_WIDTH;

import java.util.*;

public class TermBackground {
   private static TermBackground single_instance = null; 

   private Rectangle box;
   
   private long toggleTick, togglePeriod;
   private int toggleIndex;
   private boolean toggleColors;
   private String currentFill;
   private List<String> colorList;
   
   public TermBackground() { 
      box = null;
      toggleColors = false;
   }
   
   /*
   public static TermBackground getBackground() {
      if (single_instance == null)
         single_instance =  new TermBackground();
      
      return single_instance;
   }
   */
   
   public void init(String bkgdColor) {
      if (box != null)
         return;
      
      int x = 0;
      int y = 0;
      int w = SCREEN_WIDTH;
      int h = SCREEN_HEIGHT;
      int b = 0;
      currentFill = bkgdColor ;
      String border = INVISIBLE ;
      box = new Rectangle(x,y,w,h,currentFill,b,border, false);
   }
   
   public void setFill(String color) {
      if (box == null)
         init(INVISIBLE);
      box.updateFill(color);
      box.show();
   }
   
   public String getFillColor() {
      if (box == null)
         init(INVISIBLE);
      return currentFill;
   }
   
   public void enableToggle(long period) {
      toggleColors = true;
      toggleIndex = 0;
      togglePeriod = period;
      toggleTick = 0L;
   }
   
   public void tick() {
      if (!toggleColors) 
         return;
      
      if (colorList == null || colorList.isEmpty())
         return;
      
      if (toggleTick == 0) {
         toggleTick = System.currentTimeMillis();
         setFill(colorList.get(0));
         return;
      }
      
      long now = System.currentTimeMillis();
      if (now > toggleTick + togglePeriod) {
         if (++toggleIndex >= colorList.size())
            toggleIndex = 0;
         setFill(colorList.get(toggleIndex));
         toggleTick = now;
      }
         
   }
   
   public void appendColorList( String color ) {
      if (colorList == null)
         colorList = new ArrayList<String>();
      colorList.add(color);
   }
   
   public void setColorList( List<String> list ) {
      colorList = list;
   }
}
