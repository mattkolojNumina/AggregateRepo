#
# RDS3 logix Makefile
#

###

BINFILES := logix_read logix_write logix_test
LIBFILES := librds_logix.so
JARFILES :=

SRC  := $(patsubst lib%.so,%.w,$(LIBFILES))
LIBS := rds_trn
DEP  :=

$(BINFILES): LDLIBS += -lrds_logix

rds_%.c logix_%.c rds_%.h: rds_logix.w
	$(CTANGLE) $<

### control variables

HEADER := $(patsubst lib%.so,%.h,$(LIBFILES))
DOC    := $(patsubst %.w,%.pdf,$(SRC))

INSTALL_BIN := $(HOME)/bin
INSTALL_LIB := $(HOME)/lib
INSTALL_INC := $(HOME)/include
INSTALL_DOC := $(HOME)/doc
LIBDIR      := $(HOME)/lib
INCDIR      := $(HOME)/include
STOREPASS   := numina

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
	jar cf $$@ $$(foreach D,$$($1_SRCDIR),$$D/*.class) $$($1_EXTRA)
	jarsigner -storepass $(STOREPASS) $$@ rds

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
lib%.so:%.o
	$(LINK.o) $^ $(LOADLIBES) $(LDLIBS) -o $@

### targets
.PHONY: all bin lib jar doc clean install install-lib install-bin install-jar

.DEFAULT_GOAL := all
all: install

bin: $(BINFILES)

OBJ := $(addsuffix .o,$(BINFILES))
.INTERMEDIATE: $(OBJ)
$(OBJ): $(DEP)

lib: $(LIBFILES)

jar: $(JARFILES)

ifdef SRC
checkin:: | RCS
	ci -l -M -T $(SRC)
RCS:
	mkdir -p $@
endif

install: install-lib install-bin install-jar

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

doc: $(DOC)
ifdef DOC
	install -m 644 $(DOC) $(INSTALL_DOC)
endif

clean::
	@$(RM) *.c *.h *.o $(BINFILES) $(LIBFILES)
	@$(RM) *.aux *.log *.idx *.scn *.toc *.tex *.dvi *.ps *.pdf

$(foreach J,$(basename $(JARFILES)),$(eval $(call make-jar,$J)))
