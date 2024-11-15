%
%   rds_util.w -- general-purpose utility functions
%
%   Author: Adam Marshall
%
%   History:
%      2008-01-08 -AHM- init
%      2015-02-20 -AHM/RME- split app_lib into app_ and util_ functions
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
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2024/10/18 18:14:51 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This library contains general constants and utility functions.

@c
static char rcsid[] = "$Id: rds_util.w,v 1.1 2024/10/18 18:14:51 rds Exp $";
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

#include "rds_util.h"

#ifdef TRAK
#include <rds_trak.h>
#endif


@ Definitions.
@<Defines@>+=
#define BUF_LEN   32  // length of small statically allocated strings
#define BUF_LRG  128  // length of large statically allocated string
#define HOST_LEN  64  // length of hostname
#define MSG_LEN  256  // length of longer message strings
#define SLEEP_DURATION   50  // sleep duration while waiting (msec)
#define TIMEOUT        5000  // max time before releasing (msec)


@* General utilities.

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

@ Get the zone for an application.
@<Exported TRAK Functions@>+=
void util_zone_get( const char app[], char zone[] ) {
   static char value[ BUF_LEN + 1 ] = "";
Inform("zone for app %s",app) ;
   if (strlen( value ) == 0) {
      strncpy( value, util_get_control( app, "zone", "" ), BUF_LEN );
      value[ BUF_LEN ] = '\0';
   }

   strcpy( zone, value );
}

@ Release a zone.
@<Exported TRAK Functions@>+=
void util_zone_release( const char app[] ) {
   static int dp = -1;

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
         char zone[ BUF_LEN + 1 ];
         char dp_name[ BUF_LRG + 1 ];

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

   if (dp > 0) {
      Inform( "release carton" );
      dp_set( dp, 1 );
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
         char dp_name[ BUF_LRG + 1 ];

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
int util_create_seq( void ) {
   char *val;
   long seq = -1;

   val = sql_getvalue( "SELECT GET_LOCK( 'cartonSequenceLock', 60 )" );
   if (val == NULL || atoi( val ) <= 0)
      return -1;

   val = sql_getvalue(
         "SELECT value FROM runtime "
         "WHERE name = 'cartonSequence'" );
   if (val != NULL && strlen( val ) >= 0)
      seq = atol( val );
   if (seq < 0)
      seq = 0;

   seq++;
   sql_query(
         "REPLACE runtime SET "
         "name = 'cartonSequence', "
         "value = '%ld'",
         seq );

   val = sql_getvalue( "SELECT RELEASE_LOCK( 'cartonSequenceLock' )" );

   return seq;
}

@ Prototype the functions.
@<Exported Prototypes@>+=
int util_create_seq( void );

@ Set the value of a carton parameter.
@<Exported Functions@>+=
void util_carton_set( const char area[], int seq, const char name[],
      const char value[] ) {
   if (strlen( area ) == 0 || seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_carton_set: "
            "area [%s], seq [%d], name [%s]", area, seq, name );
      return;
   }

   // Trace("area [%s], name [%s] value [%s] seq [%d]", area, name, value, seq );
   sql_query(
         "UPDATE `%sCartons` SET "
         "`%s` = '%s' "
         "WHERE seq = %d",
         area, name, value, seq );
}

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * util_carton_get( const char area[], int seq, const char name[] ) {
   if (strlen( area ) == 0 || seq <= 0 || strlen( name ) == 0) {
      Alert( "invalid params in util_carton_get: "
            "area [%s], seq [%d], name [%s]", area, seq, name );
      return NULL;
   }

   return sql_getvalue(
         "SELECT `%s` FROM `%sCartons` "
         "WHERE `seq` = %d",
         name, area, seq );
}

@ Prototype the functions.
@<Exported Prototypes@>+=
void util_carton_set( const char area[], int seq, const char name[],
      const char value[] );
char * util_carton_get( const char area[], int seq, const char name[] );


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
         "UPDATE `cartonStatus` SET "
         "`status` = '%s', "
         "`value` = '%s' "
         "WHERE `seq` = %d "
         "AND `name` = '%s' "
         "AND `status` <> 'failed'",
         status, value, seq, name );
}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_update_status( int seq, const char name[], const char status[],
      const char value[] );


@ Update the carton description.
@<Exported Functions@>+=
void util_update_description( const char area[], int seq,
      const char hist_id[], const char hist_code[], const char *fmt, ... ) {
   va_list ap;
   char description[ MSG_LEN + 1 ];

   va_start( ap, fmt );
   vsnprintf( description, MSG_LEN, fmt, ap );
   description[ MSG_LEN ] = '\0';
   va_end( ap );

   if (area != NULL && strlen( area ) > 0 && seq > 0)
      sql_query(
            "UPDATE `%sCartons` SET "
            "`description` = '%s' "
            "WHERE `seq` = %d "
            "AND `status` >= 0",
            area, description, seq );

   if (hist_id != NULL && strlen( hist_id ) > 0 && strlen( description ) > 0)
      hist_post( hist_id, hist_code, description );

}
@ Prototype the function.
@<Exported Prototypes@>+=
void util_update_description( const char area[], int seq,
      const char hist_id[], const char hist_code[], const char *fmt, ... );


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


@* ZPL functions.

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


@* Index.

