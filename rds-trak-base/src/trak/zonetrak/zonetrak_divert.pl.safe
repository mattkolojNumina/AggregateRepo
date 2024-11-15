#!/bin/perl -I.
#
# zonetrak_divert : three-way (straight/left/right) ZoneTRAK divert zone
#       divert destination stored in box data
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
#    deact      - output to deactivate for straight destination
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
#    runout     - run trail-edge of box out past straight confirm eye (cycles)
#    runoff     - run trail-edge of box out past left/right confirm eye (cycles)
#    rundown    - run activate/deactivate output (if act/deact eye not
#                 defined) (cycles)
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


my @code;

# global timing parameters
rp::const( 'divert_runin',    100, 'divert run-in cycles' );
rp::const( 'divert_runout',    10, 'divert run-out cycles' );
rp::const( 'divert_runoff',    10, 'divert run-off cycles' );
rp::const( 'divert_rundown',   25, 'divert run-down cycles' );
rp::const( 'divert_min',       10, 'divert minimum time (cycles)' );
rp::const( 'divert_max',      300, 'divert maximum time (cycles)' );
rp::const( 'divert_clear',    200, 'divert clear time (cycles)' );
rp::const( 'divert_choose',   200, 'divert choose time (cycles)' );
rp::const( 'divert_wait',      10, 'divert wait time (cycles)' );
rp::const( 'divert_dead',       0, 'divert dead time (cycles)' );
rp::const( 'divert_jam',      300, 'divert jam time (cycles)' );
rp::const( 'divert_slug',      10, 'divert slug-release delay time (cycles)' );

# _divert_choice_dest@  ( dp -- destination )
#    get the destination for the chosen direction
@code=qw(
   >r

   REG_CHOICE r@ dp.register.get  r@ _zone_dest@

   r> drop
);
Trk::ex_compile( '_divert_choice_dest@', 'divert get choice destination',
      \@code);

# _divert_box_pass  ( dp -- )
#    pass the box to choice->next and clear it from the current zone
@code=qw(
   >r

   r@ _divert_choice_dest@  0 > if
      r@ _zone_box@
            REG_NEXT  r@ _divert_choice_dest@  dp.register.get
            _zone_box!
   then
   BOX_NONE r@ _zone_box!

   r> drop
);
Trk::ex_compile( '_divert_box_pass', 'divert pass box', \@code);


# _divert_motors_stop  ( dp -- )
#    turn off all motors
@code=qw(
   >r

   REG_MOTOR  r@                            _zone_dp_off
   REG_MOTOR  REG_LEFT  r@ dp.register.get  _zone_dp_off
   REG_MOTOR  REG_RIGHT r@ dp.register.get  _zone_dp_off

   r> drop
);
Trk::ex_compile( '_divert_motors_stop', 'stop all motors', \@code );


# _divert_motor_next  ( dp -- )
#    run motor with the motor of the next zone
# NB: to keep the motion smooth, there is a hard-coded delay before
#     changing the motor state; also, if the downstream zone is not
#     running, check that the box must be reloaded (e.g. after a fault)
@code=qw(
   >r

   r@ _zone_elapsed@ 2 < if
      REG_MOTOR r@ _divert_choice_dest@ _zone_dp_not? if
         REG_MOTOR  r@ _divert_choice_dest@  _zone_dp_off
      else
         REG_MOTOR  r@ _divert_choice_dest@  _zone_dp_on
      then
      r@ _zone_tick
   else
      REG_MOTOR  REG_NEXT r@ _divert_choice_dest@ dp.register.get
            _zone_dp_not? if

         REG_MOTOR r@ _divert_choice_dest@  _zone_dp_off

         REG_NEXT r@ _divert_choice_dest@ dp.register.get  _zone_box@
               BOX_NONE =
         REG_NEXT r@ _divert_choice_dest@ dp.register.get  _zone_state@
               STATE_IDLE =  and if
            BOX_ANON REG_NEXT r@ _divert_choice_dest@ dp.register.get _zone_box!
         then

      else
         REG_MOTOR  r@ _divert_choice_dest@  _zone_dp_on
         r@ _zone_tick
      then
   then

   r> drop
);
Trk::ex_compile( '_divert_motor_next', 'run zone motor with next zone motor',
      \@code );


# _divert_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       motor stop
#       if straight/left/right eye blocked  --> STUCK error
#          goto FAULT
#       else
#          goto DEACT
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_off
   REG_GO r@ _zone_dp_off

   REG_EYE  DIR_STRAIGHT r@ _zone_dest@  _zone_dp?
   REG_EYE  DIR_LEFT     r@ _zone_dest@  _zone_dp? or
   REG_EYE  DIR_RIGHT    r@ _zone_dest@  _zone_dp? or if
      ERR_STUCK r@ _zone_error_msg
      REG_ZONEFAULT r@ _zone_dp_on
      STATE_FAULT r@ _zone_state!
   else
      STATE_DEACT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_runup', 'divert state run-up', \@code );

# _divert_state_idle  ( dp -- )
#    wait for incoming box
#
#       zone off
#       motor stop
#       if box not NONE
#          goto FILL_O
#       if jam-eye blocked  --> UNEXPECTED error
#          set box ANON
#          goto CHOOSE
#
@code=qw(
   >r

   r@ _zone_run? not  r@ dp.value.set
   r@ _divert_motors_stop
   REG_GO r@ _zone_dp_off

   r@ _zone_box@ BOX_NONE = not if
      r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
         r@ _zone_box_new
      then
      STATE_FILL_O r@ _zone_state!
   then

   REG_JAMEYE r@ _zone_dp? if
      MODE_CREATE r@ _zone_mode? if
         r@ _zone_box_new
      else
         ERR_UNEXPECTED r@ _zone_error_msg
         BOX_ANON r@ _zone_box!
      then
      STATE_CHOOSE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_idle', 'divert state idle', \@code );

# _divert_state_fill  ( dp -- )
#    bring a box into the zone after the fill eye clears
#
#       zone on
#       motor run + tick
#       if elapsed > runin
#          goto CHOOSE
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_RUNIN r@ _zone_rp@  > if
      STATE_CHOOSE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_fill', 'divert state fill', \@code );

# _divert_state_fill_x  ( dp -- )
#    bring a box into the zone while the fill eye is blocked
#
#       zone on
#       motor run + tick
#       if elapsed > jam  --> STUCK error
#          goto FAULT
#       if fill-eye unblocked
#          goto FILL
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_TM_JAM r@ _zone_rp@  >
   MODE_LAX r@ _zone_mode? not  and if
      ERR_STUCK r@ _zone_error_msg
      REG_ZONEFAULT r@ _zone_dp_on
      STATE_FAULT r@ _zone_state!
   else
      REG_INPUT r@ _zone_dp? not  if
         r@ _zone_elapsed@  REG_CONTROL r@ dp.register.get * 100 /
         STATE_FILL r@ _zone_state!
         REG_TIMER r@ dp.register.set
      then
   then

   r> drop
);
Trk::ex_compile( '_divert_state_fill_x', 'divert state fill (blocked)',
      \@code );

# _divert_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motor stop + tick
#       if zone running and choice->next is available
#          pass box to choice->next
#          goto ACTIVE
#       if jam-eye unblocked
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
      STATE_ACTIVE r@ _zone_state!
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
Trk::ex_compile( '_divert_state_full', 'divert state full', \@code );

# _divert_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone on (unless slug-release and active off)
#       choice->motor run + tick
#       if choice->eye blocked
#          if elapsed > jam  --> STUCK error
#             goto FAULT
#       else
#          goto DRAIN
#
@code=qw(
   >r

   r@ _zone_slug_release?
   REG_ACTIVE r@ _zone_dp? not  and
   REG_ACTIVE_EYE r@ _zone_dp? not  and
      not r@ dp.value.set

   r@ _divert_motor_next
   REG_GO r@ _zone_dp_off

   r@ _zone_box@ BOX_NONE = not if
     STATE_FILL_O r@ _zone_state!
   else
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
   then

   r> drop
);
Trk::ex_compile( '_divert_state_drain_x', 'divert state drain (blocked)',
      \@code );

# _divert_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on (unless slug-release and active off)
#       choice->motor run + tick
#       if box not NONE
#          goto FILL_O
#       else if elapsed > runout
#          goto DEACT
#
@code=qw(
   >r

   r@ _zone_slug_release?
   REG_ACTIVE r@ _zone_dp? not  and
   REG_ACTIVE_EYE r@ _zone_dp? not  and
      not r@ dp.value.set

   r@ _divert_motor_next
   REG_GO r@ _zone_dp_off

   r@ _zone_box@ BOX_NONE = not 
   if
     STATE_FILL_O r@ _zone_state!
   else
     r@ _zone_box@ BOX_NONE = not if
        r@ _zone_box@ BOX_ANON =  MODE_CREATE r@ _zone_mode?  and if
           r@ _zone_box_new
        then
        STATE_FILL_O r@ _zone_state!
     else
        r@ _zone_elapsed@  REG_RUNOUT r@ _divert_choice_dest@ _zone_rp@  > if
           STATE_DEACT r@ _zone_state!
        then
     then
   then

   r> drop
);
Trk::ex_compile( '_divert_state_drain', 'divert state drain', \@code );

# _divert_state_choose  ( dp -- )
#    pause until divert destination is assigned
#
#       zone on
#       motor stop + tick
#       if box not valid or elapsed > choose
#          choice = default
#       else
#          choice = dir from box data
#       if choice > 0
#          if choice destination invalid
#             choice = default
#          goto FULL
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   r@ _zone_tick

   r@ _zone_box_valid? not
   r@ _zone_elapsed@  REG_TM_CHOOSE r@ _zone_rp@  >  or if
      REG_DEFAULT r@ dp.register.get  REG_CHOICE r@ dp.register.set
   else
      r@ _zone_box_dir@  REG_CHOICE r@ dp.register.set
   then

   REG_CHOICE r@ dp.register.get  0 > if
      r@ _divert_choice_dest@ 0 = if
         REG_DEFAULT r@ dp.register.get  REG_CHOICE r@ dp.register.set
      then
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_choose', 'divert state choose', \@code );

# _divert_state_active  ( dp -- )
#    put divert in active state prior to transfer
#
#       zone on
#       motor stop + tick
#       if choice = straight
#          goto DRAIN_O
#       else if elapsed > wait
#          deact off + active on
#          if active-eye defined
#             if active-eye blocked
#                goto DRAIN_O
#             else if elapsed > max  --> SENSOR error
#                goto FAULT
#          else if elapsed > rundown
#             goto DRAIN_O
#
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

         REG_DEACT  r@ _zone_dp_off
         REG_ACTIVE r@ _zone_dp_on

         REG_ACTIVE_EYE r@ dp.register.get 0 > if
            REG_ACTIVE_EYE r@ _zone_dp? if
               STATE_DRAIN_O r@ _zone_state!
            else
               r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
                  ERR_SENSOR r@ _zone_error_msg
                  REG_ZONEFAULT r@ _zone_dp_on
                  STATE_FAULT r@ _zone_state!
               then
            then
         else
            r@ _zone_elapsed@  REG_RUNDOWN r@ _zone_rp@  > if
               STATE_DRAIN_O r@ _zone_state!
            then
         then

      then
   then

   r> drop
);
Trk::ex_compile( '_divert_state_active', 'divert state activate', \@code );

# _divert_state_deact  ( dp -- )
#    put divert in inactive state
#
#       zone on
#       motor stop + tick
#       active off + deact on
#       if deact-eye defined
#          if deact-eye blocked
#             goto IDLE
#          else if elapsed > max  --> SENSOR error
#             goto FAULT
#       else if elapsed > rundown
#          goto IDLE
#
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_on

   REG_DEACT_EYE r@ dp.register.get 0 > if
      REG_DEACT_EYE r@ _zone_dp? if
         STATE_IDLE r@ _zone_state!
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            ERR_SENSOR r@ _zone_error_msg
            REG_ZONEFAULT r@ _zone_dp_on
            STATE_FAULT r@ _zone_state!
         then
      then
   else
      r@ _zone_elapsed@  REG_RUNDOWN r@ _zone_rp@  > if
         STATE_IDLE r@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_divert_state_deact', 'divert state deactivate', \@code );

# _divert_state_fill_o  ( dp -- )
#    bring box into zone up to fill eye
#
#       zone on
#       motor run + tick
#       if fill-eye defined
#          if fill-eye blocked
#             goto FILL_X
#          if elapsed > max  --> MISSING error
#             goto INIT
#       else
#          goto FILL
#       
@code=qw(
   >r

   r@ on
   r@ _divert_motors_stop
   REG_MOTOR r@ _zone_dp_on
   r@ _zone_tick

   REG_INPUT r@ dp.register.get  0 > if
      REG_INPUT r@ _zone_dp? if
         STATE_FILL_X r@ _zone_state!
      else
         r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  > if
            ERR_MISSING r@ _zone_error_msg
            STATE_INIT r@ _zone_state!
         then
      then
   else
      STATE_FILL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_fill_o', 'divert state fill (unblocked)',
      \@code);


# _divert_state_drain_o  ( dp -- )
#    run box up to destination confirm eye
#
#       zone on
#       choice->motor run + tick
#       if choice->eye defined
#          if choice->eye blocked
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

   r@ on
   r@ _divert_motor_next
   REG_GO r@ _zone_dp_off

   REG_EYE  r@ _divert_choice_dest@  dp.register.get  0 > if
      REG_EYE  r@ _divert_choice_dest@  _zone_dp? if
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
            MODE_LAX r@ _zone_mode? not if
               REG_ZONEFAULT r@ _zone_dp_on
               STATE_FAULT r@ _zone_state!
            else
               STATE_INIT r@ _zone_state!
            then
         then
      then
   else
      STATE_DRAIN r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_drain_o', 'divert state drain (unblocked)',
      \@code);

# _divert_state_fault  ( dp -- )
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
   r@ _divert_motors_stop
   REG_ACTIVE r@ _zone_dp_off
   REG_DEACT  r@ _zone_dp_off
   REG_GO r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_divert_state_fault', 'divert state fault', \@code );

# _divert_machine  ( dp -- )
#    execute divert zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _divert_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _zone_state_init      then
      r@ _zone_state@  STATE_RUNUP   = if r@ _divert_state_runup   then
      r@ _zone_state@  STATE_DEACT   = if r@ _divert_state_deact   then
      r@ _zone_state@  STATE_IDLE    = if r@ _divert_state_idle    then
      r@ _zone_state@  STATE_FILL_O  = if r@ _divert_state_fill_o  then
      r@ _zone_state@  STATE_FILL_X  = if r@ _divert_state_fill_x  then
      r@ _zone_state@  STATE_FILL    = if r@ _divert_state_fill    then
      r@ _zone_state@  STATE_CHOOSE  = if r@ _divert_state_choose  then
      r@ _zone_state@  STATE_FULL    = if r@ _divert_state_full    then
      r@ _zone_state@  STATE_ACTIVE  = if r@ _divert_state_active  then
      r@ _zone_state@  STATE_DRAIN_O = if r@ _divert_state_drain_o then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _divert_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _divert_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_divert_machine', 'divert state machine', \@code );


# create divert zone
sub addDivert {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'divert zone ' . $name, $data );

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
   if (($val = $data->{deact}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT,      dp::handle( $val ) ); }
   if (($val = $data->{deact_eye}) ne '')
      { Trk::dp_registerset( $dp, REG_DEACT_EYE,  dp::handle( $val ) ); }

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'divert_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'divert_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runoff}) ne '') {
      $rp = rp::const( $name . '_runoff', $val, $name . ' run-off cycles' );
   } else { $rp = rp::handle( 'divert_runoff' ); }
   Trk::dp_registerset( $dp, REG_RUNOFF, $rp );

   if (($val = $data->{rundown}) ne '') {
      $rp = rp::const( $name . '_rundown', $val, $name . ' run-down cycles' );
   } else { $rp = rp::handle( 'divert_rundown' ); }
   Trk::dp_registerset( $dp, REG_RUNDOWN, $rp );

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
   my @stub = ( $name, '_divert_machine' );
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
