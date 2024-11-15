/**********************************************************************
* php_rdsrole.c - RDSROLE extension library fro PHP4
*
* Copyright (C) 2002 Numina Systems Corp.
* Written by Alex Korzhuk
* Last Modified: 8/12/2002
***********************************************************************/
#include "php.h"
#include "/home/rds/include/rds_role.h"

/* Functions accessable to PHP */
PHP_FUNCTION( rdsrole_find );

#define SAFE( s )  ( ( s ) ? ( s ) : "" )


/***************************************************************************
* {"PHPScriptFunctionName",dllFunctionName,1}
****************************************************************************/
function_entry rdsrole_functions[] = {
  PHP_FE( rdsrole_find, NULL )
  { NULL, NULL, NULL }
};


/****************************************************************************/
zend_module_entry php_rdsrole_module_entry = {
  STANDARD_MODULE_HEADER,
  "php_rdsrole", rdsrole_functions, 
  NULL, NULL, NULL, NULL, NULL, PHP_VERSION, STANDARD_MODULE_PROPERTIES
};


#ifdef COMPILE_DL
ZEND_GET_MODULE( php_rdsrole )
#endif


/****************************************************************************
* PHP proto: char* rdsrole_find( char* pszRole );
*****************************************************************************/
PHP_FUNCTION( rdsrole_find ) {
  zval **ppRole = NULL;

  if( ZEND_NUM_ARGS() != 1 || zend_get_parameters_ex( 1, &ppRole ) == FAILURE){
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string_ex( ppRole );
  RETURN_STRING( SAFE( role_find( Z_STRVAL_PP( ppRole ) ) ), 1 );
}
