/*
 * ReportTrendchart.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;

import rds.*;


/**
 * A report containing a trend chart (time series) of counters.
 */
public class ReportTrendchart
      extends ReportSubpanel {

   // constants
   private static final double ASPECT_RATIO = 4.0 / 3.0;

   private JFreeChart chart;
   private TimeSeriesCollection dataset;


   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportTrendchart( RDSDatabase db,
                          String report,
                          String title,
                          String paramString ) {
      super( db, report, title, paramString );
   }


   /**
    * {@inheritDoc}
    */
   @Override
   protected Component createReport() {
      dataset = new TimeSeriesCollection();
      chart = ChartFactory.createTimeSeriesChart(
                    null,
                    "time",
                    "counts",
                    dataset,
                    true,
                    false, false
                    );

      ChartPanel chartPanel = new ChartPanel( chart,
            true, false, false, false, false );
      printableComponent = chartPanel;

      JPanel reportPanel = new JPanel( new FlowLayout(
            FlowLayout.CENTER, 0, 0 ) );
      reportPanel.add( chartPanel );
      FixedRatioComponent.maintainRatio( chartPanel, ASPECT_RATIO );

      return reportPanel;
   }


   /**
    * {@inheritDoc}
    */
   @Override
   public void refresh( Date startTime, Date endTime ) {
      int reportInterval = configureInterval(
            (endTime.getTime() - startTime.getTime()) / 1000 );
      String startTimeStr = dateFormatter.format( startTime );
      String endTimeStr = dateFormatter.format( endTime );

      dataset.removeAllSeries();

      List<String> codes = db.getValueList(
                        "SELECT DISTINCT code FROM reportItems " +
                        "WHERE report='" + report + "' " +
                        "ORDER BY ordinal" );

      // create the temporary table and populate the date/time entries
      db.execute( "CREATE TEMPORARY TABLE trend (" +
                  "s INT PRIMARY KEY, " +
                  "t DATETIME" +
                  ")" );
      GregorianCalendar t = new GregorianCalendar();
      GregorianCalendar tEnd = new GregorianCalendar();
      t.setTime( startTime );
      tEnd.setTime( endTime );
      while ( !t.after( tEnd ) ) {
         db.execute( "INSERT INTO trend SET " +
         		"t = '%s', " +
         		"s = FLOOR( UNIX_TIMESTAMP( t ) / %d ) * %d",
               dateFormatter.format( t.getTime() ),
               reportInterval, reportInterval );
         t.add( GregorianCalendar.SECOND, reportInterval );
      }

      for (String code : codes ) {
         String name = db.getValue( "SELECT description FROM reportItems " +
                             "WHERE report='" + report + "' " +
                             "AND code='" + code + "'",
                             "" );
         TimeSeries ts = new TimeSeries( name, Minute.class );
         db.execute( "CREATE TEMPORARY TABLE c " +
                     "SELECT value, " +
                     "FLOOR( UNIX_TIMESTAMP( stamp ) / %d ) * %d AS stamp " +
                     "FROM counts " +
                     "WHERE code = '%s' " +
                     "AND stamp BETWEEN DATE_SUB( '%s', INTERVAL %d SECOND ) " +
                     "AND DATE_ADD( '%s', INTERVAL %d SECOND )",
                     reportInterval, reportInterval, code,
                     startTimeStr, reportInterval, endTimeStr, reportInterval );
         db.execute( "ALTER TABLE c ADD INDEX (stamp)" );

         String sql = "SELECT " +
               "YEAR(t) AS yr," +
               "MONTH(t) AS mo," +
               "DAYOFMONTH(t) AS dy," +
               "HOUR(t) AS hr," +
               "MINUTE(t) AS mn," +
               "SUM(value) AS n " +
               "FROM trend LEFT JOIN c ON s = stamp " +
               "GROUP BY t ORDER BY t";
         Statement stmt = null;
         try {
            stmt = db.connect().createStatement();
            ResultSet res = stmt.executeQuery( sql );
            while ( res.next() ) {
               Minute m = new Minute (
                         res.getInt( "mn" ),
                         res.getInt( "hr" ),
                         res.getInt( "dy" ),
                         res.getInt( "mo" ),
                         res.getInt( "yr" )
                         );
               ts.add( m, res.getFloat( "n" ) );
            }
            dataset.addSeries( ts );
         } catch (SQLException ex) {
            RDSUtil.alert( getName() + ": sql error during report refresh, " +
                 "sql = [" + sql + "]");
         } finally {
            RDSDatabase.closeQuietly( stmt );
            db.execute( "DROP TABLE IF EXISTS c" );
         }
      }
      db.execute( "DROP TABLE IF EXISTS trend" );
   }

   private int configureInterval( long duration ) {
      String axisLabel;
      int interval;

      if (duration <= 0) {
         axisLabel = "";
         interval = 0;
      } else if (duration < 200) {
         axisLabel = "counts / second";
         interval = 1;
      } else if (duration < 200 * 60) {
         axisLabel = "counts / minute";
         interval = 60;
      } else if (duration < 200 * 60 * 60) {
         axisLabel = "counts / hour";
         interval = 3600;
      } else {
         axisLabel = "counts / day";
         interval = 86400;
      }

      ((XYPlot)chart.getPlot()).getRangeAxis().setLabel( axisLabel );
      return interval;
   }

}  // end ReportTrendchart class
