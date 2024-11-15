/*
 * AdminPanel.java
 * 
 * (c) 2007 Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;
import rdsDashboard.RDSPageNavigator;


/**
 * A dashboard panel for configuring administrative access controls.
 */
public class AdminPanel
      extends RDSDashboardPanel {

   // ui variables
   private JTabbedPane tabs;

   /**
    * Constructs a dashboard panel for configuring administrative access
    * controls.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public AdminPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "Access Control" );
      setDescription( "Configure administrative access controls" );

      createUI();
   }

   /**
    * Creates the user interface for this panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP,
            JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );
      tabs.addTab( "Users",      new UsersSubpanel() );
      tabs.addTab( "Actions",    new ActionsSubpanel() );
      tabs.addTab( "Access Log", new AdminLogSubpanel() );

      add( tabs, BorderLayout.CENTER );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void refreshPanel() {
      if (!isVisible())
         return;

      Component c = tabs.getSelectedComponent();
      if (c instanceof AdminSubpanel)
         ((AdminSubpanel)c).refresh();
   }

   /*
    * ===== admin subpanel (abstract class) =====
    */

   /**
    * An abstract class for subpanels of the admin panel.
    */
   private abstract class AdminSubpanel
         extends JPanel {
      /**
       * Constructs a subpanel for inclusion in the admin panel.
       */
      public AdminSubpanel() {
         super( new BorderLayout( PADDING, PADDING ) );

         // refresh when this panel becomes visible
         addComponentListener( new ComponentAdapter() {
            public void componentShown( ComponentEvent evt ) {
               refresh();
            }
         } );
      }

      /**
       * Updates the panel contents.
       */
      protected abstract void refresh();
   }


   /*
    * ===== users subpanel =====
    */

   /**
    * A subpanel for displaying and editing user information.
    */
   private class UsersSubpanel
         extends AdminSubpanel {
      private static final int GAP = 5;
      protected final Font titleFont = UIManager.getFont(
            "Panel.font" ).deriveFont( Font.BOLD, 14.0f );

      private RDSTable usersTable;
      private JList    allowedList;

      /**
       * Constructs a subpanel for user administration.
       */
      public UsersSubpanel() {
         super();
         setName( "Admin Panel [Users]" );
         createUI();
      }

      /**
       * Creates the user interface for this subpanel.
       */
      private void createUI() {
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );
         add( createTablePanel(), BorderLayout.CENTER );
         add( createListPanel(), BorderLayout.EAST );
      }

      /**
       * Creates a panel that contains the users table and controls.
       */
      private JPanel createTablePanel() {
         JLabel titleLabel = new JLabel( "User Accounts", JLabel.CENTER );
         titleLabel.setFont( titleFont );

         JPanel tablePanel = new JPanel( new BorderLayout(
               SPACING, SPACING ) );
         tablePanel.add( titleLabel, BorderLayout.NORTH );
         tablePanel.add( createUsersTable(), BorderLayout.CENTER );
         tablePanel.add( createControlPanel(), BorderLayout.SOUTH );

         return tablePanel;
      }

      /**
       * Creates and configures the table for displaying user information.
       * 
       * @return  the table's enclosing scroll pane
       */
      private Component createUsersTable() {
         usersTable = new RDSTable( db, "User", "Level", "Expiration" );
         usersTable.setColumnToolTips(
               "User name", "Access level", "Auto-logout time (min)" );

         usersTable.setColumnWidths( "Level",      50, 75, 100 );
         usersTable.setColumnWidths( "Expiration", 50, 75, 100 );

         usersTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         usersTable.setAutoCreateRowSorter( true );

         usersTable.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getButton() == MouseEvent.BUTTON1 &&
                     evt.getClickCount() == 2) {
                  String selectedUser = (String)usersTable.getValueAt(
                        usersTable.rowAtPoint( evt.getPoint() ),
                        "User" );
                  editUser( selectedUser );
               }
            }
         } );

         usersTable.getSelectionModel().addListSelectionListener(
               new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent evt ) {
               if (!evt.getValueIsAdjusting())
                  updateAllowedList();
            }
         } );

         return usersTable.getScrollPane();
      }

      /**
       * Creates a panel for controlling the users table.
       */
      private JPanel createControlPanel() {
         JButton newButton = new JButton( "New" );
         newButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               addUser();
            }
         } );

         JButton editButton = new JButton( "Edit" );
         editButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               editUser( getSelectedUser() );
            }
         } );

         JButton deleteButton = new JButton( "Delete" );
         deleteButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               deleteUser( getSelectedUser() );
            }
         } );

         JPanel controlPanel = new JPanel();
         controlPanel.add( newButton );
         controlPanel.add( editButton );
         controlPanel.add( deleteButton );

         return controlPanel;
      }

      /**
       * Creates a panel that contains the list of allowed actions.
       */
      private JPanel createListPanel() {
         JLabel titleLabel = new JLabel( "Allowed Actions", JLabel.CENTER );
         titleLabel.setFont( titleFont );

         JPanel listPanel = new JPanel( new BorderLayout(
               SPACING, SPACING ) );
         listPanel.add( titleLabel, BorderLayout.NORTH );
         listPanel.add( createAllowedList(), BorderLayout.CENTER );

         return listPanel;
      }

      /**
       * Creates the list for displaying the allowed actions for a
       * selected user.
       * 
       * @return  a scroll pane enclosing the list
       */
      private Component createAllowedList() {

         // customize the list cell renderer
         DefaultListCellRenderer cellRenderer =
               new DefaultListCellRenderer() {
            public Component getListCellRendererComponent( JList list,
                  Object value, int index, boolean isSelected,
                  boolean cellHasFocus ) {
               super.getListCellRendererComponent( list, value, index,
                     false, false );
               setBorder( BorderFactory.createEmptyBorder( 1, 5, 1, 5 ) );
               return this;
            }
         };

         allowedList = new JList( new DefaultListModel() );
         allowedList.setCellRenderer( cellRenderer );
         allowedList.setFixedCellWidth( 200 );

         return new JScrollPane( allowedList );
      }

      /**
       * Updates the contents of the users table.
       */
      private void updateUsersTable() {
         String query =
               "SELECT username, level, expiration FROM admin " +
               "ORDER BY username";
         try {
            usersTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: error populating users table, query = [%s]",
                  getName(), query);
            RDSUtil.alert( ex );
         }
      }

      /**
       * Updates the list of allowed actions for the selected user.
       */
      private void updateAllowedList() {
         // begin by clearing the list
         DefaultListModel listModel =
               (DefaultListModel)allowedList.getModel();
         listModel.clear();

         // determine the selected user
         int row = usersTable.getSelectedRow();
         if (row < 0)
            return;
         String user = (String)usersTable.getValueAt( row, "User" );

         List<String> actionList = db.getValueList(
               "SELECT action FROM admin, adminPermissions " +
               "WHERE admin.username = '" + user + "' " +
         		"AND adminPermissions.level <= admin.level " +
         		"ORDER BY action" );
         if (actionList != null)
            for (String action : actionList)
               listModel.addElement( action );

         allowedList.validate();
      }

      /**
       * Adds a new user, allowing entry of the password, access level, and
       * auto-logout expiration.
       */
      private void addUser() {
         if (!admin.isAuthenticatedInteractive( "manage users",
               AdminPanel.this ))
            return;

         boolean success = showUserDialog( null );

         if (success) {
            admin.log( getName() + ": created new user" );
            refresh();
         }
      }

      /**
       * Edits the selected user, allowing modification of the password,
       * access level, and auto-logout expiration.
       * 
       * @param   user  the username to edit
       */
      private void editUser( String user ) {
         if (user == null || user.length() == 0)
            return;
         if (!admin.isAuthenticatedInteractive( "manage users",
               AdminPanel.this ))
            return;

         boolean success = showUserDialog( user );

         if (success) {
            if (user.equals( admin.getCurrentUser() ))
               JOptionPane.showMessageDialog( this,
                     "Current user modified.  Changes will take effect " +
                           "at the next login",
                     "Message", JOptionPane.INFORMATION_MESSAGE );

            admin.log( getName() + ": modified user [" + user + "]" );
            refresh();
         }
      }

      /**
       * Permanently removes a user.
       * 
       * @param   user  the username to delete
       */
      private void deleteUser( String user ) {
         if (user == null || user.length() == 0)
            return;
         if (!admin.isAuthenticatedInteractive( "manage users",
               AdminPanel.this ))
            return;

         if (user.equals( admin.getCurrentUser() )) {
            JOptionPane.showMessageDialog( this,
                  "The current user cannot be deleted",
                  "Deletion Error",
                  JOptionPane.ERROR_MESSAGE );
            return;
         }

         if (JOptionPane.showConfirmDialog( this,
               "Delete user [" + user + "]?",
               "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE ) != JOptionPane.OK_OPTION)
            return;

         int rows = db.execute(
               "DELETE FROM admin " +
               "WHERE username = '" + user + "'" );

         if (rows > 0) {
            admin.log( getName() + ": deleted user [" + user + "]" );
            refresh();
         }
      }

      /**
       * Returns the user from the currently selected row of the users table.
       */
      private String getSelectedUser() {
         int selectedRow = usersTable.getSelectedRow();
         if (selectedRow < 0)
            return null;
         return (String)usersTable.getValueAt( selectedRow, "User" );
      }

      /**
       * Displays a dialog for creating or editing the access settings for
       * a user.
       * 
       * @param   user  the name of the user, or {@code null} to create a
       *          new user
       * @return  {@code true} if the user was successfully modified or
       *          created, {@code false} otherwise
       */
      private boolean showUserDialog( String user ) {
         final int fieldWidth = 10;
         final Dimension spinnerSize = new Dimension( 40, 20 );

         boolean newUser = (user == null);
         String title = newUser ? "Create New User" : "Edit User";
         Map<String,String> recordMap = db.getRecordMap(
               "SELECT * FROM admin " +
               "WHERE username = '" + user + "'" );

         JTextField usernameField = new JTextField( user, fieldWidth );
         usernameField.setEditable( newUser );

         final String tmpPassword = newUser ? "" : "xxxxxxxx";
         JPasswordField passField = new JPasswordField( tmpPassword,
               fieldWidth );
         JPasswordField passReEntryField = new JPasswordField( tmpPassword,
               fieldWidth );

         JSpinner levelSpinner = new JSpinner();
         levelSpinner.setPreferredSize( spinnerSize );
         levelSpinner.setValue(
               (recordMap == null || recordMap.get( "level" ) == null) ?
               0 : Integer.valueOf( recordMap.get( "level" ) ) );
         JPanel levelSpinnerPanel = new JPanel(
               new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
         levelSpinnerPanel.add( levelSpinner );

         JSpinner expirationSpinner = new JSpinner();
         expirationSpinner.setPreferredSize( spinnerSize );
         expirationSpinner.setValue(
               (recordMap == null || recordMap.get( "expiration" ) == null) ?
               0 : Integer.valueOf( recordMap.get( "expiration" ) ) );
         JPanel expirationSpinnerPanel = new JPanel(
               new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
         expirationSpinnerPanel.add( expirationSpinner );

         JPanel userPanel = new JPanel( new SpringLayout() );
         userPanel.add( new JLabel( "User name: ", JLabel.RIGHT ) );
         userPanel.add( usernameField );
         userPanel.add( new JLabel( "Enter password: ", JLabel.RIGHT ) );
         userPanel.add( passField );
         userPanel.add( new JLabel( "Re-enter password: ", JLabel.RIGHT ) );
         userPanel.add( passReEntryField );
         userPanel.add( new JLabel( "Access level: ", JLabel.RIGHT ) );
         userPanel.add( levelSpinnerPanel );
         userPanel.add( new JLabel( "Auto-logout (min): ", JLabel.RIGHT ) );
         userPanel.add( expirationSpinnerPanel );
         SpringUtilities.makeCompactGrid( userPanel, 5, 2,
               GAP, GAP, GAP, GAP );

         JPanel containerPanel = new JPanel(
               new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
         containerPanel.add( userPanel );

         int returnValue = JOptionPane.showConfirmDialog(
               AdminPanel.this,               // parent component
               containerPanel,                // message object
               title,                         // dialog title
               JOptionPane.OK_CANCEL_OPTION,  // option type
               JOptionPane.PLAIN_MESSAGE      // message type
               );

         if (returnValue != JOptionPane.OK_OPTION)
            return false;

         String errMsg = null;

         // input check
         String newUsername = usernameField.getText().trim();
         String newPass = new String( passField.getPassword() );
         String newReEntryPass = new String( passReEntryField.getPassword() );
         if (newUser && (newUsername == null || newUsername.length() == 0))
            errMsg = "No user name provided";
         else if (newPass == null || newPass.length() == 0)
            errMsg = "Please enter a password";
         else if (newReEntryPass == null || newReEntryPass.length() == 0)
            errMsg = "Please re-enter the password";
         else if (!newPass.equals( newReEntryPass ))
            errMsg = "Supplied passwords do not match";

         if (errMsg != null) {
            JOptionPane.showMessageDialog( this, errMsg, "Input Error",
                  JOptionPane.ERROR_MESSAGE );
            return false;
         }

         String sql;
         if (newUser)
            sql = "INSERT admin SET " +
            		"username = '" + newUsername + "', " +
                  "password = PASSWORD( '" + newPass + "' ), " +
                  "level = " + (Integer)levelSpinner.getValue() + ", " +
                  "expiration = " + (Integer)expirationSpinner.getValue();
         else
            sql = "UPDATE admin SET " +
                  (newPass.equals( tmpPassword ) ? "" :
                        "password = PASSWORD( '" + newPass + "' ), ") +
                  "level = " + (Integer)levelSpinner.getValue() + ", " +
                  "expiration = " + (Integer)expirationSpinner.getValue() +
                        " " +
                  "WHERE username = '" + user + "'";
         int rows = db.execute( sql );

         return (rows > 0);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void refresh() {
         updateUsersTable();
         updateAllowedList();
      }
   }  // end AdminPanel.UsersSubpanel class


   /*
    * ===== actions subpanel =====
    */

   /**
    * A subpanel for displaying and editing action information.
    */
   private class ActionsSubpanel
   extends AdminSubpanel {
      private static final int GAP = 5;
      protected final Font titleFont = UIManager.getFont(
            "Panel.font" ).deriveFont( Font.BOLD, 14.0f );

      private RDSTable actionsTable;
      private JList    allowedList;

      /**
       * Constructs a subpanel for access-level administration.
       */
      public ActionsSubpanel() {
         super();
         setName( "Admin Panel [Actions]" );
         createUI();
      }

      /**
       * Creates the user interface for this subpanel.
       */
      private void createUI() {
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );
         add( createTablePanel(), BorderLayout.CENTER );
         add( createListPanel(), BorderLayout.EAST );
      }

      /**
       * Creates a panel that contains the actions table and controls.
       */
      private JPanel createTablePanel() {
         JLabel titleLabel = new JLabel( "Operator Actions", JLabel.CENTER );
         titleLabel.setFont( titleFont );

         JPanel tablePanel = new JPanel( new BorderLayout(
               SPACING, SPACING ) );
         tablePanel.add( titleLabel, BorderLayout.NORTH );
         tablePanel.add( createActionsTable(), BorderLayout.CENTER );
         tablePanel.add( createControlPanel(), BorderLayout.SOUTH );

         return tablePanel;
      }

      /**
       * Creates and configures the table for displaying action information.
       * 
       * @return  the table's enclosing scroll pane
       */
      private Component createActionsTable() {
         actionsTable = new RDSTable( db, "Action", "Description", "Level" );
         actionsTable.setColumnToolTips(
               "Action name", "Description",
               "Minimum required access level" );

         actionsTable.setColumnWidths( "Action", 75, 150, 200 );
         actionsTable.setColumnWidths( "Level",  25,  50,  75 );

         actionsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         actionsTable.setAutoCreateRowSorter( true );

         actionsTable.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent evt ) {
               if (evt.getButton() == MouseEvent.BUTTON1 &&
                     evt.getClickCount() == 2) {
                  String selectedAction = (String)actionsTable.getValueAt(
                        actionsTable.rowAtPoint( evt.getPoint() ),
                        "Action" );
                  editAction( selectedAction );
               }
            }
         } );

         actionsTable.getSelectionModel().addListSelectionListener(
               new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent evt ) {
               if (!evt.getValueIsAdjusting())
                  updateAllowedList();
            }
         });

         return actionsTable.getScrollPane();
      }

      /**
       * Creates a panel for controlling the actions table.
       */
      private JPanel createControlPanel() {
         JButton newButton = new JButton( "New" );
         newButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               addAction();
            }
         } );

         JButton editButton = new JButton( "Edit" );
         editButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               editAction( getSelectedAction() );
            }
         } );

         JButton deleteButton = new JButton( "Delete" );
         deleteButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               deleteAction( getSelectedAction() );
            }
         } );

         JPanel controlPanel = new JPanel();
         controlPanel.add( newButton );
         controlPanel.add( editButton );
         controlPanel.add( deleteButton );

         return controlPanel;
      }

      /**
       * Creates a panel that contains the list of users allowed to perform
       * the currently selected action.
       */
      private JPanel createListPanel() {
         JLabel titleLabel = new JLabel( "Allowed Users", JLabel.CENTER );
         titleLabel.setFont( titleFont );

         JPanel listPanel = new JPanel( new BorderLayout(
               SPACING, SPACING ) );
         listPanel.add( titleLabel, BorderLayout.NORTH );
         listPanel.add( createAllowedList(), BorderLayout.CENTER );

         return listPanel;
      }

      /**
       * Creates the list for displaying the users allowed to perform the
       * selected action.
       * 
       * @return  a scroll pane enclosing the list
       */
      private Component createAllowedList() {

         // customize the list cell renderer
         DefaultListCellRenderer cellRenderer =
               new DefaultListCellRenderer() {
            public Component getListCellRendererComponent( JList list,
                  Object value, int index, boolean isSelected,
                  boolean cellHasFocus ) {
               super.getListCellRendererComponent( list, value, index,
                     false, false );
               setBorder( BorderFactory.createEmptyBorder( 1, 5, 1, 5 ) );
               return this;
            }
         };

         allowedList = new JList( new DefaultListModel() );
         allowedList.setCellRenderer( cellRenderer );
         allowedList.setFixedCellWidth( 125 );

         return new JScrollPane( allowedList );
      }

      /**
       * Updates the contents of the actions table.
       */
      private void updateActionsTable() {
         String query =
               "SELECT action, description, level FROM adminPermissions " +
               "ORDER BY action";
         try {
            actionsTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: error populating actions table, query = [%s]",
                  getName(), query );
            RDSUtil.alert( ex );
         }
      }

      /**
       * Updates the list of users allowed to perform the selected action.
       */
      private void updateAllowedList() {
         // begin by clearing the list
         DefaultListModel listModel =
               (DefaultListModel)allowedList.getModel();
         listModel.clear();

         // determine the selected action
         int row = actionsTable.getSelectedRow();
         if (row < 0)
            return;
         String action = (String)actionsTable.getValueAt( row, "Action" );

         List<String> userList = db.getValueList(
               "SELECT username FROM admin, adminPermissions " +
               "WHERE adminPermissions.action = '" + action + "' " +
               "AND admin.level >= adminPermissions.level " +
               "ORDER BY username" );
         if (userList != null)
            for (String user : userList)
               listModel.addElement( user );

         allowedList.validate();
      }

      /**
       * Adds a new action, allowing entry of the description and access
       * level.
       */
      private void addAction() {
         if (!admin.isAuthenticatedInteractive( "manage actions",
               AdminPanel.this ))
            return;

         boolean success = showActionDialog( null );

         if (success) {
            admin.log( getName() + ": created new action" );
            refresh();
         }
      }

      /**
       * Edits the selected action, allowing modification of the description
       * and access level.
       * 
       * @param   action  the action to edit
       */
      private void editAction( String action ) {
         if (action == null || action.length() == 0)
            return;
         if (!admin.isAuthenticatedInteractive( "manage actions",
               AdminPanel.this ))
            return;

         boolean success = showActionDialog( action );

         if (success) {
            admin.log( getName() + ": modified action [" + action + "]" );
            refresh();
         }
      }

      /**
       * Permanently removes an action.
       * 
       * @param   action  the action to delete
       */
      private void deleteAction( String action ) {
         if (action == null || action.length() == 0)
            return;
         if (!admin.isAuthenticatedInteractive( "manage actions",
               AdminPanel.this ))
            return;

         if (JOptionPane.showConfirmDialog( this,
               "Delete action [" + action + "]?",
               "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE ) != JOptionPane.OK_OPTION)
            return;

         int rows = db.execute(
               "DELETE FROM adminPermissions " +
               "WHERE action = '" + action + "'" );

         if (rows > 0) {
            admin.log( getName() + ": deleted action [" + action + "]" );
            refresh();
         }
      }

      /**
       * Returns the action from the currently selected row of the actions
       * table.
       */
      private String getSelectedAction() {
         int selectedRow = actionsTable.getSelectedRow();
         if (selectedRow < 0)
            return null;
         return (String)actionsTable.getValueAt( selectedRow, "Action" );
      }

      /**
       * Displays a dialog for creating or editing the access settings for
       * an action.
       * 
       * @param   action  the name of the action, or {@code null} to create
       *          a new action
       * @return  {@code true} if the action was successfully modified or
       *          created, {@code false} otherwise
       */
      private boolean showActionDialog( String action ) {
         final int fieldWidth = 30;
         final Dimension spinnerSize = new Dimension( 40, 20 );

         boolean newAction = (action == null);
         String title = newAction ? "Create New Action" : "Edit Action";
         Map<String,String> recordMap = db.getRecordMap(
               "SELECT * FROM adminPermissions " +
               "WHERE action = '" + action + "'" );

         JTextField actionField = new JTextField( action, fieldWidth );
         actionField.setEditable( newAction );

         JTextField descriptionField = new JTextField(
               (recordMap == null) ? "" : recordMap.get( "description" ),
               fieldWidth );

         JSpinner levelSpinner = new JSpinner();
         levelSpinner.setPreferredSize( spinnerSize );
         levelSpinner.setValue(
               (recordMap == null || recordMap.get( "level" ) == null) ?
               0 : Integer.valueOf( recordMap.get( "level" ) ) );
         JPanel levelSpinnerPanel = new JPanel(
               new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
         levelSpinnerPanel.add( levelSpinner );

         JPanel userPanel = new JPanel( new SpringLayout() );
         userPanel.add( new JLabel( "Action name: ", JLabel.RIGHT ) );
         userPanel.add( actionField );
         userPanel.add( new JLabel( "Description: ", JLabel.RIGHT ) );
         userPanel.add( descriptionField );
         userPanel.add( new JLabel( "Access level: ", JLabel.RIGHT ) );
         userPanel.add( levelSpinnerPanel );
         SpringUtilities.makeCompactGrid( userPanel, 3, 2,
               GAP, GAP, GAP, GAP );

         JPanel containerPanel = new JPanel(
               new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
         containerPanel.add( userPanel );

         int returnValue = JOptionPane.showConfirmDialog(
               AdminPanel.this,               // parent component
               containerPanel,                // message object
               title,                         // dialog title
               JOptionPane.OK_CANCEL_OPTION,  // option type
               JOptionPane.PLAIN_MESSAGE      // message type
               );

         if (returnValue != JOptionPane.OK_OPTION)
            return false;

         String errMsg = null;

         // input check
         String newActionName = actionField.getText().trim();
         if (newAction &&
                  (newActionName == null || newActionName.length() == 0))
            errMsg = "No action name provided";

         if (errMsg != null) {
            JOptionPane.showMessageDialog( this, errMsg, "Input Error",
                  JOptionPane.ERROR_MESSAGE );
            return false;
         }

         String sql;
         if (newAction)
            sql = "INSERT adminPermissions SET " +
                  "action = '" + newActionName + "', " +
                  "description = '" + descriptionField.getText() + "', " +
                  "level = " + (Integer)levelSpinner.getValue();
         else
            sql = "UPDATE adminPermissions SET " +
                  "description = '" + descriptionField.getText() + "', " +
                  "level = " + (Integer)levelSpinner.getValue() + " " +
                  "WHERE action = '" + action + "'";
         int rows = db.execute( sql );

         return (rows > 0);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void refresh() {
         updateActionsTable();
         updateAllowedList();
      }
   }  // end AdminPanel.ActionsSubpanel class


   /*
    * ===== access log subpanel =====
    */

   /**
    * A subpanel for displaying the contents of the administrative access
    * log table.
    */
   private class AdminLogSubpanel
         extends AdminSubpanel
         implements RDSPageNavigator.PageNavigable {
      private static final String NO_USER_FILTER = "<all users>";

      // ui variables
      private RDSTable  logTable;
      private RDSPageNavigator navigator;
      private TableRowSorter<DefaultTableModel> sorter;
      private JComboBox userCombo;

      /**
       * Constructs a subpanel for viewing the access log.
       */
      public AdminLogSubpanel() {
         super();
         setName( "Admin Panel [AccessLog]" );
         createUI();
      }

      /**
       * Creates the user interface for this subpanel.
       */
      private void createUI() {
         setBorder( BorderFactory.createEmptyBorder(
               PADDING, PADDING, PADDING, PADDING ) );
         add( createControlPanel(), BorderLayout.NORTH );
         add( createLogTable(), BorderLayout.CENTER );
         add( createNavPanel(), BorderLayout.SOUTH );
      }

      /**
       * Creates a panel that contains controls for the access log table.
       */
      private JPanel createControlPanel() {
         userCombo = new JComboBox();
         userCombo.addItem( NO_USER_FILTER );
         userCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               updateNavigator( true );
            }
         } );

         JPanel controlPanel = new JPanel();
         controlPanel.add( new JLabel( "View activity for ", JLabel.RIGHT ) );
         controlPanel.add( userCombo );

         return controlPanel;
      }

      /**
       * Creates and configures the table for display of the access log
       * entries.
       * 
       * @return  the table's enclosing scroll pane
       */
      private Component createLogTable() {
         logTable = new RDSTable( db, "User", "Description", "Date/Time" );
         logTable.setColumnWidths( "User",       75, 100, 150 );
         logTable.setColumnWidths( "Date/Time", 100, 125, 150 );

         // create a row sorter for sorting and filtering
         sorter = new TableRowSorter<DefaultTableModel>(
               (DefaultTableModel)logTable.getModel() );
         logTable.setRowSorter( sorter );

         return logTable.getScrollPane();
      }

      /**
       * Creates a panel to hold the controls for the page navigator.
       */
      private JPanel createNavPanel() {
         JPanel navPanel = new JPanel();
         navPanel.setLayout( new BoxLayout( navPanel, BoxLayout.X_AXIS ) );

         navigator = new RDSPageNavigator( this );
         navPanel.add( navigator.getNavigationPanel() );

         return navPanel;
      }

      /**
       * Updates the list of users in the control panel combo box.  A list
       * of all users in the log table is obtained from the database, and
       * the combo box list is modified to contain the same users.  This
       * implementation attempts to behave reasonably even if an operator
       * is currently in the process of making a selection from the combo
       * box popup menu.
       */
      private void updateUserCombo() {
         List<String> userList = db.getValueList(
               "SELECT DISTINCT user FROM adminLog " +
               "ORDER BY user" );
         if (userList == null) {
            // clear the combo box list
            userCombo.removeAllItems();
            userCombo.addItem( NO_USER_FILTER );
            return;
         }

         // revert to no filter if previous selected user disappears from list
         String selectedUser = (String)userCombo.getSelectedItem();
         if (selectedUser != NO_USER_FILTER &&
               !userList.contains( selectedUser ))
            userCombo.setSelectedItem( NO_USER_FILTER );

         // sync combo box list to user list
         int index = 1;  // start after the no-user-filter item
         for (String user : userList) {
            if (index >= userCombo.getItemCount())
               userCombo.addItem( user );
            else {
               int comparison = user.compareTo(
                     (String)userCombo.getItemAt( index ) );
               if (comparison < 0)
                  userCombo.insertItemAt( user, index );
               else {
                  while (comparison > 0) {
                     userCombo.removeItemAt( index );
                     comparison = user.compareTo(
                           (String)userCombo.getItemAt( index ) );
                  }
               }
            }
            index++;
         }
         while (index < userCombo.getItemCount())
            userCombo.removeItemAt( index );
      }

      /**
       * Updates the total number of records for the log table's page
       * navigator, triggering a table update.
       * 
       * @param   resetNavigation  if {@code true}, the page navigator
       *          is reset to the first page of results; if {@code false},
       *          the total number of records is updated, but the
       *          navigator remains at the current page number
       */
      private void updateNavigator( boolean resetNavigation ) {
         String where;
         String selectedUser = (String)userCombo.getSelectedItem();
         if (selectedUser == null || selectedUser == NO_USER_FILTER)
            where = "";
         else
            where = "WHERE user = '" + selectedUser + "' ";

         navigator.setTotalResults( Integer.valueOf( db.getValue(
               "SELECT COUNT(*) FROM adminLog " + where,
               "0" ) ) );

         if (resetNavigation)
            navigator.goToFirst();
      }

      /**
       * Updates the contents of the access log table.
       */
      private void updateLogTable() {
         String select = "SELECT user, description, stamp FROM adminLog ";
         String where;
         String selectedUser = (String)userCombo.getSelectedItem();
         if (selectedUser == null || selectedUser == NO_USER_FILTER)
            where = "";
         else
            where = "WHERE user = '" + selectedUser + "' ";
         String order = "ORDER BY stamp DESC";

         String query = navigator.getNavigationQuery(
               select + where + order );
         try {
            logTable.populateTable( query );
         } catch (SQLException ex) {
            RDSUtil.alert( "%s: error populating log table, query = [%s]",
            		getName(), query );
            RDSUtil.alert( ex );
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void refresh() {
         updateUserCombo();
         updateNavigator( false );
      }

      @Override
      public void pageNavigationUpdated() {
         updateLogTable();
      }
   }  // end AdminPanel.AdminLogSubpanel class

}  // end AdminPanel class
