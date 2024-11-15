%
%   stateView.w -- prints the state changes for a DP (reg 1) 
%
%   Author: Mark Woodworth
%
%   History:
%      2024-02-26 -MRW- started 
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
%     (C) Copyright 2024 NuminaGroup, Inc.  All Rights Reserved.
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
\def\title{Belt Merge}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program controls merges on the sorter recirculation line. 

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
\centerline{Control Revision: $ $Revision: 1.4 $ $}
\centerline{Control Date: $ $Date: 2024/04/17 12:33:28 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 NuminaGroup, Inc.  
All Rights Reserved.}
}

@* Overview. 
This program manages four merges on the sorter recirc line by turning on and
off the hold DPs for the infeed belts.

@c
static char rcsid[] = "$Id: stateView.w,v 1.4 2024/04/17 12:33:28 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;

int 
main(int argc,char *argv[]) 
  {
  int state = -1 ;
  @<Initialize@>@;

  while(1)
    {
    int now ;
    dp_registerget(dp_h,1,&now) ;
    if(now != state)
      {
      state = now ;
      printf("%s state %d\n",dp,state) ;
      }
    usleep(10000L) ;
    } 
  
  exit( EXIT_SUCCESS );
  }

@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>      // for signal handling
#include <sys/select.h>  // for select()
#include <sys/time.h>    // for gettimeofday()

#include <rds_evt.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_util.h>

@ Definitions.
@<Defines@>+=
#define BUF_LEN            32  // length of small statically allocated strings
#define MSG_LEN           255  // general message length


@ General variables for this program are stored globally.
@<Globals@>+=
char dp[32+1] ;
int dp_h = -1 ;

@ Initialization.  Register for tracing and initialize global variables.
@<Initialize@>=
  {
  if(argc<2)
    {
    fprintf(stderr,"usage: %s <dp>",argv[0]) ;
    exit(1) ;
    }

  strncpy(dp,argv[1],32) ;
  dp[32] = '\0' ;

  dp_h = dp_handle(dp) ;
  if(dp_h<0)
    {
    fprintf(stderr,"cannot find dp %s",dp) ;
    exit(2) ;
    }
  printf("dp %s handle %d\n",dp,dp_h) ;
  }

@* Index.
