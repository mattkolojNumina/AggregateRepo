/*
 * RDSHistory.java
 * 
 * (c) 2010, Numina Group, Inc.
 */

package rds;


/**
 * A utility class that provides methods for recording the processing
 * history of individual items.
 */
public final class RDSHistory {
   /** The table name for storage of history records. */
   private static final String HIST_TABLE = "cartonLog";

   /** The database where history information is stored. */
   private static RDSDatabase db;


   // suppress default constructor to enforce non-instantiability
   private RDSHistory() {}


   /** Sets the database where history information is stored. */
   public static void setDatabase( RDSDatabase db ) {
      RDSHistory.db = db;
   }

   /**
    * Posts a history record for an item.  The specified item identifier
    * is application-specific and may represent an item sequence number,
    * carton barcode, order number, etc.  The record code identifies a
    * processing "milestone", for use by some reports in displaying elapsed
    * time between two such processing points; records which do not
    * correspond to any milestone should use the placeholder code "!"
    * (exclamation point).
    * 
    * @param   id           the item identifier string
    * @param   code         the record code
    * @param   description  the textual description of the processing event
    */
   public static void post( String id, String code, String description ) {
      if (id == null || id.isEmpty() || code == null || code.isEmpty() ||
            description == null || description.isEmpty())
         return;

      long currentTime = System.currentTimeMillis();

      if (db == null) {
         RDSUtil.alert( "no database specified for history records" );
         return;
      }

      db.execute(
            "INSERT DELAYED INTO " + HIST_TABLE + " SET " +
            "id = '" + id + "', " +
            "code = '" + code + "', " +
            "description = '" + description + "', " +
            "stamp = FROM_UNIXTIME( " + (currentTime / 1000) + " ), " +
            "msec = " + (currentTime % 1000) );
   }

   /**
    * Posts a formatted history record for an item.  The specified item
    * identifier is application-specific and may represent an item sequence
    * number, carton barcode, order number, etc.  The record code identifies
    * a processing "milestone", for use by some reports in displaying
    * elapsed time between two such processing points; records which do
    * not correspond to any milestone should use the placeholder code "!"
    * (exclamation point).
    * 
    * @param   id      the item identifier string
    * @param   code    the record code
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public static void post( String id, String code, String format,
         Object... args ) {
      post( id, code, String.format( format, args ) );
   }

}  // end RDSHistory class
