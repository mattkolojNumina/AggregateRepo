% 
%   rds_tty.web
%
%   Author: Mark Woodworth 
%
%   History:
%       7/10/00 -- check in (mrw)
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
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{RDS3 TTY -- Serial Utilities}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The TTY library encapsulates some often used calls when working with serial ports.

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
\centerline{RCS ID: $ $Id: rds_tty.w,v 1.7 2019/12/13 21:15:15 rds Exp rds $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}


@* TTY:  serial utilities. 
The TTY library 
provides some useful serial routines.

This library is a shared C library 

@ Exported Functions.  This library exports the following functions:
{\narrower\narrower
\dot {\bf tty_open(dev,speed,parity,bits,stopbits)} returns a file handle for
a serial port.
\par
}

@* Overview.
@c
@<Includes@>@;
@<Prototypes@>@;
@<Functions@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

#include <termios.h>

#include "rds_tty.h" 

@ Header.  We put the exported prototypes in an external header.
@(rds_tty.h@>=
#ifndef __RDS_TTY_H
#define __RDS_TTY_H
   @<Exported Prototypes@> @;
#endif

@ TTY Open.  Open a serial port.
@<Exported Functions@>+=
int tty_open(char *dev, int speed, char parity, int bits, int stopbits)
   {
   int s = -1 ;
   struct termios t ;

   s = open(dev,O_RDWR) ;
   if(s<0)
      return s ;

   tcgetattr(s,&t) ;

   /* raw  */
   t.c_lflag &= ~ICANON ;
   t.c_lflag &= ~(ECHO | ECHOCTL | ECHONL) ;
   t.c_lflag &= ~HUPCL ;

   t.c_oflag &= ~ONLCR ;
   t.c_iflag &= ~ICRNL ;

   t.c_iflag &= ~BRKINT ;
   t.c_iflag |= IGNBRK ;
   t.c_lflag &= ~ISIG ;
   t.c_cflag &= ~CRTSCTS ;
   t.c_iflag &= ~(IXON | IXOFF | IXANY) ;

   /* baud */
   cfsetospeed(&t,tty_speed(speed)) ;
   cfsetispeed(&t,tty_speed(speed)) ;

   /* parity */
   switch(parity)
      {
      case 'N':
      case 'n': 
         t.c_cflag &= ~PARENB ;
         break ;
      case 'O':
      case 'o':
         t.c_cflag |= PARENB ;
         t.c_cflag |= PARODD ;
         break ;
      case 'E':
      case 'e':
         t.c_cflag |= PARENB ;
         t.c_cflag &= ~PARODD ;
         break ;
      default :
         t.c_cflag &= ~PARENB ;
         break ;
      }
   
   /* data */ 
   t.c_cflag &= ~CSIZE ; 
   switch(bits)
      {
      case 5 : t.c_cflag |= CS5 ; break ;
      case 6 : t.c_cflag |= CS6 ; break ;
      case 7 : t.c_cflag |= CS7 ; break ;
      case 8 : t.c_cflag |= CS8 ; break ;
      default: t.c_cflag |= CS8 ; break ;
      }

   /* stop */
   switch(stopbits)
      {
      case 1 : t.c_cflag &= ~CSTOPB ; break ;
      case 2 : t.c_cflag |=  CSTOPB ; break ;
      default: t.c_cflag &= ~CSTOPB ; break ;
      }

   tcsetattr(s,TCSANOW,&t) ;

   return s ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int tty_open(char *dev, int speed, char parity, int bits, int stopbits) ;

@ Speed. We translate a numeric speed into a bitmap.
@<Functions@>+=
speed_t tty_speed(int baud)
   {
   if (baud >= 460800) return B460800 ;
   if (baud >= 230400) return B230400 ;
   if (baud >= 115200) return B115200 ;
   if (baud >=  57600) return  B57600 ;
   if (baud >=  38400) return  B38400 ;
   if (baud >=  19200) return  B19200 ;
   if (baud >=   9600) return   B9600 ;
   if (baud >=   4800) return   B4800 ;
   if (baud >=   2400) return   B2400 ;
   if (baud >=   1800) return   B1800 ;
   if (baud >=   1200) return   B1200 ;
   if (baud >=    600) return    B600 ;
   if (baud >=    300) return    B300 ;
   if (baud >=    200) return    B200 ;
   if (baud >=    150) return    B150 ;
   if (baud >=    134) return    B134 ;
   if (baud >=    110) return    B110 ;
   if (baud >=     75) return     B75 ;
                       return     B50 ;
   }

@ Prototype.
@<Prototypes@>+=
speed_t tty_speed(int baud) ;

@ Test Stub.  A test stub program |tty_open| is also created.
@(tty_open.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tty.h> 
#include <unistd.h>
int
main(int argc, char *argv[]) 
   {
   int ret ;

   if(argc!=6)
      {
      fprintf(stderr,"usage: %s dev speed parity bits stopbits\n",argv[0]) ;
      exit(1) ;
      }
  
   ret = tty_open(argv[1],atoi(argv[2]),argv[3][0],atoi(argv[4]),atoi(argv[5])) ;

   printf("tty_open(%s,%d,%c,%d,%d) = %d\n",
          argv[1],
          atoi(argv[2]),
          argv[3][0],
          atoi(argv[4]),
          atoi(argv[5]),
          ret) ;

   if(ret>=0)
      close(ret) ;
   }

@ Test Stub.  A test stub program |tty_ping| is also created.
@(tty_ping.c@>=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/select.h>
#include <rds_tty.h> 
int
main(int argc, char *argv[]) 
   {
   int tty ;
   fd_set rfds ;
   struct timeval tv ;
   int n ;
   char c ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s dev\n",argv[0]) ;
      exit(1) ;
      }
  
   tty = tty_open(argv[1],9600,'N',8,1) ;

   if(tty<0)
      {
      fprintf(stderr,"failed to open %s\n",argv[1]) ;
      exit(2) ;
      }

   while(1) 
      {
      FD_ZERO(&rfds) ;
      FD_SET(tty, &rfds) ;
      tv.tv_sec = 1 ;
      tv.tv_usec = 0 ;
   
      n = select(32,&rfds,NULL,NULL,&tv) ;
      
      if(n<0)
         {
         fprintf(stderr,"\nError on select\n") ;
         exit(3) ;
         }
      if(n==0)
         {
         write(tty,"PING ",5) ;
         write(tty,argv[1],strlen(argv[1])) ;
         write(tty,"\r\n",2) ;
         }
      if(n>0)
         {
         if(read(tty,&c,1)==1)
            {
            printf("%c",c) ;
            fflush(stdout) ;
            }
         } 
      }
   }

@*Index.
