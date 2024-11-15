%   traktrace.web
%
%   Author: Mark Olson & Mark Woodworth
%           
%           
%
%   History:
%      8/15/2000 --- Initial layout by Mark Olson
%
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
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
%
%
%
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}
\def\title{Track Core Structures --- traktrace standardized tracing program}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This is a program which interacts with Trak by polling for messages. It also 
checks the status of dp 0 (the engine dp) and traces when the engine stops 
and starts.

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
\bigskip
\centerline{Author: Mark Olson}

\centerline{Revision Date: \today}

\centerline{RCS Date $ $Date: 2011/09/19 20:44:48 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1993 Numina Systems Corporation.  
All Rights Reserved.}
}
\def\dot{\qquad\item{$\bullet$}}
@* Introduction. This program is intended to be customized for each job. 
Copy the source to the app directory and append cases as required. The 
cases already included are those imbedded in the standard PERL modules.
Any ones added to the application code (messages) should be added here
as well so that it can trace where required.
@* Program Body.
@c


#include <stdio.h>
#include <stdlib.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <dlfcn.h>
@<Functions@>@;
typedef int (*handler)(Mb_Result *msg) ;

int main(int argc, char **argv)
{
  int was,is ;
  void* dl_fd ;
  Mb_Result *data ;
  handler h ;
  int box,mtr_out ;
  char name[TRAK_NAME_LEN+1],
       desc[TRAK_DATA_LEN+1],
       device[TRAK_NAME_LEN+1],
       io[TRAK_DATA_LEN+1] ;
  trn_register("trak") ;
  Trace("Init") ;
  
  dl_fd = dlopen("libtraktrace.so",RTLD_LAZY) ;
  if (dl_fd != NULL) {
    h = (handler) dlsym(dl_fd,"trakhandler") ;
  }
  else {
    h = NULL ;
  }

  was = dp_get(0) ;
  for (;;usleep(100 * 1000)) {
    is = dp_get(0) ;
    if (was && !is) Trace("Trak Engine Stopped (count = %d)",
				 dp_counter(0)) ;
    if (!was && is) Trace("Trak Engine Started (count = %d) ",
				 dp_counter(0)) ;
    
    was = is ;

    if (!is) 
      continue ;


    while ((data = mb_poll()) != NULL) {
      if (h!= NULL)  
        {
	if (h(data)) { 
          mb_free(data) ;
          continue ;
          }
        }

      switch(data->data[0]) {
       case 0:@; break ;
       @<Trace Cases@>@;
       default:@;
	@<Display not handled cases@>@;
	break ;
      }
      mb_free(data) ;
    }
  }
}
@
@<Display not handled cases@>=
{
}

@* Cases. Each message type gets an entry here. We then decode and trace
messages as appropriate.
@<Trace Cases@>=
case 200:@;
{
  if (data->count == 6) {
  char mtr[40],aux[40] ;
  strcpy(mtr,dp_name(data->data[1])) ;
  strcpy(aux,dp_name(data->data[4])) ;
  Trace("Motor fault: %s out(%s) %d  aux(%s) %d",mtr,
	dp_name(data->data[2]),
	data->data[3],
	aux,
	data->data[5]) ;
  }
  else {
    Trace("motor fault message, bad parameter count! %d not 6",data->count) ;
  }
	
  break ;
}


@ Box creation
@<Trace Cases@>=
case 300:@;
{
  char eye[40],tach[40] ;
  strcpy(eye,dp_name(data->data[1])) ;
  strcpy(tach,dp_name(data->data[3])) ;
  if (data->count == 5) {
    Trace("%03d: box created at: %s tach: %s = %lu",
	  data->data[2],
	  eye,
	  tach,
	  data->data[4]) ;
  }
  else {
    Trace("box create number parameters = %d not 5",data->count) ;
  }
  break ;
}

@ Box measure.
@<Trace Cases@>=
case 301:@;
{
  if (data->count == 4) {
    Trace("%03d: box measured at: %s length = %d",
	  data->data[2],
	  dp_name(data->data[1]),
	  data->data[3]) ;

  }
  else {
    Trace("box create number parameters = %d not 4",data->count) ;
  }
  break ;
}


@ box load.
@<Trace Cases@>=
case 303:@;
{
  if (data->count == 5) {
    char eye[40],tach[40] ;
    strcpy(eye,dp_name(data->data[1])) ;
    strcpy(tach,dp_name(data->data[3])) ;
    Trace("%03d: box loaded at: %s tach(%s) = %lu",
	  data->data[2],
	  eye,
	  tach,
	  data->data[4]) ;

  }
  else {
    Trace("box load number parameters = %d not 5",data->count) ;
  }
  break ;
}
@ box load.
@<Trace Cases@>=
case 305:@;
{
  if (data->count == 5) {
    char eye[40],tach[40] ;
    strcpy(eye,dp_name(data->data[1])) ;
    strcpy(tach,dp_name(data->data[3])) ;
    Trace("%03d: box unloaded at: %s tach(%s) = %lu",
	  data->data[2],
	  eye,
	  tach,
	  data->data[4]) ;

  }
  else {
    Trace("box unload number parameters = %d not 5",data->count) ;
  }
  break ;
}

@ report edge.
@<Trace Cases@>=
case 307:@;
{
  if (data->count == 6) {
    char rep[40] ;
    char tach[40] ;
    strcpy(rep,dp_name(data->data[1])) ;
    strcpy(tach,dp_name(data->data[4])) ;
    Trace("%03d: report %s-> %d  tach(%s) = %lu",
	  data->data[3],
	  rep,
	  data->data[2],
	  tach,
	  data->data[5]) ;

  }
  else {
    Trace("report number parameters = %d not 6",data->count) ;
  }
  break ;
}
@* Utilities.
@<Functions@>=
char *dp_name(int handle)
{
  static char name[TRAK_NAME_LEN] ;
  char data[TRAK_DATA_LEN],device[TRAK_DATA_LEN],desc[TRAK_DATA_LEN] ;
  name[0] = '\0' ;
  dp_settings(handle,name,desc,device,data) ;
  return name ;
}
@* Index.
