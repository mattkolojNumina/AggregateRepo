/*
 * RDSCounter.java
 * 
 * (c) 2010, Numina Group, Inc.
 */

package rds;


/**
 * A utility class that provides methods for managing production counters.
 */
public final class RDSCounter {
   /** The database where counter information is stored. */
   private static RDSDatabase db;


   // suppress default constructor to enforce non-instantiability
   private RDSCounter() {}


   /** Sets the database where counter information is stored. */
   public static void setDatabase( RDSDatabase db ) {
      RDSCounter.db = db;
   }

   /**
    * Increments the counter associated with the specified code.
    * 
    * @param   code  the counter identifier code
    */
   public static void increment( String code ) {
      add( 1, code );
   }

   /**
    * Increments the counter associated with the specified code fragments;
    * the actual counter code is constructed via concatenation.  For example,
    * <blockquote>
    * <pre>increment( "system", "device", "result" )</pre>
    * </blockquote>
    * is equivalent to
    * <blockquote>
    * <pre>increment( "system/device/result" )</pre>
    * </blockquote>
    * 
    * @param   args  a list of fragments making up the counter code
    */
   public static void increment( Object... args ) {
      increment( RDSUtil.separate( "/", (Object [])args ) );
   }

   /**
    * Adds {@code number} counts to the counter associated with the
    * specified code.
    * 
    * @param   number  the number to add
    * @param   code    the counter identifier code
    */
   public static void add( int number, String code ) {
      if (code == null || code.isEmpty())
         return;

      if (db == null) {
         RDSUtil.alert( "no database specified for production counters" );
         return;
      }

      checkCounter( code );

      db.execute(
            "INSERT DELAYED INTO counts SET " +
            "code = '%s', " +
            "stamp = NOW(), " +
            "value = %d",
            code, number );
   }

   /**
    * Adds {@code number} counts to the counter associated with the
    * specified code fragments; the actual counter code is constructed
    * via concatenation.  For example,
    * <blockquote>
    * <pre>add( 2, "system", "device", "result" )</pre>
    * </blockquote>
    * is equivalent to
    * <blockquote>
    * <pre>add( 2, "system/device/result" )</pre>
    * </blockquote>
    * 
    * @param   number  the number to add
    * @param   args    a list of fragments making up the counter code
    */
   public static void add( int number, Object... args ) {
      add( number, RDSUtil.separate( "/", (Object [])args ) );
   }

   /**
    * Checks to verify that the specified counter code is already defined;
    * if not, it is created (with a description equal to its code).
    * 
    * @param   code  the counter identifier code
    */
   private static void checkCounter( String code ) {
      String val = db.getValue(
            "SELECT code FROM counters " +
            "WHERE code = '" + code + "'",
            null );
      if (val == null)
         db.execute(
               "INSERT INTO counters SET " +
               "code = '%s', " +
               "description = '%s'",
               code, code );
   }

}  // end RDSCounter class
