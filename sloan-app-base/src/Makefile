#
#  app Makefile
#

DIR=app_lib

all:
	@set -e; for i in $(DIR); do $(MAKE) -C $$i; done
doc:
	@set -e; for i in $(DIR); do $(MAKE) doc -C $$i; done
clean:
	@set -e; for i in $(DIR); do $(MAKE) clean -C $$i; done

