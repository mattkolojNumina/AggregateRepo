#!/bin/perl -I.

# CLIENT AREA Trak System 

require Trk;
require trakbase;
require dp;
require rp;
require timer;

require "utility.pl";
require "control.pl";

trakbase::message( 'CLIENT AREA System' );

# external signals to control panel 
control( 'external_start','external start') ;
control( 'external_stop', 'external stop' ) ;
control( 'external_reset','external reset') ;

# external oneshots to control area
control( 'extn_start', 'external AREA start', {oneshot=>true} ) ;
control( 'extn_stop',  'external AREA stop',  {oneshot=>true} ) ;
control( 'extn_reset', 'external AREA reset', {oneshot=>true} ) ;

# area states for control status
control( 'AREA_fault', 'AREA system fault', {report=>both} );
control( 'AREA_alert', 'AREA system alert', {report=>both} );
control( 'AREA_warn',  'AREA system warning' );
control( 'AREA_ok',    'AREA ok',           {report=>'both'}) ;
control( 'AREA_run',   'AREA system run',   {report=>both} ) ;

# area states for area control
control( 'AREA_start', 'AREA system start', {report=>lead} );
control( 'AREA_stop',  'AREA system stop',  {report=>trail} );
control( 'AREA_reset', 'AREA system reset', {report=>lead} );

# area control for reset functions
control( 'AREA_on_reset', 'AREA on reset',  {report=>'lead'}) ;

# latch signals on area reset
latchoff( 'AREA_reset', 'AREA_alert' );
latchoff( 'AREA_reset', 'AREA_warn' );

# virtual signal for output control
dp::virtual('AREA_red','AREA red beacons') ;
dp::virtual('AREA_horn','AREA horns') ;
dp::virtual('AREA_start_button','AREA start button light') ;
dp::virtual('AREA_reset_button','AREA reset button light') ;

# dummy 
dp::virtual('dummy','dummy') ;

1;
