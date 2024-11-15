
package rdsGraphics;

import java.awt.Color;
import java.awt.Font;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;


public class RDSStaticText
      extends RDSObject {
   public static final String DEFAULT_FONT = "Arial";
   public static final int DEFAULT_SIZE = 12;
   public static final int DEFAULT_COLOR = 0x000000;

   private PText text;

   public RDSStaticText() {
      super();
      paramNames = new String[] { "text", "x", "y", "rotation", "font",
            "size", "color", "hint" };

      text = new PText();
      text.setPickable( false );
      addChild( text );

      setBounds( 0, 0, 1, 1 );
      addAttribute( "type", "statictext" );

      setText( "<no text>" );
      setFont( DEFAULT_FONT );
      setSize( DEFAULT_SIZE );
      setColor( DEFAULT_COLOR );
   }

   public PNode getPrimaryChild() {
      return text;
   }

   public void setValue( String value ) {}

   public String getParamValue( String param ) {
      if (param.equalsIgnoreCase( "text" ))
         return (String)getAttribute( "text" );
      else if (param.equalsIgnoreCase( "font" ))
         return (String)getAttribute( "font" );
      else if (param.equalsIgnoreCase( "size" ))
         return (String)getAttribute( "size" );
      if (param.equalsIgnoreCase( "color" ))
         return (String)getAttribute( "color" );
      else
         return super.getParamValue( param );
   }

   public void setParamValue( String param, String value ) {
      if (param.equalsIgnoreCase( "text" ))
         setText( value );
      else if (param.equalsIgnoreCase( "font" ))
         setFont( value );
      else if (param.equalsIgnoreCase( "size" ))
         setSize( Integer.parseInt( value ) );
      else if (param.equalsIgnoreCase( "color" ))
         setColor( Integer.decode( value ) );
      else
         super.setParamValue( param, value );
   }

   public void setText( String textString ) {
      addAttribute( "text", textString );
      text.setText( textString );
      if (textString == null || textString.length() == 0)
         setVisible( false );
      else {
         setVisible( true );
         setBounds( text.getBounds() );
      }
   }

   public void setFont( String font ) {
      addAttribute( "font", font );
      text.setFont( new Font( font, Font.PLAIN, text.getFont().getSize() ) );
      setBounds( text.getBounds() );
   }

   public void setSize( int size ) {
      addAttribute( "size", Integer.toString( size ) );
      text.setFont( text.getFont().deriveFont( (float)size ) );
      setBounds( text.getBounds() );
   }

   public void setColor( int color ) {
      addAttribute( "color", String.format( "0x%06X", color ) );
      text.setTextPaint( new Color( color ) );
   }

   /**
    * Sets the bounds of the object.  When the object is resized, the
    * text's font size is adjusted to fit within the new bounds.
    */
   public boolean setBounds( double x, double y, double width,
         double height ) {
      if (!super.setBounds( x, y, width, height ))
         return false;  // bounds haven't changed

      double factor = Math.min( width / text.getWidth(),
            height / text.getHeight() );
      int fontSize = (int)Math.max( text.getFont().getSize() * factor, 1.0 );

      text.setBounds( x, y, width, height );
      text.setFont( text.getFont().deriveFont( (float)fontSize ) );
      addAttribute( "size", Integer.toString( fontSize ) );

      return true;
   }

}  /* end RDSStaticText class */
