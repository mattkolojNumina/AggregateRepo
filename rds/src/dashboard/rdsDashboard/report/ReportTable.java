/*
 * ReportTable.java
 * 
 * (c) 2009--2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.Component;
import java.sql.SQLException;
import java.util.Date;

import rds.*;


/**
 * A report containing a table of counters.
 */
public class ReportTable
      extends ReportSubpanel {

   private RDSTable counterTable;

   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportTable( RDSDatabase db,
                       String report,
                       String title,
                       String params ) {
      super( db, report, title, params );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Component createReport() {
      counterTable = new RDSTable( db, "Description", "Value" );

      counterTable.setColumnWidths( "Description",  -1, 300,  -1 );
      counterTable.setColumnWidths( "Value",        50,  50,  75 );

      printableComponent = counterTable;
      return counterTable.getScrollPane();
   }


   /**
    * {@inheritDoc}
    */
   @Override
   protected void refresh( Date startTime, Date endTime ) {
      String startTimeStr = dateFormatter.format( startTime );
      String endTimeStr = dateFormatter.format( endTime );

      String sql = "SELECT reportItems.description, " +
                   "COALESCE(SUM( counts.value ),0) " +
                   "FROM reportItems LEFT JOIN counts " +
                   "ON reportItems.code=counts.code " +
                   "AND counts.stamp > '" + startTimeStr + "' " +
                   "AND counts.stamp <= '" + endTimeStr + "' " +
                   "WHERE report='" + report + "' " +
                   "GROUP BY reportItems.code " +
                   "ORDER BY ordinal, reportItems.code";
      try {
         counterTable.populateTable( sql );
      } catch (SQLException ex) {
         RDSUtil.alert( "sql error during table refresh sql = [" + sql + "]");
         RDSUtil.alert( ex );
      }
   }

}  // ReportTable class
