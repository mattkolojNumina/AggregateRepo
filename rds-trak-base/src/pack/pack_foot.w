%
%   pack_foot.w -- release box from footswitch 
%
%   Author: Mark Woodworth 
%
%   History:
%      2024-01-19 -MRW- init for Orgill pack station 
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
\def\title{Pack Show}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Update a runtime record with the tracked LPN when the box is released to the
gravity section.

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
\centerline{Control Revision: $ $Revision: 1.126 $ $}
\centerline{Control Date: $ $Date: 2017/05/29 18:24:08 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program updates a runtime record with the tracked LPN when the carton is
released to the gravity work zone.

@c
static char rcsid[] = "$Id: pack_foot.w,v 1.126 2017/05/29 18:24:08 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int 
main( int argc, char *argv[] ) 
  {
  char runtime[BUF_LEN+1] ;
  char zone[BUF_LEN+1] ;
  int handle ;
  int box ;
  int seq ;
  char lpn[BUF_LEN+1] ;

  @<initialize@>@;
  @<process carton@>@;

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
#define BUF_LEN         32  
#define BUF_LRG        128
#define MSG_LEN        256  // length of longer message strings
#define LABEL_LEN     1024  // length of product-label buffer
#define SLEEP_DURATION  50
#define TIMEOUT       2000
#define MIN_DIM         10
#define DIM_QUALITY     10

@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LRG + 1 ];

@ Initialization.
@<initialize@>+=
  {
  if(argc<2)
    {
    fprintf(stderr,"usage: %s <area>",argv[0]) ;
    exit(1) ;
    }
  strncpy(area,argv[1],BUF_LEN) ;
  area[BUF_LEN] = '\0' ;

  sprintf( name, "%sFoot", area );
  trn_register( name );
  }

@ Process the carton.
@<process carton@>=
  {
  struct timeval start_time;

  @<begin processing@>@;
  @<handle footswitch@>@;
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
  Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
  }

@ Handle footswitch.
@<handle footswitch@>+=
  {
  int err ;

  Inform("foot switch pressed") ;

  runtime[0] = '\0' ;
  zone[0] = '\0' ;
  box = 0 ;
  seq = 0 ;
  strcpy(lpn,"?") ;

  err = sql_query("SELECT value FROM runtime "
                  "WHERE name = '%sLPN' ",area) ;
  if(!err)
    if(sql_rowcount()>0)
      if(sql_get(0,0))
        strncpy(runtime,sql_get(0,0),BUF_LEN) ;
  runtime[BUF_LEN] = '\0' ;
  Inform("runtime [%s]",runtime) ;

  if(runtime[0]=='\0')
    {
    strncpy(zone,util_get_control(name,"zone",""),BUF_LEN) ;
    zone[BUF_LEN] = '\0' ;
    Inform("zone [%s]",zone) ;
    if(zone[0]!='\0')
      {
      handle = dp_handle(zone) ;
      Inform("handle %d",handle) ;
      if(handle>=0)
        {
        dp_registerget(handle,0,&box) ;
        Inform("box %d",box) ;
        if(box>0)
          {
          seq = util_box_get_int(box,"seq") ;
          Inform("seq %d",seq) ;
          if(seq>0)
            {
            strncpy(lpn,util_carton_get(area,seq,"lpn"),BUF_LEN) ;
            lpn[BUF_LEN] = '\0' ;
            }
          else
            Inform("sequence not valid") ;
          }

        Inform("write lpn [%s]",lpn) ;
        err = sql_query("UPDATE runtime SET value = '%s' "
                        "WHERE name='%sLPN' ",
                        lpn,area) ;
        if(err)
          Alert("SQL error %d update runtime",err) ;

        Inform("release zone [%s]",zone) ;
        util_zone_release(name) ;        
        }
      else
        Alert("zone [%s] not in Trak",zone) ;
      }
    else
      {
      Alert("cannot find zone") ;
      }
    }
  else
    {
    Trace("runtime not empty, do not release") ;
    }
  }


@* Index.
