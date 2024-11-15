/*3:*/
#line 108 "rds_logix.w"

#ifndef __LOGIX_H
#define __LOGIX_H
/*18:*/
#line 295 "rds_logix.w"

typedef struct
{
int socket;
unsigned long int session_id;
int transaction;
char address[25];
int enabled;
}plc_record;
typedef plc_record*plc_handle;
typedef enum{LOGIX_INT,LOGIX_DINT}data_type;

#define LOGIX_ERR          (-1)
#define LOGIX_ERR_SOCK     (-2)
#define LOGIX_ERR_READ     (-3)
#define LOGIX_ERR_CONN     (-4)
#define LOGIX_ERR_TIMEOUT  (-5)



/*:18*/
#line 111 "rds_logix.w"

/*5:*/
#line 141 "rds_logix.w"

plc_handle logix_open(char*address);



/*:5*//*7:*/
#line 157 "rds_logix.w"

int logix_close(plc_handle plc);



/*:7*//*9:*/
#line 184 "rds_logix.w"

int logix_read(plc_handle plc,char*src,int start,
int n_words,data_type the_type,unsigned long*data);



/*:9*//*11:*/
#line 211 "rds_logix.w"

int logix_write(plc_handle plc,char*dest,int start,int n_words,
data_type the_type,unsigned long int*data);



/*:11*//*13:*/
#line 233 "rds_logix.w"

int logix_processread(plc_handle plc,int*n_words,unsigned long int*dest);


/*:13*//*15:*/
#line 246 "rds_logix.w"

int logix_processwrite(plc_handle plc);



/*:15*//*26:*/
#line 576 "rds_logix.w"

int process_write(plc_handle plc);


/*:26*//*28:*/
#line 658 "rds_logix.w"

int process_read(plc_handle plc,unsigned long int*data);



/*:28*/
#line 112 "rds_logix.w"

#endif
#line 114 "rds_logix.w"



/*:3*/
