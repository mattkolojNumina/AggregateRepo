%
%   rds_tq.web
%
%   Authors: Mark Woodworth, Alex Korzhuk
%
%   History:
%
%       12/17/03 -- rds_tq source (ank)
%
%
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
%     (C) Copyright 1998-2003 Numina Systems Corporation.  All Rights Reserved.
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
\def\dot{\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{Q -- message queues}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The TQ library is a collection of calls that allows the queuing of
variable length text messages.

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
\centerline{Authors: Mark Woodworth, Alex Korzhuk}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview. This is a tag-based message queue. The TQ message library 
provides routines to pass variable length text messages between applications.
This library is a shared C library.



@* Impementation. The library consists of the following:
@c
@<Includes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/sem.h>
#include <sys/shm.h>
#include <rds.h>           //to get the |RDS_BASE| and |RDS_HOME|
#include <rds_tag.h>
#include "rds_tq.h"



@ Header. The exported prototypes and error codes are in an external header
@(rds_tq.h@>=
#ifndef __RDS_TQ_H
#define __RDS_TQ_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif



@ Exported definitions.
@<Exported Defines@>+=
#define ERRTQ_GENERAL    (-1)
#define ERRTQ_TOTAL      (-2)
#define ERRTQ_QNAME      (-3)
#define ERRTQ_MSGTOOLONG (-4)



@* IPC. These are SYS-V style IPC constructs and are accessed by key values.
@<Defines@>+=
#define TQ_BASE   ( RDS_BASE + 0x3C ) /*msg q - ofs 0x30, q - 0x38 */
#define KEY_SEM ( TQ_BASE + 0 )   /* semaphore to access tq data */

#define TQ_LOCK() ( _lock( &Sem, KEY_SEM ) )
#define TQ_UNLOCK() ( _unlock( &Sem ) )



@ The semaphore id is stored in local integer.
@<Globals@>+=
static int Sem = -1;

@ We must define |semun| in some instances.
@<Structures@>+=
#if defined(__GNU_LIBRARY__) && !defined(_SEM_SEMUN_UNDEFINED)
#else
union semun {
  int val;
  struct demid_ds *buf;
  unsigned short int *array;
  struct seminfo *__buf;
};
#endif


@ Lock. The |_lock()| function checks to see if the |*psem| has
been created, and if not, creates it. Then the system waits til it can have
sole access.
@<Functions@>+=
static int _lock( int *psem, key_t key ) {
  union semun lock_union;
  struct sembuf lock_buf[ 1 ];
  int err;

  if( *psem < 0 ) *psem = semget( key, 1, 0777 );
  if( *psem < 0 ) {
    *psem = semget( key, 1, 0777 | IPC_CREAT );
    if( *psem < 0 ) return( -1 );
    if( *psem == 0 ) {
      err = semctl( *psem, 0, IPC_RMID, NULL );
      *psem = semget( key, 1, 0777 | IPC_CREAT );
      if( *psem < 0 ) return( -2 );
      if( *psem == 0 ) return( -3 );
    }
    lock_union.val = 1;
    semctl( *psem, 0, SETVAL, lock_union );
  }

  lock_buf[ 0 ].sem_num = 0;
  lock_buf[ 0 ].sem_op = -1;
  lock_buf[ 0 ].sem_flg = SEM_UNDO;
  semop( *psem, lock_buf, 1 );
}
@ Prototype.
@<Prototypes@>+=
static int _lock( int *psem, key_t key );


@ Unlock. The |_unlock()| function checks to see if the |*psem| has been
created, and if so, drops it by one.
@<Functions@>+=
static int _unlock( int *psem ) {
  struct sembuf lock_buf[ 1 ];

  if( *psem <= 0 ) return( -1 );

  lock_buf[ 0 ].sem_num = 0;
  lock_buf[ 0 ].sem_op = 1;
  lock_buf[ 0 ].sem_flg = SEM_UNDO;
  semop( *psem, lock_buf, 1 );
}

@ Prototype.
@<Prototypes@>+=
static int _unlock( int *psem );



@* The |tq_init()| function. Sets new value for |iTotalMessages|.
Returns 0 on success, or error code (|<0|) on error.
@<Exported Functions@>+=
int tq_init( const char *pszQName, int iTotalMessages ) {
  char szName[ 128 ], szValue[ 32 ];

  TQ_LOCK();
  if( iTotalMessages <= 0 || iTotalMessages > 999999 ) {
    TQ_UNLOCK();
    return( ERRTQ_TOTAL );
  }
  sprintf( szName, "/tq/%s/total", pszQName );
  sprintf( szValue, "%d", iTotalMessages );
  tag_insert( szName, szValue );
  TQ_UNLOCK();
  return( 0 );
}
@ Prototype.
@<Exported Prototypes@>+=
int tq_init( const char *pszQName, int iTotalMessages );


@ Test Stub. A test stub program |tq_recv| is also created.
@(tq_init.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tq.h>
main( int argc, char *argv[] ) {
  int iErr, iTotal;
  char *pszErr;

  if( argc != 3 ) {
    fprintf( stderr, "Usage:  %s <QName> <TotalMessages>\n", argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }
  if( ( iTotal = atoi( argv[ 2 ] ) ) <= 0 || iTotal > 999999 ) {
    fprintf( stderr, "Error: [%s] is a wrong number for TotalMessages\n",
             argv[ 2 ] );
    fflush( stdout );
    exit( 1 );
  }
  if( ( iErr = tq_init( argv[ 1 ], iTotal ) ) < 0 ) {
    pszErr = tq_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  fprintf( stdout, "Tag queue [%s] initialized successfully\n", argv[ 1 ] );
  fflush( stdout );
}


@* The |tq_send()| function. Inserts one message into the queue.
Function returns 0 on success, and errorr code (|<0|) on error.
@<Exported Functions@>+=
int tq_send( const char *pszQName, const char *pszMsg ) {
  int iTotal = 0, iHead = 0;
  char szName[ 128 ], *pszValue, szValue[ 32 ];

  sprintf( szName, "/tq/%s/total", pszQName );
  TQ_LOCK();
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iTotal = atoi( pszValue );
    free( pszValue );
  }
  if( iTotal <= 0 ) {
    TQ_UNLOCK();
    return( ERRTQ_TOTAL );
  }

  sprintf( szName, "/tq/%s/head", pszQName );
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iHead = atoi( pszValue );
    free( pszValue );
  }
  if( iHead < 1 || iHead > iTotal ) iHead = 1;

  sprintf( szName, "/tq/%s/%06.6d", pszQName, iHead );
  tag_insert( szName, ( char* )pszMsg );

  sprintf( szName, "/tq/%s/head", pszQName );
  sprintf( szValue, "%d", iHead % iTotal + 1 );
  tag_insert( szName, szValue );
  TQ_UNLOCK();
  return( 0 );
}
@ Prototype.
@<Exported Prototypes@>+=
int tq_send( const char *pszQName, const char *pszMsg );


@ Test Stub. A test stub program |tq_send| is also created.
@(tq_send.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_tq.h>
main( int argc, char *argv[] ) {
  int iErr;
  char szMsg[ 128 ], *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s <QName> [<Msg>]\n", argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }
  if( argc == 3 ) {
    strncpy( szMsg, argv[ 2 ], sizeof( szMsg ) - 1 );
    szMsg[ sizeof( szMsg ) - 1 ] = '\0';
  }
  else strcpy( szMsg, "" );

  if( ( iErr = tq_send( argv[ 1 ], szMsg ) ) < 0 ) {
    pszErr = tq_errstr( iErr );
    fprintf( stdout, "Tag queue [%s] error %d: %s\n", argv[ 1 ], iErr, pszErr);
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  fprintf( stdout, "[%s] <== [%s]\n", argv[ 1 ], szMsg );
  fflush( stdout );
}



@* The |tq_recv()| function. Receives one message from the queue.
Function returns 1 - on success and msg is in pszBuf, 0 - on success and
queue is empty, error code (|<0|) - on error.
@<Exported Functions@>+=
int tq_recv( const char *pszQName, char *pszBuf, int iBufSize ) {
  int iTotal = 0, iHead = 0, iTail = 0;
  char szName[ 128 ], *pszValue, szValue[ 32 ];

  sprintf( szName, "/tq/%s/total", pszQName );
  TQ_LOCK();
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iTotal = atoi( pszValue );
    free( pszValue );
  }
  if( iTotal <= 0 ) {
    TQ_UNLOCK();
    return( ERRTQ_TOTAL );
  }

  sprintf( szName, "/tq/%s/head", pszQName );
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iHead = atoi( pszValue );
    free( pszValue );
  }
  sprintf( szName, "/tq/%s/tail", pszQName );
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iTail = atoi( pszValue );
    free( pszValue );
  }
  if( iHead < 1 || iHead > iTotal ) iHead = 1;
  if( iTail < 1 || iTail > iTotal ) iTail = 1;

  for( ; iTail != iHead; iTail = iTail % iTotal + 1 ) {
    sprintf( szName, "/tq/%s/%06.6d", pszQName, iTail );
    if( ( pszValue = tag_value( szName ) ) != NULL ) {
      strncpy( pszBuf, pszValue, iBufSize - 1 );
      pszBuf[ iBufSize - 1 ] = '\0';
      free( pszValue );
      break;
    }
  }
  if( iHead == iTail ) {
    TQ_UNLOCK();
    return( 0 );
  }

  sprintf( szName, "/tq/%s/tail", pszQName );
  sprintf( szValue, "%d", iTail % iTotal + 1 );
  tag_insert( szName, szValue );
  TQ_UNLOCK();
  return( 1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int tq_recv( const char *pszQName, char *pszBuf, int iBufSize );


@ Test Stub. A test stub program |tq_recv| is also created.
@(tq_recv.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tq.h>
main( int argc, char *argv[] ) {
  int iErr;
  char szMsg[ 128 ], *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s <QName>\n", argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = tq_recv( argv[ 1 ], szMsg, sizeof( szMsg ) ) ) < 0 ) {
    pszErr = tq_errstr( iErr );
    fprintf( stdout, "Tag queue [%s] error %d: %s\n", argv[ 1 ], iErr, pszErr);
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  if( iErr == 0 ) fprintf( stdout, "Tag queue [%s] is empty\n", argv[ 1 ] );
  else fprintf( stdout, "[%s] => [%s]\n", argv[ 1 ], szMsg );
  fflush( stdout );
}



@* The |tq_empty()| function. Empties the queue by setting all reader's tail
indexes equal to the head index. On success returns the head index (from 0
to |iSegmentsTotal-1|), on error returns error code (|<0|).
@<Exported Functions@>+=
int tq_empty( const char *pszQName ) {
  int iTotal = 0, iHead = 0;
  char szName[ 128 ], *pszValue, szValue[ 32 ];

  TQ_LOCK();
  sprintf( szName, "/tq/%s/total", pszQName );
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iTotal = atoi( pszValue );
    free( pszValue );
  }
  if( iTotal <= 0 ) {
    TQ_UNLOCK();
    return( ERRTQ_TOTAL );
  }

  sprintf( szName, "/tq/%s/head", pszQName );
  if( ( pszValue = tag_value( szName ) ) != NULL ) {
    iHead = atoi( pszValue );
    free( pszValue );
  }

  if( iHead <= 0 || iHead > iTotal ) iHead = 1;
  sprintf( szValue, "%d", iHead );
  tag_insert( szName, szValue );

  sprintf( szName, "/tq/%s/tail", pszQName );
  tag_insert( szName, szValue );
  TQ_UNLOCK();
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int tq_empty( const char *pszQName );


@ Test Stub. A test stub program |tq_recv| is also created.
@(tq_empty.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tq.h>
main( int argc, char *argv[] ) {
  int iErr;
  char szMsg[ 128 ], *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s <QName>\n", argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = tq_empty( argv[ 1 ] ) ) < 0 ) {
    pszErr = tq_errstr( iErr );
    fprintf( stdout, "Tag queue [%s] error %d: %s\n", argv[ 1 ], iErr, pszErr);
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  fprintf( stdout, "Done. Queue [%s] is empty now\n", argv[ 1 ] );
  fflush( stdout );
}



@* The |tq_errstr()| function. Returns error string associated with err code.
@<Exported Functions@>+=
char *tq_errstr( int iErr ) {
  char *pszErr;

  switch( iErr ) {
    case ERRTQ_GENERAL: @;
      asprintf( &pszErr, "General error" );
      break;
    case ERRTQ_TOTAL: @;
      asprintf( &pszErr, "Invalid number of total messages" );
      break;
    case ERRTQ_QNAME: @;
      asprintf( &pszErr, "Invalid queue name" );
      break;
    case ERRTQ_MSGTOOLONG: @;
      asprintf( &pszErr, "Message is too long" );
      break;
    default:
      if( iErr >= 0 ) asprintf( &pszErr, "No errors" );
      else asprintf( &pszErr, "Unknown error" );
  }
  return( pszErr );
}

@ Prototype.
@<Exported Prototypes@>+=
char *tq_errstr( int iErr );


@*Index.
