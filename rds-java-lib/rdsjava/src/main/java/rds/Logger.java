/*
 * Logger.java
 */

package rds;


public interface Logger {
   /**
    * The priority level for trace messages.
    */
   public static enum LogLevel {
      /** High-priority message level for errors, etc. */
      Alert,

      /** Normal-priority message level for status messages, etc. */
      Trace,

      /** Low-priority message level for non-critical info, etc. */
      Inform
   }


   /**
    * Posts a formatted log message at the normal-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public void trace( String format, Object... args );

   /**
    * Posts a formatted log message at the high-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public void alert( String format, Object... args );

   /**
    * Posts a formatted log message at the low-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public void inform( String format, Object... args );
}
