@* Timer.

@ Each timer point has a period and a current count.
@<Structures@>+=
struct timer_point_struct
  {
  int handle ;
  int count ;
  int period ;
  } ;

@ There is a fixed number of these.
@<Globals@>+=
static struct timer_point_struct timer_point[TIMER_POINT_N] ;
static int timer_point_count = 0 ;

@ This is a constant.
@<Defines@>+=
#define TIMER_POINT_N (250)

@ An iterator assembles the array of points.
@<Functions@>+=
static int 
each_timer_point(Dp_Record *dp)
  {
  timer_point[timer_point_count].handle = dp_record(dp) ;
  timer_point[timer_point_count].count = 0 ;
  timer_point[timer_point_count].period = convert(dp->io_data) ;
  timer_point_count++ ;
  return 0 ;
  }

@ An iterator assembles the array of virtual device handles.
It calls an iterator for each point.
@<Functions@>+=
static int 
each_timer_device(int handle)
  {
  dp_ioscan(handle,each_timer_point) ;
  return 0 ;
  }

@ The function |timer_init()| triggers the iterator.
@<Functions@>+=
static int 
timer_init(void)
  {
  timer_point_count = 0 ;
  dp_devices("time",each_timer_device) ;
  return 0 ;
  }
    
@ This is inserted into the engine initializations.
@<Inits@>+=
  timer_init() ;

@ The function |timer_task()| starts the iterator for each device.
@<Functions@>+=
static void 
timer_task(void)
  {
  Dp_Record *dp ;
  int i ;

  for(i=0 ; i<timer_point_count ; i++)
    {
    timer_point[i].count++ ;
    if(timer_point[i].count>=timer_point[i].period)
      {
      timer_point[i].count = 0 ;
      dp = dp_pointer(timer_point[i].handle) ;
      if(dp->value)
        dp->value = 0 ;
      else
        dp->value = 1 ; 
      dp_eval(dp,dp->value) ;
      }
    }
  }

@ We want |timer_task()| to be called by the realtime task.
@<Tasks@>+=
  timer_task() ;

