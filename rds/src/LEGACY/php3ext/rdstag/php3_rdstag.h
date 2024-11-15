/**********************************************************************
* php3_rdstag.h -
*
* Copyright (C) 2001 Numina Systems Corp.
* Author: Alex Korzhuk
* Last Modified: 3/27/2001
***********************************************************************/
#ifndef _INCLUDED_PHP3_RDSTAG_H
#define _INCLUDED_PHP3_RDSTAG_H

#include "php.h"
#include "internal_functions.h"

#if WIN32|WINNT
#include <windows.h>
#define DLEXPORT __declspec(dllexport)
#else
#define DLEXPORT
#endif


/* Functions accessable to PHP */
DLEXPORT void php3_rdstag_first( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_next( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_value( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_insert( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_delete( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_space( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_test( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_branch( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_tree( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_eraseall( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_sync( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstag_strlen( INTERNAL_FUNCTION_PARAMETERS );

#endif   /* _INCLUDED_PHP3_RDSTAG_H */
