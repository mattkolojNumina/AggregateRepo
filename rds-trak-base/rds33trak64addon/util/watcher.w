%                       
%   watcher.w
%
%   Author: Mark Woodworth 
%
%   History:
%      10/30/19 - init (mrw)
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
%     (C) Copyright 2019 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Watcher}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Slaves the amber beacons to an input for test.

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
\centerline{Control Date: $ $Date: 2000/11/15 11:58:06 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2019 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
A utility to watch for events on DP 0.

@c
@<Includes@>@;
@<Globals@>@;
int
main(int argc, char *argv[])
   {
   @<Initialize@>@;

   while(1)
     {
     @<Watch@>@;
     usleep(100000L) ;
     }
   }

@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>

#include <rds_trn.h>
#include <rds_trak.h>

@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   trn_register("watcher") ;
   }

@* Watch. 
@<Watch@>=
   {
   Dp_Record *record ;

   record = dp_pointer(0) ;
   if(record->ev_head != head)
     {
     head = record->ev_head ;
     Alert("dp 0 ev_head %d",head) ;
     } 
   }

@ Has.
@<Globals@>+=
int head= 0 ;

@* Index.

