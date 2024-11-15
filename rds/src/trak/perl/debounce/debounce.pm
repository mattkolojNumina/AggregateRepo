package debounce;

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
          on=>$on_timer,desc=>$desc,
         off=>$off_timer,type=>"debounce"}) ;
    return $h ;
}


# Autoloaded methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

dead - Perl extension to add dead timed inputs to Trak



=head1 SYNOPSIS

  require debounce;

DEPRECATED
 dead::link(name,input dp,off,on,timer,desc)

SEE
  condition::create

=head1 DESCRIPTION

Use require not use, for inclusion. Use will re-evaluate the perl module each
time a use statement is seen. Require loads it once, and a "require" of
a previously "required" module will do nothing.

This module will debounce an input. The conditioned I/O point "name" will
be created as a "virtual" dp. Debouncing means that the input point must 
change state and not change for the preset time period before the "name"
point is changed. This is different than dead time conditioning. This is
appropriate for line-full and jam eyes. 

=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

timer,debounce,Trk,perl(1).

=cut

