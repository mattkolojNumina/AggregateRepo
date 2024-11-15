package rdsGraphics;

import java.awt.Color;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;

import rds.RDSUtil;


public class RDSImage
      extends RDSObject {
   public static final int DEFAULT_WIDTH = 100;
   public static final int DEFAULT_HEIGHT = 100;
   public static final Color NEUTRAL_COLOR = Color.LIGHT_GRAY;

   private PPath path;
   private PImage image;

   public RDSImage() {
      super();
      paramNames = new String[] { "name", "file", "x", "y", "width", "height",
            "rotation", "hint" };
      normalTransparency = 0.5f;

      path = PPath.createRectangle( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      path.setPickable( false );
      path.setPaint( NEUTRAL_COLOR );
      path.setTransparency( normalTransparency );
      path.setStroke( DEFAULT_STROKE );
      addChild( path );

      image = new PImage();
      image.setPickable( false );
      addChild( image );

      setBounds( 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT );
      addAttribute( "type", "image" );

      setImage( null );
   }

   public PNode getPrimaryChild() {
      return (image.getVisible()) ? image : path;
   }

   public void setValue( String value ) {
      addAttribute( "value", value );
   }

   public String getParamValue( String param ) {
      if (param.equalsIgnoreCase( "file" ))
         return (String)getAttribute( "file" );
      else
         return super.getParamValue( param );
   }

   public void setParamValue( String param, String value ) {
      if (param.equalsIgnoreCase( "file" ))
         setImage( value );
      else
         super.setParamValue( param, value );
   }

   /**
    * Loads the specified image.
    * @param file the image filename
    */
   public void setImage( String file ) {
      addAttribute( "file", file );

      boolean imageLoaded = false;

      if (file != null && !file.isEmpty()) {
         URL imageURL = null;
         try {
            imageURL = new URL( codeBase + file );
         } catch (MalformedURLException ex) {
            RDSUtil.alert( "unable to create path for [" + file + "]" );
            RDSUtil.alert( ex );
         }
         RDSUtil.inform(  "loading image [%s]", imageURL.toString() );
         image.setImage( Toolkit.getDefaultToolkit().getImage( imageURL ) );
         if (image.getImage() != null)
            imageLoaded = true;
      }

      if (imageLoaded) {
         image.setBounds( getBounds() );
         image.setVisible( true );
         path.setVisible( false );
      } else {
         path.setBounds( getBounds() );
         path.setVisible( true );
         image.setVisible( false );
      }
   }

}  /* end RDSImage class */
