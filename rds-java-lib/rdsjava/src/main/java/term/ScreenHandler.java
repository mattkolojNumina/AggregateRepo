/*
 * ScreenHandler.java
 */

package term;


public interface ScreenHandler {

   /** The default screen background color. */
   public static final String DEFAULT_SCREEN_COLOR = "$00C0C0C0";

   /** Initializes the screen and lays out UI elements. */
   public void handleInit();

   /** Performs periodic tasks, once per cycle. */
   public void handleTick();

   /** Performs exit tasks before loading the next screen. */
   public void handleExit();
   
   /** Processes a button press. */
   public void handleButton( int tag );

   /** Processes a function key. */
   public void handleFunction( int key );

   /** Processes text from an entry field. */
   public void handleEntry( int tag, String text );

   /** Processes text from a serial device. */
   public void handleSerial( int tag, String text );

   /** Processes a message from the terminal. */
   public void handleMessage( String text );

}