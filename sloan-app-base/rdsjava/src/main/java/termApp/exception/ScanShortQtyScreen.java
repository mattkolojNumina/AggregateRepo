package termApp.exception;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.List;

import dao.SloaneCommonDAO;
import rds.RDSUtil;
import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.NumPad.NumPadAction;
import termApp.util.NumPad.NumPadConstructor;

public class ScanShortQtyScreen
      extends AbstractExceptionStationScreen {

   private TextField padField;
   private NumPad np;
   private Button cancel;

   public ScanShortQtyScreen( TerminalDriver term ) {
      super( term );
      //operators should be allowed to logout; this makes the footer display a logout button
      setLogoutAllowed(false); 
   }

   /*
    * interface methods
    */

   @Override
   public void initDisplay() {
      initInfoBox();
   	setInfoBox(false);
   	initButtons();
      initPromptModule(
            "Please confirm short pick qty",
            1,
            ENTRYCODE_NONE,
            true,
            true,
            true,
            1);
      super.initDisplay();
   }  

   public void handleInit() {
      super.handleInit();
      initNumPad();
   }

   @Override
   public void setLeftInfoBox(){
      if (leftInfoBox==null)
         return;
      String cartonID = getParam(TermParam.cartonLpn);
      leftInfoBox.updateInfoPair(0, "Carton LPN", cartonID );
      leftInfoBox.updateInfoPair(1, "SKU", getParam(TermParam.sku)+" "+getParam(TermParam.uom) );
      leftInfoBox.show(); 
   }

   @Override
   public void setRightInfoBox(){
      if (rightInfoBox==null)
         return;
      rightInfoBox.updateInfoPair(0, "Invoice", getParam(TermParam.orderId));
      rightInfoBox.updateInfoPair(1, "Qty", getParam(TermParam.qty));
      rightInfoBox.show(); 
   }

   private void initNumPad() {
      int x = W1_2 + 400;
      int y = INFO_Y + 175 ;
      padField = new TextField(x-400,y+175,65,"", true);

      NumPadConstructor npBuild = new NumPadConstructor(x, y);
      np = npBuild.build();
      np.setGlobalBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            padField.updateText(value);
         }
      });
      np.setEnterBtnAction(new NumPadAction() {
         @Override
         public void action(String value) {
            inputDecision(value);
         }
      });
      np.show();
      padField.show();
   }
   
   protected void initButtons() {
      int x = SCREEN_WIDTH - MARGIN;
      int y = BTN_Y;
      int f = BTN_FONT;
      
      cancel = new Button(x,y,f, "Cancel",Align.RIGHT,-1,-1,COLOR_YELLOW,true);
      cancel.registerOnActionListener(cancelAction());
      cancel.show();
   }    

   public void inputDecision(String qty_str) {
      int qty = 0;
      int maxPickQty = getIntParam(TermParam.qty);
      try{
      	qty = Integer.parseInt(qty_str);
      } catch(Exception e) {
      	showAlertMsg("Invalid qty %s",qty_str);
      }
      if(qty > 0 && qty <= maxPickQty) {
         String cartonSeq = getParam(TermParam.cartonSeq);
         setParam(TermParam.makeUpQty, qty+"");
         //Trigger status uploads for 'qty' number of picks
         List<String> pickSeqs = db.getValueList(
            "SELECT pickSeq FROM rdsPicks " +
            "WHERE cartonSeq=%s AND picked=0 AND shortPicked=1 " +
            "AND sku=%s AND uom='%s' LIMIT %d", 
            cartonSeq, getParam(TermParam.sku),
            getParam(TermParam.uom), qty
         );
         //Insert a chasePickPut status to confirm each pick seq that the operator has chase picked.
         for(String seq : pickSeqs) {
            int pickSeq = RDSUtil.stringToInt(seq, -1);
            if(pickSeq>0) {
               SloaneCommonDAO.confirmPick(pickSeq,getOperatorId());
            }
         }
         SloaneCommonDAO.postCartonLog(cartonSeq, getStationName(),
            "Operator %s confirmed make up %d of sku %s uom %s to %s", getOperatorId(), qty, 
            getParam(TermParam.sku), getParam(TermParam.uom), getParam(TermParam.cartonLpn)
         );
         SloaneCommonDAO.postOrderLog(getParam(TermParam.orderId), getStationName(),
            "Operator %s confirmed make up %d of sku %s uom %s", getOperatorId(), qty, 
            getParam(TermParam.sku), getParam(TermParam.uom)
         );
         //Check if the current container the operator is working is complete, 
         if(isContainerComplete(getIntParam(TermParam.cartonSeq))) {
            inform("Container has all make up picks");
            showSuccessMsg("Container complete");
            initStartTimer();
         }
         //Clear current params and prompt operator for new make up short pick
         else {
            doOkay();
         }
      }
      else {
         alert("Invalid qty [%d] entered", qty);
         showAlertMsg("Invalid qty %d", qty);
      }
   }

   @Override
   protected void doOkay() {
      unreserveMakeupShorts();
      clearAllParam();
      setNextScreen("exception.ScanShortScreen");
   }
   
   @Override
   protected void doCancel(){
   	inform("cancel button pressed");
   	unreserveMakeupShorts();
   	setNextScreen("exception.IdleScreen");
   }    

   public void handleTick() {
      super.handleTick();
   }
}
