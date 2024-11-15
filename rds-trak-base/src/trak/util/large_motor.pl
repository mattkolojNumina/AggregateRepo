#!/bin/perl -I.
#

require Trk;
require trakbase;
require traksort;
require dp;
require timer;

my @code;

trakbase::message( 'large motor functions' );


# ===========================================================================

rp::const('cooldown', '10000', 'motor must wait this long after starting before starting again');

#basically this is going to create a device (see device.pl for state behavior)
#but since a lot of them all depend on special signals (gate limit switches, status signals, etc.)
#it made sense to create its own pl file to conserve space in the transport.pl file

#device reigters reprinted here for ease of access

# --- device registers ---
#  1 - run output dp
#  2 - device aux input dp
#  3 - device reset dp
#  4 - hold control dp (UNIQUE TO LARGE MOTOR)
#  5 - status input dp (UNIQUE TO LARGE MOTOR)
#  6 - panel dp       (UNIQUE TO LARGE MOTOR)
#  7 - status (0=stopped, 1=starting, 2=running)
#  8 - fault dp
#  9 - run start/test time (tm_1ms)
# 10 - last start time (tm_1ms)

@code=qw( 4 swap dp.register.get );
Trk::ex_compile( 'device_hold@', 'device hold?', \@code );
@code=qw( 5 swap dp.register.get 1 = );
Trk::ex_compile( 'device_estop_on?', 'device estop on?', \@code );
@code=qw( 6 swap dp.register.get );
Trk::ex_compile( 'device_panel@', 'device hold?', \@code );
@code=qw( 6 swap dp.register.get CONTROL_REG_FAULT swap dp.register.get tst );
Trk::ex_compile( 'device_panel_faulted?', 'device panel faulted?', \@code );
@code=qw( 6 swap dp.register.get CONTROL_REG_RUN  swap dp.register.get tst );
Trk::ex_compile( 'device_panel_running?', 'device panel running?', \@code );
@code=qw(
   device_last_start@
   tm_1ms dp.counter.get swap -
);
Trk::ex_compile('last_start_elapsed@', 'time since device was last started up', \@code); 
@code=qw( 
   last_start_elapsed@
   cooldown rp.value.get > 
);
Trk::ex_compile('device_cooled_down?', 'device cooldown complete', \@code);

@code=qw(
   evt.a device_hold@          tst not
   evt.a device_running?    not    and
   evt.a device_panel_faulted?     not and
   evt.a device_panel_running?         and if
      evt.a device_run
   then
);
Trk::ex_compile('lm_run_evt','turn large motor on and off',\@code);

# if hold off, device stopped, and panel isn't faulted,
#   run the device
# else if the hold is active, device is faulted, or panel is faulted,
# as well as the device isn't already stopped
#   stop the device
#
# NOTE: device not faulted is part of device_run


@code=qw(

   >r

   11 r@ dp.register.get 1 + 11 r@ dp.register.set

	r@ device_hold@             tst not
	r@ device_stopped?                  and 
	r@ device_panel_faulted?        not and 
   r@ device_panel_running?            and if	

      12 r@ dp.register.get 1 + 12 r@ dp.register.set
      r@ device_cooled_down? if
   	   r@ device_run
      else 
         tm_1ms &lm_run_evt cooldown rp.value.get r@ last_start_elapsed@ -
         r@
         0
         0
         0
         ev_insert
      then
	else
      r@ device_hold@          tst 
	   r@ device_fault@         tst or 
	   r@ device_panel_faulted?     or
      r@ device_panel_running? not or
	   r@ device_stopped?       not and if
	     r@ device_stop
	   then
	then	
   
   r> drop

);
Trk::ex_compile('large_motor_switch','turn large motor on and off',\@code);

@code=qw(
   
   >r 

	r@ device_estop_on? if	
	   r@ device_fault@ on
	then	

   r> drop

);
Trk::ex_compile('large_motor_status_fault','fault large_motor when status=0',\@code);


sub large_motor_def {
   my ($name, $run_dp, $aux_dp, $desc, $data) = @_;
   device_def($name, $run_dp, $aux_dp, $desc, $data);

   my $dp = dp::handle($name);
   
   my $hold_dp = $data->{hold};
   if ($hold_dp eq '') {
      $hold_dp = 'false';
   }
   traksort::report_ld($hold_dp,'tm_1ms');   
   traksort::report_trl($hold_dp,'tm_1ms');      
   Trk::dp_registerset($dp,4,dp::handle($hold_dp));
#   trakbase::submessage("DEBUG: ".$name." register 4 stores ".$hold_dp."/".dp::handle($hold_dp));
   my $estop_dp = $data->{estop};
   if ($estop_dp eq '') {
      $estop_dp='false';
   }
   traksort::report_ld($estop_dp,'tm_1ms');   
   traksort::report_trl($estop_dp,'tm_1ms');      
   Trk::dp_registerset($dp,5,dp::handle($estop_dp));   
#   trakbase::submessage("DEBUG: ".$name." register 5 stores ".$estop_dp."/".dp::handle($estop_dp));
  
   my $panel_dp = $data->{panel};
   if ($panel_dp eq '') {
      $panel_dp=$area;
   }
   else {
      $panel_dp = $panel_dp;
   }
   Trk::dp_registerset($dp,6,dp::handle($panel_dp));   
#   trakbase::submessage("DEBUG: ".$name." register 6 stores ".$panel_dp."/".dp::handle($panel_dp));

   #just get whatever value device_def would've created for fault and reset.
   # we aren't actually creating any DPs here
   my $reset_dp = $data->{reset};
   if ($reset_dp eq '') {
      $reset_dp = $name . '_reset';
   }   
   my $fault_dp = $data->{fault};
   if ($fault_dp eq '') {
      $fault_dp = $name . '_fault';
   }

   
   my @stub = ($name,'large_motor_switch');
   Trk::ex_compile($name.'_switch',$name.' switch',\@stub);
   
   trakbase::leading( $hold_dp,$name.'_switch');
   trakbase::trailing( $hold_dp,$name.'_switch');
   trakbase::leading( $fault_dp,$name.'_switch');
   trakbase::leading( $reset_dp,$name.'_switch');
   trakbase::leading( $panel_dp.'_run',$name.'_switch');
   trakbase::trailing( $panel_dp.'_run',$name.'_switch');

   @stub = ($name,'large_motor_status_fault'); 
   Trk::ex_compile($name.'_status_fault',$name.' status fault',\@stub);   
   trakbase::leading( 'tm_100ms',$name.'_status_fault');
   trakbase::trailing( 'tm_100ms',$name.'_status_fault');
}
