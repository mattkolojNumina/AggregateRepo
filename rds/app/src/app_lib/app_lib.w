%
%   app_lib.w -- application-specific utility functions
%
%   Author: Adam Marshall
%
%   History:
%      2008-01-08 -AHM- init
%      2015-02-23 -AHM/RME- moved common utility functions to rds_util lib
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
%     (C) Copyright 2008-2015 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{app\_lib}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library contains application-specific constants and utility functions.

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
\centerline{Control Revision: $ $Revision: 1.37 $ $}
\centerline{Control Date: $ $Date: 2019/03/15 19:36:54 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This library contains application-specific constants and utility functions.

@c
static char rcsid[] = "$Id: app_lib.w,v 1.37 2019/03/15 19:36:54 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Exported Functions@>@;
@<Functions@>@;


@ The project header file, included by all applications.
@(app.h@>=
#ifndef __APP_H
#define __APP_H

@<Project Constants@>@;
@<Exported Prototypes@>@;

#endif

@ General constants shared among project apps.
@<Project Constants@>+=
#define LPN_LEN1       3  // length of carton lpn barcode, like 010, 011, ...
#define LPN_LEN2       4  // length of test lpn barcode, like 1234678TEST
#define TOTE_LPN_LEN   4  // length of tote lpn barcode, like T001, T002, ...
#define HINT_LEN      64  // max length of the box hint


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <unistd.h>

#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_util.h>

#include "app.h"


@ Definitions.
@<Defines@>+=
#define BUF_LEN   32  // length of small statically allocated strings
#define MSG_LEN  256  // length of longer message strings


@* Application-specific utilities.

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * app_get_carton_val( const char carton_id[], const char name[] ) {
   return sql_getvalue(
         "SELECT `%s` FROM cartons "
         "WHERE cartonId = '%s'",
         name, carton_id );
}
@ Prototype the function.
@<Exported Prototypes@>+=
char * app_get_carton_val( const char carton_id[], const char name[] ); 


@ Update the carton description and post carton history.
@<Exported Functions@>+=
void app_update_description( const char area[], int seq,
      const char hist_code[], const char *fmt, ... ) {
   char *val;
   va_list ap;
   char description[ MSG_LEN + 1 ];
   char id[ BUF_LEN + 1 ];

   va_start( ap, fmt );
   vsnprintf( description, MSG_LEN, fmt, ap );
   description[ MSG_LEN ] = '\0';
   va_end( ap );

   //val = util_carton_get( area, seq, "barcode" );
   ////if (val == NULL || strlen( val ) == 0 || atoi( val ) == 0)
   ////  sprintf( id, "seq%d", seq );
   ////else
   //   strcpy( id, val );

   val = util_carton_get( area, seq, "cartonId" );
   if( val == NULL || strlen( val ) == 0 || atoi( val ) == 0 )
     sprintf( id, "seq%d", seq );
   else strcpy( id, val );

   util_update_description( area, seq, id, hist_code, description );
}
@ Prototype the function.
@<Exported Prototypes@>+=
void app_update_description( const char area[], int seq,
      const char hist_code[], const char *fmt, ... );


@ Update the carton description and post carton history.
@<Exported Functions@>+=
void app_update_hist( const char area[], int seq, int carton_seq ) {
   if (area == NULL || strlen( area ) == 0 || seq <= 0 ||
         carton_seq <= 0)
      return;

   sql_query(
         "UPDATE cartonLog SET "
         "id = '%d', "
         "stamp = stamp "
         "WHERE id = 'seq%d'",
         carton_seq, seq );

}
@ Prototype the function.
@<Exported Prototypes@>+=
void app_update_hist( const char area[], int seq, int carton_seq );


@ Get the mouse-over hint for a box.
@<Exported Functions@>+=
char *app_get_hint( int box ) {
   int seq = -1;
   static char hint[ HINT_LEN + 1 ];
   char *val, area[ BUF_LEN + 1 ];

   if( box < MIN_TRAK_BOX || box > MAX_TRAK_BOX ) strcpy( hint, "" );
   else if( ( seq = util_box_get_int( box, "seq" ) ) <= 0 )
      snprintf( hint, HINT_LEN, "<%d>", box );
   else {
      val = util_box_get( box, "area" );
      if( val != NULL && strlen( val ) > 0 ) {
        strncpy( area, val, sizeof( area ) - 1 );
        area[ sizeof( area ) - 1 ] = '\0';

        val = util_carton_get( area, seq, "barcode" );
        if( val != NULL && strlen( val ) > 0 )
           snprintf( hint, HINT_LEN, "[%s]", val );
        else snprintf( hint, HINT_LEN, "carton %d", seq );
      }
      else snprintf( hint, HINT_LEN, "carton %d", seq );
   }
   hint[ HINT_LEN ] = '\0';
   return( hint );
}
@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_hint( int box );


@ Parse a scanner message for valid lpn barcode(s).
@<Exported Functions@>+=
int app_parse_scan( const char scan_msg[], char barcode[] ) {
   int num_valid;
   char *str, *str_ptr;

   num_valid = 0;
   strcpy( barcode, "" );

   str = strdup( scan_msg );
   str_ptr = str;
   while (str_ptr != NULL) {
      char *candidate = strsep( &str_ptr, "," );

      //valid barcode:
      // a) fixed length 3, all digits
      // b) fixed length 4, starting with T
      // c) fixed length 12, ending with TEST
      // d) RECYCLE barcode
      if( ( strlen( candidate ) == LPN_LEN1 && atoi( candidate + 1 ) > 0 ) ||
          ( strlen( candidate ) == TOTE_LPN_LEN &&
            strstr( candidate, "T" ) == candidate ) ||
          ( strlen( candidate ) == LPN_LEN2 &&
            strstr( candidate + LPN_LEN2 - 4, "TEST" ) != NULL ) ||
          strcasecmp( candidate, "RECYCLE" ) == 0 ) {
         Inform( "     valid barcode [%s]", candidate );
         strcpy( barcode, candidate );
         num_valid++;
      } else {
         Inform( "     ignore barcode [%s]", candidate );
      }
   }
   free( str );

   return num_valid;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_parse_scan( const char scan_msg[], char barcode[] );


@ Generate a simple label with a barcode and/or a message.
@<Exported Functions@>+=
void app_generate_simple_label( char label[], const char barcode[],
      const char msg[] ) {
   char tmp[ 2 * MSG_LEN + 1 ];

   sprintf( label,
         "^XA\n"
         "^LH0,0^FS\n" );

   if (strlen( barcode ) > 0) {
      sprintf( tmp,
            "^FO200,400^BY3,,102\n"
            "^BCN^FD%s^FS\n",
            barcode );
      strcat( label, tmp );
   }

   if (strlen( msg ) > 0) {
      sprintf( tmp,
            "^FO100,600^A0N,30,30^FB600,8^FD%s^FS\n",
            msg );
      strcat( label, tmp );
   }

   strcat( label, "^XZ\n" );
}
@ Prototype the function.
@<Exported Prototypes@>+=
void app_generate_simple_label( char label[], const char barcode[],
      const char msg[] );


@ Determine if labels are ready for printing.  Returns ordinal (page
number) of next ready label; otherwise returns -1 on error or 0 if
no label is ready.
@<Exported Functions@>+=
int app_label_ready_( int seq, const char printer[] ) {
   char *val;
   char status_name[ BUF_LEN + 1 ];
   char barcode[ BUF_LEN + 1 ];
   int ordinal;

   strcpy(barcode, "");
/*
   // special logic for top+side labels
   strcpy( status_name, "label" );
   if (strcmp( printer, "prn2" ) == 0)
      strcat( status_name, "2" );

   val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, status_name );
   if (val == NULL || strcmp( val, "failed" ) == 0)
      return -1;

   if (strcmp( val, "complete" ) != 0)
      return 0;
*/
   val = util_carton_get( "xpal", seq, "barcode" );
   if (val != NULL && strlen( val ) > 0) {
      strncpy( barcode, val, BUF_LEN );
      barcode[ BUF_LEN ] = '\0';
   }
Inform( "app_label_ready() barcode [%s]", barcode );

   val = sql_getvalue(
         "SELECT ordinal FROM labels "
         "WHERE barcode = '%s' "
         "AND printer = '%s' "
         "AND printed = 'no' "
         "ORDER BY ordinal LIMIT 1",
         barcode, printer );
   if (val == NULL || (ordinal = atoi( val )) <= 0)
      return -1;

Inform( "app_label_ready() ordinal [%d]", ordinal );
   return ordinal;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_label_ready_( int seq, const char printer[] );


@ Determine if labels are ready for printing.  Returns ordinal (page
number) of next ready label; otherwise returns -1 on error or 0 if
no label is ready.
@<Exported Functions@>+=
int app_label_ready( int seq, const char printer[] ) {
   char *val;
   char carton_id[ BUF_LEN + 1 ];
   int ordinal;

   Inform( "  checking if label is ready..." );
   val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = 'label'",
         seq );
   if (val == NULL || strcmp( val, "failed" ) == 0)
      return -1;

   if (strcmp( val, "complete" ) != 0)
      return 0;

   sprintf( carton_id, "%d", seq );
   val = sql_getvalue(
         "SELECT ordinal FROM labels "
         "WHERE cartonId = '%s' "
         "AND printer = '%s' "
         "AND printed = 'no' "
         "ORDER BY ordinal LIMIT 1",
         carton_id, printer );
   if (val == NULL || (ordinal = atoi( val )) <= 0)
      return -1;

   return ordinal;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_label_ready( int seq, const char printer[] );



@ Get a label for printing.
@<Exported Functions@>+=
int app_get_label_( int seq, const char printer[], char label[], int *len ) {
   int err, ordinal, llen;
   char barcode[ BUF_LEN + 1 ];
   char *val;

   strcpy( label, "" );
   strcpy(barcode, "");

   val = util_carton_get( "xpal", seq, "barcode" );
   if (val != NULL && strlen( val ) > 0) {
      strncpy( barcode, val, BUF_LEN );
      barcode[ BUF_LEN ] = '\0';
   }

   err = sql_query(
         "SELECT ordinal, zpl FROM labels "
         "WHERE barcode = '%s' "
         "AND printer = '%s' "
         "AND printed = 'no' "
         "ORDER BY ordinal LIMIT 1",
         barcode, printer );
   if (err) {
      Alert( "sql error retrieving label data, err = %d", err );
      return 0;
   }
   if (sql_rowcount() != 1)
      return 0;

   ordinal = atoi( sql_get( 0, 0 ) );
   val = sql_getlen( 0, 1, &llen );
   if (llen > *len) {
      *len = 0;
      return 0;
   }
   memcpy( label, val, llen );
   *len = llen;

   return ordinal;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_label_( int seq, const char printer[], char label[], int *len );


@ Get a label for printing.
@<Exported Functions@>+=
int app_get_label( int seq, const char printer[], char label[], int *len ) {
   int err, ordinal, llen;
   char *val;
   char carton_id[ BUF_LEN + 1 ];
   int i;

   Inform( "  getting label..." );
   strcpy( label, "" );

   sprintf( carton_id, "%d", seq );
   err = sql_query(
         "SELECT ordinal, zpl FROM labels "
         "WHERE cartonId = '%s' "
         "AND printer = '%s' "
         "AND printed = 'no' "
         "ORDER BY ordinal LIMIT 1",
         carton_id, printer );

   if (err) {
      Alert( "sql error retrieving label data, err = %d", err );
      return 0;
   }

   if (sql_rowcount() != 1)
      return 0;

   ordinal = atoi( sql_get( 0, 0 ) );
   val = sql_getlen( 0, 1, &llen );
   if (llen > *len) {
      *len = 0;
      return 0;
   }
   bcopy( val, label, llen );

   // cleanup ZPL
   llen = zpl_remove_safe( label, llen, "^MCN" );  // map clear - no
   llen = zpl_remove_safe( label, llen, "^MD" );  // media darkness
   llen = zpl_remove_safe( label, llen, "^MM" );  // media mode
   llen = zpl_remove_safe( label, llen, "^MN" );  // media tracking
   llen = zpl_remove_safe( label, llen, "^PM" );  // mirror image
   llen = zpl_remove_safe( label, llen, "^PO" );  // print orientation
   llen = zpl_remove_safe( label, llen, "^PQ" );  // print quantity
   llen = zpl_remove_safe( label, llen, "^PR" );  // print rate
   llen = zpl_remove_safe( label, llen, "^PW" );  // print width
   llen = zpl_remove_safe( label, llen, "~SD" );  // set darkness

   *len = llen;

   if (*len < 40)
      Inform( "label data [%s]", label );

   return ordinal;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_label( int seq, const char printer[], char label[], int *len );



@ Mark a label as successfully printed.
@<Exported Functions@>+=
void app_label_printed_( int seq, const char printer[], int ordinal ) {
   char barcode[ BUF_LEN + 1 ];
   char *val;

   strcpy(barcode, "");

   val = util_carton_get( "xpal", seq, "barcode" );
   if (val != NULL && strlen( val ) > 0) {
      strncpy( barcode, val, BUF_LEN );
      barcode[ BUF_LEN ] = '\0';
   }

   sql_query(
         "UPDATE labels SET "
         "printed = 'yes' "
         "WHERE barcode = '%s' "
         "AND printer = '%s' "
         "AND ordinal = %d",
         barcode, printer, ordinal );

   app_update_description( "xpal", seq, "xpal",
         "printing complete at %s", printer );
}
@ Prototype the function.
@<Exported Prototypes@>+=
void app_label_printed_( int seq, const char printer[], int ordinal );



@ Mark a label as successfully printed.
@<Exported Functions@>+=
void app_label_printed( int seq, const char printer[], int ordinal ) {
   char *val, carton_id[ BUF_LEN + 1 ];

   Inform( "  marking label page %d as printed...", ordinal );

   sprintf( carton_id, "%d", seq );
   sql_query(
         "UPDATE labels SET "
         "printed = 'yes' "
         "WHERE cartonId = '%s' "
         "AND printer = '%s' "
         "AND ordinal = %d",
         carton_id, printer, ordinal );

   app_update_description( "xpal", seq, printer,
                           "label %d printed at %s", ordinal, printer );

   val = sql_getvalue( "SELECT COUNT(*) FROM labels WHERE cartonId = '%s' AND "
                       "printer = '%s' AND printed = 'no'", carton_id, printer);
   if( val != NULL && atoi( val ) == 0 )
      util_carton_set( "xpal", seq, "printed", "1" );
}
@ Prototype the function.
@<Exported Prototypes@>+=
void app_label_printed( int seq, const char printer[], int ordinal );



@ Remove ZPL commands by replacing the command with spaces (safe for
binary input).
@<Functions@>+=
int zpl_remove_safe( char *pszZpl, int len ,const char *pszZplCmd ) {
   char *pc ;
   int i ;
   int cmd_len = strlen(pszZplCmd) ;
   for (pc = pszZpl,i=0 ; i < len ; i++,pc++) {
      if (!strncmp(pc,pszZplCmd,cmd_len)) {
         int j ;
         for (j=0 ; j < cmd_len ; j++) *pc++ = 0x20 ;
         while (*pc != '^') *pc++ = 0x20 ;
         return len;
      }
   }

   return len;
}
@ Prototype the function.
@<Prototypes@>+=
int zpl_remove_safe( char *pszZpl, int len ,const char *pszZplCmd );



@ Determine the sort lane.
@<Exported Functions@>+=
int app_get_lane( const char area[], const char logical[],
      char description[] ) {
   int lane = -1;
   int err;

   err = sql_query(
         "SELECT physical, description FROM cfgLanes "
         "WHERE area = '%s' "
         "AND logical = '%s'",
         area, logical );
   if (err) {
      strcpy( description, "unknown" );
      Alert( "sql error (%d) determining lane for [%s/%s]",
            err, area, logical );
      return 0;
   }

   lane = atoi( sql_get( 0, 0 ) );
   strncpy( description, sql_get( 0, 1 ), MSG_LEN );
   description[ MSG_LEN ] = '\0';
   Trace( "     sort lookup: %s/%s -> %s (%d)",
         area, logical, description, lane );

   return lane;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_lane( const char area[], const char logical[],
      char description[] );


@ Placeholder for internal function.
@<Functions@>+=
void placeholder( void ) {
}
@ Prototype the function.
@<Prototypes@>+=
void placeholder( void );

@* Index.
