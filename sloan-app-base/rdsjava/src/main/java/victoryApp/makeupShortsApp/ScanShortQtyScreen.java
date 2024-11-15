package victoryApp.makeupShortsApp;

import static victoryApp.gui.GUIConstants.*;
import victoryApp.*;
import java.util.List;
import dao.SloaneCommonDAO;
import rds.RDSUtil;

public class ScanShortQtyScreen extends AbstractMakeupShortsApp {

  public ScanShortQtyScreen(VictoryApp app) {
    super(app);
  }

  public boolean handleInit() {
    this.getGUIState().getFooter().showChangeTaskButton();
    super.handleInit();

    addButtons("", "", "", CANCEL);
    initPickingInfo();

    return true;
  }
 
  @Override
  public boolean handleInput(String scan) {
    inform( "entry received text [%s]", scan );

    int qty = 0;
    int maxPickQty = getIntParam(VictoryParam.qty);

    try{
      qty = Integer.parseInt(scan);
    } catch(Exception e) {
        sayPhrase("INVALID_QTY");
        setError("INVALID_QTY");
        return false;
    }

    if(qty > 0 && qty <= maxPickQty) {
      String cartonSeq = getParam(VictoryParam.cartonSeq);
      setParam(VictoryParam.makeUpQty, qty+"");
      //Trigger status uploads for 'qty' number of picks
      List<String> pickSeqs = db.getValueList(
        "SELECT pickSeq FROM rdsPicks " +
        "WHERE cartonSeq=%s AND picked=0 AND shortPicked=1 " +
        "AND sku=%s AND uom='%s' LIMIT %d", 
        cartonSeq, getParam(VictoryParam.sku),
        getParam(VictoryParam.uom), qty
      );

      //Insert a chasePickPut status to confirm each pick seq that the operator has chase picked.
      for(String seq : pickSeqs) {
        int pickSeq = RDSUtil.stringToInt(seq, -1);
        if(pickSeq>0) {
            SloaneCommonDAO.confirmPick(pickSeq,getOperatorID());
        }
      }

      SloaneCommonDAO.postCartonLog(cartonSeq, "Victory Voice",
        "Operator %s confirmed make up %d of sku %s uom %s to %s", getOperatorID(), qty, 
        getParam(VictoryParam.sku), getParam(VictoryParam.uom), getParam(VictoryParam.cartonLpn)
      );
      SloaneCommonDAO.postOrderLog(getParam(VictoryParam.orderId), "Victory Voice",
        "Operator %s confirmed make up %d of sku %s uom %s", getOperatorID(), qty, 
        getParam(VictoryParam.sku), getParam(VictoryParam.uom)
      );

      completeOperation();
      return true;
    }
    else {
      sayPhrase("INVALID_QTY");
      setError("INVALID_QTY");
      return false;
    }

  }

  protected void completeOperation() {
    unreserveMakeupShorts();

    clearAllParam();

    //Check if the current container the operator is working is complete, 
    if(isContainerComplete(getIntParam(VictoryParam.cartonSeq))) {
      setParam(VictoryParam.showComplete, "true");
    }

    setNextScreen(new StartScreen(app));
  }


}
