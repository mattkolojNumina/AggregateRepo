%                       
%   watch.web
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
\def\title{Watch Utility}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This is an interface to the Accusort Scanning System.

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
A utility to watch the values of Trak DPs.

@c
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
int
main(int argc, char *argv[])
   {
   @<Initialize@>@;

   for(;;)
      {
      @<Show@>@; 
      usleep(100000) ;
      }
   }

@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>

#include <rds_trak.h>

@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   int i ;
   for(i=1 ; i<argc ; i++)
      if(hn<HANDLE_N)
         handle[hn++] = dp_handle(argv[i]) ;

   printf("\n") ;
   for(i=0 ; i<hn ; i++)
      printf("%7s ",argv[i+1]) ;
   printf("\n") ;
   }

@* Show. 
@<Show@>=
   {
   int i ;

   printf("\r") ;
   for(i=0 ; i<hn ; i++)
      {
      printf("%5d %1d ",dp_counter(handle[i]),dp_get(handle[i])) ;
      }
   fflush(stdout) ;
   }

@ Handle count.
@<Defines@>+=
#define HANDLE_N (10) 

@ Handle storage and number.
@<Globals@>+=
int hn=0 ;
int handle[HANDLE_N+1] ;

