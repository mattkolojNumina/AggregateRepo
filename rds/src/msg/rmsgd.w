%                       
%   rmsgd.web
%
%   Author: Mark Woodworth 
%
%   History:
%      10/23/98 - init (mrw)
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
%     (C) Copyright 1998 Numina Systems Corporation.  All Rights Reserved.
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
\def\quote#1{\smallskip\centerline{#1}\smallskip}

% --- title block ---
\def\title{Remote Message Server}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a TCP server that allows remote access to the text queue.

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
\centerline{Control Revision: $ $Revision:$ $}
\centerline{Control Date: $ $Date: 1998/07/23 17:37:38 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 1998 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
This program allows remote access over sockets to the MSG text queue
system.

The program listens on port 20002 and forks off a server for each connection.

The following commands are understood and processed:

\dot{\bf Send} This command inserts a string into a queue.

{\narrower
The format for sending is:
\quote{{\tt S} {\it qqq} {\it ttt$\cdots$t}}
where {\it qqq} is the message type (or queue number), and
{\it ttt$\cdots$t} is the text.  The message ends with a carriage return.
\par
The response is:
\quote{{\tt S} {\it eee}}
where
{\it eee} is the returned error code. 
\par
}


@* Structure. 

@c
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Handler@>@;
main(int argc, char *argv[])
   {
   @<Build Socket@>@;
   @<Enable Handler@>@;

   while(1)
      {
      ns = accept(s,(struct sockaddr *)&name,&len) ;
      if(ns>=0)
         {
         if(!fork())        
            {
            @<Read Crank@>@;
            close(ns) ;
            exit(0) ;
            }
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

#include <msg.h>

@* Read Crank.  Read commands, return messages.
@<Read Crank@>=
while(recv(ns,&c,1,0)>0)
   {
   if(c==10)
      continue ;
   if(c==EOM)
      {
      text[t++] = '\0' ;
      @<Process@>@;
      t = 0 ;
      }
   else
      if(t<TEXT_LEN)
         text[t++] = c ;
   }

@ Messages are collected into a text buffer.
@<Globals@>+=
char text[TEXT_LEN+1] ;

@ The size of the text buffer is limited to the maximum text message.
@<Defines@>+=
#define TEXT_LEN (32*256) 

@ An input pointer traverses the array.
@<Globals@>+=
int t ;

@ A variable holds individual input characters.
@<Globals@>+=
char c ;

@ A carriage return is the end of message signal.
@<Defines@>+=
#define EOM (13) 

@* Process.  Each message is interpreted by its first character.
@<Process@>=
   {
   int err, n, index, queue ;
   char msg[80+1] ;

   switch(text[0])
      {
      @<Send Process@>@;
      @<Type Process@>@;
      @<Recv Process@>@;
      @<Next Process@>@;
      default:
         send(ns,"?\r",2,0) ;
         break ;
      }
   }

@ Send.
@<Send Process@>=
case 'S':
case 's':
   {
   char tmp[32+1] ;
   int t ;
   int i ;
   char c ;

   t = 0 ;
   tmp[t] = '\0' ;
 
   for(i=2 ; i<32 ; i++)
      {
      c = text[i] ;
      if(!isdigit(c))
         break ;
      tmp[t++] = c ;
      tmp[t  ] = '\0' ;
      }
   queue = atoi(tmp) ;

   if(queue>0)
      {
      err = msg_send(queue,text+i+1) ;
      sprintf(msg,"S %d\r",err) ;
      send(ns,msg,strlen(msg),0) ;
      }
   else
      send(ns,"? S\r",4,0) ;
   break ;
   }

@ Type.
@<Type Process@>=
case 'T':
case 't':
   {
   n = sscanf(text+2,"%d",&index) ;
   if(n==1)
      {
      sprintf(msg,"T %3d\r",msg_type(index)) ;
      send(ns,msg,strlen(msg),0) ;
      }
   else
      send(ns,"?\r",2,0) ;
   break ;
   }

@ Recv.
@<Recv Process@>=
case 'R':
case 'r':
   {
   char txt[8096+1] ;

   n = sscanf(text+2,"%d",&index) ;
   if(n==1)
      {
      char *r ;
      r = msg_recv(index) ;
      if(r)
         {
         sprintf(txt,"R %s\r",r) ;
         send(ns,txt,strlen(txt),0) ;
         free(r) ;
         }
      else
         {
         sprintf(txt,"R \r") ;
         send(ns,txt,strlen(txt),0) ;
         }
      }
   else
      send(ns,"?\r",2,0) ;
   break ;
   }

@ Next.
@<Next Process@>=
case 'N':
case 'n':
   {
   n = sscanf(text+2,"%d",&index) ;
   if(n==1)
      {
      sprintf(msg,"N %3d\r",msg_next(index)) ;
      send(ns,msg,strlen(msg),0) ;
      }
   else
      send(ns,"? N\r",4,0) ;
   break ;
   }

@* Socket.
@<Globals@>=
int n,s,ns,len ;
struct sockaddr_in name ;
char text[] ;

@ Build Socket.
@<Build Socket@>=
   {
   int i ;

   if((s = socket(AF_INET,SOCK_STREAM,0)) < 0)
      exit(1) ;

   memset(&name, 0, sizeof(struct sockaddr_in)) ;
   name.sin_family = AF_INET ;
   name.sin_port = htons(20002) ;
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
@* Handler.  This function handles the child process change signals to
wait on their exit status.
@<Handler@>=
void handler(int sig)
   {
   pid_t pid ;
   int status ;

   pid = waitpid(-1,&status,WNOHANG) ;

   signal(SIGCHLD,handler) ;
   }

@ This is started in the parent.
@<Enable Handler@>=
   signal(SIGCHLD,handler) ;

@* Index.
