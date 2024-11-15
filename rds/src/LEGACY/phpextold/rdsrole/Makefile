#***************************************************************************
# Makefile
#*************************************************************************** 
HDIR=/usr/include/php
HOPT= -I/usr/include/apache -I. -I$(HDIR) -I$(HDIR)/main -I$(HDIR)/TSRM -I$(HDIR)/Zend -I/home/rds/include

CC = cc -fpic -DCOMPILE_DL=1 $(HOPT)
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_role
SODIR = /usr/lib/php4

all: php_rdsrole.so

OBJS = ./php_rdsrole.o

php_rdsrole.so: $(OBJS)
	$(LD) -o $@ $(OBJS)

./%.o: ./%.c
	$(CC) -c -o $@ $<
install:
	install -m 755 php_rdsrole.so $(SODIR)

clean:
	-rm -f ./*.so ./*.o

# The end
