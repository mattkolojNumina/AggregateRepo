package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;

import rds.*;
import rdsDashboard.*;

public class DashboardDataTable 
extends JPanel 
implements MouseListener {

   private String name;
   private RDSDatabase db;
   private RDSAdmin admin;
   private RDSTable table;
   private JLabel tableLabel;
   private Map<String,String> tableParams;
   private List<Map<String,String>> columns;
   private RDSDashboard parentDashboard;
   private RDSDashboardPanel parentPanel;
   private String currentSelection = "1";
   private String selectionCaption = "";
   private String footerCaption = "";

   private static final int HGAP = 5;
   private static final int VGAP = 2;
   private static final int SPACING = 2;
   private static final Color DONE_COLOR = Color.GREEN.darker();
   private static final Color ERROR_COLOR = Color.RED;
   private static final String SELECTION_META = "&";

   public DashboardDataTable( String name, RDSDashboardPanel parentPanel, RDSDashboard parentDashboard ) {
      this.name = name;

      this.parentDashboard = parentDashboard;
      this.parentPanel = parentPanel;
      db = parentPanel.getDatabase();
      admin = parentPanel.getAdmin();
      tableParams = db.getRecordMap( "SELECT * FROM dashboardDataTables WHERE name='%s'", name );

      RDSUtil.inform( "creating data table [" + name + "]" );

      createUI();

      // refresh when this subpanel becomes visible
      addComponentListener( new ComponentAdapter() {
         public void componentShown( ComponentEvent evt ) {
            refresh( false );
         }
      } );

      int refresh = 0;
      try {
         refresh = db.getIntValue( "SELECT refresh FROM dashboardDataTables WHERE name='" + name + "'", 0 );
      } catch (NumberFormatException ex) {}
      if (refresh > 0) {
         Timer timer = new Timer( refresh, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refresh( false );
            }
         } );
         timer.start();
      }

   }

   private String getParam( String name, String otherwise ) {
      String value = tableParams.get( name );
      return (value != null) ? value : otherwise;
   }

   public void setCurrentSelection( String selection ) {
      currentSelection = selection;
      refresh( true );
   }

   public void setSelectionCaption( String caption ) {
      selectionCaption = caption;
   }

   public void clear() {
      RDSUtil.inform( "clearing table " + name );
      table.removeAllRows();
      currentSelection = "0";
   }

   /** Gets the underyling table. */
   public RDSTable getTable() {
      return table;
   }

   /** Gets the table description. */
   public String getTitle() {
      return getParam( "title", "" );
   }

   public String getDescription() {
      String description = getParam( "description", "" );
      if (description.isEmpty())
         description = getTitle();
      return description;
   }

   public String getDataSource() {
      String fullDataSource = getParam( "fullDataSource", "" );
      if (fullDataSource.isEmpty()) {
         String dataSource = getParam( "dataSource", "" );
         fullDataSource = db.getValue(
            "SELECT dataSource FROM dashboardDataSources " +
            "WHERE name='" + dataSource + "'", dataSource );
         tableParams.put( "fullDataSource", fullDataSource );
      }
      return fullDataSource;
   }

   public String getExtra() {
      return getParam( "extra", "" );
   }

   private boolean isEditable() {
      String editable = getParam( "editable", "no" );
      return "yes".equals( editable );
   }

   private boolean showRowCount() {
      String showRowCount = getParam( "showRowCount", "no" );
      return "yes".equals( showRowCount );
   }

   private String getLinkField() {
      return getParam( "linkField", "" );
   }

   private String getLinkColumn() {
      return getParam( "linkColumn", "" );
   }

   private String getLinkPanel() {
      return getParam( "linkPanel", "" );
   }

   private String getLinkDetail() {
      return getParam( "linkDetail", "" );
   }

   /** Creates the user interface for this subpanel. */
   private void createUI() {
      setLayout( new BorderLayout( HGAP, VGAP ) );

      add( createRefreshPanel(), BorderLayout.NORTH );
      add( createTable(), BorderLayout.CENTER );
      JPanel p = new JPanel( new BorderLayout( 0, 0 ) );
      p.add( createSelectorPanel(), BorderLayout.NORTH );
      if ( isEditable() ) {
         p.add( createControlPanel(), BorderLayout.SOUTH );
      }
      add( p, BorderLayout.SOUTH );
   }

   private JPanel createTable() {
      JPanel p = new JPanel( new BorderLayout( HGAP, VGAP) );

      columns = db.getResultMapList( String.format(
            "SELECT * FROM dashboardDataColumns " +
                  "WHERE dataTable = '%s' " +
                  "ORDER BY columnNum",
                  name ) );

      List<String> columnNames = new ArrayList<String>();
      for (Map<String,String> colMap : columns)
         columnNames.add( colMap.get( "columnName" ) );

      table = new RDSTable( db, columnNames.toArray( new String[0] ) );
      table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
      table.setAutoCreateRowSorter( true );

      for (Map<String,String> colMap : columns) {
         String columnName = colMap.get( "columnName" );

         String hint = colMap.get( "hint" );
         if (hint != null && !hint.isEmpty())
            table.setColumnHeaderToolTip( columnName, hint );

         int minWidth = RDSUtil.stringToInt( colMap.get( "minWidth" ), -1 );
         int preferredWidth = RDSUtil.stringToInt( colMap.get( "prefWidth" ), -1 );
         int maxWidth = RDSUtil.stringToInt( colMap.get( "maxWidth" ), -1 );
         table.setColumnWidths( columnName, minWidth, preferredWidth, maxWidth );

         String alignment = colMap.get( "alignment" );
         if ("left".equals( alignment ))
            table.setColumnAlignment( columnName, SwingConstants.LEFT );
         else if ("right".equals( alignment ))
            table.setColumnAlignment( columnName, SwingConstants.RIGHT );
         else if ("center".equals( alignment ))
            table.setColumnAlignment( columnName, SwingConstants.CENTER );

         String renderer = colMap.get( "renderer" );
         if (renderer != null && renderer.startsWith( "double" )) {
            String[] strArray = renderer.split( ":", 2 );
            if (strArray.length == 2)
               table.getColumn( columnName ).setCellRenderer(
                     new RDSTableCellRenderer.DoubleRenderer( strArray[1] ) );
         } else if (renderer != null && renderer.startsWith( "boolean" )) {
            String[] strArray = renderer.split( ":", 2 );
            if (strArray.length == 2)
               table.getColumn( columnName ).setCellRenderer(
                     new RDSTableCellRenderer.BooleanRenderer( strArray[1] ) );
            else
               table.getColumn( columnName ).setCellRenderer(
                     new RDSTableCellRenderer.BooleanRenderer() );
         } else if (renderer != null && renderer.startsWith( "status" )) {
            String[] strArray = renderer.split( ":", 2 );
            if (strArray.length == 2)
               table.getColumn( columnName ).setCellRenderer(
                     new StatusRenderer( strArray[1] ) );
         } else if (renderer != null && renderer.startsWith( "button" )) {
            String[] strArray = renderer.split( ":", 2 );
            if (strArray.length == 2) {
               table.getColumn( columnName ).setCellRenderer(
                     new RDSTableCellRenderer.ButtonRenderer() );
               table.getColumn( columnName ).setCellEditor(
                     new ColumnButtonEditor( strArray[1] ) );
               table.setColumnEditable( columnName, true );
            }
         } else if (renderer != null && renderer.startsWith( "led" )) {
            String[] strArray = renderer.split( ":", 2 );
            if (strArray.length == 2) {
               String[] valArray = strArray[1].split( "," );
               // TODO
            }
         }
      }

      table.addMouseListener( this );

      p.add( table.getScrollPane(), BorderLayout.CENTER );

      if ( showRowCount() ) {
         tableLabel = new JLabel( "" );
         p.add( tableLabel, BorderLayout.SOUTH );
      }

      return p;
   }

   private JPanel createRefreshPanel() {
      JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT, HGAP, VGAP ) );
      int refresh = 0;
      try {
         refresh = db.getIntValue( "SELECT refresh FROM dashboardDataTables WHERE name='" + name + "'", 0 );
      } catch (NumberFormatException ex) {}
      if (refresh == 0) {
         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refresh( false );
            }
         } );
         p.add( refreshButton );
      }
      JButton printButton = new JButton( "Print" );
      printButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            print();
         }
      } );
      p.add( printButton );

      return p;
   }

   private JPanel createSelectorPanel() {
      JPanel selectorPanel = new JPanel( new FlowLayout( FlowLayout.CENTER,
            HGAP, VGAP ) );
      List<SelectorRadioButton> buttonList = new ArrayList<SelectorRadioButton>();

      List<Map<String,String>> selectors;
      selectors = db.getResultMapList( String.format(
            "SELECT * FROM dashboardDataTableSelectors " +
                  "WHERE dataTable = '%s' " +
                  "ORDER BY ordinal",
                  name ) );
      if ( selectors != null) {
         boolean firstButton = true;
         for ( Map<String,String> selector : selectors ) {
            SelectorRadioButton newButton = new SelectorRadioButton( 
                  selector.get( "title" ), 
                  selector.get( "description" ),
                  selector.get( "selection" ), 
                  buttonList,
                  this,
                  firstButton );
            selectorPanel.add( newButton );
            firstButton = false;
         }

      }

      return selectorPanel;
   }

   /**
    * Creates a panel for controlling the table records.
    */
   private JPanel createControlPanel() {
      JButton newButton = new JButton( "Add" );
      newButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            if (!admin.isAuthenticatedInteractive( "edit tables",
                  parentPanel ))
               return;
            addRecord();
         }
      } );

      JButton editButton = new JButton( "Edit" );
      editButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            if (!admin.isAuthenticatedInteractive( "edit tables",
                  parentPanel ))
               return;
            editRecord();
         }
      } );

      JButton deleteButton = new JButton( "Delete" );
      deleteButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            if (!admin.isAuthenticatedInteractive( "edit tables",
                  parentPanel ))
               return;
            deleteRecord();
         }
      } );

      JPanel controlPanel = new JPanel( new FlowLayout( FlowLayout.CENTER,
            HGAP, VGAP ) );
      controlPanel.add( newButton );
      controlPanel.add( editButton );
      controlPanel.add( deleteButton );

      return controlPanel;
   }

   public void mouseClicked( MouseEvent evt ) {
      if (evt.getButton() == MouseEvent.BUTTON1 &&
            evt.getClickCount() == 2) {
         if ( getLinkPanel().isEmpty() || getLinkField().isEmpty() )
            return;
         RDSUtil.inform( "selected row " + table.rowAtPoint( evt.getPoint() ) + " --> " + 
               getLinkPanel() + "/" + getLinkDetail() + ":" + 
               getLinkColumn() + "/" + getLinkField() );
         String linkValue = table.getValueAt( table.rowAtPoint(
               evt.getPoint() ), getLinkColumn() ).toString();
         if ( parentDashboard != null )
            parentDashboard.displayPanel( getLinkPanel(), getLinkDetail(), getLinkField(), linkValue );
      }

   }

   public void mouseEntered(MouseEvent e) {}
   public void mouseExited(MouseEvent e) {}
   public void mousePressed(MouseEvent e) {}
   public void mouseReleased(MouseEvent e) {}

   /** Updates the panel contents. */
   public void refresh( boolean force ) {
      /**
       * Updates the contents of the table.
       */
      if ( ( !isVisible() || !parentPanel.isVisible() ) && !force )
         return;

      List<String> fields = new ArrayList<String>();
      for (Map<String,String> colMap : columns)
         fields.add( colMap.get( "field" ) );

      String select = "SELECT " +
            RDSUtil.separate( ", ", fields.toArray() );
      String from   = " FROM " + getDataSource();
      String where  = " WHERE " + currentSelection + " " + getExtra();

      String sql = select + from + where;
//      RDSUtil.inform( "refreshing table: " + sql );
      try {
         table.populateTable( sql );
      } catch (SQLException ex) {
         RDSUtil.alert( "%s: sql error during table refresh, sql = [%s]",
               getName(), sql );
         RDSUtil.alert( ex );
      }

      if ( showRowCount() ) 
         tableLabel.setText( String.format( "Number of records: %d", table.getRowCount()) );
   }

   /**
    * A custom table cell renderer with text color that depends on the
    * processing status.
    */
   private class StatusRenderer
   extends RDSTableCellRenderer {
      private String statusColumn;

      public StatusRenderer( String statusColumn ) {
         this.statusColumn = statusColumn;
      }

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column ) {
         super.getTableCellRendererComponent( table, value, isSelected,
               hasFocus, row, column );

         // override text color
         if (table instanceof RDSTable) {
            RDSTable rdsTable = (RDSTable)table;
            Object statusVal = rdsTable.getValueAt( row, statusColumn );
            int status = 0;
            if (statusVal != null)
               status = RDSUtil.stringToInt( statusVal.toString(), 0 );
            Color c = null;
            if (status > 0)
               c = DONE_COLOR;
            else if (status < 0)
               c = ERROR_COLOR;
            setForeground( c );
         }

         return this;
      }
   }

   private class ColumnButtonEditor
         extends RDSTableCellRenderer.ButtonEditor
         implements ActionListener {
      private String query;
      private int row;

      public ColumnButtonEditor( String query ) {
         super();

         this.query = query;
         this.row = -1;

         button.addActionListener( this );
      }

      @Override
      public Component getTableCellEditorComponent( JTable table,
            Object value, boolean isSelected, int row, int column ) {
         this.row = row;

         return super.getTableCellEditorComponent( table, value, isSelected,
               row, column );
      }

      @Override
      public boolean stopCellEditing() {
         row = -1;
         return super.stopCellEditing();
      }

      @Override
      public void cancelCellEditing() {
         row = -1;
         super.cancelCellEditing();
      };

      @Override
      public void actionPerformed( ActionEvent evt ) {
         if (row < 0)
            return;

         if (!admin.isAuthenticatedInteractive( "edit tables",
               parentPanel ))
            return;

         String cmd = evt.getActionCommand();
         String linkValue = table.getValueAt( row, getLinkColumn() ).toString();
         String sql = String.format(
               "UPDATE %s SET %s WHERE %s = '%s'",
               getDataSource(), query, getLinkField(), linkValue );
         RDSUtil.inform( "%s button: %s", cmd, sql );
         db.execute( sql );
         admin.log( String.format( "activated %s button for row %d, value %s",
               cmd, row, linkValue ) );

         refresh( false );
      }
   }


   public class SelectorRadioButton extends JPanel implements ActionListener {
      private String selection;
      private JRadioButton radioButton;
      private JTextField textField;
      private DashboardDataTable subpanel;

      private List<SelectorRadioButton> buttonList;

      public SelectorRadioButton( 
            String title, String description,
            final String selection, 
            List<SelectorRadioButton> buttonList, 
            DashboardDataTable subpanel,
            boolean selected ) {
         super( new FlowLayout( FlowLayout.CENTER,
            HGAP, VGAP ) );
         radioButton = new JRadioButton( title );
         if (description != null && !description.isEmpty())
            radioButton.setToolTipText( description );
         radioButton.setSelected( selected );
         if ( selected )
            footerCaption = getDescription();
         add( radioButton );
         this.selection = selection;
         this.buttonList = buttonList;
         this.subpanel = subpanel;
         if ( selection.contains( SELECTION_META ) ) {
            add( textField = new JTextField( 12 ) );
            textField.addActionListener( this );
         }
         if ( selected ) {
            if ( textField != null ) {
               String s = selection;
               currentSelection = s.replaceAll( SELECTION_META, textField.getText() );
            } else
               currentSelection = selection;
         }

         buttonList.add( this );

         radioButton.addActionListener( this );
      }

      public String getDescription() {
         String fullDescription = radioButton.getText();
         String description = radioButton.getToolTipText();
         if (description != null && !description.isEmpty())
            fullDescription += " (" + description + ")";
         if (textField != null)
            fullDescription += " " + textField.getText();
         return fullDescription;
      }

      /**
       * Handles actions performed on components within this subpanel.
       */
      public void actionPerformed( ActionEvent evt ) {
         for ( SelectorRadioButton srb : buttonList ) {
            if ( ( evt.getSource() == srb.radioButton ) || ( evt.getSource() == srb.textField ) ) {
               srb.radioButton.setSelected( true );
               if ( textField != null ) {
                  String s = selection;
                  currentSelection = s.replaceAll( SELECTION_META, textField.getText() );
               } else
                  currentSelection = selection;
               footerCaption = srb.getDescription();
            } else
               srb.radioButton.setSelected( false );
         }
         subpanel.refresh( false );
      }
   } 

   public void print() {
      String now = db.getValue( "SELECT NOW()", "" );
      String header = getDescription();
      if ( !selectionCaption.isEmpty() )
         header += " for " + selectionCaption;
      String footer = "printed at " + now;
      if ( !footerCaption.isEmpty() )
         footer = footerCaption + " -- " + footer;

      try {
         RDSUtil.inform( "header: " + header );
         RDSUtil.inform( "footer: " + footer );
         table.print( header, footer );
//      } catch (java.awt.print.PrinterException ex) {
//         RDSUtil.alert( getName() + ": error during printing" );
      } catch (Exception ex) {}
   }

   /** Adds a new record. */
   private void addRecord() {
      showEditDialog( true );
      refresh( false );
   }

   /** Edits the selected record. */
   private void editRecord() {
      if ( table.getSelectedRowCount() < 1 )
         return;
      table.setRowSelectionInterval( table.getSelectedRows()[0], table.getSelectedRows()[0] );
      showEditDialog( false );
      refresh( false );
   }

   /** Permanently removes a record from the table. */
   private void deleteRecord() {
      if ( table.getSelectedRowCount() < 1 )
         return;

      String prompt = (table.getSelectedRows().length > 1 ? "Delete records?" : "Delete record?" );
      if (JOptionPane.showConfirmDialog( this, prompt,
            "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE ) != JOptionPane.OK_OPTION)
         return;

      for ( int row : table.getSelectedRows() ) {
         String selectedValue = table.getValueAt( row, getLinkColumn() ).toString();
         String sql = String.format( "DELETE FROM %s WHERE %s='%s'", getDataSource(), getLinkField(), selectedValue );
         db.execute( sql );
         admin.log( parentPanel.getName() + ": deleted record [" + name + "] " + getLinkField() + "=" + selectedValue );
      }
      table.setRowSelectionInterval( table.getSelectedRows()[0], table.getSelectedRows()[0] );
      refresh( false );
   }

   /**
    * Displays a dialog for creating or editing a table record.
    * 
    * @param   keyVals  the values of the primary keys of the record to
    *          edit, or {@code null} to create a new record
    * @return  {@code true} if the record was successfully modified or
    *          created, {@code false} otherwise
    */
   private void showEditDialog( boolean newRecord ) {
      final int fieldWidth = 10;

      String title;
      Map<String,String> recordMap;
      String selectedValue = "";

      if (newRecord) {
         title = "Create New Record";
         recordMap = new HashMap<String,String>();
      } else {
         title = "Edit Record";
         selectedValue = table.getValueAt( table.getSelectedRow(), getLinkColumn() ).toString();
         String sql = String.format( "SELECT * FROM %s WHERE %s='%s'", getDataSource(), getLinkField(), selectedValue );
//         RDSUtil.inform( "getting edit values : " + sql);
         recordMap = db.getRecordMap( sql );
      }

      Map<String,JComponent> fieldMap = new HashMap<String,JComponent>();
      JPanel fieldsPanel = new JPanel( new SpringLayout() );
      int n = 0;
      for (Map<String,String> colMap : columns) {
         String col = colMap.get( "columnName" );
         String field = colMap.get( "field" );
         n++;
         fieldsPanel.add( new JLabel( col + ": ", JLabel.RIGHT ) );
         String val = recordMap.get( field );
         JComponent valComponent = new JLabel( val );
         String editVals = colMap.get( "editVals" );
         if (editVals == null || editVals.isEmpty())
            valComponent = new JTextField( val, fieldWidth );
         else if (editVals.startsWith( "query:" )) {
            String[] data = editVals.split( ":", 2 );
            if (data.length > 1) {
               List<String> valList = db.getValueList( data[1] );
               valComponent = new JComboBox( new Vector<String>( valList ) );
               if (val != null)
                  ((JComboBox)valComponent).setSelectedItem( val );
            }
         } else {
            String[] vals = editVals.split( "," );
            valComponent = new JComboBox( vals );
            if (val != null)
               ((JComboBox)valComponent).setSelectedItem( val );
         }
         valComponent.setEnabled( colMap.get( "editable" ).equals( "yes" ) );
         fieldMap.put( col, valComponent );
         fieldsPanel.add( valComponent );
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
         return;

      RDSUtil.inform( "updating " + name );

      String fieldList = "";
      String fieldListNoQuotes = "";
      for (Map<String,String> colMap : columns) {
         if ( colMap.get( "editable" ).equals( "yes" ) ) {
            String col = colMap.get( "columnName" );
            String field = colMap.get( "field" );
            String text = getEditValue( fieldMap.get( col ) );
            if ( fieldList.isEmpty() ) {
               fieldList = String.format( " %s=%s", field, RDSDatabase.convertValue( text ) );
               fieldListNoQuotes = String.format( " %s=%s", field, text);
            } else {
               fieldList = fieldList + String.format( ", %s=%s", field, RDSDatabase.convertValue( text ) );
               fieldListNoQuotes = fieldListNoQuotes + String.format( ", %s=%s", field, text);
            }
         }
      }

      if ( newRecord ) {
         if ( db.execute( "INSERT INTO %s SET %s", getDataSource(), fieldList) < 1 ) {
            RDSUtil.trace( "failed to add record");
            JOptionPane.showMessageDialog( this, "Unable to add record" );
         } else
            admin.log( parentPanel.getName() + ": added record to " + name + ": " + fieldListNoQuotes );
         return;
      } else {
         if ( db.execute( "UPDATE %s SET %s WHERE %s='%s'", getDataSource(), fieldList, getLinkField(), selectedValue ) < 1 ) {
            RDSUtil.trace( "failed to update record" );
            JOptionPane.showMessageDialog( this, "Unable to update record" );
         } else
            admin.log( parentPanel.getName() + ": modified record [" + selectedValue + "] in " + name + ": " + fieldListNoQuotes );
         return;
      }
   }

   String getEditValue( JComponent c ) {
      String val = null;
      if (c instanceof JTextComponent)
         val = ((JTextComponent)c).getText().trim();
      else if (c instanceof JComboBox)
         val = (String)((JComboBox)c).getSelectedItem();
      return val;
   }

}
