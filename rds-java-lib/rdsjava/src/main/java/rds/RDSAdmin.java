/*
 * RDSAdmin.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rds;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.*;

/**
 * A utility class that provides management of user logins and access
 * control authentication.
 */
public class RDSAdmin {
   // constants
   private static final int DEFAULT_EXPIRATION = 5;  // expiration (min)
   private static final int GAP = 2;                 // gap btwn ui elements
   private static final int MAX_LEN = 255;           // max text length

   // class variables
   private RDSDatabase db;
   private String username;
   private int level;
   private Timer logoutTimer;

   // ui variables
   private Component parentComponent;
   private JPanel loginPanel;
   private JLabel userLabel;
   private JButton loginButton, changeButton;

   /*
    * --- constructor and initialization methods ---
    */

   /**
    * Constructs an instance of the admin class for access control.
    */
   public RDSAdmin( RDSDatabase db ) {
      this.db = db;

      init();
   }

   /**
    * Initalizes class variables and creates a timer for auto-logout.
    */
   private void init() {
      username = null;
      level = -1;

      logoutTimer = new Timer( DEFAULT_EXPIRATION * 60 * 1000,
            new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            logout();
         }
      } );
      logoutTimer.setRepeats( false );
   }

   public void setParentComponent( Component parentComponent ) {
      this.parentComponent = parentComponent;
   }

   /*
    * --- access methods ---
    */

   /**
    * Returns whether or not there is a valid user currently logged in.
    * 
    * @return  {@code true} if a user is logged in, {@code false} otherwise
    */
   public boolean isLoggedIn() {
      return (level >= 0 && username != null && !username.isEmpty());
   }

   /**
    * Returns the name of the currently logged-in user or {@code null} if
    * there is no current user.
    * 
    * @return  a {@code Sting} containing the name of the current user or
    *          {@code null} if there is none
    */
   public String getCurrentUser() {
      return username;
   }

   /**
    * Gets the access level of the currently logged-in user.  This value
    * will be less than zero if there is no current user.
    * 
    * @return  the access level of the current user
    */
   public int getCurrentLevel() {
      return level;
   }

   /**
    * Sets the expiration time, i.e. the duration following login or
    * an authenticated action after which the current user will be
    * automatically logged out.  An input value less than zero will
    * result in a login that will never automatically expire, thus
    * requiring a manual logout.
    * 
    * @param   expiration  the new expiration interval (minutes)
    */
   public void setExpiration( int expiration ) {
      if (expiration >= 0)
         logoutTimer.setInitialDelay( expiration * 60 * 1000 );
      else
         logoutTimer.setInitialDelay( Integer.MAX_VALUE );
   }


   /*
    * --- login/logout methods ---
    */

   /**
    * Attempts to log a user in by comparing the given password to an
    * access control table.  If the lookup is successful, the associated
    * access level is assigned to the user, and the logout timer is
    * started with the obtained expiration.
    * 
    * @param   username  the username
    * @param   password  the password
    * @return  {@code true} if the user has been successfully logged in,
    *          {@code false} otherwise
    */
   public boolean login( String username, String password ) {
      // check inputs
      if (db == null ||
            username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty())
         return false;

      username = username.trim();
      password = password.trim();

      Map<String,String> userMap = db.getRecordMap(
            "SELECT level, expiration FROM admin " +
            "WHERE username = '" + username + "' " +
            "AND password = PASSWORD( '" + password + "' )" );
      if (userMap == null || userMap.isEmpty())
         return false;

      int loginLevel = RDSUtil.stringToInt( userMap.get( "level" ), -1 );
      if (loginLevel < 0)
         return false;

      this.username = username;
      this.level = loginLevel;
      setExpiration( Integer.valueOf( userMap.get( "expiration" ) ) );
      logoutTimer.restart();
      log( "logged in at level [" + level + "]" );
      updateLoginPanel();

      return true;
   }

   /**
    * Logs out the current user and clears all authentication information.
    */
   public void logout() {
      if (isLoggedIn()) {
         log( "logged out" );
         username = null;
         level = -1;
      }
      logoutTimer.stop();
      updateLoginPanel();
   }

   /**
    * Displays a graphical login dialog.  If the login fails, an alert
    * dialog is shown.
    * 
    * @param   parentComponent  the parent component on which to center
    *                           the login dialog
    * @return  {@code true} if a valid username and password are
    *          entered, {@code false} otherwise
    */
   public boolean graphicalLogin( Component parentComponent ) {
      final int GAP = 5;

      // create custom login panel
      JPanel loginPanel = new JPanel( new SpringLayout() );
      loginPanel.add( new JLabel( "Username:", JLabel.RIGHT ) );
      final JTextField usernameField = new JTextField();
      loginPanel.add( usernameField );
      loginPanel.add( new JLabel( "Password:", JLabel.RIGHT ) );
      JPasswordField passwordField = new JPasswordField();
      loginPanel.add( passwordField );
      SpringUtilities.makeCompactGrid( loginPanel, 2, 2, 0, 0, GAP, GAP );

      // create the dialog
      final JOptionPane optionPane = new JOptionPane(
            loginPanel,                   // message object
            JOptionPane.PLAIN_MESSAGE,    // message type
            JOptionPane.OK_CANCEL_OPTION  // option type
            );
      JDialog dialog = optionPane.createDialog(
            parentComponent,           // parent component
            "Authentication Required"  // title text
            );

      // make username field get the focus when dialog is initially displayed
      dialog.addWindowFocusListener( new WindowAdapter() {
         public void windowGainedFocus( WindowEvent evt ) {
            usernameField.requestFocusInWindow();
         }
      } );

      // display the dialog
      dialog.setVisible( true );
      dialog.dispose();

      // if dialog was canceled, exit
      Object selectedValue = optionPane.getValue();
      if (selectedValue == null ||
            (Integer)selectedValue != JOptionPane.OK_OPTION)
         return false;

      // get username/password from dialog and attempt the login
      String username = usernameField.getText().trim();
      String password = new String( passwordField.getPassword() ).trim();
      boolean loginSuccess = login( username, password );

      // display login status notification
      if (!loginSuccess) {
         JOptionPane.showMessageDialog(
               parentComponent,
               "Login failed -- invalid username and/or password.",
               "Login Failed",
               JOptionPane.ERROR_MESSAGE );
      }

      return loginSuccess;
   }

   /**
    * Determines whether the current user is authenticated (at the default
    * level).
    * 
    * @return  {@code true} if the current user is authorized to peform
    *          default actions, {@code false} otherwise
    */
   public boolean isAuthenticated() {
      return isAuthenticated( "default action" );
   }

   /**
    * Determines whether the current user is authenticated at the level
    * required to perform {@code action}, resetting the expiration timer
    * on success.  If there is no level set for the specified action, the
    * level of the default action is used.
    * 
    * @param   action  the name of the action
    * @return  {@code true} if the current user is authorized to peform the
    *          action, {@code false} otherwise
    */
   public boolean isAuthenticated( String action ) {
      return isAuthenticated( action, true );
   }

   /**
    * Determines whether the current user is authenticated at the level
    * required to perform {@code action}. If there is no level set for the
    * specified action, the level of the default action is used. If the
    * {@code update} parameter is {@code true} (the default), the expiration
    * timer will be reset on a successful authentication.
    * 
    * @param   action the name of the action
    * @param   update whether or not a successful authentication should reset
    *          the expiration timer
    * @return  {@code true} if the current user is authorized to peform the
    *          action, {@code false} otherwise
    */
   public boolean isAuthenticated( String action, boolean update ) {
      // check status and input
      if (action == null || action.trim().isEmpty())
         return false;

      return isAuthenticated( getActionLevel( action ), update );
   }

   /**
    * Determines whether the current user is authenticated at the specified
    * level, resetting the expiration timer on success.  Note that an action
    * level of zero requires no authentication (not even a logged-in user),
    * while an action level less than zero will always fail.
    * 
    * @param   actionLevel  the level required for authentication
    * @return  {@code true} if the current user is authorized at the
    *          required level, {@code false} otherwise
    */
   public boolean isAuthenticated( int actionLevel ) {
      return isAuthenticated( actionLevel, true );
   }

   /**
    * Determines whether the current user is authenticated at the
    * specified level.  Note that an action level of zero requires no
    * authentication (not even a logged-in user), while an action
    * level less than zero will always fail.  If the {@code update}
    * parameter is {@code true} (the default), the expiration timer
    * will be reset on a successful authentication.
    * 
    * @param   actionLevel  the level required for authentication
    * @param   update       whether or not a successful authentication
    *          should reset the expiration timer
    * @return  {@code true} if the current user is authorized at the
    *          required level, {@code false} otherwise
    */
   public boolean isAuthenticated( int actionLevel, boolean update ) {
      // some actions require no authentication
      if (actionLevel == 0)
         return true;

      // check status and input
      if (!isLoggedIn() || actionLevel < 0)
         return false;

      // check the current user's level
      if (level < actionLevel)
         return false;

      // successful authentication resets the expiration timer
      if (update)
         logoutTimer.restart();

      return true;
   }


   /*
    * --- ui methods ---
    */

   /**
    * Performs interactive authentication to determine authorization at
    * the level required to perform {@code action}. If there is no level
    * set for the specified action, the level of the default action is used.
    * If there is no current user, or if the current user is not authorized
    * to perform the action, a graphical dialog is displayed to allow for
    * login at the appropriate level.
    * <p>
    * This method returns silently if the current user is sufficiently
    * authenticated.  As with the default versions of other authentication
    * methods, successful authentication resets the expiration timer.
    * 
    * @param   action            the name of the action
    * @param   parentComponent   the parent component on which to center the
    *          login dialog
    * @return  {@code true} if the current user is authorized to peform
    *          the action, {@code false} otherwise
    */
   public boolean isAuthenticatedInteractive( String action,
         Component parentComponent ) {
      // check input
      if (action == null || action.trim().isEmpty())
         return false;

      // get level associated with action
      int actionLevel = getActionLevel( action );

      // check if user is currently authenticated
      if (isAuthenticated( actionLevel ))
         return true;

      // failed authentication -- allow login to (re-)establish access level
      if (graphicalLogin( parentComponent )) {
         if (isAuthenticated( actionLevel ))
            return true;

         // on successful login but failed authentication, show alert
         JOptionPane.showMessageDialog(
               parentComponent,
               "Authentication failed -- insufficient privileges to " +
                     "perform this action.",
               "Authentication Failed",
               JOptionPane.ERROR_MESSAGE );
      }

      return false;
   }


   /**
    * Creates a panel that contains login information and controls.
    */
   private void createLoginPanel() {
      userLabel = new JLabel( "", JLabel.CENTER );
      userLabel.setFont( userLabel.getFont().deriveFont( Font.BOLD ) );
      userLabel.setAlignmentX( Component.CENTER_ALIGNMENT );

      loginButton = new JButton( "Login" );
      loginButton.setAlignmentX( Component.CENTER_ALIGNMENT );
      loginButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            if (isLoggedIn())
               logout();
            else
               graphicalLogin( parentComponent );
         }
      } );

      changeButton = new JButton( "Change User" );
      changeButton.setAlignmentX( Component.CENTER_ALIGNMENT );
      changeButton.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            graphicalLogin( parentComponent );
         }
      } );
      changeButton.setEnabled( false );

      // make the two buttons the same width
      loginButton.setMaximumSize( changeButton.getMaximumSize() );

      loginPanel = new JPanel();
      loginPanel.setLayout( new BoxLayout( loginPanel, BoxLayout.Y_AXIS ) );
      loginPanel.add( userLabel );
      loginPanel.add( Box.createVerticalStrut( GAP ) );
      loginPanel.add( loginButton );
      loginPanel.add( Box.createVerticalStrut( GAP ) );
      loginPanel.add( changeButton );

      return;
   }

   /**
    * Gets the panel that contains login information and controls.  If the
    * panel does not exist, it is created.
    * 
    * @return  the login panel object
    */
   public JPanel getLoginPanel() {
      if (loginPanel == null)
         createLoginPanel();

      return loginPanel;
   }

   /**
    * Updates the text and ui elements in the login panel.
    */
   public void updateLoginPanel() {
      if (loginPanel == null)
         return;

      if (isLoggedIn()) {
         userLabel.setText( "<html><center>Current User:<br>" +
         		username + "</center></html>");
         userLabel.setVisible( true );
         loginButton.setText( "Logout" );
         changeButton.setEnabled( true );
      } else {
         userLabel.setVisible( false );
         loginButton.setText( "Login" );
         changeButton.setEnabled( false );
      }
   }


   /*
    * --- additional utility methods ---
    */

   /**
    * Enters a message for this user into the {@code adminLog} table.
    * 
    * @param   description  the textual description to enter into the log
    */
   public void log( String description ) {
      if (db == null ||
            description == null || description.isEmpty() ||
            username == null    || username.isEmpty())
         return;

      if (description.length() > MAX_LEN)
         description = description.substring( 0, MAX_LEN );
      db.execute(
            "INSERT INTO adminLog SET " +
            "user = '%s', " +
            "description = '%s'",
            username, description );
   }

   /**
    * Determine the action level associated with a string.  The database
    * is queried for the specified action; if it is not present, the
    * default level is returned.  For a missing default or other database
    * error, a value less than zero is returned, which should result in
    * a failed authentication.
    * 
    * @param   action  the string describing the action
    * @return  the associated action level, an integer
    */
   private int getActionLevel( String action ) {
      int actionLevel = -1;

      if (db == null)
         return actionLevel;

      actionLevel = db.getIntValue(
            "SELECT level FROM adminPermissions " +
            "WHERE action = '" + action + "'",
            -1 );
      if (actionLevel < 0)  // action not specified... use default
         actionLevel = db.getIntValue(
               "SELECT level FROM adminPermissions " +
               "WHERE action = 'default action'",
               -1 );

      return actionLevel;
   }

}  // end RDSAdmin class
