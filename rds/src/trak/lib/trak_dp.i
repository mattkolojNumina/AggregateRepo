@* DP.
A data point is the basic data object in Trak. Each data point has the following attributes:
\dot name --- a string giving the data point name.
\dot description --- A description of this data point.
\dot driver --- a text string describing the driver processing this I/O.
\dot driver\_data --- a text string parsed by the driver to assign it 
to physical I/O.
\dot value --- the current digital value.
\dot counter --- the current 32 bit unsigned counter value.
\dot carton --- the carton handle (tracking number) assigned to this point.
\dot action lists --- Two
 action lists are assigned to this data point. Action lists are 
actions which will be performed at leading or trailing edges. 
\dot event list --- A sorted event list which causes events to occurr when the counter reaches
presest values is available for each I/O point.

@<Exported Includes@>+=
#include <time.h>
#include <rds_trn.h>

@ Formatting
@f dp_scan int
@f h_scan int

@ Statically allocated sizes.
@<Exported Defines@>+=
#define DP_N (7500)
#define DP_REG_N 50
#define DP_DEV_REG_N 10

@ Counter controls. The default is |CC_BOTH|.
@<Exported Defines@>+=
enum Counter_Controls { CC_BOTH,CC_LEADING, CC_TRAILING} ;

@ DP Record.
@<Exported Structures@>+=
typedef struct 
  {
  int link ; /* handle to next data point in driver list */
  char value ;
  char set_value ; /* value external programs wish to set the bit to */
  char force_flag ;
  char force_value ;
  char name[TRAK_NAME_LEN+1] ;
  char io_data[TRAK_DATA_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;
  int device ; /* handle for the driver */
  unsigned  counter ;  /* tach counter */
  unsigned ev_head ; /* event list, head pointer */
  unsigned leading ; /* handle to action list activated on leading edge */
  unsigned trailing ;  /* handle to action list activated on trailing edge */
  unsigned data[DP_REG_N] ; /* carton handle */
  char counter_control ;
  char trace_on ; 
  char smudge ; /* unused */
  char export ; /* fill out to 4 byte boundary */
  } Dp_Record ;

@ Export.
@<Exported Structures@>+=
typedef struct 
  {
  unsigned value ;
  unsigned counter ;
  unsigned carton ; 
  unsigned data[DP_REG_N] ;
  int ev_head ;
  int evt_count ;
  int force_flag ;
  int force_value  ;
  int leading ;
  int trailing ;
  } Dp_Export ;

@ Device.
@<Exported Structures@>+=
typedef struct 
  {
  int link ; /* next device in driver list */
  char name[TRAK_NAME_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;
  char device_data[TRAK_DATA_LEN+1] ;
  int supplemental[DP_DEV_REG_N] ;
  int head ; /* head pointer to first  */
  int count ;
  } Dp_Device ;

@ Task.
@<Exported Structures@>+=
typedef int (*task_gadfly)(int t) ;

@ Driver.
@<Exported Structures@>+=
typedef struct 
  {
  char name[TRAK_NAME_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;
  int head ; /* first device in list */
  int count ;
  task_gadfly poll ;
  } Dp_Driver ;

@ Pool.
@<Structures@>+=
typedef struct 
  {
  Dp_Device Devices[TRAK_DEVICE_N] ;
  Dp_Driver Drivers[TRAK_DRIVER_N] ;
  Dp_Record Points[DP_N] ;
  int free ;
  } Dp_Pool ;

@ Helpers.
@<Exported Structures@>+=
typedef int (*dp_scan)(Dp_Record *dp) ;
typedef int (*h_show)(int handle) ;

@ Add |Dp_Pool| to the trak core shared memory.
@<Trak Data@>+=
Dp_Pool dp ;

@ Globals.  New static globals are defined next.
@<Globals@>+=
static Dp_Device *devices = 0 ;
static Dp_Driver *drivers = 0 ;
static Dp_Record *points = 0 ;

@ Init..
@<Exported Functions@>+=
void 
dp_init(void)
  {
  int i ;
  int old_count ;
  Dp_Device *dev ;
  Dp_Driver *drv ;
  Dp_Record *r ;

  dp_attach() ;

  r = points ;
  old_count = r->counter ;
  
  for (i=0, dev = devices ; i < TRAK_DEVICE_N ; i++,dev++) 
    {
    bzero(dev,sizeof(Dp_Device)) ;
    dev->head = NIL ;
    }
  for (i=0,drv= drivers ; i < TRAK_DRIVER_N ; i++,drv++) 
    {
    bzero(drv,sizeof(Dp_Driver)) ;
    drv->head = NIL ;
    }
  for (i=0, r = points ; i < DP_N ; i++,r++) 
    {
    bzero(r,sizeof(Dp_Record)) ;
    r->link = i + 1 ;
    }
  r->link = NIL ; 
  pool->dp.free = 0 ;

  @<Add |"trak"| dp@>@;
  }

@ Proto.
@<Exported Prototypes@>+=
void dp_init(void) ;

@ Add |"trak"| dp by hand.
@<Add |"trak"| dp@>=
  {
  Dp_Driver *drv ;
  Dp_Device *dev ;
  Dp_Record *p ;
  int i ;

  drv = drivers ;
  dev = devices ;

  strncpy(drv->name,"engine",TRAK_NAME_LEN) ;
  drv->name[TRAK_NAME_LEN] = 0 ;
  strncpy(drv->description,"Start Stop Driver",TRAK_DATA_LEN) ;
  drv->description[TRAK_DATA_LEN] = 0 ;
  drv->head = NIL ;

  strncpy(dev->name,"engine",TRAK_NAME_LEN) ;
  dev->name[TRAK_NAME_LEN] = 0 ;
  strncpy(dev->description,"Start Stop Device",TRAK_DATA_LEN) ;
  dev->description[TRAK_DATA_LEN] = 0 ;
  strncpy(dev->device_data,"blank",TRAK_DATA_LEN) ;
  dev->device_data[TRAK_DATA_LEN] = 0 ;
  dev->count = 1 ;
  dev->link = drv->head ;
  drv->head = 0 ;
  
  i = pool->dp.free ;

  p = points + pool->dp.free ;
  pool->dp.free = p->link ;

  p->link = dev->head ;
  dev->head = i ; /* add to driver list */
  dev->count++ ;

  strncpy(p->description,"Start/Stop Data Point",TRAK_DATA_LEN) ;
  strncpy(p->io_data,"blank",TRAK_DATA_LEN) ;
  strncpy(p->name,"trak",TRAK_NAME_LEN) ;

  p->description[TRAK_DATA_LEN] = 0 ;
  p->io_data[TRAK_DATA_LEN] = 0 ;
  p->name[TRAK_NAME_LEN] = 0 ;
    
  p->device = 0 ;
  p->value = 0 ;
  p->set_value = 0 ;
  p->counter = old_count+1 ;
  p->counter_control = CC_BOTH ;
  p->ev_head = NIL ;
  p->leading = NIL ;
  p->trailing = NIL ;
  
  p->force_flag = 0 ;
  p->force_value = 0 ;
  }

@ Attach.
@<Functions@>+=
static int 
dp_attach(void) 
  {
  devices = pool->dp.Devices ;
  drivers = pool->dp.Drivers ;
  points = pool->dp.Points ;  
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int dp_attach(void) ;

@ Add driver.
@<Functions@>+=
int 
dp_adddriver(char *name, char *description)
  {
  int i ;
  Dp_Driver *d ;

  trak_init() ;

  i = dp_getdriver(name) ;
  if (i!= NIL) 
    { 
    /* already exists!, update description only */
    d = drivers + i ;
    strncpy(d->description,description,TRAK_DATA_LEN) ;
    d->description[TRAK_DATA_LEN] = 0 ;
    return i ;
    }
  for (i=0,d=drivers ; i < TRAK_DRIVER_N ; i++,d++) 
    {
    if (!strlen(d->name)) 
      {
      strncpy(d->name,name,TRAK_NAME_LEN) ;
      d->name[TRAK_NAME_LEN] = 0 ;
      strncpy(d->description,description,TRAK_DATA_LEN) ;
      d->description[TRAK_DATA_LEN] = 0 ;
      d->head = NIL ;
      return i ;
      }
    }
  return NIL ; 
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_adddriver(char *name, char *description) ;

@ Add Device.
@<Exported Functions@>+=
int 
dp_adddevice(char *name, char *description,char *driver,
             char *device_data) 
  {
  int drv_h ;
  int i; 
  Dp_Device *d ;
  Dp_Driver *drv ;

  trak_init() ;

  drv_h = dp_getdriver(driver) ;
  if (drv_h == NIL) 
    {
    return NIL ;
    }
  drv = drivers + drv_h ;
  i = dp_getdevice(name) ;
  if (i!= NIL) 
    { 
    /* already exists!, update description only */
    d = devices + i ;
    strncpy(d->description,description,TRAK_DATA_LEN) ;
    d->description[TRAK_DATA_LEN] = 0 ;
    return i ;
    }
  for (i=0,d=devices ; i < TRAK_DEVICE_N ; i++,d++) 
    {
    if (!strlen(d->name)) 
      {
      strncpy(d->name,name,TRAK_NAME_LEN) ;
      d->name[TRAK_NAME_LEN] = 0 ;
      strncpy(d->description,description,TRAK_DATA_LEN) ;
      d->description[TRAK_DATA_LEN] = 0 ;
      strncpy(d->device_data,device_data,TRAK_DATA_LEN) ;
      d->device_data[TRAK_DATA_LEN] = 0 ;
      d->head = NIL ;
      d->count = 0 ;
      d->link = drv->head ;
      drv->head = i ;
      return i ;
      }
    }
  return NIL ; 
  }

@ Proto.
@<Exported Prototypes@>+=
int 
dp_adddevice(char *name, char *description,char *driver,
             char *device_data) ;

@ Get Driver. We use simple linear search. This does not have to be fast.
@<Exported Functions@>=
int 
dp_getdriver(char *name)
  {
  int i ;
  Dp_Driver *d ;

  trak_init() ;

  for (i=0, d = drivers ; i < TRAK_DRIVER_N ; i++,d++) 
    {
    if (!strcmp(name,d->name)) return i ;
    }
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_getdriver(char *name) ;

@ Get gadfly.
@<Exported Functions@>+=
task_gadfly 
dp_getdrvgadfly(char *name) 
  {
  Dp_Driver *d ;

  trak_init() ;

  int h = dp_getdriver(name) ;
  if (h != NIL) 
    {
    d=  drivers + h ;
    return d->poll ;
    } 
  return NULL ;
  }

@ Proto.
@<Exported Prototypes@>+=
task_gadfly dp_getdrvgadfly(char *name) ;

@ Set gadfly.
@<Exported Functions@>+=
void 
dp_setdrvgadfly(char *name,task_gadfly task) 
  {
  trak_init() ;

  int h = dp_getdriver(name) ;
  if (h!=NIL) 
    {
    Dp_Driver *d = drivers + h ;
    d->poll = task ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void dp_setdrvgadfly(char *name, task_gadfly task) ;

@ Get device.
@<Exported Functions@>+=
int 
dp_getdevice(char *name)
  {
  int i ;
  Dp_Device *d ;

  trak_init() ;
  
  for (i=0, d = devices ; i < TRAK_DEVICE_N ; i++,d++) 
    {
    if (!strcmp(name,d->name)) return i;
    }
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_getdevice(char *name) ;

@ New. 
@<Exported Functions@>+=
int 
dp_new(char *name,char *device,char *io_data,char *description)
  {
  int i,drv ;
  int type ;
  Dp_Device *d ;
  Dp_Record *p ;

  trak_init() ;
 
  type = const_type(name,&i) ;
  if (type != NIL && type != EX_TOKEN_DP) 
    {
    printf("namespace corruption, %s already exists (%d)\n",name, type) ;
    return NIL ;
    }
  drv = dp_getdevice(device) ;
  if (drv==NIL) 
    return NIL ;

  /* else */
  d = devices + drv ;
  if (i==NIL) 
    { /* new data point */
    i = pool->dp.free ;
    p = points + pool->dp.free ;
    pool->dp.free = p->link ;
    const_set(name,i,EX_TOKEN_DP) ;
    p->link = d->head ;
    d->head = i ; /* add to driver list */
    d->count++ ;
    strncpy(p->description,description,TRAK_DATA_LEN) ;
    strncpy(p->io_data,io_data,TRAK_DATA_LEN) ;
    strncpy(p->name,name,TRAK_NAME_LEN) ;
    p->description[TRAK_DATA_LEN] = 0 ;
    p->io_data[TRAK_DATA_LEN] = 0 ;
    p->name[TRAK_NAME_LEN] = 0 ;
    p->device = drv ;
    p->value = 0 ;
    p->set_value = p->value ;
    p->counter = 0 ;
    p->counter_control = CC_BOTH ;
    p->ev_head = NIL ;
    p->leading = NIL ;
    p->trailing = NIL ;
    p->force_flag = 0 ;
    p->force_value = 0 ;
    return i ;
    }
  else 
    { /* just update description */
    p = points + i ;
    strncpy(p->description,description,TRAK_DATA_LEN) ;
    strncpy(p->io_data,io_data,TRAK_DATA_LEN) ;
    p->description[TRAK_DATA_LEN] = 0 ;
    p->io_data[TRAK_DATA_LEN] = 0 ;
    return i ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
int 
dp_new(char *name,char *device,char *io_data,char *description) ;

@ Device Supplemental Set.
@<Exported Functions@>+=
int 
dp_dev_suppset(int dev_handle, int which, int value) 
  {
  Dp_Device *dev ;

  trak_init() ;

  dev = devices + dev_handle ;
  dev->supplemental[which] = value ; 
  
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_dev_suppset(int dev_handle, int which, int value) ;

@ Handle.
@<Exported Functions@>+=
int 
dp_handle(char *name)
  {
  int type,handle ;

  trak_init() ;

  type = const_type(name,&handle) ;
  if (type == EX_TOKEN_DP) 
    {
    return handle ;
    }
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_handle(char *name) ;

@ Record.
@<Exported Functions@>+=
int 
dp_record(Dp_Record *dp)
  {
  return (dp - points) ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_record(Dp_Record *dp) ;

@ Value Set.
@<Exported Functions@>+=
int 
dp_set(int dp_handle,int new_value)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  p->set_value = new_value ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_set(int dp_handle, int new_value) ;

@ Box Set.
@<Exported Functions@>+=
int 
dp_bxset(int dp_handle,int bx)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p = points +  dp_handle ;
  p->data[0] = bx ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_bxset(int dp_handle, int bx) ;

@ Box Get.
@<Exported Functions@>+=
int 
dp_bxget(int dp_handle,int *bx)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p = points +  dp_handle ;
  *bx = p->data[0] ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_bxget(int dp_handle, int *bx) ;

@ Eval.
Important note: arguments are preset for leading/trailing actions.
these arguments are,
  \dot a --- data point handle.
  \dot b --- data point value.
  \dot c --- data point counter.
  \dot d --- data point carton.
@<Exported Functions@>+=
static int dp_eval_handle = NIL ;
void 
dp_eval(Dp_Record *dp,int new_value)
  {
  int data[4] ;
  int handle ;

  trak_init() ;
  handle = dp_record(dp) ;
  
  if (handle < 0 || handle >= DP_N) {
     Alert("dp_eval called on out of range dp record") ;
     return ;
  }
  dp_eval_handle = handle ;
  if (dp->force_flag) 
    {
    if (dp->force_value == dp->value) 
      return ;
    new_value = dp->force_value ;
    }

  dp->value = new_value ;
  if(dp->counter_control == CC_LEADING && new_value)  
    dp->counter++ ;
  if(dp->counter_control == CC_TRAILING && !new_value) 
    dp->counter++ ;
  if(dp->counter_control == CC_BOTH) 
    dp->counter++ ;

  ev_top(dp-points,dp->device,dp->ev_head,dp->counter) ; 

  if (new_value && dp->leading != NIL) 
    {
    ex_edgeargs(dp->device,handle,dp->value,dp->counter) ;
    ex_eval(dp->device,dp->leading) ;
    }
  if (!new_value && dp->trailing != NIL) 
    {
    ex_edgeargs(dp->device,handle,dp->value,dp->counter) ;
    ex_eval(dp->device,dp->trailing) ;
    }
  if (dp->trace_on) 
    {
    data[0] = 1 ;
    data[1] = handle ;
    data[2] = dp->value ;
    data[3] = dp->counter ;
    mb_post(4,data) ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void dp_eval(Dp_Record *dp,int new_value) ;

@ Value Get.
@<Exported Functions@>+=
int 
dp_get(int dp_handle)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;

  return p->value ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_get(int dp_handle) ;

@ Counter Get.
@<Exported Functions@>+=
unsigned  
dp_counter(int dp_handle) 
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  return p->counter ;
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned dp_counter(int dp_handle) ;

@ Read.
@<Exported Functions@>+=
int 
dp_read(int dp_handle, Dp_Export *status)
  {
  Dp_Record *p ;
  int i;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  bzero(status,sizeof(Dp_Export)) ;
  status->value = p->value ;
  status->counter = p->counter ;
  status->carton = p->data[0] ;
  for (i=0 ;i < DP_REG_N ; i++) 
    status->data[i] = p->data[i] ;

  status->evt_count = ev_count(p->ev_head) ;
  status->ev_head = p->ev_head ;
  status->force_flag = p->force_flag ;
  status->force_value = p->force_value ;
  status->trailing = p->trailing ;
  status->leading = p->leading ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_read(int dp_handle, Dp_Export *status) ;

@ Read Extended.
@<Exported Functions@>+=
int 
dp_readX(int dp_handle,Dp_Record *record)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  *record = *p ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>++
int dp_readX(int dp_handle, Dp_Record *record) ;

@ Settings.
@<Exported Functions@>+=
int 
dp_settings(int dp_handle,
            char *name, char *description, char *device,char *io_data)
  {
  Dp_Record *p ;
  Dp_Device *drv ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  strcpy(name,p->name) ;
  strcpy(description,p->description) ;
  drv = devices + p->device ;
  strcpy(device,drv->name) ;
  strcpy(io_data,p->io_data) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_settings(int dp_handle,
                char *name, char *description, char *device, char *io_data) ;   

@ Force Clear.
@<Exported Functions@>+=
int 
dp_forceclear(int dp_handle)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  p->force_flag = 0 ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_forceclear(int dp_handle) ;

@ Force.
@<Exported Functions@>+=
int 
dp_force(int dp_handle, int force)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  p->force_flag = 1 ;
  p->force_value = force ;
  p->set_value = force ;

  if (p->force_value != p->value) 
    dp_eval(p,force) ; 
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_force(int dp_handle, int force) ;

@ Counter Set Control.
@<Exported Functions@>+=
int 
dp_setcounter_control(int dp_handle, int value)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  if (value == CC_BOTH || value == CC_LEADING || value == CC_TRAILING) 
    {
    p->counter_control = value ;
    }
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_setcounter_control(int dp_handle, int value) ;

@ Set Counter.
@<Exported Functions@>+=
int 
dp_setcounter(int dp_handle, unsigned value)
  {
  Dp_Record *p ;

  if (dp_handle < 0 || dp_handle >= DP_N) 
    return NIL ;

  trak_init() ;

  p= points + dp_handle ;
  p->counter = value ;
  return 0;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_setcounter(int dp_handle, unsigned value) ;

@ IO Scan 
@<Exported Functions@>+=
int 
dp_ioscan(int device_handle, dp_scan iterator)
  {
  Dp_Device *d ;
  Dp_Record *p ;
  
  trak_init() ;

  if ( points != pool->dp.Points) 
    {
    Alert("local var points damaged %p should be %p",
       points, pool->dp.Points) ;
    dp_attach() ;
    }

  d = devices + device_handle ;
  if (d->head == NIL) return 0 ;

  for  (p = points + d->head ;
   p!=NULL;
   p = (p->link == NIL) ? NULL : points + p->link) 
    iterator(p) ;

  return d->count ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_ioscan(int device_handle, dp_scan iterator) ;

@ Scan All.
@<Exported Functions@>+=
int 
dp_scanall(h_show iterator)
  {
  Dp_Record *p ;
  int i,ctr ;

  trak_init() ;
  
  for (ctr=i=0,p=points ; i< DP_N ; i++,p++) 
    {
    if (strlen(p->name)) 
      {
      ctr++; 
      iterator(i) ;
      }
    }
  return ctr ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_scanall(h_show iterator) ;

@ Scan drivers ;
@<Exported Functions@>+=
int 
dp_drivers(h_show iterator)
  {
  Dp_Driver *d ;
  int i,ctr ;

  trak_init() ;
  
  for (ctr=i=0,d=drivers ; i < TRAK_DRIVER_N ; i++,d++) 
    {
    if (strlen(d->name)) 
      {
      ctr++ ;
      iterator(i) ;
      }
    }
  return ctr ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_drivers(h_show iterator) ;

@ Scan devices for a driver.
@<Exported Functions@>+=
int 
dp_devices(char *driver, h_show iterator)
  {
  int handle ;
  Dp_Driver *drv ;
  Dp_Device *d ;
  int ctr ;

  trak_init() ;
  
  handle = dp_getdriver(driver) ;
  if (handle == NIL) return 0 ;
  drv = drivers + handle ;
  for (handle = drv->head ; handle != NIL ; handle = d->link) 
    {
    ctr++ ;
    d = devices + handle ;
    iterator(handle) ;
    }
  return 0 ;  
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_devices(char *driver, h_show iterator) ;

@ Add trailing. 
@<Exported Functions@>+=
int 
dp_addtrailing(int dp_handle, int list_handle)
  {
  Dp_Record *r ;

  trak_init() ;

  if (dp_handle < 0 || dp_handle > DP_N) return NIL ;
  r = points + dp_handle ;
  r->trailing = ex_insert(r->trailing,list_handle) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_addtrailing(int dp_handle, int list_handle) ;

@ Add leading.
@<Exported Functions@>+=
int 
dp_addleading(int dp_handle,int list_handle)
  {
  Dp_Record *r ;

  if (dp_handle < 0 || dp_handle > DP_N) 
    return NIL ;

  trak_init() ;

  r = points + dp_handle ;
  r->leading = ex_insert(r->leading,list_handle) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_addleading(int dp_handle, int list_handle) ;

@ Counter control.
@<Exported Functions@>+=
int 
dp_counter_control(int dp_handle, int value)
  {
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_counter_control(int dp_handle, int value) ;

@ Read Driver.
@<Exported Functions@>+=
int 
dp_readdriver(int handle, Dp_Driver *drv)
  {
  trak_init() ;

  if (handle < 0 || handle > TRAK_DRIVER_N) return NIL ;

  *drv = *(drivers + handle) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_readdriver(int handle, Dp_Driver *drv) ;

@ Read Device.
@<Exported Functions@>+=
int 
dp_readdevice(int handle, Dp_Device *dev)
  {
  trak_init() ;

  if (handle < 0 || handle > TRAK_DEVICE_N) 
    return NIL ;

  *dev = *(devices + handle) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_readdevice(int handle, Dp_Device *dev) ;

@ Set Event Head.
@<Exported Functions@>+=
void 
dp_setevhead(int handle, int newhead)
  {
  Dp_Record *r ;

  if ((handle < 0) || (handle > DP_N)) 
    return ;

  trak_init() ;

  r = points + handle ;
  r->ev_head = newhead ;
  }

@ Proto.
@<Prototypes@>+=
void dp_setevhead(int handle, int newhead) ;

@ Pointer.
@<Exported Functions@>+=
Dp_Record *
dp_pointer(int dp_handle)
  {
  trak_init() ;

  return points + dp_handle ;
  }

@ Proto.
@<Exported Prototypes@>+=
Dp_Record *dp_pointer(int dp_handle) ;

@ Record Get.
@<Exported Functions@>+=
Dp_Record *
dp_record_get(int dp_handle)
  {
  trak_init() ;

  return points + dp_handle ;
  }

@ Proto.
@<Exported Prototypes@>+=
Dp_Record *dp_record_get(int dp_handle) ;

@ Register Set.
@<Exported Functions@>+=
int 
dp_registerset(int handle, int which, int value)
  {
  Dp_Record *r ;

  if ((handle < 0) || (handle > DP_N)) 
    return NIL ;
  if (which < 0 || which >= DP_REG_N) 
    return NIL ;

  trak_init() ;

  r = points + handle ;
  r->data[which] = value ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_registerset(int handle, int which, int value) ;

@ Register Get.
@<Exported Functions@>+=
int 
dp_registerget(int handle,int which, int *value)
  {
  Dp_Record *r ;

  if ((handle < 0) || (handle > DP_N)) 
    return NIL ;
  if (which < 0 || which >= DP_REG_N) 
    return NIL ;

  trak_init() ;

  r = points + handle ;
  *value = r->data[which] ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_registerget(int handle, int which, int *value) ;

@ OK. 
@<Exported Functions@>+=
int 
dp_ok(int dp_handle)
  {
  Dp_Record *r ;

  if ((dp_handle < 0) || (dp_handle > DP_N)) 
    return NIL ;

  trak_init() ;

  r = points + dp_handle ;
  if (!strlen(r->name)) return NIL ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_ok(int dp_handle) ;

@ Trace Set.
@<Exported Functions@>+=
int 
dp_traceset(int dp_handle,int value)
  {
  Dp_Record *r ;

  if ((dp_handle < 0) || (dp_handle > DP_N)) 
    return NIL ;

  trak_init() ;

  r = points + dp_handle ;
  r->trace_on = value ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int dp_traceset(int dp_handle, int value) ;

@ A Dump function. This prints low level information about the dp table.
@<Exported Functions@>+=
int 
dp_dump(void)
  {
  Dp_Driver *drv ;
  Dp_Device *dev ;
  Dp_Record *rec ;
  int i; 

  trak_init() ;

  printf("Dp Drivers\n") ;

  for (i=0 ;i < TRAK_DRIVER_N ; i++) 
    {
    drv = drivers + i ;
    if (!strlen(drv->name)) break ;
    printf("[%d] %s (%s) head: %d count: %d\n",
	   i,
	   drv->name,
	   drv->description,
	   drv->head,
	   drv->count) ;
    }

  printf("DP Devices\n") ;
  for (i=0 ;i < TRAK_DEVICE_N ; i++) 
    {
    dev = devices + i ;
    if (!strlen(dev->name)) break ;
    printf("[%d] %s (%s) io:(%s) link: %d head: %d\n",
	   i,
	   dev->name,
	   dev->description,
	   dev->device_data,
	   dev->link,
	   dev->head) ;
    }
  printf("Dp Points\n") ;
  for (i=0 ;i < DP_N ; i++) 
    {
    rec = points + i ;
    if (!strlen(rec->name)) break ;
    printf("[%d] %s (%s) io: %s\n",
	   i,
	   rec->name,
	   rec->description,
	   rec->io_data) ;
    printf("\tlink: %d value:%d (%d) ctr: %d force: %d(%d)\n",
	   rec->link,
	   rec->value,rec->set_value,
	   rec->counter,
	   rec->force_value,rec->force_flag) ;
    printf("\tev_head: %d leading: %d trailing: %d\n",
	   rec->ev_head,rec->leading,rec->trailing) ;
    printf("\t%d  %d  %d  %d  %d\n",
	   rec->data[0],rec->data[1],rec->data[2],rec->data[3],rec->data[4]) ;
    printf("\t%d  %d  %d  %d  %d\n",
	   rec->data[5],rec->data[6],rec->data[7],rec->data[8],rec->data[9]) ;
    
    }
  
  return 0 ;
  }
int dp_drv_offset()
{
  trak_init() ;
  return (void*)drivers - (void*)pool ;  
}
int dp_dev_offset()
{
  trak_init() ;
  return (void*)devices - (void*)pool ;  
}
int dp_point_offset()
{
  trak_init() ;
  return (void*)points - (void*)pool ;  
}

@ Proto.
@<Exported Prototypes@>+=
int dp_dump(void) ;
int dp_drv_offset(void) ;
int dp_dev_offset() ;
int dp_point_offset() ;

