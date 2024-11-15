/*
 * RDSTrak.java
 * 
 * (c) 2011, Numina Group, Inc.
 */

package rds;


/**
 * A utility class that provides methods for accessing and modifying
 * TRAK variables.
 */
public final class RDSTrak {
   /** The database where TRAK information is stored. */
   private static RDSDatabase db;


   // suppress default constructor to enforce non-instantiability
   private RDSTrak() {}


   /** Sets the database where TRAK information is stored. */
   public static void setDatabase( RDSDatabase db ) {
      RDSTrak.db = db;
   }

   /** Reads the value of a single TRAK variable, by register. */
   public static int read( String name, int register ) {
      int val = -1;

      if (name == null || name.isEmpty())
         return val;

      if (db == null) {
         RDSUtil.alert( "no database specified for TRAK info" );
         return val;
      }

      String strVal = db.getValue( String.format(
            "SELECT get FROM trak " +
            "WHERE name = '%s' " +
            "AND register = %d",
            name, register ), "-1" );

      try {
         val = Integer.valueOf( strVal );
      } catch (NumberFormatException ex) {}

      return val;
   }

   /** Reads the value of a single TRAK variable. */
   public static int read( String name ) {
      return read( name, -1 );
   }

   /** Writes a value to a single TRAK variable, by register. */
   public static void write( String name, int register, int value ) {
      if (name == null || name.isEmpty())
         return;

      if (db == null) {
         RDSUtil.alert( "no database specified for TRAK info" );
         return;
      }

      db.execute(
            "UPDATE trak SET " +
            "put = %d, " +
            "state = 'write' " +
            "WHERE name = '%s' " +
            "AND register = %d",
            value, name, register );
   }

   /** Writes a value to a single TRAK variable. */
   public static void write( String name, int value ) {
      write( name, -1, value );
   }

   /** Saves all current TRAK rp variables. */
   public static void save() {
      if (db == null) {
         RDSUtil.alert( "no database specified for TRAK info" );
         return;
      }

      db.execute(
            "UPDATE trak " +
            "SET state = 'save' " +
            "WHERE zone = 'rp'" );
   }

   /** Loads all TRAK rp variables with their saved values. */
   public static void load() {
      if (db == null) {
         RDSUtil.alert( "no database specified for TRAK info" );
         return;
      }

      db.execute(
            "UPDATE trak " +
            "SET state = 'standard' " +
            "WHERE zone = 'rp'" );
   }
}