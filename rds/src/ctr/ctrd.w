%  ctrd.web
%
%   Author: Mark Woodworth
%
%   History:
%      09/09/99   - started (mrw)
%      2006-07-14 - change to 'counters' table --AHM
%      8/6/08    -- added support for ctr_incr(), counts table (rme)
%      2010-07-30 - remove 'B' and 'Z' codes, remove zone
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
%     (C) Copyright 1999--2010 Numina Group, Inc.  All Rights Reserved.
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
This program reads the counter message queue and updates records
into the database.
\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.  
It must not be disclosed to any person who does not have a current signed
non-disclosure form on file with Numina Group, Inc..  
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
\centerline{RCS Revision $ $Revision: 1.4 $ $}
\centerline{RCS Date $ $Date: 2010/10/05 22:42:04 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Introduction. 
This program reads messages and updates Postgres database.
@c
static char rcsid[] = "$Id: ctrd.w,v 1.4 2010/10/05 22:42:04 rds Exp $" ;
@<Includes@>@;
#define FALSE (0)
#define TRUE  (!FALSE)
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
   exit(EXIT_SUCCESS) ;
   }

@ The following include files are used.
@<Includes@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <rds_ctr.h>
#include <rds_msg.h>
#include <rds_sql.h>
#include <rds_trn.h>

@ First let's register with Tron and the Watchdog.
@<Initialize@>=
   {
   trn_register("ctrd") ;
   Inform(rcsid) ;

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
         Alert("   ctrd") ;
         Alert("   ctrd <machine>") ;
         Alert("   ctrd <machine> <user> <password> <database>") ;
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
char txt[CTR_MSG_LEN+1] ;
int  reader=CTR_MSG ;

@ The loop goes as long as there are messages.
@<Process@>+=
   {
   while( (next=msg_type(reader)) >0 )
      {
      switch(next)
         {
         case CTR_MSG :
            {
            /* ctr message */
            char cmd [32+1] ;
            char code[CTR_MSG_LEN+1] ;
            int n ;

            msg = msg_recv(reader) ;
            strncpy(txt,msg,CTR_MSG_LEN);
            txt[CTR_MSG_LEN] = '\0';
            free(msg) ;

            n = sscanf(txt,"%s %s",
                       cmd,
                       code) ;

            if(n==2)
               ctr_log(cmd,code) ;
            else
               Alert("bad [%s]",txt) ;
            break ;
            }

         default:
            break ;
         }

      msg_next(reader) ;
      }
   }

@ Insert into the ctr tables.
@<Functions@>+=
int ctr_log(char *cmd, char *code) 
   {
   int err ;

   /* make sure there is a counters table entry */
   err = sql_query("SELECT * "
                   "FROM counters "
                   "WHERE code='%s' ",
                   code) ;
   if(!err)
      {
      if(sql_rowcount()==0)
         {
         err = sql_query("INSERT INTO counters "
                         "(code,description) "
                         "VALUES "
                         "('%s','%s') ",code,code) ;
         if(err)
            {
            Alert("SQL error %d insert counters",err) ;
            Alert("cmd [%c], code [%s]",cmd[0],code) ;

            return -2 ;
            }
         }
      }
   else
      {
      Alert("SQL error %d select counters",err) ;
      Alert("cmd [%c], code [%s]",cmd[0],code) ;

      return -1 ;
      }
        
   if(cmd[0]=='I')
      {
      err = sql_query("INSERT INTO counts SET "
                      "code='%s', "
                      "stamp=NOW(), "
                      "value=1",
                      code);
      if(err)
         {
         Alert("SQL error %d INSERT INTO counts",err) ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;

         return -3 ;
         }

      return 0 ;
      }

   Alert("unknown cmd [%c]",cmd[0]) ;
   return -4 ;
   }

@* Index.
