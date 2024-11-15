
package rdsGraphics;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


public class RDSMotor
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 30;
   public static final int DEFAULT_HEIGHT = 30;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;
   public static final Color FAULT_COLOR = Color.RED;
   public static final Color RUN_COLOR = Color.GREEN;
   public static final float RUN_TRANSPARENCY = 0.5f;
   public static final float OFF_TRANSPARENCY = 0.25f;

   private PPath path;

   public RDSMotor() {
      super();

      path = PPath.createEllipse( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( RUN_COLOR );
      path.setTransparency( initTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "motor" );
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
      } else if (value.equalsIgnoreCase( "run" )) {
         normalTransparency = RUN_TRANSPARENCY;
         stopFlash();
         path.setPaint( RUN_COLOR );
      } else {
         normalTransparency = OFF_TRANSPARENCY;
         stopFlash();
         path.setPaint( NEUTRAL_COLOR );
      }
   }

}  /* end RDSMotor class */
