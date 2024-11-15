/*
 * RDSGraphicsEditorPanel.java
 * 
 * (c) 2006-2008 Numina Group, Inc.
 */
package rdsGraphicsEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.handles.PBoundsHandle;

import rds.*;
import rdsGraphics.*;


public class RDSGraphicsEditorPanel
      extends RDSGraphicsPanel {
   private static final float SELECTION_TRANSPARENCY = 0.1f;
   private static final float HANDLE_SIZE = 1.0f;
   
   private ArrayList<RDSObject> selectedObjects;
   private PPath selectionRectangle;
   private boolean dirty;

   /**
    * Constructs a new editor panel object.
    */
   public RDSGraphicsEditorPanel() {
      super();
      selectedObjects = new ArrayList<RDSObject>();
      dirty = false;
   }

   /**
    * Initializes the editor panel.  This method is called on the
    * event-dispatching thread when the editor panel is constructed.
    * All Piccolo-related initialization code should occur in this
    * method. 
    */
   public void initialize() {
      super.initialize();
      selectionRectangle = PPath.createRectangle(0, 0, 1, 1);
      selectionRectangle.setPaint( Color.BLUE );
      selectionRectangle.setTransparency( SELECTION_TRANSPARENCY );
      selectionRectangle.resetBounds();
      selectionRectangle.setVisible( false );
      nodeLayer.getCamera( 0 ).addChild( selectionRectangle );

      // change default zoom/pan handlers to use ctrl+drag
      getCanvas().getPanEventHandler().getEventFilter().setAndMask(
            InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK );
      getCanvas().getZoomEventHandler().getEventFilter().setAndMask(
            InputEvent.BUTTON3_MASK | InputEvent.CTRL_MASK );

      // add an event handler for node management, ignore ctrl key events
      BasicInputHandler handler = new BasicInputHandler();
      handler.getEventFilter().setNotMask( InputEvent.CTRL_MASK );
      getCanvas().addInputEventListener( handler );
   }

   /**
    * Determines whether or not an object is currently selected.
    * @param obj the object
    * @return true if the object is currently selected, false otherwise
    */
   private boolean isSelected( RDSObject obj ) {
      for (RDSObject selectedObj : selectedObjects)
         if (selectedObj == obj)
            return true;
      return false;
   }

   /**
    * Clears all objects from the list of selected objects.
    */
   public void clearSelectedObjects() {
      for (RDSObject selectedObj : selectedObjects)
         PBoundsHandle.removeBoundsHandlesFrom( selectedObj );
      selectedObjects.clear();
   }

   /**
    * Sets an object as the only selected object.
    * @param obj the object to select
    */
   private void setSelected( RDSObject obj ) {
      if (!obj.isLocked()) {
         clearSelectedObjects();
         addToSelected( obj );
      }
   }

   /**
    * Adds an object to the list of selected objects.
    * @param obj the object to add
   */
   private void addToSelected( RDSObject obj ) {
      if (!obj.isLocked() && !isSelected( obj )) {
         selectedObjects.add( obj );

         // add resize handles to the object
         PBoundsHandle.addBoundsHandlesTo( obj );
         Iterator<?> i = obj.getChildrenIterator();
         while (i.hasNext()) {
            PNode child = (PNode)i.next();
            if (child instanceof PBoundsHandle) {
               PBoundsHandle handle = (PBoundsHandle)child;
               handle.setPathToEllipse(
                     0.0f, 0.0f, HANDLE_SIZE, HANDLE_SIZE );
               handle.setPaint( Color.BLACK );
               handle.relocateHandle();
            }
         }
      }
   }

   /**
    * Removes an object from the list of selected objects.
    * @param obj the object to remove
    */
   private void removeFromSelected( RDSObject obj ) {
      selectedObjects.remove( obj );
      PBoundsHandle.removeBoundsHandlesFrom( obj );
   }

   /**
    * Returns the status of the "dirty" flag, indicating that changes
    * have been made since the file was last saved.
    * @return the state of the "dirty" flag
    */
   public boolean isDirty() {
      return dirty;
   }

   /**
    * Opens the selected file, creating objects as the parameter information
    * for each one is read.
    * @param file the file to open
    */
   public void openFile( File file ) {
      super.openFile( file );
      dirty = false;
   }

   /**
    * Writes parameter information for all objects to the specified file.
    * 
    * @param   file  the file to save
    */
   public boolean saveFile( File file ) {
      boolean result = false;

      try {
         BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
         out.write( "# " + file.getName() );
         out.newLine();
         out.write( "#" );
         out.newLine();
         if (minZoom > 0) {
            out.write( "#!minzoom = " + minZoom );
            out.newLine();
         }
         if (maxZoom < Double.MAX_VALUE) {
            out.write( "#!maxzoom = " + maxZoom );
            out.newLine();
         }
         for (Object obj : nodeLayer.getChildrenReference()) {
            if (obj instanceof RDSObject) {
               out.newLine();
               ((RDSObject)obj).serialize( out );
            }
         }
         out.close();
         dirty = false;
         result = true;
      } catch (IOException ex) {
         RDSUtil.alert( "error saving file [" + file + "]" );
         RDSUtil.alert( ex );
      }

      return result;
   }

   /**
    * Adds all rds objects to the list of selected objects.
    */
   public void selectAll() {
      for (Object obj : nodeLayer.getChildrenReference())
         if (obj instanceof RDSObject)
            addToSelected( (RDSObject)obj );
   }

   /**
    * Locks each object that is currently selected.
    */
   public void lock() {
      for (RDSObject obj : selectedObjects)
            obj.setLocked( true );
         clearSelectedObjects();
   }

   /**
    * Unlocks all objects.
    */
   public void unlockAll() {
      for (Object obj : nodeLayer.getChildrenReference())
         if (obj instanceof RDSObject)
            ((RDSObject)obj).setLocked( false );
   }

   /**
    * Removes the selected objects from the canvas.
    */
   public void deleteObjects() {
      for (RDSObject obj : selectedObjects) {
         nodeLayer.removeChild( obj );
         dirty = true;
      }
      selectedObjects.clear();
   }

   /**
    * Changes the selected objects to be drawn in front of other objects.
    */
   public void moveToFront() {
      for (RDSObject obj : selectedObjects) {
         obj.moveToFront();
         dirty = true;
      }
   }

   /**
    * Changes the selected objects to be drawn behind other objects.
    */
   public void moveToBack() {
      for (RDSObject obj : selectedObjects) {
         obj.moveToBack();
         dirty = true;
      }
   }

   /**
    * Aligns selected objects vertically to have the same left edge.
    */
   public void alignLeft() {
      double alignPosition = Double.MAX_VALUE;
      for (RDSObject obj : selectedObjects)
         alignPosition = Math.min( alignPosition,
               Double.parseDouble( obj.getParamValue( "x" ) ) );
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "x", Double.toString( alignPosition ) );
         dirty = true;
      }
   }

   /**
    * Aligns selected objects vertically to have the same centers.
    */
   public void alignCenter() {
      double alignPosition = 0.0;
      for (RDSObject obj : selectedObjects)
         alignPosition += Double.parseDouble( obj.getParamValue( "x" ) ) +
               Double.parseDouble( obj.getParamValue( "width" ) ) / 2;
      alignPosition /= selectedObjects.size();
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "x", Double.toString( alignPosition -
               Double.parseDouble( obj.getParamValue( "width" ) ) / 2 ) );
         dirty = true;
      }
   }

   /**
    * Aligns selected objects to have the same right edge.
    */
   public void alignRight() {
      double alignPosition = -Double.MAX_VALUE;
      for (RDSObject obj : selectedObjects)
         alignPosition = Math.max( alignPosition,
               Double.parseDouble( obj.getParamValue( "x" ) ) +
               Double.parseDouble( obj.getParamValue( "width" ) ) );
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "x", Double.toString( alignPosition -
               Double.parseDouble( obj.getParamValue( "width" ) ) ) );
         dirty = true;
      }
   }

   /**
    * Aligns selected objects vertically to have the same left edge.
    */
   public void alignTop() {
      double alignPosition = Double.MAX_VALUE;
      for (RDSObject obj : selectedObjects)
         alignPosition = Math.min( alignPosition,
               Double.parseDouble( obj.getParamValue( "y" ) ) );
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "y", Double.toString( alignPosition ) );
         dirty = true;
      }
   }

   /**
    * Aligns selected objects vertically to have the same centers.
    */
   public void alignMiddle() {
      double alignPosition = 0.0;
      for (RDSObject obj : selectedObjects)
         alignPosition += Double.parseDouble( obj.getParamValue( "y" ) ) +
               Double.parseDouble( obj.getParamValue( "height" ) ) / 2;
      alignPosition /= selectedObjects.size();
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "y", Double.toString( alignPosition -
               Double.parseDouble( obj.getParamValue( "height" ) ) / 2 ) );
         dirty = true;
      }
   }

   /**
    * Aligns selected objects to have the same right edge.
    */
   public void alignBottom() {
      double alignPosition = -Double.MAX_VALUE;
      for (RDSObject obj : selectedObjects)
         alignPosition = Math.max( alignPosition,
               Double.parseDouble( obj.getParamValue( "y" ) ) +
               Double.parseDouble( obj.getParamValue( "height" ) ) );
      for (RDSObject obj : selectedObjects) {
         obj.setParamValue( "y", Double.toString( alignPosition -
               Double.parseDouble( obj.getParamValue( "height" ) ) ) );
         dirty = true;
      }
   }

   public void resetZoom() {
      resetZoom( ZOOM_DURATION );
   }

   public void resetZoom( long duration ) {
      getCanvas().getCamera().animateViewToTransform(
            new AffineTransform(), duration );
   }

   public void zoomToFitSelected() {
      zoomToFitSelected( ZOOM_DURATION );
   }

   public void zoomToFitSelected( long duration ) {
      PBounds selectedBounds = new PBounds();
      for (RDSObject obj : selectedObjects)
         selectedBounds.add( obj.getFullBounds() );
      getCanvas().getCamera().animateViewToCenterBounds(
            selectedBounds, true, duration );
   }

   /**
    * Adds an object to the canvas via the overridden superclass method.
    * Also, the newly added object is centered in the view and selected.
    */
   public RDSObject addObject( RDSObject obj ) {
      RDSObject newObj = super.addObject( obj );

      PBounds b = getCanvas().getCamera().getViewBounds();
      newObj.translate( b.getX() + (b.getWidth() - newObj.getWidth()) / 2,
            b.getY() + (b.getHeight() - newObj.getHeight()) / 2);

      setSelected( newObj );
      dirty = true;
      return newObj;
   }

   /**
    * Clears the canvas of all objects.
    */
   public void clearPanel() {
      clearSelectedObjects();
      nodeLayer.removeAllChildren();
      clearMinZoom();
      clearMaxZoom();
      resetZoom( 0 );
      dirty = false;
   }

   /**
    * Displays a dialog for modifying the selected RDS Object.
    * 
    * @param   rdsObject  the object to modify
    * @return  {@code true} if the object was modified, {@code false}
    *          otherwise
    */
   private boolean showParameterDialog( RDSObject rdsObject ) {
      final int HGAP = 5;
      final int VGAP = 2;
      final Font TITLE_FONT = UIManager.getFont( "Panel.font" ).deriveFont(
            Font.BOLD, 12.0f );

      if (rdsObject == null)
         return false;

      String[] paramNames = rdsObject.getParamNames();
      int numParams = paramNames.length;
      if (numParams == 0)
         return false;
      JTextField[] paramFields = new JTextField[ numParams ];

      JPanel paramPanel = new JPanel( new SpringLayout() );
      JLabel paramTitleLabel = new JLabel( "Parameter", JLabel.CENTER );
      paramTitleLabel.setFont( TITLE_FONT );
      paramPanel.add( paramTitleLabel );
      JLabel valueTitleLabel = new JLabel( "Value", JLabel.CENTER );
      valueTitleLabel.setFont( TITLE_FONT );
      paramPanel.add( valueTitleLabel );
      for (int i = 0; i < numParams; i++) {
         JLabel paramLabel = new JLabel( paramNames[i], JLabel.CENTER );
         paramPanel.add( paramLabel );

         paramFields[i] = new JTextField( rdsObject.getParamValue(
               paramNames[i] ) );
         paramFields[i].setPreferredSize( new Dimension( 100, 18 ) );
         paramFields[i].setHorizontalAlignment( JTextField.CENTER );
         paramFields[i].addFocusListener( new FocusAdapter() {
            public void focusGained( FocusEvent evt ) {
               ((JTextField)evt.getSource()).selectAll();
            }
         } );
         paramPanel.add( paramFields[i] );
      }
      SpringUtilities.makeCompactGrid( paramPanel, numParams + 1, 2,
            0, 0, HGAP, VGAP );

      JPanel containerPanel = new JPanel(
            new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
      containerPanel.add( paramPanel );

      JCheckBox lockedCheckbox = new JCheckBox( "Object locked" );
      lockedCheckbox.setSelected( rdsObject.isLocked() );
      lockedCheckbox.setFocusable( false );
      JPanel controlPanel = new JPanel();
      controlPanel.add( lockedCheckbox );


      JPanel layoutPanel = new JPanel( new BorderLayout() );
      layoutPanel.add( containerPanel, BorderLayout.CENTER );
      layoutPanel.add( controlPanel, BorderLayout.SOUTH );

      int returnValue = JOptionPane.showConfirmDialog(
            this,                          // parent component
            layoutPanel,                   // message object
            "Edit Object Parameters",      // dialog title
            JOptionPane.OK_CANCEL_OPTION,  // option type
            JOptionPane.PLAIN_MESSAGE      // message type
            );

      if (returnValue != JOptionPane.OK_OPTION)
         return false;

      // apply the parameters
      for (int i = 0; i < numParams; i++)
         rdsObject.setParamValue( paramNames[i], paramFields[i].getText() );
      rdsObject.setLocked( lockedCheckbox.isSelected() );

      // set hint to name by default
      if ("".equals( rdsObject.getParamValue( "hint" ) ))
         rdsObject.setParamValue( "hint", rdsObject.getParamValue( "name" ) );

      return true;
   }

   /**
    * Inner class to handle input events.
    */
   class BasicInputHandler
         extends PBasicInputEventHandler {
      private Point2D selectionOrigin;
      private boolean selecting;

      public BasicInputHandler() {
         super();
         selecting = false;
      }

      public void mousePressed( PInputEvent evt ) {
         evt.getInputManager().setKeyboardFocus( this );

         if (evt.isLeftMouseButton()) {
            PNode node = evt.getPickedNode();
            if (isValidObject( node )) {
               RDSObject currentObject = (RDSObject)node;
               if (evt.isShiftDown()) {
                  if (isSelected( currentObject ))
                     removeFromSelected( currentObject );
                  else
                     addToSelected( currentObject );
               } else if (!isSelected( currentObject ))
                  setSelected( currentObject );
            } else {
               if (!evt.isShiftDown())
                  clearSelectedObjects();
               selectionOrigin = evt.getCanvasPosition();
               selecting = true;
            }
         }
      }

      public void mouseReleased( PInputEvent evt ) {
         if (selecting) {
            PBounds selectionBounds = new PBounds(
                  getCanvas().getCamera().localToView(
                  selectionRectangle.getGlobalBounds() ) );
            for (Object obj : nodeLayer.getChildrenReference()) {
               if (obj instanceof RDSObject) {
                  RDSObject rdsObj = (RDSObject)obj;
                  if (!rdsObj.isLocked() && selectionBounds.contains(
                        rdsObj.getGlobalFullBounds() ))
                     addToSelected( rdsObj );
               }
            }
            selectionRectangle.setVisible( false );
            selectionRectangle.resetBounds();
            selecting = false;
         }
      }

      public void mouseClicked( PInputEvent evt ) {
         PNode node = evt.getPickedNode();
         if (!(node instanceof RDSObject))
            return;

         RDSObject currentObject = (RDSObject)node;
         if (evt.isLeftMouseButton()) {
            if (!evt.isShiftDown())
               setSelected( currentObject );
         } else if (evt.isRightMouseButton()) {
            if (showParameterDialog( currentObject ))
               dirty = true;
            if (currentObject.isLocked())
               removeFromSelected( currentObject );
         }
      }

      public void mouseDragged( PInputEvent evt ) {
         if (selecting) {
            PBounds bounds = new PBounds();
            bounds.add( selectionOrigin );
            bounds.add( evt.getCanvasPosition() );
            selectionRectangle.setBounds( bounds );
            selectionRectangle.setVisible( true );
            return;
         }

         PNode node = evt.getPickedNode();
         if (!isValidObject( node ))
            return;

         RDSObject currentObject = (RDSObject)node;
         if (evt.isLeftMouseButton()) {
            if (isSelected( currentObject )) {
               for (RDSObject obj : selectedObjects) {
                  obj.translate( evt.getDelta().width,
                        evt.getDelta().height );
                  dirty = true;
               }
            } else {
               currentObject.translate( evt.getDelta().width,
                     evt.getDelta().height );
               dirty = true;
            }
         }
      }

      public void keyPressed( PInputEvent evt ) {
         double delta = (evt.isShiftDown()) ? 0.1 : 1.0;
         if (evt.isAltDown())
            delta *= 10.0;

         switch (evt.getKeyCode()) {
            case KeyEvent.VK_UP:
               for (RDSObject obj : selectedObjects) {
                  obj.translate( 0, -delta );
                  dirty = true;
               }
               break;
            case KeyEvent.VK_DOWN:
               for (RDSObject obj : selectedObjects) {
                  obj.translate( 0, delta );
                  dirty = true;
               }
               break;
            case KeyEvent.VK_LEFT:
               for (RDSObject obj : selectedObjects) {
                  obj.translate( -delta, 0 );
                  dirty = true;
               }
               break;
            case KeyEvent.VK_RIGHT:
               for (RDSObject obj : selectedObjects) {
                  obj.translate( delta, 0 );
                  dirty = true;
               }
               break;
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
               deleteObjects();
               break;
         }
      }

      private boolean isValidObject( PNode node ) {
         return (node instanceof RDSObject &&
               !((RDSObject)node).isLocked());
      }
   }  /* end BasicInputHandler inner class */

}  /* end RDSGraphicsEditor class */
