/*4:*/
#line 124 "./rds_qhist.w"

#ifndef __RDS_QHIST_H
#define __RDS_QHIST_H
/*5:*/
#line 133 "./rds_qhist.w"

#define QHIST_MSGTYPE ( 23 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



/*:5*/
#line 127 "./rds_qhist.w"

/*7:*/
#line 181 "./rds_qhist.w"

int qhist_post(const char*pszCarton,const char*pszCode,
const char*pszDesc);



/*:7*/
#line 128 "./rds_qhist.w"

#endif


/*:4*/
