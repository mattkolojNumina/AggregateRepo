package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
PTOCNL
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;
  String owner_code = "" ;

  public
  PTOCNL()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("ptocnl") ;
    }

  private void
  getInfo()
    {
    user_id  = db.getControl(zone,"user_id" ,"numina") ;
    user_key = db.getControl(zone,"user_key","12345") ; 
    owner_code = db.getControl(zone,"ownder_code","numina") ;
    hostname = db.getControl(zone,"hostname","172.17.31.130") ;
    port     = db.getControl(zone,"port"    ,"4000") ;
    }

  protected void
  cycle()
    {
    // check for changes
    int total 
      = db.getIntValue("SELECT COUNT(*) "
                      +"FROM geekPutawayCancel "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekPutawayCancel "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekPutawayCancel "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("ptocnl: warehouse code %s unprocessed %d",warehouse_code,total) ;

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
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),cancel_date) AS u_cancel_date "
                           +" UNIX_TIMESTAMP(cancel_date) AS u_cancel_date "
                           +"FROM geekPutawayCancel "
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
      r.put("owner_code",receipt.get("owner_code"))  ;
      r.put("cancel_date",1000*Long.parseLong(receipt.get("u_"+"cancel_date"))) ;
 
      // optional string elements
      String[] options 
        =  {"remark"} ;
      for(String option : options)
        if(receipt.get(option)!=null) 
          r.put(option,receipt.get(option)) ;

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
               + "/geekplus/api/artemis/pushJson/receiptNoteCancel"
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
        RDSLog.trace("ptocnl: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("ptocnl: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<receipt_amount ; i++)
        db.execute("UPDATE geekPutawayCancel "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual receipts
      for(int i=0 ; i<receipt_amount ; i++)
        db.execute("UPDATE geekPutawayCancel "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
