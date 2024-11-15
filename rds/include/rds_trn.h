/*4:*/
#line 137 "rds_trn.w"

#ifndef __TRN_H 
#define __TRN_H 
/*6:*/
#line 167 "rds_trn.w"

#define TRN_NAME_LEN 15        
#define TRN_MSG_LEN 256        
#define TRN_MSG_N ( 1 << 13 )  

enum{trn_alert= 0,trn_trace= 1,trn_inform= 2};

#include <sys/types.h>    
typedef struct{
char name[TRN_NAME_LEN+1];
pid_t pid;
time_t stamp;
char level;
char msg[TRN_MSG_LEN+1];
}trn_msg;



/*:6*/
#line 140 "rds_trn.w"

/*11:*/
#line 247 "rds_trn.w"

void trn_register(char*name);



/*:11*//*13:*/
#line 276 "rds_trn.w"

void trn_post(int level,char*msg);



/*:13*//*15:*/
#line 304 "rds_trn.w"

int trn_remote_post(int level,char*msg,char*name);



/*:15*//*17:*/
#line 334 "rds_trn.w"

int Alert(char*fmt,...);


/*:17*//*19:*/
#line 354 "rds_trn.w"

int Trace(char*fmt,...);



/*:19*//*21:*/
#line 375 "rds_trn.w"

int Inform(char*fmt,...);



/*:21*//*23:*/
#line 426 "rds_trn.w"

trn_msg*trn_fetch(int*count);



/*:23*/
#line 141 "rds_trn.w"

#endif 
#line 143 "rds_trn.w"



/*:4*/
