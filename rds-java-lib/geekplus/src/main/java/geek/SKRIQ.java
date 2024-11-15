package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
SKRIQ
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  SKRIQ()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("skriq") ;
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
      = db.getIntValue("SELECT * "
                      +"FROM geekInventoryQuery "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // owner code
    String owner_code = "" ;

    // get query
    Map<String,String> query 
      = db.getRecordMap("SELECT * "
                       +"FROM geekInventoryQuery "
                       +"WHERE processed='no' "
                       +"LIMIT 1 ","") ;

    String parent = query.get("seq") ;
    String warehouse_code = query.get("warehouse_code") ;
    
    RDSLog.inform("skriq: seq %s warehouse_code %s unprocessed 1",
                  parent,warehouse_code) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build sku_list 
    JSONArray sku_list = new JSONArray() ; 
    int max_skus = 200 ;
    int sku_amount = 0 ;

    List<Map<String,String>> skus
      = db.getResultMapList("SELECT *,"
                           +"UNIX_TIMESTAMP(production_date) AS u_production_date, "
                           +"UNIX_TIMESTAMP(expiration_date) AS u_expiration_date "
                           +"FROM geekInventoryQuerySku "
                           +"WHERE parent=%s "
                           +"LIMIT %d ",
                           parent,max_skus) ; 

    for(Map<String,String> sku : skus) 
      {
      JSONObject s = new JSONObject() ;
   
      if(owner_code.equals(""))
        owner_code = sku.get("owner_code") ;
 
      // required elements
      s.put("owner_code",sku.get("owner_code")) ;
      s.put("sku_code",sku.get("sku_code")) ;

      // optional string elements
      String[] options 
        =  {"out_batch_code","product_batch"} ;
      for(String option : options)
        if(sku.get(option)!=null) 
          s.put(option,sku.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"sku_level"} ;
      for(String option : i_options)
        if(sku.get(option)!=null) 
          s.put(option,Integer.parseInt(sku.get(option))) ;

      // optional decimal elements
      String[] f_options
        = {} ;
      for(String option : f_options)
        if(sku.get(option)!=null)
          s.put(option,Double.parseDouble(sku.get(option))) ;

      // optional date elements
      String[] d_options
        = {"production_date","expiration_date"} ;
      for(String option : d_options)
        if(sku.get("u_"+option)!=null)
          s.put(option,1000*Long.parseLong(sku.get("u_"+option))) ;

      sku_list.put(s) ;

      sku_amount++ ;
      }

    // build expect_list 
    JSONArray expect_sku_list = new JSONArray() ; 

    String[] expects
      = {"sku_level","out_batch","production_date","expiration_date",
         "inventory_status","shelf_code","shelf_bin_code","product_batch"};
    for(String expect : expects)
      {
      if(query.get("expect_"+expect)!=null)
        {
        if(query.get("expect_"+expect).equals("1"))
          {
          JSONObject e = new JSONObject() ;
          e.put(expect,true) ;
          expect_sku_list.put(e) ;
          }
        }
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("warehouse_code",warehouse_code) ;
    body.put("sku_list",sku_list) ;
    body.put("expect_sku_list",expect_sku_list) ;

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
               + "/geekplus/api/artemis/pushJson/skuInventoryQuery"
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
        RDSLog.trace("skriq: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("skriq: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // save responses
    if(success)
      {
      JSONObject result_body = input.getJSONObject("body") ;
      String warehouse = result_body.optString("warehouse_code") ;
      if(!body.isNull("sku_list"))
        {
        JSONArray response_sku_list = result_body.getJSONArray("sku_list") ;
        for(int s=0 ; s<response_sku_list.length() ; s++)
          {
          JSONObject sku = response_sku_list.getJSONObject(s) ;

          String response_warehouse_code = sku.optString("warehouse_code") ;
          String response_owner_code     = sku.optString("owner_code") ;
          String sku_code       = sku.optString("sku_code") ;

          db.execute("INSERT INTO geekInventoryQueryResponse "
                    +"(parent,warehouse_code,owner_code,sku_code) "
                    +"VALUES "
                    +"(%s,'%s','%s','%s') ",
                     parent,response_warehouse_code,response_owner_code,sku_code) ;
          int s_seq = db.getSequence() ;

          GeekUpdate u = new GeekUpdate(db,"geekInventoryQueryResponse",sku) ;
          u.addKeyIntLiteral(s_seq,"seq") ;
          u.addString("sku_id") ;
          u.addInt("sku_level") ;
          u.addLongDate("production_date") ;
          u.addLongDate("expiration_date") ;
          u.addString("out_batch_code") ;
          u.addInt("inventory_status") ;
          u.addString("shelf_code") ;
          u.addString("shelf_bin_code") ;
          u.addInt("amount") ;
          u.addString("product_batch") ;
          u.addLongDate("audit_date") ;
          u.execute() ;
          }
        }
      }

    // mark changes as complete
    if(success)
      {
      db.execute("UPDATE geekInventoryQuery "
                +"SET processed='yes' "
                +"WHERE seq=" + parent + " ") ;
      }
    else
      {
      db.execute("UPDATE geekInventoryQuery "
                +"SET processed='err' "
                +"WHERE seq=" + parent + " ") ;
      }
      
    }
  }
