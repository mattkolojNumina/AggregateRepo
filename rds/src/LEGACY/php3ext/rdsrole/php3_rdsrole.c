/**********************************************************************
* php3_rdsrole.c - interface functions
*
* Copyright (C) 2002 Numina Systems Corp.
* Written by Alex Korzhuk
* Last Modified: 6/20/2001
***********************************************************************/
#include "php3_rdsrole.h"
#include "/home/rds/include/rds_role.h"

#define SAFE( s )  ( ( s ) ? ( s ) : "" )


/***************************************************************************
* This array should be set up as {"PHPScriptFunctionName",dllFunctionName,1}
****************************************************************************/
function_entry rdsrole_functions[] = {
  { "rdsrole_find", php3_rdsrole_find, NULL },
  { NULL, NULL, NULL }
};

/****************************************************************************/
php3_module_entry rdsrole_module_entry = {
  "php3_rdsrole", rdsrole_functions, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL
};


#if COMPILE_DL
DLEXPORT php3_module_entry *get_module( void ) { return &rdsrole_module_entry; }
#endif



/****************************************************************************
* PHP3 proto: char* rdsrole_find( char* pszRole );
*****************************************************************************/
DLEXPORT void php3_rdsrole_find( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pRole = NULL;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pRole ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pRole );
  return_value->value.str.val = estrdup( SAFE(role_find(pRole->value.str.val)));
  return_value->value.str.len = strlen( return_value->value.str.val );
  return_value->type = IS_STRING;
}
