/*1:*/
#line 75 "./trak_drv.w"

/*3:*/
#line 4 "./core.i"

#include <rds.h> 
#ifndef NULL
#define NULL (0)
#endif
#ifdef MODULE
#include <linux/kernel.h> 
#define __NO_VERSION__
#include <linux/module.h> 
#include <linux/version.h> 
#include <rtai_shm.h> 
#endif

/*6:*/
#line 50 "./core.i"

#define MAGIC (0xCAFEFACE)
#define TRAK_KEY (RDS_BASE+0x40)
#define EOS ('\0')
/*:6*//*13:*/
#line 111 "./core.i"


#define TRAK_NAME_LEN 31
#define TRAK_DATA_LEN 63
#define NIL 0xffffffff

/*:13*//*21:*/
#line 14 "./const.i"


#define CONST_N (DP_N + RP_N + ACT_N/10)

/*:21*//*37:*/
#line 34 "./dp.i"

#define DP_N (5000)
#define DRIVER_N (10)
#define DEVICE_N (500)
#define REG_N 50
#define TRAK_DEV_REGS 10

enum Counter_Controls{CC_BOTH,CC_LEADING,CC_TRAILING};

/*:37*//*75:*/
#line 16 "./rp.i"




#define RP_N (1000)
#define DRIVER_N (10)
#define RP_DATA_LEN 127

/*:75*//*106:*/
#line 2 "./mb.i"



#define MSG_N (1000)
#define MSGPOOL_N (5000)
#define MAXMSG (30)
/*:106*//*119:*/
#line 8 "./q.i"

#define Q_N 20
#define QDATA_N 1000

/*:119*//*136:*/
#line 6 "./carton.i"

#define CARTON_N (1000)

/*:136*//*164:*/
#line 9 "./action.i"




#define ACT_N (80000)

#define TOKEN_STATEMENT 1024
#define TOKEN_DP 1025
#define TOKEN_RP 1026
#define TOKEN_Q  1027

#define TOKEN_STRUCT 1028
#define TOKEN_MEMBER 1029
#define TOKEN_DATA   1030
#define TOKEN_MACHINE 1031
#define TOKEN_STATE 1032

/*:164*//*261:*/
#line 3 "./event.i"



#define EV_N (4000)

/*:261*/
#line 17 "./core.i"

/*22:*/
#line 19 "./const.i"

typedef struct{
char name[TRAK_NAME_LEN+1];
unsigned value;
unsigned c_type;
}Const_Record;

typedef struct{
int count;
unsigned int index[CONST_N];
Const_Record Names[CONST_N];
}Const_Pool;


/*:22*//*38:*/
#line 44 "./dp.i"

typedef struct{
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
unsigned data[REG_N];
char counter_control;
char trace_on;
char smudge;
char export;
}Dp_Record;



typedef struct{
unsigned value;
unsigned counter;
unsigned carton;
unsigned data[REG_N];
int ev_head;
int evt_count;
int force_flag;
int force_value;
int leading;
int trailing;
}Dp_Export;
typedef struct{
int link;
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
char device_data[TRAK_DATA_LEN+1];
int supplemental[TRAK_DEV_REGS];
int head;
int count;
}Dp_Device;


typedef int(*task_gadfly)(int t);

typedef struct{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
int head;
int count;
task_gadfly poll;
}Dp_Driver;

typedef struct{
Dp_Device Devices[DEVICE_N];
Dp_Driver Drivers[DRIVER_N];
Dp_Record Points[DP_N];
int free;
}Dp_Pool;
typedef int(*dp_scan)(Dp_Record*dp);
typedef int(*h_show)(int handle);

/*:38*//*76:*/
#line 25 "./rp.i"

typedef struct{
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



typedef struct{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
int head;
}Rp_Driver;

typedef struct{
int link;
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN+1];
char driver_data[TRAK_DATA_LEN+1];
int driver;
int head;
}Rp_Device;
typedef struct{
int free;
Rp_Driver Drivers[DRIVER_N];
Rp_Device Devices[DRIVER_N];
Rp_Record Points[RP_N];
}Rp_Pool;

typedef int(*rp_scan)(Rp_Record*dp);
typedef int(*rp_show)(int handle);
/*:76*//*107:*/
#line 9 "./mb.i"

typedef struct{
int count;
int first;
}Mb_Record;




typedef struct{
int head;
Mb_Record messages[MSG_N];
int buf_head;
int Data[MSGPOOL_N];
}Mb_Pool;

typedef struct{

int count;
int*data;
}Mb_Result;

/*:107*//*121:*/
#line 32 "./q.i"


enum{Q_FIFO= 1,Q_LIFO= 2,Q_ARRAY};
typedef struct{
char q_name[TRAK_NAME_LEN+1];
int q_type;
int start;
int s_d;
int f_h,f_t;
int depth;
}Q_Record;


typedef struct{
Q_Record head[Q_N];
unsigned int data[QDATA_N];
int highwater;
int nextrecord;
}Q_Pool;

/*:121*//*137:*/
#line 10 "./carton.i"

typedef struct{
int state;
int gap;
int length;
int tach_value;
int tach_handle;
int data;
int stamp;
}Bx_Record;

typedef struct{
Bx_Record cartons[CARTON_N];
int next;
}Bx_Pool;

/*:137*//*165:*/
#line 27 "./action.i"

typedef struct{
unsigned type;
unsigned link;
unsigned data;
}Ex_Code;

typedef struct{
char name[TRAK_NAME_LEN+1];
char description[TRAK_DATA_LEN];
unsigned code;
}Ex_Statement;

typedef struct{
char description[TRAK_DATA_LEN];
unsigned statement;
unsigned link;
}Ex_List;

#define STACK_D 200
typedef struct{
unsigned data[STACK_D];
int depth;
}Ex_Stack;
typedef struct{
unsigned pc;
Ex_Stack stack;
Ex_Stack r_stack;
unsigned a,b,c,d;
unsigned ea,eb,ec;
unsigned eva,evb,evc,evd;
int trace_on;
}Ex_Engine;

typedef struct{
int cfree;
int lfree;
Ex_Code Codes[ACT_N];
Ex_Statement Statements[ACT_N/10];
Ex_List Lists[ACT_N/20];
Ex_Engine Engines[DRIVER_N];
}Ex_Pool;
typedef int(*list_show)(Ex_List*alist);
typedef int(*ex_print)(char*operator,int data);


/*:165*//*197:*/
#line 901 "./action.i"

typedef int(*ex_action)(Ex_Engine*e,int data,int link);
typedef void(*act_compile)(int which,Ex_Code*c,Ex_Stack*s);
typedef struct{
char token[TRAK_NAME_LEN+1];
ex_action action;
act_compile compile;
}ExActionArray;

/*:197*//*262:*/
#line 12 "./event.i"


typedef struct{
int n;
int p;
unsigned trigger;
int statement;
int a,b,c,d;
}Ev_Record;


typedef struct{
Ev_Record Records[EV_N];
int free;
}Ev_Pool;
typedef void(*ev_show)(int which,int n,int p,unsigned trigger,int statement,
int a,int b,
int c,int d);

/*:262*/
#line 18 "./core.i"

/*10:*/
#line 71 "./core.i"

void trak_start(void);
void trak_stop(void);
int trak_test(void);
void*trak_ptr(void);
int trak_size(void);

/*:10*//*23:*/
#line 34 "./const.i"

void const_init(void);
void const_attach(void);
unsigned int const_value(char*name);

/*:23*//*44:*/
#line 152 "./dp.i"

void dp_init(void);
void dp_eval(Dp_Record*dp,int new_value);
int dp_scanall(h_show iterator);
int dp_drivers(h_show iterator);
int dp_devices(char*driver,h_show iterator);
int dp_ok(int dp_handle);


int dp_readdriver(int handle,Dp_Driver*drv);
int dp_readdevice(int handle,Dp_Device*dev);

int dp_handle(char*name);
int dp_set(int dp_handle,int new_value);

int dp_get(int dp_handle);
unsigned dp_counter(int dp_handle);
int dp_bxset(int dp_handle,int bx);
int dp_bxget(int dp_handle,int*bx);
int dp_registerset(int dp_handle,int which,int value);
void dp_dev_suppset(int dev_handle,int which,int value);
int dp_registerget(int dp_handle,int which,int*value);
int dp_read(int dp_handle,Dp_Export*status);
int dp_readX(int dp_handle,Dp_Record*status);
int dp_settings(int dp_handle,char*name,char*description,
char*device,char*io_data);
Dp_Record*dp_pointer(int dp_handle);
int dp_counter_control(int dp_handle,int value);
int dp_setcounter(int dp_handle,unsigned value);
int dp_record(Dp_Record*dp);


/*:44*//*78:*/
#line 70 "./rp.i"


void rp_Init(void);
int rp_adddriver(char*name,char*description);
int rp_getdriver(char*name);
int rp_adddevice(char*name,char*description,char*driver,char*driver_data);
int rp_getdevice(char*name);
int rp_readdriver(int handle,Rp_Driver*driver);
int rp_readdevice(int handle,Rp_Device*device);
int rp_pointer(Rp_Record*rp);

int rp_new(char*name,char*driver,char*driver_data,char*description);
int rp_handle(char*name);
int rp_set(int rp_handle,unsigned new_value);


unsigned rp_get(int rp_handle);
int rp_settings(int rp_handle,char*name,char*description,
char*driver,char*driver_data);

int rp_setdata(int rp_handle,char*data);
int rp_setprop(int rp_handle,char*key,char*value);

int rp_ok(int handle);

int rp_dump(void);
/*:78*//*80:*/
#line 104 "./rp.i"

int rp_driverlist(rp_show iterator);
int rp_devicelist(char*driver,rp_show iterator);
int rp_scanall(rp_show iterator);

/*:80*//*110:*/
#line 42 "./mb.i"

void mb_init(void);

Mb_Result*mb_poll(void);

void mb_post(int count,int*data);
void mb_free(Mb_Result*victim);

/*:110*//*120:*/
#line 13 "./q.i"


void q_init(void);
void q_attach(void);

int q_mkfifo(char*name,int depth);
int q_mklifo(char*name,int depth);
int q_mkarray(char*name,int depth);

unsigned q_pop(int q_h);
int q_push(int q_h,unsigned value);

unsigned q_arrayfetch(int q_h,int offset);
int q_arraystore(int q_h,int offset,unsigned value);

int q_name(int q_h,char*name);

void q_dump(void);
/*:120*//*141:*/
#line 39 "./carton.i"


void bx_init(void);

int bx_new(int tach_handle);
void bx_setgap(int bx_no,int gap);
void bx_setlength(int bx_no,int length);
void bx_move(int bx_no,int tach_handle);
void bx_cancel(int bx_no);
void bx_setdata(int bx_no,int data);
void bx_setstate(int bx_no,unsigned int state);
int bx_read(int bx_no,
int*state,
int*length,
unsigned int*tach_value,
int*tach_handle,
int*stamp,
int*gap,
int*data);
int bx_isvalid(int bx_no);

void bx_dump(void);
/*:141*//*167:*/
#line 80 "./action.i"


void ex_init(void);
int ex_start(int engine);

int ex_exec(int engine,int statement);
int ex_eval(int engine,int ex_list);


int ex_edgeargs(int engine,int a,int b,int c);
int ex_evtargs(int engine,int a,int b,int c,int d);


/*:167*//*265:*/
#line 42 "./event.i"


int ev_insert(int dp_handle,unsigned act_statement,unsigned offset,
int a,int b,int c,int d);
void ev_init(void);
int ev_count(int ev_head);
void ev_top(int dp_handle,int driver,int ev_head,unsigned counter);
void ev_dump(void);
/*:265*/
#line 19 "./core.i"

/*41:*/
#line 123 "./dp.i"

int dp_ioscan(int driver_handle,dp_scan iterator);


task_gadfly dp_getdrvgadfly(char*name);
void dp_setdrvgadfly(char*name,task_gadfly task);
/*:41*//*79:*/
#line 97 "./rp.i"



int rp_ioscan(int device_handle,rp_scan iterator);
void rp_eval(Rp_Record*dp,unsigned new_value);

/*:79*//*266:*/
#line 51 "./event.i"




/*:266*/
#line 20 "./core.i"


/*:3*//*16:*/
#line 205 "./core.i"

void bzero(void*in,int len);

#line 1 "./ipc.i"
/*:16*/
#line 76 "./trak_drv.w"


/*:1*/
