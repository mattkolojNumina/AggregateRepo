#***************************************************************************
# Makefile
#*************************************************************************** 
CC = cc -O2 -m486 -fno-strength-reduce -Dbool=char -DHAS_BOOL -I/usr/include/apache -I. -I/usr/src/redhat/BUILD/php-3.0.15 -fpic
LD = cc -rdynamic -shared -L/home/rds/lib -lrds_tag

all: php3_rdstag.so

RDSTAG_OBJS = ./php3_rdstag.o

php3_rdstag.so: $(RDSTAG_OBJS)
	$(LD) -o $@ $(RDSTAG_OBJS)

./%.o: ./%.c
	$(CC) $(CFLAGS) -DCOMPILE_DL=1 -c -o $@ $<

clean:
	-rm -f ./*.so ./*.o perl.out

# The end
