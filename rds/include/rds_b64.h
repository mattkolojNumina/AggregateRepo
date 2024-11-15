/*2:*/
#line 97 "./rds_b64.w"

#ifndef __RDS_B64_H
#define __RDS_B64_H
/*3:*/
#line 109 "./rds_b64.w"

#ifndef EOS
#define EOS '\0'
#endif



/*:3*/
#line 100 "./rds_b64.w"

int b64_encode(const char*pBin,char*pszMime,int iBinLen,int bPad);
int b64_decode(const char*pszMime,char*pBin);
int b64_decode_len(const char*pszMime);
#endif



/*:2*/
