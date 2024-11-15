# makefile for cif.ko

obj-m += cif.o

%.c: %.w
	ctangle $*

all: cif.c
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) modules
	install cif.ko /home/rds/modules

clean:
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) clean
	rm -f cif.c cif.o cif.ko cif.mod.c cif.mod.o modules.order Module.symvers
