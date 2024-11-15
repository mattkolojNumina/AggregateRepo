package rdsGraphics;

import java.awt.geom.Point2D;
import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;


public class RDSHint
      extends PNode {
   public static final Color HINT_BOX_COLOR = Color.YELLOW;
   public static final int HINT_MARGIN = 3;
   public static final int HINT_HORIZ_OFFSET = 5;
   public static final int HINT_VERT_OFFSET = 20;

   private PNode hintNode;
   private PText hintText;
   private PPath hintBox;
   private HintHandler hintHandler;

   public RDSHint() {
      hintNode = null;

      hintText = new PText();
      hintText.setOffset( HINT_MARGIN, 0 );

      hintBox = PPath.createRectangle( 0, 0, 1, 1 );
      hintBox.setPaint( HINT_BOX_COLOR );
      hintBox.addChild( hintText );

      addChild( hintBox );
      clearHint();
   }

   public void updateHint( PNode node, Point2D p ) {
      if (node == null) {
         clearHint();
         return;
      }

      // update hint text only if underlying node has changed
      if (node != hintNode) {
         hintNode = node;
         updateHintText( (String)node.getAttribute( "hint" ) );
      }

      // if hint is displayed, update the position
      if (getVisible())
         updateHintPosition( p );
   }

   public void updateHintText( String text ) {
      if (text == null || text.isEmpty()) {
         clearHint();
         return;
      }

      hintText.setText( text );
      PBounds bounds = hintText.getBounds();
      hintBox.setBounds( 0, 0,
            bounds.getWidth() + 2 * HINT_MARGIN, bounds.getHeight() );
      setVisible( true );
   }

   public void updateHintPosition( Point2D p ) {
      double x = p.getX() + HINT_HORIZ_OFFSET;
      double y = p.getY() + HINT_VERT_OFFSET;
      PNode parent = this.getParent();

      if (parent != null) {
         PBounds bounds = hintBox.getBounds();
         PBounds parentBounds = parent.getBounds();
         x = Math.min( x, parentBounds.width - bounds.width );
         x = Math.max( x, 0 );
         if (y > parentBounds.height - bounds.height)
            y = p.getY() - bounds.height - 1;
         y = Math.max( y, 0 );
      }

      hintBox.setOffset( x, y );
   }

   public void clearHint() {
      hintText.setText( "" );
      hintBox.setBounds( 0, 0, 1, 1 );
      setVisible( false );
   }

   public HintHandler getHintHandler() {
      if (hintHandler == null)
         hintHandler = new HintHandler();
      return hintHandler;
   }

   class HintHandler
         extends PBasicInputEventHandler {
      public HintHandler() {
         super();
      }

      public void mouseMoved( PInputEvent evt ) {
         updateHint( evt.getPickedNode(), evt.getCanvasPosition() );
      }

      public void mouseDragged( PInputEvent evt ) {
         // hint moves with pan, but not with zoom
         if (evt.isLeftMouseButton())
            updateHint( evt.getPickedNode(), evt.getCanvasPosition() );
         else
            clearHint();
      }
   }
}
