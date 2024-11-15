/*2:*/
#line 85 "pcl_lib.w"

/*3:*/
#line 93 "pcl_lib.w"

#include <stdio.h>  
#include <stdlib.h>  
#include <unistd.h>  
#include <string.h>  
#include <ctype.h>  

#include <pcl.h>  

/*:3*//*11:*/
#line 154 "pcl_lib.w"

#include <stdarg.h>  

/*:11*/
#line 86 "pcl_lib.w"

/*5:*/
#line 111 "pcl_lib.w"

static int pcl= 1;

/*:5*//*38:*/
#line 386 "pcl_lib.w"

int x,y,h,t;

/*:38*//*43:*/
#line 458 "pcl_lib.w"

char*patterns[]= {
"212222",
"222122",
"222221",
"121223",
"121322",
"131222",
"122213",
"122312",
"132212",
"221213",
"221312",
"231212",
"112232",
"122132",
"122231",
"113222",
"123122",
"123221",
"223211",
"221132",
"221231",
"213212",
"223112",
"312131",
"311222",
"321122",
"321221",
"312212",
"322112",
"322211",
"212123",
"212321",
"232121",
"111323",
"131123",
"131321",
"112313",
"132113",
"132311",
"211313",
"231113",
"231311",
"112133",
"112331",
"132131",
"113123",
"113321",
"133121",
"313121",
"211331",
"231131",
"213113",
"213311",
"213131",
"311123",
"311321",
"331121",
"312113",
"312311",
"332111",
"314111",
"221411",
"431111",
"111224",
"111422",
"121124",
"121421",
"141122",
"141221",
"112214",
"112412",
"122114",
"122411",
"142112",
"142211",
"241211",
"221114",
"413111",
"241112",
"134111",
"111242",
"121142",
"121241",
"114212",
"124112",
"124211",
"411212",
"421112",
"421211",
"212141",
"214121",
"412121",
"111143",
"111341",
"131141",
"114113",
"114311",
"411113",
"411311",
"113141",
"114131",
"311141",
"411131",
"211412",
"211214",
"211232",
"233111"
};

/*:43*/
#line 87 "pcl_lib.w"

/*33:*/
#line 325 "pcl_lib.w"

char*trim_left(char*src)
{
while(isspace(*src))
src++;
return src;
}

char*trim_right(char*src)
{
char*c;
int len;

len= strlen(src);
if(len)
{
for(c= src+(len-1);isspace(*c);c--)
{
if(c==src)return src;
*c= '\0';
}
}
return src;
}

/*:33*//*35:*/
#line 357 "pcl_lib.w"

int is_blank(char*string)
{
while(*string)
if(*(string++)!=' ')
return 0;
return 1;
}

/*:35*//*37:*/
#line 372 "pcl_lib.w"

int is_number(char*string)
{
while(*string)
{
if((*string<'0')||(*string> '9'))
return 0;
else
string++;
}
return 1;
}

/*:37*//*39:*/
#line 390 "pcl_lib.w"

void bar(int w,int fill)
{
if(fill==0)
{
pcl_printf("\033*p%dX",x);
pcl_printf("\033*p%dY",y);
pcl_printf("\033*c%dA",w*t);
pcl_printf("\033*c%dB",h);
pcl_printf("\033*c%dP",0);
}
x+= w*t;
}


/*:39*//*40:*/
#line 406 "pcl_lib.w"

void ladder_bar(int w,int fill)
{
if(fill==0)
{
pcl_printf("\033*p%dX",x);
pcl_printf("\033*p%dY",y);
pcl_printf("\033*c%dA",h);
pcl_printf("\033*c%dB",w*t);
pcl_printf("\033*c%dP",0);
}
y+= w*t;
}




/*:40*//*41:*/
#line 424 "pcl_lib.w"

void module(char*pattern)
{
int i,n;
n= strlen(pattern);
for(i= 0;i<n;i++)
{
int wide= pattern[i]-'0';
int fill= i%2;

bar(wide,fill);
}
}

/*:41*//*42:*/
#line 440 "pcl_lib.w"

void ladder_module(char*pattern)
{
int i,n;
n= strlen(pattern);
for(i= 0;i<n;i++)
{
int wide= pattern[i]-'0';
int fill= i%2;

ladder_bar(wide,fill);
}
}



/*:42*/
#line 88 "pcl_lib.w"

/*6:*/
#line 115 "pcl_lib.w"

int pcl_handle(int handle)
{
pcl= handle;
return pcl;
}

/*:6*//*9:*/
#line 133 "pcl_lib.w"

int pcl_printf(char*fmt,...)
{
char buffer[256+1];
va_list argptr;
int cnt;

/*8:*/
#line 128 "pcl_lib.w"

if(pcl<0)
return 0;

/*:8*/
#line 140 "pcl_lib.w"


va_start(argptr,fmt);
cnt= vsprintf(buffer,fmt,argptr);
va_end(argptr);
write(pcl,buffer,cnt);
return cnt;
}

/*:9*//*13:*/
#line 163 "pcl_lib.w"

void pcl_init(void)
{

pcl_printf("\033E");

pcl_printf("\033&l%dU",0);
pcl_printf("\033&l%dZ",0);
pcl_printf("\033&l%dS",0);

}

/*:13*//*15:*/
#line 180 "pcl_lib.w"

void pcl_done(void)
{
pcl_printf("\f");
pcl_printf("\033E");

}

/*:15*//*17:*/
#line 193 "pcl_lib.w"

void pcl_push(void)
{
pcl_printf("\033&f0S");
}
void pcl_pop(void)
{
pcl_printf("\033&f1S");
}

/*:17*//*19:*/
#line 209 "pcl_lib.w"

void pcl_orient(int o)
{
pcl_printf("\033&l%dO",o);
}

/*:19*//*21:*/
#line 223 "pcl_lib.w"

void pcl_font(int height,
int style,
int stroke,
int family)
{
pcl_printf("\033(%dU",0);
pcl_printf("\033(s%dP",1);
pcl_printf("\033(s%dV",(int)((long)height*72L/100L));

pcl_printf("\033(s%dS",style);
pcl_printf("\033(s%dB",stroke);
pcl_printf("\033(s%dT",family);
}

/*:21*//*23:*/
#line 244 "pcl_lib.w"

void pcl_fix(int height,
int style,
int stroke,
int family)
{
pcl_printf("\033(%dU",0);
pcl_printf("\033(s%dP",0);
pcl_printf("\033(s%dV",(int)((long)height*72L/100L));

pcl_printf("\033(s%dS",style);
pcl_printf("\033(s%dB",stroke);
pcl_printf("\033(s%dT",family);
}

/*:23*//*25:*/
#line 266 "pcl_lib.w"

void pcl_xy(int x,int y)
{
pcl_printf("\033*p%dX",3*x);
pcl_printf("\033*p%dY",3*y);
}

/*:25*//*27:*/
#line 279 "pcl_lib.w"

void pcl_rule(int wide,int high)
{
pcl_printf("\033*c%dA",3*wide);
pcl_printf("\033*c%dB",3*high);
pcl_printf("\033*c%dP",0);
}

/*:27*//*29:*/
#line 293 "pcl_lib.w"

void pcl_eject(void)
{
pcl_printf("%c",0x0C);
pcl_printf("\033&r%dF",0);
}

/*:29*//*31:*/
#line 305 "pcl_lib.w"

void pcl_watermark(char*msg)
{
pcl_printf("\033*v0N");
pcl_printf("\033*v0O");
pcl_printf("\033*c5G");
pcl_printf("\033*v2T");

pcl_font(300,1,5,4148);
pcl_xy(100,275);
pcl_printf("%s",msg);

pcl_printf("\033*v0T");
}

/*:31*//*44:*/
#line 570 "pcl_lib.w"

void pcl_code128c(int startx,int starty,int height,int thick,char*data)
{
int sum,i,n;

x= startx*3;
y= starty*3;
h= height*3;
t= thick;

n= strlen(data);

sum= 105;
module(patterns[105]);


for(i= 0;i<n/2;i++)
{
int code;
char tmp[2+1];
strncpy(tmp,data+(2*i),2);tmp[2]= '\0';
code= atoi(tmp);
sum+= code*(i+1);

module(patterns[code]);
}
sum%= 103;
module(patterns[sum]);


module(patterns[106]);

module("2");
}

/*:44*//*46:*/
#line 611 "pcl_lib.w"

void pcl_code128c_ladder(int startx,int starty,int height,int thick,char*data)
{
int sum,i,n;

x= startx*3;
y= starty*3;
h= height*3;
t= thick;

n= strlen(data);

sum= 105;
ladder_module(patterns[105]);


for(i= 0;i<n/2;i++)
{
int code;
char tmp[2+1];
strncpy(tmp,data+(2*i),2);tmp[2]= '\0';
code= atoi(tmp);
sum+= code*(i+1);

ladder_module(patterns[code]);
}
sum%= 103;
ladder_module(patterns[sum]);


ladder_module(patterns[106]);

ladder_module("2");
}

/*:46*/
#line 89 "pcl_lib.w"


/*:2*/
