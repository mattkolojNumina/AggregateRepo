package host;

import java.util.List;

import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.Get;

import org.json.*;

import rds.*;

public class SendShipment
    extends ServerResource {
  RDSDatabase db;
  HostError error;

  public SendShipment() {
    super();
    // TO DO - this is a potential problem. This will lead to DB connectivity issue;
    // running out of DB connections
    //
    db = new RDSDatabase("db");
    error = new HostError("sendShipment");
  }

  @Override
  public void doInit() {
  }

  @Post("json")
  public String sendShipment(JsonRepresentation entity) {
    String shipmentId = "";
    String status = "ok";
    String description = "";
    int shipInfoSeq = -1;

    try {
      JSONObject input = entity.getJsonObject();

      // shipmentId
      shipmentId = input.optString("shipmentId");
      if (shipmentId == null) {
        throw new ShipmentException("",
            "error-syntax",
            "empty shipmentId");
      }

      if (!shipmentId.equals("") && isDuplicateShipment(shipmentId)) {
        throw new ShipmentException(shipmentId,
            "error-duplicate",
            "shipment is a duplicate");
      }

      // check for required fields
      String[] needs = { "shippingMethod", "shipmentType", "priority",
          // "orderDate", "shipDate",
          "shipTo",
          // "packslipRequired", "cartonContentRequired", "vas", "cartonLabelRequired",
          // "palletLabelRequired",
          // "qcRequired", "cartonization", "oneSkuPerCarton", "maxCartonWeight",
          "consolidate", "poNumber", "salesOrder", "roomNumber", "comments", "skuLabelRequired",
          "orders" };

      for (String need : needs) {
        if (input.isNull(need)) {
          throw new ShipmentException(shipmentId,
              "error-syntax",
              "missing required field: " + need);
        }
      }

      JSONObject ship_to_object = input.getJSONObject("shipTo");
      String[] s_needs = { "name", "company", "address1", "address2", "address3", "city", "state", "postalCode", };

      for (String need : s_needs) {
        if (ship_to_object.isNull(need)) {
          throw new ShipmentException(shipmentId,
              "error-syntax",
              "missing required field: " + need);
        }
      }

      JSONArray order_list = input.getJSONArray("orders");
      if (order_list.length() == 0) {
        throw new ShipmentException(shipmentId,
            "error-syntax",
            "no orders in shipment");
      }

      for (int o = 0; o < order_list.length(); o++) {
        JSONObject order = order_list.getJSONObject(o);
        String[] o_needs = { "orderId", "requirements", "lines" };
        String orderId = order.optString("orderId");
        for (String need : o_needs) {
          if (order.isNull(need)) {
            throw new ShipmentException(shipmentId,
                "error-syntax",
                "order " + o + " missing: " + need);
          }
        }

        if (orderId.equals("")) {
          throw new ShipmentException(shipmentId,
              "error-syntax",
              "order " + o + " orderId is blank");
        }

        /*
         * -ZZ- 2022/11/11 no need to check duplicate order since orderId is not unique
         * if(!checkOrder(order.optString("orderId")))
         * throw new ShipmentException(shipmentId,
         * "error-duplicate",
         * "order "+order.optString("orderId")+" "
         * +"is a duplicate") ;
         */
        JSONArray line_list = order.getJSONArray("lines");
        if (line_list.length() == 0)
          throw new ShipmentException(shipmentId,
              "error-syntax",
              "order " + order.optString("orderId") + " "
                  + "has no lines");
        for (int l = 0; l < line_list.length(); l++) {
          JSONObject line = line_list.getJSONObject(l);
          /*
           * changed due to customer limitations - HYN 2022.10.18
           * String[] l_needs = { "lineId","sku","uom","qty","location",
           * "ucc","qcRequired","bomSkus" } ;
           */
          /*
           * We need to check all the following fields eventually - ZZ 2022.11.30
           * String[] l_needs = { "lineId","sku","uom","qty","location",
           * "ucc","qcRequired","sidemarksRequired" } ;
           */
          String[] l_needs = {
              "lineId",
              "sku",
              "uom",
              // "description",
              "qty",
              "location",
              // "unitPrice",
              // "requirements"
          };

          for (String need : l_needs)
            if (line.isNull(need))
              throw new ShipmentException(shipmentId,
                  "error-syntax",
                  "order " + order.optString("orderId") + " "
                      + "line " + line.optString("lineId") + " "
                      + "missing required field: " + need);
          if (line.optString("lineId").equals(""))
            throw new ShipmentException(shipmentId,
                "error-syntax",
                "order " + order.optString("orderId") + " "
                    + "line " + (l + 1) + " lineId is blank");
          if (!checkLine(orderId, line.optString("lineId"),
              line.optString("sku"), line.optString("uom"), line.optString("location")))
            throw new ShipmentException(shipmentId,
                "error-duplicate",
                "order " + order.optString("orderId") + " "
                    + "line " + line.optString("lineId") + " "
                    + "is a duplicate");
          if (!checkSku(line.optString("sku"), line.optString("uom")))
            throw new ShipmentException(shipmentId,
                "error-sku",
                "order " + order.optString("orderId") + " "
                    + "line " + line.optString("lineId") + " "
                    + "invalid sku: "
                    + line.optString("sku") + " "
                    + line.optString("uom"));
          if (!checkLocation(line.optString("location")))
            throw new ShipmentException(shipmentId,
                "error-location",
                "order " + order.optString("orderId") + " "
                    + "line " + line.optString("lineId") + " "
                    + "invalid location: "
                    + line.optString("location"));
          if (line.getInt("qty") <= 0)
            throw new ShipmentException(shipmentId,
                "error-qty",
                "order " + order.optString("orderId") + " "
                    + "line " + line.optString("lineId") + " "
                    + "invalid quantity: " + line.getInt("qty"));

        }
      }

      // create shipment
      db.execute(" INSERT INTO custShipments "
          + " (shipmentId) VALUES ('%s') ", shipmentId);

      // update shipment
      HostUpdate u1 = new HostUpdate(db, "custShipments", input);
      u1.addKeyString("shipmentId");
      u1.addString("shipmentType");
      u1.addInt("priority");
      u1.addBoolInt("packslipRequired");
      u1.addBoolInt("cartonContentRequired");
      u1.addBoolInt("uccCartonLabelRequired");
      u1.addBoolInt("uccPalletLabelRequired");
      u1.execute();

      // shipping Info
      if (!input.isNull("shippingMethod")) {
        db.execute("INSERT INTO custShippingInfo "
            + " (shipMethod, shipFromName, shipFromCompany, shipFromAddress1, shipFromAddress2, shipFromAddress3, shipFromCity, shipFromState, shipFromPostalCode) "
            + " VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
            input.getString("shippingMethod"),
            ship_to_object.getString("name"),
            ship_to_object.getString("company"),
            ship_to_object.getString("address1"),
            ship_to_object.getString("address2"),
            ship_to_object.getString("address3"),
            ship_to_object.getString("city"),
            ship_to_object.getString("state"),
            ship_to_object.getString("postalCode"));
      } else {
        RDSLog.alert("shipping method is null");
        db.execute(" INSERT INTO custShippingInfo "
            + " SET shipMethod = NULL ");
      }

      shipInfoSeq = db.getSequence();
      db.execute(" UPDATE custShipments "
          + " SET shipInfoSeq = %d"
          + " WHERE shipmentId = '%s' ",
          shipInfoSeq, shipmentId);

      // vas
      if (!input.isNull("vas")) {
        JSONArray vas_list = input.getJSONArray("vas");
        for (int v = 0; v < vas_list.length(); v++) {
          String vas = vas_list.getString(v);
          db.execute("INSERT INTO custShipmentData "
              + " (shipmentId,dataType,dataValue) "
              + " VALUES "
              + " ('%s','vas','%s') ", shipmentId, vas);
        }
      }

      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "consolidate", input.optBoolean("consolidate"));
      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "poNumber", input.optString("poNumber"));
      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "salesOrder", input.optString("salesOrder"));
      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "roomNumber", input.optString("roomNumber"));
      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "comments", input.optString("comments"));
      db.execute("INSERT INTO custShipmentData  (shipmentId, dataType, dataValue) "
          + " VALUES ('%s','%s','%s') ", shipmentId, "skuLabelRequired", input.optBoolean("skuLabelRequired"));

      // orders
      for (int o = 0; o < order_list.length(); o++) {
        JSONObject order = order_list.getJSONObject(o);
        // String vcOrderId = order.optString("orderId");
        String orderId = order.optString("orderId");

        // create order
        db.execute("INSERT INTO custOrders "
            + " (orderId,shipmentId,shipInfoSeq,priority) "
            + " VALUES ('%s','%s',%d,%d) ",
            orderId, shipmentId, shipInfoSeq, input.optInt("priority"));

        // order lines
        JSONArray line_list = order.getJSONArray("lines");
        if (line_list.length() == 0) {
          throw new ShipmentException(shipmentId,
              "error-syntax",
              "order " + order.optString("orderId") + " "
                  + "has no lines");
        }

        for (int l = 0; l < line_list.length(); l++) {
          JSONObject line = line_list.getJSONObject(l);

          // create order line
          db.execute("INSERT INTO custOrderLines "
              + " (orderId, lineId, sku, uom, location) "
              + " VALUES "
              + " ('%s','%s','%s','%s','%s') ",
              orderId,
              line.optString("lineId"),
              line.optString("sku"),
              line.optString("uom"),
              line.optString("location"));

          int orderLineSeq = db.getSequence();

          // update order line
          HostUpdate u2 = new HostUpdate(db, "custOrderLines", line);
          u2.addKeyIntLiteral(orderLineSeq, "orderLineSeq");
          u2.addInt("qty");
          u2.addString("location");
          u2.addBoolInt("qcRequired");
          u2.execute();

        }
      }

      // trigger download complete
      // -ZZ-10/03/2022 update custShipments downloaded status
      db.execute("UPDATE custShipments "
          + "SET status='downloaded', downloadStamp=NOW() "
          + "WHERE shipmentId='" + shipmentId + "' ");
      // -ZZ-10/03/2022 update custOrders downloaded status
      db.execute("UPDATE custOrders "
          + "SET status='downloaded', downloadStamp=NOW() "
          + "WHERE shipmentId='" + shipmentId + "' ");

      // log the response
      String resp = response(shipmentId, status, description);

      return resp;
    } catch (ShipmentException e) {
      // per customer request - send validation error as status 200 - HYN 2022.10.20
      // setStatus(Status.CLIENT_ERROR_BAD_REQUEST) ;
      String resp = response(e.shipmentId, e.status, e.description);
      return resp;
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      String resp = response("", "error-other", e.getMessage());
      return resp;
    } finally {
      db.disconnect();
    }
  }

  private boolean isDuplicateShipment(String shipmentId) {
    // if exists but is canceled or error, clear and allow download
    // -ZZ- 2022/11/11 modified to allow redownload on error shipment
    int canceledOrError = db.getInt(-1, "SELECT COUNT(*) "
        + " FROM custShipments "
        + " WHERE shipmentId = '%s' "
        + " AND ( cancelStamp IS NOT NULL OR status='error' )",
        shipmentId);
    if (canceledOrError == 1) {
      clearShipment(shipmentId);
    }

    int exists = db.getInt(-1, "SELECT COUNT(*) "
        + " FROM custShipments "
        + " WHERE shipmentId='%s' ",
        shipmentId);
    return exists > 0;
  }

  private boolean clearShipment(String shipmentId) {

    List<String> order_list = db.getValueList("SELECT orderId "
        + " FROM custOrders "
        + " WHERE shipmentId = '%s' ",
        shipmentId);
    for (String orderId : order_list) {
      List<String> line_list = db.getValueList("SELECT orderLineSeq "
          + " FROM custOrderLines "
          + " WHERE orderId = '%s' ",
          orderId);
      for (String orderLineSeq : line_list) {
        db.execute(" DELETE FROm custOrderLineData "
            + " WHERE orderLineSeq = %s ",
            orderLineSeq);
        db.execute(" DELETE FROm custOrderLines "
            + " WHERE orderLineSeq = %s ",
            orderLineSeq);
      }
      db.execute(" DELETE FROM custOrderData "
          + " WHERE orderId = '%s' ",
          orderId);
      db.execute(" DELETE FROM custOrders "
          + " WHERE orderId = '%s' ",
          orderId);
    }
    int shipInfoSeq = db.getIntValue("SELECT shipInfoSeq "
        + " FROM custShipments "
        + " WHERE shipmentId = '" + shipmentId + "' ",
        0);
    db.execute(" DELETE FROM custShippingInfo "
        + " WHERE shipInfoSeq = %d ",
        shipInfoSeq);
    db.execute("DELETE FROM custShipmentData "
        + "WHERE shipmentId = '" + shipmentId + "' ");
    db.execute("DELETE FROM custShipments "
        + "WHERE shipmentId = '" + shipmentId + "' ");
    return true;
  }

  private boolean checkOrder(String orderId) {
    // if exists but is canceled, clear and allow download
    String canceled = db.getValue("SELECT orderId "
        + "FROM custOrders "
        + "WHERE orderId='" + orderId + "' "
        + "AND cancelStamp IS NOT NULL ",
        "");
    if (!canceled.equals(""))
      return clearOrder(orderId);

    String exists = db.getValue("SELECT orderId "
        + "FROM custOrders "
        + "WHERE orderId='" + orderId + "' ",
        "");
    return exists.equals("");
  }

  private boolean clearOrder(String orderId) {
    db.execute("DELETE FROM custOrderLines "
        + "WHERE orderId='" + orderId + "' ");
    db.execute("DELETE FROM custOrders "
        + "WHERE orderId='" + orderId + "' ");
    return true;
  }

  private boolean checkLine(String orderId, String lineId, String sku, String uom, String location) {
    // if exists but is canceled, clear and allow download
    // -ZZ- 2022/11/11 join custOrders since vcOrderId is only stored there
    String canceled = db.getValue("SELECT lineId "
        + "FROM custOrderLines "
        + "WHERE orderId='" + orderId + "' "
        + "AND lineId='" + lineId + "' "
        + "AND sku='" + sku + "' "
        + "AND uom='" + uom + "' "
        + "AND location='" + location + "' "
        + "AND cancelStamp IS NOT NULL ",
        "");
    if (!canceled.equals(""))
      return clearLine(orderId, lineId, sku, uom);

    String exists = db.getValue("SELECT lineId "
        + "FROM custOrderLines "
        + "WHERE orderId='" + orderId + "' "
        + "AND lineId='" + lineId + "' "
        + "AND sku='" + sku + "' "
        + "AND uom='" + uom + "' "
        + "AND location='" + location + "' ",
        "");
    return exists.equals("");
  }

  private boolean clearLine(String orderId, String lineId, String sku, String uom) {
    db.execute("DELETE FROM cusOrderLines "
        + "WHERE orderId='" + orderId + "' "
        + "AND lineId='" + lineId + "' "
        + "AND sku='" + sku + "' "
        + "AND uom='" + uom + "' ");
    return true;
  }

  private boolean checkSku(String sku) {
    String exists = db.getValue("SELECT sku FROM custSkus "
        + "WHERE sku='" + sku + "' ",
        "");
    return !exists.equals("");
  }

  private boolean checkSku(String sku, String uom) {
    String exists = db.getValue("SELECT sku FROM custSkus "
        + "WHERE sku='" + sku + "' "
        + "AND   uom='" + uom + "' ",
        "");
    return !exists.equals("");
  }

  private boolean checkLocation(String location) {
    String exists = db.getValue("SELECT location FROM rdsLocations "
        + "WHERE location='" + location + "' ",
        "");
    return !exists.equals("");
  }

  private String response(String shipmentId, String status, String description) {
    JSONObject output = new JSONObject();
    output.put("shipmentId", shipmentId);
    output.put("status", status);
    output.put("description", description);
    return output.toString();
  }

  @Get
  public String handleGet() {
    return "hello from SendShipment!";
  }

  private class ShipmentException
      extends Exception {
    String shipmentId;
    String status;
    String description;

    public ShipmentException(String shipmentId, String status, String description) {
      this.shipmentId = shipmentId;
      this.status = status;
      this.description = description;
    }
  }

}
