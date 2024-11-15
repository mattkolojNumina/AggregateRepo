#!/bin/perl -I.
#

require Trk;
require trakbase;
require traksort;
require dp;
require dead;
require debounce;
require timer;

my @code;

trakbase::message( 'utility functions' );

dp::virtual('false','always off');

# pulse
# usage:  <dp> pulse
rp::const( 'pulse_duration', 250, 'duration of pulsed outputs (ms)' );
@code=qw( 
   >r
   r@ on
   tm_1ms &delay_clr  pulse_duration rp.value.get
      r@ 0 0 0 ev_insert
   r> drop
);
Trk::ex_compile( 'pulse', 'pulse a dp', \@code );

# pulse_off
# usage:  <dp> pulse_off
rp::const( 'pulse_off_duration', 250, 'duration of pulsed off outputs (ms)' );
@code=qw( 
   >r
   r@ off
   tm_1ms &delay_clr  pulse_off_duration rp.value.get
      r@ 0 0 0 ev_insert
   r> drop
);
Trk::ex_compile( 'pulse_off', 'pulse a dp off', \@code );

# pulse event
#  a - dp to pulse
@code=qw(
   evt.a pulse
);
Trk::ex_compile( 'pulse_evt', 'pulse a dp', \@code );

# toggle
# usage:  <dp> toggle
@code=qw( dup tst not swap dp.value.set );
Trk::ex_compile( 'toggle', 'toggle a dp', \@code );

rp::const( 'fault_on', 250, 'debounce time for fault (msec)' );
rp::const( 'fault_off', 250, 'debounce time for fault clear (msec)' );

rp::const( 'conditioning', 100, 'dead/debounce time for control I/O (msec)' );
rp::const( 'conditioning_long', 1000, 'longer dead/debounce time (msec)' );

rp::const( 'full_on', 3000, 'debounce time for line full (msec)' );
rp::const( 'full_off', 100, 'debounce time for line full clear (msec)' );

# utility to rename and condition inputs
sub in 
   {
   my ($io,$dp,$desc,$extra) = @_;

   my $ppi = $extra->{'ppi'};
   if ($ppi eq '') { $ppi = 'tm_1ms'; }

   my $input = $io;
   if($extra->{'dir'} eq 'rev')
      {
      dp::revlink($dp.'^',$io,$desc);
      $input = $dp.'^';
      }

   my $h;
   if(($extra->{'edge'} eq '') or ($extra->{'edge'} eq 'dead'))
      {
      $h = dead::link($dp,$input,'conditioning','conditioning',$ppi,$desc);
      }
   elsif($extra->{'edge'} eq 'dead_long')
      {
      $h = dead::link($dp,$input,'conditioning_long','conditioning_long',$ppi,$desc);
      }
   elsif($extra->{'edge'} eq 'fault')
      {
      $h = debounce::link($dp,$input,'fault_off','fault_on',$ppi,$desc);
      }
   elsif($extra->{'edge'} eq 'full')
      {
      $h = debounce::link($dp,$input,'full_off','full_on',$ppi,$desc);
      }
   else  # raw
      {
      $h = dp::link($dp,$input,$desc);
      }

   if($extra->{'report'} eq 'lead')
      {
      traksort::report_ld ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'trail')
      {
      traksort::report_trl ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'both')
      {
      traksort::report_ld ($dp,$ppi);
      traksort::report_trl($dp,$ppi);
      }

   return $h;
   }
      
# utility to rename outputs
sub out 
   {
   my ($io,$dp,$desc,$extra) = @_;
   
   my $ppi = $extra->{'ppi'};
   if ($ppi eq '') { $ppi = 'tm_1ms'; }

   my $h = dp::linkout($dp,$io,$desc);

   if($extra->{'report'} eq 'lead')
      {
      traksort::report_ld ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'trail')
      {
      traksort::report_trl ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'both')
      {
      traksort::report_ld ($dp,$ppi);
      traksort::report_trl($dp,$ppi);
      }

   if($extra->{'oneshot'} ne '')
      {
      my @stub = ($dp,"pulse");
      Trk::ex_compile($dp."_oneshot",$dp." oneshot",\@stub);
      trakbase::leading($dp,$dp."_oneshot",$dp." oneshot");
      my $dummy = pop(@stub);
      }

   return $h;
   }

# generate a system control
sub control     
   {
   my ($dp,$desc,$extra) = @_;

   my $ppi = $extra->{'ppi'};
   if ($ppi eq '') { $ppi = 'tm_1ms'; }

   my $h = dp::virtual($dp,$desc);

   if($extra->{'report'} eq 'lead')
      {
      traksort::report_ld ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'trail')
      {
      traksort::report_trl ($dp,$ppi);
      }
   elsif($extra->{'report'} eq 'both')
      {
      traksort::report_ld ($dp,$ppi);
      traksort::report_trl($dp,$ppi);
      }

   if($extra->{'oneshot'} ne '')
      {
      my @stub = ($dp,"pulse");
      Trk::ex_compile($dp."_oneshot",$dp." oneshot",\@stub);
      trakbase::leading($dp,$dp."_oneshot",$dp." oneshot");
      my $dummy = pop(@stub);
      }

   return $h;
   }

# create a virtual attach point
# attach(source,target,desc,ppi,offset,window,edge)
#      create target as a dp
#      create offset and window rps
#      based on edge, schedule id load and shadow on source edge
#      schedule a report on lead edge
sub attach 
   {
   my ($source,$target,$desc,$ppi,$offset,$window,$edge) = @_ ;

   dp::virtual($target,$desc) ;

   rp::const($target."_offset",$offset,$target." offset") ;
   rp::const($target."_window",$window,$target." window") ;

   if ($edge eq 'trailing') 
      {
      traksort::shadow_bx_trl( $source,$target,$ppi,
                               $target."_offset",$target."_window") ;
      traksort::send_bx_id_trl($source,$target,$ppi,
                               $target."_offset",$target."_window") ;
      }
   else 
      {
      traksort::shadow_bx_ld(  $source,$target,$ppi,
                               $target."_offset",$target."_window") ;
      traksort::send_bx_id_ld( $source,$target,$ppi,
                               $target."_offset",$target."_window") ;
      }

   traksort::report_ld($target,$ppi) ;
   }

# handoff
#    create offset and window rps
#    schedule id load and shadow on source edge
#    schedule a report on lead
sub handoff
   {
   my ($source,$target,$ppi,$offset,$window) = @_ ;

   rp::const($target."_offset",$offset,$source." to ".$target." offset") ;
   rp::const($target."_window",$window,$source." to ".$target." window") ;

   traksort::send_bx_id_ld($source,$target,$ppi,
                           $target."_offset",$target."_window") ;
   traksort::report_ld($target,$ppi) ;
   }


# handoff_trl
#    create offset and window rps
#    schedule id load and shadow on source edge
#    schedule a report on trail
sub handoff_trl
   {
   my ($source,$target,$ppi,$offset,$window) = @_ ;

   rp::const($target."_offset",$offset,$source." to ".$target." offset") ;
   rp::const($target."_window",$window,$source." to ".$target." window") ;

   traksort::send_bx_id_trl($source,$target,$ppi,
                           $target."_offset",$target."_window") ;
   traksort::report_trl($target,$ppi) ;
   }


# latch
#    
sub latch
   {
   my ($source,$target) = @_ ;

   my @stub = ($target,"on");
   Trk::ex_compile($target."_latch",$target." latch",\@stub);
   trakbase::leading($source,$target."_latch",$target." latch");
   my $dummy = pop(@stub);
   }

sub latchoff
   {
   my ($source,$target) = @_ ;

   my @stub = ($target,"off");
   Trk::ex_compile($target."_latch_off",$target." latch off",\@stub);
   trakbase::leading($source,$target."_latch_off",$target." latch off");
   my $dummy = pop(@stub);
   }


# ----- jam -----
#
#   sends dp MB_FAULT_LEAD  (1301)
#         dp MB_FAULT_TRAIL (1302)
#

# jam_trail [edge] 
#
@code = qw(
          3 edge.dp dp.register.get if
             0 3 edge.dp dp.register.set
             edge.dp 1302 2 mb_npost
             4 edge.dp dp.register.get off
          then  
          );
Trk::ex_compile('jam_trail','jam trail',\@code) ;
                   
# jam_test [event]
#    evt.a - dp for jam
#    evt.b - count at last edge
#
@code = qw( 
          evt.a dp.counter.get  evt.b = if 
             1 3 evt.a dp.register.set
             evt.a 1301 2 mb_npost
             4 evt.a dp.register.get on
          then 
          ) ;
Trk::ex_compile('jam_test','jam test',\@code) ;

# jam_lead [edge]
#    reg 1 - ppi
#    reg 2 - jamtime rp
#    reg 3 - jammed 
#    reg 4 - jam dp
#
@code = qw( 
          1 edge.dp dp.register.get
          &jam_test
          2 edge.dp dp.register.get rp.value.get
          edge.dp  edge.dp dp.counter.get 0 0 
          ev_insert
          ) ;
Trk::ex_compile('jam_lead','jam lead',\@code) ;

#  jam(dp,ppi,count) [macro] - configure a jam timer
#
sub jam
   {
   my ($dp, $ppi, $count) = @_ ;

   dp::link($dp.'_j',$dp,'jam work') ;
   my $jd = dp::handle($dp.'_j')  ;

   dp::virtual($dp.'_jam',$dp,'jam fault') ;
   traksort::report_ld($dp.'_jam',$ppi);

   my $jr = rp::const($dp."_jamtime",$count,$dp." jam time (".$ppi.")") ;

   Trk::dp_registerset($jd,1,dp::handle($ppi)) ;
   Trk::dp_registerset($jd,2,$jr) ;
   Trk::dp_registerset($jd,4,dp::handle($dp.'_jam')) ;
  
   trakbase::leading( $dp.'_j','jam_lead', 'jam lead, schedule') ;       
   trakbase::trailing($dp.'_j','jam_trail','jam trail, clear') ;
   }

# jammed? ( jam_work -- flag )
#
@code=qw(
  3 swap dp.register.get ) ;
Trk::ex_compile('jammed?','test jam work dp',\@code) ;

