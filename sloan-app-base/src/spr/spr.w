% 
%   spr.w -- simple print application
%
%   Author: Richard Ernst
%
%   History:
% 2004-07-19 -- check in (mrw)
% 2010-05-13 (mdo) adapted for use with laser printer
% 01/11/2011 ank: modified for O&M, removed sending ctrl^D, added simples/duplex
%   pcl tweaking
% 03/03/2011 ank: fixed minor bugs, added reverse portrait/landscape switch
% 03/10/2011 ank: modified for promat: replaced get_id() with app_get_id(),
%   added get_pack(), getting now barcode from recentCartons tbl instead of
%   pieces tbl, updated includes
% 05/09/2011 ank: modified for BSC: changed reffences to functions and constants
%   from rds_pcl.h to rds_pcl5.h lib, changed packlabels tbl to packslips tbl
% 08/04/2011 ank: added get_pack_test() function and 'test' control parameter
% 10/31/2011 ank: changed 'convey' dp to 'feed' dp
% 12/02/2011 ank: added call pcl5_strippjl() to print_packslip() function
% 3/29/2014 rme: renamed dpr, modified queuing mechanism
% 06/03/2014 ahm: renamed spr, simplified
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
%     (C) Copyright 2014 Numina Systems Corporation.  All Rights Reserved.
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
\def\myitem{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{dpr}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This application prints packing slips on a network laser printer.

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
\centerline{Authors: Richard Ernst}
\centerline{RCS ID: $ $Id: spr.w,v 1.1 2024/10/18 18:04:00 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2014 Numina Systems Corporation.  
All Rights Reserved.}
}


@* Overview.
@c
static char rcsid[] = "$Id: spr.w,v 1.1 2024/10/18 18:04:00 rds Exp $" ;
@<Includes@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int
main( int argc, char *argv[] ) {
  int queue_seq, doc_seq;

  @<Initialize@>@;
  
  for( ; ; usleep( 50 * 1000 ) ) {
    if ( !get_queued_doc( &queue_seq, &doc_seq ) )
      continue;  // no documents to print

    print_doc( doc_seq );
  }
}



@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/signal.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <string.h>

#include <rds_evt.h>
#include <rds_net.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_util.h>


@ We store and instance ID and the local service number. 
@<Globals@>+=
char myName[ 64 ];
char printer_ip[ 32+1 ];
int printer_port;
int printer_timeout;
int printer_retry;


@ Initialize.
@<Initialize@>=
{
   if (argc > 1)
      strcpy( myName, argv[ 1 ] );
   else
      strcpy( myName, "spr" );
   trn_register( myName );

   strcpy( printer_ip, util_get_control( myName, "ip", "" ) );
   printer_port = atoi( util_get_control( myName, "port", "9100" ) );
   printer_timeout = atoi( util_get_control( myName, "timeout", "25" ) );
   printer_retry = atoi( util_get_control( myName, "retry", "3" ) );
   Trace( "Init printer [%s:%d]", printer_ip, printer_port );
}


@* Functions.
@ |get_queue_seq()|.
@<Functions@>+=
int get_queued_doc( int *queue_seq, int *doc_seq ) {
   int err;
   char sql[ 255 ];

   sprintf( sql,
         "SELECT queueSeq, docSeq "
         "FROM docQueue WHERE device='%s' "
         "AND complete=0 "
         "ORDER BY queueSeq LIMIT 1", myName );
   err = sql_query( sql );
   if ( err ) {
      Alert( "sql error [%s]", sql );
      return FALSE;
   }
   if ( sql_rowcount() != 1 )
      return FALSE;
   *queue_seq = atoi( sql_getbyname( 0, "queueSeq" ) );
   *doc_seq = atoi( sql_getbyname( 0, "docSeq" ) );
   Inform( "process seq [%d], doc [%d]", *queue_seq, *doc_seq );
   sql_query( "UPDATE docQueue SET complete=1 "
              "WHERE queueSeq=%d", *queue_seq );
   return TRUE;
}
@ Prototype the function.
@<Prototypes@>+=
int get_queued_doc( int *queue_seq, int *doc_seq );


@ The |print_doc| function. Returns 1 on success, 0 on error.
@<Functions@>+=
int print_doc( int doc_seq ) {
  int iLen, fd, iSentBytes;
  char *doc, sId[ 22 ];
  char evt_code[ 128+1 ];

  doc = get_doc( doc_seq, &iLen );
  if( doc == NULL || iLen <= 0 ) {
    Alert( "doc %d not found", doc_seq );
    sprintf( evt_code, "%s_doc_missing", myName );
    evt_instant( evt_code );
    return( 0 );
  }

  sprintf( evt_code, "%s_comm", myName );
  if ((fd = net_open_with_timeout(
      printer_ip, printer_port, printer_timeout, printer_retry )) <= 0) {
    Alert( "printer err: [%s] not on network", printer_ip );
    free( doc );
    evt_start( evt_code );
    return( 0 );
  }
  iSentBytes = write( fd, doc, iLen );

  Trace( "sent %d of %d bytes of doc %d to printer [%s]",
         iSentBytes, iLen, doc_seq, printer_ip );
  free( doc );
  close( fd );
  evt_stop( evt_code );

  return( 1 );
}
@ Prototype the function.
@<Prototypes@>+=
int print_doc( int doc_seq );


@ The |get_doc| function. Retrieves data from table by id.
@<Functions@>+=
char *get_doc( int doc_seq, int *piLen ) {
  int err;
  char *doc, *p;

  Inform( "getting data for doc %d", doc_seq );
  err = sql_query( "SELECT printDoc FROM rdsDocuments WHERE docSeq=%d", doc_seq );
  if( !err && sql_rowcount() == 1 ) {
    p = sql_getlen( 0, 0, piLen );
    if( *piLen > 0 ) {
      doc = calloc( *piLen, 1 );
      memcpy( doc, p, *piLen );
      return( doc );
    }
  }
  Alert( "no data in tbl for doc %d", doc_seq );
  *piLen = 0;
  return( NULL );
}
@ Prototype.
@<Prototypes@>+=
char *get_doc( int id, int *piLen );


@* Utility functions.

@ Signal handler for SIGALARM. Used by the |net_open_with_timeout()| function.
@<Functions@>+=
void fnSigAlarm( int sig ) {
  //Inform( "timeout" );
}
@ Prototype the function.
@<Prototypes@>+=
static void fnSigAlarm( int sig );


@ Open network connection.
@<Functions@>+=
int net_open_with_timeout( char *host, int port, int iTimeoutMsec, int attempts ) {
  int i, n;
  struct itimerval guard, zero;
  static struct sigaction action;

  if( attempts <= 0 ) attempts = 1;
  if( iTimeoutMsec <= 0 ) iTimeoutMsec = 1;

  for( i = 0; i < attempts; i++ ) {
    if( i>0 )
      Alert( "connection attempt #%d failed, retry...", i );

    guard.it_value.tv_sec = iTimeoutMsec / 1000;
    guard.it_value.tv_usec = iTimeoutMsec % 1000 * 1000;
    guard.it_interval.tv_sec = 0;
    guard.it_interval.tv_usec = 0;

    zero.it_value.tv_sec = 0;
    zero.it_value.tv_usec = 0;
    zero.it_interval.tv_sec = 0;
    zero.it_interval.tv_usec = 0;

    action.sa_handler = fnSigAlarm;
    //action.sa_sigaction = NULL;
    //action.sa_restorer = NULL;
    //action.sa_flags = SA_RESETHAND;
    sigemptyset( &action.sa_mask );
    sigaction( SIGALRM, &action, NULL );

    setitimer( ITIMER_REAL, &guard, NULL );
    n = net_open( host, port );
    setitimer( ITIMER_REAL, &zero, NULL );
    if( n >= 0 ) break;
  }
  return( n );
}
@ Prototype the function.
@<Prototypes@>+=
int net_open_with_timeout( char *host, int port, int iTimeoutMsec, int attempts );


@* Index.
