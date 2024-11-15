package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import java.sql.SQLException;
import java.text.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for modifying configuration parameters.
 */
public class ConfigPanel
      extends RDSDashboardPanel {
   private JTabbedPane tabs;

   /**
    * Constructs a panel for modifying configuration parameters.
    * 
    * @param   parentContainer  the parent container of this panel
    */
   public ConfigPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Control Parameters" );
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );

      createControlsTabs();

      this.add( tabs, BorderLayout.CENTER );
   }

   /**
    * Creates tabs for modifying configuration paramters; a tab is
    * generated for each distinct zone in the 'controls' table.
    */
   private void createControlsTabs() {
      List<String> zoneList = db.getValueList(
            "SELECT DISTINCT host FROM controls " +
            "WHERE editable = 'yes' " +
            "ORDER BY host" );

      if (zoneList != null)
         for (String host : zoneList)
            tabs.add( host, new ConfigHostSubpanel( host ) );
   }


   private class ConfigHostSubpanel
         extends JPanel {
      private JTabbedPane hostTabs;

      public ConfigHostSubpanel(String host) {
         setLayout( new BorderLayout() );
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );

         hostTabs = new JTabbedPane( JTabbedPane.TOP,
               JTabbedPane.SCROLL_TAB_LAYOUT );
         hostTabs.setFocusable( false );

         List<String> zoneList = db.getValueList(
               "SELECT DISTINCT zone FROM controls " +
               "WHERE editable = 'yes' " +
               "AND host = '%s' " +
               "ORDER BY zone",
               host );

         if (zoneList != null)
            for (String zone : zoneList) {
               hostTabs.add( zone, new ConfigSubpanel( host, zone ) );
            }
         this.add( hostTabs, BorderLayout.CENTER );
      }
   }

   /**
    * Subpanel containing the modifiable configuration parameters for a
    * given zone.
    */
   private class ConfigSubpanel
         extends JPanel
         implements ActionListener {
      private String zone;
      private String host;
      private RDSTable paramTable;

      /**
       * Constructs a subpanel for modifying parameters for a given zone.
       * 
       * @param   zone  the zone (program) designation for this subpanel
       */
      public ConfigSubpanel( String host,String zone ) {
         this.host = host ;
         this.zone = zone;
         setName( "Config Subpanel [" + host + "] [" + zone + "]" );

         createUI();
         refresh();
      }

      /**
       * Handles actions performed on components within this subpanel.
       */
      public void actionPerformed( ActionEvent evt ) {
         if (evt.getActionCommand().equals( "Refresh" ))
            refresh();
         else if (evt.getActionCommand().equals( "Print" ))
            print();
      }

      /**
       * Creates the user interface for the subpanel.  The parameter
       * table is also created and configured.
       */
      private void createUI() {
         setLayout( new BorderLayout() );
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );

         // top panel for buttons
         JPanel topPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( this );
         topPanel.add( refreshButton );
         JButton printButton = new JButton( "Print" );
         printButton.addActionListener( this );
         topPanel.add( printButton );
         this.add( topPanel, BorderLayout.NORTH );

         // main table
         paramTable = new RDSTable( db,
               "Name", "Description", "Value", "Changed" );
         paramTable.setColumnWidths( "Name",         50, 100, 150 );
         paramTable.setColumnWidths( "Description",  -1, 300,  -1 );
         paramTable.setColumnWidths( "Value",        50, 125,  -1 );
         paramTable.setColumnWidths( "Changed",     125, 125, 125 );
         paramTable.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getClickCount() == 2) {
                  if (!admin.isAuthenticatedInteractive(
                        "modify control parameters", ConfigSubpanel.this ))
                     return;

                  int row = paramTable.rowAtPoint( evt.getPoint() );
                  String name = (String)paramTable.getValueAt(
                        row, "Name" );
                  String value = (String)paramTable.getValueAt(
                        row, "Value" );
                  String newValue = update( name, value );
                  if (newValue != null)
                     admin.log( getName() + ": [" + name + "] changed from " +
                           "[" + value + "] to [" + newValue + "]" );
               }
            }
         } );
         this.add( paramTable.getScrollPane(), BorderLayout.CENTER );
      }

      /**
       * Reloads the current values into the parameter table.
       */
      private void refresh() {
         String query =
               "SELECT name, description, value, stamp FROM controls " +
               "WHERE zone = '" + zone + "' " +
               "AND host = '" + host + "' " +
               "AND editable = 'yes' " +
               "ORDER BY name";
         try {
            paramTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: sql error during table refresh, query = [%s]",
                  getName(), query );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Prints the contents of the parameter table.
       */
      public void print() {
         try {
            paramTable.print( "Configuration Parameters [" + zone + "], " +
                  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(
                  new java.util.Date() ), "{0}" );
         } catch (java.awt.print.PrinterException ex) {
            RDSUtil.alert( "%s: error during printing", getName() );
         }
      }

      /**
       * Updates a single parameter.
       * 
       * @param   name   the parameter name
       * @param   value  the current value of the parameter
       * @return  the new value or <tt>null</tt> if the value is not changed
       */
      public String update( String name, String value ) {
         String newValue = (String)JOptionPane.showInputDialog(
               this,                                 // parent component
               "Enter new value for " + name + ":",  // message text
               "Modify Parameter",                   // title text
               JOptionPane.QUESTION_MESSAGE,         // message type
               null,                                 // icon
               null,                                 // selection values
               value                                 // initial value
               );
         if (newValue == null || newValue.trim().length() == 0)
            return null;

         newValue = newValue.trim();
         int numRows = db.execute(
            "UPDATE controls " +
            "SET value = '" + newValue + "' " +
            "WHERE zone = '" + zone + "' " +
            "AND host = '" + host + "' " +
            "AND name = '" + name + "'" );

         if (numRows != 1)
            newValue = null;

         refresh();
         return newValue;
      }
   }  // end TuningSubpanel inner class

}  // end TuningPanel class
