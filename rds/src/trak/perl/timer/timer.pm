package timer;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	
);
$VERSION = '0.01';


# Preloaded methods go here.
require Trk ;
Trk::dp_new("tm_1s","time","2000","1 second click") ;
Trk::dp_new("tm_500ms","time","1000","500 millisecond click") ;
Trk::dp_new("tm_100ms","time","200","100 millisecond click") ;
my $fast = Trk::dp_new("tm_10ms","time","20","10 millisecond click") ;
# 1 millisecond timing requires kernel device.
Trk::dp_new("tm_1ms","time","2","1 millisecond click") ;



#
# The folowing creates a function timer::link which takes four arguments
# name -> new timer dp name
# test -> if the test is true this timer "runs".
# base -> the base timer to use.
# desc -> description for new timer object.
#

my @code=qw( tst if edge.value swap dp.value.set else drop then ) ;
Trk::ex_compile("tmr_linker","Word used linking timers",\@code) ;


require trakbase ;
sub link {
    my $name = @_[0] ;
    my $test = @_[1] ;
    my $base = @_[2] ;
    my $desc = @_[3] ;


    my $handle = Trk::dp_handle($name) ;

    if ($handle == -1) {
#	print "not found " . $name . " create\n" ;
	$handle = Trk::dp_new($name,"virtual","blank",$desc) ;
    }	
	@code = ( $name, $test , "tmr_linker") ;

      Trk::ex_compile($name . "_time",$name . " linked to " . $test , \@code) ;
    my $foo = pop(@code) ;
    
    my $t_h = trakbase::dhandle($base) ;

  #  print "base = $base\n" ;
  #  print "test = $test\n" ;

  trakbase::leading($base,$name . "_time","Dependent Timer") ;
  trakbase::trailing($base,$name . "_time","Dependent Timer") ;
    
}


# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

timer - Perl extension for Trak to handle standard timers

=head1 SYNOPSIS

  use timer;
 timer::link --- adds a timer, based on a timer + an input. This new timer only

=head1 DESCRIPTION

This module installs the time device and driver into Trak. Then it installs
four standard timers tm_1ms, tm_10ms, tm_100ms, and tm_1s. These dp's click at
the indicated rates.


timer::link takes for arguments: 
  *) the name of the new timer dp.
  *) the test, which when true causes the current dp to run.
  *) the base timer. When the test is true, the new timer dp is slaved to this
     dp.
  *) a description for the new object.


=head1 AUTHOR

M. I. Goofy goofy@a.galaxy.far.far.away
Mark Olson marko@numinasys.com

=head1 SEE ALSO

Trk,trakbase,perl(1).

=cut
