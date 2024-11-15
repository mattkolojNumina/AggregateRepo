package victoryApp.makeupShortsApp;

import victoryApp.*;
import java.util.Map;

public class StartScreen extends AbstractMakeupShortsApp {

  public StartScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    super.handleInit();

    if(getParam(VictoryParam.showComplete).equals("true")) {
      setParam(VictoryParam.showComplete, "false");
      sayPhrase("MAKEUP_SHORTS_COMPLETE_PHRASE");
      //setWarning("MAKEUP_SHORTS_COMPLETE_DISPLAY"); //Makeup shorts complete
    }

    return true;
  }
 
  @Override
  public boolean handleInput(String scan) {
    inform( "entry received text [%s]", scan );

    try {
   	if (lock("makeupShorts") ) {
	      Map<String,String> skuFromScan = getSkuFromScan(scan);
	      if( skuFromScan == null || skuFromScan.isEmpty() ) {
	        sayPhrase("INVALID_BARCODE"); //Invalid barcode
	        setError("INVALID_BARCODE"); //Invalid barcode
	        return false;
	      }
	      else {
	        String sku = getMapStr(skuFromScan,"sku");
	        String uom = getMapStr(skuFromScan, "uom");
	        setParam(VictoryParam.sku, sku);
	        setParam(VictoryParam.uom, uom);
	        inform("Found SKU [%s] uom [%s] from scan [%s]", sku, uom, scan);
	
	        //Try to find a carton that is short with the given sku/uom from the upc scan
	        Map<String,String> cartonInfo = db.getRecordMap(
					"SELECT p.orderId, cartonSeq, lpn, sku, uom, p.pickType, CAST(SUM(qty) AS INT) AS qty " +
				  	"FROM rdsPicks p " +
				  	"JOIN rdsCartons c USING (cartonSeq) " +
					"JOIN custOrders o " +
					"ON c.orderId = o.orderId " +
				  	"WHERE picked = 0 AND shortPicked=1 " +
					"AND sku=%s AND uom = '%s' " +
				  	"AND makeupShortsOperatorId = '' " +
					"AND c.cancelStamp IS NULL " +
					"AND c.lpn<>c.trackingNumber " +
	              	"GROUP BY cartonSeq, sku, uom HAVING qty > 0 " +
					"ORDER BY o.waveSeq" +
					"LIMIT 1", sku, uom
	        );
	        if(cartonInfo == null || cartonInfo.isEmpty()) {
	          sayPhrase("MAKEUP_SHORTS_NO_CONTAINERS");
	          setError("MAKEUP_SHORTS_NO_CONTAINERS");
	          return false;
	        }
	
	        String cartonLPN = getMapStr(cartonInfo, "lpn");
	        String orderId = getMapStr(cartonInfo, "orderId");
	        int cartonSeq = getMapInt(cartonInfo, "cartonSeq");
	        int pickQty = getMapInt(cartonInfo, "qty");
			
	        inform("pickQty: " + pickQty);
	
	        if(cartonLPN == null || cartonLPN.isEmpty()) {
	          sayPhrase("MAKEUP_SHORTS_NO_CONTAINERS");
	          setError("MAKEUP_SHORTS_NO_CONTAINERS");
	          return false;
	        }
	        if(cartonSeq < 1) {
	          sayPhrase("MAKEUP_SHORTS_NO_CONTAINERS");
	          setError("MAKEUP_SHORTS_NO_CONTAINERS");
	          return false;
	        }
	
	        setParam(VictoryParam.cartonLpn, cartonLPN);
	        setParam(VictoryParam.cartonSeq, cartonSeq+"");
	        setParam(VictoryParam.qty, pickQty+"");
	        setParam(VictoryParam.putQty, pickQty+"");
	        setParam(VictoryParam.uomPhrase, getUomPhrase(uom));
	        setParam(VictoryParam.orderId, orderId);
	        
	        //Reserve the makeup picks for this operator
	        db.execute("UPDATE rdsPicks SET makeupShortsOperatorId='%s' "
	        		+ "WHERE cartonSeq=%d AND sku='%s' AND uom='%s'", getOperatorID(), cartonSeq, sku, uom);	        
	
	        setNextScreen(new ScanShortContainerScreen(app));
	        
	      }
   	} else {
         sayPhrase("MAKEUP_SHORTS_NO_CONTAINERS");
         setError("MAKEUP_SHORTS_NO_CONTAINERS");
         return false;   		
   	}
    }
    catch (Exception e) {
      alert("Could not reserve the picks");
      return false;
    }
    finally {
      unlock("makeupShorts");
    }

    return true;
  }


}
