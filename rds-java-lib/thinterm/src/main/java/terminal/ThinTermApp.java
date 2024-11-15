/*
 * ThinTermApp.java
 * 
 * The Numina Thin Terminal application.
 * 
 * (c) 2013, Numina Group, Inc.
 */
package terminal;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.prefs.Preferences;
import javax.swing.*;

import rds.CommonAscii;
import rds.RDSUtil;
import rds.SpringUtilities;
import Serialio.*;


/**
 * An application for controlling the Numina Thin Terminal.
 */
public class ThinTermApp {
   /*
    * --- constants ---
    */

   /** Server status. */
   private enum Status { INIT, LISTEN, CONNECTED, CLOSED, EXIT };

   /** Default server port. */
   private static final String DEFAULT_PORT = "10000";

   /** Default value for fullscreen mode. */
   private static final String DEFAULT_FULLSCREEN = "no";

   /** Polling interval (msec). */
   private static final long POLL_INTERVAL = 500L;

   /** Pause time (msec) in case of error. */
   private static final long SLEEP_DURATION = 5 * 1000L;

   /** File path of logo. */
   private static final String DEFAULT_LOGO ="/home/pi/img/numina-group-logo.png";
   
   // other constants
   private static final int SOCKET_TIMEOUT = 400;  // msec
   private static final int SERIAL_BUF_LEN = 1024;
   private static final int SERIAL_TIMEOUT = 10000;  // msec
   private static final String DEFAULT_SCREEN_COLOR = "$009C9CF7";
   private static final String DEFAULT_TEXT_COLOR = "$00000000";
   public static final String FN_RESET = "F12";  // fn key to reset settings


   private class Serial {
      public String dev;
      public int baud;
      public String parity;
      public int databits;
      public int stopbits;
      public byte endChar;

      public SerialPort port;
   }


   /*
    * --- class variables ---
    */

   /** Storage map for local configuration parameters. */
   private Map<String,String> configMap;

   /** Collection of serial ports. */
   private Map<Integer,Serial> portMap;

   /** The user-interface class for the application. */
   private ThinTermGui gui;


   // communications variables
   private ServerSocket serverSocket;
   private Socket clientSocket;
   private BufferedReader in;
   private PrintStream out;

   // other class variables
   private Status status;
   private TimerTask serverTask;
   private SimpleDateFormat timestampFormatter;


   /*
    * --- constructor/init ---
    */

   /** Constructs the application. */
   private ThinTermApp() {
      trace( "Numina Thin Terminal application started" );
      setStatus( Status.INIT );
   }


   /*
    * --- getter/setter/maintenance methods ---
    */

   /** Sets the server status. */
   private void setStatus( Status status ) {
      this.status = status;
      trace( "status: %s", status.toString() );
   }

   /**
    * Checks the stored values of the station-specific application settings.
    */
   private boolean checkStationSettings() {
      String configStatus = getConfig( "configStatus", "" );
      if ("saved".equals( configStatus ))
         return true;

      final String port = getConfig( "port", DEFAULT_PORT );
      final String fullscreen = getConfig( "fullscreen", DEFAULT_FULLSCREEN );

      return enterStationSettings( port, fullscreen );
   }

   /** Prompts the user to enter the station-specific settings. */
   private boolean enterStationSettings( String port, String fullscreen ) {
      JTextField portField = new JTextField( port, 10 );
      JTextField fullscreenField = new JTextField( fullscreen, 10 );

      JPanel fieldPanel = new JPanel( new SpringLayout() );
      fieldPanel.add( new JLabel( "Network Port:", JLabel.RIGHT ) );
      fieldPanel.add( portField );
      fieldPanel.add( new JLabel( "Fullscreen (yes/no):", JLabel.RIGHT ) );
      fieldPanel.add( fullscreenField );
      SpringUtilities.makeCompactGrid( fieldPanel, 2, 2, 5, 5, 5, 5 );

      JPanel containerPanel = new JPanel( new FlowLayout(
            FlowLayout.CENTER, 0, 0 ) );
      containerPanel.add( fieldPanel );

      int returnValue = JOptionPane.showConfirmDialog(
            null,                          // parent component
            containerPanel,                // message object
            "Edit Terminal Settings",      // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION) {
         alert( "incomplete settings" );
         return false;
      }

      port = portField.getText().trim();
      fullscreen = fullscreenField.getText().trim();
      if (RDSUtil.stringToInt( port, -1 ) <= 0) {
         alert( "invalid port" );
         return false;
      }

      writeConfig( "port", port );
      writeConfig( "fullscreen", fullscreen );
      writeConfig( "configStatus", "saved" );

      return true;
   }

   /** Clears the stored values of the station-specific settings. */
   public void resetStationSettings() {
      Preferences prefs = Preferences.userNodeForPackage( this.getClass() );
      prefs.put( "configStatus", "reset" );
      trace( "station settings reset" );
   }

   /**
    * Gets the value of a configuration parameter.  If the parameter is
    * not defined, the specified default value is returned.  Specifically,
    * this method will only return {@code null} if the passed-in default
    * value is {@code null}.
    * 
    * @param   param         the name of the parameter, a {@code String}
    * @param   defaultValue  the default {@code String} value to return if no
    *          matching parameter is found
    * @return  the {@code String} value of the specified parameter, or the
    *          default value if the parameter is not defined
    */
   public String getConfig( String param, String defaultValue ) {
      String value = null;
      if (configMap != null && param != null)
         value = configMap.get( param );
      if (value == null && param != null)
         value = readConfig( param, defaultValue );
      return (value != null) ? value : defaultValue;
   }

   /**
    * Reads the value of a configuration parameter from the stored user
    * preferences.
    * 
    * @param   param         the name of the parameter, a {@code String}
    * @param   defaultValue  the default {@code String} value to return if no
    *          matching parameter is found
    * @return  the {@code String} value of the specified parameter, or the
    *          default value if the parameter is not defined
    */
   private String readConfig( String param, String defaultValue ) {
      Preferences prefs = Preferences.userNodeForPackage( this.getClass() );
      return prefs.get( param, defaultValue );
   }

   /**
    * Sets the value of a configuration parameter.  If the specified value
    * is {@code null}, the parameter will be removed.
    * 
    * @param   param  the name of the parameter
    * @param   value  the new value for the parameter
    * @return  {@code true} if the new value is different than the previous
    *          value, {@code false} otherwise
    */
   private boolean setConfig( String param, String value ) {
      if (param == null || param.isEmpty())
         return false;

      if (configMap == null)
         configMap = new HashMap<String,String>();

      String oldValue;
      boolean changed;
      if (value == null) {
         oldValue = configMap.remove( param );
         changed = (oldValue != null);
      } else {
         oldValue = configMap.put( param, value );
         changed = (oldValue == null || !value.equals( oldValue ));
      }

      if (changed) {
         if (oldValue == null)
            trace( "parameter '%s' initialized to '%s'",
                  param, value );
         else
            trace( "parameter '%s' changed from '%s' to '%s'",
                  param, oldValue, value );
      }

      return changed;
   }

   /**
    * Writes the value of a configuration parameter into the stored user
    * preferences.
    * 
    * @param   param  the name of the parameter
    * @param   value  the new value for the parameter
    */
   private void writeConfig( String param, String value ) {
      Preferences prefs = Preferences.userNodeForPackage( this.getClass() );
      prefs.put( param, value );

      setConfig( param, value );
   }


   /*
    * --- comms ---
    */

   /** Schedules the server to poll for incoming connections. */
   private void runServer() {
      serverTask = new TimerTask() {
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
      (new Timer( true )).schedule( serverTask, 0, POLL_INTERVAL );
   }

   /** Polls the connection for messages to process. */
   private void poll() {
      if (testConnection())
         recv();
      else
         RDSUtil.sleep( SLEEP_DURATION );
   }

   /** Opens the connection, if necessary. */
   private boolean testConnection() {
      if (serverSocket == null || clientSocket == null)
         return open();

      return true;
   }

   /** Accepts a new incoming connection and prepares the I/O. */
   private boolean open() {
      if (gui == null) {
         trace( "wait for gui before creating server..." );
         return false;
      }

      int port = RDSUtil.stringToInt( getConfig( "port", "-1" ), -1 );

      serverSocket = null;
      try {
         serverSocket = new ServerSocket( port );
      } catch (IOException ex) {
         alert( "unable to create server at port %d", port );
         alert( ex.getMessage() );
         return false;
      }
      setStatus( Status.LISTEN );
      trace( "listening for connection on port %d", port );

      clientSocket = null;
      try {
         clientSocket = serverSocket.accept();
         clientSocket.setSoTimeout( SOCKET_TIMEOUT );
      } catch (IOException ex) {
         alert( "unable to accept incoming connection" );
         alert( ex.getMessage() );
         return false;
      }
      setStatus( Status.CONNECTED );
      trace( "connection accepted" );

      try {
         out = new PrintStream( clientSocket.getOutputStream(), true,
               "US-ASCII" );
      } catch (UnsupportedEncodingException ex) {
         alert( "unable to create output stream: unsupported encoding" );
         alert( ex.getMessage() );
         return false;
      } catch (IOException ex) {
         alert( "unable to create output stream: i/o error" );
         alert( ex.getMessage() );
         return false;
      }

      try {
         in = new BufferedReader( new InputStreamReader(
               clientSocket.getInputStream() ) );
      } catch (IOException ex) {
         alert( "unable to create input stream: i/o error" );
         alert( ex.getMessage() );
         return false;
      }

      return true;
   }

   /** Closes the connection. */
   private void close() {
      setStatus( Status.CLOSED );
      clearScreen();
      removeSerial();

      try {
         if (clientSocket != null)
            clientSocket.close();
         if (serverSocket != null)
            serverSocket.close();
         if (in != null)
            in.close();
         if (out != null)
            out.close();
      } catch (IOException ex) {
         alert( "i/o exception during close" );
         alert( ex.getMessage() );
      }

      clientSocket = null;
      serverSocket = null;
      in = null;
      out = null;
   }

   /** Sends a message to the remote client. */
   public void send( String msg ) {
      if (out == null)
         return;

      out.format( "%s\r", msg );

      if (out.checkError()) {
         String msgSnippet = (msg.length() > 40) ?
               msg.substring( 0, 40 ) + "..." : msg;
         alert( "i/o error sending msg [%s]", msgSnippet );
         close();
      }
   }

   /** Receives a message from the remote client. */
   public void recv() {
      String inputLine;
      try {
         while (status == Status.CONNECTED) {
            inputLine = in.readLine();
            if (inputLine == null) {
               trace( "connection closed" );
               close();
               return;
            }
            processInput( inputLine );
         }
      } catch (SocketTimeoutException ex) {
         send( "H" );
      } catch (IOException ex) {
         alert( "i/o error during recv" );
         alert( ex.getMessage() );
         close();
      }
   }


   /*
    * --- serial comms ---
    */

   /**
    * Creates and configures a serial port for communication to external
    * devices.
    */
   private void handleSerialConfig( String... data ) {
      if (portMap == null)
         portMap = new HashMap<Integer,Serial>();

      if (data == null || data.length == 0) {
         alert( "invalid data in serial-config request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         alert( "invalid tag in serial-config request: %d", tag );
         return;
      }

      Serial serial = portMap.get( tag );
      if (serial != null) {
         alert( "serial %d already configured", tag );
         return;
      }

      serial = new Serial();
      serial.dev      = getDataString( field++, "", data );
      serial.baud     = getDataInt( field++, -1, data );
      serial.parity   = getDataString( field++, "", data );
      serial.databits = getDataInt( field++, -1, data );
      serial.stopbits = getDataInt( field++, -1, data );
      serial.endChar  = (byte)getDataInt( field++, 0, data );
      trace( "create serial %d at %s: %d %s-%d-%d, %d",
            tag, serial.dev, serial.baud, serial.parity, serial.databits,
            serial.stopbits, serial.endChar );

      createSerialPort( serial );
      if (serial.port == null) {
         alert( "error creating serial %d at %s", tag, serial.dev );
         return;
      }

      portMap.put( tag, serial );
      gui.addSerial( tag );
   }

   /** Creates the low-level serial port associated with configuration. */
   private void createSerialPort( Serial serial ) {
      SerialConfig cfg = new SerialConfig( serial.dev );

      int cfgBaud = SerialConfig.BR_9600;
      switch (serial.baud) {
         case   2400: cfgBaud = SerialConfig.BR_2400;   break;
         case   4800: cfgBaud = SerialConfig.BR_4800;   break;
         case  57600: cfgBaud = SerialConfig.BR_57600;  break;
         case 115200: cfgBaud = SerialConfig.BR_115200; break;
      }
      cfg.setBitRate( cfgBaud );

      int cfgData = SerialConfig.LN_8BITS;
      switch (serial.databits) {
         case 5: cfgData = SerialConfig.LN_5BITS; break;
         case 6: cfgData = SerialConfig.LN_6BITS; break;
         case 7: cfgData = SerialConfig.LN_7BITS; break;
      }
      cfg.setDataBits( cfgData );

      int cfgStop = SerialConfig.ST_1BITS;
      switch (serial.stopbits) {
         case 2: cfgStop = SerialConfig.ST_2BITS; break;
      }
      cfg.setStopBits( cfgStop );

      int cfgParity = SerialConfig.PY_NONE;
      if (serial.parity != null & !serial.parity.isEmpty()) {
         switch (serial.parity.charAt( 0 )) {
            case 'E': cfgParity = SerialConfig.PY_EVEN; break;
            case 'O': cfgParity = SerialConfig.PY_ODD;  break;
         }
      }
      cfg.setParity( cfgParity );

      cfg.setHandshake( SerialConfig.HS_NONE );

      try {
         serial.port = new SerialPortLocal( cfg );
      } catch (Throwable t) {
         alert( t.toString() );
      }
   }

   /**
    * Transmits text to a serial device.
    */
   private void handleSerialXmit( String... data ) {
      if (portMap == null)
         portMap = new HashMap<Integer,Serial>();

      if (data == null || data.length == 0) {
         alert( "invalid data in serial-xmit request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         alert( "invalid tag in serial-xmit request: %d", tag );
         return;
      }

      Serial serial = portMap.get( tag );
      if (serial == null) {
         alert( "serial %d not configured", tag );
         return;
      }

      String message = getDataString( field++, "", data );
      String text = RDSUtil.fromBinHex( message );
      // trace("send ["+text+"] from ["+message+"] ") ;
      sendSerial( tag, text );
   }

   /** Sends text to the specified serial port. */
   public void sendSerial( int tag, String text ) {
      if (text == null || text.isEmpty())
         return;

      if (portMap == null)
         return;

      Serial serial = portMap.get( tag );
      if (serial == null)
         return;

      SerialPort port = serial.port;
      if (port == null)
         return;

      try {
         port.putString( text );
         trace( "sent %d bytes to port %d", text.length(), tag );
      } catch (IOException ex) {
         alert( "failed to send data to port %d port", tag );
         alert( ex.getMessage() );
      }
   }

   /** Receives a line of text from the specified serial port. */
   public String recvSerial( int tag ) {
      if (portMap == null)
         return null;

      Serial serial = portMap.get( tag );
      if (serial == null)
         return null;

      SerialPort port = serial.port;
      if (port == null)
         return null;

      SerInputStream in;
      byte[] buf = new byte[ SERIAL_BUF_LEN ];
      int len = 0;

      try {
         in = new SerInputStream( port );
         in.setRcvTimeout( SERIAL_TIMEOUT );
      } catch (IOException ex) {
         alert( "failed to create input stream for port %d", tag );
         alert( ex.getMessage() );
         in = null;
      }

      if (in == null)
         return null;

      // add received bytes to the buffer, return the entire string
      // when the end character is encountered
      while (true) {
         int retval = -1;
         try {
            retval = in.read();  // blocks until input is available or timeout
         } catch (IOException ex) {
            alert( "failed to read from port %d", tag );
            alert( ex.getMessage() );
         }

         if (retval < 0)
            return null;
         else if (retval == serial.endChar || len >= SERIAL_BUF_LEN)
            return (len > 0) ? new String( buf, 0, len ) : "";
         else if (retval >= CommonAscii.MIN_PRINTABLE &&
               retval <= CommonAscii.MAX_PRINTABLE)
            buf[len++] = (byte)retval;
      }
   }

   /** Remove all serial ports. */
   private void removeSerial() {
      if (gui != null)
         gui.stopSerial();

      if (portMap == null)
         return;

      for (int tag : portMap.keySet()) {
         Serial serial = portMap.get( tag );
         if (serial == null) continue;
         SerialPort port = serial.port;
         if (port == null) continue;
         try {
            port.close();
            trace( "port %d closed", tag );
         } catch (IOException ex) {
            alert( "error closing port %d", tag );
            alert( ex.getMessage() );
         }
         portMap.remove( tag );
      }
   }


   /*
    * --- processing ---
    */

   /** Processes a single input command. */
   private void processInput( String text ) {
      if (text == null || text.isEmpty())
         return;

      String cmd = text.substring( 0, 1 );
      final String[] data = text.substring( 1 ).split( "\\|" );

      if (cmd.equals( "H" )) {            // heartbeat
         return;
      } else if (cmd.equals( "C" )) {     // clear
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleClear( data ); }
         } );
      } else if (cmd.equals( "B" )) {     // button
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleButton( data ); }
         } );
      } else if (cmd.equals( "E" )) {     // entry field
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleEntry( data ); }
         } );
      } else if (cmd.equals( "I" )) {     // image field
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleImage( data ); }
         } );
      } else if (cmd.equals( "P" )) {     // password field
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handlePass( data ); }
         } );
      } else if (cmd.equals( "R" )) {     // rectangle
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleRectangle( data ); }
         } );
      } else if (cmd.equals( "S" )) {     // config serial
         handleSerialConfig( data );
      } else if (cmd.equals( "T" )) {     // text
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleText( data ); }
         } );
      } else if (cmd.equals( "Y" )) {     // focus
         SwingUtilities.invokeLater( new Runnable() {
            public void run() { gui.handleFocus( data ); }
         } );
      } else if (cmd.equals( "X" )) {     // serial transmit
         handleSerialXmit( data );
      }
   }


   /*
    * --- general utility ---
    */

   /** Gets an integer from a data array at the specified index. */
   public static int getDataInt( int index, int otherwise, String... data ) {
      int val = otherwise;
      if (data != null && data.length > index)
         val = RDSUtil.stringToInt( data[index], otherwise );
      return val;
   }

   /** Gets a string from a data array at the specified index. */
   public static String getDataString( int index, String otherwise, String... data ) {
      String val = otherwise;
      if (data != null && data.length > index)
         val = data[index];
      return val;
   }

   /** Gets a color from a data array at the specified index. */
   public static Color getDataColor( int index, Color otherwise, String... data ) {
      Color val = otherwise;
      if (data != null && data.length > index) {
         String c = data[index];
         if (c.length() == 9 && c.charAt( 0 ) == '$') {
            int p = 1;
            int a = 255 - RDSUtil.stringToInt( c.substring( p, p+=2 ), 16, 0 );
            int b = RDSUtil.stringToInt( c.substring( p, p+=2 ), 16, 0 );
            int g = RDSUtil.stringToInt( c.substring( p, p+=2 ), 16, 0 );
            int r = RDSUtil.stringToInt( c.substring( p, p+=2 ), 16, 0 );
            val = new Color( r, g, b, a );
         } else if (c.charAt( 0 ) == '#') {
            try {
               val = Color.decode(c);
            } catch (NumberFormatException e) {}
         } else {
            String cName = c.toLowerCase();
            if (c.startsWith( "cl" ))
               cName = cName.substring( 2 );

            if ("black".equals( cName )) val = Color.BLACK;
            else if ("blue".equals( cName )) val = Color.BLUE;
            else if ("cyan".equals( cName )) val = Color.CYAN;
            else if ("darkGray".equals( cName )) val = Color.DARK_GRAY;
            else if ("gray".equals( cName )) val = Color.GRAY;
            else if ("green".equals( cName )) val = Color.GREEN;
            else if ("lightGray".equals( cName )) val = Color.LIGHT_GRAY;
            else if ("magenta".equals( cName )) val = Color.MAGENTA;
            else if ("orange".equals( cName )) val = Color.ORANGE;
            else if ("pink".equals( cName )) val = Color.PINK;
            else if ("red".equals( cName )) val = Color.RED;
            else if ("white".equals( cName )) val = Color.WHITE;
            else if ("yellow".equals( cName )) val = Color.YELLOW;
         }
      }
      return val;
   }

   /** Gets an double from a data array at the specified index. */
   public static double getDataDouble( int index, double otherwise, String... data ) {
      double val = otherwise;
      if (data != null && data.length > index)
         val = RDSUtil.stringToDouble(data[index], otherwise );
      return val;
   }
   
   /** Performs cleanup duties upon application close. */
   public void cleanup() {
      if (serverTask != null)
         serverTask.cancel();

      close();
      setStatus( Status.EXIT );
   }

   /** Logs a message to standard output. */
   public void trace( String format, Object... args ) {
      String text = String.format( format, args );
      String traceText;
      traceText = "T:" + text.replaceAll( "\n", "\nT:   " );
      System.out.println( traceText );
   }

   /** Logs an alert to standard output. */
   public void alert( String format, Object... args ) {
      String text = String.format( format, args );
      String traceText;
      traceText = "A:" + text.replaceAll( "\n", "\nA:   " );
      System.out.println( traceText );
   }


   /*
    * --- UI creation/config ---
    */

   /**
    * Creates the GUI and displays it.  For thread safety, this method
    * should be invoked from the event-dispatching thread.
    */
   private void createAndShowGUI() {
      // set LaF and configure UI defaults
      if (!configureUI())
         System.exit( 0 );

      // create the application frame and content panel
      final JFrame frame = new JFrame(
            "Numina Workstation" );
      gui = new ThinTermGui( this );
      frame.setContentPane( gui.getMainPanel() );

      // override application close
      frame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
      frame.addWindowListener( new WindowAdapter() {
         @Override
         public void windowClosing( WindowEvent evt ) {
            gui.exit();
         }
      } );

      // display the application, maximized or centered on the screen
      GraphicsDevice device = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice();
      if ("yes".equals( getConfig( "fullscreen", "" ) ) &&
            device.isFullScreenSupported()) {  // full-screen mode
         frame.setResizable( false );
         frame.setUndecorated( true );
         device.setFullScreenWindow( frame );
         frame.validate();
      } else {  // windowed mode
         frame.pack();
         frame.setLocationRelativeTo( null );
      }
      frame.setVisible( true );
      clearScreen();
   }

   /**
    * Performs global UI configuration.  For thread safety, this method
    * should be invoked from the event-dispatching thread.
    */
   private boolean configureUI() {
      // set the look and feel
      try {
         UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName() );
      } catch (Exception ex) {
         alert( "error setting look-and-feel" );
         alert( ex.getMessage() );
         return false;
      }

      if (!checkStationSettings())
         return false;

      return true;
   }

   /** Clears the screen with a default color. */
   private void clearScreen() {
      SwingUtilities.invokeLater( new Runnable() {
         public void run() { 
            InetAddress ip = null;
            String ipAddress = "";
            String hostname = "";
            String dbAddress = getDatabaseAddress();
            try {
               ip = InetAddress.getLocalHost();
               ipAddress = ip.getHostAddress();
               hostname = ip.getHostName();
            } catch (UnknownHostException e) {
               e.printStackTrace();
            } catch (Exception e) {
               
            }
            
            int tag = 1;
            gui.handleClear( DEFAULT_SCREEN_COLOR );
            gui.handleText( new String[] {"" + tag++, "0", "100", "80",
                        "RDS WORKSTATION", DEFAULT_TEXT_COLOR, "1920" } ); 
            
            gui.handleText( new String[] {"" + tag++, "100", "250", "65",
                  "Hostname: " + hostname, DEFAULT_TEXT_COLOR, "0" } );
            gui.handleText( new String[] {"" + tag++, "100", "350", "65",
                  "IP Address: " + ipAddress, DEFAULT_TEXT_COLOR, "0" } );
            gui.handleText( new String[] {"" + tag++, "100", "450", "65",
                  "DB Address: " + dbAddress, DEFAULT_TEXT_COLOR, "0" } );
            
            gui.handleText( new String[] {"" + tag++, "100", "750", "65",
                  "Waiting for connection from server...", DEFAULT_TEXT_COLOR, "0" } );
            gui.handleImage(new String[] {"" + tag++, DEFAULT_LOGO, 
                  "1895", "1055", "2", "2", "0", "200", "0" } );
            /*gui.handleRectangle(new String[] {"5", "0", "0", "1920", "1080",
                  "red", "10", "black" });*/
         }
      } );
   }

   protected String getDatabaseAddress() {
      String dbAddress = "";
      boolean isLinux = System.getProperty("os.name")
            .toLowerCase().contains("linux");
      if (!isLinux)
         return dbAddress;
      try {
         String[] cmd = {
               "/bin/sh",
               "-c",
               "grep db /etc/hosts | awk '{print $1}'"
         };
         Process proc = Runtime.getRuntime().exec(cmd);

         BufferedReader stdInput = new BufferedReader(new 
               InputStreamReader(proc.getInputStream()));

          String s = null;
          while ((s = stdInput.readLine()) != null) {
             dbAddress = dbAddress + s;
         }
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return dbAddress;
   }


   /*
    * --- main ---
    */

   /**
    * The application entry point.
    * 
    * @param   args  a list of command-line arguments
    */
   public static void main( String... args ) {
      // configure SwingWorkers to use a pool of threads
      sun.awt.AppContext.getAppContext().put( SwingWorker.class,
            java.util.concurrent.Executors.newCachedThreadPool() );

      // create and initialize the application server
      final ThinTermApp app = new ThinTermApp();

      // create the GUI on the event-dispatching thread
      SwingUtilities.invokeLater( new Runnable() {
         public void run() { app.createAndShowGUI(); }
      } );

      // run the thin terminal server
      app.runServer();
   }

}
