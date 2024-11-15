#!/bin/perl -I.
#
# System control
#

require Trk ;
require trakbase ;
require dp ;
require rp ;
require timer ;

my @code ;

# ----- f l a s h e r ------
#
rp::const("flash_period",100,"flash period (cycles)") ;
rp::const("flash_duty",50,"flash duty cycle (percent)") ;
rp::const("flash_beep",10,"flash horn duty cycle (percent)") ;

($FLASH_REG_CONTROL,$FLASH_REG_OUTPUT,$FLASH_REG_TIMER) 
= (1,5,6) ;
Trk::const_set("FLASH_REG_CONTROL",$FLASH_REG_CONTROL) ;
Trk::const_set("FLASH_REG_OUTPUT", $FLASH_REG_OUTPUT) ;
Trk::const_set("FLASH_REG_TIMER",  $FLASH_REG_TIMER) ;

# _flasher ( dp -- )
@code=qw(
   >r
   FLASH_REG_CONTROL r@ dp.register.get

   dup 0 = if
      FLASH_REG_OUTPUT r@ dp.register.get off
      0 FLASH_REG_TIMER r@ dp.register.set
   then

   dup 1 = if
      FLASH_REG_OUTPUT r@ dp.register.get on
      0 FLASH_REG_TIMER r@ dp.register.set
   then

   dup 2 = if
      FLASH_REG_TIMER r@ dp.register.get
      1 +
      FLASH_REG_TIMER r@ dp.register.set

      FLASH_REG_TIMER r@ dp.register.get
      flash_period rp.value.get flash_duty rp.value.get * 100 / > if
         FLASH_REG_OUTPUT r@ dp.register.get off

         FLASH_REG_TIMER r@ dp.register.get
         flash_period rp.value.get > if
            0 FLASH_REG_TIMER r@ dp.register.set
         then
      else
         FLASH_REG_OUTPUT r@ dp.register.get on
      then
   then

   dup 3 = if
      FLASH_REG_TIMER r@ dp.register.get
      1 +
      FLASH_REG_TIMER r@ dp.register.set

      FLASH_REG_TIMER r@ dp.register.get
      flash_period rp.value.get flash_beep rp.value.get * 100 / > if
         FLASH_REG_OUTPUT r@ dp.register.get off

         FLASH_REG_TIMER r@ dp.register.get
         flash_period rp.value.get > if
            0 FLASH_REG_TIMER r@ dp.register.set
         then
      else
         FLASH_REG_OUTPUT r@ dp.register.get on
      then
   then

   drop
   r> drop
   ) ;
Trk::ex_compile("_flasher","flasher",\@code) ;

# _flash_off ( dp -- )
@code=qw(
   >r
   r@ 0 > if  0 FLASH_REG_CONTROL r@ dp.register.set  then
   r> drop
);
Trk::ex_compile("_flash_off","flash off",\@code) ;

# _flash_on ( dp -- )
@code=qw(
   >r
   r@ 0 > if  1 FLASH_REG_CONTROL r@ dp.register.set  then
   r> drop
);
Trk::ex_compile("_flash_on","flash on",\@code) ;

# _flash_flash ( dp -- )
@code=qw(
   >r
   r@ 0 > if  2 FLASH_REG_CONTROL r@ dp.register.set  then
   r> drop
);
Trk::ex_compile("_flash_flash","flash flash",\@code) ;

# _flash_beep ( dp -- )
@code=qw(
   >r
   r@ 0 > if  3 FLASH_REG_CONTROL r@ dp.register.set  then
   r> drop
);
Trk::ex_compile("_flash_beep","flash beep",\@code) ;

# _flash_reset ( dp -- )
@code=qw(
   >r
   r@ 0 > if  0 FLASH_REG_TIMER r@ dp.register.set  then
   r> drop
);
Trk::ex_compile("_flash_reset","flash reset",\@code) ;

sub addFlasher
   {
   my ($name,$output,$desc) = @_ ;

   my $dp = dp::virtual($name,$desc) ;

   Trk::dp_registerset($dp,$FLASH_REG_CONTROL,0) ;
   Trk::dp_registerset($dp,$FLASH_REG_OUTPUT,dp::handle($output)) ;

   my @stub = ($name,"_flasher") ;
   Trk::ex_compile($name."_flasher",$name." flasher",\@stub) ;
   trakbase::leading( "tm_10ms",$name."_flasher",$name." flasher") ;
   trakbase::trailing("tm_10ms",$name."_flasher",$name." flasher") ;
   my $dummy = pop(@stub) ;

   return $dp ;
   }

# ----- c o n t r o l -----
#
rp::const("start_delay",1000,"system start delay") ;
rp::const("stop_delay",  100,"system stop delay") ;
rp::const("fault_delay", 100,"system fault delay") ;

# ----- control states -----
#
($CONTROL_STATE_INIT,$CONTROL_STATE_CHECK,$CONTROL_STATE_IDLE,
 $CONTROL_STATE_STARTING,$CONTROL_STATE_RUN,
 $CONTROL_STATE_STOPPING,$CONTROL_STATE_LATCH,
 $CONTROL_STATE_FAULT)
= (0,1,2,
   3,4,
   5,6,
   31) ;
Trk::const_set("CONTROL_STATE_INIT",    $CONTROL_STATE_INIT) ;
Trk::const_set("CONTROL_STATE_CHECK",   $CONTROL_STATE_CHECK) ;
Trk::const_set("CONTROL_STATE_IDLE",    $CONTROL_STATE_IDLE) ;
Trk::const_set("CONTROL_STATE_STARTING",$CONTROL_STATE_STARTING) ;
Trk::const_set("CONTROL_STATE_RUN",     $CONTROL_STATE_RUN) ;
Trk::const_set("CONTROL_STATE_STOPPING",$CONTROL_STATE_STOPPING) ;
Trk::const_set("CONTROL_STATE_FAULT",   $CONTROL_STATE_FAULT) ;
Trk::const_set("CONTROL_STATE_LATCH",   $CONTROL_STATE_LATCH) ;

# ----- registers -----
#
($CONTROL_REG_STATE, $CONTROL_REG_TIMER,
 $CONTROL_REG_FAULT, $CONTROL_REG_ALERT,
 $CONTROL_REG_WARNING, $CONTROL_REG_START,
 $CONTROL_REG_STOP,    $CONTROL_REG_RESET,
 $CONTROL_REG_RED,     $CONTROL_REG_AMBER,
 $CONTROL_REG_GREEN,   $CONTROL_REG_HORN,
 $CONTROL_REG_START_BUTTON,  $CONTROL_REG_RESET_BUTTON,
 $CONTROL_REG_RUN,           $CONTROL_REG_ON_RESET,
 $CONTROL_REG_OK)
= (0,1,
   2,3,
   4,5,
   6,7,
   8,9,
   10,11,
   12,13,
   14,15,
   16) ;
Trk::const_set("CONTROL_REG_STATE",  $CONTROL_REG_STATE) ;
Trk::const_set("CONTROL_REG_TIMER",  $CONTROL_REG_TIMER) ;
Trk::const_set("CONTROL_REG_FAULT",  $CONTROL_REG_FAULT) ;
Trk::const_set("CONTROL_REG_ALERT",  $CONTROL_REG_ALERT) ;
Trk::const_set("CONTROL_REG_WARNING",$CONTROL_REG_WARNING) ;
Trk::const_set("CONTROL_REG_START",  $CONTROL_REG_START) ;
Trk::const_set("CONTROL_REG_STOP",   $CONTROL_REG_STOP) ;
Trk::const_set("CONTROL_REG_RESET",  $CONTROL_REG_RESET) ;
Trk::const_set("CONTROL_REG_RED",    $CONTROL_REG_RED) ;
Trk::const_set("CONTROL_REG_AMBER",  $CONTROL_REG_AMBER) ;
Trk::const_set("CONTROL_REG_GREEN",  $CONTROL_REG_GREEN) ;
Trk::const_set("CONTROL_REG_HORN",   $CONTROL_REG_HORN) ;
Trk::const_set("CONTROL_REG_START_BUTTON", $CONTROL_REG_START_BUTTON) ;
Trk::const_set("CONTROL_REG_RESET_BUTTON", $CONTROL_REG_RESET_BUTTON) ;
Trk::const_set("CONTROL_REG_RUN",    $CONTROL_REG_RUN) ;
Trk::const_set("CONTROL_REG_ON_RESET", $CONTROL_REG_ON_RESET) ;
Trk::const_set("CONTROL_REG_OK",     $CONTROL_REG_OK) ;

# _control_tick ( dp -- )
@code=qw(
   >r

   CONTROL_REG_TIMER r@ dp.register.get
   1 +
   CONTROL_REG_TIMER r@ dp.register.set

   r> drop
   ) ;
Trk::ex_compile('_control_tick','control timer tick',\@code) ;

# _control_elapsed@ ( dp -- elapsed )
@code=qw(
   CONTROL_REG_TIMER swap dp.register.get
   ) ;
Trk::ex_compile('_control_elapsed@','control get elapsed',\@code) ;

# _control_state_init ( dp -- )
#
@code=qw(
  >r

  r@ off
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get off
  then

  CONTROL_REG_RED    r@ dp.register.get _flash_off
  CONTROL_REG_AMBER  r@ dp.register.get _flash_off
  CONTROL_REG_GREEN  r@ dp.register.get _flash_off
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_off
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_off

  CONTROL_STATE_CHECK CONTROL_REG_STATE r@ dp.register.set

  r> drop ) ;
Trk::ex_compile("_control_state_init","control state init",\@code) ;

# _control_state_check
#
@code=qw(
  >r

  r@ off
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get off
  then

  CONTROL_REG_RED    r@ dp.register.get _flash_off
  CONTROL_REG_AMBER  r@ dp.register.get _flash_off
  CONTROL_REG_GREEN  r@ dp.register.get _flash_off
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_off
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_on

  1
  CONTROL_REG_OK r@ dp.register.get 0 = not
  if
    drop  CONTROL_REG_OK r@ dp.register.get tst
  then
  if
    CONTROL_STATE_IDLE CONTROL_REG_STATE r@ dp.register.set
  else
    CONTROL_REG_FAULT r@ dp.register.get tst
    if
      CONTROL_STATE_FAULT CONTROL_REG_STATE r@ dp.register.set
    then
  then

  r> drop ) ;
Trk::ex_compile("_control_state_check","control state check",\@code) ;

# _control_state_idle
#
@code=qw(
  >r

  r@ off
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get off
  then

  CONTROL_REG_ON_RESET r@ dp.register.get 0 = not
  if
    CONTROL_REG_ON_RESET r@ dp.register.get off
  then

  CONTROL_REG_RED    r@ dp.register.get _flash_off
  CONTROL_REG_AMBER  r@ dp.register.get _flash_off
  CONTROL_REG_GREEN  r@ dp.register.get _flash_off
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_flash
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_off

  CONTROL_REG_START r@ dp.register.get dp.value.get 
  if
    0 CONTROL_REG_TIMER r@ dp.register.set
    CONTROL_REG_RED    r@ dp.register.get _flash_reset
    CONTROL_REG_AMBER  r@ dp.register.get _flash_reset
    CONTROL_REG_GREEN  r@ dp.register.get _flash_reset
    CONTROL_REG_HORN   r@ dp.register.get _flash_reset
    CONTROL_STATE_STARTING CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_FAULT r@ dp.register.get dp.value.get 
  if
    CONTROL_STATE_FAULT CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_idle","control state idle",\@code) ;

# _control_state_starting
#
# NB: the control dp is intentionally not set during this state
@code=qw(
  >r

  CONTROL_REG_RED    r@ dp.register.get _flash_flash
  CONTROL_REG_AMBER  r@ dp.register.get _flash_flash
  CONTROL_REG_GREEN  r@ dp.register.get _flash_flash
  CONTROL_REG_HORN   r@ dp.register.get _flash_beep
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_on
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_off

  r@ _control_tick
  r@ _control_elapsed@ start_delay rp.value.get > 
  if
    CONTROL_STATE_RUN CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_STOP  r@ dp.register.get dp.value.get not 
  if
    CONTROL_STATE_IDLE CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_FAULT r@ dp.register.get dp.value.get 
  if
    CONTROL_STATE_FAULT CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_starting","control state starting",\@code) ;

# _control_state_run
#
@code=qw(
  >r

  r@ on
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get on
  then

  CONTROL_REG_ALERT r@ dp.register.get dp.value.get 
  if
    CONTROL_REG_RED r@ dp.register.get _flash_on
  else
    CONTROL_REG_RED r@ dp.register.get _flash_off
  then

  CONTROL_REG_WARNING r@ dp.register.get dp.value.get 
  if
    CONTROL_REG_AMBER r@ dp.register.get _flash_on
  else
    CONTROL_REG_AMBER r@ dp.register.get _flash_off
  then

  CONTROL_REG_GREEN  r@ dp.register.get _flash_on
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_on
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_off

  CONTROL_REG_RESET r@ dp.register.get dp.value.get 
  if
    0 CONTROL_REG_TIMER r@ dp.register.set
    CONTROL_STATE_STARTING CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_STOP  r@ dp.register.get dp.value.get not 
  if
    0 CONTROL_REG_TIMER r@ dp.register.set
    CONTROL_STATE_STOPPING CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_FAULT r@ dp.register.get dp.value.get 
  if
    CONTROL_STATE_FAULT CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_run","control state run",\@code) ;

# _control_state_stopping
#
# NB: the control dp is intentionally not set during this state
@code=qw(
  >r

  CONTROL_REG_RED    r@ dp.register.get _flash_off
  CONTROL_REG_AMBER  r@ dp.register.get _flash_off
  CONTROL_REG_GREEN  r@ dp.register.get _flash_flash
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_off
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_off

  r@ _control_tick
  r@ _control_elapsed@ stop_delay rp.value.get > 
  if
    CONTROL_STATE_IDLE CONTROL_REG_STATE r@ dp.register.set
  then

  CONTROL_REG_FAULT r@ dp.register.get dp.value.get 
  if
    CONTROL_STATE_FAULT CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_stopping","control state stopping",\@code) ;

# _control_state_fault
#
@code=qw(
  >r

  r@ off
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get off
  then

  CONTROL_REG_RED    r@ dp.register.get _flash_on
  CONTROL_REG_GREEN  r@ dp.register.get _flash_off
  CONTROL_REG_HORN   r@ dp.register.get _flash_on
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_flash

  r@ _control_tick
  r@ _control_elapsed@ fault_delay rp.value.get > 
  if
    CONTROL_STATE_LATCH CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_fault","control state fault",\@code) ;

# _control_state_latch
#
@code=qw(
  >r

  r@ off
  CONTROL_REG_RUN r@ dp.register.get 0 = not
  if
    CONTROL_REG_RUN r@ dp.register.get off
  then

  CONTROL_REG_RED    r@ dp.register.get _flash_flash
  CONTROL_REG_GREEN  r@ dp.register.get _flash_off
  CONTROL_REG_HORN   r@ dp.register.get _flash_off
  CONTROL_REG_START_BUTTON r@ dp.register.get _flash_off
  CONTROL_REG_RESET_BUTTON r@ dp.register.get _flash_flash

  CONTROL_REG_RESET r@ dp.register.get dp.value.get 
  if
    CONTROL_REG_ON_RESET r@ dp.register.get 0 = not 
    if
      CONTROL_REG_ON_RESET r@ dp.register.get on 
    then
    CONTROL_REG_FAULT    r@ dp.register.get off
    CONTROL_STATE_CHECK CONTROL_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_control_state_latch","control state latch",\@code) ;

# _control_machine ( dp -- )
#
@code=qw(
  >r

  CONTROL_REG_STATE r@ dp.register.get
  dup CONTROL_STATE_INIT     = if r@ _control_state_init     then
  dup CONTROL_STATE_CHECK    = if r@ _control_state_check    then
  dup CONTROL_STATE_IDLE     = if r@ _control_state_idle     then
  dup CONTROL_STATE_STARTING = if r@ _control_state_starting then
  dup CONTROL_STATE_RUN      = if r@ _control_state_run      then
  dup CONTROL_STATE_STOPPING = if r@ _control_state_stopping then
  dup CONTROL_STATE_FAULT    = if r@ _control_state_fault    then
  dup CONTROL_STATE_LATCH    = if r@ _control_state_latch    then
  drop

  r> drop ) ;
Trk::ex_compile("_control_machine","control machine",\@code) ;

sub addControl()
   {
   my ($name,$fault,$warning,$start,$stop,$reset,$red,$amber,$green,$horn,$button,$desc) = @_ ;

   createControl( $name, {
      fault=>$fault,
      warning=>$warning,
      start=>$start,
      stop=>$stop,
      reset=>$reset,
      red=>$red,
      amber=>$amber,
      green=>$green,
      horn=>$horn,
      start_button=>$button,
      desc=>$desc,
      } );
   }


# create a control
sub 
createControl 
  {
  my ($name, $data) = @_;
  my $val;

  # create the control dp
  my $desc = $name . ' control';
  if (($val = $data->{desc}) ne '') { $desc = $val; }
  my $dp = dp::virtual( $name, $desc );

  if (($val = $data->{'ok'}) ne '')
    { Trk::dp_registerset($dp, $CONTROL_REG_OK, dp::handle( $val ) ); }
  if (($val = $data->{'on_reset'}) ne '')
    { Trk::dp_registerset($dp, $CONTROL_REG_ON_RESET, dp::handle( $val )); }
  if (($val = $data->{run})   ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_RUN,   dp::handle( $val ) ); }
  if (($val = $data->{fault}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_FAULT, dp::handle( $val ) ); }
  if (($val = $data->{alert}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_ALERT, dp::handle( $val ) ); }
  if (($val = $data->{warning}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_WARNING, dp::handle( $val ) ); }
  if (($val = $data->{start}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_START, dp::handle( $val ) ); }
  if (($val = $data->{stop}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_STOP, dp::handle( $val ) ); }
  if (($val = $data->{reset}) ne '')
    { Trk::dp_registerset( $dp, $CONTROL_REG_RESET, dp::handle( $val ) ); }

  if (($val = $data->{red}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_RED, 
        addFlasher( $name . "_red", $val, $name . " red beacon" ) );
    }
  if (($val = $data->{amber}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_AMBER,
        addFlasher( $name . "_amber", $val, $name . " amber beacon" ) );
    }
  if (($val = $data->{green}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_GREEN,
        addFlasher( $name . "_green", $val, $name . " green beacon" ) );
    }
  if (($val = $data->{horn}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_HORN,
        addFlasher( $name . "_horn", $val, $name . " alarm horn" ) );
    }
  if (($val = $data->{start_button}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_START_BUTTON,
        addFlasher( $name . "_start_button", $val, $name . " start button light" ) );
    }
  if (($val = $data->{reset_button}) ne '') 
    { Trk::dp_registerset( $dp, $CONTROL_REG_RESET_BUTTON,
        addFlasher( $name . "_reset_button", $val, $name . " reset button light" ) );
    }

  # run the state machine
  my @stub = ( $name, '_control_machine' );
  Trk::ex_compile( $name . '_machine', $name . ' state machine', \@stub );

  my $timer = 'tm_10ms';
  if (($val = $data->{timer}) ne '') { $timer = $val; }
  trakbase::leading(  $timer, $name . '_machine',
                      'run ' . $name . ' state machine' );
  trakbase::trailing( $timer, $name . '_machine',
                      'run ' . $name . ' state machine' );

  my $tmp = pop( @stub );

  return $dp ;
  }

# return a true value to indicate successful initialization
1;

