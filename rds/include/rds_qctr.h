/*4:*/
#line 122 "./rds_qctr.w"

#ifndef __RDS_QCTR_H
#define __RDS_QCTR_H
/*5:*/
#line 131 "./rds_qctr.w"

#define QCTR_MSGTYPE ( 21 )

#ifndef FALSE
#define FALSE ( 0 )
#endif

#ifndef TRUE
#define TRUE ( !FALSE )
#endif



/*:5*/
#line 125 "./rds_qctr.w"

/*7:*/
#line 169 "./rds_qctr.w"

int qctr_bump(const char*pszZone,const char*pszCode);



/*:7*//*10:*/
#line 220 "./rds_qctr.w"

int qctr_zero(const char*pszZone,const char*pszCode);


/*:10*/
#line 126 "./rds_qctr.w"

#endif


/*:4*/
