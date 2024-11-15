/*
 * This file was generated automatically by xsubpp version 1.9507 from the 
 * contents of Msg.xs. Do not edit this file, edit Msg.xs instead.
 *
 *	ANY CHANGES MADE HERE WILL BE LOST! 
 *
 */

#line 1 "Msg.xs"
#include "EXTERN.h"
#include "perl.h"
#include "XSUB.h"

#include </home/rds/include/rds_msg.h>

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


#line 39 "Msg.c"
XS(XS_RDS__Msg_constant)
{
    dXSARGS;
    if (items != 2)
	croak("Usage: RDS::Msg::constant(name,arg)");
    {
	char *	name = (char *)SvPV(ST(0),PL_na);
	int	arg = (int)SvIV(ST(1));
	double	RETVAL;

	RETVAL = constant(name, arg);
	ST(0) = sv_newmortal();
	sv_setnv(ST(0), (double)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_send)
{
    dXSARGS;
    if (items != 2)
	croak("Usage: RDS::Msg::send(mtype, msg)");
    {
	int	mtype = (int)SvIV(ST(0));
	char *	msg = (char *)SvPV(ST(1),PL_na);
	int	RETVAL;

	RETVAL = msg_send(mtype, msg);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_type)
{
    dXSARGS;
    if (items != 1)
	croak("Usage: RDS::Msg::type(handle)");
    {
	int	handle = (int)SvIV(ST(0));
	int	RETVAL;

	RETVAL = msg_type(handle);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_recv)
{
    dXSARGS;
    if (items != 1)
	croak("Usage: RDS::Msg::recv(handle)");
    {
	int	handle = (int)SvIV(ST(0));
	char *	RETVAL;

	RETVAL = msg_recv(handle);
	ST(0) = sv_newmortal();
	sv_setpv((SV*)ST(0), RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_next)
{
    dXSARGS;
    if (items != 1)
	croak("Usage: RDS::Msg::next(handle)");
    {
	int	handle = (int)SvIV(ST(0));
	int	RETVAL;

	RETVAL = msg_next(handle);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_get)
{
    dXSARGS;
    if (items != 1)
	croak("Usage: RDS::Msg::get(handle)");
    {
	int	handle = (int)SvIV(ST(0));
	int	RETVAL;

	RETVAL = msg_get(handle);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_set)
{
    dXSARGS;
    if (items != 2)
	croak("Usage: RDS::Msg::set(handle, value)");
    {
	int	handle = (int)SvIV(ST(0));
	long int	value;
	int	RETVAL;

	if (sv_derived_from(ST(1), "long int")) {
	    IV tmp = SvIV((SV*)SvRV(ST(1)));
	    value = (long int) tmp;
	}
	else
	    croak("value is not of type long int");

	RETVAL = msg_set(handle, value);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Msg_index)
{
    dXSARGS;
    if (items != 1)
	croak("Usage: RDS::Msg::index(name)");
    {
	char *	name = (char *)SvPV(ST(0),PL_na);
	int	RETVAL;

	RETVAL = msg_index(name);
	ST(0) = sv_newmortal();
	sv_setiv(ST(0), (IV)RETVAL);
    }
    XSRETURN(1);
}

#ifdef __cplusplus
extern "C"
#endif
XS(boot_RDS__Msg)
{
    dXSARGS;
    char* file = __FILE__;

    XS_VERSION_BOOTCHECK ;

        newXS("RDS::Msg::constant", XS_RDS__Msg_constant, file);
        newXS("RDS::Msg::send", XS_RDS__Msg_send, file);
        newXS("RDS::Msg::type", XS_RDS__Msg_type, file);
        newXS("RDS::Msg::recv", XS_RDS__Msg_recv, file);
        newXS("RDS::Msg::next", XS_RDS__Msg_next, file);
        newXS("RDS::Msg::get", XS_RDS__Msg_get, file);
        newXS("RDS::Msg::set", XS_RDS__Msg_set, file);
        newXS("RDS::Msg::index", XS_RDS__Msg_index, file);
    XSRETURN_YES;
}
