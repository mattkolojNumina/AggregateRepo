%                       
%   launch.web
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
\def\title{Launch}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Launch acts as a MySQL based init for RDS.

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
\centerline{Control Revision: $ $Revision: 1.5 $ $}
\centerline{Control Date: $ $Date: 2016/03/30 22:01:03 $ $}
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
static char rcsid[] = "$Id: launch.w,v 1.5 2016/03/30 22:01:03 rds Exp $" ;
@<Definitions@>@;
@<Globals@>@;
@<Functions@>@;
main(int argc, char *argv[])
   {
   int ctr ;
   @<Initialize@>@;
   if (fork()) exit(0) ;

   @<Startup Processes@>@;

   for(ctr=0;;sleep(DELAY)) {
      @<Restarts?@>@;
      @<Check trigger@>@;
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
#include <mysql/mysql.h>

#include <rds_trn.h>
#include <rds_sql.h>

@ Globals
@<Globals@>=
char hostname[80] ;
char db[80] ;
@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   char *test ;
   trn_register("launch") ;

   if (argc >= 2)
      strcpy(db,argv[1]) ;
   else
      strcpy(db,"db") ;
   gethostname(hostname,60) ;
   Trace("init, database = %s, hostname = %s",db,hostname) ;
   sql_setconnection(db,"rds","rds","rds") ;

   for (;;sleep(1)) {
     test = sql_getvalue("SELECT NOW()") ;
     if (test!=NULL) break ;
     Inform("wait for db") ;
   } 
   sql_query("UPDATE launch set pid=-1,operation='idle',throttled='no',count=0 "
             "WHERE host='%s' AND ordinal>0",
             hostname);
   signal(SIGCHLD,onChildDeath) ;
   }


@ Globals for restarting. A short queue is necessary on busy systems.
Depth is large to insure that we won't have be overwhelmed by signals.

Finally we make another call to |signal| so we can catch the next one.
@d P_DEPTH 100
@<Globals@>=

int restartCount = 0 ;
pid_t rPID[P_DEPTH] ;
@ This 
@<Functions@>=
void onChildDeath(int unused)
{
  int status ;
  int restartPID ;
  Inform("signal SIGCHLD caught") ;
  do {
    restartPID = waitpid(-1,&status,WNOHANG) ;
    if (restartCount < P_DEPTH-1 && restartPID > 0) {
      Trace("pid=%d died",restartPID) ;
      rPID[restartCount++] = restartPID ;
      }
  } while (restartPID > 0) ;
  signal(SIGCHLD,onChildDeath) ;
}

@* Watch. Because MySQL queries do not nest, we have a local structure to 
hold updates of PID information as we start processes initially.

This version of d\ae mon works sort of like "init". It uses a database 
instead of a flat file but essentially serves to run all RDS processes
and acts as the parent process. It is intended to replace the rc scripting
currently (formerly?) used. 

All processes with a mode not |"manual"| are started. |"startonce"| 
processes are not restarted if they terminate.

@<Startup Processes@>=
{
  int err ;
  char *homeDir ;
  char *process ;
  char *traceName ;
  char **args ;
  ProcWatch *head = NULL ;
  int i ;
  pid_t pid ;
  ProcWatch *next ;
  int delayAfter ;
  char *mode ;
  err = sql_query("SELECT home,process,args,mode,ordinal,"
              "delayAfter,traceName "
              "FROM launch WHERE host='%s' AND ordinal>0 "
              "AND mode<> 'manual' ORDER by ordinal asc",hostname) ;
  if (!err && sql_rowcount() > 0) {
    for (i=0 ;i < sql_rowcount() ;i++) 
        @<start processes@>@;
  } 
  @<update launch@>@;
  while(head) {
   next = head->link ;
   free(head) ; 
   head = next ;
  }
}

@
@<Definitions@>=
  typedef struct _ProcWatch {
    struct _ProcWatch *link ;
    int pid ;
    int ordinal ;
    int startOnce ;
    int interval ;
    int lastStart ;
   } ProcWatch ;

@
@<Globals@>=


@ We ran a query for processes on startup. Here is the start code
@<start processes@>=
{
   chdir(sql_get(i,0)) ;
   mode = strdup(sql_get(i,3)) ;
   delayAfter = atoi(sql_get(i,5)) ;
   traceName = strdup(sql_get(i,6)) ;
   @<build args list@>@;
   pid = fork() ;
   if (!pid) {
     if (strlen(traceName) > 0) @<attach tracer@>@;
     execvp(args[0],args) ;
     // exec only returns on failure
     Alert("failed to launch [%s]",args[0]) ;
     exit(EXIT_FAILURE) ;
   }
   if (sql_get(i,2) == NULL || strlen(sql_get(i,2)) == 0)
     Trace("pid: %d started [%s]",pid,args[0]) ;
   else
     Trace("pid: %d started [%s %s]",pid,args[0],sql_get(i,2)) ;

   if (delayAfter>0) {
     Trace("sleep for %d",delayAfter) ;
     sleep(delayAfter) ;
   }

   ProcWatch *next = (ProcWatch*) malloc(sizeof(ProcWatch)) ;
   bzero(next,sizeof(ProcWatch)) ;
   next->link = head ;
   head = next ;
   next->pid = pid ;
   next->ordinal = atoi(sql_get(i,4)) ;
   next->startOnce =  (!strcmp("startonce",mode))  ;
   next->lastStart = time(NULL) ;
   @<free args list@>@;
   free(mode) ;
   free(traceName) ;
}


@ Some processes need tracer to be attached because they cannot
link to the tron/tracing library.
@<attach tracer@>=
{
  FILE *fp ;
  char openCmd[24] ;
  sprintf(openCmd,"tracer %s",traceName) ;
  Trace("attach tracer [%s]",traceName) ;
  fp = popen(openCmd,"w") ;
  dup2(fileno(fp),1) ;
  dup2(fileno(fp),2) ;
}

@ After we have finished starting processes, we've noted data 
for updating the launch table with pid information.
@<update launch@>=
{
  for(next = head ; next != NULL; next = next->link) {
    sql_query("UPDATE launch set pid=%d,count=count+1,lastStart=NOW() "
                "WHERE host='%s' AND "
                "ordinal = %d",next->pid,hostname,next->ordinal) ;
  }

}
@ the |"args"| field is white space separated arguments. We expect less
than 100 of them. The arguments are a |malloc()|ed vector of pointer
arrays initialzed to 0 (|NULL|) and into which |strdup()| strings are 
placed.
@d MAX_ARGS 100
@<build args list@>=
{
   int ctr = 0 ;
   char tmp[100] ;
   char *ptr ;
   char *dst ;
   args = (char **) malloc(MAX_ARGS*sizeof(char *)) ;
   bzero(args,sizeof(char **) * MAX_ARGS) ;
   args[ctr++] = strdup(sql_get(i,1)) ; 
   ptr = sql_get(i,2) ; 
   if (ptr != NULL && strlen(ptr) > 0) {
     for (tmp[0] = 0,dst=tmp ; *ptr != 0 ; ptr++) {
       if (!isspace(*ptr)) {
         *dst++ = *ptr ; 
         *dst = 0 ;
         continue ;
       }
       if (tmp[0] != 0) {
         args[ctr++] = strdup(tmp) ;
         dst = tmp ; 
         *dst = 0 ;
       }
     }
     if (tmp[0] != 0) {
         args[ctr++] = strdup(tmp) ;
     }
   }
}
@ The arguments are released as follows.
@<free args list@>=
{
  int i ;
  for (i=0 ;args[i] != NULL ; i++) {
     free(args[i]) ;
  }
  free(args) ;
}
@ The main gadfly loop has a sleep.
That sleep we are in is broken when it expires or when we receive a
signal. If we got a signal, we need to check if the expiring child
pid implies a restart. Here we check the for that.
@<Restarts?@>=
{
  int restartPID ;
  int ordinal,pid,err ;
  char **args ;
  char *traceName ;
  int t_diff ;
  int i = 0 ;
  // test for db connection good.
  char *test ;
  test = sql_getvalue("SELECT NOW()") ;
  if (test==NULL) continue ;
  while (restartCount>0) {
    restartPID = rPID[--restartCount] ;
    Inform("test for restart pid: %d",restartPID) ;
    err = sql_query("SELECT home,process,args,mode,ordinal,"
              "UNIX_TIMESTAMP()-UNIX_TIMESTAMP(lastStart),stamp,traceName "
              "FROM launch "
              "WHERE host='%s' AND ordinal>0 "
              "AND pid = %d AND (mode='daemon' "
              "OR mode='startonce')",hostname,restartPID) ;
    if (!err && sql_rowcount() == 1) { 
      chdir(sql_get(i,0)) ;
      ordinal = atoi(sql_get(i,4)) ;
      t_diff = atoi(sql_get(i,5)) ;
      if (!strcmp("startonce",sql_get(i,3))) {
        sql_query("UPDATE launch set pid=-1,count=count-1 WHERE "
             "host = '%s' AND ordinal=%d",hostname,ordinal) ;
        continue ;
      }
      traceName = strdup(sql_get(i,7)) ;
      @<build args list@>@; 
      pid = fork() ;
      if (!pid) {  // child process
        MYSQL *mysql ;
        int err ;
        char query[120] ;
        // new mysql connection
        mysql = mysql_init(NULL) ;
        mysql_real_connect(mysql,db,"rds","rds","rds",0,NULL,0) ;
        if (t_diff < 10) {  // restarting too soon
          sprintf(query,"UPDATE launch set throttled='yes' WHERE "
               "host = '%s' AND ordinal=%d",hostname,ordinal) ;
          mysql_query(mysql,query) ;
          Alert("delay! restarting too frequently") ;
          sleep(60) ;
          Inform("delay complete, launch %s", args[0]) ;
        }

        sprintf(query,"UPDATE launch set throttled='no' WHERE "
             "host = '%s' AND ordinal=%d",hostname,ordinal) ;
        err = mysql_query(mysql,query) ;
        mysql_close(mysql) ;

        if (strlen(traceName) > 0) @<attach tracer@>@;
        execvp(args[0],args) ; 
        // exec only returns on failure
        Alert("failed to launch [%s]",args[0]) ;
        exit(EXIT_FAILURE) ;
      }
      // parent process
      if (sql_get(i,2) == NULL || strlen(sql_get(i,2)) == 0)
        Trace("pid: %d re-started [%s]",pid,args[0]) ;
      else
        Trace("pid: %d re-started [%s %s]",pid,args[0],sql_get(i,2)) ;
      sql_query("UPDATE launch set pid=%d,count=count+1,lastStart=NOW() "
                "WHERE host='%s' AND "
                "ordinal = %d",pid,hostname,ordinal) ;
      @<free args list@>@;
      free(traceName) ;
     }
     restartPID = -1 ;
     continue ;
  }
}

@* Triggers. Behavior depends control type. If |"daemon"| it is 
restarted and kept running.

If |"startonce"| it is run. 

If |"manual"|, if it was daemon (had a stored pid) it is killed.
@<Check trigger@>=
{
   int err,pid,ordinal ;
   int oldPid ;
   int termDelay ;
   char **args ;
   char mode[48] ;
   char *traceName ;
   int i = 0 ;
//   sql_query("UPDATE launch set stamp=NOW() WHERE host='%s' and ordinal=0",
//          hostname) ;
   {
      err = sql_query("SELECT home,process,args,mode,ordinal,pid,"
                "traceName,termDelay FROM launch "
                "WHERE host='%s'  AND ordinal > 0 "
                "AND operation='trigger' ORDER by ordinal asc LIMIT 1"
                ,hostname) ;
      if (!err && sql_rowcount() == 1) {
        oldPid = atoi(sql_get(i,5)) ;
        ordinal = atoi(sql_get(i,4)) ;
        strcpy(mode,sql_get(i,3)) ;
        traceName = strdup(sql_get(i,6)) ;
        termDelay = atoi(sql_get(i,7)) ;
        chdir(sql_get(i,0)) ;
        @<build args list@>@;
        pid = fork() ;
        if (!pid) {
          usleep(50 * 1000) ;
          if (!strcmp("manual",mode)) {
            sql_query("UPDATE launch set pid=-1,operation='idle' "
                  "WHERE host='%s' AND "
                  "ordinal = %d",hostname,ordinal) ;
           exit(0) ;
          }
          // kill old process 
          if (oldPid>0) {
            if (termDelay > 0) {
               kill(oldPid,SIGTERM) ;
               sleep(termDelay) ;
            }
            kill(oldPid,SIGKILL) ;
          }
          if (strlen(traceName)) @<attach tracer@>@;
          execvp(args[0],args) ;
          // exec only returns on failure
          Alert("failed to launch [%s]",args[0]) ;
          exit(EXIT_FAILURE) ;
        }
        Trace("pid: %d start on trigger  [%s]",pid,args[0]) ;
        if (!strcmp("startonce",mode)) {
          sql_query("UPDATE launch set operation='idle',"
                  "count=count+1,lastStart=NOW() "
                  "WHERE host='%s' AND "
                  "ordinal = %d",hostname,ordinal) ;
        } else {
          if (strcmp("manual",mode)) 
            sql_query("UPDATE launch set pid=%d,operation='idle', "
                  "count=count+1,lastStart=NOW() "
                  "WHERE host='%s' AND "
                  "ordinal = %d",pid,hostname,ordinal) ;
        }
        @<free args list@>@;
        free(traceName) ;
      }
   }
}



@* Index.
