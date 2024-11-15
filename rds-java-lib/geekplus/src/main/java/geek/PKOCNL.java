package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
PKOCNL
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  PKOCNL()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("pkocnl") ;
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
                      +"FROM geekPickCancel "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekPickCancel "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // owner code
    String owner_code = "" ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekPickCancel "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("pkocnl: warehouse code %s unprocessed %d",warehouse_code,total) ;

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
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),cancel_date) AS u_cancel_date "
                           +" UNIX_TIMESTAMP(cancel_date) AS u_cancel_date "
                           +"FROM geekPickCancel "
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
      o.put("owner_code",order.get("owner_code"))  ;
      o.put("cancel_date",1000*Long.parseLong(order.get("u_"+"cancel_date"))) ;
 
      // optional string elements
      String[] options 
        =  {"remark"} ;
      for(String option : options)
        if(order.get(option)!=null) 
          o.put(option,order.get(option)) ;

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
               + "/geekplus/api/artemis/pushJson/outOrderCancel"
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
        RDSLog.trace("pkocnl: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("pkocnl: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<order_amount ; i++)
        db.execute("UPDATE geekPickCancel "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual records 

      for(int i=0 ; i<order_amount ; i++)
        db.execute("UPDATE geekPickCancel "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
