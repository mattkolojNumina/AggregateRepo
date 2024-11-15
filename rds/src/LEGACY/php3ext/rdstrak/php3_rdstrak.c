/**********************************************************************
* php3_rdstrak.c - RDSTRAK extension library for PHP3
*
* Copyright (C) 2001 Numina Systems Corp.
* Author: Alex Korzhuk
* Last Modified: 5/23/2001
***********************************************************************/
#include "php.h"
#include "internal_functions.h"
#include "/home/rds/include/rds_trak.h"

#if WIN32|WINNT
  #include <windows.h>
  #define DLEXPORT __declspec(dllexport)
#else
  #define DLEXPORT
#endif

/* Functions accessable to PHP */
DLEXPORT void php3_rdstrak_rplist( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rptotal( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpsettings( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpsettings_( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rphandle( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpget( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpset( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_strlen( INTERNAL_FUNCTION_PARAMETERS );

#define SAFE( s )  ( ( s ) ? ( s ) : "" )

/***************************************************************************
* {"PHPScriptFunctionName", dllFunctionName, NULL }
****************************************************************************/
function_entry rdstrak_functions[] = {
  { "rdstrak_rplist", php3_rdstrak_rplist, NULL },
  { "rdstrak_rptotal", php3_rdstrak_rptotal, NULL },
  { "rdstrak_rpsettings", php3_rdstrak_rpsettings, NULL },
  { "rdstrak_rpsettings_", php3_rdstrak_rpsettings_, NULL },
  { "rdstrak_rphandle", php3_rdstrak_rphandle, NULL },
  { "rdstrak_rpget", php3_rdstrak_rpget, NULL },
  { "rdstrak_rpset", php3_rdstrak_rpset, NULL },
  { "rdstrak_strlen", php3_rdstrak_strlen, NULL },
  { NULL, NULL, NULL }
};

/***************************************************************************/
php3_module_entry rdstrak_module_entry = {
  "php3_rdstrak", rdstrak_functions, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL
};


#if COMPILE_DL
DLEXPORT php3_module_entry *get_module( void ) { return &rdstrak_module_entry; }
#endif


/***************************************************************************/
typedef struct _RPHANDLE {
  struct _RPHANDLE *pnext;
  int handle;
} RPHANDLE;
static RPHANDLE *pList;


/***************************************************************************/
static int AddHandle( int handle ) {
  RPHANDLE *p;

  p = ( RPHANDLE* )malloc( sizeof( RPHANDLE ) );
  memset( p, 0, sizeof( RPHANDLE ) );
  p->handle = handle;
  p->pnext = pList;
  pList = p;
  return( 1 );
}
/***************************************************************************/
static int DoNothing( int handle ) {
}


/****************************************************************************
* PHP3 proto: int rdstrak_rplist( void );
*****************************************************************************/
DLEXPORT void php3_rdstrak_rplist( INTERNAL_FUNCTION_PARAMETERS ) {
  RPHANDLE *p;

  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) {
    return_value->type = IS_STRING;
    return_value->value.str.val = empty_string;
    return_value->value.str.len = 0;
    return;
  }
  pList = NULL;
  rp_scanall( AddHandle );
  while( pList ) {
    p = pList->pnext;
    add_next_index_long( return_value, pList->handle );
    free( pList );
    pList = p;
  }
}


/*****************************************************************************/
DLEXPORT void php3_rdstrak_rptotal( INTERNAL_FUNCTION_PARAMETERS ) {
  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  return_value->value.lval = rp_scanall( DoNothing );
  return_value->type = IS_LONG;
}


/****************************************************************************/
DLEXPORT void php3_rdstrak_rpsettings( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pvalHandle = NULL;
  char szName[ NAME_LEN + 1 ], szDesc[ DATA_LEN + 1 ];
  char szDevice[ NAME_LEN + 1 ], szDeviceData[ DATA_LEN + 1 ];

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pvalHandle ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) {
    return_value->type = IS_STRING;
    return_value->value.str.val = empty_string;
    return_value->value.str.len = 0;
    return;
  }
  convert_to_long( pvalHandle );
  szName[ 0 ] = 0; szDesc[ 0 ] = 0; szDevice[ 0 ] = 0; szDeviceData[ 0 ] = 0;
  rp_settings( pvalHandle->value.lval, szName, szDesc, szDevice, szDeviceData );
  add_next_index_string( return_value, szName, 1 );
  add_next_index_string( return_value, szDesc, 1 );
  add_next_index_string( return_value, szDevice, 1 );
  add_next_index_string( return_value, szDeviceData, 1 );
}

/****************************************************************************/
DLEXPORT void php3_rdstrak_rpsettings_( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pvalHandle = NULL;
  char szName[ NAME_LEN + 1 ], szDesc[ DATA_LEN + 1 ];
  char szDevice[ NAME_LEN + 1 ], szDeviceData[ DATA_LEN + 1 ];

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pvalHandle ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) {
    return_value->type = IS_STRING;
    return_value->value.str.val = empty_string;
    return_value->value.str.len = 0;
    return;
  }
  convert_to_long( pvalHandle );
  szName[ 0 ] = 0; szDesc[ 0 ] = 0; szDevice[ 0 ] = 0; szDeviceData[ 0 ] = 0;
  rp_settings( pvalHandle->value.lval, szName, szDesc, szDevice, szDeviceData );
  add_assoc_index_string( return_value, "name", szName, 1 );
  add_assoc_index_string( return_value, "desc", szDesc, 1 );
  add_assoc_index_string( return_value, "device", szDevice, 1 );
  add_assoc_index_string( return_value, "devicedata", szDeviceData, 1 );
}


/****************************************************************************
* PHP3 proto: int rdstrak_rphandle( char *szName );
*****************************************************************************/
DLEXPORT void php3_rdstrak_rphandle( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pvalName = NULL;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pvalName ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pvalName );
  return_value->value.lval = rp_handle( pvalName->value.str.val );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: unsigned rdstrak_rpget( int handle );
*****************************************************************************/
DLEXPORT void php3_rdstrak_rpget( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pvalHandle = NULL;
  int iHandle, iValue;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pvalHandle ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long( pvalHandle );
  return_value->value.lval = ( int )rp_get( pvalHandle->value.lval );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstrak_rpset( int handle, unsigned uNewValue );
*****************************************************************************/
DLEXPORT void php3_rdstrak_rpset( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pvalHandle = NULL, *pvalNewValue = NULL;

  if( ARG_COUNT( ht ) != 2 || 
      getParameters( ht, 2, &pvalHandle, &pvalNewValue ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long( pvalHandle );
  convert_to_long( pvalNewValue );
  return_value->value.lval = rp_set( pvalHandle->value.lval,
                                     pvalNewValue->value.lval );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstrak_strlen( char* psz ); - for testing
*****************************************************************************/
DLEXPORT void php3_rdstrak_strlen( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *psz;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &psz ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( psz );
  return_value->value.lval = strlen( SAFE( psz->value.str.val ) );
  return_value->type = IS_LONG;
}

