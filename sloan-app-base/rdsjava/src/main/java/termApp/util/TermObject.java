package termApp.util;

import term.TerminalDriver;
import termApp.AbstractScreen;

public abstract class TermObject {
   protected static TerminalDriver term;
   protected static AbstractScreen screen;
   protected boolean on;

   
   /*
    * display methods
    */
   
   public void refresh() {
      if ( on ) show();
      else hide();
   }
   
   /**
    * Hide the object, i.e., remove from screen. 
    * Also sets {@link #on} to false.
    */
   public void hide() {
      on = false;
      // hide object
   }
   
   /**
    * Show the object, i.e., draw it on the screen.
    * Also sets {@link #on} to true.
    */
   public void show() {
      on = true;
      // display object
   }
   
   /**
    * Enable object. Set {@link #on} to true.
    */
/*   public void enable() {
      on = true;
   }
   */
   /**
    * Disable object. Set {@link #on} to false.
    */
   public void disable() {
      on = false;
   }
  
   /**
    * Get the on value of the object
    * 
    * @return on status of object
    */
   public boolean on() {
      return on;
   }
   
}
