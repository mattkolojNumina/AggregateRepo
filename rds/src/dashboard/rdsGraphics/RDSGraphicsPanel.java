
package rdsGraphics;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

import rds.RDSUtil;


public class RDSGraphicsPanel
      extends PPanel {
   public static final int ZOOM_DURATION = 1000;  // msec

   protected PLayer nodeLayer;
   protected double minZoom, maxZoom;

   public RDSGraphicsPanel() {
      super();
      minZoom = 0.0;
      maxZoom = Double.MAX_VALUE;
   }

   @Override
   public void beforeInitialize() {
      nodeLayer = getCanvas().getLayer();
   }

   @Override
   public void initialize() {
      RDSHint hint = new RDSHint();
      PCamera rootCamera = nodeLayer.getCamera( 0 );
      rootCamera.addChild( hint );
      rootCamera.addInputEventListener( hint.getHintHandler() );
   }

   /**
    * Gets the minimum zoom value.
    */
   public double getMinZoom() {
      return minZoom;
   }

   /**
    * Sets the minimum zoom value; this will be written into the config file.
    */
   public void setMinZoom( double value ) {
      minZoom = value;
      PZoomEventHandler handler = getCanvas().getZoomEventHandler();
      if (handler != null)
         handler.setMinScale( minZoom );
   }

   /**
    * Clears the minimum zoom value; no value will be written to file.
    */
   public void clearMinZoom() {
      minZoom = 0.0;
      PZoomEventHandler handler = getCanvas().getZoomEventHandler();
      if (handler != null)
         handler.setMinScale( minZoom );
   }

   /**
    * Gets the maximum zoom value.
    */
   public double getMaxZoom() {
      return maxZoom;
   }

   /**
    * Sets the maximum zoom value; this will be written into the config file.
    */
   public void setMaxZoom( double value ) {
      maxZoom = value;
      PZoomEventHandler handler = getCanvas().getZoomEventHandler();
      if (handler != null)
         handler.setMaxScale( maxZoom );
   }

   /**
    * Clears the maximum zoom value; no value will be written to file.
    */
   public void clearMaxZoom() {
      maxZoom = Double.MAX_VALUE;
      PZoomEventHandler handler = getCanvas().getZoomEventHandler();
      if (handler != null)
         handler.setMaxScale( maxZoom );
   }

   public RDSObject addObject( RDSObject obj ) {
      if (obj != null)
         nodeLayer.addChild( obj );
      return obj;
   }

   public RDSEstop addEstop() {
      return (RDSEstop)addObject( new RDSEstop() );
   }

   public RDSImage addImage() {
      return (RDSImage)addObject( new RDSImage() );
   }

   public RDSJam addJam() {
      return (RDSJam)addObject( new RDSJam() );
   }

   public RDSLineFull addLineFull() {
      return (RDSLineFull)addObject( new RDSLineFull() );
   }

   public RDSMotor addMotor() {
      return (RDSMotor)addObject( new RDSMotor() );
   }

   public RDSStaticText addStaticText() {
      return (RDSStaticText)addObject( new RDSStaticText() );
   }

   public RDSText addText() {
      return (RDSText)addObject( new RDSText() );
   }

   public RDSZone addZone() {
      return (RDSZone)addObject( new RDSZone() );
   }

   public RDSIndicator addIndicator() {
      return (RDSIndicator)addObject( new RDSIndicator() );
   }

   public void zoomToFitAll() {
      zoomToFitAll( ZOOM_DURATION );
   }

   public void zoomToFitAll( long duration ) {
      getCanvas().getCamera().animateViewToCenterBounds(
            nodeLayer.getUnionOfChildrenBounds( null ),
            true, duration );
   }

   public void openFile( File rdsFile ) {
      URL rdsFileURL;
      try {
         rdsFileURL = rdsFile.toURI().toURL();
      } catch (MalformedURLException ex) {
         RDSUtil.alert( getName() + ": error obtaining URL from file [" +
               rdsFile.toString() + "]" );
         return;
      }
      readFile( rdsFileURL );
   }

   public void readFile( String rdsFileName ) {
      URL rdsFileURL;
      try {
         rdsFileURL = new URL( RDSObject.codeBase + rdsFileName );
      } catch (MalformedURLException e) {
         RDSUtil.alert( getName() + ": unable to open rds file [" +
               rdsFileName + "]" );
         return;
      }
      readFile( rdsFileURL );
   }

   public void readFile( URL rdsFileURL ) {
      InputStream in;
      try {
         in = rdsFileURL.openStream();
      } catch (IOException e) {
         RDSUtil.alert( getName() + ": error opening filestream for [" +
               rdsFileURL.toString() + "]" );
         return;
      }

      String configLine = "";
      while (true) {
         int data = -1;
         try {
            data = in.read();
         } catch (IOException e) {
            RDSUtil.alert( getName() + ": error reading data from [" +
                  rdsFileURL.toString() + "]" );
         }
         if (data < 0)
            break;

         if (data == '\n') {
            RDSParse.parseLine( configLine, this );
            configLine = "";
         } else
            configLine += (char)data;
      }

      try {
         in.close();
      } catch (IOException e) {
         RDSUtil.alert( getName() + ": error closing filestream for [" +
               rdsFileURL.toString() + "]" );
      }
   }
}
