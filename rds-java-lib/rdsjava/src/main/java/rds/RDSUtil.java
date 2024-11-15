/*
 * RDSUtil.java
 * 
 * (c) 2007--2011 Numina Group, Inc.
 */

package rds;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;


/**
 * Utility class for RDS applications, web pages, etc.
 */
public final class RDSUtil {

   /** A shade of the color red associated with the Numina Group */
   public static final Color NUMINA_RED = new Color( 181, 10, 0 );

   // suppress default constructor to enforce non-instantiability
   private RDSUtil() {}


   /*
    * --- Tracing ---
    */

   /**
    * Posts a formatted trace message at the normal-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void trace( String format, Object... args ) {
      RDSLog.trace( format, args );
   }

   /**
    * Posts a formatted trace message at the normal-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    * @deprecated  as of August, 2009,
    *          replaced by {@code trace( String format, Object args... )}
    */
   @Deprecated
   public static void Trace( String format, Object... args ) {
      trace( format, args );
   }

   /**
    * Posts a formatted trace message at the high-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void alert( String format, Object... args ) {
      RDSLog.alert( format, args );
   }

   /**
    * Posts a formatted trace message at the high-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    * @deprecated  as of August, 2009,
    *          replaced by {@code alert( String format, Object args... )}
    */
   @Deprecated
   public static void Alert( String format, Object... args ) {
      alert( format, args );
   }

   /**
    * Posts an alert message associated with a thrown exception or error.
    * 
    * @param   t  the {@code Throwable} (typically an {@code Exception}
    *          or {@code Error}) to alert
    */
   public static void alert( Throwable t ) {
      alert( "%s", t.toString() );
   }

   /**
    * Posts an alert message associated with a thrown exception or error.
    * 
    * @param   t  the {@code Throwable} (typically an {@code Exception}
    *          or {@code Error}) to alert
    * @deprecated  as of August, 2009,
    *          replaced by {@code alert( Throwable t )}
    */
   @Deprecated
   public static void Alert( Throwable t ) {
      alert( t );
   }

   /**
    * Posts a formatted trace message at the low-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void inform( String format, Object... args ) {
      RDSLog.inform( format, args );
   }

   /**
    * Posts a formatted trace message at the low-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    * @deprecated  as of August, 2009,
    *          replaced by {@code inform( String format, Object args... )}
    */
   @Deprecated
   public static void Inform( String format, Object... args ) {
      inform( format, args );
   }


   /*
    * --- General utility functions ---
    */

   /** Sleep for the specified duration (msec). */
   public static boolean sleep( long duration ) {
      boolean success = true;

      try {
         Thread.sleep( duration );
      } catch (InterruptedException ex) {
         success = false;
         Thread.currentThread().interrupt();
      }

      return success;
   }

   /**
    * Converts a string to a decimal integer.  If the string is not parseable
    * as an integer, the specified alternative is returned.
    */
   public static int stringToInt( String strValue, int otherwise ) {
      return stringToInt( strValue, 10, otherwise );
   }

   /**
    * Converts a string to an integer.  If the string is not parseable as
    * an integer, the specified alternative is returned.
    */
   public static int stringToInt( String strValue, int radix, int otherwise ) {
      if (strValue == null || strValue.isEmpty())
         return otherwise;

      int intValue = otherwise;

      try {
         intValue = Integer.parseInt( strValue, radix );
      } catch (NumberFormatException ex) {}

      return intValue;
   }

   /**
    * Converts a string to a double.  If the string is not parseable as
    * a double, the specified alternative is returned.
    */
   public static double stringToDouble( String strValue, double otherwise ) {
      if (strValue == null || strValue.isEmpty())
         return otherwise;

      double doubleValue = otherwise;

      try {
         doubleValue = Double.parseDouble( strValue );
      } catch (NumberFormatException ex) {}

      return doubleValue;
   }

   /**
    * Generates a string containing the list of objects separated by
    * the specified separator string.
    * 
    * @param   sep  the {@code String} that separates consecutive elements
    * @param   obj  the elements to separate
    * @return  a {@code String} containing the separated list of elements
    */
   public static String separate( String sep, Object... obj ) {
      if (obj == null)
         return "null";
      if (obj.length == 0)
         return "";

      String result = (obj[0] == null) ? "null" : obj[0].toString();
      for (int i = 1, n = obj.length; i < n; i++) {
         result += sep;
         result += (obj[i] == null) ? "null" : obj[i].toString();
      }

      return result;
   }

   /**
    * Determines the mathematical median of a collection of numeric values.
    */
   public static <T extends Number & Comparable<? super T>> double
         median( List<T> valueList ) {
      if (valueList == null || valueList.isEmpty())
         return 0.0;

      Collections.sort( valueList );

      int n = valueList.size();
      if (n % 2 == 1)
         return valueList.get( (n + 1) / 2 - 1 ).doubleValue();

      double lower = ((Number)valueList.get( n / 2 - 1 )).doubleValue();
      double upper = ((Number)valueList.get( n / 2 )).doubleValue();

      return (lower + upper) / 2.0;
   }

   /**
    * Determines the extension of a file, i.e. the portion of the filename
    * following the last occurrence of the period character.
    * 
    * @param   file  the {@code File} for which the extension should
    *          be obtained
    * @return  a {@code String} containing the file extension, or
    *          {@code null} if the filename does not contain an extension
    */
   public static String getExtension( java.io.File file ) {
       String ext = null;
       String filename = file.getName();
       int i = filename.lastIndexOf( '.' );

       if (i > 0 &&  i < filename.length() - 1)
           ext = filename.substring( i + 1 ).toLowerCase();

       return ext;
   }


   /**
    * Creates an {@code ImageIcon} from an image file; the path to the
    * image file is based on the location of the specified parent object.
    * 
    * @param   parent  object from which to base the resourch search
    * @param   path    the name of the image file
    * @return  the {@code ImageIcon} corresponding to the specified image
    *          file or {@code null} if the path is invalid
    */
   public static ImageIcon createImageIcon(
         Object parent, String path ) {
      return createImageIcon( parent.getClass(), path );
   }

   /**
    * Creates an {@code ImageIcon} from an image file; the path to the
    * image file is based on the location of the specified class.
    * 
    * @param   parentClass  class from which to base the resource search
    * @param   path         the name of the image file
    * @return  the {@code ImageIcon} corresponding to the specified image
    *          file or {@code null} if the path is invalid
    */
   public static ImageIcon createImageIcon(
         Class<?> parentClass, String path ) {
      URL imgURL = parentClass.getResource( path );
      if (imgURL == null) {
         alert( "unable to locate [%s]", path );
         return null;
      }

      return new ImageIcon( imgURL );
   }

   /**
    * Creates an {@code ImageIcon} from an image file that is scaled in
    * both dimesions by the specified factor; the path to the image file
    * is based on the location of the specified parent class.
    * 
    * @param   parentClass  class from which to base the resource search
    * @param   path         the name of the image file
    * @param   scaleFactor  the scaling factor
    * @return  the {@code ImageIcon} corresponding to the specified image
    *          file or {@code null} if the path is invalid
    */
   public static ImageIcon createScaledImageIcon(
         Class<?> parentClass, String path, double scaleFactor ) {
      URL imgURL = parentClass.getResource( path );
      if (imgURL == null) {
         alert( "unable to locate [%s]", path );
         return null;
      }

      BufferedImage origImage;
      try {
         origImage = javax.imageio.ImageIO.read( imgURL );
      } catch (IOException ex) {
         alert( "unable to read image from [%s]", imgURL.toString() );
         return null;
      }

      int w = (int)(origImage.getWidth() * scaleFactor);
      int h = (int)(origImage.getHeight() * scaleFactor);

      BufferedImage scaledImage;
      if (scaleFactor < 1.0)
         scaledImage = scaleImage( origImage, w, h,
               RenderingHints.VALUE_INTERPOLATION_BILINEAR, true );
      else
         scaledImage = scaleImage( origImage, w, h,
               RenderingHints.VALUE_INTERPOLATION_BICUBIC, false );

      return new ImageIcon( scaledImage );
   }


   /**
    * Creates a scaled instance of the provided {@code BufferedImage}.
    *
    * @param   img            the original image to be scaled
    * @param   targetWidth    the desired width of the scaled instance,
    *          in pixels
    * @param   targetHeight   the desired height of the scaled instance,
    *          in pixels
    * @param   hint           one of the rendering hints that corresponds to
    *          {@code RenderingHints.KEY_INTERPOLATION} (e.g.
    *          {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
    *          {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
    *          {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
    * @param   higherQuality  if {@code true}, this method will use a
    *          multi-step scaling technique that provides higher quality
    *          than the usual one-step technique (only useful in downscaling
    *          cases and generally only when the {@code BILINEAR} hint is
    *          specified)
    * @return  a scaled version of the original {@code BufferedImage}
    */
   public static BufferedImage scaleImage(BufferedImage img,
         int targetWidth, int targetHeight, Object hint,
         boolean higherQuality) {
      int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
      BufferedImage ret = img;
      int w, h;

      if (higherQuality) {
         // multi-step technique: start with original size, then scale
         // down in multiple passes until the target size is reached
         w = img.getWidth();
         h = img.getHeight();
      } else {
         // one-step technique: scale directly from original
         // size to target size
         w = targetWidth;
         h = targetHeight;
      }

      do {
         if (higherQuality && w > targetWidth) {
            w /= 2;
            if (w < targetWidth)
               w = targetWidth;
         }

         if (higherQuality && h > targetHeight) {
            h /= 2;
            if (h < targetHeight)
               h = targetHeight;
         }

         BufferedImage tmp = new BufferedImage( w, h, type );
         Graphics2D g2 = tmp.createGraphics();
         g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, hint );
         g2.drawImage( ret, 0, 0, w, h, null );
         g2.dispose();

         ret = tmp;
      } while (w != targetWidth || h != targetHeight);

      return ret;
   }


   /**
    * Recursively enables or disables a container and all of its
    * children.
    * 
    * @param   c        the parent container
    * @param   enabled  the value of the enabled state to set
    */
   public static void setEnabledRecursive( Container c, boolean enabled ) {
      c.setEnabled( enabled );
      for( Component child : c.getComponents() ) {
         if (child instanceof Container)
            setEnabledRecursive( (Container)child, enabled );
         else
            child.setEnabled( enabled );
      }
   }


   /**
    * Converts binary text into a new {@code String} consisting of the
    * hexadecimal values of the input characters. The output string,
    * while twice the length of the input, consists entirely of
    * printable characters, and is thus "ASCII-armored" for transmission.
    * 
    * @param   bytes   the binary input
    * @return  the hex-coded output string
    */
   static public String toBinHex( byte... bytes ) {
      final char[] HEXCODES = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

      StringBuffer out = new StringBuffer();

      for (int i = 0, n = bytes.length; i < n; i++) {
         out.append( HEXCODES[ (bytes[i] >> 4) & 0x0F ] );  // hi
         out.append( HEXCODES[ bytes[i] & 0x0F ] );         // lo
      }

      return out.toString();
   }

   /**
    * Converts a {@code String} of pairs of hexadecimal values into a
    * new {@code String} consisting of the corresponding binary characters. 
    * 
    * @param   in  the hex-coded input string
    * @return  the decoded output string
    * @throws  NumberFormatException  if a non-hexadecimal (i.e. 0-9, A-Z)
    *          value is encountered in the input
    */
   static public String fromBinHex( String in )
         throws NumberFormatException {
      StringBuffer out = new StringBuffer();
      char ch = (char)0;

      for (int i = 0, n = in.length(); i < n; i++) {
         int val = Character.digit( in.charAt( i ), 16 );
         if (val < 0)
            throw new NumberFormatException(
                  "invalid hex character [" + in.charAt( i ) + "]" );

         if (i % 2 == 0)  // hi
            ch = (char)(val << 4);
         else {           // lo
            ch |= (char)val;
            out.append( ch );
            ch = (char)0;
         }
      }

      return out.toString();
   }

}  // end RDSUtil class
