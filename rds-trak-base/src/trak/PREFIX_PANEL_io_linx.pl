#!/bin/perl -I.

# CLIENT AREA I/O IDLinx

require Trk;
require trakbase;
require dp;
require rp;
require timer;

require linx ;
require "idlinx_motor.pl" ;

trakbase::message( 'CLIENT AREA I/O IDLinx' );

linx::add( 'ib1000a-1', { 'ip'=>'192.168.1.11', run=>'AREA_run',reset=>'AREA_reset'});
in( 'ib1000a-1_sns_a',        'snsAtest', 'Test for Sensor A',      { report=>'both' } );
in( 'ib1000a-1_sns_a_flt',    'fltAtest', 'Test for Fault A',       { report=>'both' } );
in( 'ib1000a-1_sns_b',        'snsBtest', 'Test for Sensor B',      { report=>'both' } );
in( 'ib1000a-1_sns_b_flt',    'fltBtest', 'Test for Fault B',       { report=>'both' } );
idlinx_motor( 'mdr1a', 'ib1000a-1', 'a', { dir=>'cw', run=>'AREA_run', reset=>'AREA_reset' } );
idlinx_motor( 'mdr1b', 'ib1000a-1', 'b', { dir=>'cw', run=>'AREA_run', reset=>'AREA_reset' } );
in( 'ib1000a-1_in1',          'inp1Test', 'Test for Input 1',       { report=>'both' } ); 
in( 'ib1000a-1_in2',          'inp2Test', 'Test for Input 2',       { report=>'both' } ); 
in( 'ib1000a-1_in3',          'inp3Test', 'Test for Input 3',       { report=>'both' } ); 
out( 'ib1000a-1_out1',        'out1Test', 'Test for Output 1',      { report=>'both' } ); 
out( 'ib1000a-1_out2',        'out2Test', 'Test for Output 2',      { report=>'both' } ); 
out( 'ib1000a-1_out3',        'out3Test', 'Test for Output 3',      { report=>'both' } ); 
out( 'ib1000a-1_out4',        'out4Test', 'Test for Output 4',      { report=>'both' } ); 
out( 'ib1000a-1_out5',        'out5Test', 'Test for Output 5',      { report=>'both' } ); 

1 ;
