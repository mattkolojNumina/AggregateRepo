package motor;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
  link, chain	
);
$VERSION = '0.01';


# Preloaded methods go here.
require Trk ;
require trakbase ;
require timer ;
require dp ;
require zone ;
my @code ;

@code=qw( 4 swap dp.register.get ) ;
Trk::ex_compile("mtr.aux@","Fetch Motor Aux",\@code) ;
@code=qw( 1 swap dp.register.get ) ;
Trk::ex_compile("mtr.state@","Fetch Motor State",\@code) ;
@code=qw( 1 swap dp.register.set ) ;
Trk::ex_compile("mtr.state!","Set Motor State",\@code) ;
@code=qw( 5 swap dp.register.get ) ;
Trk::ex_compile("mtr.out@","Fetch Motor Output",\@code) ;
@code=qw( 2 swap dp.register.get ) ;
Trk::ex_compile("mtr.zone@","Fetch Motor Zone",\@code) ;
@code=qw( 0 swap dp.register.get ) ;
Trk::ex_compile("mtr.prev@","Fetch Previous Motor",\@code) ;
@code=qw( 6 swap dp.register.get ) ;
Trk::ex_compile("mtr.counter@","Fetch Motor Counter",\@code) ;
@code=qw( 6 swap dp.register.set ) ;
Trk::ex_compile("mtr.counter!","Fetch Motor Counter",\@code) ;
@code=qw( dup mtr.counter@ 1 + swap  mtr.counter! ) ;
Trk::ex_compile("mtr.counter++","Increment Motor Counter",\@code) ;
@code=qw( >r 0  6 r> dp.register.set ) ;
Trk::ex_compile("mtr.counter-reset","Reset Motor Counter",\@code) ;

@code=qw( >r  
	r@ mtr.out@ off
	r@ mtr.counter-reset
        0 r@ mtr.state! 
        r> off
        ) ;
Trk::ex_compile("mtr.reset","Reset Motor",\@code) ;

@code=qw(
        mtr.state@ 4 = if 0 else 1 then        
        ) ;
Trk::ex_compile("mtr.ok?","Is motor Ok to run",\@code) ;
@code=qw(
        >r 
        r@ mtr.ok? 
        if
        r@ mtr.out@ on
        r@ mtr.counter-reset
        1 r@ mtr.state!
        r> on
        else
        r> off
        then
        ) ;
Trk::ex_compile("mtr.start","Start Motor",\@code) ;

@code=qw(
        >r
        r@ mtr.out@  off
        r@ mtr.counter-reset
        3 r@ mtr.state!
        r> off
        ) ;
Trk::ex_compile("mtr.stop","Stop Motor",\@code) ;

@code=qw(
        a.set
        a mtr.out@ tst b.set
        a mtr.aux@ tst c.set
        b  c = 
        if 
          a mtr.counter-reset 
          b if 2 a mtr.state! else 0 a mtr.state! then
        else
          a mtr.counter++ 
          a mtr.counter@ 10 > 
            if 
            a mtr.stop 
            4 a mtr.state! 
            a mtr.zone@ zone.overload
            c a mtr.aux@ b a mtr.out@ a 200 6 mb_npost 
            then
        then
        ) ;
Trk::ex_compile("mtr.check","Check motor status",\@code) ;

# start next if not nil
# plus update state
@code=qw(
	 edge.dp a.set
        2 partner@  mtr.state!
        ) ;
Trk::ex_compile("mtr.started","Motor Aux returned on",\@code) ;
# stop previous if not nil + update state
@code=qw(
	 edge.dp a.set
        0 partner@ mtr.state!
        ) ;
Trk::ex_compile("mtr.stopped","Motor Aux returned off",\@code) ;

sub link {
    my $name = @_[0] ;
    my $input = @_[1] ;
    my $output = @_[2] ;
    my $zone = @_[3] ;
    my $desc = @_[4] ;
    my $b ;

    my $h = Trk::dp_new($name,"virtual","blank",$desc) ;
    my $o_h = trakbase::dhandle($output) ;
    my $i_h = trakbase::dhandle($input) ;
    my $z_h = trakbase::dhandle($zone) ;

  Trk::dp_registerset($h,1,0) ;
  Trk::dp_registerset($h,4,$i_h) ;
  Trk::dp_registerset($h,5,$o_h) ;
  Trk::dp_registerset($h,2,$z_h) ;
  Trk::dp_registerset($h,0,-1) ;
  Trk::dp_registerset($h,3,-1) ;

  Trk::dp_registerset($i_h,2,$h) ;
  Trk::dp_registerset($o_h,2,$h) ;


    my @code=( $name, "mtr.check" ) ;
  my $ex_name = $name . ":mtr.check" ;
  Trk::ex_compile($ex_name,"Test  Motor " . $name,\@code) ;
  my $foo = pop(@code) ;
  trakbase::leading("tm_100ms",$ex_name,$name . " :Check on motor status") ;

  trakbase::leading($input,"mtr.started","Motor did start") ;
  trakbase::trailing($input,"mtr.stopped","Motor did start") ;

  @code = ($name , "mtr.stop") ;
  Trk::ex_compile($name . ":zonestop","Stop when zone is turned off",\@code) ;
  trakbase::trailing($zone,$name . ":zonestop",
         "Stop " . $name . " when zone does") ;
  $foo = pop(@code) ;
}


@code=qw( 
	  edge.dp a.set
          edge.value
	  if
          partner@ mtr.prev@ mtr.start 
          else
          partner@ mtr.prev@ mtr.stop
          then
        ) ;
Trk::ex_compile("mtr.chain.edge","To Chain, Edge",\@code) ;

sub chain {
    my $motor = @_[0] ;
    my $previous = @_[1] ;

    my $h = trakbase::dhandle($motor) ;
    my $p_h = -1 ;
    if ($previous ne '') {
      $p_h = trakbase::dhandle($previous) ;
      Trk::dp_registerset($h,0,$p_h) ;
    }

    my $i_h = Trk::dp_registerget($h,4) ;
  my $input ;
  $input  = Trk::dp_getname($i_h) ;
  trakbase::leading($input,"mtr.chain.edge",
          $motor . " Chained to " . $previous) ;
  trakbase::trailing($input,"mtr.chain.edge",
          $motor . " Chained to " . $previous) ;
}


sub start {
  my $name = @_[0] ;
  my $zone = @_[1] ;

  my @code=($name, "mtr.start") ;
  my $st = $name . ":start-with-zone" ;
  Trk::ex_compile($st,"Startup tied to zone",\@code) ;
  my $foo = pop(@code) ;

  trakbase::leading($zone,$st,"When zone starts, start motor: " . $name) ;
}
# Autoload methods go after =cut, and are processed by the autosplit program.




1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

motor - Perl extension for defining and chaining motors in Trk.

=head1 SYNOPSIS

  use motor;
  link(name,input(aux),output(contactor),zone,description) ;
  chain(name,previous) ;

=head1 DESCRIPTION

The motor module allows definition and chaining of motors.


=head1 AUTHOR

Boi M. I.  Thor, ouch@a.galaxy.far.far.away
Mark Olson. Numina Systems Corporation. marko@numinasys.com

=head1 SEE ALSO

Trk,perl(1).

=cut
