package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
PTOCRE
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  private static final String DEFAULT_WAREHOUSE_CODE = "SWBI";
  private static final String DEFAULT_OWNER_CODE = "SWBI";

  public
  PTOCRE()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("ptocre") ;
    }

  private void
  getInfo()
    {
    user_id  = db.getControl(zone,"user_id" ,"numina") ;
    user_key = db.getControl(zone,"user_key","12345") ; 
    hostname = db.getControl(zone,"hostname","172.17.31.130") ;
    port     = db.getControl(zone,"port"    ,"4000") ;
    }

  protected void
  cycle()
    {
    // check for changes
    int total 
      = db.getIntValue("SELECT COUNT(*) "
                      +"FROM geekPutawayOrder "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekPutawayOrder "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // owner code
    String owner_code = "SWBI";

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekPutawayOrder "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("ptocre: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build receipt_list 
    JSONArray receipt_list = new JSONArray() ; 
    int max_receipts = 200 ;
    int receipt_amount = 0 ;
    String[] seq_codes = new String[max_receipts] ;

    List<Map<String,String>> receipts
      = db.getResultMapList("SELECT *,"
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),creation_date) AS u_creation_date "
                           +"UNIX_TIMESTAMP(creation_date) AS u_creation_date "
                           +"FROM geekPutawayOrder "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_receipts) ; 

    for(Map<String,String> receipt : receipts) 
      {
      seq_codes[receipt_amount] = receipt.get("seq") ;

      JSONObject r = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = receipt.get("owner_code") ;

      // required elements
      r.put("warehouse_code",warehouse_code) ;
      r.put("receipt_code",receipt.get("receipt_code")) ;
      r.put("type",Integer.parseInt(receipt.get("type")))  ;

      // optional string elements
      String[] options 
        =  {"pallet_code","orig_note","related_receipt","supplier_code",
            "carrier_code","remark","orig_platform_code",
            "source_warehouse_code","target_warehouse_code","orig_order_code",
            "new_waybill_code"} ;
      for(String option : options)
        if(receipt.get(option)!=null) 
          r.put(option,receipt.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"sku_amount","sku_type_amount","is_original_box",
            "putaway_type"} ;
      for(String option : i_options)
        if(receipt.get(option)!=null) 
          r.put(option,Integer.parseInt(receipt.get(option))) ;

      // optional date elements
      String[] d_options
        = {"creation_date"} ;
      for(String option : d_options)
        if(receipt.get("u_"+option)!=null)
          r.put(option,1000*Long.parseLong(receipt.get("u_"+option))) ;

      // sku_list
      int s_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekPutawayOrderSku "
                         +"WHERE parent='"+receipt.get("seq")+"' ",0) ;
      if(s_count>0)
        {
        JSONArray sku_list = new JSONArray() ;

        List<Map<String,String>> skus 
          = db.getResultMapList(
              "SELECT *, "
             +"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),production_date) AS u_production_date, " 
             +"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),expiration_date) AS u_exiration_date, "
             +"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),input_date) AS u_input_date "
             +"FROM geekPutawayOrderSku "
             +"WHERE parent=%s ",receipt.get("seq")) ;
        for(Map<String,String>sku : skus)
          {
          JSONObject s = new JSONObject() ;

          s.put("sku_code",sku.get("sku_code")) ; 
          s.put("owner_code",sku.get("owner_code")) ;
          s.put("amount",Integer.parseInt(sku.get("amount"))) ;
          s.put("sku_level",Integer.parseInt(sku.get("sku_level"))) ;
          s.put("container_type",sku.get("container_type"));          

          // optional string elements
          String[] sku_options 
            =  {"container_code","sku_id","bar_code","sku_name","box_code",
                "packing_spec","sku_version","product_code",
                "consignor_province","consignor_city","consignor_district",
                "consignor_address","consignor_zip_code","consignor",
                "consignor_phone","consignor_tel",
                "out_batch_code","batch_property04"} ;
          for(String option : sku_options)
            if(sku.get(option)!=null) 
              s.put(option,sku.get(option)) ;

          // optional int elements
          String[] ski_options 
            =  {"item","receipt_mode","third_packing_count",
                "second_packing_count","mini_packing_count",
                "can_deposit_count"} ;
          for(String option : ski_options)
            if(sku.get(option)!=null) 
              s.put(option,Integer.parseInt(sku.get(option))) ;

          // optional date elements
          String[] skd_options
            = {"production_date","expiration_date","input_date"} ;
          for(String option : skd_options)
            if(sku.get("u_"+option)!=null)
              s.put(option,1000*Long.parseLong(sku.get("u_"+option))) ;

          sku_list.put(s) ;
          }
        
        r.put("sku_list",sku_list) ;  
        }

      receipt_list.put(r) ;

      receipt_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("receipt_amount",receipt_amount) ;
    body.put("receipt_list",receipt_list) ;

    // log start
    log.start() ;

    // build output
    JSONObject output = new JSONObject() ;
    output.put("header",header) ;
    output.put("body",body) ;

    // log the request
    log.request(output.toString()) ;

    // build the URL
    String url = "http://"
               + hostname 
               + ":"
               + port 
               + "/geekplus/api/artemis/pushJson/receiptNoteImport"
               + "?warehouse_code="+warehouse_code
               + "&owner_code="+owner_code ;

    // log the url
    log.url(url) ;

    // build the client resource 
    ClientResource cr = new ClientResource(url) ;

    // call the service
    JSONObject input = null ;
    try 
      { 
      JsonRepresentation outputRep = new JsonRepresentation(output) ;
      Representation result = cr.post(outputRep) ; 
      JsonRepresentation inputRep = new JsonRepresentation(result) ;
      input = inputRep.getJsonObject() ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // log the response
    log.response(input.toString()) ;

    // process the result
    boolean success = false ; 
    try
      {
      int result_code = cr.getStatus().getCode() ;
      if(result_code==200)
        {
        JSONObject result_header = input.getJSONObject("header") ;
        int msgCode = Integer.parseInt(result_header.optString("msgCode")) ;
        String message = result_header.optString("message") ;
        RDSLog.trace("ptocre: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("ptocre: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<receipt_amount ; i++)
        db.execute("UPDATE geekPutawayOrder "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual receipts

      for(int i=0 ; i<receipt_amount ; i++)
        db.execute("UPDATE geekPutawayOrder "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
