%  evtd.web
%
%   Author: Mark Woodworth
%
%   History:
%      09/09/99 - started (mrw)
%      07/21/06 - don't restart active events, changed time functions,
%                 tracing, and table names (ahm)
%      07/30/10 - cleanup; change duration field to int (ahm)
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
%     (C) Copyright 1999--2010 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Event Daemon}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program reads the event message queue and loads messages 
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
\centerline{RCS Revision $ $Revision: 1.12 $ $}
\centerline{RCS Date $ $Date: 2017/10/02 19:26:00 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1999 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Introduction. 
This program reads messages and updates Postgres database.
@c
static char rcsid[] = "$Id: evtd.w,v 1.12 2017/10/02 19:26:00 rds Exp $" ;
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
#include <string.h>
#include <unistd.h>
#include <sys/types.h>

#include <rds_evt.h>
#include <rds_msg.h>
#include <rds_sql.h>
#include <rds_trn.h>

@ Register with tron; set sql connection parameters (if specified).
@<Initialize@>=
   {
   trn_register("evtd") ;
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
         Alert("   evtd") ;
         Alert("   evtd <machine>") ;
         Alert("   evtd <machine> <user> <password> <database>") ;
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
char txt[EVT_MSG_LEN+1] ;
int  reader=EVT_MSG ;

@ The loop goes as long as there are messages.
@<Process@>+=
   {
   while( (next=msg_type(reader)) >0 )
      {
      switch(next)
         {
         case EVT_MSG :
            {
            /* evt message */
            char cmd [32+1] ;
            char code[EVT_MSG_LEN+1] ;
            time_t stamp ;
            int n ;

            msg = msg_recv(reader) ;
            strncpy(txt,msg,EVT_MSG_LEN) ;
            txt[EVT_MSG_LEN]='\0' ;
            free(msg) ;
            
            n = sscanf(txt,"%s %ld %s",
                       cmd,
                       &stamp,
                       code) ;

            if(n==3)
               evt_log(cmd,code,stamp) ;
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

@ Insert into the evt tables.
@<Functions@>+=
int evt_log(char *cmd, char *code, time_t stamp) 
   {
   char state[32+1];
   int err ;

   /* make sure there is an events table entry */
   err = sql_query( "SELECT state FROM events WHERE code='%s'",code) ;
   if(!err)
      {
      if(sql_rowcount()==1)
         {
         strcpy( state, sql_get(0,0) ) ;
         }
      else
         {
         strcpy( state, "off" ) ;
         err = sql_query("INSERT INTO events "
                         "(code,description) "
                         "VALUES "
                         "('%s','%s')",code,code) ;
         if(err)
            {
            Alert("SQL error %d insert events",err) ;
            Alert("cmd [%c], code [%s]",cmd[0],code) ;
            return -2 ;
            }
         }
      }
   else
      {
      Alert("SQL error %d select events",err) ;
      Alert("cmd [%c], code [%s]",cmd[0],code) ;
      return -1 ;
      }
        
   if(cmd[0]=='A')  /* evt_instant */
      {
      Trace( "instant event: [%s]", code ) ;

      if (strcmp(state,"on") == 0)
         err = sql_query("UPDATE events "
                         "SET state='off' "
                         "WHERE code='%s'",code) ;
      else
         err = sql_query("UPDATE events "
                         "SET state = 'off', "
                         "start = FROM_UNIXTIME(%ld) "
                         "WHERE code='%s'",stamp,code) ;
      if(err)
         {
         Alert("SQL error %d update events",err) ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -3 ;
         }

      err = sql_query("INSERT INTO eventLog "
                      "(code,state,start,duration) "
                      "VALUES ('%s','off',FROM_UNIXTIME(%ld),0)",
                      code,stamp) ;
      err = err || sql_query("UPDATE eventLog "
                      "SET state='off', "
                      "duration = %ld-UNIX_TIMESTAMP(start) "
                      "WHERE code='%s' "
                      "AND state='on'",stamp,code) ;
      if(err)
         {
         Alert("SQL error %d insert eventLog",err)  ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -4 ; 
         }

      notify( code, "instant" );

      return 0 ;
      }


   if(cmd[0]=='B')  /* evt_start */
      {
      // only proceed if event is currently off
      if (strcmp(state,"off") != 0)
         return 0 ;

      Trace( "start event: [%s]", code ) ;
      err = sql_query("UPDATE events "
                      "SET state='on', "
                      "start = FROM_UNIXTIME(%ld) "
                      "WHERE code='%s' ",stamp,code) ;
      if(err)
         {
         Alert("SQL error %d update events",err) ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -3 ;
         }

      err = sql_query("INSERT INTO eventLog "
                      "(code,state,start,duration) "
                      "VALUES ('%s','on',FROM_UNIXTIME(%ld),0)",
                      code,stamp) ;
      if(err)
         {
         Alert("SQL error %d insert eventLog",err)  ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -4 ; 
         }

      notify( code, "start" );

      return 0 ;
      }


   if(cmd[0]=='C')  /* evt_stop */
      {
      // only proceed if event is currently on
      if (strcmp(state,"on") != 0)
         return 0 ;

      Trace( "stop event: [%s]", code ) ;
      err = sql_query("UPDATE events "
                      "SET state='off' "
                      "WHERE code='%s' ",code) ;
      if(err)
         {
         Alert("SQL error %d update events",err) ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -3 ;
         }
     
      err = sql_query("UPDATE eventLog "
                      "SET state='off', "
                      "duration = %ld-UNIX_TIMESTAMP(start) "
                      "WHERE state='on' "
                      "AND code='%s' ",
                      stamp,code) ;
      if(err)
         {
         Alert("SQL error %d insert eventLog",err)  ;
         Alert("cmd [%c], code [%s]",cmd[0],code) ;
         return -4 ; 
         }

      notify( code, "stop" );

      return 0 ;
      }

   Alert("unknown cmd [%c],cmd[0]") ;
   return -5 ;
   }



@ Send a notification.
@<Functions@>+=
int notify(char *code, char *cmd) 
   {
   char *val;
   char message[255+1];
   int err;

   // do nothing if the events table doesn't have the notify column
   if ( !sql_getvalue( "SELECT * FROM information_schema.COLUMNS "
                       "WHERE TABLE_SCHEMA = 'rds' "
                       "AND TABLE_NAME = 'events' "
                       "AND COLUMN_NAME = 'notify'" ) )
      {
      return -1;
      }

   // do nothing if the event is a notification failure
   if ( !strcmp( code, "notifier" ) )
      return -2;

   val = sql_getvalue( "SELECT notify FROM events WHERE code='%s'", code );
   if ( val && ( strlen( val ) > 0 ) )
      {
      char recipient[32+1];
      char description[255+1];
      char hostAlias[64+1];
      strcpy( recipient, val );
      val = sql_getvalue( "SELECT description FROM events WHERE code='%s'", code );
      if ( val )
         {
         strcpy( description, val );
         val = sql_getcontrol( "notifier", "hostAlias" );
         strcpy( hostAlias, "RDS system" );
         if ( val )
            strcpy( hostAlias, val );
         if ( !strcmp( cmd, "start" ) )
            sprintf( message, "[%s]\nevent start:\n[%s]", hostAlias, description );
         if ( !strcmp( cmd, "stop" ) )
            sprintf( message, "[%s]\nevent stop:\n[%s]", hostAlias, description );
         if ( !strcmp( cmd, "instant" ) )
            sprintf( message, "[%s]\nevent:\n[%s]", hostAlias, description );
         err = sql_query( "INSERT INTO notifications SET "
                          "recipient='%s', "
                          "message='%s'",
                          recipient, message );
         if(err)
            {
            Alert("SQL error %d insert notifications",err) ;
            return -3;
            }
         }
      }
   return 0;
   }


@* Index.
