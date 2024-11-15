%
%   pack_start.w -- box creation app
%
%   Author: Adam Marshall
%
%   History:
%      2010-08-23 -AHM- init, for X-Press PAL
%      2013-03-23 -AHM- updated to release box when complete
%      2013-11-02 -AHM- updated for multiple lines
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
%     (C) Copyright 2013 Numina Group, Inc.  All Rights Reserved.
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
\def\title{Pack Start}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program creates a box and initializes its status entries.

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
\centerline{Control Revision: $ $Revision: 1.10 $ $}
\centerline{Control Date: $ $Date: 2017/05/03 15:22:34 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Group, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program creates a box and initializes its status entries.

@c
static char rcsid[] = "$Id: pack_start.w,v 1.10 2017/05/03 15:22:34 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int 
main( int argc, char *argv[] ) 
  {
  int box;
  int seq;

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
char area[ BUF_LEN + 1 ];
char name[ BUF_LRG + 1 ];

@ Initialization.
@<initialize@>+=
  {
  int first ;
  int last ;

  if (argc != 3) 
    {
    printf( "usage: %s <area> <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }

  strncpy(area,argv[1],BUF_LEN) ;
  area[BUF_LEN]='\0' ;
 
  sprintf( name, "%sStart", area);
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

  Trace( "%03d: create carton", box);
  }

@ Process the box.
@<process box@>=
  {
  struct timeval start_time;

  @<begin processing@>@;
  @<create box@>@;
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
  sql_query("UPDATE `%sCartons` "
            "SET `startStamp` = NOW() "
            "WHERE `seq` = %d",
            area, seq );

  Inform( "%03d: processing complete, took %.3f sec",
         box, util_get_elapsed( start_time ) );
  util_zone_release( name );
  }

@ Create the box.
@<create box@>=
  {
  int bypass_h;
  char dp_name[ BUF_LRG + 1 ];

  // clear any previous box data
  util_box_clear( box );

  // create the new box - assign it a seq no.
  seq = util_create_seq();
  sql_query("REPLACE INTO `%sCartons` SET "
            "seq = %d, "
            "box = %d ",
            area, seq, box);

  util_box_set_int( box, "seq", seq );
  Inform( "%03d: box created with seq [%d]", box, seq );

  // initialize carton status
  sql_query("DELETE FROM `cartonStatus` "
            "WHERE seq = %d",
            seq );

  snprintf( dp_name, BUF_LRG, "%s_bypass", area );
  dp_name[ BUF_LRG ] = '\0';
  bypass_h = dp_handle( dp_name );
  if (bypass_h > 0 && dp_get( bypass_h )) 
    {
    Inform( "%03d: box created in bypass mode", box );
    sql_query("INSERT `cartonStatus` SET "
              "seq = %d, "
              "ordinal = 1, "
              "name = 'bypass', "
              "status = 'pending'",
              seq );
    sql_query("UPDATE `%sCartons` SET "
              "lane = 'bypass' "
              "WHERE seq = %d",
              area, seq );
    } 
  else 
    {
    sql_query("INSERT `cartonStatus` "
              "SELECT %d, name, ordinal, status, value, NOW() "
              "FROM cartonStatusInit "
              "WHERE area = '%s'",
              seq, area );
    }

   util_update_description(area,seq,NULL,NULL,
                           "start processing at pack" );
   }

@* Index.
