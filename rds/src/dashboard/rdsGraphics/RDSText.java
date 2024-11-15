
package rdsGraphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;


public class RDSText
      extends RDSObject {
   public static final String DEFAULT_FONT = "Arial";
   public static final int DEFAULT_SIZE = 12;
   public static final float DEFAULT_JUSTIFICATION = 0.5f;
   public static final int DEFAULT_COLOR = 0x000000;

   private PText text;

   public RDSText() {
      super();
      paramNames = new String[] { "name", "x", "y", "rotation",
            "justification", "font", "size", "color", "hint" };

      text = new PText();
      text.setPickable( false );
      addChild( text );

      setBounds( 0, 0, 1, 1 );
      addAttribute( "type", "text" );

      setFont( DEFAULT_FONT );
      setSize( DEFAULT_SIZE );
      setColor( DEFAULT_COLOR );
      setJustification( DEFAULT_JUSTIFICATION );
      setValue( "<dynamic text>" );
   }

   private double getTextReferenceX() {
      return text.getX() + text.getWidth() * text.getJustification();
   }

   private void setTextReferenceX( double x ) {
      text.setX( x - text.getWidth() * text.getJustification() );
   }

   public PNode getPrimaryChild() {
      return text;
   }

   public void setValue( String value ) {
      double x = getTextReferenceX();

      addAttribute( "value", value );
      text.setText( value );
      setTextReferenceX( x );
      setBounds( text.getBounds() );
      if (value == null || value.isEmpty())
         setVisible( false );
      else
         setVisible( true );
   }

   @Override
   public String getParamValue( String param ) {
      DecimalFormat formatter = new DecimalFormat( "0.0" );

      if (param.equalsIgnoreCase( "width" ))
         return formatter.format( 0.0 );
      if (param.equalsIgnoreCase( "font" ))
         return (String)getAttribute( "font" );
      if (param.equalsIgnoreCase( "size" ))
         return (String)getAttribute( "size" );
      if (param.equalsIgnoreCase( "color" ))
         return (String)getAttribute( "color" );
      if (param.equalsIgnoreCase( "justification" ))
         return (String)getAttribute( "justification" );
      return super.getParamValue( param );
   }

   @Override
   public void setParamValue( String param, String value ) {
      if (param.equalsIgnoreCase( "font" ))
         setFont( value );
      else if (param.equalsIgnoreCase( "size" ))
         setSize( Integer.parseInt( value ) );
      else if (param.equalsIgnoreCase( "color" ))
         setColor( Integer.decode( value ) );
      else if (param.equalsIgnoreCase( "justification" ))
         setJustification( Float.parseFloat( value ) );
      else
         super.setParamValue( param, value );
   }

   public void setFont( String font ) {
      double x = getTextReferenceX();

      addAttribute( "font", font );
      text.setFont( new Font( font, Font.PLAIN, text.getFont().getSize() ) );

      setTextReferenceX( x );
      setBounds( text.getBounds() );
   }

   public void setSize( int size ) {
      double x = getTextReferenceX();

      addAttribute( "size", Integer.toString( size ) );
      text.setFont( text.getFont().deriveFont( (float)size ) );

      setTextReferenceX( x );
      setBounds( text.getBounds() );
   }

   public void setColor( int color ) {
      addAttribute( "color", String.format( "0x%06X", color ) );
      text.setTextPaint( new Color( color ) );
   }

   public void setJustification( float justification ) {
      double x = getTextReferenceX();

      addAttribute( "justification", Float.toString( justification ) );
      text.setJustification( justification );

      setTextReferenceX( x );
      setBounds( text.getBounds() );
   }

   @Override
   protected double getGlobalX() {
      Point2D.Double p = new Point2D.Double( getTextReferenceX(), getY() );
      localToGlobal( p );
      return p.x;
   }

   @Override
   protected double getGlobalY() {
      Point2D.Double p = new Point2D.Double( getTextReferenceX(), getY() );
      localToGlobal( p );
      return p.y;
   }

   /**
    * Sets the bounds of the object.  When the object is resized, the
    * text's font size is adjusted to fit within the new bounds.
    */
   @Override
   public boolean setBounds( double x, double y, double width,
         double height ) {
      if (!super.setBounds( x, y, width, height ))
         return false;  // bounds haven't changed

      double factor = (text == null || text.getWidth() == 0 ||
            text.getHeight() == 0) ? 0.0 :
            Math.min( width / text.getWidth(), height / text.getHeight() );
      int fontSize = (int)Math.max( text.getFont().getSize() * factor, 1.0 );

      text.setFont( text.getFont().deriveFont( (float)fontSize ) );
      text.setBounds( x, y, width, height );
      setTextReferenceX( x + width * text.getJustification() );
      addAttribute( "size", Integer.toString( fontSize ) );

      return true;
   }

}  /* end RDSText class */
