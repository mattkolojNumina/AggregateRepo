/*2:*/
#line 86 "app_lib.w"

#ifndef __APP_H
#define __APP_H

/*3:*/
#line 96 "app_lib.w"

#define LPN_LEN1       3  
#define LPN_LEN2       4  
#define TOTE_LPN_LEN   4  
#define HINT_LEN      64  


/*:3*/
#line 90 "app_lib.w"

/*8:*/
#line 136 "app_lib.w"

char*app_get_carton_val(const char carton_id[],const char name[]);


/*:8*//*10:*/
#line 168 "app_lib.w"

void app_update_description(const char area[],int seq,
const char hist_code[],const char*fmt,...);


/*:10*//*12:*/
#line 189 "app_lib.w"

void app_update_hist(const char area[],int seq,int carton_seq);


/*:12*//*14:*/
#line 220 "app_lib.w"

char*app_get_hint(int box);


/*:14*//*16:*/
#line 261 "app_lib.w"

int app_parse_scan(const char scan_msg[],char barcode[]);


/*:16*//*18:*/
#line 293 "app_lib.w"

void app_generate_simple_label(char label[],const char barcode[],
const char msg[]);


/*:18*//*20:*/
#line 347 "app_lib.w"

int app_label_ready_(int seq,const char printer[]);


/*:20*//*22:*/
#line 386 "app_lib.w"

int app_label_ready(int seq,const char printer[]);



/*:22*//*24:*/
#line 433 "app_lib.w"

int app_get_label_(int seq,const char printer[],char label[],int*len);


/*:24*//*26:*/
#line 493 "app_lib.w"

int app_get_label(int seq,const char printer[],char label[],int*len);



/*:26*//*28:*/
#line 524 "app_lib.w"

void app_label_printed_(int seq,const char printer[],int ordinal);



/*:28*//*30:*/
#line 554 "app_lib.w"

void app_label_printed(int seq,const char printer[],int ordinal);



/*:30*//*34:*/
#line 611 "app_lib.w"

int app_get_lane(const char area[],const char logical[],
char description[]);


/*:34*/
#line 91 "app_lib.w"


#endif
#line 94 "app_lib.w"

/*:2*/
