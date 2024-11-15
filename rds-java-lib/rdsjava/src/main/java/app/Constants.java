package app;

public class Constants {
	// COUNTERS
	public static final String COUNTER_ORDER_PREP_ERROR = "order_prep_error";
	public static final String COUNTER_ORDER_PREP_SUCCESS = "order_prep_success";
	
	public static final String COUNTER_ORDER_CARTONIZE_ERROR = "order_cartonize_error";
	public static final String COUNTER_ORDER_CARTONIZE_SUCCESS = "order_cartonize_success";
	
	public static final String COUNTER_ORDER_COMPLETE = "order_complete";
	public static final String COUNTER_PARCEL_ORDER_COMPLETE = "parcel_order_complete";
	public static final String COUNTER_LTL_ORDER_COMPLETE = "ltl_order_complete";
	
	// SHIPMENT TYPE
	public static final String SHIPMENTTYPE_LTL = "LTL";
	public static final String SHIPMENTTYPE_PARCEL = "PARCEL";
	
	// ORDER TYPE
	public static final String ORDERTYPE_LTL = "LTL";
	public static final String ORDERTYPE_LTL_SINGLEPALLET = "LTL_SINGLEPALLET";
	public static final String ORDERTYPE_PARCEL = "PARCEL";
	public static final String ORDERTYPE_CONVERTEDLTL = "CONVERTED_LTL";
	public static final String ORDERTYPE_CONVERTEDPARCEL = "CONVERTED_PARCEL";
	
	// PALLET TYPE
	public static final String PALLETTYPE_AMRBULK = "AMR-BULK";
	public static final String PALLETTYPE_AMROVERPACK = "AMR-OVERPACK";
	public static final String PALLETTYPE_BULK = "BULK";
	public static final String PALLETTYPE_SHIPPING = "SHIPPING";
	
	
	// carton typeclass
	public static final String TYPECLASS_PADDEDMAILER = "paddedMailer";
	public static final String TYPECLASS_SPLITCASE = "splitCase";

	// HOST ORDER STATUS
	public static final String ORDERSTATUS_CARTONIZED = "cartonized";
	public static final String ORDERSTATUS_PICKSTART = "picking-start";
	public static final String ORDERSTATUS_PICKEND = "picking-end";
	public static final String ORDERSTATUS_COMPLETE = "complete";
	
	// HOST CARTON STATUS
	public static final String CARTONSTATUS_CARTONIZED = "cartonized";
	public static final String CARTONSTATUS_PICKSTART = "picking-start";
	public static final String CARTONSTATUS_PICKEND = "picking-end";
	public static final String CARTONSTATUS_COMPLETE = "complete";
	
	// PROCESS LOCATION TYPE
	public static final String LOCATIONTYPE_SINGLEPALLET = "singlePallet";
	public static final String LOCATIONTYPE_CONSOLIDATIONCELL = "consolidateCell";
	
	public static final String SLOT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String ERROR = "error";
	
	public static final String UOM_PALLET = "PA";
	public static final String UOM_EACH = "EA";
	public static final String UOM_CASE = "CA";
	
	public static final String PICKTYPE_SPLITCASE = "splitCase";
	public static final String PICKTYPE_TOTE = "tote";
	public static final String PICKTYPE_BULK = "bulk";
	public static final String PICKTYPE_AMRBULK = "amr-bulk";
	public static final String PICKTYPE_AMROVERPACK = "amr-overpack";
	public static final String PICKTYPE_PALLET = "pallet";
	public static final String PICKTYPE_UNPICK = "unpick";
	public static final String PICKTYPE_NESTTING = "nestting";
   
}
