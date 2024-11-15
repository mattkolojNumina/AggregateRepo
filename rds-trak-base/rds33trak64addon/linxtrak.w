% 
%   linxtrak.w -- do EtherNET/IP protocol with Itoh-Denki IB-E01 controllers
%
%   Author: Mark Woodworth 
%   Author: Mark Olson (continued) 
%
%   History:
%      2013-04-18 -- check in (mrw) 
%      2013-04-22 -- debug -> go live (mdo) 
%      2013-09-09 -- modified from ERSC to P8IO (ahm)
%      2016-05-05 -- modified from P8IO to MOXA/IDLinx (mdo)
%      2018-08-19 -- modified to talk Rockwell's horrific protocol
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
%     (C) Copyright 2016 Numina Group, Inc.  All Rights Reserved.
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
\def\title{Linx Trak}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Talk to IdLinx EB-01 controller cards. These have two motors 
two sensors, 3 general purpose inputs and 5 general purpose outputs.

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
\centerline{Authors: Mark Woodworth \& Mark Olson}
\centerline{RCS ID: $ $Id: linxtrak.w,v 1.40 2022/11/07 20:44:09 rds Exp rds $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2018 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview.

Basic structure of this program is that we open a session, setup a forwarding 
packet which defines our packet rate. At the packet rate we are
essentially contracted then to send UDP packets to the card at that rate
and will receive them at the same rate. 

So, lots of processes get launched here. First off, atfter initialization
(so we all work from the same data set), we fork off a process to 
handle incoming UDP datagrams. The incoming IP is used to index to the
card structure to handle the data. Reporting on packet rates is done 
once every 100 seconds and one half of the connection loss logic is in 
this process.

Then for each card, we fork another process. This process sets up the 
connection, holds open the TCP port, and streams UDP packets to the card
at the contracted rate. Once a second connection loss is detected.

We detect connection loss by means of identifying that we are no longer
receiving packets from that card. When a connection is lost, 
sockets are closed, the loss is noted by an event, and after a short time  
connection is attempted to be re-established.

The main thread doesn't do anything else, but just hangs around to make sure
child processes are not terminated.

@c
static char rcsid[] = "$Id: linxtrak.w,v 1.40 2022/11/07 20:44:09 rds Exp rds $" ;
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
    {
    if((!run) && (trak_test()))
      {
      run = 1 ;
      linx_init() ;
      }
    if((run) && (!trak_test()))
      {
      run = 0 ;
      linx_stop(0) ;
      }
    usleep(100000) ;
    }

  return 0 ;
  }

@ Run.
@<Globals@>+=
int run = 0 ;

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <limits.h>
#include <pthread.h>

#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <signal.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/tcp.h>

#include <rds_net.h>
#include <rds_trak.h>
#include <rds_trn.h>
#include <rds_evt.h>
#include <rds_tag.h>

@ Defines.
@<Defines@>+=
#define INTERCHANGE_PORT  2222
#define ETHERNETIP_PORT   44818
#define MSG_SIZE          2076
#define POLLRATE          10

@ UDP socket.
@<Globals@>+=
int udp = -1 ;

@ Linx Server.
@<Functions@>+=
void *
linx_server(void *vargp)
  {
  @<UDP server (handles all inputs)@>@;  
  }

@ Proto.
@<Prototypes@>+=
void *linx_server(void *vargp) ;

@ Linx client.
@<Functions@>+=
void *
linx_client(void *vargp)
  {
  idlinx_struct *mycard = (idlinx_struct *)vargp ;
  Inform("client %d",mycard->octet[3]) ;
  @<process one card@>@;
  }

@ Proto.
@>Prototypes@>+=
void *linx_client(void *vargp) ;

@ Linx init.
@<Functions@>+=
void
linx_init(void) 
  {
  int pid ;

  // clear cards
  bzero(cards,sizeof(idlinx_struct) * N_CARDS) ;
  
  // traverse devices 
  dp_devices("io:linx",idcomm_each_device) ;

  sleep(1) ;

  // start server thread
    {
    pthread_t thread_id ;
    pthread_create(&thread_id,NULL,linx_server,NULL) ;
    }

  // start client threads
    {
    int i ; 
    pthread_t thread_id ;

    for (i=0 ; i < N_CARDS ; i++) 
      { 
      idlinx_struct *mycard = cards + i ;

      if (mycard->octet[0]==0) 
        continue ;
      pthread_create(&thread_id,NULL,linx_client,(void *)mycard) ;
      } 
    }
  }

@ Proto.
@<Prototypes@>+=
void linx_init(void) ;

@ Stop.
@<Functions@>+=
void
linx_stop(int term)
  {
  }

@ Proto.
@<Prototypes@>+=
void linx_stop(int term) ;

@ Child
@<Functions@>+=
void
linx_child(int sig)
  {
  }

@ Proto.
@<Prototypes@>+=
void linx_child(int pid) ;

@ Initialize. Intially limited to one octet of an IP address (256 cards).
Here we register for tracing, parse our host IP to an array of four ints
from string. Setup an array of "cards" for handling incoming data and
locate all the DPs that our I/O services.
@<Initialize@>+=
  {
  char name[32] ;
  char tmp[80] ;
  int handle ;

  if (argc != 2) 
    {
    fprintf(stderr,"usage: idcomm host_ip\n") ;
    exit(0) ;
    }

  sprintf(name,"linxtrak") ;
  trn_register(name) ;

  parse_octet(argv[1],host_octet) ;

  Trace("%s ip %d.%d.%d.%d",rcsid,
        host_octet[0],
        host_octet[1],
        host_octet[2],
        host_octet[3]) ;
  lnxnet_setup() ;
  }

@* Data structures.   

@ Inputs.
@<Structures@>+=
typedef struct 
_input_struct
  {
  int type ;
  int reg ;
  int bit ;
  int active ;
  int match ;
  unsigned int value ;
  int handle ;
  Dp_Record *record ;
  struct _input_struct *link ;
  } input_struct ;

@ Output. Link an DP to a register and masks in an ERSC.
@<Structures@>+=
typedef struct 
_output_struct
  {
  int handle ;
  Dp_Record *record ;
  unsigned int value ;
  int reg ;
  int active_and_mask ;
  int active_or_mask ;
  int inactive_and_mask ;
  int inactive_or_mask ;
  int is_motor_output ;
  int is_a ;
  struct _output_struct *link ;
  } output_struct ;

@ idcommDp.
@<Structures@>+= 
typedef struct 
_idcommDp 
  {
  struct _idDp *link ;
  int handle ;
  int is ;
  int was ;
  } idcommDp ;

@ idlinx.
@<Structures@>+=
typedef struct 
_idlinx_struct 
  {
  char name[TRAK_NAME_LEN+1] ;
  char *ip ;
  int octet[4] ; // IP address is 4 "octets".  
  int pid ;
  unsigned char session[4] ; // for protocol  
  unsigned char connection[4] ; // for protocol
  char input_buffer[64] ; // input bytes copied here
  char output_buffer[64] ;
  unsigned int outputs_regs[32] ; // modbus routines copied to ints
  unsigned int inputs_regs[32] ; // we borrowed code from that so we translate
  int state ; // connection state.
  input_struct inputs[N_INPUTS] ; 
  int n_inputs ;
  output_struct outputs[N_OUTPUTS] ; 
  int n_outputs ;
  } idlinx_struct ; 

@ Fixed number of inputs and outputs.
@<Defines@>+=
#define N_INPUTS (16)
#define N_OUTPUTS (16)

@ States.
@<Defines@>+=
enum iostates {ID_IDLE, ID_HAVESESSION, ID_REGISTERED} ;

@ Globals.
@<Globals@>=
idlinx_struct cards[N_CARDS] ;
int host_octet[4] ;

@ We make room for the full octet of cards.
@<Defines@>+=
#define N_CARDS (256)

@ We keep the current device being configured
in a global so that it is accessible to the 
iterators.
@<Globals@>+=
idlinx_struct *cur_scan_dev ;

@ Read all points associated with one device. In the initialization
we iterate over all devices attached to the named driver 
|"io:linx"|. The function below adds each point to a linked
lists associated with one device (card).
@<Functions@>=
int 
idcomm_each_point(Dp_Record *dpr)
  {
  char *type = dpr->io_data ; 
  int handle = dp_record(dpr) ;

  Inform("adding [%s] type %s",dpr->name,type) ;
  if (is_input(type)) 
    add_input(cur_scan_dev,type,handle) ;
  else 
    add_output(cur_scan_dev,type,handle) ;
  }

@ Proto.
@<Prototypes@>+=
int idcomm_each_point(Dp_Record *dpr) ;

@* Parsing IP. Data for an IP in |"w.x.y.z"| format needs to be parsed to
four strings. This is a helper function for that. |MAXSPLIT| is the max
number of ints (should be 4).
@<Functions@>=
char **
splitstring(char *src, char split, int *count)
  {
  char **result ;
  int i ;
  char tmp[256] ;
  char *dst ;
  int string_count  ;

  result = malloc(sizeof(char **) * MAXSPLIT) ;
  bzero(result,sizeof(char**) * MAXSPLIT) ;

  for (dst=tmp,string_count = 0 ; *src != 0 ; src++) 
    {
    if (*src==split) 
      {
      *dst = 0 ;
      result[string_count++] = strdup(tmp) ;
      tmp[0] = 0 ;
      dst = tmp ;
      continue ;
      }
    *dst++ = *src ;
    }
  *dst = 0 ;
  result[string_count++] = strdup(tmp) ;

  *count = string_count ;
  return result ;
  }

@ Proto.
@<Prototypes@>+=
char **splitstring(char *src, char split, int *count) ;

@ The maximum to be split.
@<Defines@>+=
#define MAXSPLIT 12

@ This parses the octet, after being split into |N| strings.
@<Functions@>=
int 
parse_octet(char *input,int *octet)
  {
  char **split ;
  int count ;
  int which = 0 ;
  int s = 0 ;

  split = splitstring(input,'.',&count) ;
  if (count==4) 
    {
    int i;

    for (i=0 ; i < 4 ; i++) 
      octet[i] = atoi(split[i]) ;
    for(s=0 ; s<count ; s++)
      if(split[s])
         free(split[s]) ;
    free(split) ;
    }
  }

@ Proto.
@<Prototypes@>+=
int parse_octet(char *input,int *octet) ;

@* Get devices and points. Above, we added inputs to the card array. 

@ |idcomm_each_device()| is called by the iterator for each device 
defined that will be serviced by this program.

We put the "card" data in the |idlinx_struct| array |cards|. This is an
array indexed by the final byte (octet) of the IPv4 address.

The |dp_ioscan()| iterates the |idcomm_each_point()| over each dp assigned
to the card device.
@<Functions@>=
int 
idcomm_each_device(int handle)
  {
  Dp_Device dev ;
  char ip[48]  ;
  int octet[4] ;
  int index ;
  int i ;

  dp_readdevice(handle,&dev) ;
  strcpy(ip,dev.device_data) ; 

  // parse final octet.
  parse_octet(ip,octet) ; 
  index = octet[3] ; 
  Inform("[%s] => index %d",ip,index) ;

  cur_scan_dev = cards + index ;
  for (i=0 ; i<4 ; i++) 
    cards[index].octet[i] = octet[i] ;
  cur_scan_dev->ip = strdup(ip) ;
  cur_scan_dev->state = ID_IDLE ;

  strncpy(cards[index].name,dev.name,TRAK_NAME_LEN) ;
  cards[index].name[TRAK_NAME_LEN] = '\0' ;

  Inform("scan %s %s for points",dev.name,dev.device_data) ;
  dp_ioscan(handle,idcomm_each_point) ;
  }

@ Proto.
@<Prototypes@>+=
int idcomm_each_device(int handle) ;

@ Report.
@<Globals@>+=
char *id ;
char *event ;

@* ID Linx points and logic. The following functions assist in mapping
data words and offsets to/from ID Linx to dp. 

@ Decode I/O. inputs have |"types"|. 
@<Functions@>=
int
is_input(char *type)
  {
  if (!strcmp("sns_a",type))     return 1 ;
  if (!strcmp("sns_b",type))     return 1 ;
  if (!strcmp("mtr_a_aux",type)) return 1 ;
  if (!strcmp("mtr_b_aux",type)) return 1 ;
  if (!strcmp("sns_a_flt",type)) return 1 ;
  if (!strcmp("sns_b_flt",type)) return 1 ;
  if (!strcmp("mtr_a_flt",type)) return 1 ;
  if (!strcmp("mtr_b_flt",type)) return 1 ;
  if (!strcmp("in1",type))       return 1 ;
  if (!strcmp("in2",type))       return 1 ;
  if (!strcmp("in3",type))       return 1 ;
  // else
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int is_input(char *type)  ;

@ Add input.
@<Functions@>+=
void 
add_input(idlinx_struct *this, char *type, int handle)
  {
  if(handle>=0)
    {
    input_struct *input ;

    input = this->inputs + this->n_inputs++ ;
    bzero(input,sizeof(input_struct)) ;
    input->type   = 'b' ; // bit read
    input->reg    = get_input_reg(type) ;
    input->bit    = get_input_bit(type) ;
    if (input->bit < 0) 
      input->type = 's' ; 
    input->active = 1 ;
    input->value  = -1 ;
    input->handle = handle ;
    input->record = dp_pointer(handle) ;
    }
  }

@ Proto.
@<Prototypes@>+=
void add_input(idlinx_struct *this, char *type, int handle) ;

@ Get input register.
@<Functions@>+=
int 
get_input_reg(char *type)
  {
  if (!strcmp("sns_a",type))     return 2 ;
  if (!strcmp("sns_b",type))     return 2 ;
  if (!strcmp("mtr_a_aux",type)) return 2 ;
  if (!strcmp("mtr_b_aux",type)) return 2 ;
  if (!strcmp("sns_a_flt",type)) return 2 ;
  if (!strcmp("sns_b_flt",type)) return 2 ;
  if (!strcmp("mtr_a_flt",type)) return 2 ;
  if (!strcmp("mtr_b_flt",type)) return 3 ;
  if (!strcmp("in1",type))       return 3 ;
  if (!strcmp("in2",type))       return 3 ;
  if (!strcmp("in3",type))       return 3 ;
  // else
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
int get_input_reg(char *type) ;

@ Get input bit.
@<Functions@>+=
int 
get_input_bit(char *type)
  {
  if (!strcmp("sns_a",type))     return 8 + 0 ;
  if (!strcmp("sns_b",type))     return 8 + 1 ;
  if (!strcmp("mtr_a_aux",type)) return 8 + 4 ;
  if (!strcmp("mtr_b_aux",type)) return 8 + 5 ;
  if (!strcmp("sns_a_flt",type)) return 8 + 2 ;
  if (!strcmp("sns_b_flt",type)) return 8 + 3 ;
  if (!strcmp("mtr_a_flt",type)) return -1 ;
  if (!strcmp("mtr_b_flt",type)) return -2 ;
  if (!strcmp("in1",type))       return 0 ;
  if (!strcmp("in2",type))       return 1 ;
  if (!strcmp("in3",type))       return 2 ;
  // else
  return -1;
  }

@ Proto.
@<Prototypes@>+=
int get_input_bit(char *type) ;

@ Add output.
@<Functions@>+=
void 
add_output(idlinx_struct *this, char *type, int handle)
  {
  if(handle>=0)
    {
    output_struct *output ;
    int bit = get_output_bit(type) ;
    output = this->outputs + this->n_outputs++ ;
    bzero(output,sizeof(output_struct)) ;

    if (!strcmp(type,"mtr_a") ||
        !strcmp(type,"mtr_b")) 
      {
      output->is_motor_output = 1 ;
      output->is_a = !strcmp(type,"mtr_a") ;
      }
    output->reg    = get_output_reg(type) ;
    output->active_and_mask   = ~(1 << bit) ;
    output->active_or_mask    =   1 << bit ;
    output->inactive_and_mask = ~(1 << bit) ;
    output->inactive_or_mask  =  0 ;
    output->value  = -1 ;
    output->handle = handle ;
    output->record = dp_pointer(handle) ;
    }
  }

@ Proto.
@<Prototypes@>+=
void add_output(idlinx_struct *this, char *type, int handle) ;

@ Get output register.
@<Functions@>+=
int 
get_output_reg(char *type)
  {
  if (!strcmp(type,"mtr_a"))     return 0 ; 
  if (!strcmp(type,"mtr_b"))     return 0 ; 
  if (!strcmp(type,"mtr_a_dir")) return 1 ; 
  if (!strcmp(type,"mtr_b_dir")) return 1 ; 
  if (!strcmp(type,"mtr_a_rst")) return 1 ; 
  if (!strcmp(type,"mtr_b_rst")) return 1 ; 
  if (!strncmp(type,"out",3))    return 1 ;
  // else
  return -1 ;
  }

@ Proto.
@<Prototypes@>+=
int get_output_reg(char *type) ;

@ Get output bit.
@<Functions@>+=
int 
get_output_bit(char *type)
  {
  if (!strcmp(type,"mtr_a"))     return 8 ; 
  if (!strcmp(type,"mtr_b"))     return 9 ; 
  if (!strcmp(type,"mtr_a_dir")) return 8+4 ; 
  if (!strcmp(type,"mtr_b_dir")) return 8+5 ; 
  if (!strcmp(type,"mtr_a_rst")) return 8+6 ; 
  if (!strcmp(type,"mtr_b_rst")) return 8+7 ; 
  if (!strcmp(type,"out1"))      return 0 ;
  if (!strcmp(type,"out2"))      return 1 ;
  if (!strcmp(type,"out3"))      return 2 ;
  if (!strcmp(type,"out4"))      return 3 ;
  if (!strcmp(type,"out5"))      return 4 ;
  // else
  return -1 ;
  }

@ Proto.
@<Prototypes@>+=
int get_output_bit(char *type) ;

@ Input data is received in a UDP datagram. First, this is copied from 
the byte array it was received in, to a word by word array of int.

Then we process all inputs in the list of inputs assigned to the card and
evaluate changes.
@<eval inputs from one card@>=
  {
  int i,n ;
  input_struct *input ;

  for (i=0 ; i<8 ; i++) 
    {
    mycard->inputs_regs[i] = 
        (mycard->input_buffer[2*i + 0] <<8) + 
            mycard->input_buffer[2*i + 1] ;
    } 

  for(n=0 ; n<mycard->n_inputs ; n++)
    {
    int current ;

    input = mycard->inputs + n ;
    current = mycard->inputs_regs[input->reg] ;
    if(input->type=='b')
      {
      current >>= input->bit ;
      current  &= 1 ;
      if(input->active==0)
        current = current==0 ? 1 : 0 ;
      }
    else if(input->type=='m')
      {
      current = (current==input->match) ? 1 : 0 ;
      }
    else if (input->type=='s')  // motor faults are special for inputs
      { // do special input
      int active ;
      current >>=(input->bit == -1) ? 0 : 8 ;
      current &= 0xff ;
      active = (current != 0) ;
      if (active != input->value)
        {
        input->record->export = active ;
        input->value = active ;
        }
      if(active)  // register 2 set to bitmask to indicate fault
        {
        input->record->data[2] = current ;
        }
      else
        {
        input->record->data[2] = 0 ;
        }
      }

    if(input->type != 's' && current != input->value)
      {
      Inform("changed %s -> %d",input->record->name,current) ;
      input->record->export = current ;
      input->value = current ;
      }
    }  
  }

@ Likewise, for outputs (which are sent out in a UDP datagram). 
Periodically (by polling rate),  we process all outputs building
the output word array, which is then copied to the output byte array.
@<eval linx outputs@>=
  {
  int i,n ;
  output_struct *output ;

  for(n=0 ; n<mycard->n_outputs ; n++)
    {
    int err ;
    int off ;
    int current ;

    output = mycard->outputs + n ;
    off = output->reg ;
    current = output->record->set_value ;
    if(current != output->value)
      {
      Inform("set %s -> %d",
             output->record->name,current) ;
      if(current)
        {
        mycard->outputs_regs[off] &= output->active_and_mask ;
        mycard->outputs_regs[off] |= output->active_or_mask ;
        }
      else
        {
        mycard->outputs_regs[off] &= output->inactive_and_mask ;
        mycard->outputs_regs[off] |= output->inactive_or_mask ;
        }
      output->record->export = current ;
      output->value = current ;
      }
    if (output->is_motor_output)
      { // set speed.
      int and_mask,or_mask ;
      int shift ;
      int speed ;

      speed = output->record->data[3] ;
      if (speed < 1) 
        speed = 1 ;
      if (speed > 4) 
        speed = 1 ;
      speed-- ;
      shift = (output->is_a) ? 0 : 4 ;
      and_mask = ~(0x0f << shift) ;
      or_mask  =  (1    << speed) << shift ;
      mycard->outputs_regs[off] &= and_mask ;
      mycard->outputs_regs[off] |= or_mask ;
      }
    }
  // copy locally
  for (i=0 ; i < 2 ; i++) 
    {
    mycard->output_buffer[2*i] =  (mycard->outputs_regs[i] >>8) & 0xff ;
    mycard->output_buffer[2*i+1] =  (mycard->outputs_regs[i] >>0) & 0xff ;
    }
  }

@* Ether/IP.

@ IP.
@<Globals@>+=
char ip[32+1] ;

@* Processes. These are forked ... most run as cweb macros.

@ Recv. On process receives all (input) data.
First socketry. Bind to UDP 2222. 
Then do |recvfrom|. Decode the incomming address to locate the card and
process.
Finally, logic to report packet rate reported every 100 seconds.
@<UDP server (handles all inputs)@>=
  {
  int len ;
  int last_time ;
  int pcount ;
  struct sockaddr_in name,remote ;

  if((udp=socket(AF_INET,SOCK_DGRAM,0))<0) 
    {
    Alert( "unable to obtain socket, exiting" );
    exit( EXIT_FAILURE );
    }
  len = sizeof( struct sockaddr_in );
  memset( &name, 0, len );
  name.sin_family = AF_INET;
  name.sin_port = htons( 2222 );
  name.sin_addr.s_addr = htonl( INADDR_ANY );

  if(bind(udp,(struct sockaddr*)&name,len)<0) 
    {
    Alert( "unable to bind to socket, exiting" );
    exit( EXIT_FAILURE );
    }
  Trace("udp server %d  bound to socket 2222",udp) ;

  pcount = 0 ;
  last_time = time(NULL) ;
  for (;;) 
    {
    int err ;
    char request[256] ;
    char addr[256+1] ;
    int octet[4] ;
    int offset ;
    int index ;
    int this_time ;
    idlinx_struct *mycard ;
    int i ;
    len = sizeof( struct sockaddr_in );
    err = recvfrom( udp, request, 200, 0, ( struct sockaddr* )&remote, &len);
    if (err < 0) 
      continue ;
// Inform("udp") ;
    strncpy(addr,inet_ntoa(remote.sin_addr),256) ;
    parse_octet(addr,octet) ; 
    index = octet[3] ; 
    // process inputs at that index
    mycard = cards + index ;
    offset = err - 64 ;
    for (i=0 ; i < 64 ; i++) 
      {
      mycard->input_buffer[i] = request[i+ offset] ;
      } 
    @<eval inputs from one card@>@;
    @<in: packet loss@>@;
    pcount++ ;
    this_time = time(NULL) ;
    if (this_time != last_time) 
      {
      last_time = this_time ;
      if (last_time % 100 == 0) 
        Inform("recv %d packets per second",pcount) ;
      pcount = 0 ;
      }
    }

  Inform("never get here") ;
  }

@ Every time a packet for this card is received, if a tag exists it is 
removed. 
@<in: packet loss@>=
  {
  char stem[32] ;
  sprintf(stem,"/connect-%d/",index) ;
  tag_delete(stem) ;
  }

@ Check packet loss.
@<out: check packet loss@>=
  {
  char stem[32] ;
  char *val ;

  sprintf(stem,"/connect-%d/",mycard->octet[3]) ;
  val = tag_value(stem) ; 
  if (val != NULL) 
    {  
    char comm_name[32+1] ;
    int  comm ;

    free(val) ;
    Alert("connection to %s %d.%d.%d.%d has been lost",
          mycard->name,
          mycard->octet[0],
          mycard->octet[1],
          mycard->octet[2],
          mycard->octet[3]) ;
    close(s) ;
    s = -1 ;
 
    mycard->state = ID_IDLE ;
    tns = 0 ;

    strcpy(comm_name,mycard->name) ;
    comm_name[strlen(comm_name)-4]='\0' ;
    strcat(comm_name,"_comm") ;
    comm = dp_handle(comm_name) ;
    if(comm>=0)
      dp_set(comm,0) ;
    
    sleep(30) ;
    continue ;
    }
  }

@ Outbound packet loss
@<out: setup packet loss@>=
  {
  char stem[32] ;
  char val[32] ;
  sprintf(stem,"/connect-%d/",mycard->octet[3]) ;
  sprintf(val,"%d",tns) ;
  tag_insert(stem,val) ;
  }

@ This process does two things. First it establishes and holds a
connection. It registers and establishes forwarding. It also needs to 
detect lost connection and reconnect as needed.
If a good connection is available, it will try to hammer 
 out 200 datagram packets a second to
the device.
@<process one card@>=
  {
  unsigned int tns =0;
  int s ;
  int last_time, this_time ;
  int first_time  ;
  int pcount =0 ;
  char *event ;
  char tmp[32] ;
  char comm_name[32+1] ;
  int  comm ;
  struct sockaddr_in to ;

  memset(&to, 0, sizeof(to)) ;
  to.sin_family = AF_INET ;
  to.sin_addr.s_addr = inet_addr(mycard->ip) ;
  to.sin_port   = htons(2222) ; 

  sprintf(tmp,"idlinx-noconnect-%d",mycard->octet[3]) ; 
  event = strdup(tmp) ;
  last_time = time(NULL) ;
  strcpy(comm_name,mycard->name) ;
  comm_name[strlen(comm_name)-4]='\0' ;
  strcat(comm_name,"_comm") ;
  comm = dp_handle(comm_name) ;
  if(comm>=0)
    dp_set(comm,0) ;

  for (;;usleep(POLLRATE * 990)) 
    {
    if (mycard->state == ID_IDLE) 
      {
      @<register@>@;
      } 
    else if (mycard->state == ID_HAVESESSION) 
      {
      first_time = 1 ;
      @<send forward packet@>@;
      } 
    else if (mycard->state == ID_REGISTERED) 
      {
      @<send UDP datagram@>@;
      ++pcount;
      this_time = time(NULL);
      if (last_time != this_time) 
        {
        if (first_time) 
          {
          char comm_name[32+1] ;
          int comm ;
          strcpy(comm_name,mycard->name) ;
          comm_name[strlen(comm_name)-4]='\0' ;
          strcat(comm_name,"_comm") ;
          comm = dp_handle(comm_name) ;
          if(comm>=0)
            dp_set(comm,1) ;
          first_time=0 ;
          } 
        else 
          {
          @<out: check packet loss@>@;
          }
        if (last_time %100==0)
          //if(pcount<110)
          if( pcount < 90 )   //ank
            Alert("%s sending %d per second",mycard->ip,pcount) ;
        last_time = this_time ;
        pcount = 0 ;
        @<out: setup packet loss@>@;
        }
      }
    }
  }

@ This is the UDP outbound datagram. We use the session ID as 
connection ID. (card is located not by connection id by recvfrom address).
@<send UDP datagram@>=
  {
  char out[100] ;
  int i ;
  int sent ;
  int c = 0 ;

  out[c++] = 0x2 ; // 2 bytes, number of objects
  out[c++] = 0x0 ; //
  out[c++] = 0x02 ; // 2 bytes "sequenced address item
  out[c++] = 0x80 ;
  out[c++] = 0x8 ; // 2 byte length session + tns = 8
  out[c++] = 0x0 ; // session + tns = 8
  for (i=0 ;i < 4 ; i++) // 4 bytes, copy in session id
    out[c++] = mycard->connection[i] ;
  out[c++] = (tns++  ) & 0xff ;
  out[c++] = (tns>> 8) & 0xff ;
  out[c++] = (tns>>16) & 0xff ;
  out[c++] = (tns>>24) & 0xff ;
      // "data" ... first 6 bytes not part of the 64
      // don't know what that is.
  out[c++] = 0xb1 ;
  out[c++] = 0x00 ;
  out[c++] = 0x46 ;
  out[c++] = 0x00 ;
  out[c++] = (tns++  ) & 0xff ; // CIP sequence number
  out[c++] = (tns>> 8) & 0xff ;
  out[c++] = 1 ;
  out[c++] = 0 ;
  out[c++] = 0 ;
  out[c++] = 0 ;

  @<eval linx outputs@>@;

  for (i=0 ; i < 64 ; i++) 
    out[c++] = mycard->output_buffer[i] ;

  sent = sendto(udp,out,c,0,(struct sockaddr *)&to, sizeof(to)) ;
//Inform("udp %d out %d",udp,sent) ;
  }

@ Registration. For us that is just to get a session id.
We also open the socket. 
@<register@>=
  {
  char msg[MSG_SIZE] ;
  int m, r1, j, sent ;
  int optval ;
  int len = sizeof(optval) ;
  struct hostent *hp;
  struct sockaddr_in name;
  struct in_addr addr;
  int keepcnt = 5 ;
  int keepidle = 15 ;
  int keepintvl = 15 ;

  @<select vars@>@;
  
  Trace("open socket to [%s]",mycard->ip) ;
  s = lnxnet_open(mycard->ip,ETHERNETIP_PORT) ;
  if (s < 0) 
    {
    Alert("failed to open socket to [%s]",mycard->ip) ;
    sleep(30) ;
    continue ;
    }
  optval = 1 ;

  err = setsockopt(s,SOL_SOCKET,SO_KEEPALIVE,(void *)&optval,sizeof(optval)) ;
  Inform("keepalive %d",err) ;

  err = setsockopt(s,IPPROTO_TCP,TCP_KEEPCNT,(void *)&keepcnt,sizeof(keepcnt)) ;
  Inform("keepcnt %d %d",keepcnt,err) ;

  err = setsockopt(s,IPPROTO_TCP,TCP_KEEPIDLE,(void *)&keepidle,sizeof(keepidle)) ;
  Inform("keepidle %d %d",keepidle,err) ;

  err = setsockopt(s,IPPROTO_TCP,TCP_KEEPINTVL,(void *)&keepintvl,sizeof(keepintvl)) ;
  Inform("keepintvl %d %d",keepintvl,err) ;

#if 0
  memset(&name,0,sizeof(struct sockaddr_in)) ;
  name.sin_family=AF_INET ;
  name.sin_port = htons(2222) ;
  hp = gethostbyname( mycard->ip) ;
  memcpy(&name.sin_addr,hp->h_addr_list[0],sizeof(name.sin_addr)) ; 
  connect(us,(struct sockaddr*) &name,sizeof(name)) ;
#endif

  Inform("opened sockets TCP: %d",s) ;
  m = 0 ;

  msg[m++] = 0x65 ; // command: register session
  msg[m++] = 0x00 ;
  r1 = m ;
  msg[m++] = 0x00 ; // length beyond 24 byte header
  msg[m++] = 0x00 ;
  for(j=0 ; j<4 ; j++)
    msg[m++] = 0x00 ; // session handle
  for(j=0 ; j<4 ; j++)
    msg[m++] = 0x00 ; // status
#if 1
  msg[m++] = 0xF0 ;
  msg[m++] = 0xCA ;
  msg[m++] = 0x01 ;
  msg[m++] = 0x00 ;

  msg[m++] = 0x00 ;
  msg[m++] = 0x00 ;
  msg[m++] = 0x00 ;
  msg[m++] = 0x00 ;
#else
  for(j=0 ; j<8 ; j++)
    msg[m++] = 0x00 ; // sender context
#endif
  for(j=0 ; j<4 ; j++)
    msg[m++] = 0x00 ; // options
  msg[m++] = 0x01 ;
  msg[m++] = 0x00 ; // protocol version
  msg[m++] = 0x00 ;
  msg[m++] = 0x00 ; // options
  msg[r1] = m - 24 ;

  Inform("register packet sent") ;
  sent = write(s,msg,m) ;
  if(sent != m)
    {
    char comm_name[32+1] ;
    int comm ;

    Alert("failed to write session packet to plc") ;

    strcpy(comm_name,mycard->name) ;
    comm_name[strlen(comm_name)-4]='\0' ;
    strcat(comm_name,"_comm") ;
    comm = dp_handle(comm_name) ;
    if(comm>=0)
      dp_set(comm,0) ;

    if (s > 0) close(s) ;
    s = -1 ;
    mycard->state = ID_IDLE ;

    sleep(10) ;
    continue ;
    }
 
  // process response
  Inform("setup select for response") ;
  @<setup select@>@;
  
  if (n>0) 
    {
    @<get packet@>@;
    Inform("got response") ;
    if (recvd && reply[0] == 0x65) 
      {
      mycard->session[0] = reply[4+0] ;
      mycard->session[1] = reply[4+1] ;
      mycard->session[2] = reply[4+2] ;
      mycard->session[3] = reply[4+3] ;
      Inform("response is good have session") ;
      Inform("session %x %x %x %x",
             mycard->session[0], 
             mycard->session[1], 
             mycard->session[2], 
             mycard->session[3] ) ;
      mycard->state = ID_HAVESESSION ;
      }
    else
      {
      char comm_name[32+1] ;
      int comm ;

      Alert("session response invalid") ;
      strcpy(comm_name,mycard->name) ;
      comm_name[strlen(comm_name)-4]='\0' ;
      strcat(comm_name,"_comm") ;
      comm = dp_handle(comm_name) ;
      if(comm>=0)
        dp_set(comm,0) ;
      if (s > 0) 
        close(s) ;
      s = -1 ;
      mycard->state = ID_IDLE ;
      sleep(10) ;
      continue ;
      }
    } 
  else
    {
    char comm_name[32+1] ;
    int comm ;

    Alert("no session response") ;
    strcpy(comm_name,mycard->name) ;
    comm_name[strlen(comm_name)-4]='\0' ;
    strcat(comm_name,"_comm") ;
    comm = dp_handle(comm_name) ;
    if(comm>=0)
      dp_set(comm,0) ;
    if (s > 0) 
      close(s) ;
    s = -1 ;
    mycard->state = ID_IDLE ;
    sleep(10) ;
    continue ;
    }
  }

@ Two types of packets are received here. One is session, one is
forward. We have standard vars and select handling done in macros.
@<select vars@>=
fd_set rfds ;
struct timeval tv ;
int err, n, mcount, received ;
unsigned char reply[MSG_SIZE+1] ;
int result ;
int recvd = 0 ;

@ setup the select
@<setup select@>=
  FD_ZERO(&rfds) ;
  FD_SET(s,&rfds) ;
  tv.tv_sec  = 2 ;
  tv.tv_usec = 0 ;
  n = s+1 ;
  Inform("select started") ;
  n = select(n,&rfds,NULL,NULL,&tv) ;
  Inform("select returned %d",n) ;

@ Get packet.
@<get packet@>=
mcount = recv(s,&reply,4,MSG_PEEK|MSG_WAITALL) ;
Inform("peek got %d",mcount) ;
if(mcount==4)
  {
  memcpy(&mcount,&reply[2],2) ;
  mcount += 24 ;
  received = recv(s,&reply,mcount,MSG_WAITALL) ;
  Trace("received %d",received) ;
  recvd = 1 ;
  }

@ Forward packet setup data, similar but just more data than the 
session/register packet.
@<send forward packet@>=
  {
  char out[100] ;
  int i ;
  int c = 0 ;
  int lpos ;
  int timeout ;  
  int sent ;

  mycard->connection[3] = 0x00 ;
  mycard->connection[2] = 0x01 ;
  mycard->connection[1] = 0xa5 ;
  mycard->connection[0] = 0xd8 ;

#if 0
  for(i=0 ; i<2 ; i++)
    mycard->connection[i] = rand() & 0xff ;
#endif

  @<select vars@>@;

  Trace("prepare forward packet") ;
  out[c++] = 0x6f ; // send RR data (?RR?) 2 bytes   
  out[c++] = 0x00 ; // 
  lpos = c ;
  out[c++] = 0x60; // length  2 bytes ... hard coded now
  out[c++] = 0x00 ; // length 
  for (i=0 ;i  < 4 ; i++) // four byte session
    out[c++] = mycard->session[i] ;
  Inform("session %x %x %x %x",
         mycard->session[0], 
         mycard->session[1], 
         mycard->session[2], 
         mycard->session[3] ) ;
  for (i=0 ;i < 4 ; i++)  // four byte status
    out[c++] = 0 ; 
#if 1
  out[c++] = 0xf0 ;
  out[c++] = 0xca ;
  out[c++] = 0x01 ;
  out[c++] = 0x00 ;

  out[c++] = mycard->connection[0] ;
  out[c++] = mycard->connection[1] ;
  out[c++] = mycard->connection[2] ;
  out[c++] = mycard->connection[3] ;
#else
  for (i=0 ;i < 8 ; i++) // 8 byte sender context
    out[c++] = 1 ;
#endif
  for (i=0 ;i < 4 ; i++)  // four byte options
    out[c++] = 0 ; 
  for (i=0 ;i < 4 ; i++)  // "command specific data 4 bytes
    out[c++] = 0 ; 
  out[c++] = 0x0A ; //   timeout
  out[c++] = 0x0 ; //  
  out[c++] = 0x3 ; // item count = 3 (2 bytes)
  out[c++] = 0x0 ; // 

  out[c++] = 0x0 ;
  out[c++] = 0x0 ;

  out[c++] = 0x0 ;
  out[c++] = 0x0 ;

  out[c++] = 0xb2 ;
  out[c++] = 0x00 ;

  out[c++] = 0x3c ;
  out[c++] = 0x00 ;

  out[c++] = 0x54 ; // "unknown service request 1 byte

  out[c++] = 0x2 ; // request path size (words?)
  out[c++] = 0x20 ; // request path? 
  out[c++] = 0x06 ; 
  out[c++] = 0x24 ; 
  out[c++] = 0x01 ; 
  out[c++] = 0x0A ; // "command specific data" 
  out[c++] = 0xF0 ; // "command specific data" 
  // T->O connection ID ?? could be random? using what moxa used
  for (i=0 ;i < 4 ; i++) 
    out[c++] = mycard->connection[i] ;
  // O->T connection ID. .. .is same?
  // will be returned in udp packets (and sent)
  for (i=0 ;i < 4 ; i++) 
    out[c++] = mycard->connection[i] ;
  out[c++] = mycard->connection[0] ; // connection serial number 
  out[c++] = mycard->connection[1] ; 
  out[c++] = 0xDF ; // vendor serial number (seriously?)
  out[c++] = 0x03 ;
  out[c++] = 0x36 ; // originator serial number
  out[c++] = 0x1D ;
  out[c++] = 0x00 ;
  out[c++] = 0x00 ;
  out[c++] = 0x2 ; // connection timeout mulitplier
  for (i=0 ;i < 3 ; i++) // fill out 4 byte boundary ... "reserved"
    out[c++] = 0x0 ;
  // O->T time and params
  timeout = POLLRATE * 1000 ; // one second
  out[c++]  = (timeout >> 0) & 0xff ;
  out[c++]  = (timeout >> 8) & 0xff ;
  out[c++]  = (timeout >> 16) & 0xff ;
  out[c++]  = (timeout >> 24) & 0xff ;
  out[c++] = 0x46 ;
  out[c++] = 0x48 ;
  // T->O time and params
  timeout = POLLRATE * 1000 ; // one second
  out[c++]  = (timeout >> 0) & 0xff ;
  out[c++]  = (timeout >> 8) & 0xff ;
  out[c++]  = (timeout >> 16) & 0xff ;
  out[c++]  = (timeout >> 24) & 0xff ;
  out[c++] = 0x42 ;
  out[c++] = 0x48 ;
  out[c++] = 0x1 ; // transport type/trigger
  out[c++] = 0x9 ; // connection path size
  // connection path
  out[c++] = 0x34 ; // elect key seg
  out[c++] = 0x4 ; // key format
  out[c++] = 0x00 ; // vendor id (2 bytes)
  out[c++] = 0x00 ; 
  out[c++] = 0x00 ; // "deprecated"   ... srsly
  out[c++] = 0x00 ; //  2nd byte
  out[c++] = 0x00 ; // product code
  out[c++] = 0x00 ; 
  out[c++] = 0x00 ; // compatibility major revision 
  out[c++] = 0x00 ; // compat minor rev 
  out[c++] = 0x20 ; // path segement 8 bit class
  out[c++] = 0x04 ;
  out[c++] = 0x24 ; // path 8 bit instance 
  out[c++] = 0x01 ; 
  out[c++] = 0x2c ; // 8 bit connection point
  out[c++] = 0x64 ; // instance 100
  out[c++] = 0x2c ; // 8 bit connection point
  out[c++] = 0x65 ; // instance 101
  // additional undocumented bytes
  out[c++] = 0x01 ; // socket address info
  out[c++] = 0x80 ;
  out[c++] = 0x10 ; // length 16
  out[c++] = 0x00 ;
  out[c++] = 0x00 ; // sin familly 2
  out[c++] = 0x02 ;
  out[c++] = 0x08 ;  // 2222 (port)
  out[c++] = 0xAE ;  
// host IP goes here. Can't do automatically, is argument
// because host likely has several NICs
  out[c++] = host_octet[0] ;  // instruct where to send
  out[c++] = host_octet[1] ;  // packets back
  out[c++] = host_octet[2] ;  
  out[c++] = host_octet[3] ;  
  for (i=0 ;i < 8 ; i++) 
   out[c++] = 0x0 ;
  
  Inform("packet length  = %d compare to %d",c,0x60) ;
  sent = write(s,out,c) ;
  if (sent != c) 
    {
    char comm_name[32+1] ;
    int comm ;

    Alert("failed to write forward packet to plc") ;
    strcpy(comm_name,mycard->name) ;
    comm_name[strlen(comm_name)-4]='\0' ;
    strcat(comm_name,"_comm") ;
    comm = dp_handle(comm_name) ;
    if(comm>=0)
      dp_set(comm,0) ;
    if (s > 0) 
      close(s) ;
    s = -1 ;
    mycard->state = ID_IDLE ;
    sleep(10) ;
    continue ;
    }

  // process response
  @<setup select@>@;
  if (n>0)
    {
    @<get packet@>@;
    Inform("got response reply[0] = %x",reply[0]) ;
  
    if (recvd && reply[0] == 0x6f)
      {
      int i ;

      for(i=0 ; i<4 ; i++)
        mycard->connection[i] = reply[44+i] ;

      Inform("response good %02x %02x %02x %02x",
             mycard->connection[0],
             mycard->connection[1],
             mycard->connection[2],
             mycard->connection[3]) ;

      mycard->state = ID_REGISTERED ;
      }
    else
      {
      char comm_name[32+1] ;
      int comm ;

      Alert("forward packet response invalid") ;
      strcpy(comm_name,mycard->name) ;
      comm_name[strlen(comm_name)-4]='\0' ;
      strcat(comm_name,"_comm") ;
      comm = dp_handle(comm_name) ;
      if(comm>=0)
        dp_set(comm,0) ;
      if (s > 0) 
        close(s) ;
      s = -1 ;
      mycard->state = ID_IDLE ;
      sleep(10) ;
      continue ;
      }
    }
  else
    {
    char comm_name[32+1] ;
    int comm ;

    Alert("forward packet no response") ;
    strcpy(comm_name,mycard->name) ;
    comm_name[strlen(comm_name)-4]='\0' ;
    strcat(comm_name,"_comm") ;
    comm = dp_handle(comm_name) ;
    if(comm>=0)
      dp_set(comm,0) ;
    if (s > 0) 
      close(s) ;
    s = -1 ;
    mycard->state = ID_IDLE ;
    sleep(10) ;
    continue ;
    }
  }

@* Net Open.  Open a socket.
@<Functions@>+=
static pthread_mutex_t mut ;

int lnxnet_setup() {
  pthread_mutex_init(&mut,NULL) ;
}
int lnxnet_open( char *host, int port ) {
  int s;
  struct hostent *hp;
  struct sockaddr_in name;
  struct in_addr addr;

  pthread_mutex_lock(&mut) ;

  if( ( s = socket( AF_INET, SOCK_STREAM, 0 ) ) < 0 ) return( -1 );
  memset( &name, 0, sizeof( struct sockaddr_in ) );
  name.sin_family = AF_INET;
  name.sin_port = htons( port );
  if( ( hp = gethostbyname( host ) ) == NULL ) return( -2 );
  memcpy( &name.sin_addr, hp->h_addr_list[ 0 ], sizeof( name.sin_addr ) );
  pthread_mutex_unlock(&mut) ;
  if( connect( s, ( struct sockaddr* )&name, sizeof( name ) ) < 0 ) {
    close( s );
    return( -3 );
  }
  return( s );
}
@ Prototype.
@<Prototypes@>+=
int lnxnet_open( char *host, int port );


@* Index.
