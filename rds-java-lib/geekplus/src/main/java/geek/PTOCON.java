package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
PTOCON 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  PTOCON()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("ptocon") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  ptocon(JsonRepresentation entity)
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
      int receipt_amount = body.getInt("receipt_amount") ; 

      JSONArray receipt_list = body.getJSONArray("receipt_list") ;
      for(int i=0 ; i<receipt_list.length() ; i++)
        {
        JSONObject receipt = receipt_list.getJSONObject(i) ;
        String receipt_code = receipt.getString("receipt_code") ;
        db.execute("INSERT INTO geekPutawayConf "
                  +"(warehouse_code,receipt_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+receipt_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekPutawayConf",receipt) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addString("pallet_code") ;
        u1.addString("workstation_no") ;
        u1.addString("receiptor") ;
        u1.addString("supplier_code") ;
        u1.addString("carrier_code") ;
        u1.addInt("status") ;
        u1.addInt("type") ;
        u1.addInt("receipt_status") ;
        u1.addInt("plan_sku_amount") ;
        u1.addInt("plan_sku_type_amount") ;
        u1.addInt("sku_amount") ;
        u1.addInt("sku_type_amount") ;
        u1.addLongDate("start_time") ;
        u1.addLongDate("completion_time") ;
        u1.execute() ;

        if(!receipt.isNull("sku_list"))
          {
          JSONArray sku_list = receipt.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            String sku_code = sku.optString("sku_code") ;
            db.execute("INSERT INTO geekPutawayConfSku "
                      +"(parent,warehouse_code,receipt_code,sku_code) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +receipt_code+"','"+sku_code+"') ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekPutawayConfSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ;
            u2.addString("sku_id") ;
            u2.addString("bar_code") ;
            u2.addString("container_code") ;
            u2.addString("sku_name") ;
            u2.addString("owner_code") ; 
            u2.addString("out_batch_code") ;
            u2.addString("batch_property04") ;
            u2.addString("packing_spec") ;
            u2.addInt("item") ;
            u2.addInt("receipt_flag") ;
            u2.addInt("plan_amount") ;
            u2.addInt("amount") ;
            u2.addInt("sku_level") ;
            u2.addLongDate("production_date") ;
            u2.addLongDate("expiration_date") ;
            u2.execute() ;

            if(!sku.isNull("shelf_bin_list"))
              {
              JSONArray shelf_bin_list = sku.getJSONArray("shelf_bin_list") ;
              for(int b=0 ; b<shelf_bin_list.length() ; b++)
                {
                JSONObject shelf_bin = shelf_bin_list.getJSONObject(b) ;

                db.execute("INSERT INTO geekPutawayConfSkuShelf "
                          +"(parent,warehouse_code,receipt_code,sku_code) "
                          +"VALUES "
                          +"("
                          +s_seq+",'"
                          +warehouse_code+"','"
                          +receipt_code+"','"
                          +sku_code+"') ") ;
                int b_seq = db.getSequence() ;

                GeekUpdate u3 = new GeekUpdate(db,"geekPutawayConfSkuShelf",shelf_bin) ;
                u3.addKeyIntLiteral(b_seq,"seq") ;
                u3.addString("shelf_code") ;
                u3.addString("shelf_bin_code") ;
                u3.addString("operator") ;
                u3.addInt("quantity") ;
                u3.execute() ;

                } 
              }
            }
          }

        db.execute("UPDATE geekPutawayConf " 
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
