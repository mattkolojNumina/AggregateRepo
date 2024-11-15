/*4:*/
#line 121 "./rds_tq.w"

#ifndef __RDS_TQ_H
#define __RDS_TQ_H
/*5:*/
#line 131 "./rds_tq.w"

#define ERRTQ_GENERAL    (-1)
#define ERRTQ_TOTAL      (-2)
#define ERRTQ_QNAME      (-3)
#define ERRTQ_MSGTOOLONG (-4)



/*:5*/
#line 124 "./rds_tq.w"

/*14:*/
#line 237 "./rds_tq.w"

int tq_init(const char*pszQName,int iTotalMessages);


/*:14*//*17:*/
#line 308 "./rds_tq.w"

int tq_send(const char*pszQName,const char*pszMsg);


/*:17*//*20:*/
#line 399 "./rds_tq.w"

int tq_recv(const char*pszQName,char*pszBuf,int iBufSize);


/*:20*//*23:*/
#line 468 "./rds_tq.w"

int tq_empty(const char*pszQName);


/*:23*//*26:*/
#line 526 "./rds_tq.w"

char*tq_errstr(int iErr);


/*:26*/
#line 125 "./rds_tq.w"

#endif



/*:4*/
