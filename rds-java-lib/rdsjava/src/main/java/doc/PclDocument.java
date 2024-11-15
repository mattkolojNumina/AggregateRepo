package doc;


import static rds.CommonAscii.ESC;


public final class PclDocument {

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


   public PclDocument() {
      this( new byte[0] );
   }

   private PclDocument( byte[] data ) {
      this.data = data;
   }


   private PclDocument append( String data ) {
      return appendBytes( data.getBytes() );
   }

   private PclDocument append( String format, Object... args ) {
      return append( String.format( format, args ) );
   }

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
            .appendCommand( "&u%dD", 300 )  // unit of measure, 4-13
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
         cmd += String.format( "+%dx", 3 * dx );
      else
         cmd += String.format( "%dx", 3 * dx );

      if (dy >= 0)
         cmd += String.format( "+%dY", 3 * dy );
      else
         cmd += String.format( "%dY", 3 * dy );

      return appendCommand( cmd );
   }

   /**
    * Moves the cursor horizontally, relative to the current cursor position.
    * <p>
    * Ref: horizontal cursor positioning (PCL units), 6-7
    */
   public PclDocument moveHorizontal( int dx ) {
      if (dx >= 0)
         return appendCommand( "*p+%dX", 3 * dx );
      return appendCommand( "*p%dX", 3 * dx );
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
      return appendCommand( "*p%dx%dY", 3 * x, 3 * y );
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
      return appendCommand( "*p%dX", 3 * x );
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
      return appendCommand( "*p%dY", 3 * y );
   }

   /**
    * Moves the cursor vertically, relative to the current cursor position.
    * Ref: vertical cursor positioning (PCL units), 6-12
    */
   public PclDocument moveVertical( int dy ) {
      if (dy >= 0)
         return appendCommand( "*p+%dY", 3 * dy );
      return appendCommand( "*p%dY", 3 * dy );
   }

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
      return appendCommand( "*c%da%db%dP", 3 * dx, 3 * dy, 0 );
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
