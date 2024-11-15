#!/bin/perl -I.
#
# zonetrak_angle_divert : two-way (straight vs spur) ZoneTRAK divert zone
#       divert destination stored in box data, must be present when box loaded
#       use DIR_LEFT (2) for spur destination
#
# addDivert( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode - create new box if anonymous box present
#    lax_mode    - ignore faults/jams/etc
#    slug_mode   - slug release
#    stop_mode   - stop each box until permissive fired
#
# accepts the following control elements:
#    desc       - description (default: 'angle divert zone <name>')
#    offset     - depth of divert in destination tree
#    default    - default direction
#    next       - downstream zone or control dp
#    eye        - presence photoeye for straight destination
#    motor      - transport motor for straight
#    active     - output to activate for spur divert
#    deact      - output to deactivate for straight destination
#    spur_next  - downstream zone for spur
#    spur_eye   - presence photoeye for spur
#    spur_motor - transport motor for spur
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
#    runin   - run box into zone (if destination eye not defined) (cycles)
#    runover - run lead-edge of box past destination eye during fill (cycles)
#    runout  - run trail-edge of box out past straight eye (cycles)
#    runoff  - run trail-edge of box out past spur eye (cycles)
#    runup   - run zone during initialization (cycles)
#    min     - min time in state, e.g., detect early (cycles)
#    max     - max time in state, e.g., detect missing (cycles)
#    clear   - max time with eye clear, e.g., detect removed (cycles)
#    dead    - dead time before detecting eye clear during release (cycles)
#    jam     - jam time (cycles)
#    slug    - min time before slug-mode release (cycles, if slug_mode)
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
rp::const('angle_runin',  100,'angle divert run-in cycles');
rp::const('angle_runover',  0,'angle divert run-over cycles');
rp::const('angle_runout',  10,'angle divert run-out cycles');
rp::const('angle_runoff',  10,'angle divert run-off cycles');
rp::const('angle_runup',  100,'angle divert run-up cycles' );
rp::const('angle_min',     10,'angle divert minimum time (cycles)');
rp::const('angle_max',    300,'angle divert maximum time (cycles)');
rp::const('angle_clear',  200,'angle divert clear time (cycles)');
rp::const('angle_dead',     0,'angle divert dead time (cycles)' );
rp::const('angle_jam',    300,'angle divert jam time (cycles)');
rp::const('angle_slug',    10,'angle divert slug-release delay time (cycles)');


# _angle_spur@  ( dp -- spur-dp )
#    get the dp associated with the divert spur
@code=qw(
   REG_LEFT swap dp.register.get
);
Trk::ex_compile( '_angle_spur@', 'angle divert get spur', \@code );

# _angle_set_divert  ( dp -- )
#    set the divert according to the current choice
@code=qw(
   >r

   REG_CHOICE r@ dp.register.get  DIR_LEFT  = if
      REG_DEACT  r@ _zone_dp_off
      REG_ACTIVE r@ _zone_dp_on
   else
      REG_ACTIVE r@ _zone_dp_off
      REG_DEACT  r@ _zone_dp_on
   then

   r> drop
);
Trk::ex_compile( '_angle_set_divert', 'set angle divert', \@code );


# _angle_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       set choice to default + set divert direction
#       motor run + spur motor run + tick
#       if elapsed > runup
#          goto IDLE
#       if eye/spur->eye blocked
#          eye: choice = straight; spur->eye: choice = spur
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ on

   REG_DEFAULT r@ dp.register.get  REG_CHOICE r@ dp.register.set
   r@ _angle_set_divert

   REG_MOTOR  r@               _zone_dp_on
   REG_MOTOR  r@ _angle_spur@  _zone_dp_on
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _zone_elapsed@  REG_RUNUP r@ _zone_rp@  > if
      STATE_IDLE r@ _zone_state!
   then

   REG_EYE r@ _zone_dp?  REG_EYE r@ _angle_spur@ _zone_dp?  or if
      REG_EYE r@ _zone_dp? if
         DIR_STRAIGHT REG_CHOICE r@ dp.register.set
      else
         DIR_LEFT REG_CHOICE r@ dp.register.set
      then

      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         BOX_ANON r@ _zone_box!
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_angle_state_runup', 'angle divert state run-up', \@code );

# _angle_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop + tick
#       if box not NONE
#          goto CHOOSE
#       if eye/spur->eye blocked
#          eye: choice = straight; spur->eye: choice = spur
#          if elapsed < min  --> EARLY error
#             goto FAULT
#          else  --> UNEXPECTED error
#             set box ANON
#             goto FULL
#
@code=qw(
   >r

   r@ off
   r@ _divert_motors_stop
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _zone_box@ BOX_NONE = not if
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_CHOOSE r@ _zone_state!
   then

   REG_EYE r@ _zone_dp?  REG_EYE r@ _angle_spur@ _zone_dp?  or if
      REG_EYE r@ _zone_dp? if
         DIR_STRAIGHT REG_CHOICE r@ dp.register.set
      else
         DIR_LEFT REG_CHOICE r@ dp.register.set
      then

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
Trk::ex_compile( '_angle_state_idle', 'angle divert state idle', \@code );

# _angle_state_fill  ( dp -- )
#    bring a box into the zone
#
#       zone on
#       choice->motor run + tick
#       if choice->eye defined
#          if choice->eye blocked
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
   r@ _divert_motors_stop
   REG_MOTOR  r@ _divert_choice_dest@  _zone_dp_on
   r@ _zone_tick

   REG_EYE r@ _divert_choice_dest@ dp.register.get  0 > if
      REG_EYE r@ _divert_choice_dest@ _zone_dp? if
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
Trk::ex_compile( '_angle_state_fill', 'angle divert state fill (unblocked)',
      \@code );


# _angle_state_fill_x  ( dp -- )
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
   r@ _divert_motors_stop
   REG_MOTOR r@ _divert_choice_dest@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_RUNOVER r@ _zone_rp@  > if
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_angle_state_fill_x', 'angle divert state fill (blocked)',
      \@code );

# _angle_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motor stop + tick
#       if zone running and choice->next is available
#          pass box to choice->next
#          goto DRAIN_X
#       if choice->eye unblocked
#          if elapsed > clear  --> REMOVED error
#             goto INIT
#       else
#          clear timer
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   r@ _zone_tick

   r@ _zone_run?
   r@ _divert_choice_dest@ _zone_next_avail?              and
   REG_HOLD r@ _zone_dp? not                              and
   MODE_STOP r@ _zone_mode? not  REG_GO r@ _zone_dp?  or  and if
      r@ _divert_box_pass
      STATE_DRAIN_X r@ _zone_state!
   then

   REG_EYE r@ _divert_choice_dest@ _zone_dp_not? if
      r@ _zone_elapsed@  REG_TM_CLEAR r@ _zone_rp@  > if
         ERR_REMOVED r@ _zone_error_msg
         STATE_INIT r@ _zone_state!
      then
   else
      r@ _zone_timer_clr
   then

   r> drop
);
Trk::ex_compile( '_angle_state_full', 'angle divert state full', \@code );

# _angle_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone on (unless slug-release)
#       choice->motor run + tick  (run with choice->next if multi_mode)
#       if choice->eye blocked
#          if elapsed > jam  --> STUCK error
#             goto FAULT
#       else
#          goto DRAIN
#
# TODO  _zone_motor_next does not run with choice->next, so multi mode NYI
@code=qw(
   >r

   r@ _zone_slug_release?  not  r@ dp.value.set
   r@ _divert_motors_stop
   REG_GO r@ _zone_dp_off

   MODE_MULTI r@ _zone_mode? if
      r@ _zone_motor_next
   else
      REG_MOTOR r@ _zone_dp_on
      r@ _zone_tick
   then

   REG_EYE  r@ _divert_choice_dest@  _zone_dp? if
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
Trk::ex_compile( '_angle_state_drain_x', 'angle divert state drain (blocked)',
      \@code );

# _angle_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on (unless slug-release)
#       choice->motor run + tick  (run with choice->next if multi_mode)
#       if box not NONE
#          goto CHOOSE
#       else if elapsed > runout
#          goto IDLE
#
# TODO  _zone_motor_next does not run with choice->next, so multi mode NYI
@code=qw(
   >r

   r@ _zone_slug_release?  not  r@ dp.value.set
   r@ _divert_motors_stop
   REG_GO r@ _zone_dp_off

   MODE_MULTI r@ _zone_mode? if
      r@ _zone_motor_next
   else
      REG_MOTOR  r@ _divert_choice_dest@  _zone_dp_on
      r@ _zone_tick
   then

   r@ _zone_box@ BOX_NONE = not if
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_CHOOSE r@ _zone_state!
   else
      r@ _zone_elapsed@  REG_RUNOUT r@ _divert_choice_dest@ _zone_rp@  > if
         STATE_IDLE r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_angle_state_drain', 'angle divert state drain', \@code );

# _angle_state_choose  ( dp -- )
#    assign divert destination prior to fill
#
#       zone on
#       if box not valid
#          choice = default
#       else
#          choice = dir from box data
#       if choice destination invalid
#          choice = default
#       set divert solenoids
#       goto FILL
#
@code=qw(
   >r

   r@ on

   r@ _zone_box_valid? not if
      REG_DEFAULT r@ dp.register.get  REG_CHOICE r@ dp.register.set
   else
      r@ _zone_box_dir@  REG_CHOICE r@ dp.register.set
   then

   r@ _divert_choice_dest@ 0 = if
      REG_DEFAULT r@ dp.register.get  REG_CHOICE r@ dp.register.set
   then

   r@ _angle_set_divert
   STATE_FILL r@ _zone_state!

   r> drop
);
Trk::ex_compile( '_angle_state_choose', 'angle divert state choose', \@code );

# _angle_machine  ( dp -- )
#    execute divert zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _divert_state_fault  then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init     then
      r@ _zone_state@  STATE_RUNUP   = if r@ _angle_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _angle_state_idle    then
      r@ _zone_state@  STATE_CHOOSE  = if r@ _angle_state_choose  then
      r@ _zone_state@  STATE_FILL    = if r@ _angle_state_fill    then
      r@ _zone_state@  STATE_FILL_X  = if r@ _angle_state_fill_x  then
      r@ _zone_state@  STATE_FULL    = if r@ _angle_state_full    then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _angle_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _angle_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_angle_machine', 'angle divert state machine', \@code );


# create angle divert zone
sub addAngleDivert {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'angle divert zone ' . $name, $data );

   # set additional registers from input data
   if (($val = $data->{offset}) ne '')
      { Trk::dp_registerset( $dp, REG_OFFSET,     $val ); }
   if (($val = $data->{default}) ne '')
      { Trk::dp_registerset( $dp, REG_DEFAULT,    $val ); }
   if (($val = $data->{active}) ne '')
      { Trk::dp_registerset( $dp, REG_ACTIVE,     dp::handle( $val ) ); }
   if (($val = $data->{deact}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT,      dp::handle( $val ) ); }

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'angle_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'angle_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'angle_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runoff}) ne '') {
      $rp = rp::const( $name . '_runoff', $val, $name . ' run-off cycles' );
   } else { $rp = rp::handle( 'angle_runoff' ); }
   Trk::dp_registerset( $dp, REG_RUNOFF, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'angle_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'angle_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'angle_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name.' clear time (cycles)' );
   } else { $rp = rp::handle( 'angle_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'angle_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'angle_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # set spur registers
   if (($val = $data->{spur_next}) ne '') {
      my $spur = dp::virtual( $name . '_spur', $name . ' spur destination' );
      Trk::dp_registerset( $dp, REG_LEFT, $spur );

      Trk::dp_registerset( $spur, REG_NEXT, dp::handle( $val ) );
      if (($val = $data->{spur_eye}) ne '')
         { Trk::dp_registerset( $spur, REG_EYE,   dp::handle( $val ) ); }
      if (($val = $data->{spur_motor}) ne '')
         { Trk::dp_registerset( $spur, REG_MOTOR, dp::handle( $val ) ); }

      Trk::dp_registerset( $spur, REG_RUNOUT,
            Trk::dp_registerget( $dp, REG_RUNOFF ) );
   }

   # run the state machine
   my @stub = ( $name, '_angle_machine' );
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
