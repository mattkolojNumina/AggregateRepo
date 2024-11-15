/*
 * RDSWidget.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.awt.Dimension;
import javax.swing.JPanel;


/**
 * A generic container class that may be added to the header of an
 * RDS Dashboard.  All panels that wish to act as dashboard widgets
 * must be subclasses of this class. 
 */
public class RDSWidget
      extends JPanel {

   /**
    * Constructs a generic RDS Widget object.
    * 
    * @param   dashboard  the dashboard that contains the widget
    */
   public RDSWidget( RDSDashboard dashboard ) {
      super();
      setName( "Generic RDS Widget" );

      Dimension widgetSize = dashboard.getWidgetSize();
      setMinimumSize( widgetSize );
      setPreferredSize( widgetSize );
      setMaximumSize( widgetSize );
   }

}  /* end RDSWidget class */
