#
# trak perl makefile 
#

SUBDIR = Trk condition dead debounce dp merge meter motor panel rack rp \
			timer trakbase traksort zone

all:
	@set -e ; for i in $(SUBDIR) ; do echo building $$i... ; cd $$i ; perl Makefile.PL ; make ; cd .. ; done

install:
	@su -c ' \
	   set -e ; for i in $(SUBDIR) ; do echo installing $$i... ; cd $$i ; make install ; cd .. ; done'

clean:
	@set -e ; for i in $(SUBDIR) ; do echo cleaning $$i... ; cd $$i ; perl Makefile.PL ; make clean ; rm Makefile.old ; cd .. ; done
