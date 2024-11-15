#!/bin/perl -I.
#
# zonetrak_merge : ZoneTRAK spur-into-trunk merge zone
#
# addMerge( name, {data} )
#
# allows the following modes (set non-empty to activate):
#    create_mode - create new box if anonymous box present
#    lax_mode    - ignore faults/jams/etc
#    slug_mode   - slug release (NYI)
#    stop_mode   - stop each box until permissive fired
#
# accepts the following elements:
#    desc       - description (default: 'merge zone <name>')
#    default    - default input direction (0 = round robin, -1 = last input)
#    next       - downstream zone or control dp
#    eye        - presence photoeye
#    motor      - transport motor
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
#    runin      - run box into zone (if eye not defined) (cycles)
#    runover    - run lead-edge of box past presence eye during fill (cycles)
#    runout     - run trail-edge of box past presence eye during drain (cycles)
#    runup      - run zone during initialization (cycles)
#    min        - min allowed time in state, e.g., detect early (cycles)
#    max        - max allowed time in state, e.g., detect missing (cycles)
#    clear      - max time with eye clear, e.g., detect removed (cycles)
#    choose     - time to spend in input selection (cycles)
#    wait       - time to spend waiting in-between selections (cycles)
#    dead       - dead time before detecting eye clear during release (cycles)
#    jam        - jam time (cycles)
#    slug       - min time before slug-mode release (cycles, if slug_mode)
#
# creates the following dp's for external control:
#    <name>_spur  - virtual zone to monitor for availability from spur
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
rp::const( 'merge_runin',    50, 'merge run-in cycles' );
rp::const( 'merge_runover',   0, 'merge run-over cycles' );
rp::const( 'merge_runout',   10, 'merge run-out cycles' );
rp::const( 'merge_runup',   100, 'merge run-up cycles' );
rp::const( 'merge_min',      50, 'merge minimum time (cycles)' );
rp::const( 'merge_max',     300, 'merge maximum time (cycles)' );
rp::const( 'merge_clear',   200, 'merge clear time (cycles)' );
rp::const( 'merge_choose',   40, 'merge choose time (cycles)' );
rp::const( 'merge_wait',     10, 'merge wait time (cycles)' );
rp::const( 'merge_dead',      0, 'merge dead time (cycles)' );
rp::const( 'merge_jam',     300, 'merge jam time (cycles)' );
rp::const( 'merge_slug',     10, 'merge slug-release delay time (cycles)' );


# _merge_spur@  ( dp -- spur-dp )
#    get the dp associated with the merge spur
@code=qw(
   REG_LEFT swap dp.register.get
);
Trk::ex_compile( '_merge_spur@', 'merge get spur', \@code );


# _merge_control@  ( dp -- control-dp )
#    get the dp associated with the merge input control
@code=qw(
   REG_CONTROL swap dp.register.get
);
Trk::ex_compile( '_merge_control@', 'merge get control', \@code );


# _merge_input@  ( dp -- input )
#    get the current input direction
@code=qw(
   REG_CHOICE  swap _merge_control@  dp.register.get
);
Trk::ex_compile( '_merge_input@', 'merge get input direction', \@code );


# _merge_input_dest@  ( dp -- destination )
#    get the destination dp for the current input direction
@code=qw(
   >r

   r@ _merge_input@  r@ _zone_dest@

   r> drop
);
Trk::ex_compile( '_merge_input_dest@', 'merge get input destination', \@code );


# _merge_cycle  ( dp -- )
#    cycle to the next input direction for availability
#         none
#           |
#           v
#       straight --> left --> right --> straight --> ...
@code=qw(
   >r

   DIR_STRAIGHT
   r@ _merge_input@  DIR_STRAIGHT =  if
      REG_LEFT  r@ dp.register.get 0 > if drop DIR_LEFT  else
      REG_RIGHT r@ dp.register.get 0 > if drop DIR_RIGHT then then
   else
      r@ _merge_input@  DIR_LEFT =  if
         REG_RIGHT r@ dp.register.get 0 > if drop DIR_RIGHT then
      then
   then
   REG_CHOICE  r@ _merge_control@  dp.register.set

   r> drop
);
Trk::ex_compile( '_merge_cycle', 'merge cycle input direction', \@code );


# _merge_init  ( dp -- )
#    initialize the input direction for availability, based on default:
#      DIR_LAST  --> choose same input (do nothing)
#      DIR_NONE  --> round-robin (_merge_cycle)
#      otherwise --> set input to default
@code=qw(
   >r

   REG_DEFAULT r@ dp.register.get
   dup DIR_LAST = if
      drop
   else
      dup DIR_NONE = if
         r@ _merge_cycle
         drop
      else
         REG_CHOICE  r@ _merge_control@  dp.register.set
      then
   then

   r> drop
);
Trk::ex_compile( '_merge_init', 'merge initialize input direction', \@code );


# _merge_unavailable  ( dp -- )
#    make the zone unavailable from all input directions
@code=qw(
   >r

   r@ on   REG_LEFT r@ _zone_dp_on   REG_RIGHT r@ _zone_dp_on

   r> drop
);
Trk::ex_compile( '_merge_unavailable', 'make all inputs unavailable', \@code );


# _merge_motors_stop  ( dp -- )
#    turn off motors from all inputs
@code=qw(
   >r

   REG_MOTOR  r@                            _zone_dp_off
   REG_MOTOR  REG_LEFT  r@ dp.register.get  _zone_dp_off
   REG_MOTOR  REG_RIGHT r@ dp.register.get  _zone_dp_off

   r> drop
);
Trk::ex_compile( '_merge_motors_stop', 'stop all motors', \@code );


# _merge_choose  ( dp -- )
#    manage input direction availability and handle the selection if
#    a box is loaded
#
#       set all inputs unavailable
#       tick
#
#       if state INIT
#          choose initial input
#          set state CHOOSE
#       if state IDLE
#          if elapsed > wait
#             cycle input
#             set state CHOOSE
#       if state CHOOSE
#          input off
#          if elapsed > choose
#             set state IDLE
#
#       if input->box not NONE
#          set choice to input
#          set state INIT
#       else
#          set choice to NONE
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_control@ _zone_tick

   r@ _merge_control@ _zone_state@  STATE_INIT = if
      r@ _merge_init
      STATE_CHOOSE r@ _merge_control@ _zone_state!
   then

   r@ _merge_control@ _zone_state@  STATE_IDLE = if
      r@ _merge_control@ _zone_elapsed@  REG_TM_WAIT r@ _zone_rp@  > if
         r@ _merge_cycle
         STATE_CHOOSE r@ _merge_control@ _zone_state!
      then
   then

   r@ _merge_control@ _zone_state@  STATE_CHOOSE = if
      r@ _merge_control@ _zone_elapsed@  REG_TM_CHOOSE r@ _zone_rp@  > if
         STATE_IDLE r@ _merge_control@ _zone_state!
      else
         r@ _merge_input_dest@  dup 0 > if  off  else  drop  then
      then
   then


   DIR_NONE REG_CHOICE r@ dp.register.set
   r@ _merge_input_dest@  0 > if
      r@ _merge_input_dest@ _zone_box@ BOX_NONE = not if
         r@ _merge_input@  REG_CHOICE r@ dp.register.set
         STATE_INIT r@ _merge_control@ _zone_state!
      then
   then

   r> drop
);
Trk::ex_compile( '_merge_choose', 'merge choose', \@code );


# _merge_choice_dest@  ( dp -- destination )
#    get the destination for the chosen direction
@code=qw(
   >r

   REG_CHOICE r@ dp.register.get  r@ _zone_dest@

   r> drop
);
Trk::ex_compile('_merge_choice_dest@','merge get choice destination',\@code);


# _merge_box_load  ( dp -- )
#    load the box from the chosen input to the zone
@code=qw(
   >r
 
   r@ _merge_choice_dest@  dup r@ = if drop else
      dup _zone_box@  r@ _zone_box!
      BOX_NONE swap _zone_box!
   then

   r> drop
);
Trk::ex_compile( '_merge_box_load', 'merge load box from input', \@code );


# _merge_state_init  ( dp -- )
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
   BOX_NONE r@ _merge_spur@ _zone_box!

   r@ _zone_run? if
      STATE_RUNUP r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_merge_state_init', 'merge state init', \@code );


# _merge_state_runup  ( dp -- )
#    run up from init to a well-defined state
#
#       zone on
#       motors run + tick
#       if elapsed > runup
#          goto IDLE
#       if eye blocked
#          set box ANON
#          goto FULL
#
@code=qw(
   >r

   r@ _merge_unavailable
   REG_MOTOR r@ _zone_dp_on
   REG_MOTOR r@ _merge_spur@ _zone_dp_on
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
Trk::ex_compile( '_merge_state_runup', 'merge state run-up', \@code );

# _merge_state_idle  ( dp -- )
#    wait for incoming box
#
#       motor stop + tick
#       choose input
#       if box not NONE
#          load box from choice
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

   r@ _merge_unavailable
   r@ _merge_motors_stop
   r@ _zone_tick
   REG_GO r@ _zone_dp_off

   r@ _merge_choose

   REG_CHOICE r@ dp.register.get  DIR_NONE = not if
      r@ _merge_box_load
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
Trk::ex_compile( '_merge_state_idle', 'merge state idle', \@code );

# _merge_state_fill  ( dp -- )
#    bring a box into the zone
#
#       zone on
#       choice->motor run + tick
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

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR  r@ _merge_choice_dest@  _zone_dp_on
   r@ _zone_tick

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
Trk::ex_compile( '_merge_state_fill', 'merge state fill (unblocked)', \@code );

# _merge_state_fill_x  ( dp -- )
#    bring a box into the zone while the eye is blocked
#
#       zone on
#       choice->motor run + tick
#       if elapsed > runover
#          goto FULL
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_MOTOR  r@ _merge_choice_dest@  _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_RUNOVER r@ _zone_rp@  > if
      STATE_FULL r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_merge_state_fill_x', 'merge state fill (blocked)', \@code );

# _merge_state_full  ( dp -- )
#    hold a box and monitor downstream availability
#
#       zone on
#       motors stop + tick
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

   r@ _merge_unavailable
   r@ _merge_motors_stop
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
Trk::ex_compile( '_merge_state_full', 'merge state full', \@code );

# _merge_state_drain_x  ( dp -- )
#    transport the box out of the zone while the eye is blocked
#
#       zone on
#       motor run with next
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
   REG_GO r@ _zone_dp_off

   r@ _zone_motor_next

   REG_NEXT r@ dp.register.get  _zone_box@  BOX_NONE =
   REG_NEXT r@ dp.register.get  _zone_state@  STATE_IDLE =  and if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   then

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
Trk::ex_compile( '_merge_state_drain_x', 'merge state drain (blocked)',
      \@code );

# _merge_state_drain  ( dp -- )
#    transport the box out of the zone
#
#       zone on
#       choice->motor run + tick
#       if box not NONE
#          goto FILL
#       else if elapsed > runout
#          goto IDLE
#
@code=qw(
   >r

   r@ _merge_unavailable
   r@ _merge_motors_stop
   REG_GO r@ _zone_dp_off

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
Trk::ex_compile( '_merge_state_drain', 'merge state drain (unblocked)',
      \@code );

# _merge_state_fault  ( dp -- )
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
   REG_GO r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_merge_state_fault', 'merge state fault', \@code );

# _merge_machine  ( dp -- )
#    execute merge zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT   = if r@ _merge_state_fault   then
      r@ _zone_state@  STATE_INIT    = if r@ _merge_state_init    then
      r@ _zone_state@  STATE_RUNUP   = if r@ _merge_state_runup   then
      r@ _zone_state@  STATE_IDLE    = if r@ _merge_state_idle    then
      r@ _zone_state@  STATE_FILL    = if r@ _merge_state_fill    then
      r@ _zone_state@  STATE_FILL_X  = if r@ _merge_state_fill_x  then
      r@ _zone_state@  STATE_FULL    = if r@ _merge_state_full    then
      r@ _zone_state@  STATE_DRAIN_X = if r@ _merge_state_drain_x then
      r@ _zone_state@  STATE_DRAIN   = if r@ _merge_state_drain   then

      r@ _zone_set_output

   then

   r> drop
);
Trk::ex_compile( '_merge_machine', 'merge state machine', \@code );


# create merge zone
sub addMerge {
   my ($name, $data) = @_;
   my $val;

   # create the base zone
   my $dp = createZone( $name, 'merge zone ' . $name, $data );

   # set additional registers from input data
   if (($val = $data->{default}) ne '')
      { Trk::dp_registerset( $dp, REG_DEFAULT, $val ); }

   # create additional controls
   Trk::dp_registerset( $dp, REG_CONTROL,
         dp::virtual( $name . '_control', $name . ' input control' ) );

   # set timers
   my $rp;
   if (($val = $data->{runin}) ne '') {
      $rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );
   } else { $rp = rp::handle( 'merge_runin' ); }
   Trk::dp_registerset( $dp, REG_RUNIN, $rp );

   if (($val = $data->{runover}) ne '') {
      $rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );
   } else { $rp = rp::handle( 'merge_runover' ); }
   Trk::dp_registerset( $dp, REG_RUNOVER, $rp );

   if (($val = $data->{runout}) ne '') {
      $rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );
   } else { $rp = rp::handle( 'merge_runout' ); }
   Trk::dp_registerset( $dp, REG_RUNOUT, $rp );

   if (($val = $data->{runup}) ne '') {
      $rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );
   } else { $rp = rp::handle( 'merge_runup' ); }
   Trk::dp_registerset( $dp, REG_RUNUP, $rp );

   if (($val = $data->{min}) ne '') {
      $rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );
   } else { $rp = rp::handle( 'merge_min' ); }
   Trk::dp_registerset( $dp, REG_TM_MIN, $rp );

   if (($val = $data->{max}) ne '') {
      $rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );
   } else { $rp = rp::handle( 'merge_max' ); }
   Trk::dp_registerset( $dp, REG_TM_MAX, $rp );

   if (($val = $data->{clear}) ne '') {
      $rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );
   } else { $rp = rp::handle( 'merge_clear' ); }
   Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );

   if (($val = $data->{choose}) ne '') {
      $rp = rp::const( $name . '_choose', $val, $name.' choose time (cycles)' );
   } else { $rp = rp::handle( 'merge_choose' ); }
   Trk::dp_registerset( $dp, REG_TM_CHOOSE, $rp );

   if (($val = $data->{wait}) ne '') {
      $rp = rp::const( $name . '_wait', $val, $name . ' wait time (cycles)' );
   } else { $rp = rp::handle( 'merge_wait' ); }
   Trk::dp_registerset( $dp, REG_TM_WAIT, $rp );

   if (($val = $data->{dead}) ne '') {
      $rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );
   } else { $rp = rp::handle( 'merge_dead' ); }
   Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );

   if (($val = $data->{jam}) ne '') {
      $rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );
   } else { $rp = rp::handle( 'merge_jam' ); }
   Trk::dp_registerset( $dp, REG_TM_JAM, $rp );

   # set spur registers
   my $spur = dp::virtual( $name . '_spur', $name . ' spur' );
   Trk::dp_registerset( $dp, REG_LEFT, $spur );

   if (($val = $data->{spur_eye}) ne '')
      { Trk::dp_registerset( $spur, REG_EYE, dp::handle( $val ) ); }
   if (($val = $data->{spur_motor}) ne '') {
      Trk::dp_registerset( $spur, REG_MOTOR, dp::handle( $val ) );
   }

   Trk::dp_registerset( $spur, REG_RUNIN,
         Trk::dp_registerget( $dp, REG_RUNIN ) );

   # run the state machine
   my @stub = ( $name, '_merge_machine' );
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
