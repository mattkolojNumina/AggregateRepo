package geek ;

import java.util.Map ;
import java.util.List ;

import org.restlet.representation.Representation ;
import org.restlet.resource.ClientResource ;
import org.restlet.ext.json.JsonRepresentation ;
import org.json.* ;

import rds.* ;

public class
SKSYN
  {
  RDSDatabase db ;
  GeekLog log ;
  String zone = "geekplus" ;
  String user_id  = "" ;
  String user_key = "" ;
  String hostname = "" ;
  String port     = "" ;

  public
  SKSYN()
    {
    db = new RDSDatabase("db") ;
    log = new GeekLog("sksyn") ;
    }

  private void
  getInfo()
    {
    user_id  = db.getControl(zone,"user_id" ,"numina") ;
    user_key = db.getControl(zone,"user_key","12345") ; 
    hostname = db.getControl(zone,"hostname","172.17.31.130") ;
    port     = db.getControl(zone,"port"    ,"4000") ;
    }

  protected void
  cycle()
    {
    // check for changes
    int total 
      = db.getIntValue("SELECT COUNT(*) "
                      +"FROM geekSku "
                      +"WHERE processed='no' ",
                       0) ;
    if(total==0)
      return ;

    // choose warehouse code
    String warehouse_code 
      = db.getValue("SELECT DISTINCT warehouse_code "
                   +"FROM geekSku "
                   +"WHERE processed='no' "
                   +"LIMIT 1 ","") ;

    // owner code
    String owner_code = "" ;

    // get count
    total = db.getIntValue("SELECT COUNT(*) "
                          +"FROM geekSku "
                          +"WHERE processed='no' "
                          +"AND warehouse_code='" + warehouse_code + "' ",
                           0) ;

    RDSLog.inform("sksyn: warehouse code %s unprocessed %d",warehouse_code,total) ;

    // get user parameters
    getInfo() ;

    // build header 
    JSONObject header = new JSONObject() ;
    header.put("warehouse_code",warehouse_code) ;
    header.put("user_id",user_id) ;
    header.put("user_key",user_key) ;
     
    // build sku_list 
    JSONArray sku_list = new JSONArray() ; 
    int max_skus = 1000 ;
    int sku_amount = 0 ;
    String[] sku_codes = new String[max_skus] ;

    List<Map<String,String>> skus
      = db.getResultMapList("SELECT * FROM geekSku "
                           +"WHERE warehouse_code='%s' "
                           +"AND processed='no' "
                           +"LIMIT %d ",
                            warehouse_code,max_skus) ; 

    for(Map<String,String> sku : skus) 
      {
      sku_codes[sku_amount] = sku.get("sku_code") ;

      JSONObject s = new JSONObject() ;
     
      if(owner_code.equals(""))
        owner_code = sku.get("owner_code") ;

      // required elements
      s.put("warehouse_code",warehouse_code) ;
      s.put("owner_code",sku.get("owner_code")) ;
      s.put("sku_code",sku.get("sku_code")) ;

      // optional string elements
      String[] options 
        =  {"owner_name","sku_id","sku_name","remark","unit",
            "sku_abc","wares_type_code","production_location","specification",
            "sku_brand","item_size","item_color","item_style","pic_url"} ;
      for(String option : options)
        if(sku.get(option)!=null) 
          s.put(option,sku.get(option)) ;

      // optional int elements
      String[] i_options 
        =  {"shelf_life","sku_status","min_count","max_count",
            "is_sequence_sku","is_breakable","is_dangerous",
            "is_precious","is_abnormity","is_need_product_batch_manage",
            "is_need_exp_manage","is_need_batch_manage","is_material"} ;
      for(String option : i_options)
        if(sku.get(option)!=null) 
          s.put(option,Integer.parseInt(sku.get(option))) ;

      // optional decimal elements
      String[] d_options 
        =  {"sku_price","sku_length","sku_width","sku_height","sku_volume",
            "net_weight","gross_weight"} ;
      for(String option : d_options)
        if(sku.get(option)!=null) 
          s.put(option,Double.parseDouble(sku.get(option))) ;

      // barcode list
      int b_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekSkuBarcode "
                         +"WHERE warehouse_code='"+warehouse_code+"' "
                         +"AND sku_code='"+sku.get("sku_code")+"' ",0) ;
      if(b_count>0)
        {
        JSONArray bar_code_list = new JSONArray() ;

        List<Map<String,String>> barcodes
          = db.getResultMapList("SELECT * FROM geekSkuBarcode "
                               +"WHERE warehouse_code='"+warehouse_code+"' "
                               +"AND sku_code='%s' ",sku.get("sku_code")) ;
        for(Map<String,String>barcode : barcodes)
          {
          JSONObject b = new JSONObject() ;
          b.put("bar_code",barcode.get("bar_code")) ;
          bar_code_list.put(b) ;
          }
        
        s.put("bar_code_list",bar_code_list) ;  
        }


      // packing spec list
      int p_count 
        = db.getIntValue("SELECT COUNT(*) FROM geekSkuPacking "
                         +"WHERE warehouse_code='"+warehouse_code+"' "
                         +"AND sku_code='"+sku.get("sku_code")+"' ",0) ;
      if(p_count>0)
        {
        JSONArray sku_packing = new JSONArray() ;

        List<Map<String,String>> packings
          = db.getResultMapList("SELECT * FROM geekSkuPacking "
                               +"WHERE sku_code='%s' ",sku.get("sku_code")) ;
        for(Map<String,String>packing : packings)
          {
          JSONObject p = new JSONObject() ;

          p.put("sku_code",packing.get("sku_code")) ;
          p.put("packing_spec",packing.get("packing_spec")) ;

          if(packing.get("mini_packing_code")!=null)
            p.put("mini_packing_code",packing.get("mini_packing_code")) ;
          String[] minis
            = {"mini_packing_amount",
               "mini_length","mini_width","mini_height",
               "mini_volume","mini_weight"} ;
          for(String mini : minis)
            p.put(mini,Double.parseDouble(packing.get(mini))) ; 

          if(packing.get("second_packing_code")!=null)
            p.put("second_packing_code",packing.get("second_packing_code")) ;
          String[] seconds
            = {"second_packing_amount",
               "second_length","second_width","second_height",
               "second_volume","second_weight"} ;
          for(String second : seconds)
            p.put(second,Double.parseDouble(packing.get(second))) ; 

          if(packing.get("third_packing_code")!=null)
            p.put("third_packing_code",packing.get("third_packing_code")) ;
          String[] thirds
            = {"third_packing_amount",
               "third_length","third_width","third_height",
               "third_volume","third_weight"} ;
          for(String third : thirds)
            p.put(third,Double.parseDouble(packing.get(third))) ; 

          sku_packing.put(p) ;
          }
        
        s.put("sku_packing",sku_packing) ;  
        }

      sku_list.put(s) ;
 
      sku_amount++ ;
      }

    // create log record
    log.start() ;

    // build body
    JSONObject body = new JSONObject() ;
    body.put("sku_amount",sku_amount) ;
    body.put("sku_list",sku_list) ;

    // build output
    JSONObject output = new JSONObject() ;
    output.put("header",header) ;
    output.put("body",body) ;

    // log request
    log.request(output.toString()) ;

    // build the URL
    String url = "http://"
               + hostname 
               + ":"
               + port 
               + "/geekplus/api/artemis/pushJson/skuInfoImport"
               + "?warehouse_code="+warehouse_code
               + "&owner_code="+owner_code ;

    // log the url
    log.url(url) ;

    // build the client resource 
    ClientResource cr = new ClientResource(url) ;

    // call the service
    JSONObject input = null ;
    try 
      { 
      JsonRepresentation outputRep = new JsonRepresentation(output) ;
      Representation result = cr.post(outputRep) ; 
      JsonRepresentation inputRep = new JsonRepresentation(result) ;
      input = inputRep.getJsonObject() ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // log the response 
    log.response(input.toString()) ;

    // process the result
    boolean success = false ; 
    try
      {
      int result_code = cr.getStatus().getCode() ;
      if(result_code==200)
        {
        JSONObject result_header = input.getJSONObject("header") ;
        int msgCode = Integer.parseInt(result_header.optString("msgCode")) ;
        String message = result_header.optString("message") ;
        RDSLog.trace("sksyn: msgCode %d message %s",msgCode,message) ;
        if(msgCode==200)
          success=true ;
        }
      else
        RDSLog.alert("sksyn: http call fails, code %d",result_code) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    // mark changes as complete
    if(success)
      {
      for(int i=0 ; i<sku_amount ; i++)
        db.execute("UPDATE geekSku "
                  +"SET processed='yes' "
                  +"WHERE warehouse_code='"+warehouse_code+"' "
                  +"AND sku_code='" + sku_codes[i] + "' ") ;
      }
    else
      {
      // TODO mark individual skus

      for(int i=0 ; i<sku_amount ; i++)
        db.execute("UPDATE geekSku "
                  +"SET processed='err' "
                  +"WHERE warehouse_code='"+warehouse_code+"' "
                  +"AND sku_code='" + sku_codes[i] + "' ") ;
      }
    }
  }
