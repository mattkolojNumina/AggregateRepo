#include <stdio.h>

#include <string.h>

int cp = 1 ;

int
main(int argc, char *argv[])
  {
  char dp[32+1] ;
  
  while(fgets(dp,32,stdin)!=0)
    {
    dp[strlen(dp)-1] = '\0' ;
    fprintf(stderr,"%s\n",dp) ;

    printf("REPLACE INTO trakObjects "
           "(host,dp,name,type) "
           "VALUES "
           "('org-trk%d','%s','%s','fault') ;\n",
            cp,dp,dp) ;
    printf("REPLACE INTO webObjects "
           "(name,area,type) "
           "VALUES "
           "('%s','main','fault') ;\n",
            dp) ;
    printf("INSERT IGNORE INTO events "
           "(code,description) "
           "VALUES "
           "('%s','%s') ;\n",
            dp,dp) ;
    }
  }
