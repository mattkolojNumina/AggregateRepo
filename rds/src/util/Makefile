#
#  RDS3 utilities makefile 
#

UTIL = jarsign ftp_script.pl modstatus pstatus rc rds_daily sqldump tty_connect

BINDIR=$(HOME)/bin

all:
	install -m 755 $(UTIL) $(BINDIR)
	cp -pfR config $(BINDIR)
	cp -pfR system $(BINDIR)

lib:
install-lib:
install:
doc:

clean:
