/*2:*/
#line 88 "rds_util.w"

#ifndef __RDS_UTIL_H
#define __RDS_UTIL_H

/*3:*/
#line 102 "rds_util.w"

#define FALSE  0
#define TRUE   (!FALSE)

#define NUL  0x00  
#define SOH  0x01  
#define STX  0x02  
#define ETX  0x03  
#define EOT  0x04  
#define ACK  0x06  
#define LF   0x0A  
#define CR   0x0D  
#define NAK  0x15  
#define FS   0x1C  
#define GS   0x1D  
#define RS   0x1E  
#define US   0x1F  

#define MIN_TRAK_BOX    1
#define MAX_TRAK_BOX  999
#define TRAK
#define SQL_LEN   1024

#define DEFAULT_CONSEC  5


/*:3*/
#line 92 "rds_util.w"

/*8:*/
#line 176 "rds_util.w"

int util_valid_box(int box,const char name[]);

/*:8*//*10:*/
#line 197 "rds_util.w"

int util_valid_seq(int box,int*seq,const char name[]);

/*:10*//*12:*/
#line 218 "rds_util.w"

char*util_get_control(const char zone[],const char control[],
const char otherwise[]);

/*:12*//*14:*/
#line 240 "rds_util.w"

int util_get_int_control(const char zone[],const char control[],
int otherwise);

/*:14*//*16:*/
#line 294 "rds_util.w"

int util_set_int_control(const char zone[],const char name[],
int value);

/*:16*//*18:*/
#line 310 "rds_util.w"

int util_valid_str(const char val[]);

/*:18*//*20:*/
#line 330 "rds_util.w"

float util_get_float_control(const char zone[],const char control[],
float otherwise);

/*:20*//*22:*/
#line 345 "rds_util.w"

double util_get_elapsed(struct timeval start_time);


/*:22*//*33:*/
#line 523 "rds_util.w"

char*util_get_hint(int box);


/*:33*//*40:*/
#line 615 "rds_util.w"

void util_box_set(int box,const char name[],const char value[]);
void util_box_set_int(int box,const char name[],int value);
char*util_box_get(int box,const char name[]);
int util_box_get_int(int box,const char name[]);
void util_box_clear(int box);


/*:40*//*53:*/
#line 889 "rds_util.w"

int util_create_seq(int box,const char area[]);
void util_carton_set(int seq,const char name[],
const char value[]);
void util_carton_set_int(int seq,const char name[],
const int value);
void util_carton_set_float(int seq,const char name[],
const float value);
void util_set_stamp(int seq,const char name[]);
char*util_carton_get(int seq,const char name[]);
int util_carton_get_int(int seq,const char name[]);
float util_carton_get_float(int seq,const char name[]);
char*util_rds_carton_get(int seq,const char name[]);
int util_rds_carton_get_int(int seq,const char name[]);
float util_rds_carton_get_float(int seq,const char name[]);
char*util_rds_carton_data_get(int seq,const char name[]);
int util_rds_carton_data_get_int(int seq,const char name[]);
float util_rds_carton_data_get_float(int seq,const char name[]);


/*:53*//*55:*/
#line 948 "rds_util.w"

void util_update_status(int seq,const char name[],const char status[],
const char value[]);


/*:55*//*57:*/
#line 987 "rds_util.w"

void util_update_description(int seq,const char area[],const char*fmt,...);


/*:57*//*59:*/
#line 1002 "rds_util.w"

int util_required(int seq,const char name[]);

/*:59*//*61:*/
#line 1016 "rds_util.w"

int util_optional(int seq,const char name[]);

/*:61*//*63:*/
#line 1030 "rds_util.w"

int util_complete(int seq,const char name[]);

/*:63*//*65:*/
#line 1044 "rds_util.w"

int util_failed(int seq,const char name[]);

/*:65*//*67:*/
#line 1060 "rds_util.w"

void util_get_status_value(int seq,const char name[],char status_val[]);

/*:67*//*69:*/
#line 1115 "rds_util.w"

int util_read_scanner(int box,int seq,const char name[],const char device[],char msg[]);

/*:69*//*71:*/
#line 1130 "rds_util.w"

int util_trigger_scale(const char dev_name[]);

/*:71*//*73:*/
#line 1207 "rds_util.w"

float util_get_scale_weight(const char area[],int box,int seq,const char dev_name[]);


/*:73*//*75:*/
#line 1244 "rds_util.w"

void util_poll_for_msg(int box,const char device[],char msg[]);


/*:75*//*80:*/
#line 1329 "rds_util.w"

void util_clear_consec(const char item[]);
void util_bump_consec(const char item[]);
int util_check_consec(const char item[]);
int util_do_consec(const char item[],int success);

/*:80*//*83:*/
#line 1357 "rds_util.w"

int util_is_carton(const char candidate[]);

/*:83*//*85:*/
#line 1381 "rds_util.w"

int util_folder_avail(const char folder[]);


/*:85*//*87:*/
#line 1423 "rds_util.w"

int util_prn_avail(const char prn[]);

/*:87*//*89:*/
#line 1436 "rds_util.w"

int util_strcpy(char*dest,const char*src);

/*:89*//*91:*/
#line 1449 "rds_util.w"

int util_strncpy(char*dest,const char*src,int n);

/*:91*//*94:*/
#line 1482 "rds_util.w"

void util_generate_simple_label(char label[],const char barcode[],
const char msg[]);

/*:94*//*96:*/
#line 1518 "rds_util.w"

int util_get_next_doc(int seq,const char printer[]);

/*:96*//*98:*/
#line 1552 "rds_util.w"

int util_get_doc_seq(int seq,const char doc_type[]);


/*:98*//*100:*/
#line 1612 "rds_util.w"

int util_get_label(int seq,const char printer[],
char label[],int*len);

/*:100*//*102:*/
#line 1631 "rds_util.w"

void util_assign_printer(int seq,const char doc_type[],
const char printer[]);

/*:102*//*104:*/
#line 1685 "rds_util.w"

int util_label_ready(int cartonSeq,const char doc_type[]);


/*:104*//*106:*/
#line 1741 "rds_util.w"

void util_label_printed(int seq,int doc_seq);

/*:106*//*108:*/
#line 1797 "rds_util.w"

void util_label_verified(int seq,int doc_seq);

/*:108*//*110:*/
#line 1819 "rds_util.w"

int zpl_remove_safe(char*pszZpl,int len,const char*pszZplCmd);


/*:110*//*112:*/
#line 1857 "rds_util.w"

int zpl_replace(char*pszZpl,int iMaxZplBufSize,const char*pszZplCmd,
const char*pszNewZplCmd);


/*:112*//*115:*/
#line 1882 "rds_util.w"

int pos(const void*pSrc,int iSrcLen,const void*pNeedle,int iNeedleLen);



/*:115*//*117:*/
#line 1908 "rds_util.w"

int poscase(const void*pSrc,int iSrcLen,const void*pNeedle,
int iNeedleLen);



/*:117*//*119:*/
#line 1932 "rds_util.w"

int lastpos(const void*pSrc,int iSrcLen,const void*pNeedle,int iNeedleLen);



/*:119*//*121:*/
#line 1946 "rds_util.w"

int strpos(const char*pszSrc,const char*pszNeedle);



/*:121*//*123:*/
#line 1961 "rds_util.w"

int strlastpos(const char*pszSrc,const char*pszNeedle);



/*:123*//*125:*/
#line 1983 "rds_util.w"

char*copy(char*pszDst,int iDstSize,const char*pszSrc,int iStartPos,
int iLen);



/*:125*//*127:*/
#line 2009 "rds_util.w"

char*cat(char*pszDst,int iDstSize,const char*pszSrc,int iStartPos,
int iLen);


/*:127*//*129:*/
#line 2021 "rds_util.w"

char*trim(char*pszSrc);


/*:129*//*131:*/
#line 2039 "rds_util.w"

char*righttrim(char*pszSrc);


/*:131*//*133:*/
#line 2062 "rds_util.w"

char*lefttrim(char*pszSrc);



/*:133*//*135:*/
#line 2085 "rds_util.w"

char*strip(char*pszSrc,int iStartPos,int iLen);



/*:135*//*137:*/
#line 2104 "rds_util.w"

char*del(char*pszSrc,const char*pszDelete);



/*:137*//*139:*/
#line 2130 "rds_util.w"

char*ins(char*pszSrc,int iSrcSize,int iStartPos,const char*pszInsert);

/*:139*//*141:*/
#line 2156 "rds_util.w"

int util_timeMS_since(struct timeval start_time);

/*:141*//*143:*/
#line 2179 "rds_util.w"

float util_timeMS_since_float(struct timeval start_time);


/*:143*//*145:*/
#line 2201 "rds_util.w"

int util_get_status(int seq,const char name[],char status[],char val[]);





/*:145*/
#line 93 "rds_util.w"

#ifdef TRAK
/*25:*/
#line 374 "rds_util.w"

int util_get_zone_box(const char name[]);

/*:25*//*27:*/
#line 392 "rds_util.w"

int util_get_zoneDP_box(int zoneDP);

/*:27*//*31:*/
#line 491 "rds_util.w"

void util_zone_get(const char app[],char zone[]);
void util_zone_release(const char app[]);
void util_zone_fault(const char app[]);

/*:31*/
#line 95 "rds_util.w"

#endif  
#line 97 "rds_util.w"

#endif
#line 99 "rds_util.w"


/*:2*/
