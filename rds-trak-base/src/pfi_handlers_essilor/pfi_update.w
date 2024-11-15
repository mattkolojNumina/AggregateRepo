%
%   pfi_update.w -- update carton data
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
\def\title{update}
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
\centerline{Control Revision: $ $Revision: 1.8 $ $}
\centerline{Control Date: $ $Date: 2021/06/11 16:40:16 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program applies business rules to interpret data captured from field
devices (scanners, scale, etc.).

@c
static char rcsid[] = "$Id: pfi_update.w,v 1.8 2021/06/11 16:40:16 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   int box;
   int seq;
   int line;

   @<initialize@>@;
   @<process carton@>@;

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_util.h>
#include <rds_ctr.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN       32  // length of small statically allocated strings
#define MSG_LEN      256  // length of longer message strings
#define SLEEP_DURATION  50  // sleep duration while waiting (msec)
#define TIMEOUT     2000  // max time for device polling before release (msec)


@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];


@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s <box> <line>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   line = atoi( argv[2] ); // 1, 2, or 3

   strcpy( area, "pfi" );
   sprintf( name, "p%d-updt", line );
   trn_register( name );

   box = atoi( argv[1] );
   if (box <= 0 || box > 999) {
      Alert( "invalid box [%d]", box );
      util_zone_release( name );
      exit( EXIT_SUCCESS );
   }

   seq = util_box_get_int( box, "seq" );
   if (seq <= 0) {
      Alert( "%03d: invalid carton [%d]", box, seq );
      util_zone_release( name );
      exit( EXIT_SUCCESS );
   }

   if (!valid_process( box, seq )) {
      Alert( "%03d: invalid processing status", box );
      util_zone_fault( name );
      exit( EXIT_SUCCESS );
   }

   Trace( "%03d: update carton [%d]", box, seq );
}


@ Check the processing status for this carton.
@<Functions@>+=
int valid_process( int box, int seq ) {
   char *val = sql_getvalue(
         "SELECT COUNT(*) FROM `%sCartons` "
         "WHERE seq = %d "
         "AND startStamp IS NOT NULL "
         "AND updateStamp IS NULL",
         area, seq );
   if (val != NULL && atoi( val ) == 1)
      return TRUE;

   return FALSE;
}
@ Prototype the function.
@<Prototypes@>+=
int valid_process( int box, int seq );


@ Update the carton.
@<process carton@>=
{
   struct timeval start_time;

   char barcode[ BUF_LEN + 1 ];

   int barcode_success, status_success;
   int done = FALSE;
   int carton_seq;


   @<begin processing@>@;

   @<get barcode@>@;
   @<check carton status@>@;

   @<finish processing@>@;
}


@ Begin processing: initialization, etc.
@<begin processing@>=
{
   gettimeofday( &start_time, NULL );

   strcpy( barcode, "" );

   barcode_success = status_success = FALSE;
}


@ Finish processing.
@<finish processing@>=
{
   sql_query(
         "UPDATE `%sCartons` SET "
         "updateStamp = NOW() "
         "WHERE seq = %d",
         area, seq );

   if (line == 1)
      util_carton_set( area, seq, "pfi", "a" );

   if (line == 2)
      util_carton_set( area, seq, "pfi", "b" );

   if (line == 3)
      util_carton_set( area, seq, "pfi", "c" );

   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
   util_zone_release( name );
}


@ Get the barcode for the carton.  Apply business rules to the scan result
to extract a single valid identifier barcode.  If zero or multiple valid
barcodes are found, this is an error.
@<get barcode@>=
{
   if (!util_required( seq, "barcode" )) {
      Inform( "%03d: barcode processing not required for [%d]", box, seq );
      barcode_success = TRUE;
   } else {
      char dev_name[ BUF_LEN + 1 ];
      char dev_msg[ MSG_LEN + 1 ];

      strncpy( dev_name, util_get_control( name, "scan", "" ), BUF_LEN );
      dev_name[ BUF_LEN ] = '\0';
      util_poll_for_msg( box, dev_name, dev_msg );
      Inform( "%03d: %s msg [%s]", box, dev_name, dev_msg );

      char status_val[ BUF_LEN + 1 ];
      char description[ MSG_LEN + 1 ];

      strcpy( status_val, "" );
      strcpy( description, "" );
      barcode_success = FALSE;
  
      ctr_incr( "/%s", dev_name );

      if (strlen( dev_msg ) == 0) {
         strcpy( description, "unable to locate induct scanner result" );
         strcpy( status_val, "missing" );
         ctr_incr( "/%s/missing", dev_name);
      } else if (dev_msg[0] == '?') {
         strcpy( description, "induct scanner noread" );
         strcpy( status_val, "noread" );
         ctr_incr( "/%s/noread", dev_name );
      } else {
         int num_valid = app_parse_scan( dev_msg, barcode );
         if (num_valid == 0) {
            strcpy( description, "no valid barcode scanned at induct" );
            strcpy( status_val, "none" );
            ctr_incr( "/%s/novalid", dev_name );
         } else if (num_valid > 1) {
            strcpy( description, "multiple valid barcodes scanned at induct" );
            strcpy( status_val, "multiple" );
            ctr_incr( "/%s/multiple", dev_name );
         } else {
            sprintf( description, "detected barcode %s", barcode );
            barcode_success = TRUE;
            ctr_incr( "/%s/ok", dev_name ); 
         }
      }

      if (barcode_success) {
         Trace( "%03d: %s for [%d]", box, description, seq );
         util_update_status( seq, "barcode", "complete", barcode );
         util_carton_set( area, seq, "barcode", barcode );
      } else {
         Alert( "%03d: %s for [%d]", box, description, seq );
         util_update_status( seq, "barcode", "failed", status_val );
         done = TRUE;
      }

      app_update_description( area, seq, area, description );
      if (util_do_consec( dev_name, barcode_success ))
         util_zone_fault( name );
   }
}

@ Check carton status.
@<check carton status@>=
{
   if (!util_required( seq, "status" )) {
      Inform( "%03d: status check not required for [%d]", box, seq );
      status_success = TRUE;
   } else {
      char status_val[ BUF_LEN + 1 ];
      char description[ MSG_LEN + 1 ];

      char seq_str[ BUF_LEN + 1 ];

      strcpy( status_val, "" );
      strcpy( description, "" );
      status_success = FALSE;

      if (strlen( barcode ) > 0) {
         char *val = sql_getvalue(
               "SELECT cartonSeq FROM cartons "
               "WHERE barcode = '%s'",
               barcode );
         if (val != NULL && (carton_seq = atoi( val )) > 0) {
            status_success = TRUE;
            Inform( "%03d: matched to carton seq %d", box, carton_seq );
            sprintf( seq_str, "%d", carton_seq );
         } else {
            strcpy( description, "unable to determine carton" );
            strcpy( status_val, "data" );
         }
      } else {
         strcpy( description, "unable to determine carton" );
         strcpy( status_val, "data" );
      }

      if (status_success) {
         Trace( "%03d: %s for [%d]", box, description, seq );
         util_update_status( seq, "status", "complete", "" );
         util_carton_set( area, seq, "cartonSeq", seq_str );
      } else {
         Alert( "%03d: %s for [%d]", box, description, seq );
         util_update_status( seq, "status", "failed", status_val );
         done = TRUE;
      }
      
      app_update_description( area, seq, area, description );
   }
}

@* Index.
