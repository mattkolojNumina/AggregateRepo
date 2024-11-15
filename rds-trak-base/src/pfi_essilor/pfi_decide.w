%
%   pfi_decide.w -- determine processing status
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
\def\title{decide}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program decides the processing status of a box and assigns the
physical lane for sortation.

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
\centerline{Control Revision: $ $Revision: 1.3 $ $}
\centerline{Control Date: $ $Date: 2021/06/17 17:58:36 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program decides the processing status of a box and assigns the
physical lane for sortation.

@c
static char rcsid[] = "$Id: pfi_decide.w,v 1.3 2021/06/17 17:58:36 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
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
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <rds_ctr.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // length of small statically allocated strings
#define MSG_LEN         256  // length of longer message strings
#define CODE_SUCCESS      1  // status code for success
#define CODE_ERROR       -1  // status code for unspecified error
#define CODE_LOST       -99  // status code for lost box


@ Global status variables.
@<Globals@>+=
int line;
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];
char line_c[ BUF_LEN + 1 ];


@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s <box> <line>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   line = atoi( argv[2] );   
   strcpy( area, "pfi" );
   snprintf( name, BUF_LEN, "p%d-decd", line );   
   trn_register( name );

   box = atoi( argv[1] );
   if (box <= 0 || box > 999) {
      Alert( "invalid box [%d]", box );
      //util_zone_release( name );
      exit( EXIT_SUCCESS );
   }

   seq = util_box_get_int( box, "seq" );
   if (seq <= 0) {
      Alert( "%03d: invalid carton [%d]", box, seq );
      //util_zone_release( name );
      exit( EXIT_SUCCESS );
   }

   Trace( "%03d: decide carton [%d]", box, seq );
}


@ Process the carton.
@<process carton@>=
{
   struct timeval start_time;
   int code;

   @<begin processing@>@;

   @<determine status@>@;
   @<update carton@>@;

   @<finish processing@>@;
}


@ Begin processing: initialization, etc.
@<begin processing@>=
{
   gettimeofday( &start_time, NULL );

   code = CODE_ERROR;
}


@ Finish processing.
@<finish processing@>=
{

   if (line == 1)
      strcpy(line_c, "a");

   if (line == 2)
      strcpy(line_c, "b");

   if (line == 3)
      strcpy(line_c, "c");

   line_c[1] = '\0';

   sql_query(
         "UPDATE `%sCartons` SET "
         "decideStamp = NOW() "
         "WHERE seq = %d",
         area, seq );

   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );

   // mark lost boxes
   for ( ; ; usleep( 10 ) ) {
      int lost_seq;
      char *val = sql_getvalue(
            "SELECT seq FROM `%sCartons` "
            "WHERE seq < %d "
            "AND status = 0 "
            "AND pfi = '%s' "
            "ORDER BY seq LIMIT 1",
            area, seq, line );
      if (val == NULL || strlen( val ) == 0)
         break;

      lost_seq = atoi( val );
      Alert( "carton [%d] detected as lost", lost_seq );
      sql_query(
            "UPDATE `%sCartons` SET "
            "status = %d, "
            "description = 'lost in tracking', "
            "decideStamp = NOW() "
            "WHERE seq = %d",
            area, CODE_LOST, lost_seq );
   }

   //util_zone_release( name );
}


@ Determine the carton status.
@<determine status@>=
{
   char result_name[ BUF_LEN + 1 ];
   char result_status[ BUF_LEN + 1 ];
   char result_value[ BUF_LEN + 1 ];
   char result_desc[ MSG_LEN + 1 ];

   code = get_carton_result( seq, result_name, result_status,
         result_value, result_desc );

   ctr_incr( "/%s/%d", area,line );
   if (code >= 0) {
      Trace( "%03d: %s for [%d] (code %d)", box, result_desc, seq, code );
      ctr_incr( "/%s/%d/decide/pass", area,line );
      ctr_incr( "/%s/%d/decide/pass/%s", area,line, result_name );
   } else {
      Alert( "%03d: %s for [%d] (code %d)", box, result_desc, seq, code );
      ctr_incr( "/%s/%d/decide/fail", area,line );
      ctr_incr( "/%s/%d/decide/fail/%s", area, line, result_name );
      ctr_incr( "/%s/%d/decide/fail/%s/%s", area,line, result_name, result_status );
      if (strlen( result_value ) > 0)
         ctr_incr( "/%s/%d/decide/fail/%s/%s/%s",
               area, line, result_name, result_status, result_value );
   }

   // update the carton
   app_update_description( area, seq, area, result_desc );
   sql_query(
         "UPDATE `%sCartons` SET "
         "status = %d "
         "WHERE seq = %d",
         area, code, seq );
}


@ Determine the processing status for a carton.
@<Functions@>+=
int get_carton_result( int seq, char result_name[], char result_status[],
      char result_value[], char description[] ) {
   int err, code;

   strcpy( result_name, "" );
   strcpy( result_status, "" );
   strcpy( result_value, "" );
   strcpy( description, "" );

   err = sql_query(
         "SELECT name, status, value FROM cartonStatus "
         "WHERE seq = %d "
         "AND status IN ('pending', 'failed') "
         "ORDER BY ordinal LIMIT 1",
         seq );

   if (err) {
      strcpy( result_name, "unknown" );
      strcpy( description, "unable to determine carton status" );
      Alert( "sql error (%d) determining carton status for [%d]",
            err, seq );
      return CODE_ERROR;
   }

   if (sql_rowcount() == 0) {
      strcpy( result_name, "success" );
      strcpy( description, "carton processed successfully" );
      return CODE_SUCCESS;
   }
   strncpy( result_name, sql_get( 0, 0 ), BUF_LEN );
   result_name[ BUF_LEN ] = '\0';
   strncpy( result_status, sql_get( 0, 1 ), BUF_LEN );
   result_status[ BUF_LEN ] = '\0';
   strncpy( result_value, sql_get( 0, 2 ), BUF_LEN );
   result_value[ BUF_LEN ] = '\0';
   Inform( "carton [%d] has result [%s-%s], value = [%s]",
         seq, result_name, result_status, result_value );

   // look up the result description
   err = sql_query(
         "SELECT code, result FROM results "
         "WHERE name = '%s' "
         "AND status = '%s' "
         "AND value = '%s'",
         result_name, result_status, result_value );
   if (!err && sql_rowcount() == 1) {
      code = atoi( sql_get( 0, 0 ) );
      strcpy( description, sql_get( 0, 1 ) );
   } else {
      code = CODE_ERROR;
      if (strlen( result_value ) > 0)
         sprintf( description, "%s %s, value = %s",
               result_name, result_status, result_value );
      else
         sprintf( description, "%s %s", result_name, result_status );
   }

   return code;
}
@ Prototype the function.
@<Prototypes@>+=
int get_carton_result( int seq, char result_name[], char result_status[],
      char result_value[], char description[] );


@ Update processing status.
@<update carton@>=
{
   if (code > 0) {
      int carton_seq = atoi( util_carton_get( area, seq, "cartonSeq" ) );

      sql_query(
            "UPDATE cartons SET "
            "`%sStatus` = 'complete', "
            "`%sStamp` = NOW() "
            "WHERE cartonSeq = %d",
            area, area, carton_seq );
      Inform( "%03d: carton seq [%d] marked as complete at %s",
            box, carton_seq, area );
   }
}


@* Index.
