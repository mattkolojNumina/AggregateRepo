%
%   rds_util.w -- general-purpose utility functions
%
%   Author: Will Warden
%
%   History:
%      2023-04-11 -WGW- Init, for use with conveyorBoxes table
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
%     (C) Copyright 2008-2023 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{rds\_util}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library contains general constants and utility functions.

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
\centerline{Control Revision: $ $Revision: 1.4 $ $}
\centerline{Control Date: $ $Date: 2023/08/23 22:33:42 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This library contains general constants and utility functions.

@c
static char rcsid[] = "$Id: rds_util.w,v 1.4 2023/08/23 22:33:42 rds Exp $";
@<Includes@>@;
@<Defines@>@;

@<Exported Functions@>@;
#ifdef TRAK
@<Exported TRAK Functions@>@;
#endif  // TRAK


@ The project header file, included by all applications.
@(rds_util.h@>=
#ifndef __RDS_UTIL_H
#define __RDS_UTIL_H

@<General Constants@>@;
@<Exported Prototypes@>@;
#ifdef TRAK
@<Exported TRAK Prototypes@>@;
#endif  // TRAK

#endif


@ Constants for general use.
@<General Constants@>=
#define FALSE  0
#define TRUE   (!FALSE)

#define NUL  0x00  // null
#define SOH  0x01  // start of heading
#define STX  0x02  // start of text
#define ETX  0x03  // end of text
#define EOT  0x04  // end of transmission
#define ACK  0x06  // acknowledge
#define LF   0x0A  // line feed, \n
#define CR   0x0D  // carriage return, \r
#define NAK  0x15  // negative acknowledge
#define FS   0x1C  // file separator
#define GS   0x1D  // group separator
#define RS   0x1E  // record separator
#define US   0x1F  // unit separator

#define MIN_TRAK_BOX    1
#define MAX_TRAK_BOX  999
#define TRAK
#define SQL_LEN   1024

#define DEFAULT_CONSEC  5


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/time.h>  // for gettimeofday()

#include <rds_evt.h>
#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_ctr.h>

#include "rds_util.h"

#ifdef TRAK
#include <rds_trak.h>
#endif


@ Definitions.
@<Defines@>+=
#define BUF_LEN   32  // length of small statically allocated strings
#define HOST_LEN  64  // length of hostname
#define MSG_LEN  256  // length of longer message strings
#define HINT_LEN  64

#define SLEEP_DURATION   50  // sleep duration while waiting (msec)
#define TIMEOUT        5000  // max time before releasing (msec)
#define TRAK


@* General utilities.

@ Make sure the box ID is valid
@<Exported Functions@>+=
int util_valid_box( int box, const char name[] ) {

   if (box <= 0 || box > 999) {
      Alert( "invalid box [%d]", box );
      util_zone_release( name );
      return FALSE;
   }

   return TRUE;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_valid_box( int box, const char name[] );

@ Sanity check for all but the initial zone handler for a conveyor process 
@<Exported Functions@>+=
int util_valid_seq( int box, int *seq, const char name[] ) {

   if (!util_valid_box(box, name)) {
      return FALSE;
   }

   *seq = util_box_get_int( box, "seq" );
   if (*seq <= 0) {
      Alert( "%03d: invalid carton [%d]", box, *seq );
      util_zone_release( name );
      return FALSE;
   }

   return TRUE;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_valid_seq( int box, int *seq, const char name[] );

@ Get the value of a control parameter from the database; if not found,
the specified default is returned.
@<Exported Functions@>+=
char * util_get_control( const char zone[], const char control[],
      const char otherwise[] ) {
   static char value[ BUF_LEN + 1 ];
   char *val;

   val = sql_getcontrol( zone, control );
   if (val == NULL || strlen( val ) == 0)
      strncpy( value, otherwise, BUF_LEN );
   else
      strncpy( value, val, BUF_LEN );
   value[ BUF_LEN ] = '\0';

   return value;
}
@ Prototype the function.
@<Exported Prototypes@>+=
char * util_get_control( const char zone[], const char control[],
      const char otherwise[] );

@ Get the value of a control parameter from the database; if not found,
the specified default is returned.
@<Exported Functions@>+=
int util_get_int_control( const char zone[], const char control[],
      int otherwise ) {
   static int value;
   char *val;

   val = sql_getcontrol( zone, control );
   if (val == NULL || strlen( val ) == 0)
      value = otherwise;
   else
      value = atoi( val );

   return value;
}

@ Prototype the function.
@<Exported Prototypes@>+=
int util_get_int_control( const char zone[], const char control[],
      int otherwise );

@ Get the value of a control parameter from the database; if not found,
the specified default is returned.
@<Exported Functions@>+=
int util_set_int_control( const char zone[], const char name[],
      int value ) {
   
   char sql[SQL_LEN+1];
   char host[ HOST_LEN + 1 ];
   int err;

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';
   
   snprintf(sql, SQL_LEN,
      "REPLACE controls SET host = '%s', zone = '%s', name = '%s', value = '%d'",
      host, zone, name, value);

   err = sql_query(sql);

   if(err) {
      Alert("%s", sql);
      return FALSE;
   }

   return TRUE;

   /*  "REPLACE runtime SET "
         "name = 'cartonSequence', "
         "value = '%ld'",*/

         

   /*if (box < MIN_TRAK_BOX || box > MAX_TRAK_BOX || strlen( name ) == 0) {
      Alert( "invalid params in util_box_set: box [%d], name [%s]", box, name );
      return;
   }

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   sql_query(
         "REPLACE boxData SET "
         "host = '%s', "
         "box = %d, "
         "name = '%s', "
         "value = '%s'",
         host, box, name, value );*/
}

@ Prototype the function.
@<Exported Prototypes@>+=
int util_set_int_control( const char zone[], const char name[],
      int value );

@ Check if str is non-null and length > 0 
@<Exported Functions@>+=
int util_valid_str( const char val[] ) {
   if(val == NULL)
      return FALSE;
   if(strlen(val) > 0)
      return TRUE;

   return FALSE;
}

@ Prototype the function.
@<Exported Prototypes@>+=
int util_valid_str( const char val[] );

@ Get the value of a control parameter from the database; if not found,
the specified default is returned.
@<Exported Functions@>+=
float util_get_float_control( const char zone[], const char control[],
      float otherwise ) {
   static int value;
   char *val;

   val = sql_getcontrol( zone, control );
   if (val == NULL || strlen( val ) == 0)
      value = otherwise;
   else
      value = atof( val );

   return value;
}
@ Prototype the function.
@<Exported Prototypes@>+=
float util_get_float_control( const char zone[], const char control[],
      float otherwise );
	  
@ Determine elapsed time.
@<Exported Functions@>+=
double util_get_elapsed( struct timeval start_time ) {
   struct timeval current_time;

   gettimeofday( &current_time, NULL );
   return (current_time.tv_sec - start_time.tv_sec) +
         (current_time.tv_usec - start_time.tv_usec) * 0.000001;
}

@ Prototype the function.
@<Exported Prototypes@>+=
double util_get_elapsed( struct timeval start_time );


@* Zone utilities.

@ Check that box is still present in zone.
@<Exported TRAK Functions@>+=
int util_get_zone_box( const char name[] ) {
   int zone_dp = -1;
   int box;
   char zone[BUF_LEN+1];

   util_zone_get( name, zone );
   if (strlen( zone ) == 0) {
      Alert( "no zone specified" );
      return -1;
   }
   zone_dp = dp_handle( zone );
   if (zone_dp < 0) {
      Alert( "unable to obtain dp for zone %s", zone );
      return -1;
   }

   dp_registerget( zone_dp, 0, &box ); //REG_BOX = 0
   return box;
}

@ Prototype the function.
@<Exported TRAK Prototypes@>+=
int util_get_zone_box( const char name[] );

@ Check that box is still present in zone.
@<Exported TRAK Functions@>+=
int util_get_zoneDP_box( int zone_dp ) {
   int box;
   
   if (zone_dp < 0) {
      Alert( "Invalid zoneDP", zone_dp );
      return -1;
   }

   dp_registerget( zone_dp, 0, &box ); //REG_BOX = 0
   return box;
}

@ Prototype the function.
@<Exported TRAK Prototypes@>+=
int util_get_zoneDP_box( int zoneDP );

@ Get the zone for an application.
@<Exported TRAK Functions@>+=
void util_zone_get( const char app[], char zone[] ) {

      util_strncpy(zone, util_get_control( app, "zone", "" ), BUF_LEN );
      zone[BUF_LEN]='\0';
}

@ Release a zone.
@<Exported TRAK Functions@>+=
void util_zone_release( const char app[] ) {
   static int dp = -1;
   char zone[ BUF_LEN + 1 ];
   char dp_name[ BUF_LEN + 1 ];

   strcpy(zone, "");
   strcpy(dp_name, "");

   if (dp < 0) {
      char *val;

      val = util_get_control( app, "release", "" );
      if (val != NULL && strcmp( val, "none" ) == 0) {
         // release specified as none -- do nothing
         dp = 0;
      } else if (val != NULL && strlen( val ) > 0) {
         dp = dp_handle( val );
         if (dp < 0)
            Alert( "unable to obtain dp for release [%s]", val );
      } else {


         util_zone_get( app, zone );
         if (strlen( zone ) == 0) {
            Alert( "invalid zone for release" );
            return;
         }
         sprintf( dp_name, "%s_go", zone );
         dp = dp_handle( dp_name );
         if (dp < 0)
            Alert( "unable to obtain dp for release [%s]", dp_name );
      }
   }

   Inform( "release carton app[%s] zone[%s] dpName[%s] dp[%d]",
            app, zone, dp_name, dp);

   if (dp > 0) {
      //Inform( "release carton app[%s]", app );
      dp_set( dp, 1 );
   }
   else {
      Alert("Unable to release, invalid DP");
   }
}

@ Fault a zone.
@<Exported TRAK Functions@>+=
void util_zone_fault( const char app[] ) {
   static int dp = -1;

   if (dp < 0) {
      char *val;

      val = util_get_control( app, "fault", "" );
      if (val != NULL && strcmp( val, "none" ) == 0) {
         // fault specified as none -- do nothing
         dp = 0;
      } else if (val != NULL && strlen( val ) > 0) {
         dp = dp_handle( val );
         if (dp < 0)
            Alert( "unable to obtain dp for fault [%s]", val );
      } else {
         char zone[ BUF_LEN + 1 ];
         char dp_name[ BUF_LEN + 1 ];

         util_zone_get( app, zone );
         if (strlen( zone ) == 0) {
            Alert( "invalid zone for fault" );
            return;
         }
         sprintf( dp_name, "%s_fault", zone );
         
         dp = dp_handle( dp_name );
         if (dp < 0)
            Alert( "unable to obtain dp for fault [%s]", dp_name );
      }
   }

   if (dp > 0) {
      Inform( "set fault" );
      dp_set( dp, 1 );
   }
}

@ Prototype the functions.
@<Exported TRAK Prototypes@>+=
void util_zone_get( const char app[], char zone[] );
void util_zone_release( const char app[] );
void util_zone_fault( const char app[] );

@ Get the mouse-over hint for a box.
@<Exported Functions@>+=
char *util_get_hint( int box ) {
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

        val = util_carton_get( seq, "barcode" );
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
char *util_get_hint( int box );


@* Box/carton utilities.

@ Set a box data value.
@<Exported Functions@>+=
void util_box_set( int box, const char name[], const char value[] ) {
   char host[ HOST_LEN + 1 ];

   if (box < MIN_TRAK_BOX || box > MAX_TRAK_BOX || strlen( name ) == 0) {
      Alert( "invalid params in util_box_set: box [%d], name [%s]", box, name );
      return;
   }

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   sql_query(
         "REPLACE boxData SET "
         "host = '%s', "
         "box = %d, "
         "name = '%s', "
         "value = '%s'",
         host, box, name, value );
}

@ Set an integer box data value.
@<Exported Functions@>+=
void util_box_set_int( int box, const char name[], int value ) {
   char sval[ BUF_LEN + 1 ];

   sprintf( sval, "%d", value );
   util_box_set( box, name, sval );
}

@ Get a box data value.
@<Exported Functions@>+=
char * util_box_get( int box, const char name[] ) {
   char host[ HOST_LEN + 1 ];

   if (box < MIN_TRAK_BOX || box > MAX_TRAK_BOX || strlen( name ) == 0) {
      Alert( "invalid params in util_box_get: box [%d], name [%s]", box, name );
      return NULL;
   }

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   return sql_getvalue(
         "SELECT value FROM boxData "
         "WHERE host = '%s' "
         "AND box = %d "
         "AND name = '%s'",
         host, box, name );
}

@ Get an integer box data value.
@<Exported Functions@>+=
int util_box_get_int( int box, const char name[] ) {
   char *val;
   int ival = -1;

   val = util_box_get( box, name );
   if (val != NULL && strlen( val ) > 0)
      ival = atoi( val );

   return ival;
}

@ Clear all data for a box.
@<Exported Functions@>+=
void util_box_clear( int box ) {
   char host[ HOST_LEN + 1 ];

   if (box < MIN_TRAK_BOX || box > MAX_TRAK_BOX) {
      Alert( "invalid params in util_box_clear: box [%d]", box );
      return;
   }

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   sql_query(
         "DELETE FROM boxData "
         "WHERE host = '%s' "
         "AND box = %d",
         host, box );
}

@ Prototype the functions.
@<Exported Prototypes@>+=
void util_box_set( int box, const char name[], const char value[] );
void util_box_set_int( int box, const char name[], int value );
char * util_box_get( int box, const char name[] );
int util_box_get_int( int box, const char name[] );
void util_box_clear( int box );


@ Generate and return a carton sequence number.
@<Exported Functions@>+=
int util_create_seq( int box, const char area[] ) {
   char *val;
   int seq = -1;
   int err;
   char sql[SQL_LEN+1];
   
   // clear any previous box data
   util_box_clear( box );

   val = sql_getvalue( "SELECT GET_LOCK( 'cartonSequenceLock', 60 )" );

   if (val == NULL || atoi( val ) <= 0)
      return -1;
	  
   sql_query(
         "INSERT `conveyorBoxes` SET "
         "box = %d, area='%s'", box, area 
   );
   val = sql_getvalue(
      "SELECT MAX(seq) FROM conveyorBoxes "
      "WHERE box=%d and area='%s'", box, area
   );
   
   if((val!=NULL) && (strlen(val)>0)) {
      seq=atoi(val);
   }
   util_box_set_int( box, "seq", seq );
   Inform( "%03d: box created with seq [%d]", box, seq );

   // initialize carton status
   /*
   sql_query(
            "REPLACE INTO cartonStatus "
            "SELECT %d, area, ordinal, name, status, value, NOW() "
            "FROM cartonStatusInit "
            "WHERE area = '%s'",
            seq, area );*/

   snprintf(sql, SQL_LEN,
      "REPLACE INTO cartonStatus "
            "SELECT %d, area, name, ordinal, status, value, NOW() "
            "FROM cartonStatusInit "
            "WHERE area = '%s'",
            seq, area );

   err = sql_query(sql);

   if(err) {
      Alert("%03d: Error inserting record into cartonStatus", box);
      Alert("%s", sql);
   }

   val = sql_getvalue( "SELECT RELEASE_LOCK( 'cartonSequenceLock' )" );

   return seq;
}


@ Set the value of a carton parameter.
@<Exported Functions@>+=
void util_carton_set( int seq, const char name[],
      const char value[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_carton_set: "
            "seq [%d], name [%s]", seq, name );
      return;
   }

   sql_query(
         "UPDATE conveyorBoxes SET "
         "`%s` = '%s' "
         "WHERE seq = %d",
         name, value, seq );
}

void util_carton_set_int( int seq, const char name[],
      const int value ) {
   if (seq <= 0 ) {
      Alert( "invalid params in util_carton_set: "
            "seq [%d], name [%s]", seq, name );
      return;
   }

   sql_query(
         "UPDATE conveyorBoxes SET "
         "`%s` = '%d' "
         "WHERE seq = %d",
         name, value, seq );
}

void util_carton_set_float( int seq, const char name[],
      const float value ) {
   if (seq <= 0 ) {
      Alert( "invalid params in util_carton_set: "
            "seq [%d], name [%s]", seq, name );
      return;
   }

   sql_query(
         "UPDATE conveyorBoxes SET "
         "`%s` = '%.3f' "
         "WHERE seq = %d",
         name, value, seq );
}


@ Set the value of a carton parameter.
@<Exported Functions@>+=
void util_set_stamp( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_set_stamp: "
            "seq [%d], name [%s]", seq, name );
      return;
   }

   sql_query(
         "UPDATE conveyorBoxes SET "
         "`%s` = NOW() "
         "WHERE seq = %d",
         name, seq );
}

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * util_carton_get( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_carton_get: "
            "seq [%d], name [%s]", seq, name );
      return NULL;
   }
   char *val = sql_getvalue(
         "SELECT `%s` FROM conveyorBoxes "
         "WHERE seq = %d",
         name, seq );
   if(util_valid_str(val)) return val;
   else return NULL;
}

@ Get the int value of a carton parameter.
@<Exported Functions@>+=
int util_carton_get_int( int seq, const char name[] ) {
   int result = -1;
   char str_val[BUF_LEN + 1];
   if(util_valid_str(util_carton_get(seq, name))){
       strcpy(str_val, util_carton_get(seq, name));
   }
  if (str_val != NULL && strlen( str_val ) > 0) {
      result =  atoi(str_val);
   }
   return result;
}

@ Get the int value of a carton parameter.
@<Exported Functions@>+=
float util_carton_get_float( int seq, const char name[] ) {
   float result = 0.0f;
   char str_val[BUF_LEN + 1];
   strcpy(str_val, util_carton_get(seq, name));
   if (str_val != NULL && strlen( str_val ) > 0) {
      result = atof(str_val);
   }
   return result;
}

@ Get the value of a carton parameter in rdsCartons.
@<Exported Functions@>+=
char * util_rds_carton_get( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_get: "
            "seq [%d], name [%s]", seq, name );
      return NULL;
   }
   char *val = sql_getvalue(
         "SELECT `%s` FROM rdsCartons "
         "WHERE cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return val;
   else return NULL;
}

@ Get the value of a carton int parameter in rdsCartons.
@<Exported Functions@>+=
int util_rds_carton_get_int( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_get_int: "
            "seq [%d], name [%s]", seq, name );
      return -1;
   }
   char *val = sql_getvalue(
         "SELECT `%s` FROM rdsCartons "
         "WHERE cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return atoi(val);
   else return -1;
}

@ Get the value of a float carton parameter in rdsCartons.
@<Exported Functions@>+=
float util_rds_carton_get_float( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_get_float: "
            "seq [%d], name [%s]", seq, name );
      return -1.0f;
   }
   char *val = sql_getvalue(
         "SELECT `%s` FROM rdsCartons "
         "WHERE cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return atof(val);
   else return -1.0f;
}

@ Get the value of a carton parameter in rdsCartonData.
@<Exported Functions@>+=
char * util_rds_carton_data_get( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_data_get: "
            "seq [%d], name [%s]", seq, name );
      return NULL;
   }
   char *val = sql_getvalue(
         "SELECT dataValue FROM rdsCartonData "
         "WHERE dataType = '%s' "
         "AND cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return val;
   else return NULL;
}

@ Get the value of a carton int parameter in rdsCartonData.
@<Exported Functions@>+=
int util_rds_carton_data_get_int( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_data_get_int: "
            "seq [%d], name [%s]", seq, name );
      return -1;
   }
   char *val = sql_getvalue(
         "SELECT dataValue FROM rdsCartonData "
         "WHERE dataType = '%s' "
         "AND cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return atoi(val);
   else return -1;
}

@ Get the value of a float carton parameter in rdsCartonData.
@<Exported Functions@>+=
float util_rds_carton_data_get_float( int seq, const char name[] ) {
   if (seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_rds_carton_data_get_float: "
            "seq [%d], name [%s]", seq, name );
      return -1.0f;
   }
   char *val = sql_getvalue(
         "SELECT dataValue FROM rdsCartonData "
         "WHERE dataType = '%s' "
         "AND cartonSeq = %d",
         name, seq );
   if(util_valid_str(val)) return atof(val);
   else return -1.0f;
}

@ Prototype the functions.
@<Exported Prototypes@>+=
int util_create_seq( int box, const char area[] );
void util_carton_set( int seq, const char name[],
      const char value[] );
void util_carton_set_int( int seq, const char name[],
      const int value );
void util_carton_set_float( int seq, const char name[],
      const float value );
void util_set_stamp( int seq, const char name[] );
char * util_carton_get( int seq, const char name[] );
int util_carton_get_int( int seq, const char name[] );
float util_carton_get_float( int seq, const char name[] );
char * util_rds_carton_get( int seq, const char name[] );
int util_rds_carton_get_int( int seq, const char name[] );
float util_rds_carton_get_float( int seq, const char name[] );
char * util_rds_carton_data_get( int seq, const char name[] );
int util_rds_carton_data_get_int( int seq, const char name[] );
float util_rds_carton_data_get_float( int seq, const char name[] );


@ Update the processing status for a single item.
@<Exported Functions@>+=
void util_update_status( int seq, const char name[], const char status[],
      const char value[] ) {
   if (seq <= 0 || strlen( name ) == 0 || strlen( status ) == 0) {
      Alert( "invalid params in util_update_status: "
            "seq [%d], name [%s], status [%s]", seq, name, status );
      return;
   }

   sql_query(
         "UPDATE cartonStatus SET "
         "status = '%s', "
         "value = '%s' "
         "WHERE seq = %d "
         "AND name = '%s' ",
         status, value, seq, name );
   
   if (strcmp(status,"failed")==0) {
      int ordinal = 0;
      char *val = sql_getvalue(
         "SELECT ordinal FROM cartonStatus "
         "WHERE seq=%d AND name='%s' ",
         seq, name
      );
      if(val!=NULL && strlen(val) > 0) {
         ordinal = atoi(val);
         sql_query(
               "UPDATE cartonStatus SET "
               "status = 'optional' "
               "WHERE seq = %d "
               "AND ordinal > %d ",
               seq, ordinal
         );
      }
   }   
}

@ Prototype the function.
@<Exported Prototypes@>+=
void util_update_status( int seq, const char name[], const char status[],
      const char value[] );


@ Update the carton description.
@<Exported Functions@>+=
void util_update_description( int seq, const char area[], const char *fmt, ... ) {
   va_list ap;
   char description[ MSG_LEN + 1 ];

   va_start( ap, fmt );
   vsnprintf( description, MSG_LEN, fmt, ap );
   description[ MSG_LEN ] = '\0';
   va_end( ap );

   if (seq > 0) {
      sql_query(
            "UPDATE `conveyorBoxes` SET "
            "description = '%s' "
            "WHERE seq = %d "
            "AND status >= 0",
            description, seq );
   }
   char ref_type[BUF_LEN+1];
   char ref_val[BUF_LEN+1];

   strncpy(ref_val,"",BUF_LEN);
   strncpy(ref_type,"",BUF_LEN);

   util_strcpy(ref_type, util_carton_get(seq, "refType"));
   ref_type[BUF_LEN]='\0';
   util_strcpy(ref_val, util_carton_get(seq, "refValue"));
   ref_val[BUF_LEN]='\0';
   if (util_valid_str( ref_type ) && util_valid_str( ref_val ) && util_valid_str( description ) ) 
      log_post( ref_val, ref_type, area, description );

}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_update_description( int seq, const char area[], const char *fmt, ... );


@ Determine if a subsystem result is required.
@<Exported Functions@>+=
int util_required( int seq, const char name[] ) {
   char *val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   return (val != NULL && strcmp( val, "pending" ) == 0);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_required( int seq, const char name[] );

@ Determine if a subsystem result is required.
@<Exported Functions@>+=
int util_optional( int seq, const char name[] ) {
   char *val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   return (val != NULL && strcmp( val, "optional" ) == 0);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_optional( int seq, const char name[] );

@ Determine if a subsystem result is complete.
@<Exported Functions@>+=
int util_complete( int seq, const char name[] ) {
   char *val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   return (val != NULL && strcmp( val, "complete" ) == 0);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_complete( int seq, const char name[] );

@ Determine if a subsystem result is complete.
@<Exported Functions@>+=
int util_failed( int seq, const char name[] ) {
   char *val = sql_getvalue(
         "SELECT status FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   return (val != NULL && strcmp( val, "failed" ) == 0);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_failed( int seq, const char name[] );

@ Determine if a subsystem result is complete.
@<Exported Functions@>+=
void util_get_status_value( int seq, const char name[], char status_val[]) {
   char *val = sql_getvalue(
         "SELECT value FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   if (val != NULL && strlen(val) == 0) {
      strcpy(status_val,val);
   }
}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_get_status_value( int seq, const char name[], char status_val[]);

@ Preprocess a message acquired from util_poll_for_msg
@<Exported Functions@>+=
int util_read_scanner( int box, int seq, const char name[], const char device[], char msg[] ) {
   int result = -99;
   
   if (device == NULL || strlen( device ) == 0) {
      Alert( "no scanner configured for this process" );
	  return result;
   }
   else if (!util_required(seq, name)) {
      Inform("%03d: %s processing not required for seq %d name[%s]", box, device, seq, name);   
	  result = 0;
	  return result;
   }
   else {
      util_poll_for_msg(box, device, msg);
      Inform( "%03d: %s msg [%s]", box, device, msg );
	  
      char status_val[ BUF_LEN + 1 ];
      char description[ MSG_LEN + 1 ];

      strcpy( status_val, "" );
      strcpy( description, "" );

      //ctr_incr("/%s", device) ;
      //ctr_incr("/%s/read", device) ;

      if (strlen(msg)==0 || msg[0] == '?') {
         if (strlen( msg ) == 0) {
            strcpy( description, "unable to locate induct scanner result" );
            strcpy( status_val, "missing" );
		   	result = -1;
		   } else {
            strcpy( description, "induct scanner noread" );
            strcpy( status_val, "noread" );
			   result = -2;
         }
         //ctr_incr("/%s/read/fail", device) ;
         //ctr_incr("/%s/read/fail/%s", device, status_val);
         util_update_status( seq, name, "failed", status_val);
		   return result;
      } else {
         strcpy(description, "got message from scanner");
         //ctr_incr("/%s/read/pass", device) ;
         util_update_status( seq, name, "complete", msg);   
      } 
      description[MSG_LEN]='\0';
   }
   return 1;
}

@ Prototype
@<Exported Prototypes@>+=
int util_read_scanner( int box, int seq, const char name[], const char device[], char msg[] );

@ Trigger a scale to transmit.
@<Exported Functions@>+=
int util_trigger_scale( const char dev_name[]) {
   int err;

   err = sql_query(
	"UPDATE runtime SET value = CONCAT('W',0x0D) WHERE name = '%s/xmit'", dev_name);
   Alert("UPDATE runtime SET value = CONCAT('W',0x0D) WHERE name = '%s/xmit'", dev_name);

   return !err;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_trigger_scale( const char dev_name[]);

@ Connect to a scale and get the weight
@<Exported Functions@>+=
float util_get_scale_weight(const char area[], int box, int seq, const char dev_name[])
{
   char dev_msg[MSG_LEN+1];
   char description[MSG_LEN+1];
   char status_val[BUF_LEN+1];
   float weight = 0.0f;
   int weight_success = FALSE;
   struct timeval start_time;
   
   gettimeofday( &start_time, NULL );
   
   
   if (dev_name == NULL || strlen(dev_name) == 0)
   {
      Alert( " *** NO SCALE CONFIGURED" );
	  return weight;
   }
   else
   {
      util_poll_for_msg( box, dev_name, dev_msg );
   }

   strcpy(status_val, "");
   strcpy(description, "");

   if (dev_msg == NULL || strlen(dev_msg) == 0)
   {
      sprintf(description, "unable to locate scale result");
      strcpy( status_val, "missing" );
   }
   else
   {
      int n = sscanf(dev_msg, " %f lb", &weight);
	  
      if (n != 1)
	  {
         strcpy(description, "failed to parse weight from scale message");
         strcpy(status_val, "format");
      }
	  else if (weight < 0.0)
	  {
         sprintf(description, "invalid weight %.2f", weight);
         strcpy(status_val, "invalid");
      }
	  else
	  {
         sprintf(description, "recorded weight [%.2f] lbs. from %s", weight, dev_name);
         weight_success = TRUE;
      }
   }

   //ctr_incr("/%s", dev_name);
   if (weight_success)
   {
      //ctr_incr("/%s/pass", dev_name);
	  Trace( "%03d: %s for [%d], %.3f", box, description, seq,
		util_get_elapsed(start_time));
      util_update_status( seq, dev_name, "failed", status_val);		
   }
   else
   {
      Alert( "%03d: %s for [%d], %.3f", box, description, seq,
		util_get_elapsed(start_time));
      //ctr_incr("/%s/fail", dev_name);
      //ctr_incr("/%s/fail/%s", dev_name, status_val);	
      util_update_status( seq, dev_name, "complete", dev_msg);	  
   }
   
   return weight;
}

@ Prototype the function.
@<Exported Prototypes@>+=
float util_get_scale_weight(const char area[], int box, int seq, const char dev_name[]);


@ Poll for a message from a field device (scanner, scale, etc.).  If
the global timeout is reached, mark the message as late.
@<Exported Functions@>+=
void util_poll_for_msg( int box, const char device[], char msg[] ) {
   char *val;
   int timeout = -1;
   struct timeval start_time;

   val = util_get_control( device, "pollTimeout", "" );
   if (val != NULL && strlen( val ) > 0)
      timeout = atoi( val );
   if (timeout <= 0)
      timeout = TIMEOUT;

   gettimeofday( &start_time, NULL );
   strcpy( msg, "" );

   for ( ; ; usleep( SLEEP_DURATION * 1000 ) ) {
      val = util_box_get( box, device );

      if (val != NULL && strlen( val ) > 0) {
         strncpy( msg, val, MSG_LEN );
         msg[ MSG_LEN ] = '\0';
         return;
      }

      if (util_get_elapsed( start_time ) * 1000 > timeout) {
         Alert( "timeout waiting for %s result", device );
         return;
      }
   }
}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_poll_for_msg( int box, const char device[], char msg[] );


@ Clear the count of consecutive failures for a specific item.
@<Exported Functions@>+=
void util_clear_consec( const char item[] ) {
   char host[ HOST_LEN + 1 ];

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   sql_query(
         "REPLACE INTO runtime SET "
         "name = '%s/%s/consec', "
         "value = '0'",
         host, item );
}

@ Increment the count of consecutive failures for a specific item.
@<Exported Functions@>+=
void util_bump_consec( const char item[] ) {
   char host[ HOST_LEN + 1 ];

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   sql_query(
         "UPDATE runtime SET "
         "value = value + 1 "
         "WHERE name = '%s/%s/consec'",
         host, item );
}

@ Checks whether the maximum allowed number of consecutive failures for a
specific item has been exceeded.
@<Exported Functions@>+=
int util_check_consec( const char item[] ) {
   char *val;
   int consec, max_consec;
   char host[ HOST_LEN + 1 ];

   gethostname( host, HOST_LEN );
   host[ HOST_LEN ] = '\0';

   val = sql_getvalue(
         "SELECT value FROM runtime "
         "WHERE name = '%s/%s/consec'",
         host, item );
   consec = (val == NULL || strlen( val ) == 0) ? 0 : atoi( val );

   val = sql_getcontrol( item, "maxConsec" );
   max_consec = (val == NULL || strlen( val ) == 0) ? DEFAULT_CONSEC :
         atoi( val );

   return (max_consec > 0 && consec >= max_consec);
}

@ Increment or clear the count of consecutive failures for a specific
item and take action if the allowed maximum has been exceeded.
@<Exported Functions@>+=
int util_do_consec( const char item[], int success ) {
   int fault;
   char evt[ BUF_LEN + 1 ];

   sprintf( evt, "%s_consec", item );

   if (success) {
      util_clear_consec( item );
      evt_stop( evt );
      fault = FALSE;
   } else {
      util_bump_consec( item );

      fault = util_check_consec( item );
      if (fault) {
         Alert( "maximum allowed %s failures exceeded", item );
         evt_start( evt );
      }
   }

   return fault;
}

@ Prototype the functions.
@<Exported Prototypes@>+=
void util_clear_consec( const char item[] );
void util_bump_consec( const char item[] );
int util_check_consec( const char item[] );
int util_do_consec( const char item[], int success );

@* Other functions

@ Check if the string matches an lpn in the rdsCartons table.
Returns the max cartonSeq for that lpn on a positive match
@<Exported Functions@>+=
int util_is_carton( const char candidate[] ) {
   int carton_seq = 0;
   char *val;

   val = sql_getvalue(
      "SELECT cartonSeq FROM rdsCartons " 
      "WHERE lpn='%s' ORDER BY cartonSeq DESC "
      "LIMIT 1", candidate
   );

   if(util_valid_str(val))
         carton_seq = atoi(val);

   return carton_seq;
}

@ Prototype the function
@<Exported Prototypes@>+=
int util_is_carton( const char candidate[] );

@ Check printer availability.
@<Exported Functions@>+=
int util_folder_avail( const char folder[] ) {
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
@<Exported Prototypes@>+=
int util_folder_avail( const char folder[] );


@ Check printer availability.
@<Exported Functions@>+=
int util_prn_avail( const char prn[] ) {
   char dp_name[TRAK_NAME_LEN+1];
   int dp = -1;
   int avail = TRUE;

   int prnOk = FALSE;
   int prnFaulted = FALSE;
   int prnBypass = FALSE;

   if(strlen(prn) == 0) {
      Alert("util_prn_avail invalid printer");
      return FALSE;
   }

   sprintf(dp_name, "%s_ok", prn);
   dp = dp_handle(dp_name);
   prnOk = dp_get(dp);
   avail &= (dp > 0 && prnOk == 1);

   sprintf(dp_name, "%s_fault", prn);
   dp = dp_handle(dp_name);
   prnFaulted = dp_get(dp);
   avail &= (dp > 0 && prnFaulted == 0);

   sprintf(dp_name, "%s_bypass", prn);
   dp = dp_handle(dp_name);
   prnBypass = dp_get(dp);
   avail &= (dp <= 0 || prnBypass == 0);

   if(!avail)
      Alert("Printer [%s] not available [%d]. Ok[%d] Faulted[%d] Bypass[%d]",
         prn, avail, prnOk, prnFaulted, prnBypass);

   return avail;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_prn_avail( const char prn[] );

@ Null-checked strcpy.
@<Exported Functions@>+=
int util_strcpy( char *dest, const char *src ) {
  if(src != NULL && strlen(src) > 0) {
    strcpy(dest, src);
    return 1;
  }
  return 0;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_strcpy( char *dest, const char *src );

@ Null-checked strncpy.
@<Exported Functions@>+=
int util_strncpy( char *dest, const char *src, int n ) {
  if(src != NULL && strlen(src) > 0) {
    strncpy(dest, src, n);
    return 1;
  }
  return 0;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_strncpy( char *dest, const char *src, int n );

@* ZPL functions.

@ Generate a simple label with a barcode and/or a message.
@<Exported Functions@>+=
void util_generate_simple_label( char label[], const char barcode[],
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
void util_generate_simple_label( char label[], const char barcode[],
      const char msg[] );

@ Determine if label is ready for printing.  Returns docSeq
if label exists, otherwise returns -1
@<Exported Functions@>+=
int util_get_next_doc( int seq, const char printer[] ) {

   char *val;
   char ref_type[BUF_LEN+1];
   char ref_val[BUF_LEN+1];
   strncpy(ref_val,"",BUF_LEN);
   strncpy(ref_type,"",BUF_LEN);
  
   util_strcpy(ref_type, util_carton_get(seq, "refType"));
   ref_type[BUF_LEN]='\0';
   util_strcpy(ref_val, util_carton_get(seq, "refValue"));
   ref_val[BUF_LEN]='\0';

   Inform( "  checking if label is ready..." );

   val = sql_getvalue(
         "SELECT docSeq FROM rdsDocuments "
         "WHERE refType='%s' AND refValue='%s' "
         "AND printerId='%s' AND printed IS NULL "
         "AND hostDoc IS NOT NULL "
         "ORDER BY printSequence LIMIT 1",
         ref_type, ref_val, printer
   );
   if (val == NULL || strlen(val) == 0 || atoi( val ) <= 0)
      return -1;

   return atoi(val);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_get_next_doc( int seq, const char printer[] );

@ Determine if label of chosen type isready for printing.  Returns docSeq
if label exists, otherwise returns -1
@<Exported Functions@>+=
int util_get_doc_seq( int seq, const char doc_type[] ) {

   char *val;
   char ref_type[BUF_LEN+1];
   char ref_val[BUF_LEN+1];
   strncpy(ref_val,"",BUF_LEN);
   strncpy(ref_type,"",BUF_LEN);

   util_strcpy(ref_type, util_carton_get(seq, "refType"));
   ref_type[BUF_LEN]='\0';
   util_strcpy(ref_val, util_carton_get(seq, "refValue"));
   ref_val[BUF_LEN]='\0';

   Inform( "  checking if label is ready..." );

   val = sql_getvalue(
         "SELECT docSeq FROM rdsDocuments "
         "WHERE refType='%s' AND refValue='%s' "
         "AND docType='%s' "
         "AND hostDoc IS NOT NULL LIMIT 1",
         ref_type, ref_val, doc_type
   );
   if (val == NULL || strlen(val) == 0 || atoi( val ) <= 0)
      return -1;

   return atoi(val);
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_get_doc_seq( int seq, const char doc_type[] );


@ Get a label for printing.
@<Exported Functions@>+=
int util_get_label( int seq, const char printer[],
      char label[], int *len ) {
   int err, ordinal, llen;
   char *val;

   Inform( "  getting label..." );
   strcpy( label, "" );

   int doc_seq = util_get_next_doc(seq, printer);
   if(doc_seq <= 0) return -1;

   err = sql_query(
         "SELECT FROM_BASE64(IF(printDoc IS NOT NULL, printDoc, hostDoc)) AS document "
         "FROM rdsDocuments "
         "WHERE docSeq=%d ",
         doc_seq );

   if (err) {
      Alert( "sql error retrieving label data, err = %d", err );
      return -1;
   }

   if (sql_rowcount() != 1)
      return -1;

   val = sql_getlen( 0, 0, &llen );
   if (llen > *len) {
      *len = 0;
      return -1;
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

   //if (*len < 40)
   //   Inform( "label data [%s]", label );

   //Inform( "label data [%s]", label );

   return doc_seq;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_get_label( int seq, const char printer[],
      char label[], int *len );

@ Assign a printer for a label
@<Exported Functions@>+=
void util_assign_printer(int seq, const char doc_type[],
      const char printer[] ) {
   int doc_seq = util_get_doc_seq(seq, doc_type);
   Inform( "  assigning %s for seq %d to %s", doc_type,
         seq, printer );
   sql_query(
      "UPDATE rdsDocuments SET "
      "printerId='%s', printed = NULL "
      "WHERE docSeq=%d ",
      printer, doc_seq );
}

@ Prototype the function.
@<Exported Prototypes@>+=
void util_assign_printer( int seq, const char doc_type[],
      const char printer[] );

@ Mark a label as ready for printing.
@<Exported Functions@>+=
int util_label_ready( int cartonSeq, const char printer[] ) {

   char ref_type[BUF_LEN+1];
   char ref_val[BUF_LEN+1];
   char *val;

   val = util_carton_get(cartonSeq, "refType");
   if(!util_valid_str(val)) {
      Alert("util_label_ready invalid refType ");
      strcpy(ref_type, "");
   }
   else {
      strncpy(ref_type, val, BUF_LEN);
   }

   val = util_carton_get(cartonSeq, "refValue");
   if(!util_valid_str(val)) {
      Alert("util_label_ready invalid refValue ");
      strcpy(ref_val, "");
   }
   else {
      strncpy(ref_val, val, BUF_LEN);
   }

   if(!util_valid_str(ref_type) || !util_valid_str(ref_val)) {
      Alert("Invalid refType or refValue. Can not find label");
      return -1;
   }

   char sql[SQL_LEN+1];

   snprintf(sql, SQL_LEN,
     "SELECT docSeq FROM rdsDocuments "
     "WHERE refType='%s' AND refValue='%s' "
     "AND printerId='%s' AND verified IS NULL AND printed IS NULL",
     ref_type, ref_val, printer);

   val = sql_getvalue(sql); 
   //return (val != NULL && strlen(val) > 0);
   if(val == NULL || strlen(val) <= 0)
      return FALSE;

   int docSeq = atoi(val);

   return docSeq;
}

@ Prototype the function.
@<Exported Prototypes@>+=
int util_label_ready( int cartonSeq, const char doc_type[] );


@ Mark a label as successfully printed.
@<Exported Functions@>+=
void util_label_printed( int seq, int doc_seq ) {

   sql_query(
         "UPDATE rdsDocuments SET "
         "printed = NOW() "
         "WHERE docSeq=%d ",
         doc_seq );

   char printer[BUF_LEN+1];
   char doc_type[BUF_LEN+1];
   char *val;
   val = sql_getvalue(
      "SELECT printerId FROM rdsDocuments " 
      "WHERE docSeq=%d ", doc_seq
   );
   if(!util_valid_str(val)) return;
   strcpy(printer, val);
   printer[BUF_LEN]='\0';

   val = sql_getvalue(
      "SELECT docType FROM rdsDocuments " 
      "WHERE docSeq=%d ", doc_seq
   );
   if(util_valid_str(val)) {
      strcpy(doc_type, val);
      doc_type[BUF_LEN]='\0';

      util_update_description( seq, printer,
            "%s printed at %s", doc_type, printer );
   }

   val = sql_getvalue(
      "SELECT seq FROM conveyorBoxes "
      "WHERE seq=%d AND topPrinter='%s'",
      seq, printer
   );
   if( val != NULL && strlen(val) > 0 && atoi(val) == seq) {
      util_set_stamp( seq, "topPrintStamp" );    
   } else {
      val = sql_getvalue(
         "SELECT seq FROM conveyorBoxes "
         "WHERE seq=%d AND sidePrinter='%s'",
         seq, printer
      );
      if( val != NULL && strlen(val) > 0 && atoi(val) == seq) {
         util_set_stamp( seq, "sidePrintStamp" );    
      }
   }
}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_label_printed( int seq, int doc_seq );

@ Mark a label as successfully printed.
@<Exported Functions@>+=
void util_label_verified( int seq, int doc_seq ) {

   sql_query(
         "UPDATE rdsDocuments SET "
         "verified = NOW() "
         "WHERE docSeq=%d ",
         doc_seq );

   /*
   char printer[BUF_LEN+1];
   char doc_type[BUF_LEN+1];
   char *val;
   val = sql_getvalue(
      "SELECT printerId FROM rdsDocuments " 
      "WHERE docSeq=%d ", doc_seq
   );
   if(!util_valid_str(val)) return;
   strcpy(printer, val);
   printer[BUF_LEN]='\0';

   val = sql_getvalue(
      "SELECT docType FROM rdsDocuments " 
      "WHERE docSeq=%d ", doc_seq
   );
   if(util_valid_str(val)) {
      strcpy(doc_type, val);
      doc_type[BUF_LEN]='\0';

      util_update_description( seq, printer,
            "%s verified at %s", doc_type, printer );
   }

   val = sql_getvalue(
      "SELECT seq FROM conveyorBoxes "
      "WHERE seq=%d AND topPrinter='%s'",
      seq, printer
   );
   if( val != NULL && strlen(val) > 0 && atoi(val) == seq) {
      util_set_stamp( seq, "topPrintStamp" );    
   } else {
      val = sql_getvalue(
         "SELECT seq FROM conveyorBoxes "
         "WHERE seq=%d AND sidePrinter='%s'",
         seq, printer
      );
      if( val != NULL && strlen(val) > 0 && atoi(val) == seq) {
         util_set_stamp( seq, "sidePrintStamp" );    
      }
   }*/
}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_label_verified( int seq, int doc_seq );

@ Remove ZPL commands by replacing the command with spaces (safe for
binary input).
@<Exported Functions@>+=
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
@<Exported Prototypes@>+=
int zpl_remove_safe( char *pszZpl, int len ,const char *pszZplCmd );


@ The |zpl_replace| function. Finds a specified ZPL command |pszZplCmd|
(wihout data fields specified) in the |pszZpl| buffer and replaces it with
new ZPL command (with data fields specified) |pszNewZplCmd|.
The function returns new lenght of the |pszZpl| data buffer on success,
or (-1) on error.
@<Exported Functions@>+=
int zpl_replace( char *pszZpl, int iMaxZplBufSize, const char *pszZplCmd,
                 const char *pszNewZplCmd ) {
  int iPos, iNextPos, iXAPos;

  iPos = poscase( pszZpl, strlen( pszZpl ), pszZplCmd, strlen( pszZplCmd ) );

  //=== nothing to replace, insert new command right after ^XA command
  if( iPos < 0 ) {
    if( strlen( pszNewZplCmd ) > 0 ) {
      iXAPos = poscase( pszZpl, strlen( pszZpl ), "^XA", strlen( "^XA" ) );
      if( iXAPos >= 0 ) {
        Inform( "inserting [%s] into zpl after ^XA", pszNewZplCmd );
        ins( pszZpl, iMaxZplBufSize, iXAPos + 3, pszNewZplCmd );
      }
    }
    return( strlen( pszZpl ) );
  }

  iNextPos = pos( pszZpl + iPos + 1, strlen( pszZpl ) - iPos - 1,
                  "^", strlen( "^" ) );
  iNextPos = ( iNextPos < 0 ) ? strlen( pszZpl ) : iNextPos + iPos + 1;
  Inform( "replacing [%s] zpl cmd with [%s] @@pos%d nextpos%d",
          pszZplCmd, pszNewZplCmd, iPos, iNextPos );
  strip( pszZpl, iPos, iNextPos - iPos );
  ins( pszZpl, iMaxZplBufSize, iPos, pszNewZplCmd );
  return( strlen( pszZpl ) );
}
@ Prototype.
@<Exported Prototypes@>+=
int zpl_replace( char *pszZpl, int iMaxZplBufSize, const char *pszZplCmd,
                 const char *pszNewZplCmd );


@* String helper functions.

@ The |pos| function. Finds a position of substring (needle) in a string (src).
The string and a substring may contain null characters, they are not
null-terminated strings.
The function returns a starting position of the substring (from 0 to
iStrLen-iNeedleLen-1) if substring is found, or (-1) if substring is not found.
@<Exported Functions@>+=
int pos( const void *pSrc, int iSrcLen, const void *pNeedle, int iNeedleLen ) {
  int i, j, iEnd;

  if( ( iEnd = iSrcLen - iNeedleLen + 1 ) < 0 ) return( -1 );
  for( i = 0; i < iEnd; i++ ) {
    for( j = 0; j < iNeedleLen; j++ ) 
      if( ( ( char* )pSrc )[ i + j ] != ( ( char* )pNeedle )[ j ] ) break;
    if( j == iNeedleLen ) return( i );
  }
  return( -1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int pos( const void *pSrc, int iSrcLen, const void *pNeedle, int iNeedleLen );



@ The |poscase| function. Finds a position of substring (needle) in a string
(src) ignoring upper/lower case.
The string and a substring may contain null characters, they are not
null-terminated strings.
The function returns a starting position of the substring (from 0 to
iStrLen-iNeedleLen-1) if substring is found, or (-1) if substring is not found.
@<Exported Functions@>+=
int poscase( const void *pSrc, int iSrcLen, const void *pNeedle,
             int iNeedleLen ) {
  int i, j, iEnd;

  if( ( iEnd = iSrcLen - iNeedleLen + 1 ) < 0 ) return( -1 );
  for( i = 0; i < iEnd; i++ ) {
    for( j = 0; j < iNeedleLen; j++ ) 
      if( toupper( ( ( char* )pSrc )[ i + j ] ) !=
          toupper( ( ( char* )pNeedle )[ j ] ) ) break;
    if( j == iNeedleLen ) return( i );
  }
  return( -1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int poscase( const void *pSrc, int iSrcLen, const void *pNeedle,
             int iNeedleLen );



@ The |lastpos| function. Finds a position of the last occurence of the
substring (needle) in a string (src). The string and a substring may contain
null characters, they are not null-terminated strings.
The function returns a starting position of the substring (from 0 to
iStrLen-iNeedleLen-1) if substring is found, or (-1) if substring is not found.
@<Exported Functions@>+=
int lastpos( const void *pSrc, int iSrcLen,const void *pNeedle,int iNeedleLen){
  int i, j, iStart;

  if( ( iStart = iSrcLen - iNeedleLen ) < 0 ) return( -1 );
  for( i = iStart; i >= 0; i-- ) {
    for( j = 0; j < iNeedleLen; j++ )
      if( ( ( char* )pSrc )[ i + j ] != ( ( char* )pNeedle )[ j ] ) break;
    if( j == iNeedleLen ) return( i );
  }
  return( -1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int lastpos( const void *pSrc, int iSrcLen,const void *pNeedle,int iNeedleLen);



@ The |strpos| function. Finds a position of substring (needle) in a string
(src). The string and a substring are null-terminated.
The function returns a starting position of the substring (from 0 to
iStrLen-iNeedleLen-1) if substring is found, or (-1) if substring is not found.
@<Exported Functions@>+=
int strpos( const char *pszSrc, const char *pszNeedle ) {
  return( pos( pszSrc, strlen( pszSrc ), pszNeedle, strlen( pszNeedle ) ) );
}
@ Prototype.
@<Exported Prototypes@>+=
int strpos( const char *pszSrc, const char *pszNeedle );



@ The |strlastpos| function. Finds a position of the last occurence of the
substring (needle) in a string (src). The string and a substring are
null-terminated. The function returns a starting position of the substring
(from 0 to iStrLen-iNeedleLen-1) if substring is found, or (-1) if substring
is not found.
@<Exported Functions@>+=
int strlastpos( const char *pszSrc, const char *pszNeedle ) {
  return( lastpos( pszSrc, strlen( pszSrc ), pszNeedle, strlen( pszNeedle ) ) );
}
@ Prototype.
@<Exported Prototypes@>+=
int strlastpos( const char *pszSrc, const char *pszNeedle );



@ The |copy| function. Copies a substring from pszSrc to pszDst. The substring
first charater position is iStartPos, the last character position of
the substring is iEndPos. All strings are null-terminated. Returns pszDst.
@<Exported Functions@>+=
char *copy( char *pszDst, int iDstSize, const char *pszSrc, int iStartPos,
            int iLen ) {
  if( iStartPos >= strlen( pszSrc ) || iLen <= 0 ) {
    pszDst[ 0 ] = '\0';
    return( pszDst );
  }
  if( iStartPos < 0 ) iStartPos = 0;
  if( iLen > iDstSize - 1 ) iLen = iDstSize - 1;
  strncpy( pszDst, pszSrc + iStartPos, iLen );
  pszDst[ iLen ] = '\0';
  return( pszDst );
}
@ Prototype.
@<Exported Prototypes@>+=
char *copy( char *pszDst, int iDstSize, const char *pszSrc, int iStartPos,
            int iLen );



@ The |cat| function. Appends a substring from pszSrc to pszDst. The substring
first charater position is iStartPos, the last character position of
the substring is iEndPos. All strings are null-terminated. Total size of the
destination buffer (including terminated zero)is in iDstSize.
The function returns pszDst.
@<Exported Functions@>+=
char *cat( char *pszDst, int iDstSize, const char *pszSrc, int iStartPos,
           int iLen ) {
  int iDstLen;

  if( iStartPos >= strlen( pszSrc ) || iLen <= 0 ) return( pszDst );
  if( iStartPos < 0 ) iStartPos = 0;

  iDstLen = strlen( pszDst );
  if( iLen > iDstSize - iDstLen - 1 ) iLen = iDstSize - iDstLen - 1;
  strncpy( pszDst + iDstLen, pszSrc + iStartPos, iLen );
  pszDst[ iDstLen + iLen ] = '\0';
  return( pszDst );
}
@ Prototype.
@<Exported Prototypes@>+=
char *cat( char *pszDst, int iDstSize, const char *pszSrc, int iStartPos,
           int iLen );


@ The |trim| function. Trims blank spaces at begining and the end of the
string. String is null-terminated. Function returns pszSrc.
@<Exported Functions@>+=
char *trim( char *pszSrc ) {
  return( lefttrim( righttrim( pszSrc ) ) );
}
@ Prototype.
@<Exported Prototypes@>+=
char *trim( char *pszSrc );


@ The |righttrim| function. Trims blank spaces at the end of the string.
String is null-terminated. Function returns pszSrc.
@<Exported Functions@>+=
char *righttrim( char *pszSrc ) {
  int i, iLen;

  iLen = strlen( pszSrc );
  for( i = iLen - 1; i >= 0; i-- ) {
    if( pszSrc[ i ] != ' ' ) break;
    pszSrc[ i ] = '\0';
  }
  return( pszSrc );
}
@ Prototype.
@<Exported Prototypes@>+=
char *righttrim( char *pszSrc );


@ The |lefttrim| function. Trims blank spaces at the beginning of the string.
String is null-terminated. Function returns pszSrc.
@<Exported Functions@>+=
char *lefttrim( char *pszSrc ) {
  int i, j, iLen;

  if( ( iLen = strlen( pszSrc ) ) == 0 ) return( pszSrc );
  if( pszSrc[ 0 ] != ' ' ) return( pszSrc );
  for( i = 1; i < iLen; i++ ) {
    if( pszSrc[ i ] != ' ' ) {
      for( j = i; j < iLen; j++ ) pszSrc[ j - i ] = pszSrc[ j ];
      pszSrc[ iLen - i ] = '\0';
      return( pszSrc );
    }
  }
  pszSrc[ 0 ] = '\0';
  return( pszSrc );
}
@ Prototype.
@<Exported Prototypes@>+=
char *lefttrim( char *pszSrc );



@ The |strip| function. Strips iLen number of characters from string pszSrc
starting at position iStartPos. String is null-terminated. Function returns
pszSrc.
@<Exported Functions@>+=
char *strip( char *pszSrc, int iStartPos, int iLen ) {
  int iSrcLen;

  iSrcLen = strlen( pszSrc );
  if( iStartPos >= strlen( pszSrc ) || iLen <= 0 ) return( pszSrc );

  if( iStartPos < 0 ) iStartPos = 0;
  if( iLen > iSrcLen - iStartPos ) iLen = iSrcLen - iStartPos;
  strncpy( pszSrc + iStartPos, pszSrc + iStartPos + iLen,
           iSrcLen - iStartPos - iLen );
  pszSrc[ iSrcLen - iLen ] = '\0';
  return( pszSrc );
}
@ Prototype.
@<Exported Prototypes@>+=
char *strip( char *pszSrc, int iStartPos, int iLen );



@ The |del| function. Finds and deletes all occurences of a substring
pszDelete from a string pszSrc. Strings is null-terminated. Function returns
resulting pszSrc.
@<Exported Functions@>+=
char *del( char *pszSrc, const char *pszDelete ) {
  int iPos;
  char szTmp[ 42 ];

  while( ( iPos = strpos( pszSrc, pszDelete ) ) >= 0 ) {
    strip( pszSrc, iPos, strlen( pszDelete ) );
  }
  return( pszSrc );
}
@ Prototype.
@<Exported Prototypes@>+=
char *del( char *pszSrc, const char *pszDelete );



@ The |ins| function. Inserts a string pszInsert into the string pszSrc
at position iStartPos. The strings are null-terminated. The maximum pszSrc
buffer size (including terminating zero) is iSrcSize. The function returns
resulting string in pszSrc.
@<Exported Functions@>+=
char *ins( char *pszSrc, int iSrcSize, int iStartPos, const char *pszInsert ) {
  int i, iSrcLen, iLen;

  if( ( iLen = strlen( pszInsert ) ) <= 0 ) return( pszSrc );
  if( iStartPos >= ( iSrcLen = strlen( pszSrc ) ) )
    return( cat( pszSrc, iSrcSize, pszInsert, 0, strlen( pszInsert ) ) );

  if( iStartPos < 0 ) iStartPos = 0;

  if( iLen > iSrcSize - iSrcLen - 1 ) iLen = iSrcSize - iSrcLen - 1;
  for( i = iSrcLen - 1; i >= iStartPos; i-- ) pszSrc[ i + iLen ] = pszSrc[ i ];
  for( i = 0; i < iLen; i++ ) pszSrc[ iStartPos + i ] = pszInsert[ i ];
  pszSrc[ iSrcLen + iLen ] = '\0';
  return( pszSrc );
}
@ Prototype.
@<Exported Prototypes@>+=
char *ins( char *pszSrc, int iSrcSize, int iStartPos, const char *pszInsert );

@ Time in milliseconds since.
@<Exported Functions@>+=
int util_timeMS_since(struct timeval start_time) {
   struct timeval current_time;
   long secondsElapsed = 0;
   long microsecondsElapsed = 0;
   long timeElapsedMicroSeconds = 0;
   long timeElapsedMilliseconds = 0;

   gettimeofday(&current_time, NULL);

   secondsElapsed = current_time.tv_sec - start_time.tv_sec;
   microsecondsElapsed = current_time.tv_usec - start_time.tv_usec;

   timeElapsedMicroSeconds = (secondsElapsed*1000000) + (microsecondsElapsed);
   timeElapsedMilliseconds = (long)(timeElapsedMicroSeconds/1000);
   
   if(timeElapsedMilliseconds < 1) {
      timeElapsedMilliseconds = 1;
   }
   return (int)timeElapsedMilliseconds;
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_timeMS_since(struct timeval start_time);

@ Time in milliseconds since.
@<Exported Functions@>+=
float util_timeMS_since_float(struct timeval start_time) {
   struct timeval current_time;
   long secondsElapsed = 0;
   long microsecondsElapsed = 0;
   long timeElapsedMicroSeconds = 0;
   float timeElapsedMilliseconds = 0;

   gettimeofday(&current_time, NULL);

   secondsElapsed = current_time.tv_sec - start_time.tv_sec;
   microsecondsElapsed = current_time.tv_usec - start_time.tv_usec;

   timeElapsedMicroSeconds = (secondsElapsed*1000000) + (microsecondsElapsed);
   timeElapsedMilliseconds = timeElapsedMicroSeconds/1000.0f;
   
   return timeElapsedMilliseconds;
}
@ Prototype the function.
@<Exported Prototypes@>+=
float util_timeMS_since_float(struct timeval start_time);


@ Determine if a subsystem result is complete.
@<Exported Functions@>+=
int util_get_status( int seq, const char name[], char status[], char val[] ) {
   int err = sql_query(
         "SELECT status, value "
         "FROM cartonStatus "
         "WHERE seq = %d "
         "AND name = '%s'",
         seq, name );
   if(!err && sql_rowcount()==1) {
      strcpy(status, sql_get(0,0));
      strcpy(val, sql_get(0,1));
      return 1;
   } else {
      return 0;
   }
}
@ Prototype the function.
@<Exported Prototypes@>+=
int util_get_status( int seq, const char name[], char status[], char val[] );





@* Index.

