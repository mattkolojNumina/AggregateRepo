#
#  RDS3 makefile
#

DIR = javautil polling msg vt100 trn sql tag \
      busy ctr evt execute hist launch logix net runtime tty util \
      notifier

all: lib _all

ch_all:
	@set -e; for i in $(DIR); do make -C $$i; done
lib:
	@set -e; for i in $(DIR); do make install-lib -C $$i; done
	sudo /sbin/ldconfig
doc:
	@set -e; for i in $(DIR); do make doc -C $$i; done
clean:
	@set -e; for i in $(DIR); do make clean -C $$i; done

