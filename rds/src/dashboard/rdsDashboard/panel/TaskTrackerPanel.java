/*
 * TaskTrackerPanel.java
 * 
 * (c) 2007-2010, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

import com.lavantech.gui.comp.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboad panel to provide an interface for tracking tasks.
 */
public class TaskTrackerPanel
      extends RDSDashboardPanel {
   
   // constants
   private static final Color ALERT_COLOR = Color.RED;
   private static final Color ALT_ALERT_COLOR = new Color( 232, 0, 0 );
   private static final Color ALERT_TEXT_COLOR = Color.WHITE;
   private static final int GAP = 10;
   private static final int BIG_GAP = 20;
   private static final Dimension PICKER_SIZE = new Dimension( 165, 20 );

   private JComboBox locationCombo;
   private JComboBox taskCombo;
   private DateTimePicker startTimePicker, endTimePicker;
   private JLabel goalLabel;
   private JLabel durationLabel;
   private RDSTable taskTable;

   /**
    * Constructs a panel to track operator tasks.
    * 
    * @param   parentContainer  the parent container of this panel
    */
   public TaskTrackerPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Task Tracker" );
      setDescription( "Track employee efficiency" );

      createUI();
   }

   /**
    * Creates the user interface for the panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( true );

      add( createHeaderPanel(), BorderLayout.NORTH);
      add( createTaskPanel(), BorderLayout.CENTER);
   }

   /**
    * Creates a panel for monitoring tasks.
    */
   private JScrollPane createTaskPanel() {
      taskTable = new RDSTable( db, "Operator", "Total", "Total/Min", " ",
                                    "Defects", "Locations", "Highlight" );

      taskTable.setColumnWidths( "Operator",  100,  60, 150);
      taskTable.setColumnWidths( "Total",      50,  50,  50);
      taskTable.setColumnWidths( "Total/Min",  60,  60,  60);
      taskTable.setColumnWidths( " ",         100, 100, 100);
      taskTable.setColumnWidths( "Defects",    50,  50,  50);
      taskTable.setColumnWidths( "Highlight",   0,   0,   0 );  // hidden

      taskTable.getColumn( " " ).setCellRenderer( 
            new ProgressColumnRenderer( this ) );

      return taskTable.getScrollPane();
   }

   /** Creates the header panel. */
   private JPanel createHeaderPanel() {
      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.Y_AXIS) );

      JPanel p1 = new JPanel();
      p1.setLayout( new BoxLayout( p1, BoxLayout.X_AXIS ) );

      JPanel p2 = new JPanel();
      p2.setLayout( new BoxLayout( p2, BoxLayout.Y_AXIS ) );

      JLabel l = new JLabel( "Location", JLabel.CENTER );
      l.setAlignmentX( Component.CENTER_ALIGNMENT );
      locationCombo = new JComboBox();
      populateCombo( locationCombo, "SELECT location FROM taskLocations " +
                                    "ORDER BY location");
      locationCombo.addItem( "All pick zones" );
      locationCombo.setMaximumSize( locationCombo.getPreferredSize() );
      p2.add( l );
      p2.add( locationCombo );

      p1.add( p2 );

      p2 = new JPanel();
      p2.setLayout( new BoxLayout( p2, BoxLayout.Y_AXIS ) );

      l = new JLabel( "Task", JLabel.CENTER );
      l.setAlignmentX( Component.CENTER_ALIGNMENT );
      taskCombo = new JComboBox();
      populateCombo( taskCombo, "SELECT task FROM tasks " +
                                "ORDER BY task" );
      taskCombo.setMaximumSize( taskCombo.getPreferredSize() );
      p2.add( l );
      p2.add( taskCombo );

      p1.add( Box.createHorizontalStrut( BIG_GAP ) );
      p1.add( p2 );

      headerPanel.add( p1 );

      JButton trackButton = new JButton( "Track" );
      trackButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            refreshTaskTable();
         }
      } );

      JButton printButton = new JButton( "Print" );
      printButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            print();
         }
      } );

      p1 = new JPanel();
      p1.setLayout( new BoxLayout( p1, BoxLayout.X_AXIS ) );
      createDateTimePickers( p1 );
      p1.add( Box.createHorizontalStrut( GAP ) );
      p1.add( trackButton );
      p1.add( Box.createHorizontalGlue() );
      p1.add( printButton );

      headerPanel.add( Box.createVerticalStrut( GAP ) );
      headerPanel.add( p1 );

      p1 = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      p1.add( goalLabel = new JLabel( "Task Goal:" ) );
      p1.add( Box.createHorizontalStrut( BIG_GAP ) );
      p1.add( durationLabel = new JLabel( "Duration:" ) );

      headerPanel.add( Box.createVerticalStrut( GAP ) );
      headerPanel.add( p1 );
      headerPanel.add( Box.createVerticalStrut( GAP ) );

      return headerPanel;
   }

   /**
    * Fills in the selections for the machine combobox.
    */
   private void populateCombo( JComboBox combo, String sql ) {
      List<String> list = db.getValueList( sql );
      combo.removeAllItems();
      if (list != null)
         for (String item : list )
            combo.addItem( item );
   }


   /**
    * Updates the tasks table.
    */
   private void refreshTaskTable() {
      String locationClause = "";
      if ( locationCombo.getSelectedItem().equals("All pick zones") )
         locationClause = "AND location LIKE 'Pick zone%' ";
      else
         locationClause = "AND location='" + 
                          locationCombo.getSelectedItem() + "' "; 

      SimpleDateFormat dateFormat = new SimpleDateFormat(
                  "yyyy-MM-dd HH:mm:ss" );
      String startTime = dateFormat.format( startTimePicker.getDate() );
      String endTime = dateFormat.format( endTimePicker.getDate() );
      String timeClause = " AND taskTracker.stamp > '" + startTime + "' " +
                          "AND taskTracker.stamp <= '" + endTime + "' ";

     
      String task = taskCombo.getSelectedItem().toString();
      String goal = db.getValue(
            "SELECT goal FROM tasks WHERE task='" + task + "'",
            "1.0" );
      goalLabel.setText( "Task Goal: " + goal + "/min" );
      long duration = (endTimePicker.getDate().getTime() -
            startTimePicker.getDate().getTime()) / 1000 / 60;
      durationLabel.setText( "Duration: " + duration + " min" ); 
      String query =
            "SELECT " +
            "taskTracker.operator, SUM(value), " +
            "SUM(value)/" + duration + ", " +
            "ROUND(SUM(value)/" + duration + "/" + goal + "*100.0), " +
            "0, location, 0 " +
            "FROM taskTracker,taskOperators " +
            "WHERE taskTracker.operator=taskOperators.operator " +
            "AND taskTracker.task='" + task + "' " +
            locationClause +
            timeClause +
            "GROUP BY operator " +
            "ORDER BY operator";
      try {
         taskTable.populateTable( query );
      } catch (SQLException ex) {
         RDSUtil.alert( getName() + ": error populating counters table, " +
               "query = [" + query + "]" );
         RDSUtil.alert( ex );
      }
   }


   /**
    * A custom table cell renderer that highlights a row under certain
    * conditions.
    */
   class TaskTableCellRenderer
         extends RDSTableCellRenderer {
      SimpleDateFormat dateFormatter;

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column ) {
         super.getTableCellRendererComponent( table, value, isSelected,
               hasFocus, row, column );

         // override background colors to show alert status
         long highlightVal = (Long)((RDSTable)table).getValueAt(
               row, "Highlight" );
         if (highlightVal == 1L) {
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row ))
               setBackground( TaskTrackerPanel.ALT_ALERT_COLOR );
            else
               setBackground( TaskTrackerPanel.ALERT_COLOR );
            setForeground( TaskTrackerPanel.ALERT_TEXT_COLOR );
         }

         return this;
      }

      @Override
      protected void setValue( Object value ) {
         if (value instanceof java.util.Date) {
            if (dateFormatter == null)
               dateFormatter = new SimpleDateFormat( "yyyy-mm-dd HH:mm:ss" );
            setText( (value == null) ? "" : dateFormatter.format( value ) );
         } else
            setText( (value == null) ? "" : value.toString() );
      }
   }  // end TaskTracker.TaskTableCellRenderer class


   /**
    * Class for rendering the "Progress" column with a progress bar.
    */
   private class ProgressColumnRenderer
         extends JProgressBar
         implements TableCellRenderer {

      public ProgressColumnRenderer( TaskTrackerPanel processPanel ) {
         super( );
         // setHorizontalAlignment( JProgressBar.CENTER );
         // setMargin( new Insets( 1, 1, 1, 1 ) );
         setForeground( Color.RED );
         setMinimum( 0 );
         setMaximum( 100 );
         setValue( 0 );
      }

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column ) {

         double val = (Double)((RDSTable)table).getValueAt( row, " " );
         Double d = new Double( val );
         // setValue( val );
         setValue( d.intValue() );

         // set background color
         long highlightVal = (Long)((RDSTable)table).getValueAt(
               row, "Highlight" );
         if (isSelected) {
            setBackground( table.getSelectionBackground() );
         } else if (highlightVal == 1L) {
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row ))
               setBackground( TaskTrackerPanel.ALT_ALERT_COLOR );
            else
               setBackground( TaskTrackerPanel.ALERT_COLOR );
         } else {
            Color c = table.getBackground();
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row )) {
               int r = Math.max( c.getRed()   - 15, 0 );
               int g = Math.max( c.getGreen() - 15, 0 );
               int b = Math.max( c.getBlue()  - 15, 0 );
               setBackground( new Color( r, g, b ) );
            } else {
               setBackground( c );
            }
         }

         return this;
      }
   }  // end TaskTracker.ProgressColumnRenderer class


   private void print() {
      String header = "Task Tracker";

      try {
         taskTable.print( header, "{0}" );
      } catch (java.awt.print.PrinterException ex) {
         RDSUtil.alert( getName() + ": error during printing" );
      } catch (Exception ex) {}
   }


   /**
    * Creates and configures the date/time selectors.
    *
    * @param   container  the container to which the selectors should
    *          be added
    */
   private void createDateTimePickers( Container container ) {
      GregorianCalendar cal = new GregorianCalendar();

      JLabel startLabel = new JLabel( "Start: ", JLabel.RIGHT );

      // initialize to start of previous hour
      cal.add( GregorianCalendar.HOUR, -1 );
      startTimePicker = new DateTimePicker( cal, "MMM d, yyyy, h:mm:ss aa" );
      startTimePicker.setMinimumSize( PICKER_SIZE );
      startTimePicker.setPreferredSize( PICKER_SIZE );
      startTimePicker.setMaximumSize( PICKER_SIZE );
      startTimePicker.setDisplayTodayButton( true );
      startTimePicker.getTimePanel().setDisplayAnalog( false );
      startTimePicker.getTimePanel().setMinDisplayed( true );
      startTimePicker.getTimePanel().setSecDisplayed( true );
      startTimePicker.setEditable( true );

      JLabel endLabel = new JLabel( "End: ", JLabel.RIGHT );

      cal.add( GregorianCalendar.HOUR, 1 );
      endTimePicker = new DateTimePicker( cal, "MMM d, yyyy, h:mm:ss aa" );
      endTimePicker.setMinimumSize( PICKER_SIZE );
      endTimePicker.setPreferredSize( PICKER_SIZE );
      endTimePicker.setMaximumSize( PICKER_SIZE );
      endTimePicker.setDisplayTodayButton( true );
      endTimePicker.getTimePanel().setDisplayAnalog( false );
      endTimePicker.getTimePanel().setMinDisplayed( true );
      endTimePicker.getTimePanel().setSecDisplayed( true );
      endTimePicker.setEditable( true );

      JPanel p = new JPanel( new SpringLayout() );
      p.add( startLabel );
      p.add( startTimePicker );
      p.add( endLabel );
      p.add( endTimePicker );
      SpringUtilities.makeCompactGrid( p, 2, 2, 5, 5, 5, 5 );

      container.add( p );
   }

}  // end TaskTracker class
