#
# Makefile for ecat_app 
#

INCDIR = $(HOME)/include
LIBDIR =  $(HOME)/lib
ETHERLABINCDIR = /opt/etherlab/include
ETHERLABLIBDIR = /opt/etherlab/lib

INC_OPT= -I$(INCDIR) -I$(ETHERLABINCDIR) 
LIB_OPT=  -L$(LIBDIR) -L$(ETHERLABLIBDIR)  

CC=gcc
CFLAGS=-g -fpic -fPIC -Dlinux $(INC_OPT) 

LIBS= -lrds_trak -lrds_trn -lethercat
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

TARGETS=ecattrak

all: $(TARGETS)

clean:
	rm -f $(TARGETS)
	rm -f *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log
