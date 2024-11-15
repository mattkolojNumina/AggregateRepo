#!/usr/bin/perl
#
# EtherCAT Servo Control
#

require Trk ;
require trakbase ;
require dp ;
require rp ;
require timer ;

my @code ;

# ----- s t a t e s -----
#
($SERVO_STATE_INIT, $SERVO_STATE_RESET, 
 $SERVO_STATE_SETUP, $SERVO_STATE_HOME,
 $SERVO_STATE_IDLE, $SERVO_STATE_MOVE,
 $SERVO_STATE_FAULT )
= (0,1,
   2,3,
   4,5,
   6 ) ;
Trk::const_set("SERVO_STATE_INIT", $SERVO_STATE_INIT) ;
Trk::const_set("SERVO_STATE_RESET",$SERVO_STATE_RESET) ;
Trk::const_set("SERVO_STATE_SETUP",$SERVO_STATE_SETUP) ;
Trk::const_set("SERVO_STATE_HOME", $SERVO_STATE_HOME) ;
Trk::const_set("SERVO_STATE_IDLE", $SERVO_STATE_IDLE) ;
Trk::const_set("SERVO_STATE_MOVE", $SERVO_STATE_MOVE) ;
Trk::const_set("SERVO_STATE_FAULT",$SERVO_STATE_FAULT) ;

# ----- r e g i s t e r s -----
#
($SERVO_REG_STATE, $SERVO_REG_TIMER, $SERVO_REG_POSITION,
 $SERVO_REG_DEBUG, $SERVO_REG_FAULT, $SERVO_REG_RESET,
 $SERVO_REG_READY, $SERVO_REG_MOVING,$SERVO_REG_TRIGGER,
 $SERVO_REG_TARGET, $SERVO_REG_SPEED, $SERVO_REG_ACCEL,
 $SERVO_REG_6010, $SERVO_REG_6011, 
 $SERVO_REG_6020, $SERVO_REG_6021, $SERVO_REG_6022, $SERVO_REG_6023,
 $SERVO_REG_7010, $SERVO_REG_7011, $SERVO_REG_7012, $SERVO_REG_7020,
 $SERVO_REG_7021, $SERVO_REG_7022, $SERVO_REG_7023, $SERVO_REG_7024,
 $SERVO_REG_7025, $SERVO_REG_7026, $SERVO_REG_7027, $SERVO_REG_7028,
 $SERVO_REG_7029, $SERVO_REG_702A, $SERVO_REG_702B)
= (0,1,2,
   3,4,5,
   6,7,8,
   9,10,11,
   20,21,22,
   23,24,25,26,
   27,28,29,
   30,31,32,33,
   34,35,36,37,
   38,39,40) ;
Trk::const_set("SERVO_REG_STATE",$SERVO_REG_STATE) ;
Trk::const_set("SERVO_REG_TIMER",$SERVO_REG_TIMER) ;
Trk::const_set("SERVO_REG_POSITION",$SERVO_REG_POSITION) ;
Trk::const_set("SERVO_REG_DEBUG",$SERVO_REG_DEBUG) ;
Trk::const_set("SERVO_REG_FAULT",$SERVO_REG_FAULT) ;
Trk::const_set("SERVO_REG_RESET",$SERVO_REG_RESET) ;
Trk::const_set("SERVO_REG_READY",$SERVO_REG_READY) ;
Trk::const_set("SERVO_REG_MOVING",$SERVO_REG_MOVING) ;
Trk::const_set("SERVO_REG_TRIGGER",$SERVO_REG_TRIGGER) ;
Trk::const_set("SERVO_REG_TARGET",$SERVO_REG_TARGET) ;
Trk::const_set("SERVO_REG_SPEED",$SERVO_REG_SPEED) ;
Trk::const_set("SERVO_REG_ACCEL",$SERVO_REG_ACCEL) ;
Trk::const_set("SERVO_REG_6010",$SERVO_REG_6010) ;
Trk::const_set("SERVO_REG_6011",$SERVO_REG_6011) ;
Trk::const_set("SERVO_REG_6020",$SERVO_REG_6020) ;
Trk::const_set("SERVO_REG_6021",$SERVO_REG_6021) ;
Trk::const_set("SERVO_REG_6022",$SERVO_REG_6022) ;
Trk::const_set("SERVO_REG_6023",$SERVO_REG_6023) ;
Trk::const_set("SERVO_REG_7010",$SERVO_REG_7010) ;
Trk::const_set("SERVO_REG_7011",$SERVO_REG_7011) ;
Trk::const_set("SERVO_REG_7012",$SERVO_REG_7012) ;
Trk::const_set("SERVO_REG_7020",$SERVO_REG_7020) ;
Trk::const_set("SERVO_REG_7021",$SERVO_REG_7021) ;
Trk::const_set("SERVO_REG_7022",$SERVO_REG_7022) ;
Trk::const_set("SERVO_REG_7023",$SERVO_REG_7023) ;
Trk::const_set("SERVO_REG_7024",$SERVO_REG_7024) ;
Trk::const_set("SERVO_REG_7025",$SERVO_REG_7025) ;
Trk::const_set("SERVO_REG_7026",$SERVO_REG_7026) ;
Trk::const_set("SERVO_REG_7027",$SERVO_REG_7027) ;
Trk::const_set("SERVO_REG_7028",$SERVO_REG_7028) ;
Trk::const_set("SERVO_REG_7029",$SERVO_REG_7029) ;
Trk::const_set("SERVO_REG_702A",$SERVO_REG_702A) ;
Trk::const_set("SERVO_REG_702B",$SERVO_REG_702B) ;

# ----- u t i l i t i e s -----
#

# s@ ( dp -- reg0)
@code=qw( dp.carton.get ) ;
Trk::ex_compile('s@','reg 0 fetch',\@code) ;

# s! ( dp -- reg0)
@code=qw( dp.carton.set ) ;
Trk::ex_compile('s!','reg 0 store',\@code) ;

# s_set ( bit dp -- )
@code=qw(
  >r
  1 swap shl
  r@ s@ 
  or
  r@ s! 
  r> drop ) ;
Trk::ex_compile('s_set','reg 0 bit set',\@code) ;

# s_clr ( bit dp -- )
@code=qw(
  >r
  1 swap shl neg
  r@ s@ 
  and 
  r@ s! 
  r> drop ) ;
Trk::ex_compile('s_clr','reg 0 bit clr',\@code) ;

# _servo_fault? ( dp - f )
#
@code=qw(
  >r

  SERVO_REG_6010 r@ dp.register.get s@
  1 15 shl and 
  
  r> drop ) ;
Trk::ex_compile('_servo_fault?','servo faulted',\@code) ;

# ----- s t a t e s -----
#

# _servo_state_init ( dp -- )
#
# ready off
# moving off
# reset off
#
# set 7010 IN0-IN5 (bits 0-5) to 1  (input step 1)
# write 0 to 7012 (start off)
#
# write fff0 to 7010 to override all settings in step 1
# write 1    to 7020 for ABS movement mode
# write value of SPEED rp to 7021 (speed)
# write value of ACCEL rp to 7023 (accel)
# write value of ACCEL rp to 7024 (decel) 
# write 100 to 7028 (moving force)
# write 5 to 702B (in position)
#
# read reg 0 6020 to POSITION
#
# set 7010 SVON    (bit 9)    to 1  (servo on)
# if 6010 SVRE (bit 9) is 1
#   goto RESET 
#
@code=qw(
  >r

  SERVO_REG_READY  r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get off
  SERVO_REG_RESET  r@ dp.register.get off
 
  0 SERVO_REG_7010 r@ dp.register.get s!

  1 SERVO_REG_7010 r@ dp.register.get s!

  0 SERVO_REG_7012 r@ dp.register.get s!

  65520 SERVO_REG_7011 r@ dp.register.get s!
  1     SERVO_REG_7020 r@ dp.register.get s! 
  SERVO_REG_SPEED r@ dp.register.get rp.value.get
        SERVO_REG_7021 r@ dp.register.get s!
  SERVO_REG_ACCEL r@ dp.register.get rp.value.get
        SERVO_REG_7023 r@ dp.register.get s!
  SERVO_REG_ACCEL r@ dp.register.get rp.value.get
        SERVO_REG_7024 r@ dp.register.get s!
  100   SERVO_REG_7028 r@ dp.register.get s!
  5     SERVO_REG_702B r@ dp.register.get s!

  SERVO_REG_6020 r@ dp.register.get s@
    SERVO_REG_POSITION r@ dp.register.set

  9  SERVO_REG_7010 r@ dp.register.get s_set 

  SERVO_REG_6010 r@ dp.register.get s@
  1 9 shl and if
    0 SERVO_REG_TIMER r@ dp.register.set
    SERVO_STATE_RESET SERVO_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_servo_state_init","servo state init",\@code) ;

# _servo_state_reset ( dp -- )
#
# ready off
# moving off
#
# trigger on
#
# if 6010 ALARM (bit 15) is 1
#   set 7010 RESET (bit 11) to 1
# else
#   set 7010 RESET (bit 11) to 0
#   goto SETUP 
@code=qw(
  >r

  SERVO_REG_READY r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get off

  SERVO_REG_TRIGGER r@ dp.register.get on

  SERVO_REG_6010 r@ dp.register.get s@
  1 15 shl and
  if
    11 SERVO_REG_7010 r@ dp.register.get s_set
  else
    11 SERVO_REG_7010 r@ dp.register.get s_clr
    12 SERVO_REG_7010 r@ dp.register.get s_set
    SERVO_STATE_SETUP SERVO_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_servo_state_reset","servo state reset",\@code) ;

# _servo_state_setup ( dp -- )
#
# ready off
# moving off
#
# set 7010 SETUP (bit 12) on
# if 6010 BUSY (bit 8) is on 
#   set 7010 SETUP (bit 12) off
#   goto HOME 
# if 6010 SETON (bit 10) is on
#   set 7010 SETUP (bit 12) off
#   goto HOME
@code=qw(
  >r

  SERVO_REG_READY  r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get off

  12  SERVO_REG_7010 r@ dp.register.get  s_set

  SERVO_REG_6010 r@ dp.register.get s@
  1 8 shl and 
  if
    12 SERVO_REG_7010 r@ dp.register.get s_clr
    SERVO_STATE_HOME SERVO_REG_STATE r@ dp.register.set
  then

  SERVO_REG_6010 r@ dp.register.get s@
  1 10 shl and 
  if
    12 SERVO_REG_7010 r@ dp.register.get s_clr
    SERVO_STATE_HOME SERVO_REG_STATE r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_servo_state_setup","servo state setup",\@code) ;

# _servo_state_home ( dp -- )
#
# ready off
# moving on
#
# get 6020 and save in POSITION
#
# if 6010 BUSY (bit 8) is off
#   if 6010 SETON (bit 10) is on
#     if 6010 INP (bit 11) is on
#       goto IDLE
@code=qw(
  >r

  r@ _servo_fault?
  if
    SERVO_REG_RESET r@ dp.register.get off
    SERVO_STATE_FAULT SERVO_REG_STATE r@ dp.register.set
  then

  SERVO_REG_READY  r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get on

  SERVO_REG_6020 r@ dp.register.get s@
    SERVO_REG_POSITION r@ dp.register.set

  SERVO_REG_6010 r@ dp.register.get s@
  1 8 shl and not
  if
    SERVO_REG_6010 r@ dp.register.get s@
    1 10 shl and
    if
      SERVO_REG_6010 r@ dp.register.get s@
      1 11 shl and
      if
        SERVO_STATE_IDLE SERVO_REG_STATE r@ dp.register.set
      then
    then 
  then
  
  r> drop ) ;
Trk::ex_compile("_servo_state_home","servo state home",\@code) ;


# _servo_state_idle ( dp -- )
#
# ready on
# moving off
#
# get 6020 save in POSITION
#
# write value of SPEED rp to 7021 (speed)
# write value of ACCEL rp to 7023 (accel)
# write value of ACCEL rp to 7024 (decel) 
#
# if trigger 
#  trigger off
#  write TARGET rp to 7022
#  write 1 to 7012 (start) 
#
@code=qw(
  >r

  r@ _servo_fault?
  if
    SERVO_REG_RESET r@ dp.register.get off
    SERVO_STATE_FAULT SERVO_REG_STATE r@ dp.register.set
  then

  SERVO_REG_READY  r@ dp.register.get on
  SERVO_REG_MOVING r@ dp.register.get off

  SERVO_REG_6020 r@ dp.register.get s@
    SERVO_REG_POSITION r@ dp.register.set

  SERVO_REG_SPEED r@ dp.register.get rp.value.get
        SERVO_REG_7021 r@ dp.register.get s!
  SERVO_REG_ACCEL r@ dp.register.get rp.value.get
        SERVO_REG_7023 r@ dp.register.get s!
  SERVO_REG_ACCEL r@ dp.register.get rp.value.get
        SERVO_REG_7024 r@ dp.register.get s!

  SERVO_REG_TRIGGER r@ dp.register.get tst
  if
    SERVO_REG_TRIGGER r@ dp.register.get off
    SERVO_REG_TARGET r@ dp.register.get rp.value.get
    SERVO_REG_7022     r@ dp.register.get s!
    1 SERVO_REG_7012 r@ dp.register.get s!    
    SERVO_STATE_MOVE SERVO_REG_STATE r@ dp.register.set
  then
    
  r> drop ) ;
Trk::ex_compile("_servo_state_idle","servo state idle",\@code) ;

# _servo_state_move ( dp -- )
#
# ready off
# moving on
#
# get 6020 save to POSITION
#
# if 6010 BUSY (bit 8) is 0
#   write 0 to 7012 (start)
#   goto IDLE
@code=qw(
  >r

  r@ _servo_fault?
  if
    SERVO_REG_RESET r@ dp.register.get off
    SERVO_STATE_FAULT SERVO_REG_STATE r@ dp.register.set
  then

  SERVO_REG_READY  r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get on
 
  SERVO_REG_6020 r@ dp.register.get s@
    SERVO_REG_POSITION r@ dp.register.set

  SERVO_REG_6010 r@ dp.register.get s@
  1 8 shl and not
  if
    0 SERVO_REG_7012 r@ dp.register.get s!    
    SERVO_STATE_IDLE SERVO_REG_STATE r@ dp.register.set
  then
 
  r> drop ) ;
Trk::ex_compile("_servo_state_move","servo state move",\@code) ;

# _servo_state_fault( dp -- )
#
@code=qw(
  >r

  SERVO_REG_READY  r@ dp.register.get off
  SERVO_REG_MOVING r@ dp.register.get off 

  SERVO_REG_FAULT  r@ dp.register.get on

  SERVO_REG_RESET  r@ dp.register.get tst
  if
    SERVO_REG_RESET r@ dp.register.get off
    SERVO_REG_FAULT r@ dp.register.get off
    SERVO_STATE_INIT SERVO_REG_STATE r@ dp.register.set
  then
 
  r> drop ) ;
Trk::ex_compile("_servo_state_fault","servo state fault",\@code) ;

# ----- m a c h i n e -----
#

# _servo_machine ( dp -- )
#
@code=qw(
  >r

  SERVO_REG_DEBUG r@ dp.register.get tst not
  if

    SERVO_REG_STATE r@ dp.register.get
      dup SERVO_STATE_INIT  = if r@ _servo_state_init  then
      dup SERVO_STATE_RESET = if r@ _servo_state_reset then
      dup SERVO_STATE_SETUP = if r@ _servo_state_setup then
      dup SERVO_STATE_HOME  = if r@ _servo_state_home  then
      dup SERVO_STATE_IDLE  = if r@ _servo_state_idle  then
      dup SERVO_STATE_MOVE  = if r@ _servo_state_move  then
      dup SERVO_STATE_FAULT = if r@ _servo_state_fault then
    drop

  then

  r> drop ) ;
Trk::ex_compile("_servo_machine","servo machine",\@code) ;

# create servo
sub
createServo
  {
  my($name, $data) = @_ ;
  my $val ;

  my $dp = dp::virtual($name,$name.' servo') ;

  my $cp ;

  $cp = dp::virtual($name.'_fault',$name.' fault') ;
  Trk::dp_registerset($dp,$SERVO_REG_FAULT,$cp) ;

  $cp = dp::virtual($name.'_ready',$name.' ready') ;
  Trk::dp_registerset($dp,$SERVO_REG_READY,$cp) ;

  $cp = dp::virtual($name.'_reset',$name.' reset') ;
  Trk::dp_registerset($dp,$SERVO_REG_RESET,$cp) ;

  $cp = dp::virtual($name.'_moving',$name.' moving') ;
  Trk::dp_registerset($dp,$SERVO_REG_MOVING,$cp) ;
  
  $cp = dp::virtual($name.'_debug',$name.' debug') ;
  Trk::dp_registerset($dp,$SERVO_REG_DEBUG,$cp) ;
  
  $cp = dp::virtual($name.'_trigger',$name.' trigger') ;
  Trk::dp_registerset($dp,$SERVO_REG_TRIGGER,$cp) ;
  
  my $rp ;


  $rp = rp::const($name.'_target',0,$name.' target') ;
  Trk::dp_registerset($dp,$SERVO_REG_TARGET,$rp) ;

  $rp = rp::const($name.'_speed',1000,$name.' speed') ;
  Trk::dp_registerset($dp,$SERVO_REG_SPEED,$rp) ;

  $rp = rp::const($name.'_accel',1000,$name.' accel') ;
  Trk::dp_registerset($dp,$SERVO_REG_ACCEL,$rp) ;

  my $prefix = $name ;
  if(($val = $data->{'prefix'}) ne '')
    { $prefix = $val } ;

  Trk::dp_registerset($dp,$SERVO_REG_6010,dp::handle($prefix.'.6010')) ;
  Trk::dp_registerset($dp,$SERVO_REG_6011,dp::handle($prefix.'.6011')) ;
  Trk::dp_registerset($dp,$SERVO_REG_6020,dp::handle($prefix.'.6020')) ;
  Trk::dp_registerset($dp,$SERVO_REG_6021,dp::handle($prefix.'.6021')) ;
  Trk::dp_registerset($dp,$SERVO_REG_6022,dp::handle($prefix.'.6022')) ;
  Trk::dp_registerset($dp,$SERVO_REG_6023,dp::handle($prefix.'.6023')) ;

  Trk::dp_registerset($dp,$SERVO_REG_7010,dp::handle($prefix.'.7010')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7011,dp::handle($prefix.'.7011')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7012,dp::handle($prefix.'.7012')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7020,dp::handle($prefix.'.7020')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7021,dp::handle($prefix.'.7021')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7022,dp::handle($prefix.'.7022')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7023,dp::handle($prefix.'.7023')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7024,dp::handle($prefix.'.7024')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7025,dp::handle($prefix.'.7025')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7026,dp::handle($prefix.'.7026')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7027,dp::handle($prefix.'.7027')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7028,dp::handle($prefix.'.7028')) ;
  Trk::dp_registerset($dp,$SERVO_REG_7029,dp::handle($prefix.'.7029')) ;
  Trk::dp_registerset($dp,$SERVO_REG_702A,dp::handle($prefix.'.702a')) ;
  Trk::dp_registerset($dp,$SERVO_REG_702B,dp::handle($prefix.'.702b')) ;

  my @stub = ($name, '_servo_machine') ;
  Trk::ex_compile($name.'_machine',$name.' machine',\@stub) ;
  trakbase::leading('tm_10ms',$name.'_machine',$name.' machine') ;
  my $tmp = pop(@stub) ;
  
  } ;

1;
