#include <stdio.h>
#include <rds_trak.h>

int
main(int argc, char *argv[])
  {
  trak_stop() ;

  usleep( 100 * 1000) ;

  mb_init() ;
  rp_init() ;
  ev_init() ;
  dp_init() ;
  bx_init() ;
  ex_init() ;
  const_init() ;
  q_init() ;
  printf("trak re-initialized\n") ; 
  }
