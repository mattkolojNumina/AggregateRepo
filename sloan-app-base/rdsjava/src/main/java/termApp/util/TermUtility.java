package termApp.util;

import java.util.LinkedHashMap;
import java.util.Map;

import term.TerminalDriver;
import termApp.AbstractScreen;

public abstract class TermUtility {
   protected static TerminalDriver term;
   protected static final int MASTER_TAG_INIT = 1;
   protected static final boolean TERM_DEBUG = true;

   protected static int masterTag = MASTER_TAG_INIT;
   private static Map<Class <? extends TermBaseObject>,TermGroup> all;

   /*
    * static methods
    */
   /**
    * Resets master tag to the init value. Needs to be called on every screen change.
    */
   public static final void clearTags() {
      masterTag = MASTER_TAG_INIT;
      try { all.clear(); }
      catch (NullPointerException ex ) {}
   }
  
   /**
    * Assigns the current terminal driver for all objects
    * 
    * @param term terminal driver instance
    */
   public static void setTerm( TerminalDriver term, AbstractScreen screen ) {
      TermUtility.term = term;
      TermObject.screen = screen ;
      TermObject.term = term;
      all = new LinkedHashMap<Class <? extends TermBaseObject>,TermGroup>();
   }
   
   public static int getTag() {
      return masterTag++;
   }
   
   protected static void addToAll(TermBaseObject termObject) {
      Class<? extends TermBaseObject> objClass = termObject.getClass();
      if (all.containsKey(objClass)) {
         all.get(objClass).add(termObject);
      } else {
         all.put(objClass, new TermGroup(termObject));
      }
   }

   /**
    * @return the all text entries
    */
   public static TermGroup getTextEntries() {
      return all.get(TextEntry.class);
   }
   
   /**
    * @return the all password entries
    */
   public static TermGroup getPasswordEntries() {
      return all.get(PasswordEntry.class);
   }
   
   /**
    * @return the all buttons
    */
   public static TermGroup getButtons() {
      return all.get(Button.class);
   }
   
   /**
    * @return the all
    */
   public static Map<Class <? extends TermBaseObject>,TermGroup> getAll() {
      return all;
   }

   
   /**
    * A method for alerting errors in term.util that is enabled by a parameter.
    * Calls {@linkplain rds.RDSLog.alert} on the formated string when {@link #TERM_DEBUG} is true.
    * 
    * @param format format of the string
    * @param args format parameters
    */
   protected final static void error( String format, Object... args ) {
      if (TERM_DEBUG)
         rds.RDSLog.alert(format, args);
   }

}
