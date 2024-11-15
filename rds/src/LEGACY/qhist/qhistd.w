%  qhistd.web
%
%   Author: Alex Korzhuk
%
%   History:
%      03/16/04 - qhistd started (ank)
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
%
%     (C) Copyright 2004 Numina Systems Corporation.  All Rights Reserved.
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
% --- title ---
%
\def\title{Carton History Daemon}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This program reads the carton history message from queue and updates records
in the database.

%
% --- confidential
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
\centerline{Authors: Alex Korzhuk}
\centerline{Format Date: \today}
\centerline{RCS Revision $ $Revision: 1.1 $ $}
\centerline{RCS Date $ $Date: 2004/03/16 14:00:00 $ $}
}

%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 2004 Numina Systems Corporation.  
All Rights Reserved.}
}



@* Implementation. This program reads messages from queue and updates mysql
database.
@c
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;

int main( int argc, char *argv[] ) {
  int iType, iReader = 23;
  char szCarton[ 32 + 1 ], szCode[ 32 + 1 ], szDesc[ 128 + 1 ], szStamp[ 16+1 ];

  trn_register( "qhistd" );
  q_setreadername( iReader, "qhistd" );

  /* |QHIST_MSGTYPE| defined in |rds_qhist.h| */
  q_settypename( QHIST_MSGTYPE, "qhist" );

  if( argc == 1 ) sql_setconnection( "localhost", "rds", "rds", "rds" );
  else if( argc == 5 ) {
    Trace( "Connecting to db [%s] on [%s] as [%s:%s]", 
           argv[ 2 ], argv[ 1 ], argv[ 3 ], argv[ 4 ] );
    sql_setconnection( argv[ 1 ], argv[ 2 ], argv[ 3 ], argv[ 4 ] );
  }
  else {
    fprintf(stderr,"Usage:  qhistd [<machine> <user> <password> <database>]\n");
    fflush( stderr );
    Alert( "Usage: qhistd [<machine> <user> <password> <database>]" );
    exit( 1 );
  }
  Trace( "Init" );

  signal( SIGINT, fnAbort );
  signal( SIGQUIT, fnAbort );
  signal( SIGABRT, fnAbort );
  signal( SIGKILL, fnAbort );
  signal( SIGTERM, fnAbort );

  for( ;; sleep( 5 ) ) {
    for( iType = q_type( iReader ); iType > 0; iType = q_nexttype( iReader ) ) {
      if( iType == QHIST_MSGTYPE ) { 
        pszMsg = q_recv( iReader );
        Inform( "Got qhist msg type:%d size:%d", iType, strlen( pszMsg ) );
        parse( pszMsg, 0, szCarton, sizeof( szCarton ) - 1 );
        parse( pszMsg, 1, szCode, sizeof( szCode ) - 1 );
        parse( pszMsg, 2, szStamp, sizeof( szStamp ) - 1 );
        parse( pszMsg, 3, szDesc, sizeof( szDesc ) - 1 );
        FREE( pszMsg );
        qhist_log( szCarton, szCode, szDesc, szStamp );
      }
    }
  }
  return( 0 );
}


@ The following include files are used.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>
#include <signal.h>
#include <time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>

#include <rds_q.h>
#include <rds_qhist.h>
#include <rds_sql.h>



@ Useful definitions.
@<Defines@>+=
#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE  ( !FALSE )
#endif

#define FREE( p )  { if( p != NULL ) { free( p ); p = NULL; } }


@ Globals.  
@<Globals@>+=
char *pszMsg = NULL;


@ The |fnAbort()| function. Called on a termination.
@<Functions@>+=
void fnAbort( int sig ) {
  Trace( "Got signal %d, deallocating memory buffers", sig );
  FREE( pszMsg );
  exit( -1 );
}



@ The |parse()| function. The arguments are
\dot a tab-delimited string of fields,
\dot a field index,
\dot a destination string,
\dot a maximum length.
@<Functions@>+=
int parse( char *text, int index, char *target, int max ) {
  int src, dst;

  target[ 0 ] = '\0';

  src = 0;
  while( ( index > 0 ) && ( text[ src ] != '\0' ) ) {
    if( text[ src++ ] == '\t' ) index--;
  }
  dst = 0;
  while( ( dst < max ) && ( text[ src ] != '\0' ) && ( text[ src ] != '\t' ) )
    target[ dst++ ] = text[ src++ ];

  target[ dst ] = '\0';
}
@ Prototype.
@<Prototypes@>+=
int parse( char *text, int index, char *target, int max );



@ The |qhist_log()| finction. Inserts a carton history record into the table.
@<Functions@>+=
int qhist_log( const char *pszCarton, const char *pszCode, const char *pszDesc,
               const char *pszStamp ) {
  char szTable[ 32 ];

  strcpy( szTable, "carton_log" );
  if( sql_query( "INSERT INTO %s SET carton='%s',code='%s',description='%s',"
                 "stamp=FROM_UNIXTIME(%s)",
                 szTable, pszCarton, pszCode, pszDesc, pszStamp ) ) {
    Alert( "SQL err inserting [%s] code[%s][%s] in %s tbl",
           pszCarton, pszCode, pszDesc, szTable );
    return( FALSE );
  }
  Trace( "[%s] code[%s] [%s]", pszCarton, pszCode, pszDesc );
  return( TRUE );
}

@ Prototype.
@<Prototypes@>+=
int qhist_log( const char *pszCarton, const char *pszCode, const char *pszDesc,
               const char *pszStamp );


@* Index.
