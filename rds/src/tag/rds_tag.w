% 
%   rds_tag.web -- memory based string database 
%
%   Author: Mark Woodworth 
%
%   History:
%      05/31/00 -- check in (mrw)
%      03/13/01 -- speed up tag_sync.
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
%     (C) Copyright 2000 Numina Systems Corporation.  All Rights Reserved.
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
\def\dot{\qquad\item{$\bullet$\ }}

%
% --- title ---
%
\def\title{RDS-3 TAG -- Fast String Database}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip

The TAG system implements a fast memory-resident string database.

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
\centerline{Authors: Mark Woodworth}
}

%
% --- copyright ---
\def\botofcontents{\vfill
\centerline{\copyright 2001 Numina Systems Corporation.  
All Rights Reserved.}
}

@* TAG: fast string database. 

The TAG library implements a fast string library.  

The library creates a database of key-value pairs, where the
key and value can be any printable string.

The database is stored in a shared memory segment, controlled by a 
semaphore, and is synchronized to a disk image for non-volatility.

Strings are retrieved by searching through a red-black balanced binary tree.
This imposes a natural lexical ordering on the keys, allowing the retrieval
of groups of tags, or tree-structuring of tags, through the use of
prefixes.

@ Exported Functions.  This library exports the following functions:
{\narrower\narrower
\dot {\bf tag\_insert(key,value)} inserts one key-value pair.
If the key already exists, the net result is the value is updated.
Both the key and value string should be null-terminated printable strings
containing no special characters ({\it i.e.} the characters should be 
between 0x20 and 0x7E inclusive).
The strings can be any length up to the remaining capacity of the 
shared memory segment.

\dot {\bf tag\_value(key)} retrieves the value associated with a
key.
The value is dynamically allocated and must be freed by the caller.
A NULL pointer is returned if the key is not found.

\dot {\bf tag\_delete(key)} removes a key-value pair from the database.

\dot {\bf tag\_first(key)} 
starts with a target
key and returns the key closest
to, but not less than, the target key.

\dot {\bf tag\_next(key,value)}
starts with a target key
and returns the key and value that follows in lexical order. 

\dot {\bf tag\_interate(stem,function)} calls the passed
function (with key and value as parameters) for every
key that begins with the stem.

\dot {\bf tag\_init()} clears the database, dropping all tags.
Use with care.

\dot {\bf tag\_sync()} synchronizes the non-volatile disk file with
the volatile shared memory file.
Should be called periodically to maintain the currency of the backing
store.

\dot {\bf tag\_export()} dumps the store to a passed file descriptor.

\dot {\bf tag\_import()} adds tags from a passed file descriptor.

\dot {\bf tag\_space()} returns the bytes remaining in the string store.

\dot {\bf tag\_verify()} performs consistency checks on the store and
rebuilds the search tree. 

\par
}

@ References.  Two books are especially useful as references for this
module.
{\narrower\narrower
\dot {\bf Algorithms}: Thomas H. Cormen, Charles E. Leiserson, and Ronald L. Rivest,
{\it Introduction to Algorithms}, The MIT Press, 1990.  The source for the
red-black binary search algorithms.
\dot {\bf Systems}: David A. Curry, {\it Unix Systems Programming for SVR4},
O'Reilly \& Associates, Inc. 1996.  A good reference on using the Unix library.
\par
}

@* Overview.
This is a shared-object library.
Only the exported functions are visible to other applications.
@c
@<Includes@>@;
@<Prototypes@>@;
@<Defines@>@;
@<Structures@>@;
@<Globals@>@;
@<Functions@>@;
@<Exported Functions@>@;

@ Includes.  Standard sytem includes are collected here, as
well as this library's exported prototypes.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/shm.h>
#include "rds_tag.h" 

@ Header.  We put the prototypes in an external header.
@(rds_tag.h@>=
#ifndef __RDS_TAG_H
#define __RDS_TAG_H
   @<Exported Prototypes@> @;
#endif

@ RDS. We expect two defines, |RDS_HOME| and |RDS_BASE| from the
general RDS header.
@<Includes@>+=
#include <rds.h>

@* Strings.
At the lowest level are non-volatile shared-memory variable-length strings.

A fixed array of segments in allocated in the shared-memory space, and is
linked into a free list.  Segments are allocated from the free list to
store strings, and are returned to the free list on a delete.

Strings are not updated in place:  to change a string, the old string
is freed and the new string is allocated.

The string is divided into a key and value by a Tab character.
All characters up the the first Tab (or end of string) is the key.

Key and Value strings should contain no special characters.

@ Functions. Care must be taken when calling these functions 
as they do not verify the creation and attachment of the
shared memory segments, and do not lock the semaphore.
@<Prototypes@>+=
static int str_init(void) ;
static int str_delete(int head) ;
static int str_insert(char *text) ;
static char *str_text(int head) ;
static char *str_key(int head) ;
static int str_reload(void) ;

@ Strings are broken into segments of fixed
size |STR_SIZE|. A fixed number |STR_N| of segments are allocated. 
@<Defines@>+=
#define STR_SIZE (63)  
#define STR_N ((2*3125) << 5)
#define STR_FAT (STR_N >> 5)

@ A special link value is defined to indicate NIL.
As we don't use the first segment for data, this can simply 
be zero.
@<Defines@>+=
#define NIL (0)

@ Each segment holds
{\narrower\narrower
\dot a text segment
\dot a link to the next segment (or NIL)
\dot a first flag
\par
}
@<Structures@>+=
struct string_struct
   {
   char text[STR_SIZE+1] ;
   int link: 31 ;
   int first: 1 ;
   } ;

@ The first segment acts as a sentinel and anchors the free list.
@<Defines@>+=
#define str_free (strings[0].link)

@ The variable |strings| points to
the array of string segments.
This starts out unallocated:  a call to |tag_check()| will attach the
shared-memory and set the pointer.
@<Globals@>+=
struct string_struct *strings = NULL ;
unsigned int *smudge = NULL ;

@ String Init.  This function initializes the string array 
in an empty state with all of the segments smudged.
All of the segments will be in the free list.  The smudging will
cause |tag_sync()| to update all of the backing store.
@<Functions@>+=
int str_init(void)
   {
   int i ;

   for(i=0 ; i<STR_N ; i++)
      {
      strings[i].text[0] = '\0' ;
      strings[i].link = i+1 ;
      strings[i].first = 0 ;
      str_smudge(i) ;
      }
   strings[STR_N-1].link = NIL ;
   str_smudge(STR_N-1) ;
   }


@ Smudge. Smudge sets the smudge bit and sets an index bit. 
|tag_sync()| just tests (and clears) the index integers and looks for smudge
bits set in words where the index is non-zero.

The smudge is cleared by |tag_sync()|.
@<Functions@>+=
static void str_smudge(int which)
   {
   smudge[which>>5] = 1 ;
   }

@ We prototype it.
@<Prototypes@>+=
static void str_smudge(int which) ;

@ String Space. We reserve an amount of internal space from normal use.
The number of reserved segments is |STR_SAFETY|.
@<Defines@>+=
#define STR_SAFETY (32) 
   
@ A utility function checks if enough space is available with safety.
@<Functions@>+=
static int str_space(int size)
   {
   int chunks ;
   int ptr ;

   chunks = (size / STR_SIZE) + 1 ;
   chunks += STR_SAFETY ;

   ptr = str_free ;
   while((ptr != NIL) && (chunks > 0))
      {
      chunks-- ;
      ptr = strings[ptr].link ;
      }

   return (chunks == 0) ;
   }

@ We prototype it.
@<Prototypes@>+=
static int str_space(int size) ;

@ String Delete.  Return a string to the free list.
The segment passed must be a head segment.
For each segment we clear out the data fields.  Then, we point the link
field to the segment pointed to by the free list pointer, and then
point the free list pointer at this segment.
@<Functions@>+=
static int str_delete(int head)
   {
   int link ;

   while(head != NIL)
      {
      strings[head].first = 0 ;
      str_smudge(head) ;
      strings[head].text[0] = '\0' ;

      link = strings[head].link ;
      strings[head].link = str_free ;
      str_free = head ;
      str_smudge(0) ;
      head = link ;
      }
   }   

@ String Insert.  This function inserts a null-terminated string into the 
string store, and returns the index of the head pointer.

We break the text string into chunks, usually |STR_SIZE| but possibly
smaller on the last chunk. 
For each chunk we take a segment from the free list and copy in
the text.
(If we cannot get a segment from the free list we return all previously
allocated segments to avoid a leak).
For each segment after the first, we go back to the previous segment to
update its link.  At the end, the last segment link is pointed to NIL,
and the first segment has |first| bit set.

The index of the head of the string, or NIL, is returned.
@<Functions@>+=
static int str_insert(char *text)
   {
   int head, prev, s, i, len, remain, chunk ;
 
   len = strlen(text) ;
   if(len==0)
      return NIL ;
   
   remain = len ;
   prev = NIL ;
   head = NIL ;

   for(i=0 ; i<len ; i+=STR_SIZE)
      {
      s = str_free ;

      if(s==NIL) 
         {
         if(head!=NIL)
            str_delete(head) ;
         return NIL ;
         }
      str_free = strings[s].link ;
      strings[s].link = NIL ;
      str_smudge(0) ; /* was |str_smudge = 1 ;|*/

      chunk = (remain>STR_SIZE) ? STR_SIZE : remain ;
      remain -= chunk ;
      strncpy(strings[s].text,text+i,chunk) ;
      strings[s].text[chunk] = '\0' ;

      if(prev != NIL)
         strings[prev].link = s ;
      prev = s ;

      if(i==0)
         {
         head = s ;
         strings[s].first = 1 ;
         }
      else
         strings[s].first = 0 ;

      str_smudge(s) ;
      }

   return head ;
   }

@ Text.  Given a head index, |str_text| dynamically allocates a string
holding the contents.
The calling program must free the allocated text.
If there is a problem a NULL pointer is returned.

We make a first pass to count the segments, and then allocate the
memory. (We are a bit inefficient: we allocated only full segments).
Then a second pass fills in the string.
@<Functions@>+=
static char *str_text(int head)
   {
   char *text = NULL ;
   int link, i, count ;
     
   if(!strings[head].first)
      return NULL ;
     
   count = 0 ;
   link = head ;
   for(i=0 ; i<STR_N ; i++) 
       { 
       if(link <  0    ) break ;
       if(link >= STR_N) break ;
       if(link == NIL  ) break ;
       count++ ;
       link = strings[link].link ;
       }

   text = (char *)malloc(count*STR_SIZE) ;
   if(text==NULL)
      return NULL ;

   link = head ;       
   for(i=0 ; i<count ; i++)  
      {
      strcpy(text+(i*STR_SIZE),strings[link].text) ;
      link = strings[link].link ;
      }

   return text ;
   }

@ Key.  We define the key of a string to be everything up to the
first tab character.

This function returns just the key portion of the string.
(Actually, the whole string is returned, but a null character
replaces the tab.)
@<Functions@>+=
char *str_key(int head)
   {
   char *text, *c ;

   text = str_text(head) ;
   if(text)
      {
      c = text ;
      while(*c)
         {
         if(*c=='\t')
            {
            *c = '\0' ;
            break ;
            }
         c++ ;
         }
      }
   return text ;
   }

@ The function |str_reload()| retrieves the 
string store en-masse.
This depends on the configured |RDS_HOME| and |BACK_STORE| defines.
@<Defines@>+=
#define BACK_STORE "/data/tag.dat"

@ This reloads the tags as a block.  The search tree must be rebuilt
after a call to this function.
@<Functions@>+=
int str_reload(void)
   {
   int infile ;

   infile = open(RDS_HOME BACK_STORE,O_RDONLY) ;
   if(infile<0)
      return -10 ;
   read(infile,strings,STR_N*sizeof(struct string_struct)) ;
   if(infile>=0)
      close(infile) ;
   }

@* Tree. A red-black balance binary search tree is constructed to
assist in searching for strings.

@ Functions. Care should be taken when calling these functions as
they do not check for initialized memory segments, pointers, or
semaphore locking.
@<Prototypes@>+=
static int node_init(void) ;
static int tree_insert(int head) ;
static void node_rot_r(int node) ;
static void node_rot_l(int node) ;
static int node_insert(int head) ;
static int node_search(char *key) ;
static int node_rekey(void) ;
static int node_min(int x) ;
static int node_next(int x) ;
static int tree_delete(int x) ;
static int node_rb_fix(int x) ;
static int node_delete(int x) ;

@ Each node in the tree contains
{\narrower\narrower
\dot A string index
\dot A link to the parent
\dot A link to the right
\dot A link to the left
\dot A red flag.
\par
}
@<Structures@>+=
struct node_struct 
   {
   int red : 1 ;
   int string : 31 ;
   int parent ;
   int left ;
   int right ;
   } ;

@ There needs to be a node for each string segment in the worst case.
@<Defines@>+=
#define NODE_N (STR_N)

@ A static global anchors an array of nodes.
@<Globals@>+=
static struct node_struct *nodes = NULL ;

@ The first node is a sentinel node, always black.
We use the sentinel left pointer as the current tree head pointer, and
the sentinal right pointer as the current free head pointer.
@<Defines@>+=
#define node_head (nodes[0].left)
#define node_free (nodes[0].right)

@ As the tree is somewhat delicate, we keep track of when functions
might be changing the tree.  This is handled mostly by the
semaphore, but if a process dies in the middle of a call, the 
semaphore will unwind but the tree might be left broken.
A field in the sentinal keeps track of tree changing activity.
@<Defines@>+=
#define node_flag (nodes[0].string) 

@ Every time we might change the tree, we increment this flag.
@<Node Change Begin@>+=
(node_flag)++ ;

@ When we are done, we decrement. 
@<Node Change End@>+=
(node_flag)-- ;

@ The function |node_init()| initializes an empty tree.
All of the nodes start in the free list.
@<Functions@>+=
static int node_init(void)
   {
   int i ;

   for(i=0 ; i<NODE_N ; i++)
      {
      nodes[i].red = 0 ;
      nodes[i].string = NIL ;
      nodes[i].parent = NIL ;
      nodes[i].left = NIL ;
      nodes[i].right = i+1 ;
      } 
   nodes[NODE_N-1].right = NIL ;
   node_head = NIL ;
   node_free = 1 ;
   node_flag = 0 ;
   }

@ Insert. Given a string index, we insert the node.

@ Tree Insert.  The simpler binary tree insert forms the
basis for the red-black tree insert.
The code here follows \S13.13, page 251 of {\it Algorithms}
@<Functions@>+=
int tree_insert(int head) 
   {
   char *key, *match ;
   int order ;
   int x, y, z ;

   @<Node Change Begin@> @;

   z = node_free ;
   node_free = nodes[z].right ;

   nodes[z].string = head ;
   nodes[z].right  = NIL ;
   nodes[z].left   = NIL ;
   nodes[z].parent = NIL ;
   key = str_key(head) ;

   y = NIL ;
   x = node_head ;
   while(x != NIL)
      {
      y = x ;
      match = str_key(nodes[x].string) ;
      if(match)
         {
         order = strcmp(key,match) ;
         free(match) ;
         }
      else
         order = 1 ;

      if(order<0)
         x = nodes[x].left ;
      else
         x = nodes[x].right ;
      }

   nodes[z].parent = y ;

   if(y==NIL)
      {
      node_head = z ;      
      }
   else
      {
      match = str_key(nodes[y].string) ;
      if(match)
         {
         order = strcmp(key,match) ;
         free(match) ;
         }
      else
         order = 1 ;

      if(order<0)
         {
         nodes[y].left = z ;
         }
      else
         {
         nodes[y].right = z ;
         }
      }
   free(key) ;

   @<Node Change End@> @;

   return z ;
   }      

@ Rotations. Maintaining the tree balance requires rotations.
These are described in \S14.2, page 266 of {\it Algorithms}.   
@<Functions@>+=
static void node_rot_r(int x)
   {
   int y ;

   if(nodes[x].left==NIL)
      return ;

   @<Node Change Begin@> @;

   y = nodes[x].left ;
   nodes[x].left = nodes[y].right ;
   if(nodes[y].right != NIL)
      nodes[nodes[y].right].parent = x ;
   nodes[y].parent = nodes[x].parent ;
   if(nodes[x].parent == NIL)
      node_head = y ;
   else
      {
      if(x==nodes[nodes[x].parent].right)
         nodes[nodes[x].parent].right = y ;
      else
         nodes[nodes[x].parent].left = y ;
      }
   nodes[y].right = x ;
   nodes[x].parent = y ;

   @<Node Change End@> @;
   }

static void node_rot_l(int x)
   {
   int y ;

   if(nodes[x].right==NIL)
      return ;

   @<Node Change Begin@> @;

   y = nodes[x].right ;
   nodes[x].right = nodes[y].left ;
   if(nodes[y].left != NIL)
      nodes[nodes[y].left].parent = x ;
   nodes[y].parent = nodes[x].parent ;
   if(nodes[x].parent == NIL)
      node_head = y ;
   else
      {
      if(x==nodes[nodes[x].parent].left)
         nodes[nodes[x].parent].left = y ;
      else
         nodes[nodes[x].parent].right = y ;
      }
   nodes[y].left = x ;
   nodes[x].parent = y ;

   @<Node Change End@> @;
   }

@ Node Insert.  After we perform the simple binary tree insert,
up to two rotations and some recoloring may be required.
See \S14.3, page 268 of {\it Algorithms}. 
@<Functions@>+=
static int node_insert(int head)
   {
   int x,y,z ;

   @<Node Change Begin@> @;

   z = tree_insert(head) ;

   x = z ;
   nodes[x].red = 1 ;
   while((x!=node_head) && (nodes[nodes[x].parent].red!=0))
      {
      if(nodes[x].parent==nodes[nodes[nodes[x].parent].parent].left)
         {
         @<Node Insert Right@> @;
         }
      else
         {
         @<Node Insert Left@> @;
         }
      }
   nodes[node_head].red = 0 ;

   @<Node Change End@> @;

   return z ;
   }

@ The insert on the right.
@<Node Insert Right@>=
y = nodes[nodes[nodes[x].parent].parent].right ;
if(nodes[y].red != 0)
   {
   nodes[nodes[x].parent].red = 0 ;
   nodes[y].red = 0 ;
   nodes[nodes[nodes[x].parent].parent].red = 1 ;
   x = nodes[nodes[x].parent].parent ;
   }
else
   {
   if(x==nodes[nodes[x].parent].right)
      {
      x = nodes[x].parent ;
      node_rot_l(x) ;
      }
   nodes[nodes[x].parent].red = 0 ;
   nodes[nodes[nodes[x].parent].parent].red = 1 ;
   node_rot_r(nodes[nodes[x].parent].parent) ;
   }

@ The insert on the left.
@<Node Insert Left@>=
y = nodes[nodes[nodes[x].parent].parent].left ;
if(nodes[y].red != 0)
   {
   nodes[nodes[x].parent].red = 0 ;
   nodes[y].red = 0 ;
   nodes[nodes[nodes[x].parent].parent].red = 1 ;
   x = nodes[nodes[x].parent].parent ;
   }
else
   {
   if(x==nodes[nodes[x].parent].left)
      {
      x = nodes[x].parent ;
      node_rot_r(x) ;
      }
   nodes[nodes[x].parent].red = 0 ;
   nodes[nodes[nodes[x].parent].parent].red = 1 ;
   node_rot_l(nodes[nodes[x].parent].parent) ;
   }

@ Search. Given a string, we search for a node to match.
See \S13.2, page 247 of {\it Algorithms}.
@<Functions@>+=
static int node_search(char *key)
   {
   char *match ;
   int node ;
   int order ;

   node = node_head ;
   while(node!=NIL)
      {
      match = str_key(nodes[node].string) ;
      if(match)
         {
         order = strcmp(key,match) ;
         free(match) ;
         }
      else
         order = 1 ;
      if(order==0)
         break ;
      if(order>0)
         node = nodes[node].right ;
      else
         node = nodes[node].left ;
      }

   return node ;
   }

@ ReKey.  Rebuild the entire tree from the current strings.
This is most useful when reloading from the backing store.
@<Functions@>+=
static int node_rekey(void) 
   {
   int s ;
   char *key ;
   int x ;

   node_init() ;
   for(s=1 ; s<STR_N ; s++)
      {
      if(strings[s].first)
         {
         key = str_key(s) ;
         if(key)
            {
            x = node_search(key) ;
            if(x==NIL)
               node_insert(s) ;
            else
               str_delete(s) ;
            free(key) ;
            }
         }
      }
   }
  
@ Minimum.  This finds the minimum item in a sub-tree starting at x.
This is useful for traversing the tree, and for finding a successor.
See \S13.2, page 248 of {\it Algorithms}.
@<Functions@>+=
static int node_min(int x) 
   {
   while(nodes[x].left != NIL)
      x = nodes[x].left ;
   return x ;
   }

@ Next. This finds the successor to an item.
This is useful for tree-traversal, and is required for deletions.
See \S13.2, page 249 of {\it Algorithms}.
@<Functions@>+=
static int node_next(int x)
   {
   int y ;
   if(nodes[x].right != NIL)
      {
      return node_min(nodes[x].right) ;
      }
   y = nodes[x].parent ;
   while((y != NIL) && (x == nodes[y].right))
      {
      x = y ;
      y = nodes[y].parent ;
      }
   return y ;
   }

@ Tree Delete.  Remove a node from the binary tree.
See \S13.1, page 253, of {\it Algorithms}.
@<Functions@>+=
int tree_delete(int z) 
   {
   int x, y ;

   if(z==NIL)
      return NIL ;

   @<Node Change Begin@> @;

   if( (nodes[z].left==NIL) || (nodes[z].right==NIL) )
      y = z ;
   else
      y = node_next(z) ;

   if(nodes[y].left != NIL)
      x = nodes[y].left ;
   else
      x = nodes[y].right ;

   if(x!=NIL)
      nodes[x].parent = nodes[y].parent ;

   if(nodes[y].parent==NIL)
      node_head = x ;
   else
      {
      if(y == nodes[nodes[y].parent].left)
         nodes[nodes[y].parent].left = x ;
      else
         nodes[nodes[y].parent].right = x ;
     }
   
   if(y != z)
      nodes[z].string = nodes[y].string ;      

   nodes[y].right = node_free ;
   node_free = y ;

   @<Node Change End@> @;

   return y ;
   }

@ Fixup.  Maintaining the red-blue rules requires a fixup.
See \S14.4, page 274 of {\it Algorithms}.
@<Functions@>+=
static int node_fixup(int x)
   {
   int y,w ;

   @<Node Change Begin@> @;

   while((x!=node_head)&&(nodes[x].red==0))
      {
      if(x==nodes[nodes[x].parent].left)
         {
         @<Node Fixup Left Case@> @;
         }
      else
         {
         @<Node Fixup Right Case@> @;
         }
      }
   nodes[x].red = 0 ;

   @<Node Change End@> @;
   }

@ The left case.
@<Node Fixup Left Case@>=
   w = nodes[nodes[x].parent].right ;
   if(nodes[w].red != 0)
      {
      nodes[w].red = 0 ;
      nodes[nodes[x].parent].red = 1 ;
      node_rot_l(nodes[x].parent) ;
      w = nodes[nodes[x].parent].right ;
      }
   if((nodes[nodes[w].left].red==0)&&(nodes[nodes[w].right].red==0))
      {
      nodes[w].red = 1 ;
      x = nodes[x].parent ;
      }
   else
      {
      if(nodes[nodes[w].right].red==0)
         {
         nodes[nodes[w].left].red = 0 ;
         nodes[w].red = 1 ;
         node_rot_r(w) ;
         w = nodes[nodes[x].parent].right ;
         }
      nodes[w].red = nodes[nodes[x].parent].red ;
      nodes[nodes[x].parent].red = 0 ;
      nodes[nodes[w].right].red = 0 ;
      node_rot_l(nodes[x].parent) ;
      x = node_head ;
      }

@ The right case.
@<Node Fixup Right Case@>=
   w = nodes[nodes[x].parent].left ;
   if(nodes[w].red != 0)
      {
      nodes[w].red = 0 ;
      nodes[nodes[x].parent].red = 1 ;
      node_rot_r(nodes[x].parent) ;
      w = nodes[nodes[x].parent].left ;
      }
   if((nodes[nodes[w].right].red==0)&&(nodes[nodes[w].left].red==0))
      {
      nodes[w].red = 1 ;
      x = nodes[x].parent ;
      }
   else
      {
      if(nodes[nodes[w].left].red==0)
         {
         nodes[nodes[w].right].red = 0 ;
         nodes[w].red = 1 ;
         node_rot_l(w) ;
         w = nodes[nodes[x].parent].left ;
         }
      nodes[w].red = nodes[nodes[x].parent].red ;
      nodes[nodes[x].parent].red = 0 ;
      nodes[nodes[w].left].red = 0 ;
      node_rot_r(nodes[x].parent) ;
      x = node_head ;
      }


@ Node Delete.
We perform the simpler binary tree delete, and then
fixup the red-black rules.
See \S14.4, page 273 of {\it Algorithms}.
@<Functions@>+=
static int node_delete(int z)
   {
   int x, y ;

   if(z==NIL)
      return NIL ;

   @<Node Change Begin@> @;

   if((nodes[z].left==NIL) || (nodes[z].right==NIL))
      y = z ;
   else
      y = node_next(z) ;

   if(nodes[y].left != NIL)
      x = nodes[y].left ;
   else
      x = nodes[y].right ;

   nodes[x].parent = nodes[y].parent ;
   if(nodes[y].parent == NIL)
      node_head = x ;
   else
      {
      if(y==nodes[nodes[y].parent].left)
         nodes[nodes[y].parent].left = x ;
      else
         nodes[nodes[y].parent].right = x ;
      }

   if(y!=z)
      nodes[z].string = nodes[y].string ;

   if(nodes[y].red == 0)
      node_fixup(x) ;
   
   nodes[y].right = node_free ;
   node_free = y ;

   @<Node Change End@> @;

   return y ;
   }

@* IPC. 
The tag database is contained in a shared 
memory segment and is controlled by a semaphore.
These are SYS-V style IPC constructs and are accessed by
key values.
@<Defines@>+=
#define TAG_BASE (RDS_BASE + 0x10)
#define TAG_SEM  (TAG_BASE + 0)
#define TAG_SHM  (TAG_BASE + 1)

@ The semaphore id is stored in a local integer.
@<Globals@>+=
static int tag_sem = -1 ;

@ We must define |semun| in some instances.
@<Structures@>+=
#if defined(__GNU_LIBRARY__) && !defined(_SEM_SEMUN_UNDEFINED)
#else
union semun {
   int val ;
   struct demid_ds *buf ;
   unsigned short int *array ;
   struct seminfo *__buf ;
   } ;
#endif

@ Tag Lock.  The |tag_lock()|
function checks to see if the |tag_sem| has been created,
and if not, creates it.  Then the system waits til it can have sole access.
@<Functions@>+=
static int tag_lock(void)
   {
   union semun lock_union ;
   struct sembuf lock_buf[1] ;
   int err ;

   if(tag_sem<0)
      tag_sem = semget(TAG_SEM,1,0777) ;
   
   if(tag_sem<0)
      {
      tag_sem = semget(TAG_SEM,1,0777|IPC_CREAT) ;
      if(tag_sem<0)
         return -1 ;

      if(tag_sem==0)
         {
         err = semctl(tag_sem,0,IPC_RMID,NULL) ;
         tag_sem = semget(TAG_SEM,1,0777|IPC_CREAT) ;
         if(tag_sem<0)
            return -2 ;
         if(tag_sem==0)
            {
            return -3 ;
            }
         }

      lock_union.val = 1 ;
      semctl(tag_sem,0,SETVAL,lock_union) ;
      }

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = -1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(tag_sem,lock_buf,1) ;
   }

@ Tag Unlock.  The |tag_unlock()| function checks to see if the |tag_sem| has been created,
and if so, drops it by one.
@<Functions@>+=
static int tag_unlock(void)
   {
   struct sembuf lock_buf[1] ;

   if(tag_sem<=0)
      return -1 ;

   lock_buf[0].sem_num = 0 ;
   lock_buf[0].sem_op  = 1 ;
   lock_buf[0].sem_flg = SEM_UNDO ;
   semop(tag_sem,lock_buf,1) ;
   }

@ These are prototyped.
@<Prototypes@>+=
static int tag_lock(void) ;
static int tag_unlock(void) ;

@ Shared Memory.
The nodes and strings are stored in a structure.
@<Structures@>+=
struct store_struct 
   {
    struct string_struct s[STR_N] ;
    struct node_struct   n[NODE_N] ;
    unsigned int sm[STR_FAT] ;
    } ;

@ This is anchored by a global.
@<Globals@>+=
static struct store_struct *store = NULL ;

@ The function |tag_check()| makes sure that the 
local shared memory segment is attached, creating and initializing it
if required.

This function should be called before accessing any of the string store or
nodes to ensure that they are created and populated.  It is called
at the start of all exported functions.
@<Functions@>+=
static int tag_check(void) 
   {
   int id ;
   int made = 0 ;

   if(store==NULL)
      {
      tag_lock() ;

      id = shmget(TAG_SHM,sizeof(struct store_struct),0777) ;
      if(id<0)
         {
         made = 1 ;
         id = shmget(TAG_SHM,sizeof(struct store_struct),IPC_CREAT|0777) ;
         if(id<0)
            {
            tag_unlock() ;
            return -1 ;
            }
         }
    
       store = (struct store_struct *)shmat(id,0,0) ;
       if(store==NULL)
          {
          tag_unlock() ;
          return -2 ;
         }

      strings = (struct string_struct *)&(store->s) ;
      nodes   = (struct node_struct   *)&(store->n) ;
      smudge = (unsigned int*) &(store->sm) ;
   
      if(made)
         {
         str_init() ;
         str_reload() ;
         if(str_verify())
            str_fixup() ;
         node_init() ;
         node_rekey() ;
         }

      tag_unlock() ;
      }

   tag_lock() ;
   if(node_flag)
      {
      node_rekey() ;
      }
   tag_unlock() ;
      
   return 0 ;
   }

@ This is also prototyped.
@<Prototypes@>+=
static int tag_check(void) ;

@* Tags. These are the user level routines that handle tags.
All of these functions are smart about allocating the
shared memory segments and locking the semaphore.

@ The |tag_sync()| function makes one pass to synchronize
all smudged segments to disk.
This should be called periodically to ensure a valid
backing store.
@<Exported Functions@>+=
int tag_sync(int flag)
   {
   int err ;
   int out ;
   int i,k ;
   struct string_struct s[32] ;

   if(err=tag_check())
      return(err) ;

   out = open(RDS_HOME BACK_STORE,O_WRONLY|O_CREAT,0666) ;
   if(out<0)
      return -10 ;

   if(flag)
      for(i=0 ; i < STR_FAT ; i++)
         smudge[i] = 1 ;
   
   for (i=0 ; i < STR_FAT ; i++) 
      {
      if (smudge[i]) 
         {
         tag_lock() ;
         for(k=0 ; k<32 ; k++)
            s[k] = strings[(i<<5)+k] ; 
         smudge[i] = 0 ;
         tag_unlock() ;

         for (k = 0 ; k < 32 ; k++) 
            {
            lseek(out,((i<<5)+k)*sizeof(struct string_struct),0) ;
            write(out,s+k,sizeof(struct string_struct)) ;
            }
         }
      }
   

   if(out >= 0)
      close(out) ;
   }

@ This is prototyped.
@<Exported Prototypes@>+=
int tag_sync(int flag) ;

@ A stub program calls the function every second.
@(tag_sync.c@>+=
#include <stdio.h>
#include <unistd.h>
#include <rds_tag.h>
int
main()
   {
   tag_sync(1) ;
   while(1)
      {
      tag_sync(0) ;
      sleep(1) ;
      }
   }

@ Tag Init. This call initializes an empty string store.
Use with care.
@<Exported Functions@>+=
int tag_init(void) 
   {
   int err ;
  
   if(err=tag_check())
      return err ;

   tag_lock() ;
   str_init() ;
   node_init() ;
   tag_unlock() ;

   tag_sync(1) ;
   
   return 0 ;
   }   

@ We prototype it.
@<Exported Prototypes@>+=
int tag_init(void) ;

@ A stub program calls the function.  This should not be left out
for just anybody to run.
@(tag_init.c@>+=
#include <stdio.h>
#include <rds_tag.h>
int
main()
   {
   printf("Initializing Tags to empty...") ; fflush(stdout) ;
   tag_init() ;
   printf("done.\n") ;
   }
  
@ Tag Export.  This function traverses the string space only, and 
exports each string to a file.
The order is nothing in particular.
@<Exported Functions@>+=
int tag_export(FILE *out)
   {
   int count ;
   int err ;
   int i, j ;

   if(err=tag_check())
      return err ;

   tag_lock() ;
   for(i=1 ; i<STR_N ; i++)
      {
      if(strings[i].first)
         {
         j = i ;
         count = 0 ;
         while((j!=NIL) && (j>=0) && (j<STR_N) && (count<STR_N))
            {
            fputs(strings[j].text,out) ;
            count++ ;
            j = strings[j].link ;
            }
         fputs("\n",out) ;
         }
      }
   tag_unlock() ;
   }

@ This is prototyped.
@<Exported Prototypes@>+=
int tag_export(FILE *out) ; 

@ A stub program sends them all to |stdout|.
@(tag_export.c@>+=
#include <stdio.h>
#include <rds_tag.h>
int
main()
   {
   tag_export(stdout) ;
   }

@ Tag Import. Tags are added from a file stream.
Everything up to a |'\t'| is a key, everything from
there up to a |'\n'| is a value.
This implementation limits keys and values to 1K characters.
@<Exported Functions@>+=
int tag_import(FILE *in)
   {
   int c,k,v,past ;
   char key[1024+1], val[1024+1] ;
  
   past = k = v = 0 ; 
   key[k] = '\0' ;
   val[v] = '\0' ;
   while((c=fgetc(in))!=EOF)
      {
      if(!past)
         {
         if(c=='\t')
            past = 1 ;
         else
            {
            if(k<1024)
               {
               key[k++] = c ;
               key[k] = '\0' ;
               }
            }
         }
      else
         {
         if(c=='\n')
            {
            tag_insert(key,val) ;
            k = v = past = 0 ;
            key[k] = '\0' ;
            val[v] = '\0' ;
            }
         else
            {
            if(v<1024)
               {
               val[v++] = c ;
               val[v] = '\0' ;
               }
            }
         }
      }
   }

@ This is prototyped.
@<Exported Prototypes@>+=
int tag_import(FILE *in) ;

@ A stub program imports from |stdin|.
@(tag_import.c@>+=
#include <stdio.h>
#include <rds_tag.h>
int
main()
   {
   tag_import(stdin) ;
   }

@ Tag Delete. 
Given a key string, we delete the tag.
@<Exported Functions@>+=
int tag_delete(char *key)
   {
   int err, x ;
  
   if(err=tag_check())
      return err ;

   tag_lock() ;

   x = node_search(key) ;
   if(x != NIL)
      {
      str_delete(nodes[x].string) ;
      node_delete(x) ;
      }

   tag_unlock() ;
   return 0 ;
   }

@ This is placed in the header.
@<Exported Prototypes@>+=
int tag_delete(char *key) ;

@ A stub program calls the function.
@(tag_delete.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>
int
main(int argc, char *argv[])
   {
   if(argc!=2)
      {
      fprintf(stderr,"usage: %s <key>\n",argv[0]) ;
      exit(1) ;
      }
   tag_delete(argv[1]) ;
   }

@ Tag Insert.  First any previous versions are deleted, 
then the key-value string is created, inserted into the
string store, and then inserted into the node tree.
@<Exported Functions@>+=
int tag_insert(char *key, char *value) 
   {
   int x ;
   int ok,err ;
   int len ;
   int head ;
   char *text ;

   if(err=tag_check())
      return err ;

   len = strlen(key) + 1 + strlen(value) + 1 ;

   tag_lock() ;
   ok = str_space(len) ;
   tag_unlock() ;

   if(!ok)
      return -11 ;
    
   text = (char *)malloc(len) ;
   if(text==NULL)
      return -10 ;

   strcpy(text,key) ;
   strcat(text,"\t") ;
   strcat(text,value) ;

   tag_lock() ;

   x = node_search(key) ;
   if(x != NIL)
      {
      str_delete(nodes[x].string) ;
      node_delete(x) ;
      }

   head = str_insert(text) ;
   if(head !=NIL)
      node_insert(head) ; 

   tag_unlock() ;

   free(text) ;
   return head==NIL ? -1 : 0  ;
   }

@ Prototype.
@<Exported Prototypes@>+=
int tag_insert(char *key, char *value) ;

@ A stub program calls the function.
@(tag_insert.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>
int
main(int argc, char *argv[])
   {
   int err ;
   if(argc!=3)
      {
      fprintf(stderr,"usage: %s <key> <value>\n",argv[0]) ;
      exit(1) ; 
      }
   err = tag_insert(argv[1],argv[2]) ;
   printf("tag_insert(%s,%s)=%d\n",argv[1],argv[2],err) ;
   }

@ Tag Value. Given a key, we retrieve the value.
A NULL pointer is returned if the key is not found.
Otherwise, the calling procedure must free the passed string.
@<Exported Functions@>+=
char *tag_value(char *key)
   {
   char *text, *val ;
   int n, s, i, j ;
  
   text = NULL ;
 
   if(tag_check())
      return NULL ;
  
   tag_lock() ; 
   n = node_search(key) ;
   if(n!=NIL)
      {
      s = nodes[n].string ;
      if(s!=NIL)
         {
         text = str_text(s) ;
         }
      }
   tag_unlock() ;

   if(text==NULL)
      return text ;

   for(i=0 ; text[i] != '\0' ; i++)
      {
      if(text[i]=='\t')
         {
         i++ ;
         break ;
         }
      }
   for(j=0 ; text[j+i] != '\0' ; j++)
      text[j] = text[j+i] ;
   text[j] = '\0' ;

   return text ;
   }

@ This is prototyped.
@<Exported Prototypes@>+=
char *tag_value(char *key) ;

@ A stub programs returns the value when passed a key.
@(tag_value.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>
int
main(int argc, char *argv[])
   {
   char *text ;

   if(argc != 2)
      {
      fprintf(stderr,"usage: %s <key>\n",argv[0]) ;
      exit(1) ;
      }
   text = tag_value(argv[1]) ;
   if(text)
      {
      printf("key %s value %s\n",argv[1],text) ;   
      free(text) ;
      }
   else
      printf("key %s not found\n",argv[1]) ;
   }

@ Tag First.
We find a tag in the database, or if it isn't, we
temporarily insert it to find its successor.
In either case we return the key.
@<Exported Functions@>+=
char *tag_first(char *key) 
   {
   int err ;
   int n,s,x ;
   char *text ;

   if(err=tag_check())
      return NULL ;

   text = NULL ;

   tag_lock() ;
   n = node_search(key) ;
   if(n!=NIL)
      text = str_key(nodes[n].string) ;
   tag_unlock() ;

   if(n!=NIL)
      {
      return text ;
      }
   tag_lock() ;
   s = str_insert(key) ;
   n = tree_insert(s) ;
   x = node_next(n) ;
   if(x!=NIL)
      text = str_key(nodes[x].string) ;
   tree_delete(n) ;
   str_delete(s) ;
   tag_unlock() ;

   return text ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *tag_first(char *key) ;

@ A stub program calls the function.
@(tag_first.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>
int
main(int argc, char *argv[])
   {
   char *text ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s <key>\n",argv[0]) ;
      exit(1) ;
      }
   text = tag_first(argv[1]) ;
   if(text)
      {
      printf("key %s first %s\n",argv[1],text) ;
      free(text) ;
      }
   else
      printf("key %s past last key\n",argv[1]) ;
   }

@ Tag Next.
We find a tag in the database, or if it isn't, we
temporarily insert, and then find its successor.
In either case we return the key.
@<Exported Functions@>+=
char *tag_next(char *key) 
   {
   int err ;
   int n,s,x ;
   char *text ;

   if(err=tag_check())
      return NULL ;

   text = NULL ;

   tag_lock() ;
   n = node_search(key) ;
   if(n!=NIL)
      {
      x = node_next(n) ;
      if(x!=NIL)
         text = str_key(nodes[x].string) ;
      }
   else
      {
      s = str_insert(key) ;
      n = tree_insert(s) ;
      x = node_next(n) ;
      if(x!=NIL)
         text = str_key(nodes[x].string) ;
      tree_delete(n) ;
      str_delete(s) ;
     }
   tag_unlock() ;

   return text ;
   }

@ Prototype.
@<Exported Prototypes@>+=
char *tag_next(char *key) ;

@ A stub program calls the function.
@(tag_next.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>
int
main(int argc, char *argv[])
   {
   char *text ;

   if(argc!=2)
      {
      fprintf(stderr,"usage: %s <key>\n",argv[0]) ;
      exit(1) ;
      }
   text = tag_next(argv[1]) ;
   if(text)
      {
      printf("key %s next %s\n",argv[1],text) ;
      free(text) ;
      }
   else
      printf("key %s past last key\n",argv[1]) ;
   }

@ Tag Iterate.  A call to this function traverses the subtree, calling
the passed function on every key that matches the stem.
@<Exported Functions@>+=
int tag_iterate(char *stem, int (*item)(char *key))
   {
   int err ;
   char *o_key, *n_key ;

   if(err=tag_check())
      return err ;
   
   n_key = tag_first(stem) ;
   while(n_key != NULL)
      {
      if(0!=strncmp(stem,n_key,strlen(stem)))
         break ;
      item(n_key) ;
      o_key = n_key ;
      n_key = tag_next(o_key) ;
      free(o_key) ;
      }
   free(n_key) ;
  
   return 0 ; 
   }

@ This is prototyped.
@<Exported Prototypes@>+=
int tag_iterate(char *stem, int (*item)(char *key)) ;

@ A stub program calls |tag_iterate()| to print out a group.
@(tag_list.c@>+=
#include <stdio.h>
#include <stdlib.h>
#include <rds_tag.h>

int item(char *key)
   {
   char *value ;

   value = tag_value(key) ;
   printf("%s\t",key) ;
   if(value)
      printf("%s",value) ;
   free(value) ;
   printf("\n") ;
   }

int
main(int argc, char *argv[])
   {
   char *stem ;

   if(argc == 2)
      stem = argv[1] ;
   else
      stem = " " ;

   tag_iterate(stem,item) ;
   }

@ Tag Space.  A function returns the total number of free bytes.
@<Exported Functions@>+=
int tag_space(void) 
   {
   int err ;
   int segments ;
   int ptr ;

   if(err=tag_check())
      return err ;

   segments = 0 ;

   tag_lock() ;

   ptr = str_free ;
   while((ptr != NIL) && (segments < STR_N))
      {
      segments++ ;
      ptr = strings[ptr].link ;
      }

   tag_unlock() ;

   return STR_SIZE * segments ;
   }

@ This is prototyped.
@<Exported Prototypes@>+=
int tag_space(void) ;

@ A stub program calls the function.
@(tag_space.c@>+=
#include <stdio.h>
#include <rds_tag.h>
int
main()
   {
   printf("Tag space: %d\n",tag_space()) ;
   }

@ Tree.  A recursive tree lister.
@<Exported Functions@>+=
void tag_branch(int x) 
   {
   char *text ;

   if(x==NIL) return ;
   text = str_key(nodes[x].string) ;
   printf("%d\tl:%d\tr:%d\tp:%d\ts:%d\tk:%s\n",
          x,
          nodes[x].left,
          nodes[x].right,
          nodes[x].parent,
          nodes[x].string,
          text) ;
   free(text) ;
   tag_branch(nodes[x].left) ;
   tag_branch(nodes[x].right) ;
   }

int tag_tree(void)
   {
   if(tag_check())
      return -1 ;
   tag_lock() ;
   tag_branch(node_head) ;
   tag_unlock() ;
   }

@ Prototype.
@<Exported Prototypes@>+=
void tag_branch(int x) ;
int tag_tree(void) ;

@ Tag Verify. 
@<Exported Functions@>=
void tag_verify(void)
   {
   if (tag_check()) return ;
   tag_lock() ;
   if (str_verify()) 
      {
      fprintf(stderr,"string space is corrupt...") ;
      fflush(stderr) ;
      str_fixup() ;
      fprintf(stderr,"strings repaired...") ;
      fflush(stderr) ; 
      node_init() ;
      node_rekey() ;
      printf("done\n") ;
      }
   else
      {
      printf("string space ok\n") ;
      }
  tag_unlock() ;
}

@ Prototype.
@<Exported Prototypes@>+=
void tag_verify(void) ;

@ Stub.
@(tag_verify.c@>=
#include <stdio.h>
#include <rds_tag.h>
int 
main(int argc, char **argv)
   {
   tag_verify() ;
   return 0 ;
   }

@ Verify. Check that each string chunk is used only once. 
@<Functions@>=
static int 
str_verify(void)
   {
   char *count ;
   int i, j;
   int link ;
   int bad = 0 ;

   count = (char *)malloc(STR_N) ;
   for (i=0 ; i < STR_N ; i++) 
       count[i] = 0 ;
  
   /* count active strings */ 
   for(i=1 ; i<STR_N ; i++)
      { 
      if(strings[i].first)
         {
         count[i]++ ;
         link = strings[i].link ;
         for(j=0 ; j<STR_N ; j++) 
            {
            if(link==NIL)  break ;
            if(link< 0)    break ;
            if(link>STR_N) break ;
            count[link]++ ;
            link = strings[link].link ;
            }
         }
      }

   /* count the free list */
   link = str_free ;
   for(j=0 ;j<STR_N ; j++)
      {
      if(link==NIL) break ;
      if(link<0) break ;
      if(link>STR_N) break ;
      count[link]++ ;
      link = strings[link].link ;
      }

   /* check for problems */
   for(i=1 ; i<STR_N ; i++)
      {
      if(count[i] != 1)
         {
         bad++ ;
         }
      }

  free(count) ;
  return bad ;
  }

@ Prototype.
@<Prototypes@>+=
static int str_verify(void) ;

@ The string space is damaged. We need to repair it. 
Here is how it works.
\item{1.} Count |first| strings.
\item{2.} Allocate |argv| like array for strings.
\item{3.} Save off entire strings (with size limit) to this space.
\item{4.} re-initialize string space.
\item{5.} Reload from heap.
\item{6.} free heap.
@<Functions@>=
static void str_fixup(void)
  {
  int i,h,count ;
  char **heap ;

  count = 0 ;
  for (i=1 ; i < STR_N ; i++) 
     if (strings[i].first) 
	count++ ;

   heap = (char **) malloc(count * sizeof(char **)) ;
   if(heap==NULL)
      return ;
    
   h = 0 ;
   for (i=1 ; i < STR_N ; i++) 
      if (strings[i].first) 
         heap[h++] = str_text(i) ;

    str_init() ;

   for (h=0 ; h < count ; h++) 
      if (heap[h]) 
         str_insert(heap[h]) ;

    for (h=0 ; h<count ; h++)
       free(heap[h]) ;
    free(heap) ;
  }

@ Prototype.
@<Prototypes@>+=
static void str_fixup(void) ;

@ Prune off a branch of tags.
@(tag_prune.c@>=
#include <stdio.h>
#include <rds_tag.h>

int 
main(int argc, char **argv)
  {
  if (argc ==2) 
    {
    return tag_iterate(argv[1],tag_delete) ;
    }
  else 
    return -1 ;
  }

@*Index.
