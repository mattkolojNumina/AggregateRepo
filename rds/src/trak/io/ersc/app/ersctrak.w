% 
%   ersc.w -- connect networked ERSC modules with trak 
%
%   Author: Mark Woodworth 
%   Author: Mark Olson (continued) 
%
%   History:
%      2013-04-18 -- check in (mrw) 
%      2013-04-22 -- debug -> go live (mdo) 
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
\def\title{ERSC}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Connect ERSC conveyor control modules to Trak

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
\centerline{RCS ID: $ $Id: ersc.w,v 1.2 2013/04/19 16:08:48 rds Exp rds $ $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: ersc.w,v 1.2 2013/04/19 16:08:48 rds Exp rds $" ;
@<Includes@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int main(int argc, char *argv[])
   {
   int ctr = 0 ;
   int now ;
   @<Initialize@>@;

   Trace("load device") ;

   now = time(NULL) ;
   for(;;/* usleep(5 * 1000L)*/)
      {
//      ++ctr ;
//      if (now != time(NULL)) 
//        {
//        Inform("%d hz",ctr) ;
//        now = time(NULL) ;
//        ctr = 0 ;
//        }
      if (dp_get(0)) 
         {
         if (here==NULL) 
            {
            find_devices() ;
            if (here==NULL) 
              {
              Alert("device not found") ;  
              sleep(1) ;
              continue ;
              }
            Inform("read begin: %d read_end %d",
               here->read_begin,here->read_end) ;
            }
         } 
      else 
         {
         release_devices() ;
         sleep(1) ;
         continue ;
         }
      if (here != NULL) 
        {
        report(do_input(here)) ;
        report(do_output(here)) ;
        } 
      }
   }

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>

#include <rds_trn.h>
#include <rds_net.h>
#include <rds_trak.h>
#include <rds_evt.h>

@ Initialize.
@<Initialize@>+=
   {
   char name[128] ;
   char *dot ;
   if (argc != 2) 
     {
     printf("usage: ersctrak ip\n") ;
     exit(0) ;
     }
   strcpy(ip,argv[1]) ;
   dot= ip ;
   dot = strstr(dot+1,".") ;
   dot = strstr(dot+1,".") ;
   sprintf(name,"e:%s",dot+1) ;
   trn_register(name) ;
   Trace("%s",rcsid) ;
   signal(SIGTERM,onTerm) ;
   }

@
@<Functions@>=
void onTerm(int pid)
{
  exit(0) ;
}


@ Report.
@<Globals@>+=
int comm_status = -1 ;
char ip[64] ;

@ Report the error.
@<Functions@>+=
int report(int err)
   {
   int status ;

   if(err==0)
      return 0 ;

   status = (err>0) ;

   if(comm_status != status)
      {
      if(status) 
         {
         Inform("communications restored") ;
         if (here->comm!=NULL) 
           evt_stop(here->comm->name) ;
         }
      else
         {
         Alert("communications lost") ;
         if (here->comm!=NULL) 
           evt_start(here->comm->name) ;
         }
      comm_status = status ;
      }

   if (comm_status == 0) 
     {
     if (!here->comm->value) 
       {
       here->comm->export =1 ; 
       }
     if (net>0) 
       {
       close(net) ;
       net = -1 ;
       }
     }
   return status ;
   }

@* IO.

@ Do Input.
@d MAX_READ 20
@<Functions@>+=
int do_input(struct ersc_struct *e)
   {
   unsigned int values[512] ;
   int count, err  ;

   count = e->read_end - e->read_begin + 1 ;
   
//   Inform("count = %d check input begin: %d count %d",
//       count,e->read_begin,e->read_end ) ;
   if (count <= 0) 
     {
     //   Inform("no reads") ;
     return 1 ;
      }
   if (count <MAX_READ)  
      {
      err = do_read(e->read_begin,count,values) ;
      } 
   else 
      {
      int start ;
      for (start = 0 ; start < count ; start += MAX_READ) 
         {
         int size = (count - start > MAX_READ) ? 
                    MAX_READ : count - start ;
         err = do_read(e->read_begin+ start,size,values+start) ;
         if (err < 0) break ;
         }
      }

   if(err<0)
      {
      Alert("do_input do_read fails %d",err) ;
      return err ;
      }

   struct input_struct *input ;
   for(input=e->inputs ; input!=NULL ; input=input->link)
      {
      int current ;
      current = values[input->reg - e->read_begin] ;

      if(input->type=='b')
         {     
         current >>= input->bit ;
         current  &= 1 ;
//         if(input->active==0)
//            current = current==0 ? 1 : 0 ;
         }
      if (input->type=='s')  // currently the motor faults
         {
         current &= 0xff80;
//         current &= ~(1<<9) ; // mask out overcurrent ... happens a lot
         
#if 0
         Inform("got [%x] for %s reg: %d %d %d",
            current,
            input->record->name,
            input->reg,
            input->record->export,
            input->record->value) ;
#endif
         if (current != 0) 
           {
           input->record->data[1] = current ;
           current = 1 ;
           }
         }

      if(input->type=='m')
         {
#if 0
         Inform("[%d] current %x match %x match_mask %x val %x",
              input->reg,current, input->match,input->match_mask,
                 current & input->match_mask) ;
#endif
         current = ((current & input->match_mask) ==input->match ) ? 1 : 0 ;
         }

      if(current != input->record->value)
         {
//         Inform("changed ... %s",input->record->name) ;
         input->record->export = current ;
         input->value = current ;
         }
      }

   return 1 ;
   }

@ Proto.
@<Prototypes@>+=   
int do_input(struct ersc_struct *e) ;

@ Do Output.
@<Functions@>+=
int do_output(struct ersc_struct *e)
   {
   int comm = 0 ;

   struct output_struct *output ;
   for(output=e->outputs ; output!=NULL ; output=output->link)
      {
      int current = output->record->set_value ;
      if(current != output->value)
         {
         int err ;
         int reg = output->reg - 40001 ;
         Inform("current = %d set value = %d name %s",
           current,output->record->set_value, output->record->name) ;
         if(current)
            {
            e->regs[reg] &= output->active_and_mask ;
            e->regs[reg] |= output->active_or_mask ;
            }
         else
            {
            e->regs[reg] &= output->inactive_and_mask ;
            e->regs[reg] |= output->inactive_or_mask ;
            }

//         if (output->reg==40232) 
//            {
//            int value = (e->regs[reg] == 5) ? 0 : 1024 ;
//            err = do_write(40184,value) ;  
//            }
//         else
            err = do_write(output->reg,e->regs[reg]) ; 
         if(err<0)
            return err ;
         else
            comm = 1 ;
         
     

         output->record->export = current ;
         output->value = current ;
         }
      }
   return comm ;
   }

@ Proto.
@<Prototypes@>+=
int do_output(struct ersc_struct *e) ;


@* ESRC.

@ Input. Link a register and bit in an ERSC to a DP.
@<Structures@>+=
struct input_struct
   {
   int type ;
   int reg ;
   int bit ;
   int active ;
   int match ;
   int match_mask ;
   unsigned int value ;
   int handle ;
   Dp_Record *record ;
   struct input_struct *link ;
   } ;

@ Output. Link an DP to a register and masks in an ERSC.
@<Structures@>+=
struct output_struct
   {
   int handle ;
   Dp_Record *record ;
   unsigned int value ;
   int reg ;
   int active_and_mask ;
   int active_or_mask ;
   int inactive_and_mask ;
   int inactive_or_mask ;
   struct output_struct *link ;
   } ;

@ Module.
@<Structures@>+=
struct ersc_struct
   {
   unsigned int regs[512] ;
   int read_begin ;
   int read_end ;
   Dp_Record *comm ;
   struct input_struct *inputs ;
   struct output_struct *outputs ;
   struct ersc_struct *link ;
   } ;

@ Global anchor.
@<Globals@>+=
struct ersc_struct *anchor = NULL ;
struct ersc_struct *here   = NULL ;
   
@ Add.
@<Functions@>+=
void add_ersc(int handle,char *device)
   {
   char tmp[TRAK_NAME_LEN+1],*p ;

   Inform("ersc %s %s",device,ip) ;

   // create a new record if not found
   Trace("create new ersc instance, [%s]",ip) ;
   here = (struct ersc_struct *)malloc(sizeof(struct ersc_struct)) ;
   if(here!=NULL)
      {
      int i ;


      here->read_begin = 49999 ;
      here->read_end   = 0 ;

      for(i=0 ; i<512 ; i++)
         here->regs[i] = 0 ;
    
      here->inputs = NULL ;
      here->outputs = NULL ;
      
      }
      
     dp_ioscan(handle,add_point) ;
     Inform("comm dp: %s",here->comm->name) ;
     sleep(1) ;
   }

int add_point(Dp_Record *dp)
{
  int handle = dp_record(dp) ;
  int is_input = 0 ;
  if (!strcmp(dp->io_data,"pe_lf")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 4 ;
      @<end |input|@>@;

  } else if (!strcmp(dp->io_data,"pe_rt")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 6 ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"io_i1r")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 3 ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"io_i2r")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 7 ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"io_or")) {
      int my_bit= 1<<3 ;
      @<start |output|@>@;
      
      output->reg=  40037 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"io_i1l")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 1 ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"io_i2l")) {
      @<start |input|@>@; 

      input->type = 'b' ;
      input->reg = 40035  ;
      input->bit = 5 ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"io_ol")) {
      int my_bit= 1<<1 ;
      @<start |output|@>@;

      output->reg=  40037 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"mtr_lf")) {
      int my_bit= 1<<0 ;
      @<start |output|@>@;

      output->reg=  40260 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"mtr_rt")) {
      int my_bit= 1<<0 ;
      @<start |output|@>@;

      output->reg=  40270 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"mtr_lf_r")) {
      int my_bit= 1<<8 ;
      @<start |output|@>@;

      output->reg=  40260 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"mtr_rt_r")) {
      int my_bit= 1<<8 ;
      @<start |output|@>@;

      output->reg=  40270 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;
  } else if (!strcmp(dp->io_data,"mtr_rt_bk")) {
      int my_bit= 4 ;
      @<start |output|@>@;

      output->reg=  40271 ;
      output->active_and_mask = 3 ;
      output->active_or_mask = 3 ;
      output->inactive_and_mask = 1 ;
      output->inactive_or_mask = 1 ;
  } else if (!strcmp(dp->io_data,"mtr_lf_bk")) {
      int my_bit= 4 ;
      @<start |output|@>@;

      output->reg=  40261 ;
      output->active_and_mask = 3 ;
      output->active_or_mask = 3 ;
      output->inactive_and_mask = 1 ;
      output->inactive_or_mask = 1 ;

  } else if (!strcmp(dp->io_data,"mtr_lf_flt")) {
      @<start |input|@>@; 

      input->type = 's' ;
      input->reg = 40058  ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"mtr_rt_flt")) {
      @<start |input|@>@; 

      input->type = 's' ;
      input->reg = 40082  ;
      @<end |input|@>@;
  } else if (!strcmp(dp->io_data,"reset")) {
      int my_bit= 1<<0 ;
      @<start |output|@>@;

      output->reg=  40022 ;
      output->active_and_mask = ~(my_bit) ;
      output->active_or_mask = (my_bit) ;
      output->inactive_and_mask = ~(my_bit) ;
      output->inactive_or_mask = 0 ;

  } else if (!strcmp(dp->io_data,"comm")) {
    here->comm = dp ;
  }
}
@ common code for input read start/end adjustment
@<end |input|@>=
     if(here->read_begin>input->reg)
        here->read_begin=input->reg ;
     if(here->read_end<input->reg)
        here->read_end=input->reg ; 
@ Proto.
@<Prototypes@>+=
void add_ersc(int handle,char *device) ;
int add_point(Dp_Record *dp) ;

@ adding an output, common code.
@<start |output|@>=
   struct output_struct *output ;
   output = (struct output_struct *)malloc(sizeof(struct output_struct)) ;
   
   output->link = here->outputs ;
   here->outputs = output ;
   output->handle = handle ;
   output->record = dp_pointer(handle) ;
//   output->value = 1 ;
//   output->record->export = 1 ;

@ adding an input ... common code.
@<start |input|@>=
      struct input_struct *input ;
      input = (struct input_struct *)malloc(sizeof(struct input_struct)) ;
      is_input = 1 ;

      input->link = here->inputs ;
      here->inputs  = input ;

      input->handle = handle ;
      input->record = dp_pointer(handle) ;


@* Find.
@<Functions@>+=
void find_devices(void)
   {
   here = NULL ;
   dp_devices("io:ersc",each_device) ;
   }

void release_devices(void) 
   {
   struct input_struct *in,*next_in ;
   struct output_struct *out,*next_out ;
   if (here==NULL) return ;
   
   for (in = here->inputs ; in != NULL ; in= next_in) 
     {
     next_in = in->link ;
     free(in) ;
     }
   for (out = here->outputs ; out != NULL ; out= next_out) 
     {
     next_out = out->link ;
     free(out) ;
     }
   free(here) ;
   here = NULL ;
   }
@ Proto.
@<Prototypes@>+=
void find_devices(void) ;
void release_devices(void) ;
int each_device(int handle) ;

@ Each device.
@<Functions@>+=
int each_device(int handle)
   {
   Dp_Device device ;
//   Inform("locate device %d",handle) ;
   dp_readdevice(handle,&device) ;
//   Inform("check ersc [%s]",device.device_data) ;
   if (!strcmp(device.device_data,ip)) 
     {
//     Inform("add ersc [%s]",device.device_data) ;
     add_ersc(handle,device.name) ;
     }
   }


@* Modbus

@* Modbus Read.
@<Functions@>+=
int do_read(int reg, int count, unsigned int *values) 
   {
   int offset ;
   unsigned char out[128+1] ;
   int o ; 
   unsigned char in[128+1] ;
   int i ;
   int n ;
   int pos ;
   int err ;

   offset = reg - 40001 ;
   if(offset <0)
      {
      Alert("bad register address %d",reg) ;
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
   out[o++] = 0x00 ; // unit id 
   out[o++] = 0x03 ;
   out[o++] = (offset>>8) & 0xff ;
   out[o++] = (offset>>0) & 0xff ;
   out[o++] = (count >>8) & 0xff ;
   out[o++] = (count >>0) & 0xff ;
  
   out[pos+0] = ((o-pos-2)>>8) & 0xff ; 
   out[pos+1] = ((o-pos-2)>>0) & 0xff ;

   err = request(out,o) ;
   if(err<0)
     return -3 ; 

   i = response(in) ;
   if(i<=0) {
      Alert("message response < 0 %x reg: %d count %d",i,
               reg,count) ;
      sleep(1) ;
      return -4 ;
   }

   if(in[7] == 0x03)
      {
      int total ;
      total = in[8] /2 ;
      for(n=0 ; n<total ; n++)
         {
         int content ;
         int address ;

         address = reg + n ;
         content =   in[9+2*n+0] ;
         content <<= 8 ;        
         content +=  in[9+2*n+1] ;
        
         values[n] = content ; 
   //      Trace("%5d: %5d (0x%04x)",address,content,content) ;
         }
      return n ;
      }
   else
      {
      Alert("message error %x reg: %d count %d",in[8]/2,
               reg,count) ;
      sleep(1) ;
      return -2 ;
      }

   return 0 ;
   }

@ Proto.
@<Prototypes@>+=
int do_read(int reg, int count, unsigned int *values) ;

@* Write.
@<Functions@>+=
int do_write(int reg, int value) 
   {
   int offset ;
   unsigned char out[128+1] ;
   int o ; 
   unsigned char in[128+1] ;
   int i ;
   int n ;
   int pos ;
   int err ;

   Inform("write reg %d v[0]: %d",
          reg,value) ;

   offset = reg - 40001 ;
   if(offset <0)
      {
      Alert("bad register address %d",reg) ;
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
   out[o++] = 0x00 ; // unit id 
   out[o++] = 0x06 ;
   out[o++] = (offset>>8) & 0xff ;
   out[o++] = (offset>>0) & 0xff ;
   out[o++] = (value >>8) & 0xff ;
   out[o++] = (value >>0) & 0xff ;
  
   out[pos+0] = ((o-pos-2)>>8) & 0xff ; 
   out[pos+1] = ((o-pos-2)>>0) & 0xff ;

   err = request(out,o) ;
   if(err<0) {
      Alert("request bad") ;
      return -3 ;
    }

   i = response(in) ;
   if(i<=0) {
      Alert("response bad") ;
      return -4 ;
   }

   if(in[7] == 0x06)
      {
      Trace("wrote %d to %d",value,reg) ;
      return 1 ;
      }
   else
      {
      Alert("message error %x",in[7]) ;
      sleep(1) ;
      return -2 ;
      }
   Inform("good response") ;
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

   s = comm_attach() ;
   if(s>=0)
      {
      int sent ;
      sent = write(s,cmd,len) ; 
      if(sent!=len)
         {
         // Alert("request write fails %d",sent) ;
         comm_detach() ;
         return -2 ;
         }
      }
   else
      {
      // Alert("request comm_attach fails %d",s) ;
      return -1 ;
      }

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

   m=0 ;
   msg[m  ] = '\0' ;

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
      else
         return -1 ;

      tv.tv_sec  = 0 ;
      tv.tv_usec = 20 * 1000L ;

      n = select(n,&rfds,NULL,NULL,&tv) ;

      if(n>0)
         {
         if(FD_ISSET(s,&rfds))
            { 
            unsigned char c ;
    
            if(read(s,&c,1)==1)
               {
               if(m<(128-1))
                  msg[m++] = c ;
               }
            else
               {
               comm_detach() ;
               }
            }
         }
      else if(n==0)
         {
         break ; 
         }
      else if(n<0)
         {
         Alert("select error") ;
         comm_detach() ;
         return -2 ;
         break ;
         }
      }

   return m ;
   }

@ Proto.
@<Prototypes@>+=
int response(unsigned char *cmd) ;

@* Net.
@<Globals@>+=
int net = -1 ;

@ Attach.
@<Functions@>+=
int comm_attach()
   {
   unsigned char recv[128+1] ;
   int r ;
   if (comm_status==-1) 
     {
     if (net>0) 
        {
        close(net) ;
        net = -1 ;
        }
     }

   if(net<0)
      {
      net = net_open(ip,502) ;
      
      if(net>=0)
         {
         Inform("open %s handle %d",ip,net) ;
         usleep(10000L) ;
         here->comm->export =0 ; 
         }
      else
         {
         // Alert("failed to open [%s]",ip) ;
         sleep(5) ;
         }
      }

   return net ;
   }

@ Proto.
@<Prototypes@>+=
int comm_attach() ;

@ Detach.
@<Functions@>+=
void comm_detach(void)
   {
   if(net)
      close(net) ;
   net = -1 ;
   }

@ Proto.
@<Prototypes@>+=
void comm_detach(void) ;

@* Index.
