/*
 * AbstractPollingApp.java
 * 
 * Generic polling application.
 * 
 * (c) 2013-2016, Numina Group, Inc.
 */

package polling;


import java.util.*;

import rds.*;
import static rds.RDSLog.*;


/**
 * Base class for an application that performs a repeated polling task.
 */
public abstract class AbstractPollingApp {

   /* Default settings. */
   protected static final String DEFAULT_ID = "app";
   protected static final String DEFAULT_RDS_DB = "db";
   private static final long DEFAULT_POLL_DELAY     = 10 * 1000L;  // msec
   private static final long DEFAULT_POLL_PERIOD    = 60 * 1000L;  // msec
   private static final long DEFAULT_MAX_TIME       =  1 * 1000L;  // msec
   private static final long DEFAULT_SLEEP_DURATION =       100L;  // msec

   /* Timer periods for communications (msec). */
   private static final long MONITOR_PERIOD  = 10 * 1000L;  // msec
   protected static final int LOCK_TIMEOUT   = 10;  // seconds

   /* Other constants. */
   protected static final String ZERO_STAMP = "0000-00-00 00:00:00";


   /*
    * --- class variables ---
    */

   /** The application ID. */
   protected String id;

   /** The database connection. */
   protected RDSDatabase db;

   /** Control parameters for the application. */
   protected Map<String,String> params;


   /*
    * --- constructor/initialization/access ---
    */

   /** Constructs the class for processing. */
   public AbstractPollingApp( String id, String rdsDb ) {
      this.id = id;
      this.db = new RDSDatabase( rdsDb );

      init();
   }

   /** Performs initialization. */
   protected void init() {
      RDSCounter.setDatabase( db );
      RDSHistory.setDatabase( db );
      RDSEvent.setDatabase( db );

      params = db.getControlMap( id );
   }

   /** Sets the named control parameter. */
   protected void setParam( String name, String value ) {
      if (params == null)
         return;
      params.put( name, value );
   }

   /** Gets the named control parameter. */
   protected String getParam( String name, String otherwise ) {
      if (params == null)
         return otherwise;
      String value = params.get( name );
      return (value == null) ? otherwise : value;
   }

   /** Gets the named control parameter as an integer. */
   protected int getIntParam( String name, int otherwise ) {
      return RDSUtil.stringToInt( getParam( name, "" ), otherwise );
   }

   /** Gets a lock with the specified name. */
   protected boolean lock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT GET_LOCK( '%s', %d )",
            lockName, LOCK_TIMEOUT );
      return (lockVal == 1);
   }

   /** Releases a lock with the specified name. */
   protected boolean unlock( String lockName ) {
      int lockVal = db.getInt( -1,
            "SELECT RELEASE_LOCK( '%s' )",
            lockName );
      return (lockVal == 1);
   }


   /*
    * --- connections ---
    */

   /** Monitors the open connections, (re-)establishing if necessary. */
   protected synchronized boolean monitorConnections() {
      if (db == null || !db.isValid()) {
         alert( "rds database connection invalid" );
         disconnect();
         return false;
      }

      if (!db.ping()) {
         alert( "rds database connection lost" );
         disconnect();
         return false;
      }

      return true;
   }

   /** Closes the database connection. */
   protected synchronized void disconnect() {
      if (db != null)
         db.disconnect();
   }


   /*
    * --- processing/communications ---
    */

   /** Performs a period polling task. */
   protected abstract void poll();

   /** Gets the initial delay before polling starts. */
   protected long getPollDelay() {
      return getIntParam( "pollDelay", (int)DEFAULT_POLL_DELAY );
   }

   /** Gets the polling period. */
   protected long getPollPeriod() {
      return getIntParam( "pollPeriod", (int)DEFAULT_POLL_PERIOD );
   }

   /** Gets the max time for processing. */
   protected long getMaxTime() {
      return getIntParam( "maxTime", (int)DEFAULT_MAX_TIME );
   }

   /** Gets the sleep duration following an error. */
   protected long getSleepDuration() {
      return getIntParam( "sleepDuration", (int)DEFAULT_SLEEP_DURATION );
   }

   /** Runs periodic tasks. */
   protected void run() {
      scheduleMonitor();
      schedulePolling();
   }

   /** Runs the timer to periodically monitor the data connections. */
   private void scheduleMonitor() {
      TimerTask monitorTask = new TimerTask() {
         public void run() {
            try {
               monitorConnections();
            } catch (Exception ex) {
               RDSLog.alert( "error during monitor" );
               ex.printStackTrace();
               disconnect();
            }
         }
      };
      (new Timer()).schedule( monitorTask, 0, MONITOR_PERIOD );
   }

   /** Runs the timer to periodically check for triggered transactions. */
   private void schedulePolling() {
      TimerTask t = new TimerTask() {
         public void run() {
            try {
               poll();
            } catch (Exception ex) {
               alert( "error during polling" );
               ex.printStackTrace();
               disconnect();
            }
         }
      };
      long delay = getPollDelay();
      long period = getPollPeriod();
      if (period > 0) {
         inform( "schedule polling: delay %d, period %d", delay, period );
         (new Timer()).schedule( t, delay, period );
      }
   }


   /*
    * --- utilities ---
    */

   /** Gets a value from a {@code Map} or an empty string. */
   protected static String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String v = m.get( name );
      return (v == null) ? "" : v;
   }

   /** Gets a value from a {@code Map} and converts it to an int. */
   protected static int getMapInt( Map<String,String> m, String name ) {
      if (m == null)
         return -1;
      return RDSUtil.stringToInt( m.get( name ), -1 );
   }

   /** Gets a value from a {@code Map} and converts it to a double. */
   protected static double getMapDbl( Map<String,String> m, String name ) {
      if (m == null)
         return 0.0;
      return RDSUtil.stringToDouble( m.get( name ), 0.0 );
   }

   /** An {@code Exception} due to invalid data. */
   public static class DataException
         extends Exception {
      public DataException( String message ) {
         super( message );
      }
      public DataException( String message, Throwable cause ) {
         super( message, cause );
      }
   }

   /** An {@code Exception} due to failure during processing. */
   public static class ProcessingException
         extends Exception {
      public ProcessingException( String message ) {
         super( message );
      }
      public ProcessingException( String message, Throwable cause ) {
         super( message, cause );
      }
   }


   /*
    * --- main ---
    */

   /**
    * Application entry point.
    * 
    * @param   args  command-line arguments
    */
   public static void main( String... args ) {
      String id = (args.length > 0) ? args[0] : DEFAULT_ID;
      String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;

      trace( "application started, id = [%s], db = [%s]", id, rdsDb );

/* example implementation:
      App app = new App( id, rdsDb );

      app.run();
*/
   }

}
