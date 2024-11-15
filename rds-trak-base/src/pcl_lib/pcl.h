/*4:*/
#line 103 "pcl_lib.w"

/*7:*/
#line 123 "pcl_lib.w"

int pcl_handle(int handle);

/*:7*//*10:*/
#line 150 "pcl_lib.w"

int pcl_printf(char*fmt,...);

/*:10*//*14:*/
#line 176 "pcl_lib.w"

void pcl_init(void);

/*:14*//*16:*/
#line 189 "pcl_lib.w"

void pcl_done(void);

/*:16*//*18:*/
#line 204 "pcl_lib.w"

void pcl_push(void);
void pcl_pop(void);

/*:18*//*20:*/
#line 216 "pcl_lib.w"

void pcl_orient(int o);

/*:20*//*22:*/
#line 239 "pcl_lib.w"

void pcl_font(int height,int style,int stroke,int family);

/*:22*//*24:*/
#line 260 "pcl_lib.w"

void pcl_fix(int height,int style,int stroke,int family);

/*:24*//*26:*/
#line 274 "pcl_lib.w"

void pcl_xy(int x,int y);

/*:26*//*28:*/
#line 288 "pcl_lib.w"

void pcl_rule(int wide,int high);

/*:28*//*30:*/
#line 301 "pcl_lib.w"

void pcl_eject(void);

/*:30*//*32:*/
#line 321 "pcl_lib.w"

void pcl_watermark(char*msg);

/*:32*//*45:*/
#line 606 "pcl_lib.w"

void pcl_code128c(int startx,int starty,int height,int thick,char*data);


/*:45*/
#line 104 "pcl_lib.w"



/*:4*/
