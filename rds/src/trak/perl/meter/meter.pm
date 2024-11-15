package meter;

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

# Autoload methods go after =cut, and are processed by the autosplit program.

use Trk;

package meter ;

require trakbase ;
require timer ;
require rp ;
require dp ;
require zone ;


my @code;

@code=qw( 0 swap dp.register.get ) ;
Trk::ex_compile("meter-inhibit@","Fetch Meter Inhibit",\@code) ;
@code=qw( 0 swap dp.register.set ) ;
Trk::ex_compile("meter-inhibit!","Store Meter Inhibit",\@code) ;

@code=qw( 1 swap dp.register.get ) ;
Trk::ex_compile("meter-state@","Fetch Meter Inhibit",\@code) ;
@code=qw( 1 swap dp.register.set ) ;
Trk::ex_compile("meter-state!","Store Meter Inhibit",\@code) ;


@code=qw( 2 swap dp.register.get ) ;
Trk::ex_compile("meter-bx@","Fetch Meter Zone",\@code) ;

@code=qw( 3 swap dp.register.get tst ) ;
Trk::ex_compile("meter-eye@","Fetch Meter Solenoid (Clutch/Brake)",\@code) ;

@code=qw( 4 swap dp.register.get ) ;
Trk::ex_compile("meter-air@","Fetch Meter Solenoid (Clutch/Brake)",\@code) ;

@code=qw( 5 swap dp.register.get ) ;
Trk::ex_compile("meter-motor@","Fetch Meter Motor",\@code) ;

@code=qw( 6 swap dp.register.get rp.value.get ) ;
Trk::ex_compile("meter-preset@","Fetch Meter Preset",\@code) ;


@code=qw( 7 swap dp.register.get ) ;
Trk::ex_compile("meter-tach@","Fetch Meter Tach/Timer",\@code) ;


# the guts of metering are in these next words.

@code=qw( dup  meter-eye@ not  if  meter-air@ off  else  drop  then ) ;
Trk::ex_compile("meter-nudge","run belt if not blocked",\@code) ;

@code=qw( nop ) ;
Trk::ex_compile("meter-send","define ahead of time",\@code) ;

@code=qw(
        203 a  a meter-eye@  mb_post
        0 a meter-inhibit!
        a meter-eye@ if a meter-send else a meter-air@ off then
        ) ;
Trk::ex_compile("meter-event","Event posted to clear inhibit",\@code) ;

@code=qw( 
        >r
        r@ meter-tach@  &meter-event  r@ meter-preset@
        r@ 0 0 0 ev_insert
        1 r@ meter-inhibit!
        202 r@  r@ meter-inhibit@  mb_post
	r@ meter-bx@ on
        r> meter-air@ off
        ) ;
Trk::ex_compile("meter-send","On leading edge of input",\@code) ;

@code=qw( 
        >r 
        r@ meter-state@ 2 = not
        r@ meter-motor@ tst 
        r@ meter-eye@ 
        r> meter-inhibit@ not
        and  and  and
        ) ;
Trk::ex_compile("meter-ok?","Test if we can send this parcel",\@code) ;


@code=qw( meter-air@ on ) ;
Trk::ex_compile("meter-halt","Stop this parcel",\@code) ;

@code=qw( dup meter-ok? if  meter-send  else  meter-halt then) ; 
Trk::ex_compile("meter-release","release/jog meter belt",\@code) ;

@code=qw( 2 swap meter-state! ) ;
Trk::ex_compile("meter-stop","disable Meter belt",\@code) ;

@code=qw( 
         >r  
         0 r@ meter-state! 
         r@ meter-eye@
         if 
           r> meter-send
         else
           r> meter-air@ off
         then
        ) ;
Trk::ex_compile("meter-start","enable Meter belt",\@code) ;


# on leading edge test if it can release. Make the decision to halt or not.
@code=qw( 
	  partner@ dup meter-ok? if meter-send else meter-halt then 
	  ) ;
Trk::ex_compile("meter-ld-edge","Meter belt leading edge",\@code) ;

# on trailing edge, clear the box.
@code=qw(
	 partner@ meter-bx@ off
	 ) ;
Trk::ex_compile("meter-trl-edge","Meter belt trailing edge",\@code) ;


sub link {
    my $name = @_[0] ;
    my $input = @_[1] ;
    my $output = @_[2] ;
    my $motor = @_[3] ;
    my $preset = @_[4] ;
    my $tach = @_[5] ;
    my $desc = @_[6] ;

    my $handle = Trk::dp_handle ($name) ;
    if ($handle == -1) { # we need to create the meter belt controls.
	$handle = Trk::dp_new($name,"virtual","blank",$desc) ;
	my $bx_h = Trk::dp_new($name . "_bx","virtual","blank",
			       $name . " Box Indicator/Counter") ;

	my $i_h = Trk::dp_handle($input) ;
	my $o_h = Trk::dp_handle($output) ;
	my $p_h = Trk::rp_handle($preset) ;
        my $m_h = Trk::dp_handle($motor) ;
	my $t_h = Trk::dp_handle($tach) ;

      Trk::dp_registerset($bx_h,2,$handle) ; # assign partner for dp.
      Trk::dp_registerset($i_h,2,$handle) ;

      Trk::dp_registerset($handle,1,0) ;
      Trk::dp_registerset($handle,2,$bx_h) ;
      Trk::dp_registerset($handle,3,$i_h) ;
      Trk::dp_registerset($handle,4,$o_h) ;
      Trk::dp_registerset($handle,5,$m_h) ;
      Trk::dp_registerset($handle,6,$p_h) ;
      Trk::dp_registerset($handle,7,$t_h) ;

      trakbase::leading($input,"meter-ld-edge",$name . " leading edge") ;
      trakbase::trailing($input,"meter-trl-edge",$name . " trailing edge") ;

    }
    
}



1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

meter - Perl extension for setting up meter belts in Trak.

=head1 SYNOPSIS

  use meter;
 meter::link(name,input,output,motor,preset,tach,description) ;

=head1 DESCRIPTION

This module allows a input/output to meter parcels out. 
Metering is based on carton pitch (not gap). This creates the dp name. 
Control of the meter belt at that point is given by the following 
trak "statements":
  name meter-start --- starts the meter belt metering parcels out.
  name meter-stop --- stops the belt when the first parcel reaches the 
           eye (input).

Also a dp, "meter" . "_bx" is created. This is set to true when a parcel
is released and false when it clears the eye. 

=head1 AUTHOR

Mark Olson   marko@numinasys.com

=head1 SEE ALSO

Trk,perl(1).

=cut
