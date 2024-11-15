package termApp.palletBuild;

import termApp.util.*;
import static rds.RDSLog.*;
import term.TerminalDriver;
import static termApp.util.Constants.*;

import java.util.Map;
import java.util.List;


public class PalletBuildStation extends AbstractPalletBuild {

   String stationName = "";
   String orderId = "";
   String name = "";

   Map<String,String> orderData;

   TextField order;
   TextField customer;
   TextWrap specialInstructionsDisplay;
   TextField palletMaxCube;

   TextField shortsNoticeDisplay;
   Rectangle shortsNoticeRect;

   int tick = 0;

   //Pallet
   TextField fullPallets;
   int pickedPallets = 0;
   int palletizedPallets = 0;
   int allPallets = 0;

   //Bulk
   TextField oversizeCases;
   int pickedOversizeCases = 0;
   int palletizedOversizeCases = 0;
   int allOversizeCases = 0;

   //Amr-bulk
   TextField amrCases;
   TextField amrCasesPalletized;
   int pickedAmrCases = 0;
   int palletizedAmrCases = 0;
   int allAmrCases = 0;

   //Split case
   TextField splitCaseCases;
   int pickedSplitCaseCases = 0;
   int palletizedSplitCaseCases = 0;
   int allSplitCaseCases = 0;
   

   public PalletBuildStation( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(true); 
   }




   /*
    * interface methods
    */

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("PALLET BUILD STATION");
      footer.show();

      stationName = getParam("stationName");
      header.updateTitle("PALLET BUILD STATION #" + stationName.substring(13));

      orderId = db.getString("","SELECT assignmentValue FROM rdsLocations WHERE location = '" + stationName + "' " + 
                                 "AND locationType = 'consolidateCell' AND assignmentType = 'orderId'");

      inform("Order Id: " + orderId);

      orderData = db.getRecordMap("SELECT o.orderId, o.shipmentId, s.shipToShippingInfoSeq FROM custOrders o "+
                                                                              "JOIN custShipments s USING (shipmentId)" + 
                                                                              "WHERE o.orderId = '%s' GROUP BY orderId", orderId);

      name = db.getString("", "SELECT name FROM custShippingInfo WHERE shipInfoSeq = " + orderData.get("shipToShippingInfoSeq"));

      order = new TextField( 50, 150, 60, "Order: " + orderId, true );
      customer = new TextField( 50, 225,60, "Customer: " + name , true );
      TextField specialInstructionsHeader = new TextField( 50, 325, 60, "Special Instructions:", true );

      //Special Instructions
      List<String> specialInstructionsList = db.getValueList("SELECT UNIQUE dataValue FROM custOrderData WHERE orderId = '" + orderId + "' AND datatype='requirement';");
      String specialInstructionsString = "";

      for(String instruction : specialInstructionsList) {
         specialInstructionsString += instruction + " \n ";
      }

      specialInstructionsDisplay = new TextWrap(50,400, 2000, 50, 5, "");
      specialInstructionsDisplay.wrap(specialInstructionsString);

      //@TODO
      //This needs to be changed as right now it just uses place holder text
      //palletMaxCube = new TextField( 50, 650, 80,       "Pallet Max Cube:       ? ", true ); //we need to figure out how we are going to get this value


      //These are the 4 carton types that we need to display, hide them if they have no values to display
      //Pallet
      fullPallets = new TextField( 50, 750, 70, "Full pallets: " + pickedPallets + "/" + palletizedPallets + "/" + allPallets, true );
      //Bulk
      oversizeCases = new TextField( 50, 850, 70, "Oversize cases: " + pickedOversizeCases + "/" + palletizedOversizeCases + "/" + allOversizeCases, true );
      //Amr-bulk
      amrCases = new TextField(1000, 750, 70, "AMR cases: " + pickedAmrCases + "/" + palletizedAmrCases + "/" + allAmrCases, true );
      //amrCases = new TextField(1000, 750, 70, "AMR cases: " + pickedAmrCases + "/" + allAmrCases, true );
      //amrCasesPalletized = new TextField(1100, 850, 60, "Palletized: " + palletizedAmrCases, true );
      //Split case
      splitCaseCases = new TextField(1000, 850, 70, "Splitcase: " + pickedSplitCaseCases + "/" + palletizedSplitCaseCases + "/" + allSplitCaseCases, true );

      shortsNoticeDisplay = new TextField(1400, 175, 60, TEXT_COLOR, "**SHORTS**", Align.LEFT, false );
      shortsNoticeRect = new Rectangle(1375,150,475,125,COLOR_RED,1,TEXT_COLOR,false);
   }

   public void handleTick() {
      super.handleTick();
      tick++;

      //Every 2.5 seconds
      if(tick % 5 == 0) {
         updateQtyValues();
         updateOrderInformation();
         checkForShorts();
      }

      //Reset tick counter
      if(tick > 1000000) {
         tick = 0;
      }

   }


   public void updateQtyValues(){

      /** Pallet **/
      /**********************************************************************************************************************************/
      pickedPallets = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'pallet' AND pickStamp IS NOT NULL AND cancelStamp IS NULL;");
      palletizedPallets = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'pallet' AND palletStamp IS NOT NULL AND cancelStamp IS NULL;");
      allPallets  = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'pallet' AND cancelStamp IS NULL;");

      fullPallets.updateText("Full pallets: " + pickedPallets + "/" + palletizedPallets + "/" + allPallets);

      if(allPallets == 0) fullPallets.hide();
      else fullPallets.show();

      /** Bulk **/
      /**********************************************************************************************************************************/
      pickedOversizeCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'bulk' AND pickStamp IS NOT NULL AND cancelStamp IS NULL;");
      palletizedOversizeCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'bulk' AND palletStamp IS NOT NULL AND cancelStamp IS NULL;");
      allOversizeCases  = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'bulk' AND cancelStamp IS NULL;;");
      
      oversizeCases.updateText("Oversize cases: " + pickedOversizeCases + "/" + palletizedOversizeCases + "/" + allOversizeCases);

      if(allOversizeCases == 0) oversizeCases.hide();
      else oversizeCases.show();

      /** Amr-bulk **/
      /**********************************************************************************************************************************/
      pickedAmrCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'amr-bulk' AND pickStamp IS NOT NULL AND cancelStamp IS NULL;");
      palletizedAmrCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'amr-bulk' AND palletStamp IS NOT NULL AND cancelStamp IS NULL;");
      allAmrCases  = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'amr-bulk' AND cancelStamp IS NULL;");

      amrCases.updateText("AMR cases: " + pickedAmrCases + "/" + palletizedAmrCases + "/" + allAmrCases);
      //amrCases.updateText("AMR cases: " + pickedAmrCases + "/" + allAmrCases);
      //amrCasesPalletized.updateText("Palletized: " + palletizedAmrCases);

      if(allAmrCases == 0) amrCases.hide();
      else amrCases.show();

      /** Split case **/
      /**********************************************************************************************************************************/
      pickedSplitCaseCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'splitCase' AND pickStamp IS NOT NULL AND cancelStamp IS NULL;");
      palletizedSplitCaseCases = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'splitCase' AND palletStamp IS NOT NULL AND cancelStamp IS NULL;");
      allSplitCaseCases  = db.getInt(0,"SELECT COUNT(*) FROM rdsCartons WHERE orderId = '" + orderId + "' AND pickType = 'splitCase' AND cancelStamp IS NULL;");

      splitCaseCases.updateText("Splitcase: " + pickedSplitCaseCases + "/" + palletizedSplitCaseCases + "/" + allSplitCaseCases);

      if(allSplitCaseCases == 0) splitCaseCases.hide();
      else splitCaseCases.show();
      
      /**********************************************************************************************************************************/
   }

   public void updateOrderInformation() {

      orderId = db.getString("","SELECT assignmentValue FROM rdsLocations WHERE location = '" + stationName + "' " + 
                                 "AND locationType = 'consolidateCell' AND assignmentType = 'orderId'");

      inform("Order Id: " + orderId);

      orderData = db.getRecordMap("SELECT o.orderId, o.shipmentId, s.shipToShippingInfoSeq FROM custOrders o "+
                                                                              "JOIN custShipments s USING (shipmentId)" + 
                                                                              "WHERE o.orderId = '%s' GROUP BY orderId", orderId);

      name = db.getString("", "SELECT name FROM custShippingInfo WHERE shipInfoSeq = " + orderData.get("shipToShippingInfoSeq"));

      order = new TextField( 50, 150, 60, "Order: " + orderId, true );
      customer = new TextField( 50, 225,60, "Customer: " + name , true );
      TextField specialInstructionsHeader = new TextField( 50, 325, 60, "Special Instructions:", true );

      //Special Instructions
      List<String> specialInstructionsList = db.getValueList("SELECT UNIQUE dataValue FROM custOrderData WHERE orderId = '" + orderId + "' AND datatype='requirement';");
      String specialInstructionsString = "";

      for(String instruction : specialInstructionsList) {
         specialInstructionsString += instruction + " \n ";
      }

      //specialInstructionsDisplay = new TextWrap(50,400, 500, 50, 5, "");
      specialInstructionsDisplay.wrap(specialInstructionsString);

      //@TODO
      //This needs to be changed as right now it just uses place holder text
      //palletMaxCube.updateText("Pallet Max Cube:       ? "); //we need to figure out how we are going to get this value
   }

   private void checkForShorts() {
      int numberOfShorts = db.getInt(0, "SELECT COUNT(*) FROM rdsCartons WHERE shortStamp IS NOT NULL AND orderId = '" + orderId + "';");

      //We have shorts, display the shorts boxc
      if(numberOfShorts > 0){
         shortsNoticeDisplay.show();
         shortsNoticeRect.show();
      }
      //No shorts in this order, hide the box
      else {
         shortsNoticeDisplay.hide();
         shortsNoticeRect.hide();
      }
   }



}
