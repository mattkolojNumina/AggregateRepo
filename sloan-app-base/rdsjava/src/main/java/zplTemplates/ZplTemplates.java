/*
 * zplTemplates.java
 * 
 * Generates and inserts records into rdsDocuments for ZPL templates.
 * 
 * For Sloane order-fulfillment system.
 * 
 * (c) 2024, Numina Group, Inc.
 */

package zplTemplates;

import java.util.*;
import java.sql.*;
import rds.*;
import static rds.RDSLog.*;
import java.text.*;

public class ZplTemplates {

   private static RDSDatabase db;

   public ZplTemplates(RDSDatabase db) {
      ZplTemplates.db = db;
   }

   // ----------------------------------------------------------- 
   // Template requests
   // -----------------------------------------------------------

   /*
    * Requests a 1x4" order label for a carton
    * @return The rdsDocumentSeq
    */
   public int requestOrderLabel(int cartonSeq) {
      String template = getTemplate("orderLabel");

      if(template==null || template.isEmpty()) {
         alert("Order label template not found");
         return -1;
      }

      Map<String, String> cartonInfo = db.getRecordMap(
         "SELECT waveSeq, c.trackingNumber, `stop` FROM rdsCartons c " +
         "JOIN custOrders o USING (orderId) " +
         "WHERE cartonSeq=%d", cartonSeq
      );

      if(cartonInfo==null || cartonInfo.isEmpty()) {
         alert("Order info for cartonSeq[%d] not found for order label", cartonSeq);
         return -1;
      }

      String wave = getMapStr(cartonInfo, "wave");
      String trackingNumber = getMapStr(cartonInfo, "trackingNumber");
      String stop = getMapStr(cartonInfo, "stop");
      String plaintext = plaintextString(trackingNumber);

      template = template.replace("{{wave}}", wave);
      template = template.replace("{{barcode}}", trackingNumber);
      template = template.replace("{{plaintext}}", plaintext);
      template = template.replace("{{stop}}", stop);

      return(uploadTemplate(template, "label", "label", "cartonSeq", cartonSeq+"", trackingNumber));
   }


   /*
    * Requests a 6x4" shipping label for a carton
    * Includes the following information for the carton:
    * Daily wave number, zone, external wave number, door number,
    * demand date, invoice number, truck number, customer address,
    * customer numbr, tracking number, tracking number barcode,
    * stop code, carton weight, carton cube size, and a carton contents list. 
    * @return The rdsDocumentSeq
    */
   public int requestShippingLabel(int cartonSeq) {
      // trace("Start of request shipping label");

      String template = getTemplate("shippingLabel");

      // trace("Template size: " + template.length());

      if(template==null || template.isEmpty()) {
         alert("Shipping label template not found");
         return -1;
      }

      Map<String, String> cartonInfo = db.getRecordMap(
         "SELECT o.dailyWaveSeq, o.waveSeq, o.door, o.demandDate, c.trackingNumber, o.truckNumber, o.stop, c.pickType, o.poId, " +
         "o.orderId, o.customerNumber, s.customerName, s.addressLine1, s.addressLine2, s.addressLine3, s.city, s.state, s.zipcode, " +
         "c.estWeight, c.estLength, c.estWidth, c.estHeight " +
         "FROM rdsCartons c " +
         "JOIN custOrders o USING (orderId) " +
         "JOIN custCustomers s USING (customerNumber) " +
         "WHERE cartonSeq=%d", cartonSeq
      );

      String calculatedWeight = db.getString("-1", "SELECT SUM(s.weight) FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku WHERE cartonSeq = " + cartonSeq + " AND canceled != 1 AND markOut != 1;");

      double totalVolume = 0.0;
      List<Map<String,String>> itemDimensions = new ArrayList<Map<String, String>>();
      itemDimensions = db.getResultMapList("SELECT s.length, s.width, s.height, s.cubicDivisorInt FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku WHERE cartonSeq = " + cartonSeq + " AND canceled != 1 AND markOut != 1;");

      for(Map<String, String> item : itemDimensions) {
         double length = getMapDbl(item, "length");
         double width = getMapDbl(item, "width");
         double height = getMapDbl(item, "height");
         int cubicDivisorInt = getMapInt(item, "cubicDivisorInt");
         if( cubicDivisorInt == 0 )
         	cubicDivisorInt = 1;

         double itemVolume = length * width * height / cubicDivisorInt;

         totalVolume += itemVolume;
      }

      Map<String,String> lineNumbers = db.getRecordMap("SELECT MIN(lineNumber) AS startLineSeq, MAX(lineNumber) AS endingLineSeq FROM rdsPicks WHERE cartonSeq = " + cartonSeq + ";");
      String startLineSeq = getMapInt(lineNumbers, "startLineSeq") + "";
      String endingLineSeq = getMapInt(lineNumbers, "endingLineSeq") + "";

      // trace("Carton Info: " + cartonInfo);

      if(cartonInfo==null || cartonInfo.isEmpty()) {
         alert("Order info for cartonSeq[%d] not found for shipping label", cartonSeq);
         trace("Order info for cartonSeq " + cartonSeq + " not found for shipping label");
         return -1;
      }

      // = getMapStr(cartonInfo, "")

      String dailyWaveNumber = getMapStr(cartonInfo, "dailyWaveSeq");
      String zone = getMapStr(cartonInfo, "pickType");
      String externalWaveNumber = getMapStr(cartonInfo, "waveSeq");
      String doorNumber = getMapStr(cartonInfo, "door");
      String demandDate = getMapStr(cartonInfo, "demandDate");
      String invoiceNumber = getMapStr(cartonInfo, "orderId");
      String truckNumber = getMapStr(cartonInfo, "truckNumber");
      String concatNumber = plaintextString(getMapStr(cartonInfo, "trackingNumber"));
      String customerNumber = getMapStr(cartonInfo, "customerNumber");
      String barcode = getMapStr(cartonInfo, "trackingNumber");
      String stopCode = getMapStr(cartonInfo, "stop");
      String poNumber = getMapStr(cartonInfo, "poId");

      //Address cleanup
      String customer = getMapStr(cartonInfo, "customerName");
      String addressLine1 = addressLineCleanup(getMapStr(cartonInfo, "addressLine1"));
      String addressLine2 = addressLineCleanup(getMapStr(cartonInfo, "addressLine2"));
      String addressLine3 = addressLineCleanup(getMapStr(cartonInfo, "addressLine3"));
      String addressLine4 = getMapStr(cartonInfo, "city");
      String addressLine5 = getMapStr(cartonInfo, "state") + " " + getMapStr(cartonInfo, "zipcode");

      //Weight and Dimensions cleanup
      String weight = calculatedWeight + " lbs";

      DecimalFormat df = new DecimalFormat("#.##");
      String dimensions = df.format(totalVolume) + " inches";

      template = template.replace("{{dailyWaveNumber}}", dailyWaveNumber);
      template = template.replace("{{zone}}", zone);
      template = template.replace("{{externalWaveNumber}}", externalWaveNumber);
      template = template.replace("{{doorNumber}}", doorNumber);
      template = template.replace("{{demandDate}}", demandDate);
      template = template.replace("{{invoiceNumber}}", invoiceNumber);
      template = template.replace("{{truckNumber}}", truckNumber);
      template = template.replace("{{customer}}", customer);
      template = template.replace("{{addressLine1}}", addressLine1);
      template = template.replace("{{addressLine2}}", addressLine2);
      template = template.replace("{{addressLine3}}", addressLine3);
      template = template.replace("{{addressLine4}}", addressLine4);
      template = template.replace("{{addressLine5}}", addressLine5);
      template = template.replace("{{poNumber}}", poNumber);
      template = template.replace("{{concatNumber}}", concatNumber);
      template = template.replace("{{customerNumber}}", customerNumber);
      template = template.replace("{{barcode}}", barcode);
      template = template.replace("{{stopCode}}", stopCode);
      template = template.replace("{{weight}}", weight);
      template = template.replace("{{dimensions}}", dimensions);
      template = template.replace("{{startLineSeq}}", startLineSeq);
      template = template.replace("{{endingLineSeq}}", endingLineSeq);

      // trace("Template size after main data: " + template.length());
      
      //Carton contents section at bottom of label
      String buyingDepartmentData = "";
      String uniqueSKUCountData = "";
      String sumOfSalesQuantityData = "";

      String orderId = getMapStr(cartonInfo, "orderId");

      List<Map<String,String>> cartonContents = new ArrayList<Map<String, String>>();
      
      cartonContents = db.getResultMapList("SELECT s.buyingDepartment, b.departmentDesc, COUNT(DISTINCT p.sku) AS uniqueSkuCount, CAST(SUM(p.qty) AS int) AS totalCount " +
      "FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku AND p.baseUom = s.uom JOIN custBuyingDepartments AS b ON s.buyingDepartment = b.departmentName " + 
      "WHERE p.cartonSeq = %d AND p.picked=1 AND p.shortPicked=0 AND p.canceled=0 GROUP BY s.buyingDepartment",cartonSeq);

      if(cartonContents==null || cartonContents.isEmpty()) {
         alert("No carton contents found for cartonSeq[%d]", cartonSeq);
         return -1;
      }

      int Column1X = 51;
      int Column2X = 305;
      int Column3X = 548;

      int RowY = 941;

      //trace("Carton contents: " + cartonContents);

      for(Map<String, String> individual : cartonContents) {
         String buyingDepartment = getMapStr(individual, "buyingDepartment");
         String departmentName = getMapStr(individual, "departmentDesc");
         String uniqueSkuCount = getMapStr(individual, "uniqueSkuCount");
         String totalCount = getMapStr(individual, "totalCount");

         //trace("Line contents: " + buyingDepartment + " " + departmentName + " " + uniqueSkuCount + " " + totalCount);

         String column1Template = "^FT" + Column1X + "," + RowY + "^A0N,22^FD" + buyingDepartment + " - " + departmentName + "^FS";
         String column2Template = "^FT" + Column2X + "," + RowY + "^A0N,22^FD" + uniqueSkuCount + "^FS";
         String column3Template = "^FT" + Column3X + "," + RowY + "^A0N,22^FD" + totalCount + "^FS";

         //trace("Template1: " + column1Template);
         //trace("Template2: " + column2Template);
         //trace("Template3: " + column3Template);

         buyingDepartmentData = buyingDepartmentData + column1Template;
         uniqueSKUCountData = uniqueSKUCountData + column2Template;
         sumOfSalesQuantityData = sumOfSalesQuantityData + column3Template;

         RowY+=30;
      }

      //trace("buyingDepartmentData: " + buyingDepartmentData);
      //trace("uniqueSKUCountData: " + uniqueSKUCountData);
      //trace("sumOfSalesQuantityData: " + sumOfSalesQuantityData);
      
      template = template.replace("{{buyingDepartmentData}}", buyingDepartmentData);
      template = template.replace("{{uniqueSKUCountData}}", uniqueSKUCountData);
      template = template.replace("{{sumOfSalesQuantityData}}", sumOfSalesQuantityData);

      // trace("Template size after contents data: " + template.length());

      return(uploadTemplate(template, "shipLabel", "shipLabel", "cartonSeq", cartonSeq+"",barcode));

      //return 1;
   }

   /*
    * Requests a 6x4" shipping label for an ecommerce carton
    * Includes the following information for the carton:
    * Daily wave number, zone, external wave number, door number,
    * demand date, truck number, customer count,
    * tracking number, invoice count, tracking number barcode,
    * carton weight, carton cube size, and a carton contents list. 
    * @return The rdsDocumentSeq
    */
   public int requestECommerceShippingLabel(int cartonSeq) {
      // trace("Start of request ecommerce shipping label");

      String template = getTemplate("ecommerceShippingLabel");

      // trace("Template size: " + template.length());

      if(template==null || template.isEmpty()) {
         alert("Ecommerce shipping label template not found");
         return -1;
      }

      Map<String, String> cartonInfo = db.getRecordMap(

         "SELECT o.dailyWaveSeq, o.waveSeq, o.door, o.demandDate, c.trackingNumber, o.truckNumber, o.orderId, c.pickType, " +
         "c.estWeight, c.estLength, c.estWidth, c.estHeight " +
         "FROM rdsCartons c " +
         "JOIN custOrders o USING (orderId) " +
         //"JOIN custShippingInfo s USING (orderId) " +
         "WHERE cartonSeq=%d", cartonSeq
      );

      String calculatedWeight = db.getString("-1", "SELECT SUM(s.weight) FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku WHERE cartonSeq = " + cartonSeq + " AND canceled != 1 AND markOut != 1;");

      double totalVolume = 0.0;
      List<Map<String,String>> itemDimensions = new ArrayList<Map<String, String>>();
      itemDimensions = db.getResultMapList("SELECT s.length, s.width, s.height, s.cubicDivisorInt FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku WHERE cartonSeq = " + cartonSeq + " AND canceled != 1 AND markOut != 1;");

      for(Map<String, String> item : itemDimensions) {
         double length = getMapDbl(item, "length");
         double width = getMapDbl(item, "width");
         double height = getMapDbl(item, "height");
         int cubicDivisorInt = getMapInt(item, "cubicDivisorInt");
         if( cubicDivisorInt == 0 )
         	cubicDivisorInt = 1;
         double itemVolume = length * width * height / cubicDivisorInt;

         totalVolume += itemVolume;
      }

      // trace("Carton Info: " + cartonInfo);

      if(cartonInfo==null || cartonInfo.isEmpty()) {
         alert("Order info for cartonSeq[%d] not found for ecomerce shipping label", cartonSeq);
         trace("Order info for cartonSeq " + cartonSeq + " not found for ecommerce shipping label");
         return -1;
      }

      // = getMapStr(cartonInfo, "")

      String dailyWaveNumber = getMapStr(cartonInfo, "dailyWaveSeq");
      String zone = getMapStr(cartonInfo, "pickType");
      String externalWaveNumber = getMapStr(cartonInfo, "waveSeq");
      String doorNumber = getMapStr(cartonInfo, "door");
      String demandDate = getMapStr(cartonInfo, "demandDate");
      String truckNumber = getMapStr(cartonInfo, "truckNumber");
      String concatNumber = plaintextString(getMapStr(cartonInfo, "trackingNumber"));
      String barcode = getMapStr(cartonInfo, "trackingNumber");

      int numInvoice = db.getInt(-1, "SELECT COUNT(DISTINCT orderId) AS invoiceCount FROM rdsPicks WHERE cartonSeq = '%s';", cartonSeq);
      int numCustomer = db.getInt(-1, "SELECT COUNT(DISTINCT o.customerNumber) as customerCount FROM rdsPicks AS p JOIN custOrders AS o ON p.orderId = o.orderId WHERE p.cartonSeq = '%s';", cartonSeq);;

      String invoiceCount = String.format("%04d", numInvoice);
      String customerCount = String.format("%05d", numCustomer);

      //Weight and Dimensions cleanup
      String weight = calculatedWeight + " lbs";

      DecimalFormat df = new DecimalFormat("#.##");
      String dimensions = df.format(totalVolume) + " inches";

      template = template.replace("{{dailyWaveNumber}}", dailyWaveNumber);
      template = template.replace("{{zone}}", zone);
      template = template.replace("{{externalWaveNumber}}", externalWaveNumber);
      template = template.replace("{{doorNumber}}", doorNumber);
      template = template.replace("{{demandDate}}", demandDate);
      template = template.replace("{{truckNumber}}", truckNumber);
      template = template.replace("{{concatNumber}}", concatNumber);
      template = template.replace("{{barcode}}", barcode);
      template = template.replace("{{invoiceCount}}", invoiceCount);
      template = template.replace("{{customerCount}}", customerCount);
      template = template.replace("{{weight}}", weight);
      template = template.replace("{{dimensions}}", dimensions);

      // trace("Template size after main data: " + template.length());
      
      //Carton contents section at bottom of label
      String buyingDepartmentData = "";
      String uniqueSKUCountData = "";
      String sumOfSalesQuantityData = "";

      List<Map<String,String>> cartonContents = new ArrayList<Map<String, String>>();
      
      cartonContents = db.getResultMapList("SELECT s.buyingDepartment, b.departmentDesc, COUNT(DISTINCT p.sku) AS uniqueSkuCount, CAST(SUM(p.qty) AS int) AS totalCount " +
      "FROM rdsPicks AS p JOIN custSkus AS s ON p.sku = s.sku AND p.baseUom = s.uom JOIN custBuyingDepartments AS b ON s.buyingDepartment = b.departmentName " + 
      "WHERE p.cartonSeq = %d AND p.picked=1 AND p.shortPicked=0 AND p.canceled=0 GROUP BY s.buyingDepartment", cartonSeq);

      if(cartonContents==null || cartonContents.isEmpty()) {
         alert("No carton contents found for cartonSeq[%d]", cartonSeq);
         return -1;
      }

      int Column1X = 51;
      int Column2X = 305;
      int Column3X = 548;

      int RowY = 896;

      //trace("Carton contents: " + cartonContents);

      for(Map<String, String> individual : cartonContents) {
         String buyingDepartment = getMapStr(individual, "buyingDepartment");
         String departmentName = getMapStr(individual, "departmentDesc");
         String uniqueSkuCount = getMapStr(individual, "uniqueSkuCount");
         String totalCount = getMapStr(individual, "totalCount");

         //trace("Line contents: " + buyingDepartment + " " + departmentName + " " + uniqueSkuCount + " " + totalCount);

         String column1Template = "^FT" + Column1X + "," + RowY + "^A0N,22^FD" + buyingDepartment + " - " + departmentName + "^FS";
         String column2Template = "^FT" + Column2X + "," + RowY + "^A0N,22^FD" + uniqueSkuCount + "^FS";
         String column3Template = "^FT" + Column3X + "," + RowY + "^A0N,22^FD" + totalCount + "^FS";

         //trace("Template1: " + column1Template);
         //trace("Template2: " + column2Template);
         //trace("Template3: " + column3Template);

         buyingDepartmentData = buyingDepartmentData + column1Template;
         uniqueSKUCountData = uniqueSKUCountData + column2Template;
         sumOfSalesQuantityData = sumOfSalesQuantityData + column3Template;

         RowY+=30;
      }

      //trace("buyingDepartmentData: " + buyingDepartmentData);
      //trace("uniqueSKUCountData: " + uniqueSKUCountData);
      //trace("sumOfSalesQuantityData: " + sumOfSalesQuantityData);
      
      template = template.replace("{{buyingDepartmentData}}", buyingDepartmentData);
      template = template.replace("{{uniqueSKUCountData}}", uniqueSKUCountData);
      template = template.replace("{{sumOfSalesQuantityData}}", sumOfSalesQuantityData);

      // trace("Template size after contents data: " + template.length());



      return(uploadTemplate(template, "shipLabel", "shipLabel", "cartonSeq", cartonSeq+"",barcode));

      //return 1;
   }

   // ----------------------------------------------------------- 
   // Helper methods
   // -----------------------------------------------------------

   /*
    * Converts a tracking number into a "slashed" human readable plaintext version
    */
   private String plaintextString(String trackingNumber) {
      return String.format(
         "%s/%s/%s / %s", 
         trackingNumber.substring(0,7),
         trackingNumber.substring(7,10),
         trackingNumber.substring(10, 14),
         trackingNumber.substring(14)
      );
   }

   /*
    * Returns a zpl template given it's name.
    */
   private String getTemplate(String name) {
      return (db.getString("",
         "SELECT template FROM cfgZPLTemplates " +
         "WHERE name='%s'", name
      ));
   }

   /*
    * Uploads a document for printing
    */
   private int uploadTemplate(String zpl, String docId, String docType, String refType, String refValue, String verification) {
      String sql =
         "INSERT INTO rdsDocuments SET " +
         "docId = ?, " + 
         "docType = ?, " +
         "refType = ?, " +
         "refValue = ?, " +
         "printDocFormat = 'zpl', " +
         "printDoc = ?, " +
         "verification = ?";
      PreparedStatement pstmt = null;
      int rowCount = 0;
      try {
         pstmt = db.connect().prepareStatement(sql);
         pstmt.setString(1, docId);
         pstmt.setString(2, docType);
         pstmt.setString(3, refType);
         pstmt.setString(4, refValue);
         pstmt.setString(5, zpl);
         pstmt.setString(6, verification);
         rowCount = pstmt.executeUpdate();
      } catch (SQLException ex) {
         alert( ex );
      } finally {
         RDSDatabase.closeQuietly(pstmt);
      }

      if (rowCount <= 0) {
         alert("Unable to store document");
         return -1;
      }

      int docSeq = db.getSequence();
      if (docSeq <= 0) {
         alert("Unable to store document");
         return -1;
      }

      inform("Template stored with document sequence [%d]", docSeq );
      return docSeq;
   }

   protected static String getMapStr(Map<String, String> m, String name) {
      if (m == null) {
         return "";
      } else {
         String v = (String)m.get(name);
         return v == null || v.equals("n/a") ? "" : v;
      }
   }

   protected static int getMapInt(Map<String, String> m, String name) {
      return m == null ? -1 : RDSUtil.stringToInt((String)m.get(name), -1);
   }

   protected static double getMapDbl(Map<String, String> m, String name) {
      return m == null ? 0.0 : RDSUtil.stringToDouble((String)m.get(name), 0.0);
   }

   private String addressLineCleanup(String addressLine) {
      //if(addressLine.equals("n/a")) return "";
      
      //else return addressLine;

      return addressLine.equals("n/a") ? "" : addressLine;
   }
}
