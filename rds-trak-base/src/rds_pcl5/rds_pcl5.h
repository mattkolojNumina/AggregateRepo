/*4:*/
#line 159 "rds_pcl5.w"

#ifndef __RDS_PCL5_H 
#define __RDS_PCL5_H 
/*7:*/
#line 178 "rds_pcl5.w"

#define ERRPCL5_NOERRORS    ( 0 ) 
#define ERRPCL5_GENERAL     (-1 ) 



#define PCL5_MAX_OVERHEAD   ( 16 )  

#define PCL5_NOT_DEFINED   ( 99999 )    
#define PCL5_SIMPLEX           ( 0 )    
#define PCL5_DUPLEX_LONGEDGE   ( 1 )    
#define PCL5_DUPLEX_SHORTEDGE  ( 2 )    

#define PCL5_PORTRAIT          ( 0 )    
#define PCL5_LANDSCAPE         ( 1 )    
#define PCL5_REV_PORTRAIT      ( 2 )    
#define PCL5_REV_LANDSCAPE     ( 3 )    



/*:7*/
#line 162 "rds_pcl5.w"

/*11:*/
#line 467 "rds_pcl5.w"

int pcl5_info(const char*pData,int iDataLen,int*piPages,int*piCopies,
int*piDuplexStatus,int*piPortraitStatus,int*piInputBin,
int*piOutputBin,int*piPaperSize);


/*:11*//*14:*/
#line 590 "rds_pcl5.w"

const char*pcl5_duplex2str(int iValue);



/*:14*//*16:*/
#line 612 "rds_pcl5.w"

const char*pcl5_portrait2str(int iValue);



/*:16*//*18:*/
#line 645 "rds_pcl5.w"

const char*pcl5_inputbin2str(int iValue);



/*:18*//*20:*/
#line 671 "rds_pcl5.w"

const char*pcl5_outputbin2str(int iValue);



/*:20*//*22:*/
#line 709 "rds_pcl5.w"

const char*pcl5_papersize2str(int iValue);



/*:22*//*24:*/
#line 972 "rds_pcl5.w"

int pcl5_setparam(char*pData,int iDataLen,char chCmd1,char chCmd2,
char chCmdSuffix,int iParamValue);



/*:24*//*26:*/
#line 998 "rds_pcl5.w"

int pcl5_setduplex(char*pData,int iDataLen,int iValue);



/*:26*//*28:*/
#line 1026 "rds_pcl5.w"

int pcl5_setportrait(char*pData,int iDataLen,int iValue);




/*:28*//*30:*/
#line 1053 "rds_pcl5.w"

int pcl5_setinputbin(char*pData,int iDataLen,int iValue);



/*:30*//*32:*/
#line 1081 "rds_pcl5.w"

int pcl5_setoutputbin(char*pData,int iDataLen,int iValue);



/*:32*//*34:*/
#line 1114 "rds_pcl5.w"

int pcl5_setpapersize(char*pData,int iDataLen,int iValue);



/*:34*//*37:*/
#line 1392 "rds_pcl5.w"

int pcl5_stripkyo(char*pData,int iDataLen,int*piTotalKyo);


/*:37*//*40:*/
#line 1551 "rds_pcl5.w"

int pcl5_strippjl(char*pData,int iDataLen,int*piTotalPjl);


/*:40*//*43:*/
#line 1741 "rds_pcl5.w"

int pcl5_insert(char*pData,int iDataLen,int iPage,const char*pBlock,
int iBlockLen);



/*:43*/
#line 163 "rds_pcl5.w"

#endif 
#line 165 "rds_pcl5.w"



/*:4*/
