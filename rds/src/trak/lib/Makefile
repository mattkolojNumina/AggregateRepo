#
#  RDS Trak Library Makefile 
#

HOME=/home/rds

INC_DIR=$(HOME)/include

LIBDIR=$(HOME)/lib
OBJDIR=$(HOME)/obj

INCLUDE_OPT= -I$(INC_DIR)
LIB_OPT=  -L$(LIBDIR)

CC=gcc
LIBCFLAGS=-g -Wall -fpic -fPIC -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) $(LIB_OPT)
CFLAGS=-g -Wall -O2 -DHOME=\"$(HOME)\" -Dlinux $(INCLUDE_OPT) 
LDFLAGS= -lrds_trn

OBJS=rds_trak.o

%.c:%.w
	ctangle $*
	cp *.h $(INC_DIR)

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*

%: %.c
	$(CC) $(CFLAGS) $*.c $(LDFLAGS) $(LIB_OPT) -lrds_trak -o $*
	install -c -m 711 $* $(HOME)/bin

WEB_PARTS=trak_ex.i trak_mb.i trak_rp.i trak_dp.i trak_ev.i trak_bx.i \
          trak_ipc.i trak_q.i trak_const.i 

all:   librds_trak.so

tests: q_dump bx_dump dp_dump rp_dump evt_dump ex_dump mb_view

user: librds_trak.so

rds_trak.c: rds_trak.w $(WEB_PARTS)

rds_trak.o: rds_trak.c
	$(CC) $(LIBCFLAGS) rds_trak.c -c 

librds_trak.so: rds_trak.o
	$(CC) $(LIBCFLAGS) -shared rds_trak.o $(LDFLAGS) -o librds_trak.so
	install -c -m 644 librds_trak.so $(LIBDIR)

clean:
	rm -f *.c *.o lib* *.h *.tex *.toc *.scn *.dvi
	rm -f librds_trak.so q_dump bx_dump dp_dump rp_dump evt_dump ex_dump mb_view
