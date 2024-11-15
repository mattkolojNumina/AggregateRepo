@* BX. 

@<Exported Defines@>+=
#define BX_N (1000)

@ Record.
@<Exported Structures@>+=
typedef struct 
  {
  int state ;
  int gap ;
  int length ;
  int tach_value ;
  int tach_handle ;
  int data ;
  int stamp ;
  } Bx_Record ;

@ Pool.
@<Structures@>+=
typedef struct 
  {
  Bx_Record boxes[BX_N] ;
  int next ;
  } Bx_Pool ;

@ Add to Trak.
@<Trak Data@>=
Bx_Pool bx ;

@ Globals.
@<Globals@>=
static Bx_Pool *bx_pool = NULL ;

@ Init.
@<Exported Functions@>=
void 
bx_init(void)
  {
  int i ;
  Bx_Record *c ;

  bx_attach() ;

  bx_pool->next = 0 ;
  for (i=0,c=bx_pool->boxes ; i< BX_N ; i++,c++) 
    c->state = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_init(void) ;

@ Attach.
@<Functions@>+=
static void 
bx_attach(void) 
  {
  bx_pool = (Bx_Pool*) &(pool->bx) ;
  }

@ Proto.
@<Prototypes@>+=
static void bx_attach(void)  ;

@ New.
@<Exported Functions@>+=
int 
bx_new(int tach_handle) 
  {
  Bx_Record *c ;

  trak_init() ;
    
  bx_pool->next++ ;
  if (bx_pool->next == BX_N) 
    bx_pool->next = 1 ; /* skip 0 */
  c = bx_pool->boxes + bx_pool->next ;
  c->state = 1;
  c->stamp = 0 ; /* we would like | time(NULL) ;|*/
  c->tach_handle = tach_handle ;
  c->tach_value = dp_counter(tach_handle) ;
  return bx_pool->next ;
  }

@ Proto.
@<Exported Prototypes@>+=
int bx_new(int tach_handle)  ;

@ Box Set Gap.
@<Exported Functions@>+=
void 
bx_setgap(int bx_no, int gap)
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  /* we don't change data on invalid boxes */
  if (c->state) 
    c->gap = gap ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_setgap(int bx_no, int gap) ;

@ Box Length Set. 
@<Exported Functions@>+=
void 
bx_setlength(int bx_no, int length)
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init()  ;
    
  c = bx_pool->boxes + bx_no ;
  /* we don't change data on invalid boxes */
  if (c->state) 
    c->length = length ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_setlength(int bx_no, int length) ;

@ Box Set Data.
@<Exported Functions@>+=
void 
bx_setdata(int bx_no, int data) 
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
/* we don't change data on invalid boxes */
  if (c->state) 
    c->data = data ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_setdata(int bx_no, int data) ;

@ Box Set State. 
@<Exported Functions@>+=
void 
bx_setstate(int bx_no, unsigned int state)
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  c->state = state ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_setstate(int bx_no, unsigned int state) ;

@ Box Move.
@<Exported Functions@>+=
void 
bx_move(int bx_no, int tach_handle)
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  /* we don't change data on invalid boxes */
  if (c->state) 
    {
    c->tach_handle = tach_handle ;
    c->tach_value = dp_counter(tach_handle) ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_move(int bx_no, int tach_handle) ;

@ Box Cancel. 
@<Exported Functions@>+=
void 
bx_cancel(int bx_no)
  {
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  /* we don't change data on invalid boxes */
  if (c->state) 
      c->state = 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
void bx_cancel(int bx_no) ;

@ Box Read.
@<Exported Functions@>+=
int 
bx_read(int bx_no,
        int *state,
        int *length,
        unsigned int *tach_value,
        int *tach_handle,
        int* stamp,
        int *gap,
        int *data) 
  {
  Bx_Record *c ;
  int result = NIL;

  if (bx_no < 1 || bx_no > BX_N) 
    return result ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  /* we don't change data on invalid boxes */
  if (c->state) 
    { 
    *state = c->state ;
    *length = c->length ;
    *tach_value = c->tach_value ;
    *tach_handle = c->tach_handle ;
    *stamp = c->stamp ;
    *gap = c->gap ;
    *data = c->data  ;
    result = 0 ;
    }
  return result ; 
  }

@ Proto.
@<Exported Prototypes@>+=
int 
bx_read(int bx_no,
        int *state,
        int *length,
        unsigned int *tach_value,
        int *tach_handle,
        int* stamp,
        int *gap,
        int *data)  ;

@ Box Is Valid?
@<Exported Functions@>+=
int 
bx_isvalid(int bx_no)
  {
  int result ;
  Bx_Record *c ;

  if (bx_no < 1 || bx_no > BX_N) 
    return -1 ;

  trak_init() ;
    
  c = bx_pool->boxes + bx_no ;
  result = c->state ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int bx_isvalid(int bx_no) ;

@ Dump utility.
@<Exported Functions@>+=
void 
bx_dump(void)
  {
  Bx_Record *bx ;
  int i;

  trak_init() ;

  for (i=0,bx = bx_pool->boxes ; i < BX_N ; i++) 
    {
    if (!bx->state) 
      continue ;
    printf("[%d] state: %d gap: %d len: %d tach: %d(%d) data: %d stamp: %d\n",
	   i,
	   bx->state,
	   bx->gap,
	   bx->length,
	   bx->tach_handle,
	   bx->tach_value,
	   bx->data,
	   bx->stamp) ;
    }
  } 

@ Proto.
@<Exported Prototypes@>+=
void bx_dump(void) ;
