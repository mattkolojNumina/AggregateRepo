%
t   zone_route.w -- get scan, determine destination in zone route system 
%
%   Author: Mark Woodworth 
%
%   History:
%      2024-02-02 -MRW- init for Orgill
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
\def\title}
{Zone Route}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program applies business rules to interpret data captured from field
devices (scanners, scale, etc.).

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
\centerline{Control Revision: $ $Revision: 1.22 $ $}
\centerline{Control Date: $ $Date: 2024/03/29 15:19:09 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program applies business rules to interpret data captured from field
devices (scanners, scale, dim, etc.).

@c
static char rcsid[] = "$Id: zone_route.w,v 1.22 2024/03/29 15:19:09 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int 
main( int argc, char *argv[] ) 
  {
  int box;

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
#define BUF_LEN         32  // length of small strings 
#define BUF_LRG         64
#define MSG_LEN        256  // length of longer message strings
#define SLEEP_DURATION  50
#define TIMEOUT       2000

@ Global status variables.
@<Globals@>+=
char name[ BUF_LRG + 1];

@ Initialization.
@<initialize@>+=
  {
  if (argc != 3) 
    {
    printf( "usage: [%s] <route> <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }

  strncpy(name, argv[1], BUF_LEN) ;
  name[BUF_LEN] = '\0' ;
  trn_register( name );

  box = atoi( argv[2] );
  if (box<1 || box>999) 
    {
    Alert( "invalid box [%d]", box );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }
  }

@ Process the carton.
@<process carton@>=
  {
  struct timeval start_time;
  char barcode[ BUF_LEN + 1 ];
  int barcode_success;
  int cartonSeq ;
  int divert = 3 ;
  char zone[16+1] ;

  @<begin processing@>@;
  @<get barcode@>@;
  @<get carton@>@;
  @<get divert@>@;
  @<log@>@;
  @<finish processing@>@;
  }

@ Begin processing: initialization, etc.
@<begin processing@>=
  {
  gettimeofday( &start_time, NULL );

  strcpy( barcode, "" );
  barcode_success = FALSE ;
  cartonSeq = -1 ;
  zone[0] = '\0' ;
  }

@ Finish processing.
@<finish processing@>=
  {
  Inform( "%03d: processing complete for carton, took %.3f sec",
         box, util_get_elapsed( start_time ) );
  util_zone_release( name );
  }

@ Get the barcode for the carton.  Apply business rules to the scan result
to extract a single valid identifier barcode. If zero or multiple valid
barcodes are found, this is an error.
@<get barcode@>=
  {
  char description[ MSG_LEN + 1 ];
  char dev_name[ BUF_LEN + 1 ];
  char dev_msg[ MSG_LEN + 1 ];

  barcode_success = FALSE;
  description[0] = '\0' ;

  strncpy(dev_name,util_get_control(name,"scan",""),BUF_LEN);
  dev_name[ BUF_LEN ] = '\0';
  strcpy( dev_msg, "" );

  if (dev_name == NULL || strlen( dev_name ) == 0)
    {
    Alert( "no scanner configured" );
    strcpy(description,"no scanner configured") ;
    }
  else 
    {
    util_poll_for_msg( box, dev_name, dev_msg );
    Inform( "%03d: [%s] msg [%s]", box, dev_name, dev_msg );
    ctr_incr( "/%s", dev_name );
    if (strlen( dev_msg ) == 0) 
      {
      Trace("%03d: %s result missing",box,dev_name);
      strcpy(description,"no message from scanner") ;
      ctr_incr( "/%s/fail", dev_name );
      ctr_incr( "/%s/fail/missing", dev_name );
      } 
    else if (dev_msg[0] == '?') 
      {
      Trace("%03d: %s no read", box, dev_name);
      strcpy(description,"no read from scanner") ;
      ctr_incr( "/%s/fail", dev_name );
      ctr_incr( "/%s/fail/noread", dev_name );
      } 
    else 
      {
      int num_valid = app_parse_scan( dev_msg, barcode);
      if (num_valid == 0) 
        {
        Trace("%03d: %s no valid barcode detected",box,dev_name);
        strcpy(description,"no valid barcode read") ;
        ctr_incr( "/%s/fail", dev_name );
        ctr_incr( "/%s/fail/invalid", dev_name );
        } 
      else if (num_valid > 1) 
        {
        Trace("%03d: %s multiple valid barcodes detected",box,dev_name);
        strcpy(description,"multiple valid barcodes read") ;
        } 
      else 
        {
        Trace("%03d: %s barcode [%s]",box,dev_name,barcode);
        strcpy(description,"valid barcode read") ;
        barcode_success = TRUE ;
        ctr_incr( "/%s/ok", dev_name );
        }
      }
    }

  if (barcode_success) 
    {
    strcpy(description,"barcode read") ;
    Trace( "%03d: [%s]",box,description );
    util_box_set( box, "lpn", barcode );
    }
  }

@ Carton Lookup 
@<get carton@>+=
  {
  char description[MSG_LEN+1] ;

  if(barcode_success)
    {
    if(0==strcmp(barcode,"FullCase"))
      {
      Inform("%03d: is full case",box) ;
      }
    else
      {
      cartonSeq = app_lookup_cartonSeq(barcode) ;
      if(cartonSeq>0)
        {
        Inform("%03d: found carton %d",box,cartonSeq) ;
        strcpy(description,"carton found") ;
        util_box_set( box, "cartonSeq", string(cartonSeq) );
        }
      else
        {
        Inform("%03d: carton not found",box) ;
        strcpy(description,"carton not found") ;
        }
      }
    }
  }

@ Get divert.
@<get divert@>+=
  {
  int code = 033 ;

  if(0==strcmp(barcode,"FullCase"))
    {
    divert = 1 ;
    }
  else if(cartonSeq>=0)
    {
    divert = app_carton_route_divert(name,cartonSeq,zone) ;
    }
  
  switch(divert)
    {
    case 1 : code = 001 ; break ;
    case 2 : code = 022 ; break ;
    case 3 : code = 033 ; break ;
    default: code = 033 ; break ;
    }

  bx_setdata(box,code) ;
  Inform("%03d: divert %d code %d",box,divert,code) ;
  }

@ Log.
@<log@>+=
  {
  const char *diverts[5] = {"?","straight","left","right","?"} ;
  char msg[255+0] ;
Inform("%d divert",divert) ;
  snprintf(msg,255,"floor %c divert %c sent %s %s",
           name[6],name[7],diverts[divert],zone) ;
Inform("%s",msg) ;

  if(0!=strcmp(barcode,"FullCase"))
    app_log_lpn_location(barcode,name,msg) ;
  }

@ String from int.
@<Functions@>+=
char *
string(int val)
  {
  static char s[32+1] ;

  snprintf(s,32,"%d",val) ;
  s[32] = '\0' ;

  return s ;
  }

@ Proto.
@<Prototypes@>+=
char *string(int val) ;        

@* Index.
