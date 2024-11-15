#!/bin/perl -I.
#
# zonetrak_xfer_merge : ZoneTRAK three-to-one pop-up merge zone
#
# addXferMerge( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode - create new box if anonymous box present
#    lax_mode    - ignore faults/jams/etc
#    slug_mode   - slug release
#    stop_mode   - stop each box until permissive fired
#
# accepts the following control elements:
#    desc       - description (default: 'transfer merge zone <name>')
#    default    - default input direction (0 = round robin, -1 = last input)
#    next       - downstream zone or control dp
#    eye        - confirm photoeye for straight destination
#    motor      - transport motor for run-in/straight
#    jameye     - diagonal jam-detect eye
#    filleye    - upstream fill eye
#    active     - output to activate for left/right transfer
#    active_eye - sensor to detect activated state
#    deact      - output to deactivate for straight destination
#    deact_eye  - sensor to detect deactivated state
#    left       - set non-empty to indicate presence of left input in case
#                 eye and motor are not defined
#    left_eye   - upstream fill eye from left
#    left_motor - output to transport in from left
#    right      - set non-empty to indicate presence of right input in case
#                 eye and motor are not defined
#    right_eye  - upstream fill eye from right
#    right_motor- output to transport in from right
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
#    runout     - run trail-edge of box out past straight confirm eye (cycles)
#    runside    - run trail-edge of box in past left/right fill eye (cycles)
#    rundown    - run activate/deactivate output (if act/deact eye not
#                 defined) (cycles)
#    min        - min time in state, e.g., allow run to fill eye, pause
#                 before activate, etc. (cycles)
#    max        - max time in state, e.g., detect missing (cycles)
#    choose     - max time for single-input availability (cycles)
#    wait       - wait time between availability switches to avoid race
#                 conditions (cycles)
#    dead       - dead time before detecting eye clear during release (cycles)
#    jam        - jam time (cycles)
#    slug       - min time before slug-mode release (cycles, if slug_mode)
#
# creates the following dp's for external control:
#    <name>_left and/or <name>_right - virtual zone(s) to monitor for
#                   availability from left or right input
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
require "zonetrak_merge.pl";


my @code;

# global timing parameters
rp::const( 'xmerge_runin',    100, 'xfer merge run-in cycles' );
rp::const( 'xmerge_runout',    10, 'xfer merge run-out cycles' );
rp::const( 'xmerge_runside',   10, 'xfer merge run-side cycles' );
rp::const( 'xmerge_rundown',   25, 'xfer merge run-down cycles' );
rp::const( 'xmerge_min',       10, 'xfer merge minimum time (cycles)' );
rp::const( 'xmerge_max',      300, 'xfer merge maximum time (cycles)' );
rp::const( 'xmerge_choose',    40, 'xfer merge choose time (cycles)' );
rp::const( 'xmerge_wait',      10, 'xfer merge wait time (cycles)' );
rp::const( 'xmerge_dead',       0, 'xfer merge dead time (cycles)' );
rp::const( 'xmerge_jam',      300, 'xfer merge jam time (cycles)' );
rp::const( 'xmerge_slug',      10,
      'xfer merge slug-release delay time (cycles)' );


# _xmerge_state_init  ( dp -- )
#    initialize the merge state machine
#
#       zone on
#       motors stop
#       set box NONE
#       if zone running
#          goto RUNUP
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_GO r@ _zone_dp_off

   BOX_NONE r@ _zone_box!
   REG_LEFT r@ dp.register.get
   dup 0 > if  BOX_NONE swap _zone_box!  else drop then
   REG_RIGHT r@ dp.register.get
   dup 0 > if  BOX_NONE swap _zone_box!  else drop then

   r@ _zone_run? if
      STATE_RUNUP r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_init', 'merge state init', \@code );


# _xmerge_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       motors stop
#       if eye blocked  --> STUCK error
#          goto FAULT
#       else
#          goto DEACT
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_off
   REG_GO r@ _zone_dp_off

   REG_EYE r@ _zone_dp? if
      ERR_STUCK r@ _zone_error_msg
      REG_ZONEFAULT r@ _zone_dp_on
      STATE_FAULT r@ _zone_state!
   else
      STATE_DEACT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_runup', 'transfer merge state run-up', \@code );

# _xmerge_state_idle  ( dp -- )
#    wait for incoming box
#
#       motors stop
#       choose input
#       if choice not NONE
#          load box from choice
#          goto ACTIVE
#       if jam-eye blocked  --> UNEXPECTED error
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_GO r@ _zone_dp_off

   r@ _merge_choose

   REG_CHOICE r@ dp.register.get  DIR_NONE = not if
      r@ _merge_box_load
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_ACTIVE r@ _zone_state!
   then

   REG_JAMEYE r@ _zone_dp? if
      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         ERR_UNEXPECTED r@ _zone_error_msg
         BOX_ANON r@ _zone_box!
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_idle', 'transfer merge state idle', \@code );

# _xmerge_state_fill  ( dp -- )
#    bring a box into the zone after the fill eye clears
#
#       zone on
#       choice->motor run + tick
#       if elapsed > runin
#          goto DEACT
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR  r@ _merge_choice_dest@  _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_RUNIN r@ _merge_choice_dest@ _zone_rp@  > if
      STATE_DEACT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_fill', 'transfer merge state fill (unblocked)',
      \@code );

# _xmerge_state_fill_x  ( dp -- )
#    bring a box into the zone while the fill eye is blocked
#
#       zone on
#       choice->motor run + tick
#       if elapsed > jam  --> STUCK error
#          goto FAULT
#       else
#          if elapsed > min and fill-eye unblocked
#             goto FILL
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR  r@ _merge_choice_dest@  _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_TM_JAM r@ _zone_rp@  >
   MODE_LAX r@ _zone_mode? not  and if
      ERR_STUCK r@ _zone_error_msg
      REG_ZONEFAULT r@ _zone_dp_on
      STATE_FAULT r@ _zone_state!
   else
      r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  >
      REG_INPUT r@ _zone_dp? not  and if
         STATE_FILL r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_fill_x', 'transfer merge state fill (blocked)',
      \@code );

# _xmerge_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motors stop + tick
#       if zone running and next is available
#          pass box to next
#          goto DRAIN_O
#       if jam-eye unblocked
#          if elapsed > clear  --> REMOVED error
#             goto INIT
#       else
#          clear timer
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   r@ _zone_tick

   r@ _zone_run?
   r@ _zone_next_avail?                                   and
   REG_HOLD r@ _zone_dp? not                              and
   MODE_STOP r@ _zone_mode? not  REG_GO r@ _zone_dp?  or  and if
      r@ _zone_box_pass
      STATE_DRAIN_O r@ _zone_state!
   then

   REG_JAMEYE r@ _zone_dp_not? if
      r@ _zone_elapsed@  REG_TM_CLEAR r@ _zone_rp@  > if
         ERR_REMOVED r@ _zone_error_msg
         STATE_INIT r@ _zone_state!
      then
   else
      r@ _zone_timer_clr
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_full', 'transfer merge state full', \@code );

# _xmerge_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone on
#       motor run + tick
#       if eye blocked
#          if elapsed > jam  --> STUCK error
#             goto FAULT
#       else
#          goto DRAIN
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_EYE r@ _zone_dp? if
      r@ _zone_elapsed@  REG_TM_JAM r@ _zone_rp@  > if
         MODE_LAX r@ _zone_mode? not if
            ERR_STUCK r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         else
            STATE_INIT r@ _zone_state!
         then
      then
   else
      r@ _zone_elapsed@  REG_TM_DEAD r@ _zone_rp@  > if
         STATE_DRAIN r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_drain_x','transfer merge state drain (blocked)',
      \@code );

# _xmerge_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on
#       motor run + tick
#       if elapsed > runout
#          goto DEACT
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _zone_elapsed@  REG_RUNOUT r@ _zone_rp@  > if
      STATE_DEACT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_drain', 'transfer merge state drain', \@code );

# _xmerge_state_active  ( dp -- )
#    put transfer in active state prior to transfer
#
#       zone on
#       motors stop + tick
#       if choice = straight
#          goto FILL_X
#       else
#          deact off + active on
#          if active-eye defined
#             if active-eye blocked
#                goto FILL_X
#             else if elapsed > max  --> SENSOR error
#                goto FAULT
#          else if elapsed > rundown
#             goto FILL_X
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   r@ _zone_tick

   REG_CHOICE r@ dp.register.get  DIR_STRAIGHT = if
      STATE_FILL_X r@ _zone_state!
   else

      REG_DEACT  r@ _zone_dp_off
      REG_ACTIVE r@ _zone_dp_on

      REG_ACTIVE_EYE r@ dp.register.get 0 > if
         REG_ACTIVE_EYE r@ _zone_dp? if
            STATE_FILL_X r@ _zone_state!
         else
            r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
               ERR_SENSOR r@ _zone_error_msg
               REG_ZONEFAULT r@ _zone_dp_on
               STATE_FAULT r@ _zone_state!
            then
         then
      else
         r@ _zone_elapsed@  REG_RUNDOWN r@ _zone_rp@  > if
            STATE_FILL_X r@ _zone_state!
         then
      then

   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_active', 'transfer merge state activate',
      \@code );

# _xmerge_state_deact  ( dp -- )
#    put transfer in inactive state
#
#       zone on
#       motors stop + tick
#       active off + deact on
#       if deact-eye defined
#          if deact-eye blocked
#             if box NONE
#                goto IDLE
#             else
#                goto FULL
#          else if elapsed > max  --> SENSOR error
#             goto FAULT
#       else if elapsed > rundown
#          if box NONE
#             goto IDLE
#          else
#             goto FULL
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   r@ _zone_tick

   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_on

   REG_DEACT_EYE r@ dp.register.get 0 > if
      REG_DEACT_EYE r@ _zone_dp? if
         r@ _zone_box@ BOX_NONE = if
            STATE_IDLE r@ _zone_state!
         else
            STATE_FULL r@ _zone_state!
         then
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            ERR_SENSOR r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         then
      then
   else
      r@ _zone_elapsed@  REG_RUNDOWN r@ _zone_rp@  > if
         r@ _zone_box@ BOX_NONE = if
            STATE_IDLE r@ _zone_state!
         else
            STATE_FULL r@ _zone_state!
         then
      then
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_deact', 'transfer merge state deactivate',
      \@code );

# _xmerge_state_drain_o  ( dp -- )
#    run box up to confirm eye
#
#       zone on
#       motor run + tick
#       if eye defined
#          if eye blocked
#             if elapsed < min  --> EARLY error
#                goto FAULT
#             else
#                goto DRAIN_X
#          if elapsed > max  --> MISSING error
#             goto INIT
#       else
#          goto DRAIN
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_EYE r@ dp.register.get 0 > if
      REG_EYE r@ _zone_dp? if
         r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  <
         MODE_LAX r@ _zone_mode? not  and if
            ERR_EARLY r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         else
            STATE_DRAIN_X r@ _zone_state!
         then
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            ERR_MISSING r@ _zone_error_msg
            STATE_INIT r@ _zone_state!
         then
      then
   else
      STATE_DRAIN r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_drain_o',
      'transfer merge state drain (unblocked)', \@code );

# _xmerge_state_fault  ( dp -- )
#    halt the zone until fault clears
#
#       zone on
#       motors stop
#       if fault clear
#          goto INIT
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_off
   REG_GO r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_xmerge_state_fault', 'transfer merge state fault', \@code );

# _xmerge_machine  ( dp -- )
#    execute transfer merge zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _xmerge_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _xmerge_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _xmerge_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _xmerge_state_idle    then
      r@ _zone_state@  STATE_ACTIVE  = if r@ _xmerge_state_active  then
      r@ _zone_state@  STATE_FILL_X  = if r@ _xmerge_state_fill_x  then
      r@ _zone_state@  STATE_FILL    = if r@ _xmerge_state_fill    then
      r@ _zone_state@  STATE_DEACT   = if r@ _xmerge_state_deact   then
      r@ _zone_state@  STATE_FULL    = if r@ _xmerge_state_full    then
      r@ _zone_state@  STATE_DRAIN_O = if r@ _xmerge_state_drain_o then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _xmerge_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _xmerge_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_xmerge_machine', 'transfer merge state machine', \@code );


# create transfer merge zone
sub addXferMerge {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'transfer merge zone ' . $name, $data );

   # set additional registers from input data
   if (($val = $data->{default}) ne '')
      { Trk::dp_registerset( $dp, REG_DEFAULT, $val ); }

   # create additional controls
   Trk::dp_registerset( $dp, REG_CONTROL,
         dp::virtual( $name . '_control', $name . ' input control' ) );

   # set additional registers from input data
   if (($val = $data->{jameye}) ne '')
      { Trk::dp_registerset( $dp, REG_JAMEYE, dp::handle( $val ) ); }
   if (($val = $data->{filleye}) ne '')
      { Trk::dp_registerset( $dp, REG_INPUT,  dp::handle( $val ) ); }
   if (($val = $data->{active}) ne '')
      { Trk::dp_registerset( $dp, REG_ACTIVE,     dp::handle( $val ) ); }
   if (($val = $data->{active_eye}) ne '')
      { Trk::dp_registerset( $dp, REG_ACTIVE_EYE, dp::handle( $val ) ); }
   if (($val = $data->{deact}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT,      dp::handle( $val ) ); }
   if (($val = $data->{deact_eye}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT_EYE,  dp::handle( $val ) ); }

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'xmerge_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'xmerge_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runside}) ne '') {
      $rp = rp::const( $name . '_runside', $val, $name . ' run-side cycles' );
   } else { $rp = rp::handle( 'xmerge_runside' ); }
   Trk::dp_registerset( $dp, REG_RUNOFF, $rp );

   if (($val = $data->{rundown}) ne '') {
      $rp = rp::const( $name . '_rundown', $val, $name . ' run-down cycles' );
   } else { $rp = rp::handle( 'xmerge_rundown' ); }
   Trk::dp_registerset( $dp, REG_RUNDOWN, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{choose}) ne '') {
      $rp = rp::const( $name . '_choose', $val, $name.' choose time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_choose' ); }
   Trk::dp_registerset( $dp, REG_TM_CHOOSE, $rp );

   if (($val = $data->{wait}) ne '') {
      $rp = rp::const( $name . '_wait', $val, $name.' wait time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_wait' ); }
   Trk::dp_registerset( $dp, REG_TM_WAIT, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'xmerge_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # set inputs
   if ($data->{left} ne '' or
         $data->{left_eye} ne '' or
         $data->{left_motor} ne '' ) {
      my $left = dp::virtual( $name . '_left', $name . ' left input' );
      Trk::dp_registerset( $dp, REG_LEFT, $left );

      if (($val = $data->{left_eye}) ne '')
         { Trk::dp_registerset( $left, REG_EYE,   dp::handle( $val ) ); }
      if (($val = $data->{left_motor}) ne '')
         { Trk::dp_registerset( $left, REG_MOTOR, dp::handle( $val ) ); }

      Trk::dp_registerset( $left, REG_RUNIN,
            Trk::dp_registerget( $dp, REG_RUNOFF ) );
   }
   if ($data->{right} ne '' or
         $data->{right_eye} ne '' or
         $data->{right_motor} ne '' ) {
      my $right = dp::virtual( $name . '_right', $name . ' right input' );
      Trk::dp_registerset( $dp, REG_RIGHT, $right );

      if (($val = $data->{right_eye}) ne '')
         { Trk::dp_registerset( $right, REG_EYE,   dp::handle( $val ) ); }
      if (($val = $data->{right_motor}) ne '')
         { Trk::dp_registerset( $right, REG_MOTOR, dp::handle( $val ) ); }

      Trk::dp_registerset( $right, REG_RUNIN,
            Trk::dp_registerget( $dp, REG_RUNOFF ) );
   }

   # run the state machine
   my @stub = ( $name, '_xmerge_machine' );
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
