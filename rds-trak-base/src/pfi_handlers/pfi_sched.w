%
%   xpal_sched.w -- schedule printing
%
%   Author: Adam Marshall
%
%   History:
%      2011-12-08 -AHM- init, for X-Press PAL
%      2014-04-30 -AHM- modified for OneStep+
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
%     (C) Copyright 2011-2015 Numina Group, Inc.  All Rights Reserved.
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
\def\title{schedule}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program schedules printing.

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
\centerline{Control Revision: $ $Revision: 1.29 $ $}
\centerline{Control Date: $ $Date: 2023/03/22 14:36:19 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Group, Inc.
All Rights Reserved.}
}

@* Overview. 
This program schedules printing.

@c
static char rcsid[] = "$Id: pfi_sched.w,v 1.29 2023/03/22 14:36:19 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int main( int argc, char *argv[] ) {
   int box;
   int line;
   int seq;

   @<initialize@>@;
   @<process box@>@;

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <zonetrak.h>
#include <rds_ctr.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN           32  // length of small statically allocated strings
#define LABEL_LEN      10000  // length of longer messages strings
#define SLEEP_DURATION   100  // sleep between host-monitoring attempts (msec)
#define TIMEOUT         10.0  // response timeout (sec)
#define ZERO_STAMP   "0000-00-00 00:00:00"  // timestamp for error
#define STATUS_ORD       299  // max status ordinal for error check


@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];
char msg[255+1];

@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s (line) <box>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   strcpy( area, "pfi" );
   sprintf( name, "pfisched" );
   trn_register( name );

   line = atoi( argv[1] );
   box = atoi( argv[2] );
   if (!util_valid_seq(box, &seq, name)) {
      exit( EXIT_SUCCESS );
   }

}


@ Process the box.
@<process box@>=
{
   struct timeval start_time;
   char lpn[255+1];
   strcpy(lpn,"");   
   int label_success = 0;
    
   @<begin processing@>@;

   if(strlen(lpn) > 0)
     @<schedule inserts@>@;
   
   @<finish processing@>@;
}


@ Begin processing: initialization, etc.
@<begin processing@>=
{

   gettimeofday( &start_time, NULL );
   util_strcpy(lpn,util_carton_get(seq, "barcode"));
   lpn[255]='\0';
   if(lpn!=NULL && strlen(lpn)>0) {
     Trace( "%03d: schedule carton [%s] (seq %d)", box, lpn, seq );
   }

}


@ Finish processing.
@<finish processing@>=
{
   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
   util_zone_release( name );
}

@ Schedule printing.
@<schedule inserts@>=
{  
 
 	 int have_docs = 0;
    int carton_seq = util_carton_get_int(seq, "refValue");

    char printer[BUF_LEN+1];
    strcpy(printer, util_get_control(name, "pfi", "pfi1"));
    printer[BUF_LEN]='\0';

    if(carton_seq <= 0) {
       util_update_description(seq,area,"carton lookup failed");
       util_update_status(seq,"schedule","failed","lookup");
    }
    else if(!util_required( seq, "schedule")) {
         Inform( "%03d: host/print not required",box) ;
         util_update_description(seq,area,"printing not required");		 
    }
	 else {
       ctr_incr("/pfi/%s", printer);
       ctr_incr("/pfi/%s/queue", printer);
       char *val = sql_getvalue(
	      "SELECT COUNT(*) FROM rdsDocuments WHERE refType='cartonSeq' "
		   "AND refValue='%d' AND docType!='shippingLabel'", carton_seq
	    );
	    if(val!=NULL && strlen(val) > 0) {
	       have_docs = (atoi(val) > 0);
	    }
    }
	
    if(util_required( seq, "schedule") && have_docs) {
	  label_success = 1;	
	  Inform("%03d: scheduling print at %s for [%s] (conveyorBoxes seq %d)",
		    box, printer, lpn, seq);
     app_queue_pfi_docs(box, seq, printer);
     ctr_incr("/pfi/%s/queue/pass", printer);
     ctr_incr("/pfi/%s/queue/pass/queued", printer);
	  util_carton_set(seq,"topPrinter",printer);
	  util_update_status( seq,"schedule","complete","");                      
     util_set_stamp(seq,"schedStamp");
    } 
    else {
      char ins_req[BUF_LEN+1];
      strcpy(ins_req,"true");
      util_strcpy( ins_req, util_rds_carton_data_get(carton_seq, "insertRequired") );
      ins_req[BUF_LEN]='\0';
      if (strcmp(ins_req,"true")==0) {
         ctr_incr("/pfi/%s/queue/fail", printer);
         ctr_incr("/pfi/%s/queue/fail/missing", printer);
         Alert("%03d: missing inserts for conveyorBoxes seq %d (rdsCartons seq %d)", 
               box, seq, carton_seq);
 	      util_update_status( seq,"schedule","failed","no docs");                      
         util_update_description(seq, area, "missing inserts for [%s]", lpn);
      } else {
         Trace("%03d: no inserts required for conveyorBoxes seq %d (rdsCartons seq %d)", 
               box, seq, carton_seq);
         ctr_incr("/pfi/%s/queue/pass", printer);
         ctr_incr("/pfi/%s/queue/pass/noinserts", printer);
 	      util_update_status( seq,"schedule","optional","");                    
  	      util_update_status( seq,"inserts","optional","noinserts");                     
         sql_query(
           "REPLACE rdsCartonData "
           "SET cartonSeq=%d, "
           "dataType='insertStatus', "
           "dataValue='ok'", 
           carton_seq
         );
         util_update_description(seq, area, "no inserts required for [%s]", lpn);
      }
    }
}


@* Index.
