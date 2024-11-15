/** 
 * Iterate through all rp's (and dp's, if specified), creating an entry in
 * the trak table for each one.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <rds_trak.h>
#include <rds_sql.h>

void fill_rp( void )
{
  int i, err ;
  char name[TRAK_NAME_LEN+1],
       desc[TRAK_DATA_LEN+1],
       driver[TRAK_NAME_LEN+1],
       driver_data[TRAK_NAME_LEN+1] ;
  int value ;
  char hostname[ 80 ];

  gethostname( hostname, 79 );

  for (i=0 ; i < RP_N ; i++)
    {
    if (rp_ok(i)) break ;
    value = rp_get(i) ;
    rp_settings(i,name,desc,driver,driver_data) ;

    // determine whether the rp already exists in the table
    err = sql_query(
        "SELECT * FROM trak "
        "WHERE zone = 'rp' "
        "AND name = '%s' "
        "AND `host`='%s'",
        name, hostname );

    // insert or update
    if (!err && sql_rowcount() == 1)
      sql_query(
          "UPDATE trak SET "
          "description = '%s' "
          "WHERE zone = 'rp' "
          "AND name = '%s' "
          "AND `host`='%s'",
          desc, name, hostname );
    else
      sql_query(
          "INSERT INTO trak (`host`,zone,name,`get`,standard,state,description) "
          "VALUES ('%s', 'rp', '%s', 0, %d, 'idle', '%s')",
          hostname, name, value, desc ) ;
    }
}

void fill_dp( void )
{
  int i, err ;
  char name[TRAK_NAME_LEN+1],
       desc[TRAK_DATA_LEN+1],
       driver[TRAK_NAME_LEN+1],
       driver_data[TRAK_NAME_LEN+1] ;
  int value ;
  char hostname[ 80 ];

  gethostname( hostname, 79 );

  for (i=0 ; i < DP_N ; i++)
    {
    if (dp_ok(i)) break ;
    value = dp_get(i) ;
    dp_settings(i,name,desc,driver,driver_data) ;

    // determine whether the dp already exists in the table
    err = sql_query(
        "SELECT * FROM trak "
        "WHERE zone = 'dp' "
        "AND name = '%s' "
        "AND `host`='%s'",
        name, hostname );

    // insert or update
    if (!err && sql_rowcount() == 1)
      sql_query(
          "UPDATE trak SET "
          "description = '%s' "
          "WHERE zone = 'dp' "
          "AND name = '%s' "
          "AND `host`='%s'",
          desc, name, hostname );
    else
      sql_query(
          "INSERT INTO trak (`host`,zone,name,`get`,standard,state,description) "
          "VALUES ('%s', 'dp', '%s', 0, %d, 'idle', '%s')",
          hostname, name, value, desc ) ;
    }
}

int main(int argc, char **argv)
{
  fill_rp();
  if (argc > 1 && strcmp( argv[1], "dp" ) == 0)
    fill_dp();
}
