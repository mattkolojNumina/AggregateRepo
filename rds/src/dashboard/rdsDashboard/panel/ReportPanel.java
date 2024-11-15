/*
 * ReportsPanel.java
 * 
 * (c) 2008, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.Map;

import rds.*;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.report.*;


public class ReportPanel
      extends RDSDashboardPanel {

   // ui variables
   private JTabbedPane tabs;
   private String lastUpdate;

   /**
    * Constructs a panel for viewing production counters.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public ReportPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Reports" );
      setDescription( "View production reports" );
      createUI();
      createReportTabs();
   }

   /**
    * Creates the user interface for this panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );
   }

   private void createReportTabs() {
      List<String> reports = db.getValueList( 
                              "SELECT report FROM reports " +
                              "ORDER BY ordinal" ); 
      for (String report : reports) {
         Map<String,String> reportMap = db.getRecordMap(
               "SELECT * FROM reports " +
               "WHERE report = '" + report + "'" );
         String type = reportMap.get( "type" );
         RDSUtil.inform( "%s: constructing %s report [%s]",
               getName(), type, report );

         if ( "table".equals( type ) )
           tabs.add( report, new ReportTable( db, report,
                 reportMap.get( "title" ), reportMap.get( "params" ) ) );
         else if ( "piechart".equals( type ) )
            tabs.add( report, new ReportPiechart( db, report,
                  reportMap.get( "title" ), reportMap.get( "params" ) ) );
         else if ( type.equals( "trendchart" ) )
            tabs.add( report, new ReportTrendchart( db, report,
                  reportMap.get( "title" ), reportMap.get( "params" ) ) );
         else if ( type.equals( "tree" ) )
           tabs.add( report, new ReportTree( db, report,
                 reportMap.get( "title" ), reportMap.get( "params" ) ) );
         else if ( type.equals( "sqltable" ) )
           tabs.add( report, new ReportSQL( db, report,
                 reportMap.get( "title" ), reportMap.get( "params" ) ) );
      }

      add( tabs, BorderLayout.CENTER );
   }

   /**
    * Refresh the currently visible report.
    */
   @Override
   public void refreshPanel() {
      if (!isVisible())
         return;

      String val = db.getValue( "SELECT NOW() " +
                                "FROM reports " +
                                "WHERE stamp > '" + lastUpdate + "'",
                                "" );
      if (!val.isEmpty()) {
         lastUpdate = val;
         RDSUtil.trace( "%s: updating all reports", getName() );
         tabs.removeAll();
         createReportTabs();
      }
   }

}  // ReportsPanel class
