package doc;


public final class ZplDocument {
   private static final int DEFAULT_DPI = 203;
   private static final String DEFAULT_FONT = "0";
   private static final int DEFAULT_FONT_SIZE = 15;

   private final int dpi;

   private final byte[] data;


   public enum Justification {
      LEFT       ("L"),
      CENTER     ("C"),
      RIGHT      ("R"),
      JUSTIFIED  ("J");
      private final String value;
      Justification( String value ) { this.value = value; }
      public String value() { return value; }
   }


   public ZplDocument() {
      this( new byte[0], DEFAULT_DPI );
   }

   public ZplDocument( int dpi ) {
      this( new byte[0], dpi );
   }

   private ZplDocument( byte[] data, int dpi ) {
      this.data = data;
      this.dpi = dpi;
   }


   private ZplDocument append( String data ) {
      return appendBytes( data.getBytes() );
   }

   private ZplDocument append( String format, Object... args ) {
      return append( String.format( format, args ) );
   }

   public ZplDocument appendBytes( byte[] data ) {
      byte newData[] = new byte[ this.data.length + data.length ];
      System.arraycopy( this.data, 0, newData, 0, this.data.length );
      System.arraycopy( data, 0, newData, this.data.length, data.length );
      return new ZplDocument( newData, dpi );
   }

   public ZplDocument barcode( int h, String format, Object... args ) {
      return this
            .append( "^BCN,%d,,,A", dim( h ) )
            .text( format, args );
   }

   public ZplDocument barcodeWidth( int mils ) {
      int w = Math.round( dim( mils ) / 10.0f );
      return this
            .append( "^BY%d", w );
   }

   public ZplDocument box( int dx, int dy, int t ) {
      if (dx < 0 || dy < 0 || t < 0)
         throw new IllegalArgumentException( String.format(
               "Invalid box arguments: (%d,%d,%d)", dx, dy, t ) );
      int dxDim = dim( dx );
      int dyDim = dim( dy );
      int tDim = dim( t );
      if (tDim == 0)
         tDim = 1;

      return this
            .append( "^GB%d,%d,%d^FS", dxDim, dyDim, tDim )
            .newline();
   }

   public byte[] bytes() {
      return data.clone();
   }

   /**
    * Converts the specified dimension, in hundredths of an inch, to
    * native units via the dots-per-inch setting.
    */
   private int dim( int x ) {
      return x * dpi / 100;
   }

   public ZplDocument done() {
      return this
            .append( "^XZ" )
            .newline();
   }

   public ZplDocument font( String fontName, int h, int w ) {
      return this
            .append( "^CF%s,%d,%d", fontName, dim( h ), dim( w ) );
   }

   public ZplDocument fontInit() {
      return font( DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE );
   }

   public ZplDocument fontSize( int h, int w ) {
      return font( "", h, w );
   }

   public ZplDocument init() {
      return init( 0, 0 );
   }

   public ZplDocument init( int left, int top ) {
      return this
            .append( "^XA^LH%d,%d^FS", dim( left ), dim( top ) )
            .newline();
   }

   public boolean isEmpty() {
      return data.length == 0;
   }

   /**
    * Moves the cursor to the specified position.
    */
   public ZplDocument moveTo( int x, int y ) {
      if (x < 0 || y < 0)
         throw new IllegalArgumentException(
               String.format( "Invalid position: (%d,%d)", x, y ) );
      return append( "^FO%d,%d", dim( x ), dim( y ) );
   }

   public ZplDocument newline() {
      return append( "\n" );
   }

   /**
    * Draws a rule (filled black rectangle).
    */
   public ZplDocument rule( int dx, int dy ) {
      return box( dx, 0, dy );
   }

   /**
    * Adds text to the document at the current position.
    */
   public ZplDocument text( String format, Object... args ) {
      String text = String.format( format, args );
      return this
            .append( "^FD" + text + "^FS" )
            .newline();
   }

   public ZplDocument textBox( int dx, int numLines, Justification j,
         String format, Object... args ) {
      return this
            .append( "^FB%d,%d,,%s", dx, numLines, j.value() )
            .text( format, args );
   }

   /**
    * Returns a string representation of the document, namely the 
    * PCL data string that may be used to render it.
    */
   public String toString() {
      return new String( data );
   }


   public static void main( String... args ) {
      String s = new ZplDocument()
            .init()
            .fontInit()
            .moveTo( 100, 100 )
            .box( 200, 150, 0 )
            .moveTo( 200, 200 )
            .text( "Test" )
            .moveTo( 200, 300 )
            .text( "Test2" )
            .fontSize( 30, 30 )
            .moveTo( 200, 400 )
            .text( "Big Test" )
            .moveTo( 150, 500 )
            .barcodeWidth( 15 )
            .barcode( 100, "TEST12345678" )
            .done()
            .toString();
      System.out.println( s );
   }

}
