%
%   app_lib.w -- application-specific utility functions
%
%   Author: Adam Marshall, Mark Woodworth
%
%   History:
%      2008-01-08 -AHM- init
%      2015-02-23 -AHM/RME- moved common utility functions to rds_util lib
%      2024-01-18 -MRW- modified for Orgill
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
%     (C) Copyright 2008-2024 Numina Systems Corporation.  All Rights Reserved.
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
\def\title{app\_lib}
\def\topofcontents{\null\smallskip
\centerline{\titlefont \title}
\smallskip
This library contains application-specific constants and utility functions.

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
\centerline{Control Date: $ $Date: 2024/06/05 15:35:18 $ $}
}

% --- copyright notice ---
\def\botofcontents{\vfill
\centerline{\copyright 2015 Numina Group, Inc.  All Rights Reserved.}
}

@* Overview. 
This library contains application-specific constants and utility functions.

@c
static char rcsid[] = "$Id: app_lib.w,v 1.1 2024/06/05 15:35:18 rds Exp $";
@<Includes@>@;
@<Defines@>@;
@<Prototypes@>@;
@<Exported Functions@>@;
@<Functions@>@;


@ The project header file, included by all applications.
@(app.h@>=
#ifndef __APP_H
#define __APP_H

@<Project Constants@>@;
@<Exported Prototypes@>@;

#endif

@ General constants shared among project apps.
@<Project Constants@>+=
#define HINT_LEN      64  // max length of the box hint


@ Included files.
@<Includes@>+=
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <unistd.h>

#include <rds_hist.h>
#include <rds_sql.h>
#include <rds_trn.h>
#include <rds_util.h>

#include "app.h"


@ Definitions.
@<Defines@>+=
#define BUF_LEN   32  // length of small statically allocated strings
#define LBUF_LEN  64  // length of small statically allocated strings
#define MSG_LEN  256  // length of longer message strings


@* Application-specific utilities.

@ Find the cartonSeq from the lpn.
@<Exported Functions@>+=
int
app_lookup_cartonSeq(char lpn[]) 
  {
  int cartonSeq = -1 ;
  int err ;

  err = sql_query("SELECT cartonSeq "
                  "FROM rdsCartons "
                  "WHERE lpn='%s' "
                  "ORDER BY cartonSeq DESC ",
                  lpn) ;
  if(!err)
    {
    if(sql_rowcount()>0)
      if(sql_get(0,0))
        cartonSeq = atoi(sql_get(0,0)) ;
    }
  else
    Alert("SQL error %d select rdsCartons",err) ;

  return cartonSeq ;
  }

@ Proto.
@<Exported Prototypes@>+=
int app_lookup_cartonSeq(char lpn[]) ;

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * 
app_get_carton_val_lpn(const char carton_lpn[],const char column_name[]) 
  {
  // char val_arr[32 + 1];
  char *val 
    = sql_getvalue(" SELECT `%s` FROM rdsCartons "
                   " WHERE lpn = '%s'",
                   column_name, carton_lpn );

  if (val == NULL || strlen(val) == 0 )
    return "";

  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_carton_val_lpn(const char carton_lpn[],const char column_name[]); 

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * 
app_get_carton_val(const int carton_seq,const char column_name[]) 
  {
  char *val 
    = sql_getvalue(" SELECT `%s` FROM rdsCartons "
                   " WHERE cartonSeq = %d",
                   column_name, carton_seq );

  if (val == NULL || strlen(val) == 0 )
    return "";

  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_carton_val(const int carton_seq,const char column_name[]); 

@ Get the value of a carton parameter.
@<Exported Functions@>+=
char * 
app_get_carton_data_val(const int carton_seq,const char data_type[]) 
  {
  char *val 
    = sql_getvalue(" SELECT dataValue FROM rdsCartonData "
                   " WHERE cartonSeq = %d "
                   " AND dataType = '%s'",
                   carton_seq, data_type );

  if (val == NULL || strlen(val) == 0 )
    return "";
  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_carton_data_val(const int carton_seq,const char data_type[]); 

@ Set the value of a carton parameter.
@<Exported Functions@>+=
int 
app_set_carton_val_lpn(const char carton_lpn[],
                       const char column_name[],const char value[]) 
  {
  int err ;

  err = sql_query("UPDATE rdsCartons SET `%s`='%s' "
                  "WHERE lpn = '%s'",
                  column_name, value, carton_lpn );

  return err;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_set_carton_val_lpn(const char carton_lpn[], 
                           const char column_name[],const char value[]);

@ Set the value of a carton parameter.
@<Exported Functions@>+=
int 
app_set_carton_val(const int carton_seq, 
                   const char column_name[],const char value[]) 
  {
  int err ;

  err = sql_query("UPDATE rdsCartons SET `%s`='%s' "
                  "WHERE cartonSeq = %d",
                  column_name, value, carton_seq );

  return err;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_set_carton_val(const int carton_seq,
                       const char column_name[],const char value[]); 

@ Get the type of a order.
@<Exported Functions@>+=
char * 
app_get_order_val(const char order_id[],const char column_name[]) 
  {
  char *val 
    =  sql_getvalue("SELECT `%s` FROM custOrders " 
                    "WHERE orderId = '%s' "
                    "AND `status`<>'canceled'; ",
                    column_name, order_id );

  if (val == NULL || strlen(val) == 0 )
    return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_order_val(const char order_id[],const char column_name[]); 

@ Get the type of a order.
@<Exported Functions@>+=
char * 
app_get_rdsDocuments_val(const char ref_value[], 
                         const char column_name[],const char doc_type[]) 
  {
  char * val 
    =  sql_getvalue("SELECT `%s` FROM rdsDocuments " 
                    "WHERE docType = '%s' AND refValue = '%s' ", 
                    column_name, doc_type, ref_value);

  if (val == NULL || strlen(val) == 0 )
    return "";

  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_rdsDocuments_val(const char ref_value[], 
                               const char column_name[],const char doc_type[]); 

@ Get the type of a order.
@<Exported Functions@>+=
char * 
app_get_label_val(const char seq[],const char column_name[]) 
  {
  char *val 
    =  sql_getvalue("SELECT `%s` FROM labels " 
                    "WHERE seq = %s; ", 
                    seq, column_name);

  if (val == NULL || strlen(val) == 0 )
    return "";

  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_label_val(const char seq[],const char column_name[]); 

@ Get physical lane from the ship method
@<Exported Functions@>+=
char *
app_get_physical_lane(const char ship_method[]) 
  {
  char *val 
    = sql_getvalue("SELECT `physical` FROM `cfgLogicalLanes` "
                   "WHERE `logical` = '%s' ",
                   ship_method );

  if (val == NULL || strlen(val) == 0 )
    return "exception";
  return val;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_physical_lane(const char ship_method[]);

@ Load label record from rdsDocuments.
@<Exported Functions@>+=
int
app_label_load(int cartonSeq, char area[], int seq)
  {
  int err ;
  int tries = 0 ;
  int docSeq = -1 ;
  char verify[128] ;

  while(tries < MAX_TRIES)
    {
    err = sql_query("SELECT docSeq "
                    "FROM rdsDocuments "
                    "WHERE refValue=%d "
                    "AND refType='cartonSeq' "
                    "AND docType='shipLabel' ",
                    cartonSeq) ;
    if(!err)
      {
      if(sql_rowcount()>0)
        {
        if(sql_get(0,0))
          {
          docSeq = atoi(sql_get(0,0)) ;
          break ;
          }
        else
          {
          Alert("SQL error NULL docSeq") ;
          return -1 ; // sql in error
          }
        }
      else
        {
        usleep(100000L) ;
        tries++ ;
        continue ;
        }
      }
    else
      {
      Alert("SQL error %d select rdsDocuments",err) ;
      return -1 ; // sql in error 
      }
    }
                 
  if(docSeq>=0)
    {
    err = sql_query("REPLACE INTO labels "
                    "(seq,ordinal,printed,zpl) "
                    "SELECT %d, 1, 'no', document "
                    "FROM rdsDocuments "
                    "WHERE docSeq=%d "
                    "AND docType='shipLabel' "
                    "AND refType='cartonSeq' "
                    "AND refValue='%d' ",
                    seq,docSeq,cartonSeq) ;
    if(err)
      {
      Alert("SQL document copy fails %d",err) ;
      return -2 ; // sql out error 
      }
  
    verify[0] = '\0' ;
    err = sql_query("SELECT verification "
                    "FROM rdsDocuments "
                    "WHERE docSeq=%d ",
                    docSeq) ;
    if(!err)
      if(sql_rowcount()>0)
        if(sql_get(0,0))
          strncpy(verify,sql_get(0,0),128) ;
    verify[128] = '\0' ;

    util_carton_set(area,seq,"verify",verify) ;

    return docSeq ;
    }

  return 0 ; // document not found
  }

@ Proto.
@<Exported Prototypes@>+=
int app_label_load(int cartonSeq, char area[], int seq) ;

@ Max tries (0.1 sec)
@<Defines@>+=
#define MAX_TRIES (100)

@ Update the carton ship label from the host result.
@<Exported Functions@>+=
void 
app_update_carton_ship_label(const char area[],int seq,int carton_seq) 
  {
  int err;
  char verification[ LBUF_LEN + 1 ];

  if (carton_seq < 0)
    return;

  err = sql_query("SELECT `verification` "
                  "FROM `rdsDocuments` "
                  "WHERE `docType` = 'shipLabel' "
                  "AND `refValue` = %d",
                  carton_seq );
  if (err || sql_rowcount() == 0) 
    {
    Alert("sql error (%d) determining label data for carton [%d]",
          err, carton_seq );
    return;
    }

  strncpy(verification,sql_get(0,0),LBUF_LEN); 
  verification[LBUF_LEN] = '\0';
      
  Inform("  ship label for carton [%d], verify [%s]", 
         carton_seq, verification );
  sql_query("REPLACE labels (seq, ordinal, zpl) "
            "SELECT %d, 1, "
            "REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( "
            "REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( document, "
            "'^LH', '^FX' ), "    // label home
            "'^MCN', '^FXN' ), "  // map clear - no
            "'^MD', '^FX' ), "    // media darkness
            "'^MM', '^FX' ), "    // media mode
            "'^MN', '^FX' ), "    // media tracking
            "'^PM', '^FX' ), "    // mirror image
            "'^PQ', '^FX' ), "    // print quantity
            "'^PR', '^FX' ), "    // print rate
            "'^PW', '^FX' ), "    // print width
            "'~SD', '^FX' ), "    // set darkness
            "'^XA', '^XA^LH0,0' ), "  // set origin at start
            "'^XZ', '^XZ' ) AS zpl "  // placeholder at end
            "FROM rdsDocuments "
            "WHERE `docType` = 'shipLabel' "
            "AND `refValue` = %d "
            "AND LENGTH( document ) > 0",
            seq, carton_seq );
  Trace("Setting verify value to tracking number");
  util_carton_set(area,seq,"verify",verification);
  util_update_status( seq, "label", "pending", "" );
  }

@ Prototype the function.
@<Exported Prototypes@>+=
void app_update_carton_ship_label(const char area[],int seq,int carton_seq);

@ Parse a scanner message for valid lpn barcode(s).
@<Exported Functions@>+=
int 
app_parse_scan(const char scan_msg[],char barcode[]) 
  {
  char *str, *str_ptr;
  char found[10][64+1] ;
  int i, n_found ;

  n_found = 0;
  strcpy( barcode, "" );
  for(i=0 ; i<10 ; i++)
    found[i][0]='\0' ;

  str = strdup( scan_msg );
  str_ptr = str;
  while (str_ptr != NULL) 
    {
    char *candidate = strsep( &str_ptr, "," );

    if (is_valid(candidate)) 
      {
      Inform( "     valid barcode [%s]",candidate );
      for(i=0 ; i<n_found ; i++)
        {
        if(0==strcmp(found[i],candidate))
          {
          Inform("      already found");
          break ;
          }
        }
      if(i==n_found)
        {
        Inform("      new barcode, add") ;
        if(i<(10-1))
          {
          strncpy(found[n_found],candidate,64) ;
          found[n_found][64] = '\0' ;
          n_found++ ;
          }
        else
          Inform("     too many barcodes, drop") ;
        }
      } 
    else 
      {
      Inform( "     ignore barcode [%s]",candidate );
      }
    if(n_found>0)
      strcpy(barcode,found[0]) ;
    }
  free( str );

  return n_found;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_parse_scan(const char scan_msg[],char barcode[]);

@ Is valid: is this a valid LPN barcode
@<Functions@>+=
int
is_valid(char *candidate) 
  {
  if(strlen(candidate)!=9)
    return 0 ;
  if( (candidate[0]!='T') && (candidate[0]!='C') )
    return 0 ;
  return 1 ;
  }

@ Proto.
@<Prototypes@>+=
int is_valid(char *candidate) ;

@ Update the carton description and post carton history.
@<Exported Functions@>+=
void 
app_update_hist(const char area[],int seq,int carton_seq) 
  {
  if (area==NULL || strlen( area )==0 || seq<=0 || carton_seq<=0)
    return;

  sql_query("UPDATE cartonLog SET "
            "id = '%d', "
            "stamp = stamp "
            "WHERE id = 'seq%d'",
            carton_seq, seq );

  }

@ Prototype the function.
@<Exported Prototypes@>+=
void app_update_hist(const char area[],int seq,int carton_seq);

@ Get the mouse-over hint for a box.
@<Exported Functions@>+=
char *
app_get_hint(int box) 
  {
  int seq = -1;
  static char hint[ HINT_LEN + 1 ];
  char *val, area[ BUF_LEN + 1 ];

  if(box<MIN_TRAK_BOX || box>MAX_TRAK_BOX) 
    strcpy( hint, "" );
  else if( (seq=util_box_get_int(box,"seq"))<=0 )
    snprintf( hint, HINT_LEN, "<%d>", box );
  else 
    {
    val = util_box_get( box, "area" );
    if( val!=NULL && strlen(val)>0 ) 
      {
      strncpy(area,val,sizeof(area)-1);
      area[sizeof(area)-1]='\0';

      val = util_carton_get(area,seq,"barcode");
      if(val!=NULL && strlen(val)>0 )
        snprintf(hint,HINT_LEN,"[%s]",val);
      else 
        snprintf(hint,HINT_LEN,"carton %d",seq);
      }
    else 
      snprintf(hint,HINT_LEN,"carton %d",seq);
    }
  hint[ HINT_LEN ] = '\0';
  return( hint );
  }

@ Prototype the function.
@<Exported Prototypes@>+=
char *app_get_hint(int box);

@ Generate a simple label with a barcode and/or a message.
@<Exported Functions@>+=
void 
app_generate_simple_label(char label[],const char barcode[],const char msg[]) 
  {
  char tmp[ 2 * MSG_LEN + 1 ];

  sprintf(label, "^XA\n^LH0,0^FS\n" );

  if(strlen(barcode)>0) 
    {
    sprintf(tmp,
            "^FO200,400^BY3,,102\n"
            "^BCN^FD%s^FS\n",
            barcode );
    strcat(label,tmp);
    }

  if(strlen(msg)>0) 
    {
    sprintf(tmp,
            "^FO100,600^A0N,30,30^FB600,8^FD%s^FS\n",
            msg );
    strcat(label,tmp);
    }

  strcat( label, "^XZ\n" );
  }

@ Prototype the function.
@<Exported Prototypes@>+=
void app_generate_simple_label(char label[],const char barcode[],
                               const char msg[]);

@ Determine if labels are ready for printing.  Returns ordinal (page
number) of next ready label; otherwise returns -1 on error or 0 if
no label is ready.
@<Exported Functions@>+=
int 
app_label_ready(int seq,const char printer[]) 
  {
  char *val;
  char carton_id[ BUF_LEN + 1 ];
  int ordinal;

  Inform( "  checking if label is ready..." );
  val = sql_getvalue("SELECT status FROM cartonStatus "
                     "WHERE seq = %d "
                     "AND name = 'label'",
                     seq );
  if(val==NULL || strcmp(val,"failed")==0)
    return -1;

  if(strcmp(val,"complete")!=0)
    return 0;

  sprintf(carton_id,"%d",seq);
  val = sql_getvalue("SELECT ordinal FROM labels "
                     "WHERE seq = %d "
                     "AND printer = '%s' "
                     "AND printed = 'no' "
                     "ORDER BY ordinal LIMIT 1",
                     seq, printer );
   if(val==NULL || (ordinal = atoi( val )) <= 0)
     return -1;

  return ordinal;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_label_ready( int seq, const char printer[] );

@ Get a label for printing.
@<Exported Functions@>+=
int 
app_get_label(int seq,const char printer[],char label[],int *len) 
  {
  int err, ordinal, llen;
  char *val;
  char carton_id[ BUF_LEN + 1 ];
  int i;

  Inform( "  getting label..." );
  strcpy( label, "" );

  sprintf( carton_id, "%d", seq );
  err = sql_query("SELECT ordinal, zpl FROM labels "
                  "WHERE seq = %d "
                  "AND printer = '%s' "
                  "AND printed = 'no' "
                  "ORDER BY ordinal LIMIT 1",
                  seq, printer );

  if(err)
    {
    Alert("sql error retrieving label data, err = %d",err);
    return 0;
    }

  if(sql_rowcount() != 1)
    return 0;

  ordinal = atoi(sql_get(0,0));
  val = sql_getlen(0,1,&llen);
  if (llen > *len) 
    {
    *len = 0;
    return 0;
    }
  bcopy( val, label, llen );

  llen = zpl_remove_safe( label, llen, "^MCN" ); // map clear - no
  llen = zpl_remove_safe( label, llen, "^MD" );  // media darkness
  llen = zpl_remove_safe( label, llen, "^MM" );  // media mode
  llen = zpl_remove_safe( label, llen, "^MN" );  // media tracking
  llen = zpl_remove_safe( label, llen, "^PM" );  // mirror image
  llen = zpl_remove_safe( label, llen, "^PO" );  // print orientation
  llen = zpl_remove_safe( label, llen, "^PQ" );  // print quantity
  llen = zpl_remove_safe( label, llen, "^PR" );  // print rate
  llen = zpl_remove_safe( label, llen, "^PW" );  // print width
  llen = zpl_remove_safe( label, llen, "~SD" );  // set darkness

  *len = llen;

  if (*len < 40)
    Inform( "label data [%s]", label );

  return ordinal;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_label(int seq,const char printer[],char label[],int *len);

@ Mark a label as successfully printed.
@<Exported Functions@>+=
void 
app_label_printed(int seq,const char printer[],int ordinal) 
  {
  char *val, carton_id[ BUF_LEN + 1 ];

  Inform("  marking label page %d as printed...",ordinal);

  sprintf( carton_id, "%d", seq );
  sql_query("UPDATE labels SET "
            "printed = 'yes' "
            "WHERE seq = %d "
            "AND printer = '%s' "
            "AND ordinal = %d",
            seq, printer, ordinal );

  val = sql_getvalue("SELECT COUNT(*) FROM labels "
                     "WHERE seq = %d "
                     "AND printer = '%s' "
                     "AND printed = 'no' ", 
                     seq, printer);
  if( val!=NULL && atoi(val)==0 )
  util_carton_set( "xpal", seq, "printed", "1" );
  }

@ Prototype the function.
@<Exported Prototypes@>+=
void app_label_printed(int seq,const char printer[],int ordinal);

@ Remove ZPL commands by replacing the command with spaces (safe for
binary input).
@<Functions@>+=
int 
zpl_remove_safe(char *pszZpl,int len,const char *pszZplCmd) 
  {
  char *pc ;
  int i ;
  int cmd_len = strlen(pszZplCmd) ;
  for (pc=pszZpl, i=0 ; i<len ; i++, pc++) 
    {
    if (!strncmp(pc,pszZplCmd,cmd_len)) 
      {
      int j ;
      for (j=0 ; j<cmd_len ; j++) 
        *pc++ = 0x20 ;
      while(*pc != '^') 
        *pc++ = 0x20 ;
      return len;
      }
    }

  return len;
  }

@ Prototype the function.
@<Prototypes@>+=
int zpl_remove_safe(char *pszZpl,int len,const char *pszZplCmd);

@ Determine the sort lane.
@<Exported Functions@>+=
int 
app_get_lane(const char area[],const char logical[],char description[]) 
  {
  int lane = -1;
  int err;

  err = sql_query("SELECT physical, description FROM cfgLanes "
                  "WHERE area = '%s' "
                  "AND logical = '%s'",
                  area, logical );
  if (err) 
    {
    strcpy(description,"unknown");
    Alert("sql error (%d) determining lane for [%s/%s]",
          err, area, logical );
    return 0;
    }

  lane = atoi(sql_get(0,0));
  strncpy(description,sql_get(0,1),MSG_LEN);
  description[ MSG_LEN ] = '\0';
  Trace("     sort lookup: %s/%s -> %s (%d)",
        area, logical, description, lane );

  return lane;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_lane( const char area[],const char logical[],char description[]);

@ Trigger a scale to transmit.
@<Exported Functions@>+=
int 
app_trigger_scale(const char dev_name[]) 
  {
  int err;

  err = sql_query("UPDATE runtime "
                  "SET value = CONCAT('SRP','\\r') "
                  "WHERE name = '%s/xmit' ", 
                  dev_name);

  return !err;
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_trigger_scale(const char dev_name[]);

@ Get the physical value of lane form cfgLanes.
@<Exported Functions@>+=
int 
app_get_physical_code(const char area[],const char physical[]) 
  {
  char *val 
    = sql_getvalue("SELECT `physicalCode` "
                   "FROM `cfgPhysicalLanes` "
                   "WHERE `area` = '%s' "
                   "AND `physical` = '%s' ",
                   area, physical );

  if (val==NULL || strlen(val)==0 )
    return 0;
  return atoi(val);
  }

@ Prototype the function.
@<Exported Prototypes@>+=
int app_get_physical_code( const char area[], const char physical[] );

@ Generate a simple label with a barcode and/or a message.
@<Exported Functions@>+=
void 
app_get_lpn_label(char label[],int *len) 
  {
  char tmp[ 2 * MSG_LEN + 1 ];
  char carton_type[ BUF_LEN + 1 ];

  sprintf(label,
          "^XA"
          "^FWR"
          "^BY3,2,130"
          "^FO375,100^BC^FD" );

  int lpn_seq 
    = atoi(sql_getvalue("SELECT value FROM runtime "
                        "WHERE name = 'lpn/ctr'"));
  if (lpn_seq < 0)
      lpn_seq = 0;

  sprintf( tmp, "B%07d", lpn_seq);
  strcat( label, tmp );

  sql_query("REPLACE INTO runtime SET name = 'lpn/ctr', value = '%d'",
            lpn_seq + 1);

  strcat(label,"^XZ");

  *len = strlen( label );   
  }

@ Prototype the function.
@<Exported Prototypes@>+=
void app_get_lpn_label(char label[],int *len);

@ Carton ready to label
@<Exported Functions@>+=
int
app_carton_ready(int cartonSeq)
  {
  return 1 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int app_carton_ready(int cartonSeq) ;

@ First box.
@<Exported Functions@>+=
int
app_first_box(char *area)
  {
  int first = 1 ;

  if(0==strcmp(area,"eastPack"))
    first = 1 ;
  else if(0==strcmp(area,"east"))
    first = 26 ;
  return first ;
  }

@ Proto.
@<Exported Prototypes@>+=
int app_first_box(char *area) ;

@ Last box.
@<Exported Functions@>+=
int
app_last_box(char *area)
  {
  int last = 999 ;

  if(0==strcmp(area,"eastPack"))
    last = 25 ;
  else if(0==strcmp(area,"east"))
    last = 75 ; 

  return last ;
  }

@ Proto.
@<Exported Prototypes@>+=
int app_last_box(char *area) ;

@ Carton needs QC.
@<Exported Functions@>+=
int
app_carton_needs_qc(int cartonSeq) 
  {
  return 0 ;
  }

@ Proto.
@<Exported Prototypes@>+=
int app_carton_needs_qc(int cartonSeq) ;

@ String from int.
@<Exported Functions@>+=
char *
string(int val)
  {
  static char s[32+1] ;

  snprintf(s,32,"%d",val) ;
  s[32] = '\0' ;

  return s ;
  }

@ Proto.
@<Exported Prototypes@>+=
char *string(int val) ;        

@ PlaceHolder.
@<Functions@>+=
void proto(void) {} ;
@ Proto.
@<Prototypes@>+=
void proto(void) ;

@* Index.

