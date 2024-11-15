/*
 * EventPanel.java
 * 
 * (c) 2007--2010, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.RDSStatusElement;


/**
 * A dashboard panel for displaying active and recently completed system
 * events.
 */
public class EventPanel
      extends RDSDashboardPanel {
   /** Indicator for the type of the counter subpanel */
   private enum SubpanelType { Active, EventLog };

   private JTabbedPane tabs;
   private RDSStatusElement statusElement;

   /**
    * Constructs a dashboard panel for displaying currently active and
    * recently completed events.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public EventPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "System Events" );
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );
      tabs.add( "Active Events",
            new EventSubpanel( SubpanelType.Active ) );
      tabs.add( "Recently Completed Events",
            new EventSubpanel( SubpanelType.EventLog ) );
      tabs.add( "Event Editor", new EventEditorSubpanel() );

      this.add( tabs, BorderLayout.CENTER );

      statusElement = new RDSStatusElement( "System Events" );
      if (parentContainer instanceof RDSDashboard) {
         ((RDSDashboard)parentContainer).registerStatusElement( this,
               statusElement );
         statusElement.getLabel().addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
               setSelectedTab( "Active Events" );
            }
         } );
      }
   }

   /**
    * Sets the currently selected tab.
    * 
    * @param   title  the name of the desired tab
    */
   public void setSelectedTab( String title ) {
      tabs.setSelectedIndex( tabs.indexOfTab( title ) );
   }

   /**
    * Refreshes the panel contents.  The status element is always updated;
    * panel contents are updated only when the panel is currently visible.
    */
   @Override
   public void refreshPanel() {
      updateStatusElement();

      if (!isVisible())
         return;

      Component c = tabs.getSelectedComponent();
      if (c instanceof EventSubpanel)
         ((EventSubpanel)c).refresh();
   }

   private void updateStatusElement() {
      String countStr = db.getValue(
            "SELECT COUNT(*) FROM events " +
            "WHERE state = 'on' " +
            "AND severity = 0",
            "0" );
      if (Integer.valueOf( countStr ) > 0)
         statusElement.setStatus( RDSStatusElement.Status.Error );
      else
         statusElement.setStatus( RDSStatusElement.Status.OK );
   }

   /**
    * A subpanel containing the events/eventLog table information.
    */
   class EventSubpanel
         extends JPanel
         implements ActionListener {
      private static final int PADDING = EventPanel.PADDING;
      private static final int SPACING = EventPanel.SPACING;
      private static final int NUMBER_INITVAL = 50;
      private static final int NUMBER_STEP = 10;

      private SubpanelType type;
      private RDSTable eventTable;
      private JSpinner numberSpinner;
      private JButton refreshButton;
      private JButton printButton;

      /**
       * Constructs a subpanel that holds the events table.
       * 
       * @param   type  an indicator of the subpanel type, either
       *          {@code Active} or {@code EventLog}
       */
      public EventSubpanel( SubpanelType type ) {
         this.type = type;

         setName( "Event Subpanel [" + type.toString() + "]" );

         createUI();

         // refresh when this panel becomes visible
         addComponentListener( new ComponentAdapter() {
            public void componentShown( ComponentEvent evt ) {
               refresh();
            }
         } );
      }

      /**
       * Handles actions performed on the UI elements of this panel.
       */
      @Override
      public void actionPerformed( ActionEvent evt ) {
         if (evt.getSource() == refreshButton)
            refresh();
         else if (evt.getSource() == printButton)
            print();
      }

      /**
       * Creates the user interface for the subpanel.
       */
      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );

         // top panel for spinner and buttons
         JPanel topPanel = new JPanel();
         topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.X_AXIS ) );
         topPanel.add( Box.createHorizontalGlue() );

         if (type == SubpanelType.EventLog) {
            JPanel numberPanel = new JPanel( new BorderLayout() );
            numberPanel.add( new JLabel( "Recent events:", JLabel.CENTER ),
                  BorderLayout.NORTH );
            numberSpinner = new JSpinner();
            SpinnerNumberModel model =
                  (SpinnerNumberModel)numberSpinner.getModel();
            model.setMinimum( 0 );
            model.setValue( NUMBER_INITVAL );
            model.setStepSize( NUMBER_STEP );
            ((JSpinner.NumberEditor)numberSpinner.getEditor()).getTextField().
                  setEditable( false );
            numberPanel.add( numberSpinner );
            numberPanel.setMaximumSize( numberPanel.getPreferredSize() );
            topPanel.add( numberPanel );
            topPanel.add( Box.createHorizontalStrut( SPACING ) );
         }
         refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( this );
         printButton = new JButton( "Print" );
         printButton.addActionListener( this );

         topPanel.add( refreshButton );
         topPanel.add( Box.createHorizontalStrut( SPACING ) );
         topPanel.add( printButton );
         this.add( topPanel, BorderLayout.NORTH );

         // main table
         String[] columnNames = null;
         if (type == SubpanelType.Active)
            columnNames = new String[] { "Severity", "Description",
                  "Start Time" };
         else if (type == SubpanelType.EventLog)
            columnNames = new String[] { "Severity", "Description",
                  "Start Time", "Duration" };
         eventTable = new RDSTable( db, columnNames );
         eventTable.setColumnWidths( "Severity",     60,  60,  60 );
         eventTable.setColumnWidths( "Description",  -1, 300,  -1 );
         eventTable.setColumnWidths( "Start Time",  100, 125, 150 );
         eventTable.setColumnAlignment( "Start Time", SwingConstants.CENTER );
         if (type == SubpanelType.EventLog) {
            eventTable.setColumnWidths( "Duration",  50, 100, 125 );
            eventTable.setColumnAlignment( "Duration",
                  SwingConstants.CENTER );
            eventTable.getColumn( "Duration" ).setCellRenderer(
                  createDurationCellRenderer() );
         }
         eventTable.getColumn( "Severity" ).setCellRenderer(
               createSeverityCellRenderer() );
         this.add( eventTable.getScrollPane(), BorderLayout.CENTER );
      }

      /**
       * Refreshes the event/event-log display.
       */
      public void refresh() {
         String query;
         if (type == SubpanelType.Active)
            query = "SELECT severity, description, start FROM events " +
                  "WHERE state = 'on' " +
                  "ORDER BY start DESC";
         else if (type == SubpanelType.EventLog)
            query = "SELECT evt.severity, evt.description, log.start, " +
                  "log.duration " +
                  "FROM events AS evt, eventLog AS log " +
                  "WHERE log.code = evt.code " +
                  "AND log.state = 'off' " +
                  "ORDER BY start DESC " +
                  "LIMIT " + numberSpinner.getValue();
         else
            query = "";
         try {
            eventTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: sql error during table refresh, query = [%s]",
                  getName(), query );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Prints the contents of the event/event-log table.
       */
      public void print() {
         try {
            eventTable.print( "System Events, " +
                  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(
                  new java.util.Date() ), "{0}" );
         } catch (java.awt.print.PrinterException ex) {
            RDSUtil.alert( "%s: error during printing", getName() );
            RDSUtil.alert( ex );
         }
      }

      private RDSTableCellRenderer createSeverityCellRenderer() {
         RDSTableCellRenderer.IconRenderer renderer =
               new RDSTableCellRenderer.IconRenderer();
         renderer.mapIcon( "0", RDSUtil.createImageIcon(
               RDSDashboard.class, "images/red.png" ) );
         renderer.mapIcon( "1", RDSUtil.createImageIcon(
               RDSDashboard.class, "images/yellow.png" ) );
         renderer.mapIcon( "default", RDSUtil.createImageIcon(
               RDSDashboard.class, "images/gray.png" ) );
         return renderer;
      }

      private RDSTableCellRenderer createDurationCellRenderer() {
         RDSTableCellRenderer renderer = new RDSTableCellRenderer() {
            @Override
            public void setValue( Object value ) {
               if (value == null || !(value instanceof Number)) {
                  setText( "" );
                  return;
               }
               int val = ((Number)value).intValue();
               String durationStr = "";
               int d = val/86400;
               if (d > 0)
                  durationStr += d + "d ";
               int h = (val/3600) % 24;
               if (d > 0 || h > 0)
                  durationStr += h + "h ";
               int m = (val/60) % 60;
               if (d > 0 || h > 0 || m > 0)
                  durationStr += m + "m ";
               durationStr += (val % 60) + "s";
               setText( durationStr );
            }
         };
         return renderer;
      }

   }

   /**
    * A subpanel for displaying and editing information about system events.
    */
   private class EventEditorSubpanel
         extends JPanel {
      private static final int GAP = 5;
      protected final Font titleFont = UIManager.getFont(
            "Panel.font" ).deriveFont( Font.BOLD, 14.0f );

      private RDSTable eventsTable;

      /**
       * Constructs a subpanel for editing event information.
       */
      public EventEditorSubpanel() {
         super();
         setName( "EventSubpanel [Edit]" );
         createUI();
         refresh();
      }

      /**
       * Creates the user interface for this subpanel.
       */
      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );

         add( createHeaderPanel(), BorderLayout.NORTH );
         add( createEventsTable(), BorderLayout.CENTER );
      }

      private JPanel createHeaderPanel() {
         JLabel titleLabel = new JLabel( "Event Editor", JLabel.CENTER );
         titleLabel.setFont( titleFont );

         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refresh();
            }
         });

         JPanel headerPanel = new JPanel();
         headerPanel.setLayout( new BoxLayout( headerPanel,
               BoxLayout.X_AXIS ) );
         headerPanel.add( Box.createHorizontalGlue() );
         headerPanel.add( titleLabel );
         headerPanel.add( Box.createHorizontalGlue() );
         headerPanel.add( refreshButton );

         return headerPanel;
      }

      /**
       * Creates and configures the table for displaying event information.
       * 
       * @return  the table's enclosing scroll pane
       */
      private Component createEventsTable() {
         eventsTable = new RDSTable( db,
               "Event Code", "Description", "Severity" );
         eventsTable.setColumnToolTips(
               "Internal event code", "Event description",
               "Event severity (0 - Error, 1 - Warning)" );

         eventsTable.setColumnWidths( "Event Code", 50, 75, 100 );
         eventsTable.setColumnWidths( "Severity",   50, 75, 100 );
         eventsTable.setColumnAlignment( "Severity", SwingConstants.CENTER );

         eventsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

         eventsTable.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getButton() == MouseEvent.BUTTON1 &&
                     evt.getClickCount() == 2) {
                  String selectedEvent = (String)eventsTable.getValueAt(
                        eventsTable.rowAtPoint( evt.getPoint() ),
                        "Event Code" );
                  editEvent( selectedEvent );
               }
            }
         } );

         return eventsTable.getScrollPane();
      }

      /**
       * Updates the contents of the events table.
       */
      private void updateEventsTable() {
         String query =
               "SELECT code, description, severity FROM events " +
               "ORDER BY code";
         try {
            eventsTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: error populating events table, query = [%s]",
                  getName(), query );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Edits the selected event, allowing modification of the description
       * and severity level.
       * 
       * @param   code  the event code to edit
       */
      private void editEvent( String code ) {
         if (code == null || code.length() == 0)
            return;
         if (!admin.isAuthenticatedInteractive( "edit events",
               EventPanel.this ))
            return;

         boolean success = showEventDialog( code );

         if (success) {
            admin.log( getName() + ": modified event [" + code + "]" );
            refresh();
         }
      }

      /**
       * Displays a dialog for editing event information.
       * 
       * @param   code  the event code
       * @return  {@code true} if the event was successfully modified,
       *          {@code false} otherwise
       */
      private boolean showEventDialog( String code ) {
         final int fieldWidth = 30;
         final Dimension spinnerSize = new Dimension( 40, 20 );

         String title = "Edit Event";
         Map<String,String> recordMap = db.getRecordMap(
               "SELECT * FROM events " +
               "WHERE code = '" + code + "'" );

         JTextField codeField = new JTextField( code, fieldWidth );
         codeField.setEditable( false );

         JTextField descriptionField = new JTextField(
               (recordMap == null || recordMap.get( "description" ) == null) ?
               "" : recordMap.get( "description" ),
               fieldWidth );

         JSpinner severitySpinner = new JSpinner();
         severitySpinner.setPreferredSize( spinnerSize );
         severitySpinner.setValue(
               (recordMap == null || recordMap.get( "severity" ) == null) ?
               0 : Integer.valueOf( recordMap.get( "severity" ) ) );
         JPanel severitySpinnerPanel = new JPanel(
               new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
         severitySpinnerPanel.add( severitySpinner );

         JPanel eventPanel = new JPanel( new SpringLayout() );
         eventPanel.add( new JLabel( "Event code: ", JLabel.RIGHT ) );
         eventPanel.add( codeField );
         eventPanel.add( new JLabel( "Description: ", JLabel.RIGHT ) );
         eventPanel.add( descriptionField );
         eventPanel.add( new JLabel( "Severity level: ", JLabel.RIGHT ) );
         eventPanel.add( severitySpinnerPanel );
         SpringUtilities.makeCompactGrid( eventPanel, 3, 2,
               GAP, GAP, GAP, GAP );

         JPanel containerPanel = new JPanel(
               new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
         containerPanel.add( eventPanel );

         int returnValue = JOptionPane.showConfirmDialog(
               EventPanel.this,               // parent component
               containerPanel,                // message object
               title,                         // dialog title
               JOptionPane.OK_CANCEL_OPTION,  // option type
               JOptionPane.PLAIN_MESSAGE      // message type
               );

         if (returnValue != JOptionPane.OK_OPTION)
            return false;

         String errMsg = null;

         // input check
         String newDescription = descriptionField.getText().trim();
         if (newDescription == null || newDescription.isEmpty())
            errMsg = "No event description provided";

         if (errMsg != null) {
            JOptionPane.showMessageDialog( this, errMsg, "Input Error",
                  JOptionPane.ERROR_MESSAGE );
            return false;
         }

         int rows = db.execute(
               "UPDATE events SET " +
               "description = '" + descriptionField.getText().trim() + "', " +
               "severity = " + (Integer)severitySpinner.getValue() + " " +
               "WHERE code = '" + code + "'" );

         return (rows > 0);
      }

      /**
       * Refreshes the contents of the events table.
       */
      protected void refresh() {
         updateEventsTable();
      }
   }

}
