package termApp.BigScreen;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ScrollBoxConstructor;
import termApp.util.TermActionObject.OnActionListener;

public class BigScreenTruck
extends AbstractBigScreen {

   private TextField truckNumberText, doorText, oldDoorText, titleText;
   
   private ScrollBox changeList, textList;

   private int SCREEN_WIDTH = 1920;
   private int SCREEN_HEIGHT = 1080;
   private int GAP_WIDTH = 1920/12;
   private int GAP_HEIGHT = 1080/12;
   private int OFFSET_WIDTH = GAP_WIDTH/2;
   private int OFFSET_HEIGHT = GAP_HEIGHT/4;

   private int count = 0;
   private int size = 3;
   
   public BigScreenTruck(TerminalDriver term) {
      super(term);
   }

   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      count++;

      if (count >= 100) {
         updateList();
         count = 0;
      }
   }

   /*
    * display methods
    */

   @Override
   public void initDisplay() {
      super.initDisplay();

      //Draw default screen
      titleText = new TextField(SCREEN_WIDTH/2, OFFSET_HEIGHT * 2, 90, "Daily Truck Changes", Align.CENTER);
      term.setRectangle(1, 0, 0, SCREEN_WIDTH, GAP_HEIGHT * 2, "gray", 3, "black");
      term.setRectangle(2, GAP_WIDTH * 2, GAP_HEIGHT * 3, GAP_WIDTH * 8 + OFFSET_WIDTH , OFFSET_HEIGHT/8, "black", 0, "black");
      //term.setRectangle(3, SCREEN_WIDTH/2 - GAP_WIDTH - OFFSET_WIDTH, GAP_HEIGHT * 3 + OFFSET_HEIGHT, OFFSET_HEIGHT/8, GAP_HEIGHT * size, "black", 0, "black");
      //term.setRectangle(4, SCREEN_WIDTH/2 + GAP_WIDTH + OFFSET_WIDTH, GAP_HEIGHT * 3 + OFFSET_HEIGHT, OFFSET_HEIGHT/8, GAP_HEIGHT * size, "black", 0, "black");


      //Draw List Headers
      truckNumberText = new TextField(SCREEN_WIDTH/2 - GAP_WIDTH * 3, GAP_HEIGHT * 2, 80, "Truck", Align.CENTER);
      doorText = new TextField(SCREEN_WIDTH/2, GAP_HEIGHT * 2, 80, "Door", Align.CENTER);
      oldDoorText = new TextField(SCREEN_WIDTH/2 + GAP_WIDTH * 3, GAP_HEIGHT * 2, 80, "Old Door", Align.CENTER);
      
      initList();
      updateList();
   }
   
   private void initList() {
      ScrollBoxConstructor sbModel = new ScrollBoxConstructor(0,GAP_HEIGHT * 3 + OFFSET_HEIGHT/2);
      sbModel.setFont(70,70);
      sbModel.drawHeaders(false);
      sbModel.setRows(8, 20);
      sbModel.setButton(0, 9999, 9999);
      sbModel.addColumn(SCREEN_WIDTH/2 - GAP_WIDTH * 3, GAP_HEIGHT * 4, Align.CENTER, "truckNumber", "truckNumber");
      sbModel.addColumn(SCREEN_WIDTH/2, GAP_HEIGHT * 4, Align.CENTER, "door", "door");
      sbModel.addColumn(SCREEN_WIDTH/2 + GAP_WIDTH * 3, GAP_HEIGHT * 4, Align.CENTER, "oldDoor", "oldDoor");
      changeList = sbModel.build();
      changeList.hide();
   }
   
   private void updateList() {
      List<Map<String,String>> buildList = getChangeList();
      changeList.updateDisplayList(buildList);
      changeList.show();    
    
   }
   
}
