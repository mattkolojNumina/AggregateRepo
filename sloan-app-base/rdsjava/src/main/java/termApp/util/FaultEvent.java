package termApp.util;

import java.util.HashMap;
import java.util.Map;

public class FaultEvent {
   boolean on;
   long delay, lastReset;
   String message;
   Map<String,String> param = new HashMap<String,String>();
   FaultAction trigOn, trigOff, reset;
   
   public FaultEvent() {
      delay = 0L;
      lastReset = 0L;
      setMessage(null);
   }
   
   public boolean checkIfOn() {
      faultTick();
      return isOn();
   }
   
   public boolean isOn() {
      return on;
   }
   
   public void setDelay( long d ) {
      delay = d;
   }
   
   public void setMessage( String msg ) {
      if (msg == null || msg.isEmpty())
         msg = "FAULT DETECTED";
      message = msg;
   }
   
   public String getMessage() {
      return message;
   }
   
   public boolean delayElapsed() {
      return (lastReset + delay <= System.currentTimeMillis());
   }
   
   public void setOnTrigger( FaultAction trigger ) {
      trigOn = trigger;
   }
   
   public void setOffTrigger( FaultAction trigger ) {
      trigOff = trigger;
   }
   
   public void setGlobalTrigger( FaultAction trigger ) {
      setOnTrigger(trigger);
      setOffTrigger(trigger);
   }
   
   public void setFaultReset(FaultAction fltReset) {
      reset = fltReset;         
   }
   
   public void reset() {
      if (!on || reset == null)
         return;
      reset.action();
      lastReset = System.currentTimeMillis();
   }
   
   public void faultTick() {
      
      if (on && trigOn != null) {
         if (!trigOn.action()) {
            on = false;
         }
      } else if (!on && trigOff != null) {
         if(trigOff.action()) {
            on = true; //state change
         }
      }
         
   }

   public interface FaultAction { 
      boolean action(); 
   }

   public boolean resetable() {
      return reset != null;
   }
}


