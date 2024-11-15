/*
 * FixedRatioComponent.java
 * 
 * (c) 2010, Numina Group, Inc.
 */

package rds;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


/**
 * A wrapper around a UI component that maintains a fixed aspect ratio when
 * its parent container is resized.
 */
public class FixedRatioComponent {

   /** The wrapped component. */
   private Component component;

   /** The aspect ratio (width/height) of the component. */
   private double ratio;

   
   /**
    * Constructs a wrapped component to resize with the specified fixed
    * aspect ratio.
    * 
    * @param   component  the component to resize
    * @param   ratio      the width-to-height ratio of the component
    */
   public FixedRatioComponent( Component component, double ratio ) {
      super();

      this.component = component;
      setRatio( ratio );
   }

   /** Gets the wrapped component. */
   public Component getComponent() {
      return component;
   }

   /** Monitors the component's parent for resizing events. */
   public void monitorParent() {
      if (component == null)
         return;

      Container parent = component.getParent();
      if (parent == null)
         return;

      parent.addComponentListener( new ComponentAdapter() {
         public void componentResized( ComponentEvent evt ) {
            resize();
         }
      } );
   }

   /** Gets the aspect ratio of the component. */
   public double getRatio() {
      return ratio;
   }

   /** Sets the aspect ratio of the component. */
   public void setRatio( double ratio ) {
      if (ratio <= 0.0)
         throw new IllegalArgumentException( "ratio <= 0" );
      this.ratio = ratio;
   }

   /** Sets the aspect ratio of the component to its current ratio. */
   public void fixCurrentRatio() {
      if (component == null)
         return;

      setRatio( component.getWidth() / component.getHeight() );
   }

   /**
    * Updates the preferred size to fill the parent while maintaining
    * aspect ratio.  The component's minimum and maximum dimesions are
    * also taken into account.
    */
   private void resize() {
      if (component == null)
         return;

      Container parent = component.getParent();
      if (parent == null)
         return;

      double newWidth = parent.getWidth();
      double newHeight = parent.getHeight();

      if (component.isMaximumSizeSet()) {
         Dimension maxDim = component.getMaximumSize();
         newWidth = Math.min( newWidth, maxDim.getWidth() );
         newHeight = Math.min( newHeight, maxDim.getHeight() );
      }
      if (component.isMinimumSizeSet()) {
         Dimension minDim = component.getMinimumSize();
         newWidth = Math.max( newWidth, minDim.getWidth() );
         newHeight = Math.max( newHeight, minDim.getHeight() );
      }

      double w, h;
      if (newWidth <= 0.0 || newHeight <= 0.0)
         w = h = 0.0;
      else if (newWidth / newHeight < ratio) {
         w = newWidth;
         h = w / ratio;
      } else {
         h = newHeight;
         w = h * ratio;
      }

      component.setPreferredSize( new Dimension( (int)w, (int)h ) );
      component.invalidate();
   }

   /**
    * Maintains a component at the specified aspect ratio when its parent
    * is resized.
    * 
    * @param   component  the component to resize  
    * @param   ratio      the width-to-height ratio to maintain
    */
   public static void maintainRatio( Component component, double ratio ) {
      (new FixedRatioComponent( component, ratio )).monitorParent();
   }

}  // end FixedRatioComponent class
