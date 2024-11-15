#!/bin/perl -I.
#
# zonetrak_edivert : electric three-way ZoneTRAK divert zone
#       divert destination stored in box data
#
# addEDivert( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode - create new box if anonymous box present
#    lax_mode    - ignore faults/jams/etc
#    slug_mode   - slug release
#    stop_mode   - stop each box until permissive fired
#
# accepts the following control elements:
#    desc       - description (default: 'divert zone <name>')
#    offset     - depth of divert in destination tree
#    default    - default direction
#    next       - downstream zone or control dp
#    eye        - confirm photoeye for straight destination
#    motor      - transport motor for run-in/straight
#    jameye     - diagonal jam-detect eye
#    filleye    - upstream fill eye
#    active     - output to activate for left/right divert
#    active_eye - sensor to detect activated state
#    deact_eye  - sensor to detect deactivated state
#    left       - left destination
#    left_eye   - confirm photoeye for left divert
#    left_motor - output to transport left
#    right      - right destination
#    right_eye  - confirm photoeye for right divert
#    right_motor- output to transport right
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
#    runin      - run trail-edge of box in past fill eye (cycles)
#    runover    - run active motor past active/deact sensors (cycles)
#    runout     - run trail-edge of box out past straight confirm eye (cycles)
#    runoff     - run trail-edge of box out past left/right confirm eye (cycles)
#    min        - min time in state, e.g., allow run to fill eye, pause
#                 before activate, etc. (cycles)
#    max        - max time in state, e.g., detect missing (cycles)
#    clear      - max time with jam-eye clear, e.g., detect removed (cycles)
#    choose     - max time for divert decision (cycles)
#    wait       - pause before activating output (cycles)
#    dead       - dead time before detecting eye clear during release (cycles)
#    jam        - jam time (cycles)
#    slug       - min time before slug-mode release (cycles, if slug_mode)
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
require "zonetrak_divert.pl";


my @code;

# global timing parameters
rp::const('edivert_runover',   0,'e-divert run-over cycles');


# _edivert_state_active  ( dp -- )
#    put divert in active state prior to transfer
#
#       zone on
#       motor stop + tick
#       if choice = straight
#          goto DRAIN_O
#       else if elapsed > wait
#          active on
#          if active-eye blocked
#             goto DRAIN_O
#          else if elapsed > max  --> SENSOR error
#             goto FAULT
#
# NB:  use temp register to track runover past active sensor
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_CHOICE r@ dp.register.get  DIR_STRAIGHT = if
      STATE_DRAIN_O r@ _zone_state!
   else
      r@ _zone_elapsed@  REG_TM_WAIT r@ _zone_rp@  > if

         REG_ACTIVE_EYE r@ _zone_dp? if
            REG_TEMP r@ dp.register.get
            1 +
            REG_TEMP r@ dp.register.set

            REG_TEMP r@ dp.register.get  REG_RUNOVER r@ _zone_rp@  > if
               REG_ACTIVE r@ _zone_dp_off
               STATE_DRAIN_O r@ _zone_state!
            then
         else
            REG_ACTIVE r@ _zone_dp_on
            0 REG_TEMP r@ dp.register.set

            r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
               ERR_SENSOR r@ _zone_error_msg
               REG_ZONEFAULT r@ _zone_dp_on
               STATE_FAULT r@ _zone_state!
            then
         then

      then
   then

   r> drop
);
Trk::ex_compile( '_edivert_state_active', 'divert state activate', \@code );

# _edivert_state_deact  ( dp -- )
#    put divert in inactive state
#
#       zone on
#       motor stop + tick
#       active on
#       if deact-eye blocked
#          goto IDLE
#       else if elapsed > max  --> SENSOR error
#          goto FAULT
#
# NB:  use temp register to track runover past deact sensor
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_DEACT_EYE r@ _zone_dp? if
      REG_TEMP r@ dp.register.get
      1 +
      REG_TEMP r@ dp.register.set

      REG_TEMP r@ dp.register.get  REG_RUNOVER r@ _zone_rp@  > if
         REG_ACTIVE r@ _zone_dp_off
         STATE_IDLE r@ _zone_state!
      then
   else
      REG_ACTIVE r@ _zone_dp_on
      0 REG_TEMP r@ dp.register.set

      r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
         ERR_SENSOR r@ _zone_error_msg
         REG_ZONEFAULT r@ _zone_dp_on
         STATE_FAULT r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_edivert_state_deact', 'e-divert state deactivate', \@code );


# _edivert_machine  ( dp -- )
#    execute e-divert zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _divert_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init      then
      r@ _zone_state@  STATE_RUNUP   = if r@ _divert_state_runup   then
      r@ _zone_state@  STATE_DEACT   = if r@ _edivert_state_deact  then
      r@ _zone_state@  STATE_IDLE    = if r@ _divert_state_idle    then
      r@ _zone_state@  STATE_FILL_O  = if r@ _divert_state_fill_o  then
      r@ _zone_state@  STATE_FILL_X  = if r@ _divert_state_fill_x  then
      r@ _zone_state@  STATE_FILL    = if r@ _divert_state_fill    then
      r@ _zone_state@  STATE_CHOOSE  = if r@ _divert_state_choose  then
      r@ _zone_state@  STATE_FULL    = if r@ _divert_state_full    then
      r@ _zone_state@  STATE_ACTIVE  = if r@ _edivert_state_active then
      r@ _zone_state@  STATE_DRAIN_O = if r@ _divert_state_drain_o then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _divert_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _divert_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_edivert_machine', 'e-divert state machine', \@code );


# create e-divert zone
sub addEDivert {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'e-divert zone ' . $name, $data );

   # set additional registers from input data
   if (($val = $data->{offset}) ne '')
      { Trk::dp_registerset( $dp, REG_OFFSET,     $val ); }
   if (($val = $data->{default}) ne '')
      { Trk::dp_registerset( $dp, REG_DEFAULT,    $val ); }
   if (($val = $data->{edge}) ne '')
      { Trk::dp_registerset( $dp, REG_CONTROL,    $val ); }
   if (($val = $data->{jameye}) ne '')
      { Trk::dp_registerset( $dp, REG_JAMEYE, dp::handle( $val ) ); }
   if (($val = $data->{filleye}) ne '')
      { Trk::dp_registerset( $dp, REG_INPUT,  dp::handle( $val ) ); }
   if (($val = $data->{active}) ne '')
      { Trk::dp_registerset( $dp, REG_ACTIVE,     dp::handle( $val ) ); }
   if (($val = $data->{active_eye}) ne '')
      { Trk::dp_registerset( $dp, REG_ACTIVE_EYE, dp::handle( $val ) ); }
   if (($val = $data->{deact_eye}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT_EYE,  dp::handle( $val ) ); }

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'divert_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'edivert_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'divert_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runoff}) ne '') {
      $rp = rp::const( $name . '_runoff', $val, $name . ' run-off cycles' );
   } else { $rp = rp::handle( 'divert_runoff' ); }
   Trk::dp_registerset( $dp, REG_RUNOFF, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'divert_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'divert_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );
   } else { $rp = rp::handle( 'divert_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{choose}) ne '') {
      $rp = rp::const( $name . '_choose', $val, $name.' choose time (cycles)' );
   } else { $rp = rp::handle( 'divert_choose' ); }
   Trk::dp_registerset( $dp, REG_TM_CHOOSE, $rp );

   if (($val = $data->{wait}) ne '') {
      $rp = rp::const( $name . '_wait', $val, $name.' wait time (cycles)' );
   } else { $rp = rp::handle( 'divert_wait' ); }
   Trk::dp_registerset( $dp, REG_TM_WAIT, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'divert_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'divert_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # set destinations
   if (($val = $data->{left}) ne '') {
      my $left = dp::virtual( $name . '_left', $name . ' left destination' );
      Trk::dp_registerset( $dp, REG_LEFT, $left );

      Trk::dp_registerset( $left, REG_NEXT, dp::handle( $val ) );
      if (($val = $data->{left_eye}) ne '')
         { Trk::dp_registerset( $left, REG_EYE,   dp::handle( $val ) ); }
      if (($val = $data->{left_motor}) ne '')
         { Trk::dp_registerset( $left, REG_MOTOR, dp::handle( $val ) ); }

      Trk::dp_registerset( $left, REG_RUNOUT,
            Trk::dp_registerget( $dp, REG_RUNOFF ) );
   }
   if (($val = $data->{right}) ne '') {
      my $right = dp::virtual( $name . '_right', $name . ' right destination' );
      Trk::dp_registerset( $dp, REG_RIGHT, $right );

      Trk::dp_registerset( $right, REG_NEXT, dp::handle( $val ) );
      if (($val = $data->{right_eye}) ne '')
         { Trk::dp_registerset( $right, REG_EYE,   dp::handle( $val ) ); }
      if (($val = $data->{right_motor}) ne '')
         { Trk::dp_registerset( $right, REG_MOTOR, dp::handle( $val ) ); }

      Trk::dp_registerset( $right, REG_RUNOUT,
            Trk::dp_registerget( $dp, REG_RUNOFF ) );
   }

   # run the state machine
   my @stub = ( $name, '_edivert_machine' );
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
