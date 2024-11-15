% 
%   rds_dsd.web -- compatibility wrapper for tags 
%
%   Author: Mark Woodworth 
%
%   History:
%      07/14/00 -- check in (mrw)
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
\def\title{RDS-3 DSD -- Compatibility Library for Tags}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

This library implements the older DSD calls using the newer Tag library.

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
\centerline{RCS ID: $ $Id: rds_dsd.web,v 1.1 2003/06/26 12:26:13 rds Exp rds $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2000 Numina Systems Corporation.  
All Rights Reserved.}
}

@* DSD: compatibility wrapper around the fast string database. 

@ Exported Functions.  This library exports the following functions:
{\narrower\narrower
\dot {\bf dsd\_put(group,name,value)} 
\dot {\bf dsd\_get(group,name,value)}
\dot {\bf dsd\_drop(group,name,value)}
\dot {\bf dsd\_add(group,name,pcount)}
\dot {\bg dsd\_gdrop(group)}
\par
}

@* Overview.
This is a shared-object library.
Only the exported functions are visible to other applications.
@c
@<Defines@>@;
@<Includes@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <rds_tag.h> 

@ Header.  We put the prototypes in an external header.
@(rds_dsd.h@>=
   @<Exported Prototypes@> @;

@ Separator.
We separate the group and name with a single character.
@<Defines@>+=
#define SEP "/"

@ Stem.
We prefix each entry with a stem.
@<Defines@>+=
#define STEM "/"

@ DSD Put. 
@<Exported Functions@>+=
int dsd_put(char *group, char *name, char *value) 
   {
   int err ;
   char key[128+1] ;

   sprintf(key,"%s%s%s%s",STEM,group,SEP,name) ;
   err = tag_insert(key,value) ;

   return err ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int dsd_put(char *group, char *name, char *value) ;

@ A stub program calls the function.
@(dsd_put.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_dsd.h>
main(int argc, char *argv[])
   {
   int err ;
   if(argc!=4)
      {
      fprintf(stderr,"usage: %s <group> <name> <value>\n",argv[0]) ;
      exit(1) ; 
      }
   err = dsd_put(argv[1],argv[2],argv[3]) ;
   printf("dsd_put(%s,%s,%s)=%d\n",argv[1],argv[2],argv[3],err) ;
   }

@ DSD Get. 
@<Exported Functions@>+=
int dsd_get(char *group, char *name, char *value) 
   {
   char key[128+1] ;
   char *answer ;

   sprintf(key,"%s%s%s%s",STEM,group,SEP,name) ;
   answer = tag_value(key) ;
   if(!answer)
      return -1 ;

   strncpy(value,answer,32) ;
   value[32] = '\0' ;
   free(answer) ;

   return 0 ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int dsd_get(char *group, char *name, char *value) ;

@ A stub program calls the function.
@(dsd_get.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_dsd.h>
main(int argc, char *argv[])
   {
   int err ;
   char value[32+1] ;

   if(argc!=3)
      {
      fprintf(stderr,"usage: %s <group> <name> \n",argv[0]) ;
      exit(1) ; 
      }
   value[0] = '\0' ;
   err = dsd_get(argv[1],argv[2],value) ;
   printf("dsd_get(%s,%s,%s)=%d\n", argv[1], argv[2], value,err) ;
   }

@ DSD Drop. 
@<Exported Functions@>+=
int dsd_drop(char *group, char *name) 
   {
   int err ;
   char key[128+1] ;

   sprintf(key,"%s%s%s%s",STEM,group,SEP,name) ;
   err = tag_delete(key) ;

   return err ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int dsd_drop(char *group, char *name) ;

@ A stub program calls the function.
@(dsd_drop.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_dsd.h>
main(int argc, char *argv[])
   {
   int err ;
   if(argc!=3)
      {
      fprintf(stderr,"usage: %s <group> <name>\n",argv[0]) ;
      exit(1) ; 
      }
   err = dsd_drop(argv[1],argv[2]) ;
   printf("dsd_put(%s,%s)=%d\n",argv[1],argv[2],err) ;
   }

@ DSD Add. 
@<Exported Functions@>+=
int dsd_add(char *group, char *name, int *delta) 
   {
   int err ;
   char key[128+1] ;
   char *value ; 
   int sum ;
   char newvalue[32+1] ;

   sprintf(key,"%s%s%s%s",STEM,group,SEP,name) ;
   value = tag_value(key) ;
   if(value)
      {
      sum = atoi(value) ;
      free(value) ;
      }
   else
      sum = 0 ;
    
   *delta = sum + *delta ;
   sprintf(newvalue,"%d",*delta) ;

   err = tag_insert(key,newvalue) ;     

   return err ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int dsd_add(char *group, char *name, int *delta) ;

@ A stub program calls the function.
@(dsd_add.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_dsd.h>
main(int argc, char *argv[])
   {
   int err ;
   int delta ;

   if(argc!=4)
      {
      fprintf(stderr,"usage: %s <group> <name> <delta>\n",argv[0]) ;
      exit(1) ; 
      }
   delta = atoi(argv[3]) ;

   err = dsd_add(argv[1],argv[2],&delta) ;
   printf("dsd_add(%s,%s,%d)=%d now %d\n",argv[1],argv[2],atoi(argv[3]),err,delta) ;
   }

@ DSD Drop Group. 
@<Exported Functions@>+=
int dsd_gdrop(char *group) 
   {
   int err ;
   char stem[128+1]  ;

   sprintf(stem,"%s%s%s",STEM,group,SEP) ;
   err = tag_iterate(stem,tag_delete) ;

   return err ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int dsd_gdrop(char *group) ;

@ A stub program calls the function.
@(dsd_gdrop.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_dsd.h>
main(int argc, char *argv[])
   {
   int err ;
   int delta ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s <group>\n",argv[0]) ;
      exit(1) ; 
      }

   err = dsd_gdrop(argv[1]) ;
   printf("dsd_gdrop(%s)=%d\n",argv[1],err) ;
   }
@*Index.
