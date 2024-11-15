package rp;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
  const, handle	
);
$VERSION = '0.01';


# Preloaded methods go here.
require Trk ;
require trakbase ;

sub const {
    my $name = @_[0] ;
    my $def = @_[1] ;
    my $desc = @_[2] ;
    my $data = @_[3] ;
    my $value ;
    my $handle ;

    $handle = Trk::rp_handle($name) ;
    if ($handle == -1) {
	$handle = Trk::rp_new($name,"register",$data,$desc) ;

      Trk::rp_set($handle,$def) ;
    }
    return $handle ;
}

sub data {
    my $handle = @_[0] ;
    my $data = @_[1] ;
    my $rph;

    $rph = $handle;
    return Trk::rp_data($rph,$data) ;
}

sub prop {
    my $handle = @_[0] ;
    my $key = @_[1] ;
    my $value = @_[2] ;
    my $rph;

    $rph = $handle;
    return Trk::rp_prop($rph,$key,$value) ;
}

sub handle {
  my $handle = Trk::rp_handle(@_[0]) ;
    if ($handle == -1) {
	die "Could not locate constant: " . @_[0] ;
    }
  return $handle ;
}
    

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

rp - Perl extension for rp points in Trk.

=head1 SYNOPSIS

  use rp;
rp::handle (name) ;
rp::const( name, default, description) ;

=head1 DESCRIPTION

The rp package exports two subs to assist in handling register values. One is
"handle" which locates the handle of a data point, and "die's" if it is not 
found. This was done so many times in my code, that sub'ing it seemed a 
logical step, even if the sub was very simple.

The const function creates the register if it doesn't exist. If it does exist, 
then the register point is created, with the given description and initial
value (the initial default value is not changed if the data point already
exists).


=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
