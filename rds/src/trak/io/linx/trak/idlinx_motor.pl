#!/usr/bin/perl
#
#

# name is motor name (XXX). Run by turning on/off dp XXX
#                        check status with XXX_run?
#                        check/clear fault with XXX_fault
#                        disable operations with XXX_debug 
#                        $data hash elements include
#      stem is the moxalinx object stem
#      which takes value 'a' or 'b'
#
#      reset  => reset dp, leading edge resets motor if faulted
#      speed => default speed 
#      dir => default direction
#      run => default speed/direction loaded on leading edge of this.


require trakbase ;
require linx ;
require "zonetrak_common.pl" ;
require dp ;

my @code ;

Trk::include('idlinx.h','LINX_') ;


use constant {
    LINX_REG_MOTOR  =>   4,
    LINX_REG_RST    =>   5,
    LINX_REG_FLT    =>   6,
    LINX_REG_AUX    =>   7,
    LINX_REG_FAULT  =>   10,
    LINX_REG_RUN    =>   11,
    LINX_REG_DEBUG  =>   12,
    LINX_REG_DIR    =>   13,
    LINX_REG_DIRDP  =>   14,
    LINX_REG_RESET  =>   15,
    LINX_REG_SPEED  =>   16,
} ;

use constant {
  MODE_CCW  => 0,
  MODE_CW  => 1,
} ;

@code=qw( 3 swap dp.register.set ) ;
Trk::ex_compile('set.motor.speed','set motor speed',\@code) ;

@code=qw(
  >r
  on
  tm_1ms &delay_clr 250 r> 0 0 0 ev_insert ) ;
Trk::ex_compile('id_pulse','id motor pulse',\@code) ;

@code=qw(
  1 edge.dp dp.register.get >r
    LINX_REG_FLT r@ _zone_dp? if
      LINX_REG_RST r@ _zone_dp? id_pulse
    then
  r> drop
) ;
Trk::ex_compile('idlinx_fault_reset','on fault clear',\@code) ;
@code=qw(
  1 edge.dp dp.register.get >r
    LINX_REG_FLT r@ _zone_dp? if
      LINX_REG_RST r@ id_pulse
    then
  r> drop
) ;
Trk::ex_compile('idlinx_sys_reset','system reset for idlinx motor',\@code) ;

@code=qw(
   >r
    LINX_REG_SPEED r@ dp.register.get 
        LINX_REG_MOTOR r@  dp.register.get
           set.motor.speed
    LINX_REG_DIR r@ dp.register.get if
      LINX_REG_DIRDP r@ _zone_dp_on
    else
      LINX_REG_DIRDP r@ _zone_dp_off
    then
  r> drop
) ;
Trk::ex_compile('do_idlinx_setup','set defaults for motor fault',\@code) ;

@code=qw(
  1 edge.dp dp.register.get >r
    LINX_REG_FAULT r@ _zone_dp_on
    r@ off
  r> drop
) ;
Trk::ex_compile('idlinx_faulted','on idlinx motor fault',\@code) ;

@code=qw(
  >r
    LINX_REG_FAULT r@ dp.register.get tst if
      LINX_REG_FAULT r@ dp.register.get off
    then
  r> drop 
) ;
Trk::ex_compile('do_idlinx_reset','reset a motor',\@code) ;

# set speed, speeds = 1, 2, 3, or 4 ... not equal 1-4 treated as speed 1
# we store 
# so stack diagram is 
# speed motor_dp set.motor.speed
@code=qw(
  3 swap dp.register.set 
) ;
Trk::ex_compile('set.motor.speed','motor speed set',\@code) ;

sub idlinx_motor {
  my ($name,$stem,$which,$data) = @_ ;

  
  my $desc = $data->{'desc'} ;
  if ($desc eq '') {
     $desc = 'motor defined from '. $stem . '_mtr_'.$which ;
  }
  my $dp_h = dp::virtual($name,$desc) ;

  dp::slave($stem.'_mtr_'.$which,$name) ;
  
  my $fault_h = dp::virtual($name.'_fault','fault for '.$desc) ;
  Trk::dp_registerset($dp_h,LINX_REG_FAULT,$fault_h) ;
  Trk::dp_registerset($dp_h,LINX_REG_DIRDP,
            dp::handle($stem.'_mtr_'.$which.'_dir')) ;
  Trk::dp_registerset($dp_h,LINX_REG_MOTOR,
            dp::handle($stem.'_mtr_'.$which)) ;
  Trk::dp_registerset($dp_h,LINX_REG_AUX,
            dp::handle($stem.'_mtr_'.$which.'_aux')) ;
  Trk::dp_registerset($dp_h,LINX_REG_FLT,
            dp::handle($stem.'_mtr_'.$which.'_flt')) ;

  Trk::dp_registerset($fault_h,1,$dp_h) ;

  if ($data->{'speed'} eq '') {
    Trk::dp_registerset($dp_h,LINX_REG_SPEED,2) ;
  } else {
    Trk::dp_registerset($dp_h,LINX_REG_SPEED,$data->{'speed'}) ;
  }


  Trk::dp_registerset(dp::handle($stem.'_mtr_'.$which._flt),1,$dp_h) ;
 
  #my $run_h = dp::virtual($name.'_run?','run for '.$desc) ;
  #dp::slave($name.'_run?',$stem.'_mtr_'.$which.'_aux') ;

  my $debug_h = dp::virtual($name.'_debug','fault for '.$desc) ;
  Trk::dp_registerset($dp_h,LINX_REG_DEBUG,$debug_h) ;


  if ($data->{'dir'} ne '') {
     if ($data->{'dir'} eq 'ccw') {
       Trk::dp_registerset($dp_h,LINX_REG_DIR,MODE_CCW) ;
     } else {
       Trk::dp_registerset($dp_h,LINX_REG_DIR,MODE_CW) ;
     }
  } 

  my $reset = 'system_reset' ;
  if ($data->{'reset'} ne '') {
     $reset = $data->{'reset'} ;    
  } 
  Trk::dp_registerset($dp_h,LINX_REG_RESET,dp::handle($reset)) ;
  my @code=($name,'do_idlinx_reset') ;
  Trk::ex_compile($name.'.id_reset','reset ' .$name,\@code) ;
  my $foo = pop(@code) ;
  trakbase::leading($reset,$name.'.id_reset','') ;

  my $run = 'system_run' ;
  if ($data->{'run'} ne '') {
    $run = $data->{'run'} ;
  } 
  my @code=($name,'do_idlinx_setup') ;
  Trk::ex_compile($name.'.id_setup','setup ' .$name,\@code) ;
  my $foo = pop(@code) ;
  trakbase::leading($run,$name.'.id_setup','') ;

  trakbase::trailing($name.'_fault','idlinx_fault_reset','') ;

  trakbase::leading($stem.'_mtr_'.$which.'_flt','idlinx_faulted','') ;

  
} 
return 1;
