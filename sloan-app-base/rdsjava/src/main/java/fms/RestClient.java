package fms ;

import java.util.Map ;
import java.util.List ;
import java.lang.System ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.restlet.data.ChallengeScheme ;

import org.json.* ;

import rds.* ;

public class
RestClient
  {
  RDSDatabase db ;
  String zone="fms" ;
  String service = "fmsRest" ;
  FMSLog log ;
  FMSError error ;
  String ip ;
  String port ;
  String user ;
  String pass ;

  public
  RestClient()
    {
    db = new RDSDatabase("db") ;
    log = new FMSLog(service) ;
    error = new FMSError(service) ;
    }

  private void
  getInfo()
    {
    ip   = db.getControl(zone,"ip","10.14.0.57") ;
    port = db.getControl(zone,"port"    ,"9901") ;
    user = db.getControl(zone,"user","root") ;
    pass = db.getControl(zone,"pass","toor") ;
    }

  protected void
  cycle()
    {
    // get user parameters
    getInfo() ;

    // get transactions to send
    List<Map<String,String>> transactions 
      = db.getResultMapList("SELECT * "
                           +"FROM fmsRequests "
                           +"WHERE sent='no' " ) ;
    if((transactions!=null) && (!transactions.isEmpty()))
      {
      for(Map<String,String>transaction : transactions)
        {
        String seq     = transaction.get("seq") ;
        String service = transaction.get("service") ;
        String request = transaction.get("request") ;

        String response = restRequest(seq,service,request) ;

        db.execute("UPDATE fmsRequests "
                  +"SET sent = 'yes', "
                  +"response = '%s' "
                  +"WHERE seq = %s ",response,seq) ;
        }
      }
    }

  private String
  restRequest(String seq, String service, String request)
    {
    String response = null ;
    int logSeq = 0 ;

    try
      {
      // create log record
      logSeq = log.start() ;

      // log request
      log.request(request) ;

      // build the URL
      String url = "http://"
                 + ip 
                 + ":"
                 + port 
                 + "/"
                 + service ;
 
      // log the url
      log.url(url) ;

      // build the client resource 
      ClientResource cr = new ClientResource(url) ;
 
      // set the authentication
      cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC,user,pass) ;
 
      // call the service
      JSONObject input = null ;
      long start = System.currentTimeMillis() ;
      long stop = start ;
      String description = "" ;
      boolean success = false ;
      try 
        { 
        JSONObject output = new JSONObject(request) ;
        JsonRepresentation outputRep = new JsonRepresentation(output) ;
System.out.println(cr) ;
        Representation result = cr.post(outputRep) ; 
System.out.println(result) ;
        JsonRepresentation inputRep = new JsonRepresentation(result) ;
        input = inputRep.getJsonObject() ;
        }
      catch(Exception e) 
        { 
        e.printStackTrace() ;
        }
      stop = System.currentTimeMillis() ;

      if(input != null)
        {
        // log the response 
        log.response(input.toString()) ;
        response = input.toString() ;
        }
      }
    catch(Exception e) 
      { 
      e.printStackTrace() ; 
      error.log("request seq "+seq,"error",e.getMessage(),logSeq) ;
      }

    return response ;
    }

  }
