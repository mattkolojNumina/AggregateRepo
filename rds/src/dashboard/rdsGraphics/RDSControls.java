
package rdsGraphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class RDSControls
      extends JPanel
      implements ActionListener {
   RDSGraphicsPanel graphicsPanel;

   public RDSControls( RDSGraphicsPanel graphicsPanel ) {
      super();
      this.graphicsPanel = graphicsPanel;

      JButton resetButton = new JButton( "Reset View" );
      resetButton.addActionListener( this );
      add( resetButton );
   }

   /**
    * Handles actions performed on this panel
    */
   public void actionPerformed( ActionEvent evt ) {
      if (evt.getActionCommand().equals( "Reset View" ))
         graphicsPanel.zoomToFitAll();
   }
}
