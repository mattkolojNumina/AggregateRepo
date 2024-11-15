% 
%   rds_qevt.web -- event handler
%
%   Authors: Alex Korzhuk
%
%   History:
%      03/24/04 -- check in (ank)
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
\def\title{QEVT - event handling}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The QEVT library handles events in a uniform manner.
This is an intermediate library that depends on Q.

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
\centerline{Author: Alex Korzhuk}
}

%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 2004 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. This library handles the assertion and clearing of events in a
systematic fashion.

A process |qevtd| reads messages and inserts records in
tables, annunciations, and tracing. This library encapsulates the queue calls.

@ Exported Functions.  This library exports the following functions:

\dot |qevt_instant()| handles an instantaneous event.

\dot |qevt_start()| handles the beginning of an event with finite duration.

\dot |qevt_stop()| handles the end of an event with duration.



@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes. Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/ipc.h>
#include <sys/sem.h>

#include <rds_trn.h>
#include <rds_q.h>
#include "rds_qevt.h"



@ Header. We put the prototypes in an external header.
@(rds_qevt.h@>=
#ifndef __RDS_QEVT_H
#define __RDS_QEVT_H
  @<Exported Defines@>@;
  @<Exported Prototypes@> @;
#endif



@ The counter queue message type is a constant.
@<Exported Defines@>+=
#define QEVT_MSGTYPE ( 20 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



@* The |qevt_instant()| function. This function is called by applications
to handle an instantanious event. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qevt_instant( const char *pszCode ) {
  char *pszErr, szMsg[ 128 + 1 ];
  int iErr, iCodeLen;

  iCodeLen = strlen( pszCode );
  if( iCodeLen >= sizeof( szMsg ) - 3 ) {
    Alert( "Event code (%d chars) is too long", iCodeLen );
    return( FALSE );
  }
  sprintf( szMsg,"A\t%s\t%lld", pszCode, now() );

  if( ( iErr = q_fastsend( QEVT_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}
  
@ Prototype.
@<Exported Prototypes@>+=
int qevt_instant( const char *pszCode );


@ Test Stub. A test stub program |qevt_instant| is also created.
@(qevt_instant.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qevt.h> 
main( int argc, char *argv[] ) {
  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <code>\n", argv[ 0 ] );
    exit( 1 );
  }
   
  if( qevt_instant( argv[ 1 ] ) )
    fprintf( stdout, "qevt_instant(%s) successfully posted\n", argv[ 1 ] );
  else fprintf( stdout, "Error posting qevt_instant(%s)", argv[ 1 ] );
  fflush( stdout );
}



@* The |qevt_start| function. This function is called by applications
to start an event. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qevt_start( const char *pszCode ) {
  char *pszErr, szMsg[ 128 + 1 ];
  int iErr, iCodeLen;

  iCodeLen = strlen( pszCode );
  if( iCodeLen >= sizeof( szMsg ) - 3 ) {
    Alert( "Event code (%d chars) is too long", iCodeLen );
    return( FALSE );
  }
  sprintf( szMsg,"B\t%s\t%lld", pszCode, now() );

  if( ( iErr = q_fastsend( QEVT_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}

@ Prototype.
@<Exported Prototypes@>+=
int qevt_start( const char *pszCode );


@ Test Stub. A test stub program |qevt_start| is also created.
@(qevt_start.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qevt.h>
main( int argc, char *argv[] ) {
  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <code>\n", argv[ 0 ] );
    exit( 1 );
  }

  if( qevt_start( argv[ 1 ] ) )
    fprintf( stdout, "qevt_start(%s) successfully posted\n", argv[ 1 ] );
  else fprintf( stdout, "Error posting qevt_start(%s)", argv[ 1 ] );
  fflush( stdout );
}



@* The |qevt_srtop()| function. This function is called by applications
to stop an event. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qevt_stop( const char *pszCode ) {
  char *pszErr, szMsg[ 128 + 1 ];
  int iErr, iCodeLen;

  iCodeLen = strlen( pszCode );
  if( iCodeLen >= sizeof( szMsg ) - 3 ) {
    Alert( "Event code (%d chars) is too long", iCodeLen );
    return( FALSE );
  }
  sprintf( szMsg,"C\t%s\t%lld", pszCode, now() );

  if( ( iErr = q_fastsend( QEVT_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}
@ Prototype.
@<Exported Prototypes@>+=
int qevt_stop( const char *pszCode );


@ Test Stub. A test stub program |qevt_stop| is also created.
@(qevt_stop.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qevt.h>
main( int argc, char *argv[] ) {
  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <code>\n", argv[ 0 ] );
    exit( 1 );
  }

  if( qevt_stop( argv[ 1 ] ) )
    fprintf( stdout, "qevt_stop(%s) successfully posted\n", argv[ 1 ] );
  else fprintf( stdout, "Error posting qevt_stop(%s)", argv[ 1 ] );
  fflush( stdout );
}


@ The |now| function. Returns number of milliceconds since Epoch.
@<Functions@>+=
long long now( void ) {
  struct timeval tv;

  gettimeofday( &tv, NULL );
  return( ( long long )tv.tv_sec * 1000 + ( long long )tv.tv_usec / 1000 );
}
@ Prototype.
@<Prototypes@>+=
long long now( void );


@*Index.
