/*
 * TimeTrackerPanel.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.statistics.HistogramDataset;

import com.lavantech.gui.comp.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for viewing the distribution of times between two
 * production milestones.
 */
public class TimeTrackerPanel
      extends RDSDashboardPanel {

   // constants
   private static final int GAP = 10;
   private static final int BIG_GAP = 20;
   private static final double ASPECT_RATIO = 4.0 / 3.0;
   private static final int NUM_BINS = 20;
   private static final Dimension PICKER_SIZE = new Dimension( 165, 20 );

   // ui variables
   private JComboBox milestone1Combo;
   private JComboBox milestone2Combo;
   DateTimePicker startTimePicker;
   DateTimePicker endTimePicker;
   JSpinner loSpinner;
   JSpinner hiSpinner;
   private JFreeChart chart;
   private JLabel totalLabel;
   Component printableComponent;


   /**
    * Constructs a panel for viewing production timing distributions.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public TimeTrackerPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Time Tracker" );
      setDescription( "Track production times" );

      createUI();
   } // TimeTracker()

   /** Creates the user interface for this panel. */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( true );

      add( createHeaderPanel(), BorderLayout.NORTH );
      add( createHistogramPanel(), BorderLayout.CENTER );
   }

   /** Creates the header panel. */
   private JPanel createHeaderPanel() {
      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.Y_AXIS) );

      JPanel p1 = new JPanel();
      p1.setLayout( new BoxLayout( p1, BoxLayout.X_AXIS ) );

      JPanel p2 = new JPanel();
      p2.setLayout( new BoxLayout( p2, BoxLayout.Y_AXIS ) );

      JLabel l = new JLabel( "First event", JLabel.CENTER );
      l.setAlignmentX( Component.CENTER_ALIGNMENT );
      milestone1Combo = new JComboBox();
      populateCombo( milestone1Combo, "SELECT DISTINCT code FROM cartonLog " +
                                      "WHERE code<>'!' " +
                                      "ORDER BY code" );
      milestone1Combo.setMaximumSize( milestone1Combo.getPreferredSize() );
      p2.add( l );
      p2.add( milestone1Combo );

      p1.add( p2 );
      p2 = new JPanel();
      p2.setLayout( new BoxLayout( p2, BoxLayout.Y_AXIS ) );

      l = new JLabel( "Second event", JLabel.CENTER );
      l.setAlignmentX( Component.CENTER_ALIGNMENT );
      milestone2Combo = new JComboBox();
      populateCombo( milestone2Combo, "SELECT DISTINCT code FROM cartonLog " +
                                      "WHERE code<>'!' " +
                                      "ORDER BY code" );
      milestone2Combo.setMaximumSize( milestone2Combo.getPreferredSize() );
      p2.add( l );
      p2.add( milestone2Combo );

      p1.add( Box.createHorizontalStrut( BIG_GAP ) );
      p1.add( p2 );

      headerPanel.add( p1 );
      headerPanel.add( Box.createVerticalStrut( GAP ) );

      loSpinner = new JSpinner();
      ((JSpinner.DefaultEditor)loSpinner.getEditor()).getTextField().
            setColumns(5);
      loSpinner.setMaximumSize( loSpinner.getPreferredSize() );
      hiSpinner = new JSpinner();
      ((JSpinner.DefaultEditor)hiSpinner.getEditor()).getTextField().
            setColumns(5);
      hiSpinner.setMaximumSize( hiSpinner.getPreferredSize() );
      hiSpinner.setValue( 60 );
      p2 = new JPanel( new SpringLayout() );
      p2.add( new JLabel( "Minimum: ", JLabel.RIGHT ) );
      p2.add( loSpinner );
      p2.add( new JLabel( "Maximum: ", JLabel.RIGHT ) );
      p2.add( hiSpinner );
      SpringUtilities.makeCompactGrid( p2, 2, 2, 5, 5, 5, 5 );

      JButton trackButton = new JButton( "Track" );
      trackButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            track();
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
      p1.add( p2 );
      p1.add( Box.createHorizontalStrut( GAP ) );
      p1.add( trackButton );
      p1.add( Box.createHorizontalGlue() );
      p1.add( printButton );

      headerPanel.add( p1 );

      p1 = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      totalLabel = new JLabel( "Total items tracked: 0" );
      p1.add( totalLabel );

      headerPanel.add( Box.createVerticalStrut( GAP ) );
      headerPanel.add( p1 ); 
      headerPanel.add( Box.createVerticalStrut( GAP ) );

      return headerPanel;
   }

   /**
    *  Creates and configures the date/time selectors.
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


   /** Creates the histogram. */
   private JComponent createHistogramPanel() {
      chart = ChartFactory.createHistogram(
            null,
            "Time Between Events (sec)",
            "Frequency",
            new HistogramDataset(),
            PlotOrientation.VERTICAL,
            false,
            false,
            false
         );
      ChartPanel chartPanel = new ChartPanel( chart,
            true, false, false, false, false );
      printableComponent = chartPanel;

      JPanel histPanel = new JPanel( new FlowLayout(
            FlowLayout.CENTER, 0, 0 ) );
      histPanel.add( chartPanel );
      FixedRatioComponent.maintainRatio( chartPanel, ASPECT_RATIO );

      return histPanel;
   }

   private void track() {
      String m1 = milestone1Combo.getSelectedItem().toString();
      String m2 = milestone2Combo.getSelectedItem().toString();
      SimpleDateFormat dateFormat = new SimpleDateFormat(
                       "yyyy-MM-dd HH:mm:ss" );
      String startTime = dateFormat.format( startTimePicker.getDate());
      String endTime = dateFormat.format( endTimePicker.getDate());
      int lo = (Integer)loSpinner.getValue();
      int hi = (Integer)hiSpinner.getValue();
      if (hi <= lo)
         hi = lo + 1;

      String sql = String.format( "SELECT " +
                   "UNIX_TIMESTAMP(t2.stamp) - UNIX_TIMESTAMP(t1.stamp) + " +
                   "(t2.msec - t1.msec)/1000.0 AS dt " +
                   "FROM cartonLog as t1, cartonLog as t2 " +
                   "WHERE t1.code = '%s' " +
                   "AND t2.code = '%s' " +
                   "AND t1.id = t2.id " +
                   "AND t1.stamp >= '%s' " +
                   "AND t2.stamp < '%s' " +
                   "HAVING dt >= %d AND dt <= %d",
                   m1, m2, startTime, endTime, lo, hi );
      double[] dataArray = new double[ 0 ];
      List<String> list = db.getValueList( sql );
      if (list != null) {
         dataArray = new double[ list.size() ];
         int i = 0;
         for (String item : list)
            dataArray[i++] = new Double( item );
      }

      HistogramDataset dataset = new HistogramDataset();
      dataset.addSeries("H1", dataArray, NUM_BINS, lo, hi);
      ((XYPlot)chart.getPlot()).setDataset( dataset );

      totalLabel.setText( "Total items tracked: " + list.size() );
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


   private void print() {
      if (printableComponent == null)
         return;

      String m1 = milestone1Combo.getSelectedItem().toString();
      String m2 = milestone2Combo.getSelectedItem().toString();
      SimpleDateFormat dateFormat = new SimpleDateFormat(
                       "yyyy-MM-dd HH:mm:ss" );
      String startTime = dateFormat.format( startTimePicker.getDate());
      String endTime = dateFormat.format( endTimePicker.getDate());

      String header = String.format(
            "Production Timing, ''%s'' to ''%s''", m1, m2 );
      String footer = startTime + " to " + endTime;

      try {
         ComponentPrintable.printComponent( printableComponent,
               header, footer );
      } catch (PrinterException ex) {
         RDSUtil.alert( "%s: error during printing", getName() );
         RDSUtil.alert( ex );
      } catch (Exception ex) {
         RDSUtil.alert( "%s: error during printing", getName() );
         RDSUtil.alert( ex );
      }
   }

}  // TimeTrackerPanel class
