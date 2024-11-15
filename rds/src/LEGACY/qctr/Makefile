#
#  RDS3 QCTR makefile 
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
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_qctr -lrds_sql -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = qctrd qctr_bump qctr_zero

all: $(TESTS)
lib: librds_qctr.so
doc: rds_qctr.dvi qctrd.dvi
pdf: rds_qctr.pdf qctrd.pdf
install:

$(TESTS): librds_qctr.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
