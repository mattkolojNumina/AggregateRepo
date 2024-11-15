
package rdsGraphicsEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import rds.RDSUtil;
import rdsGraphics.RDSObject;


public class RDSGraphicsEditor
      extends JFrame
      implements ActionListener {
   public static final String VERSION = "1.1";
   public static final int WIDTH = 640;
   public static final int HEIGHT = 480;

   File rdsFile;
   JFileChooser fileChooser;
   RDSGraphicsEditorPanel editorPanel;

   public RDSGraphicsEditor() {
      super( "RDS Graphics Editor v" + VERSION );
      setName( "RDS Graphics Editor" );
      rdsFile = null;

      // determine codebase
      URL baseURL;
      try {
         baseURL = new URL( "file:" + System.getProperty( "user.dir" ) +
               File.separator );
      } catch (java.net.MalformedURLException ex) {
         RDSUtil.alert( getName() + ": error obtaining working directory" );
         baseURL = null;
      }
      if (baseURL != null) {
         RDSUtil.inform( "code base for RDS objects = [%s]",
               baseURL.toString() );
         RDSObject.setCodeBase( baseURL.toString() );
      }

      // set look-and-feel
      try {
         UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName() );
      } catch (Exception ex) {
         RDSUtil.alert( getName() + ": error setting look-and-feel" );
      }

      setJMenuBar( createMenuBar() );
      setContentPane( createContentPane() );

      setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
      addWindowListener( new WindowAdapter() {
         @Override
         public void windowClosing( WindowEvent e ) {
            confirmExit();
         }
      } );
      fileChooser = createFileChooser( baseURL.getPath() );
   }

   private JPanel createContentPane() {
      JPanel contentPane = new JPanel( new BorderLayout() );
      contentPane.setOpaque( true );

      editorPanel = new RDSGraphicsEditorPanel();
      editorPanel.setPreferredSize( new Dimension( WIDTH, HEIGHT ) );
      contentPane.add( editorPanel, BorderLayout.CENTER );

      return contentPane;
   }

   /**
    * Creates a file chooser with a custom filter for rds files.
    * 
    * @return  the file chooser object
    */
   private JFileChooser createFileChooser( String path ) {
      JFileChooser fileChooser = new JFileChooser( path );
      fileChooser.addChoosableFileFilter( new FileFilter() {
         @Override
         public boolean accept( File file ) {
            return (file.isDirectory() ||
                  "rds".equals( RDSUtil.getExtension( file ) ));
         }

         @Override
         public String getDescription() {
            return "RDS Configuration Files (*.rds)";
         }               
      } );

      return fileChooser;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void actionPerformed( ActionEvent evt ) {
      String cmd = evt.getActionCommand();

      // file menu commands
      if (cmd.equals( "New" )) {
         if (!editorPanel.isDirty() || confirmSave()) {
            editorPanel.clearPanel();
            rdsFile = null;
            updateMenuItems();
         }
      } else if (cmd.equals( "Open File..." )) {
         if (!editorPanel.isDirty() || confirmSave()) {
            int returnVal = fileChooser.showOpenDialog( this );
            if (returnVal == JFileChooser.APPROVE_OPTION) {
               rdsFile = fileChooser.getSelectedFile();
               editorPanel.clearPanel();
               editorPanel.openFile( rdsFile );
               editorPanel.clearSelectedObjects();
               updateMenuItems();
            }
         }
      } else if (cmd.equals( "Save" )) {
         saveFile( rdsFile );
      } else if (cmd.equals( "Save As..." )) {
         saveFile( null );
      } else if (cmd.equals( "Exit" )) {
         confirmExit();
      }

      // edit menu commands
      else if (cmd.equals( "Delete" )) {
         editorPanel.deleteObjects();
      } else if (cmd.equals( "Select All" )) {
         editorPanel.selectAll();
      } else if (cmd.equals( "Lock" )) {
         editorPanel.lock();
      } else if (cmd.equals( "Unlock All" )) {
         editorPanel.unlockAll();
      } else if (cmd.equals( "Bring to Front" )) {
         editorPanel.moveToFront();
      } else if (cmd.equals( "Send to Back" )) {
         editorPanel.moveToBack();
      } else if (cmd.equals( "Align Left" )) {
         editorPanel.alignLeft();
      } else if (cmd.equals( "Align Center" )) {
         editorPanel.alignCenter();
      } else if (cmd.equals( "Align Right" )) {
         editorPanel.alignRight();
      } else if (cmd.equals( "Align Top" )) {
         editorPanel.alignTop();
      } else if (cmd.equals( "Align Middle" )) {
         editorPanel.alignMiddle();
      } else if (cmd.equals( "Align Bottom" )) {
         editorPanel.alignBottom();
      }

      // view menu commands
      else if (cmd.equals( "Fit All" )) {
         editorPanel.zoomToFitAll();
      } else if (cmd.equals( "Fit Selected" )) {
         editorPanel.zoomToFitSelected();
      } else if (cmd.equals( "Reset Zoom" )) {
         editorPanel.resetZoom();
      } else if (cmd.equals( "Set Min Zoom" )) {
         editorPanel.setMinZoom(
               editorPanel.getCanvas().getCamera().getViewScale() );
         updateZoomMenuItems();
      } else if (cmd.equals( "Clear Min Zoom" )) {
         editorPanel.clearMinZoom();
         updateZoomMenuItems();
      } else if (cmd.equals( "Set Max Zoom" )) {
         editorPanel.setMaxZoom(
               editorPanel.getCanvas().getCamera().getViewScale() );
         updateZoomMenuItems();
      } else if (cmd.equals( "Clear Max Zoom" )) {
         editorPanel.clearMaxZoom();
         updateZoomMenuItems();
      }

      // insert menu commands
      else if (cmd.equals( "E-Stop" )) {
         editorPanel.addEstop();
      } else if (cmd.equals( "Image" )) {
         editorPanel.addImage();
      } else if (cmd.equals( "Indicator" )) {
         editorPanel.addIndicator();
      } else if (cmd.equals( "Jam" )) {
         editorPanel.addJam();
      } else if (cmd.equals( "Line Full" )) {
         editorPanel.addLineFull();
      } else if (cmd.equals( "Motor" )) {
         editorPanel.addMotor();
      } else if (cmd.equals( "Static Text" )) {
         editorPanel.addStaticText();
      } else if (cmd.equals( "Text" )) {
         editorPanel.addText();
      } else if (cmd.equals( "Zone" )) {
         editorPanel.addZone();
      }
   }

   private void updateMenuItems() {
      updateZoomMenuItems();
   }

   private void updateZoomMenuItems() {
      final int VIEW_MENU_INDEX = 2;
      final int MIN_ZOOM_ITEM_INDEX = 2;
      final int MAX_ZOOM_ITEM_INDEX = 3;

      JMenu viewMenu = getJMenuBar().getMenu( VIEW_MENU_INDEX );
      JMenuItem minZoomItem = viewMenu.getItem( MIN_ZOOM_ITEM_INDEX );
      JMenuItem maxZoomItem = viewMenu.getItem( MAX_ZOOM_ITEM_INDEX );

      if (editorPanel.getMinZoom() > 0.0)
         minZoomItem.setText( "Clear Min Zoom" );
      else
         minZoomItem.setText( "Set Min Zoom" );

      if (editorPanel.getMaxZoom() < Double.MAX_VALUE)
         maxZoomItem.setText( "Clear Max Zoom" );
      else
         maxZoomItem.setText( "Set Max Zoom" );
   }

   /**
    * Creates the menu bar for the application.
    * @return the menubar object
    */
   private JMenuBar createMenuBar() {
      JMenuBar menuBar;
      JMenu menu, submenu;
      JMenuItem menuItem;

      menuBar = new JMenuBar();

      // file menu
      menu = new JMenu( "File" );
      menu.setMnemonic( KeyEvent.VK_F );
      menuBar.add( menu );

      menu.add( createMenuItem( "New", KeyEvent.VK_N, KeyEvent.VK_N,
            InputEvent.CTRL_DOWN_MASK ) );
      menu.add( createMenuItem( "Open File...", KeyEvent.VK_O, KeyEvent.VK_O,
            InputEvent.CTRL_DOWN_MASK ) );
      menu.addSeparator();
      menu.add( createMenuItem( "Save", KeyEvent.VK_S, KeyEvent.VK_S,
            InputEvent.CTRL_DOWN_MASK ) );
      menuItem = createMenuItem( "Save As...", KeyEvent.VK_A, KeyEvent.VK_S,
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
      menuItem.setDisplayedMnemonicIndex( 5 );
      menu.add( menuItem );
      menu.addSeparator();
      menu.add( createMenuItem( "Exit", KeyEvent.VK_X ) );

      // edit menu
      menu = new JMenu( "Edit" );
      menu.setMnemonic( KeyEvent.VK_E );
      menuBar.add( menu );

      menu.add( createMenuItem( "Delete", KeyEvent.VK_D, KeyEvent.VK_DELETE,
            0 ) );
      menu.add( createMenuItem( "Select All", KeyEvent.VK_A, KeyEvent.VK_A,
            InputEvent.CTRL_DOWN_MASK ) );
      menu.addSeparator();

      menu.add( createMenuItem( "Lock", KeyEvent.VK_L, KeyEvent.VK_L,
            InputEvent.CTRL_DOWN_MASK ) );
      menu.add( createMenuItem( "Unlock All", KeyEvent.VK_U, KeyEvent.VK_U,
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ) );
      menu.addSeparator();

      submenu = new JMenu( "Order" );
      submenu.setMnemonic( KeyEvent.VK_O );
      menu.add( submenu );
      submenu.add( createMenuItem( "Bring to Front", KeyEvent.VK_F ) );
      submenu.add( createMenuItem( "Send to Back", KeyEvent.VK_B ) );

      submenu = new JMenu( "Alignment" );
      submenu.setMnemonic( KeyEvent.VK_N );
      menu.add( submenu );
      menuItem = createMenuItem( "Align Left", KeyEvent.VK_L );
      menuItem.setDisplayedMnemonicIndex( 6 );
      submenu.add( menuItem );
      submenu.add( createMenuItem( "Align Center", KeyEvent.VK_C ) );
      submenu.add( createMenuItem( "Align Right", KeyEvent.VK_R ) );
      submenu.addSeparator();
      submenu.add( createMenuItem( "Align Top", KeyEvent.VK_T ) );
      submenu.add( createMenuItem( "Align Middle", KeyEvent.VK_M ) );
      submenu.add( createMenuItem( "Align Bottom", KeyEvent.VK_B ) );

      // view menu
      menu = new JMenu( "View" );
      menu.setMnemonic( KeyEvent.VK_V );
      menuBar.add( menu );

      submenu = new JMenu( "Zoom" );
      submenu.setMnemonic( KeyEvent.VK_Z );
      menu.add( submenu );
      submenu.add( createMenuItem( "Fit All", KeyEvent.VK_A ) );
      submenu.add( createMenuItem( "Fit Selected", KeyEvent.VK_S ) );
      submenu.add( createMenuItem( "Reset Zoom", KeyEvent.VK_R ) );
      menu.addSeparator();

      menu.add( createMenuItem( "Set Min Zoom", KeyEvent.VK_N ) );
      menu.add( createMenuItem( "Set Max Zoom", KeyEvent.VK_X ) );

      // insert menu
      menu = new JMenu( "Insert" );
      menu.setMnemonic( KeyEvent.VK_I );
      menuBar.add( menu );

      menu.add( createMenuItem( "E-Stop", KeyEvent.VK_E ) );
      menu.add( createMenuItem( "Image", KeyEvent.VK_I ) );
      menu.add( createMenuItem( "Indicator", KeyEvent.VK_N ) );
      menu.add( createMenuItem( "Jam", KeyEvent.VK_J ) );
      menu.add( createMenuItem( "Line Full", KeyEvent.VK_L ) );
      menu.add( createMenuItem( "Motor", KeyEvent.VK_M ) );
      menu.add( createMenuItem( "Static Text", KeyEvent.VK_S ) );
      menu.add( createMenuItem( "Text", KeyEvent.VK_T ) );
      menu.add( createMenuItem( "Zone", KeyEvent.VK_Z ) );

      return menuBar;
   }

   /**
    * Creates a single menu item for addition into a menu.  The indicated
    * mnemonic is underlined in the menu item label.
    * 
    * @param   label     the item's label
    * @param   mnemonic  a mnemonic for menu traversal
    * @return  the menu item object
    */
   private JMenuItem createMenuItem( String label, int mnemonic ) {
      return createMenuItem( label, mnemonic, 0, 0 );
   }

   /**
    * Creates a single menu item for addition into a menu.  The indicated
    * mnemonic is underlined in the menu item label, and the accelerator key
    * (with modifiers, if specified) may be used to execute the menu item
    * command from outside the menu structure.
    * 
    * @param   label        the item's label
    * @param   mnemonic     a mnemonic for menu traversal
    * @param   accelerator  "shortcut" key
    * @param   modifiers    accelerator key modifiers
    * @return  the menu item object
    */
   private JMenuItem createMenuItem( String label, int mnemonic,
         int accelerator, int modifiers ) {
      JMenuItem menuItem = new JMenuItem( label );
      if (mnemonic > 0)
         menuItem.setMnemonic( mnemonic );
      if (accelerator > 0)
         menuItem.setAccelerator( KeyStroke.getKeyStroke( accelerator,
               modifiers ) );
      menuItem.addActionListener( this );
      return menuItem;
   }

   /**
    * Saves the current configuration to file.
    * 
    * @param   file  the file to save
    * @return  {@code true} if the file was successfully saved, {@code false}
    *          otherwise
    */
   private boolean saveFile( File file ) {
      boolean result = false;

      if (file == null) {
         int returnVal = fileChooser.showSaveDialog( this );
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            rdsFile = fileChooser.getSelectedFile();
            result = editorPanel.saveFile( rdsFile );
         }
      } else
         result = editorPanel.saveFile( rdsFile = file );

      return result;
   }

   /**
    * Confirms program exit if there are unsaved changes.  This is the
    * only exit point from the application.
    */
   private void confirmExit() {
      if (editorPanel.isDirty() && !confirmSave())
         return;
      dispose();  // program will exit if this is last window to close
   }

   /**
    * Queries the user as to whether or not the current configuration
    * should be saved to file before proceeding (to close the program,
    * open a new file, etc.).
    * 
    * @return  {@code true} if the action should proceed (i.e. the file
    *          has been saved or the user has explicitly declined to save
    *          the file), {@code false} otherwise
    */
   private boolean confirmSave() {
      boolean result = false;

      int returnVal = JOptionPane.showConfirmDialog( this,
            "Save changes to graphics file?", "Question",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE );
      if (returnVal == JOptionPane.YES_OPTION)
         result = saveFile( rdsFile );
      else if (returnVal == JOptionPane.NO_OPTION)
         result = true;

      return result;
   }

   /**
    * Creates the GUI and displays it.  For thread safety, this method
    * should be invoked from the event-dispatching thread.
    */
   private static void createAndShowGUI() {
      RDSGraphicsEditor editorFrame = new RDSGraphicsEditor();

      // display the window, centered on the screen
      editorFrame.pack();
      editorFrame.setLocationRelativeTo( null );
      editorFrame.setVisible( true );
   }

   /**
    * The application entry point.
    * 
    * @param   args  a list of command-line arguments
    */
   public static void main( String... args ) {
      // create the GUI on the event-dispatching thread
      SwingUtilities.invokeLater( new Runnable() {
         @Override
         public void run() {
            createAndShowGUI();
         }
      } );
   }

}  /* end RDSGraphicsEditor class */
