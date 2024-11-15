% 
%   rds_ctr.web -- RDS counter library 
%
%   Author: Mark Woodworth 
%
%   History:
%      10/18/02 -- check in (mrw)
%      8/6/08   -- added ctr_incr() (rme)
%      07/30/10 -- ctr_bump now wraps ctr_incr(), removed ctr_zero()  (ahm)
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
%     (C) Copyright 2002--2010 Numina Group, Inc.  All Rights Reserved.
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
\def\title{CTR - counter handling}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The CTR library handles counters in a uniform manner.

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
\centerline{RCS ID: $ $Id: rds_ctr.w,v 1.4 2012/10/04 16:43:03 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2002 Numina Systems Corporation.  
All Rights Reserved.}
}

@* CTR: counter handling.

This library handles incrementing production counters.
A process {\it ctrd} reads messages and inserts records in
tables and tracing.  This library encapsulates the message
calls.

@ Exported Functions.  This library exports the following functions:

{\bf ctr\_bump()} increments a counter (wraps ctr_incr, provided for
backward compatibility).

{\bf ctr\_incr()} increments a counter. 


@* Overview.
@c
@<Includes@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>

#include <rds_msg.h>
#include "rds_ctr.h" 

@ Header.  We put the prototypes in an external header.
@(rds_ctr.h@>=
#ifndef __RDS_CTR_H
#define __RDS_CTR_H
   @<Defines@>@;
   @<Prototypes@> @;
#endif


@ The msg class is a constant.
@<Defines@>+=
#define CTR_MSG (21)
#define CTR_MSG_LEN  128

@* Bump.  This function is called by applications to increment a counter.

@<Exported Functions@>+=
int ctr_bump(const char *zone, const char *code) 
   {
   return ctr_incr( "%s/%s", zone, code ) ;
   }
  
@ Prototype.
@<Prototypes@>+=
int ctr_bump(const char *zone, const char *code) ;

@ Test Stub.  A test stub program |ctr_bump| is also created.
@(ctr_bump.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_ctr.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=3)
      {
      fprintf(stderr,"usage: %s zone code\n",argv[0]) ;
      exit(EXIT_FAILURE) ;
      }
   
   ret = ctr_bump(argv[1],argv[2]) ;
   printf("ctr_bump(%s,%s) = %d\n",
          argv[1],argv[2],ret) ;
   }

@* Increment.  This function is called by applications to increment a counter.

@<Exported Functions@>+=
int ctr_incr( const char *fmt, ... ) 
   {
   va_list ap;
   char *code;
   char msg[CTR_MSG_LEN+1] ;
   int err ;

   va_start( ap, fmt );
   vasprintf( &code, fmt, ap );
   va_end( ap );

   snprintf(msg,CTR_MSG_LEN,"I %s",code) ;
   msg[CTR_MSG_LEN] = '\0' ;
   err = msg_send(CTR_MSG,msg) ;

   free(code) ;
   return err ;
   }
  
@ Prototype.
@<Prototypes@>+=
int ctr_incr( const char *fmt, ... );

@ Test Stub.  A test stub program |ctr_incr| is also created.
@(ctr_incr.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_ctr.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s code\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = ctr_incr(argv[1]) ;
   printf("ctr_incr(%s) = %d\n",
          argv[1],ret) ;
   }


@*Index.
