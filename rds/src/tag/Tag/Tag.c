/*
 * This file was generated automatically by xsubpp version 1.9508 from the
 * contents of Tag.xs. Do not edit this file, edit Tag.xs instead.
 *
 *	ANY CHANGES MADE HERE WILL BE LOST!
 *
 */

#line 1 "Tag.xs"
#include "EXTERN.h"
#include "perl.h"
#include "XSUB.h"

#include </home/rds/include/rds_tag.h>

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


#line 39 "Tag.c"
XS(XS_RDS__Tag_constant); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_constant)
{
    dXSARGS;
    if (items != 2)
	Perl_croak(aTHX_ "Usage: RDS::Tag::constant(name, arg)");
    {
	char *	name = (char *)SvPV_nolen(ST(0));
	int	arg = (int)SvIV(ST(1));
	double	RETVAL;
	dXSTARG;

	RETVAL = constant(name, arg);
	XSprePUSH; PUSHn((double)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_init); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_init)
{
    dXSARGS;
    if (items != 0)
	Perl_croak(aTHX_ "Usage: RDS::Tag::init()");
    {
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_init();
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_delete); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_delete)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::delete(key)");
    {
	char *	key = (char *)SvPV_nolen(ST(0));
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_delete(key);
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_insert); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_insert)
{
    dXSARGS;
    if (items != 2)
	Perl_croak(aTHX_ "Usage: RDS::Tag::insert(key, value)");
    {
	char *	key = (char *)SvPV_nolen(ST(0));
	char *	value = (char *)SvPV_nolen(ST(1));
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_insert(key, value);
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_value); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_value)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::value(key)");
    {
	char *	key = (char *)SvPV_nolen(ST(0));
	char *	RETVAL;
	dXSTARG;

	RETVAL = tag_value(key);
	sv_setpv(TARG, RETVAL); XSprePUSH; PUSHTARG;
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_first); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_first)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::first(key)");
    {
	char *	key = (char *)SvPV_nolen(ST(0));
	char *	RETVAL;
	dXSTARG;

	RETVAL = tag_first(key);
	sv_setpv(TARG, RETVAL); XSprePUSH; PUSHTARG;
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_next); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_next)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::next(key)");
    {
	char *	key = (char *)SvPV_nolen(ST(0));
	char *	RETVAL;
	dXSTARG;

	RETVAL = tag_next(key);
	sv_setpv(TARG, RETVAL); XSprePUSH; PUSHTARG;
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_space); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_space)
{
    dXSARGS;
    if (items != 0)
	Perl_croak(aTHX_ "Usage: RDS::Tag::space()");
    {
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_space();
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_test); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_test)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::test(id)");
    {
	char	id = (char)*SvPV_nolen(ST(0));

	tag_test(id);
    }
    XSRETURN_EMPTY;
}

XS(XS_RDS__Tag_branch); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_branch)
{
    dXSARGS;
    if (items != 1)
	Perl_croak(aTHX_ "Usage: RDS::Tag::branch(x)");
    {
	int	x = (int)SvIV(ST(0));
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_branch(x);
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

XS(XS_RDS__Tag_tree); /* prototype to pass -Wmissing-prototypes */
XS(XS_RDS__Tag_tree)
{
    dXSARGS;
    if (items != 0)
	Perl_croak(aTHX_ "Usage: RDS::Tag::tree()");
    {
	int	RETVAL;
	dXSTARG;

	RETVAL = tag_tree();
	XSprePUSH; PUSHi((IV)RETVAL);
    }
    XSRETURN(1);
}

#ifdef __cplusplus
extern "C"
#endif
XS(boot_RDS__Tag); /* prototype to pass -Wmissing-prototypes */
XS(boot_RDS__Tag)
{
    dXSARGS;
    char* file = __FILE__;

    XS_VERSION_BOOTCHECK ;

        newXS("RDS::Tag::constant", XS_RDS__Tag_constant, file);
        newXS("RDS::Tag::init", XS_RDS__Tag_init, file);
        newXS("RDS::Tag::delete", XS_RDS__Tag_delete, file);
        newXS("RDS::Tag::insert", XS_RDS__Tag_insert, file);
        newXS("RDS::Tag::value", XS_RDS__Tag_value, file);
        newXS("RDS::Tag::first", XS_RDS__Tag_first, file);
        newXS("RDS::Tag::next", XS_RDS__Tag_next, file);
        newXS("RDS::Tag::space", XS_RDS__Tag_space, file);
        newXS("RDS::Tag::test", XS_RDS__Tag_test, file);
        newXS("RDS::Tag::branch", XS_RDS__Tag_branch, file);
        newXS("RDS::Tag::tree", XS_RDS__Tag_tree, file);
    XSRETURN_YES;
}

