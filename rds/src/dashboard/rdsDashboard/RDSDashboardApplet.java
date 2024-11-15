/*
 * RDSDashboardApplet.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.lang.reflect.Constructor;
import javax.swing.JApplet;
import javax.swing.UIManager;

import rds.*;


/**
 * An applet for running a dashboard or a single dashboard panel.
 */
public class RDSDashboardApplet
      extends JApplet {

   private RDSDashboard dashboard;

   /**
    * {@inheritDoc}
    */
   @Override
   public void init() {
      setName( "RDS Dashboard Applet" );

      // set look-and-feel
      try {
         UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName() );
      } catch (Exception ex) {
         RDSUtil.alert( getName() + ": error setting look-and-feel" );
         RDSUtil.alert( ex.toString() );
      }

      // add content (full dashboard or single RDSDashboardPanel)
      String panelName = getParameter( "panel" );
      if (panelName == null) {  // full dashboard
         RDSUtil.trace( "begin dashboard initialization" );
         dashboard = new RDSDashboard( this );
         setContentPane( dashboard );
         RDSUtil.trace( "initialization complete" );
      } else {  // single content panel
         try {
            Class<?> panelClass = Class.forName( panelName );
            Constructor<?> panelConstructor =
                  panelClass.getConstructor( java.awt.Container.class );
            RDSDashboardPanel panelObj =
                  (RDSDashboardPanel)panelConstructor.newInstance( this );
            setContentPane( panelObj );
         } catch (Exception ex) {
            RDSUtil.alert( ex );
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void start() {
      if (dashboard != null)
         dashboard.restartRefreshTimer();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop() {
      if (dashboard != null)
         dashboard.stopRefreshTimer();
   }

}  // end RDSDashboardApplet class
