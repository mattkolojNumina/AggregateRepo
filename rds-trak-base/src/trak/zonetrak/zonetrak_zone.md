# zonetrak_zone.pl

## Dependencies

| #!/bin/perl -I. | Includes current directory in Perl compilation |
| --- | --- |
| require Trk; |     |
| require dp; |     |
| require rp; |     |
| require trakbase; |     |
| require "zonetrak_common.pl"; | Includes core zonetrak functions |

## Zone Components - addZone( name, {data} )

### Modes

| **REG_MODE** | **Description** | **Activated Hashes** |
| --- | --- | --- |
| create_mode | Generate Box Number for Carton in zone | first, last |
| lax_mode | Ignore Faults/Jams \[False\] | \-  |
| slug_mode | Start DRAIN on next zone DRAIN (instead of FILL) | slug |
| stop_mode | Stop boxes in zone until zone_go is asserted | \-  |
| beltfeed_mode | loads box in pe, runs a time in idle | runidle |
| pushout_mode | Instead of DRAIN_X timeout, duty cycle motor | pushout_period, pushout_duty |

### Controls

| **Hash** | **Description** | **Default** | **Register** |
| --- | --- | --- | --- |
| desc | Description for zone | generic | \-  |
| next | Downstream zone or dp to decide release | next dp on stack | REG_NEXT |
| eye | Base zone photoeye to decide presence | pe&lt;name&gt; | REG_EYE |
| motor | Base zone motor to activate for transport | mdr&lt;name&gt; | REG_MOTOR |
| run | Area run control (ie. cp1_run) | 1   | REG_RUN |
| fault | Area fault control (ie. cp1_fault) | 0   | REG_FAULT |
| reset | Area reset control (ie. cp1_reset) – clears REG_ZONEFAULT | \-  | REG_RESET |
| first | Lower bound of box range | 1   | REG_FIRST |
| last | Upper bound of box range | 999 | REG_LAST |
| output | ? OUTPUT DP ? | \-  | REG_OUTPUT |
| out_states | Bitmask set to OUTPUT DP | \-  | REG_OUT_STATES |
| timer | State machine timer | 10ms spread | REG_TIMER |

### Timers

| **Timer** | **Description** | **Errors** | **Register** | **Default** |
| --- | --- | --- | --- | --- |
| zone_min | Minimum time needed for a box to get to photoeye | EARLY | REG_TM_MIN | 50  |
| zone_max | Maximum time needed for box to get to photoeye | MISSING | REG_TM_MAX | 0   |
| zone_clear | ? max time with eye clear ? | REMOVED | REG_TM_CLEAR | 10  |
| zone_runin | Time to run box into zone with no photoeye \[FILL\] | \-  | REG_RUNIN | 100 |
| zone_runover | Time to run box through photoeye \[FILL_X\] | \-  | REG_RUNOVER | 500 |
| zone_runout | Time to run box past photoeye \[DRAIN\] | \-  | REG_RUNOUT | 50  |
| zone_runup | Maximum time to run motors during startup \[RUNUP\] | \-  | REG_RUNUP | 300 |
| zone_runidle | Time to run when IDLE when in beltfeed | \-  | \-  | 200 |
| zone_dead | Time for photoeye to be dead timed after state change | \-  | REG_TM_DEAD | 0   |
| zone_jam | Time needed for zone to declare a jam | \-  | REG_TM_JAM | 300 |
| zone_slug | Minimum time before slug releasing | \-  | \-  | 10  |
| pushout_period | Period of which pushout_duty will be active | \-  | \-  | 1000 |
| pushout_duty | Time out of pushout_period to run motor | \-  | \-  | 300 |

### Virtual DP Creation

| **DP** | **Description** | **Register** |
| --- | --- | --- |
| &lt;name&gt;\_fault | Used to declare zone faults | REG_ZONEFAULT |
| &lt;name&gt;\_debug | Used to detach zone form state machine for control | REG_DEBUG |
| &lt;name&gt;\_hold | Hold boxes in zone | REG_HOLD |
| &lt;name&gt;\_go | Used in zones with stop_mode to release carton | REG_GO |

## States

### Code Snippets

#### \_zone_state_init

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_off

REG_GO r@ \_zone_dp_off

&nbsp;

BOX_NONE r@ \_zone_box!

&nbsp;

r@ \_zone_run? if

r@ \_zone_reset_msg

STATE_RUNUP r@ \_zone_state!

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_init', 'zone state init', \\@code );

#### \_zone_state_runup

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_on

r@ \_zone_tick

REG_GO r@ \_zone_dp_off

&nbsp;

r@ \_zone_elapsed@ REG_RUNUP r@ \_zone_rp@ > if

STATE_IDLE r@ \_zone_state!

then

&nbsp;

REG_EYE r@ \_zone_dp? if

MODE_CREATE r@ \_zone_mode? if

r@ \_zone_box_new

else

BOX_ANON r@ \_zone_box!

then

STATE_FULL r@ \_zone_state!

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_runup', 'zone state run-up', \\@code );

#### \_zone_state_idle

@code=qw(

\>r

&nbsp;

r@ \_zone_run? not r@ dp.value.set

&nbsp;

r@ \_zone_tick

REG_GO r@ \_zone_dp_off

&nbsp;

MODE_IDLERUN r@ \_zone_mode?

r@ \_zone_run? and if

REG_MOTOR r@ \_zone_dp_on

else

MODE_BELTFEED r@ \_zone_mode? if

r@ \_zone_run?

r@ \_zone_elapsed@ zone_runidle rp.value.get < and if

REG_MOTOR r@ \_zone_dp_on

else

REG_MOTOR r@ \_zone_dp_off

then

else

REG_MOTOR r@ \_zone_dp_off

then

then

&nbsp;

r@ \_zone_box@ BOX_NONE = not if

&nbsp;

r@ \_zone_box@ BOX_ANON = MODE_CREATE r@ \_zone_mode? and if

r@ \_zone_box_new

then

&nbsp;

STATE_FILL r@ \_zone_state!

then

&nbsp;

REG_EYE r@ \_zone_dp? if

r@ \_zone_elapsed@ REG_TM_MIN r@ \_zone_rp@ <

MODE_LAX r@ \_zone_mode? not and if

ERR_EARLY r@ \_zone_error_msg

REG_ZONEFAULT r@ \_zone_dp_on

STATE_FAULT r@ \_zone_state!

else

MODE_CREATE r@ \_zone_mode? if

r@ \_zone_box_new

else

BOX_ANON r@ \_zone_box!

ERR_UNEXPECTED r@ \_zone_error_msg

then

STATE_FULL r@ \_zone_state!

then

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_idle', 'zone state idle', \\@code );

#### \_zone_state_fill

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_on

r@ \_zone_tick

&nbsp;

MODE_BELTFEED r@ \_zone_mode? if

r@ \_zone_box@

REG_EYE r@ dp.register.get dp.carton.set

then

&nbsp;

REG_EYE r@ dp.register.get 0 > if

REG_EYE r@ \_zone_dp? if

r@ \_zone_elapsed@ REG_TM_MIN r@ \_zone_rp@ <

MODE_LAX r@ \_zone_mode? not and if

ERR_EARLY r@ \_zone_error_msg

REG_ZONEFAULT r@ \_zone_dp_on

STATE_FAULT r@ \_zone_state!

else

STATE_FILL_X r@ \_zone_state!

then

else

r@ \_zone_elapsed@ REG_TM_MAX r@ \_zone_rp@ > if

ERR_MISSING r@ \_zone_error_msg

STATE_INIT r@ \_zone_state!

then

then

else

r@ \_zone_elapsed@ REG_RUNIN r@ \_zone_rp@ > if

STATE_FULL r@ \_zone_state!

then

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_fill', 'zone state fill (unblocked)', \\@code );

#### \_zone_state_fill_x

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_on

r@ \_zone_tick

&nbsp;

r@ \_zone_elapsed@ REG_RUNOVER r@ \_zone_rp@ > if

STATE_FULL r@ \_zone_state!

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_fill_x', 'zone state fill (blocked)', \\@code );

#### \_zone_state_full

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_off

r@ \_zone_tick

&nbsp;

r@ \_zone_run?

r@ \_zone_next_avail? and

REG_HOLD r@ \_zone_dp? not and

MODE_STOP r@ \_zone_mode? not REG_GO r@ \_zone_dp? or and if

r@ \_zone_box_pass

STATE_DRAIN_X r@ \_zone_state!

then

&nbsp;

REG_EYE r@ \_zone_dp_not? if

r@ \_zone_elapsed@ REG_TM_CLEAR r@ \_zone_rp@ > if

ERR_REMOVED r@ \_zone_error_msg

STATE_INIT r@ \_zone_state!

then

else

r@ \_zone_timer_clr

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_full', 'zone state full', \\@code );

#### \_zone_state_drain_x

@code=qw(

\>r

&nbsp;

r@ \_zone_slug_release? not r@ dp.value.set

REG_GO r@ \_zone_dp_off

r@ \_zone_tick

MODE_PUSHOUT r@ \_zone_mode? if

r@ \_zone_elapsed@ pushout_period rp.value.get %

pushout_duty rp.value.get < if

REG_MOTOR r@ \_zone_dp_on

else

REG_MOTOR r@ \_zone_dp_off

then

else

r@ \_zone_motor_next

then

&nbsp;

REG_NEXT r@ dp.register.get \_zone_box@ BOX_NONE =

REG_NEXT r@ dp.register.get \_zone_state@ STATE_IDLE = and if

BOX_ANON REG_NEXT r@ dp.register.get \_zone_box!

then

&nbsp;

REG_EYE r@ \_zone_dp? if

MODE_PUSHOUT r@ \_zone_mode? not if

r@ \_zone_elapsed@ REG_TM_JAM r@ \_zone_rp@ > if

MODE_LAX r@ \_zone_mode? not if

ERR_STUCK r@ \_zone_error_msg

REG_ZONEFAULT r@ \_zone_dp_on

STATE_FAULT r@ \_zone_state!

else

STATE_INIT r@ \_zone_state!

then

then

then

else

r@ \_zone_elapsed@ REG_TM_DEAD r@ \_zone_rp@ > if

STATE_DRAIN r@ \_zone_state!

then

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_drain_x', 'zone state drain (blocked)', \\@code );

#### \_zone_state_drain

@code=qw(

\>r

&nbsp;

r@ \_zone_slug_release? not r@ dp.value.set

REG_GO r@ \_zone_dp_off

r@ \_zone_tick

&nbsp;

r@ \_zone_motor_next

&nbsp;

REG_NEXT r@ dp.register.get \_zone_box@ BOX_NONE =

REG_NEXT r@ dp.register.get \_zone_state@ STATE_IDLE = and if

BOX_ANON REG_NEXT r@ dp.register.get \_zone_box!

then

&nbsp;

r@ \_zone_box@ BOX_NONE = not if

r@ \_zone_box@ BOX_ANON = MODE_CREATE r@ \_zone_mode? and if

r@ \_zone_box_new

then

STATE_FILL r@ \_zone_state!

else

r@ \_zone_elapsed@ REG_RUNOUT r@ \_zone_rp@ > if

STATE_IDLE r@ \_zone_state!

then

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_drain', 'zone state drain (unblocked)', \\@code );

#### \_zone_state_fault

@code=qw(

\>r

&nbsp;

r@ on

REG_MOTOR r@ \_zone_dp_off

REG_GO r@ \_zone_dp_off

&nbsp;

r@ \_zone_fault? not if

STATE_INIT r@ \_zone_state!

then

&nbsp;

r> drop

);

Trk::ex_compile( '\_zone_state_fault', 'zone state fault', \\@code );

### Base Functions

#### State Machine (\_zone_machine)

<table><tbody><tr><th><p># _zone_machine ( dp -- )</p><p># execute transport zone state machine</p><p>@code=qw(</p><p>&gt;r</p><p>&nbsp;</p><p>REG_DEBUG r@ _zone_dp? not if</p><p>&nbsp;</p><p>r@ _zone_fault? if STATE_FAULT r@ _zone_state! then</p><p>&nbsp;</p><p>r@ _zone_state@ STATE_FAULT = if r@ _zone_state_fault then</p><p>r@ _zone_state@ STATE_INIT = if r@ _zone_state_init then</p><p>r@ _zone_state@ STATE_RUNUP = if r@ _zone_state_runup then</p><p>r@ _zone_state@ STATE_IDLE = if r@ _zone_state_idle then</p><p>r@ _zone_state@ STATE_FILL = if r@ _zone_state_fill then</p><p>r@ _zone_state@ STATE_FILL_X = if r@ _zone_state_fill_x then</p><p>r@ _zone_state@ STATE_FULL = if r@ _zone_state_full then</p><p>r@ _zone_state@ STATE_DRAIN_X = if r@ _zone_state_drain_x then</p><p>r@ _zone_state@ STATE_DRAIN = if r@ _zone_state_drain then</p><p>&nbsp;</p><p>r@ _zone_set_output</p><p>&nbsp;</p><p>then</p><p>&nbsp;</p><p>r&gt; drop</p><p>);</p><p>Trk::ex_compile( '_zone_machine', 'zone state machine', \@code );</p></th><th><p>If not REG_DEBUG</p><ul><li>If _zone_fault? then dp(STATE) = STATE_FAULT</li><li>If dp(STATE) = STATE_FAULT then zone_state_fault (execute state)</li><li>If dp(STATE) = STATE_INIT then zone_state_init (execute state)</li><li>If dp(STATE) = STATE_RUNUP then zone_state_runup (execute state)</li><li>If dp(STATE) = STATE_IDLE then zone_state_idle (execute state)</li><li>If dp(STATE) = STATE_FILL then zone_state_fill (execute state)</li><li>If dp(STATE) = STATE_FILL_X then zone_state_fill_x (execute state)</li><li>If dp(STATE) = STATE_FULL then zone_state_full (execute state)</li><li>If dp(STATE) = STATE_DRAIN_X then zone_state_drain_x (execute state)</li><li>If dp(STATE) = STATE_DRAIN then zone_state_drain (execute state)0</li><li>dp(REG_OUTPUT) = OUT_STATE</li></ul><p>Create word _zone_machine</p></th></tr></tbody></table>

#### addZone

<table><tbody><tr><th><p># create transport zone</p><p>sub addZone {</p><p>my ($name, $data) = @_;</p><p>my $val;</p><p>&nbsp;</p><p># create the base zone</p><p>my $dp = createZone( $name, 'transport zone ' . $name, $data );</p><p>&nbsp;</p><p># set timers</p><p>my $rp;</p><p>if (($val = $data-&gt;{runin}) ne '') {</p><p>$rp = rp::const( $name . '_runin', $val, $name . ' run-in cycles' );</p><p>} else { $rp = rp::handle( 'zone_runin' ); }</p><p>Trk::dp_registerset( $dp, REG_RUNIN, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{runover}) ne '') {</p><p>$rp = rp::const( $name . '_runover', $val, $name . ' run-over cycles' );</p><p>} else { $rp = rp::handle( 'zone_runover' ); }</p><p>Trk::dp_registerset( $dp, REG_RUNOVER, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{runout}) ne '') {</p><p>$rp = rp::const( $name . '_runout', $val, $name . ' run-out cycles' );</p><p>} else { $rp = rp::handle( 'zone_runout' ); }</p><p>Trk::dp_registerset( $dp, REG_RUNOUT, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{runup}) ne '') {</p><p>$rp = rp::const( $name . '_runup', $val, $name . ' run-up cycles' );</p><p>} else { $rp = rp::handle( 'zone_runup' ); }</p><p>Trk::dp_registerset( $dp, REG_RUNUP, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{min}) ne '') {</p><p>$rp = rp::const( $name . '_min', $val, $name . ' minimum time (cycles)' );</p><p>} else { $rp = rp::handle( 'zone_min' ); }</p><p>Trk::dp_registerset( $dp, REG_TM_MIN, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{max}) ne '') {</p><p>$rp = rp::const( $name . '_max', $val, $name . ' maximum time (cycles)' );</p><p>} else { $rp = rp::handle( 'zone_max' ); }</p><p>Trk::dp_registerset( $dp, REG_TM_MAX, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{clear}) ne '') {</p><p>$rp = rp::const( $name . '_clear', $val, $name . ' clear time (cycles)' );</p><p>} else { $rp = rp::handle( 'zone_clear' ); }</p><p>Trk::dp_registerset( $dp, REG_TM_CLEAR, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{dead}) ne '') {</p><p>$rp = rp::const( $name . '_dead', $val, $name . ' dead time (cycles)' );</p><p>} else { $rp = rp::handle( 'zone_dead' ); }</p><p>Trk::dp_registerset( $dp, REG_TM_DEAD, $rp );</p><p>&nbsp;</p><p>if (($val = $data-&gt;{jam}) ne '') {</p><p>$rp = rp::const( $name . '_jam', $val, $name . ' jam time (cycles)' );</p><p>} else { $rp = rp::handle( 'zone_jam' ); }</p><p>Trk::dp_registerset( $dp, REG_TM_JAM, $rp );</p><p>&nbsp;</p><p># run the state machine</p><p>my @stub = ( $name, '_zone_machine' );</p><p>Trk::ex_compile( $name . '_machine', $name . ' state machine', \@stub );</p><p>&nbsp;</p><p>my $timer = spread_timer();</p><p>if (($val = $data-&gt;{timer}) ne '') { $timer = $val; }</p><p>trakbase::leading( $timer, $name . '_machine',</p><p>'run ' . $name . ' state machine' );</p><p>trakbase::trailing( $timer, $name . '_machine',</p><p>'run ' . $name . ' state machine' );</p><p>&nbsp;</p><p>my $tmp = pop( @stub );</p><p>}</p><p>&nbsp;</p><p>&nbsp;</p><p># return a true value to indicate successful initialization</p><p>1;</p></th><th><p>Declare 2 scalars: name, data store inputs respectively</p><p>Declare scalar val</p><p>Declare scalar dp and assign return value of createZone(name, desc, data)</p><p>Declare scalar rp</p><p>val = runin</p><p>If val != ‘’</p><ul><li>rp = const(name_runin) and name_runin = val</li></ul><p>Else</p><ul><li>rp = zone_runin</li><li>dp(REG_RUNIN) = rp</li></ul><p>(val = runover) If val != ‘’</p><ul><li>rp = const(name_runover) and name_runover = val</li></ul><p>Else</p><ul><li>rp = zone_runover</li><li>dp(REG_RUNOVER) = rp</li></ul><p>(val = runout) If val != ‘’</p><ul><li>rp = const(name_runout) and name_runout = val</li></ul><p>Else</p><ul><li>rp = zone_runout</li><li>dp(REG_RUNOUT) = rp</li></ul><p>(val = runup) If val != ‘’</p><ul><li>rp = const(name_runup) and name_runup = val</li></ul><p>Else</p><ul><li>rp = zone_runup</li><li>dp(REG_RUNUP) = rp</li></ul><p>(val = min) If val != ‘’</p><ul><li>rp = const(name_min) and name_min = val</li></ul><p>Else</p><ul><li>rp = zone_min</li><li>dp(REG_TM_MIN) = rp</li></ul><p>(val = max) If val != ‘’</p><ul><li>rp = const(name_max) and name_max = val</li></ul><p>Else</p><ul><li>rp = zone_max</li><li>dp(REG_TM_MAX) = rp</li></ul><p>(val = clear) If val != ‘’</p><ul><li>rp = const(name_clear) and name_clear = val</li></ul><p>Else</p><ul><li>rp = zone_clear</li><li>dp(REG_TM_CLEAR) = rp</li></ul><p>(val = dead) If val != ‘’</p><ul><li>rp = const(name_dead) and name_dead = val</li></ul><p>Else</p><ul><li>rp = zone_dead</li><li>dp(REG_TM_DEAD) = rp</li></ul><p>(val = jam) If val != ‘’</p><ul><li>rp = const(name_jam) and name_clear = val</li></ul><p>Else</p><ul><li>rp = zone_jam</li><li>dp(REG_TM_JAM) = rp</li></ul><p>Declare array stub = name_zone_machine</p><p>Create word name_machine = stub</p><p>Declare scalar timer = assign spread timer</p><p>(val = timer) If val != ‘’</p><ul><li>Execute name_machine on leading and trailing edge of timer</li></ul><p>Needed on any files as argument to “require” (trak.pl)</p></th></tr></tbody></table>
