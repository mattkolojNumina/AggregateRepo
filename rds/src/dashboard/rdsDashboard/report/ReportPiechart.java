/*
 * ReportPiechart.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.JPanel;

import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;

import rds.*;


/**
 * A report containing a piechart of counters.
 */
public class ReportPiechart
      extends ReportSubpanel {

   // constants
   private static final double ASPECT_RATIO = 4.0 / 3.0;

   private JFreeChart chart;
   private DefaultPieDataset dataset;


   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportPiechart( RDSDatabase db,
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
      dataset = new DefaultPieDataset();
      chart = ChartFactory.createPieChart3D(
                    null,
                    dataset,
                    false,
                    false, false
                   );

      PiePlot3D plot = (PiePlot3D)chart.getPlot();
      plot.setForegroundAlpha(0.7f);
      plot.setCircular( true );
      plot.setInsets( new RectangleInsets() );

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
   protected void refresh( Date startTime, Date endTime ) {
      String startTimeStr = dateFormatter.format( startTime );
      String endTimeStr = dateFormatter.format( endTime );
      DecimalFormat df = new DecimalFormat( "0.0" );

      dataset.clear();

      String totalVal = db.getValue(
            "SELECT SUM( counts.value ) " +
            "FROM counts, reportItems " +
            "WHERE report = '" + report + "' " +
            "AND counts.code = reportItems.code " +
            "AND counts.stamp > '" + startTimeStr + "' " +
            "AND counts.stamp <= '" + endTimeStr + "' ",
            "0" );
      long total = (totalVal == null) ? 0L : Long.valueOf( totalVal );

      String sql =
            "SELECT reportItems.description AS descr, " +
            "SUM( counts.value ) AS count " +
            "FROM counts, reportItems " +
            "WHERE report = '" + report + "' " +
            "AND counts.code = reportItems.code " +
            "AND counts.stamp > '" + startTimeStr + "' " +
            "AND counts.stamp <= '" + endTimeStr + "' " +
            "GROUP BY counts.code " +
            "ORDER BY reportItems.ordinal ASC, count DESC";
      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = stmt.executeQuery( sql );
         while ( res.next() ) {
            long count = res.getLong( "count" );
            double percent = (double)count / total * 100.0;
            String descr = String.format( "%s\n%d = %s%%",
                  res.getString( "descr" ), count, df.format( percent ) );
            dataset.setValue( descr, count );
         }
      } catch (SQLException ex) {
         RDSUtil.alert( getName() + ": sql error during report refresh, " +
               "sql = [" + sql + "]");
         RDSUtil.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }
   }

}  // ReportPiechart class
