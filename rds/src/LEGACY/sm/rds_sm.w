%
%   rds_sm.web
%
%   Author: Alex Korzhuk
%
%   History:
%      10/8/03 -- check in msg source (ank)
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
%     (C) Copyright 2003 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{SM -- shared memory storage}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The SM library is a collection of calls that allows to use shared memory
storage for interprocess data exchange.

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
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview. The SM library provides routines to share data between
applications. This library is a shared C library. 

@ Exported Functions. This library exports the following functions:
\dot |sm_open()| opens a shared memory segment of data
\dot |sm_close()| closes a shared memory segment of data
\dot |sm_lock()| locks a shared memory segment.
\dot |sm_unlock()| unlocks a shared memory segment.
\dot |sm_errstr()| returns a string associated with an error code.



@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes. Standard system includes are collected here, as well as this
library's exported prototypes.
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
#include <rds.h>      //to get the |RDS_BASE| and |RDS_HOME|
#include "rds_sm.h"



@ Header. The exported prototypes and error codes are in an external header
@(rds_sm.h@>=
#ifndef __RDS_SM_H
#define __RDS_SM_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif



@ Exported definitions
@<Exported Defines@>+=
#define ERRSM_GENERAL     (-1)
#define ERRSM_INIT        (-2)
#define ERRSM_ID          (-3)
#define ERRSM_DATASIZE    (-4)  /* invalid segment data size */
#define ERRSM_SEM         (-5)  /* error in semaphore operation */



@ General defenitions
@<Defines@>+=
#define SM_MIN_ID 0
#define SM_MAX_ID 15
#define SM_MAX_DATASIZE ( 1024 * 1024 * 128 ) /* max size of data in storage */
#define SM_BASEKEY   ( RDS_BASE + 0x48 ) /* msg q has offset 0x30, q - 0x38 */



@ Shared memory data storage structure.
@<Structures@>+=
typedef struct {
  char szSignature[ 4 ];
  int iDataSize;
  int iTimeStamp;
} TSMHeader;

typedef struct {
  TSMHeader oSMHeader;
  char achData[ SM_MAX_DATASIZE ]; //real data length is less
} TSMStorage;



@ Definitions for the storage structure
@<Defines@>+=
#define SM_HEADER_SIZE ( sizeof( TSMHeader ) )
#define SM_DATA_OFFSET SM_HEADER_SIZE



@* IPC. Access to the message database is controlled by a file.
These are SYS-V style IPC constructs and are accessed by key values.

@ The semaphore and shared mem ids are stored in local arrays of integers.
@<Globals@>+=
static int ahSem[ SM_MAX_ID - SM_MIN_ID + 1 ];
static int ahMem[ SM_MAX_ID - SM_MIN_ID + 1 ];

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
  return( 0 );
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
  return( 0 );
}
@ Prototype.
@<Prototypes@>+=
static int _unlock( int *psem );



@* The |sm_lock()| function. Waits til it can have sole access to shared memory
storage. Returns 0 on success, or error code on error.
@<Exported Functions@>+=
int sm_lock( int id ) {
  int iKey;

  if( id > SM_MAX_ID || id < SM_MIN_ID ) return( ERRSM_ID );
  iKey = SM_BASEKEY + id * 2 + 1;
  if( _lock( &ahSem[ id - SM_MIN_ID ], iKey ) < 0 ) return( ERRSM_SEM );
}
@ Prototype.
@<Exported Prototypes@>+=
int sm_lock( int id );



@* The |sm_unlock()| function. Returns 0 on success, or error code on error.
@<Exported Functions@>+=
int sm_unlock( int id ) {
  int iKey;

  if( id > SM_MAX_ID || id < SM_MIN_ID ) return( ERRSM_ID );
  if( _unlock( &ahSem[ id - SM_MIN_ID ] ) < 0 ) return( ERRSM_SEM );
}
@ Prototype.
@<Exported Prototypes@>+=
int sm_unlock( int id );



@* The |sm_open()| function. Opens shared memory segment, creating and
initializing it if required. Returns handle of the shared memory storage on
success, error code on error.
@<Exported Functions@>+=
void *sm_open( int id, int iDataSize, int *piErrCode ) {
  int iSize, iKey, iErr, *piErr, *ph;
  void *pMem;

  piErr = ( piErrCode != NULL ) ? piErrCode : &iErr;

  *piErr = ERRSM_ID;
  if( id > SM_MAX_ID || id < SM_MIN_ID ) return( NULL );
  iKey = SM_BASEKEY + id * 2;
  ph = &ahMem[ id - SM_MIN_ID ];

  *piErr = ERRSM_DATASIZE;
  if( iDataSize > SM_MAX_DATASIZE ) return( NULL );
  iSize = sizeof( TSMStorage ) - SM_MAX_DATASIZE + iDataSize;

  *piErr = ERRSM_INIT;
  if( ( *ph = shmget( iKey, iSize, 0777 ) ) < 0 )
    if( ( *ph = shmget( iKey, iSize, IPC_CREAT | 0777 ) ) < 0 ) return( NULL );
  if( ( pMem = shmat( *ph, NULL, 0 ) ) == NULL ) return( NULL );

  *piErr = 0;
  return( pMem );
}

@ Prototype.
@<Exported Prototypes@>+=
void *sm_open( int id, int iDataSize, int *piErrCode );



@* The |sm_close()| function. Marks a shared memory segment to be destroyed.
is attached. It will actualy be destroyed after the last detach. Returns 0
on success, or error code on error.
@<Exported Functions@>+=
int sm_close( int id, void *ptr ) {
  int handle;
  struct shmid_ds oInfo;

  if( id > SM_MAX_ID || id < SM_MIN_ID ) return( ERRSM_ID );
  handle = ahMem[ id - SM_MIN_ID ];

  if( shmdt( ptr ) < 0 ) return( ERRSM_GENERAL );
  if( shmctl( handle, IPC_STAT, &oInfo ) < 0 ) return( ERRSM_GENERAL );
  if( oInfo.shm_nattch > 0 ) return( 0 );
  if( shmctl( handle, IPC_RMID, NULL ) < 0 ) return( ERRSM_GENERAL );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int sm_close( int id, void *ptr );



@* The |sm_errstr()| function. Returns error string associated with err code.
@<Exported Functions@>+=
char *sm_errstr( int iErr ) {
  char *pszErr;

  switch( iErr ) {
    case ERRSM_GENERAL: @;
      asprintf( &pszErr, "General error" );
      break;
    case ERRSM_INIT: @;
      asprintf( &pszErr, "Shared memory initialization error" );
      break;
    case ERRSM_ID: @;
      asprintf( &pszErr, "Invalid id" );
      break;
    case ERRSM_DATASIZE: @;
      asprintf( &pszErr, "Invalid datasize" );
      break;
    case ERRSM_SEM: @;
      asprintf( &pszErr, "Error in semaphore operation" );
      break;
    default: @;
      if( iErr >= 0 ) asprintf( &pszErr, "No errors" );
      else asprintf( &pszErr, "Unknown error" );
  }
  return( pszErr );
}

@ Prototype.
@<Exported Prototypes@>+=
char *sm_errstr( int iErr );


@*Index.
