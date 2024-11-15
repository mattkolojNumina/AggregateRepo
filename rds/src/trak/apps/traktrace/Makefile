#
#  traktrace makefile 
#

HOME=/home/rds

INCLUDEDIR=$(HOME)/include
LIBDIR=$(HOME)/lib

INCLUDE_OPT= -I$(INCLUDEDIR) 
LIB_OPT=  -L$(LIBDIR) -lrds_trn -ldl

CC=gcc
LIBCFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)
CFLAGS=-g -O -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)
OBJS=rds_trak.o

%.c:%.w
	ctangle $*

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*

%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_msg -o $*
	install -c -m 711 $* $(HOME)/bin




USER= traktrace 
all:    $(USER)


traktrace.c: traktrace.w
traktrace:  traktrace.c
	$(CC) $(CFLAGS) -o traktrace traktrace.c \
           -lrds_sql -lrds_trn -lrds_trak -ldl $(LDFLAGS) 
	install -c -m 711 traktrace $(HOME)/bin

name_test:  name_test.c
	$(CC) $(CFLAGS) -o name_test $(LDFLAGS) \
           -lrds_sql -lrds_trn -lrds_trak name_test.c
	install -c -m 711 traktrace $(HOME)/bin

install:

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi
	rm -f $(USER)
