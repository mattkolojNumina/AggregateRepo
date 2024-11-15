/*5:*/
#line 187 "./rds_par.w"

#ifndef __RDS_PAR_H
#define __RDS_PAR_H
/*6:*/
#line 197 "./rds_par.w"

#define ERRPAR_GENERAL     (-1)
#define ERRPAR_ARG         (-2)
#define ERRPAR_PORTNUM     (-3)
#define ERRPAR_OPEN        (-4)
#define ERRPAR_CLAIM       (-5)
#define ERRPAR_RELEASE     (-6)
#define ERRPAR_SETDATA     (-7)
#define ERRPAR_GETDATA     (-8)



/*:6*/
#line 190 "./rds_par.w"

/*8:*/
#line 237 "./rds_par.w"

int par_open(int iPortNum);



/*:8*//*10:*/
#line 253 "./rds_par.w"

int par_close(int hPort);



/*:10*//*12:*/
#line 283 "./rds_par.w"

int par_getbyte(int hPort,int*piByteValue);


/*:12*//*15:*/
#line 355 "./rds_par.w"

int par_setbyte(int hPort,int iByteValue);


/*:15*//*18:*/
#line 440 "./rds_par.w"

int par_setbit(int hPort,int iBit,int iBitValue);



/*:18*//*21:*/
#line 517 "./rds_par.w"

int par_getcbyte(int hPort,int*piByteValue);


/*:21*//*24:*/
#line 600 "./rds_par.w"

int par_setcbit(int hPort,int iBit,int iBitValue);


/*:24*//*27:*/
#line 676 "./rds_par.w"

int par_getsbyte(int hPort,int*piByteValue);


/*:27*//*30:*/
#line 743 "./rds_par.w"

int par_setstrob(int hPort,int iBitValue);


/*:30*//*33:*/
#line 831 "./rds_par.w"

char*par_errstr(int iErr);


/*:33*/
#line 191 "./rds_par.w"

#endif



/*:5*/
