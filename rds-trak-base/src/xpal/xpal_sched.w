%
%   xpal_sched.w -- schedule printing
%
%   Author: Adam Marshall
%
%   History:
%      2011-12-08 -AHM- init, for X-Press PAL
%      2014-04-30 -AHM- modified for OneStep+
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
%     (C) Copyright 2011-2015 Numina Group, Inc.  All Rights Reserved.
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
\def\title{XPAL schedule}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program schedules printing.

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
\centerline{Control Revision: $ $Revision: 1.21 $ $}
\centerline{Control Date: $ $Date: 2019/02/13 22:42:06 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Group, Inc.
All Rights Reserved.}
}

@* Overview. 
This program schedules printing.

@c
static char rcsid[] = "$Id: xpal_sched.w,v 1.21 2019/02/13 22:42:06 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Functions@>@;
@<Prototypes@>@;

int 
main(int argc,char *argv[]) 
  {
  int box;
  int seq;
  int cartonSeq ;

  @<initialize@>@;
  @<process box@>@;

  exit( EXIT_SUCCESS );
  }

@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <zonetrak.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN           32  // length of small statically allocated strings
#define BUF_LRG          128
#define MSG_LEN          255  // length of longer messages strings
#define SLEEP_DURATION   100  // sleep between host-monitoring attempts (msec)
#define TIMEOUT           10   // response timeout (sec)
#define ZERO_STAMP   "0000-00-00 00:00:00"  // timestamp for error

@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LRG + 1 ];

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

  sprintf( name, "%sSchedule", area );
  trn_register( name );

  first = app_first_box(area) ;
  last  = app_last_box(area) ;

  box = atoi( argv[2] );
  if (box<first || box>last) 
    {
    Alert( "invalid box [%d]", box );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }

  seq = util_box_get_int( box, "seq" );
  if (seq <= 0) 
    {
    Alert( "%03d: invalid seq [%d]", box, seq );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }

  cartonSeq = util_box_get_int(box,"cartonSeq") ;
  if(cartonSeq<=0)
    {
    Alert("%03d: invalid cartonSeq [%d]",box,cartonSeq) ;
    util_zone_release(name) ;
    exit(EXIT_SUCCESS) ;
    }

  Trace( "%03d: update carton [%d]", box, seq );
  }

@ Process the box.
@<process box@>=
  {
  struct timeval start_time;
  char prn[ BUF_LEN + 1 ];
  int label_success = 0 ;

  @<begin processing@>@;
  @<check label@>@;
  @<schedule printing@>@;
  @<finish processing@>@;
  }

@ Begin processing: initialization, etc.
@<begin processing@>=
  {
  gettimeofday( &start_time, NULL );
  strcpy( prn, "" );
  label_success = FALSE;
  }

@ Finish processing.
@<finish processing@>=
  {
  Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
  util_zone_release( name );
  }

@ Check if label exists in labels table.
@<check label@>=
  {
  if (!util_required( seq, "label" )) 
    {
    Inform( "%03d: label processing not required for [%d]", box, seq );
    } 
  else 
    {
    int docSeq = -1 ;

    docSeq = app_label_load(cartonSeq,"east",seq) ;
 
    if (docSeq>0)
      label_success = TRUE;

    if (label_success) 
      {
      Trace( "%03d: label data present for [%d]", box, seq );
      util_update_status( seq, "label", "complete", "" );
      } 
    else 
      {
      Alert( "%03d: no label data for [%d]", box, seq );
      util_update_status( seq, "label", "failed", "nolabel" );
      }

    if (util_do_consec( "label", label_success ))
      util_zone_fault( name );
    }
  }

@ Schedule printing.
@<schedule printing@>=
  {
  if(label_success)
    {
    char prn1[ BUF_LEN + 1 ];
    char prn2[ BUF_LEN + 1 ];

    strcpy( prn1, util_get_control(name, "prn1", "") );
    strcpy( prn2, util_get_control(name, "prn2", "") );
    Inform("%03d: prn1 [%s], prn2 [%s]", box, prn1, prn2);

    for(strcpy(prn,""); strlen(prn)==0; usleep(SLEEP_DURATION*1000)) 
      {
      int avail1, avail2;
      char *val;
         
      if (get_zone_box() != box) 
        {
        Alert( "%03d: box removed during processing", box );
        break;
        }
         
      avail1 = prn_avail( prn1 ) ;
      avail2 = prn_avail( prn2 ) ;
      Inform("avail %d %d",avail1,avail2) ;

      if (avail1 && avail2) 
        {
        val = sql_getvalue("SELECT `value` FROM `runtime` "
                           "WHERE `name` = 'eastLast'");
        if (val != NULL && strcmp( val, prn1 ) == 0)
          strcpy( prn, prn2 );
        else
          strcpy( prn, prn1 );
        } 
      else if (avail1 && !avail2)
        strcpy( prn, prn1 );
      else if (!avail1 && avail2)
        strcpy( prn, prn2 );
      else
        strcpy( prn, "" );
      }

    Inform("%03d: choose printer [%s]", box, prn);
    util_carton_set( area, seq, "printer", prn );
    if (strlen( prn ) > 0) 
      {
      sql_query("REPLACE `runtime` SET "
                "`name` = 'eastLast', "
                "`value` = '%s'",prn );

      sql_query("UPDATE `labels` SET "
                "`printer` = '%s', "
                "`printed` = 'no' "
                "WHERE `seq` = %d "
                "AND `ordinal` = 1", 
                prn, seq );
         
      util_update_status( seq, prn, "pending", "" );
      util_update_status( seq, "verify", "pending", "" );
      util_update_description( area, seq,NULL,NULL,
                              "schedule to [%s]", prn );
      }
    }
  }

@ Check printer availability.
@<Functions@>+=
int 
prn_avail( const char prn[] ) 
  {
  char dp_name[ TRAK_NAME_LEN + 1 ];
  int dp = -1;
  int avail = TRUE;

  if (strlen( prn ) == 0)
    return FALSE;

  sprintf( dp_name, "%s_ok", prn );
  dp = dp_handle( dp_name );
  avail &= (dp <= 0 || dp_get( dp ) == 1);

  sprintf( dp_name, "%s_fault", prn );
  dp = dp_handle( dp_name );
  avail &= (dp <= 0 || dp_get( dp ) == 0);

  sprintf( dp_name, "%s_bypass", prn );
  dp = dp_handle( dp_name );
  avail &= (dp <= 0 || dp_get( dp ) == 0);

  return avail;
  }

@ Prototype the function.
@<Prototypes@>+=
int prn_avail( const char prn[] );

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
