% 
%   purge_replication.w 
%
%   Author: Mark Woodworth 
%
%   History:
%      2007-10-03 -- check in (mrw)
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
%     (C) Copyright 2007 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Purge Replication}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Remove replication files that are older than a fixed number of days.

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
\centerline{RCS ID: $Id: purge_replication.w,v 1.4 2019/12/13 20:59:23 rds Exp $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2007 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: purge_replication.w,v 1.4 2019/12/13 20:59:23 rds Exp $" ;
@<Defines@>@;
@<Includes@>@;
int
main(int argc, char *argv[])
   {
   int days ;
   time_t now ;
   char log_name[80+1] ;

   trn_register("purge") ;
   Trace("purging replication files") ;

   days = KEEP_DAYS ;
   log_name[0] = '\0' ;
   now = time(NULL) ;

   @<Find the youngest@>@;
   @<Purge the logs@>@;
   }

@ Configuration
@<Defines@>+=
#define PURGE_DIR "/var/lib/mysql/"
#define KEEP_DAYS (14) 

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <sys/stat.h>
#include <sys/time.h>

#include <rds_sql.h>
#include <rds_trn.h>

@ Find the youngest of the files that are too old.
@<Find the youngest@>+=
   {
   int err ;

   sql_setconnection("localhost","root","numina","mysql") ;

   err = sql_query("show master logs") ;
   if (!err) 
      {
      int i, n ;
      time_t newest ;

      newest = 0 ;
      n = sql_rowcount() ; 
      Trace("master log count %d  keep %d days",n,days) ; 
      for(i=0 ; i<n ; i++)
         {
         char file_name[80+1], path_name[80+1] ;
         struct stat file_stat ;
         int old ;

         strncpy(file_name,sql_get(i,0),80) ;
         file_name[80] = '\0' ;
         strcpy(path_name,PURGE_DIR) ;
         strcat(path_name,file_name) ;
         stat(path_name,&file_stat) ;
         old = (now-file_stat.st_ctime)/(24*60*60) ; 
         Trace("%s days old %d",path_name,old) ;

         if(old > days)
            {
            if(file_stat.st_ctime >= newest)
               {
               newest = file_stat.st_ctime ;
               strcpy(log_name,file_name) ;
               }
            }
         }
      }
   }

@ Purge up to the youngest of the too old, if we have found one.
@<Purge the logs@>+=
   {
   if(log_name[0]!='\0')
      {
      Trace(    "purge master logs to '%s'",log_name) ;   
      sql_query("purge master logs to '%s'",log_name) ;
      }
   else
      Trace("no files to purge") ;
   }
