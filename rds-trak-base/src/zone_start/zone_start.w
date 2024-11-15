%
%   zone_start.w -- clear box data 
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
{Zone Start}
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
\centerline{Control Revision: $ $Revision: 1.3 $ $}
\centerline{Control Date: $ $Date: 2024/02/20 20:07:13 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program applies business rules to interpret data captured from field
devices (scanners, scale, dim, etc.).

@c
static char rcsid[] = "$Id: zone_start.w,v 1.3 2024/02/20 20:07:13 rds Exp rds $";
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
  if (argc != 2) 
    {
    printf( "usage: [%s] <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }

  trn_register( "zstart" );

  box = atoi( argv[1] );
  if (box<1 || box>999) 
    {
    Alert( "invalid box [%d]", box );
//    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }
  }

@ Process the carton.
@<process carton@>=
  {
  struct timeval start_time;

  @<begin processing@>@;
  @<clear box data@>@;
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
//  util_zone_release( name );
  }

@ Get the barcode for the carton.  Apply business rules to the scan result
to extract a single valid identifier barcode. If zero or multiple valid
barcodes are found, this is an error.
@<clear box data@>=
  {
  util_box_clear(box) ;
  }

@* Index.
