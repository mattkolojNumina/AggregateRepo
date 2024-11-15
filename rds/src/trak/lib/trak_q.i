@* Queues. 
This modules contains three queue types, fifo and lifo queues
and a random access queue (like an array),
allowing the track program to queue integers for whatever reason the
programmer requires.

More complicated data structures may not be required.

@ Static allocations. 
@<Exported Defines@>+=
#define Q_N 20
#define QDATA_N 1000

@ Types.
@<Exported Defines@>+=
enum { Q_FIFO = 1, Q_LIFO = 2, Q_ARRAY} ;

@ Record.
@<Exported Structures@>+=
typedef struct 
  {
  char q_name[TRAK_NAME_LEN+1] ;
  int q_type ;
  int start ;
  int s_d ;
  int f_h,f_t ;
  int depth ;
  } Q_Record ;

@ Pool.
@<Structures@>+=
typedef struct 
  {
  Q_Record head[Q_N] ;
  unsigned int data[QDATA_N] ;
  int highwater ;
  int nextrecord ;
  } Q_Pool ;

@ Add this to the trak shared memory.
@<Trak Data@>=
Q_Pool qPool ;

@ Some global shortcuts
@<Globals@>=
static Q_Pool *qp = NULL ;
static Q_Record *qRecords = NULL ;
static unsigned int *qData = NULL ;

@ Init.
@<Exported Functions@>+=
void 
q_init(void) 
  {
  Q_Record *r ;
  unsigned int *ip ;
  int i ;

  q_attach() ;

  qp->highwater = 0 ;
  qp->nextrecord = 0 ;

  for (i=0 ; i < Q_N ; i++) 
    {
    r = qRecords + i ;
    bzero(r,sizeof(Q_Record)) ;
    }
  for (i=0,ip=qData ; i < QDATA_N ; i++,ip++) 
    *ip = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void q_init(void)  ;

@ Attach.
@<Functions@>+=
static void 
q_attach(void)
  {
  qp = &(pool->qPool) ;
  qRecords = qp->head ;
  qData = qp->data ;
  return ;
  }

@ Proto.
@<Prototypes@>+=
static void q_attach(void) ;

@ Make FIFO. 
@<Exported Functions@>+=
int 
q_mkfifo(char *name, int depth) 
  {
  Q_Record *r ;
  int result ;

  trak_init() ;

  if (qp->nextrecord == 20) 
    return NIL ;
  result = qp->nextrecord ;
  r = qp->head + result ;
  bzero(r,sizeof(Q_Record)) ;
  strncpy(r->q_name,name,TRAK_NAME_LEN) ;
  const_set(name,result,EX_TOKEN_Q) ;
  qp->nextrecord++ ;
  r->q_type = Q_FIFO ;
  r->start = qp->highwater ;
  r->depth = depth ;
  qp->highwater += depth ;
  r->s_d = -1 ;
  r->f_h = r->f_t = 0 ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_mkfifo(char *name, int depth)  ;

@ Make LIFO.
@<Exported Functions@>+=
int 
q_mklifo(char *name, int depth)
  {
  Q_Record *r ;
  int result ;

  trak_init() ;

  if (qp->nextrecord == 20) 
    return NIL ;
  result = qp->nextrecord ;
  r =  qp->head + result ;
  bzero(r,sizeof(Q_Record)) ;
  strncpy(r->q_name,name,TRAK_NAME_LEN) ;
  const_set(name,result,EX_TOKEN_Q) ;
  qp->nextrecord++ ;
  r->q_type = Q_LIFO ;
  r->start = qp->highwater ;
  r->depth = depth ;
  qp->highwater += depth ;
  r->s_d = -1 ; 
  r->f_h = r->f_t = 0 ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_mklifo(char *name, int depth) ;

@ Make Array.
@<Exported Functions@>+=
int 
q_mkarray(char *name, int depth)
  {
  Q_Record *r ;
  int result ;
  int i ;

  trak_init() ;

  if (qp->nextrecord == 20) 
    return NIL ;
  result = qp->nextrecord ;
  r = qp->head +result ;
  bzero(r,sizeof(Q_Record)) ;
  strncpy(r->q_name,name,TRAK_NAME_LEN) ;
  const_set(name,result,EX_TOKEN_Q) ;
  qp->nextrecord++ ;
  r->q_type = Q_ARRAY ;
  r->start = qp->highwater ;
  r->depth = depth ;
  qp->highwater += r->depth ;
  r->s_d = r->f_h = r->f_t = 0 ;
  for (i=0 ; i < r->depth ; i++) 
    qp->data[r->start + i] = 0 ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_mkarray(char *name, int depth) ;

@ Name.
@<Exported Functions@>+=
int 
q_name(int q_h, char *name)
  {
  Q_Record *r ;

  trak_init() ;

  if (q_h>qp->nextrecord) 
    {
    strcpy(name,"overflow") ;
    return NIL ;
    }
  r = qp->head + q_h ;
  strcpy(name,r->q_name) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_name(int q_h, char *name) ;

@ Pop.
@<Exported Functions@>+=
unsigned 
q_pop(int q_h)
  {
  Q_Record *r ;
  int result = NIL ;

  trak_init() ;

  if (q_h > qp->nextrecord) 
    return NIL ;
  r = qp->head + q_h ;
  if (r->q_type == Q_FIFO) 
    @<FIFO pop@>@;
  if (r->q_type == Q_LIFO) 
    @<LIFO pop@>@;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned q_pop(int q_h) ;

@ Push.
@<Exported Functions@>+=
int 
q_push( int q_h, unsigned int value)
  {
  Q_Record *r ;
  int result = NIL ;

  trak_init() ;

  if (q_h > qp->nextrecord) 
    return NIL ;
  r = qp->head + q_h ;
  if (r->q_type == Q_FIFO) 
    @<FIFO push@>@;
  if (r->q_type == Q_LIFO) 
    @<LIFO push@>@;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_push( int q_h, unsigned int value) ;

@ For the lifo |s_d| is the current stack depth. We check 
to make sure it is not going
larger than the allocated depth.
@<LIFO push@>=
  {
  if (r->s_d == r->depth) 
    return NIL ;
  r->s_d++ ;
  qp->data[r->start + r->s_d] = value ;
  result = 0 ;
  }

@ NIL is returned if the stack is empty.
@<LIFO pop@>=
  {
  if (r->s_d == -1) 
    return NIL ;
  result = qp->data[r->start + r->s_d] ;
  r->s_d-- ;
  }

@ A fifo uses head/tail movement. On collision, the tail is moved too.
@<FIFO push@>=
  {
  r->f_h++ ;
  r->f_h %= r->depth ;
  qp->data[r->start + r->f_h] = value ;
  if (r->f_h == r->f_t) 
    r->f_t++ ;
  result = 0 ;
  }

@ If nothing is available, return NIL.
@<FIFO pop@>=
  {
  if (r->f_h == r->f_t) 
    return NIL ;
  r->f_t++ ;
  r->f_t %= r->depth ;
  result = qp->data[r->start + r->f_t] ;
  }

@ Array Fetch
@<Exported Functions@>+=
unsigned 
q_arrayfetch(int q_h, int offset) 
  {
  Q_Record *r ;

  trak_init() ;

  r = qp->head + q_h ;
  if (q_h > qp->nextrecord) 
    return NIL ;
  if (offset < 0 || offset > r->depth) 
    return NIL ;
  if (r->q_type != Q_ARRAY) 
    return NIL ;
  return qp->data[r->start + offset] ;
  }

@ Proto.
@<Exported Prototypes@>+=
unsigned q_arrayfetch(int q_h, int offset)  ;

@ Array Store.
@<Exported Functions@>+= 
int 
q_arraystore(int q_h, int offset, unsigned value)
  {
  Q_Record *r ;

  trak_init() ;

  r = qp->head + q_h ;
  if (q_h > qp->nextrecord) 
    return NIL ;
  if (offset < 0 || offset > r->depth) 
    return NIL ;
  if (r->q_type != Q_ARRAY) 
    return NIL ;
  qp->data[r->start + offset] = value ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int q_arraystore(int q_h, int offset, unsigned value) ;

@ A dump function.
@<Exported Functions@>+=
void 
q_dump(void)
  {
  int i ;
  Q_Record *q ;
  unsigned  *data ;

  trak_init() ;

  for (i=0 ;i  < Q_N ; i++) 
    {
    q = qRecords + i ;
    if (!strlen(q->q_name)) 
      break ;
    printf("[%d] (%s) start: %d sd: %d fh: %d ft: %d depth: %d\n",
	   i,
	   q->q_name,
	   q->start,
	   q->s_d,
	   q->f_h,
	   q->f_t,
	   q->depth) ;
    }
  for (data = qp->data,i=0 ; i < QDATA_N ; i++,data++) 
    {
    printf("% 6d ",*data) ;
    if (((i+1) % 10) == 0) 
      printf("\n") ;
    }
  printf("\n") ;
  }

@ Proto.
@<Exported Prototypes@>+=
void q_dump(void) ;

