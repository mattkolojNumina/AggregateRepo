%                       
%   execute.web
%
%   Author: Mark Olson 
%
%   History:
%      080404 - init (mdo)
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
\def\title{Execute}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Executes commands queued in the execute table to be run by shell script,
mysql or posted into the message queue.

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
\centerline{Control Revision: $ $Revision: 1.4 $ $}
\centerline{Control Date: $ $Date: 2012/10/04 20:26:30 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2008 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program reads an control table record and
executes commands.
@d DELAY 1
@c
static char rcsid[] = "$Id: execute.w,v 1.4 2012/10/04 20:26:30 rds Exp $" ;
@<Definitions@>@;
@<Globals@>@;
@<Functions@>@;
main(int argc, char *argsv[])
   {
   int ctr ;
   @<Initialize@>@;


   for(ctr=0;;sleep(DELAY)) {
      @<Check commands@>@;
      }
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

struct timeval start_time,end_time ;
double dt ;


@
@<Functions@>=
void onChildDeath(int unused) 
{
  int pid ;
  int status ;
  gettimeofday( &end_time, NULL );
  dt = (end_time.tv_sec - start_time.tv_sec) +
         (end_time.tv_usec - start_time.tv_usec) * 0.000001;



  do {
    pid = waitpid(-1,&status,WNOHANG) ;
    if (pid >0) Inform( "child (pid=%d) exited elapsed %.3f seconds", pid, dt );
  } while (pid > 0) ;


  signal(SIGCHLD,onChildDeath) ;
}
@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   trn_register("execute") ;
   Trace(rcsid) ;

   gethostname(hostname,60) ;
   Trace("database = db, hostname = %s",hostname) ;
   sql_setconnection("db","rds","rds","rds") ;

   sql_query("UPDATE execute set completed='true' "
             "WHERE host='%s'", hostname);
   signal(SIGCHLD,onChildDeath) ;
   }


@* Process.
@<Check commands@>=
{
  char *val ;
  int count ;
  val = sql_getvalue("select count(*) from execute WHERE "
                "host='%s' AND completed='false'",hostname) ;
  while (val != NULL && atoi(val) > 0) {
    @<Run next@>@;
    val = sql_getvalue("select count(*) from execute WHERE "
                "host='%s' AND completed='false'",hostname) ;
  }
  if (val == NULL) exit(0) ;
}
@
@<Run next@>=
{
  int err ;
  char **args ;
  err = sql_query("SELECT seq,command "
            "FROM execute WHERE host='%s' "
            "AND completed='false' ORDER BY seq LIMIT 1",hostname) ;
  if (!err && sql_rowcount() == 1) {
    int seq ;
    int pid ;
    char *command = NULL ; 
    seq = atoi(sql_get(0,0)) ;
    command = sql_get(0,1) ;
    
    gettimeofday( &start_time, NULL );

    if (command != NULL) {

      @<Build args list@>@;
      pid = fork() ;
      if (!pid) { /* am child */
        char outCmd[24] ;
        char errCmd[24] ;
        FILE *fp ;
        strcpy(outCmd,"tracer exe.out") ;
        strcpy(errCmd,"tracer exe.err") ;
        fp = popen(outCmd,"w") ;
        dup2(fileno(fp),1) ; 
        fp = popen(errCmd,"w") ;
        dup2(fileno(fp),2) ; 
        execvp(args[0],args) ;
        Alert("command [%s] not found",args[0]) ;
      }
      Trace("executing [%s] pid: %d",command,pid) ;
      @<free args list@>@; 
    }
    sql_query("UPDATE execute set completed='true' WHERE seq=%d",seq) ;
  }
}
@
@d MAX_ARGS 100
@<Build args list@>=
{
   int ctr ;
   char tmp[100] ;
   char *ptr ;
   char *dst ;
   args = (char **) malloc(MAX_ARGS*sizeof(char **)) ;
   bzero(args,sizeof(char **) * MAX_ARGS) ;
   ptr = command ;
   for (ctr=0,tmp[0] = 0,dst = tmp ; *ptr != 0 ; ptr++) {
     if (!isspace(*ptr)) {
       *dst++ = *ptr ;
       continue ;
     }
     *dst = 0 ;
     if (tmp[0] != 0) {
       args[ctr++] = strdup(tmp) ;
       dst = tmp ;
       *dst = 0 ;
     }
   } 
   if (tmp[0] != 0) {
       *dst = 0 ;
       args[ctr++] = strdup(tmp) ;
   }
}

@
@<free args list@>=
{
  int i ;
  for (i=0 ;args[i] != NULL ; i++) {
     free(args[i]) ;
  }
  free(args) ;
}
@* Index.
