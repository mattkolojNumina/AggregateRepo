#!/bin/perl -I.
#
# zonetrak_belt : basic ZoneTRAK zone to mimic belt transport
#
# addBelt( name, {data} )
#
# allows the following modes (set non-empty to activate):
#
# accepts the following control elements:
#    desc       - description (default: 'belt zone <name>')
#    next       - downstream zone or control dp
#    eye        - presence photoeye
#    motor      - transport motor
#    run        - area run control (default: always run)
#    fault      - area fault control (default: never fault)
#    reset      - area reset control (clears local zone fault)
#    output     - arbitrary output dp (case stop, beacon, etc.)
#    out_states - bitmask of states in which the output is on
#    timer      - state machine timer (default: 10ms)
#
# accepts the following zone-specific timers:
#    runin   - run box into zone (if eye not defined) (cycles)
#    runover - run lead-edge of box past presence eye during fill (cycles)
#    runout  - run trail-edge of box past presence eye during drain (cycles)
#    runup   - run zone during initialization (cycles)
#    min     - min allowed gap time during drain (cycles)
#    max     - max allowed time in state, e.g., detect missing (cycles)
#    clear   - max time with eye clear, e.g., detect removed (cycles)
#    dead    - dead time before detecting eye clear during release (cycles)
#    jam     - jam time (cycles)
#
# creates the following dp's for external control:
#    <name>_fault - declare the zone faulted
#    <name>_debug - disable all state-machine controls
#    <name>_hold  - hold all boxes
#


require Trk;
require dp;
require rp;
require trakbase;

require "zonetrak_common.pl";


my @code;

# global timing parameters
rp::const( 'belt_runin',    50, 'belt run-in cycles' );
rp::const( 'belt_runover',   0, 'belt run-over cycles' );
rp::const( 'belt_runout',   10, 'belt run-out cycles' );
rp::const( 'belt_runup',   100, 'belt run-up cycles' );
rp::const( 'belt_min',      10, 'belt minimum time (cycles)' );
rp::const( 'belt_max',     300, 'belt maximum time (cycles)' );
rp::const( 'belt_clear',   200, 'belt clear time (cycles)' );
rp::const( 'belt_dead',      0, 'belt dead time (cycles)' );
rp::const( 'belt_jam',     300, 'belt jam time (cycles)' );


# _belt_cnt@  ( dp -- )
#    get the box counter
@code=qw(
   REG_CONTROL swap dp.register.get
);
Trk::ex_compile( '_belt_cnt@', 'belt zone get box count', \@code);

# _belt_cnt!  ( count dp -- )
#    set the box counter
@code=qw(
   >r

   r@ 0 > if
      REG_CONTROL r@ dp.register.set
   else
      drop
   then

   r> drop
);
Trk::ex_compile( '_belt_cnt!', 'belt zone set box count', \@code);

# _belt_cnt++  ( dp -- )
#    increment the box counter
@code=qw(
   >r

   r@ 0 > if
      r@ _belt_cnt@
      1 +
      r@ _belt_cnt!
   then

   r> drop
);
Trk::ex_compile( '_belt_cnt++', 'belt zone increment box count', \@code);

# _belt_cnt--  ( dp -- )
#    decrement the box counter
@code=qw(
   >r

   r@ 0 > if
      r@ _belt_cnt@
      dup 0 > if
         1 -
         r@ _belt_cnt!
      else
         drop
      then
   then

   r> drop
);
Trk::ex_compile( '_belt_cnt--', 'belt zone decrement box count', \@code);

# _belt_box_load  ( dp -- )
#    load a box: if the box register is populated, increment the box
#    counter and clear the box register
@code=qw(
   >r

   r@ _zone_box@ BOX_NONE = not if
      r@ _belt_cnt++
      BOX_NONE r@ _zone_box!
   then

   r> drop
);
Trk::ex_compile( '_belt_box_load', 'belt zone load box', \@code);

# _belt_box_pass  ( dp -- )
#    pass an anonymous box to the next zone and decrement the box
#    counter; if create mode, create a new box number to pass
@code=qw(
   >r

   MODE_CREATE r@ _zone_mode? if
      r@ _zone_box@
         r@ _zone_box_new
         r@ _zone_box@  REG_NEXT r@ dp.register.get  _zone_box!
      r@ _zone_box!
   else
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then
   r@ _belt_cnt--

   r> drop
);
Trk::ex_compile( '_belt_box_pass', 'belt zone pass box to next', \@code);


# _belt_state_init  ( dp -- )
#    initialize the zone state machine
#
#       zone on
#       motor stop
#       zero box counter
#       if zone running
#          goto RUNUP
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off

   BOX_ZERO r@ _belt_cnt!

   r@ _zone_run? if
      STATE_RUNUP r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_belt_state_init', 'belt zone state init', \@code );


# _belt_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone off
#       motor run + tick
#       load box
#       if box counter > 0
#          goto FILL
#       if elapsed > runup
#          goto IDLE
#       if eye blocked
#          increment box counter
#          goto FILL_X
#
@code=qw(
   >r

   r@ off
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _belt_box_load

   r@ _belt_cnt@ 0 >  if
      STATE_FILL r@ _zone_state!
   then

   r@ _zone_elapsed@  REG_RUNUP r@ _zone_rp@  > if
      STATE_IDLE r@ _zone_state!
   then

   REG_EYE r@ _zone_dp? if
      r@ _belt_cnt++
      STATE_FILL_X r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_belt_state_runup', 'belt zone state run-up', \@code );

# _belt_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop + tick
#       load box
#       if box counter > 0
#          goto FILL
#       if eye blocked
#          increment box counter
#          goto FULL
#
@code=qw(
   >r

   r@ off
   REG_MOTOR r@ _zone_dp_off
   r@ _zone_tick

   r@ _belt_box_load

   r@ _zone_run?  r@ _belt_cnt@ 0 >  and if
      STATE_FILL r@ _zone_state!
   then

   REG_EYE r@ _zone_dp? if
      r@ _belt_cnt++
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_belt_state_idle', 'belt zone state idle', \@code );

# _belt_state_fill  ( dp -- )
#    bring a box into the zone
#
#       zone off
#       motor run + tick
#       load box and zero timer if new box inducted
#       if eye defined
#          if eye blocked
#             goto FILL_X
#          else if elapsed > max
#             zero box counter
#             goto IDLE
#       else if elapsed > runin
#          goto FULL
#
@code=qw(
   >r

   r@ off
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_box@ BOX_NONE = not if
      r@ _belt_cnt++
      BOX_NONE r@ _zone_box!
      r@ _zone_timer_clr
   then

   REG_EYE r@ dp.register.get  0 > if
      REG_EYE r@ _zone_dp? if
         STATE_FILL_X r@ _zone_state!
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            BOX_ZERO r@ _belt_cnt!
            STATE_IDLE r@ _zone_state!
         then
      then
   else
      r@ _zone_elapsed@  REG_RUNIN r@ _zone_rp@  > if
         STATE_FULL r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_belt_state_fill', 'belt zone state fill (unblocked)',
      \@code );

# _belt_state_fill_x  ( dp -- )
#    bring a box into the zone while the eye is blocked
#
#       zone off
#       motor run + tick
#       load box
#       if elapsed > runover
#          goto FULL
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _belt_box_load

   r@ _zone_elapsed@  REG_RUNOVER r@ _zone_rp@  > if
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_belt_state_fill_x', 'belt zone state fill (blocked)',
      \@code );

# _belt_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motor stop + tick
#       if zone running and next is available
#          pass to next
#          goto DRAIN_X
#       if eye unblocked
#          if elapsed > clear
#             decrement box counter
#             goto RUNUP
#       else
#          clear timer
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off
   r@ _zone_tick

   r@ _zone_run?
   r@ _zone_next_avail?       and
   REG_HOLD r@ _zone_dp? not  and if
      r@ _belt_box_pass
      STATE_DRAIN_X r@ _zone_state!
   then

   REG_EYE r@ _zone_dp_not? if
      r@ _zone_elapsed@  REG_TM_CLEAR r@ _zone_rp@  > if
         r@ _belt_cnt--
         STATE_RUNUP r@ _zone_state!
      then
   else
      r@ _zone_timer_clr
   then

   r> drop
);
Trk::ex_compile( '_belt_state_full', 'belt zone state full', \@code );

# _belt_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone off
#       motor run + tick  (run with next)
#       make sure next has box loaded
#       load box
#       if eye blocked
#          if elapsed > jam  --> STUCK error
#             goto FAULT
#       else
#          goto DRAIN
#
@code=qw(
   >r

   r@ off
   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =
   REG_NEXT r@ dp.register.get  _zone_elapsed@ 10 >  and if
      r@ _belt_cnt++
      r@ _belt_box_pass
   then

   r@ _belt_box_load

   REG_EYE r@ _zone_dp? if
      r@ _zone_elapsed@  REG_TM_JAM r@ _zone_rp@  > if
         ERR_STUCK r@ _zone_error_msg
         REG_ZONEFAULT r@ _zone_dp_on
         STATE_FAULT r@ _zone_state!
      then
   else
      r@ _zone_elapsed@  REG_TM_DEAD r@ _zone_rp@  > if
         STATE_DRAIN r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_belt_state_drain_x', 'belt zone state drain (blocked)',
      \@code );

# _belt_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone off
#       motor run + tick  (run with next)
#       make sure next has box loaded
#       load box
#       if eye blocked
#          if elapsed > min
#             goto FILL_X
#          else
#             motor stop
#       else if elapsed > runout & min
#          if box counter > 0
#             goto FILL
#          else
#             goto RUNUP
#
@code=qw(
   >r

   r@ off
   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =
   REG_NEXT r@ dp.register.get  _zone_elapsed@ 10 >  and if
      r@ _belt_cnt++
      r@ _belt_box_pass
   then

   r@ _belt_box_load

   REG_EYE r@ _zone_dp? if
      r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  > if
         STATE_FILL_X r@ _zone_state!
      else
         REG_MOTOR r@ _zone_dp_off
      then
   else
      r@ _zone_elapsed@  REG_RUNOUT r@ _zone_rp@  >
      r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  >  and if
         r@ _belt_cnt@ 0 > if
            STATE_FILL r@ _zone_state!
         else
            STATE_RUNUP r@ _zone_state!
         then
      then
   then

   r> drop
);
Trk::ex_compile( '_belt_state_drain', 'belt zone state drain (unblocked)',
      \@code );

# _belt_state_fault  ( dp -- )
#    halt the zone until fault clears
#
#       zone on
#       motor stop
#       if fault clear
#          goto INIT
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_belt_state_fault', 'belt zone state fault', \@code );

# _belt_machine  ( dp -- )
#    execute belt zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _belt_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _belt_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _belt_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _belt_state_idle    then
      r@ _zone_state@  STATE_FILL    = if r@ _belt_state_fill    then
      r@ _zone_state@  STATE_FILL_X  = if r@ _belt_state_fill_x  then
      r@ _zone_state@  STATE_FULL    = if r@ _belt_state_full    then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _belt_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _belt_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_belt_machine', 'belt zone state machine', \@code );


# create belt zone
sub addBelt {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'belt zone ' . $name, $data );

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'belt_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'belt_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'belt_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'belt_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'belt_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'belt_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );
   } else { $rp = rp::handle( 'belt_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'belt_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'belt_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # run the state machine
   my @stub = ( $name, '_belt_machine' );
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
