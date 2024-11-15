@  EX. 
Actions form a  byte code interpreted language
which can be attached to data point edges, events, or run in user space.
This form the core of the control language. Action statements can 
nest (i.e., contain other action statements), forming a programming language.

@ A fixed allocation of execution items.
@<Exported Defines@>+=
#define EX_N (250000)

@ Token types.
@<Exported Defines@>+=
#define EX_TOKEN_STATEMENT 1024
#define EX_TOKEN_DP 1025
#define EX_TOKEN_RP 1026
#define EX_TOKEN_Q  1027
#define EX_TOKEN_STRUCT 1028
#define EX_TOKEN_MEMBER 1029
#define EX_TOKEN_DATA   1030
#define EX_TOKEN_MACHINE 1031
#define EX_TOKEN_STATE 1032

@ Code Structure.
@<Exported Structures@>+=
typedef struct  
  {
  unsigned type ;
  unsigned link ;
  unsigned data ;
  } Ex_Code ;

@ Statement structure.
@<Exported Structures@>+=
typedef struct  
  {
  char name[TRAK_NAME_LEN+1] ;
  char description[TRAK_DATA_LEN] ;
  unsigned code ;
  } Ex_Statement ;

@ List structure.
@<Exported Structures@>+=
typedef struct  
  {
  char description[TRAK_DATA_LEN] ;
  unsigned statement ;
  unsigned link ; /* next |ex_list| element */
  } Ex_List ;

@ Execution environment stack
@<Exported Structures@>+=
typedef struct 
  {
  unsigned data[EX_STACK_N] ;
  int depth ;
  } Ex_Stack ;

@ A fixed allocation of stack depth.
@<Exported Defines@>+=
#define EX_STACK_N 400 

@ Engine, the execution environment.
@<Exported Structures@>+= 
typedef struct  
  {
  unsigned pc ;
  Ex_Stack stack ;
  Ex_Stack r_stack ;
  unsigned a,b,c,d ;
  unsigned ea,eb,ec ;
  unsigned eva,evb,evc,evd ;
  int trace_on ;
  } Ex_Engine ;

@ Pool.
@<Exported Structures@>+=
typedef struct 
  {
  int cfree ;
  int lfree ;
  Ex_Code Codes[EX_N] ;
  Ex_Statement Statements[EX_N/10] ;
  Ex_List Lists[EX_N/20] ;
  Ex_Engine Engines[TRAK_DRIVER_N] ; /* each driver gets it's own engine */
  } Ex_Pool ;

@ Helpful types.
@<Exported Structures@>+=
typedef int (*list_show)(Ex_List *exlist) ;
typedef int (*ex_print)(char *operator, int data) ;

@ Add to shared memory.
@<Trak Data@>=
Ex_Pool ex ;

@ Helpful pointers.
@<Globals@>=
static Ex_Pool *ex = 0 ;

static int ex_exec_statement = NIL ;
@ Init.
@<Exported Functions@>+=
void 
ex_init(void)
  {
  int i ;
  Ex_Code *c ;
  Ex_Statement *s ;
  Ex_List *l ;
  Ex_Engine *e ;

  ex_attach() ;

  for (i=0, c = ex->Codes ; i < EX_N ; i++,c++) 
    {
    c->type = c->data = 0 ;
    c->link = i+1 ;
    }
  c->link = NIL ;
  ex->cfree = 0 ;

  for (i=0,s = ex->Statements ; i < EX_N/10 ; i++,s++) 
    bzero(s,sizeof(Ex_Statement)) ;
  
  for (i=0,l=ex->Lists ; i < EX_N/20 ; i++,l++) 
    {
    bzero(l,sizeof(Ex_List)) ;
    l->link = i + 1 ;
    }

  l->link = NIL ;
  ex->lfree = 0 ;

  for (i=0,e=ex->Engines ; i < TRAK_DRIVER_N ; i++,e++) 
    {
    bzero(e,sizeof(Ex_Engine)) ;
    ex_start(i) ;
    }
  
  }

@ Proto.
@<Exported Prototypes@>+=
void ex_init(void) ;

@ Attach.
@<Functions@>+=
static void 
ex_attach(void)
  {
  ex = &(pool->ex) ;
  @<Init Action Array@>@;
  }

@ Proto.
@<Prototypes@>+=
static void ex_attach(void) ;

@ Start.
@<Exported Functions@>+=
int 
ex_start(int engine)
  {
  Ex_Engine *e ;

  trak_init() ;

  engine = 0 ;

  e = ex->Engines + engine ;
  e->pc = NIL ;
  e->stack.depth = -1 ;
  e->r_stack.depth = -1 ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_start(int engine) ;

@ Edge.
@<Exported Functions@>+=
int 
ex_edgeargs(int engine, int a, int b, int c)
  {
  Ex_Engine *e ;
  int value ;

  trak_init() ;

  engine = 0 ;
  e = ex->Engines + engine ;
  e->a = a ;
  e->b = b ;
  e->c = c ; 
  dp_registerget(a,0,&value) ;
  e->d = value ;
  e->eva = e->evb = e->evc = e->evd = NIL ;
  e->ea = a ;
  e->eb = b ;
  e->ec = c ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_edgeargs(int engine, int a, int b, int c) ;

@ Event.
@<Exported Functions@>+=
int 
ex_evtargs(int engine, int a, int b, int c, int d)
  {
  Ex_Engine *e ;

  trak_init() ;

  engine = 0;
  e = ex->Engines + engine ;
  e->a = a ;
  e->b = b ;
  e->c = c ;
  e->d = d ;
  e->ea = e->eb = e->ec = NIL ;
  e->eva = a ;
  e->evb = b ;
  e->evc = c ;
  e->evd = d ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_evtargs(int engine, int a, int b, int c, int d) ;

@ Exec.
@<Exported Functions@>+=
int 
ex_exec(int engine, int  statement)
  {
  Ex_Engine *e ;
  Ex_Code *c ;
  Ex_Statement *s ;

  if (statement < 0 || statement >= EX_N/10) 
    return NIL ;

  trak_init() ;
  ex_exec_statement = statement ;

  engine = 0 ;
  e = ex->Engines + engine ;
  s = ex->Statements + statement ;
  c = ex->Codes + s->code ;
  while (c != NULL ) 
    c = ex_runcode(e,c) ;

  return 0;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_exec(int engine, int  statement) ;

@ Eval.
@<Exported Functions@>+=
int 
ex_eval(int engine, int ex_list)
  {
  Ex_List *l ;
  Ex_Engine *e ;
  int a,b,c,d ;

  trak_init() ;

  engine = 0 ;
  e = ex->Engines + engine ;
  a = e->a ;
  b = e->b ;
  c = e->c ;
  d = e->d ;
  for (l = ex->Lists + ex_list ; 
       l != NULL ; 
       l = (l->link == NIL) ? NULL : ex->Lists + l->link) 
    {
    ex_start(engine) ;
    ex_exec(engine,l->statement) ;
    e->a = a ;
    e->b = b ;
    e->c = c ;
    e->d = d ;
    }
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_eval(int engine, int ex_list) ;

@ Get Statement.
@<Exported Functions@>+=
int 
ex_getstatement(char *name)
  {
  int handle,type ;

  trak_init() ;

  type = const_type(name,&handle) ;
  if (type == EX_TOKEN_STATEMENT) 
    return handle ;
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_getstatement(char *name) ;

@ Run List.
@<Exported Functions@>+=
int 
ex_runlist(int head, list_show iterate)
  {
  Ex_List ptr,*next ;
  int result ;

  if (head < 0 || head >= EX_N/20) 
    return NIL ;

  trak_init() ;
  
  result =0 ;
  for (next = (head ==NIL) ? NULL: ex->Lists+head ;
       next != NULL ;
       next = (next->link ==NIL) ? NULL: ex->Lists + next->link ) 
    {
    ptr = *next ;
    iterate(&ptr) ;
    result++ ;
    }
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_runlist(int head, list_show iterate) ;

@ Read State.
@<Exported Functions@>+=
int 
ex_readstate(int statement,char *name, char *description, int *start)
  {
  Ex_Statement *s ;

  if (statement < 0 || statement > EX_N/10) 
    return NIL ;

  trak_init() ;

  s = ex->Statements + statement ;
  strcpy(name,s->name) ;
  strcpy(description, s->description) ;
  *start = s->code ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_readstate(int statement,char *name, char *description, int *start) ;

@ De-compile.
@<Exported Functions@>+=
int 
ex_decompile(int statement, ex_print iterate)
  {
  Ex_Statement *s ;
  Ex_Code *c ;
  static char token[20] ;
  int link ;

  if (statement < 0 || statement > EX_N/10) 
    return NIL ;

  trak_init() ;

  s = ex->Statements + statement ;
  link = s->code ;
  while(link != NIL) 
    {
    c = ex->Codes + link ;
    link = c->link ;
    if (c->type == EX_TOKEN_STATEMENT) 
      {
      char description[50] ;
      int foo ;
      ex_readstate(c->data,token,description,&foo) ;
      }
    else 
      {
      if (c->type == EX_TOKEN_DP) 
        {
	char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
	char driver[TRAK_NAME_LEN+1], foo[TRAK_DATA_LEN+1] ;
	dp_settings(c->data,name,desc,driver,foo) ;
	strcpy(token,name) ;
        }
      if (c->type == EX_TOKEN_RP) 
        {
	char name[TRAK_NAME_LEN+1],desc[TRAK_DATA_LEN+1] ;
	char driver[TRAK_NAME_LEN+1], foo[TRAK_DATA_LEN+1] ;
	rp_settings(c->data,name,desc,driver,foo) ;
	strcpy(token,name) ;
        }
      if (c->type == EX_TOKEN_Q) 
        {
	char name[TRAK_NAME_LEN+1] ;
	q_name(c->data,name) ;
	sprintf(token,"q or array: %s",name) ;
        }
      if (c->type == 0) 
        {
	sprintf(token,"literal %d",c->data) ;
        }
      if (c->type < 512 && c->type > 0) 
        {
	strcpy(token,ExArray[c->type].token) ;
        }
      }
    
    iterate(token,c->data) ;
    }
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_decompile(int statement, ex_print iterate) ;

@ New engine.
@<Exported Functions@>+=
Ex_Engine 
*ex_newengine(void)
  {
  Ex_Engine *result ;

  trak_init() ;

  result = (Ex_Engine*) malloc(sizeof(Ex_Engine)) ;
  bzero(result,sizeof(Ex_Engine)) ;
  result->stack.depth = -1 ;
  result->r_stack.depth = -1 ;
  result->pc = NIL ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
Ex_Engine *ex_newengine(void) ;

@ User start.
@<Exported Functions@>+=
int 
ex_usrstart(Ex_Engine *ex)
  {
  trak_init() ;

  ex->pc = NIL ;
  ex->stack.depth = -1 ;
  ex->r_stack.depth = -1 ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_usrstart(Ex_Engine *ex) ;

@ User Args.
@<Exported Functions@>+=
void 
ex_usrargs(Ex_Engine *ex, int a, int b, int c, int d)
  {
  ex->a = a ;
  ex->b = b ;
  ex->c = c ;
  ex->d = d;
  }

@ Proto.
@<Exported Prototypes@>+=
void ex_usrargs(Ex_Engine *ex, int a, int b, int c, int d) ;

@ Now.
@<Exported Functions@>+=
int 
ex_now(Ex_Engine *e, char *cmd)
  {
  Ex_Code *code ;
  Ex_Statement *statement ;
  Ex_Stack *s ;
  int handle,c_type ;

  trak_init() ;

  handle= ex_getstatement(cmd) ;
  if (handle == NIL) 
    {
    handle = ex_getcmd(cmd) ;
    if (handle != NIL) 
      {
      ExArray[handle].action(e,0,NIL) ;
      return 0 ;
      }
    }
  else 
    {
    statement = ex->Statements + handle ;
    code = ex->Codes + statement->code ;
    while (code != NULL ) 
      code = ex_runcode(e,code) ;
    return 0 ;
    }

  if (*cmd == '&') 
    {
    Ex_Stack *s ;
    cmd++ ;
    handle = ex_getstatement(cmd) ;
    s = &(e->stack) ;
    stk_push(s,handle) ;
    return 0 ;
    }

  s = &(e->stack) ;
  c_type = const_type(cmd,&handle) ;

  switch(c_type) 
    {
    case EX_TOKEN_DP:@;
    case EX_TOKEN_RP:@;
    case EX_TOKEN_Q:@;
    case 0:@;
      stk_push(s,handle) ;
      return 0 ;
    default: @;
      break ;
    } ;

  if (isdigit(*cmd) || *cmd == '-') 
    { 
    /* literal, code 0 */
    ExArray[0].action(e,atoi(cmd),NIL) ;
    return 0 ;
    }

  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_now(Ex_Engine *e, char *cmd) ;

@ Show Engine.
@<Exported Functions@>+=
void 
ex_showengine(Ex_Engine *e)
  {
  Ex_Stack *s ;
  int i ;

  printf("pc: %d\n",e->pc) ;
  printf("Stack: ") ;
  s = &(e->stack) ;
  if (s->depth == -1) 
    printf("Empty\n") ;
  else 
    printf("Has %d items\n",s->depth+1) ;
  for (i=0 ; i< s->depth + 1 ; i++) 
    {
    printf("%d  ",s->data[s->depth-i]) ;
    if (!((i+1) % 15)) 
      printf("\n") ;
    }
  printf("\n") ;

  printf("Return stack:") ;
  s = &(e->r_stack) ;
  if (s->depth == -1) 
    printf("Empty\n") ;
  else 
    printf("Has %d items\n",s->depth+1) ;
  for (i=0 ; i< s->depth + 1 ; i++) 
    {
    printf("%d  ",s->data[s->depth-i]) ;
    if (!((i+1) % 15)) 
      printf("\n") ;
    }
  printf("\n") ;
  }

@ Proto.
@<Exported Prototypes@>+=
void ex_showengine(Ex_Engine *e) ;

@ Stack pop.
@<Functions@>+=
static int 
stk_pop(Ex_Stack *s, int *value)
  {
  if (s->depth <= -1) 
    {
    s->depth = -1 ;
    return NIL ;
    }
  *value = s->data[s->depth--] ;
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int stk_pop(Ex_Stack *s, int *value) ;

@ Stack push.
@<Functions@>+=
static int 
stk_push(Ex_Stack *s, int value)
  {
  s->data[++s->depth] = value ;
  return 0 ;
  }

@ Proto.
@<Prototypes@>+=
static int stk_push(Ex_Stack *s, int value) ;

@ Depth check.
@<Functions@>+=
static int 
stk_depthchk(Ex_Stack *s, int depth)
  {
  if (s->depth < (--depth)) 
    {
    s->depth = -1 ; /* reset stack */
    return -1 ;
    }
  return 0 ; /* ok */
  }

@ Proto.
@<Prototypes@>+=
static int stk_depthchk(Ex_Stack *s, int depth) ;

@ Shell Compile.
@<Exported Functions@>+=
int 
ex_shellcompile(Ex_Engine *e,char *name, char *descr, 
                int argc, char **argv)
  {
  int id ;

  trak_init() ;

  id = ex_compile(name,descr,argc, argv) ;
  return id ;
  }

@ Proto.
@<Exported Prototypes@>+=
int 
ex_shellcompile(Ex_Engine *e,char *name, char *descr, 
                int argc, char **argv) ;

@ Compile.
@<Exported Functions@>+=
int 
ex_compile(char *name, char *description, int argc, char **argv) 
  {
  int i ;
  int ch,handle ;
  int last ;
  Ex_Stack *s ;

  trak_init() ;

  @<Check token list for errors@>@;
  
  s = (Ex_Stack *)malloc(sizeof(Ex_Stack)) ;
  s->depth = -1 ;

  last = NIL ;
  for (i=0; i < argc ; i++) 
    @<Next token@>@;
  @<Check return stack@>@;
  free(s) ;
  @<Fixup |&this|@>@;
  return handle ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_compile(char *name, char *description, int argc, char **argv)  ;

@ Here we fixup the |&this| code.
@<Fixup |&this|@>=
  {
  Ex_Code *c ;
  Ex_Statement *s ;

  s = ex->Statements + handle ;
  for (c = ex->Codes + s->code  ; 
       c != NULL ;
       c = (c->link == NIL) ? NULL : ex->Codes + c->link) 
    {
    if (c->type == 1) 
      { 
      /* the compile time "type" for |&this| */
      c->type = 0 ;
      c->data = handle ;
      }
    }
  }

@ We scan the token list and name for errors, i.e., 
all tokens in string
must be findable and name must not already exist 
in namespaces. This block
will return |NIL| as the handle to the code fragment if no 
@<Check token list for errors@>=
  {
  int j ;
  int type,handle ;
  int found ;
  char *nptr ;

  if (name[0] == '&') 
    {
    printf("ERROR (%s): name cannot have a &\n",name) ;
      return NIL ; 
    }

  for (i=0 ; i < 512 ; i++) 
    {
    if (!strcmp(ExArray[i].token,name)) 
      {
      printf("ERROR (%s):name is a builtin command\n",name) ;
        return NIL ;
      }
    }

  type = const_type(name,&handle) ;
  switch(type) 
    {
    case EX_TOKEN_STATEMENT:@;
    case NIL:@;
      break ;
    default:@;
      printf("%s already in namespace (%d)\n",name,handle) ;
      return NIL ;
    }

  for (j=0 ; j < argc ; j++) 
    {
    found = 0 ;

    nptr = argv[j] ;
    if (!strcmp("&this",nptr)) 
      {
      found = 1;
      }
    else 
      {
      if (argv[j][0] == '&') 
        { 
        /* have to find it as a statement */
        nptr++ ;
        type = const_type(nptr,&handle) ;
        if (type == EX_TOKEN_STATEMENT) 
          continue ; /* ok */
        else 
          {
          printf("ERROR: Pointer (&) must point to action word\n") ;
          return NIL ;
          }
        }
      }

    type = const_type(nptr,&handle) ;
    if (type != NIL) 
      {
      found = 1; 
      }
    if (!found) 
      {
      for(i=0 ; i < 512 ; i++) 
        {
        if (!strcmp(ExArray[i].token,nptr)) 
          {
          found = 1 ;
          break ;
          }
        }
      }

    if (!found && !isdigit(*nptr) && (*nptr != '-')) 
      {
      printf("ERROR (%s): token %s not found \n",name,nptr) ;
      return NIL ;
      }
    }
  }

@ Handle the word token by token
@<Next token@>=
  {
  Ex_Statement *statement;
  int j ;
  Ex_Code *code,*prev ;
  int type,found ;
  char *nptr ;

  ch = ex->cfree ;
  code = ex->Codes + ch ;
  ex->cfree = code->link ;
  code->link = NIL ;
  if (last != NIL) 
    {
    prev = ex->Codes + last ;
    prev->link = ch ;
    }
  last = ch ;

  for (found=0,j=1; j < 512 ; j++) 
    { 
    /* 0 is literal */
    if (ExArray[j].action== 0)  
      continue ;

    if (!strcmp(ExArray[j].token,argv[i])) 
      {
      found = 1 ;
      ExArray[j].compile(j,code,s) ;
      break ;
      }
    }
  type = const_type(argv[i],&j) ;
  if (type != NIL) 
    {
    found = 1 ;
    code->type = type ;
    code->data = j ;
    }

  @<Check for address of code@>@;

  if (!found) 
    {
    code->type = 0 ;
    code->data = atoi(argv[i]) ;
    found = 1 ;
    }

  if (i==0) 
    {
    type = const_type(name,&handle) ;
    if (type == NIL) 
      {
      handle = NIL ;
      }
    if (type == EX_TOKEN_STATEMENT) 
      {
      @<cleanup and replace@>@;
      }

    if (handle == NIL) 
      {
      for (handle = 0 ; handle < EX_N/10 ; handle++) 
        {
	if (!strlen(ex->Statements[handle].name)) 
          break ;
        }
      }

    statement = ex->Statements + handle ;
    strcpy(statement->name,name) ;
    strcpy(statement->description,description) ;
    statement->code = ch ;
    const_set(name,handle,EX_TOKEN_STATEMENT) ; /* put into namespace */
    }
  }

@ Cleanup the old code list. Get ready for the new.
@<cleanup and replace@>=
  {
  Ex_Code *cptr,*cn ;

  statement = ex->Statements + handle ;
  for (cptr = ex->Codes + statement->code ; cptr != NULL ; cptr = cn) 
    {
    cn = (cptr->link == NIL) ? NULL : ex->Codes + cptr->link ;
    cptr->link = ex->cfree ;
    cptr->type = 0 ;
    cptr->data = 0 ;
    ex->cfree = cptr->link ;
    }
  }

@ It is useful to put the address of code statements on the stack.
@<Check for address of code@>=
  {
  int k,typ ;
  nptr = argv[i] ;
  if (*nptr == '&' && strcmp(nptr,"&this")) 
    {
    nptr++ ;
    typ = const_type(nptr,&k) ;
     
    if (typ == EX_TOKEN_STATEMENT) 
      {
      found = 1 ;
      code->type = 0 ; /* literal */
      code->data = k ;
      }
    }
  }

@ On completion of the compile, we check the return stack. If it is not 
empty this could badly corrupt Trak. We replace the tokens we just compiled 
with a nop. This will render the just compiled code invalid. We return |NIL|
to indicate that there was an error if the upper layer checks return codes, but
do compile in a nop so that the code just put in will be replaced with less
dangerous code.
@<Check return stack@>=
  {
  if (s->depth != -1) 
    {
    char *send[] = { "nop" } ;
    printf("ERROR (%s): return stack not empty (mis-nested if/then?)",name) ;
    ex_compile(name,description,1,send) ;
    free(s) ;
    return NIL ;
    }
  }

@ New List.
@<Exported Functions@>+=
int 
ex_newlist(char *description, int statement)
  {
  int result ;
  Ex_List *l ;

  trak_init() ;
  
  result = ex->lfree ;
  l = ex->Lists + result ;
  ex->lfree = l->link ;

  strncpy(l->description,description,TRAK_DATA_LEN) ;
  l->description[TRAK_DATA_LEN] = EOS ;
  l->link = NIL ;
  l->statement = statement ;
  return result ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_newlist(char *description, int statement) ;

@ Insert.
@<Exported Functions@>+=
int 
ex_insert(int head, int add_list)
  {
  Ex_List *next ;

  if (add_list < 0 || add_list > (EX_N/20)) 
    return NIL ;
  
  trak_init() ;

  if (head == NIL) 
    {
    next = ex->Lists + add_list ;
    next->link = NIL ;
    return add_list ;
    }

  next = ex->Lists + head ;

  while (next->link != NIL) 
    next = ex->Lists + next->link ;
  next->link = add_list ;
  return head ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_insert(int head, int add_list) ;

@ Run Code. Action (low level) are held in an array. We allocate the
first 512 actions (low byte) for actions hard coded into the program. If the
|type| of the action code has bits set in the higher bytes, then it is a 
pointer to execute a stored action statement. Push the link to the return
stack and execute the statement list pointed to by that. 
@<Functions@>+=
static Ex_Code* 
ex_runcode(Ex_Engine *e, Ex_Code *c)
  {
  int link ;
  Ex_Stack *s,*r ;
  int rec_num ;

  r = &(e->r_stack) ;
  s = &(e->stack) ;
  rec_num = c - ex->Codes ; 

  if (r->depth > (EX_STACK_N - 5)) 
    {
    Alert("return stack overflow %d dp: %d st: %d",
          rec_num,dp_eval_handle,ex_exec_statement) ;  
    return NULL ;
    }
  if (rec_num < 0 || rec_num >= EX_N) 
    {
    Alert("code record out of bounds %d dp: %d st: %d",
          rec_num,dp_eval_handle,ex_exec_statement) ;  
    if (r->depth == -1) return NULL ;
    stk_pop(r,&link) ;
    if (link==NIL) return NULL ;
    return ex->Codes + link ;
    }

  if (c->type == EX_TOKEN_STATEMENT) 
    {
    if (c->link != NIL) 
      {
      stk_push(r,c->link) ;
      }
    return ex->Codes + ex->Statements[c->data].code ;
    }
  link =NIL ;
  if (   c->type == EX_TOKEN_DP 
      || c->type == EX_TOKEN_RP 
      || c->type == EX_TOKEN_Q) 
    {
    stk_push(s,c->data) ;
    link = c->link ;
    }
  else 
    {
    if (c->type >512) 
       {
       Alert("call to native out of bounds %d (code %d) dp: %d st: %d",
          c->type,
          c - ex->Codes,dp_eval_handle,ex_exec_statement) ;
       link = NIL ;
       } 
    else if (ExArray[c->type].action != NULL) 
      {
      link = ExArray[c->type].action(e,c->data,c->link) ;
      }
     else
      {
      Alert("call to native function not defined %d (code %d)",
          c->type,
          c - ex->Codes) ;
      link = NIL ;
      }
    }
  if (link == NIL) 
    {
    if (r->depth == -1) 
      return NULL ;
    stk_pop(r,&link) ;
    }
  if (link==NIL) return NULL ;
  return ex->Codes + link ;
  }

@ Proto.
@<Prototypes@>+=
static Ex_Code* ex_runcode(Ex_Engine *e, Ex_Code *c) ;

@ Define and build the |ExArray|.
@<Exported Structures@>+=
typedef int (*ex_action)(Ex_Engine *e,int data,int link) ;
typedef void (*act_compile)(int which,Ex_Code *c,Ex_Stack *s) ;
typedef struct 
  {
  char token[TRAK_NAME_LEN+1] ;
  ex_action action ;
  act_compile compile ;
  } ExActionArray ;

@ Default.
@<Functions@>+=
static void 
def_compile(int which, Ex_Code *c, Ex_Stack *s)
  {
  c->type = which ;
  c->data = 0 ;
  }

@ Proto.
@<Prototypes@>+=
static void def_compile(int which, Ex_Code *c, Ex_Stack *s) ;

@ Branch Start.
@<Functions@>+=
static void
branch_start(int which, Ex_Code *c, Ex_Stack *s) 
  {
  c->type = which ;
  c->data = NIL ;
  stk_push(s,c - ex->Codes) ;
  }

@ Proto.
@<Prototypes@>+=
static void branch_start(int which, Ex_Code *c, Ex_Stack *s) ;

@ Branch Else.
@<Functions@>+=
static void 
branch_else(int which, Ex_Code *c, Ex_Stack *s)
  {
  int if_code,here ;
  Ex_Code *the_if ;
  stk_pop(s,&if_code) ;
  c->type = which ;
  c->data = NIL ;
  here = c - ex->Codes ;
  the_if = ex->Codes + if_code ;
  the_if->data =here ;
  stk_push(s,here) ;
  }

@ Proto.
@<Prototypes@>+=
static void branch_else(int which, Ex_Code *c, Ex_Stack *s) ;

@ Branch Then.
@<Functions@>+=
static void 
branch_then(int which, Ex_Code *c, Ex_Stack *s)
  {
  Ex_Code *before ;
  int other,here ;
  c->data = NIL ;
  c->type = which ;
  stk_pop(s,&other) ;
  before = ex->Codes + other ;
  here = c - ex->Codes ;
  before->data = here ;
  }

@ Proto.
@<Prototypes@>+=
static void branch_then(int which, Ex_Code *c, Ex_Stack *s) ;

@ An array of primitive functions.
@<Globals@>=
static ExActionArray ExArray[EX_ARRAY_N] ;

@ The staticly allocated number.
@<Defines@>+=
#define EX_ARRAY_N (512)

@ Init array.
@<Init Action Array@>=
  {
  int i ;
  int ctr ;

  for (i=0 ; i<EX_ARRAY_N ; i++)  
    ExArray[i].action = 0 ;
  
  ctr = 2 ;
  @<Actions@>@;
  }

@ Print all these fundamental core words added to the core.
@<Exported Functions@>+=
void 
ex_printcore(void)
  {
  int i ;
  for (i=0 ; i<EX_ARRAY_N ; i++) 
    {
    if (ExArray[i].action == 0) 
      continue ;
    printf("%s ",ExArray[i].token) ;
    }
  }

@ Proto.
@<Exported Prototypes@>+=
void ex_printcore(void) ;

@ Get command.
@<Exported Functions@>+=
int 
ex_getcmd(char *name)
  {
  int i ;

  for (i=0 ; i < EX_ARRAY_N; i++) 
    {
    if (!strcmp(name,ExArray[i].token)) 
      return i;
    }
  return NIL ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_getcmd(char *name) ;

@ Set Action.
@<Functions@>+=
static void 
setaction(int which, char *name, ex_action act)
  {
  strcpy(ExArray[which].token,name) ;

  ExArray[which].action = act ;
  ExArray[which].compile = def_compile ;
  }

@ Proto.
@<Prototypes@>+=
static void setaction(int which, char *name, ex_action act) ;

@ Execute.
@<Functions@>+=
static int 
act_exe(Ex_Engine *e,int data, int link)
  {
  Ex_Stack *s,*r ;
  int code ;

  s = &(e->stack) ;
  r = &(e->r_stack) ;
  if (s->depth == -1) 
    return link ;
  stk_pop(s,&code) ;
  if (link != NIL) 
    stk_push(r,link) ;
  return ex->Statements[code].code ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_exe(Ex_Engine *e,int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"exe",act_exe) ;

@ Literal. 
@<Functions@>+=
static int 
act_literal(Ex_Engine *e,int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,data) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_literal(Ex_Engine *e,int data, int link) ;

@ Action.
@<Actions@>+=
setaction(0,"literal",act_literal) ;
setaction(1,"&this",act_literal) ; /* hard coded in compiler as well */

@ Drop.
@<Functions@>+=
static int 
act_drop(Ex_Engine *e,int data,int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (s->depth > -1) 
    s->depth-- ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_drop(Ex_Engine *e,int data,int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"drop",act_drop) ;

@ NOP.
@<Functions@>+=
static int 
act_nop(Ex_Engine *e,int data,int link)
  {
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_nop(Ex_Engine *e,int data,int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"nop",act_nop) ;

@ Swap.
@<Functions@>+=
static int 
act_swap(Ex_Engine *e,int data,int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;

  t = s->data[s->depth-1] ;
  s->data[s->depth-1] = s->data[s->depth] ;
  s->data[s->depth] = t ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_swap(Ex_Engine *e,int data,int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr++,"swap",act_swap) ;

@ Size.
@<Functions@>+=
static int 
act_size(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,sizeof(Trak_Pool)) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_size(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"traksize",act_size) ; 

@ Dup.
@<Functions@>+=
static int 
act_dup(Ex_Engine *e,int data,int link) 
  {
  Ex_Stack *s ;

  s = &(e->stack) ;

  if (stk_depthchk(s,1)) 
    return link ;

  stk_push(s,s->data[s->depth]) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dup(Ex_Engine *e,int data,int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dup",act_dup) ;

@ 2Dup.
@<Functions@>+=
static int 
act_2dup(Ex_Engine *e, int data, int link) 
  {
  unsigned a,b ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  a = s->data[s->depth-1] ;
  b = s->data[s->depth] ;
  stk_push(s,a) ;
  stk_push(s,b) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_2dup(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"2dup",act_2dup) ;	

@ Rot.
@<Functions@>+=
static int 
act_rot(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,2) ;
  return act_roll(e,data,link) ;
  }

@ Proto.
@<Prototypes@>+=
static int act_rot(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"rot",act_rot) ;

@ Pick.
@<Functions@>+=
static int 
act_pick(Ex_Engine *e,int data,int link) 
  {
  int count ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&count) ;
  if (s->depth >= count) 
    stk_push(s,s->data[count]) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_pick(Ex_Engine *e,int data,int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"pick",act_pick) ;

@ Roll.
@<Functions@>+=
static int 
act_roll(Ex_Engine *e, int data, int link) 
  {
  int count ;
  int val ;
  int i ;
  int p,n ;
  Ex_Stack *s ;

  s = &(e->stack) ;

  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&count) ;

  val = s->data[s->depth - count] ;
  for (i=0 ; i < count   ; i++) 
    {
    p = s->depth -count +i ;
    n = s->depth  - count + i+1 ;
    s->data[p] = s->data[n] ;
    }
  s->data[s->depth] = val ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_roll(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"roll",act_roll) ;

@ R-Fetch.
@<Functions@>+=
static int 
act_retget(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s,*r ;

  s = &(e->stack) ;
  r = &(e->r_stack) ;
  if (stk_depthchk(r,1)) 
    {
    stk_push(s,0) ;
    }
  else 
    {
    stk_push(s,r->data[r->depth]) ;
    }
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_retget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"r@@",act_retget) ;

@ To-R.
@<Functions@>+=
static int 
act_retpush(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s,*r ;
  int t ;

  s = &(e->stack) ;
  r = &(e->r_stack) ;

  if (stk_depthchk(s,1)) 
    {
    stk_push(r,0) ;
    return link ;
    } 
  else 
    {
    stk_pop(s,&t) ;
    stk_push(r,t) ;
    }
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_retpush(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,">r",act_retpush) ;

@ R_From
@<Functions@>+=
static int 
act_retpop(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s,*r ;
  int t ;

  s = &(e->stack) ;
  r = &(e->r_stack) ;
  if (stk_depthchk(r,1))
    {
    stk_push(s,0) ;
    return link ;
    }
  else 
    {
    stk_pop(r,&t) ;
    stk_push(s,t) ;
    }

  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_retpop(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"r>",act_retpop) ;

@ Add.
@<Functions@>+=
static int 
act_add(Ex_Engine *e,int data, int link)
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  
  stk_pop(s,&t) ;
  s->data[s->depth] += t ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_add(Ex_Engine *e,int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"+",act_add) ;

@ Subtract 
@<Functions@>+=
static int 
act_subtract(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  
  stk_pop(s,&t) ;
  s->data[s->depth] -= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_subtract(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"-",act_subtract) ;

@ Multiply.
@<Functions@>+=
static int 
act_multiply(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] *= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_multiply(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"*",act_multiply) ;

@ Divide.
@<Functions@>+=
static int 
act_divide(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] /= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_divide(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"/",act_divide) ;

@ Mod.
@<Functions@>+=
static int 
act_mod(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] %= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_mod(Ex_Engine *e, int data, int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr++,"%",act_mod) ;

@ And.
@<Functions@>+=
static int 
act_and(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] &= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_and(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"and",act_and) ;

@ Not.
@<Functions@>+=
static int 
act_not(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;

  s = &(e->stack) ;

  if (stk_depthchk(s,1)) 
    return link ;
  s->data[s->depth] = !s->data[s->depth] ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_not(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"not",act_not) ;

@ Or.
@<Functions@>+=
static int 
act_or(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] |= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_or(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"or",act_or) ;

@ Xor.
@<Functions@>+=
static int 
act_xor(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] ^= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_xor(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"xor",act_xor) ;

@ Negate.
@<Functions@>+=
static int 
act_negate(Ex_Engine *e, int data, int link) 
 {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  s->data[s->depth] = ~s->data[s->depth] ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_negate(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"neg",act_negate) ;

@ Shift left.
@<Functions@>+=
static int 
act_shiftleft(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] <<= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_shiftleft(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"shl",act_shiftleft) ;

@ Shift right
@<Functions@>+=
static int 
act_shiftright(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] >>= t ;
  return link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_shiftright(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"shr",act_shiftright) ;

@ Less than.
@<Functions@>+=
static int 
act_lessthan(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] = (s->data[s->depth] < t) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_lessthan(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"<",act_lessthan) ;

@ Greater Than.
@<Functions@>+=
static int 
act_greaterthan(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] = (s->data[s->depth] > t) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_greaterthan(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,">",act_greaterthan) ;

@ Equals.
@<Functions@>+=
static int 
act_equals(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int t ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&t) ;
  s->data[s->depth] = (s->data[s->depth] == t) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_equals(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"=",act_equals) ;

@ Max.
@<Functions@>+=
static int 
act_max(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int a,b ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&a) ;
  b = s->data[s->depth] ;
  s->data[s->depth] = (a > b) ? a: b ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_max(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"max",act_max) ;

@ Min.
@<Functions@>+=
static int 
act_min(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int a,b ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&a) ;
  b = s->data[s->depth] ;
  s->data[s->depth] = (a < b) ? a: b ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_min(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"min",act_min) ;

@ DP Value Set.
@<Functions@>+=
static int 
act_dpset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int d ;
  int v ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_set(d,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.value.set",act_dpset) ;

@ DP Value Get.
@<Functions@>+=
static int 
act_dpget(Ex_Engine *e, int data, int link) 
  {
  int handle ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  s->data[s->depth] = dp_get(handle) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.value.get",act_dpget) ;

@ DP Counter Set.
@<Functions@>+=
static int 
act_dpctrset(Ex_Engine *e, int data, int link) 
  {
  int d,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_setcounter(d,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpctrset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.counter.set",act_dpctrset) ;

@ DP Counter Get.
@<Functions@>+=
static int 
act_dpctrget(Ex_Engine *e, int data, int link) 
  {
  int handle ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  s->data[s->depth] = dp_counter(handle) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpctrget(Ex_Engine *e, int data, int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.counter.get",act_dpctrget) ;

@ DP Register Set.
@<Functions@>+=
static int 
act_dpregisterset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int dp,which,value ;

  s = &(e->stack) ;
  if (stk_depthchk(s,3)) 
    return link ;
  stk_pop(s,&dp) ;
  stk_pop(s,&which) ;
  stk_pop(s,&value) ;
  dp_registerset(dp,which,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpregisterset(Ex_Engine *e, int data, int link) ;

@ Action
@<Actions@>+=
setaction(ctr++,"dp.register.set",act_dpregisterset) ;

@ DP Register Get.
@<Functions@>+=
static int 
act_dpregisterget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int dp,which,value ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&dp) ;
  which = s->data[s->depth] ;
  if (dp_registerget(dp,which,&value) != NIL) 
    s->data[s->depth] = value ;
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpregisterget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.register.get",act_dpregisterget) ;

@ DP Box Set.
@<Functions@>+=
static int 
act_dpbxset(Ex_Engine *e, int data, int link) 
  {
  int d,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_bxset(d,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpbxset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.carton.set",act_dpbxset) ;

@ DP Box Get.
@<Functions@>+=
static int 
act_dpbxget(Ex_Engine *e, int data, int link) 
  {
  int handle,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  if (dp_bxget(handle,&v) != NIL) 
    s->data[s->depth] = v ; 
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpbxget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.carton.get",act_dpbxget) ;

@ DP State Get.
@<Functions@>+=
static int 
act_dpstateget(Ex_Engine *e, int data, int link)
  {
  int handle,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  if (dp_registerget(handle,1,&v) != NIL) 
    s->data[s->depth] = v ; 
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpstateget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.state.get",act_dpstateget) ;

@ DP State Set.
@<Functions@>+=
static int 
act_dpstateset(Ex_Engine *e, int data, int link)
  {
  int d,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_registerset(d,1,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpstateset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.state.set",act_dpstateset) ;

@ DP Partner Get
@<Functions@>+=
static int 
act_dppartnerget(Ex_Engine *e, int data, int link)
  {
  int handle,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  if (dp_registerget(handle,2,&v) != NIL) 
    s->data[s->depth] = v ; 
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dppartnerget(Ex_Engine *e, int data, int link) ;

@ Action
@<Actions@>+=
setaction(ctr++,"dp.partner.get",act_dppartnerget) ;

@ DP Partner Set.
@<Functions@>+=
static int 
act_dppartnerset(Ex_Engine *e, int data, int link)
  {
  int d,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_registerset(d,2,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dppartnerset(Ex_Engine *e, int data, int link) ;

@ Action
@<Actions@>+=
setaction(ctr++,"dp.partner.set",act_dppartnerset) ;

@ DP Next Get.
@<Functions@>+=
static int 
act_dpnextget(Ex_Engine *e, int data, int link)
  {
  int handle,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  if (dp_registerget(handle,3,&v) != NIL) 
    s->data[s->depth] = v ; 
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpnextget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.next.get",act_dpnextget) ;

@ DP Next Set.
@<Functions@>+=
static int 
act_dpnextset(Ex_Engine *e, int data, int link)
  {
  int d,v ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&v) ;
  dp_registerset(d,3,v) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpnextset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.next.set",act_dpnextset) ;

@ DP Event Count.
@<Functions@>+=
static int 
act_dpevtcount(Ex_Engine *e, int data, int link) 
  {
  Dp_Export exp;
  int handle ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&handle) ;
  dp_read(handle,&exp) ;
  stk_push(s,exp.evt_count) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dpevtcount(Ex_Engine *e, int data, int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr++,"dp.evtcount",act_dpevtcount) ;

@ Zero-Branch.
@<Functions@>+=
static int 
act_zbranch(Ex_Engine *e, int data, int link) 
  {
  int val ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    {
    Ex_Code *c ;
    c = ex->Codes + data ;
    return c->link ;
    }
  stk_pop(s,&val) ;

  if (!val) 
    {
    Ex_Code *c ;
    c = ex->Codes + data ;
    return c->link ;
    } 
  return link ;
  }

@ Proto.      
@<Prototypes@>+=
static int act_zbranch(Ex_Engine *e, int data, int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr,"if",act_zbranch) ;
ExArray[ctr].compile = branch_start ;
ctr++ ;


@ Zero-Break
@<Functions@>+=
static int 
act_zbreak(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int val ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return NIL ;
  stk_pop(s,&val) ;
  return  (val == 0) ? NIL : link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_zbreak(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"ifbreak",act_zbreak) ;

@ Else.
@<Functions@>+=
static int 
act_else(Ex_Engine *e, int data, int link)
  {
  Ex_Code *c ;

  c = ex->Codes + data ;
  return c->link ;  
  }

@ Proto.
@<Prototypes@>+=
static int act_else(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr,"else",act_else) ;
ExArray[ctr].compile = branch_else ;
ctr++ ;

@ Then.
@<Functions@>+=
static int 
act_then(Ex_Engine *e, int data, int link)
  { 
  /* nop */
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_then(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr,"then",act_then) ;

ExArray[ctr].compile = branch_then ;
ctr++ ;

@ A Set.
@<Functions@>+=
static int 
act_aset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,(int *)&(e->a)) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_aset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"a.set",act_aset) ;

@ B Set. 
@<Functions@>+=
static int 
act_bset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,(int *)&(e->b)) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"b.set",act_bset) ;

@ C Set.
@<Functions@>+=
static int 
act_cset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  if (s->depth != -1) 
    stk_pop(s,(int *)&(e->c)) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_cset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"c.set",act_cset) ;

@ D Set.
@<Functions@>+=
static int 
act_dset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,(int *)&(e->d)) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"d.set",act_dset) ;

@ A Get.
@<Functions@>+=
static int 
act_aget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->a) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_aget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"a",act_aget) ;

@ B Get.
@<Functions@>+=
static int 
act_bget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->b) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"b",act_bget) ;

@ C Get.
@<Functions@>+=
static int 
act_cget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->c) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_cget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"c",act_cget) ;

@ D Get.
@<Functions@>+=
static int 
act_dget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->d) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_dget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"d",act_dget) ;

@ Edge DP Get. 
@<Functions@>+=
static int 
act_edgedpget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->ea) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_edgedpget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"edge.dp",act_edgedpget) ;

@ Edge Value Get.
@<Functions@>+=
static int 
act_edgevalueget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->eb) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_edgevalueget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"edge.value",act_edgevalueget) ;

@ Edge Counter Get.
@<Functions@>+=
static int 
act_edgecounterget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->ec) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_edgecounterget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"edge.counter",act_edgecounterget) ;

@ Edge Carton Get.
@<Functions@>+=
static int 
act_edgecartonget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int value ;

  s = &(e->stack) ;
  dp_registerget(e->ea,0,&value) ;
  stk_push(s,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_edgecartonget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"edge.carton",act_edgecartonget) ;

@ Event A Get.
@<Functions@>+=
static int 
act_evtaget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->eva) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evtaget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"evt.a",act_evtaget) ;

@ Event B Get.
@<Functions@>+=
static int 
act_evtbget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->evb) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evtbget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"evt.b",act_evtbget) ;

@ Event C Get.
@<Functions@>+=
static int 
act_evtcget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->evc) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evtcget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"evt.c",act_evtcget) ;

@ Event D Get.
@<Functions@>+=
static int 
act_evtdget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  stk_push(s,e->evd) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evtdget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"evt.d",act_evtdget) ;

@ Event Insert.
@<Functions@>+=
static int 
act_evinsert(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int a,b,c,d,count,a_h,dp_h ;

  s = &(e->stack) ;
  if (stk_depthchk(s,7)) 
    return link ;
  stk_pop(s,&d) ;
  stk_pop(s,&c) ;
  stk_pop(s,&b) ;
  stk_pop(s,&a) ;
  stk_pop(s,&count) ;
  stk_pop(s,&a_h) ;
  stk_pop(s,&dp_h) ;

  ev_insert(dp_h,a_h,count,a,b,c,d) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evinsert(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"ev_insert",act_evinsert) ;

@ Event Insert Short.
@<Functions@>+=
static int 
act_evshort(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int count,a_h,dp_h ;

  s = &(e->stack) ;
  if (stk_depthchk(s,3)) 
    return link ;
  stk_pop(s,&count) ;
  stk_pop(s,&a_h) ;
  stk_pop(s,&dp_h) ;
  ev_insert(dp_h,a_h,count,e->a,e->b,e->c,e->d) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_evshort(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"ev_ins",act_evshort) ;

@ Message Post
@<Functions@>+=
static int 
act_mbpost(Ex_Engine *e, int data, int link)
  {
  int id,a,b ;
  Ex_Stack *s ;
  int tmp[4] ;

  s = &(e->stack) ;
  if (stk_depthchk(s,3)) 
    return link ;
  stk_pop(s,&b) ;
  stk_pop(s,&a) ;
  stk_pop(s,&id) ;
  tmp[0] = id ;
  tmp[1] = a ;
  tmp[2] = b ;
  mb_post(3,tmp) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_mbpost(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"mb_post",act_mbpost) ;
 
@ Message N-Post
@<Functions@>+=
static int 
act_mbnpost(Ex_Engine *e, int data, int link)
  {
  int i,count ;
  int array[30] ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&count) ;
  if (stk_depthchk(s,count)) 
    return link ;
  if (count > 30) 
    { 
    /* can't put THAT many in a message! */
    s->depth = -1 ;
    return link ;
    }
  for (i=0 ; i < count ; i++) 
    stk_pop(s,array+i) ;
  mb_post(count,array) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_mbnpost(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"mb_npost",act_mbnpost) ;

@ Box New.
@<Functions@>+=
static int 
act_bxnew(Ex_Engine *e, int data, int link)
  {
  int tach ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;

  tach = s->data[s->depth] ;
  s->data[s->depth] = bx_new(tach) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxnew(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.new",act_bxnew) ;

@ Box Valid.
@<Functions@>+=
static int 
act_bxvalid(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  s->data[s->depth] = bx_isvalid(s->data[s->depth]) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxvalid(Ex_Engine *e, int data, int link) ;

@ Action
@<Actions@>+=
setaction(ctr++,"bx.isvalid",act_bxvalid) ;

@ Box Cancel.
@<Functions@>+=
static int 
act_bxcancel(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&bx) ;
  bx_cancel(bx) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxcancel(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.cancel",act_bxcancel) ;

@ Box State Get.
@<Functions@>+=
static int 
act_bxstateget(Ex_Engine *e, int data, int link) 
  {
  Ex_Stack *s ;
  int bx ;
  int state,len,tach,val,gap ;
  unsigned tach_val ;
  int stamp ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  bx = s->data[s->depth] ;
  if (!bx_read(bx,&state,&len,&tach_val,&tach,&stamp,&gap,&val)) 
    s->data[s->depth] = state ;
  else 
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxstateget(Ex_Engine *e, int data, int link)  ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.state.get",act_bxstateget) ;

@ Box State Set.
@<Functions@>+=
static int 
act_bxstateset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx,state ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&bx) ;
  stk_pop(s,&state) ;
  bx_setstate(bx,state) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxstateset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.state.set",act_bxstateset) ;

@ Box Length Get.
@<Functions@>+=
static int 
act_bxlenget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx ;
  int state,len,tach,val,gap ;
  unsigned tach_val ;
  int stamp ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  bx = s->data[s->depth] ;
  if (!bx_read(bx,&state,&len,&tach_val,&tach,&stamp,&gap,&val)) 
    s->data[s->depth] = len ;
  else
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxlenget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.length.get",act_bxlenget) ;

@ Box Length Set.
@<Functions@>+=
static int 
act_bxlenset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx,len ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&bx) ;
  stk_pop(s,&len) ;
  bx_setlength(bx,len) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxlenset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.length.set",act_bxlenset) ;

@ Box Move.
@<Functions@>+=
static int 
act_bxmove(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  unsigned bx,tach ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,(int *)&tach) ;
  stk_pop(s,(int *)&bx) ;
  bx_move(bx,tach) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxmove(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.move",act_bxmove) ;

@ Box Position.
@<Functions@>+=
static int 
act_bxposition(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx ;
  int state,len,tach,val,gap ;
  unsigned tach_val ;
  int stamp ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  bx = s->data[s->depth] ;
  bx_read(bx,&state,&len,&tach_val,&tach,&stamp,&gap,&val) ;
  s->data[s->depth] = tach_val ;
  stk_push(s,tach) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxposition(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.position",act_bxposition) ;

@ Box Data Get.
@<Functions@>+=
static int 
act_bxdataget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx ;
  int state,len,tach,val,gap ;
  unsigned tach_val ;
  int stamp ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  bx = s->data[s->depth] ;
  if (!bx_read(bx,&state,&len,&tach_val,&tach,&stamp,&gap,&val)) 
    s->data[s->depth] = val ;
  else
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxdataget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.data.get",act_bxdataget) ;

@ Box Data Set.
@<Functions@>+=
static int 
act_bxdataset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx,val ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&bx) ;
  stk_pop(s,&val) ;
  bx_setdata(bx,val) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxdataset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.data.set",act_bxdataset) ;

@ Box Gap Get.
@<Functions@>+=
static int 
act_bxgapget(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ; 
  int bx ;
  int state,len,tach,val,gap ;
  unsigned tach_val ;
  int stamp ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  bx = s->data[s->depth] ;
  if (!bx_read(bx,&state,&len,&tach_val,&tach,&stamp,&gap,&val))
    s->data[s->depth] = gap ;
  else
    s->data[s->depth] = NIL ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxgapget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.gap.get",act_bxgapget) ;

@ Box Gap Set.
@<Functions@>+=
static int 
act_bxgapset(Ex_Engine *e, int data, int link)
  {
  Ex_Stack *s ;
  int bx,gap ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&gap) ;
  stk_pop(s,&bx) ;
  bx_setgap(bx,gap) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_bxgapset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"bx.gap.set",act_bxgapset) ;

@ Register Value Set.
@<Functions@>+=
static int 
act_rpset(Ex_Engine *e, int data, int link)
  {
  int handle, value ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&handle) ;
  stk_pop(s,&value) ;
  rp_set(handle,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_rpset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"rp.value.set",act_rpset) ;

@ Register Value Get.
@<Functions@>+=
static int 
act_rpget(Ex_Engine *e, int data, int link)
  {
  int handle ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  handle = s->data[s->depth] ;
  s->data[s->depth] = rp_get(handle) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_rpget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"rp.value.get",act_rpget) ;

@ Queue Pop.
@<Functions@>+=
static int 
act_qpop(Ex_Engine *e, int data, int link)
  {
  int q_h ;
  unsigned value ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,1)) 
    return link ;
  stk_pop(s,&q_h) ;
  value = q_pop(q_h) ;
  stk_push(s,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_qpop(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"q.pop",act_qpop) ;

@ Queue Push.
@<Functions@>+=
static int 
act_qpush(Ex_Engine *e, int data, int link)
  {
  int q_h ;
  int value ;
  Ex_Stack *s ;
    
  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&q_h) ;
  stk_pop(s,&value) ;
  q_push(q_h,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_qpush(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"q.push",act_qpush) ;

@ Array Get 
@<Functions@>+=
static int 
act_arrayget(Ex_Engine *e, int data, int link)
  {
  int q_h,offset ;
  unsigned value ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,2)) 
    return link ;
  stk_pop(s,&q_h) ;
  stk_pop(s,&offset) ;
  value = q_arrayfetch(q_h,offset) ;
  stk_push(s,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_arrayget(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"array.get",act_arrayget) ;

@ Array Set.
@<Functions@>+=
static int 
act_arrayset(Ex_Engine *e, int data, int link)
  {
  int q_h,offset ;
  int value ;
  Ex_Stack *s ;

  s = &(e->stack) ;
  if (stk_depthchk(s,3)) 
    return link ;
  stk_pop(s,&q_h) ;
  stk_pop(s,&offset) ;
  stk_pop(s,&value) ;
  q_arraystore(q_h,offset,value) ;
  return link ;
  }

@ Proto.
@<Prototypes@>+=
static int act_arrayset(Ex_Engine *e, int data, int link) ;

@ Action.
@<Actions@>+=
setaction(ctr++,"array.set",act_arrayset) ;

@ Dump Print.
@<Exported Functions@>+=
int 
ex_dumpprint(char *operator, int data)
  {
  printf(" %s (%d) ",operator,data) ;
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int ex_dumpprint(char *operator, int data) ;

@ Dump.
@<Exported Functions@>+=
void 
ex_dump(void)
  {
  int i;
  Ex_List *l ;
  Ex_Statement *s ;

  trak_init() ;
 
  printf("Statements\n") ;
  for (s=ex->Statements,i=0 ;i < EX_N/10; i++,s++) 
    {
    if (!strlen(s->name)) 
      break ;
    printf("[%d] %s (%s)\n\t",
	   i,
	   s->name,
	   s->description) ;
    ex_decompile(i,ex_dumpprint) ;
    printf("\n") ;
    }
  printf("Statement Lists\n") ;
  for (l = ex->Lists,i=0 ; i < EX_N/20; i++,l++) 
    {
    if (!strlen(l->description)) 
      break ;
    s = ex->Statements + l->statement ;
    printf("[%d] %s (%s) link: %d\n",
	   i,
	   s->name,
	   l->description,
	   l->link) ;
    }
  }

int trak_expool_offset() {
   trak_init() ;
   return (void*) ex - (void*) pool ;  
}
@ Proto.
@<Exported Prototypes@>+=
void ex_dump(void) ;
int trak_expool_offset() ;
