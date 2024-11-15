package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
MCM 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;
  String requestId ;
  String clientCode ;
  String requestTime ;
  String msgType ;

  public
  MCM()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("mcm") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  msm(JsonRepresentation entity)
    {
    String answer = "" ;

    try
      {
      boolean success = true ;

      JSONObject input = entity.getJsonObject() ;

      // log the request
      log.start() ;
      log.request(input.toString()) ;

      JSONObject header = input.getJSONObject("header") ;
      requestId = header.optString("requestId") ;
      clientCode = header.optString("clientCode") ;
      requestTime = header.optString("requestTime") ;

      db.execute("INSERT INTO gmsCallback"
                +"(requestId,clientCode) "
                +"VALUES "
                +"('%s','%s') ",
                 requestId,clientCode) ;
      int s_seq = db.getSequence() ;

      JSONObject body = input.getJSONObject("body") ;     
      msgType = body.optString("msgType") ;
 
      GeekUpdate u1 = new GeekUpdate(db,"gmsCallback",body) ;
      u1.addKeyIntLiteral(s_seq,"seq") ;
      u1.addString("msgType") ;
      u1.addString("taskCode") ;
      u1.addLong("instanceId") ;
      u1.addLong("parentInstanceId") ;
      u1.addString("robot") ;
      u1.addString("locationFrom") ;
      u1.addString("locationTo") ;
      u1.addString("waitLocation") ;
      u1.addString("waitDir") ;
      u1.addString("waitNextLocation") ;
      u1.addInt("workFlowPhase") ;
      u1.addInt("taskPhase") ;
      u1.addString("robotPhase") ;
      u1.addString("robotError") ;
      u1.addString("requestTime") ;
      u1.execute() ; 

      db.execute("UPDATE gmsCallback "
                +"SET processed='no' "
                +"WHERE seq="+s_seq+" ") ;

      // log the response
      String resp = response(success) ;
      log.response(resp) ;

      return resp ; 
      }
    catch(Exception e) 
      { 
      e.printStackTrace() ;
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST) ;
      return response(false) ;
      }
      finally {
        db.disconnect();
        //RDSUtil.inform("db connection closed");
      }
    }

  private String
  response(boolean success)
    {
    if(success)
      {
      JSONObject header = new JSONObject() ;
      header.put("responseId",requestId) ;
      header.put("clientCode",clientCode) ;
      header.put("requestTime",requestTime) ;
      header.put("msgType",msgType) ;
      header.put("code","0") ;
      header.put("msg","success") ;

      JSONObject body = new JSONObject() ;

      JSONObject output = new JSONObject() ;
      output.put("header",header) ;
      output.put("body",body) ;
      return output.toString() ;
      }           
    else
      {
      JSONObject header = new JSONObject() ;
      header.put("msgCode","400") ;
      header.put("message","Call failed!") ;

      JSONObject body = new JSONObject() ;

      JSONObject output = new JSONObject() ;
      output.put("header",header) ;
      output.put("body",body) ;
      return output.toString() ;
      }
    }

  @Get
  public String
  handleGet()
    {
    return "hello from MCM!" ;
    }
  }
