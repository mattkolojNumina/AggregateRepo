%   tron.web
%
%   Author: Mark Olson
%
%   History:
%      4/12/97  (mo)  -- reworked to give NT tron features.
%      8/5/2004 (ank) -- uses rds_trn.h instead of rds_trn_r.h, fixed multiline
%                        msgs, fixed displaying non-printable characters, added
%                        color support, added configurable number of output
%                        lines, added help and command line options, remove
%                        displaying PID, structurized code.
%     4/24/2017 (kht) -- removed vt100 and reverse; rewrote msg display style  
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
%
%     (C) Copyright 1993--2017 Numina Group, Inc.  All Rights Reserved.
%
%
%


%
% --- macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\quad\qquad\item{$\bullet$\ }}


%
% --- title
%
\def\title{TRON}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Tron is a real-time display of messages posted to the TRN subsystem. This is a 
system in which program interactions can be traced and watched. Tron refreshes
the screen when new messages are displayed and updates the screen with recent messages.

The screen displays 24 messages in realtime. It allows command line and run-time configuration, 
and allows the screen to be frozen temporarily to allow review of the screen.


%
% --- confidential ---
%
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


%
% --- id ---
%
\bigskip
\centerline{Author: Mark Olson, Mark Woodworth}
\centerline{Revision Date: \today}
\centerline{Version: 2.0}
}


%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 1993,1997 Numina Systems Corporation.  
All Rights Reserved.}
}



@* TRON: Trace Log Display Utility.
 
The TRON program show the most recent 24 trace log with on-line or command-line
configurability. By this, we mean that 
users can specify the way they want the TRON to
list the trace log, filter which programs to see and the message priority.

the TRON program has the following functions:
\dot List the most recent 24 trace log.
\dot List the most recent 24 trace log in reverse order.
\dot Filter the trace log based on run-time specifications or by command line arguments.
\dot reconfigure the TRON program during run-time.

By default, the TRON program is configured to show the trace log with filter 
|Informs|, no name filter, and in the order of the latest process first.

This package uses LynxOS system threads and semaphores, the switch is necessary
when compiling.

The TRON program consists of three parts:
\dot Definition --- this includes the files needed, the structures defined,
and all function prototypes.
\dot Filter and show trace log --- gets the trace log by calling function
|trn_fetch()|, filters and shows the trace log with the current configuration.
\dot Configuration --- the |config| function, allows users to configure the
TRON program at the running time.



@* Overview.
Tron starts up, initializes and configures data, then begins displaying messages
as they come in. 
@ We start with some default typedefs.

@f trn_msg int
@c

@<Includes@>@;
@<Definitions@>@;
@<Prototypes@>@;
@<Globals@>@;
@<Functions@>@;


int main( int argc, char **argv ) {
  trn_register( "tron" );
  @<Initialization@>@;
  @<Parse Command Line@>@;
  @<Process@>@;
  return 0;
}



@ Includes.  The include files goes here
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>
#include <ctype.h>

#include <rds_trn.h>


@ Definitions.
@<Definitions@>=
enum Modes { ShowAll, ExcludeMode, IncludeMode };

#define EOS '\0'
#define True ( 1 == 1 )
#define False ( 0 )
typedef int bool;



@ Globals: 
An array of type |trn_msg| is declared to store the most recent 22
trace log to be displayed. The structure |Processes| is declared to handle
the Include and Exclude lists. 
@<Definitions@>+=
#define MAX_DISPLAY  500
#define INIT_DISPLAY 25
typedef struct {
  int Reverse;  /* option for display: 0 - do not reverse, 1 - reverse */
  int Mode;     /* enum value */
  int Level;    /* filter based on message level */
  int UseColor; /* 0 - do not use color, 1 - use color */
  int MaxLines; /* max number of lines to display: from 1 to |MAX_DIPLAY| */
} Display;

typedef struct __ProgramList {
  struct __ProgramList *link;
  char Name[ TRN_NAME_LEN + 1 ];
  int PID;
  time_t lastStamp;
} PEntry;


@ We add our required globals.
@<Globals@>=
PEntry *ExList, *InList;
Display *show;



@ We put the |Display| structure |show| on the heap.
@<Initialization@>= {
  trn_msg *list;
  int count, i;

  show = ( Display* )malloc( sizeof( Display ) );
  if( show == NULL ) {
    Alert( "Dangerously low on RAM, couldn't get %ld bytes", sizeof( Display ) );
    printf("Dangerously low on RAM, couldn't get %ld bytes\n",sizeof(Display ) );
    exit( 1 );
  }
  ExList = InList = NULL;
  show->Reverse = 0;
  show->Level = trn_inform;
  show->Mode = ShowAll;
  show->UseColor = 1;
  show->MaxLines = INIT_DISPLAY;
}


  
@* Display Data. This is done once per second. If new data is to be added to
the list, we display it.
@<Process@>= {
  for( ;; sleep( 1 ) ) {
    static bool init = True;

    trn_msg *next, *list;
    int i, count;
    bool bShouldAdd;

    struct tm *tm;

    next = list = trn_fetch( &count );
    if( !count ) continue;
    for( i = 0; i < count;  i++, next++ ) {
      tm = localtime( &( next->stamp ) );
      if( show->Level < next->level ) continue;  /* can set filter level */
      bShouldAdd = True;
      if( show->Mode == ExcludeMode ) {    /* add to exclude/include lists */
        if( IsInList( ExList, next ) ) bShouldAdd = False;
        else if( !IsInList( InList, next ) ) InList = ListAdd( InList, next );
      }
      if( show->Mode == IncludeMode ) {    /* add to exclude/include lists */
        if( !IsInList( InList, next ) ) bShouldAdd = False;
        else if( !IsInList( ExList, next ) ) ExList = ListAdd( ExList, next );
      }
      if( !bShouldAdd ) continue;

      if (!init || i >= count - show->MaxLines){
         if(show->UseColor) {
              if (next->level == trn_alert){
                  printf( "\033[31m%2d:%02d:%02d %-8s:%s \033[0m \n",
                         tm->tm_hour, tm->tm_min, tm->tm_sec,
                         next->name, next->msg);
              } else if (next->level == trn_inform){
                  printf("\033[01;36m%2d:%02d:%02d %-8s:%s \033[0m \n",
                         tm->tm_hour, tm->tm_min, tm->tm_sec,
                         next->name, next->msg);
              } else
                  printf("%2d:%02d:%02d %-8s:%s \n",
                         tm->tm_hour, tm->tm_min, tm->tm_sec,
                         next->name, next->msg );
         } else
           printf("%2d:%02d:%02d %-8s:%s \n",
                  tm->tm_hour, tm->tm_min, tm->tm_sec,
                  next->name, next->msg );  
      }
   }

    free( list );
    init = False;
  }
}


@* List Utilities.
\dot |IsInList()| --- Check if a message ``Poster'' is a given list.
\dot |ListAdd()| --- Add a Message from a List.
\dot |ListDrop()| --- Drop a ``Poster'' from a list.

@ The |BuildEntry| function.
@<Functions@>+=
void BuildEntry( PEntry *ePtr, trn_msg *msg ) {
  bzero( ePtr, sizeof( PEntry ) );
  strcpy( ePtr->Name, msg->name );
  ePtr->PID = msg->pid;
  ePtr->lastStamp = time( NULL );
}
@ Prototype.
@<Prototypes@>+=
void BuildEntry( PEntry *ePtr, trn_msg *msg );



@ The |MatchEntry| function.
@<Functions@>+=
bool MatchEntry( PEntry *a, PEntry *b ) {
  if( a->PID == -1 || b->PID == -1 ) return( !strcmp( a->Name, b->Name ) );
  return( ( a->PID == b->PID ) && ( !strcmp( a->Name, b->Name ) ) );
}
@ Prototype.
@<Prototypes@>+=
bool MatchEntry( PEntry *a, PEntry *b );



@ The |IsInList| function.
@<Functions@>+=
bool IsInList( PEntry *list, trn_msg *msg ) {
  PEntry mEntry, *next;

  BuildEntry( &mEntry, msg );
  for( next = list; next; next = next->link ) 
    if( MatchEntry( &mEntry, next ) ) return True;
  return False;
}
@ Prototype.
@<Prototypes@>+=
bool IsInList( PEntry *list, trn_msg *msg );



@ The |ListAdd()| function.
@<Functions@>+=
PEntry *ListAdd( PEntry *list, trn_msg *msg ) {
  PEntry *newEntry;
  newEntry = ( PEntry* )malloc( sizeof( PEntry ) );

  BuildEntry( newEntry, msg );
  newEntry->link = list;
  return newEntry;
}
@ Prototype.
@<Prototypes@>+=
PEntry *ListAdd( PEntry *list, trn_msg *msg );



@ The |PListAdd()| function.
@<Functions@>+=
PEntry *PListAdd( PEntry *list, PEntry *next ) {
  next->link = list;
  return next;
}
@ Prototype.
@<Prototypes@>+=
PEntry *PListAdd( PEntry *list, PEntry *next );



@ The |ListDrop()| function.
@<Functions@>+=
PEntry *ListDrop( PEntry *list, trn_msg *msg ) {
  PEntry mEntry, *victim, *next, *previous;

  BuildEntry( &mEntry, msg );
  if( MatchEntry( list, &mEntry ) ) {
    victim = list;
    list = list->link;
    free( victim );
    return list;
  }
  previous = list;
  for( next = list->link; next; next = next->link ) {
    if( MatchEntry( &mEntry, next ) ) {
      victim = next;
      previous->link = next->link;
      free( victim );
      return list;
    }
    previous = next;
  }
  return list;
}
@ Prototype.
@<Prototypes@>+=
PEntry *ListDrop( PEntry *list, trn_msg *msg );



@* Configuration. At the time of startup, we parse all command line arguments
to configure the display.
@<Parse Command Line@>= {
  PEntry *cfg_list; /* we don't know till the configuration is complete */
                    /* whether to add these to the include or exclude lists */
  PEntry *n_program;
  int i;
  char *arg;

  cfg_list = NULL;
  for( i = 1; i < argc; i++ ) {
    arg = argv[ i ];
    if( *arg == '-' ) {
      @<Handle Option Arguments@>@;
      continue;
    }
    n_program = ( PEntry* )malloc( sizeof( PEntry ) );
    bzero( n_program, sizeof( PEntry ) );
    strncpy( n_program->Name, arg, TRN_NAME_LEN );
    n_program->Name[ TRN_NAME_LEN ] = EOS;
    n_program->PID = -1;
    n_program->lastStamp = time( NULL );
    cfg_list = PListAdd( cfg_list, n_program );
  }
  if( show->Mode == ShowAll && cfg_list != NULL ) show->Mode = IncludeMode;
  if( show->Mode == IncludeMode ) InList = cfg_list;
  if( show->Mode == ExcludeMode ) ExList = cfg_list;
}



@ Options,
\dot `-t' --- don't display informs
\dot `-a' --- only display alerts
\dot `-e' --- exclude mode (programs listed will not be shown).
\dot `-i' --- include mode (programs listed will be shown).
\dot `-c' --- disable colors.
\dot `-h' --- display help.
@<Handle Option Arguments@>= {
  switch( tolower( arg[ 1 ] ) ) {
    case 't':@;
      show->Level = trn_trace;
      break;
    case 'a':@;
      show->Level = trn_alert;
      break;
    case 'e':@;
      show->Mode = ExcludeMode;
      break;
    case 'i':@;
      show->Mode = IncludeMode;
      break;
    case 'c':@;
      show->UseColor = 0;
      break;
    case 'l':@;
      show->MaxLines = atoi( &arg[ 2 ] );
      if( show->MaxLines < 1 || show->MaxLines > MAX_DISPLAY ) {
        fprintf( stderr, "Invalid number of lines in [%s], try -h for help\n",
                 arg );
        fflush( stderr );
        return( 1 );
      }
      break;
    case 'h':@;
      Usage( argv[ 0 ] );
      return( 1 );
    default:@;
      fprintf( stderr, "Unknown option [%s], try -h for help\n", arg );
      fflush( stderr );
      return( 1 );
  }
}



@ |Usage()|.
@<Functions@>+=
void Usage( const char *pszProgName ) {
  fprintf(stdout,"Usage:\n  %s [-t] [-a] [-e]"@;
                  "[-i] [-r] [-c] [-l<lines>] [-h] "@;
                  "[<name1> ... <nameN>]\n\n", pszProgName ); 
  fprintf(stdout,"Where <name1> ... <nameN> - names of the programs, "@;
                 "registered with trn\n\n" );
  fprintf(stdout,"Options:\n" );
  fprintf(stdout,"  -t        - display only trace and alert messages "@;
                 "(no informs)\n");
  fprintf(stdout,"  -a        - display only alert messages (no traces and "@;
                 "informs)\n");
  fprintf(stdout,"  -e        - exclude mode: programs listed will not be "@;
                 "shown\n" );
  fprintf(stdout,"  -i        - include mode: programs listed will be shown "@;
                 "(default)\n" );
  fprintf(stdout,"  -c        - do not use colors "@;
                 "(by default alert messages "@;
                 "are displayed in red,\n" );
  fprintf(stdout,"              trace messages - in white, and inform "@;
                 "messages - in yellow)\n" );
  fprintf(stdout,"  -h        - display this help screen\n\n" );
  fprintf(stdout,"Example:\n  %s -t -e dbd hostd\n\n", pszProgName );
  fflush( stdout );
}


@*Index.

