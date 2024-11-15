
package rdsGraphics;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


public class RDSJam
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 10;
   public static final int DEFAULT_HEIGHT = 30;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;
   public static final Color FAULT_COLOR = Color.RED;

   private PPath path;

   public RDSJam() {
      super();

      path = PPath.createEllipse( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( FAULT_COLOR );
      path.setTransparency( initTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "jam" );
   }

   public PNode getPrimaryChild() {
      return path;
   }

   public void setValue( String value ) {
      if (value == null || value.equals( getAttribute( "value" ) ))
         return;

      addAttribute( "value", value );

      if (value.equalsIgnoreCase( "fault" )) {
         path.setPaint( FAULT_COLOR );
         startFlash();
      } else {
         stopFlash();
         path.setPaint( NEUTRAL_COLOR );
      }
   }

}  /* end RDSJam class */
