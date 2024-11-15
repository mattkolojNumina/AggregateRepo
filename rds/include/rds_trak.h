/*1:*/
#line 77 "rds_trak.w"

/*11:*/
#line 220 "rds_trak.w"

#define NIL 0xffffffff
#define TRAK_NAME_LEN 31
#define TRAK_DATA_LEN 63
#define TRAK_DRIVER_N 10
#define TRAK_DEVICE_N 500

#line 1 "trak_ipc.i"
/*:11*//*40:*/
#line 26 "trak_dp.i"

#define DP_N (7500)
#define DP_REG_N 50
#define DP_DEV_REG_N 10

/*:40*//*41:*/
#line 32 "trak_dp.i"

enum Counter_Controls{CC_BOTH,CC_LEADING,CC_TRAILING};

/*:41*//*136:*/
#line 3 "trak_rp.i"

#define RP_N (1000)
#define RP_DRIVER_N (10)
#define RP_DEVICE_N (10)
#define RP_DATA_LEN 127

/*:136*//*193:*/
#line 4 "trak_mb.i"

#define MSG_N (1000)
#define MSGPOOL_N (5000)
#define MAXMSG (30)

/*:193*//*210:*/
#line 10 "trak_q.i"

#define Q_N 20
#define QDATA_N 1000

/*:210*//*211:*/
#line 15 "trak_q.i"

enum{Q_FIFO= 1,Q_LIFO= 2,Q_ARRAY};

/*:211*//*242:*/
#line 3 "trak_bx.i"

#define BX_N (1000)

/*:242*//*272:*/
#line 8 "trak_ex.i"

#define EX_N (250000)

/*:272*//*273:*/
#line 12 "trak_ex.i"

#define EX_TOKEN_STATEMENT 1024
#define EX_TOKEN_DP 1025
#define EX_TOKEN_RP 1026
#define EX_TOKEN_Q  1027
#define EX_TOKEN_STRUCT 1028
#define EX_TOKEN_MEMBER 1029
#define EX_TOKEN_DATA   1030
#define EX_TOKEN_MACHINE 1031
#define EX_TOKEN_STATE 1032

/*:273*//*278:*/
#line 59 "trak_ex.i"

#define EX_STACK_N 400

/*:278*//*628:*/
#line 4 "trak_ev.i"

#define EV_N (4000)

/*:628*/
#line 78 "rds_trak.w"

/*3:*/
#line 98 "rds_trak.w"

#include <stdio.h> 
#include <unistd.h> 
#include <stdlib.h> 
#include <string.h> 
#include <ctype.h> 
#include <unistd.h> 
#include <time.h> 
#include <fcntl.h> 
#include <sys/types.h> 
#include <sys/mman.h> 
#include <sys/stat.h> 
#include <sys/ipc.h> 
#include <sys/shm.h> 
#include <sys/sem.h> 

/*:3*//*38:*/
#line 17 "trak_dp.i"

#include <time.h> 
#include <rds_trn.h> 

/*:38*/
#line 79 "rds_trak.w"

/*18:*/
#line 27 "trak_const.i"

typedef struct
{
char name[TRAK_NAME_LEN+1];
unsigned value;
unsigned c_type;
}Const_Record;

/*:18*//*42:*/
#line 36 "trak_dp.i"

typedef struct
{
int link;
char value;
char set_value;
char force_flag;
char force_value;
char name[TRAK_NAME_LEN+1];
char io_data[TRAK_DATA_LEN+1];
char description[TRAK_DATA_LEN+1];
int device;
unsigned counter;
unsigned ev_head;
unsigned leading;
unsigned trailing;
unsigned data[DP_REG_N];
char counter_control;
char trace_on;
char smudge;
char export;
}Dp_Record;

/*:42*//*43:*/
#line 60 "trak_dp.i"

typedef struct
{
unsigned value;
unsigned counter;
unsigned carton;
unsigned data[DP_REG_N];
int ev_head;
int evt_count;
int force_flag;
int force_value;
int leading;
int trailing;
}Dp_Export;

/*:43*//*44:*/
#line 76 "trak_dp.i"

typedef struct
{
int link;
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
char device_data[TRAK_DATA_LEN+1];
int supplemental[DP_DEV_REG_N];
int head;
int count;
}Dp_Device;

/*:44*//*45:*/
#line 89 "trak_dp.i"

typedef int(*task_gadfly)(int t);

/*:45*//*46:*/
#line 93 "trak_dp.i"

typedef struct
{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
int head;
int count;
task_gadfly poll;
}Dp_Driver;

/*:46*//*48:*/
#line 114 "trak_dp.i"

typedef int(*dp_scan)(Dp_Record*dp);
typedef int(*h_show)(int handle);

/*:48*//*137:*/
#line 10 "trak_rp.i"

typedef struct
{
int link;
unsigned set_value;
int device;
char name[TRAK_NAME_LEN+1];
char device_data[RP_DATA_LEN+1];
char description[TRAK_DATA_LEN+1];

unsigned counter;
unsigned trigger;
unsigned trace_on;
int smudge;
}Rp_Record;

/*:137*//*138:*/
#line 27 "trak_rp.i"

typedef struct
{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
int head;
}Rp_Driver;

/*:138*//*139:*/
#line 36 "trak_rp.i"

typedef struct
{
int link;
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
char driver_data[TRAK_DATA_LEN+1];
int driver;
int head;
}Rp_Device;

/*:139*//*140:*/
#line 48 "trak_rp.i"

typedef struct
{
int free;
Rp_Driver Drivers[RP_DRIVER_N];
Rp_Device Devices[RP_DEVICE_N];
Rp_Record Points[RP_N];
}Rp_Pool;

/*:140*//*141:*/
#line 58 "trak_rp.i"

typedef int(*rp_scan)(Rp_Record*dp);
typedef int(*rp_show)(int handle);

/*:141*//*194:*/
#line 10 "trak_mb.i"

typedef struct
{
int count;
int first;
}Mb_Record;

/*:194*//*196:*/
#line 28 "trak_mb.i"

typedef struct
{
int count;
int*data;
}Mb_Result;

/*:196*//*212:*/
#line 19 "trak_q.i"

typedef struct
{
char q_name[TRAK_NAME_LEN+1];
int q_type;
int start;
int s_d;
int f_h,f_t;
int depth;
}Q_Record;

/*:212*//*243:*/
#line 7 "trak_bx.i"

typedef struct
{
int state;
int gap;
int length;
int tach_value;
int tach_handle;
int data;
int stamp;
}Bx_Record;

/*:243*//*274:*/
#line 24 "trak_ex.i"

typedef struct
{
unsigned type;
unsigned link;
unsigned data;
}Ex_Code;

/*:274*//*275:*/
#line 33 "trak_ex.i"

typedef struct
{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN];
unsigned code;
}Ex_Statement;

/*:275*//*276:*/
#line 42 "trak_ex.i"

typedef struct
{
char description[TRAK_DATA_LEN];
unsigned statement;
unsigned link;
}Ex_List;

/*:276*//*277:*/
#line 51 "trak_ex.i"

typedef struct
{
unsigned data[EX_STACK_N];
int depth;
}Ex_Stack;

/*:277*//*279:*/
#line 63 "trak_ex.i"

typedef struct
{
unsigned pc;
Ex_Stack stack;
Ex_Stack r_stack;
unsigned a,b,c,d;
unsigned ea,eb,ec;
unsigned eva,evb,evc,evd;
int trace_on;
}Ex_Engine;

/*:279*//*280:*/
#line 76 "trak_ex.i"

typedef struct
{
int cfree;
int lfree;
Ex_Code Codes[EX_N];
Ex_Statement Statements[EX_N/10];
Ex_List Lists[EX_N/20];
Ex_Engine Engines[TRAK_DRIVER_N];
}Ex_Pool;

/*:280*//*281:*/
#line 88 "trak_ex.i"

typedef int(*list_show)(Ex_List*exlist);
typedef int(*ex_print)(char*operator,int data);

/*:281*//*338:*/
#line 1084 "trak_ex.i"

typedef int(*ex_action)(Ex_Engine*e,int data,int link);
typedef void(*act_compile)(int which,Ex_Code*c,Ex_Stack*s);
typedef struct
{
char token[TRAK_NAME_LEN+1];
ex_action action;
act_compile compile;
}ExActionArray;

/*:338*//*629:*/
#line 8 "trak_ev.i"

typedef struct
{
int n;
int p;
unsigned trigger;
int statement;
int a,b,c,d;
}Ev_Record;

/*:629*//*631:*/
#line 27 "trak_ev.i"

typedef void(*ev_show)(int which,int n,int p,unsigned trigger,int statement,
int a,int b,
int c,int d);

/*:631*/
#line 80 "rds_trak.w"

/*10:*/
#line 212 "rds_trak.w"

void trak_start(void);
void trak_stop(void);
int trak_test(void);
void*trak_ptr(void);
int trak_size(void);

/*:10*//*22:*/
#line 57 "trak_const.i"

void const_init(void);

/*:22*//*26:*/
#line 98 "trak_const.i"

int const_set(char*name,unsigned value,unsigned c_type);

/*:26*//*33:*/
#line 217 "trak_const.i"

unsigned int const_value(char*name);

/*:33*//*35:*/
#line 236 "trak_const.i"

unsigned int const_type(char*name,int*value);

/*:35*//*37:*/
#line 261 "trak_const.i"

void const_dump(void);
#line 1 "trak_dp.i"
/*:37*//*52:*/
#line 166 "trak_dp.i"

void dp_init(void);

/*:52*//*57:*/
#line 276 "trak_dp.i"

int dp_adddriver(char*name,char*description);

/*:57*//*59:*/
#line 328 "trak_dp.i"

int
dp_adddevice(char*name,char*description,char*driver,
char*device_data);

/*:59*//*61:*/
#line 351 "trak_dp.i"

int dp_getdriver(char*name);

/*:61*//*63:*/
#line 373 "trak_dp.i"

task_gadfly dp_getdrvgadfly(char*name);

/*:63*//*65:*/
#line 392 "trak_dp.i"

void dp_setdrvgadfly(char*name,task_gadfly task);

/*:65*//*67:*/
#line 413 "trak_dp.i"

int dp_getdevice(char*name);

/*:67*//*69:*/
#line 479 "trak_dp.i"

int
dp_new(char*name,char*device,char*io_data,char*description);

/*:69*//*71:*/
#line 499 "trak_dp.i"

int dp_dev_suppset(int dev_handle,int which,int value);

/*:71*//*73:*/
#line 520 "trak_dp.i"

int dp_handle(char*name);

/*:73*//*75:*/
#line 532 "trak_dp.i"

int dp_record(Dp_Record*dp);

/*:75*//*77:*/
#line 553 "trak_dp.i"

int dp_set(int dp_handle,int new_value);

/*:77*//*79:*/
#line 574 "trak_dp.i"

int dp_bxset(int dp_handle,int bx);

/*:79*//*81:*/
#line 595 "trak_dp.i"

int dp_bxget(int dp_handle,int*bx);

/*:81*//*83:*/
#line 659 "trak_dp.i"

void dp_eval(Dp_Record*dp,int new_value);

/*:83*//*85:*/
#line 680 "trak_dp.i"

int dp_get(int dp_handle);

/*:85*//*87:*/
#line 700 "trak_dp.i"

unsigned dp_counter(int dp_handle);

/*:87*//*89:*/
#line 734 "trak_dp.i"

int dp_read(int dp_handle,Dp_Export*status);

/*:89*//*93:*/
#line 782 "trak_dp.i"

int dp_settings(int dp_handle,
char*name,char*description,char*device,char*io_data);

/*:93*//*95:*/
#line 804 "trak_dp.i"

int dp_forceclear(int dp_handle);

/*:95*//*97:*/
#line 830 "trak_dp.i"

int dp_force(int dp_handle,int force);

/*:97*//*99:*/
#line 854 "trak_dp.i"

int dp_setcounter_control(int dp_handle,int value);

/*:99*//*101:*/
#line 875 "trak_dp.i"

int dp_setcounter(int dp_handle,unsigned value);

/*:101*//*103:*/
#line 907 "trak_dp.i"

int dp_ioscan(int device_handle,dp_scan iterator);

/*:103*//*105:*/
#line 932 "trak_dp.i"

int dp_scanall(h_show iterator);

/*:105*//*107:*/
#line 957 "trak_dp.i"

int dp_drivers(h_show iterator);

/*:107*//*109:*/
#line 985 "trak_dp.i"

int dp_devices(char*driver,h_show iterator);

/*:109*//*111:*/
#line 1004 "trak_dp.i"

int dp_addtrailing(int dp_handle,int list_handle);

/*:111*//*113:*/
#line 1025 "trak_dp.i"

int dp_addleading(int dp_handle,int list_handle);

/*:113*//*115:*/
#line 1037 "trak_dp.i"

int dp_counter_control(int dp_handle,int value);

/*:115*//*117:*/
#line 1054 "trak_dp.i"

int dp_readdriver(int handle,Dp_Driver*drv);

/*:117*//*119:*/
#line 1072 "trak_dp.i"

int dp_readdevice(int handle,Dp_Device*dev);

/*:119*//*123:*/
#line 1106 "trak_dp.i"

Dp_Record*dp_pointer(int dp_handle);

/*:123*//*125:*/
#line 1120 "trak_dp.i"

Dp_Record*dp_record_get(int dp_handle);

/*:125*//*127:*/
#line 1143 "trak_dp.i"

int dp_registerset(int handle,int which,int value);

/*:127*//*129:*/
#line 1166 "trak_dp.i"

int dp_registerget(int handle,int which,int*value);

/*:129*//*131:*/
#line 1187 "trak_dp.i"

int dp_ok(int dp_handle);

/*:131*//*133:*/
#line 1208 "trak_dp.i"

int dp_traceset(int dp_handle,int value);

/*:133*//*135:*/
#line 1293 "trak_dp.i"

int dp_dump(void);
int dp_drv_offset(void);
int dp_dev_offset();
int dp_point_offset();

#line 1 "trak_rp.i"
/*:135*//*145:*/
#line 98 "trak_rp.i"

void rp_init(void);

/*:145*//*149:*/
#line 151 "trak_rp.i"

int rp_adddriver(char*name,char*description);

/*:149*//*151:*/
#line 175 "trak_rp.i"

int rp_getdriver(char*name);

/*:151*//*153:*/
#line 234 "trak_rp.i"

int rp_new(char*name,char*driver,char*driver_data,char*description);

/*:153*//*155:*/
#line 254 "trak_rp.i"

int rp_handle(char*name);

/*:155*//*157:*/
#line 274 "trak_rp.i"

int rp_set(int rp_handle,unsigned new_value);

/*:157*//*159:*/
#line 296 "trak_rp.i"

int rp_data(int rp_handle,char*data);

/*:159*//*161:*/
#line 378 "trak_rp.i"

int rp_prop(int rp_handle,char*key,char*value);

/*:161*//*163:*/
#line 398 "trak_rp.i"

void rp_eval(Rp_Record*rp,unsigned new_value);

/*:163*//*165:*/
#line 418 "trak_rp.i"

unsigned rp_get(int rp_handle);

/*:165*//*167:*/
#line 445 "trak_rp.i"

int
rp_settings(int rp_handle,
char*name,char*description,char*device,char*device_data);

/*:167*//*169:*/
#line 477 "trak_rp.i"

int
rp_ioscan(int dev_handle,rp_scan iterator);

/*:169*//*171:*/
#line 503 "trak_rp.i"

int rp_driverlist(rp_show iterator);

/*:171*//*173:*/
#line 557 "trak_rp.i"

int rp_adddevice(char*name,char*description,char*driver,char*driver_data);

/*:173*//*175:*/
#line 579 "trak_rp.i"

int rp_getdevice(char*name);

/*:175*//*177:*/
#line 600 "trak_rp.i"

int rp_readdriver(int handle,Rp_Driver*driver);

/*:177*//*179:*/
#line 621 "trak_rp.i"

int rp_readdevice(int handle,Rp_Device*device);

/*:179*//*181:*/
#line 635 "trak_rp.i"

int rp_pointer(Rp_Record*rp);

/*:181*//*183:*/
#line 649 "trak_rp.i"

Rp_Record*rp_record_get(int rp_handle);

/*:183*//*185:*/
#line 681 "trak_rp.i"

int rp_devicelist(char*driver,rp_show iterator);

/*:185*//*187:*/
#line 706 "trak_rp.i"

int rp_scanall(rp_show iterator);

/*:187*//*189:*/
#line 728 "trak_rp.i"

int rp_ok(int handle);

/*:189*//*191:*/
#line 790 "trak_rp.i"

int rp_dump(void);
#line 1 "trak_mb.i"
/*:191*//*200:*/
#line 57 "trak_mb.i"

void mb_init(void);

/*:200*//*204:*/
#line 112 "trak_mb.i"

Mb_Result*mb_poll(void);


/*:204*//*206:*/
#line 129 "trak_mb.i"

void mb_free(Mb_Result*victim);

/*:206*//*208:*/
#line 162 "trak_mb.i"

void mb_post(int count,int*src);
#line 1 "trak_q.i"
/*:208*//*217:*/
#line 74 "trak_q.i"

void q_init(void);

/*:217*//*221:*/
#line 120 "trak_q.i"

int q_mkfifo(char*name,int depth);

/*:221*//*223:*/
#line 151 "trak_q.i"

int q_mklifo(char*name,int depth);

/*:223*//*225:*/
#line 184 "trak_q.i"

int q_mkarray(char*name,int depth);

/*:225*//*227:*/
#line 207 "trak_q.i"

int q_name(int q_h,char*name);

/*:227*//*229:*/
#line 231 "trak_q.i"

unsigned q_pop(int q_h);

/*:229*//*231:*/
#line 255 "trak_q.i"

int q_push(int q_h,unsigned int value);

/*:231*//*237:*/
#line 320 "trak_q.i"

unsigned q_arrayfetch(int q_h,int offset);

/*:237*//*239:*/
#line 344 "trak_q.i"

int q_arraystore(int q_h,int offset,unsigned value);

/*:239*//*241:*/
#line 382 "trak_q.i"

void q_dump(void);

#line 1 "trak_bx.i"
/*:241*//*248:*/
#line 51 "trak_bx.i"

void bx_init(void);

/*:248*//*252:*/
#line 87 "trak_bx.i"

int bx_new(int tach_handle);

/*:252*//*254:*/
#line 109 "trak_bx.i"

void bx_setgap(int bx_no,int gap);

/*:254*//*256:*/
#line 131 "trak_bx.i"

void bx_setlength(int bx_no,int length);

/*:256*//*258:*/
#line 153 "trak_bx.i"

void bx_setdata(int bx_no,int data);

/*:258*//*260:*/
#line 173 "trak_bx.i"

void bx_setstate(int bx_no,unsigned int state);

/*:260*//*262:*/
#line 198 "trak_bx.i"

void bx_move(int bx_no,int tach_handle);

/*:262*//*264:*/
#line 220 "trak_bx.i"

void bx_cancel(int bx_no);

/*:264*//*266:*/
#line 260 "trak_bx.i"

int
bx_read(int bx_no,
int*state,
int*length,
unsigned int*tach_value,
int*tach_handle,
int*stamp,
int*gap,
int*data);

/*:266*//*268:*/
#line 290 "trak_bx.i"

int bx_isvalid(int bx_no);

/*:268*//*270:*/
#line 320 "trak_bx.i"

void bx_dump(void);
#line 1 "trak_ex.i"
/*:270*//*285:*/
#line 143 "trak_ex.i"

void ex_init(void);

/*:285*//*289:*/
#line 178 "trak_ex.i"

int ex_start(int engine);

/*:289*//*291:*/
#line 206 "trak_ex.i"

int ex_edgeargs(int engine,int a,int b,int c);

/*:291*//*293:*/
#line 233 "trak_ex.i"

int ex_evtargs(int engine,int a,int b,int c,int d);

/*:293*//*295:*/
#line 262 "trak_ex.i"

int ex_exec(int engine,int statement);

/*:295*//*297:*/
#line 297 "trak_ex.i"

int ex_eval(int engine,int ex_list);

/*:297*//*299:*/
#line 316 "trak_ex.i"

int ex_getstatement(char*name);

/*:299*//*301:*/
#line 345 "trak_ex.i"

int ex_runlist(int head,list_show iterate);

/*:301*//*303:*/
#line 368 "trak_ex.i"

int ex_readstate(int statement,char*name,char*description,int*start);

/*:303*//*305:*/
#line 436 "trak_ex.i"

int ex_decompile(int statement,ex_print iterate);

/*:305*//*307:*/
#line 457 "trak_ex.i"

Ex_Engine*ex_newengine(void);

/*:307*//*309:*/
#line 474 "trak_ex.i"

int ex_usrstart(Ex_Engine*ex);

/*:309*//*311:*/
#line 489 "trak_ex.i"

void ex_usrargs(Ex_Engine*ex,int a,int b,int c,int d);

/*:311*//*313:*/
#line 559 "trak_ex.i"

int ex_now(Ex_Engine*e,char*cmd);

/*:313*//*315:*/
#line 601 "trak_ex.i"

void ex_showengine(Ex_Engine*e);

/*:315*//*323:*/
#line 667 "trak_ex.i"

int
ex_shellcompile(Ex_Engine*e,char*name,char*descr,
int argc,char**argv);

/*:323*//*325:*/
#line 699 "trak_ex.i"

int ex_compile(char*name,char*description,int argc,char**argv);

/*:325*//*333:*/
#line 965 "trak_ex.i"

int ex_newlist(char*description,int statement);

/*:333*//*335:*/
#line 996 "trak_ex.i"

int ex_insert(int head,int add_list);

/*:335*//*351:*/
#line 1196 "trak_ex.i"

void ex_printcore(void);

/*:351*//*353:*/
#line 1215 "trak_ex.i"

int ex_getcmd(char*name);

/*:353*//*624:*/
#line 3446 "trak_ex.i"

int ex_dumpprint(char*operator,int data);

/*:624*//*626:*/
#line 3491 "trak_ex.i"

void ex_dump(void);
int trak_expool_offset();
#line 1 "trak_ev.i"
/*:626*//*635:*/
#line 62 "trak_ev.i"

void ev_init(void);

/*:635*//*641:*/
#line 166 "trak_ev.i"

int
ev_insert(int dp_handle,unsigned act_statement,unsigned offset,
int a,int b,int c,int d);

/*:641*//*644:*/
#line 213 "trak_ev.i"

void ev_top(int dp,int driver,int ev_head,unsigned counter);

/*:644*//*646:*/
#line 234 "trak_ev.i"

int ev_count(int ev_head);

/*:646*//*648:*/
#line 262 "trak_ev.i"

void ev_list(int ev_head,ev_show iterator);

/*:648*//*650:*/
#line 321 "trak_ev.i"

void ev_dump(void);
#line 236 "rds_trak.w"

/*:650*/
#line 81 "rds_trak.w"


/*:1*/
