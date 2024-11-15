#***************************************************************************
# Makefile
#*************************************************************************** 
CC = cc -O2 -m486 -fno-strength-reduce -Dbool=char -DHAS_BOOL -I/usr/include/apache -I. -I/usr/src/redhat/BUILD/php-3.0.15 -fpic -I/home/rds/include
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_trak
SODIR = /usr/lib/apache

all: php3_rdstrak.so
OBJS = ./php3_rdstrak.o

php3_rdstrak.so: $(OBJS)
	$(LD) -o $@ $(OBJS)

./%.o: ./%.c
	$(CC) $(CFLAGS) -DCOMPILE_DL=1 -c -o $@ $<
install:
	install -m 755 php3_rdstrak.so $(SODIR)
clean:
	rm -f ./*.so ./*.o

# The end
