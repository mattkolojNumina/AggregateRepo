/**********************************************************************
* php3_rdstag.h -
*
* Copyright (C) 2001 Numina Systems Corp.
* Author: Bill McCormick
* Last Modified: 4/26/2001
***********************************************************************/
#ifndef _INCLUDED_PHP3_RDSTRAK_H
#define _INCLUDED_PHP3_RDSTRAK_H

#include "php.h"
#include "internal_functions.h"

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
DLEXPORT void php3_rdstrak_rphandle( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpget( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_rpset( INTERNAL_FUNCTION_PARAMETERS );
DLEXPORT void php3_rdstrak_strlen( INTERNAL_FUNCTION_PARAMETERS );


#endif   /* _INCLUDED_PHP3_RDSTRAK_H */
