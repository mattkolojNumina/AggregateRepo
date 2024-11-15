%
%   pfi.w -- sends packing lists to a Kyocera printer and controls PFI
%
%   Author: Mark Woodworth 
%   Modified: Mark Olson, Alex Korzhuk, Rushi Patel, Will Warden
%
%   History:
% 20120609 - init (mrw)
% 20130220 - put to production for HDS (mdo)
% 01/02/2014 ank: modified for McKesson
% 07/21/2021 rpp: modified for PFI v2
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
\def\title{PFI}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program interacts with Trak to print packing lists.

This program prints multiple batches and multiple documents per box.

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
\centerline{Author: Mark Olson}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.124 $ $}
\centerline{Control Date: $ $Date: 2023/04/12 18:31:19 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program interacts with Trak to print packing lists.
@c
char rcsId[] = "$Id: pfi.w,v 1.124 2023/04/12 18:31:19 rds Exp rds $" ;
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int main( int argc, char *argv[] ) {
  int qid, box, id, docId, sheets;

  @<Initialize@>@;

  for( ; ; usleep( 50 * 1000 ) ) {
    check_status();
    check_purge();
    //if( !run() ) continue;

    check_status();
    if( ready_to_drop() ) drop();
    check_status();
    check_feed();
    check_status();
    check_release();
  }
}



@ Initialize.
@<Initialize@>=
{

  if ( argv[1] != NULL )
     sprintf( myName, "%s/prt", argv[1] );
  else 
     strcpy( myName, "prt" );

  char *v, name[ 32 ], go_name[ 32 ], hold_name[ 32 ], fault_name[ 32 ];
  if( argc < 2 ) {
    fprintf( stderr, "usage: %s <pfi id (a/b/etc.)>\n", argv[ 0 ] );
    exit( 1 );
  }

  pfi[ 0 ] = '\0';
  strncpy( pfi, argv[ 1 ], 32 ); pfi[ 32 ] = '\0';

  sprintf( shortname, "p-%s", pfi );
  sprintf( progname, "pfi-%s", pfi );
  trn_register( progname );
  Inform( "%s", rcsId );

  strcpy( area, "pfi" );

  pfi_dp = dp_handle( shortname );
  Inform( "pfi [%s] dp:%d ", shortname, pfi_dp );

  sprintf( name, "p-%s.formax", pfi );
  formax_dp = dp_handle( name );
  Inform( "formax fsm [%s] dp:%d ", name, formax_dp );

  sprintf( name, "p-%s.platform", pfi );
  platform_dp = dp_handle( name );
  Inform( "platorm fsm [%s] dp:%d ", name, platform_dp );

  sprintf( name, "p-%s.pocket", pfi );
  pocket_dp = dp_handle( name );
  Inform( "pocket fsm [%s] dp:%d ", name, pocket_dp );

  sprintf( name, "p-%s.grip", pfi );
  grip_dp = dp_handle( name );
  Inform( "drop fsm [%s] dp:%d", name, grip_dp );

  sprintf( name, "p-%s_go", pfi );
  drop_go = dp_handle( name );
  Inform( "drop go [%s] dp:%d", name, drop_go );

  sprintf( name, "run" );
  run_h = dp_handle( name );

  sprintf( name, "p-%s_reset", pfi );
  reset_dp = dp_handle( name );
  Inform( "reset dp [%s] dp:%d", name, reset_dp );

  sprintf( name, "p-%s.purging", pfi );
  purge_dp = dp_handle( name );
  Inform( "purge dp [%s] dp:%d", name, purge_dp );

  sprintf( name, "p-%s_fault", pfi );
  fault_dp = dp_handle( name );
  Inform( "fault dp [%s] dp:%d", name, fault_dp );

  strcpy( printer, util_get_control( progname, "printer", "" ) );

  strcpy( name, util_get_control( progname, "zone", "" ) );
  zone_h = dp_handle( name );
  Inform( "zone [%s] dp:%d", name, zone_h );
  sprintf( go_name, "%s_go", name );
  zone_go_h = dp_handle( go_name );
  sprintf( fault_name, "%s_fault", name );
  zone_fault_h = dp_handle( fault_name );

  strcpy( name, util_get_control( progname, "bypass", "" ) );
  bypass_h = dp_handle( name );
  Inform( "bypass [%s] dp:%d", name, bypass_h);

  strcpy( name, util_get_control( "pfisched", "zone", "" ) );
  schedzone_h = dp_handle( name );
  Inform( "schedzone [%s] dp:%d", name, schedzone_h );

  flash_good_h = dp_handle( "insert-green" );
}



@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <ctype.h>

#include <app.h>
#include <rds_ctr.h>
#include <rds_evt.h>
#include <rds_net.h>
#include <rds_trn.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_util.h>
#include <zonetrak.h>
#include <zonepfi.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN            32  // length of small statically allocated strings

typedef struct {
  int cartonId;
  int box;
} Carton;



@ Globals.
@<Globals@>+=
char myName [ 64 ] ;
char progname[ 32 + 1 ], pfi[ 32 + 1 ], shortname[ 32 + 1 ], printer[ 32+1 ];
char area[ 32 + 1 ];
int pfi_dp, formax_dp, platform_dp, pocket_dp, grip_dp, drop_go, run_h, bypass_h;
int zone_h, zone_go_h, zone_fault_h;
int schedzone_h;
int bPFIFaulted, flash_good_h;
int reset_dp, purge_dp, fault_dp, purge_counter = 0, reset_counter = 0;



@* Run.
@<Functions@>+=
int run( void ) {
  return( dp_get( run_h ) );
}
@ Prototype the function.
@<Prototypes@>+=
int run( void );


@* Bypass.
@<Functions@>+=
int bypass( void ) {
  return( dp_get( bypass_h ) );
}
@ Prototype the function.
@<Prototypes@>+=
int bypass( void );



@ can we feed paper.
Returns qid (>0) on success, when there is a document to feed.
Returns 0 in the following:
(a) on sql error,
(b) when there is no document to feed at this time,
(c) when there is no room to feed a document.
@<Functions@>+=
int check_feed( void ) {
  //Inform("check_feed");
  int rows, qid, box, id, docId, sheets, drops;
  if( dp_get( formax_dp ) ) return( 0 ); //pfi is not ready
  if( bPFIFaulted ) return( 0 );     //do not feed paper into faulted machine
  if( purge_counter ) return( 0 );      //do not claim if purging
  if( !printer_ok() ) return( 0 );      //PFI is not ready
  if( sql_query( "SELECT qSeq, box, cartonId, docSeq, sheets, drops "
                 "FROM pfiQueue WHERE printed IS NULL AND canceled IS NULL "
                 "AND pfi = '%s' "
                 "ORDER BY qSeq LIMIT 1", shortname ) ) {
    Alert( "sql err reading pfiQueue tbl in check_feed()" );
    return( 0 );
  }
  if( ( rows = sql_rowcount() ) < 0 ) {
    Alert( "sql err in rowcount reading pfiQueue tbl in check_feed()" );
    return( 0 );
  }
  if( rows == 0 ) return( 0 );
  qid = atoi( sql_get( 0, 0 ) );
  box = atoi( sql_get( 0, 1 ) );
  id = atoi( sql_get( 0, 2 ) );
  docId = atoi( sql_get( 0, 3 ) );
  sheets = atoi( sql_get( 0, 4 ) );
  drops = atoi( sql_get( 0, 5 ) );

  if( !batch_fits( box, drops, sheets, qid ) ) {
    Inform( "%03d: qid%d id%d docid%d sh%d drops%d wait for room",
            box, qid, id, docId, sheets, drops );
    return( 0 );
  }
  Trace( "%03d: qid%d id%d docid%d: sh%d drops%d",
         box, qid, id, docId, sheets, drops );
  sql_query( "UPDATE pfiQueue SET printed=NOW() WHERE qSeq=%d", qid );
  sql_query( "UPDATE conveyorBoxes SET topPrintStamp=NOW() WHERE seq=%d", id );
  sql_query( "UPDATE rdsDocuments SET printed=NOW() WHERE docSeq=%d", docId );
 
  dp_registerset( formax_dp, PFI_REG_TOTAL, sheets );  // TOTAL
  dp_registerset( formax_dp, PFI_REG_COUNT, 0 );  // COUNT
  dp_registerset( formax_dp, PFI_REG_BOX, box + qid * 1000 ); // BOX
  Inform("box %d assigned to %s.formax", box + qid * 1000, shortname );
  if( printer_send( docId ) == 0 ) {
    util_update_status( id, "inserts", "failed", "print" );
  }
  return( qid );
}
@ Prototype the function.
@<Prototypes@>+=
int check_feed( void );



@ A document can be more than one insertion batch. 
@<Functions@>+=
int batch_fits( int box, int drops, int sheets, int qSeq ) {
  static int m1 = -1;
  static int m2 = -1;
  static int m3 = -1;
  static int m4 = -1;

  int state1, state2, state3, state4, pfi1_box, zone_box;
  char name[ 40 ];

  if( !dp_get( 0 ) ) return( 0 );

  if( m1 == -1 ) {
    sprintf( name, "%s.formax", shortname );
    m1 = dp_handle( name );
  }
  if( m2 == -1 ) {
    sprintf( name, "%s.platform", shortname );
    m2 = dp_handle( name );
  }
  if( m3 == -1 ) {
    sprintf( name, "%s.pocket", shortname );
    m3 = dp_handle( name );
  }
  if( m4 == -1 ) {
    sprintf( name, "%s.grip", shortname );
    m4 = dp_handle( name );
  }

  dp_registerget( m1, PFI_REG_BOX, &pfi1_box );
  dp_registerget( m1, PFI_REG_STATE, &state1 );
  dp_registerget( m2, PFI_REG_STATE, &state2 );
  dp_registerget( m3, PFI_REG_STATE, &state3 );
  dp_registerget( m4, PFI_REG_STATE, &state4 );

  Inform( "%03d: drops%d state1:%d state2:%d state3:%d state4:%d",
          box, drops, state1, state2, state3, state4 );

//  if ( sheets == 1 ){
  char *val = sql_getvalue(
    "SELECT sheets FROM pfiQueue "
    "WHERE qSeq = %d", qSeq - 1
  );
  //if the previous doc was a single page
  //and we don't have to worry about our doc getting misinterpreted as
  //something for the previous box...
  if(valid_str(val) && atoi(val) == 1 && drops == 1)  { 
    if ( pfi1_box == BOX_NONE && state1 != PFI1_STATE_DRAIN_X ) return ( 1 ); 
  } else {
    if ( drops == 1 
        && state1 == PFI1_STATE_IDLE 
        && state2 == PFI2_STATE_IDLE ) return ( 1 );
    if ( drops == 2 
        && state1 == PFI1_STATE_IDLE 
        && state2 == PFI2_STATE_IDLE 
        && state3 == PFI3_STATE_IDLE ) return ( 1 );
    if ( drops == 3 
        && state1 == PFI1_STATE_IDLE 
        && state2 == PFI2_STATE_IDLE 
        && state3 == PFI3_STATE_IDLE 
        && state4 == PFI4_STATE_IDLE ) return ( 1 );
  }

  Inform( "wait for box to get in zone" );
  if( dp_get( zone_h ) != 1 ) return( 0 );
  dp_registerget( zone_h, REG_BOX, &zone_box );
  Inform( "%03d: in zone, pack docs for box%03d", box, box );

  if( drops > 3 
      && state1 == PFI1_STATE_IDLE 
      && state2 == PFI2_STATE_IDLE 
      && state3 == PFI3_STATE_IDLE 
      && state4 == PFI4_STATE_IDLE 
      && zone_box == box ) return( 1 );
  return( 0 );
}
@ Prototype the function.
@<Prototypes@>+=
int batch_fits( int box, int drops, int sheets, int qSeq );



@* Printer.
@ Printer Send.
@<Functions@>+=
int printer_send( int docId ) {
  int rows, len, net = - 1;
  char *data;
  char evt[ BUF_LEN + 1 ];
  sprintf( evt, "p-%s_prn_comm", pfi );

  if( ( net = net_open( printer, 9100 ) ) <= 0 ) {
    Alert( "Cannot open network connection to printer[%s] port:9100", printer );
    evt_start(evt);
    return( 0 );
  }
  evt_stop(evt);
  Inform( "docid%d: connected to printer[%s]", docId, printer );

  if( sql_query( 
      "SELECT printDoc "
      "FROM rdsDocuments WHERE docSeq=%d", docId ) 
  ) {
    Alert( "docid%d sql err getting pcl data", docId );
    return( 0 );
  }
  if( ( rows = sql_rowcount() ) < 0 ) {
    Alert( "docid%d sql err in rowcount getting doc data", docId );
    return( 0 );
  }
  if( rows == 0 ) return( 0 );

  data = sql_getlen( 0, 0, &len );
  Inform( "docid%d: got %d-bytes doc", docId, len );
  write( net, data, len );
  close( net );
  Trace( "docid%d: %d-bytes doc sent to printer[%s]", docId, len, printer );

  //app_pq_hist( seq, "pfi", "print packslip" );
  ctr_incr( "/pfi/%s/print", shortname );
  return( 1 );
}
@ Prototype the function.
@<Prototypes@>+=
int printer_send( int docId );

@ Is ready to drop? We can drop, when box matches packslip box.
We retrieve carton from conveyor zone and from pfi machine.
Returns 1, if the box in the pfi machine matches box in zone,
Otherwise, returns 0.
@<Functions@>+=
int ready_to_drop( void ) {
  int state, fbox, zbox, box;

  //Inform(" ready to drop");

  if( !dp_get( grip_dp ) ) return( 0 );
  dp_registerget( grip_dp, PFI_REG_BOX, &fbox );
  dp_registerget( grip_dp, PFI_REG_STATE, &state );
  if( state != PFI4_STATE_EXTEND_FULL ) return( 0 );
  if( fbox == -2 ) {
    Inform( "purge in progress, drop" );
    return( 1 );
  }
  box = fbox % 1000;
  if (zone_h < 0) return( 1 );
  if( !dp_get( zone_h ) ) return( 0 );
  dp_registerget( zone_h, REG_BOX, &zbox );
  dp_registerget( zone_h, REG_STATE, &state );
  if( state != STATE_FULL && state != STATE_FILL_X ) return( 0 );
  //Inform( "ready to drop 1");   
  if( box != zbox ) return( 0 );
  return( 1 );
}
@ Prototype the function.
@<Prototypes@>+=
int ready_to_drop( void );



@ Drop paper.
@<Functions@>+=
void drop( void ) {
  int fbox, box, qid, state;

  dp_registerget( grip_dp, PFI_REG_BOX, &fbox );
  box = fbox % 1000;
  qid = fbox / 1000;

  if( fbox > 0 ) {
    sql_query( "UPDATE pfiQueue SET dropped = dropped + 1 WHERE qSeq=%d", qid);
    Inform( "%03d: qid%d allow to drop, wait for completion", box, qid );
  }

  dp_set( drop_go, 1 );
  do {
    dp_registerget( grip_dp, PFI_REG_STATE, &state );
    usleep( 10 * 1000 );
  } while( state != PFI4_STATE_RETRACT && state != PFI4_STATE_IDLE && state != PFI4_STATE_FAULT);
  //} while( state != SF_RAISE && state != SF_FAULT );

  char *val = sql_getvalue(
     "SELECT docType FROM rdsDocuments "
     "JOIN pfiQueue USING (docSeq) "
     "WHERE qSeq=%d", qid
  );
  char doc_type[BUF_LEN+1];
  util_strcpy(doc_type, val);
  doc_type[BUF_LEN]='\0';
  int seq = 0;
  if(box > 0) seq = util_box_get_int( box, "seq" );

  if( fbox > 0 ) {
    if( state == PFI4_STATE_FAULT ) {
      Alert( "%03d: qid%d failed to complete drop", box, qid );
      sql_query( "UPDATE pfiQueue SET canceled=NOW() WHERE qSeq=%d", qid );

      if(valid_str(doc_type)) {
         sql_query(
            "REPLACE INTO rdsCartonData "
            "SET cartonSeq=%d, "
            "dataType='%s', "
            "dataValue='error' ",
            util_carton_get_int(seq,"refValue"), doc_type
         );
      }
      util_update_status(seq, "inserts", "failed", "grip");
    }
    else {
      Trace( "%03d: qid%d drop completed", box, qid );
      if(valid_str(doc_type)) {
         sql_query(
            "REPLACE INTO rdsCartonData "
            "SET cartonSeq=%d, "
            "dataType='%s', "
            "dataValue='ok' ",
            util_carton_get_int(seq,"refValue"), doc_type
         );
      }
      sql_query( "UPDATE pfiQueue SET inserted=NOW() WHERE qSeq=%d AND "
                 "drops=dropped", qid );
      ctr_incr( "/pfi/%s/drop", shortname );
      val = sql_getvalue(
         "SELECT drops=dropped FROM pfiQueue "
         "WHERE qSeq=%d", qid
      ); 
      if(valid_str(val) && atoi(val)==1) {
         util_update_status(seq, "inserts", "complete", "");
      }
    }
  }
}
@ Prototype the function.
@<Prototypes@>+=
void drop( void );



@ check release.
@ Returns 1 is box is releasing from conveyor zone.
@ Returns 0 if box is not releasing from zone.
@<Functions@>=
int check_release( void ) {
  int box, id = 0, state;
  char *psz;

  dp_registerget( zone_h, REG_BOX, &box );
  dp_registerget( zone_h, REG_STATE, &state );

  if( bPFIFaulted ) return( 0 );     //don't release a box at faulted machine
  if( purge_counter > 0 ) return( 0 );  //purging is in progress, do nor release
  if( !dp_get( zone_h ) ) return( 0 );
  if( state != STATE_FULL && state != STATE_FILL_X ) return( 0 );

  int seq = 0;
  if (box > 0) seq = util_box_get_int( box, "seq" ); 
  //Inform( "check_release");
  if(box > 0 && !util_required(seq, "inserts")) {
    ctr_incr( "/pfi/%s/box-release", shortname );
    dp_set( zone_go_h, 1 );
    return( 1 );
  }

  if( box > 0 ) {
    if( ( id = util_box_get_int( box, "seq" ) ) <= 0 ) {
//      Alert( "%03d: invalid id (%d) in check_release from util_box_get_int", box, id );
      return( 0 );
    }
    if( ( psz = sql_getvalue( "SELECT qSeq FROM pfiQueue WHERE cartonId=%d AND "
                              "(inserted IS NULL OR canceled IS NOT NULL) "
                               " AND pfi = '%s' " 
                              "ORDER BY qSeq LIMIT 1", id, shortname ) ) != NULL ) {
      //Inform( "%03d: id%d next qid%d, do not release box", box, id, atoi(psz) );
      return( 0 );
    }
    if( ( psz = sql_getvalue( "SELECT qSeq FROM pfiQueue WHERE cartonId=%d AND "
                              "inserted > DATE_SUB(NOW(), INTERVAL 500000 MICROSECOND) "
                               " AND pfi = '%s' " 
                              "ORDER BY qSeq LIMIT 1", id, shortname ) ) != NULL ) {
      //Inform( "%03d: id%d next qid%d, do not release box", box, id, atoi(psz) );
      return( 0 );
    }
  }
 
  if( dp_get( zone_go_h ) ) {
     return( 0 ); //already in process of releasing box
  }
  if( box > 0 ) {
    ctr_incr( "/pfi/%s/box-release", shortname );
    util_update_status( id, "inserts", "complete", "" );
    util_update_description( id, area, "pfi processing complete" );
  }
  dp_set( zone_go_h, 1 );
  return( 1 );
}
@ Prototype the function.
@<Prototypes@>+=
int check_release( void );



@ check pfi machines status.
@<Functions@>=
void check_status( void ) {
  static int init = 0 ;
  static int m[ 4 ];
  int i, state, fbox, zbox, check_zbox, bFault = 0;

   //Inform(" check_status ");

  if( dp_get( 0 ) == 0 ) {
    init = 0;
    return;
  }

  if( !init ) {
    char name[ 32 ];
    sprintf( name, "p-%s.formax", pfi ); m[ 0 ] = dp_handle( name );
    sprintf( name, "p-%s.platform", pfi ); m[ 1 ] = dp_handle( name );
    sprintf( name, "p-%s.pocket", pfi ); m[ 2 ] = dp_handle( name );
    sprintf( name, "p-%s.grip", pfi ); m[ 3 ] = dp_handle( name );
  }
  char dp_name[BUF_LEN+1];
  for( i = 0; i <= 3; i++ ) {
    dp_registerget( m[ i ], PFI_REG_STATE, &state );
    sprintf(dp_name,"p-%s.%s", pfi, (i==0 ? "formax" :
                                    (i==1 ? "platform" :
                                    (i==2 ? "pocket" : "grip")))
    );
    if( state == 99 ) {
      bFault = 1;
      dp_registerget( m[ i ], PFI_REG_BOX, &fbox );
      evt_start(dp_name);
      if( fbox >= 0 ){
        Trace("%03d: invalidate box in %d", fbox, i);
        dp_registerset( m[ i ], PFI_REG_BOX, BOX_ANON );//invalidate bx
      }
    } else {
      evt_stop(dp_name);
    }
  }

  //if gripping a page that's for a box that's gone, we need to purge.
  //hold upstream zone and declare fault
  dp_registerget(m[ 3 ], PFI_REG_BOX, &fbox);
  fbox = fbox % 1000;
  dp_registerget(zone_h, REG_BOX, &zbox);
  dp_registerget( zone_h, REG_STATE, &state );


  if(!dp_get(fault_dp) && fbox != -1 && (state == STATE_FULL || state == STATE_FILL_X) ) {

    int box_missing = 1;
    int checkzone_h = schedzone_h;
    while (zone_h != checkzone_h) {
      dp_registerget(checkzone_h, REG_BOX, &check_zbox);
    
      if(check_zbox == fbox) {
        box_missing = 0;
        break;
      }
      dp_registerget(checkzone_h, REG_NEXT, &checkzone_h);
    }
    if(zbox == fbox) box_missing = 0;
    if(fbox == -2 || box_missing) {
      Alert("pfi is holding a page for a box no longer here (%d); fault pfi", fbox);
      dp_registerset( pfi_dp, PFI_REG_STATE, PFI_STATE_FAULT ); 
    }
  }
  bPFIFaulted = bFault;

  if( bFault ) {
    int already_faulted = dp_get(zone_fault_h);
    if(!already_faulted) {
       Trace( "pfi fault detected; fault zone" );
       dp_set( zone_fault_h, 1 );
    }
  }
}
@ Prototype the function.
@<Prototypes@>+=
void check_status( void );



@ check for purge
@<Functions@>=
int check_purge( void ) {
  if (dp_get(purge_dp)){
    if (purge_counter == 0){
      Trace("start purging... clear queue");
      ClearQueue();
    }

    purge_counter = 1;
  } else {
    purge_counter = 0;
  }
}
@ Prototype the function.
@<Prototypes@>+=
int check_purge( void );



@ The |ClearQueue| function.
@<Functions@>+=
int ClearQueue( void ) {
  int i, total;
  Carton *cartons;

  if( sql_query( "SELECT DISTINCT( cartonId ), box FROM pfiQueue "
                 "WHERE inserted IS NULL AND canceled IS NULL " 
                 "AND printed IS NOT NULL AND pfi = '%s'",shortname ) ) {
    Alert( "sql err getting a list of cartonid from pfiQueue tbl" );
    return( 0 );
  }
  if( ( total = sql_rowcount() ) < 0 ) {
    Alert( "sql err in rowcount getting a list of cartonid from pfiQueue tbl");
    return( 0 );
  }
  if( total == 0 ) return( 1 );

  if( ( cartons = calloc( total, sizeof( Carton ) ) ) == NULL ) {
    Alert( "allocation memory err for %d carton records", total );
    return( 0 );
  }

  for( i = 0; i < total; i++ ) {
    cartons[ i ].cartonId = atoi( sql_get( i, 0 ) );
    cartons[ i ].box = atoi( sql_get( i, 1 ) );
  }

  for( i = 0; i < total; i++ ) {
    if( cartons[ i ].cartonId <= 0 ) continue;
    sql_query( "DELETE FROM pfiQueue WHERE inserted IS NULL AND "
               "canceled IS NULL AND pfi = '%s' AND cartonId=%d", 
               shortname, cartons[i].cartonId );
    Trace( "%03d: id%d mark as failed to insert before clearing queue",
            cartons[ i ].box, cartons[ i ].cartonId );
    util_update_status( cartons[ i ].cartonId, "inserts", "failed", "purge" );
    sql_query(
       "REPLACE INTO rdsCartonData (cartonSeq, dataType, dataValue) "
       "SELECT %d, docType, 'error' "
       "FROM rdsDocuments "
       "WHERE refValue=%d AND docType <> 'shippingLabel' ",
       util_carton_get_int(cartons[ i ].cartonId, "refValue"),
       util_carton_get_int(cartons[ i ].cartonId, "refValue")
    );

  }

  free( cartons );

  return( 1 );

}
@ Prototype the function.
@<Prototypes@>+=
int ClearQueue( void );



@ The |printer_ok| function. Polls the kyocera status data to see if
the printer is OK. Return 1 if printer is OK, returns 0 if printer is faulted.
@<Functions@>+=
int printer_ok( void ) {

  if( !dp_get( 0 ) ) return( 0 );

  char name[ 40 ];
  int fault, state, state1, state2, state3, state4;
  int m1, m2, m3, m4;

  sprintf( name, "%s.formax", shortname );
  m1 = dp_handle( name );
  sprintf( name, "%s.platform", shortname );
  m2 = dp_handle( name );
  sprintf( name, "%s.pocket", shortname );
  m3 = dp_handle( name );
  sprintf( name, "%s.grip", shortname );
  m4 = dp_handle( name );

  dp_registerget( dp_handle(shortname), PFI_REG_STATE, &state );
  fault = dp_get(fault_dp);
  dp_registerget( m1, PFI_REG_STATE, &state1 );
  dp_registerget( m2, PFI_REG_STATE, &state2 );
  dp_registerget( m3, PFI_REG_STATE, &state3 );
  dp_registerget( m4, PFI_REG_STATE, &state4 );

  if(       fault ||  state == 99 || state1 == 99 ||
     state2 == 99 || state3 == 99 || state4 == 99) return( 0 );

  return( 1 );
}
@ Prototype the function.
@<Prototypes@>+=
int printer_ok( void );



@ The |printer_ready| function. Returns 1 if printer is ready to accept data,
returns 0 if printer is not ready yet to accept data.
@<Functions@>+=
int printer_ready( void ) {
  char *value = sql_getvalue( "SELECT value from webObjects WHERE "
                              "name='prtDisplayLine1'" );
  if( value == NULL ) return( 0 );
  return( strcasecmp( value, "Ready" ) == 0 );
}
@ Prototype the function.
@<Prototypes@>+=
int printer_ready( void );



@ Count number of substrings in a string.
@<Functions@>+=
int count_string( const char *str, const char *sub ) {
  int length, count = 0;

  if( ( length = strlen( sub ) ) == 0 ) return( 0 );
  for( str = strstr( str, sub ); str; str = strstr( str + length, sub ) )
    ++count;
  return( count );
}
@ Prototype the function.
@<Prototypes@>+=
int count_string( const char *str, const char *sub );

@* Index.
