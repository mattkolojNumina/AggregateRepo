/*2:*/
#line 87 "app_lib.w"

#ifndef __APP_H
#define __APP_H

/*3:*/
#line 97 "app_lib.w"

#define HINT_LEN      64  


/*:3*/
#line 91 "app_lib.w"

/*8:*/
#line 152 "app_lib.w"

int app_lookup_cartonSeq(char lpn[]);

/*:8*//*10:*/
#line 173 "app_lib.w"

char*app_get_carton_val_lpn(const char carton_lpn[],const char column_name[]);

/*:10*//*12:*/
#line 193 "app_lib.w"

char*app_get_carton_val(const int carton_seq,const char column_name[]);

/*:12*//*14:*/
#line 213 "app_lib.w"

char*app_get_carton_data_val(const int carton_seq,const char data_type[]);

/*:14*//*16:*/
#line 232 "app_lib.w"

int app_set_carton_val_lpn(const char carton_lpn[],
const char column_name[],const char value[]);

/*:16*//*18:*/
#line 252 "app_lib.w"

int app_set_carton_val(const int carton_seq,
const char column_name[],const char value[]);

/*:18*//*20:*/
#line 272 "app_lib.w"

char*app_get_order_val(const char order_id[],const char column_name[]);

/*:20*//*22:*/
#line 293 "app_lib.w"

char*app_get_rdsDocuments_val(const char ref_value[],
const char column_name[],const char doc_type[]);

/*:22*//*24:*/
#line 314 "app_lib.w"

char*app_get_label_val(const char seq[],const char column_name[]);

/*:24*//*26:*/
#line 333 "app_lib.w"

char*app_get_physical_lane(const char ship_method[]);

/*:26*//*28:*/
#line 420 "app_lib.w"

int app_label_load(int cartonSeq,char area[],int seq);

/*:28*//*31:*/
#line 482 "app_lib.w"

void app_update_carton_ship_label(const char area[],int seq,int carton_seq);

/*:31*//*33:*/
#line 542 "app_lib.w"

int app_parse_scan(const char scan_msg[],char barcode[]);

/*:33*//*37:*/
#line 578 "app_lib.w"

void app_update_hist(const char area[],int seq,int carton_seq);

/*:37*//*39:*/
#line 616 "app_lib.w"

char*app_get_hint(int box);

/*:39*//*41:*/
#line 649 "app_lib.w"

void app_generate_simple_label(char label[],const char barcode[],
const char msg[]);

/*:41*//*43:*/
#line 689 "app_lib.w"

int app_label_ready(int seq,const char printer[]);

/*:43*//*45:*/
#line 751 "app_lib.w"

int app_get_label(int seq,const char printer[],char label[],int*len);

/*:45*//*47:*/
#line 781 "app_lib.w"

void app_label_printed(int seq,const char printer[],int ordinal);

/*:47*//*51:*/
#line 843 "app_lib.w"

int app_get_lane(const char area[],const char logical[],char description[]);

/*:51*//*53:*/
#line 862 "app_lib.w"

int app_trigger_scale(const char dev_name[]);

/*:53*//*55:*/
#line 883 "app_lib.w"

int app_get_physical_code(const char area[],const char physical[]);

/*:55*//*57:*/
#line 918 "app_lib.w"

void app_get_lpn_label(char label[],int*len);

/*:57*//*59:*/
#line 930 "app_lib.w"

int app_carton_ready(int cartonSeq);

/*:59*//*61:*/
#line 948 "app_lib.w"

int app_first_box(char*area);

/*:61*//*63:*/
#line 967 "app_lib.w"

int app_last_box(char*area);

/*:63*//*65:*/
#line 979 "app_lib.w"

int app_carton_needs_qc(int cartonSeq);

/*:65*//*67:*/
#line 996 "app_lib.w"

char*string(int val);

/*:67*/
#line 92 "app_lib.w"


#endif
#line 95 "app_lib.w"

/*:2*/
