/*
 *   TracePanel.java
 *   Reads trace messages from a local server and displays them.  Messages
 *   may be filtered by program, alert level, or content.
 *
 *   (C) Copyright 1999-2006 Numina Systems Corporation.  All Rights Reserved.
 *
 *   991114 - initial version from chat example (mrw)
 *   010209 - modified to use command prefixes
 *   060526 - major update: use swing components; new look-and-feel; add
 *            program, level, and message filters; pause button --AHM
 *   060828 - incorporate into dashboard framework --AHM
 */

package rdsDashboard.panel;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for viewing recent system trace messages.
 */
public class TracePanel
      extends RDSDashboardPanel
      implements Runnable, ActionListener {
   private static final int NUM_LINES = 100;
   private static final int MAX_MSG_LEN = 72;
   private static final int PORT = 20006;
   private static final int MARGIN = 5;

   private boolean pauseState = false;
   private String msgFilter = new String( "" );

   protected JList programList;
   protected JComboBox maxLvlCombo;
   protected JTextField filterField;
   protected JButton pauseButton;
   protected RDSTable messageTable;
   protected JLabel statusLabel;

   // constructor
   public TracePanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Trace Messages" );
      createUI();

      statusLabel.setText( "Start" );
      new Thread( this ).start();
   }

   // run() [Runnable], invoked by Thread.start() in constructor
   @Override
   public void run() {
      String host = this.getParam( "host" );
      if (host == null)
         host = baseURL.getHost();
      RDSUtil.trace( getName() + ": connecting to [" + host + ":" +
            PORT + "]" );

      // get parameters, connect, and execute
      try {
         statusLabel.setText( "Connecting..." );
         Socket s = new Socket( host, PORT );
         statusLabel.setText( "Connected" );

         execute( s );
      } catch (IOException ex) {
         statusLabel.setText( "Failed to connect" );
      }
   }
   
   // createUI() [local], invoked by constructor
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( true );

      // program list
      programList = new JList( new DefaultListModel() );
      programList.setVisibleRowCount( 5 );
      JScrollPane programListPane = new JScrollPane( programList );
      programListPane.setPreferredSize( new Dimension( 100, 75 ) );

      // max level selector combobox
      JPanel maxLvlPanel = new JPanel( new BorderLayout() );
      JLabel maxLvlLabel = new JLabel( "Maximum Level", JLabel.CENTER );
      String levels[] = { "2 - Inform", "1 - Trace", "0 - Alert" };
      maxLvlCombo = new JComboBox( levels );
      maxLvlPanel.add( maxLvlLabel, BorderLayout.NORTH );
      maxLvlPanel.add( maxLvlCombo, BorderLayout.CENTER );

      // message filter textfield
      JLabel filterLabel = new JLabel( "Message Filter" );
      filterField = new JTextField();
      filterField.addActionListener( this );
      JPanel filterPanel = new JPanel( new BorderLayout() );
      filterPanel.add( filterLabel, BorderLayout.NORTH );
      filterPanel.add( filterField, BorderLayout.CENTER );

      // pause button
      pauseButton = new JButton( "Pause" );
      pauseButton.addActionListener( this );

      // header panel
      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new FlowLayout() );
      headerPanel.add( programListPane );
      headerPanel.add( Box.createHorizontalStrut( SPACING ) );
      headerPanel.add( maxLvlPanel );
      headerPanel.add( Box.createHorizontalStrut( SPACING ) );
      headerPanel.add( filterPanel );
      headerPanel.add( Box.createHorizontalStrut( SPACING ) );
      headerPanel.add( pauseButton );
      this.add( headerPanel, BorderLayout.NORTH );

      // message table
      messageTable = new RDSTable( "Time", "Program", "Lvl", "Message" );
      ((javax.swing.table.DefaultTableModel)messageTable.
            getModel()).setRowCount( NUM_LINES );
      messageTable.setAlternateBlockSize( 0 );  // disable shading

      // message table formatting
      messageTable.setColumnWidths( 0, 60, 60, 60 );
      messageTable.setColumnWidths( 1, 75, 75, 100 );
      messageTable.setColumnWidths( 2, 35, 35,  35 );
      messageTable.getColumn( "Lvl" ).setCellRenderer(
            createLevelCellRenderer() );

      JScrollPane messageTablePane = messageTable.getScrollPane();
      messageTablePane.getViewport().scrollRectToVisible(
            messageTable.getCellRect( NUM_LINES - 1, 0, true ) );
      messageTablePane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

      this.add( messageTablePane, BorderLayout.CENTER );

      // status label
      statusLabel = new JLabel( "Init", SwingConstants.RIGHT );
      statusLabel.setBorder( BorderFactory.createEmptyBorder(
            0, MARGIN, 0, MARGIN ) );

      this.add( statusLabel, BorderLayout.SOUTH );
   }

   // execute() [local], invoked in run()
   private void execute( Socket s ) {
      BufferedReader in = null;
      String msg;

      try {
         in = new BufferedReader( new InputStreamReader(
               s.getInputStream() ) );
         while ((msg = in.readLine()) != null)
            process( msg );
         in.close();
      } catch (IOException ex) {
         RDSUtil.alert( getName() + ": I/O error on input stream" );
         RDSUtil.alert( ex );
      }
   }

   // process() [local], invoked during execute()
   private void process( String str ) {
      String fields[] = str.split( "\t", 4 );
      if (fields.length < 4)
         return;

      // String stamp = fields[0] ;
      String program = fields[1];
      int level = Integer.parseInt( fields[2] );
      String msg = fields[3];

      // add new programs to (sorted) list, select by default
      DefaultListModel programListModel = (DefaultListModel)programList
            .getModel();
      if (!programListModel.contains( program )) {
         int idx = 0;
         while (idx < programListModel.getSize()
               && program.compareToIgnoreCase(
               (String)programListModel.get( idx ) ) > 0)
            idx++;
         programListModel.add( idx, program );
         programList.addSelectionInterval( idx, idx );
      }

      // add messages to table
      String maxLvlStr = (String)maxLvlCombo.getSelectedItem();
      int maxLevel = Integer.parseInt( maxLvlStr.substring( 0, 1 ) );
      if (pauseState == false && level <= maxLevel
            && isProgramSelected( program ) && filterMatch( msg )) {
         while (msg.length() > MAX_MSG_LEN) {
            // split long messages over multiple lines
            fields[3] = msg.substring( 0, MAX_MSG_LEN );
            messageTable.addRow( (Object[])fields );

            // construct the following row
            fields[0] = "";
            fields[1] = "";
            fields[2] = "";
            msg = msg.substring( MAX_MSG_LEN );
         }
         fields[3] = msg;
         messageTable.addRow( (Object[])fields );

         while (messageTable.getRowCount() > NUM_LINES)
            messageTable.removeRow( 0 );
      }

      return;
   }

   // isProgramSelected() [local], invoked during process()
   private boolean isProgramSelected( String program ) {
      return programList.isSelectedIndex(
            ((DefaultListModel)programList.getModel()).indexOf( program ) );
   }

   // filterMatch() [local], invoked during process()
   private boolean filterMatch( String msg ) {
      boolean match;

      try {
         match = msg.matches( ".*" + msgFilter + ".*" );
      } catch (Exception ex) {
         match = true;
      }
      return match;
   }

   // actionPerformed() [ActionListener]
   public void actionPerformed( ActionEvent evt ) {
      if (evt.getSource() == filterField) {
         msgFilter = filterField.getText();
      } else if (evt.getSource() == pauseButton) {
         if (pauseState) { // reconnect after resuming from pause
            pauseButton.setSelected( false );
            pauseState = false;
            statusLabel.setText( "Connected" );
         } else {
            pauseButton.setSelected( true );
            pauseState = true;
            statusLabel.setText( "Paused" );
         }
      }
      return;
   }

   private RDSTableCellRenderer createLevelCellRenderer() {
      RDSTableCellRenderer.IconRenderer renderer =
            new RDSTableCellRenderer.IconRenderer();
      renderer.mapIcon( "0", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/red.png" ) );
      renderer.mapIcon( "1", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/gray.png" ) );
      renderer.mapIcon( "2", RDSUtil.createImageIcon(
            RDSDashboard.class, "images/yellow.png" ) );
      return renderer;
   }

} // end TracePanel class