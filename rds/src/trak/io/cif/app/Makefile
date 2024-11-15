#
# Makefile for ciftrak 
#

INCDIR = $(HOME)/include
LIBDIR =  $(HOME)/lib

INC_OPT= -I$(INCDIR)
LIB_OPT=  -L$(LIBDIR) 

CC=gcc
CFLAGS=-g -fpic -fPIC -Dlinux $(INC_OPT) 

LIBS= -lrds_trak -lrds_trn
LDFLAGS=$(LIB_OPT) $(LIBS)

%.c:%.w
	ctangle $*

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*

%: %.c 
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -o $*
	install -c -m 711 $* $(HOME)/bin

TARGETS=ciftrak

all: $(TARGETS)

clean:
	rm -f $(TARGETS)
	rm -f *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log
