package geek;

import java.util.Map;
import java.util.List;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.ext.json.JsonRepresentation;
import org.json.*;

import rds.*;

public class SNSYN {
  RDSDatabase db;
  GeekLog log;
  String zone = "geekplus";
  String user_id = "";
  String user_key = "";
  String hostname = "";
  String port = "";

  public SNSYN() {
    db = new RDSDatabase("db");
    log = new GeekLog("snsyn");
  }

  private void getInfo() {
    user_id = db.getControl(zone, "user_id", "numina");
    user_key = db.getControl(zone, "user_key", "12345");
    hostname = db.getControl(zone, "hostname", "172.17.31.130");
    port = db.getControl(zone, "port", "4000");
  }

  protected void cycle() {
    // check for changes
    int total = db.getIntValue("SELECT COUNT(*) "
        + "FROM geekSequence "
        + "WHERE processed='no' ",
        0);
    if (total == 0)
      return;

    // choose warehouse code
    String warehouse_code = db.getValue("SELECT DISTINCT warehouse_code "
        + "FROM geekSequence "
        + "WHERE processed='no' "
        + "LIMIT 1 ", "");

    // owner code
    String owner_code = "";

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
        + "FROM geekSequence "
        + "WHERE processed='no' "
        + "AND warehouse_code='" + warehouse_code + "' ",
        0);

    RDSLog.inform("snsyn: warehouse code %s unprocessed %d", warehouse_code, total);

    // get user parameters
    getInfo();

    // build header
    JSONObject header = new JSONObject();
    header.put("warehouse_code", warehouse_code);
    header.put("user_id", user_id);
    header.put("user_key", user_key);

    // build sn_list
    JSONArray sn_list = new JSONArray();
    int max_sns = 1000;
    int sn_amount = 0;
    String[] sku_codes = new String[max_sns];
    String[] sn_codes = new String[max_sns];

    List<Map<String, String>> sns = db.getResultMapList("SELECT * FROM geekSequence "
        + "WHERE warehouse_code='%s' "
        + "AND processed='no' "
        + "LIMIT %d ",
        warehouse_code, max_sns);

    RDSLog.inform("sns %d", sns.size());

    for (Map<String, String> sn : sns) {
      sku_codes[sn_amount] = sn.get("sku_code");
      sn_codes[sn_amount] = sn.get("sn_code");

      RDSLog.inform("%d %s %s", sn_amount, sn.get("sku_code"), sn.get("sn_code"));

      JSONObject s = new JSONObject();

      if (owner_code.equals(""))
        owner_code = sn.get("owner_code");

      // required elements
      s.put("warehouse_code", warehouse_code);
      s.put("sku_code", sn.get("sku_code"));
      s.put("owner_code", sn.get("owner_code"));
      s.put("sn_code", sn.get("sn_code"));
      s.put("status", Integer.parseInt(sn.get("status")));

      // optional string elements
      String[] options = { "remark" };
      for (String option : options)
        if (sn.get(option) != null)
          s.put(option, sn.get(option));

      // optional int elements
      String[] i_options = {};
      for (String option : i_options)
        if (sn.get(option) != null)
          s.put(option, Integer.parseInt(sn.get(option)));

      // optional decimal elements
      String[] d_options = {};
      for (String option : d_options)
        if (sn.get(option) != null)
          s.put(option, Double.parseDouble(sn.get(option)));

      sn_list.put(s);

      sn_amount++;
    }

    // create log record
    log.start();

    // build body
    JSONObject body = new JSONObject();
    body.put("sn_amount", sn_amount);
    body.put("sn_list", sn_list);

    // build output
    JSONObject output = new JSONObject();
    output.put("header", header);
    output.put("body", body);

    // log request
    log.request(output.toString());

    // build the URL
    String url = "http://"
        + hostname
        + ":"
        + port
        + "/geekplus/api/artemis/pushJson/snImportRequest"
        + "?warehouse_code=" + warehouse_code
        + "&owner_code=" + owner_code;

    // log the url
    log.url(url);

    // build the client resource
    ClientResource cr = new ClientResource(url);

    // call the service
    JSONObject input = null;
    try {
      JsonRepresentation outputRep = new JsonRepresentation(output);
      Representation result = cr.post(outputRep);
      JsonRepresentation inputRep = new JsonRepresentation(result);
      input = inputRep.getJsonObject();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // log the response
    log.response(input.toString());

    // process the result
    boolean success = false;
    try {
      int result_code = cr.getStatus().getCode();
      if (result_code == 200) {
        JSONObject result_header = input.getJSONObject("header");
        int msgCode = Integer.parseInt(result_header.optString("msgCode"));
        String message = result_header.optString("message");
        RDSLog.trace("snsyn: msgCode %d message %s", msgCode, message);
        if (msgCode == 200)
          success = true;
      } else
        RDSLog.alert("snsyn: http call fails, code %d", result_code);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // mark changes as complete
    if (success) {
      for (int i = 0; i < sn_amount; i++)
        db.execute("UPDATE geekSequence "
            + "SET processed='yes' "
            + "WHERE warehouse_code='" + warehouse_code + "' "
            + "AND sku_code='" + sku_codes[i] + "' "
            + "AND sn_code='" + sn_codes[i] + "' ");
    } else {
      // TODO mark individual skus

      for (int i = 0; i < sn_amount; i++)
        db.execute("UPDATE geekSequence "
            + "SET processed='err' "
            + "WHERE warehouse_code='" + warehouse_code + "' "
            + "AND sku_code='" + sku_codes[i] + "' "
            + "AND sn_code='" + sn_codes[i] + "' ");
    }
  }
}
