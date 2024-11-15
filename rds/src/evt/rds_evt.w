% 
%   rds_evt.web -- event handler 
%
%   Author: Mark Woodworth 
%
%   History:
%      10/18/02 -- check in (mrw)
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
%     (C) Copyright 2002--2010 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{EVT - event handling}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The EVT library handles events in a uniform manner.

This is an intermediate RDS library that depends on SQL and MSG.

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
\centerline{RCS ID: $ $Id: rds_evt.w,v 1.3 2012/10/04 20:24:18 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2002 Numina Systems Corporation.  
All Rights Reserved.}
}

@* EVT: event message handling.

This library handles the assertion and clearing of events in a sytematic
fashion.  A process {\it evtd} reads messages and inserts records in
tables, annunciations, and tracing.  This library encapsulates the message
calls.

@ Exported Functions.  This library exports the following functions:

{\bf evt\_instant()} handles an instantaneous event. 

{\bf evt\_start()}  handles the beginning of an event with finite duration.

{\bf evt\_stop()} handles the end of an event with duration.


@* Overview.
@c
@<Includes@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <rds_msg.h>
#include "rds_evt.h" 

@ Header.  We put the prototypes in an external header.
@(rds_evt.h@>=
#ifndef __RDS_EVT_H
#define __RDS_EVT_H
   @<Defines@>@;
   @<Prototypes@> @;
#endif


@ The msg class is a constant.
@<Defines@>+=
#define EVT_MSG       20
#define EVT_MSG_LEN  128

@* Instant.  This function 
is called by applications to handle an instantaneous event.

@<Exported Functions@>+=
int evt_instant(const char *code) 
   {
   char msg[EVT_MSG_LEN+1] ;
   int err ;
   time_t now ;

   now = time(NULL) ;
   snprintf(msg,EVT_MSG_LEN,"A %ld %s",now,code) ;
   msg[EVT_MSG_LEN] = '\0' ;
   err = msg_send(EVT_MSG,msg) ;

   return err ;
   }
  
@ Prototype.
@<Prototypes@>+=
int evt_instant(const char *code)  ;

@ Test Stub.  A test stub program |evt_instant| is also created.
@(evt_instant.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_evt.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s code\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = evt_instant(argv[1]) ;
   printf("evt_instant(%s) = %d\n",
          argv[1],ret) ;
   }


@* Start.  This function 
is called by applications to handle an instantaneous event.

@<Exported Functions@>+=
int evt_start(const char *code) 
   {
   char msg[EVT_MSG_LEN+1] ;
   int err ;
   time_t now ;

   now = time(NULL) ;
   snprintf(msg,EVT_MSG_LEN,"B %ld %s",now,code) ;
   msg[EVT_MSG_LEN] = '\0' ;
   err = msg_send(EVT_MSG,msg) ;

   return err ;
   }
  
@ Prototype.
@<Prototypes@>+=
int evt_start(const char *code)  ;

@ Test Stub.  A test stub program |evt_strart| is also created.
@(evt_start.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_evt.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s code\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = evt_start(argv[1]) ;
   printf("evt_start(%s) = %d\n",
          argv[1],ret) ;
   }

@* Stop.  This function 
is called by applications to handle an instantaneous event.

@<Exported Functions@>+=
int evt_stop(const char *code) 
   {
   char msg[EVT_MSG_LEN+1] ;
   int err ;
   time_t now ;

   now = time(NULL) ;
   snprintf(msg,EVT_MSG_LEN,"C %ld %s",now,code) ;
   msg[EVT_MSG_LEN] = '\0' ;
   err = msg_send(EVT_MSG,msg) ;

   return err ;
   }
  
@ Prototype.
@<Prototypes@>+=
int evt_stop(const char *code)  ;

@ Test Stub.  A test stub program |evt_stop| is also created.
@(evt_stop.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_evt.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s code\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = evt_stop(argv[1]) ;
   printf("evt_stop(%s) = %d\n",
          argv[1],ret) ;
   }


@*Index.
