/*1:*/
#line 76 "app_lib.w"

static char rcsid[]= "$Id: app_lib.w,v 1.37 2019/03/15 19:36:54 rds Exp $";
/*4:*/
#line 104 "app_lib.w"

#include <stdio.h> 
#include <stdlib.h> 
#include <stdarg.h> 
#include <string.h> 
#include <unistd.h> 

#include <rds_hist.h> 
#include <rds_sql.h> 
#include <rds_trn.h> 
#include <rds_util.h> 

#include "app.h"


/*:4*/
#line 78 "app_lib.w"

/*5:*/
#line 120 "app_lib.w"

#define BUF_LEN   32  
#define MSG_LEN  256  


/*:5*/
#line 79 "app_lib.w"

/*32:*/
#line 578 "app_lib.w"

int zpl_remove_safe(char*pszZpl,int len,const char*pszZplCmd);



/*:32*//*36:*/
#line 621 "app_lib.w"

void placeholder(void);

/*:36*/
#line 80 "app_lib.w"

/*7:*/
#line 128 "app_lib.w"

char*app_get_carton_val(const char carton_id[],const char name[]){
return sql_getvalue(
"SELECT `%s` FROM cartons "
"WHERE cartonId = '%s'",
name,carton_id);
}
/*:7*//*9:*/
#line 141 "app_lib.w"

void app_update_description(const char area[],int seq,
const char hist_code[],const char*fmt,...){
char*val;
va_list ap;
char description[MSG_LEN+1];
char id[BUF_LEN+1];

va_start(ap,fmt);
vsnprintf(description,MSG_LEN,fmt,ap);
description[MSG_LEN]= '\0';
va_end(ap);







val= util_carton_get(area,seq,"cartonId");
if(val==NULL||strlen(val)==0||atoi(val)==0)
sprintf(id,"seq%d",seq);
else strcpy(id,val);

util_update_description(area,seq,id,hist_code,description);
}
/*:9*//*11:*/
#line 174 "app_lib.w"

void app_update_hist(const char area[],int seq,int carton_seq){
if(area==NULL||strlen(area)==0||seq<=0||
carton_seq<=0)
return;

sql_query(
"UPDATE cartonLog SET "
"id = '%d', "
"stamp = stamp "
"WHERE id = 'seq%d'",
carton_seq,seq);

}
/*:11*//*13:*/
#line 194 "app_lib.w"

char*app_get_hint(int box){
int seq= -1;
static char hint[HINT_LEN+1];
char*val,area[BUF_LEN+1];

if(box<MIN_TRAK_BOX||box> MAX_TRAK_BOX)strcpy(hint,"");
else if((seq= util_box_get_int(box,"seq"))<=0)
snprintf(hint,HINT_LEN,"<%d>",box);
else{
val= util_box_get(box,"area");
if(val!=NULL&&strlen(val)> 0){
strncpy(area,val,sizeof(area)-1);
area[sizeof(area)-1]= '\0';

val= util_carton_get(area,seq,"barcode");
if(val!=NULL&&strlen(val)> 0)
snprintf(hint,HINT_LEN,"[%s]",val);
else snprintf(hint,HINT_LEN,"carton %d",seq);
}
else snprintf(hint,HINT_LEN,"carton %d",seq);
}
hint[HINT_LEN]= '\0';
return(hint);
}
/*:13*//*15:*/
#line 225 "app_lib.w"

int app_parse_scan(const char scan_msg[],char barcode[]){
int num_valid;
char*str,*str_ptr;

num_valid= 0;
strcpy(barcode,"");

str= strdup(scan_msg);
str_ptr= str;
while(str_ptr!=NULL){
char*candidate= strsep(&str_ptr,",");






if((strlen(candidate)==LPN_LEN1&&atoi(candidate+1)> 0)||
(strlen(candidate)==TOTE_LPN_LEN&&
strstr(candidate,"T")==candidate)||
(strlen(candidate)==LPN_LEN2&&
strstr(candidate+LPN_LEN2-4,"TEST")!=NULL)||
strcasecmp(candidate,"RECYCLE")==0){
Inform("     valid barcode [%s]",candidate);
strcpy(barcode,candidate);
num_valid++;
}else{
Inform("     ignore barcode [%s]",candidate);
}
}
free(str);

return num_valid;
}
/*:15*//*17:*/
#line 266 "app_lib.w"

void app_generate_simple_label(char label[],const char barcode[],
const char msg[]){
char tmp[2*MSG_LEN+1];

sprintf(label,
"^XA\n"
"^LH0,0^FS\n");

if(strlen(barcode)> 0){
sprintf(tmp,
"^FO200,400^BY3,,102\n"
"^BCN^FD%s^FS\n",
barcode);
strcat(label,tmp);
}

if(strlen(msg)> 0){
sprintf(tmp,
"^FO100,600^A0N,30,30^FB600,8^FD%s^FS\n",
msg);
strcat(label,tmp);
}

strcat(label,"^XZ\n");
}
/*:17*//*19:*/
#line 301 "app_lib.w"

int app_label_ready_(int seq,const char printer[]){
char*val;
char status_name[BUF_LEN+1];
char barcode[BUF_LEN+1];
int ordinal;

strcpy(barcode,"");

















val= util_carton_get("xpal",seq,"barcode");
if(val!=NULL&&strlen(val)> 0){
strncpy(barcode,val,BUF_LEN);
barcode[BUF_LEN]= '\0';
}
Inform("app_label_ready() barcode [%s]",barcode);

val= sql_getvalue(
"SELECT ordinal FROM labels "
"WHERE barcode = '%s' "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
barcode,printer);
if(val==NULL||(ordinal= atoi(val))<=0)
return-1;

Inform("app_label_ready() ordinal [%d]",ordinal);
return ordinal;
}
/*:19*//*21:*/
#line 354 "app_lib.w"

int app_label_ready(int seq,const char printer[]){
char*val;
char carton_id[BUF_LEN+1];
int ordinal;

Inform("  checking if label is ready...");
val= sql_getvalue(
"SELECT status FROM cartonStatus "
"WHERE seq = %d "
"AND name = 'label'",
seq);
if(val==NULL||strcmp(val,"failed")==0)
return-1;

if(strcmp(val,"complete")!=0)
return 0;

sprintf(carton_id,"%d",seq);
val= sql_getvalue(
"SELECT ordinal FROM labels "
"WHERE cartonId = '%s' "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
carton_id,printer);
if(val==NULL||(ordinal= atoi(val))<=0)
return-1;

return ordinal;
}
/*:21*//*23:*/
#line 392 "app_lib.w"

int app_get_label_(int seq,const char printer[],char label[],int*len){
int err,ordinal,llen;
char barcode[BUF_LEN+1];
char*val;

strcpy(label,"");
strcpy(barcode,"");

val= util_carton_get("xpal",seq,"barcode");
if(val!=NULL&&strlen(val)> 0){
strncpy(barcode,val,BUF_LEN);
barcode[BUF_LEN]= '\0';
}

err= sql_query(
"SELECT ordinal, zpl FROM labels "
"WHERE barcode = '%s' "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
barcode,printer);
if(err){
Alert("sql error retrieving label data, err = %d",err);
return 0;
}
if(sql_rowcount()!=1)
return 0;

ordinal= atoi(sql_get(0,0));
val= sql_getlen(0,1,&llen);
if(llen> *len){
*len= 0;
return 0;
}
memcpy(label,val,llen);
*len= llen;

return ordinal;
}
/*:23*//*25:*/
#line 438 "app_lib.w"

int app_get_label(int seq,const char printer[],char label[],int*len){
int err,ordinal,llen;
char*val;
char carton_id[BUF_LEN+1];
int i;

Inform("  getting label...");
strcpy(label,"");

sprintf(carton_id,"%d",seq);
err= sql_query(
"SELECT ordinal, zpl FROM labels "
"WHERE cartonId = '%s' "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
carton_id,printer);

if(err){
Alert("sql error retrieving label data, err = %d",err);
return 0;
}

if(sql_rowcount()!=1)
return 0;

ordinal= atoi(sql_get(0,0));
val= sql_getlen(0,1,&llen);
if(llen> *len){
*len= 0;
return 0;
}
bcopy(val,label,llen);


llen= zpl_remove_safe(label,llen,"^MCN");
llen= zpl_remove_safe(label,llen,"^MD");
llen= zpl_remove_safe(label,llen,"^MM");
llen= zpl_remove_safe(label,llen,"^MN");
llen= zpl_remove_safe(label,llen,"^PM");
llen= zpl_remove_safe(label,llen,"^PO");
llen= zpl_remove_safe(label,llen,"^PQ");
llen= zpl_remove_safe(label,llen,"^PR");
llen= zpl_remove_safe(label,llen,"^PW");
llen= zpl_remove_safe(label,llen,"~SD");

*len= llen;

if(*len<40)
Inform("label data [%s]",label);

return ordinal;
}
/*:25*//*27:*/
#line 499 "app_lib.w"

void app_label_printed_(int seq,const char printer[],int ordinal){
char barcode[BUF_LEN+1];
char*val;

strcpy(barcode,"");

val= util_carton_get("xpal",seq,"barcode");
if(val!=NULL&&strlen(val)> 0){
strncpy(barcode,val,BUF_LEN);
barcode[BUF_LEN]= '\0';
}

sql_query(
"UPDATE labels SET "
"printed = 'yes' "
"WHERE barcode = '%s' "
"AND printer = '%s' "
"AND ordinal = %d",
barcode,printer,ordinal);

app_update_description("xpal",seq,"xpal",
"printing complete at %s",printer);
}
/*:27*//*29:*/
#line 530 "app_lib.w"

void app_label_printed(int seq,const char printer[],int ordinal){
char*val,carton_id[BUF_LEN+1];

Inform("  marking label page %d as printed...",ordinal);

sprintf(carton_id,"%d",seq);
sql_query(
"UPDATE labels SET "
"printed = 'yes' "
"WHERE cartonId = '%s' "
"AND printer = '%s' "
"AND ordinal = %d",
carton_id,printer,ordinal);

app_update_description("xpal",seq,printer,
"label %d printed at %s",ordinal,printer);

val= sql_getvalue("SELECT COUNT(*) FROM labels WHERE cartonId = '%s' AND "
"printer = '%s' AND printed = 'no'",carton_id,printer);
if(val!=NULL&&atoi(val)==0)
util_carton_set("xpal",seq,"printed","1");
}
/*:29*//*33:*/
#line 584 "app_lib.w"

int app_get_lane(const char area[],const char logical[],
char description[]){
int lane= -1;
int err;

err= sql_query(
"SELECT physical, description FROM cfgLanes "
"WHERE area = '%s' "
"AND logical = '%s'",
area,logical);
if(err){
strcpy(description,"unknown");
Alert("sql error (%d) determining lane for [%s/%s]",
err,area,logical);
return 0;
}

lane= atoi(sql_get(0,0));
strncpy(description,sql_get(0,1),MSG_LEN);
description[MSG_LEN]= '\0';
Trace("     sort lookup: %s/%s -> %s (%d)",
area,logical,description,lane);

return lane;
}
/*:33*/
#line 81 "app_lib.w"

/*31:*/
#line 561 "app_lib.w"

int zpl_remove_safe(char*pszZpl,int len,const char*pszZplCmd){
char*pc;
int i;
int cmd_len= strlen(pszZplCmd);
for(pc= pszZpl,i= 0;i<len;i++,pc++){
if(!strncmp(pc,pszZplCmd,cmd_len)){
int j;
for(j= 0;j<cmd_len;j++)*pc++= 0x20;
while(*pc!='^')*pc++= 0x20;
return len;
}
}

return len;
}
/*:31*//*35:*/
#line 617 "app_lib.w"

void placeholder(void){
}
/*:35*/
#line 82 "app_lib.w"



/*:1*/
