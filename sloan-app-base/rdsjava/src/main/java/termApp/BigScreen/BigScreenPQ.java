package termApp.BigScreen;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnTextActionListener;
import termApp.util.TermActionObject.OnActionListener;
import termApp.util.ScrollBox.ScrollBoxConstructor;
import termApp.util.Rectangle;
import termApp.util.TextField;
import static termApp.util.Constants.INVISIBLE;

import static termApp.util.Constants.*;

import java.util.List;
import java.util.Map;

import static sloane.SloaneConstants.*;

import static rds.RDSLog.*;

public class BigScreenPQ
extends AbstractBigScreen {


   protected String bkgdColor;
   protected Rectangle bkgdA;
   protected Rectangle bkgdB;
   protected Rectangle bkgdC;
   protected Rectangle bkgdD;
   protected Rectangle bkgdE;
   protected Rectangle bkgdF;
   protected Rectangle bkgdG;
   protected Rectangle bkgdH;
   protected Rectangle bkgdI;
   protected Rectangle bkgdJ;
   protected Rectangle vertRect;

   //private ScrollBox leftList;
   private ScrollBox listA, listB, listC, listD, listE, listF, listG;
   private ScrollBox listAA, listAB, listAC, listAD, listAE;
   private ScrollBox listBA, listBB, listBC, listBD, listBE;
   private ScrollBox listCA, listCB, listCC, listCD, listCE;
   private ScrollBox listDA, listDB, listDC, listDD, listDE;
   private ScrollBox listEA, listEB, listEC, listED, listEE;
   private ScrollBox listFA, listFB, listFC, listFD, listFE;
   private ScrollBox listGA, listGB, listGC, listGD, listGE;
   private ScrollBox listHA, listHB, listHC, listHD, listHE;
   private ScrollBox listIA, listIB, listIC, listID, listIE;
   private ScrollBox listJA, listJB, listJC, listJD, listJE;
   private ScrollBox listKA, listKB, listKC, listKD, listKE;
   private ScrollBox listLA, listLB, listLC, listLD, listLE;
   private ScrollBox listMA, listMB, listMC, listMD, listME;
   private ScrollBox listNA, listNB, listNC, listND, listNE;
   private ScrollBox listOA, listOB, listOC, listOD, listOE;
   private ScrollBox listPA, listPB, listPC, listPD, listPE;
   private ScrollBox listQA, listQB, listQC, listQD, listQE;
   private ScrollBox listRA, listRB, listRC, listRD, listRE;
   private ScrollBox listSA, listSB, listSC, listSD, listSE;
   private ScrollBox listTA, listTB, listTC, listTD, listTE;

   private Rectangle rectA, rectB, rectC, rectD, rectE, rectF, rectG, rectH, rectI, rectJ;

   private ScrollBox waveA, waveB, waveC, waveD, waveE, waveF, waveG, waveH, waveI, waveJ;

   private ScrollBox demandA, demandB, demandC, demandD, demandE, demandF, demandG, demandH, demandI, demandJ;

   //private TextField waveA, waveB, waveC, waveD, waveE, waveF, waveG;
   private List<Map<String,String>> cartonList;

   private Button switchMode;

   private Button boom, aersol, pq;

   private int numWaves;

   private TextField boomCountText, pqCountText, buildTypeText, availableCartonsText, noCountCartonsText, scanMsg;
   private TextField toteText, smallText, mediumText, largeText, exportText, waveText;
   private TextField helloText;

   private TextField cartonCountText, cartonCountNumText, orderLinesCountText, orderLinesCountNumText;
   private TextField greenText, yellowText, redText, grayText;

   private String cartType;

   //How many waves we want showing on the screen at once
   private int MAX_WAVES = 10;
   private final static int LIST_LENGTH = 6;

   private int SCREEN_WIDTH =  1920;
   private int SCREEN_HEIGHT =  1080;
   //private int FONT_SIZE = ;
   private int GAP_HEIGHT = SCREEN_HEIGHT/15;
   private int GAP_WIDTH = SCREEN_WIDTH/(12);
   private int offset_height = GAP_HEIGHT/2; 
   private int offset_width = GAP_WIDTH/2;

   private int cycle = 1;

   public BigScreenPQ(TerminalDriver term) {

      super(term);

   }

   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();

      //Update display every so often
      if(cycle == 1 || cycle % 50 == 0 ) {
         hideLists();
         updateDisplay();
      }

      if( cycle > 1000 )
      	cycle = 0 ;

      cycle ++ ;
   }

   /*
    * helper methods
    */



   /*
    * display methods
    */
    public void updateDisplay() { 
         updateList();
         drawLists();
    }

    private void initList() {
      
      //Horizontal Lines
      term.setRectangle(0, 0, GAP_HEIGHT, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(1, 0, GAP_HEIGHT * 2, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(2, 0, GAP_HEIGHT * 4, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(3, 0, GAP_HEIGHT * 6, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(4, 0, GAP_HEIGHT * 8, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(5, 0, GAP_HEIGHT * 10, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );
      term.setRectangle(6, 0, GAP_HEIGHT * 12, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT/24, "black", 1, "black" );

      //verticalLines
      rectA = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectB = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectC = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectD = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectE = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectF = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectG = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectH = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectI = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);
      rectJ = new Rectangle(0, 0, 0, 0, INVISIBLE, 0, INVISIBLE);

      //Vertical End line
      term.setRectangle(401, GAP_WIDTH * 12 - offset_width / 2, GAP_HEIGHT, GAP_HEIGHT/24, GAP_HEIGHT * 11 + 3, "black", 1, "black");
      
      //Informational Legend Displayed at bottom of screen
      term.setRectangle(402, GAP_WIDTH - offset_width/2, GAP_HEIGHT * 12 + offset_width/8, GAP_WIDTH - offset_width/2, GAP_HEIGHT + offset_height, INVISIBLE, 1, "black");
      
      cartonCountText = new TextField(GAP_WIDTH * 2 - offset_width/2, GAP_HEIGHT * 12 + offset_height/2, 25, "Containers waiting for LPN assignment");
      orderLinesCountText = new TextField(GAP_WIDTH * 2 - offset_width/2, GAP_HEIGHT * 13, 25, "Order lines waiting for fulfillment");
      cartonCountNumText = new TextField(GAP_WIDTH - offset_width/8, GAP_HEIGHT * 12 + offset_height/2, 30, "#/#");
      orderLinesCountNumText = new TextField(GAP_WIDTH - offset_width/8, GAP_HEIGHT * 13, 30, "#/#");


      term.setRectangle(400, GAP_WIDTH * 7, GAP_HEIGHT * 12 + offset_height, GAP_WIDTH, GAP_HEIGHT, INVISIBLE, 1, "black");
      grayText = new TextField(GAP_WIDTH * 7, GAP_HEIGHT * 12 + offset_height/4, 20, "Created");
      term.setRectangle(301, GAP_WIDTH * 8, GAP_HEIGHT * 12 + offset_height, GAP_WIDTH, GAP_HEIGHT, "yellow", 1, "black");
      greenText = new TextField(GAP_WIDTH * 8, GAP_HEIGHT * 12 + offset_height/4, 20, "Released");
      term.setRectangle(302, GAP_WIDTH * 9, GAP_HEIGHT * 12 + offset_height, GAP_WIDTH, GAP_HEIGHT, "green", 1, "black");
      yellowText = new TextField(GAP_WIDTH * 9, GAP_HEIGHT * 12 + offset_height/4, 20, "Complete");
      term.setRectangle(303, GAP_WIDTH * 10, GAP_HEIGHT * 12 + offset_height, GAP_WIDTH, GAP_HEIGHT, "red", 1, "black");
      redText = new TextField(GAP_WIDTH * 10, GAP_HEIGHT * 12 + offset_height/4, 20, "Cancelled");

      //term.setBackground(101, GAP_WIDTH *2, GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 10, "black", 1, "black" );
      int y = 405;

//-----------------------------------DRAWS CARTON COUNTS LIST-----------------------------------
      ScrollBoxConstructor sbModel = new ScrollBoxConstructor(0,y);
      sbModel.setFont(34, 34);
      sbModel.setMargins(000, 0, 0, 0);
      sbModel.setWidth(50);
      sbModel.drawHeaders(true);
      sbModel.addColumn(0, GAP_WIDTH + offset_width / 8, Align.CENTER, "", "cartonCount");

      //leftList = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listAA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listAB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listAC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listAD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listAE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listBA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listBB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listBC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listBD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listBE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listCA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listCB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listCC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listCD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listCE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listDA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listDB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listDC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listDD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listDE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listEA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listEB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listEC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listED = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listEE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listFA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listFB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listFC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listFD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listFE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listGA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listGB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listGC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listGD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listGE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listHA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listHB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listHC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listHD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listHE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listIA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listIB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listIC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listID = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listIE = sbModel.build();

      sbModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT * 2 + offset_height/2); listJA = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT * 4 + offset_height/2); listJB = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT * 6 + offset_height/2); listJC = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT * 8 + offset_height/2); listJD = sbModel.build(); sbModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT * 10 + offset_height/2); listJE = sbModel.build();

      //-----------------------------------DRAWS ORDER LINES COUNTS LIST-----------------------------------

      ScrollBoxConstructor orderLinesModel = new ScrollBoxConstructor(0,y);
      orderLinesModel.setFont(34, 34);
      orderLinesModel.setMargins(000, 0, 0, 0);
      orderLinesModel.setWidth(50);
      orderLinesModel.drawHeaders(true);
      orderLinesModel.addColumn(0, GAP_WIDTH + offset_width / 8, Align.CENTER, "", "orderLinesCount");

      orderLinesModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listKA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listKB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listKC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listKD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listKE = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listLA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listLB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listLC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listLD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listLE = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listMA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listMB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listMC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listMD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listME = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listNA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listNB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listNC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listND = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listNE = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listOA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listOB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listOC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listOD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listOE = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listPA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listPB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listPC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listPD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listPE = orderLinesModel.build();

      orderLinesModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listQA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listQB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listQC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listQD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listQE = orderLinesModel.build();
       
      orderLinesModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listRA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listRB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listRC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listRD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listRE = orderLinesModel.build();
       
      orderLinesModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listSA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listSB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listSC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listSD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listSE = orderLinesModel.build();
       
      orderLinesModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, (GAP_HEIGHT * 3 + offset_height/2)); listTA = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, (GAP_HEIGHT * 5 + offset_height/2)); listTB = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, (GAP_HEIGHT * 7 + offset_height/2)); listTC = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, (GAP_HEIGHT * 9 + offset_height/2)); listTD = orderLinesModel.build();
      orderLinesModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, (GAP_HEIGHT * 11 + offset_height/2)); listTE = orderLinesModel.build();
       
      //-----------------------------------DRAWS WAVE LIST----------------------------------------------

      ScrollBoxConstructor waveModel = new ScrollBoxConstructor(0,y);
      waveModel.setFont(60, 60);
      waveModel.setMargins(000, 0, 0, 0);
      waveModel.setWidth(50);
      waveModel.drawHeaders(true);
      waveModel.addColumn(0, 0, Align.CENTER, "", "dailyWaveSeq");

      waveModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveA = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveB = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveC = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveD = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveE = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveF = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveG = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveH = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveI = waveModel.build();
      waveModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT - offset_height/4 + 1); waveJ = waveModel.build();

      //-----------------------------------DRAWS DEMAND DATES----------------------------------------------

      ScrollBoxConstructor demandDateModel = new ScrollBoxConstructor(0,y);
      demandDateModel.setFont(25, 25);
      demandDateModel.setMargins(000, 0, 0, 0);
      demandDateModel.setWidth(50);
      demandDateModel.drawHeaders(true);
      demandDateModel.addColumn(0, 0, Align.CENTER, "", "demandDate");

      demandDateModel.setOrigin(GAP_WIDTH * 2 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandA = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 3 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandB = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 4 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandC = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 5 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandD = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 6 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandE = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 7 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandF = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 8 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandG = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 9 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandH = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 10 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandI = demandDateModel.build();
      demandDateModel.setOrigin(GAP_WIDTH * 11 + offset_width/2, GAP_HEIGHT + offset_height + offset_height/4); demandJ = demandDateModel.build();
      
   }

    private void updateList() {

      cartonList = getBuildListPQ();

      if (cartonList == null || cartonList.isEmpty()) {
         alert("update list empty");
         return;
      }

      //drawLists();      
   }

   @Override
   public void initDisplay() {
      super.initDisplay();
      initList();

      helloText = new TextField(SCREEN_WIDTH/2, 0, 75, "Perishables/Liquids", Align.CENTER);

      waveText = new TextField(0, GAP_HEIGHT, 50, "Wave");
      toteText = new TextField(0, GAP_HEIGHT * 2 + offset_height, 60, "Tote");
      smallText = new TextField(0, GAP_HEIGHT * 4 + offset_height, 60, "Small");
      mediumText = new TextField(0, GAP_HEIGHT * 6 + offset_height, 60, "Medium");
      largeText = new TextField(0, GAP_HEIGHT * 8 + offset_height, 60, "Large");
      exportText = new TextField(0, GAP_HEIGHT * 10 + offset_height, 60, "Export");

      bkgdA = new Rectangle(GAP_WIDTH * 2 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdB = new Rectangle(GAP_WIDTH * 3 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdC = new Rectangle(GAP_WIDTH * 4 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdD = new Rectangle(GAP_WIDTH * 5 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdE = new Rectangle(GAP_WIDTH * 6 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdF = new Rectangle(GAP_WIDTH * 7 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdG = new Rectangle(GAP_WIDTH * 8 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdH = new Rectangle(GAP_WIDTH * 9 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdI = new Rectangle(GAP_WIDTH * 10 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      bkgdJ = new Rectangle(GAP_WIDTH * 11 - offset_width / 2 , GAP_HEIGHT, GAP_WIDTH, GAP_HEIGHT * 11, Constants.INVISIBLE, 1, "black", true );
      
   }

   public void drawLists() {
      //---------------------------Draw Values On Screen Per Wave--------------------------------
      
      //Each wave has waveCount values to display. 5 for carton counts and 5 for order lines counts
      
      int waveSeq = -1;
      
      int numWaves = cartonList.size() / 5;
      int remainingWaves = cartonList.size() / 5;
      boolean drawWave = false;
      int i = 0;

      String pickTypePQ = String.format("%s','%s", PICKTYPE_PERISHABLES, PICKTYPE_LIQUIDS);
      if (numWaves >= 1) {

//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         //Draw cartonCount list
         listAA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listAB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listAC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listAD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listAE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listAA.show(); listAB.show(); listAC.show(); listAD.show(); listAE.show();

         //Draw cartonCount list
         listKA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listKB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listKC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listKD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listKE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listKA.show(); listKB.show(); listKC.show(); listKD.show(); listKE.show();
      
         //Draw the waveNumber at the top
         waveA.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveA.show();

         //Draws the demandDate under waveNumber
         demandA.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandA.show();
         
         //Set the background based upon stamps set in rdsWaves
         bkgdA.updateFill(bkgdColor);
         bkgdA.show();

         //Draw the vertical line seperator
         rectA = new Rectangle(GAP_WIDTH * 3 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectA.show();
         
         i++;
         drawWave = false;
      } 

      if (numWaves >= 2) {

//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         listBA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listBB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listBC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listBD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listBE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listBA.show(); listBB.show(); listBC.show(); listBD.show(); listBE.show();

         listLA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listLB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listLC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listLD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listLE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listLA.show(); listLB.show(); listLC.show(); listLD.show(); listLE.show();
         
         waveB.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveB.show();

         demandB.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandB.show();

         bkgdB.updateFill(bkgdColor);
         bkgdB.show();

         rectB = new Rectangle(GAP_WIDTH * 3 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectB.show();

         i++;
         drawWave = false;
      }

      if (numWaves >= 3) {

//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }
         
         listCA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listCB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listCC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listCD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listCE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listCA.show(); listCB.show(); listCC.show(); listCD.show(); listCE.show();

         listMA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listMB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listMC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listMD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listME.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listMA.show(); listMB.show(); listMC.show(); listMD.show(); listME.show();
         
         waveC.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveC.show();

         demandC.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandC.show(); 

         bkgdC.updateFill(bkgdColor);
         bkgdC.show();

         rectC = new Rectangle(GAP_WIDTH * 4 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectC.show();
         
         i++;
         drawWave = false;
      }

      if (numWaves >= 4) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }
          
         listDA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listDB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listDC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listDD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listDE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listDA.show(); listDB.show(); listDC.show(); listDD.show(); listDE.show();

         listNA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listNB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listNC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listND.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listNE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listNA.show(); listNB.show(); listNC.show(); listND.show(); listNE.show();
         
         waveD.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveD.show();

         demandD.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandD.show();

         bkgdD.updateFill(bkgdColor);
         bkgdD.show();

         rectD = new Rectangle(GAP_WIDTH * 5 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectD.show();
         //term.setRectangle(14, GAP_WIDTH * 5 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );

         i++;
         drawWave = false;
      }

      if (numWaves >= 5) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         listEA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listEB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listEC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listED.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listEE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listEA.show(); listEB.show(); listEC.show(); listED.show(); listEE.show();

         listOA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listOB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listOC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listOD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listOE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listOA.show(); listOB.show(); listOC.show(); listOD.show(); listOE.show();
         
         waveE.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveE.show();

         demandE.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandE.show(); 

         bkgdE.updateFill(bkgdColor);
         bkgdE.show();

         rectE = new Rectangle(GAP_WIDTH * 5 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectE.show();
         //term.setRectangle(15, GAP_WIDTH * 6 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );

         i++;
         drawWave = false;
      }

      if (numWaves >= 6) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }
          
         listFA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listFB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listFC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listFD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listFE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listFA.show(); listFB.show(); listFC.show(); listFD.show(); listFE.show();

         listPA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listPB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listPC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listPD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listPE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listPA.show(); listPB.show(); listPC.show(); listPD.show(); listPE.show();
         
         waveF.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveF.show();

         demandF.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandF.show(); 

         bkgdF.updateFill(bkgdColor);
         bkgdF.show();

         rectF = new Rectangle(GAP_WIDTH * 7 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectF.show();

         i++;
         drawWave = false;
      }

      if (numWaves >= 7) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         listGA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listGB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listGC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listGD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listGE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listGA.show(); listGB.show(); listGC.show(); listGD.show(); listGE.show();

         listQA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listQB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listQC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listQD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listQE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listQA.show(); listQB.show(); listQC.show(); listQD.show(); listQE.show();
         
         waveG.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveG.show();

         demandG.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandG.show(); 

         bkgdG.updateFill(bkgdColor);
         bkgdG.show();

         rectG = new Rectangle(GAP_WIDTH * 8 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectG.show();

         i++;
         drawWave = false;
      }

      if (numWaves >= 8) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }
          
         listHA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listHB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listHC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listHD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listHE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listHA.show(); listHB.show(); listHC.show(); listHD.show(); listHE.show();

         listRA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listRB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listRC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listRD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listRE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listRA.show(); listRB.show(); listRC.show(); listRD.show(); listRE.show();
         
         waveH.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveH.show();

         demandH.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandH.show(); 

         bkgdH.updateFill(bkgdColor);
         bkgdH.show();

         rectH = new Rectangle(GAP_WIDTH * 8 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectH.show();
         
         i++;
         drawWave = false;
      }

      if (numWaves >= 9) {
         
//         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
//            inform("background color for index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
//               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         listIA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listIB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listIC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listID.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listIE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listIA.show(); listIB.show(); listIC.show(); listID.show(); listIE.show();

         listSA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listSB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listSC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listSD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listSE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listSA.show(); listSB.show(); listSC.show(); listSD.show(); listSE.show();
         
         waveI.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveI.show();

         demandI.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandI.show(); 

         bkgdI.updateFill(bkgdColor);
         bkgdI.show();

         rectI = new Rectangle(GAP_WIDTH * 10 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black");
         rectI.show();
         

         i++;
         drawWave = false;
      }

      if (numWaves >= 10) {
         
         inform("numWaves = [%d], remainingWaves = [%d]", numWaves, remainingWaves);
         while (drawWave == false) {
            bkgdColor = getBackgroundColor(cartonList, i, pickTypePQ);
            inform("background color for waveNum 10, index [%d] is [%s]", i, bkgdColor);
            if (remainingWaves > 10 && bkgdColor == "green") {
               inform("removing wave index [%d]",i);
               //inform("numRemaining: [%d] > 10, bkgdColor: [%s] == green ", remainingWaves, bkgdColor);
               i++;
               remainingWaves--;
             }
            else {
               inform("drawing wave index [%d]", i);
               drawWave = true;
            }
          }

         listJA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listJB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listJC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listJD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listJE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listJA.show(); listJB.show(); listJC.show(); listJD.show(); listJE.show();

         listTA.updateDisplayList(cartonList.subList(i * 5, (i * 5) + 1));
         listTB.updateDisplayList(cartonList.subList((i * 5) + 1, (i * 5) + 2));
         listTC.updateDisplayList(cartonList.subList((i * 5) + 2,(i * 5) + 3));
         listTD.updateDisplayList(cartonList.subList((i * 5) + 3,(i * 5) + 4));
         listTE.updateDisplayList(cartonList.subList((i * 5) + 4,(i * 5) + 5));
         listTA.show(); listTB.show(); listTC.show(); listTD.show(); listTE.show();
         
         waveJ.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         waveJ.show();

         demandJ.updateDisplayList(cartonList.subList((i * 5), (i * 5) + 1));
         demandJ.show(); 

         bkgdJ.updateFill(bkgdColor);
         bkgdJ.show();

         rectJ = new Rectangle(GAP_WIDTH * 11 - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
         rectJ.show();

         i++;
         drawWave = false;
      }

      //Draws the final rectangle to close the chart
      term.setRectangle(21, GAP_WIDTH * (numWaves + 2)  - offset_width/2, GAP_HEIGHT, GAP_HEIGHT/24, SCREEN_HEIGHT - GAP_HEIGHT * 4, "black", 1, "black" );
   }

   public void hideLists() {
      listAA.hide(); listAB.hide(); listAC.hide(); listAD.hide(); listAE.hide(); 
      listBA.hide(); listBB.hide(); listBC.hide(); listBD.hide(); listBE.hide(); 
      listCA.hide(); listCB.hide(); listCC.hide(); listCD.hide(); listCE.hide(); 
      listDA.hide(); listDB.hide(); listDC.hide(); listDD.hide(); listDE.hide(); 
      listEA.hide(); listEB.hide(); listEC.hide(); listED.hide(); listEE.hide(); 
      listFA.hide(); listFB.hide(); listFC.hide(); listFD.hide(); listFE.hide(); 
      listGA.hide(); listGB.hide(); listGC.hide(); listGD.hide(); listGE.hide(); 
      listHA.hide(); listHB.hide(); listHC.hide(); listHD.hide(); listHE.hide(); 
      listIA.hide(); listIB.hide(); listIC.hide(); listID.hide(); listIE.hide(); 
      listJA.hide(); listJB.hide(); listJC.hide(); listJD.hide(); listJE.hide(); 
      listKA.hide(); listKB.hide(); listKC.hide(); listKD.hide(); listKE.hide(); 
      listLA.hide(); listLB.hide(); listLC.hide(); listLD.hide(); listLE.hide(); 
      listMA.hide(); listMB.hide(); listMC.hide(); listMD.hide(); listME.hide(); 
      listNA.hide(); listNB.hide(); listNC.hide(); listND.hide(); listNE.hide(); 
      listOA.hide(); listOB.hide(); listOC.hide(); listOD.hide(); listOE.hide(); 
      listPA.hide(); listPB.hide(); listPC.hide(); listPD.hide(); listPE.hide(); 
      listQA.hide(); listQB.hide(); listQC.hide(); listQD.hide(); listQE.hide(); 
      listRA.hide(); listRB.hide(); listRC.hide(); listRD.hide(); listRE.hide(); 
      listSA.hide(); listSB.hide(); listSC.hide(); listSD.hide(); listSE.hide(); 
      listTA.hide(); listTB.hide(); listTC.hide(); listTD.hide(); listTE.hide(); 
      waveA.hide(); waveB.hide(); waveC.hide(); waveD.hide(); waveE.hide(); waveF.hide(); waveG.hide(); waveH.hide(); waveI.hide(); waveJ.hide(); 
      demandA.hide(); demandB.hide(); demandC.hide(); demandD.hide(); demandE.hide(); demandF.hide(); demandG.hide(); demandH.hide(); demandI.hide(); demandJ.hide();

      bkgdA.updateFill(Constants.INVISIBLE); bkgdB.updateFill(Constants.INVISIBLE); bkgdC.updateFill(Constants.INVISIBLE); bkgdD.updateFill(Constants.INVISIBLE); bkgdE.updateFill(Constants.INVISIBLE); bkgdF.updateFill(Constants.INVISIBLE); bkgdG.updateFill(Constants.INVISIBLE); bkgdH.updateFill(Constants.INVISIBLE); bkgdI.updateFill(Constants.INVISIBLE); bkgdJ.updateFill(Constants.INVISIBLE); 
      bkgdA.hide(); bkgdB.hide(); bkgdC.hide(); bkgdD.hide(); bkgdE.hide(); bkgdF.hide(); bkgdG.hide(); bkgdH.hide(); bkgdI.hide(); bkgdJ.hide();

      rectA.hide(); rectB.hide(); rectC.hide(); rectD.hide(); rectE.hide(); rectF.hide(); rectG.hide(); rectH.hide(); rectI.hide(); rectJ.hide();
   }

}