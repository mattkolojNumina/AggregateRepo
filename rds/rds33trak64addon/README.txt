Additions and fixes to trak64 rds33 Ubuntu22 build 
==================================================
These notes should be applied after rds0033_xxxxx.tgz installed on the
recent trak64 build on Ubuntu 22.

1. As rds, install and recompile new version of Trk.xs
   New version fixed a bug in Trk::include() which incorrectly uses the "access"
   c-function results.
     cd ~/src/trak/perl/Trk
     mv Trk.xs Trk.xs.orig
     cp -p ~/app/rds33trak64addon/Trk.xs ./
     make clean
     perl Makefile.PL
     make
     make all
     sudo make install
     make clean

2. As rds, install and recompile new version of linxtrak.w
   New version works with IBE03A and IBE03B cards.
   (Old version did NOT work with IBE03A cards).
     cd ~/src/trak/io/linx/app
     mv linxtrak.w linxtrak.w.orig
     cp -p ~/app/rds33trak64addon/linxtrak.w ./
     make clean; make; make all; make clean

3. As a superuser (sudo su -) install gdb package
   apt install gdb
   (press Y when requested)

4. If the project uses ethercat, make sure that ethercat is enabled and
   running after rds33_Trak64_Ub22 built. As root:
   systemctl status ethercat
   systemctl enable ethercat
   systemctl restart ethercat
   systemctl status ethercat

5. Fix typo in zoneinfo link as a superuser (sudo su -):
   ls -l /etc/localtime
   ln -sf /usr/share/zoneinfo/America/Chicago /etc/localtime

6. Fix snmp as a superuser (sudo su -):
  cd /home/rds/app/config/snmp
  cat README
  install -m 644 -g root -o root *-MIB.txt /usr/share/snmp/mibs/
  mv /etc/snmp/snmp.conf /etc/snmp/snmp.conf.orig
  install -m 644 -g root -o root snmp.conf /etc/snmp/

  Test it as rds user:
    snmpwalk -v 1 -c rds 172.17.35.35      //Kyocera P4060
    snmpwalk -v 1 -c public 172.17.1.221   //Zebra desktop labeler
               
7. Recompile traksh. It fixes the issue when you start traksh:
     traksh: error while loading shared libraries: libreadline.so.7:
     cannot open shared object file: No such file or directory
   cd ~/src/trak/apps/traksh
   make
   make all
   make clean



