package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
PKOBCN 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  PKOBCN()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("pkobcn") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  pkobcn(JsonRepresentation entity)
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
      String warehouse_code = header.optString("warehouse_code") ;
      String interface_code = header.optString("interface_code") ;
      String user_id        = header.optString("user_id") ;
      String user_key       = header.optString("user_key") ;
     
      JSONObject body = input.getJSONObject("body") ;     
      String warehouse_body = body.optString("warehouse_code") ;

      JSONArray binding_list = body.getJSONArray("binding_list") ;
      for(int i=0 ; i<binding_list.length() ; i++)
        {
        JSONObject binding = binding_list.getJSONObject(i) ;
        int id = binding.getInt("id") ;
        db.execute("INSERT INTO geekBinding "
                  +"(warehouse_code,id) "
                  +"VALUES "
                  +"('"+warehouse_code+"',"+id+") ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekBinding",binding) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addString("out_order_code") ;
        u1.addString("owner_code") ;
        u1.addString("operator") ;
        u1.addString("container_code") ;
        u1.addString("workstation_no") ;
        u1.addString("seeding_bin_code") ;
        u1.addString("destination") ;
        u1.addInt("order_type") ;
        u1.addLongDate("operate_time") ;
        u1.execute() ;

        db.execute("UPDATE geekBinding "
                  +"SET processed='yes' "
                  +"WHERE seq=" + seq + " ") ;
        }

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
      header.put("msgCode","200") ;
      header.put("message","Call succeed!") ;
      JSONObject body = new JSONObject() ;
      body.put("success",true) ;
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
      body.put("success",false) ;
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
    return "hello from PTOCON!" ;
    }
  }
