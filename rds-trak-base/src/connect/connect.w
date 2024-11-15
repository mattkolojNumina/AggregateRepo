% 
%   connect.w -- connect outputs to inputs through the trak table 
%
%   Author: Mark Woodworth 
%
%   History:
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
%     (C) Copyright 2024 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{Connect}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Connect outputs to inputs through the trak table.

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
\centerline{RCS ID: $ $Id: connect.w,v 1.7 2024/03/08 21:53:13 rds Exp $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2024 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: connect.w,v 1.7 2024/03/08 21:53:13 rds Exp $" ;
@<Defines@>@;
@<Includes@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int
main(int argc, char *argv[])
  {
  @<Initialize@>@;
 
  @<Configure@>@;
 
  while(1)
    {
    crank() ;
    usleep(DELAY) ;
    }
  }

@ Includes.
@<Includes@>+=
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <rds_trn.h>
#include <rds_sql.h>

@ Initialize.
@<Initialize@>+=
  {
  trn_register("connect") ;
  }

@ Structures. 
@<Structures@>+=
struct dp_struct
  {
  char host[32+1] ;
  char name[32+1] ;
  int  value ;
  } ;

struct connect_struct
  {
  struct dp_struct input ;
  struct dp_struct outputs[MAX_OUTPUTS] ;
  int n_outputs ;
  } ;

@ Globals.
@<Globals@>+=
struct connect_struct connects[MAX_CONNECTS] ;
int current = 0 ;
int n_connects = 0 ;

@ Limits.
@<Defines@>+=
#define MAX_OUTPUTS  (20)
#define MAX_CONNECTS (20) 
#define DELAY (1000000L) 

@* Crank.
@<Functions@>+=
void
crank(void)
  {
  int c ;

  for(c=0 ; c<n_connects ; c++)
    {
    int err ;
    int value ;

    value = -1 ;
    err = sql_query("SELECT `get` FROM trak "
                    "WHERE host='%s' "
                    "AND zone='dp' "
                    "AND name='%s' ",
                    connects[c].input.host,connects[c].input.name) ;
    if(!err)
      {
      if(sql_rowcount()>0)
        if(sql_get(0,0))
          value = atoi(sql_get(0,0)) ;
      }
    else
      Alert("SQL error %d select trak",err) ;
    
    if(value>=0)
      if(value!=connects[c].input.value)
        {
        int o ;
        for(o=0 ; o<connects[c].n_outputs ; o++)
          {
          Inform("write %d from %s to %s",
                 value,
                 connects[c].input.name,
                 connects[c].outputs[o].name) ;
          err = sql_query("UPDATE trak "
                          "SET put=%d, "
                          "state='write' "
                          "WHERE host='%s' "
                          "AND zone='dp' "
                          "AND name='%s' ",
                          value,
                          connects[c].outputs[o].host,
                          connects[c].outputs[o].name) ;
          if(err)
            Alert("SQL error %d update trak",err) ;       
          }
        connects[c].input.value = value ;
        }
    } 
  }

@ Proto.
@<Prototypes@>+=
void crank(void) ;

@* Configuration.

@ Add Input.
@<Functions@>+=
int
add_input(char *host, char *name)
  {
  current = n_connects ;

  strcpy(connects[current].input.host,host) ;
  strcpy(connects[current].input.name,name) ;
  connects[current].input.value = -1 ;

  if(n_connects+2<MAX_CONNECTS)
    n_connects++ ;

  return current ;
  }

@ Proto.
@<Prototypes@>+=
int add_input(char *host, char *name) ;

@ Add output.
@<Functions@>+=
int
add_output(char *host, char *name) 
  {
  int o = connects[current].n_outputs ;

  strcpy(connects[current].outputs[o].host,host) ;
  strcpy(connects[current].outputs[o].name,name) ;

  if(o+2<MAX_OUTPUTS)
    connects[current].n_outputs++ ;
  }

@ Config.
@<Configure@>+=
  {
  add_input ("org-trk5","cr-62102"  ) ; 
  add_output("org-trk4","cp4_fire") ; 
  add_output("org-trk3","cp3_fire") ; 
  add_output("org-trk2","cp2_fire") ; 
  add_output("org-trk1","cp1_fire") ;
  }

@* Index.

