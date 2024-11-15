%                       
%   trakd.web
%
%   Author: Mark Woodworth 
%
%   History:
%      050128 - init (mrw)
%      050329 - added rp functionality (clw)
% change
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
%     (C) Copyright 2005 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Trak Daemon}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Monitors an SQL table for Trak write requests.

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
\centerline{Control Date: $ $Date: 2019/11/25 21:27:55 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2005 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program reads an SQL table for Trak write instructions.
@c
static char rcsid[] = "$Id: trakd.w,v 1.4 2019/11/25 21:27:55 rds Exp $" ;
@<Includes@>@;
@<Globals@>@;
int
main(int argc, char *argv[])
   {
   @<Initialize@>@;

   for(;;)
      {
      @<Check handles@>@;
      @<Read from trak@>@;
      @<Write to trak@>@;
      @<Save values@>@;
      @<Write defaults@>@;
      usleep(200000L) ;
      }
   }

@ Includes.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>

#include <rds_trn.h>
#include <rds_trak.h>
#include <rds_sql.h>


@ Initialization. Here we register for tracing.
@<Initialize@>+=
   {
   trn_register("trakd") ;
   Trace(rcsid) ;

   gethostname( hostname, 79 );
   }


@* Handles.  
@<Globals@>+=
int last_trak = -1 ;
char hostname[80];

@ If trak starts or stops we grab handles.
@<Check handles@>+=
   {
   int trak ;
   int err, i, n ;
   void *rez ;

   trak = dp_counter(0) ;
   if(trak != last_trak)
      {
      last_trak = trak ;
      Inform("getting handles") ;
      
      err = sql_query("SELECT zone, name, register FROM trak "
                      "WHERE `host`='%s'", hostname ) ;
      if(!err)
         {
         rez = sql_get_result() ;
         if(rez)
            {
            n = sql_rowcount_r(rez) ;
            for(i=0 ; i<n ; i++)
               {
               char zone[2+1] ;
               char name[32+1] ;
               int reg ;
               int handle, value ;
              
               zone[0] = '\0' ;
               if(sql_get_r(rez,i,0))
                  {
                  strncpy(zone,sql_get_r(rez,i,0),2) ;
                  zone[2] = '\0' ;
                  } 

               name[0] = '\0' ;
               if(sql_get_r(rez,i,1))
                  {
                  strncpy(name,sql_get_r(rez,i,1),32) ;
                  name[32] = '\0' ;
                  }

               reg = -1 ;
               if(sql_get_r(rez,i,2))
                  {
                  reg = atoi(sql_get_r(rez,i,2)) ;
                  }

               handle = -1 ;
               if(0==strcmp(zone,"dp"))
                  handle = dp_handle(name) ;
               if(0==strcmp(zone,"rp"))
                  handle = rp_handle(name) ;
               
               err = sql_query("UPDATE trak SET handle=%d "
                               "WHERE zone='%s' " 
                               "AND name='%s' "
                               "AND `host`='%s'",
                               handle,zone,name,hostname) ;
               Trace("zone %s name %s handle %d",zone,name,handle) ;
               }
            sql_free_result(rez) ;
            } 
         }
      }  
   }

@* Read. 
@<Read from trak@>+=
   {
   int trak ;
   int err, i, n ;
   void *rez ;

   trak = dp_get(0) ;
   if(trak)
      {
      err = sql_query("SELECT zone, name, register, `get`, handle FROM trak "
                      "WHERE handle >= 0 "
                      "AND `host`='%s'", hostname ) ;
      if(!err)
         {
         rez = sql_get_result() ;
         if(rez)
            {
            n = sql_rowcount_r(rez) ;
            for(i=0 ; i<n ; i++)
               {
               char zone[2+1] ;
               char name[32+1] ;
               int reg ;
               int get ;
               int handle ;
              
               zone[0] = '\0' ;
               if(sql_get_r(rez,i,0))
                  {
                  strncpy(zone,sql_get_r(rez,i,0),2) ;
                  zone[2] = '\0' ;
                  } 

               name[0] = '\0' ;
               if(sql_get_r(rez,i,1))
                  {
                  strncpy(name,sql_get_r(rez,i,1),32) ;
                  name[32] = '\0' ;
                  }

               reg = -1 ;
               if(sql_get_r(rez,i,2))
                  {
                  reg = atoi(sql_get_r(rez,i,2)) ;
                  }

               get = 0 ;
               if(sql_get_r(rez,i,3))
                  get = atoi(sql_get_r(rez,i,3)) ;

               handle = -1 ;
               if(sql_get_r(rez,i,4))
                  handle = atoi(sql_get_r(rez,i,4)) ;

               if((0==strcmp(zone,"dp")) || (0==strcmp(zone,"rp")))
                  {
                  if(handle>=0)
                     {
                     int is ;

                     if(0==strcmp(zone,"dp"))
                        {
                        if(reg<0)
                           is = dp_get(handle) ;
                        else
                           dp_registerget(handle,reg,&is) ;
                        }
                     else
                        {
                        is = rp_get(handle) ;
                        }

                     if(is != get)
                        {
                        err = sql_query("UPDATE trak "
                                        "SET `get` = %d "
                                        "WHERE zone = '%s' "
                                        "AND name = '%s' "
                                        "AND register = %d "
                                        "AND `host`='%s'",
                                        is,zone,name,reg,hostname) ;

                        }
                     }
                  }             
               }
            sql_free_result(rez) ;
            } 
         }
      }
   }


@* Write. 
@<Write to trak@>+=
   {
   int trak ;
   int err, i, n ;
   void *rez ;

   trak = dp_get(0) ;
   if(trak)
      {
      err = sql_query("SELECT zone, name, register, put, handle FROM trak "
                      "WHERE handle >= 0 "
                      "AND state='write' "
                      "AND `host`='%s'", hostname ) ;
      if(!err)
         {
         rez = sql_get_result() ;
         if(rez)
            {
            n = sql_rowcount_r(rez) ;
            for(i=0 ; i<n ; i++)
               {
               char zone[2+1] ;
               char name[32+1] ;
               int reg ;
               int put ;
               int handle ;
              
               zone[0] = '\0' ;
               if(sql_get_r(rez,i,0))
                  {
                  strncpy(zone,sql_get_r(rez,i,0),2) ;
                  zone[2] = '\0' ;
                  } 

               name[0] = '\0' ;
               if(sql_get_r(rez,i,1))
                  {
                  strncpy(name,sql_get_r(rez,i,1),32) ;
                  name[32] = '\0' ;
                  }

               reg = -1 ;
               if(sql_get_r(rez,i,2))
                  reg = atoi(sql_get_r(rez,i,2)) ;

               put = 0 ;
               if(sql_get_r(rez,i,3))
                  put = atoi(sql_get_r(rez,i,3)) ;

               handle = -1 ;
               if(sql_get_r(rez,i,4))
                  handle = atoi(sql_get_r(rez,i,4)) ;

               if((0==strcmp(zone,"dp")) || (0==strcmp(zone,"rp")))
                  {
                  if(handle>=0)
                     {
                     if(0==strcmp(zone,"dp")) 
                        {
                        if(reg<0)
                           dp_set(handle,put) ;  
                        else
                           dp_registerset(handle,reg,put) ;
                        } 
                     else
                        {
                        rp_set(handle,put) ;  
                        }
                     err = sql_query("UPDATE trak "
                                     "SET state = 'idle', "
                                     "    `get` = put "
                                     "WHERE zone = '%s' "
                                     "AND name = '%s' "
                                     "AND register = %d "
                                     "AND `host`='%s'",
                                      zone,name,reg,hostname) ;

                     Trace("zone %s name %s reg %d put %d",zone,name,reg,put) ;
                     }
                  }
                   
               }
            sql_free_result(rez) ;
            } 
         }
      }
   }

@ Save values. Copy current to standard.
@<Save values@>=
  {
  int err, count ;

  count = 0 ;
  err = sql_query("SELECT count(*) FROM trak "
                  "WHERE state='save' ") ;
  if(!err)
    if(sql_rowcount()>0)
        if(sql_get(0,0))
          count = atoi(sql_get(0,0)) ;  

  if(count>0)
    sql_query("UPDATE trak set state='idle',standard=`get` WHERE state='save'") ; 
}

@ much like the put values, except we put the standard, not the put column.
@<Write defaults@>=
   {
   int trak ;
   int err, i, n ;
   void *rez ;

   trak = dp_get(0) ;
   if(trak)
      {
      err = sql_query("SELECT zone, name, register, standard, handle FROM trak "
                      "WHERE handle >= 0 "
                      "AND state='standard' "
                      "AND `host`='%s'", hostname ) ;
      if(!err)
         {
         rez = sql_get_result() ;
         if(rez)
            {
            n = sql_rowcount_r(rez) ;
            for(i=0 ; i<n ; i++)
               {
               char zone[2+1] ;
               char name[32+1] ;
               int reg ;
               int put ;
               int handle ;
              
               zone[0] = '\0' ;
               if(sql_get_r(rez,i,0))
                  {
                  strncpy(zone,sql_get_r(rez,i,0),2) ;
                  zone[2] = '\0' ;
                  } 

               name[0] = '\0' ;
               if(sql_get_r(rez,i,1))
                  {
                  strncpy(name,sql_get_r(rez,i,1),32) ;
                  name[32] = '\0' ;
                  }

               reg = -1 ;
               if(sql_get_r(rez,i,2))
                  {
                  reg = atoi(sql_get_r(rez,i,2)) ;
                  }

               put = 0 ;
               if(sql_get_r(rez,i,3))
                  put = atoi(sql_get_r(rez,i,3)) ;

               handle = -1 ;
               if(sql_get_r(rez,i,4))
                  handle = atoi(sql_get_r(rez,i,4)) ;

               if((0==strcmp(zone,"dp")) || (0==strcmp(zone,"rp")))
                  {
                  if(handle>=0)
                     {
                     if(0==strcmp(zone,"dp")) 
                        {
                        if(reg<0)
                           dp_set(handle,put) ;  
                        else
                           dp_registerset(handle,reg,put) ;
                        }
                     else
                        {
                        rp_set(handle,put) ;  
                        }

                     err = sql_query("UPDATE trak "
                                     "SET state = 'idle' "
                                     "WHERE zone = '%s' "
                                     "AND name = '%s' "
                                     "AND `host`='%s'",
                                      zone,name,hostname) ;

                     Trace("zone %s name %s put %d",zone,name,put) ;
                     }
                  }
                   
               }
            sql_free_result(rez) ;
            } 
         }
      }
   }

@* Index.
