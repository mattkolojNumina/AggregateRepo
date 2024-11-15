%
%   rds_par.web
%
%   Author: Alex Korzhuk
%
%   History:
%      9/30/03 -- check in par source (ank)
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
\def\title{par -- parallel port IOs}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The par library is a collection of calls that allows to use parallel port IOs.

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
\centerline{Author: Alex Korzhuk}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2003 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview. This library is a shared C library. Works with kernel 2.4.x+

1. The device |/dev/parportX| should exist (major 99), where X is 0,1,...
and have read/write permisions for the user: |crw-rw-rw-|


2. Drivers |parport_pc|, |ppdev|, and |parport| must be loaded:

      |/sbin/modprobe parport|

      |/sbin/modprobe parport_pc io=0x378 irq=7|

      |/sbin/modprobe ppdev| 


Parallel port connector: 25 PIN D-SUB FEMALE at the PC.
The pinout for the parallel port is:


{\tt\obeylines\obeyspaces
Pin  Reg  Dir  Name                 Common Usage
---  ---  ---  -------------------  -----------------------------
1    -C0  ==>  -STROBE              Set Low pulse >0.5 us to send
2    +D0  ==>  +Data Bit 0          Set to least significant data
3    +D1  ==>  +Data Bit 1
4    +D2  ==>  +Data Bit 2
5    +D3  ==>  +Data Bit 3
6    +D4  ==>  +Data Bit 4
7    +D5  ==>  +Data Bit 5
8    +D6  ==>  +Data Bit 6
9    +D7  ==>  +Data Bit 7          Set to most significant data
10   +S6  <==  -ACK (Ack,IRQ)       Low Pulse ~ 5 uS, after accept
11   -S7  <==  +BUSY                High for Busy/Offline/Error
12   +S5  <==  +PE (Paper End)      High for out of paper
13   +S4  <==  +SELIN (Select In)   High for printer selected
14   -C1  ==>  -AUTOFD (Autofeed)   Set Low to autofeed one line
15   +S3  <==  -ERROR (Error)       Low for Error/Offline/PaperEnd
16   +C2  ==>  -INIT (Initialize)   Set Low pulse > 50uS to init
17   -C3  ==>  -SEL (Select)        Set Low to select printer
18             GND (Signal Ground)
19             GND (Signal Ground)
20             GND (Signal Ground)
21             GND (Signal Ground)
22             GND (Signal Ground)
23             GND (Signal Ground)
24             GND (Signal Ground)
25             GND (Signal Ground)
--   +C4       internal             IRQ enable
--   +C5       internal             Tristate data
}

\par
|<== In| and |==> Out| are defined from viewpoint of the PC, not the printer.

The IRQ line |-ACK/S6+| is positive edge triggered, but only enabled if C4 is 1.

|-C0| means C0 has an inverted output.

|+C2| means C2 has straight output.

Straight output is |~0| volts for bit value 0 and |~3.3| volts for bit value 1.

Inverted output is |~5| volts for bit value 0 and |~0.9| volts for bit value 1.


@ Exported functions. This library exports the following functions:
\dot |par_open()| opens a parallel port.
\dot |par_closes()| closes a parallel port.
\dot |par_getbyte()| gets values of the 8 data bits of the parallel port.
\dot |par_setbyte()| sets values of the 8 data bits in parallel port.
\dot |par_setbit()| sets a single data bit in parallel port.
\dot |par_getcbyte()| gets value of the control byte of the parallel port.
\dot |par_setcbit()| sets a single control bit in parallel port.
\dot |par_getsbyte()| gets value of the status byte of the parallel port.
\dot |par_setstobe()| sets a value of a stobe bit of the parallel port.



@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Exported Functions@>@;



@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <linux/ppdev.h>
#include <linux/parport.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include "rds_par.h"



@ Header. The exported prototypes and error codes are in an external header
@(rds_par.h@>=
#ifndef __RDS_PAR_H
#define __RDS_PAR_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif



@ Exported definitions
@<Exported Defines@>+=
#define ERRPAR_GENERAL     (-1)
#define ERRPAR_ARG         (-2)
#define ERRPAR_PORTNUM     (-3)
#define ERRPAR_OPEN        (-4)
#define ERRPAR_CLAIM       (-5)
#define ERRPAR_RELEASE     (-6)
#define ERRPAR_SETDATA     (-7)
#define ERRPAR_GETDATA     (-8)



@* The |par_open| function.
@<Exported Functions@>+=
int par_open( int iPortNum ) {
  int hPort, iArg;
  char szDev[ 20 ];

  if( iPortNum < 0 || iPortNum > 7 ) return( ERRPAR_PORTNUM );
  sprintf( szDev, "/dev/parport%d", iPortNum );

  if( ( hPort = open( szDev, O_RDWR ) ) < 0 ) return( ERRPAR_OPEN );

#if 0
  if( ioctl( hPort, PPEXCL ) ) return( ERRPAR_CLAIM ); //no need for this
#endif

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  iArg = IEEE1284_MODE_COMPAT; /*|IEEE1284_MODE_BYTE|*/
  if( ioctl( hPort, PPSETMODE, &iArg ) ) return( ERRPAR_CLAIM );

  iArg = 0; //forward mode
  if( ioctl( hPort, PPDATADIR, &iArg ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( hPort );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_open( int iPortNum );



@* The |par_close| function.
@<Exported Functions@>+=
int par_close( int hPort ) {
  if( hPort <= 0 ) return( 0 );

  ioctl( hPort, PPRELEASE );
  close( hPort );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_close( int hPort );



@* The |par_getbyte()| function. Gets value of the data register (bits D0...D7)
of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|piByteValue| - ptr to buffer to store the value of the data register.

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_getbyte( int hPort, int *piByteValue ) {
  unsigned char byValue = 0;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPRDATA, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_GETDATA );
  }
  *piByteValue = ( int )byValue;

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_getbyte( int hPort, int *piByteValue );


@ Test Stub. A test stub program |par_getbyte| is also created.
@(par_getbyte.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, hPort, iByteValue, iErr;
  char *pszErr;

  if( argc != 2 ) {
    fprintf( stdout, "Usage:  %s {<port_num>}\n", argv[ 0 ] );
    fflush( stdout );
  }

  iPortNum = ( argc > 1 ) ? atoi( argv[ 1 ] ) : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_getbyte( hPort, &iByteValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Got data register value %d (0x%02.2X) from "
                "par port %d\n", iByteValue, iByteValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_setbyte| function. Sets value in the data register (bits D0...D7)
of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|iByteValue| - the value of the data register to set.

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_setbyte( int hPort, int iByteValue ) {
  unsigned char byValue;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  byValue = ( unsigned char )( 0xFF & iByteValue );
  if( ioctl( hPort, PPWDATA, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_SETDATA );
  }

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_setbyte( int hPort, int iByteValue );


@ Test Stub. A test stub program |par_setbyte| is also created.
@(par_setbyte.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, iByteValue, hPort, iErr;
  char *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s {<port_num>} [<byte_value>]\n", argv[ 0 ] );
    exit( 1 );
  }

  iPortNum = ( argc == 3 ) ? atoi( argv[ 1 ] ) : 0;
  iByteValue = ( argc == 3 ) ? atoi( argv[ 2 ] ) : atoi( argv[ 1 ] );

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_setbyte( hPort, iByteValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Set %d (0x%02.2X) in data register of par port %d\n",
                iByteValue, iByteValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_setbit| function. Sets single bit (D0...D7) in the data register
of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|iBit| - bit number to set (from 0 to 7).

|iBitValue| - bit value to set (0 or 1).

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_setbit( int hPort, int iBit, int iBitValue ) {
  unsigned char byValue, byMask;

  if( iBit < 0 || iBit > 7 ) return( ERRPAR_ARG );

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPRDATA, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_GETDATA );
  }

  byMask = ( unsigned char )1 << iBit;
  byValue &= ~byMask;
  if( iBitValue ) byValue |= byMask;

  if( ioctl( hPort, PPWDATA, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_SETDATA );
  }

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_setbit( int hPort, int iBit, int iBitValue );



@ Test Stub. A test stub program |par_setbit| is also created.
@(par_setbit.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, iBit, iBitValue, hPort, iErr;
  char *pszErr;

  if( argc < 3 || argc > 4 ) {
    fprintf( stderr, "Usage:  %s {<port_num>} [<bit_num>] [<bit_value>]\n",
             argv[ 0 ] );
    exit( 1 );
  }

  iPortNum = ( argc == 4 ) ? atoi( argv[ 1 ] ) : 0;
  iBit = ( argc == 4 ) ? atoi( argv[ 2 ] ) : atoi( argv[ 1 ] );
  iBitValue = ( argc == 4 ) ? atoi( argv[ 3 ] ) : atoi( argv[ 2 ] );
  iBitValue = iBitValue ? 1 : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_setbit( hPort, iBit, iBitValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Bit D%d set to %d in par port %d\n",
                iBit, iBitValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_getcbyte()| function. Gets value of the control register
(bits C0...C3) of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|piByteValue| - ptr to buffer to store the value of control register.

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_getcbyte( int hPort, int *piByteValue ) {
  unsigned char byValue = 0;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPRCONTROL, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_GETDATA );
  }
  *piByteValue = ( int )byValue;

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_getcbyte( int hPort, int *piByteValue );


@ Test Stub. A test stub program |par_getcbyte| is also created.
@(par_getcbyte.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, hPort, iByteValue, iErr;
  char *pszErr;

  if( argc != 2 ) {
    fprintf( stdout, "Usage:  %s {<port_num>}\n", argv[ 0 ] );
    fflush( stdout );
  }

  iPortNum = ( argc > 1 ) ? atoi( argv[ 1 ] ) : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_getcbyte( hPort, &iByteValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Got control register value %d (0x%02.2X) from "
                "par port %d\n", iByteValue, iByteValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_setcbit| function. Sets single bit (C0...C3) in the control
register of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|iBit| - bit number to set (from 0 to 3).

|iBitValue| - bit value to set (0 or 1).

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_setcbit( int hPort, int iBit, int iBitValue ) {
  struct ppdev_frob_struct frob;

  if( iBit < 0 || iBit > 3 ) return( ERRPAR_ARG );
  switch( iBit ) {
    case 0: frob.mask = PARPORT_CONTROL_STROBE; break;
    case 1: frob.mask = PARPORT_CONTROL_AUTOFD; break;
    case 2: frob.mask = PARPORT_CONTROL_INIT; break;
    case 3: frob.mask = PARPORT_CONTROL_SELECT; break;
    default: return( ERRPAR_ARG );
  }
  frob.val = iBitValue ? frob.mask : 0;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPFCONTROL, &frob ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_SETDATA );
  }

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_setcbit( int hPort, int iBit, int iBitValue );


@ Test Stub. A test stub program |par_setcbit| is also created.
@(par_setcbit.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, iBit, iBitValue, hPort, iErr;
  char *pszErr;

  if( argc < 3 || argc > 4 ) {
    fprintf(stderr,"Usage:  %s {<port_num>} [<ctrl_bit_num>] [<bit_value>]\n",
             argv[ 0 ] );
    exit( 1 );
  }

  iPortNum = ( argc == 4 ) ? atoi( argv[ 1 ] ) : 0;
  iBit = ( argc == 4 ) ? atoi( argv[ 2 ] ) : atoi( argv[ 1 ] );
  iBitValue = ( argc == 4 ) ? atoi( argv[ 3 ] ) : atoi( argv[ 2 ] );
  iBitValue = iBitValue ? 1 : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_setcbit( hPort, iBit, iBitValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Bit C%d set to %d in par port %d\n",
                iBit, iBitValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_getsbyte()| function. Gets value of the status register
(bits S3...S7) of the parallel port.

|hPort| - port handle, returned by the |open()| function.

|piByteValue| - ptr to buffer to store the value of status register.

The function returns 0 on success, or errorcode (|<0|) on error.
@<Exported Functions@>+=
int par_getsbyte( int hPort, int *piByteValue ) {
  unsigned char byValue = 0;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPRSTATUS, &byValue ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_GETDATA );
  }
  *piByteValue = ( int )byValue;

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_getsbyte( int hPort, int *piByteValue );


@ Test Stub. A test stub program |par_getsbyte| is also created.
@(par_getsbyte.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, hPort, iByteValue, iErr;
  char *pszErr;

  if( argc != 2 ) {
    fprintf( stdout, "Usage:  %s {<port_num>}\n", argv[ 0 ] );
    fflush( stdout );
  }

  iPortNum = ( argc > 1 ) ? atoi( argv[ 1 ] ) : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_getsbyte( hPort, &iByteValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Got status register value %d (0x%02.2X) from "
                "par port %d\n", iByteValue, iByteValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}



@* The |par_setstrobe| function.
@<Exported Functions@>+=
int par_setstrobe( int hPort, int iBitValue ) {
  struct ppdev_frob_struct frob;

  frob.mask = PARPORT_CONTROL_STROBE;
  frob.val = iBitValue ? PARPORT_CONTROL_STROBE : 0;

  if( ioctl( hPort, PPCLAIM ) ) return( ERRPAR_CLAIM );

  if( ioctl( hPort, PPFCONTROL, &frob ) ) {
    ioctl( hPort, PPRELEASE );
    return( ERRPAR_SETDATA );
  }

  if( ioctl( hPort, PPRELEASE ) ) return( ERRPAR_RELEASE );
  return( 0 );
}

@ Prototype.
@<Exported Prototypes@>+=
int par_setstrob( int hPort, int iBitValue );


@ Test Stub. A test stub program |par_setstrobe| is also created.
@(par_setstrobe.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_par.h>
main( int argc, char *argv[] ) {
  int iPortNum, iBitValue, hPort, iErr;
  char *pszErr;

  if( argc < 2 || argc > 3 ) {
    fprintf( stderr, "Usage:  %s {<port_num>} [<bit_value>]\n", argv[ 0 ] );
    exit( 1 );
  }

  iPortNum = ( argc == 3 ) ? atoi( argv[ 1 ] ) : 0;
  iBitValue = ( argc == 3 ) ? atoi( argv[ 2 ] ) : atoi( argv[ 1 ] );

  iBitValue = iBitValue ? 1 : 0;

  if( ( hPort = par_open( iPortNum ) ) < 0 ) {
    pszErr = par_errstr( hPort );
    fprintf( stdout, "Error %d: %s\n", hPort, pszErr );
    free( pszErr );
    fflush( stdout );
    exit( 1 );
  }

  if( ( iErr = par_setstrobe( hPort, iBitValue ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
  }
  else fprintf( stdout, "Stobe control bit set to %d in par port %d\n",
                iBitValue, iPortNum );
  fflush( stdout );
  
  if( ( iErr = par_close( hPort ) ) < 0 ) {
    pszErr = par_errstr( iErr );
    fprintf( stdout, "Error %d: %s\n", iErr, pszErr );
    free( pszErr );
    fflush( stdout );
  }
}


@* The |par_errstr()| function. Returns error string associated with err code.
@<Exported Functions@>+=
char *par_errstr( int iErr ) {
  char *pszErr;

  switch( iErr ) {
    case ERRPAR_GENERAL: @;
      asprintf( &pszErr, "General error" );
      break;
    case ERRPAR_ARG: @;
      asprintf( &pszErr, "Invalid value of the argument" );
      break;
    case ERRPAR_PORTNUM: @;
      asprintf( &pszErr, "Invalid port number" );
      break;
    case ERRPAR_OPEN: @;
      asprintf( &pszErr, "Open port error" );
      break;
    case ERRPAR_CLAIM: @;
      asprintf( &pszErr, "Port claim error. Check modules parport_pc, "
                         "ppdev, and parport" );
      break;
    case ERRPAR_RELEASE: @;
      asprintf( &pszErr, "Port release error" );
      break;
    case ERRPAR_SETDATA: @;
      asprintf( &pszErr, "Error writing data" );
      break;
    case ERRPAR_GETDATA: @;
      asprintf( &pszErr, "Error reading data" );
      break;
    default: @;
      if( iErr >= 0 ) asprintf( &pszErr, "No errors" );
      else asprintf( &pszErr, "Unknown error" );
  }
  return( pszErr );
}

@ Prototype.
@<Exported Prototypes@>+=
char *par_errstr( int iErr );


@*Index.
