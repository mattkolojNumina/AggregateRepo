%
%   rds_q.web
%
%   Authors: Mark Woodworth, Alex Korzhuk
%
%   History:
% 10/24/98 mrw: check in msg source
% 07/10/00 mrw: review msg source
% 07/11/03 ank: parts were ported from VCS for Win2K, renamed to vmsg
% 08/11/03 ank: added/redesigned/renamed - q source
% 08/28/04 ank: renamed to rds_q, changed outputs in q_info and q_config
%               utilities, now the library is a part of the RDS3 package
% 03/13/06 ank: fixed bug in q_type, q_nexttype, and q_recv when byMsgType=0
%               and byCount=0
% 09/11/09 ank: changed MAX_DATA_LEN from 4096 to 32768
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
\def\dot{\quad\qquad\item{$\bullet$\ }}


%
% --- title ---
%
\def\title{RDS3 Q -- message queues}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The RDS3 Q library is a collection of calls that allows the queuing of
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
%
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview.
The Q message library provides routines to pass variable length text messages
between applications.

Messages and status information are stored in a non-volatile disk based file.

This library is a shared C library and exports the following functions:

\dot |q_send()| queues up one text message with an associated queue id.

\dot |q_type()| returns the type of the next message to be read for one of the
readers. A return of type 0 indicates that the queue is empty (i.e. up to date).

\dot |q_recv()| returns the text of the next message for one reader.
The caller must free the retrieved buffer.

\dot |q_next()| advances one of the readers to the next message.

\dot |q_get()| returns the current index for the head or one of the readers.

\dot |q_put()| sets the value of the current index for the head or one of the
readers.

\dot |q_export()| dumps the contents of the queue into a text readable format.

\dot |q_import()| recovers the queue from a previously dumped file.



@* Implementation. The library consists of the following:
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
#include <rds.h>  //to get the |RDS_BASE| and |RDS_HOME|
#include "rds_q.h"



@ Header. The exported prototypes and error codes are in an external header
@(rds_q.h@>=
#ifndef __RDS_Q_H
#define __RDS_Q_H
  @<Exported Prototypes@>@;
  @<Exported Defines@>@;
#endif



@* Data Structures.
The file containing the data structures has four sections:
the header, the readers list, the message types list, and the data segments.

@ Exported definitions
@<Exported Defines@>+=
#define ERRQ_GENERAL     (-1)
#define ERRQ_INIT        (-2)
#define ERRQ_READER      (-3)
#define ERRQ_MSGTYPE     (-4)
#define ERRQ_TAIL        (-5)
#define ERRQ_MSGTOOLONG  (-6)
#define ERRQ_HOST        (-7)
#define ERRQ_FILE        (-8)  //error in operations with export/import file
#define ERRQ_ALLOC       (-9)  //memory allocation error
#define ERRQ_SEGDATASIZE (-10) //invalid segment data size
#define ERRQ_SEGTOTAL    (-11) //invalid total number of segments
#define ERRQ_READERNAME  (-12) //reader name not found
#define ERRQ_MSGTYPENAME (-13) //name of the message type not found


@ General defenitions
@<Defines@>+=
#define FILENAME ( RDS_HOME "/data/q.dat" ) /* data is all held in one file */
#define NAME_LEN ( 32 )        /* the name is fixed in length */
#define MAX_DATA_LEN ( 32768 ) /* max size of data in segment */


@ Queue file header information.
@<Structures@>+=
typedef struct {
  char szSignature[ 4 ];
  int iSegmentDataSize;
  int iSegmentsTotal;
  int iHead;
} TQHeader;

@ Definitions for the queue file header
@<Defines@>+=
#define QHEADER_OFFSET 0
#define QHEADER_SIZE ( sizeof( TQHeader ) )
#define QHEADER_EXTENT QHEADER_SIZE


@ Index information.
The Q library maintains a number of named indices into the data area.
Each of these indices points to the `next record to use'. The first record
is reserved for the unique head record.
Each index entry holds a name and an index value.
@<Structures@>+=
typedef struct {
  int iActive;
  char szName[ NAME_LEN + 1 ];
  int iTail;
} TQReader;

@ Definitions for indexes
@<Defines@>+=
#define QREADERS_OFFSET ( QHEADER_OFFSET + QHEADER_EXTENT )
#define QREADERS_TOTAL ( 32 ) //there are a fixed number of index records
#define QREADER_SIZE ( sizeof( TQReader ) ) //the size of each record
#define QREADERS_EXTENT ( QREADERS_TOTAL * QREADER_SIZE ) //the space taken up


@ Queue message types information
The library maintains a list of message types or queues.
@<Structures@>+=
typedef struct {
  int iActive;
  char szName[ NAME_LEN + 1 ];
} TMsgType;

@ Definitions for message types. The message types records follow readers
@<Defines@>+=
#define MSGTYPES_OFFSET ( QREADERS_OFFSET + QREADERS_EXTENT )
#define MSGTYPES_TOTAL ( 127 )  /* fixed number of msg types (msg q had 32) */
#define MSGTYPE_SIZE ( sizeof( TMsgType ) )
#define MSGTYPES_EXTENT ( MSGTYPES_TOTAL * MSGTYPE_SIZE )


@ Segment records
The content of messages is stored in segment records.  Each segment knows
the message type, the total segments in the message, and the segment count
of this segment.
@<Structures@>+=
typedef struct {
  unsigned char byTotal;
  unsigned char byCount;
  unsigned char byMsgType;
  char szData[ MAX_DATA_LEN + 1 ]; //real data length is less
} TSegment;

@ Defenitions for segments. Segments follow the message types
@<Defines@>+=
#define SEGMENTS_OFFSET ( MSGTYPES_OFFSET + MSGTYPES_EXTENT )
#define GET_SEGMENT_SIZE( iSegmentDataSize ) \
        ( sizeof( TSegment ) - MAX_DATA_LEN + ( iSegmentDataSize ) )



@ Shared memory queue header information.
@<Structures@>+=
typedef struct {
  int iSegmentDataSize;
  int iHead;
  int iTail;
} TSMHeader;

@ Definitions for the shared memory queue
@<Defines@>+=
#define SM_HEADER_SIZE ( sizeof( TSMHeader ) )
#define SM_SEGMENTS_OFFSET SM_HEADER_SIZE
#define SM_SEGMENTS_TOTAL 256  /* can be tuned */


@ A local file handle is kept. This is initialized to an illegal handle value
@<Globals@>+=
static int hFile = -1;


@* IPC. Access to the message database is controlled by a file.
These are SYS-V style IPC constructs and are accessed by key values.
@<Defines@>+=
#define Q_BASE   ( RDS_BASE + 0x38 ) /* msg q has offset 0x30, q - 0x38 */
#define KEY_FILESEM ( Q_BASE + 0 )   /* semaphore to access q file */
#define KEY_SMSEM   ( Q_BASE + 1 )   /* semaphore to access shared memory q */
#define KEY_SHM     ( Q_BASE + 2 )   /* shared memory id */

#define FILE_LOCK() ( _lock( &FileSem, KEY_FILESEM ) )
#define FILE_UNLOCK() ( _unlock( &FileSem ) )
#define SM_LOCK() ( _lock( &SmSem, KEY_SMSEM ) )
#define SM_UNLOCK() ( _unlock( &SmSem ) )

@ The semaphore ids are stored in local integers.
@<Globals@>+=
static int FileSem = -1;
static int SmSem = -1;

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

@ Prototypes.
@<Prototypes@>+=
static int _lock( int *psem, key_t key );
static int _unlock( int *psem );



@ Shared Memory.
@<Globals@>+=
static void *pSM = NULL;

@ The function |InitSM()| makes sure that the local shared memory segment
is attached, creating and initializing it if required. Returns 1 on success,
0 on error.
@<Functions@>+=
static int InitSM( void ) {
  int id, iSize;

  if( pSM == NULL ) {
    iSize = SM_HEADER_SIZE + sizeof( TSegment ) * SM_SEGMENTS_TOTAL;
    if( ( id = shmget( KEY_SHM, iSize, 0777 ) ) < 0 )
      if( ( id = shmget( KEY_SHM, iSize, IPC_CREAT | 0777 ) ) < 0 ) return(0);
    if( ( pSM = shmat( id, NULL, 0 ) ) == NULL ) return( 0 );
  }
  return( 1 );
}

@ Prototype.
@<Prototypes@>+=
static int InitSM( void );




@ The |CircDistance()| function.
@<Functions@>+=
static int CircDistance( int iFirst, int iSecond, int iSize ) {
  if( iFirst <= iSecond ) return( iFirst - iSecond + iSize );
  return( iFirst - iSecond );
}
@ Prototype.
@<Prototypes@>+=
static int CircDistance( int iFirst, int iSecond, int iSize );


@ The |InitQFile()| function.
@<Functions@>+=
static void InitQFile( int iSegmentDataSize, int iSegmentsTotal ) {
  int i, iSegmentSize;
  TQHeader oHeader;
  TQReader oReader;
  TMsgType oMsgType;
  TSegment *pSegment;

  //initialize the queue file header
  strcpy( oHeader.szSignature, "Q20" );
  oHeader.iHead = 0;
  oHeader.iSegmentDataSize = iSegmentDataSize;
  oHeader.iSegmentsTotal = iSegmentsTotal;
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  write( hFile, &oHeader, QHEADER_SIZE );

  //initialize the readers records
  oReader.iActive = 0;
  oReader.iTail = 0;
  memset( oReader.szName, 0, sizeof( oReader.szName ) );
  for( i = 0; i < QREADERS_TOTAL; i++ ) {
    lseek( hFile, QREADERS_OFFSET + i * QREADER_SIZE, SEEK_SET );
    write( hFile, &oReader, QREADER_SIZE );
  }

  //initialize the message type description records
  oMsgType.iActive = 0;
  memset( oMsgType.szName, 0, sizeof( oMsgType.szName ) );
  for( i = 0; i < MSGTYPES_TOTAL; i++ ) {
    lseek( hFile, MSGTYPES_OFFSET + i * MSGTYPE_SIZE, SEEK_SET );
    write( hFile, &oMsgType, MSGTYPE_SIZE );
  }

  //initialized segments
  iSegmentSize = GET_SEGMENT_SIZE( oHeader.iSegmentDataSize );
  pSegment = calloc( iSegmentSize, 1 );
  pSegment->byMsgType = 0;
  pSegment->byTotal = 1;
  pSegment->byCount = 0;
  for( i = 0; i < oHeader.iSegmentsTotal; i++ ) {
    lseek( hFile, SEGMENTS_OFFSET + i * iSegmentSize, SEEK_SET );
    write( hFile, pSegment, iSegmentSize );
  }
  free( pSegment );
}
@ Prototype.
@<Prototypes@>+=
static void InitQFile( int iSegmentDataSize, int iSegmentsTotal );


@* Init. A call to this function first checks to see if the file is already
open. If not, it attempts to open the file.  If this fails because the file
does not exist, the file is created and initialized. Returns 1 on success
or 0 on error.
@<Functions@>+=
static int q_init( void ) {
  int bInit = 0;
  TQHeader oQHeader;
  TSMHeader *pSMHeader;

  SM_LOCK();
  FILE_LOCK();
  if( hFile >= 0 ) {
    SM_UNLOCK();
    FILE_UNLOCK();
    return( 1 ); //file is already open
  }

  hFile = open( FILENAME, O_RDWR, 0666 ); //open if it exists
  if( hFile >= 0 ) bInit = 1; 
  else {
    umask( 0 );
    hFile = open( FILENAME, O_RDWR | O_CREAT, 0666 ); //create the file
    if( hFile >= 0 ) {
      bInit = 1;
      InitQFile( 32, 1000000 );
    }
  }

  if( bInit ) {
    lseek( hFile, QHEADER_OFFSET, SEEK_SET );
    read( hFile, &oQHeader, QHEADER_SIZE );
    if( !InitSM() ) {
      close( hFile );
      hFile = -1;
      SM_UNLOCK();
      FILE_UNLOCK();
      return( 0 );
    }
    pSMHeader = ( TSMHeader* )pSM;
    if( pSMHeader->iSegmentDataSize != oQHeader.iSegmentDataSize ) {
      pSMHeader->iSegmentDataSize = oQHeader.iSegmentDataSize;
      pSMHeader->iHead = 0;
      pSMHeader->iTail = 0;
    }
  }

  SM_UNLOCK();
  FILE_UNLOCK();
  return( bInit );
}


@* The |q_check()| function.
@<Functions@>+=
static int q_check( void ) {
  return( hFile );
}



@* The |q_fastsend()| function. This function inserts one message into the
shared memory queue.
\dot |iMsgType| - is a message type (from 1 to |MSGTYPES_TOTAL|);
\dot |pszMsg| - is a message (max length is
                |iSegmentDataSize * SM_SEGMENTS_TOTAL| ).

Function returns: on success - (0...|SM_SEGMENTS_TOTAL|) - number of segments
moved from shared memory to a file, on error - error code (|<0|).
@<Exported Functions@>+=
int q_fastsend( int iMsgType, const char *pszMsg ) {
  int iSegmentDataSize, iSegmentSize, iSegment, iSegments, iFreeSegments;
  int iHead, iTail, iResult = 0;
  TSegment *pSegment;
  TQHeader oQHeader;
  TSMHeader *pSMHeader;

  if( iMsgType < 1 || iMsgType > ( 0x0080 | MSGTYPES_TOTAL ) )
    return( ERRQ_MSGTYPE );
  if( !q_init() ) return( ERRQ_INIT );

  //calculate the number of segments required to store this message
  SM_LOCK();
  iSegmentDataSize = ( ( TSMHeader* )pSM )->iSegmentDataSize;
  iSegments = ( strlen( pszMsg ) + iSegmentDataSize - 1 ) / iSegmentDataSize;
  if( iSegments >= SM_SEGMENTS_TOTAL ) {
    SM_UNLOCK();
    return( ERRQ_MSGTOOLONG );
  }
  iSegmentSize = GET_SEGMENT_SIZE( iSegmentDataSize );
  pSMHeader = ( TSMHeader* )pSM;
  iHead = pSMHeader->iHead;
  iTail = pSMHeader->iTail;

  iFreeSegments = CircDistance( iTail, iHead, SM_SEGMENTS_TOTAL );
  if( iFreeSegments <= iSegments ) {
    FILE_LOCK();
    lseek( hFile, QHEADER_OFFSET, SEEK_SET );
    read( hFile, &oQHeader, QHEADER_SIZE );
    if( oQHeader.iSegmentDataSize == iSegmentDataSize ) {
      //move shared memory messages to file
      while( iHead != iTail ) {
        pSegment = ( TSegment* )( pSM + SM_SEGMENTS_OFFSET+iTail*iSegmentSize);
        lseek( hFile, SEGMENTS_OFFSET + oQHeader.iHead * iSegmentSize,SEEK_SET);
        write( hFile, pSegment, iSegmentSize );
        oQHeader.iHead = ( oQHeader.iHead + 1 ) % oQHeader.iSegmentsTotal;
        iTail = ( iTail + 1 ) % SM_SEGMENTS_TOTAL;
        iResult++;
      }
      pSMHeader->iTail = iTail;
      lseek( hFile, QHEADER_OFFSET, SEEK_SET );
      write( hFile, &oQHeader, QHEADER_SIZE );
      FILE_UNLOCK();
    }
    else { //ignore all data and reset the shared memory buffer
      FILE_UNLOCK();
      pSMHeader->iSegmentDataSize = oQHeader.iSegmentDataSize;
      pSMHeader->iHead = 0;
      pSMHeader->iTail = 0;
      iSegmentDataSize = pSMHeader->iSegmentDataSize;
      iSegments = ( strlen(pszMsg) + iSegmentDataSize - 1 ) / iSegmentDataSize;
      if( iSegments >= SM_SEGMENTS_TOTAL ) {
        SM_UNLOCK();
        return( ERRQ_MSGTOOLONG );
      }
      iSegmentSize = GET_SEGMENT_SIZE( iSegmentDataSize );
      iHead = pSMHeader->iHead;
    }
  }

  //copy new message to shared memory queue
  pSegment = calloc( iSegmentSize, 1 );
  pSegment->byMsgType = iMsgType;
  pSegment->byTotal = iSegments;
  for( iSegment = 0; iSegment < iSegments; iSegment++ ) {
    pSegment->byCount = iSegment;
    strncpy(pSegment->szData,pszMsg+iSegment*iSegmentDataSize,iSegmentDataSize);
    memcpy( pSM + SM_SEGMENTS_OFFSET+iHead*iSegmentSize, pSegment,iSegmentSize);
    iHead = ( iHead + 1 ) % SM_SEGMENTS_TOTAL;
  }
  pSMHeader->iHead = iHead;
  free( pSegment );
  SM_UNLOCK();
  return( iResult );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_fastsend( int iMsgType, const char *pszMsg );


@ Test Stub.  A test stub program |q_fastsend| is also created.
@(q_fastsend.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iMsgType, iResult;
  char *pszMsgType, *pszMsg, *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s {<MsgType>|<MsgTypeName>} [<Msg>]\n", argv[0]);
    exit( 1 );
  }

  if( ( iMsgType = atoi( argv[ 1 ] ) ) <= 0 ) {
    if( ( iMsgType = q_gettypebyname( argv[ 1 ] ) ) < 0 ) {
      pszErr = q_errstr( iMsgType );
      fprintf( stdout, "Error %d: %s\n", iMsgType, pszErr );
      free( pszErr );
      exit( 1 );
    }
  }

  if( argc == 3 ) asprintf( &pszMsg, "%s", argv[ 2 ] );
  else asprintf( &pszMsg, "" );

  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, "Sending msg \"%s\" of type [%s:%d]... ",
             pszMsg, pszMsgType, iMsgType );
    free( pszMsgType );
  }
  else fprintf( stdout, "Sending msg \"%s\" of type [%d]... ",
                 pszMsg, iMsgType );
  fflush( stdout );

  if( ( iResult = q_fastsend( iMsgType, pszMsg ) ) >= 0 ) fputs("OK\n",stdout);
  else {
    pszErr = q_errstr( iResult );
    fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
    free( pszErr );
  }
  fflush( stdout );

  free( pszMsg );
}



@* The |q_fastsendremote()| function. Inserts one message into the shared
memory queue. This message has a special type (127) and will be processed by
QClient process. All other processes shoul ignore message type 127.
\dot |pszHost| - host name or IP address, where the message should be processed
(up to 32 characters).
\dot |iMsgType| - is a message type (from 2 to |MSGTYPES_TOTAL|);
\dot |pszMsg| - is a msg (max length is
|iSegmentDataSize * SM_SEGMENTS_TOTAL - 35| ).

Function returns: on success - (0...|SM_SEGMENTS_TOTAL|) - number of segments
moved from shared memory to a file, on error - error code (|<0|).
@<Exported Functions@>+=
int q_fastsendremote( const char *pszHost, int iMsgType, const char *pszMsg ) {
  int iLen, iResult;
  char *pszNewMsg;

  if( ( iLen = strlen( pszHost ) ) < 1 || iLen > 32 ) return( ERRQ_HOST );
  if( iMsgType < 1 || iMsgType > MSGTYPES_TOTAL ) return( ERRQ_MSGTYPE );

  asprintf( &pszNewMsg, "%-32s%03.3d%s", pszHost, iMsgType, pszMsg );
  iResult = q_fastsend( 127, pszNewMsg );
  free( pszNewMsg );
  return( iResult );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_fastsendremote( const char *pszHost, int iMsgType, const char *pszMsg );



@* The |q_sync()| function. Move messages from the shared memory to a file.
Function returns: on success - (0...|SM_SEGMENTS_TOTAL|) - number of segments
moved from shared memory to a file, on error - error code (|<0|).
@<Exported Functions@>+=
int q_sync( void ) {
  int iHead, iTail, iSegmentDataSize, iSegmentSize, iSegment, iResult = 0;
  TSMHeader *pSMHeader;
  TQHeader oQHeader;
  void *pSegments;

  if( !q_init() ) return( ERRQ_INIT );

  SM_LOCK();
  pSMHeader = ( TSMHeader* )pSM;
  iHead = pSMHeader->iHead;
  iTail = pSMHeader->iTail;
  if( iHead == iTail ) { //no segments to move
    SM_UNLOCK();
    return( 0 );
  }

  //copy messages from shared memory to local buffer
  iSegmentDataSize = pSMHeader->iSegmentDataSize;

  iSegmentSize = GET_SEGMENT_SIZE( iSegmentDataSize );
  pSegments = calloc( iSegmentSize * SM_SEGMENTS_TOTAL, 1 );
  do {
    memcpy( pSegments + iResult * iSegmentSize,
            pSM + SM_SEGMENTS_OFFSET + iTail * iSegmentSize, iSegmentSize );
    iTail = ( iTail + 1 ) % SM_SEGMENTS_TOTAL;
    iResult++;
  } while( iHead != iTail );
  pSMHeader->iTail = iTail;
  FILE_LOCK();
  SM_UNLOCK();

  //copy messages from a local buffer to a file
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  if( oQHeader.iSegmentDataSize == iSegmentDataSize ) {
    for( iSegment = 0; iSegment < iResult; iSegment++ ) {
      lseek( hFile, SEGMENTS_OFFSET + oQHeader.iHead * iSegmentSize, SEEK_SET );
      write( hFile, pSegments + iSegment * iSegmentSize, iSegmentSize );
      oQHeader.iHead = ( oQHeader.iHead + 1 ) % oQHeader.iSegmentsTotal;
    }
    lseek( hFile, QHEADER_OFFSET, SEEK_SET );
    write( hFile, &oQHeader, QHEADER_SIZE );
    FILE_UNLOCK();
    free( pSegments );
  }
  else {  //ignore all data and reset the shared memory buffer
    SM_LOCK();
    FILE_UNLOCK();
    free( pSegments );
    pSMHeader->iSegmentDataSize = oQHeader.iSegmentDataSize;
    pSMHeader->iHead = 0;
    pSMHeader->iTail = 0;
    SM_UNLOCK();
  }
  return( iResult );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_sync( void );


@ Test Stub. A test stub program |q_sync| is also created.
@(q_sync.c@>=
#include <unistd.h>
#include <rds_trn.h>
#include <rds_q.h>
main( void ) {
  int iSegments;

  trn_register( "q_sync" );
  Trace( "Started" );

  for( ; ; usleep( 10000 ) ) {
    if( ( iSegments = q_sync() ) > 0 ) Trace( "Segments: %d", iSegments );
  }
}


@ Another Test Stub. A test stub program |q_flush| is also created.
@(q_flush.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( void ) {
  int iSegments;
  char *pszErr;

  fputs( "Flushing data from shared memory to the disk file... ", stdout );
  iSegments = q_sync();
  fputs( "\n", stdout );

  if( iSegments >= 0 ) fprintf( stdout, "Done. Saved %d segments\n", iSegments );
  else {
    pszErr = q_errstr( iSegments );
    fprintf( stdout, "Error %d: %s\n", iSegments, pszErr );
    free( pszErr );
  }
}



@* The |q_send()| function. Inserts one message into the message queue file.
\dot |iMsgType| - is a message type (from 1 to |MSGTYPES_TOTAL|);
\dot |pszMsg| - is a msg (max length is |iSegmentDataSize * SM_SEGMENTS_TOTAL|)
Function returns: on success - (0...|SM_SEGMENTS_TOTAL|) - number of segments
moved from shared memory to a file, on error - error code (|<0|).
@<Exported Functions@>+=
int q_send( int iMsgType, const char *pszMsg ) {
  int iSegment, iSegments, iSegmentDataSize, iSegmentSize, iHead, iTail, iResult = 0;
  TSMHeader *pSMHeader;
  TQHeader oQHeader;
  TSegment *pSegment;
  void *pSegments;

  if( iMsgType < 1 || iMsgType > ( 0x0080 | MSGTYPES_TOTAL ) )
    return( ERRQ_MSGTYPE );
  if( !q_init() ) return( ERRQ_INIT );

  SM_LOCK();
  pSMHeader = ( TSMHeader* )pSM;
  iHead = pSMHeader->iHead;
  iTail = pSMHeader->iTail;
  iSegmentDataSize = pSMHeader->iSegmentDataSize;
  iSegmentSize = GET_SEGMENT_SIZE( iSegmentDataSize );

  if( iHead != iTail ) {
    //copy messages from shared memory to a local buffer
    pSegments = calloc( iSegmentSize * SM_SEGMENTS_TOTAL, 1 );
    for( ; iHead != iTail; iTail = ( iTail + 1 ) % SM_SEGMENTS_TOTAL ) {
      memcpy( pSegments + iResult * iSegmentSize,
              pSM + SM_SEGMENTS_OFFSET + iTail * iSegmentSize, iSegmentSize );
      iResult++;
    }
    pSMHeader->iTail = iTail;
    FILE_LOCK();
    SM_UNLOCK();

    //copy messages from a local buffer to a file
    lseek( hFile, QHEADER_OFFSET, SEEK_SET );
    read( hFile, &oQHeader, QHEADER_SIZE );
    if( oQHeader.iSegmentDataSize == iSegmentDataSize ) {
      for( iSegment = 0; iSegment < iResult; iSegment++ ) {
        lseek( hFile, SEGMENTS_OFFSET + oQHeader.iHead * iSegmentSize,SEEK_SET);
        write( hFile, pSegments + iSegment * iSegmentSize, iSegmentSize );
        oQHeader.iHead = ( oQHeader.iHead + 1 ) % oQHeader.iSegmentsTotal;
      }
      lseek( hFile, QHEADER_OFFSET, SEEK_SET );
      write( hFile, &oQHeader, QHEADER_SIZE );
      free( pSegments );
    }
    else {  //ignore all data and reset the shared memory buffer
      SM_LOCK();
      FILE_UNLOCK();
      free( pSegments );
      pSMHeader->iSegmentDataSize = oQHeader.iSegmentDataSize;
      pSMHeader->iHead = 0;
      pSMHeader->iTail = 0;
      FILE_LOCK();
      SM_UNLOCK();
    }
  }
  else {
    FILE_LOCK();
    SM_UNLOCK();
  }

  //copy the current message to a file
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );          //get the head
  iSegmentSize = GET_SEGMENT_SIZE( oQHeader.iSegmentDataSize );
  iSegments = ( strlen( pszMsg ) + oQHeader.iSegmentDataSize - 1 ) /
              oQHeader.iSegmentDataSize;
  if( iSegments > SM_SEGMENTS_TOTAL ) {
    FILE_UNLOCK();
    return( ERRQ_MSGTOOLONG );
  }
  pSegment = calloc( iSegmentSize, 1 );
  pSegment->byMsgType = iMsgType;
  pSegment->byTotal = iSegments;

  for( iSegment = 0; iSegment < iSegments; iSegment++ ) {
    pSegment->byCount = iSegment;
    strncpy( pSegment->szData, pszMsg  + iSegment * oQHeader.iSegmentDataSize,
             oQHeader.iSegmentDataSize );
    lseek( hFile, SEGMENTS_OFFSET + oQHeader.iHead * iSegmentSize, SEEK_SET );
    write( hFile, pSegment, iSegmentSize );
    oQHeader.iHead = ( oQHeader.iHead + 1 ) % oQHeader.iSegmentsTotal;
  }
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  write( hFile, &oQHeader, QHEADER_SIZE ); //update the head
  free( pSegment );
  FILE_UNLOCK();

  return( iResult );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_send( int iMsgType, const char *pszMsg ) ;


@ Test Stub.  A test stub program |q_send| is also created.
@(q_send.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iMsgType, iResult;
  char *pszMsgType, *pszMsg, *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s {<MsgType>|<MsgTypeName>} [<Msg>]\n",
             argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iMsgType = atoi( argv[ 1 ] ) ) <= 0 ) {
    if( ( iMsgType = q_gettypebyname( argv[ 1 ] ) ) < 0 ) {
      pszErr = q_errstr( iMsgType );
      fprintf( stdout, "Error %d: %s\n", iMsgType, pszErr );
      fflush( stdout );
      free( pszErr );
      exit( 1 );
    }
  }

  if( argc == 3 ) asprintf( &pszMsg, "%s", argv[ 2 ] );
  else asprintf( &pszMsg, "" );

  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, "Sending msg \"%s\" of type [%s:%d]... ",
             pszMsg, pszMsgType, iMsgType );
    free( pszMsgType );
  }
  else fprintf( stdout, "Sending msg \"%s\" of type [%d]... ",
                 pszMsg, iMsgType );
  fflush( stdout );

  if( ( iResult = q_send( iMsgType, pszMsg ) ) >= 0 ) fputs( "OK\n", stdout );
  else {
    pszErr =  q_errstr( iResult );
    fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
    free( pszErr );
  }
  fflush( stdout );
  free( pszMsg );
}



@* The |q_sendremote()| function. Inserts one message into the queue file.
The message has a special type (127) and will be processed by QClient process.
All other processes shoul ignore message type 127.
\dot |pszHost| - host name or IP address, where the message should be processed
(up to 32 characters).
\dot |iMsgType| - is a message type (from 1 to |MSGTYPES_TOTAL|);
\dot |pszMsg| - is a msg (max length is
|iSegmentDataSize * SM_SEGMENTS_TOTAL - 35| ).

Function returns: on success - (0...|SM_SEGMENTS_TOTAL|) - number of segments
moved from shared memory to a file, on error - error code (|<0|).
@<Exported Functions@>+=
int q_sendremote( const char *pszHost, int iMsgType, const char *pszMsg ) {
  int iLen, iResult;
  char *pszNewMsg;

  if( ( iLen = strlen( pszHost ) ) < 1 || iLen > 32 ) return( ERRQ_HOST );
  if( iMsgType < 1 || iMsgType > MSGTYPES_TOTAL ) return( ERRQ_MSGTYPE );

  asprintf( &pszNewMsg, "%-32s%03.3d%s", pszHost, iMsgType, pszMsg );
  iResult = q_send( 127, pszNewMsg );
  free( pszNewMsg );
  return( iResult );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_sendremote( const char *pszHost, int iMsgType, const char *pszMsg );


@* The |q_rawtype()| function.
@<Exported Functions@>+=
int q_rawtype( int iReader ) {
  int iHead, iTail, iSegmentSize, iMsgType;
  TQHeader oQHeader;
  TQReader oQReader;
  TSegment *pSegment;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( ERRQ_READER );
  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  //get a head and a tail
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  iHead = oQHeader.iHead;
  iTail = oQReader.iTail;

  iSegmentSize = GET_SEGMENT_SIZE( oQHeader.iSegmentDataSize );

  //align to a message boundary
  pSegment = calloc( iSegmentSize, 1 );
  for( ; iHead != iTail; iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal ) {
    lseek( hFile, SEGMENTS_OFFSET + iTail * iSegmentSize, SEEK_SET );
    read( hFile, pSegment, iSegmentSize );
    //check if it is the 1st segment of msg
    if( pSegment->byCount == 0 && pSegment->byMsgType != 0 ) break;
  }

  iMsgType = ( iHead != iTail ) ? pSegment->byMsgType: 0;
  free( pSegment );
  FILE_UNLOCK();
  return( iMsgType );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_rawtype( int iReader );


@ Test Stub.  A test stub program |q_rawtype| is also created.
@(q_rawtype.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iReader, iMsgType, iMsgRawType;
  char *pszReader, *pszMsgType, *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s {<Reader>|<ReaderName>}\n", argv[ 0 ] );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
    pszErr = q_errstr( iReader );
    fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
    fprintf( stdout, "Reader [%s:%d] => msg raw type... ", pszReader, iReader );
    free( pszReader );
  }
  else fprintf( stdout, "Reader [%d] => msg raw type... ", iReader );
  fflush( stdout );

  if( ( iMsgRawType = q_rawtype( iReader ) ) < 0 ) {
    pszErr = q_errstr( iMsgRawType );
    fprintf( stdout, "Error %d: %s\n", iMsgRawType, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( iMsgType = q_type( iReader ) ) < 0 ) {
    fprintf( stdout, "[%d]\n", iMsgRawType );
    pszErr = q_errstr( iMsgType );
    fprintf( stdout, "Error %d: %s\n", iMsgType, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, "[%s:%d]\n", pszMsgType, iMsgRawType );
    free( pszMsgType );
  }
  else fprintf( stdout, "[%d]\n", iMsgRawType );
  fflush( stdout );
}



@* The |q_type()| function. Returns a message type of the message at the
reader's tail.
@<Exported Functions@>+=
int q_type( int iReader ) {
  int iMsgType;

  if( ( iMsgType = q_rawtype( iReader ) ) > 0 ) iMsgType &= 0x007F;
  return( iMsgType );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_type( int iReader );


@ Test Stub.  A test stub program |q_type| is also created.
@(q_type.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iReader, iMsgType;
  char *pszReader, *pszMsgType, *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s {<Reader>|<ReaderName>}\n", argv[ 0 ] );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
    pszErr = q_errstr( iReader );
    fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
    fprintf( stdout, "Reader [%s:%d] => msg type... ", pszReader, iReader );
    free( pszReader );
  }
  else fprintf( stdout, "Reader [%d] => msg type... ", iReader );
  fflush( stdout );

  if( ( iMsgType = q_type( iReader ) ) < 0 ) {
    pszErr = q_errstr( iMsgType );
    fprintf( stdout, "Error %d: %s\n", iMsgType, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, "[%s:%d]\n", pszMsgType, iMsgType );
    free( pszMsgType );
  }
  else fprintf( stdout, "[%d]\n", iMsgType );
  fflush( stdout );
}



@* The |q_recv()| function. Returns a message at the reader's tail as a ptr
to the null-terminated string. The caller should free the allocated pointer
when done. Returning value is not NULL on success, or NULL when queue is empty
or on error (queue initialization error, invalid reader number, memory
allocation error).
@<Exported Functions@>+=
char *q_recv( int iReader ) {
  int i, iHead, iTail, iSegmentSize, iTotal;
  TQHeader oQHeader;
  TQReader oQReader;
  TSegment *pSegment;
  char *pszMsg = NULL;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return(NULL);//invalid reader
  if( !q_init() ) return( NULL );                 //queue initialization error

  FILE_LOCK();
  //get a head
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  iHead = oQHeader.iHead;

  //get a tail
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  iTail = oQReader.iTail;

  iSegmentSize = GET_SEGMENT_SIZE( oQHeader.iSegmentDataSize );

  //align to a message boundary
  pSegment = calloc( iSegmentSize, 1 );
  for( ; iHead != iTail; iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal ) {
    lseek( hFile, SEGMENTS_OFFSET + iTail * iSegmentSize, SEEK_SET );
    read( hFile, pSegment, iSegmentSize );
    //check if it is the 1st segment of msg
    if( pSegment->byCount == 0 && pSegment->byMsgType != 0 ) break;
  }

  if( iHead == iTail ) {
    FILE_UNLOCK();
    free( pSegment );
    return( NULL );  //queue is empty
  }

  iTotal = pSegment->byTotal;

  //allocate a buffer for a message
  if( ( pszMsg = malloc( iTotal * oQHeader.iSegmentDataSize + 1 ) ) == NULL ) {
    FILE_UNLOCK();
    free( pSegment );
    return( NULL );  //memory allocation error
  }
  pszMsg[ iTotal * oQHeader.iSegmentDataSize ] = '\0';

  //copy data
  for( i = 0; i < iTotal; i++ ) {
    lseek( hFile, SEGMENTS_OFFSET + iTail * iSegmentSize, SEEK_SET );
    read( hFile, pSegment, iSegmentSize );
    memcpy( pszMsg + i * oQHeader.iSegmentDataSize, pSegment->szData,
            oQHeader.iSegmentDataSize );
    iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal;
  }
  FILE_UNLOCK();
  free( pSegment );
  return( pszMsg );
}


@ Prototype.
@<Exported Prototypes@>+=
char *q_recv( int iReader );


@ Test Stub. A test stub program |q_recv| is also created.
@(q_recv.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iReader, iMsgType;
  char *pszReader, *pszMsgType, *pszMsg, *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s {<Reader>|<ReaderName>}\n", argv[ 0 ] );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
    pszErr = q_errstr( iReader );
    fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
    fprintf( stdout, "Reader [%s:%d] received msg type ", pszReader, iReader );
    free( pszReader );
  }
  else fprintf( stdout, "Reader [%d] received msg type ", iReader );
  fflush( stdout );

  if( ( iMsgType = q_type( iReader ) ) < 0 ) {
    pszErr = q_errstr( iMsgType );
    fprintf( stdout, "- Error %d: %s\n", iMsgType, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, "[%s:%d] ", pszMsgType, iMsgType );
    free( pszMsgType );
  }
  else fprintf( stdout, "[%d] ", iMsgType );
  fflush( stdout );

  if( iMsgType == 0 ) fprintf( stdout, "(queue is empty)\n" );
  else if( ( pszMsg = q_recv( iReader ) ) == NULL ) {
    fprintf( stdout, " - Error in in q_recv()" );
    fflush( stdout );
    exit( 1 );
  }
  else {
    fprintf( stdout, "of %d chars:\n%s\n", strlen( pszMsg ), pszMsg );
    free( pszMsg );
  }
  fflush( stdout );
}



@* The |q_nextrawtype()| function. Bumps a Reader's tail to the next message
and gets raw type of the next message. On success returns raw message type
(from 1 to 255), on error returns (|<0|).
@<Exported Functions@>+=
int q_nextrawtype( int iReader ) {
  int i, iHead, iTail, iSegmentSize, iTotal, iRawMsgType;
  TQHeader oQHeader;
  TQReader oQReader;
  TSegment *pSegment;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( ERRQ_READER );
  if( !q_init() ) return( ERRQ_INIT );

  //get a head and a tail
  FILE_LOCK();
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  FILE_UNLOCK();
  iHead = oQHeader.iHead;
  iTail = oQReader.iTail;

  iSegmentSize = GET_SEGMENT_SIZE( oQHeader.iSegmentDataSize );

  //align to a message boundary
  pSegment = calloc( iSegmentSize, 1 );
  for( ; iHead != iTail; iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal ) {
    lseek( hFile, SEGMENTS_OFFSET + iTail * iSegmentSize, SEEK_SET );
    read( hFile, pSegment, iSegmentSize );
    //check if it is the 1st segment of msg
    if( pSegment->byCount == 0 && pSegment->byMsgType != 0 ) break;
  }

  if( iHead != iTail ) {
    //walk through the record, stopping if we hit the head
    iTotal = pSegment->byTotal;
    for( i = 0; i < iTotal; i++ ) {
      iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal;
      if( iHead != iTail ) break;
    }

    //align to the next message boundary
    for( ; iHead != iTail; iTail = ( iTail + 1 ) % oQHeader.iSegmentsTotal ) {
      lseek( hFile, SEGMENTS_OFFSET + iTail * iSegmentSize, SEEK_SET );
      read( hFile, pSegment, iSegmentSize );
      //check if it is the 1st segment of msg
      if( pSegment->byCount == 0 && pSegment->byMsgType != 0 ) break;
    }
  }

  //update the tail
  oQReader.iTail = iTail;
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  write( hFile, &oQReader, QREADER_SIZE );

  iRawMsgType = ( iHead != iTail ) ? pSegment->byMsgType : 0;
  free( pSegment );
  return( iRawMsgType );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_nextrawtype( int handle );



@* The |q_nexttype()| function. Bumps a Reader's tail to the next message
and gets raw type of the next message. On success returns raw message type
(from 1 to 127), on error returns (|<0|).
@<Exported Functions@>+=
int q_nexttype( int iReader ) {
  int iMsgType;

  if( ( iMsgType = q_nextrawtype( iReader ) ) > 0 ) iMsgType &= 0x007F;
  return( iMsgType );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_nexttype( int iReader );


@ Test Stub.  A test stub program |q_next| is also created.
@(q_next.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h> 
main(int argc, char *argv[]) {
  int iReader, iMsgType;
  char *pszReader, *pszMsgType, *pszErr;

  if( argc != 2 ) {
    fprintf( stderr, "Usage:  %s {<Reader>|<ReaderName>}\n", argv[ 0 ] );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
    pszErr = q_errstr( iReader );
    fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
    fprintf( stdout, "Reader [%s:%d] is jumping to the next msg... ",
             pszReader, iReader );
    free( pszReader );
  }
  else fprintf( stdout, "Reader [%d] is jumping to the next msg... ",iReader);
  fflush( stdout );

  if( ( iMsgType = q_nexttype( iReader ) ) < 0 ) {
    pszErr = q_errstr( iMsgType );
    fprintf( stdout, "Error %d: %s\n", iMsgType, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( pszMsgType = q_gettypename( iMsgType ) ) != NULL ) {
    fprintf( stdout, " Type [%s:%d]\n", pszMsgType, iMsgType );
    free( pszMsgType );
  }
  else fprintf( stdout, "Type [%d]\n", iMsgType );
  fflush( stdout );

}



@* The |q_gethead()| function. Returns a head of the queue (|>=0|) on success,
or error code (|<0|) on error.
@<Exported Functions@>+=
int q_gethead( void ) {
  TQHeader oQHeader;

  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  FILE_UNLOCK();
  return( oQHeader.iHead );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_gethead( void );



@* The |q_gettail()| function. Returns a reader's tail (|>=0|) on success,
or error code (|<0|) on error.
@<Exported Functions@>+=
int q_gettail( int iReader ) {
  TQReader oQReader;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( ERRQ_READER );
  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  FILE_UNLOCK();
  return( oQReader.iTail );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_gettail( int iReader );



@* The |q_settail()| function. Set a reader's tail. Return a tail (|>=0|) on
success, or error code (|<0|) on error.
@<Exported Functions@>+=
int q_settail( int iReader, int iTail ) {
  TQHeader oQHeader;
  TQReader oQReader;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( ERRQ_READER );
  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  if( iTail < 0 || iTail >= oQHeader.iSegmentsTotal ) {
    FILE_UNLOCK();
    return( ERRQ_TAIL );
  }
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  oQReader.iTail = iTail;
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  write( hFile, &oQReader, QREADER_SIZE );
  FILE_UNLOCK();

  return( iTail );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_settail( int iReader, int iTail );


@ Test Stub. A test stub program |q_settail| is also created.
@(q_settail.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iReader, iTail, iResult;
  char *pszReader, *pszErr;

  if( argc != 3 ) {
    fprintf( stderr, "Usage:  %s {<Reader>|<ReaderName>} <Tail>\n", argv[0] );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
    pszErr = q_errstr( iReader );
    fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }

  if( ( iTail = atoi( argv[ 2 ] ) ) > 0 ) ;
  else if( iTail == 0 && argv[ 2 ][ 0 ] == '0' ) ;
  else {
    fprintf( stdout, "Error: [%s] is not valid decimal number for <Tail>\n",
             argv[ 2 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
    fprintf( stdout, "Setting tail index %d for reader [%s:%d]... ",
             iTail, pszReader, iReader );
    free( pszReader );
  }
  else fprintf( stdout, "Setting tail index %d for reader [%d]... ",
                iTail, iReader );
  fflush( stdout );


  if( ( iResult = q_settail( iReader, iTail ) ) >= 0 ) fputs( "OK\n", stdout );
  else {
    pszErr = q_errstr( iResult );
    fprintf( stdout, " Error %d: %s\n", iResult, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  fflush( stdout );
}



@* The |q_empty()| function. Empties the queue by setting all reader's tail
indexes equal to the head index. On success returns the head index (from 0
to |iSegmentsTotal - 1| ), on error returns error code (|<0|).
@<Exported Functions@>+=
int q_empty( void ) {
  int iReader;
  TQHeader oQHeader;
  TQReader oQReader;

  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );

  for( iReader = 0; iReader < QREADERS_TOTAL; iReader++ ) {
    lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
    read( hFile, &oQReader, QREADER_SIZE );

    oQReader.iTail = oQHeader.iHead;

    lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
    write( hFile, &oQReader, QREADER_SIZE );
  }

  FILE_UNLOCK();
  return( oQHeader.iHead );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_empty( void );


@ Test Stub. A test stub program |q_empty| is also created.
@(q_empty.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
main( int argc, char *argv[] ) {
  int iReader, iResult;
  char *pszReader, szCmd[ 16 ], *pszErr;

  if( argc == 1 ) {
    fprintf( stdout, "Usage:  %s [<Reader>|<ReaderName>]\n", argv[ 0 ] );
    fputs( "You are going to reset the tail indexes for all queue readers.\n",
           stdout );
    fputs( "Do you really want to continue? [Y/N] ", stdout );
    fflush( stdout );

    fgets( szCmd, sizeof( szCmd ), stdin );
    if( szCmd[ 0 ] == 'Y' || szCmd[ 0 ] == 'y' ) {
      iResult = q_empty();
      if( iResult < 0 ) {
        pszErr = q_errstr( iResult );
        fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
        fflush( stdout );
        free( pszErr );
        exit( 1 );
      }
      else {
        fprintf( stdout, "Done. Head is %d\n", iResult );
        fflush( stdout );
      }
    }
  }
  else if( argc == 2 ) {
    if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
    else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
    else if( ( iReader = q_getreaderbyname( argv[ 1 ] ) ) < 0 ) {
      pszErr = q_errstr( iReader );
      fprintf( stdout, "Error %d: %s\n", iReader, pszErr );
      fflush( stdout );
      free( pszErr );
      exit( 1 );
    }

    if( ( pszReader = q_getreadername( iReader ) ) != NULL ) {
      fprintf( stdout, "You are going to reset the tail index for reader "
               "[%s:%d]\n", pszReader, iReader );
      free( pszReader );
    }
    else fprintf( stdout, "You are going to reset the tail index for reader "
                  "[%d]\n", iReader );
    fprintf( stdout, "Do you really want to continue? [Y/N] " );
    fflush( stdout );

    fgets( szCmd, sizeof( szCmd ), stdin );
    if( szCmd[ 0 ] == 'Y' || szCmd[ 0 ] == 'y' ) {
      if( ( iResult = q_gethead() ) >= 0 ) {
        if( ( iResult = q_settail( iReader, iResult ) ) >= 0 ) {
          fprintf( stdout, "Done. Head is %d\n", iResult );
          fflush( stdout );
        }
        else {
          pszErr = q_errstr( iResult );
          fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
          fflush( stdout );
          free( pszErr );
        }
      }
      else {
        pszErr = q_errstr( iResult );
        fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
        fflush( stdout );
        free( pszErr );
      }
    }
  }
  else {
    fprintf( stdout, "Usage:  %s [<Reader>|<ReaderName>]\n", argv[ 0 ] );
    fflush( stdout );
    exit( 1 );
  }
}



@* The |q_setreadername()| function. Assign the name to a reader.
\dot |iReader| - is a queue reader number,
\dot |pszName| - reader name to assign.

Returns 0 on success, or error code (|<0|) on error.
@<Exported Functions@>+=
int q_setreadername( int iReader, const char *pszName ) {
  TQReader oQReader;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( ERRQ_READER );
  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );

  if( pszName != NULL ) {
    strncpy( oQReader.szName, pszName, NAME_LEN );
    oQReader.szName[ NAME_LEN ] = '\0';
  }
  else oQReader.szName[ 0 ] = '\0';

  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  write( hFile, &oQReader, QREADER_SIZE );
  FILE_UNLOCK();
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_setreadername( int iReader, const char *pszName );


@ Test Stub.  A test stub program |q_readername| is also created.
@(q_readername.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h> 
main( int argc, char *argv[] ) {
  int iReader, iResult;
  char *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s <Reader> [<ReaderName>]\n", argv[0] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iReader = atoi( argv[ 1 ] ) ) > 0 ) ;
  else if( iReader == 0 && argv[ 1 ][ 0 ] == '0' ) ;
  else {
    fprintf( stdout, "Error: [%s] is not valid decimal value for <Reader>\n",
             argv[ 1 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( argc == 3 ) {
    fprintf( stdout, "Setting name \"%s\" for reader %d... ",argv[2],iReader);
    fflush( stdout );
    iResult = q_setreadername( iReader, argv[ 2 ] );
  }
  else {
    fprintf( stdout, "Clearing name for reader %d... ", iReader );
    fflush( stdout );
    iResult = q_setreadername( iReader, NULL );
  }

  if( iResult < 0 ) {
    pszErr = q_errstr( iResult );
    fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  else fputs( "OK\n", stdout );
  fflush( stdout );
}



@* The |q_getreadername()| function. Returns the reader's name.
\dot |iReader| - is a queue reader number.
The function returns: on success - ptr to buffer with the reader name; or NULL
if no reader name was assigned, or on error (invalid reader number, queue
initialization error, or memory allocation error).
@<Exported Functions@>+=
char *q_getreadername( int iReader ) {
  TQReader oQReader;
  char *pszName;

  if( iReader < 0 || iReader >= QREADERS_TOTAL ) return( NULL );
  if( !q_init() ) return( NULL );

  FILE_LOCK();
  lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
  read( hFile, &oQReader, QREADER_SIZE );
  FILE_UNLOCK();

  if( strlen( oQReader.szName ) <= 0 ) return( NULL );

  if( ( pszName = calloc( NAME_LEN + 1, 1 ) ) == NULL ) return( NULL );
  strncpy( pszName, oQReader.szName, NAME_LEN );
  return( pszName );
}

@ Prototype.
@<Exported Prototypes@>+=
char *q_getreadername( int iReader );



@* The |q_getreaderbyname()| function. Returns the reader number.
\dot |pszReader| - ptr to the name of the reader.
The function returns: on success - iReader (|>=0|), on error - error code (|<0|)
@<Exported Functions@>+=
int q_getreaderbyname( const char *pszName ) {
  int iReader;
  TQReader oQReader;

  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  for( iReader = 1; iReader <= QREADERS_TOTAL; iReader++ ) {
    lseek( hFile, QREADERS_OFFSET + iReader * QREADER_SIZE, SEEK_SET );
    read( hFile, &oQReader, QREADER_SIZE );
    if( strncasecmp( oQReader.szName, pszName, NAME_LEN ) == 0 ) {
      FILE_UNLOCK();
      return( iReader );
    }
  }

  FILE_UNLOCK();
  return( ERRQ_READERNAME );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_getreaderbyname( const char *pszName );



@* The |q_settypename()| function. Assigns name to the message type.
|iMsgType| - is type of the message, |pszName| - message type name to assign.
Returns 0 on success, or error code (|<0|) on error.
@<Exported Functions@>+=
int q_settypename( int iMsgType, const char *pszName ) {
  TMsgType oMsgType;

  if( iMsgType < 1 || iMsgType > MSGTYPES_TOTAL ) return( ERRQ_MSGTYPE );
  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, MSGTYPES_OFFSET + ( iMsgType - 1 ) * MSGTYPE_SIZE, SEEK_SET );
  read( hFile, &oMsgType, MSGTYPE_SIZE );

  if( pszName != NULL ) {
    strncpy( oMsgType.szName, pszName, NAME_LEN );
    oMsgType.szName[ NAME_LEN ] = '\0';
  }
  else oMsgType.szName[ 0 ] = '\0';

  lseek( hFile, MSGTYPES_OFFSET + ( iMsgType - 1 ) * MSGTYPE_SIZE, SEEK_SET );
  write( hFile, &oMsgType, MSGTYPE_SIZE );
  FILE_UNLOCK();
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_settypename( int iMsgType, const char *pszName );


@ Test Stub.  A test stub program |q_typename| is also created.
@(q_typename.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h> 
main( int argc, char *argv[] ) {
  int iMsgType, iResult;
  char *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s <MsgType> [<MsgTypeName>]\n", argv[0] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iMsgType = atoi( argv[ 1 ] ) ) <= 0 ) {
    fprintf( stdout, "Error: [%s] is not valid decimal value for <MsgType>\n",
             argv[ 1 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( argc == 3 ) {
    fprintf( stdout, "Setting name \"%s\" for msg type %d... ",
             argv[ 2 ], iMsgType );
    fflush( stdout );
    iResult = q_settypename( iMsgType, argv[ 2 ] );
  }
  else {
    fprintf( stdout, "Clearing name for msg type %d... ", iMsgType );
    fflush( stdout );
    iResult = q_settypename( iMsgType, NULL );
  }

  if( iResult < 0 ) {
    pszErr = q_errstr( iResult );
    fprintf( stdout, "Error %d: %s\n", iResult, pszErr );
    fflush( stdout );
    free( pszErr );
    exit( 1 );
  }
  else fputs( "OK\n", stdout );
  fflush( stdout );
}



@* The |q_gettypename()| function. Returns the message type name.
|iMsgType| - is an integer type of the message.
The function returns: on success - ptr to buffer with the message type name;
NULL - if no msd type name was assigned, or on error (invalid message type,
queue initialization error, or memory allocation error).
@<Exported Functions@>+=
char *q_gettypename( int iMsgType ) {
  TMsgType oMsgType;
  char *pszName;

  if( iMsgType < 1 || iMsgType >= MSGTYPES_TOTAL ) return( NULL );
  if( !q_init() ) return( NULL );

  FILE_LOCK();
  lseek( hFile, MSGTYPES_OFFSET + ( iMsgType - 1 ) * MSGTYPE_SIZE, SEEK_SET );
  read( hFile, &oMsgType, MSGTYPE_SIZE );
  FILE_UNLOCK();

  if( strlen( oMsgType.szName ) <= 0 ) return( NULL );

  if( ( pszName = calloc( NAME_LEN + 1, 1 ) ) == NULL ) return( NULL );
  strncpy( pszName, oMsgType.szName, NAME_LEN );
  return( pszName );
}

@ Prototype.
@<Exported Prototypes@>+=
char *q_gettypename( int iMsgType );


@* The |q_gettypebyname()| function. Returns the message type number.
\dot |pszMsgType| - ptr to the name of the message type.

The function returns: on success - |iMsgType| (|>=0|), on error - error code
(|<0|).
@<Exported Functions@>+=
int q_gettypebyname( const char *pszName ) {
  int iMsgType;
  TMsgType oMsgType;

  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  for( iMsgType = 1; iMsgType <= MSGTYPES_TOTAL; iMsgType++ ) {
    lseek( hFile, MSGTYPES_OFFSET + ( iMsgType - 1 ) * MSGTYPE_SIZE, SEEK_SET );
    read( hFile, &oMsgType, MSGTYPE_SIZE );
    if( strncasecmp( oMsgType.szName, pszName, NAME_LEN ) == 0 ) {
      FILE_UNLOCK();
      return( iMsgType );
    }
  }

  FILE_UNLOCK();
  return( ERRQ_MSGTYPENAME );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_gettypebyname( const char *pszName );



@* The |q_getconfig()| function. Gets |iSegmnentDataSize| and |iSegmentsTotal|.
Returns 0 on success, or error code (|<0|) on error.
@<Exported Functions@>+=
int q_getconfig( int *piSegmentDataSize, int *piSegmentsTotal ) {
  TQHeader oQHeader;

  *piSegmentDataSize = 0;
  *piSegmentsTotal = 0;

  if( !q_init() ) return( ERRQ_INIT );

  FILE_LOCK();
  lseek( hFile, QHEADER_OFFSET, SEEK_SET );
  read( hFile, &oQHeader, QHEADER_SIZE );
  FILE_UNLOCK();

  *piSegmentDataSize = oQHeader.iSegmentDataSize;
  *piSegmentsTotal = oQHeader.iSegmentsTotal;

  if( *piSegmentDataSize < 1 || *piSegmentDataSize > MAX_DATA_LEN )
    return( ERRQ_SEGDATASIZE );

  if( *piSegmentsTotal < SM_SEGMENTS_TOTAL ) return( ERRQ_SEGTOTAL );
  if( ( unsigned )65536 * ( unsigned )32768 / ( unsigned )*piSegmentsTotal <
      ( unsigned )*piSegmentDataSize ) return( ERRQ_SEGTOTAL );

  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_getconfig( int *piSegmentDataSize, int *piSegmentsTotal );


@ Test Stub.  A test stub program |q_info| is also created.
@(q_info.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds.h>
#include <rds_q.h>
@<Defines@>@;
@<Structures@>@;

main( int argc, char *argv[] ) {
  int i, iErr, iSegmentDataSize, iSegmentsTotal, iHead, iTail, iReader;
  int iMsgType;
  char *pszName, *pszErr, szReader[ 32 + 1 ];

  //display configuration settings of the queue
  if( ( iErr = q_getconfig( &iSegmentDataSize, &iSegmentsTotal ) ) >= 0 ) {
    fprintf( stdout, "%-40sTotal number of segments: %d\r", "", iSegmentsTotal);
    fprintf( stdout, "Segment data size:  %d bytes\n", iSegmentDataSize );

    fprintf( stdout, "%-40sFile name: %s\r", "", FILENAME );
    fprintf( stdout, "Max message length: %d characters\n",
             iSegmentDataSize * SM_SEGMENTS_TOTAL );

    fprintf( stdout, "%-40sFile size: %d bytes\r", "", SEGMENTS_OFFSET +
             iSegmentsTotal * GET_SEGMENT_SIZE( iSegmentDataSize ) );
    fprintf( stdout, "Shared memory size: %d bytes\n",
             SM_HEADER_SIZE + sizeof( TSegment ) * SM_SEGMENTS_TOTAL );
  }
  else {
    pszErr = q_errstr( iErr );
    fprintf( stdout, "q_getconfig() err %d: %s\n", iErr, pszErr );
    free( pszErr );
  }

  if( ( iHead = q_gethead() ) < 0 ) {
    pszErr = q_errstr( iHead );
    fprintf( stdout, "q_gethead() err %d: %s", iHead, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "%-19s Head index: %d", "", iHead);
  fflush( stdout );

  //display a list of readers in two columns
  for( i = 0; i < QREADERS_TOTAL; i++ ) {
    iReader = ( ( i % 2 ) ? i - 1 : i + QREADERS_TOTAL ) / 2;
    if( i % 2 == 0 ) fprintf( stdout, "\n%-40s", "" ); //second column

    if( ( pszName = q_getreadername( iReader ) ) != NULL ) {
      sprintf( szReader, "[%s:%d]", pszName, iReader );
      free( pszName );
    }
    else sprintf( szReader, "[%d]", iReader );

    if( ( iTail = q_gettail( iReader ) ) < 0 ) {
      pszErr = q_errstr( iTail );
      fprintf( stdout,"Reader %-12s err %d: %s\r", szReader, iTail, pszErr );
      free( pszErr );
    }
    else fprintf( stdout, "Reader %-12s Tail index: %d\r", szReader, iTail );
    fflush( stdout );
  }
  fputs( "\n\n", stdout );

  //show a list of message types
  fprintf( stdout, "Valid message types are from 1 to %d\n", MSGTYPES_TOTAL );
  for( iMsgType = 1; iMsgType <= MSGTYPES_TOTAL; iMsgType++ ) {
    if( ( pszName = q_gettypename( iMsgType ) ) != NULL ) {
      fprintf( stdout, "Message type %d has a name [%s]\n", iMsgType, pszName );
      free( pszName );
    }
  }
}



@* The |q_setconfig()| function. Sets new values for |iSegmnentDataSize| and
|iSegmentsTotal|. Returns 0 on success, or error code (|<0|) on error.
@<Exported Functions@>+=
int q_setconfig( int iSegmentDataSize, int iSegmentsTotal ) {
  TSMHeader *pSMHeader;

  if( !q_init() ) return( ERRQ_INIT );

  if( iSegmentDataSize < 1 || iSegmentDataSize > MAX_DATA_LEN )
    return( ERRQ_SEGDATASIZE );

  if( iSegmentsTotal < SM_SEGMENTS_TOTAL ) return( ERRQ_SEGTOTAL );
  if( ( ( unsigned )65536 * ( unsigned )32768 - SEGMENTS_OFFSET - 1 ) /
      ( unsigned )iSegmentsTotal <
      ( unsigned )GET_SEGMENT_SIZE( iSegmentDataSize ) ) return( ERRQ_SEGTOTAL);

  SM_LOCK();
  pSMHeader = ( TSMHeader * )pSM;
  pSMHeader->iSegmentDataSize = iSegmentDataSize;
  pSMHeader->iHead = 0;
  pSMHeader->iTail = 0;

  FILE_LOCK();
  SM_UNLOCK();

  close( hFile );
  if( ( hFile = creat( FILENAME, 0666 ) ) < 0 ) {
    FILE_UNLOCK();
    return( ERRQ_INIT );
  }

  InitQFile( iSegmentDataSize, iSegmentsTotal );
  FILE_UNLOCK();
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int q_setconfig( int iSegmentDataSize, int iSegmentsTotal );


@ Test Stub.  A test stub program |q_config| is also created.
@(q_config.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_q.h>
@<Defines@>@;

main( int argc, char *argv[] ) {
  int iErr, iSegmentDataSize, iSegmentsTotal;
  char szCmd[ 16 ], *pszErr;

  if( argc != 3 ) {
    fprintf( stdout, "Usage:\n  %s <NewSegmentDataSize> <NewTotalSegments>\n\n",
             argv[ 0 ] );
    fprintf( stdout, "Where\n  <NewSegmentDataSize> - in bytes, valid values "
                     "are from 1 to %d, which\n", MAX_DATA_LEN );
    fprintf( stdout, "%-24s correspond to the message length upper limits "
                     "from %d\n", "", SM_SEGMENTS_TOTAL );
    fprintf( stdout, "%-24s to %d characters (up to %d segments per message)\n",
                     "", MAX_DATA_LEN * SM_SEGMENTS_TOTAL, SM_SEGMENTS_TOTAL );
    fputs( "\n", stdout );
    fprintf( stdout, "  <NewTotalSegments>   - number of segments in a queue "
                     "storage file,\n" );
    fprintf( stdout, "%-24s valid values are from %d and up. Upper value is\n",
                     "", SM_SEGMENTS_TOTAL );
    fprintf( stdout, "%-24s limited by the max file size of the file system\n",
                     "" );
    fputs( "\n", stdout );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iSegmentDataSize = atoi( argv[ 1 ] ) ) <= 0 ) {
    fprintf( stdout,"[%s] is invalid decimal value for <NewSegmentDataSize>\n",
             argv[ 1 ] );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iSegmentsTotal = atoi( argv[ 2 ] ) ) <= 0 ) {
    fprintf( stdout, "[%s] is invalid decimal value for <NewTotalSegments>\n",
             argv[ 1 ] );
    fflush( stdout );
    exit( 1 );
  }

  fputs( "You are going to change the queue configuration with "
         "the following parameters:\n", stdout );
  fprintf( stdout, "     1. Segment data size will be %d bytes\n",
           iSegmentDataSize );
  fprintf( stdout, "     2. Total number of segments in a queue file "
           "will be %d\n\n", iSegmentsTotal );

  fputs( "WARNING!!! All data in the queue will be lost.\n", stdout );
  fputs( "Do you really want to continue? [Y/N] ", stdout );
  fflush( stdout );

  fgets( szCmd, sizeof( szCmd ), stdin );

  if( szCmd[ 0 ] == 'Y' || szCmd[ 0 ] == 'y' ) {
    fputs( "Changing the queue configuration. Please, wait...\n", stdout );
    fflush( stdout );
    if( ( iErr = q_setconfig( iSegmentDataSize, iSegmentsTotal ) ) >= 0 )
      fputs( "Done.\n", stdout );
    else {
      pszErr = q_errstr( iErr );
      fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
      free( pszErr );
    }
    fflush( stdout );
  }
}



@* The |q_errstr()| function. Returns error string associated with err code.
@<Exported Functions@>+=
char *q_errstr( int iErr ) {
  char *pszErr;

  switch( iErr ) {
    case ERRQ_GENERAL:
      asprintf( &pszErr, "General error" );
      break;
    case ERRQ_INIT:
      asprintf( &pszErr, "Queue file initialization error" );
      break;
    case ERRQ_READER:
      asprintf( &pszErr, "Invalid queue reader number" );
      break;
    case ERRQ_MSGTYPE:
      asprintf( &pszErr, "Invalid message type number" );
      break;
    case ERRQ_TAIL:
      asprintf( &pszErr, "Invalid tail index" );
      break;
    case ERRQ_MSGTOOLONG:
      asprintf( &pszErr, "Message is too long" );
      break;
    case ERRQ_HOST:
      asprintf( &pszErr, "Invalid host name" );
      break;
    case ERRQ_FILE:
      asprintf( &pszErr, "Error in operations with an export/import text file");
      break;
    case ERRQ_ALLOC:
      asprintf( &pszErr, "Memory allocation error" );
      break;
    case ERRQ_SEGDATASIZE:
      asprintf( &pszErr, "Invalid segment data size" );
      break;
    case ERRQ_SEGTOTAL:
      asprintf( &pszErr, "Invalid total number of segments" );
      break;
    case ERRQ_READERNAME:
      asprintf( &pszErr, "Reader name not found" );
      break;
    case ERRQ_MSGTYPENAME:
      asprintf( &pszErr, "Name of the message type not found" );
      break;
    default:
      if( iErr >= 0 ) asprintf( &pszErr, "No errors" );
      else asprintf( &pszErr, "Unknown error" );
  }
  return( pszErr );
}

@ Prototype.
@<Exported Prototypes@>+=
char *q_errstr( int iErr );


@*Index.
