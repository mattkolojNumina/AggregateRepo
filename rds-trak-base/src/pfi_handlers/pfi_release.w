%
%   pfi_release.w -- box creation app
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
\centerline{Control Revision: $ $Revision: 1.8 $ $}
\centerline{Control Date: $ $Date: 2023/03/27 15:38:46 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Group, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program releases the box to the pfi based on the pfi's state.

@c
static char rcsid[] = "$Id: pfi_release.w,v 1.8 2023/03/27 15:38:46 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;


int main( int argc, char *argv[] ) {
   int box;
   int line;
   int seq;

   @<run@>@;

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
#include <zonetrak.h>
#include <zonepfi.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // length of small statically allocated strings

@ Global variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];

@ Run.
@<run@>+=
{
   if (argc != 3) {
      printf( "usage: %s (lane) <box>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   strcpy( area, "pfi" );
   sprintf( name, "pfirelease" );
   trn_register( name );

   char zone[BUF_LEN+1];
   int zone_h = 0;

   util_strcpy(zone, util_get_control(name, "zone", ""));
   if(valid_str(zone)) zone_h = dp_handle(zone);
   else {
      Alert("no 'zone' control value defined! Abort");
      exit( EXIT_FAILURE );
   }

   line = atoi( argv[1] );
   box = atoi( argv[2] );
   if (!util_valid_seq(box, &seq, name)) {
      Inform("unknown box; release");
      util_zone_release( name );  
      exit( EXIT_SUCCESS );
   }

   char pfi[BUF_LEN+1];
   util_strcpy(pfi, util_get_control(name, "pfi", ""));
   if(!valid_str(pfi)) {
      Alert("no pfi defined! release");
      util_zone_release( name );  
      exit( EXIT_SUCCESS );
   }

   int pfi_h = dp_handle(pfi);

   if(pfi_h <= 0) {
      Alert("%s does not exist in trak! release", pfi);
      util_zone_release( name );  
      exit( EXIT_SUCCESS );
   }

   int pfi_state = 0;
   int current_box = 0;

   dp_registerget(pfi_h, PFI_REG_STATE, &pfi_state);
   dp_registerget(zone_h, REG_BOX, &current_box);

   while (pfi_state >= 1 && pfi_state <= 12) {
      if(box != current_box) {
         Alert("box removed from zone!");
         break;
      }
      Inform("Waiting for %s to finish purging...", pfi);
      sleep(1);
      dp_registerget(pfi_h, PFI_REG_STATE, &pfi_state);
      dp_registerget(zone_h, REG_BOX, &current_box);
   }

   if (box != current_box) {
      exit( EXIT_SUCCESS );
   }

   char *val = sql_getvalue(
      "SELECT COUNT(*) FROM pfiQueue " 
      "WHERE pfi='%s' AND cartonId=%d "
      "AND canceled IS NULL "
      "AND inserted IS NULL ",
      pfi, seq
   );
   if(!valid_str(val) ||  atoi(val) == 0) {
     Inform("%03d: No printing required for conveyorBoxes seq %d; release", box, seq);
     util_zone_release( name );  
     exit( EXIT_SUCCESS );
   }

   dp_registerget(pfi_h, PFI_REG_STATE, &pfi_state);
   dp_registerget(zone_h, REG_BOX, &current_box);
   while(pfi_state != PFI_STATE_RUN) {
      if(box != current_box) {
         Alert("box removed from zone!");
         break;
      }
      Inform("Waiting for %s to enter run state...", pfi);
      sleep(1);
      dp_registerget(pfi_h, PFI_REG_STATE, &pfi_state);
   }

   if (box != current_box) {
      exit( EXIT_SUCCESS );
   }

   util_zone_release( name );  
   exit( EXIT_SUCCESS );
}




@* Index.
