%
%   Author: Alex Korzhuk
%
%   History:
%      10/1/03 - check in (ank)
%
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
%     (C) Copyright 2003 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Database Daemon}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program reads the carton update queue and writes messages 
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
\centerline{Author: Alex Korzhuk}
\centerline{Format Date: \today}
\centerline{RCS Revision $ $Revision: 1.1 $ $}
\centerline{RCS Date $ $Date: 2003/10/1 13:50:16 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Introduction. 
This program write data bits to parallel port directly. Must be root to run.
@c
@<Includes@>@;
@<Prototypes@>@;
@<Functions@>@;

int main( int argc, char *argv[] ) {
  int iAddr, iByteValue;
  unsigned char byValue;
  
  iAddr = 0x378;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s [<byte_value>]\n", argv[ 0 ] );
    exit( 1 );
  }
  iByteValue = atoi( argv[ 1 ] );
  byValue = ( unsigned char )( 0xFF & iByteValue );

  if( ioperm( iAddr, 3, 1 ) == -1 ) {
    perror( "Error in ioperm(). Must be root to run\n" );
    exit( 1 );
  }
  outb( byValue, iAddr );
  fprintf( stdout, "Value %d (0x%02.2X) written to port 0x%3X\n",
           iByteValue, iByteValue, iAddr );

  if( ioperm( iAddr, 3, 0 ) == -1 ) {
    perror( "Error in ioperm() releasing the port\n" );
    exit( 1 );
  }

  return( 0 );
}

@ The following include files are used.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <ctype.h>
#include <sys/types.h>
#include <sys/io.h>
#include <sys/ioctl.h>



@ The |test()| function.
@<Functions@>+=
int test( void ) {
  return( 0 );
}
@ Prototype.
@<Prototypes@>+=
int test( void );


@* Index.
