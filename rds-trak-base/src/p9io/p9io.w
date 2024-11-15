% 
%   p9io.w -- connect networked Panther 9 I/O modules with trak 
%
%   Author: Mark Woodworth 
%   Author: Mark Olson (continued) 
%
%   History:
%      2013-04-18 -- check in (mrw) 
%      2013-04-22 -- debug -> go live (mdo) 
%      2013-09-09 -- modified from ERSC to P8IO (ahm)
%      2019-03-11 -- modified from P8IO to P9IO (ank)
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
%     (C) Copyright 2013 Numina Group, Inc.  All Rights Reserved.
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
\def\title{P9IO}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

Connect Panther 9 I/O to Trak.

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
\centerline{RCS ID: $ $Id: p9io.w,v 1.8 2024/01/17 20:37:05 rds Exp rds $}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2013 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview.
@c
static char rcsid[] = "$Id: p9io.w,v 1.8 2024/01/17 20:37:05 rds Exp rds $" ;
@<Includes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;
int main(int argc, char *argv[])
   {
   @<Initialize@>@;

   Inform("detecting devices...") ;
   find_devices() ; 

   for(here=anchor ; here!=NULL ; here=here->link,usleep(5 * 1000))
      {
      int pid = 0 ;
      Inform("fork %s read begin %d, end %d",
           here->ip,here->read_begin,here->read_end) ;
      if((pid=fork())==0)  
         {
         re_register(here) ;
         strcpy(ip,here->ip) ;
         while(1)
            {
            report(do_input(here)) ;
            report(do_output(here)) ;
            usleep(5 * 1000L) ;
            }
         }
      }
   for(;;)sleep(60) ;
   }

@ Includes.  Standard system includes are collected here.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <limits.h>

#include <rds_net.h>
#include <rds_sql.h>
#include <rds_trak.h>
#include <rds_trn.h>

@ Initialize.
@<Initialize@>+=
   {
   trn_register("p9io") ;
   Trace("%s",rcsid) ;
   signal(SIGTERM,onTerm) ;
   }

@ On Term
@<Functions@>=
void onTerm(int pid)
{
  exit(0) ;
}

@ ReRegister.
@<Functions@>+=
void re_register(struct p9io_struct *this)
   {
   trn_register(this->name) ;
   }

@ Proto.
@<Prototypes@>+=
void re_register(struct p9io_struct *this) ;

@ Report.
@<Globals@>+=
int comm_status = -1 ;
int err_count = 0 ;

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
         Inform("communications established/restored") ;
      else
         {
         Alert("communications lost") ;
         comm_detach();
         }
      comm_status = status ;
      }

   return status ;
   }

@* IO.

@ Do Input.
@d MAX_READ 45
@<Functions@>+=
int do_input(struct p9io_struct *e)
   {
   unsigned int values[512] ;
   int count, err  ;

   count = e->read_end - e->read_begin + 1 ;
   
//   Inform("count = %d check input begin: %d count %d",
//       count,e->read_begin,e->read_end ) ;
   if (count <= 0) 
      {
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
         int size = (count - start > MAX_READ) ? MAX_READ : count - start ;
         err = do_read(e->read_begin+ start,size,values+start) ;
         if (err < 0) break ;
         }
      }

   if(err<0)
      {
      Alert("do_input do_read fails with error %d",err) ;
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
         if(input->active==0)
            current = current==0 ? 1 : 0 ;
         }

      if(input->type=='m')
         {
         current = (current==input->match) ? 1 : 0 ;
         }

      if(current != input->value)
         {
         input->record->export = current ;
         input->value = current ;
         }
      }

   return 1 ;
   }

@ Proto.
@<Prototypes@>+=   
int do_input(struct p9io_struct *e) ;

@ Do Output.
@<Functions@>+=
int do_output(struct p9io_struct *e)
   {
   int comm = 0 ;

   struct output_struct *output ;
   for(output=e->outputs ; output!=NULL ; output=output->link)
      {
      int current = output->record->set_value ;
      if(current != output->value)
         {
         int err ;
         int off = get_offset(output->reg) ;
         Inform("set %s -> %d",output->record->name,current) ;
         if(current)
            {
            e->write_reg &= output->active_and_mask ;
            e->write_reg |= output->active_or_mask ;
            }
         else
            {
            e->write_reg &= output->inactive_and_mask ;
            e->write_reg |= output->inactive_or_mask ;
            }

         err = do_write(output->reg,e->write_reg) ; 
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
int do_output(struct p9io_struct *e) ;


@* Data structures.

@ Input. Link a register and bit in an ERSC to a DP.
@<Structures@>+=
struct input_struct
   {
   int type ;
   int reg ;
   int bit ;
   int active ;
   int match ;
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
struct p9io_struct
   {
   char name[32+1] ;
   char ip[32+1] ;
   int read_begin ;
   int read_end ;
   unsigned int write_reg ;  // assume a single output register
   struct input_struct *inputs ;
   struct output_struct *outputs ;
   struct p9io_struct *link ;
   } ;

@ Global anchor.
@<Globals@>+=
struct p9io_struct *anchor = NULL ;
struct p9io_struct *here   = NULL ;
   
@ Add.
@<Functions@>+=
void add_p9io(char *device, char *id, char *ip)
   {
   struct p9io_struct *this ;

   // find the p9io
   for(this=anchor ; this!=NULL ; this=this->link)
      if(0==strcmp(this->ip,ip))
         break ;
   // create a new record if not found
   if(this==NULL)
      {
      char dev_ip[32+1] ;
      char *v ;
      strncpy(dev_ip,ip,32) ;
      v=sql_getcontrol(id,"p9io") ;
      if((v!=NULL)&&(strlen(v)>0))
         strncpy(dev_ip,v,32) ;
      dev_ip[32] = '\0' ;

      Inform("create new p9io instance: %s, ip = [%s]",device,dev_ip) ;
      this = (struct p9io_struct *)malloc(sizeof(struct p9io_struct)) ;
      if(this!=NULL)
         {
         int i ;

         this->link = anchor ;
         anchor = this ;

         strncpy(this->name,device,32) ;
         this->name[32] = '\0' ;

         strncpy(this->ip,dev_ip,32) ;
         this->ip[32] = '\0' ;

         this->read_begin = INT_MAX ;
         this->read_end   = 0 ;
         this->write_reg = 0 ;

         this->inputs = NULL ;
         this->outputs = NULL ;
         }
      }
   if(this!=NULL)
      {
      add_input(this,device,"ok") ;
      add_input(this,device,"home") ;
      add_input(this,device,"lotar") ;
      add_input(this,device,"data") ;
      add_input(this,device,"label") ;
      add_input(this,device,"ribbon") ;

      add_output(this,device,"tamp") ;
      add_output(this,device,"reset") ;
      }
   else
      Alert("unable to add %s",device) ;
   }

@ Proto.
@<Prototypes@>+=
void add_p9io(char *device, char *id, char *ip) ;

@ Find a handle for a |device| and |io_data|.
@<Functions@>+=
int dp_locate(char *device, char *io_data)
   {
   int dp ;
   char name[TRAK_NAME_LEN+1] ;
   char description[TRAK_DATA_LEN+1] ;
   char dp_device[TRAK_NAME_LEN+1] ;
   char dp_io_data[TRAK_DATA_LEN+1] ;

   for(dp=0 ; dp<DP_N ; dp++)
      {
      dp_settings(dp,name,description,dp_device,dp_io_data) ;
      if(0==strcmp(device,dp_device))
         if(0==strcmp(io_data,dp_io_data))
            return dp ;
      }      

   return -1 ;
   }

@ Proto.
@<Prototypes@>+=
int dp_locate(char *device, char *io_data) ;

@ Add input.
@<Functions@>+=
void add_input(struct p9io_struct *this, char *device, char *type)
   {
   int handle = dp_locate(device,type) ;
   if(handle>=0)
      {
      struct input_struct *input ;
      input = (struct input_struct *)malloc(sizeof(struct input_struct)) ;
      if(input!=NULL)
         {
         input->link = this->inputs ;
         this->inputs = input ;

         input->type   = 'b' ; // bit read
         input->reg    = get_input_reg(type) ;
         input->bit    = get_input_bit(type) ;
         input->active = 1 ;
         input->value  = -1 ;
         input->handle = handle ;
         input->record = dp_pointer(handle) ;

         if(this->read_begin>input->reg)
            this->read_begin=input->reg ;
         if(this->read_end<input->reg)
            this->read_end=input->reg ;
         } 
      else
         Alert("could not add input device %s io_data %s",device,type) ; 
      }
   else
      Inform("could not find dp device %s io_data %s",device,type) ;
   }

@ Proto.
@<Prototypes@>+=
void add_input(struct p9io_struct *this, char *device, char *type) ;

@ Get input register.
@<Functions@>+=
int get_input_reg(char *type)
   {
   //return 412789 ;  // single word for inputs
   return( 980 + 40001 );  // single word for inputs
   }

@ Proto.
@<Prototypes@>+=
int get_input_reg(char *type) ;

@ Get input bit.
@<Functions@>+=
int get_input_bit(char *type)
   {
   if(0==strcmp(type,"ok"))     return  7 ;
   if(0==strcmp(type,"home"))   return  9 ;
   if(0==strcmp(type,"lotar"))  return 11 ;
   if(0==strcmp(type,"data"))   return  4 ;
   if(0==strcmp(type,"label"))  return 10 ;
   if(0==strcmp(type,"ribbon")) return  2 ;
   return 0 ;
   }

@ Proto.
@<Prototypes@>+=
int get_input_bit(char *type) ;

@ Add output.
@<Functions@>+=
void add_output(struct p9io_struct *this, char *device, char *type)
   {
   int handle = dp_locate(device,type) ;
   if(handle>=0)
      {
      struct output_struct *output ;
      output = (struct output_struct *)malloc(sizeof(struct output_struct)) ;
      if(output!=NULL) 
         {
         int bit = get_output_bit(type) ;

         output->link = this->outputs ;
         this->outputs = output ;

         output->reg    = get_output_reg(type) ;
         output->active_and_mask   = ~(1 << bit) ;
         output->active_or_mask    =   1 << bit ;
         output->inactive_and_mask = ~(1 << bit) ;
         output->inactive_or_mask  =  0 ;
         output->value  = -1 ;
         output->handle = handle ;
         output->record = dp_pointer(handle) ;
         }
      else
         Alert("could not add output device %s io_data %s",device,type) ;
      }
   else
      Inform("could not find dp device %s io_data %s",device,type) ;
   }

@ Proto.
@<Prototypes@>+=
void add_output(struct p9io_struct *this, char *device, char *type) ;

@ Get output register.
@<Functions@>+=
int get_output_reg(char *type)
   {
   //return 412794 ;  // single word for outputs
   return( 990 + 40001 );  // single word for outputs
   }

@ Proto.
@<Prototypes@>+=
int get_output_reg(char *type) ;

@ Get output bit.
@<Functions@>+=
int get_output_bit(char *type)
   {
   if(0==strcmp(type,"tamp"))  return 0 ;
   if(0==strcmp(type,"reset")) return 2 ;
   return 2 ;  // use reset bit for default
   }

@ Proto.
@<Prototypes@>+=
int get_output_bit(char *type) ;

@* Find.
@<Functions@>+=
void find_devices(void)
   {
   dp_devices("extern:p9io",each_device) ;
   }

@ Proto.
@<Prototypes@>+=
void find_devices(void) ;

@ Each device.
@<Functions@>+=
int each_device(int handle)
   {
   Dp_Device device ;

   dp_readdevice(handle,&device) ;
   Trace("detected p9io device: %s",device.name) ;

   parse(device.device_data) ;
   add_p9io(device.name,value("id"),value("ip")) ;
   }

@ Proto.
@<Prototypes@>+=
int each_device(int handle) ;

@* Pairs.
@<Structures@>+=
struct pair_struct
   {
   char name[32+1] ;
   char value[32+1] ;
   struct pair_struct *link ;
   } ;

@ Free.
@<Functions@>+=
void pair_free(struct pair_struct *head)
   {
   struct pair_struct *victim ;
   struct pair_struct *ondeck ;

   victim = head ;
   while(victim!=NULL)
      {
      ondeck=victim->link ;
//Inform("victim %x name %s value %s ondeck %x",
//    (int)victim,victim->name,victim->value,(int) ondeck) ;
      free(victim) ;
      victim = ondeck ;
      }
   head = NULL ;
   }

@ Proto.
@<Prototypes@>+=
void pair_free(struct pair_struct *head) ; 

@ Parse.
@<Functions@>+=
void pair_parse(struct pair_struct *head, char *text)
   {
   char name[32+1] ;
   char value[32+1] ;
   int t, n, v ;

   t = 0 ;
   while(text[t]!='\0')
      {
      // name
      n = 0 ;
      name[n  ] = '\0' ;
      while((text[t]!='\0')&&(text[t]!='='))
         {
         name[n++] = text[t++] ;
         name[n  ] = '\0' ;
         }
      if(text[t]=='=') t++ ;

      // value 
      v = 0 ;
      value[v  ] = '\0' ;
      while((text[t]!='\0')&&(text[t]!='&'))
         {
         value[v++] = text[t++] ;
         value[v  ] = '\0' ;
         }
      if(text[t]=='&') t++ ;
 
      // pair
      if((n>0)&&(v>0))
         {
         struct pair_struct *add ;
         add = (struct pair_struct *)malloc(sizeof(struct pair_struct)) ;
         if(add)
            {
            add->link = pairs ;
            pairs = add ;
            strncpy(add->name,name,32) ;
            add->name[32] = '\0' ;
            strncpy(add->value,value,32) ;
            add->value[32] = '\0' ;
            }
         else
            Alert("could not add %s=%s",name,value) ;        
         }
      }
   }

@ Proto.
@<Prototypes@>+=
void pair_parse(struct pair_struct *head, char *text) ;

@ Value.
@<Functions@>+=
char *pair_value(struct pair_struct *head, char *name)
   {
   struct pair_struct *check ;

   for(check=head ; check!=NULL ; check = check->link)
      if(0==strncmp(check->name,name,32))
         return check->value ;
   return "" ;
   }

@ Proto.
@<Prototypes@>+=
char *pair_value(struct pair_struct *head, char *name) ;

@ Anchor
@<Globals@>+=
struct pair_struct *pairs = NULL ;

@ Parse.
@<Functions@>+=
void parse(char *text)
   {
   pair_free(pairs) ;
   pairs = NULL ;
   pair_parse(pairs,text) ;
   }

@ Proto.
@<Prototypes@>+=
void parse(char *text) ;

@ Value.
@<Functions@>+=
char *value(char *name)
   {
   return pair_value(pairs,name) ;
   }

@ Proto.
@<Prototypes@>+=
char *value(char *name) ;


@* Modbus

@<Globals@>+=
char ip[32+1] ;

@ Convert register number to modbus offset.
@<Functions@>+=
int get_offset(int reg)
   {
   return (reg < 100000) ? reg - 40001 : reg - 400001 ;
   }

@ Proto.
@<Prototypes@>+=
int get_offset(int reg) ;

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

   offset = get_offset(reg) ;
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
   out[o++] = 0x01 ; // unit id, changed 00 -> 01 (ank) 
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
   if(i<0) 
     {
     Alert("invalid read response") ;
     return -4 ;
     }
   else if(i==0)
     {
     err_count++ ;
     if(err_count>5)
       {
       Alert("too many failures") ;
       err_count = 0 ;
       return -5 ;
       }
     return 0 ;
     }
   err_count = 0 ;

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
      Alert("message error in read response: 0x%2x",in[8]/2) ;
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

   offset = get_offset(reg) ;
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
   out[o++] = 0x01 ; // unit id, changed 00 -> 01 (ank)
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
   if(i<0) 
     {
     Alert("invalid write response") ;
     return -4 ;
     }
   else if(i==0)
     {
     err_count++ ;
     if(err_count>5)
       {
       Alert("too many failures") ;
       err_count = 0 ;
       return -5 ;
       }
     return 0 ;
     }
   err_count = 0 ;

   if(in[7] == 0x06)
      {
//      Inform("wrote %d to %d",value,reg) ;
      return 1 ;
      }
   else
      {
      Alert("message error in write response: 0x%2x",in[7]) ;
      return -2 ;
      }

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
   int expect_len = 0 ;

   struct MBAP_Header 
     {
     short xact ;
     short procotol ;
     short length ; // byte swapped
     unsigned char data[128] ;
     } *msg_buff ;

   msg_buff = (struct MBAP_Header*) msg ;

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
         {
         Alert("failed to attach") ;
         return -1 ;
         }

      tv.tv_sec  = 0 ;
      tv.tv_usec = 50000L ;

      n = select(n,&rfds,NULL,NULL,&tv) ;

      if(n>0)
        {
        if(FD_ISSET(s,&rfds))
          { 
          unsigned char c ;
    
          if(read(s,&c,1)==1)
            {
            if(m> 9 && expect_len ==0) 
              expect_len = ((msg_buff->length>>8) & 0xff)
                         + ((msg_buff->length & 0xff) << 8) + 6 ; 
            if(m<(128-1))
              msg[m++] = c ;
            if (m == expect_len) 
              break ;
            }
          else
            {
            Alert("read failed") ;
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

@ Net defines.
@<Defines@>+=
//#define MODBUS_PORT 502
#define MODBUS_PORT 2020

@ Attach.
@<Functions@>+=
int comm_attach()
   {
   unsigned char recv[128+1] ;
   int r ;

   while(net<0)
      {
      net = net_open(ip,MODBUS_PORT) ;
      
      if(net>=0)
         {
         Inform("open %s handle %d",ip,net) ;
         usleep(10000L) ;
         }
      else
         {
         Alert("failed to open [%s:%d]",ip,MODBUS_PORT) ;
         sleep(60) ;
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
