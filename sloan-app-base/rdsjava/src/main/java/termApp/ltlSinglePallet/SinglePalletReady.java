package termApp.ltlSinglePallet;

import termApp.util.*;
import termApp.util.InfoBox.InfoBoxConstructor;
import term.TerminalDriver;
import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import termApp.util.TermActionObject.*;

public class SinglePalletReady extends AbstractLtlSinglePallet {

   int tick = 0;
   protected InfoBoxConstructor infoBoxModel;
   protected InfoBox[] infos = new InfoBox[2];
   
   Rectangle[] rects = new Rectangle[2];
   Button[] availableButtons = new Button[2];
   Button[] confirmButtons = new Button[2];
   Button[] cancelButtons = new Button[2];
   List<Map<String,String>> locations;
   

   public SinglePalletReady( TerminalDriver term ) {
      super( term );
     setLogoutAllowed(true); 
   }

   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      initInfoBox();
      initGui();
   }
   
   protected void initInfoBox() {
      infoBoxModel = new InfoBoxConstructor(0,INFO_Y);
      infoBoxModel.setFont(60, 50);
      infoBoxModel.setWidth(W1_2);
      infoBoxModel.setWidthRatio(0.5);
      infoBoxModel.setMargins(0, 0, 50, 50);
      infoBoxModel.setRows(5, 10);
      infos[0] = infoBoxModel.build();
      infoBoxModel.setOrigin(W1_2, INFO_Y);
      infos[1] = infoBoxModel.build();
   }
   
   private void initGui() {
   	for( int i=0;i<2;i++ ) {
   		int dx = W1_2*i;
   		int index = i;
   		availableButtons[i] = new Button( 350+dx, 800, "Available", Align.LEFT,400);
   		availableButtons[i].registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
            	confirmAvailable( index );
            }
         });
   		confirmButtons[i] = new Button( 50+dx, 800, "Confirm", Align.LEFT,350);
   		confirmButtons[i].registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
            	markAvailable( index );
	            updateStatus();
            }
         }); 
   		
   		cancelButtons[i] = new Button( 900+dx, 800, "Cancel", Align.RIGHT,300);
   		cancelButtons[i].registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
         		cancelAvailable( index );
            }
         });    		
   		rects[i] = new Rectangle(25 + dx, INFO_Y, W1_2-30, 750, COLOR_BLUE, 2, COLOR_BLUE, true );
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
   
   private void confirmAvailable( int i ) {
   	if( availableButtons[i].on() ) {
	   	availableButtons[i].hide();
	   	confirmButtons[i].show();
	   	cancelButtons[i].show();
      	rects[i].setFill(COLOR_RED);
   		rects[i].show();	
   	}
   }
   
   private void cancelAvailable( int i ) {
   	if( cancelButtons[i].on() ) {
	   	confirmButtons[i].hide();
	   	cancelButtons[i].hide();
	   	availableButtons[i].show();
	   	updateStatus();
   	}
   }
   
   private void markAvailable( int i ) {
   	if( confirmButtons[i].on() ) {
	   	Map<String,String> m = locations.get(i);
	   	doMarkAvailable( m );
	   	confirmButtons[i].hide();
	   	cancelButtons[i].hide();
	   	availableButtons[i].show();
	   	updateStatus();
   	}
   }


   void updateStatus(){
   	locations = getSinglePalletLocationDetails();
   	for( int i=0;i<2;i++ ) {
   		if( confirmButtons[i].on() ) continue;
   		Map<String,String> m = locations.get(i);
   		String location = getMapStr(m,"location");
   		String lpn = getMapStr(m,"lpn");
   		String orderId = getMapStr(m,"refValue");
   		String lastPositionLogical = getMapStr(m,"lastPositionLogical");
   		String pickStartStamp = getMapStr(m,"pickStartStamp");
   		String pickEndStamp = getMapStr(m,"pickEndStamp");
   		String status = orderId.isEmpty()?"Available":
   			pickStartStamp.isEmpty()?"Order Assigned":
				pickEndStamp.isEmpty()?"Order Picking":
				!lastPositionLogical.equals(location)?"Order Picked":"Order Dropped";
         infos[i].updateInfoPair(0, location, status );
         infos[i].updateInfoPair(1, "Order", orderId );
         infos[i].updateInfoPair(2, "LPN", lpn );
         availableButtons[i].display(lastPositionLogical.equals(location));
         if( !confirmButtons[i].on() ) {
            String color = 
					orderId.isEmpty()?COLOR_BLUE:
					!lastPositionLogical.equals(location)?COLOR_YELLOW:COLOR_GREEN;
         	rects[i].setFill(color);
      		rects[i].show();	
         }
   	}
   }




}
