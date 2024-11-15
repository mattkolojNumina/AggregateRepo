%
%   engine.w -- Trak Engine 
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
\def\title{Trak3 Engine}

\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is the kernel module engine for the Trak3 system.

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
@<Utilities@>@;
@<Functions@>@;
@<Drivers@>@;
int
main(int argc, char *argv[])
  {
  @<Initialize@>@; 

  while(1)
    {
    @<Realtime Start@>@;
    engine_task() ;
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

@* Drivers.  
Each driver include file adds structures, globals, and functions,
and insert code into Tasks and Init.
@<Drivers@>+=
@i engine_virtual.i
@i engine_timer.i
@i engine_register.i
@i engine_io.i

@ Task.  
@<Functions@>=
void 
engine_task(void)
  {
  @<Check for run/stop@>@;
  if(run)
    {
    @<Tasks@>@;
    }
  }

@ Proto.
@<Prototypes@>+=
void engine_task(void) ;

@ Run/Stop.  We keep a local concept of run or stop, and look
for changes in the Trak system.
@<Globals@>+=
int run = 0 ;

@ If Trak goes to run we start, if it goes to stop we stop.
@<Check for run/stop@>=
   {
   if((!run) && (trak_test()))
      {
      run = 1 ;
      Trace("run") ;
      @<Go to Run@>@;
      }

   if((run) &&(!trak_test()))
      {
      run = 0 ;
      Trace("stop") ;
      @<Go to Stop@>@;
      }
   }

@ To run, we re-initialize all of the internal structures and
start the state machine.
@<Go to Run@>=
   {
   @<Inits@>@;
   @<Turn on  |"trak"| dp@>@;
   }

@ To stop, we complete safe shutdown processing and 
mark the device register.
@<Go to Stop@>=
   {
   @<Turn off |"trak"| dp@>@;
   }

@ After resetting the trak engine, a "trak" dp with the |"engine"| device and
driver are always created. It is our responsibility on start to turn on 
(and and "exec") the trak dp. On stop the last thing we do is to turn off the
dp. This allows startup/shutdown initialization code to be tied to the edges of
this bit to perform any actions we require.
@<Turn on  |"trak"| dp@>=
   {
   Dp_Record *r ;
   @<Locate |"trak"|@>@;
  
   dp_eval(r,1) ;
   }

@ Turning off |"trak"| is similar.
@<Turn off |"trak"| dp@>=
   {
   Dp_Record *r ;
   @<Locate |"trak"|@>@;
  
   dp_eval(r,0) ;
   }

@ Locate the dp.
@<Locate |"trak"|@>=
   {
   r = dp_pointer(0) ;
   }


@* RT.

@ Timing.
@<Globals@>+=
struct timespec rt_clock ;
struct timespec rt_start ;

@ Period.
@<Defines@>+=
#define PERIOD 500000 
#define PRIORITY 90
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
  clock_gettime(0,&rt_start) ;
  rt_clock.tv_nsec += PERIOD ; 
  rt_clock_wrap(&rt_clock) ;

  trn_register("engine") ;
  Inform("init") ;
  }

@ Start.
@<Realtime Start@>+=
  {
  clock_gettime(0,&rt_start) ;
  }

@ Yield.
@<Realtime Yield@>+=
  {
  struct timespec rt_now ;
  int diff ;
 
  clock_gettime(0,&rt_now) ;
  diff = rt_clock_diff(rt_start,rt_now) ;
  dp_registerset(0,1,diff) ;
 
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

@* Utilities.
@ A utility function converts strings to integers.
@<Utilities@>+=
int convert(char *text)
   {
   int x = 0 ;
   int base = 10 ;

   while(*text==' ')
      text++ ;

   if(*text=='0')
      {
      base = 8 ;
      text++ ;
      }

   if(*text=='x')
      {
      base = 16 ;
      text++ ;
      }

   while(*text != '\0')
      {
      x = x * base ;
      switch(*(text++))
         {
         case '0' : x +=  0 ; break ;
         case '1' : x +=  1 ; break ;
         case '2' : x +=  2 ; break ;
         case '3' : x +=  3 ; break ;
         case '4' : x +=  4 ; break ;
         case '5' : x +=  5 ; break ;
         case '6' : x +=  6 ; break ;
         case '7' : x +=  7 ; break ;
         case '8' : x +=  8 ; break ;
         case '9' : x +=  9 ; break ;
         case 'a' : x += 10 ; break ;
         case 'A' : x += 10 ; break ;
         case 'b' : x += 11 ; break ;
         case 'B' : x += 11 ; break ;
         case 'c' : x += 12 ; break ;
         case 'C' : x += 12 ; break ;
         case 'd' : x += 13 ; break ;
         case 'D' : x += 13 ; break ;
         case 'e' : x += 14 ; break ;
         case 'E' : x += 14 ; break ;
         case 'f' : x += 15 ; break ;
         case 'F' : x += 15 ; break ;
         default  :         ; break ;
         }
      }

   return x ;
   }

@ Prototype.
@<Prototypes@>+=
int convert(char *text) ;

@* Index.

