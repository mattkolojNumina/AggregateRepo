/**********************************************************************
* php3_rdsrole.h - header file for php3_rdsrole.so library
*
* Copyright (C) 2002 Numina Systems Corp.
* Written by Alex Korzhuk
* Last Modified: 6/20/2001
***********************************************************************/
#ifndef _INCLUDED_PHP3_RDSROLE_H
#define _INCLUDED_PHP3_RDSROLE_H

#include "php.h"
#include "internal_functions.h"

#if WIN32|WINNT
#include <windows.h>
#define DLEXPORT __declspec(dllexport)
#else
#define DLEXPORT
#endif

/* Functions accessable to PHP */
DLEXPORT void php3_rdsrole_find( INTERNAL_FUNCTION_PARAMETERS );

#endif   /* _INCLUDED_PHP3_RDSROLE_H */
