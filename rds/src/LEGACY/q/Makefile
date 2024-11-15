#
#  RDS3 Q makefile 
#
HOME=/home/rds

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
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_q -lrds_trn -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = q_fastsend q_sync q_flush q_send q_rawtype q_type q_recv q_next \
        q_settail q_empty q_readername q_typename q_info q_config

all: $(TESTS)
lib: librds_q.so
doc: rds_q.dvi
pdf: rds_q.pdf
install:

$(TESTS): librds_q.so

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi *.idx *.log *.pdf
	rm -f $(TESTS)
