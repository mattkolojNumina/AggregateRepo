package termApp.orderStart;

import term.TerminalDriver;

import termApp.util.TextField;
import termApp.util.Constants.Align;
import termApp.util.ScrollBox.ButtonAction;
import termApp.util.ScrollBox.ScrollBoxConstructor;
import termApp.util.TermActionObject.OnActionListener;

import static termApp.util.Constants.*;
import termApp.util.Button;
import termApp.util.ScrollBox;
import java.util.*;

import static rds.RDSLog.*;
import static app.Constants.*;
import static sloane.SloaneConstants.*;


public class IdleScreen
extends AbstractCartonStartScreen {

	private TextField info,selectText;
	private String currentCartonType,currentPickType;
  
	private Button switchMode;

	private List<Map<String,String>> LeftCartonTypeMap;//, RightCartonTypeMap;
	
	private ScrollBox LeftTable;//, RightTable;
	
	private int cartonSeq = -1;
	//private int priority = 0;
	private int cycle = 0;
  
   public IdleScreen(TerminalDriver term) {
      super(term);
      setLogoutAllowed(true);
      clearAllParam();
      currentCartonType = currentPickType = "";
   }

   /*
    * interface methods
    */

   /** Initializes the screen and lays out UI elements. */
   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();  // displays login footer
   }

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();  // calls handleScan -> processScan
      //addButtons();
      cycle ++ ;
      if( cycle > 9999999 )
      	cycle = 0 ;
	
      if( currentCartonType.isEmpty() ) {
      	if( cycle == 10 || cycle % 30 == 0 ) {
      		getCartonTypes();
      } else {
      	if( System.currentTimeMillis() - lastActivity > 60 * 1000L ) {
      		clearCartonType();
      		getCartonTypes();
      	}
      }
	}

	  //Done every 5 seconds
	  //if(cycle % 10 == 0) {
		//trace("Number of bulk picks: " + getNumberOfParcelBulkPicks());
	  //}
   }

   /*
    * processing methods
    */

   @Override
   protected void processScan(String scan) {
      trace( "processing scan [%s]", scan);
      if(currentCartonType.isEmpty()) {
      	info.setFontColor("red");
         info.updateText("No carton type selected, scan ignored");
         return;     	
      }
      /*if(prevScan.equals(scan)){
      	 info.setFontColor("red");
         info.updateText("Duplicate scan [%s]",scan);
         return;
      } else */
      if(!isValidLpn(scan,currentCartonType)) {
      	info.setFontColor("red");
      	info.updateText("[%s] is invalid for %s container",scan, currentCartonType);
         return;
	   } else if (!isLpnAvailable(scan,currentCartonType)) {
		   info.setFontColor("red");
		   info.updateText("LPN already assigned [%s]",scan);
         return;
      } else {
      	getCartonSeq();
	      if( cartonSeq>0 ){
	      	setLpn(cartonSeq,scan);
            updateTextField();
            //if( priority <=0 ) {
            	info.setFontColor("black");
            	info.updateText(String.format("LPN scanned: [%s]. Place on conveyor", scan));
            /* } else {
            	info.setFontColor("red");
            	info.updateText(String.format("LPN scanned: %s, PRIORITY", scan));            	
            }*/
            //prevScan=scan;
	      } else {
	      	info.setFontColor("red");
	      	info.updateText("No carton for %s, select a different type",currentCartonType);
	      	clearCartonType();
	      }
      }
   }

   /*
    * display methods
    */
   @Override
   public void initDisplay() {
      super.initDisplay();
      initScreen();
     
      switchMode = new Button(1600, 880, 60, "Change Mode",Align.CENTER,false);

      switchMode.registerOnActionListener(modeAction());
      switchMode.show();
   }

   //Displays all TextFields
   protected void initScreen(){
      initTextFields();
      initLeftTable();
      //initRightTable();
      //getCartonTypes();
   }
   
   private void initTextFields() {
   	TextField currentText = new TextField(50,175, 40,"Current carton:", true);
   	selectText = new TextField(425,165, 50,"", true);
   	info = new TextField(50,925, 50,"", true);
   }
   
   public void initLeftTable() {  
   	ScrollBoxConstructor model = new ScrollBoxConstructor(0, 300);
   	model.setFont(40, 40);
   	model.setMargins(0,0,0,0);
   	model.setRows(10, 20);
   	model.setWidth(SCREEN_WIDTH/2);
   	model.setIndexJump(1);
   	model.drawHeaders(true);
	model.setButton(0, 0, 0);

	model.addColumn(50,350,Align.LEFT,"Type","cartonType");
	model.addColumn(400,200,Align.LEFT,"# Carton","numCartons");
	Button btn = new Button(0,-5,40,"SELECT",Align.LEFT,-1,false);
	ButtonAction action = new ButtonAction() {
		@Override
		public void onAction(Button btn, Map<String, String> map) {
			select(map);
		}
	};
	model.addButtonColumn(725, "", btn, action);
	LeftTable = model.build();
	LeftTable.hide();
}
   
   /*public void initRightTable() {  
   	ScrollBoxConstructor model = new ScrollBoxConstructor(960, 300);
   	model.setFont(40, 40);
   	model.setMargins(0,0,0,0);
   	model.setRows(10, 20);
   	model.setWidth(SCREEN_WIDTH/2);
   	model.setIndexJump(1);
   	model.drawHeaders(true);
   	model.setButton(0, 0, 0);

   	model.addColumn(50,350,Align.LEFT,"Type","cartonType");
   	model.addColumn(400,200,Align.LEFT,"# Carton","numCartons");
    RightTable = model.build();
    RightTable.hide();
   }   */
   
   private void select(Map<String, String> p) {
   	initActivityTimer();
   	for( Map<String,String> m : LeftCartonTypeMap ) {
   		m.put("background", "");
   	}
   	p.put("background", "green");
   	String cartonType = getMapStr(p,"cartonType");
   	int numCartons = getMapInt(p,"numCartons");
   	String pickType = getMapStr(p,"pickType");
   	currentCartonType = cartonType;
   	currentPickType = pickType;
   	 
   	selectText.setFontColor("black");
   	selectText.updateText("%s, %d needed",cartonType,numCartons);
   	
   	info.setFontColor("black");
      info.updateText("Scan LPN");
   	LeftTable.updateDisplayList(LeftCartonTypeMap);
   	//RightTable.updateDisplayList(RightCartonTypeMap);
   }   

   protected void getCartonTypes(){
	int waveSeq = getOldestWaveSeq();
   	List<Map<String,String>> cartonTypeMapLeft  = db.getResultMapList(  
   			"SELECT cartonType, pickType, MIN(waveSeq) AS minWaveSeq, " +
   			"COUNT(*) as numCartons " +
   			"FROM rdsCartons c " +
            "JOIN custOrders AS o USING (orderId) " + 
            "WHERE lpn IS NULL AND c.releaseStamp IS NOT NULL AND c.cancelStamp IS NULL " +
			"AND waveSeq = %d AND pickType IN ('%s') " +
            "GROUP BY cartonType ORDER BY minWaveSeq ASC, numCartons DESC", waveSeq, PICKTYPE_ZONEROUTE);
   	LeftCartonTypeMap = new ArrayList<>();
   	//RightCartonTypeMap = new ArrayList<>();
      if(cartonTypeMapLeft.isEmpty()){
      	info.setFontColor("black");
         info.updateText("There are no cartons found at this time.");
      } else {
      	//int index = 0;
      	for( Map<String,String> m : cartonTypeMapLeft ) {
      			LeftCartonTypeMap.add(m);
      		}
		  /*for( Map<String,String> m : cartonTypeMapRight ) {
				RightCartonTypeMap.add(m);
			} */
      	info.setFontColor("black");
         info.updateText("Select next carton type to start");
      }
      if( LeftCartonTypeMap.size()>0 ) {
      	LeftTable.show();
      	LeftTable.updateDisplayList(LeftCartonTypeMap);
      } else
      	LeftTable.hide();
      /*if( RightCartonTypeMap.size()>0 ) {
      	RightTable.show();
      	RightTable.updateDisplayList(RightCartonTypeMap);
      } else 
      	RightTable.hide();*/
   }

   protected void getCartonSeq(){//Finds carton with selected type and highest priority
   	Map<String,String> m = db.getRecordMap("SELECT cartonSeq, waveSeq " +
			"FROM rdsCartons as c " +
            "JOIN custOrders AS o USING (orderId) " +
            "WHERE cartonType = '%s' AND pickType IN ('%s') " +
            "AND lpn IS NULL AND c.releaseStamp IS NOT NULL AND c.cancelStamp IS NULL " +
            "ORDER BY waveSeq, c.releaseStamp, c.cartonSeq " + 
            "LIMIT 1",currentCartonType, PICKTYPE_ZONEROUTE);
   	if( m!=null && !m.isEmpty() ) {
	      cartonSeq = getMapInt(m,"cartonSeq");
	      int waveSeq = getMapInt(m,"waveSeq");
	      inform("Select cartonSeq: [%d], waveSeq: [%d]",cartonSeq, waveSeq);
   	}
   }

   protected int getOldestWaveSeq() {
		return db.getInt(-1, 
			"SELECT MIN(waveSeq) " +
			"FROM rdsCartons AS c " +
			"JOIN custOrders AS o ON c.orderId = o.orderId " +
			"WHERE c.releaseStamp IS NOT NULL " +
			"AND c.cancelStamp IS NULL " +
			"AND c.lpn IS NULL " +
			"AND pickType = 'zoneroute' " +
			"GROUP BY c.orderId " +
			"ORDER BY waveSeq " +
			"LIMIT 1;"
		);
   }

   protected void updateTextField(){
	  int waveSeq = getOldestWaveSeq();
	  inform("Oldest waveSeq is %s", waveSeq);
      for(Map<String,String> m : LeftCartonTypeMap){
      	String cartonType = getMapStr(m,"cartonType");
      	if( !cartonType.equals(currentCartonType) ) continue;
      	Map<String,String> count = db.getRecordMap(
      			"SELECT cartonType, pickType, COUNT(*) as numCartons " +
	   			"FROM rdsCartons c " +
	            "JOIN custOrders AS o USING (orderId) " +
	            "WHERE c.cartonType='%s' AND pickType IN ('%s') " +
				"AND waveSeq = %d " + 
	            "AND lpn IS NULL AND c.releaseStamp IS NOT NULL AND c.cancelStamp IS NULL ",
	            currentCartonType,PICKTYPE_ZONEROUTE, waveSeq);
      	int numCartons = getMapInt(count,"numCartons");
      	if( numCartons <= 0 ) {
      		clearCartonType();
            getCartonTypes();
      	} else {
      		m.put("numCartons", ""+numCartons);
      		LeftTable.updateDisplayList(LeftCartonTypeMap);
         	selectText.setFontColor("black");
         	selectText.updateText("%s, %d needed",cartonType,numCartons);
         	
      	}
      } 
        /*for(Map<String,String> m : RightCartonTypeMap){
      	String cartonType = getMapStr(m,"cartonType");
      	if( !cartonType.equals(currentCartonType) ) continue;
      	Map<String,String> count = db.getRecordMap(
      			"SELECT cartonType, pickType, COUNT(*) as numCartons " +
	   			"FROM rdsCartons c " +
	            "JOIN custOrders AS o USING (orderId) " +
	            "WHERE c.cartonType='%s' AND pickType IN ('%s') " +
				"AND waveSeq != %d " +
	            "AND lpn = 'n/a' AND c.releaseStamp IS NOT NULL AND c.cancelStamp IS NULL ",
	            cartonType,PICKTYPE_ZONEROUTE, waveSeq);
      	int numCartons = getMapInt(count,"numCartons");
      	if( numCartons <= 0 ) {
      		clearCartonType();
            getCartonTypes();
      	} else {
      		m.put("numCartons", ""+numCartons);
      		RightTable.updateDisplayList(RightCartonTypeMap);
         	selectText.setFontColor("black");
         	selectText.updateText("%s, %d needed",cartonType,numCartons);
         	
      	}
      }  */
   }
   
   private void clearCartonType() {
   	currentCartonType = "";
   	currentPickType = "";
   	selectText.updateText("");
   }

    protected int getNumberOfParcelBulkPicks() {
      return db.getInt(0,"SELECT COUNT(*) FROM rdsPicks JOIN custOrders USING(orderId) "
      		+ "WHERE pickType='%s' "
      		+ "AND pickOperatorId='' AND readyForPick=1 AND picked=0 AND shortPicked=0 AND canceled=0 "
      		+ "AND orderType='%s'",PICKTYPE_BULK,ORDERTYPE_PARCEL);
   }

}