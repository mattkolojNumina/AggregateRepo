/*5:*/
#line 119 "rds_ctr.w"

#ifndef __RDS_CTR_H
#define __RDS_CTR_H
/*6:*/
#line 128 "rds_ctr.w"

#define CTR_MSG (21)
#define CTR_MSG_LEN  128

/*:6*/
#line 122 "rds_ctr.w"

/*8:*/
#line 141 "rds_ctr.w"

int ctr_bump(const char*zone,const char*code);

/*:8*//*11:*/
#line 189 "rds_ctr.w"

int ctr_incr(const char*fmt,...);

/*:11*/
#line 123 "rds_ctr.w"

#endif
#line 125 "rds_ctr.w"


/*:5*/
