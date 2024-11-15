#include "EXTERN.h"
#include "perl.h"
#include "XSUB.h"

#include </home/rds/include/rds_trak.h>

static int
not_here(char *s)
{
    croak("%s not implemented on this architecture", s);
    return -1;
}


static double
constant(char *name, int arg)
{
    errno = 0;
    switch (*name) {
    case 'A':
        break;
    case 'B':
        break;
    case 'C':
        break;
    case 'D':
        if (strEQ(name, "TRAK_DATA_LEN"))
#ifdef TRAK_DATA_LEN
            return TRAK_DATA_LEN;
#else
            goto not_there;
#endif
        break;
    case 'E':
        break;
    case 'F':
        break;
    case 'G':
        break;
    case 'H':
        break;
    case 'I':
        break;
    case 'J':
        break;
    case 'K':
        break;
    case 'L':
        break;
    case 'M':
        break;
    case 'N':
        if (strEQ(name, "TRAK_NAME_LEN"))
#ifdef TRAK_NAME_LEN
            return TRAK_NAME_LEN;
#else
            goto not_there;
#endif
        break;
    case 'O':
        break;
    case 'P':
        break;
    case 'Q':
        break;
    case 'R':
  if (strEQ(name, "REG_N"))
#ifdef REG_N
            return REG_N;
#else
            goto not_there;
#endif
        break;
    case 'S':
        break;
    case 'T':
        break;
    case 'U':
        break;
    case 'V':
        break;
    case 'W':
        break;
    case 'X':
        break;
    case 'Y':
        break;
    case 'Z':
        break;
    }
    errno = EINVAL;
    return 0;

not_there:
    errno = ENOENT;
    return 0;
}


typedef struct _hLink {
  struct _hLink *link ;
  int handle ;
} hLink ;
static hLink *handleList ;

static int dp_hscan(int handle)
{
  hLink *next ;
  next = (hLink*) malloc(sizeof(hLink)) ;
  bzero(next,sizeof(hLink)) ;
  next->handle = handle ;
  next->link = handleList ;
  handleList = next ;
  return 1 ;
}

static HV* hash ;
static int statecount ;
static char edge[30] ;
static int build_exlist(Ex_List *list)
{
  //char tmp[200],key[30] ;
  char tmp[200],key[50] ;     // JMK 7/11/22 - key[30] causes compile warning
  int foo,len ;
  char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
  ex_readstate(list->statement,name,desc,&foo) ;
  len = sprintf(tmp,"list[%s] run %s (%s)",
		list->description,
		name,
		desc) ;
  foo = sprintf(key,"%s_%d",edge,statecount++) ;
  hv_store(hash,key,foo,newSVpv(tmp,0),0) ;
  return 1;
}

static void add_ev(int which, int n, int p, unsigned trigger, int state,
		   int a,int b, int c, int d) 
{
  char tmp[200],key[30] ;
  int foo,len ;
  char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
  ex_readstate(state,name,desc,&foo) ;
  len = sprintf(key,"ev_%u",trigger) ;
  sprintf(tmp,"run %s (%s) args a: %d b: %d c: %d d: %d",name,desc,a,b,c,d) ;
  hv_store(hash,key,len,newSVpv(tmp,0),0) ;
  
}
MODULE = Trk		PACKAGE = Trk		


double
constant(name,arg)
	char *		name
	int		arg


void
trak_start()

void
trak_stop()

int
trak_test()

void
dp_init()

int
dp_handle(name)
	char *	name

int
dp_set(dp_handle, new_value)
	int	dp_handle
	int	new_value

int
dp_get(dp_handle)
	int	dp_handle

unsigned
dp_counter(dp_handle)
	int	dp_handle

int
dp_registerset(dp_handle, which, value)
	int	dp_handle
	int	which
	int	value

int
dp_registerget(dp_handle, which)
	int	dp_handle
	int	which
CODE:
{
  int a,b ;
  a = dp_registerget(dp_handle,which,&b) ;
  if (a == -1) b = -1 ;
  RETVAL = b ;
}
OUTPUT:
  RETVAL

char* 
dp_getname(dp_handle)
       int dp_handle
CODE:
{
  char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1],dev[TRAK_NAME_LEN+1],io[TRAK_DATA_LEN+1];
  dp_settings(dp_handle,name,desc,dev,io) ;
  
  RETVAL = name ;
}
OUTPUT:
  RETVAL

int
dp_counter_control(dp_handle, value)
	int	dp_handle
	int	value

int
dp_setcounter(dp_handle, value)
	int	dp_handle
	unsigned	value

int
rp_adddriver(name, description)
	char *	name
	char *	description

int
rp_getdriver(name)
	char *	name

int
rp_adddevice(name, description, driver, driver_data)
	char *	name
	char *	description
	char *	driver
	char *	driver_data

int
rp_getdevice(name)
	char *	name

int
rp_new(name, driver, driver_data, description)
	char *	name
	char *	driver
	char *	driver_data
	char *	description

int
rp_handle(name)
	char *	name

int
rp_set(rp_handle, new_value)
	int	rp_handle
	unsigned	new_value

unsigned
rp_get(rp_handle)
	int	rp_handle

int
rp_data(rp_handle, new_data)
	int	rp_handle
	char *	new_data

int
rp_prop(rp_handle, key, value)
	int	rp_handle
	char *	key
	char *  value	

int
ev_insert(dp_handle, act_statement, offset, a, b, c, d)
	int	dp_handle
	unsigned	act_statement
	unsigned	offset
	int	a
	int	b
	int	c
	int	d

int
dp_adddriver(name, description)
	char *	name
	char *	description

int
dp_getdriver(name)
	char *	name

int
dp_adddevice(name, description, driver, device_data)
	char *	name
	char *	description
	char *	driver
	char *	device_data

int
dp_getdevice(name)
	char *	name

int
dp_new(name, device, io_data, description)
	char *	name
	char *	device
	char *	io_data
	char *	description

int
dp_addtrailing(dp_handle, list_handle)
	int	dp_handle
	int	list_handle

int
dp_addleading(dp_handle, list_handle)
	int	dp_handle
	int	list_handle

int
ex_getstatement(name)
	char *	name

int
ex_getcmd(name)
	char *	name

int
ex_compile(name, description, argv)
	char *	name
	char *	description
	SV* argv
CODE:
{
  int i ;
  SV* next ;
  int comp_len ;
  char **tokens ;
  AV* aptr = (AV*) SvRV(argv) ;

  comp_len = av_len(aptr)+1 ;
  tokens = (char **) malloc(comp_len * sizeof(char**)) ;
  for (i=0 ; i < comp_len ; i++) {
    next = av_shift(aptr) ;
    tokens[i] = SvPVX(next) ;
    av_push(aptr,next) ;
  }
  RETVAL= ex_compile(name,description,comp_len,tokens) ;
  free(tokens) ;
}
OUTPUT:
  RETVAL

int
ex_newlist(description, statement)
	char *	description
	int	statement

int
ex_insert(head, add_list)
	int	head
	int	add_list

AV*
dp_list()
CODE:
{
  hLink *p ;
  RETVAL = newAV() ;
  handleList = 0 ;
  dp_scanall(dp_hscan) ;
  while(handleList) {
    p = handleList->link ;

    av_push(RETVAL,newSViv(handleList->handle)) ;
    free(handleList) ;
    handleList = p ;
  }
}
OUTPUT:
  RETVAL

AV*
rp_list()
CODE:
{
  hLink *p ;
  RETVAL = newAV() ;
  handleList = 0 ;
  rp_scanall(dp_hscan) ;
  while(handleList) {
    p = handleList->link ;
    av_push(RETVAL,newSViv(handleList->handle)) ;
    free(handleList) ;
    handleList = p ;
  } 
}
OUTPUT:
  RETVAL

void
dp_about(handle,argv)
     int handle
     SV* argv
CODE:
{
  int i,len ;
  Dp_Export data ;
  char name[TRAK_NAME_LEN+1],device[TRAK_NAME_LEN+1] ;
  char desc[TRAK_DATA_LEN+1],ddata[TRAK_DATA_LEN+1] ;
  char tmp[80] ;

  hash = (HV*) SvRV(argv) ;

  dp_settings(handle,name,desc,device,ddata) ;
  dp_read(handle,&data) ;

  hv_store(hash,"name",strlen("name"),newSVpv(name,0),0) ;
  hv_store(hash,"description",strlen("description"),newSVpv(desc,0),0) ;
  hv_store(hash,"device",strlen("device"),newSVpv(device,0),0) ;
  hv_store(hash,"io_data",strlen("io_data"),newSVpv(ddata,0),0) ;

  for (i=0 ;i < 10 ; i++) {
    len = sprintf(name,"reg_%d",i) ;
    hv_store(hash,name,len,newSViv(data.data[i]),0) ;
  }
  hv_store(hash,"force_flag",strlen("force_flag"),newSViv(data.force_flag),0);
  hv_store(hash,"force_value",strlen("force_value"),
	   newSViv(data.force_value),0);

  if (data.leading != -1) {
    strcpy(edge,"leading") ;
    statecount = 0 ;
    ex_runlist(data.leading,build_exlist) ;
  }
  if (data.trailing != -1) {
    strcpy(edge,"trailing") ;
    statecount = 0 ;
    ex_runlist(data.trailing,build_exlist) ;
  }
  if (data.evt_count) 
    ev_list(data.ev_head,add_ev) ;
}

void
rp_about(handle,argv)
     int handle
     SV* argv
CODE:
{

  char name[TRAK_NAME_LEN+1],device[TRAK_NAME_LEN+1] ;
  char desc[TRAK_DATA_LEN+1],ddata[TRAK_DATA_LEN+1] ;


  hash = (HV*) SvRV(argv) ;
  
  rp_settings(handle,name,desc,device,ddata) ;
  
  hv_store(hash,"name",strlen("name"),newSVpv(name,0),0) ;
  hv_store(hash,"description",strlen("description"),newSVpv(desc,0),0) ;
  hv_store(hash,"device",strlen("device"),newSVpv(device,0),0) ;
  hv_store(hash,"io_data",strlen("io_data"),newSVpv(ddata,0),0) ;

  hv_store(hash,"value",strlen("value"),newSViv(rp_get(handle)),0) ;
}

void
ex_readstate(statement,name,description)
   int statement
   SV *name
   SV *description
CODE:
{
  int foo ;
  char n[40] ;
  char d[TRAK_DATA_LEN+1] ;
  ex_readstate(statement,n,d,&foo) ;
  sv_setpvn(name,n,strlen(n)) ;
  sv_setpvn(description,d,strlen(d)) ;
}


int
dp_dev_suppset(handle, which,value)
   int handle
   int which
   int value

int
const_value(name)
     char *name ;

void 
const_set(name,value)
    char *name ;
    int value ;
CODE:
{
  const_set(name,value,0) ;
}

void
include(name,stem)
    char *name ;
    char *stem ;
CODE:
{
  char filename[80] ; 
  char testname[80] ;
  char cmd[120] ;
  FILE *pipe ;
  /* build filename first */
  if (!access(name,F_OK)) {
    strcpy(filename,name) ;
  } else {
    sprintf(testname,"/home/rds/app/include/%s",name) ;
    if (!access(testname,F_OK)) {
      strcpy(filename,testname) ; 
    }
    else {
      sprintf(testname,"/home/rds/include/%s",name) ;
      if (!access(testname,F_OK)) {
        strcpy(filename,testname) ;
      }
      else {
        printf("failed to locate [%s]\n",name) ;
        printf("failed to access [%s]\n",testname) ;
        return ;
      }
    }
  }
  sprintf(cmd,"grep \"#define %s\" %s",stem,filename) ; 
  pipe = popen(cmd,"r") ; 
  if (pipe != NULL) {
    char buffer[120] ; 
    while( fgets(buffer,110,pipe)  != NULL) {
       char *src, *dst ;
       char tag[32],value[32] ;
       dst = tag ; 
       for (src = buffer+ strlen("%define ") ; 
               !isspace(*src) && *src!= 0 ; 
                   src++) { 
         *dst++ = *src ;
       }
       *dst = 0 ;
       if (*src==0) continue ;
       while(isspace(*src) && *src != 0) src++ ;
       if (*src==0) continue ;
       for (dst=value;
             !isspace(*src) && *src!= 0 ; 
                   src++) { 
         *dst++ = *src ;
       }
       *dst = 0 ;
       const_set(tag,atoi(value),0) ; 
    }
    fclose(pipe) ;
  }
}


int
q_mkarray(name, depth)
        char *   name
        int      depth

int
q_mkfifo(name,depth)
        char *   name
        int      depth

int
q_mklifo(name,depth)
        char *   name
        int      depth

void
q_arraystore(name,offset,value)
        char *   name
        int      offset
        unsigned      value
CODE:
  {
    int q_h ;
    q_h = const_value(name) ;
    q_arraystore(q_h,offset,value) ;
  }


int
q_pop(name)
        char *   name
CODE:
  {
    int q_h ;
    q_h = const_value(name) ;
    if (q_h == -1) RETVAL = -1 ;
    else RETVAL = q_pop(q_h) ;
  }
OUTPUT:
  RETVAL

void
q_push(name,value)
        char *   name
        unsigned      value
CODE:
  {
    int q_h ;
    q_h = const_value(name) ;
    if (q_h != -1) q_push(q_h,value) ;
  }

int
q_arrayfetch(handle,offset)
        int     handle
        int     offset


