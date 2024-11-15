#
#  RDS3 QHIST makefile 
#
HOME=/home/rds

INCLUDE_OPT=-I$(HOME)/include 
LIB_OPT=-L$(HOME)/lib
LIBS=-lrds_q

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
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_qhist -lrds_sql -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = qhistd qhist_post

all: $(TESTS)
lib: librds_qhist.so
doc: rds_qhist.dvi qhistd.dvi
pdf: rds_qhist.pdf qhistd.pdf
install:

$(TESTS): librds_qhist.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
