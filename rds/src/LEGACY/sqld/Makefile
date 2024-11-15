#
#  RDS3 SQLD makefile 
#

INCLUDE_OPT=-I$(HOME)/include 
LIB_OPT=-L$(HOME)/lib
LIBS=-lrds_msg -lrds_trn

CC=gcc
CFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT)
LDFLAGS=$(LIB_OPT) $(LIBS)

%.c: %.w
	ctangle $*
	cp *.h $(HOME)/include
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
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_sqld -lrds_sql -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = sqld sqld_post

all: $(TESTS)
lib: librds_sqld.so
doc: rds_sqld.dvi sqld.dvi
pdf: rds_sqld.pdf sqld.pdf
install:

$(TESTS): librds_sqld.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
