package dead;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	     link
	     );

$VERSION = '0.01';


# Preloaded methods go here.


require condition ;

sub link {
    my $name = @_[0] ;
    my $old = @_[1] ;
    my $off_timer = @_[2] ;
    my $on_timer = @_[3] ;
    my $timer = @_[4] ;
    my $desc = @_[5] ;

    my $h =  condition::create($name,$old,{ timer=>$timer,
          on=>$on_timer,desc=>$desc,type=>'dead',
         off=>$off_timer}) ;
    return $h ;
}


# Autoloaded methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

dead - Perl extension to add dead timed inputs to Trak



=head1 SYNOPSIS

  require dead;

DEPRECATED
 dead::link(name,input dp,off,on,timer,desc)

SEE
  condition::create

=head1 DESCRIPTION

Use require not use, for inclusion. Use will re-evaluate the perl module each
time a use statement is seen. Require loads it once, and a "require" of
a previously "required" module will do nothing.

This modoule (dead) creates code "statements" in Trak to handle dead time 
conditioning of data points. An I/O point with dead time conditioning will 
change state with the input i/o dp, but then until the dead time has expired
will no longer change state until the dead time has passed. This is similar
(but not the same) as debounced I/O. Note: the input "name" data point, i.e.,
the new conditioned I/O *is* created by this call.

For the input dp, some registers are used. Please do not use these for
another purpose. We use register 1,2,4,5,6 (state,partner,4,5,6).
state (register 1) holds a state variable. partner holds the index of the
conditioned data point. Register 4 contains the On preset. The On preset
 is an index to the register containing the
on preset. Register 5 contains the Off preset.
Register 6 contains the index of the timer dp.


=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

timer,debounce,Trk,perl(1).

=cut

