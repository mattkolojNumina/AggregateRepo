%
%   pfi_decide.w -- determine processing status and sorter lane assignment
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
\centerline{Control Revision: $ $Revision: 1.31 $ $}
\centerline{Control Date: $ $Date: 2023/04/11 22:43:24 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program decides the processing status of a box and assigns the
physical lane for sortation.

@c
static char rcsid[] = "$Id: pfi_decide.w,v 1.31 2023/04/11 22:43:24 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   int box;
   int line;
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
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];
char barcode[MSG_LEN+1];


@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s (line) <box>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   strcpy( area, "pfi" );
   sprintf( name, "pfidecide" );
   trn_register( name );

   line = atoi( argv[1] );
   box = atoi( argv[2] );
   if (!util_valid_seq(box, &seq, name)) {
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
   util_set_stamp(seq, "decideStamp");

   int carton_seq = util_carton_get_int(seq, "refValue");
   char status[BUF_LEN+1];
   strcpy(status, "");
   util_get_status_value(seq, "inserts", status);
   status[BUF_LEN]='\0';
   if(util_complete(seq, "inserts")) {
     strcpy(status, "ok");
   } else if (valid_str(status) && strcmp(status,"noinserts")==0) {
     strcpy(status, "ok");
   } else if (util_failed(seq, "inserts")) {
     strcpy(status, "error");
   } else if (util_required(seq, "inserts")) {
     strcpy(status, "error");
   } else if (util_failed(seq, "schedule")) {
     strcpy(status, "error");
   }
  
   // mark lost boxes
   for ( ; ; usleep( 10 ) ) {
      int lost_seq;
      char *val = sql_getvalue(
            "SELECT seq FROM `conveyorBoxes` "
            "WHERE seq < %d "
            "AND area='pfi' "
            "AND line=%d "
            "AND status = 0 "
            "ORDER BY seq LIMIT 1", seq, line );
      if (!valid_str(val))
         break;

      lost_seq = atoi( val );
      Alert( "carton [%d] detected as lost", lost_seq );
      sql_query(
            "UPDATE `conveyorBoxes` SET "
            "status = %d, "
            "description = 'lost in tracking', "
            "decideStamp = NOW() "
            "WHERE seq = %d", CODE_LOST, lost_seq );
     val = util_carton_get(lost_seq, "refValue");
     if(valid_str(val)) {
        sql_query(
          "REPLACE rdsCartonData "
          "SET cartonSeq=%d, " 
          "dataType='status', "
          "dataValue='errorTracking' ", 
          util_carton_get_int(lost_seq, "refValue")
        );
        sql_query(
          "REPLACE INTO rdsCartonData (cartonSeq, dataType, dataValue) "
          "SELECT %d, docType, 'error' FROM rdsDocuments "
          "WHERE refValue=%d AND docType <> 'shippingLabel' ",
          util_carton_get_int(lost_seq, "refValue"),
          util_carton_get_int(lost_seq, "refValue")
        );
        sql_query(
          "REPLACE rdsCartonData "
          "SET cartonSeq=%d, " 
          "dataType='insertStatus', "
          "dataValue='error' ", 
          util_carton_get_int(lost_seq, "refValue")
        );
      }
   }

   if(valid_str(status)) {
      sql_query(
        "REPLACE rdsCartonData "
        "SET cartonSeq=%d, "
        "dataType='insertStatus', "
        "dataValue='%s'", 
        carton_seq, status
      );
      if(strcmp(status,"ok")==0) {
        sql_query(
          "REPLACE INTO rdsCartonData (cartonSeq, dataType, dataValue) "
          "SELECT %d, docType, 'ok' FROM rdsDocuments "
          "WHERE refValue=%d AND docType <> 'shippingLabel' ",
          util_carton_get_int(seq, "refValue"),
          util_carton_get_int(seq, "refValue")
        );
      }
   }

   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );


   util_box_clear( box );
}


@ Determine the carton status.
@<determine status@>=
{
   char result_name[ BUF_LEN + 1 ];
   char result_status[ BUF_LEN + 1 ];
   char result_value[ BUF_LEN + 1 ];

   code = get_carton_result( seq, result_name, result_status,
         result_value );

   ctr_incr( "/%s", area );
   if (code >= 0) {
      Trace( "%03d: %s for [%d] (code %d)", box, result_value, seq, code );
      ctr_incr( "/%s/pass", area );
   } else {
      Alert( "%03d: %s for [%d] (code %d)", box, result_value, seq, code );
      ctr_incr( "/%s/fail", area );
      ctr_incr( "/%s/fail/%s", area, result_name );
   }

   // update the carton
   strcpy(barcode, util_carton_get(seq,"barcode"));
   barcode[MSG_LEN]='\0';
   util_update_description( seq, area, result_value );
   sql_query(
         "UPDATE `conveyorBoxes` SET "
         "status = %d "
         "WHERE seq = %d", code, seq );
}

@ Determine the processing status for a carton.
@<Functions@>+=
int get_carton_result( int seq, char result_name[], char result_status[],
      char result_value[] ) {	  
	  
   int err, code;

   strcpy( result_name, "" );
   strcpy( result_status, "" );
   strcpy( result_value, "" );
   

   err = sql_query(
         "SELECT name, status, value FROM cartonStatus "
         "WHERE seq = %d "
         "AND status IN ('pending', 'failed') "
         "ORDER BY ordinal LIMIT 1",
         seq );

   if (err) {
      strcpy( result_name, "unknown" );
      strcpy( result_value, "unable to determine carton status" );
      Alert( "sql error (%d) determining carton status for [%d]",
            err, seq );
      return CODE_ERROR;
   }

   if (sql_rowcount() == 0) {
      strcpy( result_name, "success" );
      strcpy( result_value, "carton processed successfully" );
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

   return CODE_ERROR;
}
@ Prototype the function.
@<Prototypes@>+=
int get_carton_result( int seq, char result_name[], char result_status[],
      char result_value[] );

@* Index.
