#!/bin/perl -I.
#

require Trk;
require trakbase;
require traksort;
require dp;
require timer;

my @code;

trakbase::message( 'device utility functions' );


# ===========================================================================

rp::const( 'aux_delay', 5000, 'aux test delay (msec)' );


# --- device registers ---
#  1 - run output dp
#  2 - device aux input dp
#  3 - device reset dp
#  7 - status (0=stopped, 1=starting, 2=running)
#  8 - fault dp
#  9 - run start/test time (tm_1ms)

@code=qw( 1 swap dp.register.get );
Trk::ex_compile( 'device_run@', 'fetch device run dp', \@code );
@code=qw( 2 swap dp.register.get );
Trk::ex_compile( 'device_aux@', 'fetch device aux dp', \@code );
@code=qw( 7 swap dp.register.get 0 = );
Trk::ex_compile( 'device_stopped?', 'device stopped?', \@code );
@code=qw( 7 swap dp.register.get 2 = );
Trk::ex_compile( 'device_running?', 'device running?', \@code );
@code=qw( 8 swap dp.register.get );
Trk::ex_compile( 'device_fault@', 'fetch device fault dp', \@code );
@code=qw( 9 swap dp.register.get  tm_1ms dp.counter.get  swap - );
Trk::ex_compile('device_elapsed@','fetch device elapsed time (msec)', \@code);
@code=qw( 10 swap dp.register.get );
Trk::ex_compile( 'device_last_start@', 'device last start time', \@code );


# device_run  ( dp -- )
#
@code=qw(
   >r

   r@ tst not  
   r@ device_fault@ tst not and if
      r@ on
      r@ device_run@ on
      1 7 r@ dp.register.set
      tm_1ms dp.counter.get 9 r@ dp.register.set
      tm_1ms dp.counter.get 10 r@ dp.register.set
   then

   r> drop
);
Trk::ex_compile('device_run','run device',\@code);

@code=qw(
  evt.a device_run
) ;
Trk::ex_compile('device_run_evt','run device',\@code);

# device_stop  ( dp -- )
#
@code=qw(
   >r

   r@ off
   r@ device_run@ off
   r@ device_stopped? not if
     tm_1ms dp.counter.get  9 r@ dp.register.set
   then
   0 7 r@ dp.register.set
   r> drop
);
Trk::ex_compile('device_stop','stop device',\@code);

# device_clear  ( dp -- )
#
@code=qw(
   >r

   r@ device_fault@ tst if
      r@ device_stop
      r@ device_fault@ off
   then

   r> drop
);
Trk::ex_compile('device_clear','clear device fault',\@code);

# device_test  ( dp -- )
#
@code=qw(
   >r

   r@ tst  r@ device_run@ tst  and if
      r@ device_aux@ tst if
         7 r@ dp.register.get  1 = if
            r@ device_elapsed@  aux_delay rp.value.get  >  if
               2 7 r@ dp.register.set
            then
         else
            tm_1ms dp.counter.get  9 r@ dp.register.set
         then
      else
         r@ device_elapsed@  aux_delay rp.value.get  >  if
            r@ device_stop
            r@ device_fault@ on
         then
      then
   else
      r@ device_stop
   then

   r> drop
);
Trk::ex_compile('device_test','test device status',\@code);


sub device_def {
   my ($name, $run_dp, $aux_dp, $desc, $data) = @_;

   my $dp = dp::virtual($name,$desc);
   Trk::dp_registerset($dp,1,dp::handle($run_dp));
   Trk::dp_registerset($dp,2,dp::handle($aux_dp));

   my $reset_dp = $data->{reset};
   if ($reset_dp eq '') {
      $reset_dp = $name . '_reset';
      dp::virtual($reset_dp,$desc.' reset');
      traksort::report_ld($reset_dp,'tm_1ms');
   }
   Trk::dp_registerset($dp,3,dp::handle($reset_dp));

   my $fault_dp = $data->{fault};
   if ($fault_dp eq '') {
      $fault_dp = $name . '_fault';
      dp::virtual($fault_dp,$desc.' fault');
      traksort::report_ld($fault_dp,'tm_1ms');
   }
   Trk::dp_registerset($dp,8,dp::handle($fault_dp));

   my @stub = ($name,'device_test');
   Trk::ex_compile($name.'_test',$name.' test',\@stub);
   trakbase::leading( 'tm_100ms',$name.'_test',$desc.' test');
   trakbase::trailing('tm_100ms',$name.'_test',$desc.' test');

   @stub = ($name,'device_clear');
   Trk::ex_compile($name.'_clr',$name.' clr',\@stub);
   trakbase::leading($reset_dp,$name.'_clr',$desc.' fault clear');

   my $tmp = pop(@stub);
}
