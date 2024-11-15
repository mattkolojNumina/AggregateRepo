package panel;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
 mcr, estop,start,stop,reset,jam_light,overload_light,start_light,estop_light
);
$VERSION = '0.01';


# Preloaded methods go here.

# Autoload methods go after =cut, and are processed by the autosplit program.

require Trk ;
require zone ;

my @code=qw(edge.dp dp.partner.get zone.estop) ;
Trk::ex_compile("mcr.lost","When MCR drops, e-stop the zone",\@code) ;
sub mcr {
  my $zone = @_[0] ;
  my $input = @_[1] ;

  my $i_h = trakbase::dhandle($input) ; 
  my $z_h = trakbase::dhandle($zone) ; 

  Trk::dp_registerset($i_h,2,$z_h) ;
  trakbase::trailing($input,"mcr.lost","MCR dropped out") ;
}

@code=qw(edge.dp dp.partner.get  zone.estop ) ;
Trk::ex_compile("estop.pulled","An E-stop has been pulled",\@code) ;

# 
@code=qw( tst not if on then ) ; 
Trk::ex_compile("test.for.estop","E-stop test",\@code) ;
@code=qw( tst if on then ) ; 
Trk::ex_compile("revtest.for.estop","E-stop test",\@code) ;

sub estop{
  my $zone = @_[0] ;
  my $input = @_[1] ;
  my $light = @_[2] ;
  my $direct = @_[3] ;

  my $i_h = trakbase::dhandle($input) ; 
  my $z_h = trakbase::dhandle($zone) ; 
  Trk::dp_registerset($i_h,2,$z_h) ;

  trakbase::trailing($input,"estop.pulled","E-stop pulled out") ;
  my @code ;
  if ($direct eq "reverse") {
    @code= ( $light, $input, "revtest.for.estop") ;
  } 
  else {
    @code= ( $light, $input, "test.for.estop") ;
  }
  my $st = $light . "-" . $input . "-" . $zone . ".es.tst" ;
  Trk::ex_compile($st,"Test for estop",\@code) ;
  my $foo = pop(@code) ; 

  trakbase::trailing("tm_500ms",$st,"E-stop Test") ;
  trakbase::leading("tm_500ms",$st,"E-stop Test") ;
}

@code=qw( edge.dp dp.partner.get zone.start ) ;
Trk::ex_compile("push.start","start pressed",\@code) ;
sub start {
  my $zone = @_[0] ;
  my $input = @_[1] ;
  my $desc = @_[2] ;

  my $i_h = trakbase::dhandle($input) ; 
  my $z_h = trakbase::dhandle($zone) ; 
  Trk::dp_registerset($i_h,2,$z_h) ;
  
  trakbase::leading($input,"push.start", $desc) ;
}

@code=qw( edge.dp dp.partner.get zone.stop ) ;
Trk::ex_compile("push.stop","start pressed",\@code) ;

sub stop {
  my $zone = @_[0] ;
  my $input = @_[1] ;
  my $desc = @_[2] ;

  my $i_h = trakbase::dhandle($input) ; 
  my $z_h = trakbase::dhandle($zone) ; 
  Trk::dp_registerset($i_h,2,$z_h) ;
  
  trakbase::trailing($input,"push.stop", $desc) ;
  
}

@code=qw( edge.dp dp.partner.get zone.reset ) ;
Trk::ex_compile("push.reset","start pressed",\@code) ;

sub reset {
  my $zone = @_[0] ;
  my $input = @_[1] ;
  my $desc = @_[2] ;

  my $i_h = trakbase::dhandle($input) ; 
  my $z_h = trakbase::dhandle($zone) ; 
  Trk::dp_registerset($i_h,2,$z_h) ;
  
  trakbase::leading($input,"push.reset", $desc) ;
  
}

@code=qw( >r 
          r@ zone.overload? 
          if 
          on  
          else
          r@ zone.jam? if  edge.value swap dp.value.set  else off  then
          then 
          r> drop ) ;
Trk::ex_compile("oload.test","test for jam condition",\@code) ;
sub jamoload_light {
  my $zone = @_[0] ;
  my $output = @_[1] ;
  
  my $z_h = trakbase::dhandle($zone) ;
  my $h = trakbase::dhandle($output) ;
   Trk::dp_registerset($h,2,$z_h) ;

  my @code=($output, $zone, "oload.test") ;
  Trk::ex_compile($output . ".olo.tst","Test jam",\@code) ;
  my $foo = pop(@code) ;
  trakbase::trailing("tm_500ms",$output . ".olo.tst","overload test") ; 
  trakbase::leading("tm_500ms",$output . ".olo.tst","overload test") ; 
}

@code=qw(
        a.set c.set
        a zone.state@ 0 = if edge.value c dp.value.set then
        a zone.state@ 1 = if c on then
        a zone.state@ 2 = if c on then
        a zone.state@ 3 = if c off then
        ) ;
Trk::ex_compile("start.indicator","Flash start lights",\@code) ;
sub start_light{
  my $zone = @_[0] ;
  my $output = @_[1] ;

  
  my @code=($output, $zone, "start.indicator") ;
  Trk::ex_compile($output . ".strt.lgt",$output . " is start light",\@code) ;
  trakbase::leading("tm_500ms",$output . ".strt.lgt","setup start light") ;
  trakbase::trailing("tm_500ms",$output . ".strt.lgt","setup start light") ;
}

@code=qw( zone.estop? if edge.value swap dp.value.set then ) ;
Trk::ex_compile("test.estop.light","Test for estop",\@code) ;

sub estop_light {
  my $zone = @_[0] ;
  my $output = @_[1] ;

  my @code=($output, $zone, "test.estop.light") ;
  Trk::ex_compile($output . ".tst.es","Test light for panel",\@code) ;
  my $foo = pop(@code) ;

  trakbase::leading("tm_500ms",$output . ".tst.es","test for estop") ;
  trakbase::trailing("tm_500ms",$output . ".tst.es","test for estop") ;
  @code=($output, "off") ;
  Trk::ex_compile($output . ".clr.rst","Clear output on reset",\@code) ;
  trakbase::leading($zone . ".reset",$output . ".clr.rst",
       "clear on reset") ;
  my $foo = pop(@code) ;
}

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

panel - Perl extension panel control/access in Trak.

=head1 SYNOPSIS

  use panel;
  panel::mcr
  panel::estop
  panel::start
  panel::stop
  panel::reset
  panel::jamoload_light 
  panel::start_light
  panel::estop_light

=head1 DESCRIPTION

This module encapulates the panel interface. Panels have mcr's estops,
start, stop, reset buttons, jam/overload lights, start and stop lights.

=head1 AUTHOR

Mark Olson marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
