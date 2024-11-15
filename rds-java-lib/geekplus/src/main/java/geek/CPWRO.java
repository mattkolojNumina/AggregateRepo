package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
CPWRO
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  private static final String DEFAULT_WAREHOUSE_CODE = "GP";
  private static final String DEFAULT_OWNER_CODE = "GP";

  public
  CPWRO()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("cpwro") ;
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
                      +"FROM geekContainerOccupy "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekContainerOccupy "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // owner code
    String owner_code = "GP";

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekContainerOccupy "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("cpwro: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build receipt_list 
    JSONArray container_list = new JSONArray() ; 
    int max_containers = 200 ;
    int container_amount = 0 ;
    String[] seq_codes = new String[max_containers] ;

    List<Map<String,String>> containers
      = db.getResultMapList("SELECT * "
                           +"FROM geekContainerOccupy "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_containers) ; 

    for(Map<String,String> container : containers) 
      {
      seq_codes[container_amount] = container.get("seq") ;

      JSONObject r = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = container.get("owner_code") ;

      // required elements
      r.put("warehouse_code",warehouse_code) ;
      r.put("container_code",container.get("container_code")) ;
      r.put("type",Integer.parseInt(container.get("type")))  ;

      // optional string elements
      String[] options 
        =  {"remark"} ;
      for(String option : options)
        if(container.get(option)!=null) 
          r.put(option,container.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"operation_type"} ;
      for(String option : i_options)
        if(container.get(option)!=null) 
          r.put(option,Integer.parseInt(container.get(option))) ;

      container_list.put(r) ;

      container_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("container_amount",container_amount) ;
    body.put("container_list",container_list) ;

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
               + "/geekplus/api/artemis/pushJson/containerRelease"
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
        RDSLog.trace("cpwro: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("cpwro: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<container_amount ; i++)
        db.execute("UPDATE geekContainerOccupy "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual receipts

      for(int i=0 ; i<container_amount ; i++)
        db.execute("UPDATE geekContainerOccupy "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
