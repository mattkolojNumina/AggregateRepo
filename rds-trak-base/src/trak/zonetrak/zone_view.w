%
%   zone_view.w -- ZoneTRAK live-look utility
%
%   Author: Adam Marshall
%
%   History:
%      2012-01-24 -AHM- init
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
%     (C) Copyright 2012 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{zone\_view.w}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program displays zone-related information on a single screen,
updating periodically.

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
\centerline{Author: Adam Marshall}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2014/08/07 15:16:15 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2012 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program displays zone-related information on a single screen,
updating periodically.

@c
static char rcsid[] = "$Id: zone_view.w,v 1.1 2014/08/07 15:16:15 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   @<declare vars@>@;
   @<initialize@>@;

   for ( ; ; usleep( UPDATE_TIME * 1000 )) {
      @<update zone@>@;
   }

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>    // for time() and ctime()

#include <zonetrak.h>
#include <rds_vt100.h>
#include <rds_trak.h>


@ Definitions.
@<Defines@>+=
#define UPDATE_TIME       500  // update time (msec)
#define SCREEN_WIDTH       80  // screen width (number of columns)
#define COL                 3  // print column
#define BUF_LEN            32  // length of small statically allocated strings


@ Declare variables for control and display.
@<declare vars@>=
char zone[ BUF_LEN + 1 ];
int zone_h = -1;


@ Initialization.  Register for tracing and initialize global variables.
@<initialize@>=
{
   if (argc != 2) {
      printf( "usage: %s <zone name>\n", argv[0] );
      exit( EXIT_SUCCESS );
   }

   strncpy( zone, argv[1], BUF_LEN );  zone[ BUF_LEN ] = '\0';
   zone_h = dp_handle( zone );
   if (zone_h < 0) {
      printf( "unable to locate dp for zone %s\n", zone );
      exit( EXIT_FAILURE );
   }

   vt100_Open( 0, 1 );
   vt100_Clear();
}


@ Update.  Display current information for the zone.
@<update zone@>=
{
   int val, c;
   int row = 0;
   time_t t = time( NULL );
   int mode;

   dp_registerget( zone_h, REG_MODE, &mode );

   vt100_Print( row++, 0, "%*s", SCREEN_WIDTH, ctime( &t ) );
   vt100_Bold();
   vt100_Print( row++, 0, "%s", center_line( zone ) );
   vt100_Normal();
   vt100_Print( row++, 0, "%s", fill_line( '-' ) );

   vt100_Print( row++, COL, "zone: %s", print_zone( zone_h ) );

   dp_registerget( zone_h, REG_NEXT, &val );
   if (val > 0) vt100_Print( row++, COL, "next: %s", print_zone( val ) );

   dp_registerget( zone_h, REG_LEFT, &val );
   if (val > 0) {
      int next;
      dp_registerget( val, REG_NEXT, &next );
      vt100_Print( row++, COL, "left: %s", print_zone( next ) );
   }

   dp_registerget( zone_h, REG_RIGHT, &val );
   if (val > 0) {
      int next;
      dp_registerget( val, REG_NEXT, &next );
      vt100_Print( row++, COL, "right: %s", print_zone( next ) );
   }

   vt100_Print( row++, 0, "%s", fill_line( '-' ) );
   c = 0;

   dp_registerget( zone_h, REG_EYE, &val );
   if (val > 0) vt100_Print( row, col(c++), "eye: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_MOTOR, &val );
   if (val > 0) vt100_Print( row, col(c++), "motor: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_INPUT, &val );
   if (val > 0) vt100_Print( row, col(c++), "input: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_OUTPUT, &val );
   if (val > 0) vt100_Print( row, col(c++), "output: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_JAMEYE, &val );
   if (val > 0) vt100_Print( row, col(c++), "jameye: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_ACTIVE, &val );
   if (val > 0) vt100_Print( row, col(c++), "active: %s", print_dp( val ) );
   if (c > 0) { row++; c = 0; }

   dp_registerget( zone_h, REG_ACTIVE_EYE, &val );
   if (val > 0) vt100_Print( row, col(c++), "active eye: %s", print_dp( val ) );
   if (c > 0) { row++; c = 0; }

   dp_registerget( zone_h, REG_DEACT, &val );
   if (val > 0) vt100_Print( row, col(c++), "deact: %s", print_dp( val ) );
   if (c > 0) { row++; c = 0; }

   dp_registerget( zone_h, REG_DEACT_EYE, &val );
   if (val > 0) vt100_Print( row, col(c++), "deact eye: %s", print_dp( val ) );
   if (c > 0) { row++; c = 0; }

   dp_registerget( zone_h, REG_TIMER, &val );
   vt100_Print( row, col(c++), "timer: %lu", val );
   if (c > 0) { row++; c = 0; }

   vt100_Print( row++, 0, "%s", fill_line( '-' ) );

// TODO mode

   dp_registerget( zone_h, REG_RUN, &val );
   if (val > 0) vt100_Print( row, col(c++), "run: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RESET, &val );
   if (val > 0) vt100_Print( row, col(c++), "reset: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_FAULT, &val );
   if (val > 0) vt100_Print( row, col(c++), "fault: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_ZONEFAULT, &val );
   if (val > 0) vt100_Print( row, col(c++), "zonefault: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_DEBUG, &val );
   if (val > 0) vt100_Print( row, col(c++), "debug: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_HOLD, &val );
   if (val > 0) vt100_Print( row, col(c++), "hold: %s", print_dp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_GO, &val );
   if (val > 0) vt100_Print( row, col(c++), "go: %s", print_dp( val ) );
   if (c > 0) { row++; c = 0; }

   if (mode & (1L << MODE_CREATE)) {
      int first, last, current;
      dp_registerget( zone_h, REG_FIRST, &first );
      dp_registerget( zone_h, REG_LAST, &last );
      dp_registerget( zone_h, REG_CURRENT, &current );
      vt100_Print( row++, COL, "box creation: %d < %d > %d",
            first, current, last );
   }

   vt100_Print( row++, 0, "%s", fill_line( '-' ) );

   dp_registerget( zone_h, REG_RUNUP, &val );
   if (val > 0) vt100_Print( row, col(c++), "runup: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RUNIN, &val );
   if (val > 0) vt100_Print( row, col(c++), "runin: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RUNOVER, &val );
   if (val > 0) vt100_Print( row, col(c++), "runover: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RUNOUT, &val );
   if (val > 0) vt100_Print( row, col(c++), "runout: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RUNOFF, &val );
   if (val > 0) vt100_Print( row, col(c++), "runoff: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_RUNDOWN, &val );
   if (val > 0) vt100_Print( row, col(c++), "rundown: %s", print_rp( val ) );
   if (c > 0) { row++; c = 0; }

// TODO out-state

   dp_registerget( zone_h, REG_TM_MIN, &val );
   if (val > 0) vt100_Print( row, col(c++), "min: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_MAX, &val );
   if (val > 0) vt100_Print( row, col(c++), "max: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_CLEAR, &val );
   if (val > 0) vt100_Print( row, col(c++), "clear: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_CHOOSE, &val );
   if (val > 0) vt100_Print( row, col(c++), "choose: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_WAIT, &val );
   if (val > 0) vt100_Print( row, col(c++), "wait: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_SLUG, &val );
   if (val > 0) vt100_Print( row, col(c++), "slug: %s", print_rp( val ) );
   if (c > 1) { row++; c = 0; }

   dp_registerget( zone_h, REG_TM_JAM, &val );
   if (val > 0) vt100_Print( row, col(c++), "jam: %s", print_rp( val ) );
   if (c > 0) { row++; c = 0; }

   vt100_Print( row++, 0, "%s", fill_line( '-' ) );
}


@ Print status text for a zone, including name, box, and state.
@<Functions@>+=
char * print_zone( int dp ) {
   static char text[ SCREEN_WIDTH + 1 ];

   int box, state;

   strcpy( text, "" );
   if (dp <= 0)
      return text;

   dp_registerget( dp, REG_BOX, &box );
   dp_registerget( dp, REG_STATE, &state );

   snprintf( text, SCREEN_WIDTH,
         "%s, box = %d, state = %d (%s)",
         print_dp( dp ), box, state, get_state_name( state ) );
   text[ SCREEN_WIDTH ] = '\0';

   return text;
}
@ Prototype the function.
@<Prototypes@>+=
char * print_zone( int dp );


@ Get the state name.
@<Functions@>+=
char * get_state_name( int state ) {
   static char text[ SCREEN_WIDTH + 1 ];

   strcpy( text, "" );
   switch (state) {
      case STATE_INIT:    strcpy( text, "INIT" );    break;
      case STATE_IDLE:    strcpy( text, "IDLE" );    break;
      case STATE_FILL:    strcpy( text, "FILL" );    break;
      case STATE_FILL_X:  strcpy( text, "FILL_X" );  break;
      case STATE_FULL:    strcpy( text, "FULL" );    break;
      case STATE_DRAIN_X: strcpy( text, "DRAIN_X" ); break;
      case STATE_DRAIN:   strcpy( text, "DRAIN" );   break;
      case STATE_RUNUP:   strcpy( text, "RUNUP" );   break;
      case STATE_CHOOSE:  strcpy( text, "CHOOSE" );  break;
      case STATE_ACTIVE:  strcpy( text, "ACTIVE" );  break;
      case STATE_DEACT:   strcpy( text, "DEACT" );   break;
      case STATE_FILL_O:  strcpy( text, "FILL_O" );  break;
      case STATE_DRAIN_O: strcpy( text, "DRAIN_O" ); break;
      case STATE_FAULT:   strcpy( text, "FAULT" );   break;
   }

   return text;
}
@ Prototype the function.
@<Prototypes@>+=
char * get_state_name( int state );


@ Print status text for a dp, including name and current value.
@<Functions@>+=
char * print_dp( int dp ) {
   static char text[ BUF_LEN + 1 ];

   char name[ TRAK_NAME_LEN + 1 ];
   char desc[ TRAK_DATA_LEN + 1 ];
   char dev[ TRAK_NAME_LEN + 1 ];
   char data[ TRAK_DATA_LEN + 1 ];
   int val;

   strcpy( text, "" );
   if (dp <= 0) {
      strcpy( text, "(undefined)" );
      return text;
   }

   val = dp_settings( dp, name, desc, dev, data );
   if (val == NIL) {
      strcpy( text, "(error)" );
      return text;
   }

   val = dp_get( dp );

   snprintf( text, BUF_LEN, "%s -> %s", name, (val) ? "ON" : "OFF" );
   text[ BUF_LEN ] = '\0';

   return text;
}
@ Prototype the function.
@<Prototypes@>+=
char * print_dp( int dp );


@ Print status text for a rp, including name and current value.
@<Functions@>+=
char * print_rp( int rp ) {
   static char text[ BUF_LEN + 1 ];

   char name[ TRAK_NAME_LEN + 1 ];
   char desc[ TRAK_DATA_LEN + 1 ];
   char dev[ TRAK_NAME_LEN + 1 ];
   char data[ TRAK_DATA_LEN + 1 ];
   int val;

   strcpy( text, "" );
   if (rp <= 0) {
      strcpy( text, "(undefined)" );
      return text;
   }

   val = rp_settings( rp, name, desc, dev, data );

   snprintf( text, BUF_LEN, "%s = %d", name, val );
   text[ BUF_LEN ] = '\0';

   return text;
}
@ Prototype the function.
@<Prototypes@>+=
char * print_rp( int rp );


@ Get the print column.
@<Functions@>+=
int col( int index ) {
   return (index == 0) ? COL : COL + SCREEN_WIDTH / 2;
}
@ Prototype the function.
@<Prototypes@>+=
int col( int index );


@ Fill a line with a single character, repeated.
@<Functions@>+=
char * fill_line( char c ) {
   static char line[ SCREEN_WIDTH + 1 ];
   int i;

   for (i = 0; i < SCREEN_WIDTH; i++)
      line[i] = c;
   line[ SCREEN_WIDTH ] = '\0';

   return line;
}
@ Prototype the function.
@<Prototypes@>+=
char * fill_line( char c );


@ Center a string within the screen.
@<Functions@>+=
char * center_line( const char s[] ) {
   static char line[ SCREEN_WIDTH + 1 ];
   int i;
   int n = (SCREEN_WIDTH - strlen( s )) / 2;

   strcpy( line, "" );
   for (i = 0; i < n; i++)
      line[i] = ' ';
   strncpy( line + i, s, SCREEN_WIDTH );
   line[ SCREEN_WIDTH ] = '\0';

   return line;
}
@ Prototype the function.
@<Prototypes@>+=
char * center_line( const char s[] );


@* Index.
