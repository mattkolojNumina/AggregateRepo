%   rds_trak.web
%
%   Author: Mark Olson & Mark Woodworth
%           
%           
%
%   History:
%      8/16/2000 --- Initial layout by Mark Olson
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
\def\title{Track Core Structures --- Shared Library (user space)}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
The trak core modules are contained in monolithic object modules which are
inserted into the kernel. However, for user space programs a shared library
is built. This library currently exports all functions. Eventually, it 
will only export those appropriate to the user space programs.

This module exports only the functions used by user space programs. Internal
functions are kept as statics so as not to corrupt namespaces. Globals are
kept local to the module and not exported.
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

\centerline{RCS Date $ $Date: 2001/01/16 15:53:19 $ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1993 Numina Systems Corporation.  
All Rights Reserved.}
}
\def\dot{\qquad\item{$\bullet$}}

@* Introduction. This file builds the library files.

@(rds_trak.h@>=
@<Exported Defines@>@;
@<Exported Includes@>@;
@<Exported Structures@>@;
@<Exported Prototypes@>@;

@ and the library.
@(rds_trak.c@>=
@<Defines@>@;
@<Exported Defines@>@;
@<Exported Includes@>@;
@<Exported Structures@>@;
@<Structures@>@;
@<Master Structures@>@;
@<Exported Prototypes@>@;
@<Prototypes@>@;
@<Globals@>@;
@<Exported Functions@>@;
@<Functions@>;

@ We need some includes.
@<Exported Includes@>=
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <time.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/sem.h>

@ Constants.
@<Defines@>=
#define MAGIC (0xCAFEFACE)
#define TRAK_KEY (0x1040)
#define EOS ('\0')

@ All shared memory is placed into one pool structure.
@<Master Structures@>+=
typedef struct 
  {
  int magic ;
  int on_switch ;
  @<Trak Data@>@;
  } Trak_Pool ;

@ One new global.
@<Globals@>=
static Trak_Pool *pool ;

@ {\it Every} program must call |trak_init()|. Care should be taken 
in design so each entry point calls init as required.
@<Functions@>=
static void trak_init(void)
  {
  static int initialized = 0 ;
  if (initialized) return ;
  initialized = 1 ;

  pool = (Trak_Pool*) ipc_smemget(TRAK_KEY,sizeof(Trak_Pool)) ;
  
  if(pool->magic != MAGIC)   
    {
    bzero(pool,sizeof(Trak_Pool)) ;
    pool->magic = MAGIC ;
    pool->on_switch = 0 ;
    dp_init() ;
    rp_init() ;
    ex_init() ;
    ev_init() ;
    bx_init() ;
    mb_init() ;
    const_init() ;
    q_init() ;
    }
  else 
    {
    dp_attach() ;
    rp_attach() ;
    ex_attach() ;
    ev_attach() ;
    bx_attach() ;
    mb_attach() ;
    const_attach() ; 
    q_attach() ;
    }
  }

@ Internal prototypes. 
@<Prototypes@>=
static void trak_init(void) ;

@ Start, stop, test, and size. 
@<Exported Functions@>+=
void 
trak_start(void)
  {
  trak_init() ;
  pool->on_switch = 1 ;
  }

void 
trak_stop(void)
  {
  trak_init() ;
  pool->on_switch = 0 ;
  }

int 
trak_test(void)
  {
  trak_init() ;
  return pool->on_switch ;
  }

void* 
trak_ptr(void)
  {
  trak_init() ;
  return pool ;
  } 

int 
trak_size(void)
  {
  return sizeof(Trak_Pool) ;
  }

@ Proto. 
@<Exported Prototypes@>=
void trak_start(void) ;
void trak_stop(void) ;
int trak_test(void) ;
void* trak_ptr(void) ;
int trak_size(void) ;

@ Some constants. 
@<Exported Defines@>=
#define NIL 0xffffffff
#define TRAK_NAME_LEN 31
#define TRAK_DATA_LEN 63
#define TRAK_DRIVER_N 10
#define TRAK_DEVICE_N 500

@i trak_ipc.i
@i trak_const.i
@i trak_dp.i
@i trak_rp.i
@i trak_mb.i
@i trak_q.i
@i trak_bx.i
@i trak_ex.i
@i trak_ev.i

@* Index.
