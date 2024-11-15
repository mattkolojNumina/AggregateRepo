% 
%   rds_logix.w
%
%   Author: Richard Ernst
%
%   History: 
%      9/23/00 -- Started (rme)
%      8/28/04 -- fixed a bug (a conflict with a standard C function index())
%                 (ank)
%     10/11/05 -- made read/write return error codes on select() timeout --AHM
%     04/05/06 -- create stub programs for read and write --AHM
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
%
%
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
%

%
% --- useful macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{logix}
\def\topofcontents{\null\smallskip
\centerline{\titlefont\title}
\smallskip
This document contains source code for
a library of socket-based routines for reading and writing to Allen-Bradley
ControlLogix 5000 PLCs.

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
\bigskip
\centerline{Authors: Mark Woodworth, Mark Olson, Richard Ernst}
\centerline{RCS Version: $ $Revision: 1.6 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1998 Numina Systems Corporation.  
All Rights Reserved.}
}



@* Library overview.
This library consists of five function calls:

\dot |logix_open()|, which establishes the connection with the PLC,

\dot |logix_close()|, which closes the plc connection,

\dot |logix_read()|, which is the asynchronous read request,

\dot |logix_write()|, which is the asynchronous write request,

\dot |logix_process()|, which processes the PLC response to a read or write
request.


@* Implementation. The linrary consists of the following:
@c
@<Includes@>@;
@<Exported Defines@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Utility Functions@>@;
@<Functions@>@;



@* PLC header file.
@(rds_logix.h@>=
#ifndef __LOGIX_H
#define __LOGIX_H
  @<Exported Defines@>@;
  @<Prototypes@>@;
#endif



@* |logix_open()|.
@<Functions@>+=
plc_handle logix_open(char* address)
  {
  plc_handle plc ;
  
  plc = (plc_handle)malloc(sizeof(plc_record)) ;
  strcpy(plc->address,address) ;
  plc->transaction = 0 ;
  plc->enabled = 1 ;

  @<Open client socket@>@;
  @<Request session ID@>@;
  @<Process session ID@>@;
  if (!plc->enabled) 
    {
    free(plc) ;
    plc = NULL ;
    }
  return plc ;
  }


@ |logix_open()| prototype.
@<Prototypes@>=
plc_handle logix_open(char* address) ;



@* |logix_close()|.
@<Functions@>+=
int logix_close(plc_handle plc)
   {
   @<Close socket@>@;
   free(plc) ;
   return 0 ;
   }


@ |logix_close()| prototype.
@<Prototypes@>+=
int logix_close(plc_handle plc) ;



@* |logix_read()|.
@<Functions@>+=
int logix_read(plc_handle plc,char* src,int start,
               int n_words, data_type the_type, unsigned long int* data)
   {
   int err, n ;

   err = 0 ;
   if (plc == NULL)
     {
     Alert("plc connection not established") ;
     return LOGIX_ERR ;
     }
   @<Request TCP Read@>@;
   if (err == 0)
     if (logix_processread(plc,&n,data) != n_words)
       err = LOGIX_ERR_READ ;
   return err ;
   }


@ |logix_read()| prototype.
@<Prototypes@>+=
int logix_read(plc_handle plc, char* src, int start,
               int n_words, data_type the_type, unsigned long* data) ;



@* |logix_write()|.
@<Functions@>+=
int logix_write(plc_handle plc, char* dest, int start, int n_words, 
                data_type the_type, unsigned long int *data)
   {
   int count ;
   int err ;
   if (plc == NULL)
     {
     Alert("plc connection not established") ;
     return LOGIX_ERR ;
     }

   err = 0 ;
   @<Send write request@>@;
   idx = 0 ;
   return logix_processwrite(plc) ;
   }


@ |logix_write()| prototype.
@<Prototypes@>+=
int logix_write(plc_handle plc, char* dest, int start, int n_words, 
                data_type the_type, unsigned long int *data) ;



@* |logix_processread()|.
@<Functions@>+=
int logix_processread(plc_handle plc, int *n_words, unsigned long int *dest)
   {
   unsigned long int data[200] ;
   int i, n_values ;
   int status ;

   n_values = process_read(plc, data) ;
   for (i = 0; i < n_values; i++)
     dest[i] = data[i] ;
   return n_values ;
   }


@ |logix_processread()| prototype.
@<Prototypes@>+=
int logix_processread(plc_handle plc, int *n_words, unsigned long int *dest) ;


@* |logix_processwrite()|.
@<Functions@>+=
int logix_processwrite(plc_handle plc)
   {
   return process_write(plc) ;
   }


@ |logix_processwrite()| prototype.
@<Prototypes@>+=
int logix_processwrite(plc_handle plc) ;



@ Includes.  The standard IO files are included.
@<Includes@>+=
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/time.h>

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <sys/ioctl.h>
#include <netinet/in.h>

#include <rds_trn.h>



@ Defines.
@<Defines@>=

#define INTERCHANGE_PORT  2222
#define ETHERNETIP_PORT   44818 
#define MSG_SIZE          2076

/* INTERCHANGE header flags */
#define REQUEST           1
#define REPLY             2

/* INTERCHANGE header commands */
#define NOP               0
#define NEW_SESSION       1
#define BACKLOG           3
#define UNSOL_DEF         4
#define UNSOL_UNDEF       5
#define DH_PLUS_CMD       6
#define PCCC_LOCAL_CMD    7
#define UNSOL_GETALL      8



@ Exported defines.
@<Exported Defines@>=
typedef struct 
   {
   int socket ;
   unsigned long int session_id ;
   int transaction ;
   char address[25] ;
   int enabled ;
   } plc_record ;
typedef plc_record *plc_handle ;
typedef enum {LOGIX_INT, LOGIX_DINT} data_type ;

#define LOGIX_ERR          (-1)
#define LOGIX_ERR_SOCK     (-2)
#define LOGIX_ERR_READ     (-3)
#define LOGIX_ERR_CONN     (-4)
#define LOGIX_ERR_TIMEOUT  (-5)



@ Globals.
@<Globals@>=
plc_handle first_plc = NULL ;
char response[ 2048 ];
int idx;  /* index */



@ Close socket.
@<Close socket@>=
   {
   shutdown(((plc_record *)plc)->socket,2) ;
   close(((plc_record *)plc)->socket) ;
   }



@ Open client socket.
@<Open client socket@>=
  {
  if ((plc->socket = s_open(plc->address,ETHERNETIP_PORT)) < 0)
    {
    Alert("Can't open socket connection to plc") ;
    return (plc_handle)(NULL) ;
    }
  }



@ Request session ID.
@<Request session ID@>=
  {
  char msg[MSG_SIZE] ;
  int count ;

  memset(msg,0,MSG_SIZE) ;
  msg[0] = 0x65 ; 
  msg[2] = 0x04 ;
  msg[13] = 0x04 ;
  msg[15] = 0x05 ;
  msg[24] = 0x01 ;
  count = write(plc->socket,msg,28) ;
  if (count != 28)
    {
    Alert("Error in socket write") ;
    @<Reconnect TCP@>@;
    }
  }



@ Request TCP Read.
@<Request TCP Read@>=
   {
   int i, j ;
   char msg[MSG_SIZE] ;
   int msg_size ;
   char tag[25] ;
   int count ;

   memset(msg,0,MSG_SIZE) ;
   msg[0] = 0x6f ;
   msg[2] = strlen(src) + (strlen(src)%2)+ 22 + 16 ;
   memcpy(&msg[4],&(plc->session_id),4) ;
   msg[12] = 0x02 ;
   msg[16] = 0xa0 ;
   msg[17] = 0xc6 ;
   msg[18] = 0xfa ;
   msg[19] = 0x01 ;
   msg[28] = 0x05 ;
   msg[30] = 0x02 ;
   msg[36] = 0xb2 ;
   msg[38] = strlen(src) + (strlen(src)%2) + 22 ;
   msg[40] = 0x52 ;
   msg[41] = 0x02 ;
   msg[42] = 0x20 ;
   msg[43] = 0x06 ;
   msg[44] = 0x24 ;
   msg[45] = 0x01 ;
   msg[46] = 0x05 ;
   msg[47] = 0x99 ;
   msg[48] = strlen(src) +(strlen(src)%2) + 8 ;
   msg[50] = 0x4c ;
   msg[51] = (strlen(src)+3)/2 + 1 ;
   msg[52] = 0x61 ;
   memset(tag,0,25) ;
   for (i = 0; i <= strlen(src); i++)
     tag[i] = toupper(src[i]) ;
   msg[53] = strlen(tag) ;
   memcpy(&msg[54],tag,strlen(tag)) ;
   msg_size = 54+strlen(tag)+(strlen(tag) % 2) ; 
   msg[msg_size++] = '(' ;
   msg[msg_size++] = start & 0xff ;
   msg[msg_size] = n_words & 0xff ;
   msg_size += 2 ;
   msg[msg_size] = 0x01 ;
   msg_size += 2 ;
   msg[msg_size] = 0x01 ;
   msg_size += 2 ;

   if ((count = write(plc->socket,&msg,msg_size)) != msg_size)
      {
      Alert("logix_read(): socket write error") ;
      err = LOGIX_ERR_SOCK ;
      @<Reconnect TCP@>@;
      }
   }



@ Send write request.
@<Send write request@>=
   {
   int i, j ;
   char msg[MSG_SIZE] ;
   int msg_size ;
   char tag[25] ;
   int count ;

   memset(msg,0,MSG_SIZE) ;
   msg[0] = 0x6f ;
   msg[2] = n_words*4 + strlen(dest) + (strlen(dest)%2)+ 22 + 16 ;
   msg[2] = n_words*4 + strlen(dest) + (strlen(dest)%2)+ 24 + 16 ;
   memcpy(&msg[4],&(plc->session_id),4) ;
   msg[12] = 0x02 ;
   msg[16] = 0xa0 ;
   msg[17] = 0xc6 ;
   msg[18] = 0xfa ;
   msg[19] = 0x01 ;
   msg[28] = 0x05 ;
   msg[30] = 0x02 ;
   msg[36] = 0xb2 ;
   msg[38] = n_words*4 + strlen(dest) + (strlen(dest)%2) + 22 ;
   msg[38] = n_words*4 + strlen(dest) + (strlen(dest)%2) + 24 ;
   msg[40] = 0x52 ;
   msg[41] = 0x02 ;
   msg[42] = 0x20 ;
   msg[43] = 0x06 ;
   msg[44] = 0x24 ;
   msg[45] = 0x01 ;
   msg[46] = 0x05 ;
   msg[47] = 0x99 ;
   msg[48] = n_words*4 + strlen(dest) +(strlen(dest)%2) + 8 ;
   msg[48] = n_words*4 + strlen(dest) +(strlen(dest)%2) + 10 ;
   msg[50] = 0x4d ;
   msg[51] = (strlen(dest)+3)/2 + 1 ;
   msg[52] = 0x61 ;
   memset(tag,0,25) ;
   for (i = 0; i <= strlen(dest); i++)
     tag[i] = toupper(dest[i]) ;
   msg[53] = strlen(tag) ;
   memcpy(&msg[54],tag,strlen(tag)) ;
   msg_size = 54+strlen(tag)+(strlen(tag) % 2) ; 
   msg[msg_size++] = '(' ;
   msg[msg_size++] = start & 0xff ;
   switch (the_type)
     {
     case LOGIX_INT :
       msg[msg_size] = 0xc3 ;
       break ;
     case LOGIX_DINT :
       msg[msg_size] = 0xc4 ;
       break ;
     }
   msg_size += 2 ;
   msg[msg_size] = n_words & 0xff ;
   msg_size += 2 ;
   switch (the_type)
     {
     case LOGIX_INT :
       for (i = 0; i < n_words; i++)
         {
         msg[msg_size] = data[i] & 0xff ;
         msg[msg_size+1] = (data[i] >> 8) & 0xff ;
         msg_size += 2 ;
         }
       break ;
     case LOGIX_DINT :
       for (i = 0; i < n_words; i++)
         {
         msg[msg_size] = data[i] & 0xff ;
         msg[msg_size+1] = (data[i] >> 8) & 0xff ;
         msg[msg_size+2] = (data[i] >> 16) & 0xff ;
         msg[msg_size+3] = (data[i] >> 24) & 0xff ;
         msg_size += 4 ;
         }
       break ;
     }
   msg[msg_size] = 0x01 ;
   msg_size += 2 ;
   msg[msg_size] = 0x01 ;
   msg_size += 2 ;

   if ((count = write(plc->socket,&msg,msg_size)) != msg_size)
      {
      Alert("logix_write(): socket write error") ;
      err = LOGIX_ERR_SOCK ;
      @<Reconnect TCP@>@;
      }
   }


@ |process_write()| 
@<Functions@>+=
int process_write(plc_handle plc)
   {
   fd_set rfds;
   struct timeval tv;
   int retval;
   unsigned char reply[MSG_SIZE] ;
   unsigned long int temp ;
   int count ;
   int i ;

   if (plc->enabled) 
     {
     FD_ZERO(&rfds);
     FD_SET(plc->socket, &rfds);
     tv.tv_sec = 1 ;
     tv.tv_usec = 0 ;
     while ((retval = select(plc->socket+1, &rfds, NULL, NULL, &tv)) > 0)
       {
       count = recv(plc->socket,&reply,1,0) ;
       if (count < 1)
         {
         Alert("connection lost") ;
         return LOGIX_ERR_CONN ;
         @<Reconnect TCP@>@;
         }
       else
         {
         response[idx++] = reply[0] ;
         if (response[0] != 0x6f)
           {
           Alert("Bad response") ;
           idx = 0 ;
           return LOGIX_ERR ;
           }
         if (idx >= 44)
           if (response[40] == (char)0xcd)
             {
             idx = 0 ;
             return response[42]+(response[43]<<8) ;
             }
           else
             {
             Alert("Bad PLC response (1)") ;
             idx = 0 ;
             return LOGIX_ERR ;
             }
         }
       }
       Alert( "process_write() timeout" ) ;
       return LOGIX_ERR_TIMEOUT ;
     }
     Alert( "PLC not enabled" ) ;
     return LOGIX_ERR ;
   }


@ |process_write()| prototype.
@<Prototypes@>=
int process_write(plc_handle plc) ;


@ |process_read()| 
@<Functions@>+=
int process_read(plc_handle plc, unsigned long int *data) 
   {
   fd_set rfds;
   struct timeval tv;
   int retval;
   unsigned char reply[MSG_SIZE] ;
   unsigned long int temp ;
   int count ;
   int i, n_values ;

   if (plc->enabled) 
     {
     n_values = 0 ;
     idx = 0 ;

     FD_ZERO(&rfds);
     FD_SET(plc->socket, &rfds);
     tv.tv_sec = 1 ;
     tv.tv_usec = 0 ;
     while ((retval = select(plc->socket+1, &rfds, NULL, NULL, &tv)) > 0)
       {
       count = recv(plc->socket,&reply,1,0) ;
       if (count < 1)
         {
         Alert("connection lost") ;
         @<Reconnect TCP@>@;
         }
       else
         {
         response[idx++] = reply[0] ;
         if (response[0] != 0x6f)
           {
           Alert("Bad response") ;
           idx = 0 ;
           return LOGIX_ERR ;
           }
         if (idx >= 46)
           {
           if (response[40] == (char)0xcc)
             {
             switch (response[44])
               {
               case (char)0xc3 :
                 n_values = (response[38]-6)/2 ;
                 if (idx >= 46+n_values*2)
                   {
                   for (i = 0; i < n_values; i++)
                     data[i] = response[46+i*2] +
                              (response[46+i*2+1] << 8) ; 
                   idx = 0 ;
                   return n_values ;
                   }
                 break ;
               case (char)0xc4 :
                 n_values = ((response[38]&0xff)-6)/4 ;
                 if (idx >= 46+n_values*4)
                   {
                   for (i = 0; i < n_values; i++)
                     memcpy(&data[i],&response[46+i*4],4) ;
                   idx = 0 ;
                   return n_values ;
                   }
                 break ;
               }
             }
           }
         }
       }

     Alert("logix_read() timeout") ;
     return LOGIX_ERR_TIMEOUT ;
     }
   }


@ |process_read()| prototype.
@<Prototypes@>=
int process_read(plc_handle plc, unsigned long int *data) ;



@ Process session ID. 
@<Process session ID@>=
  {
  fd_set rfds;
  struct timeval tv;
  int retval;
  char reply[MSG_SIZE] ;
  int count ;

  if (plc->enabled) 
    {
     
    FD_ZERO(&rfds);
    FD_SET(plc->socket, &rfds);
    tv.tv_sec = 1;
    tv.tv_usec = 0;
    retval = select(plc->socket+1, &rfds, NULL, NULL, &tv);
    if (retval > 0)
      {
      count = recv(plc->socket,&reply,28,0) ;
      if (count < 1)
        {
        Alert("connection lost") ;
        @<Reconnect TCP@>@;
        }
      else
        {
        memcpy(&plc->session_id,&reply[4],4) ;
        }
      }
    else
      {
      Alert("timeout in processing") ;
      @<Reconnect TCP@>@;
      }
    }
  }


@ Reconnect TCP.
@<Reconnect TCP@>=
   {
     plc->enabled = 0 ;
     close(plc->socket) ;
#if 0
     int w_count,count ;
     int s ;
     if (plc->enabled) {
       plc->enabled = 0 ;
       Alert("Reconnecting! %s",plc->address) ;
       


       sleep(2) ;
       plc->socket = s_open(plc->address,INTERCHANGE_PORT) ;
       Trace("now socket = %d",plc->socket) ;
       do {
	 char msg[MSG_SIZE] ;
	 char reply[100] ;
	 
	 
	 /* fill in the INTERCHANGE message for a new session 
	    (see section 4.1.1, INTERCHANGE Software: Client/Server Protocol) */
	 memset(msg,0,MSG_SIZE) ;
	 msg[0] = REQUEST ; /* INTERCHANGE flag */
	 msg[1] = NEW_SESSION ; /* INTERCHANGE command */
	 msg[13] = 4 ; /* INTERCHANGE protocol revision 4 */
	 msg[15] = 40 ; /* allow maximum number (40) of pending PCCC commands */
	 w_count = write(plc->socket,msg,28) ;
	 count = recv(plc->socket,&reply,4,MSG_PEEK) ;
	 memcpy(&count,&reply[2],2) ;
	 count = ntohs(count) ;
	 count = recv(plc->socket,&reply,count+28,0) ;
	 Trace("got back %d bytes",count) ;
       } while(w_count != 28) ;
     }
     plc->enabled = 1 ;

#endif
   }



@* Sockets.
We open and configure a socket to the PLC with this call.
@<Utility Functions@>=
int s_open(char *hostname, int port)
  {
  extern int errno ;
  int s ;
  struct sockaddr_in addr ;
  int len ;
  struct hostent *host ;

  @<Create the Socket@>@;
  @<Set Socket Options@>@;
  @<Configure the Socket@>@;
  @<Construct the Remote Address@>@;
  @<Connect the Socket@>@;
  return s ;
  }




@ The socket is created as a stream socket in the INET domain.
@<Create the Socket@>=
   s = socket(AF_INET,SOCK_STREAM,6) ;
   if(s < 0)
      return s ;



@ We set three socket options.
@<Set Socket Options@>=
   {
   int opt ;

   opt = 1 ;
   setsockopt(s,SOL_SOCKET,SO_KEEPALIVE,&opt,sizeof(opt)) ;

   opt = 2920 ;
   setsockopt(s,SOL_SOCKET,SO_SNDBUF,&opt,sizeof(opt)) ;
   setsockopt(s,SOL_SOCKET,SO_RCVBUF,&opt,sizeof(opt)) ;
   }



@ We configure the descriptor to be non-blocking.
@<Configure the Socket@>=
   {
   int opt ;
   opt = 1 ;
   }



@ It takes several steps to fill in the address.
@<Construct the Remote Address@>=
   @<Clear the address structure@>@;
   @<Set the address family@>@;
   @<Set the address port@>@;
   @<Convert the host name@>@;
   @<Copy in the host name@>@;


   
@ Clearing the structure first prevents some problems.
@<Clear the address structure@>=
   len = sizeof(struct sockaddr_in) ;
   memset(&addr,0,len) ;



@ The address family is always |AF_INET|.
@<Set the address family@>=
   addr.sin_family = AF_INET ;



@ The port is passed as a function argument.
It needs to be network ordered.
@<Set the address port@>=
   addr.sin_port = htons(port) ;


@ The name is tried first as a domain name, then as a dotted IP address.
@<Convert the host name@>=
   {
   if((host = gethostbyname(hostname)) == NULL)
      if((host = gethostbyaddr(hostname,4,AF_INET)) == NULL) 
         {
	 Alert("failed to locate PLC '%s'", hostname) ;
         return LOGIX_ERR ;
         }
   }



@ We copy the converted host name into the address structure.
@<Copy in the host name@>=
   memcpy(&addr.sin_addr,host->h_addr_list[0],host->h_length) ;



@ Finally we connect to the remote socket.
@<Connect the Socket@>=
   {
   if(connect(s,(struct sockaddr *)&addr,len) < 0)
      {
      Alert("Can't connect()") ;
      return LOGIX_ERR ;
      }
   }


@* Stub program for reading from the PLC.
@(logix_read.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_logix.h>
#include <rds_trn.h>

int main( int argc, char *argv[] ) 
   {
   plc_handle plc ;
   char file[64] ;
   int offset, num_words ;
   unsigned long data ;
   int i, err ;

   if (argc != 5)
      {
      fprintf( stderr, "usage: %s <plc> <file> <offset> <num_words>\n",
            argv[0] );
      exit( EXIT_SUCCESS );
      }

   trn_register("logix") ;

   plc = logix_open( argv[1] ) ;
   strcpy( file, argv[2] ) ;
   offset = atoi( argv[3] ) ;
   num_words = atoi( argv[4] ) ;

   err = 0;
   for (i = 0 ; i < num_words ; i++)
      {
      err += logix_read( plc, file, offset + i, 1, LOGIX_DINT, &data ) ;
      printf( "%lu\n", data );
      Inform( "read from %s:%s[%d] -> %lu, %lu (%lx)",
            argv[1], file, offset + i, data >> 16, data & 0x0FFFF, data ) ;
      }

   if (err < 0)
      {
      Trace( "error during logix read: %d", err ) ;
      exit( EXIT_FAILURE ) ;
      }

   exit( EXIT_SUCCESS ) ;
   }


@* Stub program for writing to the PLC.
@(logix_write.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_logix.h>
#include <rds_trn.h>

int main( int argc, char *argv[] ) 
   {
   plc_handle plc ;
   char file[64] ;
   int offset, num_words ;
   unsigned long data ;
   int i, err ;

   if (argc != 6)
      {
      fprintf( stderr, "usage: %s <plc> <file> <offset> <num_words> <data>\n",
            argv[0] );
      exit( EXIT_SUCCESS );
      }

   trn_register("logix") ;

   plc = logix_open( argv[1] ) ;
   strcpy( file, argv[2] ) ;
   offset = atoi( argv[3] ) ;
   num_words = atoi( argv[4] ) ;
   data = strtoul( argv[5], (char **) NULL, 10 ) ;

   Inform( "write to %s:%s[%d-%d] -> %lu (%0lx)", argv[1], file, offset,
         offset + num_words - 1, data, data ) ;

   err = 0;
   for (i = 0 ; i < num_words ; i++)
      err += logix_write( plc, file, offset + i, 1, LOGIX_DINT, &data ) ;

   if (err < 0)
      {
      Trace( "Error during logix_write()" ) ;
      exit( EXIT_FAILURE ) ;
      }

   exit( EXIT_SUCCESS ) ;
   }


@* Test program.
|logix_test.c| is a program that tests the functions in the logix library.
@(logix_test.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_trn.h>
#include <rds_logix.h>

main(int argc, char *argv[]) 
  {
  plc_handle plc ;
  int err, transaction, read_transaction ;
  int i, iter, offset, n ;
  unsigned long int rbuf[100], wbuf[100] ;
  char file[64] ;

  if (argc < 5) {
    fprintf( stderr,
      "usage: %s <plc> <file> <offset> <num_words> [<data> ...]\n", argv[0] );
    exit( EXIT_SUCCESS );
  }

  trn_register("logix") ;
  Trace("----------- logix test starting...") ;

  plc = logix_open(argv[1]) ;
  strcpy(file,argv[2]) ;
  offset = atoi( argv[3] ) ;
  n = atoi( argv[4] ) ;

  Trace("plc session established, session = %d",plc->session_id) ;

  for (iter=1; ;iter++)
     {
     sleep(1) ;


#if 0
     err = logix_read(plc,file,offset,n,LOGIX_DINT,rbuf) ;
     Trace("%s: %c %c %c %c %c %c %c %c %c %c",
         file,
         rbuf[0] & (1<<1) ? '1' : '-', 
         rbuf[0] & (1<<2) ? '2' : '-', 
         rbuf[0] & (1<<3) ? '3' : '-', 
         rbuf[0] & (1<<4) ? '4' : '-', 
         rbuf[0] & (1<<5) ? '5' : '-', 
         rbuf[0] & (1<<6) ? '6' : '-', 
         rbuf[0] & (1<<7) ? '7' : '-', 
         rbuf[0] & (1<<8) ? '8' : '-', 
         rbuf[0] & (1<<9) ? '9' : '-', 
         rbuf[0] & (1<<10) ? '0' : '-' ) ;
#endif

#if 1
     if (argc >= 6)
        {
        int i;
        for (i = 0 ; i < argc - 5 ; i++ )
           wbuf[i] = atoi( argv[i+5] );

        Trace("writing %s iteration %d",file,iter) ;
        while ((err = logix_write(plc,file,offset,n,LOGIX_DINT,wbuf)) < 0)
          {
          Alert("Error in write: %d", err) ;
          sleep(1) ;
          iter = 1 ;
          }
       }
#endif

#if 1
     while ((err = logix_read(plc,file,offset,n,LOGIX_DINT,rbuf)) < 0)
       {
       Alert("Error in read: %d", err) ;
       sleep(1) ;
       iter = 1 ;
       }
#endif

#if 1
     for (i=0;i<n;i++)
       Trace("%s[%2d]: %ld",file,i+offset,rbuf[i]) ;
#endif
     }
   }


@* Index.
