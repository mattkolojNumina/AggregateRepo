Additions and fixes to trak64 rds33 Ubuntu22.04 build 
=====================================================
These notes should be applied after rds0033_xxxxx.tgz installed on the
recent trak64 build on Ubuntu 22.04.

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
   [If you are having trouble acessing Ubuntu servers and DNS is properly configured, run 'apt-get update']

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

8. Recompile and install Ersc.pm
   cd ~/src/trak/io/ersc/perl/Ersc
   make clean
   perl Makefile.PL
   make
   make all
   sudo make install
   make clean

9. Fix apcupsd configuration issue for APC UPS with usb cable.
   As a superuser (sudo su -):
     systemctl status apcupsd
     apcaccess | grep STATUS  (should be "STATUS   : COMMLOST")
     vi /etc/apcupsd/apcupsd.conf, comment out "#DEVICE /dev/ttyS0", add DEVICE:
       UPSTYPE usb
       #DEVICE /dev/ttyS0
       DEVICE
     systemctl restart apcupsd
     systemctl status apcupsd
     apcaccess | grep STATUS  (should be "STATUS   : ONLINE")

//==========================================================================
// NOTE: Item 10 should be applied after node and dashboard are already
//       installed at ~/app/www/ui/
//==========================================================================
10. Fix node vulnerabilities. As rds:
   a) make sure the node in not running and not getting restarted by launch:
      UPDATE launch SET mode='manual' WHERE process='node' AND mode='daemon';
      killall node
   b) Re-install debug package:
      cd ~/app/www/ui/; npm uninstall debug; npm install debug;
   c) cd ~/app/www/ui/; vi package-lock.json
      to replace the following line in "ui" module dependencies:
          "debug": "4.18.2",
      to
          "debug": "^4.3.4",
      Note: 1) It is needed since debug 4.18.2 module does not exist,
               likely it was a typo copied&pasted from "express" package.
               To see available versions, execute "npm view debug versions"
            2) If ~/app/www/ui/package-lock.json file does not exist, create it:
               npm i --package-lock-only
   d) see node vulnerabilities:
      cd ~/app/www/ui/; npm install
   e) fix node vulnerabilities without breaking the code:
      cd ~/app/www/ui/; npm audit fix
   f) fix node vulnerabilities that break the "ui" code:
      cd ~/app/www/ui/; npm audit fix --force
   g) make change in the code that was broken:
      g1) cd ~/app/www/ui/views/; cp -p index.ejs index.ejs.safe231109
      g2) vi index.ejs   to replace each include line from:
             <%- include ../public/ui/XXX %>
          to
             <%- include( "../public/ui/XXX" ) %>
      g3) killall node
      g4) refresh a dashboard webpage to confirm it is operational
   h) verify package-lock.json file in step (c) again to make sure
      "debug": "^4.3.4"
   i) verify that there are no more node vulnerabilities (should show 0):
      cd ~/app/www/ui/; npm audit
   j) have launch to start the node:
      UPDATE launch SET mode='daemon', operation='trigger' WHERE process='node';
 
11. Fix vulnerabilities in jquery. As rds:
    a) cd ~/app/www/ui/public/js/;
       mv jquery-3.1.1.min.js jquery-3.5.0.min.js.orig;
       cp -p ~/app/rds33trak64addon/jquery-3.5.0.min.js ./
    b) cd ~/app/www/ui/public/ui/; vi script.ejs
       replace a line
          <script src="/js/jquery-3.1.1.min.js"></script>
       with the line
          <script src="/js/jquery-3.5.0.min.js"></script>
    c) Restart node: killall node
    d) Open the dashboard Events=>History tab to make sure that version change
       did not break dataTable object.

12. Update packages to fix vulnerabilities in Ubuntu 22.04, As root (sudo su -):
   apt-get install xxd           (to have version >= 2:8.2.3995-1ubuntu2.13)
   apt-get install vim           (to have version >= 2:8.2.3995-1ubuntu2.13)
   apt-get install vim-common    (to have version >= 2:8.2.3995-1ubuntu2.13)
   apt-get install vim-runtime   (to have version >= 2:8.2.3995-1ubuntu2.13)
   apt-get install vim-tiny      (to have version >= 2:8.2.3995-1ubuntu2.13)
   apt-get install libcurl3-gnutls  (to have version >= 7.81.0-1ubuntu1.14)
   apt-get install libtiff-dev   (to have version >= 4.3.0-6ubuntu0.6)
   apt-get install libtiff5      (to have version >= 4.3.0-6ubuntu0.6)
   apt-get install libtiffxx5    (to have version >= 4.3.0-6ubuntu0.6)
   apt-get install libx11-6      (to have version >= 2:1.7.5-1ubuntu0.3)
   apt-get install libx11-data   (to have version >= 2:1.7.5-1ubuntu0.3)
   apt-get install libx11-dev    (to have version >= 2:1.7.5-1ubuntu0.3)
   apt-get install libx11-xcb1   (to have version >= 2:1.7.5-1ubuntu0.3)
   apt-get install libxpm4       (to have version >= 1:3.5.12-1ubuntu0.22.04.2)
   apt-get install libwbclient0  (to have version >= 2:4.15.13+dfsg-0ubuntu1.5)
   apt-get install libssl-dev    (to have version >= 3.0.2-0ubuntu1.12)
   apt-get install libssl3       (to have version >= 3.0.2-0ubuntu1.12)
   apt-get install libc-bin      (to have version >= 2.35-0ubuntu3.4)
   apt-get install libc-devtools (to have version >= 2.35-0ubuntu3.4)
   apt-get install locales       (to have version >= 2.35-0ubuntu3.4)

13. As a superuser (sudo su -) edit /etc/fstab file to avoid seeing an error:
   "[...] /dev/disk/by_id/scsi-1ATA_UDinfo_M2S-...-part1: Can't open blockdev"
   a) df   (to see /dev/sda1 and /dev/sda3 partitions names)
   b) cd /etc/; cp -p fstab fstab.orig
   c) vi fstab
   c1) replace a line
      /dev/disk/by-uuid/235e97e7-041c-4523-adeb-e5126116b53b none swap sw 0 0
        with
      /dev/sda2 none swap sw 0 0
   c2) replace a line
     /dev/disk/by-uuid/eb5502e7-108b-452b-8d5b-14f6ce3bbfa0 / ext4 defaults 0 1
        with
     /dev/sda3 / ext4 defaults 0 1
   c3) replace a line
     /dev/disk/by-uuid/6181-A21A /boot/efi vfat defaults 0 1
        with
     /dev/sda1 /boot/efi vfat defaults 0 1

   d) /sbin/reboot
   e) df   (to see /dev/sda1 and /dev/sda3 partitions names)
   f) top  (to see that swap partition was also created)

14. Update rds_hist
  a) cd ~/src/hist
  b) mv rds_hist.w rds_hist.orig
  c) cp ~/app/rds33trak64addon/hist/rds_hist.w rds_hist.w
  d) mv histd.w rds_histd.orig
  e) cp ~/app/rds33trak64addon/hist/histd.w histd.w
  f) make clean; make; make all

15. Get the latest set of trak tools on the machine (~/src/trak/util)
  a) cd ~/src/trak/
  b) cp -r ~/app/rds33trak64addon/util ./
  c) make clean; make

16. Make secure on your way out
  a) cd ~/bin/system
  b) make secure
  c) Password: 60Shore!