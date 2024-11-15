
package rdsDashboard.panel;

import java.awt.*;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.RDSStatusElement;
import rdsDashboard.RDSStatusElement.Status;
import rdsGraphics.*;


/**
 * A dashboard panel for viewing graphical diagnostics.
 */
public class DiagPanel
      extends RDSDashboardPanel {
   private RDSGraphicsDisplayPanel displayPanel;
   private RDSStatusElement statusElement;

   private String area;
   private String rdsFileName;

   /**
    * Constructs a dashboard panel for viewing graphical diagnostics.
    */
   public DiagPanel( String id, Container parentContainer ) {
      super( id, parentContainer );
      setName( "Graphical Diagnostics" );
      setDescription( "View the conveyor status" );

      createUI();

      statusElement = new RDSStatusElement( "Conveyor Status" );
      if (parentContainer instanceof RDSDashboard)
         ((RDSDashboard)parentContainer).registerStatusElement( this,
               statusElement );
   }

   /**
    * Creates the user interface for this panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( true );

      area = getParam( "area" );
      if (area == null || area.isEmpty())
         area = "main";
      rdsFileName = getParam( "rdsFile" );
      RDSUtil.trace( "%s: area = [%s], rds file = [%s]",
            getName(), area, rdsFileName );
      RDSObject.setCodeBase( baseURL.toString() );

      displayPanel = new RDSGraphicsDisplayPanel( area, rdsFileName, db );
      displayPanel.setBorder( BorderFactory.createEtchedBorder() );
      add( displayPanel, BorderLayout.CENTER );
      add( new RDSControls( displayPanel ), BorderLayout.SOUTH );
   }

   /**
    * Makes the component visible or invisible.  Overrides {@code
    * JComponent.setVisible} to start or stop the update activity of the
    * contained graphics display panel.
    * 
    * @param   aFlag  {@code true} to make the component visible; {@code
    *          false} to make it invisible
    */
   @Override
   public void setVisible( boolean aFlag ) {
      if (aFlag != isVisible()) {
         if (aFlag)
            displayPanel.startUpdate();
         else
            displayPanel.stopUpdate();
      }
      super.setVisible( aFlag );
   }

   /**
    * Refreshes the panel by updating the status element associated with
    * the conveyor status.  The periodic update of the graphical elements
    * of the panel is controlled by a separate timer within the contained
    * graphics display panel.
    */
   @Override
   public void refreshPanel() {
      updateStatus();
   }

   /**
    * Updates the status element for this diagnostics panel.  If any of
    * the web objects has a value of 'fault', an error is declared.
    */
   private void updateStatus() {
      int faultCount = Integer.valueOf( db.getValue(
            "SELECT COUNT(*) AS num FROM webObjects " +
            "WHERE area = '" + area + "' " +
            "AND value = 'fault'",
            "-1" ) );
      if (faultCount == 0)
         statusElement.setStatus( Status.OK );
      else if (faultCount > 0)
         statusElement.setStatus( Status.Error );
      else
         statusElement.setStatus( Status.Other );
   }
}
