/*
 * RDSDashboardPanel.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.applet.Applet;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

import rds.*;


/**
 * An extension of {@code JPanel} with variables and methods suitable for
 * further extension by classes that will be panels in an RDS Dashboard.
 * All such panels must be subclasses of this class.
 */
public abstract class RDSDashboardPanel
      extends JPanel {

   /*
    * --- constants ---
    */

   /** The standard width for all dashboard panels. */
   public static final int WIDTH  = 640;

   /** The standard height for all dashboard panels. */
   public static final int HEIGHT = 480;

   /**
    * The amount of padding between the panel border and the contents.
    * This value may also be used for padding space between subpanels.
    */
   public static final int PADDING = 12;

   /** The spacing between UI elements. */
   protected static final int SPACING = 5;

   /** The panel title font. */
   protected static final Font TITLE_FONT = UIManager.getFont(
         "TitledBorder.font" ).deriveFont( Font.BOLD, 18.0f );


   /*
    * --- class variables ---
    */

   // class variables, typically shared between all dashboard panels
   protected RDSDashboard parentDashboard;
   protected RDSDatabase  db;
   protected RDSAdmin     admin;
   protected URL          baseURL;

   // other per-panel class variables
   protected String id;
   protected String description;
   protected Map<String,String> params;


   /*
    * -- constructor, initialization, and layout methods ---
    */

   /**
    * Constructs a panel for insertion into the dashboard.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public RDSDashboardPanel( String id, Container parentContainer ) {
      super();
      setName( "Generic RDS Panel" );

      this.id = id;
      setClassVariables( parentContainer );
      setPanelParameters();

      String name = getParam( "name" );
      if (name != null && !name.isEmpty())
         setName( name );
      String desc = getParam( "decription" );
      if (desc != null && !desc.isEmpty())
         setName( desc );

      createUI();
   }

   /**
    * Constructs a panel for insertion into the dashboard.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public RDSDashboardPanel( Container parentContainer ) {
      this( "", parentContainer );
   }

   /**
    * Sets the values of the class variables.  The method for obtaining
    * the values depends on the type of the enclosing container.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   private void setClassVariables( Container parentContainer ) {
      if (parentContainer instanceof RDSDashboard) {
         parentDashboard = (RDSDashboard)parentContainer;
         db = parentDashboard.getDatabase();
         admin = parentDashboard.getAdmin();
         baseURL = parentDashboard.getBaseURL();
      } else {  // panel is content pane of applet or app
         parentDashboard = null;
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
                     System.getProperty( "user.dir" ) + "\\" );
            } catch (java.net.MalformedURLException ex) {
               RDSUtil.alert( getName() +
                     ": error obtaining working directory" );
               RDSUtil.alert( ex );
               baseURL = null;
            }
         }

         // connect to database (defaults to localhost if no host specified)
         db = new RDSDatabase( hostName );

         // create admin for access control
         admin = new RDSAdmin( db );
      }
   }

   /**
    * Sets the parameters (key/value pairs) for this panel, as specified
    * in the 'dashboard' database table.
    */
   private void setPanelParameters() {
      String paramString = null;

      if (id != null && !id.isEmpty())
         paramString = db.getValue( String.format(
               "SELECT params FROM dashboard " +
               "WHERE zone = 'panel' " +
               "AND id = %s",
               id ), null );
      else
         paramString = db.getValue(
               "SELECT params FROM dashboard " +
               "WHERE zone = 'panel' " +
               "AND object = '" + getClass().getName() + "'",
               null );

      if (paramString == null || paramString.isEmpty())
         return;

      for (String param : paramString.split( "," )) {
         String[] keyVal = param.split( "=", 2 );
         if (keyVal.length == 2)
            setParam( keyVal[0], keyVal[1] );
      }
   }

   /**
    * Creates the user interface for this panel.  This method does nothing
    * more than fix the size of the panel.
    */
   private void createUI() {
      Dimension panelSize = new Dimension( WIDTH, HEIGHT );
      setMinimumSize( panelSize );
      setPreferredSize( panelSize );
      setMaximumSize( panelSize );
   }


   /*
    * --- access methods ---
    */

   /**
    * Gets the database for this panel.
    */
   public RDSDatabase getDatabase() {
      return db;
   }

   /**
    * Sets the database for this panel.
    */
   public void setDatabase( RDSDatabase db ) {
      this.db = db;
   }

   /**
    * Gets the {@code RDSAdmin} object that manages administrative access
    * for this panel.
    */
   public RDSAdmin getAdmin() {
      return admin;
   }

   /**
    * Sets the {@code RDSAdmin} object that manages administrative access
    * for this panel.
    */
   public void setAdmin( RDSAdmin admin ) {
      this.admin = admin;
   }

   /**
    * Gets the base URL (code base or working directory) for this panel.
    */
   public URL getBaseURL() {
      return baseURL;
   }

   /**
    * Sets the base URL (code base or working directory) for this panel.
    */
   public void setBaseURL( URL baseURL ) {
      this.baseURL = baseURL;
   }

   /**
    * Gets a description of this panel; the default text is the same as
    * the panel name.
    */
   public String getDescription() {
      if (description != null && !description.isEmpty())
         return description;

      return getName();
   }

   /**
    * Sets the description for this panel.  The description may be used
    * in short summaries and/or tooltip text.
    */
   public void setDescription( String description ) {
      this.description = description;
   }

   /**
    * Gets one of this panel's parameter values.  If the parameter map does
    * not exist, or if the map does not contain the specified key, {@code
    * null} is returned.
    * 
    * @param   key  the parameter name
    * @return  the value of the named parameter
    */
   public String getParam( String key ) {
      if (params == null)
         return null;
      return params.get( key );
   }

   /**
    * Sets one of this panel's parameter values.  The parameter map is
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
    * Sets the border for the panel, creating a {@code TitledBorder} using
    * the name of the panel.  Depending on the value of the {@code fullBorder}
    * parameter, one of two borders is created:  an etched border that
    * fully surrounds the panel and contains interior padding or a single
    * "etched" line along the top of the panel. 
    * 
    * @param   fullBorder  if {@code true}, a border is created that is
    *          displayed on all four sides of the panel; if {@code false},
    *          the border is only located along the top of the panel 
    */
   protected void createTitledBorder( boolean fullBorder ) {
      Border border;

      if (fullBorder) {
         // an etched titled border with interior padding
         TitledBorder titledBorder = BorderFactory.createTitledBorder(
               BorderFactory.createEtchedBorder(), getName() );
         titledBorder.setTitleFont( TITLE_FONT );
         border = BorderFactory.createCompoundBorder(
               titledBorder,
               BorderFactory.createEmptyBorder(
                     0, PADDING, PADDING, PADDING ) );
      } else {
         // a titled border using an "etched" top-line
         Color c = getBackground();
         TitledBorder titledBorder = BorderFactory.createTitledBorder(
               BorderFactory.createCompoundBorder(
                     BorderFactory.createMatteBorder(
                           1, 0, 0, 0, c.darker() ),
                     BorderFactory.createMatteBorder(
                           1, 0, 0, 0, c.brighter() ) ),
               getName() );
         titledBorder.setTitleFont( TITLE_FONT );
         border = titledBorder;
      }

      setBorder( border );
   }

   /**
    * Refreshes the contents of the panel, typically as a result of a
    * periodic timer by an enclosing dashboard.  As this method may be
    * called at any time, it should complete in a timely fashion.
    * <p>
    * By default, this method performs no action; it is intended (although
    * not required) to be overridden by subclasses.
    */
   public void refreshPanel() {}

   /**
    * Displays the specified item(s) within the panel.  This method is
    * intended as a data pass-through from other dashboard panels (for
    * instance, to display drill-down information about an object that
    * a user has interacted with in an overview panel).  It is typically
    * called by the {@code RDSDashboard.displayPanel()} method since
    * panels do not, in general, have direct access to each other.
    * <p>
    * By default, this method performs no action; it is intended (although
    * not required) to be overridden by subclasses.
    * 
    * @param   data  items to be displayed (interpretation is left up to
    *          the subclass) 
    */
   public void display( Object... data ) {}


   /*
    * --- superclass method overrides ---
    */

   /**
    * Gets the name of this dashboard panel.  If the panel has a defined
    * parameter called 'name', that value is returned; otherwise, this
    * method delegates to the superclass.
    */
   @Override
   public String getName() {
      String name = getParam( "name" );
      if (name != null && !name.isEmpty())
         return name;
      else
         return super.getName();
   }
}  // end RDSDashboardPanel class