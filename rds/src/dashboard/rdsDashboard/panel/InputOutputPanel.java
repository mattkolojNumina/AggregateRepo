package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for viewing, setting, and forcing inputs and outputs.
 * The panel manipulates the contents of the 'trak' database table; all
 * low-level input/output changes are managed by the {@code trakd} program
 * and the underlying TRAK engine and drivers. 
 */
public class InputOutputPanel
      extends RDSDashboardPanel {
   private static final int REFRESH_DELAY = 1000;  // msec

   private JTabbedPane tabs;
   private RDSTableCellRenderer.IconRenderer iconRenderer;
   private RDSTableCellRenderer.BooleanRenderer forceRenderer;

   /**
    * Constructs a panel for viewing, setting, and forcing inputs and
    * outputs.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public InputOutputPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Input/Output Viewer" );
      setDescription( "View and set the state of inputs and outputs" );

      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );

      createRenderers();
      createTrakTabs();

      this.add( tabs, BorderLayout.CENTER );

      //create a timer to perform periodic refreshes
      Timer timer = new Timer( REFRESH_DELAY, new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            refreshPanel();
         }
      } );
      timer.setInitialDelay( 0 );
      timer.start();
   }

   /**
    * Creates tabs for tuning trak paramters; a tab is generated for
    * each distinct area in the 'trak' table.
    */
   private void createTrakTabs() {
      List<Map<String,String>> hostAreaList = db.getResultMapList(
            "SELECT DISTINCT host,area FROM trak " +
            "WHERE zone = 'dp' " +
            "AND area <> '' " +
            "ORDER BY host,area" );
      if (hostAreaList != null)
         for (Map<String,String> hostArea : hostAreaList) {
            String host = hostArea.get( "host" );
            String area = hostArea.get( "area" );
            tabs.add( host + ":" + area, new IOSubpanel( host,area ) );
         }
   }

   /**
    * Create custom table cell renderers for the "State" and "Force"
    * columns.
    */
   private void createRenderers() {
      iconRenderer = new RDSTableCellRenderer.IconRenderer();
      iconRenderer.mapIcon( "default", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/red.png" ) );
      iconRenderer.mapIcon( "0", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/gray.png" ) );
      iconRenderer.mapIcon( "1", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/green.png" ) );

      forceRenderer = new RDSTableCellRenderer.BooleanRenderer( "yes" );
   }

   /**
    * Refreshes the currently selected tab.
    */
   @Override
   public void refreshPanel() {
      if (!isVisible())
         return;

      Component c = tabs.getSelectedComponent();
      if (c instanceof IOSubpanel)
         ((IOSubpanel)c).refresh();
   }

   /**
    * Subpanel containing the inputs and outputs for a given area.
    */
   private class IOSubpanel
         extends JPanel
         implements ActionListener {
      private String host;
      private String area;
      private RDSTable ioTable;

      /**
       * Constructs a subpanel for viewing and/or setting the inputs
       * and outputs for a given area.
       * 
       * @param   area  the area designation for this subpanel
       */
      public IOSubpanel( String host, String area ) {
         this.host = host;
         this.area = area;
         setName( "I/O Subpanel [" + host + ":" + area + "]" );

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
       * Creates the user interface for the subpanel.  The i/o
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
         ioTable = new RDSTable( db,
               "Name", "Description", "State", "Force", "Changed" );
         ioTable.setColumnWidths( "Name",         50, 100, 150 );
         ioTable.setColumnWidths( "Description",  -1, 300,  -1 );
         ioTable.setColumnWidths( "State",        45,  45,  45 );
         ioTable.setColumnWidths( "Force",        45,  45,  45 );
         ioTable.setColumnWidths( "Changed",     125, 125, 125 );

         ioTable.getColumn( "State" ).setCellRenderer( iconRenderer );
         ioTable.getColumn( "Force" ).setCellRenderer( forceRenderer );

         ioTable.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
               Point p = evt.getPoint();
               handleTableClick(
                     ioTable.rowAtPoint( p ),
                     ioTable.columnAtPoint( p ) );
            }
         } );
         this.add( ioTable.getScrollPane(), BorderLayout.CENTER );
      }

      /**
       * Reloads the current values into the i/o table.
       */
      private void refresh() {
         // TODO update force column when database structure changes
         String query =
               "SELECT name, description, `get`, 'no', stamp FROM trak " +
               "WHERE zone = 'dp' " +
               "AND `host` = '" + host + "' " +
               "AND area = '" + area + "' " +
               "ORDER BY name";
         try {
            ioTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: sql error during table refresh, query = [%s]",
                  getName(), query );  
            RDSUtil.alert( ex );
         }
      }

      /**
       * Saves the current parameter values.
       */
      private void save() {
         if (!admin.isAuthenticatedInteractive( "update trak io", this ))
            return;

         db.execute(
               "UPDATE trak " +
               "SET state = 'save' " +
               "WHERE zone = 'dp' " +
               "AND `host` = '" + host + "' " +
               "AND area = '" + area + "'" );
      }

      /**
       * Prints the contents of the table.
       */
      public void print() {
         try {
            ioTable.print( "Input/Output Values [" + host + ":" + area + "], " +
                  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(
                  new java.util.Date() ), "{0}" );
         } catch (java.awt.print.PrinterException ex) {
            RDSUtil.alert( "%s: error during printing", getName() );
            RDSUtil.alert( ex );
         }
      }

      private void handleTableClick( int row, int col ) {
         if (!admin.isAuthenticatedInteractive(
               "update trak io", IOSubpanel.this ))
            return;

         if (col == ioTable.getColumnModel().getColumnIndex( "State" ))
            toggleState( row );
         else if (col == ioTable.getColumnModel().getColumnIndex( "Force" ))
            toggleForce( row );
         else
            return;

         refresh();
      }

      private void toggleState( int row ) {
         Integer currentVal = (Integer)ioTable.getValueAt( row, "State" );
         if (currentVal == null)
            return;

         String name = (String)ioTable.getValueAt( row, "Name" );
         int newVal = (currentVal == 0) ? 1 : 0;

         db.execute(
            "UPDATE trak " +
            "SET put = " + newVal + ", " +
            "state = 'write' " +
            "WHERE zone = 'dp' " +
            "AND name = '" + name + "'" );
      }

      private void toggleForce( int row ) {
         // TODO forcing dp's is not yet implemented
         RDSUtil.trace( "%s: forcing a dp is not currently implemented",
               getName() );

/*
         String name = (String)ioTable.getValueAt( row, "Name" );
         int state = (Integer)ioTable.getValueAt( row, "State" );
         String currentVal = (String)ioTable.getValueAt( row, "Force" );
         String newVal = currentVal.equals( "no" ) ? "yes" : "no";

         db.execute(
               "UPDATE trak SET " +
               "put = '" + state + "', " +
               "force = '" + newVal + "', " +
               "state = 'write' " +
               "WHERE zone = 'dp' " +
               "AND name = '" + name + "'" );
*/
      }

   }  // end TuningPanel.TuningSubpanel class

}  // end TuningPanel class
