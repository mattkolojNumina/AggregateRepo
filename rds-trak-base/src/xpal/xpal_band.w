%
%   xpal_band.w -- release into bander 
%
%   Author: Mark Woodworth 
%
%   History:
%      2024-01-19 -MRW- init for Orgill East bander
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
%     (C) Copyright 2024 Numina Group, Inc.  All Rights Reserved.
%
%
%

% --- helpful macros ---
\def\dot{\item{ $\bullet$}}
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}

% --- title block ---
\def\title{XPAL band}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program holds a box until ready to pass through the bander. 

% --- confidentiality statement ---
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

% --- author and version ---
\bigskip
\centerline{Author: Mark Woodworth}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.10 $ $}
\centerline{Control Date: $ $Date: 2017/05/03 15:22:34 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 Numina Group, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program holds a box to release into a bander.

@c
static char rcsid[] = "$Id: xpal_start.w,v 1.10 2017/05/03 15:22:34 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;

int 
main(int argc,char *argv[]) 
  {
  @<initialize@>@;
  @<process box@>@;

  exit( EXIT_SUCCESS );
  }

@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_trak.h>
#include <rds_ctr.h>
#include <rds_util.h>
#include <zonetrak.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // length of small statically allocated strings
#define BUF_LRG         128  // length of large statically allocated strings

@ Global variables.
@<Globals@>+=
int box ;
int seq ;
char area[ BUF_LEN + 1 ];
char name[ BUF_LRG + 1 ];
int bypass_h       = -1 ;
int numina_ready_h = -1 ;
int wake_up_h      = -1 ;
int bander_ready_h = -1 ;
int bander_fault_h = -1 ;
int bander_out_h   = -1 ;
int zone_after_h   = -1 ;

@ Initialization.
@<initialize@>+=
  {
  int first, last ;

  if (argc != 3) 
    {
    printf( "usage: %s <area> <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }

  strcpy(area,argv[1]) ;
  area[BUF_LEN]='\0' ;

  sprintf( name, "%sBand",area);
  trn_register( name );

  first = app_first_box(area) ;
  last  = app_first_box(area) ;

  box = atoi( argv[2] );

  bypass_h       = dp_handle("boint1020-1") ;
  numina_ready_h = dp_handle("boint1020-2") ;
  wake_up_h      = dp_handle("boint1020-3") ;

  bander_ready_h = dp_handle("biint1020-4") ;
  bander_fault_h = dp_handle("biint1020-5") ;
  bander_out_h   = dp_handle("biint1020-6") ;
 
  zone_after_h   = dp_handle("z1020g-1") ;
 
  Inform("%03d: out %d %d %d in %d %d %d",
         box,
         bypass_h,numina_ready_h,wake_up_h,
         bander_ready_h,bander_fault_h,bander_out_h) ;

  }

@ Process the box.
@<process box@>=
  {
  struct timeval start_time;

  @<begin processing@>@;
  @<handle box@>@;
  @<finish processing@>@;
  }

@ Begin processing: initialization, etc.
@<begin processing@>=
  {
  gettimeofday( &start_time, NULL );
  }


@ Finish processing.
@<finish processing@>=
  {
  Inform( "%03d: processing complete, took %.3f sec",
         box, util_get_elapsed( start_time ) );
  }

@ Handle the box.
@<handle box@>=
  {

  while(!ready() && in_place())
    usleep(10*1000L) ;

  dp_set(bypass_h,bypass()) ;

  if(in_place())
    {
    Trace("%03d: release",box) ;
    util_zone_release(name) ;
    }
  }

@ Ready.
@<Functions@>+=
int
ready(void)
  {
  // is zone after not available 
  if(dp_get(zone_after_h)!=0)
    {
    Inform("%03d: downstream not ready",box) ;
    dp_set(numina_ready_h,0) ;
    dp_set(wake_up_h,0) ;
    return 0 ;
    }
  
  // we are ready
  dp_set(numina_ready_h,1) ;
  dp_set(wake_up_h,1) ;
     
  // is bander faulted?
  if(dp_get(bander_fault_h))
    {
    Inform("%03d: bander faulted",box) ;
    sleep(10) ;
    return 0 ;
    }

  // is bander out of strap?   
  if(dp_get(bander_out_h))
    {
    Inform("%03d: bander out of strap",box) ;
    sleep(10) ;
    return 0 ;
    }

  // is bander not ready to receive?
  if(!dp_get(bander_ready_h))
    {
    Inform("%03d: bander not ready",box) ;
    return 0 ;
    }

  return 1 ;
  }

@ Proto.
@<Prototypes@>+=
int ready(void) ;

@ Bypass cartons.
@<Functions@>+=
int
bypass(void)
  {
  int err ;
  int tote = 0 ;
  int seq = 0 ;
  char lpn[BUF_LEN+1] ;

  lpn[0]='\0' ;
  
  seq = util_box_get_int( box, "seq" );
  if(seq>0)
    {
    err = sql_query("SELECT lpn FROM eastCartons "
                    "WHERE seq=%d ",seq) ;
    if(!err)
      if(sql_rowcount())
        if(sql_get(0,0))
          strncpy(lpn,sql_get(0,0),BUF_LEN) ;
    lpn[BUF_LEN]='\0' ;
    }
    
  if(lpn[0]=='T')
    tote=1 ;

  Inform("%03d: seq %d  lpn [%s] tote %d",
         box,seq,lpn,tote) ;

  return !tote ;
  }

@ In Place
@<Functions@>+=
int
in_place(void)
  {
  return box == get_zone_box() ;
  }

@ Proto.
@<Prototypes@>+=
int in_place(void) ;

@ Check current box.
@<Functions@>+=
int 
get_zone_box( void ) 
  {
  static int zone_dp = -1;
  int box;

  if (zone_dp < 0) 
    {
    char zone[ TRAK_NAME_LEN + 1 ];
    util_zone_get( name, zone );
    if (strlen( zone ) == 0) 
      {
      Alert( "no zone specified" );
      return BOX_NONE;
      }

    zone_dp = dp_handle( zone );
    if (zone_dp < 0) 
      {
      Alert( "unable to obtain dp for zone [%s]", zone );
      return BOX_NONE;
      }
    } 
  dp_registerget( zone_dp, REG_BOX, &box );
  return box;
  }

@ Prototype the function.
@<Prototypes@>+=
int get_zone_box( void );

@* Index.
