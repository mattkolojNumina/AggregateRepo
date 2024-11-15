@* RP.

@<Exported Defines@>+=
#define RP_N (1000)
#define RP_DRIVER_N (10)
#define RP_DEVICE_N (10) 
#define RP_DATA_LEN 127

@ Record.
@<Exported Structures@>+=
typedef struct 
  {
  int link ; /* handle to next data point in driver list */
  unsigned  set_value ; /* value external programs wish to set the bit to */
  int device ;
  char name[TRAK_NAME_LEN+1] ;
  char device_data[RP_DATA_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;

  unsigned  counter ;  /* tach counter */
  unsigned  trigger ; /* action list on change */
  unsigned trace_on ;
  int smudge ;
  } Rp_Record ;

@ Driver
@<Exported Structures@>+=
typedef struct 
  {
  char name[TRAK_NAME_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;
  int head ; /* head pointer to first,device  */
  } Rp_Driver ;

@ Device.
@<Exported Structures@>+=
typedef struct 
  {
  int link ;
  char name[TRAK_NAME_LEN+1] ;
  char description[TRAK_DATA_LEN+1] ;
  char driver_data[TRAK_DATA_LEN+1] ;
  int driver ;
  int head ; /* first pointer to rp in device list */
  } Rp_Device ;

@ Pool.
@<Exported Structures@>+=
typedef struct 
  {
  int free ;
  Rp_Driver Drivers[RP_DRIVER_N] ;
  Rp_Device Devices[RP_DEVICE_N] ;
  Rp_Record Points[RP_N] ;
  } Rp_Pool ;

@ Helpers.
@<Exported Structures@>+=
typedef int (*rp_scan)(Rp_Record *dp) ;
typedef int (*rp_show)(int handle) ;

@ We add to the global pool.
@<Trak Data@>=
Rp_Pool rp ;

@ Useful pointers.
@<Globals@>+=
static Rp_Record *rp_points = 0 ;
static Rp_Driver *rp_drivers = 0 ;
static Rp_Device *rp_devices = 0 ;

@ Init.
@<Exported Functions@>+=
void 
rp_init(void)
  {
  int i;
  Rp_Record *p ;
  Rp_Driver *d ;
  
  rp_attach() ;

  bzero(&pool->rp,sizeof(Rp_Pool)) ;
  for (i=0,p=rp_points ; i < RP_N ; i++,p++) 
    {
    bzero(p,sizeof(Rp_Record)) ;
    p->link = i+1 ;
    }
  p->link = NIL ;
  for (i=0,d=rp_drivers ; i < RP_DRIVER_N ; i++,d++) 
    {
    d->head = NIL ;
    }
  pool->rp.free = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void rp_init(void) ;

@ Attach.
@<Functions@>=
static void 
rp_attach(void)
  {
  if (rp_points) return ;
  rp_points = pool->rp.Points ;
  rp_drivers = pool->rp.Drivers ;
  rp_devices = pool->rp.Devices ;
  }

@ Proto.
@<Prototypes@>+=
static void rp_attach(void) ;

@ Add Driver.
@<Exported Functions@>+=
int 
rp_adddriver(char *name, char *description)
  {
  int drv ;
  Rp_Driver *d ;

  trak_init() ;

  drv = rp_getdriver(name) ;
  if (drv != NIL) 
    {
    d = rp_drivers + drv ;
    strncpy(d->description,description,TRAK_DATA_LEN) ;
    return drv ;
    } /* else */
  
  for (drv=0,d=rp_drivers; drv < RP_DRIVER_N ; drv++,d++) 
    {
    if (!strlen(d->name)) 
      {
      strncpy(d->name,name,TRAK_NAME_LEN) ;
      strncpy(d->description,description,TRAK_DATA_LEN) ;
      d->name[TRAK_NAME_LEN] = 0 ;
      d->description[TRAK_DATA_LEN] = 0 ;
      d->head = NIL ;
      return drv ;
      }
    }
  
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_adddriver(char *name, char *description) ;

@ Get Driver.
@<Exported Functions@>+=
int 
rp_getdriver(char *name)
  {
  int drv ;
  Rp_Driver *d ;
  
  trak_init() ;

  for (drv=0,d=rp_drivers; drv < RP_DRIVER_N ; drv++,d++) 
    {
    if (!strcmp(name,d->name)) 
      {
      return drv ;
      }
    }  
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_getdriver(char *name) ;

@ New.
@<Exported Functions@>+=
int 
rp_new(char *name,char *driver,char *driver_data,char *description)
  {
  int dev ;
  int i,type ;
  Rp_Device *d ;
  Rp_Record *p ;

  trak_init() ;

  dev = rp_getdevice(driver) ;
  if (dev == NIL) 
    return NIL ;

  /* else */
  d = rp_devices + dev ;
  type = const_type(name,&i) ;
  if (type != NIL && type != EX_TOKEN_RP) 
    {
    printf("%s already in namespace (%d)",name,type) ;
    return NIL ;
    }
  
  if (i==NIL) 
    {
    i = pool->rp.free ;
    p = rp_points + i ;
    pool->rp.free = p->link ;
    const_set(name,i,EX_TOKEN_RP) ;
    p->link = d->head ;
    d->head = i ;
    strncpy(p->description,description,TRAK_DATA_LEN) ;
    strncpy(p->device_data,driver_data,RP_DATA_LEN) ;
    strncpy(p->name,name,TRAK_NAME_LEN) ;
    p->description[TRAK_DATA_LEN] = 0 ;
    p->device_data[RP_DATA_LEN] = 0 ;
    p->name[TRAK_NAME_LEN] = 0 ;
    p->counter = 0 ;
    p->trigger = NIL ;
    p->device = dev ;
    }
  else 
    { 
    /* just update description and driver data */
    p = rp_points + i ;
    strncpy(p->description,description,TRAK_DATA_LEN) ;
    strncpy(p->device_data,driver_data,RP_DATA_LEN) ;
    p->description[TRAK_DATA_LEN] = 0 ;
    p->device_data[RP_DATA_LEN] = 0 ;
    }
  return i ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_new(char *name,char *driver,char *driver_data,char *description) ;

@ Handle.
@<Exported Functions@>+=
int 
rp_handle(char *name)
  {
  int handle,type ;
  
  trak_init() ;

  type = const_type(name,&handle) ;
  if (type == EX_TOKEN_RP) 
    return handle ;
   
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_handle(char *name) ;

@ Set.
@<Exported Functions@>+=
int 
rp_set(int rp_handle,unsigned new_value)
  {
  Rp_Record *p ;
  
  if (rp_handle < 0 || rp_handle >= RP_N) return NIL ;

  trak_init() ;

  p = rp_points + rp_handle ;
  p->set_value = new_value ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_set(int rp_handle, unsigned new_value) ;

@ Data.
@<Exported Functions@>+=
int 
rp_data(int rp_handle, char *data)
  {
  Rp_Record *p ;
  
  if (rp_handle < 0 || rp_handle >= RP_N) 
    return NIL ;

  trak_init() ;

  p = rp_points + rp_handle ;
  strncpy(p->device_data,data,RP_DATA_LEN) ;
  p->device_data[RP_DATA_LEN] = 0 ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_data(int rp_handle, char *data) ;

@ Prop.
@<Exported Functions@>+=
int 
rp_prop(int rp_handle,char *key, char *value)
  {
  Rp_Record *p ;
  char *prop_key, *next_key, prop_buf[RP_DATA_LEN+1], prop_pair[32];
  int len;
  
  if (rp_handle < 0 || rp_handle >= RP_N) 
    return NIL ;

  trak_init() ;

  p = rp_points + rp_handle ;
  sprintf(prop_pair,"%s=%s",key,value);

  if((len = strlen(p->device_data)) > 0) 
    { 
    // need to check if key already here
    prop_key = strstr(p->device_data,key);
    if(prop_key) 
      { 
      // it's here
      if(*prop_key == p->device_data[0]) 
        { 
        //key is the first property
        next_key = strchr(prop_key,',');
        if(next_key) 
          { 
          // there's another property
          strcpy(p->device_data,prop_pair);
          strcat(p->device_data,",");
          strcat(p->device_data,++next_key);
          }
        else  
          {
          // it's the only property
          strcpy(p->device_data,prop_pair);
          }
        }
      else 
        { 
        // it's not the first property 
        next_key = strchr(prop_key,',');
        *prop_key = '\0';
        if(next_key) 
          { 
          // there's another property
          strcpy(prop_buf,p->device_data);
          strcat(prop_buf,",");
          strcat(prop_buf,prop_pair);
          strcpy(p->device_data,prop_pair);
          }
        else  
          {
          // it's the last property
          strcat(p->device_data,prop_pair);
          }
        }
      }
    else 
      { 
      // just add it at the end
      strcat(p->device_data,",");
      strcat(p->device_data,prop_pair);
      }
    }
  else 
    {
    // just add it
    strcpy(p->device_data,prop_pair);
    }

  p->device_data[RP_DATA_LEN] = 0 ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_prop(int rp_handle,char *key, char *value) ;

@ Eval.
@<Exported Functions@>+=
void 
rp_eval(Rp_Record *rp, unsigned new_value)
  {
  if (rp->counter != new_value) 
    {
    rp->counter = new_value ;
    if (rp->trigger != NIL) 
      {
      ex_edgeargs(rp->device,rp - rp_points,rp->counter,0) ;
      ex_eval(rp->device,rp->trigger) ;
      }
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void rp_eval(Rp_Record *rp, unsigned new_value) ;

@ Get.
@<Exported Functions@>+=
unsigned 
rp_get(int rp_handle)
  {
  Rp_Record *p ;
  
  if (rp_handle < 0 || rp_handle >= RP_N) 
    return NIL ;

  trak_init() ;

  p = rp_points + rp_handle ;
  return p->counter ;
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned rp_get(int rp_handle) ;

@ Settings.
@<Exported Functions@>+=
int 
rp_settings(int rp_handle,
            char *name, char *description, char *device,char *device_data)
  {
  Rp_Record *p ;
  Rp_Device *d ;

  if (rp_handle < 0 || rp_handle >= RP_N) 
    return NIL ;

  trak_init() ;
  
  p = rp_points + rp_handle ;
  strcpy(name,p->name) ;
  strcpy(description,p->description) ;
  d = rp_devices + p->device ;
  strcpy(device,d->name) ;
  strcpy(device_data,p->device_data) ;
  return p->counter ;
  }

@ Proto.
@<Exported Prototypes@>+=
int 
rp_settings(int rp_handle,
            char *name, char *description, char *device,char *device_data) ;

@ IO Scan.
@<Exported Functions@>+=
int 
rp_ioscan(int dev_handle,rp_scan iterator)
  {
  Rp_Device *d ;
  Rp_Record *p ;
  int ctr ;

  if (dev_handle < 0 || dev_handle >= RP_DRIVER_N) 
    return NIL ;

  trak_init() ;
  
  d = rp_devices + dev_handle ;
  if (d->head == NIL) return 0 ;
  p = rp_points + d->head ; 

  for  (ctr=0,p = rp_points + d->head ;
	p!=NULL;
	p = (p->link == NIL) ? NULL : rp_points + p->link,ctr++)
    iterator(p) ;

  return ctr ;
  }

@ Proto.
@<Exported Prototypes@>+=
int 
rp_ioscan(int dev_handle,rp_scan iterator) ;

@ Driver List.
@<Exported Functions@>+=
int 
rp_driverlist(rp_show iterator)
  {
  int i,ctr ;
  Rp_Driver *d ;

  trak_init() ;
  
  for (ctr=i=0, d = rp_drivers ; i < RP_DRIVER_N ; i++,d++) 
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
int rp_driverlist(rp_show iterator) ;

@ Add Device.
@<Exported Functions@>+=
int 
rp_adddevice(char *name, char *description,char *driver,char *driver_data)
  {
  int dev_handle,drv_handle ;
  Rp_Device *d ;
  Rp_Driver *drv ;

  trak_init() ;

  drv_handle = rp_getdriver(driver) ;
  if (drv_handle == NIL) 
    {
    printf("driver %s not found\n",driver) ;
    return NIL ;
    }
  drv = rp_drivers + drv_handle ;

  dev_handle = rp_getdevice(name) ;
  if (dev_handle == NIL) 
    {
    for (dev_handle = 0 ; dev_handle < RP_DRIVER_N ; dev_handle++) 
      {
      d = rp_devices + dev_handle ;
      if (!strlen(d->name)) 
        {
	d = rp_devices + dev_handle ;
	bzero(d,sizeof(Rp_Device)) ;
	strncpy(d->name,name,TRAK_NAME_LEN) ;
	strncpy(d->description,description,TRAK_DATA_LEN) ;
	d->driver = drv_handle ;
	d->head = NIL ;
	
	d->link = drv->head ;
	drv->head = dev_handle ;
	break ;
        }
      }
    }

  d = rp_devices + dev_handle ;
  strncpy(d->name,name,TRAK_NAME_LEN) ;
  strncpy(d->description,description,TRAK_DATA_LEN) ;
  d->description[TRAK_DATA_LEN] = 0 ;
  strncpy(d->driver_data,driver_data,TRAK_DATA_LEN) ;
  d->driver_data[TRAK_DATA_LEN] = 0 ;
  return dev_handle ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_adddevice(char *name, char *description,char *driver,char *driver_data) ;

@ Get Device.
@<Exported Functions@>+=
int 
rp_getdevice(char *name)
  {
  Rp_Device *dev ;
  int i ;

  trak_init() ;

  for (dev = rp_devices, i=0 ; i < RP_DRIVER_N ; i++,dev++) 
    {
    if (!strcmp(name,dev->name)) 
      return i ;
    }
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_getdevice(char *name) ;

@ Read Driver
@<Exported Functions@>+=
int 
rp_readdriver(int handle, Rp_Driver *driver)
  {
  Rp_Driver *drv ;

  if (handle < 0 || handle >= RP_DRIVER_N) 
    return NIL ;

  trak_init() ;

  drv = rp_drivers + handle ;
  *driver = *drv ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_readdriver(int handle, Rp_Driver *driver) ;

@ Read Device.
@<Exported Functions@>+=
int 
rp_readdevice(int handle, Rp_Device *device) 
  {
  Rp_Device *dev ;

  if (handle < 0 || handle >= RP_DRIVER_N) 
    return NIL ;

  trak_init() ;

  dev = rp_devices + handle ;
  *device = *dev ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_readdevice(int handle, Rp_Device *device) ;

@ Pointer.
@<Exported Functions@>+=
int 
rp_pointer(Rp_Record *rp) 
  {
  trak_init() ;

  return rp - rp_points ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_pointer(Rp_Record *rp) ;

@ Record Get.
@<Exported Functions@>+=
Rp_Record *
rp_record_get(int rp_handle)
  {
  trak_init() ;

  return rp_points + rp_handle ;
  }

@ Proto.
@<Exported Prototypes@>+=
Rp_Record *rp_record_get(int rp_handle) ;

@ Device List.
@<Exported Functions@>+=
int 
rp_devicelist(char *driver,rp_show iterator)
  {
  Rp_Device *dev ;
  Rp_Driver *drv ;
  int drv_handle ;
  int count ;

  trak_init() ;

  drv_handle = rp_getdriver(driver) ;
  if (drv_handle == NIL) 
    {
    return NIL ;
    }
  drv = rp_drivers + drv_handle ;
  for (count=0,dev = (drv->head != NIL) ? rp_devices + drv->head : NULL ;
       dev != NULL ;
       dev = (dev->link != NIL) ? rp_devices + dev->link : NULL) 
    {
    iterator(dev - rp_devices) ;
    count++ ;
    }
  return count ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_devicelist(char *driver, rp_show iterator) ;

@ Scan All.
@<Exported Functions@>+=
int 
rp_scanall(rp_show iterator)
  {
  Rp_Record *r ;
  int i,count ;

  trak_init() ;

  for (count=i=0,r=rp_points ; i < RP_N ; i++,r++) 
    {
    if (strlen(r->name)) 
      {
      iterator(i) ;
      count++ ;
      }
    }
  return count ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_scanall(rp_show iterator) ;

@ OK.
@<Exported Functions@>+=
int 
rp_ok(int handle)
  {
  Rp_Record *r ;

  if ((handle < 0) || (handle > RP_N)) 
    return NIL ;

  trak_init() ;

  r = rp_points + handle ;
  if (!strlen(r->name)) 
    return NIL ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_ok(int handle) ;

@ Dump.
@<Exported Functions@>+=
int rp_dump(void)
  {
  Rp_Device *dev ;
  Rp_Driver *drv ;
  Rp_Record *rec ;
  int i;

  trak_init() ;
  
  printf("RP Drivers\n") ;
  for (i=0 ; i< RP_DRIVER_N ; i++) 
    {
    drv = rp_drivers + i ;
    if (!strlen(drv->name)) 
      break ;
    printf("[%d] %s (%s) head: %d\n",
	   i,
	   drv->name,
	   drv->description,
	   drv->head) ;
    }
  printf("RP Devices\n") ;
  for (i=0 ;i < RP_DRIVER_N ; i++) 
    {
    dev = rp_devices + i ;
    if (!strlen(dev->name)) 
      break ;
    printf("[%d] %s (%s) data:(%s) link: %d head: %d\n",
	   i,
	   dev->name,
	   dev->description,
	   dev->driver_data,
	   dev->link,
	   dev->head) ;
    }
  printf("RP Points\n") ;
  for (i=0 ;i < RP_N ; i++) 
    {
    rec = rp_points + i ;
    if (!strlen(rec->name)) 
      break ;
    printf("[%d] %s (%s) data: (%s)\n",
	   i,
	   rec->name,
	   rec->description,
	   rec->device_data) ;
    printf("\tvalue: %d (%d) link: %d trigger: %d\n",
	   rec->counter,
	   rec->set_value,
	   rec->link,
	   rec->trigger) ;

    }
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int rp_dump(void) ;
