/*
 * ThinTermGui.java
 * 
 * The graphical user interface for the Numina Thin Terminal application.
 * 
 * (c) 2013, Numina Group, Inc.
 */
package terminal;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.UIManager;

import rds.RDSUtil;
import static terminal.ThinTermApp.*;

/**
 * The graphical user interface for the Numina Thin Terminal.
 */
public class ThinTermGui
      implements ActionListener {

   /*
    * --- constants ---
    */

   /** The dimensions of the main application panel. */
   private static final Dimension MAIN_DIM = new Dimension( 800, 600 );

   /** Amount of time to sleep between fast processing cycles (msec). */
   private static final int SLEEP_DURATION = 100;


   /*
    * --- class variables ---
    */

   /** The server application that created this panel. */
   private ThinTermApp app;

   /** Background workers for receiving messages from serial devices. */
   private List<SerialRecvWorker> serialWorkerList;

   /** The main panel. */
   private JPanel mainPanel;

   // components
   private Map<Integer,JButton> buttonMap;
   private Map<Integer,JTextField> entryMap;
   private Map<Integer,JPasswordField> passMap;
   private Map<Integer,JPanel> rectMap;
   private Map<Integer,JLabel> textMap;
   private Map<Integer,JLabel> imageMap;


   /*
    * --- constructor/initialization ---
    */

   /**
    * Constructs an instance of the graphical user interface.
    * 
    * @param   app  the parent application 
    */
   public ThinTermGui( ThinTermApp app ) {
      this.app = app;

      serialWorkerList = new ArrayList<SerialRecvWorker>();

      buttonMap = new HashMap<Integer,JButton>();
      entryMap = new HashMap<Integer,JTextField>();
      passMap = new HashMap<Integer,JPasswordField>();
      rectMap = new HashMap<Integer,JPanel>();
      textMap = new HashMap<Integer,JLabel>();
      imageMap = new HashMap<Integer,JLabel>();

      app.trace( "user interface created" );
   }


   /*
    * --- graphical elements and layout ---
    */

   /** Gets the gui's main panel. */
   public JPanel getMainPanel() {
      if (mainPanel == null)
         createMainPanel();
      return mainPanel;
   }

   /** Creates the main user-interface panel. */
   private void createMainPanel() {
      mainPanel = new JPanel();
      mainPanel.setLayout( null );
      mainPanel.setPreferredSize( MAIN_DIM );

      // bind the function keys
      for (int i = 1; i <= 12; i++) {
         final String fn = "F" + i;
         mainPanel.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put(
               KeyStroke.getKeyStroke( fn ), fn );
         mainPanel.getActionMap().put( fn, new AbstractAction() {
            public void actionPerformed( ActionEvent evt ) {
               if (FN_RESET.equals( fn )) {
                  app.resetStationSettings();
               } else {
                  app.trace( "input: function key [%s]", fn );
                  app.send( fn );
               }
            }
         } );
      }

      app.trace( "user interface layout complete" );
   }

   /** 
    * Handles a clear-screen request.  The fields are as follows:
    * color.
    */
   public void handleClear( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in rectangle request" );
         return;
      }

      int field = 0;
      Color c = getDataColor( field++, mainPanel.getBackground(), data );
      mainPanel.setBackground( c );

      // clear tags
      buttonMap.clear();
      entryMap.clear();
      passMap.clear();
      rectMap.clear();
      textMap.clear();
      imageMap.clear();

      // clear screen elements
      mainPanel.removeAll();
      mainPanel.repaint();
   }

   /**
    * Handles a button element request.  The fields are as follows:
    * tag, left, top, width, height, size, text and color
    */
   public void handleButton( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in button request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in button request: %d", tag );
         return;
      }

      JButton button = buttonMap.get( tag );
      if (button == null) {
         button = new JButton();
         button.setActionCommand( "B" + tag );
         button.addActionListener( this );
         mainPanel.add( button );
         buttonMap.put( tag, button );
      }

      Rectangle bounds = button.getBounds();
      Font font = button.getFont();

      int x = getDataInt( field++, bounds.x, data );
      int y = getDataInt( field++, bounds.y, data );
      int w = getDataInt( field++, bounds.width, data );
      int h = getDataInt( field++, bounds.height, data );
      int s = getDataInt( field++, button.getFont().getSize(), data );
      String t = getDataString( field++, button.getText(), data );
      Color c = getDataColor( field++, button.getForeground(), data );

      button.setFont( font.deriveFont( (float)s ) );
      button.setText( t );

      if(c != null && !c.equals(Color.BLACK)) {
         // Try-Catch is required to "fill" button and not just change the border color
         try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
         } catch (Exception e) { }
         button.setBackground(c); //Background is the "fill" color
      }
      else { //Set the default color to the standard color so the fill isn't black
         button.setBackground(new JButton().getBackground());
      }

      button.setBounds( x, y, w, h );
      mainPanel.repaint();
   }

   /**
    * Handles a text-entry element request.  The fields are as follows:
    * tag, left, top, width, height, size.
    */
   public void handleEntry( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in entry field request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in entry field request: %d", tag );
         return;
      }

      JTextField entry = entryMap.get( tag );
      if (entry == null) {
         entry = new JTextField();
         entry.setActionCommand( "E" + tag );
         entry.addActionListener( this );
         mainPanel.add( entry );
         entryMap.put( tag, entry );
      }

      Rectangle bounds = entry.getBounds();
      Font font = entry.getFont();

      int x = getDataInt( field++, bounds.x, data );
      int y = getDataInt( field++, bounds.y, data );
      int w = getDataInt( field++, bounds.width, data );
      int h = getDataInt( field++, bounds.height, data );
      int s = getDataInt( field++, entry.getFont().getSize(), data );

      entry.setFont( font.deriveFont( (float)s ) );

      entry.setBounds( x, y, w, h );
      mainPanel.repaint();
   }

   /**
    * Handles a password-entry element request.  The fields are as follows:
    * tag, left, top, width, height, size.
    */
   public void handlePass( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in password field request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in password field request: %d", tag );
         return;
      }

      JPasswordField pass = passMap.get( tag );
      if (pass == null) {
         pass = new JPasswordField();
         pass.setActionCommand( "P" + tag );
         pass.addActionListener( this );
         mainPanel.add( pass );
         passMap.put( tag, pass );
      }

      Rectangle bounds = pass.getBounds();
      Font font = pass.getFont();

      int x = getDataInt( field++, bounds.x, data );
      int y = getDataInt( field++, bounds.y, data );
      int w = getDataInt( field++, bounds.width, data );
      int h = getDataInt( field++, bounds.height, data );
      int s = getDataInt( field++, pass.getFont().getSize(), data );

      pass.setFont( font.deriveFont( (float)s ) );

      pass.setBounds( x, y, w, h );
      mainPanel.repaint();
   }

   /**
    * Handles a rectangle request.  The fields are as follows:
    * tag, left, top, width, height, color, border-thickness, border-color.
    */
   public void handleRectangle( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in rectangle request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in text request: %d", tag );
         return;
      }

      JPanel panel = rectMap.get( tag );
      if (panel == null) {
         panel = new JPanel();
         panel.setOpaque( true );
         panel.setBorder( BorderFactory.createRaisedBevelBorder() );
         mainPanel.add( panel );
         rectMap.put( tag, panel );
      }

      Rectangle bounds = panel.getBounds();

      int currentThickness = -1;
      Color currentBorderColor = null;
      Border border = panel.getBorder();
      if (border != null && border instanceof LineBorder) {
         LineBorder lineBorder = (LineBorder)border;
         currentThickness = lineBorder.getThickness();
         currentBorderColor = lineBorder.getLineColor();
      }

      int x = getDataInt( field++, bounds.x, data );
      int y = getDataInt( field++, bounds.y, data );
      int w = getDataInt( field++, bounds.width, data );
      int h = getDataInt( field++, bounds.height, data );
      Color c = getDataColor( field++, panel.getBackground(), data );
      int t = getDataInt( field++, currentThickness, data );
      Color c2 = getDataColor( field++, currentBorderColor, data );

      panel.setBackground( c );
      if (t == 0)
         panel.setBorder( null );
      else if (t > 0)
         panel.setBorder( BorderFactory.createLineBorder( c2, t ) );
      else if (t < 0)
         panel.setBorder( BorderFactory.createRaisedBevelBorder() );

      panel.setBounds( x, y, w, h );
      mainPanel.repaint();
   }

   /**
    * Handles a text element request.  The fields are as follows:
    * tag, left, top, size, text, color, width.
    */
   public void handleText( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in text request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in text request: %d", tag );
         return;
      }

      JLabel label = textMap.get( tag );
      if (label == null) {
         label = new JLabel();
         mainPanel.add( label );
         textMap.put( tag, label );
      }

      Rectangle bounds = label.getBounds();
      Font font = label.getFont();

      int x = getDataInt( field++, bounds.x, data );
      int y = getDataInt( field++, bounds.y, data );
      int s = getDataInt( field++, label.getFont().getSize(), data );
      String t = getDataString( field++, label.getText(), data );
      Color c = getDataColor( field++, label.getForeground(), data );
      int w = getDataInt( field++, 0, data );

      label.setFont( font.deriveFont( (float)s ) );
      label.setForeground( c );
      label.setText( t );

      Dimension d = label.getPreferredSize();
      int align = JLabel.LEFT;
      int width = d.width;
      if (w > 0) {
         align = JLabel.CENTER;
         width = w;
      } else if (w < 0) {
         align = JLabel.RIGHT;
         width = -w;
      }
      label.setHorizontalAlignment( align );

      label.setBounds( x, y, width, d.height );
      mainPanel.repaint();
   }


   /**
    * Handles a button element request.  The fields are as follows:
    * tag.
    */
   public void handleFocus( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in focus request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in focus request: %d", tag );
         return;
      }

      // there is no system for tag uniqueness, so assume the request
      // is for a text field; if no matching text field, try password
      // field and then button

      JTextField entry = entryMap.get( tag );
      if (entry != null) {
         entry.requestFocusInWindow();
         entry.selectAll();
         return;
      }

      JPasswordField pass = passMap.get( tag );
      if (pass != null) {
         pass.requestFocusInWindow();
         pass.selectAll();
         return;
      }

      JButton button = buttonMap.get( tag );
      if (button != null) {
         button.requestFocusInWindow();
         return;
      }

      app.alert( "unable to locate component with tag %d for focus request",
            tag );
   }

   /**
    * Handles a rectangle request.  The fields are as follows:
    * tag, image-path, scale, left, top.
    */
   public void handleImage( String... data ) {
      if (data == null || data.length == 0) {
         app.alert( "invalid data in image request" );
         return;
      }

      int field = 0;

      int tag = getDataInt( field++, -1, data );
      if (tag < 0) {
         app.alert( "invalid tag in text request: %d", tag );
         return;
      }

      JLabel label = imageMap.get( tag );
      if (label == null) {
         label = new JLabel();
         mainPanel.add( label );
         imageMap.put( tag, label );
      }
      
      String imagePath = getDataString( field++, null, data );
      BufferedImage myPicture = null;
      InputStream connection;

      try {
         myPicture = ImageIO.read(new File(imagePath));
      } catch (Exception ex) {
         app.alert("failed to load local image [%s], trying as URL",  imagePath);

         try {
            URL url = new URL(imagePath);  
            if(imagePath.indexOf("https://")!=-1){
               final SSLContext sc = SSLContext.getInstance("SSL");
               sc.init(null, getTrustingManager(), new java.security.SecureRandom());                                 
               HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
               connection = url.openStream();
            }
            else{
               connection = url.openStream();
            }
            myPicture = ImageIO.read(connection);
         } 
         catch (MalformedURLException e) {
            app.alert("URL is not correct : " + imagePath);
            app.alert(e.getMessage());
            e.printStackTrace();
         } 
         catch (IOException e) {
            app.alert("IOException Occurred : " + e);
            app.alert(e.getMessage());
            e.printStackTrace();
         }
         catch (Exception e) {
            app.alert("Exception Occurred : " + e);
            app.alert(e.getMessage());
            e.printStackTrace();
         }
      }

      int width = myPicture.getWidth();
      int height = myPicture.getHeight();

      int x = getDataInt( field++, 0, data );
      int y = getDataInt( field++, 0, data );
      int alignX = getDataInt( field++, 0, data );
      int alignY = getDataInt( field++, 0, data );
      
      int w = getDataInt(field++, width, data);
      int h = getDataInt(field++, height, data);
      double scale = getDataDouble( field++, 0.0, data );

      if (scale > 0) {
         w = (int) (width  * scale / 100);
         h = (int) (height * scale / 100);
      } else if (w > 0) {
         h = h > 0 ? h : height * w / width ;
      } else if (h > 0) {
         w = w > 0 ? w : width * h / height ;
      } else {
         w = width;
         h = height;
      }
      
      label.setIcon(new ImageIcon(myPicture.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
      x += getAlignment(alignX, w);
      y += getAlignment(alignY, h);

      label.setBounds( x, y, w, h );
      
      mainPanel.repaint();
   }

   /**
    * Handles HTTPS requests for images
    * 
    */
   private static TrustManager[] getTrustingManager() {
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
             @Override
             public X509Certificate[] getAcceptedIssuers() {
                   return null;
             }

             @Override
             public void checkClientTrusted(X509Certificate[] certs, String authType) {
             }

             @Override
             public void checkServerTrusted(X509Certificate[] certs, String authType) {
             }
      } };
      return trustAllCerts;
   }

   private int getAlignment(int align, int dim) {
      switch(align) {
      case 1:
         return -dim/2;
      case 2:
         return -dim;
      case 0:
      default:
         return 0;
      }
   }


   /*
    * --- updates and processing ---
    */

   /** Exits the application, performing any necessary cleanup operations. */
   public void exit() {
      app.trace( "begin application termination" );

      stopSerial();

      // find and dispose the root window
      Component c = getMainPanel();
      while (c.getParent() != null)
         c = c.getParent();
      if (c instanceof Window)
         ((Window)c).dispose();
      app.trace( "user interface disposed" );

      app.cleanup();

      app.trace( "application terminated" );
   }

   /** Processes a single serial message. */
   protected void processSerial( int tag, String message ) {
      app.trace( "received from port %d: '%s'", tag, message );

      String data = RDSUtil.toBinHex( message.getBytes() );
      String msg = String.format( "S%d|%s", tag, data );
      app.send( msg );
   }


   /* --- interface methods --- */

   @Override
   public void actionPerformed( ActionEvent evt ) {
      String cmd = evt.getActionCommand();
      if (cmd == null || cmd.isEmpty())
         return;

      // button
      if (cmd.startsWith( "B" ) && cmd.length() > 1) {
         int tag = RDSUtil.stringToInt( cmd.substring( 1 ), 0 );
         JButton button = buttonMap.get( tag );
         if (button != null) {
            String text = button.getText();
            app.trace( "input: button [%d] (%s)", tag, text );

            String msg = String.format( "B%d", tag );
            app.send( msg );
         }
      }

      // text entry
      if (cmd.startsWith( "E" ) && cmd.length() > 1) {
         int tag = RDSUtil.stringToInt( cmd.substring( 1 ), 0 );
         JTextField entry = entryMap.get( tag );
         if (entry != null) {
            String text = entry.getText();
            entry.setText( "" );
            app.trace( "input: entry field [%d], text [%s]", tag, text );

            String msg = String.format( "E%d|%s", tag, text );
            app.send( msg );
         }
      }

      // password entry
      if (cmd.startsWith( "P" ) && cmd.length() > 1) {
         int tag = RDSUtil.stringToInt( cmd.substring( 1 ), 0 );
         JPasswordField pass = passMap.get( tag );
         if (pass != null) {
            String text = new String( pass.getPassword() );
            app.trace( "input: password field [%d]", tag );

            String msg = String.format( "P%d|%s", tag, text );
            app.send( msg );
         }
      }

   }


   /*
    * --- serial processing ---
    */

   /** Adds a processor for the specified serial port. */
   public void addSerial( int tag ) {
         SerialRecvWorker worker = new SerialRecvWorker( tag );
         serialWorkerList.add( worker );
         worker.execute();
   }

   /** Stops all serial processing. */
   public void stopSerial() {
      for (SerialRecvWorker worker : serialWorkerList)
         worker.cancel( true );
      serialWorkerList.clear();
   }

   /**
    * A worker class to receive messages from a serial port in a
    * background thread.  A single instance of this class should be
    * created per serial port to run indefinitely; individual messages
    * will be published and processed as intermediate results.
    */
   private class SerialRecvWorker
         extends SwingWorker<Void,String> {
      private int tag;

      public SerialRecvWorker( int tag ) {
         this.tag = tag;
      }

      @Override
      public Void doInBackground() {
         app.trace( "listening for serial messages from port %d...", tag );
         while (!isCancelled()) {
            String message = app.recvSerial( tag );
            if (message == null || message.isEmpty())
               RDSUtil.sleep( SLEEP_DURATION );
            else
               publish( message );
         }
         return null;
      }

      @Override
      protected void process( List<String> messageList ) {
         for (String message : messageList) {
            processSerial( tag, message );
         }
      }
   }

}
