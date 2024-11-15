#***************************************************************************
# Makefile
#*************************************************************************** 
CC = cc -O2 -m486 -fno-strength-reduce -Dbool=char -DHAS_BOOL -I/usr/include/apache -I. -I/usr/src/redhat/BUILD/php-3.0.15 -fpic -I/home/rds/include
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_trak

all: php3_rdstrak.so

RDSTRAK_OBJS = ./php3_rdstrak.o

php3_rdstrak.so: $(RDSTRAK_OBJS)
	$(LD) -o $@ $(RDSTRAK_OBJS)

./%.o: ./%.c
	$(CC) $(CFLAGS) -DCOMPILE_DL=1 -c -o $@ $<

clean:
	-rm -f ./*.so ./*.o perl.out

# The end
