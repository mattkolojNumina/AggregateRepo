%                       
%   tracer.w
%
%   Author: Mark Woodworth 
%
%   History:
%      06/15/2002 mrw: init
%      10/27/2004 ank: added Alert and Inform
%      06/28/2007 ank: renamed to *.w, included string.h to avoid warning with
%                 FC6 C-compiler
%      05/03/2016 ank: removed escaping for back-slash, double-quote,
%                 and percent sign. Instead, changed to use safe tracing
%                 functions, ex. trace( "%s", msg );
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
%     (C) Copyright 2002 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Tracer - tracing gateway}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program sends its standard input to Tron.

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
\centerline{Control Revision: $ $Revision: 1.5 $ $}
\centerline{Control Date: $ $Date: 2019/02/15 20:43:59 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2002 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program redirect stdin messages to tracing pool.


@c
@<Includes@>@;
@<Functions@>@;
int
main( int argc, char *argv[] ) {
  char szLine[ TRN_MSG_LEN + 1 ], szEsc[ TRN_MSG_LEN + 1 ];

  if( argc > 1 ) trn_register( argv[ 1 ] );
  else trn_register( "tracer" );
  Inform( "Init tracer" );

  while( fgets( szLine, TRN_MSG_LEN, stdin ) ) {
    szLine[ strlen( szLine ) - 1 ] = '\0';
    Escape( szLine, szEsc, sizeof( szEsc ) );
    if( strlen( szEsc ) >= 2 ) {
      if( szEsc[ 0 ] == 'A' && szEsc[ 1 ] == ':' ) Alert( "%s", szEsc + 2 );
      else if( szEsc[ 0 ] == 'T' && szEsc[ 1 ] == ':' ) Trace( "%s", szEsc + 2);
      else if( szEsc[ 0 ] == 'I' && szEsc[ 1 ] == ':' ) Inform("%s", szEsc + 2);
      else Trace( "%s", szEsc );
    }    
    else Trace( "%s", szEsc );
  }
  return 0 ;
}


@ Escape special characters for tracing. Returns length of the |pszDst|.
@<Functions@>+=
int Escape( const char *pszSrc, char *pszDst, int iDstBufSize ) {
  int i, j, iLen;
  char ch;

  if( ( iLen = strlen( pszSrc ) ) <= 0 ) return 0 ;
  for( i = 0, j = 0; i < iLen; i++ ) {
    if( j >= iDstBufSize - 2 ) {
      pszDst[ j ] = '\0';
      return( j );
    }
    //if( ( ch = pszSrc[ i ] ) == '\"' ) {
    //  pszDst[ j++ ] = '\\';
    //  pszDst[ j++ ] = '\"';
    //}
    //else if( ch == '\\' ) {
    //  pszDst[ j++ ] = '\\';
    //  pszDst[ j++ ] = '\\';
    //}
    //if( ( ch = pszSrc[ i ] ) == '\%' ) {
    //  pszDst[ j++ ] = '\%';
    //  pszDst[ j++ ] = '\%';
    //}
    if( ( ch = pszSrc[ i ] ) == '\n' || ch == '\r' ) {
      pszDst[ j++ ] = ' ';
      pszDst[ j++ ] = ' ';
    }
    else if( !isprint( ch ) ) pszDst[ j++ ] = '.';
    else pszDst[ j++ ] = ch;
  }
  pszDst[ j ] = '\0';
  return( j );
}


@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>  // for isprint()

#include <rds_trn.h>


@* Index.
