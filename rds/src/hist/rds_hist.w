% 
%   rds_hist.web -- carton history handler 
%
%   Author: Richard Ernst
%
%   History:
%      02/24/04 -- check in (rme)
%      Jan. 5, 2009 (rme) added msec timing
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
%     (C) Copyright 2004--2010 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{HIST}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The HIST library handles carton history in a uniform manner.

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
\centerline{Authors: Richard Ernst}
\centerline{RCS ID: $ $Id: rds_hist.w,v 1.4 2010/10/05 22:57:17 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2002 Numina Systems Corporation.  
All Rights Reserved.}
}

@* CTR: counter handling.

This library handles carton history logging. 
A process {\it histd} reads messages and inserts records in
tables and tracing.  This library encapsulates the message
calls.

@ Exported Functions.  This library exports the following functions:

{\bf hist\_post()} posts a history message. 



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
#include <sys/time.h>

#include <rds_msg.h>

#include "rds_hist.h" 

@ Header.  We put the prototypes in an external header.
@(rds_hist.h@>=
   @<Defines@>@;
   @<Prototypes@> @;


@ The msg class is a constant.
@<Defines@>+=
#define HIST_MSG       23
#define HIST_MSG_LEN  128


@* Post.  This function 
is called by applications to post a carton history message.
@<Exported Functions@>+=
int hist_post(const char *carton, const char *code, const char *description)
   {
   char msg[HIST_MSG_LEN+1] ;
   int err ;
   struct timeval tv ;

   gettimeofday(&tv, NULL) ;
   snprintf(msg,HIST_MSG_LEN,"%s\t%s\t%ld\t%ld\t%s",
         carton,code,tv.tv_sec,tv.tv_usec,description) ;
   msg[HIST_MSG_LEN] = '\0' ;
   err = msg_send(HIST_MSG,msg) ;

   return err ;
   }
  
@ Prototype.
@<Prototypes@>+=
int hist_post(const char *carton, const char *code, const char *description) ;



@ Test Stub.  A test stub program |hist_post| is also created.
@(hist_post.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_hist.h> 
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=4)
      {
      fprintf(stderr,"usage: %s carton code description\n",argv[0]) ;
      exit(1) ;
      }
   
   ret = hist_post(argv[1],argv[2],argv[3]) ;
   printf("hist_post(%s,%s,%s) = %d\n",
          argv[1],argv[2],argv[3],ret) ;
   }


@*Index.
