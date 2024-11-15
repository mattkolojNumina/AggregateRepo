/*
 * ReportSQL.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.*;

import rds.*;


/**
 * A report containing the results of an SQL query.
 */
public class ReportSQL
      extends ReportSubpanel {

   // constants
   public static final String PARAM_REGEX = "\\[(.*)\\] *(.*)";

   private RDSTable sqlTable;

   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportSQL( RDSDatabase db,
                       String report,
                       String title,
                       String paramString ) {
      super( db, report, title, paramString );
   }


   /**
    * Configures the report's parameters.  This report uses a non-standard
    * parameter specification.
    */
   @Override
   protected void configureParams( String paramString ) {
      Pattern p = Pattern.compile( PARAM_REGEX );
      Matcher m = p.matcher( paramString );
      if (m.matches()) {
         setParam( "columns", m.group( 1 ) );
         setParam( "sql", m.group( 2 ) );
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Component createReport() {
      String columnStr = getParam( "columns" );
      if (columnStr == null)
         columnStr = "Column 1,Column2,Column 3,Column 4";
      sqlTable = new RDSTable( db, columnStr.split( "," ) );

      printableComponent = sqlTable;
      return sqlTable.getScrollPane();
   }


   /**
    * {@inheritDoc}
    */
   @Override
   protected void refresh( Date startTime, Date endTime ) {
      String startTimeStr = dateFormatter.format( startTime );
      String endTimeStr = dateFormatter.format( endTime );

      String sql = MessageFormat.format(
            getParam( "sql" ).replace( "'", "''" ),
            startTimeStr, endTimeStr );

      try {
         sqlTable.populateTable( sql );
      } catch (SQLException ex) {
         RDSUtil.alert( "sql error during table refresh, sql = [" + sql + "]" );
         RDSUtil.alert( ex );
      }
   }

}  // end ReportSQL class
