%
%   zonediag.w -- backend app to update database-driven diagnostics for
%                 trak zones
%
%   Author: Adam Marshall
%
%   History:
%      2010-08-17 -AHM- init, for x-press pal
%      2011-10-11 -AHM- updated for new ZoneTRAK message format
%      2018-09-28 -ANK/AHM- check/clear faults on start; include PFI types
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
%     (C) Copyright 2010-2011 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{zonediag}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program updates web diagnostics for zone-based TRAK systems.

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
\centerline{Control Date: $ $Date: 2024/06/05 19:47:18 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2010--2011 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program updates web diagnostics for zone-based TRAK systems.

@c
static char rcsid[] = "$Id: zone_diag.w,v 1.1 2024/06/05 19:47:18 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;


int main( int argc, char *argv[] ) {
   @<initialize@>@;

   for ( ; ; usleep( SLEEP_DURATION * 1000 )) {
      @<poll for messages@>@;
      @<update web objects@>@;
   }

   @<cleanup@>@;
   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <rds_evt.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>

#include <app.h>
#include <zonetrak.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN           32  // length of small statically allocated strings
#define MSG_LEN          128  
#define HINT_LEN          64  // length of hint for web object
#define SLEEP_DURATION   100  // sleep time btwn processing cycles (msec)


/* structure for holding information about a single web object */
typedef struct {
   char name[ BUF_LEN + 1 ];
   char type[ BUF_LEN + 1 ];
   int dp;
   char value[ BUF_LEN + 1 ];
   char hint[ HINT_LEN + 1 ];
   int faulted;
   int errcode;
   int changed;
} web_object_t;


@ Initialization.
@<initialize@>+=
{
   if (argc != 1) {
      printf( "usage: %s\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   trn_register( "zonediag" );
   Trace( "init" );
   Inform( rcsid );

   @<setup variables@>@;

   Inform( "creating and initializing objects" );
   @<create web objects@>@;
   Inform( "initialization complete" );
}


@ Obtain program settings from the database.
@<setup variables@>=
{
   zone_msg = (strcmp( "yes", util_get_control( "diag", "zoneMsg", "" ) ) == 0);
   if (zone_msg)
      Inform( "zone message tracing enabled" );
}


@ Cleanup, freeing allocated objects.
@<cleanup@>=
{
   free( web_objects );
   web_objects = NULL;
}



@* Data Initialization.

@ Globals for data storage.
@<Globals@>+=
web_object_t *web_objects;
int num_web_objects;
int zone_msg = FALSE;



@ Scan the database for web objects.  Allocate and initialize.
@<create web objects@>=
{
   int err, i;

   // scan db for info
   err = sql_query(
         "SELECT name, type FROM webObjects "
         "WHERE type IN ('zone','folder','servo')" );
   if (err) {
      Alert( "sql error [%d] retrieving web objects, exit", err );
      exit( EXIT_FAILURE );
   }

   num_web_objects = sql_rowcount();
   Inform( "%d web object(s) found", num_web_objects );

   // allocate
   web_objects = malloc( num_web_objects * sizeof *web_objects );
   if (web_objects == NULL) {
      Alert( "unable to allocate memory for web objects, exit" );
      exit( EXIT_FAILURE );
   }

   // initialize
   for (i = 0; i < num_web_objects; i++) {
      int dp;
      web_object_t *obj = &web_objects[i];

      strncpy( obj->name, sql_get( i, 0 ), BUF_LEN );
      (obj->name)[ BUF_LEN ] = '\0';
      strncpy( obj->type, sql_get( i, 1 ), BUF_LEN );
      (obj->type)[ BUF_LEN ] = '\0';

      obj->dp = dp_handle( obj->name );
      strcpy( obj->value, "" );
      strcpy( obj->hint, "" );
      obj->faulted = FALSE;
      obj->errcode = 0;
      obj->changed = TRUE;
   }
}


@ Get a reference to a web object, by dp handle.  If the object doesn't exist,
return NULL.
@<Functions@>+=
web_object_t * get_web_object( int dp ) {
   int i;

   if (dp < 0)
      return NULL;

   // search for a web object matching the dp handle
   for (i = 0; i < num_web_objects; i++)
      if (web_objects[i].dp == dp)
         return &web_objects[i];

   return NULL;
}
@ Prototype the function.
@<Prototypes@>+=
web_object_t * get_web_object( int dp );



@* Processing.

@ Poll for TRAK messages.
@<poll for messages@>=
{
   Mb_Result *msg;

   // make sure trak is running
   if (!dp_get( 0 ))
      continue;

   while ((msg = mb_poll()) != NULL) {
      int type, state;
      int msg_num = msg->data[0];

      if (msg_num == MSG_ZONE_STATE) {
         if (msg->count < 5) {
            Alert( "invalid zonetrak state msg cnt (%d)", msg->count );
         } else {
            handle_state_msg( msg->data[1], msg->data[2], msg->data[3],
                  msg->data[4] );
         }
      } else if (msg_num == MSG_ZONE_ERR) {
         if (msg->count < 5) {
            Alert( "invalid zonetrak error msg cnt (%d)", msg->count );
         } else {
            handle_error_msg( msg->data[1], msg->data[2], msg->data[3],
                  msg->data[4] );
         }
      }
#if 0
      else if (msg_num == MSG_ZONE_RESET) {
         if (msg->count < 5) {
            Alert( "invalid zonetrak reset msg cnt (%d)", msg->count );
         } else {
            handle_reset_msg( msg->data[1], msg->data[2], msg->data[3],
                  msg->data[4] );
         }
      }
#endif
      mb_free( msg );
   }
}


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


@ Update a zone in response to a state-change message.
@<Functions@>+=
void handle_state_msg( int zone_dp, int state, int box, int msec ) {
   web_object_t *obj;

   obj = get_web_object( zone_dp );
   if (obj == NULL)
      return;

   if (zone_msg)
      Inform( "[%s] %s, box %d at %lu msec%s",
            obj->name, GetStateString( obj->type, state ), box, msec,
            (obj->faulted) ? ", faulted" : "" );

   if (obj->faulted) {
      int fault_h = 0;
      if (strcasecmp( obj->type, "zone" ) == 0) {
         dp_registerget( obj->dp, REG_ZONEFAULT, &fault_h );
         if (fault_h > 0 && dp_get( fault_h ))
            return;  // don't update web object if faulted
      }
#if 0
      else if (strcasecmp( obj->type, "servo" ) == 0) {
         dp_registerget( obj->dp, REGS_FAULTDP, &fault_h );
         if (fault_h > 0 && dp_get( fault_h ))
            return;  // don't update web object if faulted
      } 
      else if (strcasecmp( obj->type, "folder" ) == 0) {
         if (state == SF_FAULT)
            return;  // don't update web object if faulted
      }
#endif
      clr_zone_fault( obj );
   }

   if (box == -1)
      set_web_values( obj, "", "" );
   else {
      if (strcasecmp( obj->type, "zone" ) == 0) {
         set_web_values( obj, "box", app_get_hint( box ) );
      } else if (strcasecmp( obj->type, "servo" ) == 0) {
         if( box <= 0 )
            set_web_values( obj, "", "" );
         else {
            char hint[ HINT_LEN + 1 ];
            snprintf( hint, sizeof( hint ) - 1, "servo position %d", box );
            set_web_values( obj, "box", hint );
         }
      } else if( strcasecmp( obj->type, "folder" ) == 0 ) {
         int iActualBox = box % 100;
         set_web_values( obj, "box", app_get_hint( iActualBox ) );
      } else
         set_web_values( obj, "box", app_get_hint( box ) );
   }
}
@ Prototype the function.
@<Prototypes@>+=
void handle_state_msg( int zone_dp, int state, int box, int msec );


@ Update a zone in response to an error message.
@<Functions@>+=
void handle_error_msg( int zone_dp, int code, int box, int msec ) {
   web_object_t *obj;

   obj = get_web_object( zone_dp );
   if (obj == NULL)
      return;

   Alert( "[%s] %s, box %d at %lu msec",
         obj->name, GetErrString( obj->type, code ), box, msec );

   if (!obj->faulted) {
      int fault_h = 0;

      if (strcasecmp( obj->type, "zone" ) == 0) {
         dp_registerget( obj->dp, REG_ZONEFAULT, &fault_h );
         if (fault_h > 0 && dp_get( fault_h ))
            set_zone_fault( obj, code );
      } 
#if 0
      else if (strcasecmp( obj->type, "servo" ) == 0) {
         dp_registerget( obj->dp, REGS_FAULTDP, &fault_h );
         if (fault_h > 0 && dp_get( fault_h ))
            set_zone_fault( obj, code );
      } 
#endif
        else
         set_zone_fault( obj, code );
   }
}
@ Prototype the function.
@<Prototypes@>+=
void handle_error_msg( int zone_dp, int code, int box, int msec );


@ Update a zone in response to a reset message.
@<Functions@>+=
void handle_reset_msg( int zone_dp, int code, int box, int msec ) {
   web_object_t *obj;

   obj = get_web_object( zone_dp );
   if (obj == NULL)
      return;

   if (zone_msg)
      Inform( "[%s] reset at %lu msec", obj->name, msec );

   clr_zone_fault( obj );
}
@ Prototype the function.
@<Prototypes@>+=
void handle_reset_msg( int zone_dp, int code, int box, int msec );


@ Declare a zone fault.
@<Functions@>+=
void set_zone_fault( web_object_t *obj, int code ) {
  char msg[ HINT_LEN + 1 ];
  char evt_name[ MSG_LEN + 1 ];

  if( obj == NULL ) return;
  obj->faulted = TRUE;
  obj->errcode = code;

  if( strcasecmp( obj->type, "zone" ) == 0 ) {
    if( code == ERR_UNEXPECTED ) strcpy( msg, "unexpected box detected" );
    else if( code == ERR_EARLY ) strcpy( msg, "box arrived early" );
    else if( code == ERR_MISSING ) strcpy( msg, "box missing" );
    else if( code == ERR_REMOVED ) strcpy( msg, "box removed" );
    else if( code == ERR_STUCK ) strcpy( msg, "box stuck" );
    else if( code == ERR_SENSOR ) strcpy( msg, "sensor error" );
    else if( code == ERR_LOGIC ) strcpy( msg, "control logic error" );
    else sprintf( msg, "zone fault %d", code );
  }
#if 0
  else if( strcasecmp( obj->type, "servo" ) == 0 ) {
    sprintf( msg, "servo fault %d", code );
  }
  else if( strcasecmp( obj->type, "folder" ) == 0 ) {
    if( code == ERF_PAPER_JAM ) strcpy( msg, "paper jam at folder machine" );
    else if( code == ERF_PAPER_LOST )
      strcpy( msg, "lost paper at folder machine" );
    else if( code == ERF_PRINTER_TIMEOUT )
      strcpy( msg, "printer timeout at folder machine" );
    else if( code == ERF_FOLDER_FAIL )
      strcpy( msg, "fault at folder machine" );
    else if( code == ERF_POCKET_FAULT )
      strcpy( msg, "pocket fault at folder machine" );
    else if( code == ERF_PAPER_MISFEED )
      strcpy( msg, "paper misfeed at folder machine" );
    else if( code == ERF_CHUTE_JAM )
      strcpy( msg, "chute jam at folder machine" );
    else if( code == ERF_SERVO_FAULT )
      strcpy( msg, "servo fault at folder machine" );
    else if( code == ERF_SETUP_FAIL )
      strcpy( msg, "initial setup fault at folder machine" );
    else if( code == ERF_YGRIP_FAIL )
      strcpy( msg, "Y-grip fault at folder machine" );
    else sprintf( msg, "folder fault %d", code );
  }
#endif

  Alert( "%s %s fault: %s", obj->type, obj->name, msg );
  set_web_values( obj, "fault", msg );

  sprintf( evt_name, "%s_fault", obj->name );
  evt_start( evt_name );
}


@ Clear a zone fault.
@<Functions@>+=
void clr_zone_fault( web_object_t *obj ) {
   char evt_name[ MSG_LEN + 1 ];

   if (obj == NULL)
      return;

   if (obj->faulted)
      Inform( "%s %s fault cleared", obj->type, obj->name );

   obj->faulted = FALSE;
   obj->errcode = 0;
   set_web_values( obj, "", "" );

   sprintf( evt_name, "%s_fault", obj->name );
   evt_stop( evt_name );
}
@ Prototype the functions.
@<Prototypes@>+=
void set_zone_fault( web_object_t *obj, int code );
void clr_zone_fault( web_object_t *obj );


@ Update the value and hint of a web object.
@<Functions@>+=
void set_web_values( web_object_t *obj, const char value[],
      const char hint[] ) {
   if (obj == NULL)
      return;

   if (strcmp( value, obj->value ) == 0 &&
         strcmp( hint, obj->hint ) == 0)
      return;

   strncpy( obj->value, value, BUF_LEN );
   obj->value[ BUF_LEN ] = '\0';
   strncpy( obj->hint, hint, HINT_LEN );
   obj->hint[ HINT_LEN ] = '\0';
   obj->changed = TRUE;
}
@ Prototype the function.
@<Prototypes@>+=
void set_web_values( web_object_t *obj, const char value[],
      const char hint[] );


@ Update a web object, based on its type.
@<Functions@>+=
void update_web_object( web_object_t *obj ) {
   sql_query(
         "UPDATE webObjects SET "
         "value = '%s', "
         "hint = '%s' "
         "WHERE name = '%s'",
         obj->value, obj->hint, obj->name );
}
@ Prototype the function.
@<Prototypes@>+=
void update_web_object( web_object_t *obj );


@ Returns a string name for the zone state.
@<Functions@>+=
char *GetStateString( const char *pszType, int iState ) {
  static char szState[ 64 ];

  if( strcasecmp( pszType, "zone" ) == 0 ) {
    if( iState == STATE_INIT ) sprintf( szState, "STATE_INIT(%d)", iState );
    else if( iState == STATE_IDLE ) sprintf( szState, "STATE_IDLE(%d)", iState );
    else if( iState == STATE_FILL ) sprintf( szState, "STATE_FILL(%d)", iState );
    else if( iState == STATE_FILL_X ) sprintf( szState, "STATE_FILL_X(%d)", iState );
    else if( iState == STATE_FULL ) sprintf( szState, "STATE_FULL(%d)", iState );
    else if( iState == STATE_DRAIN_X ) sprintf( szState, "STATE_DRAIN_X(%d)", iState );
    else if( iState == STATE_DRAIN ) sprintf( szState, "STATE_DRAIN(%d)", iState );
    else if( iState == STATE_RUNUP ) sprintf( szState, "STATE_RUNUP(%d)", iState );
    else if( iState == STATE_CHOOSE ) sprintf( szState, "STATE_CHOOSE(%d)", iState );
    else if( iState == STATE_ACTIVE ) sprintf( szState, "STATE_ACTIVE(%d)", iState );
    else if( iState == STATE_DEACT ) sprintf( szState, "STATE_DEACT(%d)", iState );
    else if( iState == STATE_FILL_O ) sprintf( szState, "STATE_FILL_O(%d)", iState );
    else if( iState == STATE_DRAIN_O ) sprintf( szState, "STATE_DRAIN_O(%d)", iState );
    else if( iState == STATE_FAULT ) sprintf( szState, "STATE_FAULT(%d)", iState );
    else sprintf( szState, "state:%d", iState );
  }
#if 0
  else if( strcasecmp( pszType, "folder" ) == 0 ) {
    if( iState == SF_INIT ) sprintf( szState, "SF_INIT(%d)", iState );
    else if( iState == SF_IDLE ) sprintf( szState, "SF_IDLE(%d)", iState );
    else if( iState == SF_WAIT ) sprintf( szState, "SF_WAIT(%d)", iState );
    else if( iState == SF_BLOCK ) sprintf( szState, "SF_BLOCK(%d)", iState );
    else if( iState == SF_SETTLE ) sprintf( szState, "SF_SETTLE(%d)", iState );
    else if( iState == SF_SQUARE ) sprintf( szState, "SF_SQUARE(%d)", iState );
    else if( iState == SF_FULL ) sprintf( szState, "SF_FULL(%d)", iState );
    else if( iState == SF_EXTEND ) sprintf( szState, "SF_EXTEND(%d)", iState );
    else if( iState == SF_RETRACT ) sprintf( szState, "SF_RETRACT(%d)", iState );
    else if( iState == SF_GRIP ) sprintf( szState, "SF_GRIP(%d)", iState );
    else if( iState == SF_RELEASE ) sprintf( szState, "SF_RELEASE(%d)", iState );
    else if( iState == SF_CRIMP ) sprintf( szState, "SF_CRIMP(%d)", iState );
    else if( iState == SF_XFER_GRAB ) sprintf( szState, "SF_XFER_GRAB(%d)", iState );
    else if( iState == SF_RAISE ) sprintf( szState, "SF_RAISE(%d)", iState );
    else if( iState == SF_XFER ) sprintf( szState, "SF_XFER(%d)", iState );
    else if( iState == SF_CLOSE ) sprintf( szState, "SF_CLOSE(%d)", iState );
    else if( iState == SF_XFER_RELEASE ) sprintf( szState, "SF_XFER_RELEASE(%d)", iState );
    else if( iState == SF_LOWER ) sprintf( szState, "SF_LOWER(%d)", iState );
    else if( iState == SF_SETSERVO ) sprintf( szState, "SF_SETSERVO(%d)", iState );
    else if( iState == SF_SETSLIDE ) sprintf( szState, "SF_SETSLIDE(%d)", iState );
    else if( iState == SF_DONE ) sprintf( szState, "SF_DONE(%d)", iState );
    else if( iState == SF_SLIDEIN ) sprintf( szState, "SF_SLIDEIN(%d)", iState );
    else if( iState == SF_SLIDEOUT ) sprintf( szState, "SF_SLIDEOUT(%d)", iState );
    else if( iState == SF_FAULT ) sprintf( szState, "SF_FAULT(%d)", iState );
    else sprintf( szState, "state:%d", iState );
  }
  else if( strcasecmp( pszType, "servo" ) == 0 ) {
    if( iState == SVR_POWERUP ) sprintf( szState, "SVR_POWERUP(%d)", iState );
    else if( iState == SVR_INIT ) sprintf( szState, "SVR_INIT(%d)", iState );
    else if( iState == SVR_MOVING ) sprintf( szState, "SVR_MOVING(%d)", iState );
    else if( iState == SVR_IDLE ) sprintf( szState, "SVR_IDLE(%d)", iState );
    else if( iState == SVR_FAULT ) sprintf( szState, "SVR_FAULT(%d)", iState );
    else sprintf( szState, "state:%d", iState );
  }
#endif
  else sprintf( szState, "state:%d", iState );
  return( szState );
}
@ Prototype the function.
@<Prototypes@>+=
char *GetStateString( const char *pszType, int iState );


@ Returns a string name for the zone error.
@<Functions@>+=
char *GetErrString( const char *pszType, int iErr ) {
  static char szErr[ 64 ];

  if( strcasecmp( pszType, "zone" ) == 0 ) {
    if( iErr == ERR_UNEXPECTED ) sprintf( szErr, "ERR_UNEXPECTED(%d)", iErr );
    else if( iErr == ERR_EARLY ) sprintf( szErr, "ERR_EARLY(%d)", iErr );
    else if( iErr == ERR_MISSING ) sprintf( szErr, "ERR_MISSING(%d)", iErr );
    else if( iErr == ERR_REMOVED ) sprintf( szErr, "ERR_REMOVED(%d)", iErr );
    else if( iErr == ERR_STUCK ) sprintf( szErr, "ERR_STUCK(%d)", iErr );
    else if( iErr == ERR_SENSOR ) sprintf( szErr, "ERR_SENSOR(%d)", iErr );
    else if( iErr == ERR_LOGIC ) sprintf( szErr, "ERR_LOGIC(%d)", iErr );
    else sprintf( szErr, "error:%d", iErr );
  }
#if 0
  else if( strcasecmp( pszType, "folder" ) == 0 ) {
    if( iErr == ERF_PAPER_JAM ) sprintf( szErr, "ERF_PAPER_JAM(%d)", iErr );
    else if( iErr == ERF_PAPER_LOST ) sprintf( szErr, "ERF_PAPER_LOST(%d)", iErr );
    else if( iErr == ERF_PRINTER_TIMEOUT ) sprintf( szErr, "ERF_PRINTER_TIMEOUT(%d)", iErr );
    else if( iErr == ERF_FOLDER_FAIL ) sprintf( szErr, "ERF_FOLDER_FAIL(%d)", iErr );
    else if( iErr == ERF_POCKET_FAULT ) sprintf( szErr, "ERF_POCKET_FAULT(%d)", iErr );
    else if( iErr == ERF_PAPER_MISFEED ) sprintf( szErr, "ERF_PAPER_MISFEED(%d)", iErr );
    else if( iErr == ERF_CHUTE_JAM ) sprintf( szErr, "ERF_CHUTE_JAM(%d)", iErr );
    else if( iErr == ERF_SERVO_FAULT ) sprintf( szErr, "ERF_SERVO_FAULT(%d)", iErr );
    else if( iErr == ERF_SETUP_FAIL ) sprintf( szErr, "ERF_SETUP_FAIL(%d)", iErr );
    else if( iErr == ERF_YGRIP_FAIL ) sprintf( szErr, "ERF_YGRIP_FAIL(%d)", iErr );
    else sprintf( szErr, "error:%d", iErr );
  }
#endif
  else sprintf( szErr, "error:%d", iErr );
  return( szErr );
}
@ Prototype the function.
@<Prototypes@>+=
char *GetErrString( const char *pszType, int iErr );



@* Index.
