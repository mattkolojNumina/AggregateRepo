% 
%
%   rds_sqld.web
%
%   Author: Alex Korzhuk
%
%   History:
%      8/9/2004  (ank) -- sqld_post function implementation
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
%     (C) Copyright 2000-2004 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{SQLD Library}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a wrapper for SQL access. This version uses mySQL, a fast public 
domain (in linux) database which is available via RPM. This package gives
a simpler interface to the connection, query, and results handling 

It is one of the auxiliary packages of the RDS system.


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
\centerline{Authors: Alex Korzhuk}
\centerline{Revision Date: \today}
\centerline{Version: 3.0}
}


%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 2004 Numina Systems Corporation.  
All Rights Reserved.}
}



@* Overview.
SQLD shared library contains |sqld_post()| function, 


@* Library. 
@(rds_sqld.c@>=
@<Includes@>@;
@<Exported Functions@>@;



@ The Header file.  
@(rds_sqld.h@>=
#ifndef __SQLD_H
#define __SQLD_H
#include <rds_msg.h>
  @<Exported Definitions@>@;
  @<Exported Prototypes@>@;
#endif



@* Library Implementation. We start with the includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include "rds_sqld.h"


@ Exported definitions.
@<Exported Definitions@>+=
#define SQL_MSG ( 24 )


@ |sqld_post()|. Returns 0 on success, |(-1)| on error.
@<Exported Functions@>+=
int sqld_post( char *fmt, ... ) {
  va_list ap;
  char *query;

  va_start( ap, fmt );
  vasprintf( &query, fmt, ap );
  va_end( ap );
  if( msg_send( SQL_MSG, query ) < 0 ) return( -1 );
  return( 0 );
}
@ Prototype.
@<Exported Prototypes@>+=
int sqld_post( char *fmt, ... );



@* Test Stub. A test stub program |sqld_post| is also created.
@(sqld_post.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rds_sqld.h>

int main( int argc, char **argv ) {
  int i;
  char szText[ 512 ];

  if( argc < 2 ) {
    fprintf( stderr, "Usage: %s <sql_query>\n", argv[ 0 ] );
    fflush( stderr );
    return( 1 );
  }
  strcpy( szText, argv[ 1 ] );
  for( i = 2; i < argc; i++ ) {
    strcat( szText, " " );
    strcat( szText, argv[ i ] );
  }

  if( sqld_post( szText ) < 0 )
    fprintf( stdout, "Error posting query [%s] to msg queue\n", szText );
  else fprintf( stdout, "Query [%s] successfully posted to msg queue\n",szText);
  fflush( stdout );
}
  

@*Index.

