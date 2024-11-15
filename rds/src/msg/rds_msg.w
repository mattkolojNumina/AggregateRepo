% 
%   msg.web
%
%   Author: Mark Woodworth 
%
%   History:
%      10/24/98 -- check in (mrw)
%       7/10/00 -- review (mrw)
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
%     (C) Copyright 1998,2000 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{RDS3 Msg -- message queues}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The Msg library is a collection of calls that allows the queuing of variable
length text messages.

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
\centerline{Authors: Mark Woodworth}
\centerline{RCS ID: $Id: rds_msg.w,v 1.3 2014/07/11 21:27:35 rds Exp $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 1998 Numina Systems Corporation.  
All Rights Reserved.}
}


@* MSG: text message queues. 
The MSG message library 
provides routines to pass variable length text messages between applications.

Messages and status information are stored in a non-volatile disk based
file.

This library is a shared C library 

@ Exported Functions.  This library exports the following functions:
{\narrower\narrower
\dot {\bf msg\_send()} queues up one text message with an associated queue
id.

\dot {\bf msg\_type()} returns the type of the next message to be read  for
one of the readers.
A return of type 0 indicates that the queue is empty (i.e. up to date).

\dot {\bf msg\_recv()} returns the text of the next message for one reader.
The caller must free the retrieved buffer.

\dot {\bf msg\_next()} advances one of the readers to the next message.

\dot {\bf msg\_get()} returns the current index for the head or one of the readers.

\dot {\bf msg\_put()} sets the value of the current index for the head or one of
the readers.

\dot {\bf msg\_export()} dumps the contents of the queue into a text readable format.

\dot {\bf msg\_import()} recovers the queue from a previously dumped file.

\par
}

@* Overview.
@c
@<Includes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
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
#include "rds_msg.h" 

@ Header.  We put the exported prototypes in an external header.
@(rds_msg.h@>=
#ifndef __RDS_MSG_H
#define __RDS_MSG_H
   @<Exported Prototypes@> @;
#endif

@ We also get the |RDS_BASE| and |RDS_HOME| from a global header file.
@<Includes@>+=
#include "rds.h"

@* Data Structures.
The file containing the data structures has three sections:
the index information,
the queue information,  and 
the data segments.

@ Index information.
The MSG library maintains a number of named indices into the data area.
Each of these indices points to the `next record to use'.  The first record
is reserved for the unique head record.

Each index entry holds a name and an index value.
@<Structures@>+=
struct msg_index
   {
   int active ;
   char name[NAME_LEN+1] ;
   long int value ;
   } ;

@ The name is fixed in length.
@<Defines@>+=
#define NAME_LEN (32) 

@ There are a fixed number of index records.
@<Defines@>+=
#define INDEX_MAX (32) 

@ The size of each record is handy.
@<Defines@>+=
#define INDEX_SIZE (sizeof(struct msg_index))

@ The space taken up by all of these is a handy constant.
@<Defines@>+=
#define INDEX_EXTENT (INDEX_MAX*INDEX_SIZE)

@ These are the first things in the file.
@<Defines@>+=
#define INDEX_START (0L) 

@ Queue information.
The library maintains a list of message types or queues.
@<Structures@>+=
struct msg_queue {
   int active ;
   char name[NAME_LEN+1] ;
   } ;

@ There are a fixed number of queue records.
@<Defines@>+=
#define QUEUE_MAX (32)

@ It is handy to name the size.
@<Defines@>+=
#define QUEUE_SIZE (sizeof (struct msg_queue))

@ It is useful to name the extent.
@<Defines@>+=
#define QUEUE_EXTENT (QUEUE_MAX*QUEUE_SIZE)

@ The queue information follows the index information.
@<Defines@>+=
#define QUEUE_START (INDEX_START+INDEX_EXTENT) 

@ Segment records.
The content of messages is stored in segment records.  Each segment knows
the message type, the total segments in the message, and the segment count
of this segment.
@<Structures@>+=
struct msg_segment
   {
   unsigned char total ;
   unsigned char count ;
   unsigned char mtype ;
   char data[DATA_LEN+1] ;
   } ;

@ The data length could be tuned for storage efficiency, but cannot change
while data is stored in the file.
@<Defines@>+=
#define DATA_LEN (32) 

@ The size of a segment is also a useful constant.
@<Defines@>+=
#define SEGMENT_SIZE (sizeof (struct msg_segment))

@ The start of the segments in the file is set to the end of the previous
pieces.
@<Defines@>+=
#define SEGMENT_START (QUEUE_START+QUEUE_EXTENT)

@ The number of segment records is also fixed.
@<Defines@>+=
#define SEGMENT_MAX (1000000L)

@ File. The data is all held in one file.
@<Defines@>+=
#define FILENAME (RDS_HOME "/data/msg.dat")

@ A local file handle is kept. This is initialized to an illegal handle value.
@<Globals@>+=
static int msg_file = -1 ;

@* IPC. 
Acces to the message database is controlled by a file.
These are SYS-V style IPC constructs and are accessed by
key values.
@<Defines@>+=
#define MSG_BASE (RDS_BASE + 0x30)
#define MSG_SEM  (MSG_BASE + 0)

@ The semaphore id is stored in a local integer.
@<Globals@>+=
static int msg_sem = -1 ;

@ We must define |semun| in some instances.
@<Structures@>+=
#if defined(__GNU_LIBRARY__) && !defined(_SEM_SEMUN_UNDEFINED)
#else
union semun {
   int val ;
   struct demid_ds *buf ;
   unsigned short int *array ;
   struct seminfo *__buf ;
   } ;
#endif

@ Msg Lock.  The |msg_lock()|
function checks to see if the |msg_sem| has been created,
and if not, creates it.  Then the system waits til it can have sole access.
@<Functions@>+=
static int msg_lock(void)
   {
   union semun lock_union ;
   struct sembuf lock_buf[1] ;
   int err ;

   if(msg_sem<0)
      msg_sem = semget(MSG_SEM,1,0777) ;
   
   if(msg_sem<0)
      {
      msg_sem = semget(MSG_SEM,1,0777|IPC_CREAT) ;
      if(msg_sem<0)
         return -1 ;

      if(msg_sem==0)
         {
         err = semctl(msg_sem,0,IPC_RMID,NULL) ;
         msg_sem = semget(MSG_SEM,1,0777|IPC_CREAT) ;
         if(msg_sem<0)
            return -2 ;
         if(msg_sem==0)
            {
            return -3 ;
            }
         }

      lock_union.val = 1 ;
      semctl(msg_sem,0,SETVAL,lock_union) ;
      }

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = -1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(msg_sem,lock_buf,1) ;
   }

@ Msg Unlock.  The |msg_unlock()| function checks to see if the |msg_sem| has been created,
and if so, drops it by one.
@<Functions@>+=
static int msg_unlock(void)
   {
   struct sembuf lock_buf[1] ;

   if(msg_sem<=0)
      return -1 ;

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = 1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(msg_sem,lock_buf,1) ;
   }

@ These are prototyped.
@<Prototypes@>+=
static int msg_lock(void) ;
static int msg_unlock(void) ;

@* Check.  A call to this function first checks to see if the file is already
open.  If not, it attempts to open the file.  If this fails because the
file does not exist, the file is created and initialized.
@<Functions@>+=
static int msg_check(void)
   {
   extern int errno ;
   int i ;
   struct msg_index index ;
   struct msg_queue queue ;
   struct msg_segment segment ;

   @<Init: check if open@>@;
   @<Init: open if it exists@>@;
   @<Init: create@>@;

   @<Init: indices@>@;   
   @<Init: queues@>@;
   @<Init: segmenst@>@;

   return msg_file ;
   }

@ If the file is already open the we are done and leave.
@<Init: check if open@>=
   if (msg_file >= 0)
      return msg_file ;

@ If not open, then open if it exists and leave.
@<Init: open if it exists@>=
   msg_file = open(FILENAME,O_RDWR,0666) ;
   if (msg_file >= 0)
      return msg_file ;

@ Create the file, and leave if we can't.
@<Init: create@>=
   umask(0) ;
   msg_file = open(FILENAME,O_RDWR|O_CREAT,0666) ;
   if (msg_file < 0)
      {
      return msg_file ;
      }

@ Create the index records.
@<Init: indices@>=  
   memset(&index,0,INDEX_SIZE) ;
   index.active = 1 ;
   strcpy(index.name,"head") ;
   index.value = 0L ;

   lseek(msg_file,INDEX_START,SEEK_SET) ;
   write(msg_file,&index,INDEX_SIZE) ;

   memset(&index,0,INDEX_SIZE) ;
   index.active = 0 ;
   strcpy(index.name,"") ;
   index.value = 0 ;

   for(i= 1 ; i < INDEX_MAX ; i++)
      {
      lseek(msg_file,INDEX_START+i*INDEX_SIZE,SEEK_SET) ;
      write(msg_file,&index,INDEX_SIZE) ;
      }

@ Fill in the queue records.
@<Init: queues@>=
   memset(&queue,0,QUEUE_SIZE) ;
   queue.active = 1 ;
   strcpy(queue.name,"head") ;

   lseek(msg_file,QUEUE_START,SEEK_SET) ;
   write(msg_file,&queue,QUEUE_SIZE) ;

   memset(&queue,0,QUEUE_SIZE) ;
   queue.active = 0 ;
   strcpy(queue.name,"") ;

   for(i= 1 ; i < QUEUE_MAX ; i++)
      {
      lseek(msg_file,QUEUE_START+i*QUEUE_SIZE,SEEK_SET) ;
      write(msg_file,&queue,QUEUE_SIZE) ;
      }

@ Fill in the segment records.
@<Init: segmenst@>=
   memset(&segment,0,SEGMENT_SIZE) ;
   segment.mtype = 0 ;
   segment.total = 1 ;
   segment.count = 0 ;
   strcpy(segment.data,"") ;

   for(i=0 ; i < SEGMENT_MAX ; i++)
      {
      lseek(msg_file,SEGMENT_START+i*SEGMENT_SIZE,SEEK_SET) ;
      write(msg_file,&segment,SEGMENT_SIZE) ;
      }

@* Send.  This function inserts one message into the queue.

The message string must be null terminated, and must be less than
256 * |DATA_LEN| in length.

@<Exported Functions@>+=
int msg_send(int mtype, char *msg) 
   {
   int m ;
   struct msg_index index ;
   struct msg_segment segment ;
   long int head ;
   int length, total,count ;

   @<Send: open file@>@;
   @<Send: calculate the segment count@>@; 
   msg_lock() ;
   @<Send: move the head@>@;
   @<Send: move the data@>@; 
   msg_unlock() ;

   return 0 ;
   }
  
@ Open the file if not already opened. 
@<Send: open file@>=
   m = msg_check() ;
   if (m < 0)
      return -1 ;

@ Set |total| to be the number of required segments.
@<Send: calculate the segment count@>=
   length = strlen(msg) ;
   total = length / DATA_LEN ;
   if(length % DATA_LEN)
      total++ ;
   if(total > 256)
      return -2 ;

@ Move the head.
@<Send: move the head@>=
   lseek(m,0,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;

   head = index.value ;
   index.value += total ;
   if(index.value >= SEGMENT_MAX) 
      index.value -= SEGMENT_MAX ;

   lseek(m,0,SEEK_SET) ;
   write(m,&index,INDEX_SIZE) ;

@ Move the data.
@<Send: move the data@>=
   for(count=0 ; count < total ; count++)
      {
      segment.mtype = mtype ;
      segment.total = total ;
      segment.count = count ;
      strncpy(segment.data,msg+(count*DATA_LEN),DATA_LEN) ;
      segment.data[DATA_LEN] = '\0' ;
      lseek(m,SEGMENT_START+head*SEGMENT_SIZE,SEEK_SET) ;
      write(m,&segment,SEGMENT_SIZE) ;
      head++ ;
      if(head >= SEGMENT_MAX)
         head -= SEGMENT_MAX ;
      }

@ Prototype.
@<Exported Prototypes@>+=
int msg_send(int mtype, char *msg)  ;

@ Test Stub.  A test stub program |msg_send| is also created.
@(msg_send.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h>
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=3)
      {
      fprintf(stderr,"usage: %s mtype text\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = msg_send(atoi(argv[1]),argv[2]) ;
   printf("msg_send(%d,%s) = %d\n",
          atoi(argv[1]),argv[2],ret) ;
   }

@* Type. This call requests the message type (or queue) of the next record.
@<Exported Functions@>+=
int msg_type(int handle) 
   {
   int m ;
   int i, head, tail,total ;
   struct msg_index index ;
   struct msg_segment segment ;

   @<Type: open the file@>@;
   msg_lock() ;
   @<Type: get the head and tail@>@;
   msg_unlock() ;

   @<Type: align to a message boundary@>@;
   @<Type: check for empty@>@;

   return segment.mtype ;
   }

@ Open the file. 
@<Type: open the file@>=
   m = msg_check() ;
   if(m<0)
      return -1 ;
   if((handle<1) || (handle >= INDEX_MAX))
      return -2 ;

@ Get the head and tail.
@<Type: get the head and tail@>=
   lseek(m,INDEX_START,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   head = index.value ;

   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   tail = index.value ;


@ Align to a message boundary, where |count=0|.
@<Type: align to a message boundary@>=
   while(head != tail)
      {
      lseek(m,SEGMENT_START+tail*SEGMENT_SIZE,SEEK_SET) ;
      read(m,&segment,SEGMENT_SIZE) ;
      if(segment.count==0)
         break ;
      tail++ ;
      if(tail >= SEGMENT_MAX)
         tail -= SEGMENT_MAX ;
      }

@ Check for queue empty.
@<Type: check for empty@>=
   if(head==tail)
      return 0 ;


@ Prototype.
@<Exported Prototypes@>+=
int msg_type(int handle) ;


@ Test Stub.  A test stub program |msg_type| is also created.
@(msg_type.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s handle\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = msg_type(atoi(argv[1])) ;

   printf("msg_type(%d) = %d\n",
          atoi(argv[1]),ret) ;
   }

@* Recv.  This call requests the next record.
It returns a pointer to the data.
The caller should free the allocated pointer when done.

@<Exported Functions@>+=
char *msg_recv(int handle) 
   {
   int m ;
   int i, head, tail,total ;
   struct msg_index index ;
   struct msg_segment segment ;
   char *text ;

   @<Recv: attach to the file@>@;

   msg_lock() ;
   @<Recv: get the head and tail@>@;
   msg_unlock() ;

   @<Recv: align to a message boundary@>@;
   @<Recv: check for queue empty@>@;
   @<Recv: allocate a text buffer@>@;
   @<Recv: copy the data@>@;

   return text ;
   }

@ Open the file. 
@<Recv: attach to the file@>=
   m = msg_check() ;
   if(m<0)
      return NULL ;
   if((handle<1) || (handle >= INDEX_MAX))
      return NULL ;

@ Get the head and tail.
@<Recv: get the head and tail@>=
   lseek(m,INDEX_START,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   head = index.value ;

   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   tail = index.value ;

@ Align to a message boundary where |count=0|.
@<Recv: align to a message boundary@>=
   while(head != tail)
      {
      lseek(m,SEGMENT_START+tail*SEGMENT_SIZE,SEEK_SET) ;
      read(m,&segment,SEGMENT_SIZE) ;
      if(segment.count==0)
         break ;
      tail++ ;
      if(tail >= SEGMENT_MAX)
         tail -= SEGMENT_MAX ;
      }

@ Check for queue empty and bail out if so.
@<Recv: check for queue empty@>=
   if(head==tail)
      {
      return NULL ;
      }

@ Allocate a text buffer.
@<Recv: allocate a text buffer@>=
   total  = segment.total ;
   text = (char *)malloc(total*DATA_LEN+1) ;
   if(text==NULL)
      {
      return NULL ;
      }
   text[total*DATA_LEN] = '\0' ;

@ Copy the data.
@<Recv: copy the data@>=
   for(i=0 ; i<total ; i++)
      {
      lseek(m,SEGMENT_START+tail*SEGMENT_SIZE,SEEK_SET) ;
      read(m,&segment,SEGMENT_SIZE) ;
      strncpy(text+i*DATA_LEN,segment.data,DATA_LEN) ;
      tail++ ;
      if(tail >= SEGMENT_MAX)
         tail -= SEGMENT_MAX ;
      }

@ Prototype.
@<Exported Prototypes@>+=
char *msg_recv(int handle) ;


@ Test Stub.  A test stub program |msg_recv| is also created.
@(msg_recv.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   char *msg = NULL ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s handle\n",argv[0]) ;
      exit(1) ;
      }
  
   msg = msg_recv(atoi(argv[1])) ;

   if(msg!=NULL)
      {
      printf("msg_recv(%d) = %s\n",
             atoi(argv[1]),msg) ;
     free(msg) ; 
     }
   else
      printf("msg_recv(%d) = NULL\n",
             atoi(argv[1])) ;
   }


@* Next.  This function advances one of the indices.
@<Exported Functions@>+=
int msg_next(int handle) 
   {
   int m ;
   int i, head, tail,total ;
   struct msg_index index ;
   struct msg_segment segment ;
   char *text ;

   @<Next: open the file@>@; 

   msg_lock() ;
   @<Next: get the head and tail@>@;
   msg_unlock() ;

   @<Next: align to a message boundary@>@; 
   @<Next: check for queue empty@>@;
   @<Next: walk through the record@>@;
   @<Next: update the tail@>@; 
 
   return 0 ;
   }

@ Open the file. 
@<Next: open the file@>=
   m = msg_check() ; 
   if(m<0)
      return -1 ;
   if((handle<1) || (handle >= INDEX_MAX))
      return -2 ;

@ Get the head and tail.
@<Next: get the head and tail@>=
   lseek(m,INDEX_START,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   head = index.value ;

   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   tail = index.value ;

@ Align to a message boundary.
@<Next: align to a message boundary@> =
   while(head != tail)
      {
      lseek(m,SEGMENT_START+tail*SEGMENT_SIZE,SEEK_SET) ;
      read(m,&segment,SEGMENT_SIZE) ;
      if(segment.count==0)
         break ;
      tail++ ;
      if(tail >= SEGMENT_MAX)
         tail -= SEGMENT_MAX ;
      }

@ Check for queue empty and leave if so.
@<Next: check for queue empty@>=
   if(head==tail)
      return 0 ;

@ Walk through the record, stopping if we hit the head.
@<Next: walk through the record@>=
   total  = segment.total ;
   for(i=0 ; i<total ; i++)
      {
      if(head==tail)
         return 0 ;
      tail++ ;
      if(tail >= SEGMENT_MAX)
         tail -= SEGMENT_MAX ;
      }
   
@ Update the tail. 
@<Next: update the tail@>=
   index.value = tail ;
   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   write(m,&index,INDEX_SIZE) ;

@ Prototype.
@<Exported Prototypes@>+=
int msg_next(int handle) ;

@ Test Stub.  A test stub program |msg_next| is also created.
@(msg_next.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s handle\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = msg_next(atoi(argv[1])) ;

   printf("msg_next(%d) = %d\n",
          atoi(argv[1]),ret) ;
   }

@* Get.  This function returns the value of one of the indices.
@<Exported Functions@>+=
int msg_get(int handle) 
   {
   int m ;
   struct msg_index index ;
   int tail ;

   @<Get: open the file@>@;

   msg_lock() ;
   @<Get: get the tail@>@;
   msg_unlock() ;

   return tail ;
   }

@ Open the file. 
@<Get: open the file@>=
   m = msg_check() ;
   if(m<0)
      return -1 ;
   if((handle<0) || (handle >= INDEX_MAX))
      return -2 ;

@ Get the tail.
@<Get: get the tail@>=
   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;
   tail = index.value ;

@ Prototype.
@<Exported Prototypes@>+=
int msg_get(int handle) ;

@ Test Stub.  A test stub program |msg_next| is also created.
@(msg_get.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s handle\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = msg_get(atoi(argv[1])) ;

   printf("msg_get(%d) = %d\n",
          atoi(argv[1]),ret) ;
   }

@* Set.  This function returns the value of one of the indices.
@<Exported Functions@>+=
int msg_set(int handle, long int value) 
   {
   int m ;
   struct msg_index index ;
   int tail ;

   @<Set: open the file@>@;
   msg_lock() ;
   @<Set: read the record@>@; 
   index.value = value ;
   @<Set: write the record@>@;
   msg_unlock() ;

   return value ;
   }

@ Open the file 
@<Set: open the file@>=
   m = msg_check() ;
   if(m<0)
      return -1 ;
   if((handle<0) || (handle >= INDEX_MAX))
      return -2 ;

@ Read the record in.
@<Set: read the record@>=
   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   read(m,&index,INDEX_SIZE) ;

@ Write the record out.
@<Set: write the record@>=
   lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
   write(m,&index,INDEX_SIZE) ;

@ Prototype.
@<Exported Prototypes@>+=
int msg_set(int handle, long int value) ;

@ Test Stub.  A test stub program |msg_next| is also created.
@(msg_set.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=3)
      {
      fprintf(stderr,"usage: %s handle value\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = msg_set(atoi(argv[1]),atoi(argv[2])) ;

   printf("msg_set(%d,%d) = %d\n",
          atoi(argv[1]),atoi(argv[2]),ret) ;
   }

@* Index.  This function looks up an index value by name.
If not found, a new one is allocated.
@<Exported Functions@>+=
int msg_index(char *name) 
   {
   int m,i ;
   struct msg_index index ;
   int handle ;

   @<Index: open the file@>@;
   @<Index: find the handle@>@;
   if(handle<0)
      {
      @<Index: allocate a new handle@>@;
      }

   return handle ;
   }

@ Open the file 
@<Index: open the file@>=
   m = msg_check() ;
   if(m<0)
      return -1 ;

@ Look for the handle.
@<Index: find the handle@>=
   handle = -1 ;

   for(i=0 ; i<INDEX_MAX ; i++)
      {
      lseek(m,INDEX_START+i*INDEX_SIZE,SEEK_SET) ;
      read(m,&index,INDEX_SIZE) ;
      if(index.active)
         {
         if(0==strcmp(name,index.name))
            {
            handle = i ;
            break ;
            }
         }
      }   

@ Allocate a new handle. 
@<Index: allocate a new handle@>=
   {
   msg_lock() ;
   for(i=0 ; i<INDEX_MAX ; i++)
      {
      lseek(m,INDEX_START+i*INDEX_SIZE,SEEK_SET) ;
      read(m,&index,INDEX_SIZE) ;
      if(!index.active)
         {
         handle = i ;
         index.active = 1 ;
         strncpy(index.name,name,NAME_LEN) ;
         index.name[NAME_LEN] = '\0' ;
         index.value = 0 ;
         lseek(m,INDEX_START+handle*INDEX_SIZE,SEEK_SET) ;
         write(m,&index,INDEX_SIZE) ;
         break ;
         }
      }
   msg_unlock() ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int msg_index(char *name) ;

@ Test Stub.  A test stub program |msg_index| is also created.
@(msg_index.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_msg.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s name\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = msg_index(argv[1]) ;

   printf("msg_index(%s) = %d\n",
          argv[1],ret) ;
   }


@*Index.
