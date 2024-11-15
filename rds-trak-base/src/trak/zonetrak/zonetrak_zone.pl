#!/bin/perl -I.
#
# zonetrak_zone : standard ZoneTRAK transport zone
#
# addZone( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode - create new box if anonymous box present
#    lax_mode    - ignore faults/jams/etc
#    slug_mode   - slug release
#    stop_mode   - stop each box until permissive fired
#    beltfeed_mode - loads box in pe, runs a time in idle
#    pushout_mode  - does not fault on drain_x timeout, cycles motor
#
# accepts the following control elements:
#    desc       - description (default: 'transport zone <name>')
#    next       - downstream zone or control dp
#    eye        - presence photoeye
#    motor      - transport motor
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
#    runin   - run box into zone (if eye not defined) (cycles)
#    runover - run lead-edge of box past presence eye during fill (cycles)
#    runout  - run trail-edge of box past presence eye during drain (cycles)
#    runup   - run zone during initialization (cycles)
#    runidle - run for a period in idle when in beltfeed mode (cycles)
#    min     - min allowed time in state, e.g., detect early (cycles)
#    max     - max allowed time in state, e.g., detect missing (cycles)
#    clear   - max time with eye clear, e.g., detect removed (cycles)
#    dead    - dead time before detecting eye clear during release (cycles)
#    jam     - jam time (cycles)
#    slug    - min time before slug-mode release (cycles, if slug_mode)
#    pushout_period  - period for push cycles
#    pushout_duty    - on time in push cycles
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


my @code;

# global timing parameters
rp::const( 'zone_runin',    50, 'zone run-in cycles' );
rp::const( 'zone_runover',   0, 'zone run-over cycles' );
rp::const( 'zone_runout',   10, 'zone run-out cycles' );
rp::const( 'zone_runup',   100, 'zone run-up cycles' );
rp::const( 'zone_runidle', 500, 'zone run-idle cycles') ;
rp::const( 'zone_min',      50, 'zone minimum time (cycles)' );
rp::const( 'zone_max',     300, 'zone maximum time (cycles)' );
rp::const( 'zone_clear',   200, 'zone clear time (cycles)' );
rp::const( 'zone_dead',      0, 'zone dead time (cycles)' );
rp::const( 'zone_jam',     300, 'zone jam time (cycles)' );
rp::const( 'zone_slug',     10, 'zone slug-release delay time (cycles)' );

rp::const( 'pushout_period', 1000, 'push period (cycles) ') ;
rp::const( 'pushout_duty',    300, 'push motor on') ;

# _zone_state_init  ( dp -- )
#    initialize the zone state machine
#
#       zone on
#       motor stop
#       set box NONE
#       if zone running
#          goto RUNUP
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_off
   REG_GO r@ _zone_dp_off

   BOX_NONE r@ _zone_box!

   r@ _zone_run? if
      r@ _zone_reset_msg
      STATE_RUNUP r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_zone_state_init', 'zone state init', \@code );


# _zone_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       motor run + tick
#       if elapsed > runup
#          goto IDLE
#       if eye blocked
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
Trk::ex_compile( '_zone_state_runup', 'zone state run-up', \@code );

# _zone_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop + tick
#       if box not NONE
#          goto FILL
#       if eye blocked
#          if elapsed < min  --> EARLY error
#             goto FAULT
#          else  --> UNEXPECTED error
#             set box ANON
#             goto FULL
#
@code=qw(
   >r

   r@ _zone_run? not  r@ dp.value.set

   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   MODE_IDLERUN r@ _zone_mode? 
   r@ _zone_run? and if
      REG_MOTOR r@ _zone_dp_on
   else
      MODE_BELTFEED r@ _zone_mode? if
        r@ _zone_run? 
        r@ _zone_elapsed@  zone_runidle rp.value.get  < and if
          REG_MOTOR r@ _zone_dp_on
        else 
          REG_MOTOR r@ _zone_dp_off
        then
      else
        REG_MOTOR r@ _zone_dp_off
      then
   then

   r@ _zone_box@ BOX_NONE = not if

      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then

      STATE_FILL r@ _zone_state!
   then

   REG_EYE r@ _zone_dp? if
      r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  <
      MODE_LAX r@ _zone_mode? not  and if
         ERR_EARLY r@ _zone_error_msg
         REG_ZONEFAULT r@ _zone_dp_on
         STATE_FAULT r@ _zone_state!
      else
         MODE_CREATE r@ _zone_mode? if
            r@ _zone_box_new
         else
            BOX_ANON r@ _zone_box!
            ERR_UNEXPECTED r@ _zone_error_msg
         then
         STATE_FULL r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_zone_state_idle', 'zone state idle', \@code );

# _zone_state_fill  ( dp -- )
#    bring a box into the zone
#
#       zone on
#       motor run + tick
#       if eye defined
#          if eye blocked
#             if elapsed < min  --> EARLY error
#                goto FAULT
#             else
#                goto FILL_X
#          else if elapsed > max  --> MISSING error
#             goto INIT
#       else if elapsed > runin
#          goto FULL
#
@code=qw(
   >r

   r@ on
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   MODE_BELTFEED r@ _zone_mode? if 
      r@ _zone_box@
      REG_EYE r@ dp.register.get dp.carton.set
   then

   REG_EYE r@ dp.register.get  0 > if
      REG_EYE r@ _zone_dp? if
         r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  <
         MODE_LAX r@ _zone_mode? not  and if
            ERR_EARLY r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         else
            STATE_FILL_X r@ _zone_state!
         then
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            ERR_MISSING r@ _zone_error_msg
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
Trk::ex_compile( '_zone_state_fill', 'zone state fill (unblocked)', \@code );

# _zone_state_fill_x  ( dp -- )
#    bring a box into the zone while the eye is blocked
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
Trk::ex_compile( '_zone_state_fill_x', 'zone state fill (blocked)', \@code );

# _zone_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motor stop + tick
#       if zone running and next is available
#          pass box to next
#          goto DRAIN_X
#       if eye unblocked
#          if elapsed > clear  --> REMOVED error
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
         ERR_REMOVED r@ _zone_error_msg
         STATE_INIT r@ _zone_state!
      then
   else
      r@ _zone_timer_clr
   then

   r> drop
);
Trk::ex_compile( '_zone_state_full', 'zone state full', \@code );

# _zone_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone on (unless slug-release)
#       if pushout_mode
#         if elapsed modulo period < duty
#           motor on
#         else
#           motor off
#       else
#         motor run with next
#      
#      if next zone is idle with no box
#         fill it with an anonymous box 
#
#      
#       if eye blocked
#         if not pushout mode
#            if elapsed > jam 
#              if not lax mode 
#                goto FAULT
#              else
#                goto init
#       else
#         if past dead time
#           goto DRAIN
#
@code=qw(
   >r

   r@ _zone_slug_release?  not  r@ dp.value.set
   REG_GO r@ _zone_dp_off
   r@ _zone_tick
   
   MODE_PUSHOUT r@ _zone_mode?  if
     r@ _zone_elapsed@  pushout_period rp.value.get %
     pushout_duty rp.value.get < if
       REG_MOTOR r@ _zone_dp_on 
     else
       REG_MOTOR r@ _zone_dp_off
     then 
   else
     r@ _zone_motor_next
   then

   REG_NEXT r@ dp.register.get  _zone_box@  BOX_NONE =
   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =  and if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then

   REG_EYE r@ _zone_dp? if
     MODE_PUSHOUT r@ _zone_mode? not if 
       r@ _zone_elapsed@  REG_TM_JAM r@ _zone_rp@  > if
         MODE_LAX r@ _zone_mode? not if
            ERR_STUCK r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         else
            STATE_INIT r@ _zone_state!
         then
       then
     then
   else
      r@ _zone_elapsed@  REG_TM_DEAD r@ _zone_rp@  > if
         STATE_DRAIN r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_zone_state_drain_x', 'zone state drain (blocked)', \@code );

# _zone_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on (unless slug-release)
#       motor run with next
#       if box not NONE
#          goto FILL
#       else if elapsed > runout
#          goto IDLE
#
@code=qw(
   >r

   r@ _zone_slug_release?  not  r@ dp.value.set
   REG_GO r@ _zone_dp_off
   r@ _zone_tick

   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_box@  BOX_NONE =
   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =  and if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then

   r@ _zone_box@ BOX_NONE = not if
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_FILL r@ _zone_state!
   else
      r@ _zone_elapsed@  REG_RUNOUT r@ _zone_rp@  > if
         STATE_IDLE r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_zone_state_drain', 'zone state drain (unblocked)', \@code );

# _zone_state_fault  ( dp -- )
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
   REG_GO r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_zone_state_fault', 'zone state fault', \@code );

# _zone_machine  ( dp -- )
#    execute transport zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _zone_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _zone_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _zone_state_idle    then
      r@ _zone_state@  STATE_FILL    = if r@ _zone_state_fill    then
      r@ _zone_state@  STATE_FILL_X  = if r@ _zone_state_fill_x  then
      r@ _zone_state@  STATE_FULL    = if r@ _zone_state_full    then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _zone_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _zone_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_zone_machine', 'zone state machine', \@code );


# create transport zone
sub addZone {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'transport zone ' . $name, $data );

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'zone_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'zone_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'zone_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'zone_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'zone_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'zone_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );
   } else { $rp = rp::handle( 'zone_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'zone_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'zone_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # run the state machine
   my @stub = ( $name, '_zone_machine' );
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
