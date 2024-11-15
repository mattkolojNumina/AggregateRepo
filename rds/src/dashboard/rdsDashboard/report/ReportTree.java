/*
 * ReportTree.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.report;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.util.Date;
import javax.swing.tree.*;

import rds.*;


/**
 * A report containing a tree of production counters.
 */
public class ReportTree
      extends ReportSubpanel {

   // constants
   private static final String SEPARATOR = "/";
   private static final String INDENT = "     ";

   private JTree counterTree;
   private RDSTable printTable;

   /**
    * Constructs a subpanel that holds the report.
    */
   public ReportTree( RDSDatabase db,
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
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            new CounterNode( "root", title ) );
      counterTree = new JTree( new DefaultTreeModel( root ) );
      DefaultTreeCellRenderer r = new CounterNodeRenderer();
      r.setOpenIcon( null );
      r.setClosedIcon( null );
      r.setLeafIcon( null );
      counterTree.setCellRenderer( r );

      printTable = new RDSTable( db, "Description", "Value" );
      printTable.setColumnWidths( "Description", -1, 400, -1 );
      printTable.setColumnWidths( "Value", 50, 50, 50 );
      JFrame dummyFrame = new JFrame();
      dummyFrame.getContentPane().add( printTable.getScrollPane() );
      dummyFrame.pack();
      printableComponent = printTable;

      return new JScrollPane(counterTree);
   }

   /**
    * A tree-node class to hold both the counter code used to determine
    * the heirarchy ({@code s1}) and the display text ({@code s2}).
    */
   class CounterNode {
      private String s1;
      private String s2;

      public CounterNode( String s1, String s2 ) {
         this.s1 = s1;
         this.s2 = s2;
      }

      public String toString() {
         return s1;
      }
   }  // end CounterNode inner class

   /**
    * A renderer to display only the description field of each counter node.
    */
   class CounterNodeRenderer extends DefaultTreeCellRenderer {
      public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
         super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
         CounterNode nodeInfo = (CounterNode)node.getUserObject();
         setText( nodeInfo.s2 );

         return this;
      }
   }  // end CounterNodeRenderer inner class


   /**
    * {@inheritDoc}
    */
   @Override
   protected void refresh( Date startTime, Date endTime ) {
      String startTimeStr = dateFormatter.format( startTime );
      String endTimeStr = dateFormatter.format( endTime );

      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            new CounterNode( "", title ) );
      DefaultTreeModel treeModel = new DefaultTreeModel( root );
      counterTree.setModel( treeModel );

      printTable.removeAllRows();

      String sql = "SELECT " +
            "reportItems.code AS code, " +
            "reportItems.description AS description, " +
            "COALESCE(SUM(counts.value),0) AS value " +
            "FROM reportItems LEFT JOIN counts " +
            "ON counts.code=reportItems.code " +
            "AND counts.stamp > '" + startTimeStr + "' " +
            "AND counts.stamp <= '" + endTimeStr + "' " +
            "WHERE report='" + report + "' " +
            "GROUP BY reportItems.code " +
            "ORDER BY ordinal, reportItems.code";
      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = stmt.executeQuery( sql );
         while ( res.next() ) {
            String code = res.getString("code");
            String description = res.getString( "description" );
            int value = res.getInt( "value" );

            // create the node
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                  new CounterNode( code,
                  String.format( "%s - %d", description, value ) ) );

            // determine the structural position of the node
            TreePath path = null;
            int separatorIndex = code.lastIndexOf( SEPARATOR ); 
            if ( separatorIndex > 0 ) {
               String rootCode = code.substring( 0, separatorIndex );
               path = getFirstMatch( rootCode );
            }

            // add the node to the tree
            if ( path == null ) {
               root.add( node );
            } else {
               DefaultMutableTreeNode n =
                    (DefaultMutableTreeNode)path.getLastPathComponent();
               n.add( node );
            }
            treeModel.reload();
            for (int i = 0; i < counterTree.getRowCount(); i++)
               counterTree.expandRow( i );

            // add the node to the table for printing
            String tableDescription = description;
            separatorIndex = -1;
            while ((separatorIndex = code.indexOf(
                  SEPARATOR, separatorIndex + 1 )) >= 0)
               tableDescription = INDENT + tableDescription;
            printTable.addRow( tableDescription, value );
         }
      } catch (SQLException ex) {
         RDSUtil.alert( getName() + ": sql error during table refresh, " +
                  "sql = [" + sql + "]");
         RDSUtil.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }

      // prepare table for printing
      printTable.updateColumnClasses();
      printTable.revalidate();
      printTable.setSize( printTable.getPreferredSize() );
   }

   private TreePath getFirstMatch( String match ) {
      if (match == null)
         return null;

      for (int row = 0, max = counterTree.getRowCount(); row < max; row++) {
         TreePath path = counterTree.getPathForRow(row);
         String text = counterTree.convertValueToText(
               path.getLastPathComponent(), false, true, true, row, false);
         if (text.equals( match ))
            return path;
      }

      return null;
   }

}  // end ReportTree class
