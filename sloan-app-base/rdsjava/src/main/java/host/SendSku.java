package host;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import org.json.*;

import rds.*;

public class SendSku
    extends ServerResource {
  RDSDatabase db;
  // HostLog log;
  HostError error;

  JSONObject input;
  JSONArray skus_list;
  JSONObject skuObj;
  String sku = "";
  String uom = "";
  String action = "";
  String status = "ok";
  String description = "";
  String resp = "";
  boolean skuError = false;
  String errorMsg = "";
  boolean isArray = false;

  JSONObject output;
  JSONArray outputArray = new JSONArray();;

  public SendSku() {
    super();
    db = new RDSDatabase("db");
    error = new HostError("sendSku");// make a control param
  }

  @Override
  public void doInit() {
  }

  @Post("json")
  public String sendSku(JsonRepresentation entity) {

    try {
      input = entity.getJsonObject();

      /* check if an array or not */
      if (input.isNull("skus")) {
        // throw new SkuException(sku, "", "error-syntax",
        // "missing field: skus");
        isArray = false;
      } else {
        isArray = true;
      }

      /* Fields validation */
      int i = 0;
      if (isArray) {
        skus_list = input.getJSONArray("skus");
        for (i = 0; i < skus_list.length(); i++) {
          skuObj = skus_list.getJSONObject(i);
          resp = fieldValidation();
        }
      } else {
        skuObj = input;
        resp = fieldValidation();
      }

      if (skuError) {
        throw new SkuException(sku, uom,
            "error-syntax",
            errorMsg);
      }

      /* Advance validation - dims, weights, etc */

      /* */

      /*
       * Business logic - if CRUD operations are allowed or not eg. picking in
       * progress etc.
       */
      /*
       * Business logic and database execution
       */

      if (isArray) {
        for (i = 0; i < skus_list.length(); i++) {
          skuObj = skus_list.getJSONObject(i);

          businessRules();
          if (!skuError) {
            databaseExection();
          }
        }
      } else {
        skuObj = input;
        businessRules();
        if (!skuError) {
          databaseExection();
        }
      }

      return resp;

    } catch (SkuException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray(e.sku, e.uom, e.status, e.description);
        return resp;
      } else {
        resp = response(e.sku, e.uom, e.status, e.description);
        return resp;
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray("", "", "error-other", e.getMessage());
        return resp;
      } else {
        resp = response("", "", "error-other", e.getMessage());
        return resp;
      }
    } finally {
      db.disconnect();
      RDSUtil.inform("db connection closed");
    }
  }

  private String fieldValidation() throws SkuException {
    try {

      // check for required fields
      String[] needs = {
          "sku",
          "uom",
          "action",
          "itemType",
          // "baseUom",
          // "qtyBaseUom",
          "barcode",
          "length",
          "width",
          "height",
          "weight",
          "conveyable",
          // "fragile",
          "shippable",
          "hazmatCode" };

      for (String need : needs) {
        if (skuObj.isNull(need)) {
          skuError = true;
          errorMsg.concat("/missing required field: [" + need + "]");
        }
      }

      // sku uom
      sku = skuObj.optString("sku");
      if (skuObj.optString("sku") == null || skuObj.optString("sku").isEmpty()) {
        throw new SkuException("", "",
            "error-syntax",
            "empty sku");
      }
      uom = skuObj.optString("uom");
      if (skuObj.optString("uom") == null || skuObj.optString("uom").isEmpty()) {
        throw new SkuException(sku, "",
            "error-syntax",
            "empty uom");
      }
      action = skuObj.optString("action");
      if (skuObj.optString("action") == null || skuObj.optString("action").isEmpty()) {
        throw new SkuException(sku, uom,
            "error-syntax",
            "empty action");
      }
      if (skuObj.optString("itemType") == null || skuObj.optString("itemType").isEmpty()) {
        throw new SkuException(sku, uom,
            "error-syntax",
            "empty itemType");
      }
      if (skuObj.optString("barcode") == null || skuObj.optString("barcode").isEmpty()) {
        throw new SkuException(sku, uom,
            "error-syntax",
            "empty barcode");
      }
      if (skuObj.getFloat("length") <= 0) {
        skuError = true;
        errorMsg += "/invalid length value: " + skuObj.get("length").toString();
      }
      if (skuObj.getFloat("width") <= 0) {
        skuError = true;
        errorMsg += "/invalid width value: " + skuObj.get("width").toString();
      }
      if (skuObj.getFloat("height") <= 0) {
        skuError = true;
        errorMsg += "/invalid height value: " + skuObj.get("height").toString();
      }
      if (skuObj.getFloat("weight") <= 0) {
        skuError = true;
        errorMsg += "/invalid weight value: " + skuObj.getString("weight");
      }
      if (!skuObj.get("conveyable").toString().equals("true")
          && !skuObj.get("conveyable").toString().equals("false")) {
        skuError = true;
        errorMsg += "/invalid conveyable value: " + skuObj.get("conveyable").toString();
      }
      if (!skuObj.get("shippable").toString().equals("true") && !skuObj.get("shippable").toString().equals("false")) {
        skuError = true;
        errorMsg += "/invalid shippable value: " + skuObj.get("shippable").toString();
      }
    } catch (SkuException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray(e.sku, e.uom, e.status, e.description);
        return resp;
      } else {
        resp = response(e.sku, e.uom, e.status, e.description);
        return resp;
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray("", "", "error-other", e.getMessage());
        return resp;
      } else {
        resp = response("", "", "error-other", e.getMessage());
        return resp;
      }
    } finally {
      db.disconnect();
      RDSUtil.inform("db connection closed");
    }
    return resp;
  }

  private void databaseExection() throws SkuException {

    try {
      sku = skuObj.optString("sku");
      uom = skuObj.optString("uom");
      action = skuObj.optString("action");

      if (action.equals("add")) {

        int exists = db.getInt(-1, "SELECT COUNT(*) FROM custSkus WHERE sku = '%s' and uom = '%s'", sku, uom);

        if (exists < 1) {
          // create sku
          db.execute("INSERT INTO custSkus (sku,uom) VALUES ('%s','%s') ",
              sku, uom);

          insertToCustSkus();

        } else {
          throw new SkuException(sku, uom,
              "record exists",
              errorMsg);
        }

      } else if (action.equals("update")) {

        int exists = db.getInt(-1, "SELECT COUNT(*) FROM custSkus WHERE sku = '%s' and uom = '%s'", sku, uom);

        if (exists == 1) {
          // replace sku
          db.execute("REPLACE INTO custSkus "
              + " (sku,uom) VALUES ('%s','%s') ",
              sku, uom);

          insertToCustSkus();

        } else {
          throw new SkuException(sku, uom,
              "record does not exists",
              errorMsg);
        }

      } else if (action.equals("delete")) {
        int exists = db.getInt(-1, "SELECT COUNT(*) FROM custSkus WHERE sku = '%s' and uom = '%s'", sku, uom);

        if (exists == 1) {
          // clear sku data
          db.execute("DELETE FROM custSkuData "
              + " WHERE sku='%s' AND uom='%s' ",
              sku, uom);

          // clear sku
          db.execute("DELETE FROM custSkus "
              + " WHERE sku='%s' AND uom='%s' ",
              sku, uom);
        } else {
          throw new SkuException(sku, uom,
              "record does not exists",
              errorMsg);
        }

      }
      // log the response

      if (isArray) {
        RDSLog.trace("status: [%s]", status);
        RDSLog.trace("description: [%s]", description);
        resp = responseArray(sku, uom, status, description);
      } else {
        resp = response(sku, uom, status, description);
      }
    } catch (SkuException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray(e.sku, e.uom, e.status, e.description);
      } else {
        resp = response(e.sku, e.uom, e.status, e.description);
      }

    }

  }

  private String businessRules() throws SkuException {
    try {
      sku = skuObj.optString("sku");
      uom = skuObj.optString("uom");
      RDSLog.trace("working on sku: [%s] uom: [%s]", sku, uom);

      int inProcessCount = db.getInt(-1, "SELECT COUNT(DISTINCT co.orderId) FROM custOrders co "
          + " JOIN rdsPicks p "
          + " ON p.orderId = co.orderId "
          + " WHERE p.sku = '%s' "
          + " AND p.uom = '%s' "
          + " AND (co.status = 'cartonized' OR co.status = 'picking' OR co.status = 'prepared')", sku, uom);

      if (inProcessCount > 0) {
        skuError = true;
        errorMsg = "picking in progress";
        status = "error-other";
        description = "picking in progress";
      } else {
        skuError = false;
        errorMsg = "";
        status = "ok";
        description = "";
      }

      // if error, then add in response array, otherwise dataExecution func will add
      // the reponse if ok
      if (skuError) {
        if (isArray) {
          RDSLog.trace("BL status: [%s]", status);
          RDSLog.trace("BL description: [%s]", description);
          resp = responseArray(sku, uom, status, description);
        } else {
          resp = response(sku, uom, status, description);
        }
      }

    }
    // catch (SkuException e) {
    // setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    // if (isArray) {
    // resp = responseArray(sku, uom, status, description);
    // return resp;
    // } else {
    // resp = response(sku, uom, status, description);
    // return resp;
    // }
    // }
    catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        RDSLog.trace("DE status: [%s]", status);
        RDSLog.trace("DE description: [%s]", description);
        resp = responseArray("", "", "error-other", e.getMessage());
        return resp;
      } else {
        resp = response("", "", "error-other", e.getMessage());
        return resp;
      }
    }
    return resp;
  }

  private void insertToCustSkus() {

    try {
      HostUpdate u1 = new HostUpdate(db, "custSkus", skuObj);
      u1.addKeyString("sku");
      u1.addKeyString("uom");
      u1.addString("baseUom");
      u1.addFloat("qtyBaseUom");
      u1.addString("barcode");
      u1.addFloat("length");
      u1.addFloat("width");
      u1.addFloat("height");
      u1.addFloat("weight");
      u1.addBoolInt("conveyable");
      u1.addBoolInt("fragile");
      u1.addBoolInt("shippable");
      u1.addString("itemType", "skuType");
      String executeMessage = u1.execute();

      if (!executeMessage.isEmpty()) {
        throw new SkuException(sku, uom,
            "error-sql",
            executeMessage);
      }

      // hazmatCode
      if (!skuObj.isNull("hazmatCode")) {
        JSONArray hazmat_list = skuObj.getJSONArray("hazmatCode");
        for (int h = 0; h < hazmat_list.length(); h++) {
          String hazmatCode = hazmat_list.getString(h);
          db.execute("REPLACE INTO custSkuData "
              + " (sku,uom,dataType,dataValue) "
              + " VALUES "
              + " ('%s','%s','hazmatCode','%s')",
              sku, uom, hazmatCode);
        }
      }

      // trigger download complete
      db.execute("UPDATE custSkus "
          + " SET downloadStamp=NOW() "
          + " WHERE sku='%s' AND uom='%s'",
          sku, uom);
      RDSUtil.inform("Sku downloaded: sku [%s], uom [%s]",
          sku, uom);
    } catch (SkuException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      if (isArray) {
        resp = responseArray(e.sku, e.uom, e.status, e.description);
      } else {
        resp = response(e.sku, e.uom, e.status, e.description);
      }

    }

  }

  private String response(String sku, String uom, String status, String description) {
    output = new JSONObject();
    output.put("sku", sku);
    output.put("uom", uom);
    output.put("status", status);
    output.put("description", description);
    // RDSLog.trace("output.toString(): " + output.toString());
    return output.toString();
  }

  // JSONObject output;
  // JSONArray outputArray = new JSONArray();;

  private String responseArray(String sku, String uom, String status, String description) {
    output = new JSONObject();
    output.put("sku", sku);
    output.put("uom", uom);
    output.put("status", status);
    output.put("description", description);

    outputArray.put(output);
    // RDSLog.trace("outputArray.toString(): " + outputArray.toString());
    return outputArray.toString();
  }

  @Get
  public String handleGet() {
    return "hello from SendSku!";
  }

  private class SkuException
      extends Exception {
    String sku;
    String uom;
    String status;
    String description;

    public SkuException(String sku, String uom, String status, String description) {
      this.sku = sku;
      this.uom = uom;
      this.status = status;
      this.description = description;
      RDSUtil.alert("Sku Exception: sku [%s], uom [%s], status [%s], description [%s]",
          sku, uom, status, description);
    }
  }

}
