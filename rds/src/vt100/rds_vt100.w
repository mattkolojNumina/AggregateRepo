%                       
%   vt100.web
%
%   Author: Mark Olson 
%
%   History:
%      10/20/1999 wpm  added SCN_ncPrint & SCN_ClearField
%       7/13/2000 mdo  Update and cleanup for RDS v3
%       8/9/2004  ank  Recompliled for RH9, fixed errno issue
%       6/22/2005 ank  added vt100_GetChar function
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
%     (C) Copyright 1993 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{VT100 Screen Control Library Module}
\def\dot{\quad\qquad\item{$\bullet$\ }}


%
% --- title ---
%
\def\title{RDS3 VT100 -- Screen Control Library}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a lightweight vt100 compliant screen control library. It exports a 
series of lightweight low-level calls allowing a program to clear the screen,
move to a screen position, clear to end of line and print to the screen.
Additionally, it supports some basic attribute changes.


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
% --- id ----
%
\bigskip
\centerline{Author: Mark Olson}
\centerline{Printing Date: \today}
\centerline{RCS Version: $ $ Revision:$ $}
\centerline{RCS Date: $ $Date: 2019/02/15 20:52:56 $ $}
}


%
% --- copyright ---
%
\def\botofcontents{\vfill
\centerline{\copyright 1995 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview.
The exported function calls are:

\dot |void vt100_Open(int ifd, int ofd)| -- Subsequent calls to this library
will use this file descriptor. It is up to the client program to open device.

\dot |void vt100_Attr(int attr)| -- sets a screen attribute.

\dot |void vt100_AltChar(int flag)| chooses or resets the alternate charset.

\dot |void vt100_Clear(void)| -- Clears the screen.

\dot |void vt100_ClearLine(void)| -- Clears from the cursor to the end of 
the line.

\dot |void vt100_Move(int row,int columnt)| -- Moves the cursor to a given
position.

\dot |void vt100_Bold(int flag)| -- Turns on or off the reverse video.

\dot |void vt100_Print(int row,int column,char *fmt,...)| -- Print (after 
clearing to end of line).

\dot |void vt100_ncPrint(int row,int column,char *fmt,...)| -- Print (Don't 
clear to end of line).

\dot |int vt100_GetLine(char *input,int maxchars)| -- Get input, taking care
of backspaces and editing. The return value is the length of the returned
string.

\dot |char vt100_GetChar()| -- Get a single character of input


@* Implementation. The library consists of the following:
@c
@<Includes@>@;
@<Definitions@>@;
@<Globals@>@;
@<Exported Functions@>@;



@ We export a header file for those people who use a library.
@(rds_vt100.h@>=
#ifndef __VT100_H
#define __VT100_H
  @<Exported Definitions@>@;
  @<Exported Prototypes@>@;
#endif



@ Definitions start with includes:
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <time.h>
#include <termios.h>
#include <stdarg.h>
#include <errno.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include <rds_trn.h>
#include "rds_vt100.h"



@ Internal Definitions.
@<Definitions@>=
#define CMD_CLEAR     "\033[2J"
#define CMD_CLEARLINE "\033[K"
#define CMD_SINGLE    "\033#5"
#define CMD_DOUBLE    "\033#6"
#define BS            0x7F
#define EOS           '\0'



@ The |scr_ifd| and |scr_ofd| are internal globals.
@<Globals@>=
static int scr_ifd = -1;
static int scr_ofd = -1;



@ Export the attributes.
@<Exported Definitions@>+=
#define A_NORMAL    0
#define A_BOLD      1
#define A_DIM       2
#define A_BLINK     3
#define A_REVERSE   7
#define A_F_BLACK   30
#define A_F_RED     31
#define A_F_GREEN   32
#define A_F_BROWN   33
#define A_F_BLUE    34
#define A_F_MAGENTA 35
#define A_F_CYAN    36
#define A_F_WHITE   37
#define A_B_BLACK   40
#define A_B_RED     41
#define A_B_GREEN   42
#define A_B_BROWN   43
#define A_B_BLUE    44
#define A_B_MAGENTA 45
#define A_B_CYAN    46
#define A_B_WHITE   47



@* Code.
@<Exported Functions@>+=
void vt100_Open( int ifd, int ofd ) {
  if( ifd == 0 && ofd == 1 ) {
    struct termios t;
    tcgetattr( 1, &t );
    t.c_cflag |= CLOCAL;
    t.c_lflag &= ~( ICANON | ECHO );
    tcsetattr( 1, TCSANOW, &t );
  }
  scr_ifd = ifd;
  scr_ofd = ofd;   
  return;
}
@ Prototypes.
@<Exported Prototypes@>+=
void vt100_Open( int ifd, int ofd );



@ Close.
@<Exported Functions@>+=
int vt100_Close( void ) {
  if( scr_ofd == 1 ) {
    struct termios t;
    tcgetattr( 1, &t );
    t.c_cflag &= ~CLOCAL;
    t.c_lflag |= ICANON | ECHO;
    tcsetattr( 1, TCSANOW, &t );
  }
  close( scr_ifd );
  if( scr_ifd != scr_ofd ) close( scr_ofd );
}
@ Prototype.
@<Exported Prototypes@>+=
int vt100_Close( void );



@ Clear.
@<Exported Functions@>+=
void vt100_Clear( void ) {
  int err;

  err = write( scr_ofd, CMD_CLEAR, strlen( CMD_CLEAR ) );
  @<Test for Error@>@;
  
}

@ Prototypes.
@<Exported Prototypes@>+=
void vt100_Clear( void );



@ Clear to end of line.
@<Exported Functions@>+=
void vt100_ClearLine( void ) {
  int err;

  err = write( scr_ofd, CMD_CLEARLINE, strlen( CMD_CLEARLINE ) );
  @<Test for Error@>@;
}

@ Prototypes.
@<Exported Prototypes@>+=
void vt100_ClearLine( void );



@ Move.
@<Exported Functions@>+=
void vt100_Move( int row, int column ) {
  char send[ 32 ];
  int len, err;

  len = sprintf( send, "%c[%d;%dH", 033, row + 1, column );  /* ?? */
  err = write( scr_ofd, send, len );
  @<Test for Error@>@;
}
@ Prototypes.
@<Exported Prototypes@>+=
void vt100_Move( int row, int column );



@ Set the attribute bytes.
@<Exported Functions@>+=
void vt100_Attr( int attr ) {
  char send[ 32 ];
  int len, err;

  len = sprintf( send, "%c[%dm", 033, attr );
  err = write( scr_ofd, send, len );
  @<Test for Error@>@;
}

void vt100_Normal( void ) {
  vt100_Attr( 0 );
}

void vt100_Bold( void ) {
  vt100_Attr( 1 );
}

void vt100_Reverse( void ) {
  vt100_Attr( 7 );
}

void vt100_AltChar( int flag ) {
  char send[ 32 ];
  int len, err;

  len = sprintf( send, "%c", flag ? 14 : 15 );
  err = write( scr_ofd, send, len );
  @<Test for Error@>@;
}      

@ Prototypes.
@<Exported Prototypes@>+=
void vt100_Attr( int attr );
void vt100_Normal( void );
void vt100_Bold( void );
void vt100_Reverse( void );
void vt100_AltChar( int flag );



@ Single and Double.
@<Exported Functions@>+=
void vt100_Single( void ) {
  int err;

  err = write( scr_ofd, CMD_SINGLE, strlen( CMD_SINGLE ) );
  @<Test for Error@>@;
}

void vt100_Double( void ) {
  int err;

  err =  write( scr_ofd, CMD_DOUBLE, strlen( CMD_DOUBLE ) );
  @<Test for Error@>@;
}

@ Prototypes.
@<Exported Prototypes@>+=
void vt100_Single( void );
void vt100_Double( void );



@ ClearField.
@<Exported Functions@>+=
void vt100_ClearField( int row, int column, int len ) {
  int i, err;
  char buffer[ 80 ];

  for( i = 0; i < len; i++ ) buffer[ i ] = ' ';
  buffer[ i ] = '\0';
  vt100_Move( row, column );
  err = write( scr_ofd, buffer, len );
  @<Test for Error@>@;
}
@ Prototype.
@<Exported Prototypes@>+=
void vt100_ClearField( int row, int column, int len );



@ ncPrint. Print the line w/out clearing it first
@<Exported Functions@>+=
void vt100_ncPrint( int row, int column, char *fmt, ... ) {
  va_list argptr;
  int len, err;
  static char buffer[ 200 ];

  vt100_Move( row, column );
  va_start( argptr, fmt );       /* required for var args */
  len = vsprintf( buffer, fmt, argptr );
  va_end( argptr );              /* null, but formally required for var args */
  err = write( scr_ofd, buffer, len );
  @<Test for Error@>@;
}
@ Prototype.
@<Exported Prototypes@>+=
void vt100_ncPrint( int row, int column, char *fmt, ... );



@ Print. 
@<Exported Functions@>+=
void vt100_Print( int row, int column, char *fmt, ... ) {
  va_list argptr;
  int len, err;
  static char buffer[ 200 ];

  vt100_Move( row, column );
  vt100_ClearLine();
  vt100_Move( row, column );  /* this might not be required */
  va_start( argptr, fmt );    /* required for var args */
  len = vsprintf( buffer, fmt, argptr );
  va_end( argptr );           /* null, but formally required for var args */
  err = write( scr_ofd, buffer, len );
  @<Test for Error@>@;
}
@ Prototype.
@<Exported Prototypes@>+=
void vt100_Print( int row, int column, char *fmt, ... );



@ We add a similar function which centers it's data.
@<Exported Functions@>+=
void vt100_Center( int row, char *fmt, ... ) {
  va_list argptr;
  int len, column, err;
  static char buffer[ 200 ];

  va_start( argptr, fmt );  /* required for var args */
  len = vsprintf( buffer, fmt, argptr );
  va_end( argptr );         /* null, but formally required for var args */
  column = 40 - ( len / 2 );
  vt100_Move( row, 0 );
  vt100_ClearLine();
  vt100_Move( row, column ); /* this might not be required */
  err = write( scr_ofd, buffer, len );
  @<Test for Error@>@;
}
@ Prototype.
@<Exported Prototypes@>+=
void vt100_Center( int row, char *fmt, ... );



@ Finally the input/editing function. This takes in backspaces and 
re-positions the cursor appropriately. The edited string is also
adjusted with the backspace correctly.

@<Exported Functions@>+=
int vt100_GetLine( char *input, int maxchars ) {
  char c;
  int i, err, count = 0;

  for( i = 0; i < maxchars; i++ ) input[ i ] = EOS;
  vt100_Reverse();
  while( count < maxchars ) {
    err = read( scr_ifd, &c, 1 );
    @<Process Input@>@;
    err = write( scr_ofd, &c, 1 );    
    count++;
    @<Test for Error@>@;
  }
  vt100_Normal();
  return count;
}
@ Prototype.
@<Exported Prototypes@>+=
int vt100_GetLine(char *input, int maxchars );


@ |vt100_GetChar()|.
@<Exported Functions@>+=
char vt100_GetChar() {
  char c;
  int err;
  err = read( scr_ifd, &c, 1 );
  return c;
}
@ Prototype.
@<Exported Prototypes@>+=
char vt100_GetChar();


@ and
@<Exported Functions@>+=
int vt100_SelectLine( char *input, int maxchars, int timeout ) {
  char c;
  fd_set readfds;
  struct timeval tv;
  int i, n, err, count = 0;

  for( i = 0; i < maxchars; i++ ) input[ i ] = EOS;
  vt100_Reverse();
  tv.tv_sec = timeout;
  tv.tv_usec = 0;
  
  while( count < maxchars ) {
    FD_ZERO( &readfds );
    FD_SET( scr_ifd, &readfds );
    n = select( 1, &readfds, NULL, NULL, &tv );
    if( !n ) {
      vt100_Normal();
      for( i = 0; i < maxchars ; i++ ) input[ i ] = EOS;
      return 0;
    }
    if( FD_ISSET( scr_ifd, &readfds ) ) {
      err = read( scr_ifd, &c, 1 );
      @<Process Input@>@;
      err = write( scr_ofd, &c, 1 );
      count++ ;
      @<Test for Error@>@;
      tv.tv_sec = timeout;
      tv.tv_usec = 0;
    }
  }
  return count;
}
@ Prototype.
@<Exported Prototypes@>+=
int vt100_SelectLine( char *input, int maxchars, int timeout );


@ and
@<Exported Functions@>+=
int vt100_GetPasswd( char *input, int maxchars ) {
  char c;
  int err, count = 0;
  int i;
  for( i = 0; i < maxchars; i++ ) input[ i ] = EOS;
  vt100_Reverse();
  while( count < maxchars ) {
    err = read( scr_ifd, &c, 1 );
    @<Process Input@>@;
    err = write( scr_ofd, "*", 1 );    
    count++;
    @<Test for Error@>@;
  }
  vt100_Normal();
  return count;
}
@ Prototype.
@<Exported Prototypes@>+=
int vt100_GetPasswd( char *input, int maxchars );



@ Process Input.
@<Process Input@>= {
  if( err <= -1 ) {
    Trace( "port was lost" );
    exit( 1 );
  }
  if( c == 0x0D ) {
    vt100_Normal();
    return count;
  }
  if( c == 0x0A ) {
    vt100_Normal();
    return count;
  }
  if( c < 0x20 ) continue ;
  if( c == BS ) {
    if( count ) {
      char buffer[ 48 ];
      int len;

      len = sprintf( buffer, "%c[0m\b \b%c[7m", 033, 033 );
      err = write( scr_ifd, buffer, len );
      input--;
      count--;
      @<Test for Error@>@;
      *input = EOS;
    }
    continue;
  }
  if( c ) {
    *input++ = c;
    *input = EOS;
  }
}



@ After each read or write, we test for errors.
@<Test for Error@>= {
  if( err < 0 ) {
    Alert( "Error [%d] on read/write to socket", errno );
    exit( 1 );
  }
}


@* Index.
