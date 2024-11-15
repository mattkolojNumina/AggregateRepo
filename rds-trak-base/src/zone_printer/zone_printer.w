%
%   zone_printer.w -- printer app for ZoneTRAK systems
%
%   Author: Mark Woodworth 
%
%   History:
%      2011-10-31 -MRW- modified for autolabe
%      2011-12-09 -AHM- modified for standard X-Press PAL
%      2012-02-07 -AHM/MDO- cleanup + multiple-label printing
%      2014-04-21 -AHM/ANK- modified for OneStep+ compatibility
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
%     (C) Copyright 2011--2014 Numina Group, Inc.  All Rights Reserved.
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
\def\title{Printer}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program implements a state machine to run a printer in a ZoneTRAK
system.

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
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2024/06/05 19:36:39 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 NuminaGroup, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program implements a state machine to run a printer in a ZoneTRAK
system.

@c
static char rcsid[] = "$Id: zone_printer.w,v 1.1 2024/06/05 19:36:39 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int main( int argc, char *argv[] ) 
   {
   @<Initialize@>@;

   for(;;usleep(SLEEP_DURATION*1000)) 
      crank() ;

   exit( EXIT_SUCCESS );
   }

@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <rds_net.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>

#include <app.h>
#include <zonetrak.h>

#ifdef PNP
#include <pnp.h>
#endif

@ Definitions.
@<Defines@>+=
#define BUF_LEN           32  // length of small, statically allocated strings
#define MSG_LEN          128
#define LABEL_LEN      50000  // max length of label data

#define SLEEP_DURATION    10  // sleep time btwn processing cycles (msec)
#define TIMEOUT_WAITING   "5000"  // default waiting timeout (msec)
#define TIMEOUT_PRINTING  "5000"  // default printing timeout (msec)
#define TIMEOUT_HOLD     "25000"  // default hold timeout (msec)
#define TIMEOUT_BLOCK    "10000"  // default block timeout (msec)
#define TIMEOUT_UNBLOCK  "10000"  // default unblock timeout (msec)
#define TIMEOUT_FILLING   "3000"  // default filling timeout (msec)
#define TIMEOUT_TAMP      "2000"  // default tamp timeout (msec)
#define TIMEOUT_TAMPING   "2000"  // default tamping timeout (msec)

@ Initialization.
@<Initialize@>+=
   {
   if (argc != 2)
      {
      printf( "usage: %s <printer>\n", argv[0] ) ;
      exit( EXIT_SUCCESS ) ;
      }

   strncpy( printer, argv[1], BUF_LEN );
   printer[ BUF_LEN ] = '\0';

   trn_register( printer ) ;
   Trace( "init" ) ;
   Inform( rcsid ) ;

   strcpy(pnp_name,util_get_control(printer,"pnp",""));

   timeout_waiting = atoi( util_get_control( printer, "timeoutWaiting",
         TIMEOUT_WAITING ) ) / SLEEP_DURATION;
   timeout_printing = atoi( util_get_control( printer, "timeoutPrinting",
         TIMEOUT_PRINTING ) ) / SLEEP_DURATION;
   timeout_hold = atoi( util_get_control( printer, "timeoutHold",
         TIMEOUT_HOLD ) ) / SLEEP_DURATION;
   timeout_block = atoi( util_get_control( printer, "timeoutBlock",
         TIMEOUT_BLOCK ) ) / SLEEP_DURATION;
   timeout_unblock = atoi( util_get_control( printer, "timeoutUnblock",
         TIMEOUT_UNBLOCK ) ) / SLEEP_DURATION;
   timeout_filling = atoi( util_get_control( printer, "timeoutFilling",
         TIMEOUT_FILLING ) ) / SLEEP_DURATION;
   timeout_tamp = atoi( util_get_control( printer, "timeoutTamp",
         TIMEOUT_TAMP ) ) / SLEEP_DURATION;
   timeout_tamping = atoi( util_get_control( printer, "timeoutTamping",
         TIMEOUT_TAMPING ) ) / SLEEP_DURATION;

   tamp_delay = atoi( util_get_control( printer, "tampDelay", "0" ) );
   }

@ Global variables.
@<Globals@>+=
char printer[ BUF_LEN + 1 ] ;
char pnp_name[ BUF_LEN + 1 ] ;
int box ;
int carton_id ;
int label_required ;
int pnp_required ;
int tamp_delay ;

int timeout_waiting ;
int timeout_printing ;
int timeout_hold ;
int timeout_block ;
int timeout_unblock ;
int timeout_filling ;
int timeout_tamp ;
int timeout_tamping ;


@ Crank.
@<Functions@>+=
void crank(void)
   {
   cycles++ ;
   switch(state)
      {
      @<States@>@;
      }

   if (cycles % 100 == 0)
      @<Status Update@>@;
   }
@ Proto.
@<Prototypes@>+=
void crank(void) ;

@ Determine printer status and update diagnostics.
@<Status Update@>=
{
   static int ribbon_dp = -1 ;
   static int label_dp = -1 ;

   char status_msg[ BUF_LEN + 1 ] ;

   printer_dp( "ribbon", &ribbon_dp, FALSE );
   printer_dp( "label", &label_dp, FALSE );

   if (printer_fault( FALSE ))
      strcpy(status_msg,"fault") ;
   else if (dp_get(ribbon_dp) || dp_get(label_dp)) 
      sprintf(status_msg,"low%s%s",
            (dp_get(ribbon_dp)) ? " ribbon" : "",
            (dp_get(label_dp)) ? " label" : "") ;
   else
      strcpy(status_msg,"ok") ;

   sql_query(
         "UPDATE webObjects SET "
         "value='%s (%s)' "
         "WHERE name='%s'",
         status_msg,state_string(),printer) ;
}

@ State Definitions.
@<Defines@>+=
#define sINIT          0  // initialization
#define sIDLE          1  // wait for box in zone
#define sWAITING       2  // wait for label data
#define sPRINTING      3  // print label data
#define sHOLD          4  // wait for pnp slide to hold
#define sBLOCK         5  // wait for pnp slide to block
#define sUNBLOCK       6  // wait for pnp slide to unblock
#define sFILLING       7  // wait for box in position
#define sTAMP          8  // fire tamp
#define sTAMPING       9  // wait for tamp
#define sCOMPLETE     10  // complete the cycle
#define sRELEASE      11  // release box from zone
#define sFAULT        99  // program fault

@
@<Functions@>=
char *state_string(void)
   {
   if (state == sINIT)     return "init" ;
   if (state == sIDLE)     return "idle" ;
   if (state == sWAITING)  return "wait" ;
   if (state == sPRINTING) return "print" ;
   if (state == sHOLD)     return "hold" ;
   if (state == sBLOCK)    return "block" ;
   if (state == sUNBLOCK)  return "unblock" ;
   if (state == sFILLING)  return "fill" ;
   if (state == sTAMP)     return "tamp" ;
   if (state == sTAMPING)  return "tamping" ;
   if (state == sCOMPLETE) return "complete" ;
   if (state == sRELEASE)  return "release" ;
   if (state == sFAULT)    return "fault" ;
   }
@ Proto.
@<Prototypes@>+=
char *state_string(void) ;

@ The state variable.
@<Globals@>+=
int state=sINIT ;
int cycles=0 ;


@ setState
@<Functions@>+=
int setState(int newState)
   {
   if(state!=newState)
      {
      state = newState ;
      Inform("switch to %s state (%d), cycles=%d",
            state_string(),state,cycles) ;
      cycles = 0 ;
      switch(state)
         {
         @<Edges@> @;
         }
      }
   }

@ Proto.
@<Prototypes@>+=
int setState(int newState) ;

@ Init.
@<States@>+=
   {
   case sINIT:
      {
      setState(sIDLE) ;
      }
   break ;
   }

@ Clear data before processing a new box.
@<Edges@>+=
   {
   case sIDLE:
      {
      box = BOX_NONE ;
      carton_id = -1 ;
      label_required = FALSE ;
      pnp_required = FALSE ;
      }
   break ;
   }

@ Look for zone box.
@<States@>+=
   {
   case sIDLE:
      {
      if(get_zone_state() == STATE_FULL)
      {
         util_zone_release(printer);
      }
      box = get_zone_box() ;
      if(box != BOX_NONE)
         {
         Inform("%03d: box detected in printer zone",box) ;

         if(box<0)
            {
            Inform("%03d: anonymous box, ignore",box) ;
            setState(sFILLING) ;
            break ;
            }

         carton_id = util_box_get_int(box,"seq") ;
         if(carton_id<0)
            {
            Inform("%03d: unable to determine carton id",box) ;
            setState(sFILLING) ;
            break ;
            }

         label_required = util_required(carton_id,printer) ;
         if(label_required)
            {
            Inform("%03d: label required for [%d]",box,carton_id) ;
            setState(sWAITING) ;
            break ;
            }

         Trace("%03d: no printing required for [%d]",box,carton_id) ;
         setState(sFILLING) ;
         break ;
         }

      if(get_printer_format())
         send_formats();
      if(get_printer_config())
         config_printer();
      }
   break ;
   }

@ Prepare for box/label arrival.
@<Edges@>+=
   {
   case sWAITING:
      {
      printer_case( TRUE ) ;
      }
   break ;
   }

@ Look for label.
@<States@>+=
   {
   case sWAITING:
      {
      int status ;

      if(cycles > timeout_waiting)
         {
         Alert("%03d: timeout waiting for label for [%d]",box,carton_id) ;
         label_required = FALSE ;
         setState(sFILLING) ;
         break ;
         } 

      if(!printer_ready())
         {
         Alert("%03d: printer not ready for [%d]",box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }
 
      status = app_label_ready(carton_id,printer) ;
      if (status < 0)
         {
         Alert("%03d: label ready error for [%d]",box,carton_id) ;
         label_required = FALSE ;
         setState(sFILLING) ;
         break ;
         }
      if (status > 0)
         {
         Inform("%03d: label ready for [%d]",box,carton_id) ;
         setState(sPRINTING) ;
         break ;
         }
      }
   break ;
   }

@ Print the label.
@<Edges@>+=
   {
   case sPRINTING:
      {
      char label[ LABEL_LEN + 1 ] ;
      int len = LABEL_LEN ;
      int ordinal ;
      int err ;

      ordinal = app_get_label( carton_id, printer, label, &len ) ;
      if (ordinal <= 0)
         {
         Alert("%03d: label get error for [%d]",box,carton_id) ;
         util_update_status(carton_id,printer,"failed","label") ;
         setState(sFAULT) ;
         break ;
         }

      Trace("%03d: got page %d for [%d] (%d bytes)",box,ordinal,carton_id,len) ;
      err = send_label( label, len ) ;
      if (err)
         {
         Alert("%03d: printing error for [%d]",box,carton_id) ;
         util_update_status(carton_id,printer,"failed","xmit") ;
         setState(sFAULT) ;
         break ;
         }

      app_label_printed(carton_id,printer,ordinal) ;
      }
   break ;
   }

@ And wait for it.
@<States@>+=
   {
   case sPRINTING:
      {
      if (cycles > timeout_printing)
         {
         Alert("%03d: timeout waiting for lotar for [%d]",box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }

      if (get_printer_lotar())
         {
         pnp_required = util_required(carton_id,pnp_name) ;
         if (pnp_required)
            {
            Inform("%03d: %s doc required for [%d]",box,pnp_name,carton_id) ;
            setState(sHOLD) ;
            break ;
            }

         setState(sFILLING) ;
         break ;
         }
      }
   break ;
   }


#ifdef PNP  // start PNP states

@ Wait for the pnp slide to get into position.
@<States@>+=
   {
   case sHOLD:
      {
      if (cycles > timeout_hold)
         {
         Alert("%03d: timeout waiting for slide for [%d]",box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }

      if (get_slide_reg(REG_STATE) == STATE_SLIDE_HOLD)
         {
         setState(sBLOCK) ;
         break ;
         }
      }
   break ;
   }

@ Trigger the slide to place the document.
@<Edges@>+=
   {
   case sBLOCK:
      {
      int pnp_box = get_slide_reg(REG_BOX) ;
      if (pnp_box != box)
         {
         Alert("%03d: tracking err for [%d], slide box = %03d",
               box,carton_id,pnp_box) ;
         setState(sFAULT) ;
         break ;
         }

      Inform("%03d: place document for [%d]",box,carton_id) ;
      set_slide_place(1) ;
      }
   break ;
   }

@ And wait for it.
@<States@>+=
   {
   case sBLOCK:
      {
      if(cycles > timeout_block)
         {
         Alert("%03d: timeout waiting for slide blocked for [%d]",
               box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }

      if(get_slide_blocked())
         {
         setState(sUNBLOCK) ;
         break ;
         }
      }
   break ;
   }

@ Monitor the slide to ensure it is not blocking the tamp.
@<States@>+=
   {
   case sUNBLOCK:
      {
      if(cycles > timeout_unblock)
         {
         Alert("%03d: timeout waiting for slide unblocked for [%d]",
               box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }

      if(!get_slide_blocked())
         {
         setState(sFILLING) ;
         break ;
         }
      }
   break ;
   }

#endif  // end PNP states


@ Get into position.
@<States@>+=
   {
   case sFILLING:
      {
      if(cycles > timeout_filling)
         {
         Alert("%03d: timeout waiting for zone full for [%d]",box,carton_id) ;
         if(label_required)
           setState(sFAULT) ;
         else
           setState(sRELEASE) ;
         break ;
         }

      if(get_zone_state() == STATE_FULL)
         {
         if(label_required)
            setState(sTAMP) ;
         else
            setState(sRELEASE) ;
         break ;
         }
      }
   break ;
   }

@ Fire the tamp.
@<Edges@>+=
   {
   case sTAMP:
      {
      int zone_box = get_zone_box() ;
      if (zone_box != box)
         {
         Alert("%03d: tracking err for [%d], zone box = %03d",
               box,carton_id,zone_box) ;
         setState(sFAULT) ;
         break ;
         }

      Inform("%03d: fire tamp for [%d]",box,carton_id) ;
      printer_tamp() ;
      }
   break ;
   }

@ And wait for it.
@<States@>+=
   {
   case sTAMP:
      {
      if(cycles > timeout_tamp)
         {
         Alert("%03d: timeout waiting for tamp for [%d]",box,carton_id) ;
         setState(sFAULT) ;
         break ;
         }

      if(!get_printer_home())
         {
         setState(sTAMPING) ;
         break ;
         }
      }
   break ;
   }

@ Monitor the tamp motion to complete the label application.
@<States@>+=
   {
   case sTAMPING:
      {
      if(cycles > timeout_tamping || get_printer_home())
         {
         if(app_label_ready(carton_id,printer) > 0)
            {
            Inform("%03d: additional label ready for [%d]",box,carton_id) ;
            setState(sPRINTING) ;
            }
         else
            setState(sCOMPLETE) ;
         break ;
         }
      }
   break ;
   }

@ Update the box as complete.
@<Edges@>+=
   {
   case sCOMPLETE:
      {
      Trace("%03d: printing complete for [%d]",box,carton_id) ;
      util_update_status(carton_id,printer,"complete","") ;
      if(pnp_required)
         util_update_status(carton_id,pnp_name,"complete","") ;
      }
   break ;
   }

@ Complete the printing cycle.
@<States@>+=
   {
   case sCOMPLETE:
      {
      setState(sRELEASE) ;
      }
   break ;
   }

@ Release the box.
@<Edges@>+=
   {
   case sRELEASE:
      {
      printer_case(FALSE) ;
      util_zone_release( printer );
      }
   break ;
   }

@ And wait for it.
@<States@>+=
   {
   case sRELEASE:
      {
      if(get_zone_state() == STATE_FULL)
      {
         util_zone_release(printer);
      }
      if(get_zone_box() == BOX_NONE)
         {
         setState(sIDLE) ;
         break ;
         }
      }
   break ;
   }

@ Fault the printer.
@<Edges@>+=
   {
   case sFAULT:
      {
      Inform("set printer fault") ;
      printer_fault(TRUE) ;

      // default error status if not already set
      util_update_status(carton_id,printer,"failed","processing") ;
      }
   break ;
   }

@ Fault.
@<States@>+=
   {
   case sFAULT:
      {
      setState(sRELEASE) ;
      }
   break ;
   }


@* Printer control.

@ Printer definitions.
@<Defines@>+=
#define DEFAULT_PORT         "9100"  // default network port

#define ERR_NODATA           -1
#define ERR_ATTACH           -2
#define ERR_WRITE            -3


@ Printer globals.
@<Globals@>+=
int printer_handle = -1;


@ Is printer ready?
@<Functions@>+=
int printer_ready(void) {
   return !printer_fault(FALSE) &&
         !get_printer_lotar() &&
         get_printer_home();
}
@ Prototype the function.
@<Prototypes@>+=
int printer_ready(void);


@ Send label to printer.
@<Functions@>+=
int send_label( const char data[], int len ) {
   int  cnt;

   if (data == NULL || len  == 0) {
      Alert( "no data to send" );
      return ERR_NODATA;
   }

   printer_attach();
   if (printer_handle < 0)
      return ERR_ATTACH;

//   write( printer_handle, "~JA", 3 );
   Inform( "transmitting %d bytes to printer", len );
   cnt = write( printer_handle, data, len );

   if (cnt != len) {
      Alert( "write to printer device failed, detach" );
      printer_detach();
      return ERR_WRITE;
   }

   printer_detach();
   return 0;
}
@ Prototype the function.
@<Prototypes@>+=
int send_label( const char data[], int len );


@ Send label formats to printer.
@<Functions@>+=
int send_formats( void ) {
   int i, err;
   int num_templates;

   Trace( "send label formats" );

   printer_attach();
   if (printer_handle < 0)
      return ERR_ATTACH;

   err = sql_query( "SELECT name, zpl FROM templates" );
   if (err || (num_templates = sql_rowcount()) == 0)
      return ERR_NODATA;

   Trace( "found %d template(s)", num_templates );
   for (i = 0; i < num_templates; i++) {
      int len;
      char *data = sql_getlen( i, 1, &len );

      Inform( "   send %s template", sql_get( i, 0 ) );
      err = send_label( data, len );

      if (err)
         return err;
   }
   Trace( "label formats sent" );

   return 0;
}
@ Prototype the function.
@<Prototypes@>+=
int send_formats( void );


@ Open the connection to the printer, if necessary.
@<Functions@>+=
void printer_attach( void ) {
   char ip[ BUF_LEN + 1 ];
   int port;

   if (printer_handle < 0) {
      strcpy( ip, util_get_control( printer, "ip", "" ) );
      port = atoi( util_get_control( printer, "port", DEFAULT_PORT ) );

      Inform( "connect to printer at [%s:%d]", ip, port );
      printer_handle = net_open( ip, port );
      if (printer_handle < 0)
         Alert( "failed to open printer device at [%s:%d]", ip, port );
      else
         Trace( "connected to printer device at [%s:%d], handle [%d]",
               ip, port, printer_handle );

   }
}
@ Prototype the function.
@<Prototypes@>+=
void printer_attach( void );


@ Close the connection to the printer.
@<Functions@>+=
void printer_detach( void ) {
   if (printer_handle >= 0)
      close( printer_handle );
   printer_handle = -1;
}
@ Prototype the function.
@<Prototypes@>+=
void printer_detach( void );


@ Configure the printer from factory defaults.
@<Functions@>+=
void config_printer( void ) {
   char filename[ 64 ];
   FILE *config_file;
   char c;

   sprintf( filename, "/home/rds/app/data/%s.conf", printer );
   config_file = fopen( filename, "r" );
   if (config_file == NULL) {
      Alert( "unable to configure print engine" );
      return;
   }

   Alert( "configuring print engine" );
   printer_attach();
   while ((c = fgetc( config_file )) != EOF)
      write( printer_handle, &c, 1 );
   fclose( config_file );
   printer_detach();
   printer_attach();
}
@ Prototype the function.
@<Prototypes@>+=
void config_printer( void );


@* Utilities.

@ Get the value from a register of the zone dp.
@<Functions@>+=
int get_zone_reg( int reg )
   {
   static int zone_dp = -1 ;
   int val;

   if (zone_dp < 0)
      {
      char zone_name[ TRAK_NAME_LEN + 1 ] ;
      util_zone_get( printer, zone_name ) ;
      if (strlen( zone_name ) == 0)
         {
         Alert( "unable to determine %s zone", printer ) ;
         return -1;
         }
      zone_dp = dp_handle( zone_name ) ;
      if (zone_dp < 0)
         {
         Alert( "unable to obtain dp for %s zone", printer ) ;
         return -1 ;
         }
      }

   dp_registerget( zone_dp, reg, &val ) ;
   return val ;
   }

@ Get the current box from the printer zone.
@<Functions@>+=
int get_zone_box( void )
   {
   int zone_box = get_zone_reg(REG_BOX) ;
   return (zone_box == 0) ? BOX_NONE : zone_box ;
   }

@ Get the current state of the printer zone.
@<Functions@>+=
int get_zone_state( void )
   {
   return get_zone_reg( REG_STATE );
   }

@ Prototype the functions.
@<Prototypes@>+=
int get_zone_reg( int reg ) ;
int get_zone_box( void ) ;
int get_zone_state( void ) ;

@ Checks a dp for printer i/o.
@<Functions@>+=
int printer_dp( const char type[], int *dp, int alert )
   {
   if (*dp < 0)
      {
      char name[ MSG_LEN + 1 ] ;
      sprintf( name, "%s_%s", printer, type ) ;
      *dp = dp_handle( name ) ;
      if (*dp < 0)
         {
         if (alert)
            Alert( "unable to obtain dp for [%s]", name ) ;
         return FALSE ;
         }
      }
   return TRUE ;
   }
@ Prototype the function.
@<Prototypes@>+=
int printer_dp( const char type[], int *dp, int alert ) ;

@ Fire the printer tamp.
@<Functions@>+=
void printer_tamp( void )
   {
   static int tamp_dp = -1 ;

   if (!printer_dp( "tamp", &tamp_dp, TRUE ))
      return ;

   if (tamp_delay > 0)
      usleep( tamp_delay * 1000 ) ;

   dp_set( tamp_dp, 1 ) ;
   }
@ Prototype the function.
@<Prototypes@>+=
void printer_tamp( void ) ;

@ Get/set the printer fault.
@<Functions@>+=
int printer_fault( int fault )
   {
   static int ok_dp = -1 ;
   static int fault_dp = -1 ;

   if (!printer_dp( "ok", &ok_dp, TRUE ))
      return FALSE;
   if (!printer_dp( "fault", &fault_dp, TRUE ))
      return FALSE;

   if (fault)
      dp_set( fault_dp, 1 ) ;

   return !dp_get( ok_dp ) || dp_get( fault_dp ) ;
   }

@ Get/set the printer reset.
@<Functions@>+=
int printer_reset( int reset )
   {
   static int reset_dp = -1 ;

   if (!printer_dp( "reset", &reset_dp, TRUE ))
      return FALSE ;

   if (reset)
      dp_set( reset_dp, 1 ) ;

   return dp_get( reset_dp ) ;
   }

@ Get the state of the data-ready input.
@<Functions@>+=
int get_printer_data( void )
   {
   static int data_dp = -1 ;

   if (!printer_dp( "data", &data_dp, FALSE ))
      return FALSE ;

   return dp_get( data_dp ) ;
   }

@ Get the state of the LOTAR input.
@<Functions@>+=
int get_printer_lotar( void )
   {
   static int lotar_dp = -1 ;

   if (!printer_dp( "lotar", &lotar_dp, TRUE ))
      return FALSE ;

   return dp_get( lotar_dp ) ;
   }

@ Get the state of the tamp-home input.
@<Functions@>+=
int get_printer_home( void )
   {
   static int home_dp = -1 ;

   if (!printer_dp( "home", &home_dp, TRUE ))
      return FALSE ;

   return dp_get( home_dp ) ;
   }

@ Prototype the functions.
@<Prototypes@>+=
int printer_fault( int fault ) ;
int printer_reset( int reset ) ;
int get_printer_data( void ) ;
int get_printer_lotar( void ) ;
int get_printer_home( void ) ;


@ Raise/lower the case stop.
@<Functions@>+=
void printer_case( int value )
   {
   static int case_dp = -1 ;

   if (!printer_dp( "case", &case_dp, FALSE ))
      return ;

   dp_set( case_dp, value ) ;
   }
@ Prototype the function.
@<Prototypes@>+=
void printer_case( int value ) ;


@ Determine if formatting templates should be sent to the printer.
@<Functions@>+=
int get_printer_format( void )
   {
   static int format_dp = -1 ;
   int val ;

   if (!printer_dp( "format", &format_dp, FALSE ))
      return FALSE ;

   val = dp_get( format_dp ) ;
   dp_set( format_dp, 0 ) ;
   return val ;
   }

@ Determine if the printer configuration should be triggered.
@<Functions@>+=
int get_printer_config( void )
   {
   static int config_dp = -1 ;
   int val ;

   if (!printer_dp( "config", &config_dp, FALSE ))
      return FALSE ;

   val = dp_get( config_dp ) ;
   dp_set( config_dp, 0 ) ;
   return val ;
   }

@ Prototype the functions.
@<Prototypes@>+=
int get_printer_format( void ) ;
int get_printer_config( void ) ;

@ Get the value from a register of the pnp slide dp.
@<Functions@>+=
int get_slide_reg( int reg )
   {
   static int slide_dp = -1 ;
   int val;

   if (slide_dp == 0)
      return 0 ;

   if (slide_dp < 0)
      {
      char slide_name[ MSG_LEN + 1 ] ;

      if (strlen( pnp_name ) == 0)
         {
         slide_dp = 0 ;
         return 0 ;
         }

      sprintf(slide_name,"%s-slide",pnp_name) ;
      slide_dp = dp_handle(slide_name) ;
      if (slide_dp < 0)
         {
         Alert("unable to obtain dp for %s slide",pnp_name) ;
         return -1 ;
         }
      }

   dp_registerget( slide_dp, reg, &val ) ;
   return val ;
   }
@ Prototype the function.
@<Prototypes@>+=
int get_slide_reg( int reg ) ;

@ Trigger the slide to place the document.
@<Functions@>+=
void set_slide_place( int val )
   {
   static int place_dp = -1 ;

   if (place_dp == 0)
      return ;

   if (place_dp < 0)
      {
      place_dp = get_slide_reg( REG_INPUT ) ;
      if (place_dp < 0)
         {
         place_dp = 0 ;
         return ;
         }
      }
   Inform("set slide place -> %d",val) ;
   dp_set(place_dp,val) ;
   }
@ Prototype the function.
@<Prototypes@>+=
void set_slide_place( int val ) ;

@ Check if the slide is blocking the tamp.
@<Functions@>+=
int get_slide_blocked( void )
   {
   static int blocked_dp = -1 ;

   if (blocked_dp == 0)
      return 0 ;

   if (blocked_dp < 0)
      {
      blocked_dp = get_slide_reg( REG_OUTPUT ) ;
      if (blocked_dp < 0)
         {
         blocked_dp = 0 ;
         return 0 ;
         }
      }
   return dp_get(blocked_dp) ;
   }
@ Prototype the function.
@<Prototypes@>+=
int get_slide_blocked( void ) ;


@ Index.
