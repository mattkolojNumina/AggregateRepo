%
%   pfi_start.w -- box creation app
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
\def\title{start}
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
\centerline{Control Revision: $ $Revision: 1.11 $ $}
\centerline{Control Date: $ $Date: 2023/03/15 14:21:15 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Group, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program creates a box and initializes its status entries.

@c
static char rcsid[] = "$Id: pfi_start.w,v 1.11 2023/03/15 14:21:15 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;


int main( int argc, char *argv[] ) {
   int box;
   int line;
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
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_util.h>
#include <rds_trak.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // length of small statically allocated strings

@ Global variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];

@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s (lane) <box>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   strcpy( area, "pfi" );
   sprintf( name, "pfistart" );
   trn_register( name );

   line = atoi( argv[1] );
   box = atoi( argv[2] );
   if (!util_valid_box(box, name)) {
      exit( EXIT_SUCCESS );
   }
   Trace( "%03d: create box", box );
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
   util_set_stamp( seq, "startStamp" );
   Inform( "%03d: processing complete, took %.3f sec",
         box, util_get_elapsed( start_time ) );
   util_zone_release( name );
}


@ Create the box.
@<create box@>=
{
   seq = util_create_seq( box, area );
   util_carton_set_int(seq, "line", line);
   util_update_description( seq, area, "start processing at print/apply" );
}


@* Index.
