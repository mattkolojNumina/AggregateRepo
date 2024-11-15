package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
PKOCRE
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  PKOCRE()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("pkocre") ;
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
                      +"FROM geekPickOrder "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekPickOrder "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // owner code
    String owner_code = "" ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekPickOrder "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("pkocre: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build order_list 
    JSONArray order_list = new JSONArray() ; 
    int max_orders = 200 ;
    int order_amount = 0 ;
    String[] seq_codes = new String[max_orders] ;

    List<Map<String,String>> orders
      = db.getResultMapList("SELECT *,"
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),creation_date) AS u_creation_date, "
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),expected_finish_date) AS u_expected_finish_date, "
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),pay_time) AS u_pay_time "
                           +"UNIX_TIMESTAMP(creation_date) AS u_creation_date, "
                           +"UNIX_TIMESTAMP(expected_finish_date) AS u_expected_finish_date, "
                           +"UNIX_TIMESTAMP(pay_time) AS u_pay_time "
                           +"FROM geekPickOrder "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_orders) ; 

    for(Map<String,String> order : orders) 
      {
      seq_codes[order_amount] = order.get("seq") ;

      JSONObject o = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = order.get("owner_code") ;

      // required elements
      o.put("warehouse_code",warehouse_code) ;
      o.put("out_order_code",order.get("out_order_code")) ;
      o.put("owner_code",order.get("owner_code")) ;
      o.put("order_type",Integer.parseInt(order.get("order_type"))) ;
      o.put("creation_date",1000*Long.parseLong(order.get("u_creation_date"))) ;

      // optional string elements
      String[] options 
        =  {"out_wave_code","orig_note","waybill_code","print_content",
            "carrier_code","carrier_name","carrier_line_code",
            "carrier_line_name","consignor_province","consignor_city",
            "consignor_district","consignor_address","consignor_zip_code",
            "consignor","consignor_phone","consignor_tel",
            "consignment_company","consignor_remark",
            "consingee_province","consignee_city","consignee_district",
            "consignee_address","consignee_zip_code","consignee_code",
            "consignee","consignee_phone","consignee_tel",
            "consignee_remark","orig_platform_code","orig_platform_name",
            "shop_code","shop_name","package_type_code",
            "package_type_name","follow_operation","real_name",
            "identity_card" } ;
      for(String option : options)
        if(order.get(option)!=null) 
          o.put(option,order.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"picking_type","is_waiting","is_allow_lack","is_auto_push",
            "can_merge","designated_container_type","is_allow_split",
            "priority","inf_function","print_type","carrier_type",
            "is_need_waybill","pay_method","is_reverse",
            "express_type"} ;
      for(String option : i_options)
        if(order.get(option)!=null) {
            o.put(option,Integer.parseInt(order.get(option))) ;
        }

      // optional decimal elements
      String[] f_options
        = {"payment_fee","total_fee","sku_fee","discount_fee",
           "receivable_fee"} ;
      for(String option : f_options)
        if(order.get(option)!=null)
          o.put(option,Double.parseDouble(order.get(option))) ;

      // optional date elements
      String[] d_options
        = {"expected_finish_date","pay_time"} ;
      for(String option : d_options)
        if(order.get("u_"+option)!=null)
          o.put(option,1000*Long.parseLong(order.get("u_"+option))) ;

      // sku_list
      int s_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekPickOrderSku "
                         +"WHERE parent='"+order.get("seq")+"' ",0) ;
      if(s_count>0)
        {
        JSONArray sku_list = new JSONArray() ;

        List<Map<String,String>> skus 
          = db.getResultMapList(
              "SELECT *, "
             //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),production_date) As u_production_date, " 
             //+"TIMESTAMPDIFF(UNIX_TIMESTAMP(0),expiration_date) AS u_expiration_date "
             +"UNIX_TIMESTAMP(production_date) As u_production_date, " 
             +"UNIX_TIMESTAMP(expiration_date) AS u_expiration_date "
             +"FROM geekPickOrderSku "
             +"WHERE parent=%s ",order.get("seq")) ;
        for(Map<String,String>sku : skus)
          {
          JSONObject s = new JSONObject() ;

          s.put("sku_code",sku.get("sku_code")) ; 
          s.put("owner_code",sku.get("owner_code")) ;
          s.put("amount",Integer.parseInt(sku.get("amount"))) ;
          s.put("sku_level",Integer.parseInt(sku.get("sku_level"))) ;

          // optional string elements
          String[] sku_options 
            =  { "sku_id","bar_code","out_batch_code","packing_spec" } ;
          for(String option : sku_options)
            if(sku.get(option)!=null) 
              s.put(option,sku.get(option)) ;

          // optional int elements
          String[] ski_options 
            =  {"item","third_packing_count",
                "second_packing_count","mini_packing_count"} ;
          for(String option : ski_options)
            if(sku.get(option)!=null) 
              s.put(option,Integer.parseInt(sku.get(option))) ;

          // optional float elements
          String[] skf_options
            = {"actual_price","discount_fee"} ;
          for(String option : skf_options)
            if(sku.get(option)!=null) 
              s.put(option,Double.parseDouble(sku.get(option))) ;
          
          // optional date elements
          String[] skd_options
            = {"production_date","expiration_date"} ;
          for(String option : skd_options)
            if(sku.get("u_"+option)!=null)
              s.put(option,1000*Long.parseLong(sku.get("u_"+option))) ;

          sku_list.put(s) ;
          }
        
        o.put("sku_list",sku_list) ;  
        }

      order_list.put(o) ;

      order_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("order_amount",order_amount) ;
    body.put("order_list",order_list) ;

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
               + "/geekplus/api/artemis/pushJson/outOrderImport"
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
        RDSLog.trace("pkocre: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("pkocre: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<order_amount ; i++)
        db.execute("UPDATE geekPickOrder "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual orders

      for(int i=0 ; i<order_amount ; i++)
        db.execute("UPDATE geekPickOrder "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
      
    }
  }
