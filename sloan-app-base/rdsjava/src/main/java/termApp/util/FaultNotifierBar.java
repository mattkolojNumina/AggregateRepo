package termApp.util;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import java.util.ArrayList;
import java.util.List;

import termApp.util.TermActionObject.OnActionListener;

/*
 * FaultNotifier sub class
 *   instantiated on demand
 *   disabled by default
 *   time is updated every tick (but not displayed if disabled)
 */

public class FaultNotifierBar 
extends TermObject {

   private boolean init;
   private Button reset;
   private TextField message;
   private Rectangle border, box;
   private List<FaultEvent> events;
   private FaultEvent current;
   
   private int msgW, msgX0, msgY0, marqueShift;
   private boolean enableMarque, trigMarque;
   private long startMarque, endMarque;

   private int width, height, font, margin;
   private boolean aboveFooter;
   
   private static final int FNB_WIDTH  = SCREEN_WIDTH;
   private static final int FNB_HEIGHT = 90;
   private static final int FNB_FONT   = 60;
   private static final int FNB_MARGIN = 10;
   
   private static final String FNB_COLOR = "$006060FF";

   private static final int MARQUE_EDGE = 0;
   private static final int MARQUE_RATE = -100; //pixels/second
   private static final int MARQUE_WIDTH = SCREEN_WIDTH - 300;
   private static final long MARQUE_DELAY = 2000L;
   
   public FaultNotifierBar() {
      width    = FNB_WIDTH;
      height   = FNB_HEIGHT;
      font     = FNB_FONT;
      margin   = FNB_MARGIN;
      
      aboveFooter = false;
      init = false;
      reset = null;
      message = null;
      border = null;
      box = null;
      events = new ArrayList<FaultEvent>();
      enableMarque = true;
      marqueShift = (int)(MARQUE_RATE * term.getPoll()/1000);
   }
   
   /*
   public static FaultNotifierBar getFaultNotifierBar() {
      if (single_instance == null)
         single_instance =  new FaultNotifierBar();
      
      return single_instance;
   }
   */
   
   public void addEvent(FaultEvent event) {
      events.add(event);
   }
   
   private boolean isActive() {
      return current != null;
   }
   
   public void aboveFooter() {
      aboveFooter = true;
   }
   
   public void init() {
      if (init)
         return;
      
      int x0 = 0;
      int y0 = getBarY0();
      int x, y, w, h, f, b;
      
      x = width - margin;
      y = y0 + margin;
      f = font - margin;
      reset = new Button(x,y,f,"Reset",Align.RIGHT,false);
      reset.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            current.reset();
            faultReset();
         }
      });
      
      x = MARQUE_WIDTH;
      w = SCREEN_WIDTH - x ;
      h = height;
      y = y0 + margin * 3 /2;
      box = new Rectangle(x,y,w,h,FNB_COLOR,0,"Black",false); 

      x = x0 + margin;
      msgX0 = x;
      msgY0 = y;
      message = new TextField( x, y, f,"black","", Align.LEFT, false);
      
      b = margin/2;
      x = x0 - b;
      y = y0;
      w = width + 2*b;
      h = height + 2*b;
      border = new Rectangle(x,y,w,h,FNB_COLOR,b,"Black",false); 
      
      init = true;
   }
   
   private int getBarY0() {
      return SCREEN_HEIGHT-height * (aboveFooter ? 2 : 1);
   }

   public void tick() {
      if (isActive()) {
         if (!current.checkIfOn())
            faultReset();
         else 
            displayTick();
      } else {
         for (FaultEvent e : events) {
            if (e.checkIfOn() && e.delayElapsed()) {
               faultActive(e);
               return;
            }
         }
      }
   }

   private void displayTick() {
      if (!enableMarque || !trigMarque)
         return;
      int msgX = message.getX();
      
      long now = System.currentTimeMillis();
      if (startMarque + MARQUE_DELAY > now) 
         return;
      
      
      if (msgW+msgX < getMarqueWidth() - MARQUE_EDGE) {
         if (endMarque == 0)
            endMarque = now;
         if (endMarque + MARQUE_DELAY > now)
            return;
         
         startMarque = now;
         endMarque = 0L;
         message.move(msgX0, msgY0);
      } else
         message.shift(marqueShift, 0);
      
   };
   
   private void faultActive(FaultEvent e) {
      current = e;
      String msg = current.getMessage();
      setMessage(msg);
      alert("fault detected: [%s]", msg);
      show();
   }

   private int getMarqueWidth() {
      if (current == null || current.resetable())
         return MARQUE_WIDTH;
      return SCREEN_WIDTH;
   }
   
   private void setMessage(String msg) {
      message.setText(msg);
      message.move(msgX0, msgY0);
      
      msgW = message.getWidth();
      startMarque = System.currentTimeMillis();
      endMarque = 0L;
      trigMarque = msgW > getMarqueWidth();
   }

   private void faultReset() {
      String msg = current.getMessage();
      message.hide();
      alert("fault reset: [%s]", msg);
      current = null;
      tick();
      if (!isActive())
         hide();
   }
   
   public void show() {
      super.show();
      if(!aboveFooter)
         screen.getFooter().hide();
      border.show();
      if (current.resetable()) {
         reset.show();
      } else {
         reset.hide();
      }
      box.move(getMarqueWidth(), getBarY0() + margin * 3 /2);
      box.show();
      message.show();
   }
   
   public void hide() {
      super.hide();
      screen.getFooter().show();
      message.hide();         
      reset.hide();
      box.hide();
      border.hide();
   }
}
