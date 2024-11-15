#include "EXTERN.h"
#include "perl.h"
#include "XSUB.h"

#include </home/rds/include/rds_trn.h>

static int
not_here(char *s)
{
    croak("%s not implemented on this architecture", s);
    return -1;
}

static double
constant(char *name, int arg)
{
    errno = 0;
    switch (*name) {
    }
    errno = EINVAL;
    return 0;

not_there:
    errno = ENOENT;
    return 0;
}


MODULE = RDS::Trn		PACKAGE = RDS::Trn		PREFIX = trn_


double
constant(name,arg)
	char *		name
	int		arg


void
trn_register(name)
	char *	name

int
Alert(fmt, ...)
	char *	fmt

int
Trace(fmt, ...)
	char *	fmt

int
Inform(fmt, ...)
	char *	fmt
