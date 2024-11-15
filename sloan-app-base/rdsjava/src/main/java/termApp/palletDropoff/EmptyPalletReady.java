package termApp.palletDropoff;

import termApp.util.*;
import term.TerminalDriver;
import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import termApp.util.TermActionObject.*;

public class EmptyPalletReady extends AbstractPalletDropoff {

   int tick = 0;
   Button singleDroppedOff;
   Button stackDroppedOff;
   
   Rectangle[] rects = new Rectangle[3];
   Button[] singleDroppedOffs = new Button[3];
   Button[] stackDroppedOffs = new Button[3];
   TextField[] locationStatus  = new TextField[3];
   String[] locations = new String[3];

   public EmptyPalletReady( TerminalDriver term ) {
      super( term );
     setLogoutAllowed(true); 
   }

   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      initLocations();
   }
   
   private void initLocations() {
   	List<String> palletDropoffLocations = getPalletDropOffLocations();
   	for( int i=0;i<3;i++ ) {
   		int dy = 250*i;
   		locations[i] = palletDropoffLocations.get(i);
   		String location = locations[i];
   		String message = String.format("%s:", locations[i]);
   		locationStatus[i] = new TextField(50, 225 + dy, 60, message, true);
   		stackDroppedOffs[i] = new Button( 50, 300 + dy, "Stack drop-off", Align.LEFT,600);
         stackDroppedOffs[i].registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
            	dropoffStack( location );
            	updateStatus();
            }
         });
   		singleDroppedOffs[i] = new Button( 900, 300 + dy, "Single drop-off", Align.LEFT,600);
   		singleDroppedOffs[i].registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
            	dropoffSingle( location );
            	updateStatus();
            }
         }); 
   		rects[i] = new Rectangle(25, 200 + dy, 1875, 200, COLOR_BLUE, 10, COLOR_BLUE, true );
   	}
   }  

   public void handleTick() {
      super.handleTick();
      tick++;
      if(tick % 20 == 0) { //Update every 5 second
         updateStatus();
      }
      //Reset tick counter
      if(tick > 1000000) {
         tick = 0;
      }

   }


   void updateStatus(){
   	int destackHealthy = getDestackHealthy();
   	for( int i=0;i<3;i++ ) {
   		String location = locations[i];
   		Map<String,String> m = getLocationStatus(location);
   		int enabled = getMapInt(m,"enabled");
   		String assignmentValue = getMapStr(m,"assignmentValue");
   		String message = 
   				(enabled==1 && destackHealthy>0)?
					(assignmentValue.isEmpty()?String.format("%s: Stack pallet mode, currently empty", location):
														String.format("%s: Stack pallet mode, has %s", location, assignmentValue)):
   				(assignmentValue.isEmpty()?String.format("%s: Single pallet mode, currently empty", location):
   													String.format("%s: Single pallet mode, has %s", location, assignmentValue));
   		locationStatus[i].updateText(message);
   		singleDroppedOffs[i].display( enabled==0 );
   		stackDroppedOffs[i].display( enabled==1 );
   		rects[i].setFill(assignmentValue.isEmpty()?COLOR_BLUE:COLOR_GREEN);
   		rects[i].show();
   	}
   }




}
