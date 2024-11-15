%   traksh.web
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
\def\title{Track Core Structures --- traksh a shell}
%
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This is the main user interface for trak. Both a 
PERL and C API are available, but
I foresee the main access to the trak engine for the experienced programmer
as the traksh (a command shell to access the trak controller).
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

\centerline{RCS Date $ $Date:$ $}
}
%
\def\botofcontents{\vfill
\centerline{\copyright 1993 Numina Systems Corporation.  
All Rights Reserved.}
}
\def\dot{\qquad\item{$\bullet$}}
@* Introduction. In the TRAK 3 core drivers become RTAI kernel modules. In
this way strict polling rates can be maintained. In the TRAK 3 system, I/O
drivers drive the action in the background. An I/O driver is simply 
structured as follows:


(Initially, this driver is a program in user space.)
@c

@<Definitions@>@;
@<Prototypes@>@;
Cmd cmds[] = {
  @<Shell Commands@>@;
  {NULL,NULL}
} ;
Ex_Engine *e ;
#define MAX_WORDS 512
char *compile[MAX_WORDS] ; /* list of strings for compilation */
int compile_count = 0 ;
char *compile_word = 0 ;
int compile_flag = 0 ;
char compile_desc[36] ;
int use_rl = 1 ;
@<Methods@>@;

char PC ;
char *UP ;
char *BC ;

int main(int argc, char **argv)
{
  char c,*p ;
  char word[80] ;
  strList *list,*next ;
  int i,count ;
  @<Initialize@>@;
  for (list=NULL,p = word;;) @<Process Input@>@;
  
}

@ Setup compile array (clear it).
@<Init...@>=
for (i = 0 ; i< MAX_WORDS ; i++) compile[i] = 0 ;

@ The includes.
@<Definitions@>=
#include <stdio.h>
#include <curses.h>
#include <term.h>
#include <sys/types.h>
#include <sys/file.h>
#include <sys/stat.h>
#include <sys/errno.h>
#include <readline/readline.h>
#include <readline/history.h>  
#include <rds_trak.h>

@ Constants.
@<Definitions@>+=
#define EOS '\0' 

@ Initialize.
@<Init...@>=


e = ex_newengine() ;

@ Check how we were run.
@<Initialize@>=

{
  if (argc == 3) {
    if (!strcmp(argv[1],"-f")) {
      stdin = freopen(argv[2],"r",stdin) ;
      printf("RUN SCRIPT:\n\t %s\n",argv[2]) ;
      use_rl = 0 ;
    }
    
  }

  if (use_rl) rl_initialize() ;
}

@* Input Parsing. Input is put into a string list. The list is then
converted to an array prior to processing.

@<Definitions@>=

typedef struct _sList {
  struct _sList *link ;
  char *text ;
} strList ;


@ Functions. We replace |getc| used in the prior versions with a version
which uses the GNU |getline| function. 
@<Methods@>=
char getachar(FILE *fp)
{
  char result ;
  static char *line ;
  size_t len ;
  static int last_point= -1  ;
  if (last_point!=-1) {
    result = line[last_point] ;
    if (line[last_point] == '\n' || line[last_point] == EOS) {
      last_point = -1 ;
      if (line!=NULL) free(line) ;
      result = '\n' ;
    }
    if (last_point != -1) last_point++ ;
    return result ;
  }
  /*| else |*/
  do {
    line = readline("% ") ;
  }  while (strlen(line) == 0)  ;

  add_history(line) ;
  if (line == NULL) {
    exit(0) ;
  }
  
  last_point = 1 ;
  return line[0] ;
}
@ Input processing (one char at at time).
@<Process Input@>=
{
  static int commented = 0 ;
  if (!use_rl) c = getchar() ;
  else 
    c = getachar(stdin) ;
  if (c == EOF) {
    exit(0) ;
  }
  if (c=='#') {
    commented = 1 ;
    continue ;
  }
  if (c == '\n') {
    commented = 0 ;
    if (p!= word) @<Add Word...@>@;
    if (list == NULL) continue ;
    @<Evaluate@>@;
    continue ;
  }
  if (commented) continue ;
 
  if (isspace(c) && (p != word)) {
    @<Add Word to List@>@;
    continue ;
  }
  
  if (!isspace(c)) {
    *p++ = c ;
    *p = EOS ;
  }
      
}

@ Copy out word and reset
@<Add Word to List@>=
{
  next = (strList *) malloc(sizeof(strList)) ;
  next->text = malloc(strlen(word) + 1) ;
  strcpy(next->text,word) ;
  next->link = list ;
  list = next ;
  p = word ;

  
}

@ Now evaulate
@<Evaluate@>=
{
  int skip = 0 ;

  @<Reverse List@>@;

  
  while (list!=NULL) {

    next = list->link ;
    if (skip) {
      skip -- ;
      free(list) ; 
      list = next ;
      continue ;
    }
    @<Do Command@>@;
    free(list) ;
    list = next ;
  }
  list = 0 ;
}

@
@<Reverse List@>=
{
  strList *other ;
  other = NULL ;
  while(list != NULL) {
    next = list->link ;
    list->link = other ;
    other =list ;
    list = next ;
  }
  list = other ;
}
@* Processing.
@<Do Command@>=
   {
   int j ;
   int done = 0 ;

   if (compile_flag) 
      {
      if (!strcmp(list->text,";")) 
         {
         exendcompile(NULL) ;
         compile_flag = 0 ;
         done = 1 ;
         }
      else 
         {
         done = 1 ;

         compile[compile_count] = malloc(strlen(list->text)+1) ;
         strcpy(compile[compile_count],list->text) ;
         compile_count++ ;
#if 0
printf("added [%s]\n",list->text) ;
#endif
         }
      }
   else 
      {
      for (j=0 ; cmds[j].text != NULL ; j++) 
         {
         if (!strcmp(cmds[j].text,list->text)) 
            {
	    skip =  cmds[j].cmd(next) ;
            done = 1 ;
            }
         }
      if (!done && (ex_now(e,list->text) == NIL)) 
         {
         int err ;
         err = system(list->text) ;
         }
      }
   }

@* Shell Commands. First setup command list.
@<Prototypes@>=

typedef int (*topCmd)(strList *list) ;

typedef struct {
  char *text ;
  topCmd cmd ;
} Cmd ;

@ dplist. To enter each commmand, add prototype.
@<Prototypes@>=
int dplist(strList *next) ;
int devlist(strList *next) ;
int drvlist(strList *next) ;
int dpnew(strList *next) ;
int trakexit(strList *next) ;
int dpinit(strList *next) ;
int exinit(strList *next) ;
int mbinit(strList *next) ;
int evinit(strList *next) ;
int bxinit(strList *next) ;
int rpinit(strList *next) ;
int trakinit(strList *next) ;


int drvnew(strList *next) ;
int devnew(strList *next) ;
int mbpoll(strList *next) ;
int mbpost(strList *next) ;
int mbinit(strList *next) ;
int exshow(strList *next) ;
int exdot(strList *next) ;
int exstartcompile(strList *next) ;
int exdecompile(strList *next) ;
int exwords(strList *next) ;
int dpaddleading(strList *next) ;
int dpaddtrailing(strList *next) ;
int dpshow(strList *next) ;

int traceon(strList *next) ;
int traceoff(strList *next) ;
int force(strList *next) ;
int forceoff(strList *next) ;
int countertype(strList *next) ;

int tkstart(strList *next) ;
int tkstop(strList *next) ;
int tktest(strList *next) ;

int rpnew(strList *next) ;
int rpdev(strList *next) ;
int rpdrv(strList *next) ;
int rpshow(strList *next) ;
int make_const(strList *next) ;

int rplist(strList *next) ;

int load_edge(strList *next) ;
int copy_edge(strList *next) ;
int load_evt(strList *next) ;

int makefifo(strList *next) ;
int makelifo(strList *next) ;
int makearray(strList *next) ;

@ Add to the array.
@<Shell Commands@>=
{"quit",trakexit},
{"X",trakexit},
{"ZZ",trakexit},
{"dplist", dplist},
{"drvlist",drvlist},
{"devlist",devlist},
{"dpnew",  dpnew},
{"rpnew",  rpnew},
{"rpdrv",  rpdrv},
{"rpdev",  rpdev},
{"rpshow", rpshow},
{"exit",   trakexit},
{"dpinit", dpinit},
{"exinit", exinit},
{"rpinit", rpinit},
{"bxinit", bxinit},
{"mbinit", mbinit},
{"evinit", evinit},
{"trakstart",tkstart},
{"trakstop",tkstop},
{"traktest",tktest},
{"trakinit",trakinit},
{"traceon", traceon},
{"traceoff",traceoff},
{"force",force},
{"forceoff", forceoff},
{"setcounter", countertype},
{"drvnew", drvnew},
{"devnew", devnew},
{"mbpost",mbpost},
{"mbinit",mbinit},
{".s",exshow},
{".",exdot},
{":",exstartcompile},
{"see",exdecompile},
{"words",exwords},
{"dpmkleading",dpaddleading},
{"dpmktrailing",dpaddtrailing},
{"dpshow",dpshow},
{"rplist",rplist},
{"mkconst",make_const},
{"load_edge",load_edge},
{"copy_edge",copy_edge},
{"load_evt",load_evt},
{"mkfifo",makefifo},
{"mklifo",makelifo},
{"mkarray",makearray},
@ define the function.
@<Methods@>=
int trakexit(strList *next) 
{
  exit(0) ;
}
int listdp(int handle)
{
  Dp_Export data ;
  char name[TRAK_NAME_LEN+1],
       dev[TRAK_NAME_LEN+1],
       io[TRAK_DATA_LEN+1],
       description[TRAK_DATA_LEN+1] ;
  
  dp_read(handle,&data) ;
  dp_settings(handle,name,description,dev,io) ;

  printf("%s: %d ctr: %d ctn: %d\n",name,data.value,data.counter,
	 data.carton) ;
  printf("\t%s\n\tdev: %s io: %s\n",description,dev,io) ;
}
int dplist(strList *next)
{
  dp_scanall(listdp) ;
  return 0 ;
}
int make_const(strList *next)
{
  char *name,  *value ;
  name = next->text ;
  next = next->link ;
  if (next == NULL) {
    printf("usage: mkconst name value\n") ;
    return 1 ;
  }
  value = next->text ;
  next = next->link ;
  const_set(name,atoi(value),0) ;
  return 2 ;
}
int dpnew(strList *next)
{
  int ctr ;
  char *name,*dev,*io ;
  char description[40] ;
  
  ctr = 0 ;
  if (next == NULL) @<|dpnew| usage@>@;
  ctr++ ;
  name = next->text ;
  next = next->link ;
  

  if (next == NULL) @<|dpnew| usage@>@;
  ctr++ ;
  dev = next->text ;
  next = next->link ;
  
  if (next == NULL) @<|dpnew| usage@>@;
  ctr++ ;
  io = next->text ;
  next = next->link ;

  description[0] = EOS ;
  while(next != NULL) {
    ctr++ ;
    strcat(description,next->text) ;
    strcat(description," ") ;
    next = next->link ;
  }
  dp_new(name,dev,io,description) ;
  printf("added dp: %s %s %s [%s]\n",name,dev,io,description) ;
  return ctr ;
  
}

@
@<|dpnew| usage@>=
{
  printf("usage: dpnew name dev io description\n") ;
  return ctr ;
}

@
@<Methods@>=
int dpinit(strList *next)
{
  printf("dp cleared\n") ;
  dp_init() ;
  return 0 ;
}
int exinit(strList *next)
{
  printf("code cleared\n") ;
  ex_init() ;
  return 0 ;
}
int mbinit(strList *next)
{
  printf("mb cleared\n") ;
  mb_init() ;
  return 0 ;
}
int evinit(strList *next)
{
  printf("ev cleared\n") ;
  ev_init() ;
  return 0 ;
}
int bxinit(strList *next)
{
  printf("bx cleared\n") ;
  bx_init() ;
  return 0;
}
int rpinit(strList *next)
{
  printf("rp cleared\n") ;
  rp_init() ;
  return 0;
}
int trakinit(strList *next)
{
  trak_stop() ;
  usleep( 100 * 1000 ) ;
  mb_init() ;
  rp_init() ;
  ev_init() ;
  dp_init() ;
  bx_init() ;
  ex_init() ;
  const_init() ;
  q_init() ;
  printf("trak re-initialized\n") ;
  return 0 ;
}
int tkstop(strList *next) 
{
  trak_stop() ;
  return 0;
}
int tkstart(strList *next)
{
  trak_start() ;
  return 0 ;
}
int tktest(strList *next)
{
  printf("Trak test = %d\n",trak_test()) ;
  return 0;
}
int drvnew(strList *next)
{
  char *name ;
  int ctr ;
  char description[80] ;
  if (next == NULL) {
    printf("drvnew driver [desription]\n") ;
    return 0 ;
  }
  ctr = 1 ;
  name = next->text ;
  next = next->link ;

  for (description[0] = EOS ; next != NULL ; next=next->link) {
    ctr++ ;
    strcat(description,next->text) ;
    strcat(description," ") ;
  }

  printf("add driver: %s\n",name) ;

  dp_adddriver(name,description) ;
  
  return ctr; 
}


int devnew(strList *next)
{
  int ctr ;
  char *name,*driver,*io ;
  char description[40] ;
  
  ctr = 0 ;
  if (next == NULL) @<|devnew| usage@>@;
  ctr++ ;
  name = next->text ;
  next = next->link ;
  

  if (next == NULL) @<|devnew| usage@>@;
  ctr++ ;
  driver = next->text ;
  next = next->link ;
  
  if (next == NULL) @<|devnew| usage@>@;
  ctr++ ;
  io = next->text ;
  next = next->link ;

  description[0] = EOS ;
  while(next != NULL) {
    ctr++ ;
    strcat(description,next->text) ;
    strcat(description," ") ;
    next = next->link ;
  }
  dp_adddevice(name,description,driver,io) ;
  printf("added driver: %s %s %s [%s]\n",name,driver,io,description) ;
  return ctr ;
  
}

@
@<|devnew| usage@>=
{
  printf("usage: devnew name dev io description\n") ;
  return ctr ;
}

@ Post a message.
@<Methods@>=
int mbpost(strList *next)
{
  int ctr = 0 ;
  int i,n,*arg ;
  
  if (next == NULL) {
    printf("usage: mbpost n x1 .... xn (n args)\n") ;
    return ctr ;
  }
  n = atoi(next->text) ;
  arg = malloc(sizeof(int) * n) ;
  bzero(arg,sizeof(int) * n) ;
  for (i=0 ; i < n ; i++) {
    ctr++ ;
    if (next == NULL) {
      printf("usage: mbpost n x1 .... xn (n args)\n") ;
      free(arg) ;
      return ctr ;
    }
    arg[i] = atoi(next->text) ;
    next = next->link ;
  }
  

  mb_post(n,arg) ;
  free(arg) ;
  return ctr ;
}

@
@<Methods@>=
int exshow(strList *next)
{
  ex_showengine(e) ;
  return 0 ;
}
int exdot(strList *next)
{
  Ex_Stack *s ;
  s = &(e->stack) ;
  if (s->depth == -1) printf("Empty\n") ;
  else printf("%u (%x)\n",s->data[s->depth],s->data[s->depth]) ;
  ex_now(e,"drop") ;
  return 0 ;
}

@ Start compiling a word.
@<Methods@>=
int exstartcompile(strList *next)
   {
   int i,ctr ;
   char name[20] ;
   char desc[140] ;

   if (next == NULL) 
      {
      printf("need word name\n") ;
      return 0 ;
      }
   ctr = 1 ;
   if (compile_word) 
      free(compile_word) ;
   compile_word = malloc(strlen(next->text)+1) ;
  
   strcpy(compile_word,next->text) ;
   next = next->link ;
  
   desc[0] = EOS ;
   while(next != NULL) 
      {
      ctr++ ;
      strcat(desc,next->text) ;
      strcat(desc," ") ;
      next = next->link ;
      }

   strncpy(compile_desc,desc,35) ;
   compile_desc[35] = EOS ;

   for (i=0 ; i < MAX_WORDS ; i++) 
      {
      if (compile[i]) 
         free(compile[i]) ;
//      compile[i] = 0 ;
      }
   compile_count = 0 ;  
   compile_flag = 1 ;
   }

int exendcompile(strList *next)
{
  int id = ex_shellcompile(e,compile_word,compile_desc,compile_count,compile) ;
  printf("word %s -> %d\n",compile_word,id) ;
  return 0 ;
}
@
@<Methods@>=
int decompile_print(char *cmd, int data)
{
  if (data)  printf(" [%s](%d) ",cmd,data) ;
  else printf(" [%s] ",cmd) ;
  return 1; 
}
int exdecompile(strList *next)
{
  int statement ;
  char name[30],desc[40] ;
  int foo ;
  if (next == NULL) {
    printf("usage: decompile cmd\n") ;
    return 0 ;
  }
  statement = ex_getstatement(next->text) ;
  if (statement == NIL) {
    printf("%s: not found\n",next->text) ;
    return 1 ;
  }
  ex_readstate(statement,name,desc,&foo) ;
  printf("%s: %s\n",name,desc) ;
  ex_decompile(statement,decompile_print) ;
  printf("\n") ;
  return 1;
}

@
@<Methods@>=
int exwords(strList *next)
{
  int i ;
  char name[40],description[40] ;
  int foo ;
  printf("traksh level:\n") ;
  for (i=0 ; cmds[i].text != NULL ; i++) 
    printf("%s  ",cmds[i].text) ;
  printf("; ") ;
  
  printf("\n\ncore actions:\n") ;
  ex_printcore() ;
  printf("\n\ndefined statements\n") ;
  for (i=0 ; i < (EX_N/10) ; i++) {
    
    if (ex_readstate(i,name,description,&foo) != NIL) {
      if (!strlen(name)) continue ;
      printf("%s: %s\n",name,description) ;
    }
  }
}


@ Now given that number, assign it to a leading edge.
@<Methods@>=
int dpaddleading(strList *next)
{
  char *dp ;
  char *statement, desc[TRAK_DATA_LEN+1] ;
  int state_h,dp_h,list_h ;
  int ctr ;
  
  ctr = 0 ;
  if (next == NULL) {
    printf("usage: dpmkleading dp wordname  description\n") ;
    return ctr ;
  }
  ctr++ ;
  dp = next->text ;
  next = next->link ;

  if (next == NULL) {
    printf("usage: dpmkleading dp wordname listname description\n") ;
    return ctr ;
  }

  ctr++ ;
  statement = next->text ;
  next = next->link ;
  
  
  desc[0] = EOS ;
  while (next != NULL) {
    ctr++ ;
    strcat(desc,next->text) ;
    strcat(desc," ") ;
    next = next->link ;
  }
  dp_h = dp_handle(dp) ;
  if (dp_h == NIL) {
    printf("data point %s not found\n",dp) ;
    return ctr ;
  }
  state_h = ex_getstatement(statement) ;
  if (state_h != NIL) {
    list_h = ex_newlist(desc,state_h) ;
    dp_addleading(dp_h,list_h) ;
  }
  else {
    printf("command statement %s not found\n",statement) ;
  }
  
  return ctr ;

}

@
@<Methods@>=
int dpaddtrailing(strList *next)
{
  char *dp ;
  char *statement, desc[TRAK_DATA_LEN+1] ;
  int state_h,list_h,dp_h ;
  int ctr ;
  
  ctr = 0 ;
  if (next == NULL) {
    printf("usage: dpmktrailing dp wordname listname description\n") ;
    return ctr ;
  }
  ctr++ ;
  dp = next->text ;
  next = next->link ;

  if (next == NULL) {
    printf("usage: dpmkleading dp wordname listname description\n") ;
    return ctr ;
  }

  ctr++ ;
  statement = next->text ;
  next = next->link ;
  

  
  desc[0] = EOS ;
  while (next != NULL) {
    ctr++ ;
    strcat(desc,next->text) ;
    strcat(desc," ") ;
    next = next->link ;
  }
  dp_h = dp_handle(dp) ;
  if (dp_h == NIL) {
    printf("data point %s not found\n",dp) ;
    return ctr ;
  }
  state_h = ex_getstatement(statement) ;
  if (state_h != NIL) {
    list_h = ex_newlist(desc,state_h) ;
    dp_addtrailing(dp_h,list_h) ;

  }
  else {
    printf("command statement %s not found\n",statement) ;
  }
  
  return ctr ;
}

@
@<Methods@>=

int print_list(Ex_List *list)
{
  char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
  int foo ;
  printf("\t\t%s  (%d)\n",list->description,list->statement) ;
  ex_readstate(list->statement,name,desc,&foo) ;
  printf("\t\t\t%s:%s\n",name,desc) ;
}
void ev_print(int which,int n,int p,unsigned trigger, int state,int a,int b,int c,int d) 
{
  char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
  int foo ;
  ex_readstate(state,name,desc,&foo) ;
  printf("[%d] At %u run %s (%s) a: %d b: %d c: %d d: %d\n",which,
	 trigger,name,desc,a,b,c,d) ;
  printf("\tnext: %d previous: %d\n",n,p) ;

}
int dpshow(strList *next)
{
  int handle ;
  char name[TRAK_NAME_LEN+1],description[TRAK_DATA_LEN+1] ;
  char device[TRAK_NAME_LEN+1],io[TRAK_DATA_LEN+1] ;
  Ex_Stack *s ;
  Dp_Export data ;
  
  s = &(e->stack) ;
  if (s->depth < 0) {
    printf("DP Handle must be on the stack\n") ;
    return 0 ;
  }
  handle = s->data[s->depth--] ;
  if (handle == NIL) {
    printf("not found\n") ;
    return 0 ;
  }
  
  dp_settings(handle,name,description,device,io) ;
  if (!strlen(name)) {
    printf("dp is not defined\n") ;
    return 0 ;
  }
  dp_read(handle,&data) ;
  printf("data point %s: %s\n",name,description) ;
  printf("\tdriver: %s -> [%s]\n",device,io) ;
  printf("\tvalue: %d  counter: %u carton: %d\n",data.value,data.counter,
	 data.carton) ;
  printf("\tr[0]: %d r[1]: %d r[2]: %d r[3]: %d r[4]: %d\n",data.data[0],
	 data.data[1],data.data[2],data.data[3],data.data[4]) ;
  printf("\tr[5]: %d r[6]: %d r[7]: %d r[8]: %d r[9]: %d\n",data.data[5],
	 data.data[6],data.data[7],data.data[8],data.data[9]) ;
  printf("\tleading: %d trailing: %d\n",data.leading,data.trailing) ;
  if (data.leading != NIL) {
    printf("\tleading: \n") ;
    ex_runlist(data.leading,print_list) ;
  }
  if (data.trailing != NIL) {
    printf("\ttrailing: \n") ;
    ex_runlist(data.trailing,print_list) ;
  }

  printf("Events Scheduled: %d\n",data.evt_count) ;
  if (data.evt_count) {
    printf("ev_head = %d\n",data.ev_head) ;
    ev_list(data.ev_head,ev_print) ;
  }
  return 0 ;
}

@
@<Methods@>=
int devprint(int handle)
{
  Dp_Device dev ;
  dp_readdevice(handle,&dev) ;
  printf("%s: %s\n\tcfg: %s\n",dev.name,dev.description,dev.device_data) ;
}

int drvprint(int handle) 
{
  Dp_Driver drv ;
  dp_readdriver(handle,&drv) ;
  printf("%s: %s\n",drv.name,drv.description) ;
}

int devlist(strList *next)
{
  if (next==NULL) {
    printf("usage: devlist driver\n") ;
    return 0 ;
  } 
  printf("Devices for driver: %s\n",next->text) ;
  dp_devices(next->text,devprint) ;
  return 1;
}
int drvlist(strList *next)
{
  printf("All Drivers installed: \n") ;
  dp_drivers(drvprint) ;
  return 0;
}

@
@<Methods@>=
int rpnew(strList *next)
{
  int ctr ;
  char *name, *device, *device_data ;
  char description[100] ;
  if (next == NULL) {
    printf("usage: rpnew name device device_data description\n") ;
    return 0 ;
  }
  ctr = 1 ;
  name = next->text ;
  next = next->link ;
  
  if (next == NULL) return ctr ;
  device = next->text ;
  ctr++ ;
  next = next->link ;

  if (next == NULL) return ctr ;
  device_data = next->text ;
  ctr++ ;
  next = next->link ;
  
  description[0] = EOS ;
  while (next != NULL) {
    ctr++ ;
    strcat(description,next->text) ;
    strcat(description," ") ;
    next = next->link ;
  }
  printf("call rpnew(%s,%s,%s,%s)\n",name,device,device_data,description) ;
  rp_new(name,device,device_data,description) ;
  return ctr ;
}
int rpshow(strList *next)
{
  int handle ;
  char name[TRAK_NAME_LEN+1], description[TRAK_DATA_LEN+1] ;
  char device[TRAK_NAME_LEN+1],data[RP_DATA_LEN+1] ;
  char foo[TRAK_NAME_LEN+1] ;
  Ex_Stack *s ;
  s = &(e->stack) ;
  if (s->depth < 0) {
    printf("rp handle should be on stack\n") ;
    return 0 ;
  }
  handle = s->data[s->depth--] ;

  if (handle == NIL) {
    printf("%s not found in rp table\n",next->text) ;
    return 0;
  }
  rp_settings(handle,name,description,device,data) ;
  printf("RP: %s %s\n",name,description) ;
  printf("\tdev: %s data:%s\n",device,data) ;
  printf("\tvalue: %d\n",rp_get(handle)) ;
  
  return 0;
}
int rpprint(int handle)
{
  char name[TRAK_NAME_LEN] ;
  char description[TRAK_DATA_LEN+1],
       device[TRAK_NAME_LEN+1],
       data[TRAK_DATA_LEN+1] ;



  rp_settings(handle,name,description,device,data) ;
  printf("RP: %s %s handle: %d\n",name,description,handle) ;
  printf("\tdev: %s data:%s\n",device,data) ;
  printf("\tvalue: %d\n",rp_get(handle)) ;
}
int rplist (strList *next)
{
  rp_scanall(rpprint) ;
  return 0 ;
}

@
@<Methods@>=
int rpdev(strList *next)
{
  char *name,*device,*device_data, description[80] ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: rpdev name device device_data [description]\n") ;
    return ctr ;
  }
  name = next->text ;
  ctr++ ;
  next = next->link ;

  if (next == NULL) {
    printf("usage: rpdev name device device_data [description]\n") ;
    return ctr ;
  }
  
  device = next->text ;
  ctr++ ;
  next = next->link ;
  if (next == NULL) {
    printf("usage: rpdev name device device_data [description]\n") ;
    return ctr ;
  }
  
  device_data= next->text ;
  ctr++ ;
  next = next->link ;

  description[0]= 0 ;
  while (next != NULL) {
    strcat(description,next->text) ;
    strcat(description," ") ;
    ctr++ ;
    next = next->link ;
  }
  rp_adddevice(name,description,device,device_data) ;
  printf("device %s added\n",name) ;
  return ctr ;
}
int rpdrv(strList *next)
{
  int ctr = 0 ;
  char *name, description[80] ;
  if (next == NULL) {
    printf("usage: name [description]\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  next = next->link ;
  
  description[0] = 0 ;
  while(next != NULL) {
    ctr++ ;
    strcat(description,next->text) ;
    strcat(description," ") ;
    next = next->link ;
  }
  rp_adddriver(name,description) ;
  printf("driver added\n") ;
  return ctr ;
}

@
@<Methods@>=
int traceon(strList *next)
{
  char *name ;
  int handle ;
  int ctr= 0 ;
  if (next == NULL) {
    printf("usage: traceon dp\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  handle = dp_handle(name) ;
  dp_traceset(handle,1) ;
  return 1 ;
}
@
@<Methods@>=
int traceoff(strList *next)
{
  char *name ;
  int handle ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: traceoff dp\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  handle = dp_handle(name) ;
  dp_traceset(handle,0) ;
  return 1 ;
}
@
@<Methods@>=
int force(strList *next)
{
  char *name,*value ;
  int handle ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: force name value\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  handle = dp_handle(name) ;
  if (next->text == NULL) {
    printf("usage: force name value\n") ;
    return ctr ;
  }
  value = next->text ;
  ctr++ ;
  dp_force(handle,atoi(value)) ;
  return ctr ;
}
@
@<Methods@>=
int forceoff(strList *next)
{
  char *name ;
  int handle ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: forceoff dp\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  handle = dp_handle(name) ;
  dp_traceset(handle,0) ;
  return 1 ;
}

int countertype(strList *next)
{
  char *name ;
  char *type ;
  int handle ;
  int typeflag ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: countcontrol dp type\n") ;
    return 0 ;
  }
  name = next->text ;
  ctr++ ;
  if (next->text == NULL) {
    printf("usage: countcontrol dp type\n") ;
    return 0 ;
  }
  type = next->text ;
  typeflag = CC_BOTH ;

  if (!strncasecmp("lea",type,3)) typeflag == CC_LEADING ;
  if (!strncasecmp("tra",type,3)) typeflag == CC_TRAILING ;
  handle = dp_handle(name) ;
  dp_setcounter_control(handle,typeflag) ;

  return ctr ;
}

@ Here we load into our local engine four numbers off the stack. This
allows for testing of code.
@<Methods@>=
int load_edge(strList *next)
{
  Ex_Stack *s ;
  int a,b,c,d ;
  s = &(e->stack) ;
  if (s->depth <= 3) {
    printf("4 numbers on stack required\n") ;
    return 1 ;
  }
  d = s->data[s->depth--] ;
  c = s->data[s->depth--] ;
  b = s->data[s->depth--] ;
  a = s->data[s->depth--] ;
  e->ea = a ;
  e->eb = b ;
  e->ec = c ;
  return 0 ;
}

int copy_edge(strList *next)
{
  Ex_Stack *s ;
  int a,b,c,d ;
  int dp_handle ;


  
  s = &(e->stack) ;
  if (s->depth < 0) {
    printf("Dp handle should be on the stack\n") ;
    return 0 ;
  }
  dp_handle = s->data[s->depth--] ;
  e->ea = dp_handle ;
  e->eb = dp_get(dp_handle) ;
  e->ec = dp_counter(dp_handle) ;

  return 1 ;
}
int load_evt(strList *next)
{
  Ex_Stack *s ;
  int a,b,c,d ;
  s = &(e->stack) ;
  if (s->depth <= 3) {
    printf("4 numbers on stack required\n") ;
    return 1 ;
  }
  d = s->data[s->depth--] ;
  c = s->data[s->depth--] ;
  b = s->data[s->depth--] ;
  a = s->data[s->depth--] ;
  e->eva = a ;
  e->evb = b ;
  e->evc = c ;
  e->evd = d ;
  return 0 ;
  
}


@ Create queue and array objects. For each of these we collect the name
and depth from the command line after the command. 
@<Methods@>=
int makefifo(strList *next)
{
  char *name, *depth ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: mkfifo name depth\n") ;
    return 0 ;
  }

  name = next->text ;
  ctr++ ;
  next = next->link ;
  if (next == NULL) {
    printf("depth required\n") ;
    return ctr ;
  }
  depth = next->text ;
  ctr++ ;
  
  if (atoi(depth) == 0) {
    printf("depth must be a number\n") ;
    return ctr ;
  }
  q_mkfifo(name,atoi(depth)) ;
  
  return ctr ;
}
int makelifo(strList *next)
{
  char *name, *depth ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: mklifo name depth\n") ;
    return 0 ;
  }

  name = next->text ;
  ctr++ ;
  next = next->link ;
  if (next == NULL) {
    printf("depth required\n") ;
    return ctr ;
  }
  depth = next->text ;
  ctr++ ;
  
  if (atoi(depth) == 0) {
    printf("depth must be a number\n") ;
    return ctr ;
  }
  q_mklifo(name,atoi(depth)) ;
  
  return ctr ;
}
int makearray(strList *next)
{
  char *name, *depth ;
  int ctr = 0 ;
  if (next == NULL) {
    printf("usage: mkarray name depth\n") ;
    return 0 ;
  }

  name = next->text ;
  ctr++ ;
  next = next->link ;
  if (next == NULL) {
    printf("depth required\n") ;
    return ctr ;
  }
  depth = next->text ;
  ctr++ ;
  
  if (atoi(depth) == 0) {
    printf("depth must be a number\n") ;
    return ctr ;
  }
  q_mkarray(name,atoi(depth)) ;
  
  return ctr ;
}

@* Index.
