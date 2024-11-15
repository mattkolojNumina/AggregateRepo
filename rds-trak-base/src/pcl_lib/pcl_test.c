/*48:*/
#line 653 "pcl_lib.w"

#include <stdio.h>  
#include <pcl.h>  
#include <rds_trn.h>  

int
main()
{
trn_register("pcl_test");
pcl_init();
pcl_xy(100,100);
pcl_printf("pcl_code128c(100,200,50,3,\"0123456789\")");
pcl_code128c(100,200,50,3,"0123456789");
pcl_done();
}

/*:48*/
