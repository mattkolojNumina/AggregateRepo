/*
 * TableEditorPanel.java
 * 
 * (c) 2010 Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for providing editing capability to database table(s).
 */
public class TableEditorPanel
      extends RDSDashboardPanel {

   protected List<TableEditorSubpanel> subpanelList;

   /**
    * Constructs a dashboard panel for editing database tables.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public TableEditorPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Table Editor" );
      setDescription( "Edit database tables" );

      createSubpanels();
      createUI();
   }

   /** Determine which table(s) will be displayed for editing. */
   private void createSubpanels() {
      subpanelList = new ArrayList<TableEditorSubpanel>();

      String tableListStr = getParam( "table" );
      if (tableListStr == null || tableListStr.isEmpty()) {
         RDSUtil.alert( getName() + ": empty table list" );
         return;
      }

      String[] tableNameArray = tableListStr.split( "\\+" );
      for (String tableName : tableNameArray) {
         TableEditorSubpanel subpanel = new TableEditorSubpanel( this,
               tableName );
         if (subpanel.isValidTable())
            subpanelList.add( subpanel );
         else
            RDSUtil.alert( "table [%s] is not valid for editing", tableName );
      }
   }

   /** Creates the user interface for this panel. */
   private void createUI() {
      setLayout( new BorderLayout() );

      if (subpanelList.isEmpty())
         return;

      if (subpanelList.size() == 1) {
         createTitledBorder( true );
         add( subpanelList.get( 0 ), BorderLayout.CENTER );
      } else {
         createTitledBorder( false );

         JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP,
               JTabbedPane.SCROLL_TAB_LAYOUT );
         tabs.setFocusable( false );

         for (int i = 0, n = subpanelList.size(); i < n; i++) {
            TableEditorSubpanel subpanel = subpanelList.get( i );
            subpanel.setBorder( BorderFactory.createEmptyBorder(
                  PADDING, PADDING, PADDING, PADDING ) );
            tabs.add( subpanel, subpanel.getTableName() );
            String tooltip = subpanel.getTableDescription();
            if (tooltip != null && !tooltip.isEmpty())
               tabs.setToolTipTextAt( i, tooltip );
         }
         add( tabs, BorderLayout.CENTER );
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void refreshPanel() {
      if (!isVisible())
         return;

      // refresh the visible subpanel
      for (TableEditorSubpanel subpanel : subpanelList) {
         if (subpanel.isVisible())
            subpanel.refresh();
      }
   }


   /*
    * === S U B P A N E L ===
    */

   /** A class for subpanels of the table editor panel. */
   public static class TableEditorSubpanel
         extends JPanel {
      private RDSDashboardPanel parentPanel;
      private String tableName;
      private String tableDescription;

      private RDSDatabase db;
      private RDSAdmin admin;
      private RDSTable table;

      private List<String> columnNames;
      private List<String> keys;
      private List<String> tooltips;

      // ui variables
      JLabel titleLabel;

      /** Constructs a subpanel for inclusion in the table editor panel. */
      public TableEditorSubpanel( RDSDashboardPanel parentPanel,
            String tableName ) {
         this.parentPanel = parentPanel;
         this.tableName = tableName;

         db = parentPanel.getDatabase();
         admin = parentPanel.getAdmin();

         getTableInfo();
         if (!isValidTable())
            return;

         createUI();

         // refresh when this subpanel becomes visible
         addComponentListener( new ComponentAdapter() {
            public void componentShown( ComponentEvent evt ) {
               refresh();
            }
         } );
      }

      /**
       * Determines if this subpanel represents a valid table that is
       * capable of being edited by this panel. 
       */
      public boolean isValidTable() {
         return (columnNames.size() > 0 && keys.size() > 0);
      }

      /** Gets the table name. */
      public String getTableName() {
         return tableName;
      }

      /** Gets the table description. */
      public String getTableDescription() {
         return tableDescription;
      }

      /** Gets the underyling table. */
      public RDSTable getTable() {
         return table;
      }

      /** Sets the title string. */
      public void setTitle( String title ) {
         titleLabel.setText( title );
      }

      /**
       * Gets information about the table, including column names
       * and primary key(s).
       */
      private void getTableInfo() {
         columnNames = new ArrayList<String>();
         keys = new ArrayList<String>();
         tooltips = new ArrayList<String>();

         DatabaseMetaData metadata = null;
         ResultSet res = null;
         try {
            metadata = db.connect().getMetaData();

            res = metadata.getTables( null, null, tableName, null );
            if (res.next()) {
               tableDescription = res.getString( "REMARKS" );
            }

            res = metadata.getColumns( null, null, tableName, "%" );
            while (res.next()) {
               columnNames.add( res.getString( "COLUMN_NAME" ) );
               String tip = res.getString( "REMARKS" );
               tooltips.add( (tip.isEmpty()) ? null : tip );
            }

            res = metadata.getPrimaryKeys( null, null, tableName );
            while (res.next()) {
               keys.add( res.getString( "COLUMN_NAME" ) );
            }
         } catch (SQLException ex) {
            RDSUtil.alert( "sql error getting meta data" );
            RDSUtil.alert( ex );
            return;
         } finally {
            RDSDatabase.closeQuietly( res );
         }
      }

      /** Creates the user interface for this subpanel. */
      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );

         titleLabel = new JLabel( "", JLabel.CENTER );
         Font font = titleLabel.getFont();
         float newSize = font.getSize2D() * 1.25f;
         titleLabel.setFont( font.deriveFont( Font.BOLD, newSize ) );
         setTitle( "Edit the " + getTableName() + " table" );

         add( titleLabel, BorderLayout.NORTH );
         add( createTable(), BorderLayout.CENTER );
         add( createControlPanel(), BorderLayout.SOUTH );
      }

      /** Creates and configures the table. */
      private Component createTable() {
         table = new RDSTable( db, columnNames.toArray( new String[0] ) );
         table.setColumnToolTips( tooltips.toArray( new String[0] ) );

         table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         table.setAutoCreateRowSorter( true );

         table.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getButton() == MouseEvent.BUTTON1 &&
                     evt.getClickCount() == 2) {
                  int row = table.rowAtPoint( evt.getPoint() );
                  List<String> keyVals = new ArrayList<String>();
                  for (String key : keys)
                     keyVals.add( table.getValueAt( row, key ).toString() );
                  editRecord( keyVals );
               }
            }
         } );

         return table.getScrollPane();
      }

      /**
       * Creates a panel for controlling the table records.
       */
      private JPanel createControlPanel() {
         JButton newButton = new JButton( "Add" );
         newButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               addRecord();
            }
         } );

         JButton editButton = new JButton( "Edit" );
         editButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               editRecord( getSelectedRecord() );
            }
         } );

         JButton deleteButton = new JButton( "Delete" );
         deleteButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               deleteRecord( getSelectedRecord() );
            }
         } );

         JPanel controlPanel = new JPanel();
         controlPanel.add( newButton );
         controlPanel.add( editButton );
         controlPanel.add( deleteButton );

         return controlPanel;
      }

      /** Updates the panel contents. */
      public void refresh() {
         String order = "";
         if (keys.size() > 0) {
            order = " ORDER BY " + keys.get( 0 );
            for (int i = 1, n = keys.size(); i < n; i++)
               order += ", " + keys.get( i );
         }
         String query =
               "SELECT * FROM " + tableName + order;
         try {
            table.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: error populating table, query = [%s]",
                  getName(), query);
            RDSUtil.alert( ex );
         }
      }

      /** Adds a new record. */
      private void addRecord() {
         if (!admin.isAuthenticatedInteractive( "edit tables",
               parentPanel ))
            return;

         boolean success = showEditDialog( null );

         if (success) {
            admin.log( getName() + ": added new record" );
            refresh();
         }
      }

      /** Edits the selected record. */
      private void editRecord( List<String> keyVals ) {
         if (keyVals == null || keyVals.isEmpty())
            return;
         if (!admin.isAuthenticatedInteractive( "edit tables",
               parentPanel ))
            return;

         boolean success = showEditDialog( keyVals );

         if (success) {
            admin.log( getName() + ": modified record [" +
                  RDSUtil.separate( "/", keyVals.toArray() ) + "]" );
            refresh();
         }
      }

      /** Permanently removes a record from the table. */
      private void deleteRecord( List<String> keyVals ) {
         if (keyVals == null || keyVals.isEmpty())
            return;
         if (!admin.isAuthenticatedInteractive( "edit tables",
               parentPanel ))
            return;

         if (JOptionPane.showConfirmDialog( this, "Delete record?",
               "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE ) != JOptionPane.OK_OPTION)
            return;

         String where = " WHERE " + keys.get( 0 ) + " = '" +
               keyVals.get( 0 ) + "'"; 
         for (int i = 1, n = keys.size(); i < n; i++) {
            where += " AND " + keys.get( 1 ) + " = '" +
                  keyVals.get( i ) + "'";
         }
         int rows = db.execute(
               "DELETE FROM " + tableName + where );

         if (rows > 0) {
            admin.log( getName() + ": deleted record [" +
                  RDSUtil.separate( "/", keyVals.toArray() ) + "]" );
            refresh();
         }
      }

      /** Returns the key values from the currently selected row. */
      private List<String> getSelectedRecord() {
         int row = table.getSelectedRow();
         if (row < 0)
            return null;

         List<String> keyVals = new ArrayList<String>();
         for (String key : keys)
            keyVals.add( table.getValueAt( row, key ).toString() );
         return keyVals;
      }

      /**
       * Displays a dialog for creating or editing a table record.
       * 
       * @param   keyVals  the values of the primary keys of the record to
       *          edit, or {@code null} to create a new record
       * @return  {@code true} if the record was successfully modified or
       *          created, {@code false} otherwise
       */
      private boolean showEditDialog( List<String> keyVals ) {
         final int fieldWidth = 10;

         boolean newRecord = (keyVals == null);
         String title;
         Map<String,String> recordMap;
         if (newRecord) {
            title = "Create New Record";
            recordMap = new HashMap<String,String>();
         } else {
            title = "Edit Record";

            String where = " WHERE " + keys.get( 0 ) + " = '" +
                  keyVals.get( 0 ) + "'"; 
            for (int i = 1, n = keys.size(); i < n; i++) {
               where += " AND " + keys.get( 1 ) + " = '" +
                     keyVals.get( i ) + "'";
            }
         
            recordMap = db.getRecordMap(
                  "SELECT * FROM " + tableName + where );
         }

         Map<String,JTextField> fieldMap = new HashMap<String,JTextField>();
         JPanel fieldsPanel = new JPanel( new SpringLayout() );
         int n = 0;
         for (String col : columnNames) {
            n++;
            fieldsPanel.add( new JLabel( col + ": ", JLabel.RIGHT ) );
            JTextField valField = new JTextField( recordMap.get( col ),
                  fieldWidth );
            if (!newRecord && keys.contains( col ))
               valField.setEnabled( false );
            fieldMap.put( col, valField );
            fieldsPanel.add( valField );
         }
         SpringUtilities.makeCompactGrid( fieldsPanel, n, 2,
               SPACING, SPACING, SPACING, SPACING );

         JPanel containerPanel = new JPanel(
               new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
         containerPanel.add( fieldsPanel );

         int returnValue = JOptionPane.showConfirmDialog(
               parentPanel,                   // parent component
               containerPanel,                // message object
               title,                         // dialog title
               JOptionPane.OK_CANCEL_OPTION,  // option type
               JOptionPane.PLAIN_MESSAGE      // message type
               );

         if (returnValue != JOptionPane.OK_OPTION)
            return false;

         Map<String,String> fields = new HashMap<String,String>();
         for (String col : columnNames) {
            String text = fieldMap.get( col ).getText().trim();
            fields.put( col, text );
         }
         int rows = 0;
         try {
            rows = db.update( tableName, keys.toArray( new String[0] ),
                  fields );
         } catch (SQLException ex) {
            RDSUtil.alert( "sql error updating record" );
            RDSUtil.alert( ex );
         }

         return (rows > 0);
      }
   }

}
