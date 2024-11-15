package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
PKOCBC 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  PKOCBC()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("pkocbc") ;
    }

  @Override
  public void 
  doInit() 
    {
    }

  @Post("json")
  public String 
  pkocbo(JsonRepresentation entity)
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
//      int container_amount = body.getInt("container_amount") ; 

      JSONArray container_list = body.getJSONArray("container_list") ;
      for(int i=0 ; i<container_list.length() ; i++)
        {
        JSONObject container = container_list.getJSONObject(i) ;
        String container_code = container.getString("container_code") ;
        db.execute("INSERT INTO geekPickContainerConf "
                  +"(warehouse_code,container_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+container_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekPickContainerConf",container) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addInt("container_type") ;
        u1.addString("pallet_code") ;
        u1.addInt("sku_amount") ;
        u1.addInt("sku_type_amount") ;
        u1.addLongDate("creation_date") ;
        u1.addString("picker") ;
        u1.addString("workstation_no") ;
        u1.addInt("target_outlet") ;
        u1.addString("pick_seeding_pin_no") ;
        u1.execute() ;


        if(!container.isNull("sku_list"))
          {
          JSONArray sku_list = container.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            String out_order_code = sku.optString("out_order_code") ;
            int item = sku.optInt("item") ;
            db.execute("INSERT INTO geekPickContainerConfSku "
                      +"(parent,warehouse_code,container_code,out_order_code,item) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +container_code+"','"
                      +out_order_code+"',"+item+") ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekPickContainerConfSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ; 
            u2.addString("sku_code") ;
            u2.addString("sku_id") ;
            u2.addString("bar_code") ;
            u2.addString("owner_code") ;
            u2.addInt("sku_level") ;
            u2.addInt("amount") ;
            u2.addLongDate("production_date") ;
            u2.addLongDate("expiration_date") ;
            u2.addString("out_batch_code") ;
            u2.addString("packing_spec") ;
            u2.addInt("pickup_seq") ;
            u2.addLongDate("pick_order_item_finish_time") ;
            u2.addInt("lack_flag") ;
            u2.addInt("is_last_container") ;
            u2.addInt("container_amount") ;
            u2.execute() ;

            if(!sku.isNull("sn_list"))
              {
              JSONArray sn_list = sku.getJSONArray("sn_list") ;
              for(int n=0 ; n<sn_list.length() ; n++)
                {
                JSONObject sn = sn_list.getJSONObject(n) ;

                String sequence_no = sn.getString("sequence_no") ;
                db.execute("INSERT INTO geekPickContainerConfSkuSeq "
                          +"(parent,warehouse_code,container_code, "
                          +"out_order_code,item,sequence_no) "
                          +"VALUES "
                          +"("
                          +s_seq+",'"
                          +warehouse_code+"','"
                          +container_code+"','"
                          +out_order_code+"',"
                          +item+",'"+sequence_no+"') ") ;
                int n_seq = db.getSequence() ;

                } 
              }

            if(!sku.isNull("shelf_bin_list"))
              {
              JSONArray shelf_bin_list = sku.getJSONArray("shelf_bin_list") ;
              for(int b=0 ; b<shelf_bin_list.length() ; b++)
                {
                JSONObject shelf_bin = shelf_bin_list.getJSONObject(b) ;

                String shelf_code = shelf_bin.getString("shelf_code") ;
                db.execute("INSERT INTO geekPickContainerConfSkuShelf "
                          +"(parent,warehouse_code,container_code, "
                          +"out_order_code,item,shelf_code) "
                          +"VALUES "
                          +"("
                          +s_seq+",'"
                          +warehouse_code+"','"
                          +container_code+"','"
                          +out_order_code+"',"
                          +item+",'"+shelf_code+"') ") ;
                int b_seq = db.getSequence() ;

                GeekUpdate u3 = new GeekUpdate(db,"geekPickContainerConfSkuShelf",shelf_bin) ;
                u3.addKeyIntLiteral(b_seq,"seq") ;
                u3.addString("shelf_bin_code") ;
                u3.addString("operator") ;
                u3.addInt("quantity") ;
                u3.execute() ;

                } 
              }

            }
          }

        db.execute("UPDATE geekPickContainerConf " 
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
    return "hello from PKOCBC!" ;
    }
  }
