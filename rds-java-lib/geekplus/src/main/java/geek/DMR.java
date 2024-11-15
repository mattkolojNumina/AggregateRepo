package geek ;

import java.util.Map ;
import java.util.List ;
import java.util.UUID ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
DMR
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "gms" ;
  String channelId  = "" ;
  String clientCode  = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  DMR()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("dmr") ;
    }

  private void
  getInfo()
    {
    channelId  = db.getControl(zone,"channelId" ,"client-01") ;
    clientCode = db.getControl(zone,"clientCode","geekplus") ; 
    hostname = db.getControl(zone,"hostname","172.17.31.130") ;
    port     = db.getControl(zone,"port"    ,"4000") ;
    }

  protected void
  cycle()
    {
    // check for requests
    List<Map<String,String>> requests 
      = db.getResultMapList("SELECT * FROM gmsRequest "
                           +"WHERE processed='no' ") ;
    if((requests==null) || (requests.size()==0))
      return ;

    int total = requests.size() ;
    RDSLog.inform("dmr: unprocessed %d",total) ;

    // get user parameters
    getInfo() ;

    for(Map<String,String> request : requests)
      {
      int seq = Integer.parseInt(request.get("seq")) ;

      String requestId = request.get("requestId") ;
      if( (requestId==null) || (requestId.length()==0))
        {
        requestId = UUID.randomUUID().toString() ;
        db.execute("UPDATE gmsRequest "
                  +"SET requestId='%s' "
                  +"WHERE seq=%d",requestId,seq) ;
        }

      String requestTime = request.get("requestTime") ;
      if( (requestTime==null) || (requestTime.length()==0))
        {
        requestTime = db.getValue("SELECT NOW()","1970-01-01 00:00:00") ;
        db.execute("UPDATE gmsRequest "
                  +"SET requestTime='%s' "
                  +"WHERE seq=%d",requestTime,seq) ;
        }
      
      // build header 
      JSONObject header = new JSONObject() ;
      header.put("requestId",requestId) ;
      header.put("channelId",channelId) ;
      header.put("clientCode",clientCode) ;
      header.put("requestTime",requestTime) ;

      // build body
      JSONObject body = new JSONObject() ;
     
      // required elements
      body.put("msgType",request.get("msgType")) ;
      body.put("taskType",request.get("taskType")) ;
      body.put("locationFrom",request.get("locationFrom")) ;
      body.put("locationTo",  request.get("locationTo")) ;
      //body.put("taskCode", request.get("taskCode"));

      // optional string elements
      String[] options 
        =  {"taskCode"} ;
      for(String option : options)
        if(request.get(option)!=null) 
          body.put(option,request.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"priority"} ;
      for(String option : i_options)
        if(request.get(option)!=null) 
          body.put(option,Integer.parseInt(request.get(option))) ;

      // optional decimal elements
      String[] d_options 
        =  {} ;
      for(String option : d_options)
        if(request.get(option)!=null) 
          body.put(option,Double.parseDouble(request.get(option))) ;

      // create log record
      log.start() ;

      // build output
      JSONObject output = new JSONObject() ;
      output.put("header",header) ;
      output.put("body",body) ;

      // log request
      log.request(output.toString()) ;

      // build the URL
      String url = "http://"
                 + hostname 
                 + ":"
                 + port 
                 + "/GMSAPI" ;

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
        JSONObject result_header ;
        JSONObject result_body ;

        int result_code = cr.getStatus().getCode() ;
        if(result_code==200)
          {
          result_header = input.getJSONObject("header") ;
          int code = Integer.parseInt(result_header.optString("code")) ;
          String msg = result_header.optString("msg") ;
          RDSLog.trace("dmr: code %d msg %s",code,msg) ;
          if(code==0)
            {
            success=true ;

            GeekUpdate u1 = new GeekUpdate(db,"gmsRequest",result_header) ;
            u1.addKeyIntLiteral(seq,"seq") ;
            u1.addString("responseId") ;
            u1.addInt("code") ;
            u1.addString("msg") ;
            u1.execute() ;

            result_body = input.getJSONObject("body") ;
            GeekUpdate u2 = new GeekUpdate(db,"gmsRequest",result_body) ; 
            u2.addKeyIntLiteral(seq,"seq") ;
            u2.addString("workflowCode") ;
            u2.addInt("instanceId") ;
            u2.addInt("instancePriority") ;
            u2.execute() ;
            }
          }
        else
          RDSLog.alert("dmr: http call fails, code %d",result_code) ;
        }
      catch(Exception e) { e.printStackTrace() ; }

        
      // mark changes as complete
      if(success)
        {
        db.execute("UPDATE gmsRequest "
                  +"SET processed='yes' "
                  +"WHERE seq=%s ",seq) ;
        }
      else
        {
        db.execute("UPDATE gmsRequest "
                  +"SET processed='err' "
                  +"WHERE seq=%s ",seq) ;
        }
      }
    }

  }
