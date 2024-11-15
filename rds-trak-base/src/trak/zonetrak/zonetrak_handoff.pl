#!/bin/perl -I.
#
# zonetrak_handoff : handoff zones to/from ZoneTRAK
#
# addHandoffIn( name, {data} )
# addHandoffOut( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode  - create new box if anonymous box present
#    stop_mode    - stop each box until permissive fired [in]
#    idlerun_mode - run the motor in the idle state [in]
#
# accepts the following control elements:
#    desc       - description (default: 'handoff (in/out) zone <name>')
#    next       - downstream zone or control dp [in]
#    eye        - request/ready input
#    motor      - release/sending output
#    run        - area run control (default: always run)
#    fault      - area fault control (default: never fault)
#    reset      - area reset control (clears local zone fault)
#    first      - lower bound of box range (if create_mode, default: 1)
#    last       - upper bound of box range (if create_mode, default: 999)
#    output     - arbitrary output dp (case stop, beacon, etc.)
#    out_states - bitmask of states in which the output is on
#    timer      - state machine timer (default: 10ms)
#
# accepts the following zone-specific timers:
#    runin   - run box into zone (if ready signal not defined) (cycles) [out]
#    runover - run box past ready signal during fill (cycles) [out]
#    runout  - run box past request signal during drain (cycles) [in]
#    runup   - run zone during initialization (cycles)
#    max     - max allowed time in state, e.g., detect missing (cycles) [out]
#    clear   - max time with eye clear, e.g., detect removed (cycles) [in]
#    dead    - dead time before detecting eye clear during release (cycles)
#
# creates the following dp's for external control:
#    <name>_fault - declare the zone faulted
#    <name>_debug - disable all state-machine controls
#    <name>_hold  - hold all boxes
#    <name>_go    - single-box release (if stop_mode present)
#


require Trk;
require dp;
require rp;
require trakbase;

require "zonetrak_common.pl";
require "zonetrak_zone.pl";


my @code;

# global timing parameters
rp::const( 'handoff_runin',    50, 'handoff run-in cycles' );
rp::const( 'handoff_runover',   0, 'handoff run-over cycles' );
rp::const( 'handoff_runout',    0, 'handoff run-out cycles' );
rp::const( 'handoff_runup',     0, 'handoff run-up cycles' );
rp::const( 'handoff_max',     300, 'handoff maximum time (cycles)' );
rp::const( 'handoff_clear',   200, 'handoff clear time (cycles)' );
rp::const( 'handoff_dead',      0, 'handoff dead time (cycles)' );


# _handoff_in_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop + tick
#       if eye blocked
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ off
   MODE_IDLERUN r@ _zone_mode? if
      REG_MOTOR r@ _zone_dp_on
   else
      REG_MOTOR r@ _zone_dp_off
   then

   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_EYE r@ _zone_dp? if
      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         BOX_ANON r@ _zone_box!
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_handoff_in_state_idle', 'handoff (in) state idle', \@code );

# _zone_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motor stop + tick
#       if zone running and next is available
#          pass box to next
#          goto DRAIN_X
#       if eye unblocked
#          if elapsed > clear
#             goto INIT
#       else
#          clear timer
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off
   r@ _zone_tick

   r@ _zone_run?
   r@ _zone_next_avail?                                   and
   REG_HOLD r@ _zone_dp? not                              and
   MODE_STOP r@ _zone_mode? not  REG_GO r@ _zone_dp?  or  and if
      r@ _zone_box_pass
      STATE_DRAIN_X r@ _zone_state!
   then

   REG_EYE r@ _zone_dp_not? if
      r@ _zone_elapsed@  REG_TM_CLEAR r@ _zone_rp@  > if
         STATE_INIT r@ _zone_state!
      then
   else
      r@ _zone_timer_clr
   then

   r> drop
);
Trk::ex_compile( '_handoff_in_state_full', 'handoff (in) state full', \@code );

# _zone_state_drain_x  ( dp -- )
#    transport the box out of the zone while the request is active
#
#       zone on
#       motor run with next
#       if eye not blocked
#          goto DRAIN
#
@code=qw(
   >r

   r@ on
   REG_GO r@ _zone_dp_off

   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_box@  BOX_NONE =
   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =  and if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then

   REG_EYE r@ _zone_dp? not if
      r@ _zone_elapsed@  REG_TM_DEAD r@ _zone_rp@  > if
         STATE_DRAIN r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_handoff_in_state_drain_x',
      'handoff (in) state drain (blocked)', \@code );

# _zone_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on
#       motor run with next
#       if elapsed > runout
#          goto IDLE
#
@code=qw(
   >r

   r@ on
   REG_GO r@ _zone_dp_off

   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_box@  BOX_NONE =
   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =  and if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then

   r@ _zone_elapsed@  REG_RUNOUT r@ _zone_rp@  > if
      STATE_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_handoff_in_state_drain',
      'handoff (in) state drain (unblocked)', \@code );

# _handoff_in_machine  ( dp -- )
#    execute handoff (in) zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _zone_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _zone_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _handoff_in_state_idle    then
      r@ _zone_state@  STATE_FULL    = if r@ _handoff_in_state_full    then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _handoff_in_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _handoff_in_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_handoff_in_machine', 'handoff (in) state machine', \@code );


# create handoff (in) zone
sub addHandoffIn {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'handoff (in) zone ' . $name, $data );

   # set timers
   my $rp;

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'handoff_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'handoff_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );
   } else { $rp = rp::handle( 'handoff_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'handoff_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   # run the state machine
   my @stub = ( $name, '_handoff_in_machine' );
   Trk::ex_compile( $name . '_machine', $name . ' state machine', \@stub );

   my $timer = spread_timer();
   if (($val = $data->{timer}) ne '') { $timer = $val; }
   trakbase::leading(  $timer, $name . '_machine',
         'run ' . $name . ' state machine' );
   trakbase::trailing( $timer, $name . '_machine',
         'run ' . $name . ' state machine' );

   my $tmp = pop( @stub );
}



# _handoff_out_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       motor run + tick
#       if elapsed > runup
#          goto IDLE
#       if eye [ready] off
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _zone_elapsed@  REG_RUNUP r@ _zone_rp@  > if
      STATE_IDLE r@ _zone_state!
   then

   REG_EYE r@ _zone_dp_not? if
      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         BOX_ANON r@ _zone_box!
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_handoff_out_state_runup', 'handoff (out) state run-up',
      \@code );

# _handoff_out_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop + tick
#       if box not NONE
#          goto FILL
#       if eye [ready] off
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ off
   REG_MOTOR r@ _zone_dp_off
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _zone_box@ BOX_NONE = not if
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_FILL r@ _zone_state!
   then

   REG_EYE r@ _zone_dp_not? if
      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         BOX_ANON r@ _zone_box!
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile('_handoff_out_state_idle','handoff (out) state idle',\@code);

# _handoff_out_state_fill  ( dp -- )
#    bring a box into the zone
#
#       zone on
#       motor run + tick
#       if eye defined
#          if eye [ready] off
#             goto FILL_X
#          else if elapsed > max
#             goto INIT
#       else if elapsed > runin
#          goto FULL
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   REG_EYE r@ dp.register.get  0 > if
      REG_EYE r@ _zone_dp_not? if
         STATE_FILL_X r@ _zone_state!
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            STATE_INIT r@ _zone_state!
         then
      then
   else
      r@ _zone_elapsed@  REG_RUNIN r@ _zone_rp@  > if
         STATE_FULL r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_handoff_out_state_fill',
      'handoff (out) state fill (unblocked)', \@code );

# _handoff_out_state_fill_x  ( dp -- )
#    bring a box into the zone after the ready signal drops
#
#       zone on
#       motor run + tick
#       if elapsed > runover
#          goto FULL
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_RUNOVER r@ _zone_rp@  > if
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_handoff_out_state_fill_x',
      'handoff (out) state fill (blocked)', \@code );

# _handoff_out_state_full  ( dp -- )
#    clear box and hold until ready signal activates
#
#       zone on
#       motor stop
#       clear box
#       if eye [ready] on
#          goto IDLE
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off

   BOX_NONE r@ _zone_box!

   REG_EYE r@ _zone_dp? if
      STATE_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile('_handoff_out_state_full','handoff (out) state full',\@code);

# _handoff_out_machine  ( dp -- )
#    execute handoff (out) zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _zone_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _handoff_out_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _handoff_out_state_idle    then
      r@ _zone_state@  STATE_FILL    = if r@ _handoff_out_state_fill    then
      r@ _zone_state@  STATE_FILL_X  = if r@ _handoff_out_state_fill_x  then
      r@ _zone_state@  STATE_FULL    = if r@ _handoff_out_state_full    then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile('_handoff_out_machine','handoff (out) state machine',\@code);


# create handoff (out) zone
sub addHandoffOut {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'handoff (out) zone ' . $name, $data );

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'handoff_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'handoff_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'handoff_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'handoff_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   # run the state machine
   my @stub = ( $name, '_handoff_out_machine' );
   Trk::ex_compile( $name . '_machine', $name . ' state machine', \@stub );

   my $timer = spread_timer();
   if (($val = $data->{timer}) ne '') { $timer = $val; }
   trakbase::leading(  $timer, $name . '_machine',
         'run ' . $name . ' state machine' );
   trakbase::trailing( $timer, $name . '_machine',
         'run ' . $name . ' state machine' );

   my $tmp = pop( @stub );
}


# return a true value to indicate successful initialization
1;
