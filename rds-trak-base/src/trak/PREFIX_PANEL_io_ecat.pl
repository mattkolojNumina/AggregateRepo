#!/bin/perl -I.

# CLIENT AREA I/O EtherCat

require Trk;
require trakbase;
require dp;
require rp;
require timer;

require "utility.pl";
require ethercat;

trakbase::message( 'CLIENT AREA I/O Ethercat' );

# TEST SCRIPT FOR IN HOUSE VALIDATION
# AREA (rack 1) slot 0 (addr 0) EK1100 coupler
ethercat::add('EK1100', {prefix=>''});

# AREA (rack 1) slot 1 (addr 1) EL1018
ethercat::add('EL1018', {prefix=>'r1m1'});
in('r1m1i1',  'AREA-1ss',      'AREA Control Power',               {'report'=>'both'});
in('r1m1i2',  'AREA-1es',      'AREA E-Stop PB',                   {'report'=>'both'});
in('r1m1i3',  'AREA-1pb',      'AREA Start PB',                    {'report'=>'both'});
in('r1m1i4',  'AREA-2pb',      'AREA Stop PB',                     {'report'=>'both'});
in('r1m1i5',  'AREA-3pb',      'AREA Reset PB',                    {'report'=>'both'});
in('r1m1i6',  'cr-n',         'AREA Control Relay',               {'report'=>'both'});
in('r1m1i7',  'mcr-n',        'AREA Master Control Relay',        {'report'=>'both'});
in('r1m1i8',  'cr-esna',      'AREA E-Stop Relay',                {'report'=>'both'});

# AREA (rack 1) slot 2 (addr 2) EL2008
ethercat::add('EL2008', {prefix=>'r1m2'});
out('r1m2o1', 'AREAhrn-1',     'Test Output 1',            {'report'=>'both'});
out('r1m2o2', 'AREAbcn-3',     'Test Output 2',            {'report'=>'both'});
out('r1m2o3', 'AREAbcn-2',     'Test Output 3',            {'report'=>'both'});
out('r1m2o4', 'AREAbcn-1',     'Test Output 4',            {'report'=>'both'});
out('r1m2o5', 'AREA-1pbpl',    'Test Output 5',            {'report'=>'both'});
out('r1m2o6', 'AREA-3pbpl',    'Test Output 6',            {'report'=>'both'});
# out('r1m2o7', '',             '',                         {'report'=>'both'});
# out('r1m2o8', '',             '',                         {'report'=>'both'});

1 ;

