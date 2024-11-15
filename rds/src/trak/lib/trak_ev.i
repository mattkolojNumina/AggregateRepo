@* EV. 

@ Static allocation.
@<Exported Defines@>+=
#define EV_N (4000)

@ Record.
@<Exported Structures@>+=
typedef struct 
  {
  int n ; /* next in list */
  int p ; /* previous in list */
  unsigned trigger ; 
  int statement ;
  int a,b,c,d ;
  } Ev_Record ;

@ Pool
@<Structures@>+=
typedef struct 
  {
  Ev_Record Records[EV_N] ;
  int free ;
  } Ev_Pool ;

@ Helpful typedef.
@<Exported Structures@>+=
typedef void (*ev_show)(int which,int n,int p, unsigned trigger,int statement,
			int a,int b,
			int c,int d) ;

@ Add to the trak pool
@<Trak Data@>=
Ev_Pool evt ;

@ Globals.
@<Globals@>=
static Ev_Pool *ev_pool = NULL ;
static Ev_Record *evr=NULL ; 

@ Init.
@<Exported Functions@>+=
void 
ev_init(void)
  {
  Ev_Record *p ;
  int i ;

  ev_attach() ;

  bzero(ev_pool,sizeof(Ev_Pool)) ;
  for (i=0,p=evr; i < EV_N ; i++,p++)
    {
    p->n = i+1 ; /* free list is singly linked */
    if ((i+1) == EV_N) 
      p->n = NIL ;
    }
  ev_pool->free = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void ev_init(void) ;

@ Attach.
@<Functions@>=
static void 
ev_attach(void)
  {
  ev_pool = &(pool->evt) ;
  evr = ev_pool->Records ;
  }

@ Proto.
@<Prototypes@>+=
static void ev_attach(void) ;

@ A function to decide if the tach has passed (and we missed it).
@<Functions@>+=
int 
ev_earlier(unsigned a, unsigned b) 
  {
  a = a - b ;
  return (a > 0x80000000) ;
  }

@ Proto.
@<Prototypes@>+=
int ev_earlier(unsigned a, unsigned b)  ;

@ Insert.
@<Exported Functions@>+=
int 
ev_insert(int dp_handle, unsigned act_statement,unsigned offset,
          int a,int b,int c,int d)
  {
  int old_head,new_head,ev_h ;
  Ev_Record *evt,*other ;
  Dp_Export data ;

  /* test ev_handle */
  if (dp_ok(dp_handle) == NIL) 
    return NIL ;

  ev_h = ev_pool->free ;
  if (ev_h == NIL) 
    {
    @<EVT: Recover free list@>@;
    ev_h = ev_pool->free ;
    }
  evt = evr + ev_h ;
  ev_pool->free = evt->n ;
  evt->statement = act_statement ;
  evt->trigger = offset + dp_counter(dp_handle) ;
  evt->a = a ;
  evt->b = b ;
  evt->c = c ;
  evt->d = d ;
  dp_read(dp_handle,&data) ;
  old_head = data.ev_head ;
  evt->p = NIL ;
  evt->n = NIL ;
  if (old_head == NIL) 
    {
    new_head = ev_h ;
    dp_setevhead(dp_handle,new_head) ;
    return 0 ;
    }
  else 
    {
    new_head = old_head ;
    other = evr + old_head ;
    while (ev_earlier(other->trigger,evt->trigger)) 
      {
      if (other->n == NIL) 
        {
	other->n = ev_h ;
	evt->p = other - evr ;
	return 0 ;
        }
      other = evr + other->n ;
      }
    if (other->p == NIL) 
      {
      new_head = ev_h ;
      other->p = ev_h ;
      evt->n = other-evr ;
      evt->p = NIL ;
      }
    else 
      {
      other = evr + other->p ;
      evt->n = other->n ;
      other->n = ev_h ;
      evt->p = other - evr ;
      other = evr + evt->n ;
      other->p = ev_h ;
      }
    }
  if (new_head != old_head) 
    dp_setevhead(dp_handle,new_head) ;
  return 0 ; 
  }

@ Proto.
@<Exported Prototypes@>+=
int 
ev_insert(int dp_handle, unsigned act_statement,unsigned offset,
          int a,int b,int c,int d) ;

@ All events are used up!!! This is a serious emergency. First try dumping
all the events attached to this dp. If that fails, we dump them all.
@<EVT: Recover free list@>=
  {
  ev_init() ;
  }


@ This method evaluates actions. It sets the new top with |dp_setevhead|.
@<Exported Functions@>+=
void 
ev_top(int dp, int driver, int ev_head, unsigned counter)
  {
  Ev_Record *evt,*next ;

  if (ev_head == NIL) 
    return ;
  evt = evr + ev_head ;
  while (ev_earlier(evt->trigger,counter)) 
    {
    ex_start(driver) ;
    ex_evtargs(driver,evt->a,evt->b,evt->c,evt->d) ;
    ex_exec(driver,evt->statement) ;
    if (evt->n == NIL) 
      {
      evt->n = ev_pool->free ;
      ev_pool->free = evt-evr ;
      dp_setevhead(dp,NIL) ;
      return ;
      }
    next = evr + evt->n ;
    next->p = NIL ;
    evt->n = ev_pool->free ;
    ev_pool->free = evt-evr ;
    evt=next ;
    evt->p = NIL ;
    dp_setevhead(dp,evt-evr) ;
    }
  return ;
  }

@ Proto.
@<Exported Prototypes@>+=
void ev_top(int dp, int driver, int ev_head, unsigned counter) ;

@ Event Count.
@<Exported Functions@>+=
int 
ev_count(int ev_head)
  {
  Ev_Record *evt ;
  int result = 0 ;

  if (ev_head == NIL) 
    return result ;
  for (evt = evr + ev_head ; 
       evt != NULL ;
       evt = (evt->n == NIL) ? NULL : evr + evt->n ) 
    result++ ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ev_count(int ev_head) ;

@ Event List.
@<Exported Functions@>+=
void 
ev_list(int ev_head, ev_show iterator)
  {
  Ev_Record *evt ;

  if (ev_head == NIL) 
    return ;
  for (evt = evr + ev_head ; 
       evt != NULL ;
       evt = (evt->n == NIL) ? NULL : evr + evt->n ) 
    iterator(evt-evr,
             evt->n,
             evt->p,
             evt->trigger,
             evt->statement,
             evt->a,
             evt->b,
             evt->c,
             evt->d) ;
  return  ;
  }

@ Proto.
@<Exported Prototypes@>+=
void ev_list(int ev_head, ev_show iterator) ;

@ Dump.
@<Exported Functions@>+=
void 
ev_dump(void)
  {
  int i,ctr ;
  char *ev_array = NULL ;
  Ex_Statement *s ;
  Ev_Record *rec ;

  ev_array = malloc(EV_N) ;

  trak_init() ;

  if (ev_array==NULL) 
    {
    printf("failed to malloc(%d)\n",EV_N) ;
    return ;
    }
  for (i=0 ;i < EV_N ; i++) 
    ev_array[i] = 0 ;
  for (ctr=0,rec = evr + ev_pool->free ; 
       rec != NULL ; 
       rec = (rec->n != NIL) ? evr + rec->n: NULL,ctr++) 
    {
    if (ctr > EV_N) 
      {
      printf("free list too large\n") ;
      free(ev_array) ;
      return ;
      }
    i = rec - evr ;
    ev_array[i] = 1 ;
    }
  for (rec = evr,i=0 ; i < EV_N ; i++,rec++) 
    {
    if (ev_array[i]) 
      continue ;
    s = ex->Statements + rec->statement ;
    printf("[%d] %s eva: %d evb: %d evc: %d evd: %c\n",
	   i,
	   s->name,
	   rec->a,
	   rec->b,
	   rec->c, 
	   rec->d) ;
    printf("\ttrigger: %d next: %d previous: %d\n",
	   rec->trigger,
	   rec->n,
	   rec->p) ;
    
    }
  free(ev_array) ;
  }

@ Proto.
@<Exported Prototypes@>+=
void ev_dump(void) ;
