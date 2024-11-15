%                       
%   rannd.web
%
%   Author: Mark Woodworth 
%
%   History:
%      12/29/99 - init (mrw)
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
%     (C) Copyright 1999 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Remote Annunciation Server}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a TCP server that allows remote access to the annunciation messages.

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
\centerline{Author: Mark Woodworth}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2008/03/25 15:46:36 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
This program allows remote access over sockets to the annunciation
system.

The program listens on port 20006 and forks off a server for each connection.

All current annunciations are immediately spooled to the connecting socket.
Then, all new messages are spooled.


@* Structure. 

@c
@<Includes@>@;
@<Globals@>@;
@<Child Handler@>@;
@<Pipe Handler@>@;
main(int argc, char *argv[])
   {
   @<Build Socket@>@;
   @<Enable Handlers@>@;

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
               usleep(10000) ;
               }
            exit(0) ;
            }
         close(ns) ;
         }
      }
   }

@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fcntl.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <string.h>
#include <signal.h>

#include <rds_ann.h>

@* Send Messages. 
We keep a local sequence variable initialized to zero.
@<Globals@>+=
int seq = 0 ;

@ We hold a pointer to the messages we receive.
@<Globals@>+=
char *txt ;

@ In addition, we assemble the messages to send in a work buffer.
@<Globals@>+=
char buf[128+1] ;

@ We call |ann_get()| until we get a null in response.
@<Send Messages@>=
while((txt=ann_get(&seq)) != NULL)
   {
   int err ;

   sprintf(buf,"%s\r\n",txt) ;
   err = send(ns,buf,strlen(buf),0) ;
   free(txt) ;
   if(err<0)
      exit(1) ;
   }
 
@* Socket.
@<Globals@>=
int n,s,ns,len ;
struct sockaddr_in name ;

@ Build Socket.
@<Build Socket@>=
   {
   int i ;

   if((s = socket(AF_INET,SOCK_STREAM,0)) < 0)
      exit(1) ;

   memset(&name, 0, sizeof(struct sockaddr_in)) ;
   name.sin_family = AF_INET ;
   name.sin_port = htons(20006) ;
   len = sizeof(struct sockaddr_in) ;

   n = INADDR_ANY ;
   memcpy(&name.sin_addr, &n, sizeof(long)) ;

   for(i=0 ; i<60 ; i++)
      {
      if(bind(s,(struct sockaddr *)&name,len)>=0)
         break ;
      sleep(2) ;
      }
   if(i==60)
      exit(2) ;

   if(listen(s,5)<0)
      exit(3) ;
   }

@* Handlers.  

@ Child Handler. This function handles the child process change signals to
wait on their exit status.
@<Child Handler@>=
void child_handler(int sig)
   {
   pid_t pid ;
   int status ;

   pid = waitpid(-1,&status,WNOHANG) ;

   signal(SIGCHLD,child_handler) ;
   }

@ Pipe Handler.  This function handles the 
pipe error signal.
@<Pipe Handler@>=
void pipe_handler(int sig)
   {
   pid_t pid ;
   int status ;

   exit(1) ;
   }

@ These are started in the parent.
@<Enable Handlers@>=
   signal(SIGCHLD,child_handler) ;
   signal(SIGPIPE,pipe_handler) ;
@* Index.
