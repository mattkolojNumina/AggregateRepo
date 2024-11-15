/*5:*/
#line 122 "./rds_ann.w"

#ifndef __RDS_ANN_H
#define __RDS_ANN_H
/*16:*/
#line 246 "./rds_ann.w"

static int ann_lock(void);
static int ann_unlock(void);

/*:16*//*20:*/
#line 313 "./rds_ann.w"

static int ann_check(void);

/*:20*//*22:*/
#line 347 "./rds_ann.w"

int ann_put(char*msg);

/*:22*//*25:*/
#line 410 "./rds_ann.w"

char*ann_get(int*seq);

/*:25*/
#line 125 "./rds_ann.w"

#endif


/*:5*/
