/*
 * RDSDashboardApp.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.awt.Container;
import java.lang.reflect.Constructor;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rds.RDSUtil;


/**
 * An application for running a dashboard or a single dashboard panel.
 */
public class RDSDashboardApp
      extends JFrame {
   private String[] args;

   /**
    * Constructs the application.
    * 
    * @param   args  the program arguments, passed directly from {@code main}
    */
   private RDSDashboardApp( String... args ) {
      super( "RDS Dashboard" );
      this.args = args;

      setName( "RDS Dashboard Application" );

      // set the look and feel
      try {
         UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName() );
      } catch (Exception ex) {
         RDSUtil.alert( getName() + ": error setting look-and-feel" );
         RDSUtil.alert( ex );
      }

      // add content (full dashboard or single RDSDashboardPanel)
      String panelName = (args != null && args.length > 1) ? args[1] : null;
      if (panelName == null) {  // full dashboard
         setContentPane( new RDSDashboard( this ) );
      } else {  // single panel
         try {
            Class<?> panelClass = Class.forName( panelName );
            Constructor<?> panelConstructor =
                  panelClass.getConstructor( Container.class );
            RDSDashboardPanel panel =
                  (RDSDashboardPanel)panelConstructor.newInstance( this );
            setContentPane( panel );
         } catch (Exception ex) {
            RDSUtil.alert( ex );
         }
      }

      setVisible( true );
   }

   /**
    * Retrieves the command-line arguments as passed in during program
    * invocation.
    * 
    * @return  the command-line arguments, an array of {@code String}s
    */
   public String[] getArgs() {
      return args;
   }

   /**
    * Creates the GUI and displays it.  For thread safety, this method
    * should be invoked from the event-dispatching thread.
    */
   private static void createAndShowGUI( final String... args ) {
      JFrame app = new RDSDashboardApp( args );

      // display the window, centered on the screen
      app.pack();
      app.setLocationRelativeTo( null );
      app.setVisible( true );
   }

   /**
    * The application entry point.
    * 
    * @param   args  a list of command-line arguments
    */
   public static void main( final String... args ) {
      // create the GUI on the event-dispatching thread
      SwingUtilities.invokeLater( new Runnable() {
         @Override
         public void run() {
            createAndShowGUI( args );
         }
      } );
   }

}  // end RDSDashboardApp class