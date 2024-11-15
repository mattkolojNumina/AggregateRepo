package h3g;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);
use Sys::Hostname;

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
@EXPORT = qw(module instantiate);
$VERSION = '0.01';

# Preloaded methods go here.
require Trk ;
require trakbase ;
require dp ;
require DBI ;

my $haveDriver = 0 ;

sub
checkDriver
  {
  if($haveDriver==0)
    {
    Trk::dp_adddriver("io:h3g","Hilmot 3g/octo driver") ;
    $haveDriver = 1 ;
    }
  }

sub
add
  {
  my($device,$data) = @_ ;
  my $i ;

  my $ip   = $data->{'ip'} ;
  my $stem = $data->{'stem'} ; 

  my $desc   = $data->{'description'} ;
  if($desc eq '')
    { $desc = 'h3g '.$device}  ;
  my $virtual = 0 ;
  if($data->{'virtual'} ne '') { $virtual = 1 } ;

  checkDriver() ;

  Trk::dp_adddevice($device,$desc,"io:h3g",'direct:'.$ip) ; 
  for($i=0 ; $i<8 ; $i++)
    {
    Trk::dp_new('i'.$stem.($i+1),
                $device,
                'i'.$i,
                $desc.' input '.($i+1) ) ;
    Trk::dp_new('o'.$stem.($i+1),
                $device,
                'o'.$i,
                $desc.' output '.($i+1) ) ;
    }  
    dp::virtual($device . '_comm',$device . ' card comms state') ;
  }

 
# Autoload methods go after =cut, and are processed by the autosplit program.
1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

smart3g - Perl extension for smart3g modules in Trak.


=head1 SYNOPSIS

  use smart3g ;

  smart3g::module() ;

=head1 DESCRIPTION

The smart3g module allows easy generation of generic DPs for Smart 3g
modules.

=head1 AUTHOR

Mark Woodworth. 

=head1 SEE ALSO

perl(1).

=cut

