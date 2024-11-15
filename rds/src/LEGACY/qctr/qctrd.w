%  qctrd.web
%
%   Author: Alex Korzhuk
%
%   History:
%      03/16/04 - qctrd started (ank)
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
\def\title{Counter Daemon}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This program reads the counter message queue and updates records into the
database.

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
  int iType, iReader = 21;
  char szCmd[ 32 + 1 ], szCode[ 32 + 1 ], szZone[ 32 + 1 ];

  trn_register( "qctrd" );
  q_setreadername( iReader, "qctrd" );
  q_settypename( QCTR_MSGTYPE, "qctr");//|QCTR_MSGTYPE| defined in |rds_qctr.h| 

  if( argc == 1 ) sql_setconnection( "localhost", "rds", "rds", "rds" );
  else if( argc == 5 ) {
    Trace( "Connecting to db [%s] on [%s] as [%s:%s]", 
           argv[ 2 ], argv[ 1 ], argv[ 3 ], argv[ 4 ] );
    sql_setconnection( argv[ 1 ], argv[ 2 ], argv[ 3 ], argv[ 4 ] );
  }
  else {
    fprintf( stderr,"Usage:  qctrd [<machine> <user> <password> <database>]\n");
    fflush( stderr );
    Alert( "Usage: qctrd [<machine> <user> <password> <database>]" );
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
      if( iType == QCTR_MSGTYPE ) { 
        pszMsg = q_recv( iReader );
        Inform( "Got qctr msg type:%d size:%d", iType, strlen( pszMsg ) );
        parse( pszMsg, 0, szCmd, sizeof( szCmd ) - 1 );
        parse( pszMsg, 1, szZone, sizeof( szZone ) - 1 );
        parse( pszMsg, 2, szCode, sizeof( szCode ) - 1 );
        FREE( pszMsg );
        qctr_log( szCmd, szZone, szCode );
      }
    }
  }
  return 0;
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
#include <rds_qctr.h>
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



@ The |qctr_log()| finction. Inserts counters into the counter table.
@<Functions@>+=
int qctr_log( char *pszCmd, char *pszZone, char *pszCode ) {
  int iErr;

  if( iErr = sql_query( "SELECT zone,code,description FROM counter "
                        "WHERE zone='%s' AND code='%s'", pszZone, pszCode ) ) {
    Alert( "SQL err %d select counter [%s:%s]", iErr, pszZone, pszCode );
    return( 0 );
  }
  if( sql_rowcount() <= 0 ) {
    if( iErr = sql_query( "INSERT INTO counter SET zone='%s',code='%s',"
                          "description='%s',value=0", pszZone,pszCode,pszCode)){
      Alert( "SQL err %d insert counter [%s:%s]", iErr, pszZone, pszCode );
      return( 0 );
    }
  }
        
  if( strcmp( pszCmd, "B" ) == 0 ) {
    Trace( "Increment counter [%s:%s]", pszZone, pszCode );
    if( iErr = sql_query( "UPDATE counter SET value=value+1 "
                          "WHERE zone='%s' AND code='%s'", pszZone, pszCode )) {
      Alert( "SQL err %d update counter [%s:%s]", iErr, pszZone, pszCode );
      return( 0 );
    }
  }
  else if( strcmp( pszCmd, "Z" ) == 0 ) {
    Trace( "Reset counter [%s:%s]", pszZone, pszCode );
    if( iErr = sql_query( "UPDATE counter SET value=0 "
                          "WHERE zone='%s' AND code='%s'", pszZone, pszCode)) {
      Alert( "SQL err %d update counter [%s:%s]", iErr, pszZone, pszCode );
      return( 0 );
    }
  }
  else Alert( "Unknown cmd [%s] for counter [%s:%s]", pszCmd, pszZone, pszCode);
  return( 1 );
}

@* Index.
