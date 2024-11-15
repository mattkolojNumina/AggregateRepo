/*
 * PclPacklistApp.java
 * 
 * Generates and prints the packlist for an order, in PCL format.
 * 
 * For Sloane order-fulfillment system.
 * 
 * (c) 2024, Numina Group, Inc.
 */

package packlist;

import java.sql.*;
import java.util.*;

import org.json.JSONObject;

import dao.AbstractDAO;
import doc.PclDocument;
import doc.PclDocument.*;
import polling.AbstractPollingApp;
import rds.*;

import static rds.RDSLog.*;
import static rds.RDSUtil.stringToInt;

public class PclPacklistApp
      extends AbstractPollingApp {

   private static final String DOC_TYPE = "pcl";
   private static final String DEFAULT_ID = DOC_TYPE + "pack";

   private static final int DOC_LEFT = 30;
   private static final int DOC_TOP = 0;

   private List<Map<String,String>> docConfig;

   private int seq;
   private String orderId;
   private int cartonSeq;
   private Map<String,String> orderMap;
   private Map<String,String> lineMap;
   private PclDocument doc;
   private int fontSize;

   public PclPacklistApp( String id, String rdsDb ) {
      super( id, rdsDb );
      AbstractDAO.setDatabase(db);
      initConfig();
      initRequestData();
   }

   private void initRequestData() {
      seq = -1;
      orderId = "";
      cartonSeq = 0;
      orderMap = lineMap = null;
      doc = null;
      fontSize = -1;
   }

   private void initConfig() {
      docConfig = null;
   }

   private void populateConfig()
         throws DataException {
      if (docConfig != null)
         return;

      docConfig = getConfig();
 
      if (docConfig == null)
         throw new DataException( "invalid configuration" );
   }

   private List<Map<String,String>> getConfig() {
      return db.getResultMapList(
         "SELECT * FROM cfgDocFields " +
         "WHERE docType = '%s' " +
         "AND ordinal > 0 " +
         "ORDER BY subType, ordinal",
         id 
      );
   }

   protected void poll() {
      if (!isPollingEnabled())
         return;

      long start = System.currentTimeMillis();

      initConfig();
      int numPacklists = 0;

      while (pollPacklist())
         numPacklists++;

      long dt = System.currentTimeMillis() - start;
      if (numPacklists > 0  || dt > getMaxTime())
         inform("[%d] packlist(s) processed, took %d msec", numPacklists, dt);
   }

   private boolean isPollingEnabled() {
      return !"false".equals( db.getControl( id, "enabled", "" ) );
   }

   private boolean pollPacklist() {
      long start = System.currentTimeMillis();

      initRequestData();
      Map<String,String> requestMap = null;
      try {
         requestMap = getRequest();
      } catch (ProcessingException ex) {
         alert("proc error: %s", ex.getMessage());
      }
      if (requestMap == null || requestMap.isEmpty())
         return false;

      try {
         processRequest( requestMap );
      } catch (DataException ex) {
         alert("data error for cartonSeq [%s]: %s", cartonSeq, ex.getMessage());
         errorRequest(ex.getMessage());
      } catch (ProcessingException ex) {
         alert("proc error for cartonSeq [%s]: %s", cartonSeq, ex.getMessage());
         errorRequest(ex.getMessage());
      } catch (Exception ex) {
         alert("error during processing: %s", ex.getMessage() );
         ex.printStackTrace();
         errorRequest(ex.getMessage());
      }

      long dt = System.currentTimeMillis() - start;
      if (dt > getMaxTime())
         inform("processing took %d msec", dt);

      return true;
   }

   /*
    * Gets an unprocessed packlist request for a carton
    */
   private Map<String,String> getRequest()
         throws ProcessingException {
      Map<String,String> requestMap = AbstractDAO.getStatusMessage(DEFAULT_ID);
      if (requestMap == null)
         throw new ProcessingException("error retrieving packlist request");

      return requestMap;
   }

   private void processRequest( Map<String,String> requestMap )
         throws DataException, ProcessingException {
      startProcessing( requestMap );

      trace( "generate %s packlist for cartonSeq [%d]", DOC_TYPE, cartonSeq );
      docStart();
      docFields();
      docLines();
      docFinish();

      finishProcessing();
   }

   private void startProcessing( Map<String,String> requestMap )
         throws DataException, ProcessingException {
      if (requestMap == null || requestMap.isEmpty())
         throw new DataException( "invalid packlist request" );

      seq = getMapInt( requestMap, "seq" );
      if (seq <= 0)
         throw new DataException( "invalid request sequence" );
      String data = getMapStr( requestMap, "data" );
   	JSONObject jsonObject = new JSONObject(data);
   	cartonSeq = jsonObject.getInt("cartonSeq");
      if (cartonSeq < 1)
         throw new DataException( "invalid cartonSeq" );

      inform( "process packlist request seq [%d], type [%s], cartonSeq [%s]",
            seq, DOC_TYPE, cartonSeq );

      orderMap = db.getRecordMap(
         "SELECT trackingNumber, o.*, c.*, CONCAT(c.city,', ',c.state,' ',c.zipCode) AS cityStateZip " +
         "FROM rdsCartons " +
         "JOIN custOrders AS o USING (orderId) " + 
         "JOIN custCustomers AS c USING (customerNumber) " +
         "WHERE cartonSeq = %d", cartonSeq 
      );
      if (orderMap == null || orderMap.isEmpty())
         throw new DataException( "invalid order data" );

      orderId = getMapStr(orderMap, "orderId");

      //Provide barcode and "plaintext" version of barcode with slashes
      String trackingNumber = getMapStr(orderMap, "trackingNumber");
      if (trackingNumber == null || trackingNumber.isEmpty() || trackingNumber.length()!=18) {
         throw new DataException( "invalid tracking number" );
      }
      String plaintext = plaintextString(trackingNumber);
      orderMap.put( "trackingNumber", trackingNumber );
      orderMap.put( "plaintext", plaintext );

      orderMap.put( "pageNum", "Page 1" );

      populateConfig();
   }

   /*
    * Converts a tracking number into a "slashed" human readable plaintext version
    */
   private String plaintextString(String trackingNumber) {
      return String.format(
         "%s/%s/%s/%s", 
         trackingNumber.substring(0,7),
         trackingNumber.substring(7,10),
         trackingNumber.substring(10,14),
         trackingNumber.substring(14)
      );
   }

   private void finishProcessing() {
   	AbstractDAO.setStatusMessageDone(seq);
   }

   private void errorRequest(String error) {
   	AbstractDAO.setStatusMessageError(seq, error);
   }

   /*
    * --- packlist generation ---
    */

   private void docStart() {
      doc = new PclDocument();
      doc = doc
            .init( DOC_LEFT, DOC_TOP, PrintMode.SIMPLEX )
            .fontInit();
   }

   private void docFields()
         throws DataException {
      for (Map<String,String> m : docConfig)
         handleDocField( m );
   }

   private void handleDocField( Map<String,String> m )
         throws DataException {
      handleDocField( "", m );
   }
   
   private void handleDocField( String subType, Map<String,String> m )
         throws DataException {
      if (m == null || m.isEmpty() || subType == null)
         return;
      if (!subType.equals( m.get( "subType" ) ))
         return;

      String c = getMapStr( m, "class" );
      if (c == null || c.isEmpty())     return;
      else if ("barcode".equals( c ))   handleBarcode( m );
      else if ("box".equals( c ))       handleBox( m );
      else if ("image".equals( c ))     handleImage( m );
      else if ("position".equals( c ))  handlePosition( m );
      else if ("rule".equals( c ))      handleRule( m );
      else if ("text".equals( c ))      handleText( m );
      else if ("value".equals( c ))     handleOrderValue( m );
   }

   private void docLines()
         throws DataException, ProcessingException {
      List<Map<String,String>> lineList = db.getResultMapList(
              "WITH picks as ( " +
              "        SELECT cartonSeq, sku, uom, orderLineSeq, lineNumber, orderId, sum(qty) as pickedQty " +
              "        FROM rdsPicks p " +
              "        WHERE p.cartonSeq = %d AND p.picked=1 AND p.shortPicked=0 AND p.canceled=0 " +
              "        group by cartonSeq, sku, uom, orderLineSeq, orderId " +
              " ) " +
              " select *, s.barcode AS `upcbarcode`, SUBSTRING(s.`description`,1,22) AS `description`, " +
              " SUBSTRING(CONCAT(departmentName,'-',departmentDesc),1,22) AS buyingDept " +
              " FROM rdsCartons as c " +
              " JOIN picks as p on c.cartonSeq = p.cartonSeq " +
              " JOIN custOrderLines AS l ON l.orderLineSeq = p.orderLineSeq " +
              " JOIN custSkus AS s ON (s.sku=l.sku AND s.uom=l.uom) " +
              " JOIN custBuyingDepartments AS d ON (s.buyingDepartment=d.departmentName) " +
              " WHERE c.cartonSeq = %d " +
              " ORDER BY p.lineNumber",
         cartonSeq, cartonSeq
      );
      if (lineList == null || lineList.isEmpty()) {
         alert( "no order lines found for order" );
         return;
      }

      int pageNum = 1;
      int numLines = 1;
      int maxLines = getIntParam( "maxLines", -1 );

      for (int i = 0, n = lineList.size(); i < n; i++) {
         // start new page, if necessary
         if (maxLines > 0 && numLines > maxLines) {
            pageNum++;
            orderMap.put( "pageNum", "Page " + pageNum );

            inform( "   number of lines (%d) exceeds maximum (%d), " +
                  "start page %d", numLines, maxLines, pageNum );

            doc = doc.newPage();
            for (Map<String,String> m : docConfig)
               handleDocField( "page2", m );

            numLines = 1;
            maxLines = getIntParam( "maxLines2", -1 );
         }

         lineMap = lineList.get( i );
         String subType = "lineItem";

         for (Map<String,String> m : docConfig)
            handleLineField( subType, m );
         numLines++;
      }
   }

   private void handleLineField( String subType, Map<String,String> m )
         throws DataException {
      if (m == null || m.isEmpty() || subType == null)
         return;
      if (!subType.equals( m.get( "subType" ) ))
         return;

      String c = getMapStr( m, "class" );
      if (c == null || c.isEmpty())     return;
      else if ("value".equals( c ))     handleItemValue( m );
      else if ("valueInt".equals( c ))  handleItemValueInt( m );
      else                              handleDocField( subType, m );
   }

   private void docFinish()
         throws ProcessingException {
      doc = doc.done();

      String sql =
         "INSERT INTO rdsDocuments SET " +
         "docType = 'packlist', " +
         "refType = ?, " +
         "refValue = ?, " +
         "printDocFormat = 'pcl', " +
         "printDoc = ?"; //TO_BASE64(?)
      PreparedStatement pstmt = null;
      int rowCount = 0;
      try {
         pstmt = db.connect().prepareStatement(sql);
         pstmt.setString(1, "cartonSeq");
         pstmt.setInt(2, cartonSeq);
         pstmt.setBytes(3, doc.bytes());
         rowCount = pstmt.executeUpdate();
      } catch (SQLException ex) {
         alert( ex );
      } finally {
         RDSDatabase.closeQuietly( pstmt );
      }
      if (rowCount <= 0) {
         throw new ProcessingException( "unable to store document, rowCount <=0" );
      }
      int docSeq = db.getSequence();
      if (docSeq <= 0) {
         throw new ProcessingException( "unable to store document, docSeq <=0" );
      }

      inform("packlist stored for cartonSeq [%d] with document sequence [%d]", cartonSeq, docSeq );
   }

   // doc methods

   private void handleBarcode( Map<String,String> m )
         throws DataException {
      String name = m.get( "name" );
      if (name == null || name.isEmpty())
         return;
      String value = orderMap.get( name );
      if (value == null || value.isEmpty())
         return;

      handleMoveTo( m );

      int dy = getMapInt( m, "dy" );
      int t = getMapInt( m, "size" );
      if (dy < 0 || t < 0)
         throw new DataException( String.format(
               "invalid barcode values: dy = %d, t = %d", dy, t ) );
      // doc = doc.barcode( dy, t, "%s", value ); //SR
   }

   private void handleBox( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );

      int dx = getMapInt( m, "dx" );
      int dy = getMapInt( m, "dy" );
      int t = getMapInt( m, "size" );
      if (dx < 0 || dy < 0 || t < 0)
         throw new DataException( String.format(
               "invalid box values: dx = %d, dy = %d, t = %d", dx, dy, t ) );

      doc = doc.box( dx, dy, t );
   }

   private void handleFont( Map<String,String> m ) {
      String font = m.get( "font" );
      if ("fixed".equals( font ))
         doc = doc.font( Spacing.FIXED, Typeface.DEFAULT_FIXED );
      else if (font != null)
         doc = doc.font( Spacing.PROPORTIONAL, Typeface.DEFAULT );

      String style = m.get( "style" );
      if (style != null)
         doc = doc.fontStyle( Style.valueOf( style ) );

      String stroke = m.get( "stroke" );
      if (stroke != null)
         doc = doc.fontStroke( Stroke.valueOf( stroke ) );

      int size = getMapInt( m, "size" );
      if (size > 0) {
         fontSize = size;
         if ("fixed".equals( font ))
            doc = doc.fontSize( Spacing.FIXED, size );
         else
            doc = doc.fontSize( size );
      }
   }

   private void handleImage( Map<String,String> m ) {
      handleMoveTo( m );

      String id = m.get( "name" );
      if (id == null)
         return;

      handleMove( m );
      doc = doc.pushCursor();

      Statement stmt = null;
      ResultSet res = null;
      String sql = String.format(
            "SELECT data FROM docImages " +
            "WHERE id = '%s'",
            id );
      try {
         stmt = db.connect().createStatement();
         res = db.executeTimedQuery( stmt, sql );
         byte[] bytes = null;
         if (res.next())
            bytes = res.getBytes( 1 );
         doc = doc.appendBytes( bytes );
      } catch (SQLException ex) {
         alert( ex );
      } finally {
         RDSDatabase.closeQuietly( res );
         RDSDatabase.closeQuietly( stmt );
         doc = doc.popCursor();
      }
   }

   private void handleMove( Map<String,String> m ) {
      if (m == null)
         return;
      int dx = stringToInt( m.get( "dx" ), 0 );
      int dy = stringToInt( m.get( "dy" ), 0 );

      if (dx != 0 && dy != 0)
         doc = doc.move( dx, dy );
      else if (dx != 0)
         doc = doc.moveHorizontal( dx );
      else if (dy != 0)
         doc = doc.moveVertical( dy );
   }

   private void handleMoveTo( Map<String,String> m ) {
      int x = getMapInt( m, "x" );
      int y = getMapInt( m, "y" );

      if (x >= 0 && y >= 0)
         doc = doc.moveTo( x, y );
      else if (x >= 0)
         doc = doc.moveToHorizontal( x );
      else if (y >= 0)
         doc = doc.moveToVertical( y );
   }

   private void handlePosition( Map<String,String> m ) {
      handleMoveTo( m );
      handleMove( m );
      handleFont( m );
   }

   private void handleRule( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );

      int dx = getMapInt( m, "dx" );
      int dy = getMapInt( m, "dy" );
      if (dx < 0 || dy < 0)
         throw new DataException( String.format(
               "invalid rule values: dx = %d, dy = %d", dx, dy ) );

      doc = doc.rule( dx, dy );
   }

   private void handleText( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );
      handleFont( m );

      String text = m.get( "name" );
      if (text == null)
         return;

      handleMove( m );

      String align = m.get( "align" );
      if ("center".equalsIgnoreCase( align ))
         doc = doc.textCenter( fontSize, "%s", text );
      else if ("right".equalsIgnoreCase( align ))
         doc = doc.textRight( "%s", text );

      //else if ("reverse".equalsIgnoreCase( align ))//SR
         //doc = doc.textReverse( "%s", text ); //SR

      else
         doc = doc.text( "%s", text );
   }

   private void handleOrderValue( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );

      String name = m.get( "name" );
      if (name == null || name.isEmpty())
         return;
      String value = orderMap.get( name );
      if (value == null || value.isEmpty())
         return;

      handleMove( m );
      handleFont( m );

      String align = m.get( "align" );
      if ("center".equalsIgnoreCase( align ))
         doc = doc.textCenter( fontSize, "%s", value );
      else if ("right".equalsIgnoreCase( align ))
         doc = doc.textRight( "%s", value );

      //else if ("reverse".equalsIgnoreCase( align )) //SR
         //doc = doc.textReverse( "%s", value ); //SR

      else
         doc = doc.text( "%s", value );
   }

   private void handleItemValue( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );

      String name = m.get( "name" );
      if (name == null || name.isEmpty())
         return;
      String value = lineMap.get( name );
      if (value == null || value.isEmpty())
         return;

      handleMove( m );
      handleFont( m );

      String align = m.get( "align" );
      if ("center".equalsIgnoreCase( align ))
         doc = doc.textCenter( fontSize, "%s", value );
      else if ("right".equalsIgnoreCase( align ))
         doc = doc.textRight( "%s", value );

      //else if ("reverse".equalsIgnoreCase( align )) //SR
         //doc = doc.textReverse( "%s", value ); //SR

      else
         doc = doc.text( "%s", value );
   }

   private void handleItemValueInt( Map<String,String> m )
         throws DataException {
      handleMoveTo( m );

      String name = m.get( "name" );
      if (name == null || name.isEmpty())
         return;
      String value = lineMap.get( name );
      if (value == null || value.isEmpty())
         return;
      // check if value has a decimal component.  if so, strip the decimal and everything to the right
      int periodIndex = value.indexOf(".");
      // if the period is in the first position, return as if the string were null
      if (periodIndex == 0) {
         return;
      }
      if (periodIndex > 0) {
         value = value.substring(0, periodIndex);
      }
      int valueInt = stringToInt( value, 0 );

      handleMove( m );
      handleFont( m );

      String align = m.get( "align" );
      if ("center".equalsIgnoreCase( align ))
         doc = doc.textCenter( fontSize, "%d", valueInt );
      else if ("right".equalsIgnoreCase( align ))
         doc = doc.textRight( "%d", valueInt );

      //else if ("reverse".equalsIgnoreCase( align )) //SR
         //doc = doc.textReverse( "%d", valueInt ); //SR

      else
         doc = doc.text( "%d", valueInt );
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

      PclPacklistApp app = new PclPacklistApp( id, rdsDb );

      app.run();
   }

}
