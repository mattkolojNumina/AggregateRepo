package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
SCCR
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
  SCCR()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("sccr") ;
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
                      +"FROM geekStockCommand "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekStockCommand "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekStockCommand "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("sccr: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build command_list 
    JSONArray command_list = new JSONArray() ; 
    int max_commands = 200 ;
    int command_amount = 0 ;
    String[] seq_codes = new String[max_commands] ;

    List<Map<String,String>> commands
      = db.getResultMapList("SELECT *,"
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),creation_date) AS u_creation_date, "
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),start_datetime) AS u_start_datetime, "
                           //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0,end_datetime) AS u_end_datetime "
                           +"UNIX_TIMESTAMP(creation_date) AS u_creation_date, "
                           +"UNIX_TIMESTAMP(start_datetime) AS u_start_datetime, "
                           +"UNIX_TIMESTAMP(end_datetime) AS u_end_datetime "
                           +"FROM geekStockCommand "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_commands) ; 

    for(Map<String,String> command : commands) 
      {
      seq_codes[command_amount] = command.get("seq") ;

      JSONObject c = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = command.get("owner_code") ;

      // required elements
      c.put("warehouse_code",warehouse_code) ;
      c.put("stocktake_command_code",command.get("stocktake_command_code")) ;
      c.put("type",Integer.parseInt(command.get("type"))) ;
      c.put("creation_date",1000*Long.parseLong(command.get("u_creation_date"))) ;

      // optional string elements
      String[] options 
        =  {"remark"} ;
      for(String option : options)
        if(command.get(option)!=null) 
          c.put(option,command.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"stocktake_command_type","random_stocktake_amount"} ;
      for(String option : i_options)
        if(command.get(option)!=null) 
          c.put(option,Integer.parseInt(command.get(option))) ;

      // optional date elements
      String[] d_options
        = {"start_datetime","end_datetime"} ;
      for(String option : d_options)
        if(command.get("u_"+option)!=null)
          c.put(option,1000*Long.parseLong(command.get("u_"+option))) ;

      // sku_list
      int s_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekStockCommandSku "
                         +"WHERE parent='"+command.get("seq")+"' ",0) ;
      if(s_count>0)
        {
        JSONArray sku_list = new JSONArray() ;

        List<Map<String,String>> skus 
          = db.getResultMapList(
              "SELECT *, "
             //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),production_date) AS u_production_date, " 
             //+"TIMESTAMPDIFF(SECOND,UNIX_TIMESTAMP(0),expiration_date) AS u_expiration_date "
             +"UNIX_TIMESTAMP(production_date) AS u_production_date, " 
             +"UNIX_TIMESTAMP(expiration_date) AS u_expiration_date "
             +"FROM geekStockCommandSku "
             +"WHERE parent=%s ",command.get("seq")) ;
        for(Map<String,String>sku : skus)
          {
          JSONObject s = new JSONObject() ;

          s.put("owner_code",sku.get("owner_code")) ;

          // optional string elements
          String[] sku_options 
            =  { "sku_code","shelf_code","sku_id","packing_spec",
                 "shelf_bin_code","external_batch" } ;
          for(String option : sku_options)
            if(sku.get(option)!=null) 
              s.put(option,sku.get(option)) ;

          // optional int elements
          String[] ski_options 
            =  {"sku_level"} ;
          for(String option : ski_options)
            if(sku.get(option)!=null) 
              s.put(option,Integer.parseInt(sku.get(option))) ;

          // optional date elements
          String[] skd_options
            = {"production_date","expiration_date"} ;
          for(String option : skd_options)
            if(sku.get("u_"+option)!=null)
              s.put(option,1000*Long.parseLong(sku.get("u_"+option))) ;

          sku_list.put(s) ;
          }
        
        c.put("sku_list",sku_list) ;  
        }

      command_list.put(c) ;

      command_amount++ ;
      }

    // build body
    JSONObject body = new JSONObject() ;
    body.put("command_amount",command_amount) ;
    body.put("command_list",command_list) ;

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
               + "/geekplus/api/artemis/pushJson/stockTakeImport"
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
        RDSLog.trace("sccr: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("sccr: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<command_amount ; i++)
        db.execute("UPDATE geekStockCommand "
                  +"SET processed='yes' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual commands

      for(int i=0 ; i<command_amount ; i++)
        db.execute("UPDATE geekStockCommand "
                  +"SET processed='err' "
                  +"WHERE seq='" + seq_codes[i] + "' ") ;
      }
    }
  }
