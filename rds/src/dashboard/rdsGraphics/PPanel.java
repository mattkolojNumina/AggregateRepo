
package rdsGraphics;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.umd.cs.piccolo.PCanvas;


public class PPanel
      extends JPanel {
   private PCanvas canvas;

   public PPanel() {
      super();
      setLayout( new BorderLayout() );
      canvas = new PCanvas();
      add( canvas, BorderLayout.CENTER );

      beforeInitialize();

      /*
       * Manipulation of Piccolo's scene graph should be done from Swing's
       * event dispatch thread since Piccolo is not thread safe.  This code
       * calls initialize() from that thread once the PFrame is initialized,
       * so you are safe to start working with Piccolo in the initialize()
       * method.
       */
      SwingUtilities.invokeLater(
            new Runnable() {
               public void run() {
                  initialize();
                  repaint();
               }
            } );
   }

   public PCanvas getCanvas() {
      return canvas;
   }

   /**
    * Pre-initialization routine, to be overridden by subclasses.  This
    * method is called on the original thread; graphical manipulation
    * should be performed in the <code>initialize()</code> method.
    */
   public void beforeInitialize() {}

   /**
    * Initialization routine, to be overridden by subclasses.  This method
    * is called on the Swing event-dispatching thread.
    */
   public void initialize() {}
}
