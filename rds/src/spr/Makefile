#
# spr Makefile
#

###

BINFILES := spr
LIBFILES :=
JARFILES :=
SCRIPTFILES := 

SRC  := spr.w
LIBS := rds_evt rds_net rds_sql rds_trn rds_util
DEP  :=

### control variables

HEADER := $(patsubst lib%.so,%.h,$(LIBFILES))
DOC    := $(patsubst %.w,%.pdf,$(SRC)))

INSTALL_BIN := $(HOME)/app/bin
INSTALL_LIB := $(HOME)/app/lib
INSTALL_INC := $(HOME)/app/include
INSTALL_DOC := $(HOME)/app/doc
LIBDIR      := $(HOME)/lib $(HOME)/app/lib
INCDIR      := $(HOME)/include $(HOME)/app/include /usr/realtime/include

KEYSTORE    := $(HOME)/numina.jks
STOREPASS   := 10331Werch 
ALIAS       := numinagroup.com

CC    = gcc
JAVAC = javac

CPPFLAGS += $(addprefix -I,$(INCDIR))
CFLAGS   += -g -pipe
LDFLAGS  += $(addprefix -L,$(LIBDIR))
LDLIBS   += $(addprefix -l,$(LIBS))

ifdef DEBUG
OLD_SHELL := $(SHELL)
SHELL = $(warning [$@ ($^) ($?)])$(OLD_SHELL)
endif

### jar building

# $(call make-jar,jar-prefix)
define make-jar

$1_JAR := $$(addsuffix .jar,$1)
ifndef $1_SRCDIR
$1_SRCDIR := $1
endif
$1_SRC := $$(foreach D,$$($1_SRCDIR),$$(wildcard $$D/*.java))
$1_OBJ := $$(patsubst %.java,%.class,$$($1_SRC))
$1_RCSDIR := $$(addsuffix /RCS,$$($1_SRCDIR))

$$($1_JAR): $$($1_OBJ)
	jar cfm $$@ $(HOME) Manifest.txt $$(foreach D,$$($1_SRCDIR),$$D/*.class) $$($1_EXTRA)
	jarsigner -keystore $(KEYSTORE) -storepass $(STOREPASS) $$@ $(ALIAS)

checkin:: | $$($1_RCSDIR)
	ci -l -M -T $$($1_SRC)
$$($1_RCSDIR):
	mkdir -p $$@

clean::
	@$(RM) $$(foreach D,$$($1_SRCDIR),$$D/*.class) $$($1_JAR)

endef

### implicit rules

%.class:%.java
	$(JAVAC) $<

%.dvi:%.tex
	$(TEX) $<
	@$(RM) -f $*.idx $*.log $*.scn $*.toc

%.ps:%.dvi
	dvips $< -o $@

%.pdf:%.ps
	ps2pdf $<

lib%.so: CFLAGS += -fPIC
lib%.so: LDFLAGS += -shared -fPIC
lib%.so:%_lib.o
	$(LINK.o) $^ $(LOADLIBES) $(LDLIBS) -o $@

### targets
.PHONY: all bin lib jar doc clean \
      install install-lib install-bin install-jar install-script

.DEFAULT_GOAL := all
all: install

bin: $(BINFILES)

OBJ := $(addsuffix .o,$(BINFILES))
.INTERMEDIATE: $(OBJ)
$(OBJ): $(DEP)

lib: $(LIBFILES)

jar: $(JARFILES)

script: $(SCRIPTFILES)

ifdef SRC
checkin:: | RCS
	ci -l -M -T $(SRC)
RCS:
	mkdir -p $@
endif

install: install-lib install-bin install-jar install-script

install-lib: checkin lib
ifdef LIBFILES
	install -m 644 $(LIBFILES) $(INSTALL_LIB)
endif
ifdef HEADER
	install -m 644 $(HEADER) $(INSTALL_INC)
endif

install-bin: checkin install-lib bin
ifdef BINFILES
	install -m 755 $(BINFILES) $(INSTALL_BIN)
endif

install-jar: checkin jar
ifdef JARFILES
	install -m 644 $(JARFILES) $(INSTALL_LIB)
endif

install-script: checkin script
ifdef SCRIPTFILES
	install -m 444 $(SCRIPTFILES) $(INSTALL_BIN)
endif

doc: $(DOC)
ifdef DOC
	install -m 644 $(DOC) $(INSTALL_DOC)
endif

clean::
	@$(RM) *.c *.h *.o $(BINFILES) $(LIBFILES)
	@$(RM) *.aux *.log *.idx *.scn *.toc *.tex *.dvi *.ps *.pdf

$(foreach J,$(basename $(JARFILES)),$(eval $(call make-jar,$J)))
