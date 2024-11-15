% 
%   rds_ann.web -- text annunciation
%
%   Author: Mark Woodworth 
%
%   History:
%      12/27/99 -- check in (mrw)
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
%     (C) Copyright 1999 Numina Systems Corporation.  All Rights Reserved.
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
\def\myitem{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{RDS3 - ANN -- Text Message Annunciation}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The ANN library is a collection of calls that allows the 
broadcast of text messages.

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
\centerline{RCS ID: $ $Id: rds_ann.w,v 1.2 2012/10/04 16:38:12 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* ANN: text message annunciation. 

The ANN text annunciation library allows for the broadcast of text messages.

@ Exported Functions.  This library exports the following functions:

{\bf ann\_put()} inserts one text message.

{\bf ann\_get()} returns one text message.
On the first call to {\tt ann\_get()}, pass a pointer to 
a variable containing zero.
The function call will return a string and will update the variable.
On subsequent calls, pass the variable to get the next record.  If there are
no new messages, a Null will be returned and the variable will be unchanged.

@* Overview.
@c
@<Includes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
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
#include "rds_ann.h" 

@ Header.  We put the prototypes in an external header.
@(rds_ann.h@>=
#ifndef __RDS_ANN_H
#define __RDS_ANN_H
   @<Prototypes@> @;
#endif


@* Data Structures.
Text messages are limited to a fixed maximum size.
@<Defines@>+=
#define ANN_LEN (80)

@ The number of messages kept in a circular file is also limited to a 
fixed number.
@<Defines@>+=
#define ANN_MAX (32)

@ Each entry has text, a sequence number, and a stamp.
@<Structures@>+=
struct entry_struct
   {
   char txt[ANN_LEN+1] ;
   int sequence ;
   int stamp ;
   } ;

@ A structure holds the current head pointer and the array of 
messages.
@<Structures@>+=
struct ann_struct 
   {
   int head ;
   int sequence ; 
   struct entry_struct entry[ANN_MAX] ;
   } ;

@* IPC. 
The ann database is contained in a shared 
memory segment and is controlled by a semaphore.
These are SYS-V style IPC constructs and are accessed by
key values.
@<Defines@>+=
#define ANN_BASE (RDS_BASE + 0x50)
#define ANN_SEM  (ANN_BASE + 0)
#define ANN_SHM  (ANN_BASE + 1)

@ The value of |RDS_BASE| comes from the general RDS header file.
@<Includes@>+=
#include <rds.h>

@ The semaphore id is stored in a local integer.
@<Globals@>+=
static int ann_sem = -1 ;

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

@ Ann Lock.  The |ann_lock()|
function checks to see if the |ann_sem| has been created,
and if not, creates it.  Then the system waits til it can have sole access.
@<Functions@>+=
static int ann_lock(void)
   {
   union semun lock_union ;
   struct sembuf lock_buf[1] ;
   int err ;

   if(ann_sem<0)
      ann_sem = semget(ANN_SEM,1,0777) ;
   
   if(ann_sem<0)
      {
      ann_sem = semget(ANN_SEM,1,0777|IPC_CREAT) ;
      if(ann_sem<0)
         return -1 ;

      if(ann_sem==0)
         {
         err = semctl(ann_sem,0,IPC_RMID,NULL) ;
         ann_sem = semget(ANN_SEM,1,0777|IPC_CREAT) ;
         if(ann_sem<0)
            return -2 ;
         if(ann_sem==0)
            {
            return -3 ;
            }
         }

      lock_union.val = 1 ;
      semctl(ann_sem,0,SETVAL,lock_union) ;
      }

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = -1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(ann_sem,lock_buf,1) ;
   }

@ Ann Unlock.  The |ann_unlock()| function checks to see if the |tag_sem| has been created,
and if so, drops it by one.
@<Functions@>+=
static int ann_unlock(void)
   {
   struct sembuf lock_buf[1] ;

   if(ann_sem<=0)
      return -1 ;

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = 1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(ann_sem,lock_buf,1) ;
   }

@ These are prototyped.
@<Prototypes@>+=
static int ann_lock(void) ;
static int ann_unlock(void) ;

@ Shared Memory.

@ This is anchored by a global.
@<Globals@>+=
static struct ann_struct *ann = NULL ;

@ The function |ann_check()| makes sure that the 
local shared memory segment is attached, creating and initializing it
if required.

This function should be called before accessing any of the string store or
nodes to ensure that they are created and populated.  It is called
at the start of all exported functions.
@<Functions@>+=
static int ann_check(void) 
   {
   int id ;
   int made = 0 ;

   if(ann==NULL)
      {
      ann_lock() ;

      id = shmget(ANN_SHM,sizeof(struct ann_struct),0777) ;
      if(id<0)
         {
         made = 1 ;
         id = shmget(ANN_SHM,sizeof(struct ann_struct),IPC_CREAT|0777) ;
         if(id<0)
            {
            ann_unlock() ;
            return -1 ;
            }
         }
    
       ann = (struct ann_struct *)shmat(id,0,0) ;
       if(ann==NULL)
          {
          ann_unlock() ;
          return -2 ;
         }

      if(made)
         {
         int i ;

         for(i=0 ; i<ANN_MAX ; i++)
            {
            strcpy(ann->entry[i].txt,"") ;
            ann->entry[i].sequence = 0 ;
            ann->entry[i].stamp = 0 ;
            }
         ann->head = 0 ;
         ann->sequence = 0 ;
         }

      ann_unlock() ;
      }

   return 0 ;
   }

@ This is also prototyped.
@<Prototypes@>+=
static int ann_check(void) ;

@* Put.  This function inserts one message into the queue.

The message string must be null terminated, and should be less than
|ANN_LEN| in length.

@<Exported Functions@>+=
int ann_put(char *msg) 
   {
   int err ;
   int h ;

   if(err=ann_check())
      return err ;

   ann_lock() ;

   h = ann->head ;
   strncpy(ann->entry[h].txt,msg,ANN_LEN) ;
   ann->entry[h].txt[ANN_LEN] = '\0' ;
   ann->entry[h].sequence = ++(ann->sequence) ;
   h++ ;
   if(h>=ANN_MAX)
      h = 0 ;
   ann->head = h ;

   ann_unlock() ;

   return 0 ;
   }
  
@ Prototype.
@<Prototypes@>+=
int ann_put(char *msg)  ;

@ Test Stub.  A test stub program |ann_put| is also created.
@(ann_put.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_ann.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s text\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = ann_put(argv[1]) ;
   printf("ann_put(%s) = %d\n",
          argv[1],ret) ;
   }

@* Get.  This function retrieves one message from the queue.
@<Exported Functions@>+=
char *ann_get(int *seq) 
   {
   int err ;
   char *msg ;
   int i, minseq ;
 
   if(err=ann_check())
      return NULL ;

   msg = (char *)malloc(ANN_LEN+1) ;
   if(msg==NULL)
      return NULL ;

   minseq = 0 ;

   ann_lock() ;
   for(i=0 ; i<ANN_MAX ; i++)
      {
      if(ann->entry[i].sequence > *seq)
         {
         if((minseq == 0) || (ann->entry[i].sequence < minseq))
            {
            strcpy(msg,ann->entry[i].txt) ;
            minseq = ann->entry[i].sequence ;
            }
         }
      }
   ann_unlock() ;

   if(minseq == 0)
      return NULL ;
   
   *seq = minseq ;

   return msg ;
   }
  
@ Prototype.
@<Prototypes@>+=
char *ann_get(int *seq)  ;

@ Test Stub.  A test stub program |ann_get| is also created.
@(ann_get.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_ann.h> 
main(int argc, char *argv[]) 
   {
   int ret ;
   int seq ;
   char *msg ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s index\n",argv[0]) ;
      exit(1) ;
      }
  
   seq = atoi(argv[1]) ; 
   msg = ann_get(&seq) ;
   if(msg != NULL)
      {
      printf("ann_get(%d->%d) = [%s]\n",
             atoi(argv[1]),seq,msg) ;
      free(msg) ;
      }
   else
      printf("ann_get(%d) up to date\n",atoi(argv[1])) ;

   }

@*Index.
