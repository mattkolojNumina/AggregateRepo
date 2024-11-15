/*
 * ReportEditorPanel.java
 * 
 * (c) 2008-2010, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.tree.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.report.ReportSQL;


/**
 * A dashboad panel for creating and editing reports.
 */
public class ReportEditorPanel
   extends RDSDashboardPanel {

   // constants
   private static final int GAP = 15;
   private static final int SMALL_GAP = 5;
   private static final int BIG_GAP = 45;
   private static final String SEPARATOR = "/";

   // ui variables
   private JComboBox reportCombo;
   private JTextField titleField;
   private JComboBox reportTypeCombo;
   private JPanel detailsPanel;
   private JTree counterTree;
   private JTextField columnsField;
   private JTextArea sqlArea;

   /**
    * Constructs a panel for editing reports.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public ReportEditorPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Report Editor" );
      setDescription( "Edit production reports" );

      createUI();
      refreshCountersTree();
   }

   private String getReport() {
      if (reportCombo == null)
         return "";

      Object selectedItem = reportCombo.getSelectedItem();
      if (selectedItem instanceof String)
         return (String)selectedItem;

      return "";
   }

   /**
    * Creates the user interface for the panel.
    */
   private void createUI() {
      setLayout( new BorderLayout( GAP, GAP ) );
      createTitledBorder( true );

      add( createReportPropertiesPanel(), BorderLayout.NORTH);

      detailsPanel = new JPanel( new CardLayout() );
      detailsPanel.add( createCounterPanel(), "counters" );
      detailsPanel.add( createSQLDetails(), "sql" );
      add( detailsPanel, BorderLayout.CENTER);

      populateReportCombo();
      populateProperties();
   }

   /** Creates a panel for selecting and editing report properties. */
   private JPanel createReportPropertiesPanel() {
      JPanel topPanel = new JPanel();
      topPanel.add( createReportSelectorPanel() );
      topPanel.add( Box.createHorizontalStrut( GAP ) );
      topPanel.add( createButtonPanel() );

      JPanel propertiesPanel = new JPanel();
      propertiesPanel.setLayout( new BoxLayout( propertiesPanel,
            BoxLayout.Y_AXIS ) );
      propertiesPanel.add( topPanel );
      propertiesPanel.add( createTypeTitlePanel() );

      return propertiesPanel;
   }

   private JPanel createReportSelectorPanel() {
      reportCombo = new JComboBox();
      reportCombo.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            populateProperties();
         }
      } );

      JPanel reportPanel = new JPanel();
      reportPanel.setLayout( new BoxLayout( reportPanel, BoxLayout.X_AXIS ) );
      reportPanel.add( new JLabel( "Select a report: " ) );
      reportPanel.add( reportCombo );

      return reportPanel;
   }

   private JPanel createButtonPanel() {
      JPanel buttonPanel = new JPanel();

      JButton newButton = new JButton( "New" );
      newButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            String report = doReportName( "" );
            if ( !report.isEmpty() ) {
               db.execute( "INSERT INTO reports SET " +
                           "report='" + report + "', " +
                           "type='table', " +
                           "title='Report Title', " +
                           "params=''" );
               titleField.setText( "Report Title" );
               populateReportCombo();
               reportCombo.setSelectedItem( report );
               populateProperties();
            }
         }
      } );
      buttonPanel.add( newButton );

      JButton renameButton = new JButton( "Rename" );
      renameButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            String oldReport = getReport();
            String newReport = doReportName( oldReport );
            if ( !newReport.isEmpty() ) {
               db.execute( "UPDATE reports SET " +
                           "report='" + newReport + "' " +
                           "WHERE report='" + oldReport + "'" );
               db.execute( "UPDATE reportItems SET " +
                           "report='" + newReport + "' " +
                           "WHERE report='" + oldReport + "'" );
               populateReportCombo();
               reportCombo.setSelectedItem( newReport );
            }
         }
      } );
      buttonPanel.add( renameButton );

      JButton deleteButton = new JButton( "Delete" );
      deleteButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            doReportDelete( getReport() );
         }
      } );
      buttonPanel.add( deleteButton );

      JButton saveButton = new JButton( "Save" );
      saveButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            doReportSave( getReport() );
         }
      } );
      buttonPanel.add( saveButton );

      return buttonPanel;
   }

   private JPanel createTypeTitlePanel() {
      titleField = new JTextField( 32 );
      titleField.setMaximumSize( titleField.getPreferredSize() );

      reportTypeCombo = new JComboBox();
      reportTypeCombo.addItem( "Table" );
      reportTypeCombo.addItem( "Tree" );
      reportTypeCombo.addItem( "Trend Chart" );
      reportTypeCombo.addItem( "Pie Chart" );
      reportTypeCombo.addItem( "SQL Table" );
      reportTypeCombo.setMaximumSize( reportTypeCombo.getPreferredSize() );
      reportTypeCombo.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            String typeName = (String)reportTypeCombo.getSelectedItem();
            if ("SQL Table".equals( typeName ))
               showDetails( "sql" );
            else
               showDetails( "counters" );
         }
      } );

      JPanel typeTitlePanel = new JPanel();
      typeTitlePanel.setLayout( new BoxLayout( typeTitlePanel,
            BoxLayout.X_AXIS ) );
      typeTitlePanel.add( Box.createHorizontalGlue() );
      typeTitlePanel.add( new JLabel( "Report title: " ) );
      typeTitlePanel.add( titleField );
      typeTitlePanel.add( Box.createHorizontalStrut( BIG_GAP ) );
      typeTitlePanel.add( new JLabel( "Report type: " ) );
      typeTitlePanel.add( reportTypeCombo );
      typeTitlePanel.add( Box.createHorizontalGlue() );

      return typeTitlePanel;
   }

   private JPanel createCounterPanel() {
      JLabel instructionLabel = new JLabel(
            "<html>Select the counters to include in this report. " +
            "Shift-click to select a sub-tree of counters. " +
            "Right-click to edit a counter description.</html>",
            JLabel.LEFT );

      JButton selectAllButton = new JButton( "Select All" );
      selectAllButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            setTreeSelected(
                  (DefaultMutableTreeNode)counterTree.getModel().getRoot(),
                  true );
         }
      } );

      JButton selectNoneButton = new JButton( "Select None" );
      selectNoneButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            setTreeSelected(
                  (DefaultMutableTreeNode)counterTree.getModel().getRoot(),
                  false );
         }
      } );

      JPanel buttonPanel = new JPanel();
      buttonPanel.add( selectAllButton );
      buttonPanel.add( selectNoneButton );

      JPanel counterPanel = new JPanel( new BorderLayout(
            SMALL_GAP, SMALL_GAP ) );
      counterPanel.setBorder( BorderFactory.createEmptyBorder(
            0, BIG_GAP, 0, BIG_GAP ) );
      counterPanel.add( instructionLabel, BorderLayout.NORTH );
      counterPanel.add( createCounterTree(), BorderLayout.CENTER );
      counterPanel.add( buttonPanel, BorderLayout.SOUTH );

      return counterPanel;
   }

   /** Creates the tree for displaying the selectable counters. */
   private JScrollPane createCounterTree() {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode( "" ); 
      DefaultTreeModel treeModel = new DefaultTreeModel( root );
      counterTree = new JTree( treeModel );
      counterTree.setCellRenderer(new CheckBoxNodeRenderer());
      counterTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION );
      counterTree.setToggleClickCount( 0 );
      counterTree.addMouseListener( new MouseAdapter() {
         public void mousePressed( MouseEvent evt ) {
            handleTreePressed( evt );
         }
      } );

      return new JScrollPane( counterTree );
   }

   /** Creates the panel for specifying the sql query details. */
   private JPanel createSQLDetails() {
      columnsField = new JTextField( 20 );
      JPanel columnsPanel = new JPanel();
      columnsPanel.add( new JLabel( "Column headers (comma-separated): " ) );
      columnsPanel.add( columnsField );

      JLabel instructionLabel = new JLabel(
            "<html>Enter the SQL query to execute.  Use '{0}' and '{1}' as " +
            "placeholders for start and end time, respectively.</html>",
            JLabel.LEFT );

      sqlArea = new JTextArea( 4, 60 );
      sqlArea.setLineWrap( true );
      sqlArea.setWrapStyleWord( true );
      JScrollPane sqlPane = new JScrollPane( sqlArea );

      JPanel sqlAreaPanel = new JPanel( new BorderLayout(
            SMALL_GAP, SMALL_GAP ) );
      sqlAreaPanel.setBorder( BorderFactory.createEmptyBorder(
            0, BIG_GAP, BIG_GAP, BIG_GAP ) );
      sqlAreaPanel.add( instructionLabel, BorderLayout.NORTH );
      sqlAreaPanel.add( sqlPane, BorderLayout.CENTER );

      JPanel sqlPanel = new JPanel( new BorderLayout( GAP, GAP ) );
      sqlPanel.add( columnsPanel, BorderLayout.NORTH );
      sqlPanel.add( sqlAreaPanel, BorderLayout.CENTER );

      return sqlPanel;
   }

   /**
    * Fills in the selections for the machine combobox.
    */
   private void populateReportCombo() {
      List<String> reportList = db.getValueList(
            "SELECT DISTINCT report FROM reports " +
            "ORDER BY report" );
      reportCombo.removeAllItems();
      for (String report : reportList)
         reportCombo.addItem( report );
      reportCombo.setMaximumSize( reportCombo.getPreferredSize() );
   }


   private void populateProperties() {
      Map<String,String> reportMap = db.getRecordMap(
            "SELECT * FROM reports " +
            "WHERE report='" + getReport() + "'" );
      String title = (reportMap == null) ? null : reportMap.get( "title" );
      String type  = (reportMap == null) ? null : reportMap.get( "type" );

      titleField.setText( title );

      if ( "sqltable".equals( type ) ) {
         Pattern p = Pattern.compile( ReportSQL.PARAM_REGEX );
         Matcher m = p.matcher( reportMap.get( "params" ) );
         if (m.matches()) {
            columnsField.setText( m.group( 1 ) );
            sqlArea.setText( m.group( 2 ) );
         }
         reportTypeCombo.setSelectedItem( "SQL Table" );
      } else {
         if ( "table".equals( type ) )
            reportTypeCombo.setSelectedItem( "Table" );
         else if ( "tree".equals( type ) )
            reportTypeCombo.setSelectedItem( "Tree" );
         else if ( "trendchart".equals( type ) )
            reportTypeCombo.setSelectedItem( "Trend Chart" );
         else if ( "piechart".equals( type ) )
            reportTypeCombo.setSelectedItem( "Pie Chart" );
   
         refreshCountersTree();
      }
   }

   private void showDetails( String name ) {
      LayoutManager layoutManager = detailsPanel.getLayout();
      if (layoutManager instanceof CardLayout)
         ((CardLayout)layoutManager).show( detailsPanel, name );
   }

   /**
    * Updates the counters table.
    */
   private void refreshCountersTree() {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            new CheckBoxNode( getParam( "root" ), "All system counters" ) );
      DefaultTreeModel treeModel = new DefaultTreeModel( root );
      counterTree.setModel( treeModel );

      String sql =
            "SELECT " +
            "IF(ISNULL(reportItems.report),'false','true') AS selected, " +
            "counters.code AS code, " +
            "counters.description AS description " +
            "FROM counters LEFT JOIN reportItems " +
            "ON (counters.code=reportItems.code " +
            "AND reportItems.report='" + getReport() + "') " +
            "ORDER BY counters.code";
      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = stmt.executeQuery( sql );
         while ( res.next() ) {
            String code = res.getString( "code" );
            String description = res.getString( "description" );
            boolean selected = "true".equals( res.getString( "selected" ) );

            // create the node
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                  new CheckBoxNode( code, description, selected ) );

            // determine the structural position of the node
            TreePath parentPath = null;
            int separatorIndex = code.lastIndexOf( SEPARATOR ); 
            if ( separatorIndex > 0 ) {
               String rootCode = code.substring( 0, separatorIndex );
               parentPath = getFirstMatch( rootCode );
            }

            // add the node to the tree
            DefaultMutableTreeNode parentNode = null;
            if ( parentPath == null )
               parentNode = root;
            else
               parentNode =
                    (DefaultMutableTreeNode)parentPath.getLastPathComponent();
            parentNode.add( node );
            treeModel.reload( parentNode );
            counterTree.expandPath( parentPath );
         }
      } catch (SQLException ex) {
         RDSUtil.alert( getName() + ": sql error during table refresh, " +
                  "sql = [" + sql + "]");
         RDSUtil.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }

      for (int i = 0; i < counterTree.getRowCount(); i++)
         counterTree.expandRow( i );
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

   private void doDescriptionDialog( String code, String description ) {
      JTextField codeField = new JTextField( code, 10 );
      codeField.setEditable( false );

      JTextField descriptionField = new JTextField( description, 20 );

      JPanel descriptionPanel = new JPanel( new SpringLayout() );
      descriptionPanel.add( new JLabel( "Counter code: ", JLabel.RIGHT ) );
      descriptionPanel.add( codeField );
      descriptionPanel.add( new JLabel( "Description: ", JLabel.RIGHT ) );
      descriptionPanel.add( descriptionField );
      SpringUtilities.makeCompactGrid( descriptionPanel, 2, 2,
            SMALL_GAP, SMALL_GAP, SMALL_GAP, SMALL_GAP );

      JPanel containerPanel = new JPanel(
            new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
      containerPanel.add( descriptionPanel );

      int returnValue = JOptionPane.showConfirmDialog(
            this,                          // parent component
            containerPanel,                // message object
            "Edit Counter Description",    // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return;

      String errMsg = null;

      // input check
      String newDescription = descriptionField.getText().trim();
      if (newDescription == null || newDescription.isEmpty())
         errMsg = "No counter description provided";
      if ( !db.getValue( "SELECT description FROM reportItems " +
                         "WHERE description='" +
                         newDescription + "' LIMIT 1", "" ).equals("") )
         errMsg = "There is already a counter with the description '" +
                      newDescription+ "'";
      if ( !db.getValue( "SELECT description FROM counters " +
                         "WHERE description='" +
                         newDescription + "' LIMIT 1", "" ).equals("") )
         errMsg = "There is already a counter with the description '" +
                      newDescription+ "'";

      if (errMsg != null) {
         JOptionPane.showMessageDialog( this, errMsg, "Input Error",
               JOptionPane.ERROR_MESSAGE );
         return;
      }

      db.execute(
            "UPDATE counters SET " +
            "description = '" + descriptionField.getText().trim() + "' " +
            "WHERE code = '" + code + "'" );
      db.execute(
            "UPDATE reportItems SET " +
            "description = '" + descriptionField.getText().trim() + "' " +
            "WHERE code = '" + code + "'" );
      refreshCountersTree();
   }


   private void doReportDelete( String report ) {
      final int GAP = 5;

      String title = "Alert";

      JPanel namePanel = new JPanel( new SpringLayout() );
      namePanel.add( new JLabel( 
             "Are you sure you want to delete the report [" + 
             report + "]?", JLabel.CENTER) );
      SpringUtilities.makeCompactGrid( namePanel, 1, 1,
            GAP, GAP, GAP, GAP );

      JPanel containerPanel = new JPanel(
            new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
      containerPanel.add( namePanel );

      int returnValue = JOptionPane.showConfirmDialog(
            this,
            containerPanel,                // message object
            title,                         // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return;

      db.execute( "DELETE FROM reportItems WHERE report='" +
                         report+ "'" );
      db.execute( "DELETE FROM reports WHERE report='" +
                         report+ "'" );

      populateReportCombo();
      populateProperties();

      return;
   }

   private void doReportSave( String report ) {
      String typeName = (String)reportTypeCombo.getSelectedItem();
      String type = "";
      String params = "";
      if ( "Table".equals( typeName ) )
         type = "table";
      else if ( "Tree".equals( typeName ) )
         type = "tree";
      else if ( "Trend Chart".equals( typeName ) )
         type = "trendchart";
      else if ( "Pie Chart".equals( typeName ) )
         type = "piechart";
      else if ( "SQL Table".equals( typeName ) ) {
         type = "sqltable";
         params = "[" + columnsField.getText().trim() + "] " +
               sqlArea.getText();
      }

      String sql = "UPDATE reports SET " +
            "title = ?, " +
            "type = ?, " +
            "params = ? " +
            "WHERE report = ?";
      db.executePreparedStatement( sql,
            titleField.getText().trim(), type, params, getReport() );
      saveCounters();
   }

   private void saveCounters() {
      DefaultMutableTreeNode rootNode =
         (DefaultMutableTreeNode)counterTree.getModel().getRoot();
      Enumeration<?> treeEnum = rootNode.preorderEnumeration();
      while (treeEnum.hasMoreElements()) {
         DefaultMutableTreeNode node =
               (DefaultMutableTreeNode)treeEnum.nextElement();
         CheckBoxNode cbNode = (CheckBoxNode)node.getUserObject();
         saveReportItem( cbNode.getCode(), cbNode.getText(),
               cbNode.isSelected() );
      }
   }

   private void saveReportItem( String code, String text, boolean selected ) {
      String report = getReport();
      if ( selected )
         db.execute( "REPLACE INTO reportItems SET " +
                     "report='%s', " +
                     "code='%s', " +
                     "description='%s'",
                     report, code, text );
      else
         db.execute( "DELETE FROM reportItems " +
                     "WHERE report='%s' " +
                     "AND code='%s'",
                     report, code );
   }

   private String doReportName( String report ) {
      JTextField nameField = new JTextField( report, 20 );
      nameField.setMaximumSize( nameField.getPreferredSize() );

      JPanel namePanel = new JPanel( new SpringLayout() );
      namePanel.add( new JLabel( "Enter the new report name: ", JLabel.RIGHT ) );
      namePanel.add( nameField );
      SpringUtilities.makeCompactGrid( namePanel, 1, 2,
            SMALL_GAP, SMALL_GAP, SMALL_GAP, SMALL_GAP );

      int returnValue = JOptionPane.showConfirmDialog(
            this,
            namePanel,                     // message object
            "Report Name",                 // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return "";

      String errMsg = null;

      // input check
      String newName = nameField.getText().trim();
      if (newName == null || newName.isEmpty())
         errMsg = "No report name entered.";
      if ( !db.getValue( "SELECT report FROM reports WHERE report='" +
                         newName + "'", "" ).equals("") )
         errMsg = "There is already a report named '" + newName + "'.";

      if (errMsg != null) {
         JOptionPane.showMessageDialog( this, errMsg, "Input Error",
               JOptionPane.ERROR_MESSAGE );
         return "";
      }

      return newName;
   }

   private void handleTreePressed( MouseEvent evt ) {
      TreePath path = counterTree.getPathForLocation( evt.getX(), evt.getY() );
      if (path == null)
         return;

      Object node = path.getLastPathComponent();
      if (node == null || !(node instanceof DefaultMutableTreeNode))
         return;

      Object userObject = ((DefaultMutableTreeNode)node).getUserObject();
      if (!(userObject instanceof CheckBoxNode))
         return;

      CheckBoxNode cbNode = (CheckBoxNode)userObject;
      if (SwingUtilities.isRightMouseButton( evt ))
         doDescriptionDialog( cbNode.getCode(), cbNode.getText() );
      else {
         boolean newSelection = !cbNode.isSelected();
         if (evt.isShiftDown())
            setTreeSelected( (DefaultMutableTreeNode)node, newSelection );
         else {
            cbNode.setSelected( newSelection );
            counterTree.repaint();
         }
      }
   }

   private void setTreeSelected( DefaultMutableTreeNode rootNode,
         boolean selected ) {
      Enumeration<?> treeEnum = rootNode.preorderEnumeration();
      while (treeEnum.hasMoreElements()) {
         DefaultMutableTreeNode node =
               (DefaultMutableTreeNode)treeEnum.nextElement();
         ((CheckBoxNode)node.getUserObject()).setSelected( selected );
      }
      counterTree.repaint();
   }

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private JCheckBox leafRenderer = new JCheckBox();

  private DefaultTreeCellRenderer nonLeafRenderer = 
            new DefaultTreeCellRenderer();

  Color selectionBorderColor,
        selectionForeground,
        selectionBackground,
        textForeground,
        textBackground;

  public CheckBoxNodeRenderer() {
    Font fontValue = UIManager.getFont("Tree.font");
    if (fontValue != null)
      leafRenderer.setFont(fontValue);

    Boolean drawFocus = (Boolean)UIManager.get(
          "Tree.drawsFocusBorderAroundIcon");
    leafRenderer.setFocusPainted(drawFocus != null && drawFocus);

    selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
    selectionForeground = UIManager.getColor("Tree.selectionForeground");
    selectionBackground = UIManager.getColor("Tree.selectionBackground");
    textForeground = UIManager.getColor("Tree.textForeground");
    textBackground = UIManager.getColor("Tree.textBackground");
  }

   public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf, int row,
      boolean hasFocus) {

      leafRenderer.setEnabled(tree.isEnabled());

      if (selected) {
        leafRenderer.setForeground(selectionForeground);
        leafRenderer.setBackground(selectionBackground);
      } else {
        leafRenderer.setForeground(textForeground);
        leafRenderer.setBackground(textBackground);
      }

      if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
        Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
        if (userObject instanceof CheckBoxNode) {
          CheckBoxNode node = (CheckBoxNode) userObject;
          if ( !node.editable ) {
             nonLeafRenderer.setText( node.getText() );
             return nonLeafRenderer;
          }
          leafRenderer.setText(node.getCode() + ": " + node.getText());
          leafRenderer.setSelected(node.isSelected());
          return leafRenderer;
        } 
      }
      return nonLeafRenderer.getTreeCellRendererComponent(tree,
              value, selected, expanded, leaf, row, hasFocus);
   }
}  // end CheckBoxNodeRenderer class


class CheckBoxNode {
  String code;
  String text;
  boolean selected;
  boolean editable;


  public CheckBoxNode(String code, String text) {
    this.code = code;
    this.text = text;
    this.selected = false;
    this.editable = false;
  }

  public CheckBoxNode(String code, String text, boolean selected) {
    this.code = code;
    this.text = text;
    this.selected = selected;
    this.editable = true;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean newValue) {
    selected = newValue;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String newValue) {
    code = newValue;
  }
  public String getText() {
    return text;
  }

  public void setText(String newValue) {
    text = newValue;
  }

  public String toString() {
    return code;
  }
} // end CheckBoxNode class

}  // end ReportEditor class
