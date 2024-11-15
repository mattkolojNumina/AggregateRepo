/*
 * ReplicationMonitor.java
 * 
 * (c) 2010, Numina Group, Inc.
 */

package replication;

import java.util.Timer;
import java.util.TimerTask;

import rds.RDSDatabase;
import rds.RDSEvent;
import rds.RDSUtil;

/**
 * A utility for monitoring the status of database replication.
 */
public class ReplicationMonitor {

   /*
    * --- constants ---
    */

   /** The polling period for replication monitoring (msec). */
   private static final long MONITOR_PERIOD = 600000L;

   /** The delay before testing the replication slave (msec). */
   private static final long MONITOR_DELAY = 10000L;


   /*
    * --- class variables ---
    */

   /** The hostname of the slave database. */
   private String slaveDatabaseName;

   /** The master and slave databases. */
   private RDSDatabase masterDatabase;
   private RDSDatabase slaveDatabase;


   /*
    * --- constructor ---
    */

   /**
    * Constructs a utility to monitor the specified named slave database.
    * 
    * @param   slaveDatabaseName  the hostname of the slave database
    */
   public ReplicationMonitor( String slaveDatabaseName ) {
      this.masterDatabase = new RDSDatabase( "localhost" );

      this.slaveDatabaseName = slaveDatabaseName;
      this.slaveDatabase = new RDSDatabase( slaveDatabaseName );

      RDSEvent.setDatabase( masterDatabase );
   }


   /*
    * --- processing ---
    */

   /** Schedules the periodic monitor. */
   private void startMonitor() {
      TimerTask monitorTask = new TimerTask() {
         public void run() {
            monitor();
         }
      };
      (new Timer()).schedule( monitorTask,
            MONITOR_PERIOD, MONITOR_PERIOD );
   }

   /**
    * Monitors database replication.  An entry is posted into the master
    * database; after a brief delay, the slave database is checked to
    * verify that the entry was properly replicated.
    */
   private void monitor() {
      String masterStamp = masterDatabase.getValue(
            "SELECT NOW()",
            "" );
      if ("".equals( masterStamp )) {
         RDSUtil.alert( "unable to test database replication on %s",
               slaveDatabaseName );
         return;
      }

      masterDatabase.execute(
            "REPLACE INTO runtime SET " +
            "name = 'replication/%s/stamp', " +
            "value = '%s'",
            slaveDatabaseName, masterStamp );

      try {
         Thread.sleep( MONITOR_DELAY );
      } catch (InterruptedException ex) {
         RDSUtil.alert( ex );
         return;
      }

      String slaveStamp = slaveDatabase.getValue( String.format(
            "SELECT value FROM runtime " +
            "WHERE name = 'replication/%s/stamp'",
            slaveDatabaseName ), "" );

      if ("".equals( slaveStamp )) {
         RDSEvent.start( "replication/%s-nodb", slaveDatabaseName );
      } else if (!masterStamp.equals( slaveStamp )) {
         RDSEvent.stop( "replication/%s-nodb", slaveDatabaseName );
         RDSEvent.start( "replication/%s-repl", slaveDatabaseName );
      } else {
         RDSEvent.stop( "replication/%s-nodb", slaveDatabaseName );
         RDSEvent.stop( "replication/%s-repl", slaveDatabaseName );
      }
   }


   /*
    * --- main ---
    */

   /**
    * The application entry point.
    * 
    * @param   args  a list of command-line arguments
    */
   public static void main( String... args ) {
      if (args.length == 1) {
         String slaveDatabaseName = args[0];
         RDSUtil.trace( "begin monitoring replication on %s",
               slaveDatabaseName );
         ReplicationMonitor replicationMonitor =
               new ReplicationMonitor( slaveDatabaseName );
         replicationMonitor.startMonitor();
      } else {
         RDSUtil.alert( "no replication-slave database specified" );
      }
   }

}
