% 
%   rds_role.web
%
%   Author: Mark Olson 
%
%   History:
%       7/10/00  (mdo) -- check in
%       8/9/2004 (ank) -- Recompiled for RH9, fixed some warnings.
%     2006/08/02 (ahm) -- in role_find(), obtain bcast addr via ioctl;
%                         also, some code cleanup and additional tracing
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
%     (C) Copyright 2002 Numina Systems Corporation.  All Rights Reserved.
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
\def\dot{\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{RDS3 Role -- Network Role Query \& Retrieval Utilities}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library encapsulates role requests and replies. The role query is 
contained in a database.

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
\centerline{Authors: Mark Olson}

}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Role: The Role library and server
This library is a shared C library 

@ Exported Functions.  This library exports the following functions:
{\narrower\narrower
\dot |char *role_find(char *id)| --- returns the IP address (string) of a
machine matching that identity.

And a server |roled|, which takes on the command lines all role identity
strings to match.
}

@* Overview.
@c
@<Includes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes.  Standard sytem includes are collected here, as
well as this library prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <signal.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <netdb.h>
#include <netinet/in.h>


@ Header.  We put the exported prototypes in an external header.
@(rds_role.h@>=
#ifndef __RDS_ROLE_H
#define __RDS_ROLE_H

#include <sys/types.h>
#include <sys/socket.h>
  @<Exported Prototypes@> @;
#endif


@ Internal function |get_broadcast_addr()|.  Returns the broadcast address
for the specified network interface (typically eth0).
@<Functions@>+=
in_addr_t get_broadcast_addr( int s, char *name ) {
  struct ifreq ifr;
  struct sockaddr_in bcast_addr;

  strncpy( ifr.ifr_name, name, IF_NAMESIZE );
  ifr.ifr_name[IF_NAMESIZE] = '\0';

  if( ioctl( s, SIOCGIFBRDADDR, (char *) &ifr ) < 0 )
    return INADDR_NONE;
  memcpy( (char *) &bcast_addr, (char *) &(ifr.ifr_broadaddr),
      sizeof ifr.ifr_broadaddr );
  return bcast_addr.sin_addr.s_addr;
}


@ Function |role_find|. Finds a computer on the net with specified role.
@f fd_set int
@<Exported Functions@>+=
char *role_find( char *id ) {
  int s, len, set, err;
  struct sockaddr_in name, server;
  struct in_addr addr;
  static char reply[ 80 ];
  struct timeval tv;
  fd_set fds;

  if( ( s = socket( AF_INET, SOCK_DGRAM, 0 ) ) < 0 ) return NULL;

  memset( &name, 0, sizeof( struct sockaddr_in ) );
  name.sin_family = AF_INET;
  name.sin_port = htons( 0x1261 );
  name.sin_addr.s_addr = get_broadcast_addr( s, "eth0" );

  set = 1;
  err = setsockopt( s, SOL_SOCKET, SO_BROADCAST, ( const void* )&set, sizeof( int ));
  if( err < 0 ) {
    close( s );
    return( NULL );
  }

  len = sizeof( struct sockaddr_in );
  err = sendto( s, id, strlen( id ), 0, ( struct sockaddr* )&name, len );
  if( err < 0 ) {
    close( s );
    return( NULL );
  }

  memset( &name, 0, sizeof( struct sockaddr_in ) );
  name.sin_family = AF_INET;
  name.sin_port = htons( 0x1261 );
  name.sin_addr.s_addr = htonl( INADDR_ANY );

  tv.tv_sec = 1;
  tv.tv_usec = 0;
  FD_ZERO( &fds );
  FD_SET( s, &fds );
  err = select( s + 1, &fds, NULL, NULL, &tv );

  if( !FD_ISSET( s, &fds ) ) return( NULL );
  err = recvfrom( s, reply, 80, 0, ( struct sockaddr* )&name, &len );
  close( s );
  if( err <= 0 ) return( NULL );

  reply[ err ] = '\0';
  return( reply );
}
@ Prototype.
@<Exported Prototypes@>+=
char *role_find( char *id );



@ Role Daemon.  A daemon program |roled| is also created.  This daemon
waits for udp broadcasts with role requests and responds if the requested
role is in the list of roles supplied on the command line.
@(roled.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <net/if.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>


char* find_prot_addr( int s, char *name ) {
  struct ifreq ifr;
  struct sockaddr_in prot_addr;

  strncpy( ifr.ifr_name, name, IF_NAMESIZE );
  ifr.ifr_name[IF_NAMESIZE] = '\0';

  if( ioctl( s, SIOCGIFADDR, (char *) &ifr ) < 0 )
    return NULL;
  memcpy( (char *) &prot_addr, (char *) &(ifr.ifr_addr),
      sizeof ifr.ifr_addr );
  return inet_ntoa( prot_addr.sin_addr );
}


int in_list( char *req, int ac, char **av ) {
  while( --ac > 0 ) {
    if( !strcmp( req, av[ ac ] ) ) return 1;
  }
  return 0;
}


int main( int argc, char **argv ) {
  int i, s, len, slen, err;
  struct sockaddr_in name, remote;
  char buf[ 80 ], request[ 80 ];
  char *reply;

  trn_register( "roled" );
  Inform( "init with %d role(s)", argc - 1 );
  for( i = 1; i < argc; i++)
    Inform( "   role %d = [%s]", i, argv[i] );

  if( ( s = socket( AF_INET, SOCK_DGRAM, 0 ) ) < 0 ) {
    Alert( "unable to obtain socket, exiting" );
    exit( EXIT_FAILURE );
  }

  len = sizeof( struct sockaddr_in );
  memset( &name, 0, len );
  name.sin_family = AF_INET;
  name.sin_port = htons( 0x1261 );
  name.sin_addr.s_addr = htonl( INADDR_ANY );

  if( bind( s, ( struct sockaddr* )&name, len ) < 0 ) {
    Alert( "unable to bind to socket, exiting" );
    exit( EXIT_FAILURE );
  }
   
  for( ;; ) {
    len = sizeof( struct sockaddr_in );
    err = recvfrom( s, request, 80, 0, ( struct sockaddr* )&remote, &len );
    if( err < 0 ) continue;
    request[ err ] = '\0';
    Inform( "got request for [%s] from %s", request,
        inet_ntoa( remote.sin_addr ) );
    if( in_list( request, argc, argv ) ) {
      reply = find_prot_addr( s, "eth0" );
      if( !reply ) {
        Alert( "error determining local network address" );
        continue;
      }
      sendto( s, reply, strlen( reply ), 0, ( struct sockaddr* )&remote, len );
      Trace( "responded to request for [%s] from %s", request,
          inet_ntoa( remote.sin_addr ) );
    }
  }

  exit( EXIT_SUCCESS );
}




@ |role_find.c|.
@(role_find.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_role.h>

int main( int argc, char **argv ) {
  char *result ;

  if( argc != 2 ) {
    fprintf( stderr, "Usage: %s <role_name>\n", argv[ 0 ] );
    fflush( stderr ); 
    return( -1 );
  }
  result = role_find( argv[ 1 ] );
  if( result != NULL )  fprintf( stdout, "%s got role [%s]\n", result, argv[1]);
  else fprintf( stdout, "No response for role [%s]\n", argv[ 1 ] );
  fflush( stdout );
  return 0;
}

@* Index.
