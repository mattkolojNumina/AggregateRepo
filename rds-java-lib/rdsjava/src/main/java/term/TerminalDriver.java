/*
 * TerminalDriver.java
 * 
 * Application to drive the "thin server" user-interface terminal.
 * 
 * (c) 2011-2012, Numina Group, Inc.
 */

package term;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;

import rds.CommonAscii;
import rds.RDSDatabase;
import static rds.RDSLog.*;
import rds.RDSUtil;
import rds.parse.ParseEndOnly;


/**
 * An application to communicate with the "thin server" WinTerminal for
 * user-interface screens.  The display definition and processing methods
 * for the screens are implemented in project-specific classes and
 * configured through the database.
 */
public class TerminalDriver {
   /** The default session ID. */
   private static final String DEFAULT_TERM_NAME = "term";

   /** The default RDS database hostname. */
   private static final String DEFAULT_RDS_DB = "db";

   /** The default connection port. */
   private static final int DEFAULT_PORT = 10000;

   /** The default polling interval (msec). */
   private static final int DEFAULT_POLL = 500;

   /** Pause time (msec) in case of error. */
   private static final long SLEEP_DURATION = 10 * 1000L;

   /** The maximum message length. */
   private static final int MSG_LEN = 4096;

   // class variables
   private String termName;
   private RDSDatabase db;
   private TreeMap<String,String> atoms;
   private int poll;
   private String startScreen;
   private String currentScreen;
   private String nextScreen;
   private ScreenHandler currentHandler;

   // connection variables
   private String host;
   private int port;
   private SocketChannel socket;
   private Selector selector;
   private SelectionKey inKey;

   // communications variables
   private ByteBuffer in;
   private ByteBuffer out;
   private CharBuffer buf;
   private ParseEndOnly parser;
   private CharsetEncoder encoder;


   public TerminalDriver( String termName, String dbHost ) {
      this.termName = termName;
      this.db = new RDSDatabase( dbHost );
      atoms = new TreeMap<String, String>();

      Map<String,String> paramMap = db.getControlMap( termName );
      if (paramMap == null || paramMap.isEmpty()) {
         alert( "unable to configure session [%s]", termName );
      } else {
         host = paramMap.get( "ip" );
         port = RDSUtil.stringToInt( paramMap.get( "port" ), DEFAULT_PORT );
         poll = RDSUtil.stringToInt( paramMap.get( "poll" ), DEFAULT_POLL );

         alert("host at init = [%s]", host);
         alert("port at init = [%d]", port);
         
         
         alert("poll at init = [%d]", poll);
         if(poll <=0)
            poll = 500;

         startScreen = paramMap.get( "startScreen" );
         trace( "connect to %s:%d, start at %s", host, port, startScreen );
      }

      in = ByteBuffer.allocateDirect( MSG_LEN );
      out = ByteBuffer.allocateDirect( MSG_LEN );
      buf = CharBuffer.allocate( MSG_LEN );
      parser = new ParseEndOnly( CommonAscii.CR );
      encoder = Charset.forName( "US-ASCII" ).newEncoder();
   }

   private boolean open() {
      if (host == null || host.isEmpty() || port <= 0) {
         alert( "invalid connection params [%s]:[%d]",
               host, port );
         return false;
      }

      try {
         alert("Trying to connect");
         alert(host);
         alert(String.valueOf(port));
         socket = SocketChannel.open( new InetSocketAddress( host, port ) );
         socket.configureBlocking( false );
         selector = Selector.open();
         inKey = socket.register( selector, SelectionKey.OP_READ );
      } catch (java.net.ConnectException ex) {
         alert(ex);
         alert("terminal not listening, fail silently and retry");
         close();
         return false;
      } catch (java.net.NoRouteToHostException ex) {
         // terminal machine not on network, fail silently and retry
         close();
         return false;
      } catch (IOException ex) {
         alert( "i/o exception during open" );
         alert( ex );
         close();
         return false;
      }
      trace( "connected!" );
      in.clear();
      currentScreen = null;
      setNextScreen( startScreen );

      return true;
   }

   private void close() {
      try {
         if (selector != null)
            selector.close();
         if (socket != null)
            socket.close();
      } catch (IOException ex) {
         alert( "i/o exception during close" );
         alert( ex );
      }

      selector = null;
      socket = null;
   }

   public void run() {
      TimerTask pollTask = new TimerTask() {
         public void run() {
            try {
               poll();
            } catch (Exception ex) {
               alert( "error while polling" );
               ex.printStackTrace();
               close();
               RDSUtil.sleep( SLEEP_DURATION );
            }
         }
      };
      if(poll <=0)
            poll = 500;
      alert("poll = [%d]", poll);
      (new Timer()).schedule( pollTask, 0, poll );
   }

   private void poll() {
      if (!testConnection()) {
         alert("test connection failed");
         RDSUtil.sleep( SLEEP_DURATION );
         alert("after sleep");
         return;
      }

      updateScreen();

      if (currentHandler != null)
         currentHandler.handleTick();

      try {
         if (!checkSelector())
            return;
         for (SelectionKey key : selector.selectedKeys()) {
            if (key == inKey && key.isReadable())
               dataIn();
         }
         selector.selectedKeys().clear();
      } catch (Exception ex) {
         alert( "i/o error during read from terminal" );
         alert( ex );
         close();
      }
   }

   private void updateScreen() {
      if (nextScreen == null || nextScreen.isEmpty() ||
            nextScreen.equals( currentScreen ))
         return;

      inform( "change to [%s]", nextScreen );
      if (currentHandler != null)
         currentHandler.handleExit();
      
      ScreenHandler nextHandler = loadScreen( nextScreen );
      if (nextHandler != null) {
         currentScreen = nextScreen;
         currentHandler = nextHandler;
         currentHandler.handleInit();
      }
   }

   private ScreenHandler loadScreen( String screen ) {
      ScreenHandler handler = null;

      try {
         @SuppressWarnings("unchecked")
         Class<ScreenHandler> cls = (Class<ScreenHandler>)Class.forName(
               screen );
         Constructor<ScreenHandler> ctor = cls.getConstructor(
               TerminalDriver.class );
         if (ctor != null)
            handler = ctor.newInstance( this );
      } catch (Exception ex) {
         alert( "failed to load screen handler for [%s]", screen );
         alert( ex );
         ex.printStackTrace();
      }

      return handler;
   }

   private boolean checkSelector()
         throws IOException {
      if (selector == null)
         return false;
      int n = selector.select( 1 );
      return (n > 0);
   }

   private boolean testConnection() {
      if (socket == null)
         return open();

      return true;
   }

   private void dataIn()
         throws IOException {
      if (socket == null)
         return;

      in.clear();
      socket.read( in );
      while (parser.parse( in )) {
//         inform( "recv [%s]", parser.getResult() );
         processMsg( parser.getResult() );
      }

   }

   private void processMsg( String msg ) {
      if (msg == null || msg.isEmpty())
         return;

      String cmd = msg.substring( 0, 1 );
      String[] msgData = msg.substring( 1 ).split( "\\|" );

      int tag = RDSUtil.stringToInt( msgData[0], 0 );
//      inform( "cmd = [%s], tag = %d, data len = %d",
//            cmd, tag, msgData.length );

      if (cmd.equals( "H" )) {         // heartbeat
         return;
      } else if (cmd.equals( "B" )) {  // button
         currentHandler.handleButton( tag );
      } else if (cmd.equals( "E" )) {  // entry field
         currentHandler.handleEntry( tag,
               (msgData.length > 1) ? msgData[1] : "" );
      } else if (cmd.equals( "F" )) {  // function key
         currentHandler.handleFunction( tag );
      } else if (cmd.equals( "P" )) {  // password entry field
         currentHandler.handleEntry( tag,
               (msgData.length > 1) ? msgData[1] : "" );
      } else if (cmd.equals( "S" )) {  // serial msg
         currentHandler.handleSerial( tag,
               (msgData.length > 1) ? RDSUtil.fromBinHex( msgData[1] ) : "" );
      } else if (cmd.equals( "M" )) {  // message
         currentHandler.handleMessage( msgData[0] );
      } else {
         alert( "unknown or improperly formatted message: [%s]", msg );
      }
   }

   private void sendMsg( String msg ) {
      if (socket == null)
         return;

      buf.clear();
      buf.put( msg, 0, Math.min( msg.length(), MSG_LEN - 2 ) );
      buf.put( "\r" );
      buf.flip();

      out.clear();
      encoder.encode( buf, out, true );
      out.flip();

      String msgSnippet = (msg.length() > 40) ?
            msg.substring( 0, 40 ) + "..." : msg;
      try {
         socket.write( out );
         //RDSUtil.inform( "sent [%s]", msgSnippet );
      } catch (IOException ex) {
         alert( "i/o error sending msg [%s]", msgSnippet );
         alert( ex );
         close();
      }
   }


   /*
    * screen methods
    */

   public String fetchAtom( String name, String otherwise ) {
      String atom = atoms.get( name );
      return (atom == null) ? otherwise : atom;
   }

   public void saveAtom( String name, String value ) {
      atoms.put( name, value );
   }

   public void dropAtom( String name ) {
      atoms.remove( name );
   }

   public void setNextScreen( String screen ) {
      nextScreen = screen;
   }

   /**
    * Clears the screen with a solid background color.
    * <p>
    * The color may be specified in one of three ways:
    * <li>as one of a few pre-defined color names (white, red, yellow,
    * green, blue, or gray)
    * <li>as a Delphi-defined color variable name (e.g. 'clBlue' or
    * 'clBtnFace')
    * <li>as a 24-bit hex value of the form {@code $00bbggrr} where
    * {@code bb}, {@code gg}, and {@code rr} are the blue, green, and
    * red values, respectively
    */
   public void clearScreen( String color ) {
      sendMsg( String.format( "C%s", color ) );
   }

   public void heartbeat() {
      sendMsg( "H" );
   }

   public void sendVoiceCommand( String textToSpeak ) {
      String msg = String.format( "V|%s", textToSpeak );
      sendMsg( msg );
   }
   
   public void setImage( int tag, String path, int left, int top, int alignX, int alignY, int width, int height,
         int scale ) {
      String msg = String.format( "I%d|%s|%d|%d|%d|%d|%d|%d|%d", tag, path, left, top, alignX, alignY,
            width, height, scale );
      sendMsg( msg );
   }

   public void setButton( int tag, int left, int top, int width, int height,
         int size, String caption, String color ) {
      String msg = String.format( "B%d|%d|%d|%d|%d|%d|%s", tag, left, top,
            width, height, size, caption );
      sendMsg( msg );
   }

   public void setEntry( int tag, int left, int top, int width, int height,
         int font ) {
      String msg = String.format( "E%d|%d|%d|%d|%d|%d", tag, left, top,
            width, height, font );
      sendMsg( msg );
   }

   public void setPassword( int tag, int left, int top, int width,
         int height, int font ) {
      String msg = String.format( "P%d|%d|%d|%d|%d|%d", tag, left, top,
            width, height, font );
      sendMsg( msg );
   }

   public void setFocus( int tag ) {
      String msg = String.format( "Y%d", tag );
      sendMsg( msg );
   }

   public void setSerial( int tag, int port, int baud, char parity, int data,
         int stop, int endchar ) {
      setSerial( tag, "COM" + port, baud, parity, data, stop, endchar );
   }
   public void setSerial( int tag, String dev, int baud, char parity, int data,
         int stop, int endchar ) {
      String msg = String.format( "S%d|%s|%d|%c|%d|%d|%d", tag, dev,
            baud, parity, data, stop, endchar );
      sendMsg( msg );
   }

   public void staticText( int tag, int left, int top, int size, String text ) {
      staticTextColor( tag, left, top, size, text, "clBlack" );
   }

   public void staticTextColor( int tag, int left, int top, int size,
         String text, String color ) {
      staticTextColorAlign( tag, left, top, size, text, color, 0 );
   }

   public void staticTextColorAlign( int tag, int left, int top, int size,
         String text, String color, int width ) {
      String msg = String.format( "T%d|%d|%d|%d|%s|%s|%d", tag, left, top, size,
            text, color, width );
      sendMsg( msg );
   }

   public void setRectangle( int tag, int left, int top, int width,
         int height, String color, int thick, String borderColor ) {
      String msg = String.format( "R%d|%d|%d|%d|%d|%s|%d|%s",
            tag, left, top, width, height, color, thick, borderColor );
      sendMsg( msg );
   }

   public void xmitSerial( int tag, String message ) {
      String data = RDSUtil.toBinHex( message.getBytes() );
      String outMsg = String.format( "X%d|%s", tag, data );
      sendMsg( outMsg );
   }

   public String getTermName() {
      return termName;
   }

   public RDSDatabase getDb() {
      return db;
   }

   public int getPoll(){
      return poll;
   }


   /*
    * --- main ---
    */

   /**
    * Application entry point.
    * 
    * @param  args  command-line arguments
    */
   public static void main( String... args ) {
      trace( "terminal application started" );

      String termName = (args.length > 0) ? args[0] : DEFAULT_TERM_NAME;
      String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;
      trace("Terminal Driver main termName='%s' db='%s'", termName, rdsDb);
      TerminalDriver term = new TerminalDriver( termName, rdsDb );

      term.run();
   }

}