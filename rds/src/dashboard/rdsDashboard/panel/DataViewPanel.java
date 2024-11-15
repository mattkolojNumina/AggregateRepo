/*
 * DataViewPanel.java
 * 
 * Data-driven selection panel.
 * 
 * (c) 2012, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel that displays tabular results based on selection
 * criteria.
 */
public class DataViewPanel
extends RDSDashboardPanel {

   JTabbedPane tabs;
   private Map<String,DetailMap> details;
   
   /**
    * Creates a dashboard panel for viewing recent cartons.
    * 
    * @param   id               the panel id
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public DataViewPanel( String id, Container parentContainer ) {
      super( id, parentContainer );

      setName( "Data View" );
      createTitledBorder( false );

      setLayout( new BorderLayout() );

      details = new HashMap<String,DetailMap>();

      String content = getParam( "content" );
      String[] panels = content.split( ";+" );
      if ( panels.length > 1 ) {
         tabs = new JTabbedPane( JTabbedPane.TOP,
               JTabbedPane.SCROLL_TAB_LAYOUT );
         tabs.setFocusable( false );

         for (String panel : panels ) {
            String name = panel.substring( panel.indexOf( ':' ) + 1 );
            if ( panel.contains( "table:" ) ) {
               String title = db.getValue( "SELECT title FROM dashboardDataTables WHERE name='" + name + "'", name);
               DashboardDataTable ddt = new DashboardDataTable( name, this, parentDashboard );
               tabs.add( ddt, title );
            }
            if ( panel.contains( "detail:" ) ) {
               String title = db.getValue( "SELECT title FROM dashboardDataDetails WHERE name='" + name + "'", name);
               DashboardDetailPanel ddp = new DashboardDetailPanel( name, this, parentDashboard );
               tabs.add( ddp, title );
               DetailMap dm = new DetailMap();
               dm.detailPanel = ddp;
               dm.tabIndex = tabs.getTabCount()-1; 
               details.put( name, dm );
            }
         }

         add( tabs, BorderLayout.CENTER );
      } else {
         String name = panels[0].substring( panels[0].indexOf( ':' ) + 1 );
         if ( panels[0].contains( "table:" ) ) {
            DashboardDataTable ddt = new DashboardDataTable( name, this, parentDashboard );
            add( ddt, BorderLayout.CENTER );
         }
         if ( panels[0].contains( "detail:" ) ) {
            DashboardDetailPanel ddp = new DashboardDetailPanel( name, this, parentDashboard );
            add( ddp, BorderLayout.CENTER );
            DetailMap dm = new DetailMap();
            dm.detailPanel = ddp;
            dm.tabIndex = -1; 
            details.put( name, dm );
         }
      }
   }
   
   public void display( Object... data ) {
      String linkDetail = (String)data[0];
      String linkField = (String)data[1];
      String linkValue = (String)data[2];
      
      RDSUtil.inform( "got link to detail " + linkDetail + ": " + linkField + " = " + linkValue );
      
      details.get( linkDetail ).detailPanel.link( linkField, linkValue );
      if ( details.get( linkDetail ).tabIndex > -1 )
         tabs.setSelectedIndex( details.get( linkDetail ).tabIndex );
   }

   private class DetailMap {
      DashboardDetailPanel detailPanel;
      int tabIndex;
   }
}