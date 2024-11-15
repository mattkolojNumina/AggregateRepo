package merge;

use strict;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter;
require AutoLoader;

@ISA = qw(Exporter AutoLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	 link,addlane
);
$VERSION = '0.01';


# Preloaded methods go here.

# Autoload methods go after =cut, and are processed by the autosplit program.

require Trk ;
require trakbase ;
require dp ;
my @code ;

# 



@code=qw( 1 swap dp.register.get ) ;
Trk::ex_compile("merge-state@","Fetch Merge State",\@code) ;
@code=qw( 1 swap dp.register.set ) ;
Trk::ex_compile("merge-state!","Store merge state",\@code) ;

@code=qw( 2 swap dp.register.get ) ;
Trk::ex_compile("merge-first@","Fetch first merge lane",\@code) ;

@code=qw( 3 swap dp.register.get tst ) ;
Trk::ex_compile("merge-downstream@","Test Downstream",\@code) ;

@code=qw( 4 swap dp.register.get ) ;
Trk::ex_compile("merge-current@","Get current lane dp",\@code) ;
@code=qw( 5 swap dp.register.get rp.value.get ) ;
Trk::ex_compile("merge-preset@","Get transtion time preset",\@code) ;
@code=qw( 6 swap dp.register.get ) ;
Trk::ex_compile("merge-tach@","Get transtion rp",\@code) ;


@code=qw( 0 swap dp.register.get) ;
Trk::ex_compile("lane-go@","Fetch lane solenoid",\@code) ;
@code=qw( 1 swap dp.register.get) ;
Trk::ex_compile("lane-state@","Fetch lane state",\@code) ;
@code=qw( 1 swap dp.register.set ) ;
Trk::ex_compile("lane-state!","Store Lane state",\@code) ;

@code=qw( 2 swap dp.register.get ) ;
Trk::ex_compile("lane-merge@","Fetch lane merge dp",\@code) ;

@code=qw( 3 swap dp.register.get ) ;
Trk::ex_compile("lane-next@","Fetch next lane",\@code) ;

@code=qw( 4 swap dp.register.get rp.value.get ) ;
Trk::ex_compile("lane-no-preset@","Fetch no-carton preset",\@code) ;
@code=qw( 5 swap dp.register.get rp.value.get ) ;
Trk::ex_compile("lane-max-preset@","Fetch max run-time preset",\@code) ;

@code=qw( 6 swap dp.register.get) ;
Trk::ex_compile("lane-tach@","Fetch lane tach dp",\@code) ;


# Lanes are started and stopped by the merge.

# lane states:
# 0 - stopped
# 1 - running
# 2 - halt on next pkg.

@code=(nop) ; 
Trk::ex_compile("lane-switch","Switch to the other lane",\@code) ;
@code=qw(
	 a tst 
	 if
	 b a dp.counter.get = 
	   if
           a lane-switch
 	   else
	   a lane-tach@ &lane-no-event a lane-no-preset@
	   a a lane-input@ dp.counter.get 0 0 ev_insert
	   then
	 then
	 ) ;
Trk::ex_compile("lane-no-event","Event triggered to test idle lane",\@code) ;

@code=qw(
	 a tst if a lane-switch then
	 ) ;
Trk::ex_compile("lane-max-event","Event triggered to test idle lane",\@code) ;

@code=qw(
	 >r
	 r@ lane-go@ off
	 r> off
	 ) ;
Trk::ex_compile("lane-stop","Stop a lane",\@code) ;


@code=qw(
	 >r 
	 r@ lane-state@ 3 = 
	 if 
	  r@ lane-stop
  	  r> lane-switch
	 else
	  r@ on
	  r@ lane-go@ on
	  1 r@ lane-state!
	  r@ lane-tach@ &lane-no-event r@ lane-no-preset@
	  r@ r@ lane-input@ dp.counter.get 0 0 ev_insert
	  r@ lane-tach@ &lane-max-event r@ lane-max-preset@
	  r> 0 0 0 ev_insert
	 then
	 ) ;
Trk::ex_compile("lane-start","Start the lane",\@code) ;

@code=("nop") ;
Trk::ex_compile("merge-next","Switch to next merge lane",\@code) ;


@code=qw(
	 >r
	 r@ lane-merge@ merge-next 
	 2 r> lane-state!

	 ) ;
Trk::ex_compile("lane-switch","Switch to next lane",\@code) ;


@code=qw(
	 partner@ lane-state@ 2 = 
	 if 
	 partner@ lane-go@ off
	 0 partner@ lane-state!
	 partner@ off
	 then
	 ) ;
Trk::ex_comile("lane-ld-edge","Lane handler on leading edge",\@code) ;


@code=qw(
	 a merge-state!
	 1 = if a merge-current@ lane-start then
	 ) ;
Trk::ex_compile("merge-event","Event triggered for merge",\@code) ;


@code=qw(
	 dup merge--zone@ tst 
	 if
	  >r
	  1 r@ merge-state!
	  r@ merge-tach@  &merge-event  r@ mrege-preset@ 
	  r@ 0 0 0 ev_insert
	  r@ merge-first@ r@ merge-current!
	  r> on
	 then
	 ) ;
Trk::ex_compile("merge-start","Start up merge",\@code) ;
@code=qw(
	 dup 0 swap merge-state!
	 dup merge-current@ lane-stop
	 off
	 ) ;
Trk::ex_compile("merge-stop","Start up merge",\@code) ;
@code=qw(
	 >r
	 r@ merge-current@ lane-stop
	 1 r@ merge-state!
	 r@ merge-tach@  &merge-event  r@ mrege-preset@ 
	 r@ 0 0 0 ev_insert
	 r@ merge-current@ lane-next@
	 dup nil = if drop r@ merge-first@ then
	 r> merge-current!
	 ) ;
Trk::ex_compile("merge-next","Start up merge",\@code) ;



# setup the merge

sub link {
    my $name = @_[0] ;
    my $zone = @_[1] ;
    my $preset = @_[2] ;
    my $tach = @_[3] ;
    my $desc = @_[4] ;
    
    my $h = Trk::dp_new($name,"virtual","blank",$desc) ;
    my $z_h = trakbase::dhandle($zone) ;
    my $p_h = rp::handle($preset) ;
    my $t_h = trakbase::dhandle($tach) ;

  Trk::dp_registerset($h,1,0) ;
  Trk::dp_registerset($h,2,$z_h) ;
  Trk::dp_registerset($h,3,-1) ;
  Trk::dp_registerset($h,4,-1) ;
  Trk::dp_registerset($h,5,$p_h) ;
  Trk::dp_registerset($h,6,$t_h) ;

    
}

sub addlane {
    my $name = @_[0] ;
    my $go = @_[1] ;
    my $eye = @_[2] ;
    my $merge = @_[3] ;
    my $next = @_[4] ;
    my $none = @_[5] ;
    my $max = @_[6] ;
    my $tach = @_[7] ;
    my $desc = @_[8] ;
    
    my $h = Trk::dp_new($name,"virtual","blank",$desc) ;
    my $g_h = trakbase::dhandle($go) ;
    my $m_h = trakbase::dhandle($merge) ;
    my $n_h = Trk::dp_handle($next) ;
    if ($n_h == -1) {
	$n_h = Trk::dp_new($next,"virtual","blank","") ;
    }

    my $e_h = trakbase::dhandle($eye) ;
    my $no_h = rp::handle($none) ;
    my $max_h = rp::handle($max) ;
    my $t_h = trakbase::dhandle($tach) ;
    
  Trk::dp_registerset($h,0,$g_h) ;
  Trk::dp_registerset($h,1,0) ;
  Trk::dp_registerset($h,2,$m_h) ;
  Trk::dp_registerset($h,3,$n_h) ;
  Trk::dp_registerset($h,4,$no_h) ;
  Trk::dp_registerset($h,5,$max_h) ;
  Trk::dp_registerset($h,6,$t_h) ;
  Trk::dp_registerset($h,7,$e_h) ;

    my $m_first = Trk::dp_registerget($m_h,2) ;
    if ($m_first == -1) {
      Trk::dp_registerset($m_h,2,$h) ;
    }
    
  Trk::dp_registerset($e_h,2,$h) ;
  trakbase::leading($eye,"lane-ld-edge",$eye . " Leading Edge Handler");
}

1;
__END__
# Below is the stub of documentation for your module. You better edit it!

=head1 NAME

merge - Perl extension for merge definition and control in Trak.

=head1 SYNOPSIS

  use merge;
 merge::link ;
 merge::addlane ;

=head1 DESCRIPTION

Stub documentation for merge was created by h2xs. It looks like the
author of the extension was negligent enough to leave the stub
unedited.

Blah blah blah.

=head1 AUTHOR

A. U. Thor, a.u.thor@a.galaxy.far.far.away

=head1 SEE ALSO

perl(1).

=cut
