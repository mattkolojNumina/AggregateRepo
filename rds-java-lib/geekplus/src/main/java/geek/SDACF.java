package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
SDACF 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  SDACF()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("sdacf") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  sdacf(JsonRepresentation entity)
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
        String out_adjustment_code = adjustment.optString("out_adjustment_code") ;
        db.execute("INSERT INTO geekStockAdjustConf "
                  +"(warehouse_code,out_adjustment_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+out_adjustment_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekStockAdjustConf",adjustment) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addString("stocktake_command_code") ;
        u1.addInt("status") ;
        u1.execute() ;


        if(!adjustment.isNull("sku_list"))
          {
          JSONArray sku_list = adjustment.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            String sku_code = sku.optString("sku_code") ;
            db.execute("INSERT INTO geekStockAdjustConfSku "
                      +"(parent,warehouse_code,out_adjustment_code,sku_code) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +out_adjustment_code+"','"+sku_code+"') ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekStockAdjustConfSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ;
            u2.addString("packing_spec") ;
            u2.addString("owner_code") ;
            u2.addString("out_batch_code") ;
            u2.addInt("sku_level") ;
            u2.addInt("amount") ;
            u2.addInt("line_status") ;
            u2.execute() ;

            }
          }

        db.execute("UPDATE geekStockAdjustConf " 
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
