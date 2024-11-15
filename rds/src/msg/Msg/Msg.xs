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


MODULE = RDS::Msg		PACKAGE = RDS::Msg		PREFIX = msg_


double
constant(name,arg)
	char *		name
	int		arg


int
msg_send(mtype, msg)
	int	mtype
	char *	msg

int
msg_type(handle)
	int	handle

char *
msg_recv(handle)
	int	handle

int
msg_next(handle)
	int	handle

int
msg_get(handle)
	int	handle

int
msg_set(handle, value)
	int	handle
	long int	value

int
msg_index(name)
	char *	name
