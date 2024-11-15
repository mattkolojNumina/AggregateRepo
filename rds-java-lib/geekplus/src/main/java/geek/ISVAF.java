package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
ISVAF 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  ISVAF()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("isvaf") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  isvaf(JsonRepresentation entity)
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
      int adjustment_amount = body.optInt("adjustment_amount") ; 

      JSONArray adjustment_list = body.getJSONArray("adjustment_list") ;
      for(int i=0 ; i<adjustment_list.length() ; i++)
        {
        JSONObject adjustment = adjustment_list.getJSONObject(i) ;
        String adjustment_code = adjustment.optString("adjustment_code") ;
        db.execute("INSERT INTO geekInternalFeedback "
                  +"(warehouse_code,adjustment_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+adjustment_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekInternalFeedback",adjustment) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addString("stocktake_code") ;
        u1.addString("operator") ;
        u1.addString("workstation_no") ;
        u1.execute() ;

        if(!adjustment.isNull("sku_list"))
          {
          JSONArray sku_list = adjustment.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            String sku_code = sku.optString("sku_code") ;
            db.execute("INSERT INTO geekInternalFeedbackSku "
                      +"(parent,warehouse_code,adjustment_code,sku_code) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +adjustment_code+"','"+sku_code+"') ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekInternalFeedbackSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ;
            u2.addString("sku_id") ;
            u2.addString("owner_code") ;
            u2.addString("out_batch_code") ;
            u2.addString("reason_code") ;
            u2.addInt("sku_level") ;
            u2.addInt("amount") ;
            u2.addLongDate("production_date") ;
            u2.addLongDate("expiration_date") ;
            u2.execute() ;

            }
          }

        db.execute("UPDATE geekInternalFeedback " 
                  +"SET processed='no' "
                  +"WHERE seq="+seq+" ") ; 
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
