#
#   linx.pm
#
#   (C) Copyright 2019 Numina Group, Inc.  All Rights Reserved.
#

package linx;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);

@EXPORT = qw();
$VERSION = '0.01';

require Trk ;
require dp ;
require timer ; 
require traksort ;

my $haveDriver = 0 ;
my $lastAddress = -1 ;
my @code ;

@code=qw(
  >r
  r@ on
  tm_10ms &delay_clr 25 r> 0 0 0 ev_insert 
  ) ;
Trk::ex_compile('linx_pulse','short pulse',\@code) ;

sub
checkDriver
   {
   if($haveDriver==0)
      {
      Trk::dp_adddriver("io:linx","ID Linx driver") ;
      $haveDriver = 1 ;
      }
   }

sub 
add 
  {
  my ($base, $linx_data) = @_;

  # create the device
  my $ip = $linx_data->{'ip'} ;
  my $desc = $linx_data->{'desc'} ;
  if ($desc eq '') {
       $desc = $base . ' idxlinx at '. $ip ;
   }

  checkDriver() ;
  Trk::dp_adddevice( $base.'_dev', $desc, 'io:linx', $ip );
  dp::virtual($base.'_comm',$base.' communicating') ;   
  dp::virtual($base.'_fault',$base.' fault') ;

  if($linx_data->{'reset'} ne '')
    {
    my @stub = ($base.'_fault','off') ;
    Trk::ex_compile($base.'_fault_off',$base.' reset',\@stub) ;
     trakbase::leading($linx_data->{'reset'},$base.'_fault_off',$base.' reset') ; 
    my $dummy = pop(@stub) ;
    }

  if($linx_data->{'run'} ne '')
    {
    my @stub = ($linx_data->{'run'},'tst','if',$base.'_fault','on','then') ;
    Trk::ex_compile($base.'_fault_on',$base.' trip',\@stub) ;
    trakbase::trailing($base.'_comm',$base.'_fault_on',$base.' trip') ;
    my $dummy = pop(@stub) ;
    }
  else
    {
    my @stub = ($base.'_fault','on') ;
    Trk::ex_compile($base.'_fault_on',$base.' trip',\@stub) ;
    trakbase::trailing($base.'_comm',$base.'_fault_on',$base.' trip') ;
    my $dummy = pop(@stub) ;
    }

  define_io($base,'sns_a',      {'report'=>'both'});
  define_io($base,'sns_b',      {'report'=>'both'});
  define_io($base,'mtr_a_aux',  {'report'=>'both'});
  define_io($base,'mtr_b_aux',  {'report'=>'both'});
  define_io($base,'sns_a_flt',  {'report'=>'lead'});
  define_io($base,'sns_b_flt',  {'report'=>'lead'});
  define_io($base,'mtr_a_flt',  {'report'=>'both'});
  define_io($base,'mtr_b_flt',  {'report'=>'both'});
  define_io($base,'in1',        {'report'=>'both'});
  define_io($base,'in2',        {'report'=>'both'});
  define_io($base,'in3',        {'report'=>'both'});
  
  define_io($base,'mtr_a',      {'report'=>'both'});
  define_io($base,'mtr_b',      {'report'=>'both'});
  define_io($base,'mtr_a_dir',  {'report'=>'both'});
  define_io($base,'mtr_b_dir',  {'report'=>'both'});
  define_io($base,'mtr_a_rst',  {'report'=>'lead','oneshot'=>'true'});
  define_io($base,'mtr_b_rst',  {'report'=>'lead','oneshot'=>'true'});
  define_io($base,'out1',       {'report'=>'both'});
  define_io($base,'out2',       {'report'=>'both'});
  define_io($base,'out3',       {'report'=>'both'});
  define_io($base,'out4',       {'report'=>'both'});
  define_io($base,'out5',       {'report'=>'both'});
  }

sub 
define_io 
  {
  my ($base,$type,$extra) = @_;

  my $ppi = 'tm_1ms' ;
  if ($extra->{'ppi'} ne '') 
    {
    $ppi = $extra->{'ppi'} ; 
    }

  my $dp = $base.'_'.$type;
  my $h = Trk::dp_new( $dp, $base.'_dev',  $type, $base.' '.$type );

  if($extra->{'report'} eq 'lead') 
    {
    traksort::report_ld($dp,$ppi);
    } 
  elsif($extra->{'report'} eq 'trail') 
    {
    traksort::report_trl($dp,$ppi);
    } 
  elsif($extra->{'report'} eq 'both') 
    {
    traksort::report_ld($dp,$ppi);
    traksort::report_trl($dp,$ppi);
    }

  if($extra->{'oneshot'} ne '') 
    {
    my @stub = ($dp,"linx_pulse");
    Trk::ex_compile($dp."_oneshot",$dp." oneshot",\@stub);
    trakbase::leading($dp,$dp."_oneshot",$dp." oneshot");
    my $dummy = pop(@stub);
    }
  }

# return a true value to indicate successful initialization
1;

__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

linx - Perl extension for Trak ID Linxdefinition.

=head1 SYNOPSIS

  use linx;

=head1 DESCRIPTION

This module constructs Trak drivers, devices, and points
to implement ID Linx modules.

=head1 AUTHOR

Mark Woodworth. mwoodworth@numinagroup.com

=head1 SEE ALSO

perl(1).

=cut
