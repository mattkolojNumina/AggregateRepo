/*
 * ReportSubpanel.java
 * 
 * (c) 2007-2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import com.lavantech.gui.comp.*;

import rds.*;


/**
 * The abstract parent class of all reports.
 */
public abstract class ReportSubpanel
      extends JPanel
      implements ActionListener {

   // constants
   private static final int PADDING = 12;
   private static final int HEADER_SPACING = 10;
   private static final Dimension PICKER_SIZE = new Dimension( 165, 20 );
   private static final float TITLE_FONT_SCALE = 1.5f;

   protected static final SimpleDateFormat dateFormatter =
         new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

   protected RDSDatabase db;
   protected String report;
   protected String title;
   private Map<String,String> params;
   private DateTimePicker startTimePicker, endTimePicker;
   protected Component printableComponent;

   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportSubpanel( RDSDatabase db,
                          String report,
                          String title,
                          String paramString ) {
      super();
      this.db = db;
      this.report = report;
      this.title = title;

      setName( report );
      configureParams( paramString );

      createUI();
   }

   /** Configures the report's parameters. */
   protected void configureParams( String paramString ) {
      for (String param : paramString.split( "," )) {
         String[] paramVal = param.split( "=", 2 );
         if (paramVal.length == 2)
            setParam( paramVal[0], paramVal[1] );
      }
   }

   /** Clears all of the report's parameters. */
   protected void clearParams() {
      if (params != null)
         params.clear();
   }

   /**
    * Gets one of the report's parameter values.
    * 
    * @param   key  the parameter name
    * @return  the value of the named parameter, or {@code null} if the
    *          parameter does not exist
    */
   protected String getParam( String key ) {
      if (params == null)
         return null;
      return params.get( key );
   }

   /**
    * Sets one of the report's parameter values.  The parameter map is
    * lazily created.
    * 
    * @param   key    the parameter name
    * @param   value  the value of the parameter
    */
   protected void setParam( String key, String value ) {
      if (params == null)
         params = new HashMap<String, String>();
      params.put( key, value );
   }

   private void showLastNMinutes( int minutes ) {
      long currentTimeSecs = Long.valueOf( db.getValue(
            "SELECT UNIX_TIMESTAMP()", "0") );

      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis( currentTimeSecs * 1000L );
      endTimePicker.removeActionListener( this );
      endTimePicker.setCalendar( cal );
      endTimePicker.addActionListener( this );

      cal.add( GregorianCalendar.MINUTE, -minutes );
      startTimePicker.removeActionListener( this );
      startTimePicker.setCalendar( cal );
      startTimePicker.addActionListener( this );

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
      add( createReportPanel(), BorderLayout.CENTER );
   }

   /**
    * Creates the panel that contains control elements.
    * 
    * @return  the panel object
    */
   private JPanel createHeaderPanel() {
      // last 60 minutes
      JButton lastHourButton = new JButton( "Last 60 minutes" );
      lastHourButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            showLastNMinutes( 60 );
         }
      } );

      // last 24 hours
      JButton lastDayButton = new JButton( "Last 24 hours" );
      lastDayButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            showLastNMinutes( 60*24 );
         }
      } );

      // refresh
      JButton refreshButton = new JButton( "Refresh" );
      refreshButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            refresh();
         }
      } );

      // print
      JButton printButton = new JButton( "Print" );
      printButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            print( startTimePicker.getDate(), endTimePicker.getDate() );
         }
      } );

      JPanel refreshPanel = new JPanel();
      refreshPanel.setLayout( new BoxLayout( refreshPanel, BoxLayout.X_AXIS ) );
      refreshPanel.add( lastHourButton );
      refreshPanel.add( Box.createHorizontalStrut( HEADER_SPACING ) );
      refreshPanel.add( lastDayButton );
      refreshPanel.add( Box.createHorizontalStrut( HEADER_SPACING ) );
      refreshPanel.add( refreshButton );

      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new BoxLayout( headerPanel,
            BoxLayout.X_AXIS ) );

      createDateTimePickers( headerPanel );
      headerPanel.add( Box.createHorizontalStrut( HEADER_SPACING ) );
      headerPanel.add( refreshPanel );
      headerPanel.add( Box.createHorizontalGlue() );
      headerPanel.add( printButton );

      return headerPanel;
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

      startTimePicker = new DateTimePicker( cal, "MMM d, yyyy, h:mm:ss aa" );
      startTimePicker.setMinimumSize( PICKER_SIZE );
      startTimePicker.setPreferredSize( PICKER_SIZE );
      startTimePicker.setMaximumSize( PICKER_SIZE );
      startTimePicker.addActionListener( this );
      startTimePicker.setDisplayTodayButton( true );
      startTimePicker.getTimePanel().setDisplayAnalog( false );
      startTimePicker.getTimePanel().setMinDisplayed( true );
      startTimePicker.getTimePanel().setSecDisplayed( true );
      startTimePicker.setEditable( true );

      JLabel endLabel = new JLabel( "End: ", JLabel.RIGHT );

      endTimePicker = new DateTimePicker( cal, "MMM d, yyyy, h:mm:ss aa" );
      endTimePicker.setMinimumSize( PICKER_SIZE );
      endTimePicker.setPreferredSize( PICKER_SIZE );
      endTimePicker.setMaximumSize( PICKER_SIZE );
      endTimePicker.addActionListener( this );
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

   private JPanel createReportPanel() {
      JLabel titleLabel = new JLabel( title, JLabel.CENTER );
      Font font = titleLabel.getFont();
      float newSize = font.getSize2D() * TITLE_FONT_SCALE;
      titleLabel.setFont( font.deriveFont( Font.BOLD, newSize ) );

      JPanel reportPanel = new JPanel( new BorderLayout() );
      reportPanel.add( titleLabel, BorderLayout.NORTH );
      reportPanel.add( createReport(), BorderLayout.CENTER );

      return reportPanel;
   }

   private void refresh() {
      Date startTime = startTimePicker.getDate();
      Date endTime = endTimePicker.getDate();

      refresh( startTime, endTime );
   }

   private void print( Date startTime, Date endTime ) {
      if (printableComponent == null)
         return;

      String header = title;
      String footer = dateFormatter.format( startTime ) + " to " +
            dateFormatter.format( endTime );

      try {
         if (printableComponent instanceof RDSTable)
            ((RDSTable)printableComponent).print( header, footer );
         else
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

   /**
    * Handles actions performed on the UI elements of this panel.
    */
   @Override
   public void actionPerformed( ActionEvent evt ) {
      if (evt.getSource() == startTimePicker ||
            evt.getSource() == endTimePicker) {
         refresh();
      }
   }

   /*
    *  abstract methods to be implemented by report subclasses
    */

   /**
    * Creates the report component.
    */
   protected abstract Component createReport();

   /**
    * Updates the report over the specified time range.
    */
   protected abstract void refresh( Date startTime, Date endTime );

}  // end ReportSubpanel class
