%
%   diag.w -- backend app to update database-driven diagnostics
%
%   Author: Adam Marshall
%
%   History:
%      2008-03-05 -AHM- init, for Numina R&D Loop
%      2008-05-13 -AHM- modified for Trane (Southaven, MS) to include
%                       TRAK-based diagnostics
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
%     (C) Copyright 2008 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{diag}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program polls the ControlLogix PLC for diagnostics information and
updates the corresponding database records.

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
\centerline{Control Date: $ $Date: 2024/06/05 19:37:01 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2008 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program polls the CompactLogix PLC for diagnostics information and
updates the corresponding database records.

@c
static char rcsid[] = "$Id: diag.w,v 1.1 2024/06/05 19:37:01 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   @<initialize@>@;

   while (TRUE) {
      @<poll and update@>@;
      usleep( SLEEP_DURATION * 1000 );
   }

   @<cleanup@>@;
   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>  // for signal handling
#include <time.h>    // for time()

#include <app.h>
#include <rds_evt.h>
#include <rds_logix.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // length of small statically allocated strings
#define TXT_LEN         128  // length of longer text strings
#define SLEEP_DURATION  500  // sleep time btwn processing cycles (msec)
#define DEFAULT_TXT     "(default)"  // indicator for default text value


/* structure for holding information about a single web object */
typedef struct {
   char name[ BUF_LEN + 1 ];
   char type[ BUF_LEN + 1 ];
   unsigned long value;
   unsigned long secondary_value;
   char on_text[ TXT_LEN + 1 ];
   char secondary_on_text[ TXT_LEN + 1 ];
   char off_text[ TXT_LEN + 1 ];
   int changed;
} web_object_t;


/* structure for holding information about a single trak object */
typedef struct {
   int dp_handle;
   int dp_register;
   char type[ BUF_LEN + 1 ];
   unsigned long value;
   unsigned long active;
   web_object_t *web;
} trak_object_t;


/* structure for holding information about a single plc object */
typedef struct {
   int word;
   int bit;
   char type[ BUF_LEN + 1 ];
   unsigned long value;
   web_object_t *web;
} plc_object_t;


@ Global variables to hold general application info.
@<Globals@>+=
char app_name[ BUF_LEN + 1 ];
char hostname[ BUF_LEN + 1 ];
int plc_enabled = TRUE;
int trak_enabled = TRUE;


@ Initialization.
@<initialize@>+=
{
   if (argc != 1) {
      printf( "usage: %s\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   signal( SIGPIPE, sigpipe_handler );
   signal( SIGTERM, sigterm_handler );

   strcpy( app_name, "diag" );

   trn_register( app_name );
   Trace( "init" );
   Inform( rcsid );

   @<setup variables@>@;
   @<create objects@>@;

   if (plc_enabled && trak_enabled)
      Trace( "polling plc and trak objects for diagnostics" );
   else if (plc_enabled)
      Trace( "polling plc objects for diagnostics" );
   else if (trak_enabled)
      Trace( "polling trak objects for diagnostics" );
   else
      Alert( "neither plc nor trak is configured for diagnostics polling" );
}

@ Obtain program settings from the database.
@<setup variables@>=
{

   gethostname( hostname, BUF_LEN );
   hostname[ BUF_LEN ] = '\0';

   strncpy( plc_name, util_get_control( app_name, "plc_name", "" ), BUF_LEN );
   plc_name[ BUF_LEN ] = '\0';
   strncpy( plc_file, util_get_control( app_name, "plc_file", "" ), BUF_LEN );
   plc_file[ BUF_LEN ] = '\0';

   if (strlen( plc_name ) == 0 || strlen( plc_file ) == 0) {
      Trace( "no plc name/file specified" );
      plc_enabled = FALSE;
   }
}


@ Cleanup, freeing allocated objects.
@<cleanup@>=
{
   free( trak_objects );
   trak_objects = NULL;

   free( plc_objects );
   plc_objects = NULL;

   free( web_objects );
   web_objects = NULL;

   free( plc_data );
   plc_data = NULL;
}


@* Data Initialization.

@ Globals for data storage.
@<Globals@>+=
web_object_t *web_objects;
int num_web_objects;
plc_object_t *plc_objects;
int num_plc_objects;
trak_object_t *trak_objects;
int num_trak_objects;
unsigned long *plc_data;
int plc_data_length;


@ Create the local data structures, based on records in the associated
database tables.
@<create objects@>=
{
   Inform( "creating and initializing objects" );

   @<create web objects@>@;
   @<create plc objects@>@;
   @<create trak objects@>@;

   Inform( "initialization complete" );
}


@ Scan the database for web objects.  Allocate and initialize.
@<create web objects@>=
{
   int err, i;

   // scan db for info
   err = sql_query(
         "SELECT name, type FROM webObjects" );
   if (err) {
      Alert( "sql error [%d] retrieving web objects, exit", err );
      exit( EXIT_FAILURE );
   }

   num_web_objects = sql_rowcount();
   Inform( "%d web objects found", num_web_objects );

   // allocate
   web_objects = malloc( num_web_objects * sizeof *web_objects );
   if (web_objects == NULL) {
      Alert( "unable to allocate memory for web objects, exit" );
      exit( EXIT_FAILURE );
   }

   // initialize
   for (i = 0; i < num_web_objects; i++) {
      web_object_t *obj = &web_objects[i];
      strncpy( obj->name, sql_get( i, 0 ), BUF_LEN );
      (obj->name)[ BUF_LEN ] = '\0';
      strncpy( obj->type, sql_get( i, 1 ), BUF_LEN );
      (obj->type)[ BUF_LEN ] = '\0';
      obj->value = 0L;
      obj->secondary_value = 0L;
      strcpy( obj->on_text, DEFAULT_TXT );
      strcpy( obj->secondary_on_text, DEFAULT_TXT );
      strcpy( obj->off_text, DEFAULT_TXT );
      obj->changed = FALSE;
   }
}


@ Scan the database for plc objects.  Allocate and initialize.  Also,
create the plc data-storage block.
@<create plc objects@>=
{
   char *val;
   int err, i;
   int word_max;

   if (plc_enabled) {
      // scan db for plc object info
      val = sql_getvalue( "SHOW TABLES LIKE 'plcObjects'" );
      if (!val || strcmp( val, "plcObjects" ) != 0) {
         Trace( "no plc objects table found" );
         plc_enabled = FALSE;
      } else {
         err = sql_query(
               "SELECT word, bit, name, type FROM plcObjects "
               "WHERE plc = '%s' "
               "AND file = '%s'",
               plc_name, plc_file );
         if (err) {
            Alert( "sql error [%d] retrieving plc objects", err );
            plc_enabled = FALSE;
         } else {
            num_plc_objects = sql_rowcount();
            Inform( "%d plc objects found for %s/%s",
                  num_plc_objects, plc_name, plc_file );
            if (num_plc_objects == 0)
               plc_enabled = FALSE;
         }
      }
   }

   if (plc_enabled) {
      // allocate
      plc_objects = malloc( num_plc_objects * sizeof *plc_objects );
      if (plc_objects == NULL) {
         Alert( "unable to allocate memory for plc objects, exit" );
         exit( EXIT_FAILURE );
      }

      // initialize
      word_max = 0;
      for (i = 0; i < num_plc_objects; i++) {
         plc_object_t *obj = &plc_objects[i];

         obj->word = atoi( sql_get( i, 0 ) );
         if (obj->word > word_max)
            word_max = obj->word;

         obj->bit  = atoi( sql_get( i, 1 ) );
         strncpy( obj->type, sql_get( i, 3 ), BUF_LEN );
         (obj->type)[ BUF_LEN ] = '\0';

         // initialize to an invalid value to force updates on start
         obj->value = -1L;

         // link to the corresponding web object
         obj->web = get_web_object( sql_get( i, 2 ) );
      }

      // create the plc data storage block
      plc_data_length = word_max + 1;
      Inform( "plc data length = %d words for %s/%s",
            plc_data_length, plc_name, plc_file );
      plc_data = malloc( plc_data_length * sizeof *plc_data );
      if (plc_data == NULL) {
         Alert( "unable to allocate memory for plc data, exit" );
         exit( EXIT_FAILURE );
      }
   }
}


@ Scan the database for trak objects.  Allocate and initialize.
@<create trak objects@>=
{
   char *val;
   int err, i;

   // scan db for trak object info
   val = sql_getvalue( "SHOW TABLES LIKE 'trakObjects'" );
   if (!val || strcmp( val, "trakObjects" ) != 0) {
      Trace( "no trak objects table found" );
      trak_enabled = FALSE;
   } else {
      err = sql_query(
            "SELECT dp, register, name, type, onText, offText, active "
            "FROM trakObjects " 
            "WHERE host = '%s'",
            hostname );
      if (err) {
         Alert( "sql error [%d] retrieving trak objects", err );
         trak_enabled = FALSE;
      } else {
         num_trak_objects = sql_rowcount();
         Inform( "%d trak objects found", num_trak_objects );
         if (num_trak_objects == 0)
            trak_enabled = FALSE;
      }
   }

   if (trak_enabled) {
      // allocate
      trak_objects = malloc( num_trak_objects * sizeof *trak_objects );
      if (trak_objects == NULL) {
         Alert( "unable to allocate memory for trak objects, exit" );
         exit( EXIT_FAILURE );
      }

      // initialize
      for (i = 0; i < num_trak_objects; i++) {
         trak_object_t *obj = &trak_objects[i];
         web_object_t *web;

         obj->dp_handle = dp_handle( sql_get( i, 0 ) );
         obj->dp_register = atoi( sql_get( i, 1 ) );
         strncpy( obj->type, sql_get( i, 3 ), BUF_LEN );
         (obj->type)[ BUF_LEN ] = '\0';
         obj->active = atoi( sql_get( i, 6 ) );

         // initialize to an invalid value to force updates on start
         obj->value = -1L;

         // update and link to the corresponding web object
         web = get_web_object( sql_get( i, 2 ) );
         if (web != NULL) {
            char *txt = sql_get( i, 4 );
            if (txt != NULL) {
               if (strcmp( obj->type, "motor_run" ) == 0) {
                  strncpy( web->secondary_on_text, txt, TXT_LEN );
                  (web->secondary_on_text)[ TXT_LEN ] = '\0';
               } else {
                  strncpy( web->on_text, txt, TXT_LEN );
                  (web->on_text)[ TXT_LEN ] = '\0';
               }
            }

            txt = sql_get( i, 5 );
            if (txt != NULL) {
               strncpy( web->off_text, sql_get( i, 5 ), TXT_LEN );
               (web->off_text)[ TXT_LEN ] = '\0';
            }
         }
         obj->web = web;
      }
   }
}


@ Get a reference to a web object, by name.  If the object doesn't exist,
return NULL.
@<Functions@>+=
web_object_t * get_web_object( const char name[] ) {
   int i;

   // search for a web object matching the name
   for (i = 0; i < num_web_objects; i++)
      if (strcmp( web_objects[i].name, name ) == 0)
         return &web_objects[i];

   return NULL;
}
@ Prototype the function.
@<Prototypes@>+=
web_object_t * get_web_object( const char name[] );


@* Processing.

@ Poll the PLC and/or TRAK for data and update any objects whose values have
changed.
@<poll and update@>=
{
   if (plc_enabled)
      poll_plc();

   if (trak_enabled)
      poll_trak();

   @<update web objects@>@;
}


@ Obtain diagnostics data from the PLC.
@<Functions@>+=
void poll_plc( void ) {
   int err, i;

   // check the connection to the plc
   if (!plc_attach())
      return;

   // read the block of data from the plc
   err = logix_read( plc_h, plc_file, 0, plc_data_length, LOGIX_DINT,
         plc_data );
   if (err) {
      Alert( "error [%d] reading plc data, detach", err );
      plc_detach();
      return;
   }

   // update the plc objects
   for (i = 0; i < num_plc_objects; i++) {
      plc_object_t *obj = &plc_objects[i];
      unsigned long value;

      // determine the new value, either a single bit or the whole word
      if (obj->bit < 0)
         value = plc_data[ obj->word ];
      else
         value = plc_data[ obj->word ] & (1 << obj->bit);

      // update the web object if the value has changed
      if (obj->value != value) {
         obj->value = value;
         set_web_value( obj->web, obj->type, obj->value );
      }
   }
}
@ Prototype the function.
@<Prototypes@>+=
void poll_plc( void );


@ Obtain diagnostics data from TRAK.
@<Functions@>+=
void poll_trak( void ) {
   int i;

   // make sure trak is running
   if (!dp_get( 0 ))
      return;

   // update the trak objects
   for (i = 0; i < num_trak_objects; i++) {
      trak_object_t *obj = &trak_objects[i];
      int raw, value;

      if (obj->dp_handle < 0)
         continue;

      // determine the new value, either a single register or the dp value
      if (obj->dp_register >= 0)
         dp_registerget( obj->dp_handle, obj->dp_register, &raw );
      else
         raw = dp_get( obj->dp_handle );
      value = (raw == obj->active) ? 1 : 0;
       
      // update the web object if the value has changed
      if (obj->value != value) {
         obj->value = value;
         set_web_value( obj->web, obj->type, obj->value );
      }
   }
}
@ Prototype the function.
@<Prototypes@>+=
void poll_trak( void );



@ Update the database entry for each web object that has changed.
@<update web objects@>=
{
   int i;

   // update the web objects marked as changed
   for (i = 0; i < num_web_objects; i++) {
      web_object_t *obj = &web_objects[i];
      if (obj->changed) {
         update_web_object( obj );
         obj->changed = FALSE;
      }
   }
}


@* Web Object Updates.

@ Update one of the values of a web object, based on the type of value.
@<Functions@>+=
void set_web_value( web_object_t *obj, char type[], unsigned long value ) {
   if (obj == NULL)
      return;

   if (strcmp( type, "motor_run" ) == 0)
      obj->secondary_value = value;
   else
      obj->value = value;

   obj->changed = TRUE;
}
@ Prototype the function.
@<Prototypes@>+=
void set_web_value( web_object_t *obj, char type[], unsigned long value );


@ Update a web object, based on its type.
@<Functions@>+=
void update_web_object( web_object_t *obj ) {
   char web_value[ BUF_LEN + 1 ];
   char web_hint[ TXT_LEN + 1 ];

   strcpy( web_value, "" );
   strcpy( web_hint, "" );

   if (strcmp( obj->type, "estop" ) == 0)
      @<update estop@>@;
   else if (strcmp( obj->type, "fault" ) == 0)
      @<update general fault@>@;
   else if (strcmp( obj->type, "full" ) == 0)
      @<update line full@>@;
   else if (strcmp( obj->type, "jam" ) == 0)
      @<update jam@>@;
   else if (strcmp( obj->type, "motor" ) == 0)
      @<update motor@>@;
   else if (strcmp( obj->type, "status" ) == 0)
      @<update status@>@;
   else if (strcmp( obj->type, "counter" ) == 0) {
      // set the text to the value of the counter
      sprintf( web_value, "%lu", obj->value );
   } else
      @<update default@>@;

   sql_query(
         "UPDATE webObjects SET "
         "value = '%s', "
         "hint = '%s' "
         "WHERE name = '%s'",
         web_value, web_hint, obj->name );
}
@ Prototype the function.
@<Prototypes@>+=
void update_web_object( web_object_t *obj );


@ Set/clear an emergency stop fault.
@<update estop@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "fault" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "e-stop %s activated", obj->name );
      else
         sprintf( web_hint, obj->on_text );
      evt_start( obj->name );
   } else {
      strcpy( web_value, "" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         strcpy( web_hint, "" );
      else
         sprintf( web_hint, obj->off_text );
      evt_stop( obj->name );
   }
}


@ Set/clear a general fault.
@<update general fault@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "fault" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "general fault %s activated", obj->name );
      else
         sprintf( web_hint, obj->on_text );
      evt_start( obj->name );
   } else {
      strcpy( web_value, "" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         strcpy( web_hint, "" );
      else
         sprintf( web_hint, obj->off_text );
      evt_stop( obj->name );
   }
}


@ Set/clear a line full condition.
@<update line full@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "full" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "line full at %s", obj->name );
      else
         sprintf( web_hint, obj->on_text );
   } else {
      strcpy( web_value, "" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         strcpy( web_hint, "" );
      else
         sprintf( web_hint, obj->off_text );
   }
}


@ Set/clear a photoeye jam.
@<update jam@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "fault" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "photoeye %s jammed", obj->name );
      else
         sprintf( web_hint, obj->on_text );
      evt_start( obj->name );
   } else {
      strcpy( web_value, "" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         strcpy( web_hint, "" );
      else
         sprintf( web_hint, obj->off_text );
      evt_stop( obj->name );
   }
}


@ Update the state of a motor: running, faulted, or off.  For motors, the
primary value indicates the presence of a motor overload fault; the
secondary value indicates the motor's run status.
@<update motor@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "fault" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "motor %s faulted", obj->name );
      else
         sprintf( web_hint, obj->on_text );
      evt_start( obj->name );
   } else if (obj->secondary_value > 0) {
      strcpy( web_value, "run" );
      if (strcmp( obj->secondary_on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "motor %s running", obj->name );
      else
         sprintf( web_hint, obj->secondary_on_text );
      evt_stop( obj->name );
   } else {
      strcpy( web_value, "" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "motor %s stopped", obj->name );
      else
         sprintf( web_hint, obj->off_text );
      evt_stop( obj->name );
   }
}


@ Set/clear a status bit.
@<update status@>=
{
   if (obj->value > 0) {
      strcpy( web_value, "on" );
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "%s on", obj->name );
      else
         sprintf( web_hint, obj->on_text );
   } else {
      strcpy( web_value, "off" );
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         sprintf( web_hint, "%s off", obj->name );
      else
         sprintf( web_hint, obj->off_text );
   }
}


@ Update a default web object
@<update default@>=
{
   if (obj->value > 0) {
      if (strcmp( obj->on_text, DEFAULT_TXT ) == 0)
         sprintf( web_value, "", obj->name );
      else
         sprintf( web_value, obj->on_text );
   } else {
      if (strcmp( obj->off_text, DEFAULT_TXT ) == 0)
         sprintf( web_value, "", obj->name );
      else
         sprintf( web_value, obj->off_text );
   }
}


@* PLC Connection.

@ Definitions for plc communications.
@<Defines@>+=
#define RETRY_TIME  10  // time btwn connection retry attempts (sec)

@ Global variables to hold plc information.
@<Globals@>+=
char plc_name[ BUF_LEN + 1 ];
char plc_file[ BUF_LEN + 1 ];
plc_handle plc_h = NULL;

@ Open a connection to the PLC, if necessary.
@<Functions@>+=
int plc_attach( void ) {
   static time_t attempt_time = 0;
   time_t current_time;

   if (plc_h)
      return TRUE;

   current_time = time( NULL );
   if (current_time - attempt_time < RETRY_TIME)
      return FALSE;
   attempt_time = current_time;

   plc_h = logix_open( plc_name );
   if (plc_h) {
      Trace( "connected to [%s], session = %lu", plc_name, plc_h->session_id );
      return TRUE;
   }

   Alert( "failed to open connection to [%s]", plc_name );
   return FALSE;
}

@ Close the connection to the PLC.
@<Functions@>+=
void plc_detach( void ) {
   if (plc_h)
      logix_close( plc_h );
   plc_h = NULL;
}

@ Prototype the functions.
@<Prototypes@>+=
int  plc_attach( void );
void plc_detach( void );


@* Utility functions.

@ Signal handler for SIGPIPE.
@<Functions@>+=
static void sigpipe_handler( int sig ) {
   Inform( "SIGPIPE detected -- detach from plc" );
   plc_detach();
   signal( SIGPIPE, sigpipe_handler );
}
@ Prototype the function.
@<Prototypes@>+=
static void sigpipe_handler( int sig );

@ Signal handler for SIGTERM.
@<Functions@>+=
static void sigterm_handler( int sig ) {
   Inform( "SIGTERM detected -- detach and exit" );
   plc_detach();
   exit( EXIT_SUCCESS );
}
@ Prototype the function.
@<Prototypes@>+=
static void sigterm_handler( int sig );


@* Index.
