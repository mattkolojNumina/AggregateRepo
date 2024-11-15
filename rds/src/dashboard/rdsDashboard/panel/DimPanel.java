/*
 * DimPanel.java
 * 
 * (c) 2012 Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.*;
import javax.swing.*;

import org.math.plot.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for monitoring and configuring a camera-based
 * dimensioner.
 */
public class DimPanel
      extends RDSDashboardPanel {
   private static final float LARGE_FONT_SIZE = 20.0f;
   private static final float NORMAL_FONT_SIZE = 13.0f;
   private static final float SUBTITLE_FONT_SIZE = 15.0f;

   private static final int WAIT_TIME   = 500;  // command wait time (msec)
   private static final int WAIT_PERIOD = 500;  // command wait period (msec)

   /** The dimensioner instance name. */
   private String instance;

   // ui variables
   private JTabbedPane tabs;
   private GraphicsSubpanel graphics;
   private ConfigSubpanel config;

   /**
    * Constructs a dashboard panel for monitoring and configuring the
    * dimensioner.
    */
   public DimPanel( String id, Container parentContainer ) {
      super( id, parentContainer );
      this.instance = getParam( "instance" );
      if (instance == null || instance.isEmpty())
         instance = "dim";

      setName( "Dimensioner" );
      setDescription( "Monitor or configure the dimensioner" );

      createUI();
   }

   /** Creates the user interface for this panel. */
   private void createUI() {
      setLayout( new BorderLayout( PADDING, PADDING ) );
      createTitledBorder( false );

      tabs = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
      tabs.setFocusable( false );
      tabs.addTab( "Control", new ControlSubpanel() );
      tabs.addTab( "Graphics", graphics = new GraphicsSubpanel() );
      tabs.addTab( "Configuration", config = new ConfigSubpanel() );

      add( tabs, BorderLayout.CENTER );
   }


   /*
    * ===== control subpanel =====
    */

   private class ControlSubpanel
         extends JPanel
         implements ActionListener {
      JLabel lengthLabel, widthLabel, heightLabel;
      JLabel xLabel, yLabel, angleLabel, confidenceLabel;

      public ControlSubpanel() {
         super();
         createUI();

         refresh();
      }

      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );

         // result panel
         JPanel resultPanel = new JPanel( new SpringLayout() );

         resultPanel.add( createLabel( "Length: ", LARGE_FONT_SIZE ) );
         resultPanel.add( lengthLabel = createLabel( "0.0",
               LARGE_FONT_SIZE ) );
         resultPanel.add( createLabel( "Width: ", LARGE_FONT_SIZE ) );
         resultPanel.add( widthLabel  = createLabel( "0.0",
               LARGE_FONT_SIZE ) );
         resultPanel.add( createLabel( "Height: ", LARGE_FONT_SIZE ) );
         resultPanel.add( heightLabel = createLabel( "0.0",
               LARGE_FONT_SIZE ) );
         resultPanel.add( createLabel( "Center X: ", NORMAL_FONT_SIZE ) );
         resultPanel.add( xLabel = createLabel( "0.0",
               NORMAL_FONT_SIZE ) );
         resultPanel.add( createLabel( "Center Y: ", NORMAL_FONT_SIZE ) );
         resultPanel.add( yLabel = createLabel( "0.0",
               NORMAL_FONT_SIZE ) );
         resultPanel.add( createLabel( "Angle: ", NORMAL_FONT_SIZE ) );
         resultPanel.add( angleLabel = createLabel( "0.0",
               NORMAL_FONT_SIZE ) );
         resultPanel.add( createLabel( "Confidence: ", NORMAL_FONT_SIZE ) );
         resultPanel.add( confidenceLabel = createLabel( "0%",
               NORMAL_FONT_SIZE ) );

         SpringUtilities.makeCompactGrid( resultPanel, 7, 2,
               SPACING, SPACING, SPACING, SPACING );

         JPanel containerPanel = new JPanel();
         containerPanel.setLayout( new BoxLayout( containerPanel,
               BoxLayout.Y_AXIS ) );
         containerPanel.add( Box.createVerticalGlue() );
         containerPanel.add( resultPanel );
         containerPanel.add( Box.createVerticalGlue() );
         this.add( containerPanel, BorderLayout.CENTER );

         // button panel
         JPanel buttonPanel = new JPanel();

         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( this );
         buttonPanel.add( refreshButton );

         JButton measureButton = new JButton( "Measure" );
         measureButton.addActionListener( this );
         buttonPanel.add( measureButton );

         JButton calibrateButton = new JButton( "Calibrate" );
         calibrateButton.addActionListener( this );
         buttonPanel.add( calibrateButton );

         this.add( buttonPanel, BorderLayout.SOUTH );
      }

      private boolean confirmCalibrate() {
         int retval = JOptionPane.showConfirmDialog( DimPanel.this,
               "<html>Changing the dimensioner calibration settings will<br>" +
               "affect all future measurements.  Place a level surface<br>" +
               "or flat object at the position from which objects will<br>" +
               "be measured.<br>" +
               "<br>" +
               "Do you want to proceed with calibration?</html>",
               "Confirm Calibration", JOptionPane.YES_NO_OPTION );
         return (retval == JOptionPane.YES_OPTION);
      }

      private void command( String command ) {
         db.execute( "REPLACE INTO runtime SET "
               + "name = '%s/status', " + "value = '%s'", instance,
               command );

         // monitor for completed command
         showWaitingDialog( "Waiting for dimensioner...",
               new SwingWorker<Boolean,Void>() {
                  protected Boolean doInBackground() {
                     while (!isCancelled()) {
                        String status = db.getRuntime( instance + "/status" );
                        if ("idle".equals( status ))
                           return true;
                        try {
                           Thread.sleep( WAIT_PERIOD );
                        } catch (InterruptedException ex) {}
                     }
                     return false;  // canceled
                  }
               } );

         // refresh the results and graphs
         refresh();
         graphics.refresh();
         config.refresh();
      }

      private void refresh() {
         DecimalFormat doubleFormatter = new DecimalFormat( "0.0" );
         DecimalFormat percentFormatter = new DecimalFormat( "0%" );
         lengthLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/length" ), 0.0 ) ) );
         widthLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/width" ), 0.0 ) ) );
         heightLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/height" ), 0.0 ) ) );
         xLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/x" ), 0.0 ) ) );
         yLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/y" ), 0.0 ) ) );
         angleLabel.setText( doubleFormatter.format( RDSUtil.stringToDouble(
               db.getRuntime( instance + "/angle" ), 0.0 ) ) );
         confidenceLabel.setText( percentFormatter.format(
               RDSUtil.stringToDouble(
               db.getRuntime( instance + "/confidence" ), 0.0 ) ) );

         lengthLabel.getParent().validate();
      }

      public void actionPerformed( ActionEvent evt ) {
         String actionCommand = evt.getActionCommand();
         if ("Refresh".equals( actionCommand ))
            refresh();
         else if ("Measure".equals( actionCommand ))
            command( "measure" );
         else if ("Calibrate".equals( actionCommand )) {
            if (confirmCalibrate() && admin.isAuthenticatedInteractive(
                  "calibrate dimensioner", DimPanel.this ))
            command( "calibrate" );
         }
      }
   }  // end ControlSubpanel class


   /*
    * ===== graphics subpanel =====
    */

   /** A subpanel for displaying graphical dimensioner results. */
   private class GraphicsSubpanel
         extends JPanel
         implements ActionListener {
      private JPanel plotPanel;
      private Plot3DPanel allPointsPanel;
      private Plot3DPanel validPointsPanel;
      private Plot2DPanel topSurfacePanel;
      private Plot2DPanel histPanel;

      private class DimPoint {
         double x, y, z;
         String type;
      }

      public GraphicsSubpanel() {
         super();
         createUI();

         refresh();
      }

      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );
         plotPanel = new JPanel( new CardLayout() );

         JPanel selectionPanel = new JPanel();
         ButtonGroup selectionGroup = new ButtonGroup();

         JRadioButton topButton = new JRadioButton( "Top Surface" );
         selectionPanel.add( topButton );
         selectionGroup.add( topButton );
         topButton.addActionListener( this );
         topSurfacePanel = new Plot2DPanel( BorderLayout.EAST );
         plotPanel.add( topSurfacePanel, topButton.getActionCommand() );

         JRadioButton validButton = new JRadioButton( "Valid Points" );
         selectionPanel.add( validButton );
         selectionGroup.add( validButton );
         validButton.addActionListener( this );
         validPointsPanel = new Plot3DPanel( BorderLayout.EAST );
         plotPanel.add( validPointsPanel, validButton.getActionCommand() );

         JRadioButton allButton = new JRadioButton( "All Points" );
         selectionPanel.add( allButton );
         selectionGroup.add( allButton );
         allButton.addActionListener( this );
         allPointsPanel = new Plot3DPanel( BorderLayout.EAST );
         plotPanel.add( allPointsPanel, allButton.getActionCommand() );

         JRadioButton histButton = new JRadioButton( "Height Data" );
         selectionPanel.add( histButton );
         selectionGroup.add( histButton );
         histButton.addActionListener( this );
         histPanel = new Plot2DPanel();
         plotPanel.add( histPanel, histButton.getActionCommand() );

         JPanel controlPanel = new JPanel();
         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( this );
         controlPanel.add( refreshButton );

         add( selectionPanel, BorderLayout.NORTH );
         add( plotPanel, BorderLayout.CENTER );
         add( controlPanel, BorderLayout.SOUTH );
         topButton.doClick();
      }

      private List<DimPoint> getAllPoints() {
         List<DimPoint> pointList = new ArrayList<DimPoint>();
         String sql = String.format(
               "SELECT x, y, z, type FROM dimPoints " +
               "WHERE instance = '%s' " +
               "AND rawZ > 0 " +
               "ORDER BY ordinal",
               instance );
         Statement stmt = null;
         try {
            stmt = db.connect().createStatement();
            ResultSet res = db.executeTimedQuery( stmt, sql );
            while (res.next()) {
               DimPoint p = new DimPoint();
               p.x = res.getDouble( "x" );
               p.y = res.getDouble( "y" );
               p.z = res.getDouble( "z" );
               p.type = res.getString( "type" );
               pointList.add( p );
            }
         } catch (SQLException ex) {
            RDSUtil.alert( "sql error, query = [" + sql + "]" );
            RDSUtil.alert( ex );
            pointList = null;
         } finally {
            RDSDatabase.closeQuietly( stmt );
         }

         return pointList;
      }

      private double[] listToArray( List<DimPoint> pointList, String dim ) {
         double[] pts = new double[pointList.size() * dim.length()];

         int i = 0;
         for (DimPoint p : pointList) {
            if (dim.contains( "x" ))  pts[i++] = p.x;
            if (dim.contains( "y" ))  pts[i++] = p.y;
            if (dim.contains( "z" ))  pts[i++] = p.z;
         }

         return pts;
      }

      private double[][] listToArray2D( List<DimPoint> pointList, String dim ) {
         double[][] pts = new double[pointList.size()][dim.length()];

         int i = 0;
         for (DimPoint p : pointList) {
            int j = 0;
            if (dim.contains( "x" ))  pts[i][j++] = p.x;
            if (dim.contains( "y" ))  pts[i][j++] = p.y;
            if (dim.contains( "z" ))  pts[i][j++] = p.z;
            i++;
         }

         return pts;
      }

      private List<DimPoint> getPointsByType( List<DimPoint> pointList,
            String... types ) {
         List<DimPoint> typeList = new ArrayList<DimPoint>();
         if (types == null)
            return typeList;

         for (DimPoint p : pointList)
            for (String type : types)
               if (type.equals( p.type ))
                  typeList.add( p );

         return typeList;
      }

      /** Refresh the panel contents. */
      protected void refresh() {
         List<DimPoint> pointList = getAllPoints();

         topSurfacePanel.removeAllPlots();
         addScatterPlot( topSurfacePanel, Color.GREEN, pointList, "top" );
         addScatterPlot( topSurfacePanel, Color.RED, pointList, "boundary" );
         topSurfacePanel.addLinePlot( "outline", Color.MAGENTA, getOutline() );

         validPointsPanel.removeAllPlots();
         addScatterPlot3d( validPointsPanel, Color.GRAY, pointList, "valid" );
         addScatterPlot3d( validPointsPanel, Color.GREEN, pointList, "top" );
         addScatterPlot3d( validPointsPanel, Color.RED, pointList, "boundary" );

         allPointsPanel.removeAllPlots();
         addScatterPlot3d( allPointsPanel, Color.LIGHT_GRAY, pointList,
               "excluded" );
         addScatterPlot3d( allPointsPanel, Color.GRAY, pointList,
               "valid", "top", "boundary" );

         histPanel.removeAllPlots();
         double[] histPts = listToArray( getPointsByType( pointList,
               "valid", "top", "boundary" ), "z" );
         if (histPts.length > 1)
            histPanel.addHistogramPlot( "Histogram", Color.BLUE, histPts, 50 );
         histPanel.setAxisLabels( "Z", "" );
      }

      private void addScatterPlot( Plot2DPanel panel, Color color,
            List<DimPoint> pointList, String... types ) {
         double[][] pts = listToArray2D( getPointsByType( pointList, types ),
               "xy" );
         if (pts.length > 0)
            panel.addScatterPlot( types[0], color, pts );
      }

      private void addScatterPlot3d( Plot3DPanel panel3d, Color color,
            List<DimPoint> pointList, String... types ) {
         double[][] pts = listToArray2D( getPointsByType( pointList, types ),
               "xyz" );
         if (pts.length > 0)
            panel3d.addScatterPlot( types[0], color, pts );
      }

      private double[][] getOutline() {
         double l = RDSUtil.stringToDouble(
               db.getRuntime( instance + "/length" ), 0.0 );
         double w = RDSUtil.stringToDouble(
               db.getRuntime( instance + "/width" ), 0.0 );
         double x = RDSUtil.stringToDouble(
               db.getRuntime( instance + "/x" ), 0.0 );
         double y = RDSUtil.stringToDouble(
               db.getRuntime( instance + "/y" ), 0.0 );
         double angle = RDSUtil.stringToDouble(
               db.getRuntime( instance + "/angle" ), 0.0 );
         if ("deg".equals(
               db.getControl( instance, "angleUnit", "DEG" ).toLowerCase() ))
            angle = Math.toRadians( angle );

         Point2D center = new Point2D.Double( x, y );
         rotate( center, -angle );
         double cx = center.getX(), cy = center.getY();
         Point2D p1 = new Point2D.Double( cx - l * 0.5, cy - w * 0.5 );
         Point2D p2 = new Point2D.Double( cx + l * 0.5, cy - w * 0.5 );
         Point2D p3 = new Point2D.Double( cx + l * 0.5, cy + w * 0.5 );
         Point2D p4 = new Point2D.Double( cx - l * 0.5, cy + w * 0.5 );
         rotateAll( angle, p1, p2, p3, p4 );

         double[][] outline = new double[5][2];
         outline[0][0] = p1.getX(); outline[0][1] = p1.getY();
         outline[1][0] = p2.getX(); outline[1][1] = p2.getY();
         outline[2][0] = p3.getX(); outline[2][1] = p3.getY();
         outline[3][0] = p4.getX(); outline[3][1] = p4.getY();
         outline[4][0] = p1.getX(); outline[4][1] = p1.getY();

         return outline;
      }

      private void rotate( Point2D p, double angle ) {
         double x = p.getX();
         double y = p.getY();
         double c = Math.cos( angle );
         double s = Math.sin( angle );
         p.setLocation( x * c - y * s, x * s + y * c );
      }

      private void rotateAll( double angle, Point2D... pts ) {
         for (Point2D p : pts)
            rotate( p, angle );
      }

      public void actionPerformed( ActionEvent evt ) {
         String command = evt.getActionCommand();
         if ("Refresh".equals( command ))
            refresh();
         else
            ((CardLayout)plotPanel.getLayout()).show( plotPanel,
                  command );
      }
   } // end GraphicsSubpanel class


   /*
    * ===== config subpanel =====
    */

   /** A subpanel for configuring the dimensioner. */
   private class ConfigSubpanel
         extends JPanel 
         implements ActionListener {
      private static final int FIELD_COLS = 10;

      private Map<String,JTextField> appFieldMap;
      private Map<String,JTextField> hardwareFieldMap;

      public ConfigSubpanel() {
         super();
         createUI();

         refresh();
      }

      private void createUI() {
         setLayout( new BorderLayout( PADDING, PADDING ) );

         JPanel controlPanel = new JPanel();
         JButton refreshButton = new JButton( "Refresh" );
         refreshButton.addActionListener( this );
         controlPanel.add( refreshButton );

         add( createConfigPanel(), BorderLayout.CENTER );
         add( controlPanel, BorderLayout.SOUTH );
      }

      private JPanel createConfigPanel() {
         JPanel p = new JPanel( new GridBagLayout() );

         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.NORTH;
         c.weightx = c.weighty = 1.0;

         c.insets = new Insets( PADDING, PADDING, 0, 0 );
         p.add( createHardwarePanel(), c );

         c.insets = new Insets( PADDING, PADDING, 0, PADDING );
         p.add( createAppPanel(), c );

         return p;
      }

      private JPanel createHardwarePanel() {
         hardwareFieldMap = new HashMap<String,JTextField>();
         JPanel p = new ScrollablePanel();
         p.setLayout( new SpringLayout() );

         List<Map<String,String>> configList = db.getResultMapList(
               "SELECT name, value FROM dimConfig " +
               "WHERE instance = '%s' " +
               "ORDER BY name",
               instance );
         int rows = 0;
         for (Map<String,String> config : configList) {
            String name = config.get( "name" );
            p.add( createLabel( name + ": ", JLabel.RIGHT,
                  NORMAL_FONT_SIZE, false ) );
            JTextField field = new JTextField( config.get( "value" ),
                  FIELD_COLS );
            hardwareFieldMap.put( name, field );
            p.add( field );

            JButton readButton = new JButton( "Read" );
            // TODO
            p.add( readButton );

            JButton writeButton = new JButton( "Write" );
            // TODO
            p.add( writeButton );

            rows++;
         }

         SpringUtilities.makeCompactGrid( p, rows, 4, 5, 3, 5, 3 );

         JScrollPane scrollPane = new JScrollPane( p );
         scrollPane.setHorizontalScrollBarPolicy(
               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
         scrollPane.getViewport().setOpaque( false );

         return createSubtitlePanel( scrollPane, "Hardware Settings" );
      }

      private JComponent createAppPanel() {
         appFieldMap = new HashMap<String,JTextField>();
         JPanel p = new ScrollablePanel();
         p.setLayout( new SpringLayout() );

         List<Map<String,String>> configList = db.getResultMapList(
               "SELECT name, description, value, editable FROM controls " +
               "WHERE zone = '%s' " +
               "ORDER BY name",
               instance );
         int rows = 0;
         for (Map<String,String> config : configList) {
            String name = config.get( "name" );
            p.add( createLabel( config.get( "description" ) + ": ",
                  JLabel.RIGHT, NORMAL_FONT_SIZE, false ) );
            JTextField field = new JTextField( config.get( "value" ),
                  FIELD_COLS );
            field.setEditable( "yes".equals( config.get( "editable" ) ) );
            appFieldMap.put( name, field );
            p.add( field );
            rows++;
         }

         SpringUtilities.makeCompactGrid( p, rows, 2, 5, 3, 5, 3 );

         JScrollPane scrollPane = new JScrollPane( p );
         scrollPane.setHorizontalScrollBarPolicy(
               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
         scrollPane.getViewport().setOpaque( false );

         return createSubtitlePanel( scrollPane, "Application Settings" );
      }

      private void refresh() {
         // hardware settings
         List<Map<String,String>> configList = db.getResultMapList(
               "SELECT name, value FROM dimConfig " +
               "WHERE instance = '%s' " +
               "ORDER BY name",
               instance );
         for (Map<String,String> config : configList) {
            JTextField field = hardwareFieldMap.get( config.get( "name" ) );
            if (field != null) {
               field.setColumns( FIELD_COLS );
               field.setText( config.get( "value" ) );
            }
         }

         // app settings
         configList = db.getResultMapList(
               "SELECT name, description, value FROM controls " +
               "WHERE zone = '%s' " +
               "ORDER BY name",
               instance );
         for (Map<String,String> config : configList) {
            JTextField field = appFieldMap.get( config.get( "name" ) );
            if (field != null) {
               field.setColumns( FIELD_COLS );
               field.setText( config.get( "value" ) );
            }
         }
      }

      public void actionPerformed( ActionEvent evt ) {
         String command = evt.getActionCommand();
         if ("Refresh".equals( command ))
            refresh();
      }
   } // end ConfigSubpanel class

   /** A panel that scrolls vertically but fills the viewport width. */
   private static class ScrollablePanel
         extends JPanel
         implements Scrollable {
      public Dimension getPreferredScrollableViewportSize() {
         return getPreferredSize();
      }
      public int getScrollableBlockIncrement( Rectangle visibleRect,
            int orientation, int direction ) {
         if (orientation == SwingConstants.VERTICAL)
            return visibleRect.height;
         else
            return visibleRect.width;
      }
      public boolean getScrollableTracksViewportHeight() { return false; }
      public boolean getScrollableTracksViewportWidth() { return true; }
      public int getScrollableUnitIncrement( Rectangle visibleRect,
            int orientation, int direction ) {
         return 10;
      }
   }  // end ScrollablePanel class

   private static JPanel createSubtitlePanel( JComponent c, String title ) {
      JLabel titleLabel = createLabel( title, JLabel.CENTER,
            SUBTITLE_FONT_SIZE, true );

      JPanel p = new JPanel( new BorderLayout() );
      p.add( titleLabel, BorderLayout.NORTH );
      p.add( c, BorderLayout.CENTER );

      return p;
   }

   private static JLabel createLabel( String text, int align, float fontSize,
         boolean isBold ) {
      JLabel label = new JLabel( text, align );
      label.setFont( label.getFont().deriveFont(
            (isBold) ? Font.BOLD : Font.PLAIN, fontSize ) );
      return label;
   }

   private static JLabel createLabel( String text, float fontSize ) {
      return createLabel( text, JLabel.RIGHT, fontSize, true );
   }

   private void showWaitingDialog( String title, SwingWorker<?,?> worker ) {
      JProgressBar bar = new JProgressBar();
      bar.setIndeterminate( true );
      JOptionPane pane = new JOptionPane( bar,
            JOptionPane.PLAIN_MESSAGE, 0, null,
            new String[] {"Cancel"} );
      final JDialog dialog = pane.createDialog( this, title );

      worker.addPropertyChangeListener( new PropertyChangeListener() {
         public void propertyChange( PropertyChangeEvent evt ) {
            if ("state".equals( evt.getPropertyName() ) &&
                  evt.getNewValue() == SwingWorker.StateValue.DONE) {
               dialog.setVisible( false );
               dialog.dispose();
            }
         }
      } );
      worker.execute();

      // check for prompt completion
      try {
         worker.get( WAIT_TIME, TimeUnit.MILLISECONDS );
         return;
      } catch (Exception ex) {}

      dialog.setVisible( true );  // blocks until canceled or worker completes
      worker.cancel( true );
   }
}
