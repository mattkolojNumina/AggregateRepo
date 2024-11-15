#define REG_N 10 
#define NAME_LEN 15
#define DATA_LEN 39

typedef struct{
unsigned value;
unsigned counter;
unsigned carton;
unsigned data[REG_N];
int last_edge;
int ev_head;
int evt_count;
int force_flag;
int force_value;
int leading;
int trailing;
}Dp_Export;


typedef struct{
char name[NAME_LEN+1];
char description[DATA_LEN];
unsigned statement;
unsigned link;
}Ex_List;

void trak_start(void);
void trak_stop(void);
int trak_test(void);


void dp_init(void) ;
int dp_handle(char*name);
int dp_set(int dp_handle,int new_value);

int dp_get(int dp_handle);
unsigned dp_counter(int dp_handle);
int dp_registerset(int dp_handle,int which,int value);
int dp_registerget(int dp_handle,int which,int*value);
int dp_counter_control(int dp_handle,int value);
int dp_setcounter(int dp_handle,unsigned value);


int rp_adddriver(char*name,char*description);
int rp_getdriver(char*name);
int rp_adddevice(char*name,char*description,char*driver,char*driver_data);
int rp_getdevice(char*name);

int rp_new(char*name,char*driver,char*driver_data,char*description);
int rp_handle(char*name);
int rp_set(int rp_handle,unsigned new_value);


unsigned rp_get(int rp_handle);


int ev_insert(int dp_handle,unsigned act_statement,unsigned offset,int a,int b,int c,int d);

int dp_adddriver(char*name,char*description);
int dp_getdriver(char*name);
int dp_adddevice(char*name,char*description,char*driver,char*device_data);
int dp_getdevice(char*name);
int dp_new(char*name,char*device,char*io_data,char*description);

int dp_addtrailing(int dp_handle,int list_handle);
int dp_addleading(int dp_handle,int list_handle);


int ex_getstatement(char*name);
int ex_getcmd(char*name);
int ex_compile(char*name,char*description,int argc,char**argv);
int ex_newlist(char*description,int statement);
int ex_insert(int head,int add_list);

