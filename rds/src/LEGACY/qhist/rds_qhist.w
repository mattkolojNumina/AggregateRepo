% 
%   rds_qhist.web -- carton history handler
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
\def\title{RDS3 QHIST - carton history handling}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The QHIST library handles carton history logging in a uniform manner.

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
This library handles carton history logging. A process |qhistd|
reads messages from queue and inserts records in tables. This library
encapsulates the message calls.
The library exports the following functions:

\dot |qhist_post()| posts a carton history message.




@* Structure. The library consists of the following:
@c
@<Includes@>@;
@<Exported Functions@>@;


@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/time.h>
#include <sys/ipc.h>
#include <sys/sem.h>

#include <rds_trn.h>
#include <rds_q.h>
#include "rds_qhist.h"


@ Header.  We put the prototypes in an external header.
@(rds_qhist.h@>=
#ifndef __RDS_QHIST_H
#define __RDS_QHIST_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif


@ The counter queue message type is a constant.
@<Exported Defines@>+=
#define QHIST_MSGTYPE ( 23 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



@* The |qhist_post()| function. This function is called by applications to
post a carton history message. Returns TRUE on success, or FALSE on error.
@<Exported Functions@>+=
int qhist_post( const char *pszCarton, const char *pszCode,
                const char *pszDesc ) {
  char *pszErr, szMsg[ 256 + 1 ];
  int iErr, iCartonLen, iCodeLen, iDescLen;
  struct timeval tv;

  iCartonLen = strlen( pszCarton );
  iCodeLen = strlen( pszCode );
  iDescLen = strlen( pszDesc );
  if( iCartonLen + iCodeLen + iDescLen >= sizeof( szMsg ) - 20 ) {
    Alert("[%s][%s] in qhist_post: carton(%d), code(%d), desc(%d) are too long",
           pszCarton, pszCode, iCartonLen, iCodeLen, iDescLen );
    return( FALSE );
  }
  if( gettimeofday( &tv, NULL ) < 0 ) {
    Alert( "[%s][%s] in q_hist_post: gettimeofday() returns error",
           pszCarton, pszCode );
    return( FALSE );
  }

  sprintf( szMsg, "%s\t%s\t%ld\t%s", pszCarton, pszCode, tv.tv_sec, pszDesc );

  if( ( iErr = q_fastsend( QHIST_MSGTYPE, szMsg ) ) < 0 ) {
    pszErr = q_errstr( iErr );
    Alert( "Error %d in q_fastsend(): %s\n", iErr, pszErr );
    free( pszErr );
    return( FALSE );
  }
  return( TRUE );
}

@ Prototype.
@<Exported Prototypes@>+=
int qhist_post( const char *pszCarton, const char *pszCode,
                const char *pszDesc );



@ Test Stub.  A test stub program |qhist_post| is also created.
@(qhist_post.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_qhist.h> 
main( int argc, char *argv[] ) {
  if( argc != 4 ) {
    fprintf( stderr, "Usage: %s <carton> <code> <desc>\n", argv[ 0 ] );
    exit( 1 );
  }

  if( qhist_post( argv[ 1 ], argv[ 2 ], argv[ 3 ] ) )
    fprintf( stdout, "qhist_post(%s,%s,%s) successfully posted\n",
             argv[ 1 ], argv[ 2 ], argv[ 3 ] );
  else fprintf( stdout, "Error posting qhist_post(%s,%s,%s)\n",
                argv[ 1 ], argv[ 2 ], argv[ 3 ] );
  fflush( stdout );
}



@*Index.
