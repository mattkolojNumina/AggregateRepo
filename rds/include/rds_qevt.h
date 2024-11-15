/*5:*/
#line 127 "./rds_qevt.w"

#ifndef __RDS_QEVT_H
#define __RDS_QEVT_H
/*6:*/
#line 137 "./rds_qevt.w"

#define QEVT_MSGTYPE ( 20 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



/*:6*/
#line 130 "./rds_qevt.w"

/*8:*/
#line 174 "./rds_qevt.w"

int qevt_instant(const char*pszCode);


/*:8*//*11:*/
#line 221 "./rds_qevt.w"

int qevt_start(const char*pszCode);


/*:11*//*14:*/
#line 267 "./rds_qevt.w"

int qevt_stop(const char*pszCode);


/*:14*/
#line 131 "./rds_qevt.w"

#endif



/*:5*/
