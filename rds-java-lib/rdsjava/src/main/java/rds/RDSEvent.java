/*
 * RDSEvent.java
 * 
 * (c) 2010, Numina Group, Inc.
 */

package rds;


import java.util.Map;


/**
 * A utility class that provides methods for managing system events.
 */
public final class RDSEvent {
   /** The database where event information is stored. */
   private static RDSDatabase db;


   // suppress default constructor to enforce non-instantiability
   private RDSEvent() {}


   /** Sets the database where event information is stored. */
   public static void setDatabase( RDSDatabase db ) {
      RDSEvent.db = db;
   }

   /**
    * Starts an event.  If the event does not exist, it is created.  If the
    * event is already started, no further action is taken.
    * 
    * @param   code  the event identifier code
    */
   public static void start( String code ) {
      Map<String,String> eventMap = getEventMap( code );
      if (eventMap == null)
         return;

      // do nothing if event is already started
      if ("on".equals( eventMap.get( "state" ) ))
         return;

      RDSUtil.trace( "start event: [%s]", code );

      // start the event
      db.execute(
            "UPDATE events SET " +
            "state = 'on', " +
            "start = NOW() " +
            "WHERE code = '%s'",
            code );

      // log the event
      db.execute(
            "INSERT eventLog " +
            "(code, state, start, duration) " +
            "VALUES ('%s', 'on', NOW(), 0)",
            code );

      notify( code, "start" );
   }

   /** Starts an event with a formatted event code. */
   public static void start( String format, Object... args ) {
      start( String.format( format, args ) );
   }

   /**
    * Stops an event.  All active instances of the event are ended.  If
    * the event does not exist, it is created.
    * 
    * @param   code  the event identifier code
    */
   public static void stop( String code ) {
      Map<String,String> eventMap = getEventMap( code );
      if (eventMap == null)
         return;

      // do nothing if event is not already started
      if ("off".equals( eventMap.get( "state" ) ))
         return;

      RDSUtil.trace( "stop event: [%s]", code );

      // stop the event
      if ("on".equals( eventMap.get( "state" ) ))
         db.execute(
               "UPDATE events SET " +
               "state = 'off' " +
               "WHERE code = '%s'",
               code );

      // stop active event(s) and log the duration
      db.execute(
            "UPDATE eventLog SET " +
            "state = 'off', " +
            "duration = UNIX_TIMESTAMP( NOW() ) - UNIX_TIMESTAMP( start ) " +
            "WHERE code = '%s' " +
            "AND state = 'on'",
            code );

      notify( code, "stop" );
   }

   /** Stops an event with a formatted event code. */
   public static void stop( String format, Object... args ) {
      stop( String.format( format, args ) );
   }

   /**
    * Posts an event that occurs at a single instant.  Any currently active
    * instances of the event are stopped.
    * 
    * @param   code  the event identifier code
    */
   public static void instant( String code ) {
      Map<String,String> eventMap = getEventMap( code );
      if (eventMap == null)
         return;

      RDSUtil.trace( "instant event: [%s]", code );

      // post the event
      if ("on".equals( eventMap.get( "state" ) ))
         db.execute(
               "UPDATE events SET " +
               "state = 'off' " +
               "WHERE code = '%s'",
               code );
      else
         db.execute(
               "UPDATE events SET " +
               "state = 'off'," +
               "start = NOW() " +
               "WHERE code = '%s'",
               code );

      // log the instant event
      db.execute(
            "INSERT eventLog " +
            "(code, state, start, duration) " +
            "VALUES ('%s', 'off', NOW(), 0)",
            code );

      // stop any other active event(s) and log the duration
      db.execute(
            "UPDATE eventLog SET " +
            "state = 'off', " +
            "duration = UNIX_TIMESTAMP( NOW() ) - UNIX_TIMESTAMP( start ) " +
            "WHERE code = '%s' " +
            "AND state = 'on'",
            code );

      notify( code, "instant" );
   }

   /** Posts an instant event with a formatted event code. */
   public static void instant( String format, Object... args ) {
      instant( String.format( format, args ) );
   }

   /**
    * Gets the parameters of the specified event.  If the event code is not
    * already defined, it is created (with a description equal to its code).
    * 
    * @param   code  the event identifier code
    * @return  a map containing the event parameters
    */
   private static Map<String,String> getEventMap( String code ) {
      if (db == null) {
         RDSUtil.alert( "no database specified for system events" );
         return null;
      }

      Map<String,String> eventMap = db.getRecordMap(
            "SELECT * FROM events " +
            "WHERE code = '%s'",
            code );
      if (eventMap == null)
         return null;
      if (eventMap.isEmpty()) {
         db.execute(
               "REPLACE events (code, description) " +
               "VALUES ('%s', '%s')",
               code, code );
         eventMap.put( "code", code );
         eventMap.put( "description", code );
         eventMap.put( "state", "off" );
      }

      return eventMap;
   }

   private static void notify( String code, String cmd ) {
      // do nothing if the events table doesn't have the notify column
      if ( db.getValue( "SELECT * FROM information_schema.COLUMNS "
                      + "WHERE TABLE_SCHEMA = 'rds' "
                      + "AND TABLE_NAME = 'events' "
                      + "AND COLUMN_NAME = 'notify'", "" ).isEmpty() ) {
         return;
      }

      // do nothing if the event is a notification failure
      if ( code.equals( "notifier" ) )
         return;

      String notify = db.getValue( "SELECT notify FROM events WHERE code='" + code + "'", "" );
      if ( !notify.isEmpty() ) {
         String description = db.getValue( "SELECT description FROM events WHERE code='" + code + "'", "" );
         if ( description.isEmpty() )
            description = code;
         String hostAlias = db.getControl( "notify", "hostAlias", "RDS system" );
         String message = "";
         if ( cmd.equals( "start" ) )
            message = String.format( "[%s]\nevent start:\n[%s]", hostAlias, description );
         if ( cmd.equals( "stop" ) )
            message = String.format( "[%s]\nevent stop:\n[%s]", hostAlias, description );
         if ( cmd.equals( "instant" ) )
            message = String.format( "[%s]\nevent:\n[%s]", hostAlias, description );
         db.execute( "INSERT INTO notifications SET "
                   + "recipient='%s', "
                   + "message='%s'",
                     notify, message );
      }
   }


}  // end RDSEvent class
