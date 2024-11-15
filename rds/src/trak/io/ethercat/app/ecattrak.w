%
%   ecattrak.w -- EtherCAT external IO application for Trak 
%
%   Author: Mark Olson & Mark Woodworth
%           
%   History:
%      8/15/2000 --- Initial layout by Mark Olson
%
%
%
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Systems Corporation.  
%
%
%
%
%
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\qquad\item{$\bullet$}}

% --- title ---
%
\def\title{EtherCAT Application}

\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This program connects EtherCAT IO to the Trak engine.

\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.  
It must not be disclosed to any person
who does not have a current signed non-disclosure form on file with Numina
Systems Corporation.  
It must only be disseminated on a need-to-know basis.
It must be stored in a secure location. 
It must not be left out unattended.  
It must not be copied.  
It must be destroyed by burning or shredding. 
\smallskip
}}
\bigskip

\centerline{Author: Mark Olson \& Mark Woodworth}
\centerline{Revision Date: \today}
\centerline{RCS Date $ $Date: 2002/04/22 13:01:17 $ $}
}

%
\def\botofcontents{\vfill
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}

% --- overview ---
%
@* Overview. 
@c
@<Defines@>@;
@<Includes@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int
main(int argc, char *argv[])
  {
  @<Initialize@>@; 

  while(1)
    {
    ethercat_task() ;
    @<Realtime Yield@>@;    
    }  
  }

@ Includes. 
The program needs the Trak3 library.
@<Includes@>+=
#include <stdio.h>
#include <time.h>
#include <sched.h>

#include <ecrt.h>

#include <rds_trak.h>
#include <rds_trn.h>

@ Task.  
@<Functions@>=
void 
ethercat_task(void)
  {
  @<Check for run/stop@>@;
  if(run && !frozen)
    {
    ethercat_scan() ;
    }
  }

@ Proto.
@<Prototypes@>+=
void ethercat_task(void) ;

@ Run/Stop.  We keep run flag.
@<Globals@>+=
int run = 0 ;
int frozen = 0 ;
int freezes = 0 ;
int tm_1ms_h = -1 ;
int last_ms = 0 ;

@ If trak goes to run, we initialize.
@<Check for run/stop@>+=
  {
  if((!run) && (trak_test()))
    {
    run = 1 ;
    ethercat_init() ;
    }
  if((run) && (!trak_test()))
    {
    run = 0 ;
    ethercat_stop() ;
    }
  if((run) && (trak_test()))
    {
    if(tm_1ms_h<0)
      {
      tm_1ms_h = dp_handle("tm_1ms") ;
      Inform("tm_1ms %d",tm_1ms_h) ;
      }
    if(tm_1ms_h>=0)
      {
      int ms = dp_counter(tm_1ms_h) ;
      if(last_ms != ms)
        {
        freezes = 0 ;
        frozen  = 0 ;
        }
      else
        {
        freezes++ ;
        if(freezes>10)
          {
          if(!frozen)
            Alert("trak frozen") ;
          frozen = 1 ;
          }
        }
      last_ms = ms ;
      } 
    }
  }

@* Data Structures.

@ Device Structure.
@<Structures@>+=
struct device_struct
  {
  int handle ;
  int direction ;
  int pdo ;
  int offset ;
  } ;

@ We allocate a fixed number of devices.
@<Defines@>+=
#define DEV_N (128) 

@ These are statically allocated.
@<Globals@>+=
struct device_struct device[DEV_N] ;

@ We keep a count of the number allocated.
@<Globals@>+=
int device_n = 0 ;

@ Points are also described by a structure.
@<Structures@>+=
struct point_struct
  {
  int direction ;
  int handle ;
  int *offset ;
  int bit ;
  int was ;
  } ;

@ We allocate a fixed number of points.
@<Defines@>+=
#define POINT_N (1028)

@ The points are statically allocated.
@<Globals@>+=
static struct point_struct point[POINT_N] ;

@ We keep track of how many are active.
@<Globals@>+=
int point_n = 0 ;

@ PDOs are stored in an array.
@<Globals@>+=
ec_pdo_entry_reg_t pdo[PDO_N] ;

@ We allocate a fixed number of PDOs.
@<Defines@>+=
#define PDO_N (128)

@ We keep trak of the active number.
@<Globals@>+=
int pdo_n = 0 ;
 
@* Init.
@<Functions@>+=
void
ethercat_init(void)
  {
  Inform("init") ;
  @<Clear local data@>@; 
  @<Traverse the devices@>@;
  @<Configure EtherCAT@>@;
  }

@ Proto.
@<Prototypes@>+=
void ethercat_init(void) ;

@ Stop.
@<Functions@>+=
void
ethercat_stop(void)
  {
  if(master)
    {
    ecrt_release_master(master) ;
    master = NULL ;
    Inform("release") ;
    }
  }

@ Proto.
@<Prototypes@>+=
void ethercat_stop(void) ;

@ Clear local data.
@<Clear local data@>+=
  {
  device_n = 0 ;
  point_n  = 0 ;
  pdo_n    = 0 ;

  pdo[pdo_n].alias = 0 ;
  pdo[pdo_n].position = 0 ;
  pdo[pdo_n].vendor_id = 0 ;
  pdo[pdo_n].product_code = 0 ;
  pdo[pdo_n].index = 0 ;
  pdo[pdo_n].subindex = 0 ;
  pdo[pdo_n].offset = NULL ;
  pdo[pdo_n].bit_position = NULL ;
  }

@ Traverse the devices.
@<Traverse the devices@>+=
  {
  dp_devices("io:ethercat",ethercat_each_device) ;
  }

@ Each device.
@<Functions@>+=
int
ethercat_each_device(int handle)
  {
  Dp_Device device ;

  dp_readdevice(handle,&device) ;
  
  add_device(handle,device.device_data) ;

  dp_ioscan(handle,ethercat_each_point) ; 

  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int ethercat_each_device(int handle) ;

@ Add Device.
@<Functions@>+=
void
add_device(int handle, char *data)
  {
  unsigned int direction ;
  unsigned int position ;
  unsigned int vendor_id ;
  unsigned int product_code ;
  unsigned int index ;
  unsigned int subindex ;
  int n ;

  n = sscanf(data,"%x:%x:%x:%x:%x:%x",
             &direction,
             &position,
             &vendor_id,
             &product_code,
             &index,
             &subindex) ;
  Inform("device %d %x %x %x %x %x %x",
         n,direction,position,vendor_id,product_code,index,subindex) ;

  device[device_n].handle = handle ;
  device[device_n].direction = direction ;
  device[device_n].pdo = pdo_n ;
    pdo[pdo_n].alias = 0 ;
    pdo[pdo_n].position = position ;
    pdo[pdo_n].vendor_id = vendor_id ;
    pdo[pdo_n].product_code = product_code ;
    pdo[pdo_n].index = index ;
    pdo[pdo_n].subindex = subindex ;
    pdo[pdo_n].offset = &(device[device_n].offset) ;

  pdo_n++ ;
  pdo[pdo_n].alias = 0 ;
  pdo[pdo_n].position = 0 ;
  pdo[pdo_n].vendor_id = 0 ;
  pdo[pdo_n].product_code = 0 ;
  pdo[pdo_n].index = 0 ;
  pdo[pdo_n].subindex = 0 ;
  pdo[pdo_n].offset = NULL ;
  pdo[pdo_n].bit_position = NULL ;

  device_n++ ;
  }

@ Proto.
@<Prototypes@>+=
void add_device(int handle, char *data) ;

@ Find an index from a handle.
@<Functions@>+=
int
find_device(int handle)
  {
  int i ;
  for(i=0 ; i<device_n; i++)
    if(device[i].handle==handle)
      return i ;
  return -1 ;
  }

@ Proto.
@<Prototypes@>+=
int find_device(int handle) ;

@ Each point.
@<Functions@>+=
int
ethercat_each_point(Dp_Record *dp)
  {
  int dev ;

  dev = find_device(dp->device) ;
  if(dev<0)
    return dev ;

  Inform("  dp %s dev %d",dp->name,dev) ;

  point[point_n].direction = device[dev].direction ;
  point[point_n].handle    = dp_record(dp) ;
  point[point_n].offset    = &(device[dev].offset) ;
  point[point_n].bit       = atoi(dp->io_data) ;
  point[point_n].was       = -1 ;

  point_n++ ;

  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int ethercat_each_point(Dp_Record *dp) ;

@ Configure EtherCAT.
@<Configure EtherCAT@>+=
  {
  int err ;

  err = 0 ; 
  @<Request master@>@;
  if(!err)
    {
    @<Create domain@>@;
    if(!err)
      {
      @<Register PDOs@>@;
      if(!err)
        {
        @<Activate master@>@;
        if(!err) 
          {
          Inform("configured") ;   
          }
        }
      }
    if(err)
      @<Release master@>@;
    }
  }

@ Request the master.
@<Request master@>+=
  {
  master = ecrt_request_master(0) ;
  if(master)
    {
    Inform("master request succeeds") ;
    // ecrt_master_callbacks(master,ethercat_send,ethercat_recv) ;
    }
  else
    {
    Alert("master request fails") ;
    err = -1 ;
    }
  }

@ Release master.
@<Release master@>+=
  {
  if(master)
    {
    ecrt_release_master(master) ;
    }
  }

@ The master handle.
@<Globals@>+=
ec_master_t *master = NULL ;

@ EtherCAT send callback.
@<Functions@>+=
void
ethercat_send(void *data) 
  {
  }

@ Proto.
@<Prototypes@>+=
void ethercat_send(void *data) ;

@ EtherCAT receive callback.
@<Functions@>+=
void
ethercat_recv(void *data) 
  {
  }

@ Proto.
@<Prototypes@>+=
void ethercat_recv(void *data) ;

@ Create the domain.
@<Create domain@>+=
  {
  domain = ecrt_master_create_domain(master) ;
  if(domain)
    {
    Inform("domain create succeeds") ;
    }
  else
    {
    err = -2 ;
    Alert("domain create fails") ;
    }
  }

@ The domain handle.
@<Globals@>+=
ec_domain_t *domain      = NULL ;
uint8_t     *domain_data = NULL ;

@ Register PDOs.
@<Register PDOs@>+=
  {
  if(!ecrt_domain_reg_pdo_entry_list(domain,pdo))
    {
    Inform("pdo registration succeeds") ;
    }
  else
    {
    err = -3 ;
    Alert("pdo registration fails") ;
    }
  }

@ Activate the master.
@<Activate master@>+=
  {
  if(!ecrt_master_activate(master))
    {
    domain_data = ecrt_domain_data(domain) ;
    Inform("master activation succeeds") ;
    }
  else
    {
    err = -4 ;
    Alert("master activation fails") ;
    }
  }

@* Scan.
@<Functions@>+=
void
ethercat_scan(void)
  {
  ecrt_master_receive(master) ;
  ecrt_domain_process(domain) ;
  @<Process points@>@;
  ecrt_domain_queue(domain) ;
  ecrt_master_send(master) ;
  }

@ Proto.
@<Prototypes@>+=
void ethercat_scan(void) ;

@ Process points.
@<Process points@>+=
  {
  int p ;
  
  for(p=0 ; p<point_n ; p++)
    {
#if 1
    switch(point[p].direction)
      {
      case 0: @<Process output@>@;          break ;
      case 1: @<Process input@>@;           break ;
      case 2: @<Process register output@>@; break ;
      case 3: @<Process register input@>@;  break ; 
      }
#else
    if(point[p].direction)
      @<Process input@>@;
    else
      @<Process output@>@;
#endif
    }
  }

@ Process input.
@<Process input@>+=
  {
  Dp_Record *record ;
  int value ;

  record = dp_pointer(point[p].handle) ;
//  value = ((EC_READ_U8(domain_data + *(point[p].offset)))>>point[p].bit) & 1 ;
  value = ((EC_READ_U8(domain_data + *(point[p].offset)+ point[p].bit/8))>>point[p].bit%8) & 1 ;
  
  record->export = value ;
  }

@ Process output.
@<Process output@>+=
  {
  Dp_Record *record ;
  int value ;
  int mask ;

  record = dp_pointer(point[p].handle) ;
  value = record->set_value ? 1 : 0 ;

//  mask = EC_READ_U8(domain_data + *(point[p].offset)) ;
  mask = EC_READ_U8(domain_data + *(point[p].offset) + point[p].bit/8) ;
  if(value)
    mask |=  (1 << point[p].bit) ;
  else
    mask &= ~(1 << point[p].bit) ;
//  EC_WRITE_U8(domain_data + *(point[p].offset), mask) ;
  EC_WRITE_U8(domain_data + *(point[p].offset) + point[p].bit/8, mask) ;

  record->export = value ;
  }

@ Process register input.
@<Process register input@>+=
  {
  Dp_Record *record ;
  int value = 0  ;

  record = dp_pointer(point[p].handle) ;
  switch(point[p].bit)
    {
    case 1: value = EC_READ_U8 (domain_data + *(point[p].offset)) ; break ;
    case 2: value = EC_READ_U16(domain_data + *(point[p].offset)) ; break ;
    case 4: value = EC_READ_U32(domain_data + *(point[p].offset)) ; break ;
    }

  record->data[0] = value ; 
  }

@ Process register output.
@<Process register output@>+=
  {
  Dp_Record *record ;
  int value = 0  ;

  record = dp_pointer(point[p].handle) ;
  value = record->data[0] ;
  switch(point[p].bit)
    {
    case 1: EC_WRITE_U8 (domain_data + *(point[p].offset),value) ; break ;
    case 2: EC_WRITE_U16(domain_data + *(point[p].offset),value) ; break ;
    case 4: EC_WRITE_U32(domain_data + *(point[p].offset),value) ; break ;
    }
  }

@* RT.

@ Timing.
@<Globals@>+=
struct timespec rt_clock ;

@ Period.
@<Defines@>+=
#define PERIOD 500000 
#define PRIORITY 89 
#define NSEC_PER_SEC 1000000000  

@ Start preempt.
@<Initialize@>+=
  {
  struct sched_param rt_param ;
  int err ;

  rt_param.sched_priority = PRIORITY ;
  err = sched_setscheduler(0,SCHED_FIFO,&rt_param) ;
  if(err<0)
    {
    perror("sched setcheduler fails") ;
    fprintf(stderr,"err %d: cannot set scheduler to priority %d",
            err,PRIORITY) ;
    exit(1) ;
    } 

  clock_gettime(0,&rt_clock) ;
  rt_clock.tv_nsec += PERIOD ; 
  rt_clock_wrap(&rt_clock) ;

  trn_register("ecattrak") ;
  Inform("init") ;
  }

@ Yield.
@<Realtime Yield@>+=
  {
  clock_nanosleep(0, TIMER_ABSTIME, &rt_clock, NULL) ;
  rt_clock.tv_nsec += PERIOD ;
  rt_clock_wrap(&rt_clock) ;
  }

@ Clock wrap.
@<Functions@>+=
static inline void
rt_clock_wrap(struct timespec *ts)
  {
  while(ts->tv_nsec >= NSEC_PER_SEC)
    {
    ts->tv_nsec -= NSEC_PER_SEC ;
    ts->tv_sec  += 1 ;
    }
  }

@ Proto.
@<Prototypes@>+=
static inline void rt_clock_wrap(struct timespec *ts) ;

@ Clock diff.
@<Functions@>+=
int
rt_clock_diff(struct timespec from, struct timespec to)
  {
  int diff = 0 ;

  diff = NSEC_PER_SEC * (to.tv_sec-from.tv_sec)
       + (to.tv_nsec-from.tv_nsec) ;

  diff /= 1000 ;
  return diff ;
  }

@ Proto.
@<Prototypes@>+=
int rt_clock_diff(struct timespec from, struct timespec to) ; 

@* Index.

