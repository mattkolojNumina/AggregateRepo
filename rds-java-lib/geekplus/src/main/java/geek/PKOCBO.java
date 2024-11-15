package geek ;

import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation ;

import org.json.* ;

import rds.* ;

public class 
PKOCBO 
extends ServerResource 
  {
  RDSDatabase db ;
  GeekLog log ;

  public
  PKOCBO()
    {
    super() ;
    db = new RDSDatabase("db") ;
    log = new GeekLog("pkocbo") ;
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
      int order_amount = body.getInt("order_amount") ; 

      JSONArray order_list = body.getJSONArray("order_list") ;
      for(int i=0 ; i<order_list.length() ; i++)
        {
        JSONObject order = order_list.getJSONObject(i) ;
        String out_order_code = order.getString("out_order_code") ;
        db.execute("INSERT INTO geekPickConf "
                  +"(warehouse_code,out_order_code) "
                  +"VALUES "
                  +"('"+warehouse_code+"','"+out_order_code+"') ") ;
        int seq = db.getSequence() ;

        GeekUpdate u1 = new GeekUpdate(db,"geekPickConf",order) ;
        u1.addKeyIntLiteral(seq,"seq") ;
        u1.addString("owner_code") ;
        u1.addString("picker") ;
        u1.addString("shop_code") ;
        u1.addString("shop_name") ;
        u1.addString("wall_code") ;
        u1.addInt("order_type") ;
        u1.addInt("status") ;
        u1.addInt("is_exception") ;
        u1.addInt("lack_flag") ;
        u1.addInt("pick_type") ;
        u1.addInt("plan_sku_amount") ;
        u1.addInt("pickup_sku_amount") ;
        u1.addLongDate("start_time") ; 
        u1.addLongDate("finish_date") ;
        u1.execute() ;


        if(!order.isNull("sku_list"))
          {
          JSONArray sku_list = order.getJSONArray("sku_list") ;
          for(int s=0 ; s<sku_list.length() ; s++)
            {
            JSONObject sku = sku_list.getJSONObject(s) ;

            int item = sku.optInt("item") ;
            db.execute("INSERT INTO geekPickConfSku "
                      +"(parent,warehouse_code,out_order_code,item) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +out_order_code+"',"+item+") ") ;
            int s_seq = db.getSequence() ;

            GeekUpdate u2 = new GeekUpdate(db,"geekPickConfSku",sku) ;
            u2.addKeyIntLiteral(s_seq,"seq") ; 
            u2.addString("sku_id") ;
            u2.addString("sku_code") ;
            u2.addString("owner_code") ;
            u2.addString("packing_spec") ;
            u2.addString("out_batch_code") ;
            u2.addString("bar_code") ;
            u2.addString("remark") ; 
            u2.addInt("plan_amount") ;
            u2.addInt("pickup_amount") ;
            u2.addInt("sku_level") ;
            u2.addLongDate("production_data") ;
            u2.addLongDate("expiration_date") ;
            u2.execute() ;

            if(!sku.isNull("sn_list"))
              {
              JSONArray sn_list = sku.getJSONArray("sn_list") ;
              for(int n=0 ; n<sn_list.length() ; n++)
                {
                JSONObject sn = sn_list.getJSONObject(n) ;

                String sequence_no = sn.getString("sequence_no") ;
                db.execute("INSERT INTO geekPickConfSkuSeq "
                          +"(parent,warehouse_code,out_order_code,item,sequence_no) "
                          +"VALUES "
                          +"("
                          +s_seq+",'"
                          +warehouse_code+"','"
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
                db.execute("INSERT INTO geekPickConfSkuShelf "
                          +"(parent,warehouse_code,out_order_code,item,shelf_code) "
                          +"VALUES "
                          +"("
                          +s_seq+",'"
                          +warehouse_code+"','"
                          +out_order_code+"',"
                          +item+",'"+shelf_code+"') ") ;
                int b_seq = db.getSequence() ;

                GeekUpdate u3 = new GeekUpdate(db,"geekPickConfSkuShelf",shelf_bin) ;
                u3.addKeyIntLiteral(b_seq,"seq") ;
                u3.addString("shelf_bin_code") ;
                u3.addString("operator") ;
                u3.addInt("quantity") ;
                u3.execute() ;

                } 
              }

            }
          }

        if(!order.isNull("container_list"))
          {
          JSONArray container_list = order.getJSONArray("container_list") ;
          for(int c=0 ; c<container_list.length() ; c++)
            {
            JSONObject container = container_list.getJSONObject(c) ;

            String container_code = container.optString("container_code") ;
            db.execute("INSERT INTO geekPickConfContainer "
                      +"(parent,warehouse_code,out_order_code,container_code) "
                      +"VALUES "
                      +"("+seq+",'"+warehouse_code+"','"
                      +out_order_code+"','"+container_code+"') ") ;
            int c_seq = db.getSequence() ;

            GeekUpdate u4 = new GeekUpdate(db,"geekPickConfContainer",container) ;
            u4.addKeyIntLiteral(c_seq,"seq") ;
            u4.addString("pallet_code") ;
            u4.addString("workstation_no") ;
            u4.addString("seeding_bin_code") ;
            u4.addString("picker") ;
            u4.addInt("sku_amount") ;
            u4.addInt("sku_type_amount") ;
            u4.addLongDate("production_date") ;
            u4.addLongDate("expiration_date") ;
            u4.execute() ;


            if(!container.isNull("sku_list"))
              {
              JSONArray c_sku_list = container.getJSONArray("sku_list") ;
              for(int k=0 ; k<c_sku_list.length() ; k++)
                {
                JSONObject c_sku = c_sku_list.getJSONObject(k) ;

                int item = c_sku.getInt("item") ;
                db.execute("INSERT INTO geekPickConfContainerSku "
                          +"(parent,warehouse_code,out_order_code,container_code,item) "
                          +"VALUES "
                          +"("
                          +c_seq+",'"
                          +warehouse_code+"','"
                          +out_order_code+"','"
                          +container_code+"',"
                          +item+") ") ;
                int k_seq = db.getSequence() ;

                GeekUpdate u5 = new GeekUpdate(db,"geekPickConfContainerSku",c_sku) ;
                u5.addKeyIntLiteral(k_seq,"seq") ;
                u5.addString("sku_code") ;
                u5.addString("sku_id") ;
                u5.addString("owner_code") ;
                u5.addString("bar_code") ;
                u5.addString("out_batch_code") ;
                u5.addString("packing_spec") ;
                u5.addString("remark") ;
                u5.addInt("sku_level") ;
                u5.addInt("amount") ; 
                u5.addLongDate("production_date") ;
                u5.addLongDate("expiration_date") ;
                u5.execute() ;


                if(!c_sku.isNull("sn_list"))
                  {
                  JSONArray cs_sn_list = c_sku.getJSONArray("sn_list") ;
                  for(int m=0 ; m<cs_sn_list.length() ; m++)
                    {
                    JSONObject cs_sn = cs_sn_list.getJSONObject(m) ;

                    String sequence_no = cs_sn.getString("sequence_no") ;
                    db.execute("INSERT INTO geekPickConfContainerSkuSeq "
                          +"(parent,warehouse_code,out_order_code,container_code,item,sequence_no) "
                          +"VALUES "
                          +"("
                          +k_seq+",'"
                          +warehouse_code+"','"
                          +out_order_code+"','"
                          +container_code+"',"
                          +item+",'"+sequence_no+"') ") ;
                    int m_seq = db.getSequence() ;

                    }
                  }
                } 
              }
            }
          }


        db.execute("UPDATE geekPickConf " 
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
