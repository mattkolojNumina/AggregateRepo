%
%   ciftrak.w -- CIF external IO application for Trak 
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
\def\title{CIF Application}

\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This program connects Hilscher CIF IO to the Trak engine.

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
\centerline{\copyright 2019 Numina Systems Corporation.  
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
    cif_task() ;
    @<Realtime Yield@>@;
    }  
  }

@ Includes. 
The program needs the Trak3 library.
@<Includes@>+=
#include <stdio.h>
#include <time.h>
#include <sched.h>

#include <rds_trak.h>
#include <rds_trn.h>

@* RT.

@ Timing.
@<Globals@>+=
struct timespec rt_clock ;

@ Realtime parameters.
@<Defines@>+=
#define PERIOD 2500000
#define PRIORITY 88
#define NSEC_PER_SEC 1000000000


@ Initialize.
@<Initialize@>+=
  {
  struct sched_param rt_param ;
  int err ;

  rt_param.sched_priority = PRIORITY ;
  err = sched_setscheduler(0,SCHED_FIFO,&rt_param) ;
  if(err<0)
    {
    perror("Sched setscheduler fails") ;
    fprintf(stderr,"err %d cannot set scheduler to priority %d",
            err,PRIORITY) ;
    exit(1) ;
    }
  
  clock_gettime(0,&rt_clock) ;
  rt_clock.tv_nsec += PERIOD ;
  rt_clock_wrap(&rt_clock) ;

  trn_register("ciftrak") ;
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
       + (to.tv_nsec - from.tv_nsec) ;

  diff /= 1000 ;
  return diff ;
  }

@ Proto.
@<Prototypes@>+=
int rt_clock_diff(struct timespec from, struct timespec to) ;


@ Task.  
@<Functions@>=
void 
cif_task(void)
  {
  @<Check for run/stop@>@;
  if(run)
    {
    cif_scan() ;
    }
  }

@ Proto.
@<Prototypes@>+=
void cif_task(void) ;

@ Run/Stop.  We keep run flag.
@<Globals@>+=
int run = 0 ;

@ If trak goes to run, we initialize.
@<Check for run/stop@>+=
  {
  if((!run) && (trak_test()))
    {
    run = 1 ;
    cif_init() ;
    }
  if((run) && (!trak_test()))
    {
    run = 0 ;
    cif_stop() ;
    }
  }

@* Data Structures.

@ Points.
@<Structures@>+=
struct point_struct
  {
  Dp_Record *dp ;
  int offset ;
  int bit ;
  int is_input ;
  } ;

@ These are statically allocated.
@<Globals@>+=
struct point_struct points[POINT_N] ;

@ There is a fixed maximum number.
@<Defines@>+=
#define POINT_N (2000) 

@ We keep track of how many are active.
@<Globals@>+=
int point_n = 0 ;

@ We use arrays to store the working inputs and outputs.
@<Globals@>+=
unsigned char cif_i[IO_N] ;
unsigned char cif_o[IO_N] ;

@ There are a fixed number.
@<Defines@>+=
#define IO_N (128)

@ We keep track of how many are in use.
@<Globals@>+=
int cif_i_n = 0 ;
int cif_o_n = 0 ;

@* Init.
@<Functions@>+=
void
cif_init(void)
  {
  Inform("cif init") ;

  @<Clear the points@>@;
  @<Traverse the devices@>@;

  cif = cif_connect() ;
  }

@ Proto.
@<Prototypes@>+=
void cif_init(void) ;

@ We keep the CIF connection in a handle.
@<Globals@>+=
int cif = -1 ;

@ CIF connect
@<Functions@>+=
int
cif_connect(void)
  {
  if(cif<=0)
    cif = open("/dev/cif0",O_RDWR) ;
  if(cif<=0)
    {
    Alert("cannot connect to cif0") ;
    sleep(10) ;
    }

  return cif ;
  }

@ Proto.
@<Prototypes@>+=
int
cif_connect(void) ;

@ Disconnect.
@<Functions@>+=
void
cif_disconnect(void)
  {
  if(cif>=0)
    close(cif) ;
  cif = -1 ;
  }

@ Proto.
@<Prototypes@>+=
void cif_disconnect(void) ;

@ Stop.
@<Functions@>+=
void
cif_stop(void)
  {
  cif_disconnect() ;

  Inform("cif stop") ;  
  }

@ Proto.
@<Prototypes@>+=
void cif_stop(void) ;

@ Clear the points by resetting the point count.
@<Clear the points@>+=
  {
  point_n = 0 ;
  cif_i_n = 0 ;
  cif_o_n = 0 ;
  }

@ Traverse the devices using an iterator.
@<Traverse the devices@>+=
  {
  Inform("devices") ;
  dp_devices("io:cif",each_device) ;
  Inform("cif_i_n %d cif_o_n %d",cif_i_n,cif_o_n) ;
  }

@ Each device.
@<Functions@>+=
int
each_device(int handle)
  {
  Inform("device %d",handle) ;
  dp_ioscan(handle,each_point) ;
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int each_device(int handle) ;

@ Each point.
@<Functions@>+=
int
each_point(Dp_Record *dp)
  {
  char dir ;
  int offset, bit ;
  int n ;

  n = sscanf(dp->io_data,"%c:%d/%d",&dir,&offset,&bit) ;
  if(n==3)
    {
    Inform("  %s %s %c %d %d",dp->name,dp->io_data,
           dir,offset,bit) ;
    points[point_n].dp       = dp ;
    points[point_n].offset   = offset ;
    points[point_n].bit      = bit ;
    points[point_n].is_input = (dir=='I') || (dir=='i') ;

    if(points[point_n].is_input)
      {
      if(cif_i_n <= points[point_n].offset)
        cif_i_n = points[point_n].offset + 1 ;
      }
    else
      {
      if(cif_o_n <= points[point_n].offset)
        cif_o_n = points[point_n].offset + 1 ;
      }  
    point_n++ ;
    }
  else
    Alert("%s cannot parse io_data %s",dp->name,dp->io_data) ;

  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int each_point(Dp_Record *dp) ;

@* Scan.
@<Functions@>+=
void
cif_scan(void)
  {
  @<Gather outputs@>@;
  @<Write outputs@>@;
  @<Read inputs@>@;
  @<Process exports@>@;
  }

@ Proto.
@<Prototypes@>+=
void cif_scan(void) ;

@ Gather outputs.
@<Gather outputs@>+=
  {
  int i ;

  for(i=0 ; i<cif_o_n ; i++)
    cif_o[i]=0;

  for(i=0 ; i<point_n ; i++)
    if(points[i].is_input==0)
      {
      if(points[i].dp->set_value)
        cif_o[points[i].offset] |= 1 << points[i].bit ; 
      }
  }

@ Write outputs.
@<Write outputs@>+=
  {
  cif_connect() ;
  if(cif>=0)
    {
    int n ;
    n = write(cif,cif_o,cif_o_n) ;
    if(n!=cif_o_n)
      {
      Alert("write error %d",n) ;
      sleep(10) ;
      cif_disconnect() ;
      }
    }
  }

@ Read inputs.
@<Read inputs@>+=
  {
  cif_connect() ;
  if(cif>=0)
    {
    int n ;
    n = read(cif,cif_i,cif_i_n) ;
    if(n!=cif_i_n)
      {
      Alert("read error %d",n) ;
      sleep(10) ;
      cif_disconnect() ;
      }
    }
  }

@ Process exports.
@<Process exports@>+=
  {
  int i ;

  for(i=0 ; i<point_n ; i++)
    if(points[i].is_input==1)
      points[i].dp->export = (cif_i[points[i].offset] >> points[i].bit) & 1 ;
    else
      points[i].dp->export = (cif_o[points[i].offset] >> points[i].bit) & 1 ;
  }

@* Index.

