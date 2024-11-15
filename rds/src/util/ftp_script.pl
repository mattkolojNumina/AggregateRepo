#!/usr/bin/perl

#
#
# file:        ftp_script.pl
# description: This perl script makes a pre-authorized file transfer via
#              hard-coded username/password info, thus allowing use of the
#              command-line interface
# author:      R. Ernst
# history:     10/01/02 initial version
#              12/20/05 modified for generic use --AHM
#

use Net::FTP ;

if ($#ARGV != 2)
   {
   die "usage: $0 <filename> <remote hostname> <remote directory>\n" ;
   }

$ftp = Net::FTP->new($ARGV[1]) ;
if (defined($ftp) == FALSE)
   {
   die "FTP object creation failure\n" ;
   }
if ($ftp->login("rds","numina") == FALSE)
   {
   die "FTP login failure\n" ;
   }
if ($ftp->cwd($ARGV[2]) == FALSE)
   {
   die "FTP cwd failure\n" ;
   }

$ftp->binary() ;
$res = $ftp->put($ARGV[0]) ;
if ($res ne $ARGV[0])
   {
   die "FTP put failure\n" ;
   }

$ftp->quit ;

