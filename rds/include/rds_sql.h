/*3:*/
#line 125 "rds_sql.w"

#ifndef __SQL_H
#define __SQL_H
#include <mysql/mysql.h> 

typedef struct resNode
{
char value[128];
struct resNode*next;
}resultNode;

/*11:*/
#line 259 "rds_sql.w"

void sql_setconnection(const char*set_machine,
const char*set_user,
const char*set_password,
const char*set_database);


/*:11*//*13:*/
#line 284 "rds_sql.w"

void sql_close(void);


/*:13*//*15:*/
#line 325 "rds_sql.w"

int sql_query(char*fmt,...);



/*:15*//*17:*/
#line 339 "rds_sql.w"

int sql_affected_rows(void);



/*:17*//*19:*/
#line 359 "rds_sql.w"

char*sql_get(int iRow,int iColumn);


/*:19*//*21:*/
#line 388 "rds_sql.w"

char*sql_getlen(int iRow,int iColumn,int*piLen);


/*:21*//*23:*/
#line 405 "rds_sql.w"

char*sql_rget(MYSQL_RES*rez,int iRow,int iColumn);


/*:23*//*25:*/
#line 422 "rds_sql.w"

char*sql_get_r(void*rez,int r,int c);


/*:25*//*27:*/
#line 440 "rds_sql.w"

int sql_rowfetch(void);


/*:27*//*29:*/
#line 456 "rds_sql.w"

char*sql_rowget(int iColumn);


/*:29*//*31:*/
#line 479 "rds_sql.w"

int sql_getcolumn(const char*pszFieldName);

/*:31*//*33:*/
#line 495 "rds_sql.w"

char*sql_getbyname(int iRow,const char*pszFieldName);


/*:33*//*35:*/
#line 512 "rds_sql.w"

int sql_rowcount(void);


/*:35*//*37:*/
#line 527 "rds_sql.w"

int sql_rrowcount(MYSQL_RES*rez);


/*:37*//*39:*/
#line 542 "rds_sql.w"

int sql_rowcount_r(void*rez);


/*:39*//*41:*/
#line 557 "rds_sql.w"

unsigned long long sql_sequence(void);


/*:41*//*43:*/
#line 578 "rds_sql.w"

char*sql_datetime(int when);


/*:43*//*45:*/
#line 599 "rds_sql.w"

char*sql_timestamp(int when);

/*:45*//*47:*/
#line 647 "rds_sql.w"

int sql_escape(const char*pBin,int iBinLen,char*pszEsc);


/*:47*//*49:*/
#line 666 "rds_sql.w"

MYSQL_RES*sql_result(void);


/*:49*//*51:*/
#line 678 "rds_sql.w"

void*sql_get_result(void);


/*:51*//*53:*/
#line 692 "rds_sql.w"

void sql_release(MYSQL_RES*res);


/*:53*//*55:*/
#line 706 "rds_sql.w"

int sql_free_result(void*rez);

/*:55*//*56:*/
#line 710 "rds_sql.w"

char*sql_getcontrol(const char zone[],const char name[]);

/*:56*//*58:*/
#line 739 "rds_sql.w"

char*sql_getvalue(char*fmt,...);

/*:58*//*60:*/
#line 796 "rds_sql.w"

int sql_getvaluelist(resultNode**result,char*fmt,...);

/*:60*//*63:*/
#line 867 "rds_sql.w"

int sql_setConnTimeout(int iConnTimeoutSec,int iConnAtempts);



/*:63*//*65:*/
#line 881 "rds_sql.w"

int sql_setMaxTime(int t);


/*:65*//*71:*/
#line 933 "rds_sql.w"

void sql_freevaluelist(resultNode*node);


/*:71*/
#line 136 "rds_sql.w"

#endif
#line 138 "rds_sql.w"


/*:3*/
