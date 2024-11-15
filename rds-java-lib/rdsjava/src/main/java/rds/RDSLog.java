/*
 * RDSLog.java
 * 
 * (c) 2007--2012 Numina Group, Inc.
 */

package rds;

import rds.Logger.LogLevel;


/**
 * Utility class for generating log messages for RDS applications.
 */
public final class RDSLog {

   /**
    * Posts a formatted log message at the specified priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *           the format string
    * @see String#format(String, Object...)
    */
   public static void trace( LogLevel level, String format,
         Object... args ) {
      String text = String.format( format, args );

      String traceText;
      if (level == LogLevel.Alert)
         traceText = "A:" + text.replaceAll( "\n", "\nA:   " );
      else if (level == LogLevel.Inform)
         traceText = "I:" + text.replaceAll( "\n", "\nI:   " );
      else
         traceText = "T:" + text.replaceAll( "\n", "\nT:   " );

      System.out.println( traceText );
   }

   /**
    * Posts a formatted log message at the normal-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void trace( String format, Object... args ) {
      trace( LogLevel.Trace, format, args );
   }

   /**
    * Posts a formatted log message at the high-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void alert( String format, Object... args ) {
      trace( LogLevel.Alert, format, args );
   }

   /**
    * Posts an alert message associated with a thrown exception or error.
    * 
    * @param   t  the {@code Throwable} (typically an {@code Exception}
    *          or {@code Error}) to alert
    */
   public static void alert( Throwable t ) {
      alert( "%s", t.toString() );
   }

   /**
    * Posts an alert message for a thrown exception or error, along with
    * the associated stack trace.
    * 
    * @param   t  the {@code Throwable} (typically an {@code Exception}
    *          or {@code Error}) to alert
    */
   public static void alertStackTrace( Throwable t ) {
      alert( t );
      t.printStackTrace( System.out );
   }

   /**
    * Posts a formatted log message at the low-priority level.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void inform( String format, Object... args ) {
      trace( LogLevel.Inform, format, args );
   }



   /**
    * A concrete implementation of the {@code Logger} interface, with
    * default behavior specified via the static methods of this utility
    * class.
    */
   public static class RDSLogger
         implements Logger {

      public void trace( String format, Object... args ) {
         RDSLog.trace( format, args );
      }

      public void alert( String format, Object... args ) {
         RDSLog.alert( format, args );
      }

      public void inform( String format, Object... args ) {
         RDSLog.inform( format, args );
      }
   }


   /** Utility method to enable logging of focus events for debugging. */
   public static void enableFocusLogging() {
      // Obtain a reference to the logger
      java.util.logging.Logger focusLog = java.util.logging.Logger.getLogger(
            "java.awt.focus.Component" );

      // the logger should log all messages
      focusLog.setLevel( java.util.logging.Level.ALL );

      // create a new handler
      java.util.logging.ConsoleHandler handler =
            new java.util.logging.ConsoleHandler();

      // the handler must handle all messages
      handler.setLevel( java.util.logging.Level.ALL );

      // add the handler to the logger
      focusLog.addHandler(handler);
  }

}
