/*
 * LinkPanel.java
 * 
 * (c) 2010 Numina Group, Inc.
 */

package rdsDashboard.panel;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.*;
import javax.swing.*;

import rds.*;
import rdsDashboard.RDSDashboardPanel;


/**
 * A dashboard panel for displaying links to external resources that will
 * be opened outside the dashboard.  This panel is only appropriate in an
 * applet context.
 */
public class LinkPanel
      extends RDSDashboardPanel {

   // constants

   /** The font-size scale for header text. */
   private static final float HEADER_FONT_SCALE = 1.5f;

   /** The font-size scale for link text. */
   private static final float LINK_FONT_SCALE = 1.1f;

   /** The height of a separator. */
   private static final int SEPARATOR_SIZE = 25;

   /** The text color for links. */
   private static final Color LINK_COLOR = Color.BLUE;

   private static final int BORDER = 15;
   private static final int HGAP = 15;
   private static final int VGAP = 10;


   // ui variables
   private JPanel panel;


   /**
    * Constructs a dashboard panel for displaying links to external
    * resources.
    * 
    * @param   parentContainer  the parent container (dashboard, applet,
    *          or frame) of this panel
    */
   public LinkPanel( Container parentContainer ) {
      super( parentContainer );
      setName( "External Links" );
      setDescription( "View links to external documents and resources" );

      createUI();
   }

   /**
    * Creates the user interface for this panel.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      createTitledBorder( false );

      panel = new ScrollablePanel();
      panel.setLayout( new GridBagLayout() );
      panel.setBorder( BorderFactory.createEmptyBorder(
            BORDER, BORDER, BORDER, BORDER ) );

      JScrollPane scrollPane = new JScrollPane( panel );
      scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      scrollPane.getViewport().setOpaque( false );

      populatePanel();
      add( scrollPane, BorderLayout.CENTER );
   }

   /**
    * Creates the links and other specified items, adding them to the panel.
    */
   private void populatePanel() {
      String query =
            "SELECT * FROM dashboardLinks " +
            "WHERE ordinal > 0 " +
            "ORDER BY ordinal";

      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = stmt.executeQuery( query );
         while (res.next()) {
            String type = res.getString( "type" );
            String title = res.getString( "title" );
            String description = res.getString( "description" );
            String link = res.getString( "link" );

            if ("header".equals( type ))
               addHeader( title );
            else if ("separator".equals( type ))
               addSeparator( SEPARATOR_SIZE );
            else if ("link".equals( type ))
               addLink( title, description, link );
         }
      } catch (SQLException ex) {
         RDSUtil.alert( "sql error, query = [" + query + "]" );
         RDSUtil.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }
   }

   /**
    * Adds a header element to the panel.  A corresponding sub-panel is
    * also created and added to house subsequent links.
    */
   private void addHeader( String header ) {
      JLabel label = new JLabel( header );
      Font font = label.getFont();
      float newSize = font.getSize2D() * HEADER_FONT_SCALE;
      label.setFont( font.deriveFont( Font.BOLD, newSize ) );

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridwidth = 2;
      c.insets = new Insets( 0, 0, VGAP, 0 );
      panel.add( label, c );
   }

   /** Adds a separator element to the panel. */
   private void addSeparator( int size ) {
      JLabel sep = new JLabel();

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.insets = new Insets( 0, 0, size + VGAP, 0 );
      panel.add( sep, c );
   }

   /** Adds a relative-link element to the panel. */
   private void addLink( String title, String description,
         final String link ) {
      JButton button = new JButton( title );
      Font font = button.getFont();
      float newSize = font.getSize2D() * LINK_FONT_SCALE;
      button.setFont( font.deriveFont( newSize ) );
      button.setForeground( LINK_COLOR );
      button.setAlignmentX( JComponent.CENTER_ALIGNMENT );
      button.addActionListener( new ActionListener() {
         public void actionPerformed( ActionEvent evt ) {
            openLink( link );
         }
      } );

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.insets = new Insets( 0, 0, VGAP, HGAP );
      panel.add( button, c );

      // add html tags to force word wrapping
      JLabel label = new JLabel( "<html>"  + description + "</html>" );
      c.gridx = 1;
      c.weightx = 1.0;
      c.insets = new Insets( 0, 0, VGAP, 0 );
      panel.add( label, c );
   }

   /** Opens the specified link in a new browser window. */
   private void openLink( String link ) {
      if (link == null || link.isEmpty())
         return;

      RDSUtil.trace( "%s: opening link [%s]", getName(), link );

      //get context from top-level applet
      AppletContext appletContext = null;
      Container c = this;
      while( c.getParent() != null && !( c instanceof Applet ) )
         c = c.getParent();
      if (c instanceof Applet)
         appletContext = ((Applet)c).getAppletContext();
      else {
         RDSUtil.alert( "%s: unable to obtain applet context for link [%s]",
               getName(), link );
         return;
      }

      // open a new browser window for image viewing
      try {
         URI linkURI = new URI( link );
         appletContext.showDocument( baseURL.toURI().resolve(
               linkURI ).toURL(), "_blank" );
      } catch (Exception ex) {
         RDSUtil.alert( "%s: error opening link [%s]", getName(), link );
      }
   }


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
   }  // end ScrollablePanel inner class

}  // end LinkPanel class
