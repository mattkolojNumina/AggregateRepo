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


MODULE = RDS::Tag		PACKAGE = RDS::Tag		PREFIX = tag_


double
constant(name,arg)
	char *		name
	int		arg


int
tag_init()

int
tag_delete(key)
	char *	key

int
tag_insert(key, value)
	char *	key
	char *	value

char *
tag_value(key)
	char *	key

char *
tag_first(key)
	char *	key

char *
tag_next(key)
	char *	key

int
tag_space()

void
tag_test(id)
	char	id

int
tag_branch(x)
	int	x

int
tag_tree()
