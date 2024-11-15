%    
%   trn.web
%
%   Author: Mark Olson
%
%   History:
%      3/12/97   (mo) --- reworked to follow NT versions (mrw)
%      7/10/2000 (mo) --- RDS 3 revision to trak and RDS
%      6/26/2003 (mo) --- removed mysql support from trn_logd, added errno in
%                         trn_attach
%      8/9/2004 (ank) --- recompiled for RH9, added prototypes, fixed warnings,
%                         removed rds_trn_r.h, made changes in format of
%                         trn_simple and trn_logd, changed trn_post and trn_init
%
% 
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Systems Corporation.  
%
%
%     (C) Copyright 2000--2010 Numina Systems Corporation.  All Rights Reserved.
%
%

% --- macros ---
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\quad\qquad\item{$\bullet$\ }}

% --- title ---
\def\title{TRN}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The TRN package includes on-line tracing facilities and trace logging. 
Logged messages are posted into a shared memory segment. Reporting and
querying tools are included in aiding in using logged messages.

It is one of the core packages of the RDS system.


% --- confidential ---
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


% --- id ---
\bigskip
\centerline{Authors: Mark Olson}
\centerline{Revision Date: \today}
\centerline{Version: 3.0}
}


% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 1993,1997 Numina Systems Corporation.  
All Rights Reserved.}
}


@* TRN: Message and Error facilities.

The TRN package is one of the core packages of the RDS system.
The TRN package makes tracing and reporting with |printf| style functions
to a central repository. These messages are viewable via a program {\it tron} 
which is configured to filter messages on the command line.



@ Tracing.
Tracing messages are posted by RDS programs as they process
transactions.
Tracing is very fast, as it is a write to shared memory.  

A program must first register with the tracing facility
to initialize tracing by calling |trn_register()|.  
This registers the process PID and the
tracing name.

The program can then make calls to |trn_post()| to insert messages.
These calls are very fast, and can be made without fear by 
time critical processes. There is {\it no} semaphore blocking on 

For ease of use, three |printf()|-like wrappers for |trn_post()|
are provided: |Alert()|, |Trace()|, and |Inform()|.  

\dot Alerts are for system failures that require attention:
{\it e.g.} a write to a file failed unexpectedly.
In general these are also logged permanently.

\dot Traces are for standard operations of note: {\it e.g.} a carton is
confirmed as sorted.

\dot Informs are for internal steps within transactions that might be useful
when tracking down problems: {\it e.g.} the results of a lookup for a carton
barcode.


@* Library. Each program that wants to use the tracing and
watchdog facilities should link with the |trn.o| library.
The librars consists of some global variables and exported
functions.



@(rds_trn.c@>=
static char *id ="$ $Id: rds_trn.w,v 1.11 2019/02/15 20:52:01 rds Exp $ $";
@<Includes@>@;
@<Definitions@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;


@ The header file.
@(rds_trn.h@>=
#ifndef __TRN_H
#define __TRN_H
  @<Exported Definitions@>@;
  @<Exported Prototypes@>@;
#endif



@* Library Implementation. We start with the includes.
@<Includes@>+=
#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/sem.h>
#include <sys/types.h>
#include <rds.h>
#include "rds_trn.h"



@ and continue by defining the internal (and external) msg structure |trn_msg|.
@f pid_t int 
@<Exported Definitions@>+=
#define TRN_NAME_LEN 15       /* max size of trn registered name */
#define TRN_MSG_LEN 256       /* max size of trn msg */
#define TRN_MSG_N ( 1 << 13 ) /* number of trn msgs in shared memory pool */

enum { trn_alert = 0, trn_trace = 1, trn_inform = 2 };

#include <sys/types.h>  /*| definition of pid_t and time_t |*/
typedef struct {
  char name[ TRN_NAME_LEN + 1 ];
  pid_t pid;
  time_t stamp;
  char level;
  char msg[ TRN_MSG_LEN + 1 ];
} trn_msg;



@ This is used to complete the shared memory structure.
@<Definitions@>+=
#define TAG_BASE ( RDS_BASE + 0x40 )
#define TRN_SHM ( TAG_BASE + 1 )

typedef struct {
  int head;
  trn_msg data[ TRN_MSG_N ];
} trn_pool;

static trn_pool *pool = 0;
static trn_msg *msgs = 0;
static char my_name[ TRN_NAME_LEN + 1 ];



@ We will use the standard (until moved to a library) attach to smem.
@<Functions@>+=
static int trn_attach( void ) {
  int i, id, made = 0;
  trn_msg *ptr;

  if( pool ) return 0;
  if( ( id = shmget( TRN_SHM, sizeof( trn_pool ), 0777 ) ) < 0 ) {
    made = 1;
    id = shmget( TRN_SHM, sizeof( trn_pool ), IPC_CREAT | 0777 );
    if( id < 0 ) return -1;
  }

  pool = ( trn_pool* )shmat( id, 0, 0 );
  msgs = pool->data;
  if( made ) {
    pool->head = 0;      
    for( ptr = msgs, i = 0; i < TRN_MSG_N; i++, ptr++ ) {
      strcpy( ptr->name, "<none>" );
      ptr->pid = -1;
      ptr->stamp = 0;
      ptr->level = trn_inform;
      ptr->msg[ 0 ] = 0;
    } 
    Trace( "Tracing smem initialized" );
  }
  return 0;
}

@ Prototype.
@<Prototypes@>+=
static int trn_attach( void );



@ Two functions. |trn_register()| and |trn_post()|.
@<Exported Functions@>+=
void trn_register( char *name ) {
  strncpy( my_name, name, TRN_NAME_LEN );
  my_name[ TRN_NAME_LEN ] = 0;
  if( trn_attach() ) {
    fprintf( stderr, "Failed to attach to trn\n" );
    return; 
  }
}
@ Exported Prototype.
@<Exported Prototypes@>+=
void trn_register( char *name );



@ And post. This is the basic posting function. It is used by the 
C function calls after the arguments have been parsed.
@<Exported Functions@>+=
void trn_post( int level, char *msg ) {
  int head;
  trn_msg *ptr;

  if( pool == 0 ) return; /* not registered */
  if( level < 0 ) return;

  if( level > trn_inform ) level = trn_inform;
  ptr = msgs + pool->head;
  pool->head++;
  pool->head &= ( TRN_MSG_N - 1 );

  strcpy( ptr->name, my_name );
  ptr->pid = getpid();
  ptr->stamp = time( NULL );
  ptr->level = level;
  strcpy( ptr->msg, msg );
  return;
}

@ Exported Prototype.
@<Exported Prototypes@>+=
void trn_post( int level, char *msg );



@ And name post. This is an extended posting function. It is used for
remote posting of messages.
@<Exported Functions@>+=
int trn_remote_post( int level, char *msg, char *name ) {
  int head;
  trn_msg *ptr;

  if( pool == 0 ) return -1; /* not registered */
  if( level < 0 ) return -1;

  if( level > trn_inform ) level = trn_inform;
  ptr = msgs + pool->head;
  pool->head++;
  pool->head &= ( TRN_MSG_N - 1 );

  strcpy( ptr->name, name );
  ptr->pid = 0;
  ptr->stamp = time( NULL );
  ptr->level = level;
  strcpy( ptr->msg, msg );
  return 0;
}
@ Exported Prototype.
@<Exported Prototypes@>+=
int trn_remote_post( int level, char *msg, char *name );



@ Alert.  |Alert()|, |Trace()|, and |Inform()| are used like |printf()|. These
functions are defined to take variable numbers of arguments. The 
|va_list| typedef is defined in the |#include <stdarg.h>| header file. See
|vsprintf| or |vnsprintf| man pages for their definitions. |vnsprintf| is a
new extension to |vsprintf| which only allows |n| characters to be written
to the character array preventing horrible stack overruns if the library
user is not careful to limit string sizes.

|va_start()| and |va_end()| are macros.
@f va_list int
@<Exported Functions@>+=
int Alert( char *fmt, ... ) {
  va_list ap;
  int count;
  char buffer[ TRN_MSG_LEN + 1 ];

  va_start( ap, fmt );
  count = vsnprintf( buffer, TRN_MSG_LEN, fmt, ap );
  va_end( ap );
  buffer[ TRN_MSG_LEN ] = 0;
  trn_post( trn_alert, buffer );
  return count;
}

@ Exported Prototype.
@<Exported Prototypes@>+=
int Alert( char *fmt, ... );


@ Trace.
@<Exported Functions@>+=
int Trace( char *fmt, ... ) {
  va_list ap;
  int count;
  char buffer[ TRN_MSG_LEN + 1 ];

  va_start( ap, fmt );
  count = vsnprintf( buffer, TRN_MSG_LEN, fmt, ap );
  va_end( ap );
  buffer[ TRN_MSG_LEN ] = 0;
  trn_post( trn_trace, buffer );
  return count;
}

@ Exported Prototype.
@<Exported Prototypes@>+=
int Trace( char *fmt, ... );



@ Inform.
@<Exported Functions@>+=
int Inform( char *fmt, ... ) {
  va_list ap;
  int count;
  char buffer[ TRN_MSG_LEN + 1 ];

  va_start( ap, fmt );
  count = vsnprintf( buffer, TRN_MSG_LEN, fmt, ap );
  va_end( ap );
  buffer[ TRN_MSG_LEN ] = 0;
  trn_post( trn_inform, buffer );
  return count;
}

@ Exported Prototype.
@<Exported Prototypes@>+=
int Inform( char *fmt, ... );



@ The fetching function. The tail is held in a static library variable.
This version of |trn_fetch()| retrieves all valid messages in the log on 
startup. This will cause some duplication of messages on startup, but 
when {\it tron} starts up it won't be blank.
@<Exported Functions@>+=
static int mytail = -1;
trn_msg *trn_fetch( int *count ) {
  trn_msg *result, *src, *dest;
  int head, size, i;

  *count = 0;
  if( trn_attach() ) { /* we failed to attach */
    return 0;
  }
  head = pool->head;
  if( head == mytail ) return 0;

  if( mytail == -1 ) {
    /*| use this if you want to start fresh when program is restarted |*/
    /*| *count = 0; |*/
    /*| mytail = pool->head; |*/
    /*| return 0; |*/
  
    trn_msg *test;
    mytail = head;
    do {
      mytail++;
      mytail &= ( TRN_MSG_N - 1 );
      test = msgs + mytail;
    } while( test->pid == -1 );
  }

  *count = ( head - mytail + TRN_MSG_N ) & ( TRN_MSG_N - 1 );
  size = *count * sizeof( trn_msg );
  result = ( trn_msg* )malloc( size );
  bzero( result, size );
  for( dest = result, i = 0; i < *count; i++, dest++ ) {
    src = msgs + mytail;
    mytail++;
    mytail &= ( TRN_MSG_N - 1 );
    bcopy( src, dest, sizeof( trn_msg ) );
  }
  return result;
}

@ Exported Prototype.
@<Exported Prototypes@>+=
trn_msg *trn_fetch( int *count );



@* simple tron. No filtering or screen control It just prints messages to the
screen. Used mainly for testing.
@(trn_simple.c@>=

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <ctype.h>
#include <rds_trn.h>
int main( int argc, char **argv ) {
  trn_msg *next, *list;
  int i, j, count, len;
  struct tm *tm;
  char *pszType;

  for( ;; sleep( 1 ) ) {
    list = next = trn_fetch( &count );
    if( !count ) continue;
    for( i = 0; i < count; i++, next++ ) {
      tm = localtime( &( next->stamp ) );
      switch( next->level ) {
        case trn_alert:  pszType = "Alert"; break;
        case trn_trace:  pszType = "Trace"; break;
        case trn_inform: pszType = "Info "; break;
        default:         pszType = "?????"; break;
      }
      next->name[ TRN_NAME_LEN ] = '\0';
      next->msg[ TRN_MSG_LEN ] = '\0';
      len = strlen( next->msg );
      for( j = 0; j < len; j++ )
        if( !isprint( next->msg[ j ] ) ) next->msg[ j ] = '.';

      printf( "%2d:%02d:%02d %-5s %-8s:%-5d>%s\n",
              tm->tm_hour, tm->tm_min, tm->tm_sec,
              pszType, next->name, next->pid, next->msg );
    }
    free( list );
  }
  return 0;
}



@ A simple posting tester.
@(trn_post.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <rds_trn.h>
int main( int argc, char **argv ) {
  int i;
  char szText[ TRN_MSG_LEN + 1 ];

  if( argc < 3 ) {
    fprintf( stderr, "Usage: %s {i|t|a} <text_msg>\n", argv[ 0 ] );
    exit( 1 );
  }
  trn_register( "trn_post" );

  strncpy( szText, argv[ 2 ], TRN_MSG_LEN );
  szText[ TRN_MSG_LEN ] = '\0';
  for( i = 3; i < argc; i++ ) {
    if (strlen( szText ) < TRN_MSG_LEN - 1) {
      strcat( szText, " " );
      strncat( szText, argv[ i ], TRN_MSG_LEN - strlen( szText ) );
    }
  }
  
  switch( tolower( argv[ 1 ][ 0 ] ) ) {
    case 'a': Alert( szText ); break;
    case 't': Trace( szText ); break;
    default: Inform( szText ); break;
  }
}



@* Logging. Messages are traced (once per second to a file). 
@(trn_logd.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <unistd.h>
#include <rds_trn.h>
int main( int argc, char **argv ) {
  trn_msg *next, *list;
  int i, j, count, len;
  FILE *fp;
  struct tm *tm;
  char *pszType;

  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <filepath>\n", argv[ 0 ] );
    exit( 1 );
  }
  trn_register( "trn_logd" );
  
  for( ;; sleep( 1 ) ) {
    list = next = trn_fetch( &count );
    if( !count ) continue;
    fp = fopen( argv[ 1 ], "a" );
    for( i = 0; i < count; i++, next++ ) {
      tm = localtime( &( next->stamp ) );
      switch( next->level ) {
        case trn_alert:  pszType = "Alert"; break;
        case trn_trace:  pszType = "Trace"; break;
        case trn_inform: pszType = "Info "; break;
        default:         pszType = "?????";
      }

      next->name[ TRN_NAME_LEN ] = '\0';
      next->msg[ TRN_MSG_LEN ] = '\0';
      len = strlen( next->msg );
      for( j = 0; j < len; j++ )
        if( !isprint( next->msg[ j ] ) ) next->msg[ j ] = '.';

      fprintf( fp, "%02d/%02d %02d:%02d:%02d %-5s %-8s:%-5d>%s\n",
               tm->tm_mon + 1, tm->tm_mday, tm->tm_hour, tm->tm_min, tm->tm_sec,
               pszType, next->name, next->pid, next->msg );
    }
    free( list );
    fclose( fp );
  }
}



@
@(trn_init.c@>=
#include <stdio.h>
#include <rds_trn.h>
int main( int argc, char **argv ) {
  int i;

  trn_register( "trn_init" );
  for( i = 0; i < TRN_MSG_N; i++ ) trn_post( trn_inform, "blank message" );
}


@*Index.
