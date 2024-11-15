package rdsGraphics;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


public class RDSZone
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 30;
   public static final int DEFAULT_HEIGHT = 30;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;
   public static final Color FAULT_COLOR = Color.RED;
   public static final Color BOX_COLOR = Color.ORANGE.darker();
   public static final float BOX_TRANSPARENCY = 0.5f;
   public static final float OFF_TRANSPARENCY = 0.0f;

   private PPath path;

   public RDSZone() {
      super();

      path = PPath.createRectangle( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( BOX_COLOR );
      path.setTransparency( initTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "zone" );
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
      } else if (value.equalsIgnoreCase( "box" )) {
         normalTransparency = BOX_TRANSPARENCY;
         stopFlash();
         path.setPaint( BOX_COLOR );
      } else {
         normalTransparency = OFF_TRANSPARENCY;
         stopFlash();
         path.setPaint( NEUTRAL_COLOR );
      }
   }

}  /* end RDSZone class */
