#***************************************************************************
# Makefile
#*************************************************************************** 
HDIR=/usr/include/php
HOPT= -I/usr/realtime/include -I/usr/include/apache -I. -I$(HDIR) -I$(HDIR)/main -I$(HDIR)/TSRM -I$(HDIR)/Zend -I/home/rds/include

CC = cc -fpic -DCOMPILE_DL=1 $(HOPT)
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_trak
SODIR = /usr/lib/php4

all: php_rdstrak.so

OBJS = ./php_rdstrak.o

php_rdstrak.so: $(OBJS)
	$(LD) -o $@ $(OBJS)

./%.o: ./%.c
	$(CC) -c -o $@ $<
install:
	install -m 755 php_rdstrak.so $(SODIR)

clean:
	-rm -f ./*.so ./*.o

# The end
