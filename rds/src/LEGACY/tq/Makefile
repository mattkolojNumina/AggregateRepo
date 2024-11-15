#
#  RDS3 TQ makefile 
#

INCLUDE_OPT=-I$(HOME)/include 
LIB_OPT=-L$(HOME)/lib
LIBS=-lrds_tag

CC=gcc
CFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT)
LDFLAGS=$(LIB_OPT) $(LIBS)

%.c: %.w
	ctangle $*
	@if [ -f *.h ]; then cp *.h $(HOME)/include; fi;
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
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_tq -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = tq_init tq_send tq_recv tq_empty

all: $(TESTS)
lib: librds_tq.so
doc: rds_tq.dvi
pdf: rds_tq.pdf
install:

$(TESTS): librds_tq.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
