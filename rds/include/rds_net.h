/*4:*/
#line 128 "rds_net.w"

#ifndef __RDS_NET_H
#define __RDS_NET_H
#include <sys/types.h> 
#include <sys/socket.h> 
/*6:*/
#line 159 "rds_net.w"

int net_open(char*host,int port);



/*:6*//*9:*/
#line 210 "rds_net.w"

int net_serve(int port);



/*:9*/
#line 133 "rds_net.w"

#endif
#line 135 "rds_net.w"



/*:4*/
