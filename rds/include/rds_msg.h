/*5:*/
#line 145 "rds_msg.w"

#ifndef __RDS_MSG_H
#define __RDS_MSG_H
/*44:*/
#line 510 "rds_msg.w"

int msg_send(int mtype,char*msg);

/*:44*//*51:*/
#line 594 "rds_msg.w"

int msg_type(int handle);


/*:51*//*60:*/
#line 709 "rds_msg.w"

char*msg_recv(int handle);


/*:60*//*69:*/
#line 823 "rds_msg.w"

int msg_next(int handle);

/*:69*//*74:*/
#line 881 "rds_msg.w"

int msg_get(int handle);

/*:74*//*80:*/
#line 943 "rds_msg.w"

int msg_set(int handle,long int value);

/*:80*//*86:*/
#line 1036 "rds_msg.w"

int msg_index(char*name);

/*:86*/
#line 148 "rds_msg.w"

#endif
#line 150 "rds_msg.w"

/*:5*/
