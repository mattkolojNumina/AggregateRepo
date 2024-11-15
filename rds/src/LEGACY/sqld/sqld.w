%  sqld.web
%
%   Author: Mark Woodworth
%
%   History:
%      09/09/99 - started (mrw)
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
%     (C) Copyright 1999 Numina Systems Corporation.  All Rights Reserved.
%
%
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
%
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
%
\def\title{Counter Daemon}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program reads the counter message queue and updates records
into the database.
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
\centerline{Author: Mark Woodworth}
\centerline{Format Date: \today}
\centerline{RCS Revision $ $Revision: 1.1 $ $}
\centerline{RCS Date $ $Date: 2008/03/31 21:03:03 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Introduction. 
This program reads messages and updates Postgres database.
@c
@<Includes@>@;

int main( int argc, char *argv[] ) {
  char *pszMsg;
  int iMsgType, iReader = 24;

  trn_register( "sqld" );
  if( argc == 5 ) {
    Inform("Connecting to %s db on %s host as %s user",argv[4],argv[1],argv[2]);
    sql_setconnection( argv[ 1 ], argv[ 2 ], argv[ 3 ], argv[ 4 ] );
  }
  else if( argc != 1 ) {
    Alert( "Usage: %s [<machine> <user> <password> <database>]", argv[ 0 ] );
    fprintf( stderr, "Usage: %s [<machine> <user> <password> <database>]\n",
             argv[ 0 ] );
    fflush( stderr );
    return( 1 );
  }
  Trace( "Init" );

  for( ;; sleep( 5 ) ) {
    while( ( iMsgType = msg_type( iReader ) ) > 0 ) {
      switch( iMsgType ) {
        case SQL_MSG: @;
          pszMsg = msg_recv( iReader );
          sql_query( pszMsg );
          Trace( "%s", pszMsg );
          free( pszMsg );
          break;
        default: @;
          break;
      }
      msg_next( iReader );
    }
  }
}



@ The following include files are used.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <ctype.h>
#include <signal.h>
#include <time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <sys/time.h>

#include <rds_msg.h>
#include <rds_sql.h>
#include <rds_sqld.h>



@* Index.
