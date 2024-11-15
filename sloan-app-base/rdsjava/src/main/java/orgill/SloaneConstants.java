package sloane;

public class SloaneConstants {
    public static final String geekWarehouseCode = "Tifton";
    public static final String geekOwnerCode = "sloane";
    public static final int SQL_ERROR = -1;
    public static final int DUPLICATE = -2;
    public static final int GOOD_RESULT = 1;
    
 	// COUNTERS
 	public static final String COUNTER_ORDER_PREP_ERROR = "order_prep_error";
 	public static final String COUNTER_ORDER_PREP_SUCCESS = "order_prep_success";
 	
 	public static final String COUNTER_ORDER_CARTONIZE_ERROR = "order_cartonize_error";
 	public static final String COUNTER_ORDER_CARTONIZE_SUCCESS = "order_cartonize_success";
 	
 	public static final String COUNTER_ORDER_COMPLETE = "order_complete";
 	
 	// SHIPMENT TYPE
 	public static final String SHIPMENTTYPE_LTL = "LTL";
 	public static final String SHIPMENTTYPE_PARCEL = "PARCEL";
 	
 	// ORDER TYPE
 	public static final String ORDERTYPE_TOTE = "TOTE";
 	public static final String ORDERTYPE_BOX = "BOX";
 	public static final String ORDERTYPE_EXPORT = "EXPORT";
 	public static final String ORDERTYPE_ECOM = "ECOM";
 	
 	// toteOrBox value
 	public static final String TOTEORBOX_BOX = "GROUP";
 	public static final String TOTEORBOX_TOTE = "TOTE GROUP";
 	
 	// PALLET TYPE
  
 	
 	public static final String SLOT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

 	public static final String ERROR = "error";
 	
 	public static final String UOM_SHELFPACK = "shelfPack";
 	
 	// PICK TYPE
 	public static final String PICKTYPE_AERSOLBOOM= "AersolBoom";
 	public static final String PICKTYPE_PERISHABLES = "Perishables";
 	public static final String PICKTYPE_LIQUIDS = "Liquids";
 	public static final String PICKTYPE_GEEK = "Geek";
 	public static final String PICKTYPE_ZONEROUTE = "ZoneRoute"; 
 	
 	// CART TYPE
 	public static final String CARTTYPE_PQ= "P&Q";
 	public static final String CARTTYPE_DU= "D&U";
 	
 	//TYPECLASS
 	public static final String TYPECLASS_BOX = "box";
 	public static final String TYPECLASS_TOTE = "tote";
 	public static final String TYPECLASS_EXPORT = "export";

}
