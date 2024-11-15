@* Virtual.

@ There can be a number of virtual devices.
@<Globals@>+=
int virtual_device[VIRTUAL_DEVICE_N] ;
int virtual_device_count = 0 ;

@ The number is set to a constant.
@<Defines@>+=
#define VIRTUAL_DEVICE_N (32) 

@ An iterator assembles the array of virtual device handles.
@<Functions@>+=
static int 
each_virtual_device(int handle)
  {
  virtual_device[virtual_device_count++] = handle ;
  return 0 ;
  }

@ The function |virtual_init()| triggers the iterator.
@<Functions@>+=
static int 
virtual_init(void)
  {
  virtual_device_count = 0 ;
  dp_devices("virtual",each_virtual_device) ;
  return 0 ;
  }
    
@ This is inserted into the engine initializations.
@<Inits@>+=
  virtual_init() ;

@ For each point we run the engine on changes.
@<Functions@>+=
static int 
each_virtual_point(Dp_Record *dp)
  {
  if(dp->value==2)
    dp->set_value = 0 ;

  if(dp->set_value != dp->value)
    dp_eval(dp,dp->set_value) ;
   
  return 0 ;
  }

@ The function |virtual_task()| starts the iterator for each device.
@<Functions@>+=
static void 
virtual_task(void)
  {
  int i ;

  for(i=0 ; i<virtual_device_count ; i++)
    dp_ioscan(virtual_device[i],each_virtual_point) ;
  }

@ We want |virtual_task()| to be called by the realtime task.
@<Tasks@>+=
virtual_task() ;

