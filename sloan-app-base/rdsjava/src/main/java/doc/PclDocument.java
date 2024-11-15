package doc;


import static rds.CommonAscii.ESC;


public final class PclDocument {
   private static final int DPI = 300;

   private final byte[] data;


   public enum PrintMode {
      SIMPLEX      (0),
      DUPLEX       (1),
      DUPLEX_SHORT (2);
      private final int value;
      PrintMode( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public enum Orientation {
      PORTRAIT           (0),  // default
      LANDSCAPE          (1),
      REVERSE_PORTRAIT   (2),
      REVERSE_LANDSCAPE  (3);
      private final int value;
      Orientation( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public enum Spacing {
      FIXED         (0),
      PROPORTIONAL  (1);
      private final int value;
      Spacing( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public enum Stroke {
      LIGHT   (-3),
      MEDIUM  (0),   // default
      BOLD    (3);
      private final int value;
      Stroke( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public enum Style {
      NORMAL            (0),    // default
      ITALIC            (1),
      CONDENSED         (4),
      CONDENSED_ITALIC  (5),
      COMPRESSED        (8),
      EXPANDED          (24),
      OUTLINE           (32),
      INLINE            (64),
      SHADOWED          (128),
      OUTLINE_SHADOWED  (160);
      private final int value;
      Style( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public enum Typeface {
      DEFAULT        (16602),
      DEFAULT_FIXED  (4102),
      HELVETICA      (24580),
      COURIER        (4099),
      LETTER_GOTHIC  (4102);
      private final int value;
      Typeface( int value ) { this.value = value; }
      public int value() { return value; }
   }

   public static final String[] CODE128_PATTERNS = {
         "212222", // 00
         "222122", // 01
         "222221", // 02 
         "121223", // 03
         "121322", // 04
         "131222", // 05
         "122213", // 06
         "122312", // 07
         "132212", // 08
         "221213", // 09
         "221312", // 10
         "231212", // 11
         "112232", // 12
         "122132", // 13
         "122231", // 14
         "113222", // 15
         "123122", // 16
         "123221", // 17
         "223211", // 18
         "221132", // 19
         "221231", // 20
         "213212", // 21
         "223112", // 22
         "312131", // 23
         "311222", // 24
         "321122", // 25
         "321221", // 26
         "312212", // 27
         "322112", // 28
         "322211", // 29
         "212123", // 30
         "212321", // 31
         "232121", // 32
         "111323", // 33
         "131123", // 34
         "131321", // 35
         "112313", // 36
         "132113", // 37
         "132311", // 38
         "211313", // 39
         "231113", // 40
         "231311", // 41
         "112133", // 42
         "112331", // 43
         "132131", // 44
         "113123", // 45
         "113321", // 46
         "133121", // 47
         "313121", // 48
         "211331", // 49
         "231131", // 50
         "213113", // 51
         "213311", // 52
         "213131", // 53
         "311123", // 54
         "311321", // 55
         "331121", // 56
         "312113", // 57
         "312311", // 58
         "332111", // 59
         "314111", // 60
         "221411", // 61
         "431111", // 62
         "111224", // 63
         "111422", // 64
         "121124", // 65
         "121421", // 66
         "141122", // 67
         "141221", // 68
         "112214", // 69
         "112412", // 70
         "122114", // 71
         "122411", // 72
         "142112", // 73
         "142211", // 74
         "241211", // 75
         "221114", // 76
         "413111", // 77
         "241112", // 78
         "134111", // 79
         "111242", // 80
         "121142", // 81
         "121241", // 82
         "114212", // 83
         "124112", // 84
         "124211", // 85
         "411212", // 86
         "421112", // 87
         "421211", // 88
         "212141", // 89
         "214121", // 90
         "412121", // 91
         "111143", // 92
         "111341", // 93
         "131141", // 94
         "114113", // 95
         "114311", // 96
         "411113", // 97
         "411311", // 98
         "113141", // 99
         "114131", //100
         "311141", //101
         "411131", //102
         "211412", //103
         "211214", //104
         "211232", //105
         "233111"  //106
   };


   public PclDocument() {
      this( new byte[0] );
   }

   private PclDocument( byte[] data ) {
      this.data = data;
   }


   private PclDocument append( String data ) {
      return appendBytes( data.getBytes() );
   }

/*
   private PclDocument append( String format, Object... args ) {
      return append( String.format( format, args ) );
   }
*/

   public PclDocument appendBytes( byte[] data ) {
      if (data == null || data.length == 0)
         return this;

      byte newData[] = new byte[ this.data.length + data.length ];
      System.arraycopy( this.data, 0, newData, 0, this.data.length );
      System.arraycopy( data, 0, newData, this.data.length, data.length );
      return new PclDocument( newData );
   }

   private PclDocument appendCommand( String cmd ) {
      return append( "" + (char)ESC + cmd );
   }

   private PclDocument appendCommand( String format, Object... args ) {
      return append( "" + (char)ESC + String.format( format, args ) );
   }

   // TODO  only code 128 subset B currently supported
   public PclDocument barcode( int dy, int t, 
         String format, Object... args ) {
      PclDocument doc = this;
      String text = String.format( format, args );

      int start = 104;  // start code B
      int stop = 106;   // stop code

      doc = doc.barcodeSymbolCode128B( dy, t, start );
      int sum = start;
      for (int i = 0; i < text.length(); i++) {
         int idx = text.charAt( i ) - ' ';
         doc = doc.barcodeSymbolCode128B( dy, t, idx );
         sum += idx * (i + 1);
      }
      doc = doc.barcodeSymbolCode128B( dy, t, sum % 103 );
      doc = doc.barcodeSymbolCode128B( dy, t, stop );
      doc = doc.barcodeSymbol( dy, t, "2" );  // stop bar

      return doc;
   }

   private PclDocument barcodeBar( int dx, int dy, int t ) {
      int w = Math.max( dx * t, 0 );
      return this
            .appendCommand( "*c%da%db%dP", w, dim( dy ), 0 )
            .appendCommand( "*p+%dX", w );
   }

   private PclDocument barcodeSpace( int dx, int dy, int t ) {
      int w = Math.max( dx * t, 0 );
      return this
            .appendCommand( "*p+%dX", w );
   }

   public PclDocument barcodeSymbol( int dy, int t, String symbolPattern ) {
      PclDocument doc = this;

      for (int i = 0, n = symbolPattern.length(); i < n; i++) {
         int dx = symbolPattern.charAt( i ) - '0';
         if (i % 2 == 0)
            doc = doc.barcodeBar( dx, dy, t );
         else
            doc = doc.barcodeSpace( dx, dy, t );
      }

      return doc;
   }

   public PclDocument barcodeSymbolCode128B( int dy, int t, int idx ) {
      if (idx < 0 || idx >= CODE128_PATTERNS.length)
         throw new IllegalArgumentException( "Invalid code index" );

      String symbolPattern = CODE128_PATTERNS[ idx ];
      return this
            .barcodeSymbol( dy, t, symbolPattern );
   }

   public PclDocument box( int dx, int dy, int t ) {
      return this
            .rule( dx, t )
            .rule( t, dy )
            .moveHorizontal( dx - t )
            .rule( t, dy )
            .move( -(dx - t), dy - t )
            .rule( dx, t );
   }

   public byte[] bytes() {
      return data.clone();
   }

   /**
    * Converts the specified dimension, in hundredths of an inch, to
    * native units via the dots-per-inch setting.
    */
   private int dim( int x ) {
      return x * DPI / 100;
   }

   public PclDocument done() {
      return this
            .append( "" + (char)rds.CommonAscii.FF )  // form feed
            .appendCommand( "E" )          // printer reset, 4-2
            .appendCommand( "%-12345X" );  // universal exit language, 4-3
   }

   public PclDocument font( Spacing p, Typeface t ) {
      return this
            .appendCommand( "(%dU", 0 )  // symbol set, 8-6
            .appendCommand( "(s%dp%dT",
                  p.value(),    // spacing, 8-9
                  t.value() );  // typeface family, 8-18
   }

   public PclDocument font( Spacing p, Typeface t, Style s, Stroke b ) {
      return this
            .appendCommand( "(%dU", 0 )  // symbol set, 8-6
            .appendCommand( "(s%dp%ds%db%dT",
                  p.value(),    // spacing, 8-9
                  s.value(),    // style, 8-14
                  b.value(),    // stroke weight, 8-16
                  t.value() );  // typeface family, 8-18
   }

   public PclDocument fontInit() {
      return font( Spacing.PROPORTIONAL, Typeface.DEFAULT, Style.NORMAL,
            Stroke.MEDIUM );
   }

   public PclDocument fontSize( int size ) {
      return fontSize( Spacing.PROPORTIONAL, size );
   }

   // TODO  document size units and conversion logic; use dim() function?
   public PclDocument fontSize( Spacing p, int size ) {
      if (size <= 0)
         throw new IllegalArgumentException( "Invalid font size" );

      if (p == Spacing.FIXED) {
         float pitch = 100 / (float)size;
         return appendCommand( "(s%.2fH", pitch );
      }

      float height = (float)size * 72 / 100;
      return appendCommand( "(s%.2fV", height );
   }

   public PclDocument fontStroke( Stroke b ) {
      return appendCommand( "(s%dB", b.value() );
   }

   public PclDocument fontStyle( Style s ) {
      return appendCommand( "(s%dS", s.value() );
   }

   public PclDocument init() {
      return init( 0, 0, PrintMode.SIMPLEX );
   }

   public PclDocument init( int left, int top, PrintMode m ) {
      // convert offsets to decipoints (1/720 inch)
      int l = left * 720 / 100;
      int t = top * 720 / 100;
      return this
            .appendCommand( "%-12345X" )    // universal exit language, 4-3
            .appendCommand( "E" )           // printer reset, 4-2
            .appendCommand( "&u%dD", DPI )  // unit of measure, 4-13
            .appendCommand( "&l%dU", l )    // left offset registration, 4-7
            .appendCommand( "&l%dZ", t )    // top offset registration, 4-8
            .appendCommand( "&l%dS", m.value() );  // simplex/duplex print, 4-5
   }

   public boolean isEmpty() {
      return data.length == 0;
   }

   /**
    * Moves the cursor, relative to the current cursor position.
    * <p>
    * Ref: horizontal cursor positioning (PCL units), 6-7;
    *      vertical cursor positioning (PCL units), 6-12
    */
   public PclDocument move( int dx, int dy ) {
      String cmd = "*p";

      if (dx >= 0)
         cmd += String.format( "+%dx", dim( dx ) );
      else
         cmd += String.format( "%dx", dim( dx ) );

      if (dy >= 0)
         cmd += String.format( "+%dY", dim( dy ) );
      else
         cmd += String.format( "%dY", dim( dy ) );

      return appendCommand( cmd );
   }

   /**
    * Moves the cursor horizontally, relative to the current cursor position.
    * <p>
    * Ref: horizontal cursor positioning (PCL units), 6-7
    */
   public PclDocument moveHorizontal( int dx ) {
      if (dx >= 0)
         return appendCommand( "*p+%dX", dim( dx ) );
      return appendCommand( "*p%dX", dim( dx ) );
   }

   /**
    * Moves the cursor to the specified position.
    * <p>
    * Ref: horizontal cursor positioning (PCL units), 6-7;
    *      vertical cursor positioning (PCL units), 6-12
    */
   public PclDocument moveTo( int x, int y ) {
      if (x < 0 || y < 0)
         throw new IllegalArgumentException(
               String.format( "Invalid position: (%d,%d)", x, y ) );
      return appendCommand( "*p%dx%dY", dim( x ), dim( y ) );
   }

   /**
    * Moves the cursor to the specified horizontal position.
    * <p>
    * Ref: horizontal cursor positioning (PCL units), 6-7
    */
   public PclDocument moveToHorizontal( int x ) {
      if (x < 0)
         throw new IllegalArgumentException(
               String.format( "Invalid x position: %d", x ) );
      return appendCommand( "*p%dX", dim( x ) );
   }

   /**
    * Moves the cursor to the specified vertical position.
    * <p>
    * Ref: vertical cursor positioning (PCL units), 6-12
    */
   public PclDocument moveToVertical( int y ) {
      if (y < 0)
         throw new IllegalArgumentException(
               String.format( "Invalid y position: %d", y ) );
      return appendCommand( "*p%dY", dim( y ) );
   }

   /**
    * Moves the cursor vertically, relative to the current cursor position.
    * Ref: vertical cursor positioning (PCL units), 6-12
    */
   public PclDocument moveVertical( int dy ) {
      if (dy >= 0)
         return appendCommand( "*p+%dY", dim( dy ) );
      return appendCommand( "*p%dY", dim( dy ) );
   }

   /**
    * Starts a new page by appending a form-feed character.
    */
   public PclDocument newPage() {
      return this
            .append( "" + (char)rds.CommonAscii.FF );  // form feed
   }

   /**
    * Orients the page.
    * <p>
    * Ref: logical page orientation, 5-5
    */
   public PclDocument orient( Orientation o ) {
      return appendCommand( "&l%dO", o.value() );
   }

   /**
    * Restores the current cursor position.
    * <p>
    * Ref: push/pop cursor position, 6-15
    */
   public PclDocument popCursor() {
      return appendCommand( "&f%dS", 1 );
   }

   /**
    * Saves the current cursor position.
    * <p>
    * Ref: push/pop cursor position, 6-15
    */
   public PclDocument pushCursor() {
      return appendCommand( "&f%dS", 0 );
   }

   /**
    * Draws a rule (filled black rectangle).
    * <p>
    * Ref: horizontal rectangle size (PCL units), 14-3;
    *      vertical rectangle size (PCL units), 14-4;
    *      fill rectangular area, 14-9
    */
   public PclDocument rule( int dx, int dy ) {
      return appendCommand( "*c%da%db%dP", dim( dx ), dim( dy ), 0 );
   }

   /**
    * Adds text to the document at the current position.
    */
   public PclDocument text( String format, Object... args ) {
      return append( String.format( format, args ) );
   }

   /**
    * Adds text to the document, centered at the current position.  Note that
    * the font size is required in order to move half the total width before
    * printing.  This method assumes a proportional font.
    */
   public PclDocument textCenter( int size, String format, Object... args ) {
      return textCenter( Spacing.PROPORTIONAL, size, format, args );
   }

   /**
    * Adds text to the document, centered at the current position.  Note that
    * the font size and spacing are required in order to move half the total
    * width before printing.
    * <p>
    * Ref: pattern transparency mode, 13-7;
    *      select current pattern, 13-12;
    *      print direction, 5-9
    */
   public PclDocument textCenter( Spacing p, int size,
         String format, Object... args ) {
      String text = String.format( format, args );
      return this
            .appendCommand( "*v%do%dT", 0, 1 )  // transparent, solid white
            .appendCommand( "&a%dP", 180 )      // 180 degree rotation
            .fontSize( p, size / 2 )            // half-size
            .append( text )
            .appendCommand( "*v%do%dT", 1, 0 )  // opaque, solid black
            .appendCommand( "&a%dP", 0 )        // 0 degree rotation
            .fontSize( p, size )                // full-size
            .append( text );
   }

   /**
    * Adds text to the document, right-justified at the current position.
    * <p>
    * Ref: pattern transparency mode, 13-7;
    *      select current pattern, 13-12;
    *      print direction, 5-9
    */
   public PclDocument textRight( String format, Object... args ) {
      String text = String.format( format, args );
      return this
            .appendCommand( "*v%do%dT", 0, 1 )  // transparent, solid white
            .appendCommand( "&a%dP", 180 )      // 180 degree rotation
            .append( text )
            .appendCommand( "*v%do%dT", 1, 0 )  // opaque, solid black
            .appendCommand( "&a%dP", 0 )        // 0 degree rotation
            .append( text );
   }

   /**
    * Adds text to the document at the current position, reverse-printed
    * (solid white, assumes printing on a solid black background).
    * <p>
    * Ref: select current pattern, 13-12
    */
   public PclDocument textReverse( String format, Object ... args ) {
      String text = String.format( format, args );
      return this
            .appendCommand( "*v%dT", 1 )  // solid white
            .append( text )
            .appendCommand( "*v%dT", 0 ); // solid black (return to default)
   }

   /**
    * Returns a string representation of the document, namely the 
    * PCL data string that may be used to render it.
    */
   public String toString() {
      return new String( data );
   }


   public static void main( String... args ) {
      String s = new PclDocument()
            .init( 30, 0, PrintMode.SIMPLEX )
            .moveTo( 0, 0 )
            .box( 750, 1000, 1 )
            .moveTo( 250, 0 )
            .fontInit()
            .fontSize( 18 )
            .text( "Big Test" )
            .done()
            .toString();
      System.out.println( s );
   }

}
