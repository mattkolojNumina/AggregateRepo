@* Register.

@ There can be a number of register devices.
@<Globals@>+=
int register_device[REGISTER_DEVICE_N] ;
int register_device_count = 0 ;

@ The number is set to a constant.
@<Defines@>+=
#define REGISTER_DEVICE_N (32) 

@ An iterator assembles the array of virtual device handles.
@<Functions@>+=
static int 
each_register_device(int handle)
  {
  register_device[register_device_count++] = handle ;
  return 0 ;
  }

@ The function |register_init()| triggers the iterator.
@<Functions@>+=
static int 
register_init(void)
  {
  register_device_count = 0 ;
  rp_devicelist("register",each_register_device) ;
  return 0 ;
  }
    
@ This is inserted into the engine initializations.
@<Inits@>+=
  register_init() ;

@ For each point we run the engine on changes.
@<Functions@>+=
static int 
each_register_point(Rp_Record *rp)
  {
  if(rp->set_value != rp->counter)
    {
    rp_eval(rp,rp->set_value) ;
    }
  return 0 ;
  }

@ The function |register_task()| starts the iterator for each device.
@<Functions@>+=
static void 
register_task(void)
  {
  int i ;

  for(i=0 ; i<register_device_count ; i++)
    rp_ioscan(register_device[i],each_register_point) ;
  }

@ We want |register_task()| to be called by the realtime task.
@<Tasks@>+=
  register_task() ;

