#***************************************************************************
# Makefile
#*************************************************************************** 
CC = cc -O2 -m486 -fno-strength-reduce -Dbool=char -DHAS_BOOL -I/usr/include/apache -I. -I/usr/src/redhat/BUILD/php-3.0.15 -fpic -I/home/rds/include
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_role
SODIR = /usr/lib/apache

all: php3_rdsrole.so

OBJS = ./php3_rdsrole.o

php3_rdsrole.so: $(OBJS)
	$(LD) -o $@ $(OBJS)

./%.o: ./%.c
	$(CC) $(CFLAGS) -DCOMPILE_DL=1 -c -o $@ $<
install:
	install -m 755 php3_rdsrole.so $(SODIR)

clean:
	-rm -f ./*.so ./*.o

# The end
