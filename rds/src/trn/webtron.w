%
%   webtron.web
%
%   Author: Adam Marshall
%
%   History:
%      2006-05-24 -- init, using code from rannd --AHM
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
%     (C) Copyright 2006 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Web-Based Tracelog Socket Server}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a TCP server that allows a web applet to connect and receive tracelog
messages.

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
\centerline{Author: Adam Marshall}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.9 $ $}
\centerline{Control Date: $ $Date: 2019/02/15 20:42:50 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2006 Numina Systems Corporation.
All Rights Reserved.}
}

@* Overview.
This program allows remote access over sockets to the tracelog message
system.

The program listens on port 20006 and forks off a server for each connection.

All current messages are immediately spooled to the connecting socket.
Then, all new messages are spooled.


@* Structure.

@c
@<Includes@> @;
@<Handlers@>@;
@<Globals@> @;
int
main(int argc, char *argv[])
   {
   @<Initialize@> @;

   while(1)
      {
      ns = accept(s,(struct sockaddr *)&name,&len) ;
      if(ns>=0)
         {
         if(!fork())
            {
            while(1)
               {
               @<Send Messages@>
               usleep(100000) ;
               }
            exit(0) ;
            }
         close(ns) ;
         }
      }
   return 0 ;
   }

@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <fcntl.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <string.h>
#include <signal.h>

#include <rds_net.h>
#include <rds_trn.h>

@ Initialize.
@<Initialize@>=
   {
   trn_register( "webtron" ) ;

   do {
     s = net_serve(20006) ;
     if (s <0) {
       Alert("failed to bind") ;
       sleep(30) ;
       }
    } while (s<0) ;
    signal(SIGCHLD,child_handler) ;
   }
@ A signal handler to remove zombies.
@<Handlers@>=
void child_handler(int sig)
   {
   pid_t pid ;
   int status ;

   do 
      {
      pid = waitpid(-1,&status,WNOHANG) ;
      } while (pid>0) ;

   signal(SIGCHLD,child_handler) ;
   }



@* Send Messages.
We call |trn_fetch()| to retrieve all unread trace messages then spool them
to the socket.  If a large number are retrieved, only spool the last 100.
@<Send Messages@>=
   {
   trn_msg *msg, *head ;
   int i, cnt, err ;
   struct tm *tm ;
   char buf[ 9 + TRN_NAME_LEN + 3 + TRN_MSG_LEN + 1 + 100 ] ;

   msg = head = trn_fetch( &cnt ) ;
   for (i = 0; i < cnt - 100 ; i++)
      msg++ ;
   for ( ; i < cnt ; i++)
      {
      tm = localtime( &(msg->stamp) ) ;
      sprintf( buf, "%02d:%02d:%02d\t%s\t%d\t%s\n",
            tm->tm_hour, tm->tm_min, tm->tm_sec,
            msg->name, (int) msg->level, msg->msg ) ;
      err = send( ns, buf, strlen(buf), 0 ) ;
      if (err < 0)
         {
         Trace( "send error or disconnect -- exit child process" ) ;
         exit(1) ;
         }
      msg++ ;
      }

   if (head)
      free( head ) ;
   }

@* Socket.
@<Globals@>=
int n,s,ns,len ;
struct sockaddr name ;




@* Index.

