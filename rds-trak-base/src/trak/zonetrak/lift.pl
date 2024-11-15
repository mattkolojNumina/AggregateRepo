#!/bin/perl -I.
#

require Trk;
require trakbase;
require traksort;
require dp;
require timer;

my @code;

trakbase::message( 'lift utility functions' );


# ===========================================================================

# --- lift registers ---
#  1 - lift motor output dp
#  2 - up prox input dp
#  3 - down prox input dp
#  4 - up indicator dp (virtual)
#  5 - down indicator dp (virtual)
#  6 - run-up rp (default: lift_runup)
#  7 - run-down rp (default: lift_rundown)
#  9 - timer

@code=qw( 1 swap dp.register.get );
Trk::ex_compile( 'lift_motor@', 'fetch lift motor dp', \@code );
@code=qw( 2 swap dp.register.get );
Trk::ex_compile( 'lift_up_prox@', 'fetch lift up prox dp', \@code );
@code=qw( 3 swap dp.register.get );
Trk::ex_compile( 'lift_dn_prox@', 'fetch lift down prox dp', \@code );
@code=qw( 4 swap dp.register.get );
Trk::ex_compile( 'lift_up@', 'fetch lift up indicator dp', \@code );
@code=qw( 5 swap dp.register.get );
Trk::ex_compile( 'lift_dn@', 'fetch lift down indicator dp', \@code );
@code=qw( 6 swap dp.register.get rp.value.get );
Trk::ex_compile( 'lift_runup@', 'fetch lift run-up rp value', \@code );
@code=qw( 7 swap dp.register.get rp.value.get );
Trk::ex_compile( 'lift_rundown@', 'fetch lift run-down rp value', \@code );
@code=qw( 9 swap dp.register.get );
Trk::ex_compile( 'lift_timer@', 'fetch lift timer dp', \@code );

# --- prox registers ---
#  2 - lift dp
@code=qw( 2 swap dp.register.get );
Trk::ex_compile( 'prox_lift@', 'fetch prox lift dp', \@code );


rp::const('lift_runup', 1,'lift run-up cycles');
rp::const('lift_rundown', 1,'lift run-down cycles');
rp::const('lift_guard', 300,'lift guard cycles');


# lift_halt  ( dp -- )
#    stop all movement and put the lift in an indeterminate state
#
# clear the up/down indicators
# stop the motor
#
@code=qw(
   >r

   r@ lift_up@ off
   r@ lift_dn@ off
   r@ lift_motor@ off

   r> drop
);
Trk::ex_compile('lift_halt','halt the lift',\@code);


# lift_guard_evt  [event]
#    scheduled test for sensor activation; halt if no sensor change detected
#
#    evt.a - lift dp
#    evt.b - prox sensor (up or down)
#    evt.c - prox counter when evt scheduled
#
@code=qw(
   evt.b dp.counter.get  evt.c  = if
      evt.a lift_halt
   then
);
Trk::ex_compile('lift_guard_evt','test for movement',\@code);


# lift_raise  [edge]
#    run the motor to raise the lift
#
# turn the lift on
# clear the up/down indicators
# run the motor
# schedule the guard timer with the current up-prox count
#
@code=qw(
   edge.dp >r

   r@ on
   r@ lift_up@ off
   r@ lift_dn@ off
   r@ lift_motor@ on

   r@ lift_timer@  &lift_guard_evt  lift_guard rp.value.get
      r@  r@ lift_up_prox@  r@ lift_up_prox@ dp.counter.get  0 ev_insert

   r> drop
);
Trk::ex_compile('lift_raise','raise the lift',\@code);


# lift_lower  [edge]
#    run the motor to lower the lift
#
# turn the lift off
# clear the up/down indicators
# run the motor
# schedule the guard timer with the current down-prox count
#
@code=qw(
   edge.dp >r

   r@ off
   r@ lift_up@ off
   r@ lift_dn@ off
   r@ lift_motor@ on

   r@ lift_timer@  &lift_guard_evt  lift_guard rp.value.get
      r@  r@ lift_dn_prox@  r@ lift_dn_prox@ dp.counter.get  0 ev_insert

   r> drop
);
Trk::ex_compile('lift_lower','lower the lift',\@code);


# lift_up_evt  [event]
#    delayed indicator for up state
#
#    evt.a - lift dp
#
@code=qw(
   evt.a tst if
      evt.a lift_up@ on
      evt.a lift_dn@ off
      evt.a lift_motor@ off
   then
);
Trk::ex_compile('lift_up_evt','event to indicate up state',\@code);


# lift_dn_evt  [event]
#    delayed indicator for down state
#
#    evt.a - lift dp
#
@code=qw(
   evt.a tst not if
      evt.a lift_up@ off
      evt.a lift_dn@ on
      evt.a lift_motor@ off
   then
);
Trk::ex_compile('lift_dn_evt','event to indicate down state',\@code);


# on_up_prox  [edge]
#
# schedule the up event
#
@code=qw(
   edge.dp prox_lift@ >r

   r@ lift_timer@  &lift_up_evt  r@ lift_runup@
      r@  0 0 0 ev_insert

   r> drop
);
Trk::ex_compile('on_up_prox','up prox leading edge',\@code);


# on_dn_prox  [edge]
#
# schedule the down event
#
@code=qw(
   edge.dp prox_lift@ >r

   r@ lift_timer@  &lift_dn_evt  r@ lift_rundown@
      r@  0 0 0 ev_insert

   r> drop
);
Trk::ex_compile('on_dn_prox','down prox leading edge',\@code);


sub lift_def {
   my ($name, $motor, $up_prox, $dn_prox, $desc, $data) = @_;
   my $val;
   my $rp;

   my $dp = dp::virtual($name,$desc);
   my $up_prox_dp = dp::handle($up_prox);
   my $dn_prox_dp = dp::handle($dn_prox);

   Trk::dp_registerset($dp,1,dp::handle($motor));
   Trk::dp_registerset($dp,2,$up_prox_dp);
   Trk::dp_registerset($dp,3,$dn_prox_dp);

   Trk::dp_registerset($up_prox_dp,2,$dp);
   Trk::dp_registerset($dn_prox_dp,2,$dp);

   my $up = $data->{up};
   if ($up eq '') {
      $up = $name . '_up';
      dp::virtual($up,$desc.' up indicator');
      traksort::report_ld($up,'tm_1ms');
   }
   Trk::dp_registerset($dp,4,dp::handle($up));

   my $dn = $data->{dn};
   if ($dn eq '') {
      $dn = $name . '_dn';
      dp::virtual($dn,$desc.' down indicator');
      traksort::report_ld($dn,'tm_1ms');
   }
   Trk::dp_registerset($dp,5,dp::handle($dn));

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const($name.'_runup',$val,$name.' runup time (cycles)');
   } else { $rp = rp::handle( 'lift_runup' ); }
   Trk::dp_registerset($dp,6,$rp);

   if (($val = $data->{rundown}) ne '') {
      $rp = rp::const($name.'_rundown',$val,$name.' rundown time (cycles)');
   } else { $rp = rp::handle( 'lift_rundown' ); }
   Trk::dp_registerset($dp,7,$rp);

   my $timer = 'tm_10ms';
   if (($val = $data->{timer}) ne '') { $timer = $val; }
   Trk::dp_registerset($dp,9,dp::handle($timer));


   trakbase::leading( $name,'lift_raise','raise '.$desc);
   trakbase::trailing($name,'lift_lower','lower '.$desc);

   trakbase::leading( $up_prox,'on_up_prox','on up prox '.$up_prox);
   trakbase::leading( $dn_prox,'on_dn_prox','on down prox '.$dn_prox);
}

