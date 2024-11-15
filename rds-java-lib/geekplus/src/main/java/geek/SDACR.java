package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
SDACR
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String owner_code = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  SDACR()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("sdacr") ;
    }

  private void
  getInfo()
    {
    user_id  = db.getControl(zone,"user_id" ,"numina") ;
    user_key = db.getControl(zone,"user_key","12345") ; 
    owner_code = db.getControl(zone,"owner_code","numina") ;
    hostname = db.getControl(zone,"hostname","172.17.31.130") ;
    port     = db.getControl(zone,"port"    ,"4000") ;
    }

  protected void
  cycle()
    {
    // check for changes
    int total 
      = db.getIntValue("SELECT COUNT(*) "
                      +"FROM geekStockAdjust "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekStockAdjust "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekStockAdjust "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("sdacr: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build command_list 
    JSONArray adjustment_list = new JSONArray() ; 
    int max_adjustments = 200 ;
    int adjustment_amount = 0 ;
    String[] seq_codes = new String[max_adjustments] ;

    List<Map<String,String>> adjustments 
      = db.getResultMapList("SELECT *,"
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),creation_date) AS u_creation_date "
                           +"UNIX_TIMESTAMP(creation_date) AS u_creation_date "
                           +"FROM geekStockAdjust "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_adjustments) ; 

    for(Map<String,String> adjustment : adjustments) 
      {
      seq_codes[adjustment_amount] = adjustment.get("seq") ;

      JSONObject a = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = adjustment.get("owner_code") ;

      // required elements
      a.put("warehouse_code",warehouse_code) ;
      a.put("out_adjustment_code",adjustment.get("out_adjustment_code")) ; 
      a.put("stocktake_command_code",adjustment.get("stocktake_command_code")) ;
      a.put("creation_date",1000*Long.parseLong(adjustment.get("u_creation_date"))) ;

      // optional string elements
      String[] options 
        =  {"remark"} ;
      for(String option : options)
        if(adjustment.get(option)!=null) 
          a.put(option,adjustment.get(option)) ;


      // sku_list
      int s_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekStockAdjustSku "
                         +"WHERE parent='"+adjustment.get("seq")+"' ",0) ;
      if(s_count>0)
        {
        JSONArray sku_list = new JSONArray() ;

        List<Map<String,String>> skus 
          = db.getResultMapList(
              "SELECT * "
             +"FROM geekStockAdjustSku "
             +"WHERE parent=%s ",adjustment.get("seq")) ;
        for(Map<String,String>sku : skus)
          {
          JSONObject s = new JSONObject() ;

          s.put("sku_code",  sku.get("sku_code")) ;
          s.put("owner_code",sku.get("owner_code")) ;
          s.put("sku_level",Integer.parseInt(sku.get("sku_level"))) ;
          s.put("amount",Integer.parseInt(sku.get("amount"))) ;

          // optional string elements
          String[] sku_options 
            =  { "sku_id","out_batch_code",
                 "shelf_code","shelf_bin_code" } ;
          for(String option : sku_options)
            if(sku.get(option)!=null) 
              s.put(option,sku.get(option)) ;

          sku_list.put(s) ;
          }
        
        a.put("sku_list",sku_list) ;  
        }

      adjustment_list.put(a) ;

      adjustment_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("adjustment_amount",adjustment_amount) ;
    body.put("adjustment_list",adjustment_list) ;

    // start the log
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
               + "/geekplus/api/artemis/pushJson/outAdjustmentImport"
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
        RDSLog.trace("sdacr: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("sdacr: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<adjustment_amount ; i++)
        db.execute("UPDATE geekStockAdjust "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      for(int i=0 ; i<adjustment_amount ; i++)
        db.execute("UPDATE geekStockAdjust "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
