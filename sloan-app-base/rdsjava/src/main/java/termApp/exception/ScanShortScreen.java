package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.Map;

import term.TerminalDriver;
import termApp.util.*;

public class ScanShortScreen
      extends AbstractExceptionStationScreen {

   private String scan;
   private Button cancel;

   public ScanShortScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
   }

   /*
    * interface methods
    */

   @Override
   public void initDisplay() {
      initButtons();
      initPromptModule(
            "Please scan make up pick UPC",
            1,
            ENTRYCODE_NONE,
            true,
            true,
            false,
            2);
      super.initDisplay();
   }

   public void handleInit() {
      super.handleInit();
   }

   protected void initButtons() {
      int x = SCREEN_WIDTH - MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   } 

   public void handleTick() {
      super.handleTick();

      scan = getScan(); //See AbstractNuminaScreen

      if(scan != null) {
         processScan(scan);
      }
   }

   //Methods created
   protected void processScan(String scan) {
      inform( "entry received text [%s]", scan );

      try {
      	if( lock("makeupShorts") ) {
		      Map<String,String> skuFromScan = getSkuFromScan(scan);
		      if( skuFromScan == null || skuFromScan.isEmpty() ) {
		      	showAlertMsg("No SKU found for barcode [%s].",scan);
		      	return;
		      }
		      else {
		         String sku = getMapStr(skuFromScan,"sku");
		         String uom = getMapStr(skuFromScan, "uom");
		         setParam(TermParam.sku, sku);
		         setParam(TermParam.uom, uom);
		         inform("Found SKU [%s] uom [%s] from scan [%s]", sku, uom, scan);
		
		         //Try to find a oldest carton in terms of wave that is short with the given sku/uom from the upc scan
		         Map<String,String> cartonInfo = db.getRecordMap(
		            "SELECT p.orderId, cartonSeq, lpn, sku, uom, p.pickType, CAST(SUM(qty) AS INT) AS qty " +
		            "FROM rdsPicks p " +
		            "JOIN rdsCartons c USING (cartonSeq) " +
					"JOIN custOrders o " +
					"ON o.orderId = c.orderId " +
		            "WHERE picked = 0 AND shortPicked=1 " +
		            "AND sku=%s AND uom = '%s' " +
		            "AND makeupShortsOperatorId = '' " +
		            "AND c.cancelStamp IS NULL " + 
		            "AND c.lpn<>c.trackingNumber " +
					"GROUP BY cartonSeq, sku, uom HAVING qty > 0 " +
				 	"ORDER BY o.waveSeq " +
				 	"LIMIT 1", sku, uom
		         );
		         if(cartonInfo == null || cartonInfo.isEmpty()) {
		            alert("Carton info is empty, no cartons found");
		            showAlertMsg("No short container found for barcode [%s].", scan);
		            return;
		         }
		         String cartonLPN = getMapStr(cartonInfo, "lpn");
		         String orderId = getMapStr(cartonInfo, "orderId");
		         int cartonSeq = getMapInt(cartonInfo, "cartonSeq");
		         int pickQty = getMapInt(cartonInfo, "qty");
		         if(cartonLPN == null || cartonLPN.isEmpty()) {
		            alert("Carton LPN is empty in cartonInfo");
		            showAlertMsg("No short container found for barcode [%s].", scan);
		            return;
		         }
		         if(cartonSeq < 1) {
		            alert("CartonSeq is < 1 in cartonInfo");
		            showAlertMsg("No short container found for barcode [%s].", scan);
		            return;
		         }
		
		         setParam(TermParam.cartonLpn, cartonLPN);
		         setParam(TermParam.cartonSeq, cartonSeq+"");
		         setParam(TermParam.qty, pickQty+"");
		         setParam(TermParam.orderId, orderId);
		         
		       //Reserve the makeup picks for this operator
			      db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='%s' "
			        		+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s'", getOperatorId(), cartonSeq, sku, uom);
		
		         setNextScreen("exception.ScanShortContainerScreen");
		      }
      	} else {
	      	showAlertMsg("unable to obtain db lock");
	      	return;      		
      	}
      } catch (Exception e) {
      	showAlertMsg("Could not reserve make up shorts");
         return;
       }
       finally {
         unlock("makeupShorts");
       }
   }
}
