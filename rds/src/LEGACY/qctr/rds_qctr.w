% 
%   rds_qctr.web -- counter handler
%
%   Authors: Alex Korzhuk
%
%   History:
%      03/16/04 -- check in (ank)
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
\def\title{RDS3 QCTR - counter handling}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The QCTR library handles counters in a uniform manner.

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



@* Overview.
This library handles bumping and zeroing general counters. A process |qctrd|
reads messages from queue and inserts records in tables. This library
encapsulates the message calls. The library exports the following functions:

\dot |qctr_bump()| increments a counter. 

\dot |qctr_zero()| zeroes a counter.



@* Structure. The library consists of the following:
@c
@<Includes@>@;
@<Exported Functions@>@;


@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
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
#include "rds_qctr.h"


@ Header.  We put the prototypes in an external header.
@(rds_qctr.h@>=
#ifndef __RDS_QCTR_H
#define __RDS_QCTR_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif


@ The counter queue message type is a constant.
@<Exported Defines@>+=
#define QCTR_MSGTYPE ( 21 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



@* The |qctr_bump()| function. This function is called by applications to
increment a counter. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qctr_bump( const char *pszZone, const char *pszCode ) {
  char *pszErr, szMsg[ 128 + 1 ];
  int iErr, iZoneLen, iCodeLen;

  iZoneLen = strlen( pszZone );
  iCodeLen = strlen( pszCode );
  if( iZoneLen + iCodeLen >= sizeof( szMsg ) - 3 ) {
    Alert( "Counter zone (%d) and code (%d) are too long", iZoneLen, iCodeLen);
    return( FALSE );
  }
  sprintf( szMsg,"B\t%s\t%s", pszZone, pszCode );

  if( ( iErr = q_fastsend( QCTR_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}
  
@ Prototype.
@<Exported Prototypes@>+=
int qctr_bump( const char *pszZone, const char *pszCode );



@ Test Stub.  A test stub program |qctr_bump| is also created.
@(qctr_bump.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qctr.h> 
main( int argc, char *argv[] ) {
  if( argc != 3 ) {
    fprintf( stderr, "Usage: %s <zone> <code>\n", argv[ 0 ] );
    exit( 1 );
  }
   
  if( qctr_bump( argv[ 1 ], argv[ 2 ] ) )
    fprintf( stdout, "qctr_bump(%s,%s) successfully posted\n",argv[1],argv[2] );
  else fprintf( stdout, "Error posting qctr_bump(%s,%s)\n", argv[ 1 ],argv[2] );
  fflush( stdout );
}



@* The |qctr_zero()| function. This function is called by applications to zero
a counter. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qctr_zero( const char *pszZone, const char *pszCode ) {
  char *pszErr, szMsg[ 128 + 1 ];
  int iErr, iZoneLen, iCodeLen;

  iZoneLen = strlen( pszZone );
  iCodeLen = strlen( pszCode );
  if( iZoneLen + iCodeLen >= sizeof( szMsg ) - 3 ) {
    Alert( "Counter zone (%d) and code (%d) are too long", iZoneLen, iCodeLen);
    return( FALSE );
  }

  sprintf( szMsg,"Z\t%s\t%s", pszZone, pszCode );

  if( ( iErr = q_fastsend( QCTR_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}

  
@ Prototype.
@<Exported Prototypes@>+=
int qctr_zero( const char *pszZone, const char *pszCode );


@ Test Stub.  A test stub program |qctr_zero| is also created.
@(qctr_zero.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qctr.h> 
main( int argc, char *argv[] ) {
  if( argc != 3 ) {
    fprintf( stderr, "usage: %s zone code\n", argv[ 0 ] );
    exit( 1 );
  }
   
  if( qctr_zero( argv[ 1 ], argv[ 2 ] ) )
    fprintf( stdout, "qctr_zero(%s,%s) successfully posted\n",argv[1],argv[2] );
  else fprintf( stdout, "Error posting qctr_zero(%s,%s)\n", argv[ 1 ],argv[2] );
}


@*Index.
