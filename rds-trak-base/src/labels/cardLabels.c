#include <stdio.h>
#include <string.h>

#include <pcl.h>

int
main(int argc, char *argv[])
  {
  int rows=15 ;
  int cols=4 ;
  int startX= 500 ;
  int startY= 650 ;
  int deltaX=1925 ;
  int deltaY= 625 ;
  int fontSize = 25;

  char data[10000][80+1] ;
  int d = 0 ;
  char tmp[80+1] ;
  int i ;

  while(fgets(tmp,80,stdin))
    {
    if(d>=10000) break ; 
    if(strlen(tmp)>0)
      tmp[strlen(tmp)-1]='\0' ;
    strncpy(data[d],tmp,80) ;
    data[d][80]='\0' ; 
    d++ ;
    }

pcl_init() ;
  for(i=0 ; i<d ; i++)
    {
    int p = i % 60 ;
    int r = p % 15 ;
    int c = p / 15 ;
    int x = startX + c * deltaX ;
    int y = startY + r * deltaY ;
    char name[80+1] ;
    char ip[80+1] ; 
    int o[4] ;
    char hex[16+1] ;

fprintf(stderr,"i %d p %d r %d c %d x %d y %d\n",i,p,r,c,x,y) ;

sscanf(data[i],"%s - %s",name,ip) ;
fprintf(stderr,"data [%s] name [%s] ip [%s]\n",data[i],name,ip) ;
sscanf(ip,"%d.%d.%d.%d",o+0,o+1,o+2,o+3) ;
fprintf(stderr,"o %d %d %d %d\n",o[0],o[1],o[2],o[3]) ;
sprintf(hex,"%X %X",o[3]/16,o[3]%16) ;
fprintf(stderr,"hex [%s]\n\n",hex) ;

    pcl_xy(x/10+0,y/10-10) ;
    pcl_font(25,0,3,4148) ;
    pcl_printf("%s",name) ;

    pcl_xy(x/10+0,y/10+10) ;
    pcl_font(15,0,0,4148) ;
    pcl_printf("%s",ip) ;

    pcl_xy(x/10+110,y/10+10) ;
    pcl_font(20,0,0,4148) ;
    pcl_printf("%s",hex) ;

    if( (p==59) && (i!=d) )
      {
      pcl_done() ;
      pcl_init() ;
      }
    }

pcl_done() ;
  }    

