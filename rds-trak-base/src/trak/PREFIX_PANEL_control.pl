#!/bin/perl -I.

# CLIENT AREA Trak Control

require Trk;
require trakbase;
require dp;
require rp;
require timer;

require "control.pl";

trakbase::message( 'CLIENT AREA Controls' );

# AREA_fault_on
#   turn fault on
my @code=qw( AREA_fault on ) ;
Trk::ex_compile('AREA_fault_on','AREA fault on',\@code) ;

# AREA_fault_on_lead(device)
#   fault on leading edge
sub
AREA_fault_on_lead
  {
  my ($device) = @_ ;
  trakbase::leading($device,'AREA_fault_on','AREA fault on') ;
  }

# AREA_fault_on_trail(device)
#   fault on trailing edge
sub
AREA_fault_on_trail
  {
  my ($device) = @_ ;
  trakbase::trailing($device,'AREA_fault_on','AREA fault on') ;
  }

# AREA_delay_off_fault [evt]
#   a - dp to check
@code=qw( evt.a tst not if AREA_fault on then ) ;
Trk::ex_compile('AREA_delay_off_fault','AREA delay off fault',\@code) ;

# AREA_delay_off_check [edge]
#   
@code=qw( tm_500ms &AREA_delay_off_fault 2 edge.dp 0 0 0 ev_insert );
Trk::ex_compile('AREA_delay_off_check','AREA delay off check',\@code) ;

# AREA_fault_on_trail_delay(device)
#   fault if still off in 1 second
sub
AREA_fault_on_trail_delay
  {
  my ($device) = @_ ;
  trakbase::trailing($device,'AREA_delay_off_check',
                     'AREA fault on trail delay') ;
  } 

# one shots (scanners) [default 100ms]
# addOneShot('scn1000a-1',{output=>'scnint1000a-1',duration=>100}) ;

# communications faults - ibe cards
# AREA_fault_on_lead('ib1000a-1_fault') ;

# 480vac zone faults
# AREA_fault_on_lead('z1100_fault') ;

# AREA_comm_ok?
#   returns 1 if no comm issues
@code=qw(
  1
  ib1000a-1_comm tst and
) ;
Trk::ex_compile('AREA_comm_ok?','AREA comm ok?',\@code) ;


# 480vac zone resets
#   large motor resets on panel reset
#  z1000_reset on 
@code=qw(
  1
  ) ;
Trk::ex_compile('AREA_zone_reset','AREA zone reset',\@code) ;
trakbase::leading('AREA_reset','AREA_zone_reset',\@code) ;

# sub-area zone resets
#   sub-area resets on zone reset
#  z1000a-1_fault off
@code=qw(
  1  
  ) ;
Trk::ex_compile('subarea_reset','subarea zone reset',\@code) ;
trakbase::leading('AREA_reset','subarea_reset','subarea zone reset') ;

# panel system faults
#   fault panel control (redundancies)
AREA_fault_on_trail('AREA-1ss') ;
AREA_fault_on_trail('cr-n') ;
AREA_fault_on_trail('mcr-n') ;
AREA_fault_on_trail('cr-esna') ;

# AREA_system_ok?
#   dps that should cause area_ok change (redundancies)
@code=qw(
  1
  AREA-1ss   tst and
  cr-n      tst and
  mcr-n     tst and
  cr-esna   tst and
  ) ;
Trk::ex_compile('AREA_system_ok?','AREA system ok?',\@code) ;

# AREA_check
#   checks communications and system and changes area_ok
@code=qw(
  AREA_comm_ok?
  AREA_system_ok? and
    AREA_ok dp.value.set ) ;
Trk::ex_compile('AREA_check','AREA check',\@code) ;
trakbase::leading('tm_500ms','AREA_check','AREA check') ;

   
# logical area control
createControl('AREA',
              {run     => 'AREA_run',
               ok      => 'AREA_ok',
               on_reset=> 'AREA_on_reset',
               fault   => 'AREA_fault',
               alert   => 'AREA_alert',
               warning => 'AREA_warn',
               start   => 'AREA_start',
               stop    => 'AREA_stop',
               reset   => 'AREA_reset',
               red     => 'AREA_red',
               amber   => 'AREA_amber',
               green   => 'AREA_green',
               horn    => 'AREA_horn',
               start_button  => 'AREA_start_button',
               reset_button  => 'AREA_reset_button'}) ; 

# devices to link to logical control
@code=qw(
  AREA-1pb      tst
  extn_start   tst or 
  external_start tst or
    AREA_start dp.value.set 

  AREA-2pb      tst not
  extn_stop    tst or
  external_stop  tst or
    not AREA_stop dp.value.set

  AREA-3pb      tst
  extn_reset   tst or
  external_reset tst or
    AREA_reset dp.value.set 
 

  AREA_start_button tst
    dup AREA-1pbpl dp.value.set
    drop

  AREA_reset_button tst
    dup AREA-3pbpl dp.value.set
    drop

  AREA_horn tst
    dup AREAhrn-1 dp.value.set
    drop

  AREA_red tst
    dup AREAbcn-3 dp.value.set
    drop

  AREA_amber tst
    dup AREAbcn-2 dp.value.set
    drop

  AREA_green tst
    dup AREAbcn-1 dp.value.set
    drop
  ) ;
Trk::ex_compile('AREA_control','AREA control',\@code) ;
trakbase::leading('tm_100ms','AREA_control','system control') ;
trakbase::trailing('tm_100ms','AREA_control','system control') ;

1;
