%                       
%   rp_load.web
%
%   Author: Mark Woodworth 
%
%   History:
%      11/01/00 - init (mrw)
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
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Register Point Load Utility}
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
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
A utility to load the values of Trak RPs.
The RPs must already be defined.

@c
@<Includes@>@;
int
main(int argc, char *argv[])
   {
   @<Initialize@>@;

   @<Load@>@;
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

   }

@* Load. 
@<Load@>=
   {
   char name[TRAK_NAME_LEN+1] ;
   int value ;
   char text[128+1] ;
   int n ;
   int handle ;

   while(fgets(text,128,stdin))
      {
      n = sscanf(text,"%s\t%d\n",name,&value) ;
      if(n==2)
         {
         handle = rp_handle(name) ;
         rp_set(handle,value) ;
         }
      }
   }

@* Index.

