%
%   pack_update.w -- update carton data
%
%   Author: Adam Marshall
%
%   History:
%      2015-05-05 -AHM- init, for Sunstar print/apply
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
%     (C) Copyright 2015 Numina Group, Inc.  All Rights Reserved.
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
\def\title{Pack Update}
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
\centerline{Author: Adam Marshall}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.126 $ $}
\centerline{Control Date: $ $Date: 2017/05/29 18:24:08 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program applies business rules to interpret data captured from field
devices (scanners, scale, dim, etc.).

@c
static char rcsid[] = "$Id: xpal_update.w,v 1.126 2017/05/29 18:24:08 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int 
main( int argc, char *argv[] ) 
  {
  int box;
  int seq;

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
  int first ;
  int last ;

  if (argc != 3) 
    {
    printf( "usage: [%s] <area> <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }

  strncpy(area,argv[1],BUF_LEN) ;
  area[BUF_LEN] = '\0' ;

  sprintf( name, "%sUpdate", area );
  trn_register( name );

  first = app_first_box(area) ;
  last  = app_last_box(area) ;

  box = atoi( argv[2] );
  if ( (box<first) || (box>last)) 
    {
    Alert( "invalid box [%d]", box );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }

  seq = util_box_get_int( box, "seq" );
  if (seq <= 0) 
    {
    Alert( "%03d: invalid carton [%d]", box, seq );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }

  Trace( "%03d: update carton [%d]", box, seq );
  }

@ Update the carton.
@<process carton@>=
  {
  struct timeval start_time;
  char barcode[ BUF_LEN + 1 ];
  int barcode_success;

  @<begin processing@>@;
  @<get barcode@>@;
//TODO
  @<finish processing@>@;
  }

@ Begin processing: initialization, etc.
@<begin processing@>=
  {
  gettimeofday( &start_time, NULL );

  strcpy( barcode, "" );
  barcode_success = FALSE ;
  }

@ Finish processing.
@<finish processing@>=
  {
  sql_query("UPDATE `%sCartons` SET "
           "`updateStamp` = NOW() "
           "WHERE `seq` = %d",
            area, seq );

  Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
  util_zone_release( name );
  }


@ Get the barcode for the carton.  Apply business rules to the scan result
to extract a single valid identifier barcode. If zero or multiple valid
barcodes are found, this is an error.
@<get barcode@>=
  {
  char description[ MSG_LEN + 1 ];

  if (!util_required( seq, "barcode" )) 
    {
    Inform( "%03d: barcode processing not required for [%d]", box, seq );
    strcpy(description,"processing not required") ;
    barcode_success = TRUE;
    }
  else
    {
    char status_val[ BUF_LEN + 1 ];
    char dev_name[ BUF_LEN + 1 ];
    char dev_msg[ MSG_LEN + 1 ];

    barcode_success = FALSE;
    status_val[0] = '\0' ;
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
      Trace( "%03d: [%s] for [%d]", box, description, seq );
      util_box_set( box, "lpn", barcode );
      util_update_status( seq, "barcode", "complete", barcode );
      util_carton_set( area, seq, "lpn", barcode );
      }
    util_update_description( area, seq, NULL, NULL, description );
    }
  }

@* Index.
