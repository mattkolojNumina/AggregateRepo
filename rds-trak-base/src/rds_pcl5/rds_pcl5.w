%
%   rds_pcl5.w
%
%   Author: Alex Korzhuk
%
%   History:
% 04/14/2010 ank: init
% 06/04/2010 ank: added pcl5_stripkyo function and command line utility
% 12/02/2011 ank: added pcl5_strippjl function and command line utility,
%   fixed a bug in pcl5_stripkyo: replaced "i=iStartPos" with "i=iStartPos-1"
% 09/17/2012 ank: added PageSize parameter to all functions and utilities
% 12/27/2013 ank: added pcl5_insert function; modified pcl5_setparam to insert
%   new parameter at 1st found instance of the command rather than last.
%
%
%
%
%
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
%     (C) Copyright 2010 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{RDS3 pcl -- PCL procesing library)
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The RDS3 pcl library is a collection of calls that works with a PCL document.



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
%
\def\botofcontents{\vfill
\centerline{\copyright 2010 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview.
The PCL library provides routines to work with PCL document.

This library is a shared C library and exports the following functions:

\dot |pcl5_info()| gets basic parameters from PCL document and
counts number of pages in PCL document

\dot |pcl5_setparam()| sets parameter value in PCL document.

\dot |pcl5_setduplex()| sets duplex/simplex value in PCL document.

\dot |pcl5_setportrait()| sets portrait/landscape value in PCL document.

\dot |pcl5_setinputbin()| sets input bin in PCL document.

\dot |pcl5_setoutputbin()| sets output bin in PCL document.

\dot |pcl5_duplex2str()| converts duplex/simplex integer value to string

\dot |pcl5_portrait2str()| converts portrait/landscape integer value to string

\dot |pcl5_inputbin2str()| converts input bin integer value to string

\dot |pcl5_outputbin2str()| converts output bin integer value to string

\dot |pcl5_pagesize2str()| converts page size integer value to string



@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;



@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>
#include <ctype.h>
#include <libgen.h>

#include <rds_trn.h>
#include "rds_pcl5.h"



@ Header. The exported prototypes and error codes are in an external header
@(rds_pcl5.h@>=
#ifndef __RDS_PCL5_H
#define __RDS_PCL5_H
  @<Exported Defines@>@;
  @<Exported Prototypes@>@;
#endif



@ Useful definitions.
@<Defines@>+=




@* Data Structures and definitions.

@ Exported definitions.
Some usuful error numbers are defined here.
@<Exported Defines@>+=
#define ERRPCL5_NOERRORS    ( 0 )
#define ERRPCL5_GENERAL     (-1 )

/* max number of bytes that PCL5 buffer should allow above length */
/* of the PCL5 document when pcl5_setparam() function is called */
#define PCL5_MAX_OVERHEAD   ( 16 ) 

#define PCL5_NOT_DEFINED   ( 99999 )   /* Parameter not defined */
#define PCL5_SIMPLEX           ( 0 )   /* Single side (Simplex) */
#define PCL5_DUPLEX_LONGEDGE   ( 1 )   /* Duplex, long-edge binding */
#define PCL5_DUPLEX_SHORTEDGE  ( 2 )   /* Duplex, short-edge binding */

#define PCL5_PORTRAIT          ( 0 )   /* Portrait */
#define PCL5_LANDSCAPE         ( 1 )   /* Landscape */
#define PCL5_REV_PORTRAIT      ( 2 )   /* Reverse Portrait */
#define PCL5_REV_LANDSCAPE     ( 3 )   /* Reverse Landscape */



@ General non-exported definitions
@<Defines@>+=
#ifndef FALSE
#define FALSE (0)
#endif
#ifndef TRUE
#define TRUE  (!FALSE)
#endif

#ifndef EOS
#define EOS '\0'
#endif
#ifndef CR
#define CR 0x0D
#endif
#ifndef LF
#define LF 0x0A
#endif
#ifndef STX
#define STX 0x02
#endif
#ifndef ETX
#define ETX 0x03
#endif
#ifndef SOH
#define SOH 0x01
#endif
#ifndef EOT
#define EOT 0x04
#endif

#ifndef FF
#define FF 0x0C
#endif

#ifndef ESC
#define ESC 0x1B
#endif



#ifndef min
#define min(a,b) ( ((a) < (b)) ? (a) : (b) )
#endif
#ifndef max
#define max(a,b) ( ((a) > (b)) ? (a) : (b) )
#endif



@ Global variables
@<Globals@>+=


@ The |pcl5_info| function. Counts number of pages in the PCL document,
and gets basic parameters from PCL document |pData| of length |iDataLen|.

NULL pointer is permitted for arguments |piPages|, |piCopies|,
|piSimplexStatus|, |piPortraitStatus|, |piInputBin|, and |piOutputBin|.

Total number of pages to be printed (0...) is returned in |*piPages|.
Number of copies (1...) is returned in |*piCopies|.

Valid values of the variable |*piDuplexStatus| are (per PCL5 spec):
PCL5_NOT_DEFINED   ( 99999 ) - parameter not found in the PCL document 
PCL5_SIMPLEX           ( 0 ) - Single side (Simplex)
PCL5_DUPLEX_LONGEDGE   ( 1 ) - Duplex, long-edge binding
PCL5_DUPLEX_SHORTEDGE  ( 2 ) - Duplex, short-edge binding

Valid values of the variable |*piPortraitStatus| are (per PCL5 spec):
PCL5_NOT_DEFINED   ( 99999 ) - parameter not found in the PCL document 
PCL5_PORTRAIT          ( 0 ) - Portrait
PCL5_LANDSCAPE         ( 1 ) - Landscape
PCL5_REV_PORTRAIT      ( 2 ) - Reverse Portrait
PCL5_REV_LANDSCAPE     ( 3 ) - Reverse Landscape

Valid values of the variable |*piInputBin| are (per PCL5 spec):
PCL5_NOT_DEFINED   ( 99999 ) - parameter not found in the PCL document 
0 - Print current page (paper source remains unchanged)
1 - Feed paper from main paper source
2 - Feed paper from manual input
3 - Feed envelope from manual input
4 - Feed paper from alternate paper source
5 - Feed from optional large paper source
6 - Feed envelope from envelope feeder *
7 - Autoselect
8 - Feed paper from Tray 1 (right side tray)
20...39 - High Capacity Input (HCI) Trays 2-21

Valid values of the variable |*piOutputBin| are (per PCL5 spec):
PCL5_NOT_DEFINED   ( 99999 ) - parameter not found in the PCL document 
0 - Automatic selection
1 - Upper Output Bin (for the LaserJet 5Si,printer top/face-down bin—bin #1)
2 - Rear Output Bin (for the LaserJet 5Si, printer left/face-up bin—bin #2)
3 - Selects Bin #3 (HCO face-up bin)
4 - Selects Bin #4 (HCO #1 face-down bin)
5 - Selects Bin #5 (HCO #2 face-down bin)
6 - Selects Bin #6 (HCO #3 face-down bin)
7 - Selects Bin #7 (HCO #4 face-down bin)
8 - Selects Bin #8 (HCO #5 face-down bin)
9 - Selects Bin #9 (HCO #6 face-down bin)
10 - Selects Bin #10 (HCO #7 face-down bin)
11 - Selects Bin #11 (HCO #8 face-down bin)

Valid values of the variable |*piPaperSize| are (per PCL5 spec):
PCL5_NOT_DEFINED   ( 99999 ) - parameter not found in the PCL document 
1 - Executive (7.25" x 10.5")
2 - Letter (8.5" x 11")
3 - Legal (8.5" x 14")
6 - Ledger (11" x 17")
25 - A5 paper (148mm x 210mm)
26 - A4 paper (210mm x 297mm)
27 - A3 (297mm x 420mm)
45 - JIS B5 paper (182mm x 257mm)
46 - JIS B4 paper (250mm x 354mm)
71 - Hagaki postcard (100mm x 148mm)
72 - Oufuku-Hagaki postcard (200mm x 148mm)
80 - Monarch Envelope (3 7/8" x 7 1/2")
81 - Commercial Envelope 10 (4 1/8" x 9 1/2")
90 - International DL (110mm x 220mm)
91 - International C5 (162mm x 229mm)
100 - International B5 (176mm x 250mm)
101 - Custom (size varies with printer)

The function parses PCL commands per PCL5 spec in the following format:
<ESC> X y ### z1 ... ### zi ... ### Zn [data]

where <ESC> is an ASCII character 27 (dec);

X - parameterized character, an ASCII character from 33 to 47 (dec),
"!" through "/", indicating that the escape suequence in parameterized;

y - group character, an ASCII character from 96 to 126 (dec),
"`" through "~", that specifies the group type of control being performed;

### - value field, numberic value (withing a range from -32767 to 65535)
represented as ASCII charactres "0" through "9" and may be preceded by "+"
or "-" sign, and may contain a fractional portion indicated by digits after
a decimal point ".";

zi - parameter character, an ASCII character from 96 to 126 (dec),
"`" through "~", that specifies the parameter to which the previous value field
applies; this character is used when combining escape sequences;

Zn - termination character - an ASCII character from 64 to 94 (dec),
"@" through "^", that specifies the parameter to which the previous value field
applies; this characters terminates the escape sequence

[data] - optional 8-bit binary data, the number of bytes is specified by value
field of the escape sequence, the binary data immediatelly follows
the terminating character of the escape sequence.   

The function returns 1 on success and 0 on error. 
@<Exported Functions@>+=
int pcl5_info( const char *pData, int iDataLen, int *piPages, int *piCopies,
              int *piDuplexStatus, int *piPortraitStatus, int *piInputBin,
              int *piOutputBin, int *piPaperSize ) {
  int i, iValue, iStoredValue;
  char chEnd, *pEnd;

  int iPages = 0;
  int iCopies = 1;
  int iDuplexStatus = PCL5_NOT_DEFINED;
  int iPortraitStatus = PCL5_NOT_DEFINED;
  int iInputBin = PCL5_NOT_DEFINED;
  int iOutputBin = PCL5_NOT_DEFINED;
  int iPaperSize = PCL5_NOT_DEFINED;

  for( i = 0; i < iDataLen; i++ ) {
    if( pData[ i ] == FF ) iPages++;
    else if( pData[ i ] != ESC ) continue;
    i++;
    //===================================================================
    // Get Number of copies, PCL command: <ESC>&l#...#X
    // or get Simplex/Duplex value, PCL command: <ESC>&l#...#S
    // or get Portrait/Landscape value, PCL command: <ESC>&l#...#O
    // or get input bin (if defined), PCL command: <ESC>&l#...#H,
    // or get selected output bin (if defined), PCL command: <ESC>&l#...#G
    // or get paper size (if defined), PCL command: <ESC>&l#...#A
    //===================================================================
    if( memcmp( &pData[ i ], "&l", 2 ) == 0 ) {
      for( i += 2; ; i++ ) {
        iValue = strtol( &pData[ i ], &pEnd, 10 ); /* numeric value of param */
        i += pEnd - &pData[ i ];     /* suffix character is at this position */

        if( pData[ i ] == 'X' || pData[ i ] == 'X' + 32 ) iCopies = iValue;
        else if( pData[ i ] == 'S' || pData[ i ] == 'S' + 32 )
          iDuplexStatus = iValue;
        else if( pData[ i ] == 'O' || pData[ i ] == 'O' + 32 )
          iPortraitStatus = iValue;
        else if( pData[ i ] == 'H' || pData[ i ] == 'H' + 32 )
          iInputBin = iValue;
        else if( pData[ i ] == 'G' || pData[ i ] == 'G' + 32 )
          iOutputBin = iValue;
        else if( pData[ i ] == 'A' || pData[ i ] == 'A' + 32 )
          iPaperSize = iValue;

        /* we will continue the loop only if char is non-terminating suffix */
        if( pData[ i ] < 96 || pData[ i ] > 126 ) break;
      }
    }
    //===================================================================
    // Skipping binary blocks defined by the following PCL commands:
    //   <ESC>*b#...#W - block of raster data
    //   <ESC>*c#...#W - block of patern data
    //   <ESC>*v#...#W - block of color raster data
    //   <ESC>*m#...#W - block of dither matrix data
    //   <ESC>*l#...#W - block of lookup tables
    //   <ESC>*i#...#W - block of viewing illuminant data
    //   <ESC>(s#...#W - block of character data
    //   <ESC>)s#...#W - block of font data
    //   <ESC>(f#...#W - block of symbol set definition data
    //   <ESC>&b#...#W - block of AppleTalk I/O configuration data
    //   <ESC>&n#...#W - block of string IDs for font, macros, ans media types
    //   <ESC>&p#...#X - block of transparent print data
    //===================================================================
    else if( memcmp( &pData[ i ], "*b", 2 ) == 0 ||
             memcmp( &pData[ i ], "*c", 2 ) == 0 ||
             memcmp( &pData[ i ], "*v", 2 ) == 0 ||
             memcmp( &pData[ i ], "*m", 2 ) == 0 ||
             memcmp( &pData[ i ], "*l", 2 ) == 0 ||
             memcmp( &pData[ i ], "*i", 2 ) == 0 ||
             memcmp( &pData[ i ], "(s", 2 ) == 0 ||
             memcmp( &pData[ i ], ")s", 2 ) == 0 ||
             memcmp( &pData[ i ], "(f", 2 ) == 0 ||
             memcmp( &pData[ i ], "&b", 2 ) == 0 ||
             memcmp( &pData[ i ], "&n", 2 ) == 0 ||
             memcmp( &pData[ i ], "&p", 2 ) == 0 ) {
      chEnd = memcmp( &pData[ i ], "&p", 2 ) ? 'W' : 'X';
        
      for( i += 2, iStoredValue = 0; ; i++ ) {
        iValue = strtol( &pData[ i ], &pEnd, 10 ); /* numeric value of param */
        i += pEnd - &pData[ i ];     /* suffix character is at this position */

        /* known terminating suffix of PCL cmd (usually upper case char) */
        if( pData[ i ] == chEnd ) {
          i += iValue;                                /* skip binary block */
          break;
        }

        /* known non-terminating suffix of PCL cmd (usually lower case char) */
        else if( pData[ i ] == chEnd + 32 ) iStoredValue = iValue;

        /* non-terminating suffix of PCL cmd (usually lower case char) */
        else if( pData[ i ] >= 96 && pData[ i ] <= 126 ) ;

        /* terminating suffix of PCL cmd (usually upper case char) */
        else if( pData[ i ] >= 64 && pData[ i ] <= 94 ) {
          if( iStoredValue != 0 ) i += iStoredValue;  /* skip binary block */
          break;
        }

        /* not a suffix of PCL cmd - exit cmd processing loop */
        else break;
      }
    } 
  }

  if( piPages != NULL ) *piPages = iPages * iCopies;
  if( piCopies != NULL ) *piCopies = iCopies;
  if( piDuplexStatus != NULL ) *piDuplexStatus = iDuplexStatus;
  if( piPortraitStatus != NULL ) *piPortraitStatus = iPortraitStatus;
  if( piInputBin != NULL ) *piInputBin = iInputBin;
  if( piOutputBin != NULL ) *piOutputBin = iOutputBin;
  if( piPaperSize != NULL ) *piPaperSize = iPaperSize;
 
  return( 1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_info( const char *pData, int iDataLen, int *piPages, int *piCopies,
              int *piDuplexStatus, int *piPortraitStatus, int *piInputBin,
              int *piOutputBin, int *piPaperSize );


@ Test Stub.  A test stub program |pcl5_info| is also created.
@(pcl5_info.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <rds_trn.h>
#include "rds_pcl5.h"
@<Application Functions@>@;
int main( int argc, char *argv[] ) {
  int iFileSize, iReadCnt, iPages, iCopies, iDuplexValue, iPortraitValue;
  int iInputBin, iOutputBin, iPaperSize;
  char szFileName[ 256 ], *pData;

  if( argc != 2 ) {
    fprintf( stdout, "Usage:  %s <pcl file>\n",argv[0]);
    fflush( stdout );
    return( 1 );
  }
  trn_register( "pcl5_info" );

  strncpy( szFileName, argv[ 1 ], sizeof( szFileName ) - 1 );
  szFileName[ sizeof( szFileName ) - 1 ] = '\0';

  if( ( iFileSize = GetFileSize( szFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szFileName );
    fflush( stdout );
    return( 1 );
  }
  if( ( pData = calloc( iFileSize, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d bytes buffer for file [%s]\n",
             iFileSize, szFileName );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iFileSize, szFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iFileSize, szFileName );

  if( ( iReadCnt = ReadBinFile( szFileName, pData, iFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iFileSize, szFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  else if( iReadCnt != iFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iFileSize, szFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
 
  if( pcl5_info( pData, iFileSize, &iPages, &iCopies, &iDuplexValue,
                &iPortraitValue, &iInputBin, &iOutputBin, &iPaperSize ) <= 0 ) {
    fprintf( stdout, "Error getting basic info from %d-byte PCL file [%s]\n",
             iFileSize, szFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  
  fprintf( stdout, "%d-byte PCL file[%s] has the following basic parameters:\n",
           iFileSize, szFileName );
  fflush( stdout );

  fprintf( stdout, "  Total number of pages: %d\n", iPages );
  fflush( stdout );
 
  fprintf( stdout, "  Number of copies: %d\n", iCopies );
  fflush( stdout );

  fprintf( stdout, "  DuplexValue:%d (%s)\n",
           iDuplexValue, pcl5_duplex2str( iDuplexValue ) ); 
  fflush( stdout );
  
  fprintf( stdout, "  PortraitValue:%d (%s)\n",
           iPortraitValue, pcl5_portrait2str( iPortraitValue ) ); 
  fflush( stdout );
  
  fprintf( stdout, "  InputBin:%d (%s)\n",
           iInputBin, pcl5_inputbin2str( iInputBin ) ); 
  fflush( stdout );
  
  fprintf( stdout, "  OutputBin:%d (%s)\n",
           iOutputBin, pcl5_outputbin2str( iOutputBin ) ); 
  fflush( stdout );

  fprintf( stdout, "  PaperSize:%d (%s)\n",
           iPaperSize, pcl5_papersize2str( iPaperSize ) ); 
  fflush( stdout );

  free( pData );
}



@ The |pcl5_duplex2str| function. Converts an integer value of the
duplex value into human-readable string.
Returns pointer to a static string.
@<Exported Functions@>+=
const char *pcl5_duplex2str( int iValue ) {
  static char szValue[ 128 ];

  if( iValue == PCL5_NOT_DEFINED ) strcpy( szValue, "not defined" );
  else if( iValue == PCL5_SIMPLEX ) strcpy( szValue, "single side - simplex" );
  else if( iValue == PCL5_DUPLEX_LONGEDGE )
    strcpy( szValue, "duplex, long-edge binding" );
  else if( iValue == PCL5_DUPLEX_SHORTEDGE )
    strcpy( szValue, "duplex, short-edge binding" );
  else sprintf( szValue, "%d - unknown value", iValue ); 

  return( szValue );
}
@ Prototype.
@<Exported Prototypes@>+=
const char *pcl5_duplex2str( int iValue );



@ The |pcl5_portrait2str| function. Converts an integer value of the
portrait value into human-readable string.
Returns pointer to a static string.
@<Exported Functions@>+=
const char *pcl5_portrait2str( int iValue ) {
  static char szValue[ 128 ];

  if( iValue == PCL5_NOT_DEFINED ) strcpy( szValue, "not defined" );
  else if( iValue == PCL5_PORTRAIT ) strcpy( szValue, "portrait" );
  else if( iValue == PCL5_LANDSCAPE ) strcpy( szValue, "landscape" );
  else if( iValue == PCL5_REV_PORTRAIT ) strcpy( szValue, "reverse portrait" );
  else if( iValue == PCL5_REV_LANDSCAPE ) strcpy( szValue, "reverse landscape" );
  else sprintf( szValue, "%d - unknown value", iValue ); 

  return( szValue );
}
@ Prototype.
@<Exported Prototypes@>+=
const char *pcl5_portrait2str( int iValue );



@ The |pcl5_inputbin2str| function. Converts an integer value of the
input bin PCL5 parameter into human-readable string.
Returns pointer to a static string.
@<Exported Functions@>+=
const char *pcl5_inputbin2str( int iValue ) {
  static char szValue[ 128 ];

  if( iValue == PCL5_NOT_DEFINED ) strcpy( szValue, "not defined" );
  else if( iValue == 0 )
    strcpy( szValue, "print current page: paper source remains unchanged" );
  else if( iValue == 1 ) strcpy( szValue, "feed paper from main paper source" );
  else if( iValue == 2 ) strcpy( szValue, "feed paper from manual input" );
  else if( iValue == 3 ) strcpy( szValue, "feed envelope from manual input" );
  else if( iValue == 4 )
    strcpy( szValue, "feed paper from alternate paper source" );
  else if( iValue == 5 )
    strcpy( szValue, "feed from optional large paper source" );
  else if( iValue == 6 ) strcpy( szValue, "feed envelope from envelope feeder");
  else if( iValue == 7 ) strcpy( szValue, "autoselect" );
  else if( iValue == 8 )
    strcpy( szValue, "feed paper from tray 1: right side tray" );
  else if( iValue >= 20 && iValue <= 39 )
    sprintf( szValue, "high capasity input tray %d", iValue - 18 ); 
  else sprintf( szValue, "%d - unknown value", iValue ); 

  return( szValue );
}
@ Prototype.
@<Exported Prototypes@>+=
const char *pcl5_inputbin2str( int iValue );



@ The |pcl5_outputbin2str| function. Converts an integer value of the
output bin PCL5 parameter into human-readable string.
Returns pointer to a static string.
@<Exported Functions@>+=
const char *pcl5_outputbin2str( int iValue ) {
  static char szValue[ 128 ];

  if( iValue == PCL5_NOT_DEFINED ) strcpy( szValue, "not defined" );
  else if( iValue == 0 ) strcpy( szValue, "automatic selection" );
  else if( iValue == 1 ) strcpy( szValue, "upper output bin" );
  else if( iValue == 2 ) strcpy( szValue, "rear output bin" );
  else if( iValue == 3 )
    strcpy( szValue, "output bin 3: high capacity output face-up bin" );
  else if( iValue >= 4 && iValue <= 11 )
    sprintf( szValue,"output bin %d: high capacity output face-down bin %d",
             iValue, iValue - 3 ); 
  else sprintf( szValue, "%d - unknown value", iValue ); 

  return( szValue );
}
@ Prototype.
@<Exported Prototypes@>+=
const char *pcl5_outputbin2str( int iValue );



@ The |pcl5_papersize2str| function. Converts an integer value of the
paper size PCL5 parameter into human-readable string.
Returns pointer to a static string.
@<Exported Functions@>+=
const char *pcl5_papersize2str( int iValue ) {
  static char szValue[ 128 ];

  if( iValue == PCL5_NOT_DEFINED ) strcpy( szValue, "not defined" );
  else if( iValue == 1 ) strcpy( szValue, "Executive (7.25\" x 10.5\")" );
  else if( iValue == 2 ) strcpy( szValue, "Letter (8.5\" x 11\")" );
  else if( iValue == 3 ) strcpy( szValue, "Legal (8.5\" x 14\")" );
  else if( iValue == 6 ) strcpy( szValue, "Ledger (11\" x 17\")" );
  else if( iValue == 25 ) strcpy( szValue, "A5 paper (148mm x 210mm)" );
  else if( iValue == 26 ) strcpy( szValue, "A4 paper (210mm x 297mm)" );
  else if( iValue == 27 ) strcpy( szValue, "A3 (297mm x 420mm)" );
  else if( iValue == 45 ) strcpy( szValue, "JIS B5 paper (182mm x 257mm)" );
  else if( iValue == 46 ) strcpy( szValue, "JIS B4 paper (250mm x 354mm)" );
  else if( iValue == 71 ) strcpy( szValue, "Hagaki postcard (100mm x 148mm)" );
  else if( iValue == 72 ) strcpy( szValue,
                                  "Oufuku-Hagaki postcard (200mm x 148mm)" );
  else if( iValue == 80 ) strcpy( szValue,
                                  "Monarch Envelope (3 7/8\" x 7 1/2\")" );
  else if( iValue == 81 ) strcpy( szValue,
                                  "Commercial Envelope 10 (4 1/8\" x 9 1/2\")");
  else if( iValue == 90 ) strcpy( szValue, "International DL (110mm x 220mm)" );
  else if( iValue == 91 ) strcpy( szValue, "International C5 (162mm x 229mm)" );
  else if( iValue == 100 ) strcpy( szValue, "International B5 (176mm x 250mm)");
  else if( iValue == 101 ) strcpy( szValue,"Custom (size varies with printer)");
  else sprintf( szValue, "%d - unknown value", iValue ); 

  return( szValue );
}
@ Prototype.
@<Exported Prototypes@>+=
const char *pcl5_papersize2str( int iValue );



@ The |pcl5_setparam| function. Set a PCL5 parameter in the buffer of the PCL5
document.

|pData| - is a ptr to the buffer with PCL document,

|iDataLen| - is a length of the PCL document in the buffer.
Size of the buffer should allow overhead above |iDataLen| to fit new PCL command
in case if it is needed to be inserted into PCL document (rather than updated).
Conservatively, the overhead should not exceed 16 bytes and it is specified in
|PCL5_MAX_OVERHEAD| constant.

ATTENTION: Caller application should add the value of |PCL5_MAX_OVERHEAD| to
the length of the PCL document when memory is allocated for the buffer to hold
the PCL document.

Per PCL5 Spec the PCL command, a.k.a parameterized escape sequence has the
following format: <esc>Xy###z1...###zi...###Zn

|chCmd1| is the first character of the PCL command after the escape character,
(reffered as X in format above), a.k.a parameterized character in PCL5 spec.
Should be within ASCII range of 33...47 (dec) - from "!" to "/".

|chCmd2| is the second character of the PCL command (reffered as y in format
above), a.k.a group character in PCL5 spec. Should be within ASCII range of
96...126 (dec) - from "`" to "~".

|chCmdSuffix| - is a terminating suffix of the PCL command (reffered as Zn in
format above). Should be within ASCII range of 64...94 (dec) - from "@" to "^".
The function will convert terminating suffix Zn to non-terminating suffix zi
by adding 32 to ASCII code, if it is neccessary.

|iParamValue| is a valid numeric field value of the PCL command (from -32767 to
65535 per PCL5 spec). There is a special value
PCL5_NOT_DEFINED (-1 ) - parameter not defined (will remove esc sequence)

Returns new length of the PCL document in buffer (>0) on success, or (-1) on error.
Max new length of the document may exceed the value of |iDataLen| by the value of
the overhead (which is conservatively should not exceed 16 bytes).
@<Exported Functions@>+=
int pcl5_setparam( char *pData, int iDataLen, char chCmd1, char chCmd2,
                   char chCmdSuffix, int iParamValue ) {
  int i, iValue, iStoredValue, iParamValueWidth, iValueWidth, iPos, iPages = 0;
  int iFirstCmdPos = 0, iLastCmdPos = 0, iFirstStarPCmdPos = 0;
  int iLastCmdPos1stPage = 0;
  char szCmd[ 3 ], szParamValue[ 16 ], chEnd, *pEnd, chPrevSuffix;

  if( chCmd1 < 33 || chCmd1 > 47 ) {
    Alert( "Error in pcl5_setparam(%c[%02Xh],%c[%02Xh],%c[%02Xh],%d): "
           "invalid command character 1", chCmd1, chCmd1, chCmd2, chCmd2,
           chCmdSuffix, chCmdSuffix, iParamValue );
    return( -1 );
  }
  if( chCmd2 < 96 || chCmd1 > 126 ) {
    Alert( "Error in pcl5_setparam(%c[%02Xh],%c[%02Xh],%c[%02Xh],%d): "
           "invalid command character 2", chCmd1, chCmd1, chCmd2, chCmd2,
           chCmdSuffix, chCmdSuffix, iParamValue );
    return( -1 );
  }
  if( chCmd2 < 64 || chCmd1 > 94 ) {
      Alert( "Error in pcl5_setparam(%c[%02Xh],%c[%02Xh],%c[%02Xh],%d): "
           "invalid command suffix character", chCmd1, chCmd1, chCmd2, chCmd2,
           chCmdSuffix, chCmdSuffix, iParamValue );
    return( -1 );
  }
  if( ( iParamValue < -32767 || iParamValue > 65535 ) &&
      iParamValue != PCL5_NOT_DEFINED ) {
    Alert( "Error in pcl5_setparam(%c[%02Xh],%c[%02Xh],%c[%02Xh],%d): "
           "invalid parameter value", chCmd1, chCmd1, chCmd2, chCmd2,
           chCmdSuffix, chCmdSuffix, iParamValue );
    return( -1 );
  }

  szCmd[ 0 ] = chCmd1;
  szCmd[ 1 ] = chCmd2;
  szCmd[ 2 ] = '\0';
  iParamValueWidth = snprintf( szParamValue, sizeof( szParamValue ) - 1,
                               "%d", iParamValue );

  for( i = 0; i < iDataLen; i++ ) {
    if( pData[ i ] == FF ) iPages++;
    else if( pData[ i ] != ESC ) continue;
    i++;
    //===================================================================
    // Is it a command we are looking for?
    //===================================================================
    if( memcmp( &pData[ i ], szCmd, 2 ) == 0 ) {
      iLastCmdPos = i + 2;  /* 1st numeric field of last cmd at this position */
      if( iFirstCmdPos <= 0 ) iFirstCmdPos = iLastCmdPos; /* save 1st cmd pos */
      if( iPages == 0 ) iLastCmdPos1stPage = iLastCmdPos; /* pos on 1st page */

      for( i += 2; ; i++ ) {
        iPos = i;           /* numeric field starts at this postion */
        chPrevSuffix = '\0';

        iValue = strtol( &pData[ i ], &pEnd, 10 ); /* numeric value of param */
        iValueWidth = pEnd - &pData[ i ];
        i += iValueWidth;   /* suffix character is at this position */

        //===================================================================
        // suffix char is a terminating suffix that we are looking for
        //===================================================================
        if( pData[ i ] == chCmdSuffix ) {
          if( iParamValue != PCL5_NOT_DEFINED ) {
            //=== replace a value in part ###Zn of <esc>Xy###z1...###zi...###Zn
            if( iValueWidth != iParamValueWidth )
              memmove( &pData[ i + iParamValueWidth - iValueWidth ],
                       &pData[ i ], iDataLen - i );
            memmove( &pData[ iPos ], szParamValue, iParamValueWidth );
            return( iDataLen + iParamValueWidth - iValueWidth );
          }
          else if( chPrevSuffix != '\0' ) {
            //=== remove ###Zn part from <esc>Xy###z1...###zi...###Zn cmd
            pData[ iPos - 1 ] = chPrevSuffix - 32; /* non-term to term sfx */
            memmove( &pData[ iPos ], &pData[ i + 1 ], iDataLen - ( i + 1 ) );
            return( iDataLen + iPos - ( i + 1 ) );
          }
          else {
            //=== remove <esc>Xy###Zn command completelly
            memmove( &pData[ iPos - 3 ], &pData[ i + 1 ], iDataLen - (i+1) );
            return( iDataLen + ( iPos - 3 ) - ( i + 1 ) );
          }
        }
        //===================================================================
        // suffix char is a non-terminating suffix that we are looking for
        //===================================================================
        else if( pData[ i ] == chCmdSuffix + 32 ) {
          if( iParamValue != PCL5_NOT_DEFINED ) {
            //=== replace a value in part ###zi of <esc>Xy###z1...###zi...###Zn
            if( iValueWidth != iParamValueWidth )
              memmove( &pData[ i + iParamValueWidth - iValueWidth ],
                       &pData[ i ], iDataLen - i );
            memmove( &pData[ iPos ], szParamValue, iParamValueWidth );
            return( iDataLen + iParamValueWidth - iValueWidth );
          }
          else {
            //=== remove ###zi part from <esc>Xy###z1...###zi...###Zn cmd
            memmove( &pData[ iPos ], &pData[ i + 1 ], iDataLen - ( i + 1 ) );
            return( iDataLen + iPos - ( i + 1 ) );
          }
        }
        //===================================================================
        // suffix is not the one we are looking for
        //===================================================================
        else chPrevSuffix = pData[ i ];
            
        /* we will continue the loop only if char is non-terminating suffix */
        if( pData[ i ] < 96 || pData[ i ] > 126 ) break;
      }
    }
    //===================================================================
    // Skipping binary blocks defined by the following PCL commands:
    //   <ESC>*b#...#W - block of raster data
    //   <ESC>*c#...#W - block of patern data
    //   <ESC>*v#...#W - block of color raster data
    //   <ESC>*m#...#W - block of dither matrix data
    //   <ESC>*l#...#W - block of lookup tables
    //   <ESC>*i#...#W - block of viewing illuminant data
    //   <ESC>(s#...#W - block of character data
    //   <ESC>)s#...#W - block of font data
    //   <ESC>(f#...#W - block of symbol set definition data
    //   <ESC>&b#...#W - block of AppleTalk I/O configuration data
    //   <ESC>&n#...#W - block of string IDs for font, macros, ans media types
    //   <ESC>&p#...#X - block of transparent print data
    //===================================================================
    else if( memcmp( &pData[ i ], "*b", 2 ) == 0 ||
             memcmp( &pData[ i ], "*c", 2 ) == 0 ||
             memcmp( &pData[ i ], "*v", 2 ) == 0 ||
             memcmp( &pData[ i ], "*m", 2 ) == 0 ||
             memcmp( &pData[ i ], "*l", 2 ) == 0 ||
             memcmp( &pData[ i ], "*i", 2 ) == 0 ||
             memcmp( &pData[ i ], "(s", 2 ) == 0 ||
             memcmp( &pData[ i ], ")s", 2 ) == 0 ||
             memcmp( &pData[ i ], "(f", 2 ) == 0 ||
             memcmp( &pData[ i ], "&b", 2 ) == 0 ||
             memcmp( &pData[ i ], "&n", 2 ) == 0 ||
             memcmp( &pData[ i ], "&p", 2 ) == 0 ) {
      chEnd = memcmp( &pData[ i ], "&p", 2 ) ? 'W' : 'X';
        
      for( i += 2, iStoredValue = 0; ; i++ ) {
        iValue = strtol( &pData[ i ], &pEnd, 10 ); /* numeric value of param */
        i += pEnd - &pData[ i ];

        /* known terminating suffix of PCL cmd (upper case char) */
        if( pData[ i ] == chEnd ) {
          i += iValue;                                /* skip binary block */
          break;
        }

        /* known non-terminating suffix of PCL cmd (lower case char) */
        else if( pData[ i ] == chEnd + 32 ) iStoredValue = iValue;

        /* non-terminating suffix of PCL cmd (usually lower case char) */
        else if( pData[ i ] >= 96 && pData[ i ] <= 126 ) ;

        /* terminating suffix of PCL cmd (usually upper case char) */
        else if( pData[ i ] >= 64 && pData[ i ] <= 94 ) {
          if( iStoredValue != 0 ) i += iStoredValue;  /* skip binary block */
          break;
        }

        /* not a suffix of PCL cmd - exit cmd processing loop */
        else break;
      }
    }
    //=======================================================================
    // Find a position of the 1st numeric value field of the 1st [*p] command
    //=======================================================================
    else if( memcmp( &pData[ i ], "*p", 2 ) == 0 ) {
      if( iFirstStarPCmdPos == 0 ) iFirstStarPCmdPos = i + 2; 
    }
  }
  
  if( iParamValue == PCL5_NOT_DEFINED ) return( iDataLen );

  ////=== if cmd found, but suffix not found: add ###z1 as 1st param of last cmd
  //if( iLastCmdPos > 0 ) {
  //  memmove( &pData[ iLastCmdPos + iParamValueWidth + 1 ],
  //           &pData[ iLastCmdPos ], iDataLen - iLastCmdPos );
  //  memmove( &pData[ iLastCmdPos ], szParamValue, iParamValueWidth );
  //  pData[ iLastCmdPos + iParamValueWidth ] = chCmdSuffix + 32;
  //  return( iDataLen + iParamValueWidth + 1 );
  //}

  ////=== if cmd found, but suffix not found: add ###z1 as 1st param of 1st cmd
  //if( iFirstCmdPos > 0 ) {
  //  memmove( &pData[ iFirstCmdPos + iParamValueWidth + 1 ],
  //           &pData[ iFirstCmdPos ], iDataLen - iFirstCmdPos );
  //  memmove( &pData[ iFirstCmdPos ], szParamValue, iParamValueWidth );
  //  pData[ iFirstCmdPos + iParamValueWidth ] = chCmdSuffix + 32;
  //  return( iDataLen + iParamValueWidth + 1 );
  //}

  //=== if cmd found, but suffix is not: add ###z1 as 1st param of last cmd
  //=== on the first page
  if( iLastCmdPos1stPage > 0 ) {
    memmove( &pData[ iLastCmdPos1stPage + iParamValueWidth + 1 ],
             &pData[ iLastCmdPos1stPage ], iDataLen - iLastCmdPos1stPage );
    memmove( &pData[ iLastCmdPos1stPage ], szParamValue, iParamValueWidth );
    pData[ iLastCmdPos1stPage + iParamValueWidth ] = chCmdSuffix + 32;
    return( iDataLen + iParamValueWidth + 1 );
  }
  //=== if cmd not found: add <esc>Xy###Zn before first <esc>*p... cmd
  else if( iFirstStarPCmdPos > 0 ) {
    iPos = iFirstStarPCmdPos - 3; /* position at <esc>*p... */
    memmove( &pData[ iPos + 4 + iParamValueWidth ], &pData[ iPos ],
             iDataLen - iPos );
    pData[ iPos ] = 27;
    memmove( &pData[ iPos + 1 ], szCmd, 2 );
    memmove( &pData[ iPos + 3 ], szParamValue, iParamValueWidth );
    pData[ iPos + 3 + iParamValueWidth ] = chCmdSuffix;
    return( iDataLen + 4 + iParamValueWidth );
  }

  //=== if cmd not found, and <esc>*p... cmd not found (unusual/error PCL doc):
  //=== do not know where to insert <esc>Xy###Zn cmd, return an error
  return( -1 );
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setparam( char *pData, int iDataLen, char chCmd1, char chCmd2,
                   char chCmdSuffix, int iParamValue );



@ The |pcl5_setduplex| function. Sets a duplex value in the buffer of
the PCL5 document.
Valid values are:
PCL5_NOT_DEFINED  ( 99999 ) - Parameter not defined (will remove esc sequence)
PCL5_SIMPLEX          ( 0 ) - Single side - Simplex
PCL5_DUPLEX_LONGEDGE  ( 1 ) - Duplex, long-edge binding
PCL5_DUPLEX_SHORTEDGE ( 2 ) - Duplex, short-edge binding
Returns new length of the PCL document in buffer (>0) on success, or (-1) on error.
@<Exported Functions@>+=
int pcl5_setduplex( char *pData, int iDataLen, int iValue ) {
  switch( iValue ) {
    case PCL5_NOT_DEFINED: break;
    case PCL5_SIMPLEX: break;
    case PCL5_DUPLEX_LONGEDGE: break;
    case PCL5_DUPLEX_SHORTEDGE: break;
    default: return( -1 );
  }
  return( pcl5_setparam( pData, iDataLen, '&', 'l', 'S', iValue ) ); 
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setduplex( char *pData, int iDataLen, int iValue );



@ The |pcl5_setportrait| function. Sets a portrait value in the buffer of
the PCL5 document.
Valid values are:
PCL5_NOT_DEFINED  ( 99999 ) - Parameter not defined (will remove esc sequence) 
PCL5_PORTRAIT         ( 0 ) - Portrait
PCL5_LANDSCAPE        ( 1 ) - Landscape
PCL5_REV_PORTRAIT     ( 2 ) - Reverse Portrait
PCL5_REV_LANDSCAPE    ( 3 ) - Reverse Landscape
Returns new length of the PCL5 document in buffer (>0) on success, or (-1) on
error.
@<Exported Functions@>+=
int pcl5_setportrait( char *pData, int iDataLen, int iValue ) {
  switch( iValue ) {
    case PCL5_NOT_DEFINED: break;
    case PCL5_PORTRAIT: break;
    case PCL5_LANDSCAPE: break;
    case PCL5_REV_PORTRAIT: break;
    case PCL5_REV_LANDSCAPE: break;
    default: return( -1 );
  }
  return( pcl5_setparam( pData, iDataLen, '&', 'l', 'O', iValue ) ); 
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setportrait( char *pData, int iDataLen, int iValue );




@ The |pcl5_setinputbin| function. Sets an input bin value in the buffer of
the PCL5 document.
Valid values are:
PCL5_NOT_DEFINED  ( 99999 ) - Parameter not defined (will remove esc sequence)
0 - Print current page (paper source remains unchanged)
1 - Feed paper from main paper source
2 - Feed paper from manual input
3 - Feed envelope from manual input
4 - Feed paper from alternate paper source
5 - Feed from optional large paper source
6 - Feed envelope from envelope feeder *
7 - Autoselect
8 - Feed paper from Tray 1 (right side tray)
20...39 - High Capacity Input (HCI) Trays 2-21
Returns new length of the PCL document in buffer (>0) on success, or (-1) on error.
@<Exported Functions@>+=
int pcl5_setinputbin( char *pData, int iDataLen, int iValue ) {
  if( ( iValue < 0 || iValue > 39 ) && iValue != PCL5_NOT_DEFINED ) return( -1 );
  return( pcl5_setparam( pData, iDataLen, '&', 'l', 'H', iValue ) ); 
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setinputbin( char *pData, int iDataLen, int iValue );



@ The |pcl5_setoutputbin| function. Sets an output bin value in the buffer of
the PCL5 document.
Valid values are:
PCL5_NOT_DEFINED  ( 99999 ) - Parameter not defined (will remove esc sequence)
0 - Automatic selection
1 - Upper Output Bin (for the LaserJet 5Si,printer top/face-down bin—bin #1)
2 - Rear Output Bin (for the LaserJet 5Si, printer left/face-up bin—bin #2)
3 - Selects Bin #3 (HCO face-up bin)
4 - Selects Bin #4 (HCO #1 face-down bin)
5 - Selects Bin #5 (HCO #2 face-down bin)
6 - Selects Bin #6 (HCO #3 face-down bin)
7 - Selects Bin #7 (HCO #4 face-down bin)
8 - Selects Bin #8 (HCO #5 face-down bin)
9 - Selects Bin #9 (HCO #6 face-down bin)
10 - Selects Bin #10 (HCO #7 face-down bin)
11 - Selects Bin #11 (HCO #8 face-down bin)
Returns new length of the PCL document in buffer (>0) on success, or (-1) on error.
@<Exported Functions@>+=
int pcl5_setoutputbin( char *pData, int iDataLen, int iValue ) {
  if( ( iValue < 0 || iValue > 11 ) && iValue != PCL5_NOT_DEFINED ) return( -1 );
  return( pcl5_setparam( pData, iDataLen, '&', 'l', 'G', iValue ) ); 
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setoutputbin( char *pData, int iDataLen, int iValue );



@ The |pcl5_setpapersize| function. Sets the paper size value in the buffer of
the PCL5 document.
Valid values are:
PCL5_NOT_DEFINED  ( 99999 ) - Parameter not defined (will remove esc sequence)
1 - Executive (7.25" x 10.5")
2 - Letter (8.5" x 11")
3 - Legal (8.5" x 14")
6 - Ledger (11" x 17")
25 - A5 paper (148mm x 210mm)
26 - A4 paper (210mm x 297mm)
27 - A3 (297mm x 420mm)
45 - JIS B5 paper (182mm x 257mm)
46 - JIS B4 paper (250mm x 354mm)
71 - Hagaki postcard (100mm x 148mm)
72 - Oufuku-Hagaki postcard (200mm x 148mm)
80 - Monarch Envelope (3 7/8" x 7 1/2")
81 - Commercial Envelope 10 (4 1/8" x 9 1/2")
90 - International DL (110mm x 220mm)
91 - International C5 (162mm x 229mm)
100 - International B5 (176mm x 250mm)
101 - Custom (size varies with printer)
Returns new length of the PCL document in buffer (>0) on success, or (-1) on error.
@<Exported Functions@>+=
int pcl5_setpapersize( char *pData, int iDataLen, int iValue ) {
  if( ( iValue < 1 || iValue > 101 ) && iValue != PCL5_NOT_DEFINED ) return( -1 );
  return( pcl5_setparam( pData, iDataLen, '&', 'l', 'A', iValue ) ); 
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_setpapersize( char *pData, int iDataLen, int iValue );



@ Test Stub.  A test stub program |pcl5_setparam| is also created.
@(pcl5_setparam.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <rds_trn.h>
#include "rds_pcl5.h"
@<Application Functions@>@;
int main( int argc, char *argv[] ) {
  int iCmd, iValue, iSrcFileSize, iReadCnt, iWriteCnt, iNewSize;
  char szParam[ 32 ], szValue[ 128 ], szSrcFileName[ 256 ];
  char szDestFileName[ 256 ], *pData, *pEnd;

  if( argc != 5 ) {
    fprintf( stdout, "Usage: %s {-d|-p|-i|-o|-s} <value> <src_pcl_file> "
                     "<dest_pcl_file>\n", argv[ 0 ] );
    fprintf( stdout, "where" );

    fprintf( stdout, "\n-d - to set duplex/simplex value\n" );
    fprintf( stdout, "  <value> is one of the following numeric values per "
                     "PCL5 spec:\n" );
    fprintf( stdout, "    %d - single side (simplex)\n", PCL5_SIMPLEX );
    fprintf( stdout, "    %d - duplex, long-edge binding\n",
             PCL5_DUPLEX_LONGEDGE );
    fprintf( stdout, "    %d - duplex, short-edge binding\n",
             PCL5_DUPLEX_SHORTEDGE );
    fprintf( stdout, "    %d - remove duplex/simplex value parameter escape "
                     "sequence\n", PCL5_NOT_DEFINED );
    fflush( stdout );

    fprintf( stdout, "\n-p - to set portrait/landscape value\n" );
    fprintf( stdout, "  <value> is one of the following numeric values per "
                     "PCL5 spec:\n" );
    fprintf( stdout, "    %d - portrait\n", PCL5_PORTRAIT );
    fprintf( stdout, "    %d - landscape\n", PCL5_LANDSCAPE );
    fprintf( stdout, "    %d - reverse portrait\n", PCL5_REV_PORTRAIT );
    fprintf( stdout, "    %d - reverse landscape\n", PCL5_REV_LANDSCAPE );
    fprintf( stdout, "    %d - remove portrait/landscape value parameter "
                     "escape sequence\n", PCL5_NOT_DEFINED );
    fflush( stdout );

    fprintf( stdout, "\n-i - to set input bin value\n" );
    fprintf( stdout, "  <value> is one of the following numeric values per "
                     "PCL5 spec:\n" );
    fprintf( stdout, "    0 - print current page: paper source remains "
                     "unchanged\n");
    fprintf( stdout, "    1 - feed paper from main paper source\n" );
    fprintf( stdout, "    2 - feed paper from manual input\n" );
    fprintf( stdout, "    3 - feed envelope from manual input\n" );
    fprintf( stdout, "    4 - feed paper from alternate paper source\n" );
    fprintf( stdout, "    5 - feed from optional large paper source\n" );
    fprintf( stdout, "    6 - feed envelope from envelope feeder\n" );
    fprintf( stdout, "    7 - autoselect\n" );
    fprintf( stdout, "    8 - feed paper from tray 1: right side tray\n" );
    fprintf( stdout, "    20...39 - high capasity input tray 2...21\n" ); 
    fprintf( stdout, "    %d - remove input bin parameter escape sequence\n",
             PCL5_NOT_DEFINED );
    fflush( stdout );

    fprintf( stdout, "\n-o - to set output bin value\n" );
    fprintf( stdout, "  <value> is one of the following numeric values per "
                     "PCL5 spec:\n" );
    fprintf( stdout, "    0 - automatic selection\n" );
    fprintf( stdout, "    1 - upper output bin\n" );
    fprintf( stdout, "    2 - rear output bin\n" );
    fprintf( stdout, "    3 - output bin 3: high capacity output face-up "
                     "bin\n" );
    fprintf( stdout, "    4...11 - output bin 4...11: high capacity output "
                     "face-down bin 1...8\n" );
    fprintf( stdout, "    %d - remove output bin parameter escape sequence\n",
             PCL5_NOT_DEFINED );
    fflush( stdout );

    fprintf( stdout, "\n-s - to set paper size value\n" );
    fprintf( stdout, "  <value> is one of the following numeric values per "
                     "PCL5 spec:\n" );
    fprintf( stdout, "    1 - Executive (7.25\" x 10.5\")\n" );
    fprintf( stdout, "    2 - Letter (8.5\" x 11\")\n" );
    fprintf( stdout, "    3 - Legal (8.5\" x 14\")\n" );
    fprintf( stdout, "    6 - Ledger (11\" x 17\")\n" );
    fprintf( stdout, "    25 - A5 paper (148mm x 210mm)\n" );
    fprintf( stdout, "    26 - A4 paper (210mm x 297mm)\n" );
    fprintf( stdout, "    27 - A3 (297mm x 420mm)\n" );
    fprintf( stdout, "    45 - JIS B5 paper (182mm x 257mm)\n" );
    fprintf( stdout, "    46 - JIS B4 paper (250mm x 354mm)\n" );
    fprintf( stdout, "    71 - Hagaki postcard (100mm x 148mm)\n" );
    fprintf( stdout, "    72 - Oufuku-Hagaki postcard (200mm x 148mm)\n" );
    fprintf( stdout, "    80 - Monarch Envelope (3 7/8\" x 7 1/2\")\n" );
    fprintf( stdout, "    81 - Commercial Envelope 10 (4 1/8\" x 9 1/2\")\n" );
    fprintf( stdout, "    90 - International DL (110mm x 220mm)\n" );
    fprintf( stdout, "    91 - International C5 (162mm x 229mm)\n" );
    fprintf( stdout, "    100 - International B5 (176mm x 250mm)\n" );
    fprintf( stdout, "    101 - Custom (size varies with printer)\n" );
    fprintf( stdout, "    %d - remove paper size parameter escape sequence\n",
             PCL5_NOT_DEFINED );
    fflush( stdout );
    return( 1 );
  }

  trn_register( "pcl5_setparam" );

  iValue = strtol( argv[ 2 ], &pEnd, 10 );
  if( *pEnd != '\0' || ( iValue != PCL5_NOT_DEFINED &&
      ( iValue < -32767 || iValue > 65536 ) ) ) {
    fprintf( stdout, "Error, [%s] is invalid value\n", argv[ 2 ] );
    fflush( stdout );
    return( 1 );
  }

  if( strcmp( argv[ 1 ], "-d" ) == 0 ) {
    iCmd = 1;
    strcpy( szParam, "Duplex/Simplex" );
    strcpy( szValue, pcl5_duplex2str( iValue ) );
  }
  else if( strcmp( argv[ 1 ], "-p" ) == 0 ) {
    iCmd = 2;
    strcpy( szParam, "Portrait/Landscape" );
    strcpy( szValue, pcl5_portrait2str( iValue ) );
  }
  else if( strcmp( argv[ 1 ], "-i" ) == 0 ) {
    iCmd = 3;
    strcpy( szParam, "Input Bin" );
    strcpy( szValue, pcl5_inputbin2str( iValue ) );
  }
  else if( strcmp( argv[ 1 ], "-o" ) == 0 ) {
    iCmd = 4;
    strcpy( szParam, "Output Bin" );
    strcpy( szValue, pcl5_outputbin2str( iValue ) );
  }
  else if( strcmp( argv[ 1 ], "-s" ) == 0 ) {
    iCmd = 5;
    strcpy( szParam, "Paper Size" );
    strcpy( szValue, pcl5_papersize2str( iValue ) );
  }
  else {
    fprintf( stdout, "Error, [%s] is invalid command\n", argv[ 1 ] );
    fflush( stdout );
    return( 1 );
  }

  strncpy( szSrcFileName, argv[ 3 ], sizeof( szSrcFileName ) - 1 );
  szSrcFileName[ sizeof( szSrcFileName ) - 1 ] = '\0';

  strncpy( szDestFileName, argv[ 4 ], sizeof( szDestFileName ) - 1 );
  szDestFileName[ sizeof( szDestFileName ) - 1 ] = '\0';

  if( ( iSrcFileSize = GetFileSize( szSrcFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  if( ( pData = calloc( iSrcFileSize + PCL5_MAX_OVERHEAD, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d+%d bytes buffer for file [%s]\n",
             iSrcFileSize, PCL5_MAX_OVERHEAD, szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iSrcFileSize, szSrcFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iSrcFileSize, szSrcFileName );

  if( ( iReadCnt = ReadBinFile( szSrcFileName, pData, iSrcFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  else if( iReadCnt != iSrcFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
 
  switch( iCmd ) {
    case 1: iNewSize = pcl5_setduplex( pData, iSrcFileSize, iValue ); break;
    case 2: iNewSize = pcl5_setportrait( pData, iSrcFileSize, iValue ); break;
    case 3: iNewSize = pcl5_setinputbin( pData, iSrcFileSize, iValue ); break;
    case 4: iNewSize = pcl5_setoutputbin( pData, iSrcFileSize, iValue ); break;
    case 5: iNewSize = pcl5_setpapersize( pData, iSrcFileSize, iValue ); break;
  }
  if( iNewSize < 0 ) {
    fprintf( stdout, "Error setting %s value=%d in the %d-byte "
             "PCL file [%s]\n", szParam, iValue, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  if( ( iWriteCnt = WriteBinFile( szDestFileName, pData, iNewSize ) ) <= 0 ) {
    fprintf( stdout, "Error writing %d bytes of data to file [%s]\n",
             iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  if( iWriteCnt != iNewSize ) {
    fprintf( stdout, "Error, wrote %d of %d bytes to file [%s]\n",
             iWriteCnt, iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  fprintf( stdout, "%s value=%d (%s) is set in %d-byte [%s] file\n",
           szParam, iValue, szValue, iNewSize, szDestFileName ); 
  fflush( stdout );
  
  free( pData );
}



@ The |pcl5_stripkyo| function. Strips out any occurrences of the Kyocera
prescribe commands from the PCL document.

|pData| - is a ptr to the buffer with PCL document,

|iDataLen| - is a length of the PCL document in the buffer.

Per Kyocera Prescribe Language Reference, prescribe command, should be preceeded
by prefix !R! (unless prefix is changed by SCRC or FRPO P9 cmd). Each command is
terminated by |;|. Sequence of commands ends by |EXIT;| indicating exit from the
Prescribe mode. Commands are NOT case-sensitive and allow blank spaces, CR/LFs.
Prefix is case-sensitive. Suffix is NOT case-sensitive.

Example of prescribe cmd: | !R! SEM 6; EXIT; | or | !R!sem6;exit; | 

|piTotalKyo| is a ptr to an integer variable where the function stores a number
of the Kyocera prescribe command sequences found in the PCL document and
stripped. |NULL| value is allowed for |piTotalKyo|.

Returns new length of the PCL document in buffer (>0) on success, or (-1) on
error. Max new length of the document may not exceed the value of |iDataLen|.
@<Exported Functions@>+=
int pcl5_stripkyo( char *pData, int iDataLen, int *piTotalKyo ) {
  int i, iLen, iPrefixLen, iSuffixLen, iStartPos, iTotalKyo = 0;
  char szPrefix[ 8 ], szSuffix[ 8 ];

  strcpy( szPrefix, "!R!" );
  iPrefixLen = strlen( szPrefix );
  strcpy( szSuffix, "EXIT;" );
  iSuffixLen = strlen( szSuffix );
  if( piTotalKyo != NULL ) *piTotalKyo = iTotalKyo;

  if( iDataLen < 0 ) return( -1 );
  if( iDataLen < iPrefixLen + iSuffixLen ) return( iDataLen );

  for( i = 0, iLen = iDataLen; i < iLen - iPrefixLen + 1; i++ ) {
    if( memcmp( &pData[ i ], szPrefix, iPrefixLen ) != 0 ) continue;
    iStartPos = i;
    // Inform( "%d: StartPos: %d", iTotalKyo + 1, iStartPos ); 
    for( i += iPrefixLen; i < iLen - iSuffixLen + 1; i++ ) {
      if( strncasecmp( &pData[ i ], szSuffix, iSuffixLen ) != 0 ) continue;
      i += iSuffixLen;
      memmove( &pData[ iStartPos ], &pData[ i ], iLen - i );
      iLen -= i - iStartPos;
      // Inform( "%d: EndPos:%d, KyoLen:%d, new datalen:%d",
      //         iTotalKyo + 1, i, i - iStartPos, iLen ); 
      i = iStartPos - 1;
      iTotalKyo++;
      break;
    }
  }

  if( piTotalKyo != NULL ) *piTotalKyo = iTotalKyo;
  return( iLen );
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_stripkyo( char *pData, int iDataLen, int *piTotalKyo );


@ Test Stub.  A test stub program |pcl5_stripkyo| is also created.
@(pcl5_stripkyo.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <rds_trn.h>
#include "rds_pcl5.h"
@<Application Functions@>@;
int main( int argc, char *argv[] ) {
  int iCmd, iSrcFileSize, iReadCnt, iWriteCnt, iNewSize, iTotalKyo;
  char szSrcFileName[ 256 ], szDestFileName[ 256 ], *pData;

  if( argc != 3 ) {
    fprintf( stdout, "Usage: %s <src_pcl_file> <dest_pcl_file>\n", argv[ 0 ] );
    fprintf( stdout, "This utility strips out any occurrences of the Kyocera ");
    fprintf( stdout, "Prescribe language\ncommand sequences in the PCL " );
    fprintf( stdout, "document from the file <src_pcl_file>\n" );
    fprintf( stdout, "and stores the resulting document in the file " );
    fprintf( stdout, "<dest_pcl_file>\n" );
    fflush( stdout );
    return( 1 );
  }
  trn_register( "pcl5_stripkyo" );

  strncpy( szSrcFileName, argv[ 1 ], sizeof( szSrcFileName ) - 1 );
  szSrcFileName[ sizeof( szSrcFileName ) - 1 ] = '\0';

  strncpy( szDestFileName, argv[ 2 ], sizeof( szDestFileName ) - 1 );
  szDestFileName[ sizeof( szDestFileName ) - 1 ] = '\0';

  if( ( iSrcFileSize = GetFileSize( szSrcFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  if( ( pData = calloc( iSrcFileSize, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d bytes buffer for file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iSrcFileSize, szSrcFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iSrcFileSize, szSrcFileName );

  if( ( iReadCnt = ReadBinFile( szSrcFileName, pData, iSrcFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  else if( iReadCnt != iSrcFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
 
  iNewSize = pcl5_stripkyo( pData, iSrcFileSize, &iTotalKyo );
  if( iNewSize < 0 ) {
    fprintf( stdout, "Error stripping out Kyocera prescribe commands from the "
             "%d-byte PCL file [%s]\n", iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  Trace( "Stripping out %d Kyocera cmd sequences, doc size %d=>%d",
         iTotalKyo, iSrcFileSize, iNewSize ); 
  fprintf( stdout, "Stripping out %d Kyocera prescribe command sequence(s)\n",
           iTotalKyo );
  fflush( stdout );

  if( ( iWriteCnt = WriteBinFile( szDestFileName, pData, iNewSize ) ) <= 0 ) {
    fprintf( stdout, "Error writing %d bytes of data to file [%s]\n",
             iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  if( iWriteCnt != iNewSize ) {
    fprintf( stdout, "Error, wrote %d of %d bytes to file [%s]\n",
             iWriteCnt, iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  fprintf( stdout, "Saving PCL document in %d-byte file [%s]\n",
           iNewSize, szDestFileName ); 
  fflush( stdout );
  
  free( pData );
}



@ The |pcl5_strippjl| function. Strips out any occurrences of the PJL commands
from the PCL document.

|pData| - is a ptr to the buffer with PCL document,

|iDataLen| - is a length of the PCL document in the buffer.

Per PJL Reference, the PJL command should be preceeded by prefix | @@PJL |
(case sensitive). Each command is terminated by line feed (0x0A).

Example of PJL cmd: | @@PJL SET RESOLUTION=600<CR><LF> |

|piTotalPjl| is a ptr to an integer variable where the function stores a number
of the PJL commands found in the PCL document and stripped.
|NULL| value is allowed for |piTotalPjl|.

Returns new length of the PCL document in buffer (>0) on success, or (-1) on
error. Max new length of the document may not exceed the value of |iDataLen|.
@<Exported Functions@>+=
int pcl5_strippjl( char *pData, int iDataLen, int *piTotalPjl ) {
  int i, iLen, iPrefixLen, iSuffixLen, iStartPos, iTotalPjl = 0;
  char szPrefix[ 8 ], szSuffix[ 8 ];

  strcpy( szPrefix, "@@PJL" );
  iPrefixLen = strlen( szPrefix );
  szSuffix[ 0 ] = LF;
  szSuffix[ 1 ] = '\0';
  iSuffixLen = strlen( szSuffix );
  if( piTotalPjl != NULL ) *piTotalPjl = iTotalPjl;

  if( iDataLen < 0 ) return( -1 );
  if( iDataLen < iPrefixLen + iSuffixLen ) return( iDataLen );

  for( i = 0, iLen = iDataLen; i < iLen - iPrefixLen + 1; i++ ) {
    if( memcmp( &pData[ i ], szPrefix, iPrefixLen ) != 0 ) continue;
    iStartPos = i;
    //Inform( "%d: StartPos: %d", iTotalPjl + 1, iStartPos ); 
    for( i += iPrefixLen; i < iLen - iSuffixLen + 1; i++ ) {
      if( memcmp( &pData[ i ], szSuffix, iSuffixLen ) != 0 ) continue;
      i += iSuffixLen;
      memmove( &pData[ iStartPos ], &pData[ i ], iLen - i );
      iLen -= i - iStartPos;
      //Inform( "%d: EndPos:%d, PjlLen:%d, new datalen:%d",
      //        iTotalPjl + 1, i, i - iStartPos, iLen );
      i = iStartPos - 1;
      iTotalPjl++;
      break;
    }
  }

  if( piTotalPjl != NULL ) *piTotalPjl = iTotalPjl;
  return( iLen );
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_strippjl( char *pData, int iDataLen, int *piTotalPjl );


@ Test Stub.  A test stub program |pcl5_strippjl| is also created.
@(pcl5_strippjl.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <rds_trn.h>
#include "rds_pcl5.h"
@<Application Functions@>@;
int main( int argc, char *argv[] ) {
  int iCmd, iSrcFileSize, iReadCnt, iWriteCnt, iNewSize, iTotalPjl;
  char szSrcFileName[ 256 ], szDestFileName[ 256 ], *pData;

  if( argc != 3 ) {
    fprintf( stdout, "Usage: %s <src_pcl_file> <dest_pcl_file>\n", argv[ 0 ] );
    fprintf( stdout, "This utility strips out any occurrences of the PJL ");
    fprintf( stdout, "commands\nin the PCL " );
    fprintf( stdout, "document from the file <src_pcl_file>\n" );
    fprintf( stdout, "and stores the resulting document in the file " );
    fprintf( stdout, "<dest_pcl_file>\n" );
    fflush( stdout );
    return( 1 );
  }
  trn_register( "pcl5_strippjl" );

  strncpy( szSrcFileName, argv[ 1 ], sizeof( szSrcFileName ) - 1 );
  szSrcFileName[ sizeof( szSrcFileName ) - 1 ] = '\0';

  strncpy( szDestFileName, argv[ 2 ], sizeof( szDestFileName ) - 1 );
  szDestFileName[ sizeof( szDestFileName ) - 1 ] = '\0';

  if( ( iSrcFileSize = GetFileSize( szSrcFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  if( ( pData = calloc( iSrcFileSize, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d bytes buffer for file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iSrcFileSize, szSrcFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iSrcFileSize, szSrcFileName );

  if( ( iReadCnt = ReadBinFile( szSrcFileName, pData, iSrcFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  else if( iReadCnt != iSrcFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
 
  iNewSize = pcl5_strippjl( pData, iSrcFileSize, &iTotalPjl );
  if( iNewSize < 0 ) {
    fprintf( stdout, "Error stripping out PJL commands from the "
             "%d-byte PCL file [%s]\n", iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  Trace( "Stripping out %d PJL commands, doc size %d=>%d",
         iTotalPjl, iSrcFileSize, iNewSize ); 
  fprintf( stdout, "Stripping out %d PJL command(s)\n", iTotalPjl );
  fflush( stdout );

  if( ( iWriteCnt = WriteBinFile( szDestFileName, pData, iNewSize ) ) <= 0 ) {
    fprintf( stdout, "Error writing %d bytes of data to file [%s]\n",
             iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  if( iWriteCnt != iNewSize ) {
    fprintf( stdout, "Error, wrote %d of %d bytes to file [%s]\n",
             iWriteCnt, iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  fprintf( stdout, "Saving PCL document in %d-byte file [%s]\n",
           iNewSize, szDestFileName ); 
  fflush( stdout );
  
  free( pData );
}



@ The |pcl5_insert| function. Inserts a code block into a PCL5 code at the end
of the specified page (right before page-break character).
The |pData| buffer should allow enough space to insert a block |pBlock|.

The function returns new length of PCL5 code on success,
Returns 0 on error, when no specified page is found, and block was not inserted.
@<Exported Functions@>+=
int pcl5_insert( char *pData, int iDataLen, int iPage, const char *pBlock,
                 int iBlockLen ) {
  int i, iValue, iStoredValue;
  char chEnd, *pEnd;

  int iPages = 0;

  for( i = 0; i < iDataLen; i++ ) {
    if( pData[ i ] == FF ) {
      iPages++;
      if( iPage == iPages ) {
        /* insert block here at position i */
        memmove( pData + i + iBlockLen, pData + i, iDataLen - i );
        memmove( pData + i, pBlock, iBlockLen );
        return( iDataLen + iBlockLen );
      }
    }
    else if( pData[ i ] != ESC ) continue;
    i++;

    //===================================================================
    // Skipping binary blocks defined by the following PCL commands:
    //   <ESC>*b#...#W - block of raster data
    //   <ESC>*c#...#W - block of patern data
    //   <ESC>*v#...#W - block of color raster data
    //   <ESC>*m#...#W - block of dither matrix data
    //   <ESC>*l#...#W - block of lookup tables
    //   <ESC>*i#...#W - block of viewing illuminant data
    //   <ESC>(s#...#W - block of character data
    //   <ESC>)s#...#W - block of font data
    //   <ESC>(f#...#W - block of symbol set definition data
    //   <ESC>&b#...#W - block of AppleTalk I/O configuration data
    //   <ESC>&n#...#W - block of string IDs for font, macros, ans media types
    //   <ESC>&p#...#X - block of transparent print data
    //===================================================================
    if( memcmp( &pData[ i ], "*b", 2 ) == 0 ||
        memcmp( &pData[ i ], "*c", 2 ) == 0 ||
        memcmp( &pData[ i ], "*v", 2 ) == 0 ||
        memcmp( &pData[ i ], "*m", 2 ) == 0 ||
        memcmp( &pData[ i ], "*l", 2 ) == 0 ||
        memcmp( &pData[ i ], "*i", 2 ) == 0 ||
        memcmp( &pData[ i ], "(s", 2 ) == 0 ||
        memcmp( &pData[ i ], ")s", 2 ) == 0 ||
        memcmp( &pData[ i ], "(f", 2 ) == 0 ||
        memcmp( &pData[ i ], "&b", 2 ) == 0 ||
        memcmp( &pData[ i ], "&n", 2 ) == 0 ||
        memcmp( &pData[ i ], "&p", 2 ) == 0 ) {
      chEnd = memcmp( &pData[ i ], "&p", 2 ) ? 'W' : 'X';

      for( i += 2, iStoredValue = 0; ; i++ ) {
        iValue = strtol( &pData[ i ], &pEnd, 10 ); /* numeric value of param */
        i += pEnd - &pData[ i ];     /* suffix character is at this position */

        /* known terminating suffix of PCL cmd (usually upper case char) */
        if( pData[ i ] == chEnd ) {
          i += iValue;                                /* skip binary block */
          break;
        }

        /* known non-terminating suffix of PCL cmd (usually lower case char) */
        else if( pData[ i ] == chEnd + 32 ) iStoredValue = iValue;

        /* non-terminating suffix of PCL cmd (usually lower case char) */
        else if( pData[ i ] >= 96 && pData[ i ] <= 126 ) ;

        /* terminating suffix of PCL cmd (usually upper case char) */
        else if( pData[ i ] >= 64 && pData[ i ] <= 94 ) {
          if( iStoredValue != 0 ) i += iStoredValue;  /* skip binary block */
          break;
        }

        /* not a suffix of PCL cmd - exit cmd processing loop */
        else break;
      }
    }
  }
  return( 0 );
}
@ Prototype.
@<Exported Prototypes@>+=
int pcl5_insert( char *pData, int iDataLen, int iPage, const char *pBlock,
                 int iBlockLen );



@ Test Stub.  A test stub program |pcl5_insert| is also created.
@(pcl5_insert.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include <rds_trn.h>
#include "rds_pcl5.h"
@<Application Functions@>@;
int main( int argc, char *argv[] ) {
  int iPage, iSrcFileSize, iBlockFileSize, iReadCnt, iWriteCnt, iNewSize;
  char *pEnd, *pData, *pBlock;
  char szSrcFileName[ 256 ], szDestFileName[ 256 ], szBlockFileName[ 256 ];

  if( argc != 5 ) {
    fprintf( stdout, "Usage: %s <src_pcl_file> <dest_pcl_file> <page> "
                     "<block_to_insert_file>\n", argv[ 0 ] );
    fprintf( stdout, "where\n" );
    fprintf( stdout, "  <page> is the page number of the <src_pcl_file> where to "
                     "insert a binary\n"
                     "         PCL block (from 1 to total number of pages)\n" );
    fprintf( stdout, "  <block_to_insert_file> is a file name of the binary "
                     "PCL block to insert\n" );
    fflush( stdout );
    return( 1 );
  }

  trn_register( "pcl5_insert" );

  iPage = strtol( argv[ 3 ], &pEnd, 10 );
  if( *pEnd != '\0' || iPage <= 0 ) {
    fprintf( stdout, "Error, [%s] is invalid page number parameter\n", argv[ 3 ] );
    fflush( stdout );
    return( 1 );
  }

  strncpy( szSrcFileName, argv[ 1 ], sizeof( szSrcFileName ) - 1 );
  szSrcFileName[ sizeof( szSrcFileName ) - 1 ] = '\0';
  strncpy( szDestFileName, argv[ 2 ], sizeof( szDestFileName ) - 1 );
  szDestFileName[ sizeof( szDestFileName ) - 1 ] = '\0';
  strncpy( szBlockFileName, argv[ 4 ], sizeof( szBlockFileName ) - 1 );
  szBlockFileName[ sizeof( szBlockFileName ) - 1 ] = '\0';

  if( ( iSrcFileSize = GetFileSize( szSrcFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  if( ( iBlockFileSize = GetFileSize( szBlockFileName ) ) < 0 ) {
    fprintf( stdout, "File [%s] does not exist\n", szBlockFileName );
    fflush( stdout );
    return( 1 );
  }

  if( ( pData = calloc( iSrcFileSize + iBlockFileSize, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d+%d bytes buffer for file [%s]\n",
             iSrcFileSize, iBlockFileSize, szSrcFileName );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iSrcFileSize, szSrcFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iSrcFileSize, szSrcFileName );

  if( ( iReadCnt = ReadBinFile( szSrcFileName, pData, iSrcFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }
  else if( iReadCnt != iSrcFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    return( 1 );
  }

  if( ( pBlock = calloc( iBlockFileSize, 1 ) ) == NULL ) {
    fprintf( stdout, "Cannot allocate %d bytes buffer for file [%s]\n",
             iBlockFileSize, szBlockFileName );
    free( pData );
    fflush( stdout );
    return( 1 );
  }
  fprintf( stdout, "Reading %d-byte file [%s]\n", iBlockFileSize, szBlockFileName );
  fflush( stdout );
  Inform( "Reading %d-byte file [%s]", iBlockFileSize, szBlockFileName );

  if( ( iReadCnt = ReadBinFile( szBlockFileName, pBlock, iBlockFileSize ) ) < 0 ) {
    fprintf( stdout, "Error reading %d-byte file [%s]\n",
             iBlockFileSize, szBlockFileName );
    fflush( stdout );
    free( pData );
    free( pBlock );
    return( 1 );
  }
  else if( iReadCnt != iBlockFileSize ) {
    fprintf( stdout, "Error, read %d bytes of %d-byte file [%s]\n",
             iReadCnt, iBlockFileSize, szBlockFileName );
    fflush( stdout );
    free( pData );
    free( pBlock );
    return( 1 );
  }

  iNewSize = pcl5_insert( pData, iSrcFileSize, iPage, pBlock, iBlockFileSize );
  if( iNewSize <= 0 ) {
    fprintf( stdout, "Error inserting %d-byte block at page %d of the %d-byte "
             "PCL file [%s]\n",
             iBlockFileSize, iPage, iSrcFileSize, szSrcFileName );
    fflush( stdout );
    free( pData );
    free( pBlock );
    return( 1 );
  }

  if( ( iWriteCnt = WriteBinFile( szDestFileName, pData, iNewSize ) ) <= 0 ) {
    fprintf( stdout, "Error writing %d bytes of data to file [%s]\n",
             iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    free( pBlock );
    return( 1 );
  }
  if( iWriteCnt != iNewSize ) {
    fprintf( stdout, "Error, wrote %d of %d bytes to file [%s]\n",
             iWriteCnt, iNewSize, szDestFileName );
    fflush( stdout );
    free( pData );
    free( pBlock );
    return( 1 );
  }

  fprintf( stdout, "%d-byte block inserted at page %d into %d-byte PCL file "
                   "[%s]\n", iBlockFileSize, iPage, iNewSize, szDestFileName );
  fflush( stdout );
  free( pData );
  free( pBlock );
  Trace( "%d-byte block inserted at page %d into %d-byte PCL file [%s]",
         iBlockFileSize, iPage, iNewSize, szDestFileName );
}





@ The |WriteBinFile| function. Writes binary data from buffer to a file.
@<Application Functions@>+=
int WriteBinFile( const char *pszFileName, const void *pData, int iSize ) {
  int iCnt = -1;
  FILE *pf = NULL;

  if( iSize <= 0 ) Alert( "No data to write to a [%s] file", pszFileName );
  else if( ( pf = fopen( pszFileName, "wb" ) ) == NULL )
    Alert( "Cannot open file [%s] to write", pszFileName );
  else if( ( iCnt = fwrite( pData, 1, iSize, pf ) ) <= 0 )
    Alert( "Error writing to file [%s]", pszFileName );
  else if( iCnt != iSize )
    Alert( "Written %d bytes (expected %d) to file [%s]",
           iCnt, iSize, pszFileName );
  else Inform( "Written %d bytes to file [%s]", iCnt, pszFileName );
  if( pf ) fclose( pf );
  return( iCnt );
}
@ Prototype.
@<Application Prototypes@>+=
int WriteBinFile( const char *pszFileName, const void *pData, int iSize );



@ The |ReadBinFile| function.
Reads binary data from file and stores it in the memory buffer.
@<Application Functions@>+=
int ReadBinFile( const char *pszFileName, void *pDataBuf, int iBufSize ) {
  int iCnt = -1;
  FILE *pf = NULL;

  if( ( pf = fopen( pszFileName, "rb" ) ) == NULL )
    Alert( "Cannot open file [%s] to read", pszFileName );
  else if( ( iCnt = fread( pDataBuf, 1, iBufSize, pf ) ) <= 0 )
    Alert( "Error reading from file [%s]", pszFileName );
  else if( iCnt > iBufSize )
    Alert( "File is too big: read %d bytes from [%s]", iCnt, pszFileName );
  else Inform( "Read %d bytes from [%s]", iCnt, pszFileName );
  if( pf ) fclose( pf );
  return( iCnt );
}
@ Prototype.
@<Application Prototypes@>+=
int ReadBinFile( const char *pszFileName, void *pDataBuf, int iBufSize );



@ The |GetFileSize| function. Gets a size of the file having a file name.
Returns file size >= 0 on success, or |-1| on error.
@<Application Functions@>+=
int GetFileSize( const char *pszFileName ) {
  struct stat st;
  int iErr;
  
  if( stat( pszFileName, &st ) < 0 ) {
    iErr = errno;
    Alert( "Error %d in GetFileSize: (%s) for filename [%s]",
           iErr, strerror( iErr ), pszFileName );
    return( -1 );
  }
  return( st.st_size ); 
}
@ Prototype.
@<Application Prototypes@>+=
int GetFileSize( const char *pszFileName );



@ Internal (static) functions.
@<Functions@>+=

@ Prototype.
@<Prototypes@>+=


@*Index.
