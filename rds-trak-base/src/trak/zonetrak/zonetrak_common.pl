#!/bin/perl -I.
#
# ZoneTRAK common elements
#

require "spread.pl";
my @code;


# ----- C o n s t a n t s -----
#

Trk::include( "zonetrak.h", 'MSG_ZONE_' );  # message types
Trk::include( "zonetrak.h", 'BOX_' );       # box numbers
Trk::include( "zonetrak.h", 'STATE_' );     # fsm states
Trk::include( "zonetrak.h", 'DIR_' );       # direction codes
Trk::include( "zonetrak.h", 'ERR_' );       # error codes
Trk::include( "zonetrak.h", 'MODE_' );      # zone modes
Trk::include( "zonetrak.h", 'REG_' );       # registers

# perl constants for state bitmasks
use constant {
   MASK_INIT       => 1 <<  0,
   MASK_IDLE       => 1 <<  1,
   MASK_FILL       => 1 <<  2,
   MASK_FILL_X     => 1 <<  3,
   MASK_FULL       => 1 <<  4,
   MASK_DRAIN_X    => 1 <<  5,
   MASK_DRAIN      => 1 <<  6,
   MASK_RUNUP      => 1 <<  9,
   MASK_CHOOSE     => 1 << 10,
   MASK_ACTIVE     => 1 << 11,
   MASK_DEACT      => 1 << 12,
   MASK_FILL_O     => 1 << 13,
   MASK_DRAIN_O    => 1 << 14,
   MASK_FAULT      => 1 << 31,
};

# perl constants for directions
use constant {
   DIR_LAST        => -1,
   DIR_NONE        =>  0,
   DIR_STRAIGHT    =>  1,
   DIR_LEFT        =>  2,
   DIR_RIGHT       =>  3,
};

# perl constants for modes
use constant {
   MODE_CREATE     =>  0,
   MODE_LAX        =>  1,
   MODE_SLUG       =>  2,
   MODE_STOP       =>  3,
   MODE_MULTI      =>  4,
   MODE_IDLERUN    =>  5,
   MODE_BELTFEED   =>  6,
   MODE_PUSHOUT    =>  7,
};

# perl constants for registers
use constant {
   REG_BOX         =>  0,
   REG_STATE       =>  1,
   REG_MODE        =>  2,
   REG_NEXT        =>  3,
   REG_EYE         =>  4,
   REG_MOTOR       =>  5,
   REG_JAMEYE      =>  6,
   REG_INPUT       =>  7,
   REG_OUTPUT      =>  8,
   REG_TIMER       =>  9,

   REG_RUN         => 10,
   REG_FAULT       => 11,
   REG_RESET       => 12,
   REG_ZONEFAULT   => 13,
   REG_DEBUG       => 14,
   REG_HOLD        => 15,
   REG_GO          => 16,
   REG_CURRENT     => 17,
   REG_FIRST       => 18,
   REG_LAST        => 19,

   REG_ACTIVE      => 20,
   REG_ACTIVE_EYE  => 21,
   REG_DEACT       => 22,
   REG_DEACT_EYE   => 23,
   REG_LEFT        => 24,
   REG_RIGHT       => 25,
   REG_OFFSET      => 26,
   REG_DEFAULT     => 27,
   REG_CHOICE      => 28,
   REG_CONTROL     => 29,

   REG_RUNUP       => 30,
   REG_RUNIN       => 31,
   REG_RUNOVER     => 32,
   REG_RUNOUT      => 33,
   REG_RUNOFF      => 34,
   REG_RUNDOWN     => 35,
   REG_TEMP        => 37,
   REG_TEMP2       => 38,
   REG_OUT_STATE   => 39,

   REG_TM_MIN      => 40,
   REG_TM_MAX      => 41,
   REG_TM_CLEAR    => 42,
   REG_TM_CHOOSE   => 43,
   REG_TM_WAIT     => 44,
   REG_TM_DEAD     => 45,
   REG_TM_SLUG     => 48,
   REG_TM_JAM      => 49,
};


# prototypes
@code=qw( 1 drop );
Trk::ex_compile( '_zone_mode?', 'zone mode set?', \@code );


# ----- M e s s a g e s -----
#

# _zone_create_msg  ( dp -- )
#    utility to send a notification message on box creation
#    sends:
#       MSG_ZONE_CREATE  zone  0  box  msec
#    note that the third field is merely a placeholder to keep the zone
#    message formats consistent
@code=qw(
   >r

   tm_1ms dp.counter.get
   REG_BOX r@ dp.register.get
   0
   r@
   MSG_ZONE_CREATE
   5 mb_npost

   r> drop
);
Trk::ex_compile( "_zone_create_msg", "zone create msg", \@code );

# _zone_state_msg  ( dp -- )
#    utility to send a notification message on state change
#    sends:
#       MSG_ZONE_STATE  zone  state  box  msec
@code=qw(
   >r

   tm_1ms dp.counter.get
   REG_BOX   r@ dp.register.get
   REG_STATE r@ dp.register.get
   r@
   MSG_ZONE_STATE
   5 mb_npost

   r> drop
);
Trk::ex_compile( "_zone_state_msg", "zone state msg", \@code );

# _zone_choice_msg  ( dp -- )
#    utility to send a notification message on selection (e.g. merge input
#    lane or divert destination)
#    sends:
#       MSG_ZONE_CHOICE  zone  choice  box  msec
@code=qw(
   >r

   tm_1ms dp.counter.get
   REG_BOX    r@ dp.register.get
   REG_CHOICE r@ dp.register.get
   r@
   MSG_ZONE_CHOICE
   5 mb_npost

   r> drop
);
Trk::ex_compile( "_zone_choice_msg", "zone choice msg", \@code );

# _zone_err_msg  ( code dp -- )
#    utility to send a notification message on error/jam detection
#    sends:
#       MSG_ZONE_ERR  zone  code  box  msec
@code=qw(
   >r

   MODE_LAX r@ _zone_mode? if
      drop
   else

      tm_1ms dp.counter.get       swap
      REG_BOX r@ dp.register.get  swap
      r@
      MSG_ZONE_ERR
      5 mb_npost

   then

   r> drop
);
Trk::ex_compile( "_zone_error_msg", "zone error msg", \@code );

# _zone_reset_msg  ( dp -- )
#    utility to send a notification message on zone reset
#    sends:
#       MSG_ZONE_RESET  zone  0  0  msec
@code=qw(
   >r

   tm_1ms dp.counter.get
   0
   0
   r@
   MSG_ZONE_RESET
   5 mb_npost

   r> drop
);
Trk::ex_compile( "_zone_reset_msg", "zone reset msg", \@code );


# ----- U t i l i t i e s -----
#

# _zone_dp?  ( register dp -- dp_val )
#    get the value of a dp stored in a register (default off)
@code=qw(
   >r

   r@ 0 > if
      r@ dp.register.get
      dup 0 > if  dp.value.get  else  drop 0  then
   else
      drop 0
   then

   r> drop
);
Trk::ex_compile( '_zone_dp?', 'zone get dp value', \@code);

# _zone_dp_not?  ( register dp -- rev_dp_val )
#    get the reversed value of a dp stored in a register (default off)
@code=qw(
   >r

   r@ 0 > if
      r@ dp.register.get
      dup 0 > if  dp.value.get not  else  drop 0  then
   else
      drop 0
   then

   r> drop
);
Trk::ex_compile( '_zone_dp_not?', 'zone get reversed dp value', \@code);

# _zone_dp_on  ( register dp -- )
#    turn on a dp stored in a register
@code=qw(
   >r

   r@ 0 > if
      r@ dp.register.get
      dup 0 > if  on  else  drop  then
   else
      drop
   then

   r> drop
);
Trk::ex_compile( '_zone_dp_on', 'zone dp on', \@code);

# _zone_dp_off  ( register dp -- )
#    turn off a dp stored in a register
@code=qw(
   >r

   r@ 0 > if
      r@ dp.register.get
      dup 0 > if  off  else  drop  then
   else
      drop
   then

   r> drop
);
Trk::ex_compile( '_zone_dp_off', 'zone dp off', \@code);

# _zone_rp@  ( register dp -- rp_val )
#    get the value of an rp stored in a register (default 0)
@code=qw(
   >r

   r@ 0 > if
      r@ dp.register.get
      dup 0 > if  rp.value.get  else  drop 0  then
   else
      drop 0
   then

   r> drop
);
Trk::ex_compile( '_zone_rp@', 'zone get rp value', \@code);

# _zone_box@  ( dp -- box )
#    get the box
@code=qw(
   REG_BOX swap dp.register.get
);
Trk::ex_compile( '_zone_box@', 'zone get box', \@code);

# _zone_box!  ( box dp -- )
#    set the box
@code=qw(
   >r

   r@ 0 > if
      REG_BOX r@ dp.register.set
   else
      drop
   then

   r> drop
);
Trk::ex_compile( '_zone_box!', 'zone set box', \@code);

# _zone_tick  ( dp -- )
#    increment the cycle counter
@code=qw(
   >r

   REG_TIMER r@ dp.register.get
   1 +
   REG_TIMER r@ dp.register.set

   r> drop
);
Trk::ex_compile( '_zone_tick', 'zone timer tick', \@code );

# _zone_timer_clr  ( dp -- )
#    clear the cycle counter
@code=qw(
   0 swap  REG_TIMER swap  dp.register.set
);
Trk::ex_compile( '_zone_timer_clr', 'zone timer clear', \@code );

# _zone_elapsed@  ( dp -- elapsed )
#    get cycles spent in current state
@code=qw(
   REG_TIMER swap dp.register.get
);
Trk::ex_compile( '_zone_elapsed@', 'get zone elapsed', \@code );

# _zone_state@  ( dp -- state )
#    get the current state
@code=qw(
   REG_STATE swap dp.register.get
);
Trk::ex_compile( '_zone_state@', 'get zone state', \@code );

# _zone_state!  ( state dp -- )
#    set the state
#    clear cycle counter
#    last line of if body?
@code=qw(
   >r

   dup REG_STATE r@ dp.register.get = not if
      r@ _zone_timer_clr
      REG_STATE r@ dp.register.set
      r@ _zone_state_msg
   else
      drop
   then

   r> drop
);
Trk::ex_compile( '_zone_state!', 'set zone state', \@code );

# _zone_motor_next  ( dp -- )
#    run motor with the motor of the next zone
# NB: to keep the motion smooth, there is a hard-coded delay before
#     changing the motor state
@code=qw(
   >r

   r@ _zone_elapsed@ 2 < if
      REG_MOTOR r@ _zone_dp_not? if
         REG_MOTOR r@ _zone_dp_off
      else
         REG_MOTOR r@ _zone_dp_on
      then
      r@ _zone_tick
   else
      REG_MOTOR  REG_NEXT r@ dp.register.get  _zone_dp_not? if
         REG_MOTOR r@ _zone_dp_off
      else
         REG_MOTOR r@ _zone_dp_on
         r@ _zone_tick
      then
   then

   r> drop
);
Trk::ex_compile( '_zone_motor_next', 'run zone motor with next zone motor',
      \@code );

# _zone_mode?  ( mode dp -- set )
#    determine if the specified mode is set
@code=qw(
   REG_MODE swap dp.register.get
   swap shr
   1 and
);
Trk::ex_compile( '_zone_mode?', 'zone mode set?', \@code );

# _zone_next_avail?  ( dp -- available )
#    determine if the next zone is available
@code=qw(
   REG_NEXT swap _zone_dp_not?
);
Trk::ex_compile( '_zone_next_avail?', 'zone next available?', \@code );

# _zone_run?  ( dp -- running )
#    get the value of the area run (defaults to true)
@code=qw(
   REG_RUN swap dp.register.get
   dup 0 > if  dp.value.get  else  drop 1  then
);
Trk::ex_compile( '_zone_run?', 'zone area running?', \@code );

# _zone_fault?  ( dp -- faulted )
#    checks if the zone is faulted, globally or locally
@code=qw(
   >r
   REG_FAULT r@ _zone_dp?  REG_ZONEFAULT r@ _zone_dp?  or
   r> drop
);
Trk::ex_compile( '_zone_fault?', 'zone faulted?', \@code );

# _zone_first@  ( dp -- val )
#    get the first value in the range of created box numbers (default 1)
@code=qw(
   REG_FIRST swap dp.register.get
   dup dup  1 <  swap 999 >  or if
      drop 1
   then
);
Trk::ex_compile( '_zone_first@', 'zone get first', \@code);

# _zone_last@  ( dp -- val )
#    get the last value in the range of created box numbers (default 999)
@code=qw(
   REG_LAST swap dp.register.get
   dup dup  1 <  swap 999 >  or if
      drop 999
   then
);
Trk::ex_compile( '_zone_last@', 'zone get last', \@code);

# _zone_box_new  ( dp -- )
#    create and initialize a new box number and store it
#    the 'current' register is fetched and updated with the box number
#    'first' and 'last' are used to set the bounds on box number creation
@code=qw(
   >r

   REG_CURRENT r@ dp.register.get
   1 +
   dup dup  r@ _zone_first@ <  swap r@ _zone_last@ >  or if
      drop  r@ _zone_first@
   then

   dup  REG_CURRENT r@ dp.register.set
   dup  1 swap bx.state.set
   dup  0 swap bx.data.set
        r@ _zone_box!

   r@ _zone_create_msg

   r> drop
);
Trk::ex_compile( '_zone_box_new', 'create a box at a zone', \@code);

# _zone_box_pass  ( dp -- )
#    pass the box to the next zone and clear it from the current zone
@code=qw(
   >r

   MODE_LAX r@ _zone_mode? if
      BOX_ANON  REG_NEXT r@ dp.register.get  _zone_box!
   else
      r@ _zone_box@  REG_NEXT r@ dp.register.get  _zone_box!
   then
   BOX_NONE r@ _zone_box!

   r> drop
);
Trk::ex_compile( '_zone_box_pass', 'zone pass box', \@code);

# _zone_box_valid?  ( dp -- valid )
#    check if the current box is valid (1 <= box <= 999)
@code=qw(
   _zone_box@
   dup  0 >  swap 1000 <  and
);
Trk::ex_compile( '_zone_box_valid?', 'zone box valid?', \@code);

# _zone_box_dir@  ( dp -- direction )
#    get destination code from box data and convert to a local direction
#    for the zone
@code=qw(
   >r

   r@ _zone_box_valid? if
      r@ _zone_box@ bx.data.get
      REG_OFFSET r@ dp.register.get  3 * shr 7 and
   else
      DIR_NONE
   then

   r> drop
);
Trk::ex_compile( '_zone_box_dir@', 'zone get box direction', \@code);

# _zone_dest@  ( direction dp -- destination )
#    get the destination dp for the specified direction
@code=qw(
   >r

   0 swap
   dup DIR_STRAIGHT = if  swap drop r@                           swap  then
   dup DIR_LEFT     = if  swap drop REG_LEFT  r@ dp.register.get swap  then
   dup DIR_RIGHT    = if  swap drop REG_RIGHT r@ dp.register.get swap  then
   drop

   r> drop
);
Trk::ex_compile( '_zone_dest@', 'zone get destination', \@code);

# _zone_slug_release?  ( dp -- release )
#    test whether the zone should perform a slug-type release
@code=qw(
   >r

   MODE_SLUG r@ _zone_mode?
   r@ _zone_box@ BOX_NONE =  and
   r@ _zone_elapsed@  REG_TM_SLUG r@ _zone_rp@  >  and

   r> drop
);
Trk::ex_compile( '_zone_slug_release?', 'zone test slug release', \@code );

# _zone_set_output  ( dp -- )
#    set the output dp depending on the output state field and current
#    state
@code=qw(
   >r

   1 r@ _zone_state@ shl  REG_OUT_STATE r@ dp.register.get  and if
      REG_OUTPUT r@ _zone_dp_on
   else
      REG_OUTPUT r@ _zone_dp_off
   then

   r> drop
);
Trk::ex_compile( '_zone_set_output', 'zone set output', \@code);


# create the template of a zone; additional data will be processed by the
# zone-specific method
sub createZone {
   my ($name, $desc, $data) = @_;
   my $val;

   # create the zone dp
   if (($val = $data->{desc}) ne '') { $desc = $val; }
   my $dp = dp::virtual( $name, $desc );

   # create controls
   Trk::dp_registerset( $dp, REG_ZONEFAULT,
         dp::virtual( $name . '_fault', $name . ' fault' ) );
   Trk::dp_registerset( $dp, REG_DEBUG,
         dp::virtual( $name . '_debug', $name . ' debug' ) );
   Trk::dp_registerset( $dp, REG_HOLD,
         dp::virtual( $name . '_hold',  $name . ' hold' ) );

   # set mode bits
   my $mode = 0;
   if ($data->{create_mode} ne '') {
      $mode = $mode | (1 << MODE_CREATE);
      if (($val = $data->{first}) ne '')
         { Trk::dp_registerset( $dp, REG_FIRST, $val ); }
      if (($val = $data->{last}) ne '')
         { Trk::dp_registerset( $dp, REG_LAST,  $val ); }
   }
   if ($data->{lax_mode} ne '') {
      $mode = $mode | (1 << MODE_LAX);
   }
   if ($data->{slug_mode} ne '') {
      $mode = $mode | (1 << MODE_SLUG);
      if (($val = $data->{slug}) ne '') {
         $rp = rp::const( $name . '_slug', $val,
               $name . ' slug-release delay time (cycles)' );
      } else { $rp = rp::handle( 'zone_slug' ); }
      Trk::dp_registerset( $dp, REG_TM_SLUG, $rp );
   }
   if ($data->{stop_mode} ne '') {
      $mode = $mode | (1 << MODE_STOP);
      Trk::dp_registerset( $dp, REG_GO,
            dp::virtual( $name . '_go',  $name . ' release' ) );
   }
   if ($data->{multi_mode} ne '') {
      $mode = $mode | (1 << MODE_MULTI);
   }
   if ($data->{idlerun_mode} ne '') {
      $mode = $mode | (1 << MODE_IDLERUN);
   }
   if ($data->{beltfeed_mode} ne '') {
      $mode = $mode | (1 << MODE_BELTFEED) ;
   }
   if ($data->{pushout_mode} ne '') {
      $mode = $mode | (1 << MODE_PUSHOUT) ;
   }
   Trk::dp_registerset( $dp, REG_MODE, $mode );

   # set registers from input data
   my $val;
   if (($val = $data->{next}) ne '')
      { Trk::dp_registerset( $dp, REG_NEXT,   dp::handle( $val ) ); }
   if (($val = $data->{eye}) ne '')
      { Trk::dp_registerset( $dp, REG_EYE,    dp::handle( $val ) ); }
   if (($val = $data->{motor}) ne '')
      { Trk::dp_registerset( $dp, REG_MOTOR,  dp::handle( $val ) ); }
   if (($val = $data->{run}) ne '')
      { Trk::dp_registerset( $dp, REG_RUN,    dp::handle( $val ) ); }
   if (($val = $data->{fault}) ne '')
      { Trk::dp_registerset( $dp, REG_FAULT,  dp::handle( $val ) ); }
   if (($val = $data->{reset}) ne '') {
      Trk::dp_registerset(   $dp, REG_RESET,  dp::handle( $val ) );
      my @stub = ( $name . '_fault', 'off' );
      Trk::ex_compile( $name . '_fault_clr', $name . ' fault clear', \@stub );
      trakbase::leading( $val, $name . '_fault_clr',
            'clear ' . $name . ' fault' );
   }

   if (($val = $data->{output}) ne '')
      { Trk::dp_registerset( $dp, REG_OUTPUT, dp::handle( $val ) ); }
   if (($val = $data->{out_states}) ne '')
      { Trk::dp_registerset( $dp, REG_OUT_STATE, $val ); }


   return $dp;
}


# return a true value to indicate successful initialization
1;
