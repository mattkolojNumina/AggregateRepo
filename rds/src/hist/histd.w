%  histd.web
%
%   Author: Richard Ernst 
%
%   History:
%      02/24/05 - started (rme)
%      2006-07-14 changed to cartonLog table, cleaned up tracing --AHM
%      Jan. 5, 2009 (rme) added msec timing
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
%     (C) Copyright 2004--2010 Numina Systems Corporation.  All Rights Reserved.
%
%
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
%
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
%
\def\title{Counter Daemon}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program reads the hist message queue and updates records
into the database.
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
\bigskip
\centerline{Author: Mark Woodworth}
\centerline{Format Date: \today}
\centerline{RCS Revision $ $Revision: 1.5 $ $}
\centerline{RCS Date $ $Date: 2014/07/11 21:27:30 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Introduction. 
This program reads hist messages and updates the database.
@c
static char rcsid[] = "$Id: histd.w,v 1.5 2014/07/11 21:27:30 rds Exp $" ;
@<Includes@>@;
@<Globals@>@;
@<Functions@>@;

int main(int argc,char *argv[])
   {
   @<Initialize@>@;

   while(1) 
      {
      @<Process@>@;
      sleep(5) ;
      }
   return 0 ;
   }

@ The following include files are used.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <rds_hist.h>
#include <rds_msg.h>
#include <rds_sql.h>
#include <rds_trn.h>

@ First let's register with Tron and the Watchdog.
@<Initialize@>=
   {
   trn_register("histd") ;
   Trace(rcsid) ;

   switch (argc)
      {
      case 1:
         break ;
      case 2:
         Trace("connecting to default database on %s",argv[1]) ;
         sql_setconnection(argv[1],"rds","rds","rds") ;
         break ;
      case 5:
         Trace("connecting to %s database on %s",argv[4],argv[1]) ;
         sql_setconnection(argv[1],argv[2],argv[3],argv[4]) ;
         break ;
      default:
         Alert("usage:") ;
         Alert("   histd") ;
         Alert("   histd <machine>") ;
         Alert("   histd <machine> <user> <password> <database>") ;
         exit(EXIT_FAILURE) ;
         break ;
      }
   }

@* Process.
We read as many messages as we can.

@ The message read is pointed to by |txt|.  
The next type is kept in |next|.
The reader number is kept in |reader|.
@<Globals@>+=
int  next ;
char *msg ;
char txt[HIST_MSG_LEN+1] ;
int  reader=HIST_MSG ;

@ The loop goes as long as there are messages.
@<Process@>+=
   {
   while( (next=msg_type(reader)) >0 )
      {
      switch(next)
         {
         case HIST_MSG :
            {
            /* hist message */
            char carton[32+1] ;
            char code[32+1] ;
            char description[HIST_MSG_LEN+1] ;
            char timestamp[32+1] ;
            char timestampusec[32+1] ;
            int start, stop  ;

            msg = msg_recv(reader) ;
            strncpy(txt,msg,HIST_MSG_LEN);
            txt[HIST_MSG_LEN] = '\0' ;
            free(msg) ;

            start = 0 ;
            stop = 0 ;

            while (txt[stop] != '\t')
               stop++ ;
            if (stop-start > 32)
               {
               Alert( "field length exceeded for carton id in hist msg" );
               break;
               }
            strncpy(carton,txt+start,stop-start) ;
            carton[stop-start] = '\0' ;
            stop++ ;
            start = stop ;

            while (txt[stop] != '\t')
               stop++ ;
            if (stop-start > 32)
               {
               Alert( "field length exceeded for code in hist msg" );
               break;
               }
            strncpy(code,txt+start,stop-start) ;
            code[stop-start] = '\0' ;
            stop++ ;
            start = stop ;

            while (txt[stop] != '\t')
               stop++ ;
            if (stop-start > 32)
               {
               Alert( "field length exceeded for timestamp in hist msg" );
               break;
               }
            strncpy(timestamp,txt+start,stop-start) ;
            timestamp[stop-start] = '\0' ;
            stop++ ;
            start = stop ;

            while (txt[stop] != '\t')
               stop++ ;
            if (stop-start > 32)
               {
               Alert( "field length exceeded for timestamp usec in hist msg" );
               break;
               }
            strncpy(timestampusec,txt+start,stop-start) ;
            timestampusec[stop-start] = '\0' ;
            stop++ ;
            start = stop ;

            strncpy(description,txt+stop,HIST_MSG_LEN) ;
            description[HIST_MSG_LEN] = '\0' ;

            hist_log(carton,code,description,timestamp,timestampusec) ;
            break ;
            }

         default:
            break ;
         }

      msg_next(reader) ;
      }
   }

@ Insert into the cartonLog table.
@<Functions@>+=
int hist_log(char *carton, char *code, char *description, 
             char *timestamp, char *timestampusec)
   {
   int err ;

   if (carton == NULL || strlen(carton) == 0 || carton[0] == '?')
      return ;
   err = sql_query( "INSERT INTO cartonLog "
                    "SET id='%s',"
                    "code='%s',"
                    "description='%s',"
                    "stamp=FROM_UNIXTIME(%s),"
                    "msec=%s/1000",
                    carton,code,description,timestamp,timestampusec) ;
   if(err)
      {
      Alert("SQL error %d insert cartonLog",err) ;
      Alert("carton [%s] code [%s]",carton,code) ;
      Alert("descr [%s] stamp [%s]",description,timestamp) ;
      return -2 ;
      }
   }

@* Index.
