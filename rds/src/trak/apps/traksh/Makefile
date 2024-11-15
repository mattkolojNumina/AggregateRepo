#
#  traksh makefile 
#

HOME=/home/rds

INCLUDEDIR=$(HOME)/include
LIBDIR=$(HOME)/lib

INCLUDE_OPT= -I$(INCLUDEDIR)
LIB_OPT=  -L$(LIBDIR)

CC=gcc
LIBCFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)
CFLAGS=-g -O -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)
OBJS=rds_trak.o

%.c:%.w
	ctangle $*


%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -o $*
	install -c -m 711 $* $(HOME)/bin

USER= traksh  

all:    $(USER)

traksh.c: traksh.w

traksh:  traksh.c
	$(CC) $(CFLAGS) -o traksh $(LDFLAGS) \
        traksh.c -lrds_trak -lreadline -lcurses 
	install -c -m 711 traksh $(HOME)/bin

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi
	rm -f $(USER)
