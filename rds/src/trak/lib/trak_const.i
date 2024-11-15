@* Constants. This module provides trak with a single namespace interface. 
The original Trak had three seperate namespaces, which for the compiler all 
were used to convert a name to a number. The namespaces (then) were the dp, rp,
and actions. The compiler refused to add an action in the case of naming 
collision with other the dp or rp namespace, and replaced the action if an 
action was being redefined. However, with addition of queues and arrays, 
(more named) objects, it became clear one namespace interface was perferable.

The namespace is sorted. A simple binary search is used to find named objects
quickly. This should speed up the compiler and functions calls like |dp_handle|
and |rp_handle|.

Add to |CONST_N| the maximum count of any new named objects.
@<Defines@>+=
#define CONST_N (DP_N + RP_N + EX_N/10)

@ Pool.
@<Structures@>+=
typedef struct 
  {
  int count ;
  unsigned int index[CONST_N] ;
  Const_Record Names[CONST_N] ;
  } Const_Pool ;

@ We define the basic namespace structure. 
@<Exported Structures@>=
typedef struct 
  {
  char name[TRAK_NAME_LEN+1] ;
  unsigned value ;
  unsigned c_type ;
  } Const_Record ;

@ Add this to the global shared memory pool. This could be added to a 
user space only shared memory segment.
@<Trak Data@>=
Const_Pool Consts ;

@ Globals. We use some local pointers (initialized in |const_attach()|) to 
help shorten pool references. 
@<Globals@>=
Const_Record *c_rec = NULL ;
unsigned int *const_idx = NULL ;

@ Init. The init method calls attach and resets (clears) the module data.
@<Exported Functions@>+=
void 
const_init(void)
  {
  const_attach() ;
  bzero(&(pool->Consts),sizeof(Const_Pool)) ; 
  pool->Consts.count = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void const_init(void) ;

@ Attach sets the pointers.
@<Functions@>+=
void 
const_attach(void)
  {
  trak_init() ;
  if (c_rec !=NULL) return ;
  c_rec = pool->Consts.Names ;
  const_idx = pool->Consts.index ;
  }

@ Proto.
@<Prototypes@>+=
void const_attach(void) ;

@ Set. Here we assume it is being added. Code using the const module must 
first search (find) for a name. If is not found, then add it with set. Once,
set normally it will not change.
@<Exported Functions@>+=
int 
const_set(char *name,unsigned  value,unsigned c_type)
  {
  int r ;
  Const_Record *rec ;
  const_attach() ;
  r = pool->Consts.count ;
  if (r >= CONST_N) return NIL ;
  rec = c_rec + r ;

  pool->Consts.count++ ;
  strcpy(rec->name,name) ;
  rec->value = value ;
  rec->c_type = c_type ;
  @<Insert Const into index@>@;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int const_set(char *name,unsigned  value,unsigned c_type) ;

@ We insert in order. There are no deletions.
@<Insert Const into index@>=
  {
  int i;
  int next ;
  int temp;
  for (i=0 ; i < r ; i++) 
    {
    if (strcmp(c_rec[const_idx[i]].name,name)> 0) 
      {
      next = const_idx[i] ;
      const_idx[i] = r ;
      break ;
      }
    }
  if (i==r) 
    {
    const_idx[r] = r ;
    i++ ;
    }
  for (i++;i < r+1 ; i++) 
    {
    temp = const_idx[i] ;
    const_idx[i] = next ;
    next = temp ;
    }
  }

@ Internal find method. This returns the array index into the 
|Const_Record| array. It is used by the value and type functions.
@<Functions@>=
int 
logtwo(unsigned v)
  {
  int i ;

  for (i=1 ; i < 32 ; i++) 
    {
    if ((1<<i) > v) 
      {
      return i ;
      }
    }
  return 1;
  }

@ Proto.
@<Prototypes@>+=
int logtwo(unsigned v) ;

@ Find.
@<Functions@>+=
int 
const_find(char *name) 
  {
  int max ;
  int log, halfmax ;
  int test ;
  int move ;
  Const_Record *rec ;

  const_attach() ;
  max = pool->Consts.count ;
  log = logtwo(max) ;
  halfmax = 1 << (log-1) ;

  test = halfmax-1 ;
  move = halfmax ;
  do 
    {
    rec = c_rec + const_idx[test] ;

    if (test < 0) 
      {
      move >>=1 ;
      test += move ;
      continue ;
      }
    if (test >= pool->Consts.count) 
      {
      move >>= 1 ;
      test -= move ;
      continue ;
      }
    if (!strcmp(rec->name,name)) 
      return const_idx[test] ;
    move = move>>1 ;
    if (strcmp(rec->name,name) > 0) 
      test -= move ;
    else 
      test += move ;
    } 
  while(move > 0) ;

  return NIL ;
  }

@ Proto.
@<Prototypes@>+=
int const_find(char *name)  ;

@ Value.
@<Exported Functions@>+=
unsigned int 
const_value(char *name)
  {
  Const_Record *rec ;
  int r ;
  r = const_find(name) ;
  if (r==NIL) return NIL ;
  rec = c_rec + r ;

  return rec->value ;
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned int const_value(char *name) ;

@ Type.
@<Exported Functions@>+=
unsigned int 
const_type(char *name,int *value)
  {
  Const_Record *rec ;
  int r ;
  *value = NIL ;
  r = const_find(name) ;
  if (r==NIL) return NIL ;
  rec = c_rec + r ;
  *value = rec->value ;
  return rec->c_type ;  
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned int const_type(char *name, int *value) ;

@ Dump
@<Exported Functions@>+=
void 
const_dump(void)
  {
  Const_Record *r ;
  int i ;
  const_attach() ;
  r = c_rec ;
  printf("Const Pool: %d \n\n",pool->Consts.count) ;
  for (i=0 ; i < pool->Consts.count ; i++,r++) 
    {
    printf("[%d]: (%s) -> %d %d\n",i,r->name,r->value,r->c_type) ;
    } 
  printf("Const index:\n") ;
  for (i=0 ;i < pool->Consts.count ; i++) 
    {
    printf("[%d] -> %d\n",i,const_idx[i]) ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void const_dump(void) ;
