package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
SCCF 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  SCCF()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("sccf") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  sccf(JsonRepresentation entity)
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
      int command_amount = body.optInt("command_amount") ; 

      JSONArray command_list = body.getJSONArray("command_list") ;
      for(int i=0 ; i<command_list.length() ; i++)
        {
        JSONObject command = command_list.getJSONObject(i) ;
        String stocktake_code = command.optString("stocktake_code") ;
        db.execute("INSERT INTO geekStockFeedback "
                  +"(warehouse_code,stocktake_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+stocktake_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekStockFeedback",command) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        //u1.addString("stockstake_command_code") ; //bug - typo
        u1.addString("stocktake_command_code") ;
        u1.addString("remark") ;
        u1.addString("operator") ;
        u1.addString("workstation_no") ;
        u1.addInt("status") ;
        u1.addLongDate("creation_date") ;
        u1.addLongDate("operate_time") ;
        u1.execute() ;

        if(!command.isNull("sku_list"))
          {
          JSONArray sku_list = command.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            String sku_code = sku.optString("sku_code") ;
            db.execute("INSERT INTO geekStockFeedbackSku "
                      +"(parent,warehouse_code,stocktake_code,sku_code) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +stocktake_code+"','"+sku_code+"') ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekStockFeedbackSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ;
            u2.addString("sku_id") ;
            u2.addString("owner_code") ;
            u2.addString("bar_code") ;
            u2.addString("packing_spec") ;
            u2.addString("operator") ;
            u2.addString("out_batch_code") ;
            u2.addString("shelf_code") ;
            u2.addString("shelf_bin_code") ;
            u2.addInt("system_amount") ;
            u2.addInt("stocktake_amount") ;
            u2.addInt("sku_level") ; 
            u2.addLongDate("operation_date") ;
            u2.addLongDate("production_date") ;
            u2.addLongDate("expiration_date") ;
            u2.execute() ;

            }
          }

        db.execute("UPDATE geekStockFeedback " 
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
