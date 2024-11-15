@* Messages.

@ Static allocations.
@<Exported Defines@>+=
#define MSG_N (1000)
#define MSGPOOL_N (5000)
#define MAXMSG (30) 

@ Record.
@<Exported Structures@>+=
typedef struct 
  {
  int count ;
  int first ;
  } Mb_Record ;

@ Pool.
@<Structures@>+=
typedef struct 
  {
  int head ;
  Mb_Record messages[MSG_N] ;
  int buf_head ;
  int Data[MSGPOOL_N] ;
  } Mb_Pool ;

@ Result.
@<Exported Structures@>+=
typedef struct 
  {
  int count ;
  int *data  ;
  } Mb_Result ;

@ Add to the trak pool.
@<Trak Data@>=
Mb_Pool mb ;

@ Globals.
@<Globals@>=
static int mb_tail = -1 ;
static Mb_Pool *mb_pool = NULL ;
static Mb_Record *mb_messages = NULL ; 
static int *mb_data = NULL ;

@ Init.
@<Exported Functions@>+=
void 
mb_init(void)	
  {
  mb_attach() ;
  mb_pool->head = 0 ;
  bzero(mb_pool,sizeof(Mb_Pool)) ;
  }

@ Proto.
@<Exported Prototypes@>+=
void mb_init(void) ;

@ Attach.
@<Functions@>+=
static void 
mb_attach(void)
  {
  mb_pool = &(pool->mb) ;
  mb_messages = mb_pool->messages ;
  mb_data = mb_pool->Data ;
  }

@ Proto.
@<Prototypes@>+=
static void mb_attach(void) ;

@ Poll.
@<Exported Functions@>+=
Mb_Result *
mb_poll(void)
  {
  Mb_Result *result ;
  Mb_Record *r ;
  int s,i;

  trak_init() ;

  if (mb_tail == -1) 
    mb_tail = mb_pool->head ;
  if (mb_tail == mb_pool->head) 
    return NULL;
  r = mb_messages + mb_tail ;
  if (r->count == 0) 
    {
    mb_tail = mb_pool->head ;
    return NULL ;
    }
  result = (Mb_Result *) malloc(sizeof(Mb_Result)) ;
  result->data = (int*) malloc(sizeof(int) * r->count) ;
  result->count = r->count ;
  s = r->first + 1 ;
  s %= MSGPOOL_N ;
  for (i=0 ; i < result->count ;i++) 
    {
    result->data[i] = mb_data[s] ;
    s++ ;
    s %= MSGPOOL_N ;
    }
  ++mb_tail ;
  mb_tail %= MSG_N ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
Mb_Result * mb_poll(void) ;


@ Release the object passed.
@<Exported Functions@>+=
void 
mb_free(Mb_Result *victim)
  {
  if (!victim) 
    return ;
  if (victim->data) 
    free(victim->data) ;
  free(victim) ;
  }

@ Proto.
@<Exported Prototypes@>+=
void mb_free(Mb_Result *victim) ;

@ Post.
@<Exported Functions@>+=
void 
mb_post(int count, int *src)
  {
  Mb_Record *r ;
  int *dst,i,offset ;

  if (count > MAXMSG) 
    return ;

  trak_init() ;

  r = mb_messages + mb_pool->head ;
  r->count = count ;
  r->first = mb_pool->buf_head ;
  for (i=0,offset =  r->first+1 ; 
       i < count ; 
       i++,dst++,src++ ,offset++) 
    {
    offset %= MSGPOOL_N ;
    mb_data[offset] = *src ;
    }
  mb_pool->buf_head += count + 1 ;
  mb_pool->buf_head %= MSGPOOL_N ;
  pool->mb.head++ ;
  pool->mb.head %= MSG_N ;
  }

@ Proto.
@<Exported Prototypes@>+=
void mb_post(int count, int *src) ;
