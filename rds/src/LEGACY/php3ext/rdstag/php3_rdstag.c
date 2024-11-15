/**********************************************************************
* php3_rdstag.c - interface functions
*
* Copyright (C) 2001 Numina Systems Corp.
* Author: Alex Korzhuk
* Last Modified: 3/27/2001
***********************************************************************/
#include "php3_rdstag.h"
#include "/home/rds/include/rds_tag.h"

#define SAFE( s )  ( ( s ) ? ( s ) : "" )


/***************************************************************************
* This array should be set up as {"PHPScriptFunctionName",dllFunctionName,1}
****************************************************************************/
function_entry rdstag_functions[] = {
  { "rdstag_first", php3_rdstag_first, NULL },
  { "rdstag_next", php3_rdstag_next, NULL },
  { "rdstag_value", php3_rdstag_value, NULL },
  { "rdstag_insert", php3_rdstag_insert, NULL },
  { "rdstag_delete", php3_rdstag_delete, NULL },
  { "rdstag_space", php3_rdstag_space, NULL },
  { "rdstag_test", php3_rdstag_test, NULL },
  { "rdstag_branch", php3_rdstag_branch, NULL },
  { "rdstag_tree", php3_rdstag_tree, NULL },
  { "rdstag_eraseall", php3_rdstag_eraseall, NULL },
  { "rdstag_sync", php3_rdstag_sync, NULL },
  { "rdstag_strlen", php3_rdstag_strlen, NULL },
  { NULL, NULL, NULL }
};

/****************************************************************************/
php3_module_entry rdstag_module_entry = {
  "php3_rdstag", rdstag_functions, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL
};


#if COMPILE_DL
DLEXPORT php3_module_entry *get_module( void ) { return &rdstag_module_entry; }
#endif



/****************************************************************************
* PHP3 proto: char* rdstag_first( char* pszKey );
*****************************************************************************/
DLEXPORT void php3_rdstag_first( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pKey = NULL;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pKey ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pKey );
  return_value->value.str.val = estrdup( SAFE(tag_first(pKey->value.str.val)));
  return_value->value.str.len = strlen( return_value->value.str.val );
  return_value->type = IS_STRING;
}


/****************************************************************************
* PHP3 proto: char* rdstag_next( char* pszKey );
*****************************************************************************/
DLEXPORT void php3_rdstag_next( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pKey = NULL;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pKey ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pKey );
  return_value->value.str.val = estrdup( SAFE(tag_next( pKey->value.str.val)));
  return_value->value.str.len = strlen( return_value->value.str.val );
  return_value->type = IS_STRING;
}  


/****************************************************************************
* PHP3 proto: char* rdstag_value( char* pszKey );
*****************************************************************************/
DLEXPORT void php3_rdstag_value( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pKey = NULL;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pKey ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pKey );
  return_value->value.str.val = estrdup( SAFE(tag_value(pKey->value.str.val)));
  return_value->value.str.len = strlen( return_value->value.str.val );
  return_value->type = IS_STRING;
}


/****************************************************************************
* PHP3 proto: int rdstag_insert( char* pszKey, char* pszValue );
*****************************************************************************/
DLEXPORT void php3_rdstag_insert( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pKey, *pValue;

  if( ARG_COUNT( ht ) != 2 || getParameters(ht,2,&pKey,&pValue ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pKey );
  convert_to_string( pValue );
  return_value->value.lval = tag_insert( pKey->value.str.val,
                                         pValue->value.str.val );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_delete( char* pszKey );
*****************************************************************************/
DLEXPORT void php3_rdstag_delete( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pKey;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pKey ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( pKey );
  return_value->value.lval = tag_delete( pKey->value.str.val );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_space( void );
*****************************************************************************/
DLEXPORT void php3_rdstag_space( INTERNAL_FUNCTION_PARAMETERS ) {
  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  return_value->value.lval = tag_space();
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_test( int iID );
*****************************************************************************/
DLEXPORT void php3_rdstag_test( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *pID;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &pID ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long( pID );
  tag_test( ( char )pID->value.lval );
  return_value->value.lval = 0;
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_branch( int x );
*****************************************************************************/
DLEXPORT void php3_rdstag_branch( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *px;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &px ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_long( px );
  return_value->value.lval = tag_branch( px->value.lval );
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_tree( void );
*****************************************************************************/
DLEXPORT void php3_rdstag_tree( INTERNAL_FUNCTION_PARAMETERS ) {
  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  return_value->value.lval = tag_tree();
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_eraseall( void );
*****************************************************************************/
DLEXPORT void php3_rdstag_eraseall( INTERNAL_FUNCTION_PARAMETERS ) {
  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  return_value->value.lval = tag_eraseall();
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_sync( void );
*****************************************************************************/
DLEXPORT void php3_rdstag_sync( INTERNAL_FUNCTION_PARAMETERS ) {
  if( ARG_COUNT( ht ) != 0 ) {
    WRONG_PARAM_COUNT;
    return;
  }
  return_value->value.lval = tag_sync();
  return_value->type = IS_LONG;
}


/****************************************************************************
* PHP3 proto: int rdstag_strlen( char* psz );
*****************************************************************************/
DLEXPORT void php3_rdstag_strlen( INTERNAL_FUNCTION_PARAMETERS ) {
  pval *psz;
  int i;

  if( ARG_COUNT( ht ) != 1 || getParameters( ht, 1, &psz ) == FAILURE ) {
    WRONG_PARAM_COUNT;
    return;
  }
  convert_to_string( psz );
  i = strlen( psz->value.str.val );
  return_value->value.lval = i; //strlen( psz->value.str.val );
  return_value->type = IS_LONG;
}

