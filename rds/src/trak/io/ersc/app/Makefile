#
# ersctrak Makefile
#

###

FILES = ersctrak 
DOC   = ersctrak.pdf 

LIBS = -lrds_net -lrds_trak -lrds_sql -lrds_trn  -lrds_evt

APP_H = $(INSTALL_INC)/app.h

### control variables

INSTALL_BIN = $(HOME)/bin
INSTALL_INC = $(HOME)/include
INSTALL_LIB = $(HOME)/lib
INSTALL_DOC = $(HOME)/doc
INCLUDE = -I$(HOME)/include -I$(HOME)/app/include -I/usr/realtime/include
LIBDIR = -L$(HOME)/lib -L$(HOME)/app/lib

CC = gcc
CC_SHARED = $(CC) -fPIC -c

CFLAGS = -g -pipe $(INCLUDE)
LDFLAGS = $(LIBDIR) $(LIBS) -lz

### implicit rules

%.c:%.w
	ctangle $*
	@if [ -f $*.h ] ; then install -m 644 $*.h $(INSTALL_INC) ; fi

%.tex:%.w
	cweave $*

%.dvi:%.tex
	tex $*
	@rm -f $*.idx $*.log $*.scn $*.toc

%.ps:%.dvi
	dvips -o $*.ps $*

%.pdf:%.ps
	ps2pdf $*.ps
	install -m 644 $*.pdf $(INSTALL_DOC)

%.o:%.c $(APP_H)
	$(CC) $(CFLAGS) -c $*.c

lib%.so:%_lib.o
	$(CC) $(CFLAGS)  -shared -o lib$*.so $*_lib.o $(LDFLAGS)
	install -m 644 lib$*.so $(INSTALL_LIB)

%:%.c $(APP_H)
	$(CC) $(CFLAGS)  -o $* $*.c $(LDFLAGS)
	install -m 755 $* $(INSTALL_BIN)

%:%.o 
	$(CC) $(CFLAGS)  -o $* $*.o $(LDFLAGS)
	install -m 755 $* $(INSTALL_BIN)

### targets

all: $(FILES)

doc: $(DOC)

clean:	
	@rm -f *.c *.h *.o $(FILES)
	@rm -f *.aux *.log *.idx *.scn *.toc *.tex *.dvi *.ps *.pdf

