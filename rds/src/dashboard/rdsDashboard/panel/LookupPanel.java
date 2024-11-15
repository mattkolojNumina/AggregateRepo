/*
 * LookupPanel.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel that displays information for a single carton.  By
 * default, only carton history is shown; this class is designed to be
 * extended in order to provide application-specific information in a
 * panel above the carton history.  Subclasses should override the
 * {@code createInfoPanel} method to layout and populate that panel.
 */
public class LookupPanel
      extends RDSDashboardPanel {
   private static final Font MSG_FONT = UIManager.getFont(
         "Label.font" ).deriveFont( Font.BOLD, 14.0f );
   private static final int SMALL_PADDING = 5;

   // data variables
   private String cartonId;

   // ui variables
   private JButton printButton;
   private JPanel centerPanel;
   private RDSTable histTable;

   /**
    * Creates a panel for displaying the status and history of a carton.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public LookupPanel( Container parentContainer ) {
      super( parentContainer );

      setName( "Carton Lookup" );
      setDescription( "Review carton information and processing history" );

      createUI();
      lookup( null );
   }

   /**
    * Creates the user interface for the panel.
    */
   private void createUI() {
      setLayout( new BorderLayout( PADDING, PADDING ) );
      createTitledBorder( true );

      add( createHeaderPanel(), BorderLayout.NORTH );
      add( createCenterPanel(), BorderLayout.CENTER );
   }

   /**
    * Creates the header panel that contains the carton id entry field
    * and other control elements.
    */
   private JPanel createHeaderPanel() {
      JTextField idField = new JTextField( 20 );
      idField.setHorizontalAlignment( JTextField.RIGHT );
      idField.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            lookup( evt.getActionCommand() );
         }
      } );

      JPanel idPanel = new JPanel();
      idPanel.add( new JLabel( "Enter carton ID:" ) );
      idPanel.add( idField );

      printButton = new JButton( "Print" );
      printButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            print( cartonId );
         }
      } );
      printButton.setEnabled( false );

      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.X_AXIS ) );
      headerPanel.add( Box.createHorizontalGlue() );
      headerPanel.add( idPanel );
      headerPanel.add( Box.createHorizontalGlue() );
      headerPanel.add( printButton );

      return headerPanel;
   }

   /**
    * Creates the center panel, initally empty.  The panel will be cleared
    * and re-populated based on the results of the lookup.
    */
   private JPanel createCenterPanel() {
      centerPanel = new JPanel( new BorderLayout(
            SMALL_PADDING, SMALL_PADDING ) );
      return centerPanel;
   }

   /**
    * Displays information (status, carton history, etc.) for a carton
    * ID.  If multiple cartons match the ID (a substring may be entered),
    * a list of available cartons is displayed for selection.
    * 
    * @param   id  the full or partial carton identifier
    */
   public void lookup( String id ) {
      final int MAX_CARTON_CNT = 100;

      centerPanel.removeAll();
      cartonId = null;
      printButton.setEnabled( false );

      if (id != null && id.length() > 0) {
         List<String> idList = db.getValueList(
               "SELECT DISTINCT id FROM cartonLog " +
               "WHERE id LIKE '%" + id + "%'" );

         if (idList != null) {
            int cnt = idList.size();

            if (cnt == 0)
               displayMessage( "No matching cartons found" );
            else if (cnt == 1)
               displayCartonInfo( idList.get( 0 ) );
            else if (cnt <= MAX_CARTON_CNT)
               displayCartonList( idList );
            else
               displayMessage( "Too many matching cartons found (" + cnt +
                     "); provide more specific ID");
         }
      }

      // redo the panel layout
      centerPanel.validate();
      centerPanel.repaint();
   }

   /**
    * Displays a message (information, error, etc.) at the top of the
    * center panel.
    * 
    * @param   msg  the message to display
    */
   private void displayMessage( String msg ) {
      JLabel msgLabel = new JLabel( msg, JLabel.CENTER );
      msgLabel.setFont( MSG_FONT );

      JPanel p = new JPanel();
      p.add( msgLabel );

      centerPanel.add( p, BorderLayout.NORTH );
   }

   /**
    * Displays a list of cartons that match the specified substring
    * entered by the user.
    * 
    * @param   idList  a {@code List} of carton identifiers
    */
   private void displayCartonList( List<String> idList ) {
      final int vGap = 3;
      final int horizPadding = 100;

      JPanel p = new JPanel();
      p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );

      for (String id : idList) {
         JButton b = new JButton( id );
         b.setAlignmentX( Component.CENTER_ALIGNMENT );
         b.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               lookup( evt.getActionCommand() );
            }
         } );
         p.add( Box.createVerticalStrut( vGap ) );
         p.add( b );
      }

      p.add( Box.createVerticalStrut( vGap ) );

      centerPanel.add( new JScrollPane( p ), BorderLayout.CENTER );
      centerPanel.add( Box.createHorizontalStrut( horizPadding ),
            BorderLayout.WEST );
      centerPanel.add( Box.createHorizontalStrut( horizPadding ),
            BorderLayout.EAST );

      displayMessage( "Select a carton to display" );
   }

   /**
    * Displays information and processing history for the specified
    * carton.
    * 
    * @param   id  the carton identifier
    */
   private void displayCartonInfo( String id ) {
      cartonId = id;
      printButton.setEnabled( true );

      JPanel p = new JPanel( new BorderLayout(
            SMALL_PADDING, SMALL_PADDING ) );
      p.add( createInfoPanel( id ), BorderLayout.NORTH );
      p.add( createHistTable( id ), BorderLayout.CENTER );

      centerPanel.add( p, BorderLayout.CENTER );
      displayMessage( "Carton information for " + id );
   }

   /**
    * Creates a panel with detailed carton information to be placed above
    * the history table.
    * <p>
    * The default implementation creates and returns a blank panel as a
    * placeholder; subclasses should override this method to provide
    * application-specific carton information.
    * 
    * @param   id  the carton identifier
    * @return  the panel object
    */
   protected JPanel createInfoPanel( String id ) {
      return new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
   }

   /**
    * Creates, configures, and populates the carton history table.
    * 
    * @param   id  the carton identifier
    * @return  a panel containing the table along with a title
    */
   private JPanel createHistTable( String id ) {
      // carton history table
      histTable = new RDSTable( db,
            "Source", "Message", "Date/Time" );
      histTable.setColumnWidths( "Source",     50,  75, 125 );
      histTable.setColumnWidths( "Date/Time", 125, 125, 125 );
      histTable.setColumnAlignment( "Source",    JLabel.CENTER );
      histTable.setColumnAlignment( "Date/Time", JLabel.CENTER );
      histTable.getScrollPane().setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

      // fill table with entries from the carton log
      String query =
            "SELECT code, description, stamp " +
            "FROM cartonLog " +
            "WHERE id = '" + id + "' " +
            "ORDER BY sequence";
      try {
         histTable.populateTable( query );
      } catch (SQLException ex) {
         RDSUtil.alert( "%s: sql error during table population, query = [%s]",
               getName(), query );
         RDSUtil.alert( ex );
      }

      JLabel tableLabel = new JLabel( "Carton History", JLabel.LEFT );
      tableLabel.setFont( MSG_FONT );

      JPanel tablePanel = new JPanel( new BorderLayout() );
      tablePanel.add( tableLabel, BorderLayout.NORTH );
      tablePanel.add( histTable.getScrollPane(), BorderLayout.CENTER );

      return tablePanel;
   }

   /**
    * Prints the contents of the carton history table for the specified
    * carton.
    * 
    * @param   id  the carton identifier
    */
   protected void print( String id ) {
      if (id == null || id.length() == 0)
         return;

      try {
         histTable.print( "Carton Information for " + id, "{0}" );
      } catch (java.awt.print.PrinterException ex) {
         RDSUtil.alert( "%s: error during printing", getName() );
      }
   }

}  // end LookupPanel class
