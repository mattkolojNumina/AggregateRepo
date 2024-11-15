package cif;

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

my $haveDriver = 0 ;

sub
checkDriver
  {
  if($haveDriver==0)
    {
    Trk::dp_adddriver("io:cif","Hilscher CIF driver") ;
    Trk::dp_adddevice("cif0","Hilscher CIF card 0","io:cif","0") ;
    $haveDriver = 1 ;
    }
  }

sub
add
  {
  my($name,$io,$desc) = @_ ;

  checkDriver() ;

  Trk::dp_new($name,"cif0",$io,$desc) ;
  }
 
# Autoload methods go after =cut, and are processed by the autosplit program.
1;

__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

=head1 SYNOPSIS

=head1 DESCRIPTION

=head1 AUTHOR

Mark Woodworth. 

=head1 SEE ALSO

perl(1).

=cut

