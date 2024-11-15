
package rdsGraphics;

import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.*;


/**
 * The superclass of all RDS objects.
 */
public abstract class RDSObject
      extends PNode {
   public static final BasicStroke DEFAULT_STROKE = new BasicStroke( 0.5f );

   // animation settings (may be overridden by subclases)
   protected long  flashDuration        = 1000;  // cycle duration (msec)
   protected long  flashRate            = 50;    // update rate (msec)
   protected float initTransparency     = 0.50f;
   protected float normalTransparency   = 0.00f;
   protected float minFlashTransparency = 0.20f;
   protected float maxFlashTransparency = 0.80f;

   protected static String codeBase = "";

   protected String[] paramNames;
   protected boolean locked;
   protected boolean flashing;
   protected PInterpolatingActivity flashActivity;

   /**
    * Creates an RDS Object.
    */
   public RDSObject() {
      super();
      paramNames = new String[] { "name", "x", "y", "width", "height",
            "rotation", "hint" };
      locked = false;
      flashing = false;

      flashActivity = new PInterpolatingActivity(
            flashDuration, flashRate, Integer.MAX_VALUE,
            PInterpolatingActivity.SOURCE_TO_DESTINATION_TO_SOURCE ) {
         @Override
         public void setRelativeTargetValue( float zeroToOne ) {
            if (flashing) {
               getPrimaryChild().setTransparency( minFlashTransparency +
                     zeroToOne * (maxFlashTransparency - minFlashTransparency) );
               setPickable( true );
            } else {
               getPrimaryChild().setTransparency( normalTransparency );
               setPickable( normalTransparency > 0.0f );
            }
         }
      };
      flashActivity.setSlowInSlowOut( false );
   }

   /**
    * Creates and schedules an activity to make the object "flash" by
    * varying the transparency of its path.  In addition, the node is
    * designated as "pickable" for hint display and zooming.
    */
   public void startFlash() {
      if (flashing)
         return;

      flashing = true;
      flashActivity.setLoopCount( Integer.MAX_VALUE );
      addActivity( flashActivity );
   }

   /**
    * Stops the flash activity.  The object's transparency is reset to its
    * idle value; if this value is zero (the object is not visible), then
    * the object is also marked as not pickable.
    */
   public void stopFlash() {
      flashing = false;
      flashActivity.terminate();

      getPrimaryChild().setTransparency( normalTransparency );
      setPickable( normalTransparency > 0.0f );
   }

   /**
    * Sets the code base (a static variable) for the RDSObject class, and
    * thus for all RDS Object subclasses.
    * 
    * @param   codeBase  the new code base, a {@code String}
    */
   public static void setCodeBase( String codeBase ) {
      RDSObject.codeBase = codeBase;
   }

   /**
    * Determines whether or not this object is locked.  Parent containers
    * may use this value to decide whether or not the object may be
    * selected or changed.
    * 
    * @return   {@code true} if this object is locked, {@code false} otherwise
    */
   public boolean isLocked() {
      return locked;
   }

   /**
    * Sets the value of the {@code locked} parameter, indicating that
    * this object should not be modified.
    * 
    * @param   locked  {@code true} to lock the object, {@code false} to
    *          unlock it
    */
   public void setLocked( boolean locked ) {
      this.locked = locked;
   }

   /**
    * Gets the primary child (path/text/etc.) of this object.
    */
   public abstract PNode getPrimaryChild();

   /**
    * Sets the value of the object.  There is no default behavior for this
    * method; subclasses are responsible for their own implementation.
    * @param value  the value to set for the object
    */
   public abstract void setValue( String value );

   /**
    * Sets the hint (a.k.a. tooltip) for the object.
    * @param hint  the new hint to set for the object
    */
   public void setHint( String hint ) {
      addAttribute( "hint", hint );
   }

   /**
    * Returns a copy of the array of parameter names for this object.
    * @return  an array of strings containing the parameter names
    */
   public String[] getParamNames() {
      return paramNames.clone();
   }

   /**
    * Gets a parameter value for the object.
    * 
    * @param   param  the name of the parameter
    * @return  the value of the named parameter (or {@code null} if
    *          the parameter does not exist)
    */
   public String getParamValue( String param ) {
      DecimalFormat formatter = new DecimalFormat( "0.0" );

      if (param.equalsIgnoreCase( "name" ))
         return (String)getAttribute( "name" );
      if (param.equalsIgnoreCase( "x" ))
         return formatter.format( getGlobalX() );
      if (param.equalsIgnoreCase( "y" ))
         return formatter.format( getGlobalY() );
      if (param.equalsIgnoreCase( "width" ))
         return formatter.format( getWidth() );
      if (param.equalsIgnoreCase( "height" ))
         return formatter.format( getHeight() );
      if (param.equalsIgnoreCase( "rotation" )) {
         double theta = (getRotation() * 180.0 / Math.PI) % 360;
         if (theta > 180.0)
            theta -= 360.0;
         return formatter.format( theta );
      }
      if (param.equalsIgnoreCase( "hint" ))
         return (String)getAttribute( "hint" );
      return null;
   }

   /**
    * Sets a parameter value for the object.
    * 
    * @param   param  the name of the parameter
    * @param   value  the new value for the named parameter
    */
   public void setParamValue( String param, String value ) {
      if (param.equalsIgnoreCase( "name" )) {
         addAttribute( "name", value );
      } else if (param.equalsIgnoreCase( "x" )) {
         translate( Double.parseDouble( value ) - getGlobalX(), 0.0 );
      } else if (param.equalsIgnoreCase( "y" )) {
         translate( 0.0, Double.parseDouble( value ) - getGlobalY() );
      } else if (param.equalsIgnoreCase( "width" )) {
         setWidth( Double.parseDouble( value ) );
      } else if (param.equalsIgnoreCase( "height" )) {
         setHeight( Double.parseDouble( value ) );
      } else if (param.equalsIgnoreCase( "rotation" )) {
         double theta = Double.parseDouble( value ) % 360;
         theta = Math.min( Math.max( theta, -45 ), 45 );
         rotateAboutPoint( theta * Math.PI / 180.0 - getRotation(),
               getX(), getY() );
      } else if (param.equalsIgnoreCase( "hint" )) {
         addAttribute( "hint", value );
      }
   }

   protected double getGlobalX() {
      Point2D.Double p = new Point2D.Double( getX(), getY() );
      localToGlobal( p );
      return p.x;
   }

   protected double getGlobalY() {
      Point2D.Double p = new Point2D.Double( getX(), getY() );
      localToGlobal( p );
      return p.y;
   }

   /**
    * Sets the bounds of the object.  When the object is resized, the
    * child path is also resized.
    */
   @Override
   public boolean setBounds( double x, double y, double width,
         double height ) {
      return super.setBounds( x, y, width, height ) &&
            getPrimaryChild().setBounds( x, y, width, height );
   }

   @Override
   public void translate(double dx, double dy) {
      double theta = getRotation();

      if (theta == 0.0) {
         super.translate( dx, dy );
         return;
      }

      double x = getX();
      double y = getY();
      rotateAboutPoint( -theta, x, y );
      super.translate( dx, dy );
      rotateAboutPoint( theta, x, y );
   }

   /**
    * Writes a serialized representation of the object to an output stream.
    * @param out  the output stream
    */
   public void serialize( BufferedWriter out ) {
      try {
         out.write( "[" + getAttribute( "type" ) + "]" );
         out.newLine();
         for (String param : paramNames) {
            String value = getParamValue( param );
            if (value != null) {
               out.write( param + "=" + value );
               out.newLine();
            }
         }
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

}  /* end RDSObject class */
