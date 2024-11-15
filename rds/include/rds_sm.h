/*5:*/
#line 126 "./rds_sm.w"

#ifndef __RDS_SM_H
#define __RDS_SM_H
/*6:*/
#line 136 "./rds_sm.w"

#define ERRSM_GENERAL     (-1)
#define ERRSM_INIT        (-2)
#define ERRSM_ID          (-3)
#define ERRSM_DATASIZE    (-4)  
#define ERRSM_SEM         (-5)  



/*:6*/
#line 129 "./rds_sm.w"

/*18:*/
#line 264 "./rds_sm.w"

int sm_lock(int id);



/*:18*//*20:*/
#line 278 "./rds_sm.w"

int sm_unlock(int id);



/*:20*//*22:*/
#line 312 "./rds_sm.w"

void*sm_open(int id,int iDataSize,int*piErrCode);



/*:22*//*24:*/
#line 336 "./rds_sm.w"

int sm_close(int id,void*ptr);



/*:24*//*26:*/
#line 370 "./rds_sm.w"

char*sm_errstr(int iErr);


/*:26*/
#line 130 "./rds_sm.w"

#endif



/*:5*/
