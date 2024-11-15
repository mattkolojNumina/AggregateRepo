
package rdsGraphics;

import java.awt.Color;
import java.sql.*;
import java.util.*;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import rds.RDSDatabase;
import rds.RDSUtil;


public class RDSGraphicsDisplayPanel
      extends RDSGraphicsPanel {
   public static final int REFRESH_DELAY = 1000;  // msec

   private String area;
   private String rdsFileName;
   private RDSDatabase db;
   private String lastUpdate;

   private Map<String,List<RDSObject>> objectMap;
   private boolean initialViewRequired;

   // add an activity to periodically update the panel
   PActivity update = new PActivity( -1, REFRESH_DELAY,
         System.currentTimeMillis() ) {
      protected void activityStep( long elapsedTime ) {
         super.activityStep( elapsedTime );
         updatePanel();
      }
   };

   public RDSGraphicsDisplayPanel( String rdsFileName, RDSDatabase db ) {
      this( "main", rdsFileName, db );
   }

   public RDSGraphicsDisplayPanel( String area, String rdsFileName,
         RDSDatabase db ) {
      super();
      setName( "Graphics Display" );
      this.area = area;
      this.rdsFileName = rdsFileName;
      this.db = db;
      this.lastUpdate = "0000-00-00 00:00:00";
      this.initialViewRequired = true;
   }

   @Override
   public void initialize() {
      super.initialize();
      setBackground( Color.WHITE );

      // zoom to a double-clicked node
      getCanvas().getCamera().addInputEventListener(
            new PBasicInputEventHandler() {
               public void mouseClicked( PInputEvent evt ) {
                  if (evt.isLeftMouseButton() &&
                        evt.getClickCount() == 2 &&
                        evt.getPickedNode() instanceof RDSObject) {
                     zoomToNode( (RDSObject)evt.getPickedNode() );
                  }
               }
            } );

      // add objects from file, reset update timestamp when done
      readFile( rdsFileName );
      lastUpdate = "0000-00-00 00:00:00";

      populateObjectMap();
      startUpdate();
   }

   public void startUpdate() {
      getCanvas().getRoot().addActivity( update );
   }

   public void stopUpdate() {
      update.terminate();
   }

   public void updatePanel() {
      if (initialViewRequired &&
            !getCanvas().getCamera().getViewBounds().isEmpty() &&
            !nodeLayer.getUnionOfChildrenBounds( null ).isEmpty()) {
         zoomToFitAll();
         initialViewRequired = false;
      }

      String sql = String.format(
            "SELECT name, value, hint FROM webObjects " +
            "WHERE area = '%s' " +
            "AND stamp >= '%s'",
            area, lastUpdate );
      lastUpdate = db.getValue( "SELECT NOW()", "0000-00-00 00:00:00" );
      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = db.executeTimedQuery( stmt, sql );
         while (res.next()) {
            String name = res.getString( "name" );
            String value = res.getString( "value" );
            String hint = res.getString( "hint" );

            List<RDSObject> rdsObjList = getObjectList( name );
            for (RDSObject rdsObj : rdsObjList) {
               rdsObj.setValue( value );
               rdsObj.setHint( hint );
            }
         }
         stmt.close();
      } catch (SQLException ex) {
         RDSUtil.alert( "sql error updating web objects" );
         RDSUtil.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }

      List<RDSObject> stampObjList = getObjectList( "stamp" );
      for (RDSObject stampObj : stampObjList)
         stampObj.setValue( lastUpdate );
   }

   private void zoomToNode( RDSObject node ) {
      PCamera camera = getCanvas().getCamera();
      PBounds camBounds = camera.getBounds();
      PBounds nodeBounds = node.getGlobalBounds();
      double width =  nodeBounds.getWidth();
      double height = nodeBounds.getHeight();

      if (maxZoom < Double.MAX_VALUE) {
         width =  Math.max( width,  camBounds.getWidth()  / maxZoom );
         height = Math.max( height, camBounds.getHeight() / maxZoom );
      }
      if (minZoom > 0.0) {
         width =  Math.min( width,  camBounds.getWidth()  / minZoom );
         height = Math.min( height, camBounds.getHeight() / minZoom );
      }

      // center on node with scale determined by panel settings
      PBounds zoomBounds = new PBounds( nodeBounds.getCenterX() - width / 2,
            nodeBounds.getCenterY() - height / 2, width, height );

      camera.animateViewToCenterBounds( zoomBounds, true, ZOOM_DURATION );
   }

   private void populateObjectMap() {
      objectMap = new HashMap<String,List<RDSObject>>();
      for (Object obj : nodeLayer.getChildrenReference()) {
         if (obj instanceof RDSObject) {
            RDSObject rdsObj = (RDSObject)obj;
            String name = (String)rdsObj.getAttribute( "name" );
            List<RDSObject> objList = getObjectList( name );
            objList.add( rdsObj );
            objectMap.put( name, objList );
         }
      }
   }

   private List<RDSObject> getObjectList( String name ) {
      List<RDSObject> objList = objectMap.get( name );
      if (objList == null)
         return new ArrayList<RDSObject>();
      return objList;
   }
}  /* end RDSGraphicsDisplayPanel class */
