%
%   pfi_sched.w -- schedule print/fold/insert
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
\def\title{schedule}
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
\centerline{Control Revision: $ $Revision: 1.2 $ $}
\centerline{Control Date: $ $Date: 2021/06/09 20:40:38 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Group, Inc.
All Rights Reserved.}
}

@* Overview. 
This program schedules printing.

@c
static char rcsid[] = "$Id: pfi_sched.w,v 1.2 2021/06/09 20:40:38 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Globals@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   int box;
   int seq;
   int line;

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
#include <rds_ctr.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN           32  // length of small statically allocated strings
#define MSG_LEN          255  // length of longer messages strings
#define SLEEP_DURATION   100  // sleep between cycles (msec)


@ Global status variables.
@<Globals@>+=
int line;
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];


@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s <box> <line>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   line = atoi( argv[2] );
   strcpy( area, "pfi" );
   snprintf( name, BUF_LEN, "p%d-sched", line );
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

   Trace( "%03d: schedule carton [%d]", box, seq );
}


@ Process the box.
@<process box@>=
{
   struct timeval start_time;
   
   
   int carton_seq;
   char folder[ BUF_LEN + 1 ];

   char barcode[ BUF_LEN + 1 ];
   int barcode_sucess;

   int label_success;

   @<begin processing@>@;

   @<check docs@>@;
   @<schedule pfi@>@;

   @<finish processing@>@;
}


@ Begin processing: initialization, etc.
@<begin processing@>=
{
   gettimeofday( &start_time, NULL );
   carton_seq = 0;
   strcpy( folder, "" );
   barcode_sucess = label_success = FALSE;
   strcpy( barcode, "" );
}


@ Finish processing.
@<finish processing@>=
{
   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
   util_zone_release( name );
}

@ Check docs.
@<check docs@>=
{
   if (!util_required( seq, "doc" )) {
      Inform( "%03d: document processing not required for [%d]", box, seq );
   } else {
      char *val;
      int label_len = 0;

      carton_seq = atoi( util_carton_get( area, seq, "cartonSeq" ) );

      ctr_incr( "/pfi/doc/%d/", line );

      val = sql_getvalue(
            "SELECT LENGTH( doc ) FROM docs "
            "WHERE cartonSeq = %d "
            "LIMIT 1",
            carton_seq );
      if (val != NULL && strlen( val ) > 0)
         label_len = atoi( val );

      // update label status
      if (label_len > 0)
         label_success = TRUE;

      if (label_success) {
         Trace( "%03d: doc data present for [%d]", box, seq );
         util_update_status( seq, "doc", "complete", "" );
         ctr_incr( "/pfi/doc/%d/success", line );
      } else {
         Alert( "%03d: no doc data for [%d]", box, seq );
         util_update_status( seq, "doc", "failed", "nodata" );
         ctr_incr( "/pfi/doc/%d/failed", line ); 
      }
 
      if (util_do_consec( "doc", label_success ))
         util_zone_fault( name );
   }
}


@ Schedule print/fold/insert.
@<schedule pfi@>=
{
   if (TRUE) { // <- why is this here?
      char f1[ BUF_LEN + 1 ];
      char f2[ BUF_LEN + 1 ];
      char f3[ BUF_LEN + 1 ];
 
      if ( line == 1 )
         strcpy( f1, "f-a" );
      else if ( line == 2 )
         strcpy( f2, "f-b" );
      else if ( line == 3 )
         strcpy( f3, "f-c" );

      for (strcpy( folder, "" );
            strlen( folder ) == 0;
            usleep( SLEEP_DURATION * 1000 ) ) {
         if (get_zone_box() != box) {
            Alert( "%03d: box removed during processing", box );
            break;
         }

         if ( line == 1 ){
            if (folder_avail( f1 ))
               strcpy( folder, f1 );
         } else if ( line == 2) {
            if ( folder_avail( f2 ) )   
               strcpy( folder, f2 );
         } else if ( line == 3 ) {
            if ( folder_avail( f3 ) )
               strcpy( folder, f3 );
         }
  
      }

      if (strlen( folder ) > 0) {
         Trace( "%03d: print/fold/insert scheduled on %s", box, folder );

         @<queue pfi@>@;

         util_update_status( seq, "inserts", "pending", "" );
         app_update_description( area, seq, area, "doc insertion scheduled" );
      }
   }
}


@ Queue print/fold/insert.
@<queue pfi@>=
{
   int err;
   int doc_seq;
   int num_sheets;
   int batch_rp, batch;
   int num_drops;
   char foldx[ BUF_LEN + 1 ];

   doc_seq = -1;
   num_sheets = 0;
   batch = 5;  // default
   num_drops = 1;

   batch_rp = rp_handle( "page_batch" );
   if (batch_rp > 0)
      batch = rp_get( batch_rp );
   if (batch <= 0)
      batch = 1;

   err = sql_query(
         "SELECT docSeq, numPages FROM docs "
         "WHERE cartonSeq = %d "
         "ORDER BY docSeq DESC LIMIT 1",
         carton_seq );
   if (err || sql_rowcount() != 1)
      Alert( "failed to determine doc for carton seq [%d]", carton_seq );
   else {
      doc_seq = atoi( sql_get( 0, 0 ) );
      num_sheets = atoi( sql_get( 0, 1 ) );
      num_drops = ((num_sheets - 1) / batch) + 1;
      Trace( "queue doc %d (sheets = %d, drops = %d) for carton seq [%d]",
            doc_seq, num_sheets, num_drops, carton_seq );
   }

   if ( line == 1 )
      strcpy( foldx , "a");
   else if ( line == 2 )
      strcpy( foldx , "b" );
   else if( line == 3 ) 
      strcpy( foldx, "c" );


   if ( foldx != NULL && strlen(foldx) >  0 ) 
   sql_query(
          "UPDATE cartons set pfi='%s' WHERE cartonSeq = %d", 
            foldx, carton_seq );


   if (doc_seq > 0) {
      sql_query(
            "INSERT packQueue SET "
            "box = %d, "
            "cartonId = %d, "
            "docSeq = %d, "
            "sheets = %d, "
            "drops = %d, "
            "folder = '%s', "
            "created = NOW()",
            box, seq, doc_seq, num_sheets, num_drops, foldx );
   }
}


@ Check current box.
@<Functions@>+=
int get_zone_box( void ) {
   static int zone_dp = -1;
   int box;

   if (zone_dp < 0) {
      char zone[ TRAK_NAME_LEN + 1 ];
      util_zone_get( name, zone );
      if (strlen( zone ) == 0) {
         Alert( "no zone specified" );
         return BOX_NONE;
      }

      zone_dp = dp_handle( zone );
      if (zone_dp < 0) {
         Alert( "unable to obtain dp for zone %s", zone );
         return BOX_NONE;
      }
   }

   dp_registerget( zone_dp, REG_BOX, &box );
   return box;
}
@ Prototype the function.
@<Prototypes@>+=
int get_zone_box( void );


@ Check folder availability.
@<Functions@>+=
int folder_avail( const char folder[] ) {
   char dp_name[ TRAK_NAME_LEN + 1 ];
   int dp = -1;
   int avail = TRUE;

   if (strlen( folder ) == 0)
      return FALSE;

   sprintf( dp_name, "%s_ready", folder );
   dp = dp_handle( dp_name );
   avail &= (dp <= 0 || dp_get( dp ) == 1);

   sprintf( dp_name, "%s_fault", folder );
   dp = dp_handle( dp_name );
   avail &= (dp <= 0 || dp_get( dp ) == 0);

   return avail;
}
@ Prototype the function.
@<Prototypes@>+=
int folder_avail( const char folder[] );


@* Index.
