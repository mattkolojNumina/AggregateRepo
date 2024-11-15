#
# Makefile for utility apps
#

INCLUDEDIR = $(HOME)/include
LIBDIR =  $(HOME)/lib

INCLUDE_OPT= -I$(INCLUDEDIR) 
LIB_OPT=  -L$(LIBDIR)

CC=gcc
CFLAGS=-g -fpic -fPIC -Dlinux $(INCLUDE_OPT) 

LIBS=-lrds_trak
LDFLAGS=$(LIB_OPT) $(LIBS)

%.c:%.w
	ctangle $*

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*

%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -o $*
	install -c -m 711 $* $(HOME)/bin/test

TARGETS=rp_dump

all: $(TARGETS)

clean:
	rm -f $(TARGETS)
	rm -f *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log
