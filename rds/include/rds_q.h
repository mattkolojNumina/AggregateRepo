/*4:*/
#line 155 "./rds_q.w"

#ifndef __RDS_Q_H
#define __RDS_Q_H
/*35:*/
#line 601 "./rds_q.w"

int q_fastsend(int iMsgType,const char*pszMsg);


/*:35*//*38:*/
#line 679 "./rds_q.w"

int q_fastsendremote(const char*pszHost,int iMsgType,const char*pszMsg);



/*:38*//*40:*/
#line 747 "./rds_q.w"

int q_sync(void);


/*:40*//*44:*/
#line 887 "./rds_q.w"

int q_send(int iMsgType,const char*pszMsg);


/*:44*//*47:*/
#line 967 "./rds_q.w"

int q_sendremote(const char*pszHost,int iMsgType,const char*pszMsg);


/*:47*//*49:*/
#line 1009 "./rds_q.w"

int q_rawtype(int iReader);


/*:49*//*52:*/
#line 1081 "./rds_q.w"

int q_type(int iReader);


/*:52*//*55:*/
#line 1203 "./rds_q.w"

char*q_recv(int iReader);


/*:55*//*58:*/
#line 1331 "./rds_q.w"

int q_nextrawtype(int handle);



/*:58*//*60:*/
#line 1348 "./rds_q.w"

int q_nexttype(int iReader);


/*:60*//*63:*/
#line 1419 "./rds_q.w"

int q_gethead(void);



/*:63*//*65:*/
#line 1441 "./rds_q.w"

int q_gettail(int iReader);



/*:65*//*67:*/
#line 1474 "./rds_q.w"

int q_settail(int iReader,int iTail);


/*:67*//*70:*/
#line 1564 "./rds_q.w"

int q_empty(void);


/*:70*//*73:*/
#line 1681 "./rds_q.w"

int q_setreadername(int iReader,const char*pszName);


/*:73*//*76:*/
#line 1759 "./rds_q.w"

char*q_getreadername(int iReader);



/*:76*//*78:*/
#line 1789 "./rds_q.w"

int q_getreaderbyname(const char*pszName);



/*:78*//*80:*/
#line 1821 "./rds_q.w"

int q_settypename(int iMsgType,const char*pszName);


/*:80*//*83:*/
#line 1898 "./rds_q.w"

char*q_gettypename(int iMsgType);


/*:83*//*85:*/
#line 1929 "./rds_q.w"

int q_gettypebyname(const char*pszName);



/*:85*//*87:*/
#line 1964 "./rds_q.w"

int q_getconfig(int*piSegmentDataSize,int*piSegmentsTotal);


/*:87*//*90:*/
#line 2080 "./rds_q.w"

int q_setconfig(int iSegmentDataSize,int iSegmentsTotal);


/*:90*//*93:*/
#line 2212 "./rds_q.w"

char*q_errstr(int iErr);


/*:93*/
#line 158 "./rds_q.w"

/*6:*/
#line 169 "./rds_q.w"

#define ERRQ_GENERAL     (-1)
#define ERRQ_INIT        (-2)
#define ERRQ_READER      (-3)
#define ERRQ_MSGTYPE     (-4)
#define ERRQ_TAIL        (-5)
#define ERRQ_MSGTOOLONG  (-6)
#define ERRQ_HOST        (-7)
#define ERRQ_FILE        (-8)  
#define ERRQ_ALLOC       (-9)  
#define ERRQ_SEGDATASIZE (-10) 
#define ERRQ_SEGTOTAL    (-11) 
#define ERRQ_READERNAME  (-12) 
#define ERRQ_MSGTYPENAME (-13) 


/*:6*/
#line 159 "./rds_q.w"

#endif



/*:4*/
