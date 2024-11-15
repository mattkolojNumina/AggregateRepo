#!/bin/bash
# chkconfig: - 01 99
# description: Start, stop, and restart of modules required by the RDS PAR \
#              library to access bits of the parallel port

start() {
  su -l root -c "
    /sbin/rmmod lp >/dev/null 2>/dev/null
    /sbin/modprobe parport
    /sbin/modprobe parport_pc
    #/sbin/modprobe parport_pc io=0x378 irq=none
    /sbin/modprobe ppdev
  "
}

stop() {
  su -l root -c "
    /sbin/rmmod ppdev >/dev/null 2>/dev/null
    /sbin/rmmod parport_pc  >/dev/null 2>/dev/null
    /sbin/rmmod parport >/dev/null 2>/dev/null
  "
}

restart() {
  stop
  start
}

case "$1" in
  start)   start;;
  stop)    stop;;
  restart) restart;;
  *)       echo "Usage: $0 { start | stop | restart }"; exit 1
esac
