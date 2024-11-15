%                       
%   stopper.web
%
%   Author: Mark Olson 
%
%   History:
%      080403 - init (mdo)
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
%    (C) Copyright 2005 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- helpful macros ---
\def\dot{\item{ $\bullet$}}
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}

% --- title block ---
\def\title{stopper}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Stops programs run by launcher (usefult after launcher is teminated).

This version 

% --- confidentiality statement ---
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

% --- author and version ---
\bigskip
\centerline{Author: Mark Olson}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2012/10/02 19:18:28 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2005 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program reads an control table record and
executes commands.
@d DELAY 6
@c
static char rcsid[] = "$Id: stop.w,v 1.1 2012/10/02 19:18:28 rds Exp $" ;
@<Definitions@>@;
@<Globals@>@;
main(int argc, char *argv[])
   {
   @<Initialize@>@;

   @<Kill Processes@>@;
   }

@ Includes.
@<Definitions@>=
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/signal.h>
#include <sys/wait.h>

#include <rds_trn.h>
#include <rds_sql.h>

@ Globals
@<Globals@>=
char hostname[80] ;
@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   char db[80] ;

   trn_register("stop") ;

   if (argc >= 2)
      strcpy(db,argv[1]) ;
   else
      strcpy(db,"db") ;
   gethostname(hostname,60) ;
   Trace("init, database = %s, hostname = %s",db,hostname) ;
   sql_setconnection(db,"rds","rds","rds") ;

   }



@*  Kill processes marked in launch table by pid.

@<Kill Processes@>=
{
  int err ;
  int i; 
  int pid ;
  int delay ;
  Trace("kill launch") ;
  system("killall -9 -q launch") ;
  err = sql_query("SELECT pid,termDelay,nickName FROM launch WHERE host='%s'"
            " AND ordinal>0 order by ordinal desc",
             hostname) ;
  if (!err && sql_rowcount() > 0) {
    for (i=0 ; i < sql_rowcount() ; i++) {
      pid = atoi(sql_get(i,0)) ;
      if (pid < 0) continue ;
      delay = atoi(sql_get(i,1)) ;
      Trace("term %d:%d [%s]",pid,delay,sql_get(i,2)) ;
      kill(pid,SIGTERM) ; 
      if (delay>0) sleep(delay) ;
      else usleep(10 * 1000) ;
      Trace("kill %d:%d [%s]",pid,delay,sql_get(i,2)) ;
      kill(pid,SIGKILL) ;
    }
  } 
  Trace("UPDATE launch SET pid=-1 WHERE host='%s' AND ordinal>0",
             hostname) ;
  sql_query("UPDATE launch SET pid=-1 WHERE host='%s' AND ordinal>0",
             hostname) ;
}
@* Index.

