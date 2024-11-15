/*1:*/
#line 77 "app_lib.w"

static char rcsid[]= "$Id: app_lib.w,v 1.26 2024/01/24 20:35:25 rds Exp rds $";
/*4:*/
#line 102 "app_lib.w"

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
#line 79 "app_lib.w"

/*5:*/
#line 118 "app_lib.w"

#define BUF_LEN   32  
#define LBUF_LEN  64  
#define MSG_LEN  256  


/*:5*//*29:*/
#line 424 "app_lib.w"

#define MAX_TRIES (100)

/*:29*/
#line 80 "app_lib.w"

/*35:*/
#line 558 "app_lib.w"

int is_valid(char*candidate);

/*:35*//*49:*/
#line 810 "app_lib.w"

int zpl_remove_safe(char*pszZpl,int len,const char*pszZplCmd);

/*:49*//*69:*/
#line 1003 "app_lib.w"

void proto(void);

/*:69*/
#line 81 "app_lib.w"

/*7:*/
#line 127 "app_lib.w"

int
app_lookup_cartonSeq(char lpn[])
{
int cartonSeq= -1;
int err;

err= sql_query("SELECT cartonSeq "
"FROM rdsCartons "
"WHERE lpn='%s' "
"ORDER BY cartonSeq DESC ",
lpn);
if(!err)
{
if(sql_rowcount()> 0)
if(sql_get(0,0))
cartonSeq= atoi(sql_get(0,0));
}
else
Alert("SQL error %d select rdsCartons",err);

return cartonSeq;
}

/*:7*//*9:*/
#line 156 "app_lib.w"

char*
app_get_carton_val_lpn(const char carton_lpn[],const char column_name[])
{

char*val
= sql_getvalue(" SELECT `%s` FROM rdsCartons "
" WHERE lpn = '%s'",
column_name,carton_lpn);

if(val==NULL||strlen(val)==0)
return"";

return val;
}

/*:9*//*11:*/
#line 177 "app_lib.w"

char*
app_get_carton_val(const int carton_seq,const char column_name[])
{
char*val
= sql_getvalue(" SELECT `%s` FROM rdsCartons "
" WHERE cartonSeq = %d",
column_name,carton_seq);

if(val==NULL||strlen(val)==0)
return"";

return val;
}

/*:11*//*13:*/
#line 197 "app_lib.w"

char*
app_get_carton_data_val(const int carton_seq,const char data_type[])
{
char*val
= sql_getvalue(" SELECT dataValue FROM rdsCartonData "
" WHERE cartonSeq = %d "
" AND dataType = '%s'",
carton_seq,data_type);

if(val==NULL||strlen(val)==0)
return"";
return val;
}

/*:13*//*15:*/
#line 217 "app_lib.w"

int
app_set_carton_val_lpn(const char carton_lpn[],
const char column_name[],const char value[])
{
int err;

err= sql_query("UPDATE rdsCartons SET `%s`='%s' "
"WHERE lpn = '%s'",
column_name,value,carton_lpn);

return err;
}

/*:15*//*17:*/
#line 237 "app_lib.w"

int
app_set_carton_val(const int carton_seq,
const char column_name[],const char value[])
{
int err;

err= sql_query("UPDATE rdsCartons SET `%s`='%s' "
"WHERE cartonSeq = %d",
column_name,value,carton_seq);

return err;
}

/*:17*//*19:*/
#line 257 "app_lib.w"

char*
app_get_order_val(const char order_id[],const char column_name[])
{
char*val
= sql_getvalue("SELECT `%s` FROM custOrders "
"WHERE orderId = '%s' "
"AND `status`<>'canceled'; ",
column_name,order_id);

if(val==NULL||strlen(val)==0)
return val;
}

/*:19*//*21:*/
#line 276 "app_lib.w"

char*
app_get_rdsDocuments_val(const char ref_value[],
const char column_name[],const char doc_type[])
{
char*val
= sql_getvalue("SELECT `%s` FROM rdsDocuments "
"WHERE docType = '%s' AND refValue = '%s' ",
column_name,doc_type,ref_value);

if(val==NULL||strlen(val)==0)
return"";

return val;
}

/*:21*//*23:*/
#line 298 "app_lib.w"

char*
app_get_label_val(const char seq[],const char column_name[])
{
char*val
= sql_getvalue("SELECT `%s` FROM labels "
"WHERE seq = %s; ",
seq,column_name);

if(val==NULL||strlen(val)==0)
return"";

return val;
}

/*:23*//*25:*/
#line 318 "app_lib.w"

char*
app_get_physical_lane(const char ship_method[])
{
char*val
= sql_getvalue("SELECT `physical` FROM `cfgLogicalLanes` "
"WHERE `logical` = '%s' ",
ship_method);

if(val==NULL||strlen(val)==0)
return"exception";
return val;
}

/*:25*//*27:*/
#line 337 "app_lib.w"

int
app_label_load(int cartonSeq,char area[],int seq)
{
int err;
int tries= 0;
int docSeq= -1;
char verify[128];

while(tries<MAX_TRIES)
{
err= sql_query("SELECT docSeq "
"FROM rdsDocuments "
"WHERE refValue=%d "
"AND refType='cartonSeq' "
"AND docType='shipLabel' ",
cartonSeq);
if(!err)
{
if(sql_rowcount()> 0)
{
if(sql_get(0,0))
{
docSeq= atoi(sql_get(0,0));
break;
}
else
{
Alert("SQL error NULL docSeq");
return-1;
}
}
else
{
usleep(100000L);
tries++;
continue;
}
}
else
{
Alert("SQL error %d select rdsDocuments",err);
return-1;
}
}

if(docSeq>=0)
{
err= sql_query("REPLACE INTO labels "
"(seq,ordinal,printed,zpl) "
"SELECT %d, 1, 'no', document "
"FROM rdsDocuments "
"WHERE docSeq=%d "
"AND docType='shipLabel' "
"AND refType='cartonSeq' "
"AND refValue='%d' ",
seq,docSeq,cartonSeq);
if(err)
{
Alert("SQL document copy fails %d",err);
return-2;
}

verify[0]= '\0';
err= sql_query("SELECT verification "
"FROM rdsDocuments "
"WHERE docSeq=%d ",
docSeq);
if(!err)
if(sql_rowcount()> 0)
if(sql_get(0,0))
strncpy(verify,sql_get(0,0),128);
verify[128]= '\0';

util_carton_set(area,seq,"verify",verify);

return docSeq;
}

return 0;
}

/*:27*//*30:*/
#line 428 "app_lib.w"

void
app_update_carton_ship_label(const char area[],int seq,int carton_seq)
{
int err;
char verification[LBUF_LEN+1];

if(carton_seq<0)
return;

err= sql_query("SELECT `verification` "
"FROM `rdsDocuments` "
"WHERE `docType` = 'shipLabel' "
"AND `refValue` = %d",
carton_seq);
if(err||sql_rowcount()==0)
{
Alert("sql error (%d) determining label data for carton [%d]",
err,carton_seq);
return;
}

strncpy(verification,sql_get(0,0),LBUF_LEN);
verification[LBUF_LEN]= '\0';

Inform("  ship label for carton [%d], verify [%s]",
carton_seq,verification);
sql_query("REPLACE labels (seq, ordinal, zpl) "
"SELECT %d, 1, "
"REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( "
"REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( document, "
"'^LH', '^FX' ), "
"'^MCN', '^FXN' ), "
"'^MD', '^FX' ), "
"'^MM', '^FX' ), "
"'^MN', '^FX' ), "
"'^PM', '^FX' ), "
"'^PQ', '^FX' ), "
"'^PR', '^FX' ), "
"'^PW', '^FX' ), "
"'~SD', '^FX' ), "
"'^XA', '^XA^LH0,0' ), "
"'^XZ', '^XZ' ) AS zpl "
"FROM rdsDocuments "
"WHERE `docType` = 'shipLabel' "
"AND `refValue` = %d "
"AND LENGTH( document ) > 0",
seq,carton_seq);
Trace("Setting verify value to tracking number");
util_carton_set(area,seq,"verify",verification);
util_update_status(seq,"label","pending","");
}

/*:30*//*32:*/
#line 486 "app_lib.w"

int
app_parse_scan(const char scan_msg[],char barcode[])
{
char*str,*str_ptr;
char found[10][64+1];
int i,n_found;

n_found= 0;
strcpy(barcode,"");
for(i= 0;i<10;i++)
found[i][0]= '\0';

str= strdup(scan_msg);
str_ptr= str;
while(str_ptr!=NULL)
{
char*candidate= strsep(&str_ptr,",");

if(is_valid(candidate))
{
Inform("     valid barcode [%s]",candidate);
for(i= 0;i<n_found;i++)
{
if(0==strcmp(found[i],candidate))
{
Inform("      already found");
break;
}
}
if(i==n_found)
{
Inform("      new barcode, add");
if(i<(10-1))
{
strncpy(found[n_found],candidate,64);
found[n_found][64]= '\0';
n_found++;
}
else
Inform("     too many barcodes, drop");
}
}
else
{
Inform("     ignore barcode [%s]",candidate);
}
if(n_found> 0)
strcpy(barcode,found[0]);
}
free(str);

return n_found;
}

/*:32*//*36:*/
#line 562 "app_lib.w"

void
app_update_hist(const char area[],int seq,int carton_seq)
{
if(area==NULL||strlen(area)==0||seq<=0||carton_seq<=0)
return;

sql_query("UPDATE cartonLog SET "
"id = '%d', "
"stamp = stamp "
"WHERE id = 'seq%d'",
carton_seq,seq);

}

/*:36*//*38:*/
#line 582 "app_lib.w"

char*
app_get_hint(int box)
{
int seq= -1;
static char hint[HINT_LEN+1];
char*val,area[BUF_LEN+1];

if(box<MIN_TRAK_BOX||box> MAX_TRAK_BOX)
strcpy(hint,"");
else if((seq= util_box_get_int(box,"seq"))<=0)
snprintf(hint,HINT_LEN,"<%d>",box);
else
{
val= util_box_get(box,"area");
if(val!=NULL&&strlen(val)> 0)
{
strncpy(area,val,sizeof(area)-1);
area[sizeof(area)-1]= '\0';

val= util_carton_get(area,seq,"barcode");
if(val!=NULL&&strlen(val)> 0)
snprintf(hint,HINT_LEN,"[%s]",val);
else
snprintf(hint,HINT_LEN,"carton %d",seq);
}
else
snprintf(hint,HINT_LEN,"carton %d",seq);
}
hint[HINT_LEN]= '\0';
return(hint);
}

/*:38*//*40:*/
#line 620 "app_lib.w"

void
app_generate_simple_label(char label[],const char barcode[],const char msg[])
{
char tmp[2*MSG_LEN+1];

sprintf(label,"^XA\n^LH0,0^FS\n");

if(strlen(barcode)> 0)
{
sprintf(tmp,
"^FO200,400^BY3,,102\n"
"^BCN^FD%s^FS\n",
barcode);
strcat(label,tmp);
}

if(strlen(msg)> 0)
{
sprintf(tmp,
"^FO100,600^A0N,30,30^FB600,8^FD%s^FS\n",
msg);
strcat(label,tmp);
}

strcat(label,"^XZ\n");
}

/*:40*//*42:*/
#line 656 "app_lib.w"

int
app_label_ready(int seq,const char printer[])
{
char*val;
char carton_id[BUF_LEN+1];
int ordinal;

Inform("  checking if label is ready...");
val= sql_getvalue("SELECT status FROM cartonStatus "
"WHERE seq = %d "
"AND name = 'label'",
seq);
if(val==NULL||strcmp(val,"failed")==0)
return-1;

if(strcmp(val,"complete")!=0)
return 0;

sprintf(carton_id,"%d",seq);
val= sql_getvalue("SELECT ordinal FROM labels "
"WHERE seq = %d "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
seq,printer);
if(val==NULL||(ordinal= atoi(val))<=0)
return-1;

return ordinal;
}

/*:42*//*44:*/
#line 693 "app_lib.w"

int
app_get_label(int seq,const char printer[],char label[],int*len)
{
int err,ordinal,llen;
char*val;
char carton_id[BUF_LEN+1];
int i;

Inform("  getting label...");
strcpy(label,"");

sprintf(carton_id,"%d",seq);
err= sql_query("SELECT ordinal, zpl FROM labels "
"WHERE seq = %d "
"AND printer = '%s' "
"AND printed = 'no' "
"ORDER BY ordinal LIMIT 1",
seq,printer);

if(err)
{
Alert("sql error retrieving label data, err = %d",err);
return 0;
}

if(sql_rowcount()!=1)
return 0;

ordinal= atoi(sql_get(0,0));
val= sql_getlen(0,1,&llen);
if(llen> *len)
{
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

/*:44*//*46:*/
#line 755 "app_lib.w"

void
app_label_printed(int seq,const char printer[],int ordinal)
{
char*val,carton_id[BUF_LEN+1];

Inform("  marking label page %d as printed...",ordinal);

sprintf(carton_id,"%d",seq);
sql_query("UPDATE labels SET "
"printed = 'yes' "
"WHERE seq = %d "
"AND printer = '%s' "
"AND ordinal = %d",
seq,printer,ordinal);

val= sql_getvalue("SELECT COUNT(*) FROM labels "
"WHERE seq = %d "
"AND printer = '%s' "
"AND printed = 'no' ",
seq,printer);
if(val!=NULL&&atoi(val)==0)
util_carton_set("xpal",seq,"printed","1");
}

/*:46*//*50:*/
#line 814 "app_lib.w"

int
app_get_lane(const char area[],const char logical[],char description[])
{
int lane= -1;
int err;

err= sql_query("SELECT physical, description FROM cfgLanes "
"WHERE area = '%s' "
"AND logical = '%s'",
area,logical);
if(err)
{
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

/*:50*//*52:*/
#line 847 "app_lib.w"

int
app_trigger_scale(const char dev_name[])
{
int err;

err= sql_query("UPDATE runtime "
"SET value = CONCAT('SRP','\\r') "
"WHERE name = '%s/xmit' ",
dev_name);

return!err;
}

/*:52*//*54:*/
#line 866 "app_lib.w"

int
app_get_physical_code(const char area[],const char physical[])
{
char*val
= sql_getvalue("SELECT `physicalCode` "
"FROM `cfgPhysicalLanes` "
"WHERE `area` = '%s' "
"AND `physical` = '%s' ",
area,physical);

if(val==NULL||strlen(val)==0)
return 0;
return atoi(val);
}

/*:54*//*56:*/
#line 887 "app_lib.w"

void
app_get_lpn_label(char label[],int*len)
{
char tmp[2*MSG_LEN+1];
char carton_type[BUF_LEN+1];

sprintf(label,
"^XA"
"^FWR"
"^BY3,2,130"
"^FO375,100^BC^FD");

int lpn_seq
= atoi(sql_getvalue("SELECT value FROM runtime "
"WHERE name = 'lpn/ctr'"));
if(lpn_seq<0)
lpn_seq= 0;

sprintf(tmp,"B%07d",lpn_seq);
strcat(label,tmp);

sql_query("REPLACE INTO runtime SET name = 'lpn/ctr', value = '%d'",
lpn_seq+1);

strcat(label,"^XZ");

*len= strlen(label);
}

/*:56*//*58:*/
#line 922 "app_lib.w"

int
app_carton_ready(int cartonSeq)
{
return 1;
}

/*:58*//*60:*/
#line 934 "app_lib.w"

int
app_first_box(char*area)
{
int first= 1;

if(0==strcmp(area,"eastPack"))
first= 1;
else if(0==strcmp(area,"east"))
first= 26;
return first;
}

/*:60*//*62:*/
#line 952 "app_lib.w"

int
app_last_box(char*area)
{
int last= 999;

if(0==strcmp(area,"eastPack"))
last= 25;
else if(0==strcmp(area,"east"))
last= 75;

return last;
}

/*:62*//*64:*/
#line 971 "app_lib.w"

int
app_carton_needs_qc(int cartonSeq)
{
return 0;
}

/*:64*//*66:*/
#line 983 "app_lib.w"

char*
string(int val)
{
static char s[32+1];

snprintf(s,32,"%d",val);
s[32]= '\0';

return s;
}

/*:66*/
#line 82 "app_lib.w"

/*34:*/
#line 546 "app_lib.w"

int
is_valid(char*candidate)
{
if(strlen(candidate)!=9)
return 0;
if((candidate[0]!='T')&&(candidate[0]!='C'))
return 0;
return 1;
}

/*:34*//*48:*/
#line 786 "app_lib.w"

int
zpl_remove_safe(char*pszZpl,int len,const char*pszZplCmd)
{
char*pc;
int i;
int cmd_len= strlen(pszZplCmd);
for(pc= pszZpl,i= 0;i<len;i++,pc++)
{
if(!strncmp(pc,pszZplCmd,cmd_len))
{
int j;
for(j= 0;j<cmd_len;j++)
*pc++= 0x20;
while(*pc!='^')
*pc++= 0x20;
return len;
}
}

return len;
}

/*:48*//*68:*/
#line 1000 "app_lib.w"

void proto(void){};
/*:68*/
#line 83 "app_lib.w"



/*:1*/
