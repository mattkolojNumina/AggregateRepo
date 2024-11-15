/**********************************************************************
* php_rdstag.c - RDSTAG extension library fro PHP4
*
* Copyright (C) 2002 Numina Systems Corp.
* Author: Alex Korzhuk
* Last Modified: 8/12/2002
***********************************************************************/
#include "php.h"
#include "/home/rds/include/rds_tag.h"

/* Functions accessable to PHP */
PHP_FUNCTION( rdstag_first );
PHP_FUNCTION( rdstag_next );
PHP_FUNCTION( rdstag_value );
PHP_FUNCTION( rdstag_insert );
PHP_FUNCTION( rdstag_delete );
PHP_FUNCTION( rdstag_space );
PHP_FUNCTION( rdstag_test );
PHP_FUNCTION( rdstag_branch );
PHP_FUNCTION( rdstag_tree );
PHP_FUNCTION( rdstag_eraseall );
PHP_FUNCTION( rdstag_sync );
PHP_FUNCTION( rdstag_strlen );

#define SAFE( s )  ( ( s ) ? ( s ) : "" )


/***************************************************************************
* {"PHPScriptFunctionName",dllFunctionName,1}
****************************************************************************/
function_entry rdstag_functions[] = {
  PHP_FE( rdstag_first, NULL )
  PHP_FE( rdstag_next, NULL )
  PHP_FE( rdstag_value, NULL )
  PHP_FE( rdstag_insert, NULL )
  PHP_FE( rdstag_delete, NULL )
  PHP_FE( rdstag_space, NULL )
  PHP_FE( rdstag_test, NULL )
  PHP_FE( rdstag_branch, NULL )
  PHP_FE( rdstag_tree, NULL )
  PHP_FE( rdstag_eraseall, NULL )
  PHP_FE( rdstag_sync, NULL )
  PHP_FE( rdstag_strlen, NULL )
  { NULL, NULL, NULL }
};

/****************************************************************************/
zend_module_entry php_rdstag_module_entry = {
  STANDARD_MODULE_HEADER,
  "php_rdstag", rdstag_functions, 
  NULL, NULL, NULL, NULL, NULL, PHP_VERSION, STANDARD_MODULE_PROPERTIES
};


#if COMPILE_DL
ZEND_GET_MODULE( php_rdstag )
#endif



/****************************************************************************
* PHP proto: char* rdstag_first( char* pszKey );
*****************************************************************************/
PHP_FUNCTION( rdstag_first ) {
  zval **ppKey = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppKey ) == FAILURE ){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppKey );
  RETURN_STRING( SAFE( tag_first( Z_STRVAL_PP( ppKey ) ) ), 1 );
}


/****************************************************************************
* PHP proto: char* rdstag_next( char* pszKey );
*****************************************************************************/
PHP_FUNCTION( rdstag_next ) {
  zval **ppKey = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppKey ) == FAILURE ){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppKey );
  RETURN_STRING( SAFE( tag_next( Z_STRVAL_PP( ppKey ) ) ), 1 );
}  


/****************************************************************************
* PHP proto: char* rdstag_value( char* pszKey );
*****************************************************************************/
PHP_FUNCTION( rdstag_value ) {
  zval **ppKey = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppKey ) == FAILURE ){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppKey );
  RETURN_STRING( SAFE( tag_value( Z_STRVAL_PP( ppKey ) ) ), 1 );
}


/****************************************************************************
* PHP proto: int rdstag_insert( char* pszKey, char* pszValue );
*****************************************************************************/
PHP_FUNCTION( rdstag_insert ) {
  zval **ppKey, **ppValue;

  if( ZEND_NUM_ARGS() != 2 ||
      zend_get_parameters_ex( 2, &ppKey, &ppValue ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppKey );
  convert_to_string_ex( ppValue );
  RETURN_LONG( tag_insert( Z_STRVAL_PP( ppKey ), Z_STRVAL_PP( ppValue ) ) );
}


/****************************************************************************
* PHP proto: int rdstag_delete( char* pszKey );
*****************************************************************************/
PHP_FUNCTION( rdstag_delete ) {
  zval **ppKey;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppKey ) == FAILURE ){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppKey );
  RETURN_LONG( tag_delete( Z_STRVAL_PP( ppKey ) ) );
}


/****************************************************************************
* PHP proto: int rdstag_space( void );
*****************************************************************************/
PHP_FUNCTION( rdstag_space ) {
  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  RETURN_LONG( tag_space() );
}


/****************************************************************************
* PHP proto: int rdstag_test( int iID );
*****************************************************************************/
PHP_FUNCTION( rdstag_test ) {
  zval **ppID;

  if( ZEND_NUM_ARGS() != 0 || zend_get_parameters_ex( 1, &ppID ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long_ex( ppID );
  tag_test( ( char )Z_LVAL_PP( ppID ) );
  RETURN_LONG( 0 );
}


/****************************************************************************
* PHP proto: int rdstag_branch( int x );
*****************************************************************************/
PHP_FUNCTION( rdstag_branch ) {
  zval **ppx;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppx ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long_ex( ppx );
  RETURN_LONG( tag_branch( Z_LVAL_PP( ppx ) ) );
}


/****************************************************************************
* PHP proto: int rdstag_tree( void );
*****************************************************************************/
PHP_FUNCTION( rdstag_tree ) {
  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  RETURN_LONG( tag_tree() );
}


/****************************************************************************
* PHP proto: int rdstag_eraseall( void );
*****************************************************************************/
PHP_FUNCTION( rdstag_eraseall ) {
  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  RETURN_LONG( tag_eraseall() );
}


/****************************************************************************
* PHP proto: int rdstag_sync( void );
*****************************************************************************/
PHP_FUNCTION( rdstag_sync ) {
  if( ZEND_NUM_ARGS() != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  RETURN_LONG( tag_sync( 1 ) );
}


/****************************************************************************
* PHP proto: int rdstag_strlen( char* psz ); - used for testing
*****************************************************************************/
PHP_FUNCTION( rdstag_strlen ) {
  zval **ppsz;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppsz ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppsz );
  RETURN_LONG( strlen( Z_STRVAL_PP( ppsz ) ) );
}

