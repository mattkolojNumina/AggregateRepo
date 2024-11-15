package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
URSYN
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String owner_code = "" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  URSYN()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("ursyn") ;
    }

  private void
  getInfo()
    {
    owner_code = db.getControl(zone,"owner_code","numina") ;
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
                      +"FROM geekUser "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekUser "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekUser "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("ursyn: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build user_list 
    JSONArray user_list = new JSONArray() ; 
    int max_users = 200 ;
    int user_amount = 0 ;
    String[] user_names = new String[max_users] ;

    List<Map<String,String>> users
      = db.getResultMapList("SELECT * FROM geekUser "
                           +"WHERE processed='no'  "
                           +"AND warehouse_code='" + warehouse_code + "' "
                           +"LIMIT %d ",
                            max_users) ; 

    for(Map<String,String> user : users) 
      {
      user_names[user_amount] = user.get("user_name") ;

      JSONObject u = new JSONObject() ;
     
      // required elements
      u.put("user_name",user.get("user_name")) ;
      u.put("password", user.get("password")) ;
      u.put("status",Integer.parseInt(user.get("status"))) ;

      // optional string elements
      String[] options 
        =  {"real_name"} ;
      for(String option : options)
        if(user.get(option)!=null) 
          u.put(option,user.get(option)) ;

      // role list
      int r_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekUserRole "
                         +"WHERE warehouse_code='" + warehouse_code + "' "
                         +"AND user_name='"+user.get("user_name")+"' ",0) ;
      if(r_count>0)
        {
        JSONArray role_list = new JSONArray() ;

        List<Map<String,String>> roles
          = db.getResultMapList("SELECT * FROM geekUserRole "
                               +"WHERE warehouse_code='" + warehouse_code + "' "
                               +"AND user_name='%s' ",user.get("user_name")) ;
        for(Map<String,String>role : roles)
          {
          JSONObject r = new JSONObject() ;
          r.put("role_name",role.get("role_name")) ;
          role_list.put(r) ;
          }
        
        u.put("role_list",role_list) ;  
        }


      user_list.put(u) ;
 
      user_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("user_amount",user_amount) ;
    body.put("user_list",user_list) ;

    // start log
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
               + "/geekplus/api/artemis/pushJson/userCreateImportRequest"
               + "?warehouse_code="+warehouse_code
               + "&owner_code="+owner_code ;
    RDSLog.inform("url [%s]",url) ;

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

   
    RDSLog.inform("url [%s] response [%s]",url, input.toString()) ;
    
    // log the result
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
        RDSLog.trace("ursyn: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("ursyn: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<user_amount ; i++)
        db.execute("UPDATE geekUser "
                  +"SET processed='yes' "
                  +"WHERE warehouse_code='"+warehouse_code+"' "
                  +"AND user_name='" + user_names[i] + "' ") ;
      }
    else
      {
      // TODO mark individual users

      for(int i=0 ; i<user_amount ; i++)
        db.execute("UPDATE geekUser "
                  +"SET processed='err' "
                  +"WHERE warehouse_code='"+warehouse_code+"' "
                  +"AND user_name='" + user_names[i] + "' ") ;
      }
    }
  }
