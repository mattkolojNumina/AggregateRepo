/*
 * RDSStatusElement.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import rds.RDSUtil;

/**
 * A utility for displaying a status indicator in the header area of an
 * RDS Dashboard.
 */
// TODO describe /how/ to add the element to the dashboard
public class RDSStatusElement {

   /**
    * The values that a status element can assume.
    */
   public static enum Status {
      /** A status that does not match one of the other values */
      Other,

      /** A good status */
      OK,

      /** A status indicating caution or warning */
      Caution,

      /** A status representing an error or fault state */
      Error
   }

   // images used for status "lights"
   private static final ImageIcon greenIcon = RDSUtil.createImageIcon(
         RDSStatusElement.class, "images/green.png" );
   private static final ImageIcon redIcon = RDSUtil.createImageIcon(
         RDSStatusElement.class, "images/red.png" );
   private static final ImageIcon yellowIcon = RDSUtil.createImageIcon(
         RDSStatusElement.class, "images/yellow.png" );
   private static final ImageIcon grayIcon = RDSUtil.createImageIcon(
         RDSStatusElement.class, "images/gray.png" );

   private Status status;
   private JLabel label;

   /**
    * Constructs a status element object.  This constructor should be
    * called by an {@code RDSDashboardPanel} that wishes to register a
    * status element within its parent dashboard.
    * 
    * @param   description  text description, to be displayed 
    */
   public RDSStatusElement( String description ) {
      createLabel( description );
      setStatus( Status.Other );
   }

   /**
    * Gets the label associated with this status element.
    */
   public JLabel getLabel() {
      return label;
   }

   /**
    * Sets the status of this object.  Updating the status will change the
    * icon associated with this element's panel on the dashboard.
    * 
    * @param   status  the new status of the element
    */
   public void setStatus( Status status ) {
      if (this.status == status)
         return;

      this.status = status;

      // update the icon
      if (status == Status.OK)
         label.setIcon( greenIcon );
      else if (status == Status.Caution)
         label.setIcon( yellowIcon );
      else if (status == Status.Error)
         label.setIcon( redIcon );
      else
         label.setIcon( grayIcon );
   }

   /**
    * Creates a panel for insertion into the dashboard.  The element's
    * status indicator (colored icon) is set separately.
    * 
    * @param   description  text description of the element
    */
   private void createLabel( String description ) {
      label = new JLabel( description, JLabel.RIGHT );
      label.setHorizontalTextPosition( JLabel.LEFT );
   }

}  // end RDSStatusElement class
