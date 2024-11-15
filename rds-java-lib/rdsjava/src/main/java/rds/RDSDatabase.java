/*
 * RDSDatabase.java
 * 
 * (c) 2005-2011, Numina Group, Inc.
 */

package rds;

import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import javax.swing.SwingWorker;

import static rds.RDSLog.*;


/**
 * A utility class for maintaining a client connection to a database.
 */
public class RDSDatabase {

   /* Default query/execute timeout (sec). */
   private static final int DEFAULT_QUERY_TIMEOUT = 0;

   /* Default max query/execute time to trigger an alert (msec). */
   private static final long DEFAULT_MAX_TIME = 1000L;

   /* Maximum time to wait while checking connection validity (sec) */
   private static final int VALID_TIMEOUT = 5;


   /*
    * --- class variables ---
    */

   // connection parameters
   private String driver;
   private String url;
   private Properties info;

   /* The database connection object. */
   private Connection conn;

   /* Most recently generated exception. */
   private Exception exception;

   /* Query/execute timeout (sec); zero indicates no timeout. */
   private int queryTimeout;

   /* Maximum query/execute time to trigger an alert (msec). */
   private long maxTime;


   /*
    * --- constructors + access methods ---
    */

   /**
    * Constructs a database object for executing sql commands.  The connection
    * parameters are set here but not used until the first query, which
    * triggers a call to {@code connect}.
    * 
    * @param   driver  the fully qualified JDBC driver name
    * @param   url     the connection URL
    * @param   info    connection properties (key/value pairs)
    */
   public RDSDatabase( String driver, String url, Properties info ) {
      /* 2024-02-26
       * Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. 
       * The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
      */
      /* revert for now - we will refactor this for the next projects */
      setDriver( driver );
      setUrl( url );

      if (info != null)
         for (Object key : info.keySet())
            setProperty( key, info.get( key ) );
      setQueryTimeout( DEFAULT_QUERY_TIMEOUT );
      setMaxTime( DEFAULT_MAX_TIME );

      disconnect();
   }

   /**
    * Constructs a database object for executing sql commands.  The connection
    * parameters are set here but not used until the first query, which
    * triggers a call to {@code connect}.
    * 
    * @param   driver    the fully qualified JDBC driver name
    * @param   url       the connection URL
    * @param   user      the username
    * @param   password  the password for the specified user
    */
   public RDSDatabase( String driver, String url, String user,
         String password ) {
      
       /* 2024-02-26
       * Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. 
       * The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
      */
      setDriver( driver );
      setUrl( url );
      setUser( user );
      setPassword( password );
      setQueryTimeout( DEFAULT_QUERY_TIMEOUT );
      setMaxTime( DEFAULT_MAX_TIME );

      disconnect();
   }

   /**
    * Constructs a database object for connection to a MySQL server with
    * default RDS connection parameters and properties.
    * 
    * @param    ip  the IP address of the MySQL database server
    */
   public RDSDatabase( String ip ) {
       /* 2024-02-26
       * Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. 
       * The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
      */
      setDriver( "com.mysql.jdbc.Driver" );

      if (ip == null || ip.isEmpty())
         ip = "localhost";
      setUrl( String.format( "jdbc:mysql://%s/rds", ip ) );
      setUser( "rds" );
      setPassword( "rds" );

      setProperty( "autoReconnect",        "true" );
      setProperty( "noDatetimeStringSync", "true" );
      setProperty( "zeroDateTimeBehavior", "convertToNull" );
      setQueryTimeout( DEFAULT_QUERY_TIMEOUT );
      setMaxTime( DEFAULT_MAX_TIME );

      disconnect();
   }

   /** Sets the driver for the database connection. */
   public void setDriver( String driver ) {
      this.driver = driver;
      disconnect();
   }

   /** Sets the URL for the database connection. */
   public void setUrl( String url ) {
      this.url = url;
      disconnect();
   }

   /** Sets the user for the database connection. */
   public void setUser( String user ) {
      setProperty( "user", user );
   }

   /** Sets the password for the database connection. */
   public void setPassword( String password ) {
      setProperty( "password", password );
   }

   /** Sets a named property for the database connection. */
   public void setProperty( Object key, Object value ) {
      if (info == null)
         info = new Properties();
      if (key != null && value != null)
         info.put( key, value );
      disconnect();
   }

   /** Sets the value of the query/execute timeout (sec). */
   public void setQueryTimeout( int timeout ) {
      this.queryTimeout = timeout;
   }

   /** Sets the value of the maximum query/execute time (msec). */
   public void setMaxTime( long maxTime ) {
      this.maxTime = maxTime;
   }

   /** Gets the most recently generated exception. */
   public Exception getException() {
      return exception;
   }

   /** Clears any recently stored exception. */
   public void clearException() {
      exception = null;
   }


   /*
    * --- connect/disconnect ---
    */

   /**
    * Obtains the current database connection, opening a new connection if
    * one does not already exist.
    * 
    * @return  a connection to this database
    * @throws  SQLException  if a database error occurs 
    */
   public synchronized Connection connect()
         throws SQLException {
      try {
         if (conn == null) {
            trace( "db connect [%s]", url );
            Class.forName( driver );
            conn = DriverManager.getConnection( url, info );
            if (conn != null)
               trace( "db connection successful" );
         } else if (conn.isClosed()) {
            trace( "db reconnect [%s]", url );
            conn = DriverManager.getConnection( url, info );
            if (conn != null)
               trace( "db connection successful" );
         }
      } catch (SQLException ex) {
         disconnect();
         alert( "database connection failed" );
         alert( ex );
         throw ex;
      } catch (Exception ex) {
         disconnect();
         alert( "database connection failed" );
         alert( ex );
      }

      return conn;
   }

   /**
    * Submits a lightweight query, for the purposes of keeping a connection
    * alive and/or detecting a closed or invalid connection.
    */
   public boolean ping() {
      return ping( "/* ping */ SELECT 1" );
   }

   /**
    * Submits the specified query but performs no other action; this method
    * is intended to be used solely for the purposes of keeping a connection
    * alive and/or detecting a closed or invalid connection and thus the
    * supplied query should be lightweight.
    */
   public boolean ping( String sql ) {
      boolean retval = false;

      Statement stmt = null;
      try {
         stmt = connect().createStatement();
         stmt.executeQuery( sql );
         retval = true;
      } catch (SQLException ex) {
         // do nothing
      } finally {
         closeQuietly( stmt );
      }

      return retval;
   }

   /**
    * Checks the status of the current database connection.
    * 
    * @return  {@code true} if the current connection is valid, {@code false}
    *          otherwise
    */
   public boolean isValid() {
      try {
         Connection c = connect();
         if (c == null)
            return false;

         try {
            if (c.isValid( VALID_TIMEOUT ))
               return true;
         } catch (AbstractMethodError err) {
            return isValidAlternate();
         }
      } catch (SQLException ex) {
         // do nothing
      }

      return false;
   }

   /**
    * Checks the status of the current database connection, using an
    * alternative method.  This is intended for use with older JDBC drivers
    * that do not implement the {@code isValid()} method.
    * 
    * @return  {@code true} if the current connection is valid, {@code false}
    *          otherwise
    */
   private boolean isValidAlternate()
         throws SQLException {
      Connection c = connect();
      if (c == null || c.isClosed())
         return false;

      return (c.getMetaData() != null);
   }

   /**
    * Closes the current database connection.
    */
   public synchronized void disconnect() {
      closeQuietly( conn );
      conn = null;
   }


   /*
    * --- SQL queries ---
    */

   /**
    * Executes an SQL query that returns a single value.  If the query
    * succeeds, the value (as a {@code String}) is returned.  If the query
    * returns no results, or if an SQL error occurs, the specified
    * alternate value is returned instead.
    * 
    * @param   sql        the SQL query to execute
    * @param   otherwise  alternate result
    * @return  the result of the query or the specified alternate if
    *          the query fails
    */
   public String getValue( String sql, String otherwise ) {
      String value = otherwise;

      if (sql == null || sql.isEmpty())
         return value;

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         if (res.next())
            value = res.getString( 1 );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return value;
   }

   /**
    * Executes a formatted SQL query that returns a single {@code String}
    * value.  If the query returns no results, or if an SQL error occurs,
    * the specified alternate value is returned instead.
    * 
    * @param   otherwise  alternate result
    * @param   format     a {@link java.util.Formatter format string}
    * @param   args       arguments referenced by the format specifiers in
    *          the format string
    * @return  the result of the query or the specified alternate if
    *          the query fails
    */
   public String getString( String otherwise, String format, Object... args ) {
      if (format == null)
         return otherwise;
      String sql = String.format( format, args );
      return getValue( sql, otherwise );
   }

   /**
    * Executes an SQL query that returns a single integer value.  If the
    * query succeeds, the value (as an {@code int}) is returned.  If the
    * query returns no results, or if an SQL error occurs, the specified
    * alternate value is returned instead.
    * 
    * @param   sql        the SQL query to execute
    * @param   otherwise  alternate result
    * @return  the result of the query or the specified alternate if
    *          the query fails
    */
   public int getIntValue( String sql, int otherwise ) {
      int value = otherwise;

      if (sql == null || sql.isEmpty())
         return value;

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         if (res.next())
            value = res.getInt( 1 );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return value;
   }

   /**
    * Executes a formatted SQL query that returns a single integer
    * value.  If the query returns no results, or if an SQL error occurs,
    * the specified alternate value is returned instead.
    * 
    * @param   otherwise  alternate result
    * @param   format     a {@link java.util.Formatter format string}
    * @param   args       arguments referenced by the format specifiers in
    *          the format string
    * @return  the result of the query or the specified alternate if
    *          the query fails
    */
   public int getInt( int otherwise, String format, Object... args ) {
      if (format == null)
         return otherwise;
      String sql = String.format( format, args );
      return getIntValue( sql, otherwise );
   }

   /**
    * Gets the auto-increment value that was most recently generated
    * during the current server session.  If no such value has been
    * generated, 0 is returned.  If a database error occurs, the method
    * returns -1.
    *  
    * @return  the most recent auto-increment sequence number
    */
   public int getSequence() {
      return getIntValue( "SELECT LAST_INSERT_ID()", -1 );
   }

   /**
    * Obtains the value of a control parameter.
    * 
    * @param   zone       the parameter zone
    * @param   name       the parameter name
    * @param   otherwise  alternate result
    * @return  the value of the control parameter or the specified alternate
    *          if the query fails
    */
   public String getControl( String zone, String name, String otherwise ) {
      String host = "localhost";
      try {
         host = InetAddress.getLocalHost().getHostName();
      } catch ( Exception ex ) {}

      return getControl( host, zone, name, otherwise );
   }

   /**
    * Obtains the value of a control parameter for the specified host.
    * 
    * @param   host       the host name
    * @param   zone       the parameter zone
    * @param   name       the parameter name
    * @param   otherwise  alternate result
    * @return  the value of the control parameter or the specified alternate
    *          if the query fails
    */
   public String getControl( String host, String zone, String name,
         String otherwise ) {
      return getString( otherwise,
            "SELECT value FROM controls " +
            "WHERE `host` = '%s' " +
            "AND zone = '%s' " +
            "AND name = '%s'",
            host, zone, name );
   }

   /**
    * Obtains a {@code Map} of control parameters.
    * 
    * @param   zone       the parameter zone
    * @return  a {@code Map} of {@code String}/{@code String} pairs
    *          representing the control parameter names and values
    */
   public Map<String,String> getControlMap( String zone ) {
      String host = "localhost";
      try {
         host = InetAddress.getLocalHost().getHostName();
      } catch ( Exception ex ) {}

      return getControlMap( host, zone );
   }

   /**
    * Obtains a {@code Map} of control parameters for the specified host.
    * 
    * @param   host       the host name
    * @param   zone       the parameter zone
    * @return  a {@code Map} of {@code String}/{@code String} pairs
    *          representing the control parameter names and values
    */
   public Map<String,String> getControlMap( String host, String zone ) {
      return getMap(
            "SELECT name, value FROM controls " +
            "WHERE `host` = '%s' " +
            "AND zone = '%s' " +
            "ORDER BY name",
            host, zone );
   }

   /**
    * Obtains the value of a runtime parameter.
    * 
    * @param   name  the parameter name
    * @return  the value of the runtime parameter, or {@code null} if
    *          the query fails
    */
   public String getRuntime( String name ) {
      String sql = String.format(
            "SELECT value FROM runtime " +
            "WHERE name = '%s'",
            name );
      return getValue( sql, null );
   }

   /**
    * Stores (or updates) the value of a runtime parameter.  If
    * the specified value is {@code null}, the runtime parameter
    * will be removed.
    * 
    * @param name   the parameter name
    * @param value  the parameter value
    */
   public void setRuntime( String name, String value ) {
      if (name == null || name.isEmpty())
         return;

      if (value == null)
         execute(
               "DELETE FROM runtime " +
               "WHERE name = '%s'",
               name );
      else
         execute(
               "REPLACE INTO runtime SET " +
               "name = '%s', " +
               "value = '%s'",
               name, value );
   }

   /**
    * Executes an SQL query that returns a list of values.  This method
    * creates and returns a {@code List} containing the first column of
    * the query results, in order.  If there are no results, an empty list
    * is returned; if the query fails due to an SQL error, the error is
    * traced and {@code null} is returned.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  a {@code List} of {@code String}s representing the result
    *          of the query
    */
   public List<String> getValueList( String format, Object... args ) {
      if (format == null)
         return null;
      String sql = String.format( format, args );

      List<String> valueList = new ArrayList<String>();

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         while (res.next())
            valueList.add( res.getString( 1 ) );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
         valueList = null;
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return valueList;
   }

   /**
    * Executes an SQL query that returns a single record.  This method
    * creates and returns a {@code Map} containing the column names and the
    * corresponding values (as {@code String}s) from the first row of the
    * query results.  If there are no results, an empty map is returned;
    * if the query fails due to an SQL error, the error is traced and
    * {@code null} is returned.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  a {@code Map} of {@code String}/{@code String} pairs
    *          representing the column names and values resulting from the
    *          query
    */
   public Map<String,String> getRecordMap( String format, Object... args ) {
      if (format == null)
         return null;
      String sql = String.format( format, args );

      Map<String,String> recordMap = new LinkedHashMap<String,String>();

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         if (res.next()) {
            ResultSetMetaData resMeta = res.getMetaData();
            for (int i = 1, n = resMeta.getColumnCount(); i <= n; i++)
               recordMap.put( resMeta.getColumnLabel( i ),
                     res.getString( i ) );
         }
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
         recordMap = null;
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return recordMap;
   }

   /**
    * Executes an SQL query that generates two columns of results.  This
    * method creates and returns a {@code Map} that uses the data in the
    * first column as keys and the data in the second column as the
    * corresponding values.  All data are treated as {@code String}s.  If
    * the query fails due to an SQL error, {@code null} is returned.
    * <p>
    * Note that {@code null} or empty keys (data from column one) are not
    * added to the map, but {@code null} or empty values (data from column
    * two) are added.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  a {@code Map} of {@code String}/{@code String} pairs
    *          representing the data in columns one and two, respectively
    */
   public Map<String,String> getMap( String format, Object... args ) {
      if (format == null)
         return null;
      String sql = String.format( format, args );

      Map<String,String> map = new LinkedHashMap<String,String>();

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         while (res.next()) {
            String mapKey = res.getString( 1 );
            if (mapKey != null && !mapKey.isEmpty())
               map.put( mapKey, res.getString( 2 ) );
         }
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
         map = null;
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return map;
   }

   /**
    * Executes an SQL query and returns a structured view of the result
    * set.  This method creates and returns a {@code List} of result rows,
    * each of which is a {@code Map} containing the column names and the
    * corresponding values (as {@code String}s).  If the query fails due
    * to an SQL error, {@code null} is returned.
    * <p>
    * Note that this is a convenient but inefficient method of obtaining
    * result sets, especially large ones.  In particular, the column headers
    * are reproduced for every row.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  a {@code List} of {@code Map}s of
    *          {@code String}/{@code String} pairs representing the column
    *          names and values resulting from the query
    */
   public List<Map<String,String>> getResultMapList(
         String format, Object... args ) {
      if (format == null)
         return null;
      String sql = String.format( format, args );

      List<Map<String,String>> resultList =
            new ArrayList<Map<String,String>>();

      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = connect().createStatement();
         res = executeTimedQuery( stmt, sql );
         ResultSetMetaData resMeta = res.getMetaData();
         while (res.next()) {
            Map<String,String> recordMap = new LinkedHashMap<String,String>();
            for (int i = 1, n = resMeta.getColumnCount(); i <= n; i++) {
               recordMap.put( resMeta.getColumnLabel( i ),
                     res.getString( i ) );
            }
            resultList.add( recordMap );
         }
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, query = [%s]", sql );
         alert( ex );
         resultList = null;
      } finally {
         closeQuietly( res );
         closeQuietly( stmt );
      }

      return resultList;
   }

   /**
    * Executes an SQL update statement (insert/update/delete).
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  the row count, or -1 if an SQL error occurs
    * @see String#format(String, Object...)
    */
   public int execute( String format, Object... args ) {
      int rowCount = 0;

      try {
         rowCount = executeUpdate( format, args );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error, sql = [%s]", String.format( format, args ) );
         alert( ex );
         rowCount = -1;
      }

      return rowCount;
   }

   /**
    * Executes an SQL update statement (insert/update/delete) in a
    * background thread.  This method creates a {@code SwingWorker}
    * via the {@link #createExecutionWorker} method, but its return
    * value (the row count) is ignored.
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @see String#format(String, Object...)
    */
   public void executeInBackground( String format, Object... args ) {
      createExecutionWorker( format, args ).execute();
   }

   /**
    * Executes a prepared statement using the specified {@code String}
    * arguments.
    * 
    * @param   pstmt  the prepared statement to execute
    * @param   args   the arguments to the statement
    * @return  the row count, or -1 if an SQL error occurs
    * @throws  IllegalArgumentException  if the prepared statement
    *          is {@code null}
    * @see PreparedStatement#executeUpdate 
    */
   public int executePreparedStatement( PreparedStatement pstmt,
         String... args ) {
      if (pstmt == null)
         throw new IllegalArgumentException( "Prepared statment is null" );

      int rowCount = 0;
      try {
         for (int i = 0; i < args.length; i++)
            pstmt.setString( i+1, args[i] );

         pstmt.setQueryTimeout( queryTimeout );

         long start = System.currentTimeMillis();
         rowCount = pstmt.executeUpdate();
         long duration = System.currentTimeMillis() - start;

         if (duration > maxTime)
            alert( "pstmt execution took %d msec", duration );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error executing prepared statement" );
         alert( ex );
         rowCount = -1;
      }

      return rowCount;
   }

   /**
    * Creates and executes a prepared statement using the specified
    * {@code String} arguments.  The statement is closed after being
    * executed.
    * 
    * @param   sql   an SQL statement that may contain one or more '?'
    *          parameter placeholders
    * @param   args  the arguments to the statement
    * @return  the row count, or -1 if an SQL error occurs
    * @see Connection#prepareStatement(String)
    * @see PreparedStatement#executeUpdate 
    */
   public int executePreparedStatement( String sql, String... args ) {
      int rowCount = 0;

      PreparedStatement pstmt = null;
      try {
         pstmt = connect().prepareStatement( sql );
         rowCount = executePreparedStatement( pstmt, args );
      } catch (SQLException ex) {
         exception = ex;
         alert( "sql error creating prepared statement, sql = [%s]", sql );
         alert( ex );
         rowCount = -1;
      } finally {
         closeQuietly( pstmt );
      }

      return rowCount;
   }

   /**
    * Executes an SQL update statement (insert/update/delete).
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  the row count
    * @throws  SQLException  if a database error occurs
    * @see String#format(String, Object...)
    */
   public int executeUpdate( String format, Object... args )
         throws SQLException {
      String sql = String.format( format, args );
      int rowCount = 0;

      Statement stmt = null;
      try {
         stmt = connect().createStatement();
         rowCount = executeTimedUpdate( stmt, sql );
      } finally {
         closeQuietly( stmt );
      }

      return rowCount;
   }

   /**
    * Gets the number of rows affected by the previous statement.
    * 
    * @return  the row count, or -1 if the previous statement returned a
    *          result set or resulted in an error
    */
   public int getRowCount() {
      return getIntValue(
            "SELECT ROW_COUNT()",
            -1 );
   }

   /**
    * Executes an SQL 'REPLACE' statement.
    * 
    * @param   table   the table name
    * @param   fields  key/value pairs of the fields to replace
    * @return  the number of rows replaced
    * @throws  SQLException  if a database error occurs 
    */
   public int replace( String table, Map<String,? extends Object> fields )
         throws SQLException {
      if (fields == null || fields.size() == 0)
         return 0;

      String replace = "REPLACE INTO " + table;

      Set<String> keyset = fields.keySet();
      Iterator<String> i = keyset.iterator();
      String list = " ( " + i.next();
      while (i.hasNext())
         list += ", " + i.next();
      list += " )";

      i = keyset.iterator();
      String values = " VALUES ( '" + fields.get( i.next() ) + "'";
      while (i.hasNext())
         values += ", '" + fields.get( i.next() ) + "'";
      values += " )";

      String sql = replace + list + values;

      Statement stmt = connect().createStatement();
      int rowCount = executeTimedUpdate( stmt, sql );
      closeQuietly( stmt );

      return rowCount;
   }

   /**
    * Executes an SQL 'INSERT' statement.
    * 
    * @param   table   the table name
    * @param   fields  key/value pairs of the fields to insert
    * @return  the number of rows inserted
    * @throws  SQLException  if a database error occurs 
    */
   public int insert( String table, Map<String,? extends Object> fields )
         throws SQLException {
      if (fields == null || fields.size() == 0)
         return 0;

      String insert = "INSERT INTO " + table;

      Set<String> keyset = fields.keySet();
      Iterator<String> i = keyset.iterator();
      String list = " ( " + i.next();
      while (i.hasNext())
         list += ", " + i.next();
      list += " )";

      i = keyset.iterator();
      String values = " VALUES ( " + convertValue( fields.get( i.next() ) );
      while (i.hasNext())
         values += ", " + convertValue( fields.get( i.next() ) );
      values += " )";

      String sql = insert + list + values;

      Statement stmt = connect().createStatement();
      int rowCount = executeTimedUpdate( stmt, sql );
      closeQuietly( stmt );

      return rowCount;
   }

   /**
    * Executes an SQL 'UPDATE' statment.  If no rows match the update keys,
    * an equivalent 'INSERT' statement is executed.
    * 
    * @param   table   the table name
    * @param   keys    list of fields to match
    * @param   fields  key/value pairs of fields to update or insert
    * @return  the number of rows updated or inserted
    * @throws  SQLException  if a database error occurs 
    */
   public int update( String table, String[] keys,
         Map<String,? extends Object> fields )
         throws SQLException {
      if (keys == null || keys.length == 0 ||
            fields == null || fields.size() == 0)
         return 0;

      String select = "SELECT " + keys[0];
      for (int n = 1; n < keys.length; n++)
         select += ", " + keys[n];

      String from = " FROM " + table;

      String where = " WHERE " + keys[0] + " = '" + fields.get( keys[0] )
            + "'";
      for (int n = 1 ; n < keys.length ; n++)
         where += " AND " + keys[n] + " = '" + fields.get( keys[n] ) + "'";

      String sql = select + from + where;

      Statement stmt = connect().createStatement();
      ResultSet res = executeTimedQuery( stmt, sql );
      if (res.next()) {
         String update = "UPDATE " + table;

         Iterator<String> i = fields.keySet().iterator();
         String fieldname = i.next();
         String set = " SET " + fieldname + " = " +
               convertValue( fields.get( fieldname ) );
         while (i.hasNext()) {
            fieldname = i.next();
            set += ", " + fieldname + " = " +
                  convertValue( fields.get( fieldname ) );
         }

         sql = update + set + where;
         int rowCount = executeTimedUpdate( stmt, sql );
         closeQuietly( stmt );
         return rowCount;
      } else {
         closeQuietly( res );
         closeQuietly( stmt );
         return insert( table, fields );
      }
   }

   /**
    * Deletes rows from a table.
    * 
    * @param   table   the table name
    * @param   fields  key/value pairs of fields to match for deletion; if
    *          {@code null} or empty, <em>all rows will be deleted</em>
    * @return  the number of rows deleted
    * @throws  SQLException  if a database error occurs 
    */
   public int remove( String table, Map<String,? extends Object> fields )
         throws SQLException {
      String delete = "DELETE FROM " + table;
      String where = "";

      if (fields != null && fields.size() > 0) {
         Iterator<String> i = fields.keySet().iterator();
         String fieldname = i.next();
         where += " WHERE " + fieldname + " = '" + fields.get( fieldname )
               + "'";
         while (i.hasNext()) {
            fieldname = i.next();
            where += " AND " + fieldname + " = '" + fields.get( fieldname )
                  + "'";
         }
      }

      String sql = delete + where;

      Statement stmt = connect().createStatement();
      int rowCount = executeTimedUpdate( stmt, sql );
      closeQuietly( stmt );

      return rowCount;
   }


   /*
    * --- utilities ---
    */

   /**
    * Executes an SQL query, posting an alert if the maximum allowed time
    * is exceeded.
    * 
    * @param   stmt  a {@code Statement} object created with this database
    * @param   sql   the SQL query to execute
    * @return  a {@code ResultSet} object that contains the data produced 
     *         by the given query; never {@code null}
    * @throws  SQLException  if a database error occurs
    */
   public ResultSet executeTimedQuery( Statement stmt, String sql )
         throws SQLException {
      stmt.setQueryTimeout( queryTimeout );

      long start = System.currentTimeMillis();
      ResultSet res = stmt.executeQuery( sql );
      long duration = System.currentTimeMillis() - start;

      if (duration > maxTime)
         alert( "query took %d msec: [%s]", duration, sql );

      return res;
   }

   /**
    * Executes an SQL update, posting an alert if the maximum allowed time
    * is exceeded.
    * 
    * @param   stmt  a {@code Statement} object created with this database
    * @param   sql   the SQL statement to execute
    * @return  the row count
    * @throws  SQLException  if a database error occurs
    */
   public int executeTimedUpdate( Statement stmt, String sql )
         throws SQLException {
      stmt.setQueryTimeout( queryTimeout );

      long start = System.currentTimeMillis();
      int rowCount = stmt.executeUpdate( sql );
      long duration = System.currentTimeMillis() - start;

      if (duration > maxTime)
         alert( "update took %d msec: [%s]", duration, sql );

      return rowCount;
   }

   /**
    * Close a {@code Connection}, checking for {@code null} and ignoring
    * any exceptions.
    * 
    * @param   conn  {@code Connection} to close
    */
   public static void closeQuietly( Connection conn ) {
      if (conn == null)
         return;

      try {
         conn.close();
      } catch (SQLException ex) {
         // do nothing
      }
   }

   /**
    * Close an SQL {@code Statement}, checking for {@code null} and ignoring
    * any exceptions.
    * 
    * @param   stmt  {@code Statement} to close
    */
   public static void closeQuietly( Statement stmt ) {
      if (stmt == null)
         return;

      try {
         stmt.close();
      } catch (SQLException ex) {
         // do nothing
      }
   }

   /**
    * Close an SQL {@code ResultSet}, checking for {@code null} and ignoring
    * any exceptions.
    * 
    * @param   res  {@code ResultSet} to close
    */
   public static void closeQuietly( ResultSet res ) {
      if (res == null)
         return;

      try {
         res.close();
      } catch (SQLException ex) {
         // do nothing
      }
   }

   /**
    * Close database resources, generating no messages in the case of an
    * error.  This method is suitable for calling from the {@code finally}
    * of a {@code try} block that creates an SQL connection, statement, or
    * result set.
    * 
    * @param   conn  {@code Connection} to close (ignored if {@code null})
    * @param   stmt  {@code Statement} to close (ignored if {@code null})
    * @param   res   {@code ResultSet} to close (ignored if {@code null})
    */
   public static void closeQuietly( Connection conn, Statement stmt,
         ResultSet res ) {
      try {
         closeQuietly( res );
      } finally {
         try {
            closeQuietly( stmt );
         } finally {
            closeQuietly( conn );
         }
      }
   }

   /**
    * Prepares a value for entry into a table as field data.  Certain
    * input strings are converted into SQL types or commands; generic
    * values are surrounded with single-quotes for interpretation as
    * strings. 
    * 
    * @param   obj  the input object
    * @return  the converted value string
    */
   public static String convertValue( Object obj ) {
      if (obj == null)
         return null;

      String text = obj.toString();

      if ("<null>".equals( text ) || "<auto>".equals( text ))
         return null;
      if ("<now>".equals( text ))
         return "NOW()";
      if ("<default>".equals( text ))
         return "DEFAULT";
      return "'" + text + "'";
   }

   /**
    * Creates a worker for executing an SQL update statement
    * in a background thread. 
    * 
    * @param   format  a {@link java.util.Formatter format string}
    * @param   args    arguments referenced by the format specifiers in
    *          the format string
    * @return  the worker
    * @see String#format(String, Object...)
    * @see SwingWorker
    */
   public SwingWorker<Integer,Void> createExecutionWorker(
         String format, Object... args ) {
      final String sql = String.format( format, args );
      return new SwingWorker<Integer,Void>() {
         protected Integer doInBackground() {
            return RDSDatabase.this.execute( sql );
         }
      };
   }
}  // end RDSDatabase class
