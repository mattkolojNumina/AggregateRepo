%   east_view.w -- thin term PnA status display
%
%   Author: Mark Woodworth 
%
%   History:
%      2018-08-15 -MRW- init
%      2024-01-19 -MRW- mod for Orgill east PnA
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
%     (C) Copyright 2018,2024 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{East Display}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
Thin term client to display Orgill East PnA data.

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
\centerline{Author: Mark Woodworth}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.351 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2018 NuminaGroup, Inc.  All Rights Reserved.}
}

@* Overview. 
This is a thin terminal client
to display PnA data for Orgill East PnA.

@c
@<Defines@>@;
@<Structures@>@;
@<Includes@>@;
@<Globals@>@;
@<Prototypes@>;
@<Functions@>@;
int
main(int argc, char *argv[])
  {
  @<Initialize@>@;

  setup() ;
  while(1)
    loop() ;
  }

@ Includes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>

#include <sys/select.h>

#include <rds_trn.h>
#include <rds_net.h>
#include <rds_sql.h>
#include <rds_trak.h>

@ Initialize.
@<Initialize@>+=
  {
  if(argc<2)
    {
    fprintf(stderr,"usage: %s <station_id>\n",argv[0]) ;
    exit(1) ;
    }

  strncpy(stationName,argv[1],32) ;
  stationName[32] = '\0' ;

  trn_register(stationName) ;

  strcpy(net_ip,get_control(stationName,"ip","172.27.13.24")) ;
  net_port = atoi(get_control(stationName,"port","10000")) ;
  Inform("net ip %s port %d",net_ip,net_port) ;
  }

@ The network parameters.
@<Globals@>+=
char net_ip[64+1] ;
int  net_port ;

@ The station parameters.
@<Globals@>+=
char stationName[32+1] ;

@ A utility function to get control data.
@<Functions@>+=
char *
get_control(char *zone, char *name, char *otherwise)
  {
  static char value[128+1] ;
  char *v ;

  value[0] = '\0' ;
   
  v = sql_getcontrol(zone,name) ;
  if((v==NULL)||(strlen(v)==0))
    strncpy(value,otherwise,128) ;
  else
    strncpy(value,v,128) ;
  value[128] = '\0' ;

  return value ;
  }

@ Proto.
@<Prototypes@>+=
char *get_control(char *zone, char *name, char *otherwise) ;

@* Queue.

@ Structure.
@<Structures@>+=
struct q_struct 
  {
  char source ;
  int  id ;
  char type ;
  char text[Q_TEXT+1] ;
  } ;

@ Size.
@<Defines@>+=
#define N_Q (32)
#define Q_TEXT (128)

@ Storage.
@<Globals@>+=
int q_head ;
int q_tail ;
struct q_struct q[N_Q] ;

@ EnQueue.  Store a transaction in the queue.
@<Functions@>+=
int 
enqueue(char source, int id, char type, char *text)
  {
  int err = 0 ;

  q_head++ ;
  if(q_head>=N_Q)
    q_head = 0 ;

  q[q_head].source = source ;
  q[q_head].id     = id ;
  q[q_head].type   = type ;
  strncpy(q[q_head].text,text,Q_TEXT) ;
  q[q_head].text[Q_TEXT] = '\0' ;

  return err ;
  }

@ Proto.
@<Prototypes@>+=
int enqueue(char source, int id, char type, char *text) ;

@ DeQueue.
@<Functions@>+=
int
dequeue(char *source, int *id, char *type, char *text)
  {
  if(q_head==q_tail)
    return 0 ;

  q_tail++ ;
  if(q_tail>=N_Q) q_tail=0 ;

  *source = q[q_tail].source ;
  *id     = q[q_tail].id ;
  *type   = q[q_tail].type ;
  strncpy(text,q[q_tail].text,Q_TEXT) ;
  text[Q_TEXT] = '\0' ;

  return 1 ;
  }

@ Proto.
@<Prototypes@>+=
int dequeue(char *source, int *id, char *type, char *text) ;

@* Message.
@<Globals@>+=
int m ;
char msg[MAX_MSG+1] ;

@ Message size.
@<Defines@>+=
#define MAX_MSG (128)

@ Handle a new message from the remote system.
@<Functions@>+=
void
handle(char *text)
  {
  if(text[0]=='H')
    {
    handle_heartbeat(text) ;
    }
  else if(text[0]=='S')
    {
    handle_scan(text) ;
    }
  else if(text[0]=='E')
    {
    handle_entry(text) ;
    }
  else if(text[0]=='B')
    {
    handle_button(text) ;
    }
  else if(text[0]=='F')
    {
    handle_fkey(text) ;
    }
  else
    Inform("handle [%s]",text) ;

  }

@ Proto.
@<Prototypes@>+=
void handle(char *text) ;

@ Heartbeat.
@<Functions@>+=
void
handle_heartbeat(char *text)
  {
  }

@ Proto.
@<Prototypes@>+=
void handle_heartbeat(char *text) ;

@ Scan.
@<Functions@>+=
void 
handle_scan(char *text)
  {
  int id ;
  char data[256+1] ;
  char type ;
  char scan[128+1] ;
  int t,d,s ;

  Inform("scan %s",text) ;
 
  t = 0 ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ; 
    }
  id = atoi(data) ;

  if(text[t]=='|') t++ ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ;
    }

  s = 0 ;
  scan[s  ] = '\0' ;
  type = ' ' ;
  for(d=0 ; d<strlen(data) ; d+=2)
    {
    char hex[2+1] ;
    char c ;

    hex[0] = data[d+0] ;
    hex[1] = data[d+1] ;
    hex[2] = '\0' ;

    c = (char)strtol(hex,NULL,16) ;

#if 0
    if(d==0)
      type = c ;
    else
#endif
      {
      scan[s++] = c ;
      scan[s  ] ='\0' ;
      }
    } 

#if 0
  Inform("text %s id %d binhex %s type %c scan %s",
         text,id,data,type,scan) ; 
#endif

  enqueue('S',id,type,scan) ;
  }

@ Proto.
@<Prototypes@>+=
void handle_scan(char *text) ;

@ Entry.
@<Functions@>+=
void 
handle_entry(char *text)
  {
  int id ;
  char data[256+1] ;
  int t,d ;

  Inform("entry %s",text) ;
 
  t = 1 ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ; 
    }
  id = atoi(data) ;

  if(text[t]=='|') t++ ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ;
    }

  enqueue('E',id,' ',data) ;
  }

@ Proto.
@<Prototypes@>+=
void handle_entry(char *text) ;

@ Button.
@<Functions@>+=
void 
handle_button(char *text)
  {
  int id ;
  char data[256+1] ;
  int t,d ;

  Inform("button %s",text) ;
 
  t = 1 ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ; 
    }
  id = atoi(data) ;

  enqueue('B',id,' ',"") ;
  }

@ Proto.
@<Prototypes@>+=
void handle_button(char *text) ;

@ Function Key.
@<Functions@>+=
void 
handle_fkey(char *text)
  {
  int id ;
  char data[256+1] ;
  int t,d ;

  Inform("fkey %s",text) ;
 
  t = 1 ;

  d = 0 ;
  data[d  ] = '\0' ;
  while((text[t]!='\0') && (text[t]!='|') && (d<(256-1)))
    {
    data[d++] = text[t++] ;
    data[d  ] = '\0' ; 
    }
  id = atoi(data) ;

  enqueue('F',id,' ',"") ;
  }

@ Proto.
@<Prototypes@>+=
void handle_fkey(char *text) ;

@* Machine.
@<Globals@>+=
int state = -1 ;
int request = 0 ;
int entry_time = 0 ;
int entry = 0 ;
char *state_name[100] ;
char error_message[1024+1] ;
int  error_return ;
char operatorName[128+1] ;

@ Elapsed.
@<Functions@>+=
int elapsed(void)
  {
  return time(NULL) - entry_time ;
  }

@ Proto.
@<Prototypes@>+=
int elapsed(void) ;

@ Set state.
@<Functions@>+=
void set_state(int ask) 
  {
  request = ask ;
  }

@ Proto.
@<Prototypes@>+=
void set_state(int ask) ;

@ Redraw.
@<Functions@>+=
void redraw(void)
  {
  if(state>=0)
    request = state ;
  state = -1 ;
  }

@ Proto.
@<Prototypes@>+=
void redraw(void) ;

@ Set error.
@<Functions@>+=
void set_error(char *text)
  {
  strncpy(error_message,text,128) ;
  error_return = state ;
  set_state(S_ERROR) ;
  }

@ Proto.
@<Prototypes@>+=
void set_error(char *text) ;

@ Status.
@<Functions@>+=
void status(void)
  {
  static time_t last = 0 ;
  char datetime[32+1] ;
  time_t stamp ;
  struct tm *now ;

  stamp = time(NULL) ;
  if(stamp==last) return ;
  last = stamp ;

  now = localtime(&stamp) ;

  thin_text(1010,   10,1035,35,"Station:",DARK_GRAY,0) ;
  thin_text(1015,  200,1035,35,stationName,WHITE,0) ;

  if(operatorName[0]!='\0')
    {
    thin_text(1020,  450,1035,35,"User:",DARK_GRAY,0) ;
    thin_text(1025,  575,1035,35,operatorName,WHITE,0) ;
    }

  sprintf(datetime,"%2d:%02d:%02d",
          now->tm_hour, now->tm_min, now->tm_sec) ;
  thin_text(1090,1700,1035,35,datetime,WHITE,0) ;

  thin_rect(1100,0,1030,1920,50,BLACK,0) ; 
  }

@ Proto.
@<Prototypes@>+=
void status(void) ;

@ Crank.
@<Functions@>+=
void machine(void)
  {
  if(request!=state)
    {
    Inform("new state %d %s",request,state_name[request]) ;
    entry_time = time(NULL) ;
    entry = 1 ;
    state = request ;
    setup() ;
    }

  switch(state)
    {
    @<States@>@;
    }
  
  entry = 0 ;
  status() ;
  }

@ Proto.
@<Prototypes@>+=
void machine(void) ;

@ *Process.

@ Init.
@<Defines@>+=
#define S_INIT (0)

@ Name the state.
@<Initialize@>+=
state_name[S_INIT] = "init" ;

@ Init State.
@<States@>+=
case S_INIT:
  {
  if(entry)
    {
    thin_clear(BLACK) ;
    thin_text(1,100,100,50,"System starting...",WHITE,0) ;
    scan_disable() ;
    }

  if(elapsed()>0)
    set_state(S_SHOW) ;
  }
break ;

@ Show.
@<Defines@>+=
#define S_SHOW (1)

@ Name the state.
@<Initialize@>+=
state_name[S_SHOW] = "show" ;

@ Show State.
@<States@>+=
case S_SHOW:
  {
  char source, type ;
  int id ;
  char operator[Q_TEXT+1] ;

  if(entry)
    {
    thin_clear(DARK_GRAY) ;
    operatorName[0] = '\0' ;
    }

  draw_zones() ;
  }
break ;

@ Error Page.
@<Defines@>+=
#define S_ERROR (99)

@ Name the state.
@<Initialize@>+=
state_name[S_ERROR] = "error" ;

@ Scan Document State.
@<States@>+=
case S_ERROR:
  {
  char source, type ;
  int id ;
  char dummy[Q_TEXT+1] ;

  if(entry)
    {
    thin_clear(RED) ;

    thin_text(1,100,100,50,"Error.",WHITE,0) ;
    thin_text(2,100,200,35,error_message,WHITE,0) ;

    thin_button(10,1350,850,400,75,50,"RETURN") ;
    scan_error() ;
    }

  if(dequeue(&source,&id,&type,dummy))
    {
    if((source=='B') && (id>=10))
      {
      set_state(error_return) ;
      }
    }

  }
break ;

@* Processing.
 
@ Setup.
@<Functions@>+=
void
setup(void)
  {
  thin_clear(DARK_GRAY) ;
  }

@ Proto.
@<Prototypes@>+=
void setup(void) ;

@ Loop.
@<Functions@>+=
void
loop(void)
  {
  int fd ;
  int n ;
  fd_set rfds ;
  struct timeval tv ;

  fd = net_attach() ;

  FD_ZERO(&rfds) ;
  FD_SET(fd,&rfds) ;
  n = fd + 1 ;

  tv.tv_sec  =      0 ;
  tv.tv_usec = 100000 ;

  n = select(n,&rfds,NULL,NULL,&tv) ;
  
  if(n>0)
    {
    if(FD_ISSET(fd,&rfds))
      {
      int err ;
      char c ;

      err = read(fd,&c,1) ;

      if(err==1)
        {
        last_response = time(NULL) ;
        if(c==EOL)
          {
          handle(msg) ;
          m = 0 ;
          msg[m  ] = '\0' ;
          machine() ;
          }
        else if(c=='\n')
          {
          // discard
          }
        else
          {
          if(m < (MAX_MSG-1))
            {
            msg[m++] = c ;
            msg[m  ] = '\0' ;
            }
          else
            Alert("overflow character %x",c) ;
          }
        }
      if(err<=0)
        {
        Alert("read error %d",err) ;
        net_detach() ;
        }
      }
    }
  else if(n==0)
    {
#if 0
    time_t now = time(NULL) ;

    if((now-last_response)>COMM_TIMEOUT)
      {
      Alert("timeout") ;
      net_detach() ;
      }
    else
#endif
      machine() ;
    }
  else
    {
    Alert("select error %d",n) ;
    net_detach() ;
    }
  }

@ Proto.
@<Prototypes@>+=
void loop(void) ;

@ Last response.
#endif
@<Globals@>+=
time_t last_response ;

@ End of Line, communication timeout
@<Defines@>+=
#define EOL '\r' 
#define COMM_TIMEOUT (300)

@* Zones.
@<Defines@>+=
#define MAX_ZONES (20)

@ Zone Structure.
@<Structures@>+=
struct zone_struct
  {
  char name[32+1] ;
  int type ;
  int handle ;
  int x ;
  int box ;
  int state ;
  int fault ;
  char lpn[32+1] ;
  } ;

@ Zones
@<Globals@>+=
struct zone_struct zones[MAX_ZONES] ;
int n_zone = 0 ;

@ Types.
@<Defines@>+=
#define ROLLER_ZONE (1)
#define BELT_ZONE (2)
#define DIVERT_ZONE (3)
#define OTHER_ZONE (0)

@ Add Zone.
@<Functions@>+=
int
add_zone(char *name, int type) 
  {
  int z ;

  z = n_zone ;

  strncpy(zones[z].name,name,32) ;
  zones[z].name[32] = '\0' ;
  zones[z].type = type ;
  zones[z].handle = dp_handle(zones[z].name) ; 

  Inform("%d %s %d",z,zones[z].name,zones[z].handle) ;

  n_zone++ ;

  return z ;
  }

@ Proto.
@<Prototypes@>+=
int add_zone(char *name, int type) ;

@ Initialize zones.
@<Initialize@>+=
     add_zone("z1020c-2",ROLLER_ZONE) ;
s1 = add_zone("z1020c-3",ROLLER_ZONE) ;
     add_zone("z1020c-4",ROLLER_ZONE) ;

p1 = add_zone("z1020d-1",BELT_ZONE) ;
     add_zone("z1020d-2",BELT_ZONE) ;
p2 = add_zone("z1020d-3",BELT_ZONE) ;
s2 = add_zone("z1020d-4",BELT_ZONE) ;

d =  add_zone("z1020e-1",DIVERT_ZONE) ;

     add_zone("z1020f-1",ROLLER_ZONE) ;
     add_zone("z1020f-2",ROLLER_ZONE) ;
     add_zone("z1020f-3",ROLLER_ZONE) ;
     add_zone("z1020f-4",ROLLER_ZONE) ;

b =  add_zone("cp1-bander",OTHER_ZONE) ;

add_zone("z1020g-1",ROLLER_ZONE) ;

@ Conveyor
@<Defines@>+=
#define CONVEYOR_Y (75)

@ Zones.
@<Globals@>+=
int s1,p1,p2,s2,d,b ;

@ Draw Zones.
@<Functions@>+=
void
draw_zones(void)
  {
  int gap = 10 ;
  int l = 1920 / (n_zone+1) - gap ;
  int w = l * 4 / 5 ;
  int x,y,z ;
  int bl = 3 * l / 4 ;
  int bw = 3 * w / 4 ;

  for(z=0 ; z<n_zone ; z++)
    {
    x = l/2 + z*(l+gap) ;
    y = CONVEYOR_Y ;
    zones[z].x = x ;

    if(zones[z].type>0)
      {
      dp_registerget(zones[z].handle, 1,&(zones[z].state)) ;
      zones[z].fault = (zones[z].state==31) ;
//Inform("%d %d %d",z,zones[z].state,zones[z].fault) ;
      dp_registerget(zones[z].handle,0,&(zones[z].box)) ;
      if(zones[z].box!=-1)
        {
        if(zones[z].lpn[0] == '\0')
          {
          strncpy(zones[z].lpn,getLPN(zones[z].box),32) ;
          zones[z].lpn[32] = '\0' ;
          }
        }
      else
        zones[z].lpn[0] = '\0' ;
      }
 
    // box
    if(zones[z].type>0)
      {
      if(zones[z].box!=-1) 
        { 
        thin_text(z,x+l-bl,   y+2*gap, 10,zones[z].lpn,WHITE,0) ;
        thin_rect(z,x+l-gap-bl,y+gap,bl,bw,BROWN,1) ; 
        }
      else
        {
        thin_text(z,x+l-bl,   y+2*gap, 10,"",BROWN,0) ;
        thin_rect(z,x+l-gap-bl,y+gap,0,0,BROWN,0) ;
        }
      }

    // zone
    if(zones[z].fault)
      thin_rect(MAX_ZONES+z,x,y,l,w,RED,1) ;
    else if(zones[z].type==ROLLER_ZONE)
      thin_rect(MAX_ZONES+z,x,y,l,w,LIGHT_GRAY,1) ;
    else if(zones[z].type==BELT_ZONE)
      thin_rect(MAX_ZONES+z,x,y,l,w,GRAY,1) ;
    else if(zones[z].type==DIVERT_ZONE)
      thin_rect(MAX_ZONES+z,x,y,l,w,MED_GREEN,1) ;
    else if(zones[z].type==OTHER_ZONE)
      thin_rect(MAX_ZONES+z,x,y,l,w,BLUE,1) ;
  

    // names
    thin_text(MAX_ZONES+z,x,y+w+gap/2,15,zones[z].name,WHITE,0) ;

    // scan 1
    if(z==s1)
      thin_text(2*MAX_ZONES+z,x,20,20,"Scan In",WHITE,0) ;

    // print 1
    if(z==p1)
      thin_text(3*MAX_ZONES+z,x,20,20,"Printer 1",WHITE,0) ;

    // print 2 
    if(z==p2)
      thin_text(4*MAX_ZONES+z,x,20,20,"Printer 2",WHITE,0) ;

    if(z==s2)
      thin_text(5*MAX_ZONES+z,x,20,20,"Scan Out",WHITE,0) ;

    
    }
  }

@ Proto.
@<Prototypes@>+=
void draw_zones(void) ;

@ Get LPN
@<Functions@>+=
char *
getLPN(int box)
  {
  static char lpn[32+1] ;
  int err ;

  strcpy(lpn,"?") ;
  err = sql_query("SELECT value FROM boxData "
                 "WHERE box=%03d AND name='lpn' ",box) ;
  if(!err)
    if(sql_rowcount())
      if(sql_get(0,0))
        strncpy(lpn,sql_get(0,0),32) ;
  lpn[32] = '\0' ;
  
  return lpn ;
  }

@ Proto.
@<Prototypes@>+=
char * getLPN(int box) ;
  
@* Thin.

@ Send.
@<Functions@>+=
void thin_send(char *msg)
   {
   int display ;
   int len, sent ;

//Trace("out [%s]",msg) ;

   len = strlen(msg) ;
   
   display = net_attach() ;
   if(display)
      {
      sent = write(display,msg,len) ;
//Inform("len %d sent %d",len,sent) ;
      if(sent!=len)
         {
         Alert("display write fails %d",sent) ;
         net_detach() ;
         }
      }
   else
     Alert("display could not be opened") ;
   }

@ Proto.
@<Prototypes@>+=
void thin_send(char *msg) ;

@ Colors
@<Defines@>+=
#define DARKEST_GRAY 0x10,0x10,0x10
#define DARK_GRAY 0x40,0x40,0x40
#define GRAY      0x80,0x80,0x80
#define LIGHT_GRAY 0xC0,0xC0,0xC0
#define LIGHTER_GRAY 0xD0,0xD0,0xD0
#define WHITE     0xFF,0xFF,0xFF
#define RED       0xFF,0x00,0x00
#define GREEN     0x00,0xFF,0x00
#define DARK_GREEN 0x00,0x40,0x00
#define YELLOW    0xFF,0xFF,0x00
#define BROWN     0x99,0x66,0x33
#define AMBER     0xe6,0x5C,0x00
#define DARK_BLUE 0x00,0x00,0xC0
#define BLUE      0x40,0x40,0xC0
#define LIGHT_BLUE 0x80,0x80,0xFF
#define BLACK     0x00,0x00,0x00
#define MED_GREEN 0x28,0xBF,0x24
#define ORANGE    0xCC,0x7A,0x00

@ Clear screen.
@<Functions@>+=
void thin_clear(int r, int g, int b)
   {
   char msg[128+1] ;

   sprintf(msg,"C$00%02x%02x%02x\r",b,g,r) ;
   thin_send(msg) ;
   }

@ Proto.
@<Prototypes@>+=
void thin_clear(int r, int g, int b) ;

@ Thin Button.
@<Functions@>+=
void
thin_button(int tag,
           int left, int top,
           int width, int height,
           int size,char *text)
  {
  char msg[128+1] ;

  sprintf(msg,"B%d|%d|%d|%d|%d|%d|%s\r",
          tag,left,top,width,height,size,text) ;
  thin_send(msg) ;
  }

@ Proto.
@<Prototypes@>+=
void thin_button(int tag, int left, int top, int width, int height, 
                 int size, char*text) ;

@ Thin Entry.
@<Functions@>+=
void
thin_entry(int tag,
           int left, int top,
           int width, int height,
           int size)
  {
  char msg[128+1] ;

  sprintf(msg,"E%d|%d|%d|%d|%d|%d\r",
          tag,left,top,width,height,size) ;
  thin_send(msg) ;
  }

@ Proto.
@<Prototypes@>+=
void thin_entry(int tag, int left, int top, int width, int height, int size) ;

@ Thin Focus.
@<Functions@>+=
void
thin_focus(int tag)
  {
  char msg[128+1] ;

  sprintf(msg,"Y%d\r", tag) ;

  thin_send(msg) ;
  }

@ Proto.
@<Prototypes@>+=
void thin_focus(int tag) ;

@ Thin Text
@<Functions@>+=
void thin_text(int tag,
               int left, int top,
               int size,
               char *text,
               int r, int g, int b,
               int width )
   {
   char msg[128+1] ;

   sprintf(msg,"T%d|%d|%d|%d|%s|$00%02x%02x%02x|%d\r",
           tag,left,top,size,text,b,g,r,width) ;
   thin_send(msg) ;
   }

@ Proto.
@<Prototypes@>+=
void thin_text(int tag, int left, int top, int size,
               char *text, int r, int g, int b,
               int width ) ;

@ Thin Rect.
@<Functions@>+=
void thin_rect(int tag,
               int left, int top,
               int wide, int high,
               int r, int g, int b,
               int thick)
   {
   char msg[128+1] ;

   sprintf(msg,"R%d|%d|%d|%d|%d|$00%02x%02x%02x|%d|0\r",
           tag,left,top,wide,high,b,g,r,thick) ;
   thin_send(msg) ;
   }

@ Proto.
@<Prototypes@>+=
void thin_rect(int tag,
               int left, int top,
               int wide, int high,
               int r, int g, int b,
               int thick) ;

@ Thin Serial.
@<Functions@>+=
void 
thin_serial(int tag, 
            char *dev, 
            int baud, char parity, int databits, int stopbits,
            int endchar) 
  {
  char msg[128+1] ;

  sprintf(msg,"S%d|%s|%d|%c|%d|%d|%d\r",
          tag,dev,baud,parity,databits,stopbits,endchar) ;
  thin_send(msg) ;
  }

@ Proto.
@<Prototypes@>+=
void
thin_serial(int tag, 
            char *dev, 
            int baud, char parity, int databits, int stopbits,
            int endchar) ;

@ Thin Xmit.
@<Functions@>+=
void
thin_xmit(int tag, char *text)
  {
  char msg[128+1] ;

  sprintf(msg,"X%d|%s\r",
          tag,text) ;
  thin_send(msg) ;
  }

@ Proto.
@<Prototypes@>+=  
void
thin_xmit(int tag, char *text) ;

@* Scan.

@ Scan enable.
@<Functions@>+=
void
scan_enable(void)
  {
  thin_xmit(0,"45") ;
  }

@ Proto.
@<Prototypes@>+=
void scan_enable(void) ;

@ Scan disable
@<Functions@>+=
void
scan_disable(void)
  {
  thin_xmit(0,"44") ;
  }

@ Proto.
@<Prototypes@>+=
void scan_disable(void) ;

@ Scan Error.
@<Functions@>+=
void
scan_error(void)
  {
  thin_xmit(0,"46") ;
  }

@ Proto.
@<Prototypes@>+=
void scan_error(void) ;

@ Scan Beep.
@<Functions@>+=
void
scan_beep(void)
  {
  thin_xmit(0,"01") ;
  }

@ Proto.
@<Prototypes@>+=
void scan_beep(void) ;

@* Network Connection.
A global holds the handle to the port.  It is initialized to
an illegal handle.
@<Globals@>+=
int net = -1 ;


@ The function |net_attach()| 
opens the connection to the remote socket if it is not
already open.
This function sleeps and retries if the port cannot be opened.
This function should be called every time the port is about to be used.
@<Functions@>+=
int net_attach(void) 
   {
   extern int errno ;

   while(net<0)
      {
      net = net_open(net_ip,net_port) ;
   
      if(net<0)
         {
         Alert("Failed to open %s:%d",net_ip,net_port) ;
         sleep(10) ;
         }
      else 
         {
         Trace("opened %s:%d handle %d",net_ip,net_port,net) ;
         sleep(1) ;
         state = -1 ;
         setup() ;
         }
      }
   return net ;
   }

@ A second function closes the port and resets the handle 
to an illegal value.  This is called in the event of an error which
indicates a probable connection loss.
This will trigger a reconnection on the next |net_attach()| call.
@<Functions@>+=
void net_detach(void)
   {
   if(net>=0)
      close(net) ;
   net = -1 ;
   }

@ Proto.
@<Prototypes@>+=
int net_attach(void) ;
void net_detach(void) ;

@* Index.

