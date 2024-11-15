@* IO.

@ An IO point is a DP controlled by this class of drivers.
@<Structures@>+=
struct io_struct 
  {
  int used ;
  Dp_Record *dp ;
  } ;

@ These are statically allocated.
@<Globals@>+= 
static struct io_struct ios[IO_N];

@ The number is fixed.
@<Defines@>+=
#define IO_N (DP_N)

@ The count of allocated points.
@<Globals@>+=
static int io_n  ;

@ Any driver falls into the IO category if the name
begins with |extern:| or |io:|. 
@<Functions@>+=
static int 
is_io(Dp_Driver *test)
  {
  if(0==strncmp(test->name,"extern:",strlen("extern:")))
    return 1 ;
  if(0==strncmp(test->name,"io:",strlen("io:")))
    return 1 ;
  return 0 ;
  } 

@ Add a point.
@<Functions@>+=
static int 
each_io_point(Dp_Record *dp)
  {
  ios[io_n].used=1 ;
  ios[io_n].dp = dp ;
  io_n++ ;
  return 0 ;
  }

@ Iterate over points for a device.
@<Functions@>+=
static int 
each_io_device(int dev_h)
  {
  dp_ioscan(dev_h,each_io_point) ;
  return 0 ;
  }

@ Iterate over devices for a driver.
@<Functions@>+=
static int 
each_io_driver(int drv_h)
  {
  Dp_Driver drv;

  dp_readdriver(drv_h,&drv) ;
  if (is_io(&drv)) 
    {
    dp_devices(drv.name,each_io_device) ;
    }
  return 0 ;
  }

@ IO init is called at engine start.
@<Functions@>+=
static int 
io_init(void)
   {
   @<IO: initialize DPs@>@;
   @<IO: fill DPs@>@;
   return 0 ;
   }
    
@ Initialize the IO DPs.
@<IO: initialize DPs@>=
  {
  int i ;
  struct io_struct *next ;

  next = ios ;
  for (i=0 ; i < IO_N ; i++,next++) 
    next->used = 0 ;
  }

@ Traverse over drivers. 
@<IO: fill DPs@>=
  {
  io_n = 0 ;   
  dp_drivers(each_io_driver) ;
  }

@ Proto.
@<Prototypes@>+=
static int io_init(void) ;

@ This is inserted into the engine initializations.
@<Inits@>+=
   io_init() ;

@ The function |io_task()| processes all DPs in the IO list. 
@<Functions@>+=
static void 
io_task(void)
   {
   int i ;
   Dp_Record *dp ;
   struct io_struct *next ;

   next = ios ; 
   for (i=0 ; i < IO_N ; i++,next++)  
     {
     if (!next->used) 
       break ;
     dp = next->dp ;
     if (dp->export != dp->value) 
       dp_eval(dp,dp->export) ;
     }
   }

@ Proto.
@<Prototypes@>+=
static void io_task(void) ;

@ This is called periodically by the engine.
@<Tasks@>+=
   io_task() ;
