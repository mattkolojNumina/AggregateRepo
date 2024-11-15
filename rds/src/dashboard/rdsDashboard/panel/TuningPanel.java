package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for viewing and modifying system tuning parameters.
 */
public class TuningPanel
      extends RDSDashboardPanel {
   private List<JTabbedPane> zoneTabs;
   private JTabbedPane hostTabs;

   /**
    * Constructs a panel for tuning trak rp's.
    * 
    * @param   parentContainer  the parent container of this panel
    */
   public TuningPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Tuning Parameters" );
      setDescription( "View and modify system tuning parameters" );

      createUI();
   }

   /**
    * Creates the user interface for this panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      List<String> hosts = db.getValueList( "SELECT DISTINCT host FROM trak ORDER BY host" );
      if (hosts.size() > 1) {
         hostTabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
         add( hostTabs, BorderLayout.CENTER );
      }
      zoneTabs = new ArrayList<JTabbedPane>();

      for (String host : hosts) {
         JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP,
               JTabbedPane.SCROLL_TAB_LAYOUT );
         tabs.setFocusable( false );
         createTrakTabs( host, tabs );
         zoneTabs.add( tabs );

         if (hosts.size() > 1)
            hostTabs.add( host, tabs );
         else
            add( tabs, BorderLayout.CENTER );
      }
   }

   /**
    * Creates tabs for tuning trak parameters; a tab is generated for
    * each distinct area in the 'trak' table.
    */
   private void createTrakTabs( String hostname, JTabbedPane tabs ) {
      List<String> areaList = db.getValueList(
            "SELECT DISTINCT area FROM trak " +
            "WHERE zone = 'rp' " +
            "AND area <> '' " +
            "AND `host` = '%s' " +
            "ORDER BY area",
            hostname );

      if (areaList != null)
         for (String area : areaList) {
            tabs.add( area, new TuningSubpanel( hostname, area ) );
         }
   }

   /**
    * Refreshes the currently selected tab.
    */
   @Override
   public void refreshPanel() {
      if (!isVisible())
         return;

      for ( int i = 0; i < zoneTabs.size(); i++ ) {
      Component c = zoneTabs.get( i ).getSelectedComponent();
      if (c instanceof TuningSubpanel)
         ((TuningSubpanel)c).refresh();
      }
   }

   /**
    * Subpanel containing the tuning table for a given area.
    */
   private class TuningSubpanel
         extends JPanel
         implements ActionListener {
      private String host;
      private String area;
      private RDSTable paramTable;

      /**
       * Constructs a subpanel for tuning parameters for a given area.
       * 
       * @param   area  the area designation for this subpanel
       */
      public TuningSubpanel( String hostname, String area ) {
         this.host = hostname;
         this.area = area;
         setName( "Tuning Subpanel [" + host + "/" + area + "]" );

         createUI();
         refresh();
      }

      /**
       * Handles actions performed on components within this subpanel.
       */
      public void actionPerformed( ActionEvent evt ) {
         if (evt.getActionCommand().equals( "Save" ))
            save();
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
         JButton saveButton = new JButton( "Save" );
         saveButton.addActionListener( this );
         topPanel.add( saveButton );
         JButton printButton = new JButton( "Print" );
         printButton.addActionListener( this );
         topPanel.add( printButton );
         this.add( topPanel, BorderLayout.NORTH );

         // main table
         paramTable = new RDSTable( db,
               "Name", "Description", "Value", "Changed" );
         paramTable.setColumnWidths( "Name",         50, 100, 150 );
         paramTable.setColumnWidths( "Description",  -1, 300,  -1 );
         paramTable.setColumnWidths( "Value",        50,  50,  75 );
         paramTable.setColumnWidths( "Changed",     125, 125, 125 );
         paramTable.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getClickCount() == 2) {
                  if (!admin.isAuthenticatedInteractive(
                        "modify trak parameters", TuningSubpanel.this ))
                     return;

                  int row = paramTable.rowAtPoint( evt.getPoint() );
                  String name = (String)paramTable.getValueAt(
                        row, "Name" );
                  String value = paramTable.getValueAt(
                        row, "Value" ).toString();
                  String newValue = update( host, name, value );
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
         String sql =
               "SELECT name, description, `get`, stamp FROM trak " +
               "WHERE zone = 'rp' " +
               "AND area = '" + area + "' " +
               "AND `host`= '" + host + "' " +
               "ORDER BY name";
         try {
            paramTable.populateTable( sql );
         } catch (SQLException ex) {
            RDSUtil.alert( getName() + ": sql error during table refresh" );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Saves the current parameter values.
       */
      private void save() {
         if (!admin.isAuthenticatedInteractive(
               "modify trak parameters", this ))
            return;

         db.execute(
               "UPDATE trak " +
               "SET state = 'save' " +
               "WHERE zone = 'rp'" );
      }

      /**
       * Prints the contents of the parameter table.
       */
      public void print() {
         try {
            paramTable.print( "Tuning Parameters [" + host + "/" + area + "], " +
                  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(
                  new java.util.Date() ), "{0}" );
         } catch (java.awt.print.PrinterException ex) {
            RDSUtil.alert( getName() + ": error during printing" );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Updates a single parameter.
       * 
       * @param   name   the parameter name
       * @param   value  the current value of the parameter
       * @return  the new value or <tt>null</tt> if the value is not changed
       */
      public String update( String hostname, String name, String value ) {
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
            "UPDATE trak " +
            "SET put = '" + newValue + "', " +
            "state = 'write' " +
            "WHERE name = '" + name + "' " +
            "AND `host`='" + hostname + "'" );

         if (numRows != 1)
            newValue = null;

         refresh();
         return newValue;
      }
   }  // end TuningPanel.TuningSubpanel class

}  // end TuningPanel class
