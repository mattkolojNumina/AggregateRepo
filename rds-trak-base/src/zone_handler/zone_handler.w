%
%   zone_handler.w -- trak msg processor
%
%   Author: Richard Ernst
%
%   History:
%      2009-09-20 RME  initial version
%      August 17, 2010 (rme) adapted from trakapp, trak_msgd, and trak_evt
%      2011-10-12 -AHM- updated for new zonetrak message format
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
%     (C) Copyright 2009-2011 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{zone handler}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program processes zone-based trak messages, based on configuration in
the database.

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
\centerline{Author: Richard Ernst}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2024/06/05 19:35:43 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program processes zone-based trak messages, based on configuration in
the database.

@c
static char rcsid[] = "$Id: zone_handler.w,v 1.1 2024/06/05 19:35:43 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   Mb_Result *msg;

   @<initialize@>@;
   @<create msg handlers@>@;

   while (TRUE) {
      @<poll for messages@>@;
      usleep( SLEEP_DURATION * 1000 );
   }

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>     // for child signal handling
#include <sys/types.h>  // for child signal handling
#include <sys/wait.h>   // for child signal handling

#include <app.h>
#include <zonetrak.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>


@ Definitions.
@<Defines@>+=
#define SLEEP_DURATION   10  // sleep time btwn processing cycles (msec)
#define ZONE_ANY         -2  // value to match any zone
#define ZONE_MISSING     -1  // value to indicate missing zone dp
#define STATE_ANY        -1  // value to match any state

typedef struct {
   int msg_num;
   int zone;
   int state;
   char cmdType[ 16 + 1 ];
   char cmd[ 255 + 1 ];
} msgHandler;

@ Globals.
@<Globals@>+=
int numHandlers;
msgHandler *msgHandlers;

@ Initialization.
@<initialize@>=
{
   if (argc != 1) {
      printf( "usage: %s\n", argv[0] );
      exit( EXIT_SUCCESS );
   }

   trn_register( "zhandler" );
   Trace( "init" );

   signal( SIGCHLD, child_handler );
}


@ A signal handler to remove zombied child processes after they complete.
@<Functions@>+=
void child_handler( int sig ) {
   pid_t pid;
   int status;

   do {
      pid = waitpid( -1, &status, WNOHANG );
   } while (pid > 0);

   signal( SIGCHLD, child_handler );
}
@ Prototype the function.
@<Prototypes@>+=
void child_handler( int sig );


@ Create msg handlers.
@<create msg handlers@>=
{
   int err;
   int i;
   char host[128];
   char temp[32];

   gethostname( host, 127 );
   host[127] = '\0';
   err = sql_query( "SELECT msg, zone, state, cmdType, cmd " 
                    "FROM zoneHandlers "
                    "WHERE host = '%s' "
                    "ORDER BY seq", host );
   if ( err ) {
      Alert( "sql error [%d] retrieving msg handlers: exit", err );
      exit( EXIT_FAILURE );
   }

   numHandlers = sql_rowcount();
   Inform( "%d msg handlers found", numHandlers );

   msgHandlers = malloc( numHandlers * sizeof( *msgHandlers ) );
   if ( msgHandlers == NULL ) {
      Alert( "unable to allocate memory for msg handlers: exit" );
      exit( EXIT_FAILURE );
   }

   for ( i = 0; i < numHandlers; i++ ) {
      msgHandler *h = &msgHandlers[ i ];

      h->msg_num = atoi( sql_get( i, 0 ) );
      strncpy( temp, sql_get( i, 1 ), 31 );
      temp[31] = '\0';
      if (strcmp( temp, "" ) == 0)
         h->zone = ZONE_ANY;
      else
         h->zone = dp_handle( temp );
      h->state   = atoi( sql_get( i, 2 ) );

      strncpy( h->cmdType, sql_get( i, 3 ), 16 );
      (h->cmdType)[ 16 ] = '\0';
      
      strncpy( h->cmd, sql_get( i, 4 ), 255 );
      (h->cmd)[ 255 ] = '\0';
   }
}

@ Poll for messages from TRAK and process any that arrive.
@<poll for messages@>=
{
   int msg_num;
   int zone = -1;
   int state = -1;
   int box = -1;
   int i;
   int err;
   
   if ( !dp_get( 0 ) )  // make sure trak is running
      continue;
   while ( ( msg = mb_poll() ) != NULL ) {
      msg_num = msg->data[ 0 ];

      if ( msg->count > 1 )
         zone = msg->data[ 1 ];

      if ( msg->count > 2 )
         state = msg->data[ 2 ];

      if ( msg->count > 3 )
         box = msg->data[ 3 ];

      for ( i = 0; i < numHandlers; i++ ) {
         msgHandler *h = &msgHandlers[ i ];
         if ( h->msg_num != msg_num )
            continue;
         if ( ( h->zone != zone ) && ( h->zone != ZONE_ANY ) )
            continue;
         if ( ( h->state != state ) && ( h->state != STATE_ANY ) )
            continue;
         if ( strcmp( h->cmdType, "process" ) == 0 )
            handle_process( zone, box, h->cmd );
         else if ( strcmp( h->cmdType, "sql" ) == 0 )
            handle_sql( zone, box, h->cmd );
         else if ( strcmp( h->cmdType, "dp on" ) == 0 ) {
            Trace( "turn on dp %s", h->cmd );
            dp_set( dp_handle( h->cmd ), 1 );
         } else if ( strcmp( h->cmdType, "dp off" ) == 0 ) {
            Trace( "turn off dp %s", h->cmd );
            dp_set( dp_handle( h->cmd ), 0 );
         }
      }

      mb_free( msg );
   }
}


@ Handle a command-line process.
@<Functions@>+=
void handle_process( int zone, int box, char *cmd ) {
   char command[ 255 ];
   char *argv[10];
   char *arg;
   char *val;
   char temp[ 255 ];
   int i;
 
   int n = 0;
   strcpy( command, cmd );

   arg = strtok( command, " " );
   if ( arg ) {
      argv[ n ] = strdup( arg );
      n++;
      while ( arg = strtok( NULL, " " ) ) {
         if ( strcmp( arg, "<zone>" ) == 0 ) {
            sprintf( temp, "%d", zone );
            argv[ n ] = strdup( temp );
         } else if ( strcmp( arg, "<box>" ) == 0 ) {
            sprintf( temp, "%d", box );
            argv[ n ] = strdup( temp );
         } else 
            argv[ n ] = strdup( arg );
         n++;
      }
   }
   argv[ n ] = NULL;

   strcpy( command, argv[ 0 ] );
   for ( i = 1; i < n; i++ ) {
      strcat( command, " " );
      strcat( command, argv[ i ] );
   }
   Trace( "executing [%s]", command );

   if ( access( argv[0], F_OK ) ) {
      Alert( "command not found [%s]", argv[ 0 ]);
      return;
   }

   if ( !fork() ) {
      execvp( argv[ 0 ], argv );
      Alert( "fork [%s] failed", argv[ 0 ] );
      exit(1) ;
   }

   for ( i = 0; i < n; i++ )
      free( argv[ i ] );
}
@ Prototype the function.
@<Prototypes@>+=
void handle_process( int zone, int box, char *cmd );


@ Handle an SQL query.
@<Functions@>+=
void handle_sql( int zone, int box, char *cmd ) {
   char esc_cmd[ 512 + 1 ];
   char *val;
   int err;

   sql_escape( cmd, strlen( cmd ), esc_cmd );

   val = sql_getvalue(
         "SELECT REPLACE(REPLACE('%s',"
         "'<box>','%d'),"
         "'<zone>','%d')", 
         esc_cmd, box, zone );
   Trace( "sql query [%s]", val );
   err = sql_query( val );
   if ( err )
      Alert( "sql error %d in [%s]", err, val );
}
@ Prototype the function.
@<Prototypes@>+=
void handle_sql( int zone, int box, char *cmd );


@* Index.
