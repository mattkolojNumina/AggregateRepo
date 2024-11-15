%
%   east_verify.w -- verify the printed label
%
%   Author: Adam Marshall
%
%   History:
%      2010-08-24 -AHM- init, for X-Press PAL
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
%     (C) Copyright 2010 Numina Systems Corporation.  All Rights Reserved.
%
%
%

% --- helpful macros ---
\def\dot{\item{ $\bullet$}}
\def\boxit#1#2{\vbox{\hrule\hbox{\vrule\kern#1\vbox{\kern#1#2\kern#1}%
   \kern#1\vrule}\hrule}}
\def\today{\ifcase\month\or
   January\or February\or March\or April\or May\or June\or
   July\or August\or September\or October\or November\or December\or\fi
   \space\number\day, \number\year}

% --- title block ---
\def\title{EAST verify}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This program verifies the tracking number on the printed shipping label.

% --- confidentiality statement ---
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

% --- author and version ---
\bigskip
\centerline{Author: Adam Marshall}
\centerline{Printing Date: \today}
\centerline{Control Revision: $ $Revision: 1.1 $ $}
\centerline{Control Date: $ $Date: 2022/07/05 18:52:55 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2008 Numina Systems Corporation.  
All Rights Reserved.}
}

@* Overview. 
This program verifies the tracking number on the printed shipping label.

@c
static char rcsid[] = "$Id: xpal_verify.w,v 1.1 2022/07/05 18:52:55 rds Exp rds $";
@<Includes@>@;
@<Defines@>@;
@<Globals@>@;
@<Prototypes@>@;
@<Functions@>@;

int 
main( int argc, char *argv[] ) 
  {
  int box;
  int seq;

  @<initialize@>@;
  @<process carton@>@;

  exit( EXIT_SUCCESS );
  }

@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/time.h>  // for gettimeofday()

#include <app.h>
#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_trak.h>
#include <rds_ctr.h>
#include <rds_util.h>
#include <zonetrak.h>


@ Definitions.
@<Defines@>+=
#define BUF_LEN          32  // standard length for strings
#define BUF_LRG         128  // larger length for strings 
#define MSG_LEN         256  // length of longer message strings
#define CODE_SUCCESS      1  // status code for success
#define CODE_ERROR       -1  // status code for unspecified error
#define CODE_LOST       -99  // status code for lost box

@ Global status variables.
@<Globals@>+=
char area[ BUF_LEN + 1 ];
char name[ BUF_LRG + 1 ];

@ Initialization.
@<initialize@>+=
  {
  int first, last ;

  if (argc != 3) 
    {
    printf( "usage: %s <area> <box>\n", argv[0] );
    exit( EXIT_FAILURE );
    }
 
  strncpy(area,argv[1],BUF_LEN) ;
  area[BUF_LEN] = '\0' ;
 
  sprintf( name, "%sVerify", area);
  trn_register( name );
 
  first = app_first_box(area) ;
  last  = app_last_box(area) ;
 
  box = atoi( argv[2] );
  if (box<first || box>last) 
    {
    Alert( "invalid box [%d]", box );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }
  
  seq = util_box_get_int( box, "seq" );
  if (seq <= 0) 
    {
    Alert( "%03d: invalid carton [%d]", box, seq );
    util_zone_release( name );
    exit( EXIT_SUCCESS );
    }
  
  Trace( "%03d: verify carton [%d]", box, seq );
  }

@ Process the carton.
@<process carton@>=
  {  
  struct timeval start_time;
  char barcode[MSG_LEN+1] ;
  int barcode_success ;
  int validate_success ;
  int verify_success ;
  int success ;
  int code ;

  @<begin processing@>@;
  @<get barcode@>@;
  @<validate@>@;
  @<verify@>@;
  @<determine status@>@;
  @<assign lane@>@;
  @<finish processing@>@;
  }

@ Begin processing: initialization, etc.
@<begin processing@>=
  {
  gettimeofday( &start_time, NULL );
  }

@ Finish processing.
@<finish processing@>=
  {
  Inform( "%03d: processing complete for carton [%d], took %.3f sec",
         box, seq, util_get_elapsed( start_time ) );
  util_zone_release( name );
  }

@ Get barcode string.
@<get barcode@>=
  {
  char description[ MSG_LEN + 1 ];

  if (!util_required(seq,"verify") && (!util_required(seq,"validate"))) 
    {
    Inform( "%03d: verify processing not required for [%d]", box, seq );
    strcpy(description,"verification not required") ;
    barcode_success = TRUE;
    }
  else
    {
    char status_val[ BUF_LEN + 1 ];
    char dev_name[ BUF_LEN + 1 ];
    char dev_msg[ MSG_LEN + 1 ];

    barcode_success = FALSE;
    
    status_val[0] = '\0' ;
    description[0] = '\0' ;

    strncpy(dev_name,util_get_control(name,"scan",""),BUF_LEN);
    dev_name[ BUF_LEN ] = '\0';
    strcpy( dev_msg, "" );
    if (dev_name == NULL || strlen( dev_name ) == 0)
      {
      Alert( "no scanner configured" );
      strcpy(description,"no scanner configured") ;
      }
    else 
      {
      util_poll_for_msg( box, dev_name, dev_msg );
      Inform( "%03d: [%s] msg [%s]", box, dev_name, dev_msg );
      ctr_incr( "/%s", dev_name );
      if (strlen( dev_msg ) == 0) 
        {
        Trace("%03d: %s result missing",box,dev_name);
        strcpy(description,"no message from scanner") ;
        ctr_incr( "/%s/fail", dev_name );
        ctr_incr( "/%s/fail/missing", dev_name );
        } 
      else if (dev_msg[0] == '?') 
        {
        Trace("%03d: %s no read", box, dev_name);
        strcpy(description,"no read from scanner") ;
        ctr_incr( "/%s/fail", dev_name );
        ctr_incr( "/%s/fail/noread", dev_name );
        } 
      else 
        {
        strncpy(barcode,dev_msg,BUF_LEN) ;
        barcode[MSG_LEN]='\0' ;
        Trace("%03d: %s barcode [%s]",box,dev_name,barcode);
        strcpy(description,"valid barcode read") ;
        barcode_success = TRUE ;
        ctr_incr( "/%s/ok", dev_name );
        }
      }
    }
  }

@ Validate the LPN scan.
@<validate@>+=
  {
  char description[ MSG_LEN + 1 ];

  validate_success = FALSE ;

  if(barcode_success)
    {
    if (!util_required(seq,"validate")) 
      {
      Inform( "%03d: validation not required for [%d]", box, seq );
      strcpy(description,"validation not required") ;
      validate_success = TRUE ;
      }
    else
      {
      char lpn[MSG_LEN+1] ;
      int err ;
 
      lpn[0] = '\0' ;
      err = sql_query("SELECT lpn FROM eastCartons "
                      "WHERE seq=%d ",seq) ;
      if(!err)
        if(sql_rowcount()>0)
          if(sql_get(0,0))
            strncpy(lpn,sql_get(0,0),MSG_LEN) ;
      lpn[MSG_LEN] = '\0' ;
 
      if(lpn[0]!='\0')
        if(strstr(barcode,lpn)!=NULL)
          validate_success = TRUE ;
      Inform("%03d: lpn [%s] scan[%s] %s",
             box,lpn,barcode,validate_success?"MATCH":"FAIL") ;     

      if(validate_success)
        {
        util_update_status(seq,"validate","complete",lpn) ;
        strcpy(description,"lpn validated") ;
        }
      else
        {
        util_update_status(seq,"validate","failed","") ;
        strcpy(description,"lpn failed validation") ;
        }
      }
    }
  }

@ Verify the label.
@<verify@>+=
  {
  char description[ MSG_LEN + 1 ];

  verify_success = FALSE ;

  if(barcode_success)
    {
    if (!util_required(seq,"verify")) 
      {
      Inform( "%03d: verify not required for [%d]", box, seq );
      strcpy(description,"verify not required") ;
      verify_success = TRUE ;
      }
    else
      {
      char verify[MSG_LEN+1] ;
      int err ;
 
      verify[0] = '\0' ;
      err = sql_query("SELECT verify FROM eastCartons "
                      "WHERE seq=%d ",seq) ;
      if(!err)
        if(sql_rowcount()>0)
          if(sql_get(0,0))
            strncpy(verify,sql_get(0,0),MSG_LEN) ;
      verify[MSG_LEN] = '\0' ;
 
      if(verify[0]!='\0')
        if(strstr(barcode,verify)!=NULL)
          verify_success = TRUE ;
      Inform("%03d: verify [%s] scan[%s] %s",
             box,verify,barcode,verify_success?"MATCH":"FAIL") ;     

      if(verify_success)
        {
        util_update_status(seq,"verify","complete",verify) ;
        strcpy(description,"print verified") ;
        }
      else
        {
        util_update_status(seq,"verify","failed","") ;
        strcpy(description,"print failed verification") ;
        }
      }
    }
  }

@ Determine the carton status.
@<determine status@>=
  {
  char result_name[ BUF_LEN + 1 ];
  char result_status[ BUF_LEN + 1 ];
  char result_value[ BUF_LEN + 1 ];
  char result_desc[ MSG_LEN + 1 ];

  code = get_carton_result( seq, result_name, result_status,
                            result_value, result_desc );

  ctr_incr( "/%s", area );
  if (code >= 0) 
    {
    Trace( "%03d: %s for [%d] (code %d)", box, result_desc, seq, code );
    ctr_incr( "/%s/pass", area );
    ctr_incr( "/%s/pass/%s", area, result_name );
    } 
  else 
    {
    Alert( "%03d: %s for [%d] (code %d)", box, result_desc, seq, code );
    ctr_incr( "/%s/fail", area );
    ctr_incr( "/%s/fail/%s", area, result_name );
    ctr_incr( "/%s/fail/%s/%s", area, result_name, result_status );
    if (strlen( result_value ) > 0)
      ctr_incr( "/%s/fail/%s/%s/%s",
                area, result_name, result_status, result_value );
    }

  // update the carton
  util_update_description( area, seq, area, "%s at print/apply", result_desc );
  sql_query("UPDATE `%sCartons` "
            "SET status = %d "
            "WHERE seq = %d",
            area, code, seq );
  }

@ Determine and assign the physical lane for sortation.
@<assign lane@>=
  {
  int exception = 3 ;
  int pass = 1 ;
  int physical ;
  char logical[BUF_LEN+1] ;
  char description[MSG_LEN+1] ;

  if(0==strcmp(area,"west"))
    {
    exception = 3 ;
    pass = 1 ;
    }

  physical =  exception ;
  strcpy(description,"exception") ;
  strcpy(logical,"exception") ;
  if(code>0)
    {
    physical = pass ;
    strcpy(description,"pass") ;
    strcpy(logical,"pass") ;
    }
    
  bx_setdata( box, physical );

  Trace( "%03d: carton [%d] sorted to %s (%d)",
         box, seq, description, physical);
  util_update_description( area, seq, "xpal", "sorted to %s", description );

  ctr_incr( "/%s-sort", area );
  ctr_incr( "/%s-sort/%s", area, logical );
  }

@ Determine the processing status for a carton.
@<Functions@>+=
int 
get_carton_result(int seq, 
                  char result_name[], char result_status[],
                  char result_value[], char description[] ) 
  {
  int err, code;

  strcpy( result_name, "" );
  strcpy( result_status, "" );
  strcpy( result_value, "" );
  strcpy( description, "" );

  err = sql_query("SELECT name, status, value FROM cartonStatus "
                  "WHERE seq = %d "
                  "AND status IN ('pending', 'failed') "
                  "ORDER BY ordinal LIMIT 1",
                   seq );
  if(err)
    {
    strcpy(result_name,"unknown");
    strcpy(description,"unable to determine carton status" );
    Alert("sql error (%d) determining carton status for [%d]",
          err, seq );
    return CODE_ERROR;
    }

  if(sql_rowcount()==0)
    {
    strcpy( result_name, "success" );
    strcpy( description, "carton processed successfully" );
    return CODE_SUCCESS;
    }

  strncpy(result_name,sql_get(0,0),BUF_LEN);
  result_name[BUF_LEN] = '\0';
  strncpy(result_status,sql_get(0,1),BUF_LEN);
  result_status[BUF_LEN] = '\0';
  strncpy(result_value,sql_get(0,2),BUF_LEN);
  result_value[ BUF_LEN ] = '\0';
  Inform("carton [%d] has result [%s-%s], value = [%s]",
         seq, result_name, result_status, result_value );

  // look up the result description
  err = sql_query("SELECT code, result FROM results "
                  "WHERE name = '%s' "
                  "AND status = '%s' "
                  "AND value = '%s'",
                  result_name, result_status, result_value );
  if (!err && sql_rowcount() == 1) 
    {
    code = atoi(sql_get( 0, 0 ));
    strcpy(description,sql_get( 0, 1 ));
    } 
  else 
    {
    code = CODE_ERROR;
    if(strlen(result_value)>0)
      sprintf(description,"%s %s, value = %s",
              result_name, result_status, result_value );
    else
      sprintf( description, "%s %s", result_name, result_status );
    }
  return code;
  }

@ Prototype the function.
@<Prototypes@>+=
int get_carton_result( int seq, char result_name[], char result_status[],
      char result_value[], char description[] );

@ Update processing status.
@<update carton@>=
  {
  }

@* Index.
