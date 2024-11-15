% 
%   rds_net.web 
%
%   Author: Mark Woodworth 
%
%   History:
%      7/10/00 -- check in (mrw)
%      8/27/04 -- fixed a bug in net_open (ank).
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
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
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
\def\dot{\quad\qquad\item{$\bullet$\ }}


%
% --- title ---
%
\def\title{RDS3 NET -- Network Utilities}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The NET library encapsulates some often used functions when working with
sockets.


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
}


%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}



@* Overview.
The Net library provides some useful network routines.
This library is a shared C library and exports the following functions:

\dot |net_open(host,port)| returns a file handle for a socket connected to the
`port' on `host'. Host may point to a domain name or to an ethernet address. 

\dot |net_serve(port)| returns a file handle for a socket bound to the `port'.



@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes. Standard system includes are collected here, as well as this
library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>
#include <signal.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include "rds_net.h" 



@ Header.  We put the exported prototypes in an external header.
@(rds_net.h@>=
#ifndef __RDS_NET_H
#define __RDS_NET_H
#include <sys/types.h>
#include <sys/socket.h>
  @<Exported Prototypes@>@;
#endif



@* Net Open.  Open a socket.
@<Exported Functions@>+=
int net_open( char *host, int port ) {
  int s;
  struct hostent *hp;
  struct sockaddr_in name;
  struct in_addr addr;

  if( ( s = socket( AF_INET, SOCK_STREAM, 0 ) ) < 0 ) return( -1 );
  memset( &name, 0, sizeof( struct sockaddr_in ) );
  name.sin_family = AF_INET;
  name.sin_port = htons( port );
  if( ( hp = gethostbyname( host ) ) == NULL ) return( -2 );
  memcpy( &name.sin_addr, hp->h_addr_list[ 0 ], sizeof( name.sin_addr ) );
  if( connect( s, ( struct sockaddr* )&name, sizeof( name ) ) < 0 ) {
    close( s );
    return( -3 );
  }
  return( s );
}
@ Prototype.
@<Exported Prototypes@>+=
int net_open( char *host, int port );



@ Test Stub.  A test stub program |net_open| is also created.
@(net_open.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <rds_net.h>
int main( int argc, char **argv ) {
  int ret;

  if( argc != 3 ) {
    fprintf( stderr, "Usage: %s <host> <port>\n", argv[ 0 ] );
    exit( 1 );
  }
  ret = net_open( argv[ 1 ], atoi( argv[ 2 ] ) );
  fprintf( stdout, "net_open(%s,%d) = %d\n", argv[ 1 ], atoi( argv[ 2 ] ), ret);
  fflush( stdout );
  if( ret >= 0 ) close( ret );
  return( 0 );
}



@* Net Serve. Bind to act as a server for a socket.
@<Exported Functions@>+=
int net_serve( int port ) {
  int i, s;
  struct sockaddr_in name;

  if( ( s = socket( AF_INET, SOCK_STREAM, 0 ) ) < 0 ) return( -1 );

  i = 1;
  setsockopt( s, SOL_SOCKET, SO_REUSEADDR, &i, sizeof(int) );

  bzero( &name, sizeof( struct sockaddr_in ) );
  name.sin_family = AF_INET;
  name.sin_port = htons( port );
  if( bind( s, ( struct sockaddr* )&name, sizeof( name ) ) < 0 ) {
    close( s );
    return( -3 );
  }
  listen( s, 5 );
  signal( SIGCHLD, child_handler );
  signal( SIGPIPE, pipe_handler );
  return( s );
}
@ Prototype.
@<Exported Prototypes@>+=
int net_serve( int port );



@ Child Handler. This function handles the child process change signals to
wait on their exit status.
@<Functions@>+=
static void child_handler( int sig ) {
  pid_t pid;
  int status;

  pid = waitpid( -1, &status, WNOHANG );
  signal( SIGCHLD, child_handler );
}
@ Prototype.
@<Prototypes@>+=
static void child_handler( int sig );



@ Pipe Handler.  This function handles the 
pipe error signal.
@<Functions@>+=
static void pipe_handler( int sig ) {
  exit( 1 );
}
@ Prototype.
@<Prototypes@>+=
static void pipe_handler( int sig );



@ Test server. This test server receives connections. accepts and prints
anything it gets.
@(net_server.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <rds_net.h>
int main( int argc, char **argv ) {
  int s, ns;
  struct sockaddr addr;
  int len;

  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <port>\n", argv[ 0 ] );
    exit( 1 );
  }
  s = net_serve( atoi( argv[ 1 ] ) );
  if( s < 0 ) {
    fprintf( stderr, "failed to bind, port [%s]\n", argv[ 1 ] );
    exit( 1 );
  }

  for( ;; ) {
    len = sizeof( addr );
    ns = accept( s, &addr, &len );
    if( !fork() ) {
      int err;
      char c;
      for( ;; ) {
        if( ( err = read( ns, &c, 1 ) ) != 1 ) exit( 0 );
        write( 1, &c, 1 );
      }
    }
    else close( ns );
  }
  return( 0 );
}



@*Index.
