#!/usr/bin/perl
#
# PFI-2 Control
#

require Trk ;
require trakbase ;
require dp ;
require rp ;
require timer ;

my @code ;

# ----- r e g i s t e r s -----
#
my ($PFI_REG_BOX,              $PFI_REG_STATE,             $PFI_REG_TOTAL,     $PFI_REG_NEXT,       $PFI_REG_COUNT,
    $PFI_REG_MACHINE,          $PFI_REG_MACHINE1,          $PFI_REG_MACHINE2,  $PFI_REG_MACHINE3,
    $PFI_REG_TIMER, 
    $PFI_REG_RUN,              $PFI_REG_GO,
    $PFI_REG_MACHINE4,
    $PFI_REG_IS_RAM_UP,        $PFI_REG_IS_RAM_DOWN, 
    $PFI_REG_IS_BAIL_UP,       $PFI_REG_IS_BAIL_DOWN,
    $PFI_REG_IS_Y_GRIP,
    $PFI_REG_IS_CRIMP1_EXTEND, $PFI_REG_IS_CRIMP1_RETRACT,
    $PFI_REG_IS_CRIMP2_EXTEND, $PFI_REG_IS_CRIMP2_RETRACT,
    $PFI_REG_PE_SCAN,          $PFI_REG_PE_FORMAX,         $PFI_REG_PE_POCKET,
    $PFI_REG_RAM_DOWN,         $PFI_REG_RAM_UP,
    $PFI_REG_BAIL, 
    $PFI_REG_Y_GRIP,
    $PFI_REG_CRIMP1,           $PFI_REG_CRIMP2,
    $PFI_REG_SCAN_TRIGGER,
    $PFI_REG_SERVO,            $PFI_REG_SERVO_HI,          $PFI_REG_SERVO_LO,  $PFI_REG_SERVO_TOL,
    $PFI_REG_TIMEOUT,          $PFI_REG_GUARD,
    $PFI_REG_FAULT,            $PFI_REG_RESET,             $PFI_REG_DEBUG,
    $PFI_REG_FEED_DETECT,      $PFI_REG_FEED_RUN,
    $PFI_REG_BOX_1,            $PFI_REG_TOTAL_CURR,
    $PFI_REG_EXT_PURGE,        $PFI_REG_PURGING,           $PFI_REG_READY
    )
 = (
    0, 1, 2, 3, 4,
    5, 6, 7, 8,
    9,
   10,11,
   12,
   13,14,
   15,16,
   17,
   18,19,
   20,21,
   22,23,24,
   25,26,
   27,
   28,
   29,30,
   31,
   32,33,34,35,
   36,37,
   38,39,40,
   41,42,
   43,44,
   45,46,47);

Trk::const_set('PFI_REG_BOX', $PFI_REG_BOX);
Trk::const_set('PFI_REG_STATE', $PFI_REG_STATE);
Trk::const_set('PFI_REG_NEXT', $PFI_REG_NEXT);
Trk::const_set('PFI_REG_TOTAL', $PFI_REG_TOTAL);
Trk::const_set('PFI_REG_COUNT', $PFI_REG_COUNT);
Trk::const_set('PFI_REG_MACHINE', $PFI_REG_MACHINE);
Trk::const_set('PFI_REG_MACHINE_1', $PFI_REG_MACHINE1);
Trk::const_set('PFI_REG_MACHINE_2', $PFI_REG_MACHINE2);
Trk::const_set('PFI_REG_MACHINE_3', $PFI_REG_MACHINE3);
Trk::const_set('PFI_REG_MACHINE_4', $PFI_REG_MACHINE4);
Trk::const_set('PFI_REG_TIMER', $PFI_REG_TIMER);
Trk::const_set('PFI_REG_RUN', $PFI_REG_RUN);
Trk::const_set('PFI_REG_GO', $PFI_REG_GO);
Trk::const_set('PFI_REG_IS_RAM_UP', $PFI_REG_IS_RAM_UP);
Trk::const_set('PFI_REG_IS_RAM_DOWN', $PFI_REG_IS_RAM_DOWN);
Trk::const_set('PFI_REG_IS_BAIL_UP', $PFI_REG_IS_BAIL_UP);
Trk::const_set('PFI_REG_IS_BAIL_DOWN', $PFI_REG_IS_BAIL_DOWN);
Trk::const_set('PFI_REG_IS_Y_GRIP', $PFI_REG_IS_Y_GRIP);
Trk::const_set('PFI_REG_IS_CRIMP1_EXTEND', $PFI_REG_IS_CRIMP1_EXTEND);
Trk::const_set('PFI_REG_IS_CRIMP1_RETRACT', $PFI_REG_IS_CRIMP1_RETRACT);
Trk::const_set('PFI_REG_IS_CRIMP2_EXTEND', $PFI_REG_IS_CRIMP2_EXTEND);
Trk::const_set('PFI_REG_IS_CRIMP2_RETRACT', $PFI_REG_IS_CRIMP2_RETRACT);
Trk::const_set('PFI_REG_PE_SCAN', $PFI_REG_PE_SCAN);
Trk::const_set('PFI_REG_PE_FORMAX', $PFI_REG_PE_FORMAX);
Trk::const_set('PFI_REG_PE_POCKET', $PFI_REG_PE_POCKET);
Trk::const_set('PFI_REG_RAM_DOWN', $PFI_REG_RAM_DOWN);
Trk::const_set('PFI_REG_RAM_UP', $PFI_REG_RAM_UP);
Trk::const_set('PFI_REG_BAIL', $PFI_REG_BAIL);
Trk::const_set('PFI_REG_Y_GRIP', $PFI_REG_Y_GRIP);
Trk::const_set('PFI_REG_CRIMP1', $PFI_REG_CRIMP1);
Trk::const_set('PFI_REG_CRIMP2', $PFI_REG_CRIMP2);
Trk::const_set('PFI_REG_SCAN_TRIGGER', $PFI_REG_SCAN_TRIGGER);
Trk::const_set('PFI_REG_SERVO', $PFI_REG_SERVO);
Trk::const_set('PFI_REG_SERVO_HI', $PFI_REG_SERVO_HI);
Trk::const_set('PFI_REG_SERVO_LO', $PFI_REG_SERVO_LO);
Trk::const_set('PFI_REG_SERVO_TOL', $PFI_REG_SERVO_TOL);
Trk::const_set('PFI_REG_TIMEOUT', $PFI_REG_TIMEOUT);
Trk::const_set('PFI_REG_GUARD', $PFI_REG_GUARD);
Trk::const_set('PFI_REG_FAULT', $PFI_REG_FAULT);
Trk::const_set('PFI_REG_RESET', $PFI_REG_RESET);
Trk::const_set('PFI_REG_DEBUG', $PFI_REG_DEBUG);
Trk::const_set('PFI_REG_FEED_DETECT', $PFI_REG_FEED_DETECT);
Trk::const_set('PFI_REG_FEED_RUN', $PFI_REG_FEED_RUN);
Trk::const_set('PFI_REG_BOX_1', $PFI_REG_BOX_1);
Trk::const_set('PFI_REG_TOTAL_CURR', $PFI_REG_TOTAL_CURR);
Trk::const_set('PFI_REG_EXT_PURGE', $PFI_REG_EXT_PURGE);
Trk::const_set('PFI_REG_PURGING', $PFI_REG_PURGING);
Trk::const_set('PFI_REG_READY', $PFI_REG_READY);

# ----- e c o d e -----
#
Trk::const_set("PFI_ERR_UNKNOWN",         1000);
Trk::const_set("PFI_ERR_RAM_STUCK_UP",    1001);
Trk::const_set("PFI_ERR_RAM_STALL_UP",    1002);
Trk::const_set("PFI_ERR_RAM_STUCK_DOWN",  1003);
Trk::const_set("PFI_ERR_RAM_STALL_DOWN",  1004);
Trk::const_set("PFI_ERR_CRIMP1_STUCK_IN", 1005);
Trk::const_set("PFI_ERR_CRIMP1_STALL_IN", 1006);
Trk::const_set("PFI_ERR_CRIMP1_STUCK_OUT",1007);
Trk::const_set("PFI_ERR_CRIMP1_STALL_OUT",1008);
Trk::const_set("PFI_ERR_CRIMP2_STUCK_IN", 1009);
Trk::const_set("PFI_ERR_CRIMP2_STALL_IN", 1010);
Trk::const_set("PFI_ERR_CRIMP2_STUCK_OUT",1011);
Trk::const_set("PFI_ERR_CRIMP2_STALL_OUT",1012);
Trk::const_set("PFI_ERR_Y_GRIP_STUCK",    1013);
Trk::const_set("PFI_ERR_Y_GRIP_STALL",    1014);
Trk::const_set("PFI_ERR_NOT_HI",          1015);
Trk::const_set("PFI_ERR_NOT_LO",          1016);
Trk::const_set("PFI_ERR_BAIL_STUCK_UP",   1017) ;
Trk::const_set("PFI_ERR_BAIL_STALL_UP",   1018) ;
Trk::const_set("PFI_ERR_BAIL_STUCK_DOWN", 1019) ;
Trk::const_set("PFI_ERR_BAIL_STALL_DOWN", 1020) ;
Trk::const_set("PFI_ERR_PE_SCAN_JAM",     1021) ;
Trk::const_set("PFI_ERR_PE_SCAN_NONE",    1022) ;
Trk::const_set("PFI_ERR_PE_FORMAX_JAM",   1023) ;
Trk::const_set("PFI_ERR_PE_FORMAX_NONE",  1024) ;
Trk::const_set("PFI_ERR_PE_POCKET_JAM",   1025) ;
Trk::const_set("PFI_ERR_PE_POCKET_NONE",  1026) ;

# ----- T i m e r s -----
#

rp::const('pfi_feed_runup',     500,'pfi feed run-up (cycles)');
rp::const('pfi_feed_guard',     400,'pfi feed guard (cycles)');
rp::const('pfi_feed_timeout',   1500,'pfi feed guard (cycles)');
rp::const('pfi_feed_early',       0,'pfi feed early (cycles)');
rp::const('pfi_feed_fill',     2000,'pfi feed fill (cycles)');
rp::const('pfi_feed_runover',     200,'pfi feed run-over (cycles)');
rp::const('pfi_feed_runout',    200,'pfi feed run-out (cycles)');
rp::const('pfi_feed_drain',    200,'pfi feed run-out (cycles)');

rp::const('pfi_formax_clear', 10, 'pfi formax clear timeout (cycles)');
rp::const('pfi_formax_cycles', 20, 'pfi formax cycles to run');
rp::const('pfi_timeout', 1000, 'pfi timeout (cycles)');
rp::const('pfi_guard',   1000, 'pfi guard (cycles)');
rp::const('pfi_batch_pages',   5, 'number of pages in batch');

# ----- u t i l -----
#
@code=qw( 1 31 shl 1 - ) ;
Trk::ex_compile('alt0','alternate zero',\@code) ;

@code=qw( 0 swap - ) ;
Trk::ex_compile('negate','negate',\@code) ;

@code=qw( dup alt0 > if negate then ) ;
Trk::ex_compile('abs','absolute value',\@code) ;

# _pfi_reg_get@ ( dp -- dp )
@code=qw(
  >r
  
  PFI_REG_MACHINE r@ dp.register.get dp.register.get 
  
  r> drop);
Trk::ex_compile('_pfi_reg_get@','pfi get register',\@code) ;

# _pfi_servo@ ( dp -- dp )
#
@code=qw(
  >r
 
  PFI_REG_SERVO r@ dp.register.get

  r> drop ) ;
Trk::ex_compile('_pfi_servo@','pfi servo fetch',\@code) ;

# _pfi_servo_ready? ( dp -- f)
#
@code=qw(
  >r

  SERVO_REG_READY  r@ _pfi_servo@  dp.register.get tst 

  r> drop ) ;
Trk::ex_compile('_pfi_servo_ready?','pfi servo ready',\@code) ;

# _pfi_servo_hi ( dp -- )
#
@code=qw(
  >r

  PFI_REG_SERVO_HI r@ dp.register.get rp.value.get 
  SERVO_REG_TARGET  r@ _pfi_servo@  dp.register.get rp.value.set 

  SERVO_REG_TRIGGER  r@ _pfi_servo@  dp.register.get on

  r> drop ) ;
Trk::ex_compile('_pfi_servo_hi','pfi servo to hi',\@code) ;

# _pfi_servo_hi? ( dp -- f)
#
@code=qw(
  >r

  SERVO_REG_POSITION  r@ _pfi_servo@ dp.register.get 
  PFI_REG_SERVO_HI r@ dp.register.get  rp.value.get - abs 
  PFI_REG_SERVO_TOL r@ dp.register.get rp.value.get <

  r> drop ) ;
Trk::ex_compile('_pfi_servo_hi?','pfi servo at hi',\@code) ;

# _pfi_servo_lo ( dp -- )
#
@code=qw(
  >r

  PFI_REG_SERVO_LO r@ dp.register.get rp.value.get 
  SERVO_REG_TARGET  r@ _pfi_servo@  dp.register.get rp.value.set 

  SERVO_REG_TRIGGER  r@ _pfi_servo@  dp.register.get on

  r> drop ) ;
Trk::ex_compile('_pfi_servo_lo','pfi servo to lo',\@code) ;

# _pfi_servo_lo? ( dp -- f)
#
@code=qw(
  >r

  SERVO_REG_POSITION  r@ _pfi_servo@ dp.register.get 
  PFI_REG_SERVO_LO r@ dp.register.get  rp.value.get - abs
  PFI_REG_SERVO_TOL r@ dp.register.get rp.value.get <

  r> drop ) ;
Trk::ex_compile('_pfi_servo_lo?','pfi servo at lo',\@code) ;

# ----- s t a t e s   p f i -----
#
($PFI_STATE_INIT, $PFI_STATE_PURGE,    
 $PFI_STATE_PURGE_A, $PFI_STATE_PURGE_B,  $PFI_STATE_PURGE_C,
 $PFI_STATE_PURGE_D, $PFI_STATE_PURGE_E,  $PFI_STATE_PURGE_F,
 $PFI_STATE_PURGE_G, $PFI_STATE_PURGE_H,  $PFI_STATE_PURGE_I, 
 $PFI_STATE_PURGE_K, 
 $PFI_STATE_RESET, $PFI_STATE_RUN, $PFI_STATE_FAULT
 )
= (0,1,
   2,3,4,
   5,6,7, 
   8,9,10,
   11,
   12,13,99
   ) ;
Trk::const_set("PFI_STATE_INIT",    $PFI_STATE_INIT) ;
Trk::const_set("PFI_STATE_PURGE",   $PFI_STATE_PURGE) ;
Trk::const_set("PFI_STATE_PURGE_A", $PFI_STATE_PURGE_A) ;
Trk::const_set("PFI_STATE_PURGE_B", $PFI_STATE_PURGE_B) ;
Trk::const_set("PFI_STATE_PURGE_C", $PFI_STATE_PURGE_C) ;
Trk::const_set("PFI_STATE_PURGE_D", $PFI_STATE_PURGE_D) ;
Trk::const_set("PFI_STATE_PURGE_E", $PFI_STATE_PURGE_E) ;
Trk::const_set("PFI_STATE_PURGE_F", $PFI_STATE_PURGE_F) ;
Trk::const_set("PFI_STATE_PURGE_G", $PFI_STATE_PURGE_G) ;
Trk::const_set("PFI_STATE_PURGE_H", $PFI_STATE_PURGE_H) ;
Trk::const_set("PFI_STATE_PURGE_I", $PFI_STATE_PURGE_I) ;
Trk::const_set("PFI_STATE_PURGE_K", $PFI_STATE_PURGE_K) ;
Trk::const_set("PFI_STATE_RESET",   $PFI_STATE_RESET) ;
Trk::const_set("PFI_STATE_RUN",     $PFI_STATE_RUN) ;
Trk::const_set("PFI_STATE_FAULT",   $PFI_STATE_FAULT) ;

# _pfi_state_init ( dp -- )
#
# dp on
# carton1 <- -1
#
# ram_down off
# ram_up on
# crimp1 off
# crimp2 off
# y_grip off
# scan_trigger off
# run2 off
# trip2 off
#
# servo__hi
#
# goto purge a (home)
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get off

  PFI_REG_FAULT r@ dp.register.get off

  0 PFI_REG_RUN PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_2 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_3 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_4 r@ dp.register.get dp.register.set

  BOX_NONE PFI_REG_BOX PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  BOX_NONE PFI_REG_BOX_1 PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  BOX_NONE PFI_REG_BOX PFI_REG_MACHINE_2 r@ dp.register.get dp.register.set
  BOX_NONE PFI_REG_BOX PFI_REG_MACHINE_3 r@ dp.register.get dp.register.set
  BOX_NONE PFI_REG_BOX PFI_REG_MACHINE_4 r@ dp.register.get dp.register.set

  0 PFI_REG_STATE PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  0 PFI_REG_STATE PFI_REG_MACHINE_2 r@ dp.register.get dp.register.set
  0 PFI_REG_STATE PFI_REG_MACHINE_3 r@ dp.register.get dp.register.set
  0 PFI_REG_STATE PFI_REG_MACHINE_4 r@ dp.register.get dp.register.set

  PFI_REG_RAM_DOWN r@ dp.register.get off
  PFI_REG_RAM_UP   r@ dp.register.get on
  PFI_REG_CRIMP1   r@ dp.register.get off
  PFI_REG_CRIMP2   r@ dp.register.get off
  PFI_REG_SCAN_TRIGGER r@ dp.register.get off
  PFI_REG_Y_GRIP   r@ dp.register.get off

  r@ _pfi_servo_hi 

  PFI_REG_RUN r@ dp.register.get tst if
    PFI_STATE_PURGE_K r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_init","pfi state init",\@code) ;

# _pfi_state_purge_a ( dp -- )
# home
#
# if timeout goto fault
#
# if servo at hi
#   if pe_pocket
#     crimp1 on
#     crimp2 on
#     goto purge d
#   else
#     ram up off
#     ram down on
#     bail on 
#     crimp2 on
#     goto purge b 
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on
 
  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    PFI_ERR_NOT_HI r@ _zone_error_msg 

    SERVO_STATE_RESET SERVO_REG_STATE r@ _pfi_servo@ dp.register.set

    PFI_STATE_FAULT r@ _zone_state!
  then

  r@ _pfi_servo_hi?
  if
    PFI_REG_PE_POCKET r@ dp.register.get tst
    if
      PFI_REG_CRIMP1 r@ dp.register.get on
      PFI_REG_CRIMP2 r@ dp.register.get on
      PFI_REG_RAM_UP   r@ dp.register.get on
      PFI_REG_RAM_DOWN r@ dp.register.get off
      PFI_STATE_PURGE_D r@ _zone_state!           
    else
      PFI_REG_RAM_UP   r@ dp.register.get off
      PFI_REG_RAM_DOWN r@ dp.register.get on
      PFI_REG_CRIMP2   r@ dp.register.get on
      PFI_REG_BAIL     r@ dp.register.get on
      PFI_STATE_PURGE_B r@ _zone_state!            
    then
  then
  
  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_a","pfi state purge a",\@code) ;


# _pfi_state_purge_b ( dp -- )
# ram down
#
# if guard goto fault
#
# if is_ram_down and not is_ram_up 
#   ram_down off
#   ram_up   on
#   bail off
#   goto purge c
# 
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ off
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ off

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_RAM_UP r@ dp.register.get tst if
      PFI_ERR_RAM_STUCK_UP r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_DOWN r@ dp.register.get tst not if
        PFI_ERR_RAM_STALL_DOWN r@ _zone_error_msg
      else
        PFI_ERR_UNKNOWN r@ _zone_error_msg
      then
    then

    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_DOWN r@ dp.register.get tst 
  PFI_REG_IS_RAM_UP   r@ dp.register.get tst not and if
    PFI_REG_RAM_DOWN r@ dp.register.get off
    PFI_REG_RAM_UP   r@ dp.register.get on
    PFI_REG_BAIL     r@ dp.register.get off
    PFI_STATE_PURGE_C r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_b","pfi state purge b",\@code) ;


# _pfi_state_purge_c ( dp -- )
# ram up 
#
# if timeout goto fault
#
# if is_ram_up and not is_ram_down 
#   if pe_pocket
#     crimp1 on
#     goto purge d (crimp)
#   else
#     crimp1 off
#     crimp2 off
#     goto reset
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    PFI_REG_IS_RAM_DOWN r@ dp.register.get tst if
      PFI_ERR_RAM_STUCK_DOWN r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_UP r@ dp.register.get tst not if
        PFI_ERR_RAM_STALL_UP r@ _zone_error_msg 
      else
        PFI_ERR_UNKNOWN      r@ _zone_error_msg
      then
    then

    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_UP   r@ dp.register.get tst
  PFI_REG_IS_RAM_DOWN r@ dp.register.get tst not and if

    PFI_REG_PE_POCKET r@ dp.register.get tst if
      PFI_REG_CRIMP1 r@ dp.register.get on 
      PFI_STATE_PURGE_E r@ _zone_state!
    else
      PFI_REG_CRIMP1 r@ dp.register.get off
      PFI_REG_CRIMP2 r@ dp.register.get off
      PFI_STATE_RESET r@ _zone_state!
    then
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_c","pfi state purge c",\@code) ;

# _pfi_state_purge_d ( dp -- )
# ram up 
#
# if timeout goto fault
#
# if is_ram_up and not is_ram_down 
#   if pe_pocket
#     crimp1 on
#     goto purge d (crimp)
#   else
#     crimp1 off
#     crimp2 off
#     goto reset
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    PFI_REG_IS_RAM_DOWN r@ dp.register.get tst if
      PFI_ERR_RAM_STUCK_DOWN r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_UP r@ dp.register.get not if
        PFI_ERR_RAM_STALL_UP r@ _zone_error_msg 
      else
        PFI_ERR_UNKNOWN      r@ _zone_error_msg
      then
    then

    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_UP   r@ dp.register.get tst
  PFI_REG_IS_RAM_DOWN r@ dp.register.get tst not and if

    PFI_REG_PE_POCKET r@ dp.register.get tst if
      PFI_REG_CRIMP1 r@ dp.register.get on 
      PFI_STATE_PURGE_E r@ _zone_state!
    else
      PFI_REG_CRIMP1 r@ dp.register.get off
      PFI_REG_CRIMP2 r@ dp.register.get off
      PFI_STATE_RESET r@ _zone_state!
    then
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_d","pfi state purge d",\@code) ;


# _pfi_state_purge_e ( dp -- )
# crimp 
#
# if guard goto fault
#
# if  is_crimp1_extend and not is_crimp1_retract
# and is_crimp2_extend and not is_crimp2_retract
#   y_grip on 
#   goto purge_e
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst
    if
      PFI_ERR_CRIMP1_STUCK_IN r@ _zone_error_msg
    else
      PFI_REG_IS_CRIMP1_EXTEND r@ dp.register.get tst not
      if
        PFI_ERR_CRIMP1_STALL_OUT r@ _zone_error_msg
      else
        PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst
        if
          PFI_ERR_CRIMP2_STUCK_IN r@ _zone_error_msg
        else
          PFI_REG_IS_CRIMP2_EXTEND r@ dp.register.get tst not
          if
            PFI_ERR_CRIMP2_STALL_OUT r@ _zone_error_msg
          else
            PFI_ERR_UNKNOWN r@ _zone_error_msg
          then
        then
      then
    then
    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_CRIMP1_EXTEND  r@ dp.register.get tst
  PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst not and
  PFI_REG_IS_CRIMP2_EXTEND  r@ dp.register.get tst     and
  PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst not and
  if
    PFI_REG_Y_GRIP r@ dp.register.get on
    PFI_STATE_PURGE_F r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_e","pfi state purge e",\@code) ;


# _pfi_state_purge_f( dp -- )
# grip  
#
# if pfi_guard? goto fault
#
# if is_y_grip
#   crimp1 off 
#   crimp2 off 
#   goto purge f
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_Y_GRIP r@ dp.register.get tst
    if
      PFI_ERR_Y_GRIP_STALL r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_Y_GRIP r@ dp.register.get tst not 
  if
    PFI_REG_CRIMP1 r@ dp.register.get off
    PFI_REG_CRIMP2 r@ dp.register.get off
    PFI_STATE_PURGE_G r@ _zone_state!
  then

  
  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_f","pfi state purge f",\@code) ;


# _pfi_state_purge_g ( dp -- )
# uncrimp 
#
# if guard goto fault
#
# if  is_crimp1_retract and not is_crimp1_extend
# and is_crimp2_retract and not is_Crimp2_extend  
#   servo lo
#   goto purge g
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_CRIMP1_EXTEND r@ dp.register.get tst 
    if
      PFI_ERR_CRIMP1_STUCK_OUT r@ _zone_error_msg
    else
      PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst not
      if
        PFI_ERR_CRIMP1_STALL_IN r@ _zone_error_msg
      else
        PFI_REG_IS_CRIMP2_EXTEND r@ dp.register.get tst
        if
          PFI_ERR_CRIMP2_STUCK_OUT r@ _zone_error_msg
        else
          PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst not
          if
            PFI_ERR_CRIMP2_STALL_IN r@ _zone_error_msg
          else
            PFI_ERR_UNKNOWN r@ _zone_error_msg
          then
        then
      then
    then
    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst
  PFI_REG_IS_CRIMP1_EXTEND  r@ dp.register.get tst not and
  PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst     and
  PFI_REG_IS_CRIMP2_EXTEND  r@ dp.register.get tst not and if
    r@ _pfi_servo_lo
    PFI_STATE_PURGE_H r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_g","pfi state purge g",\@code) ;

# _pfi_state_purge_h ( dp -- )
# extend 
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    r@ _pfi_servo_lo? not
    if
      PFI_ERR_NOT_LO r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI_STATE_FAULT r@ _zone_state!
  then

  r@ _pfi_servo_lo? if
    PFI_REG_Y_GRIP r@ dp.register.get off
    PFI_STATE_PURGE_I r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_h","pfi state purge h",\@code) ;

# _pfi_state_purge_i ( dp -- )
# ungrip
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_Y_GRIP r@ dp.register.get tst not if
      PFI_ERR_Y_GRIP_STUCK r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then

    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_Y_GRIP r@ dp.register.get tst if
    r@ _pfi_servo_hi
    PFI_STATE_PURGE_A r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_i","pfi state purge i",\@code) ;

# _pfi_state_purge_k ( dp -- )
# ungrip
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on

  r@ _zone_elapsed@ pfi_feed_runup rp.value.get > if
    PFI_STATE_PURGE_A r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_purge_k","pfi state purge k",\@code) ;

# _pfi_state_reset ( dp -- )
#
# dp on
#
# if timeout goto fault
#
# if is_ram_up true
# and is_ram_down false
# and is_bail_up false
# and is_bail_down true
# and is_crimp1_extend false
# and is_crimp1_retract true
# and is_crimp2_extend false
# and is_crimp2_retract true
# and pe_scan false
# and pe_formax false
# and pe_pocket false
#   run2 on
#   goto idle
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get on

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ off
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ off

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    PFI_REG_IS_RAM_UP r@ dp.register.get tst not
    if
      PFI_ERR_RAM_STALL_UP r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_DOWN r@ dp.register.get tst 
      if
        PFI_ERR_RAM_STUCK_DOWN r@ _zone_error_msg
      else
        PFI_REG_IS_BAIL_UP r@ dp.register.get tst 
        if
          PFI_ERR_BAIL_STUCK_UP r@ _zone_error_msg
        else
          PFI_REG_IS_BAIL_DOWN r@ dp.register.get tst not
          if
            PFI_ERR_BAIL_STALL_DOWN r@ _zone_error_msg
          else
            PFI_REG_IS_CRIMP1_EXTEND r@ dp.register.get tst
            if
              PFI_ERR_CRIMP1_STUCK_OUT r@ _zone_error_msg
            else
              PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst not
              if
                PFI_ERR_CRIMP1_STALL_IN r@ _zone_error_msg
              else
                PFI_REG_IS_CRIMP2_EXTEND r@ dp.register.get tst 
                if
                  PFI_ERR_CRIMP2_STUCK_OUT r@ _zone_error_msg
                else
                  PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst not
                  if
                    PFI_ERR_CRIMP2_STALL_IN r@ _zone_error_msg
                  else
                    PFI_REG_PE_SCAN r@ dp.register.get tst 
                    if
                      PFI_ERR_PE_SCAN_JAM r@ _zone_error_msg
                    else
                      PFI_REG_PE_FORMAX r@ dp.register.get tst
                      if
                        PFI_ERR_PE_FORMAX_JAM r@ _zone_error_msg
                      else
                        PFI_REG_PE_POCKET r@ dp.register.get tst
                        if
                          PFI_ERR_PE_POCKET_JAM r@ _zone_error_msg
                        else
                          PFI_STATE_FAULT r@ _zone_state!
                        then
                      then
                    then
                  then
                then 
              then 
            then
          then   
        then
      then 
    then

    PFI_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_UP         r@ dp.register.get tst
  PFI_REG_IS_RAM_DOWN       r@ dp.register.get tst not and
  PFI_REG_IS_BAIL_UP        r@ dp.register.get tst not and
  PFI_REG_IS_BAIL_DOWN      r@ dp.register.get tst     and
  PFI_REG_IS_CRIMP1_EXTEND  r@ dp.register.get tst not and
  PFI_REG_IS_CRIMP1_RETRACT r@ dp.register.get tst     and
  PFI_REG_IS_CRIMP2_EXTEND  r@ dp.register.get tst not and
  PFI_REG_IS_CRIMP2_RETRACT r@ dp.register.get tst     and
  PFI_REG_PE_SCAN           r@ dp.register.get tst not and
  PFI_REG_PE_FORMAX         r@ dp.register.get tst not and
  PFI_REG_PE_POCKET         r@ dp.register.get tst not and
  if
    PFI_STATE_RUN r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_reset","pfi state reset",\@code) ;

# _pfi_state_run ( dp -- )
#

@code=qw(
  >r

  r@ off
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get on
  PFI_REG_PURGING r@ dp.register.get off

  1 PFI_REG_RUN PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  1 PFI_REG_RUN PFI_REG_MACHINE_2 r@ dp.register.get dp.register.set
  1 PFI_REG_RUN PFI_REG_MACHINE_3 r@ dp.register.get dp.register.set
  1 PFI_REG_RUN PFI_REG_MACHINE_4 r@ dp.register.get dp.register.set

  PFI_REG_RESET r@ dp.register.get tst 
  PFI_REG_EXT_PURGE r@ dp.register.get tst or if
    PFI_STATE_INIT r@ _zone_state! 
  then

  0 PFI_REG_STATE PFI_REG_MACHINE_1 r@ dp.register.get dp.register.get = 
  0 PFI_REG_STATE PFI_REG_MACHINE_2 r@ dp.register.get dp.register.get = and
  0 PFI_REG_STATE PFI_REG_MACHINE_3 r@ dp.register.get dp.register.get = and
  2 PFI_REG_STATE PFI_REG_MACHINE_4 r@ dp.register.get dp.register.get = and
  PFI_REG_RUN r@ dp.register.get tst not if
    PFI_STATE_INIT r@ _zone_state! 
  then

  99 PFI_REG_STATE PFI_REG_MACHINE_1 r@ dp.register.get dp.register.get = 
  99 PFI_REG_STATE PFI_REG_MACHINE_2 r@ dp.register.get dp.register.get = or
  99 PFI_REG_STATE PFI_REG_MACHINE_3 r@ dp.register.get dp.register.get = or
  99 PFI_REG_STATE PFI_REG_MACHINE_4 r@ dp.register.get dp.register.get = or if
    PFI_STATE_FAULT r@ _zone_state! 
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_run","pfi state run",\@code) ;

# _pfi_state_fault ( dp -- )
#

@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_READY r@ dp.register.get off
  PFI_REG_PURGING r@ dp.register.get off

  0 PFI_REG_RUN PFI_REG_MACHINE_1 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_2 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_3 r@ dp.register.get dp.register.set
  0 PFI_REG_RUN PFI_REG_MACHINE_4 r@ dp.register.get dp.register.set

  PFI_REG_FAULT r@ dp.register.get on

  PFI_REG_RESET r@ dp.register.get tst if
    SERVO_STATE_RESET SERVO_REG_STATE r@ _pfi_servo@ dp.register.set
    
    PFI_STATE_INIT r@ _zone_state! 
  then
  
  PFI_REG_RUN r@ dp.register.get tst not if
    PFI_STATE_INIT r@ _zone_state! 
  then

  r> drop ) ;
Trk::ex_compile("_pfi_state_fault","pfi state fault",\@code) ;

# ----- m a c h i n e  p f i -----
#

# _pfi_machine ( dp -- )
#
@code=qw(
  >r

  PFI_REG_DEBUG r@ dp.register.get tst not
  if

    PFI_REG_STATE r@ dp.register.get
      dup PFI_STATE_INIT     = if r@ _pfi_state_init     then
      dup PFI_STATE_PURGE_A  = if r@ _pfi_state_purge_a  then
      dup PFI_STATE_PURGE_B  = if r@ _pfi_state_purge_b  then
      dup PFI_STATE_PURGE_C  = if r@ _pfi_state_purge_c  then
      dup PFI_STATE_PURGE_D  = if r@ _pfi_state_purge_d  then
      dup PFI_STATE_PURGE_E  = if r@ _pfi_state_purge_e  then
      dup PFI_STATE_PURGE_F  = if r@ _pfi_state_purge_f  then
      dup PFI_STATE_PURGE_G  = if r@ _pfi_state_purge_g  then
      dup PFI_STATE_PURGE_H  = if r@ _pfi_state_purge_h  then
      dup PFI_STATE_PURGE_I  = if r@ _pfi_state_purge_i  then
      dup PFI_STATE_PURGE_K  = if r@ _pfi_state_purge_k  then
      dup PFI_STATE_RESET    = if r@ _pfi_state_reset    then
      dup PFI_STATE_RUN      = if r@ _pfi_state_run      then
      dup PFI_STATE_FAULT    = if r@ _pfi_state_fault    then
    drop

  then

  r> drop ) ;
Trk::ex_compile("_pfi_machine","pfi machine",\@code) ;


# ----- s t a t e s   1 -----
#
($PFI1_STATE_IDLE, $PFI1_STATE_FILL,    $PFI1_STATE_FILL_X,
 $PFI1_STATE_FULL, $PFI1_STATE_DRAIN_X, $PFI1_STATE_DRAIN, 
 $PFI1_STATE_FAULT 
 )
= (0,1,2,
   3,4,5,
   99
   ) ;
Trk::const_set("PFI1_STATE_IDLE",    $PFI1_STATE_IDLE) ;
Trk::const_set("PFI1_STATE_FILL",    $PFI1_STATE_FILL) ;
Trk::const_set("PFI1_STATE_FILL_X",  $PFI1_STATE_FILL_X) ;
Trk::const_set("PFI1_STATE_FULL",    $PFI1_STATE_FULL) ;
Trk::const_set("PFI1_STATE_DRAIN_X", $PFI1_STATE_DRAIN_X) ;
Trk::const_set("PFI1_STATE_DRAIN",   $PFI1_STATE_DRAIN) ;
Trk::const_set("PFI1_STATE_FAULT",   $PFI1_STATE_FAULT) ;

# _pfi1_state_idle ( dp -- )
#
# dp off
# feed_detect off
# feed_run on
#
# if run on and box is not none
#   go to state FILL

@code=qw(
  >r

  r@ off  
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get if
    PFI_REG_FEED_DETECT r@ _pfi_reg_get@ off
    PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on
  then

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_box@ BOX_NONE = not and if
    0 PFI_REG_COUNT r@ dp.register.set
    PFI1_STATE_FILL r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi1_state_idle","pfi 1 state idle",\@code) ;

# _pfi1_state_fill ( dp -- )
#
# dp on
# feed_detect on
# feed_run on
# 
# if formax entry pe blocked
#   go to state FILL_X

@code=qw(
  >r

  r@ _zone_box@ BOX_NONE = not if  
    r@ on  
  else  
    r@ off  
  then

  r@ _zone_tick

  r@ _zone_elapsed@ pfi_feed_timeout rp.value.get > if
    PFI_REG_COUNT r@ dp.register.get pfi_formax_cycles rp.value.get = if
      PFI_ERR_PE_SCAN_NONE r@ _zone_error_msg
      
      PFI1_STATE_FAULT r@ _zone_state!
    else
      PFI_REG_COUNT r@ dp.register.get
      1 +
      PFI_REG_COUNT r@ dp.register.set

      PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
      PFI_REG_FEED_RUN    r@ _pfi_reg_get@ off

      PFI_REG_COUNT r@ dp.register.get pfi_formax_clear rp.value.get % 0 = if
        r@ _zone_timer_clr
      then
    then
  else 
    PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
    PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on
  then

  PFI_REG_PE_SCAN r@ _pfi_reg_get@ tst if
    PFI1_STATE_FILL_X r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi1_state_fill","pfi 1 state fill",\@code) ;

# _pfi1_state_fill_x ( dp -- )
# 
# dp on
# feed_detect on
# feed_run on
#
# if elapsed > runover
#   go to state FULL
@code=qw(
  >r

  r@ _zone_box@ BOX_NONE = not if  
    r@ on  
  else  
    r@ off  
  then

  r@ _zone_tick

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on

  r@ _zone_elapsed@ pfi_feed_guard rp.value.get > if
    PFI_ERR_PE_SCAN_JAM r@ _zone_error_msg

    PFI1_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_PE_SCAN r@ _pfi_reg_get@ tst not if
    PFI1_STATE_FULL r@ _zone_state!
  then
  
  r@ _zone_box@ BOX_NONE = not 
  PFI_REG_TOTAL r@ dp.register.get 1 = and
  PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = and if
    r@ _zone_next_avail? if
      PFI_REG_BOX r@ dp.register.get PFI_REG_BOX_1 r@ dp.register.set
      BOX_NONE PFI_REG_BOX r@ dp.register.set
    then
  then

  r> drop
);
Trk::ex_compile('_pfi1_state_fill_x','pfi 1 state fill_x',\@code);

# _pfi1_state_full ( dp -- )
#
# dp on
# feed_detect on
# feed_run off
#
# if pfi2 available
#   pass box to pfi2
#   go to state DRAIN_X
@code=qw(
  >r

  r@ _zone_box@ BOX_NONE = not if  
    r@ on  
  else  
    r@ off  
  then

  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_next_avail?       and if
    PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = not if
      PFI_REG_BOX_1 r@ dp.register.get PFI_REG_BOX PFI_REG_NEXT r@ dp.register.get dp.register.set
      BOX_NONE PFI_REG_BOX_1 r@ dp.register.set

      1 PFI_REG_TOTAL PFI_REG_NEXT r@ dp.register.get dp.register.set
      1 PFI_REG_TOTAL_CURR r@ dp.register.set
    else
      PFI_REG_BOX r@ dp.register.get PFI_REG_BOX PFI_REG_NEXT r@ dp.register.get dp.register.set
      BOX_NONE PFI_REG_BOX r@ dp.register.set

      PFI_REG_TOTAL r@ dp.register.get PFI_REG_TOTAL_CURR r@ dp.register.set
      PFI_REG_TOTAL r@ dp.register.get PFI_REG_TOTAL PFI_REG_NEXT r@ dp.register.get dp.register.set
    then

    0 PFI_REG_PE_SCAN r@ dp.register.set
    1 PFI_REG_COUNT r@ dp.register.set

    PFI1_STATE_DRAIN r@ _zone_state!
  else
    PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
    PFI_REG_FEED_RUN    r@ _pfi_reg_get@ off
  
    r@ _zone_box@ BOX_NONE = not 
    PFI_REG_TOTAL r@ dp.register.get 1 = and
    PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = and if
      r@ _zone_next_avail? if
        PFI_REG_BOX r@ dp.register.get PFI_REG_BOX_1 r@ dp.register.set
        BOX_NONE PFI_REG_BOX r@ dp.register.set
      then
    then
  then

  r> drop
);
Trk::ex_compile('_pfi1_state_full','pfi 1 state full',\@code);

# _pfi1_state_drain_x ( dp -- )
#
# dp on
# feed_detect on
# feed_run on
#
# if formax entry pe off
#   go to state DRAIN
#
# if elapsed > guard
#   jam fault
@code=qw(
  >r

  r@ _zone_box@ BOX_NONE = not if  
    r@ on  
  else  
    r@ off  
  then

  r@ _zone_tick

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ on
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on

  r@ _zone_elapsed@ pfi_feed_drain rp.value.get > if
    r@ _zone_box@ BOX_NONE = not 
    PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = not or if
      PFI1_STATE_FILL r@ _zone_state!
    else
      PFI1_STATE_IDLE r@ _zone_state!
    then
  then
  
  r@ _zone_box@ BOX_NONE = not 
  PFI_REG_TOTAL r@ dp.register.get 1 = and
  PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = and if
    r@ _zone_next_avail? if
      PFI_REG_BOX r@ dp.register.get PFI_REG_BOX_1 r@ dp.register.set
      BOX_NONE PFI_REG_BOX r@ dp.register.set
    then
  then

  r> drop
);
Trk::ex_compile('_pfi1_state_drain_x','pfi 1 state drain_x',\@code);

# _pfi1_state_drain ( dp -- )
#
# if box is NONE
#   dp off
# else
#   dp on
# 
# feed_detect on
# feed_run on
#
# if elapsed > runout
#   if box is NONE
#     if formax entry pe off
#       go to state IDLE
#     else
#       jam fault
#   else
#     go to state FILL
@code=qw(
  >r

  r@ _zone_box@ BOX_NONE = not if  
    r@ on  
  else  
    r@ off  
  then

  PFI_REG_COUNT r@ dp.register.get PFI_REG_TOTAL_CURR r@ dp.register.get < not if
    r@ _zone_tick
  then

  PFI_REG_PE_SCAN r@ _pfi_reg_get@ tst not
  PFI_REG_PE_SCAN r@ dp.register.get and if
    PFI_REG_COUNT r@ dp.register.get
    1 +
    PFI_REG_COUNT r@ dp.register.set
  then

  PFI_REG_PE_SCAN r@ _pfi_reg_get@ tst PFI_REG_PE_SCAN r@ dp.register.set

  PFI_REG_PE_SCAN r@ _pfi_reg_get@ tst PFI_REG_FEED_DETECT r@ _pfi_reg_get@ dp.value.set
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ on

  r@ _zone_elapsed@ pfi_feed_runout rp.value.get > if
    r@ _zone_box@ BOX_NONE = not 
    PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = not or if
      PFI1_STATE_FILL r@ _zone_state!
    else
      PFI1_STATE_IDLE r@ _zone_state!
    then
  then
  
  r@ _zone_box@ BOX_NONE = not 
  PFI_REG_TOTAL r@ dp.register.get 1 = and
  PFI_REG_BOX_1 r@ dp.register.get BOX_NONE = and if
    r@ _zone_next_avail? if
      PFI_REG_BOX r@ dp.register.get PFI_REG_BOX_1 r@ dp.register.set
      BOX_NONE PFI_REG_BOX r@ dp.register.set
    then
  then

  r> drop
);
Trk::ex_compile('_pfi1_state_drain','pfi 1 state drain',\@code);

# _pfi1_state_fault ( dp -- )
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_FEED_DETECT r@ _pfi_reg_get@ off
  PFI_REG_FEED_RUN    r@ _pfi_reg_get@ off

  PFI_REG_RESET r@ _pfi_reg_get@ tst if
    PFI1_STATE_IDLE r@ _zone_state!
  then

  r> drop
);
Trk::ex_compile('_pfi1_state_fault','pfi 1 state fault',\@code);

# ----- m a c h i n e  1 -----
#

# _pfi1_machine ( dp -- )
#
@code=qw(
  >r

  PFI_REG_DEBUG r@ dp.register.get tst not
  if

    PFI_REG_STATE r@ dp.register.get
      dup PFI1_STATE_IDLE     = if r@ _pfi1_state_idle     then
      dup PFI1_STATE_FILL     = if r@ _pfi1_state_fill     then
      dup PFI1_STATE_FILL_X   = if r@ _pfi1_state_fill_x   then
      dup PFI1_STATE_FULL     = if r@ _pfi1_state_full     then
      dup PFI1_STATE_DRAIN_X  = if r@ _pfi1_state_drain_x  then
      dup PFI1_STATE_DRAIN    = if r@ _pfi1_state_drain    then
      dup PFI1_STATE_FAULT    = if r@ _pfi1_state_fault    then
    drop

  then

  r> drop ) ;
Trk::ex_compile("_pfi1_machine","pfi machine 1",\@code) ;

# ----- s t a t e s   2 -----
#
($PFI2_STATE_IDLE,   $PFI2_STATE_FILL,
 $PFI2_STATE_FILL_X, $PFI2_STATE_BAIL,     $PFI2_STATE_HOLD,
 $PFI2_STATE_PREP,   $PFI2_STATE_RAM_DOWN, $PFI2_STATE_RAM_UP,
 $PFI2_STATE_CRIMP,  $PFI2_STATE_FAULT 
 )
= (0,1,
   2,3,4,
   5,6,7,
   8,99) ;
Trk::const_set("PFI2_STATE_IDLE",    $PFI2_STATE_IDLE) ;
Trk::const_set("PFI2_STATE_FILL",    $PFI2_STATE_FILL) ;
Trk::const_set("PFI2_STATE_FILL_X",  $PFI2_STATE_FILL_X) ;
Trk::const_set("PFI2_STATE_BAIL",    $PFI2_STATE_BAIL) ;
Trk::const_set("PFI2_STATE_HOLD",    $PFI2_STATE_HOLD) ;
Trk::const_set("PFI2_STATE_PREP",    $PFI2_STATE_PREP) ;
Trk::const_set("PFI2_STATE_RAM_DOWN",$PFI2_STATE_RAM_DOWN) ;
Trk::const_set("PFI2_STATE_RAM_UP",  $PFI2_STATE_RAM_UP) ;
Trk::const_set("PFI2_STATE_CRIMP",   $PFI2_STATE_CRIMP) ;
Trk::const_set("PFI2_STATE_FAULT",   $PFI2_STATE_FAULT) ;

# _pfi2_state_idle ( dp -- )
#
# dp off
# feed_detect off
# feed_run on
#
# if run on and box is not none
#   go to state FILL

@code=qw(
  >r

  r@ off
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_box@ BOX_NONE = not and if
    0 PFI_REG_COUNT r@ dp.register.set
    PFI2_STATE_FILL r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_idle","pfi 2 state idle",\@code) ;

# _pfi2_state_fill ( dp -- )
#
#  if guard timeout
#    go to state FAULT
#
#  if formax exit eye on
#    go to state FILL_X    
# 
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_ERR_PE_FORMAX_NONE r@ _zone_error_msg
    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_PE_FORMAX r@ _pfi_reg_get@ tst if
    PFI2_STATE_FILL_X r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_fill","pfi2 state fill",\@code) ;

# _pfi2_state_fill_x ( dp -- )
#
#  if guard timeout
#    go to state FAULT
#
#  if not pe_scan
#    scan_trigger off
#    if not pe_formax
#      bail up
#      goto bail
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_feed_timeout rp.value.get > if
    PFI_REG_PE_FORMAX r@ _pfi_reg_get@ tst if
      PFI_ERR_PE_FORMAX_JAM r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then

    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_PE_FORMAX r@ _pfi_reg_get@ tst not
  PFI_REG_PE_FORMAX r@ dp.register.get and if
    PFI_REG_COUNT r@ dp.register.get
    1 +
    PFI_REG_COUNT r@ dp.register.set
    r@ _zone_timer_clr
  then

  PFI_REG_PE_FORMAX r@ _pfi_reg_get@ tst PFI_REG_PE_FORMAX r@ dp.register.set

  PFI_REG_PE_FORMAX r@ _pfi_reg_get@ tst not 
  PFI_REG_COUNT r@ dp.register.get PFI_REG_TOTAL r@ dp.register.get = 
  PFI_REG_COUNT r@ dp.register.get pfi_batch_pages rp.value.get % 0 = or and if
    PFI_REG_BAIL r@ _pfi_reg_get@ on
    PFI2_STATE_BAIL r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_fill_x","pfi2 state fill_x",\@code) ;

# _pfi2_state_bail ( dp -- )
#
# if guard? then goto fault
#
# if is_bail_up
#    bail off
#    goto hold
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_ERR_BAIL_STALL_UP r@ _zone_error_msg
    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_BAIL_UP r@ _pfi_reg_get@ tst if
    PFI_REG_BAIL r@ _pfi_reg_get@ off
    PFI2_STATE_HOLD r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_bail","pfi2 state bail",\@code) ;

# _pfi2_state_hold ( dp -- )
#
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_next_avail? and if
    PFI_REG_CRIMP2 r@ _pfi_reg_get@ on
    PFI2_STATE_PREP r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_hold","pfi2 state hold",\@code) ;

# _pfi2_state_prep ( dp -- )
#
#  check for carton
#
#  if guard? then goto fault
#
#  if is_crimp2_extend and not is_crimp2_retract
#    ram_up off
#    ram_down on
#    goto ram_down
#
@code=qw(
  >r
 
  r@ on
  r@ _zone_tick
 
  r@ _zone_elapsed@ pfi_guard rp.value.get > if
  
    PFI_REG_IS_CRIMP2_RETRACT r@ _pfi_reg_get@ tst if
      PFI_ERR_CRIMP2_STUCK_IN r@ _zone_error_msg
    else
      PFI_REG_IS_CRIMP2_EXTEND r@ _pfi_reg_get@ tst not if
        PFI_ERR_CRIMP2_STALL_OUT r@ _zone_error_msg
      else
        PFI_ERR_UNKNOWN r@ _zone_error_msg
      then
    then

    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_CRIMP2_EXTEND  r@ _pfi_reg_get@ tst
  PFI_REG_IS_CRIMP2_RETRACT r@ _pfi_reg_get@ tst not and if
    PFI_REG_RAM_UP   r@ _pfi_reg_get@ off
    PFI_REG_RAM_DOWN r@ _pfi_reg_get@ on 
    PFI2_STATE_RAM_DOWN r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_prep","pfi2 state prep",\@code) ;

# _pfi2_state_ram_down ( dp -- )
#
# check for carton
#
# if guard? then goto fault
#
# if not is_ram_up and is_ram_down
#   ram_down off
#   ram_up on
#   goto ram up
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if

    PFI_REG_IS_RAM_UP r@ _pfi_reg_get@ tst if
      PFI_ERR_RAM_STUCK_UP r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_DOWN r@ _pfi_reg_get@ tst not if
        PFI_ERR_RAM_STALL_DOWN r@ _zone_error_msg
      else
        PFI_ERR_UNKNOWN r@ _zone_error_msg
      then
    then

    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_UP   r@ _pfi_reg_get@ tst not
  PFI_REG_IS_RAM_DOWN r@ _pfi_reg_get@ tst     and if
    PFI_REG_RAM_UP   r@ _pfi_reg_get@ on
    PFI_REG_RAM_DOWN r@ _pfi_reg_get@ off
    PFI2_STATE_RAM_UP r@ _zone_state!
  then
  r> drop ) ;
Trk::ex_compile("_pfi2_state_ram_down","pfi2 state ram down",\@code) ;

# _pfi2_state_ram_up ( dp -- )
#
# check for carton
#
# if guard? then goto fault
#
# if is_ram_up and not is_ram_down
#   crimp1 on
#   goto crimp 
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_RAM_UP r@ _pfi_reg_get@ tst not if
      PFI_ERR_RAM_STALL_UP r@ _zone_error_msg
    else
      PFI_REG_IS_RAM_DOWN r@ _pfi_reg_get@ tst if
        PFI_ERR_RAM_STUCK_DOWN r@ _zone_error_msg
      else
        PFI_REG_PE_POCKET r@ _pfi_reg_get@ tst not if
          PFI_ERR_PE_POCKET_NONE r@ _zone_error_msg
        else
          PFI_ERR_UNKNOWN r@ _zone_error_msg
        then
      then 
    then

    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_RAM_UP   r@ _pfi_reg_get@ tst  
  PFI_REG_IS_RAM_DOWN r@ _pfi_reg_get@ tst not and
  PFI_REG_PE_POCKET   r@ _pfi_reg_get@ tst     and
  if
    PFI_REG_CRIMP1 r@ _pfi_reg_get@ on 
    PFI2_STATE_CRIMP r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_ram_up","pfi2 state ram up",\@code) ;


# _pfi2_state_crimp( dp -- )
#
# check for carton
#
# if guard? goto fault
#
# if is_crimp1_extend and not is_crimp1_retract
#      and not is_bail_up and is_bail_down
#   copy carton1 to carton2
#   set carton1 -1
#   goto idle
# 
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_CRIMP1_RETRACT r@ _pfi_reg_get@ tst if
      PFI_ERR_CRIMP1_STUCK_IN r@ _zone_error_msg
    else
      PFI_REG_IS_CRIMP1_EXTEND r@ _pfi_reg_get@ tst not if
        PFI_ERR_CRIMP1_STALL_OUT r@ _zone_error_msg
      else
        PFI_REG_IS_BAIL_UP r@ _pfi_reg_get@ tst if
          PFI_ERR_BAIL_STUCK_UP r@ _zone_error_msg
        else
          PFI_REG_IS_BAIL_DOWN r@ _pfi_reg_get@ tst not if
            PFI_ERR_BAIL_STALL_DOWN r@ _zone_error_msg
          else
            PFI_ERR_UNKNOWN r@ _zone_error_msg
          then
        then 
      then
    then

    PFI2_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_CRIMP1_EXTEND  r@ _pfi_reg_get@ tst  
  PFI_REG_IS_CRIMP1_RETRACT r@ _pfi_reg_get@ tst not and
  PFI_REG_IS_BAIL_DOWN      r@ _pfi_reg_get@ tst     and
  PFI_REG_IS_BAIL_UP        r@ _pfi_reg_get@ tst not and  
  if
    r@ _zone_box@  REG_NEXT r@ dp.register.get  _zone_box!
    PFI_REG_COUNT r@ dp.register.get PFI_REG_TOTAL r@ dp.register.get = if
      0 PFI_REG_COUNT r@ dp.register.set
      0 PFI_REG_TOTAL r@ dp.register.set
      BOX_NONE r@ _zone_box!
      PFI2_STATE_IDLE r@ _zone_state!
    else
      PFI2_STATE_FILL r@ _zone_state!
    then
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_crimp","pfi2 state crimp",\@code) ;

# _pfi2_state_fault ( dp -- )
#
# if reset
#   reset off
#   goto init
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_RESET r@ _pfi_reg_get@ tst if
    PFI2_STATE_IDLE r@ _zone_state! 
  then

  r> drop ) ;
Trk::ex_compile("_pfi2_state_fault","pfi2 state fault",\@code) ;

# ----- m a c h i n e  2 -----
#

# _pfi2_machine ( dp -- )
#
@code=qw(
  >r

  PFI_REG_DEBUG r@ dp.register.get tst not
  if

    PFI_REG_STATE r@ dp.register.get
      dup PFI2_STATE_IDLE     = if r@ _pfi2_state_idle     then
      dup PFI2_STATE_FILL_X   = if r@ _pfi2_state_fill_x   then
      dup PFI2_STATE_FILL     = if r@ _pfi2_state_fill     then
      dup PFI2_STATE_BAIL     = if r@ _pfi2_state_bail     then
      dup PFI2_STATE_HOLD     = if r@ _pfi2_state_hold     then
      dup PFI2_STATE_PREP     = if r@ _pfi2_state_prep     then
      dup PFI2_STATE_RAM_DOWN = if r@ _pfi2_state_ram_down then
      dup PFI2_STATE_RAM_UP   = if r@ _pfi2_state_ram_up   then
      dup PFI2_STATE_CRIMP    = if r@ _pfi2_state_crimp    then
      dup PFI2_STATE_FAULT    = if r@ _pfi2_state_fault    then
    drop

  then

  r> drop ) ;
Trk::ex_compile("_pfi2_machine","pfi machine 2",\@code) ;

# ----- s t a t e s   3 -----
#
($PFI3_STATE_IDLE, $PFI3_STATE_FILL, $PFI3_STATE_FULL, 
 $PFI3_STATE_DRAIN, $PFI3_STATE_FAULT )
 =
( 0, 1, 2, 
  3, 99) ;

Trk::const_set("PFI3_STATE_IDLE",   $PFI3_STATE_IDLE) ;
Trk::const_set("PFI3_STATE_FILL",   $PFI3_STATE_FILL) ;
Trk::const_set("PFI3_STATE_FULL",   $PFI3_STATE_FULL) ;
Trk::const_set("PFI3_STATE_DRAIN",  $PFI3_STATE_DRAIN) ;
Trk::const_set("PFI3_STATE_FAULT",  $PFI3_STATE_FAULT) ;

# _pfi3_state_idle ( dp -- )
#
# if carton2 != -1
#   grip on
#   ready2 off
#   goto grip
#
@code=qw(
  >r
  
  r@ off
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_box@ BOX_NONE = not and if
    PFI3_STATE_FILL r@ _zone_state!
  then
  
  r> drop ) ;
Trk::ex_compile("_pfi3_state_idle","pfi3 state idle",\@code) ;

# _pfi3_state_fill ( dp -- )
#
# dp on
# 
# if pocket pe blocked
#   go to state FULL

@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_PE_POCKET r@ _pfi_reg_get@ tst if
    PFI3_STATE_FULL r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi3_state_fill","pfi 3 state fill",\@code) ;

# _pfi3_state_full ( dp -- )
#
# dp on
#
# if pfi4 available
#   pass box to pfi4
#   go to state DRAIN_X
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_next_avail?       and if
    r@ _zone_box_pass
    PFI3_STATE_DRAIN r@ _zone_state!
  then

  r> drop
);
Trk::ex_compile('_pfi3_state_full','pfi 3 state full',\@code);

# _pfi3_state_drain ( dp -- )
#
# dp on
# 
# if pocket pe unblocked
#   go to state IDLE

@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_PE_POCKET r@ _pfi_reg_get@ tst not if
    PFI3_STATE_IDLE r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi3_state_drain","pfi 3 state drain",\@code) ;

# _pfi3_state_fault ( dp -- )
#
# if reset
#   reset off
#   goto init
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_RESET r@ _pfi_reg_get@ tst if
    PFI3_STATE_IDLE r@ _zone_state! 
  then

  r> drop ) ;
Trk::ex_compile("_pfi3_state_fault","pfi3 state fault",\@code) ;

# ----- m a c h i n e 3 -----
#

# _pfi3_machine ( dp -- )
#
@code=qw(
  >r

  PFI_REG_DEBUG r@ dp.register.get tst not
  if

    PFI_REG_STATE r@ dp.register.get
      dup PFI3_STATE_IDLE     = if r@ _pfi3_state_idle     then
      dup PFI3_STATE_FILL     = if r@ _pfi3_state_fill     then
      dup PFI3_STATE_FULL     = if r@ _pfi3_state_full     then
      dup PFI3_STATE_DRAIN    = if r@ _pfi3_state_drain    then
      dup PFI3_STATE_FAULT    = if r@ _pfi3_state_fault    then
    drop

  then

  r> drop ) ;
Trk::ex_compile("_pfi3_machine","pfi machine 3",\@code) ;

# ----- s t a t e s   4 -----
#
($PFI4_STATE_INIT,    $PFI4_STATE_RESET, 
 $PFI4_STATE_IDLE,    $PFI4_STATE_GRIP,   $PFI4_STATE_UNCRIMP,
 $PFI4_STATE_EXTEND,  $PFI4_STATE_HOLD,   $PFI4_STATE_DROP,
 $PFI4_STATE_RETRACT, $PFI4_STATE_FAULT )
 =
( 0, 1,
  2, 3, 4,
  5, 6, 7,
  8, 99 ) ;

Trk::const_set("PFI4_STATE_INIT",   $PFI4_STATE_INIT) ;
Trk::const_set("PFI4_STATE_RESET",  $PFI4_STATE_RESET) ;
Trk::const_set("PFI4_STATE_IDLE",   $PFI4_STATE_IDLE) ;
Trk::const_set("PFI4_STATE_GRIP",   $PFI4_STATE_GRIP) ;
Trk::const_set("PFI4_STATE_UNCRIMP",$PFI4_STATE_UNCRIMP) ;
Trk::const_set("PFI4_STATE_EXTEND", $PFI4_STATE_EXTEND) ;
Trk::const_set("PFI4_STATE_HOLD",   $PFI4_STATE_HOLD) ;
Trk::const_set("PFI4_STATE_DROP",   $PFI4_STATE_DROP) ;
Trk::const_set("PFI4_STATE_RETRACT",$PFI4_STATE_RETRACT) ;
Trk::const_set("PFI4_STATE_FAULT",  $PFI4_STATE_FAULT) ;

# _pfi4_state_init ( dp -- )
#
# y_grip off
# servo_hi
# goto reset
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get if

    r@ _pfi_servo_hi
    PFI_REG_Y_GRIP r@ _pfi_reg_get@ off

    PFI4_STATE_RESET r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_init","pfi4 state init",\@code) ;


# _pfi4_state_reset ( dp -- )
#
# if timeout? then goto fault
#
# if servo_hi? and not y_grip
#   goto idle
#
@code=qw(
  >r

  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    r@ _pfi_servo_hi? tst not
    if
      PFI_ERR_NOT_HI r@ _zone_error_msg

      SERVO_STATE_RESET SERVO_REG_STATE r@ _pfi_servo@ dp.register.set
    else
      PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst not
      if
        PFI_ERR_Y_GRIP_STUCK r@ _zone_error_msg
      else
        PFI_ERR_UNKNOWN r@ _zone_error_msg
      then
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  r@ _pfi_servo_hi? 
  PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst and
  if
    PFI4_STATE_IDLE r@ _zone_state!
  then 
 
  r> drop ) ;
Trk::ex_compile("_pfi4_state_reset","pfi4 state reset",\@code) ;


# _pfi4_state_idle ( dp -- )
#
# if carton2 != -1
#   grip on
#   ready2 off
#   goto grip
#
@code=qw(
  >r
  
  r@ off
  r@ _zone_tick

  PFI_REG_RUN r@ dp.register.get
  r@ _zone_box@ BOX_NONE = not and if
    PFI_REG_Y_GRIP r@ _pfi_reg_get@ on
    PFI4_STATE_GRIP r@ _zone_state!
  then
  
  r> drop ) ;
Trk::ex_compile("_pfi4_state_idle","pfi4 state idle",\@code) ;


# _pfi4_state_grip ( dp -- )
#
# if guard? then goto fault
# 
# if is_y_grip
#   crimp1 off
#   crimp2 off
#   goto uncrimp
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst
    if
      PFI_ERR_Y_GRIP_STALL r@ _zone_error_msg 
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst not
  if
    PFI_REG_CRIMP1 r@ _pfi_reg_get@ off
    PFI_REG_CRIMP2 r@ _pfi_reg_get@ off
    PFI4_STATE_UNCRIMP r@ _zone_state!
  then
  
  r> drop ) ;
Trk::ex_compile("_pfi4_state_grip","pfi4 state grip",\@code) ;

# _pfi4_state_uncrimp ( dp -- )
#
#  if guard? then goto fault
#
#  if not is_crimp1_extend and is_crimp1_retract
#  and not is_crimp2_extend and is_crimp2_retract
#    ok on
#    goto hold
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_CRIMP1_EXTEND r@ _pfi_reg_get@ tst 
    if
      PFI_ERR_CRIMP1_STUCK_OUT r@ _zone_error_msg
    else
      PFI_REG_IS_CRIMP1_RETRACT r@ _pfi_reg_get@ tst not
      if
        PFI_ERR_CRIMP1_STALL_IN r@ _zone_error_msg
      else
        PFI_REG_IS_CRIMP2_EXTEND r@ _pfi_reg_get@ tst
        if
          PFI_ERR_CRIMP2_STUCK_OUT r@ _zone_error_msg
        else
          PFI_REG_IS_CRIMP2_RETRACT r@ _pfi_reg_get@ tst
          if
            PFI_ERR_CRIMP2_STALL_IN r@ _zone_error_msg
          else
            PFI_ERR_UNKNOWN r@ _zone_error_msg 
          then
        then
      then
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_CRIMP1_EXTEND  r@ _pfi_reg_get@ tst not
  PFI_REG_IS_CRIMP1_RETRACT r@ _pfi_reg_get@ tst     and
  PFI_REG_IS_CRIMP2_EXTEND  r@ _pfi_reg_get@ tst not and
  PFI_REG_IS_CRIMP2_RETRACT r@ _pfi_reg_get@ tst     and
  if
    r@ _pfi_servo_lo
    PFI4_STATE_EXTEND r@ _zone_state!
  then
 
  r> drop ) ;
Trk::ex_compile("_pfi4_state_uncrimp","pfi4 state uncrimp",\@code) ;


# _pfi4_state_extend ( dp -- )
#
# if timeout? then goto fault
#
# if servo_lo?
#   ready2 on
#   y_grip off
#   goto drop
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    r@ _pfi_servo_lo? tst not
    if
      PFI_ERR_NOT_LO r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  r@ _pfi_servo_lo? if
    PFI4_STATE_HOLD r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_extend","pfi4 state extend",\@code) ;


# _pfi4_state_hold ( dp -- )
#
# if release
#  release off
#  servo lo
#  goto drop
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_GO r@ dp.register.get tst if
    PFI_REG_GO r@ dp.register.get off
    PFI_REG_Y_GRIP r@ _pfi_reg_get@ off
    PFI4_STATE_DROP r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_hold","pfi4 state hold",\@code) ;


# _pfi4_state_drop ( dp -- )
#
# if guard? then goto fault
#
# if not is_y_grip 
#   carton2 < -1
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_guard rp.value.get > if
    PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst not 
    if
      PFI_ERR_Y_GRIP_STUCK r@ _zone_error_msg
    else
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  PFI_REG_IS_Y_GRIP r@ _pfi_reg_get@ tst if
    r@ _pfi_servo_hi
    PFI4_STATE_RETRACT r@ _zone_state!
    BOX_NONE REG_BOX r@ dp.register.set
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_drop","pfi4 state drop",\@code) ;


# _pfi4_state_retract ( dp -- )
#
# if timeout? then goto fault
#
# if servo_hi?
#   goto idle
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  r@ _zone_elapsed@ pfi_timeout rp.value.get > if
    r@ _pfi_servo_hi? not
    if
      PFI_ERR_NOT_HI r@ _zone_error_msg

      SERVO_STATE_RESET SERVO_REG_STATE r@ _pfi_servo@ dp.register.set
    else 
      PFI_ERR_UNKNOWN r@ _zone_error_msg
    then
    PFI4_STATE_FAULT r@ _zone_state!
  then

  r@ _pfi_servo_hi? if
    PFI4_STATE_IDLE r@ _zone_state!
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_retract","pfi4 state retract",\@code) ;

# _pfi4_state_fault ( dp -- )
#
# if reset
#   reset off
#   goto init
#
@code=qw(
  >r
  
  r@ on
  r@ _zone_tick

  PFI_REG_RESET r@ _pfi_reg_get@ tst if
    PFI4_STATE_INIT r@ _zone_state! 
  then

  r> drop ) ;
Trk::ex_compile("_pfi4_state_fault","pfi4 state fault",\@code) ;

# ----- m a c h i n e 3 -----
#

# _pfi4_machine ( dp -- )
#
@code=qw(
  >r

  PFI_REG_DEBUG r@ dp.register.get tst not
  if

    PFI_REG_STATE r@ dp.register.get
      dup PFI4_STATE_INIT     = if r@ _pfi4_state_init     then
      dup PFI4_STATE_RESET    = if r@ _pfi4_state_reset    then
      dup PFI4_STATE_IDLE     = if r@ _pfi4_state_idle     then
      dup PFI4_STATE_GRIP     = if r@ _pfi4_state_grip     then
      dup PFI4_STATE_UNCRIMP  = if r@ _pfi4_state_uncrimp  then
      dup PFI4_STATE_EXTEND   = if r@ _pfi4_state_extend   then
      dup PFI4_STATE_HOLD     = if r@ _pfi4_state_hold     then
      dup PFI4_STATE_DROP     = if r@ _pfi4_state_drop     then
      dup PFI4_STATE_RETRACT  = if r@ _pfi4_state_retract  then
      dup PFI4_STATE_FAULT    = if r@ _pfi4_state_fault    then

    drop

  then

  r> drop ) ;
Trk::ex_compile("_pfi4_machine","pfi machine 4",\@code) ;

# create pfi 
sub
createPFI
  {
  my($name, $data) = @_ ;
  my $val ;

  my $dp = dp::virtual($name,$name.' pfi') ;
  my $dp1 = dp::virtual($name.'.1',$name.' pfi machine 1') ;
  my $dp2 = dp::virtual($name.'.2',$name.' pfi machine 2') ;
  my $dp3 = dp::virtual($name.'.3',$name.' pfi machine 3') ;
  my $dp4 = dp::virtual($name.'.4',$name.' pfi machine 4') ;

  Trk::dp_registerset($dp, $PFI_REG_MACHINE, $dp);
  Trk::dp_registerset($dp, $PFI_REG_MACHINE1, $dp1);
  Trk::dp_registerset($dp, $PFI_REG_MACHINE2, $dp2);
  Trk::dp_registerset($dp, $PFI_REG_MACHINE3, $dp3);
  Trk::dp_registerset($dp, $PFI_REG_MACHINE4, $dp4);

  Trk::dp_registerset($dp1, $PFI_REG_MACHINE, $dp);
  Trk::dp_registerset($dp1, $PFI_REG_MACHINE1, $dp1);
  Trk::dp_registerset($dp1, $PFI_REG_MACHINE2, $dp2);
  Trk::dp_registerset($dp1, $PFI_REG_MACHINE3, $dp3);
  Trk::dp_registerset($dp1, $PFI_REG_MACHINE4, $dp4);

  Trk::dp_registerset($dp2, $PFI_REG_MACHINE, $dp);
  Trk::dp_registerset($dp2, $PFI_REG_MACHINE1, $dp1);
  Trk::dp_registerset($dp2, $PFI_REG_MACHINE2, $dp2);
  Trk::dp_registerset($dp2, $PFI_REG_MACHINE3, $dp3);
  Trk::dp_registerset($dp2, $PFI_REG_MACHINE4, $dp4);

  Trk::dp_registerset($dp3, $PFI_REG_MACHINE, $dp);
  Trk::dp_registerset($dp3, $PFI_REG_MACHINE1, $dp1);
  Trk::dp_registerset($dp3, $PFI_REG_MACHINE2, $dp2);
  Trk::dp_registerset($dp3, $PFI_REG_MACHINE3, $dp3);
  Trk::dp_registerset($dp3, $PFI_REG_MACHINE4, $dp4);

  Trk::dp_registerset($dp4, $PFI_REG_MACHINE, $dp);
  Trk::dp_registerset($dp4, $PFI_REG_MACHINE1, $dp1);
  Trk::dp_registerset($dp4, $PFI_REG_MACHINE2, $dp2);
  Trk::dp_registerset($dp4, $PFI_REG_MACHINE3, $dp3);
  Trk::dp_registerset($dp4, $PFI_REG_MACHINE4, $dp4);

  Trk::dp_registerset($dp1, $PFI_REG_NEXT, $dp2);
  Trk::dp_registerset($dp2, $PFI_REG_NEXT, $dp3);
  Trk::dp_registerset($dp3, $PFI_REG_NEXT, $dp4);

  my $cp ;

  $cp = dp::virtual($name.'_fault',$name.' fault') ;
  Trk::dp_registerset($dp,$PFI_REG_FAULT,$cp) ;
  Trk::dp_registerset($dp1,$PFI_REG_FAULT,$cp) ;
  Trk::dp_registerset($dp2,$PFI_REG_FAULT,$cp) ;
  Trk::dp_registerset($dp3,$PFI_REG_FAULT,$cp) ;
  Trk::dp_registerset($dp4,$PFI_REG_FAULT,$cp) ;

  $cp = dp::virtual($name.'_reset',$name.' reset') ;
  Trk::dp_registerset($dp,$PFI_REG_RESET,$cp) ;
  Trk::dp_registerset($dp1,$PFI_REG_RESET,$cp) ;
  Trk::dp_registerset($dp2,$PFI_REG_RESET,$cp) ;
  Trk::dp_registerset($dp3,$PFI_REG_RESET,$cp) ;
  Trk::dp_registerset($dp4,$PFI_REG_RESET,$cp) ;

  $cp = dp::virtual($name.'_debug',$name.' debug') ;
  Trk::dp_registerset($dp,$PFI_REG_DEBUG,$cp) ;
  Trk::dp_registerset($dp1,$PFI_REG_DEBUG,$cp) ;
  Trk::dp_registerset($dp2,$PFI_REG_DEBUG,$cp) ;
  Trk::dp_registerset($dp3,$PFI_REG_DEBUG,$cp) ;
  Trk::dp_registerset($dp4,$PFI_REG_DEBUG,$cp) ;

  $cp = dp::virtual($name.'_go',$name.' go') ;
  Trk::dp_registerset($dp4,$PFI_REG_GO,$cp) ;

  Trk::dp_registerset( $dp, $PFI_REG_RUN, dp::handle( $data->{'run'} ) );

  my $rp ;

  $rp = rp::const($name.'_servo_hi',    0,$name.' servo hi') ;
  Trk::dp_registerset($dp,$PFI_REG_SERVO_HI,$rp) ;
  Trk::dp_registerset($dp4,$PFI_REG_SERVO_HI,$rp) ;

  $rp = rp::const($name.'_servo_lo',30000,$name.' servo lo') ;
  Trk::dp_registerset($dp,$PFI_REG_SERVO_LO,$rp) ;
  Trk::dp_registerset($dp4,$PFI_REG_SERVO_LO,$rp) ;

  $rp = rp::const($name.'_servo_tol',   5,$name.' servo tol') ;
  Trk::dp_registerset($dp,$PFI_REG_SERVO_TOL,$rp) ;
  Trk::dp_registerset($dp4,$PFI_REG_SERVO_TOL,$rp) ;

  Trk::dp_registerset($dp,$PFI_REG_SERVO,dp::handle($data->{'servo'})) ;
  Trk::dp_registerset($dp4,$PFI_REG_SERVO,dp::handle($data->{'servo'})) ;

  if($data->{'outputs'} ne '')
    {
    my $out = $data->{'outputs'} ;
    Trk::dp_registerset($dp,$PFI_REG_RAM_DOWN,dp::handle($out.'o1')) ;
    Trk::dp_registerset($dp,$PFI_REG_RAM_UP,dp::handle($out.'o2')) ;
    Trk::dp_registerset($dp,$PFI_REG_BAIL,dp::handle($out.'o3')) ;
    Trk::dp_registerset($dp,$PFI_REG_Y_GRIP,dp::handle($out.'o4')) ;
    Trk::dp_registerset($dp,$PFI_REG_CRIMP1,dp::handle($out.'o5')) ;
    Trk::dp_registerset($dp,$PFI_REG_CRIMP2,dp::handle($out.'o6')) ;
    Trk::dp_registerset($dp,$PFI_REG_SCAN_TRIGGER,dp::handle($out.'o7')) ;

    Trk::dp_registerset($dp,$PFI_REG_FEED_DETECT,dp::handle($out.'o9')) ;
    Trk::dp_registerset($dp,$PFI_REG_FEED_RUN,dp::handle($out.'o10')) ;

    traksort::report_ld ($out.'o1','tm_1ms');
    traksort::report_trl ($out.'o1','tm_1ms');

    traksort::report_ld ($out.'o2','tm_1ms');
    traksort::report_trl ($out.'o2','tm_1ms');

    traksort::report_ld ($out.'o3','tm_1ms');
    traksort::report_trl ($out.'o3','tm_1ms');

    traksort::report_ld ($out.'o4','tm_1ms');
    traksort::report_trl ($out.'o4','tm_1ms');

    traksort::report_ld ($out.'o5','tm_1ms');
    traksort::report_trl ($out.'o5','tm_1ms');

    traksort::report_ld ($out.'o6','tm_1ms');
    traksort::report_trl ($out.'o6','tm_1ms');

    traksort::report_ld ($out.'o7','tm_1ms');
    traksort::report_trl ($out.'o7','tm_1ms');

    traksort::report_ld ($out.'o8','tm_1ms');
    traksort::report_trl ($out.'o8','tm_1ms');
    
    traksort::report_ld ($out.'o9','tm_1ms');
    traksort::report_trl ($out.'o9','tm_1ms');

    traksort::report_ld ($out.'o10','tm_1ms');
    traksort::report_trl ($out.'o10','tm_1ms');
    } 

  if($data->{'ram_down'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_RAM_DOWN,
                            dp::handle($data->{'ram_down'})) ;
    }
  if($data->{'ram_up'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_RAM_UP,
                            dp::handle($data->{'ram_up'})) ;
    }
  if($data->{'bail'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_BAIL,
                            dp::handle($data->{'bail'})) ;
    }
  if($data->{'y_grip'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_Y_GRIP,
                            dp::handle($data->{'y_grip'})) ;
    }
  if($data->{'crimp1'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_CRIMP1,
                            dp::handle($data->{'crimp1'})) ;
    }
  if($data->{'crimp2'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_CRIMP2,
                            dp::handle($data->{'crimp2'})) ;
    }
  if($data->{'scan_trigger'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_SCAN_TRIGGER,
                            dp::handle($data->{'scan_trigger'})) ;
    }
  if($data->{'feed_detect'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_FEED_DETECT,
                            dp::handle($data->{'feed_detect'})) ;
    }
  if($data->{'feed_run'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_FEED_RUN,
                            dp::handle($data->{'feed_run'})) ;
    }


  if($data->{'inputs'} ne '')
    {
    my $in = $data->{'inputs'} ;
    Trk::dp_registerset($dp,$PFI_REG_RESET,dp::handle($in.'i1')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_RAM_UP,dp::handle($in.'i2')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_RAM_DOWN,dp::handle($in.'i3')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_BAIL_UP,dp::handle($in.'i4')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_BAIL_DOWN,dp::handle($in.'i5')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_Y_GRIP,dp::handle($in.'i6')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP1_EXTEND,dp::handle($in.'i7')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP1_RETRACT,dp::handle($in.'i8')) ;

    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP2_EXTEND,dp::handle($in.'i9')) ;
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP2_RETRACT,dp::handle($in.'i10')) ;
    Trk::dp_registerset($dp,$PFI_REG_PE_FORMAX,dp::handle($in.'i11')) ;
    Trk::dp_registerset($dp,$PFI_REG_PE_POCKET,dp::handle($in.'i12')) ;
    Trk::dp_registerset($dp,$PFI_REG_PE_SCAN,dp::handle($in.'i15')) ;

    traksort::report_ld ($in.'i1','tm_1ms');
    traksort::report_trl ($in.'i1','tm_1ms');

    traksort::report_ld ($in.'i2','tm_1ms');
    traksort::report_trl ($in.'i2','tm_1ms');

    traksort::report_ld ($in.'i3','tm_1ms');
    traksort::report_trl ($in.'i3','tm_1ms');

    traksort::report_ld ($in.'i4','tm_1ms');
    traksort::report_trl ($in.'i4','tm_1ms');

    traksort::report_ld ($in.'i5','tm_1ms');
    traksort::report_trl ($in.'i5','tm_1ms');

    traksort::report_ld ($in.'i6','tm_1ms');
    traksort::report_trl ($in.'i6','tm_1ms');

    traksort::report_ld ($in.'i7','tm_1ms');
    traksort::report_trl ($in.'i7','tm_1ms');

    traksort::report_ld ($in.'i8','tm_1ms');
    traksort::report_trl ($in.'i8','tm_1ms');

    traksort::report_ld ($in.'i9','tm_1ms');
    traksort::report_trl ($in.'i9','tm_1ms');

    traksort::report_ld ($in.'i10','tm_1ms');
    traksort::report_trl ($in.'i10','tm_1ms');

    traksort::report_ld ($in.'i11','tm_1ms');
    traksort::report_trl ($in.'i11','tm_1ms');

    traksort::report_ld ($in.'i12','tm_1ms');
    traksort::report_trl ($in.'i12','tm_1ms');

    traksort::report_ld ($in.'i15','tm_1ms');
    traksort::report_trl ($in.'i15','tm_1ms');
    } 

  if($data->{'reset'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_RESET,
                            dp::handle($data->{'reset'})) ;
    }
  if($data->{'is_ram_up'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_RAM_UP,
                            dp::handle($data->{'is_ram_up'})) ;
    }
  if($data->{'is_ram_down'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_RAM_DOWN,
                            dp::handle($data->{'is_ram_down'})) ;
    }
  if($data->{'is_bail_up'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_BAIL_UP,
                            dp::handle($data->{'is_bail_up'})) ;
    }
  if($data->{'is_bail_down'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_BAIL_DOWN,
                            dp::handle($data->{'is_bail_down'})) ;
    }
  if($data->{'is_y_grip'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_Y_GRIP,
                            dp::handle($data->{'is_y_grip'})) ;
    }
  if($data->{'is_crimp1_extend'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP1_EXTEND,
                            dp::handle($data->{'is_crimp1_extend'})) ;
    }
  if($data->{'is_crimp1_retract'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP1_RETRACT,
                            dp::handle($data->{'is_crimp1_retract'})) ;
    }
  if($data->{'is_crimp2_extend'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP2_EXTEND,
                            dp::handle($data->{'is_crimp2_extend'})) ;
    }
  if($data->{'is_crimp2_retract'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_IS_CRIMP2_RETRACT,
                            dp::handle($data->{'is_crimp2_retract'})) ;
    }
  if($data->{'pe_formax'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_PE_FORMAX,
                            dp::handle($data->{'pe_formax'})) ;
    }
  if($data->{'pe_pocket'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_PE_POCKET,
                            dp::handle($data->{'pe_pocket'})) ;
    }
  if($data->{'pe_scan'} ne '')
    {
    Trk::dp_registerset($dp,$PFI_REG_PE_SCAN,
                            dp::handle($data->{'pe_scan'})) ;
    }


  my $dp_ext_purge = dp::virtual($name.'.ext_purge', $name.' external purge');
  my $dp_purging = dp::virtual($name.'.purging', $name.' purging signal');
  my $dp_ready = dp::virtual($name.'.ready', $name.' ready signal');

  Trk::dp_registerset($dp, $PFI_REG_EXT_PURGE, $dp_ext_purge);
  Trk::dp_registerset($dp, $PFI_REG_PURGING, $dp_purging);
  Trk::dp_registerset($dp, $PFI_REG_READY, $dp_ready);


  my @stub = ( $name, '_pfi_machine',
              $name . '.1', '_pfi1_machine',
              $name . '.2', '_pfi2_machine',
              $name . '.3', '_pfi3_machine',
              $name . '.4', '_pfi4_machine', );
  Trk::ex_compile($name."_machine",$name." machine",\@stub);
  trakbase::leading("tm_10ms",$name."_machine",$name." machine") ;
  trakbase::trailing("tm_10ms",$name."_machine",$name." machine") ;
  my $dummy = pop(@stub) ;
  
  } ;

1;