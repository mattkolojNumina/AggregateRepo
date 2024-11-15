/*
 * RDSDashboard.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

import rds.*;


/**
 * {@code RDSDashboard} provides a generalized framework for displaying
 * content panels, status elements, messages, and various widgets. This
 * class extends {@code JPanel} and is an appropriate choice for the
 * content pane of an applet or application.  Configuration is performed
 * via a database table named 'dashboard'.
 */
public class RDSDashboard
      extends JPanel {

   /*
    * --- constants ---
    */

   /** The total width of the dashboard */
   public static final int WIDTH  = 800;

   /** The total height of the dashboard */
   public static final int HEIGHT = 600;

   // internal-use layout/ui constants
   private static final int BORDER = 5;             // empty border padding
   private static final int GAP    = 5;             // padding btwn elements
   private static final int TITLE_FONT_SIZE = 24;   // for dashboard title
   private static final int STATUS_FONT_SIZE = 14;  // for status elements
   private static final int LABEL_HEIGHT = 20;      // for labels & buttons
   private static final int REFRESH_DELAY = 5000;   // timer delay (msec)
   private static final double LOGO_SCALE_FACTOR = 0.75;


   /*
    * --- class variables ---
    */

   // class variables, typically shared among all dashboard panels
   private RDSDatabase db;
   private RDSAdmin admin;
   private URL baseURL;

   // UI variables
   private int headerHeight, sidePanelWidth;
   private JPanel statusPanel;
   private RDSTable messageTable;

   // other class variables
   private List<RDSDashboardPanel> panels;
   private List<AbstractButton> buttons;
   private Timer refreshTimer;
   private Map<String,String> params;


   /*
    * --- constructor and initialization methods ---
    */

   /**
    * Creates the dashboard user interface, incoporating widgets and content
    * panels, the references to which are obtained from a database table.
    * 
    * @param   parentContainer  the parent container of the dashboard,
    *          typically an applet or application frame
    */
   public RDSDashboard( Container parentContainer ) {
      setName( "RDS Dashboard" );

      setClassVariables( parentContainer );
      setDashboardParameters();

      createUI();
   }

   /**
    * Determines the values of some important class variables.  These
    * variables are made available to the content panels and widgets that
    * are added to the dashboard.
    * 
    * @param   parentContainer  the parent container of the dashboard,
    *          typically an applet or application frame
    */
   private void setClassVariables( Container parentContainer ) {
      String hostName = null;

      if (parentContainer instanceof Applet) {
         Applet parentApplet = (Applet)parentContainer;
         baseURL  = parentApplet.getCodeBase();
         hostName = parentApplet.getParameter( "host" );
         if (hostName == null)
            hostName = baseURL.getHost();
      } else if (parentContainer instanceof RDSDashboardApp) {
         String[] args = ((RDSDashboardApp)parentContainer).getArgs();
         hostName = (args != null && args.length > 0) ? args[0] : null;

         // determine base url from system properties
         try {
            baseURL = new URL( "file:" +
                  System.getProperty( "user.dir" ) + File.separator );
         } catch (java.net.MalformedURLException ex) {
            RDSUtil.alert( "%s: error obtaining working directory",
                  getName() );
            RDSUtil.alert( ex );
            baseURL = null;
         }
      }

      // connect to database (defaults to localhost if no host specified)
      db = new RDSDatabase( hostName );

      // configure admin
      admin = new RDSAdmin( db );

      // create lists for content panels and their related objects
      panels = new ArrayList<RDSDashboardPanel>();
      buttons = new ArrayList<AbstractButton>();
   }

   /**
    * Sets the parameters (key/value pairs) for the dashboard, as specified
    * in the 'dashboard' database table.
    */
   private void setDashboardParameters() {
      RDSUtil.trace( getName() + ": obtaining configuration parameters" );
      String paramString = db.getValue(
            "SELECT params FROM dashboard " +
            "WHERE zone = 'dashboard' " +
            "AND object = 'dashboard'",
            null );

      if (paramString == null || paramString.isEmpty())
         return;

      for (String param : paramString.split( "," )) {
         String[] keyVal = param.split( "=", 2 );
         if (keyVal.length == 2)
            setParam( keyVal[0], keyVal[1] );
      }
   }


   /*
    * --- UI and layout methods ---
    */

   /**
    * Creates the user interface for the dashboard.
    */
   private void createUI() {
      // set basic ui prefs
      setPreferredSize( new Dimension( WIDTH, HEIGHT ) );
      setLayout( new BorderLayout( GAP, GAP ) );
      setBorder( BorderFactory.createCompoundBorder( new EtchedBorder(),
            new EmptyBorder( BORDER, BORDER, BORDER, BORDER ) ) );

      Insets insets = getInsets();
      headerHeight = HEIGHT - RDSDashboardPanel.HEIGHT -
            insets.top - insets.bottom - GAP;
      sidePanelWidth = WIDTH - RDSDashboardPanel.WIDTH -
            insets.left - insets.right - GAP;

      add( createHeaderPanel(), BorderLayout.NORTH );
      add( createContentPanel(), BorderLayout.CENTER );
      add( createSidePanel(), BorderLayout.WEST );
   }

   /**
    * Creates the dashboard header panel.  The header contains the title
    * or company logo, status elements, a message table, and a slot to
    * which widgets may be added (based on entries in a database table).
    * 
    * @return  the panel object
    */
   private JPanel createHeaderPanel() {
      RDSUtil.trace( getName() + ": adding header content" );
      JPanel headerPanel = new JPanel();
      headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.X_AXIS ) );
      headerPanel.setPreferredSize( new Dimension( 0, headerHeight ) );

      headerPanel.add( createTitleStatusPanel() );

      JPanel messageTablePanel = createMessageTablePanel();
      if (messageTablePanel != null) {
         headerPanel.add( Box.createHorizontalGlue() );
         headerPanel.add( messageTablePanel );
      }

      JPanel widgetPanel = createWidgetPanel();
      if (widgetPanel != null) {
         headerPanel.add( Box.createHorizontalGlue() );
         headerPanel.add( widgetPanel );
      }

      return headerPanel;
   }

   /**
    * Creates the panel that contains the dashboard title/logo and any
    * status elements that are subsequently added.
    * 
    * @return the panel object
    */
   private JPanel createTitleStatusPanel() {
      // obtain dashboard title from parameters, otherwise show logo
      JLabel titleLabel = new JLabel( "", JLabel.LEFT );
      String title = getParam( "title" );
      if (title == null || title.isEmpty()) {
         ImageIcon ngLogoIcon = RDSUtil.createScaledImageIcon(
               this.getClass(), "images/numinagroup_logo.png",
               LOGO_SCALE_FACTOR );
         if (ngLogoIcon != null)
            titleLabel.setIcon( ngLogoIcon );
      } else {
         titleLabel.setText( title );
         titleLabel.setFont( titleLabel.getFont().deriveFont(
               (float)TITLE_FONT_SIZE ) );
      }

      // create the panel that will hold status elements as they are added
      statusPanel = new JPanel();
      statusPanel.setLayout( new BoxLayout( statusPanel, BoxLayout.Y_AXIS) );

      JPanel titleStatusPanel = new JPanel();
      titleStatusPanel.setLayout( new BoxLayout( titleStatusPanel,
            BoxLayout.Y_AXIS ) );
      titleStatusPanel.add( titleLabel );
      titleStatusPanel.add( Box.createVerticalGlue() );
      titleStatusPanel.add( statusPanel );

      return titleStatusPanel;
   }

   /**
    * Creates the message table, to be inserted into the header panel.
    */
   private JPanel createMessageTablePanel() {
      final Dimension messageTableSize = new Dimension( 250, headerHeight );

      // make sure the 'messages' table exists
      String test = db.getValue( "SHOW TABLES LIKE 'messages'", "" );
      if (!test.equalsIgnoreCase( "messages" ))
         return null;

      messageTable = new RDSTable( db, "Message", "Time" );
      messageTable.setColumnWidths( "Time", 75, 75, 75 );
      messageTable.getScrollPane().setBorder(
            BorderFactory.createEmptyBorder() );

      JPanel messageTablePanel = new JPanel( new BorderLayout() );
      messageTablePanel.setBorder( BorderFactory.createEtchedBorder() );
      messageTablePanel.setPreferredSize( messageTableSize );
      messageTablePanel.setMaximumSize( messageTableSize );
      messageTablePanel.add( messageTable.getScrollPane() );

      return messageTablePanel;
   }

   /**
    * Creates the header panel that holds the "widgets" -- small panels
    * of dynamic content.  The actual widget objects are specified in
    * the 'dashboard' database table and loaded at run-time.
    * 
    * @return   the panel object
    */
   private JPanel createWidgetPanel() {
      final CardLayout widgetLayout = new CardLayout();
      final JPanel widgetPanel = new JPanel( widgetLayout );

      List<String> widgetList = db.getValueList(
            "SELECT object FROM dashboard " +
            "WHERE zone = 'widget' " +
            "ORDER BY id" );
      if (widgetList == null || widgetList.isEmpty())
         return null;

      for (String widgetName : widgetList) {
         RDSWidget widget = createWidget( widgetName );
         if (widget == null)
            continue;

         widgetPanel.add( widget, widget.getName() );
         RDSUtil.inform( "%s: added widget [%s]", getName(), widgetName );
      }

      widgetPanel.setMaximumSize( getWidgetSize() );
      widgetPanel.addMouseListener( new MouseAdapter() {
         @Override
         public void mouseClicked( MouseEvent evt ) {
            if (evt.getButton() == MouseEvent.BUTTON1)
               widgetLayout.next( widgetPanel );
            else if (evt.getButton() == MouseEvent.BUTTON3)
               widgetLayout.previous( widgetPanel );
         }
      } );

      return widgetPanel;
   }

   /**
    * Creates an instance of an {@code RDSWidget}, specified by name.
    * 
    * @param   widgetName  the fully qualified class name of the widget
    *          to be constructed
    * @return  the widget object, or {@code null} if the widget could not
    *          be constructed
    */
   private RDSWidget createWidget( String widgetName ) {
      RDSWidget widget = null;

      try {
         Class<?> widgetClass = Class.forName( widgetName );
         Constructor<?> widgetConstructor = widgetClass.getConstructor(
               RDSDashboard.class );
         widget = (RDSWidget)widgetConstructor.newInstance( this );
      } catch (ClassNotFoundException ex) {
         RDSUtil.alert( "%s: unable to locate widget class [%s]",
               getName(), widgetName );
      } catch (NoSuchMethodException ex) {
         RDSUtil.alert( "%s: widget [%s] does not possess a constructor " +
         		"of the proper form", getName(), widgetName );
      } catch (Exception ex) {
         RDSUtil.alert( ex );
      }

      return widget;
   }

   /**
    * Returns the calculated size of widgets to be placed in the header
    * of the dashboard.
    * 
    * @return  a {@code Dimension} containing the widget size
    */
   public Dimension getWidgetSize() {
      return new Dimension( 2 * headerHeight, headerHeight );
   }

   /**
    * Creates the primary content panel that contains the individual
    * dashboard panels.  A database table contains references to the
    * panel objects to be loaded.  For each, a radio button is also
    * created to enable panel selection.
    * 
    * @return  the content panel object
    */
   private JPanel createContentPanel() {
      RDSUtil.trace( getName() + ": adding main-panel content" );

      final CardLayout contentPanelLayout = new CardLayout();
      final JPanel contentPanel = new JPanel( contentPanelLayout );

      List<Map<String,String>> panelList = db.getResultMapList(
            "SELECT id, object FROM dashboard " +
            "WHERE zone = 'panel' " +
            "ORDER BY id" );
      if (panelList == null || panelList.isEmpty())
         return contentPanel;

      for (Map<String,String> panelMap : panelList) {
         // create the dashboard panel
         final RDSDashboardPanel panel = createPanel( panelMap.get( "id" ),
               panelMap.get( "object" ) );
         if (panel == null)
            continue;

         contentPanel.add( panel, panel.getName() );
         RDSUtil.inform( "%s: added content panel [%s] as [%s]",
               getName(), panelMap.get( "object" ), panel.getName() );

         // create the radio button for this panel
         JRadioButton button = new JRadioButton( panel.getName() );
         button.setPreferredSize( new Dimension( 0, LABEL_HEIGHT ) );
         button.setToolTipText( panel.getDescription() );
         button.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               contentPanelLayout.show( contentPanel,
                     evt.getActionCommand() );
               panel.refreshPanel();
            }
         } );
         button.setFocusable( false );
         button.setSelected( true );

         panels.add( panel );
         buttons.add( button );
      }

      return contentPanel;
   }

   /**
    * Creates an instance of an {@code RDSDashboardPanel}, specified by name.
    * 
    * @param   id         the panel id number
    * @param   panelName  the fully qualified class name of the dashboard
    *          panel to be constructed
    * @return  the dashboard panel object, or {@code null} if the panel could
    *          not be constructed
    */
   private RDSDashboardPanel createPanel( String id, String panelName ) {
      RDSDashboardPanel panel = null;
      if (panelName == null || panelName.isEmpty()) {
         RDSUtil.alert( "no panel class name specified for id %s", id );
         return null;
      }

      try {
         Class<?> panelClass = Class.forName( panelName );
         // attempt to call the 2-argument constructor, if it exists
         try {
            Constructor<?> panelConstructor = panelClass.getConstructor(
                  String.class, Container.class );
            panel = (RDSDashboardPanel)panelConstructor.newInstance(
                  id, this );
         } catch (NoSuchMethodException ex) {
            Constructor<?> panelConstructor = panelClass.getConstructor(
                  Container.class );
            panel = (RDSDashboardPanel)panelConstructor.newInstance( this );
         }
      } catch (ClassNotFoundException ex) {
         RDSUtil.alert( "%s: unable to locate panel class [%s]",
               getName(), panelName );
         RDSUtil.alert( ex );
      } catch (NoSuchMethodException ex) {
         RDSUtil.alert( "%s: panel [%s] does not possess a constructor " +
         		"of the proper form", getName(), panelName );
         RDSUtil.alert( ex );
      } catch (Exception ex) {
         RDSUtil.alert( "%s: failed to create panel [%s]",
               getName(), panelName );
         ex.printStackTrace();
      }

      return panel;
   }

   /**
    * Creates the side panel, containing the radio button subpanel and
    * the login/logout button.
    * 
    * @return the panel object
    */
   private JPanel createSidePanel() {
      ButtonGroup buttonGroup = new ButtonGroup();
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.Y_AXIS ) );
      buttonPanel.setMaximumSize( new Dimension( sidePanelWidth,
            RDSDashboardPanel.HEIGHT ) );
      buttonPanel.setBorder( BorderFactory.createEtchedBorder() );
      buttonPanel.setAlignmentX( Component.CENTER_ALIGNMENT );
      for (AbstractButton button : buttons) {
         buttonGroup.add( button );
         buttonPanel.add( button );
      }

      JPanel sidePanel = new JPanel();
      sidePanel.setLayout( new BoxLayout( sidePanel, BoxLayout.Y_AXIS ) );
      sidePanel.setPreferredSize( new Dimension( sidePanelWidth,
            RDSDashboardPanel.HEIGHT ) );

      sidePanel.add( buttonPanel );
      sidePanel.add( Box.createVerticalGlue() );
      sidePanel.add( admin.getLoginPanel() );

      return sidePanel;
   }


   /*
    * --- access methods ---
    */

   /**
    * Gets the database object for the dashboard.
    */
   public RDSDatabase getDatabase() {
      return db;
   }

   /**
    * Gets the class that manages administrative controls for the dashboard.
    */
   public RDSAdmin getAdmin() {
      return admin;
   }

   /**
    * Gets the base URL (codebase or working directory, depending on context)
    * for the dashboard.
    */
   public URL getBaseURL() {
      return baseURL;
   }

   /**
    * Gets one of the dashboard's parameter values.
    * 
    * @param   key  the parameter name
    * @return  the value of the named parameter, or {@code null} if the
    *          parameter does not exist
    */
   public String getParam( String key ) {
      if (params == null)
         return null;
      return params.get( key );
   }

   /**
    * Sets one of the dashboard's parameter values.  The parameter map is
    * lazily created.
    * 
    * @param   key    the parameter name
    * @param   value  the value of the parameter
    */
   public void setParam( String key, String value ) {
      if (params == null)
         params = new HashMap<String, String>();
      params.put( key, value );
   }


   /*
    * --- additional class methods ---
    */

   /**
    * Registers a status element object with the dashboard, placing its
    * corresponding label into the dashboard header, and creating an action
    * that causes the calling panel to be displayed when the label is clicked.
    * 
    * @param   panel    the content panel registering the status element
    * @param   element  the RDSStatusElement object
    */
   public void registerStatusElement( final RDSDashboardPanel panel,
         RDSStatusElement element ) {
      final Dimension labelSize = new Dimension( 200, 16 );

      JLabel label = element.getLabel();
      label.setPreferredSize( labelSize );
      label.setMaximumSize( labelSize );
      label.setFont( label.getFont().deriveFont( Font.BOLD,
            (float)STATUS_FONT_SIZE ) );

      label.addMouseListener( new MouseAdapter() {
         @Override
         public void mouseClicked( MouseEvent evt ) {
            // click the radio button associated with the panel
            int index = panels.indexOf( panel );
            if (index < 0)
               return;

            AbstractButton button = buttons.get( index );
            if (button != null && button.isVisible())
               button.doClick();
         }
      } );

      statusPanel.add( label );
      statusPanel.validate();
   }

   /**
    * Restarts the timer that performs periodic refreshes of the dashboard.
    * If the time timer does not exist, it is created and started.
    */
   public void restartRefreshTimer() {
      if (refreshTimer == null) {
         refreshTimer = new Timer( REFRESH_DELAY, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
               refreshDashboard();
            }
         } );
         refreshTimer.setInitialDelay( 0 );
      }

      refreshTimer.restart();
   }

   /**
    * Stops the timer that provides periodic refreshes of the dashboard.
    */
   public void stopRefreshTimer() {
      if (refreshTimer != null)
         refreshTimer.stop();
   }

   /**
    * Updates portions of the dashboard user interface that refresh
    * periodically.
    */
   public void refreshDashboard() {
      refreshContentPanels();
      refreshMessageTable();
      admin.updateLoginPanel();
   }

   /**
    * Refreshes each of the content panels.  In addition, the list of
    * available panels for selection is updated to exclude those for
    * which the current user does not have access privileges.
    */
   private void refreshContentPanels() {
      // obtain the panel view settings from the database
      Map<String,Integer> viewMap = new HashMap<String,Integer>();
      String query =
            "SELECT action, level FROM adminPermissions " +
            "WHERE action LIKE 'view %'";
      try {
         Statement stmt = db.connect().createStatement();
         ResultSet res = stmt.executeQuery( query );
         while (res.next()) {
            String panelName = res.getString( "action" ).replaceFirst(
                  "view ", "" ).toLowerCase();
            viewMap.put( panelName, res.getInt( "level" ) );
         }
         stmt.close();
      } catch (SQLException ex) {
         RDSUtil.alert(
               "%s: error obtaining panel access settings, query = [%s]",
         		getName(), query );
         RDSUtil.alert( ex );
      }

      // refresh the individual panels
      for (int i = 0, n = panels.size(); i < n; i++) {
         RDSDashboardPanel panel = panels.get( i );

         // refresh the panel
         panel.refreshPanel();

         // set the visibility of the panel button
         Integer viewLevel = viewMap.get( panel.getName().toLowerCase() );
         boolean viewable = (viewLevel == null ||
               admin.isAuthenticated( viewLevel, false )) ? true : false;
         buttons.get( i ).setVisible( viewable );

         // if the current panel should not be viewable, select a
         // different panel (currently, the first)
         if (panel.isVisible() && !viewable)
            buttons.get( 0 ).doClick();
      }
   }

   /**
    * Changes the view to the named dashboard panel and displays the
    * specified data.
    * 
    * @param   panelName  the name of the dashboard panel
    * @param   data       the item(s) to display
    */
   public void displayPanel( String panelName, Object... data ) {
      if (panelName == null || panelName.isEmpty())
         return;

      for (int i = 0, n = panels.size(); i < n; i++) {
         RDSDashboardPanel panel = panels.get( i );

         if (panelName.equals( panel.getName() )) {
            panel.display( data );
            buttons.get( i ).doClick();
            return;
         }
      }
   }

   /**
    * Updates the contents of the message table with records from the
    * database.
    */
   private void refreshMessageTable() {
      if (messageTable == null)
         return;

      String query =
            "SELECT message, DATE_FORMAT( stamp, '%r' ) FROM messages " +
            "ORDER BY stamp DESC";
      try {
         messageTable.populateTable( query );
      } catch (SQLException ex) {
         RDSUtil.alert(
               "%s: sql error during message table refresh, query = [%s]",
               getName(), query );
         RDSUtil.alert( ex );
      }
   }

}  // end RDSDashboard class
