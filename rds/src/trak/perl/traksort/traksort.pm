package traksort;

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
$VERSION = '0.02';


# Preloaded methods go here.

# make_bx_ld --- make a box on the leading edge of a data point
# make_bx_trl --- make a box on the trailing edge of a data point
# measure_bx --- measure box
# move_bx --- on leading edge, update box position (in box data)
# shadow_bx --- simulated eye downstream.
# shadow_bx_ld --- turn downstream eye on based on leading edge.
# shadow_bx_trl --- as above, but off trailing edge.
# send_bx_id --- pass a dp box id downstream to another dp. 
#     on leading edge, invalidate on trailing.
# send_bx_id_ld --- as above, but invalidate on fixed carton window.
# send_bx_id_trl --- as above, but on trailing edge.
# report_ld --- report box id and one tach value on leading edge.
# report_trl --- as above but on trailing edge.

require trakbase ;
require rp ;


rp::const("default_lane",1,"Default Lane") ;

my @code=qw( dup >r bx.new edge.dp dp.carton.set
	     r> dup dp.counter.get swap  edge.carton a 300 5 mb_npost 
             default_lane rp.value.get edge.carton bx.data.set
	     ) ;
Trk::ex_compile("bx_maker","Create a box",\@code) ;

sub make_bx_ld {
    my $name = @_[0] ; 
    my $tach = @_[1] ;

    my @code=($tach, "bx_maker") ;
  Trk::ex_compile($name. ".bx.make","Make box at " . $name,\@code) ;
    my $foo = pop(@code) ;
    
  trakbase::leading($name,$name . ".bx.make","Create Box") ;
}
sub make_bx_trl {
    my $name = @_[0] ; 
    my $tach = @_[1] ;

    my @code=($tach, "bx_maker") ;
  Trk::ex_compile($name. ".bx.make","Make box at " . $name,\@code) ;
    my $foo = pop(@code) ;
    
  trakbase::trailing($name,$name . ".bx.make","Create Box") ;
}
my @code=qw(
	    dp.counter.get swap  edge.dp dp.register.set
	    ) ;
Trk::ex_compile("bx_measure_begin","Begin measuring a box",\@code) ;

my @code=qw(
	    dp.counter.get r>
	    edge.dp dp.register.get
	    r> - 
	    edge.carton bx.length.set
	    edge.carton bx.length.get  
	    edge.carton edge.dp 301 4 mb_post
	    ) ;
Trk::ex_compile("bx_measure_end","Complete box measurement",\@code) ;


sub measure_bx {
    my $name = @_[0] ;
    my $tach = @_[1] ;
    my $register = @_[2] ;

    my @code=($register,$tach,"bx_measure_begin") ;
  Trk::ex_compile($name . ".len.go","Initiate box measure at " . $name,
		  \@code) ;
    my $foo = pop(@code) ;
  trakbase::leading($name,$name . ".len.go",
		    "Start Measuring " . $name) ;

    my @code=($register,$tach,"bx_measure_end") ;
  Trk::ex_compile($name . ".len.end","Complete length of " . $name,\@code) ;
    my $foo = pop(@code) ;
  trakbase::trailing($name,$name . ".len.end","Finding Length") ;
}

# move box     bx_id tach tach_value bx.move
@code=qw(
	 edge.carton swap  bx.move
	 ) ;
Trk::ex_compile("update.bx.wdw","Update box window",\@code) ;

sub move_bx {
    my $name = @_[0] ;
    my $tach = @_[1] ;
    my @code=($tach, "update.bx.wdw") ;
  Trk::ex_compile($name . ".update.bx","Update box window",\@code) ;
  trakbase::leading($name,$name . ".update.bx","Update box position") ;
}


# shadow_bx takes next tach shadow_bx 
# a = tach c = next d = offset (value)
@code=qw(
	 a.set c.set rp.value.get d.set
	 a   b if &delay_set else &delay_clr then  d 
	 c 0 0 0 ev_insert
	 ) ;
Trk::ex_compile("shadow_bx","Mirror image of box on virtual dp",\@code) ;

sub shadow_bx {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset = @_[3] ;

    my @code=($offset,$next, $tach, "shadow_bx") ;
    my $nstate = $name . "." . $next . ".shadow_bx" ;
  Trk::ex_compile($nstate,"Shadowing the box down the conveyor",\@code) ;
    my $foo = pop(@code) ;
  trakbase::leading($name,$nstate,"virtual eye on: " . $next) ;
  trakbase::trailing($name,$nstate,"virtual eye off" . $next) ;
    
}

# a= tach b = next c = window d = offset (value)
@code=qw(
	 a.set b.set rp.value.get c.set rp.value.get d.set
	 a &delay_set d  b 0 0 0 ev_insert
	 a &delay_clr d c + b 0 0 0 ev_insert
	 ) ;
Trk::ex_compile("shadow_bx.edge","Create box window on one edge",\@code) ;

sub shadow_bx_ld {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset= @_[3] ;
    my $window = @_[4] ;

    my @code=($offset,$window,$next, $tach, "shadow_bx.edge") ;
    my $nstate = $name . "." . $next . ".shdwx.ld" ;
  Trk::ex_compile($nstate,"Shadowing the box down the conveyor",\@code) ;
    my $foo = pop(@code) ;
  trakbase::leading($name,$nstate,"virtual eye on/off: " . $next) ;


}
sub shadow_bx_trl {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset=@_[3] ;
    my $window = @_[4] ;

    my @code=($offset,$window,$next, $tach, "shadow_bx.edge") ;
    my $nstate = $name . "." . $next . ".shdwx.trl" ;
  Trk::ex_compile($nstate,"Shadowing the box down the conveyor",\@code) ;
    my $foo = pop(@code) ;
  trakbase::trailing($name,$nstate,"virtual eye on/off: " . $next) ;
}

@code=qw( evt.d evt.a dp.carton.set 
	  evt.c dp.counter.get  evt.c  evt.d evt.a 303 5 mb_npost ) ; 

Trk::ex_compile("box_load_event","Load box id",\@code) ;
@code=qw( evt.d >r nil evt.a dp.carton.set 
	  evt.c dp.counter.get  evt.c  r> evt.a 305 5 mb_npost ) ;

Trk::ex_compile("box_clear_event","Clear box id",\@code) ;

# b = offset c = next d = tach
@code=qw(
         edge.carton nil = not ifbreak 
	 rp.value.get b.set c.set d.set
         d
	 edge.value if &box_load_event else &box_clear_event then
	 b
	 c edge.dp d edge.carton  ev_insert
	 ) ;
Trk::ex_compile("send.bx","Send box id downstream",\@code) ;

sub send_bx_id {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset=@_[3] ;

    my @code=($tach,$next,$offset, "send.bx") ;
    my $nstate = $name . "." . $next . ".send.bx" ;
  Trk::ex_compile($nstate,"Send box id to downstream dp: " . $next,\@code) ;
    my $foo = pop(@code) ;
    
  trakbase::leading($name,$nstate,"Send box id") ;
  trakbase::trailing($name,$nstate,"Send box id") ;
}
# a = window b = offset c = next d = tach
@code=qw( edge.carton nil = not ifbreak 
          d.set c.set rp.value.get b.set rp.value.get a.set
	  d &box_load_event b   
	  c edge.dp d  edge.carton  ev_insert
	  d &box_clear_event b a +
	  c edge.dp d edge.carton  ev_insert
	  ) ;
Trk::ex_compile("send.bx.edge","Send box on one edge",\@code) ;

sub send_bx_id_ld {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset=@_[3] ;
    my $window = @_[4] ;

#    print ("start send_bx_id_ld $window $offset $next $tach\n") ;
    my @code=($window,$offset,$next,$tach, "send.bx.edge") ;
#    print (" .. code built ..") ;
    my $nstate = $name . "." . $next . '.snd.bx.ld' ;
#   print ('compile ' . $nstate) ;
  Trk::ex_compile($nstate,"Send box id to downstream dp: " . $next,\@code) ;
#   print ("  done\n") ;
    my $foo = pop(@code) ;
    
  trakbase::leading($name,$nstate,"Send box id") ;

}
sub send_bx_id_trl {
    my $name = @_[0] ;
    my $next = @_[1] ;
    my $tach = @_[2] ;
    my $offset = @_[3] ;
    my $window = @_[4] ;

    my @code=($window,$offset,$next,$tach, "send.bx.edge") ;
    my $nstate = $name . $next. ".snd.bx.ld" ;
  Trk::ex_compile($nstate,"Send box id to downstream dp: " . $next,\@code) ;
    my $foo = pop(@code) ;
    
  trakbase::trailing($name,$nstate,"Send box id") ;
}

@code=qw( c.set
	  c dp.counter.get  c edge.carton edge.value edge.dp 307 6 mb_npost
	  ) ;
Trk::ex_compile("report.edge","Report data on edge",\@code) ;
sub report_ld {
    my $name = @_[0] ;
    my $tach = @_[1] ;
    
    my @code=($tach, "report.edge") ;
    my $nstate = $name . ".rld." . $tach; 
  Trk::ex_compile($nstate,"Report leading edge",\@code) ;
    my $foo = pop(@code) ;
  trakbase::leading($name,$nstate,"Report on leading edge") ;
}
sub report_trl {
    my $name = @_[0] ;
    my $tach = @_[1] ;
    
    my @code=($tach, "report.edge") ;
    my $nstate = $name . ".rtrl." . $tach; 
  Trk::ex_compile($nstate,"Report leading edge",\@code) ;
    my $foo = pop(@code) ;
  trakbase::trailing($name,$nstate,"Report on leading edge") ;
}


#
# add a lane to a decision point. 
# basically writes a short peice of code to call a given function passing
# string . lane to it to cause a sort.
# to be defined

sub addlane {
    my $decide_dp = @_[0] ;
    my $which = @_[1] ;
    my $lane_name = @_[2] ;
    my $divert_code = @_[3] ;
   
   
    my $a_h = trakbase::dhandle($decide_dp) ;
    
    my @code=( "edge.carton", "bx.data.get", 
	       $which, "=","if", $lane_name.$which ,
	      $divert_code,"then") ;
    
  Trk::ex_compile("lane.tst.".$which,"Lane Test:".$which,\@code) ;
    my $foo = pop(@code) ;
  trakbase::leading($decide_dp,"lane.tst.".$which,"Test for Lane " . $which) ;
  
}


# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

sort - Perl extension for Trk. Used to build sorters.

=head1 SYNOPSIS


  use sort;
 sort::make_bx_ld($dp_name,$tach_name) ;
 sort::make_bx_trl($dp_name,$tach_name) ;
 sort::measure_bx($dp_name,$tach_name,$register) ;
 sort::move_bx($dp_name,$tach_name) ;
 sort::shadow_bx($dp_name,$target_dp,$tach_dp,$offset) ;
 sort::shadow_bx_ld($dp_name,$target_dp,$tach_dp,$offset,$window) ;
 sort::shadow_bx_trl($dp_name,$target_dp,$tach_dp,$offset,$window) ;
 sort::send_bx_id($dp_name,$target_dp,$tach_dp,$offset) ;
 sort::send_bx_id_ld($dp_name,$target_dp,$tach_dp,$offset,$window) ;
 sort::send_bx_id_trl($dp_name,$target_dp,$tach_dp,$offset,$window) ;
 sort::report_trl($dp_name,$tach_dp) ;
 sort::addlane ($decide_dp,$which_lane,$lane_name,$sort_word) ;


=head1 DESCRIPTION

This is the second revision of PERL extensions to Trak assisting in the
control of sorters.

  make_bx --- creates a box and loads it into the dp indicated. Tach is required to load
     into the box on creation.
  measure_bx --- on leading/trailing this stores in the indicated register a tach value on the
          lead edge and loads the box with the difference on the trail.
  shadow_bx --- "animates" a virtual dp by turning it on and off mirroring the box (just downstream)
  send_bx_id --- loads a downstream dp with the box id (and loads null on trailing edge) so that
    a box and resultant data can be passed down the conveyor.
  report_trl --- report the turning on and off of a particular dp with a tach counter. Messages are 
    sent up.
  addlane --- attaches decision logic to a lane. This gets passed the decision
     dp, a lane number. The lane number is compared with the data in the box
     loaded at the dp. The remaining parameters are used to build the divert 
     parameter passed to a sortation word (the last parameter).

=head2 EXPORT

None by default.

=head1 AUTHOR

Mark Olson Numina Systems Corporation, marko@numinasys.com

=head1 SEE ALSO

perl(1).

=cut
