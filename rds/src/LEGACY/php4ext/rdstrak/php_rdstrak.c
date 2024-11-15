/**********************************************************************
* php_rdstrak.c - RDSTRAK extension library for PHP4
*
* Copyright (C) 2002 Numina Systems Corp.
* Author: Alex Korzhuk
* Last Modified: 8/9/2002
***********************************************************************/
#include "php.h"
#include "/home/rds/include/rds_trak.h"


/* Functions accessable to PHP */
PHP_FUNCTION( rdstrak_rplist );
PHP_FUNCTION( rdstrak_rptotal );
PHP_FUNCTION( rdstrak_rpsettings );
PHP_FUNCTION( rdstrak_rpsettings_ );
PHP_FUNCTION( rdstrak_rphandle );
PHP_FUNCTION( rdstrak_rpget );
PHP_FUNCTION( rdstrak_rpset );
PHP_FUNCTION( rdstrak_strlen );

#define SAFE( s )  ( ( s ) ? ( s ) : "" )

/***************************************************************************
* {"PHPScriptFunctionName", dllFunctionName, NULL }
****************************************************************************/
function_entry rdstrak_functions[] = {
  PHP_FE( rdstrak_rplist, NULL )
  PHP_FE( rdstrak_rptotal, NULL )
  PHP_FE( rdstrak_rpsettings, NULL )
  PHP_FE( rdstrak_rpsettings_, NULL )
  PHP_FE( rdstrak_rphandle, NULL )
  PHP_FE( rdstrak_rpget, NULL )
  PHP_FE( rdstrak_rpset, NULL )
  PHP_FE( rdstrak_strlen, NULL )
  { NULL, NULL, NULL }
};

/***************************************************************************/
zend_module_entry php_rdstrak_module_entry = {
  STANDARD_MODULE_HEADER,
  "php_rdstrak", rdstrak_functions,
  NULL, NULL, NULL, NULL, NULL, PHP_VERSION, STANDARD_MODULE_PROPERTIES
};


#if COMPILE_DL
ZEND_GET_MODULE( php_rdstrak )
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
  return( 0 );
}


/****************************************************************************
* PHP proto: int rdstrak_rplist( void );
*****************************************************************************/
PHP_FUNCTION( rdstrak_rplist ) {
  RPHANDLE *p;

  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) RETURN_STRING( empty_string, 1 );
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
PHP_FUNCTION( rdstrak_rptotal ) {
  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  RETURN_LONG( rp_scanall( DoNothing ) );
}


/****************************************************************************/
PHP_FUNCTION( rdstrak_rpsettings ) {
  zval **ppHandle = NULL;
  char szName[ TRAK_NAME_LEN + 1 ], szDesc[ TRAK_DATA_LEN + 1 ];
  char szDevice[ TRAK_NAME_LEN + 1 ], szDeviceData[ TRAK_DATA_LEN + 1 ];

  if( ZEND_NUM_ARGS() != 1 ||
      zend_get_parameters_ex( 1, &ppHandle ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) RETURN_STRING( empty_string, 1 );
  convert_to_long_ex( ppHandle );
  szName[ 0 ] = 0; szDesc[ 0 ] = 0; szDevice[ 0 ] = 0; szDeviceData[ 0 ] = 0;
  rp_settings( Z_LVAL_PP( ppHandle ), szName, szDesc, szDevice, szDeviceData );
  add_next_index_string( return_value, szName, 1 );
  add_next_index_string( return_value, szDesc, 1 );
  add_next_index_string( return_value, szDevice, 1 );
  add_next_index_string( return_value, szDeviceData, 1 );
}


/****************************************************************************/
PHP_FUNCTION( rdstrak_rpsettings_ ) {
  zval **ppHandle = NULL;
  char szName[ TRAK_NAME_LEN + 1 ], szDesc[ TRAK_DATA_LEN + 1 ];
  char szDevice[ TRAK_NAME_LEN + 1 ], szDeviceData[ TRAK_DATA_LEN + 1 ];

  if( ZEND_NUM_ARGS() != 1 ||
      zend_get_parameters_ex( 1, &ppHandle ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  if( array_init( return_value ) != SUCCESS ) RETURN_STRING( empty_string, 1 );
  convert_to_long_ex( ppHandle );
  szName[ 0 ] = 0; szDesc[ 0 ] = 0; szDevice[ 0 ] = 0; szDeviceData[ 0 ] = 0;
  rp_settings( Z_LVAL_PP( ppHandle ), szName, szDesc, szDevice, szDeviceData );
  add_assoc_index_string( return_value, "name", szName, 1 );
  add_assoc_index_string( return_value, "desc", szDesc, 1 );
  add_assoc_index_string( return_value, "device", szDevice, 1 );
  add_assoc_index_string( return_value, "devicedata", szDeviceData, 1 );
}


/****************************************************************************
* PHP proto: int rdstrak_rphandle( char *szName );
*****************************************************************************/
PHP_FUNCTION( rdstrak_rphandle ) {
  zval **ppName = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppName ) == FAILURE){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppName );
  RETURN_LONG( rp_handle( Z_STRVAL_PP( ppName ) ) );
}


/****************************************************************************
* PHP proto: unsigned rdstrak_rpget( int handle );
*****************************************************************************/
PHP_FUNCTION( rdstrak_rpget ) {
  zval **ppHandle = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex(1,&ppHandle) == FAILURE ){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long_ex( ppHandle );
  RETURN_LONG( ( int )rp_get( Z_LVAL_PP( ppHandle ) ) );
}


/****************************************************************************
* PHP proto: int rdstrak_rpset( int handle, unsigned uNewValue );
*****************************************************************************/
PHP_FUNCTION( rdstrak_rpset ) {
  zval **ppHandle = NULL, **ppNewValue = NULL;

  if( ZEND_NUM_ARGS() != 2 || 
      zend_get_parameters_ex( 2, &ppHandle, &ppNewValue ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long_ex( ppHandle );
  convert_to_long_ex( ppNewValue );
  RETURN_LONG( rp_set( Z_LVAL_PP( ppHandle ), Z_LVAL_PP( ppNewValue ) ) );
}


/****************************************************************************
* PHP proto: int rdstrak_strlen( char* psz ); - for testing
*****************************************************************************/
PHP_FUNCTION( rdstrak_strlen ) {
  zval **ppsz;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppsz ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppsz );
  RETURN_LONG( strlen( Z_STRVAL_PP( ppsz ) ) );
}

