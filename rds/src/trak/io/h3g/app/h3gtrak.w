%
%   h3gtrak.w -- a user space trak driver for Hilmot 3G and Octo I/O
%
%   Author:  Mark Olson
%
%   History:
%      2009-09-01 - MDO initial version
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
%     (C) Copyright 2009 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- helpful macros ---
\def\dot{\item{ $\bullet$}}
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}

% --- title block ---
\def\title{3G and Octo IO}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program acts a trak user space driver for 3G and Octo UDP packets.

% --- confidentiality statement ---
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

% --- author and version ---
\bigskip
\centerline{Author: Mark Olson}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.39 $ $}
\centerline{Control Date: $ $Date: 2020/04/03 15:14:10 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2009 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program is a UDP driven state machine managing h3g cards for Trak.

@c
static char rcsid[] = "$Id: h3gtrak.w,v 1.39 2020/04/03 15:14:10 rds Exp rds $";
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

  while(1)
    h3g_task() ;
  }


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>    // for gethostname()
#include <signal.h>

#include <sys/time.h>  // for gettimeofday()
#include <sys/types.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_evt.h>

@ States.
@<Defines@>+=
enum 
h3g_states 
  {
  BLANK,
  ACTIVE
  } ;  

@ Types.
@<Defines@>+=
#ifndef BYTE
typedef unsigned char BYTE;
#endif

#ifndef WORD
typedef unsigned short WORD;
#endif

#ifndef DWORD
typedef unsigned int DWORD;
#endif

#pragma pack(1)


@ Structures.
@<Structures@>+=
typedef struct 
  {
  DWORD Cmd;
  BYTE Rsv1[ 26 ];

  WORD CmdZ;
  DWORD Rsv3;
  DWORD CmdU;

  DWORD Cmdx1;
  WORD Cmdx2;

  DWORD Rsv4[ 3 ];
  WORD vid;

  DWORD Rsv5;
  DWORD TimeoutMult ;
  DWORD Host_RPI;
  WORD Rsv6;

  DWORD Smart3G_RPI;
  WORD SiLen;
  WORD Rsv7;
  } CmdMsg;

typedef struct  
  {
  DWORD MsgSign;  /* 0x80020002 */
  BYTE Rsvd1[ 20 ];
  BYTE data[ 4 ]; /* Byte0:Inputs, Byte1:Output Enable Bits, Byte2:Outputs */
  WORD cw[ 2 ];   /* Ladder Control Words 9 and 10 */
  } InPacket ;

typedef struct 
  {
  DWORD MsgSign;  /* 0x80020002 */
  BYTE Rsvd1[ 16 ];
  BYTE data[ 4 ]; /* Byte0:Inputs, Byte1:Output Enable Bits, Byte2:Outputs */
  WORD cw[ 4 ];   /* Ladder Control Words 9 and 10 */
  WORD slaveIO[8] ;
  } NewInPacket ;

typedef struct 
  {
  BYTE Rsv[ 24 ];
  DWORD data; /* if VirtualIO is ON, byte0 maps to digital Outputs; */
  /* if VirtualIO is OFF, bytes 0..3 maps to Ladder Control Words 9 and 10 */
  } OutPacket ;

typedef struct 
  {
  BYTE Rsv[ 24 ];
  BYTE Outputs ;
  BYTE CW_9_Hi ;
  WORD CW_10 ;
  WORD CW_81_88[8] ;
  } NewOutPacket ;

typedef struct 
_dp_list 
  {
  struct _dp_list *link ;
  int handle ;
  Dp_Record *record ;
  int is_input ;
  int bit ;
  } dp_item ;

typedef struct 
_device 
  {
  struct _device *link ;
  struct timeval last_out ; 
  struct timeval last_in ;
  int state ;
  int touched ;
  int is_raw ; /* h3g mode raw or visual or control program running */
  struct sockaddr_in addr ;
  char *ip ;
  unsigned char in ;
  unsigned char out ;
  unsigned short cw[2] ;
  int intimeout ;
  int outtimeout ;
  unsigned int lastoutput ;
  dp_item *dp ;
  int dp_card ;
  } h3g_device ;

@ Communication handles.
@<Globals@>=
int sock = -1 ;
h3g_device *devices= NULL ;
h3g_device *current ;
int debug_on = 0 ;

@* Initialization.
@<Initialize@>+=
  {
  if (argc > 2) 
    {
    printf( "usage: %s\n", argv[0] );
    exit( EXIT_SUCCESS );
    }

  if (argc == 2) 
    debug_on = 1 ;

  trn_register( "h3gtrak" );
  Trace( "init" );
  Inform( rcsid );
  }

@ Init.
@<Functions@>+=
void
h3g_init(void)
  {
  h3g_stop() ;
  @<Init Socket@>@;
  @<Init Driver@>@;
  }

@ Proto.
@<Prototypes@>+=
void h3g_init(void) ;

@ Stop.
@<Functions@>+=
void h3g_stop(void)
  {
  h3g_device *dev ;

  alarm(0) ;

  if(sock>=0)
    {
    close(sock) ;
    sock = -1 ;
    }

  dev = devices ;
  while(dev != NULL)
    {
    h3g_device *victim = dev ;
    dp_item *dp = dev->dp ;

    while(dp!=NULL)
      {
      dp_item *fated ;
      fated = dp ;
      dp = dp->link ;
      free(fated) ;  
      } 
    dev = dev->link ;
    free(victim) ;
    }
  devices = NULL ;
  current = NULL ;
  }

@ Proto.
@<Prototypes@>+=
void h3g_stop(void) ;
@ Initialize socket.
@<Init Socket@>=
  {
  struct sockaddr_in bindaddr ;
  int i;
  struct timeval tm ;

  tm.tv_sec = TIMEOUT ;
  tm.tv_usec = 0 ;
  sock = socket(AF_INET,SOCK_DGRAM,0) ;
  if (sock < 0) 
    {
    Alert("canot locate socket no network?") ;
    exit(0) ;
    }

  i = 1;
  setsockopt( sock, SOL_SOCKET, SO_REUSEADDR, &i, sizeof(int) );
  setsockopt( sock, SOL_SOCKET, SO_RCVTIMEO, &tm, sizeof(struct timeval) );

  bzero(&bindaddr,sizeof(struct sockaddr_in)) ;
  bindaddr.sin_family=AF_INET ;
  bindaddr.sin_port = htons(2222) ;
 
  if (bind(sock,(struct sockaddr*) &bindaddr,sizeof(bindaddr)) < 0) 
    {
    perror("bind: ") ;
    Alert("bind (2222) failed") ;
    exit(0) ;
    } 

  signal(SIGALRM,alarm_handler) ;
  Trace("socket created and bound to 2222") ; 
  }

@ Alarm handler.
@<Functions@>+=
void 
alarm_handler(int unused)
  {
  Trace("got alarm") ;
  signal(SIGALRM,alarm_handler) ;
  }

@ Proto.
@<Prototypes@>+=
void alarm_handler(int unused) ;

@ Initialized trak and form local structures for devices, drivers and
state machines (trak objects).
@<Init Driver@>=
  {
  dp_devices("io:h3g",h3g_each_device) ;
  @<Setup All@>@;
  }

@ Each point.
@<Functions@>=
int 
h3g_each_point(Dp_Record *dpr)
  {
  dp_item *next ;

  next = (dp_item *) malloc(sizeof(dp_item)) ; 
  bzero(next,sizeof(dp_item)) ;
  next->link = current->dp ;
  current->dp = next ;
 
  next->handle = dp_record(dpr)  ;
  next->record = dpr ;
  next->is_input = (toupper(next->record->io_data[0]) == 'I') ;
  next->bit = atoi(next->record->io_data+1) ;
  Trace("  point [%s] %s bit %d data[%s]",
        next->record->name,
        next->is_input? "input" : "output",
        next->bit,
        next->record->io_data) ;
  }

@ Proto.
@<Prototypes@>+=
int h3g_each_point(Dp_Record *dpr) ;

@ The format of the io data and the rest is first a word 
followed by a colon separator
and an indication whether this is direct I/O or program driven 3g module.
@<Functions@>=
int 
h3g_each_device(int handle)
  {
  struct timeval tv ;
  struct timezone tz ;
  char *ptr ;
  Dp_Device dev ;
  char card_dp_name[80] ;

  dp_readdevice(handle,&dev) ;
  h3g_device *next = malloc(sizeof(h3g_device)) ;
  bzero(next,sizeof(h3g_device)) ;
  next->link = devices ;
  devices=next ;
  current = next ;

  gettimeofday(&tv,&tz) ;
  next->last_out = next->last_in = tv ;
  next->state = BLANK ;
  
  next->is_raw = (!strncmp("direct",dev.device_data,strlen("direct"))) ;
  for (ptr = dev.device_data ; !isdigit(*ptr) ; ptr++) ;
  next->ip = strdup(ptr) ;
  next->addr.sin_family = AF_INET ;
  next->addr.sin_addr.s_addr = inet_addr(next->ip) ;
  next->addr.sin_port = htons(2222) ;  
 
  next->intimeout = (dev.supplemental[0]) ? dev.supplemental[0] : 10 ;
  next->outtimeout = (dev.supplemental[1]) ? dev.supplemental[1] : 10 ;
  Trace("timeout in %d out %d",next->intimeout, next->outtimeout) ;

  /* convert miliseconds to microseconds */
  next->intimeout *= 1000 * 2 ;
  next->outtimeout *= 1000 ;
  Trace("device [%s] raw: %d addr %s",
        dev.name,next->is_raw,next->ip) ; 
  dp_ioscan(handle,h3g_each_point) ;
  sprintf(card_dp_name,"%s_comm",dev.name) ;
  next->dp_card = dp_handle(card_dp_name) ;

  Inform("d->last %d %d",
         next->last_in.tv_sec,
         next->last_in.tv_usec) ;

  }

@ Proto.
@<Prototypes@>+=
int h3g_each_device(int handle) ;

@ Timeout.
@<Defines@>+=
#define TIMEOUT (10)

@ Task
@<Functions@>+=
void
h3g_task(void)
  {
  @<Check for run/stop@>@;
  if(run)
    {
    h3g_scan() ;
    }
  }

@ Proto.
@<Prototypes@>+=
void h3g_task(void) ;

@ Run/Stop.  We keep a run flag.
@<Globals@>+=
int run = 0 ;

@ If trak goes to run, we initialize.
@<Check for run/stop@>+=
  {
  if((!run) && (trak_test()))
    {
    h3g_device *d ; 
    run = 1 ;
    h3g_init() ;
    Inform("scan started") ;
#if 0
    for (d = devices ; d != NULL ; d = d->link) 
          Inform("[%s] d->last %d %d",
             d->ip,
              d->last_in.tv_sec,
              d->last_in.tv_usec) ;
#endif
    }
  if((run) && (!trak_test()))
    {
    run = 0 ;
    h3g_stop() ;
    Inform("scan stopped") ;
    }
  }
 
@ Scan.
@<Functions@>+=
void
h3g_scan(void)
  {
  struct sockaddr_in in_addr ;
  int rlen,len ;
  unsigned char input[sizeof(InPacket) + 80] ;
  BYTE odata  ;
  BYTE idata  ;
  DWORD MsgSign ;

  len = sizeof(in_addr) ;
  bzero(&in_addr,len) ;
  alarm(TIMEOUT) ;
  if (debug_on) 
    Trace("block for read") ;
  rlen = recvfrom(sock,
                  input,
                  sizeof(NewInPacket)+80,
                  0,
                  (struct sockaddr*)&in_addr, 
                  &len) ;
  if (rlen == sizeof(NewInPacket)) 
    {
    NewInPacket *inData ;
    inData = (NewInPacket*) input ; 
    MsgSign = inData->MsgSign ;
    odata = inData->data[2] ;
    idata = inData->data[0] ;
    } 
  else 
    {
    InPacket *inData ;
    inData = (InPacket*) input ; 
    MsgSign = inData->MsgSign ;
    odata = inData->data[2] ;
    idata = inData->data[0] ;
    }
  
  if (debug_on) 
    {
    Trace("read unblocked %d %d %d",
          rlen,sizeof(InPacket),sizeof(NewInPacket)) ;
    Inform("1: %x %x %x %x %x %x %x %x %x",
           input[0],input[1],input[2],input[3],input[4],
           input[5],input[6],input[7],input[8]) ;
    Inform("2: %x %x %x %x %x %x %x %x %x",
           input[9],input[10],input[11],input[12],input[13],
           input[14],input[15],input[16],input[17]) ;
    }


  if (rlen <=0) 
    {
    Trace("alarm fired") ;
    @<Catastrophic timeout@>@;
    } 

  @<process states@>@;
  }

@ Proto.
@<Prototypes@>+=
void h3g_scan(void) ;

@ Setup.
@<Setup All@>=
  {
  struct timeval now ;
  struct timezone unused ;
  h3g_device *d ;

  gettimeofday(&now,&unused) ;
  Trace("setup all devices") ;
  for (d = devices ; d != NULL ; d = d->link) 
    {
    send_configure(d) ;
    d->state = ACTIVE ;
    }
  }


@ Process.
@<process states@>=
  {
  struct timeval now ;
  struct timezone unused ;
  h3g_device *d ; 
  char evt_code[32] ;

  gettimeofday(&now,&unused) ;
  if (debug_on) 
    Trace("data back from %s %d %d",inet_ntoa(in_addr.sin_addr),
          now.tv_sec%100,now.tv_usec/1000) ;
  for (d = devices ; d != NULL ; d = d->link) 
    {
    if (d->state == BLANK) 
      {
      @<Configure Card@>@;
      d->touched = 0 ;
      dp_set(d->dp_card,0) ; 
      continue ;
      } 
    if (d->addr.sin_addr.s_addr == in_addr.sin_addr.s_addr) 
      {
      if (debug_on)
        Inform("address match") ;
      if (MsgSign != 0x80020002 &&
          MsgSign != 0x0056006F) 
        {
        Trace("unexpected packet sign %x",MsgSign) ;
        } 
      else 
        {
        if (debug_on) 
          Trace("process inputs from %s touch: %d",d->ip, d->touched ) ;
        d->last_in = now ;
        @<process inputs@>@;
        @<outputs to device@>@;
        d->state = ACTIVE ;
        dp_set(d->dp_card,1) ; 
        if (!d->touched)  
          {
          Trace("%s: restored comm with module %ld",
                d->ip,age(d->last_in,now)) ;
          }
        d->touched++ ;
        }
      } 
    else 
      {
 //     Inform("now = %d %d age: %d timeout %d",
//       now.tv_sec,now.tv_usec,
 //        age(d->last_in,now),
  //       d->intimeout) ;
      if(age(d->last_in,now) > d->intimeout * 10)  
        {
          
          Alert("%s: lost comm with module %ld",
                d->ip,age(d->last_in,now)) ;
#if 0
          Inform("d->last %d %d",
              d->last_in.tv_sec,
              d->last_in.tv_usec) ;
          Inform("now %d %d",
              now.tv_sec,
              now.tv_usec) ;
#endif
          sprintf(evt_code, "3g%s",d->ip) ;
          dp_set(d->dp_card,0) ; 
          d->state = BLANK ;
          d->touched = 0 ;
         
        continue ;
        }  
      }
    }    
  }

@ Loss of communication.
@<Catastrophic timeout@>=
  {
  struct timeval now ;
  struct timezone unused ;
  h3g_device *d ; 

  Alert("comm lost") ;
  gettimeofday(&now,&unused) ;
  for (d = devices ; d != NULL ; d = d->link) 
    {
    send_configure(d) ;
    d->state = ACTIVE ;
    d->last_out = d->last_in = now ;
    }
  }

@ Configure.
@<Configure Card@>=
  {
  Inform("configure %s",d->ip) ;
  send_configure(d) ;
  d->state = ACTIVE ;
  d->last_out = d->last_in = now ;
  }

@ Outputs.
@<outputs to device@>=
  {
  dp_item *dp ;
  unsigned int current = 0 ;
  
  for (dp = d->dp ; dp != NULL ; dp = dp->link) 
    {
    if (dp->record->set_value) 
      {
      current |= 1 << dp->bit ;
      }
    }
  d->lastoutput = current ;
  send_output(&(d->addr),current,rlen) ;
  }

@ Inputs.
@<process inputs@>=
  {
  dp_item *dp ;
  int v ;

  if (d->is_raw) 
    {
    if (debug_on) 
      Inform("in data [%x] oData %x  delay",idata,odata) ;
    for (dp = d->dp ; dp != NULL ; dp = dp->link)  
      {
      v = (dp->is_input) ? idata : odata ;
      v >>= dp->bit ;
      v &= 1 ;
      dp->record->export = v ;
      }
    } 
  }

@* Helper Functions.
@<Functions@>+=
long 
age(struct timeval a, struct timeval b)
  {
  long diff ;

  diff = a.tv_sec - b.tv_sec ;
  diff *= 1000000L ;
  diff += a.tv_usec - b.tv_usec ;
  return labs(diff) ;
  }

@ Proto.
@<Prototypes@>+=
long age(struct timeval a, struct timeval b) ;

@ Send.
@<Functions@>+=
void 
send_output(struct sockaddr_in *addr, DWORD data,int input_len) 
  {
  if (input_len == sizeof(InPacket)) 
    {
    OutPacket outData ;
    int len ;
    bzero(&outData,sizeof(OutPacket)) ;
    outData.data = data ;
    if (debug_on) 
      Trace("output sent to %s %x",inet_ntoa(addr->sin_addr),data) ;
    len = sendto(sock,&outData,sizeof(OutPacket),0,(struct sockaddr*) addr,
                 sizeof(struct sockaddr_in)) ;
    if (len != sizeof(OutPacket)) 
      {
      Trace("output send failed") ;
      } 
    }  // always send *something* ... 
  else /* if (input_len == sizeof(NewInPacket))  */ 
      {
      NewOutPacket outData ;
      int len ;
      bzero(&outData,sizeof(NewOutPacket)) ;
      outData.Outputs = data ;
      if (debug_on) 
        Trace("output sent to %s %x",inet_ntoa(addr->sin_addr),data) ;
      len = sendto(sock,&outData,sizeof(OutPacket),0,(struct sockaddr*) addr,
                   sizeof(struct sockaddr_in)) ;
      if (len != sizeof(OutPacket)) 
        {
        Trace("output send failed") ;
        } 
      }
  }

@ Proto.
@<Prototypes@>+=
void send_output(struct sockaddr_in *addr, DWORD data,int input_len)  ;

@ Configure.
@<Functions@>+=
void 
send_configure(h3g_device *dev)
  {
  struct sockaddr_in addr ;
  CmdMsg msg ;
  int len ;

  bzero(&msg,sizeof(CmdMsg)) ;
  msg.Cmd = 0x004C006F;
  msg.CmdZ = 0x0002;
  msg.CmdU = 0x003C00B2;
  msg.Cmdx1 = 0x06200254;
  msg.Cmdx2 = 0x0124;
  msg.vid = 0x0380;
  msg.TimeoutMult = 5 ;
  msg.SiLen= 30 ;
  msg.Host_RPI = dev->intimeout * 3; /* timeout is 30 times longer */
  msg.Smart3G_RPI = dev->outtimeout  ;
 
  bzero(&addr,sizeof(struct sockaddr_in)) ;
  addr.sin_family = AF_INET ;
  addr.sin_addr.s_addr = inet_addr(dev->ip) ;
  addr.sin_port = htons(44818) ;

  if (debug_on)
    Trace("send configure to %s it: %d ot: %d",dev->ip,
          msg.Host_RPI,msg.Smart3G_RPI) ;
  len = sendto(sock,&msg,
               sizeof(CmdMsg),0,(struct sockaddr*) &addr,sizeof(addr)) ;
  if (len != sizeof(CmdMsg)) 
    {
    Trace("send to %s failed, %d",dev->ip,len) ;
    }
  }

@ Proto.
@<Prototypes@>+=
void send_configure(h3g_device *dev) ;

@* Index.

