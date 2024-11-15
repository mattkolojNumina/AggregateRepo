#!/bin/perl -I.
#
# zonetrak_oneshot : one-shot output using ZoneTRAK structures
#
# addOneShot( name, {data} )
#
# accepts the following elements:
#    desc    - description (default: '<name> one-shot')
#    output  - the output dp controlled by the one-shot
#    run     - area run control (default: always run)
#    fault   - area fault control (default: never fault)
#    reset   - area reset control (clears local fault)
#    timer   - state machine timer (default: 10ms)
#
# creates the following dp's for external control:
#    <name>_fault - declare the zone faulted
#    <name>_debug - disable all state-machine controls
#
# creates the following rp's for external control:
#    <name>_offset   - time before activating output (cycles)
#    <name>_duration - time for output to remain activated (cycles)
#


require Trk;
require dp;
require rp;
require trakbase;

require "zonetrak_common.pl";


my @code;


# _oneshot_state_init  ( dp -- )
#    initialize the zone state machine
#
#       zone off
#       output off
#       if zone running
#          goto IDLE
#
@code=qw(
   >r

   r@ off
   REG_OUTPUT r@ _zone_dp_off

   r@ _zone_run? if
      STATE_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_oneshot_state_init', 'one-shot state init', \@code );


# _oneshot_state_idle  ( dp -- )
#    monitor zone to activate output
#
#       output off
#       if zone on
#          goto DEACT
#
@code=qw(
   >r

   REG_OUTPUT r@ _zone_dp_off

   r@ _zone_run?
   r@ dp.value.get  and if
      STATE_DEACT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_oneshot_state_idle', 'one-shot state idle', \@code );

# _oneshot_state_deact  ( dp -- )
#     deactivate the output prior to activation
#
#       output off + tick
#       if elapsed > min (offset)
#          goto ACTIVE
#       if zone off
#          goto IDLE
#
@code=qw(
   >r

   REG_OUTPUT r@ _zone_dp_off
   r@ _zone_tick

   r@ _zone_elapsed@  REG_TM_MIN r@ _zone_rp@  > if
      STATE_ACTIVE r@ _zone_state!
   then

   r@ dp.value.get not if
      STATE_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_oneshot_state_deact', 'one-shot state deactivate', \@code );

# _oneshot_state_active  ( dp -- )
#     activate the output
#
#       output on + tick
#       if elapsed > max (duration)  or if zone off
#          zone off
#          output off
#          goto IDLE
#
@code=qw(
   >r

   REG_OUTPUT r@ _zone_dp_on
   r@ _zone_tick

   r@ _zone_elapsed@  REG_TM_MAX r@ _zone_rp@  >
   r@ dp.value.get not  or if
      r@ off
      REG_OUTPUT r@ _zone_dp_off
      STATE_IDLE r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_oneshot_state_active', 'one-shot state activate', \@code );

# _oneshot_state_fault  ( dp -- )
#    halt the zone until fault clears
#
#       zone off
#       output off
#       if fault clear
#          goto INIT
#
@code=qw(
   >r

   r@ off
   REG_OUTPUT r@ _zone_dp_off

   r@ _zone_fault? not if
      STATE_INIT r@ _zone_state!
   then

   r> drop
);
Trk::ex_compile( '_oneshot_state_fault', 'one-shot state fault', \@code );

# _oneshot_machine  ( dp -- )
#    execute transport zone state machine
@code=qw(
   >r

   REG_DEBUG r@ _zone_dp? not if

      r@ _zone_fault? if  STATE_FAULT r@ _zone_state!  then

      r@ _zone_state@  STATE_FAULT  = if r@ _oneshot_state_fault  then
      r@ _zone_state@  STATE_INIT   = if r@ _oneshot_state_init   then
      r@ _zone_state@  STATE_IDLE   = if r@ _oneshot_state_idle   then
      r@ _zone_state@  STATE_DEACT  = if r@ _oneshot_state_deact  then
      r@ _zone_state@  STATE_ACTIVE = if r@ _oneshot_state_active then

   then

   r> drop
);
Trk::ex_compile( '_oneshot_machine', 'one-shot state machine', \@code );


# create one-shot
sub addOneShot {
   my ($name, $data) = @_;
   my @stub;

   my $desc = $data->{'desc'};
   if ($desc eq '') { $desc = $name . ' one-shot'; }

   # create the zone
   my $dp = dp::virtual( $name, $desc );

   # create controls
   Trk::dp_registerset( $dp, REG_ZONEFAULT,
         dp::virtual( $name . '_fault', $name . ' fault' ) );
   Trk::dp_registerset( $dp, REG_DEBUG,
         dp::virtual( $name . '_debug', $name . ' debug' ) );
   Trk::dp_registerset( $dp, REG_TM_MIN,
         rp::const( $name . '_offset', 0, $name . ' offset (cycles)' ) );
   Trk::dp_registerset( $dp, REG_TM_MAX,
         rp::const( $name . '_duration', 1, $name . ' duration (cycles)' ) );

   # set registers from input data
   my $val;
   if (($val = $data->{output}) ne '')
      { Trk::dp_registerset( $dp, REG_OUTPUT, dp::handle( $val ) ); }
   if (($val = $data->{run}) ne '')
      { Trk::dp_registerset( $dp, REG_RUN,    dp::handle( $val ) ); }
   if (($val = $data->{fault}) ne '')
      { Trk::dp_registerset( $dp, REG_FAULT,  dp::handle( $val ) ); }
   if (($val = $data->{reset}) ne '') {
      Trk::dp_registerset(   $dp, REG_RESET,  dp::handle( $val ) );
      @stub = ( $name . '_fault', 'off' );
      Trk::ex_compile( $name . '_fault_clr', $name . ' fault clear', \@stub );
      trakbase::leading( $val, $name . '_fault_clr',
            'clear ' . $name . ' fault' );
   }

   # run the state machine
   @stub = ( $name, '_oneshot_machine' );
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
