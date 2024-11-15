
package rdsGraphics;

import java.awt.Color;

import rds.RDSUtil;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


public class RDSIndicator
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 30;
   public static final int DEFAULT_HEIGHT = 30;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;
   public static final float ON_TRANSPARENCY = 0.75f;
   public static final float OFF_TRANSPARENCY = 0.0f;

   private PPath path;

   public RDSIndicator() {
      super();

      path = PPath.createEllipse( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( NEUTRAL_COLOR );
      path.setTransparency( initTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "indicator" );
   }

   public PNode getPrimaryChild() {
      return path;
   }

   public void setValue( String value ) {
      if (value == null || value.equals( getAttribute( "value" ) ))
         return;

      addAttribute( "value", value );

      if (value.isEmpty()) {
         normalTransparency = OFF_TRANSPARENCY;
         stopFlash();
         path.setPaint( NEUTRAL_COLOR );
      } else {
         int val = 0;
         try {
            val = Integer.parseInt( value, 16 );
         } catch (NumberFormatException ex) {
            RDSUtil.alert( "unable to parse indicator value [%s]", value );
         }

         if (val >= 0) {
            normalTransparency = ON_TRANSPARENCY;
            stopFlash();
            path.setPaint( new Color( val ) );
         } else {
            path.setPaint( new Color( -val ) );
            startFlash();
         }
      }
   }

}  /* end RDSIndicator class */
