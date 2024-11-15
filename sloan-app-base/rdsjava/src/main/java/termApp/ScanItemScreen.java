package termApp;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;
import java.util.*;

import term.TerminalDriver;
import termApp.util.*;
import termApp.util.TermActionObject.*;
//import java.util.*;

public class ScanItemScreen
      extends AbstractNuminaScreen {

   private String scan;
   private TextEntry textBox;
   public ScanItemScreen( TerminalDriver term ) {
      super( term );
     //operators should be allowed to logout; this makes the footer display a logout button
     setLogoutAllowed(true); 
   }

   /*
    * interface methods
    */
    List<TextField> skuInfo = new ArrayList<>();
    List<Button> allButtons = new ArrayList<>();

   public void handleInit() {
      term.clearScreen( DEFAULT_SCREEN_COLOR );
      super.handleInit();
      header.init();
      header.updateTitle("SCAN ITEM SCREEN");
      footer.show();

      String text =getParam("cartonID");
      inform("VALUE OF CARTON ID IS: [%s]",text);

      statusMsg.updateText("Carton ID: %s",text);
      int getNumOfSku = getNumOfSku(text);
      List<String> myTest = new ArrayList<>();
      myTest= getSku(text);
            
      int[] quantity = new int[getNumOfSku];
      int[] cartonTotal = new int[getNumOfSku];
      TextField infoHeader = new TextField(200, 475, 75, "Item            Total          In Carton ", true);


      for(int i=0; i<getNumOfSku; i++){
         quantity[i]=getQuantity(myTest.get(i));
         String initText = String.format("%s                %d                  %d", myTest.get(i), quantity[i],cartonTotal[i]);
         String initText2 = String.format("Put 1 %s", myTest.get(i));
         TextField info = new TextField(200,575 + (100*i), 75,initText, true);
         Button myButton = new Button(1500,575 + (100*i), 50,initText2 , Align.CENTER,true);
         myButton.setCount(quantity[i]);
         myButton.setID(i);
         myButton.setSku(myTest.get(i));
         myButton.setCompletion(false);
         myButton.registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
               myButton.counter();
               inform("button pressed, value is: %d",myButton.countTotal());
               int i = myButton.getID();
               skuInfo.get(i).clear();
               String initText3 = String.format("%s                %d                  %d", myButton.getSku(), quantity[i],myButton.countTotal());
               TextField infoUpdate = new TextField(200,575 + (100*i), 75,initText3, true);
               skuInfo.set(i,infoUpdate);

               if(checkMatch(myButton))
                  myButton.hide();

               if(checkCompletion(allButtons))
                  setNextScreen("SuccessScreen");
               
            }
         });
         skuInfo.add(info);
         allButtons.add(myButton);
      }
      statusMsg.show();      

   

      //create the button, make it in the lower-mid part of the screen, make it say "OK",
      //and make it visible by default
      //textBox = new TextEntry(100, 3 * (SCREEN_HEIGHT/4), 60, 500, 70, true);

      //make the textbox do something when someone enters text
      /*
      textBox.registerOnTextActionListener(new OnTextActionListener() {
         @Override
         public void onAction(String text) {
            //evaluate the text to see if it's a good carton ID, change screen accordingly
            if((text==null) || (text.isEmpty())) return;
            inform("User typed in [%s]", text);
            setParam("cartonID", text);
            if (isCartonValid(text)) {
               setNextScreen("SuccessScreen");
            }
            else {
               setNextScreen("ErrorScreen");
            }
         }
      });
      */


   }

   public void handleTick() {
      super.handleTick();
      scan = getScan(); //See AbstractNuminaScreen
      if ((scan!=null) && (!scan.isEmpty())) setParam("SKU", scan);
      else return;

      List<String> myTest = new ArrayList<>();
      String text =getParam("cartonID");
      myTest= getSku(text);
      int getNumOfSku = getNumOfSku(text);
      boolean matchFound=false;
      int location=0;

      inform("Scan value is: '%s'",scan);

      for(int i=0; i<getNumOfSku; i++){
         inform("In my Test: '%s'",myTest.get(i));
         if(myTest.get(i).equals(scan)){
            inform("it works");
            matchFound = true;
            location = i;
            break;
         }
      }

      if(!matchFound)
      {
         setNextScreen("ErrorItemScreen");
      }

      else 
      {
         //inform("We are in here");
         //setParam("location", String.valueOf(location));
         Button myButton=allButtons.get(location);
         myButton.counter();
         skuInfo.get(location).clear();
         String initText3 = String.format("%s                %d                  %d", myButton.getSku(), myButton.getCount(),myButton.countTotal());
         TextField infoUpdate = new TextField(200,575 + (100*location), 75,initText3, true);
         skuInfo.set(location,infoUpdate);

         if(checkMatch(myButton))
            myButton.hide();

         if(checkCompletion(allButtons))
            setNextScreen("SuccessScreen");
      
      }
      
      /*
      if(isCartonValid(scan)) { //See AbstractNuminaScreen
         setNextScreen("SuccessScreen");
      }
      else {
         //setParam("cartonID",);
         setNextScreen("ErrorItemScreen");
      }
      */
   }

}
