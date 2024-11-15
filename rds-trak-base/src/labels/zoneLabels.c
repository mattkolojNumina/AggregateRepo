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

  char data[1000][80+1] ;
  int d = 0 ;
  char tmp[80+1] ;
  int i ;

  while(fgets(tmp,80,stdin))
    {
    if(d>=1000) break ; 
    if(strlen(tmp)>0)
      tmp[strlen(tmp)-1]='\0' ;
    strncpy(data[d],tmp,80) ;
    data[d][80]='\0' ; 
    d++ ;
    }

pcl_init() ;
pcl_font(fontSize,0,0,4148) ;
  for(i=0 ; i<d ; i++)
    {
    int p = i % 60 ;
    int r = p % 15 ;
    int c = p / 15 ;
    int x = startX + c * deltaX ;
    int y = startY + r * deltaY ;

fprintf(stderr,"i %d p %d r %d c %d x %d y %d\n",i,p,r,c,x,y) ;

    pcl_xy(x/10,y/10) ;
    pcl_printf("%s",data[i]) ;

    if( (p==59) && (i!=d) )
      {
      pcl_done() ;
      pcl_init() ;
      pcl_font(fontSize,0,0,4148) ;
      }
    }

pcl_done() ;
  }    

