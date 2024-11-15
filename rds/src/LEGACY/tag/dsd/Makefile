#
#  RDS3 TAG makefile 
#

INCLUDEDIR=$(HOME)/include
LIBDIR=$(HOME)/lib

INCLUDE_OPT= -I$(INCLUDEDIR) 
LIB_OPT=  -L$(LIBDIR)

CC=gcc
CFLAGS=-g -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)

OBJS=rds_dsd.o

%.c:%.w
	ctangle $*
	cp *.h $(INCLUDEDIR)

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*

%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) -lrds_dsd -o $*
	install -c -m 711 $* $(HOME)/bin

TESTS = dsd_put dsd_get dsd_drop dsd_add dsd_gdrop

all: $(TESTS)

lib: librds_dsd.so

install:

rds_dsd.o: rds_dsd.c
	$(CC) $(CFLAGS) rds_dsd.c -c 

librds_dsd.so: $(OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) -shared $(OBJS) -lrds_tag -o librds_dsd.so
	install -c -m 755 librds_dsd.so $(LIBDIR)

$(TESTS): librds_dsd.so

doc:
	cweave rds_dsd
	tex rds_dsd
clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi
	rm -f $(TESTS)
