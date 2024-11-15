#
#   ethercat.pm
#
#   (C) Copyright 2016 Numina Group, Inc.  All Rights Reserved.
#

package ethercat;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);

@EXPORT = qw();
$VERSION = '0.01';

require Trk ;

my $haveDriver = 0 ;
my $lastAddress = -1 ;

sub
checkDriver
   {
   if($haveDriver==0)
      {
      Trk::dp_adddriver("io:ethercat","EtherCat driver") ;
      $haveDriver = 1 ;
      }
   }

sub
add
   {
   my ($module,$data) = @_ ;

   my $address = $data->{'address'} ;
   my $prefix  = $data->{'prefix'} ;
   my $virtual = 0 ;
   if($data->{'virtual'} ne '') { $virtual = 1 } ;

   checkDriver() ;
    
   if($address eq '')
      {
      $address = $lastAddress + 1 ;
      }
   $lastAddress = $address ;

   if($prefix eq '')
      {
      $prefix = 'm' . $address ;
      }

   if($module eq 'EK1100') { add_EK1100($address,$prefix) ; }
   if($module eq 'CU1128') { add_CU1128($address,$prefix) ; }
   if($module eq 'EL1018') { add_EL1018($address,$prefix) ; }
   if($module eq 'EL2008') { add_EL2008($address,$prefix) ; }
   if($module eq 'EL2088') { add_EL2088($address,$prefix) ; }
   if($module eq 'EL2042') { add_EL2042($address,$prefix) ; }
   if($module eq 'EL2624') { add_EL2624($address,$prefix) ; }
   if($module eq 'EP2316') { add_EP2316($address,$prefix) ; }
   if($module eq 'EP2338') { add_EP2338($address,$prefix) ; }
   if($module eq 'EP2349') { add_EP2349($address,$prefix) ; }
   if($module eq 'JXCE1' ) { add_JXCE1( $address,$prefix) ; }
   if ($module eq 'BK1120') {add_BK1120($address,$prefix) ; }
   if ($module eq 'KL1702') {add_KL1702() ; }
   if ($module eq 'KL2702') {add_KL2702() ; }
   if ($module eq 'KL9010') {add_KL9010() ; }
   }


# some globals for k-bus
# 

my $kb_addr ;
my $kb_prefix ;

my $kb_icount = 0 ;
my $kb_ocount = 0 ;

sub
add_BK1120
	{
	my ($address, $prefix) = @_ ;
	$kb_addr = $address ;
	$kb_prefix = $prefix ;
	$kb_icount = 0 ;
	$kb_ocount = 0 ;
	}
	

sub 
add_KL9010
	{
   printf("adding 9010 addr %d in %d out  %d prefix %s\n",
         $kb_addr,$kb_icount,$kb_ocount,$kb_prefix) ;
	if ($kb_icount > 0) 
		{
		Trk::dp_adddevice($kb_prefix.'i','BK1120 '.$kb_addr,'io:ethercat',
			 '1:'. sprintf("%x",$kb_addr).':'.
		     '2:'. '04602c22:'.
                     '6000:'.
                     '1') ;
					 
		for (my $m = 1 ; $m <= $kb_icount ; $m++) 
			{
			for(my $io=0 ; $io<2 ; $io++)
				{
				Trk::dp_new($kb_prefix.sprintf("m%di%d",$m,$io+1),
                  $kb_prefix.'i',
                  ($m-1)*2 + $io,
                  $kb_prefix.' input '.sprintf("mod %d pt %d",$m,$io+1)) ;
				} 
			}
		}
      if ($kb_ocount > 0) 
		{
		Trk::dp_adddevice($kb_prefix.'o','BK1120 '.$kb_addr,'io:ethercat',
			 '0:'. sprintf("%x",$kb_addr).':'.
		     '2:'. '04602C22:'.
                     sprintf("%x:",0x7000 + 16 * $kb_icount).
                     '1') ;
		for (my $m = 1 ; $m <= $kb_ocount ; $m++) 
			{
			for(my $io=0 ; $io<2 ; $io++)
				{
				Trk::dp_new($kb_prefix.sprintf("m%do%d",$m+$kb_icount,$io+1),
                  $kb_prefix.'o',
                  ($m-1)*2 + $io,
                  $kb_prefix.' output '.sprintf("mod %d pt %d",$m,$io+1)) ;
				} 
			}					 
		}
	  
	}

sub
add_KL1702
	{
	$kb_icount =  $kb_icount+1 ; # at this point a module count
	}
sub
add_KL2702
	{
	$kb_ocount = $kb_ocount+1  ; # at this point a module count
	}
	

sub 
add_EK1100
   {
   my ($address,$prefix) = @_ ;
   }

sub
add_CU1128
  {
  my ($address,$prefix) = @_ ;
  }

sub 
add_EL1018
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'i','EL1018 '.$address,'io:ethercat',
                     '1:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '3fa3052:'.
                     '6000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'i'.sprintf("%d",$io+1),
                  $prefix.'i',
                  $io,
                  $prefix.' input '.sprintf("%d",$io+1)) ;
      } ;
   }

sub 
add_EL2008
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EL2008 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '7d83052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;
   }

sub 
add_EL2088
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EL2088 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '8283052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;
   }


sub 
add_EL2042
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EL2042 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '7fa3052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<2 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;
   }
 
sub 
add_EL2624
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EL2624 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     'a403052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<4 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' relay '.sprintf("%d",$io+1)) ;
      } ;
   }

sub 
add_EP2316
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EP2316 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '90c4052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;

   Trk::dp_adddevice($prefix.'i','EP2316 '.$address,'io:ethercat',
                     '1:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '90c4052:'.
                     '6000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'i'.sprintf("%d",$io+1),
                  $prefix.'i',
                  $io,
                  $prefix.' input '.sprintf("%d",$io+1)) ;
      } ;
   }
 
sub 
add_EP2338
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o','EP2338 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '9224052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;

   Trk::dp_adddevice($prefix.'i','EP2338 '.$address,'io:ethercat',
                     '1:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '9224052:'.
                     '6000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'i'.sprintf("%d",$io+1),
                  $prefix.'i',
                  $io,
                  $prefix.' input '.sprintf("%d",$io+1)) ;
      } ;
   }

sub 
add_EP2349
   {
   my ($address,$prefix) = @_ ;
   my $io ;

   Trk::dp_adddevice($prefix.'o0','EP2349 0 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '92d4052:'.
                     '7000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+1),
                  $prefix.'o0',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+1)) ;
      } ;

   Trk::dp_adddevice($prefix.'o1','EP2349 1 '.$address,'io:ethercat',
                     '0:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '92d4052:'.
                     '7080:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'o'.sprintf("%d",$io+8+1),
                  $prefix.'o1',
                  $io,
                  $prefix.' output '.sprintf("%d",$io+8+1)) ;
      } ;

   Trk::dp_adddevice($prefix.'i0','EP2349 0 '.$address,'io:ethercat',
                     '1:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '92d4052:'.
                     '6000:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'i'.sprintf("%d",$io+1),
                  $prefix.'i0',
                  $io,
                  $prefix.' input '.sprintf("%d",$io+1)) ;
      } ;

   Trk::dp_adddevice($prefix.'i1','EP2349 1 '.$address,'io:ethercat',
                     '1:'.
                     sprintf("%x",$address).':'.
                     '2:'.
                     '92d4052:'.
                     '6080:'.
                     '1') ;
   for($io=0 ; $io<8 ; $io++)
      {
      Trk::dp_new($prefix.'i'.sprintf("%d",$io+8+1),
                  $prefix.'i1',
                  $io,
                  $prefix.' input '.sprintf("%d",$io+8+1)) ;
      } ;
   }

sub
add_JXCE1
  {
  my ($address,$prefix) = @_ ;

  my %outs = (
    '7010' => 2,
    '7011' => 2,
    '7012' => 1,
    '7020' => 1,
    '7021' => 2,
    '7022' => 4,
    '7023' => 2,
    '7024' => 2,
    '7025' => 2,
    '7026' => 2,
    '7027' => 2,
    '7028' => 2,
    '7029' => 4,
    '702a' => 4,
    '702b' => 4 ) ;

  foreach my $pdo (keys %outs)
    {
    # print $pdo." ".$outs{$pdo}."\n" ;
   
    my $dev = $prefix . '.' . $pdo ;
 
    Trk::dp_adddevice($dev,
                      'JXCE1 '.$address.' '.$pdo,
                      'io:ethercat',
                      '2:'
                      . sprintf("%x",$address).':'
                      . '114:'
                      . '100003f:'
                      . $pdo.':'
                      . '0') ;
    Trk::dp_new($dev, $dev, $outs{$pdo}, $dev) ;
    }

  my %ins = (
    '6010' => 2,
    '6011' => 2,
    '6020' => 4,
    '6021' => 2,
    '6022' => 2,
    '6023' => 4
    ) ;

  foreach my $pdo (keys %ins)
    {
    # print $pdo." ".$ins{$pdo}."\n" ;
    
    my $dev = $prefix . '.' . $pdo ;

    Trk::dp_adddevice($dev,
                      'JXCE1 '.$address.' '.$pdo,
                      'io:ethercat',
                      '3:'
                      . sprintf("%x",$address).':'
                      . '114:'
                      . '100003f:'
                      . $pdo.':'
                      . '0') ;

    Trk::dp_new($dev, $dev, $ins{$pdo}, $dev) ;
    }

  Trk::dp_adddevice($prefix.'.6030:1',
                    'JXCE1 '.$address.' 6030:1',
                    'io:ethercat',
                    '3:'
                    . sprintf("%x",$address).':'
                    . '114:'
                    . '100003f:'
                    . '6030:'
                    . '1') ;
  Trk::dp_new($prefix.'.6030:1',
              $prefix.'.6030:1',
              1,
              $prefix.'.6030:1') ;
  }

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

ethercat - Perl extension for Trak EtherCat I/O definition.

=head1 SYNOPSIS

  use ethercat;
  ethercat::add('EL1018',{'address'=>'1'}) ; # sets address
  ethercat::add('EL2008',{'prefix'=>'m2'}) ; # sets slot prefix 
  ethercat::add('EL2008) ;    # assumes next address, prefix m+address

=head1 DESCRIPTION

This module constructs Trak drivers, devices, and points
to implement EtherCat modules.

=head1 AUTHOR

Mark Woodworth. mwoodworth@numinagroup.com

=head1 SEE ALSO

perl(1).

=cut
