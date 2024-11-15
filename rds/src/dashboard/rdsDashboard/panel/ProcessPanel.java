/*
 * ProcessPanel.java
 * 
 * (c) 2009--2011, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.RDSStatusElement;
import rdsDashboard.RDSStatusElement.Status;


/**
 * A dashboad panel to provide an interface for viewing and managing system
 * and application-specific processes.
 */
public class ProcessPanel
      extends RDSDashboardPanel
      implements ActionListener {
   private static final Color ALERT_COLOR = Color.RED;
   private static final Color WARNING_COLOR = Color.YELLOW;

   private JTabbedPane tabs;
   private RDSTable processTable, commandTable;
   private JTextField commandField;
   private RDSStatusElement statusElement;

   private String selectedMachine;

   /**
    * Constructs a panel to view and manage processes.
    * 
    * @param   parentContainer  the parent container of this panel
    */
   public ProcessPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "System Processes" );

      createUI();

      statusElement = new RDSStatusElement( "Software Status" );
      if (parentContainer instanceof RDSDashboard) {
         ((RDSDashboard)parentContainer).registerStatusElement( this,
               statusElement );
         statusElement.getLabel().addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
               setSelectedTab( "Process Monitor" );
            }
         } );
      }
   }

   /**
    * Creates the user interface for the panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );
      tabs.add( "Process Monitor", createProcessMonitorPanel() );
      tabs.add( "Command Entry", createCommandPanel() );
   
      add( createMachineSelectionPanel(), BorderLayout.NORTH );
      add( tabs, BorderLayout.CENTER );
   }

   /**
    * Refreshes the panel contents.  The status element is always updated;
    * panel contents are updated only when the panel is currently visible.
    */
   @Override
   public void refreshPanel() {
      updateStatusElement();

      if (!isVisible())
         return;

      int currentTab = tabs.getSelectedIndex();
      if (currentTab == tabs.indexOfTab( "Process Monitor" ))
         refreshProcessMonitor();
      else if (currentTab == tabs.indexOfTab( "Command Entry" ))
         refreshCommandHistory();
   }

   /**
    * Creates a panel that contains a pull-down menu for machine selection.
    * 
    * @return  the panel object
    */
   private JPanel createMachineSelectionPanel() {
      // create and populate the machine-selection combo box
      final JComboBox machineCombo = new JComboBox();
      List<String> hostList = db.getValueList(
            "SELECT DISTINCT host FROM launch " +
            "ORDER BY host" );
      if (hostList == null || hostList.isEmpty())
         machineCombo.addItem( "localhost" );
      else
         for (String host : hostList) {
            if (host == null || host.isEmpty())
               host = "localhost";
            machineCombo.addItem( host );
         }
      machineCombo.setEditable( false );
      machineCombo.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            selectedMachine = machineCombo.getSelectedItem().toString();
            refreshPanel();
         }
      } );
      selectedMachine = machineCombo.getSelectedItem().toString();

      JPanel p = new JPanel();
      p.add( new JLabel( "Select machine: " ) );
      p.add( machineCombo );

      return p;
   }

   /**
    * Creates a panel for monitoring system and application processes.
    * 
    * @return  the panel object
    */
   private JPanel createProcessMonitorPanel() {
      processTable = new RDSTable( db,
            "Ordinal", "Process", "Mode", "PID", "Count", "Throttled",
            "Last Started", "Elapsed", "Control" ) {
         @Override
         protected void createDefaultRenderers() {
            defaultRenderersByColumnClass = new UIDefaults(8, 0.75f);
            setDefaultRenderer( Object.class, new ProcessTableCellRenderer() );
         }
      };
      processTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      processTable.getColumn( "Control" ).setCellRenderer(
            new ControlColumnRenderer() );
      processTable.getColumn( "Control" ).setCellEditor(
            new ControlColumnEditor( processTable ) );
      processTable.setColumnEditable( "Control", true );

      processTable.setColumnWidths( "Ordinal",   0, 0, 0 );  // hidden
      processTable.setColumnWidths( "Throttled", 0, 0, 0 );  // hidden
      processTable.setColumnWidths( "Elapsed",   0, 0, 0 );  // hidden
      processTable.setColumnWidths( "Mode",          50,  75, 100 );
      processTable.setColumnWidths( "PID",           25,  50,  75 );
      processTable.setColumnWidths( "Count",         25,  50,  75 );
      processTable.setColumnWidths( "Last Started", 100, 125, 150 );
      processTable.setColumnWidths( "Control",       75,  75, 100 );

      processTable.setColumnAlignment( "Mode", SwingConstants.CENTER );
      processTable.setColumnAlignment( "PID", SwingConstants.CENTER );
      processTable.setColumnAlignment( "Count", SwingConstants.CENTER );
      processTable.setColumnAlignment( "Last Started", SwingConstants.CENTER );

      processTable.addMouseListener( new MouseAdapter() {
         @Override
         public void mouseClicked( MouseEvent evt ) {
            if (evt.getButton() == MouseEvent.BUTTON1 &&
                  evt.getClickCount() == 2) {
               editProcess( processTable.rowAtPoint( evt.getPoint() ) );
            }
         }
      } );

      JPanel p = new JPanel( new BorderLayout( PADDING, PADDING ) ) {
         public void setVisible( boolean isVisible ) {
            super.setVisible( isVisible );
            refreshProcessMonitor();
         }
      };
      p.setBorder( BorderFactory.createEmptyBorder(
            PADDING, PADDING, PADDING, PADDING ) );
      p.add( processTable.getScrollPane(), BorderLayout.CENTER );

      return p;
   }

   /**
    * Creates a panel for entering/viewing commands to manage system and
    * application processes.
    * 
    * @return  the panel object
    */
   private JPanel createCommandPanel() {
      JPanel systemPanel = new JPanel();
      systemPanel.setBorder( BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "System Commands" ) );
      JButton rebootButton = new JButton( "Reboot Machine" );
      rebootButton.addActionListener( this );
      systemPanel.add( rebootButton );
      JButton shutdownButton = new JButton( "Shutdown Machine" );
      shutdownButton.addActionListener( this );
      systemPanel.add( shutdownButton );

      JPanel customPanel = new JPanel();
      customPanel.setBorder( BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Custom Command" ) );
      commandField = new JTextField( 20 );
      customPanel.add( commandField );
      JButton customButton = new JButton( "Execute" );
      customButton.addActionListener( this );
      customPanel.add( customButton );

      JPanel commandsPanel = new JPanel();
      commandsPanel.setLayout( new BoxLayout( commandsPanel,
            BoxLayout.X_AXIS ) );
      commandsPanel.add( systemPanel );
      commandsPanel.add( customPanel );

      JPanel headerPanel = new JPanel( new FlowLayout(
            FlowLayout.CENTER, 0, 0 ) );
      headerPanel.add( commandsPanel );

      commandTable = new RDSTable( db,
            "Command", "Complete", "Timestamp" );

      commandTable.getColumn( "Complete" ).setCellRenderer(
            new RDSTableCellRenderer.BooleanRenderer( "true" ) );

      commandTable.setColumnWidths( "Complete", 75, 75, 75 );
      commandTable.setColumnWidths( "Timestamp", 125, 125, 125 );

      commandTable.setColumnAlignment( "Complete", SwingConstants.CENTER );

      JPanel tablePanel = new JPanel( new BorderLayout() );
      tablePanel.setBorder( BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "Command History" ) );
      tablePanel.add( commandTable.getScrollPane(), BorderLayout.CENTER );

      JPanel p = new JPanel( new BorderLayout( PADDING, PADDING ) ) {
         public void setVisible( boolean isVisible ) {
            super.setVisible( isVisible );
            refreshCommandHistory();
         }
      };
      p.setBorder( BorderFactory.createEmptyBorder(
            PADDING, PADDING, PADDING, PADDING ) );
      p.add( headerPanel, BorderLayout.NORTH );
      p.add( tablePanel, BorderLayout.CENTER );

      return p;
   }

   /**
    * Updates the status element.
    */
   private void updateStatusElement() {
      int numThrottled = Integer.valueOf( db.getValue(
            "SELECT COUNT(*) FROM launch " +
            "WHERE throttled = 'yes' " +
            "AND mode = 'daemon' " +
            "AND ordinal > 0",
            "0" ) );
      if (numThrottled > 0) {
         statusElement.setStatus( Status.Error );
         return;
      }

      int numRecent = Integer.valueOf( db.getValue(
            "SELECT COUNT(*) FROM launch " +
            "WHERE throttled = 'no' " +
            "AND lastStart > DATE_SUB( NOW(), INTERVAL 1 MINUTE ) " +
            "AND mode = 'daemon' " +
            "AND ordinal > 0",
            "0" ) );
      if (numRecent > 0) {
         statusElement.setStatus( Status.Caution );
         return;
      }

      statusElement.setStatus( Status.OK );
   }

   /**
    * Updates the process monitor table.
    */
   private void refreshProcessMonitor() {
      String query =
            "SELECT ordinal, displayName, mode, pid, count, " +
            "throttled, lastStart, " +
            "UNIX_TIMESTAMP() - UNIX_TIMESTAMP( lastStart ), " +
            "IF(mode = 'daemon', 'Restart'," +
               "IF(mode = 'startonce', 'Run', '')) " +
            "FROM launch " +
            "WHERE host = '" + selectedMachine + "' " +
            "ORDER BY ordinal";

      try {
         processTable.populateTable( query );
      } catch (SQLException ex) {
         RDSUtil.alert( "%s: error populating process table, query = [%s]",
               getName(), query );
         RDSUtil.alert( ex );
      }
   }

   /**
    * Updates the command history table.
    */
   private void refreshCommandHistory() {
      String query =
            "SELECT command, completed, stamp " +
            "FROM execute " +
            "WHERE host = '" + selectedMachine + "' " +
            "ORDER BY seq DESC LIMIT 50";
      try {
         commandTable.populateTable( query );
      } catch (SQLException ex) {
         RDSUtil.alert(
               "%s: error updating command history table, query = [%s]",
               getName(), query );
         RDSUtil.alert( ex );
      }
      return;
   }

   /**
    * Sets the currently selected tab.
    * 
    * @param   title  the name of the desired tab
    */
   public void setSelectedTab( String title ) {
      tabs.setSelectedIndex( tabs.indexOfTab( title ) );
   }

   /**
    * Handles actions generated by this panel.
    */
   public void actionPerformed( ActionEvent evt ) {
      boolean custom = false;

      String cmd = "";
      if (evt.getActionCommand().equals( "Reboot Machine" ))
         cmd = "sudo /sbin/reboot";
      else if (evt.getActionCommand().equals( "Shutdown Machine" ))
         cmd = "sudo /sbin/poweroff";
      else if (evt.getActionCommand().equals( "Execute" )) {
         cmd = commandField.getText().trim();
         custom = true;
      }

      // check inputs and authorization
      if (selectedMachine == null || selectedMachine.isEmpty() ||
            cmd.isEmpty())
         return;
      if (!custom && !admin.isAuthenticatedInteractive(
            "process control", this ))
         return;
      if (custom && !admin.isAuthenticatedInteractive(
            "advanced process control", this ))
         return;

      // schedule the command for execution
      db.execute(
            "INSERT INTO execute ( host, command ) " +
            "VALUES ( '" + selectedMachine + "', '" + cmd + "' )" );
      refreshCommandHistory();
      admin.log( getName() + ": run command [" + cmd + "] on [" +
            selectedMachine + "]" );
   }

   /**
    * Edits a single process from a row of the process table.
    * 
    * @param  row  the selected row
    */
   private void editProcess( int row ) {
      if (!admin.isAuthenticatedInteractive(
            "advanced process control", this ))
         return;

      int ordinal = (Integer)processTable.getValueAt( row, "Ordinal" );
      boolean editSuccess = showProcessEditDialog( ordinal );
      if (editSuccess) {
         refreshProcessMonitor();
         admin.log( String.format(
               "%s: modified process with ordinal [%d] on [%s]",
               getName(), ordinal, selectedMachine ) );
      }
   }

   /**
    * Handles process control on a row of the process table.
    * 
    * @param   row  the selected row
    */
   private void controlProcess( int row ) {
      if (row < 0)
         return;

      int ordinal = (Integer)processTable.getValueAt( row, "Ordinal" );
      String mode = (String)processTable.getValueAt( row, "Mode" );
      String process = (String)processTable.getValueAt( row, "Process" );

      String control = "";
      if ("daemon".equals( mode ))
         control = "Restart";
      else if ("startonce".equals( mode ))
         control = "Run";
      else
         return;

      if (!admin.isAuthenticatedInteractive( "process control", this ))
         return;

      int confirmOption = JOptionPane.showConfirmDialog(
            this,
            control + " " + process + "?",
            "Process Control",
            JOptionPane.OK_CANCEL_OPTION );
      if (confirmOption != JOptionPane.OK_OPTION)
         return;

      db.execute(
            "UPDATE launch SET " +
            "operation = 'trigger' " +
            "WHERE HOST = '%s' " +
            "AND ordinal = %d",
            selectedMachine, ordinal );

      admin.log( String.format(
            "%s: %s [%s] on [%s]",
            getName(), control.toLowerCase(), process, selectedMachine ) );
   }

   /**
    * Displays a dialog that allows editing of process parameters.
    * 
    * @param   ordinal  the ordinal that identifies the process
    * @return  {@code true} if the edit completed successfully, {@code
    *          false} otherwise
    */
   private boolean showProcessEditDialog( int ordinal ) {
      final int GAP = 5;

      Map<String,String> processMap = db.getRecordMap(
            "SELECT * FROM launch " +
            "WHERE HOST = '%s' " +
            "AND ordinal = %d",
            selectedMachine, ordinal );
      if (processMap == null || processMap.isEmpty())
         return false;

      // create a subpanel for the modifiable fields
      JPanel fieldsPanel = new JPanel( new SpringLayout() );
      int fields = 0;

      JTextField descriptionField = new JTextField(
            processMap.get( "displayName" ) );
      fieldsPanel.add( new JLabel( "Description:", JLabel.RIGHT ) );
      fieldsPanel.add( descriptionField );
      fields++;

      JTextField processField = new JTextField( processMap.get( "process" ) );
      fieldsPanel.add( new JLabel( "Process:", JLabel.RIGHT ) );
      fieldsPanel.add( processField );
      fields++;

      JTextField argsField = new JTextField( processMap.get( "args" ) );
      fieldsPanel.add( new JLabel( "Arguments:", JLabel.RIGHT ) );
      fieldsPanel.add( argsField );
      fields++;

      JTextField dirField = new JTextField( processMap.get( "home" ) );
      fieldsPanel.add( new JLabel( "Directory:", JLabel.RIGHT ) );
      fieldsPanel.add( dirField );
      fields++;

      JSpinner delaySpinner = new JSpinner();
      ((SpinnerNumberModel)delaySpinner.getModel()).setMinimum( 0 );
      delaySpinner.setPreferredSize( new Dimension( 40, 20 ) );
      delaySpinner.setValue( Integer.valueOf(
            processMap.get( "delayAfter" ) ) );
      JPanel delaySpinnerPanel = new JPanel(
            new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      delaySpinnerPanel.add( delaySpinner );
      fieldsPanel.add( new JLabel( "Delay:", JLabel.RIGHT ) );
      fieldsPanel.add( delaySpinnerPanel );
      fields++;

      SpringUtilities.makeCompactGrid( fieldsPanel, fields, 2,
            GAP, GAP, GAP, GAP );

      // create a subpanel with a mode selector
      JLabel modeLabel = new JLabel( "Run Mode:", JLabel.CENTER );
      modeLabel.setAlignmentX( JLabel.CENTER_ALIGNMENT );
      JRadioButton daemonButton = new JRadioButton( "daemon" );
      JRadioButton startonceButton = new JRadioButton( "startonce" );
      JRadioButton manualButton = new JRadioButton( "manual" );

      ButtonGroup modeGroup = new ButtonGroup();
      modeGroup.add( daemonButton );
      modeGroup.add( startonceButton );
      modeGroup.add( manualButton );

      JPanel modeButtonsPanel = new JPanel(
            new FlowLayout( FlowLayout.CENTER, GAP, 0 ) );
      modeButtonsPanel.add( daemonButton );
      modeButtonsPanel.add( startonceButton );
      modeButtonsPanel.add( manualButton );

      JPanel modePanel = new JPanel();
      modePanel.setLayout( new BoxLayout( modePanel, BoxLayout.Y_AXIS ) );
      modePanel.setBorder( BorderFactory.createEmptyBorder(
            GAP, GAP, GAP, GAP ) );
      modePanel.add( modeLabel );
      modePanel.add( modeButtonsPanel );

      String currentMode = processMap.get( "mode" );
      if ("daemon".equals( currentMode ))
         daemonButton.setSelected( true );
      else if ("startonce".equals( currentMode ))
         startonceButton.setSelected( true );
      else if ("manual".equals( currentMode ))
         manualButton.setSelected( true );

      // construct the overall panel
      JPanel editPanel = new JPanel();
      editPanel.setLayout( new BoxLayout( editPanel, BoxLayout.Y_AXIS ) );
      editPanel.add( fieldsPanel );
      editPanel.add( modePanel );

      int returnValue = JOptionPane.showConfirmDialog(
            this,                          // parent component
            editPanel,                     // message object
            "Edit Process",                // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return false;

      String displayName = descriptionField.getText().trim();
      String home = dirField.getText().trim();
      String process = processField.getText().trim();
      String args = argsField.getText().trim();
      int delayAfter = (Integer)delaySpinner.getValue();
      String mode = "";
      for (AbstractButton modeButton :
            Collections.list( modeGroup.getElements() )) {
         if (modeButton.isSelected())
            mode = modeButton.getText();
      }

      int updateRows = db.execute( String.format(
            "UPDATE launch SET " +
            "displayName = '%s', " +
            "home = '%s', " +
            "process = '%s', " +
            "args = '%s', " +
            "delayAfter = %d, " +
            "mode = '%s' " +
            "WHERE HOST = '%s' " +
            "AND ordinal = %d",
            displayName, home, process, args, delayAfter, mode,
            selectedMachine, ordinal ) );

      return (updateRows > 0);
   }

   private static Color getHighlightColor( RDSTable table, int row ) {
      Color color = table.getBackground();
      if ("yes".equals( (String)table.getValueAt( row, "Throttled" ) )) {
         color = ALERT_COLOR;
      } else if ((Long)table.getValueAt( row, "Elapsed" ) < 60) {
         color = WARNING_COLOR;
      }

      if (table.isAlternateRow( row ))
         color = RDSTableCellRenderer.getAlternateColor( color );

      return color;
   }

   /**
    * A custom table cell renderer that highlights a row under certain
    * conditions.
    */
   private class ProcessTableCellRenderer
         extends RDSTableCellRenderer {
      SimpleDateFormat dateFormatter;

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column ) {
         super.getTableCellRendererComponent( table, value, isSelected,
               hasFocus, row, column );
         if (!isSelected && table instanceof RDSTable)
            setBackground( getHighlightColor( (RDSTable)table, row ) );

         return this;
      }

      @Override
      protected void setValue( Object value ) {
         if (value instanceof java.util.Date) {
            if (dateFormatter == null)
               dateFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            setText( (value == null) ? "" : dateFormatter.format( value ) );
         } else
            setText( (value == null) ? "" : value.toString() );
      }
   }  // end ProcessPanel.ProcessTableCellRenderer class

   private class ControlColumnRenderer
         extends RDSTableCellRenderer.ButtonRenderer {

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column ) {
         super.getTableCellRendererComponent( table, value, isSelected,
               hasFocus, row, column );
         if (!isSelected && table instanceof RDSTable)
            setBackground( getHighlightColor( (RDSTable)table, row ) );

         return this;
      }
   }

   private class ControlColumnEditor
         extends RDSTableCellRenderer.ButtonEditor
         implements ActionListener {
      private int row;

      public ControlColumnEditor( RDSTable table ) {
         super();
         button.addActionListener( this );
      }

      @Override
      public Component getTableCellEditorComponent( JTable table,
            Object value, boolean isSelected, int row, int column ) {
         this.row = row;

         super.getTableCellEditorComponent( table, value, isSelected,
               row, column );
         if (!isSelected && table instanceof RDSTable)
            button.setBackground( getHighlightColor( (RDSTable)table, row ) );

         return button;
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
         controlProcess( row );
      }
   }

}  // end ProcessPanel class
