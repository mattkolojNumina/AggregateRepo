/*6:*/
#line 187 "rds_tag.w"

#ifndef __RDS_TAG_H
#define __RDS_TAG_H
/*64:*/
#line 1305 "rds_tag.w"

int tag_sync(int flag);

/*:64*//*67:*/
#line 1345 "rds_tag.w"

int tag_init(void);

/*:67*//*70:*/
#line 1394 "rds_tag.w"

int tag_export(FILE*out);

/*:70*//*73:*/
#line 1457 "rds_tag.w"

int tag_import(FILE*in);

/*:73*//*76:*/
#line 1494 "rds_tag.w"

int tag_delete(char*key);

/*:76*//*79:*/
#line 1566 "rds_tag.w"

int tag_insert(char*key,const char*value);

/*:79*//*82:*/
#line 1632 "rds_tag.w"

char*tag_value(char*key);

/*:82*//*85:*/
#line 1700 "rds_tag.w"

char*tag_first(char*key);

/*:85*//*88:*/
#line 1768 "rds_tag.w"

char*tag_next(char*key);

/*:88*//*91:*/
#line 1823 "rds_tag.w"

int tag_iterate(char*stem,int(*item)(char*key));

/*:91*//*94:*/
#line 1885 "rds_tag.w"

int tag_space(void);

/*:94*//*97:*/
#line 1928 "rds_tag.w"

void tag_branch(int x);
int tag_tree(void);

/*:97*//*99:*/
#line 1957 "rds_tag.w"

void tag_verify(void);

/*:99*/
#line 190 "rds_tag.w"

#endif
#line 192 "rds_tag.w"

/*:6*/
