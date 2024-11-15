% 
%
%   rds_sql.web
%
%   Author: Mark Olson, Alex Korzhuk
%
%   History:
%      7/12/2000 (mdo) -- RDS 3 revision to trak and RDS
%      7/19/2004 (mrw) -- added multiple result code
%      8/9/2004  (ank) -- rewrote sql_query, added sql_escape and sql_getlen
%                         functions
%      6/23/2005 (ank) -- put different variations of the functions for
%                         multiple result code in one file
%      2006-07-14 (AHM) - fixed return value of sql_getvalue() when zero
%                         or multiple matches are found
%      3/01/2007 ank: added reconnect flag set in sql_attach()
%      3/30/2010 rme: added time checking, sql_setMaxTime()
%      7/29/2015 ank: added connection timeout and attempts in sql_attach(),
%                     addeed sql_setConnTimeout() function
       2/03/2021 ank: added sql_affected_rows() function
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
%     (C) Copyright 2000-2010 Numina Systems Corporation.  All Rights Reserved.
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
\def\dot{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{RDS3 SQL}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This is a wrapper for mySQL access.  This package gives
a simpler interface to connection, query, and results handling. 

It is one of the auxiliary packages of the RDS system.


%
% --- confidential
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
\centerline{Authors: Mark Olson}
\centerline{Revision Date: \today}
\centerline{Version: 3.0}
}


%
% --- copyright
%
\def\botofcontents{\vfill
\centerline{\copyright 2010 Numina Systems Corporation.  
All Rights Reserved.}
}



@* RDS SQL. 

This shared library gives the RDS system a quick way to access the mySQL database.


@* Shared Library. 

@(rds_sql.c@>=
static char *rcsId="$ $Id: rds_sql.w,v 1.16 2021/08/09 21:01:37 rds Exp $ $";

@<Includes@>@;
@<Defines@>@;
@<Static Globals@>@;
@<Local Prototypes@>@;
@<Local Functions@>@;
@<Exported Functions@>@;


@ The header file |rds_sql.h| provides structures and  prototypes to
the RDS applications. 
@(rds_sql.h@>=
#ifndef __SQL_H
#define __SQL_H
#include <mysql/mysql.h>

typedef struct resNode
   {
   char value[128] ;
   struct resNode *next ;
   } resultNode ;

  @<Exported Prototypes@>@;
#endif


@* Library Implementation. 

@We start with the includes.
@<Includes@>=
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include <time.h>
#include <signal.h>
#include <sys/time.h>
#include <rds_trn.h>
#include "rds_sql.h"

@ These static globals hold the connection and result status between function
calls.
@<Static Globals@>+=
static MYSQL *conn;
static MYSQL_RES *res;
static MYSQL_ROW row;
static MYSQL_FIELD *field;
static int connTimeoutSec = 3;
static int connAttempts = 2;
static int maxTime = 1000000;
struct timeval startTime;
struct timeval endTime;

@ The connection parameters.
@<Static Globals@>+=
static char machine[ 80 ];
static char user[ 80 ];
static char password[ 80 ];
static char database[ 80 ];

@ Connection parameter defaults.
@<Defines@>+=
#define DEFAULT_MACHINE  "db"
#define DEFAULT_USER     "rds" 
#define DEFAULT_PASSWORD "rds"
#define DEFAULT_DATABASE "rds"

@ Attach.  This function should be called before any call to the mySQL server.
If it returns a non-zero error value, the connection could not be made.
@<Local Functions@>+=
static int sql_attach( void ) 
   {
   void (*handler)(int ) ;
   int err, i, bSuccess;

   if(!conn)
      {
      if(machine[0] =='\0') strcpy(machine, DEFAULT_MACHINE ) ;
      if(user[0]    =='\0') strcpy(user,    DEFAULT_USER    ) ;
      if(password[0]=='\0') strcpy(password,DEFAULT_PASSWORD) ;
      if(database[0]=='\0') strcpy(database,DEFAULT_DATABASE) ;

      conn = mysql_init(NULL) ;
      if(!conn)
         return mysql_errno(conn) ;

      conn->reconnect = ( my_bool )1;
      unsigned uConnTimeout = ( unsigned )connTimeoutSec;
      mysql_options( conn, MYSQL_OPT_CONNECT_TIMEOUT, ( char* )&uConnTimeout );

      for( i = 0, bSuccess = 0; i < connAttempts; i++ ) {
        if( mysql_real_connect(conn,machine,user,password,database,0,NULL,0)) {
          bSuccess = 1;
          break;
        }
      }
      if( bSuccess == 0 ) return mysql_errno( conn );

      if(res)
         {
         mysql_free_result(res) ;
         res = NULL ;
         }
      Inform("SQL connected to %s %s as %s",machine,database,user) ;
      }

   handler = signal(SIGPIPE,SIG_IGN) ;
   err = mysql_ping(conn) ;      
   signal(SIGPIPE,handler) ;

   if(err)
      {
      if( conn ) 
         {
         mysql_close( conn );
         conn = NULL;
         }
      Alert("SQL lost connection to %s %s",machine,database) ;
      return err ;
      }

   return 0 ;
   }

@ Prototype.
@<Local Prototypes@>+=
static int sql_attach( void );


@ |sql_setconnection()|.
@<Exported Functions@>+=
void sql_setconnection( const char *set_machine, 
                        const char *set_user, 
                        const char *set_password, 
                        const char *set_database ) 
   {
   strcpy( machine,  set_machine );
   strcpy( user,     set_user );
   strcpy( password, set_password );
   strcpy( database, set_database );
   }

@ Prototype.
@<Exported Prototypes@>+=
void sql_setconnection( const char *set_machine, 
                        const char *set_user, 
                        const char *set_password, 
                        const char *set_database );


@ Programs can close a connection to prepare make a new one.
@<Exported Functions@>+=
void sql_close( void ) 
   {
   if( res ) 
      { 
      mysql_free_result( res );
      res = NULL;
      }

   if( conn ) 
      {
      mysql_close( conn );
      conn = NULL;
      }
   }

@ Prototype.
@<Exported Prototypes@>+=
void sql_close( void );


@ |sql_query()|.
@<Exported Functions@>+=
int sql_query( char *fmt, ... ) 
   {
   va_list ap;
   char *query;
   int err;

   startTimeCheck();

   va_start( ap, fmt );
   vasprintf( &query, fmt, ap );
   va_end( ap );

   if(err=sql_attach())
      return err ;

   if( res ) 
      {          
      /* free a prior result */
      mysql_free_result( res );
      res = NULL ;
      }

   err = mysql_query( conn, query );
   if( err ) 
      if( conn )
         Trace( "sql error %u: %s", mysql_errno( conn ), mysql_error( conn ) );

   doTimeCheck( query );

   free( query );

   return err ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_query( char *fmt, ... );



@ |sql_affected_rows()|.
Retuns (>=0) number of rows affected by previous UPDATE, INSERT or DELETE query.
Retuns (-1) on error.
@<Exported Functions@>+=
int sql_affected_rows( void ) {
  if( conn == NULL ) return( -1 );
  return( ( int )mysql_affected_rows( conn ) );
}
@ Prototype.
@<Exported Prototypes@>+=
int sql_affected_rows( void );



@ |sql_get()|.
@<Exported Functions@>+=
char *sql_get( int iRow, int iColumn ) 
   {
   if( !res ) 
      res = mysql_store_result( conn ) ;
   if( !res )
      return NULL ;

   mysql_data_seek( res, iRow );
   row = mysql_fetch_row( res );
   return row[ iColumn ] ;  
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_get( int iRow, int iColumn );


@ |sql_getlen()|. 
Version of |sql_get| function to return additionally the
length of the field value. 
Useful for BLOB data when strlen() cannot be used.
@<Exported Functions@>+=
char *sql_getlen( int iRow, int iColumn, int *piLen ) 
   {
   unsigned long *aLens;

   *piLen = 0;

   if( !res ) 
      res = mysql_store_result( conn );
   if( !res )
      return NULL ;

   mysql_data_seek( res, iRow );
   row = mysql_fetch_row( res );
   aLens = mysql_fetch_lengths( res );
   *piLen = ( int )aLens[ iColumn ];

   return row[ iColumn ] ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_getlen( int iRow, int iColumn, int *piLen );


@ |sql_rget()|. A second version allows us to use our own result pointer.
@<Exported Functions@>+=
char *sql_rget( MYSQL_RES *rez, int iRow, int iColumn ) 
   {
   if( !rez ) 
      return NULL ;

   mysql_data_seek( rez, iRow );
   row = mysql_fetch_row( rez );
   return row[ iColumn ] ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_rget( MYSQL_RES *rez, int iRow, int iColumn );


@ |sql_get_r|. A variation of the second version
@<Exported Functions@>+=
char *sql_get_r( void *rez, int r, int c ) 
   {
   if( !rez ) 
      return NULL ;

   mysql_data_seek( ( MYSQL_RES* )rez, r );
   row = mysql_fetch_row( ( MYSQL_RES* )rez );
   return row[c];
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_get_r( void *rez, int r, int c );


@ Row Fetch. Returns true if there is another row. Loads internal row.
@<Exported Functions@>+=
int sql_rowfetch( void ) 
   {
   if( !res ) 
      res = mysql_store_result( conn );
   if( !res )
      return 0 ;

   row = mysql_fetch_row( res );
   return( row ? 1 : 0 );
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_rowfetch( void );


@ Row Get. Use with Row Fetch to get a column from the current row.
@<Exported Functions@>+=
char *sql_rowget( int iColumn ) 
   {
   if( !res ) 
      return NULL ;
   if( !row ) 
      return NULL ;
   return row[ iColumn ] ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_rowget( int iColumn );


@ |sql_getcolumn()|.
@<Exported Functions@>+=
int sql_getcolumn( const char *pszFieldName ) 
   {
   int i;

   if( !res ) 
      res = mysql_store_result( conn );
   if( !res )
      return -1 ;

   field = mysql_fetch_fields( res );
   for( i=0 ; i<mysql_num_fields( res ); i++ )
      if( !strcmp( field[ i ].name, pszFieldName ) ) 
         return i ;
   return -1 ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_getcolumn( const char *pszFieldName );

@ |sql_getbyname()|.
@<Exported Functions@>+=
char *sql_getbyname( int iRow, const char *pszFieldName ) 
   {
   int iColumn;

   iColumn = sql_getcolumn( pszFieldName );
   if(iColumn < 0)
      return NULL ;
   return sql_get( iRow, iColumn ) ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_getbyname( int iRow, const char *pszFieldName );


@ |sql_rowcount()|.
@<Exported Functions@>+=
int sql_rowcount( void ) 
   {
   if( !res ) 
      res = mysql_store_result( conn );
   if( !res )
      return 0 ;

   return mysql_num_rows( res ) ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_rowcount( void );


@ |sql_rrowcount()| - a second version uses our result pointer.
@<Exported Functions@>+=
int sql_rrowcount( MYSQL_RES *rez ) 
   {
   if(!rez)
      return 0 ;

   return mysql_num_rows( rez ) ;
   }
 
@ Prototype.
@<Exported Prototypes@>+=
int sql_rrowcount( MYSQL_RES *rez );


@ |sql_rowcount_r()| - a variation of the second version.
@<Exported Functions@>+=
int sql_rowcount_r( void *rez ) 
   {
   if( !rez ) 
      return 0;

   return mysql_num_rows( ( MYSQL_RES* )rez ) ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_rowcount_r( void *rez );


@ SQL Sequence.  Returns the last auto-incremented sequence number.
@<Exported Functions@>+=
unsigned long long sql_sequence( void ) 
   {
   if( conn ) 
      return mysql_insert_id( conn ) ;
   else 
      return 0 ;
   }

@ Prototype.
@<Exported Prototypes@>+=
unsigned long long sql_sequence( void );

  
@ SQL DateTime.  This function returns the date and time as a 
pointer to a statically allocated string in the format used by SQL.
@<Exported Functions@>+=
char *sql_datetime( int when ) 
   {
   static char datetime[ 32 + 1 ];
   struct tm *tm;

   tm = localtime( ( time_t* )&when );
   sprintf( datetime, 
            "%04d-%02d-%02d %02d:%02d:%02d",
            tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday,
            tm->tm_hour, tm->tm_min, tm->tm_sec );
   return( datetime );
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_datetime( int when );


@ SQL TimeStamp.  This function returns a timestamp as a 
pointer to a statically allocated string in the format used by SQL.
@<Exported Functions@>+=
char *sql_timestamp( int when ) 
   {
   static char timestamp[ 32 + 1 ];
   struct tm *tm;

   tm = localtime( ( time_t* )&when );
   sprintf( timestamp, 
            "%04d%02d%02d%02d%02d%02d",
            tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday,
            tm->tm_hour, tm->tm_min, tm->tm_sec );
   return( timestamp );
   }

@ Prototype.
@<Exported Prototypes@>+=
char *sql_timestamp( int when );

@ Escape special characters for sql.
@<Exported Functions@>+=
int sql_escape( const char *pBin, int iBinLen, char *pszEsc ) 
   {
   int i, j;

   for( i = 0, j = 0; i < iBinLen; i++ ) 
      {
      switch( pBin[ i ] ) 
         {
         case '\0':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = '0';
            break;
         case '\\':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = '\\';
            break;
         case '\'':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = '\'';
            break;
         case '\"':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = '\"';
            break;
         case '\n':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = 'n';
            break;
         case '\r':
            pszEsc[ j++ ] = '\\';
            pszEsc[ j++ ] = 'r';
            break;
         default:
            pszEsc[ j++ ]= pBin[ i ];
            break ;
         }
      }

   pszEsc[ j ] = '\0';  /* null terminated */
   return( j );
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_escape( const char *pBin, int iBinLen, char *pszEsc );


@ SQL result. Sometimes it is useful to save off a result set.
@<Exported Functions@>+=
MYSQL_RES *sql_result( void ) 
   {
   MYSQL_RES *result;

   if( !res ) 
      result = mysql_store_result( conn );
   else 
      result = res;
   
   return( result );
   }

@ Prototype.
@<Exported Prototypes@>+=
MYSQL_RES *sql_result( void );


@ |sql_get_result()|. A variation of the |sql_result()|.
@<Exported Functions@>+=
void *sql_get_result( void ) 
   {
   return( mysql_store_result( conn ) );
   }

@ Prototype.
@<Exported Prototypes@>+=
void *sql_get_result( void );


@ |sql_release()|. A helper function cleans up our result sets.
@<Exported Functions@>+=
void sql_release( MYSQL_RES *rez ) 
   {
   if( rez != NULL ) 
      mysql_free_result( rez );
   rez = NULL ;
   }

@ Prototype.
@<Exported Prototypes@>+=
void sql_release( MYSQL_RES *res );


@ |sql_free_result()|. A variation of the |sql_release()|.
@<Exported Functions@>+=
int sql_free_result( void *rez ) 
   {
   if( rez ) 
      mysql_free_result( ( MYSQL_RES* )rez );
   rez = NULL ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int sql_free_result( void *rez );

@ |sql_getcontrol()| prototype.
@<Exported Prototypes@>+=
char *sql_getcontrol(const char zone[], const char name[]) ;

@ |sql_getcontrol()|.
@<Exported Functions@>+=
char *sql_getcontrol(const char zone[], const char name[])
   {
   static char value[128+1] ;
   int err ;
   char hostname[ 80 ];

   value[0] = '\0' ;
   gethostname( hostname, 79 );

   err = sql_query("SELECT value FROM controls "
                   "WHERE zone='%s' "
                   "AND name='%s' "
                   "AND `host`='%s'",
                   zone, name, hostname ) ;
   if (!err)
      if (sql_rowcount() == 1)
         if(sql_get(0,0))
            strncpy(value,sql_get(0,0),128) ;  
   value[128] = '\0' ;

   return value ;
   }

@ |sql_getvalue()| prototype.
@<Exported Prototypes@>+=
char *sql_getvalue(char *fmt,...) ;

@ This function returns a single text result to a query, which is 
passed in a printf-like syntax.  The connection is
tested and made if necessary, and if a result is left over from the last 
call it is freed. 
@<Exported Functions@>+=
char *sql_getvalue(char *fmt,...)
   {
   va_list ap ;
   static char query[500] ;
   int err ;
   static char value[256+1] ;

   startTimeCheck();

   va_start(ap,fmt) ;
   vsnprintf(query,500,fmt,ap) ;
   va_end(ap) ;

   if(sql_attach())
      return NULL ;

   if (res) 
      { 
      /* if we have a prior result, free it */
      mysql_free_result(res) ;
      res = 0 ;
      } 
 
   err = mysql_query(conn,query) ;

   doTimeCheck( query );

   if(err)
      {
      Trace("sql error %u: %20s",mysql_errno(conn),mysql_error(conn)) ;
      strcpy( value, "" ) ;
      return NULL ;
      }
  
   if (sql_rowcount() == 1)
      {
      if(sql_get(0,0))
         {
         strncpy(value,sql_get(0,0),256) ;  value[256] = '\0' ;
         return value ;
         }
      }

   strcpy( value, "" ) ;
   return NULL ;
   }


@ |sql_getvaluelist()| prototype.
@<Exported Prototypes@>+=
int sql_getvaluelist(resultNode **result,char *fmt,...) ;

@ This function generates a linked-list of query results and returns 
the number of result records.  A return value of -1 indicates an SQL error.
The query is passed in a printf-like syntax.  The connection is
tested and made if necessary, and if a result is left over from the last 
call it is freed. 
@<Exported Functions@>+=
int sql_getvaluelist(resultNode **result,char *fmt,...)
   {
   va_list ap ;
   static char query[500] ;
   int err ;
   resultNode *node, *lastNode ;
   int i ;

   startTimeCheck();

   va_start(ap,fmt) ;
   vsnprintf(query,500,fmt,ap) ;
   va_end(ap) ;

   if(sql_attach())
      return -1 ;

   if (res) 
      { 
      /* if we have a prior result, free it */
      mysql_free_result(res) ;
      res = 0 ;
      } 
 
   err = mysql_query(conn,query) ;

   doTimeCheck( query );

   if(err)
      {
      Trace("sql error %u: %20s",mysql_errno(conn),mysql_error(conn)) ;
      return -1 ;
      }
   *result = NULL ;
   lastNode = NULL ;
   for (i = 0; i < sql_rowcount(); i++)
      {
      node = malloc(sizeof(resultNode)) ;
      if (*result == NULL)
         *result = node ;
      strncpy(node->value,sql_get(i,0),128) ;  node->value[128] = '\0' ;
      node->next = NULL ;
      if (lastNode)
         lastNode->next = node ;
      lastNode = node ;
      }
   return sql_rowcount() ;
   }



@ The function sets connection timeout (in seconds) to be used in sql_attach()
when making new database connection. The connection attemps is a number of times
the mysql_real_connect() function is beeing used in sql_attach() before
declaring connection error. Each time the mysql_real_connect() is used,
the iConnTimeoutSec will be applied.
@<Exported Functions@>+=
int sql_setConnTimeout( int iConnTimeoutSec, int iConnAttempts ) {
  connTimeoutSec = iConnTimeoutSec;
  connAttempts = iConnAttempts;
}
@ prototype.
@<Exported Prototypes@>+=
int sql_setConnTimeout( int iConnTimeoutSec, int iConnAtempts );



@ This function allows an application to set time value (in microseconds).
Any query taking longer than that time result in an alert trace message.
@<Exported Functions@>+=
int sql_setMaxTime(int t)
   {
   maxTime = t;
   }

@ |sql_setMaxTime()| prototype.
@<Exported Prototypes@>+=
int sql_setMaxTime(int t);


@ This function allows an application to set time value (in microseconds).
Any query taking longer than that time result in an alert trace message.
@<Local Functions@>+=
int startTimeCheck() 
   {
   gettimeofday(&startTime,NULL) ;
   }

@ |sql_setMaxTime()| prototype.
@<Local Prototypes@>+=
int startTimeCheck();


@ This function allows an application to set time value (in microseconds).
Any query taking longer than that time result in an alert trace message.
@<Local Functions@>+=
int doTimeCheck( char *query )
   {
   struct timeval endTime;
   int duration;

   gettimeofday(&endTime,NULL) ;
   duration = (endTime.tv_sec-startTime.tv_sec)*1000000 +
               endTime.tv_usec-startTime.tv_usec;
   if ( duration > maxTime )
      Alert( "query took %d msec: %s", duration/1000, query );
   }

@ |doTimeCheck()| prototype.
@<Local Prototypes@>+=
int doTimeCheck( char *query );

@ This function frees the result list generated by |sql_getvaluelist()|.
@<Exported Functions@>+=
void sql_freevaluelist(resultNode *node)
   {
   resultNode *next ;
   if (node)
      do
         {
         next = node->next ;
         free(node) ;
         node = next ;
         } 
      while (node) ;
   }

@ |sql_freevaluelist()| prototype.
@<Exported Prototypes@>+=
void sql_freevaluelist(resultNode *node) ;


@ Test (sieve).
@(sql_sieve.c@>=
#include <stdio.h>
#include <unistd.h>
#include <time.h>
#include <rds_trn.h>
#include <rds_sql.h>

#define MAX 100

int main( int argc, char **argv ) 
   {
   int i;
   time_t now;

   trn_register( "sql_sieve" );

   now = time( NULL );
   printf( "SQL now is %s\n", sql_datetime( now ) );
   printf( "SQL now is %s\n", sql_timestamp( now ) );

   printf( "drop table\n" );
   sql_query( "drop table if exists primes" );
   printf( "table dropped\n" );

   printf( "create table\n" );
   sql_query( "create table primes ( i  int )" );
   printf( "table created\n" );

   printf( "insert values\n" );
   for( i = 2; i <= MAX * MAX; i++ ) 
      sql_query( "insert into primes values (%d)", i );
   printf( "values inserted\n" );

   printf( "filter values\n" );
   for( i = 2; i <= MAX; i++ ) 
      sql_query( "delete from primes where i %% %d = 0 and i != %d", i, i );
   printf( "filtered\n" );

   sql_query( "select * from primes" );
   printf( "show primes\n" );
   for( i = 0; i < sql_rowcount(); i++ ) 
      {
      if( ( i + 1 ) % 8 ) 
         printf( "%s\t", sql_get( i, 0 ) );
      else 
         printf( "%s\n", sql_get( i, 0 ) );
      }

   sql_query( "drop table primes") ;
   printf( "done\n" );
   }

@*Index.
