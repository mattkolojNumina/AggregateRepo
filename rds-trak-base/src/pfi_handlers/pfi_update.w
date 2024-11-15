%
%   pfi_update.w -- update carton data
%
%   Author: Adam Marshall
%
%   History:
%      2015-05-05 -AHM- init, for Sunstar print/apply
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
%     (C) Copyright 2015 Numina Group, Inc.  All Rights Reserved.
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
\def\title{update}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program applies business rules to interpret data captured from field
devices (scanners, scale, etc.).

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
\centerline{Control Revision: $ $Revision: 1.33 $ $}
\centerline{Control Date: $ $Date: 2023/04/04 12:25:31 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This program applies business rules to interpret data captured from field
devices (scanners, scale, etc.).

@c
static char rcsid[] = "$Id: pfi_update.w,v 1.33 2023/04/04 12:25:31 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Functions@>@;
@<Prototypes@>@;

int main( int argc, char *argv[] ) {
   int box;
   int seq;

   @<initialize@>@;
   @<process carton@>@;

   exit( EXIT_SUCCESS );
}


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>  // for gettimeofday()

#include <rds_trn.h>
#include <rds_sql.h>
#include <rds_net.h>
#include <rds_trak.h>
#include <rds_hist.h>
#include <rds_ctr.h>
#include <rds_util.h>

#include <zonetrak.h>
#include <app.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN         32  // length of small statically allocated strings
#define MSG_LEN        256  // length of longer message strings

//box data values for the pfi servo to read
#define VERY_SMALL     110
#define SMALL          100
#define MEDIUM          90
#define TALL            80
#define VERY_TALL       70

@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LEN + 1 ];
char description[ MSG_LEN + 1 ];
int line;


@ Initialization.
@<initialize@>+=
{
   if (argc != 3) {
      printf( "usage: %s (line) <box>\n", argv[0] );
      exit( EXIT_FAILURE );
   }

   strcpy( area, "pfi" );
   sprintf( name, "pfiupdate" );
   trn_register( name );

   line = atoi( argv[1] );
   box = atoi( argv[2] );
   seq = -1;
   if (!util_valid_seq(box, &seq, name)) {
      exit( EXIT_SUCCESS );
   }

   Trace( "%03d: update carton [%d]", box, seq );
}


@ Update the carton.
@<process carton@>=
{
   struct timeval start_time;

   char barcode[ BUF_LEN + 1 ];

   int barcode_success = FALSE;
   int height_success = FALSE;
   int done = FALSE;

   @<begin processing@>@;
   Inform( "%03d: begin processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
 
   @<get barcode@>@;
   Inform( "%03d: get barcode complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
   
   if (!done)
   {
      if(util_required(seq, "height")) {
	     @<gauge height@>@;
   	     Inform( "%03d: height measurement complete for carton [%d], took %.3f sec",
                 box, seq, util_get_elapsed( start_time ) );
      }
   }
   
   @<finish processing@>@;
   Inform( "%03d: finish processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
}


@ Begin processing: initialization, etc.
@<begin processing@>=
{
   gettimeofday( &start_time, NULL );

   strcpy( barcode, "" );

   barcode_success = FALSE;
}


@ Finish processing.
@<finish processing@>=
{
   if(barcode_success && height_success)
     util_set_stamp(seq, "updateStamp");
   Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
   util_zone_release( name );
}


@ Get the barcode for the carton.  Apply business rules to the scan result
to extract a single valid identifier barcode.  If zero or multiple valid
barcodes are found, this is an error.
@<get barcode@>=
{
   char dev_name[ BUF_LEN + 1 ];
   char dev_msg[ MSG_LEN + 1 ];

   strncpy( dev_name, util_get_control( name, "scan", "" ), BUF_LEN );
   dev_name[ BUF_LEN ] = '\0';   
   strcpy( dev_msg, "" );
   
   int result = util_read_scanner( box, seq, "pfi-scan", dev_name, dev_msg );   
   
   barcode_success = FALSE;

   if (valid_str(dev_msg)) {
      util_carton_set(seq, "inductScan", dev_msg);
   }
   if (result > 0) {
      barcode_success = app_parse_induct_scan( box, seq, dev_name, dev_msg, barcode );
   }
   if (util_do_consec( dev_name, barcode_success ))
      util_zone_fault( name );
}



@ Determine whether box is small, medium, tall, or very tall
@<gauge height@>=
{

	char height_detector[ BUF_LEN + 1 ];
	char height[ BUF_LEN + 1 ];
   char desc [MSG_LEN+1];

   strncpy( height_detector, util_get_control( name, "heightDetector", "pe110a-1" ), BUF_LEN );
	
	get_box_height(box, seq, height_detector, height);
	
	Inform("%03d: seq %d height is %s", box, seq, height);
		
	if(height != NULL && strlen(height) > 0 || strcmp(height,"unknown") != 0) {
	   height_success = TRUE;
	   sprintf(desc, "determined box is %s", height);
   	util_update_status(seq, "height", "complete", height);				 
	} else {
	   sprintf(desc, "failed to get height");
   	util_update_status(seq, "height", "failed", "");				 
	}
	util_update_description(seq, area, desc);

	if (util_do_consec(height_detector, height_success))
	   util_zone_fault(name);
}

@ Determine the approximate height of a box from an array of 4 photoeyes
@<Functions@>+=
int get_box_height( int box, int seq, const char height_detector[], char height[] ) {

	char pe_small[ BUF_LEN + 1 ];
	char pe_medium[ BUF_LEN + 1 ];
	char pe_tall[ BUF_LEN + 1 ];
	char pe_vtall[ BUF_LEN + 1 ];
	
	sprintf(pe_small,  "%sd", height_detector);
	sprintf(pe_medium, "%sc", height_detector);
	sprintf(pe_tall,   "%sb", height_detector);
	sprintf(pe_vtall,  "%sa", height_detector);
   
	int pe_small_dp = dp_handle(pe_small);
	int pe_medium_dp = dp_handle(pe_medium);
	int pe_tall_dp = dp_handle(pe_tall);
	int pe_vtall_dp = dp_handle(pe_vtall);

   ctr_incr("/line%dHeight", line);

	strcpy(height, "unknown");	
   height[BUF_LEN]='\0';

   if (pe_small_dp < 0 ||	
       pe_medium_dp < 0 ||	
       pe_tall_dp < 0 ||	
       pe_vtall_dp < 0) {
      Alert("height detector photoeyes not defined!");
      ctr_incr("/line2Height/%s", height);
      return 0;
   } 


	if(dp_get(pe_vtall_dp)) {strcpy(height, "very_tall"); bx_setdata(box, VERY_TALL);}
	else if(dp_get(pe_tall_dp)) {strcpy(height, "tall"); bx_setdata(box, TALL);}
	else if(dp_get(pe_medium_dp)) {strcpy(height, "medium"); bx_setdata(box, MEDIUM);}
	else if(dp_get(pe_small_dp)) {strcpy(height, "small"); bx_setdata(box, SMALL);}
	else {strcpy(height, "very_small"); bx_setdata(box, VERY_SMALL);}

	height[BUF_LEN]='\0';
   ctr_incr("/line%dHeight/%s", line, height);

   return 1;
}
@ Prototype the function.
@<Prototypes@>+=
int get_box_height( int box, int seq, const char height_detector[], char height[] );


@* Index.
