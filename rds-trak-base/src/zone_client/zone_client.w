%
%   zone_client.w -- process a device message associated with a trak zone
%
%   Author: Adam Marshall
%
%   History:
%      2007-09-28 -AHM- init, for Brightstar Sydney
%      2009       -RME- modified for use with trak zones
%      2010-08-24 -AHM- update for current X-Press PAL status updates
%      2013-03-14 -AHM- update to zone_client for socket messages
%      2015-07-21 -AHM- add logic to send message from runtime table
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
%     (C) Copyright 2007--2015 Numina Group, Inc.  All Rights Reserved.
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
\def\title{Zone Client}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program receives a device message and associates it with a box in a
TRAK zone.

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
\centerline{Control Date: $ $Date: 2024/06/05 19:49:51 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2007--2013 Numina Group, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program receives a device message and associates it with a box in a
TRAK zone.

@c
static char rcsid[] = "$Id: zone_client.w,v 1.1 2024/06/05 19:49:51 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Globals@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {

   @<initialize@>@;

   while (TRUE) {
      @<read crank@>@;
      @<check write@>@;
   }

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>      // for signal handling
#include <sys/select.h>  // for select()
#include <sys/time.h>    // for gettimeofday()

#include <app.h>
#include <rds_evt.h>
#include <rds_net.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN            32  // length of small statically allocated strings
#define MSG_LEN           255  // general message length
#define SELECT_SEC          0  // select time (seconds)
#define SELECT_MSEC       100  // select time (milliseconds)
#define DEFAULT_TIMEOUT  "60"  // default timeout (seconds)
#define DEFAULT_IP "localhost" // default ip address
#define DEFAULT_PORT  "10000"  // default port
#define DEFAULT_START_CHAR  STX  // default message start character
#define DEFAULT_END_CHAR    ETX  // default message end character


@ General variables for this program are stored globally.
@<Globals@>+=
char name[ BUF_LEN + 1 ];
int timeout;
struct timeval keepalive_time;


@ Initialization.  Register for tracing and initialize global variables.
@<initialize@>=
{
   if (argc != 2) {
      printf( "usage: %s <device name>\n", argv[0] );
      exit( EXIT_SUCCESS );
   }

   strncpy( name, argv[1], BUF_LEN );  name[ BUF_LEN ] = '\0';

   trn_register( name );
   Trace( "init" );
   Inform( rcsid );

   @<setup variables@>@;

   signal( SIGPIPE, sigpipe_handler );
   signal( SIGTERM, sigterm_handler );
}

@ Obtain values for program and connection settings from the database.
@<setup variables@>=
{
   char *val;

   strcpy( zone, util_get_control( name, "zone", "" ) );
   while( strlen( zone ) == 0) {
      Alert( "no zone specified" );
      sleep( 10 );
      strcpy( zone, util_get_control( name, "zone", "" ) );
   }
   Inform( "zone = %s", zone );

   strcpy( net_ip, util_get_control( name, "ip", DEFAULT_IP ) );
   net_port = atoi( util_get_control( name, "port", DEFAULT_PORT ) );

   val = util_get_control( name, "startChar", "" );
   if (val != NULL && strlen( val ) > 0)
      start_char = strtol( val, NULL, 16 );
   else
      start_char = DEFAULT_START_CHAR;
   val = util_get_control( name, "endChar", "" );
   if (val != NULL && strlen( val ) > 0)
      end_char = strtol( val, NULL, 16 );
   else
      end_char = DEFAULT_END_CHAR;
   Inform( "start char = %#.2x, end char = %#.2x", start_char, end_char );

   strcpy( keepalive, util_get_control( name, "keepalive", "" ) );
   strcpy( heartbeat, util_get_control( name, "heartbeat", "" ) );
   timeout = atoi( util_get_control( name, "timeout", DEFAULT_TIMEOUT ) );
   Inform( "keepalive = [%s], heartbeat = [%s], timeout = %d sec",
         keepalive, heartbeat, timeout );
}



@ Read messages from the device, processing them when complete.
@<read crank@>=
{
   fd_set rfds;
   struct timeval tv;
   char c;
   int n, err;

   attach();

   FD_ZERO( &rfds );
   FD_SET( fd, &rfds );

   tv.tv_sec = SELECT_SEC;
   tv.tv_usec = SELECT_MSEC * 1000;

   n = select( fd + 1, &rfds, NULL, NULL, &tv );

   if (n > 0 && FD_ISSET( fd, &rfds )) {
      err = read( fd, &c, 1 );
      if (err != 1) {
         Alert( "read error from device -- detach" );
         detach();
      } else
         handle_char( c );
   } else if (timeout_expired()) {
      if (strlen( heartbeat ) > 0) {
         Alert( "lost connection to device -- reconnect" );
         detach();
      } else {
         Inform( "timeout expired, reconnect" );
         detach();
      }
   } else if (keepalive_expired() && strlen( keepalive ) > 0) {
      c = start_char;
      write( fd, &c, 1 );
      err = write( fd, keepalive, strlen( keepalive ) );
      c = end_char;
      write( fd, &c, 1 );

      if (err != strlen( keepalive )) {
         Alert( "write error to device -- detach" );
         detach();
      } else
         gettimeofday( &keepalive_time, NULL );
   }
}


@ Write a message from the runtime table.
@<check write@>=
{
   char *val;
   int len;

   val = sql_getvalue(
         "SELECT value FROM runtime "
         "WHERE name = '%s/xmit'",
         name );
   if (val != NULL && (len = strlen( val )) > 0) {
      int err;

      Inform( "send msg [%s]", val );
      attach();

      err = write( fd, val, len );
      if (err != len) {
         Alert( "write error to device -- detach" );
         detach();
      } else
         gettimeofday( &keepalive_time, NULL );

      sql_query(
            "REPLACE INTO runtime SET "
            "name = '%s/xmit', "
            "value = ''",
            name );
   }
}

@ Check if the heartbeat timeout has expired.
@<Functions@>+=
int timeout_expired( void ) {
   return (util_get_elapsed( keepalive_time ) > timeout);
}
@ Prototype the function.
@<Prototypes@>+=
int timeout_expired( void );

@ Check if the keepalive timeout has expired.
@<Functions@>+=
int keepalive_expired( void ) {
   return (util_get_elapsed( keepalive_time ) > timeout / 2.0);
}
@ Prototype the function.
@<Prototypes@>+=
int keepalive_expired( void );


@* Network connection.

@ Maintain globals to hold the connection settings and file descriptor
handle.  The handle is initialized to an illegal value, triggering a
connection the first time it is needed.
@<Globals@>+=
char net_ip[ BUF_LEN + 1 ];
int net_port;
int fd = -1;
char start_char;
char end_char;
char keepalive[ MSG_LEN + 1 ];
char heartbeat[ MSG_LEN + 1 ];

@ Open the connection, if necessary.
@<Functions@>+=
void attach( void ) {
   while (fd < 0) {
      char evt[ MSG_LEN + 1 ];
      sprintf( evt, "%s_comm", name );

      fd = net_open( net_ip, net_port );
      if (fd < 0) {
         Alert( "failed to open device at [%s:%d]", net_ip, net_port );
         evt_start( evt );
         sleep( 10 );
      } else {
         Trace( "connected to device at [%s:%d], handle [%d]",
               net_ip, net_port, fd );
         evt_stop( evt );
         gettimeofday( &keepalive_time, NULL );
      }
   }
}

@ Close the connection.
@<Functions@>+=
void detach( void ) {
   if (fd >= 0) {
      close( fd );
      sleep( 1 );
   }
   fd = -1;
}

@ Prototype the functions.
@<Prototypes@>+=
void attach( void );
void detach( void );


@ Character by character, process the data.  On end-of-transmission,
process the entire message.
@<Functions@>+=
void handle_char( char c )
{
   static char msg[ MSG_LEN + 1 ];
   static int msg_cnt = 0;

   if (c == start_char) {
      msg_cnt = 0;
      msg[ msg_cnt ] = '\0';
   } else if (c == end_char) {
      handle_msg( msg );
      msg_cnt = 0;
      msg[ msg_cnt ] = '\0';
   } else if ((c >= ' ' || c == '\t') && msg_cnt < MSG_LEN) {
      msg[ msg_cnt++ ] = c;
      msg[ msg_cnt ] = '\0';
   }
}
@ Prototype the function.
@<Prototypes@>+=
void handle_char( char c );


@* Process.

@ Maintain globals to hold the TRAK information.
@<Globals@>+=
char zone[ BUF_LEN + 1 ];

@ Process the full message.
@<Functions@>+=
void handle_msg( const char msg[] ) {
   char esc_msg[ 2 * MSG_LEN ];
   struct timeval start_time;
   int retval;

   gettimeofday( &keepalive_time, NULL );  // any msg resets keep-alive time

   if (strlen( heartbeat ) > 0 && strcmp( msg, heartbeat ) == 0)
      return;  // received heartbeat, ignore and continue

   gettimeofday( &start_time, NULL );

   sql_escape( msg, strlen( msg ), esc_msg );

   Inform( "received msg [%s]", msg );
   retval = process_message( esc_msg );
   record_message( esc_msg );
   Inform( "processing %s, took %.3f sec",
         (retval) ? "successful" : "failed", util_get_elapsed( start_time ) );
}
@ Prototype the function.
@<Prototypes@>+=
void handle_msg( const char msg[] );


@ Apply sanity checks in order to associate the message with a box.
@<Functions@>+=
int process_message( const char msg[] ) {
   static int zone_h = -1;
   static int msec_h = -1;
   int box;

   // make sure trak is running
   if (!dp_get( 0 )) {
      Alert( "trak not running -- ignore message" );
      return FALSE;
   }

   // check/get the zone handle
   if (zone_h < 0) {
      zone_h = dp_handle( zone );
      if (zone_h < 0) {
         Alert( "zone dp [%s] undefined -- ignore message", zone );
         return FALSE;
      }
   }
   if (msec_h < 0)
      msec_h = dp_handle( "tm_1ms" );

   // verify that the zone has a valid box 
   dp_registerget( zone_h, 0, &box );
   if (box <= 0 || box > 999) {
      Alert( "%03d: invalid box number in zone %s", box, zone );
      return FALSE;
   }

   // store the device message with the box
   Trace( "%03d: record [%s] at %lu msec", box, msg, dp_counter( msec_h ) );
   util_box_set( box, name, msg );

   return TRUE;
}
@ Prototype the function.
@<Prototypes@>+=
int process_message( const char msg[] );


@ Record the message in the runtime table and update the graphical
diagnostic for this device.
@<Functions@>+=
int record_message( const char msg[] ) {
   sql_query(
         "REPLACE INTO runtime SET "
         "name = '%s/msg', "
         "value = '%s'",
         name, msg );
   sql_query(
         "UPDATE webObjects SET "
         "value = '%s', "
         "hint = NOW() "
         "WHERE name = '%s'",
         msg, name );
}
@ Prototype the function.
@<Prototypes@>+=
int record_message( const char msg[] );


@* Utility functions.

@ Signal handler for SIGPIPE.
@<Functions@>+=
static void sigpipe_handler( int sig ) {
   Inform( "SIGPIPE detected -- detach" );
   detach();
   signal( SIGPIPE, sigpipe_handler );
}
@ Prototype the function.
@<Prototypes@>+=
static void sigpipe_handler( int sig );

@ Signal handler for SIGTERM.
@<Functions@>+=
static void sigterm_handler( int sig ) {
   Inform( "SIGTERM detected -- detach and exit" );
   detach();
   exit( EXIT_SUCCESS );
}
@ Prototype the function.
@<Prototypes@>+=
static void sigterm_handler( int sig );



@* Index.
