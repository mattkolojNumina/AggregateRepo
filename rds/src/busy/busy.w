% 
%   busy.web 
%
%   Author: Mark Woodworth 
%
%   History:
%      2004-07-19 -- check in (mrw)
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
%     (C) Copyright 2004 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Busy}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Note the CPU usage.

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
\centerline{Authors: Mark Woodworth}
\centerline{RCS ID: $ $Id: busy.w,v 1.1 2008/03/25 19:53:50 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2004 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: busy.w,v 1.1 2008/03/25 19:53:50 rds Exp $" ;
@<Defines@>@;
@<Includes@>@;
main(int argc, char *argv[])
   {
   int cpu ;
   char line[128+1] ;
   char name[32+1] ;
   long int is_user, is_nice, is_system, is_idle, is_total;
   long int was_user, was_nice, was_system, was_idle, was_total ;
   int delta_used, delta_total ;
   int percent ;

   trn_register("busy") ;
   Trace(rcsid) ;
 
   was_user = was_nice = was_system = was_idle = was_total = -1 ; 
   cpu = open("/proc/stat",O_RDONLY) ;
   while(cpu>=0)
      {
      lseek(cpu,0,SEEK_SET) ;
      if(read(cpu,line,64))
         {
         sscanf(line,"%s %ld %ld %ld %ld",
                name,&is_user,&is_nice,&is_system,&is_idle) ;
         if(was_user>=0)
            {
            is_total  = is_user  + is_nice  + is_system  + is_idle ;
            was_total = was_user + was_nice + was_system + was_idle ;
            delta_used = (is_total - is_idle) - (was_total - was_idle) ;
            delta_total = (is_total - was_total) ;
            if(delta_total>0)
               {
               percent = 10000 * delta_used / delta_total ;
               if(percent>=TRIP)
                  {
                  Inform("%d.%02.2d%c cpu used\n",
                         percent/100, percent % 100, '%') ;
                  }
               }
            }
         was_user = is_user ;
         was_nice = is_nice ;
         was_system = is_system ;
         was_idle = is_idle ;
         }
      sleep(1) ;
      }
   close(cpu) ;
   }

@ Trip Level is in hundredths of a percent of total. 
We set the level for reporting at 10 percent.
@<Defines@>+=
#define TRIP (10*100)

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

#include <rds_trn.h>
