
package rdsGraphics;

public class RDSParse {
   private static RDSObject rdsObject;

   public static void parseLine( String configLine, RDSGraphicsPanel panel ) {
      if (configLine == null || configLine.length() == 0)
         return;

      // parse global options
      if (configLine.charAt( 0 ) == '#') {
         if (configLine.contains( "!nozoom" )) {
            panel.getCanvas().setZoomEventHandler( null );
         }
         if (configLine.contains( "!nopan" )) {
            panel.getCanvas().setPanEventHandler( null );
         }
         if (configLine.contains( "!minzoom" )) {
            String value = configLine.split( "=", 2 )[1].trim();
            panel.setMinZoom( Double.parseDouble( value ) );
         }
         if (configLine.contains( "!maxzoom" )) {
            String value = configLine.split( "=", 2 )[1].trim();
            panel.setMaxZoom( Double.parseDouble( value ) );
         }
      }

      // parse object type
      if (configLine.charAt( 0 ) == '[') {
         String objectType = configLine.replace( "[", "" );
         objectType = objectType.replace( "]", "" );
         objectType = objectType.trim().toLowerCase();

         if (objectType.equals( "image" ))
            rdsObject = panel.addImage();
         else if (objectType.equals( "motor" ))
            rdsObject = panel.addMotor();
         else if (objectType.equals( "linefull" ))
            rdsObject = panel.addLineFull();
         else if (objectType.equals( "jam" ))
            rdsObject = panel.addJam();
         else if (objectType.equals( "estop" ))
            rdsObject = panel.addEstop();
         else if (objectType.equals( "zone" ))
            rdsObject = panel.addZone();
         else if (objectType.equals( "indicator" ))
            rdsObject = panel.addIndicator();
         else if (objectType.equals( "text" ))
            rdsObject = panel.addText();
         else if (objectType.equals( "statictext" ))
            rdsObject = panel.addStaticText();
      }

      // parse object parameters
      if (configLine.contains( "=" )) {
         String[] configArgs = configLine.split( "=", 2 );
         String parameter = configArgs[0].trim();
         String value = configArgs[1].trim();
         if (rdsObject != null)
            rdsObject.setParamValue( parameter, value );
      }
   }
}
