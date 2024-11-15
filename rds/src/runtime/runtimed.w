%
%   runtimed.w - map runtime variables from database to tags
%
%   Author: Adam Marshall
%
%   History:
%      2011-01-05 -AHM- init, from MRW's runtimed for DHL
%
%
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Group.
%
%
%
%
%
%     (c) 2011, Numina Group, Inc.  All Rights Reserved.
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
\def\title{XXX}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program maps the contents of the runtime database table into tags.

% --- confidentiality statement ---
\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.
It must not be disclosed to any person who does not have a current signed
non-disclosure form on file with Numina Group.
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
\centerline{Control Date: $ $Date: 2012/10/05 20:02:41 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011, Numina Group, Inc.
All Rights Reserved.}
}

@* Overview.
This program maps the contents of the runtime database table into tags.

@c
static char rcsid[] = "$Id: runtimed.w,v 1.1 2012/10/05 20:02:41 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {

   @<Initialize@>@;

   // main processing loop
   for ( ; ; usleep( CYCLE_PERIOD * 1000 )) {
      int err = mirror_runtime_values();
      if (err)
         usleep( ERROR_DELAY );
   }

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <rds_sql.h>
#include <rds_tag.h>
#include <rds_trn.h>


@ Definitions.
@<Defines@>+=
#define CYCLE_PERIOD  1000  // main loop period (msec)
#define ERROR_DELAY  10000  // delay following error (msec)


@ Initialization. Register for tracing and initialize global variables.
@<Initialize@>+=
{
   if (argc != 1) {
      printf( "usage: %s\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   trn_register( "runtimed" );
   Trace( "init" );
   Inform( rcsid );
}


@ Mirror the contents of the 'runtime' table into tags.
@<Functions@>+=
int mirror_runtime_values( void ) {
   int err;

   err = sql_query(
         "SELECT name, value FROM runtime" );
   if (err) {
      Alert( "SQL error %d in SELECT from runtime table", err );
      return err;
   }

   while( sql_rowfetch() ) {
      err = store_runtime( sql_rowget( 0 ), sql_rowget( 1 ) );
      if (err)
         return err;
   }

   return 0;  // no error
}
@ Prototype the function.
@<Prototypes@>+=
int mirror_runtime_values( void );


@ Store a single name/value pair in a tag.  The name is prefixed with
'/runtime' and a separator slash, if necessary.
@<Functions@>+=
int store_runtime( const char name[], const char value[] ) {
   int len, err;
   char *tag_key;

   if (strlen( name ) == 0)
      return 0;

   // allocate space for '/runtime/' + name + terminator
   len = strlen( name ) + 10;
   tag_key = malloc( len );
   if (tag_key == NULL) {
      Alert( "memory allocation failed for [%s]", name );
      return -1;
   }

   // build the tag key, with separator slash, if necessary
   strcpy( tag_key, "/runtime" );
   if (name[0] != '/')
      strcat( tag_key, "/" );
   strcat( tag_key, name );

   // insert the tag
   err = tag_insert( tag_key, value );
   if (err)
      Alert( "error %d in tag insertion for [%s]", err, name );

   free( tag_key );

   return err;
}
@ Prototype the function.
@<Prototypes@>+=
int store_runtime( const char name[], const char value[] );


@* Index.
