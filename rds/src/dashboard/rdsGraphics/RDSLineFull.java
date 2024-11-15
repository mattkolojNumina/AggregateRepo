package rdsGraphics;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


public class RDSLineFull
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 100;
   public static final int DEFAULT_HEIGHT = 25;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;
   public static final Color FULL_COLOR = Color.YELLOW;

   private PPath path;

   public RDSLineFull() {
      super();

      path = PPath.createRectangle( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( FULL_COLOR );
      path.setTransparency( initTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "linefull" );
   }

   public PNode getPrimaryChild() {
      return path;
   }

   public void setValue( String value ) {
      if (value == null || value.equals( getAttribute( "value" ) ))
         return;

      addAttribute( "value", value );

      if (value.equalsIgnoreCase( "full" )) {
         path.setPaint( FULL_COLOR );
         startFlash();
      } else {
         stopFlash();
         path.setPaint( NEUTRAL_COLOR );
      }
   }

}  /* end RDSLineFull class */
