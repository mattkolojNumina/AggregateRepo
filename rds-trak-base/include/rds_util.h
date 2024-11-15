/*2:*/
#line 89 "rds_util.w"

#ifndef __RDS_UTIL_H
#define __RDS_UTIL_H

/*3:*/
#line 103 "rds_util.w"

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

#define DEFAULT_CONSEC  5


/*:3*/
#line 93 "rds_util.w"

/*8:*/
#line 177 "rds_util.w"

char*util_get_control(const char zone[],const char control[],
const char otherwise[]);


/*:8*//*10:*/
#line 192 "rds_util.w"

double util_get_elapsed(struct timeval start_time);


/*:10*//*22:*/
#line 383 "rds_util.w"

void util_box_set(int box,const char name[],const char value[]);
void util_box_set_int(int box,const char name[],int value);
char*util_box_get(int box,const char name[]);
int util_box_get_int(int box,const char name[]);
void util_box_clear(int box);


/*:22*//*24:*/
#line 422 "rds_util.w"

int util_create_seq(void);

/*:24*//*27:*/
#line 459 "rds_util.w"

void util_carton_set(const char area[],int seq,const char name[],
const char value[]);
char*util_carton_get(const char area[],int seq,const char name[]);


/*:27*//*29:*/
#line 485 "rds_util.w"

void util_update_status(int seq,const char name[],const char status[],
const char value[]);


/*:29*//*31:*/
#line 515 "rds_util.w"

void util_update_description(const char area[],int seq,
const char hist_id[],const char hist_code[],const char*fmt,...);


/*:31*//*33:*/
#line 531 "rds_util.w"

int util_required(int seq,const char name[]);


/*:33*//*35:*/
#line 546 "rds_util.w"

int util_complete(int seq,const char name[]);


/*:35*//*37:*/
#line 583 "rds_util.w"

void util_poll_for_msg(int box,const char device[],char msg[]);


/*:37*//*42:*/
#line 668 "rds_util.w"

void util_clear_consec(const char item[]);
void util_bump_consec(const char item[]);
int util_check_consec(const char item[]);
int util_do_consec(const char item[],int success);


/*:42*//*45:*/
#line 711 "rds_util.w"

int zpl_replace(char*pszZpl,int iMaxZplBufSize,const char*pszZplCmd,
const char*pszNewZplCmd);


/*:45*//*48:*/
#line 736 "rds_util.w"

int pos(const void*pSrc,int iSrcLen,const void*pNeedle,int iNeedleLen);



/*:48*//*50:*/
#line 762 "rds_util.w"

int poscase(const void*pSrc,int iSrcLen,const void*pNeedle,
int iNeedleLen);



/*:50*//*52:*/
#line 786 "rds_util.w"

int lastpos(const void*pSrc,int iSrcLen,const void*pNeedle,int iNeedleLen);



/*:52*//*54:*/
#line 800 "rds_util.w"

int strpos(const char*pszSrc,const char*pszNeedle);



/*:54*//*56:*/
#line 815 "rds_util.w"

int strlastpos(const char*pszSrc,const char*pszNeedle);



/*:56*//*58:*/
#line 837 "rds_util.w"

char*copy(char*pszDst,int iDstSize,const char*pszSrc,int iStartPos,
int iLen);



/*:58*//*60:*/
#line 863 "rds_util.w"

char*cat(char*pszDst,int iDstSize,const char*pszSrc,int iStartPos,
int iLen);


/*:60*//*62:*/
#line 875 "rds_util.w"

char*trim(char*pszSrc);


/*:62*//*64:*/
#line 893 "rds_util.w"

char*righttrim(char*pszSrc);


/*:64*//*66:*/
#line 916 "rds_util.w"

char*lefttrim(char*pszSrc);



/*:66*//*68:*/
#line 939 "rds_util.w"

char*strip(char*pszSrc,int iStartPos,int iLen);



/*:68*//*70:*/
#line 958 "rds_util.w"

char*del(char*pszSrc,const char*pszDelete);



/*:70*//*72:*/
#line 984 "rds_util.w"

char*ins(char*pszSrc,int iSrcSize,int iStartPos,const char*pszInsert);


/*:72*/
#line 94 "rds_util.w"

#ifdef TRAK
/*15:*/
#line 289 "rds_util.w"

void util_zone_get(const char app[],char zone[]);
void util_zone_release(const char app[]);
void util_zone_fault(const char app[]);


/*:15*/
#line 96 "rds_util.w"

#endif  
#line 98 "rds_util.w"

#endif
#line 100 "rds_util.w"


/*:2*/
