@* IPC.
Wrappers on shared memory calls.

@ Includes required for IPC.
@<Includes@>+=
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>

@ Implementation.
@<Functions@>+=
static void *
ipc_smemget(int key,int size)
  {
  int id = shmget(key,size,IPC_CREAT|0666) ;
  return shmat(id,NULL,0) ; 
  }

@ Proto.
@<Prototypes@>+=
static void* ipc_smemget(int key,int size) ; 
