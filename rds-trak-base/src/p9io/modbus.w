% 
%   modbus.w -- read/write modbus holding regs over tcp/ip 
%
%   Author: Mark Woodworth 
%
%   History:
%      2013-03-28 -- check in (mrw) x
%
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
%     (C) Copyright 2013 Numina Systems Corporation.  All Rights Reserved.
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
\def\myitem{\quad\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{Modbus}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Command line tool to read and write modbus holding regs over TCP/IP

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
\centerline{RCS ID: $ $Id: modbus.w,v 1.5 2019/03/08 17:03:00 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: modbus.w,v 1.5 2019/03/08 17:03:00 rds Exp $" ;
@<Includes@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int main(int argc, char *argv[])
   {
   @<Initialize@>@;

   if(rw)
      do_write(reg,value) ;
   else
      do_read(reg,value) ; 
   }

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>

#include <rds_trn.h>
#include <rds_net.h>

@ Initialize.
@<Initialize@>+=
   {
   if(argc<5)
      {
      fprintf(stderr,"usage: %s <read/write> <ip> <reg> <count/value>\n",
              argv[0]) ;
      exit(1) ;
      }

   rw = 0 ;
   if(tolower(argv[1][0])=='w')
      rw = 1 ;
 
   strncpy(ip,argv[2],32) ;
   ip[32] = '\0' ;

   reg = atoi(argv[3]) ;

   value = (int)strtol(argv[4],NULL,0) ;

   trn_register("modbus") ;
   Trace("%s",rcsid) ;

   if(rw)
      Inform("write reg %d value %d",reg,value) ;
   else
      Inform("read  reg %d count %d",reg,value) ;
   }

@ Name.
@<Globals@>+=
char ip[32+1] ;
int rw = 0 ;
int reg = 0 ;
int value = 0 ;

@* Read.
@<Functions@>+=
int do_read(int reg, int value) 
   {
   int offset, count ;
   unsigned char out[128+1] ;
   int o ; 
   unsigned char in[128+1] ;
   int i ;
   int n ;
   int pos ;

#if 0
   if(reg < 100000)
      offset = reg - 40001 ;
   else
      offset = reg - 400001 ;
#endif
   offset = reg ;

   if(offset <0)
      {
      fprintf(stderr,"bad register address %d",reg) ;
      return -1 ;
      }
   count = value ;

   o = 0 ;
   out[o++] = 0x00 ; // tns hi
   out[o++] = 0x00 ; // tns lo 
   out[o++] = 0x00 ; // protocol hi
   out[o++] = 0x00 ; // protocol lo
   pos = o ;
   out[o++] = 0x00 ; // length hi
   out[o++] = 0x00 ; // length lo
   out[o++] = 0x01 ; // unit id 
   out[o++] = 0x03 ;
   out[o++] = (offset>>8) & 0xff ;
   out[o++] = (offset>>0) & 0xff ;
   out[o++] = (count >>8) & 0xff ;
   out[o++] = (count >>0) & 0xff ;
  
   out[pos+0] = ((o-pos-2)>>8) & 0xff ; 
   out[pos+1] = ((o-pos-2)>>0) & 0xff ;

   request(out,o) ;
   i = response(in) ;

   if(in[7] == 0x03)
      {
      int n, total ;
      total = in[8] /2 ;
      for(n=0 ; n<total ; n++)
         {
         int content ;
         int address ;

         address = reg + n ;
         content =   in[9+2*n+0] ;
         content <<= 8 ;        
         content +=  in[9+2*n+1] ;
         
         printf("%5d: %5d (0x%04x)\n",address,content,content) ;
         }
      }
   else
      {
      printf("message error\n") ;
      }
   return 0 ;
   }

@ Proto.
@<Prototypes@>+=
int do_read(int reg, int count) ;

@* Write.
@<Functions@>+=
int do_write(int reg, int count) 
   {
   int offset ;
   unsigned char out[128+1] ;
   int o ; 
   unsigned char in[128+1] ;
   int i ;
   int n ;
   int pos ;

   offset = reg ;
#if 0
   if(reg < 100000)
      offset = reg - 40001 ;
   else
      offset = reg - 400001 ;
#endif

   if(offset <0)
      {
      fprintf(stderr,"bad register address %d",reg) ;
      return -1 ;
      }

   o = 0 ;
   out[o++] = 0x00 ; // tns hi
   out[o++] = 0x00 ; // tns lo 
   out[o++] = 0x00 ; // protocol hi
   out[o++] = 0x00 ; // protocol lo
   pos = o ;
   out[o++] = 0x00 ; // length hi
   out[o++] = 0x00 ; // length lo
   out[o++] = 0x01 ; // unit id 
   out[o++] = 0x06 ;
   out[o++] = (offset>>8) & 0xff ;
   out[o++] = (offset>>0) & 0xff ;
   out[o++] = (value >>8) & 0xff ;
   out[o++] = (value >>0) & 0xff ;
  
   out[pos+0] = ((o-pos-2)>>8) & 0xff ; 
   out[pos+1] = ((o-pos-2)>>0) & 0xff ;

   request(out,o) ;
   i = response(in) ;

   if(in[7] == 0x06)
      printf("wrote %d to %d\n",value,reg) ;
   else
      fprintf(stderr,"message error\n") ;

   return 0 ;
   }

@ Proto.
@<Prototypes@>+=
int do_write(int reg, int value) ;
 
@* Request.  
@<Functions@>+=
int request(unsigned char *cmd, int len)
   {
   int s,i ;

for (i = 0; i < len; i++)


   s = comm_attach() ;
   if(s>=0)
      {
      int sent ;
      sent = write(s,cmd,len) ; 
      if(sent!=len)
         {
         fprintf(stderr,"comm error\n") ;
         comm_detach() ;
         }
      }
   else
      fprintf(stderr,"comm error\n") ;

   return len ;
   }

@ Proto.
@<Prototypes@>+=
int request(unsigned char *msg, int len) ;

@* Response.
@<Functions@>+=
int response(unsigned char *msg) 
   {
   fd_set rfds ;
   struct timeval tv ;
   int n,s,m ;
   int expect_len = 0 ;
   struct MBAP_Header {
     short xact ;
     short procotol ;
     short length ;
     unsigned char data[128] ;
   } *msg_buff ;

   msg_buff = (struct MBAP_Header*) msg ; 

   m=0 ;

   while(1)
      {
      FD_ZERO(&rfds) ;
      n = 0 ;

      s = comm_attach() ;
      if(s>=0)
         {
         FD_SET(s,&rfds) ;
         if(n<(s+1)) n = s+1 ;
         }  

      tv.tv_sec  = 0 ;
      tv.tv_usec = 100000L ;

      n = select(n,&rfds,NULL,NULL,&tv) ;

      if(n>0)
         {
         if(FD_ISSET(s,&rfds))
            { 
            unsigned char c ;
    
            if(read(s,&c,1)==1)
               {
               if (m > 9 && expect_len ==0) 
                    expect_len = ((msg_buff->length >> 8) & 0xff) +
                                    ((msg_buff->length & 0xff) << 8) + 6 ;
               if(m<(128-1))
                  {
                  msg[m++] = c ;
                  }
               if (m == expect_len ) {
                   Inform("length break") ;
                   break ;
                 }
               }
            else
               {
               printf("comm error\n") ;
               Alert("read error") ;
               comm_detach() ;
               }
            }
         }
      else if(n==0)
         {
         Inform("timeout break") ;
         break ; 
         }
      else if(n<0)
         {
         fprintf(stderr,"select error") ;
         comm_detach() ;
         break ;
         }
      }
   Trace("msg in = %d expected %d",m,expect_len) ;
   return m ;
   }

@ Proto.
@<Prototypes@>+=
int response(unsigned char *cmd) ;


@* Serial.
@<Globals@>+=
int iLink = -1 ;

@ Attach.
@<Functions@>+=
int comm_attach()
   {
   unsigned char recv[128+1] ;
   int r ;

   if(iLink<0)
      {
      iLink = net_open(ip,502) ;
      
      if(iLink>=0)
         {
         Inform("open %s handle %d",ip,iLink) ;
         usleep(10000L) ;
         }
      else
         {
         Alert("failed to open [%s]",ip) ;
         sleep(5) ;
         }
      }

   return iLink ;
   }

@ Proto.
@<Prototypes@>+=
int comm_attach() ;

@ Detach.
@<Functions@>+=
void comm_detach(void)
   {
   if(iLink)
      close(iLink) ;
   iLink = -1 ;
   }

@ Proto.
@<Prototypes@>+=
void comm_detach(void) ;

@* Index.

