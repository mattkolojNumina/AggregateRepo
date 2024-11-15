% 
%   pcl_lib.w
%
%   Author: Mark Woodworth 
%
%   History:
%       2011-01-14 - modified from Aearo printd
% 12/27/2013 ank: added barcodes in ladder orientation
%
%         C O N F I D E N T I A L
%
%     This information is confidential and should not be disclosed to
%     anyone who does not have a signed non-disclosure agreement on file
%     with Numina Systems Corporation.  
%
%
%
%
%
%
%     (C) Copyright 2011 Numina Systems Corporation.  All Rights Reserved.
%
%
%

%
% --- macros ---
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\dot{\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{RDS PCL}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library collects functions to assist in printing to PCL printers.

%
% --- confidential ---
%
\bigskip
\centerline{\boxit{10pt}{\hsize 4in
\bigskip
\centerline{\bf CONFIDENTIAL}
\smallskip
This material is confidential.  
It must not be disclosed to any person
who does not have a current signed non-disclosure form on file with Numina
Systems Corporation.  
It must only be disseminated on a need-to-know basis.
It must be stored in a secure location. 
It must not be left out unattended.  
It must not be copied.  
It must be destroyed by burning or shredding. 
\smallskip
}}

%
% --- id ---
%
\bigskip
\centerline{Authors: Mark Woodworth}
\centerline{RCS ID: $ $Id: pcl_lib.w,v 1.6 2024/04/15 10:18:31 rds Exp rds $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2011 Numina Systems Corporation.  
All Rights Reserved.}
}


@* PCL. Printer library. 

@* Overview.
@c
@<Includes@>@;
@<Locals@>@;
@<Functions@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>

#include <pcl.h>

@ Header.  We put the exported prototypes in an external header.
@(pcl.h@>=
   @<Exported Prototypes@> @;


@* Output.  We submerge the file pointer from view
by holding it in a static,
rather than passing it explicitly in all of the PCL calls.
It defaults to standard output (1).
@<Locals@>+=
   static int pcl = 1 ;

@ A function is called to set the output.
@<Exported Functions@>+=
int pcl_handle(int handle)
   {
   pcl = handle ;
   return pcl ;
   }

@ Proto.
@<Exported Prototypes@>+=
int pcl_handle(int handle) ;

@ We protect ourselves by checking to see that somebody has opened
the file pointer in the functions here. 
@<Check file pointer@>+=
   if(pcl<0)
      return 0 ;

@* Print. We encapsulate printing in a printf-like function.
@<Exported Functions@>+=
int pcl_printf(char *fmt,...)
   {
   char buffer[256+1] ;
   va_list argptr ;
   int cnt ;

   @<Check file pointer@>@;

   va_start(argptr,fmt) ;
   cnt = vsprintf(buffer,fmt,argptr) ;
   va_end(argptr) ;
   write(pcl,buffer,cnt) ;
   return cnt ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int pcl_printf(char *fmt,...) ;

@ This requires an include file.
@<Includes@>+=
#include <stdarg.h>

@* Init and Done.  
At the beginning of each print job we set critical information that
affects the whole job.  At the end we reset the printer.

@ Init. We initialize the printer by sending a collection of
setup strings.
@<Exported Functions@>+=
void pcl_init(void)
   {
//   pcl_printf("\033%%-12345X");
   pcl_printf("\033E") ;         /* reset the printer       4-3  */
//   pcl_printf("\033&u%dD",300) ; /* unit of measure 4-13 */
   pcl_printf("\033&l%dU",0) ;   /* left offset             4-10 */
   pcl_printf("\033&l%dZ",0) ;   /* top  offest             4-11 */
   pcl_printf("\033&l%dS",0) ; /* simplex 4-5 */
   
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_init(void) ;

@ Done. This finishes the job.
@<Exported Functions@>+=
void pcl_done(void)
   {
   pcl_printf("\f") ;            /* form feed */
   pcl_printf("\033E") ;         /* reset the printer       4-3  */
//   pcl_printf("\033%%-12345X") ; /* universal exit language 4-4  */
   }    

@ Prototype.
@<Exported Prototypes@>+=
void pcl_done(void) ;

@ Push/Pop.  Stores or recalls the cursor position.
@<Exported Functions@>+=
void pcl_push(void)
   {
   pcl_printf("\033&f0S") ;
   }
void pcl_pop(void)
   {
   pcl_printf("\033&f1S") ;
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_push(void) ;
void pcl_pop(void) ;

@ Orientation. This sets the orientation: 0 is portrait, 1 is landscape.
@<Exported Functions@>+=
void pcl_orient(int o)
   {
   pcl_printf("\033&l%dO",o) ;
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_orient(int o) ;

@ Fonts. 
We encapsulate the major font specifiers into a function.
We assume: ASCII symbol set, proportional, scalable.
We use hundreths of an inch for the height.
@<Exported Functions@>+=
void pcl_font(int height,
              int style,
              int stroke,
              int family) 
   {
   pcl_printf("\033(%dU" ,0) ;      /* ASCII symbol set 8-7 */
   pcl_printf("\033(s%dP",1) ;      /* proportional spacing 8-9 */
   pcl_printf("\033(s%dV",(int)((long)height*72L/100L)) ; 
                                     /* height in 0.01" 8-13 */
   pcl_printf("\033(s%dS",style) ;  /* style 8-15 */
   pcl_printf("\033(s%dB",stroke) ; /* stroke weight 8-18*/
   pcl_printf("\033(s%dT",family) ; /* family  8-21 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_font(int height, int style, int stroke, int family) ;

@ We also build a font selector for fixed space fonts.  These are
still scalable.
@<Exported Functions@>+=
void pcl_fix(int height,
             int style,
             int stroke,
             int family) 
   {
   pcl_printf("\033(%dU" ,0) ;      /* ASCII symbol set 8-7 */
   pcl_printf("\033(s%dP",0) ;      /* fixed spacing 8-9 */
   pcl_printf("\033(s%dV",(int)((long)height*72L/100L)) ; 
                                     /* height in 0.01" 8-13 */
   pcl_printf("\033(s%dS",style) ;  /* style 8-15 */
   pcl_printf("\033(s%dB",stroke) ; /* stroke weight 8-18*/
   pcl_printf("\033(s%dT",family) ; /* family  8-21 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_fix(int height, int style, int stroke, int family) ;

@ Position.
We position objects in units of hundredths of an inch.
X is measured from the left, Y is measured from the top.
@<Exported Functions@>+=
void pcl_xy(int x, int y)
   {
   pcl_printf("\033*p%dX",3*x) ; /* horizontal in PCL units 6-7  */
   pcl_printf("\033*p%dY",3*y) ; /* vertical   in PLC units 6-12 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_xy(int x, int y) ;

@ Rule.
A rule is a rectangular filled region.
@<Exported Functions@>+=
void pcl_rule(int wide, int high)
   {
   pcl_printf("\033*c%dA",3*wide) ; /* horizontal in PCL units 14-4 */
   pcl_printf("\033*c%dB",3*high) ; /* vertical   in PCL units 14-6 */
   pcl_printf("\033*c%dP",0)      ; /* fill rect black         14-11 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_rule(int wide, int high) ;

@ Eject.
When a page is complete we call this to eject it.
@<Exported Functions@>+=
void pcl_eject(void)
   {
   pcl_printf("%c",0x0C) ;       /* form feed */
   pcl_printf("\033&r%dF",0) ; /* flush all complete pages 16-29 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_eject(void) ;

@ Watermark.  As a feature, we put a very light watermark in the background.
@<Exported Functions@>+=
void pcl_watermark(char *msg)
   {
   pcl_printf("\033*v0N") ; /* source transparent  13-7 */
   pcl_printf("\033*v0O") ; /* pattern transparent 13-8 */ 
   pcl_printf("\033*c5G") ; /* pattern ID 5 percent 13-9 */
   pcl_printf("\033*v2T") ; /* shading              13-13 */
 
   pcl_font(300,1,5,4148) ;
   pcl_xy(100,275) ;
   pcl_printf("%s",msg) ;

   pcl_printf("\033*v0T") ; /* solid black 13-13 */
   }

@ Prototype.
@<Exported Prototypes@>+=
void pcl_watermark(char *msg) ;

@ A simple function to trim leading spaces for printing.
@<Functions@>+=
char *trim_left(char *src) 
   {
   while (isspace(*src)) 
      src++ ;
   return src ;
   }

char *trim_right(char *src) 
   {
   char *c ;
   int len ;

   len = strlen(src) ;
   if (len) 
      {
      for (c = src + (len-1) ; isspace(*c) ; c-- ) 
         {
         if (c == src) return src ;
         *c = '\0' ;
         }
      }
   return src ;
   }

@ Proto.
@<Prototypes@>+=
char *trim_left(char *src) ;
char *trim_right(char *src) ;

@ The function |is_blank()| checks to see if all the characters
in a string are blank.
@<Functions@>+=
int is_blank(char *string) 
   {
   while(*string)
      if(*(string++)!=' ')
         return 0 ;
   return 1 ;
   }

@ Prototype.
@<Prototypes@>+=
   int is_blank(char *string) ;

@ The function |is_number()| checks to see if all of the characters in
a string are digits.
@<Functions@>+=
int is_number(char *string)
   {
   while(*string)
      {
      if((*string<'0') || (*string>'9'))
         return 0 ;
      else
         string++ ;
      }
   return 1 ;
   }

@* Barcode.
@<Locals@>+=
int x, y, h, t ;

@ Bar. Draws one bar, advances x.
@<Functions@>+=
void bar(int w, int fill)
   {
   if(fill==0)
      {
      pcl_printf("\033*p%dX",x) ;
      pcl_printf("\033*p%dY",y) ;
      pcl_printf("\033*c%dA",w*t) ;
      pcl_printf("\033*c%dB",h  ) ;
      pcl_printf("\033*c%dP",0) ;
      }
   x += w * t ;
   }


@ Ladder Bar. Draws one ladder bar, advances y.
@<Functions@>+=
void ladder_bar(int w, int fill)
   {
   if(fill==0)
      {
      pcl_printf("\033*p%dX",x) ;
      pcl_printf("\033*p%dY",y) ;
      pcl_printf("\033*c%dA",h) ;
      pcl_printf("\033*c%dB",w*t);
      pcl_printf("\033*c%dP",0) ;
      }
   y += w * t ;
   }



  
@ Module.  Draws an alternating pattern of bars and spaces.
@<Functions@>+=
void module(char *pattern)
   {
   int i, n ;
   n = strlen(pattern) ;
   for(i=0 ; i<n ; i++)
      {
      int wide = pattern[i]-'0' ;
      int fill = i%2 ;
      //Trace("x %d y %d wide %d fill %d high %d\n",x,y,wide,fill,h) ;
      bar(wide,fill) ;
      }
   }

@ Ladder Module.  Draws an alternating pattern of bars and spaces in
ladder orientation.
@<Functions@>+=
void ladder_module(char *pattern)
   {
   int i, n ;
   n = strlen(pattern) ;
   for(i=0 ; i<n ; i++)
      {
      int wide = pattern[i]-'0' ;
      int fill = i%2 ;
      //Trace("x %d y %d wide %d fill %d high %d\n",x,y,wide,fill,h) ;
      ladder_bar(wide,fill) ;
      }
   }



@ Patterns.  Each pattern is encoded as a string, starting with the 
size of a bar, then a space, repeating.
@<Locals@>+=
char *patterns[] = {
   "212222", // 00
   "222122", // 01
   "222221", // 02
   "121223", // 03
   "121322", // 04
   "131222", // 05
   "122213", // 06
   "122312", // 07
   "132212", // 08
   "221213", // 09
   "221312", // 10
   "231212", // 11
   "112232", // 12
   "122132", // 13
   "122231", // 14
   "113222", // 15
   "123122", // 16
   "123221", // 17
   "223211", // 18
   "221132", // 19
   "221231", // 20
   "213212", // 21
   "223112", // 22
   "312131", // 23
   "311222", // 24
   "321122", // 25
   "321221", // 26
   "312212", // 27
   "322112", // 28
   "322211", // 29
   "212123", // 30
   "212321", // 31
   "232121", // 32
   "111323", // 33
   "131123", // 34
   "131321", // 35
   "112313", // 36
   "132113", // 37
   "132311", // 38
   "211313", // 39
   "231113", // 40
   "231311", // 41
   "112133", // 42
   "112331", // 43
   "132131", // 44
   "113123", // 45
   "113321", // 46
   "133121", // 47
   "313121", // 48
   "211331", // 49
   "231131", // 50
   "213113", // 51
   "213311", // 52
   "213131", // 53
   "311123", // 54
   "311321", // 55
   "331121", // 56
   "312113", // 57
   "312311", // 58
   "332111", // 59
   "314111", // 60
   "221411", // 61
   "431111", // 62
   "111224", // 63
   "111422", // 64
   "121124", // 65
   "121421", // 66
   "141122", // 67
   "141221", // 68
   "112214", // 69
   "112412", // 70
   "122114", // 71
   "122411", // 72
   "142112", // 73
   "142211", // 74
   "241211", // 75
   "221114", // 76
   "413111", // 77
   "241112", // 78
   "134111", // 79
   "111242", // 80
   "121142", // 81
   "121241", // 82
   "114212", // 83
   "124112", // 84
   "124211", // 85
   "411212", // 86
   "421112", // 87
   "421211", // 88
   "212141", // 89
   "214121", // 90
   "412121", // 91
   "111143", // 92
   "111341", // 93
   "131141", // 94
   "114113", // 95
   "114311", // 96 
   "411113", // 97
   "411311", // 98
   "113141", // 99
   "114131", //100
   "311141", //101
   "411131", //102
   "211412", //103
   "211214", //104
   "211232", //105
   "233111"  //106
   } ;

@ Barcode.
@<Exported Functions@>+=
void pcl_code128c(int startx, int starty, int height, int thick, char *data)
   {
   int sum,i,n ;

   x = startx*3 ;
   y = starty*3 ;
   h = height*3 ;
   t = thick  ;
   
   n = strlen(data) ;
 
   sum = 105 ;
   module(patterns[105]) ; /* start C */
   //Trace("start 105 pattern %s",patterns[105]) ;

   for(i=0 ;i<n/2 ; i++)
      {
      int code ;
      char tmp[2+1] ;
      strncpy(tmp,data+(2*i),2) ; tmp[2] = '\0' ;
      code = atoi(tmp) ;
      sum += code * (i+1) ;
      //Trace("i %d code %d sum %d pattern %s",i,code,sum,patterns[code]) ;
      module(patterns[code]) ; 
      }
   sum %= 103 ;
   module(patterns[sum]) ; /* checksum */

   //Trace("sum %d pattern %s",sum,patterns[sum]) ;
   module(patterns[106]) ; /*  stop */
   //Trace("stop pattern %s",patterns[106]) ;
   module("2")      ; /* term */ 
   }

@ Proto.
@<Exported Prototypes@>+=
void pcl_code128c(int startx, int starty, int height, int thick, char *data) ;


@ Ladder barcode.
@<Exported Functions@>+=
void pcl_code128c_ladder(int startx, int starty, int height, int thick, char *data)
   {
   int sum,i,n ;

   x = startx*3 ;
   y = starty*3 ;
   h = height*3 ;
   t = thick  ;
   
   n = strlen(data) ;
 
   sum = 105 ;
   ladder_module(patterns[105]) ; /* start C */
   //Trace("start 105 pattern %s",patterns[105]) ;

   for(i=0 ;i<n/2 ; i++)
      {
      int code ;
      char tmp[2+1] ;
      strncpy(tmp,data+(2*i),2) ; tmp[2] = '\0' ;
      code = atoi(tmp) ;
      sum += code * (i+1) ;
      //Trace("i %d code %d sum %d pattern %s",i,code,sum,patterns[code]) ;
      ladder_module(patterns[code]) ; 
      }
   sum %= 103 ;
   ladder_module(patterns[sum]) ; /* checksum */

   //Trace("sum %d pattern %s",sum,patterns[sum]) ;
   ladder_module(patterns[106]) ; /*  stop */
   //Trace("stop pattern %s",patterns[106]) ;
   ladder_module("2")      ; /* term */ 
   }

@ Proto.
@<Prototypes@>+=
void pcl_code128c_ladder(int startx, int starty, int height, int thick, char *data);



@* Demo.
@(pcl_test.c@>+=
#include <stdio.h>
#include <pcl.h>
#include <rds_trn.h>

int
main()
   {
   trn_register("pcl_test") ;
   pcl_init() ;
   pcl_xy(100,100) ;
   pcl_printf("pcl_code128c(100,200,50,3,\"0123456789\")") ;
   pcl_code128c(100,200,50,3,"0123456789") ;
   pcl_done() ;
   }

@* Index.

