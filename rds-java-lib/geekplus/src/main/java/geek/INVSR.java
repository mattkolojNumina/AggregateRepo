package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
INVSR 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  INVSR()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("invsr") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  isvsr(JsonRepresentation entity)
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
      int current_page = body.optInt("current_page") ; 
      int page_size    = body.optInt("page_size") ;
      int total_page_num = body.optInt("total_page_num") ;

      if(!body.isNull("sku_list"))
        {
        JSONArray sku_list = body.getJSONArray("sku_list") ;
        for(int s=0 ; s<sku_list.length() ; s++)
          {
          JSONObject sku = sku_list.getJSONObject(s) ;

          String sku_code = sku.optString("sku_code") ;
          db.execute("INSERT INTO geekSnapshot "
                    +"(warehouse_code,sku_code,current_page,page_size,total_page_num) "
                    +"VALUES "
                    +"('"+warehouse_code+"','"+sku_code+"',"
                    +current_page+","+page_size+","+total_page_num+") ") ;
          int s_seq = db.getSequence() ;

          GeekUpdate u1 = new GeekUpdate(db,"geekSnapshot",sku) ;
          u1.addKeyIntLiteral(s_seq,"seq") ;
          u1.addString("owner_code") ;
          u1.addString("out_batch_code") ;
          u1.addString("batch_property04") ;
          u1.addString("shelf_code") ;
          u1.addString("shelf_bin_code") ;
          u1.addString("sku_id") ;
          u1.addString("packing_spec") ;
          u1.addInt("sku_level") ;
          u1.addInt("amount") ; 
          u1.addLongDate("production_date") ;
          u1.addLongDate("expiration_date") ;
          u1.addLongDate("audit_time") ;
          u1.execute() ;

          db.execute("UPDATE geekSnapshot "
                    +"SET processed='no' "
                    +"WHERE seq="+s_seq+" ") ;
          }
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
    return "hello from INVSR!" ;
    }
  }
