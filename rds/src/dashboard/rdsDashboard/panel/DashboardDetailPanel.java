package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;

public class DashboardDetailPanel
extends JPanel 
implements ActionListener {
   private static final Font BOLD_FONT = UIManager.getFont(
         "Label.font" ).deriveFont( Font.BOLD );
   private static final String DEFAULT_VALUE = "-999999";
   private static final int MAX_RECORDS = 20;

   private String name;

   private RDSDatabase db;
   private RDSAdmin admin;
   private String dataSource;

   private List<Map<String,String>> selectors;
   private List<Map<String,String>> buttons;
   private List<Detail> details;
   private List<DashboardDataTable>detailTables;
   private JComboBox selectorCombo;
   private JTextField selectorValue;

   private String seqField;
   private String seqValue;
   private String selectionCaption;

   private RDSDashboard parentDashboard;
   private RDSDashboardPanel parentPanel;

   private static final int HGAP = 5;
   private static final int VGAP = 2;
   private static final int SPACING = 2;

   // ui variables

   /** Constructs a subpanel for inclusion in the table editor panel. */
   public DashboardDetailPanel( String name, RDSDashboardPanel parentPanel, RDSDashboard parentDashboard ) {
      this.name = name;
      this.parentDashboard = parentDashboard;
      this.parentPanel = parentPanel;

      db = parentPanel.getDatabase();
      admin = parentPanel.getAdmin();
      selectionCaption = "";

      RDSUtil.inform( "creating data detail " + name );

      dataSource = getDataSource();
      int refresh = 0;
      try {
         refresh = db.getIntValue( "SELECT refresh FROM dashboardDataDetails WHERE name='" + name + "'", 0 );
      } catch (NumberFormatException ex) {}
      createUI( refresh > 0 );

      if (refresh > 0) {
         Timer timer = new Timer( refresh, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refresh();
            }
         } );
         timer.start();
      }
      
      addComponentListener( new ComponentAdapter() {
         public void componentShown( ComponentEvent evt ) {
            refresh();
         }
      } );

      seqField = getKeyField();
      seqValue = DEFAULT_VALUE;
      refresh();
   }

   /** Gets the table name. */
   public String getDataSource() {
      String dataSource = db.getValue(
            "SELECT dataSource FROM dashboardDataDetails " +
            "WHERE name='" + name + "'", "" );
      return db.getValue(
            "SELECT dataSource FROM dashboardDataSources " +
            "WHERE name='" + dataSource + "'", dataSource );
   }


   /** Gets the table key. */
   public String getKeyField() {
      return db.getValue( "SELECT keyField FROM dashboardDataDetails WHERE name='" + name + "'", "" );
   }

   public String getSeqValue() {
      return seqValue;
   }

   /**
    * Gets information about the table, including column names
    * and primary key(s).
    */
   /** Creates the user interface for this subpanel. */
   private void createUI( boolean autoRefresh ) {
      setLayout( new BorderLayout( HGAP, VGAP ) );
      JPanel header = new JPanel( new BorderLayout( HGAP, VGAP ) );
      header.add( createSelectorPanel( autoRefresh ), BorderLayout.NORTH );
      header.add( createDetailPanel(), BorderLayout.CENTER );
      header.add( createControlPanel(), BorderLayout.SOUTH );
      add( header, BorderLayout.NORTH );
      add( createDetailTables(), BorderLayout.CENTER );
   }

   /**
    * Creates and configures the main view table.
    * 
    * @return  the view table's enclosing scroll pane
    */
   private JComponent createSelectorPanel( boolean autoRefresh ) {
      JPanel p = new JPanel();
      p.setLayout( new BoxLayout( p, BoxLayout.X_AXIS ) );

      selectors = db.getResultMapList( String.format(
            "SELECT * FROM dashboardDataDetailSelectors " +
                  "WHERE dataDetail = '%s' " +
                  "ORDER BY ordinal",
                  name ) );

      selectorCombo = new JComboBox();
      for (Map<String,String> selector : selectors) {
         selectorCombo.addItem( selector.get( "selectionPrompt" ) );
      }
      selectorCombo.setMaximumSize( selectorCombo.getPreferredSize() );
      p.add( Box.createHorizontalGlue() );
      p.add( selectorCombo );
      p.add( Box.createHorizontalStrut( HGAP ) );

      selectorValue = new JTextField( 16 );
      selectorValue.setMaximumSize( selectorValue.getPreferredSize() );
      selectorValue.addActionListener(new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            lookup();
         }
      } );
      p.add( selectorValue );
      p.add( Box.createHorizontalStrut( HGAP ) );

      JButton lookupButton = new JButton( "Lookup" );
      lookupButton.addActionListener(new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            lookup();
         }
      } );
      p.add( lookupButton );
      p.add( Box.createHorizontalGlue() );

      if ( !autoRefresh ) {
         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refresh();
            }
         } );
         p.add( refreshButton );
         p.add( Box.createHorizontalStrut( HGAP ) );
      }

      JButton printButton = new JButton( "Print All" );
      printButton.addActionListener(new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            printAll();
         }
      } );
      p.add( printButton );

      return p;
   }

   private JComponent createControlPanel() {
      JPanel p = new JPanel( new FlowLayout( FlowLayout.CENTER, HGAP, 0 ) );

      buttons = db.getResultMapList( String.format(
            "SELECT * FROM dashboardDataDetailButtons " +
                  "WHERE dataDetail = '%s' " +
                  "ORDER BY ordinal",
                  name ) );

      for (Map<String,String> button : buttons) {
         JButton b = new JButton( button.get( "caption" ) );
         b.addActionListener( this );
         p.add( b );

      }

      return p;
   }

   private JPanel createDetailPanel() {
      List<Map<String,String>> detailList = db.getResultMapList( String.format(
            "SELECT * FROM dashboardDataDetailFields " +
                  "WHERE dataDetail = '%s' " +
                  "ORDER BY ordinal", 
                  name ) );

      details = new ArrayList<Detail>();

      JPanel p = new JPanel( new SpringLayout() );

      boolean firstDetail = true;
      for (final Map<String,String> m : detailList) {
         final Detail d = new Detail();
         String dataSource = m.get( "dataSource" );
         d.dataSource = db.getValue( String.format(
               "SELECT dataSource FROM dashboardDataSources " +
               "WHERE name = '%s'",
               dataSource ), dataSource );
         d.field = m.get( "field" );
         d.title = m.get( "title" );

         JLabel titleLabel = new JLabel( d.title + ":", JLabel.RIGHT ); 
         JLabel l = new JLabel( "", JLabel.LEFT );
         if (firstDetail) {
            firstDetail = false;
            titleLabel.setFont( BOLD_FONT );
            l.setFont( BOLD_FONT );
         }
         JButton linkButton = null;
         final String caption = m.get( "linkCaption" );
         if (caption != null && !caption.isEmpty()) {
            linkButton = new JButton( caption );
            linkButton.addActionListener( new ActionListener() {
               public void actionPerformed( ActionEvent evt ) {
                  RDSUtil.inform( caption + " button --> " + 
                        m.get( "linkPanel" ) + "/" + m.get( "linkDetail" ) + ":" + 
                        m.get( "linkField" ) );
                  if ( parentDashboard != null )
                     parentDashboard.displayPanel( m.get( "linkPanel" ), m.get( "linkDetail" ), m.get( "linkField" ), d.value );
               }
            } );
         }

         d.label = l;
         details.add( d );

         p.add( titleLabel );
         p.add( l );
         p.add( (linkButton == null) ? new JLabel( "" ) : linkButton );
      }


      SpringUtilities.makeCompactGrid( p, details.size(), 3,
            SPACING, SPACING, SPACING, SPACING );

      return p;
   }

   private class Detail {
      public String dataSource = "";
      public String field = "";
      public String title = "";
      public String value = "";
      public JLabel label = null;

      public void determineValue() {
         String sql = String.format( "SELECT %s FROM %s WHERE %s='%s'",  
               field, dataSource, seqField, seqValue );
         value = db.getValue( sql, "" );
      }

      public void clear() {
         value = "";
         if (label != null)
            label.setText( value );
      }

      public void update() {
         determineValue();
         if (label != null)
            label.setText( value );
      }
   }

   private JPanel createDetailTables() {
      JPanel p = new JPanel( new BorderLayout() );
      JTabbedPane tbs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tbs.setFocusable( false );

      detailTables = new ArrayList<DashboardDataTable>();

      List<String> tables = db.getValueList( "SELECT tableName FROM dashboardDataDetailTables WHERE dataDetail='%s'", name );
      for ( String table : tables ) {
         DashboardDataTable ddt = new DashboardDataTable( table, parentPanel, parentDashboard );
         ddt.clear();
         String tableTitle = db.getValue( "SELECT title FROM dashboardDataTables WHERE name='" + table + "'", table);
         tbs.add( tableTitle, ddt );
         detailTables.add( ddt );
      }

      p.add( tbs, BorderLayout.CENTER );
      return p;         
   }

   private void lookup() {
      setSelection();
      refresh();
   }
   
   private void setSelection() {
      boolean exact = true;
      String sField = selectors.get( selectorCombo.getSelectedIndex() ).get( "selectionField" );
      String sValue = selectorValue.getText();
      seqField = getKeyField();

      int numRecords = getNumRecords( sField, sValue, exact );
      if (numRecords == 0) {
         exact = false;
         numRecords = getNumRecords( sField, sValue, exact );
      }

      if (numRecords == 0) {
         JOptionPane.showMessageDialog( parentPanel,
               "No matching records found",
               "Error", JOptionPane.ERROR_MESSAGE );
         seqValue = DEFAULT_VALUE;
      } else if (numRecords == 1) {
         String sql;
         if (exact)
            sql = String.format( "SELECT %s FROM %s WHERE %s='%s'", seqField, dataSource, sField, sValue );
         else
            sql = String.format( "SELECT %s FROM %s WHERE %s LIKE '%%%s%%'", seqField, dataSource, sField, sValue );
         seqValue = db.getValue( sql, "" );
      } else if (numRecords <= MAX_RECORDS) {
         seqValue = selectSeqValue( sField, sValue, exact );
      } else {
         JOptionPane.showMessageDialog( parentPanel,
               "Too many matching records found",
               "Error", JOptionPane.ERROR_MESSAGE );
         seqValue = DEFAULT_VALUE;
      }
      RDSUtil.inform( "  selected %s=%s -> %s=%s", sField, sValue, seqField, seqValue );
      selectorValue.setText( "" );
   }

   /** Updates the panel contents. */
   public void refresh() {
      refresh( false );
   }

   /** Updates the panel contents, conditionally. */
   public void refresh( boolean force ) {
      if (!force)
         if ((parentPanel != null && !parentPanel.isVisible()) || !isVisible())
            return;

      if (getNumRecords( seqField, seqValue, true ) == 1) {
         for ( Detail d : details )
            d.update();
         if ( details.size() > 0 ) {
            Detail d = details.get( 0 );
            selectionCaption = d.title + " " + d.value;
         } else
            selectionCaption = "";

         for ( DashboardDataTable dt : detailTables ) {
            dt.setSelectionCaption( selectionCaption );
            dt.setCurrentSelection( String.format( "%s='%s'", seqField, seqValue) );
         }
      } else {
         for ( Detail d : details )
            d.clear();
         for ( DashboardDataTable dt : detailTables ) {
            dt.setSelectionCaption( "" );
            dt.clear();
         }
      }
   }

   private int getNumRecords( String field, String value, boolean exact ) {
      if (exact)
         return db.getIntValue( String.format(
               "SELECT COUNT(DISTINCT %s) FROM %s " +
               "WHERE %s = '%s'",
               seqField, dataSource, field, value ), 0 );

      return db.getIntValue( String.format(
            "SELECT COUNT(DISTINCT %s) FROM %s " +
            "WHERE %s LIKE '%%%s%%'",
            seqField, dataSource, field, value ), 0 );
   }

   private String selectSeqValue( String field, String value, boolean exact ) {
      List<Detail> selectionDetails = new ArrayList<Detail>();
      List<String> columnNames = new ArrayList<String>();
      columnNames.add( "_seqValue" );
      for (Detail d : details) {
         if (true) {  // TODO
            selectionDetails.add( d );
            columnNames.add( d.title );
         }
      }
      RDSTable t = new RDSTable( columnNames.toArray( new String[0] ) );
      t.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      t.setColumnWidths( "_seqValue", 0, 0, 0 );  // hidden

      List<String> valueList;
      if (exact)
         valueList = db.getValueList(
               "SELECT %s FROM %s " +
               "WHERE %s = '%s'",
               seqField, dataSource, field, value );
      else
         valueList = db.getValueList(
               "SELECT %s FROM %s " +
               "WHERE %s LIKE '%%%s%%'",
               seqField, dataSource, field, value );

      for (String val : valueList) {
         seqValue = val;
         List<String> rowData = new ArrayList<String>();
         rowData.add( val );
         for (Detail d : selectionDetails) {
            d.determineValue();
            rowData.add( d.value );
         }
         t.addRow( rowData.toArray() );
      }

      int returnValue = JOptionPane.showConfirmDialog(
            parentPanel,                   // parent component
            t.getScrollPane(),             // message object
            "Select a Record",             // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return DEFAULT_VALUE;

      int selection = t.getSelectedRow();
      if (selection >= 0)
         return t.getValueAt( selection, "_seqValue" ).toString();

      return DEFAULT_VALUE;
   }

   private void printAll() {
      printDetails();
      for ( DashboardDataTable dt : detailTables )
         dt.print();
   }

   private void printDetails() {
      if (details.size() <= 0)
         return;

      Detail d = details.get( 0 );
      if (d == null)
         return;
      Container detailPanel = d.label.getParent();
      try {
         ComponentPrintable.printComponent( detailPanel, selectionCaption, "" );
      } catch (PrinterException ex) {}
   }

   public void link( String field, String value ) {
      RDSUtil.inform( "detail got a link " + field + "=" + value );
      seqField = field;
      seqValue = value;
      selectorValue.setText( "" );
      refresh( true );
   }

   public void actionPerformed( ActionEvent evt ) {
      for (Map<String,String> button : buttons) {
         String caption = button.get( "caption" );
         if ( evt.getActionCommand().equals( caption ) ) {
            String action = button.get( "adminAction" );
            if (action == null || (!action.isEmpty() &&
                  !admin.isAuthenticatedInteractive( action, parentPanel ) ))
               return;

            String confirmMsg = button.get( "confirmMsg" );
            if (confirmMsg != null && !confirmMsg.isEmpty()) {
               int retval = JOptionPane.showConfirmDialog( parentDashboard,
                     confirmMsg, "Confirm", JOptionPane.OK_CANCEL_OPTION );
               if (retval != JOptionPane.OK_OPTION)
                  return;
            }

            String sql = button.get( "sql" );
            sql = sql.replaceAll( "&", seqValue );
            RDSUtil.inform( "%s button: [%s]", caption, sql );
            int rowCount = db.execute( sql );
            admin.log( String.format( "execute %s button for %s = [%s]",
                  caption, seqField, seqValue ) );
 
            String resultMsg = button.get( (rowCount > 0) ?
                  "successMsg" : "failureMsg" );
            if (resultMsg != null && !resultMsg.isEmpty()) {
               JOptionPane.showMessageDialog( parentDashboard, resultMsg );
            }

            refresh();
            return;
         }
      }
   }

}