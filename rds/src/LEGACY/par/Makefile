#
#  RDS3 par makefile 
#

INCLUDE_OPT=-I$(HOME)/include 
LIB_OPT=-L$(HOME)/lib
LIBS=

CC=gcc
CFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT)
LDFLAGS=$(LIB_OPT) $(LIBS)

%.c: %.w
	ctangle $*
	@if [ -f *.h ]; then cp *.h $(HOME)/include; fi
%.tex: %.w
	cweave $*
%.dvi: %.tex
	tex $*
%.pdf: %.dvi
	dvipdf $*
	install -c -m 664 $*.pdf $(HOME)/pdf
lib%.so: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -shared -o lib$*.so
	install -c -m 755 lib$*.so $(HOME)/lib
%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_par -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = par_getbyte par_setbyte par_setbit par_getcbyte par_setcbit \
        par_getsbyte par_setstrobe

all: $(TESTS)
lib: librds_par.so
doc: rds_par.dvi
pdf: rds_par.pdf
install:

$(TESTS): librds_par.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
